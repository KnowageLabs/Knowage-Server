/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 *
 */


var wf={};
var addWidgetFunctionality=function(type,config){
	wf[type]=config;
};

//to add a widget type in the cockpitModule_widgetConfigurator ,
//call the function addWidgetFunctionality from the js of the widget directive
angular.module("cockpitModule").factory("cockpitModule_widgetConfigurator",function(){
	var wc={};
	return wc;

});



angular.module("cockpitModule").service("cockpitModule_widgetServices",function($rootScope,cockpitModule_widgetConfigurator,cockpitModule_template,sbiModule_messaging,$mdDialog,sbiModule_translate,StructureTabService,$timeout,$q,cockpitModule_datasetServices,sbiModule_restServices,cockpitModule_properties,cockpitModule_widgetSelection,cockpitModule_templateServices){

	var wi=this;
	var validForm = false;
	this.validateForm = function (formValid){
		validForm = formValid;
	}
	this.isFormValid  = function (){
		return validForm;
	}
	var oldDatasetExecutions = {};
	var fullPageWidget=false;
	var widgetInit=0;
	var widgetCount=cockpitModule_templateServices.getNumberOfWidgets();
	var widIni= $rootScope.$on("WIDGET_INITIALIZED",function(){
		//enter inside this if when all widgets (length-1 call) are initialized
		//and when all ds in association are in cache (1 call).
		widgetInit++;
		if(widgetCount+1==widgetInit){
			//remove the interceptor
			widIni();
			$rootScope.$broadcast('ALL_WIDGET_INITIALIZED');
			cockpitModule_properties.all_widget_initialized=true;
		}
	});

	this.realtimeSelections = [];

	addWidgetFunctionality=function(type,config){
		cockpitModule_widgetConfigurator[type]=config;
	};
	for(var key in wf){
		addWidgetFunctionality(key,wf[key]);
	}

	this.items=[];

	this.checkForUpdatedDataset = function(modelColumns, dsId) {

		var dsColumns = cockpitModule_datasetServices.getDatasetById(dsId).metadata.fieldsMeta;
		var tempColumns = [];
		var columnsMap = {};
		var columnsNameArray = [];
		// generating a dataset column map in order to avoid nested for
		for (var k in dsColumns) {
			columnsMap[dsColumns[k].name] = dsColumns[k];
			columnsNameArray.push(dsColumns[k].name);
		}
		// cycles for already preset values
		for (var j in modelColumns) {
			if(modelColumns[j].isCalculated){
				columnsMap[modelColumns[j].name] = modelColumns[j]
			}
			if (columnsMap[modelColumns[j].name]) {
				tempColumns.push(modelColumns[j]);           // already present fields, ignores deleted ones
				if(!modelColumns[j].isCalculated){
					columnsNameArray.splice(columnsNameArray.indexOf(modelColumns[j].name),1);
				}
			}

		}

		if (columnsNameArray.length > 0) {
			for (var y in columnsNameArray) {
				tempColumns.push(columnsMap[columnsNameArray[y]]);   // pushes values not present into model

			}
		}

		return tempColumns;
	}

	this.getAllWidgets=function(){
		var ret = [];
		var numSheets = cockpitModule_template.sheets.length;
		for(var sheet = 0; sheet < numSheets; sheet++){
			ret.push.apply(ret, this.getWidgets(sheet));
		}
		return ret;
	}

	this.getCokpitIndexFromProperty = function(sheetIndex){
		var indexProperty;
		for(sheet in cockpitModule_template.sheets){
			if(cockpitModule_template.sheets[sheet].index == sheetIndex){
				indexProperty = sheet;
				break;
			}
		}
		return indexProperty;
	}

	this.getWidgets=function(sheetIndex){
		var indexProperty = wi.getCokpitIndexFromProperty(sheetIndex);
		return cockpitModule_template.sheets[indexProperty].widgets;
	};

	this.isWidgetInSheet=function(widgetId, sheetIndex){
		var indexProperty = wi.getCokpitIndexFromProperty(sheetIndex);
		var widgets = cockpitModule_template.sheets[indexProperty].widgets;
		for(var i=0; i<widgets.length; i++){
			var widget = widgets[i];
			if(widgetId == widget.id){
				return true;
			}
		}
		return false;
	};

	this.addWidget=function(sheetIndex,item){
		angular.forEach(cockpitModule_template.sheets,function(value,key){
			if(value.index==sheetIndex){
				value.widgets.push(item);
				if(cockpitModule_properties.DIRTY_WIDGETS.indexOf(item.id) == -1){
				    cockpitModule_properties.DIRTY_WIDGETS.push(item.id);
				}
				return;
			}
		})
	};

	this.setChartTemp=function(newModel,targetVisualization){
		newModel.content.chartTemplate = {"CHART" :  StructureTabService.getBaseTemplate(targetVisualization)};
		for (var i = 0; i < newModel.content.columnSelectedOfDataset.length; i++) {
			if(newModel.content.columnSelectedOfDataset[i].fieldType=="MEASURE"){

				if(newModel.content.chartTemplate.CHART.VALUES.SERIE[0].name=="") {
					newModel.content.chartTemplate.CHART.VALUES.SERIE[0].column = newModel.content.columnSelectedOfDataset[i].alias;
					newModel.content.chartTemplate.CHART.VALUES.SERIE[0].name = newModel.content.columnSelectedOfDataset[i].alias;
				} else {
					newModel.content.chartTemplate.CHART.VALUES.SERIE.push(angular.copy(newModel.content.chartTemplate.CHART.VALUES.SERIE[0]));
					newModel.content.chartTemplate.CHART.VALUES.SERIE[newModel.content.chartTemplate.CHART.VALUES.SERIE.length-1].name = angular.copy(newModel.content.columnSelectedOfDataset[i].alias);
					newModel.content.chartTemplate.CHART.VALUES.SERIE[newModel.content.chartTemplate.CHART.VALUES.SERIE.length-1].column = angular.copy(newModel.content.columnSelectedOfDataset[i].alias);
				}
				if(!newModel.content.columnSelectedOfDataset[i].aggregationSelected) newModel.content.columnSelectedOfDataset[i].aggregationSelected = 'SUM';
				if(newModel.content.columnSelectedOfDataset[i].alias.indexOf(newModel.content.columnSelectedOfDataset[i].aggregationSelected)==-1)
					newModel.content.columnSelectedOfDataset[i].alias+="_"+newModel.content.columnSelectedOfDataset[i].aggregationSelected
			} else {
				if(newModel.content.chartTemplate.CHART.VALUES.CATEGORY.name==""){
					newModel.content.chartTemplate.CHART.VALUES.CATEGORY.name = newModel.content.columnSelectedOfDataset[i].alias;
					newModel.content.chartTemplate.CHART.VALUES.CATEGORY.column = newModel.content.columnSelectedOfDataset[i].alias;
				}
			}
		}
		newModel.content.chartTemplate.CHART.type = targetVisualization.toUpperCase();
		newModel.content.chartTemplate.CHART.isCockpitEngine = true;
		newModel.content.chartTemplate.creationFromSecondWidget = true
	};
	this.moveWidget=function(sheetIndex,item){
		angular.forEach(cockpitModule_template.sheets,function(value,key){
			if(value.index==sheetIndex){
				value.widgets.push(item);
				if(cockpitModule_properties.DIRTY_WIDGETS.indexOf(item.id) == -1){
                    cockpitModule_properties.DIRTY_WIDGETS.push(item.id);
                }
				return;
			}
		})

	};

	this.addDSWithDrivers = function(ngModel) {
		var dsDr = cockpitModule_datasetServices.getDatasetById(ngModel.dataset.dsId);
		if(dsDr) {
			if(dsDr.drivers && dsDr.drivers.length > 0 && cockpitModule_datasetServices.selectedDSWithDrivers.length == 0) {
				cockpitModule_datasetServices.selectedDSWithDrivers.push(dsDr);
			} else if (cockpitModule_datasetServices.selectedDSWithDrivers.length > 0) {
				for(var i = 0; i < cockpitModule_datasetServices.selectedDSWithDrivers.length; i++) {
					if(cockpitModule_datasetServices.selectedDSWithDrivers[i].id.dsId == ngModel.dataset.dsId) {
						return;
					}
					cockpitModule_datasetServices.selectedDSWithDrivers.push(dsDr);
				}
			}
		}
	}

	this.loadDatasetRecords = function(ngModel, options, loadDomainValues,nature){
		if(loadDomainValues == undefined){
			loadDomainValues = false;
		}
		if (!options) options = {};
		var page = options.page;
		var itemPerPage = options.itemPerPage;
		var columnOrdering = options.columnOrdering;
		var reverseOrdering = options.reverseOrdering;
		var dsLabel = options.label;
		var dsId;

		if(ngModel.dataset!=undefined && ngModel.dataset.dsId!=undefined){
			this.addDSWithDrivers(ngModel);
//			var dataset = cockpitModule_datasetServices.getDatasetById(ngModel.dataset.dsId);
			var dataset;
			if (!Array.isArray(ngModel.dataset.dsId)){
				dataset = cockpitModule_datasetServices.getDatasetById(ngModel.dataset.dsId);
			}else{
				for (ds in ngModel.dataset.dsId){
					var tmpDS = cockpitModule_datasetServices.getDatasetById(ngModel.dataset.dsId[ds]);
					if (tmpDS.label == dsLabel) {
						dataset = tmpDS;
						break;
					}
				}
			}
			if(dataset) dsId = dataset.id.dsId;
			else dsId = ngModel.dataset.dsId[0];

			//if it's a realtime dataset don't use backend filter on charts
			if (dataset && dataset.isRealtime && ngModel.content && ngModel.content.filters) {
				var ngModelCopy = {};
				angular.copy(ngModel, ngModelCopy);
				ngModelCopy.content.filters = [];
//				return cockpitModule_datasetServices.loadDatasetRecordsById(ngModel.dataset.dsId,page,itemPerPage,columnOrdering, reverseOrdering, ngModelCopy);
				return cockpitModule_datasetServices.loadDatasetRecordsById(dsId,page,itemPerPage,columnOrdering, reverseOrdering, ngModelCopy, nature);
			}
			return cockpitModule_datasetServices.loadDatasetRecordsById(dsId,page,itemPerPage,columnOrdering, reverseOrdering, ngModel, loadDomainValues, nature);
		}
		return null;
	}

	this.isFullPageWidget=function()
	{return fullPageWidget;}

	this.setFullPageWidget=function(boolean)
	{ fullPageWidget=boolean}


	function DialogController($scope, $mdDialog) {
	   $scope.hide = function() {
	     $mdDialog.hide();
	   };

	   $scope.cancel = function() {
	     $mdDialog.cancel();
	   };
	}



	this.deleteWidget=function(sheetIndex,widget,nomessage){
	    var dirtyIndex = cockpitModule_properties.DIRTY_WIDGETS.indexOf(widget.id);
        if(dirtyIndex > -1){
            cockpitModule_properties.DIRTY_WIDGETS.splice(dirtyIndex, 1);
        }
		var indexProperty = wi.getCokpitIndexFromProperty(sheetIndex);
		if(nomessage == true){
			cockpitModule_template.sheets[indexProperty].widgets.splice(cockpitModule_template.sheets[indexProperty].widgets.indexOf(widget),1);
			this.setFullPageWidget(false);
		}else{
			var confirm = $mdDialog.confirm()
			.title(sbiModule_translate.load("sbi.cockpit.widget.delete.title"))
			.textContent(sbiModule_translate.load("sbi.cockpit.widget.delete.content"))
			.ariaLabel('delete widget')
			.ok(sbiModule_translate.load("sbi.ds.wizard.confirm"))
			.cancel(sbiModule_translate.load("sbi.ds.wizard.cancel"));
			$mdDialog.show(confirm).then(function() {
				cockpitModule_template.sheets[indexProperty].widgets.splice(cockpitModule_template.sheets[indexProperty].widgets.indexOf(widget),1);

				wi.setFullPageWidget(false);
			});
		}
	};

	this.parametersAreSet = function() {
		if(cockpitModule_datasetServices.newDataSet) {
			var newDs = cockpitModule_datasetServices.newDataSet;
			for(var i = 0; i < newDs.parameters.length; i++) {
				if(newDs.parameters[i].value) {
					cockpitModule_datasetServices.parameterHasValue = cockpitModule_datasetServices.areParametersSet[newDs.id.dsId] = true;
				} else {
					cockpitModule_datasetServices.parameterHasValue = cockpitModule_datasetServices.areParametersSet[newDs.id.dsId] = false;
				}
			}
			return cockpitModule_datasetServices.areParametersSet[newDs.id.dsId];
		}
	}

	this.driversAreSet = function(config) {
		var ds;
		if(config.dataset) {
			ds = cockpitModule_datasetServices.getDatasetById(config.dataset.dsId);
		}
		if(ds && ds.drivers) {
			for(var i = 0; i < ds.drivers.length; i++) {
				if(ds.drivers[i].parameterValue) {
					cockpitModule_datasetServices.areDriversSet[ds.id.dsId] = true;
				} else {
					cockpitModule_datasetServices.areDriversSet[ds.id.dsId] = false;
				}
			}
			return cockpitModule_datasetServices.areDriversSet[ds.id.dsId];
		}
	}

	this.initWidget=function(element,config,options){
		this.parametersAreSet();
		this.driversAreSet(config);
		element.addClass('fadeOut');
		element.removeClass('fadeIn');

		try{
			var width = angular.element(element)[0].parentElement.offsetWidth;
			var height = angular.element(element)[0].parentElement.offsetHeight;

			if(this.isWidgetInSheet(config.id, cockpitModule_properties.CURRENT_SHEET)){
				if(config.dataset!=undefined && config.dataset.dsId!=undefined  && cockpitModule_widgetSelection.haveSelection() && cockpitModule_properties.all_widget_initialized!=true){
					cockpitModule_datasetServices.loadDatasetRecordsById(config.dataset.dsId,options.page,options.itemPerPage,options.columnOrdering, options.reverseOrdering, config)
					.then(function(){
						$rootScope.$broadcast("WIDGET_INITIALIZED");
					},function(){});
				}else{
					$rootScope.$broadcast("WIDGET_EVENT"+config.id,"INIT",{element:element,width:width,height:height});
					$rootScope.$broadcast("WIDGET_INITIALIZED");
				}
			}else{
				$rootScope.$broadcast("WIDGET_INITIALIZED");
			}
		}catch(err){
			console.error("The init function of "+config.type+" widget is not configured",err)
		}

		$timeout(function() {
			element.addClass('fadeIn');
			element.removeClass('fadeOut');
		}, 400);
	};

	this.refreshWidget = function(element, config, nature, options, data, changedChartType){
		var currentSheet = -1;
		if(nature != 'init'){
			for(i in cockpitModule_template.sheets){
				for(j in cockpitModule_template.sheets[i].widgets){
					if(cockpitModule_template.sheets[i].widgets[j].id == config.id){
						currentSheet = cockpitModule_template.sheets[i].index;
						break;
					}
				}
			}
		}

		var dirtyIndex = cockpitModule_properties.DIRTY_WIDGETS.indexOf(config.id);
		if(nature == 'init' || currentSheet == cockpitModule_properties.CURRENT_SHEET){
			if(dirtyIndex > -1){
				cockpitModule_properties.DIRTY_WIDGETS.splice(dirtyIndex, 1);
			}
			var thisSheetDefaultSelections = cockpitModule_properties.HASDEFAULTSELECTION[cockpitModule_properties.CURRENT_SHEET];
			if(nature === 'init' && config.type != 'selector' && config.updateble && config.dataset && thisSheetDefaultSelections && thisSheetDefaultSelections.indexOf(config.dataset.dsId) != -1) return;
			var width = angular.element(element)[0].parentElement.offsetWidth;
			var height = angular.element(element)[0].parentElement.offsetHeight;
			if(data == undefined) {
				if(nature == "fullExpand"){
				$rootScope.$broadcast("WIDGET_EVENT"+config.id,"REFRESH",{element:element,width:width,height:height,data:null,nature:nature,options:options});
				}else{
					if (config && config.dataset && config.dataset.dsId){

//					var dataset = cockpitModule_datasetServices.getDatasetById(config.dataset.dsId);
					var dataset;
					if (!Array.isArray(config.dataset.dsId)){
						dataset = cockpitModule_datasetServices.getDatasetById(config.dataset.dsId);
					}else{
						for (ds in config.dataset.dsId){
							var tmpDS = cockpitModule_datasetServices.getDatasetById(config.dataset.dsId[ds]);
							if (tmpDS.label == options.label) {
								dataset = tmpDS;
								break;
							}
						}
					}

						//for realtime dataset the associative selections are managed client side
					if (dataset && dataset.isRealtime && dataset.useCache && (nature=='selections' || nature=='filters')){
							var selections = cockpitModule_widgetSelection.getCurrentSelections(dataset.label);
						var filters = cockpitModule_widgetSelection.getCurrentFilters(dataset.label);

						for (var filterProp in filters) {
							if(selections[filterProp]==undefined){
								selections[filterProp]=filters[filterProp];
							}else{
								for (var filterProp2 in filters[filterProp]) {
									if(selections[filterProp][filterProp2]==undefined){
										selections[filterProp][filterProp2]=filters[filterProp][filterProp2];
									}else{
										selections[filterProp][filterProp2].push.apply(selections[filterProp][filterProp2], [filterProp][filterProp2]);
									}
								}
							}
						}

							if (Object.keys(selections).length === 0 && selections.constructor === Object){
								//cleaned selections
								this.realtimeSelections.length = 0;
							} else {
								//save dataset and selections inside this array (that can be watched outside)
							var realtimeSelection = {'datasetId':config.dataset.dsId, 'selections':selections};
							var found = false;
							for(var i=0; i<this.realtimeSelections.length; i++){
								if(this.realtimeSelections[i].datasetId == config.dataset.dsId){
									this.realtimeSelections[i] = realtimeSelection;
									found = true;
								}
							}
							if(!found){
								this.realtimeSelections.push(realtimeSelection);
							}
							}
							return;
						}
					}

					if(config.type != "selector" || nature == 'init'){
						var currDatasetExecution = this.loadDatasetRecords(config, options, config.type == "selector",nature);
						oldDatasetExecutions[config.id] = [];

						if(currDatasetExecution == null){
							$rootScope.$broadcast("WIDGET_EVENT"+config.id,"REFRESH",{element:element,width:width,height:height,data:undefined,nature:nature,options:options});
						}else{
							/*
								author: rselakov, Radmila Selakovic,
								radmila.selakovic@mht.net
								checking type of widget because of removing load spinner
								in case of updating charts
							*/
							if (options.type && (options.type!="chart" || options.chartInit)){
								$rootScope.$broadcast("WIDGET_EVENT"+config.id,"WIDGET_SPINNER",{show:true});
							}

							$q.all(oldDatasetExecutions[config.id]).then(function() {
								currDatasetExecution.then(function(data){
									if(config.type == "selector"){
										if(cockpitModule_widgetSelection.getLastTimestampedSelection() != undefined
												&& !cockpitModule_widgetSelection.isLastTimestampedSelection(config.dataset.label, config.content.selectedColumn.name)){
										data.activeValues = wi.loadDatasetRecords(config, options, false);
										}else{
											config.activeValues = null;
										}
									}

									$rootScope.$broadcast("WIDGET_EVENT"+config.id,"WIDGET_SPINNER",{show:false});
									$rootScope.$broadcast("WIDGET_EVENT"+config.id,"REFRESH",{element:element,width:width,height:height,data:data,nature:nature,changedChartType:changedChartType, "chartConf":data, options:options});
								}, function(){
									for (var i in oldDatasetExecutions[config.id]) {
										// removing promises that have failed (status 2)
										if (oldDatasetExecutions[config.id][i].$$state.status == 2) {
											oldDatasetExecutions[config.id].splice(i,1);
										}
									}
									$rootScope.$broadcast("WIDGET_EVENT"+config.id,"WIDGET_SPINNER",{show:false});
									console.error("Unable to load data");
								});
							}, function(error) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("kn.widgetservices.error.promise"), error);
							});

							oldDatasetExecutions[config.id].push(currDatasetExecution);
						}
					}else{
						var data = {};
					 	data.activeValues = wi.loadDatasetRecords(config, options, false);

						$rootScope.$broadcast("WIDGET_EVENT"+config.id,"REFRESH",{element:element,width:width,height:height,data:data,nature:nature});
					}
				}
			}else {
				$rootScope.$broadcast("WIDGET_EVENT"+config.id,"REFRESH",{element:element,width:width,height:height,data:data,nature:nature,options:options});
			}
		}else{
			if(dirtyIndex == -1){
				cockpitModule_properties.DIRTY_WIDGETS.push(config.id);
			}
		}
	};

	this.updateGlobalWidgetStyle=function(){
		console.log("updateGlobalWidgetStyle");
		var widgetList=document.getElementsByTagName("cockpit-widget");
		angular.forEach(widgetList, function(value, key) {
			angular.element(value).isolateScope().refreshWidgetStyle();
		});
	}

	this.checkNumOfCategory = function (category){
		if(category.length){
			return category.length
		} else {
			var groupBySplitArray = category.groupby.split(",");
			if(category.groupby=="") return 1;
			else return groupBySplitArray.length+1;
		}
	};
	this.createCompatibleCharts = function () {
		var minMaxCategoriesSeries = {"categ":{"min":{},"max":{}},"serie":{"min":{},"max":{}}};

		minMaxCategoriesSeries.categ.min.parallel = 1;
		minMaxCategoriesSeries.categ.min.sunburst = 2;
		minMaxCategoriesSeries.categ.min.treemap = 2;
		minMaxCategoriesSeries.categ.min.wordcloud = 1;
		minMaxCategoriesSeries.categ.max.wordcloud = 1;
		minMaxCategoriesSeries.categ.min.line = 1;
		minMaxCategoriesSeries.categ.min.heatmap = 2;
		minMaxCategoriesSeries.categ.max.heatmap = 2;
		minMaxCategoriesSeries.categ.min.radar = 1;
		minMaxCategoriesSeries.categ.min.bar = 1;
		minMaxCategoriesSeries.categ.min.pie = 1;

		minMaxCategoriesSeries.serie.min.parallel = 2;
		minMaxCategoriesSeries.serie.min.sunburst = 1;
		minMaxCategoriesSeries.serie.min.treemap = 1;
		minMaxCategoriesSeries.serie.max.treemap = 1;
		minMaxCategoriesSeries.serie.min.wordcloud = 1;
		minMaxCategoriesSeries.serie.max.wordcloud = 1;
		minMaxCategoriesSeries.serie.min.gauge = 1;
		minMaxCategoriesSeries.serie.min.line = 1;
		minMaxCategoriesSeries.serie.max.heatmap = 1;
		minMaxCategoriesSeries.serie.min.radar = 1;
		minMaxCategoriesSeries.serie.min.bar = 1;
		minMaxCategoriesSeries.serie.min.pie = 1;

		minMaxCategoriesSeries.charts = {
				"line":["bar","heatmap"],
				"bar":["pie","bubble","radar","sunburst","treemap","scatter","wordcloud","heatmap"],
				"pie":["bar","sunburst","treemap","wordcloud"],
				"chord":[],
				"parallel":["bubble","radar","scatter"],
				"bubble":["bar"],
				"radar":["bar"],
				"sunburst":["bar","pie","treemap"],
				"gauge":[],
				"treemap":["bar", "pie", "sunburst"],
				"scatter":[],
				"wordCloud":["bar","pie"],
				"heatmap":["bar"]
		};

		return minMaxCategoriesSeries;
	};

	this.checkCategories = function (template){
			var categories = []
			categoriesExist = template.CHART.VALUES.CATEGORY ? true : false;

			if (categoriesExist) {

				var categoryTag = template.CHART.VALUES.CATEGORY;
				if (categoryTag.length) {
					for (i=0; i<categoryTag.length; i++) {
						categories.push(categoryTag[i]);
					}
				}

				else {
					if (categoryTag.groupby.indexOf(",") > -1) {

						if(categoryTag.groupbyNames.indexOf(",") > -1) {

							categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,stacked:"",stackedType:""});

							var groupBySplitArray = categoryTag.groupby.split(",");
							for (i=0; i<groupBySplitArray.length; i++) {
								if(groupBySplitArray[i].startsWith(" ")) groupBySplitArray[i] = groupBySplitArray[i].replace(" ","")

								var obj = {column:"", groupby:"", groupbyNames:"", name:"", orderColumn:"", orderType:"", stacked:"", stackedType:""};
								obj.column = groupBySplitArray[i];
								var groupByNameSplitArray = categoryTag.groupbyNames.split(",");
								for (var j = 0; j < groupByNameSplitArray.length; j++) {
									if(groupByNameSplitArray[i].startsWith(" ")) groupByNameSplitArray[i] = groupByNameSplitArray[i].replace(" ","")

									if(j==i){
										obj.name = groupByNameSplitArray[j];
									}
								}
								 categories.push(obj);
							}


						}

						else {

							categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:"",orderType:"",stacked:"",stackedType:""});

							var gbnCounter = 0;
							var groupBySplitArray = categoryTag.groupby.split(",");
							for (i=0; i<groupBySplitArray.length; i++) {
								var obj = {column:"", groupby:"", groupbyNames:"", name:"", orderColumn:"", orderType:"", stacked:"", stackedType:""};
								 if(categoryTag.groupbyNames!="" && gbnCounter==0) {
									 obj.column = groupBySplitArray[i];
									 obj.name = categoryTag.groupbyNames;
									 gbnCounter++;
								 } else {
									 obj.column = groupBySplitArray[i];
									 obj.name = "";
								 }
								 categories.push(obj);
							}
						}

					}
					else {
						if(categoryTag.groupby=="" && categoryTag.column!=""){
							categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,stacked:"",stackedType:""});
						} else {

							 if(categoryTag.name=="" && categoryTag.column!=""){
								 categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:"",orderType:"",stacked:"",stackedType:""});
								 } else if(categoryTag.name!="" && categoryTag.column!="") {
									 categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,stacked:"",stackedType:""});
								 }

							 if(categoryTag.groupbyNames!="") {
								 categories.push({column:categoryTag.groupby,groupby:"", groupbyNames:"",name:categoryTag.groupbyNames, orderColumn:"",orderType:"",stacked:"",stackedType:""});
							 } else if (categoryTag.column!="") {
								 categories.push({column:categoryTag.groupby,groupby:"", groupbyNames:"",name:categoryTag.groupbyNames, orderColumn:"",orderType:"",stacked:"",stackedType:""});
							 }
						}
					}
				}
			}

			return categories;

	}
	this.compatibleCategories = function (chartType, categories, maxcateg){
		var maxcategory = maxcateg ? maxcateg : categories.length;
		var categ = {};
		if (chartType.toUpperCase() == "SUNBURST" || chartType.toUpperCase() == "WORDCLOUD" ||
				chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "PARALLEL" ||
					chartType.toUpperCase() == "HEATMAP" || chartType.toUpperCase() == "CHORD") {
			if(categories.length>maxcategory) categories.length = maxcategory;
			return categories;
		} else if (chartType.toUpperCase() != "GAUGE"){
			categ = {
							column:"",
							groupby:"",
							groupbyNames:"",
							name:"",
							orderColumn:"",
							orderType:"",
							stacked:"",
							stackedType:""
					};
			for (var i = 0; i < maxcategory; i++) {
				if(i==0){
					categ.column = categories[i].column;
					categ.name = categories[i].name;
					if(categ.orderColumn==""){
						categ.orderColumn = categories[i].orderColumn;
					}

					if(categ.orderType==""){
						categ.orderType = categories[i].orderType;
					}
					if(categ.stacked==""){
						categ.stacked = categories[i].stacked;
					}
					if(categ.stackedType==""){
						categ.stackedType = categories[i].stackedType;
					}
				} else {
					if(categ.groupby==""){
						categ.groupby = categories[i].column;
					} else {
						categ.groupby = categ.groupby +", "+ categories[i].column;
					}
					if(categ.groupbyNames=="") {
						if(categories[i].name!="") {
							categ.groupbyNames = categories[i].name;
						}
					} else {
						if(categories[i].name!="") {
							categ.groupbyNames = categ.groupbyNames + ", " + categories[i].name;
						}
					}
				}
			}
			return categ
		}
	}

});

