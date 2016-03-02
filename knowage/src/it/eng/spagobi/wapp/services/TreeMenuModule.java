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
package it.eng.spagobi.wapp.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Loads an objects hyerarchy (Tree) or set inforamtion for a single object execution
 */
public class TreeMenuModule extends AbstractModule {

	static private Logger logger = Logger.getLogger(TreeMenuModule.class);
	
    public static final String MODULE_PAGE ="MenuConfigurationPage";
    
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.AbstractModule#init(it.eng.spago.base.SourceBean)
	 */
	public void init(SourceBean config) {	}

	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.service.ServiceIFace#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		try {
			List menues = new ArrayList();
			menues = DAOFactory.getMenuDAO().loadAllMenues();
			response.setAttribute(SpagoBIConstants.FUNCTIONALITIES_LIST, menues);
		} catch (EMFUserError e) {
			logger.error("Error loading menus", e);
			this.getErrorHandler().addError(e);
		} catch (Exception ex) {
			logger.error("Error loading menus", ex);
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			this.getErrorHandler().addError(internalError);
		}
	}
	
}	
	
	
