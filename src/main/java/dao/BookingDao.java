package dao;

import database.DatabaseConnection;
import model.Booking;
import model.BookingStatus;
import model.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingDao {

    private static final String BASE_SELECT = "SELECT b.booking_id, b.customer_id, b.room_id, b.check_in, b.check_out, "
            + "b.days, b.total_amount, b.booking_status, b.payment_status, b.created_at, "
            + "c.full_name AS customer_name, r.room_number, r.room_type AS room_type_label ";

    public List<Booking> findAll() throws SQLException {
        String sql = BASE_SELECT
                + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "ORDER BY b.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Booking> bookings = new ArrayList<>();
            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
            return bookings;
        }
    }

    public Booking findById(int bookingId) throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.booking_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Booking> findByCustomer(int customerId) throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.customer_id = ? "
                + "ORDER BY b.created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
                return bookings;
            }
        }
    }

    public List<Booking> search(String query) throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE c.full_name LIKE ? OR r.room_number LIKE ? OR b.booking_status LIKE ? "
                + "OR CAST(b.booking_id AS CHAR) LIKE ? "
                + "ORDER BY b.created_at DESC";
        String pattern = "%" + query.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
                return bookings;
            }
        }
    }

    public List<Booking> findTodayCheckIns() throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.check_in = CURDATE() "
                + "AND b.booking_status NOT IN ('Cancelled','Checked Out') "
                + "ORDER BY c.full_name";
        return queryList(sql);
    }

    public List<Booking> findTodayCheckOuts() throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.check_out = CURDATE() "
                + "AND b.booking_status IN ('Confirmed','Checked In') "
                + "ORDER BY c.full_name";
        return queryList(sql);
    }

    public List<Booking> findCreatedToday() throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE DATE(b.created_at) = CURDATE() "
                + "ORDER BY b.created_at DESC";
        return queryList(sql);
    }

    public List<Booking> findActiveOnDate(LocalDate date) throws SQLException {
        String sql = BASE_SELECT + "FROM bookings b "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE b.booking_status NOT IN ('Cancelled','Checked Out') "
                + "AND b.check_in <= ? AND b.check_out > ? "
                + "ORDER BY b.check_in, r.room_number";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(date));
            ps.setDate(2, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                List<Booking> bookings = new ArrayList<>();
                while (rs.next()) {
                    bookings.add(mapRow(rs));
                }
                return bookings;
            }
        }
    }

    public int insert(Booking booking) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, booking);
        }
    }

    public int insert(Connection conn, Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (customer_id, room_id, check_in, check_out, days, total_amount, "
                + "booking_status, payment_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, booking.getCustomerId());
            ps.setInt(2, booking.getRoomId());
            ps.setDate(3, Date.valueOf(booking.getCheckIn()));
            ps.setDate(4, Date.valueOf(booking.getCheckOut()));
            ps.setInt(5, booking.getDays());
            ps.setBigDecimal(6, booking.getTotalAmount());
            ps.setString(7, booking.getBookingStatus().getLabel());
            ps.setString(8, booking.getPaymentStatus().getLabel());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert booking");
    }

    public void updateStatus(int bookingId, BookingStatus status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updateStatus(conn, bookingId, status);
        }
    }

    public void updateStatus(Connection conn, int bookingId, BookingStatus status) throws SQLException {
        String sql = "UPDATE bookings SET booking_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getLabel());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    public void updatePaymentStatus(int bookingId, PaymentStatus status) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            updatePaymentStatus(conn, bookingId, status);
        }
    }

    public void updatePaymentStatus(Connection conn, int bookingId, PaymentStatus status) throws SQLException {
        String sql = "UPDATE bookings SET payment_status = ? WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.getLabel());
            ps.setInt(2, bookingId);
            ps.executeUpdate();
        }
    }

    public boolean existsOverlap(int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeId)
            throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return existsOverlap(conn, roomId, checkIn, checkOut, excludeId);
        }
    }

    public boolean existsOverlap(Connection conn, int roomId, LocalDate checkIn, LocalDate checkOut, Integer excludeId)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings "
                + "WHERE room_id = ? "
                + "AND booking_status NOT IN ('Cancelled','Checked Out') "
                + "AND check_in < ? AND check_out > ? "
                + "AND (? IS NULL OR booking_id <> ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            if (excludeId == null) {
                ps.setNull(4, Types.INTEGER);
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(4, excludeId);
                ps.setInt(5, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public int countActiveForRoom(int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings "
                + "WHERE room_id = ? AND booking_status NOT IN ('Cancelled','Checked Out')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    public int countForCustomer(int customerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private List<Booking> queryList(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Booking> bookings = new ArrayList<>();
            while (rs.next()) {
                bookings.add(mapRow(rs));
            }
            return bookings;
        }
    }

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setBookingId(rs.getInt("booking_id"));
        booking.setCustomerId(rs.getInt("customer_id"));
        booking.setRoomId(rs.getInt("room_id"));
        Date checkIn = rs.getDate("check_in");
        if (checkIn != null) {
            booking.setCheckIn(checkIn.toLocalDate());
        }
        Date checkOut = rs.getDate("check_out");
        if (checkOut != null) {
            booking.setCheckOut(checkOut.toLocalDate());
        }
        booking.setDays(rs.getInt("days"));
        booking.setTotalAmount(rs.getBigDecimal("total_amount"));
        booking.setBookingStatus(BookingStatus.fromLabel(rs.getString("booking_status")));
        booking.setPaymentStatus(PaymentStatus.fromLabel(rs.getString("payment_status")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            booking.setCreatedAt(createdAt.toLocalDateTime());
        }
        booking.setCustomerName(rs.getString("customer_name"));
        booking.setRoomNumber(rs.getString("room_number"));
        booking.setRoomType(rs.getString("room_type_label"));
        return booking;
    }
}
