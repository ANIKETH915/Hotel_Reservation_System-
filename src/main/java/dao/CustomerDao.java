package dao;

import database.DatabaseConnection;
import model.Customer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class CustomerDao {

    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT customer_id, full_name, email, phone, address, id_proof, created_at "
                + "FROM customers ORDER BY full_name";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
            return customers;
        }
    }

    public Customer findById(int customerId) throws SQLException {
        String sql = "SELECT customer_id, full_name, email, phone, address, id_proof, created_at "
                + "FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        }
        return null;
    }

    public List<Customer> search(String query) throws SQLException {
        String sql = "SELECT customer_id, full_name, email, phone, address, id_proof, created_at "
                + "FROM customers "
                + "WHERE full_name LIKE ? OR email LIKE ? OR phone LIKE ? OR address LIKE ? "
                + "ORDER BY full_name";
        String pattern = "%" + query.trim() + "%";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                List<Customer> customers = new ArrayList<>();
                while (rs.next()) {
                    customers.add(mapRow(rs));
                }
                return customers;
            }
        }
    }

    public int insert(Customer customer) throws SQLException {
        String sql = "INSERT INTO customers (full_name, email, phone, address, id_proof) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindCustomer(ps, customer);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to insert customer");
    }

    public void update(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET full_name = ?, email = ?, phone = ?, address = ?, id_proof = ? "
                + "WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindCustomer(ps, customer);
            ps.setInt(6, customer.getCustomerId());
            ps.executeUpdate();
        }
    }

    public void delete(int customerId) throws SQLException {
        String sql = "DELETE FROM customers WHERE customer_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerId);
            ps.executeUpdate();
        }
    }

    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM customers";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countVip(int threshold) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ( "
                + "  SELECT customer_id FROM bookings "
                + "  WHERE booking_status NOT IN ('Cancelled') "
                + "  GROUP BY customer_id "
                + "  HAVING COUNT(*) >= ? "
                + ") vip";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, threshold);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return 0;
    }

    private void bindCustomer(PreparedStatement ps, Customer customer) throws SQLException {
        ps.setString(1, customer.getFullName());
        ps.setString(2, customer.getEmail());
        ps.setString(3, customer.getPhone());
        if (customer.getAddress() != null) {
            ps.setString(4, customer.getAddress());
        } else {
            ps.setNull(4, Types.VARCHAR);
        }
        if (customer.getIdProof() != null) {
            ps.setString(5, customer.getIdProof());
        } else {
            ps.setNull(5, Types.VARCHAR);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getInt("customer_id"));
        customer.setFullName(rs.getString("full_name"));
        customer.setEmail(rs.getString("email"));
        customer.setPhone(rs.getString("phone"));
        customer.setAddress(rs.getString("address"));
        customer.setIdProof(rs.getString("id_proof"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            customer.setCreatedAt(createdAt.toLocalDateTime());
        }
        return customer;
    }
}
