/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.commons.services;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.commons.utilities.AuditLogUtilities;

import java.util.HashMap;

import org.apache.log4j.Logger;

public class LogoutAction extends AbstractHttpAction {

	static private Logger logger = Logger.getLogger(LogoutAction.class);
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		RequestContainer reqCont = RequestContainer.getRequestContainer();
		SessionContainer sessCont = reqCont.getSessionContainer();
		SessionContainer permSess = sessCont.getPermanentContainer();
		
		IEngUserProfile profile = (IEngUserProfile)permSess.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		if (profile != null) {
			logger.debug("User profile object found, removing it from session ...");
			permSess.setAttribute(IEngUserProfile.ENG_USER_PROFILE, null);
			HashMap<String, String> logParam = new HashMap<String, String>();
			logParam.put("USER", profile.toString());
			AuditLogUtilities.updateAudit(getHttpRequest(), profile, "SPAGOBI.Logout", logParam, "OK");
		} else {
			logger.debug("User profile object not found, most likely a previous session has expired");
		}

	}
	
}
