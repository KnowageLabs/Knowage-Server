var olapMod = angular.module('olap.controllers', [
                                                  'olap.configuration',
                                                  'olap.directives'
                                                  ])

olapMod.controller("olapController", [
                                      "$scope", 
                                      "$timeout", 
                                      "$window",
                                      "$mdDialog", 
                                      "$http",
                                      '$sce',
                                      '$mdToast',
                                      'sbiModule_messaging',
                                      'sbiModule_restServices',
                                       olapFunction 
                                      ]);

function olapFunction(
		$scope, 
		$timeout, 
		$window, 
		$mdDialog, 
		$http, 
		$sce,
		$mdToast,
		sbiModule_messaging,
		sbiModule_restServices
) {
	
	templateRoot = "/knowagewhatifengine/html/template";
	$scope.sendMdxDial = "/main/toolbar/sendMdx.html";
	$scope.showMdxDial = "/main/toolbar/showMdx.html";
	$scope.sortSetDial = "/main/toolbar/sortingSettings.html";
	$scope.filterDial = "/main/filter/filterDialog.html"
	
	$scope.minNumOfLetters=4;
	$scope.searchText="";
	$scope.searchSucessText;
	$scope.showSearchInput=false;
	$scope.openLeftMenu=false;
		
	$scope.rows;
	$scope.maxRows = 3;
	$scope.topSliderNeeded;
	$scope.topStart = 0;
	
	$scope.columns;
	$scope.maxCols = 5;
	$scope.leftSliderNeeded;
	$scope.leftStart = 0;
	
	$scope.toolbarButtons = [];
	$scope.filterCardList = [];
	$scope.filterSelected = [];
	$scope.dtData = [];
	$scope.dtTree = [];
	$scope.dtMaxRows= 0;
	$scope.ord = null;
	$scope.dtAssociatedLevels= [];
	$scope.isFilterSelected = false;
	$scope.filterAxisPosition;
	$scope.showMdxVar = "";

	$scope.draggedFrom = "";
	$scope.dragIndex;
	
	$scope.doneonce =false;
	$scope.level;
	$scope.data=[];
	$scope.loadedData = [];
	$scope.dataPointers = [];
	$scope.numVisibleFilters = 5;
	$scope.shiftNeeded;
	
	$scope.modelConfig;
	$scope.filterDialogToolbarName;
	
	$scope.showSiblings = true;
	$scope.sortingSetting;
	$scope.ready = true;
	$scope.sortingEnabled = false;
	$scope.sortingModes = [{'label':'basic','value':'basic'},{'label':'breaking','value':'breaking'},{'label':'count','value':'count'}];
	$scope.selectedSortingMode = 'basic';
	$scope.sortingCount = 10;
	$scope.saveSortingSettings = function(){
		$mdDialog.hide();
		$scope.sortDisable();
	}
	$scope.loadingNodes=false;
	$scope.activeaxis;
	var filterFather;
	var h;
	var m;
	var oldSelectedFilter="";
	var visibleSelected = [];
	var hlght = false;
	
	$scope.handleResponse = function(response) {
		source = response.data;
		$scope.table = $sce.trustAsHtml(source.table)
		$scope.columns = source.columns;
		$scope.rows = source.rows;
		$scope.columnsAxisOrdinal = source.columnsAxisOrdinal;
		$scope.filterCardList = source.filters;
		$scope.hasPendingTransformations = source.hasPendingTransformations;
		$scope.modelConfig = source.modelConfig;
		$scope.rowsAxisOrdinal = source.rowsAxisOrdinal;
		$scope.showMdxVar = source.mdxFormatted;
	}
	
	$scope.toggleMenu=function(){
		
		$scope.openLeftMenu=!$scope.openLeftMenu;
	};

	 /* service for moving hierarchies* */
	$scope.moveHierarchies = function(axis, hierarchieUniqeName, newPosition,
			direction, member) {

		sbiModule_restServices.promisePost(
				'1.0/axis/' + axis + '/moveHierarchy/' + hierarchieUniqeName
						+ '/' + newPosition + '/' + direction
						+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID, "", member)
				.then(
						function(response) {
							$scope.handleResponse(response);
						},
						function(response) {
							sbiModule_messaging.showErrorMessage(
									"An error occured while movin hierarchy",
									'Error');

						});
	}

	$scope.startFrom = function(x,y) {
		if ($scope.ready) {
			$scope.ready = false;
	
			sbiModule_restServices.promiseGet(
					"1.0",
					'/member/start/'+x+'/' + y + '?SBI_EXECUTION_ID='
							+ JSsbiExecutionID).then(function(response) {
				$scope.table = $sce.trustAsHtml(response.data.table);
				$scope.ready = true;
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');
	
			});
		}
	}
	
	/**dragan*/
	angular.element(document).ready(function() {
		$scope.sendMdxQuery('null');
	});

	/****************************************************************************/
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
		} else {
			for (var i = length - 2; i >= 0; i--) {
				$scope.filterCardList[i + 1] = $scope.filterCardList[i];
			}
			$scope.filterCardList[0] = last;
		}

	}
	
	$scope.sendMdxQuery = function(mdx) {
		sbiModule_restServices.promisePost("1.0/model/?SBI_EXECUTION_ID="+JSsbiExecutionID,"",mdx)
		.then(function(response) {
			$scope.handleResponse(response);
			checkShift();
			$mdDialog.hide();
			$scope.mdxQuery = "";
			initFilterList();
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while sending MDX query", 'Error');
			
		});	
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
	
	$scope.getHierarchyMembersAsynchronus = function(hierarchy,axis,node,id){
		sbiModule_restServices.promiseGet
		("1.0",'/hierarchy/'+ hierarchy+ '/filtertree/'+ axis+ '?SBI_EXECUTION_ID='+ JSsbiExecutionID+ '&node='+node)
		.then(function(response) {
			  
			  if(node!=null)
				  expandAsyncTree($scope.data,response.data, id);
			  else{
				  checkIfExists(response.data);
			  }
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while getting hierarchy members", 'Error');	
		});	
	}

	$scope.openFilters = function(ev) {
		$mdDialog.show($mdDialog.alert().clickOutsideToClose(true).title(
				"Here goes filtering").ok("ok").targetEvent(ev));
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
	
	/**
	 * Dialogs  
	 **/
	$scope.openFiltersDialogAsync = function(ev, filter, node) {
		$scope.filterDialogToolbarName = filter.name;
		$scope.filterAxisPosition = filter.positionInAxis;
		$scope.activeaxis = filter.axis;
		filterFather = filter.uniqueName;
		h = filter.uniqueName;
		var exist = false;
		
		for(var i = 0; i< $scope.dataPointers.length;i++){
			if($scope.dataPointers[i] == filterFather){
				exist = true;
				$scope.data= $scope.loadedData[i];
				if($scope.activeaxis >= 0){
					getVisible($scope.data);
				}
			}
		}
		if(!exist){
			$scope.getHierarchyMembersAsynchronus(filterFather, filter.axis, null,filter.id);
			$scope.dataPointers.push(filterFather);
		}
		$scope.showDialog(ev,$scope.filterDial);
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
	 *Function for opening dialogs
	 **/
	$scope.showDialog = function(ev,path){
		$mdDialog
		.show({
			scope : $scope,
			preserveScope : true,
			controllerAs : 'olapCtrl',
			templateUrl : templateRoot+path,
			targetEvent : ev,
			clickOutsideToClose : false
		});
	}
	
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
	
	/**
	 *Tree functionalities 
	 **/
	$scope.expandTreeAsync = function(item){
		$scope.getHierarchyMembersAsynchronus(filterFather,$scope.activeaxis,item.uniqueName,item.id);
		
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
	
	$scope.hideAsyncTree = function(item){
		item.collapsed = false;
	}
	
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
	
	$scope.selectFilter = function(item){
		oldSelectedFilter = $scope.filterSelected[$scope.filterAxisPosition].name;
		h = $scope.filterCardList[$scope.filterAxisPosition].uniqueName;
		m = item.uniqueName;
		$scope.filterSelected[$scope.filterAxisPosition].name = item.name;
		$scope.filterSelected[$scope.filterAxisPosition].uniqueName = item.uniqueName;
		
	};
	
	$scope.filterDialogSave = function(){
		if($scope.activeaxis == -1)
			filterSlice();
		else
			filterPlaceMemberOnAxis();
		
		$mdDialog.hide();
	}
	
	filterSlice = function(){
		if(h != undefined && m!= undefined){
			sbiModule_restServices.promiseGet
			("1.0",'/hierarchy/'+ h+ '/slice/'+ m + '/'+ false + '?SBI_EXECUTION_ID='+ JSsbiExecutionID)
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
		console.log(visibleSelected);
		sbiModule_restServices.promisePost
		("1.0",'/axis/'+ $scope.activeaxis+ '/placeMembersOnAxis?SBI_EXECUTION_ID='+ JSsbiExecutionID,visibleSelected)
		.then(function(response) {
			 visibleSelected = [];			
			 $scope.table = $sce.trustAsHtml(response.data.table);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
			
		});
	};
	
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
	
	//Called to get visible elements row/column
	getVisible = function(data){
		for(var i=0;i<data.length;i++){
			if(data[i].visible){
				visibleSelected.push(data[i]);
			}
			if(data[i].children != undefined){
				getVisible(data[i].children);
			}
		}
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
	
	$scope.searchFilter = function(){
		$scope.loadingNodes = true;
		hlght = true;
		sbiModule_restServices.promiseGet
		("1.0",'/hierarchy/'+ h+ '/search/'+$scope.activeaxis+'/'+$scope.searchText+'/'+$scope.showSiblings+'?SBI_EXECUTION_ID='+ JSsbiExecutionID)
		.then(function(response) {
			//if(response.data[0].children.length != 0)
				checkIfExists(response.data);
				$scope.searchSucessText = $scope.searchText.toLowerCase();
				$scope.loadingNodes = false;
			//else
				//sbiModule_messaging.showWarningMessage("Sorry. Match not found for '"+$scope.searchText+"'", 'Warning');
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
						getVisible($scope.data);
				}
			}
		}
		if(!exist)
			$scope.dataPointers.push(filterFather);
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
	
	$scope.closeDialog = function(e){
		$mdDialog.hide();
	};
}