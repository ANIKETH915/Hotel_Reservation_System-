package service;

import dao.CustomerDao;
import dao.BookingDao;
import model.Customer;
import utils.ValidationUtil;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {

    private final CustomerDao customerDao = new CustomerDao();
    private final BookingDao bookingDao = new BookingDao();

    public List<Customer> list() throws SQLException {
        return customerDao.findAll();
    }

    public List<Customer> search(String query) throws SQLException {
        if (ValidationUtil.isBlank(query)) {
            return list();
        }
        return customerDao.search(query.trim());
    }

    public Customer get(int customerId) throws SQLException {
        return customerDao.findById(customerId);
    }

    public int add(Customer customer) throws SQLException {
        validateCustomer(customer, true);
        return customerDao.insert(customer);
    }

    public void update(Customer customer) throws SQLException {
        validateCustomer(customer, false);
        if (customerDao.findById(customer.getCustomerId()) == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        customerDao.update(customer);
    }

    public void delete(int customerId) throws SQLException {
        if (customerDao.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer not found");
        }
        if (bookingDao.countForCustomer(customerId) > 0) {
            throw new IllegalStateException(
                    "Customers with booking history cannot be deleted. Keep the record for audit and reporting."
            );
        }
        customerDao.delete(customerId);
    }

    private void validateCustomer(Customer customer, boolean isNew) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer is required");
        }
        if (!isNew && customer.getCustomerId() <= 0) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        ValidationUtil.require(customer.getFullName(), "Full name");
        String email = ValidationUtil.require(customer.getEmail(), "Email");
        if (!ValidationUtil.isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email address");
        }
        String phone = ValidationUtil.require(customer.getPhone(), "Phone");
        if (!ValidationUtil.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must be 10 digits");
        }
        customer.setEmail(email);
        customer.setPhone(phone);
    }
}
