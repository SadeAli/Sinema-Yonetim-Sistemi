package cinema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.*;

@TableName("discount")
public class Discount {
	
	@PrimaryKey
	@ColumnName("id")
	private int id;

	@ColumnName("date")
	private LocalDate date;

	// Range of discount is from 0 to 1
	@ColumnName("ratio")
	private double ratio;

	// Constructor
	private Discount() {}

	public Discount(LocalDate date, double ratio) {
		this.date = date;
		this.ratio = ratio;
	}

	// Getters

	public int getId() {
		return id;
	}

	public LocalDate getDate() {
		return date;
	}

	public double getRatio() {
		return ratio;
	}

	// Setters

	public void setId(int id) {
		this.id = id;
	}

	public static List<Discount> getAllDiscounts() {
		try {
			return DatabaseManager.getAllRows(Discount.class);
		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}
}
