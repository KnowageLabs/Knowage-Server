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

<script type="text/javascript"
	src="${pageContext.request.contextPath}/js/lib/chartJs/Chart.js"></script>

<script>
	function initChartLibrary(panelId, drillUpText, decimalPoint, thousandsSep) {
		
	};

	function renderChart(chartConf) {
		
		// NEW
		var chartPanelTitleOrNoData = document.getElementById('chartPanelTitleOrNoData');
		var chartPanelSubtitle = document.getElementById('chartPanelSubtitle');		
		var chartPanelCanvas = document.getElementById('chartPanelCanvas');		
		
		var mainPanelRegion = document.getElementById('mainPanel');		

		// No data to represent				
		if ((chartConf.data.labels && chartConf.data.labels.length == 0)
				|| (chartConf.data.datasets && chartConf.data.datasets.length == 0)) {

			if (chartConf.chart.emptyMessage && chartConf.chart.emptyMessage.style
					&& chartConf.chart.emptyMessage.text != '') {
				
					var emptyMessageContainerStyle = {
						padding : '5 20 5 20'
					};

					var emptyMessageStyleKeys = Object.keys(chartConf.chart.emptyMessage.style);
					
					chartPanelTitleOrNoData.style["padding"] = emptyMessageContainerStyle.padding;
					
					for (var i = 0; i < emptyMessageStyleKeys.length; i++) {
						
						var emptyMessageStyleName = emptyMessageStyleKeys[i];

						if (emptyMessageStyleName.toLowerCase() != 'align') {
							chartPanelTitleOrNoData.style[emptyMessageStyleName] = chartConf.chart.emptyMessage.style[emptyMessageStyleName];
						}
						else {
							chartPanelTitleOrNoData.style["text-align"] = chartConf.chart.emptyMessage.style[emptyMessageStyleName];
						}
						
					}

					chartPanelTitleOrNoData.innerHTML = chartConf.chart.emptyMessage.text;
					
			}
			
		} 		
		//The are data to represent 
		else { 
			
			// TITLE management
			if (chartConf.chart.title && chartConf.chart.title.style && chartConf.chart.title.text != '') {
			
				var titleContainerStyle = {
					padding : '5 20 5 20'
				};

				var titleStyleKeys = Object.keys(chartConf.chart.title.style);
				
				chartPanelTitleOrNoData.style["padding"] = titleContainerStyle.padding;
				
				for (var i = 0; i < titleStyleKeys.length; i++) {
					
					var titleStyleName = titleStyleKeys[i];

					if (titleStyleName.toLowerCase() != 'align') {
						chartPanelTitleOrNoData.style[titleStyleName] = chartConf.chart.title.style[titleStyleName];
					}
					else {
						chartPanelTitleOrNoData.style["text-align"] = chartConf.chart.title.style[titleStyleName];
					}
					
				}

				chartPanelTitleOrNoData.innerHTML = chartConf.chart.title.text;
				
			}

			// SUBTITLE management
			if (chartConf.chart.subtitle && chartConf.chart.subtitle.style && chartConf.chart.subtitle.text != '') {
			
				var subtitleContainerStyle = {
					padding : '5 20 5 20'
				};

				var subtitleStyleKeys = Object.keys(chartConf.chart.subtitle.style);
				
				chartPanelSubtitle.style["padding"] = subtitleContainerStyle.padding;
				
				for (var i = 0; i < subtitleStyleKeys.length; i++) {
					
					var subtitleStyleName = subtitleStyleKeys[i];
					
					if (subtitleStyleName.toLowerCase() != 'align') {
						chartPanelSubtitle.style[subtitleStyleName] = chartConf.chart.subtitle.style[subtitleStyleName];
					}
					else {
						chartPanelSubtitle.style["text-align"] = chartConf.chart.subtitle.style[subtitleStyleName];
					}
					
				}

				chartPanelSubtitle.innerHTML = chartConf.chart.subtitle.text;
			} 

			var chartType = chartConf.chart.type.toLowerCase();
			
			// Gets the context of the canvas element we want to select
			var ctx = document.getElementById("chartPanelCanvas").getContext("2d");
			
			// Destroy the ChartJS instance (the one that is placed inside the canvas HTML element) before reseting the chart (when resizing)
			window.myNewChart ? window.myNewChart.destroy() : null;
			
			/* 
				Collect heights of Title and Subtitle of the chart (they should be substracted from the overall height wished by user in 
				order to determine the height of the canvas within which the ChartJS chart will render). 
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			var titleDivHeight = document.getElementById("chartPanelTitleOrNoData").clientHeight;
			var subtitleDivHeight = document.getElementById("chartPanelSubtitle").clientHeight;
						
			var totalTitleSubtitleHeight = 0;
			
			totalTitleSubtitleHeight += titleDivHeight ?  titleDivHeight : 0;
			totalTitleSubtitleHeight += subtitleDivHeight ?  subtitleDivHeight : 0;			
			
			if (!chartConf.chart.height && !chartConf.chart.heightInPerc) {
				
				ctx.canvas.height = window.innerHeight-1-totalTitleSubtitleHeight;
				mainPanelRegion.style.height = window.innerHeight-1;
				
				if (window.myNewChart) { 
					window.myNewChart.chart.canvas.height = window.innerHeight-1-totalTitleSubtitleHeight;
				}
			}
			else {
				
				if (chartConf.chart.heightDimType=="pixels") {
					ctx.canvas.height = chartConf.chart.height - totalTitleSubtitleHeight;
					mainPanelRegion.style.height = chartConf.chart.height;
				}
				// percentage
				else {
					ctx.canvas.height = chartConf.chart.heightInPerc/100 * window.innerHeight - 1 - totalTitleSubtitleHeight;
					mainPanelRegion.style.height = chartConf.chart.heightInPerc/100 * window.innerHeight - 1;
					
					if (window.myNewChart) { 
						window.myNewChart.chart.canvas.height = chartConf.chart.heightInPerc/100 * window.innerHeight - 1 - totalTitleSubtitleHeight;
					}
				}
			}
			
			if (!chartConf.chart.width && !chartConf.chart.widthInPerc) {	
				
				ctx.canvas.width = window.innerWidth-1;
				mainPanelRegion.style.width = window.innerWidth-1;	
				
				if (window.myNewChart) { 
					window.myNewChart.chart.canvas.width=window.innerWidth-1;
				}
			}
			else {
				
				if (chartConf.chart.widthDimType=="pixels") {
					ctx.canvas.width = chartConf.chart.width;
					mainPanelRegion.style.width = chartConf.chart.width;
				}
				// percentage
				else {
					ctx.canvas.width = chartConf.chart.widthInPerc/100 * window.innerWidth-1;
					mainPanelRegion.style.width = chartConf.chart.widthInPerc/100 * window.innerWidth-1;
					
					if (window.myNewChart) { 
						window.myNewChart.chart.canvas.width=chartConf.chart.widthInPerc/100 * window.innerWidth-1;
					}
				}
			}
			
			// For responsive and scaleFontSize that are set as in the beginning (this is changed or commented in the VM however) 
			//ctx.canvas.height = (window.innerHeight+1)/5.1;			
			
			//Sets the background color
			if (chartConf.chart.backgroundColor
					&& chartConf.chart.backgroundColor != '') {				
				mainPanelRegion.style.backgroundColor = chartConf.chart.backgroundColor;
			}
				
			/* 
				window.myNewChart - the global variable that will contain the rendered chart configuration 
				(needed for reseting (destroying) the canvas content - the chart itself when resizing).
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/			
		 	if (chartType == 'line') {
				 window.myNewChart = new Chart(ctx).Line(chartConf.data, chartConf.options);
			} else if (chartType == 'pie') {
				window.myNewChart = new Chart(ctx).Pie(chartConf.data, chartConf.options);
			} else { // bar
				window.myNewChart = new Chart(ctx).Bar(chartConf.data, chartConf.options);
			}
			
			// TODO: SETTING FOR THE LEGEND
			if (chartConf && chartConf.chart && chartConf.chart.showLegend) {
				var chartPanelLegeng = Ext.create('Ext.panel.Panel', {
					id : 'chartPanelLegeng',
					floating : true,
					layout : 'fit',
					margin : 6,
					draggable : true,
					bodyStyle : 'background:transparent;',
					html : myNewChart.generateLegend()
				});

				mainPanel.add(chartPanelLegeng);
				chartPanelLegeng.show();

				var chartPanelLegengX = mainPanelRegion.right / 2, 
					chartPanelLegengY = mainPanelRegion.bottom / 2;

				var legendPosition = chartConf.chart.legendPosition.toLowerCase();
				if (legendPosition == 'top' || legendPosition == '') {
					chartPanelLegengY = mainPanelRegion.bottom / 10;
				} else if (legendPosition == 'bottom') {
					chartPanelLegengY = mainPanelRegion.bottom * (9 / 10);
				} else if (legendPosition == 'left') {
					chartPanelLegengX = mainPanelRegion.right / 10;
				} else if (legendPosition == 'right') {
					chartPanelLegengX = mainPanelRegion.right * (9 / 10);
				}
				chartPanelLegeng.setPosition(chartPanelLegengX,	chartPanelLegengY, true);
			}

			//myNewChart.draw();
			
			/* var mainPanelIsScrollable = 
				(chartPanelCanvas.clientWidth > mainPanelRegion.right 
					|| (chartPanelCanvas.clientHeight 
							+ chartPanelTitleOrNoData.clientHeight
							+ chartPanelSubtitle.clientHeight) 
								> mainPanelRegion.bottom
							 chartPanelCanvas.getHeight() 
							+ chartPanelTitleOrNoData.getHeight() 
							+ chartPanelSubtitle.getHeight()) 
								> mainPanelRegion.bottom 
								);
				
				mainPanel.setScrollable( mainPanelIsScrollable ); */
				
		}
	};

	function handleDrilldown(e) {

	};

	function handleDrillup() {
		Sbi.chart.viewer.HighchartsDrilldownHelper.drillup();
	}

	function handleCockpitSelection(e) {
		if (!e.seriesOptions) {
			var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
			var cockpitWidgets = cockpitWidgetManager.widgets;
			var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;

			var selections = {};

			for (var i = 0; i < cockpitWidgets.getCount(); i++) {
				var widget = cockpitWidgets.get(i);

				if (Sbi.isValorized(widget) && widget.wtype === 'chart'
						&& widget.id === widgetId) {

					var fieldMeta = widget.getFieldMetaByValue(e.point.name);
					var categoryFieldHeader = fieldMeta != null ? fieldMeta.header
							: null;

					selections[categoryFieldHeader] = {
						values : [ e.point.name ]
					};

					cockpitWidgetManager.onSelection(widget, selections);
				}
			}
		}
	};

	function handleCrossNavigationTo(e) {
		if (!e.seriesOptions) {
			var chart = this;

			var categoryName = e.point.name;
			var categoryValue = null;

			if (e.point.hasOwnProperty('value')) {
				categoryValue = e.point.value;
			}
			var serieName = e.point.series.name;
			var serieValue = null;
			if (e.point.series.hasOwnProperty('value')) {
				serieValue = e.point.series.value;
			}

			var groupingCategoryName = null;
			var groupingCategoryValue = null;

			if (e.point.hasOwnProperty('group')) {
				groupingCategoryName = e.point.group.name;
				groupingCategoryValue = e.point.group.value;
			}

			if(parent.execExternalCrossNavigation){
	            	var navData={
	            			chartType:	"CHARTJS",
	            			documentName:e.point.crossNavigationDocumentName,
	            			documentParameters:e.point.crossNavigationDocumentParams,
	            			CATEGORY_NAME :categoryName,
	            			CATEGORY_VALUE :categoryValue,
	            			SERIE_NAME :serieName,
	            			SERIE_VALUE :serieValue,
	            			groupingCategoryName:groupingCategoryName,
	            			groupingCategoryValue:groupingCategoryValue,
	            			stringParameters:null
	            	};
//	             	window.parent.angular.element(window.frameElement).scope().$parent.crossNavigationHelper.navigateTo(navData); 
	            	parent.execExternalCrossNavigation(navData,{},undefined,currentDocumentLabel); 
	            }else{
				Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
					e.point.crossNavigationDocumentName,
					e.point.crossNavigationDocumentParams, categoryName,
					categoryValue, serieName, serieValue, groupingCategoryName,
					groupingCategoryValue);
	            }
			var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory
					.getChartWebServiceManager();
			chart.hideLoading();
		}
	};

	function handleCrossNavigationFrom() {
		Sbi.chart.viewer.CrossNavigationHelper.navigateBackTo();
	};
</script>
