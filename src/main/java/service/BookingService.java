package service;

import dao.BookingDao;
import dao.RoomDao;
import database.DatabaseConnection;
import model.Booking;
import model.BookingStatus;
import model.PaymentStatus;
import model.Room;
import model.RoomStatus;
import utils.DateUtil;
import utils.ValidationUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class BookingService {

    private final BookingDao bookingDao = new BookingDao();
    private final RoomDao roomDao = new RoomDao();

    public List<Booking> list() throws SQLException {
        return bookingDao.findAll();
    }

    public List<Booking> search(String query) throws SQLException {
        if (ValidationUtil.isBlank(query)) {
            return list();
        }
        return bookingDao.search(query.trim());
    }

    public Booking get(int bookingId) throws SQLException {
        return bookingDao.findById(bookingId);
    }

    public List<Booking> listByCustomer(int customerId) throws SQLException {
        return bookingDao.findByCustomer(customerId);
    }

    public List<Booking> listTodayCheckIns() throws SQLException {
        return bookingDao.findTodayCheckIns();
    }

    public List<Booking> listTodayCheckOuts() throws SQLException {
        return bookingDao.findTodayCheckOuts();
    }

    public List<Booking> listCreatedToday() throws SQLException {
        return bookingDao.findCreatedToday();
    }

    public List<Booking> listActiveToday() throws SQLException {
        return bookingDao.findActiveOnDate(LocalDate.now());
    }

    public int createBooking(int customerId, int roomId, LocalDate checkIn, LocalDate checkOut) throws SQLException {
        if (customerId <= 0) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (roomId <= 0) {
            throw new IllegalArgumentException("Room is required");
        }
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in cannot be in the past");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Room room = roomDao.findByIdForUpdate(conn, roomId);
                if (room == null) {
                    throw new IllegalArgumentException("Room not found");
                }
                if (room.getStatus() == RoomStatus.MAINTENANCE || room.getStatus() == RoomStatus.CLEANING) {
                    throw new IllegalStateException("Room is not available for booking");
                }
                if (bookingDao.existsOverlap(conn, roomId, checkIn, checkOut, null)) {
                    throw new IllegalStateException("Room is not available for the selected dates");
                }

                int days = DateUtil.nightsBetween(checkIn, checkOut);
                if (days <= 0) {
                    throw new IllegalArgumentException("Invalid booking duration");
                }
                BigDecimal total = room.getPrice().multiply(BigDecimal.valueOf(days));

                Booking booking = new Booking();
                booking.setCustomerId(customerId);
                booking.setRoomId(roomId);
                booking.setCheckIn(checkIn);
                booking.setCheckOut(checkOut);
                booking.setDays(days);
                booking.setTotalAmount(total);
                booking.setBookingStatus(BookingStatus.CONFIRMED);
                booking.setPaymentStatus(PaymentStatus.PENDING);

                int bookingId = bookingDao.insert(conn, booking);
                roomDao.updateStatus(conn, roomId, RoomStatus.RESERVED);
                conn.commit();
                return bookingId;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new SQLException("Failed to create booking", e);
            }
        }
    }

    public void checkIn(int bookingId) throws SQLException {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed bookings can be checked in");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                bookingDao.updateStatus(conn, bookingId, BookingStatus.CHECKED_IN);
                roomDao.updateStatus(conn, booking.getRoomId(), RoomStatus.BOOKED);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Failed to check in", e);
            }
        }
    }

    public void checkOut(int bookingId) throws SQLException {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            throw new IllegalStateException("Only checked-in bookings can be checked out");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                bookingDao.updateStatus(conn, bookingId, BookingStatus.CHECKED_OUT);
                roomDao.updateStatus(conn, booking.getRoomId(), RoomStatus.CLEANING);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Failed to check out", e);
            }
        }
    }

    public void cancel(int bookingId) throws SQLException {
        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        if (booking.getBookingStatus() == BookingStatus.CANCELLED
                || booking.getBookingStatus() == BookingStatus.CHECKED_OUT) {
            throw new IllegalStateException("Booking cannot be cancelled");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                bookingDao.updateStatus(conn, bookingId, BookingStatus.CANCELLED);
                if (booking.getPaymentStatus() == PaymentStatus.PAID
                        || booking.getPaymentStatus() == PaymentStatus.PARTIAL) {
                    bookingDao.updatePaymentStatus(conn, bookingId, PaymentStatus.REFUNDED);
                }
                roomDao.updateStatus(conn, booking.getRoomId(), RoomStatus.AVAILABLE);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Failed to cancel booking", e);
            }
        }
    }
}
