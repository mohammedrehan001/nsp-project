# 🔬 Medical Laboratory Management System (MedLab)

A modern, high-performance **Medical Laboratory Management System** built with **Java Swing** and **MySQL Database (JDBC)** (fully compatible with IntelliJ IDEA and MySQL Workbench). Developed by **Mohammad Rehan**.

---

## 🌟 Key Functional Requirements & Implementations

### 3. ➕ Add Record
- **User-Friendly Swing Forms**: Custom dialogs for adding new **Lab Instruments**, **Medicines / Reagents**, and **Consumables**.
- **Unique ID Auto-Generation**: Automatically assigns the next sequential ID (e.g. `INS-110`, `REA-109`, `CON-110`) and verifies uniqueness against existing primary keys.
- **Direct Database Storage**: Validated data is immediately saved into MySQL database tables via JDBC `PreparedStatement` (`INSERT INTO ...`).
- **Live UI Update**: The new row is immediately appended to the JTable, KPI stat cards update in real-time, and a confirmation alert is displayed.

### 4. ✏️ Update Record
- **Search & Selection**: Select any row directly in the JTable or use the instant search filter, then click `[ ✎ Edit ]` on the row or `[ ✏️ Edit Selected ]` in the Toolbar/Menu Bar (`Ctrl+E`).
- **Pre-Populated Data**: Opens the form with existing record details loaded into the respective input fields, date pickers, and dropdowns.
- **Data Validation & Update**: Changes are strictly validated and committed to MySQL via JDBC `PreparedStatement` (`UPDATE ... WHERE id = ?`).
- **Instant Table Refresh**: The JTable model is updated live without restarting the application.

### 5. 🗑️ Delete Record
- **Role-Based Authorization**: Only authorized **Administrators** can permanently delete records. Staff attempts are securely blocked with an access alert.
- **Accidental Deletion Prevention**: Prompts a confirmation dialog displaying the exact ID and Name of the record before deletion (`Confirm Permanent Deletion`).
- **Database Removal**: Executes SQL `DELETE FROM ... WHERE id = ?` via JDBC PreparedStatement.
- **UI Synchronization**: Removes the row from the JTable, recalculates laboratory inventory KPI stats, and displays a success message.

### 6. 📊 View All Records (JTable)
- **Interactive JTable**: Displays all records loaded from the MySQL database.
- **Multi-Column Sorting**: Integrated `TableRowSorter` allows sorting any column ascending or descending with a single header click.
- **Smooth Scrolling**: Enclosed inside modern styled `JScrollPane`.
- **Instant Search Filter**: Live text box filters rows in real-time across all columns as you type.
- **Custom Renderers & Action Badges**: Status pills (`Operational`, `Calibration Required`, `Under Maintenance`), low stock alert highlights, and interactive `[ ✎ Edit ]` and `[ ✖ Delete ]` action buttons on each row.
- **Accurate Model-to-View Mapping**: Uses `table.convertRowIndexToModel()` so actions always edit/delete the correct record even when filtered or sorted.

### 7. 🗄️ MySQL Database Connectivity (JDBC)
- **Central Connection Manager (`DBConnection.java`)**:
  - Connects via official MySQL Connector/J driver (`com.mysql.cj.jdbc.Driver`).
  - Pre-configured URL: `jdbc:mysql://localhost:3306/medlab_db`.
  - Credentials stored and configurable via UI or `db_config.properties`.
- **Data Access Object (`LabDAO.java`)**:
  - Full CRUD operations via parameterized `PreparedStatement` (prevents SQL injection).
  - Handles table initialization and data seeding automatically.
- **Live Status Diagnostics**: A real-time connection badge on the Toolbar and Login page indicates `🟢 MySQL: Connected (medlab_db)` or `🟡 Offline (File Backup Mode)`.
- **Automatic Fallback Safety**: If MySQL Server is temporarily offline, the system seamlessly uses local file persistence (`inventory_*.txt`), guaranteeing 100% uptime and zero crashes!

### 8. 🛡️ Form Validation
- **Mandatory Fields Validation**: Validates that required fields (ID, Name, Manufacturer, Model, Serial, Location, Batch, Supplier) are completed and not empty whitespace.
- **Numeric & Range Validation**: Ensures Quantity and Minimum Stock are non-negative integers (`>= 0`), and Unit Price is a valid positive decimal number (`>= 0.00`).
- **Duplicate ID Prevention**: Checks against both the live MySQL database and table model before insertion to preserve Primary Key integrity.
- **Helpful User Alerts**: Highlights specific validation failures in clear dialog messages before submission.

### 9. 🎛️ Menu Bar & Toolbar
- **Full Swing `JMenuBar`**:
  - **File**: `Add New Record (Ctrl+N)`, `Refresh Data (F5)`, `Sync with MySQL`, `Logout (Ctrl+L)`, `Exit (Alt+F4)`
  - **Records**: `Add Lab Instrument`, `Add Medicine / Reagent`, `Add Consumable Supply`, `Edit Selected Record (Ctrl+E)`, `Delete Selected Record (Del)`
  - **View**: `Home / Dashboard`, `Lab Inventory Directory`, `User Profile & Password`, `About System`
  - **MySQL Database**: `Test MySQL Connection`, `Database Connection Settings...`, `Reset / Re-import Sample Data`
  - **Help**: `MySQL Workbench Setup Guide`, `About MedLab Workstation`
