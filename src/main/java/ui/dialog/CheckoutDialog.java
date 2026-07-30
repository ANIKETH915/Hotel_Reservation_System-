package ui.dialog;

import components.StyledButton;
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
import java.awt.GridLayout;
import java.awt.RenderingHints;
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

        DialogButton cancelBtn = new DialogButton("Cancel", false, false);
        cancelBtn.addActionListener(e -> dispose());

        DialogButton confirmBtn = new DialogButton("Confirm Check-out", true, true);
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

    private static class DialogButton extends StyledButton {
        private boolean hover = false;
        private final boolean primary;
        private final boolean danger;

        public DialogButton(String text, boolean primary, boolean danger) {
            super(text, danger ? Style.DANGER : (primary ? Style.PRIMARY : Style.SECONDARY));
            this.primary = primary;
            this.danger = danger;
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

            if (danger) {
                Color bg = hover ? new Color(0xDC, 0x26, 0x26) : new Color(0xEF, 0x44, 0x44);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(Color.WHITE);
            } else if (primary) {
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
