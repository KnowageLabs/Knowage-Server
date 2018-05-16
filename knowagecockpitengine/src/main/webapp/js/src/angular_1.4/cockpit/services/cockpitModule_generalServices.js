angular.module("cockpitModule").service("cockpitModule_generalServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, cockpitModule_properties,$mdPanel,cockpitModule_widgetServices,$mdToast,$mdDialog,cockpitModule_widgetSelection,cockpitModule_datasetServices,$rootScope,cockpitModule_templateServices, $location){
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
			
			// reset table widgets volatile data
			if(dataToSend.customData.templateContent.sheets){
				angular.forEach(dataToSend.customData.templateContent.sheets,function(sheet){
					if(sheet.widgets){
						angular.forEach(sheet.widgets,function(widget){
							if(widget.type == "table"){
								if(widget.settings){
									delete widget.settings.backendTotalRows;
									delete widget.settings.page;
									delete widget.settings.rowsCount;
									
									if(widget.settings.summary){
										delete widget.settings.summary.forceDisabled;
										delete widget.settings.summary.row;
									}
									
									if(widget.settings){
										if(widget.style.tr){
											delete widget.style.tr["background-color"];
										}
									}
								}
								widget.search = undefined;
								if(widget.dataset){
									widget.dataset.isRealtime = undefined;
								}
						} else if(widget.type == "selector"){
							delete widget.activeValues;
							}
						});
					}
				});
			}
			
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
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.error"));
						if(response.config && response.config.data && response.config.data.action == "DOC_SAVE"){
							cockpitModule_properties.DOCUMENT_NAME = "";
						}
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
			var requestBody = {};
			var datasets = cockpitModule_template.configuration.datasets;
			for (i = 0; i < datasets.length; i++) {
				var dataset = datasets[i];
	    		requestBody[dataset.dsLabel] = dataset.parameters;
			}
			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.promisePost("1.0/cache","clean-datasets", angular.toJson(requestBody))
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
					});
					
			//reset the variable
			angular.copy([],cockpitModule_properties.DS_IN_CACHE);
		}
		
		this.closeNewCockpit=function(){
			window.parent.angular.element(window.frameElement).scope().closeConfirm(true,true);
		}
		this.isFromNewCockpit=function(){
			return (window.parent.angular.element(window.frameElement).scope()!=undefined && window.parent.angular.element(window.frameElement).scope().closeConfirm!=undefined);
		}
	
	//get templates location
	gs.getTemplateUrl = function(widget,template){
		var basePath = $location.$$absUrl.substring(0,$location.$$absUrl.indexOf('api/'));
		var templatesUrl = 'js/src/angular_1.4/cockpit/directives/cockpit-widget/widget/'+widget+'/templates/';
  		return basePath + templatesUrl + template +'.html';
  	}
});