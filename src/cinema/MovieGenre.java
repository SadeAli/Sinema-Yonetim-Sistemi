package cinema;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import database.*;
import java.sql.PreparedStatement;

@TableName("movie_genre")
public class MovieGenre {
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ForeignKey(referencedClass = Movie.class)
	@ColumnName("movie_id")
	private int movieId;

	@ForeignKey(referencedClass = Genre.class)
	@ColumnName("genre_id")
	private int genreId;

	// Constructor
	private MovieGenre() {}

	public MovieGenre(int movieId, int genreId) {
		this.movieId = movieId;
		this.genreId = genreId;
	}

	// Getters
	public int getId() {
		return id;
	}

	public int getMovieId() {
		return movieId;
	}

	public int getGenreId() {
		return genreId;
	}

	public static List<MovieGenre> getAllMovieGenres() {
		try {
			return DatabaseManager.getAllRows(MovieGenre.class);
		} catch (Exception e) {
			System.err.println("Unable to get movie genres: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	/**
	 * Deletes movie genre records from the database with the specified movie ID.
	 * 
	 * @param movieId the ID of the movie to delete genre records for
	 * @param conn the database connection
	 * @return true if the movie genre records were successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabaseWithMovieId(int movieId, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(
				"DELETE FROM movie_genre WHERE movie_id = ?")) {
			pstmt.setInt(1, movieId);
			pstmt.executeUpdate();
			return true;
		} catch (Exception e) {
			System.err.println("Unable to delete movie genre: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes movie genre records from the database with the specified movie ID.
	 * 
	 * @param movieId the ID of the movie to delete genre records for
	 * @return true if the movie genre records were successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabaseWithMovieId(int movieId) {
		try (Connection conn = DatabaseManager.getConnection()) {
			return deleteFromDatabase(movieId, conn);
		} catch (Exception e) {
			System.err.println("Unable to delete movie genre: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes movie genre records from the database with the specified genre ID.
	 * 
	 * @param genreId the ID of the genre to delete movie records for
	 * @param conn the database connection
	 * @return true if the movie genre records were successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabase(int id, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(
				DatabaseAnnotationUtils.getDeleteQuery(MovieGenre.class))) {
			pstmt.setInt(1, id);
			pstmt.executeUpdate();
			return true;
		} catch (Exception e) {
			System.err.println("Unable to delete movie genre: " + e.getMessage());
			return false;
		}

	}

	/**
	 * Deletes movie genre records from the database with the specified genre ID.
	 * 
	 * @param genreId the ID of the genre to delete movie records for
	 * @return true if the movie genre records were successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabase(int id) {
		try (Connection conn = DatabaseManager.getConnection()) {
			return deleteFromDatabase(id, conn);
		} catch (Exception e) {
			System.err.println("Unable to delete movie genre: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Deletes the specified movie genre record from the database.
	 * 
	 * @param movieGenre the movie genre record to delete
	 * @param conn the database connection
	 * @return true if the movie genre record was successfully deleted, false otherwise
	 */
	public static boolean deleteFromDatabase(MovieGenre movieGenre) {
		return deleteFromDatabase(movieGenre.getId());
	}

}
