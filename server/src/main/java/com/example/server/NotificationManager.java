package com.example.server;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages real-time notifications for connected clients.
 */
public class NotificationManager {
    private static final Map<String, List<String>> userNotifications = Collections.synchronizedMap(new HashMap<>());
    private static final List<ClientHandler> connectedClients = new CopyOnWriteArrayList<>();

    /**
     * Register a client handler for notifications.
     */
    public static void registerClient(ClientHandler handler, String username) {
        connectedClients.add(handler);
        if (!userNotifications.containsKey(username)) {
            userNotifications.put(username, Collections.synchronizedList(new ArrayList<>()));
        }
    }

    /**
     * Unregister a client handler.
     */
    public static void unregisterClient(ClientHandler handler, String username) {
        connectedClients.remove(handler);
    }

    /**
     * Add a notification for a specific user.
     */
    public static void notify(String username, String message) {
        List<String> notifications = userNotifications.computeIfAbsent(username, k -> Collections.synchronizedList(new ArrayList<>()));
        notifications.add("[NOTIFICATION] " + message);
    }

    /**
     * Broadcast a notification to all users.
     */
    public static void notifyAll(String message) {
        String notification = "[BROADCAST] " + message;
        for (List<String> notifs : userNotifications.values()) {
            notifs.add(notification);
        }
    }

    /**
     * Get and clear notifications for a user.
     */
    public static List<String> getAndClearNotifications(String username) {
        List<String> notifs = userNotifications.getOrDefault(username, new ArrayList<>());
        List<String> result = new ArrayList<>(notifs);
        notifs.clear();
        return result;
    }

    /**
     * Notify a user of a role change.
     */
    public static void notifyRoleChange(String username, String newRole) {
        notify(username, "Your role has been changed to: " + newRole);
    }

    /**
     * Notify a user of account suspension.
     */
    public static void notifySuspension(String username) {
        notify(username, "Your account has been suspended.");
    }

    /**
     * Notify an admin of suspicious activity.
     */
    public static void notifyAdmin(String action, String details) {
        notifyAll("SECURITY ALERT: " + action + " - " + details);
    }

    /**
     * Get count of pending notifications.
     */
    public static int getNotificationCount(String username) {
        return userNotifications.getOrDefault(username, new ArrayList<>()).size();
    }
}
