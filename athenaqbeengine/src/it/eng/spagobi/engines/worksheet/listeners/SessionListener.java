/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.listeners;

import java.util.Iterator;

import it.eng.spagobi.utilities.database.temporarytable.TemporaryTable;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableManager;
import it.eng.spagobi.utilities.database.temporarytable.TemporaryTableRecorder;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

/**
 * @author Davide Zerbetto (davide.zerbetto@eng.it)
 */
public class SessionListener implements HttpSessionListener {

	public static transient Logger logger = Logger.getLogger(SessionListener.class);
	
	public void sessionCreated(HttpSessionEvent event) {
		// nothing to do
	}

	public void sessionDestroyed(HttpSessionEvent event) {
		logger.debug("IN");
		String attributeName = TemporaryTableRecorder.class.getName();
		TemporaryTableRecorder recorder = (TemporaryTableRecorder) event.getSession().getAttribute(attributeName);
		if (recorder != null) {
			Iterator<TemporaryTable> it = recorder.iterator();
			while (it.hasNext()) {
				TemporaryTable tt = it.next();
				TemporaryTableManager.removeLastDataSetSignature(tt.getTableName());
				TemporaryTableManager.removeLastDataSetTableDescriptor(tt.getTableName());
				try {
					TemporaryTableManager.dropTableIfExists(tt.getTableName(), tt.getDataSource());
					logger.debug(
							"Temporary table with name ["
									+ tt.getTableName() + "] at datasource ["
									+ tt.getDataSource() + "] dropped successfully");
				} catch (Exception e) {
					logger.error(
							"Error while dropping temporary table with name ["
									+ tt.getTableName() + "] at datasource ["
									+ tt.getDataSource() + "]", e);
					continue;
				}
			}
		}
		logger.debug("OUT");
	}

}
