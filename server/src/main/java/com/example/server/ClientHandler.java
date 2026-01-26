package com.example.server;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles individual client connections and processes commands.
 * Supports LOGIN, STATUS, REGISTER, LOGOUT, CHANGE_PASSWORD, and EXIT.
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private String authenticatedUser = null;
    private final String sessionId;
    private long connectionTime;
    private long lastActivity;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.sessionId = java.util.UUID.randomUUID().toString().substring(0, 8);
        this.connectionTime = System.currentTimeMillis();
        this.lastActivity = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                this.lastActivity = System.currentTimeMillis();
                String[] parts = inputLine.split(" ", 3);
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
                        handleLogout(out);
                        break;

                    case "CHANGE_PASSWORD":
                        if (parts.length < 3) {
                            out.println("ERROR: Usage CHANGE_PASSWORD <old> <new>");
                        } else {
                            handleChangePassword(parts[1], parts[2], out);
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
            closeSocket();
        }
    }

    private void handleLogin(String user, String pass, PrintWriter out) {
        if (authenticate(user, pass)) {
            this.authenticatedUser = user;
            out.println("SUCCESS: Welcome " + user);
            ConsoleColors.println(ConsoleColors.GREEN, "[*] User " + user + " authenticated.");
        } else {
            out.println("FAILED: Invalid credentials");
            ConsoleColors.println(ConsoleColors.RED, "[!] Failed login attempt for " + user);
        }
    }

    private void handleRegister(String user, String pass, PrintWriter out) {
        if (registerInDb(user, pass)) {
            out.println("SUCCESS: User " + user + " registered. You can now login.");
            ConsoleColors.println(ConsoleColors.GREEN, "[+] New user registered: " + user);
        } else {
            out.println("FAILED: Username already exists or database error");
            ConsoleColors.println(ConsoleColors.RED, "[!] Failed registration attempt for " + user);
        }
    }

    private void handleLogout(PrintWriter out) {
        if (authenticatedUser != null) {
            ConsoleColors.println(ConsoleColors.YELLOW, "[-] User " + authenticatedUser + " logged out.");
            authenticatedUser = null;
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

        if (updatePasswordInDb(authenticatedUser, oldPass, newPass)) {
            out.println("SUCCESS: Password updated.");
            ConsoleColors.println(ConsoleColors.GREEN, "[*] Password changed for " + authenticatedUser);
        } else {
            out.println("FAILED: Incorrect old password or database error.");
        }
    }

    private void handleStatus(PrintWriter out) {
        long duration = (System.currentTimeMillis() - connectionTime) / 1000;
        long inactive = (System.currentTimeMillis() - lastActivity) / 1000;
        String status = String.format("USER: %s | SESSION: %s | UPTIME: %ds | IDLE: %ds | IP: %s",
                (authenticatedUser != null ? authenticatedUser : "GUEST"),
                sessionId,
                duration,
                inactive,
                clientSocket.getInetAddress().getHostAddress());
        out.println("INFO: " + status);
    }

    private boolean authenticate(String username, String password) {
        // We hash the incoming password to compare with the DB hash
        String hashedPassword = SecurityUtils.hashPassword(password);
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!!] Database error check: " + e.getMessage());
            return false;
        }
    }

    private boolean registerInDb(String username, String password) {
        String hashedPassword = SecurityUtils.hashPassword(password);
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, hashedPassword);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            // Likely a duplicate entry error
            ConsoleColors.println(ConsoleColors.RED, "[!!] Database error register: " + e.getMessage());
            return false;
        }
    }

    private boolean updatePasswordInDb(String username, String oldPass, String newPass) {
        String hashedOld = SecurityUtils.hashPassword(oldPass);
        String hashedNew = SecurityUtils.hashPassword(newPass);
        String sql = "UPDATE users SET password = ? WHERE username = ? AND password = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, hashedNew);
            pstmt.setString(2, username);
            pstmt.setString(3, hashedOld);

            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!!] Database error update: " + e.getMessage());
            return false;
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
