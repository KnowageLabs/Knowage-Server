<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%
String messageBunle = "component_kpi_messages";
Map backUrlPars = new HashMap();
backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
String backUrl = urlBuilder.getUrl(request, backUrlPars);
%>
<table class='header-table-portlet-section'>		
	<tr class='header-row-portlet-section'>
		<td class='header-title-column-portlet-section' 
		    style='vertical-align:middle;padding-left:5px;'>
			<spagobi:message key = "sbi.kpi.grantsDefinition.label" bundle="<%=messageBunle%>" />
		</td>
		<td class='header-empty-column-portlet-section'>&nbsp;</td>
		<td class='header-button-column-portlet-section'>
			<a href='<%=backUrl%>'> 
      			<img class='header-button-image-portlet-section' 
      			title='<spagobi:message key = "sbi.generic.back" />' 
      			src='<%=urlBuilder.getResourceLinkByTheme(request, "/img/back.png", currTheme)%>' 
      			alt='<spagobi:message key = "sbi.generic.back" />' />
			</a>
		</td>
	</tr>
</table>

<%

String url = GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier)+  "&ACTION_NAME=MANAGE_OU_EMPTY_ACTION";
url += "&LANGUAGE=" + locale.getLanguage();
url += "&COUNTRY=" + locale.getCountry();
%>
	<iframe 
		id='grantsIframe'
		name='grantsIframe'
		src='<%= url %>'
		frameBorder = 0
		width=100%
		height=<%= ChannelUtilities.getPreferenceValue(aRequestContainer, "HEIGHT", "500") %>
	/>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>