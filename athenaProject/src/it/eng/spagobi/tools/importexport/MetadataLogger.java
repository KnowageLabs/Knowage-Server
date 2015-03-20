/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport;

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
