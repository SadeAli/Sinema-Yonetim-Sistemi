import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
		//TODO Complete this method
		//TODO Add to the database
		return false;
	}

}
