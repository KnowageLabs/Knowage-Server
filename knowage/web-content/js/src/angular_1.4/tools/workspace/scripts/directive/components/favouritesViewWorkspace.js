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

function favouritesController($scope,sbiModule_restServices,sbiModule_translate,sbiModule_user,$documentViewer,$mdDialog){

	$scope.loadFavoriteDocumentExecutionsForUser =function(){
		sbiModule_restServices.promiseGet("2.0/favorites","")
		.then(function(response) {
			angular.copy(response.data,$scope.favoriteDocumentsList);
			$scope.favoriteDocumentsInitial = $scope.favoriteDocumentsList;
			console.info("[LOAD END]: Loading of Favorites documents is finished.");
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
		});
	}

	$scope.deleteFavoriteDocumentExecutionById = function(doc) {
		var confirm = $mdDialog.confirm()
		.title(sbiModule_translate.load("sbi.workspace.favorites.delete.confirm.dialog"))
		.content(sbiModule_translate.load("sbi.workspace.favorites.delete.confirm.delete"))
		.ariaLabel('delete Document') 
		.ok(sbiModule_translate.load("sbi.general.yes"))
		.cancel(sbiModule_translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
				sbiModule_restServices.promiseDelete("2.0/favorites",doc.id)
				.then(function(response) {
					$scope.loadFavoriteDocumentExecutionsForUser();
					$scope.selectDocument(undefined);
				},function(response) {
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load('sbi.browser.folder.load.error'));
				});
		});
	}
	
	$scope.favoriteSpeedMenu=[{
		label : sbiModule_translate.load('sbi.generic.run'),
		icon:'fa fa-play-circle' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.executeFavorite(item);
		}
	} ];
	
	$scope.executeFavorite = function(document) {	
		$documentViewer.openDocument(document.objId, document.documentLabel, document.documentName);
	}

}
