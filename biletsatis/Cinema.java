// javac -d bin *.java; java -cp bin ali_prototip.Cinema

import java.time.LocalDate;

// Main class for the project
// includes UI and main logic
public class Cinema {
    public static void main(String[] args) {
        MovieSelectionWindow_v2 window = new MovieSelectionWindow_v2(1000, 800, 5);

        for (int i = 10; i > 0; i--) {
            Movie movie = new Movie("Movie " + i, 120, LocalDate.now().minusDays(i), LocalDate.now().plusDays(30));
            movie.addRating(i / 2);
            window.addMovie(movie);
        }
        
        // window.removeMovie("Movie 2");
        // window.removeMovie("Movie 3");
    }
}
