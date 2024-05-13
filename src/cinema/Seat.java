package cinema;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName("seat")
public class Seat {
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ForeignKey(referencedClass = ScreeningRoom.class)
	@ColumnName("screening_room_id")
	private int screeningRoomId;

	@ColumnName("row")
	private int row;

	@ColumnName("col")
	private int col;

	// Constructors

	private Seat() {}

	public Seat(int screeningRoomId, int row, int col) {
		this.screeningRoomId = screeningRoomId;
		this.row = row;
		this.col = col;
	}

	// Getters

	public int getId() {
		return id;
	}

	public int getScreeningRoomId() {
		return screeningRoomId;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	// Setters


	public static List<Seat> getAllSeats() {
		try {
			return DatabaseManager.getAllRows(Seat.class);
		} catch (Exception e) {
			System.err.println("Unable to get seats: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	public String toString() {
		return "Seat " + id + " at row " + row + " and column " + col + " in screening room " + screeningRoomId;
	}
}
