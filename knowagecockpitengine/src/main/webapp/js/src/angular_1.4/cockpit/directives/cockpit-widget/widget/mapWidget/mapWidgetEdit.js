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

function mapWidgetEditControllerFunction($scope,finishEdit,model,sbiModule_translate,sbiModule_restServices,cockpitModule_datasetServices,cockpitModule_generalServices,$mdDialog,mdPanelRef,$location, knModule_fontIconsService){
	$scope.translate=sbiModule_translate;
	$scope.newModel = angular.copy(model);
  	$scope.getTemplateUrl = function(template){
  		return cockpitModule_generalServices.getTemplateUrl('mapWidget',template);
  	}

  	$scope.setTargetLayer = function(layer){
  		for(var t in $scope.newModel.content.layers){
  			if($scope.newModel.content.layers[t].targetDefault && $scope.newModel.content.layers[t].datasetId != layer.datasetId){
  				$scope.newModel.content.layers[t].targetDefault = false;
  			}
  		}
  	}
  	
  	$scope.expandRow = function(layer,content){
  		for(var t in $scope.newModel.content.layers){
  			if($scope.newModel.content.layers[t].expanded != content || $scope.newModel.content.layers[t].datasetId != layer.datasetId){
  				delete $scope.newModel.content.layers[t].expanded;
  			}
  			if($scope.newModel.content.layers[t].datasetId==layer.datasetId && $scope.newModel.content.layers[t].expanded != content){
  				$scope.newModel.content.layers[t].expanded = content;
	  		}else {
	  			if($scope.newModel.content.layers[t].datasetId==layer.datasetId && $scope.newModel.content.layers[t].expanded == content){
	  				delete $scope.newModel.content.layers[t].expanded;
		  		}
	  		}
  		}
  	}
  	
  	$scope.deleteLayer = function(layer){
  		var index=$scope.newModel.content.layers.indexOf(layer);
		var tempPos = layer.order;
		$scope.newModel.content.layers.splice(index,1);
		var nextItem=$scope.newModel.content.layers[index];
		if(nextItem!=undefined){
			nextItem.order=tempPos;
		}
		delete $scope.newModel.content.columnSelectedOfDataset[layer.datasetId];
  	}
  	
  	$scope.move = function(e,row,direction){
		var lower, current, upper;
		for(var o in $scope.newModel.content.layers){
			if($scope.newModel.content.layers[o].order == row.order){
				current = o;
			}else if($scope.newModel.content.layers[o].order == row.order-1){
				upper = o;
			}else if($scope.newModel.content.layers[o].order == row.order+1){
				lower = o;
			}
		}
		if(direction=='up'){
			$scope.newModel.content.layers[upper].order = row.order;
			$scope.newModel.content.layers[current].order = row.order-1;
		}else{
			$scope.newModel.content.layers[lower].order = row.order;
			$scope.newModel.content.layers[current].order = row.order+1;
		}
		
	};
  	
  	$scope.addLayer = function(ev) {
  		$scope.myLayersId = [];
  		for(var m in $scope.newModel.content.layers){
			$scope.myLayersId.push($scope.newModel.content.layers[m].datasetId);
		}
  		$mdDialog.show({
			controller: function ($scope,$mdDialog) {
				
				//Get list of spatial layers
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.promiseGet("2.0/datasets", "","asPagedList=true&seeTechnical=TRUE&ids=&spatialOnly=true").then(
						function(result){
							$scope.availableSpatialLayers = [];
							var datasetMapById = [];
							var datasetMapByLabel = [];
							var datasetList = [];
							for(var l in result.data.item){
								//update internal service datasets structures
								var ds = result.data.item[l];
								datasetList.push(ds);
								datasetMapById[ds.id.dsId] = ds;
								datasetMapByLabel[ds.label] = ds;
								if($scope.myLayersId.indexOf(result.data.item[l].id.dsId)==-1){
									$scope.availableSpatialLayers.push(result.data.item[l]);
								}
							}
							cockpitModule_datasetServices.setDatasetList(datasetList);
							cockpitModule_datasetServices.setDatasetById(datasetMapById);
							cockpitModule_datasetServices.setDatasetByLabel(datasetMapByLabel);
						},
						function(error){
							// TODO MANAGE ERROR
						})
				
			    //Add the layers to the newModel
				$scope.add = function(){
					if (!$scope.newModel.content.layers) $scope.newModel.content.layers = [];
					for(var k in $scope.availableSpatialLayers){
						if($scope.availableSpatialLayers[k].selected){
							var tempLayer = $scope.availableSpatialLayers[k];
							var columnSelected = [];
							var newLayer =  {
								"type": "DATASET",
								"datasetId": tempLayer.id.dsId,
								"label": tempLayer.label,
								"name": tempLayer.name,
								"order": $scope.newModel.content.layers ? $scope.newModel.content.layers.length : 0,
								"attributes": [],
								"indicators": []
							}
							for(var i in tempLayer.metadata.fieldsMeta){
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'ATTRIBUTE') newLayer.attributes.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias});
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'MEASURE') newLayer.indicators.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias});
								if(tempLayer.metadata.fieldsMeta[i].fieldType === 'SPATIAL_ATTRIBUTE') newLayer.attributes.push({"name":tempLayer.metadata.fieldsMeta[i].name, "label":tempLayer.metadata.fieldsMeta[i].alias,"isGeoReference":true});
								tempLayer.metadata.fieldsMeta[i].aliasToShow = tempLayer.metadata.fieldsMeta[i].alias;
								columnSelected.push(tempLayer.metadata.fieldsMeta[i]);
							}
							$scope.newModel.content.layers.push(newLayer);
							var availableDatasets = cockpitModule_datasetServices.getAvaiableDatasets();
							var exists = false;
							for(var i in availableDatasets){
								if(availableDatasets[i].id.dsId == tempLayer.id.dsId) {
									exists = true;
									break;
								};
							}
							if(!exists) cockpitModule_datasetServices.addAvaiableDataset(tempLayer);
							if(!$scope.newModel.content.columnSelectedOfDataset) $scope.newModel.content.columnSelectedOfDataset = {};
							$scope.newModel.content.columnSelectedOfDataset[tempLayer.id.dsId] = columnSelected;
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
  	}
  	$scope.colorPickerOptions = {format:'hex'};
  	$scope.setIconType = function(layer,type) {
  	if (!layer.markerConf) layer.markerConf={}; //test anto
  		delete layer.markerConf.icon;
  		layer.markerConf.type = type;
  	}
  	
  	$scope.chooseIcon = function(ev, layer) {
  		
  		$mdDialog.show({
			controller: function ($scope,$mdDialog) {
				$scope.availableIcons = knModule_fontIconsService.icons;


				$scope.activeLayer = {};
				angular.copy(layer,$scope.activeLayer);
				
				$scope.setIcon = function(family,icon){
					if(!$scope.activeLayer.markerConf) $scope.activeLayer.markerConf = {};
					$scope.activeLayer.markerConf.icon = icon;
					$scope.activeLayer.markerConf.font = family.className;
				}
				$scope.choose = function(){
					angular.copy($scope.activeLayer,layer);
					$mdDialog.hide();
				}
				$scope.cancel = function(){
					$mdDialog.cancel();
				}
			},
			scope: $scope,
			preserveScope:true,
	      templateUrl: $scope.getTemplateUrl('mapWidgetAddIconDialog'),
	      targetEvent: ev,
	      clickOutsideToClose:true,
	      locals: {  }
	    })
  	}
  	
  	$scope.addColumnSelectedOfDataset = function(newModel){
  		if(!newModel.content.columnSelectedOfDataset){
  			newModel.content.columnSelectedOfDataset = [];
  			for(var a in newModel.content.layers){
  				if(newModel.content.layers[a].targetDefault) newModel.dataset = {"dsId":newModel.content.layers[a].datasetId};
  				for(var c in newModel.content.layers[a].attributes){
  	  				var targetAttr = newModel.content.layers[a].attributes[c];
  	  				newModel.content.columnSelectedOfDataset.push({"name":targetAttr.name, "alias":targetAttr.label,"aliasToShow":targetAttr.label, "fieldType":'ATTRIBUTE'})
  	  			}
  	  			for(var d in newModel.content.layers[a].indicators){
  	  				var targetInd = newModel.content.layers[a].attributes[d];
  	  				newModel.content.columnSelectedOfDataset.push({"name":targetInd.name, "alias":targetInd.label,"aliasToShow":targetInd.label, "fieldType":'MEASURE'})
  	  			}
  			}	
  		}
  	}
  	
  	//MAIN DIALOG BUTTONS
	$scope.saveConfiguration=function(){
		for(var c in $scope.newModel.content.layers){
			if($scope.newModel.content.layers[c].expanded) delete $scope.newModel.content.layers[c].expanded;
		}
		 mdPanelRef.close();
		 //$scope.addColumnSelectedOfDataset($scope.newModel);
		 angular.copy($scope.newModel,model);
		 finishEdit.resolve();
  	}

	$scope.cancelConfiguration=function(){
  		mdPanelRef.close();
  		finishEdit.reject();
  	}
}