import java.time.LocalDate;
import java.time.LocalTime;

public class Session {
	private int id;
	private int movieId;
	private int screeningRoomId;
	private LocalDate date;
	private LocalTime startTime;
	private int extendedDuration;

	public Session(int movieId, int screeningRoomId, LocalDate date, LocalTime startTime) {

		//TODO Check if the screening room is available at the given time.
		
	}
}
