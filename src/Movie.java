import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dbanno.*;

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
		//TODO Check if the movie name is unique
		//TODO Check if the duration is a positive integer
		//TODO Check if the release date is before the last show day
		//TODO Check if the last show day is after the current date (if necessary, based on the project requirements)
		//TODO Check if arguemnts are not null
		//TODO Check if the movie name is valid (such as not containing special characters, not too long, etc.)

		//TODO Add to the database

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
		return LocalDate.now().isBefore(lastScreeningDate) && LocalDate.now().isAfter(releaseDate);
	}

	//TODO Method to check if a movie with the given id exists
    //TODO Method to get a movie by its id

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
