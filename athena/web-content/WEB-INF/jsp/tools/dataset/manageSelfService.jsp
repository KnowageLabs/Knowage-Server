<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%@page import="it.eng.spagobi.user.UserProfileManager"%>
<%@page
	import="it.eng.spagobi.tools.dataset.service.SelfServiceDatasetStartAction"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Collection"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base410.jsp"%>
<link rel='stylesheet' type='text/css'
	href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/main.css", currTheme)%>' />
<link rel='stylesheet' type='text/css'
	href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/listview.css", currTheme)%>' />

<%
String isMyData =((String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYDATA)!=null)?(String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYDATA):"FALSE";
if (isMyData.equalsIgnoreCase("FALSE")) {%>
<link rel='stylesheet' type='text/css'
	href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-small.css",currTheme)%>' />
<%}else{%>
<link rel='stylesheet' type='text/css'
	href='<%=urlBuilder.getResourceLinkByTheme(request, "css/tools/dataset/catalogue-item-big.css",currTheme)%>' />
<%} %>
<!--  <link rel='stylesheet' type='text/css' href='<%=urlBuilder.getResourceLinkByTheme(request, "css/home40/standard.css",currTheme)%>'/>-->
<link rel='stylesheet' type='text/css'
	href='<%=urlBuilder.getResourceLinkByTheme(request, "css/analiticalmodel/browser/standard.css",currTheme)%>' />

<%
    String executionId = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_EXECUTION_ID);
    String worksheetEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_WORKSHEET_EDIT_SERVICE_URL);
    String qbeEditFromBMActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_BM_SERVICE_URL);
    String qbeEditFromDataSetActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_FROM_DATA_SET_SERVICE_URL);
    String qbeEditDatasetActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_QBE_EDIT_DATASET_SERVICE_URL);
    String georeportEditActionUrl = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.OUTPUT_PARAMETER_GEOREPORT_EDIT_SERVICE_URL);
    String typeDoc = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.TYPE_DOC);
    String fromMyAnalysis = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_MYANALYSIS);
    String fromDocBrowser = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.IS_FROM_DOCBROWSER);
    String contextName = ChannelUtilities.getSpagoBIContextName(request);
    String userCanPersist = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.USER_CAN_PERSIST);
	String tablePrefix = (String) aResponseContainer.getServiceResponse().getAttribute(SelfServiceDatasetStartAction.TABLE_NAME_PREFIX);
	String isCkanEnabled = userProfile.getFunctionalities().contains(SpagoBIConstants.CKAN_FUNCTIONALITY) ? "true" : "false";
%>

<script type="text/javascript">



    Ext.onReady(function(){
    	Sbi.settings.mydata.showCkanDataSetFilter = <%=isCkanEnabled%>;
    	Sbi.settings.mydata.showDataSetTab = <%=isMyData%>;
    	Sbi.settings.mydata.showModelsTab = <%=(typeDoc != null && "GEO".equalsIgnoreCase(typeDoc))?false:true%>;
    	Sbi.settings.mydata.showSmartFilterTab = <%=isMyData%>;
		var selfService = Ext.create('Sbi.selfservice.ManageSelfServiceContainer',{
        	worksheetEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(worksheetEditActionUrl) %>'
            , qbeFromBMBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromBMActionUrl) %>'
            , qbeFromDataSetBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditFromDataSetActionUrl) %>'
            , qbeEditDatasetUrl : '<%= StringEscapeUtils.escapeJavaScript(qbeEditDatasetActionUrl) %>'
            , georeportEngineBaseUrl : '<%= StringEscapeUtils.escapeJavaScript(georeportEditActionUrl) %>'
            , user: Sbi.user.userUniqueIdentifier
            , tablePrefix: '<%=tablePrefix%>'            
            , typeDoc: '<%=typeDoc%>'
            , fromMyAnalysis: '<%=fromMyAnalysis%>'
            , userCanPersist: '<%=userCanPersist%>'
            , fromDocBrowser: '<%=fromDocBrowser%>'
            , datasetsServicePath: 'selfservicedataset'
            , contextName: '<%=StringEscapeUtils.escapeJavaScript(contextName)%>'
		}); //by alias
		var datasetListViewport = Ext.create('Ext.container.Viewport', {
			layout:'fit',
	     	items: [selfService]	     	
	    });
    });
	
</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>