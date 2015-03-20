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
 * Authors - Monica Franceschini
 */
Ext.ns("Sbi.engines.chart");

Sbi.engines.chart.HighchartsPanel = function(config) {
	var defaultSettings = {
	};

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);
	
	// constructor
	Sbi.engines.chart.HighchartsPanel.superclass.constructor.call(this, c);
	
	this.init();
};

Ext.extend(Sbi.engines.chart.HighchartsPanel, Sbi.engines.chart.GenericChartPanel, {

	 chart : null
   , detailChart: null
   , chartsArr : []
   , chartConfig : null // mandatory object to be passed as a property of the constructor input object.	

	, init : function () {
		//gets dataset values (at the moment one dataset for all charts in a document)
		var dataConfig = this.chartConfig;
		if (this.chartConfig.charts){			
			dataConfig =  Ext.apply( this.chartConfig.charts[0], dataConfig);
			delete dataConfig.charts;
		}
		this.loadChartData(dataConfig);
		
		//show the loading mask
		if(this.rendered){
			this.showMask();
		} else{
			this.on('afterlayout',this.showMask,this);
		}
	}

	, createChart: function () {
		var totalCharts = []; //for reset
		totalCharts = this.chartConfig.charts || [];
		if (totalCharts.length === 0 && this.chartConfig !== undefined){
			//mono chart, retrocompatibility
			totalCharts.push(this.chartConfig);
		}
		Ext.each(totalCharts, function(singleChart, index) {
			var singleChartConfig = this.chartConfig;
			singleChartConfig =  Ext.apply( singleChart, singleChartConfig);
			delete singleChartConfig.charts;
			singleChartConfig.chart.renderTo = singleChartConfig.divId + '__' + index + '';			
			if (singleChartConfig.exporting == undefined || singleChartConfig == null){
				//disable exporting buttons for default
				var exp = {};
				exp.enabled = false;
				singleChartConfig.exporting = exp;
			}
			//disable credits information
			var credits = {};
			credits.enabled = false;
			singleChartConfig.credits = credits;
			this.enableDrillEvents(singleChartConfig);			
			this.setFieldValuesIntoTemplate(singleChartConfig);
			
			//looks for js function		
			if (singleChartConfig.plotOptions){
//				if(singleChartConfig.plotOptions.pie && singleChartConfig.plotOptions.pie.dataLabels &&
//								singleChartConfig.plotOptions.pie.dataLabels.formatter){
//						var formatterCode = this.getFormatter(singleChartConfig.plotOptions.pie.dataLabels.formatter);							
//					    singleChartConfig.plotOptions.pie.dataLabels.formatter = formatterCode;
//				}else{
					 for (var i in singleChartConfig.plotOptions) {
						 var po4Type = singleChartConfig.plotOptions[i];
						 if (po4Type.dataLabels && po4Type.dataLabels.formatter){
							 var formatterCode = this.getFormatter(po4Type.dataLabels.formatter);
							 po4Type.dataLabels.formatter = formatterCode;
						 }
					 }
//				}
			}
					
			if(singleChartConfig.series && singleChartConfig.series.formatter){
				var formatterCode = this.getFormatter(singleChartConfig.series.formatter);	
			    singleChartConfig.series.formatter = formatterCode;
			}				
			
			//defines tooltip
			if(singleChartConfig.tooltip){				
				if (singleChartConfig.tooltip.text){					
			    	var aliasFields = this.getFieldLabels(singleChartConfig.tooltip.text);
					var formatterCode = this.getFormatterText(singleChartConfig.tooltip.text,singleChartConfig.tooltip.formatter, this.store, aliasFields);
					singleChartConfig.tooltip.formatter = formatterCode;		
				} else if (singleChartConfig.tooltip.formatter){
					var formatterCode = this.getFormatter(singleChartConfig.tooltip.formatter);				
					singleChartConfig.tooltip.formatter = formatterCode;					
				}
			}
			
			if(singleChartConfig.xAxis && singleChartConfig.xAxis.labels && singleChartConfig.xAxis.labels.formatter){
				var formatterCode = this.getFormatter(singleChartConfig.xAxis.labels.formatter);
				 singleChartConfig.xAxis.labels.formatter = formatterCode;
			}	

				
			//defines formatter for axis y data
			this.defineFormatterAxis(singleChartConfig);
			//defines series da a
			this.defineSeriesData(singleChartConfig);
			
			//get categories for each axis from dataset
			this.definesCategoriesX(singleChartConfig);
			this.definesCategoriesY(singleChartConfig);
			//sets the highchart colors table: default uses the own definition
			var newColors = this.getColors(singleChartConfig);
			//Highcharts.setOptions({colors: newColors }); with this instruction a black color is added on the last category (pie case)
			singleChartConfig.colors = newColors;
//			alert(singleChartConfig.toSource());
			this.chart = new Highcharts.Chart(singleChartConfig);
			//saves the chart for eventually multiple export
			this.chartsArr.push(this.chart);
		}, this);
		this.hideMask();
	}

	
	
	, getPlotOptions : function () {
		var plotOptions = null;
		if (this.chartConfig.orientation === 'horizontal') {
			plotOptions = {
				bar: {
					stacking: this.getStacking(),
					dataLabels: {
						enabled: (this.chartConfig.showvalues !== undefined) ? this.chartConfig.showvalues : true
					}
				}
			};
		} else {
			plotOptions = {
				column: {
					stacking: this.getStacking(),
					dataLabels: {
						enabled: (this.chartConfig.showvalues !== undefined) ? this.chartConfig.showvalues : true
					}
				}
			};
		}
		return plotOptions;
	}
	
	, getStacking : function () {
		switch (this.chartConfig.type) {
	        case 'side-by-side-barchart':
	        	return null;
	        case 'stacked-barchart':
	        	return 'normal';
	        case 'percent-stacked-barchart':
	        	return 'percent';
	        default: 
	        	alert('Unknown chart type!');
	        return null;
		}
	}
	
	, defineSeriesData: function(config){
		if (config.chart.type == 'waterfall'){
			this.defineWaterfallSeriesData(config);
		}else{
			this.defineStandardSeriesData(config);
		}
		
	}
	
	, definesCategoriesX: function(config){
		if(config.xAxis != undefined){
			//if multiple X axis
			if(config.xAxis.length != undefined){
				//gets categories values and adds theme to the config	
				var categoriesX = this.getCategoriesX();
				if(categoriesX == undefined || categoriesX.length == 0){
					delete this.chartConfig.xAxis;
					for(var j =0; j< this.categoryAliasX.length; j++){
						config.xAxis[j].categories = categoriesX[j];
					}					
				}
				//else keep templates ones

			}else{
				//single axis
				var categoriesX = this.getCategoriesX();
				if(categoriesX != undefined && categoriesX.length != 0){
					config.xAxis.categories = categoriesX[0];
				}				
			}
		}
	}
	
	, definesCategoriesY: function(config){
		
		if(config.yAxis != undefined){
			//if multiple Y axis
			if(config.yAxis.length != undefined){
				//gets categories values and adds theme to the config	
				var categoriesY = this.getCategoriesY();
				if(categoriesY == undefined || categoriesY.length == 0){
					delete this.chartConfig.yAxis;
					for(var j =0; j< this.categoryAliasY.length; j++){
						config.yAxis[j].categories = categoriesY[j];						
					}					
				}
				//else keep templates ones
			}else{
				//single axis
				var categoriesY = this.getCategoriesY();
				if(categoriesY != undefined && categoriesY.length != 0){
					config.yAxis.categories = categoriesY[0];
				}				
			}
		}
	}
	
	, defineFormatterAxis: function(config){
		if ( config.yAxis !== undefined){
			for(var j =0; j< config.yAxis.length; j++){
				if (config.yAxis[j].labels && config.yAxis[j].labels.formatter){
					var formatterCode = this.getFormatter( config.yAxis[j].labels.formatter);
					config.yAxis[j].labels.formatter = formatterCode;
				}
			}
		}
	}
	, defineStandardSeriesData: function(config){
		//gets series values and adds theme to the config
		var seriesNode = [];

		if (config.series !== undefined ){
			var serieValue = config.series;
			if (Ext.isArray(serieValue)){
				var seriesData =  {};
				var str = "";
				for(var i = 0; i < serieValue.length; i++) {
					seriesData = serieValue[i];					
					seriesData.data = this.getSeries(serieValue[i].alias,serieValue[i].group );//values from dataset
					seriesNode.push(seriesData);
				}
			}
		}else if (config.plotOptions){ 
			seriesData = config.series;//other attributes too
			seriesData.data = this.getSeries();//values from dataset
			seriesNode.push(seriesData);
		}

		config.series = seriesNode;
	}
	
	, defineWaterfallSeriesData: function(config){
		//gets series values and adds theme to the config
		var seriesNode = [];

		if (config.series !== undefined ){
			var serieValue = config.series;
			var colors = (config.colors)? config.colors[0] : {};
			if (Ext.isArray(serieValue)){
				var seriesData =  {};
				var str = "";																				
				for(var i = 0; i < serieValue.length; i++) {
					seriesData = serieValue[i];						
					seriesData.data = this.getWaterfallSeries(serieValue[i], colors);//values from dataset
					seriesData.color = colors.downColor || colors.color ||  Highcharts.getOptions().colors[3];
					seriesData.upColor = colors.upColor || Highcharts.getOptions().colors[2];	
					seriesNode.push(seriesData);
				}
			}
		}else if (config.plotOptions){ 
			seriesData = config.series;//other attributes too
			seriesData.data = this.getWaterfallSeries();//values from dataset
			seriesData.color = colors.downColor || colors.color ||  Highcharts.getOptions().colors[3];
			seriesData.upColor = colors.upColor || Highcharts.getOptions().colors[2];	
			seriesNode.push(seriesData);
		}		
		config.series = seriesNode;
	}
	
	
	//formatter definition 
	, formatWithName: function (){
		return  function (){return this.series.name;}
	}
	, formatWithNamePercentage: function (){
		return function (){return  '<b>'+this.series.name +'</b><br/>'+this.point.name +':'+  this.y+'%';};
	}
	, formatWithNameValuePlain: function (){
		return function (){return  '<b>'+this.series.name +'</b><br/>'+this.point.name +':'+  this.y;};
	}
	, formatWithPercentage: function (){
		return  function (){return '<b>'+ this.series.name +'</b><br/>'+ this.y+'%';};
	}
	, formatWithXY: function (){
		return  function (){return 'The value for <b> '+this.x+' </b> is <b> '+this.y+' </b>';};
	}
	, formatWithNameValue: function (){
		return function (){return '<b>'+ this.series.name+ '</b><br/>'+ this.point.name ;};
	}	
	, formatSubstringLabel: function (len){
		return function (){return '<b>'+ this.value.substring(0,len)+ '</b>';};
	}
	, formatByUserFunction: function (body){
		var functCode = 'function() { return ' + body + '; } ';
		return eval('(' + functCode + ')');
	}
	, formatWithNumberFormat: function(p){
		p = 'this.point.y,' +p;
		var functCode = 'function() { return Highcharts.numberFormat(' + p + '); } ';
		return eval('(' + functCode + ')');
	}
	,formatWithAbs: function(){
		var functCode = 'function() { return Math.abs(this.point.y); } ';
		return eval('(' + functCode + ')');
	}
	, formatWithNumberRounded: function(){
		var functCode = 'function() { '+
    	'if(this.point.y/1000>1){ '+
    	'	return Math.floor(this.point.y/1000)+"K"'+
    	'}else if(this.point.y/1000000>1){ '+
    	'	return Math.floor(this.point.y/1000000)+"M"'+
    	'}else if(this.point.y/1000000000>1){ '+
    	'	return Math.floor(this.point.y/1000000000)+"G"'+
    	'}else if(this.point.y/1000000000000>1){ '+
    	'	return Math.floor(this.point.y/1000000000000)+"T"'+
    	'} }';		
    	return eval('(' + functCode + ')');
    }

	, getFormatter: function(obj){
		var formatterCode = "";
		var formatFunc = "";
		var bodyCode = "";
		var len = obj.indexOf("(") ;

		if (len != -1){
			formatFunc = obj.substring(0, len);
			len = obj.substring(obj.indexOf("(")+1, obj.length-1);			
		}else {
			formatFunc = obj;
			len = 10;
		}		

		switch (formatFunc) {
		case 'name_value_plain':
        	formatterCode = this.formatWithNameValuePlain();
        	break;    
        case 'name_percentage':
        	formatterCode = this.formatWithNamePercentage();        	
        	break;
        case 'name_value':
        	formatterCode = this.formatWithNameValue();
        	break;
        case 'percentage':
        	formatterCode = this.formatWithPercentage();
        	break;
        case 'x_y': 
        	formatterCode = this.formatWithXY();
        	break;	
        case 'name':
        	formatterCode =  this.formatWithName();
        	break;
        case 'substringLabel':
        	formatterCode = this.formatSubstringLabel(len);
        	break;        	        
        case 'numberFormat':    
        	var pars =  obj.substring(obj.indexOf("numberFormat(")+13,obj.length-1);  
        	formatterCode = this.formatWithNumberFormat(pars);
        	break;
        case 'formatWithNumberRounded':            	 
        	formatterCode = this.formatWithNumberRounded();
        	break;
        case 'abs':            	 
        	formatterCode = this.formatWithAbs();
        	break;
        case 'userFunction':        	
        	bodyCode =  obj.substring(obj.indexOf("userFunction(")+13,obj.length-1); 
        	formatterCode = this.formatByUserFunction(bodyCode);
        	break;
        default:         	
        	formatterCode = this.formatWithName();
        	break	       
		}
		return formatterCode;
	}

	, getFormatterText: function(obj, objFormatter, store, aliasFields){
		return function (){
	    	var prefix = "";
	    	var suffix = "";
	    	var text = obj;
	    	
	    	if (text.indexOf("{CATEGORY}") != -1) aliasFields.push("CATEGORY");
	    	if (text.indexOf("{SERIE_NAME}") != -1) aliasFields.push("SERIE_NAME");
	    	if (text.indexOf("{SERIE}") != -1) aliasFields.push("SERIE");			
	    	
	    	if (aliasFields.length == 0) return text;
	    	var spanText = "<span style='color:"+ this.series.color +"'>";
	    	text = spanText + text;
			for(var i=0;i<this.series.data.length;i++){				
	            var item = this.series.data[i];
	            if((item.x == this.x || item.category == this.x) && item.y == this.y){
	            	for (var j=0, jl=aliasFields.length; j<jl; j++){
	    				var alias = aliasFields[j];
	    				var fieldColumn = "";
	    				var fieldValue = "";
	    				if (alias == "CATEGORY"){
	    					prefix = "{";
	    					suffix = "}";
	    					fieldValue += this.point.category || this.point.x ;	    				
	    				}else if (alias == "SERIE"){
	    					prefix = "{";
	    					suffix = "}";
	    					if (!objFormatter)
	    						fieldValue = this.point.y;
	    					else{
	    						if (objFormatter.indexOf('numberFormat')>=0){
	    							var parsString = objFormatter.substring(obj.indexOf("numberFormat(")+14,objFormatter.length-1); 
	    							var fnc = 'Highcharts.numberFormat(this.point.y, '+parsString+');';
	    							fieldValue =  eval(fnc); 
	    						}else if(objFormatter.indexOf('formatWithNumberRounded')>=0){		
	    					    	if(this.point.y/1000>1){ 
	    					    		fieldValue =  Math.floor(this.point.y/1000)+"K"
	    					    	}else if(this.point.y/1000000>1){ 
	    					    		fieldValue =  Math.floor(this.point.y/1000000)+"M"
	    					    	}else if(this.point.y/1000000000>1){ 
	    					    		fieldValue = Math.floor(this.point.y/1000000000)+"G"
	    					    	}else if(this.point.y/1000000000000>1){ 
	    					    		fieldValue = Math.floor(this.point.y/1000000000000)+"T"
	    					    	}
	    						}else if (objFormatter.indexOf('abs')>=0){ 
	    							var fnc = 'Math.abs(this.point.y);';
	    							fieldValue =  eval(fnc); 
	    						}else{
	    							fieldValue = this.point.y;
	    						}
	    					}
	    						
	    				}else if (alias == "SERIE_NAME"){
	    					prefix = "{";
	    					suffix = "}";
	    					fieldValue = this.series.name ||  this.point.name ;
	    				}else {
	    					prefix = "$F{";
	    					suffix = "}";
	    					fieldColumn = store.getFieldNameByAlias(alias);	    						    				
			        		var rec = store.getAt(i);
			    			if(rec) fieldValue = rec.get(fieldColumn);		
	    				}
	    				
	    				if (fieldValue !== null){
	    					//var tmpText = text.replace(prefix + alias + suffix, "<b>" + fieldValue + "</b>");
	    					var tmpText = text.replace(prefix + alias + suffix,  fieldValue );
	    					text = tmpText;
	    				}	    				
	    			}          	
	            }
	        }
			text += "</span>";
			return text;	 
		};
	}
	
	
	
});