<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
<%-- 
author: Marco Cortella (marco.cortella@eng.it)
--%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="it.eng.spagobi.profiling.bean.SbiAttribute"%>
<%@page import="it.eng.spagobi.services.common.SsoServiceInterface"%>
<%@page import="it.eng.spagobi.commons.bo.Domain,
				 it.eng.spagobi.tools.datasource.bo.*,
				 java.util.ArrayList,
				 java.util.List,
				 org.json.JSONArray" %>
<%@page import="it.eng.spagobi.tools.udp.bo.Udp"%>
<%@page import="it.eng.spagobi.commons.serializer.UdpJSONSerializer"%>
<%@page import="org.json.JSONObject"%>
<%@page import="it.eng.spagobi.engines.drivers.qbe.QbeDriver"%>
<%@page import="it.eng.spagobi.engines.config.bo.Engine"%>
<%@page import="it.eng.spagobi.commons.dao.DAOFactory"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<%
	String[] fileNames = (String[])aSessionContainer.getAttribute("fileNames");
    List<Domain> datasetTypes = (List<Domain>) aSessionContainer.getAttribute("dsTypesList");
    List<SbiAttribute> profileAttributes = (List<SbiAttribute>) aSessionContainer.getAttribute("sbiAttrsList");
    
    List catTypesCd = (List) aSessionContainer.getAttribute("catTypesList");
    List dataSourceList = (List) aSessionContainer.getAttribute("dataSourceList");
    List scriptLanguageList = (List) aSessionContainer.getAttribute("scriptLanguageList");
    List trasfTypesList = (List) aSessionContainer.getAttribute("trasfTypesList");
  
	JSONArray empty = new JSONArray();
	empty.put("");
	
	// serialize fileNames
	String serializedFileNames = null;
	JSONArray fileNamesArray = new JSONArray();
	if(fileNames != null){
		for(int i=0; i< fileNames.length ; i++){
			String filename = fileNames[i];
			JSONArray temp = new JSONArray();
			temp.put(filename);
			fileNamesArray.put(temp);
		}
	}	
	serializedFileNames = fileNamesArray.toString();
	serializedFileNames = serializedFileNames.replaceAll("\"","'");
	
	// serialize datasetTypes
	String serializedDatasetTypes = null;
	JSONArray datasetTypesArray = new JSONArray();
	if(datasetTypes != null){
		for(Domain datasetType : datasetTypes){
			JSONArray temp = new JSONArray();
			temp.put(datasetType.getValueCd());
			datasetTypesArray.put(temp);
		}
	}	
	serializedDatasetTypes = datasetTypesArray.toString();
	serializedDatasetTypes = serializedDatasetTypes.replaceAll("\"","'");
	
	//  serialize profile attributes
	String serializedProfileAttributes = null;
	JSONArray profAttrsArray = new JSONArray();
	if(profileAttributes != null){
		for(SbiAttribute profileAttribute : profileAttributes){
			JSONArray temp = new JSONArray();
			temp.put(profileAttribute.getAttributeName());
			profAttrsArray.put(temp);
		}
	}	
	serializedProfileAttributes = profAttrsArray.toString();
	serializedProfileAttributes = serializedProfileAttributes.replaceAll("\"","'");
	
	//  serialize category type
	JSONArray catTypesArray = new JSONArray();
	catTypesArray.put(empty);
	if(catTypesCd != null){
		for(int i=0; i< catTypesCd.size(); i++){
			Domain domain = (Domain)catTypesCd.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueName());
			catTypesArray.put(temp);
		}
	}	
	String catTypes = catTypesArray.toString();
	catTypes = catTypes.replaceAll("\"","'");
	
	//  serialize datasource
	JSONArray dataSourcesArray = new JSONArray();
	if(dataSourceList != null){
		for(int i=0; i< dataSourceList.size(); i++){
			DataSource datasource = (DataSource)dataSourceList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(datasource.getLabel());
			temp.put(datasource.getDescr());
			dataSourcesArray.put(temp);
		}
	}	
	String dataSourceLabels = dataSourcesArray.toString();
	dataSourceLabels = dataSourceLabels.replaceAll("\"","'");
	
	//  serialize script language
	JSONArray scriptLanguagesArray = new JSONArray();
	if(scriptLanguageList != null){
		for(int i=0; i< scriptLanguageList.size(); i++){
			Domain domain = (Domain)scriptLanguageList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			temp.put(domain.getValueName());
			scriptLanguagesArray.put(temp);
		}
	}	
	String scriptTypes = scriptLanguagesArray.toString();
	scriptTypes = scriptTypes.replaceAll("\"","'");
	
	//  serialize transformation array
	JSONArray trasfTypesArray = new JSONArray();
	trasfTypesArray.put(empty);
	if(trasfTypesList != null){
		for(int i=0; i< trasfTypesList.size(); i++){
			Domain domain = (Domain)trasfTypesList.get(i);
			JSONArray temp = new JSONArray();
			temp.put(domain.getValueCd());
			trasfTypesArray.put(temp);
		}
	}	
	String trasfTypes = trasfTypesArray.toString();
	trasfTypes = trasfTypes.replaceAll("\"","'");
	
	
	String qbeEngineBaseUrl = null;
	StringBuffer qbeEngineBuildDatasetUrl = new StringBuffer();
	StringBuffer qbeEngineGetDatamartsUrl = new StringBuffer();
	List engines = DAOFactory.getEngineDAO().loadAllEngines();
	Iterator it = engines.iterator();
	while (it.hasNext()) {
		Engine engine = (Engine) it.next();
		String driver = engine.getDriverName();
		if (driver != null && driver.equals(QbeDriver.class.getName())) {
			qbeEngineBaseUrl = engine.getUrl();
			qbeEngineBuildDatasetUrl.append(qbeEngineBaseUrl);
			qbeEngineGetDatamartsUrl.append(qbeEngineBaseUrl);
			break;
		}
	}
	qbeEngineBuildDatasetUrl.append("?ACTION_NAME=BUILD_QBE_DATASET_START_ACTION");
	qbeEngineGetDatamartsUrl.append("?ACTION_NAME=GET_DATAMARTS_NAMES");
	if (!GeneralUtilities.isSSOEnabled()) {
		qbeEngineBuildDatasetUrl.append("&" + SsoServiceInterface.USER_ID + "=" + userUniqueIdentifier);
		qbeEngineGetDatamartsUrl.append("&" + SsoServiceInterface.USER_ID + "=" + userUniqueIdentifier);
	}
	qbeEngineBuildDatasetUrl.append("&" + QbeDriver.PARAM_NEW_SESSION + "=TRUE");
	qbeEngineGetDatamartsUrl.append("&" + QbeDriver.PARAM_NEW_SESSION + "=TRUE");
	qbeEngineBuildDatasetUrl.append("&" + SpagoBIConstants.SBI_LANGUAGE + "=" + curr_language);
	qbeEngineBuildDatasetUrl.append("&" + SpagoBIConstants.SBI_COUNTRY + "=" + curr_country);
