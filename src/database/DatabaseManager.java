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

/**
 * Returns a connection to the database.
 *
 * @return a connection to the database
 * @throws SQLException if a database access error occurs
 */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(SQLITE_JDBC_URL);
    }

	/**
	 * Closes the given database connection.
	 *
	 * @param connection the database connection to be closed
	 * @return true if the connection is successfully closed, false otherwise
	 */
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
	 * Sets the auto-commit mode for the given database connection.
	 *
	 * @param connection the database connection
	 * @param autoCommit the desired auto-commit mode (true for auto-commit enabled, false for disabled)
	 * @return true if the auto-commit mode was successfully set, false otherwise
	 */
	public static boolean setAutoCommit(Connection connection, boolean autoCommit) {
		if (connection != null) {
			try {
				connection.setAutoCommit(autoCommit);
				return true;
			} catch (SQLException e) {
				System.err.println("Unable to set auto commit: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Rolls back the changes made in the current transaction of the specified connection.
	 * 
	 * @param connection the connection to rollback the changes on
	 * @return true if the rollback is successful, false otherwise
	 */
	public static boolean rollback(Connection connection) {
		if (connection != null) {
			try {
				connection.rollback();
				return true;
			} catch (SQLException e) {
				System.err.println("Unable to rollback: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Commits the changes made in the current transaction to the database.
	 * 
	 * @param connection the database connection
	 * @return true if the changes are successfully committed, false otherwise
	 */
	public static boolean commit(Connection connection) {
		if (connection != null) {
			try {
				connection.commit();
				return true;
			} catch (SQLException e) {
				System.err.println("Unable to commit: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Closes the given database statement.
	 * 
	 * @param statement the statement to be closed
	 * @return true if the statement is successfully closed, false otherwise
	 */
	public static boolean closeStatement (Statement statement) {
		if (statement != null) {
			try {
				statement.close();
				return true;
			} catch (SQLException e) {
				System.err.println("Unable to close statement: " + e.getMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	
	/**
	 * Closes a list of database statements.
	 * 
	 * @param statementList the list of statements to be closed
	 * @return true if all statements were successfully closed, false otherwise
	 */
	public static boolean closeStatements (List<Statement> statementList) {
		boolean success = true;
		if (statementList == null) {
			return success;
		}
		for (Statement stmt : statementList) {
			if (!closeStatement(stmt)) {
				success = false;
			}
		}
		return success;
	}

	
	/**
	 * Clears the resources by closing the statements and the connection.
	 *
	 * @param conn The connection to be closed.
	 * @param statementList The list of statements to be closed.
	 * @return {@code true} if the resources are cleared successfully, {@code false} otherwise.
	 */
	public static boolean clearResources (Connection conn, List<Statement> statementList) {
		boolean success = true;
		
		success = closeStatements(statementList);
		
		if (!closeConnection(conn)) {
			success = false;
		}

		return success;
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
	 * Retrieves a list of objects of the specified class from the database, filtered and sorted according to the provided parameters.
	 *
	 * @param clazz The class of the objects to retrieve.
	 * @param filters The list of filter conditions to apply to the query.
	 * @param sortBy The field name to sort the results by. Can be null if no sorting is required.
	 * @param ascending Specifies whether the results should be sorted in ascending order.
	 * @param connection The database connection to use for executing the query.
	 * @return A list of objects of the specified class, filtered and sorted according to the provided parameters.
	 * @throws SQLException If an error occurs while executing the SQL query.
	 * @throws NoSuchFieldException If the specified field does not exist in the class.
	 */
	public static <T> List<T> getRowsFilteredAndSortedBy(Class<T> clazz, List<FilterCondition> filters, String sortBy, boolean ascending, Connection connection) throws SQLException, NoSuchFieldException {
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
		PreparedStatement stmt = null;
		try {
			stmt = connection.prepareStatement(query.toString());
	
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
		} catch (Exception e) {
			System.err.println("Unable to get rows: " + e.getMessage());
			throw e;
		} finally {
			closeStatement(stmt);
		}
	}

	/**
	 * Retrieves a list of rows from the database table, filtered and sorted by the specified conditions.
	 *
	 * @param clazz     The class representing the type of objects to retrieve.
	 * @param filters   The list of filter conditions to apply.
	 * @param sortBy    The name of the column to sort the rows by.
	 * @param ascending A boolean value indicating whether to sort the rows in ascending order.
	 * @return A list of objects of type T that match the filter conditions and are sorted according to the specified column.
	 * @throws SQLException           If an error occurs while accessing the database.
	 * @throws IllegalAccessException If the class or its nullary constructor is not accessible.
	 * @throws InstantiationException If the class represents an abstract class, an interface, an array class, a primitive type, or void; or if the class has no nullary constructor.
	 * @throws NoSuchFieldException    If the specified sortBy column does not exist in the class.
	 */
	public static <T> List<T> getRowsFilteredAndSortedBy(Class<T> clazz, List<FilterCondition> filters, String sortBy, boolean ascending) throws SQLException, IllegalAccessException, InstantiationException, NoSuchFieldException {
		try (Connection connection = getConnection()) {
			return getRowsFilteredAndSortedBy(clazz, filters, sortBy, ascending, connection);
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

	/**
	 * Updates a row in the database table corresponding to the given object.
	 *
	 * @param object the object representing the row to be updated
	 * @return true if the row was successfully updated, false otherwise
	 * @throws SQLException if a database access error occurs
	 * @throws IllegalArgumentException if the object is null or does not have a valid primary key value
	 * @throws IllegalAccessException if the object's fields cannot be accessed
	 */
	public static boolean updateRow(Object object) throws SQLException, IllegalArgumentException, IllegalAccessException {
		Class<?> clazz = object.getClass();
		// Get the table name from the TableName annotation
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
	
		// Build the SQL query
		StringBuilder queryBuilder = new StringBuilder("UPDATE " + tableName + " SET ");
	
		// Get all fields in the class
		Field[] fields = clazz.getDeclaredFields();
		List<Object> values = new ArrayList<>();
	
		int idIndex = 1;
		// Add the column names and values to the query
		for (Field field : fields) {
			if (!DatabaseAnnotationUtils.isPrimaryKey(field) && field.getAnnotation(ColumnName.class) != null) {
				String columnName = DatabaseAnnotationUtils.getColumnName(field);
				values.add(DatabaseAnnotationUtils.getFieldValue(field, object));
				queryBuilder.append(columnName).append(" = ?, ");
				idIndex++;
			}
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

	/**
	 * Retrieves a list of objects of the specified class from the database based on the provided query.
	 *
	 * @param <T>       the type of objects to retrieve
	 * @param clazz     the class of objects to retrieve
	 * @param addQuery  the additional query to append to the SELECT statement
	 * @return          a list of objects of the specified class retrieved from the database
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Retrieves a row from the database table based on the provided class and ID.
	 *
	 * @param <T> the type of the object to retrieve from the database
	 * @param clazz the class of the object to retrieve from the database
	 * @param id the ID of the row to retrieve
	 * @return the retrieved object, or null if no matching row is found
	 * @throws SQLException if a database access error occurs
	 */
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

	/**
	 * Counts the number of rows in the specified table that match the given filters.
	 *
	 * @param <T> the type of the entity class
	 * @param clazz the class representing the entity
	 * @param filters the list of filter conditions to apply
	 * @return the number of rows that match the filters
	 * @throws NoSuchFieldException if a field specified in the filters does not exist in the entity class
	 * @throws Exception if an error occurs while executing the count query
	 */
	public static <T> int count(Class<T> clazz, List<FilterCondition> filters) throws NoSuchFieldException, Exception {
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

	/**
	 * Deletes a row from the database table based on the provided class and ID.
	 *
	 * @param clazz the class representing the database table
	 * @param id the ID of the row to be deleted
	 * @return true if the row was successfully deleted, false otherwise
	 * @throws SQLException if an error occurs while deleting the row
	 */
	public static <T> boolean deleteRow(Class<T> clazz, int id) throws SQLException {
		String tableName = DatabaseAnnotationUtils.getTableName(clazz);
		String query = "DELETE FROM " + tableName + " WHERE id = ?";

		try (Connection connection = getConnection();
			 PreparedStatement stmt = connection.prepareStatement(query)) {
			stmt.setInt(1, id);
			int affectedRows = stmt.executeUpdate();
			return affectedRows > 0;
		} catch (SQLException e) {
			System.err.println("Unable to delete row: " + e.getMessage());
			return false;
		}
	}

}
