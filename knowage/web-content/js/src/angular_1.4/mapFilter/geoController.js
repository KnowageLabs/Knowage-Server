/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
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
 * @author Benedetto Milazzo (benedetto.milazzo@eng.it)
 */

var app = angular.module('geoManager', [ 'ngMaterial', 'geoModule', 'sbiModule' ]);

app.controller('mapCtrl', 
		[ '$scope', '$window', '$map', 'geoModule_layerServices', 'sbiModule_logger', 
	        'sbiModule_translate', 'geo_interaction',
	        'geoModule_templateLayerData', 'sbiModule_restServices', 
            mapCtrlFunction ]);

function mapCtrlFunction($scope, $window, $map, geoModule_layerServices, 
		sbiModule_logger, sbiModule_translate, geo_interaction, geoModule_templateLayerData, 
		sbiModule_restServices){
	$scope.layerConf = null;
	$scope.selectedFeatures = [];

	$scope.sbiModule_translate = sbiModule_translate;

//	$scope.isFilterSelectionSaved = true;

	$scope.geo_interaction = geo_interaction;

	$scope.showPanelFlag = function() {
		return ($scope.geo_interaction.type == 'cross');
	};
//	$scope.isButtonDisabled = function() {
//		return (
//				$scope.geo_interaction.selectedFeatures.length == 0 ||
//				$scope.isFilterSelectionSaved);
//	};

	$scope.saveSelectedFeatures = function() {
		var propKey = $scope.layerProperty;
		var selectedFeatures = $scope.geo_interaction.selectedFeatures;
		
		var dataToReturn = [];
		
		for(var i = 0; i < selectedFeatures.length ; i++) {
			var feature = selectedFeatures[i];
			var featureProperties = feature.getProperties ? feature.getProperties() : feature.properties;
			
			var value = featureProperties[propKey];
			
			if(value && value != null ) {
				dataToReturn.push(value);
			}
		}
		
		// reaching ExtJs parent function
		if(parent && parent.parent && parent.parent.mapFilterSelectedProp) {
			parent.parent.mapFilterSelectedProp(dataToReturn);

		// reaching Angular parent function
		} else if($window && $window.parent && $window.parent.angular && $window.parent.angular.element
				&& $window.parent.angular.element($window.frameElement).scope()) {
			
			var angularMapParamScope = $window.parent.angular.element($window.frameElement).scope();
			angularMapParamScope.updateSelectedFeatures(dataToReturn);
		}
		
//		$scope.isFilterSelectionSaved = true;
	};

	// Workaround for forcing the angular bind, since the click event 
	// on the selected features depends by a non-angular component
	$scope.geo_interaction.addSelectedFeaturesCallbackFunction(function(){
		if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase != '$digest') {
//			$scope.isFilterSelectionSaved = false;
			$scope.$apply();
		}
		
		$scope.saveSelectedFeatures();
	});

	$scope.setLayerConf = function(layerConfLabel, layerProperty, selectedPropData, multivalueFlag) {
		$scope.layerConfLabel = layerConfLabel;
		$scope.layerProperty = layerProperty;
		$scope.selectedPropData = selectedPropData || '';
		$scope.multivalueFlag = multivalueFlag || false;
		
		sbiModule_logger.log(
				"Initialing map filter with parameters { layerConfLabel:" + layerConfLabel + ","
				+ " layerProperty:" + layerProperty + ","
				+ " selectedPropData:[" + selectedPropData + "],"
				+ " multivalueFlag:" + multivalueFlag + " }"
			);

		$scope.selectedFeatures = geo_interaction.selectedFeatures;

		var selectedPropDataAsArray = selectedPropData.trim() != '' ? 
				selectedPropData.split(',') : [];
		
		if(!$scope.multivalueFlag) {
			var firstSelection = (selectedPropDataAsArray && selectedPropDataAsArray.length > 0) ?
					selectedPropDataAsArray[0] : null;
			
			if (firstSelection != null) {
				selectedPropDataAsArray = [firstSelection];
			}
		}
		
		var selectedPropDataAsArrayNoQuotes = [];
		for(var i = 0; i < selectedPropDataAsArray.length; i++) {
			var propDatum = selectedPropDataAsArray[i];
			
			var propDatumNoQuote = propDatum.replace(/^["']/g,'').replace(/["']$/g,'');
			
			selectedPropDataAsArrayNoQuotes.push(propDatumNoQuote);
		}
		selectedPropDataAsArray = selectedPropDataAsArrayNoQuotes;

		var OPEN_STREET_MAP_LAYER_CONF = {
			type : "TMS", category : "Default", label : "OpenStreetMap",
			layerURL : "http://tile.openstreetmap.org/",
			layerOptions : {type : "png", displayOutsideMaxExtent : true }
		};

		var openStreetLayer = geoModule_layerServices.createLayer(OPEN_STREET_MAP_LAYER_CONF, true);
		$map.addLayer(openStreetLayer);
		$map.updateSize();
		$map.render();

		var labelUriParameter = 'label=' + layerConfLabel;
		sbiModule_restServices.get("layers", 'getLayerByLabel', labelUriParameter).success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					sbiModule_logger.log("layer not retrieved: " + status);
				} else {
					sbiModule_logger.log("layer retrieved with id: " + data.layerId);
					
					$scope.layerConf = data;
					geoModule_templateLayerData.data = data;

					var layerToAdd = geoModule_layerServices.createLayer($scope.layerConf, false);
					
					if(data.type.toLowerCase() != 'file') {
						layerToAdd.setZIndex(0);
					}
					
					var isMultivalue = ($scope.multivalueFlag && !(data.type.toLowerCase() == 'wms'));
					
					if( data.type.toLowerCase() == 'file' && layerToAdd.hasOwnProperty("$$state") ){
						layerToAdd.then(function(tmpLayer) {
							geoModule_layerServices.templateLayer = tmpLayer;
							geoModule_layerServices.initLayerAndSelection(tmpLayer, layerProperty, selectedPropDataAsArray, isMultivalue);
						});
					} else {
						geoModule_layerServices.templateLayer = layerToAdd;
						geoModule_layerServices.initLayerAndSelection(layerToAdd, layerProperty, selectedPropDataAsArray, isMultivalue);
					}
				}
			}).error(function(data, status, headers, config) {
				sbiModule_logger.log("layer not retrieved: " + status);
			});
	};
};