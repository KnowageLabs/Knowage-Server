/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

/**
 * Object name
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Antonella Giachino (antonella.giachino@eng.it)
 */
Ext.ns("Sbi.engines.chart");

Sbi.engines.chart.MasterDetailChartPanel = function(config) {
	
	var defaultSettings = {
	};

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	//constructor
	Sbi.engines.chart.MasterDetailChartPanel.superclass.constructor.call(this, c);
	this.init();

};

Ext.extend(Sbi.engines.chart.MasterDetailChartPanel, Sbi.engines.chart.GenericChartPanel, {

      detailSerieData:  null
	, detailStart: null
	, chartsArr : []
	, chart : null
	, detailChart: null
	, chartConfig : null 

	, init : function () {
		//show the loading mask
		if(this.rendered){
			this.showMask();
		} else{
			this.on('afterlayout',this.showMask,this);
		}
		//gets dataset values (at the moment one dataset for all charts in a document)
		var dataConfig = this.chartConfig;
		if (this.chartConfig.charts){			
			dataConfig =  Ext.apply( this.chartConfig.charts[0], dataConfig);
			delete dataConfig.charts;
		}
		this.loadChartData(dataConfig);
	}

	, createChart: function () {
		this.chartConfig.chart.renderTo = this.chartConfig.divId + '__master';
		//disable exporting buttons
		var exp = {};
		exp.enabled = false;
		this.chartConfig.exporting = exp;
		//disable credits information
		var credits = {};
		credits.enabled = false;
		this.chartConfig.credits = credits;
		this.enableDrillEvents(this.chartConfig);
		this.setFieldValuesIntoTemplate(this.chartConfig);
		
		//defines series data for the master chart
		this.defineSeriesData(this.chartConfig);
		
		//get categories for each axis  for the master chart
		this.definesCategoriesX(this.chartConfig);
		this.definesCategoriesY(this.chartConfig);
		
		//defines utc values if necessary for the master chart
		if (this.chartConfig.xAxis.plotBands !== undefined){
			var arPlotbands = [];
			var plotBand = this.chartConfig.xAxis.plotBands[0];
			
			plotBand.from = this.getUTCValue(plotBand.from);
			plotBand.to = this.getUTCValue(plotBand.to);
			plotBand.detailMaxPlotBand = this.getUTCValue(plotBand.defaultMax);
			arPlotbands.push(plotBand);
			delete this.chartConfig.xAxis.plotBands;
			this.chartConfig.xAxis.plotBands = arPlotbands;
		}

		//defines series data for the detail chart
		this.defineDetailSeriesData(this.chartConfig.detailChart);
		//get categories for each axis  for the detail chart
		this.definesDetailCategoriesX(this.chartConfig.detailChart);
		//this.definesDetailCategoriesY(this.chartConfig.detailChart);
		
		
		//getTemplateData for the detail chart
		this.detailTemplate = this.getDetailChartTemplate();
		var detailChart = new Highcharts.Chart(this.detailTemplate);
		//defines master events to manage the detail chart
		this.createMasterEvents(this.chartConfig, this.detailSerieData , detailChart);
		
		this.chart = new Highcharts.Chart(this.chartConfig);

		//saves the chart for eventually multiple export
		this.chartsArr.push(detailChart);
		this.chartsArr.push(this.chart);		
		this.hideMask();
	}

	, createMasterEvents: function(config, detailSerieData, detailChart ) {
		
		if (config.detailChart.series[0].pointStart !== undefined){			
			this.detailStart = this.getUTCValue(config.detailChart.series[0].pointStart);
			config.detailChart.series[0].pointStart = this.detailStart;
		}
		
		//gets max value for plot bands default
		var events = {
		
			// on load of the master chart, set the detail chart
			load: function() {
				//this.detailChart = new Highcharts.Chart(detailTemplate);
				this.detailChart = detailChart;
			}
			// listen to the selection event on the master chart to update the 
			// extremes of the detail chart
		   , selection: function(event) {
				var extremesObject = event.xAxis[0],
					min = extremesObject.min,
					max = extremesObject.max,
					xAxis = this.xAxis[0];

				// move the plot bands to reflect the new detail span
				xAxis.removePlotBand('mask-before');
				var pointStart = 0;
				if (config.detailChart.series[0].pointStart !== undefined ){
					pointStart = config.detailChart.series[0].pointStart;
				}
				xAxis.addPlotBand({
					id: 'mask-before',
					from: pointStart,
					to: min,
					color: 'rgba(0, 0, 0, 0.2)'
				});
				xAxis.removePlotBand('mask-after');
				var detailMaxPlotBand = 0;
				if (config.detailChart.series[0].detailMaxPlotBand !== undefined ){
					detailMaxPlotBand = config.detailChart.series[0].detailMaxPlotBand;
				}else if (config.xAxis.plotBands !== undefined && config.xAxis.plotBands[0].detailMaxPlotBand !== undefined){
					detailMaxPlotBand = config.xAxis.plotBands[0].detailMaxPlotBand;
				}
				xAxis.addPlotBand({
					id: 'mask-after',
					from: max,
					to: detailMaxPlotBand,
					color: 'rgba(0, 0, 0, 0.2)'
				});
				
				// reverse engineer the last part of the data
				this.detailSerieData = detailSerieData;				
				Ext.each( this.detailSerieData, function(chart, index) {
					var tmpDetailData = [];
				    var sers = chart.data;
				    if (Ext.isArray(sers)){
				    		for ( v in sers){
				    			var serValues = sers[v];
					    		var xValue = serValues[0];
					    		var yValue = serValues[1];
					    		if (xValue >  min && xValue < max) {
					    			tmpDetailData.push({
										x: xValue,
										y: yValue
									});
							    }
				    		}
				    }
				    this.detailChart.series[index].setData(tmpDetailData);
				}, this);
				
				return false;
			}
		}
		
		config.chart.events = events;
		
	}

	, getDetailChartTemplate: function(){
		var chartTemplate = {};
		
		var chartOptions = {
					borderWidth: 0,
					backgroundColor: null,
					renderTo: this.chartConfig.divId + '__detail', 
					margin: [80, 30, 20, 80],
					style: {}
		};
		chartTemplate.chart = chartOptions;
		
		//disable exporting buttons
		var exp = {};
		exp.enabled = false;
		chartTemplate.exporting = exp;
		
		//disable credits information
		var credits = {};
		credits.enabled = false;
		chartTemplate.credits = credits;
		
		//sets other detail chart properties
		chartTemplate.title = this.chartConfig.detailChart.title || {};
		chartTemplate.subtitle = this.chartConfig.detailChart.subtitle || {};
		chartTemplate.xAxis = this.chartConfig.detailChart.xAxis || {};
		chartTemplate.yAxis = this.chartConfig.detailChart.yAxis || {};	
		chartTemplate.tooltip = this.chartConfig.detailChart.tooltip;
		chartTemplate.legend = this.chartConfig.detailChart.legend || {};
		chartTemplate.plotOptions = this.chartConfig.detailChart.plotOptions || {};
		
		if (chartTemplate.series && chartTemplate.series[0].pointStart !== undefined){
			chartTemplate.series[0].pointStart = eval(chartTemplate.series[0].pointStart);
		}
		
		chartTemplate.series = this.detailSerieData;
		this.setFieldValuesIntoTemplate(chartTemplate);
		return chartTemplate;
	}
	
	, getUTCValue: function(elem){
		var elemValue = elem;
		if (typeof elemValue === "string" && elemValue.indexOf("UTC") >= 0){
			elemValue = elemValue.replace(/"/gi, "");
			elemValue = eval(elemValue);
		}
		return elemValue;
	}
	
	, defineDetailSeriesData: function(config){
		//gets series values and adds theme to the config
		var seriesNode = [];

		if (config.series !== undefined ){
			var serieValue = config.series;
			if (Ext.isArray(serieValue)){
				var seriesData =  {};
				var str = "";
				for(var i = 0; i < serieValue.length; i++) {
					seriesData = serieValue[i];	
					seriesData.data = this.getSeries(seriesData.alias);//values from dataset
					seriesNode.push(seriesData);
				}
			}
		}
		config.series = seriesNode;
		//this.detailSerieData = config.series[0].data;
		this.detailSerieData = config.series;
	}
	
	, definesDetailCategoriesX: function(config){
		if(config.xAxis != undefined){
			//if multiple X axis
			/*if(config.xAxis.length != undefined){
				//gets categories values and adds theme to the config	
				this.setCategoryAliasX(config);
				var categoriesX = this.getCategoriesX();
				if(categoriesX == undefined || categoriesX.length == 0){
					delete this.chartConfig.xAxis;
					for(var j =0; j< this.categoryAliasX.length; j++){
						config.xAxis[j].categories = categoriesX[j];
					}	
				
				}
				//else keep templates ones

			}else{*/
				//single axis
				this.setCategoryAliasX(config);
				var categoriesX = this.getCategoriesX();
				if(categoriesX != undefined && categoriesX.length != 0){
					config.xAxis.categories = categoriesX[0];
				}				
			//}
		}
	}
	
	, definesDetailCategoriesY: function(config){
		if(config.yAxis != undefined){
			//if multiple Y axis
			/*if(config.yAxis.length != undefined){
				//gets categories values and adds theme to the config	
				var categoriesY = this.getCategoriesY();
				if(categoriesY == undefined || categoriesY.length == 0){
					delete this.chartConfig.yAxis;
					for(var j =0; j< this.categoryAliasY.length; j++){
						config.yAxis[j].categories = categoriesY[j];
					}
					
				}
				//else keep templates ones
			}else{*/
				//single axis
				var categoriesY = this.getCategoriesY();
				if(categoriesY != undefined && categoriesY.length != 0){
					config.yAxis.categories = categoriesY[0];
				}				
			//}
		}
	}

});