package gui.mainPanels.adminPanels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import cinema.Movie;
import database.DatabaseManager;
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

        // for (int i = 0; i < movieTable.getRowCount(); i++) {
        // for (int j = 0; j < movieTable.getColumnCount(); j++) {
        // movieTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
        // public Component getTableCellRendererComponent(JTable table, Object value,
        // boolean isSelected, boolean hasFocus, int row, int column) {
        // Component c = super.getTableCellRendererComponent(table, value, isSelected,
        // hasFocus, row, column);
        // ((JLabel) c).setHorizontalAlignment(SwingConstants.CENTER);
        // return c;
        // }
        // });
        // }
        // }

        DateCellEditor dateEditor = new DateCellEditor();
        movieTable.setDefaultEditor(LocalDate.class, dateEditor);

        movieTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int row = movieTable.getSelectedRow();
                if (row >= 0 && row < movieList.size()) {
                    // Handle row selection
                }
            }
        });

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

    // int sum = 0;
    // int totalDays = (int) ChronoUnit.DAYS.between(movie.getReleaseDate(),
    // LocalDate.now());
    // for (int i = 0; i < totalDays; i++) {
    // sum += Movie.getSales(movie.getId(), LocalDate.now().minusDays(i));
    // }
    // return sum;

    private class DateCellEditor extends DefaultCellEditor {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yy");;

        public DateCellEditor() {
            super(new JTextField()); // Explicitly invoke the super constructor with a JTextField argument

            // Create a spinner for date selection

            // add spinner for date filter
            dateModel.setCalendarField(Calendar.DAY_OF_MONTH);
            dateSpinner.setPreferredSize(new Dimension(300, 50));
            dateSpinner.setEditor(editor);

            editorComponent = editor.getSpinner();
            delegate = new EditorDelegate() {
                public void setValue(Object value) {
                    editor.getModel().setValue(value);
                }

                public Object getCellEditorValue() {
                    return editor.getModel().getValue();
                }
            };
        }

        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            if (value instanceof LocalDate) {
                value = java.util.Date.from(((LocalDate) value).atStartOfDay(ZoneId.systemDefault()).toInstant());
            }
            editor.getModel().setValue(value);
            return editorComponent;
        }

        public Object getCellEditorValue() {
            return ((java.util.Date) editor.getModel().getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
    }

    private class MovieTableModel extends AbstractTableModel {
        private List<Movie> movieList;
        private String[] columnNames = { "Name", "Duration", "releaseDate", "lastScreeningDate", "rating",
                "ratingCount" };
        private Boolean[] editables = { true, true, true, true, true, true };

        public MovieTableModel(List<Movie> movieList) {
            super();
            this.movieList = movieList;

        }

        public Object getValueAt(int row, int col) {
            Movie movie = movieList.get(row);

            switch (col) {
                case 0:
                    return movie.getName() != null ? movie.getName() : "";
                case 1:
                    return movie.getDuration();
                case 2:
                    return movie.getReleaseDate() != null ? movie.getReleaseDate() : LocalDate.now();
                case 3:
                    return movie.getLastScreeningDate() != null ? movie.getLastScreeningDate() : LocalDate.now();
                case 4:
                    return movie.getRating();
                case 5:
                    return movie.getRatingCount();
                default:
                    return "";
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

        public void setValueAt(Object value, int row, int col) {

            Movie movie = movieList.get(row);

            switch (col) {
                case 0:
                    movie.setName((String) value);
                    break;
                case 1:
                    movie.setDuration((int) value);
                    break;
                case 2:
                    movie.setLastScreeningDate((LocalDate) value);
                    System.out.println("release date: " + value);
                    break;
                case 3:
                    movie.setLastScreeningDate((LocalDate) value);
                    break;
                case 4:
                    movie.setRating((float) value);
                    break;
                case 5:
                    movie.setRatingCount((int) value);
                    break;
                default:
                    break;
            }

            try {
                System.out.println(DatabaseManager.updateRow(movie));
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            fireTableCellUpdated(row, col);
        }
    }
}