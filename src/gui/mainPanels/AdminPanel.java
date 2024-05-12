package gui.mainPanels;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import cinema.ScreeningRoom;
import gui.CinemaGUI;
import gui.mainPanels.adminPanels.MovieManagementPanel;
import gui.mainPanels.adminPanels.ScreeningRoomManagementPanel;

public class AdminPanel extends JPanel {

    List<ScreeningRoom> screeningRooms = ScreeningRoom.getAllScreeningRooms();
    JTabbedPane tabbedPane = new JTabbedPane();

    ScreeningRoomManagementPanel screeningRoomManagementPanel = new ScreeningRoomManagementPanel(screeningRooms);
    JPanel movieManagementPanel = new MovieManagementPanel();

    public AdminPanel(CinemaGUI cinemaGUI, int width, int height) {
        setLayout(new BorderLayout());

        // Create a toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        add(toolbar, BorderLayout.NORTH);

        // Create a back button
        JButton backButton = new JButton();
        backButton.setText("Back");
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cinemaGUI.showMainMenu();
            }
        });
        toolbar.add(backButton);

        // Create a label with "admin panel" text
        JLabel label = new JLabel("Admin Panel");
        toolbar.add(label);

        // Tabbed Pane for the admin panel
        tabbedPane = new JTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        // tabbed pane 0 is the movie-hall management panel
        tabbedPane.add("Salon Yönetimi", screeningRoomManagementPanel);
        tabbedPane.add("Film Yönetimi", movieManagementPanel);
    }

    public void onVisible() {
        screeningRoomManagementPanel.repaintDayMoviePanels();
    }

    private class StatisticsPanel extends JPanel {
        public StatisticsPanel() {
            setLayout(new BorderLayout());
        }
    }
}
