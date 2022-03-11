angular.module("cockpitModule").service("cockpitModule_generalServices",function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, cockpitModule_properties,$mdPanel,cockpitModule_widgetServices,$mdToast,$mdDialog,cockpitModule_widgetSelection,cockpitModule_datasetServices,$rootScope,cockpitModule_templateServices, $location, kn_regex, sbiModule_config){
			var gs=this;
	var savingDataConf = false;

	this.savingDataConfiguration = function (isSaving){
		savingDataConf = isSaving
	}
	this.isSavingDataConfiguration = function (){
		return savingDataConf
	}

	var nearRealTime = false;
	this.setNearRealTime = function (nearRT){
		nearRealTime = nearRT
	}
	this.isNearRealTime = function (){
		return nearRealTime
	}
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
			dataToSend.document.label=cockpitModule_properties.DOCUMENT_LABEL;
			dataToSend.document.description=cockpitModule_properties.DOCUMENT_DESCRIPTION;
			dataToSend.document.type="DOCUMENT_COMPOSITE";
			dataToSend.folders=[];
			if(cockpitModule_properties.FOLDER_ID != "") {
				dataToSend.folders.push(cockpitModule_properties.FOLDER_ID);
			}
			dataToSend.customData={};
			dataToSend.customData.templateContent=angular.copy(cockpitModule_template);
			dataToSend.customData.templateContent.knowageVersion = cockpitModule_properties.CURRENT_KNOWAGE_VERSION;

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
							if(widget.content && widget.content.copyColumnSelectedOfDataset){
							    delete widget.content.copyColumnSelectedOfDataset;
							}
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
						window.parent.postMessage(cockpitModule_properties);
						if(window.parent.document.getElementById('_KNOWAGE_VUE')){
							window.parent.postMessage({"type":"saveCockpit","model":cockpitModule_properties}, '*');
						}
					},
					function(response){
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.error"));
						if(response.config && response.config.data && response.config.data.action == "DOC_SAVE"){
							cockpitModule_properties.DOCUMENT_NAME = "";
						}
					})


		};

	this.saveCockpit=function(event){
			//check cockpit label
			if(angular.equals(cockpitModule_properties.DOCUMENT_NAME.trim(),"")){
		    $mdDialog.show(
	    		{
	    			controller: saveCockpitController,
	    			templateUrl: baseScriptPath + '/directives/cockpit-toolbar/templates/saveCockpit.tmpl.html',
	    			parent: angular.element(document.body),
	    			targetEvent: event,
	    			clickOutsideToClose: false
	    		}
		    ).then(function(result){
	    		cockpitModule_properties.DOCUMENT_LABEL = result.label;
	    		cockpitModule_properties.DOCUMENT_NAME = result.name;
	    		doSaveCockpit();
			    }, function() {

		    });

		} else {
				doSaveCockpit();
			}
		}


		this.cleanCache = function(){
			var requestBody = {};
			var datasets = cockpitModule_template.configuration.datasets;
			for (i = 0; i < datasets.length; i++) {
				var dataset = datasets[i];
			var parameters = cockpitModule_datasetServices.getDatasetParameters(dataset.dsId);
    		requestBody[dataset.dsLabel] = cockpitModule_datasetServices.getParametersAsString(parameters);;
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
			if(!window.parent.angular) return false;
			return (window.parent.angular.element(window.frameElement).scope()!=undefined && window.parent.angular.element(window.frameElement).scope().closeConfirm!=undefined);
		}

	//get templates location
	gs.getTemplateUrl = function(widget,template,format){
		var templatesUrl = sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/directives/cockpit-widget/widget/'+widget+'/templates/';
		return window.location.origin + templatesUrl + template + (format || '.html');
	}

	//get tools location
	gs.getToolsUrl = function(){
		return sbiModule_config.dynamicResourcesEnginePath + '/angular_1.4/cockpit/tools/commons/';
	}

	function saveCockpitController($scope, $mdDialog, sbiModule_translate, kn_regex){
		$scope.translate = sbiModule_translate;
		$scope.regex = kn_regex;
		$scope.cockpit = {
			label: '',
			name: ''
		};

		$scope.cancel = function(){
			$mdDialog.cancel();
		}

		$scope.save = function(result){
			$mdDialog.hide(result);
		}

	}
	gs.numericalColumn = ['java.lang.Double','java.lang.Float','java.math.BigInteger','java.math.BigDecimal','java.lang.Long','java.lang.Integer'];
	gs.isNumericColumn = function(column){
		if(gs.numericalColumn.indexOf(column.type) != -1) return true;
		else if(column.type === "java.lang.String" && column.fieldType === "MEASURE" && ["COUNT","COUNT_DISTINCT"].indexOf(column.aggregationSelected) != -1) return true
	    return false;
	}
});