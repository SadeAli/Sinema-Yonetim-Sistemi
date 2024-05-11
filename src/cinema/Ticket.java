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

	@ColumnName("is_paid")
	private boolean isPaid;

	// Constructors

	private Ticket() {}

	public Ticket(boolean isRated, boolean isPaid) {
		this.isRated = isRated;
		this.isPaid = isPaid;
	}

	// Getters

	public int getId() {
		return id;
	}

	public boolean isRated() {
		return isRated;
	}

	public boolean isPaid() {
		return isPaid;
	}

	// Setters

	public void setId (int id) {
		this.id = id;
	}


	public static List<Ticket> getAllTickets() {
		try {
			return DatabaseManager.getAllRows(Ticket.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Ticket>();
		}
	}
}
