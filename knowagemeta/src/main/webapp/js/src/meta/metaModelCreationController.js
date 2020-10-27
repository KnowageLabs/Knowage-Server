angular.module('metaManager').controller('metaModelCreationController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',metaModelCreationControllerFunction ]);
angular.module('metaManager').controller('metaModelCreationPhysicalController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config',metaModelCreationPhysicalControllerFunction ]);
angular.module('metaManager').controller('metaModelCreationBusinessController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout','$mdDialog','sbiModule_config','metaModelServices','$mdPanel','sbiModule_config','sbiModule_user',metaModelCreationBusinessControllerFunction ]);
angular.module('metaManager').controller('businessModelPropertyController', [ '$scope','sbiModule_translate', 'sbiModule_restServices', 'parametersBuilder','$timeout',businessModelPropertyControllerFunction ]);
angular.module('metaManager').controller('businessModelAttributeController', businessModelAttributeControllerFunction);
angular.module('metaManager').controller('calculatedBusinessColumnsController', [ '$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','sbiModule_config','metaModelServices',calculatedBusinessColumnsControllerFunction ]);
angular.module('metaManager').controller('businessViewJoinRelationshipsController', [ '$scope','sbiModule_translate', 'sbiModule_restServices',businessViewJoinRelationshipsControllerFunction ]);

function metaModelCreationControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout) {
$scope.tabResource={selectedBusinessTab:"propertiestab"};

}

function metaModelCreationPhysicalControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config) {
	$scope.selectedPhysicalModel = {};

	$scope.currentPhysicalModelParameterCategories = [];

	$scope.selectPhysicalModel = function(node) {
		$scope.selectedPhysicalModel = node;
		angular.copy(parametersBuilder
				.extractCategories($scope.selectedPhysicalModel.properties),
				$scope.currentPhysicalModelParameterCategories);
	}

	$scope.openedItems = [];

	$scope.openBusinessModel = function(model,e){
		e.stopPropagation();
		if($scope.openedItems.indexOf(model.name)!==-1){
			$scope.openedItems.splice($scope.openedItems.indexOf(model.name),1);
		}else{
			$scope.openedItems.push(model.name);
		}
	}

	$scope.physicalModel_getlevelIcon = function(node) {
		if (node.hasOwnProperty("columns")) {
			// is business model node

			// TO-DO manage folder by type
			return "fa fa-table";
		} else {
			// is column node
			if (node.primaryKey == true) {
				return "fa fa-key goldKey"
			}
			return "fa fa-columns";

		}

	}
	$scope.physicalModel_isFolder = function(node) {
		if (node.hasOwnProperty("columns")) {
			return !node.expanded;
		}else{
			return true
		}
	}

	$scope.physicalModelMiscInfo = [ {
		name : "name",
		label : sbiModule_translate.load("name")
	}, {
		name : "description",
		label : sbiModule_translate.load("description")
	}, {
		name : "comment",
		label : sbiModule_translate.load("comment")
	}, {
		name : "dataType",
		label : sbiModule_translate.load("dataType")
	}, {
		name : "decimalDigits",
		label : sbiModule_translate.load("decimalDigits")
	}, {
		name : "typeName",
		label : sbiModule_translate.load("typeName")
	}, {
		name : "size",
		label : sbiModule_translate.load("size")
	}, {
		name : "octectLength",
		label : sbiModule_translate.load("octectLength")
	}, {
		name : "radix",
		label : sbiModule_translate.load("radix")
	}, {
		name : "defaultValue",
		label : sbiModule_translate.load("defaultValue")
	}, {
		name : "nullable",
		label : sbiModule_translate.load("nullable")
	}, {
		name : "position",
		label : sbiModule_translate.load("position")
	}, {
		name : "table",
		label : sbiModule_translate.load("table")
	}];

	$scope.refreshPhysicalModel=function(){
		$mdDialog.show({
			controller: refreshPhysicalModelController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, physicalModel: $scope.meta.physicalModels},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/refreshPhysicalModel.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
			$scope.businessViewTreeInterceptor.refreshTree();
		});
	}


	$scope.fkTableColumns=[
		                       {
		                    	   label:sbiModule_translate.load("sbi.generic.name"),
		                    	   name:'name'
		                       },
		                       {
		                    	   label:sbiModule_translate.load("sbi.meta.source.columns"),
		                    	   name:'sourceColumns',
		                    	   transformer:function(item){
			                    		var toret=[];
			                    		for(var i=0;i<item.length;i++){
			                    			 toret.push(item[i].tableName+"."+item[i].name);
			                    		}
			                    		return toret.join(",");
		                    	   }
		                       },
		                       {
		                    	   label:sbiModule_translate.load("sbi.meta.target.columns"),
		                    	   name:'destinationColumns',
		                    	   transformer:function(item){
		                    		   var toret=[];
			                    		for(var i=0;i<item.length;i++){
			                    			 toret.push(item[i].tableName+"."+item[i].name);
			                    		}
			                    		return toret.join(",");
		                    	   }
		                       }
	                       ]


	$scope.getPropertyAttributes = function(prop){
		return prop[Object.keys(prop)[0]];
	}
}

function metaModelCreationBusinessControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout,$mdDialog,sbiModule_config,metaModelServices,$mdPanel,sbiModule_config,sbiModule_user){
	$scope.selectedBusinessModel = {};
	$scope.sbiModule_config=sbiModule_config;
	$scope.sbiModule_user=sbiModule_user;
	$scope.currentBusinessModelParameterCategories = [];
	$scope.openedItems = [];

	$scope.openBusinessModel = function(model,e){
		e.stopPropagation();
		if($scope.openedItems.indexOf(model.uniqueName)!==-1){
			$scope.openedItems.splice($scope.openedItems.indexOf(model.uniqueName),1);
		}else{
			$scope.openedItems.push(model.uniqueName);
		}
	}

	$scope.selectBusinessModel = function(node) {
		$scope.tabResource.selectedBusinessTab="propertiestab";
		$scope.selectedBusinessModel = node;
		$scope.$apply();
		angular.copy(parametersBuilder.extractCategories($scope.selectedBusinessModel.properties), $scope.currentBusinessModelParameterCategories);
	};

	$scope.getBusinessModelType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			var key = Object.keys(prop[i])[0];
			if(angular.equals(key,"structural.tabletype")){
				return prop[i][key].value;
			}
		}
		return "generic";
	};

	$scope.getBusinessModelColumnsType=function(bm){
		var prop=bm.properties;
		for(var i=0;i<prop.length;i++){
			var key = Object.keys(prop[i])[0];
			if(angular.equals(key,"structural.columntype")){
				return prop[i][key].value;
			}
		}
	};

	//Ag-grid table revamp
	$scope.businessClassesGrid = {
		angularCompileRows: true,
		domLayout: 'autoHeight',
		enableColResize: false,
        enableSorting: false,
        enableFilter: false,
        rowDragManaged: true,
        headerHeight: 0,
        onRowDragEnter: rowDragEnter,
        onRowDragEnd: onRowDragEnd,
        onGridReady: resizeColumns,
        onGridSizeChanged: resizeColumns,
        rowSelection: 'single',
        onRowClicked: rowSelection,
        columnDefs: [{"headerName":sbiModule_translate.load("sbi.generic.name"),"field":"name",rowDrag: true,cellRenderer: fullWidthRow }],
    	rowData : $scope.meta.businessModels
	};

	//Ag-grid table business view
	$scope.businessViewsGrid = {
		angularCompileRows: true,
		domLayout: 'autoHeight',
		enableColResize: false,
        enableSorting: false,
        enableFilter: false,
        rowDragManaged: true,
        headerHeight: 0,
        onRowDragEnter: rowDragEnter,
        onRowDragEnd: onRowDragEnd,
        onGridReady: resizeColumns,
        onGridSizeChanged: resizeColumns,
        rowSelection: 'single',
        onRowClicked: rowSelection,
        columnDefs: [{"headerName":sbiModule_translate.load("sbi.generic.name"),"field":"name",rowDrag: true,cellRenderer: fullWidthRow }],
    	rowData : $scope.meta.businessViews
	};

	$scope.$on('updateBusinessClassesGrid',function(event,data){
		$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
	})

	$scope.$on('updateBusinessViewsGrid',function(event,data){
		if($scope.businessViewsGrid.api) {
			$scope.businessViewsGrid.api.setRowData($scope.meta.businessViews);
		}
	})

	$scope.$watch('selectedBusinessModel.name',function(newValue,oldValue){
		if($scope.selectedBusinessModel && newValue!=oldValue){
			$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
			$scope.businessViewsGrid.api.setRowData($scope.meta.businessViews);
		}

	},true)

	function moveInArray(arr, fromIndex, toIndex) {
        var element = arr[fromIndex];
        arr.splice(fromIndex, 1);
        arr.splice(toIndex, 0, element);
    }

	function rowDragEnter(event){
		$scope.startingDragRow = event.overIndex;
	}
	function onRowDragEnd(event){
		var diff = event.overIndex - $scope.startingDragRow;
		$scope.moveBusinessClass($scope.startingDragRow,diff);
		//moveInArray($scope.meta.businessModels, $scope.startingDragRow, event.overIndex);
		//$scope.businessClassesGrid.api.redrawRows();
	}

	function resizeColumns(params){
		if($scope.meta.businessModels) $scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
		params.api.sizeColumnsToFit();
	}

	function fullWidthRow(params){
		return '<div layout="row" layout-align="start center">'+
			   	'<i class="fa fa-bar"></i> <span>'+params.value+'</span><span flex></span>'+
			   	'<span class="miniChip" style="padding: 0 8px; height: 20px;line-height:20px;">'+params.data.columns.length+ ' '+$scope.translate.load('sbi.glossary.attributes') +'</span>'+
			   '</div>';
	}

	function rowSelection(params){
		$scope.selectBusinessModel(params.data);
	}

	$scope.businessModelIconType={
			"generic" :"fa fa-table",
			"cube":"fa fa-cube",
			"dimension":"fa fa-square-o",
			"temporal dimension":"fa fa-calendar",
			"time dimension":"fa fa-clock-o",
			"geographic dimension":"fa fa-globe",
			"measure":"fa fa-barcode",
			"attribute":"fa fa-circle-o",
			"calendar":"fa fa-calendar-check-o",
	};

	$scope.businesslModel_getlevelIcon = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			// is business model node

			// TO-DO manage folder by type
			return $scope.businessModelIconType[$scope.getBusinessModelType(node)] ||  "fa fa-table";
		} else {
			// is column node
			if (node.identifier == true) {
				return "fa fa-key goldKey"
			}
			return $scope.businessModelIconType[$scope.getBusinessModelColumnsType(node)];

		}
	};

	$scope.businessModel_isFolder = function(node) {
		if (node.hasOwnProperty("simpleBusinessColumns")) {
			return !node.expanded;
		}else{
			return true
		}
	};
	$scope.moveBusinessClass = function(index,direction){
		sbiModule_restServices.promisePost("1.0/metaWeb", "moveBusinessClass",metaModelServices.createRequestRest({index:index,direction:direction}))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
	}
	$scope.moveBusinessColumn=function(index,direction,businessModel){
		sbiModule_restServices.promisePost("1.0/metaWeb", "moveBusinessColumn",metaModelServices.createRequestRest({businessModelUniqueName:businessModel.uniqueName,index:index,direction:direction}))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
	}

	$scope.moveUp = function(index, businessModel) {

		$scope.moveBusinessColumn(index,-1,businessModel)
	};

	$scope.moveDown = function(index, businessModel) {

		$scope.moveBusinessColumn(index,1,businessModel)
	};

	$scope.addBusinessModel=function(){
		$mdDialog.show({
			controller: addBusinessClassController,
			preserveScope: true,
			locals: {businessModel:$scope.meta.businessModels, physicalModel: $scope.meta.physicalModels, businessClassesGrid: $scope.businessClassesGrid},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessClass.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
			$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
		});
	};

	$scope.addBusinessView=function(editMode){
		$mdDialog.show({
			controller: addBusinessViewController,
			preserveScope: true,
			locals: { originalPhysicalModel: $scope.meta.physicalModels,selectedBusinessModel: $scope.selectedBusinessModel,editMode:editMode, businessViewsGrid: $scope.businessViewsGrid},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addBusinessView.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
			$scope.businessViewsGrid.api.setRowData($scope.meta.businessViews);
		});
	};

	$scope.deleteCurrentBusiness=function(){
		var isBusinessClass=!$scope.selectedBusinessModel.hasOwnProperty("joinRelationships");

		 var confirm = $mdDialog.confirm()
		 .title( isBusinessClass ? sbiModule_translate.load("sbi.meta.delete.businessclass") : sbiModule_translate.load("sbi.meta.delete.businessview") )
		 .ariaLabel('delete Business')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {
			   //delete the item;
			   sbiModule_restServices.promisePost("1.0/metaWeb",(isBusinessClass ? "deleteBusinessClass" : "deleteBusinessView"),metaModelServices.createRequestRest({name:$scope.selectedBusinessModel.uniqueName}))
			   .then(function(response){
					metaModelServices.applyPatch(response.data);
					$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
					$scope.businessViewsGrid.api.setRowData($scope.meta.businessViews);
					$scope.selectedBusinessModel=undefined;
			   },function(response){
				   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
			   })


		   }, function() {
		   });
	};

	$scope.editTemporalHierarchy=function(){
		var config = {
				attachTo:  angular.element(document.body),
				controller: editTemporalHierarchyController,
				disableParentScroll: true,
				templateUrl: sbiModule_config.contextName + '/js/src/meta/templates/editTemporalHierarchy.jsp',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {selectedBusinessModel:$scope.selectedBusinessModel,originalOlapModels:$scope.meta.olapModels},

		};

		$mdPanel.open(config);
	}

}

