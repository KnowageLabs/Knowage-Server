/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.base.SourceBean;
import it.eng.spago.error.EMFInternalError;
import it.eng.spago.navigation.LightNavigationManager;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.commons.bo.UserProfile;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.GeneralUtilities;
import it.eng.spagobi.commons.utilities.ObjectsAccessVerifier;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.themes.ThemesManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Contains all methods needed to generate and modify a tree object for Execution.
 * There are methods to generate tree, configure, insert and modify elements.
 */
public class ExecTreeHtmlGenerator implements ITreeHtmlGenerator {
	
	HttpServletRequest httpRequest = null;
	RequestContainer reqCont = null;
	protected IUrlBuilder urlBuilder = null;
	protected IMessageBuilder msgBuilder = null;
	IEngUserProfile profile = null;
	int progrJSTree = 0;
	private SessionContainer sessionContainer = null;
	private boolean thereIsOneOrMoreObjectsInTestState = false;
	protected SourceBean _serviceRequest = null;
	private int dTreeRootId = -100;
	private int dTreeObjects = -1000;
	// the name of the dtree variable, default value is treeExecObj
	private String treeName = "treeExecObj";
	protected String requestIdentity = null;
	private String currTheme="";

	static private Logger logger = Logger.getLogger(ExecTreeHtmlGenerator.class);
	
