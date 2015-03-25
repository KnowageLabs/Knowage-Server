<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@page import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetStartAction"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/main.css", currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/listview.css", currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-small.css",currTheme)%>'/>
<link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/browser/standard.css",currTheme)%>'/>


<%
    String executionId = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_EXECUTION_ID);
    String worksheetEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL);
    String qbeEditFromBMActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL);
    String qbeEditFromDataSetActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL);
    String georeportEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL);
    String cockpitEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_COCKPIT_EDIT_SERVICE_URL);
    String fromMyAnalysis = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYANALYSIS);
    String contextName = ChannelUtilities.getSpagoBIContextName(request);
%>

<script type="text/javascript">



    Ext.onReady(function(){
		var selfService = Ext.create('Sbi.adhocreporting.AdhocreportingContainer',{
        	worksheetEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(worksheetEditActionUrl) %>'
            , qbeFromBMBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromBMActionUrl) %>'
            , qbeFromDataSetBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromDataSetActionUrl) %>'
            , user: Sbi.user.userUniqueIdentifier
            , georeportEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(georeportEditActionUrl) %>'
            , cockpitEngineBaseUrl: '<%= StringEscapeUtils.escapeJavaScript(cockpitEditActionUrl) %>'
            , myAnalysisServicePath: 'documents/myAnalysisDocsList'
            , fromMyAnalysis: '<%=fromMyAnalysis%>'
            , contextName: '<%= StringEscapeUtils.escapeJavaScript(contextName) %>'
		}); //by alias
		
		var datasetListViewport = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [selfService]	     	
	    });
    });
	
</script>
 

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>
