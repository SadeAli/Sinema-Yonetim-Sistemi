package gui.mainPanels;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import cinema.Movie;
import gui.CinemaGUI;

import javax.swing.JButton;
import javax.swing.JOptionPane;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Color;
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
    // rating of the movie
    private int rating;

    /**
     * Constructor for the movie rating window.
     * 
     * @param parent The parent window.
     * @param width  The width of the window.
     * @param height The height of the window.
     */
    public MovieRatingPanel(CinemaGUI parent, int width, int height) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(250, 100, 250, 100));

        // a text field for displaying warnings
        ticketNumberField = new JTextField();

        // set the initial rating to 0
        rating = 0;

        // a text field for entering the ticket number
        ticketNumberField.setText("Enter the ticket number");
        ticketNumberField.setBounds(width / 2 - 100, height / 2 - 200, 200, 30);
        ticketNumberField.setPreferredSize(ticketNumberField.getSize());

        // 5 buttons for rating the movie
        // create a horizontal box for rating buttons
        Box ratingBox = Box.createHorizontalBox();

        ratingButtons = new JButton[5];
        for (int i = 0; i < 5; i++) {
            ratingButtons[i] = new JButton(Integer.toString(i + 1));
            ratingBox.add(ratingButtons[i]);
        }

        // a panel for the submit and back buttons
        JPanel buttonPanel = new JPanel();
        submitButton = new JButton("Submit");
        backButton = new JButton("Back");

        // place the buttons in the panel
        buttonPanel.setLayout(new BorderLayout());
        buttonPanel.add(submitButton, BorderLayout.EAST);
        buttonPanel.add(backButton, BorderLayout.WEST);

        add(ticketNumberField);
        add(ratingBox);
        add(buttonPanel);

        // add action and focus listeners

        // add focus listener to the ticket number field
        ticketNumberField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (ticketNumberField.getText().equals("Enter the ticket number")) {
                    ticketNumberField.setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (ticketNumberField.getText().equals("")) {
                    ticketNumberField.setText("Enter the ticket number");
                }
            }
        });

        // add action listeners to the rating buttons
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

        // add action listeners to the submit and back buttons
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // check if a rating is selected
                if (rating == 0) {
                    JOptionPane.showMessageDialog(parent, "Please select a rating!");
                    return;
                }

                // get the ticket number from the text field
                String ticketNumber = ticketNumberField.getText();
                try {
                    int ticketCode = Integer.parseInt(ticketNumber);
                    if (Movie.addRating(ticketCode, rating)) {
                        JOptionPane.showMessageDialog(parent, "Rating submitted successfully!");
                        parent.showMainMenu();
                    } else {
                        JOptionPane.showMessageDialog(parent, "Invalid ticket or ticket is already used to rate a movie!");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(parent, "Invalid ticket number!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(parent, "An error occurred while submitting the rating!");
                    ex.printStackTrace();
                }

            }
        });

        // add action listener to the back button
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // go back to the main menu
                parent.showMainMenu();
            }
        });
    }
}
