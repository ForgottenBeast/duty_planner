import org.joda.time.Days;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

public class gtg {
    String error;
    boolean gtg;

  public gtg(int curg,int prevint,int prevurg,Connection c,java.sql.Date curdat,
             ResultSet rs, String dowtoinc,
			 boolean interieur,boolean equilibrage) throws SQLException, ParseException {
     this.error = "none";
     this.gtg = true;
	 Statement ms4 = c.createStatement();
	 Statement ms2 = c.createStatement();
	 Statement ms3 = c.createStatement();
	 Statement ms5 = c.createStatement();
	 ResultSet rs4;
	 ResultSet rs2;
	 ResultSet rs3,rs5;
	 boolean bftest = true;
	 boolean gtg =  true;
	 java.sql.Date prevdat = curdat;
	 boolean inoptions = false;
	 int joursuivants;
	 int repos;
	 if(equilibrage == true){
		 if(!interieur){
			 rs5 = ms5.executeQuery("SELECT G.JOUR,S.REPOS FROM GARDES as G INNER JOIN(MEDECINS AS M INNER JOIN " +
                     "SERVICES AS S ON M.SERVICE = S.NUMERO) ON G.URGENCES = M.NUMERO WHERE G.URGENCES = "
                     +Integer.toString(rs.getInt("NUMERO"))+" ORDER BY JOUR ASC");
		 }
		 else{
			 rs5 = ms5.executeQuery("SELECT G.JOUR,S.REPOS FROM GARDES AS G INNER JOIN(MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) ON G.INTERIEUR = M.NUMERO WHERE G.INTERIEUR = "+Integer.toString(rs.getInt("NUMERO"))+" ORDER BY JOUR ASC");
		 }
		 while(rs5.next()){
			 repos = rs5.getInt("REPOS");
			 if(prevdat != curdat){
				 joursuivants = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(rs5.getDate("JOUR"))).getDays();

				 if(joursuivants < 0){
					 joursuivants *= -1;
				 }
				 if(joursuivants >= repos){
					 this.gtg = true;
				 }
				 else{
					 prevdat = curdat;
				 }
			 }
			 else if(Days.daysBetween(new org.joda.time.DateTime(rs5.getDate("JOUR")),new org.joda.time.DateTime(curdat)).getDays() >= repos){

				prevdat = rs5.getDate("JOUR");
			 }

		 }
		 this.gtg = false;

	 }
	 rs4 = ms4.executeQuery("SELECT NUMERO, NBTOTAL, NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE FROM OPTIONS WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs4.next()){

		inoptions = true;
	 }
	 rs2=ms2.executeQuery("SELECT DATEDEBUT,DATEFIN FROM IMPOSSIBILITES WHERE NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 while(rs2.next()){

		 if((curdat.after(rs2.getDate("DATEDEBUT")) && curdat.before(rs2.getDate("DATEFIN"))) || ((curdat.compareTo(rs2.getDate("DATEDEBUT")) == 0) || (curdat.compareTo(rs2.getDate("DATEFIN"))==0))){
			 this.gtg = false;
			 this.error = "Toutes les personnes pouvant prendre des gardes conformément au nombre de nours de repos sont en vacances";

			 break;
		 }
	 }

