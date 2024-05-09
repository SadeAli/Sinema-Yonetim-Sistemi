package gui;

import javax.swing.*;
import javax.swing.JSpinner.DateEditor;

import cinema.Movie;
import cinema.Session;
import database.DatabaseManager;
import database.FilterCondition;
import database.FilterCondition.Relation;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Represents a window for selecting movies.
 * 
 * This window displays a list of movies and provides options for searching,
 * filtering, and sorting the movies.
 */
public class MovieSelectionPanel extends JPanel {

    private TicketSellingPanel parent;

    // panel where the movies will be displayed
    private JPanel movieContainerPanel;
    private JScrollPane scrollPane;

    // list of movies
    private List<Movie> movieList;

    private LocalDate dateQuery = LocalDate.now();
    private String nameQuery = "";
    private String sortOrder = "rating";
    private boolean ascending = false;

    /**
     * Constructor for the movie selection window.
     * 
     * @param width         The width of the window.
     * @param height        The height of the window.
     * @param unitIncrement The scroll speed of the window.
     */
    MovieSelectionPanel(TicketSellingPanel parent) {
        this.parent = parent;

        setLayout(new BorderLayout());

        // add a toolbar to the window for search and filter options etc.
        JToolBar toolbar = new MovieSelectionToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        // panel which will contain the movie panels
        movieContainerPanel = new JPanel();
        movieContainerPanel.setLayout(new BoxLayout(movieContainerPanel, BoxLayout.Y_AXIS));

        // create the scroll pane for the movie list
        scrollPane = new JScrollPane(movieContainerPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class MovieBanner extends JPanel {
        public MovieBanner(Movie movie) {
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            setPreferredSize(new Dimension(0, 200));
            setMaximumSize(new Dimension(5000, 200));
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parent.selectMovie(movie, dateQuery);
                }
            });

            JLabel movieName = new JLabel(movie.getName());
            JLabel movieRating = new JLabel("Rating: " + movie.getRating());
            JLabel movieRelease = new JLabel("Release: " + movie.getReleaseDate().toString());

            add(movieName);
            add(movieRating);
            add(movieRelease);
        }
    }

    public void reapintMoviePanels() {
        movieContainerPanel.removeAll();

        for (Movie movie : movieList) {
            JPanel moviePanel = new MovieBanner(movie);
            movieContainerPanel.add(moviePanel);
        }

        movieContainerPanel.revalidate();
        movieContainerPanel.repaint();
    }

    public void goBack() {
        parent.showMainMenu();
    }

    public void onVisible() {
        applyFilter();
    }

    public void applyFilter() {
        try {
            movieList = DatabaseManager.getRowsFilteredAndSortedBy(
                    Movie.class,
                    nameQuery.isEmpty() ? List.of()
                            : List.of(new FilterCondition("name", nameQuery, Relation.LIKE)),
                    sortOrder,
                    ascending);

            List<FilterCondition> sessionFilters = new ArrayList<>();
            sessionFilters.add(new FilterCondition("date", dateQuery, Relation.EQUALS));
            List<Session> sessions = DatabaseManager.getRowsFilteredAndSortedBy(
                    Session.class,
                    sessionFilters,
                    "startTime",
                    true);

            List<Movie> availableMovies = new ArrayList<>();

            for (Movie movie : movieList) {
                for (Session session : sessions) {
                    if (session.getMovieId() == movie.getId()) {
                        availableMovies.add(movie);
                        break;
                    }
                }
            }

            movieList = availableMovies;

        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        reapintMoviePanels();
    }

    public class MovieSelectionToolbar extends JToolBar {

        MovieSelectionToolbar(MovieSelectionPanel parent) {
            setFloatable(false);

            JPanel toolbarPanel = new JPanel();
            toolbarPanel.setLayout(new BorderLayout());

            // back button
            JButton backButton = new JButton("Back");
            backButton.addActionListener(e -> {
                parent.goBack();
            });
            toolbarPanel.add(backButton, BorderLayout.WEST);

            // filter window
            JDialog filterWindow = new FilterPopup();

            // open a new window when filter button is clicked
            JButton filterButton = new JButton("Filter");
            filterButton.addActionListener(e -> {
                filterWindow.setVisible(true);
            });
            toolbarPanel.add(filterButton, BorderLayout.EAST);

            add(toolbarPanel);
        }

        private class FilterPopup extends JDialog {
            private JTextField searchField;
            private JComboBox<String> sortComboBox;
            private SpinnerDateModel dateModel;

            FilterPopup() {
                setSize(300, 300);
                setVisible(false);
                setResizable(false);
                setLocationRelativeTo(null);
                setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

                // add spinner for date filter
                dateModel = new SpinnerDateModel();
                dateModel.setCalendarField(Calendar.DAY_OF_MONTH);
                JSpinner dateSpinner = new JSpinner(dateModel);
                dateSpinner.setPreferredSize(new Dimension(300, 50));
                DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yy");
                dateSpinner.setEditor(editor);
                add(dateSpinner);

                searchField = new JTextField();
                searchField.setDragEnabled(false);
                searchField.setPreferredSize(new Dimension(300, 50));
                add(searchField);

                sortComboBox = new JComboBox<>();

                for (String option : new String[] {
                        "Sort by Name",
                        "Sort by Rating",
                        "Sort by Release Date"
                }) {
                    sortComboBox.addItem(option);
                }
                add(sortComboBox);

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BorderLayout());
                buttonPanel.setPreferredSize(new Dimension(300, 10));

                JButton applyButton = new JButton("Apply");
                applyButton.addActionListener(e -> {

                    // dateQuery
                    dateQuery = dateModel.getDate().toInstant().atZone(Calendar.getInstance().getTimeZone().toZoneId())
                            .toLocalDate();

                    // nameQuery
                    nameQuery = searchField.getText();

                    // sortOrder
                    switch ((String) sortComboBox.getSelectedItem()) {
                        case "Sort by Name":
                            sortOrder = "name";
                            ascending = true;
                            break;
                        case "Sort by Rating":
                            sortOrder = "rating";
                            ascending = false;
                            break;
                        case "Sort by Release Date":
                            sortOrder = "releaseDate";
                            ascending = false;
                            break;
                    }

                    applyFilter();

                    setVisible(false);
                });

                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(e -> {
                    setVisible(false);
                });

                buttonPanel.add(applyButton, BorderLayout.EAST);
                buttonPanel.add(cancelButton, BorderLayout.WEST);
                add(buttonPanel);
            }
        }
    }
}
