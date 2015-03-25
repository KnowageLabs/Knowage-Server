/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.ResponseContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFUserError;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.StringUtils;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;
/**
 * Contains all methods needed to generate and modify a tree object for Functionalities
 * and an object insertion.
 * There are methods to generate tree, configure, insert and modify elements.
 */
public class FunctionalitiesTreeInsertObjectHtmlGenerator implements ITreeHtmlGenerator {
	static private Logger logger = Logger.getLogger(FunctionalitiesTreeInsertObjectHtmlGenerator.class);
	
	HttpServletRequest httpRequest = null;
	RequestContainer reqCont = null;
	private IUrlBuilder urlBuilder = null;
	private IMessageBuilder msgBuilder = null;
	int progrJSTree = 0;
	private IEngUserProfile profile = null;
	private int dTreeRootId = -100;
	private int dMyFolderRootId=-50;
	private boolean privateFolderCreated=false;
	protected String requestIdentity = null;
	private String currTheme="";

	/**
	 * Make tree.
	 * 
	 * @param objectsList the objects list
	 * @param httpRequest the http request
	 * @param initialPath the initial path
	 * @param treename the treename
	 * 
	 * @return the string buffer
	 * 
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeConfigurationDtree(java.lang.StringBuffer)
	 */
	/*
	private void makeConfigurationDtree(StringBuffer htmlStream) {
		
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
		htmlStream.append("				inOrder			: false};\n");
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
	
	
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath, String treename) {
		return makeTree(objectsList, httpRequest, initialPath);
	}
	
	
	
	/**
	 * Function that builds the tree A separate root folder for personal folders.
	 * 
	 * @param objectsList the objects list
	 * @param httpReq the http req
	 * @param initialPath the initial path
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
		
		SourceBean serviceRequest = reqCont.getServiceRequest();
		SessionContainer sessionContainer = reqCont.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
        profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		ResponseContainer responseContainer = ChannelUtilities.getResponseContainer(httpRequest);
		SourceBean serviceResponse = responseContainer.getServiceResponse();
		BIObject obj = (BIObject) serviceResponse.getAttribute("DetailBIObjectModule." + DetailBIObjectModule.NAME_ATTR_OBJECT);
		
		//GET FOLDER_ID (4.0 implementation)
		String functionalityId= (String)httpRequest.getParameter(ObjectsTreeConstants.FUNCT_ID);
		LowFunctionality defaultFunc = null;
		if(functionalityId != null){
			try {
				defaultFunc = DAOFactory.getLowFunctionalityDAO().loadLowFunctionalityByID(Integer.parseInt(functionalityId), false);
			} catch (NumberFormatException e) {
				logger.error("Error in getting folder " + e.getMessage());
			} catch (EMFUserError e) {
				logger.error("Error in getting folder " + e.getMessage());
			}
		}
        
		StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css", currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.functtree.name" ,"messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<div id='divmenuFunctIns' class='dtreemenu' onmouseout='hideMenu(event);' >");
		htmlStream.append("		menu");
		htmlStream.append("</div>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr >");
		htmlStream.append("		<td width='40px'>&nbsp;</td>");
		htmlStream.append("		<td id='treeInsertObjTd" + requestIdentity + "' name='treeInsertObjTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	   	htmlStream.append("				var nameTree = 'treeFunctIns';\n");
		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}
		context+="themes/"+currTheme+"/";
	   	htmlStream.append("				treeFunctIns = new dTree('treeFunctIns', '" + context + "');\n");
	   	htmlStream.append("				treeFunctIns.config.useCookies=false;\n");	   	
	   	htmlStream.append("	        	treeFunctIns.add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
	   	Iterator it = objectsList.iterator();
	   	while (it.hasNext()) {
	   		LowFunctionality folder = (LowFunctionality) it.next();
	   		boolean isDefaultForNew= false;
	   		if(defaultFunc!= null && defaultFunc.getId() == folder.getId()){
	   			isDefaultForNew= true;
	   		}
	   		/* ********* start luca changes *************** */
	   		
	   		
	   		//boolean isUserFunct = folder.getPath().startsWith("/"+profile.getUserUniqueIdentifier());
	   		boolean isUserFunct =(folder.getCodType().equalsIgnoreCase(SpagoBIConstants.USER_FUNCTIONALITY_TYPE_CODE));
	   	/*	if(isUserFunct) {
	   			continue;
	   		}*/
	   		/* ********* end luca changes ***************** */
	   		if (initialPath != null) {
	   			if (initialPath.equalsIgnoreCase(folder.getPath())) addItemForJSTree(htmlStream, folder, obj, false, true, isDefaultForNew);
	   			else addItemForJSTree(htmlStream, folder, obj, false, false, isDefaultForNew);
	   		} else {
	   			if (folder.getParentId() == null) addItemForJSTree(htmlStream, folder, obj, true, false, isDefaultForNew);
	   			else addItemForJSTree(htmlStream, folder, obj, false, false, isDefaultForNew);
	   		}
	   	}
	   	//htmlStream.append("				document.write(treeFunctIns);\n");
	   	htmlStream.append("				document.getElementById('treeInsertObjTd" + requestIdentity + "').innerHTML = treeFunctIns;\n");
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		logger.debug("OUT");
		return htmlStream;
		
	}

	private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, BIObject obj, 
			boolean isRoot, boolean isInitialPath, boolean isDefaultForNew) {
		logger.debug("IN");	
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		name = StringUtils.escapeForHtml(name);
		String codeType = folder.getCodType();
		Integer id = folder.getId();
		Integer parentId = null;
		if (isInitialPath) parentId = new Integer (dTreeRootId);
		else parentId = folder.getParentId();

		if(codeType.equalsIgnoreCase(SpagoBIConstants.LOW_FUNCTIONALITY_TYPE_CODE)){
		if (isRoot) {
			htmlStream.append("	treeFunctIns.add(" + id + ", " + dTreeRootId + ",'" + name + "', '', '', '', '', '', 'true');\n");
		} else {
			if(codeType.equalsIgnoreCase(SpagoBIConstants.LOW_FUNCTIONALITY_TYPE_CODE)) {
				String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif", currTheme);
				String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif", currTheme);
				try{
					if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)|| ObjectsAccessVerifier.canDev(id, profile)){
						boolean checked = false;
						if (obj != null) {
							List funcs = obj.getFunctionalities();
							if (funcs.contains(id) || isDefaultForNew) {
								checked = true;
							}
						}
						htmlStream.append("	treeFunctIns.add(" + id + ", " + parentId + ",'" + name + 
							          "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + 
							          "', '', '', '" + ObjectsTreeConstants.FUNCT_ID + "', '" + id + "'," + checked + ");\n");
					} else if (ObjectsAccessVerifier.canExec(id, profile)) {
						htmlStream.append("	treeFunctIns.add(" + id + ", " + parentId + ",'" + name + 
						          "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + 
						          "', '', '', '', '',false);\n");
					}
				}catch (Exception ex){
					logger.error("Error in adding items " + ex.getMessage());
				}
			} 
		}
		}
		if(codeType.equalsIgnoreCase(SpagoBIConstants.USER_FUNCTIONALITY_TYPE_CODE)){
			if(!privateFolderCreated)	{
				privateFolderCreated=true;
				htmlStream.append("	treeFunctIns.add(" + dMyFolderRootId + ", " + dTreeRootId + ",'" + "Personal Folders" + "', '', '', '', '', '', false);\n");
							}
					String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderuser.gif", currTheme);
					String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopenuser.gif", currTheme);
					try{
						if (profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_ADMIN)|| ObjectsAccessVerifier.canDev(id, profile)){
							boolean checked = false;
							if (obj != null) {
								List funcs = obj.getFunctionalities();
								if (funcs.contains(id)) checked = true;
							}
							htmlStream.append("	treeFunctIns.add(" + id + ", " + dMyFolderRootId  + ",'" + name + 
								          "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + 
								          "', '', '', '" + ObjectsTreeConstants.FUNCT_ID + "', '" + id + "'," + checked + ");\n");
						} else if (ObjectsAccessVerifier.canExec(id, profile)) {
							htmlStream.append("	treeFunctIns.add(" + id + ", " + parentId + ",'" + name + 
							          "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + 
							          "', '', '', '', '',false);\n");
						}
					}catch (Exception ex){
						logger.error("Error in adding items " + ex.getMessage());
					}
				} 
			
		logger.debug("OUT");	
		}
		
		
		
		
		
	

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
