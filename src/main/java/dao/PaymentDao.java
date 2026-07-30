package dao;

import database.DatabaseConnection;
import model.Payment;
import model.PaymentMethod;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDao {

    private static final String BASE_SELECT = "SELECT p.payment_id, p.booking_id, p.payment_method, p.amount, "
            + "p.payment_date, p.transaction_id, c.full_name AS customer_name, r.room_number ";

    public List<Payment> findAll() throws SQLException {
        String sql = BASE_SELECT
                + "FROM payments p "
                + "JOIN bookings b ON p.booking_id = b.booking_id "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "ORDER BY p.payment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Payment> payments = new ArrayList<>();
            while (rs.next()) {
                payments.add(mapRow(rs));
            }
            return payments;
        }
    }

    public List<Payment> findByBooking(int bookingId) throws SQLException {
        String sql = BASE_SELECT
                + "FROM payments p "
                + "JOIN bookings b ON p.booking_id = b.booking_id "
                + "JOIN customers c ON b.customer_id = c.customer_id "
                + "JOIN rooms r ON b.room_id = r.room_id "
                + "WHERE p.booking_id = ? "
                + "ORDER BY p.payment_date DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> payments = new ArrayList<>();
                while (rs.next()) {
                    payments.add(mapRow(rs));
                }
                return payments;
            }
        }
    }

    public int insert(Payment payment) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return insert(conn, payment);
        }
    }

    public int insert(Connection conn, Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, payment_method, amount, payment_date, transaction_id) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getBookingId());
            ps.setString(2, payment.getPaymentMethod().getLabel());
            ps.setBigDecimal(3, payment.getAmount());
            if (payment.getPaymentDate() != null) {
                ps.setTimestamp(4, Timestamp.valueOf(payment.getPaymentDate()));
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            }
            ps.setString(5, payment.getTransactionId());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert payment");
    }

    public BigDecimal sumToday() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE date(payment_date) = date('now', 'localtime')";
        return sumQuery(sql);
    }

    public BigDecimal sumThisMonth() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments "
                + "WHERE strftime('%Y-%m', payment_date) = strftime('%Y-%m', 'now', 'localtime')";
        return sumQuery(sql);
    }

    public BigDecimal sumBetween(LocalDate start, LocalDate end) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments "
                + "WHERE date(payment_date) >= ? AND date(payment_date) <= ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(start));
            ps.setDate(2, java.sql.Date.valueOf(end));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal sumPaidForBooking(int bookingId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            return sumPaidForBooking(conn, bookingId);
        }
    }

    public BigDecimal sumPaidForBooking(Connection conn, int bookingId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM payments WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal sumQuery(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        }
        return BigDecimal.ZERO;
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setPaymentId(rs.getInt("payment_id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setPaymentMethod(PaymentMethod.fromLabel(rs.getString("payment_method")));
        payment.setAmount(rs.getBigDecimal("amount"));
        Timestamp paymentDate = rs.getTimestamp("payment_date");
        if (paymentDate != null) {
            payment.setPaymentDate(paymentDate.toLocalDateTime());
        }
        payment.setTransactionId(rs.getString("transaction_id"));
        payment.setCustomerName(rs.getString("customer_name"));
        payment.setRoomNumber(rs.getString("room_number"));
        return payment;
    }
}
