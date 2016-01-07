var app = angular.module("UsersManagementModule", ["ngMaterial", "angular_list", "angular_table", "sbiModule", "angular_2_col"]);
app.controller("UsersManagementController", ["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast", "$timeout", UsersManagementFunction]);

function UsersManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout) {

    //VARIABLES

    $scope.showme = false; // flag for showing right side 
    $scope.dirtyForm = false; // flag to check for modification
    $scope.translate = sbiModule_translate;
    $scope.selectedUser = {}; // main item
    $scope.usersList = []; // array that hold list of users
    $scope.usersRoles = []; // array that hold list of roles
    $scope.usersAttributes = [];
    $scope.tempAttributes = [];
    $scope.role = [];

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

    $scope.umSpeedMenu = [{
        label: sbiModule_translate.load("sbi.generic.delete"),
        icon: 'fa fa-trash-o fa-lg',
        color: '#153E7E',
        action: function (item, event) {

            $scope.deleteUser(item);
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
        $scope.getUsers();
        $scope.getRoles();
        $scope.getAttributes();
    });

    $scope.setDirty = function () {
        $scope.dirtyForm = true;
    }
    
    /*
     * 	this function is used to properly fill
     *  attributes table with attributes from
     *  selected user																	
     */
    $scope.setAttributes = function () {
        $scope.tempAttributes = [];
        for (i = 0; i < $scope.usersAttributes.length; i++) {
            var obj = {};
            obj.id = $scope.usersAttributes[i].attributeId;
            obj.name = $scope.usersAttributes[i].attributeName;
            if ($scope.selectedUser.hasOwnProperty("sbiUserAttributeses")) {
                if ($scope.selectedUser.sbiUserAttributeses.hasOwnProperty($scope.usersAttributes[i].attributeId)) {
                    obj.value = $scope.selectedUser.sbiUserAttributeses[$scope.usersAttributes[i].attributeId][$scope.usersAttributes[i].attributeName];
                } else {
                    obj.value = "";
                }

            } else {
                obj.value = "";
            }
            $scope.tempAttributes.push(obj);
        }

    }
    
    /*
     * 	this function is used to properly fill
     *  roles table with roles from
     *  selected user																	
     */
    $scope.setRoles = function () {
        $scope.role = [];
        for (var i = 0; i < $scope.usersRoles.length; i++) {
            for (var j = 0; j < $scope.selectedUser.sbiExtUserRoleses.length; j++) {
                if ($scope.selectedUser.sbiExtUserRoleses[j] == $scope.usersRoles[i].id) {
                    $scope.role.push($scope.usersRoles[i]);
                }
            }
        }
    }
    
    /*
     * 	this function is used to properly format
     *  selected users roles and attributes
     *  for adding or updating.
     *  																	
     */
    $scope.formatUser = function () {
        var tmpR = [];
        var tmpA = {};
        for (var i = 0; i < $scope.role.length; i++) {
            tmpR.push($scope.role[i].id);
        }
        $scope.selectedUser.sbiExtUserRoleses = tmpR;
        for (var i = 0; i < $scope.tempAttributes.length; i++) {
            if ($scope.tempAttributes[i].hasOwnProperty("value") && $scope.tempAttributes[i].value != "") {
                tmpA[$scope.tempAttributes[i].id] = {};
                tmpA[$scope.tempAttributes[i].id][$scope.tempAttributes[i].name] = $scope.tempAttributes[i].value;
            }
        }
        $scope.selectedUser.sbiUserAttributeses = tmpA;
        delete $scope.selectedUser.confirm;
    }
    
    $scope.addConfirmPwdProp = function() {
   	 for ( var l in $scope.usersList) {
   		$scope.usersList[l].confirm = null;
		}
	}
    
    $scope.loadUser = function (item) { // this function is called when item from custom table is clicked
    	console.log($scope.selectedUser);
        if ($scope.dirtyForm) {
            $mdDialog.show($scope.confirm).then(function () {
                $scope.dirtyForm = false;
                $scope.selectedUser = angular.copy(item);
                $scope.setRoles();
                $scope.setAttributes();
                $scope.showme = true;
                $scope.selectedUser.confirm = $scope.selectedUser.password;
             
                
            }, function () {
                $scope.showme = true;
                $scope.selectedUser.confirm = $scope.selectedUser.password;
              
                
            });

        } else {

            $scope.selectedUser = angular.copy(item);
            $scope.setRoles();
            $scope.setAttributes();
            $scope.showme = true;
            $scope.selectedUser.confirm = $scope.selectedUser.password;
          
        }
    }

    $scope.cancel = function () { // on cancel button
        $scope.selectedUser = {};
        $scope.showme = false;
        $scope.dirtyForm = false;
        $scope.tempAttributes = [];
        $scope.role = [];
    }

    

    $scope.createUser = function () { // this function is called when clicking on plus button
        $scope.setAttributes();
        if ($scope.dirtyForm) {
            $mdDialog.show($scope.confirm).then(function () {
            	
                $scope.dirtyForm = false;
                $scope.selectedUser = {};
                $scope.showme = true;
                $scope.setAttributes();


            }, function () {
            	
                $scope.showme = true;
                
            });

        } else {
            $scope.selectedUser = {};
            $scope.showme = true;
            $scope.setAttributes();
        }
    }

    $scope.saveUser = function () { // this function is called when clicking on save button
        $scope.formatUser();
        if($scope.selectedUser.hasOwnProperty("id")){ // if item already exists do update PUT
			sbiModule_restServices
		    .put("2.0/users",$scope.selectedUser.id,$scope.selectedUser).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.usersList=[];
							$timeout(function(){								
								$scope.getUsers();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.updated"));
							$scope.selectedUser={};
							$scope.showme=false;
							$scope.dirtyForm=false;	
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					})	
			
		}else{ // create new item in database POST
			sbiModule_restServices
		    .post("2.0/users","",angular.toJson($scope.selectedUser, true)).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							$scope.usersList=[];
							$timeout(function(){								
								$scope.getUsers();
							}, 1000);
							$scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.created"));
							$scope.selectedUser={};
							$scope.showme=false;
							$scope.dirtyForm=false;
						}
					}).error(function(data, status, headers, config) {
						console.log(sbiModule_translate.load("sbi.glossary.load.error"));
					})	
			
			
		}
    }

    $scope.getUsers = function () { // service that gets list of users GET
        sbiModule_restServices.get("2.0", "users").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                    $scope.usersList = data;
                    $scope.addConfirmPwdProp();
                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));
        })
    }
    
    $scope.getRoles = function () { // service that gets list of roles GET
        sbiModule_restServices.get("2.0", "roles").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                    $scope.usersRoles = data;
                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
    
    $scope.getAttributes = function () { // service that gets list of roles GET
        sbiModule_restServices.get("2.0", "attributes").success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                    $scope.usersAttributes = data;

                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
    
    $scope.deleteUser = function (item) { // this function is called when clicking on delete button
        sbiModule_restServices.delete("2.0/users", item.id).success(
            function (data, status, headers, config) {
                if (data.hasOwnProperty("errors")) {
                    console.log(sbiModule_translate.load("sbi.glossary.load.error"));
                } else {
                    $scope.usersList = [];
                    $timeout(function () {
                        $scope.getUsers();
                    }, 1000);
                    $scope.showActionOK(sbiModule_translate.load("sbi.catalogues.toast.deleted"));
                    $scope.selectedUser = {};
                    $scope.showme = false;
                    $scope.dirtyForm = false;
                }
            }).error(function (data, status, headers, config) {
            console.log(sbiModule_translate.load("sbi.glossary.load.error"));

        })
    }
};

/*
 * 	this directive is used for
 *  password fields matching.
 *  its not my code found this
 *  snippet on internet and it 
 *  worked best
 * 																	
 */
app.directive('nxEqualEx', function() {
    return {
        require: 'ngModel',
        link: function (scope, elem, attrs, model) {
            if (!attrs.nxEqualEx) {
                console.error('nxEqualEx expects a model as an argument!');
                return;
            }
            scope.$watch(attrs.nxEqualEx, function (value) {
                // Only compare values if the second ctrl has a value.
                if (model.$viewValue !== undefined && model.$viewValue !== '') {
                    model.$setValidity('nxEqualEx', value === model.$viewValue);
                }
            });
            model.$parsers.push(function (value) {
                // Mute the nxEqual error if the second ctrl is empty.
                if (value === undefined || value === '') {
                    model.$setValidity('nxEqualEx', true);
                    return value;
                }
                var isValid = value === scope.$eval(attrs.nxEqualEx);
                model.$setValidity('nxEqualEx', isValid);
                return isValid ? value : undefined;
            });
        }
    };
});