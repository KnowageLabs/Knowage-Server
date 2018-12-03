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
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it) v0.0.1
 *
 */
(function(){
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('cockpitModule')
.config( ['$compileProvider', function( $compileProvider ){   
        	$compileProvider.aHrefSanitizationWhitelist(/^\s*(https?|ftp|mailto|chrome-extension|data):/);
    	}
])
.directive('autoCompileWidget', function ($compile) {
            return {
                restrict: 'A',
                replace: true,
                link: function (scope, ele, attrs) {
                	$compile(ele.contents())(scope);

                }
            };
        })
 .directive('iframeFinishLoad', function ($compile) {
        	return {
        		scope: {
        			callbackFunction: '&?'
    			},
        		link: function (scope, ele, attrs) {
        			ele.on('load', function() {
        				if(scope.callbackFunction)
        					scope.callbackFunction()
    				});
        		}
        	};
        })
 .directive('actionButtonPositionHandler', function ($compile,$timeout) {
        	return {
        		restrict: 'A',

        		link: function (scope, ele, attrs) {

        			var actionButtonItem=angular.element(ele[0].querySelector("."+attrs.actionButtonClassContainer));
        			actionButtonItem.css("display", "none");
        			var showActionButton=false;
        			var lastShowActionButtonValue=(new Date).getTime();
        		    var mouseOver=function(ev){
        		    	if(angular.isObject(scope.gridster.movingItem)){
        		    		return
        		    	}
        				lastShowActionButtonValue= (new Date).getTime();;
        				handleActionButtonVisibility(true)
        			};
        			var mouseLeave=function(ev){
        				if(angular.isObject(scope.gridster.movingItem)){
        		    		return
        		    	}
        				var time=(new Date).getTime();
        				lastShowActionButtonValue=time;
        				actionButtonItem.css("z-index","2");
        				ele.css("z-index","2");
        				$timeout(function(){
    						if(angular.equals(lastShowActionButtonValue,time)){
    							handleActionButtonVisibility(false)
    						}else{
    							actionButtonItem.css("z-index","3");
    							ele.css("z-index","3");
    						}
    					},500)
        			};

        			var handleActionButtonVisibility=function(show){
        				if(!angular.equals(show,showActionButton)){
        					showActionButton=show;
        					actionButtonItem.css("display",show? "" : "none");
        					actionButtonItem.css("z-index",show? "3" : "1");
        					ele.css("z-index",show? "3" : "1");
        				}
        			}


        			actionButtonItem.bind('mouseover', mouseOver );
        			actionButtonItem.bind('mouseleave', mouseLeave );
        			ele.bind('mouseover', mouseOver );
        			ele.bind('mouseleave', mouseLeave );
        			ele.addClass(scope.$eval(attrs.widgetXPosition)<=1 ? 'rightPosition': 'leftPosition');
        			$timeout(function(){
        				ele.removeClass('rightPosition');
						ele.removeClass('leftPosition');
        				ele.addClass(scope.$eval(attrs.widgetXPosition)<=1 ? 'rightPosition': 'leftPosition');
        			},1000);



        			scope.$watch(function(){return scope.gridsterItem.isMoving()},function(newVal,oldVal){
        				if(newVal!=oldVal){
        					if(!newVal){
        						ele.removeClass('rightPosition');
        						ele.removeClass('leftPosition');
        						ele.addClass(scope.$eval(attrs.widgetXPosition)<=1 ? 'rightPosition': 'leftPosition')
        					}
        				}
        			})

        		}
        	};
        })
.filter('parameter_fill', function(cockpitModule_utilstServices) {
	return function(data) {
		return cockpitModule_utilstServices.getParameterValue(data);

	}
})
.filter('i18n', function(sbiModule_i18n) {
	return function(label) {
		return sbiModule_i18n.getI18n(label);
	}
})
.directive('cockpitWidget',function(cockpitModule_widgetConfigurator,cockpitModule_widgetServices,$compile,cockpitModule_widgetSelection,$rootScope,cockpitModule_datasetServices, cockpitModule_properties){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/templates/cockpitWidget.html',
		   controller: cockpitWidgetControllerFunction,
		   scope: {
			   ngModel: '='
		   	},
		   	compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	// inject the directive in a variable to allow the
						// access from the sub-directory
                    	scope.cockpitWidgetItem=element;
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	// init the widget
                    	element.ready(function () {
	                    		var objType=cockpitModule_widgetConfigurator[scope.ngModel.type.toLowerCase()];
	                    		var dataset;
	                    		if (scope.ngModel.dataset){
	                        		dataset = cockpitModule_datasetServices.getDatasetById(scope.ngModel.dataset.dsId)
	                    		}

	                    		if(scope.ngModel.type.toLowerCase()=="chart" && scope.ngModel.drillable == undefined){
	                    			if(scope.ngModel.cliccable){
	                    				scope.ngModel.drillable = false;
	                    			} else {
	                    				if(scope.enterpriseEdition)
	                    				scope.ngModel.drillable = true;
	                    			}

	                    		}
	                    		if(scope.ngModel.type == "selector"){
	                    			scope.updateble = true;
	                    		}else{
	                    			scope.updateble=objType.updateble==undefined? true : objType.updateble;
	                    		}
	                    		scope.cliccable=objType.cliccable==undefined? true : objType.cliccable;

	                    		if(objType!=undefined){
	                    			var directive = document.createElement("cockpit-"+scope.ngModel.type.toLowerCase()+"-widget" );
	                    			var content=element[0].querySelector("md-card-content");
	                    			content.appendChild(directive);
	                    			$compile(content)(scope) ;
	                    			scope.initializeWidgetProperty(directive);
	                    			scope.subCockpitWidget=angular.element(directive);
	                    			scope.gridsterItem=angular.element(scope.cockpitWidgetItem[0].querySelector("li.gridster-item"))
	                    		}else{
	                    			console.error(scope.ngModel.type+" widget not defined");
	                    		}

	                    		scope.refreshWidgetStyle();

                        });

                    	scope.initializeWidgetProperty=function(directive){

                    		scope.initWidget=function(){
                    			var initOnFinish = scope.ngModel.isNew == true;
                    			if(initOnFinish){
                    				scope.doEditWidget(initOnFinish).then(function(){
                    					scope.ngModel.isNew = undefined;
                    					var options = scope.getOptions == undefined? {} :  scope.getOptions();
                    					cockpitModule_widgetServices.initWidget(angular.element(directive),scope.ngModel,options);
                    				},function(){
                    					scope.deleteWidget(true);
                    				});
                    			}else{
                    				scope.ngModel.isNew = undefined;
                    				var options = scope.getOptions == undefined? {} :  scope.getOptions();
                					cockpitModule_widgetServices.initWidget(angular.element(directive),scope.ngModel,options);
                    			}
                    		};

                    		scope.refreshWidget=function(options,nature,changedChartType){
                    		    var finOptions;
                    		    if(options) {
                    		        finOptions = scope.getOptions == undefined ? options : angular.merge({}, scope.getOptions(), options);
                    		    } else {
                    		        finOptions = scope.getOptions == undefined ? {} :  scope.getOptions();
                    		    }
                    			cockpitModule_widgetServices.refreshWidget(angular.element(directive),scope.ngModel,nature==undefined? 'refresh' : nature, finOptions, undefined, changedChartType);
                    		};
                    	}

                    }
                };
		   	}
	   }
});

