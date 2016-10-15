import java.nio.charset.Charset;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.BufferedReader;
import java.io.File; 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date; 
import java.util.List;

import javax.swing.JOptionPane;



import org.joda.time.*;
import org.joda.time.DateTime;

import com.opencsv.CSVReader;

import jxl.*;
import jxl.read.biff.BiffException;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;




/**cette classe s'occupe de scanner le fichier excel pour récupérer les données utilisées pour la génération et créer le tableau de garde*/
public class scan {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws IOException 
	 * @throws BiffException 
	 * @throws WriteException 
	 * @throws RowsExceededException 
	 * @throws ParseException 
	 */
	public static void main(String[] args) throws SQLException, IOException, BiffException, RowsExceededException, WriteException, ParseException {
		Workbook data;
		 WritableWorkbook workbook;
		if(args[0].equals("--xls")){
			data = Workbook.getWorkbook(new File(args[1]));
			workbook = Workbook.createWorkbook(new File(args[2]+"_planning_garde.xls"));
		}
		else{
			reencode(args[0]);
			data = Workbook.getWorkbook(new File(args[0]+"_data.xls"));
			workbook = Workbook.createWorkbook(new File(args[0]+"_planning_garde.xls"));
		}
		Connection c ;
         c = DriverManager.getConnection("jdbc:hsqldb:mem:gardedb", "SA", "");
		 boolean hasint = setup(c,data);

		 filltables(c, data, hasint);

		 datepack monpack = new datepack(c,data,hasint);

		 writeoutput(monpack, c, workbook, hasint, data, args);
		
	}
	
