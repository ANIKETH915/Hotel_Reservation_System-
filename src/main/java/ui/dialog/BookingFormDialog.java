package ui.dialog;

import components.DateChooser;
import components.StyledButton;
import components.StyledComboBox;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.time.LocalDate;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import model.Customer;
import model.Room;
import service.BookingService;
import service.CustomerService;
import service.RoomService;
import utils.CurrencyUtil;
import utils.DateUtil;

public class BookingFormDialog extends JDialog {

    private final BookingService bookingService = new BookingService();
    private final CustomerService customerService = new CustomerService();
    private final RoomService roomService = new RoomService();
    private final Runnable onSaved;

    private final StyledComboBox<Customer> customerCombo = new StyledComboBox<>();
    private final StyledComboBox<Room> roomCombo = new StyledComboBox<>();
    private final DateChooser checkInChooser = new DateChooser(LocalDate.now().plusDays(1));
    private final DateChooser checkOutChooser = new DateChooser(LocalDate.now().plusDays(2));
    private final JLabel summaryLabel = new JLabel(" ");

    public BookingFormDialog(java.awt.Window owner, Runnable onSaved) {
        super(owner, "New Booking", ModalityType.APPLICATION_MODAL);
        this.onSaved = onSaved;

        buildUi();
        loadCustomers();
        loadAvailableRooms();

        checkInChooser.setOnChange(d -> {
            if (!checkOutChooser.getSelectedDate().isAfter(d)) {
                checkOutChooser.setSelectedDate(d.plusDays(1));
            }
            loadAvailableRooms();
        });
        checkOutChooser.setOnChange(d -> loadAvailableRooms());
        pack();
        setMinimumSize(new Dimension(500, 560));
        setLocationRelativeTo(owner);
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, UiLayout.SPACE_SM));
        root.setBackground(Theme.bgPrimary());
        root.setBorder(UiLayout.dialogBorder());

        UiLayout.ViewportWidthPanel form = new UiLayout.ViewportWidthPanel();
        form.setLayout(new GridBagLayout());
        form.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addField(form, gbc, 0, "Customer", customerCombo);
        addField(form, gbc, 1, "Check-in Date", checkInChooser);
        addField(form, gbc, 2, "Check-out Date", checkOutChooser);
        addField(form, gbc, 3, "Available Room", roomCombo);

        summaryLabel.setFont(Theme.fontMedium(13));
        summaryLabel.setForeground(Theme.ROYAL_BLUE);
        gbc.gridy = 8;
        gbc.insets = new java.awt.Insets(0, 0, UiLayout.SPACE_MD, 0);
        form.add(summaryLabel, gbc);

        roomCombo.addActionListener(e -> updateSummary());

        StyledButton saveBtn = new StyledButton("Create Booking");
        saveBtn.setPreferredSize(new Dimension(0, 40));
        gbc.gridy = 9;
        form.add(saveBtn, gbc);
        saveBtn.addActionListener(e -> save());

        JScrollPane scroll = UiLayout.pageScroll(form);
        root.add(scroll, BorderLayout.CENTER);
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

    private void loadCustomers() {
        new SwingWorker<List<Customer>, Void>() {
            @Override
            protected List<Customer> doInBackground() throws Exception {
                return customerService.list();
            }

            @Override
            protected void done() {
                try {
                    customerCombo.setModel(new DefaultComboBoxModel<>(get().toArray(new Customer[0])));
                } catch (Exception ex) {
                    Toast.error(BookingFormDialog.this, "Failed to load customers");
                }
            }
        }.execute();
    }

    private void loadAvailableRooms() {
        LocalDate in = checkInChooser.getSelectedDate();
        LocalDate out = checkOutChooser.getSelectedDate();
        new SwingWorker<List<Room>, Void>() {
            @Override
            protected List<Room> doInBackground() throws Exception {
                return roomService.listAvailable(in, out);
            }

            @Override
            protected void done() {
                try {
                    List<Room> rooms = get();
                    roomCombo.setModel(new DefaultComboBoxModel<>(rooms.toArray(new Room[0])));
                    updateSummary();
                    if (rooms.isEmpty()) {
                        summaryLabel.setText("No rooms available for selected dates");
                    }
                } catch (Exception ex) {
                    Toast.error(BookingFormDialog.this, "Failed to load rooms: " + ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateSummary() {
        Room room = (Room) roomCombo.getSelectedItem();
        LocalDate in = checkInChooser.getSelectedDate();
        LocalDate out = checkOutChooser.getSelectedDate();
        if (room == null || in == null || out == null || !out.isAfter(in)) {
            summaryLabel.setText(" ");
            return;
        }
        int nights = DateUtil.nightsBetween(in, out);
        var total = room.getPrice().multiply(java.math.BigDecimal.valueOf(nights));
        summaryLabel.setText(String.format("%d night(s) — Total: %s", nights, CurrencyUtil.format(total)));
    }

    private void save() {
        Customer customer = (Customer) customerCombo.getSelectedItem();
        Room room = (Room) roomCombo.getSelectedItem();
        if (customer == null || room == null) {
            Toast.error(this, "Select customer and room");
            return;
        }

        LocalDate in = checkInChooser.getSelectedDate();
        LocalDate out = checkOutChooser.getSelectedDate();

        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return bookingService.createBooking(customer.getCustomerId(), room.getRoomId(), in, out);
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(BookingFormDialog.this, "Booking created successfully");
                    dispose();
                    if (onSaved != null) {
                        onSaved.run();
                    }
                } catch (Exception ex) {
                    Toast.error(BookingFormDialog.this, ex.getCause() != null
                            ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }
}
