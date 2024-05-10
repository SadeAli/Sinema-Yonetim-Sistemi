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
import javax.swing.JToolBar;
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

import java.util.ArrayList;
import java.util.List;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;



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

            private Movie selectedMovie;

            private JPanel mainPanel = new JPanel();
            private JScrollPane scrollPane = new JScrollPane(mainPanel); 
            
            public ScreeningRoomManagementPanel() {
                setLayout(new BorderLayout());
                add(scrollPane, BorderLayout.CENTER);
                mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
                scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

                JComboBox<Movie> movieCombobox = new JComboBox<>();
                movieCombobox.setRenderer(new DefaultListCellRenderer() {
                
                @Override
                public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Movie) {
                        Movie movie = (Movie) value;

                        String text = String.format("%-70s%-30s%-6s", movie.getName(), "rating: " + movie.getRating(), "duration: " + movie.getDuration());
                        setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
                        
                        setText(text);
                    }
                    return this;
                }
                });
                
                List<Movie> movies = Movie.getAllMovies();
                selectedMovie = movies.get(0);

                for (Movie m: movies) {
                    movieCombobox.addItem(m);
                }

                movieCombobox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        selectedMovie = (Movie) movieCombobox.getSelectedItem();
                        onVisible();
                    }
                });

                add(movieCombobox, BorderLayout.NORTH);
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
                    assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));
                    add(assignmentPanel);
    
                    JLabel nameLabel = new JLabel("Salon " + screeningRoom.getId());
                    nameLabel.setToolTipText("Salon " + screeningRoom.getId() + " - " + screeningRoom.getSeatRowCount() + "x" + screeningRoom.getSeatColCount() + " koltuk");
                    assignmentPanel.add(nameLabel);
    
                    List<Session> sessionList = null;
                    // get sessions for the screening room
                    List<FilterCondition> filterConditions = new ArrayList<>();
                    filterConditions.add(new FilterCondition("screeningRoomId", screeningRoom.getId(), FilterCondition.Relation.EQUALS));
                    filterConditions.add(new FilterCondition("date", LocalDate.now(), FilterCondition.Relation.GREATER_THAN_OR_EQUALS));
                    filterConditions.add(new FilterCondition("date", LocalDate.now().plusDays(30), FilterCondition.Relation.LESS_THAN));
                    try {
                        sessionList = DatabaseManager.getRowsFilteredAndSortedBy(Session.class, filterConditions, "date", false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
    
                    // get sessions for each day
                    Session sessionArray[] = new Session[30];

                    for (Session s : sessionList) {
                        sessionArray[(int)(ChronoUnit.DAYS.between(LocalDate.now(), s.getDate()))] = s;
                    }
    
                    // get movie names for each session
                    Movie movieArray[] = new Movie[30];
                    for (int i = 0; i < 30; i++) {
                        if (sessionArray[i] != null) {
                            filterConditions = new ArrayList<>();
                            filterConditions.add(new FilterCondition("id", sessionArray[i].getMovieId(), FilterCondition.Relation.EQUALS));
                            try {
                                movieArray[i] = DatabaseManager.getRowsFilteredAndSortedBy(Movie.class, filterConditions, "id", true).get(0);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
    
                    // create buttons for each day
                    for (int i = 0; i < 30; i++) {
                        JButton button = new JButton();
    
                        final int dayIndex = i;
                        
                        button.setText(LocalDate.now().plusDays(i).getMonthValue() + "/" + LocalDate.now().plusDays(i).getDayOfMonth());
                        Movie movie = movieArray[dayIndex];
                        final String movieName = movie == null ? "Boş" : movie.getName();
                        button.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                if (screeningRoom.addMovieToDate(LocalDate.now().plusDays(dayIndex), selectedMovie.getId()))
                                    System.out.println("added movie " + selectedMovie.getId() + " to date " + LocalDate.now().plusDays(dayIndex));
                                else
                                    System.out.println("unable to add movie " + selectedMovie.getId() + " to date " + LocalDate.now().plusDays(dayIndex));
                                onVisible();
                            }
                        });
    
                        button.setToolTipText(movieName);
    
                        assignmentPanel.add(button);
                    }
                }
            }
        }
    }
}
