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
 * @authors Antonella Giachino (antonella.giachino@eng.it)
 * v0.0.1
 * 
 */
(function() {

angular.module('cockpitModule')
.directive('cockpitTextConfiguration',function(){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-text-configuration/templates/cockpitTextConfiguration.html',
		   replace: false,
		   scope: {
			   ngModelShared: '=',
			   editorText: '='
				   
		   	},
		   controller: cockpitTextControllerFunction		   
	   }
});

function cockpitTextControllerFunction($scope,cockpitModule_widgetServices,
		cockpitModule_properties,cockpitModule_template,$mdDialog,sbiModule_translate,sbiModule_restServices,
		cockpitModule_gridsterOptions,$mdPanel,cockpitModule_widgetConfigurator,$mdToast,
		cockpitModule_generalServices,cockpitModule_widgetSelection,cockpitModule_datasetServices,
		cockpitModule_analyticalDriversUrls,$rootScope){

	$scope.cockpitModule_properties=cockpitModule_properties;
	$scope.cockpitModule_template=cockpitModule_template;
	$scope.cockpitModule_widgetServices=cockpitModule_widgetServices;
	$scope.cockpitModule_datasetServices=cockpitModule_datasetServices;
	$scope.translate=sbiModule_translate;
	
	//set flags
	$scope.ngModelShared.selectedAggregation = false;
	$scope.ngModelShared.viewParametersDett = false;
	$scope.ngModelShared.viewParameters = false;
	$scope.ngModelShared.viewDatasets = false;
	
	

	//set parameters references to ngModel
	$scope.ngModelShared.parameters=[];
	$scope.ngModelShared.functions=[];
	$scope.ngModelShared.parameters = cockpitModule_analyticalDriversUrls;
	$scope.ngModelShared.viewParameters = Object.keys($scope.ngModelShared.parameters).length>0;
	
	//set datasets references to ngModel
	$scope.datasetColumns=[];
	$scope.ngModelShared.datasets = {};
	if ($scope.cockpitModule_template.configuration && $scope.cockpitModule_template.configuration.datasets){
		for(var ds in $scope.cockpitModule_template.configuration.datasets){
			var tmpDs = cockpitModule_datasetServices.getDatasetById($scope.cockpitModule_template.configuration.datasets[ds].dsId);
			$scope.ngModelShared.datasets[tmpDs.label] = tmpDs.metadata.fieldsMeta;
			$scope.ngModelShared.viewDatasetsDett = {};
			$scope.ngModelShared.viewDatasetsDett[tmpDs.label] = false;			
			$scope.ngModelShared.viewDatasets = true;
			$scope.ngModelShared.functions=['SUM', 'AVG', 'MIN', 'MAX','COUNT'];
		}
	}
	
	$scope.addToText = function (type, param, key, func){	
		if (type=='parameter'){
			param="<span class='paramPlaceholder'> $P{" + param + "}</span>";
		}else if (type=='dataset') {			
			var ph = "$F{" + key+"."+param + "}";
			if($scope.ngModelShared.selectedAggregation){
				//add functions only on MEASURE fields
				var fieldType = $scope.getFieldType(key, param);
				if (fieldType != 'MEASURE') return; 
				ph = $scope.ngModelShared.selectedAggregation + "(" + ph + ")";
				$scope.ngModelShared.selectedAggregation = false; // reset function selection
			}
			param="<span  class='paramPlaceholder'>" + ph + "</span>";
		}
		if (!$scope.editorText) $scope.editorText="";
		$scope.editorText += param;
	}
	
	$scope.getFieldType = function (key, field){
		var metaFields = $scope.ngModelShared.datasets[key];
		for (f in metaFields){
			if (metaFields[f].alias == field){
				return metaFields[f].fieldType;
			}
		}
		return null;
	}
	
	$scope.selectAggregation = function(aggr){
		$scope.ngModelShared.selectedAggregation = (!$scope.ngModelShared.selectedAggregation)?aggr:false;
	} 
	
	$scope.showHideParameters = function(){
		$scope.ngModelShared.viewParametersDett = (!$scope.ngModelShared.viewParametersDett);
	}
	
	$scope.showHideDatasetFields = function(key){
		$scope.ngModelShared.viewDatasetsDett[key] = (!$scope.ngModelShared.viewDatasetsDett[key]);
	}

};




})();