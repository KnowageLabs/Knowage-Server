<%@ page language="java" contentType="text/html; charset=UTF-8"
 pageEncoding="UTF-8"%>

<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/adapters/standalone-framework.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-more.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-3d.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/exporting.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/no-data-to-display.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/drilldown.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/heatmap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/treemap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/data.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/treemap/treemap.js"></script>

<script>
	function initChartLibrary(panelId, drillUpText, decimalPoint, thousandsSep) {
		Highcharts.setOptions({
		    chart: {
		 	   renderTo: panelId,
		        backgroundColor: {
		            linearGradient: [0, 0, 500, 500],
		            stops: [
		                [0, 'rgb(255, 255, 255)'],
		                [1, 'rgb(240, 240, 255)']
		            ]
		        },
		        plotBackgroundColor: 'rgba(255, 255, 255, .9)',
		        plotShadow: true,
		        plotBorderWidth: 1
		    },
		    exporting: {
		        url: 'http://'+hostName+':'+serverPort+'/highcharts-export-web/'
		    },
		    lang: {
	            drillUpText: drillUpText,
	            decimalPoint: decimalPoint,
	            thousandsSep: thousandsSep
	        }
		});
	};

	function renderChart(chartConf){
		if(chartConf.chart.type.toLowerCase() == 'treemap'){
			renderTreemap(chartConf);	
		}
		else if (chartConf.chart.type.toLowerCase() == 'heatmap'){
			console.log(chartConf);
			renderHeatmap(chartConf);	
		} 
		
		else{
			console.log("highcharts414Initializer.jspf (line 53)");
			console.log(chartConf);
			new Highcharts.Chart(chartConf);
		}
	};
	
	function handleDrilldown(e) {
		if (!e.seriesOptions) {
			var chart = this;
			chart.showLoading('Loading...');
			Sbi.chart.viewer.HighchartsDrilldownHelper.drilldown(e.point.name, e.point.series.name);
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
			var parameters = {
				breadcrumb: Ext.JSON.encode(Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb),
				jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate
			};
			chartServiceManager.run('drilldownHighchart', parameters, [], function (response) {
				var series = Ext.JSON.decode(response.responseText, true);
	            chart.addSeriesAsDrilldown(e.point, series);
				chart.hideLoading();
			});
		}
	};
	
	function handleDrillup() {
		Sbi.chart.viewer.HighchartsDrilldownHelper.drillup();
	}
	
	function handleCockpitSelection(e) {
		if (!e.seriesOptions) {
// 			debugger;
			var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
			var cockpitWidgets = cockpitWidgetManager.widgets;
			var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;
			
			var selections = {};
// 			selections[e.point.name] = {values: [e.point.series.name]};
			
			for(var i = 0; i < cockpitWidgets.getCount(); i++) {
				var widget = cockpitWidgets.get(i);
				
				if(Sbi.isValorized(widget) && widget.wtype === 'chart' && widget.id === widgetId){
					
					var fieldMeta = widget.getFieldMetaByValue(e.point.name);
					var categoryFieldHeader = fieldMeta!=null?fieldMeta.header: null;
					
					selections[categoryFieldHeader] = {values: [e.point.name]};					
					
					cockpitWidgetManager.onSelection(widget, selections);
				}
			}		
		}
	};
	
	function handleCrossNavigationTo(e) {
		if (!e.seriesOptions) {
			var chart = this;
			chart.showLoading('Loading...');
			
			var categoryName = e.point.name;
			var serieName = e.point.series.name;
			
			Sbi.chart.viewer.HighchartsCrossNavigationHelper.navigateTo(
					e.point.crossNavigationDocumentName, 
					e.point.crossNavigationDocumentParams,
					categoryName,
					serieName
					);
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
			chart.hideLoading();
		}
	};
	
	function handleCrossNavigationFrom() {
		Sbi.chart.viewer.HighchartsCrossNavigationHelper.navigateBackTo();
	};
</script>