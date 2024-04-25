import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ArrayList;

public class ScreeningRoom {
	
	private class SessionEntry {
		private LinkedList<Integer> sessionIDList;
		private int movieID;
		private LocalDate date;
		
		public SessionEntry(int movieID, LocalDate date) {
			this.movieID = movieID;
			sessionIDList = new LinkedList<Integer>();
			this.date = LocalDate.from(date);
		}

		public int getMovieID() {
			return movieID;
		}

		public LocalDate getDate() {
			return date;
		}

		public LinkedList<Integer> getSessionIDList() {
			return sessionIDList;
		}
	}
	
	private int ID;
	private static int lastID = 0;
	private LinkedList<SessionEntry> sessionEntries;
	private static final LocalTime openingTime = LocalTime.of(9, 0);
	private static final LocalTime closingTime = LocalTime.of(23, 59);
	private static final int verticalSeatNumber = 8;
	private static final int horizontalSeatNumber = 10;
	private static ArrayList<ScreeningRoom> screeningRooms = new ArrayList<>();
	
	public ScreeningRoom() {
		lastID++;
		ID = lastID;
		sessionEntries = new LinkedList<SessionEntry>();
		screeningRooms.add(this);
	}

	public static ScreeningRoom getByID(int ID) {
		for (ScreeningRoom room : screeningRooms) {
			if (room.ID == ID) {
				return room;
			}
		}
		return null;
	}
	
	public static LocalTime getOpeningTime() {
		return openingTime;
	}

	public static LocalTime getClosingTime() {
		return closingTime;
	}

	public int getID() {
		return ID;
	}

	public static int getLastID() {
		return lastID;
	}

	public static int getVerticalSeatNumber() {
		return verticalSeatNumber;
	}

	public static int getHorizontalSeatNumber() {
		return horizontalSeatNumber;
	}
	
	public boolean addMovieToDate(LocalDate date, int movieID) {
		
		LocalDate myDate = LocalDate.from(date);
		myDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		for (SessionEntry sesEnIter : sessionEntries) {
			if (sesEnIter.date.isEqual(myDate)) {
				return false;
			}
		}
		
		SessionEntry sesEn = new SessionEntry(movieID, myDate);
		
		int dur = Session.extendedDuration(Movie.getByID(movieID).getDuration());
		
		LocalDateTime closingDateTime = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(), closingTime.getHour(), closingTime.getMinute());
		LocalDateTime time = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(), openingTime.getHour(), openingTime.getMinute());
		
		while (time.plusMinutes(dur).isBefore(closingDateTime)) {
			Session ses = new Session(movieID, this.ID, date, LocalTime.from(time));
			sesEn.sessionIDList.add(ses.getID());
			time = LocalDateTime.from(time.plusMinutes(dur)); 
		}
		if (sessionEntries.isEmpty()) {
			sessionEntries.add(sesEn);
			return true;
		}
		else {
			
			for (SessionEntry sesEnIter : sessionEntries) {
				if (sesEnIter.date.isAfter(myDate)) {
					sessionEntries.add(sessionEntries.indexOf(sesEnIter), sesEn);
					return true;
				}
			}
			sessionEntries.add(sesEn);
			return true;
		}
	}
	
	public void printSessions(LocalDate date) {
		LocalDate myDate = LocalDate.from(date);
		myDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		SessionEntry sesEn = null;
		
		for (SessionEntry sesEnIter : sessionEntries) {
			if (sesEnIter.date.isEqual(myDate)) {
				sesEn = sesEnIter;
			}
		}
		
		if (sesEn == null) {
			return;
		}
		
		for (int sesid : sesEn.sessionIDList) {
			Session ses = Session.getByID(sesid);
			System.out.println(ses);
		}
	}

	@Override
	public String toString() {
		return "ScreeningRoom [ID=" + ID + ", sessionsEntries=" + sessionEntries + "]";
	}
}
