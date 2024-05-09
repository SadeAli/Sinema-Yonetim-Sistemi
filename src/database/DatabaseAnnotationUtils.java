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

public class DatabaseAnnotationUtils {

    public static String getTableName(Class<?> clazz) {
        TableName tableNameAnnotation = clazz.getAnnotation(TableName.class);
        if (tableNameAnnotation == null) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " does not have a TableName annotation");
        }
        return tableNameAnnotation.value();
    }

	public static String getColumnName(Field field) {
		ColumnName columnNameAnnotation = field.getAnnotation(ColumnName.class);
		if (columnNameAnnotation == null) {
			throw new IllegalArgumentException("Field " + field.getName() + " does not have a ColumnName annotation");
		}
		return columnNameAnnotation.value();
	}

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

	public static boolean isPrimaryKey(Field field) {
		PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
		return primaryKeyAnnotation != null;
	}

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
		} else {
			System.err.println("Setting value with setObject for type: " + value.getClass().getName());
			stmt.setObject(index, value);
		}
	}

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
}