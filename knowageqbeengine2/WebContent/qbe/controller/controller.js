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
		["$scope","entity_service","sbiModule_inputParams","sbiModule_config", "sbiModule_restServices", "sbiModule_messaging", qbeFunction]);



function qbeFunction($scope,entity_service,sbiModule_inputParams,sbiModule_config,sbiModule_restServices,sbiModule_messaging ) {
	
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	
	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
		 $scope.model = response.data;
	});
	
	$scope.onDropComplete=function(field,evt){
		$scope.addField(field);
		$scope.previewData(field); 
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
    
    $scope.queryModel = [];
    

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
    
    $scope.executeQuery = function (data) {
    	q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&start=0&limit=25&id=q1&promptableFilters=null"
    	
    	 sbiModule_restServices.promisePost('qbequery/executeQuery'+q,"")
     	.then(function(response) {
     		console.log("[POST]: SUCCESS!");
     		
     		var queryObject = {
     		    	"id":data.id,
     		    	"name":data.text,
     		    	"data":[],
     		    	"order":"",
     		    	"filters": ["less than 6"]
     		    }
     		
     		for (var i = 0; i < response.data.rows.length; i++) {
     			queryObject.data.push(response.data.rows[i].column_1)
			}
     		$scope.queryModel.push(queryObject); 
     	}, function(response) {
     	});
    }
    
    
    $scope.previewData = function (data) {
        q="?SBI_EXECUTION_ID="+sbiModule_config.sbiExecutionID+"&ambiguousRoles=null&ambiguousFieldsPaths=null&currentQueryId="+$scope.query.id;
        
        sbiModule_restServices.promisePost('qbequery/setQueryCatalog'+q,"", [$scope.query])
    	.then(function(response) {
    		console.log("[POST]: SUCCESS!");
    		$scope.executeQuery(data);
    	}, function(response) {
    	});
    }
    
    $scope.query = {"id":"q1","name":"query-q1","description":"query-q1","fields":[],"distinct":false,"filters":[],"calendar":{},"expression":{},"isNestedExpression":false,"havings":[],"graph":[],"relationsRoles":[],"subqueries":[]};
    
    $scope.openMenu = function($mdMenu, ev) {
        originatorEv = ev;
        $mdMenu.open(ev);
    };

	
}