package cinema;

import database.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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

    public int getRatingCount() {
        return ratingCount;
    }

	
	// Setters
	public void setName(String name) {
		this.name = name;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setReleaseDate(LocalDate releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setLastScreeningDate(LocalDate lastScreeningDate) {
		this.lastScreeningDate = lastScreeningDate;
	}

	public void setRating(float rating) {
		this.rating = rating;
	}

	public void setRatingCount(int ratingCount) {
		this.ratingCount = ratingCount;
	}
	
	
	public static List<Movie> getAllMovies() {
		try {
			return DatabaseManager.getAllRows(Movie.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
	
    // Method to add a rating to the movie
    public static boolean addRating(int ticketCode, float newRate) {
		String sqlGetMovie = "SELECT movie.id, movie.rating, movie.rating_count"
			+ " FROM movie"
			+ " JOIN session ON movie.id = session.movie_id"
			+ " JOIN seat_availability ON session.id = seat_availability.session_id"
			+ " JOIN ticket ON seat_availability.ticket_id = ticket.id"
			+ " WHERE ticket.code = ?"
			+ " AND ticket.is_paid = 1"
			+ " AND ticket.is_rated = 0"
			+ " LIMIT 1";
		
		String sqlGetSeatCount = "SELECT COUNT(*)"
			+ " FROM seat_availability"
			+ " JOIN ticket ON seat_availability.ticket_id = ticket.id"
			+ " WHERE ticket.code = ?";

		String sqlUpdateMovie = "UPDATE movie SET rating = ?, rating_count = ? WHERE id = ?";
		String sqlUpdateTicket = "UPDATE ticket SET is_rated = 1 WHERE code = ?";

		int seatCount = 0;
		
		// take difference
		if (newRate > RATING_UPPER_LIMIT || newRate < RATING_LOWER_LIMIT) {
			// Invalid rating value, must be between 1 and 5 (inclusive)
			System.err.println("Invalid rating value, must be between 1 and 5 (inclusive)");
            return false;
        }

		Connection conn = null;
		PreparedStatement psGetMovie = null;
		PreparedStatement psGetSeatCount = null;
		PreparedStatement psUpdateMovie = null;
		PreparedStatement psUpdateTicket = null;
		ResultSet rs = null;
		try {
			conn = DatabaseManager.getConnection();
			psGetMovie = conn.prepareStatement(sqlGetMovie);
			psGetSeatCount = conn.prepareStatement(sqlGetSeatCount);
			psUpdateMovie = conn.prepareStatement(sqlUpdateMovie);
			psUpdateTicket = conn.prepareStatement(sqlUpdateTicket);

			// Start the transaction
			conn.setAutoCommit(false);

			// Get seat count
			psGetSeatCount.setInt(1, ticketCode);
			rs = psGetSeatCount.executeQuery();
			
			if (rs.next()) {
				seatCount = rs.getInt(1);
				if (seatCount == 0) {
					throw new SQLException("No seats found for ticket code " + ticketCode);
				}
			} else {
				throw new SQLException("Movie not found");
			}

			// Get the current rating and rating count
			psGetMovie.setInt(1, ticketCode);
			rs = psGetMovie.executeQuery();

			// Update the rating and rating count
			if (rs.next()) {
				int movieId = rs.getInt("id");
				float currentRating = rs.getFloat("rating");
				int currentRatingCount = rs.getInt("rating_count");

				int newRatingCount = currentRatingCount + seatCount;
				float newRating = currentRating + (newRate - currentRating) / ((float) newRatingCount / seatCount);

				psUpdateMovie.setFloat(1, newRating);
				psUpdateMovie.setInt(2, newRatingCount);
				psUpdateMovie.setInt(3, movieId);
				psUpdateMovie.executeUpdate();
			} else {
				throw new SQLException("Movie not found");
			}

			// Update the ticket to show that it has been rated
			psUpdateTicket.setInt(1, ticketCode);
			psUpdateTicket.executeUpdate();

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
			System.err.println("Error adding rating: " + e.getMessage());
			return false;
		} finally {
			if (psGetMovie != null) {
				try {
					psGetMovie.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (psGetSeatCount != null) {
				try {
					psGetSeatCount.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (psUpdateTicket != null) {
				try {
					psUpdateTicket.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (psUpdateMovie != null) {
				try {
					psUpdateMovie.close();
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

	public static int getSales(int movieId, LocalDate date) {
		String sql = "SELECT COUNT(*)"
			+ " FROM seat_availability"
			+ " JOIN ticket ON seat_availability.ticket_id = ticket.id"
			+ " JOIN session ON seat_availability.session_id = session.id"
			+ " JOIN movie ON session.movie_id = movie.id"
			+ " WHERE movie.id = ? AND session.date = ? AND ticket.is_paid = 1";

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(sql);

			// Set the parameters
			ps.setInt(1, movieId);
			ps.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			ps.execute();

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("Error closing connection");
					e.printStackTrace();
				}
			}
		}
	}

	public static int getSales(int movieId) {
		String sql = "SELECT COUNT(*)"
			+ " FROM seat_availability"
			+ " JOIN ticket ON seat_availability.ticket_id = ticket.id"
			+ " JOIN session ON seat_availability.session_id = session.id"
			+ " JOIN movie ON session.movie_id = movie.id"
			+ " WHERE movie.id = ? AND ticket.is_paid = 1";

		Connection conn = null;
		PreparedStatement ps = null;
		try {
			conn = DatabaseManager.getConnection();
			ps = conn.prepareStatement(sql);

			// Set the parameters
			ps.setInt(1, movieId);
			ps.execute();

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					System.err.println("Error closing prepared statement");
					e.printStackTrace();
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					System.err.println("Error closing connection");
					e.printStackTrace();
				}
			}
		}
	
	}

	public static int getAssignedSeatCount(int movieId, LocalDate date, Connection conn){
		String sql = "SELECT COUNT(*)"
		+ " FROM seat_availability"
		+ " JOIN session ON seat_availability.session_id = session.id"
		+ " JOIN movie ON session.movie_id = movie.id"
		+ " WHERE movie.id = ? AND session.date = ?";

		PreparedStatement ps = null;

		if (conn == null || date == null || movieId <= 0) {
			throw new IllegalArgumentException("Invalid arguments");
		}

		try {
			ps = conn.prepareStatement(sql);

			// Set the parameters
			ps.setInt(1, movieId);
			ps.setString(2, date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
			ps.execute();

			ResultSet rs = ps.executeQuery();

			if (rs.next()) {
				return rs.getInt(1);
			} else {
				return 0;
			}
			
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			List<Statement> sList = new ArrayList<>();
			sList.add(ps);
			DatabaseManager.closeStatements(sList);
		}

	}

	public static int getAssignedSeatCount (int movieId, LocalDate date) {

		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			return getAssignedSeatCount(movieId, date, conn);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		} finally {
			DatabaseManager.closeConnection(conn);
		}
	}

	public int getAssignedSeatCount(LocalDate date) {
		return getAssignedSeatCount(this.id, date);
	}

	public static List<Integer> getAssignedSeatCountListForLast30Days (int movieId) {
		List<Integer> assignedSeatCountList = new ArrayList<>();

		LocalDate currentDate = LocalDate.now();
		for (int i = 29; i >= 0; i--) {
			assignedSeatCountList.add(getAssignedSeatCount(movieId, currentDate.minusDays(i)));
		}

		return assignedSeatCountList;
	}

	public List<Integer> getAssignedSeatCountListForLast30Days() {
		return getAssignedSeatCountListForLast30Days(this.id);
	}

	public static boolean deleteFromDatabase(int id, Connection conn) {
		try (PreparedStatement pstmt = conn.prepareStatement(
			DatabaseAnnotationUtils.getDeleteQuery(Movie.class))) {

			// Delete the movieGenres from the database
			if (!MovieGenre.deleteFromDatabaseWithMovieId(id, conn)) {
				throw new SQLException("Unable to delete movie genres");
			}

			// Set the parameters
			pstmt.setInt(1, id);
			pstmt.executeUpdate();

			return true;
		} catch (SQLException e) {
			System.err.println("Unable to delete movie: " + e.getMessage());
			return false;
		}
	}

	public static boolean deleteFromDatabase(int id) {
		Connection conn = null;
		try {
			conn = DatabaseManager.getConnection();
			DatabaseManager.setAutoCommit(conn, false);
			
			if (!deleteFromDatabase(id, conn)) {
				throw new SQLException("Unable to delete movie");
			}

			if (!DatabaseManager.commit(conn)) {
				throw new SQLException("Unable to commit transaction");
			}

			return true;
		} catch (Exception e) {
			DatabaseManager.rollback(conn);
			System.err.println("Unable to delete movie: " + e.getMessage());
			return false;
		} finally {
			DatabaseManager.setAutoCommit(conn, true);
			DatabaseManager.closeConnection(conn);
		}
	}

	public static boolean deleteFromDatabase(Movie movie) {
		return deleteFromDatabase(movie.getId());
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
