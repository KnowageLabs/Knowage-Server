var app = angular.module('hierManager');

app.controller('hierBackupController', ['sbiModule_config','sbiModule_translate','sbiModule_restServices','sbiModule_logger',"$scope",'$mdDialog', hierarchyBackupFunction ]);

var rootStructure = {
		name:'root',
		id:'root',
		root: true,
		children: [],
		leaf:false
		};

function hierarchyBackupFunction(sbiModule_config,sbiModule_translate,sbiModule_restServices, sbiModule_logger, $scope, $mdDialog){
	
	$scope.translate = sbiModule_translate;
	$scope.restService = sbiModule_restServices;
	$scope.log = sbiModule_logger;
	
	/*Initialization backup variable*/
	$scope.hierarchiesTypeBackup = ['MASTER','TECHNICAL'];
	$scope.hierarchiesBackupMap = {};
	$scope.backupTable = [];
	$scope.columnsTable = [];
	$scope.columnSearchTable = [];
	$scope.hierarchiesBackup=[];
	$scope.hierTreeBackup = [];
	$scope.hierTreeMapBackup = {};
	$scope.metadataMap = {};
	
	$scope.backupTable = [];
	
	$scope.columnsTable = [{'name':'name','label':'name','editable':true},{'name':'code','label':'code','editable':false},{'name':'description','label':'description','editable':true},{'name':'type','label':'type','editable':false}];
	$scope.backupSpeedMenu = [{
	    	label: $scope.translate.load('sbi.generic.update2'),
	    	icon:'fa fa-pencil',
	    	color:'#153E7E',
	    	action:function(item,event){
	    		$scope.editBackup(item);
	    		}
    	},{
        	label: $scope.translate.load('sbi.generic.confirmRestore'),
        	icon:'fa fa-undo',
        	color:'#153E7E',
        	action:function(item,event){
        		$scope.restoreBackup(item);
        	}
    	},{
        	label: $scope.translate.load('sbi.generic.delete'),
        	icon:'fa fa-trash',
        	color:'#153E7E',
        	action:function(item,event){
        		$scope.deleteBackup(item);
        	}
    	}];
	
	
	
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
				if (data.errors === undefined){
					$scope.dimensionBackup = angular.copy(data);
				}else{
					$scope.showAlert('ERROR',data.errors[0].message);
				}
			})	
		.error(function(data, status){
			var message = 'GET dimensions error of ' + data + ' with status :' + status;
			$scope.showAlert('ERROR',message);
			
		});
	
	$scope.getHierarchies = function (){
		var type = $scope.hierTypeBackup;
		var dim = $scope.dimBackup;
		var map = $scope.hierarchiesBackupMap; 
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
								$scope.hierarchiesBackup = angular.copy(data);
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})
					.error(function(data, status){
						var message='GET hierarchies error of ' + type +'-'+ dimName + ' with status :' + status;
						$scope.showAlert('ERROR',message);
						
					});
			}else{
				$scope.hierarchiesBackup = map[keyMap];
			}
		}
		//get the metadata for the selected dimension
		$scope.getMetadata(dim);
	}
	
	$scope.getMetadata = function (dim){
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
	
	$scope.getBackupTable = function(){
		var type = $scope.hierTypeBackup;
		var dim = $scope.dimBackup;
		var hier = $scope.hierBackup;
		if (type && dim && hier){
			var config = {
					params : {
						dimension: dim.DIMENSION_NM,
						hierarchyName: hier.HIER_NM,
						hierarchyType: type
					}
			};
			$scope.restService.get("hierarchies","getHierarchyBkps",null,config)
				.success(
					function(data, status, headers, config) {
						if (data.errors === undefined){
							$scope.createTable(data);
							//$scope.backupTable = angular.copy(backupTableFake);
						}else{
							$scope.showAlert('ERROR',data.errors[0].message);
						}
					})	
				.error(function(data, status){
					var message = 'GET backup error of ' + data + ' with status :' + status;
					$scope.showAlert('ERROR',message);
				});
		}
	}
	
	$scope.editBackup = function (backup){
		var dim = $scope.dimBackup;
		if (dim && backup){
			var item = {
						dimension: dim.DIMENSION_NM,
						name: backup.HIER_NM
						};
			var title = $scope.translate.load("sbi.generic.update2");
		    var message =  $scope.translate.load("sbi.hierarchies.backup.modify.message");
			var response = $scope.showConfirm(title,message);
			response.then(function(){
				$scope.restService.post("hierarchies","modifyHierarchy",item)
					.success(
						function(data, status, headers, config) {
							if (data.errors === undefined){
								$scope.getBackupTable();
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})	
					.error(function(data, status){
						var message = 'POST edit backup error of ' + data + ' with status :' + status;
						$scope.showAlert('ERROR',message);
					})
				,function(){}
			});
		}
	}
	
	$scope.restoreBackup = function (backup){
		var dim = $scope.dimBackup;
		if (dim && backup){
			var item = {
					dimension: dim.DIMENSION_NM,
					name: backup.HIER_NM
					};
			var title = $scope.translate.load("sbi.generic.confirmRestore");
		    var message =  $scope.translate.load("sbi.hierarchies.backup.restore.message");
			var response = $scope.showConfirm(title,message);
			response.then (function(){
				$scope.restService.post("hierarchies","restoreHierarchy",item)
					.success(
						function(data, status, headers, config) {
							if (data.errors !== undefined){
								$scope.getBackupTable();
								$scope.showAlert('INFO','Backup restored');
							}else{
								$scope.showAlert('ERROR',data.errors[0].message);
							}
						})	
					.error(function(data, status){
						var message = 'POST restore backup error of ' + data + ' with status :' + status;
						$scope.showAlert('ERROR',message);
					})
				,function(){}
			});
		}
	}
	
	$scope.deleteBackup = function (backup){
		var dim = $scope.dimBackup;
		if (dim && backup){
			var item = {
					dimension: dim.DIMENSION_NM,
					name: backup.HIER_NM
					};
			var title = $scope.translate.load("sbi.generic.delete");
		    var message =  $scope.translate.load("sbi.hierarchies.backup.modify.message");
			var response = $scope.showConfirm(title,message);
			response.then (function(){
				$scope.restService.post("hierarchies","deleteHierarchy",item)
					.success(
							function(data, status, headers, config) {
								if (data.errors === undefined){
									$scope.getBackupTable();
									$scope.showAlert('INFO','Backup deleted');
									//TODO remove item from the table
								}else{
									$scope.showAlert('ERROR',data.errors[0].message);
								}
							})	
					.error(function(data, status){
						var message = 'POST delete backup error of ' + data + ' with status :' + status;
						$scope.showAlert('ERROR',message);
					})
				,function(){
					
				}
			})
		}
	}
	
	/*Initialize the variables of the table [table, columns, columns-search]*/
	$scope.createTable = function(data){
		$scope.backupTable = data.root;
		$scope.columnsTable.splice(0,$scope.columnsTable.length);
		for (var i = 0;i<data.columns.length;i++){
			if (data.columns[i].VISIBLE == true || data.columns[i].VISIBLE == "true"){
				$scope.columnsTable.push({ 'label' : data.columns[i].NAME, 'name': data.columns[i].ID, 'editable': data.columns[i].EDITABLE});
			}
		}
		$scope.columnSearchTable = data.columns_search;
	}
	
	$scope.menuOption = [{
			}];
	
	
	$scope.confirmModification = function(item,cell,listId){
	    var title = $scope.translate.load("sbi.generic.modify");
	    var message =  $scope.translate.load("sbi.hierarchies.backup.modify.message");
	    var response = $scope.showConfirm(title,message,item).then(function(){
	    		$scope.dirtyTable = true;
		    },function(){
		    	
		    });
		return response;
	}
	
	$scope.allowEdit = function(item,cell,listId, row, column){
	    return column.EDITABLE == undefined || column.EDITABLE == true;
	}
	
	
	$scope.showConfirm = function(title,message,backup) {
	    var confirm = $mdDialog
			.confirm()
			.title(title)
			.content(message)
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
	
	$scope.formatDate = function (date){
		return date.getFullYear() + '-' + date.getMonth()+'-'+ date.getDate();
	}
};
