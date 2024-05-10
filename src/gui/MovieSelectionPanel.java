package gui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner.DateEditor;

import cinema.Movie;
import cinema.Session;

import database.DatabaseManager;
import database.FilterCondition;
import database.FilterCondition.Relation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import java.sql.SQLException;

import java.time.LocalDate;

import java.util.ArrayList;
import java.util.Calendar;
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

        JPanel toolbar = new MovieSelectionToolbar();
        JScrollPane scrollPane = new JScrollPane(movieContainerPanel);

        // panel which will contain the movie panels
        movieContainerPanel.setLayout(new BoxLayout(movieContainerPanel, BoxLayout.Y_AXIS));

        // create the scroll pane for the movie list
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(5);

        add(toolbar, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private class MovieBanner extends JPanel {
        public MovieBanner(Movie movie) {
            // colors and borders
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            // size
            setPreferredSize(new Dimension(0, 200));
            setMaximumSize(new Dimension(5000, 200));

            // content
            JLabel movieName = new JLabel(movie.getName());
            JLabel movieRating = new JLabel("Rating: " + movie.getRating());
            JLabel movieRelease = new JLabel("Release: " + movie.getReleaseDate().toString());

            // layout
            add(movieName);
            add(movieRating);
            add(movieRelease);

            // actions
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    parent.selectMovie(movie, dateQuery);
                }
            });
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

    public void onVisible() {
        applyFilter();
    }

    public void applyFilter() {
        List<Session> sessions = new ArrayList<>();
        
        try {
            // get movies
            movieList = DatabaseManager.getRowsFilteredAndSortedBy(
                    Movie.class,
                    nameQuery.isEmpty() ? List.of()
                            : List.of(new FilterCondition("name", nameQuery, Relation.LIKE)),
                    sortOrder,
                    ascending);

            // get sessions
            sessions = DatabaseManager.getRowsFilteredAndSortedBy(
                    Session.class,
                    List.of(new FilterCondition("date", dateQuery, Relation.EQUALS)),
                    "startTime",
                    true);

        } catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException ex) {
            ex.printStackTrace();
        }

        List<Movie> availableMovies = new ArrayList<>();
        for (Movie movie : movieList) {
            for (Session session : sessions) {
                if (session.getMovieId() == movie.getId()) {
                    availableMovies.add(movie);
                    // we found out that movie exists
                    // so we don't need to check other sessions
                    // continue to next movie
                    break;
                }
            }
        }

        movieList = availableMovies;

        reapintMoviePanels();
    }

    public class MovieSelectionToolbar extends JPanel {

        MovieSelectionToolbar() {
            setLayout(new BorderLayout());

            JButton backButton = new JButton("Back");
            JButton filterButton = new JButton("Filter");

            add(backButton, BorderLayout.WEST);
            add(filterButton, BorderLayout.EAST);

            backButton.addActionListener(e -> {
                parent.goBack();
            });

            JDialog filterWindow = new FilterPopup();
            filterButton.addActionListener(e -> {
                filterWindow.setVisible(true);
            });
        }

        private class FilterPopup extends JDialog {
            FilterPopup() {
                setSize(300, 300);
                setVisible(false);
                setResizable(false);
                setLocationRelativeTo(null);
                setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

                // components
                JTextField searchField = new JTextField();
                SpinnerDateModel dateModel = new SpinnerDateModel();
                JSpinner dateSpinner = new JSpinner(dateModel);
                DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yy");
                JPanel buttonPanel = new JPanel();

                JComboBox<SortOption> sortComboBox = new JComboBox<>(new SortOption[] {
                        new SortOption("Sort by Name", "name", true),
                        new SortOption("Sort by Rating", "rating", false),
                        new SortOption("Sort by Release Date", "releaseDate", false)
                });

                // add spinner for date filter
                dateModel.setCalendarField(Calendar.DAY_OF_MONTH);
                dateSpinner.setPreferredSize(new Dimension(300, 50));
                dateSpinner.setEditor(editor);

                // add text field for name filter
                searchField.setDragEnabled(false);
                searchField.setPreferredSize(new Dimension(300, 50));

                // a small panel for the apply and cancel buttons
                buttonPanel.setLayout(new BorderLayout());
                buttonPanel.setMaximumSize(new Dimension(5000, 20));
                JButton applyButton = new JButton("Apply");
                JButton cancelButton = new JButton("Cancel");
                buttonPanel.add(applyButton, BorderLayout.EAST);
                buttonPanel.add(cancelButton, BorderLayout.WEST);

                // add components to the dialog
                add(dateSpinner);
                add(searchField);
                add(sortComboBox);
                add(buttonPanel);

                // actions
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
                    final SortOption selectedSort = (SortOption) sortComboBox.getSelectedItem();
                    sortOrder = selectedSort.getSort();
                    ascending = selectedSort.isAscending();

                    applyFilter();

                    setVisible(false);
                });
            }

            private class SortOption {
                private final String name;
                private final String sort;
                private final boolean ascending;

                public SortOption(String name, String sort, boolean ascending) {
                    this.name = name;
                    this.sort = sort;
                    this.ascending = ascending;
                }

                public String getSort() {
                    return sort;
                }

                public boolean isAscending() {
                    return ascending;
                }

                @Override
                public String toString() {
                    return name;
                }
            }
        }
    }
}
