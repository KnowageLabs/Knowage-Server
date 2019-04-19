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

angular.module('schedulation_view', ['ngMaterial'])
.directive('schedulationView', function() {
	return {
		templateUrl: currentScriptPath + 'schedulation-view.html',
		controller: recentViewControllerFunction,
		priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			showAddToOrganizer:"=?",
			selectedRow:"=?",
			tableSpeedMenuOption:"=?",
			selectedDocument:"=?",
			selectDocumentAction:"&",
			deleteDocumentAction:"&",
			previewDocumentAction: "&",
			executeDocumentAction:"&",
			addToOrganizerAction:"&",
			orderingDocumentCards:"=?"
		},
		link: function (scope, elem, attrs) {
			elem.css("position","relative")
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
		}
	}
});

function recentViewControllerFunction($scope,sbiModule_translate, sbiModule_config, sbiModule_user, $mdDialog, sbiModule_restServices, sbiModule_logger){

	$scope.sbiModule_config = sbiModule_config;
	$scope.clickDocument=function(item){

		 $scope.selectDocumentAction({doc: item});
	}

	$scope.translate=sbiModule_translate;

    $scope.showSnapshots= (sbiModule_user.functionalities.indexOf("SeeSnapshotsFunctionality")>-1)? true:false;
    $scope.runSnapshots= (sbiModule_user.functionalities.indexOf("RunSnapshotsFunctionality")>-1)? true:false;

	$scope.schedulationColumns = [
	    {"label":$scope.translate.load("sbi.schedulation.jobName"),"name":"jobName"},
	    {"label":$scope.translate.load("sbi.schedulation.jobDesc"), "name":"jobDescription"}
    ];

	$scope.selectedTriggers = [];

	$scope.toggleTriggerRun = function(trigger){
		var index = $scope.selectedTriggers.indexOf(trigger);
		if(index==-1){
			$scope.selectedTriggers.push(trigger);
		}else{
			$scope.selectedTriggers.splice(index,1);
		}
	};

	$scope.runTriggers = function(trigger){

		var requestString ="executeMultipleTrigger";

		var itemsList = new Array();

		if(trigger == 'all'){
			for(var i=0; i<$scope.selectedTriggers.length; i++){
				var item = {};
				var doc = $scope.selectedTriggers[i];
				item.jobName = doc.jobName;
				item.jobGroup = doc.jobGroup;
				item.triggerName = doc.triggerName;
				item.triggerGroup = doc.triggerGroup;
				itemsList.push(item);
			}

		}else{
			var item = {};
			item.jobName = trigger.jobName;
			item.jobGroup = trigger.jobGroup;
			item.triggerName = trigger.triggerName;
			item.triggerGroup = trigger.triggerGroup;
			itemsList.push(item);
		}

		sbiModule_restServices.promisePost("scheduleree",requestString,JSON.stringify(itemsList))
		.then(function(response) {
			$mdDialog.show(
					$mdDialog.alert()
				        .clickOutsideToClose(false)
				        .title(sbiModule_translate.load("sbi.generic.ok"))
				        .content(sbiModule_translate.load("sbi.scheduler.schedulation.executed"))
				        .ok(sbiModule_translate.load("sbi.general.ok"))
				);
			sbiModule_logger.log("[LOAD END]: Execution of schedulation is finished.");
		},function(response){

			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

		});


	}

	$scope.onlySnapshots = function (item) {
		var schedulationTypes = {snapshot:"saveassnapshot", file:"saveasfile",document:"saveasdocument", mail:"sendmail", jclass:"saveasclass"};
		var value = item.jobParameters[0].value;
		if(
				value.indexOf(schedulationTypes.snapshot)!=-1
				&&
				value.indexOf(schedulationTypes.file)==-1
				&&
				value.indexOf(schedulationTypes.document)==-1
				&&
				value.indexOf(schedulationTypes.mail)==-1
				&&
				value.indexOf(schedulationTypes.jclass)==-1){
			return item;
		}
	}

	$scope.runMenu = {
		label : "Run",
		icon:'fa fa-play-circle' ,
		action : function(item,event) {
			$scope.previewDocumentAction({doc:item});
		}
	}

	$scope.getSchedulations = {
			label : sbiModule_translate.load('sbi.workspace.schedulations.view'),
			icon:'fa fa-eye' ,
			action : function(item,event) {
				$scope.previewDocumentAction({doc:item});
			}
	}


	$scope.buttonMenu=[
		{
			label : "Run",
			icon:'fa fa-play-circle' ,
			action : function(item,event) {
				$scope.previewDocumentAction({doc:item});
			}
		},
		{
			label : sbiModule_translate.load('sbi.workspace.schedulations.view'),
			icon:'fa fa-eye' ,
			action : function(item,event) {
				$scope.previewDocumentAction({doc:item});
			}
	} ];


}
})();