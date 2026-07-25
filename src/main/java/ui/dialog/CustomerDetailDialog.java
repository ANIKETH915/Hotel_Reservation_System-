package ui.dialog;

import components.ModernTable;
import components.Theme;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Booking;
import model.Customer;
import service.BookingService;
import utils.CurrencyUtil;
import utils.DateUtil;

public class CustomerDetailDialog extends JDialog {

    private final BookingService bookingService = new BookingService();
    private final DefaultTableModel historyModel = new DefaultTableModel(
            new String[]{"ID", "Room", "Check-in", "Check-out", "Amount", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    public CustomerDetailDialog(java.awt.Window owner, Customer customer) {
        super(owner, "Customer — " + customer.getFullName(), ModalityType.APPLICATION_MODAL);
        JPanel root = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        JPanel info = new JPanel(new GridLayout(0, 2, UiLayout.SPACE_MD, UiLayout.SPACE_SM));
        info.setOpaque(false);
        addInfo(info, "Full Name", customer.getFullName());
        addInfo(info, "Email", customer.getEmail());
        addInfo(info, "Phone", customer.getPhone());
        addInfo(info, "Address", customer.getAddress() != null ? customer.getAddress() : "-");
        addInfo(info, "ID Proof", customer.getIdProof() != null ? customer.getIdProof() : "-");
        addInfo(info, "Member Since", customer.getCreatedAt() != null
                ? DateUtil.format(customer.getCreatedAt()) : "-");

        ModernTable table = new ModernTable(historyModel);
        JScrollPane scroll = UiLayout.tableScroll(table);

        JLabel historyTitle = new JLabel("Booking History");
        historyTitle.setFont(Theme.fontMedium(14));
        historyTitle.setForeground(Theme.textPrimary());

        root.add(info, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, UiLayout.SPACE_SM));
        center.setOpaque(false);
        center.add(historyTitle, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);
        root.add(center, BorderLayout.CENTER);

        setContentPane(root);
        pack();
        setMinimumSize(new java.awt.Dimension(640, 480));
        setLocationRelativeTo(owner);
        loadHistory(customer.getCustomerId());
    }

    private void addInfo(JPanel panel, String label, String value) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(Theme.fontMedium(12));
        lbl.setForeground(Theme.textSecondary());
        JLabel val = new JLabel(value);
        val.setFont(Theme.fontRegular(13));
        val.setForeground(Theme.textPrimary());
        panel.add(lbl);
        panel.add(val);
    }

    private void loadHistory(int customerId) {
        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.listByCustomer(customerId);
            }

            @Override
            protected void done() {
                try {
                    historyModel.setRowCount(0);
                    for (Booking b : get()) {
                        historyModel.addRow(new Object[]{
                                b.getBookingId(),
                                b.getRoomNumber(),
                                DateUtil.format(b.getCheckIn()),
                                DateUtil.format(b.getCheckOut()),
                                CurrencyUtil.format(b.getTotalAmount()),
                                b.getBookingStatus().getLabel()
                        });
                    }
                } catch (Exception ignored) {
                    // empty table
                }
            }
        }.execute();
    }
}
