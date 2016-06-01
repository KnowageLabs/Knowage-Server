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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 * @author Ana Tomic (atomic, ana.tomic@mht.net)
 */

angular
	.module('qbe_viewer', [ 'ngMaterial' ,'sbiModule'])
	.service('$qbeViewer', function($mdDialog,sbiModule_config,sbiModule_restServices) { 
	 		
		this.openQbeInterface = function($scope,url) {
			
			$mdDialog
				.show
				(	
					{   
						scope:$scope,
						preserveScope: true,
						controller: openQbeInterfaceController,
						templateUrl: '/knowage/js/src/angular_1.4/tools/workspace/scripts/services/qbeViewerTemplate.html',
						fullscreen: true,
						locals:{url:url}				
					}
				) 
				.then(function() {});
		
		};
		
		function openQbeInterfaceController($scope,url) {
			
			$scope.documentViewerUrl = url;
			
			$scope.closeDocument = function() {
				
				$mdDialog.hide();
				
				if ($scope.datasetSavedFromQbe==true) {
					//alert("RELOAD DATASETS");
					$scope.currentOptionMainMenu=="datasets" ? $scope.reloadMyData() : $scope.reloadMyData = true;
					$scope.datasetSavedFromQbe = false;
				}
				
			}
			
			$scope.saveQbeDocument = function() {
				
				/** 
				 * Take the frame that keeps the QBE ExtJS page (inside the 'qbe' property - defined inside the qbe.jsp), so we can access functions
				 * inside the QbePanel.js (the page). We need 'openSaveDataSetWizard' function in order to save the dataset from the QBE.
				 */
				var frame = window.frames['documentViewerIframe'];
				frame.contentWindow.qbe.openSaveDataSetWizard('TRUE');				
				
				/**
				 * Catch the 'save' event that is fired when the DS is persisted (saved) after confirming the dataset wizard inside the QBE (as a 
				 * result of calling the 'openSaveDataSetWizard' function.	
				 */			 
				frame.contentWindow.qbe.on("save", function() {$scope.datasetSavedFromQbe = true;})
				
			}
	
		}
});