import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.io.File; 
import java.io.IOException;
import java.util.Calendar;
import java.util.Date; 

import javax.swing.JOptionPane;



import org.joda.time.*;
import org.joda.time.DateTime;

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
		Connection c ;

			 c = DriverManager.getConnection("jdbc:hsqldb:mem:gardedb", "SA", "");
		
		Workbook data;
		
			data = Workbook.getWorkbook(new File("data.xls"));

		 WritableWorkbook workbook = Workbook.createWorkbook(new File("planning_garde.xls"));
		 
		 
		 boolean hasint = setup(c,data);
		
		 
		 filltables(c,data,hasint);
		
		 datepack monpack = genplanning(c,data,hasint);
		
		 
		 writeoutput(monpack,c,workbook,hasint,data);
		
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
		 rs = mystatement.executeUpdate("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, INTERIEUR BOOLEAN)");///<crée la table services, chaque nom a un id utilisé partout ailleurs, un integer
		 rs = mystatement.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE Date, NBGARDES INTEGER, NBLUNDI INTEGER default 0, NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0, SERVICE INTEGER, NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");///< crée la table des medecins avec nom, id et nombre de gardes
		 rs = mystatement.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT Date, DATEFIN date, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO),PRIMARY KEY(DATEDEBUT, DATEFIN,NUMERO))");///< table des vacances
		 rs = mystatement.executeUpdate("CREATE TABLE OPTIONS(NUMERO INTEGER PRIMARY KEY, NBTOTAL INTEGER, NBLUNDI INTEGER, NBMARDI INTEGER, NBMERCREDI INTEGER, NBJEUDI INTEGER, NBVENDREDI INTEGER, NBSAMEDI INTEGER, NBDIMANCHE INTEGER, NBFERIES INTEGER)");
		 if(hasint){
		 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");///<table des jours fériés exigés
		 }
		 else{
			 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR date primary key, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO))");
		 }
		 if (!hasint){
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date primary key,URGENCES INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO))");
		 }
		 else {
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date PRIMARY KEY,URGENCES INTEGER,INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO))");///<tableau de garde final
		 }
		 return hasint;
	}



///cette méthode remplis les tables de la base de données à partir du fichier excel donné en entrée
 public static void filltables(Connection c, Workbook data,boolean hasint) throws SQLException, ParseException {
	 Statement mystatement = c.createStatement();
	 Statement ms2 = c.createStatement();
	 Calendar cal = Calendar.getInstance();
	 java.sql.Date d1,d2;
	 int rs;
	 DateCell dc1,dc2;
	 Sheet sheet;
	 boolean mbool;
	 ResultSet rs2;
	 sheet = data.getSheet(4);
	/* remplissage de la table de services*/
	for (int i = 1; i < sheet.getRows();i++){
		if(sheet.getCell(1,i).getCellFormat() != null && sheet.getCell(1,i).getContents().length() != 0){
			rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',TRUE)"));
		}
		else{
			rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',FALSE)"));
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
			rs = mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE,DERNIEREGARDE) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',").concat(Integer.toString(nservice))+",'"+d1+"')");
		}
		else{
			rs = mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',").concat(Integer.toString(nservice))+")");
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
	
	
		rs = mystatement.executeUpdate("INSERT INTO IMPOSSIBILITES(DATEDEBUT,DATEFIN,NUMERO) VALUES('"+d1+"','"+d2+"',".concat(Integer.toString(nmedecin)).concat(")"));
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
			nbferies = sheet.getCell(9,i).getContents();
			
			rs = mystatement.executeUpdate("INSERT INTO OPTIONS(NUMERO,NBTOTAL,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES) VALUES("+Integer.toString(id)+","+nbtotal+","+nblundi+","+nbmardi+","+nbmercredi+","+nbjeudi+","+nbvendredi+","+nbsamedi+","+nbdimanche+","+nbferies+")");
			}
			}
		}
	}

 
