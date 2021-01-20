(function () {
	angular.module('cockpitModule')
	.directive('cockpitColumnsConfigurator',function(cockpitModule_widgetServices,$mdDialog,$mdSidenav){

		return {
			templateUrl: baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsConfiguratorTemplate.jsp',
			controller: cockpitColumnsConfiguratorControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
					}
				};
			}
		};
	});

	function cockpitColumnsConfiguratorControllerFunction($scope,$mdDialog,cockpitModule_datasetServices,$mdToast,cockpitModule_widgetConfigurator,sbiModule_restServices,sbiModule_translate,sbiModule_config,$mdSidenav,$q,cockpitModule_generalOptions){
		$scope.translate=sbiModule_translate;
		$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
		$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();

		$scope.availableAggregations = ["NONE","SUM","AVG","MAX","MIN","COUNT","COUNT_DISTINCT"];

		if(!$scope.model.settings.modalSelectionColumn){
			$scope.model.settings.modalSelectionColumn="";
		}

		if(!$scope.model.settings.sortingColumn){
			$scope.model.settings.sortingColumn = undefined;
		}

		if(!$scope.model.settings.sortingOrder){
			$scope.model.settings.sortingOrder = "ASC";
		}

		$scope.selectedColumn = undefined;

		$scope.lastId = -1;

		if($scope.model.dataset && $scope.model.dataset.dsId){
			$scope.local = cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId);
		}

		$scope.showCircularcolumns = {value :false};

		$scope.resetValue = function(dsId){
			if($scope.model.dataset && $scope.model.dataset.dsId){
				$scope.lastId = $scope.model.dataset.dsId;
			}else{
				$scope.model.dataset = {};
			}

			if($scope.lastId==-1 || $scope.lastId!=dsId){
				$scope.showCircularcolumns = {value : true};
				$scope.safeApply();
				$scope.model.dataset.dsId = dsId;
				$scope.local = {};
				if($scope.model.dataset.dsId !=-1){
					angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.local);
					$scope.model.content.columnSelectedOfDataset  = [];
					for(var i=0;i<$scope.local.metadata.fieldsMeta.length;i++){
						var obj = $scope.local.metadata.fieldsMeta[i];
						obj["aggregationSelected"] = "NONE";
						obj["funcSummary"] = "NONE";
						obj["aliasToShow"] = obj.alias;
						$scope.model.content.columnSelectedOfDataset.push(obj);
					}
					$scope.lastId=$scope.model.dataset.dsId;
					$scope.showCircularcolumns ={value : false};
					$scope.safeApply();
				}else{
					$scope.model.content.columnSelectedOfDataset = [];
				}
				$scope.model.settings.sortingColumn = undefined;
				$scope.model.settings.pagination.frontEnd = ($scope.local && $scope.local.isRealtime);
			}
		}

		$scope.safeApply=function(){
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
				$scope.$apply();
			}
		}
		$scope.colorPickerProperty={format:'rgb'}

		$scope.columnsGrid = {
			angularCompileRows: true,
			domLayout :'autoHeight',
	        enableColResize: false,
	        enableFilter: false,
	        enableSorting: false,
	        onRowDragMove: onRowDragMove,
	        onGridReady : resizeColumns,
	        onCellEditingStopped: refreshRow,
	        singleClickEdit: true,
	        columnDefs: [
	        	//{headerName:'Order', cellRenderer: orderRenderer, field:'order',width: 100,suppressSizeToFit:true,sort: 'asc',"cellStyle":{"border":"none !important","display":"inline-flex","justify-content":"center"}},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.name'), field:'name',"editable":isInputEditable,cellRenderer:editableCell, cellClass: 'editableCell',rowDrag: true},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.alias'), field:'aliasToShow',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.type'), field: 'fieldType',"editable":true,cellRenderer:editableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",
	        		cellEditorParams: {values: ['ATTRIBUTE','MEASURE']}},
	        	{headerName: $scope.translate.load('sbi.cockpit.widgets.table.column.aggregation'), field: 'aggregationSelected', cellRenderer: aggregationRenderer,"editable":isAggregationEditable, cellClass: 'editableCell',
	        		cellEditor:"agSelectCellEditor",cellEditorParams: {values: $scope.availableAggregations}},
	        	{headerName:"",cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"border":"none !important","text-align": "right","display":"inline-flex","justify-content":"flex-end"},width: 150,suppressSizeToFit:true, tooltip: false}],
			rowData: $scope.model.content.columnSelectedOfDataset
		}

		function onRowDragMove(event) {
		    if (event.node !== event.overNode) {
		    	var fromIndex = $scope.model.content.columnSelectedOfDataset.indexOf(event.node.data);
		    	var toIndex = $scope.model.content.columnSelectedOfDataset.indexOf(event.overNode.data);

		    	var newStore = $scope.model.content.columnSelectedOfDataset.slice();
		        moveInArray(newStore, fromIndex, toIndex);
		        $scope.model.content.columnSelectedOfDataset = newStore;

		        $scope.columnsGrid.api.setRowData(newStore);
		        $scope.columnsGrid.api.clearFocusedCell();
		    }

		    function moveInArray(arr, fromIndex, toIndex) {
		        var element = arr[fromIndex];
		        arr.splice(fromIndex, 1);
		        arr.splice(toIndex, 0, element);
		    }
		}

		function resizeColumns(){
			$scope.columnsGrid.api.sizeColumnsToFit();
		}

		function orderRenderer(params){
			if(!params.data.order) {
				params.data.order = params.rowIndex;
				$scope.model.content.columnSelectedOfDataset[params.rowIndex].order = params.rowIndex;
			}
			var upButton = params.data.order !=0 ?  '<md-button ng-click="moveUp($event,'+params.data.order+')" class="md-icon-button h20" aria-label="up"><md-icon md-font-icon="fa fa-arrow-up"></md-icon></md-button>' : '';
			var downButton = params.data.order != $scope.columnsGrid.api.getDisplayedRowCount()-1 ?  '<md-button ng-click="moveDown($event,'+params.data.order+')" class="md-icon-button h20" aria-label="up"><md-icon md-font-icon="fa fa-arrow-down"></md-icon></md-button>' : '';
			return 	'<div layout="row">'+
						upButton+
						params.data.order+
						downButton+
				 	'</div>';
		}

		function editableCell(params){
			return typeof(params.value) !== 'undefined' ? '<i class="fa fa-edit"></i> <i>'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>' : '';
		}
		function isInputEditable(params) {
			return typeof(params.data.name) !== 'undefined';
		}
		function isAggregationEditable(params) {
			if (params.data.isCalculated) return false;
			return params.data.fieldType == "MEASURE" ? true : false;
		}

		function aggregationRenderer(params) {
			var aggregation = '<i class="fa fa-edit"></i> <i>'+params.value+'</i>';
			return (params.data.fieldType == "MEASURE" && !params.data.isFunction) ? aggregation : '';
		}

		function buttonRenderer(params){
			var calculator = '';
			if(params.data.isCalculated){
				calculator = '<md-button class="md-icon-button" ng-click="addNewCalculatedField(\''+params.rowIndex+'\')">'+
							 '<md-icon md-font-icon="fa fa-calculator"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.inlineCalculatedFields.title")}}</md-tooltip></md-button>';
			}
			if(params.data.isFunction){
				calculator = '<md-button class="md-icon-button" ng-click="addNewCatalogFunction(\''+params.rowIndex+'\')">'+
							 '<md-icon md-font-icon="fas fa-square-root-alt"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.inlineCatalogFunction.title")}}</md-tooltip></md-button>';
			}
			return 	calculator +
					'<md-button class="md-icon-button noMargin" ng-click="draw(\''+params.data.name+'\')" ng-style="{\'background-color\':model.content.columnSelectedOfDataset['+params.rowIndex+'].style[\'background-color\']}">'+
					'   <md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.columnstyle.icon")}}</md-tooltip>'+
					'	<md-icon ng-style="{\'color\':model.content.columnSelectedOfDataset['+params.rowIndex+'].style.color}" md-font-icon="fa fa-paint-brush" aria-label="Paint brush"></md-icon>'+
					'</md-button>'+
					'<md-button class="md-icon-button" ng-click="deleteColumn(\''+params.data.name+'\',$event)"><md-icon md-font-icon="fa fa-trash"></md-icon><md-tooltip md-delay="500">{{::translate.load("sbi.cockpit.widgets.table.column.delete")}}</md-tooltip></md-button>';
		}

		function refreshRow(cell){
			$scope.columnsGrid.api.redrawRows({rowNodes: [$scope.columnsGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
		}

		$scope.moveUp = function(evt,index){
			evt.stopImmediatePropagation();
			for(var k in $scope.model.content.columnSelectedOfDataset){
				if($scope.model.content.columnSelectedOfDataset[k].order == index) {$scope.model.content.columnSelectedOfDataset[k].order --; continue;}
				if($scope.model.content.columnSelectedOfDataset[k].order == index-1) {$scope.model.content.columnSelectedOfDataset[k].order ++; continue;}
			}
			$scope.columnsGrid.api.setRowData($scope.model.content.columnSelectedOfDataset);
		};
		$scope.moveDown = function(evt,index){
			evt.stopImmediatePropagation();
			for(var k in $scope.model.content.columnSelectedOfDataset){
				if($scope.model.content.columnSelectedOfDataset[k].order == index) {$scope.model.content.columnSelectedOfDataset[k].order ++; continue;}
				if($scope.model.content.columnSelectedOfDataset[k].order == index+1) {$scope.model.content.columnSelectedOfDataset[k].order --; continue;}
			}
			$scope.columnsGrid.api.setRowData($scope.model.content.columnSelectedOfDataset);
		};

		$scope.draw = function(rowName) {
			for(var k in $scope.model.content.columnSelectedOfDataset){
				if($scope.model.content.columnSelectedOfDataset[k].name == rowName) $scope.selectedColumn = $scope.model.content.columnSelectedOfDataset[k];
			}

			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnStyle.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: false,
				autoWrap:false,
				fullscreen: true,
				locals:{model:$scope.model, selectedColumn : $scope.selectedColumn},
				controller: cockpitStyleColumnFunction
			}).then(function(answer) {
				console.log("Selected column:", $scope.selectedColumn);
			}, function() {
				console.log("Selected column:", $scope.selectedColumn);
			});
		},

		$scope.deleteColumn = function(rowName,event) {
			for(var k in $scope.model.content.columnSelectedOfDataset){
				if($scope.model.content.columnSelectedOfDataset[k].name == rowName) var item = $scope.model.content.columnSelectedOfDataset[k];
			}
			if (!item.isFunction) {
				var index=$scope.model.content.columnSelectedOfDataset.indexOf(item);
				$scope.model.content.columnSelectedOfDataset.splice(index,1);
				if($scope.model.settings.sortingColumn == item.aliasToShow){
					$scope.model.settings.sortingColumn = null;
				}
			} else {
				var id = item.boundFunction.id;
				colsToRemove = [];
				for (var i=0; i<$scope.newModel.content.columnSelectedOfDataset.length; i++) {
					var col = $scope.newModel.content.columnSelectedOfDataset[i];
					if (col.isFunction && col.boundFunction.id == id)
						colsToRemove.push(col);
				}
				for (var j=0; j<colsToRemove.length; j++) {
					var index=$scope.newModel.content.columnSelectedOfDataset.indexOf(colsToRemove[j]);
					$scope.newModel.content.columnSelectedOfDataset.splice(index,1);
					if($scope.newModel.settings.sortingColumn == colsToRemove[j].aliasToShow){
						$scope.newModel.settings.sortingColumn = null;
					}
				}
			}
		}

		$scope.$watchCollection('model.content.columnSelectedOfDataset',function(newValue,oldValue){
			if($scope.columnsGrid.api && newValue){
				$scope.columnsGrid.api.setRowData(newValue);
				$scope.columnsGrid.api.sizeColumnsToFit();
			}
		})

		$scope.$watch('local',function(newValue,oldValue){
			if($scope.functionsCockpitColumn.columnList && newValue && newValue.metadata.fieldsMeta != $scope.functionsCockpitColumn.columnList){
				angular.copy(newValue.metadata.fieldsMeta,$scope.functionsCockpitColumn.columnList);
			}
		})
		$scope.functionsCockpitColumn = {
			translate:sbiModule_translate,
			moveUp: function(evt,index){
				$scope.model.content.columnSelectedOfDataset.splice(index-1, 0, $scope.model.content.columnSelectedOfDataset.splice(index, 1)[0]);
			},
			moveDown: function(evt,index){
				$scope.model.content.columnSelectedOfDataset.splice(index+1, 0, $scope.model.content.columnSelectedOfDataset.splice(index, 1)[0]);
			},
			changeColumn: function(row){
				for(var k in $scope.model.content.columnSelectedOfDataset){
					if($scope.model.content.columnSelectedOfDataset[k].$$hashKey == row.$$hashKey){
						$scope.model.content.columnSelectedOfDataset[k].name = row.alias;
						break;
					}
				}
			},
			canSee : function(row){
				return angular.equals(row.fieldType, "MEASURE");
			},
			typeList: [{"code":"java.lang.String", "name":"String"},{"code":"java.lang.Integer", "name":"Number"},{"code":"java.math.BigDecimal", "name":"Number"}],
			columnList : ($scope.local && $scope.local.metadata) ? $scope.local.metadata.fieldsMeta : [] ,
			getColor :function(){
				return $scope.selectedColumn.style !=undefined ? $scope.selectedColumn.style.color : "";
			},
			getBackground: function(){
				return $scope.selectedColumn.style !=undefined ?  $scope.selectedColumn.style.background : "";
			},
			draw: function(row,column,index) {
				$scope.selectedColumn = row;
				$mdDialog.show({
					templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnStyle.html',
					parent : angular.element(document.body),
					clickOutsideToClose:true,
					escapeToClose :true,
					preserveScope: false,
					autoWrap:false,
					fullscreen: true,
					locals:{model:$scope.model, selectedColumn : $scope.selectedColumn},
					controller: cockpitStyleColumnFunction
				}).then(function(answer) {
					console.log("Selected column:", $scope.selectedColumn);
				}, function() {
					console.log("Selected column:", $scope.selectedColumn);
				});
			},
			AggregationFunctions: cockpitModule_generalOptions.aggregationFunctions,
			fieldTypeChanged: function(){
				var disableShowSummary = true;
				for(var i=0; i<$scope.model.content.columnSelectedOfDataset.length; i++){
					if($scope.model.content.columnSelectedOfDataset[i].fieldType == "MEASURE"){
						disableShowSummary = false;
						break;
					}
				}
				$scope.model.settings.summary.forceDisabled = disableShowSummary;
				if(disableShowSummary){
					$scope.model.settings.summary.enabled = false;
				}
			}
		}

		$scope.openListColumn = function(){
			if($scope.model.dataset == undefined || $scope.model.dataset.dsId == undefined){
				$scope.showAction($scope.translate.load("sbi.cockpit.table.missingdataset"));
			}else{
				$mdDialog.show({
					templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsOfDataset.html',
					parent : angular.element(document.body),
					clickOutsideToClose:true,
					escapeToClose :true,
					preserveScope: true,
					autoWrap:false,
					locals: {model:$scope.model, getMetadata : $scope.getMetadata},
					fullscreen: true,
					controller: controllerCockpitColumnsConfigurator
				}).then(function(answer) {
				}, function() {
				});
			}
		}

		$scope.addSummaryInfo = function(currentRow){
			var deferred = $q.defer();
			var promise ;
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitSummaryInfo.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {items: deferred,model:$scope.model, getMetadata : $scope.getMetadata, actualItem : currentRow},
				fullscreen: true,
				controller: controllerCockpitSummaryInfo
			}).then(function(answer) {
				deferred.promise.then(function(result){
					console.log(result);
					currentRow.funcSummary = result.funcSummary;
				});
			}, function() {
			});
			promise =  deferred.promise;


		}

		$scope.addNewCalculatedField = function(currentRowIndex){
			var currentRow = $scope.model.content.columnSelectedOfDataset[currentRowIndex];
			var deferred = $q.defer();
			var promise ;
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitCalculatedFieldTemplate.html',
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {items: deferred,model:$scope.model, getMetadata : $scope.getMetadata, actualItem : currentRow},
				fullscreen: true,
				controller: controllerCockpitCalculatedFieldController
			}).then(function(answer) {
				deferred.promise.then(function(result){
					if(currentRow != undefined){
						currentRow.aliasToShow = result.alias;
						currentRow.formula = result.formula;
						currentRow.formulaArray = result.formulaArray;
						currentRow.alias = result.alias;
					}else{
						$scope.model.content.columnSelectedOfDataset.push(result);

					}
				});
			}, function() {
			});
			promise =  deferred.promise;

		}
		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
			.content(text)
			.action('OK')
			.highlightAction(false)
			.hideDelay(3000)
			.position('top')

			$mdToast.show(toast).then(function(response) {

				if ( response == 'ok' ) {


				}
			});
		}
	}
})();