%>


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVASCRIPT IMPORTS														--%>
<%-- ---------------------------------------------------------------------- --%>
<!--  
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>
-->

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVASCRIPT CODE														--%>
<%-- ---------------------------------------------------------------------- --%>
<script type="text/javascript">

    var config = {};  
    config.dsTypes = <%= serializedDatasetTypes%>;
    config.catTypeVn = <%= catTypes%>;
    config.dataSourceLabels = <%= dataSourceLabels%>;
    config.scriptTypes = <%= scriptTypes%>;
    config.trasfTypes = <%= trasfTypes%>;    
    config.attrs = <%= serializedProfileAttributes%>;  
    config.fileTypes = <%= serializedFileNames%>;  
	
	var url = {
    	host: '<%= request.getServerName()%>'
    	, port: '<%= request.getServerPort()%>'
    	, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?
    	   				  request.getContextPath().substring(1):
    	   				  request.getContextPath()%>'
    	    
    };

    Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
    	baseUrl: url
    });
    
    Sbi.config.qbeDatasetBuildUrl = '<%= StringEscapeUtils.escapeJavaScript(qbeEngineBuildDatasetUrl.toString()) %>';
    Sbi.config.qbeGetDatamartsUrl = '<%= StringEscapeUtils.escapeJavaScript(qbeEngineGetDatamartsUrl.toString()) %>';

    // for DataStorePanel.js
    Sbi.config.queryLimit = {};
    Sbi.config.queryLimit.maxRecords = <%= GeneralUtilities.getDatasetMaxResults() %>;
    
	Ext.onReady(function(){
		Ext.QuickTips.init();
		var manageDatasets = new Sbi.engines.EngineManagementPanel(config);
		var viewport = new Ext.Viewport({
			layout: 'border'
			, items: [
			    {
			       region: 'center',
			       layout: 'fit',
			       items: [manageDatasets]
			    }
			]
	
		});
	   	
	});


</script>


<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>