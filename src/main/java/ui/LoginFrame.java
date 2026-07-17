package ui;

import components.CardPanel;
import components.ModernTextField;
import components.StyledButton;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import model.Admin;
import service.AuthService;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final ModernTextField usernameField = new ModernTextField(20);
    private final ModernTextField.Password passwordField = new ModernTextField.Password();
    private final JCheckBox rememberCheck = new JCheckBox("Remember Me");
    private final StyledButton loginButton = new StyledButton("Login");

    public LoginFrame() {
        setTitle("Grand Azure — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(440, 520);
        setMinimumSize(new Dimension(400, 480));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.bgPrimary());
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setBorder(new EmptyBorder(24, 24, 24, 24));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));

        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.GOLD);
                g2.fillOval(0, 0, 64, 64);
                g2.setColor(Theme.DARK_NAVY);
                g2.setFont(Theme.fontBold(22));
                g2.drawString("GA", 18, 40);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(64, 64);
            }
        };
        logo.setOpaque(false);
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Grand Azure Hotel");
        title.setFont(Theme.fontDisplay(22));
        title.setForeground(Theme.textPrimary());
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Property Management System");
        subtitle.setFont(Theme.fontRegular(12));
        subtitle.setForeground(Theme.textSecondary());
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        header.add(logo);
        header.add(javax.swing.Box.createVerticalStrut(12));
        header.add(title);
        header.add(javax.swing.Box.createVerticalStrut(4));
        header.add(subtitle);

        CardPanel formCard = new CardPanel(new GridBagLayout());
        formCard.setBorder(new EmptyBorder(24, 28, 24, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, 6, 0);
        formCard.add(label("Username"), gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 16, 0);
        usernameField.setPreferredSize(new Dimension(0, 40));
        formCard.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(0, 0, 6, 0);
        formCard.add(label("Password"), gbc);

        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);
        passwordField.setPreferredSize(new Dimension(0, 40));
        formCard.add(passwordField, gbc);

        rememberCheck.setOpaque(false);
        rememberCheck.setFont(Theme.fontRegular(12));
        rememberCheck.setForeground(Theme.textSecondary());
        gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(0, 0, 16, 0);
        formCard.add(rememberCheck, gbc);

        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(0, 0, 8, 0);
        loginButton.setPreferredSize(new Dimension(0, 42));
        formCard.add(loginButton, gbc);

        StyledButton forgotBtn = new StyledButton("Forgot Password?", StyledButton.Style.GHOST);
        forgotBtn.setPreferredSize(new Dimension(160, 32));
        JPanel forgotPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        forgotPanel.setOpaque(false);
        forgotPanel.add(forgotBtn);
        gbc.gridy = 6;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        formCard.add(forgotPanel, gbc);

        root.add(header, BorderLayout.NORTH);
        root.add(formCard, BorderLayout.CENTER);
        setContentPane(root);

        loginButton.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        forgotBtn.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));

        tryRememberMeOnLoad();
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        return lbl;
    }

    private void tryRememberMeOnLoad() {
        new SwingWorker<Admin, Void>() {
            @Override
            protected Admin doInBackground() throws Exception {
                return authService.tryRememberMe();
            }

            @Override
            protected void done() {
                try {
                    Admin admin = get();
                    if (admin != null) {
                        openMain();
                    }
                } catch (Exception ignored) {
                    // stay on login
                }
            }
        }.execute();
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();
        if (username.isEmpty() || password.length == 0) {
            Toast.error(this, "Please enter username and password");
            return;
        }

        loginButton.setEnabled(false);
        new SwingWorker<Admin, Void>() {
            @Override
            protected Admin doInBackground() throws Exception {
                return authService.login(username, new String(password));
            }

            @Override
            protected void done() {
                loginButton.setEnabled(true);
                java.util.Arrays.fill(password, '\0');
                try {
                    Admin admin = get();
                    if (admin == null) {
                        Toast.error(LoginFrame.this, "Invalid username or password");
                        return;
                    }
                    if (rememberCheck.isSelected()) {
                        authService.enableRememberMe(admin.getAdminId());
                    }
                    openMain();
                } catch (Exception ex) {
                    Toast.error(LoginFrame.this, "Login failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void openMain() {
        dispose();
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}
