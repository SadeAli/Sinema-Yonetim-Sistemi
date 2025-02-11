package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

	@ColumnName("code")
	private int code;

	@ColumnName("price")
	private double price;
	// Constructors

	private Ticket() {}

	public Ticket(boolean isRated, boolean isPaid, LocalDate purchaseDate, double price) {
		this.isRated = isRated;
		this.isPaid = isPaid;
		this.purchaseDate = purchaseDate;
		this.price = price;
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

	public int getCode() {
		return code;
	}

	public double getPrice() {
		return price;
	}

	// Setters

	public void setId (int id) {
		this.id = id;
	}

	public void setCode (int code) {
		this.code = code;
	}


	/**
	 * Retrieves all tickets from the database.
	 * 
	 * @return a list of Ticket objects representing all the tickets in the database
	 */
	public static List<Ticket> getAllTickets() {
		try {
			return DatabaseManager.getAllRows(Ticket.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<Ticket>();
		}
	}

	
	/**
	 * Cancels a ticket by updating the seat availability and deleting the ticket from the database.
	 * 
	 * @param ticketId the ID of the ticket to be canceled
	 * @return true if the ticket was successfully canceled, false otherwise
	 */
	public static boolean cancelTicket(int ticketId) {
		String sql = "UPDATE seat_availability SET is_available = 1, ticket_id = NULL WHERE ticket_id = ?";
		String sqlGetTicket = "SELECT is_paid FROM ticket WHERE id = ?";
		String sqlDelete = "DELETE FROM ticket WHERE id = ?";

		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement psGetTicket = null;
		PreparedStatement psDelete = null;
		try {
			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(sql);
			psGetTicket = conn.prepareStatement(sqlGetTicket);
			psDelete = conn.prepareStatement(sqlDelete);

			conn.setAutoCommit(false);
			
			// Get Ticket
			psGetTicket.setInt(1, ticketId);
			ResultSet rs = psGetTicket.executeQuery();

			if (!rs.next()) {
				throw new SQLException("Ticket not found");
			}

			if (rs.getBoolean("is_paid")) {
				throw new SQLException("Ticket is already paid");
			}


			// Update seat availabilities
			ps.setInt(1, ticketId);
			ps.executeUpdate();

			// Delete ticket
			psDelete.setInt(1, ticketId);
			psDelete.executeUpdate();

			conn.commit();
			return true;

		} catch (Exception e) {
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
			if (psGetTicket != null) {
				try {
					psGetTicket.close();
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

	/**
	 * Verifies the purchase of a ticket by updating the 'is_paid' field in the database.
	 * 
	 * @param ticketId the ID of the ticket to be verified
	 * @return true if the purchase is successfully verified, false otherwise
	 */
	public static boolean verifyPurchase(int ticketId) {
		String sql = "UPDATE ticket SET is_paid = 1 WHERE id = ?";

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(sql);

			ps.setInt(1, ticketId);
			ps.executeUpdate();

			return true;

		} catch (Exception e) {
			System.err.println("Unable to verify purchase: " + e.getMessage());
			return false;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the statement: " + e.getMessage());
				}
			}
			if (conn != null) {
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
