package ui.dialog;

import components.StatusBadge;
import components.Theme;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import model.Room;
import utils.CurrencyUtil;
import utils.ImageUtil;

public class RoomDetailDialog extends JDialog {

    public RoomDetailDialog(java.awt.Window owner, Room room) {
        super(owner, "Room " + room.getRoomNumber(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(UiLayout.SPACE_MD, UiLayout.SPACE_MD));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        // Redesigned Image Header panel
        JPanel imageWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(java.awt.Graphics g) {
                java.awt.Graphics2D g2 = (java.awt.Graphics2D) g.create();
                g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.bgCard());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(Theme.border());
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        imageWrapper.setOpaque(false);
        imageWrapper.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        ImageIcon icon = ImageUtil.loadScaled(room.getImagePath(), 240, 160);
        JLabel imageLabel;
        if (icon != null) {
            imageLabel = new JLabel(icon);
        } else {
            imageLabel = new JLabel("No Room Image Configured");
            imageLabel.setFont(Theme.fontMedium(12));
            imageLabel.setForeground(Theme.textMuted());
            imageLabel.setPreferredSize(new java.awt.Dimension(240, 160));
        }
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imageWrapper.add(imageLabel, BorderLayout.CENTER);

        // Details Panel
        JPanel details = new JPanel(new GridLayout(0, 2, UiLayout.SPACE_MD, UiLayout.SPACE_SM));
        details.setOpaque(false);
        details.setBorder(BorderFactory.createEmptyBorder(UiLayout.SPACE_XS, UiLayout.SPACE_XS, UiLayout.SPACE_XS, UiLayout.SPACE_XS));

        addDetail(details, "Room Number", room.getRoomNumber());
        addDetail(details, "Type", room.getRoomType().getLabel());
        addDetail(details, "Floor", String.valueOf(room.getFloor()));
        addDetail(details, "Price/Night", CurrencyUtil.format(room.getPrice()));
        addDetail(details, "Capacity", String.valueOf(room.getCapacity()) + " guests");

        // Status alignment
        JLabel statusLabel = new JLabel("Housekeeping");
        statusLabel.setFont(Theme.fontMedium(12));
        statusLabel.setForeground(Theme.textSecondary());
        details.add(statusLabel);

        JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        badgeWrapper.setOpaque(false);
        StatusBadge badge = new StatusBadge(room.getStatus().getLabel());
        badgeWrapper.add(badge);
        details.add(badgeWrapper);

        root.add(imageWrapper, BorderLayout.NORTH);
        root.add(details, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setMinimumSize(new java.awt.Dimension(480, 440));
        setLocationRelativeTo(owner);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        JLabel val = new JLabel(value);
        val.setFont(Theme.fontRegular(13));
        val.setForeground(Theme.textPrimary());
        panel.add(lbl);
        panel.add(val);
    }
}
