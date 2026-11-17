import javax.swing.*;
import java.awt.*;
import java.io.*;


public class frame {

    private static final String ADMIN_PWD_FILE = "admin_pwd.txt";
    private static final String STAFF_PWD_FILE = "staff_pwd.txt";

    public static String getAdminPassword() {
        try {
            File f = new File(ADMIN_PWD_FILE);
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String pwd = br.readLine();
                br.close();
                if (pwd != null && !pwd.trim().isEmpty()) {
                    return pwd.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin";
    }

    public static void updateAdminPassword(String newPwd) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(ADMIN_PWD_FILE));
            bw.write(newPwd);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getStaffPassword() {
        try {
            File f = new File(STAFF_PWD_FILE);
            if (f.exists()) {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String pwd = br.readLine();
                br.close();
                if (pwd != null && !pwd.trim().isEmpty()) {
                    return pwd.trim();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "staff";
    }

    public static void updateStaffPassword(String newPwd) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(STAFF_PWD_FILE));
            bw.write(newPwd);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getPassword() {
        return getAdminPassword();
    }

    public static void updatePassword(String newPwd) {
        updateAdminPassword(newPwd);
    }

    public static void main(String[] args) {
        JFrame loginFrame = new JFrame("Medical Laboratory Management System - Login");
        loginFrame.setSize(1200, 720);
        loginFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(new BorderLayout());

        // ================= 1. TOP HEADER BANNER (POORNIMA LOGO BANNER) =================
        JPanel topHeader = new JPanel(new BorderLayout());
        topHeader.setBackground(Color.WHITE);
        topHeader.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String headerPath = new File("src/images/img_3.png").exists() ? "src/images/img_3.png" : "images/img_3.png";
        ImageIcon headerIcon = new ImageIcon(headerPath);
        JLabel headerLabel = new JLabel(headerIcon, JLabel.CENTER);
        topHeader.add(headerLabel, BorderLayout.CENTER);

        loginFrame.add(topHeader, BorderLayout.NORTH);


        // ================= 2. CENTER CONTENT (FAMILIAR LOGIN CARD) =================
        JPanel centerContainer = new JPanel(new GridBagLayout());
        centerContainer.setBackground(new Color(245, 246, 250)); // Soft clean desktop background

        // Central Login Box
        JPanel loginCard = new JPanel();
        loginCard.setLayout(null);
        loginCard.setPreferredSize(new Dimension(680, 440));
        loginCard.setBackground(Color.WHITE);
        loginCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(218, 225, 231), 1, true),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
        ));

        // Card Top Heading
        JLabel lblCardHeading = new JLabel("LABORATORY SYSTEM LOGIN", JLabel.CENTER);
        lblCardHeading.setFont(new Font("Arial", Font.BOLD, 20));
        lblCardHeading.setForeground(new Color(10, 61, 98));
        lblCardHeading.setBounds(0, 22, 680, 28);
        loginCard.add(lblCardHeading);

        JLabel lblCardSub = new JLabel("Sign in to access laboratory inventory, equipment, and supplies workstation", JLabel.CENTER);
        lblCardSub.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCardSub.setForeground(new Color(127, 140, 141));
        lblCardSub.setBounds(0, 52, 680, 20);
        loginCard.add(lblCardSub);

        // Role Switcher Radio Buttons
        JLabel lblRole = new JLabel("Select Role:");
        lblRole.setFont(new Font("Arial", Font.BOLD, 13));
        lblRole.setForeground(new Color(44, 62, 80));
        lblRole.setBounds(160, 88, 100, 24);
        loginCard.add(lblRole);

        JRadioButton rbAdmin = new JRadioButton("Administrator", true);
        rbAdmin.setFont(new Font("Arial", Font.BOLD, 13));
        rbAdmin.setForeground(new Color(10, 61, 98));
        rbAdmin.setOpaque(false);
        rbAdmin.setBounds(260, 88, 130, 24);

        JRadioButton rbStaff = new JRadioButton("Laboratory Staff", false);
        rbStaff.setFont(new Font("Arial", Font.BOLD, 13));
        rbStaff.setForeground(new Color(10, 61, 98));
        rbStaff.setOpaque(false);
        rbStaff.setBounds(395, 88, 150, 24);

        ButtonGroup bg = new ButtonGroup();
        bg.add(rbAdmin);
        bg.add(rbStaff);
        loginCard.add(rbAdmin);
        loginCard.add(rbStaff);

        // Email / Username
        JLabel lblEmail = new JLabel("Email / Username:");
        lblEmail.setFont(new Font("Arial", Font.BOLD, 13));
        lblEmail.setForeground(new Color(44, 62, 80));
        lblEmail.setBounds(160, 128, 160, 20);
        loginCard.add(lblEmail);

        JTextField txtEmail = new RoundedTextField(20);
        txtEmail.setFont(new Font("Arial", Font.PLAIN, 14));
        txtEmail.setBounds(160, 152, 360, 36);
        txtEmail.setText("admin@poornima.org");
        txtEmail.setBackground(new Color(250, 250, 250));
        loginCard.add(txtEmail);

        // Password
        JLabel lblPass = new JLabel("Password:");
        lblPass.setFont(new Font("Arial", Font.BOLD, 13));
        lblPass.setForeground(new Color(44, 62, 80));
        lblPass.setBounds(160, 202, 120, 20);
        loginCard.add(lblPass);

        JPasswordField txtPass = new RoundedPasswordField(20);
        txtPass.setFont(new Font("Arial", Font.PLAIN, 14));
        txtPass.setBounds(160, 226, 360, 36);
        txtPass.setText("admin");
        txtPass.setBackground(new Color(250, 250, 250));
        loginCard.add(txtPass);


        JToggleButton btnEye = new JToggleButton("\uD83D\uDC41");
        btnEye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btnEye.setBounds(525, 226, 45, 36);
        btnEye.setFocusPainted(false);
        btnEye.setBorderPainted(false);
        btnEye.setContentAreaFilled(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        char defaultEcho = txtPass.getEchoChar();
        btnEye.addActionListener(e -> {
            if (btnEye.isSelected()) {
                txtPass.setEchoChar((char) 0);
                btnEye.setText("\u2716");
            } else {
                txtPass.setEchoChar(defaultEcho);
                btnEye.setText("\uD83D\uDC41");
            }
        });
        loginCard.add(btnEye);

        // Radio Button Actions (Auto-fills credentials)
        rbAdmin.addActionListener(e -> {
            txtEmail.setText("admin@poornima.org");
            txtPass.setText(getAdminPassword());
        });
        rbStaff.addActionListener(e -> {
            txtEmail.setText("staff@poornima.org");
            txtPass.setText(getStaffPassword());
        });

        // Login Button (Medical Cyan/Teal Rounded)
        RoundedButton btnLogin = new RoundedButton("LOGIN", 20);
        btnLogin.setBackground(new Color(10, 61, 98)); // Medical Dark Blue
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Arial", Font.BOLD, 15));
        btnLogin.setBounds(260, 288, 160, 42);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Quick Hint Label
        JLabel lblHint = new JLabel("<html><center><font color='#7f8c8d'><b>Admin:</b> admin@poornima.org (admin) &nbsp;|&nbsp; <b>Staff:</b> staff@poornima.org (staff)</font></center></html>", JLabel.CENTER);
        lblHint.setFont(new Font("Arial", Font.PLAIN, 12));
        lblHint.setBounds(0, 340, 680, 22);
        loginCard.add(lblHint);

        // Database Connectivity Badge
        boolean isDbUp = DBConnection.isConnected();
        String dbText = isDbUp ? "🟢 MySQL Database: Connected (medlab_db)" : "🟡 Local Mode (MySQL Offline / File Storage)";
        JLabel lblDbStatus = new JLabel(dbText, JLabel.CENTER);
        lblDbStatus.setFont(new Font("Arial", Font.BOLD, 11));
        lblDbStatus.setForeground(isDbUp ? new Color(39, 174, 96) : new Color(211, 84, 0));
        lblDbStatus.setBounds(0, 362, 680, 18);
        loginCard.add(lblDbStatus);

        JLabel lblRehan = new JLabel("Developed by: Mohammad Rehan | Medical Laboratory Management System", JLabel.CENTER);
        lblRehan.setFont(new Font("Arial", Font.PLAIN, 11));
        lblRehan.setForeground(new Color(149, 165, 166));
        lblRehan.setBounds(0, 382, 680, 18);
        loginCard.add(lblRehan);

        // Action Listener for Login (Uses LabDAO MySQL Authentication with File Fallback)
        btnLogin.addActionListener(e -> {
            String emailVal = txtEmail.getText().trim();
            String passVal = new String(txtPass.getPassword()).trim();

            if (emailVal.isEmpty() || passVal.isEmpty()) {
                JOptionPane.showMessageDialog(loginFrame, "Please enter your Email and Password!", "Login Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String authRole = LabDAO.authenticateUser(emailVal, passVal);
            if (authRole != null) {
                if ("Admin".equalsIgnoreCase(authRole)) {
                    new Dashboard(true);
                } else {
                    new Dashboard(false);
                }
                loginFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(loginFrame, "Invalid Email or Password! Please verify your credentials and try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        txtEmail.addActionListener(e -> btnLogin.doClick());
        txtPass.addActionListener(e -> btnLogin.doClick());

        loginCard.add(btnLogin);
        loginFrame.getRootPane().setDefaultButton(btnLogin);

        centerContainer.add(loginCard);
        loginFrame.add(centerContainer, BorderLayout.CENTER);

        // Try initializing DB in background
        new Thread(LabDAO::initDatabaseTables).start();

        loginFrame.setVisible(true);
    }
}

