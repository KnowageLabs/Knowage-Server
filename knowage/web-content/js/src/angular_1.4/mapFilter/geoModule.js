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


var geoM = angular.module('geoModule', [ 'ngMaterial', 'sbiModule' ]);

geoM.factory('geoModule_dataset', function() {
	var ds = {};
	return ds;
});

geoM.factory('geoModule_templateLayerData', function() {
	var tld = {
		data : {}
	};
	return tld;
});

geoM.factory('$map', function() {
	var map = new ol.Map({
		target : 'map',
		layers : [],
		controls : [ new ol.control.Zoom() ],
		view : new ol.View({
//			center : [ -11545862.460039, 5598265.489834 ],
			center : [ 0, 5598265.489834 ],
//			zoom : 5
			zoom : 2
		})
	});
	return map;
});

geoM.service('geoModule_layerServices', function($http, $map, geo_interaction, 
		sbiModule_restServices, geoModule_templateLayerData, sbiModule_logger, $q) {

	var layerServ = this;
	var sFeatures;
	
	var currentInteraction = {
		"type" : null,
		"obj" : null
	};
	
	this.selectStyle = new ol.style.Style({
		stroke : new ol.style.Stroke({
			color : [ 0, 0, 0, 1 ],
			width : 2
		}),
		fill : new ol.style.Fill({
			color : "rgba(174, 206, 230, 0.5)"
		})
	});
	
	layerServ.featuresCensus = {};
	
	layerServ.selectInteraction; // the selected base layer
	layerServ.templateLayer = {};
	layerServ.selectedFeatures = [];
	layerServ.filters=[];

	this.initLayerAndSelection = function(layerToAdd, layerProperty, selectedPropDataAsArray, multivalueFlag) {
		$map.addLayer(layerToAdd);
		$map.updateSize();
		$map.render();
		
		this.templateLayer = layerToAdd;
		
		this.selectInteraction = this.addClickEvent(multivalueFlag);
		
		layerServ.initLayerProperty = layerProperty;
		layerServ.initSelectedPropDataAsArray = selectedPropDataAsArray;
	};
	
	this.initializeFeatureSelection = function(select, layerProperty, selectedPropVauesAsArray, feature) {
		var selectedFeatures = select.getFeatures(); //initially empty
		
		var featureProperties = feature.getProperties();
		var featureProperty = featureProperties[layerProperty];
		
		for(var j = 0; j < selectedPropVauesAsArray.length;  j++) {
			var selectedPropertyValue = selectedPropVauesAsArray[j];
			
			if(selectedPropertyValue == featureProperty) {
				selectedFeatures.push(feature);
				
				// for keeping up the count in the left panel
				geo_interaction.setSelectedFeatures( selectedFeatures.getArray() );
				break;
			}
		}
		
	};
	
	this.addClickEvent = function(multivalueFlag) {
		var selectStyle = this.selectStyle;

		var conditionFunction = multivalueFlag?
				ol.events.condition.click :
				function(mapBrowserEvent) {
				    return ol.events.condition.click(mapBrowserEvent) &&
				        ol.events.condition.noModifierKeys(mapBrowserEvent);
			  	}
		;
		
		var select = new ol.interaction.Select({
			condition : conditionFunction,
			style : selectStyle,
		});
		
		$map.addInteraction(select);
		select.on('select',	function(evt) {
			// if is a WMS i must load the
			// properties from server
			if (geoModule_templateLayerData.data.type == "WMS") {
				var urlInfo = layerServ.templateLayer.getSource().getGetFeatureInfoUrl(
						evt.mapBrowserEvent.coordinate,
						$map.getView().getResolution(),
						'EPSG:3857', 
						{'INFO_FORMAT' : 'application/json'}
				);
				
				$http.get(urlInfo).success(function(data,status,headers,config) {
					sbiModule_logger.log("getGetFeatureInfoUrl caricati",data);
					if (data.hasOwnProperty("errors")) {
						sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti",data.errors);
					} else {
						layerServ.selectedFeatures = data.features;
						geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
					}
				})
				.error(function(data,status,headers,config) {
					sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti ",status);
				});

			} else {

				layerServ.selectedFeatures = evt.target.getFeatures().getArray();
				geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
			}
		});
		
		if(multivalueFlag) {
			var sFeatures = select.getFeatures();
			var dragBox = new ol.interaction.DragBox({
				condition : ol.events.condition.platformModifierKeyOnly,
				style : selectStyle
			});
			
			currentInteraction.obj = dragBox;
			$map.addInteraction(dragBox);

			dragBox.on('boxend',function(e) {
				var selection = [];
				var extent = dragBox.getGeometry().getExtent();

				var vectorSource = layerServ.templateLayer.getSource();
				vectorSource.forEachFeatureIntersectingExtent(extent,function(feature) {
					sFeatures.push(feature);
					selection.push(feature);
				});

				layerServ.selectedFeatures = selection;
				geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);

			});

			// clear selection when drawing a new box and when clicking on the map
			dragBox.on('boxstart', function(e) {
				sFeatures.clear();
			});
		}
		
		return select;
	};

	this.createLayer = function(layerConf, isBase) {
		var tmpLayer;
		var asyncCall;

		switch (layerConf.type) {
		case 'WMS':
			tmpLayer = new ol.layer.Tile({
				source : new ol.source.TileWMS({
					url : layerConf.layerURL,
					params : JSON.parse(layerConf.layerParams),
					options : JSON.parse(layerConf.layerOptions)
				})
			});
			break;

		case 'WFS':
			var vectorSource = new ol.source.Vector({
				url : layerConf.layerURL,
				format : new ol.format.GeoJSON(),
				options : JSON.parse(layerConf.layerOptions)
			});

			tmpLayer = new ol.layer.Vector({
				source : vectorSource,
				style: layerServ.applyFilter
			});
			break;

		case 'TMS': // TODO check if work
			var options = (layerConf.layerOptions instanceof Object) ? layerConf.layerOptions : JSON.parse(layerConf.layerOptions);
			tmpLayer = new ol.layer.Tile({
				source : new ol.source.XYZ({
					tileUrlFunction : function(coordinate) {
						if (coordinate == null) {
							return "";
						}
						var z = coordinate[0];
						var x = coordinate[1];
						// var y = (1 << z) -coordinate[2] - 1;
						var y = -coordinate[2] - 1;
						return layerConf.layerURL + '' + z + '/' + x + '/' + y + '.' + options.type;
					},
				})
			});
			break;

		case 'OSM':
			tmpLayer = new ol.layer.Tile({
				source : new ol.source.MapQuest({
					layer : 'osm'
				})
			});
			break;

		case 'File':
//			tmpLayer= this.getLayerFromFile(layerConf);
			
			var deferredLayer = $q.defer();

			sbiModule_restServices.post("1.0/geo", 'getFileLayer', { layerUrl:layerConf.pathFile })
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					sbiModule_logger.log("file layer non Ottenuto");
				} else {
					sbiModule_logger.trace("file layer caricato",data);

					var vectorSource = new ol.source.Vector({
						features : (new ol.format.GeoJSON()).readFeatures(data,	{
							featureProjection : 'EPSG:3857'
						})
					});

					var tmpLayerVector= new ol.layer.Vector({
						source : vectorSource,
						style: layerServ.applyFilter
					}); 
					
					tmpLayerVector.setZIndex(0);
					
					deferredLayer.resolve(tmpLayerVector);
				}
			})
			.error(function(data, status, headers, config) {
				sbiModule_logger.log("file layer non Ottenuto");
			});

			tmpLayer = deferredLayer.promise;

			break;

		default:
			console.error('Layer type [' + layerConf.type + '] not supported');
		break;

		}
		if (isBase) {
			tmpLayer.setZIndex(-1)
		} else {

		}

		return tmpLayer;
	};

	this.getLayerFromFile=function(layerConf){
		var deferredLayer = $q.defer();

		sbiModule_restServices.post("1.0/geo", 'getFileLayer',{layerUrl:layerConf.pathFile})
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_logger.log("file layer non Ottenuto");
			} else {
				sbiModule_logger.trace("file layer caricato",data);

				var vectorSource = new ol.source.Vector({
					features : (new ol.format.GeoJSON()).readFeatures(data,	{
						featureProjection : 'EPSG:3857'
					})
				});

				var tmpLayer= new ol.layer.Vector({
					source : vectorSource,
					style: layerServ.applyFilter
				}); 
				deferredLayer.resolve(tmpLayer);
			}
		})
		.error(function(data, status, headers, config) {
			sbiModule_logger.log("file layer non Ottenuto");
		});

		return deferredLayer.promise;
	};

	this.applyFilter = function(feature, resolution){
		var styleTMP= [new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: "#3399cc",
				width: 1
			})
		})];
		var applFilter=false;
		var propertiesFeature = feature.getProperties();

		for(var j = 0; j < layerServ.filters.length; j++){
			var value = propertiesFeature[layerServ.filters[j].filter];
			if(layerServ.filters[j].model!=""){
				applFilter=true;
			}
			var valuesInsert = layerServ.filters[j].model.split(",");

			for(var k=0;k<valuesInsert.length;k++){
				if(value==valuesInsert[k]){
					//se contiene il filtro selezionato 
					return styleTMP;
				}
			}
		}
		
		//Census of all features
		if( !layerServ.featuresCensus[feature.getId()]) {
			layerServ.initializeFeatureSelection(
					layerServ.selectInteraction, 
					layerServ.initLayerProperty, 
					layerServ.initSelectedPropDataAsArray, 
					feature);
			
			layerServ.featuresCensus[feature.getId()] = feature;
		}
		
		if(applFilter){
			return null;
		}else{
			return styleTMP;
		}
	};

	this.setLayerFilter= function(layerConf,filters) {
		layerServ.filters=filters;
	};
});

geoM.factory('geoModule_constant', function(sbiModule_translate) {
	var cont = {
		analysisLayer : sbiModule_translate.load("gisengine.constant.analysisLayer"),
		templateLayer : sbiModule_translate.load("gisengine.constant.templateLayer"),
		noCategory : sbiModule_translate.load("gisengine.constant.noCategory"),
		defaultBaseLayer : sbiModule_translate.load("gisengine.constant.defaultBaseLayer")
	};
	return cont;
});
