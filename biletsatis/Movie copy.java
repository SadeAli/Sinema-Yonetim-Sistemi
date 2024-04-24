// Movie class

import java.time.LocalDate;
import java.util.LinkedList;

public class Movie {
    private int ID;
	private static int lastID = 0;
	private String name;
	private int duration;	//minutes
	private LocalDate release;
	private LocalDate lastShowDay; //public static final timeOut = 60; //days
	private float rating;
	private static final int rateUpperLimit = 5;
	private static final int rateLowerLimit = 0;
	private int rateNumber;
	private static LinkedList<Movie> movies = new LinkedList<>();


    // Constructor
    public Movie(String name, int duration, LocalDate release, LocalDate lastShowDay) {
        this.name = name;
		this.duration = duration;
		this.release = release;
		this.lastShowDay = lastShowDay;
		
		lastID++;
		ID = lastID;
		rating = 0;
		rateNumber = 0;
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
    public boolean addRating(int newRate) {
        if (newRate > rateUpperLimit || newRate < rateLowerLimit) {
            return false;
        }
        rateNumber++;
        
        // To prevent overflow
        rating = rating + (newRate - rating) / rateNumber;
        return true;
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
                + ", lastShowDay=" + lastShowDay + ", rating=" + rating + ", rateNumber=" + rateNumber + "]";
    }
}    

