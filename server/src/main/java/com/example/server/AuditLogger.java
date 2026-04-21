package com.example.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * Handles audit logging for all significant operations in the system.
 */
public class AuditLogger {

    /**
     * Log an operation to the audit trail.
     *
     * @param userId      ID of the user performing the action (null for anonymous)
     * @param username    Username of the actor
     * @param action      Type of action (LOGIN, LOGOUT, etc.)
     * @param resource    Resource affected (e.g., "user:admin")
     * @param details     Additional details about the operation
     * @param ipAddress   IP address of the client
     * @param status      Status of the operation (SUCCESS, FAILED, etc.)
     */
    public static void log(Integer userId, String username, String action, String resource, 
                          String details, String ipAddress, String status) {
        new Thread(() -> {
            String sql = "INSERT INTO audit_logs (user_id, username, action, resource, details, ip_address, status) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setObject(1, userId);
                pstmt.setString(2, username != null ? username : "ANONYMOUS");
                pstmt.setString(3, action);
                pstmt.setString(4, resource);
                pstmt.setString(5, details);
                pstmt.setString(6, ipAddress);
                pstmt.setString(7, status);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                ConsoleColors.println(ConsoleColors.RED, "[!] Audit log error: " + e.getMessage());
            }
        }).start();
    }

    /**
     * Log a login attempt.
     */
    public static void logLogin(Integer userId, String username, String ipAddress, boolean success) {
        log(userId, username, "LOGIN", "user:" + username, 
            "Login attempt", ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log a logout.
     */
    public static void logLogout(Integer userId, String username, String ipAddress) {
        log(userId, username, "LOGOUT", "user:" + username, 
            "User logged out", ipAddress, "SUCCESS");
    }

    /**
     * Log a registration.
     */
    public static void logRegistration(String username, String ipAddress, boolean success) {
        log(null, username, "REGISTER", "user:" + username, 
            "New user registration", ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log a password change.
     */
    public static void logPasswordChange(Integer userId, String username, String ipAddress, boolean success) {
        log(userId, username, "CHANGE_PASSWORD", "user:" + username, 
            "Password changed", ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log a permission denied event.
     */
    public static void logPermissionDenied(Integer userId, String username, String command, String ipAddress) {
        log(userId, username, "PERMISSION_DENIED", "command:" + command, 
            "Insufficient permissions for command: " + command, ipAddress, "DENIED");
    }

    /**
     * Log user role assignment.
     */
    public static void logRoleAssignment(Integer adminId, String adminName, String targetUser, 
                                        String newRole, String ipAddress, boolean success) {
        log(adminId, adminName, "ASSIGN_ROLE", "user:" + targetUser, 
            "Assigned role " + newRole + " to user " + targetUser, ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log user suspension.
     */
    public static void logUserSuspension(Integer adminId, String adminName, String targetUser, 
                                        String ipAddress, boolean success) {
        log(adminId, adminName, "SUSPEND_USER", "user:" + targetUser, 
            "User suspended: " + targetUser, ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log user deletion.
     */
    public static void logUserDeletion(Integer adminId, String adminName, String targetUser, 
                                      String ipAddress, boolean success) {
        log(adminId, adminName, "DELETE_USER", "user:" + targetUser, 
            "User deleted: " + targetUser, ipAddress, success ? "SUCCESS" : "FAILED");
    }

    /**
     * Log a generic admin action.
     */
    public static void logAdminAction(Integer adminId, String adminName, String action, 
                                     String resource, String details, String ipAddress, boolean success) {
        log(adminId, adminName, "ADMIN_" + action, resource, details, ipAddress, success ? "SUCCESS" : "FAILED");
    }
}
