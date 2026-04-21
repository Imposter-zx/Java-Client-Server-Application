# Pro Authentication Suite v3.0 - New Features Documentation

## Overview
Your application now includes comprehensive Role-Based Access Control (RBAC), advanced audit logging, and an enhanced admin dashboard with notification system.

---

## 1. ROLE-BASED ACCESS CONTROL (RBAC)

### Available Roles

#### **ADMIN**
- Full system access to all commands
- Can manage users, assign roles, suspend/delete accounts
- Full access to audit logs and system statistics
- Permissions: ALL

#### **MODERATOR** 
- Limited administrative capabilities
- Can view users, audit logs, and statistics
- Cannot delete/suspend users or assign roles
- Permissions: LOGIN, LOGOUT, VIEW_PROFILE, CHANGE_PASSWORD, VIEW_STATS, VIEW_AUDIT_LOGS, VIEW_USERS

#### **USER**
- Standard user with basic access
- Can only manage their own account
- Cannot access admin functions
- Permissions: LOGIN, LOGOUT, VIEW_PROFILE, CHANGE_PASSWORD

### Permission System

The system implements fine-grained permissions:
- `LOGIN` - Access to authentication
- `LOGOUT` - Session termination
- `VIEW_PROFILE` - View own status
- `CHANGE_PASSWORD` - Update password
- `VIEW_USERS` - List all users
- `DELETE_USER` - Remove user accounts
- `SUSPEND_USER` - Temporarily disable accounts
- `ASSIGN_ROLE` - Change user roles
- `VIEW_AUDIT_LOGS` - Access operation history
- `EXPORT_AUDIT_LOGS` - Export logs to CSV
- `VIEW_STATS` - View system statistics
- And more...

### Test Accounts

```
Username: admin
Password: password123
Role: ADMIN

Username: moderator1
Password: password123
Role: MODERATOR

Username: user1
Password: mypassword
Role: USER
```

---

## 2. AUDIT & LOGGING SYSTEM

### What Gets Logged

Every significant operation is logged with:
- **Timestamp**: When the action occurred
- **User**: Who performed the action
- **Action**: Type of operation (LOGIN, LOGOUT, CHANGE_PASSWORD, etc.)
- **Resource**: What was affected (e.g., user:admin)
- **Status**: SUCCESS, FAILED, DENIED
- **IP Address**: Client IP for security tracking
- **Details**: Additional context

### Logged Events

- Login attempts (success/failure)
- Logout operations
- Password changes
- User registrations
- Role assignments
- User suspensions/deletions
- Permission denials
- Admin actions
- Dashboard access
- Audit log exports

### Accessing Audit Logs

**Client Command**: `VIEW_AUDIT <limit>`

Example:
```
VIEW_AUDIT 50
```

Returns the last 50 audit log entries with:
- Username
- Action type
- Resource affected
- Status
- Timestamp

### Exporting Audit Logs

**Client Command**: `EXPORT_AUDIT`

Exports all audit logs to a CSV file for external analysis:
```
audit_export_<timestamp>.csv
```

CSV columns:
- Username
- Action
- Resource
- Details
- Status
- Timestamp

---

## 3. ENHANCED CONSOLE INTERFACE

### Dynamic Menu System

The client now displays contextual menus:

#### Unauthenticated Menu
```
--- MAIN MENU ---
1. Login
2. Register
3. Connection Status
4. Change Password
5. Logout
6. Exit
```

#### Authenticated Menu (shows user role)
```
--- AUTHENTICATED MENU (admin | ADMIN) ---
1. Login
2. Register
3. Connection Status
4. Change Password
5. Logout
6. View Notifications
7. View Dashboard
8. Admin Panel         (only for ADMIN/MODERATOR)
9. Exit
```

#### Admin Panel (ADMIN/MODERATOR only)
```
--- ADMIN PANEL ---
1. List All Users
2. Assign Role
3. Suspend User
4. Delete User
5. View Audit Logs
6. Export Audit Logs
7. Back to Main Menu
```

### Command Autocomplete

The client now supports command name autocomplete:
- Type partial command: `LI` → Autocompletes to `LOGIN`
- Type partial command: `AS` → Autocompletes to `ASSIGN_ROLE`
- Works with both menu numbers and full command names

### Dashboard Statistics

**Command**: `DASHBOARD`

Displays real-time system statistics:
- Total active users
- Users by role breakdown
- Total successful logins
- System health indicators

---

## 4. NOTIFICATION SYSTEM

### Notification Types

#### Role Change Notifications
When an admin assigns a new role to a user, the user receives a notification:
```
[NOTIFICATION] Your role has been changed to: ADMIN
```

#### Suspension Alerts
When an account is suspended:
```
[NOTIFICATION] Your account has been suspended.
```

#### Security Alerts
Admins receive notifications of suspicious activities:
```
[BROADCAST] SECURITY ALERT: Multiple failed login attempts from IP X.X.X.X
```

### Accessing Notifications

**Command**: `NOTIFICATIONS`

Displays all pending notifications for the current user and clears them after viewing.

---

## 5. USER MANAGEMENT COMMANDS

### List All Users

**Command**: `LIST_USERS`

Returns a table with:
- User ID
- Username
- Role
- Active Status
- Account Creation Date
- Last Login

### Assign Role

**Command**: `ASSIGN_ROLE <username> <role>`

Examples:
```
ASSIGN_ROLE john ADMIN
ASSIGN_ROLE mary MODERATOR
ASSIGN_ROLE bob USER
```

### Suspend User Account

**Command**: `SUSPEND_USER <username>`

Temporarily disables a user account without deletion.

Example:
```
SUSPEND_USER hacker123
```

