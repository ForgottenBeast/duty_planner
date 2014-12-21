import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File; 
import java.io.IOException;
import java.util.Date; 
import jxl.*; 
import jxl.read.biff.BiffException;




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
		 try {
			Workbook data = Workbook.getWorkbook(new File("data.xls"));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
