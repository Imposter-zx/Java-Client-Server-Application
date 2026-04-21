package com.example.server;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles individual client connections and processes commands with RBAC.
 * Supports: LOGIN, STATUS, REGISTER, LOGOUT, CHANGE_PASSWORD, EXIT, and ADMIN commands.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String authenticatedUser = null;
    private Integer userId = null;
    private Role userRole = null;
    private final String sessionId;
    private long connectionTime;
    private long lastActivity;
    private final String ipAddress;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.connectionTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
        this.ipAddress = socket.getInetAddress().getHostAddress();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            
            NotificationManager.registerClient(this, "general");
            
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                this.lastActivity = System.currentTimeMillis();
                String[] parts = inputLine.split(" ", 4);
                String command = parts[0].toUpperCase();

                switch (command) {
                    case "LOGIN":
                        if (parts.length < 3) {
                            out.println("ERROR: Usage LOGIN <user> <pass>");
                        } else {
                            handleLogin(parts[1], parts[2], out);
                        }
                        break;

                    case "STATUS":
                        if (!checkPermission(Permission.VIEW_PROFILE, out)) break;
                        handleStatus(out);
                        break;

                    case "REGISTER":
                        if (parts.length < 3) {
                            out.println("ERROR: Usage REGISTER <user> <pass>");
                        } else {
                            handleRegister(parts[1], parts[2], out);
                        }
                        break;

                    case "LOGOUT":
                        if (!checkPermission(Permission.LOGOUT, out)) break;
                        handleLogout(out);
                        break;

                    case "CHANGE_PASSWORD":
                        if (parts.length < 3) {
                            out.println("ERROR: Usage CHANGE_PASSWORD <old> <new>");
                        } else {
                            if (!checkPermission(Permission.CHANGE_PASSWORD, out)) break;
                            handleChangePassword(parts[1], parts[2], out);
                        }
                        break;

                    case "LIST_USERS":
                        if (!checkPermission(Permission.VIEW_USERS, out)) break;
                        handleListUsers(out);
                        break;

                    case "ASSIGN_ROLE":
                        if (parts.length < 3) {
                            out.println("ERROR: Usage ASSIGN_ROLE <username> <role>");
                        } else {
                            if (!checkPermission(Permission.ASSIGN_ROLE, out)) break;
                            handleAssignRole(parts[1], parts[2], out);
                        }
                        break;

                    case "DELETE_USER":
                        if (parts.length < 2) {
                            out.println("ERROR: Usage DELETE_USER <username>");
                        } else {
                            if (!checkPermission(Permission.DELETE_USER, out)) break;
                            handleDeleteUser(parts[1], out);
                        }
                        break;

                    case "SUSPEND_USER":
                        if (parts.length < 2) {
                            out.println("ERROR: Usage SUSPEND_USER <username>");
                        } else {
                            if (!checkPermission(Permission.SUSPEND_USER, out)) break;
                            handleSuspendUser(parts[1], out);
                        }
                        break;

                    case "VIEW_AUDIT":
                        if (parts.length < 2) {
                            out.println("ERROR: Usage VIEW_AUDIT <limit>");
                        } else {
                            if (!checkPermission(Permission.VIEW_AUDIT_LOGS, out)) break;
                            try {
                                int limit = Integer.parseInt(parts[1]);
                                handleViewAuditLogs(limit, out);
                            } catch (NumberFormatException e) {
                                out.println("ERROR: Limit must be a number");
                            }
                        }
                        break;

                    case "EXPORT_AUDIT":
                        if (!checkPermission(Permission.EXPORT_AUDIT_LOGS, out)) break;
                        handleExportAuditLogs(out);
                        break;

                    case "DASHBOARD":
                        if (!checkPermission(Permission.VIEW_STATS, out)) break;
                        handleDashboard(out);
                        break;

                    case "NOTIFICATIONS":
                        if (authenticatedUser != null) {
                            handleNotifications(out);
                        } else {
                            out.println("ERROR: Must be logged in");
                        }
                        break;

                    case "EXIT":
                        out.println("GOODBYE");
                        return;

                    default:
                        out.println("ERROR: Unknown command");
                }
            }
        } catch (IOException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Handler error: " + e.getMessage());
        } finally {
            if (authenticatedUser != null) {
                AuditLogger.logLogout(userId, authenticatedUser, ipAddress);
            }
            closeSocket();
        }
    }

    private boolean checkPermission(Permission permission, PrintWriter out) {
        if (authenticatedUser == null) {
            out.println("ERROR: Must be logged in");
            return false;
        }
        if (userRole == null) {
            out.println("ERROR: Role not assigned");
            return false;
        }
        if (!userRole.hasPermission(permission)) {
            AuditLogger.logPermissionDenied(userId, authenticatedUser, permission.getPermissionName(), ipAddress);
            out.println("ERROR: Permission denied - " + permission.getPermissionName());
            return false;
        }
        return true;
    }

    private void handleLogin(String user, String pass, PrintWriter out) {
        String hashedPassword = SecurityUtils.hashPassword(pass);
        String sql = "SELECT id, role_id, is_active FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int fetchedUserId = rs.getInt("id");
                    boolean isActive = rs.getBoolean("is_active");
                    
                    if (!isActive) {
                        AuditLogger.logLogin(fetchedUserId, user, ipAddress, false);
                        out.println("FAILED: Account suspended");
                        ConsoleColors.println(ConsoleColors.RED, "[!] Login attempt for suspended account: " + user);
                        return;
                    }
                    
                    int roleId = rs.getInt("role_id");
                    this.userId = fetchedUserId;
                    this.authenticatedUser = user;
                    this.userRole = getRoleById(roleId);

                    AuditLogger.logLogin(userId, user, ipAddress, true);
                    out.println("SUCCESS: Welcome " + user + " [" + userRole.getRoleName() + "]");
                    ConsoleColors.println(ConsoleColors.GREEN, "[*] User " + user + " (" + userRole.getRoleName() + ") authenticated.");
                    
                    // Update last login
                    updateLastLogin(user);
                } else {
                    AuditLogger.logLogin(null, user, ipAddress, false);
                    out.println("FAILED: Invalid credentials");
                    ConsoleColors.println(ConsoleColors.RED, "[!] Failed login attempt for " + user);
                }
            }
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!!] Database error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleRegister(String user, String pass, PrintWriter out) {
        String hashedPassword = SecurityUtils.hashPassword(pass);
        String sql = "INSERT INTO users (username, password, role_id) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);
            pstmt.setString(2, hashedPassword);
            pstmt.setInt(3, 3); // Default to USER role

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                AuditLogger.logRegistration(user, ipAddress, true);
                out.println("SUCCESS: User " + user + " registered. You can now login.");
                ConsoleColors.println(ConsoleColors.GREEN, "[+] New user registered: " + user);
            } else {
                AuditLogger.logRegistration(user, ipAddress, false);
                out.println("FAILED: Registration failed");
            }
        } catch (SQLException e) {
            AuditLogger.logRegistration(user, ipAddress, false);
            out.println("FAILED: Username already exists or database error");
            ConsoleColors.println(ConsoleColors.RED, "[!] Registration failed: " + e.getMessage());
        }
    }

    private void handleLogout(PrintWriter out) {
        if (authenticatedUser != null) {
            AuditLogger.logLogout(userId, authenticatedUser, ipAddress);
            ConsoleColors.println(ConsoleColors.YELLOW, "[-] User " + authenticatedUser + " logged out.");
            authenticatedUser = null;
            userId = null;
            userRole = null;
            out.println("SUCCESS: Logged out successfully.");
        } else {
            out.println("ERROR: Not logged in.");
        }
    }

    private void handleChangePassword(String oldPass, String newPass, PrintWriter out) {
        if (authenticatedUser == null) {
            out.println("ERROR: Must be logged in to change password.");
            return;
        }

        String hashedOld = SecurityUtils.hashPassword(oldPass);
        String hashedNew = SecurityUtils.hashPassword(newPass);
        String sql = "UPDATE users SET password = ? WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedNew);
            pstmt.setString(2, authenticatedUser);
            pstmt.setString(3, hashedOld);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                AuditLogger.logPasswordChange(userId, authenticatedUser, ipAddress, true);
                out.println("SUCCESS: Password updated.");
                ConsoleColors.println(ConsoleColors.GREEN, "[*] Password changed for " + authenticatedUser);
            } else {
                AuditLogger.logPasswordChange(userId, authenticatedUser, ipAddress, false);
                out.println("FAILED: Incorrect old password or database error.");
            }
        } catch (SQLException e) {
            AuditLogger.logPasswordChange(userId, authenticatedUser, ipAddress, false);
            ConsoleColors.println(ConsoleColors.RED, "[!!] Database error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleStatus(PrintWriter out) {
        long duration = (System.currentTimeMillis() - connectionTime) / 1000;
        long inactive = (System.currentTimeMillis() - lastActivity) / 1000;
        String status = String.format("USER: %s | ROLE: %s | SESSION: %s | UPTIME: %ds | IDLE: %ds | IP: %s",
                (authenticatedUser != null ? authenticatedUser : "GUEST"),
                (userRole != null ? userRole.getRoleName() : "N/A"),
                sessionId,
                duration,
                inactive,
                ipAddress);
        out.println("INFO: " + status);
    }

    private Role getRoleById(int roleId) {
        String sql = "SELECT role_name FROM roles WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roleId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Role.fromString(rs.getString("role_name"));
                }
            }
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Role lookup error: " + e.getMessage());
        }
        return Role.USER;
    }

    private void updateLastLogin(String username) {
        String sql = "UPDATE users SET last_login = NOW() WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Update last login error: " + e.getMessage());
        }
    }

    private void handleListUsers(PrintWriter out) {
        String sql = "SELECT u.id, u.username, r.role_name, u.is_active, u.created_at, u.last_login FROM users u LEFT JOIN roles r ON u.role_id = r.id ORDER BY u.id";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            
            StringBuilder sb = new StringBuilder();
            sb.append("ID | Username | Role | Active | Created | Last Login\n");
            sb.append("=================================================================\n");
            
            while (rs.next()) {
                sb.append(String.format("%d | %s | %s | %s | %s | %s\n",
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("role_name") != null ? rs.getString("role_name") : "N/A",
                    rs.getBoolean("is_active") ? "Yes" : "No",
                    rs.getString("created_at"),
                    rs.getString("last_login") != null ? rs.getString("last_login") : "Never"
                ));
            }
            out.println("INFO: " + sb.toString());
            AuditLogger.logAdminAction(userId, authenticatedUser, "LIST_USERS", "all_users", "Listed all users", ipAddress, true);
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] List users error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleAssignRole(String targetUser, String roleName, PrintWriter out) {
        String sql = "SELECT id FROM roles WHERE role_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName.toUpperCase());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (!rs.next()) {
                    out.println("ERROR: Role not found");
                    AuditLogger.logRoleAssignment(userId, authenticatedUser, targetUser, roleName, ipAddress, false);
                    return;
                }
                int roleId = rs.getInt("id");

                // Update user role
                String updateSql = "UPDATE users SET role_id = ? WHERE username = ?";
                try (PreparedStatement updatePstmt = conn.prepareStatement(updateSql)) {
                    updatePstmt.setInt(1, roleId);
                    updatePstmt.setString(2, targetUser);
                    int rows = updatePstmt.executeUpdate();
                    
                    if (rows > 0) {
                        AuditLogger.logRoleAssignment(userId, authenticatedUser, targetUser, roleName, ipAddress, true);
                        out.println("SUCCESS: Role assigned");
                        ConsoleColors.println(ConsoleColors.GREEN, "[*] Role assigned: " + targetUser + " -> " + roleName);
                        NotificationManager.notifyRoleChange(targetUser, roleName);
                    } else {
                        AuditLogger.logRoleAssignment(userId, authenticatedUser, targetUser, roleName, ipAddress, false);
                        out.println("FAILED: User not found");
                    }
                }
            }
        } catch (SQLException e) {
            AuditLogger.logRoleAssignment(userId, authenticatedUser, targetUser, roleName, ipAddress, false);
            ConsoleColors.println(ConsoleColors.RED, "[!] Assign role error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleDeleteUser(String targetUser, PrintWriter out) {
        if (targetUser.equalsIgnoreCase(authenticatedUser)) {
            out.println("ERROR: Cannot delete your own account");
            return;
        }

        String sql = "DELETE FROM users WHERE username = ? AND username != 'admin'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, targetUser);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                AuditLogger.logUserDeletion(userId, authenticatedUser, targetUser, ipAddress, true);
                out.println("SUCCESS: User deleted");
                ConsoleColors.println(ConsoleColors.GREEN, "[*] User deleted: " + targetUser);
            } else {
                AuditLogger.logUserDeletion(userId, authenticatedUser, targetUser, ipAddress, false);
                out.println("FAILED: User not found or cannot delete");
            }
        } catch (SQLException e) {
            AuditLogger.logUserDeletion(userId, authenticatedUser, targetUser, ipAddress, false);
            ConsoleColors.println(ConsoleColors.RED, "[!] Delete user error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleSuspendUser(String targetUser, PrintWriter out) {
        if (targetUser.equalsIgnoreCase(authenticatedUser)) {
            out.println("ERROR: Cannot suspend your own account");
            return;
        }

        String sql = "UPDATE users SET is_active = FALSE WHERE username = ? AND username != 'admin'";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, targetUser);
            int rows = pstmt.executeUpdate();
            
            if (rows > 0) {
                AuditLogger.logUserSuspension(userId, authenticatedUser, targetUser, ipAddress, true);
                out.println("SUCCESS: User suspended");
                ConsoleColors.println(ConsoleColors.GREEN, "[*] User suspended: " + targetUser);
                NotificationManager.notifySuspension(targetUser);
            } else {
                AuditLogger.logUserSuspension(userId, authenticatedUser, targetUser, ipAddress, false);
                out.println("FAILED: User not found");
            }
        } catch (SQLException e) {
            AuditLogger.logUserSuspension(userId, authenticatedUser, targetUser, ipAddress, false);
            ConsoleColors.println(ConsoleColors.RED, "[!] Suspend user error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleViewAuditLogs(int limit, PrintWriter out) {
        String sql = "SELECT username, action, resource, status, created_at FROM audit_logs ORDER BY created_at DESC LIMIT ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Username | Action | Resource | Status | Timestamp\n");
                sb.append("=====================================================================\n");
                
                while (rs.next()) {
                    sb.append(String.format("%s | %s | %s | %s | %s\n",
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("resource"),
                        rs.getString("status"),
                        rs.getString("created_at")
                    ));
                }
                out.println("INFO: " + sb.toString());
                AuditLogger.logAdminAction(userId, authenticatedUser, "VIEW_AUDIT_LOGS", "audit_logs", "Viewed " + limit + " audit logs", ipAddress, true);
            }
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] View audit error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleExportAuditLogs(PrintWriter out) {
        String sql = "SELECT username, action, resource, details, status, created_at FROM audit_logs ORDER BY created_at DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            
            StringBuilder csv = new StringBuilder();
            csv.append("Username,Action,Resource,Details,Status,Timestamp\n");
            
            while (rs.next()) {
                csv.append(String.format("%s,%s,%s,%s,%s,%s\n",
                    rs.getString("username"),
                    rs.getString("action"),
                    rs.getString("resource"),
                    rs.getString("details"),
                    rs.getString("status"),
                    rs.getString("created_at")
                ));
            }
            
            out.println("EXPORT: " + csv.toString());
            AuditLogger.logAdminAction(userId, authenticatedUser, "EXPORT_AUDIT_LOGS", "audit_logs", "Exported audit logs", ipAddress, true);
            ConsoleColors.println(ConsoleColors.GREEN, "[*] Audit logs exported by " + authenticatedUser);
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Export audit error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleDashboard(PrintWriter out) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Get user statistics
            String userStatsSql = "SELECT COUNT(*) as total, SUM(CASE WHEN is_active=1 THEN 1 ELSE 0 END) as active FROM users";
            String roleStatsSql = "SELECT r.role_name, COUNT(u.id) as count FROM roles r LEFT JOIN users u ON r.id = u.role_id GROUP BY r.id, r.role_name";
            String loginStatsSql = "SELECT COUNT(*) as logins FROM audit_logs WHERE action = 'LOGIN' AND status = 'SUCCESS'";
            
            StringBuilder dashboard = new StringBuilder();
            dashboard.append("====== DASHBOARD ======\n\n");
            
            try (PreparedStatement pstmt = conn.prepareStatement(userStatsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dashboard.append(String.format("Total Users: %d | Active: %d\n", 
                        rs.getInt("total"), rs.getInt("active")));
                }
            }
            
            dashboard.append("\nUsers by Role:\n");
            try (PreparedStatement pstmt = conn.prepareStatement(roleStatsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    dashboard.append(String.format("  %s: %d\n", 
                        rs.getString("role_name"), rs.getInt("count")));
                }
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(loginStatsSql);
                 ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    dashboard.append(String.format("\nTotal Successful Logins: %d\n", rs.getInt("logins")));
                }
            }
            
            out.println("INFO: " + dashboard.toString());
            AuditLogger.logAdminAction(userId, authenticatedUser, "VIEW_DASHBOARD", "system", "Viewed dashboard", ipAddress, true);
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Dashboard error: " + e.getMessage());
            out.println("ERROR: Database error");
        }
    }

    private void handleNotifications(PrintWriter out) {
        java.util.List<String> notifications = NotificationManager.getAndClearNotifications(authenticatedUser);
        if (notifications.isEmpty()) {
            out.println("INFO: No new notifications");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String notif : notifications) {
                sb.append(notif).append("\n");
            }
            out.println("INFO: " + sb.toString());
        }
    }

    private void closeSocket() {
        try {
            clientSocket.close();
            ConsoleColors.println(ConsoleColors.CYAN, "[-] Connection closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
