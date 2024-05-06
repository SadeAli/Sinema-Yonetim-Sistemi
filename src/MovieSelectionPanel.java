import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.util.List; // Add this import statement
import javax.swing.JToolBar;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.util.Collections;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JDialog;
import java.awt.BorderLayout;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner;
import java.util.Calendar;


/**
 * Represents a window for selecting movies.
 * 
 * This window displays a list of movies and provides options for searching,
 * filtering, and sorting the movies.
 */
public class MovieSelectionPanel extends JPanel {

    private TicketSellingPanel parent;
    private int width;

    // panel where the movies will be displayed
    private JPanel movieListingPanel;
    private JScrollPane scrollPane;

    // list of movies
    private List<Movie> movieList;
    private LocalDate dateFilter = LocalDate.now();

    /**
     * Constructor for the movie selection window.
     * 
     * @param width         The width of the window.
     * @param height        The height of the window.
     * @param unitIncrement The scroll speed of the window.
     */
    MovieSelectionPanel(TicketSellingPanel parent, int width, int height, int unitIncrement) {
        this.parent = parent;
        this.width = width;

        setLayout(new BorderLayout());

        // add a toolbar to the window for search and filter options etc.
        JToolBar toolbar = new MovieSelectionToolbar(this);
        add(toolbar, BorderLayout.NORTH);

        // panel which will contain the movie panels
        movieListingPanel = new JPanel();
        movieListingPanel.setLayout(new BoxLayout(movieListingPanel, BoxLayout.Y_AXIS));

        // create the scroll pane for the movie list
        scrollPane = new JScrollPane(movieListingPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);
        scrollPane.setPreferredSize(new Dimension(300, 300)); // set a preferred size
        add(scrollPane, BorderLayout.CENTER);

        updateMovieList();
        reapintMoviePanels();
    }

    /**
     * Creates a panel for a movie.
     * 
     * @param movie The movie to create the panel for.
     * @return The created panel.
     */
    private JPanel createMovieBanner(Movie movie) {
        JPanel moviePanel = new JPanel();
        moviePanel.setPreferredSize(new Dimension(width - 50, 80));
        moviePanel.setBackground(Color.WHITE);
        moviePanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        moviePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                parent.selectMovie(movie, dateFilter);
            }
        });

        // add movie name
        JLabel movieName = new JLabel(movie.getName());
        moviePanel.add(movieName);

        // add movie rating
        JLabel movieRating = new JLabel("Rating: " + movie.getRating());
        moviePanel.add(movieRating);

        // add movie release date
        JLabel movieRelease = new JLabel("Release: " + movie.getRelease().toString());
        moviePanel.add(movieRelease);

        return moviePanel;
    }

    /**
     * Sorts the movies by name.
     */
    public void sortMoviesByName() {
        Collections.sort(movieList, Comparator.comparing(Movie::getName));
        reapintMoviePanels();
    }

    /**
     * Sorts the movies by rating.
     */
    public void sortMoviesByRating() {
        Collections.sort(movieList, Comparator.comparing(Movie::getRating).reversed());
        reapintMoviePanels();
    }

    /**
     * Sorts the movies by release date.
     */
    public void sortMoviesByReleaseDate() {
        Collections.sort(movieList, Comparator.comparing(Movie::getRelease).reversed());
        reapintMoviePanels();
    }

    public void reverseOrder() {
        Collections.reverse(movieList);
        reapintMoviePanels();
    }

    private void updateMovieList() {
        movieList = Movie.getAllMovies();
    }

    /**
     * Updates the layout of the movie selection window.
     */
    public void reapintMoviePanels() {
        movieListingPanel.removeAll();

        for (Movie movie : movieList) {
            JPanel moviePanel = createMovieBanner(movie);
            movieListingPanel.add(moviePanel);
        }

        movieListingPanel.revalidate();
        movieListingPanel.repaint();
    }

    public void goBack() {
        parent.showMainMenu();
    }

    public void onVisible() {
        movieList = Movie.getAllMovies();
    }

    public class MovieSelectionToolbar extends JToolBar {

        private MovieSelectionPanel parent;

        MovieSelectionToolbar(MovieSelectionPanel parent) {
            this.parent = parent;
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
                add(dateSpinner);

                searchField = new JTextField();
                searchField.setDragEnabled(false);
                searchField.setPreferredSize(new Dimension(300, 50));
                add(searchField);

                sortComboBox = new JComboBox<>();
                sortComboBox.addItem("Sort by Name");
                sortComboBox.addItem("Sort by Rating");
                sortComboBox.addItem("Sort by Release Date");
                add(sortComboBox);

                sortComboBox.addActionListener(e -> {
                    // TODO:
                });

                JPanel buttonPanel = new JPanel();
                buttonPanel.setLayout(new BorderLayout());
                buttonPanel.setPreferredSize(new Dimension(300, 10));

                JButton applyButton = new JButton("Apply");
                applyButton.addActionListener(e -> {
                    String selectedSortOption = (String) sortComboBox.getSelectedItem();

                    switch (selectedSortOption) {
                        case "Sort by Name":
                            parent.sortMoviesByName();
                            break;
                        case "Sort by Rating":
                            parent.sortMoviesByRating();
                            break;
                        case "Sort by Release Date":
                            parent.sortMoviesByReleaseDate();
                            break;
                    }
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
