package components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

public class ModernTable extends JTable {
    private static final Set<String> STATUS_LABELS = Set.of(
            "Available", "Booked", "Reserved", "Maintenance", "Cleaning",
            "Confirmed", "Checked In", "Checked Out", "Cancelled",
            "Pending", "Paid", "Refunded", "Partial"
    );

    private final TableRowSorter<DefaultTableModel> sorter;
    private final Font bodyFont = Theme.fontRegular(13);
    private final Font statusFont = Theme.fontMedium(12);
    private final Font headerFont = Theme.fontMedium(12);

    public ModernTable(DefaultTableModel model) {
        super(model);
        setRowHeight(40);
        setShowGrid(false);
        setIntercellSpacing(new Dimension(0, 0));
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setFont(bodyFont);
        setForeground(Theme.textPrimary());
        setBackground(Theme.bgCard());
        setSelectionBackground(Theme.ROYAL_BLUE);
        setSelectionForeground(Color.WHITE);
        setDoubleBuffered(true);
        setAutoCreateRowSorter(false);
        // Faster painting for large tables
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

        JTableHeader header = getTableHeader();
        header.setReorderingAllowed(false);
        header.setResizingAllowed(true);
        header.setPreferredSize(new Dimension(0, 42));
        header.setFont(headerFont);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBackground(Theme.DARK_NAVY);
                label.setForeground(Theme.GOLD);
                label.setFont(headerFont);
                label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setOpaque(true);
                return label;
            }
        });

        setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 12, 0, 12));
                label.setFont(bodyFont);
                if (!isSelected) {
                    label.setBackground(row % 2 == 0 ? Theme.bgCard() : Theme.tableAlt());
                    label.setForeground(Theme.textPrimary());
                }
                if (value != null) {
                    String s = value.toString();
                    if (STATUS_LABELS.contains(s)) {
                        Color status = Theme.statusColor(s);
                        label.setForeground(isSelected ? Color.WHITE : status);
                        label.setFont(statusFont);
                    }
                }
                label.setOpaque(true);
                return label;
            }
        });

        sorter = new TableRowSorter<>(model);
        // Sorting large columns by string is fine; avoid custom comparators cost
        setRowSorter(sorter);
    }

    public void filter(String query) {
        if (query == null || query.isBlank()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(query.trim())));
        }
    }

    public void applyTheme() {
        setForeground(Theme.textPrimary());
        setBackground(Theme.bgCard());
        repaint();
    }

    public int getSelectedModelRow() {
        int viewRow = getSelectedRow();
        if (viewRow < 0) {
            return -1;
        }
        return convertRowIndexToModel(viewRow);
    }

    /** Bulk model update without per-row events mid-fill. */
    public static void replaceRows(DefaultTableModel model, Runnable fill) {
        model.setRowCount(0);
        fill.run();
    }
}
