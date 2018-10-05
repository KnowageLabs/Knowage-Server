angular.module('document_viewer', [ 'ngMaterial' ,'sbiModule'])
.service('$documentViewer',function($mdDialog,sbiModule_config,sbiModule_restServices){ 
	 
	var dwS=this;
	this.openDocumentByLabel=function(docLabel,localScope){
		sbiModule_restServices.promiseGet("1.0/documents",docLabel).then(
		function(response){
			dwS.openDocument(response.data.id,response.data.label,response.data.name,localScope);
		},
		function(response){
			sbiModule_restServices.errorHandler(response.data,"");
		});
	}

	this.editDocumentByLabel=function(docLabel,localScope,editMode){
		sbiModule_restServices.promiseGet("1.0/documents",docLabel).then(
		function(response){
			dwS.editDocument(response.data.id,response.data.label,response.data.name,localScope,editMode?editMode:"edit");
		},
		function(response){
			sbiModule_restServices.errorHandler(response.data,"");
		});
	}
	
	/**
	 * @param localScope The parameter added for the implementation of the Workspace, but can be used in any other interface (catalog, implementation).
	 * It represents the scope of that interface (e.g. the scope of the Workspace) and it is used for firing an event (Angular broadcast) towards that 
	 * interface, so it can be informed that the document that is executed from it is e.g. closed (user clicked on the X sign for closing the iframe that 
	 * contained executed document). This scope object can be used whenever and wherever we need it in this controller, not only when the executed 
	 * document is closed.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	this.openDocument=function(documentId,documentLabel,documentName,localScope){
		this.executeDocument(documentId,documentLabel,documentName,localScope,false);
	};
	
	this.editDocument=function(documentId,documentLabel,documentName,localScope,editMode){
		this.executeDocument(documentId,documentLabel,documentName,localScope,editMode);
		
	};
	
	this.executeDocument = function (documentId,documentLabel,documentName,localScope, editMode) {
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
			locals:{documentId:documentId,documentLabel:documentLabel,documentName:documentName,localScope:localScope, editMode:editMode}
			
		}) .then(function() { 
			
	    } );
	}
	
	function openDocumentController($scope,sbiModule_config,documentId,documentLabel,documentName,localScope,editMode){
		var pathUrl="";
		pathUrl+="&OBJECT_ID="+documentId;
		pathUrl+="&OBJECT_LABEL="+documentLabel;
		pathUrl+="&OBJECT_NAME="+documentName;

		$scope.documentViewerUrl = sbiModule_config.adapterPath+'?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&SBI_EXECUTION_ID=null'+pathUrl
		if(editMode) {
			$scope.documentViewerUrl += "&EDIT_MODE="+editMode
		}
		
		$scope.closeDocument=function(){
			
			/**
			 * If the 'openDocument' function is called without the fourth parameter, 'localScope' (the scope of the interface that executed a document), this
			 * input parameter will not be defined and we will just skip broadcasting (firing an event). Otherwise, we will fire an event that will inform the
			 * interface from which we called the document execution that the document is closed (in this case). 
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (localScope) {				
				localScope.$broadcast("documentClosed");
			}
			
			$mdDialog.hide();
		}

	}
});