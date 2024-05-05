import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
	private static final String SQLITE_JDBC_URL = "jdbc:sqlite:data/cinema_mecpine_fake.db";

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

	
}
