var app = angular.module('hierManager');

app.controller('hierTechnController', ['sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyTechFunction ]);

var rootStructure = {
		name:'root',
		id:'root',
		root: true,
		children: [],
		leaf:false
		};

function hierarchyTechFunction(sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	$scope.filterBySrc = '';
	$scope.orderByFields = ['name','id'];
	$scope.doBackup = true;
	/*Initialization Source variable*/
	$scope.hierarchiesTypeSrc = ['Master', $scope.translate.load('sbi.hierarchies.type.technical')];
	$scope.dateSrc = new Date();
	//$scope.dateFilterSrc = new Date();
	$scope.hierarchiesSrcMap = {};
	$scope.dimensionSrc = [];
	$scope.hierarchiesSrc=[];
	$scope.hierTreeSrc = [];
	$scope.hierTreeMapSrc = {};
	$scope.metadataMap = {};
	$scope.seeFilterSrc=false;

	/*Initialization Target variable*/
	$scope.dateTarget = new Date();
	//$scope.dateFilterTarget = new Date();
	$scope.hierarchiesTargetMap = {};
	$scope.hierTreeTarget = [];
	//$scope.dimensionTarget = [];
	$scope.hierarchiesTarget = [];
	$scope.hierTreeMapTarget = {};
	$scope.metadataMap = {};
	$scope.seeFilterTarget=false;
	$scope.treeTargetDirty=false;
	$scope.targetIsNew = false; //flag, if is true, the tree create from user, else is get from server
	
	$scope.keys = {'subfolders' : 'children'};
	
	$scope.hierTreeSrc.push(angular.copy(dataJson));
	$scope.hierTreeTarget.push(angular.copy(dataJson));
	
	/*Drag and Drop option*/
	$scope.treeTargetOptions = {
		beforeDrop : function(e){
			$scope.treeTargetDirty = true;
		}
	};
	
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
				//$scope.dimensionTarget = angular.copy(data);
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getHierarchies = function (choose){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc;
		var map = choose == 'src' ? $scope.hierarchiesSrcMap : $scope.hierarchiesTargetMap; 
		if (type !== undefined && dim !== undefined){
			var dimName = dim.DIMENSION_NM;
			var keyMap = type+'_'+dimName; 
			var serviceName = (type.toUpperCase() == 'AUTO' || type.toUpperCase() == 'MASTER' )? 'getHierarchiesMaster' : 'getHierarchiesTechnical';
			
			//if the hierarchies[dim][type] is not defined, get the hierarchies and save in the map. Else, get them from the map 
			if (map[keyMap] === undefined){
				$scope.restService.get("hierarchies",serviceName,"dimension="+dimName)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								map[keyMap] = data;
								choose == 'src' ?  $scope.hierarchiesSrc = angular.copy(data) : $scope.hierarchiesTarget = angular.copy(data);
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
	
	$scope.getTree = function(choose,dateFilter,seeElement){
		var type = choose == 'src' ? $scope.hierTypeSrc : 'Technical' ;
		//var dim = choose == 'src' ?  $scope.dimSrc : $scope.dimTarget;
		var dim = $scope.dimSrc ;
		var date = choose == 'src' ? $scope.dateSrc : $scope.dateTarget;
		var hier = choose == 'src' ?  $scope.hierSrc : $scope.hierTarget;
		if (type && dim && hier && date){
			var dateFormatted =$scope.formatDate(date);
			var keyMap = type + '_' + dim.DIMENSION_NM + '_' + hier.HIER_NM + '_' + dateFormatted;
			var config = {};
			config.params = {
				dimension: dim.DIMENSION_NM,
				filterType : type,
				filterHierarchy : hier.HIER_NM,
				validityDate : dateFormatted
			};
			if (dateFilter !== undefined && dateFilter!== null && dateFilter.length > 0){
				config.params.filterDate = ''+dateFilter;
				keyMap = keyMap + '_' + dateFilter;
			}
			if (seeElement == true){
				config.params.filterDimension = seeElement;
				keyMap = keyMap + '_' + seeElement;
			}
			if ($scope.hierTreeMapSrc[keyMap] === undefined && $scope.hierTreeMapTarget[keyMap] === undefined ){
				$scope.restService.get("hierarchies","getHierarchyTree",null,config)
					.success(
						function(data, status, headers, config) {
							if (data !== undefined && data.errors === undefined){
								if (typeof data =='object'){
									data = [data];
								}
								choose =='src' ? $scope.hierTreeSrc = data : $scope.hierTreeTarget = data;
								choose =='src' ? $scope.hierTreeMapSrc[keyMap] = data : $scope.hierTreeMapTarget[keyMap] = data;
								$scope.targetIsNew = false;
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
			}else{
				choose =='src' ? $scope.hierTreeSrc = $scope.hierTreeMapSrc[keyMap] : $scope.hierTreeTarget = $scope.hierTreeMapTarget[keyMap];
			}
		}	
	}
	
	 $scope.editNode = function(item,parent){
		 var parentEl = angular.element(document.body);
		 var dimName = $scope.dimSrc  !== undefined ? $scope.dimSrc.DIMENSION_NM : '';
		 var metTmp =  $scope.metadataMap[dimName];
		 if (metTmp === undefined){
			 $scope.showAlert('Error','No metadata found for dimension '+ dimName );
		 }
		 //take generals_fields if it is root[parent is null], leaf_fields if it is leaf or node_fields if it is node
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
		promise //TODO correggere inserimento
			.then(function(newItem){
					var tmpItem =angular.copy(item);
					for ( key in newItem){ 
						key =='children' ? tmpItem.children = [] : tmpItem[key] = newItem[key];
					}
					tmpItem.expanded = false;
					item.children.splice(0,0,tmpItem);
					$scope.treeTargetDirty = true;
				},function(){
				//nothing to do, request cancelled.
			});
	}
	
	$scope.modifyHier =  function(item,parent,event){
		var newItem = $scope.editNode(item,parent);
		if (newItem !== null && newItem !== undefined){
			item = newItem;
			$scope.treeTargetDirty = true;
		}
	}
	/*Clone the hierarchy of the tree with context menu. If the hier not allows duplicate, show Dialog to modify the new hier*/
	$scope.duplicateLeaf =  function(item,parent,event){
		var newItem = angular.copy(item);
		if ($scope.dimSrc && $scope.dimSrc.DIMENSION_NM && $scope.dimSrc.DIMENSION_NM.length > 0){ 
			var idx = $scope.indexOf(parent.children,item,'id');
			var allowDuplicate = $scope.metadataMap[$scope.dimSrc.DIMENSION_NM].CONFIGS.ALLOW_DUPLICATE;
			if (allowDuplicate == false || allowDuplicate == "false"){
				//must modify the dates of validity
				var promise = $scope.editNode(newItem,parent);
				promise.then(
					function(newItem){
						//check if newItem is totally equal to the old
						var isEqual=true;
						for ( k in newItem){
							if (newItem[k] != item[k]){
								isEqual = false;
								break;
							}
						}
						//if it is equal show Alert
						if (isEqual){
							$show.alert('ERROR', 'The duplicate leaf can not be equal to the original');
						}else{
							if (idx >=0){
								parent.children.splice(idx,0,newItem);
								$scope.treeTargetDirty = true;
							}
						}
					},function(){}	
				);
			}else{
				if (idx >=0){
					parent.children.splice(idx,0,newItem);
					$scope.treeTargetDirty = true;
				}
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
				$scope.treeTargetDirty = true;
			}, function() {
				//nothing to do, response is 'cancel'
		});
	}
	
	$scope.menuTargetOption = [{
			label: $scope.translate.load('sbi.generic.add'),
			showItem : function(item,event){
				//visible if it is NOT a leaf
				return item !== undefined && (item.leaf === undefined || item.leaf == false);
				},
			action: $scope.addHier
		},{
			label: $scope.translate.load('sbi.generic.clone'),
			showItem : function(item,event){
				//visible if it IS a leaf
				return item !== undefined && item.leaf !== undefined && item.leaf == true;
				},
			action : $scope.duplicateLeaf
		},{
			label: $scope.translate.load('sbi.roles.edit'),
			action : $scope.modifyHier
		},{
			label: $scope.translate.load('sbi.generic.delete'),
			action: $scope.deleteHier
		}
	];
	 
	$scope.applyFilter = function(choose){
		//use to apply the filter only when is clicked the icon
		var date = choose == 'src' ? $scope.dateFilterSrc : $scope.dateFilterTarget;
		var seeElement = choose == 'src' ? $scope.seeHideLeafSrc : $scope.seeHideLeafTarget;
		var dateFormatted;
		
		if (date !== undefined){
			dateFormatted = $scope.formatDate(date);
		}
		//get the Tree if one off two filters are active
		if ((seeElement !== undefined &&  seeElement != false) || (dateFormatted !== undefined && dateFormatted.length>0)){
			$scope.getTree('src', dateFormatted, seeElement);
		}
		//apply filter on source side (left) or target side (right)
		choose == 'src' ? $scope.filterBySrcTrigger = angular.copy($scope.filterBySrc) : $scope.filterByTargetTrigger = angular.copy($scope.filterByTarget);
		choose == 'src' ? $scope.orderBySrcTrigger = angular.copy($scope.orderBySrc) : $scope.orderByTargetTrigger = angular.copy($scope.orderByTarget);
	}
	
	$scope.removeFilter = function(choose){
		if (choose = 'src'){
			$scope.filterBySrcTrigger = "";
			$scope.filterBySrc = "";
			$scope.orderBySrcTrigger = "";
			$scope.orderBySrc = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafSrc !== undefined &&  $scope.seeHideLeafSrc != false) || ($scope.dateFilterSrc !== undefined && $scope.dateFilterSrc.length>0)){
				$scope.getTree('src');
			}
			$scope.dateFilterSrc = undefined;
			$scope.seeHideLeafSrc = false;
		}else if (choose = 'target'){
			$scope.filterByTargetTrigger = "";
			$scope.filterByTarget = "";
			$scope.orderByTargetTrigger = "";
			$scope.orderByTarget = "";
			//get tree without filters if they were active
			if (($scope.seeHideLeafTarget !== undefined &&  $scope.seeHideLeafTarget != false) || ($scope.dateFilterTarget !== undefined && $scope.dateFilterTarget.length>0)){
				$scope.getTree('target');
			}
			$scope.dateFilterTarget = undefined;
			$scope.seeHideLeafTarget = false;
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
	
	$scope.createTree = function(){
		if ($scope.dateTarget && $scope.dimSrc && $scope.hierTarget){
			var promise = $scope.editNode(angular.copy(rootStructure),null);
			promise
				.then(function(newItem){
						$scope.hierTreeTarget = [newItem]
						$scope.treeTargetDirty = true;
						$scope.targetIsNew = true;
					},function(){
					//nothing to do, request cancelled.
				}); 
		}
	}
	
	$scope.saveTree = function(){
		if ($scope.dateTarget && $scope.dimSrc && $scope.hierTarget && $scope.hierTreeTarget){
			//saveHierarchy
			var root = {};
			root.dimension = $scope.dimSrc.DIMENSION_NM;
			root.code = $scope.hierTarget.HIER_CD;
			root.description = $scope.hierTarget.HIER_DS;
			root.name = $scope.hierTarget.HIER_NM;
			root.type = $scope.hierTarget.HIER_TP;
			root.dateValidity = $scope.formatDate($scope.dateTarget);
			root.isInsert = $scope.targetIsNew;
			root.doBackup = $scope.doBackup !== undefined ? $scope.doBackup : false;
			root.root = Array.isArray($scope.hierTreeTarget) ? angular.copy($scope.hierTreeTarget[0]) : angular.copy($scope.hierTreeTarget);
			root.root.$parent = undefined;
			//remove cycle object [E.g. possible cycle -> item.$parent.children[0] = item]
			var elements = [root.root];
			do{
				var el = elements.shift();
				el.$parent=null;
				if (el.children !== undefined && el.children.length > 0){
					for (var i =0 ; i<el.children.length;i++){
						elements.push(el.children[i]);
					}
				}
			}while(elements.length > 0);
			
			var jsonString = angular.toJson(root);
			var promise = $scope.restService.post('hierarchies','saveHierarchy',jsonString);
			promise
				.success(function (data){
					if (data.errors === undefined){
						$scope.treeTargetDirty = false;
						$scope.targetIsNew = false;
						$scope.showAlert('INFO','Succesfull upate');
					}else{
						$scope.showAlert('ERROR',data.errors[0].message);
					}
				})
				.error(function(data,status){
					$scope.showAlert('ERROR','Impossible to save the Tree');
				});
		}
	}
	
	$scope.formatDate = function (date){
		var mm = date.getMonth() < 10 ? '0'+ (date.getMonth()+1) : ''+(date.getMonth()+1);
		return date.getFullYear() + '-' + mm +'-'+ date.getDate();
	}
	
	$scope.debug = function (){
		var none='none';
	}
};
