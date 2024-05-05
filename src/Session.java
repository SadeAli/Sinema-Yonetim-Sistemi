import java.time.LocalDate;
import java.time.LocalTime;

public class Session {
	private int id;
	private int movieId;
	private int screeningRoomId;
	private LocalDate date;
	private LocalTime startTime;
	private int extendedDuration;

	public Session(int movieID, int screeningRoomID, LocalDate date, LocalTime startTime) {

		//TODO Check if the screening room is available at the given time.
		
		this.movieID = movieID;
		this.screeningRoomID = screeningRoomID;
		this.startTime = LocalTime.from(startTime);
		this.date = LocalDate.from(date);
		
		// duration = extendedDuration(Movie.getByID(movieID).getDuration());
		
		lastID++;
		ID = lastID;
		
		sessions.add(this);
	
	}
}
