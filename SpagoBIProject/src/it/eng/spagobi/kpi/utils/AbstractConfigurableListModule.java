/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.kpi.utils;

import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanException;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spago.paginator.basic.PaginatorIFace;
import it.eng.spago.paginator.basic.impl.GenericList;
import it.eng.spago.paginator.basic.impl.GenericPaginator;
import it.eng.spagobi.commons.SingletonConfig;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.DelegatedBasicListService;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class AbstractConfigurableListModule extends AbstractBasicListModule {
    private static transient Logger logger = Logger.getLogger(AbstractConfigurableListModule.class);	

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
		logger.debug("IN");
		PaginatorIFace paginator = new GenericPaginator();
		
		String currentFieldOrder = (request.getAttribute("FIELD_ORDER") == null || ((String) request
			.getAttribute("FIELD_ORDER")).equals("")) ? "" : (String) request.getAttribute("FIELD_ORDER");
		if (currentFieldOrder.equals("")) {
		    currentFieldOrder = "DESCR";
		    response.delAttribute("FIELD_ORDER");
		    response.setAttribute("FIELD_ORDER", currentFieldOrder);
		}
		
		String currentTypOrder = (request.getAttribute("TYPE_ORDER") == null || ((String) request
			.getAttribute("TYPE_ORDER")).equals("")) ? "" : (String) request.getAttribute("TYPE_ORDER");
		if (currentTypOrder.equals("")) {
		    currentTypOrder = " ASC";
		    response.delAttribute("TYPE_ORDER");
		    response.setAttribute("TYPE_ORDER", currentTypOrder);
		}

		int numRows = 10;
		try {
		    SingletonConfig spagoconfig = SingletonConfig.getInstance();
		    String lookupnumRows = spagoconfig.getConfigValue("SPAGOBI.LOOKUP.numberRows");
		    if (lookupnumRows != null) {
			numRows = Integer.parseInt(lookupnumRows);
		    }
		} catch (Exception e) {
		    numRows = 10;
		    logger.error("Error while recovering number rows for " + "lookup from configuration, usign default 10", e);
		}
		paginator.setPageSize(numRows);
		logger.debug("setPageSize="+numRows);
		
		
		logger.debug("Loading the list of object");
		List objectList = getObjectList(request);
		
		for (Iterator it = objectList.iterator(); it.hasNext();) {
		    Object obj = it.next();
		    SourceBean rowSB = new SourceBean("ROW"); 
		    setRowAttribute(rowSB, obj);
		    if (rowSB != null)
		    	paginator.addRow(rowSB);
		}
		ListIFace list = new GenericList();
		list.setPaginator(paginator);
		// filter the list
		String valuefilter = (String) request.getAttribute(SpagoBIConstants.VALUE_FILTER);
		if (valuefilter != null) {
		    String columnfilter = (String) request.getAttribute(SpagoBIConstants.COLUMN_FILTER);
		    String typeFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_FILTER);
		    String typeValueFilter = (String) request.getAttribute(SpagoBIConstants.TYPE_VALUE_FILTER);
		    list = DelegatedBasicListService.filterList(list, valuefilter, typeValueFilter, columnfilter, typeFilter,
			    getResponseContainer().getErrorHandler());
		}

		HashMap parametersMap = new HashMap();
		parametersMap.put(SpagoBIConstants.OBJECTS_VIEW, SpagoBIConstants.VIEW_OBJECTS_AS_LIST);
		response.setAttribute("PARAMETERS_MAP", parametersMap);
		logger.debug("OUT");
		
		
		
		return list;		
	}
	/**
	 * Get the list of business objects.
	 * @return The list of business objects.
	 */
	protected abstract List getObjectList(SourceBean request);
	
	/**
	 * Set the attribute of the ROW of the paginator.
	 * @param rowSB SourceBean ROW where to add the attribute.
	 * @param obj Business object where to get the attribute to set in the SourceBean
	 * @throws SourceBeanException
	 */
	protected abstract void setRowAttribute(SourceBean rowSB, Object obj)throws SourceBeanException ;
	
}