/** methode de génération du planning*/
 public static datepack genplanning(Connection c/**<[in] database connection*/, Workbook data,boolean hasint) throws ParseException, SQLException{
	 int prevurg,prevint,curg;
	 int newdowcount = 0;
	 Sheet mst = data.getSheet(3);
	 int repos = Integer.parseInt(mst.getCell(2,1).getContents());
	 DateCell dc1,dc2;
	 prevurg = 666;
	 curg = 666;
	 prevint = 666;
	 int curgarde = 0;
	 Sheet msheet = data.getSheet(3);
	 dc1 = (DateCell) msheet.getCell(0,1);
	 dc2 = (DateCell) msheet.getCell(1,1);
	 datepack monpack = new datepack();
	 monpack.upto = tosql(dc1.getDate());
	 while(!monpack.upto.after(nextday(tosql(dc2.getDate())))){
		 String dowtoinc = getdow(monpack.upto);
			if(hasint){
				monpack.garde.medundefined = true;
			monpack = selecttoubib(monpack,hasint,repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,monpack.upto,true,dowtoinc);
			curg = monpack.garde.curg;
			if(monpack.garde.medundefined){
				break;
			}
			dorecord(monpack,c,true,hasint);
			}
			else{
				prevurg = 666;
			}
		 monpack = selecttoubib(monpack,hasint,repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,monpack.upto,false,dowtoinc);
		 if(monpack.garde.medundefined){
			 break;
			}
		 dorecord(monpack,c,false,hasint);
		prevurg = monpack.garde.curg;
		prevint = curg;
		monpack.upto = nextday(monpack.upto);
	 }
	 monpack.goal = tosql(dc2.getDate());
	 return monpack;
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
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(curdat);
	 cal.add(Calendar.DATE, -1);
	 return new java.sql.Date(cal.getTimeInMillis());
 }
 
 public static boolean dateferiee(java.sql.Date madate, Connection c) throws SQLException{
	 Statement ms = c.createStatement();
	 ResultSet rs = ms.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE JOUR='"+madate+"'");
	 while(rs.next()){
		 return true;
	 }
	 return false;
 }
 
/** method to select a person for a holiday*/
 public static datepack selferie(datepack monpack,boolean hasint,Connection c, java.sql.Date curdat,String dowtoinc,boolean interieur) throws SQLException{
	 Statement ms= c.createStatement();
	 Statement ms2 = c.createStatement();
	 ResultSet rs,rs2;
	 datepack res = new datepack();
	 res.garde = monpack.garde;
	 if(!interieur){
		 java.sql.Date madate = new java.sql.Date(curdat.getTime());
		 if(hasint){
			 rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE as DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(" as "+dowtoinc+",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON NUMERO = JF.NUMERO WHERE JF.JOUR = '"+madate+"' and JF.INTERIEUR = FALSE"));
		 }
		 else{
			 rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE as DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(" as "+dowtoinc+",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON NUMERO = JF.NUMERO WHERE JF.JOUR = '"+madate+"'"));
		 }
			 while(rs.next()){
			 int nbferie = 0;
		rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = "+rs.getInt("NUMERO"));
		while(rs2.next()){
			nbferie = rs2.getInt("NBFERIES");
				 }
			 res.garde.nmed = rs.getInt("NUMERO");
			 res.garde.nbferies = nbferie;
			 res.garde.curgarde = rs.getInt("NBGARDES")+1;
			 res.garde.newdowcount = rs.getInt(dowtoinc)+1;
			 res.garde.medundefined = false;
			 res.garde.jour = curdat;
			 res.upto = curdat;
			 res.garde.dowtoinc = dowtoinc;
			 res.garde.ferie = true;
			 res.garde.curg = rs.getInt("SERVICE");
			 return res;
		 }
	 }
	 else{
		 java.sql.Date madate = new java.sql.Date(curdat.getTime());
		 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO WHERE JF.JOUR = '"+madate+"' and JF.INTERIEUR = TRUE"));
	 
	 while(rs.next()){
		 int nbferie = 0;
			rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = "+rs.getInt("NUMERO"));
			while(rs2.next()){
				nbferie = rs2.getInt("NBFERIES");
					 }
		 res.garde.nmed = rs.getInt("NUMERO");
		 res.garde.curgarde = rs.getInt("NBGARDES")+1;
		 res.garde.newdowcount = rs.getInt(dowtoinc)+1;
		 res.garde.medundefined = false;
		 res.upto = curdat;
		 res.garde.ferie = true;
		 res.garde.nbferies = nbferie;
		 res.garde.jour = new java.sql.Date(curdat.getTime());
		 res.garde.dowtoinc = dowtoinc;
			 res.garde.curg = rs.getInt("SERVICE");
			 return res;
	 } 
 }
	 res.garde.medundefined = true;
	 return res;
 }

