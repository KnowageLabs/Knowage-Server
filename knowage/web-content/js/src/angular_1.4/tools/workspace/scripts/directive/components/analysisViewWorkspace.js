/**
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

/**
 * Controller for Analysis view of the Workspace.
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

angular
	.module('analysis_view_workspace', [])
	/**
	 * The HTML content of the Analysis view (analysis documents).
	 */
	.directive('analysisViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/analysisViewWorkspace.html',
		      controller: analysisController
		  };	  
	})
	.filter("asDate", function () {
		
	    return function (input) {
	        return new Date(input);
	    }
	    
	});

function analysisController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_config,sbiModule_user,$mdDialog,$mdSidenav,$documentViewer,$qbeViewer) {
	
	$scope.cockpitAnalysisDocsInitial = [];	
	$scope.activeTabAnalysis = null;	
	$scope.translate = sbiModule_translate;
	
	$scope.loadAllMyAnalysisDocuments = function() {
		
		sbiModule_restServices
			.promiseGet("documents", "myAnalysisDocsList")
			.then(
					function(response) {
						
						/**
						 * TODO: Provide a comment
						 */
						angular.copy(response.data.root,$scope.allAnalysisDocs);
						
						var tempDocumentType = "";
						
						/**
						 * TODO: Provide a comment
						 */
						for(var i=0; i<$scope.allAnalysisDocs.length; i++) {
							
							tempDocumentType = $scope.allAnalysisDocs[i].typeCode;							
							
							switch(tempDocumentType.toUpperCase()) {	
																
								case "DOCUMENT_COMPOSITE": 
									$scope.cockpitAnalysisDocs.push($scope.allAnalysisDocs[i]); 
									break;	
									
							}
							
						}
						
						angular.copy($scope.cockpitAnalysisDocs,$scope.cockpitAnalysisDocsInitial);
						
						console.info("[LOAD END]: Loading of Analysis Cockpit documents is finished.");
					},
					
					function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
					}
				);
	}
	
	/**
	 * If we are coming to the Workspace interface (web page) from the interface for the creation of the new Cockpit document, reload all Analysis documents 
	 * (Cockpit documents), so we can see the changes for the option from which we started a creation of a new documents (Analysis option).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	if (whereAreWeComingFrom == "NewCockpit") {
		$scope.loadAllMyAnalysisDocuments();
		// Do not load Analysis documents again when clicking on its option after returning back to the Workspace.
		$scope.analysisDocumentsLoaded = true;
		whereAreWeComingFrom == null;
	}
	
	/**
	 * Clone a particular Analysis document.
	 * @commentBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.cloneAnalysisDocument = function(document) {		
		
		console.info("[CLONE START]: The cloning of a selected '" + document.label + "' has started.");	
		
		var confirm = $mdDialog
						.confirm()
						.title($scope.translate.load("sbi.browser.document.clone.ask.title"))
						.content($scope.translate.load("sbi.browser.document.clone.ask"))
						.ariaLabel('delete Document') 
						.ok($scope.translate.load("sbi.general.yes"))
						.cancel($scope.translate.load("sbi.general.No"));
			
		$mdDialog
			.show(confirm)
			.then(				
					function() {
		
					sbiModule_restServices
						.promisePost("documents","clone?docId="+document.id)
						.then(
								function(response) {
									
									if (document.typeCode == "DOCUMENT_COMPOSITE") { 
										$scope.cockpitAnalysisDocsInitial.push(response.data);
										$scope.cockpitAnalysisDocs.push(response.data);
									}
									
									console.info("[CLONE END]: The cloning of a selected '" + document.label + "' went successfully.");	
								},
								
								function(response) {
									sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.clone.error'));
								}
							);
					}
			);			
	}
	
	/**
	 * Delete particular Analysis document from the Workspace.
	 */
	$scope.deleteAnalysisDocument = function(document) {
				
		console.info("[DELETE START]: Delete of Analysis Cockpit document with the label '" + document.label + "' is started.");
		
		var confirm = $mdDialog
						.confirm()
						.title(sbiModule_translate.load("sbi.browser.document.delete.ask.title"))
						.content(sbiModule_translate.load("sbi.browser.document.delete.ask"))
						.ariaLabel('delete Document') 
						.ok(sbiModule_translate.load("sbi.general.yes"))
						.cancel(sbiModule_translate.load("sbi.general.No"));
		
		$mdDialog
			.show(confirm)
			.then(
					function() {
						
						var indexInCockpit = $scope.cockpitAnalysisDocs.indexOf(document);
						var indexInCockpitInitial = $scope.cockpitAnalysisDocsInitial.indexOf(document);
						var isDocInCockpit = indexInCockpit >= 0;
						
						sbiModule_restServices
							.promiseDelete("1.0/documents", document.label)
							.then(
									function(response) {
									
										(isDocInCockpit && indexInCockpitInitial) ? $scope.cockpitAnalysisDocs.splice(indexInCockpit,1) : null;
										(isDocInCockpit && indexInCockpitInitial) ? $scope.cockpitAnalysisDocsInitial.splice(indexInCockpitInitial,1) : null;
										
										$scope.selectedDocument = undefined;	// TODO: Create and define the role of this property
										console.info("[DELETE END]: Delete of Analysis Cockpit document with the label '" + document.label + "' is done successfully.");
									},
								
									function(response) {
										sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.document.delete.error'));
									}
								);
					}
				);
	
	}
	
	/**
	 * TODO:
	 * Create a new Cockpit document.
	 */
	$scope.addNewAnalysisDocument = function() {
		console.info("[NEW COCKPIT - START]: Open page for adding a new Cockpit document.");
		window.location.href = sbiModule_config.engineUrls.cockpitServiceUrl + '&SBI_ENVIRONMENT=WORKSPACE&IS_TECHNICAL_USER=' + sbiModule_user.isTechnicalUser + "&documentMode=EDIT";
	}
	
	/**
	 * The immediate Run (preview) button functionality for the Analysis documents (for List view of documents). 
	 */
	$scope.analysisSpeedMenu = 
	[
	 	{
	 		label: sbiModule_translate.load('sbi.generic.run'),
	 		icon:'fa fa-play-circle' ,
	 		backgroundColor:'transparent',
	 		
	 		action: function(item,event) {
	 			$scope.executeDocument(item);
	 		}
	 	} 
 	];
	
}