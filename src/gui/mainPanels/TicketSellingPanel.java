package gui.mainPanels;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import cinema.Discount;
import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.Seat;
import cinema.SeatAvailability;
import cinema.Session;
import cinema.Ticket;
import database.DatabaseManager;
import database.FilterCondition;
import gui.CinemaGUI;

/**
 * Represents a panel for selling movie tickets.
 * This panel contains multiple sub-panels for selecting movies, sessions, seats, and making payments.
 * It allows the user to navigate through different stages of the ticket selling process.
 */
public class TicketSellingPanel extends JPanel {

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
    Double defaultPrice = 70.0;
    double totalPrice = 0.0;
    Ticket ticket;

    public TicketSellingPanel(CinemaGUI parent, int width, int height) {
        this.parent = parent;
        setLayout(cardLayout);

        movieSelectionPanel = new MovieSelectionPanel(this);
        sessionSelectionPanel = new SessionSelectionPanel();
        seatSelectionPanel = new SeatSelectionPanel();
        paymentPanel = new PaymentPanel();

        add("Movie", movieSelectionPanel);
        add("Session", sessionSelectionPanel);
        add("Seat", seatSelectionPanel);
        add("Payment", paymentPanel);

        cardLayout.show(this, "Movie");
    }

    public void reloadPaymentPanel() {
        remove(paymentPanel);
        paymentPanel = new PaymentPanel();
        add("Payment", paymentPanel);
    }

    public void showMainMenu() {
        cardLayout.show(this, "Movie");
        parent.showMainMenu();
    }

    public void selectMovie(Movie movie, LocalDate date) {
        selectedSeats.clear();
        selectedMovie = movie;
        selectedDate = date;
        sessionSelectionPanel.listSessions(movie, date);
        cardLayout.show(this, "Session");
    }

    public void deselectMovie() {
        selectedSeats.clear();
        selectedMovie = null;
        selectedDate = null;
        cardLayout.show(this, "Movie");
    }

    public void selectSession(Session session) {
        selectedSession = session;
        cardLayout.show(this, "Seat");
        seatSelectionPanel.listSeats(session);
    }

    public void onVisible() {
        movieSelectionPanel.applyFilter();
    }

    private class SessionSelectionPanel extends JPanel {
        JLabel movieName;
        JLabel date;
        JScrollPane scrollPane = new JScrollPane();

        List<Session> sessionsAvailable;

