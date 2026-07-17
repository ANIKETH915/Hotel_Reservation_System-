package ui.dialog;

import components.ModernTextField;
import components.StyledButton;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
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
        root.setBorder(new EmptyBorder(20, 24, 20, 24));

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

        StyledButton saveBtn = new StyledButton(existing == null ? "Add Customer" : "Save Changes");
        saveBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 10;
        gbc.insets = new java.awt.Insets(8, 0, 0, 0);
        form.add(saveBtn, gbc);
        saveBtn.addActionListener(e -> save());

        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addField(JPanel form, GridBagConstraints gbc, int row, String label, java.awt.Component field) {
        gbc.gridy = row * 2;
        gbc.insets = new java.awt.Insets(0, 0, 4, 0);
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        form.add(lbl, gbc);

        gbc.gridy = row * 2 + 1;
        gbc.insets = new java.awt.Insets(0, 0, 12, 0);
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
}
