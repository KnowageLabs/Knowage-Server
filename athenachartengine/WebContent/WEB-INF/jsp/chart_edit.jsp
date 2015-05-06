<%-- SpagoBI, the Open Source Business Intelligence suite

 Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 This Source Code Form is subject to the terms of the Mozilla Public
 License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.  If a copy of the MPL was not distributed with this file,
 You can obtain one at http://mozilla.org/MPL/2.0/. --%>

<%-- 
author: 
--%>

<%@page import="it.eng.spagobi.engine.chart.model.conf.ChartConfig"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineConfig"%>
<%@page import="it.eng.spagobi.engine.util.ChartEngineUtil"%>
<%@page import="it.eng.spagobi.engine.chart.ChartEngineInstance"%>
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
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="it.eng.spago.security.IEngUserProfile"%>
<%@page import="it.eng.spagobi.commons.utilities.ChannelUtilities"%>
<%@page import="it.eng.spagobi.commons.constants.SpagoBIConstants"%>
<%@page import="java.util.Iterator"%>
<%@page import="com.fasterxml.jackson.databind.ObjectMapper"%>
<%@page import="org.json.JSONObject"%>
<%@page import="org.json.XML"%>

<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA CODE 																--%>
<%-- ---------------------------------------------------------------------- --%>
<% 
	ChartEngineInstance engineInstance;
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
	String datasetLabel;

	engineInstance = (ChartEngineInstance)request.getSession().getAttribute(EngineConstants.ENGINE_INSTANCE);
	env = engineInstance.getEnv();
	profile = engineInstance.getUserProfile();
	profileJSONStr = new ObjectMapper().writeValueAsString(profile);
	locale = engineInstance.getLocale();
	
	datasetLabel = engineInstance.getDataSet().getLabel();
	contextName = request.getParameter(SpagoBIConstants.SBI_CONTEXT); 
	environment = request.getParameter("SBI_ENVIRONMENT"); 
	executionRole = (String)env.get(EngineConstants.ENV_EXECUTION_ROLE);
	userId = (engineInstance.getDocumentUser()==null)?"":engineInstance.getDocumentUser().toString();
	isTechnicalUser = (engineInstance.isTechnicalUser()==null)?"":engineInstance.isTechnicalUser().toString();
	template = engineInstance.getTemplate().toString(0);
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
    
%>

