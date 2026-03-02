angular.module('documentExecutionMasterModule',  [ 'ngMaterial', 'sbiModule','cross_navigation'])
.factory('$documentNavigationScope', function($window, sbiModule_logger) {

	try {

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

	} catch (err) {
		sbiModule_logger.trace(err);
		return {};
	}

})
.controller('docExMasterController',function($scope,sbiModule_translate, $timeout, sourceDocumentExecProperties, sbiModule_urlDeserializator, sbiModule_config, $crossNavigationHelper,$documentNavigationScope,$mdDialog, sbiModule_user){
	$scope.crossNavigationHelper=$crossNavigationHelper;
	$scope.documentNavigationScope=$documentNavigationScope;
//	$scope.sourceDocumentUrl="";
	$scope.executeSourceDocument = function() {
		var menuParams = {};
		var err=false;
		try{
			var menuParameters = sourceDocumentExecProperties.MENU_PARAMETERS.replace(/&$/g, ''); //removes last '&' char
			menuParams = sbiModule_urlDeserializator.deserializeParameters(menuParameters);
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

		var isPublic="";
		if(sbiModule_user.userId == ('public-'+sbiModule_user.tenant)){
			isPublic = "/public";
		}

		var url = sbiModule_config.contextName + isPublic
			+ '/restful-services/publish?PUBLISHER=documentExecutionNg'
			+ '&OBJECT_ID=' + sourceDocumentExecProperties.OBJECT_ID
			+ '&OBJECT_LABEL=' + sourceDocumentExecProperties.OBJECT_LABEL
			+ '&MENU_PARAMETERS=' + encodeURIComponent(JSON.stringify(menuParams)).replace(/'/g,"%27").replace(/"/g,"%22").replace(/%3D/g,"=").replace(/%26/g,"&")
			+ '&LIGHT_NAVIGATOR_DISABLED=TRUE'
			+ '&SBI_EXECUTION_ID=null'
			+ '&OBJECT_NAME=' + sourceDocumentExecProperties.OBJECT_NAME
			+ '&EDIT_MODE=' + sourceDocumentExecProperties.EDIT_MODE
			+ '&TOOLBAR_VISIBLE=' + sourceDocumentExecProperties.TOOLBAR_VISIBLE
			+ '&CAN_RESET_PARAMETERS=' + sourceDocumentExecProperties.CAN_RESET_PARAMETERS

			/**
			 * Getting the starting point of the document execution (JSP page from which we go to the document execution page) from
			 * the 'documentViewer.js'. This information will be forwarded towards the 'documentExecutionNg.jsp', where it will be
			 * used eventually. Originally, used for needs of the execution of the document from the Workspace Organizer, but it can
			 * be used for other starting points, as well.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			+ '&EXEC_FROM=' + sourceDocumentExecProperties.EXEC_FROM;
		if(sourceDocumentExecProperties.SELECTED_ROLE != undefined){
			url = url+ '&SELECTED_ROLE='  + sourceDocumentExecProperties.SELECTED_ROLE

		}

		//add the cockpit parameter inside the cross-Parameter Variable
		if(sourceDocumentExecProperties.COCKPIT_PARAMETER!=undefined){
			url+='&CROSS_PARAMETER='+encodeURIComponent(sourceDocumentExecProperties.COCKPIT_PARAMETER)
			.replace(/'/g,"%27")
			.replace(/"/g,"%22")
			.replace(/%3D/g,"=")
			.replace(/&/g, "&");
		}
		if(sourceDocumentExecProperties.IS_FROM_DOCUMENT_WIDGET && sourceDocumentExecProperties.IS_FROM_DOCUMENT_WIDGET!='null'){
			url+="&IS_FROM_DOCUMENT_WIDGET="+sourceDocumentExecProperties.IS_FROM_DOCUMENT_WIDGET;
		}

		var laodSourceDocToCross=function(){
			$timeout(function(){
				if($crossNavigationHelper.crossNavigationSteps.stepControl.insertBread){
					$crossNavigationHelper.crossNavigationSteps.stepControl.insertBread({name:sourceDocumentExecProperties.OBJECT_NAME,label:sourceDocumentExecProperties.OBJECT_LABEL,id:sourceDocumentExecProperties.OBJECT_ID,url:url});
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
		if(window.frameElement && window.frameElement.parentElement){
			var docViewScope=angular.element(window.frameElement.parentElement).scope();
			if(docViewScope!=undefined && docViewScope.closeDocument!=undefined){
				visible=true
			}
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
});