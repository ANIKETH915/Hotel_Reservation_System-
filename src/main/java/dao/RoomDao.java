package dao;

import database.DatabaseConnection;
import model.Room;
import model.RoomStatus;
import model.RoomType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDao {

    public List<Room> findAll() throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Room> rooms = new ArrayList<>();
            while (rs.next()) {
                rooms.add(mapRow(rs));
            }
            return rooms;
        }
    }

    public Room findById(int roomId) throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public Room findByIdForUpdate(Connection conn, int roomId) throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Room> findByStatus(RoomStatus status) throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms WHERE status = ? ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getLabel());
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
                return rooms;
            }
        }
    }

    public List<Room> findAvailableForDates(LocalDate checkIn, LocalDate checkOut, Integer excludeBookingId)
            throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms "
                + "WHERE NOT EXISTS ( "
                + "  SELECT 1 FROM bookings b "
                + "  WHERE b.room_id = rooms.room_id "
                + "    AND b.booking_status NOT IN ('Cancelled','Checked Out') "
                + "    AND b.check_in < ? AND b.check_out > ? "
                + "    AND (? IS NULL OR b.booking_id <> ?) "
                + ") "
                + "AND status NOT IN ('Maintenance','Cleaning') "
                + "ORDER BY room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            if (excludeBookingId == null) {
                ps.setNull(3, Types.INTEGER);
                ps.setNull(4, Types.INTEGER);
            } else {
                ps.setInt(3, excludeBookingId);
                ps.setInt(4, excludeBookingId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
                return rooms;
            }
        }
    }

    public List<Room> search(String query) throws SQLException {
        String sql = "SELECT room_id, room_number, room_type, floor, price, capacity, status, image_path "
                + "FROM rooms "
                + "WHERE room_number LIKE ? OR room_type LIKE ? OR CAST(floor AS CHAR) LIKE ? "
                + "ORDER BY room_number";
        String pattern = "%" + query.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                List<Room> rooms = new ArrayList<>();
                while (rs.next()) {
                    rooms.add(mapRow(rs));
                }
                return rooms;
            }
        }
    }

    public int insert(Room room) throws SQLException {
        String sql = "INSERT INTO rooms (room_number, room_type, floor, price, capacity, status, image_path) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindRoom(ps, room);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert room");
    }

    public void update(Room room) throws SQLException {
        String sql = "UPDATE rooms SET room_number = ?, room_type = ?, floor = ?, price = ?, "
                + "capacity = ?, status = ?, image_path = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindRoom(ps, room);
            ps.setInt(8, room.getRoomId());
            ps.executeUpdate();
        }
    }

    public void delete(int roomId) throws SQLException {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.executeUpdate();
        }
    }

    public int countByStatus(RoomStatus status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getLabel());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public void updateStatus(int roomId, RoomStatus status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateStatus(conn, roomId, status);
        }
    }

    public void updateStatus(Connection conn, int roomId, RoomStatus status) throws SQLException {
        String sql = "UPDATE rooms SET status = ? WHERE room_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getLabel());
            ps.setInt(2, roomId);
            ps.executeUpdate();
        }
    }

    private void bindRoom(PreparedStatement ps, Room room) throws SQLException {
        ps.setString(1, room.getRoomNumber());
        ps.setString(2, room.getRoomType().getLabel());
        ps.setInt(3, room.getFloor());
        ps.setBigDecimal(4, room.getPrice());
        ps.setInt(5, room.getCapacity());
        ps.setString(6, room.getStatus().getLabel());
        if (room.getImagePath() != null) {
            ps.setString(7, room.getImagePath());
        } else {
            ps.setNull(7, Types.VARCHAR);
        }
    }

    private Room mapRow(ResultSet rs) throws SQLException {
        Room room = new Room();
        room.setRoomId(rs.getInt("room_id"));
        room.setRoomNumber(rs.getString("room_number"));
        room.setRoomType(RoomType.fromLabel(rs.getString("room_type")));
        room.setFloor(rs.getInt("floor"));
        room.setPrice(rs.getBigDecimal("price"));
        room.setCapacity(rs.getInt("capacity"));
        room.setStatus(RoomStatus.fromLabel(rs.getString("status")));
        room.setImagePath(rs.getString("image_path"));
        return room;
    }
}
