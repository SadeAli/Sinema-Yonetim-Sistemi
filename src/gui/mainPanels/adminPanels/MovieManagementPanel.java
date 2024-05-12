package gui.mainPanels.adminPanels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import cinema.Movie;
import gui.mainPanels.adminPanels.MovieManagementPanel;

public class MovieManagementPanel extends JPanel {
    List<Movie> movieList = Movie.getAllMovies();
    JTable movieTable = new JTable(new MovieTableModel(movieList));
    private boolean editMode = false;
    Movie selectedMovie = null;
    JLabel label = new JLabel("selected: None");

    public MovieManagementPanel() {
        setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(movieTable);
        JPanel southPanel = new JPanel(new BorderLayout());

        // northPanel components
        JCheckBox checkBox = new JCheckBox("edit");

        northPanel.add(checkBox, BorderLayout.EAST);
        northPanel.add(label, BorderLayout.WEST);

        // southPanel components
        JButton addButton = new JButton("Add");
        JButton removeButton = new JButton("Remove");
        JButton updateButton = new JButton("Update");

        southPanel.add(addButton, BorderLayout.WEST);
        southPanel.add(removeButton, BorderLayout.CENTER);
        southPanel.add(updateButton, BorderLayout.EAST);

        // add main components
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        updateMoviePanels();

        checkBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (checkBox.isSelected()) {
                    editMode = true;
                } else {
                    editMode = false;
                }
            }
        });

        this.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                // updateMoviePanels();
            }

            public void focusLost(FocusEvent e) {
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // TODO: add movie
            }
        });
    }

    public void updateMovieList() {
        movieList = Movie.getAllMovies();
    }

    public void updateMoviePanels() {
        movieTable.setModel(new MovieTableModel(movieList));

        for (int i = 0; i < movieTable.getColumnCount(); i++) {
            movieTable.getColumnModel().getColumn(i).setPreferredWidth(200);
            movieTable.getColumnModel().getColumn(i).setHeaderValue(movieTable.getModel().getColumnName(i));
        }

        movieTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = movieTable.getSelectedRow();
                if (row >= 0 && row < movieList.size()) {
                    // Handle row selection
                }
            }
        });

        // for (int i = 0; i < movieTable.getColumnCount(); i++) {
        // movieTable.getColumnModel().getColumn(i).setPreferredWidth(200);
        // movieTable.getColumnModel().getColumn(i).setHeaderValue(movieTable.getModel().getColumnName(i));
        // }

        movieTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = movieTable.getSelectedRow();
                if (row >= 0 && row < movieList.size()) {
                    selectedMovie = movieList.get(row);
                    label.setText("selected: " + selectedMovie.getName());
                }
            }
        });
    }

    private class MovieTableModel extends AbstractTableModel {
        private List<Movie> movieList;
        private String[] columnNames = { "Name", "Rating", "Duration", "Sales" };
        private Boolean[] editables = { true, true, true, false };

        public MovieTableModel(List<Movie> movieList) {
            super();
            this.movieList = movieList;

            addTableModelListener(new TableModelListener() {
                public void tableChanged(TableModelEvent e) {
                }
            });
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            Movie movie = movieList.get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return movie.getName();
                case 1:
                    return movie.getRating();
                case 2:
                    return movie.getDuration();
                case 3:
                    int sum = 0;
                    int totalDays = (int) ChronoUnit.DAYS.between(movie.getReleaseDate(), LocalDate.now());
                    for (int i = 0; i < totalDays; i++) {
                        sum += Movie.getSales(movie.getId(), LocalDate.now().minusDays(i));
                    }
                    return sum;
                default:
                    return null;
            }
        }

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return movieList.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Class<?> getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        public boolean isCellEditable(int row, int col) {
            return editMode && editables[col];
        }
    }
}