        SessionSelectionPanel() {
            setLayout(new BorderLayout());
            JPanel northPanel = new JPanel();
            northPanel.setLayout(new BorderLayout());
            add(northPanel, BorderLayout.NORTH);

            JButton backButton = new JButton("Back");

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
                        new FilterCondition("movieId", movie.getId(), FilterCondition.Relation.EQUALS),
                        new FilterCondition("date", date, FilterCondition.Relation.EQUALS));
                sessionsAvailable = DatabaseManager.getRowsFilteredAndSortedBy(Session.class, conditions, "startTime",
                        true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            JPanel sessionPanel = new JPanel();
            sessionPanel.setLayout(new GridLayout(0, 3));
            scrollPane.setViewportView(sessionPanel);
            add(scrollPane, BorderLayout.CENTER);

            for (Session session : sessionsAvailable) {
                JButton sessionButton = new JButton("<html>" + session.getStartTime().toString() + " - " +
                        (session.getStartTime().plusMinutes(movie.getDuration())) + "<br> Room: "
                        + session.getScreeningRoomId() + "</html>");
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

        public SeatSelectionPanel() {
            setLayout(new BorderLayout());

            JPanel southPanel = new SouthPanel();
            JLabel screenLabel = new JLabel("Screen", JLabel.CENTER);
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
            seatPanel.removeAll();

            ScreeningRoom room = getScreeningRoomFromSession(selectedSession);
            final int row = room.getSeatRowCount(),
                    col = room.getSeatColCount();

            seatPanel.setLayout(new GridLayout(row, col));
            selectedSeats.clear();
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
                    seatPanel.add(new SeatButtonPanel(s, sa));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private class SouthPanel extends JPanel {
            JButton backButton = new JButton("Back");
            JButton nextButton = new JButton("Proceed");

            SouthPanel() {
                add(backButton);
                add(nextButton);

                backButton.addActionListener(e -> {
                    deselectMovie();
                    cardLayout.show(TicketSellingPanel.this, "Session");
                });

                nextButton.addActionListener(e -> {
                    if (selectedSeats.isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Please select at least one seat");
                        return;
                    }

                    double discountRatio = 0.0;

                    try {
                        List<Discount> dl = DatabaseManager.getRowsFilteredAndSortedBy(Discount.class, List.of(
                                new FilterCondition("date", selectedDate, FilterCondition.Relation.EQUALS)), "id",
                                true);

                        if (dl.size() > 0) {
                            discountRatio = dl.get(0).getRatio();
                        }

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    totalPrice = defaultPrice * selectedSeats.size() * (1 - discountRatio);
                    ticket = SeatAvailability.bookSeatList(selectedSeats, totalPrice);

                    if (ticket == null) {
                        JOptionPane.showMessageDialog(this, "Failed to book the seats.");
                        return;
                    }

                    reloadPaymentPanel();
                    cardLayout.show(this.getParent().getParent(), "Payment");
                });
            }
        }

        private class SeatButtonPanel extends JPanel {
            SeatButtonPanel(Seat seat, SeatAvailability sa) {
                setLayout(new GridLayout(0, 1));
                setBorder(new EmptyBorder(10, 10, 10, 10));
                add(new SeatButton(seat, sa));
            }

            private class SeatButton extends JButton {
                public SeatButton(Seat seat, SeatAvailability sa) {
                    String rowSymbol = String.valueOf((char) ('A' + seat.getRow())) + seat.getCol();

                    int row = seat.getRow(), col = seat.getCol();

                    this.setText(rowSymbol);

                    final Color selectedColor = Color.GREEN;
                    final Color unavailableColor = Color.RED;
                    final Color disabledColor = Color.GRAY;

                    if (seatStates[row][col].equals(SeatState.UNAVAILABLE)) {
                        this.setEnabled(false);
                        this.setBorder(new LineBorder(unavailableColor, 5));
                    } else if (seatStates[row][col].equals(SeatState.SELECTED)) {
                        this.setBorder(new LineBorder(selectedColor, 5));
                    } else {
                        this.setBorder(new LineBorder(disabledColor, 1));
                    }

                    addActionListener(e -> {
                        switch (seatStates[row][col]) {
                            case SeatState.AVAILABLE:
                                seatStates[row][col] = SeatState.SELECTED;
                                selectedSeats.add(sa);
                                this.setBorder(new LineBorder(selectedColor, 5));
                                break;
                            case SeatState.SELECTED:
                                seatStates[row][col] = SeatState.AVAILABLE;
                                selectedSeats.remove(sa);
                                this.setBorder(new LineBorder(disabledColor, 1));
                                break;
                            default:
                                JOptionPane.showMessageDialog(this, "Some error occured. Please try again.");
                                break;
                        }
                    });
                }
            }
        }
    }

    private class PaymentPanel extends JPanel {
        public PaymentPanel() {
            setLayout(new BorderLayout());

            // create center panel
            JPanel paymentInfoPanel = new JPanel();
            paymentInfoPanel.setLayout(new GridLayout(0, 2));
            paymentInfoPanel.setBorder(new EmptyBorder(250, 100, 250, 100));

            // create center panel components
            JTextField nameField = new JTextField("Name");
            JTextField surnameField = new JTextField("Surname");
            JTextField cardNumberField = new JTextField("Card Number");
            JTextField cvvField = new JTextField("CVV");
            JSpinner expiryDateSpinner = new JSpinner(new SpinnerDateModel());
            JSpinner.DateEditor expiryDateEditor = new JSpinner.DateEditor(expiryDateSpinner, "MM/yyyy");

            // spinner settings
            expiryDateSpinner.setEditor(expiryDateEditor);

            // set center component sizes
            nameField.setMaximumSize(new Dimension(200, 30));
            surnameField.setMaximumSize(new Dimension(200, 30));
            cardNumberField.setMaximumSize(new Dimension(400, 30));
            cvvField.setMaximumSize(new Dimension(60, 30));
            expiryDateSpinner.setMaximumSize(new Dimension(200, 30));

            // selection info
            JLabel selectionInfo = new JLabel("Seats selected: " + selectedSeats.size(), JLabel.CENTER);
            JLabel priceInfo = new JLabel(" Price: " + totalPrice + " TL", JLabel.CENTER);

            // add center panel components
            paymentInfoPanel.add(selectionInfo);
            paymentInfoPanel.add(priceInfo);

            paymentInfoPanel.add(new JLabel("Name:"));
            paymentInfoPanel.add(nameField);
            paymentInfoPanel.add(new JLabel("Surname:"));
            paymentInfoPanel.add(surnameField);
            paymentInfoPanel.add(new JLabel("Card Number:"));
            paymentInfoPanel.add(cardNumberField);
            paymentInfoPanel.add(new JLabel("CVV:"));
            paymentInfoPanel.add(cvvField);
            paymentInfoPanel.add(new JLabel("Expiry Date:"));
            paymentInfoPanel.add(expiryDateSpinner);

            // create south panel
            JPanel southPanel = new JPanel();
            JButton backButton = new JButton("Back");
            JButton payButton = new JButton("Buy");

            // add south panel components
            southPanel.add(backButton, BorderLayout.WEST);
            southPanel.add(payButton, BorderLayout.EAST);

            // add panels to main panel
            add(paymentInfoPanel, BorderLayout.CENTER);
            add(southPanel, BorderLayout.SOUTH);

            // add focus listeners to text fields
            for (JTextField t : List.of(nameField, surnameField, cardNumberField, cvvField)) {
                t.addFocusListener(new java.awt.event.FocusAdapter() {
                    final String name = t.getText();

                    public void focusGained(java.awt.event.FocusEvent evt) {
                        if (t.getText().equals(name)) {
                            t.setText("");
                        }
                    }

                    public void focusLost(java.awt.event.FocusEvent evt) {
                        if (t.getText().equals("")) {
                            t.setText(name);
                        }
                    }
                });
            }

            // add action listeners to buttons
            backButton.addActionListener(e -> {
                cardLayout.show(TicketSellingPanel.this, "Seat");
                if (ticket != null) {
                    Ticket.cancelTicket(ticket.getId());
                }
                seatSelectionPanel.listSeats(selectedSession);
            });

            payButton.addActionListener(e -> {

                if (false == checkPaymentInfo(nameField.getText(), surnameField.getText(), cardNumberField.getText(),
                        cvvField.getText(), (java.util.Date) expiryDateSpinner.getValue())) {
                    return;
                }

                Ticket.verifyPurchase(ticket.getId());

                List<Seat> seats = new ArrayList<>(selectedSeats.size());
                for (SeatAvailability sa : selectedSeats) {
                    try {
                        seats.add(DatabaseManager.getRowById(Seat.class, sa.getSeatId()));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

                String selectedSeatRowAndCols = "";
                for (int i = 0; i < seats.size(); i++) {
                    selectedSeatRowAndCols += (char) ('A' + seats.get(i).getRow());
                    selectedSeatRowAndCols += seats.get(i).getCol();

                    if (i != seats.size() - 1) {
                        selectedSeatRowAndCols += ", ";
                    } 
                    
                    if ((i + 1) % 3 == 0) {
                        selectedSeatRowAndCols += "\n";
                    }
                }

                JOptionPane.showMessageDialog(this,
                        "Payment successful!" + "\n" + "Your ticket code is: " + ticket.getCode() + "\n"
                                + "Enjoy the movie!" + "\n" + "Screening Room: " + seats.get(0).getScreeningRoomId() + "\n" + "Selected seats:" + "\n" + selectedSeatRowAndCols);

                ticket = null;
                selectedSeats.clear();

                showMainMenu();
            });
        }

        // check if the payment information is valid temorarily
        private boolean checkPaymentInfo(String name, String surname, String cardNumber, String cvv,
                java.util.Date expiryDate) {
            if (name.equals("Ad") || surname.equals("Soyad") || cardNumber.equals("Kart Numarası")
                    || cvv.equals("CVV")) {
                JOptionPane.showMessageDialog(this, "Please fill in all the payment information.");
                return false;
            }

            if (cardNumber.length() != 16) {
                JOptionPane.showMessageDialog(this, "Card number must be 16 digits long.");
                return false;
            }

            if (cvv.length() != 3) {
                JOptionPane.showMessageDialog(this, "CVV must be 3 digits long.");
                return false;
            }

            if (expiryDate.before(new java.util.Date())) {
                JOptionPane.showMessageDialog(this, "Card has expired.");
                return false;
            }

            return true;
        }
    }
}
