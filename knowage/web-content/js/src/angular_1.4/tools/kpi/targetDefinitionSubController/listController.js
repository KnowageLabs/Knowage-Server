var app = angular.module('kpiTarget').controller('listController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionListControllerFunction ]);

function KPIDefinitionListControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;


	$scope.addTarget= function(){
		
		$angularListDetail.goToDetail();
		

	}
	$scope.loadTarget =function(item){
		
		$scope.target.name = item.name;
		$scope.target.category = item.category;
	//	item.startValidation =item.startValidation.replace(/\//g, ' ');
	//	item.startValidation = item.endValidation.replace(/\//g, ' ');

		$scope.target.startValidation = new Date(item.startValidation);
		$scope.target.endValidation = new Date(item.endValidation);
		$angularListDetail.goToDetail();
	};

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
}