var app = angular.module('jobMonitor', [ 'ngMaterial', 'ui.tree',
                                         'angularUtils.directives.dirPagination', 'ng-context-menu',
                                         'angular_list', 'angular_table' ,'sbiModule', 'angular-list-detail','document_tree',
                                         'angular_time_picker', 'ngMessages', 'ngSanitize']);

app.controller('Controller', ["sbiModule_download", "sbiModule_translate","sbiModule_restServices", "sbiModule_logger", "sbiModule_config", "sbiModule_dateServices", "$scope", "$mdDialog", "$mdToast", "$timeout", "$window", mainFunction]);

function mainFunction(sbiModule_download, sbiModule_translate, sbiModule_restServices, sbiModule_logger, sbiModule_config, sbiModule_dateServices, $scope, $mdDialog, $mdToast, $timeout, $window) {
	
	sbiModule_translate.addMessageFile("component_scheduler_messages");
	$scope.translate = sbiModule_translate;
	
	// variables
	
	$scope.executions = [];
	$scope.selectedExecution = null;
	
	$scope.minDate = new Date();
	$scope.minDate = new Date($scope.minDate.getFullYear(), $scope.minDate.getMonth(), $scope.minDate.getDate());
	
	$scope.startDate = $scope.minDate;
	$scope.endDate = new Date($scope.startDate.getFullYear(), $scope.startDate.getMonth(), $scope.startDate.getDate() + 1);
	
	$scope.startDateTime = null;
	$scope.endDateTime = null;
	
	// watch
	
	$scope.$watch("startDate", function(newValue, oldValue) {
		$scope.updateStartDateTime(newValue, $scope.startTime);
		$scope.updateExecutionList();
	});
	
	$scope.$watch("startTime", function(newValue, oldValue) {
		$scope.updateStartDateTime($scope.startDate, newValue);
		$scope.updateExecutionList();
	});
	
	$scope.$watch("endDate", function(newValue, oldValue) {
		$scope.updateEndDateTime(newValue, $scope.endTime);
		$scope.updateExecutionList();
	});
	
	$scope.$watch("endTime", function(newValue, oldValue) {
		$scope.updateEndDateTime($scope.endDate, newValue);
		$scope.updateExecutionList();
	});
	
	// functions

	$scope.updateStartDateTime = function(date, time){
		if(date && time){
			$scope.startDateTime = $scope.getDateTimeString(date, time);
		}
	}
	
	$scope.updateEndDateTime = function(date, time){
		if(date && time){
			$scope.endDateTime = $scope.getDateTimeString(date, time);
		}
	}
	
	$scope.getDateTimeString = function(date, time){
		var result = "";
		if(date && time){
			var padding = "00";
			var month = "" + (date.getMonth() + 1);
			month = padding.substring(0, padding.length - month.length) + month;
			var day = "" + date.getDate();
			day = padding.substring(0, padding.length - day.length) + day;
			result = date.getFullYear() + '-' + month + '-' + day + 'T' + time + ':00';
		}
		return result;
	}
	
	$scope.updateExecutionList = function(){
		if($scope.startDateTime && $scope.endDateTime){
			sbiModule_restServices.get("2.0/scheduler", 'nextExecutions?start='+$scope.startDateTime+'&end='+$scope.endDateTime)
				.success(function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						$scope.executions = [];
						console.log("unable to get executions");
						$scope.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
					} else {
						var localizedTimestampFormat = sbiModule_config.localizedTimestampFormat.replace("Y", "y").replace("m", "M").replace("i", "m");
						var clientServerTimestampFormat = sbiModule_config.clientServerTimestampFormat.replace("Y", "y").replace("m", "M").replace("i", "m");
						$scope.executions = [];
						for(var jobIndex = 0; jobIndex < data.root.length; jobIndex++){
							var job = data.root[jobIndex];
							for(var triggerIndex = 0; triggerIndex < job.triggers.length; triggerIndex++){
								var trigger = job.triggers[triggerIndex];
								for(var docIndex = 0; docIndex < trigger.documents.length; docIndex++){
									for(var executionIndex = 0; executionIndex < trigger.executions.length; executionIndex++){
										var date = sbiModule_dateServices.getDateFromFormat(trigger.executions[executionIndex], clientServerTimestampFormat);
										var execution = {
											executionDate: sbiModule_dateServices.formatDate(date, localizedTimestampFormat),
											jobName: job.name,
											triggerName: trigger.name,
											triggerType: trigger.type,
											documentName: trigger.documents[docIndex]
										};
										$scope.executions.push(execution);
									}
								}
							}
						}
					}
				})
				.error(function(data, status, headers, config) {
					$scope.executions = [];
					console.log("unable to get executions " + status);
					if(data && data.errors && data.errors[0] && data.errors[0].message){
						$scope.showToastError(data.errors[0].message);
					}else{
						$scope.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
					}
				});
		}
	}
	
	$scope.menuExecution = [{
		label : sbiModule_translate.load('sbi.generic.edit'),
		icon:'fa fa-pencil',	 
		action : function(item,event){
			$scope.selectedExecution = item;
			var confirm = $mdDialog.confirm()
				.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
				.content(sbiModule_translate.load("sbi.generic.edit"))
				.ariaLabel('Edit job')
				.ok(sbiModule_translate.load("sbi.general.continue"))
				.cancel(sbiModule_translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm)
				.then(
					function(){$scope.editJob(item);},
					function(){console.log('Job editing aborted');}
				);
		}
	}];
	
	$scope.editJob = function(item){
		var url = sbiModule_config.contextName
				+ "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_SCHEDULER_ACTION_ANGULARJS&LIGHT_NAVIGATOR_RESET_INSERT=TRUE#/?JOB_NAME="
				+ item.jobName;
		$window.location.href = url;
	}
	
	$scope.showToastError = function(message) {
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
	
	// init
	
	$scope.endDate.setDate($scope.startDate.getDate() + 1);
	$scope.updateStartDateTime($scope.startDate, $scope.startTime);
	$scope.updateStartDateTime($scope.endDate, $scope.endTime);
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage');
    $mdThemingProvider.setDefaultTheme('knowage');
}]);
