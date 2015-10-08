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





public class datepack {

	public datepack() {
		this.upto = null;
		this.goal = null;
		this.garde = new dunit();
		this.error= "none";
	}
	Date upto;
	Date goal;
	dunit garde;
	String error;

}
