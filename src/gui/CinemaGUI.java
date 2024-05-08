package gui;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JButton;

import java.awt.CardLayout;
import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalTime;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Session;
import database.DatabaseManager;
import database.FilterCondition;
import database.FilterCondition.Relation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;


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
        CinemaGUI cinemaGUI = new CinemaGUI();
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
        mainPanel.add("Ana Menü", mainMenuPanel);
        mainPanel.add("Film Seç", ticketSellingPanel);
        mainPanel.add("Film Değerlendir", movieRatingPanel);
        mainPanel.add("Admin Girişi", adminPanel);

        showMainMenu();
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

    private class AdminPanel extends JPanel {

        ScreeningRoomManagementPanel screeningRoomManagementPanel = new ScreeningRoomManagementPanel();
        JPanel movieManagementPanel = new JPanel();

        CardLayout m_cardLayout = new CardLayout();
        List<ScreeningRoom> screeningRooms = ScreeningRoom.getAllScreeningRooms();
        JTabbedPane tabbedPane = new JTabbedPane();

        public AdminPanel(CinemaGUI cinemaGUI, int width, int height) {
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

            // Tabbed Pane for the admin panel
            tabbedPane = new JTabbedPane();
            add(tabbedPane, BorderLayout.CENTER);

            // tabbed pane 0 is the movie-hall management panel
            tabbedPane.add("Salon Yönetimi", screeningRoomManagementPanel);
            tabbedPane.add("Film Yönetimi", movieManagementPanel);
        }

        public void onVisible() {
            screeningRoomManagementPanel.onVisible();
        }

        private class ScreeningRoomManagementPanel extends JPanel {

            private JPanel mainPanel = new JPanel();
            private JScrollPane scrollPane = new JScrollPane(mainPanel); 
            
            public ScreeningRoomManagementPanel() {
                setLayout(new BorderLayout());
                add(scrollPane, BorderLayout.CENTER);
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

                screeningRooms = ScreeningRoom.getAllScreeningRooms();
            }

            public void onVisible() {
                screeningRooms = ScreeningRoom.getAllScreeningRooms();
                mainPanel.removeAll();
                for (ScreeningRoom screeningRoom : screeningRooms) {
                    DayMoviePanel dayMoviePanel = new DayMoviePanel(screeningRoom);
                    mainPanel.add(dayMoviePanel);
                }
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        }

        private class DayMoviePanel extends JPanel {
            public DayMoviePanel(ScreeningRoom screeningRoom) {

                setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
                
                JPanel assignmentPanel = new JPanel();
                assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));
                add(assignmentPanel);

                JLabel nameLabel = new JLabel("Salon " + screeningRoom.getId());
                nameLabel.setToolTipText("Salon " + screeningRoom.getId() + " - " + screeningRoom.getSeatRowCount() + "x" + screeningRoom.getSeatColCount() + " koltuk");
                assignmentPanel.add(nameLabel);

                List<Session> sessionList = null;

                List<FilterCondition> filterConditions = new ArrayList<>();
                filterConditions.add(new FilterCondition("id", screeningRoom.getId(), FilterCondition.Relation.EQUALS));
                filterConditions.add(new FilterCondition("date", LocalDate.now(), FilterCondition.Relation.GREATER_THAN_OR_EQUALS));
                filterConditions.add(new FilterCondition("date", LocalDate.now().plusDays(30), FilterCondition.Relation.LESS_THAN));
                try {
                    sessionList = DatabaseManager.getRowsFilteredAndSortedBy(Session.class, filterConditions, "date", true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Session sessionArray[] = new Session[30];
                for (Session s : sessionList) {
                    sessionArray[s.getDate().getDayOfYear() - LocalDate.now().getDayOfYear()] = s;
                }

                Movie movieArray[] = new Movie[30];
                for (int i = 0; i < 30; i++) {
                    if (sessionArray[i] != null) {

                        filterConditions = new ArrayList<>();
                        filterConditions.add(new FilterCondition("id", sessionArray[i].getMovieId(), FilterCondition.Relation.EQUALS));
                        try {
                            movieArray[i] = DatabaseManager.getRowsFilteredAndSortedBy(Movie.class, filterConditions, "", true).get(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                for (int i = 0; i < 30; i++) {
                    JButton button = new JButton();

                    final int movieIndex = i;
                    
                    button.setText(LocalDate.now().plusDays(i).getMonthValue() + "/" + LocalDate.now().plusDays(i).getDayOfMonth());
                    Movie movie = movieArray[movieIndex];
                    final String movieName = movie == null ? "Boş" : movie.getName();
                    button.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            System.out.println(movieName);
                        }
                    });

                    button.setToolTipText(movieName);

                    assignmentPanel.add(button);
                }
            }
        }
    }
}
