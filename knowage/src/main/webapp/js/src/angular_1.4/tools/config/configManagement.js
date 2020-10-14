/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
(function() {
agGrid.initialiseAgGridWithAngular1(angular);
angular
	.module('configManagementApp',  ['angular_table','ngMaterial', 'ngMessages', 'ui.tree', 'angularUtils.directives.dirPagination', 'angular_list', 'angular-list-detail', 'sbiModule', 'angularXRegExp','agGrid'])
	.controller('Controller', manageConfigFunction )
	.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	 }]);

function manageConfigFunction($angularListDetail,sbiModule_messaging, sbiModule_translate, sbiModule_restServices, kn_regex, $scope, $q, $log,  $mdDialog,$filter,sbiModule_config) {

	var path = "2.0/configs";
	var headers = {
			'Content-Type': 'application/json'
	};
	$scope.translate=sbiModule_translate;
	$scope.message = sbiModule_messaging;
	$scope.data=[]
	$scope.configSearchText = '';
	$scope.filterConfig = function(){
		var tempConfigList = $filter('filter')($scope.data,$scope.configSearchText);
		$scope.configurationGridOptions.api.setRowData(tempConfigList);
	}
	$scope.columns = [{"headerName":"Label","field":"label", "tooltipField":"description"},
		{"headerName":"Name","field":"name", "tooltipField":"description"},
		{"headerName":"Value Check","field":"valueCheck"},
		{"headerName":"Category","field":"category"},
		{"headerName":"Active","field":"active"},
		{"headerName":"",cellRenderer: buttonRenderer,"field":"id","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},
			suppressSorting:true,suppressFilter:true,width: 100,suppressSizeToFit:true, tooltip: false}];
	
	function buttonRenderer(params){
		return 	'<md-button class="md-icon-button" ng-click="editRow(\''+params.data.id+'\')"><md-icon md-font-icon="fa fa-pencil"></md-icon></md-button>'+
				'<md-button class="md-icon-button" ng-click="deleteRow(\''+params.data.id+'\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
	}
	
	$scope.configurationGridOptions = {
			angularCompileRows: true,
            enableColResize: false,
            enableFilter: true,
            enableSorting: true,
            pagination: true,
            paginationAutoPageSize: true,
            onGridSizeChanged: resizeColumns,
            columnDefs : $scope.columns,
            defaultColDef: {
            	suppressMovable: true,
            	tooltip: function (params) {
                    return params.value;
                },
            }
	};
	
	function resizeColumns(){
		$scope.configurationGridOptions.api.sizeColumnsToFit();
	}
	
	
	$scope.itemSelected= {};
	$scope.filterCategory=[];
	$scope.hashCategory={};
	$scope.regex = kn_regex;

	var rowDefault = {
		id : "",
		label : "Label",
		name : "Name",
		valueCheck : "value check",
		valueTypeId : 407,
		category : "",
		active : "false"
	};

	$scope.isObjectEmpty = function(obj){
		return Object.keys(obj).length === 0;
	}

	$scope.configSpeedMenu = [{
    	label: $scope.translate.load('sbi.generic.edit'),
    	icon:'fa fa-pencil',
    	color:'#153E7E',
    	action:function(item,event){
    		$scope.editRow(item);
    	}
	},{
    	label: $scope.translate.load('sbi.generic.delete'),
    	icon:'fa fa-trash',
    	color:'#153E7E',
    	action:function(item,event){
    		$scope.deleteRow(item);
    	}
	}];


	sbiModule_restServices.promiseGet(path, "", null).then(function(response) {
		//creating map containing filters categories
		if (response.data.errors != undefined){
			sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
			return;
		}
		var data = response.data;
		for (i=0;i<data.length;i++){
			if ($scope.hashCategory[data[i].category] === undefined){
				$scope.hashCategory[data[i].category]=data[i].category;
			}
			data[i].valueTypeId = data[i].valueTypeId == 407 ? 'NUM' : 'STRING';
		}
		//use the map to create filterCategory array for Grid filter
		for (var k in $scope.hashCategory){
			$scope.filterCategory.push({"value" :k , "label": k});
		}
		$scope.data = data;
		$scope.configurationGridOptions.api.setRowData($scope.data);
		$scope.configurationGridOptions.api.sizeColumnsToFit();
	},function(data, status, headers, config){
		$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
	});

	$scope.addConfig = function(){
		$scope.config = {};
		$angularListDetail.goToDetail();
	}

	$scope.closeDetail = function(){
		$angularListDetail.goToList();
		$scope.configForm.$setPristine();
		$scope.configForm.$setUntouched();
	}
	
	$scope.getRowFromId = function(id){
		for(var r in $scope.data){
			if($scope.data[r].id==id){
				return $scope.data[r];
			}
		}
		return false;
	}
	
	$scope.editRow = function(id) {
		$scope.config = angular.copy($scope.getRowFromId(id));
		$scope.config.valueTypeId = $scope.config.valueTypeId == 'NUM' ? 407 : 408;
		$angularListDetail.goToDetail();
	}

	$scope.saveRow = function(){
		var rowSelected = angular.copy($scope.config);
		if (rowSelected.id !== undefined) {
			$scope.saveModifiedRow(rowSelected);
		}else{
			$scope.saveNewRow(rowSelected);
		}
		$scope.configForm.$setPristine();
		$scope.configForm.$setUntouched();
	};

	$scope.saveModifiedRow = function(item){
		var idx = $scope.indexOf($scope.data, item);
		sbiModule_restServices
			.promisePut(path,item.id, angular.toJson(item), headers)
				.then(function(response){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					item.valueTypeId = item.valueTypeId ==  407 ? 'NUM' : 'STRING';
					$scope.data[idx]=item;
					$angularListDetail.goToList();
					$scope.config = {};
					$scope.configurationGridOptions.api.setRowData($scope.data);
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				},function(){
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				});
	}

	$scope.saveNewRow = function(item){
		sbiModule_restServices
			.promisePost(path,"",angular.toJson(item),headers)
			.then(function successCallback(response) {
				if (response.status == 201){
					item.id = response.data;
					item.valueTypeId = item.valueTypeId == 407 ? 'NUM' : 'STRING';
					$scope.data.splice(0, 0, item);
					$angularListDetail.goToList();
					$scope.config = {};
					$scope.configurationGridOptions.api.updateRowData({add: [item], addIndex: 0})
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				}
				else {
					item.id = "";
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					}else{
						$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
					}
				}
			},
			function errorCallback(response) {
				item.id = "";
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
			});
	}


	$scope.deleteRow = function(id) {
	  var confirm = $mdDialog.confirm()
			        .title($scope.translate.load('sbi.generic.delete'))
			        .content($scope.translate.load('sbi.generic.confirmDelete'))
			        .ok($scope.translate.load('sbi.general.yes'))
			        .cancel($scope.translate.load('sbi.general.No'));
	  $mdDialog
	  	.show(confirm)
	  	.then(function(){
	  			var rowsSelected = $scope.getRowFromId(id);
	  			if (rowsSelected.id !== undefined) {
	  				var idx = $scope.indexOf($scope.data, rowsSelected);
	  				if (idx>=0){
	  					sbiModule_restServices.promiseDelete(path, rowsSelected.id)
	  						.then(function(response){
	  							if (response.data.errors != undefined){
	  								sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
	  								return;
	  							}
	  							$scope.oldValue = $scope.data.splice(idx, 1);
	  							$scope.configurationGridOptions.api.updateRowData({remove: [rowsSelected]});
	  							$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
	  						},function(data,status){
	  							$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
	  						});
	  				}
	  			}
	  		},function(){});
	};

	$scope.labelDetailFunction = function(){
		if ($scope.config){
			if ($scope.config.id == undefined){
				return $scope.translate.load('sbi.generic.new');
			}else{
				return $scope.translate.load('sbi.generic.edit');
			}
		}
		return '';
	}

	//search function for data array
	$scope.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].id == myElement.id) {
				return i;
			}
		}
		return -1;
	};
};
})();
