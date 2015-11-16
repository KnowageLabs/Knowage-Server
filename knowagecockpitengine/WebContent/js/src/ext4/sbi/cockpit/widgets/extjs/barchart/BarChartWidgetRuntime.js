/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.widgets.extjs.barchart");

Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime = function(config) {
	Sbi.trace("[BarChartWidgetRuntime.constructor]: IN");

	var defaultSettings = {

	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	var categories = [];
	categories.push(this.wconf.category);
	if(this.wconf.groupingVariable) categories.push(this.wconf.groupingVariable);

	Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime.superclass.constructor.call(this, c);

	this.boundStore();
	this.addEvents('selection');

	Sbi.trace("[BarChartWidgetRuntime.constructor]: OUT");

};

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime, Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime, {
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	// no props for the moment


    // =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
	// cartesian chart shared methods
	// -----------------------------------------------------------------------------------------------------------------
	getSeriesConfig: function() {
		Sbi.trace("[BarChartWidgetRuntime.getSeriesConfig]: IN");

		var store = this.getStore();

	    var seriesFields = [];
		var seriesTitles = [];
		//get decimal precision and suffix to format labels
		var seriesDecimalPrecisions = [];
		var seriesSuffixes = [];

//		alert(this.wconf.series.length);

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
			position: this.isHorizontallyOriented()? 'bottom' : 'left',
			decimalPrecisions: seriesDecimalPrecisions,
			suffixes: seriesSuffixes
		};

		Sbi.trace("[BarChartWidgetRuntime.getSeriesConfig]: OUT");

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
//				var id = categories[i].id;
				var id = categories[i].alias;
				categoriesFields.push(store.fieldsMeta[id].name);
				categoriesTitles.push(id);
			}

			var categories = {
				fields: categoriesFields,
				titles: categoriesTitles,
				position: this.isHorizontallyOriented()? 'left': 'bottom'
			};

			return categories;
	}

	, getOrientation: function() {
		return this.wconf? this.wconf.orientation: null;
	}

	, isVerticallyOriented: function() {
		return this.getOrientation() === 'vertical';
	}

	, isHorizontallyOriented: function() {
		return this.getOrientation() === 'horizontal';
	}

	, isValuesVisibles: function(){
		return this.wconf? this.wconf.showvalues : null;
	}

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------



	, getChartType: function() {
		return this.isHorizontallyOriented()? 'bar': 'column';
	}

	, isStacked: function() {
		return (this.wconf.type == 'stacked-barchart' || this.wconf.type == 'percent-stacked-barchart');
		//return (this.wconf.type == 'stacked-barchart');
	}

	, isPercentStacked: function() {
		return this.wconf.type == 'percent-stacked-barchart';
	}

	, getMeasureToSort: function(){
		var store = this.getStore();
		var sortCriteria = [];
		for(var i = 0; i < this.wconf.series.length; i++) {
			var serie = this.wconf.series[i];
			if ((serie.sortMeasure != undefined) && (serie.sortMeasure != false)){
				var id = this.wconf.series[i].alias;
				sortCriteria.push(store.fieldsMeta[id].name);
				break;
			}
		}
		return sortCriteria;
	}

    , refresh:  function() {
    	Sbi.trace("[BarChartWidgetRuntime.refresh]: IN");
    	Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime.superclass.refresh.call(this);
    	Sbi.trace("[BarChartWidgetRuntime.refresh]: OUT");
	}

	, redraw: function () {
		Sbi.trace("[BarChartWidgetRuntime.redraw]: IN");

		Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime.superclass.redraw.call(this);
		
		//initialize new fonts configuration -> HAVE TO BE BEFORE getSeries() method, to redraw tooltip font type
		this.initFontConfiguration();
		//font vars used by theme constructor
		var chartFont = this.widgetFontConfiguration.widgetFontType;
		var chartAxisTitleFontSize = this.widgetFontConfiguration.axisTitleFontSize;
		var chartAxisLabelsFontSize = this.widgetFontConfiguration.axisLabelsFontSize;

		var seriresConfig = this.getSeriesConfig();
		var categoriesConfig =  this.getCategoriesConfig();

		var axes = this.getAxes( categoriesConfig, seriresConfig );
		var series = this.getSeries( categoriesConfig, seriresConfig );

		var store = this.getStore();

		var colors = this.getColors();

		for(var i = 0; i < store.data.items.length; i++){
			var seriesum = 0;

			if(this.isPercentStacked()){
				for(var j = 0; j < seriresConfig.fields.length; j++){
					for (var h in store.data.items[i].data){
						if (h == seriresConfig.fields[j]){
							seriesum = seriesum + parseFloat(store.data.items[i].data[h]);
						}
					}
				}

				for(var j = 0; j < seriresConfig.fields.length; j++){
					for (var h in store.data.items[i].data){
						if (h == seriresConfig.fields[j]){
							if (seriesum != 0){
								store.data.items[i].data[h] = parseFloat((store.data.items[i].data[h]/seriesum)*100);
							}
						}
					}
				}
			}

			//truncate long category labels, and riassign value inside store
			var categoryField = categoriesConfig.fields[0];
			var categoryLabel = store.data.items[i].data[categoryField];
			store.data.items[i].data[categoryField] = Ext.util.Format.ellipsis(categoryLabel,50);
		}
		var sortMeasure = this.getMeasureToSort();
		var sortOrder = this.getSortOrder();
		if (sortMeasure.length > 0){
			store.sort(sortMeasure[0], sortOrder);
		} else {
			store.sort(categoriesConfig.fields[0], sortOrder);
		}

		if (!this.areIncomingEventsEnabled()) {
	     	var clone = Sbi.storeManager.cloneStore(this.getStore());
	     	store = clone;
	     	this.unboundStore();
		}

		//Create theme for using custom defined colors
		Ext.define('Ext.chart.theme.CustomTheme', {
		    extend: 'Ext.chart.theme.CustomBlue',

		    constructor: function(config) {
		    	
		    	var titleLabel = {
		                font: 'bold ' + chartAxisTitleFontSize + ' ' + chartFont
		            }, axisLabel = {
		                fill: 'rgb(8,69,148)',
		                font: chartAxisLabelsFontSize + ' ' + chartFont,
		                spacing: 2,
		                padding: 5
		            };
		    	
		    	
		        this.callParent([Ext.apply({
		            colors: colors,
		            axisLabelLeft: axisLabel,
	                axisLabelBottom: axisLabel,
	                axisTitleLeft: titleLabel,
	                axisTitleBottom: titleLabel
		        }, config)]);
		    }
		});

		this.chartPanel =  Ext.create('Ext.chart.Chart', {
            store: store,
            axes: axes,
            series: series,
            shadow: true,
            animate: true,
            theme: 'CustomTheme',
            background: this.getBackground(),
	        legend: this.isLegendVisible()? this.getLegendConfiguration(): false
        });

		this.setContentPanel(this.chartPanel);

		Sbi.trace("[BarChartWidgetRuntime.redraw]: OUT");
	}

	, getBackground: function() {
		var background = {
		    gradient: {
			    id: 'backgroundGradient',
			    angle: 45,
			    stops: {
				    0: {color: '#ffffff'},
				    100: {color: '#ffffff'},
				    //100: {color: '#eaf1f8'}
				}
			}
		};
		return background;
	}

	, getAxes: function( categoriesConfig, seriesConfig ) {
		var seriesTitle;
		if (this.getSeriesAxis().length > 0){
			seriesTitle = this.getSeriesAxis();
		} else {
			seriesTitle = seriesConfig.titles.length == 1? seriesConfig.titles[0]: undefined;
		}
		var seriesAxis = {
		    type: 'Numeric'
		    , position: seriesConfig.position
		    , fields: seriesConfig.fields
		    , minorTickSteps: 1 // The number of small ticks between two major ticks. Default is zero.
		    , label: {
		    	renderer: this.getChartsNumericFormat(0)
		    }
			, title: this.isSeriesAxisNameVisible()? seriesTitle : ''
		   	, grid: true
		    , minimum: 0
		};

		//For the percent type chart set the axes scale maximum to 100
		if(this.isPercentStacked()) {
			seriesAxis.maximum = 100;
			seriesAxis.stacked = true;
		}

		var categoryTitle;
		if (this.getCategoryAxis().length > 0){
			categoryTitle = this.getCategoryAxis();
		} else {
			categoryTitle = categoriesConfig.titles.length == 1? categoriesConfig.titles[0]: undefined;
		}
		var categoryAxis = {
		    type: 'Category'
		    , position: categoriesConfig.position
		    , fields: categoriesConfig.fields
		    , title: this.isCategoryAxisNameVisible()? categoryTitle : ''
	    };

		var axes = [seriesAxis, categoryAxis];

		return axes;
	}

	, getSeries: function( categoriesConfig, seriesConfig ) {

		Sbi.trace("[BarChartWidgetRuntime.getSeries]: IN");

		var series = [{
			type: this.getChartType(),
			stacked: this.isStacked(),
			title: seriesConfig.titles,
            highlight: {
            	size: 7,
                radius: 7
            },
            axis: seriesConfig.position,
            smooth: true,
            tips: this.getSeriesTips(seriesConfig),
            label: this.getSeriesLabel(seriesConfig),
            xField: categoriesConfig.fields,
            yField: seriesConfig.fields,
            listeners: {
    	    	itemmousedown: this.onItemMouseDown,
    	    	scope: this
    	    }
        }];

		Sbi.trace("[BarChartWidgetRuntime.getSeries]: OUT");

		return series;
	}

	, getSeriesTips: function(seriesConfig) {
		var thisPanel = this;
		
		var chartFont = this.widgetFontConfiguration.widgetFontType;
		var chartTooltipFontSize = this.widgetFontConfiguration.tooltipFontSize;

		var tips =  {
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
           		var tooltipContent = thisPanel.getTooltip(storeItem, item, seriesConfig);
           		//this.setTitle(tooltipContent);
           		//now tooltip content is in the body, cause of in the title the style it's not applied
           		this.update(tooltipContent);
            }
        };

		return tips;
	}

	, getSeriesLabel: function(seriesConfig) {
		
		thePanel = this;
		
		var label = {
			//insideEnd is the only options that work with stacked!
            display: this.isStacked()? 'insideEnd':'outside',
            field: this.isValuesVisibles()?( seriesConfig.fields.length == 1? seriesConfig.fields[0]: seriesConfig.fields) : null,
            //renderer: Ext.util.Format.numberRenderer('0.##'),getChartsNumericFormat
            renderer: function(v, label, storeItem, item, i, display, animate, index)
            {
            	return thePanel.getLabelValuesNumericFormat(v, label, storeItem, item, i, display, animate, index, seriesConfig);
            },
            orientation: 'horizontal',
            contrast: true,
		    font: '1em Arial',
            'text-anchor': 'middle'
		};
		return label;
	}

	, getFieldMetaByName: function(fieldName) {
		var store = this.getStore();
		var fieldsMeta = store.fieldsMeta;
    	for(var h in fieldsMeta) {
    		var fieldMeta = fieldsMeta[h];
    		if(fieldMeta.name == fieldName) {
    			return fieldMeta;
    		}
    	}
    	return null;
	}

	, getFieldHeaderByName: function(fieldName) {
		var fieldMeta = this.getFieldMetaByName(fieldName);
		Sbi.trace("[BarChartWidgetRuntime.getFieldHeaderByName]: " + Sbi.toSource(fieldMeta));
		return fieldMeta!=null?fieldMeta.header: null;
	}

	, getItemMeta: function(item) {
		var itemMeta = {};

		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: IN " + Sbi.toSource(item, true));

		// selected categories: names, headers & values
		itemMeta.categoryFieldNames = item.series.xField;
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected categories names are equal to [" + itemMeta.categoryFieldNames +"]");

		itemMeta.categoryFieldHeaders = [];
		for(var i = 0; i < itemMeta.categoryFieldNames.length; i++) {
			itemMeta.categoryFieldHeaders[i] = this.getFieldHeaderByName( itemMeta.categoryFieldNames[i] );
		}
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected categories headers are equal to [" + itemMeta.categoryFieldHeaders +"]");

		itemMeta.categoryValues = [];
		for(var i = 0; i < itemMeta.categoryFieldNames.length; i++) {
			itemMeta.categoryValues.push( item.storeItem.data[itemMeta.categoryFieldNames[i]] );
		}
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected categories values are equal to [" + itemMeta.categoryValues +"]");

		// selected series: name, header & value
		itemMeta.seriesFieldName = item.yField;
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected series name is equal to [" + itemMeta.seriesFieldName +"]");

		itemMeta.seriesFieldHeader = this.getFieldHeaderByName(itemMeta.seriesFieldName);
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected series header is equal to [" + itemMeta.seriesFieldHeader +"]");

		itemMeta.seriesFieldValue = item.storeItem.data[itemMeta.seriesFieldName];
		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: selected series value is equal to [" + itemMeta.seriesFieldValue +"]");


		Sbi.trace("[BarChartWidgetRuntime.getItemMeta]: OUT");

		return itemMeta;
	}

	, onItemMouseDown: function(item) {
		Sbi.trace("[BarChartWidgetRuntime.onItemMouseDown]: IN");
		if (this.areOutcomingEventsEnabled()) {
			var itemMeta = this.getItemMeta(item);
		    var selections = {};
			selections[itemMeta.categoryFieldHeaders[0]] = {values: []};
		    Ext.Array.include(selections[itemMeta.categoryFieldHeaders].values, itemMeta.categoryValues[0]);
		    this.fireEvent('selection', this, selections);
		}
	    Sbi.trace("[BarChartWidgetRuntime.onItemMouseDown]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	//return category axis name (if specified)
	, getCategoryAxis: function(){
		var categoryAxis = '';
		if (this.wconf.categoryAxis){
			categoryAxis = this.wconf.categoryAxis;
		}
		return categoryAxis;
	}

	//return series axis name (if specified)
	, getSeriesAxis: function(){
		var seriesAxis = '';
		if (this.wconf.seriesAxis){
			seriesAxis = this.wconf.seriesAxis;
		}
		return seriesAxis;
	}

	, getSortOrder: function(){
		if (this.wconf.sortOrder){
			return this.wconf.sortOrder;
		}
	}

	, getTooltip : function(storeItem, item, seriesConfig){

		Sbi.trace("[BarChartWidgetRuntime.getTooltip]: IN");

		var tooltip;

		if (this.wconf.groupingVariable != null) { // first level contains groupingVariable, second level contains series

		} else {
			var itemMeta = this.getItemMeta(item);
			
			var value = itemMeta.seriesFieldValue;
			if (typeof(value) == 'number'){
				//if (this.isInteger(value)){
					
					var decimalPrecision;
					var suffix;
					
					for (var i = 0; i < seriesConfig.fields.length; i++) {
						if (itemMeta.seriesFieldName == seriesConfig.fields[i]){
							decimalPrecision = seriesConfig.decimalPrecisions[i];
							suffix = seriesConfig.suffixes[i];
							break;
						}
					}

					if(decimalPrecision !== undefined && decimalPrecision !== null)
					{
						Sbi.trace("[BarChartWidgetRuntime.getTooltip]: Value is a number, local formatting. First is: " + value);
						
						value = this.getLocalFormattedNumericValues(decimalPrecision, value);
						
						Sbi.trace("[BarChartWidgetRuntime.getTooltip]: After is: " + value);
					}
					

					if(suffix !== undefined && suffix !== null && suffix !== '')
					{
						value = value + ' ' + suffix;
						Sbi.trace("[BarChartWidgetRuntime.getTooltip]: Adding suffix to value : " + value);
					}
				//}
			}			

			var categoryValue = itemMeta.categoryValues[0];

			tooltip =  itemMeta.seriesFieldHeader + ': ' + value
						+ " <p> " +
						itemMeta.categoryFieldHeaders + ': '+ categoryValue;
		}
		Sbi.trace("[BarChartWidgetRuntime.getTooltip]: OUT");
		return tooltip;


		/*
		var chartType = this.wconf.designer;
		var allRuntimeSeries = this.getRuntimeSeries();
		var allDesignSeries = this.wconf.series;
		var type = this.wconf.type;
		var colors = this.getColors();
		var series;

		var storeObject = this.getJsonStore(percent);

		var selectedSerieName = item.yField;

		var selectedSerie;

		if(this.isHorizontallyOriented()){
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


		var valueObj = this.getFormattedValue(null, storeItem, selectedSerie, chartType, allRuntimeSeries, allDesignSeries, type, this.isHorizontallyOriented());

		if (valueObj.measureName !== valueObj.serieName) {
			tooltip = valueObj.serieName + '<br/>' + storeItem.data.categories + '<br/>';
			// in case the serie name is different from the measure name, put also the measure name
			//tooltip += this.formatTextWithMeasureScaleFactor(valueObj.measureName, valueObj.measureName) + ' : ';
		} else {
			tooltip =  storeItem.data.categories + '<br/>' + selectedSerie.displayName + ' : ' ;
		}
		tooltip += valueObj.value;

		Sbi.trace("[BarChartWidgetRuntime.getTooltip]: OUT");

		return tooltip;
		*/
	}

	/**
	 * @method
	 *
	 * ???
	 */
	, getRuntimeSeries : function () {
		var toReturn = [];
		// rows (of dataContainerObject) can contain 2 level, it depends if a groupingVariable was defined or not
		if (this.wconf.groupingVariable != null) { // first level contains groupingVariable, second level contains series

//			var groupingAttributeValues = this.dataContainerObject.rows.node_childs;
//			for(var i = 0; i < groupingAttributeValues.length; i++) {
//				var measureNodes = groupingAttributeValues[i].node_childs;
//				for(var j = 0; j < measureNodes.length; j++) {
//					toReturn.push({
//						name : groupingAttributeValues[i].node_description +
//								( measureNodes.length > 1 ? ' [' + measureNodes[j].node_description + ']' : '' )
//						, measure : measureNodes[j].node_description
//					});
//				}
//			}

			alert("getRuntimeSeries method is unable to manage groupingVariable");

		} else { // no grouping variable: series are just first level nodes

			var measureNodes = this.dataContainerObject.rows.node_childs;
			for(var i = 0; i < measureNodes.length; i++) {
				toReturn.push({
					name : measureNodes[i].node_description
					, measure : measureNodes[i].node_description
				});
			}
		}
		return toReturn;
	}

	//used for tooltip
	, getFormattedValue: function (chart, record, series, chartType, allRuntimeSeries, allDesignSeries, type){
		var theSerieName  = series.displayName;
		var value ;
		var serieName;  // the serie name without eventual scale factor
		var measureName;  // the measure related to the serie
		var serieDefinition;  // the design-time serie definition (the measure with precision, color, ....)

		if(type != 'percent-stacked-barchart'){
			if(this.isHorizontallyOriented()){
				value =  record.data[series.xField];
			}else{
				value = record.data[series.yField];
			}
		}else{
			//value = Ext.util.Format.number(record.data[series.xField], '0.00');
			if(this.isHorizontallyOriented()){
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
		Sbi.trace("[BarChartWidgetRuntime.onRender]: IN");

		this.msg = 'Sono un widget di tipo BarChart';

		Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime.superclass.onRender.call(this, ct, position);

		Sbi.trace("[BarChartWidgetRuntime.onRender]: OUT");
	}
});

Sbi.registerWidget('barchart-ext', {
	name: 'Bar Chart'
	, icon: 'js/src/ext4/sbi/cockpit/widgets/extjs/barchart/img/barchart_64x64_ico.png'
	, runtimeClass: 'Sbi.cockpit.widgets.extjs.barchart.BarChartWidgetRuntime'
	, designerClass: 'Sbi.cockpit.widgets.barchart.BarChartWidgetDesigner'
	//, designerClass: 'Ext.Panel'
});