function controllerCockpitColumnsConfigurator($scope,sbiModule_translate,$mdDialog,model,getMetadata,cockpitModule_datasetServices,cockpitModule_generalOptions){
	$scope.translate=sbiModule_translate;

	$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
	$scope.model = model;
	$scope.columnSelected = [];
	$scope.localDataset = {};
	if($scope.model.dataset && $scope.model.dataset.dsId){
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
	} else{
		$scope.model.dataset= {};
		angular.copy([], $scope.model.dataset.metadata.fieldsMeta);
	}

	$scope.filterColumns = function(){
		var tempColumnsList = $filter('filter')($scope.localDataset.metadata.fieldsMeta,$scope.columnsSearchText);
		$scope.columnsGridOptions.api.setRowData(tempColumnsList);
	}

	$scope.columnsGridOptions = {
            enableColResize: false,
            enableFilter: true,
            enableSorting: true,
            pagination: true,
            paginationAutoPageSize: true,
            onGridSizeChanged: resizeColumns,
            rowSelection: 'multiple',
			rowMultiSelectWithClick: true,
            defaultColDef: {
            	suppressMovable: true,
            	tooltip: function (params) {
                    return params.value;
                },
            },
            columnDefs :[{"headerName":"Column","field":"alias",headerCheckboxSelection: true, checkboxSelection: true},
        		{"headerName":"Field Type","field":"fieldType"},
        		{"headerName":"Type","field":"type"}],
        	rowData : $scope.localDataset.metadata.fieldsMeta
	};

	function resizeColumns(){
		$scope.columnsGridOptions.api.sizeColumnsToFit();
	}

	$scope.saveColumnConfiguration=function(){
		model = $scope.model;

		if(model.content.columnSelectedOfDataset == undefined){
			model.content.columnSelectedOfDataset = [];
		}

		for(var i in $scope.columnsGridOptions.api.getSelectedRows()){
			var obj = $scope.columnsGridOptions.api.getSelectedRows()[i];
			obj.aggregationSelected = 'SUM';
			obj.typeSelected = obj.type;
			obj.label = obj.alias;
			obj.aliasToShow = obj.alias;
			model.content.columnSelectedOfDataset.push(obj);
		}

		$mdDialog.hide();
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}
}

