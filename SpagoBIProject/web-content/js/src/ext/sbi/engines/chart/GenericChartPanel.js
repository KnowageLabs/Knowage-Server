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
  * Public Events
  * 
  *  [list]
  * 
  * Authors
  * 
  * - Antonella Giachino(antonella.giachino@eng.it)
  */
 
Ext.ns("Sbi.engines.chart");
 
Sbi.engines.chart.GenericChartPanel  = function(config) { 
	var defaultSettings = {
			border: false
	};

	var c = Ext.apply(defaultSettings, config || {});
	
	c = Ext.apply(c, {id: 'GenericChartPanel'});
	
	c.storeId = c.dsLabel;
	
	Ext.apply(this, c);

	//constructor
	Sbi.engines.chart.GenericChartPanel.superclass.constructor.call(this, c);	 
	
};

Ext.extend(Sbi.engines.chart.GenericChartPanel, Ext.Panel, {
	loadMask: null,
	storeManager: null,				//the store manager
	store: null,					//the store
	categoryAliasX: [],
	categoryAliasY: [],
	serieAlias: []
	
	/**
	 * Loads the data for the chart.. Call the action which loads the data 
	 * (uses the test method of the manageDataset class) 
	 */
	, loadChartData: function(dataConfig){
		this.setCategoryAliasX(dataConfig);
		this.setCategoryAliasY(dataConfig);
		this.setSerieAlias(dataConfig);
		
		
		var requestParameters = {
			    id: dataConfig.dsId
			  , label: dataConfig.dsLabel
			  , refreshTime: dataConfig.refreshTime || 0
			  , dsTypeCd: dataConfig.dsTypeCd
			  , pars: dataConfig.dsPars
			  , trasfTypeCd: dataConfig.dsTransformerType
		}
		var datasets = [];
		datasets.push(requestParameters);	
		this.initStore(datasets, dataConfig.dsId);
	}
	

	, setCategoryAliasX: function(dataConfig) {
		if(dataConfig.xAxis != undefined){
			if(dataConfig.xAxis.length != undefined){
				for(var i=0; i< dataConfig.xAxis.length; i++){
					var alias = dataConfig.xAxis[i].alias;
					if(alias != undefined){
						this.categoryAliasX[i]=alias;
					}
				}
			}else{
				//single axis
				var alias = dataConfig.xAxis.alias;
				if(alias != undefined){
					this.categoryAliasX[0]=alias;
				}
			}	
		}
	}
		
	, setCategoryAliasY: function(dataConfig) {
		if(dataConfig.yAxis != undefined){
			if(dataConfig.yAxis.length != undefined){
				for(var i=0; i< dataConfig.yAxis.length; i++){//it's an array
					var alias = dataConfig.yAxis[i].alias;
					if(alias != undefined){
						this.categoryAliasY[i]=alias;
					}
				}
			}else{
				//single axis
				var alias = dataConfig.yAxis.alias;
				if(alias != undefined){
					this.categoryAliasY[0]=alias;
				}
			}

		}	
	}
	
	, setSerieAlias: function(dataConfig){
		//checks series configuration; since SpagoBI 3.2 all series can be filtered through an input parameter defined 
		//into the 'paramFilterSeries' attribute.
		var finalSeries = [];
		var toFilter = false;
		if (dataConfig.series){
			var strValue = dataConfig.series;
	
			var filterSeries = [];
			if (dataConfig.chart.paramFilterSeries !== undefined && dataConfig.chart.paramFilterSeries !== null){
				toFilter = true;
				filterSeries = this.getSeriesByParam(dataConfig.chart.paramFilterSeries, dataConfig.dsPars);
			}
			
			if (Ext.isArray(strValue)){
				var str = "";
				for(var i = 0; i < strValue.length; i++) {
					var alias = strValue[i].alias;
					var aliases = strValue[i].alias.split(",");
					if (aliases.length == 2) alias = aliases[1]; //cat,ser definition
					if (toFilter && this.isFilteredSerie(alias, filterSeries )){
						finalSeries.push(strValue[i]);
						str += strValue[i].alias;
						if (i < (strValue.length-1)) str += ",";
					}
				}
				if (str) {
					this.serieAlias = str.split(",");
				}
				if (toFilter) dataConfig.series = finalSeries; //updates the series
			}			
		} 
		
		//checks plotOptions.series configuration			
		if(this.serieAlias.length == 0 && dataConfig.plotOptions && dataConfig.series !== undefined){
			var str = dataConfig.series.alias;
			if (str) {
				this.serieAlias = str.split(",");
			}
		}
	}
		
	/**
	 * Load the categories for the chart
	 */
	, getCategoriesX: function(){
		
		if(this.store!=null){
		   	var categories = [];
		   	for(var j =0; j< this.categoryAliasX.length; j++){
		    	var catColumn = this.store.getFieldNameByAlias(this.categoryAliasX[j]);
				var records = this.store.getRange();
				var categoriesPerColumn = [];
				var cont = 0;
		    	for (var i = 0; i < records.length; i++) {
		    		var rec = records[i];
					if(rec ) {
						var posValue = categoriesPerColumn.indexOf(rec.get(catColumn));
						if (posValue == -1){
							categoriesPerColumn[cont]= rec.get(catColumn);
							cont++;	
						}
					}
		        }
		    	categories[j] = categoriesPerColumn;
		   	}


			return  categories;
		}
	}
	 , getCategoriesY: function(){
			
			if(this.store!=null){
			   	var categories = [];
			   	for(var j =0; j< this.categoryAliasY.length; j++){
			    	var catColumn = this.store.getFieldNameByAlias(this.categoryAliasY[j]);
					var records = this.store.getRange();
					var categoriesPerColumn = [];
					var cont = 0;
			    	for (var i = 0; i < records.length; i++) {
			    		var rec = records[i];
						if(rec) {
							var posValue = categoriesPerColumn.indexOf(rec.get(catColumn));
							if (posValue == -1){
								categoriesPerColumn[cont]= rec.get(catColumn);
								cont++;	
							}
						}
			        }
			    	categories[j] = categoriesPerColumn;
			   	}


				return  categories;
			}
		}
	/**
	 * Loads the series for the chart
	 */
	, getSeries: function(alias, group){
		if(this.store!=null){
			//single serie
		   	var series = [];

			//coordinates or multiple columns for 1 value

		   	if (alias != undefined && alias != null){
		   		this.serieAlias = alias.trim().split(",");
		   	}
			if(this.serieAlias.length != 1){
				var records = this.store.getRange();
		    	for (var j = 0; j < records.length; j++) {
		    		var rec = records[j].data;
					if(rec) {
						var recArray = [];
						for(i = 0; i<this.serieAlias.length; i++){			
					    	var serieColumn = this.store.getFieldNameByAlias(this.serieAlias[i]);
					    	var tmpValue = rec[serieColumn];
					    	if (tmpValue == undefined) tmpValue = 0;
					    	// 14/10/2013: patch: converts values with length 0 in null
					    	if (tmpValue!=null){
				    	       if(tmpValue.length==0) tmpValue=null;
				    	    }
					    	// end of patch
					    	var posValue = recArray.indexOf(recArray[serieColumn]);					    	
							if (posValue == -1){
								//recArray.push(rec[serieColumn]);		
								recArray.push(tmpValue);		
							}
						}						
						if(group && series.length>0){
							var name = series[series.length-1][0];
							var value = series[series.length-1][1];
							var recArrayName = recArray[0];
							var recArrayValue = recArray[1]; 
							if(name==recArrayName){
								recArrayValue = parseFloat(recArrayValue)+parseFloat(value);
								series.pop();
								recArray[0]=recArrayName;
								recArray[1]=recArrayValue;
							}
						}
						series.push(recArray);
					}
		    	}
			}else{
		    	var serieColumn = this.store.getFieldNameByAlias(this.serieAlias);
				var records = this.store.getRange();
		    	for (var i = 0; i < records.length; i++) {
		    		var rec = records[i].data;
					if(rec) {						
						var posValue = series.indexOf(rec[serieColumn]);
						if (posValue == -1){
							series.push(rec[serieColumn]);
						}
					}
		        }
			}
			return  series;
		}
	}
	
	/**
	 * Loads the series for the 	 chart where data is an array of name and y values
	 */
	, getWaterfallSeries: function(serieValues, colors){
		if(this.store!=null){
			//single serie
		   	var series = [];

			//coordinates or multiple columns for 1 value
		   	if (serieValues.alias != undefined && serieValues.alias != null){
		   		this.serieAlias = serieValues.alias.trim().split(",");
		   	}

			var records = this.store.getRange();
	    	for (var j = 0; j < records.length; j++) {
	    		var rec = records[j].data;
				if(rec) {
					var obj = {};
					var recArray = [];
					for(i = 0; i<this.serieAlias.length; i++){			
						var serieColor;
						var isSumColumn;
						var isInterSumColumn;				    	
				    	if (serieValues.isSumAlias)
				    		isSumColumn =  this.store.getFieldNameByAlias(serieValues.isSumAlias);
				    	if (serieValues.isIntSumAlias)
				    		isInterSumColumn =  this.store.getFieldNameByAlias(serieValues.isIntSumAlias);
				    	if (serieValues.colorAlias)
				    		serieColor =  this.store.getFieldNameByAlias(serieValues.colorAlias);
				    	var serieColumn =  this.store.getFieldNameByAlias(this.serieAlias[i]);
				    	var tmpValue =  rec[serieColumn];
				    	var isSum = (isSumColumn)? rec[isSumColumn]:false;
				    	var isInterSum = (isInterSumColumn)? rec[isInterSumColumn]:false;
				    	var color = (serieColor)? rec[serieColor]:undefined;
				    	if (color == "") color = undefined;
				    	if (tmpValue == undefined) tmpValue = 0;
				    	if (tmpValue!=null){
			    	       if(tmpValue.length==0) tmpValue=null;
			    	    }
				    	var posValue = recArray.indexOf(recArray[serieColumn]);					    	
						if (posValue == -1){
							if ( this.chartConfig.xAxis && this.chartConfig.xAxis.alias &&
									this.serieAlias[i] == this.chartConfig.xAxis.alias){
								obj.name = tmpValue;								
							}else{
								if (isSum && isSum == "true"){
									obj.isSum = true;
									obj.color  =  color || colors.sumColor || "";
								}
								else if (isInterSum && isInterSum == "true") {
									obj.isIntermediateSum = true;
									obj.color =   color ||  colors.intSumColor || colors.sumColor || Highcharts.getOptions().colors[1];
								}
								else{
									obj.y = tmpValue;
									obj.color = color;									
								}
							}	
						}
					}
					obj.borderColor = obj.color;
					series.push(obj);
				}
	    	}
		
			return  series;
		}
	}
	
	
    , format: function(value, type, format) {
    	if(value==null){
    		return value;
    	}
		try {
			var valueObj = value;
			if (type == 'int') {
				valueObj = parseInt(value);
			} else if (type == 'float') {
				valueObj = parseFloat(value);
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
    
    , initStore: function(config, dsId) {
    	this.storeManager = new Sbi.engines.chart.data.StoreManager({datasetsConfig: config});	
		this.store = this.storeManager.getStore(dsId);
		this.store.loadStore();
		if (this.store === undefined) {
			Sbi.exception.ExceptionHandler.showErrorMessage('Dataset with identifier [' + this.storeId + '] is not correctly configurated');			
		}else{		
			this.store.on('load', this.onLoad, this);
			this.store.on('exception', Sbi.exception.ExceptionHandler.onStoreLoadException, this);
			//this.store.on('exception', Sbi.exception.ExceptionHandler.handleFailure, this);
			this.store.on('metachange', this.onMetaChange, this);
		}
	}
    
    , onMetaChange: function( store, meta ) {
		var i;
	    var fieldsMap = {};

		var tmpMeta =  Ext.apply({}, meta); // meta;
		var fields = tmpMeta.fields;
		tmpMeta.fields = new Array(fields.length);
		
		for(i = 0; i < fields.length; i++) {
			if( (typeof fields[i]) === 'string') {
				fields[i] = {name: fields[i]};
			}
			
			if (this.columnId !== undefined && this.columnId === fields[i].header ){
				fields[i].hidden = true;
			}
			tmpMeta.fields[i] = Ext.apply({}, fields[i]);
			fieldsMap[fields[i].name] = i;
		}
	   
		//adds numeration column    
		tmpMeta.fields[0] = new Ext.grid.RowNumberer();
	}
    , enableDrillEvents: function(dataConfig){
    	var drill = dataConfig.drill;
    	if(drill != null && drill !== undefined){
    		var doc = drill.document;

    		var event = {
    				click: function(ev){
		        		var params = "";
		        		for(var i = 0; i< drill.param.length; i++){
		        			if(drill.param[i].type == 'ABSOLUTE'){
		        				if(params !== ""){
		    	    				params+="&";
		    	    			}
		        				params+= drill.param[i].name +"="+drill.param[i].value;
		        			}
		        		}
		        		var relParams = dataConfig.dsPars;
		        		for(var i = 0; i< drill.param.length; i++){
		        			if(drill.param[i].type == 'RELATIVE'){
		        				for(var y =0; y<relParams.length; y++){		        					
		        					if(relParams[y].name == drill.param[i].name){
		        						if(params !== ""){
				    	    				params+="&";
				    	    			}
				        				params+= drill.param[i].name +"="+relParams[y].value+"";
			    	    				params+="&";
		        					}
		        				}
		        			}
		        		}
    					//alert(this.name+" "+ev.point.x +" " +ev.point.y);
    		    		for(var i = 0; i< drill.param.length; i++){
    		    			if(drill.param[i].type == 'CATEGORY'){
    		    				if(params !== ""){
		    	    				params+="&";
		    	    			}
    		    				if(ev.point.category !== undefined){
    		    					params+= drill.param[i].name +"="+ev.point.category;
    		    				}else{
    		    					params+= drill.param[i].name +"="+ev.point.name;
    		    				}    		    				
    		    			}
    		    			
    		    		}
    		    		for(var i = 0; i< drill.param.length; i++){
    		    			if(drill.param[i].type == 'SERIE'){
    		    				if(params !== ""){
		    	    				params+="&";
		    	    			}
    		    				params+= drill.param[i].name +"="+ev.point.y;
    		    			}
    		    			
    		    		}

    		    		for(var i = 0; i< drill.param.length; i++){
    		                if(drill.param[i].type == 'SERIE_NAME'){
    		                  if(params !== ""){
    		                    params+="&";
    		                  }
    		                  params+= drill.param[i].name +"="+ev.point.series.name;                           
    		                }             
    		            }
    					parent.execCrossNavigation("iframe_"+dataConfig.docLabel, doc, params);
    				}
    		};
    		//depending on chart type enables click navigation events
    		if(doc !== undefined && doc != null ){
				if(dataConfig.series !== undefined){
    				if(dataConfig.series.length !== undefined){
    					for(var i =0; i< dataConfig.series.length; i++){
    						dataConfig.series[i].events = event;
    					}
    				}else{
    					dataConfig.series.events = event;
    				}
				}    			
    		}
    	}
    	
    	
    }
    
    , defineSeriesData: function(config){
		//gets series values and adds theme to the config
		var seriesNode = [];

		if (config.series !== undefined ){
			var serieValue = config.series;
			if (Ext.isArray(serieValue)){
				var seriesData =  {};
				var str = "";
				for(var i = 0; i < serieValue.length; i++) {
					seriesData = serieValue[i];					
					seriesData.data = this.getSeries(serieValue[i].alias);//values from dataset
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
    ,onLoad: function(){
    	this.getCategoriesX();
    	this.getCategoriesY();
    	this.getSeries();
    	this.createChart();
    }
   
    //checks if there are some $F{<field>} to substitute into template (title, subtitle, x_axis, y_axis)
    , setFieldValuesIntoTemplate: function(config){    	
    	//checks title text   
    	if (config.title && config.title.text)
    		config.title.text = this.checkTextFields(config.title.text);
    	//checks subtitle text  
    	if (config.subtitle && config.subtitle.text)
    		config.subtitle.text = this.checkTextFields(config.subtitle.text);
    	//checks xAxis text  
    	if (config.xAxis && config.xAxis.title && config.xAxis.title.text)
    		config.xAxis.title.text = this.checkTextFields(config.xAxis.title.text);
    	//checks yAxis text  
    	if (config.yAxis && config.yAxis.title && config.yAxis.title.text)
    		config.yAxis.title.text = this.checkTextFields(config.yAxis.title.text);
		
    }

    , checkTextFields: function(text){
    	var aliasFields = [];
    	var prefix = "$F{";
    	var suffix = "}";
    	
    	aliasFields = this.getFieldLabels(text);
		for (var i=0, l=aliasFields.length; i<l; i++){
			var alias = aliasFields[i];
			var fieldValue = this.getFieldValue(alias);
			if (fieldValue !== null){
				var tmpText = text.replace(prefix + alias + suffix, fieldValue);
				text = tmpText;
			}
		}
		return text;
    }
    
	,getFieldLabels: function(text){
    	var prefix = "$F{";
    	var suffix = "}";
    	var fieldLabels = [];
    	var tmpText = text;
    	var doGetLabels = true;
    	
    	while (doGetLabels){
			if (tmpText.indexOf(prefix) >= 0){	
				var idxStart = tmpText.indexOf(prefix);
				var idxStop =  tmpText.indexOf(suffix, idxStart);
				var tmpLabel = tmpText.substring(idxStart+3, idxStop);
				var tmpLen = tmpLabel.length + 1;
				fieldLabels.push(tmpLabel);
				tmpText = tmpText.substring(tmpText.indexOf(tmpLabel)+tmpLen);
			}else{
				doGetLabels = false;
			}
    	} 
		return fieldLabels;
	}

    , getFieldValue: function(alias){
		var fieldValues = null;	
		
		if (alias === undefined || alias === null){
			return  fieldValues;
	   	}
		//gets the field value from the first record 
		//if(this.store!=null && this.store.getCount() > 0){
		if(this.store!=null ){
	    	var fieldColumn = this.store.getFieldNameByAlias(alias);
	    	    	
    		var rec = this.store.getAt(0);
			if(rec) fieldValues = rec.get(fieldColumn);						
		}
		return  fieldValues;
	}
    
    , getColors : function (config) {
		var colors = [];
		var retColors = [];
		
		if (config.colors !== undefined && config.colors[0] !== undefined && config.colors[0].color !== undefined) {
			var tmpColors =  config.colors[0].color;
			colors = tmpColors.split(",");
		}else{
			colors = Highcharts.getOptions().colors;
		}
		if(config.colors !== undefined && config.colors[0] !== undefined && config.colors[0].color !== undefined && config.colors[0].dontcut !=null && config.colors[0].dontcut !=undefined && config.colors[0].dontcut){
			return colors;
		}else{
			//adaptes the colors array to the real number of the serie (necessary for force the same color for each serie in double pie)
		    var lenColors = 0;
		    if(config.series[0] !== undefined){
			    for (var i = 0, l = config.series[0].data.length; i < l ; i++) {				
					if (lenColors == colors.length){
						lenColors = 0;
					}
					retColors.push(colors[lenColors].trim());				
					lenColors ++;
				}
		    }
			return retColors;
		}


	}
    
    , getSeriesByParam: function(filterParam, params){
    	var toReturn = [];
    	for(var i=0, l=params.length; i<l; i++){
    		var tmpPar = params[i];
    		if (tmpPar.name == filterParam){
    			toReturn = tmpPar.value.split(",");
    			break;
    		}
    	}
    	return toReturn;
    }
    
    , isFilteredSerie: function(serie, filterSeries){
    	var toReturn = false;
    	
    	for(var i=0, l=filterSeries.length; i<l; i++){
    		if (serie === filterSeries[i] ) {
//    		if (serie.indexOf(filterSeries[i]) >= 0) { //orig
    			toReturn = true;
    			break;
    		}
    	}
    	return toReturn;
    }

    /**
	 * Opens the loading mask 
	 */
    , showMask : function(){
    	this.un('afterlayout',this.showMask,this);
    	if (this.loadMask == null) {    		
    		this.loadMask = new Ext.LoadMask('GenericChartPanel', {msg: "Loading.."});
    	}
    	this.loadMask.show();
    }
	
	/**
	 * Closes the loading mask
	*/
	, hideMask: function() {
    	if (this.loadMask != null) {
    		this.loadMask.hide();
    	}
	} 
	 
});



