/**
 * Knowage, Open Source Business Intelligence suite
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
//(function() {
//	var scripts = document.getElementsByTagName("script");
//	var currentScriptPath = scripts[scripts.length - 1].src;
//	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);	
//	
//	angular.module('data_table', [])
//	      .directive('dataTable', function() {
//		return {
//			restrict: 'E',
//		    replace: 'true',
//	      	template:'<p>Hello</p>'
//			//templateUrl: currentScriptPath + 'data-preview-table.html',
//			//controller: dataPreviewControllerFunction,
//			//priority: 1000,
//			
//			//scope: {
//				//ngModel:"="
//			//},
//			
////			link: function (scope, elem, attrs) { 
////				
////				elem.css("position","relative")
////			
////				
////			}
//		};
//	});
//
////	function  dataPreviewControllerFunction($scope){
////               
////		console.log("in data table directive!!!!!!!!!!");
////	}
//
//})();

(
	function() {
		
		var scripts = document.getElementsByTagName("script");
		var currentScriptPath = scripts[scripts.length - 1].src;
		currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);
		
		var rrr = angular
			.module('chart_table', []);
		
			rrr.directive('chartTable', function ($compile) {
				
				 return {		
					  restrict: 'E',
				      replace: 'true',
				      templateUrl: currentScriptPath + 'data-preview-table.html',
				      controller: chartTableController,
				      transclude: true,
				      scope:{
				    	  ngModel:"=?",
				    	  title:"=?",
				    	  categories:"=?"
				      },
				      compile: function (tElement, tAttrs, transclude) {
			                return {
			                    pre: function preLink(scope, element, attrs, ctrl, transclud) { 
			                    	
			                    },
			                    post: function postLink(scope, element, attrs, ctrl, transclud) { 
			                    }
			                };
			                    }

				  };	  
			});
		
		
		
		function chartTableController($scope) {
		      
		}
	}	
)();