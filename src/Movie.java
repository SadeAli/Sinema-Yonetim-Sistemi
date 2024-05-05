import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.List;

public class Movie {
	public static final String TABLE_NAME = "movie";
	public static final int RATING_UPPER_LIMIT = 5; //inclusive
	public static final int RATING_LOWER_LIMIT = 1; //inclusive
	
	private int id;
	private String name;
	private int duration;	//minutes
	private LocalDate release;	//inclusive
	private LocalDate lastScreeningDate; //inclusive
	private float rating;
	private int ratingCount;

	// Constructor
	public Movie(String name, int duration, LocalDate release,
					LocalDate lastScreeningDate) {
		//TODO Check if the movie name is unique
		//TODO Check if the duration is a positive integer
		//TODO Check if the release date is before the last show day
		//TODO Check if the last show day is after the current date (if necessary, based on the project requirements)
		//TODO Check if arguemnts are not null
		//TODO Check if the movie name is valid (such as not containing special characters, not too long, etc.)

		//TODO Add to the database

		this.name = name;
		this.duration = duration;
		this.release = release;
		this.lastScreeningDate = lastScreeningDate;
		this.rating = 0;
		this.ratingCount = 0;
	}

	// Constructor for database
	private Movie(int id, String name, int duration, LocalDate release, 
				LocalDate lastScreeningDate, float rating, int ratingCount) {
		this.id = id;
		this.name = name;
		this.duration = duration;
		this.release = release;
		this.lastScreeningDate = lastScreeningDate;
		this.rating = rating;
		this.ratingCount = ratingCount;
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

    public LocalDate getRelease() {
        return release;
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

	public List<Movie> getAllMovies() {
		return DatabaseManager.getAllRows(TABLE_NAME, getResultSetParser());
	}

	// Setters


	
    // Method to add a rating to the movie
    public boolean addRating(int newRate, int ratingCount) {
        //TODO Update rating and ticket.is_rated in the database
		
		if (newRate > RATING_UPPER_LIMIT || newRate < RATING_LOWER_LIMIT) {
			// Invalid rating value, must be between 0 and 5 (inclusive)
            return false;
        }

        this.ratingCount += ratingCount;
        
        // To prevent overflow
        rating = rating + (newRate - rating) / ((float) this.ratingCount / ratingCount);
        return true;
    }

	// Method to check if a movie is currently showing
	public boolean isCurrentlyShowing() {
		return LocalDate.now().isBefore(lastScreeningDate);
	}
	
	// Returns method for parsing a ResultSet and constructing a movie object
	public static ResultSetParser<Movie> getResultSetParser() {
		return resultSet -> {
			int id = resultSet.getInt(1);
			String name = resultSet.getString(2);
			int duration = resultSet.getInt(3);
			LocalDate release = LocalDate.parse(
				resultSet.getString(4),
				DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			LocalDate lastShowDay = LocalDate.parse(
				resultSet.getString(5),
				DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			float rating = resultSet.getFloat(6);
			int rateNumber = resultSet.getInt(7);
			return new Movie(id, name, duration, release, lastShowDay, rating, rateNumber);
		};
	}

	//TODO Method to check if a movie with the given id exists
    //TODO Method to get a movie by its id

	@Override
    public String toString() {
        // Method to convert the movie object to a string representation
        return "Movie ID: " + id + "\n" +
				"Name: " + name + "\n" +
				"Duration: " + duration + " minutes\n" +
				"Release Date: " + release + "\n" +
				"Last Screening Date: " + lastScreeningDate + "\n" +
				"Rating: " + rating + " (" + ratingCount + " ratings)\n";
    }
}	
