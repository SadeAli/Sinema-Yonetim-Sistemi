package database;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DatabaseAnnotationUtils {
	
	private DatabaseAnnotationUtils() {
		throw new AssertionError("Cannot instantiate DatabaseAnnotationUtils");
	}

	/**
	 * Retrieves the table name associated with the given class.
	 *
	 * @param clazz The class for which to retrieve the table name.
	 * @return The table name associated with the class.
	 * @throws IllegalArgumentException If the class does not have a TableName annotation.
	 */
    public static String getTableName(Class<?> clazz) {
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " does not have a TableName annotation");
        }
        return tableNameAnnotation.value();
    }

	/**
	 * Retrieves the column name associated with the given field.
	 *
	 * @param field The field for which to retrieve the column name.
	 * @return The column name associated with the field.
	 * @throws IllegalArgumentException If the field does not have a ColumnName annotation.
	 */
	public static String getColumnName(Field field) {
		ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
		if (columnNameAnnotation == null) {
			throw new IllegalArgumentException("Field " + field.getName() + " does not have a ColumnName annotation");
		}
		return columnNameAnnotation.value();
	}

	/**
	 * Retrieves the column names and corresponding fields of a given class annotated with @ColumnName.
	 *
	 * @param clazz the class to retrieve the column names and fields from
	 * @return a map containing the column names as keys and the corresponding fields as values
	 * @throws IllegalArgumentException if the class does not have any fields with the ColumnName annotation
	 */
	public static Map<String, Field> getColumnNamesAndFields(Class<?> clazz) {
		Map<String, Field> columnFieldMap = new LinkedHashMap<>();
		Field[] fields = clazz.getDeclaredFields();

		for (Field field : fields) {
			ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
			if (columnNameAnnotation != null) {
				columnFieldMap.put(columnNameAnnotation.value(), field);
			}
		}

		if (columnFieldMap.isEmpty()) {
			throw new IllegalArgumentException("Class " + clazz.getName() + " does not have any fields with the ColumnName annotation");
		}

		return columnFieldMap;
	}

	/**
	 * Returns the name of the primary key column for the given class.
	 *
	 * @param clazz the class for which to retrieve the primary key column name
	 * @return the name of the primary key column
	 * @throws IllegalArgumentException if the class does not have a field with the PrimaryKey annotation
	 */
	public static String getPrimaryKey(Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (field.getAnnotation(PrimaryKey.class) != null) {
				return getColumnName(field);
			}
		}
		throw new IllegalArgumentException("Class " + clazz.getName() + " does not have a field with the PrimaryKey annotation");
	}

	/**
	 * Retrieves the value of the primary key field from the given object.
	 *
	 * @param object the object from which to retrieve the primary key value
	 * @return the value of the primary key field
	 * @throws IllegalArgumentException if the object does not have a field with the PrimaryKey annotation
	 */
	public static int getPrimaryKeyValue(Object object) {
		Class<?> clazz= object.getClass();
		for (Field field : clazz.getDeclaredFields()) {
			if (field.getAnnotation(PrimaryKey.class) != null) {
				return (int) getFieldValue(field, object);
			}
		}
		throw new IllegalArgumentException("Object " + object.getClass().getName() + " does not have a field with the PrimaryKey annotation");
	}

	/**
	 * Creates a new instance of the specified class using its default constructor.
	 *
	 * @param <T> the type of the object to create
	 * @param clazz the class of the object to create
	 * @return a new instance of the specified class
	 * @throws RuntimeException if unable to create the object
	 */
	public static <T> T createNewInstance(Class<T> clazz) {
		T object = null;
		try {
			Constructor<T> constructor = clazz.getDeclaredConstructor();
			constructor.setAccessible(true);
			object = clazz.cast(constructor.newInstance());
		} catch (NoSuchMethodException | IllegalAccessException | 
				InstantiationException | InvocationTargetException e) {
			System.err.println("Unable to access constructor: " + e.getMessage());
			throw new RuntimeException("Unable to create object: " + e.getMessage(), e);
		} finally {
			try {
				clazz.getDeclaredConstructor().setAccessible(false);
			} catch (NoSuchMethodException e) {
				System.err.println("Unable to access constructor: " + e.getMessage());
			}
		}
		return object;
	}

	/**
	 * Sets the fields of an object from the values in a ResultSet based on the provided column-field mapping.
	 *
	 * @param <T>    the type of the object
	 * @param columnFieldMap    a map that maps column names to corresponding Field objects
	 * @param clazz    the class of the object
	 * @param object    the object to set the fields on
	 * @param rs    the ResultSet containing the values
	 * @return the object with the fields set
	 * @throws IllegalArgumentException if the field type is not supported
	 * @throws RuntimeException if unable to set the field
	 */
	public static <T> T setFieldsFromResultSet(Map<String, Field> columnFieldMap, Class<T> clazz, T object, ResultSet rs) {
		try {
			int i = 1;
			for (Field field : columnFieldMap.values()) {
				field.setAccessible(true);
				Object value;
				Class<?> type = field.getType();
				if (type == int.class || type == Integer.class) {
					value = rs.getInt(i);
				} else if (type == String.class) {
					value = rs.getString(i);
				} else if (type == boolean.class || type == Boolean.class) {
					value = rs.getBoolean(i);
				} else if (type == double.class || type == Double.class) {
					value = rs.getDouble(i);
				} else if (type == long.class || type == Long.class) {
					value = rs.getLong(i);
				} else if (type == float.class || type == Float.class) {
					value = rs.getFloat(i);
				} else if (type == LocalDate.class) {
					value = LocalDate.parse(
						rs.getString(i),
						DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				} else if (type == LocalTime.class) {
					value = LocalTime.parse(
						rs.getString(i),
						DateTimeFormatter.ofPattern("HH:mm:ss"));
				} else {
					field.setAccessible(false);
					throw new IllegalArgumentException("Unsupported field type: " + type.getName());
				}
				field.set(object, value);
				field.setAccessible(false);
				i++;
			}
		} catch (IllegalAccessException | SQLException e) {
			System.err.println("Unable to set field: " + e.getMessage());
			throw new RuntimeException("Unable to set field: " + e.getMessage(), e);
		}
		return object;
	}

	/**
	 * Checks if the given field is marked as a primary key.
	 *
	 * @param field the field to check
	 * @return true if the field is marked as a primary key, false otherwise
	 */
	public static boolean isPrimaryKey(Field field) {
		PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
		return primaryKeyAnnotation != null;
	}

	/**
	 * Retrieves the value of a field from an object.
	 *
	 * @param field the field to retrieve the value from
	 * @param object the object containing the field
	 * @return the value of the field
	 * @throws RuntimeException if unable to get the field value
	 */
	public static Object getFieldValue(Field field, Object object) {
		try {
			field.setAccessible(true);
			return field.get(object);
		} catch (IllegalAccessException e) {
			System.err.println("Unable to get field value: " + e.getMessage());
			throw new RuntimeException("Unable to get field value: " + e.getMessage(), e);
		} finally {
			field.setAccessible(false);
		}
	}

	/**
	 * Sets the value of a parameter in a PreparedStatement based on its type.
	 * 
	 * @param stmt the PreparedStatement object
	 * @param index the index of the parameter
	 * @param value the value to be set
	 * @throws SQLException if a database access error occurs
	 */
	public static void setPreparedStatementValue(PreparedStatement stmt, int index, Object value) throws SQLException {
		if (value == null) {
			stmt.setNull(index, java.sql.Types.NULL);
		} else if (value instanceof LocalDate) {
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
		} else if (value instanceof LocalTime) {
			stmt.setString(index, ((LocalTime) value).format(DateTimeFormatter.ofPattern("HH:mm:ss"))); 
		} else if (value instanceof String) {
			stmt.setString(index, (String) value);
		} else {
			System.err.println("Setting value with setObject for type: " + value.getClass().getName());
			stmt.setObject(index, value);
		}
	}

	/**
	 * Sets the values of a PreparedStatement based on the given column-field mapping and object.
	 *
	 * @param columnFieldMap a map containing the column-field mapping
	 * @param object the object from which to retrieve the field values
	 * @param ps the PreparedStatement to set the values on
	 * @throws RuntimeException if unable to set the prepared statement values
	 */
	public static void setPreparedStatementValueSet(Map<String, Field> columnFieldMap, Object object, PreparedStatement ps) {
		try {
			int i = 1;
			for (Field field : columnFieldMap.values()) {
				if(!DatabaseAnnotationUtils.isPrimaryKey(field)) {
					Object value = getFieldValue(field, object);
					setPreparedStatementValue(ps, i, value);
					i++;
				}
			}
		} catch (SQLException e) {
			System.err.println("Unable to set prepared statement values: " + e.getMessage());
			throw new RuntimeException("Unable to set prepared statement values: " + e.getMessage(), e);
		}
	}

	/**
	 * Generates an SQL INSERT query for the specified class.
	 *
	 * @param clazz the class for which the INSERT query is generated
	 * @return the generated INSERT query as a string
	 */
	public static String getInsertQuery(Class<?> clazz) {
		StringBuilder queryBuilder = new StringBuilder("INSERT INTO " + getTableName(clazz) + " (");
		StringBuilder valuesBuilder = new StringBuilder(") VALUES (");

		Map<String, Field> columnFieldMap = getColumnNamesAndFields(clazz);

		for (String columnName : columnFieldMap.keySet()) {
			if (!isPrimaryKey(columnFieldMap.get(columnName))) {
				queryBuilder.append(columnName).append(", ");
				valuesBuilder.append("?, ");
			}
		}

		// Remove the last comma and space
		queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());
		valuesBuilder.delete(valuesBuilder.length() - 2, valuesBuilder.length());

		return queryBuilder.append(valuesBuilder).append(")").toString();
	}

	/**
	 * Returns the update query for the specified class.
	 *
	 * @param clazz the class for which the update query is generated
	 * @return the update query as a string
	 */
	public static String getUpdateQuery(Class<?> clazz) {
		StringBuilder queryBuilder = new StringBuilder("UPDATE " + getTableName(clazz) + " SET ");

		Map<String, Field> columnFieldMap = getColumnNamesAndFields(clazz);

		for (String columnName : columnFieldMap.keySet()) {
			if (!isPrimaryKey(columnFieldMap.get(columnName))) {
				queryBuilder.append(columnName).append(" = ?, ");
			}
		}

		// Remove the last comma and space
		queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length());

		queryBuilder.append(" WHERE ").append(getPrimaryKey(clazz)).append(" = ?");

		return queryBuilder.toString();
	}

	/**
	 * Returns the delete query for the specified class.
	 *
	 * @param clazz the class for which the delete query is generated
	 * @return the delete query string
	 */
	public static String getDeleteQuery(Class<?> clazz) {
		return "DELETE FROM " + getTableName(clazz) + " WHERE " + getPrimaryKey(clazz) + " = ?";
	}
}
