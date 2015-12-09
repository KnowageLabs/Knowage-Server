var geoM=angular.module('geoModule',['ngMaterial','ngAnimate','angular_table','sbiModule','mdColorPicker',"expander-box"]);

geoM.factory('geoModule_dataset',function(){
	var ds={};
	return ds;
});

geoM.factory('geModule_datasetJoinColumnsItem',function(){
	var dsjc={};
	return dsjc;
});

geoM.factory('geoModule_indicators',function(){
	var gi=[];
	return gi;
});

geoM.factory('geoModule_filters',function(){
	var gi=[];
	return gi;
});

geoM.factory('geoModule_templateLayerData',function(){
	var tld={};
	return tld;
});

geoM.factory('$map',function(){
	var map= new ol.Map({
		target: 'map',
		layers: [],
		controls: [
		           new ol.control.Zoom()
		           ],
		           view: new ol.View({
		        	   center:[1545862.460039, 4598265.489834],
		        	   zoom: 5
		           })
	});
	return map;
});

geoM.factory('baseLayer', function(geoModule_constant) {
	// todo thr following configuration should be loaded from a rest service (from LayerCatalogue) 
	var baseLayersConf={};
	baseLayersConf[geoModule_constant.defaultBaseLayer]={
			"OpenStreetMap": {
				"type": "TMS",
				"category":"Default",
				"label": "OpenStreetMap",
				"layerURL": "http://tile.openstreetmap.org/",
				"layerOptions": {
					"type": "png",
					"displayOutsideMaxExtent": true
				}
			},
			"OSM": {
				"type": "OSM",
				"category":"Default",
				"label":"OSM"
			}
	};


	return baseLayersConf;
});

