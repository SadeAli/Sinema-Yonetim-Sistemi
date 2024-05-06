import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;

import java.awt.CardLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import java.awt.CardLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CinemaGUI extends JFrame {
    private JPanel mainPanel = new JPanel();
    private CardLayout m_cardLayout = new CardLayout();

    private MainMenuPanel mainMenuPanel;
    private TicketSellingPanel ticketSellingPanel;
    private MovieRatingPanel movieRatingPanel;
    private AdminLoginPanel adminLoginPanel;

    public static void main(String[] args) {
        CinemaGUI cinemaGUI = new CinemaGUI();
    }

    CinemaGUI() {
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // card layout for the main window
        mainPanel.setLayout(m_cardLayout);

        // tabbed pane 0 is the main menu panel
        mainMenuPanel = new MainMenuPanel(this, getSize().width, getSize().height);
        mainPanel.add("Ana Menü", mainMenuPanel);

        // tabbed pane 1 is the movie selection panel
        ticketSellingPanel = new TicketSellingPanel(this, getSize().width, getSize().height);
        mainPanel.add("Film Seç", ticketSellingPanel);

        // tabbed pane 2 is the movie rating panel
        movieRatingPanel = new MovieRatingPanel(this, getSize().width, getSize().height);
        mainPanel.add("Film Değerlendir", movieRatingPanel);

        // tabbed pane 3 is the admin login panel
        adminLoginPanel = new AdminLoginPanel(this, getSize().width, getSize().height);
        mainPanel.add("Admin Girişi", adminLoginPanel);

        showMainMenu();

        // add the main panel to the frame
        add(mainPanel);

        setVisible(true);
    }

    public void showMainMenu() {
        m_cardLayout.show(mainPanel, "Ana Menü");
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
            JButton button1 = new JButton("Film Seç");
            button1.setBounds(buttonX - (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button1.addActionListener(e -> {
                m_cardLayout.show(mainPanel, "Film Seç");
                ticketSellingPanel.onVisible();
            });
            add(button1);

            JButton button2 = new JButton("Film Değerlendir");
            button2.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
            button2.addActionListener(e -> m_cardLayout.show(mainPanel, "Film Değerlendir"));
            add(button2);

            JButton button3 = new JButton("Admin Girişi");
            button3.setBounds(buttonX + (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button3.addActionListener(e -> {
                String password = JOptionPane.showInputDialog(this, "Enter password:");
                if (isValidPassword(password)) {
                    m_cardLayout.show(mainPanel, "Admin Girişi");
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!");
                }
            });
            add(button3);
        }
    }

    private class AdminLoginPanel extends JPanel {

        public AdminLoginPanel(CinemaGUI cinemaGUI, int width, int height) {
            setLayout(new BorderLayout());

            // Create a toolbar
            JToolBar toolbar = new JToolBar();
            toolbar.setFloatable(false);
            add(toolbar, BorderLayout.NORTH);

            // Create a back button
            JButton backButton = new JButton();
            backButton.setText("Back");
            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cinemaGUI.showMainMenu();
                }
            });
            toolbar.add(backButton);

            // Create a label with "admin panel" text
            JLabel label = new JLabel("Admin Panel");
            toolbar.add(label);
        }
    }

    private boolean isValidPassword(String password) {
        return password.equals("admin");
    }
}