function businessModelPropertyControllerFunction($scope, sbiModule_translate,sbiModule_restServices, parametersBuilder,$timeout){

	$scope.businessModelMiscInfo = [ {
		name : "name",
		label : sbiModule_translate.load("name")
	}, {
		name : "description",
		label : sbiModule_translate.load("description")
	}
	];


	$scope.initRoleVisibility=function(rv,val){
		if(!angular.equals("",val)){
			angular.copy(val.split(";"),rv );
		}
	}
	$scope.buildRoleVisibility=function(rv,val){
		val.value=rv.join(";");
	}

	$scope.getPropertyAttributes = function(prop){
		return prop[Object.keys(prop)[0]];
	}

	$scope.getPropertyKey = function(prop){
		return Object.keys(prop)[0];
	}
}

function businessModelAttributeControllerFunction($scope, $timeout,$mdDialog, sbiModule_translate, sbiModule_config, sbiModule_restServices, parametersBuilder,sbiModule_config,metaModelServices,$mdPanel ){


	$scope.addUnusedColumns = function(){
		console.log("adding unused columns!!!")

		$mdDialog.show({
			controller: function($scope,selectedBusinessModel,physicalModels,$filter,addColumns,$mdDialog){

				$scope.selectedBusinessModel = selectedBusinessModel;
				$scope.translate = sbiModule_translate;
				$scope.physicalTable = physicalModels[$scope.selectedBusinessModel.physicalTable.physicalTableIndex];
				$scope.allColumns = angular.copy($scope.physicalTable.columns)
				$scope.unUsedColumns = [];

				var contains = function(item,itemPropertyName,array,arrayItemPropertyName){
					for(var i in array){
						if(item[itemPropertyName] === array[i][arrayItemPropertyName]){
							return true;
						}
					}

					return false;
				}

				$scope.hasUnUsedColumns = function(){
					return $scope.unUsedColumns && $scope.unUsedColumns.length > 0;
				}

				// Create the unused columns list ignoring deleted columns
				$scope.allColumns
					.filter(function(el) { return !el.markedDeleted; })
					.filter(function(el) { return !contains(el,"name",$scope.selectedBusinessModel.columns,"uniqueName"); })
					.forEach(function(el) { $scope.unUsedColumns.push(el); });

				console.log($scope.selectedBusinessModel)
				console.log(physicalModels)

				$scope.save = function(){
					var toCreate = []
					for(var i in $scope.unUsedColumns){
						if($scope.unUsedColumns[i].selected){
							toCreate.push({
								businessModelUniqueName:$scope.selectedBusinessModel.uniqueName,
								physicalColumnName:$scope.unUsedColumns[i].name,
								physicalTableName:$scope.physicalTable.name
							})
						}
					}

					addColumns(toCreate)
					$mdDialog.hide();
				}

				$scope.cancel = function(){
					$mdDialog.hide();
				}


			},
			preserveScope: true,
			locals: {
				selectedBusinessModel:$scope.selectedBusinessModel,
				physicalModels:$scope.meta.physicalModels,
				addColumns:$scope.createBusinessColumnFromPhysicalColumns
				},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addUnusedFields.jsp',
			clickOutsideToClose:true,
			escapeToClose :true,
			fullscreen: true
		});


	}
	$scope.moveBusinessColumn=function(index,direction,businessModel){
		sbiModule_restServices.promisePost("1.0/metaWeb", "moveBusinessColumn",metaModelServices.createRequestRest({businessModelUniqueName:businessModel.uniqueName,index:index,direction:direction}))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
				$scope.attributesGrid.api.redrawRows();
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
	}

	//Ag-grid table revamp
	$scope.attributesGrid = {
			angularCompileRows: true,
			enableColResize: true,
	        enableSorting: false,
	        enableFilter: false,
	        rowDragManaged: true,
	        onRowDragEnter: rowDragEnter,
	        onRowDragEnd: onRowDragEnd,
	        onGridReady: resizeColumns,
	        onGridSizeChanged: resizeColumns,
	        onCellEditingStopped: refreshRow,
	        singleClickEdit: true,
	        domLayout: "autoHeight",
	        columnDefs: [{"headerName":sbiModule_translate.load("sbi.generic.name"),"field":"name",rowDrag: true, "editable":true,cellRenderer:editableCellWithIcon, cellClass: 'editableCell',width: 90},
	    		{"headerName":sbiModule_translate.load("sbi.meta.model.table.primaryKey"),"field":"identifier",cellRenderer:checkboxRenderer,width: 40},
	    		{"headerName":sbiModule_translate.load("sbi.execution.subobjects.visibility"),"field":"Visibility",cellRenderer:visibilityCheckboxRenderer,width: 40},
	    		{"headerName":sbiModule_translate.load("sbi.generic.type"),"field":"type","editable":true,cellRenderer:typeEditableCell, cellClass: 'editableCell',cellEditor:"agSelectCellEditor",cellEditorParams: {values: ['attribute','measure']} , width: 50},
	    		{"headerName":sbiModule_translate.load("sbi.generic.descr"),"field":"description","editable":true,cellRenderer:editableCell, cellClass: 'editableCell'},
	    		{"headerName":"",cellRenderer: buttonRenderer,"field":"id","cellClass":"singlePinnedButton","cellStyle":{"border":"none !important","text-align": "center","display":"inline-flex","justify-content":"center"},width: 20,pinned: 'right'}],
	    	rowData : $scope.selectedBusinessModel.columns
		};

	function moveInArray(arr, fromIndex, toIndex) {
        var element = arr[fromIndex];
        arr.splice(fromIndex, 1);
        arr.splice(toIndex, 0, element);
    }

	function rowDragEnter(event){
		$scope.startingDragRow = event.overIndex;
	}
	function onRowDragEnd(event){
		var diff = event.overIndex - $scope.startingDragRow;
		//moveInArray($scope.selectedBusinessModel.columns, $scope.startingDragRow, event.overIndex);
		$scope.moveBusinessColumn($scope.startingDragRow,diff,$scope.selectedBusinessModel);
		//$scope.attributesGrid.api.redrawRows();
	}

	function resizeColumns(params){
		params.api.sizeColumnsToFit();
	}

	$scope.getIcon = function(attribute){
		for(var k in attribute.properties){
			if(attribute.properties[k].hasOwnProperty('structural.datatype')){
				if (attribute.properties[k]['structural.datatype'].value == 'VARCHAR') return 'fa fa-font';
				else if (['INTEGER','DOUBLE','DECIMAL','BIGINT','FLOAT'].indexOf(attribute.properties[k]['structural.datatype'].value) != -1) return 'fa fa-barcode';
				else if (['DATE','TIME','TIMESTAMP'].indexOf(attribute.properties[k]['structural.datatype'].value) != -1) return 'fa fa-calendar';
				else return 'fa fa-circle-o';
				break;
			}
		}
	}

	function editableCellWithIcon(params){
		var startString = '<i class="'+$scope.getIcon(params.data)+'"></i> <i class="fa fa-edit"></i>'
		if(typeof(params.value) !== 'undefined' && params.value != ""){
			return startString + '<i class="truncated" style="width: calc(100% - 32px)">'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>';
		}else return startString+'<i></i>';
	}

	function editableCell(params){
		if(typeof(params.value) !== 'undefined' && params.value != ""){
			return '<i class="fa fa-edit"></i><i class="truncated" style="width: calc(100% - 12px)">'+params.value+'<md-tooltip>'+params.value+'</md-tooltip></i>';
		}else return '<i class="fa fa-edit"></i><i></i>';
	}

	function buttonRenderer(params){
		return '<md-button class="md-icon-button noMargin" ng-click="openDetails('+params.rowIndex+', $event)">'+
		'   <md-tooltip md-delay="500">Attribute Details</md-tooltip>'+
		'	<md-icon md-font-icon="fa fa-ellipsis-v" aria-label="attribute details"></md-icon>'+
		'</md-button>';
	}

	function checkboxRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-checked="isAttributePK('+params.rowIndex+')" ng-click="toggleAttributePrimaryKey('+params.rowIndex+')"/></div>';
	}
	function visibilityCheckboxRenderer(params){
		return '<div style="display:inline-flex;justify-content:center;width:100%;height:100%;align-items:center;"><input type="checkbox" ng-checked="isAttributeVisible('+params.rowIndex+')" ng-click="toggleAttributeVisibility('+params.rowIndex+')"/></div>';
	}
	function typeEditableCell(params){
		return '<i class="fa fa-edit"></i>' + params.data.properties[1]['structural.columntype']['value'];
	}

	function refreshRow(cell){
		if(cell.column.colDef.headerName == 'Type') cell.data.properties[1]['structural.columntype']['value'] = cell.value;
		$scope.attributesGrid.api.redrawRows({rowNodes: [$scope.attributesGrid.api.getDisplayedRowAtIndex(cell.rowIndex)]});
	}

	$scope.isAttributePK = function(index){
		return $scope.selectedBusinessModel.columns[index].identifier;
	}

	$scope.toggleAttributePrimaryKey = function(index){
		$scope.selectedBusinessModel.columns[index].identifier = !$scope.selectedBusinessModel.columns[index].identifier;
	}

	$scope.isAttributeVisible = function(index){
		for(var k in $scope.selectedBusinessModel.columns[index].properties){
			if ($scope.selectedBusinessModel.columns[index].properties[k].hasOwnProperty('structural.visible')){
				if ($scope.selectedBusinessModel.columns[index].properties[k]['structural.visible'].value=='true') return true;
				break;
			}
			return false;
		}
	}

	$scope.toggleAttributeVisibility = function(index){
		if($scope.selectedBusinessModel.columns[index].properties[0]['structural.visible']['value']=='true'){
			$scope.selectedBusinessModel.columns[index].properties[0]['structural.visible']['value'] = false;
		} else {
			$scope.selectedBusinessModel.columns[index].properties[0]['structural.visible']['value'] = true;
		}
	}

	$scope.openDetails = function(index, ev){
		$mdDialog.show({
		      controller: detailsDialogContent,
		      templateUrl: '/knowagemeta/js/src/meta/templates/attributesDialogTemplate.html',
		      parent: angular.element(document.body),
		      targetEvent: ev,
		      clickOutsideToClose:true,
		      locals: {attribute:$scope.selectedBusinessModel.columns[index],deleteBusinessColumn:$scope.deleteBusinessColumn,selectedBusinessModel:$scope.selectedBusinessModel}
		    })
	        .then(function(attribute) {
	        	if(attribute.deleteAttribute){
	        		$scope.selectedBusinessModel.columns.splice(index,1);
	        	}else{
	        		$scope.selectedBusinessModel.columns[index] = attribute;
	        	}
	        	$scope.attributesGrid.api.setRowData($scope.selectedBusinessModel.columns);
	        }, function() {
        });
	}

	function detailsDialogContent($scope, $mdDialog, attribute,deleteBusinessColumn,selectedBusinessModel){
  		$scope.translate=sbiModule_translate;
  		$scope.physicalColumn = attribute.physicalColumn;
  		$scope.sbiModule_config = sbiModule_config;
  		$scope.selectedAttribute = attribute;
  		var utilityMap = [];
  		$scope.properties = {};
  		for(var k in $scope.selectedAttribute.properties){
  			utilityMap.push(Object.keys($scope.selectedAttribute.properties[k])[0]);
  			$scope.properties[Object.keys($scope.selectedAttribute.properties[k])[0]] = $scope.selectedAttribute.properties[k][Object.keys($scope.selectedAttribute.properties[k])[0]];
  		}
  		$scope.thisObject = function(targetObj){
  			return targetObj[Object.keys(targetObj)[0]];
  		}

  		$scope.getExampleDate = function(date){
  			return moment().format(date);
  		}

  		$scope.deleteAttribute = function(){
  	    	$scope.selectedAttribute.deleteAttribute = true;
  			$mdDialog.hide($scope.selectedAttribute);
  		}

  		$scope.cancel = function(){
  			$mdDialog.cancel();
  		}
  		$scope.save = function(){
  			$mdDialog.hide($scope.selectedAttribute);
  		}

  		$scope.initRoleVisibility=function(rv,val){
  			if(!angular.equals("",val)){
  				angular.copy(val.split(";"),rv );
  			}
  		}

  		$scope.buildRoleVisibility=function(rv,val){
  			val.value=rv.join(";");
  		}

  		$scope.delete = function(selectedAttribute){
  			console.log("deleting field")
  			deleteBusinessColumn(selectedAttribute.uniqueName,selectedBusinessModel)
  			$mdDialog.cancel();
  		}
  	}//	$scope.existsBusinessModel = function(i) {
