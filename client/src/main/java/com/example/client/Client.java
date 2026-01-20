package com.example.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 5000;

    public static void main(String[] args) {
        printBanner();
        
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            ConsoleColors.println(ConsoleColors.GREEN, "[*] Connected to High-Security Auth Server.");

            boolean running = true;
            while (running) {
                printMenu();
                System.out.print(ConsoleColors.CYAN + "Choice > " + ConsoleColors.RESET);
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1":
                        handleLogin(scanner, out, in);
                        break;
                    case "2":
                        handleRegister(scanner, out, in);
                        break;
                    case "3":
                        out.println("STATUS");
                        displayResponse(in.readLine());
                        break;
                    case "4":
                        handleChangePassword(scanner, out, in);
                        break;
                    case "5":
                        handleLogout(out, in);
                        break;
                    case "6":
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

        out.println("LOGIN " + username + " " + password);
        String response = in.readLine();
        displayResponse(response);
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
        displayResponse(in.readLine());
    }

    private static void simulateLoading(String action) {
        System.out.print(ConsoleColors.YELLOW + action + " .");
        try {
            for (int i = 0; i < 3; i++) {
                Thread.sleep(300);
                System.out.print(".");
            }
        } catch (InterruptedException ignored) {}
        System.out.println(ConsoleColors.RESET);
    }

    private static void displayResponse(String response) {
        if (response == null) return;
        if (response.startsWith("SUCCESS") || response.startsWith("INFO")) {
            ConsoleColors.println(ConsoleColors.GREEN, ">>> " + response);
        } else {
            ConsoleColors.println(ConsoleColors.RED, ">>> " + response);
        }
    }

    private static void printBanner() {
        String banner = 
              "   ____ _     ___ _____ _   _ _____ \n" +
              "  / ___| |   |_ _| ____| \\ | |_   _|\n" +
              " | |   | |    | ||  _| |  \\| | | |  \n" +
              " | |___| |___ | || |___| |\\  | | |  \n" +
              "  \\____|_____|___|_____|_| \\_| |_|  \n" +
              "                                    ";
        ConsoleColors.println(ConsoleColors.CYAN, banner);
        ConsoleColors.println(ConsoleColors.RESET, "--- Terminal Secure Access Node v1.0 ---");
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
}
