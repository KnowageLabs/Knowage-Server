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


 <!--
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
-->

<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jspf"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>
<%@include file="/WEB-INF/jsp/analiticalmodel/document/TemplateBuildImport.jsp" %>

<%@page import="org.safehaus.uuid.UUIDGenerator"%>
<%@page import="org.safehaus.uuid.UUID"%>
<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="it.eng.spagobi.commons.constants.ObjectsTreeConstants"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.Iterator"%>               
<%@page import="it.eng.spagobi.engines.drivers.EngineURL"%>
<%@page import="java.util.Map" %>
<%@page import="java.util.HashMap" %>
<%@page import="it.eng.knowage.commons.security.KnowageSystemConfiguration"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%
	UUIDGenerator uuidGen  = UUIDGenerator.getInstance();
	UUID uuid = uuidGen.generateTimeBasedUUID();
	String requestIdentity = "request" + uuid.toString();
    // get module response
    SourceBean moduleResponse = (SourceBean) aServiceResponse.getAttribute("DocumentTemplateBuildModule");
	// get the BiObject from the response
    BIObject obj = (BIObject) moduleResponse.getAttribute("biobject");
	// get the url of the engine
	EngineURL engineurl = (EngineURL) moduleResponse.getAttribute(ObjectsTreeConstants.CALL_URL);
    String operation = (String) moduleResponse.getAttribute("operation");
	
	// build the string of the title
    String title = "";
	if (operation != null && operation.equalsIgnoreCase("newDocumentTemplate")) {
		title = msgBuilder.getMessage("SBIDev.docConf.templateBuild.newTemplateTitle", "messages", request);
	} else {
		title = msgBuilder.getMessage("SBIDev.docConf.templateBuild.editTemplateTitle", "messages", request);
	}
    title += " : " + obj.getName();


   	// try to get from the preferences the height of the area
   	String heightArea = (String) ChannelUtilities.getPreferenceValue(aRequestContainer, "HEIGHT_AREA", "600");

	StringBuffer urlToCall= new StringBuffer(engineurl.getMainURL());
	urlToCall.append("?"+SpagoBIConstants.SBI_LANGUAGE+"="+locale.getLanguage());
	urlToCall.append("&"+SpagoBIConstants.SBI_COUNTRY+"="+locale.getCountry());

%>
<!-- WEBLOGIC CONFS START 
	<html ng-app>
		<body style="overflow: hidden">
			<iframe flex class=" noBorder" width="100%" height="100%" ng-src="<%= StringEscapeUtils.escapeJavaScript(GeneralUtilities.getUrl(urlToCall.toString(), engineurl.getParameters())) %>" name="angularIframe"></iframe>
		</body>
	</html>
-->

	
	<html ng-app="TemplateBuildModule" ng-controller="TemplateBuildController">
		<body style="overflow: hidden">
			<iframe flex class=" noBorder" width="100%" height="100%" ng-src="<%= (GeneralUtilities.getUrl(urlToCall.toString(), engineurl.getParameters())) %>" name="angularIframe"></iframe>
		</body>
	</html>
	
	 

