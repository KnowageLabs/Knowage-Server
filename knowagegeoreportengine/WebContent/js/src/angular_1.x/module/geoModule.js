var geoM = angular.module('geoModule', [ 'ngMaterial', 'ngAnimate',
                                         'angular_table', 'sbiModule', 'mdColorPicker', "expander-box" ]);

geoM.factory('geoModule_dataset', function() {
	var ds = {};
	return ds;
});

geoM.factory('geModule_datasetJoinColumnsItem', function() {
	var dsjc = {};
	return dsjc;
});

geoM.factory('geoModule_indicators', function() {
	var gi = [];
	return gi;
});

geoM.factory('geoModule_filters', function() {
	var gi = [];
	return gi;
});

geoM.factory('geoModule_templateLayerData', function() {
	var tld = {};
	return tld;
});

geoM.factory('$map', function() {
	var map = new ol.Map({
		target : 'map',
		layers : [],
		controls : [ new ol.control.Zoom() ],
		view : new ol.View({
			center : [ 1545862.460039, 4598265.489834 ],
			zoom : 5
		})
	});
	return map;
});

geoM.factory('baseLayer', function(geoModule_constant) {
	// todo thr following configuration should be loaded from a rest service
	// (from LayerCatalogue)
	var baseLayersConf = {};
	baseLayersConf[geoModule_constant.defaultBaseLayer] = {
			"OpenStreetMap" : {
				"type" : "TMS",
				"category" : "Default",
				"label" : "OpenStreetMap",
				"layerURL" : "http://tile.openstreetmap.org/",
				"layerOptions" : {
					"type" : "png",
					"displayOutsideMaxExtent" : true
				}
			},
			"OSM" : {
				"type" : "OSM",
				"category" : "Default",
				"label" : "OSM"
			}
	};

	return baseLayersConf;
});

