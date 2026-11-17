import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

// =========================================================================
// Medical Laboratory Management System - Main Dashboard
// Horizontal top-nav layout with Lab Inventory / Lab Items Resource Management
// =========================================================================
public class Dashboard {
    private boolean isAdmin;

    private JFrame dashFrame;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // View Tracking & Toolbar / Status components
    private String currentTabName = "Home";
    private String currentInvCategory = "Instruments";
    private JButton btnDbStatusBadge;
    private JToolBar toolBar;

    // Table Models & Tables for Inventory Categories
    private DefaultTableModel overviewTableModel;
    private JTable overviewTable;

    private DefaultTableModel instrumentsTableModel;
    private JTable instrumentsTable;

    private DefaultTableModel reagentsTableModel;
    private JTable reagentsTable;

    private DefaultTableModel consumablesTableModel;
    private JTable consumablesTable;

    // Metric Labels
    private JLabel lblTotalInventory, lblTotalInstruments, lblTotalReagents, lblLowStockAlerts;
    private JLabel lblProfileInstruments, lblProfileReagents, lblProfileConsumables;

    // Navigation Buttons
    private JButton btnHome, btnInventory, btnAbout, btnProfile;

    // Category Toggle Buttons in Inventory View
    private JButton btnCatInstruments, btnCatReagents, btnCatConsumables;
    private CardLayout inventoryCatLayout;
    private JPanel inventoryCatCards;

    public Dashboard(boolean isAdmin) {
        this.isAdmin = isAdmin;
        initDashboard();
    }

