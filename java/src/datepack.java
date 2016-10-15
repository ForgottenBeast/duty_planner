
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import org.joda.time.DateTime;
import org.joda.time.Days;

public class datepack {
    Date upto = null;
    Date goal = null;
    dunit garde = new dunit();
    String error = "none";
    int nbdays;
    int daysbf;

    public datepack(Connection c, Workbook data, boolean hasint) throws ParseException, SQLException {
        byte newdowcount = 0;
        Sheet mst = data.getSheet(3);
        int prevurg = 666;
        int curg = 666;
        int prevint = 666;
        byte curgarde = 0;
        Sheet msheet = data.getSheet(3);
        DateCell dc1 = (DateCell)msheet.getCell(0, 1);
        DateCell dc2 = (DateCell)msheet.getCell(1, 1);
        this.upto = scan.tosql(dc1.getDate());
        this.goal = scan.tosql(dc2.getDate());
        System.out.println("creating new datepack, going from " + dc1.getDate() + " to " + dc2.getDate() + "\n current upto = " + this.upto + " current goal = " + this.goal);

        while(!this.upto.after(this.goal)) {
            System.out.println("generating for " + this.upto);
            String dowtoinc = scan.getdow(this.upto);
            if(hasint) {
                this.garde.medundefined = true;
                this.selecttoubib(hasint, curg, prevurg, prevint, true, newdowcount, curgarde, c, true, dowtoinc);
                curg = this.garde.curg;
                if(this.garde.medundefined) {
                    System.out.println("breaking cause medundefined");
                    break;
                }

                try {
                    scan.dorecord(this, c, true, hasint);
                } catch (SQLException var15) {
                    System.out.println("caught sql exception while recording: " + var15.getMessage());
                } catch (ParseException var16) {
                    System.out.println("caught parsexection: " + var16.getMessage());
                }
            } else {
                prevurg = 666;
            }

            this.selecttoubib(hasint, curg, prevurg, prevint, true, newdowcount, curgarde, c, false, dowtoinc);
            if(this.garde.medundefined) {
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
            if(hasint) {
                scan.equilibrer(c, true);
                scan.equilibrer(c, false);
            } else {
                scan.equilibrer(c, false);
            }
        }

    }

    private void selecttoubib(boolean hasint, int curg, int prevurg, int prevint, boolean medundefined, int newdowcount, int curgarde, Connection c, boolean interieur, String dowtoinc) throws SQLException, ParseException {
        Date curdat = this.upto;
        Statement ms2 = c.createStatement();
        Statement ms = c.createStatement();
        int nextint = 0;
        boolean ferie = false;
        this.garde.medundefined = true;
        System.out.println("entering if");
        ResultSet rs;
        if(scan.dateferiee(curdat, c)) {
            System.out.println("date est ferie");
            if(scan.isreserved(hasint, interieur, c, curdat)) {
                System.out.println(curdat + " est reserve");
                this.selferie(hasint, c, curdat, dowtoinc, interieur);
            }

            if(scan.dateferiee(scan.nextday(curdat), c) && (scan.isreserved(hasint, interieur, c, scan.nextday(curdat)) || scan.isreserved(hasint, !interieur, c, scan.nextday(curdat)))) {
                rs = ms.executeQuery("SELECT SERVICE,JOUR FROM MEDECINS AS M INNER JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO");
                System.out.println(scan.nextday(curdat) + "est reserve, donc attention a " + curdat);

                while(rs.next()) {
                    DateTime maxnbdays = new DateTime(rs.getDate("JOUR"));
                    DateTime maxbfdays = new DateTime(scan.nextday(curdat));
                    Days isgood = Days.daysBetween(maxnbdays, maxbfdays);
                    if(isgood.getDays() == 0) {
                        nextint = rs.getInt("SERVICE");
                    }
                }
            }
        }

        if(this.garde.medundefined) {
            if(!interieur) {
                System.out.println("selecttoubib branche !interieur");
                if(hasint) {
                    rs = ms.executeQuery("SELECT NUMERO,NOM, DERNIEREGARDE, NBGARDES, ".concat(dowtoinc).concat(", NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> " + curg + " AND MEDECINS.SERVICE <> " + prevurg + " AND MEDECINS.SERVICE <> " + prevint + " AND MEDECINS.SERVICE <> " + nextint + " ORDER BY NBGARDES ASC, ").concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
                } else {
                    rs = ms.executeQuery("SELECT NUMERO,NOM, DERNIEREGARDE, NBGARDES, " + dowtoinc + ",NBJEUDI, NBVENDREDI, NBSAMEDI, NBDIMANCHE, NBFERIES, SERVICE FROM MEDECINS join SERVICES ON MEDECINS.SERVICE = SERVICES.NUMERO WHERE MEDECINS.SERVICE <> " + curg + " AND MEDECINS.SERVICE <> " + prevurg + " AND MEDECINS.SERVICE <> " + prevint + " ORDER BY NBGARDES ASC, ".concat(dowtoinc).concat(" ASC,DERNIEREGARDE ASC"));
                }
            } else {
                rs = ms.executeQuery("SELECT MED.NUMERO as NUMERO,MED.NOM as NOM, MED.DERNIEREGARDE, MED.NBGARDES as NBGARDES, MED." + dowtoinc + ", MED.NBJEUDI, MED.NBVENDREDI, MED.NBSAMEDI, MED.NBDIMANCHE, MED.NBFERIES, MED.SERVICE FROM (MEDECINS AS MED INNER JOIN SERVICES AS S ON MED.SERVICE = S.NUMERO) WHERE S.INTERIEUR = TRUE AND MED.SERVICE <> " + prevurg + " AND MED.SERVICE <> " + prevint + " AND MED.SERVICE <> " + nextint + " ORDER BY NBGARDES ASC, MED." + dowtoinc + " ASC, MED.DERNIEREGARDE ASC");
            }

            int maxnbdays1 = 0;
            int maxbfdays1 = 0;

            label91:
            while(true) {
                while(true) {
                    if(!rs.next()) {
                        break label91;
                    }

                    System.out.println("now examining " + rs.getString("NOM"));
                    if(rs.getInt("SERVICE") != prevurg && rs.getInt("SERVICE") != prevint && (!interieur || rs.getInt("SERVICE") != curg) && (rs.getInt("SERVICE") != nextint || !hasint)) {
                        gtg isgood1 = new gtg(curg, prevint, prevurg, c, curdat, rs, dowtoinc, interieur, false);
                        if(isgood1.gtg) {
                            System.out.println(rs.getString("NOM") + " has been selected for " + curdat);
                            this.garde.ferie = scan.dateferiee(curdat, c);
                            if(ferie) {
                                int nbferie = 0;

                                for(ResultSet rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = " + rs.getInt("NUMERO")); rs2.next(); nbferie = rs2.getInt("NBFERIES")) {
                                    ;
                                }

                                this.garde.nbferies = nbferie;
                            }

                            this.garde.dowtoinc = dowtoinc;
                            this.garde.jour = new Date(curdat.getTime());
                            this.garde.curg = rs.getInt("SERVICE");
                            this.garde.nmed = rs.getInt("NUMERO");
                            this.garde.curgarde = rs.getInt("NBGARDES") + 1;
                            this.garde.newdowcount = rs.getInt(dowtoinc) + 1;
                            this.garde.medundefined = false;
                            this.upto = curdat;
                            break label91;
                        }

                        this.error = isgood1.error_message;
                        if(isgood1.error == 1 || isgood1.error == 2) {
                            System.out.println("maxnbdays and bfdas to be set, error is " + isgood1.error + " currently " + maxnbdays1 + " \n" + maxbfdays1);
                            if(maxnbdays1 < isgood1.nbdays) {
                                maxnbdays1 = isgood1.nbdays;
                            }

                            if(maxbfdays1 < isgood1.daysbf) {
                                maxbfdays1 = isgood1.daysbf;
                            }

                            System.out.println("now " + maxnbdays1 + "\n" + maxbfdays1);
                        }

                        System.out.println(rs.getString("NOM") + " n\'a pas été séléctionné pour cause de " + isgood1.error_message);
                    } else {
                        System.out.println("continuing because of hasint");
                    }
                }
            }

            this.nbdays = maxnbdays1;
            this.daysbf = maxbfdays1;
        }

    }

    private void selferie(boolean hasint, Connection c, Date curdat, String dowtoinc, boolean interieur) throws SQLException {
        Statement ms = c.createStatement();
        Statement ms2 = c.createStatement();
        ResultSet rs;
        ResultSet rs2;
        Date madate;
        int nbferie;
        if(!interieur) {
            System.out.println("selferie !interieur branch");
            madate = new Date(curdat.getTime());
            if(hasint) {
                rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE as DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(" as " + dowtoinc + ",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON NUMERO = JF.NUMERO WHERE JF.JOUR = \'" + madate + "\' and JF.INTERIEUR = FALSE"));
            } else {
                rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE as DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(" as " + dowtoinc + ",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON NUMERO = JF.NUMERO WHERE JF.JOUR = \'" + madate + "\'"));
            }

            while(rs.next()) {
                nbferie = 0;

                for(rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = " + rs.getInt("NUMERO")); rs2.next(); nbferie = rs2.getInt("NBFERIES")) {
                    ;
                }

                this.garde.nmed = rs.getInt("NUMERO");
                this.garde.nbferies = nbferie;
                this.garde.curgarde = rs.getInt("NBGARDES") + 1;
                this.garde.newdowcount = rs.getInt(dowtoinc) + 1;
                this.garde.medundefined = false;
                this.garde.jour = curdat;
                this.upto = curdat;
                this.garde.dowtoinc = dowtoinc;
                this.garde.ferie = true;
                this.garde.curg = rs.getInt("SERVICE");
            }
        } else {
            madate = new Date(curdat.getTime());

            for(rs = ms.executeQuery("SELECT M.NUMERO as NUMERO, M.DERNIEREGARDE, M.NBGARDES as NBGARDES,M.".concat(dowtoinc).concat(",SERVICE FROM MEDECINS AS M JOIN JOURS_FERIES AS JF ON M.NUMERO = JF.NUMERO WHERE JF.JOUR = \'" + madate + "\' and JF.INTERIEUR = TRUE")); rs.next(); this.garde.curg = rs.getInt("SERVICE")) {
                nbferie = 0;

                for(rs2 = ms2.executeQuery("SELECT NBFERIES FROM MEDECINS WHERE NUMERO = " + rs.getInt("NUMERO")); rs2.next(); nbferie = rs2.getInt("NBFERIES")) {
                    ;
                }

                this.garde.nmed = rs.getInt("NUMERO");
                this.garde.curgarde = rs.getInt("NBGARDES") + 1;
                this.garde.newdowcount = rs.getInt(dowtoinc) + 1;
                this.garde.medundefined = false;
                this.upto = curdat;
                this.garde.ferie = true;
                this.garde.nbferies = nbferie;
                this.garde.jour = new Date(curdat.getTime());
                this.garde.dowtoinc = dowtoinc;
            }
        }

    }
}