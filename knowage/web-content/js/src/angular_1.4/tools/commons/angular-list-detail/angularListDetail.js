/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */
 

var aldApp=angular.module('angular-list-detail', [ 'ngMaterial' ,'sbiModule'])
.factory('currentView',function(){
	return {value:'list'};
})
.service('$angularListDetail',function(currentView){ 
	this.goToList=function(){
		currentView.value='list';
	};
	this.goToDetail=function(){
	currentView.value='detail';
};
}) 
.directive('angularListDetail',
		function($compile) {
	return {
		template:'<div  class="kn-list-detail" layout="row" layout-wrap layout-fill></div>',
		controller : function($scope,sbiModule_translate,currentView) {
						this.currentView=currentView;
						$scope.translate=sbiModule_translate; 
					},
		controllerAs : "ALD_controller",
		transclude : true,
		replace:true,
		link: function(scope, element, attrs, ctrl, transclude) {

			

			ctrl.showDetail=scope.$eval(attrs.showDetail);
			ctrl.fullScreen=scope.$eval(attrs.fullScreen); 
			 
			scope.$watch(attrs.fullScreen, function (value){
				if(value!=undefined){
					ctrl.fullScreen=value;
				}
			});	
			
			scope.$watch(attrs.showDetail, function (value){
				if(value!=undefined){
					ctrl.showDetail=value;
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
		template:'<div ng-hide="ALD_controller.fullScreen==true && ALD_controller.currentView.value!=\'list\'" class="md-container kn-list" layout="column" layout-wrap ng-class="(ALD_controller.fullScreen==true && ALD_controller.currentView.value==\'list\') ? \'flex\' : \'flex-40\'">'+
		'<md-toolbar>'+
		'	<div class="md-toolbar-tools">'+
		'	 <h2 flex>{{AWD_listController.title}}</h2>'+
		'	<md-button  ng-disabled="AWD_listController.disableNewButton" aria-label="new" ng-if="newFuncName!=undefined && AWD_listController.showNewButton!=false" ng-click="newFuncName();" class="md-fab md-fab-top-right ">'+
		' 	<md-icon md-font-icon="fa-plus" class="fa s32 md-primary md-hue-2" ></md-icon>'+
		'	</md-button>'+
		'	</div>'+
		' </md-toolbar>'+
		'<md-content class="kn-list-content" layout-margin flex>'+
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
			
			scope.newFuncName=scope.$eval(attrs.newFunction);
			ctrl.disableNewButton=scope.$eval(attrs.disableNewButton);
			ctrl.showNewButton=scope.$eval(attrs.showNewButton);
			
			scope.$watch(attrs.disableNewButton, function (value){
				if(value!=undefined){
					ctrl.disableNewButton=value;
				}
			});	
			
			
			scope.$watch(attrs.showNewButton, function (value){
				if(value!=undefined){
					ctrl.showNewButton=value;
				}
			});	
			
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
		template:'<div ng-hide="ALD_controller.fullScreen==true && ALD_controller.currentView.value!=\'detail\'"  flex class="md-container kn-detail" layout="column" layout-wrap>'+
		' <md-toolbar>'+
		'	<div class="md-toolbar-tools">'+
//		'		<md-button aria-label="back"  ng-if="ALD_controller.fullScreen==true" ng-click="ALD_controller.currentView.value=\'list\'" >  <md-icon md-font-icon="fa fa-arrow-left"></md-icon></md-button>'+
		'		<h2 flex>{{AWD_detailController.title}}</h2>'+
		'<span class="extraButtonContainer"></span>'+
		'		<md-button aria-label="cancel" ng-disabled="AWD_detailController.disableCancelButton" ng-if="cancelFuncName!=undefined && AWD_detailController.showCancelButton!=false" ng-click="cancelFuncName()">{{translate.load("sbi.general.cancel")}}</md-button>'+
		'		<md-button aria-label="save" ng-disabled="AWD_detailController.disableSaveButton" ng-if="saveFuncName!=undefined && AWD_detailController.showSaveButton!=false" ng-click="saveFuncName()">{{translate.load("sbi.generic.update")}}</md-button>'+
		'	</div>'+
		' </md-toolbar>'+
		'<md-content class="kn-detail-content" layout-margin flex ng-show="ALD_controller.showDetail!=false">'+
		'</md-content>'+
		'</div>',
		replace:true,
		controller : detailControllerFunction,
		controllerAs:"AWD_detailController",
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
		
			scope.saveFuncName=scope.$eval(attrs.saveFunction);
			ctrl.disableSaveButton=scope.$eval(attrs.disableSaveButton);
			ctrl.showSaveButton=scope.$eval(attrs.showSaveButton);
			
			scope.cancelFuncName=scope.$eval(attrs.cancelFunction);
			ctrl.disableCancelButton=scope.$eval(attrs.disableCancelButton);
			ctrl.showCancelButton=scope.$eval(attrs.showCancelButton);
			
			ctrl.title=attrs.label;
			
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
			
			scope.$watch(attrs.label, function (value){
				if(value!=undefined){
					ctrl.title=value;
				}
			});	
			
			transclude(scope,function(clone,scope) {
				var contElem=element[0].querySelector("div.md-container>md-content ")
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			});
			
		}
	}
})


.directive('extraButton',
		function($compile) {
	return {
		template:'',
		replace:true,
		transclude : true,
		link: function(scope, element, attrs, ctrl, transclude) {
			transclude(scope,function(clone,scope) {
				var contElem=element.parent()[0].querySelector(".kn-detail md-toolbar .md-toolbar-tools .extraButtonContainer ");
				angular.element(contElem).append(clone);
//				$compile(contElem)(scope);
			}); 
		}
	}
})

 
function listControllerFunction($scope) {
}
function detailControllerFunction($scope) {
}
