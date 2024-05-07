import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.crypto.Data;

import dbanno.*;

@TableName("screening_room")
public class ScreeningRoom {
	private static LocalTime openingTime = LocalTime.of(9, 0);
	private static LocalTime closingTime = LocalTime.of(23, 59);
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("seat_row_count")
	private int seatRowCount;

	@ColumnName("seat_col_count")
	private int seatColCount;

	private ScreeningRoom() {}

	public ScreeningRoom(int seatRowCount, int seatColCount) {
		//TODO Add to the database
		this.seatRowCount = seatRowCount;
		this.seatColCount = seatColCount;
	}

	public static LocalTime getOpeningTime() {
		return openingTime;
	}

	public static LocalTime getClosingTime() {
		return closingTime;
	}

	// Getters

	public int getId() {
		return id;
	}

	public int getSeatRowCount() {
		return seatRowCount;
	}

	public int getSeatColCount() {
		return seatColCount;
	}

	// Setters


	public static List<ScreeningRoom> getAllScreeningRooms() {
		try {
			return DatabaseManager.getAllRows(ScreeningRoom.class);
		} catch (Exception e) {
			System.err.println("Unable to get screening rooms: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	public static boolean setActiveHours(LocalTime openingTime, 
			LocalTime closingTime) {
		if (openingTime.isAfter(closingTime)) {
			return false;
		}
		ScreeningRoom.openingTime = openingTime;
		ScreeningRoom.closingTime = closingTime;
		return true;
	}

	public boolean addMovieToDate(LocalDate date, int movieId) {
		List<Session> sessionList = new ArrayList<>();

		List<FilterCondition> filters = new ArrayList<>();
		filters.add(new FilterCondition("date", date, 
				FilterCondition.Relation.EQUALS));
		try {
			if (!DatabaseManager.exists(Session.class, filters)) {
				LocalDateTime closingDateTime = LocalDateTime.of(date, 
						ScreeningRoom.closingTime);
				LocalDateTime time = LocalDateTime.of(date, 
						ScreeningRoom.openingTime);
				int extendedDuration = Session.calculateExtendedDuration(DatabaseManager.getRowById(Movie.class, movieId).getDuration());
				while (time.plusMinutes(extendedDuration).isBefore(closingDateTime)) {
					Session session = new Session(movieId, this.id, date, 
							LocalTime.from(time.toLocalTime()), extendedDuration);
					DatabaseManager.insertRow(session);
					//TODO make this with a transaction
					time = time.plusMinutes(extendedDuration);
				}
			}
		} catch (Exception e) {
			System.err.println("Unable to add movie to date: " + e.getMessage());
			e.printStackTrace();
		}

		return false;
	}

}
