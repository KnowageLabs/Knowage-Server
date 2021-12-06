/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {


angular.module('filter_panel',['sbiModule','olap.services'])
.directive('filterPanel',function(sbiModule_config){
	return{
		restrict: "E",
		replace: 'true',
//		templateUrl: '/knowagewhatifengine/html/template/main/filter/filterPanel.html',
		templateUrl: function(){
	    	 return sbiModule_config.contextName+'/html/template/main/filter/filterPanel.html';

	      },
		controller: filterPanelController
	}
})

.directive('topaxis',function(sbiModule_config){
	var buttonsDimension = 64;

	function manageTopSlider(element,maxWidth){
		var elements = element[0].querySelectorAll('.top-axis-element');
		var totalWidth = 0;
		elements.forEach(function(item){
			totalWidth += item.clientWidth;
		})
		return (totalWidth + buttonsDimension) > maxWidth;
	}

	function link(scope,element,attrs){
		scope.$watch(function(){
			return element[0].offsetWidth;
		},function(newValue,oldValue){scope.topSliderNeeded = manageTopSlider(element,newValue)})

		scope.$watch(function(){
			return element[0].querySelectorAll('.top-axis-element').length;
		},function(newValue,oldValue){scope.topSliderNeeded = manageTopSlider(element,element[0].offsetWidth)})
	};
	return{
		restrict: "A",
		link:link
	}
})

.directive('leftaxis',function(sbiModule_config){
	var buttonsDimension = 64;

	function manageLeftSlider(element,maxHeight){
		var elements = element[0].querySelectorAll('.left-axis-element');
		var totalHeight = 0;
		elements.forEach(function(item){
			totalHeight += item.clientHeight;
		})
		return (totalHeight + buttonsDimension) > maxHeight;
	}

	function link(scope,element,attrs){
		scope.$watch(function(){
			return element[0].offsetHeight;
		},function(newValue,oldValue){scope.leftSliderNeeded = manageLeftSlider(element,newValue)})

		scope.$watch(function(){
			return element[0].querySelectorAll('.left-axis-element').length;
		},function(newValue,oldValue){scope.leftSliderNeeded = manageLeftSlider(element,element[0].offsetHeight)})
	};
	return{
		restrict: "A",
		link:link
	}
})

.directive('filterpanel',function(sbiModule_config){
	var buttonsDimension = 64;

	function manageTopSlider(element,maxWidth){
		var elements = element[0].querySelectorAll('.new-filter-card');
		var totalWidth = 0;
		elements.forEach(function(item){
			totalWidth += item.clientWidth;
		})
		return (totalWidth + buttonsDimension) > maxWidth;
	}

	function link(scope,element,attrs){
		scope.$watch(function(){
			return element[0].offsetWidth;
		},function(newValue,oldValue){scope.filterSliderNeeded = manageTopSlider(element,newValue)})

		scope.$watch(function(){
			return element[0].querySelectorAll('.new-filter-card').length;
		},function(newValue,oldValue){scope.filterSliderNeeded = manageTopSlider(element,element[0].offsetWidth)})
	};
	return{
		restrict: "A",
		link:link
	}
})


function filterPanelController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate, sbiModule_config,sbiModule_docInfo, toastr, indexChangingService,hierarchyTreeService,FiltersService,$httpParamSerializer) {


	var visibleSelectedTracker = [];
	var filterFather;
	var h;
	var m;
	var oldSelectedFilter="";
	var hlght = false;
	var selectedFlag = false;
	$scope.hierarchyTreeService = hierarchyTreeService;
	$scope.FiltersService = FiltersService;
	var cutArray = [12, 11, 10, 9, 6]; //array with maximum lengths for card
	$scope.selectView = false;

	$scope.currentSelectedMembers = [];


	var typeMsgWarn =sbiModule_translate.load('sbi.common.warning');
	$scope.loadingFilter = true;
	$scope.filterPanelEmpty = sbiModule_translate.load('sbi.olap.execution.table.filter.empty');

	angular.element(document).ready(function() {
		$scope.sendMdxQuery('null');

	});



	// for designer parameters binding
	$scope.adParams=[];
	$scope.profileAttributes=[];

	$scope.lastSelectedFilter=undefined;
	$scope.selectedAttribute= undefined;
	$scope.bindMode=false;
	$scope.isSlicer=true;
	$scope.parametersLoaded= false;

	$scope.unSelectAll = function(tree){
		hierarchyTreeService.setVisibilityForAll(tree,false);
		tree[0].visible = false;
		$scope.currentSelectedMembers = [];
	}

	$scope.parameterBindings=[];

	$scope.clearLoadedData = function(name){
		for(var i=0; i< $scope.dataPointers.length; i++){
			if(name == $scope.dataPointers[i]){
				$scope.dataPointers.splice(i,1);
				$scope.loadedData.splice(i,1)
				break;
			}
		}

		var visibleSelectedTracker = [];
	};

	clearSelectedList = function(){

		for(var i=0;i<visibleSelectedTracker.length;i++){
			if(visibleSelectedTracker[i].id == undefined || visibleSelectedTracker[i].id.indexOf(filterFather) == 1){
				visibleSelectedTracker.splice(i,1);
			}
		}
	};



	/**
	 * Dialogs
	 **/
	//Function for opening dialogs for every axis
	$scope.openFiltersDialogAsync = function(ev, filter, node, index) {

		$scope.clearLoadedData(filter.uniqueName);

		visibleSelectedTracker = [];//check it
		$scope.searchText = "";
		$scope.loadingFilter = true;
		var x = {name:'Waiting...'};
		$scope.data = [];
		$scope.data.push(x);


		if(filter.axis > -1){
			$scope.isSlicer=false;
		}
		$scope.filterDialogToolbarName = filter.caption;
		$scope.filterAxisPosition = index;
		$scope.activeaxis = filter.axis;
		filterFather = filter.selectedHierarchyUniqueName;
		$scope.selectedHierarchyUniqueName = filter.selectedHierarchyUniqueName;
		h = filter.uniqueName;

		$scope.selectedAttribute= undefined;

		var loadHierarchy= true;
		for (var i = 0; i < $scope.adParams.length; i++) {
			if($scope.adParams[i].bindObj != null){
			if( $scope.adParams[i].bindObj.filter.dimension === filter.uniqueName){
				$scope.data=$scope.adParams[i].bindObj.tree;
				$scope.selectedAttribute= $scope.adParams[i].bindObj.attribute;
				$scope.bindMode=true;
				loadHierarchy= false;
				break;
			}
			}
		}

		for (var i = 0; i < $scope.profileAttributes.length; i++) {
			if($scope.profileAttributes[i].bindObj != null){
			if( $scope.profileAttributes[i].bindObj.filter.dimension === filter.uniqueName){
				$scope.data=$scope.profileAttributes[i].bindObj.tree;
				$scope.selectedAttribute= $scope.profileAttributes[i].bindObj.attribute;
				$scope.bindMode=true;
				loadHierarchy= false;
				break;
			}
			}
		}

            if(loadHierarchy){
            	if ($scope.isSlicer) {
            		$scope.getSlicerTree();
            	} else {
            		$scope.findVisibleMembers(filterFather);
            	}

            	$scope.dataPointers.push(filterFather);
            }



		$scope.showDialog(ev,$scope.filterDial);

		$scope.checkSlicerSelection();

		$scope.loadingFilter = false;


	};

	/**
	 *Tree functionalities
	 **/
	$scope.expandTreeAsync = function(item){

		if ($scope.selectView) {
			item.collapsed = true;
			return;
		}

		if ($scope.bindMode){
			sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.attributeBinding.warning'), 'Warning');
		}else{
			$scope.getHierarchyMembersAsynchronus(filterFather,$scope.activeaxis,item.uniqueName,item.id);
		}
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
		 $scope.members = [];
		 var toSend = {
				 'fromAxis':fromAxis,
				 'hierarchy':member.selectedHierarchyUniqueName,
				 'toAxis': member.axis
		 }
		 var encoded = encodeURI('1.0/axis/moveDimensionToOtherAxis?SBI_EXECUTION_ID='+JSsbiExecutionID);
		 sbiModule_restServices.promisePost
		 (encoded,"",toSend)
			.then(function(response) {
				$scope.handleResponse(response);
				//checkShift();
				if(fromAxis == 1){
					$scope.leftStart = 0;
				}
				if(fromAxis == 0){
					$scope.topStart = 0;
				}
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.memberAxis.error'), 'Error');

			});
	}

	$scope.searchFilter = function(keyEvent){
		if (!keyEvent || keyEvent.which === 13 ){
			hlght = true;
			var toSend = {
				'hierarchy':filterFather,
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
					//$scope.hierarchyTreeService.setIsSlicer(response.data,filterFather)
					var visibleMembers = $scope.hierarchyTreeService.getVisibleMembers($scope.data);
					visibleMembers.forEach(function (aMember) {
						$scope.updateSelectedMembers(aMember);
					});
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.filterSearch.error'), 'Error');
			});
		}

	};

	$scope.getSlicerTree = function(){
		var queryParams = {};
		queryParams.SBI_EXECUTION_ID = JSsbiExecutionID;
		var body = {};
		body.hierarchyUniqueName = filterFather;
		sbiModule_restServices.promisePost("1.0/hierarchy/slicerTree?" + $httpParamSerializer(queryParams),"",body)
			.then(function(response) {
				checkIfExists(response.data);
				$scope.selectView = hierarchyTreeService.isAnyVisible($scope.data);
				// update current selected members array
				$scope.currentSelectedMembers = $scope.hierarchyTreeService.getVisibleMembers($scope.data);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.filterSearch.error'), 'Error');
			});
	}

	$scope.findVisibleMembers = function(hierarchy) {
		var queryParams = {};
		queryParams.SBI_EXECUTION_ID = JSsbiExecutionID;
		var body = {
				'hierarchy': hierarchy
		};
		sbiModule_restServices.promisePost("1.0/hierarchy/visibleMembers?" + $httpParamSerializer(queryParams),"",body)
			.then(function(response) {
				checkIfExists(response.data);
				$scope.selectView = hierarchyTreeService.isAnyVisible($scope.data);
				// update current selected members array
				$scope.currentSelectedMembers = $scope.hierarchyTreeService.getVisibleMembers($scope.data);
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.filterSearch.error'), 'Error');
			});
	}

	$scope.isAnySelected = function(){
		if (!$scope.isSlicer) {
			// in case of dimensions on axis, there is always at least 1 selected member
			return true;
		}
		return $scope.FiltersService.getSlicers(filterFather).length > 0;
	}

	$scope.setAllView = function(){
		hierarchyTreeService.setCollapsedForAll($scope.data,true);
	}

	$scope.setSelectedView = function(){
    	if ($scope.isSlicer) {
    		$scope.getSlicerTree();
    	} else {
    		$scope.findVisibleMembers(filterFather);
    	}
	}

	$scope.toggleViewMode = function(){
		if ($scope.selectView) {
			$scope.setSelectedView();
		} else {
			$scope.setAllView();
		}
	}

	checkIfExists = function(data){
		var exist = false;
		for(var i = 0; i< $scope.dataPointers.length;i++){
			if($scope.dataPointers[i] == filterFather){
					exist = true;
					$scope.loadedData[i] = data
					$scope.data= $scope.loadedData[i];

			}
		}
		if(!exist){
			$scope.data= data;
			$scope.dataPointers.push(filterFather);
		}

	};

	$scope.getHierarchyMembersAsynchronus = function(hierarchy,axis,node,id){

		var toSend={
			'hierarchy':hierarchy,
			'axis':axis,
			'node':node
		}
		var encoded = encodeURI('1.0/hierarchy/filtertree?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promisePost
		(encoded,"",toSend)
		.then(function(response) {

			  if(node!=null){
				  var shouldSearchVisible = true;

				  expandAsyncTree($scope.data,response.data, id);

				  for(var j = 0; j< visibleSelectedTracker.length;j++){
					if(visibleSelectedTracker[j].id == h  && visibleSelectedTracker[j].selected.length > 0)
						shouldSearchVisible= false;
					}

			  }
			  else{
				  checkIfExists(response.data);
			  }

			  var root = $scope.data[0];
			  $scope.checkSlicerSelection(root);

		}, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.hierarchyGet.error'), 'Error');
		});
	}


	//selecting filter (old selected filter is saved in order to leave interface consistent if user decide to cancel selection)
	$scope.selectFilter = function(item){
		console.log(item);



		   if(!$scope.bindMode && $scope.selectedAttribute){
			$scope.bindMode=true;
			  $scope.selectedAttribute.bindObj= item;
			var sliceArray= item.uniqueName.split(".");
			$scope.selectedAttribute.replace =sliceArray[0];
			$scope.filterSelected[$scope.filterAxisPosition].dimension= sliceArray[0];
			$scope.filterSelected[$scope.filterAxisPosition].replaceItem= item.name;


			var index= item.uniqueName.indexOf("["+item.name+"]");
			var fatherUniqueName=item.uniqueName.substring(0,index-1);

			replaceChildrenWithParameter($scope.data[0],item.uniqueName,$scope.selectedAttribute);
			$scope.selectedAttribute.bindObj.tree=$scope.data;

		selectedFlag = true;
		oldSelectedFilter = angular.copy($scope.filterSelected[$scope.filterAxisPosition]);//ex:$scope.filterAxisPosition
		h = $scope.filterCardList[$scope.filterAxisPosition].uniqueName;
		m = item.uniqueName;

		$scope.filterSelected[$scope.filterAxisPosition].uniqueName = item.uniqueName;

		   }else{
			   if(!$scope.bindMode){
			   selectedFlag = true;
				oldSelectedFilter = angular.copy($scope.filterSelected[$scope.filterAxisPosition]);//ex:$scope.filterAxisPosition
				h = $scope.filterCardList[$scope.filterAxisPosition].uniqueName;
				m = item.uniqueName;
				$scope.filterSelected[$scope.filterAxisPosition].caption = item.name;
				$scope.filterSelected[$scope.filterAxisPosition].uniqueName = item.uniqueName;
			   }else{
				   sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.attributeBindingSelection.warning'), 'Warning');
			   }
		   }




	};

	function replaceChildrenWithParameter(node,name, parameter){
		if(node.children){
			for (var i = 0; i < node.children.length; i++) {
				if(node.children[i].uniqueName== name){
					node.children=[];
					var pNode={};
					if(parameter.label){
				    pNode.name="${"+parameter.label+"}";
				    pNode.uniqueName = parameter.label;
					}
					if(parameter.attributeName){
						 pNode.name="${"+parameter.attributeName+"}";
						    pNode.uniqueName = parameter.attributeName;
					}
				    pNode.colapsed = true;
				    pNode.id=parameter.label;
				    pNode.leaf=true;
				    node.children.push(pNode);
				    break;
				}else{

						for (var j = 0; j < node.children.length; j++) {
							replaceChildrenWithParameter(node.children[j],name,parameter);
						}

			}
			}

		}
	}



	//Function for closing filter dialog (handling selected in order to leave interface in consistent state)
	$scope.closeFiltersDialog = function() {

		if(selectedFlag){
			if(oldSelectedFilter.caption != "..."){
				$scope.filterSelected[$scope.filterAxisPosition].caption = oldSelectedFilter.caption;
				$scope.filterSelected[$scope.filterAxisPosition].uniqueName = oldSelectedFilter.uniqueName;
				$scope.filterSelected[$scope.filterAxisPosition].bindedAttribute= undefined;
			}
			else{
				$scope.filterSelected[$scope.filterAxisPosition].caption = "...";
				$scope.filterSelected[$scope.filterAxisPosition].uniqueName = "";
				$scope.filterSelected[$scope.filterAxisPosition].bindedAttribute= undefined;
			}

			selectedFlag = false;
			//$scope.bindMode=false;

		}

		if($scope.selectedAttribute){
			$scope.selectedAttribute.replace='';
			$scope.selectedAttribute.bindObj=null;
			$scope.selectedAttribute=undefined;
			}

		$scope.bindMode=false;
		$scope.searchText = "";
		hlght = false;
		$scope.isSlicer=true;
		// reset current selected members array
		$scope.currentSelectedMembers = [];
		$mdDialog.hide();
	}

	$scope.filterDialogSave = function(){
		if($scope.activeaxis == -1){

			if($scope.olapMode){
				$scope.FiltersService.setSlicers(filterFather,[$scope.filterSelected[$scope.filterAxisPosition]])
			}else{
				$scope.FiltersService.setSlicers(filterFather, $scope.currentSelectedMembers);

			}
			if($scope.selectedAttribute){
				var bind= {};
				bind.filter=angular.copy($scope.filterSelected[$scope.filterAxisPosition]);
				bind.attribute= angular.copy($scope.selectedAttribute);
				bind.tree= angular.copy($scope.data);

				$scope.selectedAttribute.bindObj=bind;
				$scope.parameterBindings.push(bind);
			}

			filterSlice();
			hlght = false;
			$mdDialog.hide();
		}
		else

			if ($scope.currentSelectedMembers.length > 0) {
				filterPlaceMemberOnAxis();
				$scope.bindMode=false;
				selectedFlag = false;
				$scope.isSlicer=true;
				hlght = false;
				$mdDialog.hide();
			}else{

				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.filtering.no.selected.members'))
			}

	}

	//Save action called from filters axis=-1
	filterSlice = function(){

		var toSend = {
			'hierarchy':filterFather,
			'members':$scope.FiltersService.getSlicerUniqueNames(filterFather),
			'multi':false
		};

		console.log(toSend);

		if(filterFather != undefined && toSend.members!= undefined){
			var encoded = encodeURI('1.0/hierarchy/slice?SBI_EXECUTION_ID='+ JSsbiExecutionID);
			sbiModule_restServices.promisePost
			(encoded,"",toSend)
			.then(function(response) {
				  $scope.handleResponse(response);
				  $scope.selectedVersion=response.data.modelConfig.actualVersion;
				  $scope.filterSelected[$scope.filterAxisPosition].visible = true;//ex:$scope.filterAxisPosition
			}, function(response) {
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.generic.error'), 'Error');
			});
		}
	};

	//save action called from rows/columns axis=0/axis=1
	filterPlaceMemberOnAxis = function(){

		clearSelectedList();
		var membersToSend = [];
		$scope.currentSelectedMembers.forEach(function (aMember) {
			var aMemberCopy = angular.copy(aMember);
			delete aMemberCopy.collapsed;
			delete aMemberCopy.children;
			membersToSend.push(aMemberCopy);
		});
		var encoded = encodeURI('1.0/axis/'+ $scope.activeaxis+ '/placeMembersOnAxis?SBI_EXECUTION_ID='+ JSsbiExecutionID);
		sbiModule_restServices.promisePost
		(encoded, "", membersToSend)
		.then(function(response) {

			 $scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.memberAxis.error'), 'Error');

		});
	};


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
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.hierarchyMove.error'), 'Error');
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
		if($scope.bindMode){
			sbiModule_messaging.showWarningMessage(sbiModule_translate.load('sbi.olap.attributeBinding.warning'), 'Warning');
		}else{
		item.collapsed = false;
		}
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
				$scope.filterSelected[data.positionInAxis].caption ="...";
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
		//$scope.axisSizeSetup();
		if(data !=null){
			fromAxis = data.axis;

			if(fromAxis == -1){
				$scope.filterSelected[data.positionInAxis].caption ="...";
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

				$scope.filterSelected[$scope.filterSelected.length] = {caption:"...",uniqueName:"",visible:false};
			}
		}
		if(data!=null)
			$scope.clearLoadedData(data.uniqueName);
	};

	$scope.dragSuccess = function(df, index) {
		$scope.draggedFrom = df;
		$scope.dragIndex = index;
	};

	$scope.openFilters = function(ev) {
		$mdDialog.show($mdDialog.alert().clickOutsideToClose(true).title(
				sbiModule_translate.load('sbi.olap.filtering.info')).ok("ok").targetEvent(ev));
	};

	$scope.checkSlicerSelection = function() {
		if ($scope.isSlicer) {
			// in case of a slicer, we check if user selected members in ancestor-descendant relationship, to display warning in that case, since this means aggregating values for those members
			var root = $scope.data[0];
			var hasSelectedDescendant = $scope.hasSelectedAncestorsAndDescendant(root, false);
			$scope.ancestorAndDescendantSlicerSelection = hasSelectedDescendant;
		} else {
			$scope.ancestorAndDescendantSlicerSelection = false;
		}
	}

	$scope.hasSelectedAncestorsAndDescendant = function (item, ancestorIsSelected) {
		var visible = item.visible;
		if (visible && ancestorIsSelected) {
			return true;
		}
		var children = item.children;
		for (index in children) {
			var child = children[index];
			var hasSelectedAncestorsAndDescendant = $scope.hasSelectedAncestorsAndDescendant(child, ancestorIsSelected || visible);
			if (hasSelectedAncestorsAndDescendant) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Filter shift if necessary
	 **/
	//Function for scrolling trough filters/rows/columns if necessary
	$scope.filterShift = function(direction, index, array, numVisibleFilters) {

		$scope.index = indexChangingService.changeIndexValue(direction, index, array, numVisibleFilters);

	};

	//setting visibility of shift buttons if needed
//	checkShift = function(){
//
//
//		$scope.shiftNeeded = $scope.filterCardList.length > $scope.numVisibleFilters ? true
//				: false;
//
//		$scope.topSliderNeeded = $scope.columns.length > $scope.maxCols? true : false;
//
//		$scope.leftSliderNeeded = $scope.rows.length > $scope.maxRows? true : false;
//	};


	$scope.sendMdxQuery = function(mdx) {
		var encoded = encodeURI("1.0/model/?SBI_EXECUTION_ID="+JSsbiExecutionID)
		sbiModule_restServices.promisePost(encoded,"",mdx)
		.then(function(response) {
			$scope.handleResponse(response);
			//checkShift();
			$mdDialog.hide();
			$scope.mdxQuery = "";

			$scope.sendModelConfig($scope.modelConfig);
			 checkLock(status);
			if($scope.modelConfig.whatIfScenario)
				$scope.getVersions();
			//$scope.axisSizeSetup();

		}, function(response) {
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.sendMDX.error'), 'Error');

		});
	};

	//Function for styling of search text box
	$scope.bgColor = function(){
		if( $scope.searchText == "" || $scope.searchText.length>=  $scope.minNumOfLetters)
			return false;
		else
			return true;
	};

	$scope.cutName = function(name, axis, multi){
		var ind = axis;
		if(multi)
			ind = ind + 2;

		ind = ind+1;

		var cutProp = cutArray[ind];

		if(name == undefined){
			name = oldSelectedFilter.caption;
		}

		if(name.length <= cutProp)
			return name;
		else
			return name.substring(0,cutProp)+"...";


	};



	//Dynamic setting for number of visible elements in filter/row/column axis without scroll buttons
	//depends on size of actual html elements
//	$scope.axisSizeSetup = function(){
//		if(document.getElementById("topaxis")){
//			var taw = document.getElementById("topaxis").offsetWidth - 66;
//			$scope.maxCols = Math.round(taw/200);
//		}
//
//		if(document.getElementById("leftaxis")){
//			var lah = document.getElementById("leftaxis").offsetWidth - 66;
//			$scope.maxRows = Math.round(lah/175);
//		}
//
//		if(document.getElementById("filterpanel")){
//			var faw = document.getElementById("filterpanel").offsetWidth - 80;
//			$scope.numVisibleFilters = Math.round(faw/200);
//		}
//
//	};

	$scope.loadAnalyticalDrivers= function(){
		   console.log(sbiModule_docInfo);
		   sbiModule_restServices
			.alterContextPath("/knowage");
		   sbiModule_restServices.promiseGet("1.0/documents/",
					sbiModule_docInfo.label+"/parameters")
					.then(
							function(response) {
							     console.log(response.data);
							     $scope.adParams=[];
							     $scope.adParams=response.data.results;

							     for (var i = 0; i < $scope.adParams.length; i++) {
									$scope.adParams[i].replace = '';
									$scope.adParams[i].bindObj= null;
								}

							},
							function(response) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.ad.error'), 'Error');
							});



	   }

	   if(mode == 'edit'){
	   $scope.loadAnalyticalDrivers();
	   }

	   $scope.loadProfileAttributes= function (){

		   sbiModule_restServices
			.alterContextPath("/knowage");
		   sbiModule_restServices.promiseGet("2.0/attributes", "")
					.then(
							function(response) {


							    $scope.profileAttributes= response.data;

							    for (var i = 0; i < $scope.profileAttributes.length; i++) {
									$scope.profileAttributes[i].replace = '';
									$scope.profileAttributes[i].bindObj= null;
								}


							},
							function(response) {
								sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.profileAttributes.error'), 'Error');
							});

	   }

	   if(mode == 'edit'){
		   $scope.loadProfileAttributes();
	   }

	   $scope.updateSelectedMembers = function (item) {
		   if (item.visible) {
			   // if member is selected, add it into the array
			   $scope.currentSelectedMembers.push(item);
		   } else {
			   // if member is not selected, remove it from the array; we must search it by uniqueName first
			   const member = $scope.currentSelectedMembers.find(function(aMember) {return aMember.uniqueName == item.uniqueName});
			   if (member) {
				   const index = $scope.currentSelectedMembers.indexOf(member);
				   $scope.currentSelectedMembers.splice(index, 1);
			   }
		   }
		   console.log($scope.currentSelectedMembers);
	   }
};
})();