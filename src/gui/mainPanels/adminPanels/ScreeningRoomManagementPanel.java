package gui.mainPanels.adminPanels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import cinema.Discount;
import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Session;
import database.DatabaseManager;
import database.FilterCondition;

public class ScreeningRoomManagementPanel extends JPanel {
    private List<ScreeningRoom> screeningRooms;

    private Movie selectedMovie;

    private JPanel mainPanel = new JPanel();
    private JScrollPane scrollPane = new JScrollPane(mainPanel);
    private JPanel southPanel = new JPanel();

    public ScreeningRoomManagementPanel(List<ScreeningRoom> screeningRooms) {
        this.screeningRooms = screeningRooms;

        setLayout(new BorderLayout());

        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JComboBox<Movie> movieCombobox = new JComboBox<>();

        List<Movie> movies = Movie.getAllMovies();
        selectedMovie = null;
        movieCombobox.addItem(null);
        for (Movie m : movies) {
            movieCombobox.addItem(m);
        }

        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setPreferredSize(new Dimension(100, 100));
        southPanel.add(new ManagementPanel());
        southPanel.add(new DiscountPanel());

        add(southPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);
        add(movieCombobox, BorderLayout.NORTH);

        movieCombobox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selectedMovie = (Movie) movieCombobox.getSelectedItem();
                repaintDayMoviePanels();
            }
        });

        movieCombobox.setRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Movie) {
                    Movie movie = (Movie) value;

                    String text = String.format("%-70s%-30s%-6s", movie.getName(),
                            "rating: " + movie.getRating(), "duration: " + movie.getDuration());
                    setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

                    setText(text);
                }

                if (value == null) {
                    setText("Empty");
                }

                return this;
            }
        });
    }

    public void repaintDayMoviePanels() {
        screeningRooms = ScreeningRoom.getAllScreeningRooms();
        mainPanel.removeAll();
        for (ScreeningRoom screeningRoom : screeningRooms) {
            DayMoviePanel dayMoviePanel = new DayMoviePanel(screeningRoom);
            mainPanel.add(dayMoviePanel);
        }
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private class ManagementPanel extends JPanel {
        public ManagementPanel() {
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            JButton addButton = new JButton("Add Room");
            JButton removeButton = new JButton("Remove Room");

            add(addButton);
            add(removeButton);

            addButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new AddPopup();
                }
            });

            removeButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    new RemovePopup();
                }
            });
        }

        private class AddPopup extends JDialog {
            public AddPopup() {
                setLayout(new GridLayout(3, 2));

                JLabel rowCountLabel = new JLabel("Row Count");
                JTextField rowCount = new JTextField();
                JLabel colCountLabel = new JLabel("Column Count");
                JTextField colCount = new JTextField();
                JButton addButton = new JButton("Add");

                add(rowCountLabel);
                add(rowCount);
                add(colCountLabel);
                add(colCount);
                add(addButton);

                pack();
                setLocationRelativeTo(null);
                setVisible(true);

                addButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        new ScreeningRoom(Integer.parseInt(rowCount.getText()),
                                Integer.parseInt(colCount.getText())).insertToDatabase();
                        repaintDayMoviePanels();
                    }
                });

            }
        }

        private class RemovePopup {
            public RemovePopup() {
                String input = JOptionPane.showInputDialog("Enter a valid screening room id to remove");

                if (input == null)
                    return;

                try {
                    int id = Integer.parseInt(input);
                    if (ScreeningRoom.deleteFromDatabase(id)) {
                        JOptionPane.showMessageDialog(null, "Screening room removed");
                    } else {
                        JOptionPane.showMessageDialog(null, "Screening can't be removed");
                    }
                    repaintDayMoviePanels();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Invalid input");
                }
            }
        }
    }

    private class DiscountPanel extends JScrollPane {

        List<Discount> discountList = null;
        JPanel contentPanel = new JPanel();

        public DiscountPanel() {
            contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
            contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            setViewportView(contentPanel);

            listDiscounts();
        }

        public void listDiscounts() {
            contentPanel.removeAll();
            contentPanel.add(new JLabel("Discounts: "));

            try {
                discountList = DatabaseManager.getRowsFilteredAndSortedBy(Discount.class,
                        List.of(new FilterCondition("date", LocalDate.now(),
                                FilterCondition.Relation.GREATER_THAN_OR_EQUALS),
                                new FilterCondition("date", LocalDate.now().plusDays(30),
                                        FilterCondition.Relation.LESS_THAN)),
                        "date", false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            Discount discountArray[] = new Discount[30];
            for (Discount d : discountList) {
                discountArray[(int) (ChronoUnit.DAYS.between(LocalDate.now(), d.getDate()))] = d;
            }

            for (int i = 0; i < 30; i++) {
                contentPanel.add(new DayDiscountButton(i, discountArray[i]));
            }

            contentPanel.revalidate();
        }

        private class DayDiscountButton extends JButton {
            public DayDiscountButton(int i, Discount discount) {

                JButton button = new JButton();

                button.setText(LocalDate.now().plusDays(i).getMonthValue() + "/"
                        + LocalDate.now().plusDays(i).getDayOfMonth());

                if (discount != null) {
                    button.setToolTipText("" + discount.getRatio());
                    this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                } else {
                    button.setToolTipText("Boş");
                    this.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
                }

                this.setSize(new Dimension(100, 100));
                this.setMargin(new Insets(50, 50, 50, 50));
                this.add(button);

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        try {

                            if (discount == null) {
                                String input = JOptionPane.showInputDialog("Enter a discount ratio for the day");
                                if (input == null)
                                    return;

                                DatabaseManager.insertRow(
                                        new Discount(LocalDate.now().plusDays(i), Double.parseDouble(input)));

                            } else {
                                DatabaseManager.deleteRow(Discount.class, discount.getId());
                            }

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null, "Invalid input");
                            ex.printStackTrace();
                        }

                        listDiscounts();
                    }
                });
            }
        }
    }

    private class DayMoviePanel extends JPanel {
        public DayMoviePanel(ScreeningRoom screeningRoom) {

            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel nameLabel = new JLabel(String.format("%5s %-3s", "Room ", screeningRoom.getId()));
            // JPanel assignmentPanel = new JPanel();

            nameLabel.setToolTipText("Room " + screeningRoom.getId() + " - " + screeningRoom.getSeatRowCount()
                    + "x" + screeningRoom.getSeatColCount() + " koltuk");

            this.add(nameLabel);

            // get sessions for the screening room
            List<Session> sessionList = null;
            try {
                sessionList = DatabaseManager.getRowsFilteredAndSortedBy(Session.class,
                        List.of(new FilterCondition("screeningRoomId", screeningRoom.getId(),
                                FilterCondition.Relation.EQUALS),
                                new FilterCondition("date", LocalDate.now(),
                                        FilterCondition.Relation.GREATER_THAN_OR_EQUALS),
                                new FilterCondition("date", LocalDate.now().plusDays(30),
                                        FilterCondition.Relation.LESS_THAN)),
                        "date", false);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // put sessions into an array of size 30
            Session sessionArray[] = new Session[30];
            for (Session s : sessionList) {
                sessionArray[(int) (ChronoUnit.DAYS.between(LocalDate.now(), s.getDate()))] = s;
            }

            // get movie names for each session
            Movie movieArray[] = new Movie[30];
            for (int i = 0; i < 30; i++) {
                if (sessionArray[i] != null) {
                    try {
                        List<Movie> movieList = DatabaseManager.getRowsFilteredAndSortedBy(Movie.class,
                                List.of(new FilterCondition("id", sessionArray[i].getMovieId(),
                                        FilterCondition.Relation.EQUALS)),
                                "id", true);

                        movieArray[i] = movieList.size() > 0 ? movieList.get(0) : null;

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // create buttons for each day
            for (int i = 0; i < 30; i++) {
                this.add(new DayMovieButton(i, movieArray[i], screeningRoom));
            }
        }

        private class DayMovieButton extends JButton {
            public DayMovieButton(final int index, Movie movie, ScreeningRoom screeningRoom) {

                JButton button = new JButton();

                button.setText(LocalDate.now().plusDays(index).getMonthValue() + "/"
                        + LocalDate.now().plusDays(index).getDayOfMonth());

                if (movie != null) {
                    button.setToolTipText(movie.getName());
                    this.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2));
                } else {
                    button.setToolTipText("Boş");
                    this.setBorder(BorderFactory.createLineBorder(Color.gray, 2));
                }

                this.setSize(new Dimension(100, 100));
                this.setMargin(new Insets(50, 50, 50, 50));
                this.add(button);

                button.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {

                        if (selectedMovie == null) {
                            List<Session> sessionList = null;
                            try {
                                sessionList = DatabaseManager.getRowsFilteredAndSortedBy(Session.class,
                                        List.of(new FilterCondition("screeningRoomId", screeningRoom.getId(),
                                                FilterCondition.Relation.EQUALS),
                                                new FilterCondition("date", LocalDate.now().plusDays(index),
                                                        FilterCondition.Relation.EQUALS)),
                                        "date", false);
                            } catch (Exception e1) {
                                e1.printStackTrace();
                            }

                            if (sessionList.size() > 0) {
                                for (Session s : sessionList) {
                                    Session.delete(s.getId());
                                }
                            }

                            repaintDayMoviePanels();
                            return;
                        }

                        if (screeningRoom.addMovieToDate(LocalDate.now().plusDays(index),
                                selectedMovie.getId()))
                            JOptionPane.showMessageDialog(null, "added movie " + selectedMovie.getId() + " to date "
                                    + LocalDate.now().plusDays(index));
                        else
                            JOptionPane.showMessageDialog(null,
                                    "unable to add movie " + selectedMovie.getId() + " to date "
                                            + LocalDate.now().plusDays(index));
                        repaintDayMoviePanels();
                    }
                });
            }
        }
    }
}