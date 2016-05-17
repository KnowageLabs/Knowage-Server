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
	var selectedFlag = false;
	
	var typeMsgWarn =sbiModule_translate.load('sbi.common.warning');
	$scope.loadingFilter = true;
	$scope.filterPanelEmpty = sbiModule_translate.load('sbi.olap.execution.table.filter.empty');
	
	angular.element(document).ready(function() {
		$scope.sendMdxQuery('null');
		
	});
	
	
	$scope.clearLoadedData = function(name){
		for(var i=0; i< $scope.dataPointers.length; i++){
			if(name == $scope.dataPointers[i]){
				$scope.dataPointers.splice(i,1);
				$scope.loadedData.splice(i,1)
				break;
			}
		}
		var visibleSelected = [];
		var visibleSelectedTracker = [];
	};
	
	clearSelectedList = function(){
		for(var i=0;i< visibleSelected.length;i++){
			if(visibleSelected[i].id.indexOf(h) == -1){
				visibleSelected.splice(i,1);
			}
		}
		for(var i=0;i<visibleSelectedTracker.length;i++){
			if(visibleSelectedTracker[i].id == undefined || visibleSelectedTracker[i].id.indexOf(h) == 1){
				visibleSelectedTracker.splice(i,1);
			}
		}
	};
	
	getVisibleService = function(un,axis){
		var encoded = encodeURI('/hierarchy/'+ un+ '/getvisible/'+axis+'?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
			visibleSelected = response.data;
		}, function(response) {
			//sbiModule_messaging.showErrorMessage("An error occured during search for filter", 'Error');
		});
	};
	
	/**
	 * Dialogs  
	 **/
	$scope.openFiltersDialogAsync = function(ev, filter, node) {
		
		$scope.clearLoadedData(filter.uniqueName);
		visibleSelected = [];//check it
		visibleSelectedTracker = [];//check it
		$scope.searchText = "";
		$scope.loadingFilter = true;
		var x = {name:'Waiting...'};
		$scope.data = [];
		$scope.data.push(x);
		
		if(filter.axis > -1)
			getVisibleService(filter.uniqueName,filter.axis);
		
		$scope.filterDialogToolbarName = filter.caption;
		$scope.filterAxisPosition = filter.positionInAxis;
		$scope.activeaxis = filter.axis;
		filterFather = filter.selectedHierarchyUniqueName;
		h = filter.uniqueName;

			$scope.getHierarchyMembersAsynchronus(filterFather, filter.axis, null,filter.id);
			$scope.dataPointers.push(filterFather);

		
		$scope.showDialog(ev,$scope.filterDial);

		$scope.loadingFilter = false;
	};
	
	/**
	 *Tree functionalities 
	 **/
	$scope.expandTreeAsync = function(item){
		$scope.getHierarchyMembersAsynchronus(filterFather,$scope.activeaxis,item.uniqueName,item.id);	
	};
	
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
		 var toSend = {
				 'fromAxis':fromAxis,
				 'hierarchy':member.selectedHierarchyUniqueName,
				 'toAxis': member.axis
		 }
		 //var encoded = encodeURI('1.0/axis/'+fromAxis+'/moveDimensionToOtherAxis/'+member.selectedHierarchyUniqueName+'/'+member.axis+'?SBI_EXECUTION_ID='+JSsbiExecutionID);
		 var encoded = encodeURI('1.0/axis/moveDimensionToOtherAxis?SBI_EXECUTION_ID='+JSsbiExecutionID);
		 sbiModule_restServices.promisePost
		 (encoded,"",toSend)
			.then(function(response) {
				$scope.handleResponse(response);
				checkShift();
				updateFilterTracker();
				if(fromAxis == 1){
					$scope.leftStart = 0;
				}
				if(fromAxis == 0){
					$scope.topStart = 0;
				}
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
				
			});	
	}
	
	$scope.searchFilter = function(){		
		hlght = true;
		var toSend = {
			'hierarchy':h,
			'axis': $scope.activeaxis,
			'name': $scope.searchText,
			'showS':$scope.showSiblings
		};
		
		var encoded = encodeURI('1.0/hierarchy/search?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promisePost
		(encoded,"",toSend)
		.then(function(response) {
				checkIfExists(response.data);
				$scope.searchSucessText = $scope.searchText.toLowerCase();
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
		if(!exist){
			$scope.data= data;
			$scope.dataPointers.push(filterFather);
		}
			
	};
	
	$scope.getHierarchyMembersAsynchronus = function(hierarchy,axis,node,id){
		var encoded = encodeURI('/hierarchy/'+ hierarchy+ '/filtertree/'+ axis+ '?SBI_EXECUTION_ID='+ JSsbiExecutionID+ '&node='+node);
		sbiModule_restServices.promiseGet
		("1.0",encoded)
		.then(function(response) {
				//$scope.handleResponse(response)
			  if(node!=null){
				  var shouldSearchVisible = true;
				  expandAsyncTree($scope.data,response.data, id);
					
				  for(var j = 0; j< visibleSelectedTracker.length;j++){
					if(visibleSelectedTracker[j].id == h && visibleSelectedTracker[j].selected.length > 0)
						shouldSearchVisible= false;
					}
				  //if(shouldSearchVisible)
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
		selectedFlag = true;
		oldSelectedFilter = $scope.filterSelected[$scope.filterAxisPosition];
		h = $scope.filterCardList[$scope.filterAxisPosition].uniqueName;
		m = item.uniqueName;
		$scope.filterSelected[$scope.filterAxisPosition].name = item.name;
		$scope.filterSelected[$scope.filterAxisPosition].uniqueName = item.uniqueName;
	};
	
	$scope.closeFiltersDialog = function() {
		
		if(selectedFlag){
			if(oldSelectedFilter.name != "..."){
				$scope.filterSelected[$scope.filterAxisPosition].name = oldSelectedFilter.name;
				$scope.filterSelected[$scope.filterAxisPosition].uniqueName = oldSelectedFilter.uniqueName;
			}				
			else	
				$scope.filterSelected[$scope.filterAxisPosition].name = "...";
				$scope.filterSelected[$scope.filterAxisPosition].uniqueName = "";
			
			selectedFlag = false;
		}
		$scope.searchText = "";
		hlght = false;
		$mdDialog.hide();		
	}
	
	$scope.filterDialogSave = function(){
		if($scope.activeaxis == -1)
			filterSlice();
		else
			filterPlaceMemberOnAxis();
		
		selectedFlag = false;
		$mdDialog.hide();
	}
	
	filterSlice = function(){
		if(filterFather != undefined && m!= undefined){
			var encoded = encodeURI('/hierarchy/'+ filterFather+ '/slice/'+ m + '/'+ false + '?SBI_EXECUTION_ID='+ JSsbiExecutionID);
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
		clearSelectedList();
		console.log("from pmona"+visibleSelected);
		var encoded = encodeURI('/axis/'+ $scope.activeaxis+ '/placeMembersOnAxis?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promisePost
		("1.0",encoded,visibleSelected)
		.then(function(response) {
			 visibleSelected = [];			
			 $scope.handleResponse(response);
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
		var toSend ={ 
				'axis':axis,
				'hierarchy': hierarchieUniqeName,
				'newPosition':newPosition,
				'direction':direction
		}
		var encoded = encodeURI('1.0/axis/moveHierarchy?SBI_EXECUTION_ID=' + JSsbiExecutionID);
		sbiModule_restServices.promisePost(
				encoded, "", toSend)
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
		if(name.toLowerCase().indexOf($scope.searchSucessText.toLowerCase()) > -1)
			return true;
		else
			return false		
	};		
			
	$scope.showHideSearchOnFilters = function(){		
		$scope.showSearchInput = !$scope.showSearchInput;		
	};
	
	$scope.hideAsyncTree = function(item){
		item.collapsed = false;
	};
	
	$scope.openFiltersDialog = function(ev, filter, node) {
		$scope.clearLoadedData(filter.name);
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
		
	};
	
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
				if ($scope.draggedFrom == 'left' && leftLength == 1){
					sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.execution.table.dimension.no.enough'), typeMsgWarn);
				}					
				else {
					data.positionInAxis = topLength;
					data.axis = 0;

					$scope.putMemberOnAxis(fromAxis,data);
				}
			}
		}
		if(data!= null)
			$scope.clearLoadedData(data.uniqueName);
	};

	
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
				if ($scope.draggedFrom == 'top' && topLength == 1)
					sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.execution.table.dimension.no.enough'), typeMsgWarn);
				else {
					data.positionInAxis = leftLength;
					data.axis = 1;
					$scope.putMemberOnAxis(fromAxis,data);
				}
				
			}
		}
		if(data!= null)
			$scope.clearLoadedData(data.uniqueName);
	};

	$scope.dropFilter = function(data, ev) {
		var leftLength = $scope.rows.length;
		var topLength = $scope.columns.length;
		var fromAxis;
		
		if(data != null){
			fromAxis = data.axis;
			
			if(data.measure){
				sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.execution.table.filter.no.measure'), typeMsgWarn);
				return null;
			}
			
			if(fromAxis!=-1){			
				
				if ($scope.draggedFrom == 'left' && leftLength == 1)
					sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.execution.table.dimension.no.enough'), typeMsgWarn);
				else if ($scope.draggedFrom == 'top' && topLength == 1)
					sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.execution.table.dimension.no.enough'), typeMsgWarn);
				else {
					data.positionInAxis = $scope.filterCardList.length;
					data.axis = -1;
					
					$scope.putMemberOnAxis(fromAxis,data);
				}
				
				$scope.filterSelected[$scope.filterSelected.length] = {name:"...",uniqueName:"",visible:false};
			}
		}
		if(data!=null)
			$scope.clearLoadedData(data.uniqueName);
	};

	$scope.dragSuccess = function(df, index) {
		$scope.draggedFrom = df;
		$scope.dragIndex = index;
	};
		
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
	};
	
	$scope.openFilters = function(ev) {
		$mdDialog.show($mdDialog.alert().clickOutsideToClose(true).title(
				"Here goes filtering").ok("ok").targetEvent(ev));
	};

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
	};
	
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
	};
	
	checkShift = function(){
		$scope.shiftNeeded = $scope.filterCardList.length > $scope.numVisibleFilters ? true
				: false;
		
		$scope.topSliderNeeded = $scope.columns.length > $scope.maxCols? true : false;
		
		$scope.leftSliderNeeded = $scope.rows.length > $scope.maxRows? true : false;
	};
	
	//Initializing array filterSelected that is following selected dimension in filters 
	$scope.initFilterList = function (){
		$scope.filterSelected = [];
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
			$scope.initFilterList();
			
			$scope.sendModelConfig($scope.modelConfig);
			if($scope.modelConfig.whatIfScenario)
				$scope.getVersions();
			axisSizeSetup();
			
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while sending MDX query", 'Error');
			
		});	
	};
	
	$scope.bgColor = function(){
		if( $scope.searchText == "" || $scope.searchText.length>=  $scope.minNumOfLetters)
			return false;
		else	
			return true;
	};
	
	$scope.cutName = function(name){
		var result = name.split(" ");
		
		if(name.length < 12)
			return name;
		if(result[0].length>13){
			return result[0].substring(0,13)
		}
		else if(result[1]!=undefined){
			if(result[1].length > 3){
				var res = result[1].substring(0,3);
				return result[0]+" "+res+"...";
			}
		}
	};
	
	updateFilterTracker = function(){
		var oldSelected = $scope.filterSelected;
		$scope.initFilterList();
		for(var i=0; i<oldSelected.length;i++){			
			for(var j=0; j<$scope.filterCardList.length;j++){
				if(oldSelected[i].uniqueName.indexOf($scope.filterCardList[j].uniqueName)>-1){
					$scope.filterSelected[j] = oldSelected[i];
				}
			}
			
		}
	};
	
	axisSizeSetup = function(){
		var taw = document.getElementById("topaxis").offsetWidth - 66;
		var lah = document.getElementById("leftaxis").offsetHeight - 66;
		var faw = document.getElementById("filterpanel").offsetWidth - 80;
		$scope.maxCols = Math.round(taw/200);
		$scope.maxRows = Math.round(lah/165);
		$scope.numVisibleFilters = Math.round(faw/200);

	};
};