(function() {

	var scripts = document.getElementsByTagName("script");
	var currentScriptPath = scripts[scripts.length - 1].src;
	currentScriptPath = currentScriptPath.substring(0, currentScriptPath.lastIndexOf('/') + 1);

angular.module('bread_crumb', ['ngMaterial'])
.filter('i18n', function(sbiModule_i18n) {
	return function(label) {
		return sbiModule_i18n.getI18n(label);
	}
})
.directive('breadCrumb', function() {
	return {
		templateUrl: currentScriptPath + 'templates/bread_crumb.html',
		controller: breadCrumbControllerFunction,
		replace: true,
		scope: {
			ngModel:'=',
			itemName:"@",
			id:"@",
			homepage: '=?',
			selectedIndex:'=?',
			selectedItem:'=?',
			moveToCallback:'&?', //function callback to call when change item
			control:'=',
			disableGoBack:"=?"
		},
		link: function (scope, elm, attrs) {
			scope.control = scope.control || {};
			scope.ngModel= scope.ngModel || [];
			if(scope.control.insertBread==undefined){
				scope.control.insertBread = function(item){
					scope.addItem(item);
				};
			}
			if(scope.control.resetBreadCrumb==undefined){
				scope.control.resetBreadCrumb = function(){
					angular.copy([],scope.ngModel);
				};
			}
			if(scope.control.prevBread==undefined){
				scope.control.prevBread = function(){
					if(scope.selectedIndex!=undefined && scope.selectedIndex!=0){
						scope.moveToItem(scope.ngModel[scope.selectedIndex-1],scope.selectedIndex-1,false);
					}
				};
			}

			if(scope.control.refresh==undefined){
				scope.control.refresh = function(){
					scope.$apply();
				};
			}
		}
	}
});

function breadCrumbControllerFunction($scope,$filter,sbiModule_i18n){

	sbiModule_i18n.loadI18nMap()
		.then(function() {
			for (var i in $scope.ngModel) {
				$scope.ngModel[i].name = sbiModule_i18n.getI18n($scope.ngModel[i].name);
			}
		});

	var s=$scope;

	s.goToHome = function() {
		//TODO add link to the homepage
		return;
	}
	s.canGoBack=function(item,index){
		if($scope.disableGoBack==true && s.selectedIndex!=undefined && index<s.selectedIndex){
			return false;
		}else{
			return true
		}
	}

	s.moveToItem = function(item,index,isFromBread){

		if(!s.canGoBack(item,index) && isFromBread==true){
			return;
		}

		if(index!=s.selectedIndex){
			s.selectedIndex=index;
			s.selectedItem=item;
			s.ngModel=s.ngModel.slice(0,index+1);
			if(s.moveToCallback){
				s.moveToCallback({item:item,index:index});
			}
		}
	};

	s.addItem = function(item){
		s.ngModel.push(item);
		s.selectedIndex=s.ngModel.length-1;
		s.selectedItem=item;
	};
}
})();