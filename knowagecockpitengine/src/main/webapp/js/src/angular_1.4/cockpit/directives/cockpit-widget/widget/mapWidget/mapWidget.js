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
			$interval,
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

	    $scope.refresh = function(element,width,height, data, nature, associativeSelection, changedChartType, chartConf, options) {
    		var dsLabel = (Array.isArray(options.label)) ? options.label[0] : options.label; //on delete of selections options is an array !!!
    		$scope.createLayerWithData(dsLabel, data, false); 
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
	    	}	    	
	    	
	    	//set default indicator (first one) for each layer
	    	for (l in $scope.ngModel.content.layers){
	    		var columns = $scope.getColumnSelectedOfDataset($scope.ngModel.content.layers[l].dsId);
	    		for ( c in columns){
	    			if (columns[c].properties.showMap){
	    				$scope.ngModel.content.layers[l].defaultIndicator = columns[c].name;	
	    				break;
	    			}
	    		}
	    	}	
	    }
	    
	    $scope.createLayerWithData = function(label, data, isCluster){
	    	//prepare object with metadata for desiderata dataset columns
	    	var geoColumn = null;
	    	var selectedMeasure = null;
    		var columnsForData = [];
    		var layerDef =  $scope.getConfigLayer(label);
    		var columnsForData = $scope.getColumnSelectedOfDataset(layerDef.dsId) || [];
    		var isHeatmap = (layerDef.heatmapConf && layerDef.heatmapConf.enabled) ? true : false;
    		
    		//remove old layer
    		var previousLayer = $scope.getLayerByName(label);
    		if (previousLayer) $scope.map.removeLayer(previousLayer); //ol obj
    		$scope.removeLayer(label);
    		
    		for (f in columnsForData){
    			var tmpField = columnsForData[f];
    			if (tmpField.fieldType == "SPATIAL_ATTRIBUTE")
    				geoColumn = tmpField.name;
    		}  
    		(function() {
	angular
	.module("cockpitModule")
	.service("cockpitModule_mapServices",CockpitModuleMapServiceController)
		function CockpitModuleMapServiceController(
				sbiModule_translate,
				sbiModule_restServices,
				cockpitModule_template,
				$q, 
				$mdPanel,
				cockpitModule_widgetSelection,
				cockpitModule_properties,
				cockpitModule_utilstServices, 
				$rootScope,
				$location){
	
		var ms = this; //mapServices
		var activeInd;
		var activeConf;
		var cacheProportionalSymbolMinMax;
		
		ms.getFeaturesDetails = function(geoColumn, selectedMeasure, config, configColumns, values){
			if (values != undefined){
				var geoFieldName;
				var geoFieldValue;			
				var	featuresSource = new ol.source.Vector();
				 
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
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (lonlat.length != 2){
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (!selectedMeasure) selectedMeasure = config.defaultIndicator;
//						if (config.analysisConf && config.analysisConf.defaultAnalysis == 'proportionalSymbol'){							
							//get config for thematize
							if (selectedMeasure){
								if (!ms.getCacheProportionalSymbolMinMax()) ms.setCacheProportionalSymbolMinMax({}); //just at beginning
								if (!ms.getCacheProportionalSymbolMinMax().hasOwnProperty(config.name+"|"+selectedMeasure)){
									ms.loadIndicatorMaxMinVal(config.name+"|"+ selectedMeasure, values);
								}
							}
//						}
						
						//set ol objects
						var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
						var feature = new ol.Feature();  
				        var coordinate = transform([parseFloat(lonlat[0].trim()), parseFloat(lonlat[1].trim())]);
				        var geometry = new ol.geom.Point(coordinate);
				        feature.setGeometry(geometry);
				        ms.addDsPropertiesToFeature(feature, row, configColumns, values.metaData.fields);
				       //at least add the layer owner
				        feature.set("parentLayer",config.name);
				        feature.set("sourceType",  (config.markerConf && config.markerConf.type ) ?  config.markerConf.type : "simple");
				        featuresSource.addFeature(feature);
					}
					
					return featuresSource;
				}
			}
			return new ol.source.Vector();
		}
	    
		ms.addDsPropertiesToFeature = function (f, row, cols, meta){
			//add columns value like properties
			for (c in row){
				var header = ms.getHeaderByColumnName(c, meta);
				var prop = {};
				prop.value = row[c];
				for (p in cols){
					if (cols[p].alias == header){
						prop.type = cols[p].fieldType;
						prop.aggregationSelected = ( cols[p].properties && cols[p].properties.aggregationSelected) ? cols[p].properties.aggregationSelected : '';
						break;
					}
				}
				f.set(header, prop);
			}
		}
		
		ms.getHeaderByColumnName = function(cn, fields) {
			var toReturn = cn;
			
			for (n in fields){
				if (fields[n] && fields[n].name === cn){
					return fields[n].header;
				}
			}
			return toReturn;
		}
		
		ms.setHeatmapWeight= function(feature){
			var parentLayer = feature.get('parentLayer');
			var config = ms.getActiveConf(parentLayer) || {};
			
			var minmaxLabel = parentLayer + '|' + config.defaultIndicator;
			var minmax = ms.getCacheProportionalSymbolMinMax()[minmaxLabel];
			var props  = feature.getProperties();

		    var p = feature.get(config.defaultIndicator);
		    // perform some calculation to get weight between 0 - 1
		    // apply formule: w = w-min/max-min (http://www.statisticshowto.com/normalized/)
		    weight = (p.value - minmax.minValue)/(minmax.maxValue-minmax.minValue);
		    return weight;
		}
		
	    var styleCache = {};
	    ms.layerStyle = function(feature, resolution){
	    	
	    	var localFeature;
			if (Array.isArray(feature.get('features')))
				localFeature = feature.get('features')[0];
	    	else
	    		localFeature = feature;
	    	    	
			var props  = localFeature.getProperties();
			var parentLayer = localFeature.get('parentLayer');
			var config = ms.getActiveConf(parentLayer) || {};
			var configThematizer = config.analysisConf || {};
			var configMarker = config.markerConf || {};
			var configCluster = config.clusterConf || {};
			var useCache = false; //cache isn't use for analysis, just with fixed marker
			var isCluster = (Array.isArray(feature.get('features'))) ? true : false;
			var value;
			var style;
			
			ms.setActiveIndicator(config.defaultIndicator);

			if (isCluster){
				value = ms.getClusteredValue(feature);
			}else{
				value =  props[ms.getActiveIndicator()] || 0;
			}
			
			var thematized = false;
			if (configThematizer.defaultAnalysis == 'choropleth') {
				style = ms.getChoroplethStyles(value, props, configThematizer.choropleth, configMarker);
				thematized = true;
			}else if (configThematizer.defaultAnalysis == 'proportionalSymbol') {
				style = ms.getProportionalSymbolStyles(value, props, configThematizer.proportionalSymbol);
				thematized = true;
			}
			if (!thematized && isCluster && feature.get('features').length > 1 ){
				style = ms.getClusterStyles(value, props, configCluster);
				useCache = false;
			}
			else{
				style = ms.getOnlyMarkerStyles(props, configMarker);
				useCache = true;
			}
			
			if (useCache && !styleCache[parentLayer]) {
		          styleCache[parentLayer] = style;
		          return styleCache[parentLayer] ;
			} else {
				return style;
			}
	    }
	    
	    ms.getClusteredValue = function (feature) { 
	    	var toReturn = 0;
	    	var total = 0;
	    	var values = [];
	    	var aggregationFunc = "";
	    	
	    	if (Array.isArray(feature.get('features'))){
	    		total = 0;
	    		for (var i=0; i<feature.get('features').length; i++){
					var tmpValue = Number((feature.get('features')[i].get(ms.getActiveIndicator())) ? feature.get('features')[i].get(ms.getActiveIndicator()).value : 0);
					aggregationFunc = (feature.get('features')[i].get(ms.getActiveIndicator())) ? feature.get('features')[i].get(ms.getActiveIndicator()).aggregationSelected : "SUM";
					values.push(tmpValue);
					total = total + tmpValue;
				}
				
	    		switch(aggregationFunc) {
				    case "MIN": 
				    	toReturn = Math.min.apply(null, values);
				        break;
				    case "MAX":
				    	toReturn = Math.max.apply(null, values);
				    	break;
				    case "SUM": 
				    	toReturn = total;
				    	break;
				    case  "AVG":
				    	if (total > 0)
				    		toReturn = (total/feature.get('features').length);
				    		break;
				    case "COUNT":
				    	toReturn = feature.get('features').length;
				    	break;
				    default: //SUM
				    	toReturn = total;
	    		}			
	    	}
	    	else{
	    		toReturn += feature.get(ms.getActiveIndicator());
	    	}
			return toReturn;
	    }
	    
	    ms.getClusterStyles = function (value, props, config){
	      return new ol.style.Style({
	              image: new ol.style.Circle({
	                radius: config.radiusSize || 20,
	                stroke: new ol.style.Stroke({
	                  color: '#fff'
	                }),
	                fill: new ol.style.Fill({
	                  color: (config.style && config.style['background-color']) ? config.style['background-color'] : 'blue'
	                })
	              }),
	              text: new ol.style.Text({
	            	font: (config.style && config.style['font-size']) ? config.style['font-size'] : '12px',
	                text: value.toString(),
	                fill: new ol.style.Fill({
	                  color: (config.style && config.style['color']) ? config.style['color'] : '#fff'
	                })
	              })
	            });
	    }
		
		ms.getProportionalSymbolStyles = function(value, props, config){
			return new ol.style.Style({
		          fill: new ol.style.Fill({
		                color :  config.color
		              }),
		              stroke: new ol.style.Stroke({
		                color: '#ffcc33',
		                width: 2
		              }),
		              image: new ol.style.Circle({
		                radius: ms.getProportionalSymbolSize(value, ms.getActiveIndicator(), config),
		                fill: new ol.style.Fill({
		                 color : config.style['color'] || 'blue',
		                })
		              }),
		              text: new ol.style.Text({
		                  font: config['font-size'] || '12px Calibri,sans-serif',
		                  fill: new ol.style.Fill({ color: '#000' }),
		                  stroke: new ol.style.Stroke({
		                    color: '#fff', width: 2
		                  }),
		                  text: value.toString()
		                })
		            });
		}
		
		ms.getChoroplethStyles = function(value, props, config){
			var textValue =  props[ms.getActiveIndicator()] || "";
			
			return  [new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: borderColor,
					width: 1
				}),
				fill: new ol.style.Fill({
					color: getChoroplethColor(dsValue,layerCol).color
				}),
				image: new ol.style.Circle({
		  			radius: 5,
		  			stroke: new ol.style.Stroke({
						color: borderColor,
						width: 1
					}),
		  			fill: new ol.style.Fill({
		  				color: getChoroplethColor(dsValue,layerCol).color
		  			})
		  		})
			})];
			
			return new ol.style.Style({
			});
		}
		
		ms.getOnlyMarkerStyles = function (props, config){
			var style;
			var textValue =  props[ms.getActiveIndicator()] || "";
			
			switch(config.type) {
			
			case "icon":
				//font-awesome
				style = new ol.style.Style({
					  text: new ol.style.Text({
						  	text: config.icon.unicode, 
						    font: 'normal ' + (config.size + '%' || '100%') + config.icon.family,
						    fill: new ol.style.Fill({
						    	 color: (config.style && config.style.color) ? config.style.color : 'blue'
						    })
						  })
					});
				break;
				
			case "url": case 'img': 
				//img (upload)
				style =  new ol.style.Style({
				image: new ol.style.Icon(
						/** @type {olx.style.IconOptions} */
					({
						stroke: new ol.style.Stroke({ //border doesn't work
						color: 'red',
						width: 10
					}),
					scale: (config.scale) ? (config.scale/100) : 1,
				    opacity: 1,
				    crossOrigin: 'anonymous',
				    src: config[config.type]
					}))
		          });
				break;
				
			default:
				style =  new ol.style.Style({
				image: new ol.style.Icon(
						/** @type {olx.style.IconOptions} */
					({
						stroke: new ol.style.Stroke({ //border doesn't work
						color: 'red',
						width: 3
					}),
				    opacity: 1,
				    crossOrigin: 'anonymous',
				    color: (config.style && config.style.color) ? config.style.color : 'blue',
				    src:  $location.$$absUrl.substring(0,$location.$$absUrl.indexOf('api/')) + '/img/dot.png'
					}))
		          });
				break;
				
			}
			return style;
		}
	
		ms.getBaseLayer = function (conf){
			
			var toReturn;
			
			//check input configuration
			if (!conf || !conf.type){
				conf = {type:""}; //default case
			}
		
			switch(conf.type) {
			    case "OSM": case "OAM":
			    	toReturn = new ol.layer.Tile({
			   	    	visible: true,
			   	    	source: new ol.source.OSM({
				   	    	url:conf.url,
				   	    	attributions: conf.attributions || "",
			   	    		})
						});
			        break;
			    case "Stamen":
			    	//toner-hybrid, toner, toner-background, toner-hybrid, toner-labels, toner-lines, toner-lite,terrain, terrain-background, terrain-labels, terrain-lines
			    	toReturn = new ol.layer.Tile({
			   	    	visible: true,
			   	    	source: new ol.source.Stamen({
				   	    	layer: conf.layer,
			   	    		})
						});
			        break;
			    case "XYZ": //generic tiles (ex. for carto)
			    	toReturn = new ol.layer.Tile({
			   	    	visible: true,
			   	    	source: new ol.source.XYZ({
				   	    	url:conf.url,
				   	    	attributions: conf.attributions || "",
			   	    		})
						});
			    	break;
			    default: //OSM
			    		toReturn = new ol.layer.Tile({
				    		visible: true,
				      	    source: new ol.source.OSM()
				      	});
			}
	      
			return toReturn;
			
		}	
		
		ms.getProportionalSymbolSize = function(val, name, config){
			if (!name) return 0;
			
			var minValue = ms.cacheProportionalSymbolMinMax[config.name+'|'+name].minValue;
			var maxValue = ms.cacheProportionalSymbolMinMax[config.name+'|'+name].maxValue;
			var size;
			
			var maxRadiusSize = config.maxRadiusSize;
			var minRadiusSize = config.minRadiusSize;

			if(minValue == maxValue) { // we have only one point in the distribution
				size = (maxRadiusSize + minRadiusSize)/2;
			} else {
				size = ( parseInt(val) - minValue) / ( maxValue - minValue) * (maxRadiusSize - minRadiusSize) + minRadiusSize;
			}
			return (size < 0 ) ? 0 : size;
		}
		
		 ms.updateCoordinatesAndZoom = function(model, map, l, setValues){
		    	var coord;
		    	var zoom;
		    	
		    	if (model.content.currentView.center[0] == 0 && model.content.currentView.center[1] == 0){
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
			    	model.content.currentView.center = coord;
			    	model.content.currentView.zoom = zoom;
			    	
			    	if (setValues){
			    		map.getView().setCenter(coord);
			    		map.getView().setZoom(zoom);
			    	} 		
		    	}
		    }
		
		ms.getCacheProportionalSymbolMinMax=function(){
			return ms.cacheProportionalSymbolMinMax;
		}
		
		ms.setCacheProportionalSymbolMinMax=function(c){
			ms.cacheProportionalSymbolMinMax = c;
		}
		
		ms.getActiveIndicator=function(){
			return ms.activeInd;
		}
		
		ms.setActiveIndicator=function(i){
			ms.activeInd = i;
		}
		
		ms.getActiveConf=function(l){
			for (c in ms.activeConf){
				if (ms.activeConf[c].layer === l)
					return ms.activeConf[c].config;
			}
			console.log("Active configuration for layer ["+l+"] not found.");
			return null;
		}
		
		ms.getActiveConfIdx=function(l){
			for (var i=0; i<ms.activeConf.length; i++){
				if (ms.activeConf[i].layer === l)
					return i;
			}
			return null;
		}
		
		ms.setActiveConf=function(l, c){
			if (!ms.activeConf)
				ms.activeConf = [];
			
			var idx = ms.getActiveConfIdx(l);
			if (idx != null){
				ms.activeConf.splice(idx,1);
			} 
			ms.activeConf.push({"layer": l, "config":c});
		}
		
		ms.loadIndicatorMaxMinVal=function(key, values){
			var minV;
			var maxV;
			for(var i=0;i<values.rows.length;i++){
				var colName = ms.getColumnName(key, values.metaData.fields);
				var tmpV= parseInt(values.rows[i][colName]);
				if(minV==undefined || tmpV<minV){
					minV=tmpV;
				}
				if(maxV==undefined || tmpV>maxV){
					maxV=tmpV;

				}
			}
			ms.cacheProportionalSymbolMinMax[key]={minValue:minV, maxValue:maxV};
		}
		
		function getChoroplethColor(val,layerCol){
			var color;
			var alpha;
			for(var i=0;i<tmtz.legendItem.choroplet.length;i++){
				if(parseInt(val)>=parseInt(tmtz.legendItem.choroplet[i].from) && parseInt(val)<parseInt(tmtz.legendItem.choroplet[i].to)){
					color=tmtz.legendItem.choroplet[i].color;
					alpha=tmtz.legendItem.choroplet[i].alpha;
					if(tmtz.legendItem.choroplet[i].itemFeatures.indexOf(layerCol)==-1){
						tmtz.legendItem.choroplet[i].itemFeatures.push(layerCol);
						tmtz.legendItem.choroplet[i].item++;
					}

					break;
				}
			}
			if(color==undefined){
				color=tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].color;
				alpha=tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].alpha;
				if(tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].itemFeatures.indexOf(layerCol)==-1){
					tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].itemFeatures.push(layerCol);
					tmtz.legendItem.choroplet[tmtz.legendItem.choroplet.length-1].item++;
				}
			}
			return {color:color,alpha:alpha};
		}

		ms.getColumnName = function(key, values){
			var toReturn = key.substring(key.indexOf('|')+1);;
			for (var v=0; v<values.length; v++){
				if (values[v].header === toReturn)
					return values[v].name;
			}
				
			return toReturn;
		}
		
		ms.isCluster = function(feature) {
		  if (!feature || !feature.get('features')) { 
		        return false; 
		  }
		  return feature.get('features').length > 1;
		}
	}	

})();
    		var featuresSource = cockpitModule_mapServices.getFeaturesDetails(geoColumn, selectedMeasure, layerDef, columnsForData, data);
			if (featuresSource == null){ 
				return;
			}
			cockpitModule_mapServices.setActiveConf(layerDef.name, layerDef);
			var layer;
			if (isCluster) {
				var clusterSource = new ol.source.Cluster({source: featuresSource	
														  });
				layer =   new ol.layer.Vector({source: clusterSource,
										  	  style: cockpitModule_mapServices.layerStyle
										});
			} else if (isHeatmap) {
				layer = new ol.layer.Heatmap({source: featuresSource,
										      blur: layerDef.blur,
										      radius: layerDef.radius,
										      weight: cockpitModule_mapServices.setHeatmapWeight
										     });
			} else {
				layer = new ol.layer.Vector({source: featuresSource,
	    									 style: cockpitModule_mapServices.layerStyle
	    									});
			}

			//add decoration to layer element			
			layer.name = layerDef.name;
			layer.dsId = layerDef.dsId;
			layer.setZIndex(layerDef.order*1000);
			$scope.map.addLayer(layer); 			//add layer to ol.Map
			$scope.addLayer(layerDef.name, layer);	//add layer to internal object
			$scope.setLayerProperty (layerDef.name, 'geoColumn',geoColumn),
			$scope.setValuesLayer(layerDef.name, data); //add values to internal object
			cockpitModule_mapServices.updateCoordinatesAndZoom($scope.ngModel, $scope.map, layer, true);	
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
        	    if (Number.isInteger(e.target.getZoom())) {
	            	var previousZoom = $scope.ngModel.content.currentView.zoom;
	            	var newZoom =  e.target.getZoom();
	            	if (previousZoom > newZoom ){
	            		for (l in $scope.ngModel.content.layers){
		    		    	var layerDef =  $scope.ngModel.content.layers[l];
		    		    	var isCluster = (layerDef.clusterConf && layerDef.clusterConf.enabled) ? true : false;
			    			if (isCluster){
			    				var data = $scope.getValuesLayer(layerDef.name);
				        		$scope.createLayerWithData(layerDef.name, data.values, true); //return to cluster view
			    			}
	            		}
	            	}
        	    }
            	
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
            	                return feature;
            	            });
            	var layer = $scope.map.forEachFeatureAtPixel(evt.pixel,
        	            function(feature, layer) {

        	                return layer;
        	            });
            	//popup isn't shown with cluster
    	        if (feature) {
    	        	var tempFeature = (Array.isArray(feature.get('features')) && feature.get('features').length == 1) ? feature.get('features')[0] : feature;
    	        	
    	            var geometry = tempFeature.getGeometry();
    	            var props = tempFeature.getProperties();
    	            var coordinate = geometry.getCoordinates();
    	            var config = $scope.getColumnSelectedOfDataset(layer.dsId);
    	            var text = "";
    	            for (var p in props){
    	            	var pDett = ($scope.isDisplayableProp(p, config));
    	            	if (pDett != null)
    	            		text += '<b>' + pDett.aliasToShow + ":</b> " + props[p].value + '<br>';
    	            }
    	            if (text != ""){
	    		        popupContent.innerHTML = '<h2>Details</h2><code>' + text + '</code>';
	    		        overlay.setPosition(coordinate);
    	            }
    	        }
             });
    		

    		$scope.map.on('dblclick', function(evt) {
    			for (l in $scope.ngModel.content.layers){
    		    	var layerDef =  $scope.ngModel.content.layers[l];
    		    	var isCluster = (layerDef.clusterConf && layerDef.clusterConf.enabled) ? true : false;
	    			if (isCluster){
	    				var data = $scope.getValuesLayer(layerDef.name);
		        		$scope.createLayerWithData(layerDef.name, data.values, false);
	    			}
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
	    		if (p == config[c].name && config[c].properties.showDetails){
	    			return config[c];
	    		}
	    	}
	    	return null;
	    }
		
	    $scope.getFeaturesFromDataset = function(layerDef){
    		//prepare object with metadata for desiderata dataset columns
	    	var geoColumn = null;
    		var selectedMeasure = null;
    		var columnsForData = [];
    		var isCluster = (layerDef.clusterConf && layerDef.clusterConf.enabled) ? true : false;
    		
    		var columnsForData = $scope.getColumnSelectedOfDataset(layerDef.dsId) || [];
	    	
    		for (f in columnsForData){
    			var tmpField = columnsForData[f];
    			if (tmpField.fieldType == "SPATIAL_ATTRIBUTE")
    				geoColumn = tmpField.name;
    			else if (tmpField.properties.showMap) 	//first measure
    				selectedMeasure = tmpField.aliasToShow;
    		}    	
    		
    		var model = {content: {columnSelectedOfDataset: columnsForData }};
    		var features = [];
    		var layer =  new ol.layer.Vector();

    		//get the dataset columns values
	    	cockpitModule_datasetServices.loadDatasetRecordsById(layerDef.dsId, undefined, undefined, undefined, undefined, model).then(
	    		function(allDatasetRecords){
	    			$scope.createLayerWithData(layerDef.name, allDatasetRecords, isCluster);
			},function(error){
				console.log("Error loading dataset with id [ "+layerDef.dsId+"] "); 
				sbiModule_messaging.showInfoMessage($scope.translate.load('sbi.cockpit.map.datasetLoadingError').replace("{0}",layerDef.dsId), 'Title', 3000);
			});	
    	}

	    $scope.createMap = function (){
	    	$scope.initializeTemplate();
	    	
	    	//create the base layer
            $scope.baseLayer = cockpitModule_mapServices.getBaseLayer($scope.ngModel.content.baseLayersConf[0]);

	    	//setting coordinates (from the first layer if they aren't setted into the template)
            if ($scope.ngModel.content.currentView.center[0] == 0 && $scope.ngModel.content.currentView.center[1] == 0 && $scope.layers.length > 0){
	    		var tmpLayer = $scope.layers[0].layer;
	    		cockpitModule_mapServices.updateCoordinatesAndZoom($scope.ngModel, $scope.map, tmpLayer, false);

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
	    	layer.expandedNav = !layer.expandedNav;
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
	    	var layerColumnConfig = $scope.getColumnSelectedOfDataset(layerConfig.dsId) || []; 
	    	$scope.refreshStyle(layer, m, layerConfig, layerColumnConfig, layerValues, layerKeyColumn);
	    }
	    
	  //thematizer functions
	    $scope.refreshStyle = function (layer, measure, config, configColumns, values, geoColumn){
			//prepare object for thematization
	    	cockpitModule_mapServices.loadIndicatorMaxMinVal(config.name+'|'+measure, values);
			var newSource = cockpitModule_mapServices.getFeaturesDetails(geoColumn, measure, config, configColumns,  values);
			if (config.clusterConf && config.clusterConf.enabled){
				var clusterSource = new ol.source.Cluster({ source: newSource });
				layer.setSource(clusterSource);
			}else{
				layer.setSource(newSource);
			}
			
			layer.getSource().refresh({force:true});
		}
//	    
//	    $scope.changeHeatmapValues = function(){  		
//    		for (l in $scope.ngModel.content.layers){
//		    	var layerDef =  $scope.ngModel.content.layers[l];
//				var data = $scope.getValuesLayer(layerDef.name);
//        		$scope.createLayerWithData(layerDef.name, data.values, false); //return to cluster view
//    		}
//    		
//	    }
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
	    
	    $scope.removeLayer = function(n){
	    	for (l in $scope.layers){
	    		if ($scope.layers[l].name == n)
	    			$scope.layers.splice(l,1);
	    	}
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