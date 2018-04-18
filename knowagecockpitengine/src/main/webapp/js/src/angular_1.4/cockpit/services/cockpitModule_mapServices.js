(function() {
	angular
	.module("cockpitModule")
	.service("cockpitModule_mapServices",CockpitModuleMapServiceController)
		function CockpitModuleMapServiceController(
				sbiModule_translate,
				sbiModule_messaging,
				cockpitModule_template,
				cockpitModule_mapThematizerServices,
				$q, 
				$mdPanel, 
				$rootScope,
				$location){
	
		var ms = this; //mapServices
		
		ms.getFeaturesDetails = function(geoColumn, selectedMeasure, config, configColumns, values){
			if (values != undefined){
				var geoFieldName;
				var geoFieldValue;	
				var geoFieldConfig;
				var	featuresSource = new ol.source.Vector();
				
				for(var c=0; c < configColumns.length; c++){
					var conf = configColumns[c];
					if (conf.name === geoColumn){
						geoFieldConfig = conf;
						break;
					}
				}
				 
				for(var k=0; k < values.metaData.fields.length; k++){
					var field = values.metaData.fields[k];
					if (field.header === geoColumn){
						geoFieldName = field.name;
						break;
					}
				}
				
				if (geoFieldName){
					var lon;
					var lat;
					
					for(var r=0; r < values.rows.length; r++){
						//get coordinates
						var coord;
						var row = values.rows[r];
						geoFieldValue = row[geoFieldName].trim();
						if (!geoFieldConfig.properties.coordType){
							//retrocompatibility management
							geoFieldConfig.properties.coordType = 'string';
							geoFieldConfig.properties.coordFormat = 'lon lat';
						}
						
						if (geoFieldConfig.properties.coordType == 'json'){
							var jsonConf = JSON.parse(geoFieldValue);
							
							if (geoFieldConfig.properties.jsonFeatureType && geoFieldConfig.properties.jsonFeatureType.toUpperCase() != 'POINT'){ //for the moment just Point are managed
								sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.jsonCoordTypeInvalid').replace("{0}",geoFieldConfig.properties.jsonFeatureType), 'Title', 0);
								console.log("Json feature of type ["+ geoFieldConfig.properties.jsonFeatureType +"] is not managed. Only [Point] are permit.");
								return null;
							}
							geoFieldValue = jsonConf.coordinates[0] + " " + jsonConf.coordinates[1];
						}
						
						if (geoFieldValue.indexOf(" ") > 0){
							coord = geoFieldValue.split(" ");
						}else if (geoFieldValue.indexOf(",")){
							coord = geoFieldValue.split(",");
						}else{
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (coord.length != 2){
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						
						//setting lon, lat values with correct order (LON, LAT)
						switch(geoFieldConfig.properties.coordFormat) {
					    case "lon lat": 
					    	lon = parseFloat(coord[0].trim());
					    	lat = parseFloat(coord[1].trim());
					        break;
					    case "lat lon":
					    	lon = parseFloat(coord[1].trim());
					    	lat = parseFloat(coord[0].trim());
					    	break;
					    default: 
					    	lon = parseFloat(coord[0].trim());
				    		lat = parseFloat(coord[1].trim());
		    		}
						
						
						//set ol objects
						var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
						var feature = new ol.Feature();  

				        var coordinate = transform([lon, lat]); 
				        var geometry = new ol.geom.Point(coordinate);
				        feature.setGeometry(geometry);

						if (!selectedMeasure) selectedMeasure = config.defaultIndicator;					
						//get config for thematize
						if (selectedMeasure){
							if (!cockpitModule_mapThematizerServices.getCacheSymbolMinMax().hasOwnProperty(config.name+"|"+selectedMeasure)){
								cockpitModule_mapThematizerServices.loadIndicatorMaxMinVal(config.name+"|"+ selectedMeasure, values);
							}
						}	
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
						prop.thresholdsConfig =  ( cols[p].properties && cols[p].properties.thresholds) ? cols[p].properties.thresholds : null;
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
		
		ms.updateCoordinatesAndZoom = function(model, map, l, setValues){
		    	var coord;
		    	var zoom;
		    	var source;
		    	
		    	if (model.content.currentView.center[0] == 0 && model.content.currentView.center[1] == 0){
		    		if (l.getSource() && l.getSource().getSource)
		    			source = l.getSource().getSource(); //cluster case
		    		else
		    			source = l.getSource();
		    		
		    		if (source.getFeatures().length>0 && source.getFeatures()[0].getGeometry().getType() == 'Point')
			    		coord = source.getFeatures()[0].getGeometry().getCoordinates();
					else
						coord = source.getFeatures()[0].getGeometry().getCoordinates()[0][0][0];
			    	
			    	if(source.getFeatures().length>35){
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
		
		ms.setHeatmapWeight= function(feature){
			var parentLayer = feature.get('parentLayer');
			var config = cockpitModule_mapThematizerServices.getActiveConf(parentLayer) || {};
			
			var minmaxLabel = parentLayer + '|' + config.defaultIndicator;
			var minmax = cockpitModule_mapThematizerServices.getCacheSymbolMinMax()[minmaxLabel];
			if (!minmax) return 0;
			
			var props  = feature.getProperties();

		    var p = feature.get(config.defaultIndicator);
		    // perform calculation to get weight between 0 - 1
		    // apply formule: w = w-min/max-min (http://www.statisticshowto.com/normalized/)
		    weight = (p.value - minmax.minValue)/(minmax.maxValue-minmax.minValue);
		    return weight;
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
			    	//layer: watercolor, toner-hybrid, toner, toner-background, toner-hybrid, toner-labels, toner-lines, toner-lite,terrain, terrain-background, terrain-labels, terrain-lines
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