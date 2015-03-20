/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.engines.dossier.treegenerators;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.configuration.ConfigSingleton;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.engines.config.bo.Engine;
import it.eng.spagobi.engines.dossier.constants.DossierConstants;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

public class DocumentsTreeHtmlGenerator implements ITreeHtmlGenerator {

    HttpServletRequest httpRequest = null;
    RequestContainer reqCont = null;
    protected IUrlBuilder urlBuilder = null;
    protected IMessageBuilder msgBuilder = null;
    int progrJSTree = 0;
    IEngUserProfile profile = null;
    private int dTreeRootId = -100;
    private int dTreeObjects = -1000;
    protected String requestIdentity = null;
    protected String currTheme="";

    /**
     * Make accessible tree.
     * 
     * @param dataTree the data tree
     * @param httpRequest the http request
     * 
     * @return the string buffer
     */
    public StringBuffer makeAccessibleTree(SourceBean dataTree, HttpServletRequest httpRequest) {
	StringBuffer htmlStream = new StringBuffer();
	return htmlStream;
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
     */
    public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath, String treename) {
	return makeTree(objectsList, httpRequest, initialPath);
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public StringBuffer makeTree(List objectsList, HttpServletRequest httpReq, String initialPath) {
	// identity string for object of the page
	UUIDGenerator uuidGen = UUIDGenerator.getInstance();
	UUID uuid = uuidGen.generateTimeBasedUUID();
	requestIdentity = uuid.toString();
	requestIdentity = requestIdentity.replaceAll("-", "");
	httpRequest = httpReq;
	reqCont = ChannelUtilities.getRequestContainer(httpRequest);
	urlBuilder = UrlBuilderFactory.getUrlBuilder();
	msgBuilder = MessageBuilderFactory.getMessageBuilder();
	SessionContainer sessionContainer = reqCont.getSessionContainer();
	SessionContainer permanentSession = sessionContainer.getPermanentContainer();
	profile = (IEngUserProfile) permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
	StringBuffer htmlStream = new StringBuffer();
	
	currTheme=ThemesManager.getCurrentTheme(reqCont);
	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
	
	htmlStream.append("<LINK rel='StyleSheet' href='" + urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css",currTheme)
		+ "' type='text/css' />");
	// makeConfigurationDtree(htmlStream);
	String nameTree = msgBuilder.getMessage("tree.objectstree.name", "messages", httpRequest);
	htmlStream.append("<SCRIPT language='JavaScript' src='"
		+ urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js",currTheme) + "'></SCRIPT>");
	htmlStream.append("<SCRIPT language='JavaScript' src='"
		+ urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js",currTheme) + "'></SCRIPT>");
	htmlStream.append("<div id='divmenuFunct' class='dtreemenu' onmouseout='hideMenu(event);' >");
	htmlStream.append("		menu");
	htmlStream.append("</div>");
	htmlStream.append("<table width='100%'>");
	htmlStream.append("	<tr height='1px'>");
	htmlStream.append("		<td width='10px'>&nbsp;</td>");
	htmlStream.append("		<td>&nbsp;</td>");
	htmlStream.append("	</tr>");
	htmlStream.append("	<tr>");
	htmlStream.append("		<td>&nbsp;</td>");
	htmlStream.append("		<td id='treeDossierObjTd" + requestIdentity + "' name='treeDossierObjTd" + requestIdentity
		+ "'>&nbsp;</td>");
	htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	htmlStream.append("				var nameTree = 'treeCMS';\n");
	String context=httpRequest.getContextPath();
	if (!(context.charAt(context.length() - 1) == '/')) {
		context += '/';
	}
	context+="themes/"+currTheme+"/";
	htmlStream.append("				treeCMS = new dTree('treeCMS', '" + context + "');\n");
	htmlStream.append("	        	treeCMS.add(" + dTreeRootId + ",-1,'" + nameTree + "');\n");
	Iterator it = objectsList.iterator();
	while (it.hasNext()) {
	    LowFunctionality folder = (LowFunctionality) it.next();
	    if (initialPath != null) {
		if (initialPath.equalsIgnoreCase(folder.getPath()))
		    addItemForJSTree(htmlStream, folder, true);
		else
		    addItemForJSTree(htmlStream, folder, false);
	    } else {
		if (folder.getParentId() == null)
		    addItemForJSTree(htmlStream, folder, true);
		else
		    addItemForJSTree(htmlStream, folder, false);
	    }
	}
	htmlStream.append("				document.getElementById('treeDossierObjTd" + requestIdentity
		+ "').innerHTML = treeCMS;\n");
	htmlStream.append("			</script>\n");
	htmlStream.append("	</tr>");
	htmlStream.append("</table>");
	return htmlStream;
    }

    private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, boolean isRoot) {

	String nameLabel = folder.getName();
	String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
	String codeType = folder.getCodType();
	Integer idFolder = folder.getId();
	Integer parentId = folder.getParentId();

	if (isRoot) {
	    htmlStream.append("	treeCMS.add(" + idFolder + ", " + dTreeRootId + ",'" + name
		    + "', '', '', '', '', '', 'true');\n");
	} else {
	    if (codeType.equalsIgnoreCase(SpagoBIConstants.LOW_FUNCTIONALITY_TYPE_CODE)) {
		String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif",currTheme);
		String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif",currTheme);
		htmlStream.append("	treeCMS.add(" + idFolder + ", " + parentId + ",'" + name + "', '', '', '', '"
			+ imgFolder + "', '" + imgFolderOp + "', '', '');\n");

		List objects = folder.getBiObjects();
		for (Iterator it = objects.iterator(); it.hasNext();) {
		    BIObject obj = (BIObject) it.next();
		    Engine engine = obj.getEngine();
		    String objTypeCode = obj.getBiObjectTypeCode();
		    ConfigSingleton config = ConfigSingleton.getInstance();
		    SourceBean technologyFilterSB = (SourceBean) config
			    .getAttribute(DossierConstants.DOSSIER_DRIVER_FILTER_SB);
		    String technologyFilter = (String) technologyFilterSB.getAttribute("match");
		    if (objTypeCode.equalsIgnoreCase(SpagoBIConstants.REPORT_TYPE_CODE)
			    && engine.getDriverName().toLowerCase().indexOf(technologyFilter) != -1) {
			htmlStream.append("	treeCMS.add(" + dTreeObjects-- + ", " + idFolder + ",'" + obj.getName()
				+ "', 'javascript:linkEmpty()', '', '', '', '', '', '', '"
				+ DossierConstants.DOSSIER_CONFIGURED_BIOBJECT_ID + "', '" + obj.getId() + "' );\n");
		    }
		}
	    }
	}
    }

    /* (non-Javadoc)
     * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
     */
    public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
	return null;
    }

}
