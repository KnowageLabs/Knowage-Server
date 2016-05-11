angular
	.module('favourites_view_workspace', [])

	/**
	 * The HTML content of the Favorites view (favorite documents).
	 * @author Nikola Simović (nsimovic, nikola.simovic@mht.net)
	 */
	.directive('favoritesViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/favoritesViewWorkspace.html',
		      controller: favouritesController
		  };
	})

function favouritesController($scope,sbiModule_restServices,sbiModule_translate){
	
	$scope.selectedDocument = undefined;
	$scope.showDocumentInfo = false;

	$scope.loadFavoriteDocumentsForUser =function(){
		sbiModule_restServices.promiseGet("2.0/analyticalmodel/getmyrememberme", "biadmin")
		.then(function(response) {
			angular.copy(response.data,$scope.favoriteDocumetnsList);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}
	$scope.loadFavoriteDocumentsForUser();
	
}
