/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.worksheet.services.qbe;

import java.util.Map;

import it.eng.qbe.query.Query;
import it.eng.qbe.query.catalogue.QueryCatalogue;
import it.eng.spago.base.SourceBean;
import it.eng.spagobi.engines.qbe.QbeEngineInstance;
import it.eng.spagobi.engines.worksheet.WorksheetEngineInstance;
import it.eng.spagobi.engines.worksheet.services.initializers.WorksheetEngineStartAction;
import it.eng.spagobi.utilities.engines.EngineConstants;
import it.eng.spagobi.utilities.engines.SpagoBIEngineRuntimeException;

import org.apache.log4j.Logger;

/**
 * @authors Alberto Ghedin (alberto.ghedin@eng.it)
 *          Davide Zerbetto (davide.zerbetto@eng.it)
 *
 */
public class StartWorksheetFromQbeAction extends WorksheetEngineStartAction {

	private static final long serialVersionUID = 7152366345295485818L;

	public static transient Logger logger = Logger.getLogger(StartWorksheetFromQbeAction.class);
    
    public static final String ENGINE_NAME = "SpagoBIWorksheetEngine";
	
    @Override
	public SourceBean getTemplateAsSourceBean() {
//    	SourceBean toReturn = WorksheetXMLTemplateParser.getEmtpyWorsheetTemplate();
//    	return toReturn;
    	return null;
	}
    
    @Override
    protected QbeEngineInstance getQbeEngineInstance(WorksheetEngineInstance worksheetEngineInstance) {
    	QbeEngineInstance qbeEngineInstance = (QbeEngineInstance) getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
    	// the following instructions are needed because: if a user enter a saved query and then click on the worksheet tab,
    	// there is no active query and therefore worksheet initialization fails.
    	Query query = qbeEngineInstance.getActiveQuery();
    	if (query == null) {
    		QueryCatalogue catalogue = qbeEngineInstance.getQueryCatalogue();
    		if (catalogue == null || catalogue.getAllQueries().size() == 0) {
    			logger.error("Query catalogue is empty: no queries are defined");
    			throw new SpagoBIEngineRuntimeException("No queries are defined");
    		}
    		qbeEngineInstance.setActiveQuery(catalogue.getFirstQuery()); // this builds the statement object
    	}
		return qbeEngineInstance;
	}
    
    @Override
    protected boolean goToWorksheetPreentation() {
		return false;
	}

    // We must override the getEnv method, since the WorksheetEngine must inherit the same environment 
    // (that contains analytical drivers) of Qbe engine instance
	@Override
	public Map getEnv() {
		QbeEngineInstance qbeEngineInstance = (QbeEngineInstance) getAttributeFromSession(EngineConstants.ENGINE_INSTANCE);
		return qbeEngineInstance.getEnv();
	}
    
    
}
