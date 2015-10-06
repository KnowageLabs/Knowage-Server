/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.utils;

import org.apache.log4j.Logger;

public class MyJavaClassDestination extends JavaClassDestination {

	static protected Logger logger = Logger.getLogger(MyJavaClassDestination.class);

	@Override
	public void execute() {
		logger.debug("ciaoooo a tutti!");
	}

}
