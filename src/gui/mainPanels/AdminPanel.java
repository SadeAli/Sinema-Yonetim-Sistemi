package gui.mainPanels;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import cinema.Movie;
import cinema.ScreeningRoom;
import gui.CinemaGUI;
import gui.mainPanels.adminPanels.MovieComboBox;
import gui.mainPanels.adminPanels.MovieManagementPanel;
import gui.mainPanels.adminPanels.ScreeningRoomManagementPanel;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminPanel extends JPanel {

    List<ScreeningRoom> screeningRooms = ScreeningRoom.getAllScreeningRooms();
    JTabbedPane tabbedPane = new JTabbedPane();

    ScreeningRoomManagementPanel screeningRoomManagementPanel = new ScreeningRoomManagementPanel(screeningRooms);
    JPanel movieManagementPanel = new MovieManagementPanel();
    DiscountPanel discountPanel = new DiscountPanel();
    StatisticsPanel statisticsPanel = new StatisticsPanel();

    public AdminPanel(CinemaGUI cinemaGUI, int width, int height) {
        setLayout(new BorderLayout());

        // Create a toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);

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
        add(toolbar, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // tabbed pane 0 is the movie-hall management panel
        tabbedPane.add("Room", screeningRoomManagementPanel);
        tabbedPane.add("Movie", movieManagementPanel);
        tabbedPane.add("Discount", discountPanel);
        tabbedPane.add("Statistics", statisticsPanel);
    }

    public void onVisible() {
        screeningRoomManagementPanel.repaintDayMoviePanels();
    }

    private class StatisticsPanel extends JPanel {
        
        private Movie selectedMovie;
        
        public StatisticsPanel() {
            setLayout(new BorderLayout());

            MovieComboBox movieCombobox = new MovieComboBox();
            
            // here is your panel
            JPanel centerPanel = new OmerPanel(selectedMovie);

            List<Movie> movies = Movie.getAllMovies();
            selectedMovie = movies.get(0);
            for (Movie m : movies) {
                movieCombobox.addItem(m);
            }

            add(movieCombobox, BorderLayout.NORTH);
            add(centerPanel, BorderLayout.CENTER);

            movieCombobox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    selectedMovie = (Movie) movieCombobox.getSelectedItem();
					((OmerPanel) centerPanel).updateDataset(selectedMovie);
                    centerPanel.repaint();
                }
            });
        }
    }

    private class DiscountPanel extends JPanel {
        public DiscountPanel() {
            

            // calendar panel
            JPanel calendarPanel = new JPanel(new GridLayout(7, 7));
            calendarPanel.setPreferredSize(new Dimension(400, 200));

            
            add(calendarPanel, BorderLayout.CENTER);
        }
    }

    private class OmerPanel extends JPanel {
		private DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
		public OmerPanel(Movie movie) {
			movie = Movie.getAllMovies().get(0);

			// Create a line chart
			
			updateDataset(movie);


			JFreeChart lineChart = ChartFactory.createLineChart(
				"Selled Seats per Day", // Chart title
				"Day", // X-axis label
				"Number of Seats", // Y-axis label
				dataset, // Dataset
				PlotOrientation.VERTICAL, // Plot orientation
				true, // Show legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
			);

			// Create a chart panel and add it to the OmerPanel
			ChartPanel chartPanel = new ChartPanel(lineChart);
			chartPanel.setPreferredSize(new Dimension(900, 500));

			this.add(chartPanel);
        }
		
		public void updateDataset(Movie movie) {
			// Clear the existing dataset
			dataset.clear();
			// TODO: add new series as selling rate per seat
		
			// Populate the dataset with new values
			for (int i = 0; i < 30; i++) {
				LocalDate date = LocalDate.now().minusDays(30 - i);
				dataset.addValue(Movie.getSales(movie.getId(), date), 
					movie.getName(),
					date.format(DateTimeFormatter.ofPattern("dd/MM")));
			}
		}
    }

}
