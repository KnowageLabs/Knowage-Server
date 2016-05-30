angular.module('documentExecutionMasterModule',  [ 'ngMaterial', 'sbiModule','cross_navigation'])
.factory('$documentNavigationScope', function($window) {
	var angularTmp = null;
	var isFromCockpit = false;
	
	if($window.parent.angular) {
		angularTmp = $window.parent.angular;
	} else if ($window.parent.parent.angular){
		angularTmp = $window.parent.parent.angular;
		isFromCockpit = true;
	}
	
	var docNavFrameScope = null;
	if(isFromCockpit) {
		docNavFrameScope = angularTmp.element($window.parent.frameElement).scope();
	} else{
		docNavFrameScope = angularTmp.element($window.frameElement).scope();
	}
	
    return (docNavFrameScope == undefined || docNavFrameScope == null) ? 
    		{} : docNavFrameScope.$parent;
})
.controller('docExMasterController',['$scope','sbiModule_translate','$timeout','sourceDocumentExecProperties','sbiModule_config','$crossNavigationHelper','$documentNavigationScope','$mdDialog',docExMasterControllerFunction]);

function docExMasterControllerFunction($scope,sbiModule_translate,$timeout,sourceDocumentExecProperties,sbiModule_config,$crossNavigationHelper,$documentNavigationScope,$mdDialog){
	$scope.crossNavigationHelper=$crossNavigationHelper;
	$scope.documentNavigationScope=$documentNavigationScope;
//	$scope.sourceDocumentUrl="";
$scope.executeSourceDocument = function() { 
		var menuParams = {};
		var err=false;
		try{
			var menuParameters = sourceDocumentExecProperties.MENU_PARAMETERS.replace(/&$/g, ''); //removes last '&' char
			
			var splittedMenuParams = menuParameters=='null'? [] : menuParameters.split("&");
			
			for(var i=0;i<splittedMenuParams.length;i++){
				var splittedItem=splittedMenuParams[i].split("=");
				if(splittedItem[1]==undefined){err=true;}
				menuParams[splittedItem[0]]=splittedItem[1];
			}
		}catch(e){
			err=true
			console.error(e);
		}finally{
			if(err){ 
			 $mdDialog.show(
				      $mdDialog.alert()
				        .clickOutsideToClose(true)
				        .title(sbiModule_translate.load("sbi.execution.menu.parameter.title"))
				        .content(sbiModule_translate.load("sbi.execution.menu.parameter.messages"))
				        .ariaLabel('Alert Dialog Demo')
				        .ok(sbiModule_translate.load("sbi.general.continue"))
				    );
			}
		}
		
		var url = sbiModule_config.contextName 
			+ '/restful-services/publish?PUBLISHER=/WEB-INF/jsp/tools/documentexecution/documentExecutionNg.jsp'
			+ '&OBJECT_ID=' + sourceDocumentExecProperties.OBJECT_ID
			+ '&OBJECT_LABEL=' + sourceDocumentExecProperties.OBJECT_LABEL
			+ '&MENU_PARAMETERS=' + encodeURIComponent(JSON.stringify(menuParams)).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&")
			+ '&LIGHT_NAVIGATOR_DISABLED=TRUE'
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + sourceDocumentExecProperties.OBJECT_NAME;
		
		var laodSourceDocToCross=function(){
			$timeout(function(){
				if($crossNavigationHelper.crossNavigationSteps.stepControl.insertBread){
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
	
	
	$scope.isCloseDocumentButtonVisible=function(){
		var visible=false;
		//if document is open from dodument browser
		if($scope.documentNavigationScope.closeDocument!=undefined){
			visible=true;
		}
		
		//if document is open from documentViewer directive
		var docViewScope=window.parent.angular.element(window.frameElement).scope();
		 if(docViewScope!=undefined && docViewScope.closeDocument!=undefined){
			 visible=true
		 } 
		return visible;
	}
	
	function closeDoc(id){
		if($scope.documentNavigationScope.closeDocument!=undefined){
			$documentNavigationScope.closeDocument(id);
		}else if(window.parent.angular.element(window.frameElement).scope()!=undefined && window.parent.angular.element(window.frameElement).scope().closeDocument!=undefined){
			//close dialog of documentViewer
			window.parent.angular.element(window.frameElement).scope().closeDocument()
		}else{
			sbiModule_restServices.errorHandler("","Unable to close document")
		}
	}
	
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
//				    $crossNavigationHelper;
				   closeDoc($crossNavigationHelper.crossNavigationSteps.stepItem[0].id)
				   $documentNavigationScope.closeDocument($crossNavigationHelper.crossNavigationSteps.stepItem[0].id);
			   } );
		}else{
			closeDoc(docId);
		}
		
		 
	}
	
	$scope.changeNavigationRole=function(newRole){
		$scope.crossNavigationHelper.changeNavigationRole(newRole);
	}
	
	$scope.isNavigationInProgress=function(){
		return $crossNavigationHelper.crossNavigationSteps.stepItem.length>1;
	}
}
