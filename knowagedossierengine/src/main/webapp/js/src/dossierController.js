/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 
var app = angular.module("dossierModule",["ngMaterial","angular_table","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("dossierCTRL",dossierFunction);
dossierFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope", "$http","$mdDialog","$timeout","sbiModule_messaging","sbiModule_config", "$httpParamSerializer", "$window"];
function dossierFunction(sbiModule_translate, sbiModule_restServices, $scope, $http, $mdDialog,$timeout,sbiModule_messaging,sbiModule_config, $httpParamSerializer, $window){
	
	$scope.activitiesForDocument = [];
	
	$scope.translate = sbiModule_translate;
	
	$scope.dossierActivity = {};
	
	$scope.activitiesForDocumentColumns = [{"label":sbiModule_translate.load("sbi.dossier.activity.name"),"name":"activity"},{"label":sbiModule_translate.load("sbi.generic.dateIn"),"name":"creationDate"},{"label":sbiModule_translate.load("sbi.dossier.activity.partial"),"name":"partial","size":"40px"},{"label":sbiModule_translate.load("sbi.dossier.activity.total"),"name":"total","size":"40px"},{"label":sbiModule_translate.load("sbi.generic.state"),"name":"status"}];
	
	$scope.docId = documentId;
	
	$scope.jsonTemplate = JSON.parse(jsonTemplate);
	
	$scope.spagoBIResourceURL = "../.."+sbiModule_config.externalBasePath+"/restful-services/dossier";

	$scope.loadAllActivitesForDocument = function (documentId) {
		sbiModule_restServices.promiseGet($scope.spagoBIResourceURL+"/activities/"+documentId, "")
		.then(function(response) {
			$scope.activitiesForDocument = response.data;
			$scope.convertTimestampToDate();
			$timeout(function(){
				$scope.loadAllActivitesForDocument($scope.docId);
			}, 5000);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.error"));
		});
	}
	
	$scope.createNewActivity = function () {
		sbiModule_restServices.promisePost("../api/dossier/run?activityName="+$scope.dossierActivity.activity+"&documentId="+documentId, "",jsonTemplate)
		.then(function(response) {
			$scope.loadAllActivitesForDocument(documentId);
			sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.dossier.activity.save.success"), sbiModule_translate.load("sbi.generic.success"));
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.error"));
		});
	}
	
	$scope.loadAllActivitesForDocument(documentId);
	
	$scope.convertTimestampToDate = function(){
		for (var i = 0; i < $scope.activitiesForDocument.length; i++) {
			var timestamp = $scope.activitiesForDocument[i].creationDate;
			var date = new Date(timestamp);
			var dateString = date.toLocaleString();
			$scope.activitiesForDocument[i].creationDate = dateString;
		}	
	}
	
	$scope.storePPT = function (id, randomKey, activityName) {
		var link = sbiModule_restServices.getCompleteBaseUrl("../api/start/generatePPT?activityId="+id+"&randomKey="+randomKey+"&templateName="+$scope.jsonTemplate.PPT_TEMPLATE.name+"&activityName="+activityName);
		$window.location = link;
	}
	
	$scope.activitySpeedMenu = 
		[	 	
			//Downlaod the activity.
			{
				label: sbiModule_translate.load("sbi.generic.download"),
			 	icon:'fa fa-paperclip' ,
			 	backgroundColor:'transparent',
			
			 	action: function(item) {
			 		
			 		if(item.partial==item.total){
			 			if(item.binContent!=null){
			 				var link = sbiModule_restServices.getCompleteExternalBaseUrl("dossier/activity/"+item.id+"/pptx?activityName="+item.activity);
			 				$window.location = link;
			 			} else {
			 							
			 				
			 				sbiModule_restServices.promiseGet($scope.spagoBIResourceURL+"/random-key/"+item.progressId, "")
			 				.then(function(response) {
			 					$scope.storePPT(item.id, response.data, item.activity);
			 				}, function(response) {
			 					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.error"));
			 				});
			 				
			 			}
			 			
			 		}else if(item.status=="ERROR"){
			 			console.log("dowloading error file");
			 			
			 			if(item.binContent!=null){
			 				var link = sbiModule_restServices.getCompleteExternalBaseUrl("dossier/activity/"+item.id+"/txt?activityName="+item.activity);
			 				$window.location = link;
			 			}else{
			 				sbiModule_restServices.promiseGet($scope.spagoBIResourceURL+"/random-key/"+item.progressId, "")
			 				.then(function(response) {
			 					var link = sbiModule_restServices.getCompleteBaseUrl("../api/start/errorFile?activityId="+item.id+"&randomKey="+response.data+"&templateName="+$scope.jsonTemplate.PPT_TEMPLATE.name+"&activityName="+item.activity);
					 			$window.location = link;
			 				}, function(response) {
			 					sbiModule_messaging.showErrorMessage(response.data.errors[0].message, sbiModule_translate.load("sbi.generic.error"));
			 				});
			 			}
			 			
			 			
			 		} else {
			 			sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.dossier.activity.progress.not.finished"), sbiModule_translate.load("sbi.generic.info"));
			 		}
		 		}			
		 	},
		 	{
				label: sbiModule_translate.load("sbi.generic.delete"),
			 	icon:'fa fa-trash' ,
			 	backgroundColor:'transparent',
			
			 	action: function(item) {

				 	// TODO: translate
			    	var confirm = $mdDialog.confirm()
				         .title(sbiModule_translate.load("sbi.dossier.activity.delete"))          
				         .textContent(sbiModule_translate.load("sbi.dossier.activity.delete.confirm"))
				         .ariaLabel("Delete dossier activity")
				         .ok(sbiModule_translate.load("sbi.generic.yes"))
				         .cancel(sbiModule_translate.load("sbi.generic.no"));
					
					$mdDialog
						.show(confirm)
						.then(					
								function() {
									if(item.status=="DOWNLOAD"||item.status=="ERROR"){
										
										sbiModule_restServices.promiseDelete($scope.spagoBIResourceURL+"/activity/"+item.id, "")
										.then(function(response) {
											for (i=0; i<$scope.activitiesForDocument.length; i++) {
									 			
									 			if ($scope.activitiesForDocument[i].id == item.id) {
									 				$scope.activitiesForDocument.splice(i,1);
									 				break;
									 			}
									 		}
											sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.dossier.activity.delete.success"), sbiModule_translate.load("sbi.generic.success"));
										}, function(response) {
											sbiModule_messaging.showErrorMessage(response.data, sbiModule_translate.load("sbi.generic.error"));
										});
									
									}else{
										sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.dossier.activity.progress.not.finished"), sbiModule_translate.load("sbi.generic.error"));
									}
																		
						 		}
							);				 		
		 		}			
		 	}
		 ];
};



