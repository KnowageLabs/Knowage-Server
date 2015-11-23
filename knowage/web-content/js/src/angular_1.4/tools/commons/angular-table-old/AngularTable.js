/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * 
 */

angular.module('angular_table',[ 'ngMaterial', 'angularUtils.directives.dirPagination','ng-context-menu' ])
		.directive('dynamichtml', function ($compile) {
			return {
				restrict: 'A',
				replace: true,
				link: function (scope, ele, attrs) {
					scope.$watch(attrs.dynamichtml, function(html) {
						ele.html(html);
						$compile(ele.contents())(scope);
					});
				}
			};
		})
		.directive('angularTable',
				function() {
					return {
						templateUrl : '/knowage/js/src/angular_1.4/tools/commons/angular-table/angular-table.html',
						controller : TableControllerFunction,
						scope : {
							ngModel : '=',
							id : "@",
							columns:"=?",//items to display. if not defined list all element ordering by name
							columnsSearch:"=?",// columns where search
						 	showSearchBar:'=', //default false
						 	searchFunction:'&',
						 	pageCangedFunction:"&",
						 	totalItemCount:"=?", //if not present, create a non sync pagination and page change function is not necessary
						 	currentPageNumber:"=?",
						 	noPagination:"=?", //not create pagination and  totalItemCount and pageCangedFunction are not necessary
						 	clickFunction:"&", //function to call when click into element list
						 	menuOption:"=?", //menu to open with right click
						 	speedMenuOption:"=?", //speed menu to open with  button at the end of item
						 	selectedItem:"=?", //optional to get the selected  item value
						 	highlightsSelectedItem:"=?",
						 	multiSelect:"=?",
						 	scopeFunctions:"=?"
						
						},
						link : function(scope, elm, attrs) {

							console.log("Inizializzo AngularTable con id " + attrs.id);
							
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
					    	  if(attrs.multiSelect){
					    		  if(!attrs.selectedItem){
					    			  scope.selectedItem=[];
					    		  }
					    	  }

						}
					}
				}).filter('filterBySpecificColumns', function(){
					 return function(items,columnsSearch,searchTerm,localSearch) { 
						 if(searchTerm==undefined || searchTerm==""){
							 return items;
						 }
						  var filtered = [];
						  for(var item in items){
							if(columnsSearch!=undefined && columnsSearch.length!=0){
								for( var cols in columnsSearch){
									if (items[item][columnsSearch[cols]].toString().toUpperCase().indexOf(searchTerm.toUpperCase()) !== -1) {
										filtered.push(items[item]);
										break;
									}
								};
							}else{
								if (JSON.stringify(items[item]).toString().toUpperCase().indexOf(searchTerm.toUpperCase()) !== -1) {
									filtered.push(items[item]);
								}
							}
						};

						return filtered;
					  }});

