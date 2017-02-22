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
angular.module('cockpitModule')
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
        			ele.bind('mouseover', mouseOver )
        			ele.bind('mouseleave', mouseLeave )
        			ele.addClass(scope.$eval(attrs.widgetXPosition)<=1 ? 'rightPosition': 'leftPosition')
        			$timeout(function(){
        				ele.removeClass('rightPosition');
						ele.removeClass('leftPosition');
        				ele.addClass(scope.$eval(attrs.widgetXPosition)<=1 ? 'rightPosition': 'leftPosition')
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
.directive('cockpitWidget',function(cockpitModule_widgetConfigurator,cockpitModule_widgetServices,$compile,cockpitModule_widgetSelection,$rootScope){
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
                    		scope.updateble=objType.updateble==undefined? true : objType.updateble;
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
                    			var initOnFinish = scope.ngModel.isNew == true ? true : false;
                    			scope.ngModel.isNew = undefined;
                    			if(initOnFinish){
                    				scope.doEditWidget(initOnFinish).then(function(){
                    					var options =scope.getOptions == undefined? {} :  scope.getOptions();
                    					cockpitModule_widgetServices.initWidget(angular.element(directive),scope.ngModel,options);
                    				},function(){
                    					scope.deleteWidget(true);
                    				})
                    			}else{
                    				var options =scope.getOptions == undefined? {} :  scope.getOptions();
                					cockpitModule_widgetServices.initWidget(angular.element(directive),scope.ngModel,options);
                    			}
                    				
                    			
                    				
                    		};
                    		scope.refreshWidget=function(options,nature){
                    			var finOptions=options==undefined? (scope.getOptions == undefined? {} :  scope.getOptions()) : options;
                    			cockpitModule_widgetServices.refreshWidget(angular.element(directive),scope.ngModel,nature==undefined? 'refresh' : nature, finOptions);
                    		};
                    	}                    	
                    	
                    }
                };
		   	}
	   }
});

