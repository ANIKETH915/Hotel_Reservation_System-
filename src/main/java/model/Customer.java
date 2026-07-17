package model;

import java.time.LocalDateTime;

public class Customer {
    private int customerId;
    private String fullName;
    private String email;
    private String phone;
    private String address;
    private String idProof;
    private LocalDateTime createdAt;

    public Customer() {
    }

    public Customer(String fullName, String email, String phone, String address, String idProof) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.idProof = idProof;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIdProof() {
        return idProof;
    }

    public void setIdProof(String idProof) {
        this.idProof = idProof;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return fullName + " (" + email + ")";
    }
}
