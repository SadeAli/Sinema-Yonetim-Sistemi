package biletsatis;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class Session {
	
	private int ID;
	private static int lastID = 0;
	private int movieID;
	private int auditoriumID;
	private LocalDate date;
	private LocalTime startTime;
	private int duration;
	private int[][] tickets;
	private static ArrayList<Session> sessions = new ArrayList<>();
	
	public Session(int movieID, int auditoriumID, LocalDate date, LocalTime startTime) {
		this.movieID = movieID;
		this.auditoriumID = auditoriumID;
		this.startTime = LocalTime.from(startTime);
		this.date = LocalDate.from(date);
		
		duration = extendedDuration(Movie.getByID(movieID).getDuration());
		
		lastID++;
		ID = lastID;
		
		sessions.add(this);
		
		tickets = new int[Auditorium.getVerticalSeatNumber()][Auditorium.getHorizontalSeatNumber()];
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

	public int getAuditoriumID() {
		return auditoriumID;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public int getDuration() {
		return duration;
	}
	
	public static int extendedDuration(int dur) {
		return (dur + ((dur / 60) * 10) + 30);	//10 min breaks every one hour, 30 min break between sessions.
	}

	@Override
	public String toString() {
		return "Session [ID=" + ID + ", movieID=" + movieID + ", movieName=" + Movie.getByID(movieID).getName() + ", auditoriumID=" + auditoriumID + ", date="
				+ date +", startTime="
				+ startTime + ", duration=" + duration + "]";
	}
	
	
	
	
	
}
