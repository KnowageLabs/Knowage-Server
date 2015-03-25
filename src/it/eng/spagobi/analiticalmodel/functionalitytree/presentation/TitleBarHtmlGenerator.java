/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import it.eng.spagobi.analiticalmodel.document.service.ExecutionWorkspaceModule;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class TitleBarHtmlGenerator implements ITreeHtmlGenerator {

	protected HttpServletRequest httpRequest = null;	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpReq, String initialPath) {
		httpRequest = httpReq;	
		IUrlBuilder urlBuilder = UrlBuilderFactory.getUrlBuilder();
		StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("				<div class='UITabs'>\n");
		htmlStream.append("					<div class='first-tab-level' >\n");
		Iterator it = objectsList.iterator();
		while (it.hasNext()) {
			LowFunctionality folder = (LowFunctionality) it.next();
			String linkClass = "tab";
			if (folder.getPath().equals(initialPath)) linkClass = "tab selected";
			htmlStream.append("						<div class='" + linkClass + "'>\n");
			Map changeFolderUrlPars = new HashMap();
			changeFolderUrlPars.put(ObjectsTreeConstants.PAGE, ExecutionWorkspaceModule.MODULE_PAGE);
			changeFolderUrlPars.put(TreeObjectsModule.PATH_SUBTREE, folder.getPath());
			if(ChannelUtilities.isWebRunning()) {
				changeFolderUrlPars.put(SpagoBIConstants.WEBMODE, "TRUE");
			}
			String changeFolderUrl = urlBuilder.getUrl(httpRequest, changeFolderUrlPars);
			htmlStream.append("							<a href='" + changeFolderUrl + "'>\n");
			htmlStream.append("								" + folder.getName() + "\n");
			htmlStream.append("							</a>\n");
			htmlStream.append("						</div>\n");
		}
		htmlStream.append("");
		htmlStream.append("					</div>");
		htmlStream.append("				</div>");
		return htmlStream;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath, String treename) {
		return makeTree(objectsList, httpRequest, initialPath);
	}

}