	public static void reencode(String arg) throws IOException, RowsExceededException, WriteException, ParseException{
		//http://howtodoinjava.com/2014/08/12/parse-read-write-csv-files-opencsv-tutorial/

		
		
		
		String fpath = arg;
		WritableWorkbook workbook = Workbook.createWorkbook(new File(arg+"_data.xls"));
int k = 1;

		Label l; 
		 Calendar cal = Calendar.getInstance();
	 CSVReader reader = new CSVReader(new FileReader(fpath));
	     List<String[]> myEntries = reader.readAll();
	     WritableSheet ms = workbook.createSheet("medecins",0);
	     ;
	     WritableCell dt;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		WritableCellFormat cf1=new WritableCellFormat(DateFormats.FORMAT9);
	     for(int i = 0; i < myEntries.size();i++){
	    	
	    	 if(myEntries.get(i)[0].equals("<medecins>")){
	    	
	    		 l = new Label(0,0,"medecin");
	    		 ms.addCell(l);
	    		;
	    		 l = new Label(1,0,"service");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(2,0,"derniere garde");
	    		 ms.addCell(l);
	    		 ;
	    		 for(int j = i+1; j < myEntries.size();j+=3){
	    			 if(myEntries.get(j)[0].equals("</medecins>")){
	    				 i = j;
	    				 k = 1;
	    				
	    				 break;
	    			 }
	    			 else{
	    	
	    				 l = new Label(0,k,myEntries.get(j)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				l = new Label(1,k,myEntries.get(j+1)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 cal.setTime(formatter.parse(myEntries.get(j+2)[0]));
	    					dt = new jxl.write.DateTime(2,k,new Date(cal.getTimeInMillis()),cf1);
	    					ms.addCell(dt);	
	    
	    				 ;
	    				 k++;
	    			 }
	    		 }
	    		 continue;
	    	 }
	    	 else if(myEntries.get(i)[0].equals("<feries>")){
	   
	    		 ms = workbook.createSheet("jours feries", 1);
	    		 l = new Label(0,0,"date");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(1,0,"nom");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(2,0,"interieur");
	    		 ms.addCell(l);
	    		 for(int j = i+1; j < myEntries.size();j+=3){
	    			 if(myEntries.get(j)[0].equals("</feries>")){
	    				 i = j;
	    				 k = 1;
	    				
	    				 break;
	    			 }
	    			 else{
	    				 cal.setTime(formatter.parse(myEntries.get(j)[0]));
	    					dt = new jxl.write.DateTime(0,k,new Date(cal.getTimeInMillis()),cf1);
	    					ms.addCell(dt);	
	    				 ;
	    				 l = new Label(1,k,myEntries.get(j+1)[0]);
	    				 ms.addCell(l);
	    				 if(myEntries.get(j+2)[0].equals("true")){
	    					 l = new Label(2,k,myEntries.get(j+2)[0]);
	    					 ms.addCell(l);
	    				 }
	    			
	    				 ;
	    				 k++;
	    				 
	    			 }
	    		 }
	    		 continue;
	    	 }
	    	 else if(myEntries.get(i)[0].equals("<vacances>")){
	    		 ms = workbook.createSheet("vacances",2);
	    		 l = new Label(0,0,"date debut");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(1,0,"date fin");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(2,0,"nom");
	    		 ms.addCell(l);
	    		 ;
	    		 for(int j = i+1; j < myEntries.size();j+=3){
	    			 if(myEntries.get(j)[0].equals("</vacances>")){
	    				 i = j;
	    				 k = 1;
	    			
	    				 break;
	    			 }
	    			 else{
	    				 cal.setTime(formatter.parse(myEntries.get(j)[0]));
	    					dt = new jxl.write.DateTime(0,k,new Date(cal.getTimeInMillis()),cf1);
	    					ms.addCell(dt);	
	    				 ;
	    				 cal.setTime(formatter.parse(myEntries.get(j+1)[0]));
	    					dt = new jxl.write.DateTime(1,k,new Date(cal.getTimeInMillis()),cf1);
	    					ms.addCell(dt);	
	    				 l = new Label(2,k,(String) myEntries.get(j+2)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 k++;
	    			 }
	    		 }
	    		 continue;
	    	 }
	    	 else if(myEntries.get(i)[0].equals("<info>")){
	    		 ms = workbook.createSheet("informations generales", 3);
	    		 l = new Label(0,0,"date debut");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(1,0,"date fin");
	    		 ms.addCell(l);
	    		 ;
	    		 i++;
	    		 cal.setTime(formatter.parse(myEntries.get(i)[0]));
					dt = new jxl.write.DateTime(0,1,new Date(cal.getTimeInMillis()),cf1);
					ms.addCell(dt);	
	    		 ;
	    		 i++;
	    		 cal.setTime(formatter.parse(myEntries.get(i)[0]));
					dt = new jxl.write.DateTime(1,1,new Date(cal.getTimeInMillis()),cf1);
					ms.addCell(dt);	
	    		 ;
	    		 continue;
	    	 }
	    	 else if(myEntries.get(i)[0].equals("<services>")){
	    		 ms = workbook.createSheet("services", 4);
	    		 l = new Label(0,0,"nom");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(1,0,"interieur");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(2,0,"repos");
	    		 ms.addCell(l);
	    		 ;
	    		 for(int j = i+1; j < myEntries.size();j+=3){
	    			 if(myEntries.get(j)[0].equals("</services>")){
	    				 i = j;
	    				 k = 1;
	    				
	    				 break;
	    			 }
	    			 else{
	    				 l = new Label(0,k,myEntries.get(j)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 if(myEntries.get(j+1)[0].equals("true")){
	    					l = new Label(1,k,myEntries.get(j+1)[0]);
	    				 	ms.addCell(l);
	    				 }
	    				 ;
	    				 l = new Label(2,k,(String) myEntries.get(j+2)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 k++;
	    			 }
	    		 }
	    		 continue;
	    	 }
	    	 else if(myEntries.get(i)[0].equals("<options>")){
	    		
	    		 ms = workbook.createSheet("options", 5);
	    		 l = new Label(0,0,"nom");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(1,0,"nbgardestotal");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(2,0,"nblundi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(3,0,"nbmardi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(4,0,"nbmercredi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(5,0,"nbjeudi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(6,0,"nbvendredi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(7,0,"nbsamedi");
	    		 ms.addCell(l);
	    		 ;
	    		 l = new Label(8,0,"nbdimanche");
	    		 ms.addCell(l);
	    		 ;

	    		 for(int j = i+1; j < myEntries.size();j+=9){
	    			 if(myEntries.get(j)[0].equals("</options>")){
	    				 i = j;
	    				 k = 1;
	    		
	    				 break;
	    			 }
	    			 else{
	
	    				 l = new Label(0,k,(String) myEntries.get(j)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(1,k,(String) myEntries.get(j+1)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(2,k,(String) myEntries.get(j+2)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(3,k,(String) myEntries.get(j+3)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(4,k,(String) myEntries.get(j+4)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(5,k,(String) myEntries.get(j+5)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(6,k,(String) myEntries.get(j+6)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(7,k,(String) myEntries.get(j+7)[0]);
	    				 ms.addCell(l);
	    				 ;
	    				 l = new Label(8,k,(String) myEntries.get(j+8)[0]);
	    				 ms.addCell(l);
	    				 ;

	    				 k++;
	    			 }
	    		 }
	    		 continue;
	    	 }
	    	
	     }
	     workbook.write();
	     workbook.close();
	    
	}
	
	/** methode qui remplis la base de données avec les tables nécessaires*/
	public static boolean  setup(Connection c, Workbook data) throws SQLException	{
		Sheet sheet = data.getSheet(4);
		boolean hasint = false;///< est ce que le tableau qu'on génère peut avoir des medecins de garde d'intérieur
		 Cell cur;
		 for (int i = 1; i < sheet.getRows(); i++){
			 cur = sheet.getCell(1,i);
			 if (!hasint){
				 hasint = cur.getCellFormat() != null && cur.getContents().length() != 0;
			 }
		 }

		Statement mystatement = c.createStatement();
		 int rs;
		 rs = mystatement.executeUpdate("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO INTEGER GENERATED BY DEFAULT " +
                 "AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, INTERIEUR BOOLEAN, REPOS INTEGER DEFAULT 0)");
                 ///<crée la table services, chaque nom a un id utilisé partout ailleurs, un integer


		 rs = mystatement.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT " +
                 "AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), " +
                 "DERNIEREGARDE Date default '1970-01-01', NBGARDES INTEGER, NBLUNDI INTEGER default 0, " +
                 "NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, " +
                 "NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0," +
                 " SERVICE INTEGER, NBSAMEDI_EQUILIBRE BOOLEAN DEFAULT FALSE,NBJEUDI_EQUILIBRE BOOLEAN DEFAULT FALSE," +
                 " NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");
                 ///< crée la table des medecins avec nom, id et nombre de gardes


		 rs = mystatement.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT Date, DATEFIN date, NUMERO INTEGER, " +
                 "FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO),PRIMARY KEY(DATEDEBUT, DATEFIN,NUMERO))");
                 ///< table des vacances


		 rs = mystatement.executeUpdate("CREATE TABLE OPTIONS(NUMERO INTEGER PRIMARY KEY, NBTOTAL INTEGER," +
                 " NBLUNDI INTEGER, NBMARDI INTEGER, NBMERCREDI INTEGER, NBJEUDI INTEGER, NBVENDREDI INTEGER, " +
                 "NBSAMEDI INTEGER, NBDIMANCHE INTEGER, NBFERIES INTEGER)");

		 rs = mystatement.executeUpdate("CREATE TABLE ATTRIBUES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, " +
                 "FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),PRIMARY KEY(JOUR, INTERIEUR))");

		 if(hasint){
		 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, " +
                 "FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");
                 ///<table des jours fériés exigés
		 }
		 else{
			 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR date primary key, NUMERO INTEGER, " +
                     "FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO))");
		 }
		 if (!hasint){
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date primary key,URGENCES INTEGER, " +
                     "FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO),MANUALLY_SET BOOLEAN default FALSE)");
		 }
		 else {
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date PRIMARY KEY,URGENCES INTEGER," +
                     "INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), " +
                     "FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO),MANUALLY_SET BOOLEAN default FALSE," +
                     "MANUALLY_SETINT BOOLEAN DEFAULT FALSE)");///<tableau de garde final
		 }
		 return hasint;
	}



///cette méthode remplis les tables de la base de données à partir du fichier excel donné en entrée
 public static void filltables(Connection c, Workbook data,boolean hasint) throws SQLException, ParseException {
	 Statement mystatement = c.createStatement();
	 Statement ms2 = c.createStatement();
     Statement ms3 = c.createStatement();
	 Calendar cal = Calendar.getInstance();
	 java.sql.Date d1,d2;
	 int rs;
	 DateCell dc1,dc2;
	 Sheet sheet;
	 boolean mbool;
	 ResultSet rs2,rs3;
	 sheet = data.getSheet(4);
	/* remplissage de la table de services*/
	for (int i = 1; i < sheet.getRows();i++){
		if(sheet.getCell(1,i).getCellFormat() != null && sheet.getCell(1,i).getContents().length() != 0){
            try {
                rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR,REPOS) VALUES('" + sheet.getCell(0, i).getContents() + "',TRUE," + sheet.getCell(2, i).getContents() + ")");
            }
            catch(SQLException ex){
                System.out.println("caught "+ex.getMessage());
            }
		}
		else{
            try {
                rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR,REPOS) VALUES('" + sheet.getCell(0, i).getContents() + "',FALSE," + sheet.getCell(2, i).getContents() + ")");
            }
            catch(SQLException ex){
                System.out.println("caught "+ex.getMessage());
            }
		}
	}
	
	sheet = data.getSheet(0);
	int nservice;
	for (int i = 1; i < sheet.getRows();i++) {
		rs2 = ms2.executeQuery("SELECT NUMERO FROM SERVICES WHERE NOM = '".concat(sheet.getCell(1,i).getContents()).concat("'"));
		while(rs2.next()){
		nservice = rs2.getInt("NUMERO");
		if(sheet.getCell(2,i).getCellFormat() != null && sheet.getCell(2,i).getContents().length() != 0){
			dc1 = (DateCell) sheet.getCell(2,i);
			cal.setTime(dc1.getDate());
			d1 = new java.sql.Date(cal.getTimeInMillis());
            try {
                rs = mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE,DERNIEREGARDE) VALUES('".concat(sheet.getCell(0, i).getContents()).concat("',").concat(Integer.toString(nservice)) + ",'" + d1 + "')");
            }
            catch(SQLException ex){
                System.out.println("caught "+ex.getMessage());
            }
		}
		else{
            try {
                rs = mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE) VALUES('".concat(sheet.getCell(0, i).getContents()).concat("',").concat(Integer.toString(nservice)) + ")");
            }
            catch(SQLException ex){
                System.out.println("caught "+ex.getMessage());
            }
		}
		}
			}
	
	sheet = data.getSheet(2);
	for (int i = 1; i < sheet.getRows();i++){
		rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '".concat(sheet.getCell(2,i).getContents()).concat("'"));
while(rs2.next()){
	int nmedecin = rs2.getInt("NUMERO");
	dc1 = (DateCell) sheet.getCell(0,i);
	dc2 = (DateCell) sheet.getCell(1,i);
	
	 
	cal.setTime(dc1.getDate());
	d1 = new java.sql.Date(cal.getTimeInMillis());
	cal.setTime(dc2.getDate());
	d2 = new java.sql.Date(cal.getTimeInMillis());
	d2 = nextday(d2); //inclus le dernier jour des vacances
    try {
        rs = mystatement.executeUpdate("INSERT INTO IMPOSSIBILITES(DATEDEBUT,DATEFIN,NUMERO) VALUES('" + d1 + "','" + d2 + "',".concat(Integer.toString(nmedecin)).concat(")"));
    }
    catch(SQLException myex){
        rs3 = ms3.executeQuery("SELECT NOM FROM MEDECINS WHERE NUMERO ="+Integer.toString(nmedecin));
        String nom ="pas trouvé";
        while(rs3.next()){
            nom = rs3.getString("NOM");
        }
        System.out.println ("got exception with "+d1+" fin "+d2+" medecin= "+nom);

    }
}
}
	
	sheet = data.getSheet(1);
	for (int i = 1; i < sheet.getRows();i++){
	
		if(sheet.getCell(0,i).getCellFormat() != null && sheet.getCell(0,i).getContents().length() != 0){
		DateCell dc = (DateCell)sheet.getCell(0,i);
		dc = (DateCell) sheet.getCell(0,i);
		cal.setTime(dc.getDate());
		d1 = new java.sql.Date(cal.getTimeInMillis());
		
		if(sheet.getCell(1,i).getCellFormat() != null && sheet.getCell(1,i).getContents().length() != 0){
		rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '".concat(sheet.getCell(1,i).getContents()).concat("'"));
		while(rs2.next()){
		if(hasint){
			mbool = sheet.getCell(2, i).getCellFormat() != null && sheet.getCell(2,i).getContents().length() != 0;
		int nmedecin = rs2.getInt("NUMERO");
		
		if(mbool){
			
		rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES ('"+d1+"',".concat(Integer.toString(nmedecin).concat(",").concat("TRUE").concat(")")));
		}
		else{
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES ('"+d1+"',".concat(Integer.toString(nmedecin)).concat(",").concat("FALSE").concat(")"));

		}

		}
		else{
			int nmedecin = rs2.getInt("NUMERO");
			cal.setTime(dc.getDate());
			d1 = new java.sql.Date(cal.getTimeInMillis());
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO) VALUES ('"+d1+"',".concat(Integer.toString(nmedecin)).concat(")"));
		}
		}
		}
		else{
			if(hasint){
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES('"+d1+"',TRUE)");
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES('"+d1+"',FALSE)");
			}
			else{
				rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR) VALUES('"+d1+"')");
			}
			}
		}
	}
	sheet = data.getSheet(5);
	String nom;
	ResultSet monset;
	int id = -1;
	String nblundi,nbmardi,nbmercredi,nbjeudi,nbvendredi,nbsamedi,nbdimanche,nbferies,nbtotal;
	for(int i = 1; i < sheet.getRows();i++){
		if(sheet.getCell(0,i).getCellFormat() != null && sheet.getCell(0,i).getContents().length() != 0){
			nom = sheet.getCell(0, i).getContents();
			monset = mystatement.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '"+nom+"'");
			while(monset.next()){
				id = monset.getInt("NUMERO");
			}
			if(id != -1){
			nbtotal = sheet.getCell(1,i).getContents();
			nblundi = sheet.getCell(2,i).getContents();
			nbmardi = sheet.getCell(3,i).getContents();
			nbmercredi = sheet.getCell(4,i).getContents();
			nbjeudi = sheet.getCell(5,i).getContents();
			nbvendredi = sheet.getCell(6,i).getContents();
			nbsamedi = sheet.getCell(7,i).getContents();
			nbdimanche = sheet.getCell(8,i).getContents();

			
			rs = mystatement.executeUpdate("INSERT INTO OPTIONS(NUMERO,NBTOTAL,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE) VALUES("+Integer.toString(id)+","+nbtotal+","+nblundi+","+nbmardi+","+nbmercredi+","+nbjeudi+","+nbvendredi+","+nbsamedi+","+nbdimanche+")");
			}
			}
		}
	}

 

/** translator function from Date format to sqldate format*/
 public static java.sql.Date tosql(Date curdat){
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(curdat);
	 return new java.sql.Date(cal.getTimeInMillis());
	 
 }
 
 public static java.sql.Date nextday(java.sql.Date curdat){
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(curdat);
	 cal.add(Calendar.DATE, 1);
	 return new java.sql.Date(cal.getTimeInMillis());
 }
 
 public static java.sql.Date prevday(java.sql.Date curdat){
	 DateTime ladate = new DateTime( curdat, DateTimeZone.UTC );;
	 ladate = ladate.minusDays(1);
	 return new java.sql.Date(ladate.getMillis());
 }
 
 public static boolean dateferiee(java.sql.Date madate, Connection c) throws SQLException{
	 Statement ms = c.createStatement();
	 ResultSet rs = ms.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE JOUR='" + madate + "'");
	 while(rs.next()){
		 return true;
	 }
	 return false;
 }
 
/** method to select a person for a holiday*/

/**boolean, check if someone can actually take the shift without ruining the fun for everyone else*/
 

 
/**boolean, est ce que cette garde a été déja réservée par quelqu'un (jour férié ou autre)*/
 public static boolean isreserved(boolean hasint,boolean interieur,Connection c,java.sql.Date madate) throws SQLException{
	 boolean itis = false;
	 Statement ms = c.createStatement();
	 ResultSet rs2;
	 if(hasint){
		 
	 rs2 = ms.executeQuery("SELECT JF.NUMERO FROM (JOURS_FERIES AS JF INNER JOIN MEDECINS AS M ON M.NUMERO = JF.NUMERO) INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO AND JF.INTERIEUR = "+interieur+" WHERE JOUR = '"+madate+"' AND INTERIEUR = "+interieur);
	 }
	 else{
		 rs2 = ms.executeQuery("SELECT NUMERO FROM JOURS_FERIES WHERE JOUR = '"+madate+"'"); 
	 }
	 while(rs2.next()){
		 itis = true;
	 }
	
	 return itis;
 }
 
 
/**renvoie un médecin pouvant prendre la garde*/

/** returns the day of week for some date, to fill the database columns with numbers of each day taken by someone*/
 public static String getdow(Date curdat){
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(curdat);
	 int dow = cal.get(Calendar.DAY_OF_WEEK);
	 switch(dow){
	 case 1:
		 return "NBDIMANCHE";
	 case 2:
		 return "NBLUNDI";
	 case 3:
		 return "NBMARDI";
	 case 4:
		 return "NBMERCREDI";
	 case 5:
		 return "NBJEUDI";
	 case 6:
		 return "NBVENDREDI";
	 default:
		 return "NBSAMEDI";
	 }
 }


/**final database recording function, save the shift attribution in the db*/
public static void dorecord(datepack monpack,Connection c,boolean interieur,boolean hasint) throws SQLException, ParseException{
	Statement ms = c.createStatement();
	java.sql.Date sqldate = monpack.garde.jour;
	 int rs = ms.executeUpdate("UPDATE MEDECINS set DERNIEREGARDE = '"+monpack.garde.jour+"' WHERE NUMERO = ".concat(Integer.toString(monpack.garde.nmed)));
	 if(monpack.garde.ferie||dateferiee(monpack.garde.jour,c)){
		 int newf = monpack.garde.nbferies + 1;
		 rs = ms.executeUpdate("UPDATE MEDECINS SET NBFERIES = "+newf+" WHERE NUMERO = "+monpack.garde.nmed);
	 }
	 rs = ms.executeUpdate("update MEDECINS set ".concat(monpack.garde.dowtoinc).concat(" = ").concat(Integer.toString(monpack.garde.newdowcount)).concat("where NUMERO = ").concat(Integer.toString(monpack.garde.nmed)));
	 rs=ms.executeUpdate("UPDATE MEDECINS set NBGARDES = ".concat(Integer.toString(monpack.garde.curgarde)).concat("WHERE NUMERO = ").concat(Integer.toString(monpack.garde.nmed)));
	 if(hasint){
		 if(!interieur){
			 if(!monpack.garde.ferie){
				 rs = ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(monpack.garde.nmed))+" WHERE JOUR = '"+sqldate+"'");
			 }
			 else{
				 rs = ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(monpack.garde.nmed))+", MANUALLY_SET = TRUE WHERE JOUR = '"+sqldate+"'");
			 }
	 	}
	 	else{
	 		if(!monpack.garde.ferie){
	 			rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR,MANUALLY_SETINT) VALUES('"+sqldate+"',".concat(Integer.toString(monpack.garde.nmed)).concat(",TRUE)"));
	 		}
	 		else{
	 			rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR) VALUES('"+sqldate+"',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
	 		}
	 	}
	 }
	 else{
		 if(!monpack.garde.ferie){
             try {
                 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES) VALUES('" + sqldate + "',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
             }
             catch(SQLException ex){
                 System.out.println("constraint violation, insertion garde  le "+sqldate);
             }
		 }
		 else{
			 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES,MANUALLY_SET) VALUES('"+sqldate+"',".concat(Integer.toString(monpack.garde.nmed)).concat(",TRUE)"));
		 }
	 }
 }

