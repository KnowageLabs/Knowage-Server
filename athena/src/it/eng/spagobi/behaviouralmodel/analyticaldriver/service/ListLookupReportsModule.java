/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.behaviouralmodel.analyticaldriver.service;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.base.SourceBeanAttribute;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.services.DelegatedHibernateConnectionListService;
import it.eng.spagobi.commons.utilities.SpagoBITracer;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads the parameters lookup list
 * 
 * @author Andrea Gioia
 */

public class ListLookupReportsModule extends AbstractBasicListModule {
	
	public static final String MODULE_PAGE = "ReportsLookupPage";
	
	/**
	 * Class Constructor.
	 */
	public ListLookupReportsModule() {
		super();
	} 
	
	/* (non-Javadoc)
	 * @see it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		SessionContainer session = this.getRequestContainer().getSessionContainer();
		
		String message = (String) session.getAttribute("MESSAGE");		
		if(message!=null){
			request.setAttribute("MESSAGE", message);
			session.delAttribute("MESSAGE");
		}
		
		String page = (String) session.getAttribute("LIST_PAGE");
		if(page!=null){
			request.setAttribute("LIST_PAGE", page);
			session.delAttribute("LIST_PAGE");
		}
		super.service(request, response); 
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
		
		
		
		SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, 
	            "DetailBIObjectPublisher", 
	            "getPublisherName", 
	            "REQ: " + request);
		
		getSubreportsId(request);
		
		
		
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	}
	
	private List getSubreportsId(SourceBean request){
		List results = new ArrayList();
		List attrs = request.getContainedAttributes();
		for(int i = 0; i < attrs.size(); i++){
			SourceBeanAttribute attr = (SourceBeanAttribute)attrs.get(i);
			SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, "ListLookupReportsModule","service", " ATTR -> " + attr.getKey() + "=" + attr.getValue());
			String key = (String)attr.getKey();
			if(key.startsWith("checkbox")) {
				String id = key.substring(key.indexOf(':')+1, key.length());
				SpagoBITracer.debug(SpagoBIConstants.NAME_MODULE, "ListLookupReportsModule","service", " ATTR [OK] " + id);
				results.add(new Integer(id));
			}
		}
		
		return results;
	}
	
}
