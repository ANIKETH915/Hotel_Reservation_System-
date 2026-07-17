package service;

import dao.BookingDao;
import dao.PaymentDao;
import database.DatabaseConnection;
import model.Booking;
import model.Payment;
import model.PaymentMethod;
import model.PaymentStatus;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

public class PaymentService {

    private final PaymentDao paymentDao = new PaymentDao();
    private final BookingDao bookingDao = new BookingDao();

    public List<Payment> list() throws SQLException {
        return paymentDao.findAll();
    }

    public int processPayment(int bookingId, PaymentMethod method, BigDecimal amount) throws SQLException {
        if (bookingId <= 0) {
            throw new IllegalArgumentException("Booking is required");
        }
        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Booking booking = bookingDao.findById(bookingId);
        if (booking == null) {
            throw new IllegalArgumentException("Booking not found");
        }
        if (booking.getBookingStatus() == model.BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot process payment for cancelled booking");
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                BigDecimal paidSoFar = paymentDao.sumPaidForBooking(conn, bookingId);
                BigDecimal remaining = booking.getTotalAmount().subtract(paidSoFar);
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalStateException("This booking has already been paid in full");
                }
                if (amount.compareTo(remaining) > 0) {
                    throw new IllegalArgumentException("Payment exceeds the remaining balance of " + remaining);
                }

                Payment payment = new Payment();
                payment.setBookingId(bookingId);
                payment.setPaymentMethod(method);
                payment.setAmount(amount);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setTransactionId(generateTransactionId());

                int paymentId = paymentDao.insert(conn, payment);

                BigDecimal totalPaid = paymentDao.sumPaidForBooking(conn, bookingId);
                PaymentStatus newStatus;
                if (totalPaid.compareTo(booking.getTotalAmount()) >= 0) {
                    newStatus = PaymentStatus.PAID;
                } else {
                    newStatus = PaymentStatus.PARTIAL;
                }
                bookingDao.updatePaymentStatus(conn, bookingId, newStatus);

                conn.commit();
                return paymentId;
            } catch (Exception e) {
                conn.rollback();
                if (e instanceof SQLException) {
                    throw (SQLException) e;
                }
                throw new SQLException("Failed to process payment", e);
            }
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
