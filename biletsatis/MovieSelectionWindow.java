import javax.swing.*;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Function;

/**
 * Represents a window for selecting movies.
 * This window displays a list of movies and provides options for searching,
 * filtering, and sorting the movies.
 */
public class MovieSelectionWindow {

    private int width, height, unitIncrement;

    // enum for sorting options
    private enum SortOption {
        NAME, RATING, RELEASE_DATE
    }

    SortOption sortOption = SortOption.NAME;

    // panel where the movies will be displayed
    private JPanel panel;

    // movie count to update layout
    int movieCount = 0;

    // list of movies
    List<Movie> movieList = new ArrayList<>();
    HashMap<String, Button> buttonMap = new HashMap<>();

    /**
     * Constructor for the movie selection window.
     * 
     * @param width         The width of the window.
     * @param height        The height of the window.
     * @param unitIncrement The scroll speed of the window.
     */
    MovieSelectionWindow(int width, int height, int unitIncrement) {
        this.width = width;
        this.height = height;
        this.unitIncrement = unitIncrement;

        // create main window
        JFrame frame = createFrame(width, height);
        panel = new JPanel();
        // set the layout manager for the panel to vertical
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // add a toolbar to the window for search and filter options etc.
        JToolBar toolbar = new JToolBar();
        frame.add(toolbar, BorderLayout.NORTH);

        // add a search field to the toolbar
        JTextField searchField = new JTextField();

        // prevent search field changing with mouse
        searchField.setDragEnabled(false);
        searchField.setDropMode(DropMode.INSERT);

        searchField.setPreferredSize(new Dimension(200, 30));
        toolbar.add(searchField);

        // add reverse button for sorting
        JButton reverseButton = new JButton("Reverse Order");
        reverseButton.addActionListener(e -> {
            Collections.reverse(movieList);
            updateLayout();
        });
        toolbar.add(reverseButton);

        // create a selection box for sorting options
        JComboBox<String> sortComboBox = addSortComboBox(toolbar);

        // make the panel scrollable vertically
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // set the scroll speed
        scrollPane.getVerticalScrollBar().setUnitIncrement(unitIncrement);

        // add the scrollable pane to the main window
        frame.add(scrollPane, BorderLayout.CENTER);

        // make the window visible
        frame.setVisible(true);
    }

    /**
     * Adds a movie to the movie selection window.
     * 
     * @param movie The movie to be added.
     */
    public void addMovie(Movie movie) {
        movieList.add(movie);
        movieCount++;
        Button button = new Button(movie.getName());
        button.setPreferredSize(new Dimension(width - 50, 50));
        buttonMap.put(movie.getName(), button);
        reorderMovies();
    }

    /**
     * Removes a movie from the movie selection window.
     * 
     * @param movieName The name of the movie to be removed.
     */
    public void removeMovie(String movieName) {
        for (Movie movie : movieList) {
            if (movie.getName().equals(movieName)) {
                movieList.remove(movie);
                movieCount--;
                panel.remove(buttonMap.get(movieName));
                buttonMap.remove(movieName);
                break;
            }
        }
    }

    /**
     * Creates a frame for the movie selection window.
     * 
     * @param width  The width of the frame.
     * @param height The height of the frame.
     * @return The created frame.
     */
    private JFrame createFrame(int width, int height) {
        JFrame frame = new JFrame("Cinema");
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLocationRelativeTo(null);
        return frame;
    }

    /**
     * Adds a sort combo box to the toolbar.
     * 
     * @param toolbar The toolbar to add the combo box to.
     * @return The created combo box.
     */
    private JComboBox<String> addSortComboBox(JToolBar toolbar) {
        JComboBox<String> sortComboBox = new JComboBox<>();
        sortComboBox.addItem("Sort by Name");
        sortComboBox.addItem("Sort by Rating");
        sortComboBox.addItem("Sort by Release Date");

        toolbar.add(sortComboBox);

        sortComboBox.addActionListener(e -> {
            String selectedSortOption = (String) sortComboBox.getSelectedItem();

            switch (selectedSortOption) {
                case "Sort by Name":
                    sortOption = SortOption.NAME;
                    break;
                case "Sort by Rating":
                    sortOption = SortOption.RATING;
                    break;
                case "Sort by Release Date":
                    sortOption = SortOption.RELEASE_DATE;
                    break;
            }
            
            reorderMovies();
        });

        return sortComboBox;
    }

    /**
     * Reorders the movies based on the selected sort option.
     */
    public void reorderMovies() {
        switch (sortOption) {
            case SortOption.NAME:
                Collections.sort(movieList, Comparator.comparing(Movie::getName));
                break;
            case SortOption.RATING:
                Collections.sort(movieList, Comparator.comparing(Movie::getRating).reversed());
                break;
            case SortOption.RELEASE_DATE:
                Collections.sort(movieList, Comparator.comparing(Movie::getRelease).reversed());
                break;
            default:
                System.out.println("Invalid sort option");
                break;
        }

        updateLayout();
    }

    /**
     * Updates the layout of the movie selection window.
     */
    private void updateLayout() {
        panel.removeAll();

        for (Movie movie : movieList) {
            panel.add(buttonMap.get(movie.getName()));
        }

        panel.revalidate();
    }

}
