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
		["$scope","$rootScope","entity_service","query_service","filters_service","sbiModule_inputParams","sbiModule_config", "sbiModule_restServices", "sbiModule_messaging", "$mdPanel","$q",qbeFunction]);



function qbeFunction($scope,$rootScope,entity_service,query_service,filters_service,sbiModule_inputParams,sbiModule_config,sbiModule_restServices,sbiModule_messaging ,$mdPanel,$q){
	
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	$scope.queryModel = [];
	$scope.pars = [];
/*	$scope.expression = {  
	         "type":"NODE_CONST",
	         "value":"$F{Filter1}",
	         "color":"#F44336",
	         "condition": "age < 4",
	         "childNodes":[]
	}*/
	
	$scope.expression = {  
	         "type":"NODE_OP",
	         "value":"AND",
	         "childNodes":[  
	        	 {  
	                 "type":"NODE_CONST",
	                 "id": "1",
	                 "value":"$F{Filter1}",
	                 "condition": "age < 4",
	                 "selected":false,
	                 "grouped":false,
	                 "color":"#FFEB3B",
	                 "booleanConnector":"AND",
	                 "childNodes":[  

	                 ]
	              },
	              {  
	                 "type":"NODE_CONST",
	                 "id": "2",
	                 "value":"$F{Filter2}",
	                 "condition": "age > 1",
	                 "selected":false,
	                 "grouped":false,
	                 "color":"#8BC34A",
	                 "booleanConnector":"AND",
	                 "childNodes":[  

	                 ]
	              }
	         ]
	      };
	
	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
		 $scope.model = response.data;
	});
	
	$scope.executeQuery = function (field, query, bodySend, queryModel) {
		query_service.executeQuery(field, query, bodySend, queryModel).then(function(response){
			$scope.queryModel = response;
			
		});
	}
	
	$scope.onDropComplete=function(field,evt){
		$scope.addField(field);
		$scope.executeQuery(field, $scope.query, $scope.bodySend, $scope.queryModel); 
    };
	
	$rootScope.$on('applyFunction', function (event, data) {
	  var indexOfEntity = findWithAttr($scope.model.entities,'qtip', data.entity);
	  var indexOfFieldInEntity = findWithAttr($scope.model.entities[indexOfEntity].children,'id', data.fieldId);
	  var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.fieldId);
	  if(data.funct!= undefined && data.funct !=null && data.funct!="") {
		  $scope.query.fields[indexOfFieldInQuery].funct = data.funct.toUpperCase();
	  }
	  if(data.filters!= undefined && data.filters != null ) {
		  $scope.query.filters = data.filters;

		  $scope.query.expression = data.expression;	  }
	  $scope.query.fields[indexOfFieldInQuery].group = false;
	  $scope.executeQuery($scope.model.entities[indexOfEntity].children[indexOfFieldInEntity], $scope.query, $scope.bodySend, $scope.queryModel); 
	});
	
	$rootScope.$on('removeColumn', function (event, data) {
	  var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.id);
	  var indexOfFieldInModel = findWithAttr($scope.queryModel,'id', data.id);
	  if (indexOfFieldInQuery > -1 && indexOfFieldInModel > -1) {
		  $scope.query.fields.splice(indexOfFieldInQuery, 1);
		  $scope.queryModel.splice(indexOfFieldInModel, 1);
		}
	});
	
	$rootScope.$on('group', function (event, data) {
	  var indexOfEntity = findWithAttr($scope.model.entities,'qtip', data.entity);
	  var indexOfFieldInEntity = findWithAttr($scope.model.entities[indexOfEntity].children,'id', data.fieldId);
	  var indexOfFieldInQuery = findWithAttr($scope.query.fields,'id', data.fieldId);
	  console.log(data)
	  $scope.query.fields[indexOfFieldInQuery].group = data.group;
	  $scope.query.fields[indexOfFieldInQuery].funct = "";
	  $scope.executeQuery($scope.model.entities[indexOfEntity].children[indexOfFieldInEntity], $scope.query, $scope.bodySend, $scope.queryModel); 
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
		
		var newField  = {  
			   "id":field.id,
			   "alias":field.attributes.field,
			   "type":"datamartField",
			   "entity":field.attributes.entity,
			   "field":field.attributes.field,
			   "funct":"",
			   "group":false,
			   "order":"",
			   "include":true,
			   "visible":true,
			   "longDescription":field.attributes.longDescription
			}
		
		$scope.query.fields.push(newField);
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
            $scope.ammacool(item, event);
        }
    }];
    
    $scope.ammacool = function (item, event) {
    	console.log(item)
    }
    
    $scope.query = {"id":"q1","name":"query-q1","description":"query-q1","fields":[],"distinct":false,"filters":[],"calendar":{},"expression":{},"isNestedExpression":false,"havings":[],"graph":[],"relationsRoles":[],"subqueries":[]};

    $scope.catalogue = [$scope.query];

    $scope.bodySend = {
    		"catalogue":$scope.catalogue,
    		"qbeJSONQuery":{},
        	"pars": [],
        	"schedulingCronLine":"0 * * * * ?"
    };
    	
    $scope.$on('openFilters',function(event,field){
		$scope.openFilters(field,$scope.model);
	})
	
	$scope.openFilters = function(field, tree) {
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				templateUrl: sbiModule_config.contextName +'/qbe/templates/filterTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				controller: function($scope,field,mdPanelRef){
					$scope.model ={ "field": field, "tree": tree,"mdPanelRef":mdPanelRef};


				},
				locals: {field: field, tree: tree},
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

}