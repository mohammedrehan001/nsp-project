import java.io.*;
import java.sql.*;
import java.util.Properties;


public class DBConnection {

    private static final String CONFIG_FILE = "db_config.properties";

    private static String host = "localhost";
    private static String port = "3306";
    private static String dbName = "medlab_db";
    private static String user = "root";
    private static String password = "root";

    private static boolean driverLoaded = false;

    static {
        loadConfig();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            System.err.println("[DBConnection] MySQL JDBC Driver not found in classpath!");
            driverLoaded = false;
        }
    }

    public static void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                Properties props = new Properties();
                props.load(in);
                host = props.getProperty("db.host", "localhost").trim();
                port = props.getProperty("db.port", "3306").trim();
                dbName = props.getProperty("db.name", "medlab_db").trim();
                user = props.getProperty("db.user", "root").trim();
                password = props.getProperty("db.password", "root").trim();
            } catch (Exception e) {
                System.err.println("[DBConnection] Error loading config: " + e.getMessage());
            }
        } else {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileOutputStream out = new FileOutputStream(CONFIG_FILE)) {
            Properties props = new Properties();
            props.setProperty("db.host", host);
            props.setProperty("db.port", port);
            props.setProperty("db.name", dbName);
            props.setProperty("db.user", user);
            props.setProperty("db.password", password);
            props.store(out, "Medical Laboratory Management System - MySQL Configuration");
        } catch (Exception e) {
            System.err.println("[DBConnection] Error saving config: " + e.getMessage());
        }
    }

    public static void setCredentials(String newHost, String newPort, String newDbName, String newUser, String newPassword) {
        host = newHost;
        port = newPort;
        dbName = newDbName;
        user = newUser;
        password = newPassword;
        saveConfig();
    }

    public static String getUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + dbName + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public static String getRootServerUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&characterEncoding=UTF-8";
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC Driver (com.mysql.cj.jdbc.Driver) missing from classpath!");
            }
        }
        return DriverManager.getConnection(getUrl(), user, password);
    }

    public static boolean isConnected() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            return false;
        }
    }

    public static String testConnectionDetails() {
        StringBuilder sb = new StringBuilder();
        try (Connection conn = getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            sb.append("Connection Status: SUCCESSFUL\n");
            sb.append("Database Product: ").append(meta.getDatabaseProductName()).append(" v").append(meta.getDatabaseProductVersion()).append("\n");
            sb.append("Driver Name: ").append(meta.getDriverName()).append(" v").append(meta.getDriverVersion()).append("\n");
            sb.append("Connected URL: ").append(meta.getURL()).append("\n");
            sb.append("Current User: ").append(meta.getUserName()).append("\n");
            sb.append("Active Database: ").append(conn.getCatalog()).append("\n");

            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 'instruments', COUNT(*) FROM instruments UNION ALL " +
                                             "SELECT 'reagents', COUNT(*) FROM reagents UNION ALL " +
                                             "SELECT 'consumables', COUNT(*) FROM consumables UNION ALL " +
                                             "SELECT 'users', COUNT(*) FROM users");
            sb.append("\nTable Record Summary:\n");
            while (rs.next()) {
                sb.append(" • ").append(rs.getString(1)).append(": ").append(rs.getInt(2)).append(" records\n");
            }
            rs.close();
            stmt.close();
            return sb.toString();
        } catch (Exception e) {
            return "Connection Failed!\nError: " + e.getMessage() + "\n\nHost: " + host + ":" + port + "\nUser: " + user;
        }
    }

    public static String getHost() { return host; }
    public static String getPort() { return port; }
    public static String getDbName() { return dbName; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }

    public static void main(String[] args) {
        System.out.println(testConnectionDetails());
    }
}
