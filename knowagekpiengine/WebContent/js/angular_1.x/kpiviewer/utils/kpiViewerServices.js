(function () {
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.service('kpiViewerServices', function () {
		var kpiViewerServices = {
			GAUGE_DEFAULT_SIZE: 250,
			LINEAR_GAUGE_DEFAULT_SIZE: 400,
			
			gauge : null,
			
			createWidgetConfiguration : function(templateKpi, kpiValue, templateOptions, templateStyle) {

				
				var conf = {};
				
				conf.id = templateKpi.id;
				conf.name = kpiValue.name;
				conf.version = templateKpi.version;
				conf.viewAs = templateKpi.vieweas && templateKpi.vieweas != '' ?
						templateKpi.vieweas : templateOptions.vieweas;
				conf.size = kpiViewerServices.GAUGE_DEFAULT_SIZE;
				conf.minValue = templateKpi.rangeMinValue;
				conf.maxValue = templateKpi.rangeMaxValue;
				conf.value = kpiValue.value;
				conf.targetValue = kpiValue.targetValue;
				conf.thresholdStops = [];
				conf.showValue = templateOptions.showvalue;
				conf.showTarget = templateOptions.showtarget;
				conf.showTargetPercentage = templateOptions.showtargetpercentage;
				conf.showThreshold = templateOptions.showthreshold;
				conf.fontConf = templateStyle.font;
				conf.precision = templateOptions.history && templateOptions.history.size ?
						templateOptions.history.size : null;
				conf.units = templateOptions.history && templateOptions.history.units ?
						templateOptions.history.units : null;
				
				if(kpiValue.threshold && kpiValue.threshold != null) {
					var limits = {
						min: conf.minValue,
						max: conf.maxValue
					};
					
					var stopsConf = kpiViewerServices.generateStopsConf(
							kpiValue.threshold, limits, conf.viewAs);
					
					conf.thresholdStops = stopsConf.stops;
					
					if(stopsConf.newMin < conf.minValue) {
						conf.minValue = stopsConf.newMin;
					}
					if(stopsConf.newMax > conf.maxValue) {
						conf.maxValue = stopsConf.newMax;
					}
					
					if(conf.size < (conf.maxValue - conf.minValue)) {
						conf.size = (conf.maxValue - conf.minValue)
					}
				}
				
				//console.log('createWidgetConfiguration -> ' ,conf);
				return conf;
			},
			
			
			/**
			 * generates an object containing the stops data, the minimum and maximum values registered
			 * 
			 *  @returns 
			 *  {
			 *  	stops: Object(),
			 *  	min: Number,
			 *  	max: Number
			 *  }
			 */
			generateStopsConf : function(threshold, limits, vieweas) {
				vieweas = (vieweas || 'speedometer').toLowerCase();
				var thresholdsQuantity = threshold.thresholdValues.length;
				
				var thresholds = {};
				
				var MIN_VALUE = -(Number.MAX_VALUE);
				
				var lowestLimit = Number((limits.min < Number.MAX_VALUE)? limits.min : Number.MAX_VALUE) ;
				var highestLimit = Number((limits.max > MIN_VALUE)? limits.max : MIN_VALUE);
				
				for(var i = 0; i < threshold.thresholdValues.length; i++) {
					var thresholdValue = threshold.thresholdValues[i];
					
					var thresholdValuePosition = thresholdValue.position - 1;
					
					// Be sure the thresholds are sorted
					thresholds[thresholdValuePosition] = thresholdValue;
					
					if(thresholdValue.minValue != null) {
						lowestLimit = (thresholdValue.minValue < lowestLimit)? thresholdValue.minValue : lowestLimit;
					}
					if(thresholdValue.maxValue != null) {
						highestLimit = (thresholdValue.maxValue > highestLimit)? thresholdValue.maxValue : highestLimit;
						
					}
				}
				
//				if(vieweas == 'kpicard') {
//					highestLimit = highestLimit * 1.5;
//				}
				
				var stops = [];
				for(var i = 0; i < thresholdsQuantity; i++) {
					var threshold = thresholds[i];
					
					var stopConf = {};
					
					//first threshold
					if(i == 0){
						if(threshold.minValue == null || threshold.minValue < lowestLimit) {
							stopConf.from = lowestLimit;
						} else {
							stopConf.from = threshold.minValue;
						}
						
						stopConf.to = threshold.maxValue;
						
					}
					// last threshold
					else if (i == thresholdsQuantity - 1) {
						if(threshold.maxValue == null || threshold.maxValue > highestLimit) {
//							stopConf.to = highestLimit;
							stopConf.to = (vieweas == 'kpicard') ? highestLimit * 1.5 : highestLimit;
						} else {
							stopConf.to = threshold.maxValue;
						}

						stopConf.from = threshold.minValue;
					}
					// intermediate thresholds
					else {
						stopConf.to = threshold.maxValue;
						stopConf.from = threshold.minValue;
					}
					
					
					stopConf.color = threshold.color;
					
					stops.push(stopConf);
				}
				
				return {
					stops: stops,
					newMin: lowestLimit,
					newMax: highestLimit
				};
			},
		};
		
		return kpiViewerServices;
	});
})();