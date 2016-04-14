angular.module('filter_panel',[])
.directive('filterPanel',function(){
	return{
		restrict: "E",
		replace: 'true',
		templateUrl: '/knowagewhatifengine/html/template/main/filter/filterPanel.html',
		controller: filterPanelController
	}
});

function filterPanelController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
	
	var visibleSelected = [];
	var visibleSelectedTracker = [];
	var filterFather;
	var h;
	var m;
	var oldSelectedFilter="";
	var hlght = false;
	
	angular.element(document).ready(function() {
		$scope.sendMdxQuery('null');
		checkVersions();
	});
	
	/**
	 * Dialogs  
	 **/
	$scope.openFiltersDialogAsync = function(ev, filter, node) {
		$scope.filterDialogToolbarName = filter.name;
		$scope.filterAxisPosition = filter.positionInAxis;
		$scope.activeaxis = filter.axis;
		filterFather = filter.selectedHierarchyUniqueName;
		h = filter.uniqueName;
		var exist = false;
		
		for(var i = 0; i< $scope.dataPointers.length;i++){
			if($scope.dataPointers[i] == filterFather){
				exist = true;
				$scope.data= $scope.loadedData[i];
				if($scope.activeaxis >= 0){
					var existsInTracker = false;
					for(var i=0; i<visibleSelectedTracker.length;i++){
						if(visibleSelectedTracker[i].id == filter.uniqueName){
							visibleSelected = visibleSelectedTracker[i].selected;
							existsInTracker = true;
						}						
					}
					if(!existsInTracker)
						getVisible($scope.data, h);
				}
			}
		}
		if(!exist){
			$scope.getHierarchyMembersAsynchronus(filterFather, filter.axis, null,filter.id);
			$scope.dataPointers.push(filterFather);
		}
		$scope.showDialog(ev,$scope.filterDial);
	}
	
	/**
	 *Tree functionalities 
	 **/
	$scope.expandTreeAsync = function(item){
		$scope.getHierarchyMembersAsynchronus(filterFather,$scope.activeaxis,item.uniqueName,item.id);
		console.log($scope.data);		
	}
	
	expandAsyncTree = function(d,dput,id){		
		for(var i = 0; i< d.length; i++){
			if(d[i].id == id){
				d[i]["children"] = dput;
				d[i]["collapsed"]=true;
				break;
			}
			else{
				if(d[i].children != undefined){
					if(!d[i].leaf && d[i].children.length>0){
						expandAsyncTree(d[i].children,dput,id);
					}
				}				
			} 
		}
	};
	
	 /*service for placing member on axis**/
	 $scope.putMemberOnAxis = function(fromAxis,member){
		 var encoded = encodeURI('1.0/axis/'+fromAxis+'/moveDimensionToOtherAxis/'+member.selectedHierarchyUniqueName+'/'+member.axis+'?SBI_EXECUTION_ID='+JSsbiExecutionID);
		 sbiModule_restServices.promisePost
		 (encoded,"",member)
			.then(function(response) {
				$scope.handleResponse(response);
				$timeout(function() {
					$scope.resize();
			    }, 1);
				
				
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
				
			});	
	}
	
	$scope.searchFilter = function(){
		$scope.loadingNodes = true;
		hlght = true;
		var encoded = encodeURI('/hierarchy/'+ h+ '/search/'+$scope.activeaxis+'/'+$scope.searchText+'/'+$scope.showSiblings+'?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
				checkIfExists(response.data);
				$scope.searchSucessText = $scope.searchText.toLowerCase();
				$scope.loadingNodes = false;
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured during search for filter", 'Error');
		});
	};
	
	checkIfExists = function(data){
		var exist = false;
		for(var i = 0; i< $scope.dataPointers.length;i++){
			if($scope.dataPointers[i] == filterFather){
					exist = true;
					$scope.loadedData[i] = data
					$scope.data= $scope.loadedData[i];
					if($scope.activeaxis >= 0){
						getVisible($scope.data,h);
				}
			}
		}
		if(!exist)
			$scope.dataPointers.push(filterFather);
	};
	
	$scope.getHierarchyMembersAsynchronus = function(hierarchy,axis,node,id){
		var encoded = encodeURI('/hierarchy/'+ hierarchy+ '/filtertree/'+ axis+ '?SBI_EXECUTION_ID='+ JSsbiExecutionID+ '&node='+node);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {

			  if(node!=null){
				  var shouldSearchVisible = true;
				  expandAsyncTree($scope.data,response.data, id);
					
				  for(var j = 0; j< visibleSelectedTracker.length;j++){
					if(visibleSelectedTracker[j].id == h && visibleSelectedTracker[j].selected.length > 0)
						shouldSearchVisible= false;
					}
				  if(shouldSearchVisible)
						getVisible($scope.data, h);
			  }				  
			  else{
				  checkIfExists(response.data);
			  }
			  
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while getting hierarchy members", 'Error');	
		});	
	}
	
	//Called to get visible elements row/column
	getVisible = function(data, un){
		for(var i=0;i<data.length;i++){
			if(data[i].visible){
				visibleSelected.push(data[i]);
			}
			if(data[i].children != undefined){
				getVisible(data[i].children);
			}
		}
		var element={id:un,selected:visibleSelected};
		visibleSelectedTracker.push(element);
	};
	
	//Called if row/column dimension is unselected
	removeUnselected = function(name){
		for(var i=0;i<visibleSelected.length;i++){
			if(name == visibleSelected[i].name){
				visibleSelected.splice(i,1);	
			}
		}
	};
	
	removeChildren = function(){
		for(var i=0; i<visibleSelected.length;i++){
			if(visibleSelected[i].children != undefined){
				delete visibleSelected[i].children;
			}
			if(visibleSelected[i].collapsed != undefined){
				delete visibleSelected[i].collapsed;
			}
		}
	};
	
	$scope.selectFilter = function(item){
		oldSelectedFilter = $scope.filterSelected[$scope.filterAxisPosition].name;
		h = $scope.filterCardList[$scope.filterAxisPosition].uniqueName;
		m = item.uniqueName;
		$scope.filterSelected[$scope.filterAxisPosition].name = item.name;
		$scope.filterSelected[$scope.filterAxisPosition].uniqueName = item.uniqueName;
		
	};
	
	$scope.closeFiltersDialog = function() {
		if(oldSelectedFilter != "...")
			$scope.filterSelected[$scope.filterAxisPosition].name = oldSelectedFilter;
		else	
			$scope.filterSelected[$scope.filterAxisPosition].name = "...";
		$scope.filterSelected[$scope.filterAxisPosition].uniqueName = "";
		
		$scope.searchText = "";
		hlght = false;
		$mdDialog.hide();		
	}
	
	$scope.filterDialogSave = function(){
		if($scope.activeaxis == -1)
			filterSlice();
		else
			filterPlaceMemberOnAxis();
		
		$mdDialog.hide();
	}
	
	filterSlice = function(){
		if(h != undefined && m!= undefined){
			var encoded = encodeURI('/hierarchy/'+ h+ '/slice/'+ m + '/'+ false + '?SBI_EXECUTION_ID='+ JSsbiExecutionID);
			sbiModule_restServices.promiseGet
			("1.0",encoded)
			.then(function(response) {
				  $scope.table = $sce.trustAsHtml(response.data.table);
				  $scope.filterSelected[$scope.filterAxisPosition].visible = true;
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured", 'Error');
			});	
		}
	};
	
	filterPlaceMemberOnAxis = function(){
		removeChildren();
		console.log("from pmona"+visibleSelected);
		var encoded = encodeURI('/axis/'+ $scope.activeaxis+ '/placeMembersOnAxis?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promisePost
		("1.0",encoded,visibleSelected)
		.then(function(response) {
			 visibleSelected = [];			
			 $scope.table = $sce.trustAsHtml(response.data.table);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
			
		});
	};
		
	//Called when checkbox is clicked in row/column on front end
	$scope.checkboxSelected = function(data){
		data.visible = !data.visible;
		if(data.visible){
			visibleSelected.push(data);
		}
		else{
			removeUnselected(data.name)
		}
	}
	
	 /* service for moving hierarchies* */
	$scope.moveHierarchies = function(axis, hierarchieUniqeName, newPosition,
			direction, member) {
		var encoded = encodeURI('1.0/axis/' + axis + '/moveHierarchy/' + hierarchieUniqeName
				+ '/' + newPosition + '/' + direction
				+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promisePost(
				encoded, "", member)
				.then(
						function(response) {
							$scope.handleResponse(response);
						},
						function(response) {
							sbiModule_messaging.showErrorMessage(
									"An error occured while movin hierarchy",
									'Error');
						});
	};
	
	$scope.highlight = function(name){
		if(!hlght)
			return false;
		if(name.toLowerCase().indexOf($scope.searchText.toLowerCase()) > -1)		
			return true;		
		else		
			return false		
	};		
			
	$scope.showHideSearchOnFilters = function(){		
		$scope.showSearchInput = !$scope.showSearchInput;		
	};
	
	/**
	 *This is not in use right now but maybe will be used in the future (synchronus tree)
	 **/
	/********************************START*******************************************************/
	$scope.expandTree = function(item) {
		var id = item.id;

		for (var i = 0; i < $scope.data.length; i++) {
			if ($scope.data[i].id == id && $scope.data[i].children.length > 0) {
				$scope.data[i].collapsed = !$scope.data[i].collapsed;
				break;
			} else {
				if ($scope.data[i].children.length > 0)
					
					levelDrop(id, $scope.data[i].children);
			}
		}
	}

	levelDrop = function(id, nodes) {
		for (var i = 0; i < nodes.length; i++) {
			if (nodes[i].id == id && nodes[i].children.length > 0) {
				nodes[i].collapsed = !nodes[i].collapsed;
			} else {
				if (nodes[i].children.length > 0) {
					levelDrop(id, nodes[i].children);
				}
			}
		}
	}
	/********************************End************************************************************/
	

	$scope.hideAsyncTree = function(item){
		item.collapsed = false;
	}
	
	$scope.openFiltersDialog = function(ev, filter, node) {
		var exist = false;
		var position;
		$scope.data=[];
		$scope.filterDialogToolbarName = filter.name;
		
		for(var i = 0; i< $scope.dataPointers.length;i++){
			if($scope.dataPointers[i] == uniqueName){
				position = i;
				exist = true;
			}
		}
		
		if(!exist)
			$scope.getHierarchyMembers(uniqueName, axis, node);
		else{
			$scope.data = $scope.loadedData[position];
		}
		
		$scope.showDialog(ev,$scope.filterDial);
		
	}
	
	/**
	 * Drag and drop functionalities start
	 **/	
	$scope.dropTop = function(data, ev) {
		var leftLength = $scope.rows.length;
		var topLength = $scope.columns.length;
		var fromAxis;
		var pa;
		
		if(data!=null){
			pa = data.positionInAxis;
			fromAxis = data.axis;
			
			if(fromAxis == -1){
				$scope.filterSelected[data.positionInAxis].name ="...";
				$scope.filterSelected[data.positionInAxis].visible =false;
			}				
			
			if(fromAxis!=0){
				data.positionInAxis = topLength;
				data.axis = 0;

				if ($scope.draggedFrom == 'left' && leftLength == 1){
					sbiModule_messaging.showErrorMessage("Column", 'Error');

				}
					
				else {
					if ($scope.draggedFrom == 'left') {
						$scope.rows.splice($scope.dragIndex, 1);
						$scope.columns.push(data);
					}
					if ($scope.draggedFrom == 'filter') {
						$scope.filterCardList.splice($scope.dragIndex, 1);
						$scope.columns.push(data);
					}
				}
				$scope.putMemberOnAxis(fromAxis,data);
				checkShift();
				fixAxisPosition("left");
				fixAxisPosition("filter");
				fixFilterSelectedList(fromAxis, pa );
			}
		}				
	};

	function fixFilterSelectedList(fa, pa){
		var size = $scope.filterSelected.length;
		for(var i = pa;i<size;i++){
			$scope.filterSelected[i] = $scope.filterSelected[i+1];
		}
		$scope.filterSelected = $scope.filterSelected.slice(0,size-1);
		
	}
	
	$scope.dropLeft = function(data, ev) {
		var leftLength = $scope.rows.length;
		var topLength = $scope.columns.length;
		var fromAxis;
		
		if(data !=null){
			fromAxis = data.axis;
			
			if(fromAxis == -1){
				$scope.filterSelected[data.positionInAxis].name ="...";
				$scope.filterSelected[data.positionInAxis].visible =false;
			}	
			
			if(fromAxis != 1){
				data.positionInAxis = leftLength;
				data.axis = 1;
				
				if ($scope.draggedFrom == 'top' && topLength == 1)
					sbiModule_messaging.showErrorMessage("Row", 'Error');
				else {
					if ($scope.draggedFrom == 'top') {
						$scope.columns.splice($scope.dragIndex, 1);
						$scope.rows.push(data);
					}
					if ($scope.draggedFrom == 'filter') {
						$scope.filterCardList.splice($scope.dragIndex, 1);
						$scope.rows.push(data);
					}
				}

				$scope.putMemberOnAxis(fromAxis,data);
				checkShift();
				fixAxisPosition("top");
				fixAxisPosition("filter");
			}
		}		
	}

	$scope.dropFilter = function(data, ev) {
		var leftLength = $scope.rows.length;
		var topLength = $scope.columns.length;
		var fromAxis;
		
		if(data != null){
			fromAxis = data.axis;
			
			if(data.measure){
				sbiModule_messaging.showErrorMessage("Measures can not be used as a filters!", 'Error');
				return null;
			}
			
			if(fromAxis!=-1){
				data.positionInAxis = $scope.filterCardList.length;
				data.axis = -1;
				
				if ($scope.draggedFrom == 'left' && leftLength == 1)
					sbiModule_messaging.showErrorMessage("Column", 'Error');
				else if ($scope.draggedFrom == 'top' && topLength == 1)
					sbiModule_messaging.showErrorMessage("Row", 'Error');
				else {
					if ($scope.draggedFrom == 'top') {
						$scope.columns.splice($scope.dragIndex, 1);
						$scope.filterCardList.push(data);
					}
					if ($scope.draggedFrom == 'left') {
						$scope.rows.splice($scope.dragIndex, 1);
						$scope.filterCardList.push(data);
					}
				}

				$scope.putMemberOnAxis(fromAxis,data);
				checkShift();
				fixAxisPosition("top");
				fixAxisPosition("left");
				
				$scope.filterSelected[$scope.filterSelected.length] = {name:"...",uniqueName:"",visible:false};
			}
		}	
	}

	$scope.dragSuccess = function(df, index) {
		$scope.draggedFrom = df;
		$scope.dragIndex = index;
	}
		
	$scope.checkCheckboxes = function (item, list) {
		if(item.hasOwnProperty("name")){
			var index = $scope.indexInList(item, list);

			if(index != -1){
				$scope.dtAssociatedLevels.splice(index,1);
			}else{
				$scope.dtAssociatedLevels.push(item);
			}
		} 
		console.log($scope.dtAssociatedLevels);
	};
	
	$scope.indexInList=function(item, list) {
		if(item.hasOwnProperty("name")){
			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				
				if(object.name==item.name){
					return i;
				}
			}
		}
		return -1;
	}
	
	$scope.openFilters = function(ev) {
		$mdDialog.show($mdDialog.alert().clickOutsideToClose(true).title(
				"Here goes filtering").ok("ok").targetEvent(ev));
	}

	/**
	 * Tree structure service
	 **/
	$scope.getHierarchyMembersSynchronus = function(uniqueName,axis,node){
		
		sbiModule_restServices.promiseGet
		("1.0",'/hierarchy/'+ uniqueName+ '/filtertree2/'+ axis+ '?SBI_EXECUTION_ID='+ JSsbiExecutionID+ '&node='+node)
		.then(function(response) {
			  $scope.data = response.data;
			  $scope.loadedData.push(response.data);
			  $scope.dataPointers.push(uniqueName);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
			
		});	
	}
	
	/**
	 * Filter shift if necessary  
	 **/
	$scope.filterShift = function(direction) {
		var length = $scope.filterCardList.length;
		var first = $scope.filterCardList[0];
		var last = $scope.filterCardList[length - 1];

		if (direction == "left") {
			for (var i = 0; i < length; i++) {
				$scope.filterCardList[i] = $scope.filterCardList[i + 1];
			}
			$scope.filterCardList[length - 1] = first;
		}
		else{
			for (var i = length - 2; i >= 0; i--) {
				$scope.filterCardList[i + 1] = $scope.filterCardList[i];
			}
			$scope.filterCardList[0] = last;
		}
	}
	
	checkShift = function(){
		$scope.shiftNeeded = $scope.filterCardList.length > $scope.numVisibleFilters ? true
				: false;
		
		$scope.topSliderNeeded = $scope.columns.length > $scope.maxCols? true : false;
		
		$scope.leftSliderNeeded = $scope.rows.length > $scope.maxRows? true : false;
	};
	
	fixAxisPosition = function(axis){
		var data;
		
		if(axis == "top")
			data = $scope.columns;
		if(axis == "left")
			data = $scope.rows;
		if(axis == "filter")
			data = $scope.filterCardList;
		
		for(var i=0;i<data.length;i++)
			data[i].positionInAxis = i;
	}
	
	//Initializing array filterSelected that is following selected dimension in filters 
	initFilterList = function (){
		for(var i = 0; i < $scope.filterCardList.length;i++){
			var x ={
					name:"...",
					uniqueName:"",
					visible:false
					};
			$scope.filterSelected[i] = x;
		}
	};
	
	$scope.sendMdxQuery = function(mdx) {
		var encoded = encodeURI("1.0/model/?SBI_EXECUTION_ID="+JSsbiExecutionID)
		sbiModule_restServices.promisePost(encoded,"",mdx)
		.then(function(response) {
			$scope.handleResponse(response);
			checkShift();
			$mdDialog.hide();
			$scope.mdxQuery = "";
			initFilterList();
			$scope.resize();
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while sending MDX query", 'Error');
			
		});	
	}
	
	$scope.bgColor = function(){
		if( $scope.searchText == "" || $scope.searchText.length>=  $scope.minNumOfLetters)
			return false;
		else	
			return true;
	};
	
	checkVersions = function(){
		var index;
		for(var i=0; i< $scope.filterCardList.length;i++){
			if($scope.filterCardList[i].name == "Version"){
				index = i;
				break
			}
		}
		
		
	};
};

