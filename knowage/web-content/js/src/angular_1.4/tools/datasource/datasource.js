/**
 * @author Simović Nikola (nikola.simovic@mht.net)
 */
var app = angular.module('dataSourceModule', ['ngMaterial', 'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col','angular-list-detail']);

app.controller('dataSourceController', ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast", "$timeout","sbiModule_messaging", dataSourceFunction]);

var emptyDataSource = {
	label : "",
	descr : "",
	urlConnection: "",
	user: "",
	pwd: "",
	driver: "",
	dialectId: "",
	hibDialectClass: "",
	hibDialectName: "",
	schemaSttribute: "",
	multiSchema: false,
	readOnly: false,
	writeDefault: false	
};

function dataSourceFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout,sbiModule_messaging){
	
	//DECLARING VARIABLES
	$scope.showMe=false;
	$scope.translate = sbiModule_translate;
	$scope.dataSourceList = [];
	$scope.dialects = [];
	$scope.selectedDataSource = {};
	$scope.selectedDataSourceItems = [];
	$scope.isDirty = false;
	$scope.forms = {};
	$scope.isSuperAdmin = superadmin;
	$scope.jdbcOrJndi = {};
	
	$scope.isSuperAdminFunction=function(){               
        return !superadmin;
	};
	
	angular.element(document).ready(function () {
        $scope.getDataSources();
    });
	
	$scope.setDirty = function () {
		$scope.isDirty = true;
	}

	//REST
	$scope.getDataSources = function(){
		
		//GET DATA SOURCES
		sbiModule_restServices.promiseGet("2.0/datasources", "")
		.then(function(response) {
			$scope.dataSourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});		
		
		//GET DIALECT TYPES
		
		sbiModule_restServices.promiseGet("domains", "listValueDescriptionByType","DOMAIN_TYPE=DIALECT_HIB")
		.then(function(response) {
			$scope.dialects = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
		
	};
	
	//REST
	$scope.saveOrUpdateDataSource = function(){
		
		delete $scope.jdbcOrJndi.type;
		
		if($scope.selectedDataSource.hasOwnProperty("dsId")){
			

			var errorU = "Error updating the datasource!"
			
			//MODIFY DATA SOURCE
				
				sbiModule_restServices.promisePut('2.0/datasources','',angular.toJson($scope.selectedDataSource))
				.then(function(response) {
					console.log("[PUT]: SUCCESS!");
					$scope.dataSourceList = [];
					$scope.getDataSources();
					$scope.closeForm();
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				}, function(response) {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
					
				});		
		} else {
			
			var errorS = "Error saving the datasource!";
			
			//CREATE NEW DATA SOURCE
			sbiModule_restServices.promiseGet('2.0/datasources','', angular.toJson($scope.selectedDataSource))
			.then(function(response) {
				console.log("[POST]: SUCCESS!");
				$scope.dataSourceList = [];
				$scope.getDataSources();
				$scope.closeForm();
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
			
		}		
	};
	
	//REST
	$scope.deleteDataSource = function() {
		
		//DELETE SEVERAL DATA SORUCES
		if($scope.selectedDataSourceItems.length > 1) {
			
			sbiModule_restServices.delete("2.0/datasources",queryParamDataSourceIdsToDelete()).success(
					function(data, status, headers, config) {
						console.log(data);
						if (data.hasOwnProperty("errors")) {
							console.log("[DELETE MULTIPLE]: PROPERTY HAS ERRORS!");
						} else {
							console.log("[DELETE MULTIPLE]: SUCCESS!")
							$scope.showActionMultiDelete();
							$scope.closeForm();
							$scope.showActionDelete();
							$scope.selectedDataSourceItems = [];
							$scope.getDataSources();
						}
					}).error(function(data, status, headers, config) {
						console.log("[DELETE MULTIPLE]: FAIL!"+status)
					})
			
		} else {
			
			//DELETE  ONE DATA SOURCE
			
			sbiModule_restServices.promiseDelete("2.0/datasources", $scope.selectedDataSource.dsId)
			.then(function(response) {
				console.log("[DELETE]: SUCCESS!");
				$scope.dataSourceList = [];
				$scope.getDataSources();
				$scope.closeForm();
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
			}, function(response) {
				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
				
			});	
		}
	};
	
	//SHOW RIGHT-COLUMN
	$scope.createNewForm = function () {
		
		if($scope.isDirty==false) {
			$scope.showMe=true;
			$scope.jdbcOrJndi = {type:"JDBC"};
			$scope.selectedDataSource = {
					label : "",
					descr : "",
					dialectId: "",
					multiSchema: false,
					schemaAttribute: "",
					readOnly: false,
					writeDefault: false,
					urlConnection: "",
					user: "",
					pwd: "",
					driver: "",
					jndi: ""
			};
			
		} else {
			
			$mdDialog.show($scope.confirm).then(function() {
				$scope.showMe=true;
				$scope.selectedDataSource = {
						label : "",
						descr : "",
						dialectId: "",
						multiSchema: false,
						readOnly: false,
						writeDefault: false,
						urlConnection: "",
						user: "",
						pwd: "",
						driver: ""				
				};
				
				$scope.isDirty = false;


			}, function() {
				$scope.showMe = true;
			});
		}
		
	};
	
	//LOAD SELECTED SOURCE
	$scope.loadSelectedDataSource = function(item) {
		
		$scope.jdbcOrJndi.type = null;
		$scope.showMe=true;
			
			if($scope.isDirty==false) {
				
				$scope.selectedDataSource = angular.copy(item);
				
			} else {
				
				

				$mdDialog.show($scope.confirm).then(function() {
					
					
					$scope.selectedDataSource = angular.copy(item);
					$scope.isDirty = false;


				}, function() {
					$scope.showMe = true;
				});
			}
			
			$scope.connectionType();
	};
	
	$scope.connectionType = function () {
		
		 if($scope.selectedDataSource.driver){
			 $scope.jdbcOrJndi.type = "JDBC";
		 } 
		 if($scope.selectedDataSource.jndi!="") {
			 $scope.jdbcOrJndi.type = "JNDI";
		 }
		 
	}
	
	//CLOSE RIGHT-COLUMN AND SET SELECTED DATA SORUCE TO AN EMPTY OBJECT
	$scope.closeForm = function(){
		$scope.forms.dataSourceForm.$setPristine();
		$scope.forms.dataSourceForm.$setUntouched();
		$scope.showMe=false;
		$scope.isDirty = false;
		$scope.selectedDataSource = {};
	};
	
	//CONFIRM DELETE
	$scope.showActionDelete = function() {
		var toast = $mdToast.simple()
		.content('Successfully deleted data source!')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	};
	
	//CONFIRM MULTIPLE DELETE
	$scope.showActionMultiDelete = function() {
		var toast = $mdToast.simple()
		.content('Successfully deleted multiple data sources!')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	};
	
	//CONFIRM OK
	$scope.showActionOK = function() {
		var toast = $mdToast.simple()
		.content('Successfully saved data source!!')
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {
			}
		});
	};
	
	//TEST SUCCEEDED
	$scope.showActionTestOK = function() {
		var toast = $mdToast.simple()
		.content('Data Source correctly tested!')
		.action('OK')
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {
			}
		});
	};
	
	//TEST FAILED
	$scope.showActionTestKO = function(e) {
		var toast = $mdToast.simple()
		.content(e)
		.action('OK')
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {
			}
		});
	};
	
	//CREATING PATH FOR DELETING MULTIPLE DATA SOURCES
	queryParamDataSourceIdsToDelete = function(){
		
		   var q="?";
		   
		   for(var i=0; i<$scope.selectedDataSourceItems.length;i++){
			   q+="id="+$scope.selectedDataSourceItems[i].dsId+"&";
		   }
		   
		   return q;
		   
	};
	
	$scope.deleteItem = function (item) {
		console.log(item)
		sbiModule_restServices.promiseDelete("2.0/datasources", item.dsId)
		.then(function(response) {
			console.log("[DELETE]: SUCCESS!");
			$scope.dataSourceList = [];
			$scope.getDataSources();
			$scope.closeForm();
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});	
	}
	
	//REST
	$scope.testDataSource = function () {
		
		//TEST DATA SOURCE
		var testJSON = angular.copy($scope.selectedDataSource);
		
		if(testJSON.hasOwnProperty("dsId")){
			delete testJSON.dsId;
		}
		
		if(testJSON.hasOwnProperty("userIn")) {
			delete testJSON.userIn;
		}
		
		console.log(angular.toJson(testJSON));
		
		sbiModule_restServices.promisePost('2.0/datasources/test','',testJSON)
		.then(function(response) {
			sbiModule_messaging.showInfoMessage("Test is ok", 'Information!');
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
			
		});
	}
	
	//Get user
	$scope.getUser = function() {
		console.log("is super admin - "+$scope.isSuperAdmin);
		
	}
	$scope.getUser();
	
	//SPEED MENU TRASH ITEM
	$scope.dsSpeedMenu= [
	                     {
	                    	label:sbiModule_translate.load("sbi.generic.delete"),
	                    	icon:'fa fa-trash-o fa-lg',
	                    	color:'#153E7E',
	                    	action:function(item,event){
	                    		
	                    		$scope.deleteItem(item);
	                    	}
	                     }
	                    ];
	
	//INFO ABOUT THE JNDI INPUT FORM
	$scope.showJdniInfo = function(ev){
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.content(sbiModule_translate.load("sbi.datasource.jndiname.info"))
					.ok(sbiModule_translate.load("sbi.federationdefinition.template.button.close"))
					.targetEvent(ev)
		);
	}

	$scope.confirm = $mdDialog
	.confirm()
	.title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	.content(
			sbiModule_translate
			.load("sbi.catalogues.generic.modify.msg"))
			.ariaLabel('Lucky day').ok(
					sbiModule_translate.load("sbi.general.yes")).cancel(
							sbiModule_translate.load("sbi.general.No"));
	
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);