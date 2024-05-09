package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Seat;
import cinema.SeatAvailability;
import cinema.Session;
import database.DatabaseManager;
import database.FilterCondition;

public class TicketSellingPanel extends JPanel {

    JPanel cardPanel;
    CardLayout cardLayout;
    CinemaGUI parent;

    // panels
    MovieSelectionPanel movieSelectionPanel;
    SessionSelectionPanel sessionSelectionPanel;
    SeatSelectionPanel seatSelectionPanel;
    PaymentPanel paymentPanel;

    // used to move data between other windows
    Movie selectedMovie;
    Session selectedSession;
    LocalDate selectedDate;
    List<Seat> selectedSeats = new ArrayList<>();

    TicketSellingPanel(CinemaGUI parent, int width, int height) {
        this.parent = parent;
        setLayout(new BorderLayout());

        cardPanel = new JPanel();
        cardLayout = new CardLayout();
        cardPanel.setLayout(cardLayout);

        movieSelectionPanel = new MovieSelectionPanel(this);
        cardPanel.add("Film", movieSelectionPanel);

        sessionSelectionPanel = new SessionSelectionPanel();
        cardPanel.add("Seans", sessionSelectionPanel);

        seatSelectionPanel = new SeatSelectionPanel();
        cardPanel.add("Koltuk", seatSelectionPanel);

        paymentPanel = new PaymentPanel(this, width, height);
        cardPanel.add("Ödeme", paymentPanel);

        add(cardPanel, BorderLayout.CENTER);

        cardLayout.show(cardPanel, "Film Seç");
    }

    public void goBack() {
        parent.showMainMenu();
    }

    public void selectMovie(Movie movie, LocalDate date) {
        selectedMovie = movie;
        selectedDate = date;
        cardLayout.show(cardPanel, "Seans");
        sessionSelectionPanel.listSessions(movie, date);
    }

    public void deselectMovie() {
        selectedMovie = null;
        selectedDate = null;
        cardLayout.show(cardPanel, "Film");
    }

    public void selectSession(Session session) {
        selectedSession = session;
        cardLayout.show(cardPanel, "Koltuk");
        seatSelectionPanel.listSeats(session);
    }

    public void addSeat(Seat seat) {
        selectedSeats.add(seat);
    }

    public void removeSeat(Seat seat) {
        selectedSeats.remove(seat);
    }

    public void clearSeats() {
        selectedSeats.clear();
    }

    public void onVisible() {
        movieSelectionPanel.onVisible();
    }

    public void showMainMenu() {
        parent.showMainMenu();
    }

    private class SessionSelectionPanel extends JPanel {

        // border layout
        // north: back button / movie name / date
        // center: session buttons grid layout with scrollable pane

        // each session button has a session object attached to it and a listener
        // when clicked, selectSession is called with the session object
        // and the card layout is switched to the seat selection panel

        JButton backButton;
        JLabel movieName;
        JLabel date;

        List<Session> sessionsAvailable;

        SessionSelectionPanel() {
            setLayout(new BorderLayout());
            JPanel northPanel = new JPanel();
            northPanel.setLayout(new BorderLayout());
            add(northPanel, BorderLayout.NORTH);

            backButton = new JButton("Geri");
            backButton.addActionListener(e -> deselectMovie());
            northPanel.add(backButton, BorderLayout.WEST);

            movieName = new JLabel();
            northPanel.add(movieName, BorderLayout.CENTER);

            date = new JLabel();
            northPanel.add(date, BorderLayout.EAST);
        }

