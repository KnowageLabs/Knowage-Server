/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.tools.scheduler.gui;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.tools.scheduler.to.DispatchContext;
import it.eng.spagobi.tools.scheduler.to.TriggerInfo;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Contains all methods needed to generate and modify a tree object for scheduling.
 * There are methods to generate tree, configure, insert and modify elements.
 */
public class SelectFunctionalityTreeHtmlGenerator implements ITreeHtmlGenerator {

	HttpServletRequest httpRequest = null;
	RequestContainer reqCont = null;
	protected IUrlBuilder urlBuilder = null;
	protected IMessageBuilder msgBuilder = null;
	int progrJSTree = 0;
	IEngUserProfile profile = null;
	protected int dTreeRootId = -100;
	protected int dTreeObjects = -1000;
    List functIdsChecked = new ArrayList();
    protected String requestIdentity = null;
    protected String currTheme="";


	/**
	 * Creates the Dtree configuration, in oder to inser into jsp pages cookies,
	 * images, etc.
	 * 
	 * @param objectsList the objects list
	 * @param httpReq the http req
	 * @param initialPath the initial path
	 * @param treename the treename
	 * 
	 * @return the string buffer
	 */
    /*
	protected void makeConfigurationDtree(StringBuffer htmlStream) {
		
		htmlStream.append("<SCRIPT>\n");
		htmlStream.append("		function dTree(objName) {\n");
		htmlStream.append("			this.config = {\n");
		htmlStream.append("				target			: null,\n");
		htmlStream.append("				folderLinks		: true,\n");
		htmlStream.append("				useSelection	: true,\n");
		htmlStream.append("				useCookies		: true,\n");
		htmlStream.append("				useLines		: true,\n");
		htmlStream.append("				useIcons		: true,\n");
		htmlStream.append("				useStatusText	: true,\n");
		htmlStream.append("				closeSameLevel	: false,\n");
		htmlStream.append("				inOrder			: false\n");
		htmlStream.append("			}\n");
		htmlStream.append("			this.icon = {\n");
		htmlStream.append("				root		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treebase.gif")+"',\n");
		htmlStream.append("				folder		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treefolder.gif")+"',\n");
		htmlStream.append("				folderOpen	: '"+urlBuilder.getResourceLink(httpRequest, "/img/treefolderopen.gif")+"',\n");
		htmlStream.append("				node		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treepage.gif")+"',\n");
		htmlStream.append("				empty		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeempty.gif")+"',\n");
		htmlStream.append("				line		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeline.gif")+"',\n");
		htmlStream.append("				join		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treejoin.gif")+"',\n");
		htmlStream.append("				joinBottom	: '"+urlBuilder.getResourceLink(httpRequest, "/img/treejoinbottom.gif")+"',\n");
		htmlStream.append("				plus		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeplus.gif")+"',\n");
		htmlStream.append("				plusBottom	: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeplusbottom.gif")+"',\n");
		htmlStream.append("				minus		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeminus.gif")+"',\n");
		htmlStream.append("				minusBottom	: '"+urlBuilder.getResourceLink(httpRequest, "/img/treeminusbottom.gif")+"',\n");
		htmlStream.append("				nlPlus		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treenolines_plus.gif")+"',\n");
		htmlStream.append("				nlMinus		: '"+urlBuilder.getResourceLink(httpRequest, "/img/treenolines_minus.gif")+"'\n");
		htmlStream.append("			};\n");
		htmlStream.append("			this.obj = objName;\n");
		htmlStream.append("			this.aNodes = [];\n");
		htmlStream.append("			this.aIndent = [];\n");
		htmlStream.append("			this.root = new Node(-1);\n");
		htmlStream.append("			this.selectedNode = null;\n");
		htmlStream.append("			this.selectedFound = false;\n");
		htmlStream.append("			this.completed = false;\n");
		htmlStream.append("		};\n");
		htmlStream.append("</SCRIPT>\n");
		
	}
	*/
    
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpReq, String initialPath, String treename) {
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
		TriggerInfo tInfo = (TriggerInfo)sessionContainer.getAttribute(SpagoBIConstants.TRIGGER_INFO);
		String idObjStr = treename.substring(5);
		String indexStr = idObjStr.substring(idObjStr.lastIndexOf("__"));
		Integer idBiobj = Integer.valueOf(idObjStr.substring(0, idObjStr.lastIndexOf("__")));
		Map saveOpts = tInfo.getSaveOptions();
		DispatchContext sInfo = (DispatchContext)saveOpts.get(idBiobj+indexStr);
		String functIdsConcat = sInfo.getFunctionalityIds();
		String[] functIds =  functIdsConcat.split(",");
		for(int i=0; i<functIds.length; i++) {
			String functIdStr = functIds[i];
			if(functIdStr.trim().equals(""))
				continue;
			Integer functId = Integer.valueOf(functIdStr);
			functIdsChecked.add(functId);
		}
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
        profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
        StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css",currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.objectstree.name" ,"messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js",currTheme )+"'></SCRIPT>");		
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js",currTheme )+"'></SCRIPT>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td id='treeSchedulerFoldersTd" + requestIdentity + "' name='treeSchedulerFoldersTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	   	htmlStream.append("				var nameTree = '"+treename+"';\n");
		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}	   	
		context+="themes/"+currTheme+"/";
	   	htmlStream.append("				"+treename+" = new dTree('"+treename+"', '" + context + "');\n");
	   	htmlStream.append("	        	"+treename+".add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
	   	Iterator it = objectsList.iterator();
	   	while (it.hasNext()) {
	   		LowFunctionality folder = (LowFunctionality) it.next();
	   		if (folder.getParentId() == null) addItemForJSTree(htmlStream, folder, true, false, treename);
   			else addItemForJSTree(htmlStream, folder, false, false, treename);
	   	}
	   	htmlStream.append("				document.getElementById('treeSchedulerFoldersTd" + requestIdentity + "').innerHTML = " + treename + ";\n");
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		return htmlStream;
	}
	
	
	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpReq, String initialPath) {
		return makeTree(objectsList, httpRequest, initialPath, "Tree");
	}
	
	
	
	
	private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, 
			boolean isRoot, boolean isInitialPath, String treename) {
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		String codeType = folder.getCodType();
		Integer idFolder = folder.getId();
		Integer parentId = null;
		if (isRoot) parentId = new Integer (dTreeRootId);
		else parentId = folder.getParentId();

		if (isRoot && !codeType.equalsIgnoreCase(SpagoBIConstants.USER_FUNCTIONALITY_TYPE_CODE)) {
			htmlStream.append("	"+treename+".add(" + idFolder + ", " + dTreeRootId + ",'" + name + "', '', '', '', '', '', 'true');\n");
		} else {
			String checked = "";
			if(functIdsChecked.contains(idFolder)){
				checked = "true";
			}
			String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif",currTheme);
			String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif",currTheme);
			htmlStream.append("	"+treename+".add(" + idFolder + ", " + parentId + ",'" + name + "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + "', '', '', '"+treename+"_funct_id', '"+idFolder+"', '"+checked+"');\n");
		}
	}
	
	
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
		// TODO code for tree with no javascript
		return null;
	}
	
	
}
