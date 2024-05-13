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
import java.util.Calendar;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DateEditor;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.AbstractTableModel;

import cinema.Movie;
import database.DatabaseManager;
import gui.guiUtils.DateSpinner;
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
        JPanel southPanel = new JPanel();

        // northPanel components
        JCheckBox checkBox = new JCheckBox("edit");

        northPanel.add(checkBox, BorderLayout.EAST);
        northPanel.add(label, BorderLayout.WEST);

        // southPanel components
        JButton removeButton = new JButton("Remove");
        JButton addButton = new JButton("Add");       
        
        southPanel.add(addButton);
        southPanel.add(removeButton);

        // add main components
        add(northPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        updateMovieTable();

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
                updateMovieTable();
            }

            public void focusLost(FocusEvent e) {
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (editMode == false) {
                    JOptionPane.showMessageDialog(null, "Please enable edit mode to remove a movie", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                new AddPopup();
            }
        });
        
        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (editMode == false) {
                    JOptionPane.showMessageDialog(null, "Please enable edit mode to remove a movie", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (selectedMovie != null) {
                    int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this movie?",
                            "Delete Movie", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            Movie.deleteFromDatabase(selectedMovie);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        updateMovieList();
                        updateMovieTable();
                    }
                } 
            }
        });         
    }

    public void updateMovieList() {
        movieList = Movie.getAllMovies();
    }

    public void updateMovieTable() {
        movieTable.setModel(new MovieTableModel(movieList));

        DateCellEditor dateEditor = new DateCellEditor();
        movieTable.setDefaultEditor(LocalDate.class, dateEditor);

        revalidate();

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

    private class DateCellEditor extends DefaultCellEditor {
        SpinnerDateModel dateModel = new SpinnerDateModel();
        JSpinner dateSpinner = new JSpinner(dateModel);
        DateEditor editor = new JSpinner.DateEditor(dateSpinner, "dd/MM/yy");;

        public DateCellEditor() {
            super(new JTextField());

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
            return ((java.util.Date) editor.getModel().getValue()).toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate();
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
                if (DatabaseManager.updateRow(movie) == false) {
                    JOptionPane.showMessageDialog(null, "Error updating movie", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            fireTableCellUpdated(row, col);
        }
    }

    private class AddPopup extends JDialog {
        public AddPopup() {
            setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
            setSize(300, 300);
            setLocationRelativeTo(null);

            JTextField nameField = new JTextField(20);
            JTextField durationField = new JTextField(20);
            DateSpinner releaseDateSpinner = new DateSpinner();
            DateSpinner lastScreeningDateSpinner = new DateSpinner();

            JPanel namePanel = new JPanel();
            JPanel durationPanel = new JPanel();
            JPanel releaseDatePanel = new JPanel();
            JPanel lastScreeningDatePanel = new JPanel();
            JPanel buttonPanel = new JPanel();

            namePanel.add(new JLabel("Name: "));
            namePanel.add(nameField);

            durationPanel.add(new JLabel("Duration: "));
            durationPanel.add(durationField);

            releaseDatePanel.add(new JLabel("Release Date: "));
            releaseDatePanel.add(releaseDateSpinner);

            lastScreeningDatePanel.add(new JLabel("Last Screening Date: "));
            lastScreeningDatePanel.add(lastScreeningDateSpinner);

            JButton addButton = new JButton("Add");
            JButton cancelButton = new JButton("Cancel");

            buttonPanel.add(addButton);
            buttonPanel.add(cancelButton);

            add(namePanel);
            add(durationPanel);
            add(releaseDatePanel);
            add(lastScreeningDatePanel);
            add(buttonPanel);

            pack();

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String name = nameField.getText();
                    int duration = Integer.parseInt(durationField.getText());
                    LocalDate releaseDate = releaseDateSpinner.getDate();
                    LocalDate lastScreeningDate = lastScreeningDateSpinner.getDate();

                    Movie movie = new Movie(name, duration, releaseDate, lastScreeningDate);

                    try {
                        DatabaseManager.insertRow(movie);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    updateMovieList();
                    updateMovieTable();
                    dispose();
                }
            });

            setVisible(true);
        }
    }
}