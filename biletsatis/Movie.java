package biletsatis;

import java.time.LocalDate;
import java.util.ArrayList;

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
	private static ArrayList<Movie> movies = new ArrayList<>();
	
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
	
	public int getID() {
		return ID;
	}

	public static int getLastID() {
		return lastID;
	}

	public static int getRateupperlimit() {
		return rateUpperLimit;
	}

	public static int getRatelowerlimit() {
		return rateLowerLimit;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public LocalDate getRelease() {
		return release;
	}

	public void setRelease(LocalDate release) {
		this.release = release;
	}

	public LocalDate getLastShowDay() {
		return lastShowDay;
	}

	public void setLastShowDay(LocalDate lastShowDay) {
		this.lastShowDay = lastShowDay;
	}

	public float getRating() {
		return rating;
	}

	public int getRateNumber() {
		return rateNumber;
	}
	
	public boolean addRating(int newRate) {
		if (newRate > rateUpperLimit || newRate < rateLowerLimit) {
			return false;
		}
		rateNumber++;
		rating = rating + (newRate - rating)/rateNumber;
		return true;
	}
	
	public static Movie getByID(int ID) {
		for (Movie movie : movies) {
			if (movie.ID == ID) {
				return movie;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "Movie [ID=" + ID + ", name=" + name + ", duration=" + duration + ", release=" + release
				+ ", lastShowDay=" + lastShowDay + ", rating=" + rating + ", rateNumber=" + rateNumber + "]";
	}
	
	
	
}
