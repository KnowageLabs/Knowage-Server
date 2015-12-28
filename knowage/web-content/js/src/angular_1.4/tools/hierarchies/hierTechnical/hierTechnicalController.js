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
	$scope.metadataMap = {};
	$scope.seeFilter=false;

	$scope.keys = {'subfolders' : 'children'};
	
	$scope.hierTreeSrc.push(angular.copy(dataJson));
	$scope.hierTreeTarget.push(angular.copy(dataJson));
	
	$scope.toogleSeeFilter= function(){
		$scope.seeFilter = !$scope.seeFilter;
	}
	
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
		//get the metadata for the dimension
		var dimName = $scope.dimSrc.DIMENSION_NM;
		if ($scope.metadataMap !== undefined && $scope.metadataMap[dimName] == undefined){
			$scope.restService.get("hierarchies","nodeMetadata","dimension="+dimName+"&excludeLeaf=false")
			.success(
				function(data, status, headers, config) {
					if (data.errors === undefined){
						$scope.metadataMap[dimName] = data;
					}else{
						$scope.log.log('GET hierarchies error of ' + data + ' with message : "' + data.errors[0].message + '"');
					}
			})
			.error(function(data, status){
				$scope.log.log('GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status);
			});
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
	
	 $scope.editNode = function(item,parent){
		 var parentEl = angular.element(document.body);
		 var metTmp =  $scope.metadataMap[$scope.dimSrc.DIMENSION_NM];
		 var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
		 metadata == undefined ? metadata =  metTmp.GENERAL_FIELDS : metadata = metadata;
		 $mdDialog.show({
			templateUrl: sbiModule_config.contextName +'/js/src/angular_1.4/tools/hierarchies/templates/hierSrcDialog.html',
			parent: angular.element(document.body),
			locals: {
				   translate: $scope.translate,
		           hier:  item,
		           metadata : metadata
		         },
			preserveScope : true,
			clickOutsideToClose:false,
			controller: DialogController 
		})
		.then(function(item) {
			return item;
		}, function() {
			return null; 
		});
	 
	 	function DialogController($scope, $mdDialog, translate, hier, metadata) {
	 		$scope.translate = translate;
			$scope.hier = hier;
			$scope.metadata = metadata;
	        $scope.closeDialog = function() {
	        	$mdDialog.cancel();
	        }
	        $scope.saveHier = function(){
	        	$mdDialog.hide(hier);
	        }
		 }
	 }
	
	$scope.addHier =  function(item,parent,event){
		var newItem = $scope.editNode({},parent);
		if (newItem !== null && newItem !== undefined){
			item.children.push(newItem);
		}
	}
	
	$scope.modifyHier =  function(item,parent,event){
		var newItem = $scope.editNode(item,parent);
		if (newItem !== null && newItem !== undefined){
			item = newItem;
		}
	}
	
	$scope.duplicateLeaf =  function(item,parent,event){
		var newItem = angular.copy(item);
		if ( $scope.metadataMap[$scope.dimSrc.DIMENSION_NM].ALLOW_DUPLICATE == false){
			//must modify the dates of validity
			newItem = $scope.editNode(newItem,parent);
		}
	}
	
	$scope.deleteHier =  function(item,parent,event){
		
	}
	 
	$scope.menuOptionSrc = [{
			label: $scope.translate.load('sbi.hierarchies.node.add'),
			showItem : function(item,event){
				//visible if it is NOT a leaf
				return item !== undefined && (item.leaf === undefined || item.leaf == false);
				},
			action: $scope.addHier
		},{
			label: 'Duplicate',
			showItem : function(item,event){
				//visible if it IS a leaf
				return item !== undefined && item.leaf !== undefined && item.leaf == true;
				},
			action : $scope.duplicateLeaf
		},{
			label: $scope.translate.load('sbi.hierarchies.node.edit'),
			action : $scope.modifyHier
		},{
			label: $scope.translate.load('sbi.hierarchies.node.delete'),
			action: $scope.deleteHier
		}
	];
	 
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
