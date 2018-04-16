(function() {
	angular
	.module("cockpitModule")
	.service("cockpitModule_mapServices",CockpitModuleMapServiceController)
		function CockpitModuleMapServiceController(
				sbiModule_translate,
				sbiModule_restServices,
				sbiModule_messaging,
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
		var cacheSymbolMinMax;
		
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
							
							if (geoFieldConfig.properties.jsonFeatureType != 'Point'){ //for the moment just Point are managed
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
							if (!ms.getCacheSymbolMinMax().hasOwnProperty(config.name+"|"+selectedMeasure)){
								ms.loadIndicatorMaxMinVal(config.name+"|"+ selectedMeasure, values);
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
		
		ms.setHeatmapWeight= function(feature){
			var parentLayer = feature.get('parentLayer');
			var config = ms.getActiveConf(parentLayer) || {};
			
			var minmaxLabel = parentLayer + '|' + config.defaultIndicator;
			var minmax = ms.getCacheSymbolMinMax()[minmaxLabel];
			if (!minmax) return 0;
			
			var props  = feature.getProperties();

		    var p = feature.get(config.defaultIndicator);
		    // perform calculation to get weight between 0 - 1
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
				value =  (props[ms.getActiveIndicator()])  ? props[ms.getActiveIndicator()].value : undefined;
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
				style = ms.getOnlyMarkerStyles(value, props, configMarker);
				useCache = true;
//			useCache = false;
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
			return Math.round(toReturn*100)/100; //max 2 decimals
	    }
	    
	    ms.getClusterStyles = function (value, props, config){
	      var tmpSize =  (config.style && config.style['font-size']) ? config.style['font-size'] : '12px';
	      var tmpFont = "bold " + tmpSize + " Roboto";
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
	            	font: tmpFont,
	                text: value.toString(),
	                fill: new ol.style.Fill({
	                  color: (config.style && config.style['color']) ? config.style['color'] : '#fff'
	                })
	              })
	            });
	    }
		
		ms.getProportionalSymbolStyles = function(value, props, config){
			var tmpSize =  (config.style && config.style['font-size']) ? config.style['font-size'] : '12px';
		    var tmpFont = "normal " + tmpSize + " Roboto";
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
		                  font: tmpFont,
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
		
		ms.getOnlyMarkerStyles = function (value, props, config){
			var style;
			var color;
			 
			if (props[ms.getActiveIndicator()] && props[ms.getActiveIndicator()].thresholdsConfig) color = ms.getColorByThresholds(value, props);
			if (!color) color =  (config.style && config.style.color) ? config.style.color : 'blue';
			
			switch(config.type) {
			
			case "icon":
				//font-awesome
				var size = config.size || 100;
				style = new ol.style.Style({
					  text: new ol.style.Text({
						  	text: config.icon.unicode, 
						    font: 'normal ' + ((2*size) + '% ') + config.icon.family,
						    fill: new ol.style.Fill({
						    	 color: color
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
				    color: color,
				    src:  $location.$$absUrl.substring(0,$location.$$absUrl.indexOf('api/')) + '/img/dot.png'
					}))
		          });
				break;
				
			}
			return style;
		}
	
		ms.getColorByThresholds = function(value, props){
			var config = props[ms.getActiveIndicator()].thresholdsConfig;
			var toReturn = null;
			var isEqualOp = false;
			
			for (c in config){
				var evalText = "";
				var thr = config[c];
				var idx = 0;
				for (t in thr){
//					if (typeof(value) == 'number' && typeof(thr['operator'+idx]) != 'undefined' && typeof(thr['val'+idx]) != 'undefined'){
					if (value != '' && !isNaN(value) && typeof(thr['operator'+idx]) != 'undefined' && typeof(thr['val'+idx]) != 'undefined'){
						if (evalText != "") evalText += " && ";
						evalText += "(" + value + " " + thr['operator'+idx] + " " + thr['val'+idx] + " )";
						if (thr['operator'+idx] == '==' && eval(evalText)) {							
							toReturn = thr['color']; 
							isEqualOp = true; //the equal operator has the priority
							if (thr['warning'])
								 props[ms.getActiveIndicator()]['showWarning'] = true;
							break;
						}
					}else
						break;
					idx++;
				}
				if (!isEqualOp && eval(evalText) == true) { //get the last color definition
					toReturn = thr['color']; 
					if (thr['warning'])
						 props[ms.getActiveIndicator()]['showWarning'] = true;
				}

			}
			return toReturn;
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
//			    default: //carto temporaneo fino a rilascio ufficiale
//			    	toReturn = new ol.layer.Tile({ 
//				      	   visible: true,
//				      	   source: new ol.source.XYZ({
//				      	     url: 'https://cartocdn_a.global.ssl.fastly.net/base-light/{z}/{x}/{y}.png',
//				      	     	attributions: '&copy; <a href="http://www.openstreetmap.org/copyright">OpenStreetMap</a>, &copy;<a href="https://carto.com/attribution">CARTO</a>',
//				      	   })
//			      	});
//			    default:
//			    	 toReturn = new ol.layer.Tile({
//			             visible: true,
//			             source: new ol.source.Stamen({
//			                layer: 'watercolor' //'watercolor', 'toner-hybrid',toner, toner-background, toner-labels, toner-lines, toner-lite,terrain, terrain-background, terrain-labels, terrain-lines
//			             })
//			          });
			      
			}
	      
			return toReturn;
			
		}	
		
		ms.getProportionalSymbolSize = function(val, name, config){
			if (!name) return 0;
			
			var minValue = ms.cacheSymbolMinMax[config.name+'|'+name].minValue;
			var maxValue = ms.cacheSymbolMinMax[config.name+'|'+name].maxValue;
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
		
		ms.getCacheSymbolMinMax=function(){
			return ms.cacheSymbolMinMax || {};
		}
		
		ms.setCacheSymbolMinMax=function(n, c){
			if (!ms.cacheSymbolMinMax) ms.cacheSymbolMinMax = {};
			ms.cacheSymbolMinMax[n] = c;
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
			ms.setCacheSymbolMinMax(key, {minValue:minV, maxValue:maxV});
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