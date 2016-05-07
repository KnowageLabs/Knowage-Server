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
		var mainPanel = Ext.getCmp(panelId);

		var chartPanelTitleOrNoData = Ext.create('Ext.form.Panel', {
			id : 'chartPanelTitleOrNoData',
			layout : 'vbox',
			bodyStyle : 'background:transparent;',
			border : false
		});
		mainPanel.add(chartPanelTitleOrNoData);

		var chartPanelSubtitle = Ext.create('Ext.panel.Panel', {
			id : 'chartPanelSubtitle',
			layout : 'vbox',
			bodyStyle : 'background:transparent;',
			border : false
		});
		mainPanel.add(chartPanelSubtitle);

		var chartPanelCanvas = Ext.create('Ext.Component', {
			id : 'chartPanelCanvas',
			autoEl : {
				tag : 'canvas'
			}
		});
		mainPanel.add(chartPanelCanvas);
	};

	function renderChart(chartConf) {
		var chartPanelTitleOrNoData = Ext.getCmp('chartPanelTitleOrNoData');
		var chartPanelSubtitle = Ext.getCmp('chartPanelSubtitle');
		
		var chartPanelCanvas = Ext.getCmp('chartPanelCanvas');
		var mainPanel = chartPanelCanvas.ownerCt;
		var mainPanelRegion = mainPanel.getViewRegion();

		// No data to represent
		if ((chartConf.data.labels && chartConf.data.labels.length == 0)
				|| (chartConf.data.datasets && chartConf.data.datasets.length == 0)) {

			if (chartConf.chart.emptyMessage && chartConf.chart.emptyMessage.style
					&& chartConf.chart.emptyMessage.text != '') {
				chartPanelTitleOrNoData.setLayout({
					align : chartConf.chart.emptyMessage.style.align
				});

				var emptyMessageContainerStyle = {
					padding : '5 20 5 20'
				};

				var emptyMessageStyleKeys = Object.keys(chartConf.chart.emptyMessage.style);
				for (var i = 0; i < emptyMessageStyleKeys.length; i++) {
					var emptyMessageStyleName = emptyMessageStyleKeys[i];

					if (emptyMessageStyleName.toLowerCase() != 'align') {
						emptyMessageContainerStyle[emptyMessageStyleName] = chartConf.chart.emptyMessage.style[emptyMessageStyleName];
					}
				}

				var emptyMessageContainer = Ext.create('Ext.form.Label', {
					text : chartConf.chart.emptyMessage.text,
					style : emptyMessageContainerStyle
				});

				chartPanelTitleOrNoData.add(emptyMessageContainer);
			}
			
		} else { //The are data to represent

			// title management
			if (chartConf.chart.title && chartConf.chart.title.style
					&& chartConf.chart.title.text != '') {
				chartPanelTitleOrNoData.setLayout({
					align : chartConf.chart.title.style.align
				});

				var titleContainerStyle = {
					padding : '5 20 5 20'
				};

				var titleStyleKeys = Object.keys(chartConf.chart.title.style);
				for (var i = 0; i < titleStyleKeys.length; i++) {
					var titleStyleName = titleStyleKeys[i];

					if (titleStyleName.toLowerCase() != 'align') {
						titleContainerStyle[titleStyleName] = chartConf.chart.title.style[titleStyleName];
					}
				}

				var titleContainer = Ext.create('Ext.form.Label', {
					text : chartConf.chart.title.text,
					style : titleContainerStyle
				});

				chartPanelTitleOrNoData.add(titleContainer);
			}

			// subtitle management
			if (chartConf.chart.subtitle && chartConf.chart.subtitle.style
					&& chartConf.chart.subtitle.text != '') {
				
				chartPanelSubtitle.setLayout({
					align : chartConf.chart.subtitle.style.align
				});

				var subtitleContainerStyle = {
					padding : '5 20 5 20'
				};

				var subtitleStyleKeys = Object.keys(chartConf.chart.subtitle.style);
				for (var i = 0; i < subtitleStyleKeys.length; i++) {
					var subtitleStyleName = subtitleStyleKeys[i];

					if (subtitleStyleName.toLowerCase() != 'align') {
						subtitleContainerStyle[subtitleStyleName] = chartConf.chart.subtitle.style[subtitleStyleName];
					}
				}

				var subtitleContainer = Ext.create('Ext.form.Label', {
					text : chartConf.chart.subtitle.text,
					style : subtitleContainerStyle
				});

				chartPanelSubtitle.add(subtitleContainer);
			}

			var chartType = chartConf.chart.type.toLowerCase();

			// Sets the dimensions
			var canvasHeight = mainPanel.getHeight();
			var canvasWidth = mainPanel.getWidth();

			if (chartConf && chartConf.chart && chartConf.chart.height) {
				canvasHeight = chartConf.chart.height;
			}
			if (chartConf && chartConf.chart && chartConf.chart.width) {
				canvasWidth = chartConf.chart.width;
			}

			canvasHeight = canvasHeight	- 
				(chartPanelTitleOrNoData.getHeight() + chartPanelSubtitle.getHeight());

			chartPanelCanvas.setHeight(canvasHeight);
			chartPanelCanvas.setWidth(canvasWidth);

			// Gets the context of the canvas element we want to select
			var ctx = document.getElementById("chartPanelCanvas").getContext("2d");

			//Sets the background color
			if (chartConf.chart.backgroundColor
					&& chartConf.chart.backgroundColor != '') {
				
				mainPanel.setStyle('backgroundColor', chartConf.chart.backgroundColor);
			}

			var myNewChart;
			if (chartType == 'line') {
				myNewChart = new Chart(ctx).Line(chartConf.data, chartConf.options);
			} else if (chartType == 'pie') {
				myNewChart = new Chart(ctx).Pie(chartConf.data, chartConf.options);
			} else { // bar
				myNewChart = new Chart(ctx).Bar(chartConf.data, chartConf.options);
			}

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

			myNewChart.draw();
			
			var mainPanelIsScrollable = 
				(chartPanelCanvas.getWidth() > mainPanelRegion.right 
					|| (
							chartPanelCanvas.getHeight() 
							+ chartPanelTitleOrNoData.getHeight() 
							+ chartPanelSubtitle.getHeight()) 
								> mainPanelRegion.bottom);
				
				mainPanel.setScrollable( mainPanelIsScrollable );
				
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
	            	parent.execExternalCrossNavigation(navData); 
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
