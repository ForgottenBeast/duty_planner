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
		 System.out.println("Setting up");
		 
		 boolean hasint = setup(c,data);
		 
		 System.out.println("done setting up");
		 
		 System.out.println("filling tables");
		 
		 filltables(c,data);
		 
		 System.out.println("done");
		 
		 System.out.println("generation");
		 
		 genplanning(c,data,hasint);
		 
		 System.out.println("done");
		 
		 writeoutput(c,workbook,hasint);
		 
		 System.out.println("wrote output");
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
		 rs = mystatement.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE varchar(20) default '01 01 1970 12:01:01', NBGARDES INTEGER, NBLUNDI INTEGER default 0, NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0, SERVICE INTEGER,NBSEMESTRES INTEGER, NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");
		 rs = mystatement.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT varchar(20), DATEFIN varchar(20), NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO))");
		 rs = mystatement.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR varchar(20), NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");
		 if (!hasint){
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date primary key,URGENCES INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO))");
		 }
		 else {
			 rs = mystatement.executeUpdate("CREATE TABLE GARDES(JOUR Date PRIMARY KEY,URGENCES INTEGER,INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO))");
		 }
		 return hasint;
	}



 public static void filltables(Connection c, Workbook data) throws SQLException, ParseException {
	 Statement mystatement = c.createStatement();
	 Statement ms2 = c.createStatement();
	 int rs;
	 SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy hh:mm:ss");
	 boolean mbool;
	 ResultSet rs2;
	 Sheet sheet = data.getSheet(4);
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
			rs = mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES ('".concat(sheet.getCell(0,i).getContents()).concat("',").concat(Integer.toString(nmedecin)).concat(",").concat("FALSE").concat(")"));

		}
		}
		}
 }
 
 public static void genplanning(Connection c, Workbook data,boolean hasint) throws ParseException, SQLException{
	 int prevurg,prevint,curg;
	 int newdowcount = 0;
	 Sheet mst = data.getSheet(3);
	 int repos = Integer.parseInt(mst.getCell(2,1).getContents());
	 Calendar cal = Calendar.getInstance();
	 prevurg = 666;
	 curg = 666;
	 prevint = 666;
	 int curgarde = 0;
	 SimpleDateFormat formatter = new SimpleDateFormat("dd MM yyyy hh:mm:ss");
	 Date datedebut,curdat,datefin;
	 Sheet msheet = data.getSheet(3);
	 datedebut = formatter.parse(msheet.getCell(0,1).getContents());
	 datefin = formatter.parse(msheet.getCell(1,1).getContents());
	 curdat = datedebut;
	 while(!curdat.after(datefin)){
		 System.out.println("prevurg = "+prevurg+" prevint = "+prevint);
		 String dowtoinc = getdow(curdat);
		 dunit garde = new dunit(666, dowtoinc, dowtoinc, curgarde, curgarde, curgarde, curgarde, true);
			if(hasint){
				garde.medundefined = true;
			garde = selecttoubib(repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,curdat,true,dowtoinc,formatter);
			curg = garde.curg;
			System.out.println("curg = "+curg);
			if(garde.medundefined){
				System.out.println("arr outta luck");
				break;
			}
			dorecord(c,garde,true,formatter);
			}
		 garde = selecttoubib(repos,curg,prevurg,prevint,true,newdowcount,curgarde,c,curdat,false,dowtoinc,formatter);
		dorecord(c,garde,false,formatter);
		prevurg = garde.curg;
		prevint = curg;
		cal.setTime(curdat);
		cal.add(Calendar.DATE, 1);
		curdat = cal.getTime();
	 }
	 
 }
 
 public static boolean dateferiee(Date curdat, Connection c,SimpleDateFormat fmt) throws SQLException{
	 Statement ms = c.createStatement();
	 ResultSet rs = ms.executeQuery("SELECT * FROM JOURS_FERIES WHERE JOUR='".concat(fmt.format(curdat)).concat("'"));
	 while(rs.next()){
		 System.out.println("c ferie");
		 return true;
	 }
	 return false;
 }
 
 public static dunit selferie(Connection c,Date curdat, dunit garde,String dowtoinc,boolean interieur,SimpleDateFormat fmt) throws SQLException{
	 Statement ms= c.createStatement();
	 Statement ms2 = c.createStatement();
	 ResultSet rs,rs2;
	 dunit res = garde;
	 if(!interieur){
		 System.out.println("doing not interieur after férié dowtoinc is".concat(dowtoinc));
		 rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE as DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(" as "+dowtoinc+",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON NUMERO = JF.NUMERO WHERE JF.JOUR = '").concat(fmt.format(curdat)).concat("' and JF.INTERIEUR = FALSE"));
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
			 res.jour = fmt.format(curdat);
			 res.dowtoinc = dowtoinc;
			 res.ferie = true;
			 res.curg = rs.getInt("SERVICE");
			 System.out.println("chose toubib number "+res.nmed+" for jour ferie interieur ".concat(fmt.format(curdat)));
			 return res;
		 }
	 }
	 else{
		 System.out.println("doing else after férié");
		 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO WHERE JF.JOUR = '").concat(fmt.format(curdat)).concat("' and JF.INTERIEUR = TRUE"));
	 
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
		 res.jour = fmt.format(curdat);
		 res.dowtoinc = dowtoinc;
			 res.curg = rs.getInt("SERVICE");
		 System.out.println("returning toubib number ".concat(Integer.toString(res.nmed)).concat("for date").concat(res.jour));
		 return res;
	 }
 }
	 res.medundefined = true;
	 return res;
 }

 public static boolean isgtg(int curg,int prevint,int prevurg,Connection c,Date curdat, SimpleDateFormat fmt,ResultSet rs,String dowtoinc,boolean interieur,int repos) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms3 = c.createStatement();
	 ResultSet rs2;
	 ResultSet rs3;
	 boolean gtg =  true;
	 rs2=ms2.executeQuery("SELECT DATEDEBUT,DATEFIN FROM IMPOSSIBILITES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs2.next()){
		 if(curdat.after(fmt.parse(rs2.getString("DATEDEBUT"))) && curdat.before(fmt.parse(rs2.getString("DATEFIN")))){
			 gtg = false;
			 System.out.println("c'est dans les vacances de ".concat(Integer.toString(rs.getInt("NUMERO"))));
			 
			 break;
		 }
	 }
	 if(!gtg){
		 System.out.println("not gtg : vacances");
	 }
	 rs3 = ms3.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 int nbdays = Days.daysBetween(new org.joda.time.DateTime(fmt.parse(rs.getString("DERNIEREGARDE"))), new org.joda.time.DateTime(curdat)).getDays();
	 while(rs3.next()){
		 int daysbf = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(fmt.parse(rs3.getString("JOUR")))).getDays();
		 if(daysbf < 0){
			 daysbf = daysbf*(-1);
		 }
		 gtg = gtg && (daysbf > repos) && ((nbdays > repos)||(nbdays < 0));
		 if(!gtg){
			 System.out.println("not gtg : number of days jours feries DAYSBF = "+daysbf+"nbdays = "+nbdays+"repos = "+repos);
		 }
	 }
	 System.out.println(Integer.toString(nbdays));
	 gtg = gtg && ((nbdays > repos)||(nbdays < 0));
	 if(!gtg){
	 System.out.println("not gtg : number of days repos = ".concat(Integer.toString(nbdays)));
	 }
	 if (rs.getInt("NBSEMESTRES") == 0){
		
		
		if(interieur){
			gtg = gtg && (rs.getInt("SERVICE")!=curg);
		}
		if(!gtg){
			System.out.println("not gtg : nbsemestre 0");
		}
	 }
	 else if(rs.getInt("NBSEMESTRES")== 3 ||rs.getInt("NBSEMESTRES")== 4 ){
		 gtg = gtg && (rs.getInt("NBGARDES") < 5) && (nbdays > repos);
		 if(!gtg){
				System.out.println("not gtg : nbsemestre 3");
	}
		 if(dowtoinc == "NBJEUDI"){
			 gtg = gtg && (rs.getInt(dowtoinc) == 0);
			 if(!gtg){
					System.out.println("not gtg : nbjeudi 3");
		}
		 }
		 else if(dowtoinc == "NBVENDREDI"){
		
			 gtg = gtg && (rs.getInt(dowtoinc)==0);
			 if(!gtg){
					System.out.println("not gtg 3 nbvendredi");
		}
		 }
		 else if (dowtoinc == "NBDIMANCHE"){
			 gtg = gtg && (rs.getInt(dowtoinc) == 0);
			 if(!gtg){
					System.out.println("not gtg 3 nbdimanche");
		}
		 }
		 else if(dateferiee(curdat,c,fmt)&&dowtoinc!="NBDIMANCHE"){
			 gtg = gtg && (rs.getInt("NBDIMANCHE") == 0);
			 if(!gtg){
					System.out.println("not gtg 3 jour ferie");
		}
		 }
		 if(interieur){
			 gtg = gtg && rs.getInt("SERVICE") != curg;
			 if(!gtg){
					System.out.println("not gtg interieur = curg");
		}
		 }
	 }
	 else if(rs.getInt("NBSEMESTRES") >= 5){
		 gtg = gtg && rs.getInt("NBGARDES") < 3 && !dateferiee(curdat,c,fmt);
		 if(!gtg){
				System.out.println("not gtg 5 nbgardes");
	}
		 if((dowtoinc == "NBVENDREDI")||(dowtoinc == "NBSAMEDI")||(dowtoinc=="NBDIMANCHE")){
			 gtg = false;
		 }
		 if(interieur){
			 gtg = gtg && rs.getInt("SERVICE") != curg;
			 if(!gtg){
					System.out.println("not gtg 5 service =  curg");
		}
		 
		 }
		 gtg = gtg && (rs.getInt("SERVICE") != prevurg) && (rs.getInt("SERVICE")!= prevint);
		 if(!gtg){
				System.out.println("not gtg 5 service = prevurg or prevint");
	}
	 }
	
	 return gtg;
 }
 
 public static boolean isreserved(boolean interieur,Connection c,Date curdat, SimpleDateFormat fmt) throws SQLException{
	 boolean itis = false;
	 Statement ms = c.createStatement();
	 ResultSet rs2 = ms.executeQuery("SELECT NUMERO FROM JOURS_FERIES WHERE JOUR = '".concat(fmt.format(curdat))+"' AND INTERIEUR = "+interieur);
	 while(rs2.next()){
		 itis = true;
	 }
	 if(itis){
		 System.out.println("c'est reserve");
	 }
	 return itis;
 }
 
 public static dunit selecttoubib(int repos, int curg,int prevurg, int prevint,boolean medundefined,int newdowcount,int curgarde,Connection c,Date curdat,boolean interieur,String dowtoinc,SimpleDateFormat fmt) throws SQLException, ParseException{
	 Statement ms2 = c.createStatement();
	 Statement ms = c.createStatement();
	 boolean ferie = false;
	 dunit res = new dunit(curgarde, dowtoinc, dowtoinc, curgarde, curgarde, curgarde, curgarde, true);
	 ResultSet rs,rs2;
	 if(dateferiee(curdat,c,fmt)){
		 if(isreserved(interieur,c,curdat,fmt)){
		 res = selferie(c,curdat,res,dowtoinc,interieur,fmt);
		 if(!res.medundefined){
			 System.out.println("returning reserver "+res.nmed);
			 return res;
		 }
		 }
	 }
		 System.out.println(fmt.format(curdat).concat(" n'est pas férié"));
		 if(!interieur){
			 rs=ms.executeQuery("SELECT NUMERO, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBSEMESTRES, NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> "+curg+" AND MEDECINS.SERVICE <> "+prevurg+" AND MEDECINS.SERVICE <> "+prevint+" ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
		 }
		 else{
			 rs=ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES, M.".concat(dowtoinc).concat(", M.NBSEMESTRES, M.NBJEUDI, M.NBVENDREDI, M.NBSAMEDI, M.NBDIMANCHE, M.NBFERIES, M.SERVICE FROM (MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) WHERE S.INTERIEUR = TRUE AND M.SERVICE <> "+prevurg+" AND M.SERVICE <> "+prevint+" ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
		 } 
		 while(rs.next()){
			 		 if((rs.getInt("SERVICE") == prevurg) || (rs.getInt("SERVICE") == prevint)||(interieur && rs.getInt("SERVICE") == curg)){
			 			 System.out.println("problem : prevurg = "+prevurg+" prevint = "+prevint+" curg = "+curg+" rs.getint(service)= "+rs.getInt("SERVICE"));
			 			 continue;
			 		 }
					 if(isgtg(curg,prevint,prevurg,c,curdat,fmt,rs,dowtoinc,interieur,repos)){
						 res.curgarde = rs.getInt("NBGARDES")+1;
						 res.newdowcount = rs.getInt(dowtoinc)+1;
						 res.medundefined = false;
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
						 res.jour = fmt.format(curdat);
				
							 res.curg = rs.getInt("SERVICE");
						 
						 
						res.nmed = rs.getInt("NUMERO");
						System.out.println("Returning toubib number "+res.nmed);
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

public static void dorecord(Connection c, dunit garde,boolean interieur,SimpleDateFormat fmt) throws SQLException, ParseException{
	System.out.println("toubib "+garde.nmed+" nbgarde = "+garde.curgarde); 
	Statement ms = c.createStatement();
	 Calendar cal = Calendar.getInstance();
	 cal.setTime(fmt.parse(garde.jour));
	 java.sql.Date sqldate = new java.sql.Date(cal.getTime().getTime());
	 int rs = ms.executeUpdate("UPDATE MEDECINS set DERNIEREGARDE = '".concat(garde.jour).concat("' WHERE NUMERO = ").concat(Integer.toString(garde.nmed)));
	 if(garde.ferie){
		 int newf = garde.nbferies + 1;
		 rs = ms.executeUpdate("UPDATE MEDECINS SET NBFERIES = "+newf+" WHERE NUMERO = "+garde.nmed);
	 }
	 rs = ms.executeUpdate("update MEDECINS set ".concat(garde.dowtoinc).concat(" = ").concat(Integer.toString(garde.newdowcount)).concat("where NUMERO = ").concat(Integer.toString(garde.nmed)));
	 rs=ms.executeUpdate("UPDATE MEDECINS set NBGARDES = ".concat(Integer.toString(garde.curgarde)).concat("WHERE NUMERO = ").concat(Integer.toString(garde.nmed)));
	 if(!interieur){
		 rs = ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(garde.nmed))+" WHERE JOUR = '"+sqldate+"'");
	 }
	 else{
		 rs = ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR) VALUES('"+sqldate+"',".concat(Integer.toString(garde.nmed)).concat(")"));
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
	ResultSet rs = mst.executeQuery("SELECT G.JOUR AS JOUR, S.NOM AS S1,s2.NOM AS S2N FROM(((GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO)INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO)INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO)INNER JOIN SERVICES AS S2 ON M2.SERVICE = S2.NUMERO");
	int i = 1;
	Label l1,l2,l3;
	l1 = new Label(0,0,"Date");
	l2 = new Label(1,0,"Urgences");
	l3 = new Label(2,0,"Interieur");
	ms.addCell(l3);
	ms.addCell(l1);
	ms.addCell(l2);
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("JOUR"));
		l2 = new Label(1,i,rs.getString("S1"));
		l3 = new Label(2,i,rs.getString("S2N"));
		ms.addCell(l3);
		ms.addCell(l1);
		ms.addCell(l2);
		i++;
	}
}

