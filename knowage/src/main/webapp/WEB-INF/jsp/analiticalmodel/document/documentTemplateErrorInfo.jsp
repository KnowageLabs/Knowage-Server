<%--
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
--%>


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spago.error.EMFErrorHandler"%>
<%@page import="java.util.Iterator"%>
<%@page import="javax.portlet.PortletURL"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.Collection"%>
<%@page import="it.eng.spago.error.EMFAbstractError"%>

<%
	SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("DocumentTemplateBuildModule");
	String objectIdStr = (String) moduleResponse.getAttribute(ObjectsTreeConstants.OBJECT_ID);

    // recover error handler and error collection 
    EMFErrorHandler errorHandler = aResponseContainer.getErrorHandler();  
	Collection errors = errorHandler.getErrors();
	Iterator iter = errors.iterator();  
	
    // built url
	PortletURL backUrl = renderResponse.createActionURL();
	backUrl.setParameter(SpagoBIConstants.PAGE, "DetailBIObjectPage");
	backUrl.setParameter(SpagoBIConstants.MESSAGEDET, ObjectsTreeConstants.DETAIL_SELECT);
	backUrl.setParameter(ObjectsTreeConstants.OBJECT_ID, objectIdStr);
	backUrl.setParameter(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    
%>

<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section'>
			<spagobi:message key = "SBIInformationPage.title" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%= backUrl.toString() %>'> 
      			<img class='header-button-image-portlet-section' title='<spagobi:message key = "SBIInformationPage.backButt" />' src='<%= urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme) %>' alt='<spagobi:message key = "SBIInformationPage.backButt" />' />
			</a>
		</td>
	</tr>
</table>


<div class='div_background_no_img' style='padding-top:5px;padding-left:5px;'>
	<div class="div_detail_area_forms" >
	    <% 
	        EMFAbstractError error = null;
	        String description = "";
	    	while(iter.hasNext()) {
	    		error = (EMFAbstractError)iter.next();
	 		    description = error.getDescription();
	    %>
			<%= description %>
			<br/>
		<% } %>
	</div>
</div>		















