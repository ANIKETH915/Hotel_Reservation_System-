package components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class DateChooser extends JPanel {
    private LocalDate selected;
    private final JLabel monthLabel;
    private final JPanel grid;
    private YearMonth month;
    private Consumer<LocalDate> onChange;

    public DateChooser() {
        this(LocalDate.now());
    }

    public DateChooser(LocalDate initial) {
        this.selected = initial;
        this.month = YearMonth.from(initial);
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        StyledButton prev = new StyledButton("<", StyledButton.Style.GHOST);
        StyledButton next = new StyledButton(">", StyledButton.Style.GHOST);
        prev.setPreferredSize(new Dimension(36, 28));
        next.setPreferredSize(new Dimension(36, 28));
        monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(Theme.fontMedium(13));
        monthLabel.setForeground(Theme.textPrimary());
        header.add(prev, BorderLayout.WEST);
        header.add(monthLabel, BorderLayout.CENTER);
        header.add(next, BorderLayout.EAST);
        prev.addActionListener(e -> {
            month = month.minusMonths(1);
            rebuild();
        });
        next.addActionListener(e -> {
            month = month.plusMonths(1);
            rebuild();
        });

        grid = new JPanel(new GridLayout(0, 7, 4, 4));
        grid.setOpaque(false);
        add(header, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
        rebuild();
    }

    public LocalDate getSelectedDate() {
        return selected;
    }

    public void setSelectedDate(LocalDate date) {
        this.selected = date;
        this.month = YearMonth.from(date);
        rebuild();
    }

    public void setOnChange(Consumer<LocalDate> onChange) {
        this.onChange = onChange;
    }

    private void rebuild() {
        grid.removeAll();
        monthLabel.setText(month.getMonth() + " " + month.getYear());
        String[] days = {"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};
        for (String d : days) {
            JLabel l = new JLabel(d, SwingConstants.CENTER);
            l.setFont(Theme.fontMedium(11));
            l.setForeground(Theme.textMuted());
            grid.add(l);
        }
        LocalDate first = month.atDay(1);
        int shift = first.getDayOfWeek().getValue() - 1;
        for (int i = 0; i < shift; i++) {
            grid.add(new JLabel(""));
        }
        for (int day = 1; day <= month.lengthOfMonth(); day++) {
            LocalDate date = month.atDay(day);
            JButton btn = new JButton(String.valueOf(day)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = date.equals(selected);
                    if (sel) {
                        g2.setColor(Theme.ROYAL_BLUE);
                        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Theme.textPrimary());
                    }
                    g2.setFont(Theme.fontRegular(12));
                    int tw = g2.getFontMetrics().stringWidth(getText());
                    int x = (getWidth() - tw) / 2;
                    int y = (getHeight() + g2.getFontMetrics().getAscent() - g2.getFontMetrics().getDescent()) / 2;
                    g2.drawString(getText(), x, y);
                    g2.dispose();
                }
            };
            btn.setFocusPainted(false);
            btn.setBorderPainted(false);
            btn.setContentAreaFilled(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.addActionListener(e -> {
                selected = date;
                rebuild();
                if (onChange != null) {
                    onChange.accept(selected);
                }
            });
            grid.add(btn);
        }
        revalidate();
        repaint();
    }
}
