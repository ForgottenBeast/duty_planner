import com.opencsv.CSVReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jxl.Cell;
import jxl.DateCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import jxl.write.DateFormats;
import jxl.write.DateTime;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import org.joda.time.DateTimeZone;

public class scan {
    public scan() {
    }

    public static void main(String[] args) throws SQLException, IOException, BiffException, RowsExceededException, WriteException, ParseException {
        Workbook data;
        WritableWorkbook workbook;
        if(args[0].equals("--xls")) {
            data = Workbook.getWorkbook(new File(args[1]));
            workbook = Workbook.createWorkbook(new File(args[2] + "_planning_garde.xls"));
        } else {
            reencode(args[0]);
            data = Workbook.getWorkbook(new File(args[0] + "_data.xls"));
            workbook = Workbook.createWorkbook(new File(args[0] + "_planning_garde.xls"));
        }

        Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:gardedb", "SA", "");
        boolean hasint = setup(c, data);
        filltables(c, data, hasint);
        datepack monpack = new datepack(c, data, hasint);
        writeoutput(monpack, c, workbook, hasint, data, args);
    }

    public static void reencode(String arg) throws IOException, RowsExceededException, WriteException, ParseException {
        WritableWorkbook workbook = Workbook.createWorkbook(new File(arg + "_data.xls"));
        int k = 1;
        Calendar cal = Calendar.getInstance();
        CSVReader reader = new CSVReader(new FileReader(arg));
        List myEntries = reader.readAll();
        WritableSheet ms = workbook.createSheet("medecins", 0);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        WritableCellFormat cf1 = new WritableCellFormat(DateFormats.FORMAT9);

        for(int i = 0; i < myEntries.size(); ++i) {
            Label l;
            DateTime dt;
            int j;
            if(((String[])myEntries.get(i))[0].equals("<medecins>")) {
                l = new Label(0, 0, "medecin");
                ms.addCell(l);
                l = new Label(1, 0, "service");
                ms.addCell(l);
                l = new Label(2, 0, "derniere garde");
                ms.addCell(l);

                for(j = i + 1; j < myEntries.size(); j += 3) {
                    if(((String[])myEntries.get(j))[0].equals("</medecins>")) {
                        i = j;
                        k = 1;
                        break;
                    }

                    l = new Label(0, k, ((String[])myEntries.get(j))[0]);
                    ms.addCell(l);
                    l = new Label(1, k, ((String[])myEntries.get(j + 1))[0]);
                    ms.addCell(l);
                    cal.setTime(formatter.parse(((String[])myEntries.get(j + 2))[0]));
                    dt = new DateTime(2, k, new Date(cal.getTimeInMillis()), cf1);
                    ms.addCell(dt);
                    ++k;
                }
            } else if(((String[])myEntries.get(i))[0].equals("<feries>")) {
                ms = workbook.createSheet("jours feries", 1);
                l = new Label(0, 0, "date");
                ms.addCell(l);
                l = new Label(1, 0, "nom");
                ms.addCell(l);
                l = new Label(2, 0, "interieur");
                ms.addCell(l);

                for(j = i + 1; j < myEntries.size(); j += 3) {
                    if(((String[])myEntries.get(j))[0].equals("</feries>")) {
                        i = j;
                        k = 1;
                        break;
                    }

                    cal.setTime(formatter.parse(((String[])myEntries.get(j))[0]));
                    dt = new DateTime(0, k, new Date(cal.getTimeInMillis()), cf1);
                    ms.addCell(dt);
                    l = new Label(1, k, ((String[])myEntries.get(j + 1))[0]);
                    ms.addCell(l);
                    if(((String[])myEntries.get(j + 2))[0].equals("true")) {
                        l = new Label(2, k, ((String[])myEntries.get(j + 2))[0]);
                        ms.addCell(l);
                    }

                    ++k;
                }
            } else if(((String[])myEntries.get(i))[0].equals("<vacances>")) {
                ms = workbook.createSheet("vacances", 2);
                l = new Label(0, 0, "date debut");
                ms.addCell(l);
                l = new Label(1, 0, "date fin");
                ms.addCell(l);
                l = new Label(2, 0, "nom");
                ms.addCell(l);

                for(j = i + 1; j < myEntries.size(); j += 3) {
                    if(((String[])myEntries.get(j))[0].equals("</vacances>")) {
                        i = j;
                        k = 1;
                        break;
                    }

                    cal.setTime(formatter.parse(((String[])myEntries.get(j))[0]));
                    dt = new DateTime(0, k, new Date(cal.getTimeInMillis()), cf1);
                    ms.addCell(dt);
                    cal.setTime(formatter.parse(((String[])myEntries.get(j + 1))[0]));
                    dt = new DateTime(1, k, new Date(cal.getTimeInMillis()), cf1);
                    ms.addCell(dt);
                    l = new Label(2, k, ((String[])myEntries.get(j + 2))[0]);
                    ms.addCell(l);
                    ++k;
                }
            } else if(((String[])myEntries.get(i))[0].equals("<info>")) {
                ms = workbook.createSheet("informations generales", 3);
                l = new Label(0, 0, "date debut");
                ms.addCell(l);
                l = new Label(1, 0, "date fin");
                ms.addCell(l);
                ++i;
                cal.setTime(formatter.parse(((String[])myEntries.get(i))[0]));
                dt = new DateTime(0, 1, new Date(cal.getTimeInMillis()), cf1);
                ms.addCell(dt);
                ++i;
                cal.setTime(formatter.parse(((String[])myEntries.get(i))[0]));
                dt = new DateTime(1, 1, new Date(cal.getTimeInMillis()), cf1);
                ms.addCell(dt);
            } else if(((String[])myEntries.get(i))[0].equals("<services>")) {
                ms = workbook.createSheet("services", 4);
                l = new Label(0, 0, "nom");
                ms.addCell(l);
                l = new Label(1, 0, "interieur");
                ms.addCell(l);
                l = new Label(2, 0, "repos");
                ms.addCell(l);

                for(j = i + 1; j < myEntries.size(); j += 3) {
                    if(((String[])myEntries.get(j))[0].equals("</services>")) {
                        i = j;
                        k = 1;
                        break;
                    }

                    l = new Label(0, k, ((String[])myEntries.get(j))[0]);
                    ms.addCell(l);
                    if(((String[])myEntries.get(j + 1))[0].equals("true")) {
                        l = new Label(1, k, ((String[])myEntries.get(j + 1))[0]);
                        ms.addCell(l);
                    }

                    l = new Label(2, k, ((String[])myEntries.get(j + 2))[0]);
                    ms.addCell(l);
                    ++k;
                }
            } else if(((String[])myEntries.get(i))[0].equals("<options>")) {
                ms = workbook.createSheet("options", 5);
                l = new Label(0, 0, "nom");
                ms.addCell(l);
                l = new Label(1, 0, "nbgardestotal");
                ms.addCell(l);
                l = new Label(2, 0, "nblundi");
                ms.addCell(l);
                l = new Label(3, 0, "nbmardi");
                ms.addCell(l);
                l = new Label(4, 0, "nbmercredi");
                ms.addCell(l);
                l = new Label(5, 0, "nbjeudi");
                ms.addCell(l);
                l = new Label(6, 0, "nbvendredi");
                ms.addCell(l);
                l = new Label(7, 0, "nbsamedi");
                ms.addCell(l);
                l = new Label(8, 0, "nbdimanche");
                ms.addCell(l);

                for(j = i + 1; j < myEntries.size(); j += 9) {
                    if(((String[])myEntries.get(j))[0].equals("</options>")) {
                        i = j;
                        k = 1;
                        break;
                    }

                    l = new Label(0, k, ((String[])myEntries.get(j))[0]);
                    ms.addCell(l);
                    l = new Label(1, k, ((String[])myEntries.get(j + 1))[0]);
                    ms.addCell(l);
                    l = new Label(2, k, ((String[])myEntries.get(j + 2))[0]);
                    ms.addCell(l);
                    l = new Label(3, k, ((String[])myEntries.get(j + 3))[0]);
                    ms.addCell(l);
                    l = new Label(4, k, ((String[])myEntries.get(j + 4))[0]);
                    ms.addCell(l);
                    l = new Label(5, k, ((String[])myEntries.get(j + 5))[0]);
                    ms.addCell(l);
                    l = new Label(6, k, ((String[])myEntries.get(j + 6))[0]);
                    ms.addCell(l);
                    l = new Label(7, k, ((String[])myEntries.get(j + 7))[0]);
                    ms.addCell(l);
                    l = new Label(8, k, ((String[])myEntries.get(j + 8))[0]);
                    ms.addCell(l);
                    ++k;
                }
            }
        }

        workbook.write();
        workbook.close();
    }

