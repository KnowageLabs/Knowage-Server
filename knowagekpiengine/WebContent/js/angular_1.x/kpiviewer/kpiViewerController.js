(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');
	
	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices','sbiModule_config', 'kpiViewerGaugeService', kpiViewerControllerFn]);
	
	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices,sbiModule_config, kpiViewerGaugeService) {
		$scope.documentData = documentData;
		$scope.kpiOptions = documentData.template.chart.options;
		
		// div id for showing the chart produced by highcharts library
		$scope.viewerContainerId = "kpiViewer_" + $scope.documentData.docId;
		
		$scope.gaugeSize = 250;
		$scope.linearGaugeSize = 400;
		$scope.gaugeMinValue = 0;
		$scope.gaugeMaxValue = 150;
		$scope.gaugeValue = 0;
		$scope.gaugeTargetValue = 0;
		$scope.thresholdStops = documentData.kpiValue.threshold;
		$scope.percentage=0;
		

		
		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate","readKpiTemplate",$scope.documentData.template)
			.then(function(response){ 
//				console.log("response.data: ", response.data);
				var chart = $scope.documentData.template.chart;
				
				if(chart.type == "kpi") {
					if(Array.isArray(response.data)) {
						if(chart.model == 'widget') {
							angular.copy(response.data[0].kpi, $scope.documentData.kpiValue);
							angular.copy(response.data[0].target, $scope.documentData.targetValue);
							
							var gaugeConf = kpiViewerGaugeService.createGaugeConf(
									$scope.viewerContainerId, // container id
									$scope.documentData.kpiValue.name, // label 
									$scope.gaugeSize, // gauge size
									$scope.gaugeMinValue, // minimum value
									$scope.gaugeMaxValue, // maximum value
									$scope.documentData.kpiValue.threshold, //threshold configuration
									$scope.documentData.template.chart.options.vieweas, // speedometer / kpicard / semaphore
									$scope.documentData.template.chart.options.showvalue, // show/hide kpi value inside the gauge 
									$scope.documentData.template.chart.options.showtarget, // show/hide target value inside the gauge 
									$scope.documentData.template.chart.options.showthreshold, // show/hide kpi thresholds 
									$scope.documentData.template.chart.options.precision, // number of value digits 
									$scope.documentData.template.chart.style.font // font configuration 
							);
							
							$scope.thresholdStops = gaugeConf.stops;
							
							$scope.gaugeValue = 120;
							$scope.gaugeTargetValue = 122;
							
							 if($scope.gaugeTargetValue!=0){
									$scope.percentage = (($scope.gaugeValue / $scope.gaugeTargetValue)*100);
								}else{
									$scope.percentage = 0;
								}
								if($scope.documentData.template.chart.options!=undefined){
									if($scope.documentData.template.chart.options.history!=undefined){
										if($scope.documentData.template.chart.options.history.size!=undefined){
											$scope.percentage =$scope.percentage.toFixed($scope.documentData.template.chart.options.history.size);
										}
									}else{
										$scope.percentage =$scope.percentage.toFixed(3);
									}
								}
							
						} else {
							$scope.documentData.kpiListValue = $scope.documentData.kpiListValue || [];
							
							for(var i = 0; i < response.data.length; i++) {
								$scope.documentData.kpiListValue.push(response.data[i].kpi);
							}
						}
						
						$scope.thresholdStops = gaugeConf.stops;
				
						$scope.gaugeValue = 120;
						
					}
				} else { //scorecard
					$scope.documentData.scorecard = response.data[0].scorecard;
					var gaugeConf = kpiViewerGaugeService.createGaugeConf(
						$scope.viewerContainerId, // container id
						$scope.documentData.kpiValue.name, // label 
						$scope.gaugeSize, // gauge size
						$scope.gaugeMinValue, // minimum value
						$scope.gaugeMaxValue, // maximum value
						$scope.documentData.kpiValue.threshold, //threshold configuration
						$scope.documentData.template.chart.options.vieweas // speedometer / kpicard / semaphore
					);		
				}
			});
		};
	};
})();