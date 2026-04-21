package com.example.server;

import java.util.HashSet;
import java.util.Set;

/**
 * Enum representing user roles and their associated permissions.
 */
public enum Role {
    ADMIN("ADMIN", "Administrator with full access") {
        @Override
        public Set<Permission> getPermissions() {
            Set<Permission> perms = new HashSet<>();
            for (Permission p : Permission.values()) {
                perms.add(p);
            }
            return perms;
        }
    },
    MODERATOR("MODERATOR", "Moderator with limited admin access") {
        @Override
        public Set<Permission> getPermissions() {
            Set<Permission> perms = new HashSet<>();
            perms.add(Permission.LOGIN);
            perms.add(Permission.LOGOUT);
            perms.add(Permission.VIEW_PROFILE);
            perms.add(Permission.CHANGE_PASSWORD);
            perms.add(Permission.VIEW_STATS);
            perms.add(Permission.VIEW_AUDIT_LOGS);
            perms.add(Permission.VIEW_USERS);
            return perms;
        }
    },
    USER("USER", "Regular user with basic access") {
        @Override
        public Set<Permission> getPermissions() {
            Set<Permission> perms = new HashSet<>();
            perms.add(Permission.LOGIN);
            perms.add(Permission.LOGOUT);
            perms.add(Permission.VIEW_PROFILE);
            perms.add(Permission.CHANGE_PASSWORD);
            return perms;
        }
    };

    private final String roleName;
    private final String description;

    Role(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    public String getRoleName() {
        return roleName;
    }

    public String getDescription() {
        return description;
    }

    public abstract Set<Permission> getPermissions();

    public boolean hasPermission(Permission permission) {
        return getPermissions().contains(permission);
    }

    public static Role fromString(String roleStr) {
        for (Role r : Role.values()) {
            if (r.roleName.equalsIgnoreCase(roleStr)) {
                return r;
            }
        }
        return USER; // Default to USER if not found
    }
}
