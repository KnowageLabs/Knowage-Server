angular.module('filter_card',[])
	.directive('filterCard', function () {
		 return {
		      restrict: 'E',
		      replace: 'true',
		      templateUrl: '/knowagewhatifengine/html/template/main/filter/filterCard.html',
		      controller:filterCardController
		  };
	});

function filterCardController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	$scope.showMultiHierDialog = function(ev,f){
		if($scope.member != undefined)
			$scope.member.hierarchies = [];
		$scope.member = f;
		$scope.selecetedMultiHierUN = $scope.member.hierarchies[$scope.member.selectedHierarchyPosition].uniqueName;
		$scope.showDialog(ev,"/main/filter/multiHierarchyDialog.html");
		console.log($scope.member);
	};
	
	$scope.updateHierarchie = function(ev){
		var axis = $scope.member.axis;
		var oldHier = $scope.member.hierarchies[$scope.member.selectedHierarchyPosition].uniqueName;
		var newHier = $scope.selecetedMultiHierUN;
		var pia = $scope.member.positionInAxis;
		
		if(oldHier != newHier)
			updateHierService(axis, oldHier, newHier, pia);
		$scope.closeDialog(ev);
	};
	
	updateHierService = function(ax, oldH, newH, pia){
		sbiModule_restServices.promisePost(
				"1.0","/axis/"+ax
				+"/updateHierarchyOnDimension/"
				+newH+"/"+oldH+"/"+pia
				+"?SBI_EXECUTION_ID="+JSsbiExecutionID)
		.then(function(response){
			$scope.table = $sce.trustAsHtml(response.data.table);
			$scope.handleResponse(response);
			$scope.filterSelected = [];
			$scope.initFilterList();
		},function(response){
			sbiModule_messaging.showErrorMessage("Error", 'An error occured has occured while updateing hierachie.');
		});
	};
};