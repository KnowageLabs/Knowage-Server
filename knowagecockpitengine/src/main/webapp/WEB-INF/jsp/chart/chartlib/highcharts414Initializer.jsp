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
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-more.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/highcharts-3d.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/exporting.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/no-data-to-display.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/drilldown.src.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/heatmap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/treemap.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/lib/highcharts/4.1.4/modules/data.js"></script>
<script type="text/javascript" src="${pageContext.request.contextPath}/js/src/angular_1.4/chart/treemap/treemap.js"></script>

<script>
	
	// The global JS variable initialized in the main chart execution JSP (chartAngular.jsp) (danristo)
// 	var highchartsDrilldownHelper = highchartsDrilldownHelper();

// 	function init() {
				
// // 		Highcharts.setOptions({
// // 			chart : {
// // 				renderTo : panelId,
// // 				backgroundColor : {
// // 					linearGradient : [ 0, 0, 500, 500 ],
// // 					stops : [ [ 0, 'rgb(255, 255, 255)' ],
// // 							[ 1, 'rgb(240, 240, 255)' ] ]
// // 				},
// // 				plotBackgroundColor : 'rgba(255, 255, 255, .9)',
// // 				plotShadow : true,
// // 				plotBorderWidth : 1
// // 			},
// // 			exporting : {

// // 				/**
// // 						Removing button on the top-right corner that offers us
// // 						printing of the chart.
						
// // 						@author: danristo (danilo.ristovski@mht.net)
// // 				 */
// // 				enabled : false,

// // 				url : 'http://' + hostName + ':' + serverPort
// // 						+ '/highcharts-export-web/'
// // 			},
// // 			lang : {
// // 				drillUpText : drillUpText,
// // 				decimalPoint : decimalPoint,
// // 				thousandsSep : thousandsSep
// // 			},
// // 			drilldown:{
// // 				drillUpButton:{
// // 					 position: 
// // 		                {
// // 		                   align: "center"
// // 		                },
		              
// // 			}
// // 			},
// // 			drilledCategories:[] //array used to save category names when drilling
			
// // 		});
// 		// function wraps library method that controls series order
// 		  (function(HC) {
			  
// 			  	function defined(obj) {
// 			        return obj !== UNDEFINED && obj !== null;
// 			    }
			  
// 			  	var each = HC.each,
// 			    		pick = HC.pick,
// 			        mathMin = Math.min,
// 			        mathMax = Math.max,
// 			        mathAbs = Math.abs,
// 			        UNDEFINED;
			  
// 			    HC.wrap(HC.seriesTypes.column.prototype, 'getColumnMetrics', function(proceed) {
// 			      var series = this,
// 			        options = series.options,
// 			        xAxis = series.xAxis,
// 			        yAxis = series.yAxis,
// 			        reversedXAxis = xAxis.reversed,
// 			        stackKey,
// 			        stackGroups = {},
// 			        columnIndex,
// 			        columnCount = 0;

// 			      // Get the total number of column type series.
// 			      // This is called on every series. Consider moving this logic to a
// 			      // chart.orderStacks() function and call it on init, addSeries and removeSeries
// 			      if (options.grouping === false) {
// 			        columnCount = 1;
// 			      } else {
// 			        each(series.chart.series, function(otherSeries) {
// 			          var otherOptions = otherSeries.options,
// 			            otherYAxis = otherSeries.yAxis;
// 			          if (otherSeries.type === series.type && otherSeries.visible &&
// 			            yAxis.len === otherYAxis.len && yAxis.pos === otherYAxis.pos) { // #642, #2086
// 			            if (otherOptions.stacking) {
// 			              stackKey = otherSeries.stackKey;
// 			              if (stackGroups[stackKey] === UNDEFINED) {
// 			                stackGroups[stackKey] = columnCount++;
// 			              }
// 			              columnIndex = stackGroups[stackKey];
// 			            } else if (otherOptions.grouping !== false) { // #1162
// 			              columnIndex = columnCount++;
// 			            }
// 			            otherSeries.columnIndex = columnIndex;
// 			          }
// 			        });
// 			      }

