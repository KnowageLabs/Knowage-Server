(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');
	
	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices', 'kpiViewerGaugeService', kpiViewerControllerFn]);
	
	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices, kpiViewerGaugeService) {
		$scope.documentData = documentData;
		$scope.kpiOptions = documentData.template.chart.options;
		
		// div id for showing the chart produced by highcharts library
		$scope.viewerContainerId = "kpiViewer_" + $scope.documentData.docId;
		
		$scope.gaugeSize = 250;
		$scope.gaugeMinValue = 0;
		$scope.gaugeMaxValue = 150;
		$scope.gaugeValue = 0;
		$scope.gaugeSvg
		$scope.thresholdStops = documentData.kpiValue.threshold;
		
		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate","readKpiTemplate",$scope.documentData.template)
			.then(function(response){ 
//				console.log("response.data: ", response.data);
				
				if(Array.isArray(response.data)) {
					angular.copy(response.data[0].kpi, $scope.documentData.kpiValue);
					angular.copy(response.data[0].target, $scope.documentData.targetValue);
				} else {
					angular.copy(response.data.kpi, $scope.documentData.kpiValue);
					angular.copy(response.data.target, $scope.documentData.targetValue);
				}

				var gaugeConf = kpiViewerGaugeService.createGaugeConf(
					$scope.viewerContainerId, // container id
					$scope.documentData.kpiValue.name, // label 
					$scope.gaugeSize, // gauge size
					$scope.gaugeMinValue, // minimum value
					$scope.gaugeMaxValue, // maximum value
					$scope.documentData.kpiValue.threshold, //threshold configuration
					$scope.documentData.template.chart.options.showvalue, // show/hide kpi value inside the gauge 
					$scope.documentData.template.chart.options.showthreshold, // show/hide kpi thresholds 
					$scope.documentData.template.chart.options.precision, // number of value digits 
					$scope.documentData.template.chart.style.font // font configuration 
				);
				
				$scope.thresholdStops = gaugeConf.stops;
				
				$scope.gaugeValue = 87;
//				$scope.gaugeValue = new value;
			});
		};
		
		$scope.getSpeedoLinearConf = function(){
			return {
				chart: {
					type: 'bulletChart',
					transitionDuration: 500,
					height: 50
				}
			};
		};
		
		$scope.getSpeedoLinearData = function(){
			return {
				"title": "Revenue",
				"subtitle": "US$, in thousands",
				"ranges": [150,225,300],
				"measures": [220],
//				"markers": [250]
			};
		};
		
		$scope.getSpeedoLinearConfig = function(){
			return {
				refreshDataOnly: true,
				deepWatchDataDepth: 0
			};
		};
	};
})();