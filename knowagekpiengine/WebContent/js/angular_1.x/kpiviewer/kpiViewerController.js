(function() {
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.controller('kpiViewerController', 
			['$scope', 'documentData', 'sbiModule_restServices','sbiModule_translate','sbiModule_config', 'kpiViewerServices','$q','$mdDialog', kpiViewerControllerFn]);

	function kpiViewerControllerFn($scope, documentData, sbiModule_restServices,sbiModule_translate, sbiModule_config, kpiViewerServices,$q,$mdDialog) {
		$scope.documentData = documentData;
		$scope.kpiOptions = documentData.template.chart.options;

		$scope.kpiItems = [];


		$scope.GAUGE_DEFAULT_SIZE = 250;
		$scope.LINEAR_GAUGE_DEFAULT_SIZE= 400;
		$scope.gaugeMinValue = 0;
		$scope.gaugeMaxValue = 150;
		$scope.gaugeValue = 0;
		$scope.gaugeTargetValue = 0;
		$scope.thresholdStops = documentData.kpiValue.threshold;
		$scope.percentage=0;
		$scope.translate = sbiModule_translate;

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
					
					for(var j = 0; j < $scope.kpiItems.length; j++){
						var kpiItem = $scope.kpiItems[j];
							
						for(var i = 0; i < array.length; i++){
							var kpiArray = JSON.parse(array[i])
							
							if(kpiArray.length > 0 && kpiArray[kpiArray.length-1].kpiId == kpiItem.id 
									&& kpiArray[kpiArray.length-1].kpiVersion == kpiItem.version){
								if(kpiArray[kpiArray.length-1].manualValue!=undefined)
									kpiItem.value = kpiArray[kpiArray.length-1].manualValue;
								else
									kpiItem.value = kpiArray[kpiArray.length-1].computedValue;
						
								for(var k = 0; k < kpiArray.length; k++) {
									kpiItem.valueSeries.push(kpiArray[k]);
								}
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
			sbiModule_restServices.promiseGet("1.0/kpi", 'executeKpiScheduler/'+2).then(
					function(response) {
						console.log("Scheduler eseguito");

					},function(response) {
						console.log("Error Scheduler non eseguito");
					})
		}

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
									var conf = kpiViewerServices.createWidgetConfiguration(
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
					
			//		$scope.executeSchedulerTemp();
					
					$scope.loadKpiValue();
				} else { //scorecard
					$scope.documentData.scorecard = response.data[0].scorecard;
				}
			});
		};
		
		$scope.openEdit = function(kpiItem){
			var deferred = $q.defer();
			$mdDialog.show({
				controller: DialogController,
				templateUrl: '/knowagekpiengine/js/angular_1.x/kpi-widget/template/kpi-widget-editValue.jsp',
				clickOutsideToClose:true,
				preserveScope:true,
				locals: {
					items: deferred,
					label:kpiItem.name,
					value:kpiItem.value,
					targetValue:kpiItem.targetValue,
					valueSeries:kpiItem.valueSeries[kpiItem.valueSeries.length-1]
				}
			})
			.then(function(answer) {
				
				return deferred.resolve($scope.selectedFunctionalities);
			}, function() {
				//$scope.loadKpiValue();
				if(deferred.promise.$$state.value!=undefined){
					kpiItem.value = deferred.promise.$$state.value.value;
				}
				if(deferred.promise.$$state.comment!=undefined){
					kpiItem.valueSeries[$scope.valueSeries.length-1].manualNote = deferred.promise.$$state.value.comment;
				}
				$scope.status = 'You cancelled the dialog.';
			});
			
			return deferred.promise;
		}
		
	};
	
	function DialogController($scope,$mdDialog,sbiModule_restServices,sbiModule_config,sbiModule_translate,items,label,value,targetValue,valueSeries){
		$scope.label = label;
		$scope.value = value;
		$scope.targetValue =targetValue;
		$scope.valueSeries = valueSeries;
		$scope.array = [];
		$scope.translate = sbiModule_translate;
		
		$scope.parseLogicalKey = function(){
			var string  = $scope.valueSeries.logicalKey;
			var char = string.split(",");
			$scope.array = [];
			for(var i=0;i<char.length;i++){
				var values = char[i].split("=")
				var obj = {};
				obj["label"] = values[0];
				obj["value"] = values[1];
				$scope.array.push(obj);
			}
		}
		$scope.parseLogicalKey();
		$scope.close = function(){
			$mdDialog.cancel();

		}
		$scope.apply = function(){
			$mdDialog.cancel();
			$scope.kpiValueToSave = {};
			$scope.kpiValueToSave["manualValue"] = $scope.value;
			$scope.kpiValueToSave["manualNote"] = $scope.valueSeries.manualNote;
			$scope.kpiValueToSave["valueSeries"] = $scope.valueSeries;
			sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
			sbiModule_restServices.promisePost("1.0/kpi", 'editKpiValue',$scope.kpiValueToSave)
			.then(function(response){ 
				var obj = {};
				obj["value"] = $scope.value;
				obj["comment"] = $scope.kpiValueToSave["manualNote"];
				items.resolve(obj);
				console.log("Saved");
			},function(response){
				$scope.errorHandler(response.data,"");
			});

		}

		

	}
})();