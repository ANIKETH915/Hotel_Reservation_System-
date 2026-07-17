package model;

import java.time.LocalDateTime;

public class Admin {
    private int adminId;
    private String username;
    private String passwordHash;
    private String salt;
    private String fullName;
    private String securityAnswerHash;
    private String rememberTokenHash;
    private LocalDateTime rememberTokenExpires;
    private boolean active;
    private LocalDateTime lastLogin;

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSecurityAnswerHash() {
        return securityAnswerHash;
    }

    public void setSecurityAnswerHash(String securityAnswerHash) {
        this.securityAnswerHash = securityAnswerHash;
    }

    public String getRememberTokenHash() {
        return rememberTokenHash;
    }

    public void setRememberTokenHash(String rememberTokenHash) {
        this.rememberTokenHash = rememberTokenHash;
    }

    public LocalDateTime getRememberTokenExpires() {
        return rememberTokenExpires;
    }

    public void setRememberTokenExpires(LocalDateTime rememberTokenExpires) {
        this.rememberTokenExpires = rememberTokenExpires;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }
}
