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
 *  loadChartData(dataConfig): load the data for the chart
 *  getCategories(): Load the categories for the chart
 *  getSeries(): Load the series for the chart
 * 
 * 
 * Public Events
 * 
 *  contentloaded: fired after the data has been loaded
 * 
 * Authors
 * 
 * - Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeGenericChartPanel  = function(config) { 

	var defaultSettings = {
			border: false
	};

	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime.runtimeGenericChartPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeGenericChartPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	//this.style='width: 80%; margin-left: auto; margin-right: auto;';

	this.services = this.services || new Array();
	var params = {};
	this.services['loadData'] = this.services['loadData'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'LOAD_CROSSTAB_ACTION'
			, baseParams: params
	});
//	this.services['exportChart'] = this.services['exportChart'] || Sbi.config.serviceRegistry.getServiceUrl({
//	serviceName: 'EXPORT_CHART_ACTION'
//	, baseParams: params
//	});
	
	
	this.legendFontSize =  Sbi.settings.worksheet.runtime.chart.legend.fontSize || 10;
	
	this.addEvents('contentloaded');
	Sbi.worksheet.runtime.RuntimeGenericChartPanel.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.worksheet.runtime.RuntimeGenericChartPanel, Ext.Panel, {
	loadMask: null,
	services: null,
	sheetName: null,
	dataContainerObject: null,//the object with the data for the panel
	customStyles: null, //custom styles
	legendStyle: null,
	titleStyle: null,
	axisTitleStyle: null,
	axisValueStyle: null,
	valueStyle: null,
	legendFontSize : null

	
	, initGeneric: function(){
		this.initStyles(this.chartConfig);
	}

	/**
	 * Loads the data for the chart.. Call the action which loads the crosstab 
	 * (the crosstab is the object that contains the data for the chart)
	 * @param dataConfig the field for the chart..
	 * The syntax is {rows, measures}.. For example {'rows':[{'id':'it.eng.spagobi.SalesFact1998:product(product_id):productClass(product_class_id):productFamily','nature':'attribute','alias':'Product Family','iconCls':'attribute'}],'measures':[{'id':'it.eng.spagobi.SalesFact1998:storeCost','nature':'measure','alias':'Store Cost','funct':'SUM','iconCls':'measure'},{'id':'it.eng.spagobi.SalesFact1998:unitSales','nature':'measure','alias':'Unit Sales','funct':'SUM','iconCls':'measure'}]}
	 */
	, loadChartData: function(dataConfig, filters){

		if ( !this.chartConfig.hiddenContent ){
			var requestParameters = {
					'crosstabDefinition': Ext.util.JSON.encode({
						'rows': dataConfig.columns,
						'columns': dataConfig.rows,
						'measures': dataConfig.measures,
						'config': {'measureson':'rows'}
					})
					, 'sheetName' : this.sheetName
			};
			if ( filters != null ) {
				requestParameters.FILTERS = Ext.encode(filters);
			}
			Ext.Ajax.request({
				url: this.services['loadData'],//load the crosstab from the server
				params: requestParameters,
				success : function(response, opts) {

					this.dataContainerObject = Ext.util.JSON.decode( response.responseText );
					//this.update(' <div id="' + this.chartDivId + '" style="width: 100%; height: 100%;"></div>');
					if (this.isEmpty()) {
//						this.update(' <div id="' + this.chartDivId + '" style="width: 100%; height: 100%;"></div>');
						Ext.Msg.show({
							title: LN('sbi.qbe.messagewin.info.title'),
							msg: LN('sbi.qbe.datastorepanel.grid.emptywarningmsg'),
							buttons: Ext.Msg.OK,
							icon: Ext.MessageBox.INFO
						});
						this.fireEvent('contentloaded');
					} else {
						if(this.rendered){
							this.createChart();
							this.fireEvent('contentloaded');
						}else{
							this.on('afterrender',function(){this.createChart();this.fireEvent('contentloaded');}, this);
						}

					}

				},
				scope: this,
				failure: function(response, options) {
					this.fireEvent('contentloaded');
					Sbi.exception.ExceptionHandler.handleFailure(response, options);
				}      
			});
		}else{
			if(this.rendered){
				this.fireEvent('contentloaded');
			}else{
				this.on('afterrender',function(){this.fireEvent('contentloaded');}, this);
			}
		}
	}

, isEmpty : function () {
	var measures = this.dataContainerObject.columns.node_childs;
	return measures === undefined;
}

/**
 * Load the categories for the chart
 */
, getCategories: function(){
	if(this.dataContainerObject!=null){
		var measures = this.dataContainerObject.columns.node_childs;
		var categories = [];
		var i=0;
		for(; i<measures.length; i++){
			categories.push(measures[i].node_description);
		}
		return  categories;
	}
}

/**
 * Loads the series for the chart
 */
, getSeries: function(){
	if(this.dataContainerObject!=null){
		var runtimeSeries = this.getRuntimeSeries();
		var data = this.dataContainerObject.data;
		var measures_metadata = this.dataContainerObject.measures_metadata;
		var measures_metadata_map = {};
		//load the metadata of the measures (we need the type)
		var i=0;

		for(; i<measures_metadata.length; i++){
			measures_metadata_map[measures_metadata[i].name] ={'format':measures_metadata[i].format, 'type': measures_metadata[i].type};
			measures_metadata_map[measures_metadata[i].name].scaleFactorValue = (this.getMeasureScaleFactor(measures_metadata[i].name)).value;
		}
		var series = [];
		var serie;
		var map ;
		var serieData, serieDataFormatted;
		i=0;
		for (; i < runtimeSeries.length; i++){
			serie = {};
			serie.name = runtimeSeries[i].name;
			var measure = runtimeSeries[i].measure;
			serieData = this.dataContainerObject.data[i];
			serieDataFormatted = [];
			var j=0;
			for(; j<serieData.length; j++){
				map = measures_metadata_map[measure];
				serieDataFormatted.push(this.format(serieData[j], map.type, map.format, map.scaleFactorValue ));
			}
			serie.data = serieDataFormatted;
			serie.shadow = false;
			series.push(serie);
		}	
		return series;
	}
}

, getRuntimeSeries : function () {
	var toReturn = [];
	// rows (of dataContainerObject) can contain 2 level, it depends if a groupingVariable was defined or not
	if (this.chartConfig.groupingVariable != null) {
		// first level contains groupingVariable, second level contains series
		var groupingAttributeValues = this.dataContainerObject.rows.node_childs;
		for(var i = 0; i < groupingAttributeValues.length; i++) {
			var measureNodes = groupingAttributeValues[i].node_childs;
			for(var j = 0; j < measureNodes.length; j++) {
				toReturn.push({
					name : groupingAttributeValues[i].node_description + 
					( measureNodes.length > 1 ? ' [' + measureNodes[j].node_description + ']' : '' )
					, measure : measureNodes[j].node_description
				});
			}
		}
	} else {
		// no grouping variable: series are just first level nodes
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

, getDataLabelsFormatter: function () {
	var showPercentage = this.chartConfig.showpercentage;
	var chartType = this.chartConfig.designer;

	var allRuntimeSeries = this.getRuntimeSeries();
	var allDesignSeries = this.chartConfig.series;

	var toReturn = function () {
		var theSerieName = this.series.name;

		var serieName;  // the serie name without eventual scale factor
		var measureName;  // the measure related to the serie
		var serieDefinition;  // the design-time serie definition (the measure with precision, color, ....)

		// find the serie configuration
		var i = 0;
		for (; i < allRuntimeSeries.length; i++) {
			if (allRuntimeSeries[i].name === theSerieName) {
				serieName = allRuntimeSeries[i].name;
				measureName = allRuntimeSeries[i].measure;
				break;
			}
		}

		i = 0;
		// find the serie's (design-time) definition
		for (; i < allDesignSeries.length; i++) {
			//substring to remove the scale factor
			if (allDesignSeries[i].seriename === measureName) {
				serieDefinition = allDesignSeries[i];
				break;
			}
		}

		// format the value according to serie configuration
		var value = Sbi.qbe.commons.Format.number(this.y, {
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

		var dataLabel = null;
		if (chartType == 'Pie Chart') {
			dataLabel = '<b>'+ this.point.name +'</b>: ' + value;
		} else {
			dataLabel = value;
		}

		// display percentage if needed
		if (showPercentage) {
			dataLabel += ' ( ' + Ext.util.Format.number(this.percentage, '0.00') + ' %)';
		}

		return dataLabel;

	};
	return toReturn;
}

, getTooltipFormatter: function () {
	var showPercentage = this.chartConfig.showpercentage;
	var chartType = this.chartConfig.designer;
	var thisPanel = this;

	var allRuntimeSeries = this.getRuntimeSeries();
	var allDesignSeries = this.chartConfig.series;

	var toReturn = function () {

		var theSerieName = this.series.name;
		var serieName;  // the serie name without eventual scale factor
		var measureName;  // the measure related to the serie
		var serieDefinition;  // the design-time serie definition (the measure with precision, color, ....)


		// find the serie configuration
		var i = 0;
		for (; i < allRuntimeSeries.length; i++) {
			if (allRuntimeSeries[i].name === theSerieName) {
				serieName = allRuntimeSeries[i].name;
				measureName = allRuntimeSeries[i].measure;
				break;
			}
		}

		i = 0;
		// find the serie's (design-time) definition
		for (; i < allDesignSeries.length; i++) {
			//substring to remove the scale factor
			if (allDesignSeries[i].seriename === measureName) {
				serieDefinition = allDesignSeries[i];
				break;
			}
		}

		// format the value according to serie configuration
		var value = Sbi.qbe.commons.Format.number(this.y, {
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

		var tooltip = null;
		if (chartType == 'Pie Chart') {
			tooltip = '<b>' + this.point.name + '</b><br/>' + thisPanel.formatLegendWithScale(this.series.name) + ': ' + value;
		} else {
			// in case the serie name is different from the measure name, put also the measure name
			if (measureName !== serieName) {
				tooltip = '<b>' + this.x + '</b><br/>' + this.series.name + '<br/>' 
				+ thisPanel.formatTextWithMeasureScaleFactor(measureName, measureName) + ': ' + value;
			} else {
				tooltip = '<b>' + this.x + '</b><br/>' + thisPanel.formatLegendWithScale(this.series.name) + ': ' + value;
			}
		}

		// display percentage if needed
		if (showPercentage) {
			tooltip += ' ( ' + Ext.util.Format.number(this.percentage, '0.00') + ' %)';
		}

		return  tooltip;

	};

	return toReturn;
}

, exportContent: function() {
	var svg = this.chart.getSVG();
	var exportedChart = {SVG: svg, SHEET_TYPE: 'CHART'};
	return exportedChart;
}


, getColors : function () {
	var colors = [];
	if (this.chartConfig !== undefined && this.chartConfig.groupingVariable != null) {
		colors = Sbi.widgets.Colors.defaultColors;
	} else {
		if (this.chartConfig !== undefined && this.chartConfig.series !== undefined && this.chartConfig.series.length > 0) {
			var i = 0;
			for (; i < this.chartConfig.series.length; i++) {
				colors.push(this.chartConfig.series[i].color);
			}
		}
	}
	return colors;
}

, formatLegendWithScale : function(theSerieName) {
	var serie = this.getRuntimeSerie(theSerieName);
	var toReturn = this.formatTextWithMeasureScaleFactor(serie.name, serie.measure);
	return toReturn;
}

, getRuntimeSerie : function (theSerieName) {
	var allRuntimeSeries = this.getRuntimeSeries();
	var i = 0;
	for (; i < allRuntimeSeries.length; i++) {
		if (allRuntimeSeries[i].name === theSerieName) {
			return allRuntimeSeries[i];
		}
	}
	return null;
}

, formatTextWithMeasureScaleFactor : function(text, measureName) {
	var legendSuffix = (this.getMeasureScaleFactor(measureName)).text;
	if (legendSuffix != '' ) {
		return text + ' ' + legendSuffix;
	}
	return text;
}

, addStyle: function(chartConfig){
	this.addFontStyles(chartConfig);
}

, addFontStyles: function(chartConfig) {

	if (!chartConfig) {
		chartConfig = {};
	}
	if(!chartConfig.xAxis) {
		chartConfig.xAxis = {};
	}
	if(!chartConfig.xAxis.labels){
		chartConfig.xAxis.labels = {};
	}
	if(!chartConfig.xAxis.labels.style){
		chartConfig.xAxis.labels.style = {};
	}
	if(!chartConfig.xAxis.title){
		chartConfig.xAxis.title = {};
	}
	if(!chartConfig.xAxis.title.style){
		chartConfig.xAxis.title.style = {};
	}
	Ext.apply(chartConfig.xAxis.labels.style , this.axisValueStyle || {});
	Ext.apply(chartConfig.xAxis.title.style , this.axisTitleStyle || {});

	if(!chartConfig.yAxis){
		chartConfig.yAxis = {};
	}
	if(!chartConfig.yAxis.labels){
		chartConfig.yAxis.labels = {};
	}
	if(!chartConfig.yAxis.labels.style){
		chartConfig.yAxis.labels.style = {};
	}
	if(!chartConfig.yAxis.title){
		chartConfig.yAxis.title = {};
	}
	if(!chartConfig.yAxis.title.style){
		chartConfig.yAxis.title.style = {};
	}
	
	Ext.apply(chartConfig.yAxis.labels.style , this.axisValueStyle||{});
	Ext.apply(chartConfig.yAxis.title.style , this.axisTitleStyle||{});

}

, initStyles: function(chartConfig){
	this.titleStyle= {
			fontSize : chartConfig.outerFontSize,
			fontFamily : chartConfig.outerFontType
	};
	this.axisTitleStyle= {
			fontSize : chartConfig.outerFontSize,
			fontFamily : chartConfig.outerFontType
	};
	this.axisValueStyle= {
			fontSize : chartConfig.innerFontSize,
			fontFamily : chartConfig.innerFontType
	};
	this.valueStyle= {
			fontSize : chartConfig.innerFontSize,
			fontFamily : chartConfig.innerFontType
	};
	
	if(this.axisTitleStyle && this.axisTitleStyle.fontSize && this.axisTitleStyle.fontSize!=null){
		this.legendFontSize = this.axisTitleStyle.fontSize;
	}
}

, getValueStyle: function(){
	return this.valueStyle||{};
}

, getMeasureScaleFactor: function (theMeasureName){
	var i=0;
	var scaleFactor={value:1, text:''};
	var optionDefinition = null;
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
	return scaleFactor;
}

});