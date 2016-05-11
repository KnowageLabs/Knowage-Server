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

function analysisController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_config,$mdDialog,$mdSidenav) {
	
	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;
	$scope.translate = sbiModule_translate;
	
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
						
					},
					
					function(response) {
						sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
					}
				);
	}

	$scope.loadAllMyAnalysisDocuments();
		
	/**
	 * TODO: 
	 * Preview (execute) a particular Analysis document.
	 */
	$scope.executeDocument = function(document) {
		
		console.info("[EXECUTION]: Execution of Analysis document with the label '" + document.label + "' is started.");
		
		window.location.href = 	sbiModule_config.adapterPath 
								+ "?ACTION_NAME=EXECUTE_DOCUMENT_ACTION&OBJECT_LABEL=" + document.label 
								+ "&OBJECT_ID=" + document.id 
								+ "&MYANALYSIS=TRUE";
		
	}
	
	/**
	 * TODO:
	 * Clone a particular Analysis document.
	 */
	$scope.cloneDocument = function(document) {		
		alert("This will fire a call for service that CLONES a document");		
	}
	
	/**
	 * Delete particular Analysis document from the Workspace.
	 */
	$scope.deleteDocument = function(document) {
				
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
	
	$scope.showDocumentDetails = function() {
		$scope.selectedDocument ? console.log($scope.selectedDocument) : null;
		return $scope.showDocumentInfo && $scope.isSelectedDocumentValid();
	};
	
	
	$scope.isSelectedDocumentValid = function() {
		return $scope.selectedDocument !== undefined;
	};
	
	$scope.setDocumentDetailOpen = function(isOpen) {
		
		if (isOpen && !$mdSidenav('rightDoc').isLockedOpen() && !$mdSidenav('rightDoc').isOpen()) {
			$scope.toggleDocumentDetail();
		}

		$scope.showDocumentInfo = isOpen;
	};
	
	$scope.toggleDocumentDetail = function() {
		$mdSidenav('rightDoc').toggle();
	};
	
	$scope.selectDocument= function ( document ) { 
		
		if (document !== undefined) {
			$scope.lastDocumentSelected = document;
		}
		
		var alreadySelected = (document !== undefined && $scope.selectedDocument === document);
		
		$scope.selectedDocument = document;
		
		if (alreadySelected) {
			$scope.selectedDocument=undefined;
			$scope.setDocumentDetailOpen(!$scope.showDocumentDetail);
		} else {
			$scope.setDocumentDetailOpen(document !== undefined);
		}
	};
	
}