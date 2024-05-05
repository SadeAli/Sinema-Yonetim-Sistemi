import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetParser<T> {
	// Method to parse a ResultSet and construct an object
	T parse(ResultSet resultSet) throws SQLException;
}
