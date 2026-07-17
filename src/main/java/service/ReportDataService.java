package service;

import dao.BookingDao;
import dao.CustomerDao;
import dao.PaymentDao;
import dao.RoomDao;
import database.DatabaseConnection;
import model.Booking;
import model.Customer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportDataService {

    private final PaymentDao paymentDao = new PaymentDao();
    private final BookingDao bookingDao = new BookingDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final RoomDao roomDao = new RoomDao();
    private final SettingsService settingsService = new SettingsService();

    public BigDecimal dailyRevenue(LocalDate date) throws SQLException {
        return paymentDao.sumBetween(date, date);
    }

    public BigDecimal monthlyRevenue(YearMonth month) throws SQLException {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        return paymentDao.sumBetween(start, end);
    }

    public Map<String, Double> roomUtilization(LocalDate start, LocalDate end) throws SQLException {
        long totalDays = end.toEpochDay() - start.toEpochDay() + 1;
        if (totalDays <= 0) {
            return Map.of();
        }

        Map<String, Double> utilization = new LinkedHashMap<>();
        String sql = "SELECT r.room_number, COALESCE(SUM(DATEDIFF(LEAST(b.check_out, ?), GREATEST(b.check_in, ?))), 0) AS occupied_days "
                + "FROM rooms r "
                + "LEFT JOIN bookings b ON r.room_id = b.room_id "
                + "  AND b.booking_status NOT IN ('Cancelled') "
                + "  AND b.check_in <= ? AND b.check_out >= ? "
                + "GROUP BY r.room_id, r.room_number "
                + "ORDER BY r.room_number";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            java.sql.Date endDate = java.sql.Date.valueOf(end.plusDays(1));
            java.sql.Date startDate = java.sql.Date.valueOf(start);
            ps.setDate(1, endDate);
            ps.setDate(2, startDate);
            ps.setDate(3, java.sql.Date.valueOf(end));
            ps.setDate(4, startDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long occupiedDays = rs.getLong("occupied_days");
                    double rate = (double) occupiedDays / totalDays * 100.0;
                    utilization.put(rs.getString("room_number"),
                            BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
                }
            }
        }
        return utilization;
    }

    public List<Map<String, Object>> customerReport() throws SQLException {
        int threshold = settingsService.getVipThreshold();
        List<Map<String, Object>> report = new ArrayList<>();

        String sql = "SELECT c.customer_id, c.full_name, c.email, c.phone, "
                + "COUNT(b.booking_id) AS booking_count, "
                + "COALESCE(SUM(b.total_amount), 0) AS total_spent "
                + "FROM customers c "
                + "LEFT JOIN bookings b ON c.customer_id = b.customer_id "
                + "  AND b.booking_status NOT IN ('Cancelled') "
                + "GROUP BY c.customer_id, c.full_name, c.email, c.phone "
                + "ORDER BY booking_count DESC, c.full_name";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("customerId", rs.getInt("customer_id"));
                row.put("fullName", rs.getString("full_name"));
                row.put("email", rs.getString("email"));
                row.put("phone", rs.getString("phone"));
                int bookingCount = rs.getInt("booking_count");
                row.put("bookingCount", bookingCount);
                row.put("totalSpent", rs.getBigDecimal("total_spent"));
                row.put("vip", bookingCount >= threshold);
                report.add(row);
            }
        }
        return report;
    }

    public List<Customer> listAllCustomers() throws SQLException {
        return customerDao.findAll();
    }

    public List<Booking> listAllBookings() throws SQLException {
        return bookingDao.findAll();
    }

    public int totalRoomCount() throws SQLException {
        return roomDao.findAll().size();
    }
}
