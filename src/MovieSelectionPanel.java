import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.awt.event.*;
import java.time.LocalDate;

/**
 * Represents a window for selecting movies.
 * 
 * This window displays a list of movies and provides options for searching, filtering, and sorting the movies.
 */
public class MovieSelectionPanel extends JPanel {

    private CinemaGUI parent;
    private int width;

    // panel where the movies will be displayed
    private JPanel movieListingPanel;
    private JScrollPane scrollPane;

    // list of movies
    private LinkedList<Movie> movieList;
    private LocalDate dateFilter = LocalDate.now();

    /**
     * Constructor for the movie selection window.
     * 
     * @param width         The width of the window.
     * @param height        The height of the window.
     * @param unitIncrement The scroll speed of the window.
     */
    MovieSelectionPanel(CinemaGUI parent, int width, int height, int unitIncrement) {
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

        movieList = Movie.getMovies();
    }
 
    /**
     * Creates a panel for a movie.
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
                // handle movie selection
                System.out.println("Selected movie: " + movie.getName() + "\nDate: " + dateFilter.toString());
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
        updateMovies();
    }

    /**
     * Sorts the movies by rating.
     */
    public void sortMoviesByRating() {
        Collections.sort(movieList, Comparator.comparing(Movie::getRating).reversed());
        updateMovies();
    }

    /**
     * Sorts the movies by release date.
     */
    public void sortMoviesByReleaseDate() {
        Collections.sort(movieList, Comparator.comparing(Movie::getRelease).reversed());
        updateMovies();
    }

    public void reverseOrder() {
        Collections.reverse(movieList);
        updateMovies();
    }

    /**
     * Updates the layout of the movie selection window.
     */
    public void updateMovies() {
        movieListingPanel.removeAll();

        for (Movie movie : movieList) {
            JPanel moviePanel = createMovieBanner(movie);
            movieListingPanel.add(moviePanel);
        }

        movieListingPanel.revalidate();
        movieListingPanel.repaint();
        movieListingPanel.updateUI();
    }

    public void goBack() {
        parent.updateState("Ana Menü");
    }
}
