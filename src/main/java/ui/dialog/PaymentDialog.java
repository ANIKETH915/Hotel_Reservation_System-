package ui.dialog;

import components.ModernTextField;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.math.BigDecimal;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.Booking;
import model.PaymentMethod;
import service.PaymentService;
import utils.CurrencyUtil;

public class PaymentDialog extends JDialog {

    private final PaymentService paymentService = new PaymentService();
    private final Booking booking;
    private final Runnable onComplete;

    private final StyledComboBox<PaymentMethod> methodCombo = new StyledComboBox<>(PaymentMethod.values());
    private final ModernTextField amountField = new ModernTextField(12);

    public PaymentDialog(java.awt.Window owner, Booking booking, Runnable onComplete) {
        super(owner, "Process Payment", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.onComplete = onComplete;

        JPanel root = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        JLabel title = new JLabel("Booking #" + booking.getBookingId() + " — " + booking.getCustomerName());
        title.setFont(Theme.fontMedium(13));
        title.setForeground(Theme.textPrimary());
        gbc.gridy = 0;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_XS, 0);
        form.add(title, gbc);

        JLabel total = new JLabel("Total due: " + CurrencyUtil.format(booking.getTotalAmount()));
        total.setFont(Theme.fontRegular(12));
        total.setForeground(Theme.textSecondary());
        gbc.gridy = 1;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        form.add(total, gbc);

        addField(form, gbc, 2, "Payment Method", methodCombo);
        addField(form, gbc, 4, "Amount", amountField);

        amountField.setText(booking.getTotalAmount().toPlainString());

        DialogButton payBtn = new DialogButton("Process Payment", true);
        DialogButton cancelBtn = new DialogButton("Cancel", false);
        cancelBtn.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiLayout.SPACE_SM, 0));
        buttons.setOpaque(false);
        buttons.add(cancelBtn);
        buttons.add(payBtn);
        payBtn.addActionListener(e -> process());

        root.add(form, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        setContentPane(root);
        getRootPane().setDefaultButton(payBtn);
        pack();
        setMinimumSize(new java.awt.Dimension(420, 300));
        setLocationRelativeTo(owner);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridy = row;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_XS, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        form.add(lbl, gbc);

        gbc.gridy = row + 1;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        form.add(field, gbc);
    }

    private void process() {
        try {
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            PaymentMethod method = (PaymentMethod) methodCombo.getSelectedItem();

            new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return paymentService.processPayment(booking.getBookingId(), method, amount);
                }

                @Override
                protected void done() {
                    try {
                        get();
                        Toast.success(PaymentDialog.this, "Payment processed successfully");
                        dispose();
                        if (onComplete != null) {
                            onComplete.run();
                        }
                    } catch (Exception ex) {
                        Toast.error(PaymentDialog.this, ex.getCause() != null
                                ? ex.getCause().getMessage() : ex.getMessage());
                    }
                }
            }.execute();
        } catch (NumberFormatException ex) {
            Toast.error(this, "Invalid amount");
        }
    }

    private static class DialogButton extends StyledButton {
        private boolean hover = false;
        private final boolean primary;

        public DialogButton(String text, boolean primary) {
            super(text, primary ? Style.PRIMARY : Style.SECONDARY);
            this.primary = primary;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.fontMedium(13));

            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    hover = true;
                    repaint();
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (primary) {
                Color bg = hover ? new Color(0x25, 0x63, 0xEB) : Theme.ROYAL_BLUE;
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
            } else {
                Color bg = hover ? (Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xE5, 0xE7, 0xEB)) : Theme.bgCard();
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Theme.border());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.setColor(Theme.textPrimary());
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int tw = fm.stringWidth(getText());
            int tx = (getWidth() - tw) / 2;
            int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), tx, ty);
            g2.dispose();
        }
    }
}
