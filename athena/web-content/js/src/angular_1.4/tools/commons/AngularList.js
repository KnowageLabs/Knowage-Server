angular.module('angular_list', ['ng-context-menu','ngMaterial','ui.tree', 'angular_rest'])
.directive('angularList', function() {
  return {
    templateUrl: '/athena/js/src/angular_1.4/tools/commons/templates/angular-list.html',
    controller: ListControllerFunction,
    scope: {
    	ngModel:'=',
    	itemName:"@",
    	id:"@",
    	dragDropOptions:"=",
        showSearchBar:'=', //default false
        searchFunction:'&',
        showSearchPreloader:"=", //default false
        pageCangedFunction:"&",
        totalItemCount:"=?",
        currentPageNumber:"=?",
        noPagination:"=?",
        enableDrag:"=",
        enableClone:"=",
        clickFunction:"&", //function to call when click into element list 
        menuOption:"=?",
        speedMenuOption:"=?",
        selectedItem:"=?",	//optional to get the selected item value
        highlightsSelectedItem:"=?"
    	},
//      compile: function(element, attrs){
//    	  if(!attrs.totalItemCount){
//    		  attrs.totalItemCount=attrs.ngModel.length;
//    		  attrs.gne='gaga';
//    	  }
//       },
      link: function (scope, elm, attrs) {
    
    	  console.log("scope",scope)
    	   if(!attrs.totalItemCount){
    		  scope.SyncPagination=false;
    	  }else{
    		  scope.SyncPagination=true;
    	  }
    	  if(attrs.noPagination){
    		  scope.paginate=false;
    	  }else{
    		  scope.paginate=!attrs.noPagination;
    	  }
    		   
    	  if(attrs.showSearchBar){
    		 if(!attrs.searchFunction){
    			 console.error("searchFunction(searchValue) not defined for search")
    		 }
    		 
    		 
    		
    	  }
    	  
    	 
    	  
//    	  if(attrs.enableDragDrop){
//     		 if(!attrs.dragDropOptions){
//     			 console.error("DragDropOptions not defined for enableDragDrop")
//     		 }
//     	  }
    	  
    	  
//    	  scope.nodeContextMenu=false;
//    	  
//    	  if(attrs.showSelectFilter) {
//    		  scope.functionality.push("showSelectFilter");
//    	  }
//    	  if(attrs.showSearchBar){
//    		  scope.functionality.push("showSearchBar");
//    	  }
//    	  if(attrs.removeItem){
//    		  scope.functionality.push("removeItem");
//    		  scope.nodeContextMenu=true;
//    	  }

      }
  }
  	});


function ListControllerFunction($scope,restServices,translate,$mdDialog,$mdToast,$timeout){
	$scope.translate=translate;
	$scope.currentPageNumber=1;
	$scope.tmpWordSearch = "";
	$scope.prevSearch = "";
	
	$scope.searchItem=function(searchVal){
		$scope.tmpWordSearch = searchVal;
		$timeout(function() {
			if ($scope.tmpWordSearch != searchVal || $scope.prevSearch == searchVal) {
				return;
			}
			
			$scope.prevSearch = searchVal;
			$scope.searchFunction({searchValue:searchVal,itemsPerPage:$scope.itemsPerPage});
			
		}, 1000);
		
		
		
	}
	$scope.prova=function(){
		console.log("prova");
	}
	
	
	$scope.clickItem=function(item){
		$scope.selectedItem=item;
		$scope.clickFunction({item:item});
	}
	
		// pagination word
	function changeWordItemPP() {
			var boxHeight = angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox'))[0].offsetHeight;
		var searchBoxHeight= $scope.showSearchBar==true? angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox .searchBarList'))[0].offsetHeight : 0;
		var paginBoxHeight= angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox .box_pagination_list'))[0] == undefined ? 18 : angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox .box_pagination_list'))[0].offsetHeight;
		paginBoxHeight=paginBoxHeight==0? 18:paginBoxHeight;
		var listItemTemplBoxHeight = angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox #listItemTemplate'))[0]==undefined? 27 : angular.element(document.querySelector('#angularListTemplate.'+$scope.id+'ItemBox #listItemTemplate'))[0].offsetHeight;
		

		// bpw == 0 ? bpw = 10 : bpw = bpw;
		var nit = parseInt((boxHeight - searchBoxHeight - paginBoxHeight-3) / listItemTemplBoxHeight);
		$scope.itemsPerPage = nit <= 0 ? 1 : nit;
//		console.log("boxHeight",boxHeight)
//		console.log("searchBoxHeight",searchBoxHeight)
//		console.log("paginBoxHeight",paginBoxHeight)
//		console.log("listItemTemplBoxHeight",listItemTemplBoxHeight)
//		console.log("changeWordItemPP",nit)
		if(firstLoad){
			$scope.pageCangedFunction({itemsPerPage:$scope.itemsPerPage,newPageNumber:1,searchValue:''});
			 firstLoad=false;
		}
	}
	var firstLoad=true;
	 $timeout(function() {
		 changeWordItemPP();
       },0)
	
	$scope.$watch(
					function() {
						return angular.element(document.querySelector('#angularListTemplate'))[0].offsetHeight;
					}, function(newValue, oldValue) {
						
						if (newValue != oldValue) {
							changeWordItemPP();
						}
					}, true);

	
	
	}