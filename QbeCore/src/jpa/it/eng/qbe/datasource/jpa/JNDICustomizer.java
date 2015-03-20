/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.qbe.datasource.jpa;

import org.eclipse.persistence.config.SessionCustomizer;
import org.eclipse.persistence.sessions.Connector;
import org.eclipse.persistence.sessions.JNDIConnector;
import org.eclipse.persistence.sessions.Session;

public class JNDICustomizer implements SessionCustomizer {

	public void customize(Session session) throws Exception {
		Connector connector = session.getLogin().getConnector();
		if(connector instanceof JNDIConnector) {
			 JNDIConnector jndiConnector = (JNDIConnector) session.getLogin().getConnector();
			 jndiConnector.setLookupType(JNDIConnector.STRING_LOOKUP);
		}
	}
}