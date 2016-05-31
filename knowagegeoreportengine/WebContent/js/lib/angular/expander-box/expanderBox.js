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
		template:"<md-toolbar layout=\"row\" ng-click=\"toggle()\">" +
		"<span class=\"md-toolbar-tools flex\" >" +
		"<span id=\"customToolbarContent\" layout=\"row\"></span>"+
		"{{title}}" +
		"</span>" +
		"<span id=\"customToolbarActionContent\" layout=\"row\"></span>"+
		"<md-button   class=\"md-icon-button\" aria-label=\"More\"><md-icon class=\"fa fa-chevron-{{expanded?'up':'down'}}\"></md-icon></md-button>"+
		"</md-toolbar><md-content class=\"animate-accordion\" layout-padding  ng-show=\"expanded\"><div ng-transclude style='padding: 0;'></div></md-content> " 
		,
		controller : boxExpanderControllerFunction,
		scope : {
			id : "@",
			color:"@?",
			backgroundColor:"@?",
			borderColor:"@?",
			toolbarClass:"@?",
			title:"=",
			locals:"=?",
			expanded:"=?"

		},
		link: function(scope, element, attrs, ctrl, transclude) {
			if(attrs.color){ 
				angular.element(element[0].querySelector("md-toolbar"))[0].style.setProperty("color",scope.color,"important")
			}
			
			if(!attrs.backgroundColor && !attrs.toolbarClass){
				angular.element(element[0].querySelector("md-toolbar"))[0].style.setProperty("background-color","#3b678c ","important")
				angular.element(element[0].querySelector("md-content"))[0].style.setProperty("border","1px solid #3b678c ","important");
			}else if(attrs.backgroundColor && !attrs.toolbarClass){
				angular.element(element[0].querySelector("md-toolbar"))[0].style.setProperty("background-color",scope.backgroundColor,"important")
				angular.element(element[0].querySelector("md-content"))[0].style.setProperty("border",scope.backgroundColor,"important"); 
			}
			
			if(attrs.borderColor){
				angular.element(element[0].querySelector("md-content"))[0].style.setProperty("border","1px solid "+scope.borderColor,"important");
			}
			
			if(attrs.toolbarClass){
				angular.element(element[0].querySelector("md-toolbar")).addClass(attrs.toolbarClass);
				if(!attrs.borderColor){
					angular.element(element[0].querySelector("md-content"))[0].style.setProperty("border","1px solid #3b678c ","important");
				}
			}
			
			if(!attrs.expanded){
				scope.expanded=false;
			}

			
		
			
			element.addClass("md-whiteframe-2dp");
			
		}
	}})

.directive('customToolbar',
		function($compile) {
	return {
		template:'',
		replace:true,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
				var contElem=element.parent().parent().parent()[0].querySelector("#customToolbarContent");
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			}); 
		}
	}
})
.directive('customToolbarAction',
		function($compile) {
	return {
		template:'',
		replace:true,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
				var contElem=element.parent().parent().parent()[0].querySelector("#customToolbarActionContent");
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			}); 
		}
	}
})

function boxExpanderControllerFunction($scope){
	$scope.toggle=function(){
		event.stopPropagation();
		$scope.expanded=!$scope.expanded;
	}
}