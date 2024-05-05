import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cinema {
    public static void main(String[] args) {

        // create the main window
        CinemaGUI window = new CinemaGUI();

        for (int i = 20; i > 0; i--) {
            Movie movie = new Movie("Movie " + i, 120, LocalDate.now().minusDays(i), LocalDate.now().plusDays(30));
        }
    }
}