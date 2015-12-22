/**
 * @authors Alessio Conese (alessio.conese@eng.it)
 * 
 */
var scripts = document.getElementsByTagName("script")
var currentScriptPathFileUpload = scripts[scripts.length-1].src;

angular.module('file_upload', [ 'ngMaterial'])
.directive('fileUpload',
		function($compile) {
	return {
		templateUrl: currentScriptPathFileUpload.substring(0, currentScriptPathFileUpload.lastIndexOf('/') + 1) + 'template/file-upload.html',
		transclude : true,
		replace : false,
			scope: {
				ngModel : '='
				, id : "@"
				, label : '=?'
				, ngDisabled : '=?'
			},
	    controller: FileUploadControllerFunction,
	    controllerAs: 'ctrl',
	    link: function(scope, element, attrs, ctrl, transclude) {
	    	
	    	scope.id = "fileUpload" + Math.floor(Math.random() * 1000);
	    	if (attrs.id){
	    		scope.id = attrs.id;
	    	}
	    	
	    	scope.textButton = "Browse";
	    	if (attrs.label){
	    		scope.textButton = attrs.label;
	    	}
	    	if(!attrs.ngModel){
	    		scope.ngModel = {};
	    	}
	    }
	}
});


function FileUploadControllerFunction($scope,$timeout){
	$scope.setFile = function (element){
		$scope.ngModel.file = element.files[0];
		$scope.fileName = element.files[0] !== undefined ? element.files[0].name : '';
		$scope.ngModel.fileName = element.files[0] !== undefined ? element.files[0].name : '';
		$scope.$apply();
	}
	
	$scope.$watch('ngModel.file', function(newVal, oldVal){
		if ($scope.ngModel){
			$scope.ngModel.file = newVal;
			$scope.fileName = newVal !== undefined ? newVal.name : '';
		}
	});
}
