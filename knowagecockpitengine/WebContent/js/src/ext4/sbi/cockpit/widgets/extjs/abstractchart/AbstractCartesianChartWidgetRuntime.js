/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/*
 * NOTE: This class is meant to be extended and not directly istantiated
 */

Ext.ns("Sbi.cockpit.widgets.extjs.abstractchart");

Sbi.cockpit.widgets.extjs.abstractchart.AbstractCartesianChartWidgetRuntime = function(config) {
	Sbi.trace("[AbstractCartesianChartWidgetRuntime.constructor]: IN");
	var defaultSettings = {

	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.extjs.abstractchart.AbstractCartesianChartWidgetRuntime', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	Sbi.cockpit.widgets.extjs.abstractchart.AbstractCartesianChartWidgetRuntime.superclass.constructor.call(this, c);

	Sbi.trace("[AbstractCartesianChartWidgetRuntime.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.widgets.extjs.abstractchart.AbstractCartesianChartWidgetRuntime, Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	// no props for the moment


    // =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	getSeriesConfig: function() {

		var store = this.getStore();

		//these values are taken from Series table,
	    var seriesFields = [];
		var seriesTitles = []; 
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
			position: this.isHorizontallyOriented()? 'bottom' : 'left',
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

	, isValuesVisibles: function() {
		var showvalues;
		if (this.wconf.showvalues !== undefined){
			showvalues = this.wconf.showvalues;
		} else {
			showvalues = false;
		}
		return showvalues;
	}

	, getTooltip : function(storeItem, item, decimalPrecision, suffix){

		Sbi.trace("[AbstractCartesianChartWidgetRuntime.getTooltip]: IN");

		var tooltip;

		var itemMeta = this.getItemMeta(item);
		var value = itemMeta.seriesFieldValue;
		if (typeof(value) == 'number'){
			Sbi.trace("[AbstractCartesianChartWidgetRuntime.getTooltip]: Value is a number, local formatting. First is: " + value);
			
			value = this.getLocalFormattedNumericValues(decimalPrecision, value);
			
			Sbi.trace("[AbstractCartesianChartWidgetRuntime.getTooltip]: After is: " + value);
			
		}
		
		if(suffix !== undefined && suffix !== null && suffix !== '')
		{
			value = value + ' ' + suffix;
			Sbi.trace("[AbstractCartesianChartWidgetRuntime.getTooltip]: Adding suffix to value : " + value);
		}
		
		var categoryValue = itemMeta.categoryValues[0];

		tooltip =  itemMeta.seriesFieldHeader + ': ' + value
					+ " <p> " +
					itemMeta.categoryFieldHeaders + ': '+ categoryValue;

		Sbi.trace("[AbstractCartesianChartWidgetRuntime.getTooltip]: OUT");

		return tooltip;
	}

	, getSeriesLabel: function(seriesConfig) {
		var label = {
            display: 'insideEnd',
            field: seriesConfig.titles.length == 1? seriesConfig.titles[0]: undefined,
            renderer: Ext.util.Format.numberRenderer('0'),
            orientation: 'horizontal',
            color: '#333',
            'text-anchor': 'middle'
		};
		return label;
	}

	, getColors : function () {
		Sbi.trace("[AbstractCartesianChartWidgetRuntime.init]: IN");
		var colors = [];
		if (this.wconf !== undefined && this.wconf.groupingVariable != null) {
			colors = Sbi.widgets.Colors.defaultColors;
		} else {
			if (this.wconf !== undefined && this.wconf.series !== undefined && this.wconf.series.length > 0) {
				var i = 0;
				for (; i < this.wconf.series.length; i++) {
					colors.push(this.wconf.series[i].color);
				}
			}
		}
		Sbi.trace("[AbstractCartesianChartWidgetRuntime.init]: IN");
		return colors;
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
});