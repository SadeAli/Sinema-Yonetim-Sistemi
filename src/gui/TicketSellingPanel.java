package gui;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cinema.Movie;
import cinema.Session;


public class TicketSellingPanel extends JPanel {

    private class Seat {
        String seatNumber;
    }
    
    JPanel cardPanel;
    CardLayout cardLayout;
    CinemaGUI parent;
    
    MovieSelectionPanel movieSelectionPanel;
    SessionSelectionPanel sessionSelectionPanel;
    SeatSelectionPanel seatSelectionPanel;
    PaymentPanel paymentPanel;

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

        movieSelectionPanel = new MovieSelectionPanel(this, width, height, 1);
        cardPanel.add("Film", movieSelectionPanel);

        sessionSelectionPanel = new SessionSelectionPanel();
        cardPanel.add("Seans", sessionSelectionPanel);

        seatSelectionPanel = new SeatSelectionPanel(this, width, height);
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

    public void setSelectedSession(Session session) {
        selectedSession = session;
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

    private class SessionSelectionPanel extends JPanel{

        // border layout
        // north: back button / movie name / date
        // center: session buttons grid layout with scrollable pane

        // each session button has a session object attached to it and a listener
        // when clicked, setSelectedSession is called with the session object
        // and the card layout is switched to the seat selection panel

        JButton backButton;
        JLabel movieName;
        JLabel date;
        
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
        }
    }

    private class SeatSelectionPanel extends JPanel {
        SeatSelectionPanel(TicketSellingPanel parent, int width, int height) {
            
        }
    }

    private class PaymentPanel extends JPanel {
        PaymentPanel(TicketSellingPanel parent, int width, int height) {
        
        }
    }
}
