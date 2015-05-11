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

 			<%-- 
 			
  			initChartLibrary(mainPanel.id);
  			
  			--%>
  			var sbiExecutionId = <%=request.getParameter("SBI_EXECUTION_ID")!=null? "'"+request.getParameter("SBI_EXECUTION_ID")+"'" : "null"%>;
 			var userId = '<%=userId%>';
 			var hostName = '<%=request.getServerName()%>';
 			var serverPort = '<%=request.getServerPort()%>';
 			var jsonTemplate = Ext.JSON.decode('<%=template%>');
 			var datasetLabel  = '<%=datasetLabel%>';
  			
 			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager('http', hostName, serverPort, sbiExecutionId, userId);
 			
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
 					property: 'serieColumn',
 					direction: 'ASC'
 				}],
 				listeners: {
 					dataReady: function(jsonData) {
 						var jsonDataObj = Ext.JSON.decode(jsonData);
 						var theData = [];
 		  				Ext.each(jsonDataObj.results, function(field, index){
 		  					if(field != 'recNo' && field.nature == 'measure'){
 		  						theData.push({
 		  							serieColumn : field.alias,
 		  							axisType: 'ATTRIBUTE'
 		  						});
 		  					}
 		  				});
 		  				this.setData(theData);
 		  			}
 				}
 			});
 			var categoriesStore = Ext.create('Sbi.chart.designer.AxisesContainerStore', {
 				data: [],
 				sorters: [{
 					property: 'categoryColumn',
 					direction: 'ASC'
 				}],
 				listeners: {
 					dataReady: function(jsonData) {
 		  				var jsonDataObj = Ext.JSON.decode(jsonData);
 						var theData = [];
 		  				Ext.each(jsonDataObj.results, function(field, index){
 		  					if(field != 'recNo' && field.nature == 'attribute'){
 		  						theData.push({
 		  							categoryColumn : field.alias,
 		  							axisType: 'MEASURE'
 		  						});
 		  					}
 		  				});
 		  				this.setData(theData);
 		  			}
 				}
 			});
  			
  			chartServiceManager.run('loadDatasetFields', {}, [datasetLabel], function (response) {
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
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroup1,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroup1,
  						dragText: 'Drag from Columns Picker',
  						enableDrop: false
  					},
  					listeners: {
  						drop: function(node, data, dropRec, dropPosition) {
  							var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('serieColumn') : ' on empty view';
  							Ext.log('Drag from Columns Picker', 'Dropped ' + data.records[0].get('name') + dropOn);
  						}
  					}
  				},
  				columns: [
  					{
  						text: 'Elenco misure', 
  						dataIndex: 'serieColumn',
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
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroup2,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroup2,
  						dragText: 'Drag from Categories Picker',
  						enableDrop: false
  					},
  					listeners: {
  						drop: function(node, data, dropRec, dropPosition) {
  							var dropOn = dropRec ? ' ' + dropPosition + ' ' + dropRec.get('categoryColumn') : ' on empty view';
  							Ext.log('Drag from Categories Picker', 'Dropped ' + data.records[0].get('name') + dropOn);
  						}
  					}
  				},
  				columns: [
  					{
  						text: 'Elenco attributi', 
  						dataIndex: 'categoryColumn',
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

/*   		var leftYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartLeftAxisesContainer',
  				header:{
  					title: {hidden: true },
  					items:[{
	                    xtype: 'textfield',
  						flex: 6,
						allowBlank: false,
						height: 30,
	                    emptyText: 'Insert name',
						selectOnFocus: true,
						value: 'Custom name',
	                },{
  						xtype: 'tbfill',
  						flex: 1
  					},{
  						xtype:'button',
  						text: '+',
  						flex: 1,
  						handler: function(node, mouse){
  							var panel = Ext.getCmp('chartRightAxisesContainer');
  							if (!panel.isVisible()) {
  								panel.setVisible(true);
  							}
  							
  							addToAxisesContainer('chartRightAxisesContainer');
  						}
  					}]    
  				},
  			}); */

  			var mainPanel = Ext.create('Ext.panel.Panel', {
  				id: 'mainPanel',
  				height: 300,
  				html: '<div style="text-align:center">PREVIEW</div>'
  			});

  			var rightYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartRightAxisesContainer',
  				hidden : true
  			});
  			
  			var leftYAxisesPanel = Ext.create('Sbi.chart.designer.ChartAxisesContainer', {
  				id: 'chartLeftAxisesContainer',
  				alias: 'Asse Y',
  				otherPanel: rightYAxisesPanel
  			});

  			
  			var firstcolumn = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(
  						'chartLeftAxisesContainer', '', false, 
  						Sbi.chart.designer.ChartUtils.ddGroup1, 
  						Sbi.chart.designer.ChartUtils.ddGroup1);
  			leftYAxisesPanel.add(firstcolumn);

  			var categoriesStore = Ext.create('Sbi.chart.designer.AxisesContainerStore');
  			var bottomXAxisesPanel = Ext.create("Sbi.chart.designer.ChartCategoriesContainer", {
  				viewConfig: {
  					plugins: {
  						ptype: 'gridviewdragdrop',
  						containerScroll: true,
  						dragGroup: Sbi.chart.designer.ChartUtils.ddGroup2,
  						dropGroup: Sbi.chart.designer.ChartUtils.ddGroup2
  					},
  				},
  				store: categoriesStore,
  				columns: [
  					{
  						text: 'Categorie', 
  						dataIndex: 'categoryColumn',
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
 			
  			/*  LOADING CONFIGURATION FROM TEMPLATE >>>>>>>>>>>>>>>>>>>> */
 			Ext.log({level: 'info'}, 'CHART: IN CONFIGURATION FROM TEMPLATE');
  			
  			/**
  				START LOADING Y AXES >>>>>>>>>>>>>>>>>>>>
  			*/
  			
  			var yCount = 0;
  			var theStorePool = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
  			Ext.Array.each(jsonTemplate.CHART.AXES_LIST.AXIS, function(axis, index){
  				if(axis.type.toUpperCase() == "SERIE"){

  					var axisAlias = axis.alias;
  					
  					if(yCount == 0) {
  					
  						theStorePool[0].axisAlias = axisAlias;

  					} else {
	  					var newColumn = Sbi.chart.designer.ChartColumnsContainerManager.createChartColumnsContainer(
	  							rightYAxisesPanel.id , '', true, 
	  							Sbi.chart.designer.ChartUtils.ddGroup1, 
	  							Sbi.chart.designer.ChartUtils.ddGroup1, axisAlias);
	  					rightYAxisesPanel.add(newColumn);
	  					rightYAxisesPanel.show();
	  				}
	  				yCount++;
  				}
  			});
  			/**
				END LOADING Y AXES <<<<<<<<<<<<<<<<<<<<
			*/
  			
			
			/**
				START LOADING SERIES >>>>>>>>>>>>>>>>>>>>
			*/
  			function jsonizeStyle(str) {
  				var jsonStyle = {};
  				var styles = str.split(';');
  				for(style in styles) {
  					var keyValue = style.split(':');
  					jsonStyle[keyValue[0]] = keyValue[1];
  				}
  				
  				console.log('jsonStyle: ',  jsonStyle);
  				
  				return jsonStyle;
  			}
  			
  			Ext.Array.each(jsonTemplate.CHART.VALUES.SERIE, function(serie, index){
  				var axisAlias = serie.axis;
  				Ext.Array.each(theStorePool, function(store, index){
  					if(store.axisAlias === axisAlias) {

  						var tooltip = serie.TOOLTIP ? serie.TOOLTIP : {};
  						var tooltipStyle = serie.TOOLTIP ? serie.TOOLTIP.style : '';
  						var jsonTooltipStyle = jsonizeStyle(tooltipStyle);
  						
  						var newCol = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
  							axisName: serie.name,
  							axisType: 'MEASURE',
  							
  							serieGroupingFunction: '',
  							serieType: serie.type,
  							serieOrderType: serie.orderType,
  							serieColumn: serie.column,
  							serieColor: serie.color,
  							serieShowValue: serie.showValue,
  							seriePrecision: serie.precision+'',
  							seriePrefixChar: serie.prefixChar,
  							seriePostfixChar: serie.postfixChar,
  							
  							serieTooltipTemplateHtml: tooltip.templateHtml,
  							serieTooltipBackgroundColor: tooltip.backgroundColor,
  							serieTooltipAlign: jsonTooltipStyle.align,
  							serieTooltipColor: jsonTooltipStyle.color,
  							serieTooltipFont: jsonTooltipStyle.font,
  							serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
  							serieTooltipFontSize: jsonTooltipStyle.fontSize
  						});
  						
  						store.add(newCol);
  					}
  				});
  			});

  			/**
				END LOADING SERIES <<<<<<<<<<<<<<<<<<<<
			*/
  			
  			
			
			/**
				START LOADING CATEGORIES >>>>>>>>>>>>>>>>>>>>
			*/
			var category = jsonTemplate.CHART.VALUES.CATEGORY;
			var newCat = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
				axisName: category.column,
				axisType: 'ATTRIBUTE', 
				
				categoryColumn: category.column,
				categoryGroupby: '', 
				categoryStacked: '', 
				categoryStackedType: '', 
				categoryOrderColumn: category.orderColumn, 
				categoryOrderType: category.orderType
			});
			categoriesStore.add(newCat);
			
			/**
				END LOADING CATEGORIES <<<<<<<<<<<<<<<<<<<<
			*/
  			
 			Ext.log({level: 'info'}, 'CHART: OUT CONFIGURATION FROM TEMPLATE');
  			
  			/*  LOADED CONFIGURATION FROM TEMPLATE <<<<<<<<<<<<<<<<<<<< */
  			
 			
 			
 			
 			
 			
 			Ext.log({level: 'info'}, 'CHART: OUT');

 		  });
		
	</script>
	
	</body>
</html>