function cockpitWidgetControllerFunction($scope,$rootScope,cockpitModule_widgetServices,cockpitModule_properties,cockpitModule_template,cockpitModule_analyticalDrivers,cockpitModule_datasetServices,sbiModule_restServices,$q,cockpitModule_documentServices,cockpitModule_crossServices,cockpitModule_widgetSelection,$timeout,cockpitModule_gridsterOptions,sbiModule_translate){
	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.translate		= sbiModule_translate;
	$scope.tmpWidgetContent	= {};
	$scope.editingWidgetName= false;
	$scope.extendedStyle	= {}; // the merge of the widget style and the

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
		if(!widExp){
			$scope.widgetActionButtonsVisible=false;
		}
	}
	
	// davverna - initializing search object to give all the columns to the user searchbar
	if(!$scope.ngModel.search || $scope.ngModel.search.columns == []){
		$scope.ngModel.search ={"columns" : []};
		for(var k in $scope.ngModel.content.columnSelectedOfDataset){
			var column = $scope.ngModel.content.columnSelectedOfDataset[k];
			if(column.fieldType == "ATTRIBUTE"){
				$scope.ngModel.search.columns.push(column.name);
			}
		}
	}
	
	// davverna - method to set the actual model and search parameters to refresh the widget table
	$scope.searchColumns = function(){
		if($scope.ngModel.search.text != ""){
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
		case "UPDATE_FROM_REALTIME":
			var ds=$scope.getDataset();
			if(ds!=undefined && config.dsList.indexOf(ds.label)!=-1){
				/*
				 * author: rselakov, Radmila Selakovic,
				 * radmila.selakovic@mht.net checking type of widget because of
				 * removing load spinner in case of updating charts
				 */
				$scope.refreshWidget();
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
				$timeout(function(){
					$scope.refresh(config.element,config.width,config.height, config.data,config.nature,config.associativeSelection);
				},1000);
			}else{
				$scope.refresh(config.element,config.width,config.height,config.data,config.nature,config.associativeSelection);
			}
			break;
		case "INIT" :
			$scope.scopeInit(config.element,config.width,config.height, config.data,config.nature,config.associativeSelection);
			break;
		case "RESIZE" :
			$scope.refreshWidget(undefined, 'resize');
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
// $scope.refreshWidget();
					}
				}
		}
		if(document != undefined && cockpitModule_widgetSelection.getCurrentSelections(document.DOCUMENT_LABEL)!=undefined){
			if(isInit){
				$scope.initWidget();
			}else{
				if(associativeSelection==undefined || associativeSelection.hasOwnProperty(document.DOCUMENT_LABEL)){
// $scope.refreshWidget();
					var option =$scope.getOptions == undefined? {} :  $scope.getOptions();
					cockpitModule_widgetServices.refreshWidget($scope.subCockpitWidget,$scope.ngModel,'selections',option);
					// to-do testare se funziona con la chiamata sotto
// $scope.refreshWidget(undefined,'selections');
				}
			}
		}
		
		
	}
	
	$scope.updateFromDatasetFilter=function(label){
		var dataset= $scope.getDataset();
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
			$scope.refreshWidget(undefined,'filters');
			
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
	
	$scope.openSearchBar = function(){
		$scope.widgetSearchBar == false ? $scope.widgetSearchBar = true : $scope.widgetSearchBar = false;
	}
	 
	$scope.doSelection = function(columnName,columnValue,modalColumn,modalValue,row){
		if($scope.ngModel.cliccable==false){
			console.log("widget is not cliccable")
			return;
		}
		
		// check if cross navigation was enable don this widget
		var model = $scope.ngModel;
		if(model.cross != undefined  && model.cross.cross != undefined 
				&& model.cross.cross.enable === true
				&& model.cross.cross.column != undefined
				&& model.cross.cross.outputParameter != undefined
				){
			
			// enter cross navigation mode
			var doCross = false;
			
			var nameToCheckForCross =  columnName;
			
			// check if selected column has been renamed by an alias, in tat
			// case take the real name
			for(var colIndex in model.content.columnSelectedOfDataset){
				var col = model.content.columnSelectedOfDataset[colIndex];
				if(col.aliasToShow != undefined && col.aliasToShow == columnName){
					nameToCheckForCross = col.name;
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
					

			}
			else{
				// case a specific column is enabled for cross
				// check if column clicked is the one for cross navigation
				if(model.cross.cross.column === nameToCheckForCross){
					doCross = true;
				}
			}

			if(doCross === true){
				var outputParameter = {};
				outputParameter[model.cross.cross.outputParameter] = columnValue;
				
				// if destination document is specified don't ask
				if(model.cross.cross.crossName != undefined){
					parent.execExternalCrossNavigation(outputParameter,{},model.cross.cross.crossName);
					return;
				}
				else{
					parent.execExternalCrossNavigation(outputParameter,{});
					return;
				}
			}
		}

		if(modalColumn!=undefined && modalValue!=undefined)
		{	
			columnValue=modalValue;
			columnName=modalColumn;
		}	
		// check if all associated data
		var dsLabel=$scope.getDataset().label;
		
		var originalColumnName;
        for(var i=0; i<$scope.ngModel.content.columnSelectedOfDataset.length; i++){
        	if($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow && $scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow.toUpperCase() === columnName.toUpperCase()){
        		originalColumnName = $scope.ngModel.content.columnSelectedOfDataset[i].alias;
				break;
        	}
        }
		if(originalColumnName==undefined){
			for(var i=0; i<$scope.ngModel.content.columnSelectedOfDataset.length; i++){
				if($scope.ngModel.content.columnSelectedOfDataset[i].alias && $scope.ngModel.content.columnSelectedOfDataset[i].alias.toUpperCase() === columnName.toUpperCase()){
					originalColumnName = columnName;
					break;
				}
			}
		}
		
		var sel=cockpitModule_widgetSelection.getAssociativeSelections(columnValue,columnName,dsLabel,originalColumnName);
		if(sel!=undefined){
			if(!cockpitModule_template.configuration.aliases){
				cockpitModule_template.configuration.aliases = [];
			}
			if(!angular.equals("noAssoc",sel)){
				sel.then(function(response) {
					cockpitModule_widgetSelection.refreshAllAssociatedWidget(false,response);
				}, function(error) {
					console.log(error)
				});
			}else{
				if(!cockpitModule_template.configuration.filters.hasOwnProperty(dsLabel)){
					cockpitModule_template.configuration.filters[dsLabel]={};
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
				cockpitModule_properties.HAVE_SELECTIONS_OR_FILTERS=true;
				cockpitModule_widgetSelection.refreshAllWidgetWhithSameDataset(dsLabel);
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
	
	$scope.editWidgetName=function(){
		if(cockpitModule_properties.EDIT_MODE){
			$scope.editingWidgetName=true;
			angular.copy($scope.ngModel.content,$scope.tmpWidgetContent);
		}
	}
	$scope.applyEditName=function(){
		$scope.editingWidgetName=false;
		$scope.ngModel.content.name=$scope.tmpWidgetContent.name;
		$scope.tmpWidgetContent={};
	}
	$scope.cancelEditName=function(){
		$scope.editingWidgetName=false;
		$scope.tmpWidgetContent={};
	}
	
	$scope.refreshWidgetStyle=function(){
		angular.copy({},$scope.extendedStyle);
		angular.copy({},$scope.borderShadowStyle);
		angular.copy({},$scope.titleStyle);
		//angular.copy({},$scope.headerHeight);
		//$scope.headerHeight={};
		
		// update extended style
		angular.copy(angular.merge({},cockpitModule_template.configuration.style,$scope.ngModel.style),$scope.extendedStyle);
		
		// update shadow style
		if($scope.extendedStyle.borders!=undefined && $scope.extendedStyle.borders==true){
			angular.merge($scope.borderShadowStyle,$scope.extendedStyle.border);
		}
		// update borders style
		if($scope.extendedStyle.shadows!=undefined && $scope.extendedStyle.shadows==true){
			angular.merge($scope.borderShadowStyle,$scope.extendedStyle.shadow);
		}
		// update title style
		if($scope.extendedStyle.titles!=undefined && $scope.extendedStyle.titles==true){
			angular.merge($scope.titleStyle,$scope.extendedStyle.title);
		}
		
		// update widgets background color
		if($scope.extendedStyle.backgroundColor!=undefined) {
			var tempBackGround={'background-color': $scope.extendedStyle.backgroundColor};
			angular.merge($scope.borderShadowStyle,tempBackGround);
		}
		
		
		// update sheets background color
		if($scope.extendedStyle.sheetsBackgroundColor!=undefined) {
				
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
};

})();