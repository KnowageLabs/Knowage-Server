/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.cockpit.widgets.piechart.PieChartWidget', {
	extend: 'Sbi.cockpit.widgets.chart.AbstractChartWidget'

	, config:{

		storeId: null
		, wconf: null

		, chartConfig : null
		, chartDivId : null
		, chart : null

		,  border: false
	}


	, constructor : function(config) {
		Sbi.trace("[PieChartWidget.constructor]: IN");

		this.initConfig(config);
		this.initEvents();
		this.init(config);

		this.callParent(arguments);

		this.addEvents(
			"attributeDblClick"
			, "attributeRemoved"
			, "selection"
		);

		Sbi.trace("[PieChartWidget.constructor]: OUT");
	}

	, initComponent: function() {
        this.callParent();
    }

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, redraw: function() {
		Sbi.trace("[PieChartWidget.redraw]: IN");
		this.createChart();
		Sbi.trace("[PieChartWidget.redraw]: OUT");
	}

    , refresh:  function() {
    	Sbi.trace("[PieChartWidget.refresh]: IN");
    	this.init();
    	this.createChart();
		Sbi.trace("[PieChartWidget.refresh]: OUT");
	}

    , createChart: function () {
    	var retriever = new Sbi.cockpit.widgets.chart.DefaultChartDimensionRetrieverStrategy();
		var size = retriever.getChartDimension(this);
		this.update(' <div align=\"center\" id="' + this.chartDivId + '" style="padding-top:0px;padding-bottom:0px;width: ' + size.width + '; height: ' + size.height + ';"></div>');

		var storeObject = this.getJsonStore();
		var colors = this.getColors();

		var extraStyle ={};

		var items = {
				store: storeObject.store,
				extraStyle: extraStyle,
				style: 'height: 70%;',
				hiddenseries: new Array()
		};

	    items.series = this.getChartSeries(storeObject.serieNames, colors);

		//configuration (legend and values)
    	this.addChartConf(items);

		var titlePanel = new Ext.Panel({
			border: false,
			anchor: '100% 10%',
			html: '<div style=\"padding-top:0px; color:rgb(46,69,91);\" align=\"center\"><font size=\"4\"><b>'+storeObject.serieNames[0]+'</b></font></div>'
		});
		Sbi.trace('Title created.');

		var pieChartPanel = this.getChart(items, colors);
		Sbi.trace('Piechart created.');


		new Ext.Panel({
			renderTo : this.chartDivId,
			border: false,
			width:'100%',
			height:'100%',
			layout: 'anchor',
			items: [titlePanel, pieChartPanel]
		});
	}
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	//----- Ext 4 Implementation related functions ------------------------------------------
	, getChart : function(items, colors){

		var chartDataStore = items.store;

		//Legend visibility
		var showlegend;
		if (Sbi.isValorized(this.chartConfig.showlegend)){
			showlegend = this.chartConfig.showlegend;
		} else {
			showlegend = true;
		}

		//Create Series Configuration
		var chartSeries = this.createSeries(items, showlegend);

		//Create theme for using custom defined colors
		Ext.define('Ext.chart.theme.CustomTheme', {
		    extend: 'Ext.chart.theme.Base',

		    constructor: function(config) {
		        this.callParent([Ext.apply({
		            colors: colors
		        }, config)]);
		    }
		});

		var config = {
		    	theme: 'CustomTheme',
		        hidden: false,
		        title: "My Chart",
		        renderTo: this.chartDivId,
		        anchor: '100% 90%',
		        style: "background:#fff",
		        animate: true,
		        store: chartDataStore,
		        series: chartSeries,
		        categoryField: 'categories'
		};


		if (showlegend){
			var positionLegend = (Sbi.isValorized(this.chartConfig.legendPosition))? this.chartConfig.legendPosition:'right';
			config.legend = {position: positionLegend};
		}
		var chart = Ext.create("Ext.chart.Chart", config);

	    return chart;
	}

	, getChartSeries: function(serieNames, colors){
		var seriesForChart = new Array();
		for(var i=0; i<serieNames.length; i++){
			var serie = {
	                style: {}
			};

			serie.type = 'pie';
			serie.displayName =  this.formatLegendWithScale(serieNames[i]); //serieNames[i];
			serie.field ='series'+i;

			if(colors!=null){
				serie.style.color= colors[i];
			}

			seriesForChart.push(serie);
		}
		return seriesForChart;
	}

	/*
	 * Create the Series object configuration
	 */
	, createSeries : function(items, showLegend){
		var thisPanel = this;
		var series = [];

		var seriesNames = [];
		var displayNames = [];


		//Extract technical series names and corresponding name to display
		var displayName = '';
		for (var i=0; i< items.series.length; i++){
			var name = items.series[i].field;
			seriesNames.push(name);
			displayName = items.series[i].displayName;
			displayNames.push(displayName);
		}

		//Costruct the series object(s)
		var aSerie = {
                type: 'pie',
                highlight: {
                	 segment: {margin:20}
                },
                field: seriesNames,
                label: {
                    field: 'categories',
                    display: 'rotate',
                    contrast: true,
                    font: '0px Arial'
                },
//                title: displayNames,
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
    	        showInLegend: showLegend,
    	        listeners: {
    		  			itemmousedown:function(obj) {
    		  				var categoryField ;
    		  				var valueField ;
    		  				categoryField = obj.storeItem.data[obj.series.label.field];
//    		  				valueField = obj.slice.value;
    		  				valueField =categoryField;
    		  				var selections = {};
    		  				var values =  [];
    		  				selections[displayName] = {};
		  		    		selections[displayName].values = values; //manage multi-selection!
		  		    		Ext.Array.include(selections[displayName].values, valueField);
		  		    		thisPanel.fireEvent('selection', thisPanel, selections);
    		  			}
    			}
         };
		series.push(aSerie);

		return series;
	}