/**function to write to the output excel file
 * @throws BiffException */
public static void writeoutput(datepack monpack,Connection c, WritableWorkbook output,boolean hasint,Workbook data,String[] arg) throws SQLException, RowsExceededException, WriteException, IOException, BiffException{
	writegardes(monpack,c,output,hasint,data);
	writestats(c,output,hasint);
	writegps(c, output, hasint);
	writecalendar(c, output, hasint);
	if(arg[0].equals("--xls")){
		updatedata(c,arg[1]);
	}
	else{
		updatedata(c,arg[0]+"_data.xls");
	}
	output.write();	
	output.close();
}

/**write shift by department to the output excel file*/
public static void writegps(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException{
	WritableSheet ms = output.createSheet("GPS", 2);
	Statement mst = c.createStatement();
	int i = 1;
	Label l1,l2,l3;
	l1 = new Label(0,0,"Date");
	l2 = new Label(1,0,"Urgences");
	if(hasint){
	l3 = new Label(2,0,"Interieur");
	ms.addCell(l3);
	}
	ms.addCell(l1);
	ms.addCell(l2);
	ResultSet rs;
	if(hasint){
	    rs = mst.executeQuery("SELECT G.JOUR AS JOUR, S.NOM AS S1,s2.NOM AS S2N FROM(((GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO)INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO)INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO)INNER JOIN SERVICES AS S2 ON M2.SERVICE = S2.NUMERO");
	}
	else{
		rs = mst.executeQuery("SELECT G.JOUR AS JOUR, S.NOM AS S1 FROM (GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO)INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO");
	}
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("JOUR"));
		l2 = new Label(1,i,rs.getString("S1"));
		if(hasint){
		l3 = new Label(2,i,rs.getString("S2N"));
		ms.addCell(l3);
		}
		ms.addCell(l1);
		ms.addCell(l2);
		i++;
	}
}

