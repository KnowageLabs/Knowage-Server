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

(function() {
	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
	
angular.module('federation_view', ['ngMaterial'])
.directive('federationView', function() {
	return {
		 templateUrl: currentScriptPath + 'federation-view.html',
		controller: federationViewControllerFunction,
		 priority: 10,
		scope: {
			ngModel:"=",
			showGridView:"=?",
			tableSpeedMenuOption:"=?",
			selectedFederation:"=?",
			selectFederationAction:"&",
			showFederationAction:"&",
			editEnabled:"=?",
			editFederationAction:"&",
			//cloneDocumentAction:"&",
			deleteEnabled:"=?",
			deleteFederationAction:"&",
			//executeDocumentAction:"&",
			orderingFederationCards:"=?",
			showBusinessModels:"=?",
			
			/**
			 * This property will provide the information which columns the federation view directive should expect, since they
			 * are different for Business Models and Federation Models.
			 * @author Ana Tomic (atomic, ana.tomic@mht.net)
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			tableColumns:"=?"
		},
		
		link: function (scope, elem, attrs) { 
			
			 elem.css("position","static");
			 
			 if(!attrs.tableSpeedMenuOption){
				 scope.tableSpeedMenuOption=[];
			 }
			
//			if(document.getElementsByTagName("body")[0].id == "workspaceWebPageBody")  {
//			    elem.css("position","static");
//			 }
//			 else {
//			    elem.css("position","relative")
//			 }
			
		}
	}
});

function federationViewControllerFunction($scope,sbiModule_user,sbiModule_translate){
	$scope.clickFederation=function(item){
		
		 $scope.selectFederationAction({federation: item});
	}
	$scope.sbiUser = sbiModule_user;
	$scope.translate=sbiModule_translate;
	
	$scope.isAbletoDelete = function(federation){
		return $scope.sbiUser.isTechnicalUser == "true"|| $scope.sbiUser.userId==federation.owner;
	}
};
})();