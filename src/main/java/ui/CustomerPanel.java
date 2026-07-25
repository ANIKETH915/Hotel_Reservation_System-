package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StyledButton;
import components.TableCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Customer;
import service.CustomerService;
import ui.dialog.CustomerDetailDialog;
import ui.dialog.CustomerFormDialog;
import utils.DateUtil;

public class CustomerPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final CustomerService customerService = new CustomerService();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Email", "Phone", "Address", "Registered"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay tableOverlay;
    private TableCard tableCard;
    private PageHeader pageHeader;
    private List<Customer> customers = List.of();

    public CustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Guests", "Profiles, contact details, and reservation history");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, UiLayout.SPACE_SM, UiLayout.SPACE_XS));
        toolbar.setOpaque(false);

        StyledButton addBtn = new StyledButton("Add Customer");
        StyledButton editBtn = new StyledButton("Edit", StyledButton.Style.SECONDARY);
        StyledButton deleteBtn = new StyledButton("Delete", StyledButton.Style.DANGER);
        StyledButton viewBtn = new StyledButton("View History", StyledButton.Style.GHOST);

        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(viewBtn);
        pageHeader.addAction(addBtn);

        table = new ModernTable(tableModel);
        EmptyStatePanel emptyState = new EmptyStatePanel(
                "No guest profiles yet",
                "Register a guest before creating their first reservation."
        );
        emptyState.setIconKey("customers");
        emptyState.setAction("Add Guest",
                () -> new CustomerFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        tableOverlay = new TableEmptyOverlay(UiLayout.tableScroll(table), emptyState);
        tableCard = new TableCard(tableOverlay);

        JPanel north = new JPanel(new BorderLayout(0, UiLayout.SPACE_SM));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        addBtn.addActionListener(e -> new CustomerFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.addActionListener(e -> viewSelected());
    }

    private Customer selectedCustomer() {
        int row = table.getSelectedModelRow();
        if (row < 0 || row >= customers.size()) {
            return null;
        }
        return customers.get(row);
    }

    private void editSelected() {
        Customer customer = selectedCustomer();
        if (customer == null) {
            Toast.error(mainFrame, "Select a customer");
            return;
        }
        new CustomerFormDialog(mainFrame, customer, this::afterMutation).setVisible(true);
    }

    private void afterMutation() {
        mainFrame.notifyDataChanged(AppEvents.Domain.CUSTOMERS);
    }

    private void viewSelected() {
        Customer customer = selectedCustomer();
        if (customer == null) {
            Toast.error(mainFrame, "Select a customer");
            return;
        }
        new CustomerDetailDialog(mainFrame, customer).setVisible(true);
    }

    private void deleteSelected() {
        Customer customer = selectedCustomer();
        if (customer == null) {
            Toast.error(mainFrame, "Select a customer");
            return;
        }
        if (!ConfirmDialog.confirmDelete(mainFrame,
                "Delete customer " + customer.getFullName() + "? Related bookings will also be removed.")) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                customerService.delete(customer.getCustomerId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Customer deleted");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                return customerService.list();
            }

            @Override
            protected void done() {
                try {
                    customers = get();
                    tableModel.setRowCount(0);
                    for (Customer c : customers) {
                        tableModel.addRow(new Object[]{
                                c.getCustomerId(),
                                c.getFullName(),
                                c.getEmail(),
                                c.getPhone(),
                                c.getAddress() != null ? c.getAddress() : "-",
                                c.getCreatedAt() != null ? DateUtil.format(c.getCreatedAt()) : "-"
                        });
                    }
                    tableOverlay.updateVisibility();
                    pageHeader.setSubtitle(customers.size() + " guest profiles");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load customers");
                }
            }
        }.execute();
    }

    @Override
    public void applySearch(String query) {
        table.filter(query);
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        table.applyTheme();
        tableOverlay.applyTheme();
        tableCard.applyTheme();
        repaint();
    }
}
