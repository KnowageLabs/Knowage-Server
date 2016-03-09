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
			chart : {
				renderTo : panelId,
				backgroundColor : {
					linearGradient : [ 0, 0, 500, 500 ],
					stops : [ [ 0, 'rgb(255, 255, 255)' ],
							[ 1, 'rgb(240, 240, 255)' ] ]
				},
				plotBackgroundColor : 'rgba(255, 255, 255, .9)',
				plotShadow : true,
				plotBorderWidth : 1
			},
			exporting : {

				/**
						Removing button on the top-right corner that offers us
						printing of the chart.
						
						@author: danristo (danilo.ristovski@mht.net)
				 */
				enabled : false,

				url : 'http://' + hostName + ':' + serverPort
						+ '/highcharts-export-web/'
			},
			lang : {
				drillUpText : drillUpText,
				decimalPoint : decimalPoint,
				thousandsSep : thousandsSep
			},
			drilldown:{
				drillUpButton:{
					 position: 
		                {
		                   align: "center"
		                },
		              
			}
			},
			drilledCategories:[] //array used to save category names when drilling
			
		});
	};

	function renderChart(chartConf) {
		var chartType = chartConf.chart.type.toLowerCase();
		
		if (chartType == 'treemap') 
		{
			renderTreemap(chartConf);
		} 
		else if (chartType == 'heatmap') 
		{
			renderHeatmap(chartConf);
		}
		else
		{			
			/*
				Text that will be displayed inside the Back (drillup) button
				that appears whenever we enter deeper levels of charts (BAR
				and LINE), i.e. whenever we drilldown through categories for
				certain serie. This way we will keep record of the current 
				drill down level.
				
				not used, drillUpText is set dinamicly
				
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			
		
		/*Highcharts.setOptions
			(
				{
			        lang:
			    	{
			        	drillUpText: "Back to: <b>{series.name}</b>"
			    	}
		    	}
			); */
		    
			new Highcharts.Chart(chartConf);
		}
	};

	function handleDrilldown(e) {
		
		if (!e.seriesOptions) 
		{			
			/*
				Disable drill down when user clicks on the hyperlink that points to
				the category value (on X-axis). Drill down is enabled only when user				
				clicks on single serie within one category value (group). Detection
				of clicking on the category hyperlink is done via checking if the
				appropriate property in input parameter of the listener is not a 
				number. If it is, the hyperlink is clicked and we have a ordinal
				number of the category on which user clicked.
				
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			if (isNaN(e.category))
			{
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
					
		           
		           if(chart.options.drilledCategories.length==0){
		        	   chart.options.drilledCategories.push(chart.xAxis[0].axisTitle.textStr);
		        	   
		           }
		           
		           chart.options.drilledCategories.push(series.category);
		            var xAxisTitle={
		            	text:series.category	
		            };
		            var yAxisTitle={
		            		text:series.serieName
		            };
		            if(chart.xAxis[0].userOptions.title.customTitle==false){
		            chart.xAxis[0].setTitle(xAxisTitle);
		            }
		            if(chart.yAxis[0].userOptions.title.custom==false){
		            chart.yAxis[0].setTitle(yAxisTitle);
		            }
		           
		            chart.addSeriesAsDrilldown(e.point, series);
		            
		            var backText="Back to: <b>"+chart.options.drilledCategories[chart.options.drilledCategories.length-2]+"</b>";
		        
		            chart.drillUpButton.textSetter(backText);
		          
					chart.hideLoading();
				});
			}
		}
	};

	function handleDrillup() {
		var chart=this;
		
		// sets the title on x axis 
		
		chart.options.drilledCategories.pop();
		titleText=chart.options.drilledCategories[chart.options.drilledCategories.length-1];
		var backText=chart.options.drilledCategories[chart.options.drilledCategories.length-2];
	
	chart.drillUpButton.textSetter("Back to: <b>"+backText+"</b>");
    chart.reflow();
		var xAxisTitle={
            	text:titleText	
            };
		    if(chart.xAxis[0].userOptions.title.customTitle==false){
            chart.xAxis[0].setTitle(xAxisTitle);
		    }
		    
		var yAxisTitle={
				text: ' '
		};
	
		
       if(chart.drilldownLevels.length==0 && chart.yAxis[0].userOptions.title.custom==false){
    	   chart.yAxis[0].setTitle(yAxisTitle);
       }
   
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
			//chart.showLoading('Loading...');

			var categoryName = null;
			var categoryValue = e.point.name;

			if (e.point.hasOwnProperty('category')) {
				categoryName = e.point.category;
			}
			
			var serieName = e.point.series.name;
			var serieValue = e.point.y;
		

			var groupingCategoryName = null;
			var groupingCategoryValue = null;

			if (e.point.hasOwnProperty('group')) {
				groupingCategoryName = e.point.group.name;
				groupingCategoryValue = e.point.group.value;
			}

			Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
					e.point.crossNavigationDocumentName,
					e.point.crossNavigationDocumentParams, categoryName,
					categoryValue, serieName, serieValue, groupingCategoryName,
					groupingCategoryValue);

			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory
					.getChartWebServiceManager();
			chart.hideLoading();
		}
	};

	function handleCrossNavigationFrom() {
		Sbi.chart.viewer.CrossNavigationHelper.navigateBackTo();
	};
</script>