<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 


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















