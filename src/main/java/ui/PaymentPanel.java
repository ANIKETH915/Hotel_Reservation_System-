package ui;

import components.EmptyStatePanel;
import components.ModernTable;
import components.PageHeader;
import components.StatCard;
import components.TableCard;
import components.TableEmptyOverlay;
import components.Theme;
import components.Toast;
import components.UiLayout;
import dao.PaymentDao;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.List;
import javax.swing.JPanel;
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
        todayCard = new StatCard("Today's Revenue", CurrencyUtil.format(BigDecimal.ZERO), Theme.EMERALD);
        monthCard = new StatCard("This Month", CurrencyUtil.format(BigDecimal.ZERO), Theme.ROYAL_BLUE);
        stats.add(todayCard);
        stats.add(monthCard);

        table = new ModernTable(tableModel);
        EmptyStatePanel empty = new EmptyStatePanel("No payments recorded",
                "Payments appear here after you record them on a booking.");
        empty.setIconKey("payments");
        overlay = new TableEmptyOverlay(UiLayout.tableScroll(table), empty);
        tableCard = new TableCard(overlay);

        JPanel north = new JPanel(new BorderLayout(0, UiLayout.SPACE_MD));
        north.setOpaque(false);
        north.add(pageHeader, BorderLayout.NORTH);
        north.add(stats, BorderLayout.SOUTH);

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
        table.filter(query);
    }

    @Override
    public void applyTheme() {
        setBackground(Theme.bgPrimary());
        pageHeader.applyTheme();
        todayCard.applyTheme();
        monthCard.applyTheme();
        table.applyTheme();
        overlay.applyTheme();
        tableCard.applyTheme();
        repaint();
    }

    private record PaymentView(List<Payment> payments, BigDecimal today, BigDecimal month) {
    }
}
