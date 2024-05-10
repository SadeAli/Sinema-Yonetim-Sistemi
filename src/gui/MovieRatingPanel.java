package gui;

import javax.swing.JPanel;
import javax.swing.JTextField;

import javax.swing.JButton;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import javax.swing.BoxLayout;
import javax.swing.Box;
import java.awt.BorderLayout;

public class MovieRatingPanel extends JPanel {

    private int rating = 0;

    /**
     * Constructor for the movie rating window.
     * 
     * @param parent The parent window.
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    MovieRatingPanel(CinemaGUI parent, int width, int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // main components
        JTextField ticketNumberField = new JTextField();
        JTextField warningField = new JTextField();
        JPanel buttonPanel = new JPanel(new BorderLayout());
        Box ratingBox = Box.createHorizontalBox();

        // a text field for displaying warnings
        warningField.setBounds(width / 2 - 100, height / 2 - 250, 200, 30);
        warningField.setPreferredSize(warningField.getSize());

        // a text field for entering the ticket number
        ticketNumberField.setBounds(width / 2 - 100, height / 2 - 200, 200, 30);
        ticketNumberField.setPreferredSize(ticketNumberField.getSize());

        // 5 buttons for rating the movie
        JButton[] ratingButtons = new JButton[5];
        for (int i = 0; i < 5; i++) {
            ratingButtons[i] = new JButton(Integer.toString(i + 1));
            ratingBox.add(ratingButtons[i]);
        }

        // sub panel components
        JButton submitButton = new JButton("Submit");
        JButton backButton = new JButton("Back");

        // fill sub panels
        buttonPanel.add(submitButton, BorderLayout.EAST);
        buttonPanel.add(backButton, BorderLayout.WEST);

        // add the fields
        add(warningField);
        add(ticketNumberField);
        add(ratingBox);
        add(buttonPanel);

        // actions
        for (int i = 0; i < 5; i++) {
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
        }

        backButton.addActionListener(e -> parent.showMainMenu());

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
                parent.showMainMenu();
            }
        });
    }
}
