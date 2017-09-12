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
package it.eng.spagobi.behaviouralmodel.analyticaldriver.service;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.error.EMFErrorHandler;
import it.eng.spago.error.EMFErrorSeverity;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.Parameter;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterDAO;
import it.eng.spagobi.behaviouralmodel.analyticaldriver.dao.IParameterUseDAO;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.services.DelegatedHibernateConnectionListService;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;

/**
 * Loads the parameters lookup list
 * 
 * @author Zerbetto
 */

public class ListLookupParametersModule extends AbstractBasicListModule {
	static private Logger logger = Logger.getLogger(ListLookupParametersModule.class);
	public static final String MODULE_PAGE = "ParametersLookupPage";
	EMFErrorHandler errorHandler = null;
	
	/**
	 * Class Constructor.
	 */
	public ListLookupParametersModule() {
		super();
	} 
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		String message = (String) request.getAttribute("MESSAGEDET");	
		errorHandler  = getErrorHandler();
		if(message != null && message.equalsIgnoreCase("EXIT_FROM_MODULE")) {
			String returnState = (String) request.getAttribute("RETURN_STATE");
			response.setAttribute("PUBLISHER_NAME",  "ReturnBackPublisher");			
			RequestContainer requestContainer = this.getRequestContainer();	
			SessionContainer session = requestContainer.getSessionContainer();
			session.setAttribute("RETURN_FROM_MODULE", "ListLookupParametersModule");
			session.setAttribute("RETURN_STATUS", returnState);
			if(returnState.equalsIgnoreCase("SELECT"))
				session.setAttribute("PAR_ID", request.getAttribute("PAR_ID"));	
			
			return;
		}
		else if (message != null && message.trim().equalsIgnoreCase(AdmintoolsConstants.DETAIL_DEL)) {
			delDetailParameter(request);
			response.setAttribute("PUBLISHER_NAME",  "ReturnListPublisher");			
		 	return;
		}		
		super.service(request, response); 
		
		response.setAttribute("PUBLISHER_NAME",  "ParametersLookupPublisher");
		logger.debug("PUBLISHER_NAME = " + "ParametersLookupPublisher");
	}
	
	/**
	 * Gets the list.
	 * 
	 * @param request The request SourceBean
	 * @param response The response SourceBean
	 * 
	 * @return ListIFace
	 * 
	 * @throws Exception the exception
	 */
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	}
	
	private void delDetailParameter(SourceBean request)
											throws EMFUserError, SourceBeanException {
		try {
			IParameterDAO parDAO = DAOFactory.getParameterDAO();
			IParameterUseDAO parUseDAO = DAOFactory.getParameterUseDAO();
			String id = (String) request.getAttribute("PAR_ID");
			//controls if the parameter has any object associated
			List objectsLabels = DAOFactory.getBIObjectParameterDAO().getDocumentLabelsListUsingParameter(new Integer(id));
			if (objectsLabels != null && objectsLabels.size() > 0){
				HashMap params = new HashMap();
				params.put(AdmintoolsConstants.PAGE, MODULE_PAGE);
				Vector v = new Vector();
				v.add(objectsLabels.toString());
				EMFUserError error = new EMFUserError(EMFErrorSeverity.ERROR, 1017, v, params);
				errorHandler.addError(error);
				return;
			}
			
			//controls if the parameter has any use associated
			boolean hasUse = DAOFactory.getParameterUseDAO().hasParUseModes(id);
			if (hasUse){
					parUseDAO.eraseParameterUseByParId(Integer.valueOf(id));
			}
			//end of control
			
			Parameter parameter =  parDAO.loadForDetailByParameterID(new Integer(id)); 
			parDAO.eraseParameter(parameter);
			logger.debug("Parameter deleted correctly from lookup parmeters!");
		} catch (Exception ex) {
			logger.error("Cannot fill response container" , ex  );
			throw new EMFUserError(EMFErrorSeverity.ERROR, 100);
		}
	}
}
