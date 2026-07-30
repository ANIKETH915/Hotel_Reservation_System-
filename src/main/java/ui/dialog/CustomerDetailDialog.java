package ui.dialog;

import components.ModernTable;
import components.StatusBadge;
import components.Theme;
import components.UiLayout;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
    private int hoveredRow = -1;

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
        table.setRowHeight(40);

        // Hover listeners
        table.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                if (row != hoveredRow) {
                    hoveredRow = row;
                    table.repaint();
                }
            }
        });
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                table.repaint();
            }
        });

        // Table Header rounded renderer
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(Theme.fontBold(12));
                label.setForeground(Theme.GOLD);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

                JPanel cell = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(Theme.DARK_NAVY);
                        int w = getWidth();
                        int h = getHeight();

                        if (column == 0) {
                            g2.fillRoundRect(0, 2, w + 10, h - 4, 10, 10);
                            g2.fillRect(w - 10, 2, 10, h - 4);
                        } else if (column == t.getColumnCount() - 1) {
                            g2.fillRoundRect(-10, 2, w + 10, h - 4, 10, 10);
                            g2.fillRect(0, 2, 10, h - 4);
                        } else {
                            g2.fillRect(0, 2, w, h - 4);
                        }

                        g2.setColor(Theme.border());
                        g2.drawLine(0, h - 1, w, h - 1);

                        g2.dispose();
                    }
                };
                cell.setOpaque(false);
                cell.add(label, BorderLayout.CENTER);
                return cell;
            }
        });

        // Table Cell Renderer with Hover & Status badges
        table.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            private final StatusBadge badge = new StatusBadge("");
            private final JLabel label = new JLabel();

            {
                label.setOpaque(true);
                label.setFont(Theme.fontRegular(13));
                label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                String valStr = value == null ? "" : value.toString();

                if (column == 5) {
                    badge.setText(valStr);
                    JPanel p = new JPanel(new GridBagLayout());
                    p.setOpaque(true);

                    if (isSelected) {
                        p.setBackground(Theme.ROYAL_BLUE);
                    } else if (row == hoveredRow) {
                        p.setBackground(Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9));
                    } else {
                        p.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                    }

                    p.add(badge);
                    return p;
                }

                label.setText(valStr);

                if (isSelected) {
                    label.setBackground(Theme.ROYAL_BLUE);
                    label.setForeground(Color.WHITE);
                } else if (row == hoveredRow) {
                    label.setBackground(Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9));
                    label.setForeground(Theme.textPrimary());
                } else {
                    label.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                    label.setForeground(Theme.textPrimary());
                }

                return label;
            }
        });

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
                }
            }
        }.execute();
    }
}
