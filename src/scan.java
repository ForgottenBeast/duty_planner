import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.io.File; 
import java.io.IOException;
import java.util.Calendar;
import java.util.Date; 
import org.joda.time.*;

import java.util.Formatter;
import jxl.*; 
import jxl.read.biff.BiffException;
import jxl.write.Number;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;




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
		boolean hasint = false;
		 WritableWorkbook workbook = Workbook.createWorkbook(new File("planning_garde.xls"));
		 System.out.println("Setting up");
		 setup(c,data,hasint);
		 System.out.println("done setting up");
		 System.out.println("filling tables");
		 filltables(c,data);
		 System.out.println("done");
		 System.out.println("generation");
		 genplanning(c,data);
		 System.out.println("done");
		 writeoutput(c,workbook,hasint);

	}
	
	public static void  setup(Connection c, Workbook data,boolean hasint) throws SQLException	{
		Sheet sheet = data.getSheet(4);
		 Cell cur;
		 for (int i = 1; i < sheet.getRows(); i++){
			 cur = sheet.getCell(2,i);
			 if (!hasint){
				 hasint = cur.getCellFormat() != null;
			 }
		 }
		Statement mystatement = c.createStatement();
		 int rs;
		 rs = mystatement.executeUpdate("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, INTERIEUR BOOLEAN)");
		 rs = mystatement.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE varchar(20) default '01 01 1970 12:01:01', NBGARDES INTEGER, NBLUNDI INTEGER default 0, NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0, SERVICE INTEGER,NBSEMESTRES INTEGER, NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");
		 rs = mystatement.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT varchar(20), DATEFIN varchar(20), NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO))");
		 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR varchar(20), NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");
		 if (!hasint){
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR VARCHAR(20) primary key,URGENCES INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO))");
		 }
		 else {
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR VARCHAR(20) PRIMARY KEY,URGENCES INTEGER,INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO))");
		 }
	}



 public static void filltables(Connection c, Workbook data) throws SQLException, ParseException {
	 Statement mystatement = c.createStatement();
	 Statement ms2 = c.createStatement();
	 int rs;
	 SimpleDateFormat formatter = new SimpleDateFormat("dd mm yyyy hh:mm:ss");
	 boolean mbool;
	 ResultSet rs2;
	 Sheet sheet = data.getSheet(4);
	for (int i = 1; i < sheet.getRows();i++){
		if(sheet.getCell(2,i).getCellFormat() != null){
			rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',TRUE)"));
		}
		else{
			rs = mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',FALSE)"));
		}
	}
	
	sheet = data.getSheet(0);
	int nservice;
	for (int i = 1; i < sheet.getRows();i++) {
		rs2 = ms2.executeQuery("SELECT NUMERO FROM SERVICES WHERE NOM = '".concat(sheet.getCell(2,i).getContents()).concat("'"));
		while(rs2.next()){
		nservice = rs2.getInt("NUMERO");
		rs = mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,NBSEMESTRES,SERVICE) VALUES('".concat(sheet.getCell(0,i).getContents()).concat("',").concat(sheet.getCell(1,i).getContents()).concat(",").concat(Integer.toString(nservice)).concat(")"));

		}
			}
	
	sheet = data.getSheet(2);
	for (int i = 1; i < sheet.getRows();i++){
		rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '".concat(sheet.getCell(2,i).getContents()).concat("'"));
while(rs2.next()){
	int nmedecin = rs2.getInt("NUMERO");
	
		rs = mystatement.executeUpdate("INSERT INTO IMPOSSIBILITES(DATEDEBUT,DATEFIN,NUMERO) VALUES('".concat(formatter.format(formatter.parse(sheet.getCell(0,i).getContents()))).concat("','").concat(formatter.format(formatter.parse(sheet.getCell(1,i).getContents()))).concat("',").concat(Integer.toString(nmedecin)).concat(")"));
}
}
	
	sheet = data.getSheet(1);
	for (int i = 1; i < sheet.getRows();i++){
		rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '".concat(sheet.getCell(1,i).getContents()).concat("'"));
		while(rs2.next()){
			mbool = sheet.getCell(2, i).getCellFormat() != null;
		int nmedecin = rs2.getInt("NUMERO");
		if(mbool){
		rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES ('".concat(formatter.format(formatter.parse(sheet.getCell(0,i).getContents()))).concat("',").concat(Integer.toString(nmedecin).concat(",").concat("TRUE").concat(")")));
		}
		else{
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES ('".concat(formatter.format(formatter.parse(sheet.getCell(0,i).getContents()))).concat("',").concat(Integer.toString(nmedecin).concat(")").concat("FALSE")));

		}
		}
		}
 }
 
 public static void genplanning(Connection c, Workbook data) throws ParseException, SQLException{
	 int prevurg,prevint,curg,nbinterieur;
	 boolean medundefined,interieurundefined;
	 int newdowcount = 0;
	 Sheet mst = data.getSheet(3);
	 int repos = Integer.parseInt(mst.getCell(2,1).getContents());
	 Calendar cal = Calendar.getInstance();
	 prevurg = 666;
	 curg = 666;
	 prevint = 666;
	 int curgarde = 0;
	 int nmed = 0;
	 SimpleDateFormat formatter = new SimpleDateFormat("dd mm yyyy hh:mm:ss");
	 Date datedebut,curdat,datefin;
	 Sheet msheet = data.getSheet(3);
	 datedebut = formatter.parse(msheet.getCell(0,1).getContents());
	 datefin = formatter.parse(msheet.getCell(1,1).getContents());
	 curdat = datedebut;
	 outloop:
	 while(Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(datefin)).getDays() >= 0){
		 System.out.println("iterating at".concat(formatter.format(curdat)));
		 medundefined = true;
		 interieurundefined = true;
		 String dowtoinc = getdow(curdat);
		 dunit garde;
		garde = selecttoubib(repos,curg,prevurg,prevint,medundefined,newdowcount,curgarde,c,curdat,false,dowtoinc,formatter);
		System.out.println("chose toubib number".concat(Integer.toString(garde.nmed)));
		dorecord(c,garde,false,formatter);
		garde = selecttoubib(repos,curg,prevurg,prevint,interieurundefined,newdowcount,curgarde,c,curdat,true,dowtoinc,formatter);
		dorecord(c,garde,true,formatter);
		prevurg = garde.curg;
		prevint = garde.curint;
		cal.setTime(curdat);
		cal.add(Calendar.DATE, 1);
		curdat = cal.getTime();
	 }
	 
 }
 
 public static boolean dateferiee(Date curdat, Connection c,SimpleDateFormat fmt) throws SQLException{
	 Statement ms = c.createStatement();
	 ResultSet rs = ms.executeQuery("SELECT NUMERO, INTERIEUR FROM JOURS_FERIES WHERE JOUR='".concat(fmt.format(curdat)).concat("'"));
	 while(rs.next()){
		 return true;
	 }
	 return false;
 }
 public class dunit {
	    public dunit(int i, String string, String string2, int j, int k, int l,
			int m, boolean b) {
		
	}
		int nmed;
	    String jour;
	    String dowtoinc;
	    int curg;
	    int curint;
	    int curgarde;
	    int newdowcount;
	    boolean medundefined;
	    // etc
	}
 public static dunit selecttoubib(int repos, int curg,int prevurg, int prevint,boolean medundefined,int newdowcount,int curgarde,Connection c,Date curdat,boolean interieur,String dowtoinc,SimpleDateFormat fmt) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms3 = c.createStatement();
	 Statement ms = c.createStatement();
	 dunit res = ;
	 ResultSet rs,rs2,rs3;
	 if(dateferiee(curdat,c,fmt)){
		 if(!interieur){
			 rs = ms.executeQuery("SELECT M.NUMERO, M.DERNIEREGARDE, M.NBGARDES,M.".concat(dowtoinc).concat(",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO WHERE JF.JOUR = '").concat(fmt.format(curdat)).concat("' and JF.INTERIEUR = FALSE"));
		 }
		 else{
			 rs=ms.executeQuery("SELECT M.NUMERO, M.DERNIEREGARDE, M.NBGARDES,M.".concat(dowtoinc).concat(",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO WHERE JF.JOUR = '").concat(fmt.format(curdat)).concat("' and JF.INTERIEUR = FALSE"));
		 }
		 while(rs.next()){
			 res.nmed = rs.getInt("M.NUMERO");
			 res.curgarde = rs.getInt("M.NBGARDES")+1;
			 res.newdowcount = rs.getInt(dowtoinc)+1;
			 medundefined = false;
			 if(!interieur){
				 res.curg = rs.getInt("SERVICE");
			 }
		 }
	 }
	 else{
		 if(!interieur){
			 rs=ms.executeQuery("SELECT NUMERO, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBSEMESTRES, NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
		 }
		 else{
			 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES, M.".concat(dowtoinc).concat(", M.NBSEMESTRES, M.NBJEUDI, M.NBVENDREDI, M.NBSAMEDI, M.NBDIMANCHE, M.NBFERIES, M.SERVICE FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE S.INTERIEUR = TRUE ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
		 }
		 loops:
		 while(rs.next()){
			 rs2=ms2.executeQuery("SELECT DATEDEBUT,DATEFIN FROM IMPOSSIBILITES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
			 while(rs2.next()){
				 if(curdat.after(fmt.parse(rs2.getString("DATEDEBUT"))) && curdat.before(fmt.parse(rs2.getString("DATEFIN")))){
					 continue;
				 }
				 else{
					 rs3 = ms3.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
					 boolean gtg = true;
					 int nbdays = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(fmt.parse(rs.getString("DERNIEREGARDE")))).getDays();
					 
					 while(rs3.next()){
						 int nbdaysf = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(fmt.parse(rs3.getString("JOUR")))).getDays();
				
						 gtg = gtg && (nbdaysf > repos) && (nbdays > repos);
						 
					 }
					 gtg = gtg && (nbdays > repos);
					 if (rs.getInt("NBSEMESTRES") == 0){
						
						
						if(interieur){
							gtg = gtg && (rs.getInt("SERVICE")!=curg);
						}
					 }
					 else if(rs.getInt("NBSEMESTRES")== 3 ||rs.getInt("NBSEMESTRES")== 4 ){
						 gtg = gtg && (rs.getInt("NBGARDES") < 5) && (nbdays > repos);
						 if(dowtoinc == "NBJEUDI"){
							 gtg = gtg && (rs.getInt(dowtoinc) == 0);
						 }
						 else if(dowtoinc == "NBVENDREDI"){
							 gtg = gtg && (rs.getInt(dowtoinc)==0);
						 }
						 else if (dowtoinc == "NBDIMANCHE"){
							 gtg = gtg && (rs.getInt(dowtoinc) == 0);
						 }
						 else if(dateferiee(curdat,c,fmt)&&dowtoinc!="NBDIMANCHE"){
							 gtg = gtg && (rs.getInt("NBDIMANCHE") == 0);
						 }
						 if(interieur){
							 gtg = gtg && rs.getInt("SERVICE") != curg;
						 }
					 }
					 else if(rs.getInt("NBSEMESTRES") >= 5){
						 gtg = gtg && rs.getInt("NBGARDES") < 3 && !dateferiee(curdat,c,fmt);
						 if((dowtoinc == "NBVENDREDI")||(dowtoinc == "NBSAMEDI")||(dowtoinc=="NBDIMANCHE")){
							 continue;
						 }
						 if(interieur){
							 gtg = gtg && rs.getInt("SERVICE") != curg;
						 }
					 }
					 gtg = gtg && (rs.getInt("SERVICE") != prevurg) && (rs.getInt("SERVICE")!= prevint);
					 if(gtg){
						 res.curgarde = rs.getInt("NBGARDES")+1;
						 res.newdowcount = rs.getInt(dowtoinc)+1;
						 res.medundefined = false;
						 res.dowtoinc = dowtoinc;
						 if(interieur){
							 res.curint = rs.getInt("SERVICE");
						 }
						 else{
							 res.curg = rs.getInt("SERVICE");
						 }
						 
						res.nmed = rs.getInt("NUMERO");
						 
					 }
				 }
			 }
		 }
		
	 }
	 if(res.medundefined == true){
		 System.out.println("medundefined!");
	 }
	 return res;
 }
 
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

 
public static void dorecord(Connection c, dunit garde,boolean interieur,SimpleDateFormat fmt) throws SQLException{
	 Statement ms = c.createStatement();
	 int rs = ms.executeUpdate("UPDATE MEDECINS set DERNIEREGARDE = '".concat(fmt.format(garde.jour)).concat("' WHERE NUMERO = ").concat(Integer.toString(garde.nmed)));
	 rs = ms.executeUpdate("update MEDECINS set ".concat(garde.dowtoinc).concat(" = ").concat(Integer.toString(garde.newdowcount)).concat("where NUMERO = ").concat(Integer.toString(garde.nmed)));
	 rs=ms.executeUpdate("UPDATE MEDECINS set NBGARDES = ".concat(Integer.toString(garde.curgarde)).concat("WHERE NUMERO = ").concat(Integer.toString(garde.nmed)));
	 if(interieur){
		 rs = ms.executeUpdate("UPDATE GARDES SET INTERIEUR = ".concat(Integer.toString(garde.nmed)).concat(" WHERE JOUR = '").concat(fmt.format(garde.jour)).concat("'"));
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES) VALUES('".concat(fmt.format(garde.jour)).concat("',").concat(Integer.toString(garde.nmed)).concat(")"));
	 }
 }