function TableControllerFunction($scope, $timeout,$filter) {
	var $scope = $scope;
	$scope.currentPageNumber=1;
	$scope.tmpWordSearch = "";
	$scope.prevSearch = "";
	$scope.localSearch=false;
	$scope.searchFastVal="";
	$scope.column_ordering;
	$scope.reverse_col_ord=false;
	$scope.internal_column_ordering;
	$scope.internal_reverse_col_ord=false;
	
	
	
	$scope.searchItem=function(searchVal){ 
		if($scope.searchItem){
			$scope.searchFastVal=searchVal;
		}else{
		$scope.tmpWordSearch = searchVal;
		$timeout(function() {
			if ($scope.tmpWordSearch != searchVal || $scope.prevSearch == searchVal) {
				return;
			}
			$scope.prevSearch = searchVal;
			$scope.searchFunction({searchValue:searchVal,itemsPerPage:$scope.itemsPerPage,columnsSearch:$scope.columnsSearch,columnOrdering:$scope.column_ordering,reverseOrdering:$scope.reverse_col_ord});
			
		}, 1000);
	}
	}
	
$scope.indexInList=function(list,item){
	for( var i=0;i<list.length;i++){
		if(angular.equals(list[i], item)){
			return i;
		}
	}
	 return -1;
}
	
	$scope.isSelected=function(item){
		if($scope.multiSelect){
			return $scope.indexInList($scope.selectedItem,item) >-1;
			
		}else{
			return angular.equals($scope.selectedItem, item) ;
		}
		
	}
	
	$scope.clickItem=function(row,cell){
		if($scope.multiSelect){
			$scope.toggleMultiSelect(row);
		}else{
			$scope.selectedItem=row;	
		}
		
		$scope.clickFunction({item:row,cell:cell,listId:$scope.id});
		
	}
	
	// pagination item
	$scope.changeWordItemPP=function(){
		if(angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox'))[0]==undefined){
			return;
		}
		var boxHeight = angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox'))[0].offsetHeight;
		var searchBoxHeight= $scope.showSearchBar==true? angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .searchBarList'))[0].offsetHeight : 0;
		var paginBoxHeight= angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .box_pagination_list'))[0] == undefined ? 18 : angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .box_pagination_list'))[0].offsetHeight;
		paginBoxHeight=paginBoxHeight==0? 18:paginBoxHeight;
		var listItemTemplBoxHeight = angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .rowItem'))[0]==undefined? 29 : angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .rowItem'))[0].offsetHeight;
		var headButtonHeight = angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .thButton'))[0]==undefined? 21 : angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox .thButton'))[0].offsetHeight;
		
		var nit = parseInt((boxHeight - searchBoxHeight - paginBoxHeight-headButtonHeight-16) / listItemTemplBoxHeight);
		$scope.itemsPerPage = nit <= 0 ? 1 : nit;
//		console.log("boxHeight",boxHeight)
//		console.log("searchBoxHeight",searchBoxHeight)
//		console.log("paginBoxHeight",paginBoxHeight)
//		console.log("listItemTemplBoxHeight",listItemTemplBoxHeight)
//		console.log("headButtonHeight",headButtonHeight)
//		console.log("changeWordItemPP",nit)
		if(firstLoad){
			$scope.pageCangedFunction({itemsPerPage:$scope.itemsPerPage,newPageNumber:1,searchValue:''});
			 firstLoad=false;
		}
	
	}
	var firstLoad=true;
	 $timeout(function() {
		 $scope.changeWordItemPP();
       },0)
	
	$scope.$watch(
					function() {
						var elem=angular.element(document.querySelector('#angularTableTemplate.'+$scope.id+'ItemBox'))[0]
						return elem==undefined? null:  elem.offsetHeight;
					}, function(newValue, oldValue) {
						
						if (newValue != oldValue) {
							$scope.changeWordItemPP();
						}
					}, true);
	
	
	 
	 $scope.orderBy=function(column){
		 if( $scope.column_ordering == column){
			 $scope.reverse_col_ord=!$scope.reverse_col_ord;
		 }else{
			 $scope.column_ordering = column;
			 $scope.reverse_col_ord=false;
		 }
		 
		 if($scope.localSearch){
				$scope.internal_column_ordering=$scope.column_ordering;
				$scope.internal_reverse_col_ord=$scope.reverse_col_ord;
		 }else{
			 $scope.searchFunction({searchValue:$scope.prevSearch,itemsPerPage:$scope.itemsPerPage,columnsSearch:$scope.columnsSearch,columnOrdering:$scope.column_ordering,reverseOrdering:$scope.reverse_col_ord});
				 }
		 
		
	 }
	
	 
	 $scope.toggleMultiSelect=function(row){
		 var index=$scope.indexInList($scope.selectedItem,row) ;
		 if(index>-1){
			 $scope.selectedItem.splice(index, 1)
		 }else{
			 $scope.selectedItem.push(row);
		 }
	 }
	 
}
