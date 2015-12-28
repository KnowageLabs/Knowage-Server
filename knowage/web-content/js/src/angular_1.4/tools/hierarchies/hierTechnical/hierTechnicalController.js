var app = angular.module('hierManager');

app.controller('hierTechnController', ['sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyTechFunction ]);

function hierarchyTechFunction(sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
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
		action : function(item,parent,event){
			 var parentEl = angular.element(document.body);
			 $mdDialog.show({
					templateUrl: sbiModule_config.contextName +'/js/src/angular_1.4/tools/hierarchies/templates/hierSrcDialog.html',
					parent: angular.element(document.body),
					locals: {
						   translate: $scope.translate,
				           item:  item,
				           parent : parent
				         },
					preserveScope : true,
					clickOutsideToClose:false,
					controller: DialogController 
				})
				.then(function(item) {
					
				}, function() {
					//form was cancelled, nothing to do 
				});
			 
			 	function DialogController($scope, $mdDialog, translate, item, parent) {
			        $scope.translate = translate;
	 				$scope.item = item;
			        $scope.parent = parent;
			        $scope.closeDialog = function() {
			        	$mdDialog.cancel();
			        }
			        $scope.saveHier = function(){
			        	$mdDialog.hide();
			        }
				 }
			}
		},{
		label:'remove',
		action : function(item,event){
			var data='';
		}
	}];
	
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
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
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
			var dateFormatted =$scope.dateFilterSrc.getFullYear() + '-' + $scope.dateFilterSrc.getMonth()+'-'+$scope.dateFilterSrc.getDate();
			var config = {};
			config.params = {
				dimension: $scope.dimSrc.DIMENSION_NM,
				filterType : $scope.hierSrc.HIERARCHY_TP,
				filterHierarchy : $scope.hierSrc.HIERARCHY_NM,
				validityDate : dateFormatted
			};
			$scope.restService.get("hierarchies","getHierarchyTree",null,config)
				.success(
					function(data, status, headers, config) {
						if (data !== undefined && data.errors === undefined){
							if (typeof data =='object'){
								data = [data];
							}
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