// 			      var categoryWidth = mathMin(
// 			          mathAbs(xAxis.transA) * (xAxis.ordinalSlope || options.pointRange || xAxis.closestPointRange || xAxis.tickInterval || 1), // #2610
// 			          xAxis.len // #1535
// 			        ),
// 			        groupPadding = categoryWidth * options.groupPadding,
// 			        groupWidth = categoryWidth - 2 * groupPadding,
// 			        pointOffsetWidth = groupWidth / columnCount,
// 			        optionPointWidth = options.pointWidth,
// 			        pointPadding = defined(optionPointWidth) ? (pointOffsetWidth - optionPointWidth) / 2 :
// 			        pointOffsetWidth * options.pointPadding,
// 			        pointWidth = pick(optionPointWidth, pointOffsetWidth - 2 * pointPadding), // exact point width, used in polar charts
// 			        colIndex = (series.columnIndex || 0) + (reversedXAxis ? 1 : 0), // #1251, #3737
// 			        pointXOffset = pointPadding + (groupPadding + colIndex *
// 			          pointOffsetWidth - (categoryWidth / 2)) *
// 			        (reversedXAxis ? -1 : 1);

// 			      // Save it for reading in linked series (Error bars particularly)
// 			      return (series.columnMetrics = {
// 			        width: pointWidth,
// 			        offset: pointXOffset
// 			      });
// 			    });
// 			  })(Highcharts);
		
		
// 	};

// 	function renderChart(chartConf) {
		
// 		var chartType = chartConf.chart.type.toLowerCase();
		
// 		if (chartType == 'treemap') 
// 		{
// 			renderTreemap(chartConf);
// 		} 
// 		else if (chartType == 'heatmap') 
// 		{
// 			renderHeatmap(chartConf);
// 		}
// 		else
// 		{			
// 			/*
// 				Text that will be displayed inside the Back (drillup) button
// 				that appears whenever we enter deeper levels of charts (BAR
// 				and LINE), i.e. whenever we drilldown through categories for
// 				certain serie. This way we will keep record of the current 
// 				drill down level.
				
// 				not used, drillUpText is set dinamicly
				
// 				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
// 			*/
			
		
// 		/*Highcharts.setOptions
// 			(
// 				{
// 			        lang:
// 			    	{
// 			        	drillUpText: "Back to: <b>{series.name}</b>"
// 			    	}
// 		    	}
// 			); */
		    
			
// 			chart =  new Highcharts.Chart(chartConf);
// 			return chart;
 
// 		}
// 	};
  
	/*
		author Radmila Selakovic (radmila.selakovic@mht.net)
	
		Method for updating chart		
 	*/
//     function updateData(data) {
//  		var counterSeries = 0;
// 	    var counter = chart.options.chart.type=="gauge" ? 1 : 2;
// 	    var finish = false;
    
//    		while(!finish) {
// 			if(data[0]["column_"+counter]) {
// 				counter++;
// 				counterSeries++;
// 			}
// 			else {
// 				finish = true;
// 			}
// 		}
//    		for(var j=0;j<chart.series.length;j++) {
//    			chart.series[j].setData([]);
//     	} 
//    	 	for(var j=0;j<data.length;j++) {
//         	for(var i=0;i<counterSeries;i++){ 
//         		if(chart.options.chart.type=="gauge"){
//         			chart.series[i].addPoint([data[j]["column_"+1], parseFloat(data[j]["column_"+(i+1)])], true, false);
//         		}else{
//         			chart.series[i].addPoint([data[j]["column_"+1], parseFloat(data[j]["column_"+(i+2)])], true, false);
			
// 				}
//         	}
//     	}  
//     }

// 	function handleDrilldown(e) {
		
// 		if (!e.seriesOptions) 
// 		{			
// 			/*
// 				Disable drill down when user clicks on the hyperlink that points to
// 				the category value (on X-axis). Drill down is enabled only when user				
// 				clicks on single serie within one category value (group). Detection
// 				of clicking on the category hyperlink is done via checking if the
// 				appropriate property in input parameter of the listener is not a 
// 				number. If it is, the hyperlink is clicked and we have a ordinal
// 				number of the category on which user clicked.
				
// 				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
// 			*/
// 			if (isNaN(e.category))
// 			{
				
// 				// The "this" references to the Highcharts.Chart object. (danristo)
// 				var chart = this;
// 				chart.showLoading('Loading...');
				
// 				// TODO: commented by: danristo (EXT -> ANGULAR)
// 				//Sbi.chart.viewer.HighchartsDrilldownHelper.drilldown(e.point.name, e.point.series.name);
				
// 				/* console.log("== USAO USPESNO 2 ==");
// 				console.log(highchartsDrilldownHelper);
// 				console.log(chartExecutionWebServiceManager);
// 				console.log(jsonTemplate); */
				
