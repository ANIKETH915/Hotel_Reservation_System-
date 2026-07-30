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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.Room;
import model.RoomStatus;
import model.RoomType;
import service.RoomService;
import utils.ImageUtil;

public class RoomFormDialog extends JDialog {

    private final RoomService roomService = new RoomService();
    private final Room existing;
    private final Runnable onSaved;

    private final ModernTextField numberField = new ModernTextField(16);
    private final StyledComboBox<RoomType> typeCombo = new StyledComboBox<>(RoomType.values());
    private final ModernTextField floorField = new ModernTextField(8);
    private final ModernTextField priceField = new ModernTextField(12);
    private final ModernTextField capacityField = new ModernTextField(8);
    private final StyledComboBox<RoomStatus> statusCombo = new StyledComboBox<>(RoomStatus.values());
    private final JLabel imageLabel = new JLabel("No image selected");
    private String imagePath;

    public RoomFormDialog(java.awt.Window owner, Room existing, Runnable onSaved) {
        super(owner, existing == null ? "Add Room" : "Edit Room", ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        this.onSaved = onSaved;

        buildUi();
        if (existing != null) {
            populate(existing);
        } else {
            statusCombo.setSelectedItem(RoomStatus.AVAILABLE);
        }
        pack();
        setMinimumSize(new Dimension(460, getPreferredSize().height));
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int gridy = 0;
        addField(form, gbc, gridy, "Room Number", numberField);
        gridy += 2;
        addField(form, gbc, gridy, "Room Type", typeCombo);
        gridy += 2;
        addField(form, gbc, gridy, "Floor", floorField);
        gridy += 2;
        addField(form, gbc, gridy, "Price per Night", priceField);
        gridy += 2;
        addField(form, gbc, gridy, "Capacity", capacityField);
        gridy += 2;
        addField(form, gbc, gridy, "Status", statusCombo);
        gridy += 2;

        // Image label
        gbc.gridy = gridy++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_XS, 0);
        JLabel lblImage = new JLabel("Image");
        lblImage.setFont(Theme.fontMedium(12));
        lblImage.setForeground(Theme.textSecondary());
        form.add(lblImage, gbc);

        // Sub-panel for image label and browse button side-by-side
        JPanel imagePanel = new JPanel(new BorderLayout(UiLayout.SPACE_SM, 0));
        imagePanel.setOpaque(false);

        imageLabel.setFont(Theme.fontRegular(12));
        imageLabel.setForeground(Theme.textSecondary());
        imagePanel.add(imageLabel, BorderLayout.CENTER);

        DialogButton browseBtn = new DialogButton("Browse Image", false);
        browseBtn.addActionListener(e -> chooseImage());
        imagePanel.add(browseBtn, BorderLayout.EAST);

        gbc.gridy = gridy++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        form.add(imagePanel, gbc);

        // Buttons section
        DialogButton saveBtn = new DialogButton(existing == null ? "Add Room" : "Save Changes", true);
        saveBtn.setPreferredSize(new Dimension(0, 40));
        saveBtn.addActionListener(e -> save());

        gbc.gridy = gridy++;
        gbc.insets = new java.awt.Insets(UiLayout.SPACE_MD, 0, 0, 0);
        form.add(saveBtn, gbc);

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int gridy, String label, java.awt.Component field) {
        gbc.gridy = gridy;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_XS, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        form.add(lbl, gbc);

        gbc.gridy = gridy + 1;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        form.add(field, gbc);
    }

    private void populate(Room room) {
        numberField.setText(room.getRoomNumber());
        typeCombo.setSelectedItem(room.getRoomType());
        floorField.setText(String.valueOf(room.getFloor()));
        priceField.setText(room.getPrice().toPlainString());
        capacityField.setText(String.valueOf(room.getCapacity()));
        statusCombo.setSelectedItem(room.getStatus());
        imagePath = room.getImagePath();
        if (imagePath != null && !imagePath.isBlank()) {
            imageLabel.setText(new File(imagePath).getName());
        }
    }

    private void chooseImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Images", "jpg", "jpeg", "png", "gif", "webp"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            imageLabel.setText(file.getName());
            imageLabel.putClientProperty("pendingPath", file.toPath());
        }
    }

    private void save() {
        String roomNumber = numberField.getText().trim();
        if (roomNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Room Number must not be empty.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int floor;
        try {
            floor = Integer.parseInt(floorField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Floor must be a valid numeric integer.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal price;
        try {
            price = new BigDecimal(priceField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Price must be a valid numeric decimal.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Price must be greater than zero.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int capacity;
        try {
            capacity = Integer.parseInt(capacityField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid capacity.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (capacity <= 0) {
            JOptionPane.showMessageDialog(this,
                    "Capacity must be greater than zero.",
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Room room = existing != null ? existing : new Room();
            room.setRoomNumber(roomNumber);
            room.setRoomType((RoomType) typeCombo.getSelectedItem());
            room.setFloor(floor);
            room.setPrice(price);
            room.setCapacity(capacity);
            room.setStatus((RoomStatus) statusCombo.getSelectedItem());

            Path pending = (Path) imageLabel.getClientProperty("pendingPath");
            if (pending != null) {
                try {
                    imagePath = ImageUtil.copyImage(pending);
                } catch (java.io.IOException ioe) {
                    Toast.error(this, "Image upload failed: " + ioe.getMessage());
                    return;
                }
            }
            room.setImagePath(imagePath);

            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    if (existing == null) {
                        roomService.add(room);
                    } else {
                        roomService.update(room);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        Toast.success(RoomFormDialog.this, existing == null ? "Room added" : "Room updated");
                        dispose();
                        if (onSaved != null) {
                            onSaved.run();
                        }
                    } catch (Exception ex) {
                        Toast.error(RoomFormDialog.this, ex.getCause() != null
                                ? ex.getCause().getMessage() : ex.getMessage());
                    }
                }
            }.execute();
        } catch (Exception ex) {
            Toast.error(this, "An error occurred: " + ex.getMessage());
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
