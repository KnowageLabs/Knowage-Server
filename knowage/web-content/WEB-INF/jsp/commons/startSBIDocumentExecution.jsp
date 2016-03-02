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

<%@ page language="java" 
	contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.ExecuteDocumentAction"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%
String label = ChannelUtilities.getPreferenceValue(aRequestContainer, "DOCUMENT_LABEL", "");
String documentParameters = ChannelUtilities.getPreferenceValue(aRequestContainer, "DOCUMENT_PARAMETERS", "");
documentParameters = documentParameters.replaceAll("&", "%26");
documentParameters = documentParameters.replaceAll("=", "%3D");
String customizedViewLabel = ChannelUtilities.getPreferenceValue(aRequestContainer, "CUSTOMIZED_VIEW_LABEL", "");
String scheduledExecutionName = ChannelUtilities.getPreferenceValue(aRequestContainer, "SCHEDULED_EXECUTION_NAME", "");
String scheduledExecutionNumber = ChannelUtilities.getPreferenceValue(aRequestContainer, "SCHEDULED_EXECUTION_NUMBER", "");
String height = ChannelUtilities.getPreferenceValue(aRequestContainer, "HEIGHT", "600");
String toolbarVisible = ChannelUtilities.getPreferenceValue(aRequestContainer, "TOOLBAR_VISIBLE", "true");
String slidersVisible = ChannelUtilities.getPreferenceValue(aRequestContainer, "SLIDERS_VISIBLE", "true");

String url = GeneralUtilities.getSpagoBIProfileBaseUrl(userUniqueIdentifier)+  "&ACTION_NAME=" + ExecuteDocumentAction.SERVICE_NAME;
url += "&" + ObjectsTreeConstants.OBJECT_LABEL + "=" + label;
url += "&" + ObjectsTreeConstants.PARAMETERS + "=" + documentParameters;
url += "&" + SpagoBIConstants.SUBOBJECT_NAME + "=" + customizedViewLabel;
url += "&" + SpagoBIConstants.SNAPSHOT_NAME + "=" + scheduledExecutionName;
url += "&" + SpagoBIConstants.SNAPSHOT_HISTORY_NUMBER + "=" + scheduledExecutionNumber;
url += "&" + SpagoBIConstants.TOOLBAR_VISIBLE + "=" + toolbarVisible;
url += "&" + SpagoBIConstants.SLIDERS_VISIBLE + "=" + slidersVisible;
url += "&SBI_LANGUAGE=" + locale.getLanguage();
url += "&SBI_COUNTRY=" + locale.getCountry();

%>

<iframe 
	id='documentExecutionIframe'
	name='documentExecutionIframe'
	src='<%= url %>'
	frameBorder = 0
	width=100%
	height=<%= height %> >
</iframe>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