    public static boolean setup(Connection c, Workbook data) throws SQLException {
        Sheet sheet = data.getSheet(4);
        boolean hasint = false;

        for(int mystatement = 1; mystatement < sheet.getRows(); ++mystatement) {
            Cell cur = sheet.getCell(1, mystatement);
            if(!hasint) {
                hasint = cur.getCellFormat() != null && cur.getContents().length() != 0;
            }
        }

        Statement var7 = c.createStatement();
        int rs = var7.executeUpdate("CREATE TABLE SERVICES(NOM VARCHAR(20), NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, INTERIEUR BOOLEAN, REPOS INTEGER DEFAULT 0)");
        rs = var7.executeUpdate("CREATE TABLE MEDECINS(NUMERO INTEGER GENERATED BY DEFAULT AS IDENTITY(START WITH 1, INCREMENT BY 1)PRIMARY KEY, NOM VARCHAR(20), DERNIEREGARDE Date default \'1970-01-01\', NBGARDES INTEGER, NBLUNDI INTEGER default 0, NBMARDI INTEGER default 0, NBMERCREDI INTEGER default 0, NBJEUDI INTEGER default 0, NBVENDREDI INTEGER default 0, NBSAMEDI INTEGER default 0, NBDIMANCHE INTEGER default 0, SERVICE INTEGER, NBSAMEDI_EQUILIBRE BOOLEAN DEFAULT FALSE,NBJEUDI_EQUILIBRE BOOLEAN DEFAULT FALSE, NBFERIES INTEGER, FOREIGN KEY (SERVICE) REFERENCES SERVICES(NUMERO))");
        rs = var7.executeUpdate("CREATE TABLE IMPOSSIBILITES(DATEDEBUT Date, DATEFIN date, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS (NUMERO),PRIMARY KEY(DATEDEBUT, DATEFIN,NUMERO))");
        rs = var7.executeUpdate("CREATE TABLE OPTIONS(NUMERO INTEGER PRIMARY KEY, NBTOTAL INTEGER, NBLUNDI INTEGER, NBMARDI INTEGER, NBMERCREDI INTEGER, NBJEUDI INTEGER, NBVENDREDI INTEGER, NBSAMEDI INTEGER, NBDIMANCHE INTEGER, NBFERIES INTEGER)");
        rs = var7.executeUpdate("CREATE TABLE ATTRIBUES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),PRIMARY KEY(JOUR, INTERIEUR))");
        if(hasint) {
            rs = var7.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR DATE, NUMERO INTEGER, INTERIEUR BOOLEAN, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO),CONSTRAINT ENTRY_DD primary key (JOUR, INTERIEUR))");
        } else {
            rs = var7.executeUpdate("CREATE TABLE JOURS_FERIES(JOUR date primary key, NUMERO INTEGER, FOREIGN KEY (NUMERO) REFERENCES MEDECINS(NUMERO))");
        }

        if(!hasint) {
            rs = var7.executeUpdate("CREATE TABLE GARDES(JOUR Date primary key,URGENCES INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO),MANUALLY_SET BOOLEAN default FALSE)");
        } else {
            rs = var7.executeUpdate("CREATE TABLE GARDES(JOUR Date PRIMARY KEY,URGENCES INTEGER,INTERIEUR INTEGER, FOREIGN KEY (URGENCES) REFERENCES MEDECINS(NUMERO), FOREIGN KEY (interieur) REFERENCES MEDECINS(NUMERO),MANUALLY_SET BOOLEAN default FALSE,MANUALLY_SETINT BOOLEAN DEFAULT FALSE)");
        }

        return hasint;
    }

    public static void filltables(Connection c, Workbook data, boolean hasint) throws SQLException, ParseException {
        Statement mystatement = c.createStatement();
        Statement ms2 = c.createStatement();
        Statement ms3 = c.createStatement();
        Calendar cal = Calendar.getInstance();
        Sheet sheet = data.getSheet(4);

        int nservice;
        for(nservice = 1; nservice < sheet.getRows(); ++nservice) {
            if(sheet.getCell(1, nservice).getCellFormat() != null && sheet.getCell(1, nservice).getContents().length() != 0) {
                try {
                    mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR,REPOS) VALUES(\'" + sheet.getCell(0, nservice).getContents() + "\',TRUE," + sheet.getCell(2, nservice).getContents() + ")");
                } catch (SQLException var33) {
                    System.out.println("caught " + var33.getMessage());
                }
            } else {
                try {
                    mystatement.executeUpdate("INSERT INTO SERVICES(NOM, INTERIEUR,REPOS) VALUES(\'" + sheet.getCell(0, nservice).getContents() + "\',FALSE," + sheet.getCell(2, nservice).getContents() + ")");
                } catch (SQLException var32) {
                    System.out.println("caught " + var32.getMessage());
                }
            }
        }

        sheet = data.getSheet(0);

        java.sql.Date d1;
        DateCell dc1;
        ResultSet rs2;
        int nom;
        label165:
        for(nom = 1; nom < sheet.getRows(); ++nom) {
            rs2 = ms2.executeQuery("SELECT NUMERO FROM SERVICES WHERE NOM = \'".concat(sheet.getCell(1, nom).getContents()).concat("\'"));

            while(true) {
                while(true) {
                    if(!rs2.next()) {
                        continue label165;
                    }

                    nservice = rs2.getInt("NUMERO");
                    if(sheet.getCell(2, nom).getCellFormat() != null && sheet.getCell(2, nom).getContents().length() != 0) {
                        dc1 = (DateCell)sheet.getCell(2, nom);
                        cal.setTime(dc1.getDate());
                        d1 = new java.sql.Date(cal.getTimeInMillis());

                        try {
                            mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE,DERNIEREGARDE) VALUES(\'".concat(sheet.getCell(0, nom).getContents()).concat("\',").concat(Integer.toString(nservice)) + ",\'" + d1 + "\')");
                        } catch (SQLException var31) {
                            System.out.println("caught " + var31.getMessage());
                        }
                    } else {
                        try {
                            mystatement.executeUpdate("INSERT INTO MEDECINS(NOM,SERVICE) VALUES(\'".concat(sheet.getCell(0, nom).getContents()).concat("\',").concat(Integer.toString(nservice)) + ")");
                        } catch (SQLException var30) {
                            System.out.println("caught " + var30.getMessage());
                        }
                    }
                }
            }
        }

        sheet = data.getSheet(2);

        String nblundi;
        for(nom = 1; nom < sheet.getRows(); ++nom) {
            rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = \'".concat(sheet.getCell(2, nom).getContents()).concat("\'"));

            while(rs2.next()) {
                int monset = rs2.getInt("NUMERO");
                dc1 = (DateCell)sheet.getCell(0, nom);
                DateCell dc2 = (DateCell)sheet.getCell(1, nom);
                cal.setTime(dc1.getDate());
                d1 = new java.sql.Date(cal.getTimeInMillis());
                cal.setTime(dc2.getDate());
                java.sql.Date d2 = new java.sql.Date(cal.getTimeInMillis());
                d2 = nextday(d2);

                try {
                    mystatement.executeUpdate("INSERT INTO IMPOSSIBILITES(DATEDEBUT,DATEFIN,NUMERO) VALUES(\'" + d1 + "\',\'" + d2 + "\',".concat(Integer.toString(monset)).concat(")"));
                } catch (SQLException var34) {
                    ResultSet rs3 = ms3.executeQuery("SELECT NOM FROM MEDECINS WHERE NUMERO =" + Integer.toString(monset));

                    for(nblundi = "pas trouvé"; rs3.next(); nblundi = rs3.getString("NOM")) {
                        ;
                    }

                    System.out.println("got exception with " + d1 + " fin " + d2 + " medecin= " + nblundi);
                }
            }
        }

        sheet = data.getSheet(1);

        int id;
        label130:
        for(nom = 1; nom < sheet.getRows(); ++nom) {
            if(sheet.getCell(0, nom).getCellFormat() != null && sheet.getCell(0, nom).getContents().length() != 0) {
                DateCell var35 = (DateCell)sheet.getCell(0, nom);
                var35 = (DateCell)sheet.getCell(0, nom);
                cal.setTime(var35.getDate());
                d1 = new java.sql.Date(cal.getTimeInMillis());
                if(sheet.getCell(1, nom).getCellFormat() != null && sheet.getCell(1, nom).getContents().length() != 0) {
                    rs2 = ms2.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = \'".concat(sheet.getCell(1, nom).getContents()).concat("\'"));

                    while(true) {
                        while(true) {
                            if(!rs2.next()) {
                                continue label130;
                            }

                            if(hasint) {
                                boolean mbool = sheet.getCell(2, nom).getCellFormat() != null && sheet.getCell(2, nom).getContents().length() != 0;
                                id = rs2.getInt("NUMERO");
                                if(mbool) {
                                    mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES (\'" + d1 + "\',".concat(Integer.toString(id).concat(",").concat("TRUE").concat(")")));
                                } else {
                                    mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO,INTERIEUR) VALUES (\'" + d1 + "\',".concat(Integer.toString(id)).concat(",").concat("FALSE").concat(")"));
                                }
                            } else {
                                id = rs2.getInt("NUMERO");
                                cal.setTime(var35.getDate());
                                d1 = new java.sql.Date(cal.getTimeInMillis());
                                mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,NUMERO) VALUES (\'" + d1 + "\',".concat(Integer.toString(id)).concat(")"));
                            }
                        }
                    }
                } else if(hasint) {
                    mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES(\'" + d1 + "\',TRUE)");
                    mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR,INTERIEUR) VALUES(\'" + d1 + "\',FALSE)");
                } else {
                    mystatement.executeUpdate("INSERT INTO JOURS_FERIES(JOUR) VALUES(\'" + d1 + "\')");
                }
            }
        }

        sheet = data.getSheet(5);
        id = -1;

        for(int i = 1; i < sheet.getRows(); ++i) {
            if(sheet.getCell(0, i).getCellFormat() != null && sheet.getCell(0, i).getContents().length() != 0) {
                String var36 = sheet.getCell(0, i).getContents();

                for(ResultSet var37 = mystatement.executeQuery("SELECT NUMERO FROM MEDECINS WHERE NOM = \'" + var36 + "\'"); var37.next(); id = var37.getInt("NUMERO")) {
                    ;
                }

                if(id != -1) {
                    String nbtotal = sheet.getCell(1, i).getContents();
                    nblundi = sheet.getCell(2, i).getContents();
                    String nbmardi = sheet.getCell(3, i).getContents();
                    String nbmercredi = sheet.getCell(4, i).getContents();
                    String nbjeudi = sheet.getCell(5, i).getContents();
                    String nbvendredi = sheet.getCell(6, i).getContents();
                    String nbsamedi = sheet.getCell(7, i).getContents();
                    String nbdimanche = sheet.getCell(8, i).getContents();
                    mystatement.executeUpdate("INSERT INTO OPTIONS(NUMERO,NBTOTAL,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE) VALUES(" + Integer.toString(id) + "," + nbtotal + "," + nblundi + "," + nbmardi + "," + nbmercredi + "," + nbjeudi + "," + nbvendredi + "," + nbsamedi + "," + nbdimanche + ")");
                }
            }
        }

    }

    public static java.sql.Date tosql(Date curdat) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curdat);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public static java.sql.Date nextday(java.sql.Date curdat) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curdat);
        cal.add(5, 1);
        return new java.sql.Date(cal.getTimeInMillis());
    }

    public static java.sql.Date prevday(java.sql.Date curdat) {
        org.joda.time.DateTime ladate = new org.joda.time.DateTime(curdat, DateTimeZone.UTC);
        ladate = ladate.minusDays(1);
        return new java.sql.Date(ladate.getMillis());
    }

    public static boolean dateferiee(java.sql.Date madate, Connection c) throws SQLException {
        Statement ms = c.createStatement();
        ResultSet rs = ms.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE JOUR=\'" + madate + "\'");
        return rs.next();
    }

    public static boolean isreserved(boolean hasint, boolean interieur, Connection c, java.sql.Date madate) throws SQLException {
        boolean itis = false;
        Statement ms = c.createStatement();
        ResultSet rs2;
        if(hasint) {
            rs2 = ms.executeQuery("SELECT JF.NUMERO FROM (JOURS_FERIES AS JF INNER JOIN MEDECINS AS M ON M.NUMERO = JF.NUMERO) INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO AND JF.INTERIEUR = " + interieur + " WHERE JOUR = \'" + madate + "\' AND INTERIEUR = " + interieur);
        } else {
            rs2 = ms.executeQuery("SELECT NUMERO FROM JOURS_FERIES WHERE JOUR = \'" + madate + "\'");
        }

        while(rs2.next()) {
            itis = true;
        }

        return itis;
    }

    public static String getdow(Date curdat) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curdat);
        int dow = cal.get(7);
        switch(dow) {
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

    public static void dorecord(datepack monpack, Connection c, boolean interieur, boolean hasint) throws SQLException, ParseException {
        Statement ms = c.createStatement();
        java.sql.Date sqldate = monpack.garde.jour;
        ms.executeUpdate("UPDATE MEDECINS set DERNIEREGARDE = \'" + monpack.garde.jour + "\' WHERE NUMERO = ".concat(Integer.toString(monpack.garde.nmed)));
        if(monpack.garde.ferie || dateferiee(monpack.garde.jour, c)) {
            int ex = monpack.garde.nbferies + 1;
            ms.executeUpdate("UPDATE MEDECINS SET NBFERIES = " + ex + " WHERE NUMERO = " + monpack.garde.nmed);
        }

        ms.executeUpdate("update MEDECINS set ".concat(monpack.garde.dowtoinc).concat(" = ").concat(Integer.toString(monpack.garde.newdowcount)).concat("where NUMERO = ").concat(Integer.toString(monpack.garde.nmed)));
        ms.executeUpdate("UPDATE MEDECINS set NBGARDES = ".concat(Integer.toString(monpack.garde.curgarde)).concat("WHERE NUMERO = ").concat(Integer.toString(monpack.garde.nmed)));
        if(hasint) {
            if(!interieur) {
                if(!monpack.garde.ferie) {
                    ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(monpack.garde.nmed)) + " WHERE JOUR = \'" + sqldate + "\'");
                } else {
                    ms.executeUpdate("UPDATE GARDES SET URGENCES = ".concat(Integer.toString(monpack.garde.nmed)) + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + sqldate + "\'");
                }
            } else if(!monpack.garde.ferie) {
                ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR,MANUALLY_SETINT) VALUES(\'" + sqldate + "\',".concat(Integer.toString(monpack.garde.nmed)).concat(",TRUE)"));
            } else {
                ms.executeUpdate("INSERT INTO GARDES(JOUR,INTERIEUR) VALUES(\'" + sqldate + "\',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
            }
        } else if(!monpack.garde.ferie) {
            try {
                ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES) VALUES(\'" + sqldate + "\',".concat(Integer.toString(monpack.garde.nmed)).concat(")"));
            } catch (SQLException var8) {
                System.out.println("constraint violation, insertion garde  le " + sqldate);
            }
        } else {
            ms.executeUpdate("INSERT INTO GARDES(JOUR,URGENCES,MANUALLY_SET) VALUES(\'" + sqldate + "\',".concat(Integer.toString(monpack.garde.nmed)).concat(",TRUE)"));
        }

    }

    public static void writeoutput(datepack monpack, Connection c, WritableWorkbook output, boolean hasint, Workbook data, String[] arg) throws SQLException, RowsExceededException, WriteException, IOException, BiffException {
        writegardes(monpack, c, output, hasint, data);
        writestats(c, output, hasint);
        writegps(c, output, hasint);
        writecalendar(c, output, hasint);
        if(arg[0].equals("--xls")) {
            updatedata(c, arg[1]);
        } else {
            updatedata(c, arg[0] + "_data.xls");
        }

        output.write();
        output.close();
    }

    public static void writegps(Connection c, WritableWorkbook output, boolean hasint) throws SQLException, RowsExceededException, WriteException {
        WritableSheet ms = output.createSheet("GPS", 2);
        Statement mst = c.createStatement();
        int i = 1;
        Label l1 = new Label(0, 0, "Date");
        Label l2 = new Label(1, 0, "Urgences");
        Label l3;
        if(hasint) {
            l3 = new Label(2, 0, "Interieur");
            ms.addCell(l3);
        }

        ms.addCell(l1);
        ms.addCell(l2);
        ResultSet rs;
        if(hasint) {
            rs = mst.executeQuery("SELECT G.JOUR AS JOUR, S.NOM AS S1,s2.NOM AS S2N FROM(((GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO)INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO)INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO)INNER JOIN SERVICES AS S2 ON M2.SERVICE = S2.NUMERO");
        } else {
            rs = mst.executeQuery("SELECT G.JOUR AS JOUR, S.NOM AS S1 FROM (GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO)INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO");
        }

        while(rs.next()) {
            l1 = new Label(0, i, rs.getString("JOUR"));
            l2 = new Label(1, i, rs.getString("S1"));
            if(hasint) {
                l3 = new Label(2, i, rs.getString("S2N"));
                ms.addCell(l3);
            }

            ms.addCell(l1);
            ms.addCell(l2);
            ++i;
        }

    }

    public static void writegardes(datepack monpack, Connection c, WritableWorkbook output, boolean hasint, Workbook data) throws SQLException, RowsExceededException, WriteException, IOException {
        WritableSheet ms = output.createSheet("planning", 0);
        Statement mst = c.createStatement();
        ResultSet rs;
        if(hasint) {
            rs = mst.executeQuery("SELECT G.JOUR AS JOUR, M.NOM AS M1, M2.NOM AS M2N FROM (GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO ) INNER JOIN MEDECINS AS M2 ON G.INTERIEUR = M2.NUMERO ORDER BY JOUR ASC");
        } else {
            rs = mst.executeQuery("SELECT G.JOUR AS JOUR, M.NOM AS M1 FROM GARDES AS G INNER JOIN MEDECINS AS M ON G.URGENCES = M.NUMERO ");
        }

        int i = 1;
        Label l1 = new Label(0, 0, "date");
        Label l2 = new Label(1, 0, "urgence");
        Label l3;
        if(hasint) {
            l3 = new Label(2, 0, "interieur");
            ms.addCell(l3);
        }

        ms.addCell(l1);
        ms.addCell(l2);

        while(rs.next()) {
            l1 = new Label(0, i, rs.getString("JOUR"));
            l2 = new Label(1, i, rs.getString("M1"));
            if(hasint) {
                l3 = new Label(2, i, rs.getString("M2N"));
                ms.addCell(l3);
            }

            ms.addCell(l1);
            ms.addCell(l2);
            ++i;
        }

        if(monpack.upto.before(monpack.goal)) {
            l1 = new Label(0, i + 1, "le tableau de garde n\'a pas put être généré jusqu\'au bout, voir erreur ci dessous");
            l2 = new Label(0, i + 2, monpack.error);
            boolean target_s = false;
            String target_nom = "";
            boolean target_repos = false;
            if(monpack.nbdays != 0) {
                l2.setString(l2.getString() + " nombre max de jours de repos au moment de l\'erreur: " + monpack.nbdays + "\n" + "nombre max de jours de repos avant le prochain jour férié: " + monpack.daysbf + "\n" + "vous pouvez tenter de générer un nouveau tableau de gardes avec un nombre de jours de repos inférieur à  " + monpack.nbdays);
                System.out.println("printing error:\n" + l2.getString());
            }

            ms.addCell(l1);
            ms.addCell(l2);
        }

    }

    public static void writestats(Connection c, WritableWorkbook output, boolean hasint) throws SQLException, RowsExceededException, WriteException, IOException {
        WritableSheet ms = output.createSheet("stats", 1);
        Statement mst = c.createStatement();
        ResultSet rs = mst.executeQuery("SELECT M.NOM AS NOM, M.NBGARDES AS NBGARDES, M.NBFERIES AS NBFERIES,M.NBLUNDI AS NBLUNDI,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI, M.NBJEUDI AS NBJEUDI, M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE, S.NOM AS service FROM (MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO) order by SERVICE ASC,NOM ASC");
        Label l1 = new Label(0, 0, "nom");
        Label l2 = new Label(1, 0, "options");
        Label l3 = new Label(2, 0, "nbgardes");
        Label l4 = new Label(3, 0, "service");
        Label l5 = new Label(4, 0, "nbferies");
        Label l6 = new Label(5, 0, "nblundi");
        Label l7 = new Label(6, 0, "nbmardi");
        Label l8 = new Label(7, 0, "nbmercredi");
        Label l9 = new Label(8, 0, "nbjeudi");
        Label l10 = new Label(9, 0, "nbvendredi");
        Label l11 = new Label(10, 0, "nbsamedi");
        Label l12 = new Label(11, 0, "nbdimanche");
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

        for(int i = 1; rs.next(); ++i) {
            l1 = new Label(0, i, rs.getString("NOM"));
            l3 = new Label(2, i, Integer.toString(rs.getInt("NBGARDES")));
            l4 = new Label(3, i, rs.getString("service"));
            l5 = new Label(4, i, Integer.toString(rs.getInt("NBFERIES")));
            l6 = new Label(5, i, Integer.toString(rs.getInt("NBLUNDI")));
            l7 = new Label(6, i, Integer.toString(rs.getInt("NBMARDI")));
            l8 = new Label(7, i, Integer.toString(rs.getInt("NBMERCREDI")));
            l9 = new Label(8, i, Integer.toString(rs.getInt("NBJEUDI")));
            l10 = new Label(9, i, Integer.toString(rs.getInt("NBVENDREDI")));
            l11 = new Label(10, i, Integer.toString(rs.getInt("NBSAMEDI")));
            l12 = new Label(11, i, Integer.toString(rs.getInt("NBDIMANCHE")));
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
        }

    }

    public static void writecalendar(Connection c, WritableWorkbook output, boolean hasint) throws SQLException, RowsExceededException, WriteException {
        Statement mst = c.createStatement();
        Statement ms1 = c.createStatement();
        ResultSet rs = mst.executeQuery("SELECT NOM,NUMERO FROM MEDECINS ORDER BY NOM");

        for(int i = 3; rs.next(); ++i) {
            int j = 0;
            WritableSheet ms = output.createSheet(rs.getString("NOM"), i);
            ResultSet rs2;
            if(hasint) {
                rs2 = ms1.executeQuery("SELECT JOUR,URGENCES,INTERIEUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " OR INTERIEUR = " + rs.getInt("NUMERO"));
            } else {
                rs2 = ms1.executeQuery("SELECT JOUR,URGENCES FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO"));
            }

            while(rs2.next()) {
                Label l1 = new Label(0, j, rs2.getString("JOUR"));
                if(hasint) {
                    Label l2;
                    if(rs2.getInt("INTERIEUR") == rs.getInt("NUMERO")) {
                        l2 = new Label(1, j, "interieur");
                    } else {
                        l2 = new Label(1, j, "urgences");
                    }

                    ms.addCell(l2);
                }

                ms.addCell(l1);
                ++j;
            }
        }

    }

    public static void updatedata(Connection c, String filename) throws SQLException, IOException, RowsExceededException, WriteException, BiffException {
        Statement ms = c.createStatement();
        Workbook data = Workbook.getWorkbook(new File(filename));
        WritableWorkbook data2 = Workbook.createWorkbook(new File(filename), data);
        WritableSheet mst = data2.getSheet(0);
        Calendar cal = Calendar.getInstance();
        WritableCellFormat cf1 = new WritableCellFormat(DateFormats.FORMAT9);

        for(int i = 1; i < mst.getRows(); ++i) {
            String nom = mst.getCell(0, i).getContents();
            ResultSet rs = ms.executeQuery("SELECT DERNIEREGARDE FROM MEDECINS WHERE NOM = \'" + nom + "\'");

            while(rs.next()) {
                cal.setTime(rs.getDate("DERNIEREGARDE"));
                DateTime dt = new DateTime(2, i, new Date(cal.getTimeInMillis()), cf1);
                mst.addCell(dt);
            }
        }

        data2.write();
        data2.close();
        data.close();
    }

    public static void swap(Connection c, java.sql.Date d1, java.sql.Date d2, boolean interieur) throws SQLException {
        int med1 = 0;
        int med2 = 0;
        Statement ms = c.createStatement();
        Statement ms2 = c.createStatement();
        Statement ms3 = c.createStatement();
        Statement ms4 = c.createStatement();
        ResultSet rs;
        ResultSet rs2;
        if(interieur) {
            rs = ms.executeQuery("SELECT INTERIEUR as T1 FROM GARDES WHERE JOUR = \'" + d1 + "\'");
            rs2 = ms2.executeQuery("SELECT INTERIEUR as T2 FROM GARDES WHERE JOUR = \'" + d2 + "\'");
        } else {
            rs = ms.executeQuery("SELECT URGENCES as T1 FROM GARDES WHERE JOUR = \'" + d1 + "\'");
            rs2 = ms2.executeQuery("SELECT URGENCES as T2 FROM GARDES WHERE JOUR = \'" + d2 + "\'");
        }

        while(rs.next() && rs2.next()) {
            med1 = rs.getInt("T1");
            med2 = rs2.getInt("T2");
        }

        if(interieur) {
            ms.executeUpdate("UPDATE GARDES SET INTERIEUR  = " + Integer.toString(med2) + " WHERE JOUR = \'" + d1 + "\'");
            ms.executeUpdate("UPDATE GARDEES SET INTERIEUR = " + Integer.toString(med1) + " WHERE JOUR = " + d2 + "\'");
        } else {
            ms.executeUpdate("UPDATE GARDES SET URGENCES  = " + Integer.toString(med2) + " WHERE JOUR = \'" + d1 + "\'");
            ms.executeUpdate("UPDATE GARDEES SET URGENCES = " + Integer.toString(med1) + " WHERE JOUR = " + d2 + "\'");
        }

        rs = ms.executeQuery("SELECT JOUR FROM GARDES WHERE INTERIEUR = " + med1 + " OR URGENCES = " + med1 + " ORDER BY JOUR DESC LIMIT 1");

        while(rs.next()) {
            ms2.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs.getDate("JOUR") + "\' WHERE NUMERO = " + med1);
        }

        rs = ms.executeQuery("SELECT JOUR FROM GARDES WHERE INTERIEUR = " + med2 + " OR URGENCES = " + med2 + " ORDER BY JOUR DESC LIMIT 1");

        while(rs.next()) {
            ms2.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs.getDate("JOUR") + "\' WHERE NUMERO = " + med1);
        }

    }

    public static void equilibrer(Connection c, boolean interieur) throws SQLException, ParseException {
        Statement ms = c.createStatement();
        Statement ms2 = c.createStatement();
        Statement ms8 = c.createStatement();
        Statement ms4 = c.createStatement();
        Statement ms5 = c.createStatement();
        Statement m6 = c.createStatement();
        Statement m7 = c.createStatement();
        int curg = 0;
        int nbmeds = 0;
        int prevint = 666;
        int prevurg = 0;
        int nbjeudi = 0;
        boolean nbvendredi = false;
        int nbsamedi = 0;
        int nbdimanche = 0;
        gtg isgood = null;
        boolean done = false;
        String secteur = "URGENCES";
        int max = 0;
        int min = 0;
        boolean totgardes = false;
        boolean nbjour = false;

        ResultSet rs;
        int var38;
        int var39;
        for(rs = ms.executeQuery("SELECT COUNT(M.NUMERO) as nbmeds,SUM(M.NBVENDREDI) as allvend,SUM(M.NBDIMANCHE) as alldim, MAX(M.NBGARDES) as MAXG,MIN(M.NBGARDES) as MING,SUM(M.NBSAMEDI) as ALLSAMS,SUM(M.NBJEUDI) as allthu,SUM(M.NBGARDES) as TOTGARDES FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND S.INTERIEUR = " + interieur); rs.next(); nbmeds = rs.getInt("nbmeds")) {
            max = rs.getInt("MAXG");
            min = rs.getInt("MING");
            nbsamedi = rs.getInt("ALLSAMS");
            var38 = rs.getInt("allvend");
            nbdimanche = rs.getInt("alldim");
            nbjeudi = rs.getInt("allthu");
            var39 = rs.getInt("TOTGARDES");
        }

        ResultSet rs2;
        ResultSet rs8;
        ResultSet rs4;
        ResultSet rs5;
        ResultSet rs6;
        int calcval;
        String dowtoinc;
        int maxdim;
        for(maxdim = 0; maxdim < 2; ++maxdim) {
            if(maxdim == 0) {
                dowtoinc = "NBSAMEDI";
                calcval = nbsamedi / nbmeds;
            } else {
                dowtoinc = "NBJEUDI";
                calcval = nbjeudi / nbmeds;
            }

            if(calcval == 0) {
                calcval = 1;
            }

            if(maxdim == 0) {
                rs2 = ms2.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES as NBGARDES,M.NOM AS NOM,M.NBLUNDI AS NBLUNDI,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICE FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M." + dowtoinc + " < " + Integer.toString(calcval) + " and M.NBSAMEDI_EQUILIBRE = FALSE and S.INTERIEUR = " + interieur);
            } else {
                rs2 = ms2.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES as NBGARDES,M.NOM AS NOM,M.NBLUNDI AS NBLUNDI,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICE FROM MEDECINS as M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M." + dowtoinc + " < " + Integer.toString(calcval) + " and M.NBJEUDI_EQUILIBRE = FALSE and S.INTERIEUR = " + interieur);
            }

            while(rs2.next()) {
                rs = ms.executeQuery("SELECT M.NUMERO as NUMERO,M.NBGARDES AS NBGARDES,M.NOM AS NOM,M.DERNIEREGARDE AS DERNIEREGARDE,M.NBLUNDI AS NBLUNDI,M.NBMARDI AS NBMARDI,M.NBMERCREDI AS NBMERCREDI,M.NBJEUDI AS NBJEUDI,M.NBVENDREDI AS NBVENDREDI,M.NBSAMEDI AS NBSAMEDI,M.NBDIMANCHE AS NBDIMANCHE,M.NBFERIES AS NBFERIES,M.SERVICE AS SERVICES FROM MEDECINS AS M INNER JOIN SERVICES AS S ON M.SERVICE = S.NUMERO WHERE M.NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) AND M." + dowtoinc + " > " + Integer.toString(calcval) + "AND S.INTERIEUR = " + interieur);
                done = false;

                String curdow;
                label568: {
                    label567:
                    do {
                        if(!rs.next()) {
                            break label568;
                        }

                        if(!interieur) {
                            secteur = "URGENCES";
                        } else {
                            secteur = "INTERIEUR";
                        }

                        rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + Integer.toString(rs.getInt("NUMERO")) + " and MANUALLY_SET = FALSE");

                        do {
                            do {
                                if(!rs4.next()) {
                                    continue label567;
                                }

                                curdow = getdow(fromsql(rs4.getDate("JOUR")));
                            } while(curdow != dowtoinc);

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + rs4.getDate("JOUR") + "\'"); rs5.next(); curg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevurg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            if(interieur) {
                                for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevint = rs5.getInt("SERVICE")) {
                                    ;
                                }
                            }

                            isgood = new gtg(curg, prevint, prevurg, c, rs4.getDate("JOUR"), rs2, dowtoinc, interieur, true);
                            if(isgood.gtg) {
                                m6.executeUpdate("UPDATE GARDES SET " + secteur + " = " + rs2.getInt("NUMERO") + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + rs4.getDate("JOUR") + "\'");
                                rs6 = m6.executeQuery("SELECT " + dowtoinc + ", NBGARDES,DERNIEREGARDE, NUMERO FROM MEDECINS WHERE NUMERO = " + rs2.getInt("NUMERO"));

                                while(rs6.next()) {
                                    m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs6.getInt(dowtoinc) + 1) + ", NBGARDES = " + Integer.toString(rs6.getInt("NBGARDES") + 1) + ", " + dowtoinc + "_EQUILIBRE = TRUE WHERE NUMERO = " + rs6.getInt("NUMERO"));
                                    m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs.getInt(dowtoinc) - 1) + ", NBGARDES = " + Integer.toString(rs.getInt("NBGARDES") - 1) + "WHERE NUMERO = " + rs.getInt("NUMERO"));
                                    done = true;
                                    rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                    while(rs8.next()) {
                                        m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs.getInt("NUMERO"));
                                    }

                                    rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs2.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                    while(rs8.next()) {
                                        m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs2.getInt("NUMERO"));
                                    }
                                }
                            }
                        } while((isgood == null || !isgood.gtg) && !done);

                        done = false;
                    } while((isgood == null || !isgood.gtg) && !done);

                    done = false;
                }

                if(isgood != null && isgood.gtg || done) {
                    done = false;
                    break;
                }

                boolean found;
                if(dowtoinc == "NBSAMEDI") {
                    calcval = nbsamedi / nbmeds;
                    if(calcval == 0) {
                        boolean var40 = true;
                    }

                    boolean var41 = false;
                    found = false;

                    for(maxdim = 0; maxdim < 2; ++maxdim) {
                        done = false;
                        if(maxdim == 0) {
                            if(rs2.getInt("NBVENDREDI") != 0) {
                                done = true;
                                var41 = true;
                                continue;
                            }

                            dowtoinc = "NBVENDREDI";
                        } else {
                            if(rs2.getInt("NBDIMANCHE") != 0) {
                                found = true;
                                done = true;
                                break;
                            }

                            dowtoinc = "NBDIMANCHE";
                        }

                        rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS INNER JOIN(SELECT NUMERO FROM MEDECINS EXCEPT SELECT NUMERO FROM OPTIONS) AS M2 ON MEDECINS.NUMERO = M2.NUMERO WHERE " + dowtoinc + " > 0 ORDER BY " + dowtoinc);

                        while(rs.next()) {
                            if(!interieur) {
                                secteur = "URGENCES";
                            } else {
                                secteur = "INTERIEUR";
                            }

                            rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE " + secteur + " = " + Integer.toString(rs.getInt("NUMERO")) + " and MANUALLY_SET = FALSE");

                            label488:
                            do {
                                do {
                                    if(!rs4.next()) {
                                        break label488;
                                    }

                                    curdow = getdow(fromsql(rs4.getDate("JOUR")));
                                } while(curdow != dowtoinc);

                                for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + rs4.getDate("JOUR") + "\'"); rs5.next(); curg = rs5.getInt("SERVICE")) {
                                    ;
                                }

                                for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevurg = rs5.getInt("SERVICE")) {
                                    ;
                                }

                                if(interieur) {
                                    for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevint = rs5.getInt("SERVICE")) {
                                        ;
                                    }
                                }

                                isgood = new gtg(curg, prevint, prevurg, c, rs4.getDate("JOUR"), rs2, dowtoinc, interieur, true);
                                if(isgood.gtg) {
                                    m6.executeUpdate("UPDATE GARDES SET " + secteur + " = " + rs2.getInt("NUMERO") + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + rs4.getDate("JOUR") + "\'");
                                    rs6 = m6.executeQuery("SELECT " + dowtoinc + ", NBGARDES,DERNIEREGARDE, NUMERO FROM MEDECINS WHERE NUMERO = " + rs2.getInt("NUMERO"));

                                    while(rs6.next()) {
                                        m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs6.getInt(dowtoinc) + 1) + ", NBGARDES = " + Integer.toString(rs6.getInt("NBGARDES") + 1) + ", NBSAMEDI_EQUILIBRE = TRUE WHERE NUMERO = " + rs6.getInt("NUMERO"));
                                        m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs.getInt(dowtoinc) - 1) + ", NBGARDES = " + Integer.toString(rs.getInt("NBGARDES") - 1) + "WHERE NUMERO = " + rs.getInt("NUMERO"));
                                        done = true;
                                        rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                        while(rs8.next()) {
                                            m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs4.getDate("JOUR") + "\' WHERE NUMERO = " + rs.getInt("NUMERO"));
                                        }

                                        rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs2.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                        while(rs8.next()) {
                                            m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs4.getDate("JOUR") + "\' WHERE NUMERO = " + rs2.getInt("NUMERO"));
                                        }
                                    }
                                }
                            } while((isgood == null || !isgood.gtg) && !done);

                            if(isgood != null && isgood.gtg || done) {
                                break;
                            }
                        }
                    }

                    if(var41 && found) {
                        m7.executeUpdate("UPDATE MEDECINS SET NBSAMEDI_EQUILIBRE = TRUE WHERE NUMERO = " + rs2.getInt("NUMERO"));
                    }
                    break;
                }

                if(dowtoinc == "NBJEUDI") {
                    if(!interieur) {
                        secteur = "URGENCES";
                    } else {
                        secteur = "INTERIEUR";
                    }

                    calcval = nbjeudi / nbmeds;
                    if(calcval == 0) {
                        calcval = 1;
                    }

                    rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS INNER JOIN JOURS_FERIES as jf on jf.NUMERO = MEDECINS.NUMERO INNER JOIN(SELECT NUMERO FROM MEDECINS EXCEPT SELECT NUMERO FROM OPTIONS) AS M2 ON MEDECINS.NUMERO = M2.NUMERO");

                    while(rs.next()) {
                        if(interieur) {
                            if(secteur == "URGENCES") {
                                rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = " + Integer.toString(rs.getInt("NUMERO")) + "and INTERIEUR = FALSE");
                            } else {
                                rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = " + Integer.toString(rs.getInt("NUMERO")) + "and INTERIEUR = TRUE");
                            }
                        } else {
                            rs4 = ms4.executeQuery("SELECT JOUR FROM JOURS_FERIES WHERE NUMERO = " + Integer.toString(rs.getInt("NUMERO")));
                        }

                        label651:
                        do {
                            java.sql.Date jourJ;
                            do {
                                do {
                                    if(!rs4.next()) {
                                        break label651;
                                    }

                                    jourJ = prevday(rs4.getDate("JOUR"));
                                    jourJ = prevday(jourJ);
                                    curdow = getdow(fromsql(jourJ));
                                } while(curdow != "NBLUNDI" && curdow != "NBMARDI" && curdow != "NBMERCREDI");

                                found = false;
                                ResultSet rs7 = m7.executeQuery("SELECT MANUALLY_SET FROM GARDES WHERE JOUR = \'" + jourJ + "\'");

                                while(rs7.next()) {
                                    found = rs7.getBoolean("MANUALLY_SET");
                                    if(found) {
                                        break;
                                    }
                                }
                            } while(found);

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + jourJ + "\'"); rs5.next(); curg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + prevday(jourJ) + "\'"); rs5.next(); prevurg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            if(interieur) {
                                for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.INTERIEUR WHERE G.JOUR = \'" + prevday(jourJ) + "\'"); rs5.next(); prevint = rs5.getInt("SERVICE")) {
                                    ;
                                }
                            }

                            isgood = new gtg(curg, prevint, prevurg, c, rs4.getDate("JOUR"), rs2, curdow, interieur, true);
                            if(isgood.gtg) {
                                m6.executeUpdate("UPDATE GARDES SET " + secteur + " = " + rs2.getInt("NUMERO") + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + rs4.getDate("JOUR") + "\'");
                                rs6 = m6.executeQuery("SELECT " + curdow + ", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = " + rs2.getInt("NUMERO"));

                                while(rs6.next()) {
                                    m7.executeUpdate("UPDATE MEDECINS SET " + curdow + " = " + Integer.toString(rs6.getInt(dowtoinc) + 1) + ", NBGARDES = " + Integer.toString(rs6.getInt("NBGARDES") + 1) + ", NBJEUDI_EQUILIBRE = TRUE WHERE NUMERO = " + rs6.getInt("NUMERO"));
                                    m7.executeUpdate("UPDATE MEDECINS SET " + curdow + " = " + Integer.toString(rs.getInt(dowtoinc) - 1) + ", NBGARDES = " + Integer.toString(rs.getInt("NBGARDES") - 1) + "WHERE NUMERO = " + rs.getInt("NUMERO"));
                                    done = true;
                                    rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                    while(rs8.next()) {
                                        m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs.getDate("JOUR") + "\' WHERE NUMERO = " + rs.getInt("NUMERO"));
                                    }

                                    rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs2.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                                    while(rs8.next()) {
                                        m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs2.getDate("JOUR") + "\' WHERE NUMERO = " + rs2.getInt("NUMERO"));
                                    }
                                }
                            }
                        } while((isgood == null || !isgood.gtg) && !done);

                        if(isgood != null && isgood.gtg || done) {
                            break;
                        }
                    }
                }
            }
        }

        for(rs = ms.executeQuery("SELECT COUNT(NUMERO) as nbmeds,SUM(NBVENDREDI) as allvend,SUM(NBDIMANCHE) as alldim, MAX(NBGARDES) as MAXG,MIN(NBGARDES) as MING,SUM(NBSAMEDI) as ALLSAMS,SUM(NBJEUDI) as allthu,SUM(NBGARDES) as TOTGARDES FROM MEDECINS WHERE NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)"); rs.next(); nbmeds = rs.getInt("nbmeds")) {
            max = rs.getInt("MAXG");
            min = rs.getInt("MING");
            nbsamedi = rs.getInt("ALLSAMS");
            var38 = rs.getInt("allvend");
            nbdimanche = rs.getInt("alldim");
            nbjeudi = rs.getInt("allthu");
            var39 = rs.getInt("TOTGARDES");
        }

        if(max > min + 1 || done) {
            rs2 = ms2.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES < " + Integer.toString(max - 1) + " and NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");

            while(rs2.next()) {
                rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBGARDES = " + Integer.toString(max) + " AND NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");

                label405:
                do {
                    do {
                        if(!rs.next()) {
                            break label405;
                        }
                    } while(interieur);

                    rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + Integer.toString(rs.getInt("NUMERO")) + " and MANUALLY_SET = FALSE");

                    do {
                        do {
                            do {
                                do {
                                    do {
                                        do {
                                            if(!rs4.next()) {
                                                continue label405;
                                            }

                                            dowtoinc = getdow(fromsql(rs4.getDate("JOUR")));
                                        } while(dowtoinc == "NBJEUDI");
                                    } while(dowtoinc == "NBVENDREDI");
                                } while(dowtoinc == "NBSAMEDI");
                            } while(dowtoinc == "NBDIMANCHE");

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + rs4.getDate("JOUR") + "\'"); rs5.next(); curg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevurg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            isgood = new gtg(curg, prevint, prevurg, c, rs4.getDate("JOUR"), rs2, dowtoinc, interieur, true);
                        } while(!isgood.gtg);

                        m6.executeUpdate("UPDATE GARDES SET URGENCES = " + rs2.getInt("NUMERO") + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + rs4.getDate("JOUR") + "\'");
                        rs6 = m6.executeQuery("SELECT " + dowtoinc + ", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = " + rs2.getInt("NUMERO"));

                        while(rs6.next()) {
                            m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs6.getInt(dowtoinc) + 1) + ", NBGARDES = " + Integer.toString(rs6.getInt("NBGARDES") + 1) + "WHERE NUMERO = " + rs6.getInt("NUMERO"));
                            m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs.getInt(dowtoinc) - 1) + ", NBGARDES = " + Integer.toString(rs.getInt("NBGARDES") - 1) + "WHERE NUMERO = " + rs.getInt("NUMERO"));
                            done = true;
                            rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                            while(rs8.next()) {
                                m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs.getInt("NUMERO"));
                            }

                            rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs2.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                            while(rs8.next()) {
                                m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs2.getInt("NUMERO"));
                            }
                        }
                    } while((isgood == null || !isgood.gtg) && !done);
                } while((isgood == null || !isgood.gtg) && !done);

                if(isgood != null && isgood.gtg || done) {
                    break;
                }
            }

            rs = ms.executeQuery("SELECT COUNT(NUMERO) as nbmeds,SUM(NBVENDREDI) as allvend,SUM(NBDIMANCHE) as alldim,MAX(NBDIMANCHE) AS MAXDIM ,MAX(NBGARDES) as MAXG,MIN(NBGARDES) as MING,SUM(NBSAMEDI) as ALLSAMS,SUM(NBJEUDI) as allthu,SUM(NBGARDES) as TOTGARDES FROM MEDECINS WHERE NUMERO NOT IN (SELECT NUMERO FROM OPTIONS)");
            maxdim = 0;

            for(done = false; rs.next(); maxdim = rs.getInt("MAXDIM")) {
                max = rs.getInt("MAXG");
                min = rs.getInt("MING");
                nbsamedi = rs.getInt("ALLSAMS");
                var38 = rs.getInt("allvend");
                nbdimanche = rs.getInt("alldim");
                nbjeudi = rs.getInt("allthu");
                var39 = rs.getInt("TOTGARDES");
                nbmeds = rs.getInt("nbmeds");
            }

            calcval = nbdimanche / nbmeds;
            if(calcval == 0) {
                calcval = 1;
            }

            rs2 = ms2.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBDIMANCHE < " + calcval + " and NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) order by NBSAMEDI ASC");

            while(rs2.next()) {
                rs = ms.executeQuery("SELECT NUMERO,NBGARDES,NOM,DERNIEREGARDE,NBLUNDI,NBMARDI,NBMERCREDI,NBJEUDI,NBVENDREDI,NBSAMEDI,NBDIMANCHE,NBFERIES,SERVICE FROM MEDECINS WHERE NBDIMANCHE = " + maxdim + " AND NUMERO NOT IN (SELECT NUMERO FROM OPTIONS) order by NBSAMEDI DESC");

                label316:
                do {
                    do {
                        if(!rs.next()) {
                            break label316;
                        }
                    } while(interieur);

                    rs4 = ms4.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + Integer.toString(rs.getInt("NUMERO")) + " and MANUALLY_SET = FALSE AND DAYOFWEEK(JOUR) = 1");

                    do {
                        do {
                            if(!rs4.next()) {
                                continue label316;
                            }

                            dowtoinc = getdow(fromsql(rs4.getDate("JOUR")));

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + rs4.getDate("JOUR") + "\'"); rs5.next(); curg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            for(rs5 = ms5.executeQuery("SELECT M.SERVICE AS SERVICE FROM MEDECINS AS M INNER JOIN GARDES AS G ON M.NUMERO = G.URGENCES WHERE G.JOUR = \'" + prevday(rs4.getDate("JOUR")) + "\'"); rs5.next(); prevurg = rs5.getInt("SERVICE")) {
                                ;
                            }

                            isgood = new gtg(curg, 666, prevurg, c, rs4.getDate("JOUR"), rs2, dowtoinc, interieur, true);
                        } while(!isgood.gtg);

                        m6.executeUpdate("UPDATE GARDES SET URGENCES = " + rs2.getInt("NUMERO") + ", MANUALLY_SET = TRUE WHERE JOUR = \'" + rs4.getDate("JOUR") + "\'");
                        rs6 = m6.executeQuery("SELECT " + dowtoinc + ", NBGARDES, NUMERO FROM MEDECINS WHERE NUMERO = " + rs2.getInt("NUMERO"));

                        while(rs6.next()) {
                            m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs6.getInt(dowtoinc) + 1) + ", NBGARDES = " + Integer.toString(rs6.getInt("NBGARDES") + 1) + "WHERE NUMERO = " + rs6.getInt("NUMERO"));
                            m7.executeUpdate("UPDATE MEDECINS SET " + dowtoinc + " = " + Integer.toString(rs.getInt(dowtoinc) - 1) + ", NBGARDES = " + Integer.toString(rs.getInt("NBGARDES") - 1) + "WHERE NUMERO = " + rs.getInt("NUMERO"));
                            done = true;
                            rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                            while(rs8.next()) {
                                m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs.getInt("NUMERO"));
                            }

                            rs8 = ms8.executeQuery("SELECT JOUR FROM GARDES WHERE URGENCES = " + rs2.getInt("NUMERO") + " ORDER BY JOUR DESC LIMIT 1");

                            while(rs8.next()) {
                                m7.executeUpdate("UPDATE MEDECINS SET DERNIEREGARDE = \'" + rs8.getDate("JOUR") + "\' WHERE NUMERO = " + rs2.getInt("NUMERO"));
                            }
                        }
                    } while(!done);
                } while(!done);

                if(done) {
                    break;
                }
            }

            if(done) {
                equilibrer(c, interieur);
            }
        }

    }

    static Date fromsql(java.sql.Date d1) {
        Date utilDate = new Date(d1.getTime());
        return utilDate;
    }
}