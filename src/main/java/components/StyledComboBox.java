package components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicComboBoxUI;

public class StyledComboBox<E> extends JComboBox<E> {
    public StyledComboBox() {
        this(null);
    }

    @SuppressWarnings("unchecked")
    public StyledComboBox(E[] items) {
        super(items == null ? (E[]) new Object[0] : items);
        setFont(Theme.fontRegular(13));
        setForeground(Theme.textPrimary());
        setBackground(Theme.inputBg());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton button = new JButton() {
                    @Override
                    public void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(Theme.textMuted());
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2;
                        int[] x = {cx - 4, cx + 4, cx};
                        int[] y = {cy - 2, cy - 2, cy + 3};
                        g2.fillPolygon(x, y, 3);
                        g2.dispose();
                    }
                };
                button.setBorder(BorderFactory.createEmptyBorder());
                button.setContentAreaFilled(false);
                return button;
            }
        });
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                label.setFont(Theme.fontRegular(13));
                if (isSelected) {
                    label.setBackground(Theme.ROYAL_BLUE);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Theme.bgCard());
                    label.setForeground(Theme.textPrimary());
                }
                return label;
            }
        });
    }

    public void applyTheme() {
        setForeground(Theme.textPrimary());
        setBackground(Theme.inputBg());
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.border(), 1, true),
                new EmptyBorder(8, 10, 8, 10)
        ));
        repaint();
    }
}
