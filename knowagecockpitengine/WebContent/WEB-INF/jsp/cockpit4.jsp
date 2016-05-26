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


<%-- 
author: Andrea Gioia (andrea.gioia@eng.it)
--%>

<%@page import="it.eng.spago.base.SourceBean"%>
<%@page import="it.eng.spagobi.services.common.EnginConf"%>
<%@page import="org.apache.commons.lang.StringEscapeUtils"%>
<%@page import="java.util.HashMap"%>
<%@ page language="java" 
	     contentType="text/html; charset=UTF-8" 
	     pageEncoding="UTF-8"%>	


<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS															--%>
<%-- ---------------------------------------------------------------------- --%>
<%@page import="java.util.Locale"%>
<%@page import="java.util.List"%>
<%@page import="java.util.ArrayList"%>
<%@page import="java.util.Map"%>
<%@page import="it.eng.spagobi.engine.cockpit.CockpitEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="it.eng.spagobi.commons.utilities.GeneralUtilities"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 
	CockpitEngineInstance engineInstance;
	IEngUserProfile profile;
	String profileJSONStr;
	Map env;
	String contextName;
	String environment;
	String executionRole;
	Locale locale;
	String template;
	String docLabel;
	String docVersion;
	String docAuthor;
	String docName;
	String docDescription;
	String docIsPublic;
	String docIsVisible;
	String docPreviewFile;
	String[] docCommunities;
	String docCommunity;
	List docFunctionalities;
	String userId;
	String isTechnicalUser;
	List<String> includes;
	
	String documentMode;

	engineInstance = (CockpitEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	locale = engineInstance.getLocale();
	
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString();
	docLabel = (engineInstance.getDocumentLabel()==null)?"":engineInstance.getDocumentLabel().toString();
	docVersion = (engineInstance.getDocumentVersion()==null)?"":engineInstance.getDocumentVersion().toString();
	docAuthor = (engineInstance.getDocumentAuthor()==null)?"":engineInstance.getDocumentAuthor().toString();
	docName = (engineInstance.getDocumentName()==null)?"":engineInstance.getDocumentName().toString();
	docDescription = (engineInstance.getDocumentDescription()==null)?"":engineInstance.getDocumentDescription().toString();
	docIsPublic= (engineInstance.getDocumentIsPublic()==null)?"":engineInstance.getDocumentIsPublic().toString();
	docIsVisible= (engineInstance.getDocumentIsVisible()==null)?"":engineInstance.getDocumentIsVisible().toString();
	docPreviewFile= (engineInstance.getDocumentPreviewFile()==null)?"":engineInstance.getDocumentPreviewFile().toString();	
	docCommunities= (engineInstance.getDocumentCommunities()==null)?null:engineInstance.getDocumentCommunities();
	docCommunity = (docCommunities == null || docCommunities.length == 0) ? "": docCommunities[0];
	docFunctionalities= (engineInstance.getDocumentFunctionalities()==null)?new ArrayList():engineInstance.getDocumentFunctionalities();
	
	/*
		WORKAROUND: Replace the single quote character wherever in the chart template with the ASCII code for a single quote character, so we can render the chart 
		inside the Cockpit engine even when the JSON template contains	this character (e.g. "L'Italia"). Later, because of rendering the chart, this code
		will be replaced with the "escaped" single quote character combination (in order not to have "L&#39;Italia").
		@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	*/
	template = template.replaceAll("'","&#39;");
	
	documentMode = (request.getParameter("documentMode")==null)?"VIEW":request.getParameter("documentMode");
	
	boolean showAddChart = profile.isAbleToExecuteAction(SpagoBIConstants.MANAGE_CHART_WIDGET);
	boolean showAddStaticWidgets = profile.isAbleToExecuteAction(SpagoBIConstants.MANAGE_STATIC_WIDGET);
	boolean showAddAnalytical = profile.isAbleToExecuteAction(SpagoBIConstants.MANAGE_ANALYTICAL_WIDGET);
	boolean showMultiSheet = profile.isAbleToExecuteAction(SpagoBIConstants.MANAGE_MULTISHEET_COCKPIT);
	
	boolean forceIE8Compatibility = false;
	
	boolean fromMyAnalysis = false;
	if(request.getParameter("MYANALYSIS") != null && request.getParameter("MYANALYSIS").equalsIgnoreCase("TRUE")){
		fromMyAnalysis = true;
	}else{
		if (request.getParameter("SBI_ENVIRONMENT") != null && request.getParameter("SBI_ENVIRONMENT").equalsIgnoreCase("MYANALYSIS")){
			fromMyAnalysis = true;
		}
	}
	
    Map analyticalDrivers  = engineInstance.getAnalyticalDrivers();
    
    String param2="?"+SpagoBIConstants.SBI_CONTEXT+"="+contextName;
	String host=request.getServerName();
	String param3="&"+SpagoBIConstants.SBI_HOST+"="+host;
// 	SourceBean sb = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("AthenaChartEngineContextName"));
	SourceBean sb = ((SourceBean) EnginConf.getInstance().getConfig().getAttribute("ChartEngineContextName"));
	String chartEngineContextName = sb.getCharacters();
    
    StringBuffer chartDesignerUrlTemp = new StringBuffer("/"+chartEngineContextName+"/api/1.0/pages/edit_cockpit");
    StringBuffer chartRuntimeUrlTemp = new StringBuffer("/"+chartEngineContextName+"/api/1.0/pages/execute_cockpit");
    
    chartDesignerUrlTemp.append(param2);
    chartRuntimeUrlTemp.append(param2);
    
    chartDesignerUrlTemp.append(param3);
    chartRuntimeUrlTemp.append(param3);
    
    chartDesignerUrlTemp.append("&"+SpagoBIConstants.SBI_LANGUAGE+"="+locale.getLanguage());
    chartRuntimeUrlTemp.append("&"+SpagoBIConstants.SBI_LANGUAGE+"="+locale.getLanguage());
    
    chartDesignerUrlTemp.append("&"+SpagoBIConstants.SBI_COUNTRY+"="+locale.getCountry());
    chartRuntimeUrlTemp.append("&"+SpagoBIConstants.SBI_COUNTRY+"="+locale.getCountry());
    
    //chartDesignerUrlTemp.append("&document=159");
    
    Map testMap = new HashMap();
    testMap.put("user_id", userId);
    
    
    String chartDesignerUrl = StringEscapeUtils.escapeJavaScript(GeneralUtilities.getUrl(chartDesignerUrlTemp.toString(), testMap));
    String chartRuntimeUrl = StringEscapeUtils.escapeJavaScript(GeneralUtilities.getUrl(chartRuntimeUrlTemp.toString(), testMap));

    
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
	   <title><%=docName.trim().length() > 0? docName: "KnowageCockpitEngine"%></title>
       <meta http-equiv="X-UA-Compatible" content="IE=edge" />
       
        <%@include file="commons/includeExtJS4.jspf" %>
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeSpagoBICockpitJS4.jspf" %>
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
    
	
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">

		var template = <%= template %>;

		Sbi.user = <%= profileJSONStr %>;
		
		Sbi.config = {};
		var url = {
			protocol: '<%= request.getScheme()%>'   
		    , host: '<%= request.getServerName()%>'
		    , port: '<%= request.getServerPort()%>'
		    , contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
		    , controllerPath: null // no cotroller just servlets   
		};
		
		var executionContext = {};
        <% 
        Iterator it = analyticalDrivers.keySet().iterator();        
		while(it.hasNext()) {
			String parameterName = (String)it.next();
			String parameterValue = (String)analyticalDrivers.get(parameterName);		
			String quote = (parameterValue.startsWith("'"))? "" : "'";
			if ( parameterValue.indexOf(",") >= 0){					
		 %>
				executionContext ['<%=parameterName%>'] = [<%=quote%><%=parameterValue%><%=quote%>];
		<%	}else{
		%>
				executionContext ['<%=parameterName%>'] = <%=quote%><%=parameterValue%><%=quote%>;
		 <%
		 	}							
		 }
        %>
        Sbi.config.executionContext = executionContext;
	
		var params = {
				SBI_EXECUTION_ID: <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>
				, user_id: "<%=userId%>"
		};
	
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
		  	baseUrl: url
		  , baseParams: params
		});
		
				
		// test
		Sbi.config.serviceReg = new Sbi.service.ServiceReg();
		
		Sbi.config.serviceReg.addServiceBaseConf('cockpitServiceConf', {
			method: "GET"
			
			, baseUrlConf: {
				protocol: '<%= request.getScheme()%>'     
				, host: '<%= request.getServerName()%>'
				, port: '<%= request.getServerPort()%>'
				, contextPath: '<%= request.getContextPath().startsWith("/")||request.getContextPath().startsWith("\\")?request.getContextPath().substring(1): request.getContextPath()%>'
			}
			, controllerConf: {
				controllerPath: 'api'   
				, serviceVersion: '1.0'
				, serviceVersionParamType: 'path' 
			}
		
			, basePathParams:{}
			, baseQueryParams: params
			, baseFormParams: {}
	
			//, absolute: false
		});
		
		Sbi.config.serviceReg.addServiceBaseConf('spagobiServiceConf', {
			method: "GET"
			
			, baseUrlConf: {
				protocol: '<%= request.getScheme()%>'     
				, host: '<%= request.getServerName()%>'
				, port: '<%= request.getServerPort()%>'
				, contextPath:  Sbi.mainContextName
			}
			, controllerConf: {
				controllerPath: 'restful-services'   
				, serviceVersion: '1.0'
				, serviceVersionParamType: 'path' 
			}
		
			, basePathParams:{}
			, baseQueryParams: params
			, baseFormParams: {}
	
			//, absolute: false
		});
		
		Sbi.config.serviceReg.addServiceBaseConf('v2/spagobiServiceConf', {
			method: "GET"
			
			, baseUrlConf: {
				protocol: '<%= request.getScheme()%>'     
				, host: '<%= request.getServerName()%>'
				, port: '<%= request.getServerPort()%>'
				, contextPath:  Sbi.mainContextName
			}
			, controllerConf: {
				controllerPath: 'restful-services'   
				, serviceVersion: '2.0'
				, serviceVersionParamType: 'path' 
			}
		
			, basePathParams:{}
			, baseQueryParams: params
			, baseFormParams: {}
	
			//, absolute: false
		});
	
		Sbi.config.serviceReg.registerService('loadDataSetStore', {
			name: 'loadDataSetStore'
			, description: 'Load the store of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/data'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('v2/loadDataSetStore', {
			name: 'loadDataSetStore'
			, description: 'Load the store of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/data'
		}, 'v2/spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('v2/loadDataSetStorePost', {
			name: 'loadDataSetStorePost'
			, description: 'Load the store of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/data'
			, method: 'POST'
		}, 'v2/spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadChartDataSetStore', {
			name: 'loadChartDataSetStore'
			, description: 'Load the store of the specified dataset for a chart widget'
			, resourcePath: 'datasets/{datasetLabel}/chartData'
			, method: 'POST'
		}, 'spagobiServiceConf');
		
	    Sbi.config.serviceReg.registerService('cleanCache', {
	            name: 'cleanCache'
	            , description: 'clean the cache of used datasets'
	            , resourcePath: 'cache/{datasetLabels}/cleanCache'
	            , method: 'DELETE'
	        }, 'spagobiServiceConf');
	    
	   Sbi.config.serviceReg.registerService('checkAssociation', {
               name: 'checkAssociation'
               , description: 'check if association is valid'
               , resourcePath: 'datasets/{association}/checkAssociation'
               , method: 'POST'
           }, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadJoinedDataSetStore', {
			name: 'loadJoinedDataSetStore'
			, description: 'Load the store of the specified joined dataset'
			, resourcePath: 'datasets/joined/data'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDataSetField', {
			name: 'loadDataSetField'
			, description: 'Load all the fields of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/fields'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDataSetParams', {
			name: 'loadDataSetParams'
			, description: 'Load all the params of the specified dataset'
			, resourcePath: 'datasets/{datasetLabel}/parameters'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadEnterpriseDataSets', {
			name: 'loadEnterpriseDatasets'
			, description: 'Load all enterprise datasets'
			, resourcePath: 'datasets/enterprise'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadOwnedDataSets', {
			name: 'loadOwnedDataSets'
			, description: 'Load all datasets owned by the user'
			, resourcePath: 'datasets/owned'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadSharedDataSets', {
			name: 'loadSharedDataSets'
			, description: 'Load all datasets shared by other users (eneterprise and owned datsets are not included)'
			, resourcePath: 'datasets/shared'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadMyDataDataSets', {
			name: 'loadMyDataDataSets'
			, description: 'Load all datasets visible to the user in MyData panel (i.e. owned + shared + enterprise)'
			, resourcePath: 'datasets/mydata'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('loadDocumentParams', {
			name: 'loadDocumentParams'
			, description: 'Load all the params of the specified document'
			, resourcePath: 'documents/{documentLabel}/parameters'
		}, 'spagobiServiceConf');
		
		Sbi.config.serviceReg.registerService('setAssociations', {
			name: 'setAssociations'
			, description: 'Set the associations'
			, resourcePath: 'associations'
			, method: 'POST'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('getCrosstab', {
			name: 'getCrosstab'
			, description: 'Get the crosstab in HTML'
			, resourcePath: 'crosstab'
			, method: 'GET'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('updateCrosstab', {
			name: 'updateCrosstab'
			, description: 'Update the crosstab in HTML'
			, resourcePath: 'crosstab/update'
			, method: 'GET'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('setCrosstabSort', {
			name: 'setCrosstabSort'
			, description: 'Get the crosstab in HTML'
			, resourcePath: 'crosstab/sort'
			, method: 'POST'
		}, 'cockpitServiceConf');
		
		Sbi.config.serviceReg.registerService('v2/loadAssociativeSelections', {
			name: 'loadAssociativeSelections'
			, description: 'Load associative selections'
			, resourcePath: 'datasets/loadAssociativeSelections'
		}, 'v2/spagobiServiceConf');
	
		/*
		var testUrl = service.getServiceUrl({pathParams: {datasetLabel: 'ds__405004519'}, queryParams: {frutto: "mela"}});
		alert(testUrl);
		service.on('request', function(service, response, options){alert('Called service [' + service.name + ']');}, this);
		service.doRequest({pathParams: {datasetLabel: 'ds__405004519'}, queryParams: {frutto: "mela"}});
		Sbi.config.serviceReg.callService('loadDataSetField', {
			pathParams: {datasetLabel: 'ds__405004519'}
			, queryParams: {frutto: "mela"}
			, success: function(response) {alert(response.responseText);}
			, failure: function(response) {alert(response.responseText);}
			, scope: this
		});
		*/
		
		// test
		
		Sbi.storeManager = new Sbi.data.StoreManager({storesConf: template.storesConf, template: template});
		//Sbi.storeManager.setConfiguration(template.storesConf);
		
		Sbi.config.docLabel ="<%=docLabel%>";
		Sbi.config.docVersion = "<%=docVersion%>";
		Sbi.config.userId = "<%=userId%>";
		Sbi.config.isTechnicalUser = "<%=isTechnicalUser%>";
		Sbi.config.docAuthor = "<%=docAuthor%>";
		Sbi.config.docName = "<%=docName.replace('\n', ' ')%>";
		Sbi.config.docDescription = "<%=docDescription.replace("\n", " ").replace("\"", "\\\"").replace("\'", "\\\'")%>";
		Sbi.config.docIsPublic= "<%=docIsPublic%>";
		Sbi.config.docIsVisible= "<%=docIsVisible%>";
		Sbi.config.docPreviewFile= "<%=docPreviewFile%>";
		Sbi.config.docCommunities= "<%=docCommunity%>";
		Sbi.config.docFunctionalities= <%=docFunctionalities%>;		
		Sbi.config.SBI_EXECUTION_ID= <%= request.getParameter("SBI_EXECUTION_ID")!=null?"'" + request.getParameter("SBI_EXECUTION_ID") +"'": "null" %>;
		Sbi.config.fromMyAnalysis = <%=fromMyAnalysis%>;
		Sbi.config.environment = "<%=environment%>";
		Sbi.config.contextName =  '<%= contextName %>';
		Sbi.config.documentMode = "<%=documentMode%>";
		Sbi.config.chartDesignerUrl = "<%=chartDesignerUrl%>";
		Sbi.config.chartRuntimeUrl = "<%=chartRuntimeUrl%>";
		Sbi.config.currentRole = "<%=executionRole%>";
		Sbi.config.visibiltyButtons = {
				showAddChart : <%= showAddChart %>,
				showAddStaticWidgets : <%= showAddStaticWidgets %>,
				showAddAnalytical : <%= showAddAnalytical %>,
				showMultiSheet :  <%= showMultiSheet %>
		};
		
		var cockpitPanel = null;
		    
		Ext.onReady(function(){
					
			Ext.QuickTips.init();   
				
			cockpitPanel = new Sbi.cockpit.MainPanel({analysisState: template});	
				
			var viewport = new Ext.Viewport({
				id:    'view',
		   		layout: 'fit',
		        items: [cockpitPanel]
			});
		});
	
	</script>
	
	</body>

</html>