//		return ($scope.selectedBusinessModel.columns[i].physicalColumn!=undefined &&  angular.equals( $scope.selectedBusinessModel.columns[i].physicalColumn.name, $scope.selectedBusinessModel.columns[i].uniqueName));
//	}
//
//	$scope.isBusinessModelColumnPK = function(i) {
//		if($scope.selectedBusinessModel.columns[i].physicalColumn!=undefined &&  angular.equals( $scope.selectedBusinessModel.columns[i].physicalColumn.name, $scope.selectedBusinessModel.columns[i].uniqueName)){
//			return $scope.selectedBusinessModel.columns[i].identifier;
//		}else return false;
//	}
//
//	$scope.toggleBusinessModelColumnPK = function(i) {
//		var row = $scope.attributesList[i];
//		$scope.selectedBusinessModel.columns[i].identifier = !$scope.selectedBusinessModel.columns[i].identifier;
//	}

//	$scope.selectedBusinessModelAttributes = [
//	                              			{
//	                              				label : sbiModule_translate.load("sbi.generic.name"),
//	                              				name : "name",
//	                              				transformer: function(row){
//	                              					var bc=$scope.selectedBusinessModelAttributesScopeFunctions.BusinessColumnFromPc(row);
//	                              					if(bc==null){
//	                              						return row;
//	                              					}else{
//	                              						return bc.name;
//	                              					}
//
//	                              				}
//	                              			},
//	                              			{
//	                              				label : sbiModule_translate.load("sbi.meta.model.table.primaryKey"),
//	                              				name : "identifier",
//	                              				transformer : function(row) {
//	                              					return "<md-checkbox ng-disabled='!scopeFunctions.existsBusinessModel(row)' ng-checked='scopeFunctions.isBusinessModelColumnPK(row)' ng-click='scopeFunctions.toggleBusinessModelColumnPK(row)' aria-label='isPrimaryKey'></md-checkbox>"
//	                              					// return "<md-checkbox ng-model='row.identifier'
//	                              					// aria-label='isPrimaryKey'></md-checkbox>"
//	                              				}
//	                              			},
//	                              			{
//	                              				label : sbiModule_translate.load("sbi.meta.model.inuse"),
//	                              				name : "added",
//	                              				transformer : function(row) {
//	                              					return "<md-checkbox ng-checked='scopeFunctions.existsBusinessModel(row)' ng-click='scopeFunctions.toggleBusinessModel(row)' aria-label='isPrimaryKey'></md-checkbox>"
//	                              				}
//	                              			}
//
//	                              	];

	// add referenced table if is a business view
