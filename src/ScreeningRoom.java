import java.time.LocalDate;
import java.time.LocalTime;

public class ScreeningRoom {
	private int id;
	private static LocalTime openingTime = LocalTime.of(9, 0);
	private static LocalTime closingTime = LocalTime.of(23, 59);
	private int seatRowCount;
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

	public int getId() {
		return id;
	}

	public int getSeatRowCount() {
		return seatRowCount;
	}

	public int getSeatColCount() {
		return seatColCount;
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
