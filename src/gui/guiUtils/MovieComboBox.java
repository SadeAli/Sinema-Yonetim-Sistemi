package gui.guiUtils;

import java.awt.Component;
import java.awt.Font;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

import cinema.Movie;

/**
 * A custom JComboBox that displays Movie objects with a specific format in the
 * dropdown list.
 */
public class MovieComboBox extends JComboBox<Movie> {
    /**
     * Constructs a new MovieComboBox.
     */
    public MovieComboBox() {
        // Set the renderer for the dropdown list
        setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Movie) {
                    Movie movie = (Movie) value;

                    // Format the text to display in the dropdown list
                    String text = String.format("%-70s%-30s%-6s", movie.getName(),
                            "rating: " + movie.getRating(), "duration: " + movie.getDuration());
                    setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

                    setText(text);
                }
                return this;
            }
        });
    }
}
