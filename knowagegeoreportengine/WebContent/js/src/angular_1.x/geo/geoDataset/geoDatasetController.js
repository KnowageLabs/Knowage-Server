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

function geoDatasetControllerFunction($scope,geoModule_template,sbiModule_restServices,sbiModule_config,$mdDialog,sbiModule_translate, geoSharedSettings,geoModule_dataset){
	$scope.translate= sbiModule_translate;
	$scope.datasetRows=[];
	$scope.datasetColumns=[];
	
	$scope.showDataset=function(){
		
		$scope.getDatasetColumns(geoModule_dataset.metaData.fields);
		$scope.datasetRows=geoModule_dataset.rows;
		
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
	
	 $scope.loadDatasetRows= function(){
		 var datasetLabel= sbiModule_config.docDatasetLabel;
		 var geoSettings= geoSharedSettings.getSettings();
			params={};
	    	params.start =0;
	    	params.limit = geoSettings.maxPreviewDatasetNumber;
	    	params.page = 0;
	    	params.dataSetParameters=null;
	    	params.sort=null;
	    	params.valueFilter=null;
	    	params.columnsFilter=null;
	    	params.columnsFilterDescription=null;
	    	params.typeValueFilter=null;
	    	params.typeFilter=null;
	    	    	
	    	config={};
	    	config.params=params;
		 sbiModule_restServices
			.alterContextPath(sbiModule_config.externalBasePath);
      	sbiModule_restServices.promiseGet("restful-services/selfservicedataset/values",datasetLabel,"", config)
			.then(
					function(response) {
						console.log(response.data);
						$scope.getDatasetColumns(response.data.metaData.fields);
						$scope.datasetRows=response.data.rows;
						
						$mdDialog.show({
							  scope:$scope,
							  preserveScope: true,
						      controller: DatasetPreviewController,
						      templateUrl: sbiModule_config.contextName+'/js/src/angular_1.x/geo/geoDataset/templates/datasetPreviewTemplate.html',  
						      clickOutsideToClose:false,
						      escapeToClose :false,
						    });
						
					},
					function(response) {
						sbiModule_messaging.showErrorMessage(response.data,SbiModule_translate.load('gisengine.dataset.preview.error'));
						
					});
	 }
	   
	 
	
	function DatasetPreviewController ($scope){
		
		$scope.closeDatasetPreviewDialog=function(){
			$scope.datasetRows=[];
			$scope.datasetColumns=[];
			$mdDialog.cancel();
		}
	}
}
