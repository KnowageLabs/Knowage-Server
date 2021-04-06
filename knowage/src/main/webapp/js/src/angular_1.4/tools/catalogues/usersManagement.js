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

(function(){

agGrid.initialiseAgGridWithAngular1(angular);
angular.module("UsersManagementModule", ["ngMaterial", "angular_list", "angular_table", "sbiModule", "angular_2_col","angular-list-detail","agGrid"])
	.config(['$mdThemingProvider', function($mdThemingProvider) {
	    $mdThemingProvider.theme('knowage')
	    $mdThemingProvider.setDefaultTheme('knowage');
	 }])

	 .controller("UsersManagementController", UsersManagementFunction)
	 .directive('nxEqualEx', nxEqualExDirective)

function UsersManagementFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, $timeout,sbiModule_messaging, sbiModule_config) {

    //VARIABLES

    $scope.showme = false; // flag for showing right side
    $scope.passwordRequired = true;
    $scope.translate = sbiModule_translate;
    $scope.selectedUser = {}; // main item
    $scope.usersList = []; // array that hold list of users
    $scope.usersRoles = []; // array that hold list of roles
    $scope.usersAttributes = [];
    $scope.tempAttributes = [];
    $scope.role = [];
    $scope.attributeIdAndColumns = [];
    $scope.lovColumns = [];
    $scope.umSpeedMenu = [{
        label: sbiModule_translate.load("sbi.generic.delete"),
        icon: 'fa fa-trash',
        action: function (item, event) {
        	$scope.confirmDelete(item,event);
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


    $scope.confirmDelete = function(item,ev) {
	    var confirm = $mdDialog.confirm()
	          .title(sbiModule_translate.load("sbi.catalogues.toast.confirm.title"))
	          .content(sbiModule_translate.load("sbi.catalogues.toast.confirm.content"))
	          .ariaLabel("confirm_delete")
	          .targetEvent(ev)
	          .ok(sbiModule_translate.load("sbi.general.continue"))
	          .cancel(sbiModule_translate.load("sbi.general.cancel"));
	    $mdDialog.show(confirm).then(function() {
	    	$scope.deleteUser(item);
	    }, function() {

	    });
	  };

    //FUNCTIONS

    angular.element(document).ready(function () { // on page load function
        $scope.getUsers();
        $scope.getRoles();
        $scope.getAttributes();
    });

    /*
     * 	this function is used to properly fill
     *  attributes table with attributes from
     *  selected user
     */
    $scope.setAttributes = function () {
        $scope.tempAttributes = [];


        for (i = 0; i < $scope.usersAttributes.length; i++) {
        	var attributeIdAndColumns = {}
        	var columns = []
            var obj = {};
            obj.id = $scope.usersAttributes[i].attributeId;
            obj.name = $scope.usersAttributes[i].attributeName;
            obj.lovId = $scope.usersAttributes[i].lovId;
            obj.multivalue = $scope.usersAttributes[i].multivalue;
            obj.allowUser = $scope.usersAttributes[i].allowUser;
            obj.syntax = $scope.usersAttributes[i].syntax;

            if($scope.usersAttributes[i].lovId){
            	$scope.getLovsValues(obj)
            }

            if ($scope.selectedUser.hasOwnProperty("sbiUserAttributeses")) {
            	var value = ""
            	if($scope.selectedUser.sbiUserAttributeses.hasOwnProperty($scope.usersAttributes[i].attributeId) ){
            		value =$scope.selectedUser.sbiUserAttributeses[$scope.usersAttributes[i].attributeId][$scope.usersAttributes[i].attributeName];
            		obj.value = value;
            	}
            } else {
                obj.value = "";
            }
            $scope.tempAttributes.push(obj);
        }

    }

    $scope.openLovs = function(ev,attribute) {

        $mdDialog.show({
                controller: lovsDialogController,
                templateUrl: sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/catalogues/templates/lovValues.html',
                targetEvent: ev,
                clickOutsideToClose: true,
               // fullscreen: true,
                locals: {
                	attribute: attribute,
                }
            })
            .then(
                function(answer) {},
                function() {});
    };

    $scope.eraseAttribute = function(e,attribute) {
    	attribute.value = "";
    }

    function lovsDialogController($scope,attribute , $mdDialog) {
    	$scope.attribute = attribute;
    	if(attribute.multivalue){
    	$scope.columnsSelected =[];
    	}else $scope.columnsSelected = {}
    	$scope.lovObjects = [];
    	$scope.lovColumns = attribute.lovColumns;
    	if(attribute.lovColumns && attribute.lovColumns.length > 0){
	    	for(var i = 0; i < attribute.lovColumns.length;i++){
	    		var columnObject = {}
	    		columnObject.column = attribute.lovColumns[i];
	    		if (attribute.value && attribute.value.indexOf(attribute.lovColumns[i]) != -1)
	    			if(attribute.multivalue){
	    			$scope.columnsSelected.push(columnObject)
	    			}else $scope.columnsSelected = columnObject;

	    		$scope.lovObjects.push(columnObject);
	    	}
    	}

        $scope.close = function() {
        	$mdDialog.cancel(); }
        $scope.hide = function() {
        	var values = [];
        	if(angular.isArray($scope.columnsSelected)){
	        	for(var i =0; i< $scope.columnsSelected.length;i++){
	        			values.push($scope.columnsSelected[i].column)
	        	}
        	}else values.push($scope.columnsSelected.column);
        	$scope.attribute.value = values;
        	$mdDialog.hide(); }

    }



    $scope.getLovsValues = function(obj){
		var lovIdAndColumns = {}
		var columns = []
		sbiModule_restServices.promiseGet("2.0/lovs", obj.lovId+'/preview')
		.then(function(response){
			if(response.data[0] && response.data[0].value){
				for(var i = 0; i< response.data.length;i++){
						columns.push(response.data[i].value)
				}
				obj.lovColumns = columns;
			}else
			obj.lovColumns = response.data;
		})
	}
    /*
     * 	this function is used to properly fill
     *  roles table with roles from
     *  selected user
     */
    $scope.setRoles = function () {
        $scope.role = [];
        $scope.rolesGridOptions.api.deselectAll();
        var tempSelectedRoles = angular.copy($scope.selectedUser.sbiExtUserRoleses);
		$scope.rolesGridOptions.api.forEachNode( function(rowNode, index) {
			for(var r in tempSelectedRoles){
				if(rowNode.data.id == tempSelectedRoles[r]){
					rowNode.setSelected(true);
					continue;
				}
			}
		});
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
                if(Array.isArray($scope.tempAttributes[i].value) && $scope.tempAttributes[i].multivalue && $scope.tempAttributes[i].syntax == false){
                	var arrayToSimpleSyntax = "";
                	for(var j = 0; j<$scope.tempAttributes[i].value.length; j++){
                		$scope.tempAttributes[i].value[j] = "'" + $scope.tempAttributes[i].value[j] + "'";
                	 }
                	$scope.tempAttributes[i].value = $scope.tempAttributes[i].value.toString();
                }else if(Array.isArray($scope.tempAttributes[i].value) && $scope.tempAttributes[i].multivalue && $scope.tempAttributes[i].syntax == true){
                	var arrayToComplexSyntax = "";
                		$scope.tempAttributes[i].value = $scope.tempAttributes[i].value.toString();
                		$scope.tempAttributes[i].value = "{,{" + $scope.tempAttributes[i].value + "}}";
                		$scope.tempAttributes[i].value = $scope.tempAttributes[i].value.replace(/,/g,';')

                }else if(Array.isArray($scope.tempAttributes[i].value)){
                	$scope.tempAttributes[i].value = $scope.tempAttributes[i].value.toString();
                }
                tmpA[$scope.tempAttributes[i].id][$scope.tempAttributes[i].name] = $scope.tempAttributes[i].value;
            }else if($scope.tempAttributes[i].value == ""){
            	console.log("skip");
            }
        }
        $scope.selectedUser.sbiUserAttributeses = tmpA;
        delete $scope.selectedUser.confirm;
        delete $scope.selectedUser.warningIcon;
    }

    /*
     * 	this function is used to add
     *  temporary confirm property to
     *  user object
     *
     */
    $scope.addConfirmPwdProp = function() {
   	 for ( var l in $scope.usersList) {
   		$scope.usersList[l].confirm = null;
		}
	}

    $scope.loadUser = function (item) { // this function is called when item from custom table is clicked

    	if ($scope.oldItem && !angular.equals($scope.selectedUser, $scope.oldItem)) {
            $mdDialog.show($scope.confirm).then(function () {
                $scope.showme = true;
                $scope.selectedUser = angular.copy(item);
                $scope.setRoles();
                $scope.setAttributes();
                $scope.selectedUser.confirm = $scope.selectedUser.password;
                $scope.oldItem = angular.copy($scope.selectedUser);

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
            $scope.oldItem = angular.copy($scope.selectedUser);
        }

        $scope.passwordRequired = false;

    }

    $scope.cancel = function () { // on cancel button
        $scope.selectedUser = {};
        $scope.showme = false;
        $scope.tempAttributes = [];
        $scope.role = [];
    }

    $scope.createUser = function () { // this function is called when clicking on plus button
        $scope.setAttributes();
        if ($scope.oldItem && !angular.equals($scope.selectedUser, $scope.oldItem)) {
            $mdDialog.show($scope.confirm).then(function () {

                $scope.selectedUser = {};
                $scope.showme = true;
                $scope.role = [];
                $scope.passwordRequired = true;
                $scope.setAttributes();
                $scope.oldItem = angular.copy($scope.selectedUser);
                $scope.rolesGridOptions.api.deselectAll();

            }, function () {

                $scope.showme = true;

            });

        } else {
                $scope.selectedUser = {};
	            $scope.showme = true;
	            $scope.role = [];
                $scope.passwordRequired = true;
	            $scope.setAttributes();
	            $scope.oldItem = angular.copy($scope.selectedUser);
	            $scope.rolesGridOptions.api.deselectAll();
        }

    }

    $scope.saveUser = function () { // this function is called when clicking on save button
        $scope.formatUser();
        if($scope.selectedUser.hasOwnProperty("id")){ // if item already exists do update PUT

        	sbiModule_restServices.promisePut("2.0/users",$scope.selectedUser.id,$scope.selectedUser)
    		.then(function(response) {
    			$scope.usersList=[];
				$timeout(function(){
					$scope.getUsers();
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
				delete $scope.oldItem;
				$scope.cancel();

    		}, function(response) {
    			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

    		});

		}else{ // create new item in database POST

			sbiModule_restServices.promisePost("2.0/users","",angular.toJson($scope.selectedUser, true))
    		.then(function(response) {
    			$scope.usersList=[];
				$timeout(function(){
					$scope.getUsers();
				}, 1000);
				sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
				$scope.cancel();

    		}, function(response) {
    			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

    		});
		}
    }

    $scope.angularTableColumns = [{"label":"User ID","name":"userId"},	{"label":"Full Name","name":"fullName"}];

    $scope.getUsers = function () { // service that gets list of users GET
    	sbiModule_restServices.promiseGet("2.0", "users")
		.then(function(response) {
			$scope.usersList = response.data;

			$scope.addWarnings();

            $scope.addConfirmPwdProp();

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

    $scope.addWarnings = function() {
    	var warningColumn = {"label":" ","name":"warningIcon", "size":"8%;" };
		var warningColumnIdx = $scope.findColumn(warningColumn, $scope.angularTableColumns);

		if (warningColumnIdx != -1)
			$scope.angularTableColumns.splice(warningColumnIdx, 1);


		for (var idx in $scope.usersList) {
			if ($scope.usersList[idx].blockedByFailedLoginAttempts) {
				$scope.usersList[idx].warningIcon = "<md-icon class='ng-scope md-knowage-theme material-icons fa fa-exclamation-triangle' style='color: red;'></md-icon>";
				$scope.angularTableColumns.push(warningColumn);
				break;
			}
		}
    }

    $scope.findColumn = function (columnToFind, list) {
    	for (var i in list) {
    		if (angular.equals(list[i].label, columnToFind.label) && angular.equals(list[i].name, columnToFind.name)) {
    			return i;
    		}
    	}

    	return -1;
    }

    $scope.unlockUser = function() {
    	$scope.selectedUser.failedLoginAttempts=0;
    	$scope.saveUser();
    }

    $scope.columns = [{"headerName":"Name","field":"name","headerCheckboxSelection":true,"checkboxSelection":true},{"headerName":"Value","field":"description"}]

    $scope.rolesGridOptions = {
        enableColResize: false,
        enableSorting: true,
        domLayout: 'autoHeight',
        onSelectionChanged: rolesSelection,
        onGridSizeChanged: resizeColumns,
        defaultColDef: {
        	suppressMovable: true,
        	tooltip: function (params) {
                return params.value;
            },
        },
        rowSelection: 'multiple',
        rowMultiSelectWithClick: true,
        columnDefs: $scope.columns
	};

    function resizeColumns(grid){
		grid.api.sizeColumnsToFit();
	}

    function rolesSelection(params){
    	$scope.role = $scope.rolesGridOptions.api.getSelectedRows();
	    	for (var i = 0; i < $scope.role.length; i++) {
	    		if ($scope.selectedUser.defaultRoleId && $scope.role[i].id == $scope.selectedUser.defaultRoleId) break;
	    		if (i == $scope.role.length-1) $scope.selectedUser.defaultRoleId = null;
	    	}

    	$scope.$apply();
	}

    $scope.getRoles = function () { // service that gets list of roles GET
    	sbiModule_restServices.promiseGet("2.0", "roles")
		.then(function(response) {
			$scope.usersRoles = response.data;
			$scope.rolesGridOptions.api.setRowData($scope.usersRoles);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

    $scope.getAttributes = function () { // service that gets list of roles GET
    	sbiModule_restServices.promiseGet("2.0", "attributes")
		.then(function(response) {
			if(response.data.length != 0){
				$scope.usersAttributes = response.data;
			}else{
				sbiModule_messaging.showWarningMessage('No user attributes defined', 'Warning');
			}
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
		});
    }

    $scope.deleteUser = function (item) { // this function is called when clicking on delete button

    	sbiModule_restServices.promiseDelete("2.0/users", item.id)
		.then(function(response) {
			 $scope.usersList = [];
             $timeout(function () {
                 $scope.getUsers();
             }, 1000);
             sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
             $scope.selectedUser = {};
             $scope.showme = false;

		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
    }

	$scope.isUserIdEditable = function(user) {
		if (user.hasOwnProperty('id')) {
			return true;
		} else {
			return false;
		}
	}

};

function nxEqualExDirective() {
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
};

})();