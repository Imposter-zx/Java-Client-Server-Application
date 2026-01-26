package com.example.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Main server class that listens for client connections on port 5000.
 * Orchestrates multi-threaded client handling.
 */
public class Server {
    private static final int PORT = 5000;

    /**
     * Entry point for the server application.
     */
    public static void main(String[] args) {
        printBanner();
        ConsoleColors.println(ConsoleColors.CYAN, ">> Server initialized. Listening on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ConsoleColors.println(ConsoleColors.YELLOW,
                        "[+] Incoming connection: " + clientSocket.getInetAddress().getHostAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            ConsoleColors.println(ConsoleColors.RED, "[!] Server error: " + e.getMessage());
        }
    }

    private static void printBanner() {
        String banner = "  ____  _____ ______     _______ ____  \n" +
                " / ___|| ____|  _ \\ \\   / / ____|  _ \\ \n" +
                " \\___ \\|  _| | |_) \\ \\ / /|  _| | |_) |\n" +
                "  ___) | |___|  _ < \\ V / | |___|  _ < \n" +
                " |____/|_____|_| \\_\\ \\_/  |_____|_| \\_\\\n" +
                "                                       ";
        ConsoleColors.println(ConsoleColors.BLUE_BOLD, banner);
        ConsoleColors.println(ConsoleColors.WHITE_BOLD, "--- AUTHENTICATION ENGINE v2.5 [ENTERPRISE] ---");
        ConsoleColors.println(ConsoleColors.CYAN, ">> Build Date: Jan 2026 | Mode: SECURE-TCP");
    }
}
