(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices','sbiModule_config', 'kpiViewerGaugeService', kpiViewerControllerFn]);

	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices, sbiModule_config, kpiViewerGaugeService) {
		$scope.documentData = documentData;
		$scope.kpiOptions = documentData.template.chart.options;

		$scope.kpiItems = [];

//		$scope.gaugeSize = 250;
//		$scope.linearGaugeSize = 400;
		$scope.GAUGE_DEFAULT_SIZE = 250;
		$scope.LINEAR_GAUGE_DEFAULT_SIZE= 400;
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

				$scope.gaugeValue = 120;
				$scope.gaugeTargetValue = 122;
				
				if(chart.type == "kpi") {
					if(Array.isArray(response.data)) {
//						if(chart.model == 'widget') {
							
							var templateKpi = $scope.documentData.template.chart.data.kpi;
							if(!Array.isArray(templateKpi)) {
								var array = [templateKpi];
								templateKpi = array;
							}
							
							var templateOptions = $scope.documentData.template.chart.options;
							var templateStyle = $scope.documentData.template.chart.style;

							for(var i = 0; i < response.data.length; i++) {
								var responseItem = response.data[i];
								
								var responseItemKpi = responseItem.kpi;

								$scope.documentData.kpiValue.push(responseItemKpi);
								
								for(var j = 0; j < templateKpi.length; j++) {
									var templateKpiItem = templateKpi[j];
									responseItemKpi.targetValue = responseItem.target;
									
									if(templateKpiItem.id == responseItemKpi.id) {
										var conf = kpiViewerGaugeService.createWidgetConfiguration(
												templateKpiItem, responseItemKpi, templateOptions, templateStyle);
										
										/* MOCK */
										if(!conf.value) {
											conf.value = $scope.gaugeValue;
										}
										if(!conf.targetValue) {
											conf.targetValue = $scope.gaugeTargetValue;
										}
										/* MOCK */
										
										$scope.kpiItems.push(conf);
										
										break;
									}
								}
							}
//						} else {
//							$scope.documentData.kpiListValue = $scope.documentData.kpiListValue || [];
//
//							for(var i = 0; i < response.data.length; i++) {
//								$scope.documentData.kpiListValue.push(response.data[i].kpi);
//							}
//						}
					}
				} else { //scorecard
					$scope.documentData.scorecard = response.data[0].scorecard;
				}
			});
		};
	};
})();