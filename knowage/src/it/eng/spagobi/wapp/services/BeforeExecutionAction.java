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
import it.eng.spago.dispatching.action.AbstractHttpAction;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.error.EMFUserError;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.wapp.bo.Menu;

import org.apache.log4j.Logger;

public class BeforeExecutionAction extends AbstractHttpAction{

	static private Logger logger = Logger.getLogger(BeforeExecutionAction.class);
	
	public void service(SourceBean request, SourceBean response)
			throws Exception {
		logger.debug("IN");
		try {
			String menuId = (String) request.getAttribute("MENU_ID");
			if (menuId!=null) {
			
				Menu menu=DAOFactory.getMenuDAO().loadMenuByID(Integer.valueOf(menuId));
				String parameters = menu.getObjParameters();
				logger.debug("using parameters " + parameters);
				String labelSubObject = menu.getSubObjName();
				logger.debug("using subobject " + labelSubObject);
				String snapName = menu.getSnapshotName();
				logger.debug("using snapshot name " + snapName);
				Integer snapHistory = menu.getSnapshotHistory();
				logger.debug("using snapshot history " + snapHistory);
		        boolean displayToolbar = !menu.getHideToolbar();
		        boolean displaySliders = !menu.getHideSliders();
		        // set into request all information for invoking ExecuteBIObjectModule.pageCreationHandler on loop call
				response.setAttribute(ObjectsTreeConstants.OBJECT_ID, menu.getObjId().toString());
				response.setAttribute(SpagoBIConstants.TOOLBAR_VISIBLE, new Boolean(displayToolbar).toString());
				response.setAttribute(SpagoBIConstants.SLIDERS_VISIBLE, new Boolean(displaySliders).toString());
		        if (parameters!= null && !parameters.trim().equalsIgnoreCase("")) {
		        	response.setAttribute(ObjectsTreeConstants.PARAMETERS, parameters);
		        }
		        if (labelSubObject != null && !labelSubObject.trim().equalsIgnoreCase("")) {
		        	response.setAttribute(SpagoBIConstants.SUBOBJECT_NAME, labelSubObject);
		        }
		        if (snapName != null && !snapName.trim().equalsIgnoreCase("")) {
		        	response.setAttribute(SpagoBIConstants.SNAPSHOT_NAME, snapName);
		        }
		        if (snapHistory != null) {
		        	response.setAttribute(SpagoBIConstants.SNAPSHOT_HISTORY_NUMBER, snapHistory.toString());
		        }
			} else {
				throw new Exception("Menu identifier not found on request");
			}
		} catch (EMFUserError e) {
			logger.error("Error loading menu", e);
			this.getErrorHandler().addError(e);
		} catch (Exception ex) {
			logger.error("Error loading menu", ex);
			EMFInternalError internalError = new EMFInternalError(EMFErrorSeverity.ERROR, ex);
			this.getErrorHandler().addError(internalError);
		} finally {
			logger.debug("OUT");
		}
	}

}
