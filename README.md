# Pro Authentication Suite (v3.0)

A professional-grade multi-threaded Java application featuring enterprise-level secure authentication with **Role-Based Access Control (RBAC)**, comprehensive **audit logging**, advanced **admin panel**, and **real-time notifications**.

## ✨ New in v3.0

- **🔐 Role-Based Access Control**: Three role levels (ADMIN, MODERATOR, USER) with granular permission system
- **📋 Comprehensive Audit Logging**: Track all operations with timestamps, user, IP, and status
- **👨‍💼 Admin Panel**: Manage users, assign roles, suspend/delete accounts, and view statistics
- **🔔 Real-Time Notifications**: Get alerts for role changes, account suspensions, and security events
- **📊 Dashboard Statistics**: View user distribution, role breakdown, and login statistics
- **🎯 Command Autocomplete**: Intelligent command completion for faster interaction
- **📤 Audit Export**: Export logs to CSV for external analysis
- **🎨 Dynamic Menus**: Context-aware interface based on user role and authentication status

## 🚀 Quick Start (Recommended)

### Windows Users:
1. **Setup Database**: Run `setup_db.bat` to initialize MySQL with RBAC schema
2. **Run Application**: Double-click `run_all.bat` to:
   - Compile both client and server automatically
   - Start the Server in a separate window
   - Launch the Client (login with test accounts below)

### Linux/macOS Users:
```bash
# Setup database (ensure MySQL is running)
mysql -u root -p < server/schema.sql

# Build project
mvn clean compile

# Terminal 1: Run Server
mvn -f server/pom.xml exec:java -Dexec.mainClass="com.example.server.Server"

# Terminal 2: Run Client
mvn -f client/pom.xml exec:java -Dexec.mainClass="com.example.client.Client"
```

---

## 🎮 Key Features

### Role-Based Access Control (RBAC)
- **ADMIN**: Full system access, user management, audit logs, statistics
- **MODERATOR**: View users and logs, limited management capabilities
- **USER**: Standard access, manage own account only

### Audit & Logging
- Track login/logout events
- Monitor password changes
- Log role assignments and user deletions
- Record permission denials
- Export complete audit trail as CSV

### Admin Commands
```
LIST_USERS              List all users with roles and status
ASSIGN_ROLE <u> <role> Change user role (ADMIN/MODERATOR/USER)
SUSPEND_USER <user>     Disable user account temporarily
DELETE_USER <user>      Permanently remove user
VIEW_AUDIT <limit>      Display recent audit log entries
EXPORT_AUDIT            Export all logs to CSV file
DASHBOARD               View system statistics and metrics
NOTIFICATIONS           Check pending notifications
```

### User Commands
```
LOGIN <user> <pass>           Authenticate to system
REGISTER <user> <pass>        Create new account
STATUS                        Show session information
CHANGE_PASSWORD <old> <new>   Update password
LOGOUT                        End current session
EXIT                          Close application
```

---

## 🔒 Test Accounts

| Username | Password | Role | Capabilities |
|----------|----------|------|--------------|
| `admin` | `password123` | ADMIN | Full system access, user management |
| `moderator1` | `password123` | MODERATOR | View users/logs, limited admin access |
| `user1` | `mypassword` | USER | Standard user, manage own account |

---

## 📋 Technical Requirements

- **Java 17+** (Microsoft OpenJDK 17.0.17.10 recommended)
- **Maven 3.9.6+** (Must be in PATH)
- **MySQL 8.0+** (Running on localhost:3306)
- **JDBC Driver**: Included in Maven dependencies

### Project Structure
```
client-serveur/
├── client/                    # Client application
│   └── src/main/java/.../    # Client source code
├── server/                    # Server application
│   ├── schema.sql            # Database schema with RBAC
│   └── src/main/java/.../    # Server source code
├── pom.xml                   # Parent Maven configuration
├── run_all.bat               # Windows automation script
├── setup_db.bat              # Database setup script
├── README.md                 # This file
└── FEATURES_V3.md            # Detailed feature documentation
```

---

## 🏗 Architecture

### Server-Side (Multi-threaded)
- `Server.java` - Accepts TCP connections on port 5000
- `ClientHandler.java` - Processes client requests with RBAC validation
- `DatabaseConnection.java` - MySQL connection pooling
- `SecurityUtils.java` - SHA-256 password hashing
- `Role.java` - Role and permission management
- `Permission.java` - Fine-grained permission definitions
- `AuditLogger.java` - Asynchronous audit trail logging
- `NotificationManager.java` - Real-time notification system

