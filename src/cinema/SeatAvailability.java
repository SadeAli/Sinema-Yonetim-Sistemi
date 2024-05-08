package cinema;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName("seat_availability")
public class SeatAvailability {
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("is_available")
	private boolean isAvailable;

	@ForeignKey(referencedClass = Ticket.class)
	@ColumnName("ticket_id")
	private int ticketId;

	@ForeignKey(referencedClass = Session.class)
	@ColumnName("session_id")
	private int sessionId;

	@ForeignKey(referencedClass = Seat.class)
	@ColumnName("seat_id")
	private int seatId;

	// Constructors

	private SeatAvailability() {}

	public SeatAvailability(boolean isAvailable, int ticketId, int sessionId, int seatId) {
		this.isAvailable = isAvailable;
		this.ticketId = ticketId;
		this.sessionId = sessionId;
		this.seatId = seatId;
	}

	// Getters

	public int getId() {
		return id;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	public int getTicketId() {
		return ticketId;
	}

	public int getSessionId() {
		return sessionId;
	}

	public int getSeatId() {
		return seatId;
	}

	// Setters


	public static List<SeatAvailability> getAllSeatAvailabilities() {
		try {
			return DatabaseManager.getAllRows(SeatAvailability.class);
		} catch (Exception e) {
			System.err.println("Unable to get seat availabilities: " + e.getMessage());
			return new ArrayList<>();
		}
	}
}
