package ui;

import components.AppEvents;
import components.ConfirmDialog;
import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StyledButton;
import components.StyledComboBox;
import components.TableCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JPanel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Room;
import model.RoomStatus;
import service.RoomService;
import ui.dialog.RoomDetailDialog;
import ui.dialog.RoomFormDialog;
import utils.CurrencyUtil;

public class RoomPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final RoomService roomService = new RoomService();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Number", "Type", "Floor", "Price", "Capacity", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay overlay;
    private TableCard tableCard;
    private java.util.List<Room> rooms = java.util.List.of();
    private PageHeader pageHeader;

    public RoomPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Room Inventory", "Manage rooms, rates, and housekeeping status");

        StyledButton addBtn = new StyledButton("Add Room");
        addBtn.setToolTipText("Add a new room to inventory");
        pageHeader.addAction(addBtn);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, UiLayout.SPACE_SM, UiLayout.SPACE_XS));
        toolbar.setOpaque(false);

        StyledButton editBtn = new StyledButton("Edit", StyledButton.Style.SECONDARY);
        StyledButton deleteBtn = new StyledButton("Delete", StyledButton.Style.DANGER);
        StyledButton viewBtn = new StyledButton("View", StyledButton.Style.GHOST);

        StyledComboBox<RoomStatus> statusCombo = new StyledComboBox<>(new RoomStatus[]{
                RoomStatus.AVAILABLE, RoomStatus.CLEANING, RoomStatus.MAINTENANCE
        });
        statusCombo.setToolTipText("Housekeeping status only — Booked/Reserved are set by bookings");
        StyledButton statusBtn = new StyledButton("Set Status", StyledButton.Style.SECONDARY);

        toolbar.add(editBtn);
        toolbar.add(viewBtn);
        toolbar.add(deleteBtn);
        toolbar.add(statusCombo);
        toolbar.add(statusBtn);

        table = new ModernTable(tableModel);
        EmptyStatePanel empty = new EmptyStatePanel("No rooms configured",
                "Add your first room to start taking reservations.");
        empty.setIconKey("rooms");
        empty.setAction("Add Room", () -> new RoomFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        overlay = new TableEmptyOverlay(UiLayout.tableScroll(table), empty);
        tableCard = new TableCard(overlay);

        JPanel north = new JPanel(new BorderLayout(0, UiLayout.SPACE_SM));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);
        north.add(toolbar, BorderLayout.SOUTH);

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        addBtn.addActionListener(e -> new RoomFormDialog(mainFrame, null, this::afterMutation).setVisible(true));
        editBtn.addActionListener(e -> editSelected());
        deleteBtn.addActionListener(e -> deleteSelected());
        viewBtn.addActionListener(e -> viewSelected());
        statusBtn.addActionListener(e -> updateStatus((RoomStatus) statusCombo.getSelectedItem()));
    }

    private void afterMutation() {
        mainFrame.notifyDataChanged(AppEvents.Domain.ROOMS);
    }

    private Room selectedRoom() {
        int row = table.getSelectedModelRow();
        if (row < 0 || row >= rooms.size()) {
            return null;
        }
        return rooms.get(row);
    }

    private void editSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new RoomFormDialog(mainFrame, room, this::afterMutation).setVisible(true);
    }

    private void viewSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new RoomDetailDialog(mainFrame, room).setVisible(true);
    }

    private void deleteSelected() {
        Room room = selectedRoom();
        if (room == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        if (!ConfirmDialog.confirmDelete(mainFrame,
                "Permanently delete room " + room.getRoomNumber() + "? This cannot be undone.")) {
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                roomService.delete(room.getRoomId());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Room deleted");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    private void updateStatus(RoomStatus status) {
        Room room = selectedRoom();
        if (room == null || status == null) {
            Toast.error(mainFrame, "Select a room first");
            return;
        }
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                roomService.updateStatus(room.getRoomId(), status);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    Toast.success(mainFrame, "Status updated");
                    afterMutation();
                } catch (Exception ex) {
                    Toast.error(mainFrame, ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage());
                }
            }
        }.execute();
    }

    @Override
    public void refresh() {
        new SwingWorker<java.util.List<Room>, Void>() {
            @Override
            protected java.util.List<Room> doInBackground() throws Exception {
                return roomService.list();
            }

            @Override
            protected void done() {
                try {
                    rooms = get();
                    tableModel.setRowCount(0);
                    for (Room r : rooms) {
                        tableModel.addRow(new Object[]{
                                r.getRoomNumber(),
                                r.getRoomType().getLabel(),
                                r.getFloor(),
                                CurrencyUtil.format(r.getPrice()),
                                r.getCapacity(),
                                r.getStatus().getLabel()
                        });
                    }
                    overlay.updateVisibility();
                    pageHeader.setSubtitle(rooms.size() + " rooms in inventory");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load rooms from database");
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
        overlay.applyTheme();
        tableCard.applyTheme();
        repaint();
    }
}