//	if($scope.selectedBusinessModel.hasOwnProperty("joinRelationships")){
//		$scope.selectedBusinessModelAttributes.push({
//			label : sbiModule_translate.load("sbi.meta.model.sourcetable"),
//			name : "tableName",
//		});
//	}
//
//
//
//
//
//	$scope.isPresentInCalculatedColumn=function(bc,businessModel){
//		for(var i=0;i<businessModel.calculatedBusinessColumns.length;i++){
//			for(var j=0;j<businessModel.calculatedBusinessColumns[i].referencedColumns.length;j++){
//				if(angular.equals(businessModel.calculatedBusinessColumns[i].referencedColumns[j].uniqueName,bc.uniqueName)){
//					return businessModel.calculatedBusinessColumns[i].uniqueName;
//				}
//			}
//		}
//		return false
//	}

//	$scope.isPresentInOlapHierarchy=function(businessColumn,businessClass){
//		var olapModels = $scope.meta.olapModels;
//		for (var i=0; i< olapModels.length; i++){
//			var dimensions = olapModels[i].dimensions
//			for (var j=0; j < dimensions.length; j++){
//				var hierarchies = dimensions[j].hierarchies;
//				for (var k=0; k < hierarchies.length; k++){
//					var levels = hierarchies[k].levels;
//					for (var z=0; z < levels.length; z++ ){
//						if (angular.equals(levels[z].column.uniqueName,businessColumn.uniqueName)){
//							return hierarchies[k].name;
//						}
//					}
//				}
//			}
//		}
//		return false;
//	}
//
	$scope.createBusinessColumnFromPhysicalColumns=function(columns){
		sbiModule_restServices.promisePost("1.0/metaWeb", "createBusinessColumn",metaModelServices.createRequestRest({columns:columns}))
		   .then(function(response){
				metaModelServices.applyPatch(response.data);
				$scope.attributesGrid.api.setRowData($scope.selectedBusinessModel.columns);
				$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
		   },function(response){
			   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		   })
	}

	$scope.deleteBusinessColumn=function(businessColumnUniqueName,businessModel){
		sbiModule_restServices.promisePost("1.0/metaWeb", "deleteBusinessColumn",metaModelServices.createRequestRest({businessColumnUniqueName:businessColumnUniqueName ,businessModelUniqueName:businessModel.uniqueName}))
		.then(function(response){
			metaModelServices.applyPatch(response.data);
			$scope.attributesGrid.api.setRowData($scope.selectedBusinessModel.columns);
			$scope.businessClassesGrid.api.setRowData($scope.meta.businessModels);
		},function(response){
			sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
		})
	}

}

