(function() {
	
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
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
		$scope.loadKpiValues = [];
	
		$scope.loadKpiValue = function(){
			if($scope.documentData.template.chart.data.kpi != undefined){

					var array =JSON.parse($scope.loadKpiValues);
					
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

			
			}
		};
		

		$scope.init = function(){
			sbiModule_restServices.promisePost("1.0/jsonKpiTemplate", "readKpiTemplate", $scope.documentData.template)
			.then(function(response){ 
				var chart = $scope.documentData.template.chart;

				$scope.gaugeValue = null;
				$scope.gaugeTargetValue = null;
				$scope.loadKpiValues = response.data.loadKpiValue;
				if(chart.type == "kpi") {
					if(Array.isArray(JSON.parse(response.data.info))) {
						var templateKpi = $scope.documentData.template.chart.data.kpi;
						if(!Array.isArray(templateKpi)) {
							var array = [templateKpi];
							templateKpi = array;
						}

						var templateOptions = $scope.documentData.template.chart.options;
						var templateStyle = $scope.documentData.template.chart.style;

						for(var i = 0; i < JSON.parse(response.data.info).length; i++) {
							var responseItem = JSON.parse(response.data.info)[i];

							var responseItemKpi = responseItem.kpi;

							$scope.documentData.kpiValue.push(responseItemKpi);

							for(var j = 0; j < templateKpi.length; j++) {
								var templateKpiItem = templateKpi[j];
								responseItemKpi.targetValue = responseItem.target;

								if(templateKpiItem.id == responseItemKpi.id) {
									var conf = kpiViewerServices.createWidgetConfiguration(
											templateKpiItem, responseItemKpi, chart);

									$scope.kpiItems.push(conf);

									break;
								}
							}
						}
					}

					
					$scope.loadKpiValue();
				} else { //scorecard
					$scope.documentData.scorecard = JSON.parse(response.data.info)[0].scorecard;
				}
			});
		};
		
		$scope.openEdit = function(kpiItem){
			var deferred = $q.defer();
			$mdDialog.show({
				controller: dialogController,
				templateUrl: currentScriptPath + '../kpi-widget/template/kpi-widget-editValue.jsp',
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
	
	function dialogController($scope,$mdDialog,sbiModule_restServices,$mdToast,sbiModule_config,sbiModule_translate,items,label,value,targetValue,valueSeries){
		$scope.label = label;
		$scope.value = value;
		$scope.targetValue =targetValue;
		$scope.valueSeries = valueSeries;
		$scope.oldValue=valueSeries.computedValue;
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
			if($scope.valueSeries.manualNote==null || $scope.valueSeries.manualNote.trim()==""){
				$scope.showAction($scope.translate.load("sbi.kpi.widget.missingcomment"));
			}else{
				if($scope.value==undefined){
					$scope.value = null;
				}
				$mdDialog.cancel();
				$scope.kpiValueToSave = {};
				$scope.kpiValueToSave["manualValue"] = $scope.value;
				$scope.kpiValueToSave["manualNote"] = $scope.valueSeries.manualNote;
				$scope.kpiValueToSave["valueSeries"] = $scope.valueSeries;
				sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
				sbiModule_restServices.promisePost("1.0/kpi", 'editKpiValue',$scope.kpiValueToSave)
	
				.then(function(response){ 
					var obj = {};
					if($scope.value==null){
						$scope.value =$scope.valueSeries.computedValue;
					}
					obj["value"] = $scope.value;
					obj["comment"] = $scope.kpiValueToSave["manualNote"];
					items.resolve(obj);
				},function(response){
					$scope.errorHandler(response.data,"");
				});
			}
		};

		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top')

			$mdToast.show(toast).then(function(response) {

				if ( response == 'ok' ) {

				}
			});
		};

	};
})();