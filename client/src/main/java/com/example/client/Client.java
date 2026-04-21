package com.example.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.Arrays;
import java.util.List;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;
    private static String currentUserRole = null;
    private static String currentUsername = null;

    public static void main(String[] args) {
        printBanner();

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Scanner scanner = new Scanner(System.in)) {

            ConsoleColors.println(ConsoleColors.GREEN, "[*] Connected to High-Security Auth Server.");

            boolean running = true;
            while (running) {
                if (currentUsername != null) {
                    printAuthenticatedMenu();
                } else {
                    printMenu();
                }
                System.out.print(ConsoleColors.CYAN + "Choice > " + ConsoleColors.RESET);
                String input = scanner.nextLine();
                
                // Autocomplete support
                input = handleAutocomplete(input);

                switch (input.toUpperCase()) {
                    case "1":
                    case "LOGIN":
                        handleLogin(scanner, out, in);
                        break;
                    case "2":
                    case "REGISTER":
                        handleRegister(scanner, out, in);
                        break;
                    case "3":
                    case "STATUS":
                        out.println("STATUS");
                        displayResponse(in.readLine());
                        break;
                    case "4":
                    case "CHANGE_PASSWORD":
                        handleChangePassword(scanner, out, in);
                        break;
                    case "5":
                    case "LOGOUT":
                        handleLogout(out, in);
                        break;
                    case "6":
                    case "NOTIFICATIONS":
                        out.println("NOTIFICATIONS");
                        displayResponse(in.readLine());
                        break;
                    case "7":
                    case "DASHBOARD":
                        out.println("DASHBOARD");
                        displayResponse(in.readLine());
                        break;
                    case "8":
                    case "ADMIN":
                        handleAdminPanel(scanner, out, in);
                        break;
                    case "9":
                    case "EXIT":
                        out.println("EXIT");
                        displayResponse(in.readLine());
                        running = false;
                        break;
                    default:
                        ConsoleColors.println(ConsoleColors.RED, "[!] Invalid choice.");
                }
            }

        } catch (IOException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Connection Error: " + e.getMessage());
        }
    }

    private static String handleAutocomplete(String input) {
        if (input.length() == 0) return input;
        
        List<String> commands = Arrays.asList(
            "LOGIN", "REGISTER", "STATUS", "CHANGE_PASSWORD", "LOGOUT",
            "NOTIFICATIONS", "DASHBOARD", "ADMIN", "EXIT",
            "LIST_USERS", "ASSIGN_ROLE", "DELETE_USER", "SUSPEND_USER",
            "VIEW_AUDIT", "EXPORT_AUDIT"
        );
        
        String upper = input.toUpperCase();
        for (String cmd : commands) {
            if (cmd.startsWith(upper)) {
                return cmd;
            }
        }
        return input;
    }

    private static void handleAdminPanel(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        if (currentUserRole == null || !isAdmin(currentUserRole)) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Admin access required.");
            return;
        }

        boolean inAdmin = true;
        while (inAdmin) {
            printAdminMenu();
            System.out.print(ConsoleColors.CYAN + "Admin Choice > " + ConsoleColors.RESET);
            String choice = scanner.nextLine().toUpperCase();

            switch (choice) {
                case "1":
                case "LIST_USERS":
                    out.println("LIST_USERS");
                    displayResponse(in.readLine());
                    break;
                case "2":
                case "ASSIGN_ROLE":
                    handleAssignRole(scanner, out, in);
                    break;
                case "3":
                case "SUSPEND_USER":
                    handleSuspendUser(scanner, out, in);
                    break;
                case "4":
                case "DELETE_USER":
                    handleDeleteUser(scanner, out, in);
                    break;
                case "5":
                case "VIEW_AUDIT":
                    handleViewAudit(scanner, out, in);
                    break;
                case "6":
                case "EXPORT_AUDIT":
                    handleExportAudit(out, in);
                    break;
                case "7":
                case "BACK":
                    inAdmin = false;
                    break;
                default:
                    ConsoleColors.println(ConsoleColors.RED, "[!] Invalid choice.");
            }
        }
    }

    private static void handleAssignRole(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ ASSIGN ROLE ]" + ConsoleColors.RESET);
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Role (ADMIN/MODERATOR/USER): ");
        String role = scanner.nextLine();

        if (username.isBlank() || role.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Fields cannot be empty.");
            return;
        }

        simulateLoading("Assigning role");
        out.println("ASSIGN_ROLE " + username + " " + role);
        displayResponse(in.readLine());
    }

    private static void handleSuspendUser(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ SUSPEND USER ]" + ConsoleColors.RESET);
        System.out.print("Username to suspend: ");
        String username = scanner.nextLine();

        if (username.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Username cannot be empty.");
            return;
        }

        System.out.print("Confirm suspension (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            ConsoleColors.println(ConsoleColors.YELLOW, "[*] Operation cancelled.");
            return;
        }

        simulateLoading("Suspending user");
        out.println("SUSPEND_USER " + username);
        displayResponse(in.readLine());
    }

    private static void handleDeleteUser(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ DELETE USER ]" + ConsoleColors.RESET);
        System.out.print("Username to delete: ");
        String username = scanner.nextLine();

        if (username.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Username cannot be empty.");
            return;
        }

        System.out.print("Confirm deletion (yes/no): ");
        String confirm = scanner.nextLine();
        
        if (!confirm.equalsIgnoreCase("yes")) {
            ConsoleColors.println(ConsoleColors.YELLOW, "[*] Operation cancelled.");
            return;
        }

        simulateLoading("Deleting user");
        out.println("DELETE_USER " + username);
        displayResponse(in.readLine());
    }

    private static void handleViewAudit(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ VIEW AUDIT LOGS ]" + ConsoleColors.RESET);
        System.out.print("Number of records (default 50): ");
        String limitStr = scanner.nextLine();
        String limit = limitStr.isBlank() ? "50" : limitStr;

        try {
            Integer.parseInt(limit);
            out.println("VIEW_AUDIT " + limit);
            displayResponse(in.readLine());
        } catch (NumberFormatException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Limit must be a number.");
        }
    }

    private static void handleExportAudit(PrintWriter out, BufferedReader in) throws IOException {
        simulateLoading("Exporting audit logs");
        out.println("EXPORT_AUDIT");
        
        StringBuilder csv = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            csv.append(line).append("\n");
            if (line.startsWith("EXPORT:")) {
                break;
            }
        }
        
        if (csv.length() > 0) {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "audit_export_" + timestamp + ".csv";
            try (FileWriter fw = new FileWriter(filename)) {
                fw.write(csv.toString());
                ConsoleColors.println(ConsoleColors.GREEN, "[*] Audit logs exported to " + filename);
            } catch (IOException e) {
                ConsoleColors.println(ConsoleColors.RED, "[!] Export failed: " + e.getMessage());
            }
        }
    }

    private static void handleLogin(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ LOGIN ]" + ConsoleColors.RESET);
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Fields cannot be empty.");
            return;
        }

        if (username.contains(" ")) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Username cannot contain spaces.");
            return;
        }

        simulateLoading("Authenticating");
        out.println("LOGIN " + username + " " + password);
        String response = in.readLine();
        displayResponse(response);
        
        if (response.startsWith("SUCCESS")) {
            currentUsername = username;
            // Extract role from response
            if (response.contains("[ADMIN]")) {
                currentUserRole = "ADMIN";
            } else if (response.contains("[MODERATOR]")) {
                currentUserRole = "MODERATOR";
            } else {
                currentUserRole = "USER";
            }
            ConsoleColors.println(ConsoleColors.GREEN, "[*] Welcome " + username + " (" + currentUserRole + ")");
        }
    }

    private static void handleRegister(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ REGISTER NEW ACCOUNT ]" + ConsoleColors.RESET);
        System.out.print("Desired Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Fields cannot be empty.");
            return;
        }

        if (username.contains(" ")) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Username cannot contain spaces.");
            return;
        }

        simulateLoading("Registering");
        out.println("REGISTER " + username + " " + password);
        String response = in.readLine();
        displayResponse(response);
    }

    private static void handleChangePassword(Scanner scanner, PrintWriter out, BufferedReader in) throws IOException {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n[ CHANGE PASSWORD ]" + ConsoleColors.RESET);
        System.out.print("Old Password: ");
        String oldPass = scanner.nextLine();
        System.out.print("New Password: ");
        String newPass = scanner.nextLine();

        if (oldPass.isBlank() || newPass.isBlank()) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Fields cannot be empty.");
            return;
        }

        simulateLoading("Updating");
        out.println("CHANGE_PASSWORD " + oldPass + " " + newPass);
        String response = in.readLine();
        displayResponse(response);
    }

    private static void handleLogout(PrintWriter out, BufferedReader in) throws IOException {
        simulateLoading("Logging out");
        out.println("LOGOUT");
        String response = in.readLine();
        displayResponse(response);
        if (response.startsWith("SUCCESS")) {
            currentUsername = null;
            currentUserRole = null;
        }
    }

    private static void simulateLoading(String action) {
        System.out.print(ConsoleColors.YELLOW + action + " .");
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
        } catch (InterruptedException ignored) {
        }
        System.out.println(ConsoleColors.RESET);
    }

    private static void displayResponse(String response) {
        if (response == null)
            return;
        if (response.startsWith("SUCCESS") || response.startsWith("INFO")) {
            ConsoleColors.println(ConsoleColors.GREEN, ">>> " + response);
        } else {
            ConsoleColors.println(ConsoleColors.RED, ">>> " + response);
        }
    }

    private static void printBanner() {
        String banner = "   ____ _     ___ _____ _   _ _____ \n" +
                "  / ___| |   |_ _| ____| \\ | |_   _|\n" +
                " | |   | |    | ||  _| |  \\| | | |  \n" +
                " | |___| |___ | || |___| |\\  | | |  \n" +
                "  \\____|_____|___|_____|_| \\_| |_|  \n" +
                "                                    ";
        ConsoleColors.println(ConsoleColors.CYAN_BOLD, banner);
        ConsoleColors.println(ConsoleColors.WHITE_BOLD, "--- PRO Terminal Secure Access Node v2.5 ---");
        ConsoleColors.println(ConsoleColors.RESET, "--- Professional Authentication Suite ---");
    }

    private static void printMenu() {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n--- MAIN MENU ---" + ConsoleColors.RESET);
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Connection Status");
        System.out.println("4. Change Password");
        System.out.println("5. Logout");
        System.out.println("6. Exit");
    }

    private static void printAuthenticatedMenu() {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n--- AUTHENTICATED MENU (" + currentUsername + " | " + currentUserRole + ") ---" + ConsoleColors.RESET);
        System.out.println("1. Login");
        System.out.println("2. Register");
        System.out.println("3. Connection Status");
        System.out.println("4. Change Password");
        System.out.println("5. Logout");
        System.out.println("6. View Notifications");
        System.out.println("7. View Dashboard");
        if (isAdmin(currentUserRole)) {
            System.out.println("8. Admin Panel");
        }
        System.out.println("9. Exit");
    }

    private static void printAdminMenu() {
        System.out.println(ConsoleColors.WHITE_BOLD + "\n--- ADMIN PANEL ---" + ConsoleColors.RESET);
        System.out.println("1. List All Users");
        System.out.println("2. Assign Role");
        System.out.println("3. Suspend User");
        System.out.println("4. Delete User");
        System.out.println("5. View Audit Logs");
        System.out.println("6. Export Audit Logs");
        System.out.println("7. Back to Main Menu");
    }

    private static boolean isAdmin(String role) {
        return role != null && (role.equals("ADMIN") || role.equals("MODERATOR"));
    }
}
