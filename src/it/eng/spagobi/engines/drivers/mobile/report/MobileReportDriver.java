/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.drivers.mobile.report;

import java.util.Map;

import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.engines.drivers.generic.GenericDriver;

public class MobileReportDriver extends GenericDriver {

	@Override
	public Map getParameterMap(Object biobject, IEngUserProfile profile,
			String roleName) {
		throw new RuntimeException(
				"This kind of document cannot be executed within SpagoBI server; " +
				"log into SpagoBI Mobile application using a mobile device and retry");
	}

	@Override
	public Map getParameterMap(Object object, Object subObject,
			IEngUserProfile profile, String roleName) {
		throw new RuntimeException(
				"This kind of document cannot be executed within SpagoBI server; " +
				"log into SpagoBI Mobile application using a mobile device and retry");
	}

}
