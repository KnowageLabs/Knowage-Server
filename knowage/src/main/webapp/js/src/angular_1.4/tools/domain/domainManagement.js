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
agGrid.initialiseAgGridWithAngular1(angular);

angular.module('domainManagementApp', ['angular_table','ngMaterial', 'ngMessages', 'angular_list', 'angular-list-detail', 'sbiModule','agGrid'])
	.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	}])
	.controller('Controller', manageDomainFucntion);

function manageDomainFucntion($angularListDetail,sbiModule_messaging, sbiModule_translate, sbiModule_restServices, kn_regex, $scope, $q,  $mdDialog,sbiModule_config, $timeout) {

	$scope.path = "2.0/domains";
	var headers = {
			'Content-Type': 'application/json'
	};
	$scope.translate = sbiModule_translate;
	$scope.message = sbiModule_messaging;
	$scope.data=[];
	$scope.itemSelected= {};
	$scope.columns = [{"headerName":"valueCd","field":"valueCd"},
		{"headerName":"valueName","field":"valueName"},
		{"headerName":"domainCode","field":"domainCode"},
		{"headerName":"domainName","field":"domainName"},
		{"headerName":"valueDescription","field":"valueDescription",editable:true,cellEditor: "agLargeTextCellEditor"},
		{"headerName":"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},
			suppressSorting:true,suppressFilter:true,width: 100,suppressSizeToFit:true, tooltip: false}];
	$scope.regex = kn_regex;
	
	function buttonRenderer(params){
		return 	'<md-button class="md-icon-button" ng-click="editRow(\''+params.data.valueId+'\')"><md-icon md-font-icon="fa fa-pencil"></md-icon></md-button>'+
				'<md-button class="md-icon-button" ng-click="deleteRow(\''+params.data.valueId+'\')"><md-icon md-font-icon="fa fa-trash"></md-icon></md-button>';
	}
	
	$scope.gridOptions = {
			angularCompileRows: true,
            enableColResize: false,
            enableFilter: true,
            enableSorting: true,
            pagination: true,
            paginationAutoPageSize: true,
            onGridSizeChanged: resizeColumns,
            defaultColDef: {
            	suppressMovable: true,
            	tooltip: function (params) {
                    return params.value;
                },
            }
	};
	
	function resizeColumns(){
		$scope.gridOptions.api.sizeColumnsToFit();
	}

	var rowDefault = {
		valueId : "",
		valueCd : "",
		valueName : "Value Name",
		domainCode : "Domain Code",
		domainName : "Domain Name",
		valueDescription : "Description Default"
	};

	sbiModule_restServices.promiseGet($scope.path, "", null)
		.then(function(response) {
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
					return;
				}
				$scope.data = response.data;
				$scope.gridOptions.api.setColumnDefs($scope.columns);
				$scope.gridOptions.api.setRowData($scope.data);
					$scope.gridOptions.api.sizeColumnsToFit();
			}, function(data, status, headers, config){
				$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
		});

	$scope.addDomain = function(){
		$scope.domain = {};
		$angularListDetail.goToDetail();
	}

	$scope.closeDetail = function(){
		$angularListDetail.goToList();
		$scope.domainForm.$setPristine();
		$scope.domainForm.$setUntouched();
	}
	
	$scope.getRowFromId = function(id){
		for(var r in $scope.data){
			if($scope.data[r].valueId==id){
				return $scope.data[r];
			}
		}
		return false;
	}

	$scope.editRow = function(id) {
		$scope.domain = angular.copy($scope.getRowFromId(id));
		$angularListDetail.goToDetail();
	}

	$scope.saveRow = function(){
		var rowSelected = angular.copy($scope.domain);
		if (rowSelected.valueId !== undefined) {
			$scope.saveModifiedRow(rowSelected);
		}else{
			$scope.saveNewRow(rowSelected);
		}
		$scope.domainForm.$setPristine();
		$scope.domainForm.$setUntouched();
	};

	$scope.saveModifiedRow = function(item){
		var idx = $scope.indexOf($scope.data, item);
		sbiModule_restServices
			.promisePut($scope.path,item.valueId, angular.toJson(item), headers)
				.then(function(response){
					if (response.data.errors != undefined){
						sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
						return;
					}
					$scope.data[idx]=item;
					$angularListDetail.goToList();
					$scope.domain = {};
					$scope.gridOptions.api.setRowData($scope.data);
					$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
				},function(){
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				});
	}


	$scope.saveNewRow = function(item){
		sbiModule_restServices
		.post($scope.path,"",item,headers)
		.then(function successCallback(response) {
			if (response.status == 201){
				item.valueId = response.data;
				$scope.data.splice(0, 0, item);
				$angularListDetail.goToList();
				$scope.gridOptions.api.updateRowData({add: [item], addIndex: 0});
				$scope.domain = {};
				$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
			}
			else {
				item.valueId = "";
				if (response.data.errors != undefined){
					sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
				}else{
					$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + response.data);
				}
			}
		},
		function errorCallback(response) {
			item.valueId = "";
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
  	  			if (rowsSelected.valueId !== undefined) {
  	  				var idx = $scope.indexOf($scope.data, rowsSelected);
  	  				if (idx>=0){
  	  					sbiModule_restServices.promiseDelete($scope.path, rowsSelected.valueId)
  	  						.then(function(response){
  	  							if (response.data.errors != undefined){
  	  								sbiModule_restServices.errorHandler(response.data,$scope.translate.load('sbi.generic.error.msg'));
  	  								return;
  	  							}
  	  							$scope.oldValue = $scope.data.splice(idx, 1);
  	  							//$scope.gridOptions.api.setRowData($scope.data);
  	  							$scope.gridOptions.api.updateRowData({remove: [rowsSelected]});
  	  							$scope.message.showSuccessMessage($scope.translate.load('sbi.generic.operationSucceded'));
  	  						},function(data,status){
  	  							$scope.message.showErrorMessage($scope.translate.load('sbi.generic.error.msg') + data);
  	  						});
  	  				}
  	  			}
  	  		},function(){});
	};

	$scope.labelDetailFunction = function(){
		if ($scope.domain){
			if ($scope.domain.valueId == undefined){
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
			if (myArray[i].valueId == myElement.valueId) {
				return i;
			}
		}
		return -1;
	};
};