        public void listSessions(Movie movie, LocalDate date) {
            // get sessions for the movie and date
            // add buttons for each session
            // set the movie name and date

            movieName.setText(movie.getName());
            this.date.setText(date.toString());

            try {
                List<FilterCondition> conditions = List.of(
                        new FilterCondition("movieId", selectedMovie.getId(), FilterCondition.Relation.EQUALS),
                        new FilterCondition("date", date, FilterCondition.Relation.EQUALS));
                sessionsAvailable = DatabaseManager.getRowsFilteredAndSortedBy(Session.class, conditions, "startTime",
                        true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            JPanel sessionPanel = new JPanel();
            sessionPanel.setLayout(new GridLayout(0, 3));
            JScrollPane scrollPane = new JScrollPane(sessionPanel);
            add(scrollPane, BorderLayout.CENTER);

            for (Session session : sessionsAvailable) {
                JButton sessionButton = new JButton(session.getStartTime().toString());
                sessionButton.addActionListener(e -> {
                    selectSession(session);
                });
                sessionPanel.add(sessionButton);
            }
        }
    }

    private class SeatSelectionPanel extends JPanel {

        enum SeatState {
            AVAILABLE, SELECTED, UNAVAILABLE
        }

        ScreeningRoom room;
        JPanel seatPanel = new JPanel();
        SeatState seatStates[][];
        List<Seat> selectedSeats = new ArrayList<>();

        public SeatSelectionPanel() {
            setLayout(new BorderLayout());
            add(seatPanel);

            JLabel screenLabel = new JLabel("Perde");
            screenLabel.setFont(screenLabel.getFont().deriveFont(34.0f));
            screenLabel.setHorizontalAlignment(JLabel.CENTER);
            screenLabel.setPreferredSize(new Dimension(0, 100));
            add(screenLabel, BorderLayout.NORTH);

            JPanel southPanel = new JPanel();

            JButton backButton = new JButton("Geri");
            backButton.addActionListener(e -> {
                cardLayout.show(cardPanel, "Seans");
                seatPanel.removeAll();
                clearSeats();
            });
            southPanel.add(backButton);

            JButton nextButton = new JButton("Ödeme");
            nextButton.addActionListener(e -> {
                cardLayout.show(cardPanel, "Ödeme");
            });
            southPanel.add(nextButton);

            add(southPanel, BorderLayout.SOUTH);
        }

        private ScreeningRoom getScreeningRoomFromSession(Session session) {
            try {
                room = DatabaseManager.getRowById(ScreeningRoom.class, session.getScreeningRoomId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return room;
        }

        public void listSeats(Session session) {
            ScreeningRoom room = getScreeningRoomFromSession(selectedSession);
            final int row = room.getSeatRowCount(),
                    col = room.getSeatColCount();

            seatPanel.setLayout(new GridLayout(row, col));
            seatStates = new SeatState[row][col];

            List<SeatAvailability> seatAvailabilities = null;
            try {
                List<FilterCondition> conditions = List
                        .of(new FilterCondition("sessionId", selectedSession.getId(), FilterCondition.Relation.EQUALS));
                seatAvailabilities = DatabaseManager.getRowsFilteredAndSortedBy(SeatAvailability.class, conditions,
                        "id", true);

                for (SeatAvailability sa : seatAvailabilities) {
                    Seat s = DatabaseManager.getRowById(Seat.class, sa.getSeatId());
                    seatStates[s.getRow()][s.getCol()] = sa.isAvailable() ? SeatState.AVAILABLE : SeatState.UNAVAILABLE;
                    seatPanel.add(new SeatButton(s));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class SeatButton extends JButton {
            public SeatButton(Seat seat) {
                String rowSymbol = String.valueOf((char) ('A' + seat.getRow())) + seat.getCol();

                int row = seat.getRow(), col = seat.getCol();
                
                setText(rowSymbol);

                if (!seatStates[row][col].equals(SeatState.AVAILABLE)) {
                    setBackground(java.awt.Color.RED);
                }
                
                addActionListener(e -> {
                    switch (seatStates[row][col]) {
                        case SeatState.AVAILABLE:
                            seatStates[row][col] = SeatState.SELECTED;
                            selectedSeats.add(seat);
                            setBackground(java.awt.Color.GREEN);
                            break;
                        case SeatState.SELECTED:
                            seatStates[row][col] = SeatState.AVAILABLE;
                            selectedSeats.remove(seat);
                            setBackground(null);
                            break;
                        default:
                            // TODO: give feedback to the user
                            break;
                    }
                });
            }
        }
    }

    private class PaymentPanel extends JPanel {
        public PaymentPanel(TicketSellingPanel parent, int width, int height) {

        }
    }

    private class ScreeningRoomSelectionPanel {
        public ScreeningRoomSelectionPanel() {

        }
    }
}