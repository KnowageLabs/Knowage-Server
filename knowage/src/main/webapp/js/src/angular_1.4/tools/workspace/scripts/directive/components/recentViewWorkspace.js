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

angular
	.module('recent_view_workspace', [])

	.directive('recentViewWorkspace', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: currentScriptPath + '../../../templates/recentViewWorkspace.html',
		      controller: recentController
		  };
	});

function recentController($scope, sbiModule_restServices, sbiModule_translate, $documentViewer, toastr, sbiModule_i18n){

	$scope.translate=sbiModule_translate;
	$scope.i18n = sbiModule_i18n;

	$scope.loadRecentDocumentExecutionsForUser = function(){
		sbiModule_restServices.promiseGet("2.0/recents","")
		.then(function(response) {
			console.info("[LOAD START]: Loading of Recent documents is started.");

			$scope.i18n.loadI18nMap().then(function() {

				angular.copy(response.data,$scope.recentDocumentsList);

				for (var i = 0 ; i < $scope.recentDocumentsList.length; i ++ ){
					$scope.recentDocumentsList[i].documentName = $scope.i18n.getI18n($scope.recentDocumentsList[i].documentName);
					$scope.recentDocumentsList[i].documentLabel = $scope.i18n.getI18n($scope.recentDocumentsList[i].documentLabel);
				}

				$scope.recentDocumentsInitial = $scope.recentDocumentsList;
				//$scope.convertTimestampToDate();
				console.info("[LOAD END]: Loading of Recent documents is finished.");

			}); // end of load I 18n
		},function(response){

			// Take the toaster duration set inside the main controller of the Workspace. (danristo)
			toastr.error(response.data, sbiModule_translate.load('sbi.browser.folder.load.error'), $scope.toasterConfig);

		});
	}

	$scope.convertTimestampToDate = function(){
		for (var i = 0; i < $scope.recentDocumentsInitial.length; i++) {
			var timestamp = $scope.recentDocumentsInitial[i].requestTime;
			var date = new Date(timestamp);
			var dateString = date.toLocaleString();
			$scope.recentDocumentsInitial[i].requestTime = dateString;
		}
	}

	$scope.loadRecentDocumentExecutionsForUser();

	$scope.recentSpeedMenu=[{
		label : sbiModule_translate.load('sbi.generic.run'),
		icon:'fa fa-play-circle' ,
		backgroundColor:'transparent',
		action : function(item,event) {
			$scope.executeRecent(item);
		}
	} ];

	$scope.executeRecent = function(document) {

		$documentViewer.openDocument(document.objId, document.documentLabel, document.documentName, $scope);

		/**
		 * After opening (executing) a document listen for the 'documentClosed' event that will be fired from the 'documentViewer.js', i.e. the controller that the
		 * 'openDocument' function is referring to. The event will be fired when user closes an executed document. This information will be used to re-call the GET
		 * method towards the REST service that collects the last (recently) executed documents. This way the Workspace's RECENT view will be up-to-date.
		 *
		 * Also, load all folders and their content (document) for the DOCUMENTS (Organizer) view, because user could add an executed document to the Workspace.
		 *
		 * This calling of the 'hideRightSidePanel' function is necessary, since user can potentially modify the document (e.g. a cockpit document) that is executed
		 * from the Recent (e.g. document's label that is shown in the detail panel could be changed).
		 *
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		$scope.$on("documentClosed", function() { $scope.loadRecentDocumentExecutionsForUser(); $scope.loadAllFolders(); $scope.hideRightSidePanel(); });

	}

}
})();