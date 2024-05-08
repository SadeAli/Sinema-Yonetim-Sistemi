package cinema;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import database.*;

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
}
