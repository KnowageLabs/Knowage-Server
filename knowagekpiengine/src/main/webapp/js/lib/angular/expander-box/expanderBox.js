/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */

angular.module('expander-box', [ 'ngMaterial'])
.directive('expanderBox',function() {
	return {
		transclude : true,
		template: '<md-toolbar ng-click="toggle($event)" ng-class="toolbarClass">'+
						'<div class="md-toolbar-tools"><h2>{{expanderTitle}}</h2><span flex></span>'+
							'<md-button class="md-icon-button" aria-label="More">'+
								'<md-icon class="fa fa-chevron-{{expanded?\'up\':\'down\'}}"></md-icon>'+
							'</md-button>'+
						'</div>'+
					'</md-toolbar>'+
					'<md-content class="animate-accordion" layout-padding  ng-show="expanded" ng-style="{\'background-color\':backgroundColor}">'+
						'<div ng-transclude></div>'+
					'</md-content>',
							
		controller : boxExpanderControllerFunction,
		scope : {
			id : "@",
			color:"@?",
			backgroundColor:"@?",
			toolbarClass:"@?",
			expanderTitle:"=",
			locals:"=?",
			expanded:"=?"

		},
		link: function(scope, element, attrs, ctrl, transclude) {
			if(!attrs.color){
				scope.color="white"
			}
			
			if(!attrs.expanded){
				scope.expanded=false;
			}

			angular.element(element[0].querySelector("md-toolbar")).css("color",scope.color);
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
	$scope.toggle=function(event){
		event.stopPropagation();
		$scope.expanded=!$scope.expanded;
	}
}