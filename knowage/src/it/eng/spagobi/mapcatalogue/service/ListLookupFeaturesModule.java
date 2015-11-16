/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.mapcatalogue.service;

import it.eng.spago.base.SourceBean;
import it.eng.spago.dispatching.module.list.basic.AbstractBasicListModule;
import it.eng.spago.paginator.basic.ListIFace;
import it.eng.spagobi.commons.services.DelegatedHibernateConnectionListService;
/**
 * Loads the lov lookup list
 * 
 * @author sulis
 */

public class ListLookupFeaturesModule extends AbstractBasicListModule {
	
	public static final String MODULE_PAGE = "FeaturesLookupPage";
	
	/**
	 * Class Constructor.
	 */
	public ListLookupFeaturesModule() {
		super();
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
		response.setAttribute("MAP_ID", request.getAttribute("MAP_ID"));
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	} 

} 

