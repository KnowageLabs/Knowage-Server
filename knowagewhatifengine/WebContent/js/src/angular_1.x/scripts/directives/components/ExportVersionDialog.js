var app = angular.module('export_version_dialogs',[]);

app.directive('exportVersionWizard',
	function(){
	return {
	      restrict: 'E',
	      replace: true,
	      templateUrl: '/knowagewhatifengine/html/template/right/export/exportWizard.html'
	  };
	}
)

app.directive('exportVersionFileWizard', 
	function(){
		return{
			restrict:'E',
			replace:true,
			templateUrl: '/knowagewhatifengine/html/template/right/export/exportWizardFile.html'
		};
	}
)

app.directive('exportVersionTableWizard', 
	function(){
		return{
			restrict:'E',
			replace:true,
			templateUrl: '/knowagewhatifengine/html/template/right/export/exportWizardTable.html'
		};
	}
)

app.directive('exportVersionMsg', 
	function(){
		return{
			restrict:'E',
			replace:true,
			templateUrl: '/knowagewhatifengine/html/template/right/export/exportWizardMessage.html'
		};
	}
)