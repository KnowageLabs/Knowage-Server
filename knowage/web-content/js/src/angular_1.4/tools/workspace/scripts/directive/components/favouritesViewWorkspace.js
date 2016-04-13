angular
	.module('favourites_view_workspace', [])

	/**
	 * The HTML content of the Favorites view (favorite documents).
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
	.directive('favoritesViewWorkspace', function () {		
		 return {			 
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/templates/favoritesViewWorkspace.html',
		      controller: favouritesController
		  };	  
	})

function favouritesController($scope){
	
}