	/**
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeJSFunctionForMenu(java.lang.StringBuffer)
	 */
	private void makeJSFunctionForMenu(StringBuffer htmlStream) {
		/* ********* start luca changes *************** */
		htmlStream.append("		function menu" + requestIdentity + "(prog, event, urlExecution, urlMetadata, urlEraseDoc, urlEraseFolder, urlAddFolder,urlDown, urlUp) {\n");
		htmlStream.append("			divM = document.getElementById('divmenu" + requestIdentity + "');\n");
		htmlStream.append("			divM.innerHTML = '';\n");
		String capExec = msgBuilder.getMessage("SBISet.devObjects.captionExecute", "messages", httpRequest);
		htmlStream.append("			if(urlExecution!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlExecution+'\">"+capExec+"</a></div>';\n");
		/*String capMetadata = msgBuilder.getMessage("SBISet.objects.captionMetadata", "messages", httpRequest);
		htmlStream.append("			if(urlMetadata!=''){ divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:makePopup(\\''+prog+'\\',\\''+urlMetadata+'\\')\" >"+capMetadata+"</a></div>'; }\n");
		*/
		String capErase = msgBuilder.getMessage("SBISet.devObjects.captionErase", "messages", httpRequest);
        htmlStream.append("         if(urlEraseDoc!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capErase+"\\', \\''+urlEraseDoc+'\\');\">"+capErase+"</a></div>';\n");
        htmlStream.append("         if(urlEraseFolder!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capErase+"\\', \\''+urlEraseFolder+'\\');\">"+capErase+"</a></div>';\n");
        String capMoveDown = msgBuilder.getMessage("SBISet.objects.captionMoveDownShort", "messages", httpRequest);
		htmlStream.append("         if(urlDown!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capMoveDown+"\\', \\''+urlDown+'\\');\">"+capMoveDown+"</a></div>';\n");
		String capMoveUp = msgBuilder.getMessage("SBISet.objects.captionMoveUpShort", "messages", httpRequest);
		htmlStream.append("         if(urlUp!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capMoveUp+"\\', \\''+urlUp+'\\');\">"+capMoveUp+"</a></div>';\n");
	
        
        htmlStream.append("				showMenu(event, divM);\n");
        String capAddSub = "Add folder";
        htmlStream.append("			if(urlAddFolder!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlAddFolder+'\">"+capAddSub+"</a></div>';\n");
        
        
		htmlStream.append("		}\n");
		
		htmlStream.append("		function linkEmpty() {\n");
		htmlStream.append("		}\n");
		
        String confirmCaption = msgBuilder.getMessage("SBISet.devObjects.confirmCaption", "messages", httpRequest);
        htmlStream.append("     function actionConfirm(message, url){\n");
        htmlStream.append("         if (confirm('" + confirmCaption + " ' + message + '?')){\n");
        htmlStream.append("             location.href = url;\n");
        htmlStream.append("         }\n");
        htmlStream.append("     }\n");
        
        htmlStream.append("     function addSubFolder(idFP){\n");
        htmlStream.append("         alert('Not Implemented');");
        htmlStream.append("     }\n");
        
        htmlStream.append("     function eraseFolder(idF){\n");
        htmlStream.append("         alert('Not Implemented');");
        htmlStream.append("     }\n");
        /*
    	htmlStream.append("function makePopup(id, urlMetadata ) {\n");		
		htmlStream.append(" var win = new Ext.Window({id:id , \n"
					+"            bodyCfg:{ \n" 
					+"                tag:'div' \n"
					+"                ,cls:'x-panel-body' \n"
					+"               ,children:[{ \n"
					+"                    tag:'iframe', \n"
					+"                    name: 'dynamicIframe1', \n"
					+"                    id  : 'dynamicIframe1', \n"
					+"                    src: urlMetadata , \n"
					+"                    frameBorder:0, \n"
					+"                    width:'100%', \n"
					+"                    height:'100%', \n"
					+"                    style: {overflow:'auto'}  \n "        
					+"               }] \n"
					+"            }, \n"
					+"            modal: true,\n"
					+"            layout:'fit',\n"
					+"            height:400,\n"
			+"            width:500,\n"
			+"            closeAction:'close',\n"
			+"            scripts: true, \n"
			+"            plain: true \n"
														        
			+"        });  \n"
			+"   win.show(); \n" );
						
		htmlStream.append("}\n");*/
        
        /* ********* end luca changes *************** */
	}
		
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
	 */
	public StringBuffer makeTree(List objectsList, HttpServletRequest httpRequest, String initialPath, String treename) {
		this.treeName = treename;
		return makeTree(objectsList, httpRequest, initialPath);
	}
	
	/**
	 * Function that builds the tree: It should build more common folders and one personal folder (user's one).
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
	    // get spago containers and buildres classes
		httpRequest = httpReq;
		reqCont = ChannelUtilities.getRequestContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		_serviceRequest = reqCont.getServiceRequest();
		sessionContainer = reqCont.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();

    	currTheme=ThemesManager.getCurrentTheme(reqCont);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		// get user profile
        profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
		StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css", currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.objectstree.name" ,"messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js", currTheme )+"'></SCRIPT>");
		/* ********* start luca changes *************** */
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<div id='divmenu" + requestIdentity + "' class='dtreemenu' onmouseout='hideMenu(event, \"divmenu" + requestIdentity + "\");' >");
		htmlStream.append("		menu");
		htmlStream.append("</div>");
		/* ********* end luca changes *************** */
		htmlStream.append("<div id='viewOnlyTestDocument" + requestIdentity + "' style='display:none;'>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td>");
		String checked = "";
		String onlyTestObjectsView = httpRequest.getParameter("view_only_test_objects");
		String onlyTestObjectsViewLbl = msgBuilder.getMessage("tree.objectstree.showOnlyTestObject", "messages", httpRequest);
		if ("true".equalsIgnoreCase(onlyTestObjectsView)) checked = "checked='checked'";
		htmlStream.append("			<span class=\"dtree\">" + onlyTestObjectsViewLbl + "</span>\n");
		htmlStream.append("			<input type=\"checkbox\" " + checked + " \n");
		htmlStream.append("				onclick=\"document.getElementById('view_only_test_objects" + requestIdentity + "').checked=this.checked;document.getElementById('objectForm').submit()\" />\n");
		htmlStream.append("		</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		htmlStream.append("</div>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td id='treeExecObjTd" + requestIdentity + "' name='treeExecObjTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	   	//htmlStream.append("				var nameTree = 'treeExecObj';\n");

		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}
		context+="themes/"+currTheme+"/";
		
		htmlStream.append("				" + treeName + " = new dTree('" + treeName + "', '" + context + "');\n");
	   	htmlStream.append("	        	" + treeName + ".add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
	   	Iterator it = objectsList.iterator();
	   	while (it.hasNext()) {
	   		LowFunctionality folder = (LowFunctionality) it.next();
	   		boolean isRoot = false;
	   		
	   		//only user personal folder
	   		boolean isUserFunct = folder.getPath().startsWith("/"+((UserProfile)profile).getUserId());
	   		if(!isUserFunct) {
	   			if (initialPath != null) {
		   			if (initialPath.equalsIgnoreCase(folder.getPath())) 
		   				isRoot = true;
		   		} else {
		   			if (folder.getParentId() == null) 
		   				isRoot = true;
		   		}
	   		}
	   		try {
				addItemForJSTree(htmlStream, folder, isRoot, isUserFunct);
			} catch (EMFInternalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	   	}
    	htmlStream.append("				document.getElementById('treeExecObjTd" + requestIdentity + "').innerHTML = " + treeName + ";\n");
    	makeJSFunctionForMenu(htmlStream);	
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		htmlStream.append("<br/>");
		
		// if there is one or more document in test state diplay the div with id='viewOnlyTestDocument'
		if (thereIsOneOrMoreObjectsInTestState) {
    		
			Map formUrlPars = new HashMap();
			formUrlPars.put("PAGE", "LOGIN_PAGE_SBI_FUNCTIONALITY");
			formUrlPars.put(SpagoBIConstants.OBJECTS_VIEW, SpagoBIConstants.VIEW_OBJECTS_AS_TREE);
			if(ChannelUtilities.isWebRunning()) {
				formUrlPars.put(SpagoBIConstants.WEBMODE, "TRUE");
			}
			String formUrl = urlBuilder.getUrl(httpRequest, formUrlPars);
		
    		String updateTree = msgBuilder.getMessage("tree.objectstree.update", "messages", httpRequest);
    		htmlStream.append("	<div style=\"display:none;\">\n");
    		htmlStream.append("	<form method='POST' action='" + formUrl + "' id ='objectForm' name='objectForm'>\n");
    		htmlStream.append("		<span>" + onlyTestObjectsViewLbl + "</span>\n");
    		htmlStream.append("		<input type=\"checkbox\" name=\"view_only_test_objects\" id=\"view_only_test_objects" + requestIdentity + "\" value=\"true\" " + checked + " />\n");
    		htmlStream.append("		<input type=\"image\" style=\"width:25px;height:25px\" title=\"" + updateTree + "\" alt\"" + updateTree + "\" \n");
    		htmlStream.append("			src=\"" + urlBuilder.getResourceLinkByTheme(httpRequest, "/img/updateState.png", currTheme ) + "\" />\n");
    		htmlStream.append("	</form>\n");
    		htmlStream.append("	</div>\n");
			htmlStream.append("<script type='text/javascript'>\n");
			htmlStream.append("	document.getElementById('viewOnlyTestDocument" + requestIdentity + "').style.display='inline';\n");
			htmlStream.append("</script>\n");
		}
		
		logger.debug("Generated HTML for tree: \n" + htmlStream.toString());
		
		logger.debug("OUT");
		return htmlStream;
	}

	private void addRootItemForJSTree(StringBuffer htmlStream, LowFunctionality folder) throws EMFInternalError {
		logger.info("IN");
		
		//String exec = msgBuilder.getMessage("SBISet.objects.captionExecute", "messages", httpRequest);
		//String metadata = msgBuilder.getMessage("SBISet.objects.captionMetadata", "messages", httpRequest);
		
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		Integer idFolder = folder.getId();		
		String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif", currTheme);
		String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif", currTheme);
		boolean canExec = ObjectsAccessVerifier.canExec(idFolder, profile);
		boolean canTest = ObjectsAccessVerifier.canTest(idFolder, profile);
		
		logger.info("Adding ROOT node [" + nameLabel + "] ...");
		logger.debug("Node [" + nameLabel + "] id property is set to: " + idFolder);
		logger.debug("Node [" + nameLabel + "] label property is set to: " + name);
		logger.debug("Node [" + nameLabel + "] executable property is set to: " + canExec);
		logger.debug("Node [" + nameLabel + "] testable property is set to: " + canTest);
		
		
		htmlStream.append(treeName + ".add(" + idFolder + ", " + dTreeRootId + ",'" + name + "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + "', 'true');\n");
		
		List objects = folder.getBiObjects();
		logger.debug("Node [" + nameLabel + "] contains [" + objects.size() + "] document(s)");
		logger.info("Adding contained dcuments ...");
		for (Iterator it = objects.iterator(); it.hasNext(); ) {			
			BIObject obj = (BIObject) it.next();
			Integer idObj = obj.getId();
			String stateObj = obj.getStateCode();
			Integer visibleObj = obj.getVisible();
			
			logger.info("Adding document node [" + obj.getName() + "] ...");
			logger.debug("Node [" + obj.getName() + "] id property is set to: " + idObj);	
			logger.debug("Node [" + obj.getName() + "] state property is set to: " + stateObj);	
			logger.debug("Node [" + obj.getName() + "] visible property is set to: " + (visibleObj.intValue() != 0));
			
			//insert the correct image for each BI Object type
			String biObjType = obj.getBiObjectTypeCode();
			String imgUrl = "/img/objecticon_"+ biObjType+ ".png";
			String userIcon = urlBuilder.getResourceLinkByTheme(httpRequest, imgUrl, currTheme);
			String biObjState = obj.getStateCode();
			String stateImgUrl = "/img/stateicon_"+ biObjState+ ".png";
			String stateIcon = urlBuilder.getResourceLinkByTheme(httpRequest, stateImgUrl, currTheme);
			String onlyTestObjectsView = (String)_serviceRequest.getAttribute("view_only_test_objects");
			
			Map execUrlPars = new HashMap();
			//execUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
			execUrlPars.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);			
			execUrlPars.put(ObjectsTreeConstants.OBJECT_ID, idObj.toString());
			execUrlPars.put(SpagoBIConstants.MESSAGEDET, ObjectsTreeConstants.EXEC_PHASE_CREATE_PAGE);
			execUrlPars.put(ObjectsTreeConstants.BIOBJECT_TREE_LIST, ObjectsTreeConstants.BIOBJECT_TREE_LIST);			
			
			
			
			
			if (visibleObj != null && visibleObj.intValue() == 0 && (stateObj.equalsIgnoreCase("REL") || stateObj.equalsIgnoreCase("TEST"))) {
				logger.debug("Node [" + obj.getName() + "] is not accessible by user [" + profile.getUserUniqueIdentifier() + "]");
			} else {
				logger.debug("Node [" + obj.getName() + "] is accessible by user [" + profile.getUserUniqueIdentifier() + "]");
				
				if (canTest && (stateObj.equals("TEST"))) {
					logger.debug("Node [" + obj.getName() + "] will be accessed by user [" + profile.getUserUniqueIdentifier() + "] in TEST mode");
					thereIsOneOrMoreObjectsInTestState = true;
					String execUrl = urlBuilder.getUrl(httpRequest, execUrlPars);
					htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + obj.getName() + "', '" + execUrl + "', '', '', '" + userIcon + "', '', '', '' );\n");
				} else if(!"true".equalsIgnoreCase(onlyTestObjectsView) && (stateObj.equals("REL")) && canExec) {
					logger.debug("Node [" + obj.getName() + "] will be accessed by user [" + profile.getUserUniqueIdentifier() + "] in EXEC mode");
					
					boolean profileAttrsOk = ObjectsAccessVerifier.checkProfileVisibility(obj, profile);
					if (profileAttrsOk) {
						String execUrl = urlBuilder.getUrl(httpRequest, execUrlPars);
						//String localizedName=msgBuilder.getUserMessage(obj.getName(), SpagoBIConstants.DEFAULT_USER_BUNDLE, httpRequest);
						String localizedName=msgBuilder.getI18nMessage(obj.getName(), httpRequest);
						htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + localizedName + "', '" + execUrl + "', '', '', '" + userIcon + "', '', '', '' );\n");
					} else {
						logger.debug("NOT visible " + obj.getName() + " because user profile attribute constraint are not satisfied");
					}
				}
			}
			
			logger.info("... document node [" + obj.getName() + "] added succesfully");
		}
		
		logger.info("... contained dcuments added succesfully");
		
		
		logger.info("OUT");
	}
	
	private void addFolderItemForJSTree(StringBuffer htmlStream, LowFunctionality folder) throws EMFInternalError {
		logger.info("IN");
		
		String exec = msgBuilder.getMessage("SBISet.objects.captionExecute", "messages", httpRequest);
		String metadata = msgBuilder.getMessage("SBISet.objects.captionMetadata", "messages", httpRequest);
		
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		Integer idFolder = folder.getId();
		Integer parentId = folder.getParentId();
		
		String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif", currTheme);
		String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif", currTheme);
		boolean canExec = ObjectsAccessVerifier.canExec(idFolder, profile);
		boolean canTest = ObjectsAccessVerifier.canTest(idFolder, profile);
		
		logger.info("Adding FOLDER node [" + nameLabel + "] ...");
		logger.debug("Node [" + nameLabel + "] id property is set to: " + idFolder);
		logger.debug("Node [" + nameLabel + "] label property is set to: " + name);
		logger.debug("Node [" + nameLabel + "] executable property is set to: " + canExec);
		logger.debug("Node [" + nameLabel + "] testable property is set to: " + canTest);
		
		if (canTest || canExec) {
			logger.debug("Node [" + nameLabel + "] is accessible by user [" + profile.getUserUniqueIdentifier() + "]");
			
			htmlStream.append("	" + treeName + ".add(" + idFolder + ", " + parentId + ",'" + name + "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + "', '', '');\n");
			List objects = folder.getBiObjects();
			logger.debug("Node [" + nameLabel + "] contains [" + objects.size() + "] document(s)");
			logger.info("Adding contained dcuments ...");
			for (Iterator it = objects.iterator(); it.hasNext(); ) {
				BIObject obj = (BIObject) it.next();
				Integer idObj = obj.getId();
				String stateObj = obj.getStateCode();
				Integer visibleObj = obj.getVisible();
				//insert the correct image for each BI Object type
				String biObjType = obj.getBiObjectTypeCode();
				String imgUrl = "/img/objecticon_"+ biObjType+ ".png";
				String userIcon = urlBuilder.getResourceLinkByTheme(httpRequest, imgUrl, currTheme);
				String biObjState = obj.getStateCode();
				String stateImgUrl = "/img/stateicon_"+ biObjState+ ".png";
				String stateIcon = urlBuilder.getResourceLinkByTheme(httpRequest, stateImgUrl, currTheme);
				String onlyTestObjectsView = (String)_serviceRequest.getAttribute("view_only_test_objects");
				
				logger.info("Adding document node [" + obj.getName() + "] ...");
				logger.debug("Node [" + obj.getName() + "] id property is set to: " + idObj);	
				logger.debug("Node [" + obj.getName() + "] state property is set to: " + stateObj);	
				logger.debug("Node [" + obj.getName() + "] visible property is set to: " + (visibleObj.intValue() != 0));
				
				if (visibleObj != null && visibleObj.intValue() == 0 && (stateObj.equalsIgnoreCase("REL") || stateObj.equalsIgnoreCase("TEST"))) {
					logger.debug("Node [" + obj.getName() + "] is not accessible by user [" + profile.getUserUniqueIdentifier() + "]");
				} else {
					logger.debug("Node [" + obj.getName() + "] is accessible by user [" + profile.getUserUniqueIdentifier() + "]");
					String prog = idObj.toString();
					if ((stateObj.equals("TEST")) && canTest && profile.isAbleToExecuteAction(SpagoBIConstants.DOCUMENT_MANAGEMENT_TEST)) {
						thereIsOneOrMoreObjectsInTestState = true;

						htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",' <a title=\\'" +exec+"\\' href=\""+createExecuteObjectLink(idObj)+"\">" + obj.getName() +"</a>', '', '', '', '" + userIcon + "', '','', 'menu" + requestIdentity + "("+prog+", event, \\'\\',\\'\\', \\'\\', \\'\\', \\'\\',\\'\\',\\'\\')' );\n");
						//htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + obj.getName() + "', 'javascript:linkEmpty()', '', '', '" + userIcon + "', '', '', 'menu" + requestIdentity + "("+prog+", event,\\'" + createExecuteObjectLink(idObj) + "\\',\\'" + createMetadataObjectLink(idObj) + "\\', \\'\\', \\'\\', \\'\\',\\'" +createMoveDownObjectLink(idObj) + "\\', \\'" +createMoveUpObjectLink(idObj) + "\\')' );\n");
					} else if(!"true".equalsIgnoreCase(onlyTestObjectsView) && (stateObj.equals("REL"))&& canExec) {
							
						//Nuovo albero con Icona dei metadati
						boolean profileAttrsOk = ObjectsAccessVerifier.checkProfileVisibility(obj, profile);
						if (profileAttrsOk) {
							htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",' <a title=\\'" +exec+"\\' href=\""+createExecuteObjectLink(idObj)+"\">" + obj.getName() +"</a>', '', '', '', '" + userIcon + "', '','', 'menu" + requestIdentity + "("+prog+", event, \\'\\',\\'\\', \\'\\', \\'\\', \\'\\',\\'\\',\\'\\')' );\n");
						} else {
							logger.debug("NOT visible " + obj.getName() + " because user profile attribute constraint are not satisfied");
						}
						
					}
				}
				logger.info("... document node [" + obj.getName() + "] added succesfully");
			}
			
			logger.info("... contained dcuments added succesfully");
		}
		logger.info("OUT");
	}
	
	
	private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, boolean isRoot, boolean isUserFunct) throws EMFInternalError {
		logger.info("IN");
		String exec = msgBuilder.getMessage("SBISet.objects.captionExecute", "messages", httpRequest);
		String metadata = msgBuilder.getMessage("SBISet.objects.captionMetadata", "messages", httpRequest);
		
		String nameLabel = folder.getName();
		String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		Integer idFolder = folder.getId();
		Integer parentId = folder.getParentId();
		
		String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif", currTheme);
		String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif", currTheme);
		boolean canExec = ObjectsAccessVerifier.canExec(idFolder, profile);
		boolean canTest = ObjectsAccessVerifier.canTest(idFolder, profile);
			
				
		if (isRoot) {
			addRootItemForJSTree(htmlStream, folder);
		} else if (isUserFunct) {
			logger.debug("User Personal Folder");
			imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderuser.gif", currTheme);
			imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopenuser.gif", currTheme);
			
			htmlStream.append(treeName + ".add(" + idFolder + ", " + dTreeRootId + ",'" + "Personal Folder: "+name + "', 'javascript:linkEmpty()', '', '', '" + imgFolder + "', '" + imgFolderOp + "', 'false', 'menu" + requestIdentity + "( \\'\\', event, \\'\\', \\'\\', \\'javascript:eraseFolder("+idFolder+")\\', \\'javascript:addSubFolder("+idFolder+")\\')');\n");
		
			List objects = folder.getBiObjects();
			for (Iterator it = objects.iterator(); it.hasNext(); ) {
				BIObject obj = (BIObject) it.next();
				Integer idObj = obj.getId();
				String stateObj = obj.getStateCode();
				Integer visibleObj = obj.getVisible();
				if( !stateObj.equalsIgnoreCase("REL") || visibleObj.intValue() != 1) {
					continue;
				}
				//insert the correct image for each BI Object type
				String biObjType = obj.getBiObjectTypeCode();
				String imgUrl = "/img/objecticon_"+ biObjType+ ".png";
				String userIcon = urlBuilder.getResourceLinkByTheme(httpRequest, imgUrl, currTheme);
				String biObjState = obj.getStateCode();
				String stateImgUrl = "/img/stateicon_"+ biObjState+ ".png";
				String stateIcon = urlBuilder.getResourceLinkByTheme(httpRequest, stateImgUrl, currTheme);
				// create execution link
				Map execUrlPars = new HashMap();
				//execUrlPars.put("PAGE", ExecuteBIObjectModule.MODULE_PAGE);
				execUrlPars.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);			
				execUrlPars.put(ObjectsTreeConstants.OBJECT_ID, idObj.toString());
				execUrlPars.put(SpagoBIConstants.MESSAGEDET, ObjectsTreeConstants.EXEC_PHASE_CREATE_PAGE);
				execUrlPars.put(ObjectsTreeConstants.BIOBJECT_TREE_LIST, ObjectsTreeConstants.BIOBJECT_TREE_LIST);			
				
				String execUrl = urlBuilder.getUrl(httpRequest, execUrlPars);
				String prog = idObj.toString();
				htmlStream.append(treeName + ".add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + obj.getName() + "', 'javascript:linkEmpty()', '', '', '" + userIcon + "', '', '', 'menu" + requestIdentity + "("+prog+", event, \\'"+execUrl+"\\',\\'" + createMetadataObjectLink(idObj) + "\\', \\'"+createEraseDocumentLink(idObj,idFolder)+"\\', \\'\\', \\'\\',\\'\\', \\'\\')' );\n");
			}
		} else {
			addFolderItemForJSTree(htmlStream, folder);
		}
		logger.info("OUT");
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private String createExecuteObjectLink(Integer id) {
		HashMap execUrlParMap = new HashMap();
		//execUrlParMap.put(ObjectsTreeConstants.PAGE, ExecuteBIObjectModule.MODULE_PAGE);
		execUrlParMap.put(ObjectsTreeConstants.ACTION, SpagoBIConstants.EXECUTE_DOCUMENT_ACTION);			
		execUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		execUrlParMap.put(ObjectsTreeConstants.BIOBJECT_TREE_LIST, ObjectsTreeConstants.BIOBJECT_TREE_LIST);		
		execUrlParMap.put(SpagoBIConstants.MESSAGEDET, ObjectsTreeConstants.EXEC_PHASE_CREATE_PAGE);
		String execUrl = urlBuilder.getUrl(httpRequest, execUrlParMap);
		return execUrl;
	}
	
	private String createMetadataObjectLink(Integer id) {
		String detUrl = GeneralUtilities.getSpagoBIProfileBaseUrl(((UserProfile)profile).getUserId().toString());
		HashMap detUrlParMap = new HashMap();
		//detUrlParMap.put(ObjectsTreeConstants.PAGE, MetadataBIObjectModule.MODULE_PAGE);
		detUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.METADATA_SELECT);
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		detUrlParMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "TRUE");
		if (detUrlParMap != null){
			Iterator keysIt = detUrlParMap.keySet().iterator();
			String paramName = null;
			Object paramValue = null;
			while (keysIt.hasNext()){
				paramName = (String)keysIt.next();
				paramValue = detUrlParMap.get(paramName); 
				detUrl += "&"+paramName+"="+paramValue.toString();
			}
		}
		return detUrl;
	}
	
