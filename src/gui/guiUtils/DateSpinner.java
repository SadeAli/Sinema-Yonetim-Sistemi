package gui.guiUtils;

import java.time.LocalDate;
import java.util.Calendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

/**
 * A custom spinner component for selecting dates.
 */
public class DateSpinner extends JSpinner {

    /**
     * Constructs a new DateSpinner.
     */
    public DateSpinner() {
        super();

        // Create a SpinnerDateModel to handle date values
        SpinnerDateModel dateModel = new SpinnerDateModel();

        // Create a DateEditor to format the date display
        DateEditor editor = new JSpinner.DateEditor(this, "dd/MM/yy");

        // Set the SpinnerDateModel as the model for this spinner
        this.setModel(dateModel);

        // Set the calendar field to be modified when spinning
        dateModel.setCalendarField(Calendar.DAY_OF_MONTH);

        // Set the DateEditor as the editor for this spinner
        this.setEditor(editor);
    }

    /**
     * Returns the selected date as a LocalDate object.
     * 
     * @return the selected date as a LocalDate object
     */
    public LocalDate getDate() {
        return ((SpinnerDateModel) this.getModel()).getDate().toInstant().atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();
    }
}
