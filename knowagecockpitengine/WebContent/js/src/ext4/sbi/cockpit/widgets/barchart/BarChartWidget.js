/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.barchart");

Sbi.cockpit.widgets.barchart.BarChartWidget = function(config) {
	Sbi.trace("[BarChartWidget.constructor]: IN");
	var defaultSettings = {

	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.barchart.BarChartWidget', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	Sbi.cockpit.widgets.barchart.BarChartWidget.superclass.constructor.call(this, c);
	this.init();

	this.addEvents('selection');

	Sbi.trace("[BarChartWidget.constructor]: OUT");

};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.barchart.BarChartWidget, Sbi.cockpit.widgets.chart.AbstractChartWidget, {
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	  chartDivId : null
	, chart : null
	, chartConfig : null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, redraw: function() {
		Sbi.trace("[BarChartWidget.redraw]: IN");
		this.createChart();
		Sbi.trace("[BarChartWidget.redraw]: OUT");
	}

    , refresh:  function() {
    	Sbi.trace("[BarChartWidget.refresh]: IN");
    	this.init();
    	this.createChart();
		Sbi.trace("[BarChartWidget.refresh]: OUT");
	}


	, createChart: function () {

		var retriever = new Sbi.cockpit.widgets.chart.DefaultChartDimensionRetrieverStrategy();
		var size = retriever.getChartDimension(this);
		this.update(' <div id="' + this.chartDivId + '" style="width: ' + size.width + '; height: ' + size.height + ';"></div>');
		var percent = ((this.chartConfig.type).indexOf('percent')>=0);
		var storeObject = this.getJsonStore(percent);
		var colors = this.getColors();
		var extraStyle ={};

		var items = {
				store: storeObject.store,
				extraStyle: extraStyle,
				style: 'height: 85%;',
				hiddenseries: new Array(),
				horizontal: this.chartConfig.orientation === 'horizontal'
		};

		//set the height if ie
		if(Ext.isIE){
			items.height = this.ieChartHeight;
		}

		if(this.chartConfig.orientation === 'horizontal'){
			items.yField = 'categories';
			items.series = this.getChartSeries(storeObject.serieNames, colors, true);

		}else{
			items.xField = 'categories';
			items.series = this.getChartSeries(storeObject.serieNames, colors);
		}

		this.addChartConf(items);


		items.region = 'center';

		var barChartPanel = this.getChart(this.chartConfig.orientation === 'horizontal', items, colors, percent);

	}
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	//----- Ext 4 Implementation related functions ------------------------------------------
	, getChart : function(horizontal, items, colors, percent){

		var chartDataStore = items.store;

		var chartType;
		var isStacked = false;

		//Define Ext4 Chart appropriate type
		if(horizontal){
			if(this.chartConfig.type == 'stacked-barchart' || this.chartConfig.type == 'percent-stacked-barchart'){
				chartType = 'bar';
				isStacked = true;
			}else{
				chartType = 'bar';
			}
		} else {
			if(this.chartConfig.type == 'stacked-barchart' || this.chartConfig.type == 'percent-stacked-barchart'){
				chartType = 'column';
				isStacked = true;
			}else{
				chartType = 'column';
			}
		}
		//Create Axes Configuration
		var chartAxes = this.createAxes(horizontal, items, percent);
		//Create Series Configuration
		var chartSeries = this.createSeries(horizontal, items, chartType, isStacked);

		//Legend visibility
		var showlegend;
		if (this.chartConfig.showlegend !== undefined){
			showlegend = this.chartConfig.showlegend;
		} else {
			showlegend = true;
		}

		//Create theme for using custom defined colors
		Ext.define('Ext.chart.theme.CustomTheme', {
		    extend: 'Ext.chart.theme.Base',

		    constructor: function(config) {
		        this.callParent([Ext.apply({
		            colors: colors
		        }, config)]);
		    }
		});

	    var chart = Ext.create("Ext.chart.Chart", {
	        width: '100%',
	    	height: '100%',
	    	theme: 'CustomTheme',
	        hidden: false,
	        title: "My Chart",
	        renderTo: this.chartDivId,
	        layout: "fit",
	        style: "background:#fff",
	        animate: true,
	        store: chartDataStore,
	        shadow: true,
	        legend: showlegend,
	        axes: chartAxes,
	        series: chartSeries

	    });
//	    chart.on('selection', this.pippo, this);
	    return chart;
	}

//	,pippo:function(){
//		alert('pippo');
//	}
	/*
	 * Create the Series object configuration
	 */
	, createSeries : function(horizontal,items, chartType, isStacked){
		var thisPanel = this;
		var axisPosition;
		var series = [];

		if (horizontal){
			//bar chart
			axisPosition = 'bottom';
		} else {
			//column chart
			axisPosition = 'left';
		}

		var seriesNames = [];
		var displayNames = [];


		//Extract technical series names and corresponding name to display
		for (var i=0; i< items.series.length; i++){
			var name;
			if (horizontal){
				name = items.series[i].xField;
			} else {
				name = items.series[i].yField;
			}
			seriesNames.push(name);
			var displayName = items.series[i].displayName;
			displayNames.push(displayName);
		}

		//Costruct the series object(s)
		var aSerie = {
                type: chartType,
                highlight: {
                    size: 7,
                    radius: 7
                },
                axis: axisPosition,
                smooth: true,
                stacked: isStacked,
                xField: "categories",
                yField: seriesNames,
                title: displayNames,
    	        tips: {
	            	  trackMouse: true,
	            	  minWidth: 140,
	            	  maxWidth: 300,
	            	  width: 'auto',
	            	  minHeight: 28,
	            	  renderer: function(storeItem, item) {
	            		   //this.setTitle(String(item.value[0])+" : "+String(item.value[1]));
	            		   var tooltipContent = thisPanel.getTooltip(storeItem, item);
	            		   this.setTitle(tooltipContent);
	            	  }
    	        },
    	        listeners: {
		  			itemmousedown:function(obj) {
		  				var categoryField ;
		  				var valueField ;
		  				categoryField = obj.storeItem.data[obj.series.xField];
//		  				valueField = obj.storeItem.data[obj.yField];
		  				valueField = obj.storeItem.data[obj.series.xField];
//		  				alert(displayNames + ' - ' + categoryField + ' - ' + valueField);
	  		    		var selections = {};
		  				var values =  [];
		  				selections[displayNames] = {};
	  		    		selections[displayNames].values = values; //manage multi-selection!
	  		    		Ext.Array.include(selections[displayNames].values, valueField);
	  		    		thisPanel.fireEvent('selection', thisPanel, selections);
		  			}
			}

         };
		series.push(aSerie);

		return series;
	}
	/*
	 * Create the Axes object configuration
	 */
	, createAxes : function(horizontal,items,percent){
		var axes;
		var positionNumeric;
		var positionCategory;

		if (horizontal){
			//bar chart
			positionNumeric = 'bottom';
			positionCategory = 'left';
		} else {
			//column chart
			positionNumeric = 'left';
			positionCategory = 'bottom';
		}

		var seriesNames = [];

		for (var i=0; i< items.series.length; i++){
			var name;
			if (horizontal){
				name = items.series[i].xField;
			} else {
				name = items.series[i].yField;
			}
			seriesNames.push(name);
		}

		axes = [{
			type: "Numeric",
			minimum: 0,
			position: positionNumeric,
			fields: seriesNames,
//			title: "Series",
			minorTickSteps: 1,
			grid: true
		}, {
			type: "Category",
			position: positionCategory,
			fields: ["categories"]
//			title: "Category"
		}];

		//For the percent type chart set the axes scale maximum to 100
		if (percent){
			axes[0].maximum = 100;
		}

		return axes;
	}

	, getTooltip : function(record, item){
		var chartType = this.chartConfig.designer;
		var allRuntimeSeries = this.getRuntimeSeries();
		var allDesignSeries = this.chartConfig.series;
		var type = this.chartConfig.type;
		var horizontal = this.chartConfig.orientation === 'horizontal';
		var colors = this.getColors();
		var series;

		var percent = ((this.chartConfig.type).indexOf('percent')>=0);
		var storeObject = this.getJsonStore(percent);

		var selectedSerieName = item.yField;

		var selectedSerie;

		if(horizontal){
			series = this.getChartSeries(storeObject.serieNames, colors, true);
			for (var i =0; i<series.length;i++){
				if (series[i].xField == selectedSerieName){
					selectedSerie = series[i];
					break;
				}
			}

		}else{
			series = this.getChartSeries(storeObject.serieNames, colors);

			for (var i =0; i<series.length;i++){
				if (series[i].yField == selectedSerieName){
					selectedSerie = series[i];
					break;
				}
			}
		}


		var valueObj = this.getFormattedValue(null, record, selectedSerie, chartType, allRuntimeSeries, allDesignSeries, type, horizontal);

		var tooltip = '';

		if (valueObj.measureName !== valueObj.serieName) {
			tooltip = valueObj.serieName + '<br/>' + record.data.categories + '<br/>';
			// in case the serie name is different from the measure name, put also the measure name
			//tooltip += this.formatTextWithMeasureScaleFactor(valueObj.measureName, valueObj.measureName) + ' : ';
		} else {
			tooltip =  record.data.categories + '<br/>' + selectedSerie.displayName + ' : ' ;
		}
		tooltip += valueObj.value;

		return tooltip;

	}


	///---------------------------------------------------------------------


	, getChartSeries: function(serieNames, colors, horizontal){
		var seriesForChart = new Array();
		for(var i=0; i<serieNames.length; i++){
			var serie = {
	                style: {}
			};

//			if(this.chartConfig.type == 'percent-stacked-barchart'){
//				serie.displayName =  (serieNames[i]);//if percent doesn't matter the scale
//			}else{
				//serie.displayName =  this.formatLegendWithScale(serieNames[i]); //Commented by MC
//			}
			serie.displayName =  serieNames[i];

			if(horizontal){
				serie.xField = 'series'+i;
			}else{
				serie.yField = 'series'+i;
			}

			if(colors!=null){
				serie.style.color= colors[i];
			}

			seriesForChart.push(serie);
		}
		return seriesForChart;
	}

	//used for tooltip
	, getFormattedValue: function (chart, record, series, chartType, allRuntimeSeries, allDesignSeries, type, horizontal){
		var theSerieName  = series.displayName;
		var value ;
		var serieName;  // the serie name without eventual scale factor
		var measureName;  // the measure related to the serie
		var serieDefinition;  // the design-time serie definition (the measure with precision, color, ....)

		if(type != 'percent-stacked-barchart'){
			if(horizontal){
				value =  record.data[series.xField];
			}else{
				value = record.data[series.yField];
			}
		}else{
			//value = Ext.util.Format.number(record.data[series.xField], '0.00');
			if(horizontal){
				value = record.data['seriesflatvalue'+series.xField.substring(series.xField.length-1)];
			}else{
				value = record.data['seriesflatvalue'+series.yField.substring(series.yField.length-1)];
			}
		}

		// find the measure's name
		var i = 0;
		for (; i < allRuntimeSeries.length; i++) {
			//substring to remove the scale factor
			if (allRuntimeSeries[i].name === theSerieName.substring(0, allRuntimeSeries[i].name.length)) {
				serieName = allRuntimeSeries[i].name;
				measureName = allRuntimeSeries[i].measure;
				break;
			}
		}

		i = 0;
		// find the serie's (design-time) definition
		for (; i < allDesignSeries.length; i++) {
			if (allDesignSeries[i].id === measureName) {
				serieDefinition = allDesignSeries[i];
				break;
			}
		}

		// format the value according to serie configuration
		value = Sbi.commons.Format.number(value, {
    		decimalSeparator: Sbi.locale.formats['float'].decimalSeparator,
    		decimalPrecision: serieDefinition.precision,
    		groupingSeparator: (serieDefinition.showcomma) ? Sbi.locale.formats['float'].groupingSeparator : '',
    		groupingSize: 3,
    		currencySymbol: '',
    		nullValue: ''
		});

		// add suffix
		if (serieDefinition.suffix !== undefined && serieDefinition.suffix !== null && serieDefinition.suffix !== '') {
			value = value + ' ' + serieDefinition.suffix;
		}

		var toReturn = {};
		toReturn.value = value;
		toReturn.serieName = serieName;
		toReturn.measureName = measureName;
		return toReturn;
	}


	//------------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	, onRender: function(ct, position) {
		Sbi.trace("[BarChartWidget.onRender]: IN");

		this.msg = 'Sono un widget di tipo BarChart';

		Sbi.cockpit.widgets.barchart.BarChartWidget.superclass.onRender.call(this, ct, position);

		Sbi.trace("[BarChartWidget.onRender]: OUT");
	}

	, getByteArraysForExport: function(){
		var byteArrays = new Array();
		for(var i=0; i<this.charts; i++){
			byteArrays.push((this.charts[i]).exportPNG());
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------
	, init : function () {
		this.chartConfig = this.wconf;

		this.loadChartData({
			'rows':[this.chartConfig.category]
			, 'measures': this.chartConfig.series
			, 'columns': this.chartConfig.groupingVariable ? [this.chartConfig.groupingVariable] : []
		});
	}

});

/*
Sbi.registerWidget('barchart', {
	name: 'Bar Chart'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/barchart/barchart_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.barchart.BarChartWidget'
	, designerClass: 'Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner'
	//, designerClass: 'Ext.Panel'
});
*/
