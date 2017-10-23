/*
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
 * @authors Ana Tomic (ana.tomic@mht.net)
 *
 */

angular.module('geoModule')
.directive('geoDataset',function(sbiModule_config){
	return{
		restrict: "E",
		templateUrl: sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoDataset/templates/geoDatasetTemplate.jsp',
		controller: geoDatasetControllerFunction,
		require: "^geoMap",
		scope: {
			id:"@"
		},
		disableParentScroll:true,
	}
})

function geoDatasetControllerFunction($scope,geoModule_template,sbiModule_restServices,sbiModule_config,$mdDialog,sbiModule_translate, geoSharedSettings,geoModule_dataset,geoModule_ranges){
	$scope.translate= sbiModule_translate;
	$scope.datasetRows=[];
	$scope.datasetColumns=[];
	if(!geoModule_template.accessibilityConf){
		geoModule_template.accessibilityConf = {"ranges" : ['very low','low','regular','high','very high']};
	}
	
	$scope.showDataset=function(){
		$scope.rangesNames = geoModule_template.accessibilityConf.ranges;		
		$scope.getDatasetColumns(geoModule_dataset.metaData.fields);
		$scope.geomoduleData=[];
		angular.copy(geoModule_dataset,$scope.geomoduleData);
		$scope.datasetRows=geoModule_ranges.getRangeableColumns($scope.geomoduleData,$scope.rangesNames);
		
		$mdDialog.show({
			  scope:$scope,
			  preserveScope: true,
		      controller: DatasetPreviewController,
		      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoDataset/templates/datasetPreviewTemplate.html',  
		      clickOutsideToClose:false,
		      escapeToClose :false,
		    });
     }
	
	 $scope.getDatasetColumns=function(fields){
	        
    	for(i=1;i<fields.length;i++){
    	 var column={};
    	 column.label=fields[i].header;
    	 column.name=fields[i].name;
    	 
    	 $scope.datasetColumns.push(column);
    	}
    	
    }
	
	function DatasetPreviewController ($scope){
		
		$scope.closeDatasetPreviewDialog=function(){
			$scope.datasetColumns=[];
			$mdDialog.cancel();
		}
	}
}
