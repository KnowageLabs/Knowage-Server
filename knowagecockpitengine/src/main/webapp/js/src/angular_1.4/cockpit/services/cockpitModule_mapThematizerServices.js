(function() {
	angular
	.module("cockpitModule")
	.service("cockpitModule_mapThematizerServices",CockpitModuleMapThematizerServiceController)
		function CockpitModuleMapThematizerServiceController(
				sbiModule_translate,
				sbiModule_config,
				sbiModule_messaging,
				cockpitModule_utilstServices,
				$q,
				$mdPanel,
				$rootScope,
				$location){

		var mts = this; // mapThematizerServices

		var cacheSymbolMinMax;
		var activeInd, activeConf;

		// TODO : Replace all related logics and use styleCache2
		var styleCache = {};
		
		// 2nd version of the cache above
		var styleCache2 = new Map();
		
		// Set the style cache before layer adding
		mts.clearStyleCache = function(layerName) {
			styleCache2.set(layerName, new Map());
		}

		mts.makeStyleCache = function(layerName, layerDef) {
//			if (layerDef.visualizationType = "balloons") {
//				var balloonConf = layerDef.balloonConf;
//				var classes = balloonConf.classes || 2;
//				var minSize = balloonConf.minSize;
//				var maxSize = balloonConf.maxSize;
//				
//				if (classes < 2) {
//					classes = 2;
//				}
//
//				var ranges = math.range(minSize, maxSize, ( ( maxSize - minSize ) / ( classes-1 /* because we include the end */ ) ), true).toArray();
//
//				var styles = {};
//				for (var i=0; i<classes; i++) {
//					styles[i] = new ol.style.Style({
//						image: new ol.style.Circle({
//								radius: size,
//								fill: new ol.style.Fill({color: color}),
//								stroke: new ol.style.Stroke({color: borderColor, width: 1})
//							}),
//					});
//				}
//				
//				debugger;
//			}
			
		}
		
		function findIndicatorStats(indicator, stats) {
			return Object.values(stats).find(function(e) {
				return e.header == indicator;
			});
		}

		mts.layerStyle = function(feature, resolution){
			var localFeature;
			if (Array.isArray(feature.get('features'))) {
				localFeature = feature.get('features')[0];
			} else {
				localFeature = feature;
			}

			var props  = localFeature.getProperties();
			var parentLayer = props["parentLayer"];
			var externalLegend = props["legend"];
			var config = mts.getActiveConf(parentLayer) || {};
			var defaultIndicator = config.defaultIndicator;

			mts.setActiveIndicator(defaultIndicator);

			var visualizationType = config.visualizationType;

			var isChoropleth = visualizationType == "choropleth";
			var isHeatmap    = visualizationType == "heatmap";
			var isCluster    = visualizationType == "clusters";
			var isMarker     = visualizationType == "markers";
			var isBalloon    = visualizationType == "balloons";
			var isPie        = visualizationType == "pies";

			var configThematizer = config.analysisConf || {};
			var useCache = false; //cache isn't use for analysis, just with fixed marker
			var coordType = props["coordType"];
			var isCluster = (Array.isArray(feature.get('features'))) ? true : false;
			var measureStat = findIndicatorStats(defaultIndicator, props["stats"]);
			var value;
			var style;

			if (isCluster){
				value = mts.getClusteredValue(feature);
			}else{
				value = (props[mts.getActiveIndicator()])  ? props[mts.getActiveIndicator()].value : undefined;
			}

			if (isChoropleth) {
				// Fixed like this in KNOWAGE-8380 but... TODO because the color is determined too many times
				var extColor = mts.getColorByThresholds(value, props) || mts.getColorFromClassification(externalLegend, value, parentLayer);
				var configMarker = config.markerConf || {};
				if (coordType == "string") {
					style = mts.getMarkerStyles(externalLegend, value, parentLayer, props, configMarker, extColor);
				} else {
					var analysisConf = config.analysisConf || {};
					var borderColor = configMarker.style.borderColor;
					style = mts.getChoroplethStyles(externalLegend, value, parentLayer, extColor, borderColor);
				}
			} else if (isCluster && feature.get('features').length > 1 ){
				var configCluster = config.clusterConf || {};
				style = mts.getClusterStyles(value, parentLayer, props, configCluster);
			} else if (isBalloon) {
				var balloonConf = config.balloonConf || {};
				style = mts.getBalloonStyles(externalLegend, value, parentLayer, props, balloonConf, measureStat);
			} else if (isPie) {
				var configPie = config.pieConf || {};
				style = mts.getPieStyles(value, parentLayer, props, configPie, measureStat);
			} else {
				var configMarker = config.markerConf || {};
				style = mts.getMarkerStyles(externalLegend, value, parentLayer, props, configMarker);
			}

			return style;
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

	    mts.getClusterStyles = function (value, parentLayer, props, config){
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

		mts.getChoroplethStyles = function(externalLegend, value, parentLayer, fillColor, borderColor) {
			var color =  mts.getColorFromClassification(externalLegend, value, parentLayer) || fillColor;
			var alpha;
			var borderAlpha;

			if (!borderAlpha) borderAlpha = mts.rgbaToAlpha(borderColor) || 1;

			return  [new ol.style.Style({
				stroke: new ol.style.Stroke({
					color: borderColor || color,
					width: 2,
					opacity: borderAlpha,
				}),
				fill: new ol.style.Fill({
					color: color,
					opacity: alpha
				})
			})];

		}

		mts.getMarkerStyles = function (externalLegend, value, parentLayer, props, config, extColor) {
			var style;
			var color = extColor;
			var alpha;
			var borderColor;
			var borderAlpha; 

			if (!color) color = mts.getColorFromClassification(externalLegend, value, parentLayer);
			if (!color) color = mts.getColorByThresholds(value, props);
			if (!color) color = (config.style && config.style.color) ? config.style.color : 'grey';
//			if (!color) color = (config.style && config.style.color) ? mts.rgbaToHex(config.style.color) : 'grey';
			if (!alpha) alpha = (config.style && config.style.color) ? mts.rgbaToAlpha(config.style.color) : 1;
			if (props.coordType != "string" && !borderColor) borderColor = color;
			if (!borderColor) borderColor = (config.style && config.style.borderColor) ? config.style.borderColor : 'grey';
			if (!borderAlpha) borderAlpha = (config.style && config.style.borderColor) ? mts.rgbaToAlpha(config.style.borderColor) : 1;

			if (props.coordType === "string") {
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
						image: new ol.style.Icon({
								stroke: new ol.style.Stroke({ //border doesn't work
									color: 'red',
									width: 10
								}),
								scale: (config.scale) ? (config.scale/100) : 1,
								opacity: 1,
								crossOrigin: null,
								src: config[config.type]
							})
					});
					break;
	
				default:
					var size = (config.size || 12);
					var scale = size / 100;
					var defaultImg = mts.getDefaultMarker(value, color, props, config);
	
					style = new ol.style.Style({
						image: new ol.style.Icon({
							crossOrigin: 'anonymous',
							opacity: alpha,
							img: defaultImg,
							imgSize: [100, 100],
							scale: scale
						})
					});
	
					break;
	
				}
			} else {
				return new ol.style.Style({
						fill: new ol.style.Fill({
							color: color,
							opacity: alpha
						}),
						stroke: new ol.style.Stroke({
							color: borderColor,
							opacity: borderAlpha,
							width: (config.size) ? (config.size) : 1
						})
					});
			}

			return style;
		}

		mts.getBalloonStyles = function (externalLegend, value, parentLayer, props, config, measureStat){

			var style;
			var layerName = props["parentLayer"];

			var size = mts.getDimensionFromClassification(externalLegend, value, parentLayer);

			if (!styleCache2.get(layerName).has(size)) {
				var color;
				var borderColor = config.borderColor ? config.borderColor : "rgba(0, 0, 0, 0.5)";
				
				color = mts.getColorFromClassification(externalLegend, value, parentLayer);
				if (!color) color = mts.getColorByThresholds(value, props);
				if (!color) color = (config.color) ? config.color : "rgba(127, 127, 127, 0.5)";
				
				style = new ol.style.Style({
					image: new ol.style.Circle({
							radius: size,
							fill: new ol.style.Fill({color: color}),
							stroke: new ol.style.Stroke({color: borderColor, width: 1})
						}),
				});

				styleCache2.get(layerName).set(size, style);
			}

			style = styleCache2.get(layerName).get(size);

			return style;
		}

		mts.getPieStyles = function (value, parentLayer, props, config, measureStat){

			var pieAggregation = props["_pie_aggregation"];
			var keys = Object.keys(pieAggregation);
			var values = Object.values(pieAggregation);

			var style;
			var layerName = props["parentLayer"];
			var minSize = config.minSize;
			var maxSize = config.maxSize;
			var total = values.reduce(function(a,c) { return a+c; }, 0) / values.length;

			var unitSize = (maxSize - minSize) / measureStat.cardinality;
			var perValueSize = minSize + ((measureStat.distinct.filter(e => e <= total).length-1) * unitSize);

			var size = perValueSize;

//			if (!styleCache2.get(layerName).has(size)) {
				var borderColor = config.borderColor;
				var type = config.type;
				var fromColor = config.fromColor;
				var toColor = config.toColor;

				var tg = tinygradient([fromColor, toColor]);
				var colors= tg.rgb(keys.length);

				style = new ol.style.Style({
					image: new ol.style.Chart({
							type: type,
							radius: size,
							colors: colors,
							data: Object.values(pieAggregation),
							stroke: new ol.style.Stroke({
								color: borderColor,
								width: 1
							})
						}),
				});
				
				styleCache2.get(layerName).set(size, style);
//			}
			
			style = styleCache2.get(layerName).get(size);

			return style;
		}

		mts.defaultMarkerCache = {};

		mts.clearDefaultMarkerCache = function() {
			mts.defaultMarkerCache = {};
		}

		mts.getDefaultMarker = function(value, color, props, config) {

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
			if (props[mts.getActiveIndicator()] && props[mts.getActiveIndicator()].thresholdsConfig) {
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
			
			return null;
		}

		mts.setHeatmapWeight= function(feature){
			var parentLayer = feature.get('parentLayer');
			var config = mts.getActiveConf(parentLayer) || {};
			var layerIds = parentLayer.split("|");
			var minmaxLabel = (layerIds.length > 1) ? layerIds[1] + '|' + config.defaultIndicator : layerIds[0] + '|' + config.defaultIndicator; //minMax uses just dslabel reference
			var minmax = mts.getCacheSymbolMinMax()[minmaxLabel];
			var props  = feature.getProperties();
			var p = feature.get(config.defaultIndicator);

			if (!minmax == undefined || p.value === "") {
				return 0;
			}

			// perform calculation to get weight between 0 - 1
			// apply formule: w = w-min/max-min (http://www.statisticshowto.com/normalized/)
			weight = (((p.value - minmax.minValue)/(minmax.maxValue-minmax.minValue))*(1-0.3))+0.3;
			weight = Math.round(weight * 1000) / 1000; //round 3 digits
			return weight;
		}

		mts.updateLegend = function(externalLegend, layerName, data, legendStyle){

			console.log("Update legend for " + layerName + ":", data);

			var config = mts.getActiveConf(layerName) || {};

			console.log("\tConfig:", config);

			if (!config.defaultIndicator) {
				console.log("Choropleth thematization isn't applied because there aren't indicators (measures) defined for the layer ["+ layerName +"]");
				return;
			}

			if (config.visualizationType == 'choropleth') {
				mts.updateLegendForChoropleth(externalLegend, layerName, config, legendStyle, data);
			} else if(config.visualizationType == 'balloons') {
				mts.updateLegendForBalloons(externalLegend, layerName, config, legendStyle, data);
			} else if(config.visualizationType == 'pies') {
				mts.updateLegendForPies(externalLegend, layerName, config, legendStyle, data);
			} else {
				console.log("\tWARNING: Legend not supported for visualization type: ", config.visualizationType);
			}
		}

		mts.updateLegendForChoropleth = function(externalLegend, layerName, config, legendStyle, data) {
			if (config.analysisConf.method == "CLASSIFY_BY_EQUAL_INTERVALS") {
				mts.updateLegendForChoroplethAndIntervals(externalLegend, layerName, config, legendStyle, data);
			} else if (config.analysisConf.method == "CLASSIFY_BY_QUANTILS") {
				mts.updateLegendForChoroplethAndQuantils(externalLegend, layerName, config, legendStyle, data);
			} else if (config.analysisConf.method == "CLASSIFY_BY_RANGES") {
				mts.updateLegendForChoroplethAndRanges(externalLegend, layerName, config, legendStyle);
			} else {
				console.log("\tClassification method not supported: " + config.analysisConf.method);
			}
		}

		mts.updateLegendForChoroplethAndIntervals = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for choropleth and intervals");

			mts.setActiveIndicator(config.defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification:[] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "choropleth";
			currLegend.method = config.analysisConf.method;

			mts.updateChoroplethLegendGradient(externalLegend, layerName, config.analysisConf, config.analysisConf.classes);

			var defaultIndicator = config.defaultIndicator;
			var measureStat = findIndicatorStats(defaultIndicator, data.stats);

			var minValue = measureStat ? measureStat.min : 0;
			var maxValue = measureStat ? measureStat.max : 0;
			var split = (maxValue-minValue)/(config.analysisConf.classes);
			for (var i=0; i<config.analysisConf.classes; i++) {
				var from = (minValue+(split*i));
				var to   = (minValue+(split*(i+1)));
				
				currLegend.classification[i].from = from;
				currLegend.classification[i].to   = to;

				currLegend.classification[i].from_label = formatLegendValue(from, legendStyle);
				currLegend.classification[i].to_label   = formatLegendValue(to,   legendStyle);
			}

			if (legendStyle.visualizationType == 'Range') {
				updateLegendForRangeMode(externalLegend, layerName);
			}

			console.log("\tLegend for choropleth and intervals: ", currLegend);
		}

		mts.updateLegendForChoroplethAndQuantils = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for choropleth and quantils");

			mts.setActiveIndicator(config.defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification:[] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "choropleth";
			currLegend.method = config.analysisConf.method;

			var values=[];
			var columnName = mts.getColumnName(mts.getActiveIndicator(), data.metaData.fields);
			for (var key in data.rows) {
				if (values.indexOf(Number(data.rows[key][columnName]))==-1) {
					values.push(Number(data.rows[key][columnName]));
				}
			}
			values.sort(function sortNumber(a,b) {
				return a - b;
			});
			var intervals = Number(values.length < config.analysisConf.classes ? values.length : config.analysisConf.classes );
			mts.updateChoroplethLegendGradient(externalLegend, layerName, config.analysisConf, intervals);
			var quantils = math.quantileSeq(values, intervals);

			var binSize = Math.floor(values.length / intervals);
			var k=0;
			for (var i=0;i<values.length;i+=binSize) {
				if (k>=intervals) {
					currLegend.classification[intervals-1].to = values[i+binSize] || values[values.length-1];
				} else {
					var from = values[i];
					var to   = values[i+binSize] || values[values.length-1];

					currLegend.classification[k].from = from;
					currLegend.classification[k].to   = to;

					currLegend.classification[k].from_label = formatLegendValue(from, legendStyle);
					currLegend.classification[k].to_label   = formatLegendValue(to,   legendStyle);

					k++;
				}
			}

			console.log("\tLegend for choropleth and quantils: ", currLegend);
		}

		mts.updateLegendForChoroplethAndRanges = function(externalLegend, layerName, config, legendStyle ) {
			console.log("\tUpdate legend for choropleth and ranges");

			mts.setActiveIndicator(config.defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification:[] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "choropleth";
			currLegend.method = config.analysisConf.method;

			for (var i = config.analysisConf.properties.thresholds.length-1; i>=0; i--) {
				var threshold = config.analysisConf.properties.thresholds[i];
				currLegend.classification[i] = {
					color: threshold.color,
					itemFeatures: []
				};
				
				var from = threshold.from;
				var to   = threshold.to;
				
				currLegend.classification[i].from = from;
				currLegend.classification[i].to   = to;
				
				currLegend.classification[i].from_label = formatLegendValue(from, legendStyle);
				currLegend.classification[i].to_label   = formatLegendValue(to, legendStyle);
			}

			if (!currLegend.classification[0].from_label) {
				currLegend.classification[0].from_label = "min";
			}

			if (!currLegend.classification[currLegend.classification.length-1].to_label) {
				currLegend.classification[currLegend.classification.length-1].to_label = "max";
			}

			console.log("\tLegend for choropleth and ranges: ", currLegend);
		}

		mts.updateLegendForBalloons = function(externalLegend, layerName, config, legendStyle, data) {
			if (config.balloonConf.method == "CLASSIFY_BY_EQUAL_INTERVALS") {
				mts.updateLegendForBalloonsAndIntervals(externalLegend, layerName, config, legendStyle, data);
			} else if (config.balloonConf.method == "CLASSIFY_BY_QUANTILS") {
				mts.updateLegendForBalloonsAndQuantils(externalLegend, layerName, config, legendStyle, data);
			} else if (config.balloonConf.method == "CLASSIFY_BY_RANGES") {
				mts.updateLegendForBalloonsAndRanges(externalLegend, layerName, config, legendStyle, data);
			} else {
				console.log("\tClassification method not supported: " + config.analysisConf.method);
			}
		}

		mts.updateLegendForBalloonsAndIntervals = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for balloons and intervals");

			var balloonConf = config.balloonConf;

			var classes     = balloonConf.classes;
			var borderColor = balloonConf.borderColor;
			var fromColor   = balloonConf.fromColor;
			var toColor     = balloonConf.toColor;
			var minSize     = balloonConf.minSize;
			var maxSize     = balloonConf.maxSize;

			if (classes < 2) {
				classes = 2;
			}

			var defaultIndicator = config.defaultIndicator;
			var measureStat = findIndicatorStats(defaultIndicator, data.stats);

			var tg = tinygradient([fromColor, toColor]);
			var gradients = tg.rgb(classes);
			var dimensions = math.range(minSize, maxSize, ( ( maxSize - minSize ) / ( classes-1 /* because we include the end */ ) ), true).toArray();

			mts.setActiveIndicator(defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification: [] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.classification = [];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "balloons";
			currLegend.method = balloonConf.method;

			var minValue = measureStat.min;
			var maxValue = measureStat.max;
			var split = (maxValue-minValue) / classes;
			for (var i=0; i<classes; i++) {
				var from = (minValue+(split*i));
				var to   = (minValue+(split*(i+1)));

				currLegend.classification[i] = {};

				currLegend.classification[i].itemFeatures = [];
				currLegend.classification[i].color = gradients[i].toRgbString();
				currLegend.classification[i].borderColor = borderColor;
				currLegend.classification[i].dimension = dimensions[i];

				currLegend.classification[i].from = from;
				currLegend.classification[i].to   = to;

				currLegend.classification[i].from_label = formatLegendValue(from, legendStyle);
				currLegend.classification[i].to_label   = formatLegendValue(to,   legendStyle);
			}

			console.log("\tLegend for balloons and intervals: ", currLegend);
		}

		mts.updateLegendForBalloonsAndQuantils = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for balloons and quantils");

			var balloonConf = config.balloonConf;

			var classes     = balloonConf.classes;
			var borderColor = balloonConf.borderColor;
			var fromColor   = balloonConf.fromColor;
			var toColor     = balloonConf.toColor;
			var minSize     = balloonConf.minSize;
			var maxSize     = balloonConf.maxSize;

			if (classes < 2) {
				classes = 2;
			}

			var defaultIndicator = config.defaultIndicator;
			var measureStat = findIndicatorStats(defaultIndicator, data.stats);

			var tg = tinygradient([fromColor, toColor]);
			var gradients = tg.rgb(classes);
			var dimensions = math.range(minSize, maxSize, ( ( maxSize - minSize ) / ( classes-1 /* because we include the end */ ) ), true).toArray();

			mts.setActiveIndicator(defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification: [] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.classification = [];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "balloons";
			currLegend.method = balloonConf.method;

			var values = measureStat.distinct;

			var intervals = Number(values.length < classes ? values.length : classes);

			var binSize = Math.floor(values.length / intervals);
			var k=0;
			for (var i=0; i < values.length; i += binSize) {
				if (k>=intervals) {
					currLegend.classification[intervals-1].to = values[i+binSize] || values[values.length-1];

					currLegend.classification[intervals-1].to_label = formatLegendValue(values[i+binSize] || values[values.length-1], legendStyle);
				} else {
					var from = values[i];
					var to   = values[i+binSize] || values[values.length-1];

					currLegend.classification[k] = {};

					currLegend.classification[k].itemFeatures = [];
					currLegend.classification[k].color = gradients[k].toRgbString();
					currLegend.classification[k].borderColor = borderColor;
					currLegend.classification[k].dimension = dimensions[k];

					currLegend.classification[k].from = from;
					currLegend.classification[k].to   = to;

					currLegend.classification[k].from_label = formatLegendValue(from, legendStyle);
					currLegend.classification[k].to_label   = formatLegendValue(to,   legendStyle);

					k++;
				}
			}

			console.log("\Legend for balloons and quantils: ", currLegend);
		}

		mts.updateLegendForBalloonsAndRanges = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for balloons and ranges");

			var balloonConf = config.balloonConf;

			var borderColor = balloonConf.borderColor;
			var classes     = balloonConf.properties.thresholds.length;
			var minSize     = balloonConf.minSize;
			var maxSize     = balloonConf.maxSize;

//			if (classes < 2) {
//				classes = 2;
//			}

			var defaultIndicator = config.defaultIndicator;
			var measureStat = findIndicatorStats(defaultIndicator, data.stats);

			var dimensions = math.range(minSize, maxSize, ( ( maxSize - minSize ) / ( classes-1 /* because we include the end */ ) ), true).toArray();

			mts.setActiveIndicator(defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification:[] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "balloons";
			currLegend.method = balloonConf.method;

			for (var i = balloonConf.properties.thresholds.length-1; i>=0; i--) {
				var threshold = balloonConf.properties.thresholds[i];
				currLegend.classification[i] = {
					color: threshold.color,
					borderColor: borderColor,
					dimension: dimensions[i],
					itemFeatures: []
				};
				
				var from = threshold.from;
				var to   = threshold.to;
				
				currLegend.classification[i].from = from;
				currLegend.classification[i].to   = to;
				
				currLegend.classification[i].from_label = formatLegendValue(from, legendStyle);
				currLegend.classification[i].to_label   = formatLegendValue(to, legendStyle);
			}

			console.log("\tLegend for balloons and ranges: ", currLegend);
		}

		mts.updateLegendForPies = function(externalLegend, layerName, config, legendStyle, data) {
			console.log("\tUpdate legend for choropleth and intervals");
			
			var pieConf = config.pieConf;

			var category    = pieConf.categorizeBy;
			var borderColor = pieConf.borderColor;
			var fromColor   = pieConf.fromColor;
			var toColor     = pieConf.toColor;

			var categoryStats = findIndicatorStats(category, data.stats);
			var categoryCardinality = categoryStats.cardinality;
			var categoryDistinct = categoryStats.distinct;
			

			var classes = categoryCardinality;
			var defaultIndicator = config.defaultIndicator;

			var tg = tinygradient([fromColor, toColor]);
			var gradients = tg.rgb(classes);

			mts.setActiveIndicator(defaultIndicator);

			if (!externalLegend[layerName]) {
				externalLegend[layerName] = { classification:[] };
			}

			var currLegend = externalLegend[layerName];

			currLegend.layer = layerName;
			currLegend.alias = config.alias;
			currLegend.visualizationType = "pies";

			for (var i=0; i<classes; i++) {
				currLegend.classification[i] = {};

				currLegend.classification[i].category = categoryDistinct[i];
				currLegend.classification[i].color = gradients[i].toRgbString();
				currLegend.classification[i].borderColor = borderColor;
			}

		}

		var updateLegendForRangeMode = function(externalLegend, layerName){
			var currLegend = externalLegend[layerName];
			var from = currLegend.classification[0].from;
			var to = currLegend.classification[0].to;
			var onlyOneRange = true;
			for (var i=0; i<currLegend.classification.length; i++) {
				if (currLegend.classification[i].from != from || currLegend.classification[i].to != to) {
					onlyOneRange = false;
					break;
				}
			}
			if (onlyOneRange) {
				var choropletToKeep = currLegend.classification[0];
				currLegend.classification = []; // reset choroplets
				currLegend.classification[0] = choropletToKeep;
			}

			var dataNotAvailable = true;
			for (var i=0; i<currLegend.classification.length; i++) {
				if (!isNaN(currLegend.classification[i].from) || !isNaN(currLegend.classification[i].to)) {
					dataNotAvailable = false;
					break;
				}
			}
			if (dataNotAvailable) {
				currLegend.dataNotAvailable = true;
			} else {
				currLegend.dataNotAvailable = false;
			}
		}

		var formatLegendValue = function (val, style){
			if (val == null) return null;

			var prefix = "";
			var suffix = "";
			var precision = 6;

			if (style && style.format && style.format.precision) precision = style.format.precision;
			if (style && style.format && style.format.prefix) prefix = style.format.prefix;
			if (style && style.format && style.format.suffix) suffix = style.format.suffix;

			var decimalFormatted = val.toFixed(precision).replace(/0{0,6}$/, "").replace(/\.0?$/, "");
			var localeFormatted = decimalFormatted.toLocaleString(sbiModule_config.curr_language);
			return prefix + localeFormatted + suffix;
		}

		mts.updateChoroplethLegendGradient = function(externalLegend, layerName, chorConfig, numberGradient){
			var tg = tinygradient([chorConfig.fromColor, chorConfig.toColor]);
			var gradients= tg.rgb(numberGradient == 1 ? 2 : numberGradient); // ternary operator required to handle single line dataset
			var currLegend = externalLegend[layerName];
			currLegend.classification.length=0;

			for(var i=0; i < Math.min(numberGradient, gradients.length); i++){
				var  tmpGrad={};
				tmpGrad.color = gradients[i].toRgbString();
				tmpGrad.item = 0; //number of features in this range
				tmpGrad.itemFeatures = []; //features in this range
				currLegend.classification.push(tmpGrad);
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

		mts.getColorFromClassification = function(externalLegend, val, layerName){
			if (!externalLegend || (externalLegend && !externalLegend[layerName])) {
				return;
			}

			var currLegend = externalLegend[layerName];
			var classification = currLegend.classification;
			var currIndicator = mts.getActiveIndicator();
			var ret;
			var value = Number(val);

			for(var i=0; i < classification.length; i++){
				if(Number(val) >= Number(classification[i].from) && Number(val) < Number(classification[i].to)){
					ret = classification[i].color;
					if(classification[i].itemFeatures.indexOf(currIndicator) == -1){
						classification[i].itemFeatures.push(currIndicator);
						classification[i].item++;
					}
					break;
				}
			}
			if(ret == undefined){
				ret = classification[classification.length-1].color;
				if(classification[classification.length-1].itemFeatures.indexOf(currIndicator)==-1){
					classification[classification.length-1].itemFeatures.push(currIndicator);
					classification[classification.length-1].item++;
				}
			}
			return ret;
		}

		mts.getDimensionFromClassification = function(externalLegend, val, layerName){
			if (!externalLegend[layerName]) {
				return;
			}

			var currLegend = externalLegend[layerName];
			var classification = currLegend.classification;
			var currIndicator = mts.getActiveIndicator();
			var ret;
			var value = Number(val);

			for(var i=0; i < classification.length; i++){
				if(Number(val) >= Number(classification[i].from) && Number(val) < Number(classification[i].to)){
					ret = classification[i].dimension;
					if(classification[i].itemFeatures.indexOf(currIndicator) == -1){
						classification[i].itemFeatures.push(currIndicator);
						classification[i].item++;
					}
					break;
				}
			}
			if(ret == undefined){
				ret = classification[classification.length-1].dimension;
				if(classification[classification.length-1].itemFeatures.indexOf(currIndicator)==-1){
					classification[classification.length-1].itemFeatures.push(currIndicator);
					classification[classification.length-1].item++;
				}
			}
			return ret;
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
