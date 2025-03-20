/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.

 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular
	.module('schedulation_view_workspace', [])

	.directive('schedulationViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: currentScriptPath + '../../../templates/schedulationViewWorkspace.html',
		      controller: schedulationController
		  };
	});

function schedulationController($scope, sbiModule_messaging, $filter, $mdDialog, $httpParamSerializer, sbiModule_restServices, sbiModule_translate, sbiModule_config , $documentViewer, toastr, $timeout, sbiModule_logger){

	$scope.translate=sbiModule_translate;
	$scope.schedulationList = [];
	$scope.schedulationListForMerge = [];
	$scope.schedulationListInitial = [];
	$scope.mergeAndNotPDF = false;

	$scope.loadSchedulations = function(){

		if(!$scope.isUserAdmin && !$scope.isUserDeveloper && $scope.showScheduler){
			sbiModule_restServices.get("scheduleree","listAllJobs")
			.then(function(response) {
				sbiModule_logger.log("[LOAD START]: Loading of Shcedulers is started.");
				angular.copy(response.data.root,$scope.schedulationList);
				for(var jobIndex = $scope.schedulationList.length - 1; jobIndex >= 0; jobIndex--){
					var job = $scope.schedulationList[jobIndex];

					if(job.jobGroup != "BIObjectExecutions"){
						// discard job if group is not BIObjectExecutions
						$scope.schedulationList.splice(jobIndex, 1);
						continue;
					}
				}

				angular.copy($scope.schedulationList,$scope.schedulationListInitial);
				sbiModule_logger.log("[LOAD END]: Loading of Shcedulers is finished.");
			},function(response){

				toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

			});
		}


	}

	$scope.loadSchedulationsForMerge = function(scheduler, collate){
		sbiModule_restServices.promiseGet("2.0/pdf",scheduler,"collate="+collate)
		.then(function(response) {
			sbiModule_logger.log("[LOAD START]: Loading of Shcedulations for selected scheduler is started.");
			angular.copy(response.data.schedulations,$scope.schedulationListForMerge);
			$scope.snapshotUrlPath=response.data.urlPath;
			$scope.mergeAndNotPDF = response.data.mergeAndNotPDF;

			sbiModule_logger.log("[LOAD END]: Loading of Shcedulations for selected scheduler is finished.");
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

		});
	}

	$scope.convertTimestampToDate = function(){
		for (var i = 0; i < $scope.recentDocumentsInitial.length; i++) {
			var timestamp = $scope.recentDocumentsInitial[i].requestTime;
			var date = new Date(timestamp);
			var dateString = date.toLocaleString();
			$scope.recentDocumentsInitial[i].requestTime = dateString;
		}
	}

	$scope.returnToDocument = function() {
		$scope.showDocSchedJsp = false;
	}

	$scope.closeFilter = function(){
		$mdDialog.cancel();
	}

	$scope.downloadSnapshotSpeedMenuOption =
		[

			 {
				 label: sbiModule_translate.load("sbi.generic.download"),
				 icon:"fa fa-download",
				 color:'#222222',
				 action : function(item) {

					var queryParams = "";

					 if($scope.mergePdfsInto1) {
						 for(var i=0; i<item.ids.length;i++){
							 queryParams=queryParams+"&mergeitems="+item.ids[i];
						 }
					 } else {
						 queryParams="&OBJECT_ID=" + item.biobjId;
					 }
					 $scope.snapshotUrl=$scope.snapshotUrlPath+
					   "&ACTION_NAME=GET_SNAPSHOT_CONTENT"+"&SNAPSHOT_ID=" + item.id+"&LIGHT_NAVIGATOR_DISABLED=TRUE"+queryParams;
					 $mdDialog.show({
							templateUrl: 'dialog1.tmpl.html',
							scope:$scope,
							preserveScope: true,
							//targetEvent:item,
							clickOutsideToClose:true
						})
				 }
			 }
	 ];

	$scope.getDocumentsSnapshots = function(scheduler, doc) {
		sbiModule_restServices.promiseGet("2.0/workspace/scheduler", doc)
		.then(function(response) {
			sbiModule_restServices.get( "1.0/documentsnapshot", "getSnapshotsForSchedulationAndDocument?id="+response.data+"&scheduler="+encodeURIComponent(scheduler))
			.success(function(data, status, headers, config) {
				sbiModule_logger.log('data scheduler '  ,  data.schedulers);
				$scope.schedulers = data.schedulers;
				sbiModule_logger.log('url path ' + data.urlPath);
				$scope.snapshotUrlPath=data.urlPath;
			})
			.error(function(data, status, headers, config) {});
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	};

	$scope.openSchedulersDocumentsAndSnapshots = function(doc) {
		$scope.mergePdfsInto1 = doc.jobMergeAllSnapshots;
		$scope.scheduler = doc;
		$scope.showDocSchedJsp = true;
		if($scope.mergePdfsInto1) {
			$scope.loadSchedulationsForMerge(doc.jobName, doc.jobCollateSnapshots);
		}
	}

	$scope.schedulatinColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.glossary.description"), "name":"description"},
	    {"label":$scope.translate.load("sbi.timespan.type.dateTime"), "name":"time"},
	    //{"label":$scope.translate.load("sbi.timespan.type.dateTime"), "name":"dateCreation"} //, transformer : function(data){ return $filter('date')(data, "yyyy-MM-dd HH:mm:ss");  }}
    ];

	$scope.schedulatinMergeColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.timespan.type.dateTime"), "name":"time"}
    ];

	$scope.triggerExecute = function(doc, merge, document){
		$scope.mergePdfsInto1 = merge;
		var requestString =
			"executeTrigger?jobName="+doc.jobName
			+"&jobGroup="+doc.jobGroup
			+"&triggerName="+doc.triggerName
			+"&triggerGroup="+doc.triggerGroup;

		sbiModule_restServices.promisePost("scheduleree",requestString)
		.then(function(response) {
			$scope.processing = true;
			$timeout(function(){$scope.triggerExecuteTimeOut(doc, merge, document)}, 10000);

			sbiModule_logger.log("[LOAD END]: Execution of schedulation is finished.");
		},function(response){

			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

		});

	}

	$scope.triggerExecuteTimeOut = function(doc, merge, document){
		$scope.processing = false;
		sbiModule_logger.log("[LOAD START]: Execution of schedulation is started.");
		if(!merge){
			$mdDialog.show(
					$mdDialog.alert()
				        .parent(angular.element(document.body))
				        .clickOutsideToClose(false)
				        .title(sbiModule_translate.load("sbi.generic.ok"))
				        .content(sbiModule_translate.load("sbi.scheduler.schedulation.executed"))
				        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
		} else {
			sbiModule_restServices.promiseGet("2.0/pdf",doc.jobName,"collate="+document.jobCollateSnapshots)

			.then(function(response) {
				sbiModule_logger.log("[LOAD START]: Loading of Shcedulations for selected scheduler is started.");
				//response.data.schedulations
				$scope.snapshotUrlPath=response.data.urlPath;
				var itemsSorted  = $filter('orderBy')(response.data.schedulations, 'time');
				$scope.openPdf(itemsSorted[itemsSorted.length-1]);
				sbiModule_logger.log("[LOAD END]: Loading of Shcedulations for selected scheduler is finished.");
			},function(response){

				// Take the toaster duration set inside the main controller of the Workspace. (danristo)
				toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

			});
		}

		sbiModule_logger.log("[LOAD END]: Execution of schedulation is finished.");
	}

	 $scope.openPdf = function(item) {

			var queryParams = "";

			 if($scope.mergePdfsInto1) {
				 for(var i=0; i<item.ids.length;i++){
					 queryParams=queryParams+"&mergeitems="+item.ids[i];
				 }
			 } else {
				 queryParams="&OBJECT_ID=" + item.biobjId;
			 }
			 $scope.snapshotUrl=$scope.snapshotUrlPath+
			   "&ACTION_NAME=GET_SNAPSHOT_CONTENT"+"&SNAPSHOT_ID=" + item.id+"&LIGHT_NAVIGATOR_DISABLED=TRUE"+queryParams;
			 $mdDialog.show({
					templateUrl: 'dialog1.tmpl.html',
					scope:$scope,
					preserveScope: true,
					//targetEvent:item,
					clickOutsideToClose:true
				})
		 }


		if(initialOptionMainMenu){
			if(initialOptionMainMenu.toLowerCase() == 'schedulation'){
				var selectedMenu = $scope.getMenuFromName('schedulation');
				$scope.leftMenuItemPicked(selectedMenu,true);
			}
		}


}
})();