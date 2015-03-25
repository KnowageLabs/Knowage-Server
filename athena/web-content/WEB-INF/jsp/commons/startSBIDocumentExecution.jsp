<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  

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