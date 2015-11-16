/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.cockpit.widgets.extjs.piechart.PieChartWidgetRuntime', {
	extend: 'Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime'

	, config:{

	}


	, constructor : function(config) {
		Sbi.trace("[PieChartWidgetRuntime.constructor]: IN");

		this.initConfig(config);
		this.initEvents();
		this.init(config);

		this.callParent(arguments);

		this.addEvents(
			"attributeDblClick"
			, "attributeRemoved"
			, "selection"
		);

		Sbi.trace("[PieChartWidgetRuntime.constructor]: OUT");
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

	, getChartType: function() {
		return 'pie';
	}

	, getSeriesConfig: function() {

		var store = this.getStore();

	    var seriesFields = [];
		var seriesTitles = [];
		//get decimal precision and suffix to format labels
		var seriesDecimalPrecisions = [];
		var seriesSuffixes = [];

		for(var i = 0; i < this.wconf.series.length; i++) {
			var id = this.wconf.series[i].alias;
			seriesFields.push(store.fieldsMeta[id].name);
			seriesTitles.push(id);
			seriesDecimalPrecisions.push(this.wconf.series[i].precision);
			seriesSuffixes.push(this.wconf.series[i].suffix);
		}

		var series = {
			fields: seriesFields,
			titles: seriesTitles,
			decimalPrecisions: seriesDecimalPrecisions,
			suffixes: seriesSuffixes
		};

		return series;
	}

	, getCategoriesConfig: function() {

    	var store = this.getStore();

    	var categories = [];
		categories.push(this.wconf.category);
		if(this.wconf.groupingVariable) categories.push(this.wconf.groupingVariable);

		var categoriesFields = [];
		var categoriesTitles = [];
		for(var i = 0; i < categories.length; i++) {
			var id = categories[i].alias;
			categoriesFields.push(store.fieldsMeta[id].name);
			categoriesTitles.push(id);
		}

		var categories = {
			fields: categoriesFields,
			titles: categoriesTitles
		};

		return categories;
	}

	, getSeries: function( categoriesConfig, seriesConfig ) {

		Sbi.trace("[PieChartWidgetRuntime.getSeries]: IN");

		if(seriesConfig.fields.length > 1) {
			alert("[PieChartWidgetRuntime.getSeries]: Pie chart can have only one series");
		}



		var series = [{
			type: this.getChartType(),
			field: seriesConfig.fields[0],
			showInLegend: this.isLegendVisible(),
			colorSet: this.getColors(),
			highlight: {
	            segment: {
	                margin: 20
	            }
	        },
	        smooth: true,
	        label: this.getSeriesLabel(categoriesConfig, seriesConfig),
	        tips: this.getSeriesTips(seriesConfig),
	        listeners: {
	        	itemmousedown: this.onItemMouseDown,
	    	    scope: this
	    	}
	    }];



		Sbi.trace("[PieChartWidgetRuntime.getSeries]: OUT");

		return series;
	}


	, getTooltip : function(storeItem, item){

		Sbi.trace("[PieChartWidgetRuntime.getTooltip]: IN");

		var tooltip;
		var percent = this.wconf.showpercentage;
		var store = this.getStore();

		var seriresConfig = this.getSeriesConfig();

//		item = slice; series; storeItem; index; startAngle; endAngle; margin; rho; startRho; endRho; middle; shadows; sprite;
//		item.storeItem.store.data.items[j].data = id; recNo; column_1; column_2;

		var seriesum = 0;

		if (percent){
			for(var i = 0; i < store.data.items.length; i++){
				for (var h in store.data.items[i].data){
					if (h == seriresConfig.fields){
						seriesum = seriesum + parseFloat(store.data.items[i].data[h]);
					}
				}
			}
		}

		var itemMeta = this.getItemMeta(item);

		var fieldValue = itemMeta.seriesFieldValue;

		if (percent) fieldValue = (fieldValue/seriesum)*100;

		if (typeof(fieldValue) == 'number'){
			//if (this.isInteger(fieldValue)){
				
				var decimalPrecision;
				var suffix;
				
				for (var i = 0; i < seriresConfig.fields.length; i++) {
					if (itemMeta.seriesFieldName == seriresConfig.fields[i]){
						decimalPrecision = seriresConfig.decimalPrecisions[i];
						suffix = seriresConfig.suffixes[i];
						break;
					}
				}
				
				if(decimalPrecision !== undefined && decimalPrecision !== null)
				{					
					fieldValue = this.getLocalFormattedNumericValues(decimalPrecision, fieldValue);
				}
				
				if(suffix !== undefined && suffix !== null && suffix !== '')
				{
					fieldValue = fieldValue + ' ' + suffix;
				}
			//}
		}

		if (percent) fieldValue = fieldValue + "%";

		var categoryValue = itemMeta.categoryValues[0];

		tooltip =  itemMeta.seriesFieldHeader + ': ' + fieldValue + " <p> " +
				   itemMeta.categoryFieldHeaders + ': '+ categoryValue;

		Sbi.trace("[PieChartWidgetRuntime.getTooltip]: OUT");

		return tooltip;
	}

	/*
	, getTooltip : function(record, item){
		var percent = this.wconf.showpercentage;
		var allDesignSeries = this.wconf.series;
		var colors = this.getColors();
		var storeObject = item.storeItem;

		//the total sum for percentage calculation
		var seriesum=0;
		if (percent) {
			for(var j=0; j<storeObject.store.data.items.length; j++){
				seriesum += parseFloat(((storeObject.store.getAt(j)).data)['seriesflatvalue0']);
			}
		}

		var selectedSerieName = 'series0';
		var selectedSerie;
		var seriesFields = [];
		var seriesTitles = [];

		for(var i = 0; i < this.wconf.series.length; i++) {
			var id = this.wconf.series[i].alias;
			seriesFields.push(storeObject.store.fieldsMeta[id].name);
			seriesTitles.push(id);
		}


		var series = this.getChartSeries(seriesFields, colors);

		for (var i =0; i<series.length;i++){
			if (series[i].field == selectedSerieName){
				selectedSerie = series[i];
				break;
			}
		}

		var valueObj = this.getFormattedValue(record, selectedSerie, null, allDesignSeries, seriesum);

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
	*/

	, getItemMeta: function(item) {
		var itemMeta = {};

		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: IN " + Sbi.toSource(item, true));
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: IN " + Sbi.toSource(item.series, true));
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: IN yField: " + item.series.yField);
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: IN xField: " + item.series.xField);

		// selected categories: names, headers & values
		var categoriesConfig = this.getCategoriesConfig();
		itemMeta.categoryFieldNames = [categoriesConfig.fields[0]];
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected categories names are equal to [" + itemMeta.categoryFieldNames +"]");

		itemMeta.categoryFieldHeaders = [];
		for(var i = 0; i < itemMeta.categoryFieldNames.length; i++) {
			itemMeta.categoryFieldHeaders[i] = this.getFieldHeaderByName( itemMeta.categoryFieldNames[i] );
		}
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected categories headers are equal to [" + itemMeta.categoryFieldHeaders +"]");

		itemMeta.categoryValues = [];
		for(var i = 0; i < itemMeta.categoryFieldNames.length; i++) {
			itemMeta.categoryValues.push( item.storeItem.data[itemMeta.categoryFieldNames[i]] );
		}
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected categories values are equal to [" + itemMeta.categoryValues +"]");

		// selected series: name, header & value
		itemMeta.seriesFieldName = item.series.field;
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected series name is equal to [" + itemMeta.seriesFieldName +"]");

		itemMeta.seriesFieldHeader = this.getFieldHeaderByName(itemMeta.seriesFieldName);
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected series header is equal to [" + itemMeta.seriesFieldHeader +"]");

		itemMeta.seriesFieldValue = item.storeItem.data[itemMeta.seriesFieldName];
		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: selected series value is equal to [" + itemMeta.seriesFieldValue +"]");


		Sbi.trace("[PieChartWidgetRuntime.getItemMeta]: OUT");

		return itemMeta;
	}

	, getSeriesLabel: function(categoriesConfig, seriesConfig) {
		var thisPanel = this;
		var percent = this.wconf.showpercentage;
		var store = this.getStore();
		var seriresConfig = this.getSeriesConfig();

		var seriesum = 0;

		if (percent){
			for(var i = 0; i < store.data.items.length; i++){
				for (var h in store.data.items[i].data){
					if (h == seriresConfig.fields){
						seriesum = seriesum + parseFloat(store.data.items[i].data[h]);
					}
				}
			}
		}

		var label = {
			field: categoriesConfig.fields[0],
		    display: 'rotate',
		    contrast: true,
		    font: '1em Arial',
		    renderer:function(value,label, storeItem, item, i, display, animate, index){
				if (thisPanel.wconf.showvalues == true){
			    	var fieldValue = storeItem.get(seriesConfig.fields[0]);
					if (percent) fieldValue = (fieldValue/seriesum)*100;

					if (typeof(fieldValue) == 'number'){
						//if (thisPanel.isInteger(fieldValue)){
							//decimal number
							//fieldValue = +fieldValue.toFixed(2);
							fieldValue = thisPanel.getLabelValuesNumericFormat(fieldValue,label, storeItem, item, i, display, animate, index, seriesConfig);
						//}
					}

					if (percent) fieldValue = fieldValue + "%";

					return fieldValue;
				} else {
					return '';
				}


            }
			,scope:this
		};

		return label;
	}

	, getColors : function () {
		Sbi.trace("[PieChartWidgetRuntime.getColors]: IN");
		var colors;
		if (this.wconf !== undefined && this.wconf.colors) {
			Sbi.trace("[PieChartWidgetRuntime.getColors]: Using custom colors");
			colors = this.wconf.colors;
		} else {
			Sbi.trace("[PieChartWidgetRuntime.getColors]: Using default colors");
			colors = Sbi.widgets.Colors.defaultColors;
		}
		Sbi.trace("[PieChartWidgetRuntime.getColors]: Colors used for series [" + colors + "]");
		Sbi.trace("[PieChartWidgetRuntime.getColors]: OUT");
		return colors;
	}

	/**
	 * @deprecated
	 */
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

	/**
	 * @deprecated
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

		//Construct the series object(s)
		
		var chartFont = this.widgetFontConfiguration.widgetFontType;
		var chartTooltipFontSize = this.widgetFontConfiguration.tooltipFontSize;
		
		var aSerie = {
                type: 'pie',
                highlight: {
                	 segment: {margin:20}
                },
                field: seriesNames,
                label: {
                    field: 'categories',
                    display: 'outside',
                    contrast: true,
                    font: '10px Arial'
                },
    	        tips: {
	            	  trackMouse: true,
	            	  minWidth: 140,
	            	  maxWidth: 300,
	            	  width: 'auto',
	            	  minHeight: 28,
                 	  bodyStyle: 
                   	  {
                   			font: 'bold ' + chartTooltipFontSize + ' ' + chartFont
                   	  },
	            	  renderer: function(storeItem, item) {
	            		   //this.setTitle(String(item.value[0])+" : "+String(item.value[1]));
	            		   var tooltipContent = thisPanel.getTooltip(storeItem, item);
	            		   this.setTitle(tooltipContent);
	            	  }
    	        },
    	        showInLegend: showLegend
         };
		series.push(aSerie);

		return series;
	}

	, refresh:  function() {
		Sbi.trace("[PieChartWidgetRuntime.refresh]: IN");
		Sbi.cockpit.widgets.extjs.piechart.PieChartWidgetRuntime.superclass.refresh.call(this);
		Sbi.trace("[PieChartWidgetRuntime.refresh]: OUT");
	}

	, redraw: function() {
		Sbi.trace("[PieChartWidgetRuntime.redraw]: IN");
		Sbi.cockpit.widgets.extjs.piechart.PieChartWidgetRuntime.superclass.redraw.call(this);
		
		//initialize new fonts configuration -> HAVE TO BE BEFORE getSeries() method, to redraw tooltip font type
		this.initFontConfiguration();

		var seriresConfig = this.getSeriesConfig();
		var categoriesConfig = this.getCategoriesConfig();

		var series = this.getSeries( categoriesConfig, seriresConfig );

		var store = this.getStore();
		store.sort(categoriesConfig.fields[0], 'ASC');

		for(var i = 0; i < store.data.items.length; i++){
			//truncate long category labels, and riassign value inside store
			var categoryField = categoriesConfig.fields[0];
			var categoryLabel = store.data.items[i].data[categoryField];
			store.data.items[i].data[categoryField] = Ext.util.Format.ellipsis(categoryLabel,50);
		}

		if (!this.areIncomingEventsEnabled()) {
	     	var clone = Sbi.storeManager.cloneStore(this.getStore());
	     	store = clone;
	     	this.unboundStore();
		}

		this.chartPanel =  Ext.create('Ext.chart.Chart', {
            store: store,
            series: series,
            shadow: true,
            animate: true,
            theme: 'CustomBlue',
            background: this.getBackground(),
	        legend: this.isLegendVisible()? this.getLegendConfiguration(): false
        });

		this.setContentPanel(this.chartPanel);
		Sbi.trace("[PieChartWidgetRuntime.redraw]: OUT");
	}

	/*
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
    */

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	//----- Ext 4 Implementation related functions ------------------------------------------
	, getChart : function(items, colors){

		Sbi.trace("[PieChartWidgetRuntime.getChart]: IN");

		var chartDataStore = items.store;

		//Legend visibility
		var showlegend;
		if (Sbi.isValorized(this.wconf.showlegend)){
			showlegend = this.wconf.showlegend;
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
			var positionLegend = (Sbi.isValorized(this.wconf.legendPosition))? this.wconf.legendPosition:'right';
			config.legend = {position: positionLegend};
		}
		var chart = Ext.create("Ext.chart.Chart", config);

		Sbi.trace("[PieChartWidgetRuntime.getChart]: OUT");

	    return chart;
	}

	//	Format the value to display
	, getFormattedValue: function (record, series, allRuntimeSeries, allDesignSeries, seriesum){
		var showPercentage = this.wconf.showpercentage;
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
		Sbi.trace("[PieChartWidgetRuntime.onRender]: IN");

		this.msg = 'Sono un widget di tipo PieChart';

		Sbi.cockpit.widgets.extjs.piechart.PieChartWidgetRuntime.superclass.onRender.call(this, ct, position);

		Sbi.trace("[PieChartWidgetRuntime.onRender]: OUT");
	}

});

Sbi.registerWidget('piechart-ext', {
	name: 'Pie Chart'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/extjs/piechart/img/piechart_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.extjs.piechart.PieChartWidgetRuntime'
	, designerClass: 'Sbi.cockpit.widgets.piechart.PieChartWidgetDesigner'
});