// 				highchartsDrilldownHelper.drilldown(e.point.name, e.point.series.name);
				
// 				// TODO: commented by: danristo (EXT -> ANGULAR)
// 				//var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
// 				/* var parameters = {
// 					breadcrumb: Ext.JSON.encode(Sbi.chart.viewer.HighchartsDrilldownHelper.breadcrumb),
// 					jsonTemplate: Sbi.chart.viewer.ChartTemplateContainer.jsonTemplate
// 				}; */
				
// 				var parameters = {
// 						breadcrumb: JSON.stringify(highchartsDrilldownHelper.breadcrumb),
// 						jsonTemplate: jsonTemplate
// 					};
				
// 				// TODO: commented by: danristo (EXT -> ANGULAR)
// 				//chartServiceManager.run('drilldownHighchart', parameters, [], function (response) {
				
// 				chartExecutionWebServiceManager.run('drilldownHighchart', parameters, [], function (response) {
										
// 					// TODO: commented by: danristo (EXT -> ANGULAR)
// 					//var series = JSON.parse(response.responseText, true);
					
// 					var series = response.data;
		           
// 		           if(chart.options.drilledCategories.length==0){
// 		        	   chart.options.drilledCategories.push(chart.xAxis[0].axisTitle.textStr);
		        	   
// 		           }
		           
// 		           chart.options.drilledCategories.push(series.category);
// 		            var xAxisTitle={
// 		            	text:series.category	
// 		            };
// 		            var yAxisTitle={
// 		            		text:series.serieName
// 		            };
// 		            if(chart.xAxis[0].userOptions.title.customTitle==false){
// 		            chart.xAxis[0].setTitle(xAxisTitle);
// 		            }
// 		            if(chart.yAxis[0].userOptions.title.custom==false){
// 		            chart.yAxis[0].setTitle(yAxisTitle);
// 		            }
		           
// 		            chart.addSeriesAsDrilldown(e.point, series);
		            
// 		            var backText="Back to: <b>"+chart.options.drilledCategories[chart.options.drilledCategories.length-2]+"</b>";
		        
// 		            chart.drillUpButton.textSetter(backText);
		          
// 					chart.hideLoading();
// 				}, 
				
// 				// Failure function that handles failures when drilldown does not perform well - some server-side problem (danristo)
// 				function() {/*chart.hideLoading();*/});
// 			}
// 		}
// 	};

// 	function handleDrillup() {
// 		var chart=this;
		
// 		// sets the title on x axis 
		
// 		chart.options.drilledCategories.pop();
// 		titleText=chart.options.drilledCategories[chart.options.drilledCategories.length-1];
// 		var backText=chart.options.drilledCategories[chart.options.drilledCategories.length-2];
// 		chart.drillUpButton.textSetter("Back to: <b>"+backText+"</b>");
//         //  chart.redraw();
// 		var xAxisTitle={
//             	text:titleText	
//             };
// 		    if(chart.xAxis[0].userOptions.title.customTitle==false){
//             chart.xAxis[0].setTitle(xAxisTitle);
// 		    }
		    
// 		var yAxisTitle={
// 				text: ' '
// 		};
	
		
//        if(chart.drilldownLevels.length==0 && chart.yAxis[0].userOptions.title.custom==false){
//     	   chart.yAxis[0].setTitle(yAxisTitle);
//        }
       
//     	// TODO: commented by: danristo (EXT -> ANGULAR)
// 		//Sbi.chart.viewer.HighchartsDrilldownHelper.drillup();
//        highchartsDrilldownHelper.drillup();
// 	}

// 	function handleCockpitSelection(e) {
// 		if (!e.seriesOptions) {
// 			if(parent && parent.angular && parent.angular.element){
// 				// Cockpit 3 (AngularJS version of ockpit)				
// 				parent.angular.element(window.frameElement.parentElement.parentElement).scope().reloadWidgetsByChartEvent(e);
// 			}else{
// 				// Old ExtJS Cockpit				
// 				var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
// 				var cockpitWidgets = cockpitWidgetManager.widgets;
				
// 				console.log(cockpitWidgets);
				
// 				//var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;
				
// 				console.log(widgetId);
				
// 				var selections = {};
// //	 			selections[e.point.name] = {values: [e.point.series.name]};
				
// 				for(var i = 0; i < cockpitWidgets.getCount(); i++) {
// 					var widget = cockpitWidgets.get(i);
									
