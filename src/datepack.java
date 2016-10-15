import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Map;


public class datepack {

	Date upto;
	Date goal;
	dunit garde;
	String error;
	int nbdays;
	int daysbf;

	public datepack(Connection c, Workbook data, boolean hasint) throws ParseException,SQLException{
		this.upto = null;
		this.goal = null;
		this.garde = new dunit();
		this.error = "none";
		int prevurg, prevint, curg;
		int newdowcount = 0;
		Sheet mst = data.getSheet(3);
		DateCell dc1, dc2;
		prevurg = 666;
		curg = 666;
		prevint = 666;
		int curgarde = 0;
		Sheet msheet = data.getSheet(3);
		dc1 = (DateCell) msheet.getCell(0, 1);
		dc2 = (DateCell) msheet.getCell(1, 1);
		this.upto = scan.tosql(dc1.getDate());
		this.goal = scan.tosql(dc2.getDate());
		System.out.println("creating new datepack, going from "+dc1.getDate()+" to "+dc2.getDate()+
		"\n current upto = "+this.upto+" current goal = "+this.goal);

		while (!this.upto.after(this.goal)) {
			System.out.println("generating for " + this.upto);
			String dowtoinc = scan.getdow(this.upto);
			if (hasint) {
				this.garde.medundefined = true;
				this.selecttoubib(hasint, curg, prevurg, prevint, true, newdowcount, curgarde, c, true, dowtoinc);
				curg = this.garde.curg;
				if (this.garde.medundefined) {
					System.out.println("breaking cause medundefined");
					break;
				}
				try {
					scan.dorecord(this, c, true, hasint);
				} catch (SQLException x) {
					System.out.println("caught sql exception while recording: " + x.getMessage());
				} catch (ParseException e) {
					System.out.println("caught parsexection: " + e.getMessage());
				}
			}
			else {
				prevurg = 666;
			}
			this.selecttoubib(hasint, curg, prevurg, prevint, true, newdowcount, curgarde, c, false, dowtoinc);
			if (this.garde.medundefined) {
				System.out.println("breaking cause medundefined 2");
				break;
			}
			this.nbdays = 0;
			this.daysbf = 0;
			scan.dorecord(this, c, false, hasint);
			prevurg = this.garde.curg;
			prevint = curg;
			this.upto = scan.nextday(this.upto);
		}
		System.out.println("done generating\n");
		if(this.upto.after(this.goal)) {
			if (hasint) {
				scan.equilibrer(c, true);
				scan.equilibrer(c, false);
			} else {
				scan.equilibrer(c, false);
			}
		}
	}

