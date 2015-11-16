/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/*
 * NOTE: This class is meant to be extended and not directly istantiated
 */

Ext.ns("Sbi.cockpit.widgets.extjs.abstractchart");

Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime = function(config) {
	Sbi.trace("[AbstractChartWidgetRuntime.constructor]: IN");

	var defaultSettings = {
		layout: 'fit',
		fieldsSelectionEnabled: true,
		msg: '<b>Rendering...</b>'
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime', defaultSettings);
	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	var categories = [];
	categories.push(this.wconf.category);
	if(this.wconf.groupingVariable) categories.push(this.wconf.groupingVariable);

	this.aggregations = {
		measures: this.wconf.series,
		categories: categories
	};
	Sbi.trace("[AbstractChartWidgetRuntime.constructor]: aggregations properties set to [" + Sbi.toSource(this.aggregations)+ "]");

	this.init();
	this.items = this.chart || this.msgPanel;

	Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime.superclass.constructor.call(this, c);

	var bounded = this.boundStore();
	if(bounded) {
		Sbi.trace("[AbstractChartWidgetRuntime.constructor]: store [" + this.getStoreId() + "] succesfully bounded to widget [" + this.getWidgetName() + "]");
	} else {
		Sbi.error("[AbstractChartWidgetRuntime.constructor]: store [" + this.getStoreId() + "] not bounded to widget [" + this.getWidgetName() + "]");
	}

	this.reload();
	Sbi.trace("[AbstractChartWidgetRuntime.constructor]: Reloading store [" + this.getStoreId() + "] ...");

	this.addEvents('selection');


	Sbi.trace("[AbstractChartWidgetRuntime.constructor]: OUT");
};

Ext.extend(Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime, Sbi.cockpit.core.WidgetRuntime, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
	 * It's the content panel used when the chart is not yet available. By default it's empty. If the config property
	 * msg is passed in then show the msg text. As soon as the chart became available it is removed and destroyed.
	 */
	msgPanel: null
	
	/**
	 * It's the object used to set font style (font family and font sizes) in the chart.
	 */
	, widgetFontConfiguration: null

    // =================================================================================================================
	// METHODS
	// =================================================================================================================

    // -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, setContentPanel: function(panel) {
		this.items.each( function(item) {
			this.items.remove(item);
	        item.destroy();
	    }, this);
		this.msgContent = null;
        this.add(panel);
        this.doLayout();
	}

	, isLegendVisible: function() {
		var showlegend;
		if (this.wconf.showlegend !== undefined){
			showlegend = this.wconf.showlegend;
		} else {
			showlegend = true;
		}
		return showlegend;
	}

	, getSeriesTips: function(decimalPrecision, suffix) {
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
                      
           		var tooltipContent = thisPanel.getTooltip(storeItem, item, decimalPrecision, suffix);
           		//this.setTitle(tooltipContent);
           		//now tooltip content is in the body, cause of in the title the style it's not applied
           		this.update(tooltipContent);
            }
        };
		


		return tips;
	}

	, getLegendConfiguration: function() {
		
		Sbi.trace("[AbstractChartWidgetRuntime.getLegendConfiguration]: START");
		
		var legendConf = {};
		legendConf.position = (Sbi.isValorized(this.wconf.legendPosition))? this.wconf.legendPosition:'bottom';
		//begin legend font style configuration: labelFont to set
		var chartFont = this.widgetFontConfiguration.widgetFontType;
		var chartLegendFontSize = this.widgetFontConfiguration.legendFontSize;
		legendConf.labelFont = chartLegendFontSize + ' ' + chartFont;
		
		Sbi.trace("[AbstractChartWidgetRuntime.getLegendConfiguration]: END");
		
		return legendConf;
	}

	, getBackground: function() {
		var background = {
		    gradient: {
			    id: 'backgroundGradient',
			    angle: 45,
			    stops: {
				    0: {color: '#ffffff'},
				    100: {color: '#ffffff'}
				    //100: {color: '#eaf1f8'}
				}
			}
		};
		return background;
	}
	
	/**
	 * @method
	 * 
	 * Returns series axis name visibility
	 *
	 * @return {boolean} true if show series axis name checkbox is checked, otherwise false
	 */
	, isSeriesAxisNameVisible: function() {
		var showSeriesAxisName;
		if (this.wconf.showSeriesName !== undefined){
			showSeriesAxisName = this.wconf.showSeriesName;
		} else {
			showSeriesAxisName = true;
		}
		Sbi.trace("[AbstractChartWidgetRuntime.isSeriesAxisNameVisible]: Series Axis Title is visible? " + showSeriesAxisName);
		return showSeriesAxisName;
	}
	
	/**
	 * @method
	 * 
	 * Returns category axis name visibility
	 *
	 * @return {boolean} true if show category axis name checkbox is checked, otherwise false
	 */
	, isCategoryAxisNameVisible: function() {
		var showCategoryAxisName;
		if (this.wconf.showCategoryName !== undefined){
			showCategoryAxisName = this.wconf.showCategoryName;
		} else {
			showCategoryAxisName = true;
		}
		Sbi.trace("[AbstractChartWidgetRuntime.isCategoryAxisNameVisible]: Category Axis Title is visible? " + showCategoryAxisName);
		return showCategoryAxisName;
	}

	//------------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	, init: function() {
		Sbi.trace("[AbstractChartWidgetRuntime.init]: IN");
		this.initMsgPanel();
		this.initFontConfiguration();
		this.initChartThemes();
		Sbi.trace("[AbstractChartWidgetRuntime.init]: OUT");
	}

	, initMsgPanel: function() {
		this.msgPanel = new Ext.Panel({
			border: false
			, bodyBorder: false
			, hideBorders: true
			, frame: false
			, html: this.msg || ''
		});
	}
	
	/**
	 * @method
	 *
	 * Sets Widget Font Configuration object with selected of default values
	 *
	 */
	, initFontConfiguration: function() {
		Sbi.trace("[AbstractChartWidgetRuntime.getFontConfiguration]: START ");
		
		this.widgetFontConfiguration = {};
		
		this.widgetFontConfiguration.widgetFontType = this.getFontFamilyConfiguration();
		this.widgetFontConfiguration.legendFontSize = this.getLegendFontSizeConfiguration();
		this.widgetFontConfiguration.axisTitleFontSize = this.getAxisTitleFontSizeConfiguration();
		this.widgetFontConfiguration.tooltipFontSize = this.getTooltipFontSizeConfiguration();
		this.widgetFontConfiguration.axisLabelsFontSize = this.getAxisLabelsFontSizeConfiguration();	

		Sbi.trace("[AbstractChartWidgetRuntime.getFontConfiguration]: END ");
	}

	, initChartThemes: function() {
		
		//font vars used by constructor
		var chartFont = this.widgetFontConfiguration.widgetFontType;
		var chartAxisTitleFontSize = this.widgetFontConfiguration.axisTitleFontSize;
		var chartAxisLabelsFontSize = this.widgetFontConfiguration.axisLabelsFontSize;
		
		Ext.define('Ext.chart.theme.CustomBlue', {
	        extend: 'Ext.chart.theme.Base',
	        
	        

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
	               axis: {
	                   stroke: '#084594'
	               },
	               axisLabelLeft: axisLabel,
	               axisLabelBottom: axisLabel,
	               axisTitleLeft: titleLabel,
	               axisTitleBottom: titleLabel
	           }, config)]);
	        }
	    });
	}

	//------------------------------------------------------------------------------------------------------------------
	// utility methods
	// -----------------------------------------------------------------------------------------------------------------
	, getColors : function () {
		Sbi.trace("[AbstractChartWidgetRuntime.init]: IN");
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
		Sbi.trace("[AbstractChartWidgetRuntime.init]: IN");
		return colors;
	}

	, getMeasureScaleFactor: function (theMeasureName){
		var i=0;
		var scaleFactor={value:1, text:''};
		var optionDefinition = null;
		if ( this.fieldsOptions != null) {
			for (; i < this.fieldsOptions.length; i++) {
				if (this.fieldsOptions[i].alias === theMeasureName) {
					optionDefinition = this.fieldsOptions[i];
					break;
				}
			}
			if(optionDefinition!=null){
				legendSuffix = optionDefinition.options.measureScaleFactor;
				if(legendSuffix != undefined && legendSuffix != null && legendSuffix!='NONE'){
					scaleFactor.text = LN('sbi.worksheet.runtime.options.scalefactor.'+legendSuffix);
					switch (legendSuffix)
					{
					case 'K':
						scaleFactor.value=1000;
						break;
					case 'M':
						scaleFactor.value=1000000;
						break;
					case 'G':
						scaleFactor.value=1000000000;
						break;
					default:
						scaleFactor.value=1;
					}
				}
			}
		}
		return scaleFactor;
	}

	, formatTextWithMeasureScaleFactor : function(text, measureName) {
		var legendSuffix;
		legendSuffix = (this.getMeasureScaleFactor(measureName)).text;

		if (legendSuffix != '' ) {
			return text + ' ' + legendSuffix;
		}
		return text;
	}

	, formatLegendWithScale : function(theSerieName) {
		var serie = this.getRuntimeSerie(theSerieName);
		var toReturn = this.formatTextWithMeasureScaleFactor(serie.name, serie.measure);
		return toReturn;
	}

    , format: function(value, type, format, scaleFactor) {
    	if(value==null){
    		return value;
    	}
		try {
			var valueObj = value;
			if (type == 'int') {
				valueObj = (parseInt(value))/scaleFactor;
			} else if (type == 'float') {
				valueObj = (parseFloat(value))/scaleFactor;
			} else if (type == 'date') {
				valueObj = Date.parseDate(value, format);
			} else if (type == 'timestamp') {
				valueObj = Date.parseDate(value, format);
			}
			return valueObj;
		} catch (err) {
			return value;
		}
	}


	, isEmpty : function () {
		var measures = undefined;

		if (Sbi.isValorized(this.dataContainerObject.columns))
			measures = this.dataContainerObject.columns.node_childs;

		return measures === undefined;
	}

	, onStoreLoad: function() {
		Sbi.trace("[AbstractChartWidgetRuntime.onStoreLoad][" + this.getId() + "]: IN");

		Sbi.cockpit.widgets.extjs.abstractchart.AbstractChartWidgetRuntime.superclass.onStoreLoad.call(this, this.getStore());


		if(this.getStore().status === "error") {
			return;
		}

		if(this.rendered){
    		this.redraw();
    	} else {
    		this.on('afterrender', function(){this.redraw();}, this);
    	}
		Sbi.trace("[AbstractChartWidgetRuntime.onStoreLoad][" + this.getId() + "]: OUT");
	}

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
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
		Sbi.trace("[AbstractChartWidgetRuntime.getFieldHeaderByName]: " + Sbi.toSource(fieldMeta));
		return fieldMeta!=null?fieldMeta.header: null;
	}
	
	/**
	 * @method
	 *
	 * Returns widget font family
	 *
	 * @return {String} The widget font family selected or 'Arial' by default.
	 */
	, getFontFamilyConfiguration: function() {
		var fontType;
		if (this.wconf === undefined || this.wconf === null || this.wconf.fontType === undefined || 
				this.wconf.fontType === null || this.wconf.fontType === ""){
			fontType = 'Arial';
		} else {
			fontType = this.wconf.fontType;
		}
		Sbi.trace("[AbstractChartWidgetRuntime.getFontFamilyConfiguration]: Font-family is " + fontType);
		return fontType;
	}
	
	
	/**
	 * @method
	 *
	 * Returns legend font size
	 *
	 * @return {String} The legend font size selected or '12px' by default.
	 */
	, getLegendFontSizeConfiguration: function() {
		var fontSize;
		if (this.wconf == undefined || this.wconf == null) {
			fontSize = '12px';
		}
		else if (this.wconf.legendFontSize == undefined || this.wconf.legendFontSize == null || this.wconf.legendFontSize == ""){
			//specific legend font size not set -> check widget font size config or default value
			if (this.wconf.fontSize == undefined || this.wconf.fontSize == null || this.wconf.fontSize == ""){
				fontSize = '12px';
			} else {
				fontSize = this.wconf.fontSize + 'px';
			}			
		} else {
			fontSize = this.wconf.legendFontSize + 'px';
		}
		Sbi.trace("[AbstractChartWidgetRuntime.getLegendFontSizeConfiguration]: Legend font size is " + fontSize);
		return fontSize;
	}
	
	/**
	 * @method
	 *
	 * Returns axis title font size
	 *
	 * @return {String} The axis title font size selected or '18px' by default.
	 */
	, getAxisTitleFontSizeConfiguration: function() {
		var fontSize;
		if (this.wconf == undefined || this.wconf == null) {
			fontSize = '18px';
		}
		else if (this.wconf.axisTitleFontSize == undefined || this.wconf.axisTitleFontSize == null || this.wconf.axisTitleFontSize == ""){
			//specific axis title font size not set -> check widget font size config or default value
			if (this.wconf.fontSize == undefined || this.wconf.fontSize == null || this.wconf.fontSize == ""){
				fontSize = '18px';
			} else {
				fontSize = this.wconf.fontSize + 'px';
			}
		} else {
			fontSize = this.wconf.axisTitleFontSize + 'px';
		}
		Sbi.trace("[AbstractChartWidgetRuntime.getAxisTitleFontSizeConfiguration]: Axis title font size is " + fontSize);
		return fontSize;
	}
	
	/**
	 * @method
	 *
	 * Returns tooltip font size
	 *
	 * @return {String} The tooltip font size selected or '11px' by default.
	 */
	, getTooltipFontSizeConfiguration: function() {
		var fontSize;
		if (this.wconf == undefined || this.wconf == null) {
			fontSize = '11px';
		}
		else if (this.wconf.tooltipLabelFontSize == undefined || this.wconf.tooltipLabelFontSize == null || this.wconf.tooltipLabelFontSize == ""){
			//specific tooltip font size not set -> check widget font size config or default value
			if (this.wconf.fontSize == undefined || this.wconf.fontSize == null || this.wconf.fontSize == ""){
				fontSize = '11px';
			} else {
				fontSize = this.wconf.fontSize + 'px';
			}
		} else {
			fontSize = this.wconf.tooltipLabelFontSize + 'px';
		}
		Sbi.trace("[AbstractChartWidgetRuntime.getTooltipFontSizeConfiguration]: Tooltip font size is " + fontSize);
		return fontSize;
	}
	
	/**
	 * @method
	 *
	 * Returns axis labels font size
	 *
	 * @return {String} The axis labels font size selected or '12px' by default.
	 */
	, getAxisLabelsFontSizeConfiguration: function() {
		var fontSize;
		if (this.wconf == undefined || this.wconf == null) {
			fontSize = '12px';
		}
		else if (this.wconf.axisLabelsFontSize == undefined || this.wconf.axisLabelsFontSize == null || this.wconf.axisLabelsFontSize == ""){
			//specific axis labels font size not set -> check widget font size config or default value
			if (this.wconf.fontSize == undefined || this.wconf.fontSize == null || this.wconf.fontSize == ""){
				fontSize = '12px';
			} else {
				fontSize = this.wconf.fontSize + 'px';
			}
		} else {
			fontSize = this.wconf.axisLabelsFontSize + 'px';
		}
		Sbi.trace("[AbstractChartWidgetRuntime.getAxisLabelsFontSizeConfiguration]: Axis labels font size is " + fontSize);
		return fontSize;
	}
	
	/**
	 * @method
	 *
	 * Returns basic numeric format for current locale
	 *
	 * @return {Function} function to render basic numeric format for current locale.
	 */
	, getChartsNumericFormat: function(decimalPrecision) {
		return function(n)
    	{
			Sbi.trace("[AbstractChartWidgetRuntime.getChartsNumericFormat]: START");
			
    		var dSeparator = Sbi.locale.formats['float'].decimalSeparator;
    		var tSeparator = Sbi.locale.formats['float'].groupingSeparator;
    		var dp = decimalPrecision;
    		
            dSeparator = dSeparator || ",";
            tSeparator = tSeparator || ".";
            
            var m = /(\d+)(?:(\.\d+)|)/.exec(n + ""),
            x = m[1].length > 3 ? m[1].length % 3 : 0;

            var v = (n < 0? '-' : '') // preserve minus sign
            + (x ? m[1].substr(0, x) + tSeparator : "")
            + m[1].substr(x).replace(/(\d{3})(?=\d)/g, "$1" + tSeparator)
            + (dp? dSeparator + (+m[2] || 0).toFixed(dp).substr(2) : "");
            
            var cResult = v;
            
            Sbi.trace("[AbstractChartWidgetRuntime.getChartsNumericFormat]: END");

            return cResult;
        };
    }
	
	/**
	 * @method
	 *
	 * Returns formatted values according to current locale
	 *
	 * @return {String} formatted value according to current locale.
	 */
	, getLocalFormattedNumericValues: function(decimalPrecision, n) {
		
		Sbi.trace("[AbstractChartWidgetRuntime.getLocalFormattedNumericValues]: START");
		
		var dSeparator = Sbi.locale.formats['float'].decimalSeparator;
		var tSeparator = Sbi.locale.formats['float'].groupingSeparator;
		var dp = decimalPrecision;
		
        dSeparator = dSeparator || ",";
        tSeparator = tSeparator || ".";

        var m = /(\d+)(?:(\.\d+)|)/.exec(n + ""),
        x = m[1].length > 3 ? m[1].length % 3 : 0;

        var v = (n < 0? '-' : '') // preserve minus sign
        + (x ? m[1].substr(0, x) + tSeparator : "")
        + m[1].substr(x).replace(/(\d{3})(?=\d)/g, "$1" + tSeparator)
        + (dp? dSeparator + (+m[2] || 0).toFixed(dp).substr(2) : "");
        
        var cResult = v;
        
        Sbi.trace("[AbstractChartWidgetRuntime.getLocalFormattedNumericValues]: END");

        return cResult;
    }
	
	/**
	 * @method
	 *
	 * Returns label values on the charts. The values format is done according to current locale
	 *
	 * @return {Function} formatted label values on the charts
	 */
	, getLabelValuesNumericFormat: function(v, label, storeItem, item, i, display, animate, index, seriesConfig) {
		
		Sbi.trace("[AbstractChartWidgetRuntime.getLabelValuesNumericFormat]: START");
		
		var formattedValue = v;		
		
		var itemMeta = this.getItemMeta(item);
		
		if (typeof(v) == 'number'){
			//if (this.isInteger(v)){
				
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
					formattedValue = this.getLocalFormattedNumericValues(decimalPrecision, v);
				}
				
				if(suffix !== undefined && suffix !== null && suffix !== '')
				{
					formattedValue = formattedValue + ' ' + suffix;
				}
			//}
		}
		
		Sbi.trace("[AbstractChartWidgetRuntime.getLabelValuesNumericFormat]: END");
		
		return formattedValue;
    }
	
	
	

	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

	, onItemMouseDown: function(item) {
		Sbi.trace("[AbstractChartWidgetRuntime.onItemMouseDown]: IN");
		if (this.areOutcomingEventsEnabled()) {
			var itemMeta = this.getItemMeta(item);
		    var selections = {};
			selections[itemMeta.categoryFieldHeaders[0]] = {values: []};
		    Ext.Array.include(selections[itemMeta.categoryFieldHeaders].values, itemMeta.categoryValues[0]);
		    this.fireEvent('selection', this, selections);
		}
	    Sbi.trace("[AbstractChartWidgetRuntime.onItemMouseDown]: OUT");
	}

	, isInteger: function(value) {
		  return !isNaN(value) &&
	         parseInt(Number(value)) == value &&
	         !isNaN(parseInt(value, 10));
	}

	//------------------------------------------------------------------------------------------------------------------
	// test methods
	// -----------------------------------------------------------------------------------------------------------------

	, getSampleStore: function() {
		var store = Ext.create('Ext.data.JsonStore', {
	        fields: ['name', 'data1', 'data2', 'data3', 'data4', 'data5', 'data6', 'data7', 'data9', 'data9'],
	        data: this.generateData()
	    });
		return store;
	}

	, generateData: function(n, floor){
        var data = [],
            p = (Math.random() *  11) + 1,
            i;

        floor = (!floor && floor !== 0)? 20 : floor;

        for (i = 0; i < (n || 12); i++) {
            data.push({
                name: Ext.Date.monthNames[i % 12],
                data1: Math.floor(Math.max((Math.random() * 100), floor)),
                data2: Math.floor(Math.max((Math.random() * 100), floor)),
                data3: Math.floor(Math.max((Math.random() * 100), floor)),
                data4: Math.floor(Math.max((Math.random() * 100), floor)),
                data5: Math.floor(Math.max((Math.random() * 100), floor)),
                data6: Math.floor(Math.max((Math.random() * 100), floor)),
                data7: Math.floor(Math.max((Math.random() * 100), floor)),
                data8: Math.floor(Math.max((Math.random() * 100), floor)),
                data9: Math.floor(Math.max((Math.random() * 100), floor))
            });
        }

        //alert('data: ' + Sbi.toSource(data));

        return data;
    }

    , generateDataNegative: function(n, floor){
        var data = [],
            p = (Math.random() *  11) + 1,
            i;

        floor = (!floor && floor !== 0)? 20 : floor;

        for (i = 0; i < (n || 12); i++) {
            data.push({
                name: Ext.Date.monthNames[i % 12],
                data1: Math.floor(((Math.random() - 0.5) * 100), floor),
                data2: Math.floor(((Math.random() - 0.5) * 100), floor),
                data3: Math.floor(((Math.random() - 0.5) * 100), floor),
                data4: Math.floor(((Math.random() - 0.5) * 100), floor),
                data5: Math.floor(((Math.random() - 0.5) * 100), floor),
                data6: Math.floor(((Math.random() - 0.5) * 100), floor),
                data7: Math.floor(((Math.random() - 0.5) * 100), floor),
                data8: Math.floor(((Math.random() - 0.5) * 100), floor),
                data9: Math.floor(((Math.random() - 0.5) * 100), floor)
            });
        }
        return data;
    }

});