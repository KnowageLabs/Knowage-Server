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
angular
	.module('cockpitModule')
	.controller('mapWidgetEditControllerFunction',mapWidgetEditControllerFunction)

function mapWidgetEditControllerFunction($scope,finishEdit,model,sbiModule_translate,sbiModule_restServices,cockpitModule_datasetServices,$mdDialog,mdPanelRef,$location){
	$scope.translate=sbiModule_translate;
	$scope.newModel = angular.copy(model);
	
	//get templates location
  	$scope.basePath = $location.$$absUrl.substring(0,$location.$$absUrl.indexOf('api/'));
  	$scope.templatesUrl = 'js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/mapWidget/templates/';
  	$scope.getTemplateUrl = function(template){
  		return $scope.basePath + $scope.templatesUrl + template +'.html';
  	}
  	
  	$scope.expandRow = function(layer,content){
  		if(layer.expanded && layer.expanded==content){
  			delete layer.expanded;
  		}else{
  			layer.expanded = content;
  		}
  	}
  	
  	$scope.deleteLayer = function(layer){
  		$scope.newModel.content.targetLayersConf.splice($scope.newModel.content.targetLayersConf.indexOf(layer),1);
  	}
  	
  	$scope.moveOrder = function(layer, direction){
  		
  	}
  	
  	$scope.addLayer = function(ev) {
  		$scope.myLayersId = [];
  		for(var m in $scope.newModel.content.targetLayersConf){
			$scope.myLayersId.push($scope.newModel.content.targetLayersConf[m].datasetId);
		}
  		$mdDialog.show({
			controller: function ($scope,$mdDialog) {
				
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promiseGet("2.0/datasets", "","asPagedList=true&seeTechnical=TRUE&ids=&spatialOnly=true").then(
						function(result){
							$scope.availableSpatialLayers = [];
							for(var l in result.data.item){
								if($scope.myLayersId.indexOf(result.data.item[l].id.dsId)==-1){
									$scope.availableSpatialLayers.push(result.data.item[l]);
								}
							}
						},
						function(error){
							// TODO MANAGE ERROR
						})
				
			    //Add the layers to the newModel
				$scope.add = function(){
					for(var k in $scope.availableSpatialLayers){
						if($scope.availableSpatialLayers[k].selected){
							var tempLayer = $scope.availableSpatialLayers[k];
							var newLayer =  {
								"type": "DATASET",
								"datasetId": tempLayer.id.dsId,
								"label": tempLayer.label,
								"name": tempLayer.name,
								"order": $scope.newModel.content.targetLayersConf.length,
								"attributes": [],
								"indicators": []
							}
							for(var i in tempLayer.metadata.fieldsMeta){
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'ATTRIBUTE') newLayer.attributes.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias});
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'MEASURE') newLayer.indicators.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias});
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'SPATIAL_ATTRIBUTE') newLayer.attributes.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias,"isGeoReference":true});
							}
							$scope.newModel.content.targetLayersConf.push(newLayer);
							cockpitModule_datasetServices.addAvaiableDataset(tempLayer);
						}
					}
					$mdDialog.hide();
				}
				
				//Exit the dialog without adding
				$scope.cancel = function(){
					$mdDialog.cancel();
				}
			},
			scope: $scope,
			preserveScope:true,
	      templateUrl: $scope.getTemplateUrl('mapWidgetAddLayerDialog'),
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {  }
	    })
	    .then(function() {

	    });
  	}
	$scope.saveConfiguration=function(){
		 mdPanelRef.close();
		 angular.copy($scope.newModel,model);
		 finishEdit.resolve();
  	}

	$scope.cancelConfiguration=function(){
  		mdPanelRef.close();
  		finishEdit.reject();
  	}
}