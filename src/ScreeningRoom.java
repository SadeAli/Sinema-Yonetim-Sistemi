import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ArrayList;

//TODO change ID with id in all classes

public class ScreeningRoom {
	
	private class SessionEntry {
		private LinkedList<Integer> sessionIDList;
		private int movieID;
		private LocalDate date;
		
		public SessionEntry(int movieID, LocalDate date) {
			//TODO Handle exceptions
			/*if (!Movie.exists(movieID)) {
				throw new IllegalArgumentException("Movie ID must be a positive integer");
			}
			if (date == null) {
				throw new IllegalArgumentException("Date cannot be null");
			}*/

			this.movieID = movieID;
			sessionIDList = new LinkedList<Integer>();
			this.date = LocalDate.from(date); // Copy the date
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
	private LinkedList<SessionEntry> sessionEntryList;
	private static final LocalTime openingTime = LocalTime.of(9, 0);
	private static final LocalTime closingTime = LocalTime.of(23, 59);
	private static final int verticalSeatNumber = 8;
	private static final int horizontalSeatNumber = 10;
	private static ArrayList<ScreeningRoom> screeningRooms = new ArrayList<>();
	
	public ScreeningRoom() {
		lastID++;
		ID = lastID;
		sessionEntryList = new LinkedList<SessionEntry>();
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

	/* 
	public boolean addMovieToDate(LocalDate date, int movieID) {

		LocalDate myDate = LocalDate.from(date);
		myDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

		for (SessionEntry sesEnIter : sessionEntryList) {
			if (sesEnIter.date.isEqual(myDate)) {
				return false;
			}
		}

		SessionEntry sesEn = new SessionEntry(movieID, myDate);

		int dur = Session.extendedDuration(Movie.getByID(movieID).getDuration());

		LocalDateTime closingDateTime = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(),
				closingTime.getHour(), closingTime.getMinute());
		LocalDateTime time = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(),
				openingTime.getHour(), openingTime.getMinute());

		while (time.plusMinutes(dur).isBefore(closingDateTime)) {
			Session ses = new Session(movieID, this.ID, date, LocalTime.from(time));
			sesEn.sessionIDList.add(ses.getID());
			time = LocalDateTime.from(time.plusMinutes(dur));
		}
		if (sessionEntryList.isEmpty()) {
			sessionEntryList.add(sesEn);
			return true;
		} else {

			for (SessionEntry sesEnIter : sessionEntryList) {
				if (sesEnIter.date.isAfter(myDate)) {
					sessionEntryList.add(sessionEntryList.indexOf(sesEnIter), sesEn);
					return true;
				}
			}
			sessionEntryList.add(sesEn);
			return true;
		}
	}
*/

	public boolean addMovieToDate(final LocalDate date, int movieID) {

		//! this section is not complete

		/*// Not sure if this is necessary
		LocalDate validatedDate = LocalDate.from(date);
		validatedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));*/
		
		// Check if the date is already in the list
		for (SessionEntry sessionEntryIterator : sessionEntryList) {
			if (sessionEntryIterator.getDate().isEqual(date/*validatedDate*/)) {
				// Date already exists exception
				return false;
			}
		}

		// Set opening and closing times for given date
		

		SessionEntry sessionEntry = new SessionEntry(movieID, date /* validatedDate */);

		int extendedDuration = Session.extendedDuration(Movie.getByID(movieID).getDuration());

		return false;
	}
		

	/*

	public void printSessions(LocalDate date) {
		LocalDate myDate = LocalDate.from(date);
		myDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		
		SessionEntry sesEn = null;
		
		for (SessionEntry sesEnIter : sessionEntryList) {
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

	*/

	@Override
	public String toString() {
		return "ScreeningRoom [ID=" + ID + ", sessionsEntries=" + sessionEntryList + "]";
	}
}
