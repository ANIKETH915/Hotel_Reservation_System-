package ui;

import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.TableCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import components.UiLayout;
import dao.PaymentDao;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import model.Payment;
import service.PaymentService;
import utils.CurrencyUtil;
import utils.DateUtil;

public class PaymentPanel extends JPanel implements MainFrame.RefreshablePanel {

    private final PaymentService paymentService = new PaymentService();
    private final PaymentDao paymentDao = new PaymentDao();
    private final MainFrame mainFrame;

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"Booking", "Guest", "Room", "Method", "Amount", "Date", "Transaction"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private ModernTable table;
    private TableEmptyOverlay overlay;
    private TableCard tableCard;
    private StatCard todayCard;
    private StatCard monthCard;
    private PageHeader pageHeader;
    private JPanel stats;

    // Search and hovers
    private SearchField searchField;
    private int hoveredRow = -1;

    public PaymentPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, UiLayout.SPACE_MD));
        setBackground(Theme.bgPrimary());
        buildUi();
    }

    private void buildUi() {
        pageHeader = new PageHeader("Payments", "All recorded transactions from the payments ledger");

        stats = new JPanel(new GridLayout(1, 2, UiLayout.SPACE_MD, 0));
        stats.setOpaque(false);
        todayCard = new StatCard("Today's Revenue", "revenue", Theme.EMERALD);
        monthCard = new StatCard("This Month", "monthly", Theme.ROYAL_BLUE);
        stats.add(todayCard);
        stats.add(monthCard);

        // Toolbar: Split layout for search
        JPanel toolbar = new JPanel(new BorderLayout(UiLayout.SPACE_MD, 0));
        toolbar.setOpaque(false);

        JPanel rightActions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));
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

        // Table Header rounded renderer
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

        EmptyStatePanel empty = new EmptyStatePanel("No payments recorded",
                "Payments appear here after you record them on a booking.");
        empty.setIconKey("payments");
        overlay = new TableEmptyOverlay(UiLayout.tableScroll(table), empty);
        tableCard = new TableCard(overlay);

        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);
        north.add(UiLayout.fullWidth(pageHeader));
        north.add(Box.createVerticalStrut(UiLayout.SPACE_SM));
        north.add(UiLayout.fullWidth(stats));
        north.add(Box.createVerticalStrut(UiLayout.SPACE_MD));
        north.add(UiLayout.fullWidth(toolbar));

        add(north, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                int cols = getWidth() < 640 ? 1 : 2;
                GridLayout layout = (GridLayout) stats.getLayout();
                if (layout.getColumns() != cols) {
                    stats.setLayout(new GridLayout(cols == 1 ? 2 : 1, cols, UiLayout.SPACE_MD, UiLayout.SPACE_MD));
                    stats.revalidate();
                }
            }
        });
    }

    @Override
    public void refresh() {
        new SwingWorker<PaymentView, Void>() {
            @Override
            protected PaymentView doInBackground() throws Exception {
                return new PaymentView(paymentService.list(), paymentDao.sumToday(), paymentDao.sumThisMonth());
            }

            @Override
            protected void done() {
                try {
                    PaymentView view = get();
                    tableModel.setRowCount(0);
                    for (Payment p : view.payments) {
                        tableModel.addRow(new Object[]{
                                p.getBookingId(),
                                p.getCustomerName(),
                                p.getRoomNumber(),
                                p.getPaymentMethod().getLabel(),
                                CurrencyUtil.format(p.getAmount()),
                                DateUtil.format(p.getPaymentDate()),
                                p.getTransactionId()
                        });
                    }
                    todayCard.setValue(CurrencyUtil.format(view.today));
                    monthCard.setValue(CurrencyUtil.format(view.month));
                    overlay.updateVisibility();
                    pageHeader.setSubtitle(view.payments.size() + " payment records");
                } catch (Exception ex) {
                    Toast.error(mainFrame, "Failed to load payments");
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
        todayCard.updateTheme();
        monthCard.updateTheme();
        table.applyTheme();
        overlay.applyTheme();
        tableCard.applyTheme();
        searchField.applyTheme();
        repaint();
    }

    private record PaymentView(List<Payment> payments, BigDecimal today, BigDecimal month) {
    }

    // Custom Component: StatCard
    private static class StatCard extends JPanel {
        private final JLabel valueLabel;
        private final JLabel titleLabel;
        private final String iconKey;
        private final Color accentColor;

        public StatCard(String title, String iconKey, Color accentColor) {
            this.iconKey = iconKey;
            this.accentColor = accentColor;

            setLayout(new BorderLayout(UiLayout.SPACE_MD, 0));
            setBackground(Theme.bgCard());
            setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

            JPanel iconPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    g2.setColor(new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), 26));
                    g2.fillOval(0, 0, 36, 36);

                    g2.setColor(accentColor);
                    g2.setStroke(new java.awt.BasicStroke(1.8f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
                    drawCustomIcon(g2, iconKey, accentColor);

                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(36, 36);
                }
            };
            iconPanel.setOpaque(false);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            titleLabel = new JLabel(title);
            titleLabel.setFont(Theme.fontMedium(11));
            titleLabel.setForeground(Theme.textSecondary());

            valueLabel = new JLabel("0");
            valueLabel.setFont(Theme.fontBold(20));
            valueLabel.setForeground(Theme.textPrimary());

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(2));
            textPanel.add(valueLabel);

            add(iconPanel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        private void drawCustomIcon(Graphics2D g2, String iconKey, Color accentColor) {
            // Draw credit card
            g2.drawRoundRect(8, 10, 20, 15, 3, 3);
            g2.drawLine(8, 14, 28, 14);
            g2.drawLine(12, 19, 16, 19);
        }

        public void setValue(String value) {
            valueLabel.setText(value);
        }

        public void updateTheme() {
            setBackground(Theme.bgCard());
            titleLabel.setForeground(Theme.textSecondary());
            valueLabel.setForeground(Theme.textPrimary());
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

            g2.setColor(Theme.border());
            g2.setStroke(new java.awt.BasicStroke(1.0f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);

            g2.dispose();
        }
    }

    // Custom Component: SearchField
    private static class SearchField extends JTextField {
        private boolean hover = false;
        private final String placeholder = "Search payments...";

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
