package service;

import dao.BookingDao;
import dao.CustomerDao;
import dao.PaymentDao;
import dao.RoomDao;
import database.DatabaseConnection;
import model.DashboardStats;
import model.RoomStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardService {

    private final BookingDao bookingDao = new BookingDao();
    private final CustomerDao customerDao = new CustomerDao();
    private final PaymentDao paymentDao = new PaymentDao();
    private final SettingsService settingsService = new SettingsService();

    public DashboardStats loadStats() throws SQLException {
        DashboardStats stats = new DashboardStats();

        // Single GROUP BY instead of one query per RoomStatus
        int totalRooms = 0;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT status, COUNT(*) AS cnt FROM rooms GROUP BY status");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int count = rs.getInt("cnt");
                totalRooms += count;
                RoomStatus status = RoomStatus.fromLabel(rs.getString("status"));
                switch (status) {
                    case AVAILABLE -> stats.setAvailableRooms(count);
                    case BOOKED -> stats.setOccupiedRooms(count);
                    case RESERVED -> stats.setReservedRooms(count);
                    case MAINTENANCE -> stats.setMaintenanceRooms(count);
                    case CLEANING -> stats.setCleaningRooms(count);
                    default -> { }
                }
            }
        }
        stats.setTotalRooms(totalRooms);

        int inUse = stats.getOccupiedRooms() + stats.getReservedRooms();
        if (totalRooms > 0) {
            double rate = (double) inUse / totalRooms * 100.0;
            stats.setOccupancyRate(BigDecimal.valueOf(rate).setScale(1, RoundingMode.HALF_UP).doubleValue());
        } else {
            stats.setOccupancyRate(0.0);
        }

        // Lightweight COUNT queries (no full row materialization)
        stats.setTodayBookings(countSql(
                "SELECT COUNT(*) FROM bookings WHERE date(created_at) = date('now', 'localtime')"));
        stats.setTodayCheckIns(countSql(
                "SELECT COUNT(*) FROM bookings WHERE check_in = date('now', 'localtime') "
                        + "AND booking_status NOT IN ('Cancelled','Checked Out')"));
        stats.setTodayCheckOuts(countSql(
                "SELECT COUNT(*) FROM bookings WHERE check_out = date('now', 'localtime') "
                        + "AND booking_status IN ('Confirmed','Checked In')"));
        stats.setTodayRevenue(paymentDao.sumToday());
        stats.setMonthRevenue(paymentDao.sumThisMonth());
        stats.setTotalCustomers(customerDao.count());
        stats.setVipCustomers(customerDao.countVip(settingsService.getVipThreshold()));

        return stats;
    }

    private int countSql(String sql) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
}
