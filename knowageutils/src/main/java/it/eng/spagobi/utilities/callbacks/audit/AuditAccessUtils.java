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
package it.eng.spagobi.utilities.callbacks.audit;

import it.eng.spagobi.services.proxy.AuditServiceProxy;

import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

public class AuditAccessUtils implements Serializable{

    static private Logger logger = Logger.getLogger(AuditAccessUtils.class);
    private List _auditIds;

    // private boolean _isNewExecution = true;

    /**
     * Instantiates a new audit access utils.
     * 
     * @param auditId the audit id
     */
    public AuditAccessUtils(String auditId) {
	_auditIds = new ArrayList();
	_auditIds.add(auditId);
    }

    /**
     * Gets the audit ids.
     * 
     * @return the audit ids
     */
    public List getAuditIds() {
	return _auditIds;
    }

    /**
     * Adds the audit id.
     * 
     * @param auditId the audit id
     */
    public void addAuditId(String auditId) {
	_auditIds.add(auditId);
    }

    /**
     * Updates the audit record with the id specified using the constructor or
     * by the setAuditId method. It makes an http call to the servlet specified
     * using the constructor or by the setAuditServlet method. If the current
     * execution is not a new one (examples: page refresh, portlet rendering)
     * nothing is updated.
     * 
     * @param auditId The id of the audit record to be modified
     * @param startTime The start time
     * @param endTime The end time
     * @param executionState The execution state
     * @param errorMessage The error message
     * @param errorCode The error code
     * @param session the session
     * @param userId the user id
     */
    public void updateAudit(HttpSession session, String userId,String auditId, Long startTime, Long endTime,
	    String executionState, String errorMessage, String errorCode) {
	logger.debug("IN");
	logger.debug("*** updateAudit userId: " + userId);
	try {
	    if (auditId == null || !_auditIds.contains(auditId))
		return;
	    // limits errorMessage length
	    if (errorMessage != null && errorMessage.length() > 390) {
		errorMessage = errorMessage.substring(0, 390);
	    }

	    AuditServiceProxy proxy = new AuditServiceProxy(userId,session); 
	    String ris = proxy.log( auditId, startTime != null ? startTime
		    .toString() : "",
		    endTime != null ? endTime.toString() : "",
		    executionState != null ? executionState : "",
		    errorMessage != null ? errorMessage : "",
		    errorCode != null ? errorCode : "");

	    if (ris != null && ris.equals("KO")) {
		logger.warn("Audit service doesn't work correctly!!!");
	    }
	} catch (Exception e) {
	    logger.error("Audit service don't work!!!",e);
	} finally {
	    logger.debug("OUT");
	}
    }

}