function cockpitWidgetControllerFunction(
		$scope,
		$rootScope,
		cockpitModule_widgetServices,
		cockpitModule_properties,
		cockpitModule_template,
		cockpitModule_analyticalDrivers,
		cockpitModule_datasetServices,
		sbiModule_restServices,
		$q,
		cockpitModule_documentServices,
		cockpitModule_crossServices,
		cockpitModule_widgetSelection,
		$timeout,
		cockpitModule_gridsterOptions,
		sbiModule_translate,
		sbiModule_user,
		sbiModule_i18n,
		sbiModule_config,
		$filter,
		$sce,
		$mdDialog,cockpitModule_backwardCompatibility)
	{

	$scope.openMenu = function($mdMenu, ev) {
	      $mdMenu.open(ev);
	    };
	    
	if(!cockpitModule_backwardCompatibility.compareVersion(cockpitModule_properties.CURRENT_KNOWAGE_VERSION,$scope.ngModel.knowageVersion)){
		$scope.ngModel = cockpitModule_backwardCompatibility.updateModel($scope.ngModel);
	}
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.translate		= sbiModule_translate;
	$scope.i18n		= sbiModule_i18n;
	$scope.enterpriseEdition = (sbiModule_user.functionalities.indexOf("EnableButtons")>-1)? true:false;
	$scope.tmpWidgetContent	= {};
	$scope.editingWidgetName= false;
	$scope.extendedStyle	= {};

	$scope.borderShadowStyle= {};
	$scope.titleStyle		= {};

	$scope.widgetSpinner	= false;
	$scope.widgetSearchBar 	= false; // default searchBar unactive
	$scope.activeSearch 	= false; // default search unactive
	$scope.actionButtonClass=[];

	$scope.widgetActionButtonsVisible = false;
	$scope.widExp = false;

	$scope.showWidgetActionButtons = function(){
		$scope.widgetActionButtonsVisible = $scope.widgetActionButtonsVisible?false:true;
	}

	$scope.closeWidgetActionButtons = function() {
		if(!$scope.widExp){
			$scope.widgetActionButtonsVisible=false;
		}
	}

	if($scope.ngModel.style && $scope.ngModel.style.title && $scope.ngModel.style.title.label){
		$scope.ngModel.content.name = $scope.ngModel.style.title.label;
	}

	// davverna - initializing search object to give all the columns to the user searchbar
	if($scope.ngModel.type.toLowerCase() == "table" && (!$scope.ngModel.search || $scope.ngModel.search.columns == [])){
		$scope.ngModel.search ={"columns" : []};
		for(var k in $scope.ngModel.content.columnSelectedOfDataset){
			var column = $scope.ngModel.content.columnSelectedOfDataset[k];
			if(column.fieldType == "ATTRIBUTE" && column.type == "java.lang.String"){
				$scope.ngModel.search.columns.push(column.name);
			}
		}
	}

	// davverna - method to set the actual model and search parameters to refresh the widget table
	$scope.searchColumns = function(){
		if($scope.ngModel.search.text != "" && $scope.ngModel.search.columns.length > 0){
			$scope.activeSearch = true;
			$scope.refreshWidget();
		}
	}

	// davverna - reset the actual search if active
	$scope.resetSearch = function(){
		$scope.ngModel.search.text = "";
		$scope.activeSearch = false;
		$scope.refreshWidget();
	}



	// global WIDGET_EVENT
	$rootScope.$on('WIDGET_EVENT',function(conf,eventType,config){
		switch(eventType){
		case "UPDATE_FROM_SELECTION"  :
			$scope.updateFromSelection(config.isInit,config.data);
			break;
		case "UPDATE_FROM_DATASET_FILTER"  :
			$scope.updateFromDatasetFilter(config.label);
			break;
		case "UPDATE_FROM_CLEAN_CACHE":

			$scope.refreshWidget();
			break;
		case "UPDATE_FROM_NEAR_REALTIME":
			var ds=$scope.getDataset();
			if(ds!=undefined && config.dsList.indexOf(ds.label)!=-1 && sbiModule_user.isAbleTo("NearRealTimeCockpit")){
				/*
				 * author: rselakov, Radmila Selakovic,
				 * radmila.selakovic@mht.net checking type of widget because of
				 * removing load spinner in case of updating charts
				 */
				$scope.refreshWidget();
			}
			break;
		case "UPDATE_FROM_REALTIME":
			var ds=$scope.getDataset();
			if( (ds != undefined) && (ds.label == config.dsLabel) ){
				var option = ($scope.getOptions == undefined) ? {} :  $scope.getOptions();
				cockpitModule_widgetServices.refreshWidget($scope.subCockpitWidget, $scope.ngModel, 'refresh', option, config.data);
			}
			break;
		case "PARAMETER_CHANGE":
			var ds=$scope.getDataset();
			if(ds!=undefined && config.dsList.hasOwnProperty(ds.label)){
				$scope.refreshWidget(undefined,"parameter_change");
			}
			var doc=$scope.getDocument();
			if(doc!=undefined && config.docList.hasOwnProperty(doc.DOCUMENT_LABEL)){
				$scope.refreshWidget(undefined,"parameter_change");
			}
			break;
		default: console.error("event "+eventType+" not found")
		}
	})
	// specific widget event 'WIDGET_EVENT' with id of widget
	$rootScope.$on('WIDGET_EVENT'+$scope.ngModel.id,function(config,eventType,config){

		switch(eventType){
		case "REFRESH"  :
			if($scope.refresh==undefined){
				/*$timeout(function(){
					$scope.refresh(config.element,config.width,config.height, config.data,config.nature,config.associativeSelection);
				},1000);*/
				$scope.refresh(config.element,config.width,config.height, config.data,config.nature,config.associativeSelection);

			}else{

				$scope.refresh(config.element,config.width,config.height,config.data,config.nature,config.associativeSelection,config.changedChartType,config.chartConf,config.options);
			}
			break;
		case "INIT" :
			$scope.scopeInit(config.element,config.width,config.height, config.data,config.nature,config.associativeSelection);
			break;
		case "RESIZE" :
			if($scope.ngModel.type=="chart" || $scope.ngModel.type=="map") {
				$scope.refreshWidget(undefined, 'resize');
			}
			break;
		case "WIDGET_SPINNER" :
			if(config.show){
				$scope.showWidgetSpinner();
			}else{
				$scope.hideWidgetSpinner();
			}
			break;
		case "EXPORT_CSV" :
			if($scope.exportCsv != undefined){
				$scope.exportCsv(config);
			}else{
				config.def.resolve();
			}
			break;
		default: console.error("event "+eventType+" not found")
		}
	})

	$scope.scopeInit=function(element,width,height,data,nature,associativeSelection){
		if($scope.init == undefined){
			$timeout(function(){
				$scope.scopeInit(element,width,height,data,nature,associativeSelection);
			},1000);
		}else{
			$scope.init(element,width,height,data,nature,associativeSelection);
		}
	}

	$scope.showWidgetSpinner=function(){
		$scope.widgetSpinner=true;
		$scope.safeApply();
	}
	$scope.hideWidgetSpinner=function(){
		$scope.widgetSpinner=false;
		$scope.safeApply();
	}

	$scope.updateFromSelection = function(isInit,associativeSelection){
		var dataset= $scope.getDataset();
		if($scope.ngModel.updateble==false){
			if(dataset && $scope.cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
				$scope.cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
			}
			console.log("widget is not updateble")
			return;
		}
		var document= $scope.getDocument();
		if(dataset != undefined && cockpitModule_widgetSelection.getCurrentSelections(dataset.label)!=undefined){
				if(isInit){
					$scope.initWidget();
				}else{
					if(associativeSelection==undefined || associativeSelection.hasOwnProperty(dataset.label)){
						var option =$scope.getOptions == undefined? {} :  $scope.getOptions();
						cockpitModule_widgetServices.refreshWidget($scope.subCockpitWidget,$scope.ngModel,'selections',option);
					}
				}
		}
		if(document != undefined && cockpitModule_widgetSelection.getCurrentSelections(document.DOCUMENT_LABEL)!=undefined){
			if(isInit){
				$scope.initWidget();
			}else{
				if(associativeSelection==undefined || associativeSelection.hasOwnProperty(document.DOCUMENT_LABEL)){
					var option =$scope.getOptions == undefined? {} :  $scope.getOptions();
					cockpitModule_widgetServices.refreshWidget($scope.subCockpitWidget,$scope.ngModel,'selections',option);
				}
			}
		}


	}

	$scope.updateFromDatasetFilter=function(label){
		var dataset= $scope.getDataset(label);


		if($scope.ngModel.updateble==false){
			if(dataset && $scope.cockpitModule_properties.DS_IN_CACHE.indexOf(dataset.label)==-1){
				$scope.cockpitModule_properties.DS_IN_CACHE.push(dataset.label);
			}
			console.log("widget is not updateble")
			return;
		}
		if(dataset != undefined &&
			(
				(angular.isArray(label) && label.indexOf(dataset.label)!=-1)
				||
				(angular.isString(label) && angular.equals(label,dataset.label))
			)
		){

			var options = {};
			options.label = label;
			$scope.refreshWidget(options,'filters');


		}
	}

	$scope.safeApply=function(){
		if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
			$scope.$apply();
		}
	}

	$scope.deleteWidget=function(nomessage){
		cockpitModule_widgetServices.deleteWidget(cockpitModule_properties.CURRENT_SHEET,$scope.ngModel,nomessage);
	}

	$scope.clearAllSelectionsAndRefresh=function(){
		cockpitModule_widgetSelection.clearAllSelections();
		cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset($scope.getDataset().label);
	}


	$scope.cloneWidget = function(){
		var newModel = angular.copy($scope.ngModel);
		delete newModel.col;
		delete newModel.row;
		newModel.id = new Date().getTime();
		cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,newModel);
	}

	var createNewWidget = function (widgetType){
		var newModel = angular.copy($scope.ngModel);
		newModel.id = new Date().getTime();
		newModel.type = widgetType;
		newModel.content.wtype = widgetType;
		return newModel;
	}
	var addAliasToShow = function (columnSelectedOfDataset){
		for(var i in columnSelectedOfDataset){
			var thisDs = columnSelectedOfDataset[i];
			thisDs.alias = thisDs.name;
			thisDs.aliasToShow = thisDs.name;
		}
	}
	var prepareColumnSelectedOfDataset = function (newModel){
		delete newModel.content.columnSelectedOfDataset;
		newModel.content.columnSelectedOfDataset=[];
		if($scope.target.attribute instanceof Array){
			Array.prototype.push.apply(newModel.content.columnSelectedOfDataset, $scope.target.attribute);
			Array.prototype.push.apply(newModel.content.columnSelectedOfDataset, $scope.target.measure);
		}else{
			newModel.content.columnSelectedOfDataset.push($scope.target.attribute);
			newModel.content.columnSelectedOfDataset.push($scope.target.measure);
		}
	}
	var prepareChartWidget = function (newModel){
		newModel.content.filters= newModel.filters;
		newModel.content.designer= "Chart Engine Designer";
		cockpitModule_widgetServices.setChartTemp(newModel,$scope.target.visualization);
	}
	$scope.addTableFromChart = function(widgetType) {
		var newModel = createNewWidget(widgetType);
		addAliasToShow(newModel.content.columnSelectedOfDataset);
		cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,newModel);
	}

	$scope.addTableFromMap = function(widgetType) {
		var newModel = createNewWidget(widgetType);
		prepareColumnSelectedOfDataset(newModel);
		addAliasToShow(newModel.content.columnSelectedOfDataset);
		newModel.dataset.dsId = $scope.target.dataset;
		cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,newModel);
	}

	$scope.addChartFromTable = function (widgetType){
		var newModel = createNewWidget(widgetType);
		newModel.content.limitRows= newModel.limitRows;
		newModel.content.datasetLabel = $scope.getDataset().label;
		newModel.content.datasetId = newModel.dataset.dsId;
		newModel.dataset.dsLabel = $scope.getDataset().label;
		prepareChartWidget(newModel);
		cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,newModel);
	}

	$scope.addChartFromMap = function(widgetType) {
		var newModel = createNewWidget(widgetType);
		prepareColumnSelectedOfDataset(newModel);
		newModel.content.datasetLabel = $scope.selectedDataset.label;
		newModel.content.datasetId = $scope.target.dataset;
		newModel.dataset.dsLabel = $scope.selectedDataset.label;
		newModel.dataset.dsId = $scope.target.dataset;
		prepareChartWidget(newModel);
		cockpitModule_widgetServices.addWidget(cockpitModule_properties.CURRENT_SHEET,newModel);
	}
	//dialog to choose the sheet where to move the widget
	$scope.moveWidget = function(ev){
		$scope.targetSheet = new Object();
		$scope.targetSheet = cockpitModule_template.sheets[cockpitModule_widgetServices.getCokpitIndexFromProperty(cockpitModule_properties.CURRENT_SHEET)];
		$mdDialog.show({
			controller: function ($scope,$mdDialog,targetSheet,cockpitModule_widgetServices) {
				$scope.targetSheet = targetSheet;
				$scope.cockpitModule_widgetServices = cockpitModule_widgetServices;
				$scope.move = function(){
					$mdDialog.hide();
				}
				$scope.cancel = function(){
					$mdDialog.cancel();
				}
			},
			scope: $scope,
			preserveScope:true,
	      templateUrl: currentScriptPath+'/templates/changeSheetDialog.tpl.html',
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: { targetSheet: $scope.targetSheet, cockpitModule_widgetServices:cockpitModule_widgetServices }
	    })
	    .then(function() {
	    	if($scope.targetSheet.index!=cockpitModule_properties.CURRENT_SHEET){
		    	cockpitModule_widgetServices.moveWidget($scope.targetSheet.index,angular.copy($scope.ngModel));
				cockpitModule_widgetServices.deleteWidget(cockpitModule_properties.CURRENT_SHEET,$scope.ngModel,true);
				$scope.refreshWidget(undefined,'filters');
	    	}
	    });
	}

	$scope.openSearchBar = function(ev,widgetName){
		//$scope.widgetSearchBar == false ? $scope.widgetSearchBar = true : $scope.widgetSearchBar = false;
		$mdDialog.show({
			controller: function ($scope,$mdDialog,ngModel) {
				$scope.widgetName = widgetName;
				$scope.searchColumnsModal = function(){
					$mdDialog.hide();
				}
				$scope.cancel = function(){
					$mdDialog.cancel();
				}
			},
			scope: $scope,
			preserveScope:true,
	      templateUrl: currentScriptPath+'/templates/tableSearch.tpl.html',
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {ngModel:$scope.ngModel}
	    })
	    .then(function() {
	    	$scope.searchColumns();
	    });
	}

	$scope.chartsForDrill = ["bar","pie","line","treemap"]
	$scope.changeClickability = function(){
		if($scope.ngModel.cliccable && !$scope.ngModel.drillable && $scope.enterpriseEdition){
			$scope.ngModel.cliccable = false;
			$scope.ngModel.drillable = true;
		} else if(!$scope.ngModel.cliccable && $scope.ngModel.drillable){
			$scope.ngModel.cliccable = false;
			$scope.ngModel.drillable = false;
		}  else {
			$scope.ngModel.cliccable = true;
			$scope.ngModel.drillable = false;
		}
		$scope.$broadcast("drillClick",{ "drillable": $scope.ngModel.drillable, "cliccable": $scope.ngModel.cliccable});
	}
	$scope.doSelection = function(columnName, columnValue, modalColumn, modalValue, row, skipRefresh, dsId, disableAssociativeLogic){
		if($scope.ngModel.cliccable==false){
			console.log("widget is not cliccable")
			return;
		}

		// check if cross navigation was enable don this widget
		var model = $scope.ngModel;
		if(model.cross != undefined  && model.cross.cross != undefined
				&& model.cross.cross.enable === true
				){

			// enter cross navigation mode
			var doCross = false;

			var nameToCheckForCross = columnName;
			if(columnName != undefined){
				// check if selected column has been renamed by an alias, in that
				// case take the real name
				for(var colIndex in model.content.columnSelectedOfDataset){
					var col = model.content.columnSelectedOfDataset[colIndex];
					if(col.aliasToShow != undefined && col.aliasToShow == columnName){
						nameToCheckForCross = col.name;
						break;
					}
				}
			}

			if(model.cross.cross.allRow == true){
				// case all columns are enabled for cross, get value for cross
				// column (or alias if present)
				var crossColumnOrAlias = model.cross.cross.column;

				for(var colIndex in model.content.columnSelectedOfDataset){
					var col = model.content.columnSelectedOfDataset[colIndex];
					if(col.aliasToShow != undefined && col.name == model.cross.cross.column){
						crossColumnOrAlias = col.aliasToShow;
					}
				}
				doCross = true;
				// get value to pass to cross navigation
				if(row){
					if(row[crossColumnOrAlias]){
						columnValue = row[crossColumnOrAlias];
					}else{
						columnValue = [];
						for(var j in row){
							columnValue.push(row[j][crossColumnOrAlias]);
						}
					}
				}
			}else{
				// case a specific column is enabled for cross
				// check if column clicked is the one for cross navigation
				if(model.cross.cross.column == undefined || model.cross.cross.column === nameToCheckForCross){
					doCross = true;
				}
			}

			if(doCross === true){
				var outputParameter = {};
				if(model.cross.cross.outputParameter){
					outputParameter[model.cross.cross.outputParameter] = columnValue;
				}


				// parse output parameters if enabled
				var otherOutputParameters = [];
				var passedOutputParametersList = model.cross.cross.outputParametersList;

				// get Aliases for column
				var columnAliasesMap = {};
				if(model.content.columnSelectedOfDataset){
					for(var i = 0; i<model.content.columnSelectedOfDataset.length; i++){
						var colDataset = model.content.columnSelectedOfDataset[i];
						if(colDataset.aliasToShow && colDataset.aliasToShow != ""){
							if(colDataset.alias){
								columnAliasesMap[colDataset.alias] = colDataset.aliasToShow;
							}
						}
					}
				}

				for(par in passedOutputParametersList){
					var content = passedOutputParametersList[par];

					if(content.enabled == true){

						/*if(content.dataType == 'date' && content.value != undefined && content.value != ''){

							content.value = content.value.toLocaleDateString('en-US');
							content.value+= "#MM/dd/yyyy";
						}*/

						if(content.type == 'static'){
							var objToAdd = {};
							objToAdd[par] = content.value;
							otherOutputParameters.push(objToAdd);
						}
						else if(content.type == 'dynamic'){
							if(content.column){
								var columnNameToSearch = columnAliasesMap[content.column] ?  columnAliasesMap[content.column] : content.column;
								var valToAdd = row[columnNameToSearch];
								var objToAdd = {};
								objToAdd[par] = valToAdd;
								otherOutputParameters.push(objToAdd);
							}
						}
						else if(content.type == 'selection'){
							var selectionsObj = cockpitModule_template.getSelections();
							if(selectionsObj){
								var found = false;
								for(var i = 0; i < selectionsObj.length && found == false; i++){
									if(selectionsObj[i].ds == content.dataset && selectionsObj[i].columnName == content.column){
										var val = selectionsObj[i].value;
										var objToAdd = {};
										objToAdd[par] = val;
										otherOutputParameters.push(objToAdd);
										found = true;
									}
								}
							}
						}
					}
				}





				// parse static parameters if present
				/*var staticParameters = [];
				if(model.cross.cross.staticParameters && model.cross.cross.staticParameters != ""){
					var err=false;
					try{
						var parsedStaticPars = model.cross.cross.staticParameters.split("&");
						for(var i=0;i<parsedStaticPars.length;i++){
							var splittedPar=parsedStaticPars[i].split("=");
							if(splittedPar[0]==undefined || splittedPar[1]==undefined){err=true;}
							else{
								var toInsert = {};
								toInsert[splittedPar[0]] = splittedPar[1];
								staticParameters.push(toInsert);
							}

						}

					}catch(e){
						err=true
						console.error(e);
					}finally{
						if(err){
							 $mdDialog.show(
								      $mdDialog.alert()
								        .clickOutsideToClose(true)
								        .title(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatTitle"))
								        .content(sbiModule_translate.load("sbi.cockpit.cross.staticParameterErrorFormatMsg"))
								        //.ariaLabel('Alert Dialog Demo')
								        .ok(sbiModule_translate.load("sbi.general.continue"))
								    );
								return;
						}

						}

				}*/

				// if destination document is specified don't ask
				if(model.cross.cross.crossName != undefined){
					parent.execExternalCrossNavigation(outputParameter,{},model.cross.cross.crossName,null,otherOutputParameters);
				}
				else{
					parent.execExternalCrossNavigation(outputParameter,{},null,null,otherOutputParameters);
				}
				return;
			}
		}

		var dataset = dsId != undefined ? cockpitModule_datasetServices.getDatasetById(dsId) : $scope.getDataset();

		if(dataset && columnName){

			if(modalColumn!=undefined && modalValue!=undefined)
			{
				columnValue = modalValue;
				columnName = modalColumn;
			}

			// check if all associated data
			var dsLabel = dataset.label;
			var originalColumnName;
			if (!Array.isArray(columnName)){
				//original management with simple value as parameters (not array, not multiselection)
				originalColumnName = "";
				if ($scope.ngModel.content.columnSelectedOfDataset){
			        for(var i=0; i<$scope.ngModel.content.columnSelectedOfDataset.length; i++){
			        	if($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow && $scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow.toUpperCase() === columnName.toUpperCase()){
			        		originalColumnName = $scope.ngModel.content.columnSelectedOfDataset[i].alias;
							break;
			        	}
			        }

					if(originalColumnName==undefined || originalColumnName==""){
						for(var i=0; i<$scope.ngModel.content.columnSelectedOfDataset.length; i++){
							if($scope.ngModel.content.columnSelectedOfDataset[i].alias && $scope.ngModel.content.columnSelectedOfDataset[i].alias.toUpperCase() === columnName.toUpperCase()){
								originalColumnName = columnName;
								break;
							}
						}
					}
				}

				if ($scope.ngModel.content.crosstabDefinition){
					//check on pivot table structure: rows and columns definition
					if (originalColumnName == undefined || originalColumnName == ""){
						for(var i=0; i<$scope.ngModel.content.crosstabDefinition.columns.length; i++){
							if($scope.ngModel.content.crosstabDefinition.columns[i].alias && $scope.ngModel.content.crosstabDefinition.columns[i].alias.toUpperCase() === columnName.toUpperCase()){
									originalColumnName = $scope.ngModel.content.crosstabDefinition.columns[i].id;
								break;
							}
						}
					}
					if (originalColumnName == undefined || originalColumnName == ""){
						for(var i=0; i<$scope.ngModel.content.crosstabDefinition.rows.length; i++){
							if($scope.ngModel.content.crosstabDefinition.rows[i].alias && $scope.ngModel.content.crosstabDefinition.rows[i].alias.toUpperCase() === columnName.toUpperCase()){
								originalColumnName = $scope.ngModel.content.crosstabDefinition.rows[i].id;
								break;
							}
						}
					}
				}

			  //at last sets the input columnName like the original name
				if (originalColumnName == undefined || originalColumnName == ""){
					originalColumnName = columnName;
				}

			}else{
				//multiple selection: only from pivot table widget (by measure selection)
				originalColumnName = [];
				if ($scope.ngModel.content.crosstabDefinition){
					//check on pivot table structure: rows and columns definition
					for (var k=0; k < columnName.length; k++){
						var singleColumnName = columnName[k];
						var foundInColumns = false;
						var foundInRows = false;
						for(var i=0; i<$scope.ngModel.content.crosstabDefinition.columns.length; i++){
							if($scope.ngModel.content.crosstabDefinition.columns[i].alias && $scope.ngModel.content.crosstabDefinition.columns[i].alias.toUpperCase() === singleColumnName.toUpperCase()){
								originalColumnName.push($scope.ngModel.content.crosstabDefinition.columns[i].id);
								foundInColumns = true;
								break;
							}
						}
						if (!foundInColumns){
							for(var i=0; i<$scope.ngModel.content.crosstabDefinition.rows.length; i++){
								if($scope.ngModel.content.crosstabDefinition.rows[i].alias && $scope.ngModel.content.crosstabDefinition.rows[i].alias.toUpperCase() === singleColumnName.toUpperCase()){
									originalColumnName.push($scope.ngModel.content.crosstabDefinition.rows[i].id);
									foundInRows = true;
									break;
								}
							}
						}
					}

//					//at last sets the input columnName like the original name
					if (!foundInColumns && !foundInRows){
						originalColumnName.push(singleColumnName);
					}
				}
			}

			var sel = disableAssociativeLogic ? "noAssoc" : cockpitModule_widgetSelection.getAssociativeSelections(columnValue,columnName,dsLabel,originalColumnName);
			if(sel!=undefined){
				cockpitModule_widgetSelection.addTimestampedSelection(dsLabel, columnName, columnValue, $scope.ngModel.id);

				if(!cockpitModule_template.configuration.aliases){
					cockpitModule_template.configuration.aliases = [];
				}

				if(!angular.equals("noAssoc",sel)){
					sel.then(function(response) {
						if(!skipRefresh){
							cockpitModule_widgetSelection.refreshAllAssociatedWidget(false,response);
						}
					}, function(error) {
						console.log(error)
					});
				}else{
					if(!cockpitModule_template.configuration.filters.hasOwnProperty(dsLabel)){
						cockpitModule_template.configuration.filters[dsLabel]={};
					} else{
						if(Object.keys(cockpitModule_template.configuration.filters).length > 1){ // sort keys
							var temp = cockpitModule_template.configuration.filters[dsLabel];
							delete cockpitModule_template.configuration.filters[dsLabel];
							cockpitModule_template.configuration.filters[dsLabel] = temp;
						}
					}
					if (Array.isArray(originalColumnName)){
						for (var o=0; o < originalColumnName.length; o++){
							var singleOriginalColumnValue = originalColumnName[o];
							if(cockpitModule_template.configuration.filters[dsLabel].hasOwnProperty(singleOriginalColumnValue)){ // sort keys
								delete cockpitModule_template.configuration.filters[dsLabel][singleOriginalColumnValue];
							}
							cockpitModule_template.configuration.filters[dsLabel][singleOriginalColumnValue]=columnValue[o];
							cockpitModule_template.configuration.aliases.push({'dataset':dsLabel,'column':singleOriginalColumnValue,'alias':columnName[o]});
						}
					}else{
							if(cockpitModule_template.configuration.filters[dsLabel].hasOwnProperty(originalColumnName)){ // sort keys
								delete cockpitModule_template.configuration.filters[dsLabel][originalColumnName];
							}
							// 02/02/17 - davverna
							// if columnvalue is an array, usually from a bulk selection, I use a copy to avoid the direct object binding.
							// With the double click there is not the same issue because the binding is on a primitive value (string).
							if(Object.prototype.toString.call( columnValue ) === '[object Array]'){
								cockpitModule_template.configuration.filters[dsLabel][originalColumnName]=[];
								angular.copy(columnValue,cockpitModule_template.configuration.filters[dsLabel][originalColumnName]);
							}else{
								cockpitModule_template.configuration.filters[dsLabel][originalColumnName]=columnValue;
							}
							cockpitModule_template.configuration.aliases.push({'dataset':dsLabel,'column':originalColumnName,'alias':columnName});
					}
					cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=true;
					if(!skipRefresh){
						cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(dsLabel);
					}

				}
			}

		}
	}

	$scope.doEditWidget=function(initOnFinish){

		var deferred;
		if(initOnFinish){
			deferred=$q.defer();
		}
		$scope.editWidget().then(
				function(){
					if(typeof $scope.reinit !== "undefined"){
						$scope.reinit();
					}
					clearRedundantStyle();
					$scope.refreshWidgetStyle();
					if(initOnFinish){
						deferred.resolve();
					}
				},function(){
					if(initOnFinish){
					deferred.reject();
					}
				});

		if(initOnFinish){
			return deferred.promise;
		}
	}

	var clearRedundantStyle=function(){
		for(var prop in $scope.ngModel.style){
			if(cockpitModule_template.configuration.style[prop]!=undefined){

				if(angular.isObject($scope.ngModel.style[prop])){
					for(var subProp in $scope.ngModel.style[prop] ){
						if( angular.equals(cockpitModule_template.configuration.style[prop][subProp],$scope.ngModel.style[prop][subProp])){
							delete $scope.ngModel.style[prop][subProp];
						}
					}
					if(Object.keys($scope.ngModel.style[prop]).length==0){
						delete $scope.ngModel.style[prop];
					}
				}else{
					if(angular.equals(cockpitModule_template.configuration.style[prop],$scope.ngModel.style[prop])){
						delete $scope.ngModel.style[prop];
					}
				}
			}
		}

	}

	$scope.getAnalyticalDrivers=function(){
		return cockpitModule_analyticalDrivers;
	}

	$scope.refreshWidgetStyle=function(){

		// update extended style
		angular.copy(angular.merge({},cockpitModule_template.configuration.style,$scope.ngModel.style),$scope.extendedStyle);

		// update border style
		if($scope.extendedStyle.borders!=undefined){
			if($scope.extendedStyle.borders){
				angular.merge($scope.borderShadowStyle,$scope.extendedStyle.border);
			}else{
				delete $scope.borderShadowStyle['border-color'];
				delete $scope.borderShadowStyle['border-width'];
				delete $scope.borderShadowStyle['border-style'];
			}
		}

		// update shadow style
		if($scope.extendedStyle.shadows!=undefined){
			if($scope.extendedStyle.shadows){
				angular.merge($scope.borderShadowStyle,$scope.extendedStyle.shadow);
			}else{
				delete $scope.borderShadowStyle['box-shadow'];
			}
		}
//		// update title style
		if($scope.extendedStyle.titles!=undefined && $scope.extendedStyle.titles==true){
			if($scope.ngModel.content.name && $scope.extendedStyle.title){
				$scope.extendedStyle.title.label = $scope.extendedStyle.title.label ? $scope.extendedStyle.title.label : $scope.ngModel.content.name;
			}
			if($scope.extendedStyle.title && !$scope.extendedStyle.title.font){
				$scope.extendedStyle.title.font = {};
				$scope.ngModel.style.title.font = {};

				if($scope.extendedStyle.title['font-weight']){
					$scope.extendedStyle.title.font['font-weight'] = $scope.extendedStyle.title['font-weight'];
					$scope.ngModel.style.title.font['font-weight'] = $scope.extendedStyle.title['font-weight'];
					}
				if($scope.extendedStyle.title['font-size']){
					$scope.extendedStyle.title.font['font-size'] = $scope.extendedStyle.title['font-size'];
					$scope.ngModel.style.title.font['font-size'] = $scope.extendedStyle.title['font-size'];
					}
				if($scope.extendedStyle.title['font-family']){
					$scope.extendedStyle.title.font['font-family'] = $scope.extendedStyle.title['font-family'];
					$scope.ngModel.style.title.font['font-family'] = $scope.extendedStyle.title['font-family'];
					}
				if($scope.extendedStyle.title.color){
					$scope.extendedStyle.title.font.color = $scope.extendedStyle.title.color;
					$scope.ngModel.style.title.font.color = $scope.extendedStyle.title.color;
					}
			}
			if($scope.extendedStyle.title && $scope.extendedStyle.title.height){
				$scope.extendedStyle.title['min-height'] = $scope.extendedStyle.title.height;
			}
		}

		// update widgets background color

		var tempBackGround={'background-color': $scope.extendedStyle.backgroundColor || ''};
		angular.merge($scope.borderShadowStyle,tempBackGround);


		// update sheets background color
		if($scope.extendedStyle.sheetsBackgroundColor!=undefined && $scope.cockpitModule_template.style) {

			$scope.sheetsBackgroundColor=$scope.extendedStyle.sheetsBackgroundColor;
			$scope.cockpitModule_template.style.background=$scope.extendedStyle.sheetsBackgroundColor;

		}

		// update header height
		if($scope.extendedStyle.headerHeight!=undefined){
			$scope.headerHeight=$scope.extendedStyle.headerHeight;
		}

	}

	$scope.getDataset = function(){
		if($scope.ngModel.dataset!=undefined && $scope.ngModel.dataset.dsId != undefined){
			 return cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
		} else{
			return undefined;
		}
	}

	$scope.getDataset = function(label){
		if($scope.ngModel.dataset!=undefined && $scope.ngModel.dataset.dsId != undefined){
			if (!Array.isArray($scope.ngModel.dataset.dsId)){
				 return cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId);
			}
			for (ds in $scope.ngModel.dataset.dsId){
				var tmpDS = cockpitModule_datasetServices.getDatasetById($scope.ngModel.dataset.dsId[ds]);
				if (tmpDS.label == label)
					return tmpDS;
			}
		} else{
			return undefined;
		}
	}

	$scope.getDocument = function(){
		if($scope.ngModel.document!=undefined && $scope.ngModel.document.docId != undefined){
			return cockpitModule_documentServices.getDocumentById( $scope.ngModel.document.docId);
		} else{
			return undefined;
		}
	}


	$scope.expandWidget=function(){

		if(angular.element($scope.cockpitWidgetItem[0].firstElementChild).hasClass("fullScreenWidget")){
			cockpitModule_widgetServices.setFullPageWidget(false);
			cockpitModule_properties.WIDGET_EXPANDED[cockpitModule_properties.CURRENT_SHEET]=false;
			$scope.cockpitWidgetItem.css("width","");
			$scope.cockpitWidgetItem.css("height","");
			angular.element(document.querySelector("cockpit-sheet>md-tabs>md-tabs-content-wrapper>md-tab-content")).css("overflow","auto");
			$scope.cockpitWidgetItem.css("position","relative");
			$scope.cockpitWidgetItem.css("z-index","");
			angular.element($scope.cockpitWidgetItem[0].firstElementChild).removeClass("fullScreenWidget");
			cockpitModule_widgetServices.fullScreenWidget = false;
			$scope.widExp = false;
		}else{
			cockpitModule_properties.WIDGET_EXPANDED[cockpitModule_properties.CURRENT_SHEET]=true;
			cockpitModule_widgetServices.setFullPageWidget(true);
			$scope.cockpitWidgetItem.css("width",document.querySelector("cockpit-sheet>md-tabs>md-tabs-content-wrapper").offsetWidth-20);
			$scope.cockpitWidgetItem.css("height",document.querySelector("cockpit-sheet>md-tabs>md-tabs-content-wrapper").offsetHeight-10);
			angular.element(document.querySelector("cockpit-sheet>md-tabs>md-tabs-content-wrapper>md-tab-content")).css("overflow","hidden");
			document.querySelector("cockpit-sheet>md-tabs>md-tabs-content-wrapper>md-tab-content").scrollTop=0
			$scope.cockpitWidgetItem.css("position","absolute");
			$scope.cockpitWidgetItem.css("z-index","9999");
			angular.element($scope.cockpitWidgetItem[0].firstElementChild).addClass("fullScreenWidget");
			cockpitModule_widgetServices.fullScreenWidget = true;
			$scope.widExp = true;
		}
		cockpitModule_gridsterOptions.draggable.enabled=cockpitModule_properties.EDIT_MODE && cockpitModule_properties.WIDGET_EXPANDED[cockpitModule_properties.CURRENT_SHEET]!=true;
		cockpitModule_gridsterOptions.resizable.enabled=cockpitModule_properties.EDIT_MODE && cockpitModule_properties.WIDGET_EXPANDED[cockpitModule_properties.CURRENT_SHEET]!=true;

		$scope.refreshWidget(undefined,'fullExpand');
	};

	$scope.getProperties = function(propertyPath) {
		propertyPath = propertyPath.replace(/\[(\w+)\]/g, '.$1'); // convert indexes to properties
		propertyPath = propertyPath.replace(/^\./, '');           // strip a leading dot
	    return propertyPath.split('.');
	}

	$scope.getPropertyValue = function(obj, propertyPath) {
	    var properties = $scope.getProperties(propertyPath);
	    for (var i = 0; i < properties.length; ++i) {
	        var property = properties[i];
	        if (property in obj) {
	        	obj = obj[property];
	        }else{
	        	return;
	        }
	    }
	    return obj;
	}

	$scope.setPropertyValue = function(obj, propertyPath, value) {
		var properties = $scope.getProperties(propertyPath);
		var lastIndex = properties.length - 1;
		var lastProperty = properties[lastIndex];
	    for (var i = 0; i < properties.length - 1; ++i) {
	        var property = properties[i];
	        if (property in obj == false) {
	        	obj[property] = {};
	        }
	        obj = obj[property];
	    }
	    obj[lastProperty] = value;
	}

	$scope.deleteProperty = function(obj, propertyPath) {
		var properties = $scope.getProperties(propertyPath);
		var lastIndex = properties.length - 1;
		var lastProperty = properties[lastIndex];
		if(lastIndex > 0){
			properties.splice(lastIndex,1);
			obj = $scope.getPropertyValue(obj, properties.join("."));
		}
		if(obj){
			delete obj[lastProperty];
			return true;
		}
		return false;
	}

	$scope.moveProperty = function(obj, srcPath, dstPath){
		var value = $scope.getPropertyValue(obj, srcPath);
		if(value != undefined){
			$scope.deleteProperty(obj, srcPath);
			$scope.setPropertyValue(obj, dstPath, value);
			return true;
		}
		return false;
	}

	$scope.mutualExclusionToggle = function() {
		$scope.ngModel.mutualExclusion = !$scope.ngModel.mutualExclusion;
		console.log($scope.ngModel.mutualExclusion);
		if($scope.ngModel.mutualExclusion !== undefined) {
			var l = $scope.getLayerByName($scope.ngModel.content.layers[0].name);
			if (!l) return; //do nothing
			l.setVisible(true);
			if( $scope.ngModel.mutualExclusion === true) {
				//By default select only the first layer
				for(var i = 1; i < $scope.ngModel.content.layers.length ; i++) {
					var l = $scope.getLayerByName($scope.ngModel.content.layers[i].name);
					if (!l) return; //do nothing
					l.setVisible(false);
				}
			} else {
				//By  default select all layers
				for(var i = 1; i < $scope.ngModel.content.layers.length ; i++) {
					var l = $scope.getLayerByName($scope.ngModel.content.layers[i].name);
					if (!l) return; //do nothing
					l.setVisible(true);
				}
			}
		}
	}

	$scope.modalQuickWidget= function(ev) {
		if($scope.ngModel.type == 'chart'){
			$scope.addTableFromChart("table");
			return;
		} else {
			$mdDialog.show({
				controller: function ($scope,$mdDialog,ngModel,cockpitModule_datasetServices) {

					$scope.target = {"dataset":ngModel.dataset.dsId};

					$scope.availableDatasetToSwitch = cockpitModule_datasetServices.getAvaiableDatasets();

					$scope.selectDataset = function(){
						$scope.selectedDataset = {};
						$scope.modalFields = {"measures":[],"attributes":[]};
						for(var i in $scope.availableDatasetToSwitch){
							if($scope.availableDatasetToSwitch[i].id.dsId === $scope.target.dataset){
								$scope.selectedDataset = $scope.availableDatasetToSwitch[i];
								for(var k in $scope.availableDatasetToSwitch[i].metadata.fieldsMeta){
									if($scope.availableDatasetToSwitch[i].metadata.fieldsMeta[k].fieldType === 'ATTRIBUTE'){
										$scope.modalFields.attributes.push($scope.availableDatasetToSwitch[i].metadata.fieldsMeta[k]);
									}
									if($scope.availableDatasetToSwitch[i].metadata.fieldsMeta[k].fieldType === 'MEASURE'){
										$scope.modalFields.measures.push($scope.availableDatasetToSwitch[i].metadata.fieldsMeta[k]);
									}
								}
							}
						}
					}
					$scope.selectDataset();

					$scope.cancel = function(){
						$mdDialog.cancel();
					}

					$scope.add = function(){
						if($scope.ngModel.type == 'table'){
							$scope.addChartFromTable("chart");
						} else if($scope.target.visualization == "table"){
							$scope.addTableFromMap("table")
						} else {
							$scope.addChartFromMap("chart");
						}
						$mdDialog.hide();
					}

					$scope.selectVisualization = function(vis){
						$scope.target.visualization = vis;
					}

				},
				scope: $scope,
		      templateUrl: currentScriptPath+'/templates/changeWidgetTypeDialog.tpl.html',
		      targetEvent: ev,
		      preserveScope: true,
		      clickOutsideToClose:true,
		      locals: {ngModel:$scope.ngModel}
		    })
		}
	}

	$scope.chartTypes = [];
	$scope.showChartTypes = function(ev,widgetName){
		if(!$scope.ngModel.content.chartTemplateOriginal){
			$scope.ngModel.content.chartTemplateOriginal = angular.copy($scope.ngModel.content.chartTemplate);

		}else{
			$scope.ngModel.content.chartTemplate = angular.copy($scope.ngModel.content.chartTemplateOriginal);
		}
		$scope.chartTypes.length = 0;
		var serie = $scope.ngModel.content.chartTemplate.CHART.VALUES.SERIE;
		var numOfCateg = cockpitModule_widgetServices.checkNumOfCategory($scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY);
		var minMaxCategoriesSeries = cockpitModule_widgetServices.createCompatibleCharts();
		for (var attrname in minMaxCategoriesSeries.serie.min) {
			if((minMaxCategoriesSeries.serie.min[attrname] <= serie.length) && (minMaxCategoriesSeries.categ.min[attrname] <= numOfCateg) ){
				$scope.chartTypes.push(attrname)
			}
		}

		$mdDialog.show({
			controller: function ($scope,$mdDialog,ngModel) {
				$scope.widgetName = widgetName;

				$scope.changeChartType = function(type){
					var chartType = $scope.ngModel.content.chartTemplate.CHART.type.toLowerCase();
					var categories = cockpitModule_widgetServices.checkCategories($scope.ngModel.content.chartTemplate);
					delete $scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY;
					var maxcateg = minMaxCategoriesSeries.categ.max[type] ? minMaxCategoriesSeries.categ.max[type] : undefined;
					$scope.ngModel.content.chartTemplate.CHART.VALUES.CATEGORY = cockpitModule_widgetServices.compatibleCategories(type, categories, maxcateg);
					if(minMaxCategoriesSeries.serie.max[type]) $scope.ngModel.content.chartTemplate.CHART.VALUES.SERIE.length = minMaxCategoriesSeries.serie.max[type];
					$scope.ngModel.content.chartTemplate.CHART.type = type.toUpperCase();
					$scope.$broadcast("changeChart",{ "type": type});
					$mdDialog.hide();
				}
				$scope.cancel = function(){
					$mdDialog.cancel();
				}
			},
			scope: $scope,
			preserveScope:true,
	      templateUrl: currentScriptPath+'/templates/chartTypes.tpl.html',
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {ngModel:$scope.ngModel}
	    })
	}
	
	$scope.captureScreenShot = function(ev,model){
		model.loadingScreen = true;
		var element = document.querySelector('#w'+model.id+' md-card-content');
		html2canvas(element,{
			imageTimeout: 0,
			width: element.scrollWidth,
		    height: element.scrollHeight
		    }
		).then(function(canvas) {
		    canvas.toBlob(function(blob) {
		        saveAs(blob, (model.content.name || 'screenshot' )+'.png');
		    });
		    delete model.loadingScreen;
		});
	};
	
};

})();