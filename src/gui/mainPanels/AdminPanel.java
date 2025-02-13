/**
 * This class represents the Admin Panel of the cinema management system.
 * It extends the JPanel class and provides functionality for managing screening rooms, movies, and statistics.
 * The Admin Panel contains a toolbar with a back button and a label, and a tabbed pane with three panels:
 * - ScreeningRoomManagementPanel: Allows the user to manage screening rooms.
 * - MovieManagementPanel: Allows the user to manage movies.
 * - StatisticsPanel: Displays statistics about movie sales and occupancy ratio.
 * 
 * The Admin Panel is used in the CinemaGUI class to display the admin interface.
 */
package gui.mainPanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;

import cinema.Movie;
import cinema.ScreeningRoom;
import gui.CinemaGUI;
import gui.guiUtils.MovieComboBox;
import gui.mainPanels.adminPanels.MovieManagementPanel;
import gui.mainPanels.adminPanels.ScreeningRoomManagementPanel;
import java.time.format.DateTimeFormatter;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.LineAndShapeRenderer;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;


/**
 * Represents the admin panel of the cinema management system.
 * This panel provides various management functionalities for the admin user.
 * It includes tabs for managing screening rooms, movies, and statistics.
 */
public class AdminPanel extends JPanel {

    List<ScreeningRoom> screeningRooms = ScreeningRoom.getAllScreeningRooms();
    JTabbedPane tabbedPane = new JTabbedPane();

    ScreeningRoomManagementPanel screeningRoomManagementPanel = new ScreeningRoomManagementPanel(screeningRooms);
    JPanel movieManagementPanel = new MovieManagementPanel();
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
            
            JPanel centerPanel = new CustomChartPanel(selectedMovie);

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
					((CustomChartPanel) centerPanel).updateDataset(selectedMovie);
                    centerPanel.repaint();
                }
            });
        }
    }

    private class CustomChartPanel extends JPanel {
		DefaultCategoryDataset dataset1 = new DefaultCategoryDataset();
		CategoryItemRenderer renderer1 = new LineAndShapeRenderer();

		DefaultCategoryDataset dataset2 = new DefaultCategoryDataset();
		CategoryItemRenderer renderer2 = new LineAndShapeRenderer();
        
		public CustomChartPanel(Movie movie) {
			movie = Movie.getAllMovies().get(0);

			// Create a line chart
			
			updateDataset(movie);

			// Create the plot
			CategoryPlot plot = new CategoryPlot();
			plot.setDataset(0, dataset1);
			plot.setRenderer(0, renderer1);
			plot.setRangeAxis(0, new NumberAxis("Seats"));

			CategoryAxis domainAxis = new CategoryAxis("Date");
			domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
			plot.setDomainAxis(domainAxis);

			plot.setDataset(1, dataset2);
			plot.setRenderer(1, renderer2);
			plot.setRangeAxis(1, new NumberAxis("Ratio"));

			/*
			JFreeChart lineChart = ChartFactory.createLineChart(
				"Selled Seats per Day", // Chart title
				"Day", // X-axis label
				"Number of Seats", // Y-axis label
				dataset1, // Dataset
				PlotOrientation.VERTICAL, // Plot orientation
				true, // Show legend
				true, // Use tooltips
				false // Configure chart to generate URLs?
			);
*/
			// Create the chart with the plot
			JFreeChart chart = new JFreeChart(plot);

			// Create a chart panel and add it to the OmerPanel
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPreferredSize(new Dimension(900, 500));

			this.add(chartPanel);
        }
		
		public void updateDataset(Movie movie) {
			// Clear the existing dataset
			dataset1.clear();

			List<Integer> assignedList = movie.getAssignedSeatCountListForLast30Days();
			
			// Find the maximum value of the dataset
			int maxAssigned = Collections.max(assignedList);

			// Populate the dataset with new values
			for (int i = 0; i < 30; i++) {
				LocalDate date = LocalDate.now().minusDays(29 - i);
				String dateStr = date.format(DateTimeFormatter.ofPattern("dd/MM"));
				int sales = Movie.getSales(movie.getId(), date);
				int assigned = assignedList.get(i);

				double normalizedValue = 0.0;

				if (assigned != 0) {
					normalizedValue = ((double)sales / assigned) * maxAssigned;
				}
				
				// Add the number of selled and assigned seats for the given date
				dataset1.addValue(
					sales, 
					"Sold Seats",
					dateStr
				);
				
				dataset1.addValue(
					assigned,
					"Assigned Seats",
					dateStr
				);

				dataset1.addValue(normalizedValue, "Occupancy Ratio", dateStr);
			}
		}
    }

}
