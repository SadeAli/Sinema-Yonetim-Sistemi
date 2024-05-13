package gui.guiUtils;

import java.time.LocalDate;
import java.util.Calendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

public class DateSpinner extends JSpinner {

    public DateSpinner() {
        super();

        SpinnerDateModel dateModel = new SpinnerDateModel();
        DateEditor editor = new JSpinner.DateEditor(this, "dd/MM/yy");
        this.setModel(dateModel);
        dateModel.setCalendarField(Calendar.DAY_OF_MONTH);

        this.setEditor(editor);
    }

    public LocalDate getDate() {
        return ((SpinnerDateModel) this.getModel()).getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
    }
}