function cockpitStyleColumnFunction(
		$scope,
		sbiModule_translate,
		$mdDialog,
		$mdPanel,
		model,
		selectedColumn,
		cockpitModule_generalServices,
		cockpitModule_datasetServices,
		$mdToast,
		cockpitModule_generalOptions,
		sbiModule_messaging,
		knModule_fontIconsService,
		cockpitModule_properties,
		dialogOptions) {

	$scope.translate=sbiModule_translate;
	$scope.generalServices=cockpitModule_generalServices;
	$scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
	$scope.cockpitModule_properties = cockpitModule_properties;
	$scope.model = model;
	$scope.selectedColumn = angular.copy(selectedColumn);

	$scope.needsCommonPrefs   = (typeof dialogOptions.needsCommonPrefs   == 'undefined' ? true : dialogOptions.needsCommonPrefs);
	$scope.needsVisualization = (typeof dialogOptions.needsVisualization == 'undefined' ? true : dialogOptions.needsVisualization);
	$scope.needsThresholds    = (typeof dialogOptions.needsThresholds    == 'undefined' ? true : dialogOptions.needsThresholds);
	$scope.needsFormat        = (typeof dialogOptions.needsFormat        == 'undefined' ? true : dialogOptions.needsFormat);
	$scope.needsStyle         = (typeof dialogOptions.needsStyle         == 'undefined' ? true : dialogOptions.needsStyle);
	$scope.needsTooltip       = (typeof dialogOptions.needsTooltip       == 'undefined' ? true : dialogOptions.needsTooltip);

	$scope.modelTextAlign = {"flex-start":sbiModule_translate.load('sbi.cockpit.style.textAlign.left'),"center":sbiModule_translate.load('sbi.cockpit.style.textAlign.center'),"flex-end":sbiModule_translate.load('sbi.cockpit.style.textAlign.right')};
	$scope.formatPattern = ['#.###','#,###','#.###,##','#,###.##'];
	$scope.colorPickerProperty={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb'}
	$scope.visTypes=['Chart','Text','Text & Chart','Icon only'];
	$scope.icons=["fa fa-warning","fa fa-bell","fa fa-bolt","fa fa-commenting","fa fa-asterisk","fa fa-ban", "fa fa-check","fa fa-clock-o","fa fa-close","fa fa-exclamation-circle","fa fa-flag","fa fa-star"];
//	function setChunks(array, dimension){
//		var newArray = [];
//		for(var f in array){
//			var familyArray = {"name":array[f].name,"className":array[f].className,icons:[]};
//			var iterator = 0;
//			for(var k in array[f].icons){
//				if (iterator == 0) var tempArray = [];
//				if (iterator < dimension) {
//					tempArray.push(array[f].icons[k]);
//					iterator ++;
//				}
//				if (iterator == dimension) {
//					familyArray.icons.push(tempArray);
//					iterator = 0;
//				}
//			}
//			newArray.push(familyArray);
//		}
//
//		return newArray;
////	}
//
	$scope.availableIcons = knModule_fontIconsService.icons;

	$scope.getTemplateUrl = function(template){
		return cockpitModule_generalServices.getTemplateUrl('tableWidget',template)
	}

	$scope.isDateColumn = function(type){
		if(type == 'oracle.sql.TIMESTAMP' || type == 'java.sql.Timestamp' || type == 'java.util.Date' || type == 'java.sql.Date' || type == 'java.sql.Time'){
			return true;
		}
		return false;
	}

	$scope.variablesExists = function(){
		return 	$scope.cockpitModule_properties.VARIABLES && !angular.equals($scope.cockpitModule_properties.VARIABLES, {});
	}

	$scope.hasPrecision = function(column){
		return $scope.generalServices.isNumericColumn(column);
	}

	$scope.chooseIcon = function(range) {
		$scope.tempVar = !$scope.tempVar;
		$scope.currentRange=range;
		$scope.iconFamily = $scope.availableIcons[0].name;

  	}
	$scope.setIcon = function(icon){
		$scope.currentRange.icon = icon.className;
		$scope.tempVar = !$scope.tempVar;
	}


	if(!$scope.selectedColumn.hasOwnProperty('colorThresholdOptions'))
	{
		$scope.selectedColumn.colorThresholdOptions={};
		$scope.selectedColumn.colorThresholdOptions.condition=[];
		for(var i=0;i<3;i++)
		{
			$scope.selectedColumn.colorThresholdOptions.condition[i]="none";
		}
	}
	$scope.defaultChart = {"enabled":true,"minValue":0,"maxValue":100,"style":{"color":"white","background-color":"#3b678c","justify-content":"start"}};

	$scope.changeVisType = function(){
		if($scope.selectedColumn.visType==undefined){
			$scope.selectedColumn.visType="Text";
		}else if($scope.selectedColumn.visType=="Chart"){
			if(!$scope.selectedColumn.barchart) $scope.selectedColumn.barchart = angular.copy($scope.defaultChart);
			if($scope.selectedColumn.text) $scope.selectedColumn.text.enabled=false;
		}else if($scope.selectedColumn.visType=="Text"){
			delete $scope.selectedColumn.barchart;
			if($scope.selectedColumn.text) $scope.selectedColumn.text.enabled=true;
		}else if($scope.selectedColumn.visType=='Icon only'){
			$scope.selectedColumn.text.enabled=false;
		}else if($scope.selectedColumn.visType == "Text & Chart") $scope.selectedColumn.barchart = angular.copy($scope.defaultChart);
	}



	$scope.conditions=['>','<','==','>=','<=','!='];
	if($scope.selectedColumn.scopeFunc==undefined){
		$scope.selectedColumn.scopeFunc={conditions:$scope.conditions, condition:[{condition:'none'},{condition:'none'},{condition:'none'},{condition:'none'}]};
	}
	//------------------------- Threshold icon table -----------------------------
	var conditionString0="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[0].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString1="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[1].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString2="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[2].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString3="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[3].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"


	var valueString0="<md-input-container class='md-block' ng-if='scopeFunctions.condition[0].condition!=undefined && scopeFunctions.condition[0].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[0].value' type='number' required> </md-input-container>";
	var valueString1="<md-input-container class='md-block' ng-if='scopeFunctions.condition[1].condition!=undefined && scopeFunctions.condition[1].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[1].value' type='number' required> </md-input-container>";
	var valueString2="<md-input-container class='md-block' ng-if='scopeFunctions.condition[2].condition!=undefined && scopeFunctions.condition[2].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[2].value' type='number' required> </md-input-container>";
	var valueString3="<md-input-container class='md-block' ng-if='scopeFunctions.condition[3].condition!=undefined && scopeFunctions.condition[3].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[3].value' type='number' required> </md-input-container>";

	$scope.thresholdsList=
		[{priority:0, icon:"<md-icon style='color:red'  md-font-icon='fa fa-exclamation-circle' ng-init='scopeFunctions.condition[0].iconColor=\"red\";	scopeFunctions.condition[0].icon=\"fa fa-exclamation-circle\"'></md-icon>",condition:conditionString0,	value:valueString0},{priority:1 , icon:"<md-icon style='color:red'	md-font-icon='fa fa-times-circle' ng-init='scopeFunctions.condition[1].iconColor=\"red\"; scopeFunctions.condition[1].icon=\"fa fa-times-circle\"'></md-icon>",condition:conditionString1, value:valueString1},	{priority:2 , icon:"<md-icon style='color:yellow'  md-font-icon='fa fa-exclamation-triangle' ng-init='scopeFunctions.condition[2].iconColor=\"yellow\"; scopeFunctions.condition[2].icon=\"fa fa-exclamation-triangle\"'></md-icon>",condition:conditionString2, value:valueString2},{priority:3 , icon:"<md-icon style='color:green'  md-font-icon='fa fa-check-circle' ng-init='scopeFunctions.condition[3].iconColor=\"green\";	scopeFunctions.condition[3].icon=\"fa fa-check-circle\"'></md-icon>",condition:conditionString3, value:valueString3}];
	$scope.tableColumns=[{label:"Icon",name:"icon", hideTooltip:true},{label:"Condition",name:"condition", hideTooltip:true},{label:"Value",name:"value", hideTooltip:true}];

	//$scope.selectedColumn.conditions=$scope.scopeFunc.condition;

	//----------------------- Cell color table ------------------------------------

	var condString0="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[0].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var condString1="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[1].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var condString2="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[2].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"

	var valString0="<md-input-container class='md-block' ng-if='scopeFunctions.condition[0].condition!=undefined && scopeFunctions.condition[0].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[0].value' type='number' required> </md-input-container>";
	var valString1="<md-input-container class='md-block' ng-if='scopeFunctions.condition[1].condition!=undefined && scopeFunctions.condition[1].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[1].value' type='number' required> </md-input-container>";
	var valString2="<md-input-container class='md-block' ng-if='scopeFunctions.condition[2].condition!=undefined && scopeFunctions.condition[2].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[2].value' type='number' required> </md-input-container>";


	$scope.cellColorThresholdsList=[{priority:0, color:"<md-input-container class=\"md-block\">  <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[0].value \"></color-picker>  </md-input-container>",condition:condString0, value:valString0},{priority:1 , color:"<md-input-container class=\"md-block\"> <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[1].value \"></color-picker></md-input-container>",condition:condString1, value:valString1},{priority:2 , color:"<md-input-container class=\"md-block\"> <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[2].value \"></color-picker></md-input-container>",condition:condString2, value:valString2}];
	$scope.cellColorTableColumns=[{label:"Color",name:"color", hideTooltip:true},{label:"Condition",name:"condition", hideTooltip:true},{label:"Value",name:"value", hideTooltip:true}];

	//----------------------------------------------------------------------------

	$scope.addRange = function(){
		if(!$scope.selectedColumn.ranges) $scope.selectedColumn.ranges = [];
		$scope.selectedColumn.ranges.push({});
	}

	$scope.deleteRange = function(hashkey){
		for(var i in $scope.selectedColumn.ranges){
			if($scope.selectedColumn.ranges[i].$$hashKey == hashkey){
				$scope.selectedColumn.ranges.splice(i,1);
				break;
			}
		}
	}

	$scope.cleanStyleColumn = function(){
		$scope.selectedColumn.style = undefined;
	}

	$scope.saveColumnStyleConfiguration = function(){
		if($scope.selectedColumn.visType=='Chart'|| $scope.selectedColumn.visType== 'Text & Chart' ){
			if($scope.selectedColumn.barchart && $scope.selectedColumn.barchart.enabled && (typeof $scope.selectedColumn.barchart.minValue == 'undefined' || typeof $scope.selectedColumn.barchart.maxValue == 'undefined')){
				var toast = $mdToast.simple()
				.content($scope.translate.load('kn.table.missingrequiredfields'))
				.action('OK')
				.highlightAction(false)
				.hideDelay(5000)
				.position('top')
				$mdToast.show(toast)
				return;
			}
		}
		angular.copy($scope.selectedColumn,selectedColumn);
		$mdDialog.cancel();
	}

	$scope.cancelcolumnStyleConfiguration = function(){
		$mdDialog.cancel();
	}

	$scope.checkIfDisable = function(){
		return false;
	}
}

function controllerCockpitSummaryInfo($scope,sbiModule_translate,$mdDialog,items,model,getMetadata,actualItem,cockpitModule_datasetServices,$mdToast,cockpitModule_generalOptions){
	$scope.translate=sbiModule_translate;
	$scope.model = model;
	$scope.row  =actualItem;

	$scope.listType = cockpitModule_generalOptions.aggregationFunctions;

	$scope.saveColumnConfiguration=function(){

		items.resolve($scope.row);
		$mdDialog.hide();
	}

	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}
}

