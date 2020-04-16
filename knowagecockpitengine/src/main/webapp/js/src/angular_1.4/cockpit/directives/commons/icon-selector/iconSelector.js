/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	angular
		.module('cockpitModule')
		.directive('iconSelector',function(){
			return{
				template: '<md-button class="md-raised md-button-empty kn-primaryButton" ng-click="chooseMenuIcon($event)">'+
						  '		<span>{{translate.load("sbi.menu.chooseIcon")}}</span>'+
						  '		<i ng-if="preview" class="icon" ng-class="selectedIcon" style="style"></i>'+
						  '</md-button>',
				controller: iconSelectorDirectiveController,
				scope: {
					selectedIcon: '=',
					iconStyle: '=?',
					preview: '@?'
				}
			}
		})
	function iconSelectorDirectiveController(
			$scope,
			$mdDialog,
			sbiModule_translate,
			knModule_fontIconsService){
		
		$scope.translate = sbiModule_translate;
		
		$scope.chooseMenuIcon = function(e){
			
			$mdDialog.show({ 
				templateUrl: baseScriptPath + '/directives/commons/icon-selector/templates/iconSelector.html',
				clickOutsideToClose:false,
				escapeToClose :false,
				preserveScope: false,
				skipHide: true,
				autoWrap: false,
				controller: function (scope, $mdDialog, knModule_fontIconsService, sbiModule_translate) {
					scope.translate = sbiModule_translate;
					scope.availableIcons = [];
					angular.copy(knModule_fontIconsService.icons, scope.availableIcons);

					scope.searchVal = "";
					
					scope.setIcon = function(family,icon) {
						scope.selectedIcon = icon;
					}
					
					scope.chooseIcon = function(){					
						$mdDialog.hide(scope.selectedIcon.className);
					}
					
					scope.cancel = function(){
						$mdDialog.cancel();
					}			
				}
				
			}).then(function(icon) {
				if (icon) {
					$scope.selectedIcon = icon;
				}
			}, function() {});
		}
		
	};
		
	
})()