<%-- SpagoBI, the Open Source Business Intelligence suite

Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. --%>
 
  
 
<%@page import="it.eng.spagobi.engines.kpi.utils.KpiGUIUtil"%>
<%@page import="org.json.JSONObject, 
				 java.util.ArrayList,
				 java.util.List,
				 org.json.JSONArray,
				 it.eng.spagobi.analiticalmodel.document.handlers.*"%>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collection" %>
<%@page import="it.eng.spago.navigation.LightNavigationManager"%>
<%@page import="java.util.HashMap"%>
<%@page import="java.util.List"%>
<%@page import="java.util.Iterator"%>
<%@page import="org.safehaus.uuid.UUID"%>
<%@page import="org.jfree.chart.entity.StandardEntityCollection"%>
<%@page import="it.eng.spago.error.EMFErrorHandler"%>
<%@page import="java.util.Vector"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.bo.BIObject"%>
<%@page import="org.jfree.data.category.DefaultCategoryDataset"%>
<%@page import="it.eng.spagobi.commons.bo.UserProfile"%>
<%@page import="it.eng.spagobi.engines.kpi.utils.StyleLabel"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.ChartImpl"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiResourceBlock"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiLine"%>
<%@page import="it.eng.spagobi.engines.kpi.bo.KpiLineVisibilityOptions"%>
<%@page import="java.util.Date"%>



<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionManager"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.ExecutionInstance"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.handlers.BIObjectNotesManager"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.analiticalmodel.document.service.ExecuteBIObjectModule"%>
<%@page import="it.eng.spagobi.commons.utilities.ParameterValuesEncoder"%>
<%@page import="it.eng.spagobi.behaviouralmodel.analyticaldriver.bo.BIObjectParameter"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>

<%@ include file="/WEB-INF/jsp/commons/portlet_base311.jsp"%>
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/src/ext/sbi/service/ServiceRegistry.js")%>'></script>


<!--
 STEP 1:
 * Very important fix to display and use Highcharts speedometer in case of IE8 or lower
 * while display and use D3 speedometer for other browsers.
 * See also STEP 2 in Sbi.kpi.KpiGUIDetail js -->

<!--[if lt IE 9]>
<!--  HighCharts -->
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/highcharts.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/highcharts-more.js")%>"></script>
<script type="text/javascript" src="<%=urlBuilder.getResourceLink(request, "js/lib/highcharts-3.0.7/modules/exporting.js")%>"></script>
<![endif]-->
<!--[if !IE]> -->
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/d3/D3.js")%>'></script>
<script type="text/javascript" src='<%=urlBuilder.getResourceLink(request, "/js/lib/d3/D3.layout.js")%>'></script>

<!-- <![endif]-->


<LINK rel='StyleSheet' 
      href='<%=urlBuilder.getResourceLinkByTheme(request, "css/kpi/kpi.css",currTheme)%>' 
      type='text/css' />

