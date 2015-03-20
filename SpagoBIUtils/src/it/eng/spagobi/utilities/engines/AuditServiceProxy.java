/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.utilities.engines;

import it.eng.spagobi.utilities.callbacks.audit.AuditAccessUtils;

import javax.servlet.http.HttpSession;

/**
 * @author Andrea Gioia (andrea.gioia@eng.it)
 *
 */
public class AuditServiceProxy {
	
	private String auditId;
	private String userId;
	private HttpSession session;
	private AuditAccessUtils proxy;
	
	public AuditServiceProxy(String auditId, String userId, HttpSession session) {
		setAuditId(auditId);
		setUserId(userId);
		setSession(session);
		proxy = new AuditAccessUtils(auditId);
	}

	private void setAuditId(String auditId) {
		this.auditId = auditId;
	}

	private void setUserId(String userId) {
		this.userId = userId;
	}

	private void setSession(HttpSession session) {
		this.session = session;
	}

	public void notifyServiceStartEvent() {
		proxy.updateAudit(session, userId, auditId, 
				new Long(System.currentTimeMillis()), null, 
				"EXECUTION_STARTED", null, null);
	}

	public void notifyServiceErrorEvent(String msg) {
		proxy.updateAudit(session, userId, auditId,  
				null, new Long(System.currentTimeMillis()), 
				"EXECUTION_FAILED", msg, null);
	}

	public void notifyServiceEndEvent() {
		proxy.updateAudit(session, userId, auditId,  
				null, new Long(System.currentTimeMillis()), 
				"EXECUTION_PERFORMED", null, null);
		
	}

}
