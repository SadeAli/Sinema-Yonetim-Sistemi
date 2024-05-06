import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dbanno.*;

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

	@ColumnName("price")
	private double price;

	// Constructors

	private Session() {}

	public Session(int movieId, int screeningRoomId, LocalDate date, LocalTime startTime) {

		//TODO Check if the screening room is available at the given time.
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

	public double getPrice() {
		return price;
	}

	// Setters


	public static List<Session> getAllSessions() {
		try {
			return DatabaseManager.getAllRows(Session.class);
		} catch (Exception e) {
			System.err.println("Unable to get sessions: " + e.getMessage());
			return new ArrayList<>();
		}
	}
}
