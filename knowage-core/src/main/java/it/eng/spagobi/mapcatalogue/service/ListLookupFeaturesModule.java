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
	@Override
	public ListIFace getList(SourceBean request, SourceBean response) throws Exception {
		response.setAttribute("MAP_ID", request.getAttribute("MAP_ID"));
		return DelegatedHibernateConnectionListService.getList(this, request, response);
	} 

} 

