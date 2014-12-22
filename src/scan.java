import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File; 
import java.io.IOException;
import java.util.Date; 

import jxl.*; 
import jxl.read.biff.BiffException;
import jxl.write.WritableWorkbook;




public class scan {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws SQLException, IOException {
		Connection c ;
		 try {
			 c = DriverManager.getConnection("jdbc:hsqldb:mem:gardedb", "SA", "");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Workbook data;
		 try {
			data = Workbook.getWorkbook(new File("data.xls"));
		} catch (BiffException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 WritableWorkbook workbook = Workbook.createWorkbook(new File("planning_garde.xls"));
	
		 Sheet sheet = data.getSheet(4);
		 boolean hasint = false;
		 for (int i = 1; i < 100; i++){
			 Cell cur = sheet.getCell(2,i);
			 if (!hasint){
				 hasint = cur.getCellFormat() != null;
			 }
		 }
		 Statement mystatement = c.createStatement();
		 ResultSet rs;
		 rs = mystatement.executeQuery("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO AUTONUMBER PRIMARY KEY, INTERIEUR BOOLEAN");
		 rs = mystatement.executeQuery("CREATE TABLE MEDECINS(NUMERO AUTONUMBER PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE DATE, NBGARDES INTEGER, NBLUNDI INTEGER, NBMARDI INTEGER, NBMERCREDI INTEGER, NBJEUDI  INTEGER, NBVENDREDI INTEGER, NBSAMEDI INTEGER, NBDIMANCHE INTEGER, FOREIGN KEY (SERVICE) REFERENCEs SERVICE(NUMERO), NBSEMESTRES INTEGER, NBFERIES INTEGER");
		 rs = mystatement.executeQuery("CREATE TABLE IMPOSSIBILITES(DATEDEBUT DATE, DATEFIN DATE, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO), CONSTRAINT ENTRY_ID PRIMARY KEY (DATEDEBUT,DATEFIN,NUMERO))");
		 rs = mystatement.executeQuery("CREATE TABLE JOURS_FERIES(JOUR DATE, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO), INTERIEUR BOOLEAN,CONSTRAINT ENTRY_ID(JOUR, INTERIEUR))");
		 if (!hasint){
			 rs = mystatement.executeQuery("CREATE TABLE GARDES(JOUR VARCHAR(20) primary key, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO))");
		 }
		 else {
			 rs = mystatement.executeQuery("CREATE TABLE GARDES(JOUR VARCHAR(20) DEFAULT '1970-01-01', FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (INTERIEUR)  MEDECINS (NUMERO))");
		 }
		  


	}

}
