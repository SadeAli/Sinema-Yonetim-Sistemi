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

	/*
	/**
	 * Retrieves all rows from the specified table in the database.
	 * 
	 * @param tableName the name of the table to retrieve rows from
	 * @param parser the ResultSetParser implementation to parse each row into an object of type T
	 * @return a list of objects representing the rows retrieved from the table
	
	public static <T> List<T> getAllRows(String tableName, ResultSetParser<T> parser) {
		List<T> objects = new ArrayList<>();
		String query = "SELECT * FROM " + tableName;

		try (	Connection connection = getConnection();
				Statement statement = connection.createStatement();
				ResultSet resultSet = statement.executeQuery(query);) {
			while (resultSet.next()) {
				T object = parser.parse(resultSet);
				objects.add(object);
			}
		} catch (SQLException e) {
			System.err.println("Unable to get rows: " + e.getMessage());
		}
		return objects;
	}
	*/

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
		String tableName = AnnotationUtils.getTableName(clazz);
	
		// Get the column names from the fields with the ColumnName annotation
		Map<String, Field> columnNamesAndFields = AnnotationUtils.getColumnNamesAndFields(clazz);
	
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
				// Create a new object of type T
				T object = AnnotationUtils.createNewInstance(clazz);

				// Set the fields of the object based on the column values in the result set
				result.add((T) AnnotationUtils.setFieldsFromResultSet(columnNamesAndFields, clazz, object, rs));
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
