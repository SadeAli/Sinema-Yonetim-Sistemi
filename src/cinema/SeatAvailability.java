package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

	public static Ticket bookSeatList (List<SeatAvailability> seatAvList, double price) {
		String sql = "UPDATE seat_availability SET is_available = 0, ticket_id = ? WHERE id = ?";
		String sqlTicket = DatabaseAnnotationUtils.getInsertQuery(Ticket.class);
		String sqlTicketCode = "UPDATE ticket SET code = ? WHERE id = ?";
		Integer ticketId = null;
		int code = 0;
		int rand = new Random().nextInt(10000);

		// Check if the list is empty
		if (seatAvList.isEmpty()) {
			return (Ticket)null;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		PreparedStatement stmtTicket = null;
		PreparedStatement stmtTicketCode = null;
		try {
			conn = DatabaseManager.getConnection();
			stmt = conn.prepareStatement(sql);
			stmtTicket = conn.prepareStatement(sqlTicket);
			stmtTicketCode = conn.prepareStatement(sqlTicketCode);

			// Start the transaction
			conn.setAutoCommit(false);

			// Create a ticket
			Ticket ticket = new Ticket(false, false, LocalDate.now(), price);

			// Set the values of the ticket
			DatabaseAnnotationUtils.setPreparedStatementValueSet(
				DatabaseAnnotationUtils.getColumnNamesAndFields(
					Ticket.class), 
				ticket, stmtTicket);
			
			// Insert the ticket and get the id
			stmtTicket.executeUpdate();
			ResultSet ticketResult = stmtTicket.getGeneratedKeys();
			if (ticketResult.next()) {
				ticketId = ticketResult.getInt(1);
			} else {
				throw new SQLException("Unable to get the ticket id");
			}

			ticket.setId(ticketId);

			// Generate code
			code = (ticketId%10) + ((ticketId/10)%10)*100 + ((ticketId/100)%10)*10000 + ((ticketId/1000)%10)*1000000;
			code += (rand%10)*10 + ((rand/10)%10)*1000 + ((rand/100)%10)*100000 + ((rand/1000)%10)*10000000;
			ticket.setCode(code);

			// Update the ticket code
			stmtTicketCode.setInt(1, code);
			stmtTicketCode.setInt(2, ticketId);
			stmtTicketCode.executeUpdate();

			// Update the seat availabilities
			for (SeatAvailability seatAv : seatAvList) {
				SeatAvailability seatAvDB = DatabaseManager.getRowById(SeatAvailability.class, seatAv.getId());
				if (seatAvDB == null || !seatAvDB.isAvailable()) {
					throw new SQLException("Seat is not available");
				}
				stmt.setInt(1, ticket.getId());
				stmt.setInt(2, seatAv.getId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			conn.commit();
			return ticket;
		} catch (Exception e) {
			System.err.println("Unable to book seat list: " + e.getMessage());
			if (conn != null) {
				try {
					conn.rollback();
				} catch (Exception e2) {
					System.err.println("Unable to rollback transaction: " + e2.getMessage());
				}
			}
			return (Ticket)null;
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (Exception e) {
					System.err.println("Unable to close statement: " + e.getMessage());
				}
			}
			if (stmtTicket != null) {
				try {
					stmtTicket.close();
				} catch (Exception e) {
					System.err.println("Unable to close statement: " + e.getMessage());
				}
			}
			if (stmtTicketCode != null) {
				try {
					stmtTicketCode.close();
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
