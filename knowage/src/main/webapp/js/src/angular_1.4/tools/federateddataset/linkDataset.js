/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
var app = angular.module("linkDatasetModule",["ngMaterial","angular_list","sbiModule"])
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller("linkDatasetCTRL",linkDatasetFunction);
linkDatasetFunction.$inject = ["sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$timeout","sbiModule_messaging","$document","sbiModule_config"];
function linkDatasetFunction(sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$timeout,sbiModule_messaging,$document,sbiModule_config){

	//VARIABLES


	$scope.translate = sbiModule_translate;
	$scope.showme = true;
	$scope.sourceList = [];
	$scope.tablesList = [];
	$scope.selectedTables = [];
	$scope.savedTables = [];
	$scope.forAdding = [];
	$scope.forDeletion = [];


	$scope.init = function(datasetId) {
	    $scope.datasetId = datasetId;
	    $scope.getTablesByDatasetID($scope.datasetId);
		$scope.getSources();
	 };

	$scope.removeFromSelected = [
	 		 		               	{
	 		 		               		label: sbiModule_translate.load("sbi.federationdefinition.delete"),
	 		 		               		icon:"fa fa-trash-o",
	 		 		               		backgroundColor:'red',
	 		 		               		action : function(item) {
	 		 		               				$scope.remove(item);
	 		 		               				console.log($scope.forDeletion);
	 		 		               			}
	 		 		               	}
	 		 		             ];


	//FUNCTIONS

	$scope.getSources = function(){ // service that gets predefined list GET
		sbiModule_restServices.promiseGet("2.0/metaSourceResource", "")
		.then(function(response) {
			$scope.sourceList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	$scope.getTablesBySourceID = function(id){
		sbiModule_restServices.promiseGet("2.0/metaSourceResource/"+id+"/metatables", "")
		.then(function(response) {
			$scope.tablesList = response.data;
		}, function(response) {
			sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');

		});
	}

	$scope.moveToSelected = function(item){

		var index = $scope.tablesList.indexOf(item);
		if($scope.selectedTables.indexOf(item)===-1){
			if(!$scope.arrayContains($scope.selectedTables,'tableId',item)){
				$scope.selectedTables.push(item);
				$scope.forAdding.push(item);
			}

		}
		console.log("selektovani" + $scope.selectedTables);
		console.log("za dodavanje" + $scope.forAdding);
	}
	$scope.arrayContains = function(array,property,item) {
		for (var i = 0; i < array.length; i++) {
			if(array[i][property] == item[property]){
				return true;
			}
		}
		return false;
	}

$scope.remove = function(item){

	var index = $scope.selectedTables.indexOf(item);
	var index1 = $scope.forAdding.indexOf(item);
	if($scope.forAdding.indexOf(item)>-1){
		$scope.forAdding.splice(index1,1);
	}

		if($scope.selectedTables.indexOf(item)>-1){
			$scope.selectedTables.splice(index,1);

			if(!$scope.arrayContains($scope.forDeletion,'tableId',item) && $scope.arrayContains($scope.savedTables,'tableId',item) ){

				$scope.forDeletion.push(item);

			}

		}


		console.log("selektovani" + $scope.selectedTables);
		console.log("za brisanje" + $scope.forDeletion);
	}

$scope.getTablesByDatasetID = function(id){
	sbiModule_restServices.promiseGet("2.0/metaDsRelationResource/dataset/"+id, "")
	.then(function(response) {

		$scope.selectedTables = response.data;
		$scope.savedTables = angular.copy(response.data);
		$scope.markDeleted(selectedTables_id);
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error getting saved', 'Error');

	});
}

$scope.deleteRelations = function(dsId,item){
	sbiModule_restServices.promiseDelete("2.0/metaDsRelationResource/"+dsId,item.tableId)
	.then(function(response) {
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error deleting', 'Error');

	});
}

$scope.insertRelations = function(dsId,item){
	sbiModule_restServices.promisePost("2.0/metaDsRelationResource/"+dsId, "",angular.toJson(item))
	.then(function(response) {
	}, function(response) {
		sbiModule_messaging.showErrorMessage('error inserting', 'Error');

	});
}

$scope.checkSave = function(){
	if($scope.forDeletion.length == 0 && $scope.forAdding.length == 0){
		return true;
	}else{
		return false;
	}

}



$scope.saveRelation = function(dsId){

	if($scope.forAdding.length > 0){
		console.log("adding");
		for (var i = 0; i < $scope.forAdding.length; i++) {
			$scope.insertRelations(dsId,$scope.forAdding[i]);
		}
		console.log($scope.forAdding);
		}



	if($scope.forDeletion.length > 0 ){
	console.log("deleting")
	for (var i = 0; i < $scope.forDeletion.length; i++) {
		$scope.deleteRelations(dsId,$scope.forDeletion[i]);
	}
	console.log($scope.forDeletion);
}


	sbiModule_messaging.showSuccessMessage('Successfully saved', 'Success!');
	$scope.forDeletion = [];
	$scope.forAdding = [];

    $scope.goBack();




//	$timeout(function(){
//
//		$scope.getTablesByDatasetID(dsId);
//	}, 1000);

	//$scope.goBack();

}

$scope.goBack = function(){
	//document.location.href = sbiModule_config.contextName + "/servlet/AdapterHTTP?ACTION_NAME=MANAGE_DATASETS_ACTION&amp;LIGHT_NAVIGATOR_DISABLED=TRUE";
	//history.go(-1);
	$scope.forDeletion = [];
	$scope.forAdding = [];
}

$scope.markDeleted = function(listId){
	for (var i = 0; i < $scope.selectedTables.length; i++) {
		if($scope.selectedTables[i].deleted){
			console.log($scope.selectedTables[i]);
			$timeout(function() {


				//document.getElementsByClassName("angularListRowItem").style.backgroundColor = "red";
				//document.getElementById('listItemTemplate').style.color  = "red";

		    }, 250);

		}
	}
}

};



