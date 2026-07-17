import components.ConfirmDialog;
import components.Theme;
import components.ThemeManager;
import database.DatabaseInitializer;
import service.AuthService;
import service.SettingsService;
import ui.LoginFrame;
import ui.MainFrame;
import ui.SplashScreen;
import utils.CurrencyUtil;
import java.awt.Color;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                applyUiDefaults();
                SplashScreen splash = new SplashScreen(null);
                splash.showSplash();
                new SwingWorker<Boolean, Integer>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        publish(20);
                        DatabaseInitializer.initialize();
                        publish(70);
                        SettingsService settings = new SettingsService();
                        CurrencyUtil.setCurrency(settings.getCurrency());
                        ThemeManager.init();
                        publish(90);
                        return new AuthService().tryRememberMe() != null;
                    }

                    @Override
                    protected void process(java.util.List<Integer> progress) {
                        splash.setProgress(progress.get(progress.size() - 1));
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean remembered = get();
                            splash.complete();
                            if (remembered) {
                                new MainFrame().setVisible(true);
                            } else {
                                new LoginFrame().setVisible(true);
                            }
                        } catch (Exception e) {
                            splash.dispose();
                            e.printStackTrace();
                            ConfirmDialog.alert(null, "Startup Failed",
                                    e.getCause() == null ? e.getMessage() : e.getCause().getMessage());
                        }
                    }
                }.execute();
            } catch (Exception e) {
                e.printStackTrace();
                ConfirmDialog.alert(null, "Startup Failed",
                        e.getMessage() == null ? "Unable to start application." : e.getMessage());
            }
        });
    }

    private static void applyUiDefaults() {
        UIManager.put("Panel.background", Theme.bgPrimary());
        UIManager.put("OptionPane.background", Theme.bgCard());
        UIManager.put("Panel.font", Theme.fontRegular(13));
        UIManager.put("Label.font", Theme.fontRegular(13));
        UIManager.put("Button.font", Theme.fontMedium(13));
        UIManager.put("ComboBox.background", Theme.inputBg());
        UIManager.put("ComboBox.foreground", Theme.textPrimary());
        UIManager.put("ScrollBar.thumb", Theme.ROYAL_BLUE);
        UIManager.put("control", Theme.bgPrimary());
        UIManager.put("text", Theme.textPrimary());
        UIManager.put("nimbusFocus", Theme.GOLD);
        UIManager.put("TabbedPane.selected", Theme.ROYAL_BLUE);
    }
}
