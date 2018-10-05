/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


angular.module('chartInitializer')

.service('d3js244',['chartEngineSettings',function(chartEngineSettings){
	
	this.cleanChart = function(panel){
		
		d3.select(panel).selectAll("*").remove();
	}
	
	this.renderChart = function(renderObj){
		
		var chartConf = renderObj.chartConf;
		var panel = renderObj.element;
		var handleCockpitSelection = renderObj.handleCockpitSelection;
		var exportWebApp = renderObj.exportWebApp;
		var locale = renderObj.locale;
		
		if(!exportWebApp){
			if(!locale){
				locale = window.navigator.userLanguage || window.navigator.language;
			}
		}
		this.cleanChart(panel);	
		
		if(chartConf.chart.type.toLowerCase() == "wordcloud") {			
			renderWordCloud(chartConf,panel,handleCockpitSelection,locale,this.handleCrossNavigationTo);			
		}
		else if (chartConf.chart.type.toLowerCase() == "sunburst") {
			renderSunburst(chartConf,panel,handleCockpitSelection,locale,this.handleCrossNavigationTo);			
		}
		else if (chartConf.chart.type.toLowerCase() == "parallel") {
			renderParallelChart(chartConf,panel,handleCockpitSelection,chartEngineSettings,locale,this.handleCrossNavigationTo);			
		}
		else if (chartConf.chart.type.toLowerCase() == "chord") {
			renderChordChart(chartConf,panel,handleCockpitSelection,locale,this.handleCrossNavigationTo);
		}
		else {
			alert("Chart not defined");
		}
	}
	
	this.initChartLibrary = function(panel){
		
	}
	
	this.handleCockpitSelection = function(e){
		
		var cockpitWidgetManager = window.parent.cockpitPanel.widgetContainer.widgetManager;
		var cockpitWidgets = cockpitWidgetManager.widgets;
		//var widgetId = Sbi.chart.viewer.ChartTemplateContainer.widgetId;
		
		var selections = {};
//			selections[e.point.name] = {values: [e.point.series.name]};
		
		for(var i = 0; i < cockpitWidgets.getCount(); i++) {
			var widget = cockpitWidgets.get(i);
			
			if(widget && widget.wtype === 'chart' && widget.id === widgetId){
				
//					var fieldMeta = widget.getFieldMetaByValue(e.categoryValue);
//					var categoryFieldHeader = fieldMeta!=null?fieldMeta.header: null;
				
//				selections[categoryFieldHeader] = {values: [e.categoryValue]};					
				
				for(var category in e){
					
					
					 if (e.hasOwnProperty(category)) {
						 selections[category] = {values: [e[category]]};
					   
					    }
				}
             
                
					//console.log(selections);
				cockpitWidgetManager.onSelection(widget, selections);
			}
		}		
	}
	
	
	this.handleCrossNavigationTo = function(e,chartType){
		
		if (!e.seriesOptions) {
			
			if(chartType=="SUNBURST") {
								
				if(parent.execExternalCrossNavigation) {
					
	            	/*
	            		Start the cross navigation according to the navigation parameters. This is the implementation that satisfied the new
	            		cross-navigation implementation (the cross-navigation data is not persisted in the chart document template ever more).
	            		@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	            	*/
	            	var navigParams = e;
					parent.execExternalCrossNavigation(navigParams,{},undefined,undefined); 
	            	
	            }
				else {
				
					/* Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
						    "SUNBURST",
							e.crossNavigationDocumentName, 
							e.crossNavigationDocumentParams,
							null,
							null,
							null,
							null,
							null,
						    null,
						    e.stringParameters
							); */
	            }
			
			}
			/*
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			else if (chartType=="CHORD") {
				
				var navigParams = e;
				parent.execExternalCrossNavigation(navigParams,{},undefined,undefined);
				
			}
			else {
			
				var chart = this;
				//chart.showLoading('Loading...');
				var categoryName=e.categoryName;
				var categoryValue = e.categoryValue;
				var serieName=e.serieName;
				var serieValue = e.serieValue;
				var groupingCategoryName=e.groupingCategoryName;
				var groupingCategoryValue=e.groupingCategoryValue;
							
				if(parent.execExternalCrossNavigation) {
					
					var navData = {
	            			chartType:	"D3CHART",
	            			documentName:e.crossNavigationDocumentName,
	            			documentParameters:e.crossNavigationDocumentParams,
	            			CATEGORY_NAME: categoryName,
	            			CATEGORY_VALUE: categoryValue,
	            			SERIE_NAME: serieName,
	            			SERIE_VALUE: serieValue,
	            			
	            			// OLD IMPLEMENTATION: commented by danristo
	            			//groupingCategoryName: groupingCategoryName,
	            			//groupingCategoryValue: groupingCategoryValue,
	            			
	            			// NEW IMPLEMENTATION: danristo
	            			GROUPING_NAME: groupingCategoryName,
	            			GROUPING_VALUE: groupingCategoryValue,
	            			
	            			stringParameters:null
	            	};   
					
					parent.execExternalCrossNavigation(navData,{},undefined,undefined); 
					
            	}
				else {
					
					/* Sbi.chart.viewer.CrossNavigationHelper.navigateTo(
							"D3CHART",
							e.crossNavigationDocumentName, 
							e.crossNavigationDocumentParams,
							categoryName,
							categoryValue,
							serieName,
							serieValue,
							groupingCategoryName,
							groupingCategoryValue
							); */
	           	 	}
				
				}
			
				// commented by: danristo
				//var chartServiceManager = Sbi.chart.rest.WebServiceManagerFactory.getChartWebServiceManager();
			
				//chart.hideLoading();
		}
		
	};
	
	this.transformeData = function(widgetData, data){
		
		var dataForReturn = {};
		
		dataForReturn.metaData = {};
		dataForReturn.metaData.fields = [];
		dataForReturn.metaData.fields.push("recNo");
		dataForReturn.metaData.id = data.metaData.id;
		dataForReturn.metaData.root = data.metaData.root;
		dataForReturn.metaData.totalProperty = data.metaData.totalProperty;
		
		dataForReturn.results = data.results;
		
		dataForReturn.rows = [];
		
		
		var category = null;
		var column = null;
		var orderColumn = null;
	
		var counter = 0;
		var arrayOfMeasuers = [];

		var arrayOfAttributes= [];
		var arrayOfDatasetColumns= [];
		var counterAtt = 1;
		var counterMeas = 2;
		for (var i = 0; i<widgetData.columnSelectedOfDataset.length; i++){

			if(widgetData.columnSelectedOfDataset[i].fieldType!="MEASURE"){
				var objekatCat = {};
				objekatCat.name = "column_"+counterAtt;
				objekatCat.header = widgetData.columnSelectedOfDataset[i].alias;
				objekatCat.nameAgg = widgetData.columnSelectedOfDataset[i].name;
				objekatCat.dataIndex = "column_"+counterAtt;
				counterAtt++;
				objekatCat.type = "";
				arrayOfAttributes.push(objekatCat)
				arrayOfDatasetColumns.push(objekatCat.nameAgg);
			} else {
				var objekatSer = {};
				objekatSer.name = "column_"+counterMeas;
				objekatSer.header = widgetData.columnSelectedOfDataset[i].alias;
				objekatSer.nameAgg = widgetData.columnSelectedOfDataset[i].name;
				objekatSer.dataIndex = "column_"+counterMeas;
				counterMeas++;
				objekatSer.type = "";
				arrayOfMeasuers.push(objekatSer)
				arrayOfDatasetColumns.push(objekatSer.nameAgg);
			}
		}

		Array.prototype.push.apply(dataForReturn.metaData.fields, arrayOfAttributes);
		Array.prototype.push.apply(dataForReturn.metaData.fields, arrayOfMeasuers);
		var newArrayofFields = [];
		var oldArrayofFields = [];
		for (var i = 0; i<data.metaData.fields.length; i++){
			if (arrayOfDatasetColumns.indexOf(data.metaData.fields[i].header)>-1){
				newArrayofFields.push({"columnName":data.metaData.fields[i].name});
			
			}
		
		}
		var counterId = 1;
		var row = {
				//"id":counterId
		}
	/*	for (var i = 0; i<dataForReturn.metaData.fields.length; i++){
			if (dataForReturn.metaData.fields[i].name){

				row[dataForReturn.metaData.fields[i].name] = "";
			
			}
		
		}*/
		for (var i = 0; i<data.rows.length; i++){
			for (var j = 0; j<newArrayofFields.length; j++){
				var rowProp = newArrayofFields[j].columnName;
				if(data.rows[i].hasOwnProperty(rowProp)){
					
					row[rowProp] = data.rows[i][rowProp];
				}
			}
			dataForReturn.rows.push(row)
			row = {};
		}
		
		for (var i = 0; i<data.metaData.fields.length; i++){
			for (var j = 0; j<dataForReturn.metaData.fields.length; j++){
				if(data.metaData.fields[i].header && dataForReturn.metaData.fields[j].nameAgg){
					if(data.metaData.fields[i].header == dataForReturn.metaData.fields[j].nameAgg){
						dataForReturn.metaData.fields[j].type = data.metaData.fields[i].type;
						dataForReturn.metaData.fields[j].name = data.metaData.fields[i].name;
					}
				}
			}
		}
	
		return dataForReturn;
	}

}])