/**write the shift planning to output excel file*/
public static void writegardes(datepack monpack,Connection c, WritableWorkbook output,boolean hasint,Workbook data) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("planning", 0);
	Statement mst = c.createStatement();
	ResultSet rs;
	if(hasint){
		rs = mst.executeQuery("SELECT G.JOUR AS JOUR, M.NOM AS M1, M2.NOM AS M2N FROM (GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO ) INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO ORDER BY JOUR ASC");
	}
	else{
		rs = mst.executeQuery("SELECT G.JOUR AS JOUR, M.NOM AS M1 FROM GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO ");
	}
	Label l1,l2,l3;
	int i = 1;
	l1 = new Label(0,0,"date");
	l2 = new Label(1,0,"urgence");
	if(hasint){
		l3 = new Label(2,0,"interieur");
		ms.addCell(l3);
	}
	ms.addCell(l1);
	ms.addCell(l2);
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("JOUR"));
		l2 = new Label(1,i,rs.getString("M1"));
		if(hasint){
			l3 = new Label(2,i,rs.getString("M2N"));
			ms.addCell(l3);
		}
		ms.addCell(l1);
		ms.addCell(l2);
		i++;
	}
	if(monpack.upto.before(monpack.goal)){
		l1 = new Label(0,i+1,"le tableau de garde n'a pas put être généré jusqu'au bout, voir erreur ci dessous");
		l2 = new Label(0,i+2,monpack.error);
		int target_s = 0;
		String target_nom = "";
		int target_repos = 0;
        if(monpack.nbdays != 0) {
            l2.setString(l2.getString()+" nombre max de jours de repos au moment de l'erreur: "+monpack.nbdays+"\n"+
                "nombre max de jours de repos avant le prochain jour férié: "+monpack.daysbf+"\n"+
                "vous pouvez tenter de générer un nouveau tableau de gardes avec un nombre de jours de repos inférieur à  " +
                            monpack.nbdays);
            System.out.println("printing error:\n"+l2.getString());
        }
		ms.addCell(l1);
		ms.addCell(l2);
	}
}

