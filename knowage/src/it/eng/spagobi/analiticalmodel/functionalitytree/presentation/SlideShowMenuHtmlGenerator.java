/* SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package it.eng.spagobi.analiticalmodel.functionalitytree.presentation;

import it.eng.spagobi.analiticalmodel.document.bo.BIObject;
import it.eng.spagobi.analiticalmodel.document.service.ExecutionWorkspaceModule;
import it.eng.spagobi.analiticalmodel.functionalitytree.bo.LowFunctionality;
import it.eng.spagobi.analiticalmodel.functionalitytree.service.TreeObjectsModule;
import it.eng.spagobi.commons.constants.ObjectsTreeConstants;
import it.eng.spagobi.commons.utilities.PortletUtilities;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;

public class SlideShowMenuHtmlGenerator implements ITreeHtmlGenerator {

	RenderResponse renderResponse = null;
	RenderRequest renderRequest = null;
	HttpServletRequest httpRequest = null;
	private String baseFolderPath = null;
	List objectsList = new ArrayList();
	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeAccessibleTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeAccessibleTree(List objectsList,
			HttpServletRequest httpRequest, String initialPath) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String, java.lang.String)
	 */
	public StringBuffer makeTree(List objectsList,
			HttpServletRequest httpRequest, String initialPath, String treename) {
		return makeTree(objectsList, httpRequest, initialPath);
	}

	
	/* (non-Javadoc)
	 * @see it.eng.spagobi.analiticalmodel.functionalitytree.presentation.ITreeHtmlGenerator#makeTree(java.util.List, javax.servlet.http.HttpServletRequest, java.lang.String)
	 */
	public StringBuffer makeTree(List objsList, HttpServletRequest httpReq, String initialPath) {
		
		objectsList = objsList;
		httpRequest = httpReq;
		baseFolderPath = initialPath;
		renderResponse =(RenderResponse)httpRequest.getAttribute("javax.portlet.response");
		renderRequest = (RenderRequest)httpRequest.getAttribute("javax.portlet.request");	
		StringBuffer stream = new StringBuffer();
		StringBuffer htmlStream = new StringBuffer();
		StringBuffer jsStream = new StringBuffer();
		jsStream.append("<script>\n");
		jsStream.append("	menulevels = new Array();\n");
		
		htmlStream.append("<SCRIPT language='JavaScript' src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/js/prototype/javascripts/prototype.js" )+"'></SCRIPT>");
		htmlStream.append("<SCRIPT language='JavaScript' src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/js/menu.js" )+"'></SCRIPT>");
		htmlStream.append("<SCRIPT language='JavaScript' src='"+renderResponse.encodeURL(renderRequest.getContextPath() + "/js/spagobi.js" )+"'></SCRIPT>");
		
		LowFunctionality root = findRoot(); 
		List childs = findChilds(root);
		if(!childs.isEmpty()) {
			generateMenu(root, childs, htmlStream, true, 0);
			processChilds(childs, htmlStream, jsStream, 1);
		}
		
		jsStream.append("</script>\n");
		
		stream.append(jsStream);
		stream.append(htmlStream);
		return stream;
	}

	
	private void processChilds(List childs, StringBuffer htmlStream, StringBuffer jsStream, int level) {
		Iterator iterChilds = childs.iterator();
		while(iterChilds.hasNext()) {
			Object obj = iterChilds.next();
			if(obj instanceof LowFunctionality) {
				LowFunctionality folder = (LowFunctionality)obj;
				List folderchilds = findChilds(folder);
				if(!folderchilds.isEmpty()) {
					jsStream.append("	menulevels[menulevels.length] = new Array('"+folder.getId()+"', '"+level+"');\n");
					processChilds(folderchilds, htmlStream, jsStream, level + 1);
					generateMenu(folder, folderchilds, htmlStream, false, level);
				}
			} 
		}
	}
	
	private void generateMenu(LowFunctionality folder, List childs, StringBuffer htmlStream, boolean isroot, int level) {
		String style = "";
		if(!isroot){
			style = "display:none;position:absolute;";
		}
		htmlStream.append("<div style='"+style+"' id=\"menu_"+folder.getId()+"\" class=\"menuBox\">					\n");
		htmlStream.append("		<table cellspacing='0' cellpadding='0' border='0' >					\n");
		htmlStream.append("			<tr>															\n");
		htmlStream.append("				<td height='20' valign='center' align='center' class=\"menuTitleBox\" >								\n");
		htmlStream.append("					"+folder.getName()+"									\n");
		htmlStream.append("				</td>														\n");
		htmlStream.append("			</tr>															\n");
		htmlStream.append("			<tr>															\n");
		htmlStream.append("				<td class=\"menuContentBox\">								\n");
		htmlStream.append("					<table cellspacing='0' cellpadding='0' border='0' >		\n");
		addMenuItemHtmlCode(childs, htmlStream, level);
		htmlStream.append("					</table>												\n");
		htmlStream.append("				</td>														\n");
		htmlStream.append("			</tr>															\n");
		htmlStream.append("		</table>															\n");  
		htmlStream.append("</div>															\n");
	}
	
	
	private void addMenuItemHtmlCode(List menuobjs, StringBuffer htmlStream, int level) {
		Iterator iterChilds = menuobjs.iterator();
		while(iterChilds.hasNext()) {
			Object obj = iterChilds.next();
			if(obj instanceof LowFunctionality) {
				LowFunctionality folderchild = (LowFunctionality)obj;
				String imgFolder = PortletUtilities.createPortletURLForResource(httpRequest, "/img/treefolder.gif");
				htmlStream.append("			<tr valign='middle' height='30' id='menu_link_"+folderchild.getId()+"' class='menuItem' " +
						          "             onmouseover=\"openmenu('"+folderchild.getId()+"', event);overHandler(this, '"+level+"', event, '"+folderchild.getId()+"');\" " +
						          "             onmouseout=\"outHandler(this, event);checkclosemenu('"+folderchild.getId()+"', event);\" >	\n");
				htmlStream.append("				<td style='vertical-align:middle;' width='40' align='center' valign='middle' >\n");
				htmlStream.append("					<img width='20' height='20' src=\""+imgFolder+"\" /> \n");
				htmlStream.append("				</td>\n");
				htmlStream.append("				<td style='vertical-align:middle;'>\n");
				htmlStream.append("					<a class='menuLink' href='javascript:void(0)' >"+folderchild.getName()+"</a>\n");
				htmlStream.append("				</td>\n");
				htmlStream.append("				<td style='vertical-align:middle;' id='menu_link_last_"+folderchild.getId()+"' width='25' align='center'>\n");
				htmlStream.append("					<span class='menuArrow'>&gt;&gt;</span>\n");
				htmlStream.append("				</td>\n");
				htmlStream.append("			</tr>	\n");
			} else if (obj instanceof BIObject) {
				BIObject biobj = (BIObject)obj;
				String execUrl = getExecutionLink(biobj);
				String biObjType = biobj.getBiObjectTypeCode();
				String imgUrl = "/img/objecticon_"+ biObjType+ ".png";
				String userIcon = PortletUtilities.createPortletURLForResource(httpRequest, imgUrl);
				htmlStream.append("			<tr valign='middle' height='30' class='menuItem' " +
				          		  "             onmouseover=\"overHandler(this, '"+level+"', event);\" " +
				          		  "             onmouseout=\"outHandler(this, event);\" >	\n");
				htmlStream.append("				<td style='vertical-align:middle;' width='40' align='center' >\n");
				htmlStream.append("					<img width='20' height='20' src=\""+userIcon+"\" /> \n");
				htmlStream.append("				</td>\n");
				htmlStream.append("				<td style='vertical-align:middle;'> \n");
				htmlStream.append("					<a class='menuLink' href=\""+execUrl+"\">"+biobj.getName()+"</a>\n");
				htmlStream.append("				</td>\n");
				htmlStream.append("				<td style='vertical-align:middle;' width='25' align='center'>\n");
				htmlStream.append("					&nbsp;\n");
				htmlStream.append("				</td>\n");
				htmlStream.append("			</tr>	\n");
			}	
		}
	}
	
	
	
	private String getExecutionLink(BIObject biobj) {
		PortletURL execUrl = renderResponse.createActionURL();
		execUrl.setParameter(ObjectsTreeConstants.PAGE, ExecutionWorkspaceModule.MODULE_PAGE);
		execUrl.setParameter(ObjectsTreeConstants.OBJECT_LABEL, biobj.getLabel());
		execUrl.setParameter(TreeObjectsModule.PATH_SUBTREE, baseFolderPath);
		return execUrl.toString();
	}
	
	
	private List findChilds(LowFunctionality father) {
		List roots = new ArrayList();
		// add biobjects
		roots.addAll(father.getBiObjects());
		// add child folders
		Iterator iterFolder1 = objectsList.iterator();
		while(iterFolder1.hasNext()){
			LowFunctionality folder1 = (LowFunctionality)iterFolder1.next();
			Integer parId = folder1.getParentId();
			if(parId.equals(father.getId())) {
				roots.add(folder1);
			}
		}
		return roots;
	}
	
	
	private LowFunctionality findRoot() {
		LowFunctionality root = null;
		Iterator iterFolder1 = objectsList.iterator();
		while(iterFolder1.hasNext()){
			LowFunctionality folder1 = (LowFunctionality)iterFolder1.next();
			Integer parId = folder1.getParentId();
			Iterator iterFolder2 = objectsList.iterator();
			boolean hasFather = false;
			while(iterFolder2.hasNext()) {
				LowFunctionality folder2 = (LowFunctionality)iterFolder2.next();
				if(folder2.getId().equals(parId)){
					hasFather = true;
				}
			}
			if(!hasFather) {
				root = folder1;
				break;
			}
		}
		return root;
	}
	
	
	private void makeJSFunctionForMenu(StringBuffer htmlStream) {
		htmlStream.append("		function linkEmpty() {\n");
		htmlStream.append("		}\n");
	}
	
}