geoM.service(
		'geoModule_layerServices',
		function($http, baseLayer, sbiModule_logger, $map,
				geoModule_thematizer, geo_interaction, crossNavigation,
				sbiModule_config, geoModule_template,
				sbiModule_restServices, geoModule_templateLayerData,$q) {

			var layerServ = this;
			var sFeatures;
			var select;
			var currentInteraction = {
					"type" : null,
					"obj" : null
			};
			this.selectedBaseLayer; // the selected base layer
			this.selectedBaseLayerOBJ;
			this.loadedLayer = {};
			this.loadedLayerOBJ = {};
			this.templateLayer = {};
			this.selectedFeatures = [];
			this.measure;
			// this.cachedFeatureStyles = {};

			this.setTemplateLayer = function(data) {
				Object.assign(geoModule_templateLayerData, data);
				geoModule_thematizer.updateLegend(geoModule_template.analysisType);
				if (geoModule_templateLayerData.type == "WMS") {
					var sldBody = geoModule_thematizer
					.getWMSSlBody(geoModule_templateLayerData);
					console.log(sldBody)
					var params = JSON
					.parse(geoModule_templateLayerData.layerParams);
					params.LAYERS = geoModule_templateLayerData.layerName;
					// var params={};
					params.SLD_BODY = sldBody;

					layerServ.templateLayer = new ol.layer.Tile(
							{
								source : new ol.source.TileWMS(
										/** @type {olx.source.TileWMSOptions} */
										{
											url : sbiModule_config.contextName
											+ "/api/1.0/geo/getWMSlayer?layerURL="
											+ geoModule_templateLayerData.layerURL,
											params : params,
											options : JSON
											.parse(geoModule_templateLayerData.layerOptions)
										})
							});

				} else {
					var vectorSource = new ol.source.Vector(
							{
								features : (new ol.format.GeoJSON()).readFeatures(geoModule_templateLayerData,
										{
											// dataProjection: 'EPSG:4326',
											featureProjection : 'EPSG:3857'
										})
							});

					layerServ.templateLayer = new ol.layer.Vector({
						source : vectorSource,
						style : geoModule_thematizer.getStyle
					});
				}

				layerServ.templateLayer.setZIndex(0);
				$map.addLayer(layerServ.templateLayer);
				var duration = 2000;
				var start = +new Date();
				var pan = ol.animation.pan({
					duration : duration,
					source : /** @type {ol.Coordinate} */($map.getView().getCenter())
				});
				var bounce = ol.animation.bounce({
					duration : duration,
					resolution : 4 * $map.getView().getResolution()
				});

				$map.beforeRender(pan, bounce);

				$map.getView().setCenter(geoModule_template.currentView.center);
				$map.getView().setZoom(geoModule_template.currentView.zoom);

				// $map.getView().fit(layerServ.templateLayer.getProperties().source.getExtent(),$map.getSize());

				layerServ.addClickEvent();
			};

			this.overlay;

			this.addClickEvent = function() {
				var selectStyle = new ol.style.Style({
					stroke : new ol.style.Stroke({
						// color: '#000000',
						color : [ 0, 0, 0, 1 ],
						width : 2
					}),
					fill : new ol.style.Fill({
						color : "rgba(174, 206, 230, 0.78)"
					})
				});

				layerServ.overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */
						({
							element : angular.element((document.querySelector('#popup')))[0],
						}));
				$map.addOverlay(layerServ.overlay);

				select = new ol.interaction.Select({
					condition : ol.events.condition.singleClick,
					style : selectStyle
				});
				$map.addInteraction(select);
				// layerServ.setInteraction('box');
				select.on('select',	function(evt) {
					if (geo_interaction.type == "identify"
						&& (evt.selected[0] == undefined || geo_interaction.distance_calculator)
						&& geoModule_templateLayerData.type != "WMS") {
						layerServ.overlay.setPosition(undefined);
						return;
					}

					// if is a WMS i must load the
					// properties from server
					if (geoModule_templateLayerData.type == "WMS") {
						var urlInfo = layerServ.templateLayer.getSource().getGetFeatureInfoUrl(
								evt.mapBrowserEvent.coordinate,
								$map.getView().getResolution(),
								'EPSG:3857',
								{
									'INFO_FORMAT' : 'application/json'
								});
						$http.get(urlInfo).success(function(data,status,headers,config) {
									sbiModule_logger.log("getGetFeatureInfoUrl caricati",data);
									if (data.hasOwnProperty("errors")) {
										sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti",data.errors);
									} else {
										if (data.features.length == 0) {
											layerServ.overlay.setPosition(undefined);
										} else {
											layerServ.doClickAction(evt,data.features[0].properties)
										}
									}
								})
								.error(function(data,status,headers,config) {
											sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti ",status);
										});

					} else {
						var prop = evt.selected.length ? evt.selected[0].getProperties(): null;
						layerServ.doClickAction(evt,prop)
					}
				});
			}

			this.intersectFeature = function() {
				// select fetures with a box
				var sFeatures = select.getFeatures();
				var selectStyle = new ol.style.Style({
					stroke : new ol.style.Stroke({
						color : [ 0, 0, 0, 1 ],
						width : 2
					}),
					fill : new ol.style.Fill({
						color : "rgba(174, 206, 230, 0.78)"
					})
				});
				var dragBox = new ol.interaction.DragBox(
						{
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

							console.log("layerServ.selectedFeatures (dragBox)-> ",layerServ.selectedFeatures);
							console.log("geo_interaction.selectedFilterType -> ",geo_interaction.selectedFilterType);
						});

				// clear selection when drawing a new box and when
				// clicking on the map
				dragBox.on('boxstart', function(e) {
					layerServ.overlay.setPosition(undefined);
					sFeatures.clear();
				});

			}

			this.insideFeature = function() {
				var sFeatures = select.getFeatures();
				var selectStyle = new ol.style.Style({
					stroke : new ol.style.Stroke({
						color : [ 0, 0, 0, 1 ],
						width : 2
					}),
					fill : new ol.style.Fill({
						color : "rgba(174, 206, 230, 0.78)"
					})
				});
				var dragBox = new ol.interaction.DragBox(
						{
							condition : ol.events.condition.platformModifierKeyOnly,
							style : selectStyle
						});
				currentInteraction.obj = dragBox;
				$map.addInteraction(dragBox);

				dragBox.on('boxend',function(e) {
					var selection = [];
					var extent = dragBox.getGeometry()
					.getExtent();

					var vectorSource = layerServ.templateLayer.getSource();
					vectorSource.forEachFeatureIntersectingExtent(extent,function(feature) {
						var geom = feature.getGeometry().A;
						if (geom[0] > extent[0]) {
							if (geom[1] > extent[1]) {
								if (geom[2] < extent[2]) {
									if (geom[3] < extent[3]) {
									// it is inside the polygon
										sFeatures.push(feature);
										selection.push(feature);
									}
								}
							}
						}

					});

					layerServ.selectedFeatures = selection;
					geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);

					
					console.log("layerServ.selectedFeatures (dragBox)-> ",layerServ.selectedFeatures);
					console.log("geo_interaction.selectedFilterType -> ",geo_interaction.selectedFilterType);

				});
				// clear selection when drawing a new box and when
				// clicking on the map
				dragBox.on('boxstart', function(e) {
					layerServ.overlay.setPosition(undefined);
					sFeatures.clear();
				});

			}
			this.spy = function() {
				var element;
				var key = 'Ak-dzM4wZjSqTlzveKz5u0d4IQ4bRzVI309GxmkgSVr1ewS6iPSrOvOKhA-CJlm3';
				var sFeatures = select.getFeatures();
				var imagery = new ol.layer.Tile({
					source : new ol.source.BingMaps({
						key : key,
						imagerySet : 'Aerial'
					}),
					opacity : 0.001
				});

				var container = document.getElementById('map');
				var coordinate;
				$map.addLayer(imagery);
				
				var radius ;
				var ray ;
				
				document.addEventListener('keydown', function(evt) {

					if(evt.which === 17 && geo_interaction.selectedFilterType == "near"){
						//se premo control seleziono le features
						sFeatures.clear();
						layerServ.near($map.getCoordinateFromPixel(coordinate), ray);
						$map.removeLayer(imagery);
					}
				})
			
				// get the pixel position with every move
				var mousePosition = null;

				container.addEventListener('mousemove',function(event) {
					
					mousePosition = $map.getEventPixel(event);
					$map.render();
				});

				container.addEventListener('mouseout', function() {
					mousePosition = null;
					$map.removeLayer(imagery);
					$map.render();
				});

				// before rendering the layer, do some clipping
				imagery.on('precompose', function(event) {
					
					var ctx = event.context;
					var pixelRatio = event.frameState.pixelRatio;
					ctx.save();
					ctx.beginPath();
					radius = layerServ.calculateRay(layerServ.measure);
					ray = radius;
					if (mousePosition) {
						// only show a circle around the mouse 
					
						ctx.arc(mousePosition[0] * pixelRatio,mousePosition[1] * pixelRatio, radius *  pixelRatio, 0, 2 * Math.PI);
						coordinate = [mousePosition[0], mousePosition[1]];
						var endCoordinate = [coordinate[0]+radius*  pixelRatio,coordinate[1]+radius*  pixelRatio];
						//ray = radius *  pixelRatio;
						ray = layerServ.calculateDistance($map.getCoordinateFromPixel(coordinate),$map.getCoordinateFromPixel(endCoordinate));
						
						console.log("ray"+ray);
						ctx.lineWidth = 5 * pixelRatio;
						ctx.strokeStyle = 'rgba(0,0,0,0.5)';
						ctx.stroke();
					}
					ctx.clip();
				});

				// after rendering the layer, restore the canvas context
				imagery.on('postcompose', function(event) {
					var ctx = event.context;
					ctx.restore();
					
				});


			}
			this.near = function(coordinate, ray) {
				console.log("near");
				console.log("ray :" + ray);
				var circleGeometry;
				var myCircle;
				var sFeatures = select.getFeatures();

				
				var features = layerServ.templateLayer.getSource().getFeatures();
				if (features.length > 0) {
					var selection = [];
					features.forEach(function(feature) {
		
						var geom = feature.getGeometry().getCoordinates();
						for (var i = 0; i < geom[0][0].length; i++) {
							var coordFeature = geom[0][0][i];							
							var x=coordFeature[0];
							var y = coordFeature[1] ;

							
							if (layerServ.findIntersect(x, y , coordinate, ray)) {
								// ok
								selection.push(feature);
								sFeatures.push(feature);
								break;
							}
						}

					})
					console.log(selection);

					layerServ.selectedFeatures = selection;
					geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
					
					console.log("layerServ.selectedFeatures (near)-> ",layerServ.selectedFeatures);
					console.log("geo_interaction.selectedFilterType -> ",geo_interaction.selectedFilterType);
				}



			}
			
			
			this.findIntersect = function(x, y, center, ray) {
				//intersect inside a circle
				
				var x = Math.pow(x - center[0], 2);
				var y = Math.pow(y - center[1], 2);
				var difference = x + y;
				if (difference <= Math.pow(ray, 2)) {
					return true;
				} else {
					return false;
				}
			}
			
		


			this.setInteraction = function() {
				var type = geo_interaction.selectedFilterType;

				if (currentInteraction != {} && currentInteraction.type != type) {
					// remove $map
					$map.removeInteraction(currentInteraction.obj);

				}
				// setta il tipo di interazione;

				currentInteraction.type = type;
				if (type == 'near') {
					$map.removeInteraction(currentInteraction.obj);
					layerServ.spy();

				} else if (type == 'intersect') {
					$map.removeInteraction(currentInteraction.obj);
					layerServ.intersectFeature();

				} else if (type == 'inside') {
					$map.removeInteraction(currentInteraction.obj);
					layerServ.insideFeature();
				}
			}
			this.doClickAction = function(evt, prop) {
			
				layerServ.selectedFeatures = evt.target.getFeatures().getArray();
				geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);

				if (geo_interaction.type == "identify") {

					var coordinate = evt.mapBrowserEvent.coordinate;
					var hdms = ol.coordinate.toStringHDMS(ol.proj.transform(coordinate, 'EPSG:3857','EPSG:4326'));

					var txt = "";
					for ( var key in prop) {
						if (key != "geometry") {
							txt += "<p>" + key + " : " + prop[key] + "</p>";
						}
					}

					angular.element((document.querySelector('#popup-content')))[0].innerHTML = txt;
					$map.getOverlays().getArray()[0].setPosition(coordinate);

				} else if (geo_interaction.type == "cross" && geo_interaction.selectedFilterType == "near") {
					layerServ.spy();
				} else if (geo_interaction.type == "cross" && geo_interaction.selectedFilterType == "intersect") {
					layerServ.intersectFeature();
				} else if (geo_interaction.type == "cross" && geo_interaction.selectedFilterType == "inside") {
					layerServ.insideFeature();
				} else if (geo_interaction.type == "cross") {
					
					layerServ.overlay.setPosition(undefined); // hides
					// eventual messages present on the map

					var multiSelect = geoModule_template.crossnav && geoModule_template.crossnav.multiSelect ? geoModule_template.crossnav.multiSelect : null;
					switch (multiSelect) {
					case (multiSelect !== undefined && true):
						/*
						 * Cross navigation with multiple selected features is handled in
						 * "geoCrossNavMultiselect" controller
						 */
						break;
					default:
						if (prop != null) {
							crossNavigation.navigateTo(layerServ.selectedFeatures[0]);
						}
					break;
					}
				}
			}

			
			
			this.calculateDistance = function(coord,endCoord){
				//calcola il raggio in km
				var wgs84Ellipsoid = new ol.Ellipsoid(6378137, 1 / 298.257223563);
				var sourceProj = $map.getView().getProjection();
				
				var c1 = ol.proj.transform(coord, sourceProj, 'EPSG:4326');
				var c2 = ol.proj.transform(endCoord, sourceProj, 'EPSG:4326');
				
				return wgs84Ellipsoid.vincentyDistance(c1,c2);
			}
			
			this.calculateRay = function(distance){
				//coordinate scelte randomicamente
				var x = -12821852.872668605;
				var y = 5021607.010222939;
				var xF = x + distance;
				var yF = y + distance;
				
				var coord = $map.getPixelFromCoordinate([x,y]);
				var coordF =  $map.getPixelFromCoordinate([xF,yF]);
				return layerServ.calculateDistance(coord,coordF);
				
			}
			this.updateTemplateLayer = function(legendType) {
				geoModule_thematizer.updateLegend(legendType);
				if (layerServ.templateLayer == undefined || Object.keys(layerServ.templateLayer).length == 0) {
					return;
				}
				if (geoModule_templateLayerData.type == "WMS") {
					var sldBody = geoModule_thematizer.getWMSSlBody(geoModule_templateLayerData);
					layerServ.templateLayer.getSource().updateParams({
						SLD_BODY : sldBody
					})
				} else {
					layerServ.templateLayer.changed();
				}
			};

			this.isSelectedBaseLayer = function(layer) {
				return angular.equals(this.selectedBaseLayerOBJ, layer);
			};

			this.layerIsLoaded = function(layer) {
				return (this.loadedLayerOBJ[layer.layerId] != undefined);
			};

			this.alterBaseLayer = function(layerConf) {
				var layer = this.createLayer(layerConf, true);
				if (layer != undefined) {
					$map.removeLayer(this.selectedBaseLayer);
					this.selectedBaseLayer = layer;
					if (this.selectedBaseLayerOBJ == undefined) {
						this.selectedBaseLayerOBJ = layerConf;
					}
					$map.addLayer(this.selectedBaseLayer);
					$map.updateSize();$map.render();
				}
			};

			this.toggleLayer = function(layerConf) {
				if (this.loadedLayer[layerConf.layerId] != undefined) {
					$map.removeLayer(this.loadedLayer[layerConf.layerId]);
					delete this.loadedLayer[layerConf.layerId];
					delete this.loadedLayerOBJ[layerConf.layerId];
				} else {
					var layer = this.createLayer(layerConf, false);
					if(layer.hasOwnProperty("$$state")){
						layer.then(function(tmpLayer) {
							layerServ.updateLayerLoaded(tmpLayer,layerConf)

							});
					}else{
						layerServ.updateLayerLoaded(layer,layerConf)
					}
					
				}
			};
			
			this.updateLayerLoaded=function(layer,layerConf){
				if (layer != undefined) {
					this.loadedLayer[layerConf.layerId] = layer;
					this.loadedLayerOBJ[layerConf.layerId] = layerConf;
					$map.addLayer(layer);
					$map.updateSize();$map.render();
				}
			}

			this.createLayer = function(layerConf, isBase) {
				var tmpLayer;
				var asyncCall;

				switch (layerConf.type) {
				case 'WMS':
					tmpLayer = new ol.layer.Tile({
						source : new ol.source.TileWMS(/** @type {olx.source.TileWMSOptions} */
								({
									url : layerConf.layerURL,
									params : JSON.parse(layerConf.layerParams),
									options : JSON.parse(layerConf.layerOptions)
								}))
					});
					break;

				case 'WFS':
					var vectorSource = new ol.source.Vector({
						url : layerConf.layerURL,
						format : new ol.format.GeoJSON(),
						// options : JSON.parse(layerConf.layerOptions)
					});

					tmpLayer = new ol.layer.Vector({
						source : vectorSource,
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
					
					tmpLayer= this.getLayerFromFile(layerConf);
					
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
				.success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							sbiModule_logger.log("file layer non Ottenuto");
						} else {
							sbiModule_logger.trace("file layer caricato",data);
//			
							var vectorSource = new ol.source.Vector(
									{
										features : (new ol.format.GeoJSON()).readFeatures(data,
												{
													featureProjection : 'EPSG:3857'
												})
									});

							var tmpLayer= new ol.layer.Vector({
								source : vectorSource
							}); 
							deferredLayer.resolve(tmpLayer);
						}
				})
				.error(function(data, status, headers, config) {
					sbiModule_logger.log("file layer non Ottenuto");
				});
				
				 return deferredLayer.promise;

			}
		});

geoM.factory('geoModule_constant', function(sbiModule_translate) {
	var cont = {
			analysisLayer : sbiModule_translate.load("gisengine.constant.analysisLayer"),
			templateLayer : sbiModule_translate.load("gisengine.constant.templateLayer"),
			noCategory : sbiModule_translate.load("gisengine.constant.noCategory"),
			defaultBaseLayer : sbiModule_translate.load("gisengine.constant.defaultBaseLayer")
	}
	return cont;
});

