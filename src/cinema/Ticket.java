package cinema;
import java.time.LocalDate;
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

	@ColumnName("purchase_date")
	private LocalDate purchaseDate;

	// Constructors

	private Ticket() {}

	public Ticket(boolean isRated, boolean isPaid, LocalDate purchaseDate) {
		this.isRated = isRated;
		this.isPaid = isPaid;
		this.purchaseDate = purchaseDate;
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

	public LocalDate getPurchaseDate() {
		return purchaseDate;
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
