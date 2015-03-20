/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.wapp.presentation;

/**

	SpagoBI - The Business Intelligence Free Platform

	Copyright (C) 2004 - 2011 Engineering Ingegneria Informatica S.p.A.

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License as published by the Free Software Foundation; either
	version 2.1 of the License, or (at your option) any later version.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
	Lesser General Public License for more details.

	You should have received a copy of the GNU Lesser General Public
	License along with this library; if not, write to the Free Software
	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 **/


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.MoveDownLowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.MoveUpLowFunctionality;
import it.eng.spagobi.commons.bo.Role;
import it.eng.spagobi.commons.constants.AdmintoolsConstants;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.constants.SpagoBIConstants;
import it.eng.spagobi.commons.dao.DAOFactory;
import it.eng.spagobi.commons.utilities.ChannelUtilities;
import it.eng.spagobi.commons.utilities.messages.IMessageBuilder;
import it.eng.spagobi.commons.utilities.messages.MessageBuilderFactory;
import it.eng.spagobi.commons.utilities.urls.IUrlBuilder;
import it.eng.spagobi.commons.utilities.urls.UrlBuilderFactory;
import it.eng.spagobi.utilities.assertion.Assert;
import it.eng.spagobi.utilities.themes.ThemesManager;
import it.eng.spagobi.wapp.bo.Menu;
import it.eng.spagobi.wapp.services.CreateMasterMenuAction;
import it.eng.spagobi.wapp.services.DetailMenuModule;
import it.eng.spagobi.wapp.services.MoveDownMenuAction;
import it.eng.spagobi.wapp.services.MoveUpMenuAction;



/**
 * Contains all methods needed to generate and modify a tree object for menus.
 * There are methods to generate tree, configure, insert and modify elements.
 */
public class MenuConfigurationHTMLTreeGenerator implements ITreeHtmlGenerator {

	HttpServletRequest httpRequest = null;
	RequestContainer reqCont = null;
	int progrJSTree = 0;
	private int dTreeRootId = -100;
	private IUrlBuilder urlBuilder = null;
	private IMessageBuilder msgBuilder = null;
	private List _objectsList = null;
	protected String requestIdentity = null;
	protected String currTheme="";
	
	/** Logger component. */
    public static transient Logger logger = Logger.getLogger(MenuConfigurationHTMLTreeGenerator.class);

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
		UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
		UUID uuid = uuidGen.generateTimeBasedUUID();
		requestIdentity = uuid.toString();
		requestIdentity = requestIdentity.replaceAll("-", "");
		_objectsList = objectsList;
		httpRequest = httpReq;
		reqCont = ChannelUtilities.getRequestContainer(httpRequest);
		
    	currTheme=ThemesManager.getCurrentTheme(reqCont);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
		
