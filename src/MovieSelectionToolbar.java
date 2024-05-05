import javax.swing.JToolBar;
import javax.xml.crypto.dsig.spec.XPathType.Filter;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.util.Collections;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JDialog;
import java.awt.BorderLayout;

public class MovieSelectionToolbar extends JToolBar {

    private MovieSelectionPanel parent;

    private class FilterPopup extends JDialog {
        private JTextField searchField;
        private JComboBox<String> sortComboBox;

        FilterPopup() {
            setSize(300, 300);
            setVisible(false);
            setResizable(false);
            setLocationRelativeTo(null);

            searchField = new JTextField();
            searchField.setDragEnabled(false);
            searchField.setPreferredSize(new Dimension(300, 30));
            add(searchField);

            sortComboBox = new JComboBox<>();
            sortComboBox.addItem("Sort by Name");
            sortComboBox.addItem("Sort by Rating");
            sortComboBox.addItem("Sort by Release Date");
            add(sortComboBox);

            sortComboBox.addActionListener(e -> {
                String selectedSortOption = (String) sortComboBox.getSelectedItem();

                switch (selectedSortOption) {
                    case "Sort by Name":
                        parent.sortMoviesByName();
                        break;
                    case "Sort by Rating":
                        parent.sortMoviesByRating();
                        break;
                    case "Sort by Release Date":
                        parent.sortMoviesByReleaseDate();
                        break;
                }
            });
        }
    }

    MovieSelectionToolbar(MovieSelectionPanel parent) {
        this.parent = parent;
        setFloatable(false);

        JPanel toolbarPanel = new JPanel();
        toolbarPanel.setLayout(new BorderLayout());

        // back button
        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> {
            parent.goBack();
        });
        toolbarPanel.add(backButton, BorderLayout.WEST);

        // filter window
        JDialog filterWindow = new FilterPopup();

        // open a new window when filter button is clicked
        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(e -> {
            filterWindow.setVisible(true);
        });
        toolbarPanel.add(filterButton, BorderLayout.EAST);

        add(toolbarPanel);
     }

    /**
     * Adds a sort combo box to the toolbar.
     * 
     * @param toolbar The toolbar to add the combo box to.
     * @return The created combo box.
     */
    private JComboBox<String> addSortComboBox(JToolBar toolbar) {
        JComboBox<String> sortComboBox = new JComboBox<>();
        sortComboBox.addItem("Sort by Name");
        sortComboBox.addItem("Sort by Rating");
        sortComboBox.addItem("Sort by Release Date");

        toolbar.add(sortComboBox);

        sortComboBox.addActionListener(e -> {
            String selectedSortOption = (String) sortComboBox.getSelectedItem();

            switch (selectedSortOption) {
                case "Sort by Name":
                    parent.sortMoviesByName();
                    break;
                case "Sort by Rating":
                    parent.sortMoviesByRating();
                    break;
                case "Sort by Release Date":
                    parent.sortMoviesByReleaseDate();
                    break;
            }
        });

        return sortComboBox;
    }
}
  