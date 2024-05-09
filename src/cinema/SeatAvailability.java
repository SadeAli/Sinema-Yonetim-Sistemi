package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
	private Integer ticketId; // nullable

	@ForeignKey(referencedClass = Session.class)
	@ColumnName("session_id")
	private int sessionId;

	@ForeignKey(referencedClass = Seat.class)
	@ColumnName("seat_id")
	private int seatId;

	// Constructors

	private SeatAvailability() {}

	public SeatAvailability(boolean isAvailable, Integer ticketId, int sessionId, int seatId) {
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

	public Integer getTicketId() {
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

	public static boolean bookSeatList (List<SeatAvailability> seatAvList) {
		String sql = "UPDATE seat_availability SET is_available = 0 WHERE id = ?";
		// Check if the list is empty
		if (seatAvList.isEmpty()) {
			return false;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement(sql);

			// Start the transaction
			conn.setAutoCommit(false);
			for (SeatAvailability seatAv : seatAvList) {
				SeatAvailability seatAvDB = DatabaseManager.getRowById(SeatAvailability.class, seatAv.getId());
				if (seatAvDB == null || !seatAvDB.isAvailable()) {
					return false;
				}
				stmt.setInt(1, seatAv.getId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			return true;
		} catch (Exception e) {
			System.err.println("Unable to book seat list: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (Exception e2) {
					System.err.println("Unable to rollback transaction: " + e2.getMessage());
				}
			}
			return false;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					System.err.println("Unable to close statement: " + e.getMessage());
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (Exception e) {
					System.err.println("Unable to set auto commit to true: " + e.getMessage());
				}

				try {
					conn.close();
				} catch (Exception e) {
					System.err.println("Unable to close connection: " + e.getMessage());
				}
			}
		}
	}
}
