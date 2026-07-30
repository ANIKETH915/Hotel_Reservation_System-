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
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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

    // Redesigned search and hover
    private SearchField searchField;
    private int hoveredRow = -1;

    public CustomerPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Guests", "Profiles, contact details, and reservation history");

        PremiumAddButton addBtn = new PremiumAddButton("Add Customer");
        pageHeader.addAction(addBtn);

        // Toolbar Panel: Split Layout
        final JPanel toolbar = new JPanel(new BorderLayout(UiLayout.SPACE_MD, 0));
        toolbar.setOpaque(false);

        // Left Actions: Edit, View, Delete
        final JPanel leftActions = new JPanel(new GridBagLayout());
        leftActions.setOpaque(false);
        GridBagConstraints lGbc = new GridBagConstraints();
        lGbc.gridy = 0;
        lGbc.fill = GridBagConstraints.VERTICAL;
        lGbc.insets = new java.awt.Insets(0, 0, 0, UiLayout.SPACE_SM);

        PremiumActionButton editBtn = new PremiumActionButton("Edit", PremiumActionButton.Style.SECONDARY, "edit");
        PremiumActionButton viewBtn = new PremiumActionButton("View History", PremiumActionButton.Style.GHOST, "view");
        PremiumActionButton deleteBtn = new PremiumActionButton("Delete", PremiumActionButton.Style.DANGER, "delete");

        leftActions.add(editBtn, lGbc);
        leftActions.add(viewBtn, lGbc);
        lGbc.insets = new java.awt.Insets(0, 0, 0, 0);
        leftActions.add(deleteBtn, lGbc);

        // Right Actions: Search field
        final JPanel rightActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
        rightActions.setOpaque(false);
        searchField = new SearchField();
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void changed() {
                table.filter(searchField.getText());
            }
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { changed(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { changed(); }
        });
        rightActions.add(searchField);

        toolbar.add(leftActions, BorderLayout.WEST);
        toolbar.add(rightActions, BorderLayout.EAST);

        table = new ModernTable(tableModel);
        table.setRowHeight(44);

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

        // Rounded table header renderer
        table.getTableHeader().setDefaultRenderer(new javax.swing.table.TableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = new JLabel(value == null ? "" : value.toString());
                label.setFont(Theme.fontBold(12));
                label.setForeground(Theme.GOLD);
                label.setHorizontalAlignment(SwingConstants.LEFT);
                label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));

                JPanel cell = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        g2.setColor(Theme.DARK_NAVY);
                        int w = getWidth();
                        int h = getHeight();

                        if (column == 0) {
                            g2.fillRoundRect(0, 2, w + 12, h - 4, 12, 12);
                            g2.fillRect(w - 12, 2, 12, h - 4);
                        } else if (column == t.getColumnCount() - 1) {
                            g2.fillRoundRect(-12, 2, w + 12, h - 4, 12, 12);
                            g2.fillRect(0, 2, 12, h - 4);
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

        // Table Cell Renderer
        table.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            private final JLabel label = new JLabel();
            {
                label.setOpaque(true);
                label.setFont(Theme.fontRegular(13));
                label.setBorder(BorderFactory.createEmptyBorder(0, 16, 0, 16));
            }

            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                label.setText(value == null ? "" : value.toString());

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

        // Add responsiveness handler
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent event) {
                int w = getWidth();
                if (w < 820) {
                    if (toolbar.getLayout() instanceof BorderLayout) {
                        toolbar.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, UiLayout.SPACE_SM));
                        toolbar.add(leftActions);
                        toolbar.add(rightActions);
                        toolbar.revalidate();
                    }
                } else {
                    if (!(toolbar.getLayout() instanceof BorderLayout)) {
                        toolbar.setLayout(new BorderLayout(UiLayout.SPACE_MD, 0));
                        toolbar.add(leftActions, BorderLayout.WEST);
                        toolbar.add(rightActions, BorderLayout.EAST);
                        toolbar.revalidate();
                    }
                }
            }
        });
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
        if (!searchField.getText().equals(query)) {
            searchField.setText(query);
        }
        table.filter(query);
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        table.applyTheme();
        tableOverlay.applyTheme();
        tableCard.applyTheme();
        searchField.applyTheme();
        repaint();
    }

    // Custom Component: PremiumAddButton (subclassing StyledButton for type compatibility)
    private static class PremiumAddButton extends StyledButton {
        private boolean hover = false;

        public PremiumAddButton(String text) {
            super(text, Style.PRIMARY);
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.fontBold(13));
            setForeground(Color.WHITE);

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
        public Dimension getPreferredSize() {
            return new Dimension(140, 42);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = hover ? new Color(0x25, 0x63, 0xEB) : Theme.ROYAL_BLUE;
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(Color.WHITE);
            g2.setStroke(new java.awt.BasicStroke(2.0f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawLine(18, 21, 26, 21);
            g2.drawLine(22, 17, 22, 25);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), 36, y);

            g2.dispose();
        }
    }

    // Custom Component: PremiumActionButton
    private static class PremiumActionButton extends JButton {
        public enum Style { SECONDARY, DANGER, GHOST }
        private final Style style;
        private final String iconType;
        private boolean hover = false;

        public PremiumActionButton(String text, Style style, String iconType) {
            super(text);
            this.style = style;
            this.iconType = iconType;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(Theme.fontMedium(12));

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
        public Dimension getPreferredSize() {
            FontMetrics metrics = getFontMetrics(getFont());
            int width = metrics.stringWidth(getText() == null ? "" : getText()) + 40;
            return new Dimension(width, 36);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill;
            Color text;
            Color border = null;

            switch (style) {
                case DANGER -> {
                    fill = hover ? new Color(0xDC, 0x26, 0x26) : new Color(0xEF, 0x44, 0x44);
                    text = Color.WHITE;
                }
                case GHOST -> {
                    fill = hover ? (Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xF1, 0xF5, 0xF9)) : new Color(0, 0, 0, 0);
                    text = Theme.ROYAL_BLUE;
                }
                default -> { // SECONDARY
                    fill = hover ? (Theme.isDark() ? new Color(0x1E, 0x29, 0x3B) : new Color(0xE5, 0xE7, 0xEB)) : Theme.bgCard();
                    text = Theme.textPrimary();
                    border = Theme.border();
                }
            }

            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new java.awt.BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            }

            g2.setColor(text);
            g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            drawIcon(g2, 12, (getHeight() - 12) / 2);

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(getText(), 32, y);

            g2.dispose();
        }

        private void drawIcon(Graphics2D g2, int x, int y) {
            switch (iconType) {
                case "edit" -> {
                    g2.drawRect(x, y + 5, 6, 3);
                    g2.drawLine(x + 1, y + 5, x + 4, y + 2);
                    g2.drawLine(x + 3, y + 7, x + 6, y + 4);
                }
                case "delete" -> {
                    g2.drawRect(x + 2, y + 2, 8, 8);
                    g2.drawLine(x, y + 2, x + 12, y + 2);
                    g2.drawLine(x + 4, y, x + 8, y);
                    g2.drawLine(x + 4, y + 4, x + 4, y + 8);
                    g2.drawLine(x + 8, y + 4, x + 8, y + 8);
                }
                case "view" -> {
                    g2.drawOval(x, y + 2, 12, 6);
                    g2.fillOval(x + 4, y + 3, 4, 4);
                }
            }
        }
    }

    // Custom Component: SearchField
    private static class SearchField extends JTextField {
        private boolean hover = false;
        private final String placeholder = "Search guests, contact...";

        public SearchField() {
            setOpaque(false);
            setFont(Theme.fontRegular(13));
            setForeground(Theme.textPrimary());
            setCaretColor(Theme.ROYAL_BLUE);
            setBorder(BorderFactory.createEmptyBorder(8, 36, 8, 12));

            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(java.awt.event.FocusEvent e) {
                    repaint();
                }
                @Override
                public void focusLost(java.awt.event.FocusEvent e) {
                    repaint();
                }
            });
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
        public Dimension getPreferredSize() {
            return new Dimension(280, 36);
        }

        public void applyTheme() {
            setForeground(Theme.textPrimary());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(Theme.inputBg());
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            if (isFocusOwner()) {
                g2.setColor(Theme.ROYAL_BLUE);
                g2.setStroke(new java.awt.BasicStroke(1.8f));
            } else if (hover) {
                g2.setColor(Theme.textSecondary());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
            } else {
                g2.setColor(Theme.border());
                g2.setStroke(new java.awt.BasicStroke(1.0f));
            }
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 18, 18);

            g2.setColor(isFocusOwner() ? Theme.ROYAL_BLUE : Theme.textSecondary());
            g2.setStroke(new java.awt.BasicStroke(1.6f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
            g2.drawOval(12, 11, 7, 7);
            g2.drawLine(18, 17, 22, 21);

            g2.dispose();

            super.paintComponent(g);

            if (getText().isEmpty()) {
                Graphics2D gPlaceholder = (Graphics2D) g.create();
                gPlaceholder.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                gPlaceholder.setColor(Theme.textMuted());
                gPlaceholder.setFont(getFont());
                FontMetrics fm = gPlaceholder.getFontMetrics();
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                gPlaceholder.drawString(placeholder, 36, y);
                gPlaceholder.dispose();
            }
        }
    }
}
