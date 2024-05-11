package cinema;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName ("movie")
public class Movie {
	public static final int RATING_UPPER_LIMIT = 5; //inclusive
	public static final int RATING_LOWER_LIMIT = 1; //inclusive
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("name")	
	private String name;

	@ColumnName("duration")
	private int duration;	//minutes

	@ColumnName("release_date")
	private LocalDate releaseDate;	//inclusive

	@ColumnName("last_screening_date")
	private LocalDate lastScreeningDate; //inclusive

	@ColumnName("rating")
	private float rating;

	@ColumnName("rating_count")
	private int ratingCount;

	// Constructor

	// Private constructor to prevent instantiation
	// of the class from outside without necessary arguments
	// This method is used to create a new movie object
	// by database manager
	private Movie () {}

	public Movie(String name, int duration, LocalDate release,
					LocalDate lastScreeningDate) {

		this.name = name;
		this.duration = duration;
		this.releaseDate = release;
		this.lastScreeningDate = lastScreeningDate;
		this.rating = 0;
		this.ratingCount = 0;
	}

	// Getters

	public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public LocalDate getLastScreeningDate() {
        return lastScreeningDate;
    }

    public float getRating() {
        return rating;
    }

    public float getRatingCount() {
        return ratingCount;
    }

	
	// Setters
	
	
	public static List<Movie> getAllMovies() {
		try {
			return DatabaseManager.getAllRows(Movie.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
    // Method to add a rating to the movie
    public static boolean addRating(int id, int newRate, int ratingCount) {
		String sqlGet = "SELECT rating, rating_count FROM movie WHERE id = ?";
		String sqlUpdate = "UPDATE movie SET rating = ?, rating_count = ? WHERE id = ?";
		
		if (newRate > RATING_UPPER_LIMIT || newRate < RATING_LOWER_LIMIT) {
			// Invalid rating value, must be between 1 and 5 (inclusive)
            return false;
        }

		Connection conn = null;
		PreparedStatement psGet = null;
		PreparedStatement psUpdate = null;
		try {
			conn = DatabaseManager.getConnection();
			psGet = conn.prepareStatement(sqlGet);
			psUpdate = conn.prepareStatement(sqlUpdate);

			// Start the transaction
			conn.setAutoCommit(false);
			
			// Get the current rating and rating count
			psGet.setInt(1, id);
			ResultSet rs = psGet.executeQuery();

			if (rs.next()) {
				float currentRating = rs.getFloat("rating");
				int currentRatingCount = rs.getInt("rating_count");

				int newRatingCount = currentRatingCount + ratingCount;
				float newRating = currentRating + (newRate - currentRating) / ((float) currentRatingCount / ratingCount);

				// Update the rating and rating count
				psUpdate.setFloat(1, newRating);
				psUpdate.setInt(2, newRatingCount);
				psUpdate.setInt(3, id);
				psUpdate.executeUpdate();
			} else {
				throw new SQLException("Movie not found");
			}

			// Commit the transaction
			conn.commit();
			return true;
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException ex) {
					System.err.println("Error rolling back transaction");
					ex.printStackTrace();
				}
			}
			return false;
		} finally {
			if (psGet != null) {
				try {
					psGet.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (psUpdate != null) {
				try {
					psUpdate.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
				} catch (SQLException e) {
					System.err.println("Error setting auto commit to true");
					e.printStackTrace();
				}
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("Error closing connection");
					e.printStackTrace();
				}
			}
		}
    }

	// Method to check if a movie is currently showing
	public boolean isCurrentlyShowing() {
		return LocalDate.now().isBefore(lastScreeningDate) && LocalDate.now().isAfter(releaseDate);
	}

	@Override
    public String toString() {
        // Method to convert the movie object to a string representation
        return "Movie ID: " + id + "\n" +
				"Name: " + name + "\n" +
				"Duration: " + duration + " minutes\n" +
				"Release Date: " + releaseDate + "\n" +
				"Last Screening Date: " + lastScreeningDate + "\n" +
				"Rating: " + rating + " (" + ratingCount + " ratings)\n";
    }
}	