function businessViewJoinRelationshipsControllerFunction($scope,sbiModule_translate, sbiModule_restServices){
	$scope.selectedBusinessViewJoinRelationships=[
	   {
		   label:sbiModule_translate.load("sbi.generic.name"),
		   name:'name'
	   },
	   {
		   label:sbiModule_translate.load("sbi.meta.source.columns"),
		   name:'sourceColumns',
		   transformer:function(item){
	    		var toret=[];
	    		for(var i=0;i<item.length;i++){
	    			 toret.push(item[i].tableName+"."+item[i].name);
	    		}
	    		return toret.join(",");
		   }
	   },
	   {
		   label:sbiModule_translate.load("sbi.meta.target.columns"),
		   name:'destinationColumns',
		   transformer:function(item){
			   var toret=[];
	    		for(var i=0;i<item.length;i++){
	    			 toret.push(item[i].tableName+"."+item[i].name);
	    		}
	    		return toret.join(",");
	    	   }
	       }
	   ]
	}

function calculatedBusinessColumnsControllerFunction($scope,sbiModule_translate, sbiModule_restServices,$mdDialog,sbiModule_config,metaModelServices ){
	$scope.selectedBusinessModelCalculatedBusinessColumns=[
		                                              {
		                                            	  label:sbiModule_translate.load("sbi.generic.name"),
		                                            	  name:'name'
		                                              }
	                                              ]
	$scope.selectedBusinessModelCalculatedBusinessColumnsScopeFunctions={
			translate:sbiModule_translate,
			addCalculatedField : function(){
				$scope.addCalculatedField(false);
			}
	}
	$scope.addCalculatedField=function(editMode,currentCF){
		$mdDialog.show({
			controller: addCalculatedFieldController,
			preserveScope: true,
			locals: {selectedBusinessModel:$scope.selectedBusinessModel,editMode:editMode,currentCF:currentCF},
			templateUrl:sbiModule_config.contextName + '/js/src/meta/templates/addCalculatedField.jsp',
			clickOutsideToClose:false,
			escapeToClose :false,
			fullscreen: true
		}).then(function(){
		});
	}

	$scope.calculatedFieldSpeedOption=[
	                                   {
										label : sbiModule_translate.load("sbi.generic.delete"),
										icon:'fa fa-trash' ,
										backgroundColor:'transparent',
										color:'black',
										action : function(item,event) {
											$scope.deleteCalculatedField(item);
										}
										 },
	                                   {
	                                	   label : sbiModule_translate.load("sbi.generic.edit"),
	                                	   icon:'fa fa-pencil' ,
	                                	   backgroundColor:'transparent',
	                                	   color:'black',
	                                	   action : function(item,event) {
	                                		   $scope.addCalculatedField(true,item);
	                                	   }
	                                   }
			];

	$scope.deleteCalculatedField=function(item){
		 var confirm = $mdDialog.confirm()
		 .title(sbiModule_translate.load("sbi.meta.delete.calculatedField"))
		 .ariaLabel('delete Calculated')
		 .ok(sbiModule_translate.load("sbi.general.continue"))
		 .cancel(sbiModule_translate.load("sbi.general.cancel"));
		   $mdDialog.show(confirm).then(function() {

			   //delete the item;
			   sbiModule_restServices.promisePost("1.0/metaWeb", "deleteCalculatedField",metaModelServices.createRequestRest({name:item.name,sourceTableName:$scope.selectedBusinessModel.uniqueName}))
			   .then(function(response){
					metaModelServices.applyPatch(response.data);
			   },function(response){
				   sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.genericError"));
			   })


		   }, function() {
		   });
	}
}
