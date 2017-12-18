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
