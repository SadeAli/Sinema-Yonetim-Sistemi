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

public class TicketSellingPanel extends JPanel implements OnVisible {

    CardLayout cardLayout = new CardLayout();
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
    List<SeatAvailability> selectedSeats = new ArrayList<>();

    TicketSellingPanel(CinemaGUI parent) {
        this.parent = parent;
        setLayout(cardLayout);

        movieSelectionPanel = new MovieSelectionPanel(this);
        sessionSelectionPanel = new SessionSelectionPanel();
        seatSelectionPanel = new SeatSelectionPanel();
        paymentPanel = new PaymentPanel();

        add("Film", movieSelectionPanel);
        add("Seans", sessionSelectionPanel);
        add("Koltuk", seatSelectionPanel);
        add("Ödeme", paymentPanel);

        cardLayout.show(this, "Film Seç");
    }

    public void goBack() {
        parent.showMainMenu();
    }

    public void selectMovie(Movie movie, LocalDate date) {
        selectedMovie = movie;
        selectedDate = date;
        cardLayout.show(this, "Seans");
        sessionSelectionPanel.listSessions(movie, date);
    }

    public void deselectMovie() {
        selectedMovie = null;
        selectedDate = null;
        cardLayout.show(this, "Film");
    }

    public void selectSession(Session session) {
        selectedSession = session;
        cardLayout.show(this, "Koltuk");
        seatSelectionPanel.listSeats(session);
    }

    public void onVisible() {
        movieSelectionPanel.onVisible();
    }

    private class SessionSelectionPanel extends JPanel {
        JLabel movieName;
        JLabel date;

        List<Session> sessionsAvailable;

        SessionSelectionPanel() {
            setLayout(new BorderLayout());
            JPanel northPanel = new JPanel();
            northPanel.setLayout(new BorderLayout());
            add(northPanel, BorderLayout.NORTH);

            JButton backButton = new JButton("Geri");

            movieName = new JLabel();
            date = new JLabel();

            northPanel.add(backButton, BorderLayout.WEST);
            northPanel.add(movieName, BorderLayout.CENTER);
            northPanel.add(date, BorderLayout.EAST);

            backButton.addActionListener(e -> deselectMovie());
        }

        public void listSessions(Movie movie, LocalDate date) {
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
        List<SeatAvailability> selectedSeats = new ArrayList<>();

        public SeatSelectionPanel() {
            setLayout(new BorderLayout());

            JPanel southPanel = new SouthPanel();
            JLabel screenLabel = new JLabel("Perde", JLabel.CENTER);
            screenLabel.setFont(screenLabel.getFont().deriveFont(34.0f));
            screenLabel.setPreferredSize(new Dimension(0, 100));

            add(seatPanel, BorderLayout.CENTER);
            add(screenLabel, BorderLayout.NORTH);
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
            selectedSeats.clear();

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
                    seatPanel.add(new SeatButton(s, sa));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class SouthPanel extends JPanel {
            JButton backButton = new JButton("Geri");
            JButton nextButton = new JButton("Ödeme");

            SouthPanel() {
                add(backButton);
                add(nextButton);

                backButton.addActionListener(e -> {
                    cardLayout.show(TicketSellingPanel.this, "Seans");
                    seatPanel.removeAll();
                });

                nextButton.addActionListener(e -> {
                    System.out.println("Selected seats: " + selectedSeats);
                    if (SeatAvailability.bookSeatList(selectedSeats)) {
                        System.out.println("Seats booked successfully");
                        selectedSeats.clear();
                    } else {
                        System.out.println("Failed to book seats");
                    }

                    // TODO dont forget payment
                    // cardLayout.show(cardPanel, "Ödeme");
                });
            }
        }

        private class SeatButton extends JButton {
            public SeatButton(Seat seat, SeatAvailability sa) {
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
                            selectedSeats.add(sa);
                            setBackground(java.awt.Color.GREEN);
                            break;
                        case SeatState.SELECTED:
                            seatStates[row][col] = SeatState.AVAILABLE;
                            selectedSeats.remove(sa);
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
        public PaymentPanel() {

        }
    }
}