package ui;

import components.CardPanel;
import components.Theme;
import components.Toast;
import components.UiLayout;
import components.StyledButton;
import model.Admin;
import service.AuthService;
import javax.swing.SwingUtilities;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class LoginFrame extends JFrame {

    private final AuthService authService = new AuthService();
    private final PremiumTextField usernameField = new PremiumTextField(IconType.USER, "Enter your username");
    private final PremiumPasswordField passwordField = new PremiumPasswordField("Enter your password");
    private final PremiumCheckBox rememberCheck = new PremiumCheckBox("Remember Me");
    private final StyledGradientButton loginButton = new StyledGradientButton("Login");

    private JPanel cardsContainer;

    public LoginFrame() {
        setTitle("Grand Azure — Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Start maximized for enterprise look
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(800, 600));

        // Root container with GridBagLayout to center the Authentication Card
        JPanel rootPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Deep full screen background gradient
                java.awt.GradientPaint gp = new java.awt.GradientPaint(
                        0, 0, new Color(0x0F, 0x17, 0x2A),
                        0, getHeight(), new Color(0x06, 0x0A, 0x13)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // Subtle luxury geometric decorative accent circles
                g2.setColor(new Color(0xC9, 0xA2, 0x27, 20));
                g2.setStroke(new java.awt.BasicStroke(1.2f));
                g2.drawOval(-120, -120, 360, 360);
                g2.drawOval(getWidth() - 240, getHeight() - 240, 360, 360);
                
                g2.dispose();
            }
        };

        // --- Create Centered Premium Authentication Card ---
        JPanel authCard = new JPanel(new BorderLayout(0, UiLayout.SPACE_LG)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Card Background (Theme bgCard or White)
                g2.setColor(Theme.bgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                // Glow outline border
                g2.setColor(Theme.border());
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                
                g2.dispose();
            }
        };
        authCard.setOpaque(false);
        authCard.setBorder(new EmptyBorder(40, 45, 40, 45));
        
        // Fixed dimensions to prevent stretching
        Dimension cardSize = new Dimension(680, 740);
        authCard.setPreferredSize(cardSize);
        authCard.setMinimumSize(cardSize);
        authCard.setMaximumSize(cardSize);

        // Header inside the Card
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new javax.swing.BoxLayout(header, javax.swing.BoxLayout.Y_AXIS));

        JPanel logo = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gold circular badge with soft outline
                g2.setColor(new Color(0xC9, 0xA2, 0x27, 40));
                g2.fillOval(0, 0, 56, 56);
                g2.setColor(Theme.GOLD);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawOval(4, 4, 48, 48);
                
                // Draw GA crest text
                g2.setColor(Theme.GOLD);
                g2.setFont(Theme.fontBold(18));
                g2.drawString("GA", 15, 34);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(56, 56);
            }

            @Override
            public Dimension getMaximumSize() {
                return getPreferredSize();
            }
        };
        logo.setOpaque(false);
        logo.setAlignmentX(CENTER_ALIGNMENT);

        JLabel title = new JLabel("Grand Azure Hotel");
        title.setFont(Theme.fontDisplay(22));
        title.setForeground(Theme.GOLD);
        title.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subtitle = new JLabel("Property Management System");
        subtitle.setFont(Theme.fontRegular(11));
        subtitle.setForeground(Theme.textSecondary());
        subtitle.setAlignmentX(CENTER_ALIGNMENT);

        header.add(logo);
        header.add(Box.createVerticalStrut(UiLayout.SPACE_SM));
        header.add(title);
        header.add(Box.createVerticalStrut(2));
        header.add(subtitle);

        cardsContainer = new JPanel(new java.awt.CardLayout());
        cardsContainer.setOpaque(false);

        // --- Build Login Card ---
        FadePanel loginCard = new FadePanel(new GridBagLayout());
        loginCard.setBorder(new EmptyBorder(UiLayout.SPACE_MD, UiLayout.SPACE_LG, UiLayout.SPACE_MD, UiLayout.SPACE_LG));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, 2, 0);

        int loginRow = 0;
        
        JLabel welcomeLbl = new JLabel("Welcome Back");
        welcomeLbl.setFont(Theme.fontMedium(18));
        welcomeLbl.setForeground(Theme.textPrimary());
        welcomeLbl.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        loginCard.add(welcomeLbl, gbc);

        JLabel signInSubtitle = new JLabel("Sign in to continue");
        signInSubtitle.setFont(Theme.fontRegular(12));
        signInSubtitle.setForeground(Theme.textSecondary());
        signInSubtitle.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_LG, 0);
        loginCard.add(signInSubtitle, gbc);

        // Username Field
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, 2, 0);
        loginCard.add(label("Username"), gbc);

        ValidationField loginUserWrap = new ValidationField(usernameField);
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_SM, 0);
        loginCard.add(loginUserWrap, gbc);

        // Password Field
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, 2, 0);
        loginCard.add(label("Password"), gbc);

        ValidationField loginPassWrap = new ValidationField(passwordField);
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_SM, 0);
        loginCard.add(loginPassWrap, gbc);

        // Remember Me & Forgot Password Row
        JPanel rememberRow = new JPanel(new BorderLayout());
        rememberRow.setOpaque(false);
        rememberRow.add(rememberCheck, BorderLayout.WEST);

        StyledButton forgotBtn = new StyledButton("Forgot Password?", StyledButton.Style.GHOST);
        forgotBtn.setFont(Theme.fontRegular(12));
        forgotBtn.setForeground(Theme.ROYAL_BLUE);
        rememberRow.add(forgotBtn, BorderLayout.EAST);

        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        loginCard.add(rememberRow, gbc);

        // Login Button
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        loginButton.setPreferredSize(new Dimension(0, 42));
        loginCard.add(loginButton, gbc);

        // Divider
        JLabel orLabel = new JLabel("────────────────  OR  ────────────────");
        orLabel.setFont(Theme.fontRegular(10));
        orLabel.setForeground(Theme.textMuted());
        orLabel.setHorizontalAlignment(JLabel.CENTER);
        gbc.gridy = loginRow++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_SM, 0);
        loginCard.add(orLabel, gbc);

        StyledButton showRegBtn = new StyledButton("Create Administrator Account", StyledButton.Style.GHOST);
        showRegBtn.setFont(Theme.fontMedium(13));
        showRegBtn.setForeground(Theme.GOLD);
        gbc.gridy = loginRow++;
        loginCard.add(showRegBtn, gbc);

        // --- Build Multi-Column Registration Card ---
        FadePanel registerCard = new FadePanel(new GridBagLayout());
        registerCard.setBorder(new EmptyBorder(UiLayout.SPACE_SM, UiLayout.SPACE_MD, UiLayout.SPACE_SM, UiLayout.SPACE_MD));

        GridBagConstraints regGbc = new GridBagConstraints();
        regGbc.fill = GridBagConstraints.HORIZONTAL;
        regGbc.weightx = 0.5; // Equal weight for columns 0 and 1
        regGbc.insets = new java.awt.Insets(0, 4, 1, 4);

        int regRow = 0;

        JLabel regTitle = new JLabel("Create Administrator Account");
        regTitle.setFont(Theme.fontMedium(18));
        regTitle.setForeground(Theme.textPrimary());
        regTitle.setHorizontalAlignment(JLabel.CENTER);
        regGbc.gridx = 0;
        regGbc.gridy = regRow++;
        regGbc.gridwidth = 2;
        regGbc.insets = new java.awt.Insets(0, 0, 2, 0);
        registerCard.add(regTitle, regGbc);

        JLabel regSubtitle = new JLabel("Set up the first administrator account");
        regSubtitle.setFont(Theme.fontRegular(11));
        regSubtitle.setForeground(Theme.textSecondary());
        regSubtitle.setHorizontalAlignment(JLabel.CENTER);
        regGbc.gridx = 0;
        regGbc.gridy = regRow++;
        regGbc.gridwidth = 2;
        regGbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        registerCard.add(regSubtitle, regGbc);

        // Inputs for Registration in columns
        PremiumTextField regNameField = new PremiumTextField(IconType.USER, "Full Name");
        ValidationField nameWrap = new ValidationField(regNameField);
        
        PremiumTextField regUserField = new PremiumTextField(IconType.USER, "Username");
        ValidationField userWrap = new ValidationField(regUserField);

        // Row 1 Labels
        regGbc.gridy = regRow;
        regGbc.gridwidth = 1;
        regGbc.gridx = 0;
        regGbc.insets = new java.awt.Insets(0, 4, 1, 4);
        registerCard.add(label("Full Name"), regGbc);
        
        regGbc.gridx = 1;
        registerCard.add(label("Username"), regGbc);

        // Row 1 Fields
        regRow++;
        regGbc.gridy = regRow;
        regGbc.gridx = 0;
        regGbc.insets = new java.awt.Insets(0, 4, 4, 4);
        registerCard.add(nameWrap, regGbc);
        
        regGbc.gridx = 1;
        registerCard.add(userWrap, regGbc);

        // Row 2: Email spans full width
        PremiumTextField regEmailField = new PremiumTextField(IconType.EMAIL, "Email Address");
        ValidationField emailWrap = new ValidationField(regEmailField);

        regRow++;
        regGbc.gridy = regRow;
        regGbc.gridx = 0;
        regGbc.gridwidth = 2;
        regGbc.insets = new java.awt.Insets(0, 4, 1, 4);
        registerCard.add(label("Email Address"), regGbc);

        regRow++;
        regGbc.gridy = regRow;
        regGbc.insets = new java.awt.Insets(0, 4, 4, 4);
        registerCard.add(emailWrap, regGbc);

        // Row 3: Passwords
        PremiumPasswordField regPassField = new PremiumPasswordField("Password");
        ValidationField passWrap = new ValidationField(regPassField);
        
        PremiumPasswordField regConfirmField = new PremiumPasswordField("Confirm Password");
        ValidationField confirmWrap = new ValidationField(regConfirmField);

        regRow++;
        regGbc.gridy = regRow;
        regGbc.gridwidth = 1;
        regGbc.gridx = 0;
        regGbc.insets = new java.awt.Insets(0, 4, 1, 4);
        registerCard.add(label("Password"), regGbc);
        
        regGbc.gridx = 1;
        registerCard.add(label("Confirm Password"), regGbc);

        regRow++;
        regGbc.gridy = regRow;
        regGbc.gridx = 0;
        regGbc.insets = new java.awt.Insets(0, 4, 4, 4);
        registerCard.add(passWrap, regGbc);
        
        regGbc.gridx = 1;
        registerCard.add(confirmWrap, regGbc);

        // Password requirements dashboard full width
        PasswordRequirementsPanel reqPanel = new PasswordRequirementsPanel();
        regRow++;
        regGbc.gridy = regRow;
        regGbc.gridx = 0;
        regGbc.gridwidth = 2;
        regGbc.insets = new java.awt.Insets(4, 4, UiLayout.SPACE_MD, 4);
        registerCard.add(reqPanel, regGbc);

        // Action Button full width
        StyledGradientButton registerBtn = new StyledGradientButton("Create Administrator Account");
        registerBtn.setPreferredSize(new Dimension(0, 42));
        regRow++;
        regGbc.gridy = regRow;
        regGbc.insets = new java.awt.Insets(0, 4, UiLayout.SPACE_MD, 4);
        registerCard.add(registerBtn, regGbc);

        // Bottom login redirect
        JPanel backLoginRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        backLoginRow.setOpaque(false);
        JLabel hasAccLabel = new JLabel("Already have an account?");
        hasAccLabel.setFont(Theme.fontRegular(12));
        hasAccLabel.setForeground(Theme.textSecondary());
        StyledButton showLoginBtn = new StyledButton("Login Here", StyledButton.Style.GHOST);
        showLoginBtn.setFont(Theme.fontMedium(12));
        showLoginBtn.setForeground(Theme.ROYAL_BLUE);
        backLoginRow.add(hasAccLabel);
        backLoginRow.add(showLoginBtn);

        regRow++;
        regGbc.gridy = regRow;
        regGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        registerCard.add(backLoginRow, regGbc);

        // Real time requirements checking
        regPassField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                reqPanel.validatePassword(new String(regPassField.getPassword()));
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        cardsContainer.add(loginCard, "login");
        cardsContainer.add(registerCard, "register");

        // Assemble authCard
        authCard.add(header, BorderLayout.NORTH);
        authCard.add(cardsContainer, BorderLayout.CENTER);

        // Add Authentication Card to GridBagLayout centering
        GridBagConstraints rootGbc = new GridBagConstraints();
        rootGbc.gridx = 0;
        rootGbc.gridy = 0;
        rootGbc.anchor = GridBagConstraints.CENTER;
        rootPanel.add(authCard, rootGbc);

        // Put the root panel in a JScrollPane to allow scrolling on small windows
        JScrollPane scroll = UiLayout.pageScroll(rootPanel);
        setContentPane(scroll);

        // Setup transitions
        showRegBtn.addActionListener(e -> animateCardTransition("register"));
        showLoginBtn.addActionListener(e -> animateCardTransition("login"));

        // Authentication action listeners
        loginButton.addActionListener(e -> doLogin(loginUserWrap, loginPassWrap));
        passwordField.addActionListener(e -> doLogin(loginUserWrap, loginPassWrap));
        forgotBtn.addActionListener(e -> new ForgotPasswordDialog(this).setVisible(true));

        // Registration form action listener
        registerBtn.addActionListener(e -> {
            String fullName = regNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String username = regUserField.getText().trim();
            char[] password = regPassField.getPassword();
            char[] confirm = regConfirmField.getPassword();

            nameWrap.clear();
            emailWrap.clear();
            userWrap.clear();
            passWrap.clear();
            confirmWrap.clear();

            boolean valid = true;

            if (fullName.isEmpty()) {
                nameWrap.setError("Name required");
                valid = false;
            } else {
                nameWrap.setSuccess("Valid Name");
            }

            if (email.isEmpty()) {
                emailWrap.setError("Email required");
                valid = false;
            } else if (!email.contains("@") || !email.contains(".")) {
                emailWrap.setError("Invalid Email");
                valid = false;
            } else {
                emailWrap.setSuccess("Valid Email");
            }

            if (username.isEmpty()) {
                userWrap.setError("Username required");
                valid = false;
            } else {
                userWrap.setSuccess("Username Available");
            }

            String passStr = new String(password);
            if (passStr.isEmpty()) {
                passWrap.setError("Password required");
                valid = false;
            } else {
                boolean len = passStr.length() >= 8;
                boolean upper = passStr.matches(".*[A-Z].*");
                boolean lower = passStr.matches(".*[a-z].*");
                boolean num = passStr.matches(".*[0-9].*");
                boolean spec = passStr.matches(".*[^A-Za-z0-9].*");
                
                if (!len || !upper || !lower || !num || !spec) {
                    passWrap.setError("Weak password");
                    valid = false;
                } else {
                    passWrap.setSuccess("Strong Password");
                }
            }

            if (confirm.length == 0) {
                confirmWrap.setError("Confirm password");
                valid = false;
            } else if (!java.util.Arrays.equals(password, confirm)) {
                confirmWrap.setError("Mismatch");
                valid = false;
            } else {
                confirmWrap.setSuccess("Match");
            }

            if (!valid) {
                return;
            }

            registerBtn.setLoading(true);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    authService.registerAdmin(username, new String(password), fullName, "azure");
                    return null;
                }

                @Override
                protected void done() {
                    registerBtn.setLoading(false);
                    java.util.Arrays.fill(password, '\0');
                    java.util.Arrays.fill(confirm, '\0');
                    try {
                        get();
                        Toast.success(LoginFrame.this, "Administrator registered! Please log in.");
                        
                        // Clear fields
                        regNameField.setText("");
                        regEmailField.setText("");
                        regUserField.setText("");
                        regPassField.setText("");
                        regConfirmField.setText("");
                        nameWrap.clear();
                        emailWrap.clear();
                        userWrap.clear();
                        passWrap.clear();
                        confirmWrap.clear();
                        reqPanel.validatePassword("");

                        animateCardTransition("login");
                    } catch (Exception ex) {
                        Toast.error(LoginFrame.this, "Registration failed: " + ex.getCause().getMessage());
                    }
                }
            }.execute();
        });

        // Initialize state
        boolean hasAdmins = false;
        try {
            hasAdmins = authService.hasAdmins();
        } catch (Exception ignored) {}

        java.awt.CardLayout cl = (java.awt.CardLayout) cardsContainer.getLayout();
        if (hasAdmins) {
            cl.show(cardsContainer, "login");
            tryRememberMeOnLoad();
        } else {
            cl.show(cardsContainer, "register");
            setTitle("Grand Azure — Create Admin");
        }
    }

    private JLabel label(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.fontMedium(11));
        lbl.setForeground(Theme.GOLD);
        return lbl;
    }

    private void animateCardTransition(String cardName) {
        java.awt.CardLayout cl = (java.awt.CardLayout) cardsContainer.getLayout();
        
        FadePanel activePanel = null;
        for (Component c : cardsContainer.getComponents()) {
            if (c.isVisible() && c instanceof FadePanel) {
                activePanel = (FadePanel) c;
                break;
            }
        }

        if (activePanel == null) {
            cl.show(cardsContainer, cardName);
            if ("register".equals(cardName)) {
                setTitle("Grand Azure — Create Admin");
            } else {
                setTitle("Grand Azure — Login");
            }
            return;
        }

        final FadePanel outPanel = activePanel;
        Timer fadeOut = new Timer(15, null);
        fadeOut.addActionListener(e -> {
            float a = outPanel.getAlpha() - 0.15f;
            if (a <= 0.0f) {
                fadeOut.stop();
                outPanel.setAlpha(0.0f);
                
                cl.show(cardsContainer, cardName);
                if ("register".equals(cardName)) {
                    setTitle("Grand Azure — Create Admin");
                } else {
                    setTitle("Grand Azure — Login");
                }
                
                FadePanel inPanel = null;
                for (Component c : cardsContainer.getComponents()) {
                    if (c.isVisible() && c instanceof FadePanel) {
                        inPanel = (FadePanel) c;
                        break;
                    }
                }
                
                if (inPanel != null) {
                    inPanel.setAlpha(0.0f);
                    final FadePanel finalInPanel = inPanel;
                    Timer fadeIn = new Timer(15, null);
                    fadeIn.addActionListener(ev -> {
                        float aIn = finalInPanel.getAlpha() + 0.15f;
                        if (aIn >= 1.0f) {
                            fadeIn.stop();
                            finalInPanel.setAlpha(1.0f);
                        } else {
                            finalInPanel.setAlpha(aIn);
                        }
                    });
                    fadeIn.start();
                }
            } else {
                outPanel.setAlpha(a);
            }
        });
        fadeOut.start();
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
                }
            }
        }.execute();
    }

    private void doLogin(ValidationField userWrap, ValidationField passWrap) {
        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        userWrap.clear();
        passWrap.clear();

        if (username.isEmpty() || password.length == 0) {
            if (username.isEmpty()) userWrap.setError("Username is required");
            if (password.length == 0) passWrap.setError("Password is required");
            return;
        }

        loginButton.setLoading(true);
        new SwingWorker<Admin, Void>() {
            @Override
            protected Admin doInBackground() throws Exception {
                return authService.login(username, new String(password));
            }

            @Override
            protected void done() {
                loginButton.setLoading(false);
                java.util.Arrays.fill(password, '\0');
                try {
                    Admin admin = get();
                    if (admin == null) {
                        passWrap.setError("Invalid username or password");
                        return;
                    }
                    if (rememberCheck.isSelected()) {
                        authService.enableRememberMe(admin.getAdminId());
                    }
                    openMain();
                } catch (Exception ex) {
                    passWrap.setError("Login failed: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void openMain() {
        dispose();
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }

    // --- Inner Helper UI Classes ---

    enum IconType { USER, LOCK, EMAIL, EYE_OPEN, EYE_CLOSED, CHECK }

    static void paintIcon(Graphics2D g2, IconType type, int x, int y, int size, Color color) {
        g2 = (Graphics2D) g2.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(color);
        g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        
        switch (type) {
            case USER:
                g2.drawOval(x + size / 4, y + size / 8, size / 2, size / 2);
                g2.drawArc(x + size / 8, y + size / 2 + 1, size * 3 / 4, size / 2, 0, 180);
                break;
            case LOCK:
                g2.drawArc(x + size / 4 + 1, y + size / 8, size / 2 - 2, size / 2, 0, 180);
                g2.drawLine(x + size / 4 + 1, y + size / 8 + size / 4, x + size / 4 + 1, y + size / 2);
                g2.drawLine(x + size * 3 / 4 - 1, y + size / 8 + size / 4, x + size * 3 / 4 - 1, y + size / 2);
                g2.drawRoundRect(x + size / 8, y + size / 2 - 1, size * 3 / 4, size / 2, 4, 4);
                g2.fillOval(x + size / 2 - 2, y + size * 5 / 8, 4, 4);
                break;
            case EMAIL:
                g2.drawRoundRect(x + size / 8, y + size / 4, size * 3 / 4, size / 2, 4, 4);
                int[] xs = { x + size / 8, x + size / 2, x + size * 7 / 8 };
                int[] ys = { y + size / 4, y + size / 2 + 1, y + size / 4 };
                g2.drawPolyline(xs, ys, 3);
                break;
            case EYE_OPEN:
                g2.drawArc(x + size / 8, y + size / 4, size * 3 / 4, size / 2, -150, 120);
                g2.drawArc(x + size / 8, y + size / 4 + 2, size * 3 / 4, size / 2, 30, 120);
                g2.fillOval(x + size / 2 - 3, y + size / 2 - 1, 6, 6);
                break;
            case EYE_CLOSED:
                g2.drawArc(x + size / 8, y + size / 4, size * 3 / 4, size / 2, -150, 120);
                g2.drawLine(x + size / 2, y + size / 2, x + size / 2, y + size * 3 / 4);
                g2.drawLine(x + size / 4, y + size / 2 - 2, x + size / 4 - 2, y + size * 5 / 8 + 2);
                g2.drawLine(x + size * 3 / 4, y + size / 2 - 2, x + size * 3 / 4 + 2, y + size * 5 / 8 + 2);
                break;
            case CHECK:
                g2.drawLine(x + size / 8, y + size / 2, x + size * 3 / 8, y + size * 3 / 4);
                g2.drawLine(x + size * 3 / 8, y + size * 3 / 4, x + size * 7 / 8, y + size / 4);
                break;
        }
        g2.dispose();
    }

    static class FadePanel extends JPanel {
        private float alpha = 1.0f;

        public FadePanel(java.awt.LayoutManager layout) {
            super(layout);
            setOpaque(false);
        }

        public void setAlpha(float alpha) {
            this.alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            repaint();
        }

        public float getAlpha() {
            return this.alpha;
        }

        @Override
        public void paint(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            super.paint(g2);
            g2.dispose();
        }
    }

    static class PremiumTextField extends JTextField {
        private final IconType iconType;
        private final String placeholder;

        public PremiumTextField(IconType iconType, String placeholder) {
            super(20);
            this.iconType = iconType;
            this.placeholder = placeholder;
            setFont(Theme.fontRegular(13));
            setForeground(Theme.textPrimary());
            setCaretColor(Theme.ROYAL_BLUE);
            setOpaque(false);
            
            int leftMargin = iconType != null ? 36 : 12;
            setBorder(new EmptyBorder(10, leftMargin, 10, 12));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.inputBg());
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

            if (iconType != null) {
                paintIcon(g2, iconType, 12, (getHeight() - 16) / 2, 16, isFocusOwner() ? Theme.ROYAL_BLUE : Theme.textSecondary());
            }

            if (getText().isEmpty() && placeholder != null) {
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.fontRegular(13));
                int xOffset = iconType != null ? 36 : 12;
                g2.drawString(placeholder, xOffset, (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2);
            }

            super.paintComponent(g);

            if (isFocusOwner()) {
                g2.setColor(new Color(30, 58, 138, 40));
                g2.setStroke(new java.awt.BasicStroke(4.0f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);

                g2.setColor(Theme.ROYAL_BLUE);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
            } else {
                g2.setColor(Theme.border());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
            }
            g2.dispose();
        }
    }

    static class PremiumPasswordField extends JPasswordField {
        private final String placeholder;
        private boolean showPassword = false;

        public PremiumPasswordField(String placeholder) {
            this.placeholder = placeholder;
            setFont(Theme.fontRegular(13));
            setForeground(Theme.textPrimary());
            setCaretColor(Theme.ROYAL_BLUE);
            setOpaque(false);
            
            setBorder(new EmptyBorder(10, 36, 10, 36));

            addFocusListener(new FocusAdapter() {
                @Override public void focusGained(FocusEvent e) { repaint(); }
                @Override public void focusLost(FocusEvent e) { repaint(); }
            });

            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    int x = e.getX();
                    if (x >= getWidth() - 32 && x <= getWidth() - 12) {
                        showPassword = !showPassword;
                        setEchoChar(showPassword ? (char) 0 : '•');
                        repaint();
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.inputBg());
            g2.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);

            paintIcon(g2, IconType.LOCK, 12, (getHeight() - 16) / 2, 16, isFocusOwner() ? Theme.ROYAL_BLUE : Theme.textSecondary());

            IconType eyeIcon = showPassword ? IconType.EYE_OPEN : IconType.EYE_CLOSED;
            paintIcon(g2, eyeIcon, getWidth() - 28, (getHeight() - 16) / 2, 16, Theme.textSecondary());

            if (getPassword().length == 0 && placeholder != null) {
                g2.setColor(Theme.textMuted());
                g2.setFont(Theme.fontRegular(13));
                g2.drawString(placeholder, 36, (getHeight() + g2.getFontMetrics().getAscent()) / 2 - 2);
            }

            super.paintComponent(g);

            if (isFocusOwner()) {
                g2.setColor(new Color(30, 58, 138, 40));
                g2.setStroke(new java.awt.BasicStroke(4.0f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);

                g2.setColor(Theme.ROYAL_BLUE);
                g2.setStroke(new java.awt.BasicStroke(1.5f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 8, 8);
            } else {
                g2.setColor(Theme.border());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 8, 8);
            }
            g2.dispose();
        }
    }

    static class PremiumCheckBox extends JCheckBox {
        public PremiumCheckBox(String text) {
            super(text);
            setFont(Theme.fontRegular(12));
            setForeground(Theme.textSecondary());
            setOpaque(false);
            setFocusPainted(false);
            
            setIcon(new javax.swing.Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.inputBg());
                    g2.fillRoundRect(x, y, 16, 16, 4, 4);
                    g2.setColor(Theme.border());
                    g2.drawRoundRect(x, y, 16, 16, 4, 4);
                    g2.dispose();
                }
                @Override public int getIconWidth() { return 16; }
                @Override public int getIconHeight() { return 16; }
            });

            setSelectedIcon(new javax.swing.Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(Theme.ROYAL_BLUE);
                    g2.fillRoundRect(x, y, 16, 16, 4, 4);
                    LoginFrame.paintIcon(g2, IconType.CHECK, x + 2, y + 2, 12, Color.WHITE);
                    g2.dispose();
                }
                @Override public int getIconWidth() { return 16; }
                @Override public int getIconHeight() { return 16; }
            });
        }
    }

    static class ValidationField extends JPanel {
        final JTextField field;
        final JLabel errorLabel;

        public ValidationField(JTextField field) {
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setOpaque(false);
            this.field = field;
            this.errorLabel = new JLabel(" ");
            this.errorLabel.setFont(Theme.fontRegular(10.5f));
            
            add(field);
            add(Box.createVerticalStrut(1));
            add(errorLabel);
        }

        public void setError(String msg) {
            errorLabel.setText(msg == null || msg.isBlank() ? " " : msg);
            errorLabel.setForeground(new Color(0xDC, 0x26, 0x26));
        }

        public void setSuccess(String msg) {
            errorLabel.setText(msg == null || msg.isBlank() ? " " : msg);
            errorLabel.setForeground(new Color(0x05, 0x96, 0x69));
        }

        public void clear() {
            errorLabel.setText(" ");
        }
    }

    static class StyledGradientButton extends JPanel {
        private final JLabel textLabel = new JLabel();
        private boolean loading = false;
        private boolean hovered = false;
        private boolean pressed = false;

        private final java.util.List<java.awt.event.ActionListener> actionListeners = new java.util.ArrayList<>();

        public StyledGradientButton(String text) {
            setLayout(new BorderLayout());
            setOpaque(false);
            
            textLabel.setText(text);
            textLabel.setFont(Theme.fontMedium(13));
            textLabel.setForeground(Color.WHITE);
            textLabel.setHorizontalAlignment(JLabel.CENTER);
            add(textLabel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!loading) {
                        hovered = true;
                        repaint();
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!loading) {
                        hovered = false;
                        pressed = false;
                        repaint();
                    }
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    if (!loading && SwingUtilities.isLeftMouseButton(e)) {
                        pressed = true;
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (!loading && SwingUtilities.isLeftMouseButton(e)) {
                        if (pressed) {
                            pressed = false;
                            repaint();
                            java.awt.event.ActionEvent ae = new java.awt.event.ActionEvent(
                                    StyledGradientButton.this, java.awt.event.ActionEvent.ACTION_PERFORMED, ""
                            );
                            for (java.awt.event.ActionListener al : actionListeners) {
                                al.actionPerformed(ae);
                            }
                        }
                    }
                }
            });
        }

        public void addActionListener(java.awt.event.ActionListener al) {
            actionListeners.add(al);
        }

        public void setLoading(boolean loading) {
            this.loading = loading;
            if (loading) {
                textLabel.setText("Processing...");
                hovered = false;
                pressed = false;
            } else {
                textLabel.setText(textLabel.getText().replace("Processing...", ""));
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color cStart = Theme.ROYAL_BLUE;
            Color cEnd = new Color(0x25, 0x63, 0xEB);
            
            if (pressed) {
                cStart = Theme.ROYAL_BLUE.darker();
                cEnd = new Color(0x25, 0x63, 0xEB).darker();
            } else if (hovered) {
                cStart = new Color(0x25, 0x63, 0xEB);
                cEnd = new Color(0x3B, 0x82, 0xF6);
            }

            java.awt.GradientPaint gp = new java.awt.GradientPaint(
                    0, 0, cStart, getWidth(), 0, cEnd
            );
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);

            g2.setColor(new Color(255, 255, 255, 30));
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);

            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class PasswordRequirementsPanel extends JPanel {
        final RequirementItem lengthItem = new RequirementItem("Minimum 8 characters");
        final RequirementItem upperItem = new RequirementItem("Contains uppercase letter");
        final RequirementItem lowerItem = new RequirementItem("Contains lowercase letter");
        final RequirementItem numberItem = new RequirementItem("Contains number");
        final RequirementItem specialItem = new RequirementItem("Contains special character");

        final JProgressBar strengthBar = new JProgressBar(0, 100);
        final JLabel strengthLabel = new JLabel("Password Strength: None");

        public PasswordRequirementsPanel() {
            setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.Y_AXIS));
            setOpaque(false);

            strengthBar.setPreferredSize(new Dimension(0, 5));
            strengthBar.setValue(0);
            strengthBar.setForeground(Theme.DANGER);
            strengthBar.setBackground(new Color(0x1E, 0x29, 0x3B));
            strengthBar.setBorderPainted(false);

            strengthLabel.setFont(Theme.fontMedium(11));
            strengthLabel.setForeground(Theme.textSecondary());

            JPanel barPanel = new JPanel(new BorderLayout(8, 0));
            barPanel.setOpaque(false);
            barPanel.add(strengthBar, BorderLayout.CENTER);
            barPanel.add(strengthLabel, BorderLayout.EAST);

            add(barPanel);
            add(Box.createVerticalStrut(4));
            
            JPanel checklistGrid = new JPanel(new java.awt.GridLayout(3, 2, 2, 2));
            checklistGrid.setOpaque(false);
            checklistGrid.add(lengthItem);
            checklistGrid.add(upperItem);
            checklistGrid.add(lowerItem);
            checklistGrid.add(numberItem);
            checklistGrid.add(specialItem);

            add(checklistGrid);
        }

        public void validatePassword(String pass) {
            boolean len = pass.length() >= 8;
            boolean upper = pass.matches(".*[A-Z].*");
            boolean lower = pass.matches(".*[a-z].*");
            boolean num = pass.matches(".*[0-9].*");
            boolean spec = pass.matches(".*[^A-Za-z0-9].*");

            lengthItem.setChecked(len);
            upperItem.setChecked(upper);
            lowerItem.setChecked(lower);
            numberItem.setChecked(num);
            specialItem.setChecked(spec);

            int score = 0;
            if (len) score += 20;
            if (upper) score += 20;
            if (lower) score += 20;
            if (num) score += 20;
            if (spec) score += 20;

            strengthBar.setValue(score);
            if (pass.isEmpty()) {
                strengthBar.setValue(0);
                strengthLabel.setText("Password Strength: None");
                strengthLabel.setForeground(Theme.textSecondary());
            } else if (score <= 40) {
                strengthBar.setForeground(Theme.DANGER);
                strengthLabel.setText("Password Strength: Weak");
                strengthLabel.setForeground(Theme.DANGER);
            } else if (score <= 80) {
                strengthBar.setForeground(Theme.GOLD);
                strengthLabel.setText("Password Strength: Medium");
                strengthLabel.setForeground(Theme.GOLD);
            } else {
                strengthBar.setForeground(Theme.EMERALD);
                strengthLabel.setText("Password Strength: Strong");
                strengthLabel.setForeground(Theme.EMERALD);
            }
        }
    }

    static class RequirementItem extends JPanel {
        final JLabel iconLabel = new JLabel("✘");
        final JLabel textLabel;

        public RequirementItem(String text) {
            setLayout(new FlowLayout(FlowLayout.LEFT, 4, 0));
            setOpaque(false);
            
            iconLabel.setFont(Theme.fontBold(10));
            iconLabel.setForeground(Theme.DANGER);

            textLabel = new JLabel(text);
            textLabel.setFont(Theme.fontRegular(10));
            textLabel.setForeground(Theme.textSecondary());

            add(iconLabel);
            add(textLabel);
        }

        public void setChecked(boolean checked) {
            if (checked) {
                iconLabel.setText("✔");
                iconLabel.setForeground(Theme.EMERALD);
                textLabel.setForeground(Theme.textPrimary());
            } else {
                iconLabel.setText("✘");
                iconLabel.setForeground(Theme.DANGER);
                textLabel.setForeground(Theme.textSecondary());
            }
        }
    }
}
