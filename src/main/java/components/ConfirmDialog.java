package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Window;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public final class ConfirmDialog extends JDialog {
    private boolean confirmed;

    private ConfirmDialog(Window owner, String title, String message, String confirmText, boolean destructive) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(420, 200));

        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.bgCard());
        root.setBorder(new EmptyBorder(24, 28, 20, 28));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontBold(18));
        titleLabel.setForeground(Theme.textPrimary());

        JLabel messageLabel = new JLabel("<html><body style='width:320px'>" + message + "</body></html>");
        messageLabel.setFont(Theme.fontRegular(13));
        messageLabel.setForeground(Theme.textSecondary());
        messageLabel.setVerticalAlignment(SwingConstants.TOP);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        StyledButton cancel = new StyledButton("Cancel", StyledButton.Style.SECONDARY);
        StyledButton confirm = new StyledButton(confirmText == null ? "Confirm" : confirmText,
                destructive ? StyledButton.Style.DANGER : StyledButton.Style.PRIMARY);
        cancel.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        confirm.addActionListener(e -> {
            confirmed = true;
            dispose();
        });
        buttons.add(cancel);
        buttons.add(confirm);

        root.add(titleLabel, BorderLayout.NORTH);
        root.add(messageLabel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
        setLocationRelativeTo(owner);
    }

    public static boolean confirm(Window owner, String title, String message) {
        return confirm(owner, title, message, "Confirm", false);
    }

    public static boolean confirmDelete(Window owner, String message) {
        return confirm(owner, "Confirm Delete", message, "Delete", true);
    }

    public static boolean confirm(Window owner, String title, String message, String confirmText, boolean destructive) {
        ConfirmDialog dialog = new ConfirmDialog(owner, title, message, confirmText, destructive);
        dialog.setVisible(true);
        return dialog.confirmed;
    }

    public static void alert(Window owner, String title, String message) {
        JDialog dialog = new JDialog(owner, title, ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBackground(Theme.bgCard());
        root.setBorder(new EmptyBorder(24, 28, 20, 28));
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontBold(18));
        titleLabel.setForeground(Theme.textPrimary());
        JLabel messageLabel = new JLabel("<html><body style='width:320px'>" + message + "</body></html>");
        messageLabel.setFont(Theme.fontRegular(13));
        messageLabel.setForeground(Theme.textSecondary());
        StyledButton ok = new StyledButton("OK", StyledButton.Style.PRIMARY);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);
        buttons.add(ok);
        ok.addActionListener(e -> dialog.dispose());
        root.add(titleLabel, BorderLayout.NORTH);
        root.add(messageLabel, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }
}
