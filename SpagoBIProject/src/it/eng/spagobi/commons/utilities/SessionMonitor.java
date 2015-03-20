/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.commons.utilities;

import it.eng.spago.base.SessionContainer;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

import java.util.List;

/**
 * @author Gioia
 *	
 *	provides mothods for a quick and dirty session monitoring and debugging
 */
public class SessionMonitor {
	
	/**
	 * Prints the session.
	 * 
	 * @param session the session
	 */
	public static void printSession(SessionContainer session) {
		List list = session.getAttributeNames();
		for(int i = 0; i < list.size(); i++) {
			SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, SessionMonitor.class.getName(),
					            "printSession", list.get(i).toString());
		}				
	}
}