	 rs3 = ms3.executeQuery("SELECT JOUR,REPOS FROM JOURS_FERIES INNER JOIN (MEDECINS INNER JOIN SERVICES ON SERVICE = SERVICES.NUMERO) ON JOURS_FERIES.NUMERO = MEDECINS.NUMERO WHERE MEDECINS.NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
	 int nbdays = Days.daysBetween(new org.joda.time.DateTime(rs.getDate("DERNIEREGARDE")), new org.joda.time.DateTime(curdat)).getDays();
	 if(nbdays < 0 && equilibrage == false){
		 nbdays = nbdays*(-1);
	 }
	 while(rs3.next()){

		 repos = rs3.getInt("REPOS");
         int daysbf = Days.daysBetween(new org.joda.time.DateTime(curdat), new org.joda.time.DateTime(rs3.getDate("JOUR"))).getDays();
         if(daysbf < 0){
                 daysbf = daysbf*(-1);
         }
         bftest = this.gtg;
         this.gtg = this.gtg && (daysbf > repos) && ((nbdays > repos)||(nbdays < 0));
         if(bftest && !this.gtg){
                System.out.println("error 1 gtg");
                 this.error = "Diminuez le nombre de jours de repos du service";
         }
 }

	 if(equilibrage == false){
	bftest = this.gtg;
	rs3 = ms3.executeQuery("SELECT REPOS FROM SERVICES AS S INNER JOIN MEDECINS AS M ON M.SERVICE = S.NUMERO WHERE M.NUMERO = "+rs.getString("NUMERO"));
	while(rs3.next()){

		repos = rs3.getInt("REPOS");
		 this.gtg = this.gtg && ((nbdays > repos)||(nbdays < 0));
		 if(bftest && !this.gtg){
             System.out.println("error 2 gtg");
			 this.error = "Diminuez le nombre de jours de repos du service";
		 }
	 }

	 }
	 else{
		 if(!interieur){
			 rs5 = ms5.executeQuery("SELECT G.JOUR, S.REPOS FROM GARDES AS G INNER JOIN (MEDECINS AS M INNER JOIN " +
                     "SERVICES AS S ON M.SERVICE = S.NUMERO) ON G.URGENCES = M.NUMERO WHERE G.URGENCES = "+
                     Integer.toString(rs.getInt("NUMERO")));
		 }
		 else{
			 rs5 = ms5.executeQuery("SELECT G.JOUR,S.REPOS FROM GARDES AS G INNER JOIN(MEDECINS AS M INNER JOIN " +
                     "SERVICES AS S ON M.SERVICE = S.NUMERO) ON G.URGENCES = M.NUMERO WHERE G.INTERIEUR = "+
                     Integer.toString(rs.getInt("NUMERO")));

		 }
		 while(rs5.next()){
			 repos = rs5.getInt("S.REPOS");
			 if(prevdat != curdat){
				 if(Days.daysBetween(new org.joda.time.DateTime(curdat),
                         new org.joda.time.DateTime(rs5.getDate("JOUR"))).getDays() >= repos){
					 this.gtg = this.gtg && true;
					 break;
				 }
				 else{
					 prevdat = curdat;
				 }
			 }
			 if(Days.daysBetween(new org.joda.time.DateTime(rs5.getDate("JOUR")),new org.joda.time.DateTime(curdat) ).getDays() >= repos){
				prevdat = rs5.getDate("JOUR");
			 }

		 }
		 if(prevdat == curdat){
			 this.gtg = false;
		 }
	 }
		if(interieur){
			bftest  = this.gtg;
			this.gtg = this.gtg && (rs.getInt("SERVICE")!=curg);
			if(bftest && !this.gtg){
				 this.error = "meme service que les urgences ce jour";
			 }
			 bftest = this.gtg;
			 this.gtg = this.gtg && (rs.getInt("SERVICE") != prevurg) && (rs.getInt("SERVICE")!= prevint);
			 if(bftest && !this.gtg){
				 this.error = "meme service que ceux de garde la veille";
			 }

		}

		if(inoptions){
			rs4 = ms4.executeQuery("SELECT REPOS,NUMERO, NBTOTAL, NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE FROM OPTIONS as O INNER JOIN(MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) ON O.NUMERO = M.NUMERO WHERE O.NUMERO = ".concat(Integer.toString(rs.getInt("NUMERO"))));
			while(rs4.next()){
				repos = rs4.getInt("REPOS");
			bftest = this.gtg;
			this.gtg = this.gtg && (rs.getInt("NBGARDES") < rs4.getInt("NBTOTAL")) && (nbdays > repos);
			if(bftest && !this.gtg){
				this.error = "medecin dans les options, plus de gardes que nbtotal";
			}
			bftest = this.gtg;
			this.gtg = this.gtg && (rs.getInt(dowtoinc) < rs4.getInt(dowtoinc)) && (nbdays > repos);
			if(bftest && !this.gtg){
				this.error = "medecin dans les options, plus de "+dowtoinc+" que attribué dans les options";
			}
			if(scan.dateferiee(curdat,c)){
				bftest = this.gtg;
				this.gtg = this.gtg && (nbdays > repos);
				if(bftest && !this.gtg){
					this.error = "medecin dans les options, plus de feries qu'attribué dans les options";
				}
			}
			}

		}
     }

}
