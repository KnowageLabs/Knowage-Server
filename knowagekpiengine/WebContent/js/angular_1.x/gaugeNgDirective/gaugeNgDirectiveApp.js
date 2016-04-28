(function() {	
	/**
	 * Downloaded from http://bl.ocks.org/tomerd/1499279
	 * 
	 * Gauge Element extended by benedetto.milazzo@eng.it
	 * @requires d3 chart library
	 */

	function Gauge(placeholderName, configuration) {
		this.placeholderName = placeholderName;

		var self = this; // for internal d3 functions

		this.configure = function (configuration) {
			this.config = configuration;

			this.config.radius = this.config.size * 0.97 / 2;
			this.config.cx = this.config.size / 2;
			this.config.cy = this.config.size / 2;

			this.config.min = undefined != configuration.min ? configuration.min : 0;
			this.config.min = Number(this.config.min);
			
			this.config.max = undefined != configuration.max ? configuration.max : 100;
			this.config.max = Number(this.config.max);

			this.config.range = this.config.max - this.config.min;
			
			this.config.showValue = configuration.showValue != undefined ? configuration.showValue : true;
			this.config.showTarget = configuration.showTarget != undefined ? configuration.showTarget : true;
			this.config.valuePrecision = configuration.valuePrecision != undefined ? configuration.valuePrecision : 0;
			this.config.fontConf = configuration.fontConf != undefined && configuration.fontConf != null ? 
					configuration.fontConf : {
						size : 1,
						color : "black",
						fontFamily : "Times New Roman",
						fontweight : "normal"
					};

			this.config.majorTicks = configuration.majorTicks || 5;
			this.config.majorTicks = Number(this.config.majorTicks);

			this.config.minorTicks = configuration.minorTicks || 2;
			this.config.minorTicks = Number(this.config.minorTicks);

			this.config.transitionDuration = configuration.transitionDuration || 500;
		};

		this.render = function () {
			d3.select("#" + this.placeholderName).html("");
			d3.select("#" + this.placeholderName).selectAll("*").remove();
			
			this.body = d3.select("#" + this.placeholderName)
				.append("svg:svg")
				.attr("class", "gauge")
				.attr("width", this.config.size)
				.attr("height", this.config.size);

			// external circle
			this.body.append("svg:circle")
				.attr("cx", this.config.cx)
				.attr("cy", this.config.cy)
				.attr("r", this.config.radius)
				.style("fill", "rgb(214, 230, 246)")
				.style("stroke", "rgb(59, 103, 140)")
				.style("stroke-width", "0.5px");

			// external circle inside the first one
			this.body.append("svg:circle")
				.attr("cx", this.config.cx)
				.attr("cy", this.config.cy)
				.attr("r", 0.9 * this.config.radius)
				.style("fill", "#fff")
				.style("stroke", "rgb(59, 103, 140)")
				.style("stroke-width", "0.5px");

			if(this.config.showThresholds) {
				for (var index in this.config.stops) {
					var stop = this.config.stops[index];
					
					this.drawBand(stop.from, stop.to, stop.color);
				}
			}

			if (undefined != this.config.label) {
				var fontSize = Math.round(this.config.size * this.config.fontConf.size / 9);

				// label
				this.body.append("svg:text")
					.attr("x", this.config.cx)
					.attr("y", this.config.cy / 2 + fontSize / 2)
					.attr("dy", fontSize / 2)
					.attr("text-anchor", "middle")
					.text(this.config.label)
					.style("font-size", fontSize + "px")
					.style("fill", this.config.fontConf.color)
					.style("font-family", this.config.fontConf.fontFamily)
					.style("font-weight", this.config.fontConf.fontWeight)
					.style("stroke-width", "0px");
			}

			var fontSize = Math.round(this.config.size * this.config.fontConf.size / 16);
			var majorDelta = this.config.range / (this.config.majorTicks - 1);
			for (var major = this.config.min; major <= this.config.max; major += majorDelta) {
				var minorDelta = majorDelta / this.config.minorTicks;
				for (var minor = major + minorDelta; minor < Math.min(major + majorDelta, this.config.max); minor += minorDelta) {
					var point1 = this.valueToPoint(minor, 0.75);
					var point2 = this.valueToPoint(minor, 0.85);

					this.body.append("svg:line")
						.attr("x1", point1.x)
						.attr("y1", point1.y)
						.attr("x2", point2.x)
						.attr("y2", point2.y)
						.style("stroke", "rgb(59, 103, 140)")
						.style("stroke-width", "1px");
				}

				var point1 = this.valueToPoint(major, 0.7);
				var point2 = this.valueToPoint(major, 0.85);

				this.body.append("svg:line")
					.attr("x1", point1.x)
					.attr("y1", point1.y)
					.attr("x2", point2.x)
					.attr("y2", point2.y)
					.style("stroke", "rgb(59, 103, 140)")
					.style("stroke-width", "2px");

				if (major == this.config.min || major == this.config.max) {
					var point = this.valueToPoint(major, 0.63);

					this.body.append("svg:text")
						.attr("x", point.x)
						.attr("y", point.y)
						.attr("dy", fontSize / 3)
						.attr("text-anchor", major == this.config.min ? "start" : "end")
						.text(major)
						.style("font-size", fontSize + "px")
						.style("fill", this.config.fontConf.color)
						.style("font-family", this.config.fontConf.fontFamily)
						.style("font-weight", this.config.fontConf.fontWeight)
						.style("stroke-width", "0px");
				}
			}
			
			var targetPoint1 = this.valueToPoint(0, 0.55);
			var targetPoint2 = this.valueToPoint(0, 0.85);
			
			// line.target
			this.targetLine = this.body.append("svg:line")
				.attr("class", 'target')
				.style("visibility", this.config.showTarget ? "visible" : "hidden")
				.attr("x1", targetPoint1.x)
				.attr("y1", targetPoint1.y)
				.attr("x2", targetPoint2.x)
				.attr("y2", targetPoint2.y)
				.style("stroke", "#AC0A08")
				.style("stroke-width", "3px");
			
				
			var pointerContainer = 
				this.body.append("svg:g").attr("class", "pointerContainer");

			var midValue = (this.config.min + this.config.max) / 2;

			var pointerPath = this.buildPointerPath(midValue);

			var pointerLine = 
				d3.svg.line()
				.x(function (d) {
					return d.x;
				})
				.y(function (d) {
					return d.y;
				})
				.interpolate("basis");

			// pointer
			pointerContainer.selectAll("path")
				.data([pointerPath])
				.enter()
				.append("svg:path")
				.attr("d", pointerLine)
				.style("fill", "rgb(59, 103, 140)")
				.style("stroke", "rgb(59, 103, 140)")
				.style("fill-opacity", 0.7);

			// center of speedometer
			pointerContainer.append("svg:circle")
				.attr("cx", this.config.cx)
				.attr("cy", this.config.cy)
				.attr("r", 0.12 * this.config.radius)
				.style("fill", "rgb(214, 230, 246)")
				.style("stroke", "rgb(59, 103, 140)")
				.style("opacity", 1);

			// shown value
			var fontSize = Math.round(this.config.size * this.config.fontConf.size / 10);
			
			pointerContainer.selectAll("text")
				.data([midValue])
				.enter()
				.append("svg:text")
				.attr("x", this.config.cx)
				.attr("y", this.config.size - this.config.cy / 4 - fontSize)
				.attr("dy", fontSize / 2)
				.attr("text-anchor", "middle")
				.style("font-size", fontSize + "px")
				.style("visibility", this.config.showValue ? "visible" : "hidden")
				.style("fill", this.config.fontConf.color)
				.style("font-family", this.config.fontConf.fontFamily)
				.style("font-weight", this.config.fontConf.fontWeight)
				.style("stroke-width", "0px");

			this.redraw(this.config.min, 0);
		};

		this.buildPointerPath = function (value) {
			function valueToPoint(value, factor) {
				var point = self.valueToPoint(value, factor);
				point.x -= self.config.cx;
				point.y -= self.config.cy;
				return point;
			};

			var delta = this.config.range / 13;

			var head = valueToPoint(value, 0.85);
			var head1 = valueToPoint(value - delta, 0.12);
			var head2 = valueToPoint(value + delta, 0.12);

			var tailValue = value - (this.config.range * (1 / (270 / 360)) / 2);
			var tail = valueToPoint(tailValue, 0.28);
			var tail1 = valueToPoint(tailValue - delta, 0.12);
			var tail2 = valueToPoint(tailValue + delta, 0.12);

			return [head, head1, tail2, tail, tail1, head2, head];
		};

		this.drawBand = function (start, end, color) {
			if ((end - start) <= 0 )
				return;

			this.body.append("svg:path")
				.style("fill", color)
				.attr("d", 
					d3.svg.arc()
					.startAngle(this.valueToRadians(start))
					.endAngle(this.valueToRadians(end))
					.innerRadius(0.65 * this.config.radius)
					.outerRadius(0.85 * this.config.radius)
				)
			.attr("transform", function () {
				return "translate(" + self.config.cx + ", " + self.config.cy + ") rotate(270)"
			});
		};

		this.redraw = function (value, transitionDuration) {
			var pointerContainer = this.body.select(".pointerContainer");

//			pointerContainer.selectAll("text").text(Math.round(value));
			
			var valueToBeRounded = 0;
			if(self.config.valuePrecision) {
				valueToBeRounded = value * Math.pow(10, self.config.valuePrecision);
				valueToBeRounded = Math.round(valueToBeRounded);
				valueToBeRounded = valueToBeRounded *  Math.pow(10, -(self.config.valuePrecision));
			}
			
			pointerContainer.selectAll("text")
				.text(self.config.valuePrecision? 
						valueToBeRounded.toFixed(self.config.valuePrecision) : Math.round(value));

			var pointer = pointerContainer.selectAll("path");
			pointer.transition()
			.duration(undefined != transitionDuration ? transitionDuration : this.config.transitionDuration)
			.attrTween("transform", function () {
				var pointerValue = value;
				if (value > self.config.max)
					pointerValue = self.config.max + 0.02 * self.config.range;
				else if (value < self.config.min)
					pointerValue = self.config.min - 0.02 * self.config.range;
				var targetRotation = (self.valueToDegrees(pointerValue) - 90);
				var currentRotation = self._currentRotation || targetRotation;
				self._currentRotation = targetRotation;

				return function (step) {
					var rotation = currentRotation + (targetRotation - currentRotation) * step;
					return "translate(" + self.config.cx + ", " + self.config.cy + ") rotate(" + rotation + ")";
				};
			});
		};
		
		this.redrawTarget = function (value, transitionDuration){
			var targetPoint1 = this.valueToPoint(value, 0.55);
			var targetPoint2 = this.valueToPoint(value, 0.85);
			
			var targetPointer = this.targetLine;
			
			targetPointer
				.attr("x1", targetPoint1.x)
				.attr("y1", targetPoint1.y)
				.attr("x2", targetPoint2.x)
				.attr("y2", targetPoint2.y);
		};

		this.valueToDegrees = function (value) {
			// thanks @closealert
			return value / this.config.range * 270 - (this.config.min / this.config.range * 270 + 45);
		};

		this.valueToRadians = function (value) {
			return this.valueToDegrees(value) * Math.PI / 180;
		};

		this.valueToPoint = function (value, factor) {
			return {
				x : this.config.cx - this.config.radius * factor * Math.cos(this.valueToRadians(value)),
				y : this.config.cy - this.config.radius * factor * Math.sin(this.valueToRadians(value))
			};
		};

		// initialization
		if(configuration) {
			this.configure(configuration);
		}
	};
	/**
	 * End of Gauge Element snippet
	 */
	
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
	var gaugeNgDirectiveApp = angular.module('gaugeNgDirectiveApp', 
			['ngMaterial', 'ngSanitize', 'ngAnimate', 'sbiModule', 'angular_table']);
	
	gaugeNgDirectiveApp.directive("kpiGauge", ['$compile', '$timeout' , function($compile, $timeout){
		return {
			restrict: 'E',
			template: 
				'<div layout-align="center center" layout="row">'
				+ '<div id="{{containerFrameId}}" svg-style="height:{{size}}px"></div></div>'
				+'<style>'
				+'</style>',
			controller: kpiGaugeCtrl,
//			transclude: true,
			scope: {
				gaugeId: '=',
				label: '=',
				size: '=',
				minValue: '=',
				maxValue: '=',
				value: '=',
				targetValue: '=',
				thresholdStops: '=?',
				showValue: '=?',
				showThresholds: '=?',
				valuePrecision: '=?',
				fontConf: '=?',
			},
			link: function(scope, element, attributes){
				$timeout(function(){
					scope.gaugeSvg = new Gauge(scope.containerFrameId, scope.initialConfig);
					scope.gaugeSvg.render();
					
					if(scope.value) {
						scope.gaugeSvg.redraw(scope.value);
					}
					
					if(scope.targetValue) {
						scope.gaugeSvg.redrawTarget(scope.targetValue);
					}
				}, 0);
			}
		};
	}]);
	
	function kpiGaugeCtrl($scope){
		$scope.containerFrameId = "kpiGaugeFrame_" + $scope.gaugeId;
		
		$scope.gaugeSvg = null;
		
		$scope.showTarget = $scope.showTarget != undefined? $scope.showTarget : true ;
		
		$scope.createGauge = function(
				frameId, label, size, min, max, thresholdStops, 
				showValue, showTarget, showThresholds, valuePrecision, fontConf) {
			
			var initialConfig = {
				size : undefined != size ? size: 100,
				label : undefined != label? label: '',
				min : undefined != min ? min : 0,
				max : undefined != max ? max : 100,
				showValue : undefined != showValue && null != showValue ? 
						showValue : true,
				showTarget : undefined != showTarget && null != showTarget ? 
						showTarget : true,
				showThresholds : undefined != showThresholds && null != showThresholds ? 
						showThresholds : true,
				stops: undefined != thresholdStops && thresholdStops != null ? thresholdStops : [],
				valuePrecision : undefined != valuePrecision && null != valuePrecision? 
						valuePrecision : 0,
				fontConf : undefined != fontConf && null != fontConf? 
						fontConf : {
							size : "1",
							color : "black",
							fontFamily : "Times New Roman",
							fontweight : "normal"
						},
						
				minorTicks : 5
			};

			var range = initialConfig.max - initialConfig.min;

			$scope.initialConfig = initialConfig;
		};
		
		$scope.updateGauge = function(newValue, newTransitionValue) {
			newValue = newValue || 0;
			if($scope.gaugeSvg && $scope.gaugeSvg != null) {
				$scope.gaugeSvg.redraw(newValue, newTransitionValue);
			}
		};
		
		$scope.updateGaugeTarget = function(newValue, newTransitionValue) {
			newValue = newValue || 0;
			if($scope.gaugeSvg && $scope.gaugeSvg != null) {
				$scope.gaugeSvg.redrawTarget(newValue, newTransitionValue);
			}
		};
		
		$scope.$watch('thresholdStops', function(newValue, oldValue) {
			console.log('thresholdStops old: ', oldValue);
			console.log('thresholdStops new: ', newValue);
			
			$scope.thresholdStops = newValue;
			$scope.initialConfig.stops = $scope.thresholdStops;
			
			$scope.gaugeSvg = new Gauge($scope.containerFrameId, $scope.initialConfig);
			$scope.gaugeSvg.render();
			
			if($scope.value) {
				$scope.gaugeSvg.redraw($scope.value);
			}
			
			if($scope.targetValue) {
				$scope.gaugeSvg.redrawTarget($scope.targetValue);
			}
		});
		
		$scope.$watch('value', function(newValue, oldValue) {
			console.log('value old: ', oldValue);
			console.log('value new: ', newValue);
			
			$scope.updateGauge(newValue, 1000);
		});
		
		$scope.$watch('targetValue', function(newValue, oldValue) {
			console.log('targetValue old: ', oldValue);
			console.log('targetValue new: ', newValue);

			if(newValue != oldValue) {
				$scope.updateGaugeTarget(newValue, 500);
			}
		});
		
		$scope.createGauge(
				$scope.containerFrameId,
				$scope.label,
				$scope.size,
				$scope.minValue,
				$scope.maxValue,
				$scope.thresholdStops,
				$scope.showValue,
				$scope.showTarget,
				$scope.showThresholds,
				$scope.valuePrecision,
				$scope.fontConf);
	};
	
	angular.forEach(['x', 'x1', 'x2', 'y', 'y1', 'y2', 'width', 'height', 'style', 'transform'], function(name) {
		var svgName = 'svg' + name[0].toUpperCase() + name.slice(1);
		
		gaugeNgDirectiveApp.directive(svgName, function() {
			return function(scope, element, attrs) {
				attrs.$observe(svgName, function(value) {
					attrs.$set(name, value); 
				});
			};
		});
	});
	
	gaugeNgDirectiveApp.directive("kpiLinearGauge", ['$compile', '$timeout' , function($compile, $timeout){
		return {
			restrict: 'E',
			templateUrl: currentScriptPath + 'kpiLinearGaugeTemplate/kpiLinearGaugeTemplate.html', 
				
			controller: kpiLinearGaugeCtrl,
			scope: {
				gaugeId: '=',
				label: '=',
				size: '=',
				minValue: '=',
				maxValue: '=',
				value: '=',
				targetValue: '=',
				thresholdStops: '=?',
				showValue: '=?',
				showTarget: '=?',
				showThresholds: '=?',
				valuePrecision: '=?',
				fontConf: '=?',
				mini: '=?'
			},
		};
	}]);
	
	function kpiLinearGaugeCtrl($scope){
		$scope.mini = 
			($scope.mini != undefined 
			&& ($scope.mini == true 
				|| $scope.mini.trim() == "" 
				|| $scope.mini.toLowerCase() != 'false'));
		
		$scope.svgId = 'kpiLinearGauge_' + $scope.gaugeId;
		
		$scope.fontColor = $scope.fontConf.color;
		$scope.labelFontSize = ($scope.fontConf.size * 2) + 'em';
		$scope.maxMinFontSize = ($scope.fontConf.size * 1.3) + 'em';
		$scope.intermediateFontSize = ($scope.fontConf.size * 1) + 'em';
		
		$scope.maxMinValueDifference = ($scope.maxValue - $scope.minValue);
		$scope.sizeScaleFactor = ($scope.size / $scope.maxMinValueDifference) ;
		
		$scope.showValue = ($scope.showValue != undefined) ? $scope.showValue : true;
		$scope.showTarget = ($scope.showTarget != undefined) ? $scope.showTarget : true;
		
		$scope.getThresholdWidth = function(stop) {
			var stopWidth = (stop.to - stop.from) * $scope.sizeScaleFactor ;
			
			return stopWidth;
		};
	};
	

	gaugeNgDirectiveApp.directive("kpiListDocument", 
			['$compile', '$timeout', function($compile, $timeout){
		return {
			restrict: 'E',
			templateUrl: currentScriptPath + 'kpiListDocumentTemplate/kpiListDocumentTemplate.html', 
			controller: kpiListDocumentCtrl,
			scope: {
				kpiItems: "="
			},
		};
	}]);
	
	function kpiListDocumentCtrl($scope, $compile, sbiModule_translate){
		$scope.LINEAR_GAUGE_SIZE = 250;
		
		$scope.dataToShow = [];
		
		$scope.columns = 
			[{
				label: sbiModule_translate.load("sbi.generic.name"),
				name: "name"
			},{
				label: sbiModule_translate.load("sbi.generic.value"),
				name: "value"
			},{
				label: sbiModule_translate.load("sbi.kpi.viewer.document.list.semaphore"),
				name: "semaphore"
			},{
				label: sbiModule_translate.load("sbi.kpi.viewer.document.list.trend"),
				name: "trend"
			},{
				label: sbiModule_translate.load("sbi.kpi.viewer.document.list.lineargauge"),
				name: "lineargauge",
				size: $scope.LINEAR_GAUGE_SIZE
			}];
		
		$scope.linearGaugeTemplate = function(kpiItem) {
			var template = 
				'<kpi-linear-gauge '
					+ 'gauge-id="' + kpiItem.id + '" '
					+ 'label="' + kpiItem.name + '" '
//					+ 'size="' + kpiItem.size + '" '
					+ 'size="' + $scope.LINEAR_GAUGE_SIZE + '" '
					+ 'min-value="' + kpiItem.minValue + '" '
					+ 'max-value="' + kpiItem.maxValue + '" '
					+ 'value="' + kpiItem.value + '" '
					+ 'threshold-stops=\'' + JSON.stringify(kpiItem.thresholdStops) + '\' '
					+ 'show-value="false" '
					+ 'show-thresholds="true" '
					+ 'value-precision="' + kpiItem.precision + '" '
					+ 'font-conf=\'' + JSON.stringify(kpiItem.fontConf) + '\' '
					+ 'target-value="' + kpiItem.targetValue + '" '
					+ 'mini="true" '
				+ '></kpi-linear-gauge>';
			
			return template;
		};
		
		$scope.getDataToShow = function() {
			var result = [];
			
			for(var i = 0; i < $scope.kpiItems.length; i++) {
				var kpiItem = $scope.kpiItems[i];
				
				var obj = {};
				
				obj.name = kpiItem.name;
				obj.value = kpiItem.value;
				obj.semaphore = "MOCK semaphore";
				obj.trend = "MOCK trend";
				obj.lineargauge = $scope.linearGaugeTemplate(kpiItem);
				
				result.push(obj)
			}
			return result;
		};
		
		$scope.$watch('kpiItems', function(newValue, oldValue) {
			$scope.dataToShow = $scope.getDataToShow();
		}, true);
	};
})();