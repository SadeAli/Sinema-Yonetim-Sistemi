import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

//TODO change ID with id in all classes

public class Session {
	
	private int ID;
	private static int lastID = 0;
	private int movieID;
	private int screeningRoomID;
	private LocalDate date;
	private LocalTime startTime;
	private int duration;
	//private int[][] tickets;
	private static ArrayList<Session> sessions = new ArrayList<>();
	
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
		
		//tickets = new int[ScreeningRoom.getVerticalSeatNumber()][ScreeningRoom.getHorizontalSeatNumber()];
	}
	
	public static Session getByID(int sesid) {
		for (Session ses : sessions) {
			if (ses.ID == sesid) {
				return ses;
			}
		}
		return null;
	}

	public int getID() {
		return ID;
	}

	public static int getLastID() {
		return lastID;
	}

	public int getMovieID() {
		return movieID;
	}

	public int getScreeningRoomID() {
		return screeningRoomID;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public int getDuration() {
		return duration;
	}
	
	/**
	 * Calculates the extended duration of a session with breaks.
	 * 
	 * @param dur the duration of the movie in minutes
	 * @return the extended duration of the session with breaks
	 */
	public static int extendedDuration(int dur) {
		return (dur + ((dur / 60) * 10) + 30);	//10 min breaks every one hour, 30 min break between sessions.
	}

	@Override
	public String toString() {
		return "Session [ID=" + ID + ", movieID=" + movieID + ", movieName=" + "Movie.getByID(movieID).getName()" + ", screeningRoomID=" + screeningRoomID + ", date="
				+ date +", startTime="
				+ startTime + ", duration=" + duration + "]";
	}
}
