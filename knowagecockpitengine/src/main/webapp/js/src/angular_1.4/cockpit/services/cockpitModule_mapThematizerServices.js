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

		var cacheSymbolMinMax;
		var activeInd, activeConf, activeLegend;


		var styleCache = {};
	    mts.layerStyle = function(feature, resolution){
	    	var featureType = feature.getGeometry().getType();

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

			if (config.visualizationType == 'choropleth') {
				configThematizer.parentLayer = parentLayer;
				if (!configMarker.style) configMarker.style = {};
				if (localFeature.get('isSimpleMarker'))
					configMarker.style.color = mts.getChoroplethColor(value, parentLayer) || "grey";
				else{
					style = mts.getChoroplethStyles(value, parentLayer, null);
					thematized = true;
				}
			}

			if (!localFeature.get('isSimpleMarker')){
				var fillColor   = (configMarker.style && configMarker.style.color)       ? configMarker.style.color       : "grey";
				var borderColor = (configMarker.style && configMarker.style.borderColor) ? configMarker.style.borderColor : undefined;

				if (props[mts.getActiveIndicator()]
						&& props[mts.getActiveIndicator()].thresholdsConfig
						&& props[mts.getActiveIndicator()].thresholdsConfig.length != 0) {
					fillColor = mts.getColorByThresholds(value, props) || fillColor;
				}

				style = mts.getChoroplethStyles(value, parentLayer, fillColor, borderColor);
				thematized = true;
			}


			if (!thematized && isCluster && feature.get('features').length > 1 ){
				style = mts.getClusterStyles(value, props, configCluster);
				useCache = false;
			}
			else if (!thematized){
				style = mts.getOnlyMarkerStyles(value, props, configMarker);
				useCache = true;
			}

			if (useCache && !styleCache[parentLayer]) {
		          styleCache[parentLayer] = style;
		          return styleCache[parentLayer] ;
			} else {
				return style;
			}
	    }

	    mts.trim = function (str) {
	    	return str.replace(/^\s+|\s+$/gm,'');
	    }

	    mts.rgbaToHex = function (rgba) {
	        var parts = rgba.substring(rgba.indexOf("(")).split(","),
	            r = parseInt(mts.trim(parts[0].substring(1)), 10),
	            g = parseInt(mts.trim(parts[1]), 10),
	            b = parseInt(mts.trim(parts[2]), 10);

	        return ('#' + r.toString(16) + g.toString(16) + b.toString(16));
	    }

	    mts.rgbaToAlpha = function (rgba) {
	        var parts = rgba.substring(rgba.indexOf("(")).split(","),
	        	alpha = '1'; //default is opaque
	        if (parts[3])
	        	alpha = parseFloat(mts.trim(parts[3].substring(0, parts[3].length - 1))).toFixed(2);

	        return alpha;
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
	                  color: (config.style && config.style['background-color']) ? config.style['background-color'] : 'grey'
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
		                 color : config.style['color'] || 'grey',
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

		mts.getChoroplethStyles = function(value, parentLayer, fillColor, borderColor) {
			var color =  mts.getChoroplethColor(value, parentLayer) || fillColor;

			return  [new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: borderColor || color,
					width: 2
				}),
				fill: new ol.style.Fill({
					color: color
				})
			})];

		}

		mts.getOnlyMarkerStyles = function (value, props, config){
			var style;
			var color;
			var alpha;

			if (props[mts.getActiveIndicator()] && props[mts.getActiveIndicator()].thresholdsConfig) color = mts.getColorByThresholds(value, props);
			if (!color)	color =  (config.style && config.style.color) ? config.style.color : 'grey';
//			if (!color)	color =  (config.style && config.style.color) ? mts.rgbaToHex(config.style.color) : 'grey';
			if (!alpha) alpha = (config.style && config.style.color) ?  mts.rgbaToAlpha(config.style.color) : 1;


			switch(config.type) {

			case "icon":
				//font-awesome
				var size = config.size || 100;
				style = new ol.style.Style({
					text: new ol.style.Text({
							text: config.icon.unicode,
							font: '' + config.icon.fontWeight + ' ' + ((2*size) + '% ') + '"' + config.icon.fontFamily + '"',
							fill: new ol.style.Fill({
								color: color,
								opacity: alpha
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
					crossOrigin: null,
					src: config[config.type]
					}))
				});
				break;

			default:
				var size = (config.size || 12);
				var scale = size / 100;
				var defaultImg = mts.getDefaultMarker(value, props, config);

				style =  new ol.style.Style({
				image: new ol.style.Icon(
						/** @type {olx.style.IconOptions} */
					({
						crossOrigin: 'anonymous',
						opacity: alpha,
						img: defaultImg,
						imgSize: [100, 100],
						scale: scale
					})
				)});
				break;

			}
			return style;
		}

		mts.defaultMarkerCache = {};

		mts.clearDefaultMarkerCache = function() {
			mts.defaultMarkerCache = {};
		}

		mts.getDefaultMarker = function(value, props, config) {
			var color;

			if (props[mts.getActiveIndicator()] && props[mts.getActiveIndicator()].thresholdsConfig) color = mts.getColorByThresholds(value, props);
			if (!color)	color =  (config.style && config.style.color) ? config.style.color : 'grey';

			var layerName = props.parentLayer;
			var key = "" + layerName + "|" + color;

			// A cache to maintaing styles for the marker
			if (!(key in mts.defaultMarkerCache)) {
				var defaultImg = new Image();
				defaultImg.src = 'data:image/svg+xml,' + encodeURIComponent('<svg xmlns="http://www.w3.org/2000/svg" version="1.1" width="120" height="120"><circle r="42.711815" cy="50" cx="50" style="opacity:1;fill:' + color + ';fill-opacity:1;stroke:#000000;stroke-width:14.5763731;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-opacity:1;paint-order:markers fill stroke" /></svg>');
				mts.defaultMarkerCache[key] = defaultImg;
			}

			return mts.defaultMarkerCache[key];
		}

		mts.getColorByThresholds = function(value, props){
			var config = props[mts.getActiveIndicator()].thresholdsConfig;
			var toReturn = undefined;
			var isEqualOp = false;

			for (c in config){
				var evalText = "";
				var thr = config[c];
				var idx = 0;
				value= Number(value); //force conversion to number type
				for (t in thr){
					if (!isNaN(value) && typeof(thr['operator'+idx]) != 'undefined' && typeof(thr['val'+idx]) != 'undefined'){
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

		mts.setHeatmapWeight= function(feature){
			var parentLayer = feature.get('parentLayer');
			var config = mts.getActiveConf(parentLayer) || {};
			var layerIds = parentLayer.split("|");
			var minmaxLabel = (layerIds.length > 1) ? layerIds[1] + '|' + config.defaultIndicator : layerIds[0] + '|' + config.defaultIndicator; //minMax uses just dslabel reference
			var minmax = mts.getCacheSymbolMinMax()[minmaxLabel];
			var props  = feature.getProperties();
		    var p = feature.get(config.defaultIndicator);

		    if (!minmax == undefined || p.value === "")
		    	return 0;

		    // perform calculation to get weight between 0 - 1
		    // apply formule: w = w-min/max-min (http://www.statisticshowto.com/normalized/)
//		    weight = (p.value - minmax.minValue)/(minmax.maxValue-minmax.minValue);
		    weight = (((p.value - minmax.minValue)/(minmax.maxValue-minmax.minValue))*(1-0.3))+0.3;
		    weight = Math.round(weight * 1000) / 1000; //round 3 digits
//		    console.log("p.value: " + p.value + " - weight: ", weight );
		    return weight;
		}

		mts.getLegend = function (referenceId){
			var toReturn = [];
			for (l in mts.activeLegend){
				var colors = "";
				var limits = [];
				var tmpLayerName = l.split("|");
				if (tmpLayerName[0] == referenceId && mts.activeLegend[l] && mts.activeLegend[l].choroplet){
					if (mts.activeLegend[l].method=="CLASSIFY_BY_RANGES") {
						var ranges = [];
						for (c in mts.activeLegend[l].choroplet){
							var tmpConf = mts.activeLegend[l].choroplet[c];
							if (tmpConf.from == null) tmpConf.from = "min";
							if (tmpConf.to == null) tmpConf.to = "max";
							ranges.push({"color": tmpConf.color, "from": tmpConf.from, "to":tmpConf.to});
						}
						toReturn.push({"layer": tmpLayerName[1], "alias": mts.activeLegend[l].alias, "method": mts.activeLegend[l].method, "ranges": ranges});
					} else {
						for (c in mts.activeLegend[l].choroplet){
							var tmpConf = mts.activeLegend[l].choroplet[c];
							if (tmpConf.color) colors += ", " + tmpConf.color;
							if (limits.length == 0) limits.push(tmpConf.from);
							if (limits.length >= 1) limits.splice(1, 1, tmpConf.to);
						}
						toReturn.push({"layer": tmpLayerName[1], "alias": mts.activeLegend[l].alias, "method": mts.activeLegend[l].method, "colors": colors, "limits": limits});
					}
				}
			}
			return toReturn;
		}

		mts.updateLegend = function(layerName, data){
			var config = mts.getActiveConf(layerName) || {};

			if (!config.visualizationType || config.visualizationType != 'choropleth') return; //legend is created just with choropleth

			if (!config.defaultIndicator) {
				console.log("Choroplet thematization isn't applied because there aren't indicators (measures) defined for the layer ["+ layerName +"]");
				return;
			}

			if (config.visualizationType == 'choropleth' && config.analysisConf){

				if (!mts.activeLegend) mts.activeLegend = {};

				mts.setActiveIndicator(config.defaultIndicator);

				if (!mts.activeLegend[layerName]){
					mts.activeLegend[layerName] = {choroplet:[]};
				}
				mts.activeLegend[layerName].alias = config.alias;
				mts.activeLegend[layerName].method = config.analysisConf.method;

				if(config.analysisConf.method == "CLASSIFY_BY_EQUAL_INTERVALS"){
					mts.updateChoroplethLegendGradient(layerName, config.analysisConf, config.analysisConf.classes);
					var layerNameForMinMAx = layerName.split("|");

					var minValue = mts.cacheSymbolMinMax[layerNameForMinMAx[1] + '|' + mts.getActiveIndicator()].minValue;
					var maxValue = mts.cacheSymbolMinMax[layerNameForMinMAx[1] + '|' + mts.getActiveIndicator()].maxValue;
					var split = (maxValue-minValue)/(config.analysisConf.classes);
					for(var i=0; i<config.analysisConf.classes; i++){
						mts.activeLegend[layerName].choroplet[i].from=(minValue+(split*i)).toFixed(2);
						mts.activeLegend[layerName].choroplet[i].to=(minValue+(split*(i+1))).toFixed(2);
					}
//					console.log("Regular intervals legends: ", mts.activeLegend[layerName]);
				}else if (config.analysisConf.method == "CLASSIFY_BY_QUANTILS"){
					//classify by quantils
					var values=[];
					var columnName =  mts.getColumnName(mts.getActiveIndicator(), data.metaData.fields);
					for(var key in data.rows){
						if(values.indexOf(Number(data.rows[key][columnName]))==-1){
							values.push(Number(data.rows[key][columnName]));
						}
					}
					values.sort(function sortNumber(a,b) {
						return a - b;
					});
					var intervals = Number(values.length < config.analysisConf.classes ? values.length : config.analysisConf.classes );
					mts.updateChoroplethLegendGradient(layerName, config.analysisConf, intervals);
					var quantils = math.quantileSeq(values, intervals);

					var binSize = Math.floor(values.length  / intervals);
					var k=0;
					for(var i=0;i<values.length;i+=binSize){
						if(k>=intervals){
							mts.activeLegend[layerName].choroplet[intervals-1].to = values[i+binSize] || values[values.length-1];
						}else{
							mts.activeLegend[layerName].choroplet[k].from = values[i];
							mts.activeLegend[layerName].choroplet[k].to = values[i+binSize] || values[values.length-1];
							k++;
						}
					}
//					console.log("Quantils legends: ", mts.activeLegend[layerName]);
				} else if (config.analysisConf.method == "CLASSIFY_BY_RANGES") {
					for(var i=config.analysisConf.properties.thresholds.length-1; i>=0; i--){
						var threshold = config.analysisConf.properties.thresholds[i];
						mts.activeLegend[layerName].choroplet[i] = {color: threshold.color, itemFeatures: []};
						mts.activeLegend[layerName].choroplet[i].from=threshold.from;
						mts.activeLegend[layerName].choroplet[i].to=threshold.to;
					}
				} else {
					console.log("Temathization method [" + config.analysisConf.method + "] not supported");
				}
			}
		}

		mts.removeLegends = function (){
			 mts.activeLegend = {};
		}

		mts.updateChoroplethLegendGradient = function(layerName, chorConfig, numberGradient){
			var grad = tinygradient([chorConfig.fromColor, chorConfig.toColor]);
			var gradienti= grad.rgb(numberGradient == 1 ? 2 : numberGradient); // ternary operator required to handle single line dataset
			mts.activeLegend[layerName].choroplet.length=0;

			for(var i=0; i < gradienti.length; i++){
				var  tmpGrad={};
				tmpGrad.color = gradienti[i].toRgbString();
				tmpGrad.item = 0; //number of features in this range
				tmpGrad.itemFeatures = []; //features in this range
				mts.activeLegend[layerName].choroplet.push(tmpGrad);
			}
		}

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
				size = ( Number(val) - minValue) / ( maxValue - minValue) * (maxRadiusSize - minRadiusSize) + minRadiusSize;
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
				var tmpV= Number(values.rows[i][colName]);
				if(minV==undefined || tmpV<minV){
					minV=tmpV;
				}
				if(maxV==undefined || tmpV>maxV){
					maxV=tmpV;

				}
			}
			mts.setCacheSymbolMinMax(key, {minValue:minV, maxValue:maxV});
		}

		mts.getChoroplethColor = function(val,layerName){
			if (!mts.activeLegend || !mts.activeLegend[layerName]) return;

			var color;
			var value = Number(val);

			for(var i=0; i < mts.activeLegend[layerName].choroplet.length; i++){
				if(Number(val) >= Number( mts.activeLegend[layerName].choroplet[i].from) && Number(val) < Number( mts.activeLegend[layerName].choroplet[i].to)){
					color = mts.activeLegend[layerName].choroplet[i].color;
					if( mts.activeLegend[layerName].choroplet[i].itemFeatures.indexOf(mts.getActiveIndicator())==-1){
						 mts.activeLegend[layerName].choroplet[i].itemFeatures.push(mts.getActiveIndicator());
						 mts.activeLegend[layerName].choroplet[i].item++;
					}
					break;
				}
			}
			if(color==undefined){
				color= mts.activeLegend[layerName].choroplet[ mts.activeLegend[layerName].choroplet.length-1].color;
				if( mts.activeLegend[layerName].choroplet[ mts.activeLegend[layerName].choroplet.length-1].itemFeatures.indexOf(mts.getActiveIndicator())==-1){
					 mts.activeLegend[layerName].choroplet[ mts.activeLegend[layerName].choroplet.length-1].itemFeatures.push(mts.getActiveIndicator());
					 mts.activeLegend[layerName].choroplet[ mts.activeLegend[layerName].choroplet.length-1].item++;
				}
			}
			return color;
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