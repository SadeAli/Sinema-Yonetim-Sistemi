package cinema;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName("ticket")
public class Ticket {
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("is_rated")
	private boolean isRated;

	@ColumnName("seat_count")
	private int seatCount;

	// Constructors

	private Ticket() {}

	public Ticket(boolean isRated, int seatCount) {
		this.isRated = isRated;
		this.seatCount = seatCount;
	}

	// Getters

	public int getId() {
		return id;
	}

	public boolean isRated() {
		return isRated;
	}

	public int getSeatCount() {
		return seatCount;
	}

	// Setters


	public static List<Ticket> getAllTickets() {
		try {
			return DatabaseManager.getAllRows(Ticket.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Ticket>();
		}
	}
}
