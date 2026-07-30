package service;

import dao.AdminDao;
import model.Admin;
import utils.PasswordUtil;
import utils.SessionManager;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class AuthService {

    private static final int REMEMBER_DAYS = 30;

    private final AdminDao adminDao = new AdminDao();

    public Admin login(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        Admin admin = adminDao.findByUsername(username.trim());
        if (admin == null || !admin.isActive()) {
            return null;
        }
        if (!PasswordUtil.verify(password, admin.getSalt(), admin.getPasswordHash())) {
            return null;
        }
        adminDao.updateLastLogin(admin.getAdminId());
        admin.setLastLogin(LocalDateTime.now());
        SessionManager.setCurrentAdmin(admin);
        return admin;
    }

    public void logout() throws SQLException {
        Admin admin = SessionManager.getCurrentAdmin();
        if (admin != null) {
            disableRememberMe(admin.getAdminId());
        }
        SessionManager.clear();
    }

    public Admin tryRememberMe() throws SQLException {
        String token = SessionManager.loadRememberToken();
        if (token == null || token.isBlank()) {
            return null;
        }
        String tokenHash = PasswordUtil.sha256(token);
        Admin admin = adminDao.findByRememberTokenHash(tokenHash);
        if (admin == null || !admin.isActive()) {
            SessionManager.clearRememberToken();
            return null;
        }
        adminDao.updateLastLogin(admin.getAdminId());
        admin.setLastLogin(LocalDateTime.now());
        SessionManager.setCurrentAdmin(admin);
        return admin;
    }

    public void enableRememberMe(int adminId) throws SQLException {
        String token = PasswordUtil.generateToken();
        String tokenHash = PasswordUtil.sha256(token);
        LocalDateTime expires = LocalDateTime.now().plusDays(REMEMBER_DAYS);
        adminDao.updateRememberToken(adminId, tokenHash, expires);
        SessionManager.saveRememberToken(token);
    }

    public void disableRememberMe(int adminId) throws SQLException {
        adminDao.clearRememberToken(adminId);
        SessionManager.clearRememberToken();
    }

    public boolean resetPassword(String username, String securityAnswer, String newPassword) throws SQLException {
        if (username == null || username.isBlank() || securityAnswer == null || securityAnswer.isBlank()
                || newPassword == null || newPassword.isBlank()) {
            return false;
        }
        Admin admin = adminDao.findByUsername(username.trim());
        if (admin == null || !admin.isActive()) {
            return false;
        }
        if (!PasswordUtil.verify(securityAnswer, admin.getSalt(), admin.getSecurityAnswerHash())) {
            return false;
        }
        // Keep the existing salt so the verified security answer remains valid.
        String passwordHash = PasswordUtil.hashPassword(newPassword, admin.getSalt());
        adminDao.updatePassword(admin.getAdminId(), passwordHash, admin.getSalt());
        return true;
    }

    public boolean hasAdmins() throws SQLException {
        return adminDao.hasAdmins();
    }

    public void registerAdmin(String username, String password, String fullName, String securityAnswer) throws SQLException {
        String salt = PasswordUtil.generateSalt();
        String passwordHash = PasswordUtil.hashPassword(password, salt);
        String answerHash = PasswordUtil.hashPassword(securityAnswer, salt);
        adminDao.register(username, passwordHash, salt, fullName, answerHash);
    }
}
