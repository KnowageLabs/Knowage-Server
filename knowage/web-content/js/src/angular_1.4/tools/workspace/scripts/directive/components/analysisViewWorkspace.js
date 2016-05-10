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
	});

function analysisController($scope,sbiModule_restServices,sbiModule_translate){
	
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
	
}