/**boolean, check if someone can actually take the shift without ruining the fun for everyone else*/
 public static gtg isgtg(int curg,int prevint,int prevurg,Connection c,java.sql.Date curdat, ResultSet rs,String dowtoinc,boolean interieur,int repos) throws SQLException, ParseException{
	 Statement ms4 = c.createStatement();
	 Statement ms2 = c.createStatement();
	 Statement ms3 = c.createStatement();
	 ResultSet rs4;
	 ResultSet rs2;
	 ResultSet rs3;
	 boolean bftest = true;
	 gtg res = new gtg();
	 boolean gtg =  true;
	 boolean inoptions = false;
	 rs4 = ms4.executeQuery("SELECT NUMERO, NBTOTAL, NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES FROM OPTIONS WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs4.next()){
			
		inoptions = true; 
	 }
	 rs2=ms2.executeQuery("SELECT DATEDEBUT,DATEFIN FROM IMPOSSIBILITES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs2.next()){
		 if((curdat.after(rs2.getDate("DATEDEBUT")) && curdat.before(rs2.getDate("DATEFIN"))) || ((curdat.compareTo(rs2.getDate("DATEDEBUT")) == 0) || (curdat.compareTo(rs2.getDate("DATEFIN"))==0))){
			 res.gtg = false;
			 res.error = "pendant les vacances";
			
			 break;
		 }
	 }
	 rs3 = ms3.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 int nbdays = Days.daysBetween(new org.joda.time.DateTime(rs.getDate("DERNIEREGARDE")), new org.joda.time.DateTime(curdat)).getDays();
	 if(nbdays < 0){
		 nbdays = nbdays*(-1);
	 }
	 while(rs3.next()){
		 int daysbf = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(rs3.getDate("JOUR"))).getDays();
		 if(daysbf < 0){
			 daysbf = daysbf*(-1);
		 }
		 bftest = res.gtg;
		 res.gtg = res.gtg && (daysbf > repos) && ((nbdays > repos)||(nbdays < 0));
		 if(bftest && !res.gtg){
			 res.error = "pas assez de temps de repos";
		 }
	 }
	bftest = res.gtg;
	 res.gtg = res.gtg && ((nbdays > repos)||(nbdays < 0));
	 if(bftest && !res.gtg){
		 res.error = "pas assez de temps de repos";
	 }
	
		if(interieur){
			bftest  = res.gtg;
			res.gtg = res.gtg && (rs.getInt("SERVICE")!=curg);
			if(bftest && !res.gtg){
				 res.error = "meme service que les urgences ce jour";
			 }
			 bftest = res.gtg;
			 res.gtg = res.gtg && (rs.getInt("SERVICE") != prevurg) && (rs.getInt("SERVICE")!= prevint);
			 if(bftest && !res.gtg){
				 res.error = "meme service que ceux de garde la veille";
			 }
		
		}
		
		if(inoptions){
			rs4 = ms4.executeQuery("SELECT NUMERO, NBTOTAL, NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES FROM OPTIONS WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
			while(rs4.next()){
			bftest = res.gtg;
			res.gtg = res.gtg && (rs.getInt("NBGARDES") < rs4.getInt("NBTOTAL")) && (nbdays > repos);
			if(bftest && !res.gtg){
				res.error = "medecin dans les options, plus de gardes que nbtotal";
			}
			bftest = res.gtg;
			res.gtg = res.gtg && (rs.getInt(dowtoinc) < rs4.getInt(dowtoinc)) && (nbdays > repos);
			if(bftest && !res.gtg){
				res.error = "medecin dans les options, plus de "+dowtoinc+" que attribué dans les options";
			}
			if(dateferiee(curdat,c)){
				bftest = res.gtg;
				res.gtg = res.gtg && (rs.getInt("NBFERIES") < rs4.getInt("NBFERIES")) && (nbdays > repos);
				if(bftest && !res.gtg){
					res.error = "medecin dans les options, plus de feries qu'attribué dans les options";
				}
			}
			}
			
		}
	
	
	 return res;
 }
 
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
public static datepack selecttoubib(datepack monpack,boolean hasint,int repos, int curg,int prevurg, int prevint,boolean medundefined,int newdowcount,int curgarde,Connection c,java.sql.Date curdat,boolean interieur,String dowtoinc) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms = c.createStatement();
	 int nextint = 0;
	 String terror = null;
	 boolean ferie = false;
	 datepack res = new datepack();
	 res.upto = monpack.upto;
	 res.goal = monpack.goal;
	 res.garde = monpack.garde;
	 ResultSet rs,rs2;
	 if(dateferiee(curdat,c)){
		 if(isreserved(hasint,interieur,c,curdat)){
		 res = selferie(res,hasint,c,curdat,dowtoinc,interieur);
		 if(!res.garde.medundefined){
				 return res;
		 }
		
		 }
	 }
	
	 	if(dateferiee(nextday(curdat),c)){
	 		if(isreserved(hasint,interieur,c,nextday(curdat))||isreserved(hasint,!interieur,c,nextday(curdat))){
	 			rs = ms.executeQuery("SELECT SERVICE,JOUR FROM MEDECINS AS M INNER JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO");
	 			while(rs.next()){
	 				DateTime d1,d2;
	 		        d1 = new DateTime(rs.getDate("JOUR"));
	 		        d2 = new DateTime(nextday(curdat));
	 				Days diff = Days.daysBetween(d1,d2);
	 				if(diff.getDays() == 0){
	 				nextint = rs.getInt("SERVICE");
	 				}
	 			}
	 		}
	 	}
		 if(!interieur){
			 if(hasint){
			 rs=ms.executeQuery("SELECT NUMERO, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> "+curg+" AND MEDECINS.SERVICE <> "+prevurg+" AND MEDECINS.SERVICE <> "+prevint+" AND MEDECINS.SERVICE <> "+nextint+" ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
			 }
			 else{
				 rs=ms.executeQuery("SELECT NUMERO, DERNIEREGARDE, NBGARDES, "+dowtoinc+",NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> "+curg+" AND MEDECINS.SERVICE <> "+prevurg+" AND MEDECINS.SERVICE <> "+prevint+" ORDER BY NBGARDES ASC, ".concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
			 }
		 }
		 else{

			 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES, M.".concat(dowtoinc).concat(", M.NBJEUDI, M.NBVENDREDI, M.NBSAMEDI, M.NBDIMANCHE, M.NBFERIES, M.SERVICE FROM (MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) WHERE S.INTERIEUR = TRUE AND M.SERVICE <> "+prevurg+" AND M.SERVICE <> "+prevint+" AND M.SERVICE <> "+nextint+" ORDER BY NBGARDES ASC ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
			} 
		 while(rs.next()){
			 		 if((rs.getInt("SERVICE") == prevurg) || (rs.getInt("SERVICE") == prevint)||(interieur && (rs.getInt("SERVICE") == curg))||((rs.getInt("SERVICE") == nextint))&& hasint){
			 			 continue;
			 		 }
			 		 gtg isgood = isgtg(curg,prevint,prevurg,c,curdat,rs,dowtoinc,interieur,repos);
					 if(isgood.gtg){
						 res.garde.ferie = dateferiee(curdat,c);
						 if(ferie){
							 int nbferie = 0;
								rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = "+rs.getInt("NUMERO"));
								while(rs2.next()){
									nbferie = rs2.getInt("NBFERIES");
										 }
							 res.garde.nbferies = nbferie;
						 }
						 res.garde.dowtoinc = dowtoinc;
						 res.garde.jour = new java.sql.Date(curdat.getTime());
						 res.garde.curg = rs.getInt("SERVICE");
						res.garde.nmed = rs.getInt("NUMERO");
						 res.garde.curgarde = rs.getInt("NBGARDES")+1;
						 res.garde.newdowcount = rs.getInt(dowtoinc)+1;
						res.garde.medundefined = false;
						res.upto = curdat;
						return res;
					 }
					 else{
						 terror = isgood.error;
					 }
				 }	 
		 res.error = terror;
	res.garde.medundefined = true;		 
	 return res;
		 }

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
		 rs = ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(monpack.garde.nmed))+" WHERE JOUR = '"+sqldate+"'");
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR) VALUES('"+sqldate+"',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
	 }
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES) VALUES('"+sqldate+"',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
	 }
 }

