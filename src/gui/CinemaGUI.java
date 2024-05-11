package gui;

import java.awt.CardLayout;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

import gui.mainPanels.AdminPanel;
import gui.mainPanels.MovieRatingPanel;
import gui.mainPanels.TicketSellingPanel;

public class CinemaGUI extends JFrame {
    static final int width = 1000;
    static final int height = 800;

    private JPanel mainPanel = new JPanel();
    private CardLayout m_cardLayout = new CardLayout();

    private MainMenuPanel mainMenuPanel = new MainMenuPanel(this, width, height);
    private TicketSellingPanel ticketSellingPanel = new TicketSellingPanel(this, width, height);
    private MovieRatingPanel movieRatingPanel = new MovieRatingPanel(this, width, height);
    private AdminPanel adminPanel = new AdminPanel(this, width, height);

    public static void main(String[] args) {

        @SuppressWarnings("unused")
        CinemaGUI cinemaGUI = new CinemaGUI();

        String lookAndFeel[] = {
                "javax.swing.plaf.metal.MetalLookAndFeel",
                "javax.swing.plaf.nimbus.NimbusLookAndFeel",
                "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
                "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
        };

        try {
            UIManager.setLookAndFeel(lookAndFeel[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    CinemaGUI() {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // card layout for the main window
        mainPanel.setLayout(m_cardLayout);
        add(mainPanel);

        // tabbed pane 0 is the main menu panel
        mainPanel.add("Main Menu", mainMenuPanel);
        mainPanel.add("Select a Movie", ticketSellingPanel);
        mainPanel.add("Rate a Movie", movieRatingPanel);
        mainPanel.add("Admin Login", adminPanel);

        showMainMenu();
        setVisible(true);
    }

    public void showMainMenu() {
        m_cardLayout.show(mainPanel, "Main Menu");
    }

    private class MainMenuPanel extends JPanel {
        MainMenuPanel(CinemaGUI parent, int width, int height) {
            setLayout(null);

            // center 3 buttons with 100px width and 50px height horizontally
            int buttonWidth = 150;
            int buttonHeight = 50;

            int buttonX = (width - buttonWidth) / 2;
            int buttonY = (height - buttonHeight) / 2 - 200;

            // create 3 buttons
            JButton button1 = new JButton("Select a Movie");
            button1.setBounds(buttonX - (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button1.addActionListener(e -> {
                m_cardLayout.show(mainPanel, "Select a Movie");
                ticketSellingPanel.onVisible();
            });
            add(button1);

            JButton button2 = new JButton("Rate a Movie");
            button2.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
            button2.addActionListener(e -> m_cardLayout.show(mainPanel, "Rate a Movie"));
            add(button2);

            JButton button3 = new JButton("Admin Login");
            button3.setBounds(buttonX + (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button3.addActionListener(e -> {
                String password = JOptionPane.showInputDialog(this, "Enter password:");

                if (password == null) {
                    return;
                }

                if (isValidPassword(password)) {
                    m_cardLayout.show(mainPanel, "Admin Login");
                    CinemaGUI.this.adminPanel.onVisible();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!");
                }
            });
            add(button3);
        }

        private boolean isValidPassword(String password) {
            return password != null && password.equals("admin");
        }
    }
}
