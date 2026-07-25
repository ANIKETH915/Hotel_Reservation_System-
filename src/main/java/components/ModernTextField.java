package components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class ModernTextField extends JTextField {
    public ModernTextField() {
        this(20);
    }

    public ModernTextField(int columns) {
        super(columns);
        setFont(Theme.fontRegular(13));
        setForeground(Theme.textPrimary());
        setCaretColor(Theme.ROYAL_BLUE);
        applyBorder(false);
        setBackground(Theme.inputBg());
        setOpaque(true);
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                applyBorder(true);
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                applyBorder(false);
            }
        });
    }

    private void applyBorder(boolean focused) {
        java.awt.Color border = focused ? Theme.ROYAL_BLUE : Theme.border();
        int thickness = focused ? 2 : 1;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(border, thickness, true),
                new EmptyBorder(focused ? 9 : 10, focused ? 11 : 12, focused ? 9 : 10, focused ? 11 : 12)
        ));
    }

    public void applyTheme() {
        setForeground(Theme.textPrimary());
        setBackground(Theme.inputBg());
        applyBorder(isFocusOwner());
    }

    public static class Password extends JPasswordField {
        public Password() {
            setFont(Theme.fontRegular(13));
            setForeground(Theme.textPrimary());
            setCaretColor(Theme.ROYAL_BLUE);
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.border(), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
            setBackground(Theme.inputBg());
            setOpaque(true);
        }

        public void applyTheme() {
            setForeground(Theme.textPrimary());
            setBackground(Theme.inputBg());
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Theme.border(), 1, true),
                    new EmptyBorder(10, 12, 10, 12)
            ));
        }
    }
}
