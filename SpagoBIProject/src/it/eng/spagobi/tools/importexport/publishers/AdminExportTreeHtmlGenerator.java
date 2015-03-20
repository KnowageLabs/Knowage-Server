/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.importexport.publishers;

import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.tools.importexport.ImportExportConstants;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Implements method to build the tree of objects to export
 */

public class AdminExportTreeHtmlGenerator extends AdminTreeHtmlGenerator {

    static private Logger logger = Logger.getLogger(AdminExportTreeHtmlGenerator.class);
	
	/**
	 * Builds the JavaScript object to make the tree. All code is appended into a
	 * String Buffer, which is then returned.
	 * 
	 * @param objectsList The list of objects and functionalities
	 * @param httpReq The http Servlet Request
	 * @param initialPath The tree initial path
	 * 
	 * @return the string buffer
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpReq, String initialPath) {
	    logger.debug("IN");
		// identity string for object of the page
	    UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
	    UUID uuid = uuidGen.generateTimeBasedUUID();
	    requestIdentity = uuid.toString();
	    requestIdentity = requestIdentity.replaceAll("-", "");
		httpRequest = httpReq;
		reqCont = ChannelUtilities.getRequestContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();

    	currTheme=ThemesManager.getCurrentTheme(reqCont);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		SessionContainer sessionContainer = reqCont.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
        profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css", currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.objectstree.name" ,"component_impexp_messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td id='treeExportObjTd" + requestIdentity + "' name='treeExportObjTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	   	htmlStream.append("				var nameTree = 'treeCMS';\n");
		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}
		context+="themes/"+currTheme+"/";
	   	htmlStream.append("				treeCMS = new dTree('treeCMS', '" + context + "');\n");
	   	htmlStream.append("	        	treeCMS.add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
	   	Iterator it = objectsList.iterator();
	   	while (it.hasNext()) {
	   		LowFunctionality folder = (LowFunctionality) it.next();
	   		/* ********* start luca changes *************** */
	   		boolean isUserFunct = folder.getCodType().equals(SpagoBIConstants.USER_FUNCTIONALITY_TYPE_CODE);
	   		if(isUserFunct) {
	   			continue;
	   		}
	   		/* ********* end luca changes ***************** */
	   		if (initialPath != null) {
	   			if (initialPath.equalsIgnoreCase(folder.getPath())) addItemForJSTree(htmlStream, folder, true);
	   			else addItemForJSTree(htmlStream, folder, false);
	   		} else {
	   			if (folder.getParentId() == null) addItemForJSTree(htmlStream, folder, true);
	   			else addItemForJSTree(htmlStream, folder, false);
	   		}
	   	}
    	//htmlStream.append("				document.write(treeCMS);\n");
    	htmlStream.append("				document.getElementById('treeExportObjTd" + requestIdentity + "').innerHTML = treeCMS;\n");
    	makeJSFunctionForMenu(htmlStream);	
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		htmlStream.append("<div id='divmenuFunct" + requestIdentity + "' class='dtreemenu' onmouseout='hideMenu(event, \"divmenuFunct" + requestIdentity + "\");' >");
		htmlStream.append("		menu");
		htmlStream.append("</div>");
		logger.debug("OUT");
		return htmlStream;
	}
	
	
	
	
	private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, boolean isRoot) {
	    logger.debug("IN");
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		String path = folder.getPath();
		String codeType = folder.getCodType();
		Integer idFolder = folder.getId();
		Integer parentId = folder.getParentId();

		if (isRoot) {
			htmlStream.append("	treeCMS.add(" + idFolder + ", " + dTreeRootId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '', '', 'true', 'menu" + requestIdentity + "(event, \\'"+path+"\\')');\n");
		} else {
			if(codeType.equalsIgnoreCase(SpagoBIConstants.LOW_FUNCTIONALITY_TYPE_CODE)) {
				String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif",currTheme);
				String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif",currTheme);
				name = name.replace('\'',' ' );
				htmlStream.append("	treeCMS.add(" + idFolder + ", " + parentId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'"+path+"\\')');\n");
				List objects = folder.getBiObjects();
				for (Iterator it = objects.iterator(); it.hasNext(); ) {
					BIObject obj = (BIObject) it.next();
					String nameObj = obj.getName();
					Integer idObj = obj.getId();
					obj.getFunctionalities();
					String stateObj = obj.getStateCode();
					
					nameObj = nameObj.replace('\'',' ' );
					htmlStream.append("	treeCMS.add("+dTreeObjects--+", "+idFolder+",'"+nameObj+"', 'javascript:linkEmpty()', '', '', '', '', '', '', '"+ImportExportConstants.OBJECT_ID_PATHFUNCT+"', '"+idObj+"_"+path+"');\n");
				}
			}
		}
		logger.debug("OUT");
	}
	
	
	
	/**
	 * Add the javascript function to manage the tree context menu, the selection and deselection of a 
	 * particular branch
	 */
	protected void makeJSFunctionForMenu(StringBuffer htmlStream) {
		htmlStream.append("		function menu" + requestIdentity + "(event, pathFather) {\n");
		htmlStream.append("			divM = document.getElementById('divmenuFunct" + requestIdentity + "');\n");
		htmlStream.append("			divM.innerHTML = '';\n");
		String capSelect = msgBuilder.getMessage("SBISet.importexport.selectall", "component_impexp_messages", httpRequest);
		htmlStream.append("			divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:select(\\''+pathFather+'\\')\">"+capSelect+"</a></div>';\n");
		String capDeselect = msgBuilder.getMessage("SBISet.importexport.deselectall", "component_impexp_messages", httpRequest);
		htmlStream.append("			divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:deselect(\\''+pathFather+'\\')\">"+capDeselect+"</a></div>';\n");
		htmlStream.append("			showMenu(event, divM);\n");
		htmlStream.append("		}\n");

		
		htmlStream.append("		function linkEmpty() {\n");
		htmlStream.append("		}\n");
		
		htmlStream.append("		function select(path) {\n");
		htmlStream.append("			var checkColl = document.getElementsByName('"+ImportExportConstants.OBJECT_ID_PATHFUNCT+"');\n");
		htmlStream.append("		    for(var i=0; i< checkColl.length; i++){\n");
		htmlStream.append("             value = checkColl[i].value;\n"); 
		htmlStream.append("             if(value.indexOf(path)!= -1) {\n"); 
		htmlStream.append("		    		if(!checkColl[i].checked){\n");
		htmlStream.append("		    			checkColl[i].click();\n");
		htmlStream.append("		    		}\n");
		htmlStream.append("		    	}\n");
		htmlStream.append("		    }\n");
		htmlStream.append("		}\n");
		
		htmlStream.append("		function deselect(path) {\n");
		htmlStream.append("			var checkColl = document.getElementsByName('"+ImportExportConstants.OBJECT_ID_PATHFUNCT+"');\n");
		htmlStream.append("		    for(var i=0; i< checkColl.length; i++){\n");
		htmlStream.append("             value = checkColl[i].value;\n"); 
		htmlStream.append("             if(value.indexOf(path)!= -1) {\n"); 
		htmlStream.append("		    		if(checkColl[i].checked){\n");
		htmlStream.append("		    			checkColl[i].click();\n");
		htmlStream.append("		    		}\n");
		htmlStream.append("		    	}\n");
		htmlStream.append("		    }\n");
		htmlStream.append("		}\n");
	}
	
	
	
}
