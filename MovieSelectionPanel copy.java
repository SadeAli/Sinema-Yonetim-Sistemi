package gui;

import javax.swing.JSpinner.DateEditor;

import cinema.Movie;
import cinema.Session;

import database.DatabaseManager;
import database.FilterCondition;
import database.FilterCondition.Relation;

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
    private JPanel movieContainerPanel = new JPanel();
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

        // panel where the movies will be displayed
        movieContainerPanel = new JPanel(new BoxLayout(movieContainerPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(
                movieContainerPanel,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        // border layout allows for a toolbar at the top and a scrollable list of movies
        // at the center
        setLayout(new BorderLayout());

        // add a toolbar to the window for search and filter options etc.
        JToolBar toolbar = new MovieSelectionToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        // create the scroll pane for the movie list
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);
        add(scrollPane, BorderLayout.CENTER);
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

    public class MovieSelectionToolbar extends JToolBar {

        private JPanel toolbarPanel = new JPanel();
        private JButton backButton = new JButton("Back");
        private JDialog filterWindow = new FilterPopup();
        private JButton filterButton = new JButton("Filter");

        MovieSelectionToolbar(MovieSelectionPanel parent) {
            setFloatable(false);

            toolbarPanel.setLayout(new BorderLayout());

            toolbarPanel.add(filterButton, BorderLayout.EAST);
            toolbarPanel.add(backButton, BorderLayout.WEST);

            add(toolbarPanel);

            backButton.addActionListener(e -> {
                parent.goBack();
            });

            filterButton.addActionListener(e -> {
                filterWindow.setVisible(true);
            });
        }

        private class FilterPopup extends JDialog {
            private JTextField searchField = new JTextField();
            private SpinnerDateModel dateModel = new SpinnerDateModel();
            private JSpinner dateSpinner = new JSpinner(dateModel);
            private DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yy");
            private JPanel buttonPanel = new JPanel();

            private JComboBox<String> sortComboBox = new JComboBox<>(new String[] {
                    "Sort by Name",
                    "Sort by Rating",
                    "Sort by Release Date"
            });

            FilterPopup() {
                setSize(300, 300);
                setVisible(false);
                setResizable(false);
                setLocationRelativeTo(null);
                setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

                // add spinner for date filter
                dateModel.setCalendarField(Calendar.DAY_OF_MONTH);
                dateSpinner.setPreferredSize(new Dimension(300, 50));
                dateSpinner.setEditor(editor);

                // add text field for name filter
                searchField.setDragEnabled(false);
                searchField.setPreferredSize(new Dimension(300, 50));

                buttonPanel.setLayout(new BorderLayout());
                buttonPanel.setPreferredSize(new Dimension(300, 10));

                JButton applyButton = new JButton("Apply");
                JButton cancelButton = new JButton("Cancel");
                buttonPanel.add(applyButton, BorderLayout.EAST);
                buttonPanel.add(cancelButton, BorderLayout.WEST);

                add(buttonPanel);
                add(sortComboBox);
                add(dateSpinner);
                add(searchField);

                cancelButton.addActionListener(e -> {
                    setVisible(false);
                });

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
                });
            }
        }
    }
}