// 					if(widget && widget.wtype === 'chart' && widget.id === widgetId){
						
// 						var fieldMeta = widget.getFieldMetaByValue(e.point.name);
// 						var categoryFieldHeader = fieldMeta!=null?fieldMeta.header: null;
						
// 						selections[categoryFieldHeader] = {values: [e.point.name]};					
						
// 						cockpitWidgetManager.onSelection(widget, selections);
// 					}
// 				}	
// 			}
// 		}
// 	};

// 	function handleCrossNavigationTo(e) {
// 		console.log(e.point);
// 		if (!e.seriesOptions) {
// 			var chart = this;
// 			//chart.showLoading('Loading...');
//  			var categoryName = null;
// 			var categoryValue = e.point.name;

// 			if (e.point.hasOwnProperty('category')) {
// 				if(isNaN(e.point.category)){
// 				categoryName = e.point.category;
// 				}
// 			}
			
// 			var serieName = e.point.series.name;
// 			var serieValue = e.point.y;
		

// 			var groupingCategoryName = null;
// 			var groupingCategoryValue = null;

// 			if (e.point.hasOwnProperty('group')) {
// 				groupingCategoryName = e.point.group.name;
// 				groupingCategoryValue = e.point.group.value;
// 			}
			
//             // fisrt parameter is string chart type 
// //              if(window.parent.angular && window.parent.parent.angular.element(window.parent.frameElement).scope().crossNavigationHelper!=undefined){
// 	  if(parent.execExternalCrossNavigation){
//             	var navData={
//             			chartType:	"HIGHCHART",
//             			documentName:e.point.crossNavigationDocumentName,
//             			documentParameters:e.point.crossNavigationDocumentParams,
//             			CATEGORY_NAME :categoryName,
//             			CATEGORY_VALUE :categoryValue,
//             			SERIE_NAME :serieName,
//             			SERIE_VALUE :serieValue,
//             			GROUPING_NAME:groupingCategoryName,
//             			GROUPING_VALUE:groupingCategoryValue,
//             			stringParameters:null
//             	}; 
//             	parent.execExternalCrossNavigation(navData,JSON.parse(driverParams),undefined,currentDocumentLabel)
// //             	parent.angular.element(frameElement).scope().navigateTo(navData); 
//             }else{
//             	/* Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
//     					"HIGHCHART",
//     					e.point.crossNavigationDocumentName,
//     					e.point.crossNavigationDocumentParams, categoryName,
//     					categoryValue, serieName, serieValue, groupingCategoryName,
//     					groupingCategoryValue, null);	 */
//             }            
			
// 			// TODO: commented by: danristo (EXT -> ANGULAR)  
// 			/* var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory
// 					.getChartWebServiceManager(); */
					
// 			chart.hideLoading();
// 		}
// 	};

// 	function handleCrossNavigationFrom() {
// 		// TODO: commented by: danristo (EXT -> ANGULAR)
// 		//Sbi.chart.viewer.CrossNavigationHelper.navigateBackTo();
// 	};
	
	/* author danristo (EXT -> ANGULAR) */
// 	function highchartsDrilldownHelper() {
	
// 		return {
			
// 			breadcrumb: [],
			
// 			drilldown: function(selectedName, selectedSerie){
				
// 				var drill = {
// 						selectedName: selectedName,
// 						selectedSerie: selectedSerie
// 				};
				
// 				console.info("IN: HighchartsDrilldownHelper (handling the clicking on the value label of the charts item)");
				
// 				/**
// 				 * The workaround solution for drilling down when clicking on the value of the chart
// 				 * (i.e. on the label that is linked to single bar in the BAR chart or on the label
// 				 * that is linked to single point inside the LINE chart), when having multiple layers.
// 				 * 
// 				 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
// 				 */
// 				var i = this.breadcrumb.length;
// 				var indicator = null;
			   
// 				while (i--) 
// 			    {
// 			       if (JSON.stringify(this.breadcrumb[i]) === JSON.stringify(drill)) 
// 			       {
			    	   
// 			    	   indicator = true;
// 			       }
// 			    }
					
// 			    if (indicator != true)
// 			    	indicator = false;
				
// 			    if (!indicator)
// 				this.breadcrumb.push(drill);
// 			},
			
// 			drillup: function(){
// 				this.breadcrumb.pop();
// 			}
// 		}
// 	}
	
</script>