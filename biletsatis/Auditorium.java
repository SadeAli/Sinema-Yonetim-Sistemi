package biletsatis;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.ArrayList;

public class Auditorium {
	
	private class SessionEntry {
		private LinkedList<Integer> sessionIDs;
		private int movieID;
		private LocalDate date;
		
		public SessionEntry(int movieID, LocalDate date) {
			this.movieID = movieID;
			sessionIDs = new LinkedList<Integer>();
			this.date = LocalDate.from(date);
		}

		public int getMovieID() {
			return movieID;
		}

		public LocalDate getDate() {
			return date;
		}

		public LinkedList<Integer> getSessionIDs() {
			return sessionIDs;
		}
	}
	
	private int ID;
	private static int lastID = 0;
	private LinkedList<SessionEntry> sessionEntries;
	private static final LocalTime openingTime = LocalTime.of(9, 0);
	private static final LocalTime closingTime = LocalTime.of(23, 59);
	private static final int verticalSeatNumber = 8;
	private static final int horizontalSeatNumber = 10;
	private static ArrayList<Auditorium> auditoriums = new ArrayList<>();
	
	public Auditorium() {
		lastID++;
		ID = lastID;
		sessionEntries = new LinkedList<SessionEntry>();
		auditoriums.add(this);
	}

	public static Auditorium getByID(int ID) {
		for (Auditorium aud : auditoriums) {
			if (aud.ID == ID) {
				return aud;
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
		
		LocalDateTime closingDateTime = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(), closingTime.getHour(), closingTime.getMinute());;
		LocalDateTime time = LocalDateTime.of(myDate.getYear(), myDate.getMonth(), myDate.getDayOfMonth(), openingTime.getHour(), openingTime.getMinute());
		
		while (time.plusMinutes(dur).isBefore(closingDateTime)) {
			Session ses = new Session(movieID, this.ID, date, LocalTime.from(time));
			sesEn.sessionIDs.add(ses.getID());
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
		
		for (int sesid : sesEn.sessionIDs) {
			Session ses = Session.getByID(sesid);
			System.out.println(ses);
		}
	}

	@Override
	public String toString() {
		return "Auditorium [ID=" + ID + ", sessionsEntries=" + sessionEntries + "]";
	}
	
	
	
}
