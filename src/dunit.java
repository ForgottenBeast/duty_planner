import java.sql.Date;








public class dunit {
    public dunit() {
        this.nmed = 0;
        this.jour = new Date((long)18);
        this.dowtoinc = "whatever";
        this.curg = 0;
        this.curgarde = 0;
        this.newdowcount = 0;
        this.medundefined = true;
        this.ferie = false;
        this.nbferies = 0;
    }
	int nmed;
    java.sql.Date jour;
    String dowtoinc;
    int curg;
    int curgarde;
    int newdowcount;
    boolean medundefined;
    boolean ferie;
    int nbferies;
    // etc
}
