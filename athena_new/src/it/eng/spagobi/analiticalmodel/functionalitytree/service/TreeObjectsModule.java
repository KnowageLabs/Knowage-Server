/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads an objects hyerarchy (Tree) or set inforamtion for a single object execution
 */
public class TreeObjectsModule extends AbstractModule {

    public static final String PATH_SUBTREE = "PATH_SUBTREE";
    SessionContainer sessionContainer = null;
    EMFErrorHandler errorHandler = null;
    
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {	}

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		
		//String actor = (String) request.getAttribute(SpagoBIConstants.ACTOR);
		String initialPath = (String) request.getAttribute(TreeObjectsModule.PATH_SUBTREE);
		List functionalities = new ArrayList();
		boolean recoverBIObjects = true;
		String operation = (String) request.getAttribute(SpagoBIConstants.OPERATION);
		if (operation != null && operation.equals(SpagoBIConstants.FUNCTIONALITIES_OPERATION)) {
			// it means that only the functionalities will be displayed
			recoverBIObjects = false;
		} else if (operation != null && operation.equals(SpagoBIConstants.IMPORTEXPORT_OPERATION)) {
			// it means that all the tree documents and functionalities will be displayed
			response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "importexportHome");
			recoverBIObjects = true;
		}
		try {
			if (initialPath != null && !initialPath.trim().equals("")) {
				functionalities = DAOFactory.getLowFunctionalityDAO().loadSubLowFunctionalities(initialPath, recoverBIObjects);
				response.setAttribute(SpagoBIConstants.MODALITY, SpagoBIConstants.FILTER_TREE_MODALITY);
				response.setAttribute(TreeObjectsModule.PATH_SUBTREE, initialPath);
			} else {
				functionalities = DAOFactory.getLowFunctionalityDAO().loadAllLowFunctionalities(recoverBIObjects);
				response.setAttribute(SpagoBIConstants.MODALITY, SpagoBIConstants.ENTIRE_TREE_MODALITY);
			}
		} catch (EMFUserError e) {
			SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, 
					"TreeObjectsMOdule", 
					"defaultModalityHandler", 
					"Error loading functionalities", e);
		}
		response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, functionalities);
		//response.setAttribute(SpagoBIConstants.ACTOR, actor);
	}
	
}	
	
	
