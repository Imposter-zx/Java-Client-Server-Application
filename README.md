# Enhanced Java Client-Server Application (v2.0)

A multi-threaded Java application featuring secure authentication, professional console UI, and interactive commands.

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
