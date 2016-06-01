angular.module('alertDefinitionManager').controller('executeETLController', function($scope, $timeout,sbiModule_translate,sbiModule_restServices) {

	$scope.initNgModel=function(){
		if($scope.ngModel==undefined){
			$scope.ngModel=angular.extend({});
		} 
		$scope.listDoc = [];
		if($scope.ngModel.listDocIdSelected == undefined){
			$scope.ngModel.listDocIdSelected = [];
			$scope.listDocSelected = [];
		}else{
			var selected = $scope.ngModel.listDocIdSelected;
			for(i in selected){
				$scope.listDocSelected.push(selected[i]);
			}
		}
	}
	
	$scope.validate=function(){
		var valid=true;
		if(!$scope.ngModel.listDocIdSelected || $scope.ngModel.listDocIdSelected.length == 0)
			valid=false;
		return valid;
	}

	sbiModule_restServices.get('2.0/documents', "listDocument", "includeType=ETL").success(function(data) {
		$scope.loading = false;
		$scope.listDoc = data.item;
	}).error(function(data, status){
		$scope.loading = false;
		//TODO check this
		$scope.listDoc = [];
		$scope.log.error('GET RESULT error of ' + data + ' with status :' + status);
	});

	$scope.onSelectDoc=function(row,column,listId){
		var id = row.DOCUMENT_ID;
		var lst = $scope.ngModel.listDocIdSelected;
		var toAdd = true;
		for(i in lst){
			if(lst[i].DOCUMENT_ID == id){
				toAdd = false;
				$scope.ngModel.listDocIdSelected.splice(i, 1);
				console.log("removed:"+id);
				break;
			}
		}
		if(toAdd){
			$scope.ngModel.listDocIdSelected.splice(0, 0, {"DOCUMENT_ID":id});
			console.log("added:"+id);
		}
	}

});