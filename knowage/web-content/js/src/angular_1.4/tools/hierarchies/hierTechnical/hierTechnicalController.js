var app = angular.module('hierManager');

app.controller('hierTechnController', ['sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyTechFunction ]);

function hierarchyTechFunction(sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	$scope.filterBySrc = '';
	$scope.orderByFields = ['name','id'];
	
	/*Initialization Source variable*/
	$scope.hierarchiesTypeSrc = ['Master', $scope.translate.load('sbi.hierarchies.type.technical')];
	$scope.dateFilterSrc = new Date();
	$scope.hierarchiesSrcMap = {};
	$scope.dimensionSrc = [];
	$scope.hierarchiesSrc=[];
	$scope.hierTreeSrc = [];
	$scope.metadataMap = {};
	$scope.seeFilterSrc=false;

	/*Initialization Target variable*/
	$scope.dateFilterTarget = new Date();
	$scope.hierarchiesTargetMap = {};
	$scope.hierTreeTarget = [];
	$scope.dimensionTarget = [];
	$scope.hierarchiesTarget = [];
	$scope.metadataMap = {};
	$scope.seeFilterTarget=false;

	$scope.keys = {'subfolders' : 'children'};
	
	$scope.hierTreeSrc.push(angular.copy(dataJson));
	$scope.hierTreeTarget.push(angular.copy(dataJson));
	
	$scope.toogleSeeFilter= function(choose){
		if (choose == 'src'){
			$scope.seeFilterSrc = !$scope.seeFilterSrc;
		}else{
			$scope.seeFilterTarget = !$scope.seeFilterTarget;
		}
	}	
	
	$scope.indexOf = function(myArray, myElement, key) {
		if (myArray ===undefined || myElement === undefined) return -1;
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i][key] !== undefined && myArray[i][key] !== null && myArray[i][key] == myElement[key]) {
				return i;
			}
		}
		return -1;
	};
	
	$scope.restService.get("dimensions","getDimensions")
		.success(
			function(data, status, headers, config) {
				$scope.dimensionSrc = angular.copy(data);
				$scope.dimensionTarget = angular.copy(data);
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getHierarchies = function (choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var map = choose == 'src' ? $scope.hierarchiesSrcMap : $scope.hierarchiesTargetMap; 
		var hierarchies = choose == 'src' ? $scope.hierarchiesSrc : $scope.hierarchiesTarget;
		if (type !== undefined && dim !== undefined){
			hierarchies = [];
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined){
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								hierarchies = angular.copy(data);
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc = hierarchies : $scope.hierarchiesTarget = hierarchies;
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert('ERROR',message);
						
					});
			}else{
				choose == 'src' ? $scope.hierarchiesSrc = map[keyMap] : $scope.hierarchiesTarget = map[keyMap];
			}
		}
		//get the metadata for the dimension
		var dimName = dim.DIMENSION_NM;
		if ($scope.metadataMap !== undefined && $scope.metadataMap[dimName] == undefined){
			$scope.restService.get("hierarchies","nodeMetadata","dimension="+dimName+"&excludeLeaf=false")
			.success(
				function(data, status, headers, config) {
					if (data.errors === undefined){
						$scope.metadataMap[dimName] = data;
					}else{
						$scope.showAlert('ERROR',data.errors[0].message);
					}
			})
			.error(function(data, status){
				var message = 'GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
				$scope.showAlert('ERROR',message);
			});
		}
	}
	
	$scope.getTree = function(choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var date = choose == 'src' ? $scope.dateFilterSrc : $scope.dateFilterTarget;
		var hier = choose == 'src' ?  $scope.hierSrc : $scope.hierTarget
		if (type && dim && hier && date){
			var dateFormatted =date.getFullYear() + '-' + date.getMonth()+'-'+date.getDate();
			var config = {};
			config.params = {
				dimension: dim.DIMENSION_NM,
				filterType : hier.HIER_TP,
				filterHierarchy : hier.HIER_NM,
				validityDate : dateFormatted
			};
			$scope.restService.get("hierarchies","getHierarchyTree",null,config)
				.success(
					function(data, status, headers, config) {
						if (data !== undefined && data.errors === undefined){
							if (typeof data =='object'){
								data = [data];
							}
							choose =='src' ? $scope.hierTreeSrc = data : $scope.hierTreeTarget = data;
						}else{
							var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
							$scope.showAlert('ERROR',data.errors[0].message);
						}
					})
				.error(function(data, status){
					var params = 'date = ' + date + ' dimension = ' + dim.DIMENSION_NM + ' type = ' +  type + ' hierachies = ' + hier.HIER_NM;
					var message='GET tree source error with parameters' + params + ' with status: "' + status+ '"';
					$scope.showAlert('ERROR',message);
				});
		}	
	}
	
	 $scope.editNode = function(item,parent){
		 var parentEl = angular.element(document.body);
		 var dimName = $scope.dimTarget !== undefined ? $scope.dimTarget.DIMENSION_NM : 'ACCOUNT'; //TODO remove hard coded for test
		 var metTmp =  $scope.metadataMap[dimName];
		 if (metTmp === undefined){
			 $scope.showAlert('Error','No metadata found for dimension '+ dimName );
		 }
		 var metadata = parent == undefined || parent == null ? metTmp.GENERAL_FIELDS : item.leaf == true ? metTmp.LEAF_FIELDS : metTmp.NODE_FIELDS;
		 metadata == undefined ? metadata =  metTmp.GENERAL_FIELDS : metadata = metadata; //TODO remove hard coded for test
		 return $mdDialog.show({
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
		var promise = $scope.editNode({},parent);
		promise
			.then(function(newItem){
					var tmpItem =angular.copy(item);
					for ( key in newItem){ tmpItem[key] = newItem[key];}
					tmpItem.children = [];
					tmpItem.expanded = false;
					item.children.splice(0,0,tmpItem);
				},function(){
				//nothing to do, request cancelled.
			});
	}
	
	$scope.modifyHier =  function(item,parent,event){
		var newItem = $scope.editNode(item,parent);
		if (newItem !== null && newItem !== undefined){
			item = newItem;
		}
		
	}
	
	$scope.duplicateLeaf =  function(item,parent,event){
		var newItem = angular.copy(item);
		if ($scope.dimTarget && $scope.dimTarget.DIMENSION_NM && $scope.dimTarget.DIMENSION_NM.length > 0){ 
			if ($scope.metadataMap[$scope.dimTarget.DIMENSION_NM].ALLOW_DUPLICATE == false){
				//must modify the dates of validity
				newItem = $scope.editNode(newItem,parent);
			}
			var idx = $scope.indexOf(parent.children,item,'id');
			if (idx>=0 && newItem !== undefined && newItem !== null){
				parent.children.splice(idx,0,newItem);
			}
		}
	}
	
	$scope.deleteHier =  function(item,parent,event){
		//rest service for deleting
		var response=$scope.showConfirm(item);
		response.then(
			function() {
				if (parent !== undefined && parent !== null){
					var idx = $scope.indexOf(parent.children,item,'id');
					parent.children.splice(idx,1);
				}else{
					item = {};
				}
			}, function() {});
	}
	$scope.menuOptionSrc = [{
		label : 'No action',
		action : function(){}
	}]; 
	
	$scope.menuOptionTarget = [{
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
	 
	$scope.applyFilter = function(choose){
		//use to apply the filter only when is clicked the icon
		if (choose = 'src'){
			$scope.filterBySrcTrigger = angular.copy($scope.filterBySrc);
			$scope.orderBySrcTrigger = angular.copy($scope.orderBySrc);
		} else if (choose = 'target'){
			$scope.filterByTargetTrigger = angular.copy($scope.filterByTarget);
			$scope.orderByTargetTrigger = angular.copy($scope.orderByTarget);
		}
	}
	
	$scope.removeFilter = function(choose){
		if (choose = 'src'){
			$scope.filterBySrcTrigger = "";
			$scope.filterBySrc = "";
			$scope.orderBySrcTrigger = "";
			$scope.orderBySrc = "";
		}else if (choose = 'target'){
			$scope.filterByTargetTrigger = "";
			$scope.filterByTarget = "";
			$scope.orderByTargetTrigger = "";
			$scope.orderByTarget = "";
		}
	}
	
	$scope.showConfirm = function(hier) {
	    var confirm = $mdDialog
			.confirm()
			.title('Delete ' + hier.name.toUpperCase())
			.content('Would you like to delete the item?')
			.ariaLabel('Lucky day')
			.ok('Yes')
			.cancel('No');
	    return  $mdDialog.show(confirm);
  	};
  	
  	//Create an alert dialog with a message
	$scope.showAlert = function (title, message){
		$scope.log.log(message);
		//if angular material version < 1.0.0_rc5 not has textContent function
		if (typeof $mdDialog.alert().textContent == 'function'){
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .textContent(message) //FROM angular material 1.0 
			        .ok('Ok')
				);
		}else {
			$mdDialog.show( 
				$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(title)
			        .content(message)
			        .ok('Ok')
		        );
		}
	};
	
	$scope.debug = function (){
		var none='none';
	}
};
