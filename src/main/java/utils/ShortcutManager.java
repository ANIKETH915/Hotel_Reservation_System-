package utils;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.util.function.Consumer;

public final class ShortcutManager {

    private static final String[] NAV_KEYS = {
            "Dashboard", "Rooms", "Customers", "Bookings",
            "Payments", "Reports", "Settings", "About"
    };

    private ShortcutManager() {
    }

    public static void register(JComponent rootPane, Consumer<String> onNavigate, Runnable onLogout) {
        for (int i = 0; i < NAV_KEYS.length; i++) {
            final String navKey = NAV_KEYS[i];
            int keyCode = KeyEvent.VK_1 + i;
            String actionKey = "nav_" + navKey;
            rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(keyCode, InputEvent.CTRL_DOWN_MASK), actionKey);
            rootPane.getActionMap().put(actionKey, new javax.swing.AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    onNavigate.accept(navKey);
                }
            });
        }

        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), "logout");
        rootPane.getActionMap().put("logout", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (onLogout != null) {
                    onLogout.run();
                }
            }
        });
    }
}
