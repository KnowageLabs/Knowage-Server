/**
 * 
 */

var geoM=angular.module('geo_module');

geoM.factory('$map',function(){
	var map= new ol.Map({
		  target: 'map',
		  layers: [],
		  view: new ol.View({
		    center: ol.proj.transform(
		        [0, 40], 'EPSG:4326', 'EPSG:3857'),
		    zoom: 5
		  })
		});
	
	
	return map;
});

geoM.factory('baseLayer', function() {
	
	var baseLayersConf={
					    "Default": {
					        "OpenStreetMap": {
					            "type": "TMS",
					            "category":"Default",
					            "name": "OpenStreetMap",
					            "url": "http://tile.openstreetmap.org/",
					            "options": {
					                "type": "png",
					                "displayOutsideMaxExtent": true
					            }
					        },
					        "OSM": {
					            "type": "OSM",
					            "category":"Default",
					            "name":"OSM"
					        }
					    }
					};
  return baseLayersConf;
});


geoM.service('layerServices', function(baseLayer, $map,$http) {
	this.selectedBaseLayer;  //the selected base layer
	this.selectedBaseLayerOBJ;
	this.loadedLayer={};
	this.loadedLayerOBJ={};
	
	this.isSelectedBaseLayer=function(layer){
		return angular.equals(this.selectedBaseLayerOBJ, layer);
	}
	
	this.layerIsLoaded=function(layer){
		return (this.loadedLayerOBJ[layer.id]!=undefined);
	}

	this.alterBaseLayer = function(layerConf) {
		console.log("alterBaseLayer", layerConf);
		var layer=this.createLayer(layerConf,true);
		if(layer!=undefined){
			$map.removeLayer(this.selectedBaseLayer);
			this.selectedBaseLayer=layer;
			this.selectedBaseLayerOBJ=layerConf;
			$map.addLayer(this.selectedBaseLayer);
			$map.render();
		}
		
	}

	this.toggleLayer = function(layerConf) {
		console.log("addLayer");
		if(this.loadedLayer[layerConf.id]!=undefined){
			$map.removeLayer(this.loadedLayer[layerConf.id]);
			delete this.loadedLayer[layerConf.id];
			delete this.loadedLayerOBJ[layerConf.id];
		}else{
			var layer=this.createLayer(layerConf,false);
			if(layer!=undefined){
				this.loadedLayer[layerConf.id]=layer;
				this.loadedLayerOBJ[layerConf.id]=layerConf;
				$map.addLayer(layer);
				$map.render();
			}
		}
	}
	
	
	this.createLayer=function(layerConf,isBase){
		
		var tmpLayer;
		var zIndex=0;
		if(isBase){
			zIndex=-1;
		}
		
		switch (layerConf.type) {
		case 'WMS':
			tmpLayer = new ol.layer.Tile({
				zIndex : zIndex,
				source : new ol.source.TileWMS(/** @type {olx.source.TileWMSOptions} */
				({
					url : layerConf.url,
					params : layerConf.params,
					options : layerConf.options
				}))
			});
			break;
		case 'WFS': // TODO test if works
			var vectorSource = new ol.source.Vector({
				  url: 'http://pacweb.eng.it/astuto-geoserver/ATeSO/ows?service=WFS&version=1.0.0&request=GetFeature&typeName=ATeSO:v_at_gis_limite_comunale_wgs84&maxFeatures=50&outputFormat=application/json',
				  format: new ol.format.GeoJSON(),
				  options : layerConf.options
				});
			
		
			tmpLayer = new ol.layer.Vector({
				  source: vectorSource,
				});
			
			
				
			break;
		case 'TMS': // TODO check if work
			tmpLayer = new ol.layer.Tile({
				zIndex : zIndex,
				source : new ol.source.XYZ({
					tileUrlFunction : function(coordinate) {
						if (coordinate == null) {
							return "";
						}
						var z = coordinate[0];
						var x = coordinate[1];
						// var y = (1 << z) -coordinate[2] - 1;
						var y = -coordinate[2] - 1;
						return layerConf.url + '' + z + '/' + x + '/' + y + '.'
								+ layerConf.options.type;
					},

				})
			});
			break;
		case 'OSM':
			tmpLayer = new ol.layer.Tile({
				source : new ol.source.MapQuest({
					layer : 'osm'
				}),
				zIndex : zIndex
			});
			break;
		default:
			console.error('Layer type [' + layerConf.type + '] not supported');
			break;
		}
		
		return tmpLayer;
	}
	
	
	 
})



geoM.service('geoReportUtils',function(baseLayer,$map){
	
	 this.osm_getTileURL= function(bounds) {
			var res = $map.getResolution();
			var x = Math.round((bounds.left - this.maxExtent.left) / (res * this.tileSize.w));
			var y = Math.round((this.maxExtent.top - bounds.top) / (res * this.tileSize.h));
			var z = $map.getZoom();
			var limit = Math.pow(2, z);

			if (y < 0 || y >= limit) {
				console.log("####################### implementare  OpenLayers.Util.getImagesLocation() + ''404.png'")
//				return OpenLayers.Util.getImagesLocation() + "404.png";
			} else {
				x = ((x % limit) + limit) % limit;
				return this.url + z + "/" + x + "/" + y + "." + this.type;
			}
		}
	
})
