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
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeBarChartPanelExt3 = function(config) {
	var defaultSettings = {
	};

	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime.runtimeBarChartPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeBarChartPanel);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	
	this.chartDivId = Ext.id();
	
	c = Ext.apply(c, {
		html : '<div id="' + this.chartDivId + '" style="width: 100%; height: 100%;"></div>'
		, autoScroll: true
	});
	Sbi.worksheet.runtime.RuntimeBarChartPanelExt3.superclass.constructor.call(this, c);
	this.init();
};

Ext.extend(Sbi.worksheet.runtime.RuntimeBarChartPanelExt3, Sbi.worksheet.runtime.RuntimeGenericChartPanelExt3, {


	chartDivId : null
	, chart : null
	, chartConfig : null 	
	
	, init : function () {
		this.initGeneric();
		this.loadChartData({
			'rows':[this.chartConfig.category]
			, 'measures': this.chartConfig.series
			, 'columns': this.chartConfig.groupingVariable ? [this.chartConfig.groupingVariable] : []
		});
	}
	
	, getByteArraysForExport: function(){
		var byteArrays = new Array();
		for(var i=0; i<this.charts; i++){
			byteArrays.push((this.charts[i]).exportPNG());
		}	
	}
	
	, createChart: function () {
		Ext.get(this.chartDivId).update(""); 
		var retriever = new Sbi.worksheet.runtime.DefaultChartDimensionRetrieverStrategy();
		var size = retriever.getChartDimension(this);
		this.update(' <div id="' + this.chartDivId + '" style="width: ' + size.width + '; height: ' + size.height + ';"></div>');
		
		var percent = ((this.chartConfig.type).indexOf('percent')>=0);
		var storeObject = this.getJsonStoreExt3(percent);
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
			items.series = this.getChartSeriesExt3(storeObject.serieNames, colors, true);
			
			//if percent stacked set the max of the axis
			if(percent){
				this.setPercentageStyleExt3(items, true);
			}
		}else{
			items.xField = 'categories';
			items.series = this.getChartSeriesExt3(storeObject.serieNames, colors);
			
			//if percent stacked set the max of the axis
			if(percent){
				this.setPercentageStyleExt3(items, false);
			}
		}
		
		this.addChartConfExt3(items);
		
		
		items.region = 'center';
		var barChartPanel = this.getChartExt3(this.chartConfig.orientation === 'horizontal', items);
	
		//Its a workaround because if you change the display name the chart is not able to write the tooltips
        
		var exportChartPanel  = new Ext.Panel({
			border: false,
			region: 'north',
			height: 20,
			html: '<div style=\"padding-top: 5px; padding-bottom: 5px; font: 11px tahoma,arial,helvetica,sans-serif;\">'+LN('sbi.worksheet.runtime.worksheetruntimepanel.chart.includeInTheExport')+'</div>'
		});
		
		var chartConf ={
			renderTo : this.chartDivId,
			border: false,
			items: [exportChartPanel, barChartPanel]
		};
		
		this.on('contentclick', function(event){
			this.byteArrays=new Array();
			try{
				this.byteArrays.push(barChartPanel.exportPNG());	
			}catch(e){}

			exportChartPanel.update('');
			this.headerClickHandler(event,null,null,barChartPanel, this.reloadJsonStoreExt3, this);
		}, this);
		
		
		new Ext.Panel(chartConf);

	}
	
	
	, setPercentageStyleExt3 : function(chart, horizontal){
		var axis =  new Ext.chart.NumericAxis({
			stackingEnabled: true,
            minimum: 0,
            maximum: 100
		});
		
		if(horizontal){
			chart.xAxis = axis;
		}else{
			chart.yAxis = axis;
		}
		

	}
		
	, getChartExt3 : function (horizontal, config) {
		if(horizontal){
			if(this.chartConfig.type == 'stacked-barchart' || this.chartConfig.type == 'percent-stacked-barchart'){
				return new Ext.chart.StackedBarChart(config);
			}else{
				return new Ext.chart.BarChart(config);
			}
		} else {
			if(this.chartConfig.type == 'stacked-barchart' || this.chartConfig.type == 'percent-stacked-barchart'){
				return new Ext.chart.StackedColumnChart(config);
			}else{
				return new Ext.chart.ColumnChart(config);
			}
		}
	}

	
	, getChartSeriesExt3: function(serieNames, colors, horizontal){
		var seriesForChart = new Array();
		for(var i=0; i<serieNames.length; i++){
			var serie = {	
	                style: {}
			};
			
//			if(this.chartConfig.type == 'percent-stacked-barchart'){
//				serie.displayName =  (serieNames[i]);//if percent doesn't matter the scale 
//			}else{
				serie.displayName =  this.formatLegendWithScale(serieNames[i]);
//			}

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
	
	//reload the store after hide a series
	, reloadJsonStoreExt3: function(chart,reloadCallbackFunctionScope ){
		var oldDataStore= chart.store;
		var hiddenseries= chart.hiddenseries;
		var percent = ((reloadCallbackFunctionScope.chartConfig.type).indexOf('percent')>=0);
		
		if(percent){
			var series = reloadCallbackFunctionScope.getSeries();
			var categories = reloadCallbackFunctionScope.getCategories();
			
			var data = new Array();
			var fields = new Array();
			var serieNames = new Array();

			for(var i=0; i<categories.length; i++){
				var z = {};
				var seriesum = 0;
				for(var j=0; j<series.length; j++){
					z['series'+j] = ((series[j]).data)[i];
					if(hiddenseries.indexOf(j)<0){
						seriesum = seriesum + parseFloat(((series[j]).data)[i]);
					}
				}
				for(var j=0; j<series.length; j++){
					z['seriesflatvalue'+j] = z['series'+j];
					z['series'+j] = (z['series'+j]/seriesum)*100;;
				}
				z['seriesum'] = seriesum;
				z['categories'] = categories[i];
				data.push(z);
			}
			oldDataStore.loadData(data);
		}else{
			chart.refresh();
		}

	}
	
	, getTooltipFormatter: function () {
	
		var chartType = this.chartConfig.designer;
		var allRuntimeSeries = this.getRuntimeSeries();
		var allDesignSeries = this.chartConfig.series;
		var type = this.chartConfig.type;
		var horizontal = this.chartConfig.orientation === 'horizontal';
		
		var thePanel = this;
		
		var toReturn = function (chart, record, index, series) {
			var tooltip = '';
			
			var valueObj = thePanel.getFormattedValueExt3(chart, record, series, chartType, allRuntimeSeries, allDesignSeries, type, horizontal);
			
			if (valueObj.measureName !== valueObj.serieName) {
				tooltip = valueObj.serieName + '\n' + record.data.categories + '\n';
				// in case the serie name is different from the measure name, put also the measure name
				tooltip += thePanel.formatTextWithMeasureScaleFactor(valueObj.measureName, valueObj.measureName) + ' : ';
			} else {
				tooltip =  record.data.categories + '\n' + series.displayName + ' : ' ;
			}
			tooltip += valueObj.value;
		
			return tooltip;
			
		};
		return toReturn;
	}
	
	//Format the value to display
	, getFormattedValueExt3: function (chart, record, series, chartType, allRuntimeSeries, allDesignSeries, type, horizontal){
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
			//substring to remove the scale factor
			if (allDesignSeries[i].seriename === measureName) {
				serieDefinition = allDesignSeries[i];
				break;
			}
		}

		// format the value according to serie configuration
		value = Sbi.qbe.commons.Format.number(value, {
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

});