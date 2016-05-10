(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);


	angular.module('kpi-widget', ['ngMaterial','sbiModule'])
	.directive('kpiWidget', function() {
		return {
			templateUrl: currentScriptPath + 'template/kpi-widget.jsp',
			controller: kpiWidgetController,
			scope: {
				gaugeSize:'=',
				minValue:'=',
				maxValue:'=',
				value:'=',
				targetValue:'=',
				thresholdStops:'=',
				precision:'=?',
				valueSeries: '=',

				widgetId:'=',
				label:'=',
				fontConf:'=',
				showTargetPercentage:'=',
				showThresholds: '=?',
				canSee:'='
			},
		};
	});

	function kpiWidgetController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config,$interval){
		$scope.translate = sbiModule_translate;
		$scope.data = [{
			"values" : []
		}];
		
		if($scope.precision) {
			if($scope.value) {
				$scope.value = Number($scope.value.toFixed($scope.precision));
			}
			
			if($scope.targetValue) {
				$scope.targetValue = Number($scope.targetValue.toFixed($scope.precision));
			}
		}

		$scope.options = {
				chart: {
					type: 'stackedAreaChart',
					height: 250,
					width:400,
					margin : {
						right: 0,
						left: 0
					},
					color:['#C4DCF3'],
					x: function(d){return d[0];},
					y: function(d){return d[1];},
					useVoronoi: false,
					clipEdge: true,
					duration: 100,
					style:{
						border:"black"
					},
					useInteractiveGuideline: true,
					xAxis: {
						showMaxMin: false,
						tickFormat: function(d) {
							return d3.time.format('%x')(new Date(d))
						}
					},
					yAxis: {
						tickFormat: function(d){
							return d3.format(',.2f')(d);
						}
					},
				}
		};

		
		$scope.$watch('valueSeries',function (newValue, oldValue) {
		
				var values = $scope.convertToStackedAreaChartData(newValue);
				$scope.data = [{"values" :	values}];
				console.log(values);
			
		}
		, true);
	/*	var initValueSeries= $interval(function() {
		       if ($scope.valueSeries!=undefined) {
		             if (angular.isDefined(initValueSeries)) {
		                 $interval.cancel(initValueSeries);
		                 initValueSeries = undefined;
		                 if($scope.data[0].values!=undefined){
		                	 var values = $scope.convertToStackedAreaChartData($scope.valueSeries);
				 			$scope.data = [{"values" :	values}];
		                 }
		                
		               }
		         }
		       }, 500,10);
*/
		$scope.convertToStackedAreaChartData= function(arrKpi){
			var array = [];
			if(arrKpi!=undefined){
				for(var i=0;i<arrKpi.length;i++){
					var arrTemp = [];
					if(arrKpi[i].manualValue!=undefined){
						arrTemp.push(arrKpi[i].timeRun,arrKpi[i].manualValue);
					}else{
						arrTemp.push(arrKpi[i].timeRun,arrKpi[i].computedValue);
					}
					
					array.push(arrTemp);
				}
				
				return array;
			}
		};

		
		$scope.getValueToShow = function(){
			var valueToShow = '-';
			
			if($scope.value != null) {
				valueToShow = $scope.value;
				
				if($scope.value >= 1000){
					valueToShow = (Number(valueToShow)/1000);
					
					if($scope.precision) {
						valueToShow = (valueToShow).toFixed($scope.precision);
					}
					
					valueToShow += " K";
				}else{
					if($scope.precision) {
						valueToShow = Number(valueToShow).toFixed($scope.precision);
					}
				}
			}
			
			return valueToShow;
		};
		
		$scope.getTargetToShow = function(){
			var targetToShow = '-';
			
			if($scope.targetValue != null) {
				targetToShow = $scope.targetValue;
				
				if(targetToShow >= 1000){
					targetToShow = (Number(targetToShow)/1000);
					
					if($scope.precision) {
						targetToShow = (targetToShow).toFixed($scope.precision);
					}
					
					targetToShow += "K";
					
				} else {
					if($scope.precision) {
						targetToShow = Number(targetToShow).toFixed($scope.precision);
					}
				}
			}
			
			return targetToShow;
		};

		$scope.getPercentage = function(){
			if($scope.value != null && $scope.targetValue != null){
				if($scope.targetValue != 0) {
					return (($scope.value / $scope.targetValue)*100).toFixed($scope.precision) + ' %';
				}else{
					return 0 + ' %';
				}
			} else {
				return '-';
			}
		};

		$scope.openEdit = function(){
			var deferred = $q.defer();
			$mdDialog.show({
				controller: DialogController,
				templateUrl: '/knowagekpiengine/js/angular_1.x/kpi-widget/template/kpi-widget-editValue.jsp',
				clickOutsideToClose:true,
				preserveScope:true,
				locals: {items: deferred,label:$scope.label,value:$scope.value, targetValue:$scope.targetValue,valueSeries:$scope.valueSeries[$scope.valueSeries.length-1] }
			})
			.then(function(answer) {
				$scope.status = 'You said the information was "' + answer + '".';
				return deferred.resolve($scope.selectedFunctionalities);
			}, function() {
				$scope.status = 'You cancelled the dialog.';
				if(deferred.promise.$$state.value!=undefined){
					$scope.value = deferred.promise.$$state.value.value;
				}
				if(deferred.promise.$$state.comment!=undefined){
					$scope.valueSeries[$scope.valueSeries.length-1].manualNote = deferred.promise.$$state.value.comment;
				}
				
				$scope.getValueToShow();
			});
			return deferred.promise;
		};
	};

	function DialogController($scope,$mdDialog,$mdToast,sbiModule_restServices,sbiModule_config,sbiModule_translate,items,label,value,targetValue,valueSeries){

		$scope.label = label;
		$scope.value =value;
		$scope.targetValue =targetValue;
		$scope.valueSeries = valueSeries;
		$scope.array = [];
		$scope.oldValue=valueSeries.computedValue;
		$scope.translate = sbiModule_translate;
		
		$scope.parseLogicalKey = function() {
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
		};
		
		$scope.parseLogicalKey();
		
		$scope.close = function(){
			$mdDialog.cancel();
		};
		
		$scope.apply = function(){
			if($scope.valueSeries.manualNote==null || $scope.valueSeries.manualNote.trim()==""){
				$scope.showAction($scope.translate.load("sbi.kpi.widget.missingcomment"));
			
			} else {
				if(!$scope.value){
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
					$scope.valueSeries.manualValue = $scope.value;
					//obj["valueSeries"] = $scope.valueSeries;
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