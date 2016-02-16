/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */

angular.module('angular-list-detail', [ 'ngMaterial' ,'sbiModule'])
.directive('angularListDetail',
		function($compile) {
	return {
		template:'<div  class="kn-list-detail" layout="row" layout-wrap layout-fill></div>',
		controller : templatesControllerFunction,
		controllerAs : "ALD_controller",
		transclude : true,
		replace:true,
		link: function(scope, element, attrs, ctrl, transclude) {
			scope.newFuncName=scope.$eval(attrs.newFunction);
			scope.saveFuncName=scope.$eval(attrs.saveFunction);
			scope.cancelFuncName=scope.$eval(attrs.cancelFunction);
			ctrl.disableNewButton=scope.$eval(attrs.disableNewButton);
			ctrl.disableSaveButton=scope.$eval(attrs.disableSaveButton);
			ctrl.disableCancelButton=scope.$eval(attrs.disableCancelButton);
			ctrl.showSaveButton=scope.$eval(attrs.showSaveButton);
			ctrl.showCancelButton=scope.$eval(attrs.showCancelButton);
			ctrl.showNewButton=scope.$eval(attrs.showNewButton);
			ctrl.showDetail=scope.$eval(attrs.showDetail);
			
			scope.$watch(attrs.showDetail, function (value){
				if(value!=undefined){
					ctrl.showDetail=value;
				}
			});	
			
			scope.$watch(attrs.disableNewButton, function (value){
				if(value!=undefined){
					ctrl.disableNewButton=value;
				}
			});	
			scope.$watch(attrs.disableSaveButton, function (value){
				if(value!=undefined){
					ctrl.disableSaveButton=value;
				}
			});	
			
			scope.$watch(attrs.disableCancelButton, function (value){
				if(value!=undefined){
					ctrl.disableCancelButton=value;
				}
			});	
			
			scope.$watch(attrs.showCancelButton, function (value){
				if(value!=undefined){
					ctrl.showCancelButton=value;
				}
			});	
			
			scope.$watch(attrs.showSaveButton, function (value){
				if(value!=undefined){
					ctrl.showSaveButton=value;
				}
			});	
			
			scope.$watch(attrs.showNewButton, function (value){
				if(value!=undefined){
					ctrl.showNewButton=value;
				}
			});	
			
			
			
			transclude(scope,function(clone,scope) {
				angular.element(element[0]).append(clone);
//				$compile(element[0])(scope);
			});
		}
	}
})
.directive('list',
		function($compile) {
	return {
		template:'<div   flex="40" class="md-container kn-list" layout="column" layout-wrap>'+
		' <md-toolbar>'+
		'	<div class="md-toolbar-tools">'+
		'	 <h2 class="md-flex">{{AWD_listController.title}}</h2>'+
		'	<md-button  ng-disabled="ALD_controller.disableNewButton" aria-label="new" ng-if="newFuncName!=undefined && ALD_controller.showNewButton!=false" ng-click="newFuncName()" class="md-fab md-fab-top-right ">'+
		' 	<md-icon md-font-icon="fa-plus" class="fa s32 md-primary md-hue-2" ></md-icon>'+
		'	</md-button>'+
		'	</div>'+
		' </md-toolbar>'+
		'<md-content layout-margin flex>'+
		'</md-content>'+
		'</div>',
		replace:true,
		controller : listControllerFunction,
		controllerAs:"AWD_listController",
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
				var contElem=element[0].querySelector("div.md-container>md-content ")
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			});

			ctrl.title=attrs.label;
			scope.$watch(attrs.label, function (value){
				if(value!=undefined){
					ctrl.title=value;
				}
			});	
		}
	}
})

.directive('detail',
		function($compile) {
	return {
		template:'<div   flex class="md-container kn-detail" layout="column" layout-wrap>'+
		' <md-toolbar>'+
		'	<div class="md-toolbar-tools">'+
		'		<h2 class="md-flex">{{AWD_detailController.title}}</h2>'+
		'		<md-button aria-label="cancel" ng-disabled="ALD_controller.disableCancelButton" ng-if="cancelFuncName!=undefined && ALD_controller.showCancelButton!=false" ng-click="cancelFuncName()">{{translate.load("sbi.general.cancel")}}</md-button>'+
		'		<md-button aria-label="save" ng-disabled="ALD_controller.disableSaveButton" ng-if="saveFuncName!=undefined && ALD_controller.showSaveButton!=false" ng-click="saveFuncName()">{{translate.load("sbi.generic.update")}}</md-button>'+
		'	</div>'+
		' </md-toolbar>'+
		'<md-content layout-margin flex ng-show="ALD_controller.showDetail!=false">'+
		'</md-content>'+
		'</div>',
		replace:true,
		controller : detailControllerFunction,
		controllerAs:"AWD_detailController",
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
				var contElem=element[0].querySelector("div.md-container>md-content ")
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			});

			ctrl.title=attrs.label;
			scope.$watch(attrs.label, function (value){
				if(value!=undefined){
					ctrl.title=value;
				}
			});	
		}
	}
})

function templatesControllerFunction($scope,sbiModule_translate) {
	$scope.translate=sbiModule_translate;
}
function listControllerFunction($scope) {
}
function detailControllerFunction($scope) {
}
