import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.CardLayout;

public class CinemaGUI extends JFrame {
    private JPanel mainPanel = new JPanel();
    private CardLayout m_cardLayout = new CardLayout();
    
    private MainMenuPanel mainMenuPanel;
    private MovieSelectionPanel movieSelectionPanel;
    private MovieRatingPanel movieRatingPanel;

    CinemaGUI() {
        setSize(1000, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);
        
        // card layout for the main window
        mainPanel.setLayout(m_cardLayout);

        // tabbed pane 0 is the main menu panel
        mainMenuPanel = new MainMenuPanel(this, getSize().width, getSize().height);
        mainPanel.add("Ana Menü", mainMenuPanel);
        
        // tabbed pane 1 is the movie selection panel
        movieSelectionPanel = new MovieSelectionPanel(this, getSize().width, getSize().height, 1);
        mainPanel.add("Film Seç", movieSelectionPanel);

        // tabbed pane 2 is the movie rating panel
        movieRatingPanel = new MovieRatingPanel(this, getSize().width, getSize().height);
        mainPanel.add("Film Değerlendir", movieRatingPanel);

        // tabbed pane 3 is the movie review panel
        
        // show the main menu by default
        m_cardLayout.show(mainPanel, "Ana Menü");
        
        // add the main panel to the frame
        add(mainPanel);
        
        setVisible(true);
    }

    void updateState(String state) {
        m_cardLayout.show(mainPanel, state);
    }

    void updateMovieList() {
        movieSelectionPanel.updateMovies();
    }
}
