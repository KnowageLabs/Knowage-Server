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
			$mdSidenav,
			$q,
			$sce,
			$filter,
			$location,
			sbiModule_translate,
			sbiModule_restServices,
			cockpitModule_mapServices,
			cockpitModule_datasetServices,
			cockpitModule_generalServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties,
			cockpitModule_generalServices){

		$scope.getTemplateUrl = function(template){
	  		return cockpitModule_generalServices.getTemplateUrl('mapWidget',template);
	  	}

		//ol objects
		$scope.layers = [];  //layers with features
		$scope.indicators = [];  //layers with indicators
		$scope.values = [];  //layers with values
		$scope.configs = [];  //layers with configuration

		//get config portions
		$scope.targetLayers= $scope.ngModel.content.targetLayersConf || [];
		$scope.baseLayer = $scope.ngModel.content.baseLayersConf || [];
		$scope.currentView = $scope.ngModel.content.currentView || {};

		//map id reference definition
		$scope.mapId = 'map-' + Math.ceil(Math.random()*1000).toString();

		$scope.optionsSidenavOpened = false;
		$scope.toggleSidenav = function(){
			$scope.optionsSidenavOpened = !$scope.optionsSidenavOpened;
	  	}
		
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


	    $scope.getAllLayers = function(){
	    	for (l in $scope.targetLayers){
	    		var layerDef  = $scope.targetLayers[l];
	    		$scope.setConfigLayer(layerDef.name, layerDef);
	    		if (layerDef.type === 'DATASET'){
	    			$scope.getDatasetFeatures(layerDef);
	    		}else if (layerDef.type === 'CATALOG'){
	    			//TODO implementare recuspero layer da catalogo
	    		}else{

	    		}
	    	}
	    }

	    $scope.reinit = function(){
            $scope.getAllLayers();
            $scope.createMap();
        }

	    $scope.refresh = function(element,width,height, data,nature,associativeSelection) {
	    	$scope.reinit();
	    }

	    $scope.getOptions =function(){
			var obj = {};
			obj["type"] = $scope.ngModel.type;
			return obj;
		}

	    $scope.getDatasetFeatures = function(layerDef){
    		//prepare object with metadata for desiderata dataset columns

    		var meta = [];
    		var geoColumn = null;
    		var selectedMeasure = null;
    		for (a in layerDef.attributes){
    			if (layerDef.attributes[a].isGeoReference || layerDef.attributes[a].showDetails){
	    			var att = {};
	    			att.name = layerDef.attributes[a].name;
	    			att.alias = layerDef.attributes[a].label;
	    			att.aliasToShow = layerDef.attributes[a].label;
	    			att.fieldType = 'ATTRIBUTE';
	    			meta.push(att);
    			}
    			if (layerDef.attributes[a].isGeoReference)
        			geoColumn = layerDef.attributes[a].name;
    		}
    		var measures = [];
    		var selectedMeasure;
    		for (m in layerDef.indicators){
    			if (layerDef.indicators[m].showMap){
	    			var measure = {};
	    			measure.selectedIndicator = layerDef.indicators[m].selectedIndicator;
	    			measure.name = layerDef.indicators[m].name;
	    			measure.alias = layerDef.indicators[m].label;
	    			measure.aliasToShow = layerDef.indicators[m].label;
	    			measure.aggregationSelected = layerDef.indicators[m].funct || 'SUM';
	    			measure.funcSummary = layerDef.indicators[m].funct || 'SUM';
	    			measure.fieldType = 'MEASURE';
	    			meta.push(measure);
	    			if (layerDef.targetDefault && measure.selectedIndicator) selectedMeasure = measure.alias;
//	    			if (measure.selectedIndicator) selectedMeasure = measure.alias;
    				measures.push({"name": measure.name, "value": measure.alias, "selectedIndicator": measure.selectedIndicator});
    			}
    		}

    		$scope.addIndicatorsToLayer(layerDef.name, selectedMeasure, measures);

    		var model = {content: {columnSelectedOfDataset: meta }};
    		var features = [];

    		//get the dataset columns values
	    	cockpitModule_datasetServices.loadDatasetRecordsById(layerDef.datasetId, undefined, undefined, undefined, undefined, model).then(

	    		function(allDatasetRecords){
					var layer = cockpitModule_mapServices.getFeaturesDetails(geoColumn, selectedMeasure, layerDef, allDatasetRecords);
					if (layer == null){
						$scope.showAction($scope.translate.load('sbi.cockpit.map.nogeomcorrectform')); //dataset geometry column value isn't correct. It should be a couble of numbers [-12 12] or [-12, 12]
						return;
					}

					layer.targetDefault = layerDef.targetDefault || false;
					layer.name = layerDef.name;
					layer.setZIndex(layerDef.order*1000);
					$scope.map.addLayer(layer); 			//add layer to ol.Map
					$scope.addLayer(layerDef.name, layer);	//add layer to internal object
					$scope.setLayerProperty (layerDef.name, 'geoColumn',geoColumn),
					$scope.setValuesLayer(layerDef.name, allDatasetRecords); //add values to internal object
					//at least simulates the click action for the target layer and hides the others
					var nLayerDefault;
					if (!layer.targetDefault){
						$scope.toggleLayer(layer.name); //hide layers not target
					}

//					cockpitModule_mapServices.setActiveConf($scope.getNumberLayerDefault());

			},function(error){
				console.log("Error loading dataset with id [ "+layerDef.datasetId+"] ");
				$scope.showAction($scope.translate.load('sbi.cockpit.map.dsError')); //error during the execution of data
			});
    	}

	    $scope.createMap = function (){
	    	var popupContainer = document.getElementById('popup');
    		//create the map with first base layer
            var baseLayer = cockpitModule_mapServices.getBaseLayer($scope.baseLayer[0]);

            //create overlayers (popup..)
            var overlay = new ol.Overlay({
              element: popupContainer,
              autoPan: true,
              autoPanAnimation: {
                duration: 250
              }
            });

//           if (!$scope.currentView.center)  $scope.currentView.center = [0,0];
           if (!$scope.currentView.center)  $scope.currentView.center =  [-122.2585837, 37.76930310];
            var view = new ol.View({
//            	projection: 'EPSG:4326',
            	center: ol.proj.fromLonLat($scope.currentView.center),
//            	center: ol.proj.transform($scope.currentView.center, 'EPSG:3857', 'EPSG:4326'),
			    zoom: $scope.currentView.zoom || 3
              });

    		$scope.map = new ol.Map({
				     target:  $scope.mapId,
				     layers: [baseLayer],
				     overlays: [overlay],
				     view: view
    		});

    		$scope.addViewEvents();
    		$scope.addMapEvents(overlay);
    	}


	    $scope.addViewEvents = function(){
	    	//view events
	    	var view = $scope.map.getView();
            view.on("change:resolution", function(e) {
            	//zoom action
        	    if (Number.isInteger(e.target.getZoom())) {
        	      console.log(e.target.getCenter());
        	    }
//              $scope.map.render();
//              $scope.refreshLayers();
//              map.getView().setCenter(ol.proj.transform([lat, long], 'EPSG:4326', 'EPSG:3857'));
        	    $scope.ngModel.content.zoom = e.target.getZoom();
//              $scope.currentView.center = e.target.getCenter();
              $scope.ngModel.content.currentView.center  = [-122.2585837, 37.76930310]; //temporaneo
            });

	    }

	    $scope.addMapEvents = function (overlay){
	    	//Elements that make up the popup.
            var popupContent = document.getElementById('popup-content');
            var closer = document.getElementById('popup-closer');

            /**
             * Add a click handler to hide the popup.
             * @return {boolean} Don't follow the href.
             */
            closer.onclick = function() {
              overlay.setPosition(undefined);
              closer.blur();
              return false;
            };

    		//map events
    		$scope.map.on('singleclick', function(evt) {
    			//popup detail
            	var feature = $scope.map.forEachFeatureAtPixel(evt.pixel,
            	            function(feature, layer) {
            	                console.log("feature on click: ",feature);
            	                return feature;
            	            });
            	var layer = $scope.map.forEachFeatureAtPixel(evt.pixel,
        	            function(feature, layer) {

        	                return layer;
        	            });

    	        if (feature) {
    	            var geometry = feature.getGeometry();
    	            var props = feature.getProperties();
    	            var coordinate = geometry.getCoordinates();
    	            var config = $scope.getConfigLayer(layer.name);
    	            var text = "";
    	            for (var p in props){
    	            	if ($scope.isDisplayableProp(p, config.config))
    	            	text += '<b>' + p + ":</b> " + props[p] + '<br>';
    	            }

    		        popupContent.innerHTML = '<h2>Details</h2><code>' + text + '</code>';
    		        overlay.setPosition(coordinate);
    	        }
             });

    		// change mouse cursor when over marker
    	      $scope.map.on('pointermove', function(e) {
    	    	  var pixel = $scope.map.getEventPixel(e.originalEvent);
    	    	  var hit = $scope.map.hasFeatureAtPixel(pixel);
    	    	  $scope.map.getViewport().style.cursor = hit ? 'pointer' : '';
    	      });

    		$scope.map.on('moveend', function(evt){
    			//check active point: pay attention that the event 'moveend' is called on each feature creation
//    			var view = $scope.map.getView();
//    			var mapExtent = view.calculateExtent($scope.map.getSize());
//
//    			for (l in $scope.layers){
//    				var source = $scope.layers[l].layer.getSource();
//    				source.forEachFeature(function(feature){
//    					//features iteration
//					  var coord = feature.getGeometry().getCoordinates();
//					  var isIntoExtent = ol.extent.containsCoordinate(mapExtent,coord);
//					  if (isIntoExtent){
////    		    				 var att = feature.getProperties();
//		    				console.log("*feature with coordinate [" + coord + "] is shown in view! " );
//    		    		}
//    				});
//    			}
    			var view = $scope.map.getView();
    			if (!$scope.ngModel.content.currentView) $scope.ngModel.content.currentView = {};
//    			$scope.ngModel.content.currentView.center  = ol.proj.transform(view.getCenter(), 'EPSG:4326', 'EPSG:3857');
    			$scope.ngModel.content.currentView.center  = [-122.2585837, 37.76930310]; //temporaneo
    			$scope.ngModel.content.currentView.zoom = view.getZoom();
//    			$scope.ngModel.content.currentView.center = view.getCenter();
    		});
	    }


	    //control panel events
	    $scope.toggleLayer = function(n){
	    	var l = $scope.getLayerByName(n);
	    	if (!l) return; //do nothing
	    	var toggle = !l.getVisible();
	    	l.setVisible(!l.getVisible());
	    }

	    $scope.getLayerVisibility = function(n){
	    	var l = $scope.getLayerByName(n);
	    	if (!l) return; //do nothing
	    	return l.getVisible();
	    }

	    $scope.thematizeMeasure = function (l, m){
	    	var layer = $scope.getLayerByName(l);
	    	var layerValues = $scope.getValuesLayer(l).values;
	    	var layerKeyColumn =  $scope.getLayerProperty(l, 'geoColumn');
	    	var layerConfig;
	    	for (var c=0; c<$scope.targetLayers.length;c++){
	    		if ($scope.targetLayers[c].name === l){
	    			layerConfig = $scope.targetLayers[c];
	    			break;
	    		}
	    	}
	    	cockpitModule_mapServices.refreshStyle(layer, m, layerConfig, layerValues, layerKeyColumn);
	    }

	    $scope.refreshLayers = function (){
	    	var m;
	    	for (l in $scope.getLayers()){
	    		var tmpLayer = $scope.getLayers()[l].name;
	    		m = $scope.getSelectedIndicator(tmpLayer);
	    		if (m)
	    			$scope.thematizeMeasure(tmpLayer, m);
	    	}
	    }

	    //getter and setter of internal objects
	    $scope.getLayers = function(){
	    	return $scope.layers;
	    }

	    $scope.getLayerByName = function(n){
	    	for (l in $scope.layers){
	    		if ($scope.layers[l].name === n)
	    			return $scope.layers[l].layer;
	    	}

	    	return null;
	    }

	    $scope.setLayers = function(l){
	    	$scope.layers = l;
	    }

	    $scope.addLayer = function(n,l){
	    	$scope.layers.push({"name": n,"layer":l});
	    }

	    $scope.getLayerProperty = function(l, p){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			return $scope.layers[o][p] || null;
	    	}

	    }

	    $scope.setLayerProperty = function(l, p, v){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			$scope.layers[o][p] = v;
	    	}

	    }

	    $scope.getIndicators = function(){
	    	return $scope.indicators;
	    }

	    $scope.getSelectedIndicator = function (n){
	    	for (l in $scope.indicators){
	    		if ($scope.indicators[l].name === n){
	    			var inds = $scope.indicators[l];
	    			return inds.defaultIndicator;
	    		}
	    	}

	    	return null;
	    }

	    $scope.setIndicators = function(i){
	    	$scope.indicators = i;
	    }

	    $scope.addIndicatorsToLayer = function(n,d,i){
	    	$scope.indicators.push({"name": n, "defaultIndicator": d, "indicators":i});
	    }

	    $scope.addIndicator = function(n,v){
	    	$scope.indicators.push({"name": n, "value":v});
	    }

	    $scope.getValuesLayer = function (n){
	    	for (l in $scope.values){
	    		if ($scope.values[l].name === n)
	    			return $scope.values[l];
	    	}

	    	return null;
	    }

	    $scope.setValuesLayer = function (n,v){
	    	$scope.values.push({"name":n, "values":v});
	    }

	    $scope.setConfigLayer = function(n,c){
	    	$scope.configs.push({"name": n,"config":c});
	    }

	    $scope.getConfigLayer = function(n){
	    	for (l in $scope.configs){
	    		if ($scope.configs[l].name === n)
	    			return $scope.configs[l];
	    	}

	    	return null;
	    }

	    $scope.isDisplayableProp = function (p, config){
	    	for (a in config.attributes){
    			if (p === config.attributes[a].label && config.attributes[a].showDetails){
	    			return true;
    			}
	    	}

	    	for (i in config.indicators){
    			if (p === config.indicators[i].label && config.indicators[i].showDetails){
	    			return true;
    			}
	    	}
	    	return false;
	    }

	    $scope.getNumberLayerDefault = function (){
	    	for (l in $scope.targetLayers){
	    		if ($scope.targetLayers[l].targetDefault)
	    			return l;
	    	}
	    	return 0; //if no layer is defined as default returns the first
	    }

	    //functions calls
		$scope.getAllLayers();



		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: mapWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: $scope.getTemplateUrl('mapWidgetEditPropertyTemplate'),
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

	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("map",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();