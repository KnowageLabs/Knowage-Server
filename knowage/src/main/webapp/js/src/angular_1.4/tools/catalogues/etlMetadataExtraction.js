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

var app = angular.module('etlMetadata', ['ngMaterial', 'sbiModule','file_upload']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);


app.controller('etlMetadataExtractionController', ["sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast","multipartForm","sbiModule_download","sbiModule_messaging", controllerFunction]);

function controllerFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast, multipartForm, sbiModule_download,sbiModule_messaging) {

	$scope.translate = sbiModule_translate;
	$scope.bmImportingShow;
	$scope.fileObj ={};
	$scope.fileClicked = false;
	$scope.contextName ;

	
	$scope.fileChange = function(){
		$scope.fileClicked = true;  // tells that file input has been clicked
	}
	
	//check if is name dirty 
	$scope.checkChange = function(){

		// if file is new check also file has been added
		if($scope.fileClicked === false){
				$scope.isDirty = false;
		}
		else{
			$scope.isDirty = true;
		}
	}
	
	$scope.importMetadata = function() {
		//alert("Inserted "+$scope.contextName+" - "+$scope.fileObj.fileName);
		
		if($scope.contextName!== undefined && $scope.contextName.length > 0 && $scope.fileObj.fileName !== undefined){
			$scope.bmImportingShow = true;
			//Upload file
			multipartForm.post("1.0/etl/"+$scope.contextName+"/ETLExtract",$scope.fileObj).success(
					
					function(data,status,headers,config){
						if(data.hasOwnProperty("errors")){						
							console.log("[UPLOAD]: DATA HAS ERRORS PROPERTY!");		
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.metadata.etl.error")+":"+data.errors[0].message, 'Error');

						}else{
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.metadata.etl.success"), 'Success!');
							console.log("[UPLOAD]: SUCCESS!");
							$scope.fileObj.fileName = "";
							$scope.fileObj = {};
							$scope.contextName = ""					
						}
						$scope.bmImportingShow = false;

					}).error(function(data, status, headers, config) {
								console.log("[UPLOAD]: FAIL!"+status);
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.ds.failedToUpload"), 'Error');
								$scope.bmImportingShow = false;
							});
		}
	}
}

app.directive('fileModel',['$parse',function($parse){
	
	return {
		restrict:'A',
		link: function(scope,element,attrs){
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;
			
			element.bind('change',function(){
				scope.$apply(function(){
					modelSetter(scope,element[0].files[0]);
					
				})
			})
		}
	}

}]);

app.service('multipartForm',['$http',function($http){
	
	this.post = function(uploadUrl,data){
		
		var formData = new FormData();
		
		formData.append("file",data.file);

		return	$http.post(uploadUrl,formData,{
				transformRequest:angular.identity,
				headers:{'Content-Type': undefined}
			})
	}
	
}]);