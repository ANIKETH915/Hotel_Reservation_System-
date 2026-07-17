package reports;

import components.StyledButton;
import components.Theme;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import javax.swing.BorderFactory;
import javax.swing.JDialog;
import components.ConfirmDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public final class PrintUtil {

    private PrintUtil() {
    }

    public static void showPreview(Component parent, String title, String content) {
        Printable printable = createTextPrintable(content);
        showPreview(parent, title, content, printable);
    }

    public static void showPreview(Component parent, String title, String content, Printable printable) {
        java.awt.Window owner = parent != null
                ? SwingUtilities.getWindowAncestor(parent)
                : null;

        JDialog dialog = new JDialog(owner, title, java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 12));
        dialog.getContentPane().setBackground(Theme.bgPrimary());

        JTextArea area = new JTextArea(content);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        area.setBackground(Theme.bgCard());
        area.setForeground(Theme.textPrimary());
        area.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scroll = new JScrollPane(area);
        scroll.setPreferredSize(new Dimension(520, 420));
        scroll.setBorder(BorderFactory.createLineBorder(Theme.border()));

        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        buttons.setBorder(new EmptyBorder(0, 12, 12, 12));

        StyledButton printBtn = new StyledButton("Print");
        printBtn.addActionListener(e -> print(parent, printable));

        StyledButton closeBtn = new StyledButton("Close", StyledButton.Style.SECONDARY);
        closeBtn.addActionListener(e -> dialog.dispose());

        buttons.add(printBtn);
        buttons.add(closeBtn);

        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(buttons, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static void print(Component parent, Printable printable) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(printable);
        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                ConfirmDialog.alert(javax.swing.SwingUtilities.getWindowAncestor(parent),
                        "Print Error", "Print failed: " + ex.getMessage());
            }
        }
    }

    public static Printable createTextPrintable(String text) {
        String[] lines = text.split("\\R");
        return (Graphics graphics, PageFormat pageFormat, int pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
            g2.setFont(font);
            int lineHeight = g2.getFontMetrics().getHeight();
            int y = lineHeight;
            int maxWidth = (int) pageFormat.getImageableWidth();

            for (String line : lines) {
                if (y > pageFormat.getImageableHeight() - lineHeight) {
                    break;
                }
                if (g2.getFontMetrics().stringWidth(line) > maxWidth) {
                    String remaining = line;
                    while (!remaining.isEmpty() && y <= pageFormat.getImageableHeight() - lineHeight) {
                        int fit = remaining.length();
                        while (fit > 0 && g2.getFontMetrics().stringWidth(remaining.substring(0, fit)) > maxWidth) {
                            fit--;
                        }
                        if (fit == 0) {
                            fit = 1;
                        }
                        g2.drawString(remaining.substring(0, fit), 0, y);
                        remaining = remaining.substring(fit);
                        y += lineHeight;
                    }
                } else {
                    g2.drawString(line, 0, y);
                    y += lineHeight;
                }
            }
            return Printable.PAGE_EXISTS;
        };
    }
}
