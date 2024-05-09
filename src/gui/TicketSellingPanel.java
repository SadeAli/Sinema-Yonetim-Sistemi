package gui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Seat;
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
    List<Seat> selectedSeats;

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

    public void goBack()
    {
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
                    new FilterCondition("date", date, FilterCondition.Relation.EQUALS)
                );
                sessionsAvailable = DatabaseManager.getRowsFilteredAndSortedBy(Session.class, conditions, "startTime", true);
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
        List<Seat> seats;
        
        public SeatSelectionPanel() {
                    
        }

        private ScreeningRoom getScreeningRoomFromSession(Session session) {
            ScreeningRoom room = null;

            try {
                room = DatabaseManager.getRowById(ScreeningRoom.class, session.getScreeningRoomId());
            } catch (Exception e) {
                e.printStackTrace();
            }

            return room;
        }

        public void listSeats(Session session) {
            // get seats for the session
            // add buttons for each seat
            // set the session start time and screening room name
            ScreeningRoom room = getScreeningRoomFromSession(selectedSession);
            setLayout(new GridLayout(room.getSeatRowCount(), room.getSeatColCount()));

            for (Seat seat : seats) {
            }    

            try {
                List<FilterCondition> conditions = List.of(
                    new FilterCondition("sessionId", selectedSession.getId(), FilterCondition.Relation.EQUALS),
                    new FilterCondition("ScreeningRoomId", selectedSession.getScreeningRoomId(), FilterCondition.Relation.EQUALS)
                );
                seats = DatabaseManager.getRowsFilteredAndSortedBy(Seat.class, conditions, "seatNumber", true);
            } catch (Exception e) {
                e.printStackTrace();
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
