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
				$rootScope){
	
		var ms = this; //mapServices
		var activeInd;
		var activeConf;
		var cacheProportionalSymbolMinMax;
		
		ms.getFeaturesDetails = function(geoColumn, selectedMeasure, config, values){
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
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
							console.log("Error getting longitude and latitude from column value ["+ geoColumn +"]. Check the dataset and its metadata.");
							return null;
						}
						if (lonlat.length != 2){
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load('sbi.cockpit.map.lonLatError').replace("{0}",geoColumn).replace("{1}",geoFieldValue), 'Title', 0);
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
				        ms.addDsPropertiesToFeature(feature, row, values.metaData.fields);
				       //at least add the layer owner
				        feature.set("parentLayer",config.name);
				        featuresSource.addFeature(feature);
					}
					
					return featuresSource;
				}
			}
			return new ol.source.Vector();
		}
	    
		ms.addDsPropertiesToFeature = function (f, row, meta){
			//add columns value like properties
			for (c in row){
				f.set(ms.getHeaderByColumnName(c, meta), row[c]);
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
		
	    var styleCache = {};
	    ms.layerStyle = function(feature, resolution){
//	          var size = feature.get('features').length;
			var props  = feature.getProperties();
			var parentLayer = feature.get('parentLayer')
			var config = ms.getActiveConf(parentLayer);
			var configThematizer = config.analysisConf || {};
			var configMarker = config.markerConf || {};
	      	var value =  props[ms.getActiveIndicator()] || 0;
			var style;
			var useCache = false; //cache isn't use for analysis, just with fixed marker
			
			switch (configThematizer.defaultAnalysis) {
			case 'choropleth':
				style = ms.getChoroplethStyles(value, props, configThematizer.choropleth, configMarker);
				break;
			case 'proportionalSymbol':
				style = ms.getProportionalSymbolStyles(value, props, configThematizer.proportionalSymbol, configMarker);
				break;
			default:
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
		
		ms.getProportionalSymbolStyles = function(value, props, config){
			var textValue =  props[ms.activeInd] || "";
			return new ol.style.Style({
		          fill: new ol.style.Fill({
		                color :  config.color
		              }),
		              stroke: new ol.style.Stroke({
		                color: '#ffcc33',
		                width: 2
		              }),
		              image: new ol.style.Circle({
		                radius: ms.getProportionalSymbolSize(value, ms.activeInd, config),
		                fill: new ol.style.Fill({
		                 color :  config.color
		                })
		              }),
		              text: new ol.style.Text({
		                  font: '12px Calibri,sans-serif',
		                  fill: new ol.style.Fill({ color: '#000' }),
		                  stroke: new ol.style.Stroke({
		                    color: '#fff', width: 2
		                  }),
		                  text: textValue.toString()
		                })
		            });
		}
		
		ms.getChoroplethStyles = function(value, props, config){
			var textValue =  props[ms.activeInd] || "";
			
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
			var textValue =  props[ms.activeInd] || "";
			return new ol.style.Style({
				image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
					stroke: new ol.style.Stroke({ //border doesn't work
						color: 'red',
						width: 3
					}),
				    opacity: 1,
				    crossOrigin: 'anonymous',
				    color: config.color || 'blue',
//				    src: 'data/icon.png'
//				    src: 'https://www.mapz.com/map/marker/svg/M_marker_heart_150910.svg'
//				    src: 'https://s3.amazonaws.com/com.cartodb.users-assets.production/maki-icons/embassy-18.svg',
				    src: config.icon || 'https://openlayers.org/en/v4.6.4/examples/data/dot.png'
					}))
		          });
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
			
			var minValue = ms.cacheProportionalSymbolMinMax[name].minValue;
			var maxValue = ms.cacheProportionalSymbolMinMax[name].maxValue;
			var size;
			
			var maxRadiusSize = config.maxRadiusSize;
			var minRadiusSize = config.minRadiusSize;
			
			if(minValue == maxValue) { // we have only one point in the distribution
				size = (maxRadiusSize + minRadiusSize)/2 + Math.random();
			} else {
				size = ( parseInt(val) - minValue) / ( maxValue - minValue) *
				(maxRadiusSize - minRadiusSize) + minRadiusSize + Math.random();
			}
			return size;
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
				if (ms.activeConf[c].layer == l)
					return ms.activeConf[c].config;
			}
			return {};
		}
		
		ms.setActiveConf=function(l, c){
			if (!ms.activeConf)
				ms.activeConf = [];
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
			var toReturn = key;
			for (var v=0; v<values.length; v++){
				if (values[v].header === key)
					return values[v].name;
			}
				
			return toReturn;
		}
	}	

})();