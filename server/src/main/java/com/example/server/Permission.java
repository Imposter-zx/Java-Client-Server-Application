package com.example.server;

/**
 * Enum representing available permissions in the system.
 */
public enum Permission {
    LOGIN("login", "Ability to login to the system"),
    LOGOUT("logout", "Ability to logout from the system"),
    REGISTER("register", "Ability to register new users"),
    CHANGE_PASSWORD("change_password", "Ability to change own password"),
    VIEW_PROFILE("view_profile", "Ability to view own profile"),
    CHANGE_OTHER_PASSWORD("change_other_password", "Ability to change other user's password"),
    VIEW_USERS("view_users", "Ability to view list of users"),
    DELETE_USER("delete_user", "Ability to delete users"),
    SUSPEND_USER("suspend_user", "Ability to suspend user accounts"),
    ASSIGN_ROLE("assign_role", "Ability to assign roles to users"),
    VIEW_AUDIT_LOGS("view_audit_logs", "Ability to view audit logs"),
    EXPORT_AUDIT_LOGS("export_audit_logs", "Ability to export audit logs"),
    VIEW_STATS("view_stats", "Ability to view system statistics"),
    MANAGE_ROLES("manage_roles", "Ability to manage roles and permissions"),
    SYSTEM_CONFIG("system_config", "Ability to configure system settings");

    private final String permissionName;
    private final String description;

    Permission(String permissionName, String description) {
        this.permissionName = permissionName;
        this.description = description;
    }

    public String getPermissionName() {
        return permissionName;
    }

    public String getDescription() {
        return description;
    }

    public static Permission fromString(String permStr) {
        for (Permission p : Permission.values()) {
            if (p.permissionName.equalsIgnoreCase(permStr)) {
                return p;
            }
        }
        return null;
    }
}
