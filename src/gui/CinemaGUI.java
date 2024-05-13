package gui;

import cinema.Movie;
import cinema.ScreeningRoom;
import cinema.SeatAvailability;
import cinema.Session;
import cinema.Ticket;
import database.DatabaseManager;
import database.FilterCondition;
import gui.mainPanels.AdminPanel;
import gui.mainPanels.MovieRatingPanel;
import gui.mainPanels.TicketSellingPanel;
import java.awt.CardLayout;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CinemaGUI extends JFrame {
    static final int width = 1000;
    static final int height = 800;

    private JPanel mainPanel = new JPanel();
    private CardLayout m_cardLayout = new CardLayout();

    private MainMenuPanel mainMenuPanel = new MainMenuPanel(this, width, height);
    private TicketSellingPanel ticketSellingPanel = new TicketSellingPanel(this, width, height);
    private MovieRatingPanel movieRatingPanel = new MovieRatingPanel(this, width, height);
    private AdminPanel adminPanel = new AdminPanel(this, width, height);

    public static void main(String[] args) {

        String lookAndFeel[] = {
                "javax.swing.plaf.metal.MetalLookAndFeel",
                "javax.swing.plaf.nimbus.NimbusLookAndFeel",
                "com.sun.java.swing.plaf.motif.MotifLookAndFeel",
                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
                "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel"
        };

        try {
            UIManager.setLookAndFeel(lookAndFeel[3]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        @SuppressWarnings("unused")
        CinemaGUI cinemaGUI = new CinemaGUI();
    }

    CinemaGUI() {
        setSize(width, height);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // card layout for the main window
        mainPanel.setLayout(m_cardLayout);
        add(mainPanel);

        // tabbed pane 0 is the main menu panel
        mainPanel.add("Main Menu", mainMenuPanel);
        mainPanel.add("Select a Movie", ticketSellingPanel);
        mainPanel.add("Rate a Movie", movieRatingPanel);
        mainPanel.add("Admin Login", adminPanel);

        showMainMenu();
        setVisible(true);
    }

    public void showMainMenu() {
        m_cardLayout.show(mainPanel, "Main Menu");
    }

    private class MainMenuPanel extends JPanel {
        MainMenuPanel(CinemaGUI parent, int width, int height) {
            setLayout(null);

            // center 3 buttons with 100px width and 50px height horizontally
            int buttonWidth = 150;
            int buttonHeight = 50;

            int buttonX = (width - buttonWidth) / 2;
            int buttonY = (height - buttonHeight) / 2 - 200;

            // create 3 buttons
            JButton button1 = new JButton("Select a Movie");
            button1.setBounds(buttonX - (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button1.addActionListener(e -> {
                m_cardLayout.show(mainPanel, "Select a Movie");
                ticketSellingPanel.onVisible();
            });
            add(button1);

            JButton button2 = new JButton("Rate a Movie");
            button2.setBounds(buttonX, buttonY, buttonWidth, buttonHeight);
            button2.addActionListener(e -> m_cardLayout.show(mainPanel, "Rate a Movie"));
            add(button2);

            JButton button3 = new JButton("Admin Login");
            button3.setBounds(buttonX + (int) (1.5 * buttonWidth), buttonY, buttonWidth, buttonHeight);
            button3.addActionListener(e -> {
                String password = JOptionPane.showInputDialog(this, "Enter password:");

                if (password == null) {
                    return;
                }

                if (isValidPassword(password)) {
                    m_cardLayout.show(mainPanel, "Admin Login");
                    CinemaGUI.this.adminPanel.onVisible();
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid password!");
                }
            });
            add(button3);
        }

        private boolean isValidPassword(String password) {
            return password != null && password.equals("admin");
        }
    }

	public static void createFakeTickets() {
		List<Session> sessionList = Session.getAllSessions();
		Random rand = new Random();

		double price = 70.0;
		
		for (Session session : sessionList) {
			try {
				List<SeatAvailability> seatAvList = DatabaseManager.getRowsFilteredAndSortedBy(
						SeatAvailability.class,
						List.of(
								new FilterCondition("sessionId", session.getId(), FilterCondition.Relation.EQUALS),
								new FilterCondition("isAvailable", true, FilterCondition.Relation.EQUALS)
						),
						null,
						false);
				
				List<SeatAvailability> seatsToBook = new ArrayList<>();
				
				Movie movie = DatabaseManager.getRowById(Movie.class, session.getMovieId());
				float rating = movie.getRating();
				
				float randFloat = rand.nextFloat(1);
				int maxSeats = (int) ((1 - randFloat*randFloat) * (rating / 5 + 0.5) * seatAvList.size());
				for (int i = 0; i < seatAvList.size() && i < maxSeats; i++) {
					seatsToBook.add(seatAvList.get(i));
				}
				
			
				if (seatsToBook.size() > 0)
					Ticket.verifyPurchase(SeatAvailability.bookSeatList(seatsToBook, seatsToBook.size()*price).getId());
			} catch (Exception ex) {
				System.err.println("Unable to create fake tickets: " + ex.getMessage());
			}
		}
	}

	public static void createSessionBackwards() {
		List<ScreeningRoom> screeningRoomList = ScreeningRoom.getAllScreeningRooms();
		Random rand = new Random();

		LocalDate firstDate	= LocalDate.now().minusDays(30);
		for (LocalDate date = LocalDate.now(); date.isAfter(firstDate); date = date.minusDays(1)) {
			try {
				List<Movie> movieList = DatabaseManager.getRowsFilteredAndSortedBy(
						Movie.class,
						List.of(
								new FilterCondition("releaseDate", date, FilterCondition.Relation.LESS_THAN_OR_EQUALS),
								new FilterCondition("lastScreeningDate", date, FilterCondition.Relation.GREATER_THAN_OR_EQUALS)
						),
						"rating",
						true
				);
				
						
				for (ScreeningRoom screeningRoom : screeningRoomList) {
					float randFloat = rand.nextFloat(1);
					int movieIndex = (int) ((1 - randFloat * randFloat) * movieList.size());
					
					if (movieIndex < 0) {
						movieIndex = 0;
					} else if (movieIndex >= movieList.size()) {
						movieIndex = movieList.size() - 1;
					}
					screeningRoom.addMovieToDate(date, movieList.get(movieIndex).getId());
				}
			} catch (Exception ex) {
				System.err.println("Unable to create session: " + ex.getMessage());
			}
		}
	}
}
