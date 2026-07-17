package ui;

import components.ModernTextField;
import components.StyledButton;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import service.AuthService;

public class ForgotPasswordDialog extends JDialog {

    private final AuthService authService = new AuthService();
    private final ModernTextField usernameField = new ModernTextField(18);
    private final ModernTextField answerField = new ModernTextField(18);
    private final ModernTextField.Password newPasswordField = new ModernTextField.Password();

    public ForgotPasswordDialog(java.awt.Frame owner) {
        super(owner, "Reset Password", true);
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.bgPrimary());
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Forgot Password");
        title.setFont(Theme.fontBold(18));
        title.setForeground(Theme.textPrimary());

        JLabel hint = new JLabel("<html>Security question: What is our hotel inspiration word?</html>");
        hint.setFont(Theme.fontRegular(12));
        hint.setForeground(Theme.textSecondary());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(fieldLabel("Username"), gbc);

        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);
        form.add(usernameField, gbc);

        gbc.gridy = 2;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(fieldLabel("Security Answer"), gbc);

        gbc.gridy = 3;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);
        form.add(answerField, gbc);

        gbc.gridy = 4;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        form.add(fieldLabel("New Password"), gbc);

        gbc.gridy = 5;
        gbc.insets = new java.awt.Insets(0, 0, 16, 0);
        form.add(newPasswordField, gbc);

        StyledButton resetBtn = new StyledButton("Reset Password");
        resetBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 6;
        gbc.insets = new java.awt.Insets(0, 0, 0, 0);
        form.add(resetBtn, gbc);

        resetBtn.addActionListener(e -> doReset());

        JPanel top = new JPanel(new BorderLayout(0, 8));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(hint, BorderLayout.SOUTH);

        root.add(top, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setMinimumSize(new Dimension(420, 340));
        setLocationRelativeTo(owner);
    }

    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        return lbl;
    }

    private void doReset() {
        String username = usernameField.getText().trim();
        String answer = answerField.getText().trim();
        char[] newPass = newPasswordField.getPassword();

        if (username.isEmpty() || answer.isEmpty() || newPass.length == 0) {
            Toast.error(this, "Please fill all fields");
            return;
        }

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return authService.resetPassword(username, answer, new String(newPass));
            }

            @Override
            protected void done() {
                java.util.Arrays.fill(newPass, '\0');
                try {
                    if (Boolean.TRUE.equals(get())) {
                        Toast.success(ForgotPasswordDialog.this, "Password reset successfully");
                        dispose();
                    } else {
                        Toast.error(ForgotPasswordDialog.this,
                                "Reset failed. Check the username and security answer.");
                    }
                } catch (Exception ex) {
                    Toast.error(ForgotPasswordDialog.this, "Reset failed: " + ex.getMessage());
                }
            }
        }.execute();
    }
}