		StringBuffer htmlStream = new StringBuffer();
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css",currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.menutree.name" ,"messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js",currTheme )+"'></SCRIPT>");
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js",currTheme )+"'></SCRIPT>");
		htmlStream.append("<div id='divmenuFunct" + requestIdentity + "' class='dtreemenu' onmouseout='hideMenu(event, \"divmenuFunct" + requestIdentity + "\");' >");
		htmlStream.append("		menu");
		htmlStream.append("</div>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td id='treeFoldersTd" + requestIdentity + "' name='treeFoldersTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
		htmlStream.append("				var nameTree = 'treeFunct';\n");
		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}		
		context+="themes/"+currTheme+"/";
		
		htmlStream.append("				treeFunct = new dTree('treeFunct', '" + context + "');\n");
		//htmlStream.append("	        	treeFunct.add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
		htmlStream.append("	treeFunct.add("+dTreeRootId + ",-1,'"+nameTree+"', 'javascript:linkEmpty()', '', '', '', '', 'true', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(null)+"\\', \\'\\', \\'\\', \\'\\', \\'\\', \\'\\')');\n");


		// Calculate which are the leaves at the 3rd level
		//List limitLeaves=fillDepths(objectsList);

		Iterator it = objectsList.iterator();
		while (it.hasNext()) {
			Menu menu= (Menu) it.next();
			/* ********* start luca changes *************** */
			RequestContainer reqCont = ChannelUtilities.getRequestContainer(httpRequest);
			SessionContainer sessionContainer = reqCont.getSessionContainer();
			SessionContainer permanentSession = sessionContainer.getPermanentContainer();
			IEngUserProfile profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			/* ********* end luca changes ***************** */
			if (initialPath != null) {
				if (initialPath.equalsIgnoreCase(menu.getName())) addItemForJSTree(htmlStream, menu, true);
				else addItemForJSTree(htmlStream, menu, false);
			} else {
				if (menu.getParentId() == null || menu.getParentId().intValue()==0) addItemForJSTree(htmlStream, menu, true);
				else addItemForJSTree(htmlStream, menu, false);
			}
		}
		//htmlStream.append("				document.write(treeFunct);\n");
		htmlStream.append("				document.getElementById('treeFoldersTd" + requestIdentity + "').innerHTML = treeFunct;\n");
		makeJSFunctionForMenu(htmlStream);	
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		return htmlStream;
	}

	private void addItemForJSTree(StringBuffer htmlStream, Menu menu, boolean isRoot) {

		String nameLabel = menu.getName();

		//String name = msgBuilder.getUserMessage(nameLabel, SpagoBIConstants.DEFAULT_USER_BUNDLE, httpRequest);
		String name = msgBuilder.getI18nMessage(nameLabel, httpRequest);
		
		name = StringEscapeUtils.escapeJavaScript(name);
		//String name = msgBuilder.getMessage(nameLabel, "messages", httpRequest);
		Integer id = menu.getMenuId();
		Integer parentId = menu.getParentId();
		Integer level=menu.getLevel();
		boolean hasChildren=menu.getHasChildren();
		//if(parentsList.contains(id))hasChildren=true;

		// set image icons
		//String imgFolder = urlBuilder.getResourceLink(httpRequest, "/img/treefolder.gif");
		//String imgFolderOp = urlBuilder.getResourceLink(httpRequest, "/img/treefolderopen.gif");

		String imgFolder=""; 
		String imgFolderOp="";
		
		if(menu.getObjId()!=null){
			//String icon=DetailMenuModule.assignImage(menu);
			if (menu.getSnapshotName() != null || menu.getSubObjName() != null) {
				imgFolder=urlBuilder.getResourceLinkByTheme(httpRequest, "/img/wapp/bullet_yellow.png",currTheme);
			} else {
				imgFolder=urlBuilder.getResourceLinkByTheme(httpRequest, "/img/wapp/bullet_blue.png",currTheme);
			}
			imgFolderOp=imgFolder;
		} else if (menu.getStaticPage() != null && !menu.getStaticPage().trim().equals("")) {
			imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/wapp/bullet_gray.png",currTheme);
			imgFolderOp=imgFolder;
		} else if (menu.getExternalApplicationUrl() != null) {
			imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/wapp/bullet_purple.png",currTheme);
			imgFolderOp=imgFolder;
		} else {
			imgFolder=urlBuilder.getResourceLinkByTheme(httpRequest, "/img/wapp/bullet_white.png",currTheme);
			imgFolderOp=imgFolder;
		}


		if (isRoot) {

			//htmlStream.append("	treeFunct.add(" + id + ", " + dTreeRootId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '', '', 'true', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(nameLabel)+"\\', \\'\\', \\'\\', \\'\\', \\'\\')');\n");
			if(hasChildren){
				htmlStream.append("	treeFunct.add(" + id + ", " + dTreeRootId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(id.toString())+"\\', \\'"+createDetailFunctionalityLink(id.toString())+"\\', \\'\\', \\'\\', \\'"+createMoveUpMenuLink(menu)+"\\',\\'"+createMoveDownMenuLink(menu)+"\\')');\n");				
			}
			else{
				htmlStream.append("	treeFunct.add(" + id + ", " + dTreeRootId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(id.toString())+"\\', \\'"+createDetailFunctionalityLink(id.toString())+"\\', \\'"+createRemoveFunctionalityLink(id.toString())+"\\', \\'"+createMasterNodeMenuLink(menu)+"\\', \\'"+createMoveUpMenuLink(menu)+"\\',\\'"+createMoveDownMenuLink(menu)+"\\')');\n");
			}

		} else {

			if(hasChildren){
				htmlStream.append("	treeFunct.add(" + id + ", " + parentId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(id.toString())+"\\', \\'"+createDetailFunctionalityLink(id.toString())+"\\', \\'\\', \\'"+createMasterNodeMenuLink(menu)+"\\',\\'"+createMoveUpMenuLink(menu)+"\\',\\'"+createMoveDownMenuLink(menu)+"\\')');\n");
			}
//			else if(leaves.contains(id)){
//				htmlStream.append("	treeFunct.add(" + id + ", " + parentId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'\\', \\'"+createDetailFunctionalityLink(id.toString())+"\\', \\'"+createRemoveFunctionalityLink(id.toString())+"\\', \\'"+createMasterNodeMenuLink(menu)+"\\',\\'"+createMoveUpMenuLink(menu)+"\\',\\'"+createMoveDownMenuLink(menu)+"\\')');\n");				
//			}
			else{
				htmlStream.append("	treeFunct.add(" + id + ", " + parentId + ",'" + name + "', 'javascript:linkEmpty()', '', '', '"+imgFolder+"', '"+imgFolderOp+"', '', 'menu" + requestIdentity + "(event, \\'"+createAddFunctionalityLink(id.toString())+"\\', \\'"+createDetailFunctionalityLink(id.toString())+"\\', \\'"+createRemoveFunctionalityLink(id.toString())+"\\', \\'"+createMasterNodeMenuLink(menu)+"\\',\\'"+createMoveUpMenuLink(menu)+"\\',\\'"+createMoveDownMenuLink(menu)+"\\')');\n");

			}

		}
	}

	/**
	 * @see it.eng.spagobi.commons.presentation.treehtmlgenerators.AdminTreeHtmlGenerator#makeConfigurationDtree(java.lang.StringBuffer)
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

	/**
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.AdminTreeHtmlGenerator#makeJSFunctionForMenu(java.lang.StringBuffer)
	 */
	private void makeJSFunctionForMenu(StringBuffer htmlStream) {
		htmlStream.append("		function menu" + requestIdentity + "(event, urlAdd, urlDetail, urlErase, urlCreateMaster, urlMoveUp, urlMoveDown) {\n");
		//htmlStream.append("	alert('urlCreateMaster: ' + urlCreateMaster + ' - urlMoveUp: ' + urlMoveUp + ' - urlMoveDown: ' + urlMoveDown );\n");
		htmlStream.append("			divM = document.getElementById('divmenuFunct" + requestIdentity + "');\n");
		htmlStream.append("			divM.innerHTML = '';\n");
		String capInsert = msgBuilder.getMessage("SBISet.MenuTree.insertCaption", "messages", httpRequest);
		htmlStream.append("			if(urlAdd!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlAdd+'\">"+capInsert+"</a></div>';\n");
		String capDetail = msgBuilder.getMessage("SBISet.MenuTree.detailCaption", "messages", httpRequest);
		htmlStream.append("			if(urlDetail!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlDetail+'\">"+capDetail+"</a></div>';\n");
		String capErase = msgBuilder.getMessage("SBISet.MenuTree.eraseCaption", "messages", httpRequest);
		htmlStream.append("			if(urlErase!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capErase+"\\', \\''+urlErase+'\\');\">"+capErase+"</a></div>';\n");
		String capCreateMaster = msgBuilder.getMessage("SBISet.MenuTree.createMasterNodeCaption", "messages", httpRequest);
		htmlStream.append("			if(urlCreateMaster=='disabled') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'white\\'\"><a class=\"dtreemenulinkdisabled\" href=\"\"><strike>"+capCreateMaster+"</strike></a></div>';\n");
		htmlStream.append("			else if(urlCreateMaster!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlCreateMaster+'\">"+capCreateMaster+"</a></div>';\n");				
		String capMoveUp = msgBuilder.getMessage("SBISet.MenuTree.moveUpCaption", "messages", httpRequest);
		htmlStream.append("			if(urlMoveUp=='disabled') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'white\\'\"><a class=\"dtreemenulinkdisabled\" href=\"\"><strike>"+capMoveUp+"</strike></a></div>';\n");
		htmlStream.append("			else if(urlMoveUp!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlMoveUp+'\">"+capMoveUp+"</a></div>';\n");
		String capMoveDown = msgBuilder.getMessage("SBISet.TreeFunct.moveDownCaption", "messages", httpRequest);
		htmlStream.append("			if(urlMoveDown=='disabled') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'white\\'\"><a class=\"dtreemenulinkdisabled\" href=\"\"><strike>"+capMoveDown+"</strike></a></div>';\n");
		htmlStream.append("			else if(urlMoveDown!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlMoveDown+'\">"+capMoveDown+"</a></div>';\n");
		htmlStream.append("				showMenu(event, divM);\n");
		htmlStream.append("		}\n");

		htmlStream.append("		function linkEmpty() {\n");
		htmlStream.append("		}\n");

		// js function for item action confirm
		String confirmCaption = msgBuilder.getMessage("SBISet.MenuTree.confirmCaption", "messages", httpRequest);
		htmlStream.append("		function actionConfirm(message, url){\n");
		htmlStream.append("			if (confirm('" + confirmCaption + " ' + message + '?')){\n");
		htmlStream.append("				location.href = url;\n");
		htmlStream.append("			}\n");
		htmlStream.append("		}\n");

	}
	
	/**
	 * Create URL to call the create master node from the child folder selected.
	 * @param folder	The folder to be master node
	 * @return	The URL to call the create master node functionality operation
	 */
	private String createMasterNodeFunctionalityLink(Menu menu) {
		HashMap createMasterUrlParamsMap = new HashMap();
		createMasterUrlParamsMap.put("ACTION_NAME", CreateMasterMenuAction.ACTION_NAME);
		createMasterUrlParamsMap.put(ObjectsTreeConstants.FUNCT_ID, menu.getMenuId().toString());
		String createMasterUrl = urlBuilder.getUrl(httpRequest, createMasterUrlParamsMap);
		return createMasterUrl;
	}

	/**
	 * Create URL to call the move up functionality operation.
	 * @param folder	The folder to be moved up
	 * @return	The URL to call the move up functionality operation
	 */
	private String createMoveUpFunctionalityLink(Menu menu) {
		HashMap moveUpUrlParamsMap = new HashMap();
		moveUpUrlParamsMap.put("ACTION_NAME", MoveUpLowFunctionality.ACTION_NAME);
		moveUpUrlParamsMap.put(ObjectsTreeConstants.FUNCT_ID, menu.getMenuId().toString());
		String moveUpUrl = urlBuilder.getUrl(httpRequest, moveUpUrlParamsMap);
		return moveUpUrl;
	}

	/**
	 * Create URL to call the move down fuctionality operation.
	 * @param folder	The folder to be moved down
	 * @return	The URL to call the move down functionality operation
	 */
	private String createMoveDownFunctionalityLink(Menu menu) {
		HashMap moveDownUrlParamsMap = new HashMap();
		moveDownUrlParamsMap.put("ACTION_NAME", MoveDownLowFunctionality.ACTION_NAME);
		moveDownUrlParamsMap.put(ObjectsTreeConstants.FUNCT_ID, menu.getMenuId().toString());
		String moveDownUrl = urlBuilder.getUrl(httpRequest, moveDownUrlParamsMap);
		return moveDownUrl;
	}





	/**
	 * Create URL to call the add fuctionality operation.
	 * @param path	The object tree path String
	 * @return	The URL to call the add functionality operation
	 */
	private String createAddFunctionalityLink(String parentId) {
		HashMap addUrlParamsMap = new HashMap();
		addUrlParamsMap.put(AdmintoolsConstants.PAGE, DetailMenuModule.MODULE_PAGE);
		addUrlParamsMap.put(AdmintoolsConstants.MESSAGE_DETAIL, AdmintoolsConstants.DETAIL_NEW);
		if(parentId!=null){
			addUrlParamsMap.put(DetailMenuModule.PARENT_ID, parentId);}
		String addUrl = urlBuilder.getUrl(httpRequest, addUrlParamsMap);
		return addUrl;
	}
	/**
	 * Create the URL to call the remove functionality operation.
	 * @param path	The object tree path String
	 * @return	the URL to call the remove functionality operation.
	 */
	private String createRemoveFunctionalityLink(String id) {
		HashMap delUrlParamsMap = new HashMap();
		delUrlParamsMap.put(AdmintoolsConstants.PAGE, DetailMenuModule.MODULE_PAGE);
		delUrlParamsMap.put(AdmintoolsConstants.MESSAGE_DETAIL, AdmintoolsConstants.DETAIL_DEL);
		delUrlParamsMap.put(DetailMenuModule.MENU_ID, id);
		String delUrl = urlBuilder.getUrl(httpRequest, delUrlParamsMap);
		return delUrl;
	}

	/**
	 * Create the URL to call the create functionality operation.
	 * @param path	The object tree path String
	 * @return	the URL to call the create functionality operation
	 */
	private String createDetailFunctionalityLink(String id) {
		HashMap createUrlParamsMap = new HashMap();
		createUrlParamsMap.put(AdmintoolsConstants.PAGE, DetailMenuModule.MODULE_PAGE);
		createUrlParamsMap.put(AdmintoolsConstants.MESSAGE_DETAIL, AdmintoolsConstants.DETAIL_SELECT);
		createUrlParamsMap.put(DetailMenuModule.MENU_ID, id);
		String createUrl = urlBuilder.getUrl(httpRequest, createUrlParamsMap);
		return createUrl;
	}

	/*
	public List fillDepths(List objectsList){
		HashMap idsMenus=new HashMap();
		ArrayList limitLeaves=new ArrayList();
		if(objectsList!=null){
			for (Iterator iterator = objectsList.iterator(); iterator.hasNext();) {
				Menu menu = (Menu) iterator.next();
				Integer id=menu.getMenuId();
				idsMenus.put(id, menu);
			}

			for (Iterator iterator = objectsList.iterator(); iterator.hasNext();) {
				Menu menu= (Menu) iterator.next();
				Integer id=menu.getMenuId();
				int depth=calculateDepth(idsMenus, menu);
				if(depth==3) // trace only limit leaves!
					limitLeaves.add(id);
				
			}
		}
		return limitLeaves;

	}
	*/

	public int calculateDepth(HashMap idsMenues, Menu menu){
		if(menu.getParentId()==null || menu.getParentId().intValue()==0) {
			return 0;
		}
		else 
		{
			return 1+calculateDepth(idsMenues, (Menu)idsMenues.get(menu.getParentId()));
		}
	}



	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList, HttpServletRequest httpRequest, String initialPath) {
		return null;
	}


	/**
	 * Create URL to call the create master node functionality operation from a child menu.
	 * @param folder	The folder to be transformed into master folder
	 * @return	The URL to call the create master menu operation
	 */
	private String createMasterNodeMenuLink(Menu menu) {
		if (canCreateMaster(menu)) {
			HashMap createMasterUrlParamsMap = new HashMap();
			createMasterUrlParamsMap.put("ACTION_NAME", CreateMasterMenuAction.ACTION_NAME);
			createMasterUrlParamsMap.put(DetailMenuModule.MENU_ID, menu.getMenuId().toString());
			String createMasterUrl = urlBuilder.getUrl(httpRequest, createMasterUrlParamsMap);
			return createMasterUrl;
		} else {
			return "disabled";
		}
	}
	
	/**
	 * Create URL to call the move up fuctionality operation.
	 * @param folder	The folder to be moved up
	 * @return	The URL to call the move up functionality operation
	 */
	private String createMoveUpMenuLink(Menu menu) {
		if (canBeMovedUp(menu)) {
			HashMap moveUpUrlParamsMap = new HashMap();
			moveUpUrlParamsMap.put("ACTION_NAME", MoveUpMenuAction.ACTION_NAME);
			moveUpUrlParamsMap.put(DetailMenuModule.MENU_ID, menu.getMenuId().toString());
			String moveUpUrl = urlBuilder.getUrl(httpRequest, moveUpUrlParamsMap);
			return moveUpUrl;
		} else {
			return "disabled";
		}
	}
	
	/**
	 * Create URL to call the move down functionality operation.
	 * @param folder	The folder to be moved down
	 * @return	The URL to call the move down functionality operation
	 */
	private String createMoveDownMenuLink(Menu menu) {
		if (canBeMovedDown(menu)) {
			HashMap moveDownUrlParamsMap = new HashMap();
			moveDownUrlParamsMap.put("ACTION_NAME", MoveDownMenuAction.ACTION_NAME);
			moveDownUrlParamsMap.put(DetailMenuModule.MENU_ID, menu.getMenuId().toString());
			String moveDownUrl = urlBuilder.getUrl(httpRequest, moveDownUrlParamsMap);
			return moveDownUrl;
		} else {
			return "disabled";
		}
	}

	private boolean canCreateMaster(Menu menu) {
		boolean canCreateMaster = false;
		Role[] currentRoles;
		Integer parentId;
		Menu parentMenu;
		Role[] parentRoles;
		
		logger.debug("IN");
		
		try {
			
			Assert.assertNotNull(menu, "Input parameter [menu] cannot be null");
			logger.debug("Menu id is equal to [" + menu.getMenuId()+ "]");
			logger.debug("Menu name is equal to [" + menu.getName()+ "]");
			logger.debug("Menu descr is equal to [" + menu.getDescr()+ "]");
			
			parentId = menu.getParentId();
			logger.debug("Parent menu id is equal to [" + menu.getMenuId()+ "]");
			if( parentId == null || parentId.intValue() == 0) {
				canCreateMaster = false;
			} else{
				
				parentMenu = null;
				try {
					parentMenu = DAOFactory.getMenuDAO().loadMenuByID(parentId);
				} catch (Throwable t) {
					logger.error("Impossible to load menu with id equal to [" + parentId + "] from database", t);
					return false;
				}
				
				parentRoles = parentMenu.getRoles();
				logger.debug("Parent menu have [" + parentRoles.length + "] role(s)");
				currentRoles = menu.getRoles();
				logger.debug("Menu have [" + currentRoles.length + "] role(s)");
	
				boolean equals=true;
				boolean found=false;
				for(int i = 0; i< parentRoles.length && equals; i++){			
					Role role = parentRoles[i];
					found = false;
					for(int j = 0; j < currentRoles.length && !found; j++){
						Role roleCurr=currentRoles[j];
						if(roleCurr.getName().equalsIgnoreCase(role.getName())) found=true;
						logger.debug("Parent menu role [" + i + "] named [" + role.getName() + "] is equal to " +
								"menu role [" + j + "] named [" + roleCurr.getName() + "] ? [" + found + "]");
					}
					if(!found)equals=false;
				}
	
				canCreateMaster = equals;
			}	
		} catch(Throwable t) {
			logger.error("An unpredicted error occours while executing method [canCreateMaster]", t);
		} finally {
			logger.debug("OUT");
		}
		
		return canCreateMaster;
	}

	private boolean canBeMovedUp(Menu menu) {
		return !(menu.getProg().intValue() == 1);
	}

	
	private boolean canBeMovedDown(Menu menu) {
		Integer parentId = (menu.getParentId()==null)?new Integer("0"):menu.getParentId();
		Integer currentProg = menu.getProg();
		Iterator it = _objectsList.iterator();
		while (it.hasNext()) {
			Menu aMenu = (Menu) it.next();
			Integer parentMenuId = (aMenu.getParentId()==null)?new Integer("0"):aMenu.getParentId();
			if (parentId != null && parentId.equals(parentMenuId) && currentProg.intValue() < aMenu.getProg().intValue())
				return true;
		}
		return false;
	}



}


