//var jsDep=["/athena/js/lib/angular/angular-material_0.10.0/angular-material.js",
//           "/athena/js/lib/angular/contextmenu/ng-context-menu.min.js",
//           "/athena/js/lib/angular/pagination/dirPagination.js",
//           "/athena/js/lib/angular/angular-tree/angular-ui-tree.js"]
//var cssDep=["/athena/js/lib/angular/angular-material_0.10.0/angular-material.min.css",
//            "/athena/js/lib/angular/angular-tree/angular-ui-tree.min.css",
//            "/athena/themes/glossary/css/tree-style.css",
//            "/athena/themes/glossary/css/angular-list.css"]
//
//var head=document.head;
//for(var i=0;i<jsDep.length;i++){
//	var contain=false;
//	for(var j=0;j<head.children.length;j++){
//		if(head.children.item(j).nodeName=="SCRIPT" && head.children.item(j).outerHTML.indexOf(jsDep[i])!=-1){
//			contain=true;
//			break;
//		}
//	}
//	if(!contain){
//		var sc = document.createElement("script");
//		sc.setAttribute("src", jsDep[i]);
//		sc.setAttribute("type", "text/javascript");
//		head.appendChild(sc);
////		head.insertBefore(sc,head.firstChild);
//		}
//}
//
//for(var i=0;i<cssDep.length;i++){
//	var contain=false;
//	for(var j=0;j<head.children.length;j++){
//		if(head.children.item(j).nodeName=="LINK" && head.children.item(j).outerHTML.indexOf(cssDep[i])!=-1){
//			contain=true;
//			break;
//		}
//	}
//	
//	if(!contain){
//	var sc = document.createElement("link");
//	sc.setAttribute("href", cssDep[i]);
//	sc.setAttribute("rel", "stylesheet");
//	head.appendChild(sc);
////	head.insertBefore(sc,head.firstChild);
//	}
//}
//setTimeout(function(){ 
//	angular.module('angular_list').requires.push('ngMaterial');
//angular.module('angular_list').requires.push('ui.tree');
//angular.module('angular_list').requires.push('ng-context-menu');
//angular.module('angular_list').requires.push('angularUtils.directives.dirPagination');}, 1000);


	
	

angular.module('angular_list', ['ng-context-menu','ngMaterial','ui.tree','angularUtils.directives.dirPagination'])
.directive('angularList', function() {
  return {
    templateUrl: '/athena/js/src/angular_1.4/tools/commons/templates/angular-list.html',
    controller: ListControllerFunction,
    scope: {
    	ngModel:'=',
    	itemName:"@",
    	id:"@",
    	dragDropOptions:"=",
    	showEmptyPlaceholder:"=?", //default false
        showSearchBar:'=', //default false
        searchFunction:'&',
        showSearchPreloader:"=", //default false
        pageCangedFunction:"&",
        totalItemCount:"=?", //if not present, create a non sync pagination and page change function is not necessary
        currentPageNumber:"=?",
        noPagination:"=?",  //not create pagination and totalItemCount and pageCangedFunction are not necessary
        enableDrag:"=",
        enableClone:"=",
        clickFunction:"&", //function to call when click into element list 
        menuOption:"=?",	//menu to open with right click
        speedMenuOption:"=?", //speed menu to open with button at the end of item
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
    
    	  console.log("Inizializzo AngularList con id "+attrs.id,scope)
    	  
    	  
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
    			 scope.localSearch=true;
    		 }    		 
    	  }
    	  
    	  if(attrs.dragDropOptions){
    		  scope.dragAndDropEnabled=true;
    	  }else{
    		  scope.dragAndDropEnabled=false;
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


function ListControllerFunction($scope,translate,$mdDialog,$mdToast,$timeout,$compile){
	
	$scope.translate=translate;
	$scope.currentPageNumber=1;
	$scope.tmpWordSearch = "";
	$scope.prevSearch = "";
	$scope.localSearch=false;
	$scope.searchFastVal="";
	
	
	$scope.searchItem=function(searchVal){
		
		if($scope.localSearch){
			$scope.searchFastVal=searchVal;
		}else{
		$scope.tmpWordSearch = searchVal;
		$timeout(function() {
			if ($scope.tmpWordSearch != searchVal || $scope.prevSearch == searchVal) {
				return;
			}
			
			$scope.prevSearch = searchVal;
			$scope.searchFunction({searchValue:searchVal,itemsPerPage:$scope.itemsPerPage});
			
		}, 1000);
	}
		
		
	}
	$scope.isSelected=function(item){
		return angular.equals($scope.selectedItem, item) ;
	}
	
	
	$scope.clickItem=function(item){
		$scope.selectedItem=item;
		$scope.clickFunction({item:item,listId:$scope.id});
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

	