	private String createMoveUpObjectLink(Integer id) {
		HashMap detUrlParMap = new HashMap();
		detUrlParMap.put(ObjectsTreeConstants.PAGE,"UpdateBIObjectStatePage" );
		detUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.MOVE_STATE_UP);
		detUrlParMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		String detUrl = urlBuilder.getUrl(httpRequest, detUrlParMap);
		return detUrl;
	}
	
	private String createMoveDownObjectLink(Integer id) {
		HashMap detUrlParMap = new HashMap();
		detUrlParMap.put(ObjectsTreeConstants.PAGE, "UpdateBIObjectStatePage" );
		detUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.MOVE_STATE_DOWN);
		detUrlParMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		String detUrl = urlBuilder.getUrl(httpRequest, detUrlParMap);
		return detUrl;
	}
	
	
	private String createEraseDocumentLink(Integer iddoc,Integer idFunct) {
		HashMap execUrlParMap = new HashMap();
		execUrlParMap.put(ObjectsTreeConstants.PAGE, "MYFOLDERMANAGEMENTPAGE");
		execUrlParMap.put(LightNavigationManager.LIGHT_NAVIGATOR_DISABLED, "true");
		execUrlParMap.put("TASK", "ERASE_DOCUMENT");
		execUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, iddoc.toString());
		execUrlParMap.put(ObjectsTreeConstants.FUNCT_ID, idFunct.toString());
		String execUrl = urlBuilder.getUrl(httpRequest, execUrlParMap);
		return execUrl;
	}
	
}
