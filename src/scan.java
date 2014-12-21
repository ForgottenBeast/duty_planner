import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;





public class scan {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		 try {
			Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:gardedb", "SA", "");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