/**write statistics page to output excel file*/
public static void writestats(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("stats", 1);
	Statement mst = c.createStatement();
	ResultSet rs = mst.executeQuery("SELECT M.NOM AS NOM, M.NBGARDES AS NBGARDES, M.NBFERIES AS NBFERIES,M.NBLUNDI AS NBLUNDI,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI, M.NBJEUDI AS NBJEUDI, M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE, S.NOM AS service FROM (MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) order by SERVICE ASC,NOM ASC");
	Label l1,l2,l3,l4,l5,l6,l7,l8,l9,l10,l11,l12;
	l1 = new Label(0,0,"nom");
	l2 = new Label(1,0,"options");
	l3 = new Label(2,0,"nbgardes");
	l4 = new Label(3,0,"service");
	l5 = new Label(4,0,"nbferies");
	l6 = new Label(5,0,"nblundi");
	l7 = new Label(6,0,"nbmardi");
	l8 = new Label(7,0,"nbmercredi");
	l9 = new Label(8,0,"nbjeudi");
	l10 = new Label(9,0,"nbvendredi");
	l11 = new Label(10,0,"nbsamedi");
	l12 = new Label(11,0,"nbdimanche");
	ms.addCell(l1);
	ms.addCell(l2);
	ms.addCell(l3);
	ms.addCell(l4);
	ms.addCell(l5);
	ms.addCell(l6);
	ms.addCell(l7);
	ms.addCell(l8);
	ms.addCell(l9);
	ms.addCell(l10);
	ms.addCell(l11);
	ms.addCell(l12);
	int i = 1;
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("NOM"));
		l3 = new Label(2,i,Integer.toString(rs.getInt("NBGARDES")));
		l4 = new Label(3,i,rs.getString("service"));
		l5 = new Label(4,i,Integer.toString(rs.getInt("NBFERIES")));
		l6 = new Label(5,i,Integer.toString(rs.getInt("NBLUNDI")));
		l7 = new Label(6,i,Integer.toString(rs.getInt("NBMARDI")));
		l8 = new Label(7,i,Integer.toString(rs.getInt("NBMERCREDI")));
		l9 = new Label(8,i,Integer.toString(rs.getInt("NBJEUDI")));
		l10 = new Label(9,i,Integer.toString(rs.getInt("NBVENDREDI")));
		l11 = new Label(10,i,Integer.toString(rs.getInt("NBSAMEDI")));
		l12 = new Label(11,i,Integer.toString(rs.getInt("NBDIMANCHE")));
		ms.addCell(l1);
		ms.addCell(l3);
		ms.addCell(l4);
		ms.addCell(l5);
		ms.addCell(l6);
		ms.addCell(l7);
		ms.addCell(l8);
		ms.addCell(l9);
		ms.addCell(l10);
		ms.addCell(l11);
		ms.addCell(l12);
		i++;
	}

}

/**write the final calendar to excel file*/
public static void writecalendar(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException{
	Statement mst = c.createStatement();
	Statement ms1 = c.createStatement();
	ResultSet rs2;
	ResultSet rs = mst.executeQuery("SELECT NOM,NUMERO FROM MEDECINS ORDER BY NOM");
	int i = 3;
	int j;
	while(rs.next()){
		j = 0;
		WritableSheet ms = output.createSheet(rs.getString("NOM"), i);
		Label l1,l2;
		if(hasint){
		rs2 = ms1.executeQuery("SELECT JOUR,URGENCES,INTERIEUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" OR INTERIEUR = "+rs.getInt("NUMERO"));
		}
		else{
			rs2 = ms1.executeQuery("SELECT JOUR,URGENCES FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO"));
		}
		while (rs2.next()){
			l1 = new Label(0,j,rs2.getString("JOUR"));
			if(hasint){
			if(rs2.getInt("INTERIEUR")==rs.getInt("NUMERO")){
				l2 = new Label(1,j,"interieur");
			}
			else{
				l2 = new Label(1,j,"urgences");
			}
			ms.addCell(l2);
			}
			ms.addCell(l1);
			
			j++;
		}
		i++;
	}
}

/**update the input excel file with informations such as last shift done
 * @throws BiffException */
public static void updatedata(Connection c, String filename) throws SQLException, IOException, RowsExceededException, WriteException, BiffException{
	Statement ms = c.createStatement();
	ResultSet rs;
	Workbook data = Workbook.getWorkbook(new File(filename));
	WritableWorkbook data2 = Workbook.createWorkbook(new File(filename),data);
	WritableSheet mst = data2.getSheet(0);
	String nom;
	Calendar cal = Calendar.getInstance();
	WritableCellFormat cf1=new WritableCellFormat(DateFormats.FORMAT9);
	for(int i = 1; i < mst.getRows();i++){
		nom = mst.getCell(0,i).getContents();
		rs = ms.executeQuery("SELECT DERNIEREGARDE FROM MEDECINS WHERE NOM = '"+nom+"'");
		while(rs.next()){
			cal.setTime(rs.getDate("DERNIEREGARDE"));
			WritableCell dt = new jxl.write.DateTime(2,i,new Date(cal.getTimeInMillis()),cf1);
			mst.addCell(dt);	
		}
	}
	data2.write();
	data2.close();
	data.close();
	
}

/**echange deux gardes a partir des dates
 * @throws SQLException */
public static void swap(Connection c, java.sql.Date d1,java.sql.Date d2,boolean interieur) throws SQLException{
	int med1 = 0,med2 =0;
	Statement ms = c.createStatement(),ms2 = c.createStatement(),ms3 = c.createStatement(),ms4= c.createStatement();
	int ru;
	ResultSet rs,rs2,rs3,rs4;
	if(interieur){
		rs = ms.executeQuery("SELECT INTERIEUR as T1 FROM GARDES WHERE JOUR = '"+d1+"'");
		rs2 = ms2.executeQuery("SELECT INTERIEUR as T2 FROM GARDES WHERE JOUR = '"+d2+"'");
	}
	else{
		rs = ms.executeQuery("SELECT URGENCES as T1 FROM GARDES WHERE JOUR = '"+d1+"'");
		rs2 = ms2.executeQuery("SELECT URGENCES as T2 FROM GARDES WHERE JOUR = '"+d2+"'");
	}
	
	while(rs.next()&&rs2.next()){
		med1 = rs.getInt("T1");
		med2 = rs2.getInt("T2");
	}
	if(interieur){
		ru = ms.executeUpdate("UPDATE GARDES SET INTERIEUR  = "+Integer.toString(med2)+" WHERE JOUR = '"+d1+"'");
		ru = ms.executeUpdate("UPDATE GARDEES SET INTERIEUR = "+Integer.toString(med1)+" WHERE JOUR = "+d2+"'");
	}
	else{
		ru = ms.executeUpdate("UPDATE GARDES SET URGENCES  = "+Integer.toString(med2)+" WHERE JOUR = '"+d1+"'");
		ru = ms.executeUpdate("UPDATE GARDEES SET URGENCES = "+Integer.toString(med1)+" WHERE JOUR = "+d2+"'");
	}
	rs = ms.executeQuery("SELECT JOUR FROM GARDES WHERE INTERIEUR = "+med1+" OR URGENCES = "+med1+" ORDER BY JOUR DESC LIMIT 1");
	while(rs.next()){
		ru = ms2.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs.getDate("JOUR")+"' WHERE NUMERO = "+med1);
	}
	rs = ms.executeQuery("SELECT JOUR FROM GARDES WHERE INTERIEUR = "+med2+" OR URGENCES = "+med2+" ORDER BY JOUR DESC LIMIT 1");
	while(rs.next()){
		ru = ms2.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs.getDate("JOUR")+"' WHERE NUMERO = "+med1);
	}
}