/**function to write to the output excel file*/
public static void writeoutput(datepack monpack,Connection c, WritableWorkbook output,boolean hasint,Workbook data) throws SQLException, RowsExceededException, WriteException, IOException{
	writegardes(monpack,c,output,hasint);
	writestats(c,output,hasint);
	writegps(c,output,hasint);
	writecalendar(c,output,hasint);
	updatedata(c,data);
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
public static void writegardes(datepack monpack,Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
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
				l2 = new Label(1,j,"service");
			}
			ms.addCell(l2);
			}
			ms.addCell(l1);
			
			j++;
		}
		i++;
	}
}

/**update the input excel file with informations such as last shift done*/
public static void updatedata(Connection c, Workbook data) throws SQLException, IOException, RowsExceededException, WriteException{
	Statement ms = c.createStatement();
	ResultSet rs;
	WritableWorkbook data2 = Workbook.createWorkbook(new File("data.xls"),data);
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
}

/**echange deux gardes a partir des dates
 * @throws SQLException */
public static void swap(Connection c, java.sql.Date d1,java.sql.Date d2,boolean interieur) throws SQLException{
	int med1 = 0,med2 =0;
	Statement ms = c.createStatement(),ms2 = c.createStatement();
	int ru;
	ResultSet rs,rs2;
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
}