### Client-Side (Interactive Console)
- Dynamic menu system with role-based options
- Command autocomplete for faster interaction
- Interactive admin panel for privileged users
- Real-time response handling from server
- CSV export functionality for audit logs

### Database (MySQL)
- `users` - User accounts with roles and activity tracking
- `roles` - Role definitions and descriptions
- `audit_logs` - Complete operation audit trail (indexed for performance)

---

## 🔐 Security Features

- **SHA-256 Password Hashing**: All passwords are one-way hashed
- **Permission Validation**: Every command checked against user role
- **IP Address Logging**: All operations tracked with client IP
- **Account Status Management**: Suspend accounts without deletion
- **Audit Trail**: Complete history of all operations
- **SQL Injection Prevention**: PreparedStatements used throughout
- **Session Management**: Unique UUID for each connection

---

## 📊 Database Schema

### Users Table
```sql
id              INT PRIMARY KEY
username        VARCHAR(50) UNIQUE
password        VARCHAR(255) -- SHA-256 hash
role_id         INT FOREIGN KEY to roles
is_active       BOOLEAN -- Account suspension flag
created_at      TIMESTAMP
last_login      TIMESTAMP
```

### Roles Table
```sql
id              INT PRIMARY KEY
role_name       VARCHAR(50) UNIQUE
description     VARCHAR(255)
created_at      TIMESTAMP
```

### Audit Logs Table
```sql
id              INT PRIMARY KEY
user_id         INT FOREIGN KEY to users
username        VARCHAR(50)
action          VARCHAR(100)
resource        VARCHAR(100)
details         VARCHAR(500)
ip_address      VARCHAR(45)
status          VARCHAR(20)
created_at      TIMESTAMP (indexed)
```

---

## 📖 Documentation

For comprehensive feature documentation, examples, troubleshooting, and best practices, see [FEATURES_V3.md](FEATURES_V3.md)

---

## 🚦 Common Operations

### Login as Admin
```
Choice > 1 (or LOGIN)
Username: admin
Password: password123
>>> SUCCESS: Welcome admin [ADMIN]
```

### View All Users
```
Choice > 8 (or ADMIN)
Admin Choice > 1 (or LIST_USERS)
>>> ID | Username | Role | Active | Created | Last Login
```

### Export Audit Logs
```
Choice > 8 (or ADMIN)
Admin Choice > 6 (or EXPORT_AUDIT)
>>> [*] Audit logs exported to audit_export_1713698645000.csv
```

### Assign Role to User
```
Choice > 8 (or ADMIN)
Admin Choice > 2 (or ASSIGN_ROLE)
Username: user1
Role: ADMIN
>>> SUCCESS: Role assigned
```

---

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| "MySQL not found" | Install MySQL 8.0+ or verify path in `setup_db.bat` |
| "Maven not found" | Add Maven to system PATH or verify installation |
| "Permission denied" | Check your user role permissions with STATUS command |
| "Cannot delete admin" | Admin account cannot be deleted for security |
| "Audit logs not showing" | Ensure ADMIN or MODERATOR role |

---

## 📈 Performance

- **Connection Pooling**: Optimized database connections
- **Indexed Queries**: Audit logs indexed by timestamp and user
- **Async Logging**: Non-blocking audit trail writes
- **Thread Pool**: Efficient multi-client handling
- **Memory Management**: Proper resource cleanup

---

## 🔄 Upgrade from v2.5

If upgrading from v2.5:
1. Run `setup_db.bat` to update database schema
2. Existing users assigned USER role by default
3. Manually promote accounts to ADMIN/MODERATOR as needed
4. Users must login again (old sessions invalidated)

---

## 📝 License

This project is provided as-is for educational and professional use.

---

## 🤝 Contributing

For feature requests, bug reports, or improvements:
1. Test thoroughly in your environment
2. Update documentation accordingly
3. Push to GitHub with descriptive commit messages

**Repository**: https://github.com/Imposter-zx/Java-Client-Server-Application

---

**Version**: 3.0 | **Last Updated**: April 2026 | **Status**: Production Ready ✅
