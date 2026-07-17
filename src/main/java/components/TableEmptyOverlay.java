package components;

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
}
