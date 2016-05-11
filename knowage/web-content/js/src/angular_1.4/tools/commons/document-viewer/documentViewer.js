angular.module('document_viewer', [ 'ngMaterial' ,'sbiModule'])
.service('$documentViewer',function($mdDialog,sbiModule_config){ 
	this.openDocument=function(documentId,documentLabel,documentName){

		$mdDialog.show({
			controller: openDocumentController,
			template: '<md-dialog aria-label="Open document"  style="width: 100%;  height: 100%;max-width: 100%;  max-height: 100%;" ng-cloak>'+
						'<md-dialog-content flex layout="column" class="dialogFrameContent" >'+
							'<iframe flex class=" noBorder" ng-src="{{documentViewerUrl}}" name="documentViewerIframe"></iframe>'+ 
						'</md-dialog-content> '+
					'</md-dialog>',  
//			clickOutsideToClose:false,
//			escapeToClose :false,
			fullscreen: true,
			locals:{documentId:documentId,documentLabel:documentLabel,documentName:documentName}
			
		}) .then(function() { 
			
	    } );
	
	};
	
	function openDocumentController($scope,sbiModule_config,documentId,documentLabel,documentName){
		var pathUrl="";
		pathUrl+="&OBJECT_ID="+documentId;
		pathUrl+="&OBJECT_LABEL="+documentLabel;
		pathUrl+="&OBJECT_NAME="+documentName;
		
		$scope.documentViewerUrl=sbiModule_config.adapterPath+'?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&SBI_EXECUTION_ID=null'+pathUrl
		
		$scope.closeDocument=function(){
			$mdDialog.hide();
		}

	}
});