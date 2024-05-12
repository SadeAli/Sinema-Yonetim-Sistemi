package database;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
	public static final String SQLITE_JDBC_URL = "jdbc:sqlite:data/cinema_mecpine.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_JDBC_URL);
    }

	public static boolean closeConnection(Connection connection) {
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
	public static <T> List<T> getRowsFilteredAndSortedBy(Class<T> clazz, List<FilterCondition> filters, String sortBy, boolean ascending) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
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
				if (filter.getRelationOperator().equalsIgnoreCase("LIKE")) {
					DatabaseAnnotationUtils.setPreparedStatementValue(stmt, index, "%" + filter.getValue() + "%");
				} else {
					DatabaseAnnotationUtils.setPreparedStatementValue(stmt, index, filter.getValue());
				}
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
	 * Inserts a new row into the database table based on the provided object.
	 *
	 * @param object the object representing the row to be inserted
	 * @return the ID of the newly inserted row
	 * @throws SQLException if an error occurs while executing the SQL statement
	 * @throws IllegalArgumentException if the provided object does not have a TableName annotation
	 * @throws RuntimeException if unable to access a field in the provided object
	 * @throws SQLException if creating the row fails or no ID is obtained
	 * @throws IllegalAccessException 
	 */
	public static <T> int insertRow(T object) throws SQLException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
		
		// Build the SQL query
		StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + tableName + " (");
		StringBuilder valuesBuilder = new StringBuilder(") VALUES (");
	
		// Get all fields in the class
		Field[] fields = clazz.getDeclaredFields();
		List<Object> values = new ArrayList<>();
	
		// Add the column names and values to the query
		for (Field field : fields) {
			if (!DatabaseAnnotationUtils.isPrimaryKey(field) && field.getAnnotation(ColumnName.class) != null) {
				String columnName = DatabaseAnnotationUtils.getColumnName(field);
				values.add(DatabaseAnnotationUtils.getFieldValue(field, object));
				queryBuilder.append(columnName).append(", ");
				valuesBuilder.append("?, ");
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
			for (int i = 0, offsetForId = 0; i < values.size() + offsetForId; i++) {
				if (!DatabaseAnnotationUtils.isPrimaryKey(fields[i]) && fields[i].getAnnotation(ColumnName.class) != null){
					DatabaseAnnotationUtils.setPreparedStatementValue(statement, i + 1 - offsetForId, values.get(i - offsetForId));
				} else {
					offsetForId++;
				}
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

	public static boolean updateRow(Object object) throws SQLException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
	
		// Build the SQL query
		StringBuilder queryBuilder = new StringBuilder("UPDATE " + tableName + " SET ");
	
		// Get all fields in the class
		Field[] fields = clazz.getDeclaredFields();
		List<Object> values = new ArrayList<>();
	
		int i = 1;
		int idIndex = 0;
		// Add the column names and values to the query
		for (Field field : fields) {
			if (!DatabaseAnnotationUtils.isPrimaryKey(field) && field.getAnnotation(ColumnName.class) != null) {
				String columnName = DatabaseAnnotationUtils.getColumnName(field);
				values.add(DatabaseAnnotationUtils.getFieldValue(field, object));
				queryBuilder.append(columnName).append(" = ?, ");
			} else if (DatabaseAnnotationUtils.isPrimaryKey(field)) {
				idIndex = i;
			}
			i++;
		}
	
		// Remove the last comma and space from the query and add the WHERE clause
		queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());
		queryBuilder.append(" WHERE id = ?");
	
		// Build the final query string
		String query = queryBuilder.toString();
	
		// Execute the query
		try (Connection connection = getConnection();
			 PreparedStatement statement = connection.prepareStatement(query)) {
		
			DatabaseAnnotationUtils.setPreparedStatementValueSet(
				DatabaseAnnotationUtils.getColumnNamesAndFields(clazz),
				object,
				statement
			);

			statement.setInt(idIndex, DatabaseAnnotationUtils.getPrimaryKeyValue(object));
	
			int affectedRows = statement.executeUpdate();
	
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Unable to update row: " + e.getMessage());
			return false;
		}
	}

	public static <T> List<T> getClassWithQuery(Class<T> clazz, String addQuery) throws SQLException {
		StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT ");
		
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);

		queryBuilder.append(tableName).append(".* FROM ").append(tableName).append(" ").append(addQuery);

		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString());
			 ResultSet rs = stmt.executeQuery()) {
			List<T> result = new ArrayList<>();
			while (rs.next()) {
				T object = DatabaseAnnotationUtils.createNewInstance(clazz);
				result.add((T) DatabaseAnnotationUtils.setFieldsFromResultSet(DatabaseAnnotationUtils.getColumnNamesAndFields(clazz), clazz, object, rs));
			}
			return result;
		}
	}

	public static <T> T getRowById (Class<T> clazz, int id) throws SQLException {
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
		
		// Get a map of column names and fields from the ColumnName annotations
		Map<String, Field> columnNamesAndFields = DatabaseAnnotationUtils.getColumnNamesAndFields(clazz);

		String query = "SELECT * FROM " + tableName + " WHERE id = ?";

		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, id);
			
			T object = null;
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					object = DatabaseAnnotationUtils.createNewInstance(clazz);
					object = DatabaseAnnotationUtils.setFieldsFromResultSet(columnNamesAndFields, clazz, object, rs);
				}
			}

			return object;
		}
	}
	

	/**
	 * Checks if a row exists in the database table based on the provided filter conditions.
	 *
	 * @param clazz the class representing the database table
	 * @param filters a list of FilterCondition objects representing the filter conditions
	 * @return true if a row exists that matches the filter conditions, false otherwise
	 * @throws SQLException if a database access error occurs
	 * @throws IllegalAccessException if the class or its nullary constructor is not accessible
	 * @throws InstantiationException if the class that declares the underlying constructor represents an abstract class
	 * @throws NoSuchFieldException if a field with the specified name is not found
	 * @throws IllegalArgumentException if the class does not have a TableName annotation or any fields with the ColumnName annotation
	 */
	public static <T> boolean exists(Class<T> clazz, List<FilterCondition> filters) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);

		// Build the SQL query
		StringBuilder query = new StringBuilder("SELECT 1 FROM " + tableName);

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

		// Execute the query and check if any rows are returned
		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query.toString())) {
				// Set the filter values in the PreparedStatement
				int index = 1;
				for (FilterCondition filter : filters) {
					DatabaseAnnotationUtils.setPreparedStatementValue(stmt, index, filter.getValue());
					index++;
				}
		
				ResultSet rs = stmt.executeQuery();
			return rs.next();
		} catch (Exception e) {
			System.err.println("Unable to check if row exists: " + e.getMessage());
			throw e;
		}
	}

	/**
	 * Updates the row in the database table based on the provided object.
	 *
	 * @param object the object representing the row to be updated
	 * @return true if the row was updated successfully, false otherwise
	 * @throws SQLException if an error occurs while executing the SQL statement
	 * @throws IllegalArgumentException if the provided object does not have a TableName annotation
	 * @throws RuntimeException if unable to access a field in the provided object
	 */
	public static boolean exists(String tableName, int id) throws SQLException {
		String query = "SELECT 1 FROM " + tableName + " WHERE id = ? LIMIT 1";

		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (rs.next()) {
					return true;
				}
			}
		}
		return false;
	}

	public static <T> int count (Class<T> clazz, List<FilterCondition> filters) throws NoSuchFieldException, Exception {
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM " + tableName);

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

		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query.toString())) {
			// Set the filter values in the PreparedStatement
			int index = 1;
			for (FilterCondition filter : filters) {
				DatabaseAnnotationUtils.setPreparedStatementValue(stmt, index, filter.getValue());
				index++;
			}
	
			ResultSet rs = stmt.executeQuery();
			return rs.getInt(1);

		} catch (Exception e) {
			System.err.println("Unable to count rows: " + e.getMessage());
			throw e;
		}
	}
}