    private void initDashboard() {
        // ================= 1. MAIN FRAME =================
        dashFrame = new JFrame("Medical Laboratory Management System - Workstation Console");
        dashFrame.setSize(1280, 800);
        dashFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        dashFrame.setLocationRelativeTo(null);
        dashFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashFrame.setLayout(new BorderLayout());

        // Attach Menu Bar (Requirement 9: Menu Bar & Shortcuts)
        dashFrame.setJMenuBar(createMenuBar());

        // ================= 2. TOP HORIZONTAL NAVIGATION BAR =================
        JPanel topNavBar = new JPanel(new BorderLayout());
        topNavBar.setBackground(new Color(10, 61, 98)); // Deep Ocean Blue
        topNavBar.setPreferredSize(new Dimension(1200, 62));
        topNavBar.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Left Branding
        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 14));
        brandPanel.setOpaque(false);

        JLabel lblLogo = new JLabel("🔬");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));

        JLabel lblTitle = new JLabel("MEDLAB SYSTEM");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);

        JLabel lblRoleBadge = new JLabel(isAdmin ? "[ ADMIN ]" : "[ STAFF ]");
        lblRoleBadge.setFont(new Font("Arial", Font.BOLD, 11));
        lblRoleBadge.setForeground(isAdmin ? new Color(46, 204, 113) : new Color(52, 152, 219));

        brandPanel.add(lblLogo);
        brandPanel.add(lblTitle);
        brandPanel.add(lblRoleBadge);
        topNavBar.add(brandPanel, BorderLayout.WEST);

        // Center Navigation Tabs
        JPanel centerNav = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 12));
        centerNav.setOpaque(false);

        btnHome = createNavTab("Home");
        btnInventory = createNavTab("Lab Inventory");
        btnAbout = createNavTab("About");
        btnProfile = createNavTab("Profile");

        JButton[] allTabs = {btnHome, btnInventory, btnAbout, btnProfile};

        // Tab Active State Setter
        java.util.function.Consumer<JButton> setActiveTab = (activeBtn) -> {
            for (JButton b : allTabs) {
                if (b == activeBtn) {
                    b.setForeground(Color.WHITE);
                    b.setFont(new Font("Arial", Font.BOLD, 14));
                    b.setBorder(BorderFactory.createMatteBorder(0, 0, 4, 0, new Color(16, 185, 129))); // Mint indicator
                } else {
                    b.setForeground(new Color(189, 195, 199));
                    b.setFont(new Font("Arial", Font.PLAIN, 14));
                    b.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
                }
            }
            topNavBar.revalidate();
            topNavBar.repaint();
        };

        setActiveTab.accept(btnHome);

        btnHome.addActionListener(e -> {
            currentTabName = "Home";
            refreshOverviewTable();
            recalculateStats();
            cardLayout.show(cardPanel, "Home");
            setActiveTab.accept(btnHome);
        });
        btnInventory.addActionListener(e -> {
            currentTabName = "Inventory";
            cardLayout.show(cardPanel, "Inventory");
            setActiveTab.accept(btnInventory);
        });
        btnAbout.addActionListener(e -> {
            currentTabName = "About";
            cardLayout.show(cardPanel, "About");
            setActiveTab.accept(btnAbout);
        });
        btnProfile.addActionListener(e -> {
            currentTabName = "Profile";
            if (lblProfileInstruments != null) {
                lblProfileInstruments.setText(String.valueOf(instrumentsTableModel.getRowCount()));
                lblProfileReagents.setText(String.valueOf(reagentsTableModel.getRowCount()));
                lblProfileConsumables.setText(String.valueOf(consumablesTableModel.getRowCount()));
            }
            cardLayout.show(cardPanel, "Profile");
            setActiveTab.accept(btnProfile);
        });

        centerNav.add(btnHome);
        centerNav.add(btnInventory);
        centerNav.add(btnAbout);
        centerNav.add(btnProfile);
        topNavBar.add(centerNav, BorderLayout.CENTER);

        // Right Action: Logout Button
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 14));
        rightActions.setOpaque(false);

        RoundedButton btnLogout = new RoundedButton("Logout", 16);
        btnLogout.setBackground(new Color(231, 76, 60)); // Coral Red
        btnLogout.setForeground(Color.WHITE);
        btnLogout.setFont(new Font("Arial", Font.BOLD, 12));
        btnLogout.setPreferredSize(new Dimension(85, 32));
        btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogout.addActionListener(e -> performLogout());
        rightActions.add(btnLogout);
        topNavBar.add(rightActions, BorderLayout.EAST);

        // Container combining Top Navigation Bar + Modern Toolbar
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(topNavBar, BorderLayout.NORTH);
        toolBar = createToolBar();
        topContainer.add(toolBar, BorderLayout.SOUTH);
        dashFrame.add(topContainer, BorderLayout.NORTH);

        // ================= 3. INITIALIZE TABLE MODELS & DATA =================
        initDataModels();

        // ================= 4. CENTER CARD LAYOUT VIEWS =================
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(new Color(245, 246, 250));


        // -------------------------------------------------------------
        // --- 1. HOME VIEW (Stats + Overview Table) ---
        // -------------------------------------------------------------
        JPanel homeView = createHomeView();

        // -------------------------------------------------------------
        // --- 2. LAB INVENTORY VIEW (Category Tabs & Multi-Tables) ---
        // -------------------------------------------------------------
        JPanel inventoryView = createInventoryView();

        // -------------------------------------------------------------
        // --- 3. ABOUT VIEW ---
        // -------------------------------------------------------------
        JPanel aboutView = createAboutView();

        // -------------------------------------------------------------
        // --- 4. PROFILE VIEW ---
        // -------------------------------------------------------------
        JPanel profileView = createProfileView();

        // Add views to main CardPanel
        cardPanel.add(homeView, "Home");
        cardPanel.add(inventoryView, "Inventory");
        cardPanel.add(aboutView, "About");
        cardPanel.add(profileView, "Profile");

        dashFrame.add(cardPanel, BorderLayout.CENTER);

        // Populate Overview & Recalculate
        refreshOverviewTable();
        recalculateStats();

        // If Staff, hide action columns
        if (!isAdmin) {
            hideActionColumn(overviewTable);
            hideActionColumn(instrumentsTable);
            hideActionColumn(reagentsTable);
            hideActionColumn(consumablesTable);
        }

        dashFrame.setVisible(true);
    }

    private void hideActionColumn(JTable table) {
        if (table != null && table.getColumnCount() > 0) {
            table.removeColumn(table.getColumnModel().getColumn(table.getColumnCount() - 1));
        }
    }

    // =========================================================================
    // INITIALIZE DATA MODELS
    // =========================================================================
    private void initDataModels() {
        // 1. Instruments Table Model
        String[] instCols = {"Instrument ID", "Instrument Name", "Category", "Manufacturer", "Model", "Serial No", "Qty", "Location", "Purchase Date", "Status", "Last Maint.", "Next Maint.", "Actions"};
        instrumentsTableModel = new DefaultTableModel(null, instCols) {
            @Override public boolean isCellEditable(int row, int col) { return col == 12; }
        };

        // 2. Reagents Table Model
        String[] reagCols = {"Item ID", "Item Name", "Category", "Manufacturer", "Batch No", "Qty", "Price (₹)", "Expiry Date", "Supplier", "Storage Location", "Min Stock", "Actions"};
        reagentsTableModel = new DefaultTableModel(null, reagCols) {
            @Override public boolean isCellEditable(int row, int col) { return col == 11; }
        };

        // 3. Consumables Table Model
        String[] consCols = {"Item ID", "Item Name", "Category", "Qty", "Unit", "Batch No", "Expiry Date", "Supplier", "Min Stock", "Actions"};
        consumablesTableModel = new DefaultTableModel(null, consCols) {
            @Override public boolean isCellEditable(int row, int col) { return col == 9; }
        };

        // 4. Home Overview Table Model
        String[] overCols = {"Item ID", "Resource / Item Name", "Category", "Stock / Qty", "Status / Expiry", "Storage / Location", "Stock Status", "Actions"};
        overviewTableModel = new DefaultTableModel(null, overCols) {
            @Override public boolean isCellEditable(int row, int col) { return col == 7; }
        };

        // Initialize MySQL database and populate tables (Requirement 7: JDBC CRUD)
        refreshAllDataFromDB();
    }

    // =========================================================================
    // 1. HOME VIEW
    // =========================================================================
    private JPanel createHomeView() {
        JPanel homeView = new JPanel();
        homeView.setLayout(new BoxLayout(homeView, BoxLayout.Y_AXIS));
        homeView.setBackground(new Color(245, 246, 250));
        homeView.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // 4 Stat Cards in a row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(2000, 110));

        JPanel card1 = createStatCard("TOTAL INVENTORY ITEMS", "0", 1, new Color(41, 128, 185)); // Ocean Blue (Package/Box)
        lblTotalInventory = (JLabel)((JPanel)card1.getComponent(0)).getComponent(1);

        JPanel card2 = createStatCard("LAB INSTRUMENTS", "0", 2, new Color(16, 185, 129)); // Emerald Mint (Microscope)
        lblTotalInstruments = (JLabel)((JPanel)card2.getComponent(0)).getComponent(1);

        JPanel card3 = createStatCard("REAGENTS & MEDICINES", "0", 3, new Color(142, 68, 173)); // Amethyst Purple (Flask)
        lblTotalReagents = (JLabel)((JPanel)card3.getComponent(0)).getComponent(1);

        JPanel card4 = createStatCard("LOW STOCK ALERTS", "0", 4, new Color(230, 126, 34)); // Coral Amber (Alert)
        lblLowStockAlerts = (JLabel)((JPanel)card4.getComponent(0)).getComponent(1);

        statsRow.add(card1);
        statsRow.add(card2);
        statsRow.add(card3);
        statsRow.add(card4);
        homeView.add(statsRow);
        homeView.add(Box.createVerticalStrut(20));

        // Overview Table Panel
        JPanel overCard = new JPanel(new BorderLayout());
        overCard.setBackground(Color.WHITE);
        overCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));

        JPanel overHeader = new JPanel(new BorderLayout());
        overHeader.setOpaque(false);
        overHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));

        JLabel lblOverTitle = new JLabel("Laboratory Resources & Critical Stock Overview");
        lblOverTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblOverTitle.setForeground(new Color(44, 62, 80));
        overHeader.add(lblOverTitle, BorderLayout.WEST);

        JPanel overHeaderRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        overHeaderRight.setOpaque(false);

        JButton btnGoInventory = new RoundedButton("Manage Full Inventory →", 16);
        btnGoInventory.setBackground(new Color(10, 61, 98));
        btnGoInventory.setForeground(Color.WHITE);
        btnGoInventory.setFont(new Font("Arial", Font.BOLD, 13));
        btnGoInventory.setPreferredSize(new Dimension(190, 34));
        btnGoInventory.addActionListener(e -> btnInventory.doClick());
        overHeaderRight.add(btnGoInventory);

        overHeader.add(overHeaderRight, BorderLayout.EAST);
        overCard.add(overHeader, BorderLayout.NORTH);

        overviewTable = new JTable(overviewTableModel);
        setupTableStyle(overviewTable, 6, new int[]{1}, new int[]{0, 2, 3, 4, 5, 6});
        overviewTable.getColumnModel().getColumn(6).setCellRenderer(new StatusPillRenderer());
        overviewTable.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
        overviewTable.getColumnModel().getColumn(7).setCellEditor(new ActionEditor(new JCheckBox(), 6, this));
        overviewTable.getColumnModel().getColumn(7).setPreferredWidth(160);

        JScrollPane overScroll = new JScrollPane(overviewTable);
        overScroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        overCard.add(overScroll, BorderLayout.CENTER);

        homeView.add(overCard);
        JScrollPane homeScroll = new JScrollPane(homeView);
        homeScroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(homeScroll, BorderLayout.CENTER);
        return wrapper;
    }

    // =========================================================================
    // 2. LAB INVENTORY VIEW (Category Switchers + 3 Dedicated Tables)
    // =========================================================================
    private JPanel createInventoryView() {
        JPanel inventoryView = new JPanel(new BorderLayout(0, 14));
        inventoryView.setBackground(new Color(245, 246, 250));
        inventoryView.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        // Top Category Bar
        JPanel topCategoryBar = new JPanel(new BorderLayout());
        topCategoryBar.setBackground(Color.WHITE);
        topCategoryBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JPanel catLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        catLeft.setOpaque(false);

        JLabel lblInvTitle = new JLabel("Category: ");
        lblInvTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblInvTitle.setForeground(new Color(44, 62, 80));
        catLeft.add(lblInvTitle);

        btnCatInstruments = createCategoryPillButton("🔬 Lab Instruments (" + instrumentsTableModel.getRowCount() + ")", true);
        btnCatReagents = createCategoryPillButton("🧪 Medicines / Reagents (" + reagentsTableModel.getRowCount() + ")", false);
        btnCatConsumables = createCategoryPillButton("📦 Consumables (" + consumablesTableModel.getRowCount() + ")", false);

        catLeft.add(btnCatInstruments);
        catLeft.add(btnCatReagents);
        catLeft.add(btnCatConsumables);
        topCategoryBar.add(catLeft, BorderLayout.WEST);

        inventoryView.add(topCategoryBar, BorderLayout.NORTH);

        // Card Panel for 3 Category Tables
        inventoryCatLayout = new CardLayout();
        inventoryCatCards = new JPanel(inventoryCatLayout);
        inventoryCatCards.setOpaque(false);

        // Category 1: Instruments Panel
        JPanel pnlInstruments = createInstrumentsPanel();
        // Category 2: Reagents Panel
        JPanel pnlReagents = createReagentsPanel();
        // Category 3: Consumables Panel
        JPanel pnlConsumables = createConsumablesPanel();

        inventoryCatCards.add(pnlInstruments, "Instruments");
        inventoryCatCards.add(pnlReagents, "Reagents");
        inventoryCatCards.add(pnlConsumables, "Consumables");

        inventoryView.add(inventoryCatCards, BorderLayout.CENTER);

        // Category Switcher Actions
        btnCatInstruments.addActionListener(e -> switchInventoryCategory(btnCatInstruments, "Instruments"));
        btnCatReagents.addActionListener(e -> switchInventoryCategory(btnCatReagents, "Reagents"));
        btnCatConsumables.addActionListener(e -> switchInventoryCategory(btnCatConsumables, "Consumables"));

        return inventoryView;
    }

    private void switchInventoryCategory(JButton activeBtn, String cardName) {
        currentInvCategory = cardName;
        JButton[] btns = {btnCatInstruments, btnCatReagents, btnCatConsumables};
        for (JButton b : btns) {
            if (b == activeBtn) {
                b.setBackground(new Color(10, 61, 98));
                b.setForeground(Color.WHITE);
            } else {
                b.setBackground(new Color(240, 243, 246));
                b.setForeground(new Color(52, 73, 94));
            }
        }
        inventoryCatLayout.show(inventoryCatCards, cardName);
    }

    private JButton createCategoryPillButton(String text, boolean active) {
        RoundedButton btn = new RoundedButton(text, 16);
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(215, 34));
        if (active) {
            btn.setBackground(new Color(10, 61, 98));
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(new Color(240, 243, 246));
            btn.setForeground(new Color(52, 73, 94));
        }
        return btn;
    }

    private void setupSearchFilter(JTextField txtSearch, TableRowSorter<DefaultTableModel> sorter) {
        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void filter() {
                String text = txtSearch.getText().trim();
                if (text.isEmpty()) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
    }

    // -------------------------------------------------------------
    // Category 1: Instruments Panel
    // -------------------------------------------------------------
    private JPanel createInstrumentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Laboratory Instruments & Diagnostic Equipment");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        JTextField txtSearch = new RoundedTextField(12, 14);
        actions.add(lblSearch);
        actions.add(txtSearch);

        JButton btnAdd = new RoundedButton("+ Add Instrument", 16);
        btnAdd.setBackground(new Color(10, 61, 98));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(160, 32));
        btnAdd.addActionListener(e -> showInstrumentForm(-1));
        btnAdd.setVisible(isAdmin);
        actions.add(btnAdd);

        header.add(actions, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        instrumentsTable = new JTable(instrumentsTableModel);
        setupTableStyle(instrumentsTable, 12, new int[]{1}, new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11});
        instrumentsTable.getColumnModel().getColumn(9).setCellRenderer(new StatusPillRenderer());
        instrumentsTable.getColumnModel().getColumn(12).setCellRenderer(new ActionRenderer());
        instrumentsTable.getColumnModel().getColumn(12).setCellEditor(new ActionEditor(new JCheckBox(), 1, this));
        instrumentsTable.getColumnModel().getColumn(12).setPreferredWidth(160);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(instrumentsTableModel);
        instrumentsTable.setRowSorter(sorter);
        setupSearchFilter(txtSearch, sorter);

        JScrollPane scroll = new JScrollPane(instrumentsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------
    // Category 2: Reagents Panel
    // -------------------------------------------------------------
    private JPanel createReagentsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Medicines, Diagnostic Reagents & Test Kits");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        JTextField txtSearch = new RoundedTextField(12, 14);
        actions.add(lblSearch);
        actions.add(txtSearch);

        JButton btnAdd = new RoundedButton("+ Add Reagent / Kit", 16);
        btnAdd.setBackground(new Color(142, 68, 173)); // Purple
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(170, 32));
        btnAdd.addActionListener(e -> showReagentForm(-1));
        btnAdd.setVisible(isAdmin);
        actions.add(btnAdd);

        header.add(actions, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        reagentsTable = new JTable(reagentsTableModel);
        setupTableStyle(reagentsTable, 11, new int[]{1}, new int[]{0, 2, 3, 4, 5, 6, 7, 8, 9, 10});
        reagentsTable.getColumnModel().getColumn(5).setCellRenderer(new StockQtyRenderer(5, 10));
        reagentsTable.getColumnModel().getColumn(11).setCellRenderer(new ActionRenderer());
        reagentsTable.getColumnModel().getColumn(11).setCellEditor(new ActionEditor(new JCheckBox(), 2, this));
        reagentsTable.getColumnModel().getColumn(11).setPreferredWidth(160);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(reagentsTableModel);
        reagentsTable.setRowSorter(sorter);
        setupSearchFilter(txtSearch, sorter);

        JScrollPane scroll = new JScrollPane(reagentsTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------
    // Category 3: Consumables Panel
    // -------------------------------------------------------------
    private JPanel createConsumablesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        JLabel title = new JLabel("Laboratory Disposables & Consumables");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(new Color(44, 62, 80));
        header.add(title, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        JLabel lblSearch = new JLabel("Search:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        JTextField txtSearch = new RoundedTextField(12, 14);
        actions.add(lblSearch);
        actions.add(txtSearch);

        JButton btnAdd = new RoundedButton("+ Add Consumable", 16);
        btnAdd.setBackground(new Color(39, 174, 96)); // Green
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Arial", Font.BOLD, 13));
        btnAdd.setPreferredSize(new Dimension(160, 32));
        btnAdd.addActionListener(e -> showConsumableForm(-1));
        btnAdd.setVisible(isAdmin);
        actions.add(btnAdd);

        header.add(actions, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        consumablesTable = new JTable(consumablesTableModel);
        setupTableStyle(consumablesTable, 9, new int[]{1}, new int[]{0, 2, 3, 4, 5, 6, 7, 8});
        consumablesTable.getColumnModel().getColumn(3).setCellRenderer(new StockQtyRenderer(3, 8));
        consumablesTable.getColumnModel().getColumn(9).setCellRenderer(new ActionRenderer());
        consumablesTable.getColumnModel().getColumn(9).setCellEditor(new ActionEditor(new JCheckBox(), 3, this));
        consumablesTable.getColumnModel().getColumn(9).setPreferredWidth(160);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(consumablesTableModel);
        consumablesTable.setRowSorter(sorter);
        setupSearchFilter(txtSearch, sorter);

        JScrollPane scroll = new JScrollPane(consumablesTable);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    // -------------------------------------------------------------
    // Helper: Setup Table Styling
    // -------------------------------------------------------------
    private void setupTableStyle(JTable table, int actionColIndex, int[] textAreaCols, int[] centerCols) {
        table.setRowHeight(44);
        table.setShowGrid(true);
        table.setGridColor(new Color(235, 238, 242));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);

        JTableHeader th = table.getTableHeader();
        th.setFont(new Font("Arial", Font.BOLD, 13));
        th.setBackground(new Color(236, 240, 241));
        th.setForeground(new Color(44, 62, 80));
        th.setPreferredSize(new Dimension(100, 36));

        DefaultTableCellRenderer centerCell = new DefaultTableCellRenderer();
        centerCell.setHorizontalAlignment(JLabel.CENTER);

        if (centerCols != null) {
            for (int col : centerCols) {
                if (col < table.getColumnCount()) {
                    table.getColumnModel().getColumn(col).setCellRenderer(centerCell);
                }
            }
        }

        if (textAreaCols != null) {
            for (int col : textAreaCols) {
                if (col < table.getColumnCount()) {
                    table.getColumnModel().getColumn(col).setCellRenderer(new TextAreaRenderer());
                }
            }
        }
    }

    // =========================================================================
    // 3. ABOUT VIEW
    // =========================================================================
    private JPanel createAboutView() {
        JPanel aboutView = new JPanel(new GridBagLayout());
        aboutView.setBackground(new Color(245, 246, 250));

        RoundedPanel aboutCard = new RoundedPanel(20, Color.WHITE);
        aboutCard.setLayout(new BoxLayout(aboutCard, BoxLayout.Y_AXIS));
        aboutCard.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        aboutCard.setPreferredSize(new Dimension(640, 460));

        JLabel aLogo = new JLabel("🔬", JLabel.CENTER);
        aLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 42));
        aLogo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aTitle = new JLabel("Medical Laboratory Management System", JLabel.CENTER);
        aTitle.setFont(new Font("Arial", Font.BOLD, 22));
        aTitle.setForeground(new Color(10, 61, 98));
        aTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aSub = new JLabel("Clinical Pathology & Diagnostics Portal", JLabel.CENTER);
        aSub.setFont(new Font("Arial", Font.PLAIN, 14));
        aSub.setForeground(new Color(127, 140, 141));
        aSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aDesc = new JLabel("<html><div style='text-align: center; width: 500px; color: #555; font-size: 13px;'>"
                + "A robust and comprehensive desktop solution designed for pathology laboratories. "
                + "Features complete Laboratory Inventory management across Instruments, Medicines & Reagents, "
                + "and Consumables, with full stock tracking, equipment maintenance schedules, and laboratory supplies control."
                + "</div></html>", JLabel.CENTER);
        aDesc.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aDev = new JLabel("<html><center><b>Developed by:</b> <font color='#0a3d62'>Mohammad Rehan</font></center></html>");
        aDev.setFont(new Font("Arial", Font.PLAIN, 15));
        aDev.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aGuide = new JLabel("<html><center><b>Guided by:</b> <font color='#27ae60'>Faculty Mentor</font></center></html>");
        aGuide.setFont(new Font("Arial", Font.PLAIN, 15));
        aGuide.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel aCopy = new JLabel("© 2026 Medical Laboratory Management System. All rights reserved.");
        aCopy.setFont(new Font("Arial", Font.PLAIN, 12));
        aCopy.setForeground(Color.GRAY);
        aCopy.setAlignmentX(Component.CENTER_ALIGNMENT);

        aboutCard.add(aLogo);
        aboutCard.add(Box.createVerticalStrut(10));
        aboutCard.add(aTitle);
        aboutCard.add(Box.createVerticalStrut(4));
        aboutCard.add(aSub);
        aboutCard.add(Box.createVerticalStrut(18));
        aboutCard.add(aDesc);
        aboutCard.add(Box.createVerticalStrut(22));
        aboutCard.add(aDev);
        aboutCard.add(Box.createVerticalStrut(8));
        aboutCard.add(aGuide);
        aboutCard.add(Box.createVerticalStrut(20));
        aboutCard.add(aCopy);

        aboutView.add(aboutCard);
        return aboutView;
    }

    // =========================================================================
    // 4. PROFILE VIEW
    // =========================================================================
    private JPanel createProfileView() {
        JPanel profileView = new JPanel(new GridBagLayout());
        profileView.setBackground(new Color(245, 246, 250));

        RoundedPanel pCard = new RoundedPanel(20, Color.WHITE);
        pCard.setLayout(new BoxLayout(pCard, BoxLayout.Y_AXIS));
        pCard.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        pCard.setPreferredSize(new Dimension(620, 460));

        JLabel pHeaderTitle = new JLabel(isAdmin ? "Chief Pathologist / Administrator Profile" : "Laboratory Staff Profile");
        pHeaderTitle.setFont(new Font("Arial", Font.BOLD, 22));
        pHeaderTitle.setForeground(new Color(10, 61, 98));
        pHeaderTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel pInfoBox = new JPanel(new GridLayout(3, 1, 6, 10));
        pInfoBox.setBackground(new Color(245, 246, 250));
        pInfoBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)
        ));
        pInfoBox.setMaximumSize(new Dimension(540, 110));

        pInfoBox.add(createProfileDataRow("Workstation Email:", isAdmin ? "admin@poornima.org" : "staff@poornima.org"));
        pInfoBox.add(createProfileDataRow("Assigned Role:", isAdmin ? "Chief Administrator / Pathologist" : "Laboratory Technician / Staff"));
        pInfoBox.add(createProfileDataRow("Security Status:", "Active Workstation Session"));

        JPanel pStatsBox = new JPanel(new GridLayout(1, 3, 12, 0));
        pStatsBox.setOpaque(false);
        pStatsBox.setMaximumSize(new Dimension(540, 85));

        lblProfileInstruments = new JLabel(String.valueOf(instrumentsTableModel.getRowCount()), JLabel.CENTER);
        lblProfileReagents = new JLabel(String.valueOf(reagentsTableModel.getRowCount()), JLabel.CENTER);
        lblProfileConsumables = new JLabel(String.valueOf(consumablesTableModel.getRowCount()), JLabel.CENTER);

        pStatsBox.add(createMiniStatWidget("Lab Instruments", lblProfileInstruments, new Color(16, 185, 129)));
        pStatsBox.add(createMiniStatWidget("Reagents & Kits", lblProfileReagents, new Color(142, 68, 173)));
        pStatsBox.add(createMiniStatWidget("Consumables", lblProfileConsumables, new Color(41, 128, 185)));

        JButton btnChangePwd = new RoundedButton("Change Password", 16);
        btnChangePwd.setBackground(new Color(10, 61, 98));
        btnChangePwd.setForeground(Color.WHITE);
        btnChangePwd.setFont(new Font("Arial", Font.BOLD, 14));
        btnChangePwd.setMaximumSize(new Dimension(190, 40));
        btnChangePwd.setPreferredSize(new Dimension(190, 40));
        btnChangePwd.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChangePwd.addActionListener(e -> showChangePasswordDialog());

        pCard.add(pHeaderTitle);
        pCard.add(Box.createVerticalStrut(16));
        pCard.add(pInfoBox);
        pCard.add(Box.createVerticalStrut(16));
        pCard.add(pStatsBox);
        pCard.add(Box.createVerticalStrut(22));
        pCard.add(btnChangePwd);

        profileView.add(pCard);
        return profileView;
    }

    // =========================================================================
    // OVERVIEW TABLE POPULATION & STATS CALCULATION
    // =========================================================================
    public void refreshOverviewTable() {
        if (overviewTableModel == null) return;
        overviewTableModel.setRowCount(0);

        // Add Instruments
        for (int i = 0; i < instrumentsTableModel.getRowCount(); i++) {
            String id = instrumentsTableModel.getValueAt(i, 0).toString();
            String name = instrumentsTableModel.getValueAt(i, 1).toString();
            String cat = instrumentsTableModel.getValueAt(i, 2).toString();
            String qty = instrumentsTableModel.getValueAt(i, 6).toString() + " Units";
            String status = instrumentsTableModel.getValueAt(i, 9).toString();
            String loc = instrumentsTableModel.getValueAt(i, 7).toString();
            String stockStat = status.equalsIgnoreCase("Operational") ? "✓ Operational" : "⚠️ " + status;
            overviewTableModel.addRow(new Object[]{id, name, cat, qty, status, loc, stockStat, ""});
        }

        // Add Reagents
        for (int i = 0; i < reagentsTableModel.getRowCount(); i++) {
            String id = reagentsTableModel.getValueAt(i, 0).toString();
            String name = reagentsTableModel.getValueAt(i, 1).toString();
            String cat = reagentsTableModel.getValueAt(i, 2).toString();
            String qtyStr = reagentsTableModel.getValueAt(i, 5).toString();
            String minStr = reagentsTableModel.getValueAt(i, 10).toString();
            String exp = "Exp: " + reagentsTableModel.getValueAt(i, 7).toString();
            String loc = reagentsTableModel.getValueAt(i, 9).toString();

            int q = parseIntSafe(qtyStr, 0);
            int m = parseIntSafe(minStr, 0);
            String stockStat = (q <= m) ? "⚠️ LOW STOCK (" + q + "/" + m + ")" : "✓ In Stock (" + q + ")";

            overviewTableModel.addRow(new Object[]{id, name, cat, qtyStr + " Vials/Kits", exp, loc, stockStat, ""});
        }

        // Add Consumables
        for (int i = 0; i < consumablesTableModel.getRowCount(); i++) {
            String id = consumablesTableModel.getValueAt(i, 0).toString();
            String name = consumablesTableModel.getValueAt(i, 1).toString();
            String cat = consumablesTableModel.getValueAt(i, 2).toString();
            String qtyStr = consumablesTableModel.getValueAt(i, 3).toString();
            String unit = consumablesTableModel.getValueAt(i, 4).toString();
            String minStr = consumablesTableModel.getValueAt(i, 8).toString();
            String exp = "Exp: " + consumablesTableModel.getValueAt(i, 6).toString();

            int q = parseIntSafe(qtyStr, 0);
            int m = parseIntSafe(minStr, 0);
            String stockStat = (q <= m) ? "⚠️ LOW STOCK (" + q + "/" + m + ")" : "✓ In Stock";

            overviewTableModel.addRow(new Object[]{id, name, cat, qtyStr + " " + unit, exp, "Supply Bay", stockStat, ""});
        }

        // Update Category Button Counts
        if (btnCatInstruments != null) btnCatInstruments.setText("🔬 Lab Instruments (" + instrumentsTableModel.getRowCount() + ")");
        if (btnCatReagents != null) btnCatReagents.setText("🧪 Medicines / Reagents (" + reagentsTableModel.getRowCount() + ")");
        if (btnCatConsumables != null) btnCatConsumables.setText("📦 Consumables (" + consumablesTableModel.getRowCount() + ")");
    }

    private void recalculateStats() {
        int totalInst = instrumentsTableModel != null ? instrumentsTableModel.getRowCount() : 0;
        int totalReag = reagentsTableModel != null ? reagentsTableModel.getRowCount() : 0;
        int totalCons = consumablesTableModel != null ? consumablesTableModel.getRowCount() : 0;
        int totalInventory = totalInst + totalReag + totalCons;

        int lowStockCount = 0;

        // Check Instruments with issues
        if (instrumentsTableModel != null) {
            for (int i = 0; i < instrumentsTableModel.getRowCount(); i++) {
                String st = instrumentsTableModel.getValueAt(i, 9).toString();
                if (!"Operational".equalsIgnoreCase(st)) {
                    lowStockCount++;
                }
            }
        }

        // Check Reagents low stock
        if (reagentsTableModel != null) {
            for (int i = 0; i < reagentsTableModel.getRowCount(); i++) {
                int q = parseIntSafe(reagentsTableModel.getValueAt(i, 5).toString(), 0);
                int m = parseIntSafe(reagentsTableModel.getValueAt(i, 10).toString(), 0);
                if (q <= m) lowStockCount++;
            }
        }

        // Check Consumables low stock
        if (consumablesTableModel != null) {
            for (int i = 0; i < consumablesTableModel.getRowCount(); i++) {
                int q = parseIntSafe(consumablesTableModel.getValueAt(i, 3).toString(), 0);
                int m = parseIntSafe(consumablesTableModel.getValueAt(i, 8).toString(), 0);
                if (q <= m) lowStockCount++;
            }
        }

        if (lblTotalInventory != null) lblTotalInventory.setText(String.valueOf(totalInventory));
        if (lblTotalInstruments != null) lblTotalInstruments.setText(String.valueOf(totalInst));
        if (lblTotalReagents != null) lblTotalReagents.setText(String.valueOf(totalReag));
        if (lblLowStockAlerts != null) lblLowStockAlerts.setText(String.valueOf(lowStockCount));
    }

    private int parseIntSafe(String val, int defaultVal) {
        try {
            return Integer.parseInt(val.replaceAll("[^0-9]", ""));
        } catch (Exception e) {
            return defaultVal;
        }
    }

    // =========================================================================
    // MODAL FORMS: ADD & EDIT
    // =========================================================================

    // 1. LAB INSTRUMENTS FORM
    void showInstrumentForm(int editRow) {
        String dialogTitle = (editRow == -1) ? "Add New Laboratory Instrument" : "Edit Instrument Details";
        JDialog dialog = new JDialog(dashFrame, dialogTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(560, 640);
        dialog.setLocationRelativeTo(dashFrame);

        JPanel contentPanel = new JPanel(new GridLayout(12, 2, 10, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(new JLabel("Instrument ID:"));
        JTextField txtId = new RoundedTextField(15);
        String defaultId;
        if (editRow == -1) {
            int nextNum = 101 + instrumentsTableModel.getRowCount();
            while (LabDAO.instrumentExists("INS-" + nextNum)) {
                nextNum++;
            }
            defaultId = "INS-" + nextNum;
            txtId.setEditable(true);
            txtId.setBackground(Color.WHITE);
        } else {
            defaultId = instrumentsTableModel.getValueAt(editRow, 0).toString();
            txtId.setEditable(false);
            txtId.setBackground(new Color(245, 245, 245));
        }
        txtId.setText(defaultId);
        contentPanel.add(txtId);

        contentPanel.add(new JLabel("Instrument Name:"));
        String[] sampleNames = {
                "Microscope", "Spectrophotometer", "Centrifuge",
                "Hematology Analyzer", "Biochemistry Analyzer",
                "Incubator", "Autoclave",
                "pH Meter", "Micropipette Set"
        };
        JComboBox<String> cbName = new JComboBox<>(sampleNames);
        cbName.setEditable(true);
        contentPanel.add(cbName);

        contentPanel.add(new JLabel("Category:"));
        JTextField txtCat = new RoundedTextField(15);
        txtCat.setText("Lab Instruments");
        txtCat.setEditable(false);
        txtCat.setBackground(new Color(245, 245, 245));
        contentPanel.add(txtCat);

        contentPanel.add(new JLabel("Manufacturer:"));
        JTextField txtMfr = new RoundedTextField(15);
        txtMfr.setText("Olympus");
        contentPanel.add(txtMfr);

        contentPanel.add(new JLabel("Model:"));
        JTextField txtModel = new RoundedTextField(15);
        txtModel.setText("CX23-LED");
        contentPanel.add(txtModel);

        contentPanel.add(new JLabel("Serial Number:"));
        JTextField txtSerial = new RoundedTextField(15);
        txtSerial.setText("SN-" + (int)(Math.random() * 90000 + 10000));
        contentPanel.add(txtSerial);

        contentPanel.add(new JLabel("Quantity:"));
        JTextField txtQty = new RoundedTextField(15);
        txtQty.setText("1");
        contentPanel.add(txtQty);

        contentPanel.add(new JLabel("Location:"));
        String[] locations = {
                "Pathology Lab Room 102", "Biochemistry Station", "Sample Prep Area",
                "Hematology Main Bay", "Microbiology Room", "Sterilization & Wash Bay", "Reagent Prep Bench"
        };
        JComboBox<String> cbLoc = new JComboBox<>(locations);
        cbLoc.setEditable(true);
        contentPanel.add(cbLoc);

        contentPanel.add(new JLabel("Purchase Date:"));
        SpinnerDateModel pDateModel = new SpinnerDateModel();
        JSpinner pDateSpinner = new JSpinner(pDateModel);
        pDateSpinner.setEditor(new JSpinner.DateEditor(pDateSpinner, "dd-MMM-yyyy"));
        contentPanel.add(pDateSpinner);

        contentPanel.add(new JLabel("Status:"));
        String[] statuses = {"Operational", "Under Maintenance", "Calibration Required", "Out of Service"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        contentPanel.add(cbStatus);

        contentPanel.add(new JLabel("Last Maintenance:"));
        SpinnerDateModel lDateModel = new SpinnerDateModel();
        JSpinner lDateSpinner = new JSpinner(lDateModel);
        lDateSpinner.setEditor(new JSpinner.DateEditor(lDateSpinner, "dd-MMM-yyyy"));
        contentPanel.add(lDateSpinner);

        contentPanel.add(new JLabel("Next Maintenance:"));
        SpinnerDateModel nDateModel = new SpinnerDateModel();
        JSpinner nDateSpinner = new JSpinner(nDateModel);
        nDateSpinner.setEditor(new JSpinner.DateEditor(nDateSpinner, "dd-MMM-yyyy"));
        contentPanel.add(nDateSpinner);

        if (editRow != -1) {
            cbName.setSelectedItem(instrumentsTableModel.getValueAt(editRow, 1).toString());
            txtCat.setText(instrumentsTableModel.getValueAt(editRow, 2).toString());
            txtMfr.setText(instrumentsTableModel.getValueAt(editRow, 3).toString());
            txtModel.setText(instrumentsTableModel.getValueAt(editRow, 4).toString());
            txtSerial.setText(instrumentsTableModel.getValueAt(editRow, 5).toString());
            txtQty.setText(instrumentsTableModel.getValueAt(editRow, 6).toString());
            cbLoc.setSelectedItem(instrumentsTableModel.getValueAt(editRow, 7).toString());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                pDateSpinner.setValue(sdf.parse(instrumentsTableModel.getValueAt(editRow, 8).toString()));
                cbStatus.setSelectedItem(instrumentsTableModel.getValueAt(editRow, 9).toString());
                lDateSpinner.setValue(sdf.parse(instrumentsTableModel.getValueAt(editRow, 10).toString()));
                nDateSpinner.setValue(sdf.parse(instrumentsTableModel.getValueAt(editRow, 11).toString()));
            } catch (Exception ignored) {}
        }

        JButton btnSave = new RoundedButton((editRow == -1) ? "💾 Save to Database" : "💾 Update Record", 16);
        btnSave.setBackground(new Color(10, 61, 98));
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = cbName.getSelectedItem() != null ? cbName.getSelectedItem().toString().trim() : "";
            String cat = txtCat.getText().trim();
            String mfr = txtMfr.getText().trim();
            String model = txtModel.getText().trim();
            String serial = txtSerial.getText().trim();
            String qty = txtQty.getText().trim();
            String loc = cbLoc.getSelectedItem() != null ? cbLoc.getSelectedItem().toString().trim() : "";
            String status = cbStatus.getSelectedItem() != null ? cbStatus.getSelectedItem().toString() : "Operational";

            // Form Validation (Requirement 8)
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Instrument ID is mandatory and cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.isEmpty() || mfr.isEmpty() || model.isEmpty() || serial.isEmpty() || qty.isEmpty() || loc.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory fields (Name, Manufacturer, Model, Serial, Quantity, Location)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int q;
            try {
                q = Integer.parseInt(qty);
                if (q < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be a valid positive integer!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            String pDateStr = sdf.format(pDateSpinner.getValue());
            String lDateStr = sdf.format(lDateSpinner.getValue());
            String nDateStr = sdf.format(nDateSpinner.getValue());

            String[] rowData = new String[]{id, name, cat, mfr, model, serial, String.valueOf(q), loc, pDateStr, status, lDateStr, nDateStr};

            if (editRow == -1) {
                // Check duplicate ID
                if (LabDAO.instrumentExists(id)) {
                    JOptionPane.showMessageDialog(dialog, "Instrument ID '" + id + "' already exists in MySQL database! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (int i = 0; i < instrumentsTableModel.getRowCount(); i++) {
                    if (instrumentsTableModel.getValueAt(i, 0).toString().equalsIgnoreCase(id)) {
                        JOptionPane.showMessageDialog(dialog, "Instrument ID '" + id + "' already exists in table! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                // Save to MySQL via JDBC
                LabDAO.insertInstrument(rowData);
                instrumentsTableModel.addRow(new Object[]{
                        id, name, cat, mfr, model, serial, String.valueOf(q), loc, pDateStr, status, lDateStr, nDateStr, ""
                });
                saveTableData(instrumentsTableModel, "inventory_instruments.txt");
                JOptionPane.showMessageDialog(dialog, "Laboratory Instrument '" + name + "' [ID: " + id + "] saved to MySQL database successfully!", "Record Added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update in MySQL via JDBC
                LabDAO.updateInstrument(rowData);
                instrumentsTableModel.setValueAt(name, editRow, 1);
                instrumentsTableModel.setValueAt(cat, editRow, 2);
                instrumentsTableModel.setValueAt(mfr, editRow, 3);
                instrumentsTableModel.setValueAt(model, editRow, 4);
                instrumentsTableModel.setValueAt(serial, editRow, 5);
                instrumentsTableModel.setValueAt(String.valueOf(q), editRow, 6);
                instrumentsTableModel.setValueAt(loc, editRow, 7);
                instrumentsTableModel.setValueAt(pDateStr, editRow, 8);
                instrumentsTableModel.setValueAt(status, editRow, 9);
                instrumentsTableModel.setValueAt(lDateStr, editRow, 10);
                instrumentsTableModel.setValueAt(nDateStr, editRow, 11);
                saveTableData(instrumentsTableModel, "inventory_instruments.txt");
                JOptionPane.showMessageDialog(dialog, "Instrument details [ID: " + id + "] updated in MySQL database successfully!", "Record Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshOverviewTable();
            recalculateStats();
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnSave);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.setVisible(true);
    }

    // 2. MEDICINES / REAGENTS FORM
    void showReagentForm(int editRow) {
        String dialogTitle = (editRow == -1) ? "Add Medicine / Reagent Item" : "Edit Reagent Details";
        JDialog dialog = new JDialog(dashFrame, dialogTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(560, 640);
        dialog.setLocationRelativeTo(dashFrame);

        JPanel contentPanel = new JPanel(new GridLayout(11, 2, 10, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(new JLabel("Item ID:"));
        JTextField txtId = new RoundedTextField(15);
        String defaultId;
        if (editRow == -1) {
            int nextNum = 101 + reagentsTableModel.getRowCount();
            while (LabDAO.reagentExists("REA-" + nextNum)) {
                nextNum++;
            }
            defaultId = "REA-" + nextNum;
            txtId.setEditable(true);
            txtId.setBackground(Color.WHITE);
        } else {
            defaultId = reagentsTableModel.getValueAt(editRow, 0).toString();
            txtId.setEditable(false);
            txtId.setBackground(new Color(245, 245, 245));
        }
        txtId.setText(defaultId);
        contentPanel.add(txtId);

        contentPanel.add(new JLabel("Item Name:"));
        String[] sampleReagents = {
                "Glucose Reagent", "Hemoglobin Reagent",
                "Cholesterol Reagent", "Urea Reagent",
                "Creatinine Reagent", "Dengue Test Kits",
                "Troponin Test Kits", "COVID-19 Test Kits"
        };
        JComboBox<String> cbName = new JComboBox<>(sampleReagents);
        cbName.setEditable(true);
        contentPanel.add(cbName);

        contentPanel.add(new JLabel("Category:"));
        JTextField txtCat = new RoundedTextField(15);
        txtCat.setText("Medicines / Reagents");
        txtCat.setEditable(false);
        txtCat.setBackground(new Color(245, 245, 245));
        contentPanel.add(txtCat);

        contentPanel.add(new JLabel("Manufacturer:"));
        JTextField txtMfr = new RoundedTextField(15);
        txtMfr.setText("Roche Diagnostics");
        contentPanel.add(txtMfr);

        contentPanel.add(new JLabel("Batch Number:"));
        JTextField txtBatch = new RoundedTextField(15);
        txtBatch.setText("BTH-" + (int)(Math.random() * 9000 + 1000));
        contentPanel.add(txtBatch);

        contentPanel.add(new JLabel("Quantity (Units):"));
        JTextField txtQty = new RoundedTextField(15);
        txtQty.setText("10");
        contentPanel.add(txtQty);

        contentPanel.add(new JLabel("Price per Unit (₹):"));
        JTextField txtPrice = new RoundedTextField(15);
        txtPrice.setText("1200");
        contentPanel.add(txtPrice);

        contentPanel.add(new JLabel("Expiry Date:"));
        SpinnerDateModel expDateModel = new SpinnerDateModel();
        JSpinner expDateSpinner = new JSpinner(expDateModel);
        expDateSpinner.setEditor(new JSpinner.DateEditor(expDateSpinner, "dd-MMM-yyyy"));
        contentPanel.add(expDateSpinner);

        contentPanel.add(new JLabel("Supplier:"));
        JTextField txtSupplier = new RoundedTextField(15);
        txtSupplier.setText("Apex Bio Supplies");
        contentPanel.add(txtSupplier);

        contentPanel.add(new JLabel("Storage Location:"));
        String[] storageLocations = {
                "Cold Storage (2-8°C)", "Deep Freezer (-20°C)", "Room Temp Cabinet", "Flammables Safety Cabinet"
        };
        JComboBox<String> cbStorage = new JComboBox<>(storageLocations);
        cbStorage.setEditable(true);
        contentPanel.add(cbStorage);

        contentPanel.add(new JLabel("Minimum Stock:"));
        JTextField txtMinStock = new RoundedTextField(15);
        txtMinStock.setText("5");
        contentPanel.add(txtMinStock);

        if (editRow != -1) {
            cbName.setSelectedItem(reagentsTableModel.getValueAt(editRow, 1).toString());
            txtCat.setText(reagentsTableModel.getValueAt(editRow, 2).toString());
            txtMfr.setText(reagentsTableModel.getValueAt(editRow, 3).toString());
            txtBatch.setText(reagentsTableModel.getValueAt(editRow, 4).toString());
            txtQty.setText(reagentsTableModel.getValueAt(editRow, 5).toString());
            txtPrice.setText(reagentsTableModel.getValueAt(editRow, 6).toString().replaceAll("[^0-9.]", ""));
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                expDateSpinner.setValue(sdf.parse(reagentsTableModel.getValueAt(editRow, 7).toString()));
            } catch (Exception ignored) {}
            txtSupplier.setText(reagentsTableModel.getValueAt(editRow, 8).toString());
            cbStorage.setSelectedItem(reagentsTableModel.getValueAt(editRow, 9).toString());
            txtMinStock.setText(reagentsTableModel.getValueAt(editRow, 10).toString());
        }

        JButton btnSave = new RoundedButton((editRow == -1) ? "💾 Save to Database" : "💾 Update Record", 16);
        btnSave.setBackground(new Color(142, 68, 173)); // Purple
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = cbName.getSelectedItem() != null ? cbName.getSelectedItem().toString().trim() : "";
            String cat = txtCat.getText().trim();
            String mfr = txtMfr.getText().trim();
            String batch = txtBatch.getText().trim();
            String qty = txtQty.getText().trim();
            String price = txtPrice.getText().trim();
            String supplier = txtSupplier.getText().trim();
            String storage = cbStorage.getSelectedItem() != null ? cbStorage.getSelectedItem().toString().trim() : "";
            String minStock = txtMinStock.getText().trim();

            // Form Validation (Requirement 8)
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Item ID is mandatory and cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.isEmpty() || mfr.isEmpty() || batch.isEmpty() || qty.isEmpty() || price.isEmpty() || supplier.isEmpty() || minStock.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all mandatory fields (Name, Manufacturer, Batch, Quantity, Price, Supplier, Min Stock)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int q, m;
            double p;
            try {
                q = Integer.parseInt(qty);
                m = Integer.parseInt(minStock);
                p = Double.parseDouble(price);
                if (q < 0 || m < 0 || p < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity and Min Stock must be positive integers, and Price must be a valid positive number!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            String expDateStr = sdf.format(expDateSpinner.getValue());

            String[] rowData = new String[]{id, name, cat, mfr, batch, String.valueOf(q), String.format("%.2f", p), expDateStr, supplier, storage, String.valueOf(m)};

            if (editRow == -1) {
                // Check duplicate ID
                if (LabDAO.reagentExists(id)) {
                    JOptionPane.showMessageDialog(dialog, "Item ID '" + id + "' already exists in MySQL database! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (int i = 0; i < reagentsTableModel.getRowCount(); i++) {
                    if (reagentsTableModel.getValueAt(i, 0).toString().equalsIgnoreCase(id)) {
                        JOptionPane.showMessageDialog(dialog, "Item ID '" + id + "' already exists in table! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                // Save to MySQL via JDBC
                LabDAO.insertReagent(rowData);
                reagentsTableModel.addRow(new Object[]{
                        id, name, cat, mfr, batch, String.valueOf(q), String.format("%.2f", p), expDateStr, supplier, storage, String.valueOf(m), ""
                });
                saveTableData(reagentsTableModel, "inventory_reagents.txt");
                JOptionPane.showMessageDialog(dialog, "Medicine / Reagent '" + name + "' [ID: " + id + "] saved to MySQL database successfully!", "Record Added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update in MySQL via JDBC
                LabDAO.updateReagent(rowData);
                reagentsTableModel.setValueAt(name, editRow, 1);
                reagentsTableModel.setValueAt(cat, editRow, 2);
                reagentsTableModel.setValueAt(mfr, editRow, 3);
                reagentsTableModel.setValueAt(batch, editRow, 4);
                reagentsTableModel.setValueAt(String.valueOf(q), editRow, 5);
                reagentsTableModel.setValueAt(String.format("%.2f", p), editRow, 6);
                reagentsTableModel.setValueAt(expDateStr, editRow, 7);
                reagentsTableModel.setValueAt(supplier, editRow, 8);
                reagentsTableModel.setValueAt(storage, editRow, 9);
                reagentsTableModel.setValueAt(String.valueOf(m), editRow, 10);
                saveTableData(reagentsTableModel, "inventory_reagents.txt");
                JOptionPane.showMessageDialog(dialog, "Reagent details [ID: " + id + "] updated in MySQL database successfully!", "Record Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshOverviewTable();
            recalculateStats();
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnSave);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.setVisible(true);
    }

    // 3. CONSUMABLES FORM
    void showConsumableForm(int editRow) {
        String dialogTitle = (editRow == -1) ? "Add Consumable Item" : "Edit Consumable Details";
        JDialog dialog = new JDialog(dashFrame, dialogTitle, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(540, 560);
        dialog.setLocationRelativeTo(dashFrame);

        JPanel contentPanel = new JPanel(new GridLayout(9, 2, 10, 12));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        contentPanel.setBackground(Color.WHITE);

        contentPanel.add(new JLabel("Item ID:"));
        JTextField txtId = new RoundedTextField(15);
        String defaultId;
        if (editRow == -1) {
            int nextNum = 101 + consumablesTableModel.getRowCount();
            while (LabDAO.consumableExists("CON-" + nextNum)) {
                nextNum++;
            }
            defaultId = "CON-" + nextNum;
            txtId.setEditable(true);
            txtId.setBackground(Color.WHITE);
        } else {
            defaultId = consumablesTableModel.getValueAt(editRow, 0).toString();
            txtId.setEditable(false);
            txtId.setBackground(new Color(245, 245, 245));
        }
        txtId.setText(defaultId);
        contentPanel.add(txtId);

        contentPanel.add(new JLabel("Item Name:"));
        String[] sampleConsumables = {
                "Glass Test Tubes", "Disposable Syringes",
                "Blood Vacutainers", "Nitrile Gloves",
                "Surgical Masks", "Hypodermic Needles",
                "Glass Slides", "Cotton Wool",
                "Alcohol Swabs"
        };
        JComboBox<String> cbName = new JComboBox<>(sampleConsumables);
        cbName.setEditable(true);
        contentPanel.add(cbName);

        contentPanel.add(new JLabel("Category:"));
        JTextField txtCat = new RoundedTextField(15);
        txtCat.setText("Consumables");
        txtCat.setEditable(false);
        txtCat.setBackground(new Color(245, 245, 245));
        contentPanel.add(txtCat);

        contentPanel.add(new JLabel("Quantity:"));
        JTextField txtQty = new RoundedTextField(15);
        txtQty.setText("100");
        contentPanel.add(txtQty);

        contentPanel.add(new JLabel("Unit of Measure:"));
        String[] units = {"Pieces", "Boxes (100 pcs)", "Boxes (50 pcs)", "Packs (50 pcs)", "Rolls", "Vials", "Pairs"};
        JComboBox<String> cbUnit = new JComboBox<>(units);
        cbUnit.setEditable(true);
        contentPanel.add(cbUnit);

        contentPanel.add(new JLabel("Batch Number:"));
        JTextField txtBatch = new RoundedTextField(15);
        txtBatch.setText("BTH-" + (int)(Math.random() * 9000 + 1000));
        contentPanel.add(txtBatch);

        contentPanel.add(new JLabel("Expiry Date:"));
        SpinnerDateModel expDateModel = new SpinnerDateModel();
        JSpinner expDateSpinner = new JSpinner(expDateModel);
        expDateSpinner.setEditor(new JSpinner.DateEditor(expDateSpinner, "dd-MMM-yyyy"));
        contentPanel.add(expDateSpinner);

        contentPanel.add(new JLabel("Supplier:"));
        JTextField txtSupplier = new RoundedTextField(15);
        txtSupplier.setText("BD Biosciences");
        contentPanel.add(txtSupplier);

        contentPanel.add(new JLabel("Minimum Stock:"));
        JTextField txtMinStock = new RoundedTextField(15);
        txtMinStock.setText("20");
        contentPanel.add(txtMinStock);

        if (editRow != -1) {
            cbName.setSelectedItem(consumablesTableModel.getValueAt(editRow, 1).toString());
            txtCat.setText(consumablesTableModel.getValueAt(editRow, 2).toString());
            txtQty.setText(consumablesTableModel.getValueAt(editRow, 3).toString());
            cbUnit.setSelectedItem(consumablesTableModel.getValueAt(editRow, 4).toString());
            txtBatch.setText(consumablesTableModel.getValueAt(editRow, 5).toString());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                expDateSpinner.setValue(sdf.parse(consumablesTableModel.getValueAt(editRow, 6).toString()));
            } catch (Exception ignored) {}
            txtSupplier.setText(consumablesTableModel.getValueAt(editRow, 7).toString());
            txtMinStock.setText(consumablesTableModel.getValueAt(editRow, 8).toString());
        }

        JButton btnSave = new RoundedButton((editRow == -1) ? "💾 Save to Database" : "💾 Update Record", 16);
        btnSave.setBackground(new Color(39, 174, 96)); // Green
        btnSave.setForeground(Color.WHITE);
        btnSave.setFont(new Font("Arial", Font.BOLD, 14));

        btnSave.addActionListener(e -> {
            String id = txtId.getText().trim();
            String name = cbName.getSelectedItem() != null ? cbName.getSelectedItem().toString().trim() : "";
            String cat = txtCat.getText().trim();
            String qty = txtQty.getText().trim();
            String unit = cbUnit.getSelectedItem() != null ? cbUnit.getSelectedItem().toString().trim() : "";
            String batch = txtBatch.getText().trim();
            String supplier = txtSupplier.getText().trim();
            String minStock = txtMinStock.getText().trim();

            // Form Validation (Requirement 8)
            if (id.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Item ID is mandatory and cannot be empty!", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (name.isEmpty() || qty.isEmpty() || unit.isEmpty() || batch.isEmpty() || supplier.isEmpty() || minStock.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory fields (Name, Quantity, Unit, Batch, Supplier, Min Stock)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int q, m;
            try {
                q = Integer.parseInt(qty);
                m = Integer.parseInt(minStock);
                if (q < 0 || m < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity and Minimum Stock must be valid non-negative numbers (0 or higher)!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
            String expDateStr = sdf.format(expDateSpinner.getValue());

            String[] rowData = new String[]{id, name, cat, String.valueOf(q), unit, batch, expDateStr, supplier, String.valueOf(m)};

            if (editRow == -1) {
                // Check duplicate ID
                if (LabDAO.consumableExists(id)) {
                    JOptionPane.showMessageDialog(dialog, "Item ID '" + id + "' already exists in MySQL database! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                for (int i = 0; i < consumablesTableModel.getRowCount(); i++) {
                    if (consumablesTableModel.getValueAt(i, 0).toString().equalsIgnoreCase(id)) {
                        JOptionPane.showMessageDialog(dialog, "Item ID '" + id + "' already exists in table! Please specify a unique ID.", "Duplicate Key Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
                // Save to MySQL via JDBC
                LabDAO.insertConsumable(rowData);
                consumablesTableModel.addRow(new Object[]{
                        id, name, cat, String.valueOf(q), unit, batch, expDateStr, supplier, String.valueOf(m), ""
                });
                saveTableData(consumablesTableModel, "inventory_consumables.txt");
                JOptionPane.showMessageDialog(dialog, "Consumable Supply '" + name + "' [ID: " + id + "] saved to MySQL database successfully!", "Record Added", JOptionPane.INFORMATION_MESSAGE);
            } else {
                // Update in MySQL via JDBC
                LabDAO.updateConsumable(rowData);
                consumablesTableModel.setValueAt(name, editRow, 1);
                consumablesTableModel.setValueAt(cat, editRow, 2);
                consumablesTableModel.setValueAt(String.valueOf(q), editRow, 3);
                consumablesTableModel.setValueAt(unit, editRow, 4);
                consumablesTableModel.setValueAt(batch, editRow, 5);
                consumablesTableModel.setValueAt(expDateStr, editRow, 6);
                consumablesTableModel.setValueAt(supplier, editRow, 7);
                consumablesTableModel.setValueAt(String.valueOf(m), editRow, 8);
                saveTableData(consumablesTableModel, "inventory_consumables.txt");
                JOptionPane.showMessageDialog(dialog, "Consumable details [ID: " + id + "] updated in MySQL database successfully!", "Record Updated", JOptionPane.INFORMATION_MESSAGE);
            }
            refreshOverviewTable();
            recalculateStats();
            dialog.dispose();
        });

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.add(btnSave);

        dialog.add(contentPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.setVisible(true);
    }

    // =========================================================================
    // OVERVIEW EDIT & DELETE ROUTING
    // =========================================================================
    void editOverviewItem(int row) {
        if (row < 0 || row >= overviewTableModel.getRowCount()) return;
        String id = overviewTableModel.getValueAt(row, 0).toString();
        String cat = overviewTableModel.getValueAt(row, 2).toString();

        if ("Lab Instruments".equalsIgnoreCase(cat)) {
            for (int i = 0; i < instrumentsTableModel.getRowCount(); i++) {
                if (instrumentsTableModel.getValueAt(i, 0).toString().equals(id)) {
                    showInstrumentForm(i);
                    return;
                }
            }
        } else if ("Medicines / Reagents".equalsIgnoreCase(cat)) {
            for (int i = 0; i < reagentsTableModel.getRowCount(); i++) {
                if (reagentsTableModel.getValueAt(i, 0).toString().equals(id)) {
                    showReagentForm(i);
                    return;
                }
            }
        } else if ("Consumables".equalsIgnoreCase(cat)) {
            for (int i = 0; i < consumablesTableModel.getRowCount(); i++) {
                if (consumablesTableModel.getValueAt(i, 0).toString().equals(id)) {
                    showConsumableForm(i);
                    return;
                }
            }
        }
    }

    void deleteOverviewItem(int row) {
        if (row < 0 || row >= overviewTableModel.getRowCount()) return;
        String id = overviewTableModel.getValueAt(row, 0).toString();
        String cat = overviewTableModel.getValueAt(row, 2).toString();

        if ("Lab Instruments".equalsIgnoreCase(cat)) {
            for (int i = 0; i < instrumentsTableModel.getRowCount(); i++) {
                if (instrumentsTableModel.getValueAt(i, 0).toString().equals(id)) {
                    deleteInstrument(i);
                    return;
                }
            }
        } else if ("Medicines / Reagents".equalsIgnoreCase(cat)) {
            for (int i = 0; i < reagentsTableModel.getRowCount(); i++) {
                if (reagentsTableModel.getValueAt(i, 0).toString().equals(id)) {
                    deleteReagent(i);
                    return;
                }
            }
        } else if ("Consumables".equalsIgnoreCase(cat)) {
            for (int i = 0; i < consumablesTableModel.getRowCount(); i++) {
                if (consumablesTableModel.getValueAt(i, 0).toString().equals(id)) {
                    deleteConsumable(i);
                    return;
                }
            }
        }
    }

    // =========================================================================
    // DELETION HANDLERS (Requirement 5: Delete Record with Confirmation)
    // =========================================================================
    void deleteInstrument(int row) {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(dashFrame, "Access Denied: Only Administrators are authorized to delete laboratory records.", "Permission Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (row < 0 || row >= instrumentsTableModel.getRowCount()) return;
        String id = instrumentsTableModel.getValueAt(row, 0).toString();
        String name = instrumentsTableModel.getValueAt(row, 1).toString();

        int choice = JOptionPane.showConfirmDialog(dashFrame,
                "Are you sure you want to permanently delete this lab instrument from the MySQL database?\n\n• ID: " + id + "\n• Name: " + name + "\n\nThis action cannot be undone.",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            LabDAO.deleteInstrument(id);
            instrumentsTableModel.removeRow(row);
            saveTableData(instrumentsTableModel, "inventory_instruments.txt");
            refreshOverviewTable();
            recalculateStats();
            JOptionPane.showMessageDialog(dashFrame, "Laboratory Instrument '" + name + "' [ID: " + id + "] has been permanently removed from MySQL database.", "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void deleteReagent(int row) {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(dashFrame, "Access Denied: Only Administrators are authorized to delete laboratory records.", "Permission Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (row < 0 || row >= reagentsTableModel.getRowCount()) return;
        String id = reagentsTableModel.getValueAt(row, 0).toString();
        String name = reagentsTableModel.getValueAt(row, 1).toString();

        int choice = JOptionPane.showConfirmDialog(dashFrame,
                "Are you sure you want to permanently delete this reagent from the MySQL database?\n\n• ID: " + id + "\n• Name: " + name + "\n\nThis action cannot be undone.",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            LabDAO.deleteReagent(id);
            reagentsTableModel.removeRow(row);
            saveTableData(reagentsTableModel, "inventory_reagents.txt");
            refreshOverviewTable();
            recalculateStats();
            JOptionPane.showMessageDialog(dashFrame, "Medicine / Reagent '" + name + "' [ID: " + id + "] has been permanently removed from MySQL database.", "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void deleteConsumable(int row) {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(dashFrame, "Access Denied: Only Administrators are authorized to delete laboratory records.", "Permission Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (row < 0 || row >= consumablesTableModel.getRowCount()) return;
        String id = consumablesTableModel.getValueAt(row, 0).toString();
        String name = consumablesTableModel.getValueAt(row, 1).toString();

        int choice = JOptionPane.showConfirmDialog(dashFrame,
                "Are you sure you want to permanently delete this consumable item from the MySQL database?\n\n• ID: " + id + "\n• Name: " + name + "\n\nThis action cannot be undone.",
                "Confirm Permanent Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            LabDAO.deleteConsumable(id);
            consumablesTableModel.removeRow(row);
            saveTableData(consumablesTableModel, "inventory_consumables.txt");
            refreshOverviewTable();
            recalculateStats();
            JOptionPane.showMessageDialog(dashFrame, "Consumable Supply '" + name + "' [ID: " + id + "] has been permanently removed from MySQL database.", "Record Deleted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    // FILE PERSISTENCE
    // =========================================================================
    private void saveTableData(DefaultTableModel model, String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                StringBuilder rowStr = new StringBuilder();
                for (int j = 0; j < model.getColumnCount() - 1; j++) {
                    Object val = model.getValueAt(i, j);
                    rowStr.append(val != null ? val.toString() : "");
                    if (j < model.getColumnCount() - 2) {
                        rowStr.append("||");
                    }
                }
                bw.write(rowStr.toString());
                bw.newLine();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadTableData(DefaultTableModel model, String filename) {
        File file = new File(filename);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] data = line.split("\\|\\|");
                Object[] rowData = new Object[model.getColumnCount()];
                for (int i = 0; i < data.length && i < model.getColumnCount() - 1; i++) {
                    rowData[i] = data[i];
                }
                rowData[model.getColumnCount() - 1] = "";
                model.addRow(rowData);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =========================================================================
    // UI HELPERS & COMPONENTS
    // =========================================================================
    private JButton createNavTab(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 14));
        btn.setForeground(new Color(189, 195, 199));
        btn.setBackground(new Color(10, 61, 98));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(0, 16, 4, 16));
        return btn;
    }

    private JPanel createStatCard(String title, String value, int iconType, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(14, 16, 12, 16)
        ));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 11));
        lblTitle.setForeground(new Color(127, 140, 141));

        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Arial", Font.BOLD, 22));
        lblVal.setForeground(new Color(44, 62, 80));

        textPanel.add(lblTitle);
        textPanel.add(lblVal);
        card.add(textPanel, BorderLayout.CENTER);

        // Vector Icon Badge
        JPanel badge = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 30));
                g2.fillOval(3, 3, 44, 44);

                g2.setColor(accentColor);
                if (iconType == 1) {
                    // Inventory Box / Package
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRect(14, 17, 22, 18);
                    g2.drawLine(14, 23, 36, 23);
                    g2.drawLine(25, 23, 25, 35);
                    g2.drawLine(20, 12, 25, 17);
                    g2.drawLine(30, 12, 25, 17);
                } else if (iconType == 2) {
                    // Microscope
                    g2.setStroke(new BasicStroke(2.2f));
                    g2.drawRect(20, 12, 10, 18);
                    g2.fillRect(23, 8, 4, 4);
                    g2.drawLine(25, 30, 25, 38);
                    g2.drawLine(15, 38, 35, 38);
                } else if (iconType == 3) {
                    // Chemical Flask
                    g2.setStroke(new BasicStroke(2.0f));
                    int[] xP = {21, 29, 36, 14};
                    int[] yP = {15, 15, 35, 35};
                    g2.drawPolygon(xP, yP, 4);
                    g2.drawLine(19, 15, 31, 15);
                    g2.fillRect(17, 28, 16, 7);
                } else if (iconType == 4) {
                    // Warning Shield / Alert
                    g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    int[] xT = {25, 14, 36};
                    int[] yT = {13, 36, 36};
                    g2.drawPolygon(xT, yT, 3);
                    g2.drawLine(25, 20, 25, 27);
                    g2.fillOval(24, 30, 3, 3);
                }
                g2.dispose();
            }
        };
        badge.setOpaque(false);
        badge.setPreferredSize(new Dimension(50, 50));
        card.add(badge, BorderLayout.EAST);

        return card;
    }

    private JPanel createProfileDataRow(String label, String val) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JLabel l = new JLabel(label);
        l.setFont(new Font("Arial", Font.BOLD, 13));
        l.setForeground(new Color(44, 62, 80));

        JLabel v = new JLabel(val);
        v.setFont(new Font("Arial", Font.PLAIN, 13));
        v.setForeground(new Color(52, 73, 94));

        p.add(l, BorderLayout.WEST);
        p.add(v, BorderLayout.CENTER);
        return p;
    }

    private JPanel createMiniStatWidget(String title, JLabel countLabel, Color col) {
        RoundedPanel p = new RoundedPanel(16, new Color(245, 246, 250));
        p.setLayout(new GridLayout(2, 1));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 224, 230), 1, true),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        countLabel.setFont(new Font("Arial", Font.BOLD, 20));
        countLabel.setForeground(col);

        JLabel t = new JLabel(title, JLabel.CENTER);
        t.setFont(new Font("Arial", Font.PLAIN, 12));
        t.setForeground(new Color(127, 140, 141));

        p.add(countLabel);
        p.add(t);
        return p;
    }

    private JPanel createPasswordRow(JPasswordField pwdField) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.setOpaque(false);
        panel.add(pwdField, BorderLayout.CENTER);

        JToggleButton btnToggle = new JToggleButton("\uD83D\uDC41");
        btnToggle.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnToggle.setFocusPainted(false);
        btnToggle.setBorderPainted(false);
        btnToggle.setContentAreaFilled(false);
        btnToggle.setCursor(new Cursor(Cursor.HAND_CURSOR));

        char defaultEcho = pwdField.getEchoChar();
        btnToggle.addActionListener(e -> {
            if (btnToggle.isSelected()) {
                pwdField.setEchoChar((char) 0);
                btnToggle.setText("\u2716");
            } else {
                pwdField.setEchoChar(defaultEcho);
                btnToggle.setText("\uD83D\uDC41");
            }
        });

        panel.add(btnToggle, BorderLayout.EAST);
        return panel;
    }

    private void showChangePasswordDialog() {
        JDialog dialog = new JDialog(dashFrame, "Change Password", true);
        dialog.setSize(400, 290);
        dialog.setLocationRelativeTo(dashFrame);
        dialog.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Current Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField txtCurr = new RoundedPasswordField(15);
        panel.add(createPasswordRow(txtCurr), gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0.0;
        panel.add(new JLabel("New Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField txtNew = new RoundedPasswordField(15);
        panel.add(createPasswordRow(txtNew), gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.weightx = 0.0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JPasswordField txtConf = new RoundedPasswordField(15);
        panel.add(createPasswordRow(txtConf), gbc);

        dialog.add(panel, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        RoundedButton btnSave = new RoundedButton("Save", 16);
        btnSave.setBackground(new Color(10, 61, 98));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            String curr = new String(txtCurr.getPassword());
            String newP = new String(txtNew.getPassword());
            String confP = new String(txtConf.getPassword());

            if (curr.isEmpty() || newP.isEmpty() || confP.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill all fields!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String role = isAdmin ? "Admin" : "Staff";
            String email = isAdmin ? "admin@poornima.org" : "staff@poornima.org";
            String authRole = LabDAO.authenticateUser(email, curr);
            String expectedPwd = isAdmin ? frame.getAdminPassword() : frame.getStaffPassword();
            if (authRole == null && !curr.equals(expectedPwd)) {
                JOptionPane.showMessageDialog(dialog, "Current password is incorrect!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!newP.equals(confP)) {
                JOptionPane.showMessageDialog(dialog, "New password and Confirm password do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            LabDAO.updatePassword(role, newP);
            JOptionPane.showMessageDialog(dialog, "Password updated successfully in MySQL database and local configuration!", "Success", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        RoundedButton btnCancel = new RoundedButton("Cancel", 16);
        btnCancel.addActionListener(e -> dialog.dispose());

        bottom.add(btnCancel);
        bottom.add(btnSave);
        dialog.add(bottom, BorderLayout.SOUTH);

        dialog.getRootPane().setDefaultButton(btnSave);
        dialog.setVisible(true);
    }

    // =========================================================================
    // REQUIREMENT 9: MENU BAR & TOOLBAR IMPLEMENTATION
    // =========================================================================

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(Color.WHITE);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 224, 230)));

        // 1. File Menu
        JMenu mnuFile = new JMenu("File");
        mnuFile.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem miNewRecord = new JMenuItem("➕ Add New Record");
        miNewRecord.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        miNewRecord.addActionListener(e -> addNewRecordDialog());

        JMenuItem miRefresh = new JMenuItem("🔄 Refresh Data");
        miRefresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0));
        miRefresh.addActionListener(e -> refreshAllDataFromDB());

        JMenuItem miSync = new JMenuItem("💾 Sync with MySQL Database");
        miSync.addActionListener(e -> syncDatabaseAction());

        JMenuItem miLogout = new JMenuItem("🚪 Logout");
        miLogout.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK));
        miLogout.addActionListener(e -> performLogout());

        JMenuItem miExit = new JMenuItem("❌ Exit");
        miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        miExit.addActionListener(e -> System.exit(0));

        mnuFile.add(miNewRecord);
        mnuFile.add(miRefresh);
        mnuFile.add(miSync);
        mnuFile.addSeparator();
        mnuFile.add(miLogout);
        mnuFile.add(miExit);

        // 2. Records Menu (CRUD Operations)
        JMenu mnuRecords = new JMenu("Records");
        mnuRecords.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem miAddInst = new JMenuItem("➕ Add Lab Instrument...");
        miAddInst.addActionListener(e -> showInstrumentForm(-1));
        JMenuItem miAddReag = new JMenuItem("➕ Add Medicine / Reagent...");
        miAddReag.addActionListener(e -> showReagentForm(-1));
        JMenuItem miAddCons = new JMenuItem("➕ Add Consumable Supply...");
        miAddCons.addActionListener(e -> showConsumableForm(-1));

        JMenuItem miEditSel = new JMenuItem("✏️ Edit Selected Record");
        miEditSel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        miEditSel.addActionListener(e -> editSelectedRecord());

        JMenuItem miDelSel = new JMenuItem("🗑️ Delete Selected Record");
        miDelSel.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        miDelSel.addActionListener(e -> deleteSelectedRecord());

        mnuRecords.add(miAddInst);
        mnuRecords.add(miAddReag);
        mnuRecords.add(miAddCons);
        mnuRecords.addSeparator();
        mnuRecords.add(miEditSel);
        mnuRecords.add(miDelSel);

        // 3. View Menu
        JMenu mnuView = new JMenu("View");
        mnuView.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem miViewHome = new JMenuItem("🏠 Home / Dashboard");
        miViewHome.addActionListener(e -> btnHome.doClick());
        JMenuItem miViewInv = new JMenuItem("🔬 Lab Inventory Directory");
        miViewInv.addActionListener(e -> btnInventory.doClick());
        JMenuItem miViewProfile = new JMenuItem("👤 User Profile & Password");
        miViewProfile.addActionListener(e -> btnProfile.doClick());
        JMenuItem miViewAbout = new JMenuItem("ℹ️ About System");
        miViewAbout.addActionListener(e -> btnAbout.doClick());

        mnuView.add(miViewHome);
        mnuView.add(miViewInv);
        mnuView.addSeparator();
        mnuView.add(miViewProfile);
        mnuView.add(miViewAbout);

        // 4. Database Menu (JDBC & Workbench Tools)
        JMenu mnuDb = new JMenu("MySQL Database");
        mnuDb.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem miTestConn = new JMenuItem("🔌 Test MySQL Connection");
        miTestConn.addActionListener(e -> showDatabaseStatusDialog());

        JMenuItem miDbSettings = new JMenuItem("⚙️ Database Connection Settings...");
        miDbSettings.addActionListener(e -> showDatabaseConfigDialog());

        JMenuItem miReimport = new JMenuItem("📥 Reset / Re-import Sample Data to MySQL");
        miReimport.addActionListener(e -> reimportSampleDataToMySQL());

        mnuDb.add(miTestConn);
        mnuDb.add(miDbSettings);
        mnuDb.addSeparator();
        mnuDb.add(miReimport);

        // 5. Help Menu
        JMenu mnuHelp = new JMenu("Help");
        mnuHelp.setFont(new Font("Arial", Font.PLAIN, 13));

        JMenuItem miWorkbenchGuide = new JMenuItem("📖 MySQL Workbench Setup Guide");
        miWorkbenchGuide.addActionListener(e -> showWorkbenchGuideDialog());

        JMenuItem miAboutMedLab = new JMenuItem("🔬 About MedLab Workstation");
        miAboutMedLab.addActionListener(e -> btnAbout.doClick());

        mnuHelp.add(miWorkbenchGuide);
        mnuHelp.add(miAboutMedLab);

        menuBar.add(mnuFile);
        menuBar.add(mnuRecords);
        menuBar.add(mnuView);
        menuBar.add(mnuDb);
        menuBar.add(mnuHelp);

        return menuBar;
    }

    private JToolBar createToolBar() {
        JToolBar tb = new JToolBar("MedLab Operations Toolbar");
        tb.setFloatable(false);
        tb.setBackground(new Color(248, 249, 250));
        tb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(222, 226, 230)),
                BorderFactory.createEmptyBorder(6, 16, 6, 16)
        ));

        // [➕ Add Record] Button
        JButton btnAdd = createToolBarButton("➕ Add Record", new Color(10, 61, 98));
        btnAdd.addActionListener(e -> addNewRecordDialog());
        btnAdd.setVisible(isAdmin);

        // [✏️ Edit Selected] Button
        JButton btnEdit = createToolBarButton("✏️ Edit Selected", new Color(41, 128, 185));
        btnEdit.addActionListener(e -> editSelectedRecord());
        btnEdit.setVisible(isAdmin);

        // [🗑️ Delete Selected] Button
        JButton btnDelete = createToolBarButton("🗑️ Delete Selected", new Color(231, 76, 60));
        btnDelete.addActionListener(e -> deleteSelectedRecord());
        btnDelete.setVisible(isAdmin);

        // [🔄 Refresh (F5)] Button
        JButton btnRefresh = createToolBarButton("🔄 Refresh Data", new Color(52, 73, 94));
        btnRefresh.addActionListener(e -> refreshAllDataFromDB());

        tb.add(btnAdd);
        tb.add(Box.createHorizontalStrut(6));
        tb.add(btnEdit);
        tb.add(Box.createHorizontalStrut(6));
        tb.add(btnDelete);
        tb.add(Box.createHorizontalStrut(6));
        tb.add(btnRefresh);

        tb.add(Box.createHorizontalStrut(16));
        tb.addSeparator(new Dimension(1, 24));
        tb.add(Box.createHorizontalStrut(16));

        // Quick Category Navigation Buttons
        JLabel lblQuick = new JLabel("Category: ");
        lblQuick.setFont(new Font("Arial", Font.BOLD, 12));
        lblQuick.setForeground(new Color(100, 116, 139));
        tb.add(lblQuick);

        JButton btnJmpInst = createToolBarCategoryButton("🔬 Instruments");
        btnJmpInst.addActionListener(e -> {
            btnInventory.doClick();
            btnCatInstruments.doClick();
        });
        JButton btnJmpReag = createToolBarCategoryButton("🧪 Reagents");
        btnJmpReag.addActionListener(e -> {
            btnInventory.doClick();
            btnCatReagents.doClick();
        });
        JButton btnJmpCons = createToolBarCategoryButton("📦 Consumables");
        btnJmpCons.addActionListener(e -> {
            btnInventory.doClick();
            btnCatConsumables.doClick();
        });

        tb.add(btnJmpInst);
        tb.add(Box.createHorizontalStrut(4));
        tb.add(btnJmpReag);
        tb.add(Box.createHorizontalStrut(4));
        tb.add(btnJmpCons);

        tb.add(Box.createHorizontalGlue());

        // Live Database Status Badge (Clickable for connection test/config)
        btnDbStatusBadge = new JButton();
        btnDbStatusBadge.setFont(new Font("Arial", Font.BOLD, 12));
        btnDbStatusBadge.setFocusPainted(false);
        btnDbStatusBadge.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDbStatusBadge.addActionListener(e -> showDatabaseStatusDialog());
        updateDbStatusBadge();
        tb.add(btnDbStatusBadge);

        tb.add(Box.createHorizontalStrut(12));

        // Toolbar Logout Shortcut
        JButton btnToolLogout = createToolBarButton("🚪 Logout", new Color(192, 57, 43));
        btnToolLogout.addActionListener(e -> performLogout());
        tb.add(btnToolLogout);

        return tb;
    }

    private JButton createToolBarButton(String text, Color bg) {
        RoundedButton btn = new RoundedButton(text, 12);
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }

    private JButton createToolBarCategoryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.PLAIN, 12));
        btn.setForeground(new Color(44, 62, 80));
        btn.setBackground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(203, 213, 225), 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        return btn;
    }

    public void updateDbStatusBadge() {
        if (btnDbStatusBadge == null) return;
        boolean isConnected = DBConnection.isConnected();
        if (isConnected) {
            btnDbStatusBadge.setText("🟢 MySQL: Connected (" + DBConnection.getDbName() + ")");
            btnDbStatusBadge.setForeground(new Color(22, 101, 52));
            btnDbStatusBadge.setBackground(new Color(240, 253, 244));
            btnDbStatusBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(187, 247, 208), 1, true),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
            btnDbStatusBadge.setToolTipText("Connected to MySQL on " + DBConnection.getHost() + ":" + DBConnection.getPort() + ". Click to test connection.");
        } else {
            btnDbStatusBadge.setText("🟡 MySQL: Offline (File Fallback Mode)");
            btnDbStatusBadge.setForeground(new Color(154, 52, 18));
            btnDbStatusBadge.setBackground(new Color(255, 247, 237));
            btnDbStatusBadge.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(254, 215, 170), 1, true),
                    BorderFactory.createEmptyBorder(5, 12, 5, 12)
            ));
            btnDbStatusBadge.setToolTipText("MySQL Server is unreachable. Operating with local file fallback. Click to configure settings.");
        }
    }

    // Active Category and Active Table Finders
    public JTable getActiveTable() {
        if ("Home".equals(currentTabName)) return overviewTable;
        if ("Inventory".equals(currentTabName)) {
            if ("Instruments".equals(currentInvCategory)) return instrumentsTable;
            if ("Reagents".equals(currentInvCategory)) return reagentsTable;
            if ("Consumables".equals(currentInvCategory)) return consumablesTable;
        }
        return overviewTable;
    }

    public int getActiveCategory() {
        if ("Home".equals(currentTabName)) return 0;
        if ("Inventory".equals(currentTabName)) {
            if ("Instruments".equals(currentInvCategory)) return 1;
            if ("Reagents".equals(currentInvCategory)) return 2;
            if ("Consumables".equals(currentInvCategory)) return 3;
        }
        return 0;
    }

    // Central Add Record Router
    public void addNewRecordDialog() {
        int active = getActiveCategory();
        if (active == 1) {
            showInstrumentForm(-1);
        } else if (active == 2) {
            showReagentForm(-1);
        } else if (active == 3) {
            showConsumableForm(-1);
        } else {
            String[] options = {"🔬 Lab Instrument", "🧪 Medicine / Reagent", "📦 Consumable Supply"};
            int choice = JOptionPane.showOptionDialog(dashFrame,
                    "Select the type of record you wish to add to the database:",
                    "Add New Record - Select Category",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if (choice == 0) showInstrumentForm(-1);
            else if (choice == 1) showReagentForm(-1);
            else if (choice == 2) showConsumableForm(-1);
        }
    }

    // Central Edit Selected Record Router
    public void editSelectedRecord() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(dashFrame, "Access Denied: Only Administrators can modify records.", "Permission Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTable table = getActiveTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(dashFrame, "Please click and select a record from the table first to edit!", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int viewRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(viewRow);
        int cat = getActiveCategory();

        if (cat == 1) showInstrumentForm(modelRow);
        else if (cat == 2) showReagentForm(modelRow);
        else if (cat == 3) showConsumableForm(modelRow);
        else editOverviewItem(modelRow);
    }

    // Central Delete Selected Record Router
    public void deleteSelectedRecord() {
        if (!isAdmin) {
            JOptionPane.showMessageDialog(dashFrame, "Access Denied: Only Administrators can delete records.", "Permission Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JTable table = getActiveTable();
        if (table == null || table.getSelectedRow() == -1) {
            JOptionPane.showMessageDialog(dashFrame, "Please click and select a record from the table first to delete!", "Selection Required", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int viewRow = table.getSelectedRow();
        int modelRow = table.convertRowIndexToModel(viewRow);
        int cat = getActiveCategory();

        if (cat == 1) deleteInstrument(modelRow);
        else if (cat == 2) deleteReagent(modelRow);
        else if (cat == 3) deleteConsumable(modelRow);
        else deleteOverviewItem(modelRow);
    }

    // Central Refresh from MySQL Database (Requirement 7)
    public void refreshAllDataFromDB() {
        // Ensure tables exist in MySQL
        LabDAO.initDatabaseTables();

        // 1. Instruments
        List<String[]> insts = LabDAO.getAllInstruments();
        instrumentsTableModel.setRowCount(0);
        for (String[] row : insts) {
            Object[] r = new Object[13];
            System.arraycopy(row, 0, r, 0, Math.min(row.length, 12));
            r[12] = "";
            instrumentsTableModel.addRow(r);
        }
        saveTableData(instrumentsTableModel, "inventory_instruments.txt");

        // 2. Reagents
        List<String[]> reags = LabDAO.getAllReagents();
        reagentsTableModel.setRowCount(0);
        for (String[] row : reags) {
            Object[] r = new Object[12];
            System.arraycopy(row, 0, r, 0, Math.min(row.length, 11));
            r[11] = "";
            reagentsTableModel.addRow(r);
        }
        saveTableData(reagentsTableModel, "inventory_reagents.txt");

        // 3. Consumables
        List<String[]> cons = LabDAO.getAllConsumables();
        consumablesTableModel.setRowCount(0);
        for (String[] row : cons) {
            Object[] r = new Object[10];
            System.arraycopy(row, 0, r, 0, Math.min(row.length, 9));
            r[9] = "";
            consumablesTableModel.addRow(r);
        }
        saveTableData(consumablesTableModel, "inventory_consumables.txt");

        // Refresh overview table, stats, and live badge
        refreshOverviewTable();
        recalculateStats();
        updateDbStatusBadge();
    }

    public void syncDatabaseAction() {
        refreshAllDataFromDB();
        boolean ok = DBConnection.isConnected();
        if (ok) {
            JOptionPane.showMessageDialog(dashFrame,
                    "Database synchronization successful!\nAll records are up-to-date with MySQL Server ('medlab_db').",
                    "Database Sync", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(dashFrame,
                    "MySQL Server is currently unreachable.\nWorking in local file storage mode.\nData is saved locally in inventory text files.",
                    "Offline Mode", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void performLogout() {
        int choice = JOptionPane.showConfirmDialog(dashFrame,
                "Are you sure you want to end your workstation session and logout?",
                "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            dashFrame.dispose();
            frame.main(new String[]{});
        }
    }

    // =========================================================================
    // MYSQL CONNECTION & CONFIGURATION DIALOGS
    // =========================================================================

    public void showDatabaseStatusDialog() {
        JDialog dlg = new JDialog(dashFrame, "MySQL Database Connection Diagnostics", true);
        dlg.setSize(520, 420);
        dlg.setLocationRelativeTo(dashFrame);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        content.setBackground(Color.WHITE);

        JLabel lblHdr = new JLabel("MySQL JDBC Connectivity Status");
        lblHdr.setFont(new Font("Arial", Font.BOLD, 16));
        lblHdr.setForeground(new Color(10, 61, 98));
        content.add(lblHdr, BorderLayout.NORTH);

        JTextArea txtReport = new JTextArea();
        txtReport.setEditable(false);
        txtReport.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReport.setBackground(new Color(248, 250, 252));
        txtReport.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        txtReport.setText(DBConnection.testConnectionDetails());

        JScrollPane sp = new JScrollPane(txtReport);
        sp.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));
        content.add(sp, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnRetest = new RoundedButton("🔄 Re-test", 14);
        btnRetest.setBackground(new Color(41, 128, 185));
        btnRetest.setForeground(Color.WHITE);
        btnRetest.addActionListener(e -> {
            txtReport.setText(DBConnection.testConnectionDetails());
            updateDbStatusBadge();
        });

        JButton btnConfig = new RoundedButton("⚙️ Settings...", 14);
        btnConfig.setBackground(new Color(52, 73, 94));
        btnConfig.setForeground(Color.WHITE);
        btnConfig.addActionListener(e -> {
            dlg.dispose();
            showDatabaseConfigDialog();
        });

        JButton btnClose = new RoundedButton("Close", 14);
        btnClose.addActionListener(e -> dlg.dispose());

        btnPanel.add(btnRetest);
        btnPanel.add(btnConfig);
        btnPanel.add(btnClose);
        dlg.add(content, BorderLayout.CENTER);
        dlg.add(btnPanel, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public void showDatabaseConfigDialog() {
        JDialog dlg = new JDialog(dashFrame, "MySQL Database Connection Settings", true);
        dlg.setSize(480, 360);
        dlg.setLocationRelativeTo(dashFrame);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 12));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        form.setBackground(Color.WHITE);

        form.add(new JLabel("Server Host:"));
        JTextField txtHost = new RoundedTextField(15);
        txtHost.setText(DBConnection.getHost());
        form.add(txtHost);

        form.add(new JLabel("Port:"));
        JTextField txtPort = new RoundedTextField(15);
        txtPort.setText(DBConnection.getPort());
        form.add(txtPort);

        form.add(new JLabel("Database Name:"));
        JTextField txtDb = new RoundedTextField(15);
        txtDb.setText(DBConnection.getDbName());
        form.add(txtDb);

        form.add(new JLabel("MySQL Username:"));
        JTextField txtUser = new RoundedTextField(15);
        txtUser.setText(DBConnection.getUser());
        form.add(txtUser);

        form.add(new JLabel("MySQL Password:"));
        JPasswordField txtPwd = new RoundedPasswordField(15);
        txtPwd.setText(DBConnection.getPassword());
        form.add(txtPwd);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnTest = new RoundedButton("🔌 Test Connection", 14);
        btnTest.setBackground(new Color(52, 152, 219));
        btnTest.setForeground(Color.WHITE);
        btnTest.addActionListener(e -> {
            DBConnection.setCredentials(txtHost.getText().trim(), txtPort.getText().trim(), txtDb.getText().trim(), txtUser.getText().trim(), new String(txtPwd.getPassword()));
            if (DBConnection.isConnected()) {
                JOptionPane.showMessageDialog(dlg, "Connection to MySQL Server is successful!", "Connection Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dlg, "Connection failed! Please verify MySQL server is running on " + txtHost.getText().trim() + ":" + txtPort.getText().trim() + " and credentials are correct.", "Connection Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnSave = new RoundedButton("💾 Save & Connect", 14);
        btnSave.setBackground(new Color(16, 185, 129));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> {
            DBConnection.setCredentials(txtHost.getText().trim(), txtPort.getText().trim(), txtDb.getText().trim(), txtUser.getText().trim(), new String(txtPwd.getPassword()));
            refreshAllDataFromDB();
            updateDbStatusBadge();
            JOptionPane.showMessageDialog(dlg, "MySQL settings saved and reconnected!", "Saved", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        JButton btnCancel = new RoundedButton("Cancel", 14);
        btnCancel.addActionListener(e -> dlg.dispose());

        bottom.add(btnTest);
        bottom.add(btnSave);
        bottom.add(btnCancel);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    public void showWorkbenchGuideDialog() {
        JDialog dlg = new JDialog(dashFrame, "MySQL Workbench Setup & Connection Guide", true);
        dlg.setSize(640, 520);
        dlg.setLocationRelativeTo(dashFrame);
        dlg.setLayout(new BorderLayout());

        JEditorPane ep = new JEditorPane();
        ep.setContentType("text/html");
        ep.setEditable(false);
        ep.setText("<html><body style='font-family: Arial; padding: 15px; color: #2c3e50;'>"
                + "<h2 style='color: #0a3d62;'>🐬 MySQL Workbench Setup & Verification Guide</h2>"
                + "<p>The Medical Laboratory Management System is pre-configured to connect to <b>MySQL Server</b> on <code>localhost:3306</code>.</p>"
                + "<hr color='#e2e8f0'/>"
                + "<h3>1. Open Database Script in MySQL Workbench:</h3>"
                + "<ul>"
                + "<li>Launch <b>MySQL Workbench</b> from your Windows Start Menu.</li>"
                + "<li>Open your <b>Local instance MySQL80</b> connection.</li>"
                + "<li>Click <b>File -> Open SQL Script...</b> and select:</li>"
                + "<code style='background: #f1f5f9; padding: 4px 8px; font-weight: bold;'>medlab_schema.sql</code> (located in project root folder)."
                + "<li>Click the yellow <b>Lightning Bolt ⚡</b> icon in Workbench to execute the script.</li>"
                + "</ul>"
                + "<h3>2. Explore Tables in MySQL Workbench:</h3>"
                + "<ul>"
                + "<li>In the left <b>SCHEMAS</b> panel, right-click and click <b>Refresh All</b>.</li>"
                + "<li>Expand <b>medlab_db</b> -> <b>Tables</b> to see:</li>"
                + "<ul>"
                + "<li><code>instruments</code> (Laboratory Diagnostic Equipment)</li>"
                + "<li><code>reagents</code> (Medicines & Test Kits)</li>"
                + "<li><code>consumables</code> (Disposables & Supplies)</li>"
                + "<li><code>users</code> (Admin & Staff Accounts)</li>"
                + "</ul>"
                + "<li>Right-click on any table and select <b>Select Rows - Limit 1000</b> to view records live.</li>"
                + "</ul>"
                + "<h3>3. Live Two-Way Synchronization:</h3>"
                + "<p>Any record added, edited, or deleted in this Java Swing application is instantly reflected in MySQL Workbench tables!</p>"
                + "</body></html>");

        JScrollPane sp = new JScrollPane(ep);
        sp.setBorder(null);
        dlg.add(sp, BorderLayout.CENTER);

        JPanel pnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        JButton btnClose = new RoundedButton("Close Guide", 14);
        btnClose.addActionListener(e -> dlg.dispose());
        pnl.add(btnClose);
        dlg.add(pnl, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    public void reimportSampleDataToMySQL() {
        int choice = JOptionPane.showConfirmDialog(dashFrame,
                "This will reset all tables in MySQL database ('medlab_db') with default sample lab records.\nAre you sure you want to proceed?",
                "Confirm Sample Data Reset", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            LabDAO.initDatabaseTables();
            refreshAllDataFromDB();
            JOptionPane.showMessageDialog(dashFrame, "Sample laboratory dataset successfully restored in MySQL database!", "Reset Complete", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public static void main(String[] args) {
        new Dashboard(true);
    }
}

// =========================================================================
// CUSTOM UI COMPONENTS & RENDERERS
// =========================================================================

// Custom Action Buttons Column (Edit & Delete)
class ActionRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
    public ActionRenderer() {
        setLayout(new GridBagLayout());
        setOpaque(true);
        setBackground(Color.WHITE);

        RoundedButton btnEdit = new RoundedButton("✎ Edit", 14);
        btnEdit.setBackground(new Color(41, 128, 185)); // Ocean Blue
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Arial", Font.BOLD, 11));
        btnEdit.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        RoundedButton btnDelete = new RoundedButton("✖ Delete", 14);
        btnDelete.setBackground(new Color(231, 76, 60)); // Coral Red
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Arial", Font.BOLD, 11));
        btnDelete.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 4);

        add(btnEdit, gbc);
        add(btnDelete, gbc);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        setBackground(isSelected ? table.getSelectionBackground() : Color.WHITE);
        return this;
    }
}

class ActionEditor extends DefaultCellEditor {
    private JPanel panel;
    private JButton btnEdit;
    private JButton btnDelete;
    private int currentRow;
    private JTable currentTable;
    private int tableType; // 1=Instrument, 2=Reagent, 3=Consumable, 6=Overview
    private Dashboard dashboard;

    public ActionEditor(JCheckBox checkBox, int tableType, Dashboard dashboard) {
        super(checkBox);
        this.tableType = tableType;
        this.dashboard = dashboard;
        panel = new JPanel(new GridBagLayout());
        panel.setOpaque(true);
        panel.setBackground(Color.WHITE);

        btnEdit = new RoundedButton("✎ Edit", 14);
        btnEdit.setBackground(new Color(41, 128, 185));
        btnEdit.setForeground(Color.WHITE);
        btnEdit.setFont(new Font("Arial", Font.BOLD, 11));
        btnEdit.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        btnDelete = new RoundedButton("✖ Delete", 14);
        btnDelete.setBackground(new Color(231, 76, 60));
        btnDelete.setForeground(Color.WHITE);
        btnDelete.setFont(new Font("Arial", Font.BOLD, 11));
        btnDelete.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));

        btnEdit.addActionListener(e -> {
            fireEditingStopped();
            int modelRow = currentTable != null ? currentTable.convertRowIndexToModel(currentRow) : currentRow;
            if (this.tableType == 1) this.dashboard.showInstrumentForm(modelRow);
            else if (this.tableType == 2) this.dashboard.showReagentForm(modelRow);
            else if (this.tableType == 3) this.dashboard.showConsumableForm(modelRow);
            else if (this.tableType == 6) this.dashboard.editOverviewItem(modelRow);
        });

        btnDelete.addActionListener(e -> {
            fireEditingStopped();
            int modelRow = currentTable != null ? currentTable.convertRowIndexToModel(currentRow) : currentRow;
            if (this.tableType == 1) this.dashboard.deleteInstrument(modelRow);
            else if (this.tableType == 2) this.dashboard.deleteReagent(modelRow);
            else if (this.tableType == 3) this.dashboard.deleteConsumable(modelRow);
            else if (this.tableType == 6) this.dashboard.deleteOverviewItem(modelRow);
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 4, 0, 4);

        panel.add(btnEdit, gbc);
        panel.add(btnDelete, gbc);
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        currentTable = table;
        currentRow = row;
        panel.setBackground(table.getSelectionBackground());
        return panel;
    }

    @Override
    public Object getCellEditorValue() {
        return "";
    }
}

// Multi-line Text Cell Renderer
class TextAreaRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
    private JTextArea textArea;

    public TextAreaRenderer() {
        setLayout(new GridBagLayout());
        setOpaque(true);

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setOpaque(false);
        textArea.setFont(new Font("Arial", Font.PLAIN, 13));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(textArea, gbc);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        if (isSelected) {
            setBackground(table.getSelectionBackground());
            textArea.setForeground(table.getSelectionForeground());
        } else {
            setBackground(Color.WHITE);
            textArea.setForeground(new Color(44, 62, 80));
        }

        String text = value != null ? value.toString() : "";
        textArea.setText(text);
        setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        return this;
    }
}

// Status & Stock Pill Renderer
class StatusPillRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 12));

        String text = value != null ? value.toString() : "";

        if (text.contains("Operational") || text.contains("In Stock")) {
            label.setForeground(new Color(39, 174, 96)); // Green
        } else if (text.contains("LOW STOCK") || text.contains("Out of Service")) {
            label.setForeground(new Color(231, 76, 60)); // Red
        } else if (text.contains("Under Maintenance") || text.contains("Calibration Required")) {
            label.setForeground(new Color(230, 126, 34)); // Orange
        } else {
            label.setForeground(new Color(52, 73, 94));
        }

        if (!isSelected) {
            label.setBackground(Color.WHITE);
        }
        return label;
    }
}

// Stock Quantity Renderer (Highlights in Red/Orange if Qty <= Min Stock)
class StockQtyRenderer extends DefaultTableCellRenderer {
    private int qtyCol;
    private int minCol;

    public StockQtyRenderer(int qtyCol, int minCol) {
        this.qtyCol = qtyCol;
        this.minCol = minCol;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 13));

        try {
            int modelRow = table.convertRowIndexToModel(row);
            int q = Integer.parseInt(table.getModel().getValueAt(modelRow, qtyCol).toString().replaceAll("[^0-9]", ""));
            int m = Integer.parseInt(table.getModel().getValueAt(modelRow, minCol).toString().replaceAll("[^0-9]", ""));

            if (q <= m) {
                label.setForeground(new Color(231, 76, 60)); // Red warning
            } else {
                label.setForeground(new Color(44, 62, 80));
            }
        } catch (Exception e) {
            label.setForeground(new Color(44, 62, 80));
        }

        if (!isSelected) {
            label.setBackground(Color.WHITE);
        }
        return label;
    }
}

