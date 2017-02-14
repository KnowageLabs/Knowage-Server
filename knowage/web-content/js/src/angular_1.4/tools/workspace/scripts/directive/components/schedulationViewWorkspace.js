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
//		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/recentViewWorkspace.html',
		      templateUrl: currentScriptPath + '../../../templates/schedulationViewWorkspace.html',
		      controller: schedulationController
		  };
	});

function schedulationController($scope, sbiModule_messaging, $mdDialog, $httpParamSerializer, sbiModule_restServices, sbiModule_translate, sbiModule_config , $documentViewer, toastr){
	
	$scope.translate=sbiModule_translate;
	$scope.schedulationList = [];
	$scope.schedulationListForMerge = [];
		
	$scope.loadSchedulations = function(){
		sbiModule_restServices.promiseGet("scheduler/listAllJobs","")
		.then(function(response) {
			console.info("[LOAD START]: Loading of Recent documents is started.");
			angular.copy(response.data.root,$scope.schedulationList);
			//$scope.recentDocumentsInitial = $scope.recentDocumentsList;
			//$scope.convertTimestampToDate();
			console.info("[LOAD END]: Loading of Recent documents is finished.");
		},function(response){
			
			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);
			
		});
	}
	
	$scope.loadSchedulationsForMerge = function(scheduler){
		sbiModule_restServices.promiseGet("2.0/pdf",scheduler)
		.then(function(response) {
			console.info("[LOAD START]: Loading of Recent documents is started.");
			angular.copy(response.data,$scope.schedulationListForMerge);
			//$scope.recentDocumentsInitial = $scope.recentDocumentsList;
			//$scope.convertTimestampToDate();
			console.info("[LOAD END]: Loading of Recent documents is finished.");
		},function(response){
			
			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);
			
		});
	}
	
	$scope.mergePdfsInto1 = true;
	
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
	
	$scope.loadRecentDocumentExecutionsForUser();
		
	$scope.downloadSnapshotSpeedMenuOption = 
		[ 			 		               	
			 			 		               	
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.generic.download"),
				 icon:"fa fa-download",
				 color:'#222222',
				 action : function(item) { 
					
					 if($scope.mergePdfsInto1) {
						 sbiModule_restServices.promisePost("2.0/pdf","merge",item.ids)
							.then(function(response) {
								console.info("[LOAD START]: Loading of Recent documents is started.");
							
								console.info("[LOAD END]: Loading of Recent documents is finished.");
							},function(response){
								
								// Take the toaster duration set inside the main controller of the Workspace. (danristo)
								toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);
								
							});
					 } else {
						 $scope.snapshotUrl=$scope.snapshotUrlPath+
						   "&ACTION_NAME=GET_SNAPSHOT_CONTENT"+"&SNAPSHOT_ID=" + item.id+"&OBJECT_ID=" + item.biobjId+
						   "&LIGHT_NAVIGATOR_DISABLED=TRUE";
						 $mdDialog.show({
								templateUrl: 'dialog1.tmpl.html',
								scope:$scope,
								preserveScope: true,
								targetEvent:item,
								clickOutsideToClose:true
							})
					 }
					 
					 
					 					 
				 }	
			 }
	 ];
	
	$scope.getDocumentsSnapshots = function(scheduler, doc) {
		sbiModule_restServices.promiseGet("2.0/workspace/scheduler", doc)
		.then(function(response) {
			var queryParams = {
					id: response.data,
					scheduler: scheduler
			}
			sbiModule_restServices.get( "1.0/documentsnapshot", "getSnapshotsForSchedulationAndDocument", 
					$httpParamSerializer(queryParams))
			.success(function(data, status, headers, config) {	
				console.log('data scheduler '  ,  data.schedulers);
				$scope.schedulers = data.schedulers;
				console.log('url path ' + data.urlPath);
				$scope.snapshotUrlPath=data.urlPath;
			})
			.error(function(data, status, headers, config) {});	
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});																
	};
	
	$scope.openSchedulersDocumentsAndSnapshots = function(doc) {
		$scope.scheduler = doc;
		$scope.showDocSchedJsp = true;
		if($scope.mergePdfsInto1) {
			$scope.loadSchedulationsForMerge(doc.jobName);
		}
	}
	
	$scope.schedulatinColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.glossary.description"), "name":"description"}
    ];
	
	$scope.schedulatinMergeColumns = [
	    {"label":$scope.translate.load("sbi.generic.name"),"name":"name"},
	    {"label":$scope.translate.load("sbi.timespan.type.time"), "name":"time"}
    ];
	
	$scope.triggerExecute = function(doc){
		var requestString =
			"executeTrigger?jobName="+doc.jobName
			+"&jobGroup="+doc.jobGroup
			+"&triggerName="+doc.triggerName
			+"&triggerGroup="+doc.triggerGroup;
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
	
}
})();