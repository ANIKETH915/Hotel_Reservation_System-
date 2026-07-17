package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StyledButton;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Booking;
import model.BookingStatus;
import reports.InvoicePrinter;
import reports.ReceiptPrinter;
import service.BookingService;
import ui.dialog.BookingFormDialog;
import ui.dialog.CheckoutDialog;
import ui.dialog.PaymentDialog;
import utils.CurrencyUtil;
import utils.DateUtil;

public class BookingPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final BookingService bookingService = new BookingService();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Guest", "Room", "Check-in", "Check-out", "Nights", "Amount", "Booking", "Payment"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay overlay;
    private List<Booking> bookings = List.of();
    private PageHeader pageHeader;

    public BookingPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 16));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Front Desk Bookings", "Reservations, arrivals, departures, and billing");

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        StyledButton newBtn = new StyledButton("New Booking");
        newBtn.setToolTipText("Create a reservation");
        StyledButton checkInBtn = new StyledButton("Check-in", StyledButton.Style.SECONDARY);
        StyledButton checkOutBtn = new StyledButton("Check-out", StyledButton.Style.SECONDARY);
        StyledButton cancelBtn = new StyledButton("Cancel", StyledButton.Style.DANGER);
        StyledButton payBtn = new StyledButton("Record Payment", StyledButton.Style.GOLD);
        StyledButton receiptBtn = new StyledButton("Receipt", StyledButton.Style.GHOST);
        StyledButton invoiceBtn = new StyledButton("Invoice", StyledButton.Style.GHOST);

        toolbar.add(newBtn);
        toolbar.add(checkInBtn);
        toolbar.add(checkOutBtn);
        toolbar.add(cancelBtn);
        toolbar.add(payBtn);
        toolbar.add(receiptBtn);
        toolbar.add(invoiceBtn);
        pageHeader.addAction(newBtn);

        table = new ModernTable(tableModel);
        EmptyStatePanel empty = new EmptyStatePanel("No bookings yet",
                "Create a booking once rooms and guests are in the system.");
        empty.setIconKey("bookings");
        empty.setAction("New Booking", () -> new BookingFormDialog(mainFrame, this::afterMutation).setVisible(true));
        overlay = new TableEmptyOverlay(new JScrollPane(table), empty);

        JPanel north = new JPanel(new BorderLayout(0, 12));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(overlay, BorderLayout.CENTER);

        newBtn.addActionListener(e ->
                new BookingFormDialog(mainFrame, this::afterMutation).setVisible(true));
        checkInBtn.addActionListener(e -> checkInSelected());
        checkOutBtn.addActionListener(e -> checkOutSelected());
        cancelBtn.addActionListener(e -> cancelSelected());
        payBtn.addActionListener(e -> paySelected());
        receiptBtn.addActionListener(e -> {
            Booking booking = selectedBooking();
            if (booking == null) {
                Toast.error(mainFrame, "Select a booking");
                return;
            }
            ReceiptPrinter.printBookingReceipt(mainFrame, booking);
        });
        invoiceBtn.addActionListener(e -> {
            Booking booking = selectedBooking();
            if (booking == null) {
                Toast.error(mainFrame, "Select a booking");
                return;
            }
            InvoicePrinter.printInvoice(mainFrame, booking);
        });
    }

    private void afterMutation() {
        mainFrame.notifyDataChanged(AppEvents.Domain.BOOKINGS);
    }

    private Booking selectedBooking() {
        int row = table.getSelectedModelRow();
        if (row < 0 || row >= bookings.size()) {
            return null;
        }
        return bookings.get(row);
    }

    private void checkInSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (booking.getBookingStatus() != BookingStatus.CONFIRMED) {
            Toast.error(mainFrame, "Only confirmed bookings can be checked in");
            return;
        }
        if (!ConfirmDialog.confirm(mainFrame, "Confirm Check-in",
                "Check in " + booking.getCustomerName() + " to room " + booking.getRoomNumber() + "?",
                "Check-in", false)) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingService.checkIn(booking.getBookingId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Guest checked in");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void checkOutSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (booking.getBookingStatus() != BookingStatus.CHECKED_IN) {
            Toast.error(mainFrame, "Only checked-in bookings can be checked out");
            return;
        }
        new CheckoutDialog(mainFrame, booking, this::afterMutation).setVisible(true);
    }

    private void cancelSelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        if (!ConfirmDialog.confirm(mainFrame, "Cancel Reservation",
                "Cancel reservation for " + booking.getCustomerName() + "? The room will become available.",
                "Cancel Booking", true)) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                bookingService.cancel(booking.getBookingId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Booking cancelled — room freed");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void paySelected() {
        Booking booking = selectedBooking();
        if (booking == null) {
            Toast.error(mainFrame, "Select a booking");
            return;
        }
        new PaymentDialog(mainFrame, booking, () -> {
            mainFrame.notifyDataChanged(AppEvents.Domain.PAYMENTS);
        }).setVisible(true);
    }

    @Override
    public void refresh() {
        new SwingWorker<List<Booking>, Void>() {
            @Override
            protected List<Booking> doInBackground() throws Exception {
                return bookingService.list();
            }

            @Override
            protected void done() {
                try {
                    bookings = get();
                    tableModel.setRowCount(0);
                    for (Booking b : bookings) {
                        tableModel.addRow(new Object[]{
                                b.getCustomerName(),
                                b.getRoomNumber(),
                                DateUtil.format(b.getCheckIn()),
                                DateUtil.format(b.getCheckOut()),
                                b.getDays(),
                                CurrencyUtil.format(b.getTotalAmount()),
                                b.getBookingStatus().getLabel(),
                                b.getPaymentStatus().getLabel()
                        });
                    }
                    overlay.updateVisibility();
                    pageHeader.setSubtitle(bookings.size() + " bookings in the system");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load bookings");
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
        repaint();
    }
}
