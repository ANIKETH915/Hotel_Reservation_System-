package components;

import java.awt.Dimension;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.OverlayLayout;
import javax.swing.event.TableModelEvent;

/** Shows an empty-state panel over a table when it has no rows. */
public class TableEmptyOverlay extends JPanel {
    private final JScrollPane scrollPane;
    private final EmptyStatePanel emptyState;
    private final JTable table;

    public TableEmptyOverlay(JScrollPane scrollPane, EmptyStatePanel emptyState) {
        this.scrollPane = scrollPane;
        this.emptyState = emptyState;
        this.table = (JTable) scrollPane.getViewport().getView();
        setLayout(new OverlayLayout(this));
        setOpaque(false);
        emptyState.setAlignmentX(0.5f);
        emptyState.setAlignmentY(0.5f);
        scrollPane.setAlignmentX(0.5f);
        scrollPane.setAlignmentY(0.5f);
        UiLayout.configureScrollPane(scrollPane);
        if (scrollPane.getBorder() == null || scrollPane.getBorder().getBorderInsets(scrollPane).top == 0) {
            scrollPane.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        }
        add(scrollPane);
        add(emptyState);
        table.getModel().addTableModelListener((TableModelEvent e) -> updateVisibility());
        updateVisibility();
    }

    public void updateVisibility() {
        boolean empty = table.getModel().getRowCount() == 0;
        emptyState.setVisible(empty);
        scrollPane.setEnabled(!empty);
        revalidate();
        repaint();
    }

    public EmptyStatePanel getEmptyState() {
        return emptyState;
    }

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JTable getTable() {
        return table;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension scrollPref = scrollPane.getPreferredSize();
        Dimension emptyPref = emptyState.getPreferredSize();
        if (emptyState.isVisible()) {
            return new Dimension(
                    Math.max(scrollPref.width, emptyPref.width),
                    Math.max(scrollPref.height, emptyPref.height));
        }
        return scrollPref;
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(120, 120);
    }

    public void applyTheme() {
        emptyState.applyTheme();
        scrollPane.getViewport().setBackground(Theme.bgCard());
        repaint();
    }
}
