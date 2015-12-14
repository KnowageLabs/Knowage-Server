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
			scope: {
				ngModel : '='
				, id : "@"
				, label : '='
			},
	    controller: FileUploadControllerFunction,
	    controllerAs: 'ctrl',
	    link: function(scope, element, attrs, ctrl, transclude) {
	    	
	    	scope.id = "fileUpload";
	    	if (attrs.id){
	    		scope.id = attrs.id;
	    	}
	    	
	    	scope.text = "Browse";
	    	if (attrs.label){
	    		scope.text = attrs.label;
	    	}
	    	
	    	scope.fileName = "";
	    }
	}
});


function FileUploadControllerFunction($scope,$timeout){
	$scope.setFile = function (element){
		$scope.ngModel = element.files[0];
		$scope.fileName = element.files[0].name;
	}
}
