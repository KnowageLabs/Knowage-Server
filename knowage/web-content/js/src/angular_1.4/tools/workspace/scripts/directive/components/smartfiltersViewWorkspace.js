angular
	.module('smartfilters_view_workspace', [])

	/**
	 * The HTML content of the smartfilters view (smartfilter documents).
	 * @author Ana Tomic (ana.tomic@mht.net)
	 */
	.directive('smartfiltersViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/smartfiltersViewWorkspace.html',
		      controller: smartfiltersController
		  };
	});
	
 function smartfiltersController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_user,$documentViewer){
	 
	   $scope.smartFiltersListInitial=[];
	   $scope.smartFilterEnabled=function(){
		   
		   return datasetParameters.IS_SMARTFILTER_ENABLED === "true";
		   
	   }  
	 
		$scope.loadSmartFilters= function(){
			params={};
			config={};
			params.inputType="SMART_FILTER";
			config.params=params;
			sbiModule_restServices.promiseGet("1.0/documents", "","",config)
			.then(function(response) {
				console.log(response.data);
				angular.copy(response.data,$scope.smartFiltersList);
				angular.copy($scope.smartFiltersList,$scope.smartFiltersListInitial);
				console.info("[LOAD END]: Loading of All smartfilters is finished.");
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
			});
		}
		
		$scope.filterSpeedMenu=[{
			label : sbiModule_translate.load('sbi.generic.run'),
			icon:'fa fa-play-circle' ,
			backgroundColor:'transparent',	
			action : function(item,event) {
				$scope.executeDocument(item);
			}
		} ];
		
    
}