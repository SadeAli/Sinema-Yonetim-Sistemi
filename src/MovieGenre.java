import java.util.ArrayList;
import java.util.List;

import dbanno.*;

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

}
