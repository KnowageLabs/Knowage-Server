/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package it.eng.knowage.commons.utilities;

public class TimeUtilities {
	
	private static final int SECOND = 1000;
	private static final int MINUTE = 60 * SECOND;
	private static final int HOUR = 60 * MINUTE;
	private static final int DAY = 24 * HOUR;
	
	public static String getUptimeInDays(long ms) {
		
		StringBuffer text = new StringBuffer("");
		if (ms > DAY) {
		  text.append(ms / DAY).append(" days ");
		  ms %= DAY;
		}
		if (ms > HOUR) {
		  text.append(ms / HOUR).append(" hours ");
		  ms %= HOUR;
		}
		if (ms > MINUTE) {
		  text.append(ms / MINUTE).append(" minutes ");
		  ms %= MINUTE;
		}
		if (ms > SECOND) {
		  text.append(ms / SECOND).append(" seconds ");
		  ms %= SECOND;
		}
		text.append(ms + " ms");
		return text.toString();	
	}

}
