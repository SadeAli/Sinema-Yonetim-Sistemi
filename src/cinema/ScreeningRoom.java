package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import database.*;
import java.sql.Statement;

@TableName("screening_room")
public class ScreeningRoom {
	private static LocalTime openingTime = LocalTime.of(9, 0);
	private static LocalTime closingTime = LocalTime.of(23, 59);
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("seat_row_count")
	private int seatRowCount;

	@ColumnName("seat_col_count")
	private int seatColCount;


	private ScreeningRoom() {
		// For database
	}

	public ScreeningRoom(int seatRowCount, int seatColCount) {
		this.seatRowCount = seatRowCount;
		this.seatColCount = seatColCount;
	}

	public static LocalTime getOpeningTime() {
		return openingTime;
	}

	public static LocalTime getClosingTime() {
		return closingTime;
	}

	// Getters

	public int getId() {
		return id;
	}

	public int getSeatRowCount() {
		return seatRowCount;
	}

	public int getSeatColCount() {
		return seatColCount;
	}

	// Setters

	private void setID (int id) {
		this.id = id;
	}

	public static List<ScreeningRoom> getAllScreeningRooms() {
		try {
			return DatabaseManager.getAllRows(ScreeningRoom.class);
		} catch (Exception e) {
			System.err.println("Unable to get screening rooms: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	
	/**
	 * Sets the active hours for the screening room.
	 * The active hours determine the opening and closing time of the screening room.
	 * 
	 * @param openingTime the opening time of the screening room
	 * @param closingTime the closing time of the screening room
	 * @return true if the active hours were set successfully, false otherwise
	 */
	public static boolean setActiveHours(LocalTime openingTime, 
			LocalTime closingTime) {
		if (openingTime.isAfter(closingTime)) {
			return false;
		}
		ScreeningRoom.openingTime = openingTime;
		ScreeningRoom.closingTime = closingTime;
		return true;
	}

	/**
	 * Adds a movie to the specified date in the screening room.
	 * 
	 * @param date The date to add the movie to.
	 * @param movieId The ID of the movie to add.
	 * @return true if the movie was successfully added, false otherwise.
	 */
	public boolean addMovieToDate(LocalDate date, int movieId) {
		List<Session> sessionList = new ArrayList<>();

		// Check if the movie is already added to the date
		List<FilterCondition> filters = new ArrayList<>();
		filters.add(new FilterCondition("date", date, 
				FilterCondition.Relation.EQUALS));
		filters.add(new FilterCondition("screeningRoomId", this.id, 
				FilterCondition.Relation.EQUALS));
		try {
			// If the movie is not already added to the date, add it
			if (!DatabaseManager.exists(Session.class, filters)) {
				LocalDateTime closingDateTime = LocalDateTime.of(date, 
						ScreeningRoom.closingTime);
				LocalDateTime time = LocalDateTime.of(date, 
						ScreeningRoom.openingTime);
				int extendedDuration = Session.calculateExtendedDuration(DatabaseManager.getRowById(Movie.class, movieId).getDuration());
				while (time.plusMinutes(extendedDuration).isBefore(closingDateTime)) {
					sessionList.add(new Session(movieId, this.id, date, 
							LocalTime.from(time.toLocalTime()), extendedDuration));

					time = time.plusMinutes(extendedDuration);
				}

				Session.insertList(sessionList);
				return true;
			}
		} catch (Exception e) {
			System.err.println("Unable to add movie to date: " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Inserts the screening room and its seats into the database.
	 * 
	 * @return true if the insertion is successful, false otherwise.
	 */
	public boolean insertToDatabase() {

		List<Seat> seatList = new ArrayList<>();

		// Create the query
		String queryScreeningRoom = DatabaseAnnotationUtils.getInsertQuery(ScreeningRoom.class);
		String querySeat = DatabaseAnnotationUtils.getInsertQuery(Seat.class);

		// Execute the query

		Connection connection = null;
		PreparedStatement stmtScreeningRoom = null;
		PreparedStatement stmtSeat = null;

		try{
			connection = DatabaseManager.getConnection();
			stmtScreeningRoom = connection.prepareStatement(queryScreeningRoom, PreparedStatement.RETURN_GENERATED_KEYS);
			stmtSeat = connection.prepareStatement(querySeat);
		
			// Add a check for seat_col_count
			if (this.getSeatRowCount() == 0 || this.getSeatColCount() == 0) {
				throw new IllegalArgumentException("seat_col_count cannot be null");
			}

			DatabaseAnnotationUtils.setPreparedStatementValueSet(
				DatabaseAnnotationUtils.getColumnNamesAndFields(ScreeningRoom.class),
				this, stmtScreeningRoom);


			// Start a transaction
			connection.setAutoCommit(false);
			
			stmtScreeningRoom.executeUpdate();
			ResultSet rsId = stmtScreeningRoom.getGeneratedKeys();

			// Get the generated id of the screening room
			if (rsId.next()) {
				this.setID(rsId.getInt(1));
			} else {
				throw new SQLException("Unable to get the generated id");
			}

			// Create the seats
			for (int i = 0; i < this.getSeatRowCount(); i++) {
				for (int j = 0; j < this.getSeatColCount(); j++) {
					seatList.add(new Seat(this.id, i, j));
				}
			}
			
			// Add seats to the batch
			for (Seat seat : seatList) {
				DatabaseAnnotationUtils.setPreparedStatementValueSet(
					DatabaseAnnotationUtils.getColumnNamesAndFields(Seat.class), 
					seat, stmtSeat);
				stmtSeat.addBatch();
			}

			// Execute the batch
			stmtSeat.executeBatch();
			
			// Commit the transaction
			connection.commit();
			return true;
		} catch (Exception e) {
			
			if (connection != null) {
				try {
					// Roll back the transaction if something went wrong
					connection.rollback();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			
			System.err.println("Unable to insert screening room: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
		finally {
			// Close the connection
			if (stmtScreeningRoom != null) {
				try {
					stmtScreeningRoom.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the statement: " + e.getMessage());
					e.printStackTrace();
				}
			}
			if (stmtSeat != null) {
				try {
					stmtSeat.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the statement: " + e.getMessage());
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("Unable to set auto commit to true: " + e.getMessage());
					e.printStackTrace();
				}
				try {
					connection.close();
				} catch (SQLException e) {
					System.err.println("Unable to close the connection: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Deletes a screening room from the database.
	 *
	 * @param id   the ID of the screening room to be deleted
	 * @param conn the database connection
	 * @return true if the screening room is successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabase(int id, Connection conn) {
		String query = "DELETE FROM screening_room WHERE id = ?";
		String querySeat = "DELETE FROM seat WHERE screening_room_id = ?";
		String queryCheckTicket = "SELECT ticket.id"
			+ " FROM ticket"
			+ " JOIN seat_availability ON seat_availability.ticket_id = ticket.id"
			+ " JOIN session ON session.id = seat_availability.session_id"
			+ " WHERE session.screening_room_id = ?";

		PreparedStatement ps = null;
		PreparedStatement psSeat = null;
		PreparedStatement psCheckTicket = null;

		try {
			// Check if there are tickets associated with the screening room
			psCheckTicket = conn.prepareStatement(queryCheckTicket);
			psCheckTicket.setInt(1, id);
			ResultSet rs = psCheckTicket.executeQuery();
			if (rs.next()) {
				throw new SQLException("Unable to delete screening room: there are tickets associated with this screening room");
			}

			// Delete sessions associated with the screening room
			if (!Session.deleteSessionsWithScreeningRoomId(id, conn)) {
				throw new SQLException("Unable to delete sessions");
			}

			// Delete the seats associated with the screening room
			psSeat = conn.prepareStatement(querySeat);
			psSeat.setInt(1, id);
			psSeat.executeUpdate();

			// Delete the screening room
			ps = conn.prepareStatement(query);
			ps.setInt(1, id);
			ps.executeUpdate();


			return true;
		} catch (SQLException e) {
			System.err.println("Unable to delete screening room: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			List<Statement> sList = new ArrayList<>();
			sList.add(ps);
			sList.add(psSeat);
			sList.add(psCheckTicket);
			DatabaseManager.closeStatements(sList);
		}
	}

	/**
	 * Deletes a screening room from the database based on the given ID.
	 * 
	 * @param id the ID of the screening room to be deleted
	 * @return true if the screening room is successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabase(int id) {
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			conn.setAutoCommit(false);
			
			if (!deleteFromDatabase(id, conn)) {
				throw new SQLException("Unable to delete screening room");
			}

			conn.commit();
			return true;
		} catch (SQLException e) {
			System.err.println("Unable to delete screening room: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			if (!DatabaseManager.setAutoCommit(conn, true)) {
				System.err.println("Unable to set auto commit to true");
			}
			DatabaseManager.closeConnection(conn);
		}
	}

	/**
	 * Deletes all sessions from a specific screening room on a given date.
	 * 
	 * @param screeningRoomId the ID of the screening room
	 * @param date the date for which sessions should be deleted
	 * @param conn the database connection
	 * @return true if the sessions were successfully deleted, false otherwise
	 */
	public static boolean deleteSessionsFromDate(int screeningRoomId, LocalDate date, Connection conn) {
		try {
			List<Session> sessionList = DatabaseManager.getRowsFilteredAndSortedBy(
				Session.class,
				List.of(new FilterCondition(
						"screeningRoomId",
						screeningRoomId,
					FilterCondition.Relation.EQUALS),
					new FilterCondition(
						"date",
						date,
						FilterCondition.Relation.EQUALS)),
				null,
				false);

			for (Session session : sessionList) {
				if (!Session.deleteFromDatabase(session.getId(), conn)) {
					throw new SQLException("Unable to delete session");
				}
			}
			return true;
		} catch (Exception e) {
			System.err.println("Unable to delete sessions from date: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes sessions from a specific screening room on a given date.
	 * 
	 * @param screeningRoomId the ID of the screening room
	 * @param date the date for which sessions should be deleted
	 * @return true if the sessions were successfully deleted, false otherwise
	 */
	public static boolean deleteSessionsFromDate(int screeningRoomId, LocalDate date) {
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			DatabaseManager.setAutoCommit(conn, false);
			
			if (!deleteSessionsFromDate(screeningRoomId, date, conn)) {
				throw new SQLException("Unable to delete sessions");
			}

			if (!DatabaseManager.commit(conn)) {
				throw new SQLException("Unable to commit");
			}

			return true;
		} catch (SQLException e) {
			DatabaseManager.rollback(conn);
			System.err.println("Unable to delete sessions from date: " + e.getMessage());
			e.printStackTrace();
			return false;
		} finally {
			if (!DatabaseManager.setAutoCommit(conn, true)) {
				System.err.println("Unable to set auto commit to true");
			}
			DatabaseManager.closeConnection(conn);
		}
	}

	/**
	 * Deletes all sessions from the specified date in this screening room.
	 *
	 * @param date the date from which sessions should be deleted
	 * @return true if sessions were successfully deleted, false otherwise
	 */
	public boolean deleteSessionsFromDate(LocalDate date) {
		return deleteSessionsFromDate(this.id, date);
	}
}
