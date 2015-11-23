<%@page import="java.util.HashMap"%>
<%@page import="it.eng.spagobi.engines.georeport.GeoReportEngineInstance"%>
<%@page import="it.eng.spagobi.utilities.engines.EngineConstants"%>
<%@page import="org.json.JSONObject"%>
<%
    Map driverParamsMap = new HashMap();
	for(Object key : engineInstance.getAnalyticalDrivers().keySet()){
		if(key instanceof String && !key.equals("widgetData")){
			String value = request.getParameter((String)key);
			if(value!=null){
				driverParamsMap.put(key, value);
			}
		}
	}
	String driverParams = new JSONObject(driverParamsMap).toString(0);
%>
<script>
/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */
 
var geoM=angular.module('geo_module',['ngMaterial','ngAnimate','angular_table','sbiModule']);
	
geoM.factory('geo_template',function(geoReportCompatibility){
	
	var t= <%= template %>;
	
	if(t.hasOwnProperty('role')) {
		t.role = t.role.charAt(0) == '/'? t.role.charAt(0): 	'/' + t.role.charAt(0);
	}
	
	var executionRole = '<%= executionRole %>';
	t.role = executionRole || t.role;
	
	var executionContext = {};
    <% 
    Iterator it = analyticalDrivers.keySet().iterator();
	while(it.hasNext()) {
		String parameterName = (String)it.next();
		String parameterValue = (String)analyticalDrivers.get(parameterName);
	 	String quote = (parameterValue.startsWith("'"))? "" : "'";
		if ( parameterValue.indexOf(",") >= 0){
	 %>
			executionContext ['<%=parameterName%>'] = [<%=quote%><%=parameterValue%><%=quote%>];
	<%	}else{
	%>
			executionContext ['<%=parameterName%>'] = <%=quote%><%=parameterValue%><%=quote%>;
	 <%
	 	}		
	 } //while
    %>
    t.executionContext = executionContext;
    geoReportCompatibility.resolveCompatibility(t);
  
    if(!t.hasOwnProperty("selectedIndicator")){
    	t.selectedIndicator = null;
    }
    
    if(!t.hasOwnProperty("selectedFilters")){
    	t.selectedFilters={};
    }

    return t;
});


geoM.factory('geo_dataset',function(){
	var ds={};
	return ds;
});

geoM.factory('dataset_join_columns_item',function(){
	var dsjc={};
	return dsjc;
});

geoM.factory('geo_indicators',function(){
	var gi=[];
	return gi;
});

geoM.factory('geo_filters',function(){
	var gi=[];
	return gi;
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
// 		    center: ol.proj.transform(  [0, 40], 'EPSG:4326', 'EPSG:3857'),
		    zoom: 5
		  })
		});
	return map;
});

geoM.factory('baseLayer', function() {
	// todo thr following configuration should be loaded from a rest service (from LayerCatalogue) 
	var baseLayersConf={
	    "Default": {
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
	    }
	};
  	return baseLayersConf;
});

