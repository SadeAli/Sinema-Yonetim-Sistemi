import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import java.awt.Dimension;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.BorderLayout;

public class MovieRatingPanel extends JPanel {
    // text field for entering the ticket number
    private JTextField ticketNumberField;
    // 5 buttons for rating the movie
    private JButton[] ratingButtons;
    // button for submitting the rating
    private JButton submitButton;
    // a button for going back to the main menu
    private JButton backButton;
    // the movie to rate
    private Movie movie;
    // the parent window
    private CinemaGUI parent;
    // the width of the window
    private int width;
    // the height of the window
    private int height;
    // rating of the movie
    private int rating;

    /**
     * Constructor for the movie rating window.
     * 
     * @param parent The parent window.
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    MovieRatingPanel(CinemaGUI parent, int width, int height) {
        this.parent = parent;
        this.width = width;
        this.height = height;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // a text field for displaying warnings
        JTextField warningField = new JTextField();
        warningField.setBounds(width / 2 - 100, height / 2 - 250, 200, 30);
        warningField.setPreferredSize(warningField.getSize());
        add(warningField);

        // a text field for entering the ticket number
        ticketNumberField = new JTextField();
        ticketNumberField.setBounds(width / 2 - 100, height / 2 - 200, 200, 30);
        ticketNumberField.setPreferredSize(ticketNumberField.getSize());
        add(ticketNumberField);

        // 5 buttons for rating the movie
        // create a horizontal box for rating buttons
        Box ratingBox = Box.createHorizontalBox();

        ratingButtons = new JButton[5];
        for (int i = 0; i < 5; i++) {
            ratingButtons[i] = new JButton(Integer.toString(i + 1));
            ratingButtons[i].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    for (int j = 0; j < 5; j++) {
                        if (e.getSource() == ratingButtons[j]) {
                            for (int k = 0; k <= j; k++) {
                                ratingButtons[k].setText("★");
                                ratingButtons[k].setBackground(Color.GREEN); // Change button color to green
                            }
                            for (int k = j + 1; k < 5; k++) {
                                ratingButtons[k].setText(Integer.toString(k + 1));
                                ratingButtons[k].setBackground(null); // Reset button color
                            }
                            rating = j + 1; // Save the rating
                        }
                        }
                }
            });
            ratingBox.add(ratingButtons[i]);
        }

        add(ratingBox);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BorderLayout());

        // a button for submitting the rating
        submitButton = new JButton("Submit");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (rating == 0) {
                    warningField.setText("Please select a rating!");
                    return;
                }
                // submit the rating
                System.out.println("Rating submitted: " + rating + " stars");
                // go back to the main menu
                parent.updateState("Ana Menü");
            }
        });
        buttonPanel.add(submitButton, BorderLayout.EAST);

        // a button for going back to the main menu
        backButton = new JButton("Back");
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go back to the main menu
                parent.updateState("Ana Menü");
            }
        });
        buttonPanel.add(backButton, BorderLayout.WEST);

        add(buttonPanel);
    }
}