//	, getTooltipFormatter: function () {
//		var showPercentage = this.chartConfig.showpercentage;
//		var allSeries = this.chartConfig.series;
//
//		var getFormattedValue = this.getFormattedValue;
//
//		var toReturn = function (chart, record, index, series) {
//
//			var valuePrefix= '';
//			var valueSuffix = '';
//
//			var value = getFormattedValue(chart, record, series, allSeries);
//
//			valuePrefix = record.data.categories+'\n';
//
//			if(showPercentage){
//				valueSuffix = '\n'+ +Ext.util.Format.number(100*record.data['series'+chart.serieNumber]/ chart.seriesum, '0.00') + '%';
//			}
//
//			return valuePrefix+value+valueSuffix;
//
//		};
//		return toReturn;
//	}

//	Format the value to display
	, getFormattedValue: function (record, series, allRuntimeSeries, allDesignSeries, seriesum){
		var showPercentage = this.chartConfig.showpercentage;
		var theSerieName  = series.displayName;
		var value ;
		var serieDefinition;

		value = record.data['series0'];

		theSerieName = series.displayName;

		// find the serie configuration
		for (var i = 0; i < allDesignSeries.length; i++) {
			//substring to remove the scale factor
			if (allDesignSeries[i].seriename === theSerieName.substring(0, allDesignSeries[i].seriename.length)) {
				serieDefinition = allDesignSeries[i];
				break;
			}
		}

		// format the value according to serie configuration
		if(showPercentage){
			value = Ext.util.Format.number(100*value/ seriesum, '0.00') + '%';
		}else{
			value = Sbi.commons.Format.number(value, {
				decimalSeparator: Sbi.locale.formats['float'].decimalSeparator,
				decimalPrecision: serieDefinition.precision,
				groupingSeparator: (serieDefinition.showcomma) ? Sbi.locale.formats['float'].groupingSeparator : '',
						groupingSize: 3,
						currencySymbol: '',
						nullValue: ''
			});
		}

		// add suffix
		if (serieDefinition.suffix !== undefined && serieDefinition.suffix !== null && serieDefinition.suffix !== '') {
			value = value + ' ' + serieDefinition.suffix;
		}

		var toReturn = {};
		toReturn.value = value;

		return toReturn;
	}

	//------------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	, onRender: function(ct, position) {
		Sbi.trace("[PieChartWidget.onRender]: IN");

		this.msg = 'Sono un widget di tipo PieChart';

		Sbi.cockpit.widgets.piechart.PieChartWidget.superclass.onRender.call(this, ct, position);

		Sbi.trace("[PieChartWidget.onRender]: OUT");
	}

	, getColors : function () {
		return this.chartConfig.colors;
	}

	, getTooltip : function(record, item){
		var percent = this.chartConfig.showpercentage;
		var allRuntimeSeries = this.getRuntimeSeries();
		var allDesignSeries = this.chartConfig.series;
		var type = this.chartConfig.type;
		var colors = this.getColors();
		var storeObject = this.getJsonStore(percent);
		var series;
		//the total sum for percentage calculation
		var seriesum=0;
		if (percent) {
			for(var j=0; j<storeObject.store.data.items.length; j++){
//				seriesum += parseFloat(((storeObject.store.getAt(j)).data)['series0']);
				seriesum += parseFloat(((storeObject.store.getAt(j)).data)['seriesflatvalue0']);
			}
		}

		var selectedSerieName = 'series0';
		var selectedSerie;

		series = this.getChartSeries(storeObject.serieNames, colors);

		for (var i =0; i<series.length;i++){
			if (series[i].field == selectedSerieName){
				selectedSerie = series[i];
				break;
			}
		}

		var valueObj = this.getFormattedValue(record, selectedSerie, allRuntimeSeries, allDesignSeries, seriesum);

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

	// -----------------------------------------------------------------------------------------------------------------
	// init methods
	// -----------------------------------------------------------------------------------------------------------------

	, init : function () {
		this.chartConfig = this.wconf;
		this.loadChartData({
			rows: [this.chartConfig.category]
			,measures: this.chartConfig.series
		});
	}

});

/*
Sbi.registerWidget('piechart', {
	name: 'Pie Chart'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/piechart/img/piechart_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.piechart.PieChartWidget'
	, designerClass: 'Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner'
});
*/
