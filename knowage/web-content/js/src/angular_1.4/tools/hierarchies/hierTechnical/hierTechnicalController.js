var app = angular.module('hierManager');

app.controller('hierTechnController', ["sbiModule_translate",'sbiModule_restServices','sbiModule_logger',"$scope", hierarchyTechFunction ]);

function hierarchyTechFunction(sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	$scope.filterBySrc = '';
	$scope.orderByFields = ['name','id'];
	
	$scope.hierarchiesTypeSrc = ['Master', $scope.translate.load('sbi.hierarchies.type.technical')];
	$scope.dateFilterSrc = new Date();
	$scope.hierarchiesSrcMap = {};
	$scope.hierTreeTarget = [];
	$scope.dimensionSrc = [];
	$scope.hierarchiesSrc=[];
	$scope.hierTreeSrc = [];
	$scope.seeFilter=false;
	
	$scope.keys = {'subfolders' : 'children'};
	
	$scope.hierTreeSrc.push(angular.copy(dataJson));
	$scope.hierTreeTarget.push(angular.copy(dataJson));
	
	$scope.toogleSeeFilter= function(){
		$scope.seeFilter = !$scope.seeFilter;
	}
	
	$scope.menuOptionSrc = [{
		label:'add',
		action : function(item,event){
			var data='';
		}
	},{
		label:'remove',
		action : function(item,event){
			var data='';
		}
	}]
	
	$scope.restService.get("dimensions","getDimensions")
		.success(
			function(data, status, headers, config) {
				$scope.dimensionSrc = data;
			})	
		.error(function(data, status){
			$scope.log.log('GET dimensions error of ' + data + ' with status :' + status);
		});
	
	$scope.getHierarchies = function (dim){
		if ($scope.hierTypeSrc !== undefined && $scope.dimSrc !== undefined){
			$scope.hierarchiesSrc = [];
			var type = $scope.hierTypeSrc;
			var dimName = $scope.dimSrc.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = type.toUpperCase() == 'AUTO' ? 'hierarchiesOfDimension' : 'getCustomHierarchies';
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if ($scope.hierarchiesSrcMap[keyMap] === undefined){
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								$scope.hierarchiesSrc = angular.copy(data);
								$scope.hierarchiesSrcMap[keyMap] = data;
							}else{
								$scope.log.log('GET hierarchies error of ' + data + ' with message : "' + data.errors[0].message + '"');
							}
						})
					.error(function(data, status){
						$scope.log.log('GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status);
					});
			}else{
				$scope.hierarchiesSrc = angular.copy($scope.hierarchiesSrcMap[keyMap]);
			}
		}
	}
	
	$scope.getTreeSrc = function(){
		if ($scope.dateFilterSrc && $scope.dimSrc && $scope.hierTypeSrc && $scope.hierSrc){
			$scope.restService.get()
				.success(
					function(data, status, headers, config) {
						if (data.errors === undefined){
							$scope.hierTreeSrc = data;
						}else{
							var params = 'date = ' + $scope.dateFilterSrc + ' dimension = ' + $scope.dimSrc.DIMENSION_NM + ' type = ' +  $scope.hierTypeSrc + ' hierachies = ' + $scope.hierSrc.HIERARCHY_NM;
							$scope.log.log('GET tree source error with parameters' + params + ' with message : "' + data.errors[0].message + '"');
						}
					})
				.error(function(data, status){
					var params = 'date = ' + $scope.dateFilterSrc + ' dimension = ' + $scope.dimSrc.DIMENSION_NM + ' type = ' +  $scope.hierTypeSrc + ' hierachies = ' + $scope.hierSrc.HIERARCHY_NM;
					$scope.log.log('GET tree source error with parameters' + params + ' with status: "' + status+ '"');
				});
		}
	}
	
	$scope.applyFilter = function(){
		//use to apply the filter only when is clicked the icon
		$scope.filterBySrcTrigger = angular.copy($scope.filterBySrc);
		$scope.orderBySrcTrigger = angular.copy($scope.orderBySrc);
	}
	
	$scope.removeFilter = function(){
		$scope.filterBySrcTrigger = "";
		$scope.filterBySrc = "";
		$scope.orderBySrcTrigger = "";
		$scope.orderBySrc = "";
	}
	
	$scope.debug = function (){
		var none='none';
	}
};
