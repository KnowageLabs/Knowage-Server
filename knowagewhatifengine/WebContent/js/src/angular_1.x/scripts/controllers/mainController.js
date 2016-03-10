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
	
	
	$scope.crossNavigationEnabled = false;
	
	$scope.enableDisableCrossNavigation = function() {
		$scope.crossNavigationEnabled = !$scope.crossNavigationEnabled;
	}
	
	$scope.enableDisableSorting = function(){
		
		$scope.sortDisable();
	}
	
	$scope.enableDisableDrillThrough = function(){
		$scope.modelConfig.enableDrillThrough = !$scope.modelConfig.enableDrillThrough;
		$scope.sendModelConfig($scope.modelConfig);
	}
	
	$scope.toggleMenu=function(){
		
		$scope.openLeftMenu=!$scope.openLeftMenu;
	};
	
	$scope.changeDrillType = function(type){
		$scope.modelConfig.drillType = type;
		$scope.sendModelConfig($scope.modelConfig);
	}

	$scope.btnFunctions = function(name){
		switch(name){
		case "BUTTON_FATHER_MEMBERS":
			$scope.modelConfig.showParentMembers = !$scope.modelConfig.showParentMembers;
			break;
		case "BUTTON_HIDE_SPANS":
			$scope.modelConfig.hideSpans = !$scope.modelConfig.hideSpans;
			break;
		case "BUTTON_SHOW_PROPERTIES":
			$scope.modelConfig.showProperties = !$scope.modelConfig.showProperties;
			break;
		case "BUTTON_HIDE_EMPTY":
			$scope.modelConfig.suppressEmpty = !$scope.modelConfig.suppressEmpty;
			break;	
		default:
			console.log("something else clicked");
		}
		$scope.sendModelConfig($scope.modelConfig);
	}
	
	/*dragan**/
	 
	 /*service for placing member on axis**/
	 $scope.putMemberOnAxis = function(fromAxis,member){
		 
		 sbiModule_restServices.promisePost
		 ('1.0/axis/'+fromAxis+'/moveDimensionToOtherAxis/'+member.uniqueName+'/'+member.axis+'?SBI_EXECUTION_ID='+JSsbiExecutionID,"",member)
			.then(function(response) {
				$scope.handleResponse(response);			
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
				
			});	
	}

	 /*service for moving hierarchies**/
	 $scope.moveHierarchies = function(axis,hierarchieUniqeName,newPosition,direction,member){
		 
		 sbiModule_restServices.promisePost
		 ('1.0/axis/'+axis+'/moveHierarchy/'+hierarchieUniqeName+'/'+newPosition+'/'+direction+'?SBI_EXECUTION_ID='+JSsbiExecutionID,"",member)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while movin hierarchy", 'Error');
				
			});	
	}
	 /*service for sending modelConfig**/
	 
	 $scope.sendModelConfig = function(modelConfig){
		 
		 sbiModule_restServices.promisePost
		 ("1.0/modelconfig?SBI_EXECUTION_ID="+JSsbiExecutionID,"",modelConfig)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sending model config", 'Error');
				
			});	
	}

	 $scope.startFrom = function(start){
		   if($scope.ready){
		    $scope.ready = false;
		    
		    sbiModule_restServices.promiseGet("1.0",'/member/start/1/'+start+'?SBI_EXECUTION_ID='+JSsbiExecutionID)
			.then(function(response) {
				$scope.table = $sce.trustAsHtml( response.data.table);
				   $scope.ready = true;
				   $scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');
				
			});	
		   }
		  }
	 
	 $scope.sortBDESC = function(){
		 
		 sbiModule_restServices.promiseGet("1.0","/member/sort/1/0/[[Measures].[Unit Sales]]/BDESC?SBI_EXECUTION_ID="+JSsbiExecutionID)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sorting", 'Error');
				
			});	
		  }
	 
	 $scope.sortDisable = function(){
		 
		 sbiModule_restServices.promiseGet("1.0","/member/sort/disable?SBI_EXECUTION_ID="+JSsbiExecutionID)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sorting", 'Error');
				
			});	
		  }
	 
	 /** dragan  sorting */
	 
	  $scope.sort = function(axisToSort,axis,positionUniqueName){
		  
		  var path; 
		  
		  if($scope.selectedSortingMode==='count'){
			  var path = '/member/sort/'+axisToSort+'/'+axis+'/'+positionUniqueName+'/'+$scope.selectedSortingMode+'/'+$scope.sortingCount+'?SBI_EXECUTION_ID='+JSsbiExecutionID;
		  }else {
			  var path = '/member/sort/'+axisToSort+'/'+axis+'/'+positionUniqueName+'/'+$scope.selectedSortingMode+'?SBI_EXECUTION_ID='+JSsbiExecutionID;
		  }
		 
		 sbiModule_restServices.promiseGet("1.0",path)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sorting", 'Error');
				
			});	
		  }

	/**dragan*/
	angular.element(document).ready(function() {
		$scope.sendMdxQuery('null');

	});

	/**dragan*/
	/*writeback funtionality*/
	/**
	 * @property {String} lastEditedFormula
	 *  the last edited formula. To restore the formula
	 */
	$scope.lastEditedFormula=null;
	
	/**
	 * @property {String} lastEditedCell
	 *  the last edited formula. To restore the formula
	 */
	$scope.lastEditedCell= null,
		
	$scope.value = null;
		
	$scope.makeEditable = function(id,measureName){
		
		var unformattedValue = "";
		var modelStatus = null;
		
		modelStatus = $scope.modelConfig.status;
		
		if(modelStatus  == 'locked_by_other' || modelStatus  == 'unlocked'){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.writeback.edit.no.locked'), 'Error');
			console.log('sbi.olap.writeback.edit.no.locked');
			return;
		}
		if($scope.modelConfig && $scope.isMeasureEditable(measureName)){
			var cell = angular.element(document.querySelector("[id='"+id+"']"));
			//console.log(cell[0].childNodes[0].data);
		}
		

			//check if the user is editing the same cell twice. If so we present again the last formula
			if($scope.lastEditedFormula && $scope.lastEditedCell && id.startsWith($scope.lastEditedCell)){
				unformattedValue = $scope.lastEditedFormula;
			}else{
				var type = "float";
				var originalValue = "";

			
					originalValue = (cell[0].childNodes[0].data).trim();
					if (originalValue  == '') { // in case the cell was empty, we type 0
						unformattedValue = 0;
					} else {
						unformattedValue = parseFloat(originalValue.replace(',','.'));//Sbi.whatif.commons.Format.cleanFormattedNumber(originalValue, Sbi.locale.formats[type]);
						console.log(originalValue);
						console.log(unformattedValue);
					}
				
					//Sbi.error("Error loading the value of the cell to edit" + err);
				

				//it's not possible to edit a cell with value 0
				if(unformattedValue ==0){
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load('sbi.olap.writeback.edit.no.zero'),'Error');
					return;
				}
				
			$scope.showEditCell(cell,id,originalValue);
				
				
			}
		
		
	}
	/**
	*checks if measure is editable
	* @param measureName the name of the measure to check
	* @returns {Boolean} return if the measure is editable
	*/
	$scope.isMeasureEditable = function(measureName){
		if($scope.modelConfig && $scope.modelConfig.writeBackConf){
			if($scope.modelConfig.writeBackConf.editableMeasures  == null || $scope.modelConfig.writeBackConf.editableMeasures.length ==0){
				return true;
			}else{
				var measures = ($scope.modelConfig.writeBackConf.editableMeasures);
				
				for(measureNameCheck in measures){
					if (measureNameCheck === measureName);
					var contained = measureName;
					return contained;
				}
				
			
		}
		return false;
	}
		
	}
	$scope.writeBackCell = function(id, value, startValue, originalValue){
		console.log("writeBackCell");
		var type = "float";
		if ( startValue ) {
			startValue = parseFloat(startValue);//Sbi.whatif.commons.Format.cleanFormattedNumber(startValue, Sbi.locale.formats[type]);
		}
		if ( value != startValue ) {
			var position = "";
			var unformattedValue = value;

			if ( id ) {
				var endPositionIndex = id.indexOf("!");
				position= id.substring(0, endPositionIndex);
			}

			
				if ( !isNaN(value) ) {
					//Value is a number
					unformattedValue = parseFloat(value);//Sbi.whatif.commons.Format.formatInJavaDouble(value, Sbi.locale.formats[type]);
				} else {
					//Value is a string/expression
					unformattedValue = value;
				}
			

			//update the last edited values
			this.lastEditedFormula = unformattedValue;
			var separatorIndex = id.lastIndexOf('!');
			this.lastEditedCell = id.substring(0,separatorIndex);
			console.log(unformattedValue+'!!!!!!!!!!!!!!!!');
			$scope.sendWriteBackCellService(position, unformattedValue);
		} else {
			/*Sbi.debug("The new value is the same as the old one");
			var cell = Ext.get(id);
			cell.dom.childNodes[0].data = originalValue;*/
			console.log(originalValue);
		}
	}
	
	$scope.showEditCell = function(cell,id,originalValue){
		console.log(cell[0]);
			cell[0].style.setProperty('position','fixed','important');
			 
			var textLength = (cell[0].childNodes[0].data).trim().length;
			var startVaue = cell[0].childNodes[0].data.trim();
			var textFontSize = cell[0].style.fontSize;
			console.log(textFontSize);
			var cellWidth = 250 + 12*textLength;
			cell[0].style.setProperty('z-index','500');
			cell.css('width',cellWidth);
			cell.css('transform','translatey(-14px)');//transform: translatey(-7px);
			
				$mdDialog
			.show({
				scope : $scope,
				parent: cell,
				preserveScope : true,
				controller : function DialogController($scope,$mdDialog){
					$scope.closeDialog = function(e){
						if(e.keyCode===13){
						$mdDialog.hide();
							
						$scope.writeBackCell( id, $scope.value,  startVaue, originalValue);
		}
						
					}
					
					
				},
				template : "<md-dialog style='min-height: 30px;position: absolute;left: 0;top:0'><input ng-model='value' type='text' style='width: 190px;transform:translateX(50px);' ng-keypress='closeDialog($event)'><input type='button'  ng-click='showFormulaDialog()' style='position:absolute;left:0px;top:0px' value='f(x)'></md-dialog>",
					onRemoving: function(){
						cell.css('width','inherit');
						cell[0].style.setProperty('position','relative','important');
						cell[0].style.setProperty('z-index','1');
						cell.css('transform','translatey(0px)');
						console.log($scope.value);
						
						
					},
				clickOutsideToClose : true,
					autoWrap:false
			});
		
		
	}
	$scope.showFormulaDialog = function(){
		
	
		$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/writeBackCell.html',
				//targetEvent : ev,
				clickOutsideToClose : false,
				hasBackdrop:false
			});
	}
	
	$scope.sendWriteBackCellService = function(ordinal,expression){
		
		 var path = '/model/setValue/'+ordinal+'?SBI_EXECUTION_ID='+JSsbiExecutionID; 
		  
		  var st = {'expression':expression};
		 
		 sbiModule_restServices.promisePost("1.0/model/setValue/"+ordinal+"?SBI_EXECUTION_ID="+JSsbiExecutionID,"",st)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("error", 'Error');
				
			});	
	}
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
	
	filterXMLResult = function(res) {
		var regEx = /([A-Z]+_*)+/g;
		var i;
		
		while (i = regEx.exec(res)){
			var btn = {};
			btn.tooltip = messageResource.get("sbi.olap.toolbar."+ i[0], 'messages');
			btn.img =i[0];//"../img/show_parent_members.png"// url(../img/show_parent_members.png);
			$scope.toolbarButtons.push(btn);	
		}
			
	}

	filterXMLResult(toolbarVisibleBtns);
	
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
	
	$scope.dimensionShift = function(direction){
		if(direction == 'left' && $scope.columns.length-1-$scope.topStart >= $scope.maxCols){
		      $scope.topStart++;      
	    }
	    if(direction == 'right' && $scope.topStart>0){
	      $scope.topStart--;
	    }
	    if(direction == 'up' && $scope.rows.length-1-$scope.leftStart >= $scope.maxRows){
	    	$scope.leftStart++;
	    }
	    if(direction == 'down' && $scope.leftStart){
	    	$scope.leftStart--;
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
				  /*$scope.data = response.data;
				  $scope.loadedData.push(response.data);
				  if($scope.activeaxis >= 0){
						getVisible($scope.data);
					}*/
				  checkIfExists(response.data);
				  console.log("-------------");
				  console.log(visibleSelected);
				  console.log($scope.data);
			  }
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured while getting hierarchy members", 'Error');
			
		});	
	}
	
	$scope.drillDown = function(axis, position, member, uniqueName,positionUniqueName) {
		sbiModule_restServices.promiseGet
		("1.0",'/member/drilldown/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}

	$scope.drillUp = function(axis, position, member, uniqueName,positionUniqueName) {
		sbiModule_restServices.promiseGet
		("1.0",'/member/drillup/'+ axis+ '/'+ position+ '/'+ member+ '/'+ positionUniqueName+ '/'+ uniqueName+ '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured by drill down functionality", 'Error');
			
		});		
	}

	$scope.swapAxis = function() {
		sbiModule_restServices.promisePost("1.0/axis/swap?SBI_EXECUTION_ID="+JSsbiExecutionID,"")
		.then(function(response) {
			$scope.handleResponse(response);
		}, function(response) {
			sbiModule_messaging.showErrorMessage("An error occured during swap axis functionality", 'Error');
			
		});	}

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
				
				consoleLogHelper();
			}
		}				
	};
	
	function consoleLogHelper(){
		console.log("filters:");
		console.log($scope.filterCardList);
		console.log("rows:");
		console.log($scope.rows);
		console.log("columns:");
		console.log($scope.columns);
		console.log("filter selected:");
		console.log($scope.filterSelected);
		
	}
	
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
				
				consoleLogHelper();
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
				consoleLogHelper();
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
	
	$scope.getCollections = function() {
		
		sbiModule_restServices.promiseGet
		("1.0",'/member/drilltrough/levels/?SBI_EXECUTION_ID=' + JSsbiExecutionID)
		.then(function(response) {
			console.log(response);
			$scope.dtTree = response.data;
		    }, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
			
				});
		}
	$scope.drillThrough = function(ordinal) {

		var c = JSON.stringify($scope.dtAssociatedLevels);
		console.log(c);
		 switch (arguments.length) {
		    case 0:
		    	console.log("FROM DIALOG");
		    	sbiModule_restServices.promiseGet
				("1.0",'/member/drilltrough/'+ $scope.ord+ '/'+c+ '/'+ $scope.dtMaxRows+ '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
				.then(function(response,ev) {
					$scope.dt = response.data;
					$scope.dtData = response.data;
					$scope.dtColumns = Object.keys(response.data[0]);
					$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
				    }, function(response) {
					sbiModule_messaging.showErrorMessage("error", 'Error');
					
						});
		        break;
		    case 1:
		    	console.log("FROM CELL");
		    	$scope.ord = ordinal;
		    	sbiModule_restServices.promiseGet
				("1.0",'/member/drilltrough/'+ ordinal + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
				.then(function(response,ev) {
					$scope.dt = response.data;
					console.log($scope.dt);
					$scope.dtData = response.data;
					$scope.dtColumns = Object.keys(response.data[0]);
					$scope.formateddtColumns =$scope.formatColumns($scope.dtColumns);
					console.log($scope.formateddtColumns);
					$scope.getCollections();
					$scope.openDtDialog();

				    }, function(response) {
					sbiModule_messaging.showErrorMessage("error", 'Error');
					
						});
		        break;
		    default:
		        break;
		    }
		 $scope.exportDrill = function() {
				
				var json = JSON.stringify($scope.dtData);
				delete $scope.dtData.$$hashKey;
				console.log(json);
					sbiModule_restServices.promiseGet
					("1.0",'/member/drilltrough/export/'+ json+ '/' + '?SBI_EXECUTION_ID=' + JSsbiExecutionID)
					.then(function(response) {
						$scope.dtData = [];
					    }, function(response) {
						sbiModule_messaging.showErrorMessage("error", 'Error');
						
							});
					}
			
			
			}
		$scope.formatColumns = function(array){
			var arr = [];
			for (var i = 0; i < array.length; i++) {
				var obj = {};
				obj.label = array[i].toUpperCase();
				obj.name = array[i];
				obj.size = "100px";
				arr.push(obj);
			}
			return arr;
		}
		$scope.openDtDialog = function(ev) {
			$scope.dtAssociatedLevels= []; 
			$mdDialog
			.show({
				scope : $scope,
				preserveScope : true,
				controllerAs : 'olapCtrl',
				templateUrl : '/knowagewhatifengine/html/template/main/toolbar/drillThrough.html',
				targetEvent : ev,
				clickOutsideToClose : true
				
			});
		  };
		  $scope.closeDtDialog = function(ev) {
			  $mdDialog.hide();
	      };  
	
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
				//levelDrop(id, $scope.data[i].children);
				break;
			} else {
				if ($scope.data[i].children.length > 0)
					
					levelDrop(id, $scope.data[i].children);
					//console.log($scope.data);
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
	
	$scope.switchPosition = function(data){
		 
		$scope.moveHierarchies(data.axis, data.uniqueName, data.positionInAxis+1,1,data);
		 if(data.axis == 0){			 
			 var pom = $scope.columns[data.positionInAxis];
			 var pia = data.positionInAxis;

			 $scope.columns[pia].positionInAxis = pia + 1;
			 $scope.columns[pia+1].positionInAxis = pia;
			 
			 $scope.columns[pia] = $scope.columns[pia+1];
			 $scope.columns[pia+1] = pom;
			 
		 }
		 else if(data.axis == 1){			 
			 var pom = $scope.rows[data.positionInAxis];
			 var pia = data.positionInAxis;

			 $scope.rows[pia].positionInAxis = pia + 1;
			 $scope.rows[pia+1].positionInAxis = pia;
			 
			 $scope.rows[pia] = $scope.rows[pia+1];
			 $scope.rows[pia+1] = pom;
			 
		 }


	};

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