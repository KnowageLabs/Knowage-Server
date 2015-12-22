var app = angular.module("RolesManagementModule", ["ngMaterial", "angular_list", "angular_table", "sbiModule", "angular_2_col"]);
app.controller("RolesManagementController", ["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", "$timeout", RolesManagementFunction]);

function RolesManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout) {

    //VARIABLES

    $scope.showme = false; // flag for showing right side 
    $scope.dirtyForm = false; // flag to check for modification
    $scope.translate = sbiModule_translate;
    $scope.selectedRole = {}; // main item
    $scope.rolesList = []; // array that hold list of users
    $scope.authList = [];
    $scope.listType = [];
    $scope.auth = [];

    $scope.showActionOK = function (msg) {
        var toast = $mdToast.simple()
            .content(msg)
            .action('OK')
            .highlightAction(false)
            .hideDelay(3000)
            .position('top')

        $mdToast.show(toast).then(function (response) {

            if (response == 'ok') {


            }
        });
    };

    $scope.rmSpeedMenu = [{
        label: sbiModule_translate.load("sbi.generic.delete"),
        icon: 'fa fa-trash-o fa-lg',
        color: '#153E7E',
        action: function (item, event) {

            $scope.deleteRole(item);
        }
    }];


    $scope.confirm = $mdDialog
        .confirm()
        .title(sbiModule_translate.load("sbi.catalogues.generic.modify"))
        .content(
            sbiModule_translate
            .load("sbi.catalogues.generic.modify.msg"))
        .ariaLabel('toast').ok(
            sbiModule_translate.load("sbi.general.continue")).cancel(
            sbiModule_translate.load("sbi.general.cancel"));




    //FUNCTIONS	

    angular.element(document).ready(function () { // on page load function
    	$scope.getRoles();
    	$scope.getDomainType();
    	$scope.getAuthorizations();
    });

    $scope.setDirty = function () {
        $scope.dirtyForm = true;
    }

    
    
    $scope.loadRole = function (item) { // this function is called when item from custom table is clicked
    	console.log($scope.selectedRole);
        if ($scope.dirtyForm) {
            $mdDialog.show($scope.confirm).then(function () {
                $scope.dirtyForm = false;
                $scope.selectedRole = angular.copy(item);
                $scope.showme = true;
              
             
                
            }, function () {
                $scope.showme = true;
                
              
                
            });

        } else {

        	$scope.selectedRole = angular.copy(item);
            $scope.showme = true;
            
          
        }
    }

    $scope.cancel = function () { // on cancel button
    	$scope.selectedRole = {};
        $scope.showme = false;
        $scope.dirtyForm = false;
    }

    

    $scope.createRole = function () { // this function is called when clicking on plus button
        if ($scope.dirtyForm) {
            $mdDialog.show($scope.confirm).then(function () {
            	
            	
                $scope.dirtyForm = false;
                $scope.selectedRole = {};
                $scope.showme = true;
                


            }, function () {
            	
                $scope.showme = true;
                
            });

        } else {
        	$scope.selectedRole = {};
            $scope.showme = true;
        }
    }

    $scope.saveRole = function () { // this function is called when clicking on save button
 
        if($scope.selectedUser.hasOwnProperty("id")){ // if item already exists do update PUT
			sbiModule_restServices
		    .put("2.0/roles",$scope.$scope.selectedRole.id,$scope.selectedRole).success(
					function(data, status, headers, config) {
						console.log(data);
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.rolesList = [];
							$timeout(function(){								
								$scope.getRoles();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.updated"));
							$scope.selectedRole = {};
							$scope.showme=false;
							$scope.dirtyForm=false;	
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						console.log(data);
					})	
			
		}else{ // create new item in database POST
			sbiModule_restServices
		    .post("2.0/roles","",angular.toJson($scope.selectedRole, true)).success(
					function(data, status, headers, config) {
						console.log(data);
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.rolesList=[];
							$timeout(function(){								
								$scope.getRoles();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.created"));
							$scope.selectedRole = {};
							$scope.showme=false;
							$scope.dirtyForm=false;
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						console.log(data);
					})	
			
			
		}
    }
    
    $scope.getRoles = function () { // service that gets list of roles GET
        sbiModule_restServices.get("2.0", "roles").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                	$scope.rolesList = data;

                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
    
    $scope.getDomainType = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("domains", "listValueDescriptionByType","DOMAIN_TYPE=ROLE_TYPE").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.listType = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
    
    $scope.getAuthorizations = function(){ // service that gets domain types for dropdown GET
		sbiModule_restServices.get("2.0/authorizations").success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						$scope.authList = data;
					}
				}).error(function(data, status, headers, config) {
					console.log(sbiModule_translate.load("sbi.glossary.load.error"));

				})	
	}
    $scope.deleteRole = function (item) { // this function is called when clicking on delete button
        sbiModule_restServices.delete("2.0/roles", item.id).success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                	$scope.rolesList = [];
                    $timeout(function () {
                        $scope.getRoles();
                    }, 1000);
                    $scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.deleted"));
                    $scope.selectedRole = {};
                    $scope.showme = false;
                    $scope.dirtyForm = false;
      
                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
};
