(function() {
	angular
	.module("cockpitModule")
	.service("cockpitModule_mapThematizerServices",CockpitModuleMapThematizerServiceController)
		function CockpitModuleMapThematizerServiceController(
				sbiModule_translate,
				sbiModule_messaging,
				cockpitModule_utilstServices, 
				$q, 
				$mdPanel,
				$rootScope,
				$location){
	
		var mts = this; //mapThematizerServices
		
		var cacheSymbolMinMax, cacheDatasetValue;
		var activeInd, activeConf, activeLegend; 
		
		
		var styleCache = {};
	    mts.layerStyle = function(feature, resolution){
	    	
	    	var localFeature;
			if (Array.isArray(feature.get('features')))
				localFeature = feature.get('features')[0];
	    	else
	    		localFeature = feature;
	    	    	
			var props  = localFeature.getProperties();
			var parentLayer = localFeature.get('parentLayer');
			var config = mts.getActiveConf(parentLayer) || {};
			var configThematizer = config.analysisConf || {};
			var configMarker = config.markerConf || {};
			var configCluster = config.clusterConf || {};
			var useCache = false; //cache isn't use for analysis, just with fixed marker
			var isCluster = (Array.isArray(feature.get('features'))) ? true : false;
			var value;
			var style;
			
			mts.setActiveIndicator(config.defaultIndicator);

			if (isCluster){
				value = mts.getClusteredValue(feature);
			}else{
				value =  (props[mts.getActiveIndicator()])  ? props[mts.getActiveIndicator()].value : undefined;
			}
			
			var thematized = false;
			if (configThematizer.defaultAnalysis == 'choropleth') {
				style = mts.getChoroplethStyles(value, props, configThematizer.choropleth, configMarker);
				thematized = true;
			}else if (configThematizer.defaultAnalysis == 'proportionalSymbol') {
				style = mts.getProportionalSymbolStyles(value, props, configThematizer.proportionalSymbol);
				thematized = true;
			}
			if (!thematized && isCluster && feature.get('features').length > 1 ){
				style = mts.getClusterStyles(value, props, configCluster);
				useCache = false;
			}
			else{
				style = mts.getOnlyMarkerStyles(value, props, configMarker);
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
	    
	    mts.getClusteredValue = function (feature) { 
	    	var toReturn = 0;
	    	var total = 0;
	    	var values = [];
	    	var aggregationFunc = "";
	    	
	    	if (Array.isArray(feature.get('features'))){
	    		total = 0;
	    		for (var i=0; i<feature.get('features').length; i++){
					var tmpValue = Number((feature.get('features')[i].get(mts.getActiveIndicator())) ? feature.get('features')[i].get(mts.getActiveIndicator()).value : 0);
					aggregationFunc = (feature.get('features')[i].get(mts.getActiveIndicator())) ? feature.get('features')[i].get(mts.getActiveIndicator()).aggregationSelected : "SUM";
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
	    		toReturn += feature.get(mts.getActiveIndicator());
	    	}
			return Math.round(toReturn*100)/100; //max 2 decimals
	    }
	    
	    mts.getClusterStyles = function (value, props, config){
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
		
		mts.getProportionalSymbolStyles = function(value, props, config){
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
		                radius: mts.getProportionalSymbolSize(value, mts.getActiveIndicator(), config),
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
		
		mts.getChoroplethStyles = function(value, props, config){
			var textValue =  props[mts.getActiveIndicator()] || "";
			
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
		
		mts.getOnlyMarkerStyles = function (value, props, config){
			var style;
			var color;
			 
			if (props[mts.getActiveIndicator()] && props[mts.getActiveIndicator()].thresholdsConfig) color = mts.getColorByThresholds(value, props);
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
	
		mts.getColorByThresholds = function(value, props){
			var config = props[mts.getActiveIndicator()].thresholdsConfig;
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
								 props[mts.getActiveIndicator()]['showWarning'] = true;
							break;
						}
					}else
						break;
					idx++;
				}
				if (!isEqualOp && eval(evalText) == true) { //get the last color definition
					toReturn = thr['color']; 
					if (thr['warning'])
						 props[mts.getActiveIndicator()]['showWarning'] = true;
				}

			}
			return toReturn;
		}


		mts.updateLegend = function(layerName){
			var config = mts.getActiveConf(layerName) || {};
		}
		
//		mts.checkForDatasetValueOfIndicator = function(){
//			//if the values of the selected indicator are not present in the cache , load them
//			var listIndicator=[];
//			if(geoModule_template.analysisType=="chart"){
//				listIndicator=geoModule_template.selectedMultiIndicator;
//			}else{
//				listIndicator.push(geoModule_template.selectedIndicator);
//			}
//
//
//			for(var indic=0;indic<listIndicator.length;indic++){
//				var indicator=listIndicator[indic].name;
//				if(!mts.cacheDatasetValue.hasOwnProperty(indicator)){
//					mts.cacheDatasetValue[indicator]={};
//					for(var i=0;i<geoModule_dataset.rows.length;i++){
//						if(!mts.cacheDatasetValue[indicator].hasOwnProperty(geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name])){
//							mts.cacheDatasetValue[indicator][geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name]]={value:geoModule_dataset.rows[i][indicator],row:i};
//						}else{
//							console.info("Multi item for dataset join column ",geoModule_dataset.rows[i][geModule_datasetJoinColumnsItem.name],geModule_datasetJoinColumnsItem.header,geoModule_dataset.rows[i][indicator]);
//						}
//					}
//				}
//			}
//
//
//		}
		
		mts.getProportionalSymbolSize = function(val, name, config){
			if (!name) return 0;
			
			var minValue = mts.cacheSymbolMinMax[config.name+'|'+name].minValue;
			var maxValue = mts.cacheSymbolMinMax[config.name+'|'+name].maxValue;
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
		
		
		
		mts.getCacheSymbolMinMax=function(){
			return mts.cacheSymbolMinMax || {};
		}
		
		mts.setCacheSymbolMinMax=function(n, c){
			if (!mts.cacheSymbolMinMax) mts.cacheSymbolMinMax = {};
			mts.cacheSymbolMinMax[n] = c;
		}
		
		
		mts.getActiveIndicator=function(){
			return mts.activeInd;
		}
		
		mts.setActiveIndicator=function(i){
			mts.activeInd = i;
		}
		
		mts.loadIndicatorMaxMinVal=function(key, values){
			var minV;
			var maxV;
			for(var i=0;i<values.rows.length;i++){
				var colName = mts.getColumnName(key, values.metaData.fields);
				var tmpV= parseInt(values.rows[i][colName]);
				if(minV==undefined || tmpV<minV){
					minV=tmpV;
				}
				if(maxV==undefined || tmpV>maxV){
					maxV=tmpV;

				}
			}
			mts.setCacheSymbolMinMax(key, {minValue:minV, maxValue:maxV});
		}
		
		mts.getChoroplethColor = function(val,layerCol){
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
	
		mts.getColumnName = function(key, values){
			var toReturn = key.substring(key.indexOf('|')+1);;
			for (var v=0; v<values.length; v++){
				if (values[v].header === toReturn)
					return values[v].name;
			}
				
			return toReturn;
		}
		
		mts.getActiveConf=function(l){
			for (c in mts.activeConf){
				if (mts.activeConf[c].layer === l)
					return mts.activeConf[c].config;
			}
			console.log("Active configuration for layer ["+l+"] not found.");
			return null;
		}
		
		mts.getActiveConfIdx=function(l){
			for (var i=0; i<mts.activeConf.length; i++){
				if (mts.activeConf[i].layer === l)
					return i;
			}
			return null;
		}
		
		mts.setActiveConf=function(l, c){
			if (!mts.activeConf)
				mts.activeConf = [];
			
			var idx = mts.getActiveConfIdx(l);
			if (idx != null){
				mts.activeConf.splice(idx,1);
			} 
			mts.activeConf.push({"layer": l, "config":c});
		}
	
	}
})();