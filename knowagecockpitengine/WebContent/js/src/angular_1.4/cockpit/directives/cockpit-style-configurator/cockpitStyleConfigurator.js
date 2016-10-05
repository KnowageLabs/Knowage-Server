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

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 * 
 */
(function() {
angular.module('cockpitModule').directive('cockpitStyleConfigurator',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-style-configurator/templates/cockpitStyleConfigurator.html',
		   controller: cockpitStyleConfiguratorControllerFunction,
		   transclude: true,
		   scope: true,

		   	compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    },
                    post: function postLink(scope, element, attrs, ctrl, transclud) {
                    	 
                    	scope.ngModel = scope.$parent.$eval(attrs.ngModel);
                    	
                    	if(scope.ngModel==undefined){
                    		scope.$parent.$eval(attrs.ngModel+"={}");
                    		scope.ngModel = scope.$parent.$eval(attrs.ngModel);
//                    		scope.ngModel={};
                    	} 
                    	
                    	if(attrs.widget!=undefined){
                    		scope.isWidget=true;
                    		scope.initModel();
                    	}else{
                    		scope.isWidget=false;
                    	}
                    
                    	 transclude(scope, function (clone, scope) {
                             angular.element(element[0].querySelector("md-content")).prepend(clone);
                         });
                    
                    }
                };
		   	}
	   }
});

angular.module('cockpitModule').directive('cockpitStyleCustomWidgetConfigurator',function($compile){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-style-configurator/templates/cockpitStyleCustomWidgetConfigurator.html',
		   transclude: true,
		   replace: true,
		   controller: function(){},
		   controllerAs : "cscwc_controller",
		   scope: true,
		   compile: function (tElement, tAttrs, transclude) {
             return {
                 pre: function preLink(scope, element, attrs,ctrl, transclud) {
                 },
                 post: function postLink(scope, element, attrs,ctrl, transclud) {
                	 
                	ctrl.labelWidget = scope.$parent.$eval(attrs.label);
                	ctrl.layoutType = attrs.layout;
                	 if(ctrl.layoutType==undefined){
                		 ctrl.layoutType="row";
                	 }
                	 transclude(scope, function (clone, scope) {
                         angular.element(element[0].querySelector("md-card>md-card-content")).append(clone);
                     });
                 }
             };
		   	}
	   }
});

function cockpitStyleConfiguratorControllerFunction($scope,sbiModule_translate,cockpitModule_template){
	$scope.translate=sbiModule_translate;
	$scope.angular=angular;
	$scope.cockpitStyle={};
	angular.copy(cockpitModule_template.configuration.style,$scope.cockpitStyle);
	
	$scope.initModel=function(){
		angular.copy(angular.merge({},$scope.cockpitStyle,$scope.ngModel),$scope.ngModel)
	}
	
	
	$scope.resetBordersStyle=function(){
		$scope.ngModel.borders=$scope.cockpitStyle.borders
		angular.copy($scope.cockpitStyle.border,$scope.ngModel.border);
	}
	$scope.resetTitlesStyle=function(){
		$scope.ngModel.titles=$scope.cockpitStyle.titles;
		angular.copy($scope.cockpitStyle.title,$scope.ngModel.title);
	}
	$scope.resetShadowsStyle=function(){
		$scope.ngModel.shadows=$scope.cockpitStyle.shadows;
		angular.copy($scope.cockpitStyle.shadow,$scope.ngModel.shadow);
	}
	
	
	
	$scope.bordersSize=[
		                    {
		                    	label:sbiModule_translate.load("sbi.cockpit.style.borders.solid"),
		                    	value:'solid',
		                    	exampleClass:"borderExampleSolid"
		                    },
		                    {
		                    	label:sbiModule_translate.load("sbi.cockpit.style.borders.dashed"),
		                    	value:'dashed',
		                    	exampleClass:"borderExampleDashed"
		                    },
		                    {
		                    	label:sbiModule_translate.load("sbi.cockpit.style.borders.dotted"),
		                    	value:'dotted',
		                    	exampleClass:"borderExampleDotted"
		                    }
	                    ];
	$scope.bordersWidth=[
		                    {
		                    	label:"1px",
		                    	value:"1px"
		                    },
		                    {
		                    	label:"2px",
		                    	value:"2px"
		                    },
		                    {
		                    	label:"3px",
		                    	value:"3px"
		                    },
		                    {
		                    	label:"4px",
		                    	value:"4px"
		                    },
	                    ];
	$scope.titleFontSize=[
		                    {
		                    	label:'SM',
		                    	value:'8px'
		                    },
		                    {
		                    	label:'MD',
		                    	value:'12px'
		                    },
		                    {
		                    	label:'LG',
		                    	value:'16px'
		                    },
		                    {
		                    	label:'XL',
		                    	value:'20px'
		                    },
	                    ];
	$scope.titleFontWeight=[
	                    {
	                    	label:sbiModule_translate.load("sbi.cockpit.style.titles.thin"),
	                    	value:'200'
	                    },
	                    {
	                    	label:sbiModule_translate.load("sbi.cockpit.style.titles.regular"),
	                    	value:'regular'
	                    },
	                    {
	                    	label:sbiModule_translate.load("sbi.cockpit.style.titles.bold"),
	                    	value:'bold'
	                    }
	                    ];
	$scope.boxShadow=[
		                    {
		                    	label:'1px',
		                    	value:'0px 1px 1px #ccc'
		                    },
		                    {
		                    	label:'2px',
		                    	value:'0px 2px 3px #ccc'
		                    },
		                    {
		                    	label:'4px',
		                    	value:'0px 4px 5px #ccc'
		                    },
		                    {
		                    	label:'8px',
		                    	value:'0px 8px 19px #ccc'
		                    },
	                    ];
 
}


})();