geoM.service('geoModule_layerServices', function(
		$http, baseLayer, sbiModule_logger, 
		$map, geoModule_thematizer, geo_interaction, 
		crossNavigation, sbiModule_config, geoModule_template, sbiModule_restServices,geoModule_templateLayerData ) {

	var layerServ=this;
	var sFeatures;
	var select;
	var currentInteraction={"type":null,"obj":null};
	this.selectedBaseLayer;  //the selected base layer
	this.selectedBaseLayerOBJ;
	this.loadedLayer={};
	this.loadedLayerOBJ={};
	this.templateLayer={};
	this.selectedFeatures = [];

//	this.cachedFeatureStyles = {};

	this.setTemplateLayer = function(data){
		Object.assign(geoModule_templateLayerData,data);
		geoModule_thematizer.updateLegend('choropleth');
		if(geoModule_templateLayerData.type=="WMS"){
			var sldBody=geoModule_thematizer.getWMSSlBody(geoModule_templateLayerData);
			console.log(sldBody)
			var params=JSON.parse(geoModule_templateLayerData.layerParams);
			params.LAYERS=geoModule_templateLayerData.layerName;
//			var params={};
			params.SLD_BODY =sldBody;

			layerServ.templateLayer = new ol.layer.Tile({
				source : new ol.source.TileWMS(/** @type {olx.source.TileWMSOptions} */
						{
							url : sbiModule_config.contextName+"/api/1.0/geo/getWMSlayer?layerURL="+geoModule_templateLayerData.layerURL,
							params : params,
							options :JSON.parse(geoModule_templateLayerData.layerOptions)
						})
			});

		}else{
			var vectorSource = new ol.source.Vector({
				features: (new ol.format.GeoJSON()).readFeatures(geoModule_templateLayerData, {
//					dataProjection: 'EPSG:4326',
					featureProjection: 'EPSG:3857'
				})
			});


			layerServ.templateLayer = new ol.layer.Vector({
				source: vectorSource
				, style: geoModule_thematizer.getStyle
			});
		}

		layerServ.templateLayer.setZIndex(0);
		$map.addLayer(layerServ.templateLayer);
		var duration = 2000;
		var start = +new Date();
		var pan = ol.animation.pan({
			duration: duration,
			source: /** @type {ol.Coordinate} */ ($map.getView().getCenter())
		});
		var bounce = ol.animation.bounce({
			duration: duration,
			resolution: 4*$map.getView().getResolution()
		});

		$map.beforeRender(pan, bounce);

		$map.getView().setCenter(geoModule_template.currentView.center);
		$map.getView().setZoom(geoModule_template.currentView.zoom); 

//		$map.getView().fit(layerServ.templateLayer.getProperties().source.getExtent(),$map.getSize());

		layerServ.addClickEvent();
	};


	this.overlay;

	this.addClickEvent=function(){
		var selectStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
//				color: '#000000',
				color: [0, 0, 0, 1],
				width: 2
			}),
			fill: new ol.style.Fill({
				color: "rgba(174, 206, 230, 0.78)"
			})
		});

		layerServ.overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
			element: angular.element((document.querySelector('#popup')))[0],
		}));
		$map.addOverlay(layerServ.overlay);

		select = new ol.interaction.Select({
			condition: ol.events.condition.singleClick,
			style: selectStyle
		});
		$map.addInteraction(select);
		//	layerServ.setInteraction('box');
		select.on('select', function(evt) {
			console.log("select");
			if(geo_interaction.type == "identify" && (evt.selected[0]==undefined || geo_interaction.distance_calculator) && geoModule_templateLayerData.type!="WMS"){
				layerServ.overlay.setPosition(undefined);
				return;
			}

			//if is a WMS i must load the properties from server
			if(geoModule_templateLayerData.type=="WMS"){
				var urlInfo= layerServ.templateLayer.getSource().getGetFeatureInfoUrl(
						evt.mapBrowserEvent.coordinate, $map.getView().getResolution(), 'EPSG:3857',
						{'INFO_FORMAT': 'application/json'});
				$http.get( urlInfo ).success(
						function(data, status, headers, config) {
							sbiModule_logger.log("getGetFeatureInfoUrl caricati",data);
							if (data.hasOwnProperty("errors")) {
								sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti",data.errors);
							} else {
								if(data.features.length==0){
									layerServ.overlay.setPosition(undefined);
								}else{
									layerServ.doClickAction(evt,data.features[0].properties)
								}

							}
						}).error(function(data, status, headers, config) {
							sbiModule_logger.log("getGetFeatureInfoUrl non Ottenuti " , status);
						});

			}else{
				var prop = evt.selected.length ? evt.selected[0].getProperties(): null;
				layerServ.doClickAction(evt,prop)
			}
		});
	}

	this.intersectFeature = function(){
		console.log("this.intersect");
		//select fetures with a box
		var sFeatures = select.getFeatures();



		var selectStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: [0, 0, 0, 1],
				width: 2
			}),
			fill: new ol.style.Fill({
				color: "rgba(174, 206, 230, 0.78)"
			})
		});

		var dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly,
			style: selectStyle
		});
		currentInteraction.obj=dragBox;
		$map.addInteraction(dragBox);



		dragBox.on('boxend', function(e) {
			var selection = [];
			var extent = dragBox.getGeometry().getExtent();

			var vectorSource = layerServ.templateLayer.getSource();
			vectorSource.forEachFeatureIntersectingExtent(extent, function(feature) {
				sFeatures.push(feature);				
				selection.push(feature);
			});

			layerServ.selectedFeatures = selection;
			geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
			console.log("layerServ.selectedFeatures (dragBox)-> ", layerServ.selectedFeatures);
			console.log("geo_interaction.selectedFilterType -> ", geo_interaction.selectedFilterType);
		//	layerServ.Render(selection);

		});

		// clear selection when drawing a new box and when clicking on the map
		dragBox.on('boxstart', function(e) {
			layerServ.overlay.setPosition(undefined);
			sFeatures.clear();
		});


	}


	this.insideFeature = function(){
		console.log("this.inside");
		var sFeatures = select.getFeatures();



		var selectStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: [0, 0, 0, 1],
				width: 2
			}),
			fill: new ol.style.Fill({
				color: "rgba(174, 206, 230, 0.78)"
			})
		});

		var dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly,
			style: selectStyle
		});
		currentInteraction.obj=dragBox;
		$map.addInteraction(dragBox);



		dragBox.on('boxend', function(e) {
			var selection = [];
			var extent = dragBox.getGeometry().getExtent();

			var vectorSource = layerServ.templateLayer.getSource();
			vectorSource.forEachFeatureIntersectingExtent(extent, function(feature) {
				var geom = feature.getGeometry().A;
				if(geom[0]>extent[0]){
					if(geom[1]>extent[1]){
						if(geom[2]<extent[2]){
							if(geom[3]<extent[3]){
								//è inside the polygon
								sFeatures.push(feature);				
								selection.push(feature);
							}
						}
					}
				}

			});

			layerServ.selectedFeatures = selection;
			geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
			//	layerServ.Render(selection);

			console.log("layerServ.selectedFeatures (dragBox)-> ", layerServ.selectedFeatures);
			console.log("geo_interaction.selectedFilterType -> ", geo_interaction.selectedFilterType);


		});

		// clear selection when drawing a new box and when clicking on the map
		dragBox.on('boxstart', function(e) {
			layerServ.overlay.setPosition(undefined);
			sFeatures.clear();
		});

	}
	
	this.near = function(){
		console.log("near");
		
		var coordinate;
		var circleGeometry;
		var myCircle;
		var ray = 500000;
		
		var fillStyle = new ol.style.Style({
			 fill: new ol.style.Fill({
			      color: 'rgba(255, 255, 255, 0.2)'
			    }),
			 stroke: new ol.style.Stroke({
			      color: '#ffcc33',
			      width: 2
			    })
		});
	
		$map.on('click', function(evt) {
			console.log("inside click");
			coordinate = evt.coordinate;
			console.log("coordinate: ",coordinate);
			//500000 è un valore scelto temporaneamente succesisvamente saranno i km che sceglie l'utente
			myCircle = new ol.geom.Circle(coordinate,ray);

			
			
		});
		
		$map.on('postcompose', function(event) {
			if(coordinate){
				//se è stato cliccato e le coordinate sono state inizializzate disegna cerchio
				var vecCtx = event.vectorContext;			
				vecCtx.setFillStrokeStyle(fillStyle, null);
				vecCtx.drawCircleGeometry(myCircle);
				$map.render();
				var extent = myCircle.getExtent();
				
				//seleziona le features interne al cerchio
				var vectorSource = layerServ.templateLayer.getSource();
				vectorSource.forEachFeatureIntersectingExtent(extent, function(feature) {
				layerServ.selectedFeatures.push(feature);				
				ctx.clip();
				});
			}
		});
	}
	
	this.Render =function(features){
		var raster = new ol.layer.Tile({
			source: new ol.source.Stamen({
				layer: 'toner'
			})
		});
		raster.setZindex=10000;
		$map.addLayer(raster);

		var osm=this.createLayer(baseLayer.Default.OSM,false);
	
		osm.setZindex=1000001;
		$map.addLayer(osm);

		// A style for the geometry.
		var fillStyle = new ol.style.Fill({color: [0, 0, 0, 0]});
		var newArray=[[[]]];
		var array=[];
		var array2=[];
		var array3=[];
		var coordinates=[];

		for(var i=0;i < features.length;i++ ){
			for(var j=0;j<features[i].getGeometry().getCoordinates().length;j++){

				for (var k = 0; k < features[i].getGeometry().getCoordinates()[j].length; k++) {

					for (var m = 0; m < features[i].getGeometry().getCoordinates()[j][k].length; m++) {
						newArray[0][0].push(features[i].getGeometry().getCoordinates()[j][k][m]);
						//array.push(features[i].getGeometry().getCoordinates()[j][k][m]);
					}

				}

			}
		}

		/*	array2.push(array);
		console.log(array2);
		array3.push(array2);
		//coordinates= new Array(array3);
		coordinates.push(array3);
		console.log("hello");*/

		console.log(newArray,features[0].getGeometry().getCoordinates());

		$map.on('precompose', function(event) {
			var ctx = event.context;
			var vecCtx = event.vectorContext;
			ctx.save();

			// Using a style is a hack to workaround a limitation in
			// OpenLayers 3, where a geometry will not be draw if no
			// style has been provided.
			vecCtx.setFillStrokeStyle(fillStyle, null);

			//	var clipGeometry = new ol.geom.MultiPolygon(features[0].getGeometry().getCoordinates());
			var clipGeometry = new ol.geom.MultiPolygon(newArray);

			vecCtx.drawMultiPolygonGeometry(clipGeometry);

			/*vecCtx.drawFeature(features[0],new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: [0, 0, 0, 1],
					width: 2
				}),
				fill: new ol.style.Fill({
					color: "rgba(174, 206, 230, 0.78)"
				})
			}));*/

			ctx.clip();
			//$map.removeLayer(raster);
			$map.render();

		});

		$map.on('postcompose', function(event) {
			var ctx = event.context;
			ctx.restore();
		});
	}

	this.serp=function(){


		var imageStyle = new ol.style.Circle({
			radius: 5,
			snapToPixel: false,
			fill: new ol.style.Fill({color: 'yellow'}),
			stroke: new ol.style.Stroke({color: 'red', width: 1})
		});

		var headInnerImageStyle = new ol.style.Style({
			image: new ol.style.Circle({
				radius: 2,
				snapToPixel: false,
				fill: new ol.style.Fill({color: 'blue'})
			})
		});

		var headOuterImageStyle = new ol.style.Circle({
			radius: 5,
			snapToPixel: false,
			fill: new ol.style.Fill({color: 'black'})
		});

		var n = 200;
		var omegaTheta = 30000; // Rotation period in ms
		var R = 7e6;
		var r = 2e6;
		var p = 2e6;
		$map.on('postcompose', function(event) {
			var vectorContext = event.vectorContext;
			var frameState = event.frameState;
			var theta = 2 * Math.PI * frameState.time / omegaTheta;
			var coordinates = [];
			var i;
			for (i = 0; i < n; ++i) {
				var t = theta + 2 * Math.PI * i / n;
				var x = (R + r) * Math.cos(t) + p * Math.cos((R + r) * t / r);
				var y = (R + r) * Math.sin(t) + p * Math.sin((R + r) * t / r);
				coordinates.push([x, y]);
			}

			vectorContext.setImageStyle(imageStyle);
			vectorContext.drawMultiPointGeometry(
					new ol.geom.MultiPoint(coordinates), null);

			var headPoint = new ol.geom.Point(coordinates[coordinates.length - 1]);
			var headFeature = new ol.Feature(headPoint);
			vectorContext.drawFeature(headFeature, headInnerImageStyle);

			vectorContext.setImageStyle(headOuterImageStyle);
			vectorContext.drawMultiPointGeometry(headPoint, null);
			console.log(headFeature);

			$map.render();
		});
		$map.render();

	}
	this.Circle = function(){
		//select features with circle

		sFeatures = select.getFeatures();
		var source = new ol.source.Vector();
		var geojsonFormat = new ol.format.GeoJSON();
		var draw = new ol.interaction.Draw({
			//source:source,
			type: "Circle",

		});
		currentInteraction.obj=draw;
		$map.addInteraction(draw);
		draw.on('drawstart',
				function() {

		}, this);
		draw.on('drawend', function(e) {
			var feature = e.feature;
			var poly1 = geojsonFormat.writeFeatureObject(feature);
			var selection = [];
			var extent = e.feature.getGeometry().getExtent();
			var vectorFeatures = layerServ.templateLayer.getSource().getFeatures();

			vectorFeatures.forEach(function(feature) {
				if (!ol.extent.intersects(extent, feature.getGeometry().getExtent())) {
					return;
				}


				var poly2 = geojsonFormat.writeFeatureObject(feature);
				var intersection = turf.intersect(poly1, poly2);
				if (intersection) {
					// intersectionLayer.getSource().addFeature(geojsonFormat.readFeature(intersection));

					sFeatures.push(geojsonFormat.readFeature(poly2));				
					selection.push(geojsonFormat.readFeature(poly2));
				}


			});

			layerServ.selectedFeatures = selection;
			geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
			$scope;
			console.log("layerServ.selectedFeatures (circlebox)-> ", layerServ.selectedFeatures);
			console.log("geo_interaction.selectedFilterType -> ", geo_interaction.selectedFilterType);
		});



	}



	this.setInteraction = function(type){
		console.log("Hiiiii");
		console.log($map);
		if(currentInteraction!={} && currentInteraction.type!=type){
			//remove $map 
			console.log("Remove....");
			console.log(currentInteraction.obj);
			$map.removeInteraction(currentInteraction.obj);
			console.log($map);
		}
		//setta il tipo di interazione;

		currentInteraction.type = type;

		if(type=='near'){
			$map.removeInteraction(currentInteraction.obj);
			//layerServ.near();
		} else if(type=='intersect'){
			$map.removeInteraction(currentInteraction.obj);
			layerServ.intersectFeature();
		} else if(type=='inside'){
			$map.removeInteraction(currentInteraction.obj);
			layerServ.insideFeature();
		}
	}
	this.doClickAction = function(evt, prop){
		console.log("doClickAction");

		layerServ.selectedFeatures = evt.target.getFeatures().getArray();
		geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);
		console.log("%%%%%%%layerServ.selectedFeatures (select)-> ", layerServ.selectedFeatures);
		console.log("%%%%%%%geo_interaction.selectedFilterType -> ", geo_interaction.selectedFilterType);
		console.log(geo_interaction.type);
		if(geo_interaction.type == "identify"){

			var coordinate = evt.mapBrowserEvent.coordinate;
			var hdms = ol.coordinate.toStringHDMS(ol.proj.transform(coordinate, 'EPSG:3857', 'EPSG:4326'));

			var txt="";
			for(var key in prop){
				if(key != "geometry"){
					txt += "<p>" + key + " : " + prop[key]+ "</p>";
				}
			}

			angular.element((document.querySelector('#popup-content')))[0].innerHTML =txt;
			$map.getOverlays().getArray()[0].setPosition(coordinate);

		}else if(geo_interaction.type == "cross"){

			layerServ.overlay.setPosition(undefined); // hides eventual messages present on the map

			var multiSelect = geoModule_template.crossnav && geoModule_template.crossnav.multiSelect? 
					geoModule_template.crossnav.multiSelect : null;
			switch (multiSelect) {
			case (multiSelect !== undefined && true):
				/* Cross navigation with multiple selected features is handled 
				 * in "geoCrossNavMultiselect" controller */
				break;
			default:
				if(prop != null) {
					crossNavigation.navigateTo(layerServ.selectedFeatures[0]);
				}
			break;
			}
		} else if(geo_interaction.type == "filter"){
			//default
			layerServ.near();
			
		}
	}

	this.updateTemplateLayer = function(legendType){
		geoModule_thematizer.updateLegend(legendType);

		if(geoModule_templateLayerData.type=="WMS"){
			var sldBody=geoModule_thematizer.getWMSSlBody(geoModule_templateLayerData);
			console.log(sldBody)
			layerServ.templateLayer.getSource().updateParams({SLD_BODY:sldBody})
		}else{
			layerServ.templateLayer.changed();
		}
	};

	this.isSelectedBaseLayer = function(layer){
		return angular.equals(this.selectedBaseLayerOBJ, layer);
	};

	this.layerIsLoaded = function(layer){
		return (this.loadedLayerOBJ[layer.layerId]!=undefined);
	};

	this.alterBaseLayer = function(layerConf) {
		console.log("alterBaseLayer", layerConf);
		var layer = this.createLayer(layerConf,true);
		if(layer!=undefined){
			$map.removeLayer(this.selectedBaseLayer);
			this.selectedBaseLayer = layer;
			if(this.selectedBaseLayerOBJ==undefined){
				this.selectedBaseLayerOBJ = layerConf;
			}
			$map.addLayer(this.selectedBaseLayer);
			$map.render();
		}
	};

	this.toggleLayer = function(layerConf) {
		console.log("addLayer");
		if(this.loadedLayer[layerConf.layerId]!=undefined){
			$map.removeLayer(this.loadedLayer[layerConf.layerId]);
			delete this.loadedLayer[layerConf.layerId];
			delete this.loadedLayerOBJ[layerConf.layerId];
		}else{
			var layer = this.createLayer(layerConf,false);
			if(layer!=undefined){
				this.loadedLayer[layerConf.layerId]=layer;
				this.loadedLayerOBJ[layerConf.layerId]=layerConf;
				$map.addLayer(layer);
				$map.render();
			}
		}
	};

	this.createLayer = function(layerConf,isBase){
		var tmpLayer;

		switch (layerConf.type) {
		case 'WMS':
			tmpLayer = new ol.layer.Tile({
				source : new ol.source.TileWMS(/** @type {olx.source.TileWMSOptions} */
						({
							url : layerConf.layerURL,
							params : JSON.parse(layerConf.layerParams),
							options :JSON.parse(layerConf.layerOptions)
						}))
			});
			break;

		case 'WFS':
			var vectorSource = new ol.source.Vector({
				url: layerConf.layerURL,
				format: new ol.format.GeoJSON(),
//				options : JSON.parse(layerConf.layerOptions)
			});

			tmpLayer = new ol.layer.Vector({
				source: vectorSource,
			});
			break;

		case 'TMS': // TODO check if work
			var options=(layerConf.layerOptions instanceof Object)? layerConf.layerOptions : JSON.parse(layerConf.layerOptions);
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
						return layerConf.layerURL + '' + z + '/' + x + '/' + y + '.'+ options.type;
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

		default:
			console.error('Layer type [' + layerConf.type + '] not supported');
		break;

		}

		if(isBase){
			tmpLayer.setZIndex(-1)
		}else{

		}

		return tmpLayer;
	};
});

