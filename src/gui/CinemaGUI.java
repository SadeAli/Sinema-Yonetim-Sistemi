package gui;

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;

import javax.swing.JOptionPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.JComboBox;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Session;

import database.DatabaseManager;
import database.FilterCondition;

import java.util.List;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CinemaGUI extends JFrame {
    static final int width = 1000;
    static final int height = 800;

    private JPanel mainPanel = new JPanel();
    private CardLayout m_cardLayout = new CardLayout();

    private JPanel mainMenuPanel = new MainMenuPanel(this, width, height);
    private TicketSellingPanel ticketSellingPanel = new TicketSellingPanel(this);
    private JPanel movieRatingPanel = new MovieRatingPanel(this, width, height);
    private AdminPanel adminPanel = new AdminPanel(this);

    public static void main(String[] args) {
        @SuppressWarnings("unused")
        CinemaGUI cinemaGUI = new CinemaGUI();
    }

    CinemaGUI() {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // card layout for the main window
        mainPanel.setLayout(m_cardLayout);

        // tabbed pane 0 is the main menu panel
        mainPanel.add("Ana Menü", mainMenuPanel);
        mainPanel.add("Film Seç", ticketSellingPanel);
        mainPanel.add("Film Değerlendir", movieRatingPanel);
        mainPanel.add("Admin Girişi", adminPanel);

        add(mainPanel);

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
            JButton button2 = new JButton("Film Değerlendir");
            JButton button3 = new JButton("Admin Girişi");

            button1.setBounds(buttonX - (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button2.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
            button3.setBounds(buttonX + (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);

            add(button1);
            add(button2);
            add(button3);

            button1.addActionListener(e -> {
                m_cardLayout.show(mainPanel, "Film Seç");
                ticketSellingPanel.onVisible();
            });

            button2.addActionListener(e -> m_cardLayout.show(mainPanel, "Film Değerlendir"));

            button3.addActionListener(e -> {
                String password = JOptionPane.showInputDialog(this, "Enter password:");
                if (isValidPassword(password)) {
                    m_cardLayout.show(mainPanel, "Admin Girişi");
                    CinemaGUI.this.adminPanel.onVisible();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!");
                }
            });
        }

        private boolean isValidPassword(String password) {
            return password != null && password.equals("admin");
        }
    }

    private class AdminPanel extends JPanel implements OnVisible {

        ScreeningRoomManagementPanel screeningRoomManagementPanel = new ScreeningRoomManagementPanel();
        JPanel movieManagementPanel = new JPanel();

        List<ScreeningRoom> screeningRooms = ScreeningRoom.getAllScreeningRooms();
        JTabbedPane tabbedPane = new JTabbedPane();

        public AdminPanel(CinemaGUI cinemaGUI) {
            setLayout(new BorderLayout());

            // Create a toolbar
            JPanel toolbar = new JPanel();
            JButton backButton = new JButton("Back");
            JLabel label = new JLabel("Admin Panel");

            toolbar.add(backButton);
            toolbar.add(label);

            // Tabbed Pane for the admin panel
            tabbedPane = new JTabbedPane();
            // tabbed pane 0 is the movie-hall management panel
            tabbedPane.add("Salon Yönetimi", screeningRoomManagementPanel);
            tabbedPane.add("Film Yönetimi", movieManagementPanel);

            add(toolbar, BorderLayout.NORTH);
            add(tabbedPane, BorderLayout.CENTER);

            backButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    cinemaGUI.showMainMenu();
                }
            });
        }

        public void onVisible() {
            screeningRoomManagementPanel.onVisible();
        }

        private class ScreeningRoomManagementPanel extends JPanel {

            private Movie selectedMovie;

            private JPanel mainPanel = new JPanel();
            private JScrollPane scrollPane = new JScrollPane(mainPanel);

            public ScreeningRoomManagementPanel() {
                setLayout(new BorderLayout());
                add(scrollPane, BorderLayout.CENTER);
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                JComboBox<Movie> movieCombobox = new JComboBox<>();

                List<Movie> movies = Movie.getAllMovies();
                selectedMovie = movies.get(0);

                for (Movie m : movies) {
                    movieCombobox.addItem(m);
                }

                movieCombobox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedMovie = (Movie) movieCombobox.getSelectedItem();
                        onVisible();
                    }
                });

                add(movieCombobox, BorderLayout.NORTH);

                // custom renderer
                movieCombobox.setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                            boolean isSelected, boolean cellHasFocus) {
                        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        if (value instanceof Movie) {
                            Movie movie = (Movie) value;

                            String text = String.format("%-70s%-30s%-6s", movie.getName(),
                                    "rating: " + movie.getRating(), "duration: " + movie.getDuration());
                            setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

                            setText(text);
                        }
                        return this;
                    }
                });
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

            private class DayMoviePanel extends JPanel {
                public DayMoviePanel(ScreeningRoom screeningRoom) {

                    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

                    JPanel assignmentPanel = new JPanel();
                    JLabel nameLabel = new JLabel("Salon " + screeningRoom.getId());

                    assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));
                    nameLabel.setToolTipText("Salon " + screeningRoom.getId() + " - " + screeningRoom.getSeatRowCount()
                            + "x" + screeningRoom.getSeatColCount() + " koltuk");

                    // get sessions for each day
                    List<Session> sessionList = getSessionForMonth(LocalDate.now(), screeningRoom);
                    Session sessionArray[] = new Session[30];
                    for (Session s : sessionList) {
                        sessionArray[(int) (ChronoUnit.DAYS.between(LocalDate.now(), s.getDate()))] = s;
                    }

                    // get movie names for each session
                    Movie movieArray[] = new Movie[30];
                    for (int i = 0; i < 30; i++) {
                        if (sessionArray[i] != null) {
                            try {
                                movieArray[i] = DatabaseManager.getRowById(Movie.class, sessionArray[i].getMovieId());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    // set buttons for each day
                    // each button contains that day's movie name as tooltip
                    JButton dayButtons[] = new JButton[30];
                    for (int i = 0; i < 30; i++) {
                        // "day/month"
                        final String buttonText = LocalDate.now().plusDays(i).getMonthValue()
                                + "/"
                                + LocalDate.now().plusDays(i).getDayOfMonth();

                        // create a button for each day
                        dayButtons[i] = new JButton(buttonText);

                        // set tooltip text for the button
                        final String movieName = movieArray[i] == null ? "Boş" : movieArray[i].getName();

                        dayButtons[i].setToolTipText(movieName);
                    }

                    assignmentPanel.add(nameLabel);
                    for (JButton b : dayButtons)
                        assignmentPanel.add(b);
                    add(assignmentPanel);

                    // add movie adding feature to the buttons
                    for (int i = 0; i < 30; i++) {
                        final int dayIndex = i;
                        dayButtons[i].addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (screeningRoom.addMovieToDate(LocalDate.now().plusDays(dayIndex),
                                        selectedMovie.getId()))
                                    System.out.println("added movie " + selectedMovie.getId() + " to date "
                                            + LocalDate.now().plusDays(dayIndex));
                                else
                                    System.out.println("unable to add movie " + selectedMovie.getId() + " to date "
                                            + LocalDate.now().plusDays(dayIndex));
                                onVisible();
                            }
                        });
                    }
                }

                private List<Session> getSessionForMonth(LocalDate date, ScreeningRoom screeningRoom) {

                    List<Session> sessionList = null;

                    try {
                        sessionList = DatabaseManager.getRowsFilteredAndSortedBy(
                                Session.class,
                                List.of(
                                        new FilterCondition("screeningRoomId", screeningRoom.getId(),
                                                FilterCondition.Relation.EQUALS),
                                        new FilterCondition("date", LocalDate.now(),
                                                FilterCondition.Relation.GREATER_THAN_OR_EQUALS),
                                        new FilterCondition("date", LocalDate.now().plusDays(30),
                                                FilterCondition.Relation.LESS_THAN)),
                                "date",
                                false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    return sessionList;
                }
            }
        }
    }
}
