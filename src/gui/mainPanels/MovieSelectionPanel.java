package gui.mainPanels;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.JSpinner.DateEditor;

import cinema.Genre;
import cinema.Movie;
import cinema.MovieGenre;
import cinema.Session;

import database.DatabaseManager;
import database.FilterCondition;
import database.FilterCondition.Relation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
    List<Genre> selectedGenreList = new ArrayList<>();

    /**
     * Constructor for the movie selection window.
     * 
     * @param width         The width of the window.
     * @param height        The height of the window.
     * @param unitIncrement The scroll speed of the window.
     */
    public MovieSelectionPanel(TicketSellingPanel parent) {
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
        List<MovieGenre> movieGenres = new ArrayList<>();

        try {
            for (Genre genre : selectedGenreList) {
                List<MovieGenre> tmp = DatabaseManager
                        .getRowsFilteredAndSortedBy(MovieGenre.class,
                                List.of(new FilterCondition("genreId", genre.getId(), Relation.EQUALS)), "id", true);
                                
                if (!tmp.isEmpty()) {
                    movieGenres.add(tmp.get(0));
                }

            }

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

        if (!selectedGenreList.isEmpty()) {
            availableMovies = new ArrayList<>();
            for (Movie movie : movieList) {
                boolean hasAllGenres = true;
                for (MovieGenre mg : movieGenres) {
                    if (mg.getMovieId() != movie.getId()) {
                        hasAllGenres = false;
                        break;
                    }
                }

                if (hasAllGenres) {
                    availableMovies.add(movie);
                }
            }

            movieList = availableMovies;
        }

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
            List<Genre> genreSelectionList = Genre.getAllGenres();
            GenreBox genreBox;
            GenreList genreList;

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
                genreList = new GenreList();
                genreBox = new GenreBox();

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
                add(genreBox);
                add(new JScrollPane(genreList));
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

            private class GenreList extends JList<Genre> {
                public GenreList() {
                    super(selectedGenreList.toArray(new Genre[0]));

                    setCellRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                boolean isSelected, boolean cellHasFocus) {
                            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected,
                                    cellHasFocus);
                            if (renderer instanceof JLabel && value instanceof Genre) {
                                ((JLabel) renderer).setText(((Genre) value).getName());
                            }
                            return renderer;
                        }
                    });

                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            int index = locationToIndex(e.getPoint());
                            Genre selectedGenre = getModel().getElementAt(index);
                            if (selectedGenreList.contains(selectedGenre)) {
                                selectedGenreList.remove(selectedGenre);
                                genreSelectionList.add(selectedGenre);

                                setListData(selectedGenreList.toArray(new Genre[0]));
                                genreBox.addItem(selectedGenre);
                            }
                        }
                    });
                }

                @Override
                public Dimension getPreferredScrollableViewportSize() {
                    return new Dimension(300, 100);
                }
            }

            private class GenreBox extends JComboBox<Genre> {
                public GenreBox() {
                    super(Genre.getAllGenres().toArray(new Genre[0]));

                    setRenderer(new DefaultListCellRenderer() {
                        @Override
                        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                boolean isSelected, boolean cellHasFocus) {
                            Component renderer = super.getListCellRendererComponent(list, value, index, isSelected,
                                    cellHasFocus);
                            if (renderer instanceof JLabel && value instanceof Genre) {
                                ((JLabel) renderer).setText(((Genre) value).getName());
                            }
                            return renderer;
                        }
                    });

                    addActionListener(e -> {
                        Genre selectedGenre = (Genre) getSelectedItem();
                        if (selectedGenre != null) {
                            selectedGenreList.add(selectedGenre);
                            genreSelectionList.remove(selectedGenre);

                            genreList.setListData(selectedGenreList.toArray(new Genre[0]));
                            removeItem(selectedGenre);
                        }
                    });
                }
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
