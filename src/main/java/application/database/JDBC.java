package application.database;

import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class JDBC {

	private static Connection connection = null;

	private static Statement statement = null;
	private static final String URL = "jdbc:postgresql://localhost/tech_park";
	private static final String USERNAME = "trubnikov";
	private static final String PASSWORD = "pass";


	static void getConnection() throws SQLException {

			connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
			System.out.println("Connected succesfully!");
			statement = connection.createStatement();
	}

	static void checkConnection() throws SQLException {
		if (connection == null) {
			getConnection();
		}
	}

	public static void beginTransaction() throws SQLException {
		checkConnection();
		statement.executeUpdate("BEGIN;");
	}

	public static void commitTransaction() throws SQLException {
		checkConnection();
		statement.executeUpdate("COMMIT;");
	}

	public static void executeReturnVoid(String query) throws SQLException {
		checkConnection();
		statement.executeUpdate(query);
	}

	public static ResultSet executeReturnSet(String query) throws SQLException {
		checkConnection();
		return statement.executeQuery(query);
	}

	public static boolean createUser() {
		try {
			checkConnection();
			java.util.Date date = new Date();
			String str = date.toString();
			System.out.println(str);
			statement.executeUpdate(
					"INSERT INTO users(nickname) VALUES ('" + str + "')"
			);
			return true;
		} catch (SQLException sqlException) {
			System.out.println("Fail connection: " + sqlException.getMessage());
			return false;
		}
	}
}
