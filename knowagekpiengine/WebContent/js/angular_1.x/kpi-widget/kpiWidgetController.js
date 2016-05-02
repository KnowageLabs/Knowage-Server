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
				//			ngModel: '=',
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

	function kpiWidgetController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config){
		//	$scope.documentData = $scope.ngModel;
		if($scope.precision){
			$scope.value = $scope.value.toFixed($scope.precision);
			$scope.targetValue = $scope.targetValue.toFixed($scope.precision);
		}
		

		$scope.options = {
				chart: {
					type: 'stackedAreaChart',
					height: 250,
					width:400,
					margin : {
						//  top: 20,
						right: 0,
						//   bottom: 30,
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
			if(newValue!=oldValue){
				var values = $scope.convertToStackedAreaChartData(newValue);
				$scope.data = [{"values" :	values}];
				console.log("Guarda:",values);
			}
		}
		, true);

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
			//return [];
		},

		$scope.data = [{
			"values" : [ [ 1025409600000 , 9.3433263069351] , [ 1028088000000 , 8.4583069475546]]
		}];

		$scope.getValueToShow = function(){

			if($scope.value>=1000){
				return ((Number($scope.value)/1000).toFixed($scope.precision))+"K";

			}else{
				return Number($scope.value).toFixed($scope.precision);
			}
		}
		$scope.getTargetToShow = function(){
			if($scope.targetValue>=1000){
				return (Number($scope.targetValue)/1000).toFixed($scope.precision)+"K";

			}else{
				return $scope.targetValue;
			}
		}

		$scope.getPercentage = function(){
			if($scope.targetValue!=0){
				return (($scope.value / $scope.targetValue)*100).toFixed($scope.precision);
			}else{
				return 0;
			}
		}


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
		}
	};

	function DialogController($scope,$mdDialog,sbiModule_restServices,sbiModule_config,sbiModule_translate,items,label,value,targetValue,valueSeries){
		debugger;
		$scope.label = label;
		$scope.value = value;
		$scope.targetValue =targetValue;
		$scope.valueSeries = valueSeries;
		$scope.array = [];
		$scope.oldValue=value;
		$scope.translate =sbiModule_translate;
		//angular.copy($scope.value,$scope.oldValue);
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