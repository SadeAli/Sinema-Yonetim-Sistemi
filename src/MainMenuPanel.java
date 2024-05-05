import javax.swing.JPanel;
import javax.swing.JButton;

public class MainMenuPanel extends JPanel {
    MainMenuPanel(CinemaGUI parent, int width, int height) {
        setLayout(null);

        // center 3 buttons with 100px width and 50px height horizontally
        int buttonWidth = 150;
        int buttonHeight = 50;

        int buttonX = (width - buttonWidth) / 2;
        int buttonY = (height - buttonHeight) / 2 - 200;

        // create 3 buttons
        JButton button1 = new JButton("Film Seç");
        button1.setBounds(buttonX - (int)(1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
        button1.addActionListener(e -> {
            parent.updateState("Film Seç");
            parent.updateMovieList();
        });
        add(button1);

        JButton button2 = new JButton("Film Değerlendir");
        button2.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
        button2.addActionListener(e -> parent.updateState("Film Değerlendir"));
        add(button2);

        JButton button3 = new JButton("Admin Girişi");
        button3.setBounds(buttonX + (int)(1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
        button3.addActionListener(e -> parent.updateState("Admin Girişi"));
        add(button3);
    }
}
