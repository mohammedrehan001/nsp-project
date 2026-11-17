import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LabDAO - Data Access Object for MySQL Database Operations
 * Medical Laboratory Management System (MedLab)
 * 
 * Provides full CRUD operations for:
 * 1. Lab Instruments
 * 2. Medicines / Reagents
 * 3. Consumable Supplies
 * 4. User Authentication & Password Management
 * 
 * Includes automated fallback to local storage if MySQL is unreachable.
 */
public class LabDAO {

    // =========================================================================
    // 1. AUTO-INITIALIZE DATABASE & TABLES
    // =========================================================================
    public static boolean initDatabaseTables() {
        try {
            // First connect to root server to ensure database exists
            try (Connection rootConn = DriverManager.getConnection(DBConnection.getRootServerUrl(), DBConnection.getUser(), DBConnection.getPassword());
                 Statement stmt = rootConn.createStatement()) {
                stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DBConnection.getDbName() + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            } catch (Exception ignored) {}

            // Now connect to medlab_db
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Create users table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS users (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY," +
                        "username VARCHAR(50) NOT NULL UNIQUE," +
                        "email VARCHAR(100) NOT NULL UNIQUE," +
                        "password VARCHAR(100) NOT NULL," +
                        "role VARCHAR(20) NOT NULL DEFAULT 'Admin'," +
                        "full_name VARCHAR(100) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create instruments table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS instruments (" +
                        "id VARCHAR(50) PRIMARY KEY," +
                        "name VARCHAR(150) NOT NULL," +
                        "category VARCHAR(100) NOT NULL DEFAULT 'Lab Instruments'," +
                        "manufacturer VARCHAR(150) NOT NULL," +
                        "model VARCHAR(100) NOT NULL," +
                        "serial_no VARCHAR(100) NOT NULL," +
                        "quantity INT NOT NULL DEFAULT 1," +
                        "location VARCHAR(150) NOT NULL," +
                        "purchase_date VARCHAR(50) NOT NULL," +
                        "status VARCHAR(50) NOT NULL DEFAULT 'Operational'," +
                        "last_maintenance VARCHAR(50) NOT NULL," +
                        "next_maintenance VARCHAR(50) NOT NULL," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create reagents table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS reagents (" +
                        "id VARCHAR(50) PRIMARY KEY," +
                        "name VARCHAR(150) NOT NULL," +
                        "category VARCHAR(100) NOT NULL DEFAULT 'Medicines / Reagents'," +
                        "manufacturer VARCHAR(150) NOT NULL," +
                        "batch_no VARCHAR(100) NOT NULL," +
                        "quantity INT NOT NULL DEFAULT 1," +
                        "price DECIMAL(10,2) NOT NULL DEFAULT 0.00," +
                        "expiry_date VARCHAR(50) NOT NULL," +
                        "supplier VARCHAR(150) NOT NULL," +
                        "storage_location VARCHAR(150) NOT NULL," +
                        "min_stock INT NOT NULL DEFAULT 5," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Create consumables table
                stmt.executeUpdate("CREATE TABLE IF NOT EXISTS consumables (" +
                        "id VARCHAR(50) PRIMARY KEY," +
                        "name VARCHAR(150) NOT NULL," +
                        "category VARCHAR(100) NOT NULL DEFAULT 'Consumables'," +
                        "quantity INT NOT NULL DEFAULT 1," +
                        "unit VARCHAR(50) NOT NULL," +
                        "batch_no VARCHAR(100) NOT NULL," +
                        "expiry_date VARCHAR(50) NOT NULL," +
                        "supplier VARCHAR(150) NOT NULL," +
                        "min_stock INT NOT NULL DEFAULT 10," +
                        "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                        ")");

                // Seed default users if empty
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
                if (rs.next() && rs.getInt(1) == 0) {
                    stmt.executeUpdate("INSERT INTO users (username, email, password, role, full_name) VALUES " +
                            "('admin', 'admin@poornima.org', 'admin', 'Admin', 'Mohammad Rehan (Administrator)')," +
                            "('staff', 'staff@poornima.org', 'staff', 'Staff', 'Laboratory Technician (Staff)')");
                }
                rs.close();

                return true;
            }
        } catch (Exception e) {
            System.err.println("[LabDAO] Error initializing tables: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // 2. INSTRUMENTS CRUD
    // =========================================================================
    public static List<String[]> getAllInstruments() {
        List<String[]> list = new ArrayList<>();
        if (DBConnection.isConnected()) {
            String sql = "SELECT id, name, category, manufacturer, model, serial_no, quantity, location, purchase_date, status, last_maintenance, next_maintenance FROM instruments ORDER BY id ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new String[]{
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("manufacturer"),
                            rs.getString("model"),
                            rs.getString("serial_no"),
                            String.valueOf(rs.getInt("quantity")),
                            rs.getString("location"),
                            rs.getString("purchase_date"),
                            rs.getString("status"),
                            rs.getString("last_maintenance"),
                            rs.getString("next_maintenance")
                    });
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error fetching instruments: " + e.getMessage());
            }
        }
        // Fallback to text file
        return loadFromFile("inventory_instruments.txt", 12);
    }

    public static boolean insertInstrument(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "INSERT INTO instruments (id, name, category, manufacturer, model, serial_no, quantity, location, purchase_date, status, last_maintenance, next_maintenance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[0]);
                ps.setString(2, data[1]);
                ps.setString(3, data[2]);
                ps.setString(4, data[3]);
                ps.setString(5, data[4]);
                ps.setString(6, data[5]);
                ps.setInt(7, Integer.parseInt(data[6].replaceAll("[^0-9]", "")));
                ps.setString(8, data[7]);
                ps.setString(9, data[8]);
                ps.setString(10, data[9]);
                ps.setString(11, data[10]);
                ps.setString(12, data[11]);
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error inserting instrument: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean updateInstrument(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "UPDATE instruments SET name=?, category=?, manufacturer=?, model=?, serial_no=?, quantity=?, location=?, purchase_date=?, status=?, last_maintenance=?, next_maintenance=? WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[1]);
                ps.setString(2, data[2]);
                ps.setString(3, data[3]);
                ps.setString(4, data[4]);
                ps.setString(5, data[5]);
                ps.setInt(6, Integer.parseInt(data[6].replaceAll("[^0-9]", "")));
                ps.setString(7, data[7]);
                ps.setString(8, data[8]);
                ps.setString(9, data[9]);
                ps.setString(10, data[10]);
                ps.setString(11, data[11]);
                ps.setString(12, data[0]);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error updating instrument: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean deleteInstrument(String id) {
        if (DBConnection.isConnected()) {
            String sql = "DELETE FROM instruments WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error deleting instrument: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean instrumentExists(String id) {
        if (DBConnection.isConnected()) {
            String sql = "SELECT COUNT(*) FROM instruments WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    // =========================================================================
    // 3. REAGENTS CRUD
    // =========================================================================
    public static List<String[]> getAllReagents() {
        List<String[]> list = new ArrayList<>();
        if (DBConnection.isConnected()) {
            String sql = "SELECT id, name, category, manufacturer, batch_no, quantity, price, expiry_date, supplier, storage_location, min_stock FROM reagents ORDER BY id ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new String[]{
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("manufacturer"),
                            rs.getString("batch_no"),
                            String.valueOf(rs.getInt("quantity")),
                            String.valueOf(rs.getDouble("price")),
                            rs.getString("expiry_date"),
                            rs.getString("supplier"),
                            rs.getString("storage_location"),
                            String.valueOf(rs.getInt("min_stock"))
                    });
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error fetching reagents: " + e.getMessage());
            }
        }
        return loadFromFile("inventory_reagents.txt", 11);
    }

    public static boolean insertReagent(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "INSERT INTO reagents (id, name, category, manufacturer, batch_no, quantity, price, expiry_date, supplier, storage_location, min_stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[0]);
                ps.setString(2, data[1]);
                ps.setString(3, data[2]);
                ps.setString(4, data[3]);
                ps.setString(5, data[4]);
                ps.setInt(6, Integer.parseInt(data[5].replaceAll("[^0-9]", "")));
                ps.setDouble(7, Double.parseDouble(data[6].replaceAll("[^0-9.]", "")));
                ps.setString(8, data[7]);
                ps.setString(9, data[8]);
                ps.setString(10, data[9]);
                ps.setInt(11, Integer.parseInt(data[10].replaceAll("[^0-9]", "")));
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error inserting reagent: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean updateReagent(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "UPDATE reagents SET name=?, category=?, manufacturer=?, batch_no=?, quantity=?, price=?, expiry_date=?, supplier=?, storage_location=?, min_stock=? WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[1]);
                ps.setString(2, data[2]);
                ps.setString(3, data[3]);
                ps.setString(4, data[4]);
                ps.setInt(5, Integer.parseInt(data[5].replaceAll("[^0-9]", "")));
                ps.setDouble(6, Double.parseDouble(data[6].replaceAll("[^0-9.]", "")));
                ps.setString(7, data[7]);
                ps.setString(8, data[8]);
                ps.setString(9, data[9]);
                ps.setInt(10, Integer.parseInt(data[10].replaceAll("[^0-9]", "")));
                ps.setString(11, data[0]);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error updating reagent: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean deleteReagent(String id) {
        if (DBConnection.isConnected()) {
            String sql = "DELETE FROM reagents WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error deleting reagent: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean reagentExists(String id) {
        if (DBConnection.isConnected()) {
            String sql = "SELECT COUNT(*) FROM reagents WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    // =========================================================================
    // 4. CONSUMABLES CRUD
    // =========================================================================
    public static List<String[]> getAllConsumables() {
        List<String[]> list = new ArrayList<>();
        if (DBConnection.isConnected()) {
            String sql = "SELECT id, name, category, quantity, unit, batch_no, expiry_date, supplier, min_stock FROM consumables ORDER BY id ASC";
            try (Connection conn = DBConnection.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new String[]{
                            rs.getString("id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            String.valueOf(rs.getInt("quantity")),
                            rs.getString("unit"),
                            rs.getString("batch_no"),
                            rs.getString("expiry_date"),
                            rs.getString("supplier"),
                            String.valueOf(rs.getInt("min_stock"))
                    });
                }
                return list;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error fetching consumables: " + e.getMessage());
            }
        }
        return loadFromFile("inventory_consumables.txt", 9);
    }

    public static boolean insertConsumable(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "INSERT INTO consumables (id, name, category, quantity, unit, batch_no, expiry_date, supplier, min_stock) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[0]);
                ps.setString(2, data[1]);
                ps.setString(3, data[2]);
                ps.setInt(4, Integer.parseInt(data[3].replaceAll("[^0-9]", "")));
                ps.setString(5, data[4]);
                ps.setString(6, data[5]);
                ps.setString(7, data[6]);
                ps.setString(8, data[7]);
                ps.setInt(9, Integer.parseInt(data[8].replaceAll("[^0-9]", "")));
                ps.executeUpdate();
                return true;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error inserting consumable: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean updateConsumable(String[] data) {
        if (DBConnection.isConnected()) {
            String sql = "UPDATE consumables SET name=?, category=?, quantity=?, unit=?, batch_no=?, expiry_date=?, supplier=?, min_stock=? WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, data[1]);
                ps.setString(2, data[2]);
                ps.setInt(3, Integer.parseInt(data[3].replaceAll("[^0-9]", "")));
                ps.setString(4, data[4]);
                ps.setString(5, data[5]);
                ps.setString(6, data[6]);
                ps.setString(7, data[7]);
                ps.setInt(8, Integer.parseInt(data[8].replaceAll("[^0-9]", "")));
                ps.setString(9, data[0]);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error updating consumable: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean deleteConsumable(String id) {
        if (DBConnection.isConnected()) {
            String sql = "DELETE FROM consumables WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[LabDAO] Error deleting consumable: " + e.getMessage());
            }
        }
        return false;
    }

    public static boolean consumableExists(String id) {
        if (DBConnection.isConnected()) {
            String sql = "SELECT COUNT(*) FROM consumables WHERE id=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1) > 0;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    // =========================================================================
    // 5. USER AUTHENTICATION & PASSWORD UPDATE
    // =========================================================================
    public static String authenticateUser(String emailOrUsername, String password) {
        if (DBConnection.isConnected()) {
            String sql = "SELECT role FROM users WHERE (email=? OR username=?) AND password=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, emailOrUsername);
                ps.setString(2, emailOrUsername);
                ps.setString(3, password);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("role");
                    }
                }
            } catch (Exception e) {
                System.err.println("[LabDAO] Authentication query error: " + e.getMessage());
            }
        }
        // Fallback to text password files
        if ((emailOrUsername.equalsIgnoreCase("admin@poornima.org") || emailOrUsername.equalsIgnoreCase("admin")) && password.equals(frame.getAdminPassword())) {
            return "Admin";
        }
        if ((emailOrUsername.equalsIgnoreCase("staff@poornima.org") || emailOrUsername.equalsIgnoreCase("staff")) && password.equals(frame.getStaffPassword())) {
            return "Staff";
        }
        return null;
    }

    public static boolean updatePassword(String role, String newPassword) {
        boolean ok = false;
        if (DBConnection.isConnected()) {
            String sql = "UPDATE users SET password=? WHERE role=?";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newPassword);
                ps.setString(2, role);
                ok = ps.executeUpdate() > 0;
            } catch (Exception e) {
                System.err.println("[LabDAO] Password update error: " + e.getMessage());
            }
        }
        // Always sync with local file too
        if ("Admin".equalsIgnoreCase(role)) {
            frame.updateAdminPassword(newPassword);
        } else {
            frame.updateStaffPassword(newPassword);
        }
        return ok;
    }

    // =========================================================================
    // 6. HELPER: FILE FALLBACK LOADER
    // =========================================================================
    private static List<String[]> loadFromFile(String filename, int columnCount) {
        List<String[]> list = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return list;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split("\\|\\|");
                String[] row = new String[columnCount];
                for (int i = 0; i < columnCount; i++) {
                    row[i] = (i < parts.length) ? parts[i].trim() : "";
                }
                list.add(row);
            }
        } catch (Exception e) {
            System.err.println("[LabDAO] Error loading file " + filename + ": " + e.getMessage());
        }
        return list;
    }
}