/** equilibre le tableau de garde de la manière la plus juste possible en prenant en compte le poids des jours
 * @throws SQLException */
public static void equilibrer(Connection c,boolean interieur,int repos) throws SQLException{
	Statement ms = c.createStatement(),ms2 = c.createStatement(),ms3 = c.createStatement(),ms4 = c.createStatement(),ms5 = c.createStatement();
	ResultSet rs,rs2,rs3,rs4,rs5;
	int curg,prevint,prevurg;
	gtg isgood; 
	int action,max = 0,min = 0,nbsamedi,totgardes,nbjour = 0;
	rs = ms.executeQuery("SELECT MAX(NBGARDES) as MAXG,MIN(NBGARDES) AS MING,SUM(NBSAMEDI) as ALLSAMS,SUM(NBGARDES) as TOTGARDES FROM MEDECINS");
	while(rs.next()){
		max = rs.getInt("MAXG");
		min = rs.getInt("MING");
		nbsamedi = rs.getInt("ALLSAMS");
		totgardes = rs.getInt("TOTGARDES");
	}
	String dowtoinc;
	if(max > min+1){
		rs2 = ms2.executeQuery("SELECT NUMERO,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES = "+Integer.toString(min));
		//j'essaie d'abord d'equilibrer avec des lundimardimercredi
		while(rs2.next()){
			rs = ms.executeQuery("SELECT NUMERO,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES = "+Integer.toString(max));
			while(rs.next()){
				rs = ms3.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NUMERO = "+Integer.toString(rs2.getInt("NUMERO")));
				nbjour = rs.getInt("NBLUNDI");
				if(nbjour == 0){
					nbjour = rs.getInt("NBMARDI");
				}
				if(nbjour == 0){
					nbjour = rs.getInt("NBMERCREDI");
				}
				if(nbjour == 0){
					continue;
				}
				if(interieur){
					rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE INTERIEUR = "+Integer.toString(rs.getInt("NUMERO")));
					while(rs4.next()){
						dowtoinc = getdow(fromsql(rs4.getDate("JOUR")));
				
							isgood = isgtg(curg,prevint,prevurg,c,curdat,rs,dowtoinc,interieur,repos);
						
					}
				}
			}
		}	
	}
}

static Date fromsql(java.sql.Date d1){
	 java.util.Date utilDate = new java.util.Date(d1.getTime());
	 return utilDate;
}

}
