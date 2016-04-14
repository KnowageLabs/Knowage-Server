(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');
	
	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices', 'kpiViewerGaugeService', kpiViewerControllerFn]);
	
	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices, kpiViewerGaugeService) {
		$scope.documentData = documentData;
		
		// div id for showing the chart produced by highcharts library
		$scope.viewerContainerId = "kpiViewer_" + $scope.documentData.docId;
		
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

				kpiViewerGaugeService.createGauge(
						$scope.viewerContainerId, // container id
						$scope.documentData.kpiValue.name, // label 
						250, // gauge size
						0, // minimum value
						150, // maximum value
						$scope.documentData.kpiValue.threshold, //threshold configuration
						$scope.documentData.template.chart.options.showvalue, // show/hide kpi value inside the gauge 
						$scope.documentData.template.chart.options.showthreshold, // show/hide kpi thresholds 
						$scope.documentData.template.chart.options.precision, // number of value digits 
						$scope.documentData.template.chart.style.font // font configuration 
					);
				
				kpiViewerGaugeService.updateGauge(87, 1000);
			});
		};
	};
})();