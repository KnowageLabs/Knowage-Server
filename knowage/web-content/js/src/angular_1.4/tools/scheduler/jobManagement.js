var app = angular.module('jobManager', [ 'ngMaterial',
                                         'angularUtils.directives.dirPagination', 'ng-context-menu',
                                         'angular_list', 'angular_table' ,'sbiModule', 'angular-list-detail','document_tree',
                                         'angular_time_picker', 'ngMessages', 'ngSanitize']);

var EmptyJob = {
	NEWJOB: true,
	jobName: "",
	jobGroup: "",
	jobDescription: "",
	jobClass: "",
	jobDurability: false,
	jobRequestRecovery: false,
	useVolatility: false,
	jobParameters: [],
	documents: [],
	triggers: []
}

app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "sbiModule_logger", "$scope", "$mdDialog", "$mdToast", "$timeout", "$location", mainFunction ]);

function mainFunction(sbiModule_download, sbiModule_translate, sbiModule_restServices, sbiModule_logger, $scope, $mdDialog, $mdToast, $timeout, $location) {
	var ctrl = this;
	sbiModule_translate.addMessageFile("component_scheduler_messages");
	
	var jobNameFromUrl = $location.search().JOB_NAME;
	
	//variables
	ctrl.isOverviewTabActive = true;
	ctrl.emptyJob = JSON.parse(JSON.stringify(EmptyJob));
	ctrl.showDetail = false;
	ctrl.isRequired = true;
	ctrl.object_temp = {};
	ctrl.forms = {};
	
	ctrl.triggerStrategies = [
   		{value : 'fixed', label : sbiModule_translate.load("scheduler.fixedValuesStrategy", "component_scheduler_messages")},
   		{value : 'loadAtRuntime', label : sbiModule_translate.load("scheduler.loadAtRuntimeStrategy", "component_scheduler_messages")},
   		{value : 'formula', label : sbiModule_translate.load("scheduler.useFormulaStrategy", "component_scheduler_messages")}];
	
	ctrl.triggerStrategiesNoFormula = [
  		{value : 'fixed', label : sbiModule_translate.load("scheduler.fixedValuesStrategy", "component_scheduler_messages")},
   		{value : 'loadAtRuntime', label : sbiModule_translate.load("scheduler.loadAtRuntimeStrategy", "component_scheduler_messages")}];
	
	ctrl.triggerIterations = [
		{value : "true", label : sbiModule_translate.load("scheduler.iterateOnParameterValues", "component_scheduler_messages")},
		{value : "false", label : sbiModule_translate.load("scheduler.doNotIterateOnParameterValues", "component_scheduler_messages")}];
	
	ctrl.formulas = [];
	ctrl.loadFormulas = function(){
		sbiModule_restServices.get("2.0/formulas", '')
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("unable to get formulas");
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					ctrl.formulas = data;
				
					for(var i = 0; i < ctrl.formulas.length; i++){
						var formulaDescription = ctrl.formulas[i].description;
						if (formulaDescription.startsWith("#")) {
							formulaDescription = sbiModule_translate.load(formulaDescription.substring(1), "component_scheduler_messages");
							ctrl.formulas[i].description = formulaDescription;
						}
					}
				}
			})
			.error(function(data, status, headers, config) {
				console.log("unable to get formulas " + status);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
			});
	}
	ctrl.loadFormulas();

	ctrl.loadJobs = function(selectedJobName){
		sbiModule_restServices.get("scheduler/listAllJobs", '')
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("unable to get jobs");
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					ctrl.jobList = data.root;
					
					for(var jobIndex = ctrl.jobList.length - 1; jobIndex >= 0; jobIndex--){
						var job = ctrl.jobList[jobIndex];
						
						if(job.jobGroup != "BIObjectExecutions"){
							// discard job if group is not BIObjectExecutions
							ctrl.jobList.splice(jobIndex, 1);
							continue;
						}else{
							// calc new date strings
							for(var triggerIndex=0; triggerIndex<job.triggers.length; triggerIndex++){
								var trigger = job.triggers[triggerIndex];
								trigger.triggerStartDateTime = trigger.triggerStartDate + " " + trigger.triggerStartTime;
								trigger.triggerEndDateTime = trigger.triggerEndDate + " " + trigger.triggerEndTime;
								trigger.triggerIsPausedString = (trigger.triggerIsPaused ? sbiModule_translate.load("sbi.general.yes") : sbiModule_translate.load("sbi.general.No"));
							}
							// update parameters
							for(var docIndex=0; docIndex<job.documents.length; docIndex++){
								var document = job.documents[docIndex];
								for(var paramIndex=0; paramIndex < document.parameters.length; paramIndex++){
									var parameter = document.parameters[paramIndex];
									if(parameter.value.trim() != ""){
										parameter.values = parameter.value.split(";");
										parameter.selectedValues = parameter.value.split(";");
									}else{
										parameter.values = [];
										parameter.selectedValues = [];
									}
								}
							}
						}
					}
					ctrl.selectJobByName(selectedJobName);
				}
			})
			.error(function(data, status, headers, config) {
				console.log("unable to get jobs " + status);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
			})
	}
	ctrl.loadJobs(jobNameFromUrl);
	
	ctrl.addJob = function(){
		ctrl.selectedJob = angular.copy(ctrl.emptyJob);
		ctrl.showDetail = true;
	}
	
	ctrl.reloadJob = function(){
		var jobName = ctrl.selectedJob.jobName;
		ctrl.loadJobs(jobName);
	}
	
	ctrl.selectJobByName = function(jobName){
		if(jobName != null && jobName != undefined && jobName != ""){
			for(var i=0; i<ctrl.jobList.length; i++){
				var job = ctrl.jobList[i];
				if(job.jobName == jobName){
					ctrl.loadJob(job);
				}
			}
		}
	}

	ctrl.loadJob = function(item){
		ctrl.showDetail = true;
		ctrl.object_temp = angular.copy(item);
		
		if(item != null){		
			//ctrl.selectedJob = angular.copy(item);
			ctrl.selectedJob = item;
		} else {
			if(ctrl.selectedJob != null ){
				ctrl.closeDetail();
			} else if(ctrl.selectedJob == null ) {
				ctrl.selectedJob = ctrl.emptyJob;
			}
		}
		
		// select tab
		if(ctrl.selectedJob.NEWJOB && ctrl.selectedJob.NEWJOB == true){
			ctrl.selectDetailTab();
		}
		
		// select first document
		var firstDocumentIndex = (ctrl.selectedJob.documents.length > 0 ? 0 : -1);
		ctrl.selectDocument(firstDocumentIndex);
	}
	
	ctrl.closeDetail = function(){
		ctrl.showDetail = false;
		ctrl.loadJobs();
	}
	
	ctrl.saveJob = function(){
		sbiModule_restServices.post("scheduler", "saveJob", ctrl.selectedJob)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				console.log("unable to save job ", data.errors);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.error.save") + " " + data.errors);
			} else {
				ctrl.showToastOk(sbiModule_translate.load("sbi.glossary.success.save"));
				ctrl.closeDetail();
			}
		})
		.error(function(data, status, headers, config) {
			console.log("unable to save job " + status);
			ctrl.showToastError(sbiModule_translate.load("sbi.glossary.error.save"));
		})
	}

	ctrl.menuJob = [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash',	 
		action : function(item,event){
			ctrl.selectedJob = item;
			var confirm = $mdDialog.confirm()
				.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
				.content(sbiModule_translate.load("sbi.layer.modify.progress.message.modify"))
				.ariaLabel('Delete job')
				.ok(sbiModule_translate.load("sbi.general.continue"))
				.cancel(sbiModule_translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm)
				.then(
					function(){ctrl.deleteJob();},
					function(){console.log('Job deleted');}
				);
		}
	}];
	
	ctrl.menuTrigger = [{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.info'),
			icon: 'fa fa-info',
			action: function(item,event){
				ctrl.selectedTrigger = item;
				ctrl.showTriggerInfo();
				console.log('Schedulation info');
			}
		},{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.detail'),
			icon: 'fa fa-pencil',
			action: function(item,event){
				ctrl.selectedTrigger = item;
				ctrl.editTrigger(ctrl.selectedJob.jobName, ctrl.selectedJob.jobGroup, item.triggerName, item.triggerGroup);
				console.log('Schedulation detail');
			}
		},{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.execute'),
			icon: 'fa fa-play',
			action: function(item,event){
				ctrl.selectedTrigger = item;
				var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
					.content(sbiModule_translate.load("sbi.scheduler.schedulation.execute"))
					.ariaLabel('Schedulation execution')
					.ok(sbiModule_translate.load("sbi.general.continue"))
					.cancel(sbiModule_translate.load("sbi.general.cancel"));
				$mdDialog.show(confirm)
					.then(
						function(){ctrl.triggerExecute();},
						function(){console.log('Schedulation execution');}
					);
			}
		},{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.pause'),
			icon: 'fa fa-lock',
			visible: function(item,event){
				return item !== undefined && item.triggerIsPaused == false;
			},
			action: function(item,event){
				ctrl.selectedTrigger = item;
				var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
					.content(sbiModule_translate.load("sbi.scheduler.schedulation.pause"))
					.ariaLabel('Schedulation pause')
					.ok(sbiModule_translate.load("sbi.general.continue"))
					.cancel(sbiModule_translate.load("sbi.general.cancel"));
				$mdDialog.show(confirm)
					.then(
						function(){ctrl.triggerPause();},
						function(){console.log('Schedulation pause');}
					);
			}
		},{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.resume'),
			icon: 'fa fa-unlock',
			visible: function(item,event){
				return item !== undefined && item.triggerIsPaused == true;
			},
			action: function(item,event){
				ctrl.selectedTrigger = item;
				var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
					.content(sbiModule_translate.load("sbi.scheduler.schedulation.resume"))
					.ariaLabel('Schedulation resume')
					.ok(sbiModule_translate.load("sbi.general.continue"))
					.cancel(sbiModule_translate.load("sbi.general.cancel"));
				$mdDialog.show(confirm)
					.then(
						function(){ctrl.triggerResume();},
						function(){console.log('Schedulation resume');}
					);
			}
		},{
			label: sbiModule_translate.load('sbi.scheduler.schedulation.delete'),
			icon: 'fa fa-trash',
			action: function(item,event){
				ctrl.selectedTrigger = item;
				var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
					.content(sbiModule_translate.load("sbi.scheduler.schedulation.delete"))
					.ariaLabel('Schedulation deletion')
					.ok(sbiModule_translate.load("sbi.general.continue"))
					.cancel(sbiModule_translate.load("sbi.general.cancel"));
				$mdDialog.show(confirm)
					.then(
						function(){ctrl.triggerDelete();},
						function(){console.log('Schedulation deletion');}
					);
			}
		}];
	
	ctrl.triggerPause = function(){
		var requestString =
			"pauseTrigger?jobName="+ctrl.selectedJob.jobName
			+"&jobGroup="+ctrl.selectedJob.jobGroup
			+"&triggerName="+ctrl.selectedTrigger.triggerName
			+"&triggerGroup="+ctrl.selectedTrigger.triggerGroup;
		sbiModule_restServices.post("scheduler", requestString)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_logger.log("unable to pause schedulation");
			} else {
				ctrl.selectedTrigger.triggerIsPaused = true;
				ctrl.selectedTrigger.triggerIsPausedString = sbiModule_translate.load("sbi.general.yes");
				$mdDialog.show( 
					$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(sbiModule_translate.load("sbi.generic.ok"))
			        .content(sbiModule_translate.load("sbi.scheduler.schedulation.paused"))
			        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
			}
		})
		.error(function(data, status, headers, config) {
			sbiModule_logger.log("unable to pause schedulation " + status);
		});
	}
	
	ctrl.triggerResume = function(){
		var requestString =
			"resumeTrigger?jobName="+ctrl.selectedJob.jobName
			+"&jobGroup="+ctrl.selectedJob.jobGroup
			+"&triggerName="+ctrl.selectedTrigger.triggerName
			+"&triggerGroup="+ctrl.selectedTrigger.triggerGroup;
		sbiModule_restServices.post("scheduler", requestString)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_logger.log("unable to resume schedulation");
			} else {
				ctrl.selectedTrigger.triggerIsPaused = false;
				ctrl.selectedTrigger.triggerIsPausedString = sbiModule_translate.load("sbi.general.No");
				$mdDialog.show( 
					$mdDialog.alert()
			        .parent(angular.element(document.body))
			        .clickOutsideToClose(false)
			        .title(sbiModule_translate.load("sbi.generic.ok"))
			        .content(sbiModule_translate.load("sbi.scheduler.schedulation.resumed"))
			        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
			}
		})
		.error(function(data, status, headers, config) {
			sbiModule_logger.log("unable to resume schedulation " + status);
		});
	}
	
	ctrl.triggerExecute = function(){
		var requestString =
			"executeTrigger?jobName="+ctrl.selectedJob.jobName
			+"&jobGroup="+ctrl.selectedJob.jobGroup
			+"&triggerName="+ctrl.selectedTrigger.triggerName
			+"&triggerGroup="+ctrl.selectedTrigger.triggerGroup;
		sbiModule_restServices.post("scheduler", requestString)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_logger.log("unable to execute schedulation");
			} else {
				$mdDialog.show( 
					$mdDialog.alert()
				        .parent(angular.element(document.body))
				        .clickOutsideToClose(false)
				        .title(sbiModule_translate.load("sbi.generic.ok"))
				        .content(sbiModule_translate.load("sbi.scheduler.schedulation.executed"))
				        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
			}
		})
		.error(function(data, status, headers, config) {
			sbiModule_logger.log("unable to execute schedulation " + status);
		});
	}
	
	ctrl.triggerDelete = function(){
		var requestString =
			"deleteTrigger?jobName="+ctrl.selectedJob.jobName
			+"&jobGroup="+ctrl.selectedJob.jobGroup
			+"&triggerName="+ctrl.selectedTrigger.triggerName
			+"&triggerGroup="+ctrl.selectedTrigger.triggerGroup;
		sbiModule_restServices.post("scheduler", requestString)
		.success(function(data, status, headers, config) {
			if (data.hasOwnProperty("errors")) {
				sbiModule_logger.log("unable to delete schedulation");
			} else {
				for(var i=0; i < ctrl.selectedJob.triggers.length; i++){
					if(ctrl.selectedTrigger == ctrl.selectedJob.triggers[i]){
						ctrl.selectedJob.triggers.splice(i);
						break;
					}
				}
				ctrl.selectedTrigger = undefined;
				$mdDialog.show( 
					$mdDialog.alert()
				        .parent(angular.element(document.body))
				        .clickOutsideToClose(false)
				        .title(sbiModule_translate.load("sbi.generic.ok"))
				        .content(sbiModule_translate.load("sbi.scheduler.schedulation.deleted"))
				        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
			}
		})
		.error(function(data, status, headers, config) {
			sbiModule_logger.log("unable to delete schedulation " + status);
		});
	}

	ctrl.deleteJob = function(){
		sbiModule_restServices.remove("scheduler", 'deleteJob', "jobGroup="+ctrl.selectedJob.jobGroup+"&jobName="+ctrl.selectedJob.jobName)
			.success(function(data, status, headers, config){
				if (data.hasOwnProperty("errors")) {
					console.log("Job deletion error");
				} else {
					ctrl.closeDetail();
					ctrl.loadJobs();
				}
			})
			.error(function(data, status, headers, config){
				console.log("Job deletion error " + status);
			})
	}
	
	ctrl.showToastOk = function(message) {
		var toast = $mdToast.simple()
			.content(message)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top right')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	};

	ctrl.showToastError = function(message) {
		var toast = $mdToast.simple()
			.content(message)
			.action('OK')
			.highlightAction(true)
			.position('top right')

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {
			}
		});
	};
	
	ctrl.selectDocument = function(documentIndex){
		ctrl.selectedDocumentIndex = documentIndex;
		if(documentIndex >= 0 && documentIndex < ctrl.selectedJob.documents.length){
			ctrl.loadSelectedDocument();
		}else{
			ctrl.selectedDocument = undefined;
		}
	}
	
	ctrl.loadSelectedDocument = function(){
		var selectedDocumentName = ctrl.selectedJob.documents[ctrl.selectedDocumentIndex].name;
		sbiModule_restServices.get("1.0/documents", selectedDocumentName)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("unable to get document");
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					ctrl.selectedDocument = data;
					ctrl.loadSelectedDocumentRolesAndParameters();
				}
			})
			.error(function(data, status, headers, config) {
				console.log("unable to get document " + status);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
			})
	}
	
	ctrl.loadSelectedDocumentRolesAndParameters = function(){
		sbiModule_restServices.get("2.0/documents", ctrl.selectedDocument.id+"/userroles")
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("unable to get document roles");
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					ctrl.selectedDocumentRoles = []
					for(var i=0; i<data.length; i++){
						var userAndRole = data[i].split("|");
						var user = userAndRole[0];
						var role = userAndRole[1];
						ctrl.selectedDocumentRoles.push({userAndRole:data[i],user:user,role:role});
					}
					if(ctrl.selectedDocumentRoles.length > 0){
						var doc = ctrl.selectedJob.documents[ctrl.selectedDocumentIndex];
						for(var i=0; i<doc.parameters.length; i++){
							var parameter = doc.parameters[i];
							parameter.role = ctrl.selectedDocumentRoles[0].role;
						}
					}
					ctrl.loadSelectedDocumentParameters();
				}
			})
			.error(function(data, status, headers, config) {
				console.log("unable to get document roles " + status);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
			})
	}
	
	ctrl.loadSelectedDocumentParameters = function(){
		sbiModule_restServices.get("2.0/documents", ctrl.selectedDocument.label+"/parameters")
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("unable to get document parameters");
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					ctrl.selectedDocumentParameters = data;
					for(var i=0; i<ctrl.selectedJob.documents[ctrl.selectedDocumentIndex].parameters.length; i++){
						var parameter = ctrl.selectedJob.documents[ctrl.selectedDocumentIndex].parameters[i];
						parameter.temporal = ctrl.getParameterByName(parameter.name).parameter.temporal;
						if(parameter.type == "fixed"){
							ctrl.loadParameterValues(parameter);
						}
					}
				}
			})
			.error(function(data, status, headers, config) {
				console.log("unable to get document parameters " + status);
				ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
			})
	}
	
	ctrl.addDocument = function(){
		$mdDialog.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'docCtrl',
			controller : function($mdDialog) {
				var docCtrl = this;
				
				sbiModule_restServices.get("scheduler/folders", "?includeDocs=true")
					.success(function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("unable to load folders ", data.errors);
							ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							docCtrl.folders = data;
						}
					})
					.error(function(data, status, headers, config) {
						console.log("unable to load folders ", status);
						ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
					});
				
				docCtrl.setSelectedDocument = function(item){
					docCtrl.selectedDocument = item;
				}
				
				docCtrl.submit = function() {
					$mdDialog.hide();
					
					sbiModule_restServices.get("2.0/documents", docCtrl.selectedDocument.label+"/parameters")
						.success(function(data, status, headers, config) {
							var newDocument = {};
							newDocument.name = docCtrl.selectedDocument.label;
							newDocument.parameters = [];
							for(var i=0;i<data.length;i++){
								var newParam = {};
								newParam.name = data[i].parameterUrlName;
								newParam.value = "";
								newParam.type = "fixed";
								newParam.iterative = false;
								newDocument.parameters.push(newParam);
							}
							newDocument.condensedParameters = "";
							for(var i=0;i<newDocument.parameters.length;i++){
								var param = newDocument.parameters[i];
								if(param.type=="fixed"){
									newDocument.condensedParameters += " " + param.name + " = " + param.value + " | ";
								}
							}
							ctrl.selectedJob.documents.push(newDocument);
							ctrl.selectDocument(ctrl.selectedJob.documents.length - 1);
						})
						.error(function(data, status, headers, config) {
							//sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
						});
				};
				
				docCtrl.cancel = function($event) {
					$mdDialog.cancel();
				};
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/scheduler/templates/dialog-document-selection.html'
		});
	}
	
	ctrl.deleteDocument = function(){
		if(ctrl.selectedDocumentIndex >= 0){
			ctrl.selectedJob.documents.splice(ctrl.selectedDocumentIndex, 1);
			
			if(ctrl.selectedJob.documents.length > 0){
				if(ctrl.selectedDocumentIndex > 0){
					ctrl.selectDocument(ctrl.selectedDocumentIndex - 1);
				}
			}else{
				ctrl.selectDocument(-1);
			}
		}
	}
	
	ctrl.cloneDocument = function(){
		if(ctrl.selectedDocumentIndex >= 0){
			var document = ctrl.selectedJob.documents[ctrl.selectedDocumentIndex];
			var newDocument = JSON.parse(JSON.stringify(document));
			ctrl.selectedJob.documents.push(newDocument);
			
			ctrl.selectDocument(ctrl.selectedJob.documents.length - 1);
		}
	}
	
	ctrl.selectOverviewTab = function(){
		ctrl.isOverviewTabActive = true;
	}
	
	ctrl.selectDetailTab = function(){
		ctrl.isOverviewTabActive = false;
	}
	
	ctrl.isSelectedJobNew = function(){
		return ctrl.selectedJob !== undefined && ctrl.selectedJob.NEWJOB === true;
	}
	
	ctrl.getDetailTitle = function(){
		if(ctrl.selectedJob){
			if(ctrl.selectedJob.NEWJOB == true){
				return sbiModule_translate.load("sbi.generic.new");
			}else{
				return ctrl.selectedJob.jobName;
			}
		}else{
			return "";
		}
	}
	
	ctrl.getParameterByName = function(parameterName){
		if(ctrl.selectedDocumentParameters){
			for(var i=0; i<ctrl.selectedDocumentParameters.length; i++){
				var parameter = ctrl.selectedDocumentParameters[i];
				if(parameter.parameterUrlName == parameterName){
					return parameter;
				}
			}
		}
	}
	
	ctrl.loadParameterValues = function(parameter){
		var selectedDocumentName = ctrl.selectedJob.documents[ctrl.selectedDocumentIndex].name
		var parameterId = ctrl.getParameterByName(parameter.name).parID;
		if(parameterId){
			sbiModule_restServices.get("2.0/documents", encodeURI(selectedDocumentName)+"/parameters/"+encodeURI(parameterId)+"/values?role="+encodeURI(parameter.role))
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("unable to load parameters ", data.errors);
						ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						if(parameter.value.trim() != ""){
							parameter.selectedValues = parameter.value.split(";");
						}else{
							parameter.selectedValues = [];
						}
						parameter.values = data.values;
						parameter.manualInput = data.manualInput;
					}
				})
				.error(function(data, status, headers, config) {
					console.log("unable to load parameters ", status);
					ctrl.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
				});
		}
	}
	
	ctrl.saveParameterValues = function(parameter){
		var parameterList = "";
		if(parameter.selectedValues){
			for(var i = 0; i < parameter.selectedValues.length; i++){
				var selectedValue = parameter.selectedValues[i];
				parameterList += selectedValue + ";"; 
			}
			parameter.value = parameterList.substring(0, parameterList.length - 1);
		}
	}

	ctrl.setDefaultValue = function(parameter){
		if(parameter.type == "fixed" || parameter.type == "formula"){
			parameter.value = "";
		}else if(parameter.type == "loadAtRuntime"){
			parameter.value = ctrl.selectedDocumentRoles[0].userAndRole
		}
		if(parameter.type == "fixed"){
			ctrl.loadParameterValues(parameter);
		}
	}
	
	ctrl.addTrigger = function(){
		ctrl.editTrigger(ctrl.selectedJob.jobName, ctrl.selectedJob.jobGroup, "", "");
	}
	
	ctrl.showTriggerInfo = function(){
		$mdDialog.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'triggerInfoCtrl',
			controller : function($mdDialog) {
				var triggerInfoCtrl = this;
				
				var requestString =
					"getTriggerInfo?jobName="+ctrl.selectedJob.jobName
					+"&jobGroup="+ctrl.selectedJob.jobGroup
					+"&triggerName="+ctrl.selectedTrigger.triggerName
					+"&triggerGroup="+ctrl.selectedTrigger.triggerGroup;
				
				sbiModule_restServices.post("scheduler", requestString)
					.success(function(data, status, headers, config) {
						triggerInfoCtrl.triggerInfo = data;
					})
					.error(function(data, status, headers, config) {
						sbiModule_logger.log("unable to load schedulation info " + status);
					});
				
				triggerInfoCtrl.cancel = function($event) {
					$mdDialog.cancel();
				};
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/scheduler/templates/dialog-trigger-info.html'
		});
	}
	
	ctrl.editTrigger = function(jobName, jobGroup, triggerName, triggerGroup){
		$mdDialog.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'activityEventCtrl',
			controller : function($mdDialog) {
				var activityEventCtrl = this;
				
				sbiModule_translate.addMessageFile("component_scheduler_messages");
				$scope.translate = sbiModule_translate;
				activityEventCtrl.SCHEDULER_TYPES = [
					{value: 'single', label: sbiModule_translate.load("scheduler.singleExec", "component_scheduler_messages")},
					{value: 'scheduler', label: sbiModule_translate.load("scheduler.schedulerExec", "component_scheduler_messages")},
					{value: 'event', label: sbiModule_translate.load("scheduler.eventExec", "component_scheduler_messages")}
				];
				activityEventCtrl.EVENT_TYPES = [
					{value: 'rest', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.rest")},
					{value: 'jms', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.jms")},
					{value: 'contextbroker', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.contextbroker")},
					{value: 'dataset', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}
				];
				activityEventCtrl.EVENT_INTERVALS = [
					{value: 'minute', label: sbiModule_translate.load("scheduler.minuteExec", "component_scheduler_messages")},
					{value: 'hour', label: sbiModule_translate.load("scheduler.hourExec", "component_scheduler_messages")},
					{value: 'day', label: sbiModule_translate.load("scheduler.dayExec", "component_scheduler_messages")},
					{value: 'week', label: sbiModule_translate.load("scheduler.weekExec", "component_scheduler_messages")},
					{value: 'month', label: sbiModule_translate.load("scheduler.monthExec", "component_scheduler_messages")}
				];
				activityEventCtrl.MONTHS = [
					{label: sbiModule_translate.load("scheduler.jan", "component_scheduler_messages"), value: '1'},
					{label: sbiModule_translate.load("scheduler.feb", "component_scheduler_messages"), value: '2'}, 
					{label: sbiModule_translate.load("scheduler.mar", "component_scheduler_messages"), value: '3'}, 
					{label: sbiModule_translate.load("scheduler.apr", "component_scheduler_messages"), value: '4'}, 
					{label: sbiModule_translate.load("scheduler.may", "component_scheduler_messages"), value: '5'}, 
					{label: sbiModule_translate.load("scheduler.jun", "component_scheduler_messages"), value: '6'}, 
					{label: sbiModule_translate.load("scheduler.jul", "component_scheduler_messages"), value: '7'}, 
					{label: sbiModule_translate.load("scheduler.aug", "component_scheduler_messages"), value: '8'}, 
					{label: sbiModule_translate.load("scheduler.sep", "component_scheduler_messages"), value: '9'}, 
					{label: sbiModule_translate.load("scheduler.oct", "component_scheduler_messages"), value: '10'}, 
					{label: sbiModule_translate.load("scheduler.nov", "component_scheduler_messages"), value: '11'}, 
					{label: sbiModule_translate.load("scheduler.dic", "component_scheduler_messages"), value: '12'}
				];
				activityEventCtrl.WEEKS = [
					{label: sbiModule_translate.load("scheduler.sun", "component_scheduler_messages"), value: '1'}, 
					{label: sbiModule_translate.load("scheduler.mon", "component_scheduler_messages"), value: '2'}, 
					{label: sbiModule_translate.load("scheduler.tue", "component_scheduler_messages"), value: '3'}, 
					{label: sbiModule_translate.load("scheduler.wed", "component_scheduler_messages"), value: '4'}, 
					{label: sbiModule_translate.load("scheduler.thu", "component_scheduler_messages"), value: '5'}, 
					{label: sbiModule_translate.load("scheduler.fri", "component_scheduler_messages"), value: '6'}, 
					{label: sbiModule_translate.load("scheduler.sat", "component_scheduler_messages"), value: '7'}
				];
				activityEventCtrl.WEEKS_ORDER = [
					{label: sbiModule_translate.load("scheduler.firstweek", "component_scheduler_messages"), value: '1'}, 
					{label: sbiModule_translate.load("scheduler.secondweek", "component_scheduler_messages"), value: '2'}, 
					{label: sbiModule_translate.load("scheduler.thirdweek", "component_scheduler_messages"), value: '3'}, 
					{label: sbiModule_translate.load("scheduler.fourthweek", "component_scheduler_messages"), value: '4'}, 
					{label: sbiModule_translate.load("scheduler.lastweek", "component_scheduler_messages"), value: '5'}, 
				];
				
				activityEventCtrl.useFixedFolderInfo = sbiModule_translate.load("scheduler.help.useFixedFolder", "component_scheduler_messages");
				activityEventCtrl.useFolderDatasetInfo = sbiModule_translate.load("scheduler.help.useFolderDataset", "component_scheduler_messages");
				activityEventCtrl.useFixedRecipientsInfo = sbiModule_translate.load("scheduler.help.useFixedRecipients", "component_scheduler_messages");
				activityEventCtrl.useDatasetInfo = sbiModule_translate.load("scheduler.help.useDataset", "component_scheduler_messages");
				activityEventCtrl.useExpressionInfo = sbiModule_translate.load("scheduler.help.useExpression", "component_scheduler_messages");
				
				activityEventCtrl.useFixedFolderFlag = false;
				activityEventCtrl.useFolderDatasetFlag = false;
				activityEventCtrl.useFixedRecipientsFlag = false;
				activityEventCtrl.useDatasetFlag = false;
				activityEventCtrl.useExpressionFlag = false;
				
				activityEventCtrl.event = {};
				activityEventCtrl.disableName=false;
				activityEventCtrl.event.jobName = '';
				activityEventCtrl.event.jobGroup = '';
				activityEventCtrl.event.triggerName = '';
				activityEventCtrl.event.triggerGroup = '';
				activityEventCtrl.event.triggerDescription = '';
				activityEventCtrl.event.documents = [];
				activityEventCtrl.datasets = [];
				activityEventCtrl.JobDocuments = [];
				activityEventCtrl.eventSched = {};
				activityEventCtrl.selectedDocument = [];
				activityEventCtrl.selectedWeek = [];
				
				activityEventCtrl.initJobsValues = function(jobName, jobGroup, triggerName, triggerGroup) {
					activityEventCtrl.event.jobName = jobName;
					activityEventCtrl.event.jobGroup = jobGroup;
					activityEventCtrl.event.triggerName = triggerName;
					activityEventCtrl.event.triggerGroup = triggerGroup;
					activityEventCtrl.jobData = null;
					
					var loadtri = triggerName != undefined
							&& triggerName != null
							&& triggerName.trim() != ""
							&& triggerGroup != undefined
							&& triggerGroup != null
							&& triggerGroup.trim() != "";
					
					activityEventCtrl.loadDataset();
					activityEventCtrl.loadJobData(loadtri);
				};
				
				activityEventCtrl.loadDataset = function() {
					sbiModule_restServices.get("2.0/datasets", "listDataset")
						.success(function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								console.error(sbiModule_translate.load("sbi.glossary.load.error"))
							} else {
								activityEventCtrl.datasets = data.item;
							}
						})
						.error(function(data, status, headers, config) {
							console.error(sbiModule_translate.load("sbi.glossary.load.error"))
						});
				};
				
				activityEventCtrl.loadJobData = function(loadTri) {
					var parameters = 'jobName=' + activityEventCtrl.event.jobName + '&jobGroup=' + activityEventCtrl.event.jobGroup;
					
					sbiModule_restServices.get("scheduler", "getJob", parameters)
						.success(function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								console.error(sbiModule_translate.load("sbi.glossary.load.error"))
							} else {
								console.log("data", data);
								
								activityEventCtrl.jobData = data.job;
								activityEventCtrl.lowFunc =data.functionality;
								activityEventCtrl.loadDocuments(loadTri);
							}
						})
						.error(function(data, status, headers, config) {
							console.error(sbiModule_translate.load("sbi.glossary.load.error"));
						});
				};
				
				activityEventCtrl.loadDocuments = function(loadTri) {
					var docs = activityEventCtrl.jobData.documents;
					for(var i = 0; i < docs.length; i++) {
						var doc = {
							labelId: docs[i].id + "__" + (i+1),
							id: docs[i].id,
							label: docs[i].name,
							parameters: docs[i].parameters
						};
						activityEventCtrl.JobDocuments.push(doc);
					}
					activityEventCtrl.createNewEvent(loadTri);
				};
				
				activityEventCtrl.getEmptyEvent = function() {
					var emptyEvent = {
						jobName: activityEventCtrl.event.jobName,
						jobGroup: activityEventCtrl.event.jobGroup,
						triggerName: activityEventCtrl.event.triggerName,
						triggerDescription: (activityEventCtrl.event.triggerDescription  && activityEventCtrl.event.triggerDescription != null ) ? activityEventCtrl.event.triggerDescription : '',
						triggerGroup: activityEventCtrl.event.triggerGroup,
						documents: [],
						chrono: {"type": "single"}
					};
					
					activityEventCtrl.typeOperation = 'single';
					
					//load document;
					for (var i = 0; i < activityEventCtrl.JobDocuments.length; i++) {
						var tmp = {};
						var doc = activityEventCtrl.JobDocuments[i];
						tmp.label = doc.label;
						tmp.parameters = doc.parameters;
						tmp.labelId = doc.labelId;
						tmp.id = doc.id;
						emptyEvent.documents.push(tmp);
					}
					
					return emptyEvent;
				};
				
				activityEventCtrl.loadScheduler = function() {
					var requestString = 
						"getTriggerInfo?jobName=" + activityEventCtrl.event.jobName
						+"&jobGroup=" + activityEventCtrl.event.jobGroup
						+"&triggerGroup=" + activityEventCtrl.event.triggerGroup
						+"&triggerName=" + activityEventCtrl.event.triggerName;
					
					sbiModule_restServices.post("scheduler", requestString	)
						.success(function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								console.error(sbiModule_translate.load("sbi.glossary.load.error"));
							} else {
								console.log("evento scaricato", data);
								activityEventCtrl.disableName=true;
								
								var d = data;
								activityEventCtrl.event.triggerName = d.triggerName;
								activityEventCtrl.event.triggerDescription = 
									(d.triggerDescription && d.triggerDescription != null) ? d.triggerDescription : "";
								activityEventCtrl.event.startDate = new Date(d.startDateRFC3339);
								activityEventCtrl.event.startTime = d.startTime;
								
								if(d.endTime != undefined && d.endTime != "") {
									activityEventCtrl.event.endTime = d.endTime;
								} else {
									activityEventCtrl.event.endTime = "";
								}
								
								if(d.endDate != undefined && d.endDate != "") {
									activityEventCtrl.event.endDate = new Date(d.endDateRFC3339);
								}
								
								activityEventCtrl.event.chrono = d.chrono;
								
								var op = d.chrono;
								activityEventCtrl.eventSched.repetitionKind = op.type;
								
								if(op.type == 'single') {
									activityEventCtrl.typeOperation = op.type;
									activityEventCtrl.shedulerType = false;
								} else if(op.type == 'event') {
									activityEventCtrl.typeOperation = op.type;
									activityEventCtrl.shedulerType = false;
									activityEventCtrl.eventSched.event_type = op.parameter.type;
									
									if(op.parameter.type == "dataset") {
										activityEventCtrl.eventSched.dataset = op.parameter.dataset;
										activityEventCtrl.eventSched.frequency = op.parameter.frequency;
									}
								} else {
									activityEventCtrl.typeOperation = "scheduler";
									activityEventCtrl.shedulerType = true;
									if(op.type == 'minute'){
										activityEventCtrl.eventSched.minute_repetition_n=op.parameter.numRepetition;
									} else if(op.type == 'hour'){
										activityEventCtrl.eventSched.hour_repetition_n=op.parameter.numRepetition;
									} else if(op.type == 'day'){
										activityEventCtrl.eventSched.day_repetition_n=op.parameter.numRepetition;
									} else if(op.type == 'week') {	
										activityEventCtrl.selectedWeek = op.parameter.days;
									} else if(op.type == 'month') {
										if(op.parameter.hasOwnProperty("months")) {
											activityEventCtrl.typeMonth = false;
											activityEventCtrl.month_repetition = op.parameter.months;
										} else {
											activityEventCtrl.typeMonth = true;
											activityEventCtrl.monthrep_n = op.parameter.numRepetition;
											activityEventCtrl.month_week_number_repetition = op.parameter.weeks;
											activityEventCtrl.month_week_repetition = op.parameter.days;
										}
										
										if(op.parameter.hasOwnProperty("days")) {
											activityEventCtrl.typeMonthWeek = false;
											activityEventCtrl.month_week_number_repetition = op.parameter.weeks;
											activityEventCtrl.month_week_repetition = op.parameter.days;
										} else {
											activityEventCtrl.typeMonthWeek = true;
											activityEventCtrl.dayinmonthrep_week = op.parameter.dayRepetition;
										}
									}
								}
								
								//carico le informazioni dei documenti
								activityEventCtrl.event.documents=d.documents;
							
								activityEventCtrl.selectedDocument = (activityEventCtrl.event.documents == undefined || activityEventCtrl.event.documents.length != 0) ? activityEventCtrl.event.documents[0] : [];
							}
						})
						.error(function(data, status, headers, config) {
							console.error(sbiModule_translate.load("sbi.glossary.load.error"))
						});
				};
				
				activityEventCtrl.createNewEvent = function(loadTrigger) {
					activityEventCtrl.event = activityEventCtrl.getEmptyEvent();
					activityEventCtrl.setSelectedDocument();
					if(loadTrigger) {
						activityEventCtrl.loadScheduler();
					}
				};
				
				activityEventCtrl.setSelectedDocument = function() {
					activityEventCtrl.selectedDocument = (activityEventCtrl.event.documents == undefined || activityEventCtrl.event.documents.length != 0)? activityEventCtrl.event.documents[0] : [];
				};
				
				activityEventCtrl.triggerEvent = function() {
					var requestTriggerEvent = "eventName=" + activityEventCtrl.event.triggerName
					
					sbiModule_restServices.get("scheduler", "triggerEvent", requestTriggerEvent)
						.success(function(data, status, headers, config) {
							if (data.hasOwnProperty("errors")) {
								console.error(sbiModule_translate.load("sbi.glossary.load.error"))
							} else {
								console.log("data", data);
							}
						})
						.error(function(data, status, headers, config) {
							console.error(sbiModule_translate.load("ERRORE triggerEvent"));
						});
					
				};
				
				activityEventCtrl.saveEvent = function(isValid,saveAndReturn) {
					if (!isValid) {
						return false;
					}
					var cloneData=JSON.parse(JSON.stringify(activityEventCtrl.event));
					if(cloneData.startDate!=undefined){
						cloneData.startDate=(new Date(cloneData.startDate)).getTime();
					}
					if(cloneData.endDate!=undefined){
						cloneData.endDate=(new Date(cloneData.endDate)).getTime();
					}
					sbiModule_restServices.post("scheduler", "saveTrigger", cloneData)
						.success(function(data) {
							if (data.hasOwnProperty("errors")) {
								console.error(sbiModule_translate.load("sbi.glossary.error.save"));
							} else if (data.Status == "NON OK") {
								console.error("errori salvataggio",data.errors);
								ctrl.showToastError(sbiModule_translate.load("sbi.glossary.error.save") + " " + data.errors);
							} else {
								ctrl.showToastOk(sbiModule_translate.load("sbi.glossary.success.save"));
								activityEventCtrl.disableName=true;
								ctrl.reloadJob();
								if(saveAndReturn){
									$mdDialog.hide();
								}
							}
						})
						.error(function(data, status, headers, config) {
							ctrl.showToastError(sbiModule_translate.load("sbi.glossary.error.save"));
							return false;
						});
				};
				
				activityEventCtrl.changeTypeOperation = function() {
					var tip = activityEventCtrl.typeOperation;
					switch(tip) {
						case 'single': 
							activityEventCtrl.eventSched.repetitionKind = 'single'; 
							activityEventCtrl.shedulerType = false; 
							break;
						case 'scheduler': 
							activityEventCtrl.shedulerType = true; 
							break;
						case 'event': 
							activityEventCtrl.eventSched.repetitionKind = 'event'; 
							activityEventCtrl.shedulerType = false; 
							break;
					}
					activityEventCtrl.changeTypeFrequency();
				};
				
				activityEventCtrl.getActivityRepetitionKindForScheduler = function() {
					if(activityEventCtrl.eventSched.repetitionKind == undefined 
							|| activityEventCtrl.eventSched.repetitionKind == 'single' 
							|| activityEventCtrl.eventSched.repetitionKind == 'event' ) {
						activityEventCtrl.eventSched.repetitionKind = 'minute';
					}
				};
				
				activityEventCtrl.getNitem = function(n) {
					var r =[];
					for(var i = 1; i <= n; i++) {
						r.push(i);
					}
					return r;
				};
				
				activityEventCtrl.toggleMonthScheduler = function() {
					activityEventCtrl.event.chrono = {
						"type": "month", 
						"parameter": {}
					};
					 
					if(activityEventCtrl.typeMonth == true) {
						activityEventCtrl.event.chrono.parameter.numRepetition = activityEventCtrl.monthrep_n;
					} else {
						activityEventCtrl.event.chrono.parameter.months = [];
						for(var k in activityEventCtrl.month_repetition) {
							activityEventCtrl.event.chrono.parameter.months.push(activityEventCtrl.month_repetition[k]);
						}
					}
						
					if(activityEventCtrl.typeMonthWeek == true) {
						activityEventCtrl.event.chrono.parameter.dayRepetition = activityEventCtrl.dayinmonthrep_week;
					} else {
						var mwnr = activityEventCtrl.month_week_number_repetition;
						
						if(mwnr == undefined) {
							mwnr = 'first';
						}
						
						activityEventCtrl.event.chrono.parameter.weeks = mwnr;
						activityEventCtrl.event.chrono.parameter.days = [];
						
						for(var k in activityEventCtrl.month_week_repetition) {
							activityEventCtrl.event.chrono.parameter.days.push(activityEventCtrl.month_week_repetition[k]);
						}
					}
				};
				
				activityEventCtrl.toggleWeek = function(week) {
					if(week != undefined) {
						var idx = activityEventCtrl.selectedWeek.indexOf(week);
						if (idx > -1) {
							activityEventCtrl.selectedWeek.splice(idx, 1);
						} else {
							activityEventCtrl.selectedWeek.push(week);
						}
					}
				
					activityEventCtrl.event.chrono = {
						"type": "week", 
						"parameter": {
							"days": []
						}
					};
					
					for(var k in activityEventCtrl.selectedWeek ) {
						activityEventCtrl.event.chrono.parameter.days.push(activityEventCtrl.selectedWeek[k]);
					}
				};
				
				activityEventCtrl.changeTypeFrequency = function() {
					$timeout(function() {
						var tip = activityEventCtrl.eventSched.repetitionKind;
						
						switch(tip) {
							case 'event': 
								activityEventCtrl.event.chrono = {
									"type": "event", 
									"parameter": {
										"type": activityEventCtrl.eventSched.event_type
									}
								};
								if(activityEventCtrl.eventSched.event_type == 'dataset') {
									activityEventCtrl.event.chrono.parameter.dataset = activityEventCtrl.eventSched.dataset;
									activityEventCtrl.event.chrono.parameter.frequency = activityEventCtrl.eventSched.frequency;
								}
								break;
							case 'single': 
								activityEventCtrl.event.chrono = {
									"type": "single"
								}; 
								break;
							case 'minute': 
								activityEventCtrl.event.chrono = {
									"type": "minute", 
									"parameter": {
										"numRepetition": activityEventCtrl.eventSched.minute_repetition_n
									}
								}; 
								break;
							case 'hour': 
								activityEventCtrl.event.chrono = {
									"type": "hour", 
									"parameter": {
										"numRepetition": activityEventCtrl.eventSched.hour_repetition_n
										}
								}; 
								break;
							case 'day': 
								activityEventCtrl.event.chrono = {
									"type": "day", 
									"parameter": {
										"numRepetition": activityEventCtrl.eventSched.day_repetition_n
										}
								};
								break;
							case 'week': 
								activityEventCtrl.toggleWeek(); 
								break;
							case 'month': 
								activityEventCtrl.toggleMonthScheduler();
								break;
						}
						console.log('chrono', activityEventCtrl.event.chrono);
					}, 500);
				};
				
				activityEventCtrl.isChecked = function (item, list, condition) {
					if(condition) {
						return list == undefined ? false : list.indexOf(item) > -1;
					} else {
						return false;
					}
				};
				
				activityEventCtrl.toggleDocFunct = function(doc, funct) {
					if(funct != undefined) {
						if(doc.funct == undefined) {
							doc.funct = [];
						}
						var idx = doc.funct.indexOf(funct);
						if (idx > -1) {
							doc.funct.splice(idx, 1);
						} else {
							doc.funct.push(funct);
						}
					}
				};
				
				activityEventCtrl.onlyNumberConvert = function(item) {
					return item.replace(/\D/g,'');
				};
				
				activityEventCtrl.prova = function(item) {
					console.log("prova",item); 	
				};
				
				activityEventCtrl.toggleEnabled = function(item,item2) {
					console.log("toggleEnabled",item,item2); 	
				};
				
				activityEventCtrl.sampleModel=[{name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
											   {name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
											   {name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
											   {name:"name3",surname:"surname3",age:'<md-checkbox   ng-click="toggleEnabled(row, key)" ng-init="true"></md-checkbox>'},
											   {name:"name5",surname:"surname5",age:"18"},
											   {name:"name6",surname:"surname6",age:"32"},
											   {name:"name7",surname:"surname7",age:"18"},
											   {name:"name8",surname:"surname8",age:"18"},
											   {name:"name9",surname:"surname9",age:"18"},
											   {name:"name10",surname:"surname10",age:"18"},
											   {name:"name11",surname:"surname11",age:"27"},
											   {name:"name12",surname:"surname12",age:"18"},
											   {name:"name13",surname:"surname13",age:"18"},
											   {name:"name14",surname:"surname14",age:"11"},
											   {name:"name15",surname:"surname15",age:"18"},
											   {name:"name16",surname:"surname16",age:"18"},
											   {name:"name17",surname:"surname17",age:"80"},
											   {name:"name18",surname:"surname18",age:"18"},
											   {name:"name19",surname:"surname19",age:"18"},
											 ];
				
				activityEventCtrl.MenuOpt = [
					{
						label : 'action1',
						action : function(item,event) {
								myfunction1(event,item);
						}
					},
					{
						label : 'action2',
						action : function(item,event) {
								myfunction2 (event,item);
						}
					}
				];
				
				activityEventCtrl.SpeedMenuOpt  = [
					{
						label : 'action1',
						icon:'fa fa-pencil' ,  
						backgroundColor:'red',  
						color:'black',		
						action : function(item,event) {
							myFunction(event,item);
						}
					} 
				];
				
				activityEventCtrl.showInfoBox=function(title,text,parentId){
					$mdDialog.show(
						$mdDialog.alert()
							.clickOutsideToClose(true)
							.title(title)
							.content(text)
							.ariaLabel('info dialog')
							.ok(sbiModule_translate.load("sbi.general.close")) 
					);
				}
				
				activityEventCtrl.cancel = function($event) {
					$mdDialog.cancel();
				};
				
				activityEventCtrl.initJobsValues(jobName, jobGroup, triggerName, triggerGroup);
			},
			templateUrl : '/knowage/js/src/angular_1.4/tools/scheduler/templates/dialog-trigger.jsp'
		});
	}
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage');
    $mdThemingProvider.setDefaultTheme('knowage');
}]);