<%	//START ADDING TITLE AND SUBTITLE

	List resources = new ArrayList();
	
	SourceBean sbModuleResponse = (SourceBean) aServiceResponse.getAttribute("ExecuteBIObjectModule");
	Integer executionAuditId_chart = null;
		EMFErrorHandler errorHandler=aResponseContainer.getErrorHandler();
	if(errorHandler.isOK()){    
		SessionContainer permSession = aSessionContainer.getPermanentContainer();
	
		if(userProfile==null){
			userProfile = (IEngUserProfile) permSession.getAttribute(IEngUserProfile.ENG_USER_PROFILE);
			userId=(String)((UserProfile)userProfile).getUserId();
		}
	}
	String crossNavigationUrl = "";
	ExecutionInstance instanceO = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
	String execContext = instanceO.getExecutionModality();
	String title = (String)sbModuleResponse.getAttribute("title");
	String subTitle = (String)sbModuleResponse.getAttribute("subName");

	//END ADDING TITLE AND SUBTITLE

	String metadata_publisher_Name =(String)sbModuleResponse.getAttribute("metadata_publisher_Name");
	String trend_publisher_Name =(String)sbModuleResponse.getAttribute("trend_publisher_Name");
	String customChartName =(String)sbModuleResponse.getAttribute("custom_chart_name");

	List kpiRBlocks =(List)sbModuleResponse.getAttribute("kpiRBlocks");

	String tickInterval =(String)sbModuleResponse.getAttribute("tickInterval");
	
	//START creating resources list
	if(!kpiRBlocks.isEmpty()){
		Iterator blocksIt = kpiRBlocks.iterator();
		while(blocksIt.hasNext()){
			KpiResourceBlock block = (KpiResourceBlock) blocksIt.next();
			if(block.getR()!=null){
				resources.add( block.getR());
			}
		}
	}
	
	ExecutionInstance instance = contextManager.getExecutionInstance(ExecutionInstance.class.getName());
	String EXECUTION_ID = instance.getExecutionId();
	//filter on resources if selected through AD ParKpiResource or ParKpiResources
	ArrayList parKpiResource = new ArrayList();
	ArrayList parKpiResources = new ArrayList();
	String parKpiDateStr = "";
	String parsToDetailDocs = "";
	   if(instance!=null && instance.getBIObject()!=null){
	   List pars = instance.getBIObject().getBiObjectParameters();			
		if(pars!=null && !pars.isEmpty()){
			Iterator ite=pars.iterator();
			while(ite.hasNext()){
				BIObjectParameter p = (BIObjectParameter)ite.next();
				String url = p.getParameterUrlName();

				String value = p.getParameterValuesAsString();
				if(value != null && !value.equals("null")){
					if(url.equals("ParKpiResource")){
						parKpiResource.add(value);
					}else if(url.equals("ParKpiResources")){
						parKpiResources.add(value);
					}else if(url.equals("ParKpiDate")){					
						parKpiDateStr = "'"+value+"'";
					}
				}
				
			}		
		}
	}

	
	JSONArray kpiRowsArray = new JSONArray();
	KpiGUIUtil util = new KpiGUIUtil();
	util.setExecutionInstance(instance, locale);

	if(!kpiRBlocks.isEmpty()){		
		
		Iterator blocksIt = kpiRBlocks.iterator();

		while(blocksIt.hasNext()){			
			KpiResourceBlock block = (KpiResourceBlock) blocksIt.next();
			String resourceName = null;
			KpiLine root = block.getRoot();
			Integer id =null;
			if(block.getR() != null){
				resourceName = block.getR().getName();
				id = block.getR().getId();
			}
			if((parKpiResource.isEmpty() && parKpiResources.isEmpty())
						|| (!parKpiResource.isEmpty() && parKpiResource.contains(resourceName))
						|| (!parKpiResources.isEmpty() && parKpiResources.contains(id+""))){
				
				JSONObject modelInstJson =  util.recursiveGetJsonObject(root);
				modelInstJson.put("resourceName", resourceName);
				kpiRowsArray.put(modelInstJson);
			}

				
		}			
	}
	SessionContainer permSession = aSessionContainer.getPermanentContainer();
	String localeExtDateFormat = GeneralUtilities.getLocaleDateFormatForExtJs(permSession);
	String serverExtTimestampFormat = GeneralUtilities.getServerTimestampFormatExtJs();
	String serverDateFormat = GeneralUtilities.getServerDateFormatExtJs();
	String engineContext = request.getContextPath();
    if( engineContext.startsWith("/") || engineContext.startsWith("\\") ) {
    	engineContext = request.getContextPath().substring(1);
    }
	//determines execution instance for each detail document

	//determines functionalities for the user:
	Collection functionalities = userProfile.getFunctionalities();
	String canDeleteComments ="false";
	if(functionalities.contains(SpagoBIConstants.KPI_COMMENT_DELETE)){
		canDeleteComments ="true";
	}
	String canEditPersonalComments ="false";
	if(functionalities.contains(SpagoBIConstants.KPI_COMMENT_EDIT_MY)){
		canEditPersonalComments ="true";
	}
	String canEditAllComments ="false";
	if(functionalities.contains(SpagoBIConstants.KPI_COMMENT_EDIT_ALL)){
		canEditAllComments ="true";
	}
%>		
<script type="text/javascript">
		var url = {
			host: 'localhost'
			, port: '8080'
			, contextPath: 'SpagoBI'
			

		};
		
		Sbi.config.serviceRegistry = new Sbi.service.ServiceRegistry({
			baseUrl: url
			
		});
		
		var grid = {
			subtitle: '<%= subTitle%>',		
			autoScroll	:true,
			autoHeight : true,
			//autoWidth: true,
			border: false,
			json: <%=kpiRowsArray%>
		};
		var dt ='';
		<%
		if(parKpiDateStr!= null && !parKpiDateStr.equals("")){
		%>

			dt = <%=parKpiDateStr%>;

		<%
		}else{
		%>
			dt = Sbi.commons.Format.date(new Date(), Sbi.config.clientServerDateFormat);
		<% 
		}
		%>
		var accordion ={SBI_EXECUTION_ID: '<%=EXECUTION_ID%>', 
						customChartName: '<%=customChartName%>',
						localeExtDateFormat: '<%=localeExtDateFormat%>',
						serverExtTimestampFormat: '<%=serverExtTimestampFormat%>',
						serverDateFormat: '<%=serverDateFormat%>',
						chartBaseUrl: '/<%= engineContext %>/js/lib/ext-3.1.1/resources/charts.swf',
						titleDate: dt+' ',
						tickInterval: <%=tickInterval%>,
						canDelete: <%=canDeleteComments%>,
						canEditPersonal: <%=canEditPersonalComments%>,
						canEditAll: <%=canEditAllComments%>,
						loggedUser: '<%=userId%>'
						};
		
		var config ={grid: grid, accordion: accordion};

		Ext.onReady(function(){

			var item = new Sbi.kpi.KpiGUILayout(config);

		    var viewport = new Ext.Viewport({
		        layout:'fit',		        
		        items:[item]
		    });

		});

</script>

<span id="chartContainer" style="border:1px solid red;"></span>

<%@ include file="/WEB-INF/jsp/commons/footer.jsp"%>