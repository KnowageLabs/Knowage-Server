angular.module('documentExecutionMasterModule',  [ 'ngMaterial', 'sbiModule','cross_navigation'])
.factory('$documentNavigationScope', function($window) {
	    return $window.parent.angular.element($window.frameElement).scope().$parent;;
})
.controller('docExMasterController',['$scope','sbiModule_translate','$timeout','sourceDocumentExecProperties','sbiModule_config','$crossNavigationHelper','$documentNavigationScope','$mdDialog',docExMasterControllerFunction]);

function docExMasterControllerFunction($scope,sbiModule_translate,$timeout,sourceDocumentExecProperties,sbiModule_config,$crossNavigationHelper,$documentNavigationScope,$mdDialog){
	$scope.crossNavigationHelper=$crossNavigationHelper;
//	$scope.sourceDocumentUrl="";
$scope.executeSourceDocument = function() { 
		var params = {};  
		var url = sbiModule_config.contextName 
			+ '/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/documentexecution/documentExecutionNg.jsp'
			+ '&OBJECT_ID=' + sourceDocumentExecProperties.OBJECT_ID
			+ '&OBJECT_LABEL=' + sourceDocumentExecProperties.OBJECT_LABEL
			+ '&LIGHT_NAVIGATOR_DISABLED=TRUE'
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + sourceDocumentExecProperties.OBJECT_NAME;
		
		var laodSourceDocToCross=function(){
			$timeout(function(){
				if($crossNavigationHelper.crossNavigationSteps.stepControl){
					$crossNavigationHelper.crossNavigationSteps.stepControl.insertBread({name:sourceDocumentExecProperties.OBJECT_LABEL,id:sourceDocumentExecProperties.OBJECT_ID,url:url});
				}else{
					laodSourceDocToCross();
				}
				
				},500);
		};
		laodSourceDocToCross();
		
//		$scope.sourceDocumentUrl=url;  
	};
	$scope.executeSourceDocument();
	
	$scope.closeDocument=function(docId){
		
		if($scope.isNavigationInProgress()){
			var confirm = $mdDialog.confirm()
	         .title(sbiModule_translate.format(sbiModule_translate.load('sbi.browser.close.document.message'), $crossNavigationHelper.crossNavigationSteps.stepItem.length))
	         .content(sbiModule_translate.load('sbi.browser.close.document.confirm'))
	         .ariaLabel('Close tab')
	         .ok(sbiModule_translate.load("sbi.general.continue"))
	         .cancel(sbiModule_translate.load("sbi.general.cancel"));
			   $mdDialog.show(confirm)
			   .then(function() {
				    $crossNavigationHelper;
				   $documentNavigationScope.closeDocument($crossNavigationHelper.crossNavigationSteps.stepItem[0].id);
			   } );
		}else{
			$documentNavigationScope.closeDocument(docId);
		}
		
		 
	}
	
	$scope.changeNavigationRole=function(newRole){
		$scope.crossNavigationHelper.changeNavigationRole(newRole);
	}
	
	$scope.isNavigationInProgress=function(){
		return $crossNavigationHelper.crossNavigationSteps.stepItem.length>1;
	}
}
