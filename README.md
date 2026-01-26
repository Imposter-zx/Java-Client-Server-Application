# Pro Authentication Suite (v2.5)

A professional-grade multi-threaded Java application featuring enterprise-level secure authentication, enhanced session tracking, and a rich console UI.

## ✨ New in v2.5

- **Enterprise Branding**: Professional ASCII banners and bold color schemes.
- **Advanced Session Tracking**: Real-time Uptime and Idle metrics via the `STATUS` command.
- **Robust Input Validation**: Strict checks on usernames (no spaces) and mandatory field validation.
- **Improved UX**: Dynamic loading simulations for a smoother terminal experience.

## 🚀 Simple Run (Recommended)

1.  **Setup Database**: Double-click `setup_db.bat` to initialize the MySQL database (or import `server/schema.sql` manually).
2.  **Run Application**: Double-click `run_all.bat`. This will:
    - Compile both client and server automatically.
    - Start the Server in a separate window.
    - Start the Client in the current window.

---

## 🛠 Manual Execution

If you prefer manual control or are on Linux/macOS:

1.  **Build**: Run `mvn clean compile` in the root directory.
2.  **Run Server**:
    ```bash
    mvn -f server/pom.xml exec:java -Dexec.mainClass="com.example.server.Server"
    ```
3.  **Run Client**:
    ```bash
    mvn -f client/pom.xml exec:java -Dexec.mainClass="com.example.client.Client"
    ```

## 📋 Technical Requirements

- **Java 17+**
- **Maven** (Must be in PATH)
- **MySQL** (Running locally on port 3306)
- **JDBC Driver**: Included in dependencies.

## 🔒 Test Accounts

- User: `admin` | Password: `password123`
- User: `user1` | Password: `mypassword`
