package ui.dialog;

import components.StyledButton;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.Booking;
import service.BookingService;
import utils.CurrencyUtil;
import utils.DateUtil;

public class CheckoutDialog extends JDialog {

    private final BookingService bookingService = new BookingService();
    private final Booking booking;
    private final Runnable onComplete;

    public CheckoutDialog(java.awt.Window owner, Booking booking, Runnable onComplete) {
        super(owner, "Check Out", ModalityType.APPLICATION_MODAL);
        this.booking = booking;
        this.onComplete = onComplete;

        JPanel root = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        JPanel details = new JPanel(new GridLayout(0, 2, UiLayout.SPACE_MD, UiLayout.SPACE_SM + 2));
        details.setOpaque(false);
        addDetail(details, "Guest", booking.getCustomerName());
        addDetail(details, "Room", booking.getRoomNumber() + " (" + booking.getRoomType() + ")");
        addDetail(details, "Check-in", DateUtil.format(booking.getCheckIn()));
        addDetail(details, "Check-out", DateUtil.format(booking.getCheckOut()));
        addDetail(details, "Total", CurrencyUtil.format(booking.getTotalAmount()));
        addDetail(details, "Payment", booking.getPaymentStatus().getLabel());
        root.add(details, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiLayout.SPACE_SM, 0));
        buttons.setOpaque(false);

        StyledButton cancelBtn = new StyledButton("Cancel", StyledButton.Style.SECONDARY);
        cancelBtn.addActionListener(e -> dispose());

        StyledButton confirmBtn = new StyledButton("Confirm Check-out", StyledButton.Style.DANGER);
        confirmBtn.addActionListener(e -> doCheckout());

        buttons.add(cancelBtn);
        buttons.add(confirmBtn);
        root.add(buttons, BorderLayout.SOUTH);

        setContentPane(root);
        pack();
        setMinimumSize(new java.awt.Dimension(440, 300));
        setLocationRelativeTo(owner);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel name = new JLabel(label);
        name.setFont(Theme.fontMedium(12));
        name.setForeground(Theme.textSecondary());
        JLabel detail = new JLabel(value == null ? "-" : value);
        detail.setFont(Theme.fontRegular(13));
        detail.setForeground(Theme.textPrimary());
        panel.add(name);
        panel.add(detail);
    }

    private void doCheckout() {
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingService.checkOut(booking.getBookingId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(CheckoutDialog.this, "Guest checked out successfully");
                    dispose();
                    if (onComplete != null) {
                        onComplete.run();
                    }
                } catch (Exception ex) {
                    Toast.error(CheckoutDialog.this, ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }
}
