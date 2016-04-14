(function () {
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.service('kpiViewerGaugeService', function () {
		var kpiViewerGaugeService = {
			gauge : null,
			
			createGauge : function(
					frameId, 
					label, 
					size, 
					min, 
					max, 
					threshold, 
					showValue,
					showThresholds,
					valuePrecision,
					fontConf) {
				
				var config = {
					size : undefined != size ? size: 100,
					label : label,
					min : undefined != min ? min : 0,
					max : undefined != max ? max : 100,
					showValue : undefined != showValue && null != showValue ? 
							showValue : true,
					showThresholds : undefined != showThresholds && null != showThresholds ? 
							showThresholds : true,
					valuePrecision : undefined != valuePrecision && null != valuePrecision? 
							valuePrecision : 0,
					fontConf : undefined != fontConf && null != fontConf? 
							fontConf : 
							{
								size : "1",
								color : "black",
								fontFamily : "Times New Roman",
								fontweight : "normal"
							},
							
					minorTicks : 5
				};

				var range = config.max - config.min;

				if(threshold && threshold != null) {
					var limits = {
						min: config.min,
						max: config.max
					};
					
					var stopsConf = kpiViewerGaugeService.generateStopsConf(threshold, limits);
					
					config.stops = stopsConf.stops;
					
					if(stopsConf.newMin < config.min) {
						config.min = stopsConf.newMin;
					}
					if(stopsConf.newMax > config.max) {
						config.max = stopsConf.newMax;
					}
				}
				
				kpiViewerGaugeService.gauge = new Gauge(frameId, config);
				kpiViewerGaugeService.gauge.render();
			},
			
			updateGauge : function(newValue, newTransitionValue) {
				newValue = newValue || 0;
				kpiViewerGaugeService.gauge.redraw(newValue);
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
			generateStopsConf : function(threshold, limits) {
				var thresholdsQuantity = threshold.thresholdValues.length;
				
				var thresholds = {};
				
				var MIN_VALUE = -(Number.MAX_VALUE);
				
				var lowestLimit = (limits.min < Number.MAX_VALUE)? limits.min : Number.MAX_VALUE ;
				var highestLimit = (limits.max > MIN_VALUE)? limits.max : MIN_VALUE;
				
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
							stopConf.to = highestLimit;
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

		
		return kpiViewerGaugeService;
	});
})();