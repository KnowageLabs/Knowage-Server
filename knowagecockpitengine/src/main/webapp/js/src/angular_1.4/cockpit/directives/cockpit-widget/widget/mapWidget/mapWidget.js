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
(function() {
	angular
		.module('cockpitModule')
		.directive('cockpitMapWidget',function(){
			return{
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/mapWidget/templates/mapWidgetTemplate.html',
				controller: cockpitMapWidgetControllerFunction,
				compile: function (tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {
						},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							element.ready(function () {
								scope.initWidget();
								scope.createMap();
							});
						}
					};
				}
			}
		})

	function cockpitMapWidgetControllerFunction(
			$scope,
			$mdDialog,
			$mdToast,
			$timeout,
			$mdPanel,
			$q,
			$sce,
			$filter,
			$location,
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_mapServices,
			cockpitModule_datasetServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties){
		
		//ol objects 
		$scope.layers = [];  //layers with features
		
		//get config portions
		$scope.targetLayers= $scope.ngModel.content.targetLayersConf || [];
		$scope.baseLayer = $scope.ngModel.content.baseLayersConf || [];
		$scope.currentView = $scope.ngModel.content.currentView || {};
		
		//map id reference definition	
		$scope.mapId = 'map-' + Math.ceil(Math.random()*1000).toString();
	  	console.log("$scope.mapId: ", $scope.mapId);
	  	
	  	
	  	
	  	$scope.showAction = function(text) {
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top')

			$mdToast.show(toast).then(function(response) {

				if ( response == 'ok' ) {
			
				}
			});
		}
	      
	    $scope.getLayers = function(){	    	
	    	for (l in $scope.targetLayers){
	    		var layerDef  = $scope.targetLayers[l];
	    		if (layerDef.type === 'DATASET'){
	    			$scope.getDatasetFeatures(layerDef);
	    		}else if (layerDef.type === 'CATALOG'){
	    			//TODO implementare recuopero layer da catalogo
	    		}else{
	    			
	    		}
	    	}	 
	    }
	    
	    $scope.getDatasetFeatures = function(layerDef){
    		//prepare object with metadata for desiderata dataset columns 
    		var meta = [];
    		var geoColumn = null;
    		for (a in layerDef.attributes){
    			var att = {};
    			att.name = layerDef.attributes[a].name;
    			att.alias = layerDef.attributes[a].label;
    			att.aliasToShow = layerDef.attributes[a].label;
    			att.fieldType = 'ATTRIBUTE';
    			meta.push(att);
    			if (layerDef.attributes[a].isGeoReference)
    				geoColumn = layerDef.attributes[a].name;
    		}
    		var measures = [];
    		for (m in layerDef.indicators){
    			var measure = {};
    			measure.name = layerDef.indicators[m].name;
    			measure.alias = layerDef.indicators[m].label;
    			measure.aliasToShow = layerDef.indicators[m].label;
    			measure.aggregationSelected = layerDef.indicators[m].funct || 'SUM';
    			measure.funcSummary = layerDef.indicators[m].funct || 'SUM';
    			measure.fieldType = 'MEASURE';
    			meta.push(measure);
    		}
    		var model = {content: {columnSelectedOfDataset: meta }};
    		var features = [];
    		//get the dataset columns values
	    	cockpitModule_datasetServices.loadDatasetRecordsById(layerDef.datasetId, undefined, undefined, undefined, undefined, model).then(
	    		function(allDatasetRecords){
					var layer = cockpitModule_mapServices.getFeaturesDetails(geoColumn, allDatasetRecords);
					if (layer == null){
						$scope.showAction($scope.translate.load('sbi.cockpit.map.nogeomcorrectform')); //dataset geometry column value isn't correct. It should be a couble of numbers [-12 12] or [-12, 12]
						return;
					}
					$scope.map.addLayer(layer);
			},function(error){
				console.log("Error loading dataset with id [ "+layerDef.datasetId+"] ");
				$scope.showAction($scope.translate.load('sbi.cockpit.map.dsError')); //error during the execution of data
			}
			); 
	    	
    	}
    	

	    $scope.createMap = function (){
    		//create the map with base layer
		    
            var baseLayer = cockpitModule_mapServices.getBaseLayer($scope.baseLayer[0]);

    		$scope.map = new ol.Map({
				//	   target: olTarget,
				     target:  $scope.mapId,
//				     target: 'map',
				     layers: [baseLayer],
				     view: new ol.View({
				       center: ol.proj.fromLonLat($scope.currentView.center), 
				       zoom: $scope.currentView.zoom || 4
				     })
    		});
    	}
	    
	    //functions calls
//	    $scope.createMap();
		$scope.getLayers();
		

		
		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: mapWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/mapWidget/templates/mapWidgetEditPropertyTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: true,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: false,
					locals: {finishEdit:finishEdit,model:$scope.ngModel},
			};
			$mdPanel.open(config);
			return finishEdit.promise;
		}
		
	}
	
	function mapWidgetEditControllerFunction($scope,finishEdit,model,sbiModule_translate,$mdDialog,mdPanelRef,$mdToast,$timeout,$location){
		$scope.translate=sbiModule_translate;
		$scope.newModel = angular.copy(model);
		
		//get templates location
	  	$scope.basePath = $location.$$absUrl.substring(0,$location.$$absUrl.indexOf('api/'));
	  	$scope.templatesUrl = 'js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/mapWidget/templates/';
	  	$scope.getTemplateUrl = function(template){
	  		return $scope.basePath + $scope.templatesUrl + template +'.html';
	  	}
	  	
	  	$scope.addLayer = function(ev) {
	  		$mdDialog.show({
				controller: function ($scope,$mdDialog) {
					
					$scope.add = function(){
						$mdDialog.hide();
					}
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

	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("map",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();