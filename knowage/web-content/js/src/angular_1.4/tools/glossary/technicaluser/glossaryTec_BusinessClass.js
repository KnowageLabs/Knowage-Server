angular.module('glossaryTecnicalFunctionality').controller("GTBusinessClassController",["$scope","sbiModule_translate","sbiModule_restServices",BCControllerFunction]);

function BCControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.listBusinessClass=[];
	$scope.showSearchBusinessClassPreloader=false;
	$scope.sizeBusinessClass=0;
	$scope.selectedBusinessClass;
		
	$scope.loadBusinessClassInfo=function(item){
		
	}
	
	$scope.businessClassLike=function(searchValue,itemsPerPage){
		
	}
	
	$scope.changeBusinessClassPage=function(newPageNumber,itemsPerPage,searchValue){
		
	}

}