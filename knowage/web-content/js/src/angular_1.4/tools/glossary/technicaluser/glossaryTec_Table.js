angular.module('glossaryTecnicalFunctionality').controller("GTTableController",["$scope","sbiModule_translate","sbiModule_restServices",TControllerFunction]);

function TControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.listTable=[];
	$scope.showSearchTablePreloader=false;
	$scope.sizeTable=0;
	$scope.selectedTable;
		
	$scope.loadTableInfo=function(item){
		
	}
	
	$scope.tableLike=function(searchValue,itemsPerPage){
		var item="Page=1&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadTableList(item);
	}
	
	$scope.changeTablePage=function(newPageNumber,itemsPerPage,searchValue){
		if(searchValue==undefined || searchValue.trim().lenght==0 ){
			searchValue='';
		}
		var item="Page="+newPageNumber+"&ItemPerPage="+itemsPerPage+"&label=" + searchValue;
		$scope.loadTableList(item);
	}
	
	$scope.loadTableList=function(item){
		sbiModule_restServices.promiseGet("1.0/table", "listTable", item).then(
				function(response) {
					
					$scope.listTable = response.data.item;
					$scope.sizeTable=response.data.itemCount;
//						$scope.showSearchDatasetPreloader = false;
					
				}
				,function(response) {
//					global.showToast(sbiModule_translate.load("sbi.glossary.load.error"), 3000);
					sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.glossary.load.error"))
//					$scope.showSearchDatasetPreloader = false;
				})
	}
	
}