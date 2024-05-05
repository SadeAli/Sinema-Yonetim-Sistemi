// Movie class

import java.time.LocalDate;
import java.util.LinkedList;

//TODO change ID with id in all classes

public class Movie {
    private int ID;
	private static int lastID = 0;
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
		
		lastID++;
		ID = lastID;
		rating = 0;
		rateCount = 0;
		movies.add(this);
    }

    // Getters

    public int getID() {
        return ID;
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
	public static boolean exists(int ID) {
		for (Movie movie : movies) {
			if (movie.ID == ID) {
				return true;
			}
		}
		return false;
	}

    // Method to get a movie by its ID
    public static Movie getByID(int ID) {
        for (Movie movie : movies) {
            if (movie.ID == ID) {
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

    @Override
    public String toString() {
        // Method to convert the movie object to a string representation
        return "Movie [ID=" + ID + ", name=" + name + ", duration=" + duration + ", release=" + release
                + ", lastShowDay=" + lastShowDay + ", rating=" + rating + ", rateNumber=" + rateCount + "]";
    }

    // getter for movies
    public static LinkedList<Movie> getMovies() {
        return movies;
    }
}    

