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

/**
 * Dependencies for the Workspace main controller:
 * 		- document_viewer: Directive that provides possibility to execute a document in separate
 * 		iframe (window) that has a button for closing the executed document. When user do that,
 * 		the iframe closes and we are having the initial page (the one from which we wished to
 * 		execute the document).
 */
angular
	.module('qbe.controller', ['configuration','directive','services'])
	.controller('qbeController', 
		["$scope","entity_service","sbiModule_inputParams",qbeFunction]);



function qbeFunction($scope,entity_service,sbiModule_inputParams) {
	
	var entityService = entity_service;
	var inputParamService = sbiModule_inputParams;
	
	entityService.getEntitiyTree(inputParamService.modelName).then(function(response){
		 $scope.model = response.data;
	});
	
	$scope.onDropComplete=function(data,evt){
	   var queryObject = {
	    	"id":data.id,
	    	"name":data.text,
	    	"data":"no data",
	    	"order":"",
	    	"filters": ["less than 6"]
	    }

		$scope.queryModel.push(queryObject);      
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
    $scope.openMenu = function($mdMenu, ev) {
        originatorEv = ev;
        $mdMenu.open(ev);
      };

	
}