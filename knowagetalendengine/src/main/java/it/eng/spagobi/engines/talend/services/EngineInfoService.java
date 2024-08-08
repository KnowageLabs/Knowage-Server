/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.talend.services;

import java.util.Date;

import it.eng.spagobi.engines.talend.TalendEngine;
import it.eng.spagobi.utilities.engines.AbstractEngineStartServlet;
import it.eng.spagobi.utilities.engines.EngineStartServletIOManager;
import it.eng.spagobi.utilities.engines.SpagoBIEngineException;


import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * @author Andrea Gioia
 *
 */
public class EngineInfoService extends AbstractEngineStartServlet {
	
	private static final String INFO_TYPE_PARAM_NAME = "infoType"; 
	private static final String INFO_TYPE_VERSION = "version"; 
	private static final String INFO_TYPE_COMPLIANCE_VERSION = "complianceVersion"; 
	private static final String INFO_TYPE_NAME = "name"; 
	
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = LogManager.getLogger(EngineInfoService.class);
	
	
	@Override
	public void service(EngineStartServletIOManager servletIOManager) throws SpagoBIEngineException {
		
		String infoType;
		String responseMessage;
		
		LOGGER.debug("IN");
		
		try {	
				
			infoType = servletIOManager.getParameterAsString(INFO_TYPE_PARAM_NAME);
		
			if(INFO_TYPE_VERSION.equalsIgnoreCase( infoType )) {
				responseMessage = TalendEngine.getVersion().toString();
			} else if(INFO_TYPE_COMPLIANCE_VERSION.equalsIgnoreCase( infoType)) {
				responseMessage = TalendEngine.getVersion().getComplianceVersion();
			} else if (INFO_TYPE_NAME.equalsIgnoreCase( infoType )) {
				responseMessage = TalendEngine.getVersion().getFullName();
			} else {
				responseMessage = TalendEngine.getVersion().getInfo();
			}
			
			servletIOManager.tryToWriteBackToClient( responseMessage );
			
		} finally {
			LOGGER.debug("OUT");
		}		
	}
	
	public void auditServiceStartEvent() {
		LOGGER.info("EXECUTION_STARTED: {}", new Date(System.currentTimeMillis()));
	}

	public void auditServiceErrorEvent() {
		LOGGER.info("EXECUTION_FAILED: {}", new Date(System.currentTimeMillis()));
	}

	public void auditServiceEndEvent() {
		LOGGER.info("EXECUTION_PERFORMED: {}", new Date(System.currentTimeMillis()));	
	}
	
	
}

