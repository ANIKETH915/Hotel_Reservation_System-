package ui.dialog;

import components.ModernTextField;
import components.StyledButton;
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
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import model.Customer;
import service.CustomerService;

public class CustomerFormDialog extends JDialog {

    private final CustomerService customerService = new CustomerService();
    private final Customer existing;
    private final Runnable onSaved;

    private final ModernTextField nameField = new ModernTextField(20);
    private final ModernTextField emailField = new ModernTextField(20);
    private final ModernTextField phoneField = new ModernTextField(16);
    private final ModernTextField addressField = new ModernTextField(20);
    private final ModernTextField idProofField = new ModernTextField(16);

    public CustomerFormDialog(java.awt.Window owner, Customer existing, Runnable onSaved) {
        super(owner, existing == null ? "Add Customer" : "Edit Customer", ModalityType.APPLICATION_MODAL);
        this.existing = existing;
        this.onSaved = onSaved;

        buildUi();
        pack();
        setMinimumSize(new Dimension(440, 460));
        setLocationRelativeTo(owner);
        if (existing != null) {
            populate(existing);
        }
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

        addField(form, gbc, 0, "Full Name", nameField);
        addField(form, gbc, 1, "Email", emailField);
        addField(form, gbc, 2, "Phone", phoneField);
        addField(form, gbc, 3, "Address", addressField);
        addField(form, gbc, 4, "ID Proof", idProofField);

        DialogButton saveBtn = new DialogButton(existing == null ? "Add Customer" : "Save Changes", true);
        saveBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 10;
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

    private void populate(Customer customer) {
        nameField.setText(customer.getFullName());
        emailField.setText(customer.getEmail());
        phoneField.setText(customer.getPhone());
        addressField.setText(customer.getAddress() != null ? customer.getAddress() : "");
        idProofField.setText(customer.getIdProof() != null ? customer.getIdProof() : "");
    }

    private void save() {
        Customer customer = existing != null ? existing : new Customer();
        customer.setFullName(nameField.getText().trim());
        customer.setEmail(emailField.getText().trim());
        customer.setPhone(phoneField.getText().trim());
        customer.setAddress(addressField.getText().trim());
        customer.setIdProof(idProofField.getText().trim());

        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (existing == null) {
                    customerService.add(customer);
                } else {
                    customerService.update(customer);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(CustomerFormDialog.this, existing == null ? "Customer added" : "Customer updated");
                    dispose();
                    if (onSaved != null) {
                        onSaved.run();
                    }
                } catch (Exception ex) {
                    Toast.error(CustomerFormDialog.this, ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
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
