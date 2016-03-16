(function() {
	angular.module('documentExecutionModule')
	.service('infoMetadataService', function(execProperties, sbiModule_translate, $mdDialog, documentExecuteServices, sbiModule_restServices, sbiModule_config) {
		/* static variables*/
		var lblTitle = sbiModule_translate.load('sbi.execution.executionpage.toolbar.metadata');
		var lblCancel = sbiModule_translate.load('sbi.general.cancel');
		var lblClose = sbiModule_translate.load('sbi.general.close');
		var lblSave = sbiModule_translate.load('sbi.generic.update');
		var lblGeneralMeta = sbiModule_translate.load('sbi.execution.metadata.generalmetadata');
		var lblShortMeta = sbiModule_translate.load('sbi.execution.metadata.shorttextmetadata');
		var lblLongMeta = sbiModule_translate.load('sbi.execution.metadata.longtextmetadata');
		
		console.log('infoMetadataService inizio');
		
		return {
		openInfoMetadata : function(){
		    $mdDialog.show({
				preserveScope : true,
		    	templateUrl: sbiModule_config.contextName + '/js/src/angular_1.4/tools/documentexecution/templates/documentMetadata.jsp',
		    	locals : {
					sbiModule_translate: sbiModule_translate,
					sbiModule_config: sbiModule_config,
					executionInstance: execProperties.executionInstance
				},
		    	parent: angular.element(document.body),
		    	clickOutsideToClose:false,
		    	controllerAs: "metadataDlgCtrl",
		    	controller : function($mdDialog, sbiModule_translate, sbiModule_config, executionInstance) {
		    		var metadataDlgCtrl = this;
		    		metadataDlgCtrl.lblTitle = lblTitle;
		    		metadataDlgCtrl.lblCancel = lblCancel;
		    		metadataDlgCtrl.lblClose = lblClose;
		    		metadataDlgCtrl.lblSave = lblSave;
		    		metadataDlgCtrl.lblGeneralMeta = lblGeneralMeta;
		    		metadataDlgCtrl.lblShortMeta = lblShortMeta;
		    		metadataDlgCtrl.lblLongMeta = lblLongMeta;
		    	
		    		metadataDlgCtrl.generalMetadata = [];
		    		metadataDlgCtrl.shortText = [];
		    		metadataDlgCtrl.longText = [];
		    		var params = null;
		    		if(executionInstance.SUBOBJECT_ID){
		    			params = {subobjectId: executionInstance.SUBOBJECT_ID};
		    		}
		    		sbiModule_restServices.promiseGet('1.0/documentexecution/' + executionInstance.OBJECT_ID, 'documentMetadata', params)
		    		.then(function(response){
		    			metadataDlgCtrl.generalMetadata = response.data.GENERAL_META;
		    			metadataDlgCtrl.shortText = response.data.SHORT_TEXT;
			    		metadataDlgCtrl.longText = response.data.LONG_TEXT;
		    		},function(response){
		    			documentExecuteServices.showToast(response.data.errors[0].message, 5000);
		    		});
		    		metadataDlgCtrl.close = function(){
		    			$mdDialog.hide();
		    		}
		    		metadataDlgCtrl.save = function(){
		    			var saveObj = {
		    				id: executionInstance.OBJECT_ID,
		    				subobjectId: executionInstance.SUBOBJECT_ID, 
		    				jsonMeta: metadataDlgCtrl.shortText.concat(metadataDlgCtrl.longText)
		    			};
		    			sbiModule_restServices.promisePost('1.0/documentexecution', 'saveDocumentMetadata', saveObj)
		    			.then(function(response){
		    				//documentExecuteServices.showToast(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"), 3000);
		    				documentExecuteServices.showToast("Salvataggio OK", 1000);
		    			},function(response){
		    				documentExecuteServices.showToast(response.data.errors[0].message, 5000);
		    			});
		    		}
		    	}
		    })
	        .then(function(answer) {
	        	
	        }, function() {
	        	
	        });
		}}
	});
})();