/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
/*
 * Created on 21-apr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package it.eng.spagobi.commons.services;

import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.dispatching.module.AbstractModule;
import it.eng.spagobi.commons.constants.SpagoBIConstants;

import java.util.List;

public class ViewBuilderModule extends AbstractModule {

	/**
	 * Service.
	 * 
	 * @param request the request
	 * @param response the response
	 * 
	 * @throws Exception the exception
	 * 
	 * @see it.eng.spago.dispatching.action.AbstractHttpAction#service(it.eng.spago.base.SourceBean, it.eng.spago.base.SourceBean)
	 */
	public void service(SourceBean request, SourceBean response) throws Exception {
		String viewCode = (String)request.getAttribute("viewCode");
		ConfigSingleton configuration = ConfigSingleton.getInstance();
		SourceBean viewSB = (SourceBean)configuration.getFilteredSourceBeanAttribute("MENU.VIEW", "code", viewCode);
		List containers = viewSB.getAttributeAsList("CONTAINERS.CONTAINER");
		String widthViewStr = (String)viewSB.getAttribute("width");
		Integer widthView = new Integer(widthViewStr); 
		String heightViewStr = (String)viewSB.getAttribute("height");
		Integer heightView = new Integer(heightViewStr); 
		response.setAttribute(SpagoBIConstants.PUBLISHER_NAME, "ViewBuilder");
		response.setAttribute("CONTAINERS_LIST", containers);
		response.setAttribute("VIEW_HEIGHT", heightView);
		response.setAttribute("VIEW_WIDTH", widthView);
	}

}
