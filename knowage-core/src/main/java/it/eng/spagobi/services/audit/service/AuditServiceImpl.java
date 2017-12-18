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
package it.eng.spagobi.services.audit.service;

import it.eng.spagobi.monitoring.dao.AuditManager;
import it.eng.spagobi.services.common.AbstractServiceImpl;
import it.eng.spagobi.services.security.exceptions.SecurityException;

import org.apache.log4j.Logger;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

/**
 * Implementation of Audit Service
 */
public class AuditServiceImpl extends AbstractServiceImpl{

    static private Logger logger = Logger.getLogger(AuditServiceImpl.class);
    
    /**
     * Log.
     * 
     * @param token String
     * @param user String
     * @param id String
     * @param start String
     * @param end String
     * @param state String
     * @param message String
     * @param errorCode String
     * 
     * @return String
     */
    public String log(String token,String user,String id,String start,String end,String state,String message,String errorCode){
	logger.debug("IN");
	Monitor monitor =MonitorFactory.start("spagobi.service.audit.log");
	try {
	    validateTicket(token, user);
	    return log( user, id, start, end, state, message, errorCode);
	} catch (SecurityException e) {
	    logger.error("SecurityException", e);
	    return null;
	} finally {
	    monitor.stop();
	    logger.debug("OUT");
	}	

    }
	
	private String log(String user,String id,String start,String end,String state,String message,String errorCode){
	        logger.debug("IN.user="+user+" /id="+id+" /start="+start+" /end="+end+" /state="+state+" /message="+message+" /errorCode="+errorCode);
		// getting audit record id
		if (id == null) {
		    logger.warn("No operations will be performed");
		    return "KO";
		}
		logger.debug("Audit id = [" + id + "]");
		Integer auditId = new Integer(id);
		// getting execution start time
		Long startTime = null;
		
		if (start != null && !start.trim().equals("")) {
			try {
				startTime = new Long(start);
			} catch (NumberFormatException nfe) {
				logger.error("Execution start time = [" + start + "] not correct!", nfe);
			}
		}
		// getting execution end time
		Long endTime = null;

		if (end != null && !end.trim().equals("")) {
			try {
				endTime = new Long(end);
			} catch (NumberFormatException nfe) {
				logger.error("Execution end time = [" + end + "] not correct!", nfe);
			}
		}

		// saving modifications
		AuditManager auditManager = AuditManager.getInstance();
		auditManager.updateAudit(auditId, startTime, endTime, state, message, errorCode);
		return "";
	}    
}
