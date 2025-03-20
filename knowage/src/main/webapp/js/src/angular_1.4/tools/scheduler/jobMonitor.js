var app = angular.module('jobMonitor', [ 'ngMaterial', 'ui.tree',
                                         'angularUtils.directives.dirPagination', 'ng-context-menu',
                                         'angular_list', 'angular_table' ,'sbiModule', 'angular-list-detail','document_tree',
                                         'angular_time_picker', 'ngMessages', 'ngSanitize']);

app.controller('Controller', ["sbiModule_download", "sbiModule_translate","sbiModule_restServices",
                              "sbiModule_logger", "sbiModule_config", "sbiModule_dateServices", "$scope",
                              "$mdDialog", "$mdToast", "$timeout", "$location", "$window","sbiModule_messaging", mainFunction]);

function mainFunction(sbiModule_download, sbiModule_translate, sbiModule_restServices, sbiModule_logger,
		sbiModule_config, sbiModule_dateServices, $scope, $mdDialog, $mdToast, $timeout, $location, $window,sbiModule_messaging) {

	sbiModule_translate.addMessageFile("component_scheduler_messages");
	$scope.translate = sbiModule_translate;

	// parameters from URL

	var executionFromUrl = $location.search().ex ? JSON.parse($location.search().ex) : null;
	var startDateFromUrl = $location.search().sd ? JSON.parse($location.search().sd) : null;
	var startTimeFromUrl = $location.search().st ? JSON.parse($location.search().st) : null;
	var endDateFromUrl = $location.search().ed ? JSON.parse($location.search().ed) : null;
	var endTimeFromUrl = $location.search().et ? JSON.parse($location.search().et) : null;
	var tablePageFromUrl = $location.search().pg ? JSON.parse($location.search().pg) : null;

	// variables

	$scope.tablePage = 1;
	$scope.executions = [];
	$scope.selectedExecution = null;
	$scope.loadingExecutions = false;

	$scope.minDate = new Date();
	$scope.minDate = new Date($scope.minDate.getFullYear(), $scope.minDate.getMonth(), $scope.minDate.getDate());

	$scope.startDate = $scope.minDate;
	$scope.endDate = new Date($scope.startDate.getFullYear(), $scope.startDate.getMonth(), $scope.startDate.getDate() + 1);

	$scope.startDateTime = null;
	$scope.endDateTime = null;

	// functions

	$scope.pageChanged = function(searchValue, itemsPerPage, currentPageNumber, columnsSearch, columnOrdering, reverseOrdering){
		$scope.tablePage = currentPageNumber;
	}

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
		$scope.updateStartDateTime($scope.startDate, $scope.startTime);
		$scope.updateEndDateTime($scope.endDate, $scope.endTime);
		if($scope.startDateTime && $scope.endDateTime){
			$timeout(function(){
				$scope.loadingExecutions = true;
				sbiModule_restServices.get("scheduleree", 'nextExecutions?start='+$scope.startDateTime+'&end='+$scope.endDateTime)
					.success(function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							$scope.executions = [];
							console.log("unable to get executions");
							$scope.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
						} else {
							var localizedTimestampFormat = sbiModule_config.localizedTimestampFormat.replace("Y", "y").replace("m", "M")
									.replace("H", "HH").replace("HHHH", "HH")
									.replace("i", "mm").replace("mmmm", "mm")
									.replace("s", "ss").replace("ssss", "ss");
							var clientServerTimestampFormat = sbiModule_config.clientServerTimestampFormat.replace("Y", "y").replace("m", "M")
									.replace("H", "HH").replace("HHHH", "HH")
									.replace("i", "mm").replace("mmmm", "mm")
									.replace("s", "ss").replace("ssss", "ss");
							$scope.executions = [];
							for(var jobIndex = 0; jobIndex < data.root.length; jobIndex++){
								var job = data.root[jobIndex];
								for(var triggerIndex = 0; triggerIndex < job.triggers.length; triggerIndex++){
									var trigger = job.triggers[triggerIndex];
									for(var docIndex = 0; docIndex < trigger.documents.length; docIndex++){
										for(var executionIndex = 0; executionIndex < trigger.executions.length; executionIndex++){
											var date = new Date(trigger.executions[executionIndex]);
											var execution = {
												executionDate: sbiModule_dateServices.formatDate(date, localizedTimestampFormat),
												jobName: job.name,
												triggerName: trigger.triggerName,
												triggerType: trigger.triggerChronType,
												triggerIsPausedString: trigger.triggerIsPaused ? sbiModule_translate.load("sbi.general.yes") : sbiModule_translate.load("sbi.general.No"),
												documentName: trigger.documents[docIndex]
											};
											$scope.executions.push(execution);
										}
									}
								}
							}
						}

						if(tablePageFromUrl){
							$scope.tablePage = tablePageFromUrl;
						}

						$scope.loadingExecutions = false;
					})
					.error(function(data, status, headers, config) {
						$scope.executions = [];
						console.log("unable to get executions " + status);
						if(data && data.errors && data.errors[0] && data.errors[0].message){
							$scope.showToastError(data.errors[0].message);
						}else{
							$scope.showToastError(sbiModule_translate.load("sbi.glossary.load.error"));
						}
						$scope.loadingExecutions = false;
					});
			}, 400);
		}
	}

	$scope.menuExecution = [{
		label : sbiModule_translate.load('sbi.generic.edit'),
		icon:'fa fa-pencil',
		action : function(item,event){
			$scope.selectedExecution = item;
			var confirm = $mdDialog.confirm()
				.title(sbiModule_translate.load("sbi.generic.pleaseConfirm"))
				.content(sbiModule_translate.load("sbi.schedulation.editConfirm"))
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
				+ "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_SCHEDULER_ACTION_ANGULARJS&LIGHT_NAVIGATOR_RESET_INSERT=TRUE#/?ex="
				+ JSON.stringify(item)
				+ "&sd="
				+ JSON.stringify($scope.startDate)
				+ "&st="
				+ JSON.stringify($scope.startTime)
				+ "&ed="
				+ JSON.stringify($scope.endDate)
				+ "&et="
				+ JSON.stringify($scope.endTime)
				+ "&pg="
				+ JSON.stringify($scope.tablePage);
		$window.location.href = url;
	}

	$scope.showToastError = function(message) {
//		var toast = $mdToast.simple()
//			.content(message)
//			.action('OK')
//			.highlightAction(true)
//			.position('top right')
//			.hideDelay(0);
//
//		$mdToast.show(toast).then(function(response) {
//			if ( response == 'ok' ) {
//			}
//		});

		sbiModule_messaging.showErrorMessage(message,"");

	};

	// init

	if(startDateFromUrl){
		$scope.startDate = new Date(startDateFromUrl);
	}
	if(startTimeFromUrl){
		$scope.startTime = startTimeFromUrl;
	}
	$scope.updateStartDateTime($scope.startDate, $scope.startTime);

	if(endDateFromUrl){
		$scope.endDate = new Date(endDateFromUrl);
	}
	if(endTimeFromUrl){
		$scope.endTime = endTimeFromUrl;
	}
	$scope.updateEndDateTime($scope.endDate, $scope.endTime);

	if(executionFromUrl){
		$scope.selectedExecution = executionFromUrl;
		$scope.updateExecutionList();
	}
};

app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage');
    $mdThemingProvider.setDefaultTheme('knowage');
}]);
