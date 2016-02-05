var olapMod = angular.module('olapManager', [ 'ngMaterial', 'ngSanitize','ngDraggable'])
.directive('compileTemplate', function($compile, $parse){
    return {
        link: function(scope, element, attr){
            var parsed = $parse(attr.ngBindHtml);
            function getStringValue() { return (parsed(scope) || '').toString(); }
            
            //Recompile if the template changes
            scope.$watch(getStringValue, function() {
                $compile(element, null,-9999)(scope);  //The -9999 makes it skip directives so that we do not recompile ourselves
            });
        }         
    }
});;
olapMod.controller("olapController", ["$scope", "$timeout", "$window","$mdDialog", "$http",'$sce',
		olapFunction ]);

function olapFunction($scope, $timeout, $window,$mdDialog, $http,$sce) {

  $scope.templateList = '/knowagewhatifengine/html/template/main/filter/treeFirstLevel.html';
  $scope.templateListChild = '/knowagewhatifengine/html/template/main/filter/treeDeeperLevels.html';
  $scope.filterCard = '/knowagewhatifengine/html/template/main/filter/filterCard.html';
  $scope.mainToolbar = '/knowagewhatifengine/html/template/main/toolbar/mainToolbar.html';
  $scope.filterPanel = '/knowagewhatifengine/html/template/main/filter/filterPanel.html';
  $scope.olapPanel = '/knowagewhatifengine/html/template/main/olap/olapPanel.html';
  $scope.rowToolbar = '/knowagewhatifengine/html/template/main/olap/rowToolbar.html';
  $scope.leftToolbarPlusMain = '/knowagewhatifengine/html/template/main/olap/leftToolbarPlusMain.html';
  $scope.leftPanel = '/knowagewhatifengine/html/template/left/leftPanel.html';
  $scope.rightPanel = '/knowagewhatifengine/html/template/right/rightPanel.html';
  
  $scope.rows;
  $scope.columns;  
  $scope.toolbarButtons=[];
  $scope.filterCardList = [];
  $scope.showMdxVar = "";
  
  $scope.numVisibleFilters = 5;
  $scope.shiftNeeded = $scope.filterCardList.length > $scope.numVisibleFilters? true : false; 
  
  angular.element(document).ready(function () {
	  $scope.sendMdxQuery('null');
		
		
  });
  
  $scope.data1=[];
  
  var counter=0;
  
  console.log(JSsbiExecutionID);
  
  filterXMLResult = function(res){
	  var regEx = /([A-Z]+_*)+/g;
	  var i;
	  
	  while(i = regEx.exec(res))
		  $scope.toolbarButtons.push(messageResource.get("sbi.olap.toolbar."+i[0],'messages'));
  }
  
  filterXMLResult(test);
  
  $scope.filterShift = function(direction){
	  var length = $scope.filterCardList.length;
	  
	  var first = $scope.filterCardList[0];
	  var last = $scope.filterCardList[length-1];
	  
	  
	  if(direction == "left"){
		  for(var i=0; i<length;i++){
			  $scope.filterCardList[i] = $scope.filterCardList[i+1];
		  }
		  
		  $scope.filterCardList[length-1] = first;
	  }
	  else{
		  for(var i=length-2; i>=0;i--){
			  $scope.filterCardList[i+1] = $scope.filterCardList[i];
		  }
		  $scope.filterCardList[0] = last;
	  }

  }
  
  $scope.sendMdxQuery = function(mdx) {
	  $http({
		  method: 'POST',
		  url: '/knowagewhatifengine/restful-services/1.0/model/?SBI_EXECUTION_ID='+JSsbiExecutionID,
		  data: mdx
		}).then(function successCallback(response) {
			
		    // this callback will be called asynchronously
		    // when the response is available
			
			$scope.table = $sce.trustAsHtml( response.data.table);
			console.log($http.url);
			$scope.rows = response.data.rows;
			$scope.columns = response.data.columns;
			$scope.filterCardList = response.data.filters;
			$scope.showMdxVar = response.data.mdxFormatted;
			
			console.log("rows->");
			console.log($scope.rows);
			console.log("columns->");
			console.log($scope.columns);
			console.log("filters->");
			console.log($scope.filterCardList);
		  }, function errorCallback(response) {
		    // called asynchronously if an error occurs
		    // or server returns response with an error status.
			console.log("Error!")
		  });
  }
  
  console.log(JSsbiExecutionID);
    
  /*$scope.getHierarchyMembers = function(uniqueName,axis,node){
	  $http({
		  method: 'GET',
		  url: '/knowagewhatifengine/restful-services/1.0/hierarchy/'+uniqueName+'/filtertree/'+axis+'?SBI_EXECUTION_ID='+JSsbiExecutionID+'&node='+node,
	  
	  }).then(function successCallback(response) {
		  if(node == 'root')
			  $scope.data1 = response.data;
		  
		  for(var i=0; i<response.data.length;i++){
			  
			  if(!response.data[i].leaf)
				  $scope.getHierarchyMembers(uniqueName,axis,response.data[i].uniqueName);
			  
			  $scope.data1["nodes"] = response.data[i];
			  console.log(response.data[i]);
		}
			
		   
		  console.log($scope.data1);
	  },function errorCallback(response) {
		    // called asynchronously if an error occurs
		    // or server returns response with an error status.
		  });
	   
  }*/
  
  $scope.drillDown = function(axis, position,  member, uniqueName, positionUniqueName){
		$http({
			  method: 'GET',
			  url: '/knowagewhatifengine/restful-services/1.0/member/drilldown/'+axis+'/'+position+'/'+member+'/'+positionUniqueName+'/'+uniqueName+'?SBI_EXECUTION_ID='+JSsbiExecutionID,
		  
		  }).then(function successCallback(response) {
			$scope.table = $sce.trustAsHtml(response.data.table);

			$scope.showMdxVar = response.data.mdxFormatted;
			

		  },function errorCallback(response) {
			    // called asynchronously if an error occurs
			    // or server returns response with an error status.
		  });
	}
  
  $scope.drillUp =function(axis, position,  member, uniqueName, positionUniqueName){
		$http({
			  method: 'GET',
			  url: '/knowagewhatifengine/restful-services/1.0/member/drillup/'+axis+'/'+position+'/'+member+'/'+positionUniqueName+'/'+uniqueName+'?SBI_EXECUTION_ID='+JSsbiExecutionID,
		  
		  }).then(function successCallback(response) {
			
			$scope.table = $sce.trustAsHtml(response.data.table);
			$scope.showMdxVar = response.data.mdxFormatted;

		  },function errorCallback(response) {
			    // called asynchronously if an error occurs
			    // or server returns response with an error status.
		  });
	}
  
  $scope.swapAxis =function(){
		$http({
			  method: 'POST',
			  url: '/knowagewhatifengine/restful-services/1.0/axis/swap?SBI_EXECUTION_ID='+JSsbiExecutionID,
		  
		  }).then(function successCallback(response) {
			  console.log(response.data.table);
			 $scope.table = $sce.trustAsHtml(response.data.table);
		  },function errorCallback(response) {
			    // called asynchronously if an error occurs
			    // or server returns response with an error status.
		  });
	}

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

  $scope.dropTop = function(data,ev){
	
	if($scope.columns.length == 1 && $scope.columns[0].id == data.id ){
		alert("Not allowed")
	}
	else{
		$scope.rows.push(data);
		remove(data.id);
	}
		
	
  }
  
  $scope.dropLeft = function(data,ev){
	  	
	  if($scope.rows.length == 1 && $scope.rows[0].id == data.id ){
			alert("Not allowed")
		}
		else{
			$scope.columns.push(data);
			remove(data.id);
		}
  }
  
  $scope.dropFilter = function(data,ev){
	  console.log(isfilter);
	  	
	  $scope.filterCardList.push(data);
  }
  

  remove = function (id){
	  for(var i=0;i<$scope.columns.length;i++){
		  if(id == $scope.columns[i].id){
			  $scope.columns.splice(i,1);
			  break;
		  }
	  }
	  for(var i=0;i<$scope.rows.length;i++){
		  if(id == $scope.rows[i].id){
			  $scope.rows.splice(i,1);
			  break;
		  }
	  }
  }
  $scope.openFiltersDialog = function(ev){
    $mdDialog.show({
      scope: $scope,
      preserveScope: true,
      controllerAs:'olapCtrl',
      templateUrl:'/knowagewhatifengine/html/template/main/filter/filterDialog.html',
      targetEvent:ev,
      clickOutsideToClose:true
    });
  }
  
  $scope.openMdxQueryDialog = function(ev){
    $mdDialog.show({
      scope: $scope,
      preserveScope: true,
      controllerAs:'olapCtrl',
      templateUrl:'/knowagewhatifengine/html/template/main/toolbar/sendMdx.html',
      targetEvent:ev,
      clickOutsideToClose:true
    });
  }
  
  $scope.openShowMdxDialog = function(ev){
	    $mdDialog.show({
	      scope: $scope,
	      preserveScope: true,
	      controllerAs:'olapCtrl',
	      templateUrl:'/knowagewhatifengine/html/template/main/toolbar/showMdx.html',
	      targetEvent:ev,
	      clickOutsideToClose:true
	    });
	  }

  $scope.closeFiltersDialog = function(){
    $mdDialog.hide();
  }

  $scope.expandTree = function(item){
     var id = item.id;
     
     for(var i=0; i < $scope.data.length; i++){
         if($scope.data[i].id == id && $scope.data[i].nodes.length > 0){
        	 $scope.data[i].collapsed = !$scope.data[i].collapsed;
        	 break;
         }
         else{
           if( $scope.data[i].nodes.length > 0)
                   levelDrop(id, $scope.data[i].nodes);
         }
       }
  }

  levelDrop = function(id,nodes){
    for(var i=0; i < nodes.length; i++){
      if(nodes[i].id == id && nodes[i].nodes.length > 0){
        nodes[i].collapsed = !nodes[i].collapsed;
      }
      else{
        if(nodes[i].nodes.length > 0){
            levelDrop(id,nodes[i].nodes);
        }
      }
    }
  }

}
