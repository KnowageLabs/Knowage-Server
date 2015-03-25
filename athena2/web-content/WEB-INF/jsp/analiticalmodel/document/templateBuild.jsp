<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
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


<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>

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
    
	//String backEndContext=GeneralUtilities.getSpagoBiHostBackEnd();
	//String param1="?"+SpagoBIConstants.SBI_BACK_END_HOST+"="+backEndContext;
	String context=GeneralUtilities.getSpagoBiContext();
	String param2="?"+SpagoBIConstants.SBI_CONTEXT+"="+context;
	String host=GeneralUtilities.getSpagoBiHost();
	String param3="&"+SpagoBIConstants.SBI_HOST+"="+host;

	StringBuffer urlToCall= new StringBuffer(engineurl.getMainURL());
	//urlToCall+=param1;
	urlToCall.append(param2);
	urlToCall.append(param3);
	urlToCall.append("&"+SpagoBIConstants.SBI_LANGUAGE+"="+locale.getLanguage());
	urlToCall.append("&"+SpagoBIConstants.SBI_COUNTRY+"="+locale.getCountry());
	
   	
   	// build the back link
   	Map backUrlPars = new HashMap();
   	backUrlPars.put(SpagoBIConstants.PAGE, "DetailBIObjectPage");
   	backUrlPars.put(SpagoBIConstants.MESSAGEDET, ObjectsTreeConstants.DETAIL_SELECT);
   	backUrlPars.put(ObjectsTreeConstants.OBJECT_ID, obj.getId().toString());
   	backUrlPars.put(LightNavigationManager.LIGHT_NAVIGATOR_BACK_TO, "1");
    String backUrl = urlBuilder.getUrl(request, backUrlPars);

%>


<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/ux/miframe/miframe-min.js")%>'></script>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/execution/main.css",currTheme)%>'/>

<script>
Ext.onReady(function(){

    var backButton = new Ext.Toolbar.Button({
    	iconCls: 'icon-back'
		, scope: this
		, handler : function() {window.location.href = '<%= backUrl %>';}
    });

    var items = ['->', backButton];
    
    var toolbar = new Ext.Toolbar({
      items: items
    });

	var templateEditIFrame = new Ext.ux.ManagedIframePanel({
		title: '<%= StringEscapeUtils.escapeJavaScript(title) %>'
		, defaultSrc: '<%= StringEscapeUtils.escapeJavaScript(GeneralUtilities.getUrl(urlToCall.toString(), engineurl.getParameters())) %>'
		, autoLoad: true
        , loadMask: true
        , disableMessaging: true
        , tbar: toolbar
        , renderTo: Sbi.user.ismodeweb ? undefined : 'edit_template_<%=requestIdentity%>'  
	});

	if (Sbi.user.ismodeweb) {
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [templateEditIFrame]
			    }
			]
		});
	}
		
});
</script>

<% if (sbiMode == "PORTLET") { %>
	<div name="edit_template_<%=requestIdentity%>" id="edit_template_<%=requestIdentity%>" 
		style="width:100%;height:<%=heightArea+"px;"%>">
	</div>	
<% } %>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>