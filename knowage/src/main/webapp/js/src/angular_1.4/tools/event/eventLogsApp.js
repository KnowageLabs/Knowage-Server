(function(){
	agGrid.initialiseAgGridWithAngular1(angular);
	angular.module("eventModule",["angular-list-detail", "angular_table","agGrid"]);
}());