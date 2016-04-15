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

//			this.config.size = this.config.size * 0.9;

			this.config.radius = this.config.size * 0.97 / 2;
			this.config.cx = this.config.size / 2;
			this.config.cy = this.config.size / 2;

			this.config.min = undefined != configuration.min ? configuration.min : 0;
			this.config.max = undefined != configuration.max ? configuration.max : 100;
			this.config.range = this.config.max - this.config.min;
			
			this.config.showValue = configuration.showValue != undefined ? configuration.showValue : true;
			this.config.valuePrecision = configuration.valuePrecision != undefined ? configuration.valuePrecision : 0;
			this.config.fontConf = configuration.fontConf != undefined && configuration.fontConf != null ? 
					configuration.fontConf : {
						size : "1",
						color : "black",
						fontFamily : "Times New Roman",
						fontweight : "normal"
					};

			this.config.majorTicks = configuration.majorTicks || 5;
			this.config.minorTicks = configuration.minorTicks || 2;

//			this.config.greenColor = configuration.greenColor || "#109618";
//			this.config.yellowColor = configuration.yellowColor || "#FF9900";
//			this.config.redColor = configuration.redColor || "#DC3912";

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
				.style("fill", "#ccc")
				.style("stroke", "#000")
				.style("stroke-width", "0.5px");

			// external circle inside the first one
			this.body.append("svg:circle")
				.attr("cx", this.config.cx)
				.attr("cy", this.config.cy)
				.attr("r", 0.9 * this.config.radius)
				.style("fill", "#fff")
				.style("stroke", "#e0e0e0")
				.style("stroke-width", "2px");

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
//					.style("fill", "#333")
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
						.style("stroke", "#666")
						.style("stroke-width", "1px");
				}

				var point1 = this.valueToPoint(major, 0.7);
				var point2 = this.valueToPoint(major, 0.85);

				this.body.append("svg:line")
					.attr("x1", point1.x)
					.attr("y1", point1.y)
					.attr("x2", point2.x)
					.attr("y2", point2.y)
					.style("stroke", "#333")
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
//						.style("fill", "#333")
						.style("fill", this.config.fontConf.color)
						.style("font-family", this.config.fontConf.fontFamily)
						.style("font-weight", this.config.fontConf.fontWeight)
						.style("stroke-width", "0px");
				}
			}

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
//				.style("fill", "#dc3912")
				.style("fill", "#000")
//				.style("stroke", "#c63310")
				.style("stroke", "#000")
				.style("fill-opacity", 0.7);

			// center of speedometer
			pointerContainer.append("svg:circle")
				.attr("cx", this.config.cx)
				.attr("cy", this.config.cy)
				.attr("r", 0.12 * this.config.radius)
//				.style("fill", "#4684EE")
				.style("fill", "#000")
//				.style("stroke", "#666")
				.style("stroke", "#000")
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
//				.style("fill", "#000")
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
			//.delay(0)
			//.ease("linear")
			//.attr("transform", function(d)
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

		this.valueToDegrees = function (value) {
			// thanks @closealert
			//return value / this.config.range * 270 - 45;
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
		this.configure(configuration);
	};
	/**
	 * End of Gauge Element snippet
	 */
	
	var gaugeNgDirectiveApp = angular.module('gaugeNgDirectiveApp', ['ngMaterial', 'ngSanitize', 'ngAnimate']);
	
	gaugeNgDirectiveApp.directive("kpiGauge", ['$compile', '$timeout' , function($compile, $timeout){
		return {
			restrict: 'E',
			template: 
				'<div layout-align="center center" layout="row">'
				+ '<div id="{{containerFrameId}}"></div></div>',
			controller: kpiGaugeCtrl,
			transclude: true,
			scope: {
				gaugeId: '=',
				label: '=',
				size: '=',
				minValue: '=',
				maxValue: '=',
				value: '=',
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
				}, 0);
			}
		};
	}]);
	
	function kpiGaugeCtrl($scope){
//		console.log("$scope.gaugeId -> ", $scope.gaugeId);
//		console.log("$scope.label -> ", $scope.label);
//		console.log("$scope.size -> ", $scope.size);
//		console.log("$scope.minValue -> ", $scope.minValue);
//		console.log("$scope.maxValue -> ", $scope.maxValue);
//		console.log("$scope.value -> ", $scope.value);
//		console.log("$scope.thresholdStops -> ", $scope.thresholdStops);
//		console.log("$scope.showValue -> ", $scope.showValue);
//		console.log("$scope.showThresholds -> ", $scope.showThresholds);
//		console.log("$scope.valuePrecision -> ", $scope.valuePrecision);
//		console.log("$scope.fontConf -> ", $scope.fontConf);
		
		$scope.containerFrameId = "kpiGaugeFrame_" + $scope.gaugeId;
		
		$scope.gaugeSvg = null;
		
		$scope.createGauge = function(
				frameId, label, size, min, max, thresholdStops, 
				showValue, showThresholds, valuePrecision, fontConf) {
			
			var initialConfig = {
				size : undefined != size ? size: 100,
				label : undefined != label? label: '',
				min : undefined != min ? min : 0,
				max : undefined != max ? max : 100,
				showValue : undefined != showValue && null != showValue ? 
						showValue : true,
				showThresholds : undefined != showThresholds && null != showThresholds ? 
						showThresholds : true,
				stops: undefined != thresholdStops && thresholdStops != null ? thresholdStops : [],
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

			var range = initialConfig.max - initialConfig.min;

			$scope.initialConfig = initialConfig;
		};
		
		$scope.updateGauge = function(newValue, newTransitionValue) {
			newValue = newValue || 0;
			if($scope.gaugeSvg && $scope.gaugeSvg != null) {
				$scope.gaugeSvg.redraw(newValue, newTransitionValue);
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
		});
		
		$scope.$watch('value', function(newValue, oldValue) {
			console.log('value old: ', oldValue);
			console.log('value new: ', newValue);
			
			$scope.updateGauge(newValue, 500);
		});
		
		$scope.createGauge(
				$scope.containerFrameId,
				$scope.label,
				$scope.size,
				$scope.minValue,
				$scope.maxValue,
				$scope.thresholdStops,
				$scope.showValue,
				$scope.showThresholds,
				$scope.valuePrecision,
				$scope.fontConf);
	};
})();