The suspended user will receive a notification and cannot login.

### Delete User

**Command**: `DELETE_USER <username>`

Permanently removes a user account from the system.

Example:
```
DELETE_USER olduser
```

**Note**: Cannot delete the 'admin' account or your own account.

---

## 6. DATABASE SCHEMA CHANGES

### New Tables

#### `roles`
- Stores role definitions
- Fields: id, role_name, description, created_at

#### `audit_logs`
- Comprehensive operation history
- Fields: id, user_id, username, action, resource, details, ip_address, status, created_at
- Indexed for fast querying

### Modified Tables

#### `users`
- Added: `role_id` (foreign key to roles)
- Added: `is_active` (boolean for suspension)
- Added: `created_at` (account creation timestamp)
- Added: `last_login` (last authentication time)

---

## 7. SECURITY ENHANCEMENTS

### Features

- **Session Tracking**: Each session has a unique UUID
- **IP Logging**: All operations logged with client IP
- **Account Status**: Users can be suspended without deletion
- **Permission Validation**: All commands checked against user role
- **Audit Trail**: Complete history of all operations
- **Failed Login Tracking**: Documents all authentication attempts

### Best Practices

1. **Regular Audit Review**: Check logs weekly for suspicious activities
2. **Role Minimization**: Assign only necessary roles to users
3. **Account Monitoring**: Watch for unusual login patterns
4. **Log Exports**: Regularly export and archive audit logs
5. **Suspension Over Deletion**: Use suspension for temporary account lockouts

---

## 8. SERVER-SIDE IMPROVEMENTS

### New Classes

#### `Role.java`
- Enum defining user roles
- Maps roles to permissions
- Provides permission checking

#### `Permission.java`
- Enum of all available permissions
- Descriptive text for each permission

#### `AuditLogger.java`
- Centralized logging functionality
- Non-blocking async logging
- Specialized log methods for common operations

#### `NotificationManager.java`
- Manages real-time user notifications
- Supports targeted and broadcast messages
- Synchronized collection for thread safety

### New Handlers in ClientHandler

- `handleListUsers()` - User enumeration
- `handleAssignRole()` - Role management
- `handleDeleteUser()` - User deletion
- `handleSuspendUser()` - Account suspension
- `handleViewAuditLogs()` - Log viewing
- `handleExportAuditLogs()` - Log export
- `handleDashboard()` - Statistics display
- `handleNotifications()` - Notification retrieval

---

## 9. CLIENT-SIDE ENHANCEMENTS

### New Features

- **Role Display**: Shows current user role in menu
- **Admin Panel**: Dedicated interface for administrators
- **Command Autocomplete**: Intelligent command completion
- **CSV Export**: Audit logs saved as CSV files
- **Dynamic Menus**: Context-aware menu display
- **Confirmation Prompts**: For destructive operations

### New Methods

- `handleAdminPanel()` - Admin interface
- `handleAssignRole()` - Role assignment UI
- `handleSuspendUser()` - Suspension interface
- `handleDeleteUser()` - Deletion interface
- `handleViewAudit()` - Audit viewing
- `handleExportAudit()` - Audit export
- `handleAutocomplete()` - Command completion

---

## 10. USAGE EXAMPLES

### Example 1: Admin Managing Users

```
Choice > ADMIN
Admin Panel
1. List All Users

Admin Choice > LIST_USERS
>>> ID | Username | Role | Active | Created | Last Login
    1 | admin    | ADMIN | Yes | 2026-01-15 | 2026-04-21
    2 | moderator1 | MODERATOR | Yes | 2026-02-10 | 2026-04-20
    3 | user1    | USER | Yes | 2026-03-05 | 2026-04-21

Admin Choice > ASSIGN_ROLE user1 ADMIN
>>> SUCCESS: Role assigned
```

### Example 2: Viewing Audit Trail

```
Choice > VIEW_AUDIT 10
>>> Username | Action | Resource | Status | Timestamp
admin | LOGIN | user:admin | SUCCESS | 2026-04-21 13:30:45
user1 | LOGIN | user:user1 | SUCCESS | 2026-04-21 13:30:50
admin | LIST_USERS | all_users | SUCCESS | 2026-04-21 13:30:52
```

### Example 3: Exporting Logs

```
Choice > EXPORT_AUDIT
>>> [*] Audit logs exported to audit_export_1713698645000.csv
```

Then open `audit_export_1713698645000.csv` in Excel or any spreadsheet application.

---

## 11. MIGRATION NOTES

If upgrading from v2.5:

1. Run `setup_db.bat` to update the database schema
2. Existing user accounts will be assigned the USER role by default
3. Manually assign ADMIN role to administrative accounts
4. Old sessions will be invalidated; users must login again
5. All previous session data is not transferred

---

## 12. TROUBLESHOOTING

### Issue: "Permission denied" error
**Solution**: Ensure your user role has the required permission. Check your role with STATUS command.

### Issue: Cannot delete admin user
**Solution**: This is intentional. Admin accounts cannot be deleted to prevent lockout.

### Issue: Audit logs not showing
**Solution**: Ensure you have `VIEW_AUDIT_LOGS` permission. Only ADMIN and MODERATOR roles have this.

### Issue: Notifications not received
**Solution**: Open a new client session to receive notifications for the current user.

---

## 13. FUTURE ENHANCEMENTS

Potential improvements:
- Two-Factor Authentication (2FA)
- OAuth2/JWT token support
- IP-based access restrictions
- Scheduled user deactivation
- Advanced reporting and analytics
- Email notifications
- REST API layer
- Web-based admin console

---

**Support**: For issues or feature requests, check logs in `audit_logs` table or review server console output.
