<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.SelectDatasetAction"%>
<%@page import="java.util.HashMap"%>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.analiticalmodel.execution.service.CreateDatasetForWorksheetAction"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base.jsp"%>
<%@ include file="/WEB-INF/jsp/commons/includeMessageResource.jspf" %>
<%@ include file="/WEB-INF/jsp/commons/importSbiJS.jspf"%>

<script type="text/javascript"
	src='<%=urlBuilder.getResourceLink(request, "/js/lib/ext-2.0.1/ux/miframe/miframe-min.js")%>'></script>

<%
	String executionId = (String) aResponseContainer.getServiceResponse().getAttribute(SelectDatasetAction.OUTPUT_PARAMETER_EXECUTION_ID);
    String worksheetEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelectDatasetAction.OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL);
	String datasetLabel = (String) aResponseContainer.getServiceResponse().getAttribute(SelectDatasetAction.OUTPUT_PARAMETER_DATASET_LABEL);

	Map<String, String> datasetParameterValuesMap = (Map<String, String>) aResponseContainer.getServiceResponse().getAttribute(CreateDatasetForWorksheetAction.OUTPUT_PARAMETER_DATASET_PARAMETERS);
	if(datasetParameterValuesMap == null) datasetParameterValuesMap=new HashMap<String, String>();
	String businessMetadata = (String) aResponseContainer.getServiceResponse().getAttribute(CreateDatasetForWorksheetAction.OUTPUT_PARAMETER_BUSINESS_METADATA);

    String selectedDatasourceLabel = (String) aResponseContainer.getServiceResponse().getAttribute(SelectDatasetAction.OUTPUT_PARAMETER_DATASOURCE_LABEL);
	
	String title ="";
	String engine = (String) aRequestContainer.getServiceRequest().getAttribute("ENGINE");

%>

<script type="text/javascript">

	var url = {
	    	host: '<%= request.getServerName()%>'
	    	, port: '<%= request.getServerPort()%>'
	    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
	    	   				  request.getContextPath().substring(1):
	    	   				  request.getContextPath()%>'
	    	    
	};
	
	var params = {
	    SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
	    , LIGHT_NAVIGATOR_DISABLED: 'TRUE'
	};
	
	Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		baseUrl: url
	    , baseParams: params
	});
	
	Ext.onReady(function(){
	
		var templateEditIFrame = new Sbi.worksheet.WorksheetEditorIframePanel({
			title: '<%= StringEscapeUtils.escapeJavaScript(title) %>'
			, defaultSrc: '<%= StringEscapeUtils.escapeJavaScript(worksheetEditActionUrl) %>'
			, businessMetadata : <%= businessMetadata %>
			, datasetLabel : '<%= datasetLabel %>'
	        , datasourceLabel : '<%= selectedDatasourceLabel%>'
			, datasetParameters : <%= new JSONObject(datasetParameterValuesMap).toString() %>
	        , engine : '<%= engine %>'
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
 
<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>