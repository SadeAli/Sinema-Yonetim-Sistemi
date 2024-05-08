package cinema;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName("genre")
public class Genre {
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("name")
	private String name;

	// Constructor
	private Genre() {}

	public Genre(String name) {
		this.name = name;
	}

	// Getters

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static List<Genre> getAllGenres() {
		try {
			return DatabaseManager.getAllRows(Genre.class);
		} catch (Exception e) {
			System.err.println("Unable to get genres: " + e.getMessage());
			return new ArrayList<>();
		}
	}
}
