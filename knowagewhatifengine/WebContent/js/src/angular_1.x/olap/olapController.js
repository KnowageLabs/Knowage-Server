var olapMod = angular.module('olapManager', [ 'ngMaterial','ui.tree']);
olapMod.controller("olapController", [ "$scope", "$timeout", "$window","$mdDialog",
		olapFunction ]);

function olapFunction($scope, $timeout, $window,$mdDialog, $mdSidenav) {


  $scope.templateList = '/knowagewhatifengine/html/template/filter/treeFirstLevel.html';
  $scope.templateListChild = '/knowagewhatifengine/html/template/filter/treeDeeperLevels.html';
  //tree example data
  $scope.data = [
    {
      "id": 1,
      "title": "node1",
      "collapsed":true,
      "nodes": [
        {
          "id": 11,
          "title": "node1.1",
          "collapsed":true,
          "nodes": [
            {
              "id": 111,
              "title": "node1.1.1",
              "collapsed":false,
              "nodes": []
            }
          ]
        },
        {
          "id": 12,
          "title": "node1.2",
          "collapsed":true,
          "nodes": []
        }
      ]
    },
    {
      "id": 2,
      "title": "node2",
      "collapsed":false,
      "nodes": [
        {
          "id": 21,
          "title": "node2.1",
          "collapsed":false,
          "nodes": []
        },
        {
          "id": 22,
          "title": "node2.2",
          "collapsed":false,
          "nodes": []
        }
      ]
    },
    {
      "id": 3,
      "title": "node3",
      "collapsed":true,
      "nodes": [
        {
          "id": 31,
          "title": "node3.1",
          "collapsed":false,
          "nodes": []
        }
      ]
    }
  ];
	$scope.cubes = ["AAA", "BBB", "CCC"];
	$scope.dimensions = ["COUNTRY","DATE","REGION","PRODUCT"];
	$scope.vals = ["val1","val2","val3"];

		$scope.openFilters = function(ev) {
		$mdDialog.show(
				$mdDialog.alert()
					.clickOutsideToClose(true)
					.title("Here goes filtering")
					.ok("ok")
					.targetEvent(ev)
		);
	}

  $scope.openFiltersDialog = function(ev){
    $mdDialog.show({
      scope: $scope,
      preserveScope: true,
      controllerAs:'olapCtrl',
      templateUrl:'/knowagewhatifengine/html/template/filter/filterDialog.html',
      targetEvent:ev,
      clickOutsideToClose:true
    });
  }

  $scope.closeFiltersDialog = function(){
    $mdDialog.hide();
  }

  $scope.testtt = function(i){
    console.log(i);
     firstLevelDrop(i.id);
  }

  firstLevelDrop = function(id){
    for(var i=0; i < $scope.data.length; i++){
      if($scope.data[i].id == id && $scope.data[i].nodes.length > 0){
        $scope.data[i].collapsed = !$scope.data[i].collapsed;
        break;
      }
      else{
        if( $scope.data[i].nodes.length > 0)
                otherLevelDrop(id, $scope.data[i].nodes);
      }
    }
  }

  otherLevelDrop = function(id,nodes){
    for(var i=0; i < nodes.length; i++){
      if(nodes[i].id == id && nodes[i].nodes.length > 0){
        nodes[i].collapsed = !nodes[i].collapsed;
      }
      else{
        if(nodes[i].nodes.length > 0){
            otherLevelDrop(id,nodes[i].nodes);
        }
      }
    }
  }

}
