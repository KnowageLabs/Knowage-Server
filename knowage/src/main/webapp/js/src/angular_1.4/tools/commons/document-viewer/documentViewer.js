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
	 * @param executedFrom The parameter that will indicate the page that we are coming from to the document execution page. Originally, this parameter
	 * was introduced for the need of the Workspace Organizer option, where user can execute a document. If so, the "Add to my workspace" option should
	 * not appear in the menu of the executed document (since this option adds label to the document into the Organizer and we execute it form there).
	 * NOTE: This parameter can be used also for other pages and other needs - you can send a string value that will indicate the starting point of the
	 * document execution (the previous page).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	this.openDocument=function(documentId,documentLabel,documentName,localScope,executedFrom){
		this.executeDocument(documentId,documentLabel,documentName,localScope,false,executedFrom);
	};

	this.editDocument=function(documentId,documentLabel,documentName,localScope,editMode){
		this.executeDocument(documentId,documentLabel,documentName,localScope,editMode);

	};

	/**
	 * The 'localScope' and 'executedFrom' values (input parameters) will be forwarded to the controller. The second one will be forwarded to
	 * the 'documentExecutionNg.jsp' page, where it will be originally be used for determining whether the "Add to my workspace" from the menu
	 * of the executed document should be removed.
	 * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	this.executeDocument = function (documentId,documentLabel,documentName,localScope, editMode,executedFrom) {
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
			locals:{documentId:documentId,documentLabel:documentLabel,documentName:documentName,localScope:localScope, editMode:editMode,executedFrom:executedFrom}

		}) .then(function() {

	    } );
	}

	function openDocumentController($scope,sbiModule_config,documentId,documentLabel,documentName,localScope,editMode,executedFrom){
		var pathUrl="";
		pathUrl+="&OBJECT_ID="+documentId;
		pathUrl+="&OBJECT_LABEL="+documentLabel;
		pathUrl+="&OBJECT_NAME="+documentName;

		/**
		 * Indicator for the starting point of the document execution, i.e. the page (option, surrounding) from which we come to the
		 * document execution page (originally, this input parameter is used for needs of the Workspace Organizer - WORKSPACE_ORGANIZER).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		pathUrl += "&EXEC_FROM=" + executedFrom;

		$scope.documentViewerUrl = sbiModule_config.adapterPath+'?ACTION_NAME=EXECUTE_DOCUMENT_ANGULAR_ACTION&SBI_ENVIRONMENT=DOCBROWSER&IS_SOURCE_DOCUMENT=true&SBI_EXECUTION_ID=null'+pathUrl
		if(editMode) {
			$scope.documentViewerUrl += "&EDIT_MODE="+editMode
		}

		//do not add this request into the stack
		$scope.documentViewerUrl += "&"+"LIGHT_NAVIGATOR_DISABLED=true";

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