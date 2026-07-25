package ui.dialog;

import components.ModernTextField;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

        StyledButton payBtn = new StyledButton("Process Payment", StyledButton.Style.GOLD);
        StyledButton cancelBtn = new StyledButton("Cancel", StyledButton.Style.SECONDARY);
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
}
