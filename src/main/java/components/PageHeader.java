package components;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public class PageHeader extends JPanel {
    private final JLabel titleLabel;
    private final JLabel subtitleLabel;
    private final JPanel actions;

    public PageHeader(String title, String subtitle) {
        setOpaque(false);
        setLayout(new BorderLayout(UiLayout.SPACE_MD, 0));
        setBorder(new EmptyBorder(0, 0, UiLayout.SPACE_MD, 0));

        JPanel text = new JPanel(new BorderLayout(0, UiLayout.SPACE_XS));
        text.setOpaque(false);
        titleLabel = new JLabel(title);
        titleLabel.setFont(Theme.fontBold(22));
        titleLabel.setForeground(Theme.textPrimary());
        subtitleLabel = new JLabel(subtitle == null ? " " : subtitle);
        subtitleLabel.setFont(Theme.fontRegular(13));
        subtitleLabel.setForeground(Theme.textSecondary());
        text.add(titleLabel, BorderLayout.NORTH);
        text.add(subtitleLabel, BorderLayout.SOUTH);

        actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, UiLayout.SPACE_SM, 0));
        actions.setOpaque(false);

        add(text, BorderLayout.CENTER);
        add(actions, BorderLayout.EAST);
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle == null ? " " : subtitle);
    }

    public void addAction(StyledButton button) {
        actions.add(button);
    }

    public void clearActions() {
        actions.removeAll();
    }

    public void applyTheme() {
        titleLabel.setForeground(Theme.textPrimary());
        subtitleLabel.setForeground(Theme.textSecondary());
    }
}
