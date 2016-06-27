var associatorDirective = angular.module('associator-directive', ['ngMaterial']);
associatorDirective.directive("associatorDirective",function(){
		return {
//			restrict: 'E',
			templateUrl:'/knowagemeta/js/src/angular_1.4/tools/commons/associatordirective/template/associatorDirectiveTemplate.jsp',
			controller: associatorDirectiveController,
			scope: {
				sourceModel:"=",
				targetModel:"=",
				sourceName:"@",
				targetName:"@",
				translate:"=",
				associatedItem:"@?"
			},
			 link: function (scope, element, attrs, ctrl, transclude) {

				 if(!attrs.associatedItem){
					 scope.associatedItem="links";
					 scope.$watch('targetModel', function() {
						 if (scope.targetModel != undefined || scope.targetModel != null)
							 scope.targetModel.forEach(function(entry) {
								 entry.links = [];
							 });
					 });
				 }
             }
		};
	});

function associatorDirectiveController($scope){

	$scope.drag=function(ev,item) {
		var id=ev.currentTarget.id.split("-")[1];
		ev.dataTransfer.setData("itemIndex", id);
	}

	$scope.allowDrop=function(ev) {

	    ev.preventDefault();
	}

	$scope.drop=function(ev,item) {
	    ev.preventDefault();
	    var data = ev.dataTransfer.getData("itemIndex");
	    var id=ev.currentTarget.id.split("-")[1];
	    if($scope.targetModel[id][$scope.associatedItem]==undefined){
	    	$scope.targetModel[id][$scope.associatedItem]=[];
	    }
	    if ($scope.targetModel[id][$scope.associatedItem].length == 0)
	    	$scope.targetModel[id][$scope.associatedItem].push($scope.sourceModel[data]);
	    $scope.$apply();
	    console.log($scope.targetModel[id]);
	}
}