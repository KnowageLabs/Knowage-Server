/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.utils;

import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.engines.dossier.actions.DossierDownloadAction;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;


public class DossierUtilities {

	static private Logger logger = Logger.getLogger(DossierUtilities.class);
	
	/**
	 * Gets the dossier service url.
	 * 
	 * @return the dossier service url
	 */
	public static String getDossierServiceUrl() {
		logger.debug("IN");
		String sbiContAdd = GeneralUtilities.getSpagoBiHost()+GeneralUtilities.getSpagoBiContext();
		String toReturn = sbiContAdd + "/servlet/AdapterHTTP?NEW_SESSION=TRUE&ACTION_NAME=" + DossierDownloadAction.ACTION_NAME;
		logger.debug("OUT");
		return toReturn;
	}
	
	/**
	 * Gets the dossier service url.
	 * 
	 * @param request the request
	 * 
	 * @return the dossier service url
	 * @throws EMFUserError 
	 */
	public static String getDossierServiceUrl(HttpServletRequest request) {
		logger.debug("IN");
		String sbiContAdd = ChannelUtilities.getSpagoBIContextName(request);
		String toReturn = sbiContAdd + "/servlet/AdapterHTTP?NEW_SESSION=TRUE&ACTION_NAME=" + DossierDownloadAction.ACTION_NAME;
		logger.debug("OUT");
		return toReturn;
	}
	
}
