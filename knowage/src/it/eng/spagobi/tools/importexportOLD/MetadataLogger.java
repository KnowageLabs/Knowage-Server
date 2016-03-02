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
package it.eng.spagobi.tools.importexportOLD;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Buffer for the import log messages
 */
public class MetadataLogger {

	private StringBuffer logBuf =  null;
	
	/**
	 * Constructor, initialize the buffer with date and time.
	 */
	public MetadataLogger() {
		logBuf = new StringBuffer();
		Calendar today = new GregorianCalendar();
		int day = today.get(Calendar.DAY_OF_MONTH);
		int month = today.get(Calendar.MONTH) + 1;
		int year = today.get(Calendar.YEAR);
		int hour = today.get(Calendar.HOUR_OF_DAY);
		int minute = today.get(Calendar.MINUTE);
		logBuf.append("Import of the day "+day+"/"+month+"/"+year+" started at "+hour+":"+minute+" \n\n");
	}
	
	/**
	 * Logs a message into the buffer.
	 * 
	 * @param msg The message to log
	 */
	public void log(String msg) {
		logBuf.append(msg + "\n");
	}
	
	
	/**
	 * Gets the array of bytes of all the logs.
	 * 
	 * @return The logs bytes
	 */
	public byte[] getLogBytes(){
		String bufStr = logBuf.toString();
		return bufStr.getBytes();
	}
	
}
