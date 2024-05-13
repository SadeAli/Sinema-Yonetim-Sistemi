package cinema;
import database.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@TableName("session")
public class Session {

	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ForeignKey(referencedClass = Movie.class)
	@ColumnName("movie_id")
	private int movieId;

	@ForeignKey(referencedClass = ScreeningRoom.class)
	@ColumnName("screening_room_id")
	private int screeningRoomId;

	@ColumnName("date")
	private LocalDate date;

	@ColumnName("start_time")
	private LocalTime startTime;

	@ColumnName("duration")
	private int extendedDuration;


	// Constructors

	private Session() {}

	public Session(int movieId, int screeningRoomId, LocalDate date, LocalTime startTime, int extendedDuration) {

		//TODO Check if the screening room is available at the given time.

		this.movieId = movieId;
		this.screeningRoomId = screeningRoomId;
		this.date = date;
		this.startTime = startTime;
		this.extendedDuration = extendedDuration;
	}

	// Getters

	public int getId() {
		return id;
	}

	public int getMovieId() {
		return movieId;
	}

	public int getScreeningRoomId() {
		return screeningRoomId;
	}

	public LocalDate getDate() {
		return date;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public int getExtendedDuration() {
		return extendedDuration;
	}

	// Setters

		/**
	 * Calculates the extended duration of a session with breaks.
	 * 
	 * @param dur the duration of the movie in minutes
	 * @return the extended duration of the session with breaks
	 */
	public static int calculateExtendedDuration(int dur) {
		return (dur + ((dur / 60) * 10) + 30);	//10 min breaks every one hour, 30 min break between sessions.
	}

	public static List<Session> getAllSessions() {
		try {
			return DatabaseManager.getAllRows(Session.class);
		} catch (Exception e) {
			System.err.println("Unable to get sessions: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	protected static boolean insertList(List<Session> sessionList) {
		// Check if the list is empty
		if (sessionList.isEmpty()) {
			return false;
		}

		int screeningRoomId = sessionList.getFirst().getScreeningRoomId();

		Map<String, Field> columnFieldMapSession = DatabaseAnnotationUtils.getColumnNamesAndFields(Session.class);
		Map<String, Field> columnFieldMapSeatAvailability = DatabaseAnnotationUtils.getColumnNamesAndFields(SeatAvailability.class);

		
		List<Seat> seatList;
		try {
			seatList = DatabaseManager.getRowsFilteredAndSortedBy(
				Seat.class, Arrays.asList(
					new FilterCondition(
						"screeningRoomId", 
						screeningRoomId, 
						FilterCondition.Relation.EQUALS
						)
					), 
				"id", 
				true
			);
		} catch (IllegalAccessException | InstantiationException | NoSuchFieldException | SQLException e) {
			e.printStackTrace();
			return false;
		}

		// Create the query
		String query = DatabaseAnnotationUtils.getInsertQuery(Session.class);
		String querySeatAvailability = DatabaseAnnotationUtils.getInsertQuery(SeatAvailability.class);

		// Execute the query
		Connection conn = null;
		PreparedStatement ps = null;
		PreparedStatement psSeatAvailability = null;
		try {

			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
			psSeatAvailability = conn.prepareStatement(querySeatAvailability);
			conn.setAutoCommit(false);
			
			for (Session session : sessionList) {
				DatabaseAnnotationUtils.setPreparedStatementValueSet(columnFieldMapSession, session, ps);
				ps.executeUpdate();

				ResultSet rsId = ps.getGeneratedKeys();
				if (rsId.next()) {
					session.id = rsId.getInt(1);
				} else {
					throw new SQLException("Unable to get the generated id");
				}

				for (Seat seat : seatList) {
					SeatAvailability seatAvailability = new SeatAvailability(true, (Integer)null, session.getId(), seat.getId());
					DatabaseAnnotationUtils.setPreparedStatementValueSet(columnFieldMapSeatAvailability, seatAvailability, psSeatAvailability);
					psSeatAvailability.addBatch();
				}

			}

			psSeatAvailability.executeBatch();
			conn.commit();
			return true;
		} catch (SQLException e) {

			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException e1) {
					System.err.println("Unable to rollback transaction: " + e1.getMessage());
				}
			}

			System.err.println("Unable to insert sessions: " + e.getMessage());
			return false;
		} finally {
			// Close the connection
			try {
				if (ps != null) {
					ps.close();
				}
				if (psSeatAvailability != null) {
					psSeatAvailability.close();
				}
				if (conn != null) {
					conn.setAutoCommit(true);
					conn.close();
				}
			} catch (SQLException e) {
				System.err.println("Unable to close connection: " + e.getMessage());
			}
		}
	}

	public static boolean deleteFromDatabase(int sessionId, Connection conn) {
		String query = "DELETE FROM session WHERE id = ?";
		String querySeatAvailability = "DELETE FROM seat_availability WHERE session_id = ?";
		String queryCheckTicket = "SELECT ticket.id FROM ticket"
			+ " JOIN seat_availability ON seat_availability.ticket_id = ticket.id"
			+ " WHERE seat_availability.session_id = ?"
			+ " LIMIT 1";


		PreparedStatement ps = null;
		PreparedStatement psSeatAvailability = null;
		PreparedStatement psCheckTicket = null;

		try {

			psCheckTicket = conn.prepareStatement(queryCheckTicket);
			
			// Set the session id
			psCheckTicket.setInt(1, sessionId);

			// Check if there are tickets associated with this session
			ResultSet rs = psCheckTicket.executeQuery();
			
			if (rs == null || rs.next()) {
				throw new SQLException("Unable to delete session: there are tickets associated with this session");
			}

			psSeatAvailability = conn.prepareStatement(querySeatAvailability);
			psSeatAvailability.setInt(1, sessionId);
			psSeatAvailability.executeUpdate();

			ps = conn.prepareStatement(query);
			ps.setInt(1, sessionId);
			ps.executeUpdate();

			return true;
		} catch (SQLException e) {
			System.err.println("Unable to delete session: " + e.getMessage());
			return false;
		} finally {
			List<Statement> statementList = new ArrayList<>();
			statementList.add(ps);
			statementList.add(psSeatAvailability);
			statementList.add(psCheckTicket);
			DatabaseManager.closeStatements(statementList);
		}
	}

	public static boolean deleteFromDatabase(int sessionId) {
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			
			DatabaseManager.setAutoCommit(conn, false);
			
			if (!deleteFromDatabase(sessionId, conn)) {
				throw new SQLException("Unable to delete session");
			}

			DatabaseManager.commit(conn);
			
			return true;
		} catch (SQLException e) {
			DatabaseManager.rollback(conn);
			System.err.println("Unable to delete session: " + e.getMessage());
			return false;
		} finally {
			DatabaseManager.setAutoCommit(conn, true);
			DatabaseManager.closeConnection(conn);
		}
	}

	public boolean deleteFromDatabase() {
		return deleteFromDatabase(this.id);
	}
}