/** equilibre le tableau de garde de la manière la plus juste possible en prenant en compte le poids des jours
 * @throws SQLException 
 * @throws ParseException */
public static void equilibrer(Connection c,boolean interieur) throws SQLException, ParseException{
	Statement ms = c.createStatement(),ms2 = c.createStatement(),ms8 = c.createStatement(),ms4 = c.createStatement(),ms5 = c.createStatement(),m6 = c.createStatement(),m7 = c.createStatement();
	ResultSet rs,rs2,rs8,rs4,rs5,rs6,rs7;
	int curg = 0,nbmeds = 0,prevint = 666,prevurg = 0,nbjeudi = 0,nbvendredi = 0,nbsamedi = 0,nbdimanche = 0;
	gtg isgood = null; 
	boolean done = false;
	String secteur = "URGENCES";
	int action,max = 0,min = 0,totgardes = 0,nbjour = 0;
	rs = ms.executeQuery("SELECT COUNT(M.NUMERO) as nbmeds,SUM(M.NBVENDREDI) as allvend,SUM(M.NBDIMANCHE) as alldim, MAX(M.NBGARDES) as MAXG,MIN(M.NBGARDES) as MING,SUM(M.NBSAMEDI) as ALLSAMS,SUM(M.NBJEUDI) as allthu,SUM(M.NBGARDES) as TOTGARDES FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND S.INTERIEUR = "+interieur);
	while(rs.next()){
		max = rs.getInt("MAXG");
		min = rs.getInt("MING");
		nbsamedi = rs.getInt("ALLSAMS");
		nbvendredi = rs.getInt("allvend");
		nbdimanche = rs.getInt("alldim");
		nbjeudi = rs.getInt("allthu");
		totgardes = rs.getInt("TOTGARDES");
		nbmeds = rs.getInt("nbmeds");
		}
	int calcval;
	String dowtoinc,curdow;
	for(int i = 0; i < 2; i++){
	 if(i==0){
		 dowtoinc = "NBSAMEDI";
		 calcval = nbsamedi/nbmeds;

	 }
	 else{
		 dowtoinc = "NBJEUDI";
		 calcval = nbjeudi/nbmeds;

	 }
	 if(calcval == 0){
		 calcval = 1;
	 }

	 if(i == 0){
			 rs2 = ms2.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES as NBGARDES,M.NOM AS NOM,M.NBLUNDI AS NBLUNDI,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICE FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M."+dowtoinc+" < "+Integer.toString(calcval)+" and M.NBSAMEDI_EQUILIBRE = FALSE and S.INTERIEUR = "+interieur);
	 }
	 else{
		 rs2 = ms2.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES as NBGARDES,M.NOM AS NOM,M.NBLUNDI AS NBLUNDI,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICE FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M."+dowtoinc+" < "+Integer.toString(calcval)+" and M.NBJEUDI_EQUILIBRE = FALSE and S.INTERIEUR = "+interieur);
	 }
	 while(rs2.next()){

		rs = ms.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES AS NBGARDES,M.NOM AS NOM,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBLUNDI AS NBLUNDI,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICES FROM MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M."+dowtoinc+" > "+Integer.toString(calcval)+"AND S.INTERIEUR = "+interieur);
		done = false;
		while(rs.next()){
			

			if(!interieur){
				secteur = "URGENCES";
			}
			else{
				secteur = "INTERIEUR";
			}
				rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+Integer.toString(rs.getInt("NUMERO"))+" and MANUALLY_SET = FALSE");
				while(rs4.next()){
					curdow = getdow(fromsql(rs4.getDate("JOUR")));
					if(curdow != dowtoinc){
						continue;
					}
					rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+rs4.getDate("JOUR")+"'");
					while(rs5.next()){
						curg = rs5.getInt("SERVICE");
					}
					rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
					while(rs5.next()){
						prevurg = rs5.getInt("SERVICE");
					}
					if(interieur){
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
						while(rs5.next()){
							prevint = rs5.getInt("SERVICE");
						}
					}

					isgood = new gtg(curg,prevint,prevurg,c,rs4.getDate("JOUR"),rs2,dowtoinc,interieur,true);
					if(isgood.gtg){
						
						action = m6.executeUpdate("UPDATE GARDES SET "+secteur+" = "+rs2.getInt("NUMERO")+", MANUALLY_SET = TRUE WHERE JOUR = '"+rs4.getDate("JOUR")+"'");
						rs6 = m6.executeQuery("SELECT "+dowtoinc+", NBGARDES,DERNIEREGARDE, NUMERO FROM MEDECINS WHERE NUMERO = "+rs2.getInt("NUMERO"));
						while(rs6.next()){
							action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs6.getInt(dowtoinc)+1)+", NBGARDES = "+Integer.toString(rs6.getInt("NBGARDES")+1)+", "+dowtoinc+"_EQUILIBRE = TRUE WHERE NUMERO = "+rs6.getInt("NUMERO"));
							action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs.getInt(dowtoinc)-1)+", NBGARDES = "+Integer.toString(rs.getInt("NBGARDES")-1)+"WHERE NUMERO = "+rs.getInt("NUMERO"));

							done = true;

							rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
							while(rs8.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs.getInt("NUMERO"));
							}
							rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs2.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
							while(rs8.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs2.getInt("NUMERO"));
							}

						
						}
					}
					if((!(isgood == null) && isgood.gtg)||done == true){
						done = false;
						break;
					}
					
				}
				if((!(isgood == null) && isgood.gtg)||done == true){
					done = false;
					break;
				}
				
		}
		if((!(isgood == null) && isgood.gtg)||done == true){
			done = false;
			break;
		}
		else{

			if(dowtoinc == "NBSAMEDI"){
				calcval = nbsamedi/nbmeds;
				 if(calcval == 0){
					 calcval = 1;
				 }
				 boolean vendredifait = false, dimanchefait = false;
				for(i = 0; i < 2; i++){
					 
					done = false;
					if(i == 0){
						if(rs2.getInt("NBVENDREDI") == 0){

							dowtoinc = "NBVENDREDI";
						}
						else{

							done = true;
							vendredifait = true;
							continue;
						}
					}
					else{
						if(rs2.getInt("NBDIMANCHE") == 0){

							dowtoinc = "NBDIMANCHE";
							
						}
						else{

							dimanchefait = true;
							done = true;
							break;
						}
					}

				rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS INNER JOIN(SELECT NUMERO FROM MEDECINS EXCEPT SELECT NUMERO FROM OPTIONS) AS M2 ON MEDECINS.NUMERO = M2.NUMERO WHERE "+dowtoinc+" > 0 ORDER BY "+dowtoinc);
				while(rs.next()){
					if(!interieur){
						secteur = "URGENCES";
					}
					else{
						secteur = "INTERIEUR";
					}
					rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE "+secteur+" = "+Integer.toString(rs.getInt("NUMERO"))+" and MANUALLY_SET = FALSE");
					while(rs4.next()){
						curdow = getdow(fromsql(rs4.getDate("JOUR")));
						if(curdow != dowtoinc){
							continue;
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+rs4.getDate("JOUR")+"'");
						while(rs5.next()){
							curg = rs5.getInt("SERVICE");
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
						while(rs5.next()){
							prevurg = rs5.getInt("SERVICE");
						}
						if(interieur){
							rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
							while(rs5.next()){
								prevint = rs5.getInt("SERVICE");
							}
						}

						isgood = new gtg(curg,prevint,prevurg,c,rs4.getDate("JOUR"),rs2,dowtoinc,interieur,true);
						if(isgood.gtg){
							
							action = m6.executeUpdate("UPDATE GARDES SET "+secteur+" = "+rs2.getInt("NUMERO")+", MANUALLY_SET = TRUE WHERE JOUR = '"+rs4.getDate("JOUR")+"'");
							rs6 = m6.executeQuery("SELECT "+dowtoinc+", NBGARDES,DERNIEREGARDE, NUMERO FROM MEDECINS WHERE NUMERO = "+rs2.getInt("NUMERO"));
							while(rs6.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs6.getInt(dowtoinc)+1)+", NBGARDES = "+Integer.toString(rs6.getInt("NBGARDES")+1)+", NBSAMEDI_EQUILIBRE = TRUE WHERE NUMERO = "+rs6.getInt("NUMERO"));
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs.getInt(dowtoinc)-1)+", NBGARDES = "+Integer.toString(rs.getInt("NBGARDES")-1)+"WHERE NUMERO = "+rs.getInt("NUMERO"));

								done = true;
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs4.getDate("JOUR")+"' WHERE NUMERO = "+rs.getInt("NUMERO"));
								}
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs2.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs4.getDate("JOUR")+"' WHERE NUMERO = "+rs2.getInt("NUMERO"));
								}


							
							}
						}
						if((!(isgood == null) && isgood.gtg)||done == true){
							
							break;
						}
				}
					if(((!(isgood == null) && isgood.gtg)||done == true)){
						
						break;
					}
					else{
						continue;
					}
				}
			}
				if(vendredifait == true && dimanchefait == true){

					action = m7.executeUpdate("UPDATE MEDECINS SET NBSAMEDI_EQUILIBRE = TRUE WHERE NUMERO = "+rs2.getInt("NUMERO"));
					
				}
				else{

				}
				
				break;
			}	

			else if(dowtoinc == "NBJEUDI"){//dowtoinc = "nbjeudi"

				if(!interieur){
					secteur = "URGENCES";
				}
				else{
					secteur = "INTERIEUR";
				}
				java.sql.Date jourJ;
				calcval = nbjeudi/nbmeds;
				 if(calcval == 0){
					 calcval = 1;
				 }
				rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS INNER JOIN JOURS_FERIES as jf on jf.NUMERO = MEDECINS.NUMERO INNER JOIN(SELECT NUMERO FROM MEDECINS EXCEPT SELECT NUMERO FROM OPTIONS) AS M2 ON MEDECINS.NUMERO = M2.NUMERO");
				while(rs.next()){
					if(interieur){
						if(secteur == "URGENCES"){
							rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = "+Integer.toString(rs.getInt("NUMERO"))+"and INTERIEUR = FALSE");
						}
						else{
							rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = "+Integer.toString(rs.getInt("NUMERO"))+"and INTERIEUR = TRUE");
						}
					}
					else{
						rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = "+Integer.toString(rs.getInt("NUMERO")));
					}
					while(rs4.next()){
						jourJ = prevday(rs4.getDate("JOUR"));
						jourJ = prevday(jourJ);

						curdow = getdow(fromsql(jourJ));
						if(curdow != "NBLUNDI"&& curdow != "NBMARDI"&& curdow !="NBMERCREDI"){

							continue;
						}
						boolean found = false;
						rs7 = m7.executeQuery("SELECT MANUALLY_SET FROM GARDES WHERE JOUR = '"+jourJ+"'");
						while(rs7.next()){
							
							found = rs7.getBoolean("MANUALLY_SET");
							if(found == true){

								break;
							}
						}
						if(found == true){
							continue;
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+jourJ+"'");
						while(rs5.next()){
							curg = rs5.getInt("SERVICE");
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+prevday(jourJ)+"'");
						while(rs5.next()){
							prevurg = rs5.getInt("SERVICE");
						}
						if(interieur){
							rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = '"+prevday(jourJ)+"'");
							while(rs5.next()){
								prevint = rs5.getInt("SERVICE");
							}
						}

						isgood = new gtg(curg,prevint,prevurg,c,rs4.getDate("JOUR"),rs2,curdow,interieur,true);
						if(isgood.gtg){
							
							action = m6.executeUpdate("UPDATE GARDES SET "+secteur+" = "+rs2.getInt("NUMERO")+", MANUALLY_SET = TRUE WHERE JOUR = '"+rs4.getDate("JOUR")+"'");
							rs6 = m6.executeQuery("SELECT "+curdow+", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = "+rs2.getInt("NUMERO"));
							while(rs6.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET "+curdow+" = "+Integer.toString(rs6.getInt(dowtoinc)+1)+", NBGARDES = "+Integer.toString(rs6.getInt("NBGARDES")+1)+", NBJEUDI_EQUILIBRE = TRUE WHERE NUMERO = "+rs6.getInt("NUMERO"));
								action = m7.executeUpdate("UPDATE MEDECINS SET "+curdow+" = "+Integer.toString(rs.getInt(dowtoinc)-1)+", NBGARDES = "+Integer.toString(rs.getInt("NBGARDES")-1)+"WHERE NUMERO = "+rs.getInt("NUMERO"));

								done = true;
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs.getDate("JOUR")+"' WHERE NUMERO = "+rs.getInt("NUMERO"));
								}
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs2.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs2.getDate("JOUR")+"' WHERE NUMERO = "+rs2.getInt("NUMERO"));
								}


							
							}
						}
						else{

						}
						if((!(isgood == null) && isgood.gtg)||done == true){
							
							break;
						}
						else{
							continue;
						}
				}
					if((!(isgood == null) && isgood.gtg)||done == true){
						
						break;
					}
				}
			}
		}
	}
	}
	rs = ms.executeQuery("SELECT COUNT(NUMERO) as nbmeds,SUM(NBVENDREDI) as allvend,SUM(NBDIMANCHE) as alldim, MAX(NBGARDES) as MAXG,MIN(NBGARDES) as MING,SUM(NBSAMEDI) as ALLSAMS,SUM(NBJEUDI) as allthu,SUM(NBGARDES) as TOTGARDES FROM MEDECINS WHERE NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");
	while(rs.next()){
		max = rs.getInt("MAXG");
		min = rs.getInt("MING");
		nbsamedi = rs.getInt("ALLSAMS");
		nbvendredi = rs.getInt("allvend");
		nbdimanche = rs.getInt("alldim");
		nbjeudi = rs.getInt("allthu");
		totgardes = rs.getInt("TOTGARDES");
		nbmeds = rs.getInt("nbmeds");
		}

	if(max > min+1 || done){
		rs2 = ms2.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES < "+Integer.toString(max-1)+" and NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");
		while(rs2.next()){

			rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES = "+Integer.toString(max)+" AND NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");
			while(rs.next()){

				if(!interieur){
					rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+Integer.toString(rs.getInt("NUMERO"))+" and MANUALLY_SET = FALSE");
					while(rs4.next()){
						dowtoinc = getdow(fromsql(rs4.getDate("JOUR")));
						if(dowtoinc == "NBJEUDI"||dowtoinc == "NBVENDREDI"||dowtoinc=="NBSAMEDI"||dowtoinc == "NBDIMANCHE"){

							continue;
						}
						
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+rs4.getDate("JOUR")+"'");
						while(rs5.next()){
							curg = rs5.getInt("SERVICE");
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
						while(rs5.next()){
							prevurg = rs5.getInt("SERVICE");
						}
						isgood = new gtg(curg,prevint,prevurg,c,rs4.getDate("JOUR"),rs2,dowtoinc,interieur,true);
						if(isgood.gtg){
							
							action = m6.executeUpdate("UPDATE GARDES SET URGENCES = "+rs2.getInt("NUMERO")+", MANUALLY_SET = TRUE WHERE JOUR = '"+rs4.getDate("JOUR")+"'");
							rs6 = m6.executeQuery("SELECT "+dowtoinc+", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = "+rs2.getInt("NUMERO"));
							while(rs6.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs6.getInt(dowtoinc)+1)+", NBGARDES = "+Integer.toString(rs6.getInt("NBGARDES")+1)+"WHERE NUMERO = "+rs6.getInt("NUMERO"));
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs.getInt(dowtoinc)-1)+", NBGARDES = "+Integer.toString(rs.getInt("NBGARDES")-1)+"WHERE NUMERO = "+rs.getInt("NUMERO"));								
								done = true;
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs.getInt("NUMERO"));
								}
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs2.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs2.getInt("NUMERO"));
								}


							}
							if((!(isgood == null) && isgood.gtg)||done == true){
								
								break;
							}
						}
						
					}
					if((!(isgood == null) && isgood.gtg)||done == true){
						
						break;
					}
				}
			}
			if((!(isgood == null) && isgood.gtg)||done == true){
				
				break;
			}
		}	
		rs = ms.executeQuery("SELECT COUNT(NUMERO) as nbmeds,SUM(NBVENDREDI) as allvend,SUM(NBDIMANCHE) as alldim,MAX(NBDIMANCHE) AS MAXDIM ,MAX(NBGARDES) as MAXG,MIN(NBGARDES) as MING,SUM(NBSAMEDI) as ALLSAMS,SUM(NBJEUDI) as allthu,SUM(NBGARDES) as TOTGARDES FROM MEDECINS WHERE NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");
		int maxdim = 0;
		done = false;
		while(rs.next()){
			max = rs.getInt("MAXG");
			min = rs.getInt("MING");
			nbsamedi = rs.getInt("ALLSAMS");
			nbvendredi = rs.getInt("allvend");
			nbdimanche = rs.getInt("alldim");
			nbjeudi = rs.getInt("allthu");
			totgardes = rs.getInt("TOTGARDES");
			nbmeds = rs.getInt("nbmeds");
			maxdim = rs.getInt("MAXDIM");
			}
		calcval = nbdimanche/nbmeds;
		if(calcval == 0){
			calcval = 1;
		}

		rs2 = ms2.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBDIMANCHE < "+calcval+" and NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) order by NBSAMEDI ASC");
		while(rs2.next()){

			rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBDIMANCHE = "+maxdim+" AND NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) order by NBSAMEDI DESC");
			while(rs.next()){

				if(!interieur){

					rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+Integer.toString(rs.getInt("NUMERO"))+" and MANUALLY_SET = FALSE AND DAYOFWEEK(JOUR) = 1");
					while(rs4.next()){
						dowtoinc = getdow(fromsql(rs4.getDate("JOUR")));
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+rs4.getDate("JOUR")+"'");
						while(rs5.next()){
							curg = rs5.getInt("SERVICE");
						}
						rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = '"+prevday(rs4.getDate("JOUR"))+"'");
						while(rs5.next()){
							prevurg = rs5.getInt("SERVICE");
						}
						isgood = new gtg(curg,666,prevurg,c,rs4.getDate("JOUR"),rs2,dowtoinc,interieur,true);
						if(isgood.gtg){
							
							action = m6.executeUpdate("UPDATE GARDES SET URGENCES = "+rs2.getInt("NUMERO")+", MANUALLY_SET = TRUE WHERE JOUR = '"+rs4.getDate("JOUR")+"'");
							rs6 = m6.executeQuery("SELECT "+dowtoinc+", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = "+rs2.getInt("NUMERO"));
							while(rs6.next()){
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs6.getInt(dowtoinc)+1)+", NBGARDES = "+Integer.toString(rs6.getInt("NBGARDES")+1)+"WHERE NUMERO = "+rs6.getInt("NUMERO"));
								action = m7.executeUpdate("UPDATE MEDECINS SET "+dowtoinc+" = "+Integer.toString(rs.getInt(dowtoinc)-1)+", NBGARDES = "+Integer.toString(rs.getInt("NBGARDES")-1)+"WHERE NUMERO = "+rs.getInt("NUMERO"));								

								done = true;
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs.getInt("NUMERO"));
								}
								rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = "+rs2.getInt("NUMERO")+" ORDER BY JOUR DESC LIMIT 1");
								while(rs8.next()){
									action = m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = '"+rs8.getDate("JOUR")+"' WHERE NUMERO = "+rs2.getInt("NUMERO"));
								}


							}
							if(done){
								break;
							}
							else{

							}
						}
						
					}
					if(done){
						break;
					}
					else{

					}
				
				}
			}
			if(done){
				break;
			}
		}
		if(done){
		equilibrer(c,interieur);
		}
	}

}

static Date fromsql(java.sql.Date d1){
	 java.util.Date utilDate = new java.util.Date(d1.getTime());
	 return utilDate;
}

}