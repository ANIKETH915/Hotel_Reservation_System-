package ui.dialog;

import components.StatusBadge;
import components.Theme;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import model.Room;
import utils.CurrencyUtil;
import utils.ImageUtil;

public class RoomDetailDialog extends JDialog {

    public RoomDetailDialog(java.awt.Window owner, Room room) {
        super(owner, "Room " + room.getRoomNumber(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.setOpaque(false);
        ImageIcon icon = ImageUtil.loadScaled(room.getImagePath(), 200, 140);
        JLabel imageLabel = icon != null ? new JLabel(icon) : new JLabel("No image");
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        JPanel details = new JPanel(new GridLayout(0, 2, 12, 8));
        details.setOpaque(false);
        addDetail(details, "Room Number", room.getRoomNumber());
        addDetail(details, "Type", room.getRoomType().getLabel());
        addDetail(details, "Floor", String.valueOf(room.getFloor()));
        addDetail(details, "Price/Night", CurrencyUtil.format(room.getPrice()));
        addDetail(details, "Capacity", String.valueOf(room.getCapacity()) + " guests");

        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusRow.setOpaque(false);
        statusRow.add(new JLabel("Status: "));
        StatusBadge badge = new StatusBadge(room.getStatus().getLabel());
        statusRow.add(badge);
        details.add(new JLabel());
        details.add(statusRow);

        root.add(imagePanel, BorderLayout.NORTH);
        root.add(details, BorderLayout.CENTER);
        setContentPane(root);
        pack();
        setMinimumSize(new java.awt.Dimension(480, 420));
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