geoM.factory('geoModule_constant',function(sbiModule_translate){
	var cont= {
			analysisLayer:sbiModule_translate.load("gisengine.constant.analysisLayer"),
			templateLayer:sbiModule_translate.load("gisengine.constant.templateLayer"),
			noCategory:sbiModule_translate.load("gisengine.constant.noCategory"),
			defaultBaseLayer:sbiModule_translate.load("gisengine.constant.defaultBaseLayer")
	}
	return cont;
});


/* code simple
this.Box = function(){
		console.log("this.Box");
		//select fetures with a box
		var sFeatures = select.getFeatures();



		var selectStyle = new ol.style.Style({
			stroke: new ol.style.Stroke({
				color: [0, 0, 0, 1],
				width: 2
			}),
			fill: new ol.style.Fill({
				color: "rgba(174, 206, 230, 0.78)"
			})
		});

		var dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly,
			style: selectStyle
		});
		currentInteraction.obj=dragBox;
		$map.addInteraction(dragBox);



		dragBox.on('boxend', function(e) {
			var selection = [];
			var extent = dragBox.getGeometry().getExtent();

			var vectorSource = layerServ.templateLayer.getSource();
			vectorSource.forEachFeatureIntersectingExtent(extent, function(feature) {
				sFeatures.push(feature);				
				selection.push(feature);
			});

			layerServ.selectedFeatures = selection;
			geo_interaction.setSelectedFeatures(layerServ.selectedFeatures);


			console.log("layerServ.selectedFeatures (dragBox)-> ", layerServ.selectedFeatures);
			console.log("geo_interaction.selectedFilterType -> ", geo_interaction.selectedFilterType);

		});

		// clear selection when drawing a new box and when clicking on the map
		dragBox.on('boxstart', function(e) {
			layerServ.overlay.setPosition(undefined);
			sFeatures.clear();
		});


	}

 */