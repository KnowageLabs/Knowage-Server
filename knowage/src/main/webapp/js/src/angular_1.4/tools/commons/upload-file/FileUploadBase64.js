/**
 * @authors Alessandro Piovani (alessandro.piovani@eng.it)
 *
 */
var scripts = document.getElementsByTagName("script")
var currentScriptPathFileUploadBase64 = scripts[scripts.length-1].src;

var defaultFileMaxSize = 10 * 1024 * 1024; // 10 MB

angular.module('file_upload_base64', [ 'ngMaterial', 'sbiModule'])
.directive('fileUploadBase64',
		function($compile) {
	return {
		templateUrl: currentScriptPathFileUploadBase64.substring(0, currentScriptPathFileUploadBase64.lastIndexOf('/') + 1) + 'template/file-upload-base64.html',
		transclude : true,
		replace : false,
			scope: {
				ngModel : '='
				, id : "@"
				, label : '=?'
				, ngDisabled : '=?'
				, fileMaxSize : '=?'
			},
	    controller: FileUploadBase64ControllerFunction,
	    controllerAs: 'ctrl',
	    link: function(scope, element, attrs, ctrl, transclude) {

	    	scope.id = "fileUploadBase64" + Math.floor(Math.random() * 1000);
	    	if (attrs.id){
	    		scope.id = attrs.id;
	    	}

	    	scope.textButton = 'Upload';
	    	if (attrs.label){
	    		scope.textButton = attrs.label;
	    	}
	    	if(!attrs.ngModel){
	    		scope.ngModel = {};
	    	}
	    }
	}
});


function FileUploadBase64ControllerFunction($scope,$timeout,$mdDialog,sbiModule_translate){
	$scope.setFile = function (element){

		var getBase64=function(element) {
			   file=element.files[0];
			   var reader = new FileReader();
			   reader.readAsDataURL(file);
			   reader.onload = function () {
				   console.log("File in Base64: ",reader.result);

					var max = $scope.fileMaxSize != undefined ? $scope.fileMaxSize : defaultFileMaxSize;
					var inputFile = element;
				    if (inputFile.files && inputFile.files[0].size > max) {

				    	$mdDialog.show(
					      $mdDialog.alert()
					        .parent(angular.element(document.body))
					        .clickOutsideToClose(true)
					        .title('Error')
					        .textContent('File too large. Max file size is: '+max/1024/1024 + 'MB')
					        .ariaLabel('File too large')
					        .ok(sbiModule_translate.load('sbi.general.ok'))
					    );

				        inputFile.value = null; // Clear the field.
				        return;
				    }
				    if($scope.ngModel==undefined)
				    {
				    	$scope.ngModel={};
				    }
				    if($scope.filename==undefined)
				    {
				    	$scope.filename={};
				    }
					$scope.ngModel.file = element.files[0];
					//$scope.ngModel.file.base64 = reader.result;
					$scope.ngModel.base64 = reader.result;
					$scope.filename = element.files[0] !== undefined ? element.files[0].name : '';
					$scope.ngModel.filename = element.files[0] !== undefined ? element.files[0].name : '';
					$scope.$apply();
			   };
			   reader.onerror = function (error) {
			     console.log('Error uploading file: ', error);
			   };
		}
		getBase64(element);


	}

	$scope.$watch('ngModel.file', function(newVal, oldVal){
		if ($scope.ngModel){
			$scope.ngModel.file = newVal;
			$scope.filename = newVal != undefined ? newVal.name : '';

		}
	});
}