public static void writegardes(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("planning", 0);
	Statement mst = c.createStatement();
	ResultSet rs = mst.executeQuery("SELECT G.JOUR AS JOUR, M.NOM AS M1, M2.NOM AS M2N FROM (GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO ) INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO ORDER BY JOUR ASC");
	Label l1,l2,l3;
	int i = 1;
	l1 = new Label(0,0,"date");
	l2 = new Label(1,0,"urgence");
	l3 = new Label(2,0,"interieur");
	ms.addCell(l1);
	ms.addCell(l2);
	ms.addCell(l3);
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("JOUR"));
		l2 = new Label(1,i,rs.getString("M1"));
		l3 = new Label(2,i,rs.getString("M2N"));
		ms.addCell(l1);
		ms.addCell(l2);
		ms.addCell(l3);
		i++;
	}
}

public static void writestats(Connection c, WritableWorkbook output,boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException{
	WritableSheet ms = output.createSheet("stats", 1);
	Statement mst = c.createStatement();
	ResultSet rs = mst.executeQuery("SELECT M.NOM AS NOM, M.NBSEMESTRES AS SEMESTRE, M.NBGARDES AS NBGARDES, S.NOM AS service FROM MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO order by SERVICE ASC, SEMESTRE ASC, NOM ASC");
	Label l1,l2,l3,l4;
	l1 = new Label(0,0,"nom");
	l2 = new Label(1,0,"score");
	l3 = new Label(2,0,"nbgardes");
	l4 = new Label(3,0,"service");
	ms.addCell(l1);
	ms.addCell(l2);
	ms.addCell(l3);
	ms.addCell(l4);
	int i = 1;
	while(rs.next()){
		l1 = new Label(0,i,rs.getString("NOM"));
		l2 = new Label(1,i,Integer.toString(rs.getInt("SEMESTRE")));
		l3 = new Label(2,i,Integer.toString(rs.getInt("NBGARDES")));
		l4 = new Label(3,i,rs.getString("service"));
		ms.addCell(l1);
		ms.addCell(l2);
		ms.addCell(l3);
		ms.addCell(l4);
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
		rs2 = ms1.executeQuery("SELECT JOUR,URGENCES,INTERIEUR FROM GARDES WHERE URGENCES = "+rs.getInt("NUMERO")+" OR INTERIEUR = "+rs.getInt("NUMERO"));
		while (rs2.next()){
			l1 = new Label(0,j,rs2.getString("JOUR"));
			if(rs2.getInt("INTERIEUR")==rs.getInt("NUMERO")){
				l2 = new Label(1,j,"interieur");
			}
			else{
				l2 = new Label(1,j,"service");
			}
			ms.addCell(l1);
			ms.addCell(l2);
			j++;
		}
		i++;
	}
}

}
