package gui.mainPanels.adminPanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Session;
import database.DatabaseManager;
import database.FilterCondition;
import gui.CinemaGUI;

public class ScreeningRoomManagementPanel extends JPanel {
    private List<ScreeningRoom> screeningRooms;

    private Movie selectedMovie;

    private JPanel mainPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(mainPanel);

    public ScreeningRoomManagementPanel(List<ScreeningRoom> screeningRooms) {
        this.screeningRooms = screeningRooms;
        
        setLayout(new BorderLayout());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JComboBox<Movie> movieCombobox = new JComboBox<>();

        List<Movie> movies = Movie.getAllMovies();
        selectedMovie = movies.get(0);
        for (Movie m : movies) {
            movieCombobox.addItem(m);
        }

        add(scrollPane, BorderLayout.CENTER);
        add(movieCombobox, BorderLayout.NORTH);

        movieCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedMovie = (Movie) movieCombobox.getSelectedItem();
                repaintDayMoviePanels();
            }
        });

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

    public void repaintDayMoviePanels() {
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
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel nameLabel = new JLabel(String.format("%5s %-3s", "Room ", screeningRoom.getId()));
            JPanel assignmentPanel = new JPanel();

            assignmentPanel.setLayout(new BoxLayout(assignmentPanel, BoxLayout.X_AXIS));

            nameLabel.setToolTipText("Room " + screeningRoom.getId() + " - " + screeningRoom.getSeatRowCount()
                    + "x" + screeningRoom.getSeatColCount() + " koltuk");

            assignmentPanel.add(nameLabel);
            add(assignmentPanel);

            // get sessions for the screening room
            List<Session> sessionList = null;
            try {
                sessionList = DatabaseManager.getRowsFilteredAndSortedBy(Session.class,
                        List.of(new FilterCondition("screeningRoomId", screeningRoom.getId(),
                                FilterCondition.Relation.EQUALS),
                                new FilterCondition("date", LocalDate.now(),
                                        FilterCondition.Relation.GREATER_THAN_OR_EQUALS),
                                new FilterCondition("date", LocalDate.now().plusDays(30),
                                        FilterCondition.Relation.LESS_THAN)),
                        "date", false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // get sessions for each day
            Session sessionArray[] = new Session[30];

            for (Session s : sessionList) {
                sessionArray[(int) (ChronoUnit.DAYS.between(LocalDate.now(), s.getDate()))] = s;
            }

            // get movie names for each session
            Movie movieArray[] = new Movie[30];
            for (int i = 0; i < 30; i++) {
                if (sessionArray[i] != null) {
                    try {
                        movieArray[i] = DatabaseManager
                                .getRowsFilteredAndSortedBy(Movie.class,
                                        List.of(new FilterCondition("id", sessionArray[i].getMovieId(),
                                                FilterCondition.Relation.EQUALS)),
                                        "id", true)
                                .get(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // create buttons for each day
            for (int i = 0; i < 30; i++) {
                assignmentPanel.add(new DayMovieButtonPanel(i, movieArray[i], screeningRoom));
            }
        }

        private class DayMovieButtonPanel extends JButton {
            public DayMovieButtonPanel(final int index, Movie movie, ScreeningRoom screeningRoom) {

                JButton button = new JButton();

                button.setText(LocalDate.now().plusDays(index).getMonthValue() + "/"
                        + LocalDate.now().plusDays(index).getDayOfMonth());

                if (movie != null) {
                    button.setToolTipText(movie.getName());
                    this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                } else {
                    button.setToolTipText("Boş");
                    this.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
                }

                this.setSize(new Dimension(100, 100));
                this.setMargin(new Insets(50, 50, 50, 50));
                this.add(button);

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        if (screeningRoom.addMovieToDate(LocalDate.now().plusDays(index),
                                selectedMovie.getId()))
                            System.out.println("added movie " + selectedMovie.getId() + " to date "
                                    + LocalDate.now().plusDays(index));
                        else
                            System.out.println("unable to add movie " + selectedMovie.getId() + " to date "
                                    + LocalDate.now().plusDays(index));
                        repaintDayMoviePanels();
                    }
                });
            }
        }
    }
}