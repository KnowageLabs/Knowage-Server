/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */
var scripts = document.getElementsByTagName("script")
var currentScriptPath = scripts[scripts.length-1].src;

angular.module('expander-box', [ 'ngMaterial'])
.directive('expanderBox',function() {
	return {
		transclude : true,
		template:"<md-toolbar ng-click=\"toggle()\">" +
		"<span class=\"md-toolbar-tools\">" +
		"{{title}}" +
		"</span>" +
		"<i style=\"position:absolute;right:5px;top:0px;\"  ng-if=\"expanded\" class=\"fa fa-angle-up\"></i>	<i  style=\"position:absolute;right:5px;top:2px;\" ng-if=\"!expanded\" class=\"fa fa-angle-down\"></i>" +
		"</md-toolbar><md-content layout-padding  ng-show=\"expanded\"><div ng-transclude style='padding: 0;'></div></md-content> " 
		,
		controller : boxExpanderControllerFunction,
		scope : {
			id : "@",
			color:"@?",
			backgroundColor:"@?",
			title:"=",
			expanded:"=?"
		},
		link: function(scope, element, attrs, ctrl, transclude) {
			if(!attrs.color){
				scope.color="white"
			}
			
			if(!attrs.backgroundColor){
				scope.backgroundColor="blue"
			}
			
			if(!attrs.expanded){
				scope.expanded=false;
			}
			
			
			angular.element(element[0].querySelector("md-toolbar")).css("background-color",scope.backgroundColor);
			angular.element(element[0].querySelector("md-toolbar")).css("color",scope.color);
			angular.element(element[0].querySelector("md-content")).css("border","1px solid "+scope.backgroundColor);
			
		}
	}});

function boxExpanderControllerFunction($scope){
	
	$scope.toggle=function(){
		$scope.expanded=!$scope.expanded;
	}
}