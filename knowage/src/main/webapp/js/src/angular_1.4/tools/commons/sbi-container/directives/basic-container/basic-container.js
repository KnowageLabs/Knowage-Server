/*
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
	var scripts = document.getElementsByTagName('script')
	var componentTreePath = scripts[scripts.length-1].src;
	componentTreePath = componentTreePath.substring(0, componentTreePath.lastIndexOf('/') + 1);

angular.module('sbi-containerModule')



.directive('basicContainer',function(){
	return{
		
		 scope:{
			 title:'=directivetitle',
			 contentLayout:'=contentlayout',
			
		 },
		 transclude: true,
		 controller:function($scope,$rootScope){
		        $scope.refresh = function(){
		        	$scope.$broadcast('refresh');
		        }
		        },
		 restrict: 'E',
		 templateUrl:componentTreePath+'/template/basic-container.html',
		
		link:function(){
			console.log('Hello from main-container directive');
		}
	}
})

})();