angular.module("cockpitModule").service("cockpitModule_generalServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, cockpitModule_properties,$mdPanel,cockpitModule_widgetServices,$mdToast,$mdDialog,cockpitModule_widgetSelection,cockpitModule_datasetServices,$rootScope,cockpitModule_templateServices){
	var gs=this;
	this.openGeneralConfiguration=function(){
		 var position = $mdPanel.newPanelPosition().absolute().center();

		  var config = {
		    attachTo: angular.element(document.body),
		    controller: cockpitGeneralConfigurationController,
		    disableParentScroll: true,
		    templateUrl: baseScriptPath+ '/directives/cockpit-general-configurator/templates/cockpitGeneralConfiguration.html',
		    hasBackdrop: true,
		    position: position,
		    trapFocus: true,
		    zIndex: 80,
		    fullscreen :true,
		    clickOutsideToClose: false,
		    escapeToClose: false,
		    focusOnOpen: true,
		    onRemoving :function(){
		    	cockpitModule_widgetServices.updateGlobalWidgetStyle();
		    	}
		  };

		  $mdPanel.open(config);
	}
	
	this.openDataConfiguration=function(){
		var position = $mdPanel.newPanelPosition().absolute().center();

		  var config = {
		    attachTo: angular.element(document.body),
		    controller: cockpitDataConfigurationController,
		    disableParentScroll: true,
		    templateUrl: baseScriptPath+ '/directives/cockpit-data-configuration/templates/cockpitDataConfiguration.html',
		    hasBackdrop: true,
		    position: position,
		    trapFocus: true,
		    zIndex: 80,
		    fullscreen :true,
		    clickOutsideToClose: false,
		    escapeToClose: false,
		    focusOnOpen: true,
		    onRemoving :function(){
		    	}
		  };
		  $mdPanel.open(config);
	}
	
	var doSaveCockpit=function(){
		
		
		var dataToSend={};
		dataToSend.action=cockpitModule_properties.DOCUMENT_ID==null ? "DOC_SAVE" : "MODIFY_COCKPIT";
		dataToSend.document={};
		dataToSend.document.name=cockpitModule_properties.DOCUMENT_NAME;
		dataToSend.document.label=cockpitModule_properties.DOCUMENT_LABEL || cockpitModule_properties.DOCUMENT_NAME ;
		dataToSend.document.description=cockpitModule_properties.DOCUMENT_DESCRIPTION;
		dataToSend.document.type="DOCUMENT_COMPOSITE";
		dataToSend.folders=[];
		dataToSend.customData={};
		dataToSend.customData.templateContent=angular.copy(cockpitModule_template);
		
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promisePost("2.0/saveDocument","",dataToSend)
		.then(
				function(response){
					$mdToast.show($mdToast.simple()
					        .textContent(sbiModule_translate.load("sbi.cockpit.saved"))
					        .position("top left")
					        .action('X')
					        .hideDelay(3000));
					cockpitModule_properties.DOCUMENT_ID=response.data.id;
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,"Error*")
				})
	
	
	};
	
	this.saveCockpit=function(){
		//check cockpit label
		if(angular.equals(cockpitModule_properties.DOCUMENT_NAME.trim(),"")){
			var confirm = $mdDialog.prompt()
		      .title(sbiModule_translate.load("sbi.cockpit.saved.name.missing.message"))
		      .textContent(sbiModule_translate.load("sbi.cockpit.saved.name.missing.text"))
		      .placeholder(sbiModule_translate.load("sbi.cockpit.name"))
		      .ariaLabel('cockpit name')
		      .initialValue('')
		      .ok(sbiModule_translate.load("sbi.generic.ok"))
		      .cancel(sbiModule_translate.load("sbi.generic.cancel"));

		    $mdDialog.show(confirm).then(function(result) {
		    	if(result==undefined || angular.equals(result.trim(),"")){
		    		gs.saveCockpit();
		    	}else{
		    		cockpitModule_properties.DOCUMENT_NAME=result;
		    		doSaveCockpit();
		    	}
		    }, function() {
		    });
			
			
			
		}else{
			doSaveCockpit();
		}
	}
	

	this.cleanCache = function(){
		//var listDataset = cockpitModule_datasetServices.getLabelDatasetsUsed();
		var oldDsInCache= angular.copy(cockpitModule_properties.DS_IN_CACHE);
		
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseDelete("1.0/cache",encodeURIComponent(cockpitModule_properties.DS_IN_CACHE.join(","))+"/cleanCache")
		.then(function(response){
			//ok
			var dsNotInCache = cockpitModule_templateServices.getDatasetAssociatedNotUsedByWidget();
			if(dsNotInCache.length>0){
				cockpitModule_datasetServices.addDatasetInCache(dsNotInCache);
			}
			$rootScope.$broadcast('WIDGET_EVENT','UPDATE_FROM_CLEAN_CACHE');

			$mdToast.show($mdToast.simple()
	        .textContent("Cache cleaned")
	        .position("top left")
	        .action('X')
	        .hideDelay(3000));
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,"Error*")
					angular.copy(oldDsInCache,cockpitModule_properties.DS_IN_CACHE);
				});
				
		//reset the variable
		angular.copy([],cockpitModule_properties.DS_IN_CACHE);
	}
});