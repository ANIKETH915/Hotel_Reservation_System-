package service;

import dao.BookingDao;
import dao.RoomDao;
import model.Room;
import model.RoomStatus;
import utils.ValidationUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class RoomService {

    private final RoomDao roomDao = new RoomDao();
    private final BookingDao bookingDao = new BookingDao();

    public List<Room> list() throws SQLException {
        return roomDao.findAll();
    }

    public List<Room> search(String query) throws SQLException {
        if (ValidationUtil.isBlank(query)) {
            return list();
        }
        return roomDao.search(query.trim());
    }

    public Room get(int roomId) throws SQLException {
        return roomDao.findById(roomId);
    }

    public int add(Room room) throws SQLException {
        validateRoom(room, true);
        if (room.getStatus() == null) {
            room.setStatus(RoomStatus.AVAILABLE);
        }
        return roomDao.insert(room);
    }

    public void update(Room room) throws SQLException {
        validateRoom(room, false);
        Room existing = roomDao.findById(room.getRoomId());
        if (existing == null) {
            throw new IllegalArgumentException("Room not found");
        }
        if (room.getStatus() == RoomStatus.BOOKED || room.getStatus() == RoomStatus.RESERVED) {
            if (room.getStatus() != existing.getStatus()) {
                throw new IllegalStateException("Booked/Reserved status is managed by the booking workflow");
            }
        }
        if (bookingDao.countActiveForRoom(room.getRoomId()) > 0
                && room.getStatus() != existing.getStatus()) {
            throw new IllegalStateException("Room status cannot change while it has active bookings");
        }
        roomDao.update(room);
    }

    public void delete(int roomId) throws SQLException {
        if (roomDao.findById(roomId) == null) {
            throw new IllegalArgumentException("Room not found");
        }
        if (bookingDao.countActiveForRoom(roomId) > 0) {
            throw new IllegalStateException("Cannot delete room with active bookings");
        }
        roomDao.delete(roomId);
    }

    public List<Room> listAvailable(LocalDate in, LocalDate out) throws SQLException {
        if (in == null || out == null || !out.isAfter(in)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        return roomDao.findAvailableForDates(in, out, null);
    }

    public void updateStatus(int roomId, RoomStatus status) throws SQLException {
        if (status == null) {
            throw new IllegalArgumentException("Status is required");
        }
        if (roomDao.findById(roomId) == null) {
            throw new IllegalArgumentException("Room not found");
        }
        if (status == RoomStatus.BOOKED || status == RoomStatus.RESERVED) {
            throw new IllegalStateException("Booked/Reserved status is set automatically by bookings");
        }
        if (status == RoomStatus.AVAILABLE && bookingDao.countActiveForRoom(roomId) > 0) {
            throw new IllegalStateException("Cannot mark available while the room has active bookings");
        }
        if ((status == RoomStatus.MAINTENANCE || status == RoomStatus.CLEANING)
                && bookingDao.countActiveForRoom(roomId) > 0) {
            throw new IllegalStateException("Cancel or complete active bookings before changing housekeeping status");
        }
        roomDao.updateStatus(roomId, status);
    }

    private void validateRoom(Room room, boolean isNew) {
        if (room == null) {
            throw new IllegalArgumentException("Room is required");
        }
        if (!isNew && room.getRoomId() <= 0) {
            throw new IllegalArgumentException("Room ID is required");
        }
        ValidationUtil.require(room.getRoomNumber(), "Room number");
        if (room.getRoomType() == null) {
            throw new IllegalArgumentException("Room type is required");
        }
        if (room.getFloor() < 0) {
            throw new IllegalArgumentException("Floor must be zero or greater");
        }
        if (room.getPrice() == null || room.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        if (room.getCapacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than zero");
        }
        if (room.getStatus() == null && !isNew) {
            throw new IllegalArgumentException("Status is required");
        }
    }
}
