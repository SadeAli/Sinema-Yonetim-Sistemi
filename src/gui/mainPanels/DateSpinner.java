package gui.mainPanels;

import java.awt.Dimension;
import java.time.LocalDate;
import java.util.Calendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class DateSpinner extends JSpinner {

    public DateSpinner() {

        SpinnerDateModel dateModel = new SpinnerDateModel();

        super(dateModel);

        DateEditor editor = new JSpinner.DateEditor(this, "dd/MM/yy");
        dateModel.setCalendarField(Calendar.DAY_OF_MONTH);

        this.setEditor(editor);
    }

    public LocalDate getDate() {
        return ((SpinnerDateModel) this.getModel()).getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
