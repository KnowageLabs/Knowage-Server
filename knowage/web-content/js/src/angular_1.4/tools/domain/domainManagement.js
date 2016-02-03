
var app = angular.module('domainManagementApp', ['angular_table','ngMaterial', 'ui.tree', 'angularUtils.directives.dirPagination', 'ng-context-menu',
                                                     'sbiModule']);

app.controller('Controller', ['sbiModule_translate','sbiModule_restServices', '$scope', '$q', '$log', '$mdDialog',"sbiModule_config", manageDomainFucntion ]);


function manageDomainFucntion(sbiModule_translate, sbiModule_restServices, $scope, $q, $log,  $mdDialog,sbiModule_config) {
	
	var s = $scope;
	
	s.vers="2.0";
	s.path = s.vers + "/domains";
	s.translate = sbiModule_translate;
	s.data=[]
	s.itemSelected= {};
	 
	
	var rowDefault = {
		valueId : "",
		valueCd : "",
		valueName : "Value Name",
		domainCode : "Domain Code",
		domainName : "Domain Name",
		valueDescription : "Description Default"
	};
	
	
	sbiModule_restServices.get(s.path, "", null).success(function(data) {
		$scope.data = data;
	});

	//search function for data array
	s.indexOf = function(myArray, myElement) {
		for (var i = 0; i < myArray.length; i++) {
			if (myArray[i].valueId == myElement.valueId) {
				return i;
			}
		}
		return -1;
	};

	// insert row
	s.addRow = function() {
		$mdDialog.show({
			controller: s.dialogController ,
			templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/domain/templates/domainDialogForm.html',
			parent: angular.element(document.body),
			locals : {
				translate : s.translate,
				itemToEdit : undefined
			},
			preserveScope : true,
			clickOutsideToClose:false
		})
		.then(function(newRow) {
			//form is correct, updating grid and db
			headers = {
					'Content-Type': 'application/json'
			};
			sbiModule_restServices
			.post(s.path,"",angular.toJson(newRow),headers)
			.then(function successCallback(response) {
				if (response.status == 201){
					var arrayLocation = response.headers('Location').split('/');
					newRow.valueId = arrayLocation[arrayLocation.length -1];
					s.data.splice(0, 0, newRow);
				}
				else {
					newRow.valueId = "";
				}
			},
			function errorCallback(response) {
				newRow.valueId = "";
			});
		}, function() {
			//form was cancelled, nothing to do 
		});
	};

	
	
	s.editRow = function() {
		var rowSelected = s.itemSelected;
		if (rowSelected.valueId !== undefined) {
			var idx = s.indexOf(s.data, rowSelected);
			$mdDialog.show({
				controller: s.dialogController ,
				templateUrl: sbiModule_config.contextName+'/js/src/angular_1.4/tools/domain/templates/domainDialogForm.html',
				parent: angular.element(document.body),
				locals : {
					translate : s.translate,
					itemToEdit : rowSelected
				},
				preserveScope : true,
				clickOutsideToClose:false
			}).then( function(editRow){
				headers = {
						'Content-Type': 'application/json'
				};
				sbiModule_restServices
				.put(s.path,editRow.valueId, angular.toJson(editRow), headers)
				.success(function(data){
					s.data[idx]=editRow;
				});
			},function(){
				//nothing to do, the request was cancelled
			});
		}
	};
	
	
	s.deleteRow = function() {
		var rowsSelected = s.itemSelected;
		if (rowsSelected.valueId !== undefined) {
				var idx = s.indexOf(s.data, rowsSelected);
				if (idx>=0){
					$oldValue = s.data.splice(idx, 1);
					sbiModule_restServices.delete(s.path, rowsSelected.valueId);
				}
		}
	};

	
	s.dialogController =function ($scope, $mdDialog, translate,itemToEdit) {
		 $scope.translate = translate; 
		 $scope.domain = itemToEdit !== undefined ? itemToEdit : undefined;
		 $scope.cancel = function() {
		    $mdDialog.cancel();
		  };
		  $scope.saveDomain = function(domain) {
			domain.valueId = domain.valueId !== undefined ? domain.valueId : "";
			var newRow = JSON.parse(JSON.stringify(domain));
		    $mdDialog.hide(newRow);
		  };
	}
};
