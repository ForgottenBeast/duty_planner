/*This file is part of duty_planner.

duty_planner is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

duty_planner is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied
warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the
GNU General Public License
along with duty_planner.  If not, see

<http://www.gnu.org/licenses/>.*/
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
