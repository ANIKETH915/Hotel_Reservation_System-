package components;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Shared spacing tokens and scroll/layout helpers for consistent, responsive Swing UIs.
 */
public final class UiLayout {

    public static final int SPACE_XS = 4;
    public static final int SPACE_SM = 8;
    public static final int SPACE_MD = 16;
    public static final int SPACE_LG = 24;
    public static final int SPACE_XL = 32;

    public static final int PAGE_INSET = 20;
    public static final int CARD_INSET = 16;
    public static final int FORM_INSET = 24;
    public static final int DIALOG_INSET = 24;

    public static final int SCROLL_UNIT = 24;
    public static final int TABLE_ROW_MIN_VISIBLE = 4;
    public static final int TABLE_VIEWPORT_CAP = 320;

    private UiLayout() {
    }

    /** Vertical strut matching the design spacing scale. */
    public static Component strut(int size) {
        return Box.createVerticalStrut(size);
    }

    /** Makes a BoxLayout child stretch to the full available width. */
    public static <T extends JComponent> T fullWidth(T component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = component.getPreferredSize();
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height > 0 ? pref.height : Integer.MAX_VALUE));
        return component;
    }

    /** Recompute max height after preferred size may have changed. */
    public static void refreshFullWidth(JComponent component) {
        component.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension pref = component.getPreferredSize();
        component.setMaximumSize(new Dimension(Integer.MAX_VALUE,
                Math.max(pref.height, component.getMinimumSize().height)));
    }

    public static EmptyBorder pageBorder() {
        return new EmptyBorder(0, 0, 0, 0);
    }

    public static EmptyBorder dialogBorder() {
        return new EmptyBorder(DIALOG_INSET, DIALOG_INSET, DIALOG_INSET, DIALOG_INSET);
    }

    public static EmptyBorder formBorder() {
        return new EmptyBorder(FORM_INSET, FORM_INSET, FORM_INSET, FORM_INSET);
    }

    /**
     * Standard table scroll pane: no outer border, smooth increments, as-needed bars.
     */
    public static JScrollPane tableScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        configureScrollPane(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        table.setFillsViewportHeight(true);
        return scroll;
    }

    /**
     * Form / page scroll pane that tracks viewport width so content stays aligned when resized.
     */
    public static JScrollPane pageScroll(JComponent content) {
        if (!(content instanceof Scrollable)) {
            // Wrap non-scrollable content in a viewport-width panel when possible
        }
        JScrollPane scroll = new JScrollPane(content);
        configureScrollPane(scroll);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scroll.getViewport().setBackground(Theme.bgPrimary());
        return scroll;
    }

    public static void configureScrollPane(JScrollPane scroll) {
        scroll.getVerticalScrollBar().setUnitIncrement(SCROLL_UNIT);
        scroll.getHorizontalScrollBar().setUnitIncrement(SCROLL_UNIT);
        scroll.getViewport().setScrollMode(JViewport.BLIT_SCROLL_MODE);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(true);
        installWheelPassthrough(scroll);
    }

    /**
     * When an inner scroll pane does not need to scroll (or is at an edge),
     * forward the wheel event to the parent scroll pane so nested scrolling feels natural.
     */
    public static void installWheelPassthrough(JScrollPane scroll) {
        if (Boolean.TRUE.equals(scroll.getClientProperty("uiLayout.wheelPassthrough"))) {
            return;
        }
        scroll.putClientProperty("uiLayout.wheelPassthrough", Boolean.TRUE);
        scroll.addMouseWheelListener(e -> {
            JScrollBar bar = scroll.getVerticalScrollBar();
            if (bar == null || !bar.isVisible()) {
                dispatchToParentScroll(scroll, e);
                e.consume();
                return;
            }
            int value = bar.getValue();
            int extent = bar.getModel().getExtent();
            int max = bar.getMaximum();
            boolean atTop = value <= bar.getMinimum();
            boolean atBottom = value + extent >= max;
            boolean scrollingUp = e.getWheelRotation() < 0;
            boolean scrollingDown = e.getWheelRotation() > 0;
            if ((scrollingUp && atTop) || (scrollingDown && atBottom)) {
                dispatchToParentScroll(scroll, e);
                e.consume();
            }
        });
    }

    private static void dispatchToParentScroll(Component source, java.awt.event.MouseWheelEvent e) {
        Container parent = source.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane parentScroll && parentScroll != source) {
                parentScroll.dispatchEvent(javax.swing.SwingUtilities.convertMouseEvent(source, e, parentScroll));
                return;
            }
            parent = parent.getParent();
        }
    }

    /**
     * Sizes a table scroll pane to fit its rows (capped), enabling the bar only when capped.
     * Prefer this inside an outer page scroll to avoid awkward nested scrolling.
     */
    public static void fitTableScroll(JScrollPane scroll, JTable table) {
        int rows = Math.max(TABLE_ROW_MIN_VISIBLE, table.getRowCount());
        int headerH = table.getTableHeader() != null
                ? table.getTableHeader().getPreferredSize().height
                : 42;
        int natural = headerH + rows * table.getRowHeight() + 2;
        if (natural > TABLE_VIEWPORT_CAP) {
            scroll.setPreferredSize(new Dimension(100, TABLE_VIEWPORT_CAP));
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        } else {
            scroll.setPreferredSize(new Dimension(100, natural));
            scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        }
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.revalidate();
    }

    /** Recursively apply smooth scroll defaults to every JScrollPane under root. */
    public static void installScrollDefaults(Container root) {
        for (Component c : root.getComponents()) {
            if (c instanceof JScrollPane sp) {
                configureScrollPane(sp);
            } else if (c instanceof Container child) {
                installScrollDefaults(child);
            }
        }
    }

    /**
     * Panel that reports correct scrollable sizes and tracks viewport width
     * (prevents horizontal blank/clip and jumpy preferred-size behavior).
     */
    public static class ViewportWidthPanel extends JPanel implements Scrollable {
        private final int unitIncrement;
        private final int blockIncrement;

        public ViewportWidthPanel() {
            this(SCROLL_UNIT, 80);
        }

        public ViewportWidthPanel(int unitIncrement, int blockIncrement) {
            this.unitIncrement = unitIncrement;
            this.blockIncrement = blockIncrement;
            setOpaque(false);
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return unitIncrement;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return orientation == SwingConstants.VERTICAL
                    ? Math.max(blockIncrement, visibleRect.height - unitIncrement)
                    : Math.max(blockIncrement, visibleRect.width - unitIncrement);
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            if (getParent() instanceof JViewport viewport) {
                return getPreferredSize().height <= viewport.getHeight();
            }
            return false;
        }
    }
}
