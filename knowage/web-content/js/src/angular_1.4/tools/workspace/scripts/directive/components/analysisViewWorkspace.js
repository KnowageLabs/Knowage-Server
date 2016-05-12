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

function analysisController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_config,$mdDialog,$mdSidenav,$documentViewer) {
		
	$scope.activeTabAnalysis = null;
	
	$scope.loadAllMyAnalysisDocuments = function() {
		
		sbiModule_restServices
			.promiseGet("documents", "myAnalysisDocsList")
			.then(
					function(response) {
						
						/**
						 * Take all Analysis documents (cockpit, geo and ad hoc) and keep them in signle
						 * array, which serves for displaying all of them.
						 */
						angular.copy(response.data.root,$scope.allAnalysisDocs);
						
						var tempDocumentType = "";
						
						/**
						 * Additional three arrays for another three criteria (except the one that hold all 
						 * analysis files - category ALL): COCKPIT, GEO and AD HOC. These are going to hold
						 * data about all files that belong to each of those three categories.
						 */
						for(var i=0; i<$scope.allAnalysisDocs.length; i++) {
							
							tempDocumentType = $scope.allAnalysisDocs[i].typeCode;							
							
							switch(tempDocumentType.toUpperCase()) {	
							
								/**
								 * KNOWAGE-859: Remove AD HOC reports option (tab).
								 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
								 */
								case "WORKSHEET": 
//									$scope.adhocReportAnalysisDocs.push($scope.allAnalysisDocs[i]); 
									/**
									 * Remove all AD HOC reports available in analysis documents from an
									 * array of all documents for Analysis perspective.
									 */
									$scope.allAnalysisDocs.splice(i,1);
									break;
									
								case "MAP": 
									$scope.geoAnalysisDocs.push($scope.allAnalysisDocs[i]); 
									break;
									
								case "DOCUMENT_COMPOSITE": 
									$scope.cockpitAnalysisDocs.push($scope.allAnalysisDocs[i]); 
									break;	
									
							}
							
						}
						
						$scope.allAnalysisDocsInitial = $scope.allAnalysisDocs;
					},
					
					function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
					}
				);
	}

	$scope.loadAllMyAnalysisDocuments();
	
	/**
	 * Set the currently active tab of the Analysis perspective in order to
	 * enable managing of visibility of "Add analysis document" button. This
	 * button should not be visible if the user is seeing all the documents.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	$scope.setActiveTabState = function(item) {		
		$scope.activeTabAnalysis = item.toUpperCase();
	};
	
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
									$scope.allAnalysisDocs.push(response.data);
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
				
		console.info("[DELETE START]: Delete of Analysis document with the label '" + document.label + "' is started.");
		
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
						
						var index = $scope.allAnalysisDocs.indexOf(document);
					
						sbiModule_restServices
							.promiseDelete("1.0/documents", document.label)
							.then(
									function(response) {
										$scope.allAnalysisDocs.splice(index,1);
										$scope.selectedDocument = undefined;	// TODO: Create and define the role of this property
										console.info("[DELETE END]: Delete of Analysis document with the label '" + document.label + "' is done successfully.");
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
	 * Create a new Analysis document.
	 */
	$scope.addNewAnalysisDocument = function() {
		alert("This button will CREATE NEW s analysis document");
	}
	
}