<%-- ---------------------------------------------------------------------- --%>
<%-- HTML	 																--%>
<%-- ---------------------------------------------------------------------- --%>
<html>
	<%-- == HEAD ========================================================== --%>
	<head>
	   <title><%=docName.trim().length() > 0? docName: "AthenaChartEngine"%></title>
       <meta http-equiv="X-UA-Compatible" content="IE=edge" />
       
        <%@include file="commons/includeExtJS5.jspf" %>
        
		<%@include file="commons/includeMessageResource.jspf" %>
		<%@include file="commons/includeAthenaChartEngineJS5.jspf" %>
		
    </head>
	
	<%-- == BODY ========================================================== --%>
    
    <body>
	
	    
	<%-- == JAVASCRIPTS  ===================================================== --%>
	<script language="javascript" type="text/javascript">
 		Ext.onReady(function(){
 			Ext.log({level: 'info'}, 'CHART: IN');
 			Ext.Loader.setPath('Sbi.chart', '/athenachartengine/js/src/ext5/sbi/chart');

 			<%-- TODO questo dovrà essere il pannello dell'anteprima --%>
 			<%-- 
 			var mainPanel = Ext.create('Ext.panel.Panel', {
 				id: 'mainPanel',
 				width: '100%',
 			    height: '100%',
 			    renderTo: Ext.getBody()
 			});
 			
  			initChartLibrary(mainPanel.id);
  			
  			--%>
  			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
 			var userId = '<%=userId%>';
 			var hostName = '<%=request.getServerName()%>';
 			var serverPort = '<%=request.getServerPort()%>';
 			var jsonTemplate = '<%=template%>';
 			var datasetLabel  = '<%=datasetLabel%>';
  			
 			var coreServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getCoreWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
 			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
 			
  			function addToAxisesContainer(id) {
  				var panel = Ext.getCmp(id);
  				var newPanel = createChartColumnsContainer();
  				panel.add(newPanel);
  			}

  			function createChartColumnsContainer(id) {
  				var chartColumnsContainer = Ext.create("Sbi.chart.designer.ChartColumnsContainer", {
  					id: (id && id != '')? id: 'ChartColumnsContainer_' + ChartColumnsContainer.idseed++,
  					flex: 1,
  					viewConfig: {
  						plugins: {
  							ptype: 'gridviewdragdrop',
  							containerScroll: true,
  							dragGroup: ddGroup1,
  							dropGroup: ddGroup1
  						},
  						listeners: {
  							beforeDrop: function(node, data, dropRec, dropPosition) {
  								if(data.view.id != this.id) {
  									data.records[0] = data.records[0].copy('id' + ChartColumnsContainer.idseed++);   
  								} 
  							}
  						}
  					},
  					store: Ext.create('Sbi.chart.designer.AxisesContainerStore'),
  					columns: [
  						{
  							text: 'Custom name (Y)',
  							dataIndex: 'axisName',
  							flex: 12,
  							sortable: false,
  						},
  						{
  							text: '',
  							dataIndex: 'groupingFunction',
  							flex: 8,
  							xtype: 'widgetcolumn',
  							widget: {
  								xtype: 'combo',
  								store: [
  									'AVG',
  									'COUNT',
  									'MAX',
  									'MIN',
  									'SUM'
  								],
  								listeners: {
  									select: function(combo, data, others){
  										console.log('combo.getValue() ' , combo.getValue());
  										console.log('data ' , data);
  									}
  								}
  							}
  						}, 
  						{
  							menuDisabled: true,
  							sortable: false,
  							flex: 1,
  							xtype: 'actioncolumn',
  							items: [{
  								icon: 'http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/extjs-build/examples/restful/images/delete.png',
  								tooltip: 'Remove column',
  								handler: function(grid, rowIndex, colIndex) {
  									var rec = grid.getStore().removeAt(rowIndex);

  								}
  							}]
  						}
  					]
  				});
  				return chartColumnsContainer;
  			}

  			var chartTypeSelector = Ext.create('Sbi.chart.designer.ChartTypeSelector', {
  				region: 'north',
  				minHeight: 50,
  			});

  			var chartTypes = [
  				{
  					name: 'Column chart', 
  					iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_64x64_ico.png',
  					handler: function(btn){
  						Ext.Msg.alert('Clicked Column chart', 'body text');
  						Ext.log('Clicked Column chart');
  					}
  				},
  				{
  					name: 'Line chart', 
  					iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/linechart/img/linechart_64x64_ico.png',
  					handler: function(btn){
  						Ext.log('Clicked Line chart');}
  				},
  				{	
  					name: 'Pie chart', 
  					iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png',
  					handler: function(btn){
  						Ext.log('Clicked Pie chart');}
  				},
  				{
  					name: 'Bar chart', 
  					iconUrl:'/athenachartengine/js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_64x64_ico.png',
  					handler: function(btn){
  						Ext.log('Clicked Pie chart');}
  				}
  			];

  			for(index in chartTypes) {
  				var button = Ext.create('Ext.button.Button', {
  					text: chartTypes[index].name,
  					icon: chartTypes[index].iconUrl,
  					handler: chartTypes[index].handler,
  					scale : "large",
  					width: '100%'
  				});
  				chartTypeSelector.add(button);
  			}

  			var ddGroup1 = 'MEASURE';
  			var ddGroup2 = 'ATTRIBUTE';

  			var columnsStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
  				data: [],
  				sorters: [{
  			        property: 'axisName',
  			        direction: 'ASC'
  			    }],
  			});
  			var categoriesStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
  				data: [],
  				sorters: [{
  			        property: 'axisName',
  			        direction: 'ASC'
  			    }]
  			});

  			columnsStore.on('dataReady', function(jsonData) {
				var jsonDataObj = Ext.JSON.decode(jsonData);
				var theData = [];
  				Ext.each(jsonDataObj.metaData.fields, function(field, index){
  					if(field != 'recNo' && field.type != 'string'){
  						theData.push({
  							axisName : field.header,
  							axisType: 'ATTRIBUTE'
  						});
  					}
  				});
  				columnsStore.setData(theData);
  			});
  			categoriesStore.on('dataReady', function(jsonData) {
  				var jsonDataObj = Ext.JSON.decode(jsonData);
				var theData = [];
  				Ext.each(jsonDataObj.metaData.fields, function(field, index){
  					if(field != 'recNo' && field.type == 'string'){
  						theData.push({
  							axisName : field.header,
  							axisType: 'MEASURE'
  						});
  					}
  				});
  				categoriesStore.setData(theData);
  			});
  			
  			
  			
  			coreServiceManager.run('loadData', {jsonTemplate: jsonTemplate}, [datasetLabel], function (response) {
  				columnsStore.fireEvent('dataReady', response.responseText);
  				categoriesStore.fireEvent('dataReady', response.responseText);
			});
  			
  			

  			var columnsPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  			    region: 'center',
  			    flex:  1,
  			    margin: '0 0 5 0',
  			    store: columnsStore,
  			    viewConfig: {
  			        copy: true,
  					plugins: {
  			            ptype: 'gridviewdragdrop',
  			            containerScroll: true,
  			            dragGroup: ddGroup1,
  			            dropGroup: ddGroup1,
  			        	dragText: 'Drag from Columns Picker',
  			        	enableDrop: false
  			        },
  			        listeners: {
  			        	drop: function(node, data, dropRec, dropPosition) {
  			        		var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('axisName') : ' on empty view';
  			        		Ext.log('Drag from Columns Picker', 'Dropped ' + data.records[0].get('name') + dropOn);
  						}
  					}
  			    },
  				columns: [
  			        {
  			        	text: 'Elenco colonne', 
  			            dataIndex: 'axisName',
  						sortable: false,
  			            flex: 1
  			        }
  			    ]
  			});

  			var categoriesPicker = Ext.create('Sbi.chart.designer.AxisesPicker', {
  			    region: 'south',
  			    flex: 1,
  			    margin: '0 0 5 0',
  			    store: categoriesStore, 
  			    viewConfig: {
  					copy: true,
  					plugins: {
  			            ptype: 'gridviewdragdrop',
  			            containerScroll: true,
  			            dragGroup: ddGroup2,
  			            dropGroup: ddGroup2,
  			        	dragText: 'Drag from Categories Picker',
  			        	enableDrop: false
  			        },
  			        listeners: {
  			        	drop: function(node, data, dropRec, dropPosition) {
  			        		var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('axisName') : ' on empty view';
  			        		Ext.log('Drag from Categories Picker', 'Dropped ' + data.records[0].get('name') + dropOn);
  						}
  					}
  			    },
  				columns: [
  			        {
  			        	text: 'Elenco categorie', 
  			            dataIndex: 'axisName',
  						sortable: false,
  			            flex: 1
  			        }
  			    ]
  			});

  			var chartTypeColumnSelector = Ext.create('Sbi.chart.designer.ChartTypeColumnSelector', {
  				chartTypeSelector: chartTypeSelector,
  			    columnsPicker: columnsPicker,
  				categoriesPicker: categoriesPicker,
  			    region: 'west'
  			});

  			var idseed = 1;

  			var firstcolumn = createChartColumnsContainer('firstcolumn');
  			var leftYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartLeftAxisesContainer',
  				title: 'Asse Y',
  				header:{
  					items:[{
  						xtype:'button',
  						text: '+',
  						handler: function(node, mouse){
  							var panel = Ext.getCmp('chartRightAxisesContainer');
  							if (!panel.isVisible()) {
  								panel.setVisible(true);
  							}
  							
  							addToAxisesContainer('chartRightAxisesContainer');
  						}
  					}]    
  				},
  			});
  			leftYAxisesPanel.add(firstcolumn);

  			var mainPanel = Ext.create('Ext.panel.Panel', {
  			    id: 'mainPanel',
  			    height: 300,
  			    html: '<div style="text-align:center">PREVIEW</div>'
  			});

  			var rightYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartRightAxisesContainer',
  				title: 'Asse Y2',
  				hidden : true,
  				header:{
  					items:[{
  						xtype:'button',
  						text: '+',
  						handler: function(node, mouse){
  							addToAxisesContainer('chartRightAxisesContainer');
  						}
  					}]    
  				},
  			});

  			var bottomXAxisesPanel = Ext.create("Sbi.chart.designer.ChartCategoriesContainer", {
  			    viewConfig: {
  					plugins: {
  			            ptype: 'gridviewdragdrop',
  			            containerScroll: true,
  			            dragGroup: ddGroup2,
  			            dropGroup: ddGroup2
  			        },
  			    },
  			    store: Ext.create('Sbi.chart.designer.AxisesContainerStore'),
  			    columns: [
  			        {
  			        	text: 'Categorie', 
  			            dataIndex: 'axisName',
  						sortable: false,
  			            flex: 10
  			        },
  					{
  						menuDisabled: true,
  						sortable: false,
  						xtype: 'actioncolumn',
  						flex: 1,
  						items: [{
  							icon: 'http://docs.sencha.com/extjs/5.1/5.1.0-apidocs/extjs-build/examples/restful/images/delete.png',
  							tooltip: 'Sell stock',
  							handler: function(grid, rowIndex, colIndex) {
  								var rec = grid.getStore().removeAt(rowIndex);
  							}
  						}]
  					}
  			    ]
  			});

  			var chartStructure = Ext.create('Sbi.chart.designer.ChartStructure', {
  			    title: 'Passo 1',
  			    leftYAxisesPanel: leftYAxisesPanel,
  			    previewPanel: mainPanel,
  			    rightYAxisesPanel: rightYAxisesPanel,
  			    bottomXAxisesPanel: bottomXAxisesPanel
  			});
  			var chartConfiguration = Ext.create('Sbi.chart.designer.ChartConfiguration', {
  				title: 'Passo 2',
  			});
  			var stepsTabPanel = Ext.create('Ext.tab.Panel', {
  			    bodyBorder: false,
  			    width: '100%',
  			    region: 'center',
  			    items: [
  			        chartStructure,
  			      	chartConfiguration,
  			        {title: 'Passo 3',},
  			    ]
  			});
  			        
  			var designerMainPanel = Ext.create('Ext.panel.Panel', {
  				renderTo: Ext.getBody(),
  				xtype: 'layout-border',
  				requires: [
  			        'Ext.layout.container.Border'
  			    ],
  			    layout: 'border',
  			    width: '100%',
  			    height: '100%',                            
  			    bodyBorder: false,
  			    defaults: {
  			        collapsible: false,
  			        split: true,
  			        bodyPadding: 10
  			    },
  			    items: [
  			        chartTypeColumnSelector,
  			        stepsTabPanel,
  			    ]
  			});

 			Ext.log({level: 'info'}, 'CHART: STILL INNNN');
 			Ext.log({level: 'info'}, 'CHART: OUT');

 		  });
		
	</script>
	
	</body>
</html>