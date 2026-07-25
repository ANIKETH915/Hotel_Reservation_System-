package ui.dialog;

import components.ModernTextField;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Path;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
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
        setMinimumSize(new Dimension(460, 500));
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
        gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);

        int row = 0;
        addField(form, gbc, row++, "Room Number", numberField);
        addField(form, gbc, row++, "Room Type", typeCombo);
        addField(form, gbc, row++, "Floor", floorField);
        addField(form, gbc, row++, "Price per Night", priceField);
        addField(form, gbc, row++, "Capacity", capacityField);
        addField(form, gbc, row++, "Status", statusCombo);

        imageLabel.setFont(Theme.fontRegular(12));
        imageLabel.setForeground(Theme.textSecondary());
        addField(form, gbc, row++, "Image", imageLabel);

        StyledButton browseBtn = new StyledButton("Browse Image", StyledButton.Style.SECONDARY);
        browseBtn.addActionListener(e -> chooseImage());
        gbc.gridy = row++;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_SM, 0);
        form.add(browseBtn, gbc);

        StyledButton saveBtn = new StyledButton(existing == null ? "Add Room" : "Save Changes");
        saveBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = row;
        gbc.insets = new java.awt.Insets(UiLayout.SPACE_SM, 0, 0, 0);
        form.add(saveBtn, gbc);
        saveBtn.addActionListener(e -> save());

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridy = row * 2;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_XS, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        form.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
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
        try {
            Room room = existing != null ? existing : new Room();
            room.setRoomNumber(numberField.getText().trim());
            room.setRoomType((RoomType) typeCombo.getSelectedItem());
            room.setFloor(Integer.parseInt(floorField.getText().trim()));
            room.setPrice(new BigDecimal(priceField.getText().trim()));
            room.setCapacity(Integer.parseInt(capacityField.getText().trim()));
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
        } catch (NumberFormatException ex) {
            Toast.error(this, "Invalid numeric value");
        }
    }
}
