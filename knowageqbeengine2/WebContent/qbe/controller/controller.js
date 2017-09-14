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

angular
	.module('qbe.controller', ['configuration','directive','services'])
	.controller('qbeController',
		["$scope","$rootScope","entity_service","query_service","filters_service","save_service","sbiModule_inputParams","sbiModule_config", "sbiModule_restServices", "sbiModule_messaging","$mdDialog", "$mdPanel","$q",qbeFunction]);



function qbeFunction($scope,$rootScope,entity_service,query_service,filters_service,save_service,sbiModule_inputParams,sbiModule_config,sbiModule_restServices,sbiModule_messaging, $mdDialog ,$mdPanel,$q){

	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	$scope.queryModel = [];
	$scope.pars = [];
	$scope.meta = [];
	$scope.editQueryObj = new Query("");
	$scope.advancedFilters = [];
	$scope.entityModel;
	$scope.subqueriesModel = {};

	$scope.$watch('editQueryObj',function(newValue,oldValue){
		$scope.meta.length = 0;
		for (var i = 0; i < newValue.fields.length; i++) {
			var meta = {
					"displayedName":newValue.fields[i].field,
					"name":newValue.fields[i].field,
					"fieldType":newValue.fields[i].fieldType,
					"type":""

			}
			$scope.meta.push(meta);
		}
		$scope.filters = $scope.editQueryObj.filters;
		if(query_service.smartView){
			$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, false);
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
		}
	},true)

	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
		 $scope.entityModel = response.data;

	});

	$scope.executeQuery = function ( query, bodySend, queryModel, isCompleteResult, start, itemsPerPage) {
		if(query.fields.length>0){
			query_service.executeQuery( query, bodySend, queryModel, isCompleteResult, start, itemsPerPage);
		}else{
			queryModel.length = 0;
		}		
	}

	$scope.openPanelForSavingQbeDataset = function (){
		var bodySend = angular.copy($scope.bodySend);
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/saveTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model ={ bodySend:bodySend,"mdPanelRef":mdPanelRef};
				},
				locals: {bodySend: bodySend},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}

	window.qbe ={};
	window.qbe.openPanelForSavingQbeDataset = $scope.openPanelForSavingQbeDataset;

	$scope.addToQueryModelWithoutExecutingQuery = function (query, queryModel) {
		if(query.fields.length>0){
			queryModel.length = 0;
     		for (var i = 0; i < query.fields.length; i++) {
     			var key = "column_"+(i+1);
     			var queryObject = {
         		    	"id":query.fields[i].id,
         		    	"name":query.fields[i].field,
         		    	"entity":query.fields[i].entity,
         		    	"color":query.fields[i].color,
         		    	"data":[],
         		    	"funct":query.fields[i].funct,
         		    	"visible":query.fields[i].visible,
         		    	"distinct":$scope.editQueryObj.distinct,
         		    	"group":query.fields[i].group,
         		    	"order":i+1,
         		    	"filters": []
         		    }
     				queryModel.push(queryObject);
			}
		}else{
			queryModel.length = 0;
		}
	}

	$scope.onDropComplete=function(field,evt){
		if(field.connector) return;
		$scope.addField(field);

    };

	$rootScope.$on('applyFunction', function (event, data) {
		var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
		var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
		var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.fieldId);
		if(data.funct!= undefined && data.funct !=null && data.funct!="") {
			$scope.query.fields[indexOfFieldInQuery].funct = data.funct.toUpperCase();
		}
		if(data.filters!= undefined && data.filters != null ) {
			$scope.query.filters = data.filters;
			$scope.query.expression = data.expression;
		}
		if(data.pars!= undefined && data.pars != null ) {
			$scope.bodySend.pars = data.pars;
		}
		$scope.query.fields[indexOfFieldInQuery].group = false;
		$scope.executeQuery($scope.entityModel.entities[indexOfEntity].children[indexOfFieldInEntity], $scope.query, $scope.bodySend, $scope.queryModel);
	});

	$rootScope.$on('applyFunctionForParams', function (event, data) {
		if(data.pars!= undefined && data.pars!= null ) {
			$scope.pars = data.pars;

		}
	});

	$rootScope.$on('smartView', function (event, data) {
		
		if(data.length>0 && query_service.smartView){
			for (var i = 0; i < data.length; i++) {
				$scope.editQueryObj.fields[i].group = data[i].group;
				$scope.editQueryObj.fields[i].funct = data[i].funct;
				$scope.editQueryObj.fields[i].visible = data[i].visible;
				$scope.editQueryObj.fields[i].distinct = data[i].distinct;
			}
		}
		if(query_service.smartView){
			$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel);
		} else {
			$scope.addToQueryModelWithoutExecutingQuery($scope.editQueryObj, $scope.queryModel);
		}
	});

	$scope.$on('executeQuery', function (event, data) {
		if(data.fields != undefined  && !query_service.smartView && data.fields.length>0){
			for (var i = 0; i < data.fields.length; i++) {
				$scope.editQueryObj.fields[i].group = data.fields[i].group;
				$scope.editQueryObj.fields[i].funct = data.fields[i].funct;
				$scope.editQueryObj.fields[i].visible = data.fields[i].visible;
				$scope.editQueryObj.fields[i].distinct = data.fields[i].distinct;
			}	
		}
		$scope.executeQuery($scope.editQueryObj, $scope.bodySend, $scope.queryModel, true, data.start, data.itemsPerPage);
	});
	
	$scope.$on('setVisible', function (event, data) {
		 var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
		  var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
		  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.fieldId);
		  $scope.isNotChangedFromHidingField = false;
		  $scope.editQueryObj.fields[indexOfFieldInQuery].visible = data.visible;
	});
	
	$scope.$on('showHiddenColumns', function (event, data) {
		 for (var i = 0; i < $scope.editQueryObj.fields.length; i++) {
			  $scope.editQueryObj.fields[i].visible = true;
		}
	});

	$rootScope.$on('removeColumn', function (event, data) {
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.id);
	  var indexOfFieldInModel = findWithAttr($scope.queryModel,'id', data.id);
	  if (indexOfFieldInQuery > -1 && indexOfFieldInModel > -1) {
		  $scope.editQueryObj.fields.splice(indexOfFieldInQuery, 1);
		  $scope.queryModel.splice(indexOfFieldInModel, 1);
		}
	});

	$rootScope.$on('group', function (event, data) {

	  var indexOfEntity = findWithAttr($scope.entityModel.entities,'qtip', data.entity);
	  var indexOfFieldInEntity = findWithAttr($scope.entityModel.entities[indexOfEntity].children,'id', data.fieldId);
	  var indexOfFieldInQuery = findWithAttr($scope.editQueryObj.fields,'id', data.fieldId);
	  $scope.editQueryObj.fields[indexOfFieldInQuery].group = data.group;
	  $scope.editQueryObj.fields[indexOfFieldInQuery].funct = "";
	  if(query_service.smartView){
		  $scope.executeQuery( $scope.editQueryObj, $scope.bodySend, $scope.queryModel, false);
	  }

	});

	var findWithAttr = function(array, attr, value) {
	    for(var i = 0; i < array.length; i += 1) {
	        if(array[i][attr] === value) {
	            return i;
	        }
	    }
	    return -1;
	}

	$scope.addField = function (field) {
		
		if($scope.queryModel != undefined  && !query_service.smartView && $scope.queryModel.length>0){
			for (var i = 0; i < $scope.queryModel.length; i++) {
				$scope.editQueryObj.fields[i].group = $scope.queryModel[i].group;
				$scope.editQueryObj.fields[i].funct = $scope.queryModel[i].funct;
				$scope.editQueryObj.fields[i].visible = $scope.queryModel[i].visible;
				$scope.editQueryObj.fields[i].distinct = $scope.queryModel[i].distinct;
			}	
		}
		
		var newField  = {
			   "id":field.id,
			   "alias":field.attributes.field,
			   "type":"datamartField",
			   "fieldType":field.attributes.iconCls,
			   "entity":field.attributes.entity,
			   "field":field.attributes.field,
			   "funct":"",
			   "color":field.color,
			   "group":false,
			   "order":"",
			   "include":true,
			   "visible":true,
			   "longDescription":field.attributes.longDescription,
			   "distinct":$scope.editQueryObj.distinct,
			}

		$scope.editQueryObj.fields.push(newField);
	}

	$scope.colors = ['#F44336', '#673AB7', '#03A9F4', '#4CAF50', '#FFEB3B', '#3F51B5', '#8BC34A', '#009688', '#F44336'];

    $scope.droppedFunction = function(data) {
        console.log(data)
    };

    $scope.entitiesFunctions = [{
        "label": "add calculated field",
        "icon": "fa fa-calculator",
        "action": function(item, event) {
            $scope.ammacool(item, event);
        }
    }];



    $scope.queryFunctions = [{
        "label": "start subquery",
        "icon": " fa fa-pencil-square-o",
        "action": function(item, event) {
        	$scope.editQueryObj = item;
        }
    },
    {
        "label": "remove subquery",
        "icon": "fa fa-trash",
        "action": function(item, $event) {
        	var index = $scope.subqueriesModel.subqueries.indexOf(item);
        	  $scope.subqueriesModel.subqueries.splice(index, 1);
        	  $scope.stopEditingSubqueries();
        }
    }

    ];



    $scope.fieldsFunctions = [{
        "label": "ranges",
        "icon": "fa fa-sliders",
        "action": function(item, event) {
            $scope.ammacool(item, event);
        }
    }, {
        "label": "filters",
        "icon": "fa fa-filter",
        "action": function(item, event) {
        	$scope.openFilters(item,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries, $scope.editQueryObj.expression, $scope.advancedFilters);
        }
    }];

    $scope.ammacool = function (item, event) {
    	console.log(item)
    }

    $scope.query = new Query(1);
    $scope.query.name = "Main query";

    $scope.catalogue = [$scope.query];

    $scope.editQueryObj = $scope.query;
    $scope.subqueriesModel.subqueries = $scope.query.subqueries;


    $scope.bodySend = {
    		"catalogue":$scope.catalogue,
    		"qbeJSONQuery":{},
        	"pars": $scope.pars,
        	"meta": $scope.meta,
        	"schedulingCronLine":"0 * * * * ?"
    };

    $scope.$on('openFilters',function(event,field){
		$scope.openFilters(field,$scope.entityModel,$scope.pars, $scope.editQueryObj.filters,$scope.editQueryObj.subqueries, $scope.editQueryObj.expression, $scope.advancedFilters);
	})
	$scope.$on('distinctSelected',function(){
    	 $scope.editQueryObj.distinct =  !$scope.editQueryObj.distinct;
    })

	$scope.$on('openDialogForParams',function(event){
		$scope.openDialogForParams($scope.pars);
	})

	$scope.openDialogForParams = function(pars){
    	var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/parameterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model ={ "pars": pars,"mdPanelRef":mdPanelRef};


				},
				locals: {pars: pars},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
    }

    $scope.$on('openFiltersAdvanced',function(event,field){
		$scope.showVisualization($scope.editQueryObj.filters, $scope.advancedFilters,$scope.editQueryObj.expression);
	})

    $scope.showVisualization = function (filters, advancedFilters, expression) {
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterVisualizationTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,mdPanelRef){
					$scope.model = {"filters":filters,"advancedFilters":advancedFilters,"expression":expression,"mdPanelRef":mdPanelRef};
				},
				locals: {filters:filters,advancedFilters:advancedFilters, expression:expression},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	}


	$scope.openFilters = function(field, tree, pars, queryFilters, subqueries, expression, advancedFilters) {
		if(field.hasOwnProperty('attributes')){
			field_copy = angular.copy(field);
			field={};
			field.field = {}
			field.field.id = field_copy.id;
			field.field.name = field_copy.text;
			field.field.entity = field_copy.attributes.entity;
			field.field.color = field_copy.color;
			field.field.visible= true;
			field.field.group= false;
			field.field.order= 1;
			field.field.filters= [];
		}
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,field,mdPanelRef){
					$scope.model ={ "field": field, "tree": tree, "pars": pars,"mdPanelRef":mdPanelRef, "queryFilters":queryFilters, "subqueries":subqueries, "expression":expression, "advancedFilters":advancedFilters};
				},
				locals: {field: field, tree: tree, pars: pars, queryFilters:queryFilters, subqueries: subqueries, expression : expression, advancedFilters : advancedFilters},
				hasBackdrop: true,
				clickOutsideToClose: true,
				escapeToClose: true,
				focusOnOpen: true,
				preserveScope: true,
		};
		$mdPanel.open(config);
		return finishEdit.promise;
	};

    $scope.openMenu = function($mdMenu, ev) {
        originatorEv = ev;
        $mdMenu.open(ev);
    };

    $scope.createQueryName = function(){
    	var lastcount = 0;
    	var lastIndex = $scope.subqueriesModel.subqueries.length-1;
    	if(lastIndex!=-1){
    		var lastQueryId = $scope.subqueriesModel.subqueries[lastIndex].id;
    		lastcount = parseInt(lastQueryId.substr(1));
    	}else{
    		lastcount = 1;
    	}

    	return lastcount +1;
    }
    $scope.createSubquery = function(){
    	var subquery = new Query($scope.createQueryName());
    	$scope.query.subqueries.push(subquery);
    	$scope.editQueryObj = subquery;
    }
    $scope.stopEditingSubqueries = function(){
    	$scope.editQueryObj = $scope.query;
    }
}