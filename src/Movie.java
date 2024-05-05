// Movie class

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;

//TODO change ID with id in all classes

public class Movie{
    private int id;
	private String name;
	private int duration;	//minutes
	private LocalDate release;	
	private LocalDate lastShowDay; //inclusive //(a fixed duration may be used instead of date) public static final timeOut = 60; //days
	private float rating;
	private static final int rateUpperLimit = 5; //inclusive
	private static final int rateLowerLimit = 1; //inclusive
	private int rateCount;
	private static LinkedList<Movie> movies = new LinkedList<>();


    // Constructor
    public Movie(String name, int duration, LocalDate release, LocalDate lastShowDay) {
		//TODO Check if the movie name is unique
		//TODO Check if the duration is a positive integer
		//TODO Check if the release date is before the last show day
		//TODO Check if the last show day is after the current date (if necessary, based on the project requirements)
		//TODO Check if arguemnts are not null
		//TODO Check if the movie name is valid (such as not containing special characters, not too long, etc.)

        this.name = name;
		this.duration = duration;
		this.release = release;
		this.lastShowDay = lastShowDay;
		
		rating = 0;
		rateCount = 0;
		movies.add(this);
    }

	// Constructor for database
	private Movie(int id, String name, int duration, LocalDate release, LocalDate lastShowDay, float rating, int rateNumber) {
		//TODO Check if arguemnts are not null
		//TODO Check if the movie name is unique
		//TODO Check if the movie name is valid (such as not containing special characters, not too long, etc.)
		//TODO Check if the duration is a positive integer
		//TODO Check if the release date is before the last show day
		//TODO Check if the last show day is after the current date (if necessary, based on the project requirements)
		this.id = id;
		this.name = name;
		this.duration = duration;
		this.release = release;
		this.lastShowDay = lastShowDay;
		this.rating = rating;
		this.rateNumber = rateNumber;
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

    public LocalDate getLastShowDay() {
        return lastShowDay;
    }

    public float getRating() {
        return rating;
    }

    // Method to add a rating to the movie
    public boolean addRating(int newRate, int ratingCount) {
        if (newRate > rateUpperLimit || newRate < rateLowerLimit) {
			// Invalid rating value, must be between 0 and 5 (inclusive)
            return false;
        }

        rateCount += ratingCount;
        
        // To prevent overflow
        rating = rating + (newRate - rating) / this.rateCount;
        return true;
    }

	// Method to check if a movie with the given ID exists
	public static boolean exists(int id) {
		for (Movie movie : movies) {
			if (movie.id == id) {
				return true;
			}
		}
		return false;
	}

    // Method to get a movie by its ID
    public static Movie getById(int id) {
        for (Movie movie : movies) {
            if (movie.id == id) {
                return movie;
            }
        }
        return null;
    }

    // Method to check if a movie is currently showing
    public boolean isCurrentlyShowing() {
        LocalDate currentDate = LocalDate.now();
        return currentDate.isBefore(lastShowDay);
    }

    // Method to display all the movies
    public static void displayAllMovies() {
        for (Movie movie : movies) {
            System.out.println(movie);
        }
    }

	// Returns method for parsing a ResultSet and constructing a movie object
	public static ResultSetParser<Movie> getResultSetParser() throws SQLException {
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

    @Override
    public String toString() {
        // Method to convert the movie object to a string representation
        return "Movie [id=" + id + ", name=" + name + ", duration=" + duration + ", release=" + release
                + ", lastShowDay=" + lastShowDay + ", rating=" + rating + ", rateNumber=" + rateCount + "]";
    }

    // getter for movies
    public static LinkedList<Movie> getMovies() {
        return movies;
    }
}    

