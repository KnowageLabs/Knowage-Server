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

		$scope.loadKpiValue = function(){
			if($scope.documentData.template.chart.data.kpi != undefined){
				var object = {
						"kpi": $scope.documentData.template.chart.data.kpi,
						"driverMap": $scope.documentData.driverMap,
						"startValidity":"",
						"endValidity":""
				};
				
				sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
				
				sbiModule_restServices
				.promisePost("1.0/kpi", 'loadKpiValue',object)
				.then(function(response) {
					var array =response.data;
					for(var i=0;i<array.length;i++){
						for(var j=0;j<$scope.kpiItems.length;j++){
							if(array[i].kpiId == $scope.kpiItems[j].id 
									&& array[i].kpiVersion == $scope.kpiItems[j].version){
								$scope.kpiItems[j]["valueInfo"] = array[i];
								$scope.kpiItems[j].value = array[i].computedValue;
							}	
						}
					}
				},function(response) {
					console.log("Error get Kpi Value");
				});
			}
		};

		$scope.executeSchedulerTemp = function(){
			sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
			
			for(var i = 0; i < $scope.kpiItems.length; i++) {
				sbiModule_restServices
				.promiseGet("1.0/kpi", 'executeKpiScheduler/' + $scope.kpiItems[i].id)
				.then(function(response) {
					console.log("Scheduler eseguito");
					
				}, function(response) {
					console.log("Error Scheduler non eseguito");
				});
			}
		};

		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate", "readKpiTemplate", $scope.documentData.template)
			.then(function(response){ 
				var chart = $scope.documentData.template.chart;

				$scope.gaugeValue = 120;
				$scope.gaugeTargetValue = 122;
				
				if(chart.type == "kpi") {
					if(Array.isArray(response.data)) {
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
					}
					
//					$scope.executeSchedulerTemp();
					
					$scope.loadKpiValue();
				} else { //scorecard
					$scope.documentData.scorecard = response.data[0].scorecard;
				}
			});
		};
	};
})();