- **Modern Sleek `JToolBar`**:
  - Shortcut buttons: `[ ➕ Add Record ]`, `[ ✏️ Edit Selected ]`, `[ 🗑️ Delete Selected ]`, `[ 🔄 Refresh Data ]`.
  - Quick Category Jump Buttons: `[ 🔬 Instruments ]`, `[ 🧪 Reagents ]`, `[ 📦 Consumables ]`.
  - Interactive Connection Badge: Click `[ 🟢 MySQL: Connected (medlab_db) ]` anytime to inspect connection parameters, database version, and table record counts.
  - One-click `[ 🚪 Logout ]` shortcut.

---

## 🐬 MySQL Workbench Setup & Verification

A complete, self-contained SQL setup script is provided in the root directory: **[`medlab_schema.sql`](file:///c:/Users/rehan/Downloads/Rehan%20project/medlab_schema.sql)**.

### How to inspect in MySQL Workbench:
1. Open **MySQL Workbench**.
2. Connect to your MySQL server (e.g. `Local instance MySQL80` on port `3306`).
3. Click **File -> Open SQL Script...** and select `medlab_schema.sql`.
4. Click the yellow **Lightning Bolt ⚡** icon to execute.
5. In the left **SCHEMAS** panel, right-click and click **Refresh All**.
6. Expand **`medlab_db`** -> **`Tables`**:
   - `instruments` (9 default records)
   - `reagents` (8 default records)
   - `consumables` (9 default records)
   - `users` (2 default accounts)
7. Right-click on any table and select **Select Rows - Limit 1000** to view table rows.
8. Any record added or modified in the MedLab Java application will instantly appear in MySQL Workbench!

---

## 🔑 Login Credentials

| Role | Email / Username | Default Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin@poornima.org` *(or `admin`)* | `admin` | Full CRUD Rights: Add, Edit, Delete, Change Password, Database Sync |
| **Staff** | `staff@poornima.org` *(or `staff`)* | `staff` | Staff Operations: View Full Inventory, Categories, Profile, Change Password |

> 💡 **Tip:** Login credentials are pre-filled by default for instant testing. Simply click **LOGIN**!

---

## 🚀 How to Run the Application

### Method 1: One-Click Execution (Windows Batch Script)
Double-click [`run.bat`](file:///c:/Users/rehan/Downloads/Rehan%20project/run.bat).
*(The script automatically verifies compilation, loads the MySQL JDBC connector from `lib/`, and starts the application).*

### Method 2: One-Click Compilation
Double-click [`compile.bat`](file:///c:/Users/rehan/Downloads/Rehan%20project/compile.bat).

### Method 3: Run via IntelliJ IDEA
1. Open this folder in IntelliJ IDEA.
2. Open [`src/Main.java`](file:///c:/Users/rehan/Downloads/Rehan%20project/src/Main.java) or [`src/frame.java`](file:///c:/Users/rehan/Downloads/Rehan%20project/src/frame.java).
3. Click the green **Run ▶** button!

---

## 📁 Project Structure

```
Rehan project/
├── medlab_schema.sql             # Complete MySQL Workbench database creation & seed script
├── db_config.properties          # MySQL JDBC connection configuration
├── compile.bat                   # 1-click Windows compiler with JDK auto-detection
├── run.bat                       # 1-click application launcher with MySQL JDBC classpath
├── Rehan project.iml             # IntelliJ module file with MySQL Connector/J library
├── lib/
│   └── mysql-connector-j-8.4.0.jar # Official MySQL JDBC Driver
├── admin_pwd.txt                 # Admin password backup file
├── staff_pwd.txt                 # Staff password backup file
├── inventory_instruments.txt     # Instruments offline cache & backup
├── inventory_reagents.txt        # Reagents offline cache & backup
├── inventory_consumables.txt     # Consumables offline cache & backup
├── README.md                     # Documentation
└── src/
    ├── Main.java                 # Main application entry point
    ├── frame.java                # Login page with MySQL authentication & DB status badge
    ├── Dashboard.java            # Main workstation console with Menu Bar, Toolbar & JTables
    ├── DBConnection.java         # MySQL JDBC connection manager & diagnostics
    ├── LabDAO.java               # Data Access Object executing MySQL CRUD queries
    ├── RoundedButton.java        # Modern UI rounded button component
    ├── RoundedTextField.java     # Modern UI rounded text field component
    ├── RoundedPasswordField.java # Custom styled password field with toggle eye
    ├── RoundedPanel.java         # Custom card panel container
    └── images/                   # Header banners and assets
```

---

## 👨‍💻 Developer Credits
- **Project**: Medical Laboratory Management System (MedLab)
- **Developer**: Mohammad Rehan
- **Technologies**: Pure Java Swing, JDBC, MySQL 8.0, MySQL Workbench
