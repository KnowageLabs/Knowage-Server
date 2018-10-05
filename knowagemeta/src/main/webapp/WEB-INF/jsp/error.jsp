
<%-- ---------------------------------------------------------------------- --%>
<%-- JAVA IMPORTS             											    --%>
<%-- ---------------------------------------------------------------------- --%>
<%@include file="/WEB-INF/jsp/commons/angular/angularResource.jsp"%>
<%@include file="/WEB-INF/jsp/commons/angular/angularImport.jsp"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html ng-app="metaError">
<head>


<title>Error</title>
<script>
var app = angular.module('metaError', [ 'ngMaterial', 'sbiModule' ]);

app.config([ '$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
} ]);

app.controller('errorController', [ '$scope', 'sbiModule_translate','$mdDialog','$window', errorControllerFunction ]);

function errorControllerFunction($scope, sbiModule_translate,$mdDialog,$window) {
	$window.parent.document.getElementById('loadMask').style.display='none';
	
	    $mdDialog.show(
	      $mdDialog.alert()
	        .clickOutsideToClose(true)
	        .title(sbiModule_translate.load("sbi.meta.model.filenotfound.title"))
	        .textContent(sbiModule_translate.load("sbi.meta.model.filenotfound.description"))
	        .ariaLabel(sbiModule_translate.load("sbi.meta.model.filenotfound.description"))
	        .ok(sbiModule_translate.load("sbi.general.close"))
	    );

}




</script>
</head>
<body ng-controller="errorController" >
	<div layout="row">
		
		<div layout="column" flex=95>
		</div>


	</div>
</body>
</html>