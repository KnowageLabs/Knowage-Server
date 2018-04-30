/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Simović Nikola (nikola.simovic@mht.net)
 */
var app = angular.module('dataSourceModule', ['ngMaterial', 'angular_list', 'angular_table' ,'sbiModule', 'angular_2_col','angular-list-detail']);

app.controller('dataSourceController', ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast",
                                        "$timeout","sbiModule_messaging","sbiModule_user","sbiModule_messaging", dataSourceFunction]);

app.filter('fromMillitoSeconds', function(){
	return function(input){
		var milisecond = 1000;
		var seconds = input / milisecond;
		return seconds;
	}
}); 

var emptyDataSource = {
	label : "",
	descr : "",
	urlConnection: "",
	user: "",
	pwd: "",
	driver: "",
	dialectName: "",
	schemaSttribute: "",
	multiSchema: false,
	readOnly: false,
	writeDefault: false
};

function dataSourceFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout,sbiModule_messaging,sbiModule_user,sbiModule_messaging){

	//DECLARING VARIABLES
	$scope.showMe=false;
	$scope.translate = sbiModule_translate;
	$scope.dataSourceList = [];
	$scope.databases = [];
	$scope.selectedDatabase = []
	$scope.selectedDataSource = {};
	$scope.selectedDataSourceItems = [];
	$scope.isDirty = false;
	$scope.readOnly= false;
	$scope.forms = {};
	$scope.isSuperAdmin = superadmin;
	$scope.jdbcOrJndi = {};
	$scope.currentUser = sbiModule_user.userUniqueIdentifier;
	$scope.JDBCAdvancedOptionsShow = false;

	$scope.isSuperAdminFunction=function(){
        return superadmin;
	};

	angular.element(document).ready(function () {
        $scope.getDataSources();

    });

	$scope.setDirty = function () {
		$scope.isDirty = true;
	};

	//EXPAND - COLAPS ADVANCED OPTIONS MENU
	$scope.showAdvancedOptions = function() {
		$scope.JDBCAdvancedOptionsShow = !$scope.JDBCAdvancedOptionsShow;
	};
	
	//REST
	$scope.getDataSources = function(){

		//GET DATA SOURCES
		sbiModule_restServices.promiseGet("2.0/datasources", "")
		.then(function(response) {
			convertMilliToSeconds(response.data);
			$scope.dataSourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});

		//GET DATABASE TYPES
		sbiModule_restServices.promiseGet("2.0/databases", "")
		.then(function(response) {
			$scope.databases = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});

	};
	
	//Function for conversion from milliseconds to seconds for JDBC Advanced Options
	var convertMilliToSeconds = function(dataSourceArr) {
		dataSourceArr.forEach(function(dataSource){
			if(dataSource.hasOwnProperty('jdbcPoolConfiguration')) {
				dataSource.jdbcPoolConfiguration.maxWait = dataSource.jdbcPoolConfiguration.maxWait / 1000;
				dataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns = dataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns / 1000;
			}
		});
		return dataSourceArr;
	};
	
	//REST
	$scope.selectDatabase = function(dialectName){
		for (i = 0; i < $scope.databases.length; i++) {
		    if($scope.databases[i].databaseDialect.value == dialectName) {
		    	$scope.selectedDatabase = $scope.databases[i];
		    	if(!$scope.selectedDatabase.cacheSupported) {
		    		$scope.selectedDataSource.readOnly = 1;
		    		$scope.selectedDataSource.writeDefault = false;
		    	}
		    }
		}
	};

	//REST
	$scope.saveOrUpdateDataSource = function(){
		if($scope.jdbcOrJndi.type=="JDBC") {
			$scope.selectedDataSource.jndi = "";
			
			//Convert seconds into milliseconds
			$scope.selectedDataSource.jdbcPoolConfiguration.maxWait = $scope.selectedDataSource.jdbcPoolConfiguration.maxWait * 1000;
			$scope.selectedDataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns = $scope.selectedDataSource.jdbcPoolConfiguration.timeBetweenEvictionRuns * 1000;		
		} else if($scope.jdbcOrJndi.type=="JNDI") {
			$scope.selectedDataSource.driver = "";
			$scope.selectedDataSource.pwd = "";
			$scope.selectedDataSource.user = "";
			$scope.selectedDataSource.urlConnection = "";						
		}

		delete $scope.jdbcOrJndi.type;				
				
		if($scope.selectedDataSource.hasOwnProperty("dsId")){

			var errorU = "Error updating the datasource!"

			//MODIFY DATA SOURCE
				$scope.checkReadOnly();
								
				sbiModule_restServices.promisePut('2.0/datasources','', $scope.selectedDataSource)
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
			$scope.checkReadOnly();

			//CREATE NEW DATA SOURCE
			sbiModule_restServices.promisePost('2.0/datasources','', $scope.selectedDataSource)
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

	$scope.clearType = function() {

		if($scope.selectedDataSource.dsId == null){

			if ($scope.jdbcOrJndi.type == 'JDBC') {
				$scope.selectedDataSource.jndi = "";
				$scope.selectedDataSource.jdbcPoolConfiguration = {
					maxWait: 60,
					abandonedTimeout: 60,
					timeBetweenEvictionRuns: 10,
					validationQuery: "",
					removeAbandonedOnBorrow: true,
					removeAbandonedOnMaintenance: true,
					logAbandoned: true,
					testOnReturn: true,
					testWhileIdle: true
				}
			} else {
				$scope.selectedDataSource.urlConnection = "";
				$scope.selectedDataSource.user = "";
				$scope.selectedDataSource.pwd = "";
				$scope.selectedDataSource.driver= "";
				delete $scope.selectedDataSource.jdbcPoolConfiguration;
			}

		}

	};

	$scope.checkReadOnly = function() {
		if($scope.selectedDataSource.readOnly==0){
			$scope.selectedDataSource.readOnly=false;
		} else if($scope.selectedDataSource.readOnly==1){
			$scope.selectedDataSource.readOnly=true;
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
	$scope.createNewDatasource = function () {

		if($scope.isDirty==false) {
			$scope.showMe=true;
			$scope.jdbcOrJndi = {type:"JDBC"};
			$scope.selectedDataSource = {
					label : "",
					descr : "",
					dialectName: "",
					multiSchema: false,
					schemaAttribute: "",
					readOnly: false,
					writeDefault: false,
					urlConnection: "",
					user: "",
					pwd: "",
					driver: "",
					jdbcPoolConfiguration: {
						maxWait: 60,
						abandonedTimeout: 60,
						timeBetweenEvictionRuns: 10,
						validationQuery: "",
						removeAbandonedOnBorrow: true,
						removeAbandonedOnMaintenance: true,
						logAbandoned: true,
						testOnReturn: true,
						testWhileIdle: true
					},
					jndi: ""
			};

		} else {

			$mdDialog.show($scope.confirm).then(function() {
				$scope.showMe=true;
				$scope.selectedDataSource = {
						label : "",
						descr : "",
						dialectName: "",
						multiSchema: false,
						readOnly: false,
						writeDefault: false,
						urlConnection: "",
						user: "",
						pwd: "",
						jdbcPoolConfiguration: {
							maxWait: 60,
							abandonedTimeout: 60,
							timeBetweenEvictionRuns: 10,
							validationQuery: "",
							removeAbandonedOnBorrow: true,
							removeAbandonedOnMaintenance: true,
							logAbandoned: true,
							testOnReturn: true,
							testWhileIdle: true
						},
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
		
		if ($scope.isSuperAdmin){
			$scope.readOnly= false;
		} else if( $scope.currentUser == item.userIn && item.jndi ==""){
			$scope.readOnly= false;
		} else {
			sbiModule_messaging.showInfoMessage("You are not the owner of this catalog", 'Information');
			$scope.readOnly= true;
		}
							
		$scope.jdbcOrJndi.type = null;
		$scope.showMe=true;

		if($scope.isDirty==false) {
			$scope.selectedDataSource = angular.copy(item);
			$scope.selectDatabase($scope.selectedDataSource.dialectName);
		} else {
			$mdDialog.show($scope.confirm).then(function() {
				$scope.selectedDataSource = angular.copy(item);
				$scope.selectDatabase($scope.selectedDataSource.dialectName);
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
		 if($scope.selectedDataSource.jndi!=undefined && $scope.selectedDataSource.jndi!="") {
			 $scope.jdbcOrJndi.type = "JNDI";
		 }

	}

	//CLOSE RIGHT-COLUMN AND SET SELECTED DATA SORUCE TO AN EMPTY OBJECT
	$scope.closeForm = function(){
		$scope.dataSourceForm.$setPristine();
		$scope.dataSourceForm.$setUntouched();
		$scope.showMe=false;
		$scope.isDirty = false;
		$scope.selectedDataSource = {};
	};

	//CONFIRM DELETE
	$scope.showActionDelete = function() {
		sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.datasource.deleted"),"");
	};

	//CONFIRM MULTIPLE DELETE
	$scope.showActionMultiDelete = function() {

		sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.datasource.deleted"),"");
	};

	//CONFIRM OK
	$scope.showActionOK = function() {
		sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.datasource.saved"),"");

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
		if(testJSON.jdbcPoolConfiguration) {
			testJSON.jdbcPoolConfiguration = angular.toJson(testJSON.jdbcPoolConfiguration);
		}
		
		if(testJSON.hasOwnProperty("dsId")){
			delete testJSON.dsId;
		}

		if(testJSON.readOnly=="1"){
			testJSON.readOnly=true;
		} else if(testJSON.readOnly=="0"){
			testJSON.readOnly=false;
		}
		sbiModule_restServices.promisePost('datasourcestest/test','',testJSON)
		.then(function(response) {
			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.datasource.testing.ok"), sbiModule_translate.load("sbi.datasource.info.msg"));
		}, function(response) {
			if(response.data.hasOwnProperty('RemoteException')){
				sbiModule_messaging.showErrorMessage(response.data.RemoteException.message, sbiModule_translate.load("sbi.datasource.error.msg"));
			}else{
				if (response.data.errors[0].message=="") {
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.ds.error.testing.datasource"), sbiModule_translate.load("sbi.datasource.error.msg"));
				} else {
					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.datasource.error.msg"));
				}
			}
		});
	}


	//SPEED MENU TRASH ITEM
	$scope.dsSpeedMenu= [
	                     {
	                    	label:sbiModule_translate.load("sbi.generic.delete"),
	                    	icon:'fa fa-trash-o',
	                    	color:'#a3a5a6',
	                    	action:function(item,event){

	                    		$scope.confirmDelete(item);
	                    	},
	                    	 visible: function(row) {
	             				return row.userIn==$scope.currentUser || $scope.isSuperAdmin ? true : false;
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

	//JDBC ADVANCED OPTIONS INFO DIALOGS
	//Max Wait INFO
	$scope.showMaxWaitInfo = function(event) {
		$mdDialog.show(
				$mdDialog.alert()
						 .clickOutsideToClose(true)
						 .content(sbiModule_translate.load("sbi.datasource.type.jdbc.maxWaitInfo"))
						 .ok(sbiModule_translate.load("sbi.federationdefinition.template.button.close"))
						 .targetEvent(event)
		);
	};
	
	//Abandoned Timeout INFO
	$scope.showAbandonedTimeoutInfo = function(event) {
		$mdDialog.show(
				$mdDialog.alert()
						 .clickOutsideToClose(true)
						 .content(sbiModule_translate.load("sbi.datasource.type.jdbc.abandonedTimeoutInfo"))
						 .ok(sbiModule_translate.load("sbi.federationdefinition.template.button.close"))
						 .targetEvent(event)
		);
	};
	
	//Time between eviction runs INFO
	$scope.showTimeBetweenEvictionRunsInfo = function(event) {
		$mdDialog.show(
				$mdDialog.alert()
						 .clickOutsideToClose(true)
						 .content(sbiModule_translate.load("sbi.datasource.type.jdbc.timeBetweenEvictionRunsInfo"))
						 .ok(sbiModule_translate.load("sbi.federationdefinition.template.button.close"))
						 .targetEvent(event)
		);
	};
	
	//Validation Query INFO
	$scope.showValidationQueryInfo = function(event) {
		$mdDialog.show(
				$mdDialog.alert()
						 .clickOutsideToClose(true)
						 .content(sbiModule_translate.load("sbi.datasource.type.jdbc.validationQueryInfo"))
						 .ok(sbiModule_translate.load("sbi.federationdefinition.template.button.close"))
						 .targetEvent(event)
		);
	};
	
	$scope.confirm = $mdDialog
	.confirm()
	.title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
	.content(
			sbiModule_translate
			.load("sbi.catalogues.generic.modify.msg")).ok(
					sbiModule_translate.load("sbi.general.yes")).cancel(
							sbiModule_translate.load("sbi.general.No"));



	 $scope.confirmDelete = function(item,ev) {
		    var confirm = $mdDialog.confirm()
		          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
		          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
		          .ariaLabel("confirm_delete")
		          .targetEvent(ev)
		          .ok(sbiModule_translate.load("sbi.general.continue"))
		          .cancel(sbiModule_translate.load("sbi.general.cancel"));
		    $mdDialog.show(confirm).then(function() {
		    	$scope.deleteItem(item);
		    }, function() {

		    });
		  };
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);
