import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dbanno.ColumnName;
import dbanno.DatabaseAnnotationUtils;
import dbanno.TableName;
import java.util.logging.Logger;

public class DatabaseManager {
	private static final String SQLITE_JDBC_URL = "jdbc:sqlite:data/cinema_mecpine_fake.db";

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_JDBC_URL);
    }

	private static boolean closeConnection(Connection connection) {
        if (connection != null) {
            try {
				connection.close();
				return true;
            } catch (SQLException e) {
                System.err.println("Unable to close connection: " + e.getMessage());
            }
		}
		return false;
    }

	/**
	 * Retrieves all rows from the database table associated with the given class.
	 * 
	 * @param clazz the class representing the database table
	 * @return a list of objects representing the rows in the database table
	 * @throws SQLException if a database access error occurs
	 * @throws IllegalAccessException if the class or its nullary constructor is not accessible
	 * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class
	 * @throws NoSuchFieldException if a field with the specified name is not found
	 * @throws IllegalArgumentException if the class does not have a TableName annotation or any fields with the ColumnName annotation
	 */
	public static <T> List<T> getAllRows(Class<T> clazz) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
	
		// Get a map of column names and fields from the ColumnName annotations
		Map<String, Field> columnNamesAndFields = DatabaseAnnotationUtils.getColumnNamesAndFields(clazz);
	
		// Build the SQL query
		String query = "SELECT * FROM " + tableName;
	
		// Execute the query and get the result set
		try (	Connection connection = getConnection();
				PreparedStatement stmt = connection.prepareStatement(query);
			 	ResultSet rs = stmt.executeQuery()) {
	
			// Create a list to hold the result objects
			List<T> result = new ArrayList<>();

			// For each row in the result set, create an object and add it to the list
			while (rs.next()) {
				T object = DatabaseAnnotationUtils.createNewInstance(clazz);
				result.add((T) DatabaseAnnotationUtils.setFieldsFromResultSet(columnNamesAndFields, clazz, object, rs));
			}
	
			// Return the list of objects
			return result;
		}
	}
	private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());


	private static void setPreparedStatementValue(PreparedStatement stmt, int index, Object value) throws SQLException {
		if (value instanceof LocalDate) {
			stmt.setString(index, ((LocalDate) value).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
		} else if (value instanceof Integer) {
			stmt.setInt(index, (Integer) value);
		} else if (value instanceof Float) {
			stmt.setFloat(index, (Float) value);
		} else if (value instanceof Double) {
			stmt.setDouble(index, (Double) value);
		} else if (value instanceof Long) {
			stmt.setLong(index, (Long) value);
		} else if (value instanceof Boolean) {
			stmt.setBoolean(index, (Boolean) value);
		} else {
			LOGGER.warning("Setting value with setObject for type: " + value.getClass().getName());
			stmt.setObject(index, value);
		}
	}

	/**
	 * Retrieves all rows from the database table associated with the given class, filtered and sorted by the provided conditions.
	 * 
	 * @param clazz the class representing the database table
	 * @param filters a list of FilterCondition objects representing the filter conditions
	 * @param sortBy the name of the field to sort by
	 * @param ascending true if the results should be sorted in ascending order, false if descending
	 * @return a list of objects representing the rows in the database table that match the filter conditions, sorted as specified
	 * @throws SQLException if a database access error occurs
	 * @throws IllegalAccessException if the class or its nullary constructor is not accessible
	 * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class
	 * @throws NoSuchFieldException if a field with the specified name is not found
	 * @throws IllegalArgumentException if the class does not have a TableName annotation or any fields with the ColumnName annotation
	 */
	public static <T> List<T> getRowsFilteredAndSortedBy(Class<T> clazz, List<FilterCondition> filters, String sortBy, Boolean ascending) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
	
		// Get a map of column names and fields from the ColumnName annotations
		Map<String, Field> columnNamesAndFields = DatabaseAnnotationUtils.getColumnNamesAndFields(clazz);
	
		// Build the SQL query
		StringBuilder query = new StringBuilder("SELECT * FROM " + tableName);
	
		// Add the filters to the query
		if (!filters.isEmpty()) {
			query.append(" WHERE ");
			for (FilterCondition filter : filters) {
				Field field = clazz.getDeclaredField(filter.getFieldName());
				String columnName = DatabaseAnnotationUtils.getColumnName(field);
				query.append(columnName).append(" ").append(filter.getRelationOperator()).append(" ? AND ");
			}
			// Remove the last " AND "
			query.setLength(query.length() - 5);
		}
	
		// Add the sort by clause to the query
		if (sortBy != null) {
			Field field = clazz.getDeclaredField(sortBy);
			String columnName = DatabaseAnnotationUtils.getColumnName(field);
			query.append(" ORDER BY ").append(columnName).append(ascending ? " ASC" : " DESC");
		}
	
		// Execute the query and get the result set
		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query.toString())) {
	
			// Set the filter values in the PreparedStatement
			int index = 1;
			for (FilterCondition filter : filters) {
				// TODO handle different types of values especially LocalDate
				setPreparedStatementValue(stmt, index, filter.getValue());
				index++;
			}
	
			ResultSet rs = stmt.executeQuery();
	
			// Create a list to hold the result objects
			List<T> result = new ArrayList<>();
	
			// For each row in the result set, create a new object and set its fields from the result set
			while (rs.next()) {
				T object = DatabaseAnnotationUtils.createNewInstance(clazz);
				result.add((T) DatabaseAnnotationUtils.setFieldsFromResultSet(columnNamesAndFields, clazz, object, rs));
			}
	
			// Return the list of objects
			return result;
		}
	}

	/**
	 * Retrieves a row from the specified table by its ID.
	 *
	 * @param <T> the type of the object to be returned
	 * @param tableName the name of the table
	 * @param id the ID of the row to retrieve
	 * @param parser the ResultSetParser used to parse the result set into an object of type T
	 * @return the object representing the retrieved row, or null if no row is found
	 *
	public static <T> T getRowById(String tableName, int id, ResultSetParser<T> parser) {
		T object = null;
		String query = "SELECT * FROM " + tableName + " WHERE id = ?";

		try (	Connection connection = getConnection();
				PreparedStatement pStatement = connection.prepareStatement(query);) {
			pStatement.setInt(1, id);
			try (ResultSet resultSet = pStatement.executeQuery()) {
				if (resultSet.next()) {
					object = parser.parse(resultSet);
				}
			}
		} catch (SQLException e) {
			System.err.println("Unable to get row: " + e.getMessage());
		}
		return object;
	}*/

	/**
	 * Inserts a new row into the database table based on the provided object.
	 *
	 * @param object the object representing the row to be inserted
	 * @return the ID of the newly inserted row
	 * @throws SQLException if an error occurs while executing the SQL statement
	 * @throws IllegalArgumentException if the provided object does not have a TableName annotation
	 * @throws RuntimeException if unable to access a field in the provided object
	 * @throws SQLException if creating the row fails or no ID is obtained
	 */
	public static <T> int insertRow(T object) throws SQLException {
		Class<?> clazz = object.getClass();
		// Get the table name from the TableName annotation
		TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
		if (tableNameAnnotation == null) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " does not have a TableName annotation");
		}
		String tableName = tableNameAnnotation.value();
		
		// Build the SQL query
		StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
		StringBuilder valuesBuilder = new StringBuilder(") VALUES (");
	
		// Get all fields in the class
		Field[] fields = clazz.getDeclaredFields();
		List<Object> values = new ArrayList<>();
	
		// Iterate through the fields and get the column name and value if the field has a ColumnName annotation
		for (Field field : fields) {
			ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
			if (columnNameAnnotation != null) {
				String columnName = columnNameAnnotation.value();
				queryBuilder.append(columnName).append(", ");
				valuesBuilder.append("?, ");
				field.setAccessible(true);
				try {
					Object value = field.get(object);
					values.add(value);
				} catch (IllegalAccessException e) {
					throw new RuntimeException("Unable to access field " + field.getName(), e);
				} finally {
					field.setAccessible(false);
				}
			}
		}
	
		// Remove the last comma and space from the query and values builders and add the closing parentheses
		queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());
		valuesBuilder.delete(valuesBuilder.length() - 2, valuesBuilder.length());
		valuesBuilder.append(")");
	
		// Build the final query string
		String query = queryBuilder.toString() + valuesBuilder.toString();
	
		// Execute the query and return the generated ID
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
			for (int i = 0; i < values.size(); i++) {
				statement.setObject(i + 1, values.get(i));
			}
	
			int affectedRows = statement.executeUpdate();
	
			if (affectedRows == 0) {
				throw new SQLException("Creating row failed, no rows affected.");
			}
	
			try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					return generatedKeys.getInt(1);
				} else {
					throw new SQLException("Creating row failed, no ID obtained.");
				}
			}
		}
	}
}