public static void writeoutput(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("planning", 0);
	Statement mst = c.createStatement();
	ResultSet rs = mst.executeQuery("SELECT * FROM GARDES");
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
	
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("JOUR"));
		l2 = new Label(1,i,Integer.toString(rs.getInt("URGENCES")));
		if(hasint){
		l3 = new Label(2,i,Integer.toString(rs.getInt("INTERIEUR")));
		ms.addCell(l3);
		}
		ms.addCell(l1);
		ms.addCell(l2);
		
		i++;
	}
	output.write();
	output.close();
}
public static void test(Connection c, WritableWorkbook output) throws SQLException, WriteException, IOException{
	 Statement ms = c.createStatement();
	 WritableSheet msheet = output.createSheet("services", 0);
	 ResultSet rs = ms.executeQuery("SELECT * FROM SERVICES");
	 boolean mbool;
	 String nom,numero;
	 int j;
	 j = 1;
	 Label l1,l2,l3;
	 l1 = new Label(0,0,"service");
	 l2 = new Label(1,0,"Interieur");
	 l3 = new Label(2,0,"Numero");
	 msheet.addCell(l1);
		msheet.addCell(l2);
		msheet.addCell(l3);
	 while(rs.next()){
		 mbool = rs.getBoolean("INTERIEUR");
		 numero = Integer.toString(rs.getInt("NUMERO"));
		 nom = rs.getString("NOM");
		 l1 = new Label(0,j,nom);
		 if(mbool){
			 l2 = new Label(1,j,"y");
		 }
		 else{
			 l2 = new Label(1,j,"");
		 }
		l3 = new Label (2,j,numero);
		msheet.addCell(l1);
		msheet.addCell(l2);
		msheet.addCell(l3);
		j++;
	 }
	 output.write();
	 output.close();
 }
}