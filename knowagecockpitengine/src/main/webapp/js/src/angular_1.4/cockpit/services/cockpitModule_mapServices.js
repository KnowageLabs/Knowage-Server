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
		

		ms.featureStyle = function(feature, resolution){
//	          var size = feature.get('features').length;
			var props  = feature.getProperties();
			var config = ms.activeConf.analysisConf || {};
			var configType = config.defaultAnalysis || 'proportionalSymbol';
			var textValue =  props[ms.activeInd] || "";
	      	var value =  props[ms.activeInd] || 0;
			var style;
			
			switch (configType) {
			case 'choropleth':
				style = ms.getChoroplethStyles(value, textValue, config.choropleth);
				break;
			case 'proportionalSymbol':
				style = ms.getProportionalSymbolStyles(value, textValue, config.proportionalSymbol);
				break;
			}
					
	      	return style;	
	    }
		
		ms.getProportionalSymbolStyles = function(value, textValue, config){
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
		
		ms.getChoroplethStyles = function(value, textValue, config){
			
//			return  [new ol.style.Style({
//				stroke: new ol.style.Stroke({
//					color: borderColor,
//					width: 1
//				}),
//				fill: new ol.style.Fill({
//					color: getChoroplethColor(dsValue,layerCol).color
//				}),
//				image: new ol.style.Circle({
//		  			radius: 5,
//		  			stroke: new ol.style.Stroke({
//						color: borderColor,
//						width: 1
//					}),
//		  			fill: new ol.style.Fill({
//		  				color: getChoroplethColor(dsValue,layerCol).color
//		  			})
//		  		})
//			})];
//			
			return new ol.style.Style({
			});
		}
		
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
						if (geoFieldValue.indexOf(" ")){
							lonlat = geoFieldValue.split(" ");
						}else if (geoFieldValue.indexOf(",")){
							lonlat = geoFieldValue.split(",");
						}else{
							console.log("Error getting longitude and latitude from column value ["+ geoFieldValue +"]");
							return null;
						}
						//get config for thematize
						ms.activeInd = selectedMeasure;
						ms.activeConf = config;
						if (!ms.cacheProportionalSymbolMinMax) ms.cacheProportionalSymbolMinMax = {}; //just at beginning
						if (!ms.cacheProportionalSymbolMinMax.hasOwnProperty(name)){
							ms.loadIndicatorMaxMinVal(selectedMeasure, values);
						}
						
						
						//set ol objects
						var transform = ol.proj.getTransform('EPSG:4326', 'EPSG:3857');
						var feature = new ol.Feature();  
				        var coordinate = transform([parseFloat(lonlat[0].trim()), parseFloat(lonlat[1].trim())]);
				        var geometry = new ol.geom.Point(coordinate);
				        feature.setGeometry(geometry);
				        feature.setStyle(ms.featureStyle);
				        ms.addDsPropertiesToFeature(feature, row, values.metaData.fields);
				        featuresSource.addFeature(feature);
					}
			
					return new ol.layer.Vector({
					      source: featuresSource
					});
					
				
					
	/* test for cluster
					 var clusterSource = new ol.source.Cluster({
					        distance: 10,
					        source: featuresSource
					      });
	
				    var styleCache = {};
					return new ol.layer.Vector({
					        source: clusterSource,
					        style: function(feature) {
					          var size = feature.get('features').length;
					          var textValue = "";
					          for (var f=0; f<size; f++){
					        	  var tmpFeature = feature.get('features')[f];
					        	  var props  = tmpFeature.getProperties();
					        	  textValue =  props["name"];
					          }	         
					          var style = styleCache[size];
					          if (!style) {
					            style = new ol.style.Style({
					              image: new ol.style.Circle({
					                radius: 10,
					                stroke: new ol.style.Stroke({
					                  color: '#fff'
					                }),
					                fill: new ol.style.Fill({
					                  color: '#3399CC'
					                })
					              }),
					              text: new ol.style.Text({
	//				                text: size.toString(),
					                text: textValue.toString(),
					                fill: new ol.style.Fill({
					                  color: '#fff'
					                })
					              })
					            });
					            styleCache[size] = style;
					          }
					          return style;
					        }
					      });
	*/
				}
			}
			return new ol.layer.Vector();
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
		
		
		//thematizer functions
		ms.refreshStyle = function (layer, measure, config, values, geoColumn){
			//prepare object for temathization
			ms.loadIndicatorMaxMinVal(measure, values);
			var newSource = ms.getFeaturesDetails(geoColumn, measure, config, values);
			var newStyle = newSource.getStyle();
			layer.setStyle(newStyle);
			//changed() and refresh don't work on 4.6.4 ol version
//			layer.getSource().changed();
//			layer.getSource().refresh({force:true});
		}
		
		ms.getProportionalSymbolSize = function(val, name, config){
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