/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 *
 */

angular.module('angular_list', ['ng-context-menu','ngMaterial','ui.tree','angularUtils.directives.dirPagination','sbiModule'])
.directive('angularList', function() {
	return {
		templateUrl: '/knowage/js/src/angular_1.4/tools/commons/templates/angular-list.html',
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
			highlightsSelectedItem:"=?",
			showItemTooltip:"=?"
		},

		link: function (scope, elm, attrs) {

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

		}
	}
});

function ListControllerFunction($scope,sbiModule_translate,$mdDialog,$mdToast,$timeout,$compile){

	$scope.translate=sbiModule_translate;
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
			var userTypingTimeOut = 1000;
			$timeout(function() {
				if ($scope.tmpWordSearch != searchVal || $scope.prevSearch == searchVal) {
					return;
				}

				$scope.prevSearch = searchVal;
				$scope.searchFunction({searchValue:searchVal,itemsPerPage:$scope.itemsPerPage});

			}, userTypingTimeOut);
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

		var nit = parseInt((boxHeight - searchBoxHeight - paginBoxHeight-3) / listItemTemplBoxHeight);
		$scope.itemsPerPage = nit <= 0 ? 1 : nit;

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
			}, true
	);

}