geoM.service('layerServices', function(baseLayer, $map,$http,thematizer,geo_interaction,crossNavigation) {
	this.selectedBaseLayer;  //the selected base layer
	this.selectedBaseLayerOBJ;
	this.loadedLayer={};
	this.loadedLayerOBJ={};
	this.templateLayer={};
	
	this.setTemplateLayer = function(data){
		var vectorSource = new ol.source.Vector({
			  features: (new ol.format.GeoJSON()).readFeatures(data, {
//				  dataProjection: 'EPSG:4326',
				  featureProjection: 'EPSG:3857'
			  })
		});

		
		this.templateLayer = new ol.layer.Vector({
			zIndex:2,
		  source: vectorSource
		  , style: thematizer.getStyle
		});
		
		this.templateLayer.setZIndex(1000);
		
		
		$map.addLayer(this.templateLayer);
		 var selectStyle = new ol.style.Style({
	          stroke: new ol.style.Stroke({
	            color: '#000000',
	            width: 2
	        }),
	        fill: new ol.style.Fill({
		          color: "rgba(174, 206, 230, 0.78)"
		        })
	      });

		 
		var select =new ol.interaction.Select({
			  condition: ol.events.condition.click,
			  style:[selectStyle]
			});
		 $map.addInteraction(select);
		 
		 var overlay = new ol.Overlay(/** @type {olx.OverlayOptions} */ ({
		   	element: angular.element((document.querySelector('#popup')))[0],
		 }));
		 
// 		 angular.element((document.querySelector('#popup-closer')))[0].onclick = function() {
// 			  overlay.setPosition(undefined);
// 			  angular.element((document.querySelector('#popup-closer')))[0].blur();
// 			  return false;
// 			};
			
		 $map.addOverlay(overlay);
		
		 select.on('select', function(evt) {
			 console.log("asewleocasokasd");
			 if(evt.selected[0]==undefined || geo_interaction.distance_calculator){
				 overlay.setPosition(undefined);
				 return;
			 }
			 
			 var prop= evt.selected[0].getProperties();
			 
			 if(geo_interaction.type=="identify"){
				 var coordinate = evt.mapBrowserEvent.coordinate;
				 var hdms = ol.coordinate.toStringHDMS(ol.proj.transform(coordinate, 'EPSG:3857', 'EPSG:4326'));
				 
				 var txt="";
				 for(var key in prop){
					 if(key!="geometry"){
					 txt+="<p>"+key+":"+prop[key]+"</p>";
					 }
				 }
			
				 angular.element((document.querySelector('#popup-content')))[0].innerHTML =txt;
			 	 $map.getOverlays().getArray()[0].setPosition(coordinate);
			 }else if(geo_interaction.type=="cross"){
				 crossNavigation.navigateTo(prop);
			 }
	    });
		 
		var tmp = new ol.View({
			extent:this.templateLayer.getProperties().source.getExtent(),
	  	});
		
		$map.getView().fit(this.templateLayer.getProperties().source.getExtent(),$map.getSize());
	};
	
	this.updateTemplateLayer = function(){
		this.templateLayer.changed();
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
// 				zIndex : zIndex,
				source : new ol.source.TileWMS(/** @type {olx.source.TileWMSOptions} */
				({
					url : layerConf.layerURL,
					params : JSON.parse(layerConf.layerParams),
					options :JSON.parse(layerConf.layerOptions)
				}))
			});
			break;
		case 'WFS': // TODO test if works
			var vectorSource = new ol.source.Vector({
				  url: layerConf.layerURL,
				  format: new ol.format.GeoJSON(),
//				  options : JSON.parse(layerConf.layerOptions)
				});
			
		
			tmpLayer = new ol.layer.Vector({
				  source: vectorSource,
				});
			
			break;
		case 'TMS': // TODO check if work
			
			var options=(layerConf.layerOptions instanceof Object)? layerConf.layerOptions : JSON.parse(layerConf.layerOptions);
			tmpLayer = new ol.layer.Tile({
// 				zIndex : zIndex,
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
// 			,zIndex : zIndex
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

geoM.factory('geoConstant',function(){
	var cont= {
			templateLayer:"Document templates"
	}
	return cont;
});

geoM.factory('geoModule_driverParameters',function(geoReportCompatibility){
	var driverParamsAsString = '<%=driverParams%>';
	
	var driverParamsToReturn = JSON.parse(driverParamsAsString);
	
	return driverParamsToReturn;
});

geoM.service('crossNavigation', function(geo_template, geoModule_driverParameters, sbiModule_translate) {	
	this.navigateTo = function(selectedElementData){
		if(!geo_template.crossnav ) {
			alert(sbiModule_translate.load('gisengine.crossnavigation.error.wrongtemplatedata'));
			return;
			
		} else {
			var parametersAsString = '';
			
			// Cross Navigation Static parameters
			if(geo_template.crossnav.staticParams 
					&& (typeof (geo_template.crossnav.staticParams) == 'object')) {
				
				var staticParams = geo_template.crossnav.staticParams;
				var staticParamsKeys = Object.keys(staticParams);
				
				for(var i = 0; i < staticParamsKeys.length; i++) {
					var staticParameterKey = staticParamsKeys[i];
					var staticParameterValue = staticParams[staticParameterKey];
					
					parametersAsString += staticParameterKey + '=' + staticParameterValue + '&';
				}
			}
			
			// Cross Navigation Dynamic parameters
			if(geo_template.crossnav.dynamicParams 
					&& Array.isArray(geo_template.crossnav.dynamicParams)) {
				
				var dynamicParams = geo_template.crossnav.dynamicParams;
				for(var i = 0; i < dynamicParams.length; i++) {
					var param = dynamicParams[i];
					
					if(param.scope.toLowerCase() == 'feature') {
						parametersAsString += param.state + '=' + selectedElementData[param.state] + '&';
					} else if(param.scope.toLowerCase() == 'env') {
						var paramInputName = param.inputpar;
						var paramOutputName = param.outputpar;
						
						//If the "paramInputName" is not set in the parameter mask (on the right side)
						if(!geoModule_driverParameters[paramInputName]) {
							continue;
						} else {
							parametersAsString += 
								(paramOutputName ? paramOutputName : paramInputName)
								 + '=' + geoModule_driverParameters[paramInputName] + '&';
						}
					}
				}
			}
			
			var frameName = "iframe_crossNavigation";
			
			parent.execCrossNavigation(frameName, geo_template.crossnav.label, parametersAsString);
		}
	}
});

</script>