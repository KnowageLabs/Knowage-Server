/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import it.eng.spago.base.RequestContainer;
import it.eng.spago.base.SessionContainer;
import it.eng.spago.security.IEngUserProfile;
import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.service.DetailBIObjectModule;
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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * Contains all methods needed to generate and modify a tree object for Developing.
 * There are methods to generate tree, configure, insert and modify elements.
 */
public class DevTreeHtmlGenerator implements ITreeHtmlGenerator {

	HttpServletRequest httpRequest = null;
	RequestContainer reqCont = null;
	protected IUrlBuilder urlBuilder = null;
	protected IMessageBuilder msgBuilder = null;
	int progrJSTree = 0;
	IEngUserProfile profile = null;
	private int dTreeRootId = -100;
	private int dTreeObjects = -1000;
	protected String requestIdentity = null;
	private String currTheme="";

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
		htmlStream.append("		function menu" + requestIdentity + "(prog, event, urlExecution, urlMetadata, urlDetail, urlErase, urlDown, urlUp) {\n");
		
		htmlStream.append("			divM = document.getElementById('divmenuFunct" + requestIdentity + "');\n");
		htmlStream.append("			divM.innerHTML = '';\n");
		String capExec = msgBuilder.getMessage("SBISet.devObjects.captionExecute", "messages", httpRequest);
		htmlStream.append("			if(urlExecution!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlExecution+'\">"+capExec+"</a></div>';\n");
		/*String capMetadata = msgBuilder.getMessage("SBISet.objects.captionMetadata", "messages", httpRequest);
		htmlStream.append("			if(urlMetadata!=''){ divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:makePopup(\\''+prog+'\\',\\''+urlMetadata+'\\')\" >"+capMetadata+"</a></div>'; }\n");
			*/												
		String capDetail = msgBuilder.getMessage("SBISet.devObjects.captionDetail", "messages", httpRequest);
		htmlStream.append("			if(urlDetail!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"'+urlDetail+'\">"+capDetail+"</a></div>';\n");
		String capErase = msgBuilder.getMessage("SBISet.devObjects.captionErase", "messages", httpRequest);
		htmlStream.append("         if(urlErase!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capErase+"\\', \\''+urlErase+'\\');\">"+capErase+"</a></div>';\n");
		String capMoveDown = msgBuilder.getMessage("SBISet.objects.captionMoveDownShort", "messages", httpRequest);
		htmlStream.append("         if(urlDown!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capMoveDown+"\\', \\''+urlDown+'\\');\">"+capMoveDown+"</a></div>';\n");
		String capMoveUp = msgBuilder.getMessage("SBISet.objects.captionMoveUpShort", "messages", httpRequest);
		htmlStream.append("         if(urlUp!='') divM.innerHTML = divM.innerHTML + '<div onmouseout=\"this.style.backgroundColor=\\'white\\'\"  onmouseover=\"this.style.backgroundColor=\\'#eaf1f9\\'\" ><a class=\"dtreemenulink\" href=\"javascript:actionConfirm(\\'"+capMoveUp+"\\', \\''+urlUp+'\\');\">"+capMoveUp+"</a></div>';\n");
	
		
		htmlStream.append("				showMenu(event, divM);\n");
		htmlStream.append("		}\n");
		
		htmlStream.append("		function linkEmpty() {\n");
		htmlStream.append("		}\n");
        
		// js function for item action confirm
        String confirmCaption = msgBuilder.getMessage("SBISet.devObjects.treeConfirmCaption", "messages", httpRequest);
        htmlStream.append("     function actionConfirm(message, url){\n");
        htmlStream.append("         if (confirm('" + confirmCaption + " ' + message + '?')){\n");
        htmlStream.append("             location.href = url;\n");
        htmlStream.append("         }\n");
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
	    UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
	    UUID uuid = uuidGen.generateTimeBasedUUID();
	    requestIdentity = uuid.toString();
	    requestIdentity = requestIdentity.replaceAll("-", "");
		httpRequest = httpReq;
		reqCont = ChannelUtilities.getRequestContainer(httpRequest);
		urlBuilder = UrlBuilderFactory.getUrlBuilder();
		msgBuilder = MessageBuilderFactory.getMessageBuilder();
		SessionContainer sessionContainer = reqCont.getSessionContainer();
		SessionContainer permanentSession = sessionContainer.getPermanentContainer();
        profile = (IEngUserProfile)permanentSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);

    	currTheme=ThemesManager.getCurrentTheme(reqCont);
    	if(currTheme==null)currTheme=ThemesManager.getDefaultTheme();
        
        StringBuffer htmlStream = new StringBuffer();
		htmlStream.append("<LINK rel='StyleSheet' href='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/css/dtree.css", currTheme )+"' type='text/css' />");
		//makeConfigurationDtree(htmlStream);
		String nameTree = msgBuilder.getMessage("tree.objectstree.name" ,"messages", httpRequest);
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/dtree.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<SCRIPT language='JavaScript' src='"+urlBuilder.getResourceLinkByTheme(httpRequest, "/js/contextMenu.js", currTheme )+"'></SCRIPT>");
		htmlStream.append("<table width='100%'>");
		htmlStream.append("	<tr height='1px'>");
		htmlStream.append("		<td width='10px'>&nbsp;</td>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("	</tr>");
		htmlStream.append("	<tr>");
		htmlStream.append("		<td>&nbsp;</td>");
		htmlStream.append("		<td id='treeDevObjTd" + requestIdentity + "' name='treeDevObjTd" + requestIdentity + "'>&nbsp;</td>");
		htmlStream.append("			<script language=\"JavaScript1.2\">\n");
	   	htmlStream.append("				var nameTree = 'treeDevObjects';\n");

		String context=httpRequest.getContextPath();
		if (!(context.charAt(context.length() - 1) == '/')) {
			context += '/';
		}
		context+="themes/"+currTheme+"/";
	   	
	   	htmlStream.append("				treeDevObjects = new dTree('treeDevObjects', '" + context + "');\n");
	   	htmlStream.append("	        	treeDevObjects.add(" + dTreeRootId + ",-1,'"+nameTree+"');\n");
	   	Iterator it = objectsList.iterator();
	   	while (it.hasNext()) {
	   		LowFunctionality folder = (LowFunctionality) it.next();
	   		/* ********* start luca changes *************** */
	   		boolean isUserFunct = folder.getPath().startsWith("/"+((UserProfile)profile).getUserId());
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
    	htmlStream.append("				document.getElementById('treeDevObjTd" + requestIdentity + "').innerHTML = treeDevObjects;\n");
    	makeJSFunctionForMenu(htmlStream);	
		htmlStream.append("			</script>\n");
		htmlStream.append("	</tr>");
		htmlStream.append("</table>");
		htmlStream.append("<div id='divmenuFunct" + requestIdentity + "' class='dtreemenu' onmouseout='hideMenu(event, \"divmenuFunct" + requestIdentity + "\");' >");
		htmlStream.append("		menu");
		htmlStream.append("</div>");
		return htmlStream;
		
	}

	private void addItemForJSTree(StringBuffer htmlStream, LowFunctionality folder, boolean isRoot) {
		
		String nameLabel = folder.getName();
		
		//String name = msgBuilder.getUserMessage(nameLabel, null, httpRequest);
		String name = msgBuilder.getI18nMessage(nameLabel, httpRequest);
		name = StringEscapeUtils.escapeJavaScript(name);
		Integer idFolder = folder.getId();
		Integer parentId = folder.getParentId();
		boolean canExec = ObjectsAccessVerifier.canExec(idFolder, profile);
		boolean canDev = ObjectsAccessVerifier.canDev(idFolder, profile);

		if (isRoot) {
			htmlStream.append("	treeDevObjects.add(" + idFolder + ", " + dTreeRootId + ",'" + name + "', '', '', '', '', '', 'true');\n");
		} else {
			if (canDev || canExec) {
				String imgFolder = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolder.gif", currTheme);
				String imgFolderOp = urlBuilder.getResourceLinkByTheme(httpRequest, "/img/treefolderopen.gif", currTheme);
				htmlStream.append("	treeDevObjects.add(" + idFolder + ", " + parentId + ",'" + name + "', '', '', '', '" + imgFolder + "', '" + imgFolderOp + "', '', '');\n");
				List objects = folder.getBiObjects();
				for (Iterator it = objects.iterator(); it.hasNext(); ) {
					
					BIObject obj = (BIObject) it.next();
					String biObjType = obj.getBiObjectTypeCode();
					String imgUrl = "/img/objecticon_"+ biObjType+ ".png";
					String userIcon = urlBuilder.getResourceLinkByTheme(httpRequest, imgUrl, currTheme);
					String biObjState = obj.getStateCode();
					String stateImgUrl = "/img/stateicon_"+ biObjState+ ".png";
					String stateIcon = urlBuilder.getResourceLinkByTheme(httpRequest, stateImgUrl, currTheme);
					Integer idObj = obj.getId();
					String stateObj = obj.getStateCode();
					String prog = idObj.toString();
					//String localizedName=msgBuilder.getUserMessage(obj.getName(), SpagoBIConstants.DEFAULT_USER_BUNDLE, httpRequest);
					String localizedName=msgBuilder.getI18nMessage(obj.getName(), httpRequest);
					localizedName = StringEscapeUtils.escapeJavaScript(localizedName);
					if (canDev && (stateObj.equals("DEV"))) {
						htmlStream.append("	treeDevObjects.add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + localizedName + "', 'javascript:linkEmpty()', '', '', '" + userIcon + "', '', '', 'menu" + requestIdentity + "("+prog+", event, \\'" + createExecuteObjectLink(idObj) + "\\',\\'" + createMetadataObjectLink(idObj) + "\\', \\'" + createDetailObjectLink(idObj) + "\\', \\'" + createEraseObjectLink(idObj, idFolder) + "\\', \\'\\', \\'" +createMoveUpObjectLink(idObj) + "\\')' );\n");
					} else if(canExec && (stateObj.equals("REL"))) {
						htmlStream.append("	treeDevObjects.add(" + dTreeObjects-- + ", " + idFolder + ",'<img src=\\'" + stateIcon + "\\' /> " + localizedName + "', 'javascript:linkEmpty()', '', '', '" + userIcon + "', '', '', 'menu" + requestIdentity + "("+prog+", event, \\'" + createExecuteObjectLink(idObj) + "\\', \\'" + createMetadataObjectLink(idObj) + "\\', \\'\\', \\'\\', \\'\\', \\'\\')' );\n");
					}
				}
			}
		}
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
		execUrlParMap.put(ObjectsTreeConstants.BIOBJECT_TREE_LIST, ObjectsTreeConstants.BIOBJECT_TREE_LIST);		
		execUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
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
		detUrlParMap.put("LIGHT_NAVIGATOR_DISABLED", "true");
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		String detUrl = urlBuilder.getUrl(httpRequest, detUrlParMap);
		return detUrl;
	}
	
	private String createMoveDownObjectLink(Integer id) {
		HashMap detUrlParMap = new HashMap();
		detUrlParMap.put(ObjectsTreeConstants.PAGE, "UpdateBIObjectStatePage" );
		detUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.MOVE_STATE_DOWN);
		detUrlParMap.put("LIGHT_NAVIGATOR_DISABLED", "true");
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		String detUrl = urlBuilder.getUrl(httpRequest, detUrlParMap);
		return detUrl;
	}
	

	private String createDetailObjectLink(Integer id) {
		HashMap detUrlParMap = new HashMap();
		detUrlParMap.put(ObjectsTreeConstants.PAGE, DetailBIObjectModule.MODULE_PAGE);
		detUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.DETAIL_SELECT);
		detUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, id.toString());
		String detUrl = urlBuilder.getUrl(httpRequest, detUrlParMap);
		return detUrl;
	}
	
	private String createEraseObjectLink(Integer idObj, Integer idFunct) {
		HashMap delUrlParMap = new HashMap();
		delUrlParMap.put(ObjectsTreeConstants.PAGE, DetailBIObjectModule.MODULE_PAGE);
		delUrlParMap.put(ObjectsTreeConstants.MESSAGE_DETAIL, ObjectsTreeConstants.DETAIL_DEL);
		delUrlParMap.put(ObjectsTreeConstants.OBJECT_ID, idObj.toString());
		delUrlParMap.put(ObjectsTreeConstants.FUNCT_ID, idFunct.toString());
		String delUrl = urlBuilder.getUrl(httpRequest, delUrlParMap);
		return delUrl;
	}
	
}
