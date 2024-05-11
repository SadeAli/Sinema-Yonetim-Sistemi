package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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

	public static boolean cancelTicket(int ticketId) {
		String sql = "UPDATE seat_availability SET is_available = 1, ticket_id = NULL WHERE ticket_id = ?";
		String sqlDelete = "DELETE FROM ticket WHERE id = ?";

		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement psDelete = null;
		try {
			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(sql);
			psDelete = conn.prepareStatement(sqlDelete);

			conn.setAutoCommit(false);
			
			// Get Ticket
			if (DatabaseManager.exists(
					Ticket.class,
					List.of(
						new FilterCondition(
							"id",
							ticketId,
							FilterCondition.Relation.EQUALS)
					))) {
				return false;
			}

			// Update seat availabilities
			ps.setInt(1, ticketId);
			ps.executeUpdate();

			// Delete ticket
			psDelete.setInt(1, ticketId);
			
			return true;

		} catch (SQLException | IllegalAccessException | InstantiationException | NoSuchFieldException e ) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException excep) {
					System.err.println("Unable to rollback the transaction: " + excep.getMessage());
				}
			}
			System.err.println("Unable to cancel ticket: " + e.getMessage());
			return false;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the statement: " + e.getMessage());
				}
			}
			if (psDelete != null) {
				try {
					psDelete.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the statement: " + e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("Unable to set the autoCommit to true: " + e.getMessage());
				}
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the connection: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
}
