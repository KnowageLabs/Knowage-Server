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
			sbiModule_messaging,
			sbiModule_restServices,
			cockpitModule_mapServices,
			cockpitModule_datasetServices,
			cockpitModule_generalServices,
			cockpitModule_widgetConfigurator,
			cockpitModule_widgetServices,
			cockpitModule_widgetSelection,
			cockpitModule_properties){
		
		//ol objects
		$scope.layers = [];  //layers with features
		$scope.values = [];  //layers with values
		$scope.configs = []; //layers with configuration


		$scope.getTemplateUrl = function(template){
	  		return cockpitModule_generalServices.getTemplateUrl('mapWidget',template);
	  	}

		
	    $scope.reinit = function(){
	    	var isNew = ($scope.layers.length == 0);
	    	for (l in $scope.ngModel.content.layers){
	    		//remove old layers 
	    		var previousLayer = $scope.getLayerByName($scope.ngModel.content.layers[l].name);
	    		if (previousLayer) $scope.map.removeLayer(previousLayer); //ol obj
	    	}
	    	$scope.removeLayers(); //clean internal obj
	    	$scope.getLayers();
	    	
	    	if (isNew) $scope.createMap();

	    	if (!$scope.map.getSize()){
    			$scope.map.setSize([cockpitModule_widgetConfigurator.map.initialDimension.width, 
    							    cockpitModule_widgetConfigurator.map.initialDimension.height]);
    		}else{
    			$scope.map.setSize($scope.map.getSize());
    		}
			$scope.map.renderSync();
        }
	    
	    $scope.optionsSidenavOpened = false;
		$scope.toggleSidenav = function(){
			$scope.optionsSidenavOpened = !$scope.optionsSidenavOpened;
			$timeout(function() {
				$scope.map.updateSize();
			}, 500);
		}

	    $scope.refresh = function(element,width,height, data,nature,associativeSelection) {
	    	$scope.reinit();
	    }
	    
	    $scope.getOptions =function(){
			var obj = {};
			obj["type"] = $scope.ngModel.type;
			return obj;
		}
	    
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
	    
//############################################## SPECIFIC MAP WIDGET METHODS #########################################################################
	    
	    $scope.getLayers = function () {
		    for (l in $scope.ngModel.content.layers){
		    	var layerDef =  $scope.ngModel.content.layers[l];
	    		$scope.setConfigLayer(layerDef.name, layerDef);
	    		if (layerDef.type === 'DATASET'){
	    			$scope.getFeaturesFromDataset(layerDef);
	    		}else if (layerDef.type === 'CATALOG'){
	    			//TODO implementare recupero layer da catalogo
	    		}else{
	    			sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.typeLayerNotManaged'), 'Title', 3000);
	    			console.log("Layer with type ["+layerDef.type+"] not managed! ");
	    		}
	    	}
	    }

	    $scope.initializeTemplate = function (){
	    	if (!$scope.ngModel.content.currentView)  $scope.ngModel.content.currentView = {};
	    	if (!$scope.ngModel.content.layers) $scope.ngModel.content.layers = [];
	    	if (!$scope.ngModel.content.baseLayersConf) $scope.ngModel.content.baseLayersConf = [];
	    	if (!$scope.ngModel.content.columnSelectedOfDataset) $scope.ngModel.content.columnSelectedOfDataset = {} ;

	    	if (!$scope.ngModel.content.currentView.center) $scope.ngModel.content.currentView.center = [0,0]; 
	    	
	    	if (!$scope.ngModel.content.mapId){
	    		$scope.ngModel.content.mapId = 'map-' + Math.ceil(Math.random()*1000).toString();
	    		console.log("*** New mapId:" ,$scope.ngModel.content.mapId );
	    	}	    	
	    	
	    	//set default indicator (first one) for each layer
	    	for (l in $scope.ngModel.content.layers){
	    		var columns = $scope.getColumnSelectedOfDataset($scope.ngModel.content.layers[l].dsId);
	    		for ( c in columns){
	    			if (columns[c].properties.showMap){
	    				$scope.ngModel.content.layers[l].defaultIndicator = columns[c].aliasToShow;	
	    				break;
	    			}
	    		}
	    	}	
	    }
	    
	    $scope.getColumnSelectedOfDataset = function(dsId) {
	    	for (di in $scope.ngModel.content.columnSelectedOfDataset){
	    		if (di == dsId){
	    			return $scope.ngModel.content.columnSelectedOfDataset[di];
	    		}
	    	}
	    	return null;
	    }
	    
	    $scope.addViewEvents = function(){
	    	//view events
	    	var view = $scope.map.getView();
            view.on("change:resolution", function(e) {
            	//zoom action
//        	    if (Number.isInteger(e.target.getZoom())) {
//        	    }
        	    $scope.ngModel.content.currentView.zoom = e.target.getZoom();
        	    $scope.ngModel.content.currentView.center = e.target.getCenter();
            });

	    }

	    $scope.addMapEvents = function (overlay){
	    	//Elements that make up the popup.
            var popupContent = document.getElementById('popup-content');
            var closer = document.getElementById('popup-closer');
            
            if (closer){
	            closer.onclick = function() {
	              overlay.setPosition(undefined);
	              closer.blur();
	              return false;
	            };
            }else
            	console.log("<div> with identifier 'popup-closer' doesn't found !!! It isn't impossible set the popup detail content ");

    		$scope.map.on('singleclick', function(evt) {
    			//popup detail
    			if (!popupContent){
    				console.log("<div> with identifier 'popup-content' doesn't found !!! It isn't impossible set the popup detail content ");
    				return;
    			}
    				
            	var feature = $scope.map.forEachFeatureAtPixel(evt.pixel,
            	            function(feature, layer) {
//            	                console.log("feature on click: ",feature);
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
    	            var config = $scope.getColumnSelectedOfDataset(layer.dsId);
    	            var text = "";
    	            for (var p in props){
    	            	if ($scope.isDisplayableProp(p, config))
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
    			var view = $scope.map.getView();
    			if (!$scope.ngModel.content.currentView) $scope.ngModel.content.currentView = {};
    			$scope.ngModel.content.currentView.center = view.getCenter();
    			$scope.ngModel.content.currentView.zoom = view.getZoom();
    		});
	    }

	    $scope.isDisplayableProp = function (p, config){
	    	for (c in config){
	    		if (p == config[c].aliasToShow && config[c].properties.showDetails){
	    			return true;
	    		}
	    	}
	    	return false;
	    }
	   
	    var styleCache = {};
	    $scope.layerStyle = function(feature, resolution){
//	          var size = feature.get('features').length;
			var props  = feature.getProperties();
			var parentLayer = feature.get('parentLayer') 
			var configThematizer = $scope.getConfigLayer(parentLayer).analysisConf || {};
			var configMarker = $scope.getConfigLayer(parentLayer).markerConf || {};
	      	var value =  props[cockpitModule_mapServices.getActiveIndicator()] || 0;
			var style;
			var useCache = false; //cache isn't use for analysis, just with fixed marker
			
			switch (configThematizer.defaultAnalysis) {
			case 'choropleth':
				style = cockpitModule_mapServices.getChoroplethStyles(value, props, configThematizer.choropleth, configMarker);
				break;
			case 'proportionalSymbol':
				style = cockpitModule_mapServices.getProportionalSymbolStyles(value, props, configThematizer.proportionalSymbol, configMarker);
				break;
			default:
				style = cockpitModule_mapServices.getOnlyMarkerStyles(props, configMarker);
				useCache = true;
			}
			
			if (useCache && !styleCache[parentLayer]) {
		          styleCache[parentLayer] = style;
		          return styleCache[parentLayer] ;
			} else {
				return style;
			}
	    }
	    
		$scope.getFeaturesDetails = function(geoColumn, selectedMeasure, config, values){
			if (values != undefined){
				var geoFieldName;
				var geoFieldValue;
				var featuresSource = new ol.source.Vector();
	
				for(var k=0; k < values.metaData.fields.length; k++){
					var field = values.metaData.fields[k];
					if (field.header === geoColumn){
						geoFieldName = field.name;
						break;
					}
				}
				if (geoFieldName){
					for(var r=0; r < values.rows.length; r++){
						//get coordinates
						var lonlat;
						var row = values.rows[r];
						geoFieldValue = row[geoFieldName].trim();
						if (geoFieldValue.indexOf(" ") > 0){
							lonlat = geoFieldValue.split(" ");
						}else if (geoFieldValue.indexOf(",")){
							lonlat = geoFieldValue.split(",");
						}else{
							sbiModule_messaging.showInfoMessage($scope.translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (lonlat.length != 2){
							sbiModule_messaging.showInfoMessage($scope.translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (config.analysisConf && config.analysisConf.defaultAnalysis == 'proportionalSymbol'){							
							//get config for thematize
							if (!selectedMeasure) selectedMeasure = config.defaultIndicator;
							cockpitModule_mapServices.setActiveIndicator(selectedMeasure);
							if (selectedMeasure){
								if (!cockpitModule_mapServices.getCacheProportionalSymbolMinMax()) cockpitModule_mapServices.setCacheProportionalSymbolMinMax({}); //just at beginning
								if (!cockpitModule_mapServices.getCacheProportionalSymbolMinMax().hasOwnProperty(selectedMeasure)){
									cockpitModule_mapServices.loadIndicatorMaxMinVal(selectedMeasure, values);
								}
							}
						}
						
						//set ol objects
						var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
						var feature = new ol.Feature();  
				        var coordinate = transform([parseFloat(lonlat[0].trim()), parseFloat(lonlat[1].trim())]);
				        var geometry = new ol.geom.Point(coordinate);
				        feature.setGeometry(geometry);
//				        feature.setStyle($scope.layesrStyle);
				        $scope.addDsPropertiesToFeature(feature, row, values.metaData.fields);
				       //at least add the layer owner
				        feature.set("parentLayer",config.name);
				        featuresSource.addFeature(feature);
					}
					
					return featuresSource;
				}
			}
			return new ol.source.Vector();
		}
	    
		$scope.addDsPropertiesToFeature = function (f, row, meta){
			//add columns value like properties
			for (c in row){
				f.set($scope.getHeaderByColumnName(c, meta), row[c]);
			}
		}
		
		$scope.getHeaderByColumnName = function(cn, fields) {
			var toReturn = cn;
			
			for (n in fields){
				if (fields[n] && fields[n].name === cn){
					return fields[n].header;
				}
			}
			return toReturn;
		}
		
	    $scope.getFeaturesFromDataset = function(layerDef){
    		//prepare object with metadata for desiderata dataset columns
	    	var geoColumn = null;
    		var selectedMeasure = null;
    		var columnsForData = [];
    		
    		var columnsForData = $scope.getColumnSelectedOfDataset(layerDef.dsId) || [];
	    	
    		for (f in columnsForData){
    			var tmpField = columnsForData[f];
    			if (tmpField.fieldType == "SPATIAL_ATTRIBUTE")
    				geoColumn = tmpField.name;
    			else if (tmpField.properties.defaultIndicator && tmpField.properties.showMap) 
    				selectedMeasure = tmpField.aliasToShow;
//    			else if (!tmpField.properties.showDetails && !tmpField.properties.showMap)
//    				continue;
//    			
//    			columnsForData.push(tmpField);
    		}    	
    		
    		var model = {content: {columnSelectedOfDataset: columnsForData }};
    		var features = [];
    		var layer =  new ol.layer.Vector();

    		//get the dataset columns values
    		var dsId = layerDef.dsId; // || layerDef.datasetId;
	    	cockpitModule_datasetServices.loadDatasetRecordsById(dsId, undefined, undefined, undefined, undefined, model).then(

	    		function(allDatasetRecords){
					var featuresSource = $scope.getFeaturesDetails(geoColumn, selectedMeasure, layerDef, allDatasetRecords);
					if (featuresSource == null){ 
						return;
					}

			    	var layer = new ol.layer.Vector({source: featuresSource,
			    									 style: $scope.layerStyle});

					//add decoration to layer element
					layer.targetDefault = layerDef.targetDefault || false;
					layer.name = layerDef.name;
					layer.dsId = layerDef.dsId;
					layer.setZIndex(layerDef.order*1000);
					$scope.map.addLayer(layer); 			//add layer to ol.Map
					$scope.addLayer(layerDef.name, layer);	//add layer to internal object
					$scope.setLayerProperty (layerDef.name, 'geoColumn',geoColumn),
					$scope.setValuesLayer(layerDef.name, allDatasetRecords); //add values to internal object
					$scope.updateCoordinatesAndZoom(layer, true);

			},function(error){
				console.log("Error loading dataset with id [ "+layerDef.dsId+"] "); 
				sbiModule_messaging.showInfoMessage($scope.translate.load('sbi.cockpit.map.datasetLoadingError').replace("{0}",layerDef.dsId), 'Title', 3000);
			});	
    	}
	      
	    $scope.updateCoordinatesAndZoom = function(l, setValues){
	    	var coord;
	    	var zoom;
	    	
	    	if ($scope.ngModel.content.currentView.center[0] == 0 && $scope.ngModel.content.currentView.center[1] == 0){
		    	if (l.getSource().getFeatures().length>0 && l.getSource().getFeatures()[0].getGeometry().getType() == 'Point')
		    		coord = l.getSource().getFeatures()[0].getGeometry().getCoordinates();
				else
					coord = l.getSource().getFeatures()[0].getGeometry().getCoordinates()[0][0][0];
		    	
		    	if(l.getSource().getFeatures().length>35){
	    			zoom = 4;
				}else{
					zoom = 5;
				}
	    	
		    	//update coordinates and zoom within the template
		    	$scope.ngModel.content.currentView.center = coord;
		    	$scope.ngModel.content.currentView.zoom = zoom;
		    	
		    	if (setValues){
		    		$scope.map.getView().setCenter(coord);
		    		$scope.map.getView().setZoom(zoom);
		    	} 		
	    	}
	    }
	    
	    $scope.createMap = function (){
	    	$scope.initializeTemplate();
	    	
	    	//create the base layer
            $scope.baseLayer = cockpitModule_mapServices.getBaseLayer($scope.ngModel.content.baseLayersConf[0]);

	    	//setting coordinates (from the first layer if they aren't setted into the template)
            if ($scope.ngModel.content.currentView.center[0] == 0 && $scope.ngModel.content.currentView.center[1] == 0 && $scope.layers.length > 0){
	    		var tmpLayer = $scope.layers[0].layer;
	    		$scope.updateCoordinatesAndZoom(tmpLayer, false);

	    		$scope.addViewEvents();
	    		$scope.addMapEvents(overlay);
    		}
    		
            var popupContainer = document.getElementById('popup');
            //create overlayers (popup..)
            var overlay = new ol.Overlay({
	              element: popupContainer,
	              autoPan: true,
	              autoPanAnimation: {
	                duration: 250
	              }
            });
    		$scope.map = new ol.Map({
    		  target:  $scope.ngModel.content.mapId,
    		  layers: [ $scope.baseLayer ],
    		  overlays: [overlay],
    		  view: new ol.View({
    		    center: $scope.ngModel.content.currentView.center,
    		    zoom:  $scope.ngModel.content.currentView.zoom || 3
    		  })
    		});
    		
    		//add events methods
    		$scope.addViewEvents();
    		$scope.addMapEvents(overlay);
    		
    		//just for refresh
    		if (!$scope.map.getSize()){
    			$scope.map.setSize([cockpitModule_widgetConfigurator.map.initialDimension.width, 
    							    cockpitModule_widgetConfigurator.map.initialDimension.height]);
    		}else{
    			$scope.map.setSize($scope.map.getSize());
    		}
			$scope.map.renderSync();
	    }
	    
	    
	    //control panel events
	    $scope.toggleLayer = function(e,n){
	    	e.stopPropagation();
	    	var l = $scope.getLayerByName(n);
	    	if (!l) return; //do nothing
	    	var toggle = !l.getVisible();
	    	l.setVisible(!l.getVisible());
	    }
	    
	    $scope.toggleLayerExpanse = function(layer){
	    	layer.expanded = !layer.expanded;
	    }	

	    $scope.getLayerVisibility = function(n){
	    	var l = $scope.getLayerByName(n);
	    	if (!l) return; //do nothing
	    	return l.getVisible();
	    }
	    
	    $scope.getIndicatorVisibility = function(l,n){
	    	for (lpos in  $scope.ngModel.content.layers){
	    		if ( $scope.ngModel.content.layers[lpos].name == l)
		    	for (var i in $scope.ngModel.content.layers[lpos].indicators){
//		    		if ($scope.ngModel.content.targetLayersConf[lpos].indicators[i].name == n){
		    		if ($scope.ngModel.content.layers[lpos].indicators[i].label == n){
		    			return $scope.ngModel.content.layers[lpos].indicators[i].showMap || false;
		    		}
		    	}
	    	}
	    	return false;
	    }

	    //Thematization 
	    $scope.thematizeMeasure = function (l, m){
	    	var layer = $scope.getLayerByName(l);
	    	var layerValues = $scope.getValuesLayer(l).values;
	    	var layerKeyColumn =  $scope.getLayerProperty(l, 'geoColumn');
	    	var layerConfig;
	    	for (var c=0; c<$scope.ngModel.content.layers.length;c++){
	    		if ($scope.ngModel.content.layers[c].name === l){
	    			layerConfig =$scope.ngModel.content.layers[c];
	    			break;
	    		}
	    	}
	    	$scope.refreshStyle(layer, m, layerConfig, layerValues, layerKeyColumn);
	    }
	    
	  //thematizer functions
	    $scope.refreshStyle = function (layer, measure, config, values, geoColumn){
			//prepare object for thematization
	    	cockpitModule_mapServices.loadIndicatorMaxMinVal(measure, values);
			var newSource = $scope.getFeaturesDetails(geoColumn, measure, config, values);
			layer.setSource(newSource);
			layer.getSource().refresh({force:true});
		}

	   
	    //Utility functions
	    $scope.getLayerByName = function(n){
	    	for (l in $scope.layers){
	    		if ($scope.layers[l].name === n)
	    			return $scope.layers[l].layer;
	    	}
	    	return null;
	    }
	    
	    $scope.addLayer = function(n,l){
	    	$scope.layers.push({"name": n,"layer":l});
	    }
	    
	    $scope.removeLayers = function(){
	    	$scope.layers = [];
	    	$scope.values = [];
			$scope.configs = [];
	    }
	    
	    $scope.setLayerProperty = function(l, p, v){
	    	for (o in $scope.layers){
	    		if ($scope.layers[o].name === l)
	    			$scope.layers[o][p] = v;
	    	}
	    }  

	    $scope.setConfigLayer = function(n,c){
	    	$scope.configs.push({"name": n,"config":c});
	    }
	    
	    $scope.getConfigLayer = function(n){
	    	for (l in $scope.configs){
	    		if ($scope.configs[l].name === n)
	    			return $scope.configs[l].config;
	    	}

	    	return null;
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
	    
	    //functions calls
		$scope.getLayers();
	}

	// this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("map",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});
})();