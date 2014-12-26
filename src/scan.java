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
import org.joda.time.DateTime;


import jxl.*;
import jxl.read.biff.BiffException;
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

		 WritableWorkbook workbook = Workbook.createWorkbook(new File("planning_garde.xls"));
		 
		 
		 boolean hasint = setup(c,data);
		 
		
		 
		 filltables(c,data,hasint);
		
		 genplanning(c,data,hasint);
		 
		
		 
		 writeoutput(c,workbook,hasint);
		
	}
	
	public static boolean  setup(Connection c, Workbook data) throws SQLException	{
		Sheet sheet = data.getSheet(4);
		boolean hasint = false;
		 Cell cur;
		 for (int i = 1; i < sheet.getRows(); i++){
			 cur = sheet.getCell(1,i);
			 if (!hasint){
				 hasint = cur.getCellFormat() != null;
			 }
		 }
		
		Statement mystatement = c.createStatement();
		 int rs;
		 rs = mystatement.executeUpdate("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, INTERIEUR BOOLEAN)");
		 rs = mystatement.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE Date, NBGARDES INTEGER, NBLUNDI INTEGER default 0, NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0, SERVICE INTEGER,NBSEMESTRES INTEGER, NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");
		 rs = mystatement.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT Date, DATEFIN date, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO))");
		 if(hasint){
		 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");
		 }
		 else{
			 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR date primary key, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO))");
		 }
		 if (!hasint){
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date primary key,URGENCES INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO))");
		 }
		 else {
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date PRIMARY KEY,URGENCES INTEGER,INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO))");
		 }
		 return hasint;
	}



 public static void filltables(Connection c, Workbook data,boolean hasint) throws SQLException, ParseException {
	 Statement mystatement = c.createStatement();
	 Statement ms2 = c.createStatement();
	 Calendar cal = Calendar.getInstance();
	 java.sql.Date d1,d2;
	 int rs;
	 Sheet sheet;
	 boolean mbool;
	 ResultSet rs2;
	 sheet = data.getSheet(4);
	for (int i = 1; i < sheet.getRows();i++){
		if(sheet.getCell(1,i).getCellFormat() != null){
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
	DateCell dc1 = (DateCell) sheet.getCell(0,i);
	DateCell dc2 = (DateCell) sheet.getCell(1,i);
	
	 
	cal.setTime(dc1.getDate());
	d1 = new java.sql.Date(cal.getTimeInMillis());
	cal.setTime(dc2.getDate());
	d2 = new java.sql.Date(cal.getTimeInMillis());
	
	
		rs = mystatement.executeUpdate("INSERT INTO IMPOSSIBILITES(DATEDEBUT,DATEFIN,NUMERO) VALUES('"+d1+"','"+d2+"',".concat(Integer.toString(nmedecin)).concat(")"));
}
}
	
	sheet = data.getSheet(1);
	for (int i = 1; i < sheet.getRows();i++){
		DateCell dc = (DateCell)sheet.getCell(0,i);
		dc = (DateCell) sheet.getCell(0,i);
		cal.setTime(dc.getDate());
		d1 = new java.sql.Date(cal.getTimeInMillis());
		if(sheet.getCell(1,i).getCellFormat() != null){
		rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = '".concat(sheet.getCell(1,i).getContents()).concat("'"));
		while(rs2.next()){
		if(hasint){
			mbool = sheet.getCell(2, i).getCellFormat() != null;
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
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES('"+d1+"',TRUE)");
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES('"+d1+"',FALSE)");
		}
		}
 }
 
 public static void genplanning(Connection c, Workbook data,boolean hasint) throws ParseException, SQLException{
	 int prevurg,prevint,curg;
	 int newdowcount = 0;
	 Sheet mst = data.getSheet(3);
	 int repos = Integer.parseInt(mst.getCell(2,1).getContents());
	 DateCell dc1,dc2;
	 prevurg = 666;
	 curg = 666;
	 prevint = 666;
	 int curgarde = 0;
	 Date datedebut,curdat,datefin;
	 Sheet msheet = data.getSheet(3);
	 dc1 = (DateCell) msheet.getCell(0,1);
	 dc2 = (DateCell) msheet.getCell(1,1);
	 datedebut = dc1.getDate();
	 datefin = dc2.getDate();
	 curdat = datedebut;
	 while(!curdat.equals(datefin)){
		 String dowtoinc = getdow(curdat);
		 dunit garde = new dunit(666, dowtoinc, dowtoinc, curgarde, curgarde, curgarde, curgarde, true);
			if(hasint){
				garde.medundefined = true;
			garde = selecttoubib(hasint,repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,curdat,true,dowtoinc);
			curg = garde.curg;
			if(garde.medundefined){
				break;
			}
			dorecord(c,garde,true,hasint);
			}
			else{
				prevurg = 666;
			}
			garde = null;
		 garde = selecttoubib(hasint,repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,curdat,false,dowtoinc);
		 if(garde.medundefined){
				break;
			}
		 dorecord(c,garde,false,hasint);
		prevurg = garde.curg;
		prevint = curg;
		curdat = nextday(curdat);
	 }
	 
 }
 
 public static java.sql.Date nextday(Date curdat){
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(curdat);
	 cal.add(Calendar.DATE, 1);
	 return new java.sql.Date(cal.getTimeInMillis());
 }
 
 public static boolean dateferiee(Date curdat, Connection c) throws SQLException{
	 Statement ms = c.createStatement();
	 java.sql.Date madate = new java.sql.Date(curdat.getTime());
	 ResultSet rs = ms.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE JOUR='"+madate+"'");
	 while(rs.next()){
		 return true;
	 }
	 return false;
 }
 
 public static dunit selferie(boolean hasint,Connection c,Date curdat, dunit garde,String dowtoinc,boolean interieur) throws SQLException{
	 Statement ms= c.createStatement();
	 Statement ms2 = c.createStatement();
	 ResultSet rs,rs2;
	 dunit res = garde;
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
			 res.nmed = rs.getInt("NUMERO");
			 res.nbferies = nbferie;
			 res.curgarde = rs.getInt("NBGARDES")+1;
			 res.newdowcount = rs.getInt(dowtoinc)+1;
			 res.medundefined = false;
			 res.jour = new java.sql.Date(curdat.getTime());
			 res.dowtoinc = dowtoinc;
			 res.ferie = true;
			 res.curg = rs.getInt("SERVICE");
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
		 res.nmed = rs.getInt("NUMERO");
		 res.curgarde = rs.getInt("NBGARDES")+1;
		 res.newdowcount = rs.getInt(dowtoinc)+1;
		 res.medundefined = false;
		 res.ferie = true;
		 res.nbferies = nbferie;
		 res.jour = new java.sql.Date(curdat.getTime());
		 res.dowtoinc = dowtoinc;
			 res.curg = rs.getInt("SERVICE");
			 return res;
	 } 
 }
	 res.medundefined = true;
	 return res;
 }

 public static boolean isgtg(int curg,int prevint,int prevurg,Connection c,Date curdat, ResultSet rs,String dowtoinc,boolean interieur,int repos) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms3 = c.createStatement();
	 ResultSet rs2;
	 ResultSet rs3;
	 boolean gtg =  true;
	 rs2=ms2.executeQuery("SELECT DATEDEBUT,DATEFIN FROM IMPOSSIBILITES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs2.next()){
		 if(curdat.after(rs2.getDate("DATEDEBUT")) && curdat.before(rs2.getDate("DATEFIN"))){
			 gtg = false;
			
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
		 gtg = gtg && (daysbf > repos) && ((nbdays > repos)||(nbdays < 0));
		
	 }
	
	 gtg = gtg && ((nbdays > repos)||(nbdays < 0));
	
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
			 gtg = gtg && (rs.getInt(dowtoinc) == 0) && (rs.getInt("NBFERIES") == 0);
			 
		 }
		 else if(dateferiee(curdat,c)&&dowtoinc!="NBDIMANCHE"){
			 gtg = gtg && (rs.getInt("NBDIMANCHE") == 0);
			 
		 }
		 if(interieur){
			 gtg = gtg && rs.getInt("SERVICE") != curg;
			
		 }
	 }
	 else if(rs.getInt("NBSEMESTRES") >= 5){
		 gtg = gtg && rs.getInt("NBGARDES") < 3 && !dateferiee(curdat,c);
		 
		 if((dowtoinc == "NBVENDREDI")||(dowtoinc == "NBSAMEDI")||(dowtoinc=="NBDIMANCHE")){
			 gtg = false;
		 }
		 if(interieur){
			 gtg = gtg && rs.getInt("SERVICE") != curg;
			
		 
		 }
		 gtg = gtg && (rs.getInt("SERVICE") != prevurg) && (rs.getInt("SERVICE")!= prevint);
		
	 }
	
	 return gtg;
 }
 
 public static boolean isreserved(boolean hasint,boolean interieur,Connection c,Date curdat) throws SQLException{
	 boolean itis = false;
	 Statement ms = c.createStatement();
	 ResultSet rs2;
	 java.sql.Date madate = new java.sql.Date(curdat.getTime());
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
 
 public static dunit selecttoubib(boolean hasint,int repos, int curg,int prevurg, int prevint,boolean medundefined,int newdowcount,int curgarde,Connection c,Date curdat,boolean interieur,String dowtoinc) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms = c.createStatement();
	 int nextint = 0;
	 boolean ferie = false;
	 dunit res = new dunit(curgarde, dowtoinc, dowtoinc, curgarde, curgarde, curgarde, curgarde, true);
	 ResultSet rs,rs2;
	 if(dateferiee(curdat,c)){
		 if(isreserved(hasint,interieur,c,curdat)){
		 res = selferie(hasint,c,curdat,res,dowtoinc,interieur);
		 if(!res.medundefined){
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
			 rs=ms.executeQuery("SELECT NUMERO, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBSEMESTRES, NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> "+curg+" AND MEDECINS.SERVICE <> "+prevurg+" AND MEDECINS.SERVICE <> "+prevint+" AND MEDECINS.SERVICE <> "+nextint+" ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
		 }
		 else{
			 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES, M.".concat(dowtoinc).concat(", M.NBSEMESTRES, M.NBJEUDI, M.NBVENDREDI, M.NBSAMEDI, M.NBDIMANCHE, M.NBFERIES, M.SERVICE FROM (MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) WHERE S.INTERIEUR = TRUE AND M.SERVICE <> "+prevurg+" AND M.SERVICE <> "+prevint+" AND M.SERVICE <> "+nextint+" ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
					 
			} 
		 while(rs.next()){
			 
			 		 if((rs.getInt("SERVICE") == prevurg) || (rs.getInt("SERVICE") == prevint)||(interieur && (rs.getInt("SERVICE") == curg))||(rs.getInt("SERVICE") == nextint)){
			 			 continue;
			 		 }
					 if(isgtg(curg,prevint,prevurg,c,curdat,rs,dowtoinc,interieur,repos)){

						 res.ferie = ferie;
						 if(ferie){
							 int nbferie = 0;
								rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = "+rs.getInt("NUMERO"));
								while(rs2.next()){
									nbferie = rs2.getInt("NBFERIES");
										 }
							 res.nbferies = nbferie;
						 }
						 res.dowtoinc = dowtoinc;
						 res.jour = new java.sql.Date(curdat.getTime());
						 res.curg = rs.getInt("SERVICE");
						res.nmed = rs.getInt("NUMERO");
						 res.curgarde = rs.getInt("NBGARDES")+1;
						 res.newdowcount = rs.getInt(dowtoinc)+1;
						res.medundefined = false;
						return res;
					 }
				 }
	res.medundefined = true;		 
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

public static void dorecord(Connection c, dunit garde,boolean interieur,boolean hasint) throws SQLException, ParseException{
	Statement ms = c.createStatement();
	java.sql.Date sqldate = garde.jour;
	 int rs = ms.executeUpdate("UPDATE MEDECINS set DERNIEREGARDE = '"+garde.jour+"' WHERE NUMERO = ".concat(Integer.toString(garde.nmed)));
	 if(garde.ferie||dateferiee(garde.jour,c)){
		 int newf = garde.nbferies + 1;
		 rs = ms.executeUpdate("UPDATE MEDECINS SET NBFERIES = "+newf+" WHERE NUMERO = "+garde.nmed);
	 }
	 rs = ms.executeUpdate("update MEDECINS set ".concat(garde.dowtoinc).concat(" = ").concat(Integer.toString(garde.newdowcount)).concat("where NUMERO = ").concat(Integer.toString(garde.nmed)));
	 rs=ms.executeUpdate("UPDATE MEDECINS set NBGARDES = ".concat(Integer.toString(garde.curgarde)).concat("WHERE NUMERO = ").concat(Integer.toString(garde.nmed)));
	 if(hasint){
	 if(!interieur){
		 rs = ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(garde.nmed))+" WHERE JOUR = '"+sqldate+"'");
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR) VALUES('"+sqldate+"',".concat(Integer.toString(garde.nmed)).concat(")"));
	 }
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES) VALUES('"+sqldate+"',".concat(Integer.toString(garde.nmed)).concat(")"));
	 }
 }

public static void writeoutput(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	writegardes(c,output,hasint);
	writestats(c,output,hasint);
	writegps(c,output,hasint);
	writecalendar(c,output,hasint);
	output.write();	
	output.close();
}

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

public static void writegardes(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
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
}

public static void writestats(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("stats", 1);
	Statement mst = c.createStatement();
	ResultSet rs = mst.executeQuery("SELECT M.NOM AS NOM, M.NBSEMESTRES AS SEMESTRE, M.NBGARDES AS NBGARDES, M.NBFERIES AS NBFERIES,M.NBLUNDI AS NBLUNDI,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI, M.NBJEUDI AS NBJEUDI, M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE, S.NOM AS service FROM (MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) order by SERVICE ASC, SEMESTRE ASC, NOM ASC");
	Label l1,l2,l3,l4,l5,l6,l7,l8,l9,l10,l11,l12;
	l1 = new Label(0,0,"nom");
	l2 = new Label(1,0,"score");
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
		l2 = new Label(1,i,Integer.toString(rs.getInt("SEMESTRE")));
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
		i++;
	}

}

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

}