	private void selecttoubib(boolean hasint, int curg, int prevurg, int prevint, boolean medundefined,
							  int newdowcount, int curgarde, Connection c,
							  boolean interieur, String dowtoinc) throws SQLException, ParseException {
		java.sql.Date curdat = this.upto;
		Statement ms2 = c.createStatement();
		Statement ms = c.createStatement();
		int nextint = 0;
		boolean ferie = false;
		ResultSet rs, rs2;
		this.garde.medundefined = true;
		System.out.println("entering if");
		if (scan.dateferiee(curdat, c)) {
			System.out.println("date est ferie");
			if (scan.isreserved(hasint, interieur, c, curdat)) {
				System.out.println(curdat + " est reserve");
				this.selferie(hasint, c, curdat, dowtoinc, interieur);
			}

			if (scan.dateferiee(scan.nextday(curdat), c)) {
				if (scan.isreserved(hasint, interieur, c, scan.nextday(curdat)) || scan.isreserved(hasint, !interieur, c, scan.nextday(curdat))) {
					rs = ms.executeQuery("SELECT SERVICE,JOUR FROM MEDECINS AS M INNER JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO");
					System.out.println(scan.nextday(curdat) + "est reserve, donc attention a " + curdat);
					while (rs.next()) {
						DateTime d1, d2;
						d1 = new DateTime(rs.getDate("JOUR"));
						d2 = new DateTime(scan.nextday(curdat));
						Days diff = Days.daysBetween(d1, d2);
						if (diff.getDays() == 0) {
							nextint = rs.getInt("SERVICE");
						}
					}
				}
			}
		}

		if(this.garde.medundefined) {

			if (!interieur) {
				System.out.println("selecttoubib branche !interieur");
				if (hasint) {
					rs = ms.executeQuery("SELECT NUMERO,NOM, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> " + curg + " AND MEDECINS.SERVICE <> " + prevurg + " AND MEDECINS.SERVICE <> " + prevint + " AND MEDECINS.SERVICE <> " + nextint + " ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
				} else {
					rs = ms.executeQuery("SELECT NUMERO,NOM, DERNIEREGARDE, NBGARDES, " + dowtoinc + ",NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> " + curg + " AND MEDECINS.SERVICE <> " + prevurg + " AND MEDECINS.SERVICE <> " + prevint + " ORDER BY NBGARDES ASC, ".concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
				}
			} else {

				System.out.println("selecttoubib branche interieur");

				rs = ms.executeQuery("SELECT MED.NUMERO as NUMERO,MED.NOM as NOM, MED.DERNIEREGARDE, MED.NBGARDES as NBGARDES, MED." + dowtoinc + ", MED.NBJEUDI, MED.NBVENDREDI, MED.NBSAMEDI, MED.NBDIMANCHE, MED.NBFERIES, MED.SERVICE FROM (MEDECINS AS MED INNER JOIN SERVICES AS S ON MED.SERVICE = S.NUMERO) WHERE S.INTERIEUR = TRUE AND MED.SERVICE <> " + prevurg + " AND MED.SERVICE <> " + prevint + " AND MED.SERVICE <> " + nextint + " ORDER BY NBGARDES ASC, MED." + dowtoinc + " ASC, MED.DERNIEREGARDE ASC");
			}
			int maxnbdays = 0;
			int maxbfdays = 0;
			while (rs.next()) {
				System.out.println("now examining " + rs.getString("NOM"));
				if ((rs.getInt("SERVICE") == prevurg) || (rs.getInt("SERVICE") == prevint) || (interieur && (rs.getInt("SERVICE") == curg)) || ((rs.getInt("SERVICE") == nextint)) && hasint) {
					System.out.println("continuing because of hasint");
					continue;
				}
				gtg isgood = new gtg(curg, prevint, prevurg, c, curdat, rs, dowtoinc, interieur, false);
				if (isgood.gtg) {
					System.out.println(rs.getString("NOM") + " has been selected for " + curdat);
					this.garde.ferie = scan.dateferiee(curdat, c);
					if (ferie) {
						int nbferie = 0;
						rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = " + rs.getInt("NUMERO"));
						while (rs2.next()) {
							nbferie = rs2.getInt("NBFERIES");
						}
						this.garde.nbferies = nbferie;
					}
					this.garde.dowtoinc = dowtoinc;
					this.garde.jour = new java.sql.Date(curdat.getTime());
					this.garde.curg = rs.getInt("SERVICE");
					this.garde.nmed = rs.getInt("NUMERO");
					this.garde.curgarde = rs.getInt("NBGARDES") + 1;
					this.garde.newdowcount = rs.getInt(dowtoinc) + 1;
					this.garde.medundefined = false;
					this.upto = curdat;
					break;
				}
				else {
					this.error = isgood.error_message;
					if(isgood.error == 1 || isgood.error == 2) {
						System.out.println("maxnbdays and bfdas to be set, error is "+isgood.error+" currently " +
								maxnbdays+" \n"+maxbfdays);
						if (maxnbdays < isgood.nbdays) {
							maxnbdays = isgood.nbdays;
						}
						if (maxbfdays < isgood.daysbf) {
							maxbfdays = isgood.daysbf;
						}
						System.out.println("now "+maxnbdays+"\n"+maxbfdays);
					}
					System.out.println(rs.getString("NOM")+ " n'a pas été séléctionné pour cause de "+isgood.error_message);
				}
			}
			this.nbdays = maxnbdays;
			this.daysbf = maxbfdays;
		}

}



	private void selferie(boolean hasint,Connection c, java.sql.Date curdat,String dowtoinc,boolean interieur)
			throws SQLException{
	 Statement ms= c.createStatement();
	 Statement ms2 = c.createStatement();
	 ResultSet rs,rs2;
	 if(!interieur){
		 System.out.println("selferie !interieur branch");
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
			 this.garde.nmed = rs.getInt("NUMERO");
			 this.garde.nbferies = nbferie;
			 this.garde.curgarde = rs.getInt("NBGARDES")+1;
			 this.garde.newdowcount = rs.getInt(dowtoinc)+1;
			 this.garde.medundefined = false;
			 this.garde.jour = curdat;
			 this.upto = curdat;
			 this.garde.dowtoinc = dowtoinc;
			 this.garde.ferie = true;
			 this.garde.curg = rs.getInt("SERVICE");
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
		 this.garde.nmed = rs.getInt("NUMERO");
		 this.garde.curgarde = rs.getInt("NBGARDES")+1;
		 this.garde.newdowcount = rs.getInt(dowtoinc)+1;
		 this.garde.medundefined = false;
		 this.upto = curdat;
		 this.garde.ferie = true;
		 this.garde.nbferies = nbferie;
		 this.garde.jour = new java.sql.Date(curdat.getTime());
		 this.garde.dowtoinc = dowtoinc;
         this.garde.curg = rs.getInt("SERVICE");
	 }
 }
 }

}