function controllerCockpitCalculatedFieldController($scope,sbiModule_translate,$mdDialog,items,model,getMetadata,actualItem,cockpitModule_datasetServices,$mdToast){
	$scope.translate=sbiModule_translate;
	$scope.model = model;
	$scope.localDataset = {};
	$scope.formula = "";
	$scope.formulaElement = [];

	if($scope.model.dataset.dsId != undefined){
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
	}

	$scope.column = {};
	$scope.measuresList = [];
	$scope.operators = ['+','-','*','/'];
	$scope.brackets = ['(',')'];


	$scope.checkInput=function(event){
		console.log(event);
		if(event.key == "Backspace"){
			event.preventDefault();
			$scope.deleteLast();
		}
		else if(event.key=="+" || event.key=="-" || event.key=="/" ||  event.key=="*" ){
			event.preventDefault();
			$scope.addOperator(event.key);
		}else if (event.char=="+" || event.char=="-" || event.char=="/" ||  event.char=="*"){
			//internet explorer
			event.preventDefault();
			$scope.addOperator(event.char);
		}
		else if(event.key=="(" || event.key==")" ){
			event.preventDefault()
			$scope.addBracket(event.key);
		}else if(event.char=="(" || event.char==")"){
			//internet explorer
			event.preventDefault()
			$scope.addBracket(event.char);
		}

		var reg = new RegExp("[0-9\.\,]+");
		if(reg.test(event.key)){
			if($scope.formulaElement.length>0){
				var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
				if(lastObj.type=='measure'){
					$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
					event.preventDefault();
					return;
				}
			}
			var obj = {};
			obj.type = 'number';
			obj.value = event.key;
			$scope.formulaElement.push(obj);
			$scope.formula = $scope.formula +""+event.key+"";
			event.preventDefault();
		} else {
			event.preventDefault();
		}

	}

	//load all columns SELECTED of type measure

	for(var i=0;i<$scope.localDataset.metadata.fieldsMeta.length;i++){
		var obj = $scope.localDataset.metadata.fieldsMeta[i];
		if(obj.fieldType == 'MEASURE'){
			$scope.measuresList.push(obj);
		}
	}


	$scope.reloadValue = function(){
		$scope.formulaElement = angular.copy(actualItem.formulaArray);
		$scope.column.alias = angular.copy(actualItem.aliasToShow);
		$scope.redrawFormula();
	}

	$scope.saveColumnConfiguration=function(){
		if($scope.formulaElement.length>0){
			var obj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(obj.type=='operator'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula1'));
				return;
			}
		}
		if(!$scope.checkBrackets()){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula5'));
			return;
		}
		$scope.result = {};
		$scope.result.alias = $scope.column.alias != undefined ? $scope.column.alias : "NewCalculatedField";
		$scope.result.formulaArray = $scope.formulaElement;
		$scope.result.formula = $scope.formula;
		$scope.result.aggregationSelected = 'NONE';
		$scope.result["funcSummary"] = "NONE";
		$scope.result.aliasToShow = $scope.result.alias;
		$scope.result.fieldType = 'MEASURE';
		$scope.result.isCalculated = true;
		$scope.result.type = "java.lang.Integer";
		items.resolve($scope.result);
		$mdDialog.hide();
	}
	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

	$scope.checkBrackets = function(){
		var countOpenBrackets = 0;
		var countCloseBrackets = 0;
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type == 'bracket'){
				if(obj.value == '('){
					countOpenBrackets++;
				} else {
					countCloseBrackets++;
				}
			}
		}

		if(countOpenBrackets != countCloseBrackets){
			return false;
		}
		return true;
	}
	$scope.addOperator= function(op){
		if($scope.formulaElement.length==0){
			$scope.showAction('Select a measure before.');
			return;
		}
		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj.type=='operator'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula2'));
			return;
		}
		var obj = {};
		obj.type = 'operator';
		obj.value = op;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +" "+op+" ";
	}
	$scope.addBracket= function(br){

		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj !=undefined && lastObj.type=='measure' && br == '('){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		if(lastObj !=undefined && lastObj.type=='operator' && br == ')'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		var obj = {};
		obj.type = 'bracket';
		obj.value = br;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +" "+br+" ";
	}
	$scope.addMeasures =function(meas){
		if($scope.formulaElement.length>0){
			var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(lastObj.type=='measure' || lastObj.type=='number'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
				return;
			}
		}
		var obj = {};
		obj.type = 'measure';
		obj.value = meas.alias;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +' "'+meas.alias+'"';
	}
	$scope.deleteLast = function(){
		if($scope.formulaElement.length>0){
			$scope.formulaElement.pop();
			$scope.redrawFormula();
		}
	}
	$scope.redrawFormula = function(){
		$scope.formula = "";
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type=="number"){
				$scope.formula = $scope.formula +""+obj.value+"";
			}else if(obj.type=="measure"){
				$scope.formula = $scope.formula +'"'+obj.value+'"';
			}else{
				$scope.formula = $scope.formula +" "+obj.value+" ";
			}

		}
	}
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	}
	if(actualItem !=undefined){
		$scope.reloadValue();
	}
}
