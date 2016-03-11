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


<%@ page language="java" contentType="text/html; charset=UTF-8"
 pageEncoding="UTF-8"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/adapters/standalone-framework.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-3d.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/exporting.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/no-data-to-display.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/d3/renderD3.js"></script> 
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/ext5/sbi/chart/viewer/CrossNavigationHelper.js"></script>
<script>

	// TODO: check if ext was imported (don't care)
	
	// TODO: Ext.create("Sbi.chart.library") (don't care)
	
	function initChartLibrary(panelId) {
		
		document.getElementById(panelId).setAttribute("style","height:0px");
	}
	
	/* 	
		Inspect if chart type that we want to render on the window of the page (panel)
		is of D3 type (belongs to D3 library). We need this information for deciding 
		whether we will re-render the chart invoking the same rendering functions for
		the D3 chart.
		(danilo.ristovski@mht.net)
	*/
	function isChartD3(chartConf)
	{
		var chartType = chartConf.chart.type.toLowerCase();
		
		if(chartType == "wordcloud" || chartType == "sunburst" || chartType== "parallel" || chartType == "chord")
		{			
			return true;			
		}
		else
		{
			return false;
		}
	}

	function renderChart(chartConf){
		
		/* 
			If rerendering the chart, we will need cleaning of the
			page before it. This method will destroy (remove) every
			page element that already existed on the page (potentially).
			It is implemented inside of the renderD3.js.
			@author: (danilo.ristovski@mht.net)
		*/
		cleanChart();
		
		if(chartConf.chart.type.toLowerCase() == "wordcloud"){
			
			renderWordCloud(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "sunburst")
		{
			renderSunburst(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "parallel")
		{
			renderParallelChart(chartConf);
			
		}
		else if (chartConf.chart.type.toLowerCase() == "chord")
		{
			renderChordChart(chartConf);
		}
		
		else
		{
			alert("Chart not defined");
		}
	}
	
	
	
	
	function handleCrossNavigationTo(e){
		if (!e.seriesOptions) {
			var chart = this;
			//chart.showLoading('Loading...');
			var categoryName=e.categoryName;
			var categoryValue = e.categoryValue;
			var serieName=e.serieName;
			var serieValue = e.serieValue;
			var groupingCategoryName=e.groupingCategoryName;
			var groupingCategoryValue=e.groupingCategoryValue;
			
			Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
					e.crossNavigationDocumentName, 
					e.crossNavigationDocumentParams,
					categoryName,
					categoryValue,
					serieName,
					serieValue,
					groupingCategoryName,
					groupingCategoryValue
					);
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
			chart.hideLoading();
		}
		
	};
	
	function handleCrossNavigationFrom(){
		Sbi.chart.viewer.CrossNavigationHelper.navigateBackTo();
	};
	
	function handleCockpitSelection(e) {
		
// 			debugger;
			var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
			var cockpitWidgets = cockpitWidgetManager.widgets;
			var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;
			
			var selections = {};
// 			selections[e.point.name] = {values: [e.point.series.name]};
			
			for(var i = 0; i < cockpitWidgets.getCount(); i++) {
				var widget = cockpitWidgets.get(i);
				
				if(Sbi.isValorized(widget) && widget.wtype === 'chart' && widget.id === widgetId){
					
// 					var fieldMeta = widget.getFieldMetaByValue(e.categoryValue);
// 					var categoryFieldHeader = fieldMeta!=null?fieldMeta.header: null;
					
//					selections[categoryFieldHeader] = {values: [e.categoryValue]};					
					
					for(var category in e){
						
						
						 if (e.hasOwnProperty(category)) {
							 selections[category] = {values: [e[category]]};
						   
						    }
					}
                 
                    
 					//console.log(selections);
					cockpitWidgetManager.onSelection(widget, selections);
				}
			}		
		
	};
	
</script>
