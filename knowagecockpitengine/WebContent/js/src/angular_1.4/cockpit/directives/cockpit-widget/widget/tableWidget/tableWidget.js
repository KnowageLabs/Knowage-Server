/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 * 
 */
(function() {
	angular.module('cockpitModule')
	.directive('cockpitTableWidget',function(cockpitModule_widgetServices,$mdDialog){
		return{
			templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/tableWidget/templates/tableWidgetTemplate.html',
			controller: cockpitTableWidgetControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
						element[0].classList.add("flex");
						element[0].classList.add("layout-column");
						element[0].classList.add("layout-fill");
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {
						//init the widget
						element.ready(function () {
							scope.initWidget();
						});



					}
				};
			}
		}
	});

	function cockpitTableWidgetControllerFunction($scope,cockpitModule_widgetConfigurator,$mdDialog,$timeout,$mdPanel,$q,cockpitModule_datasetServices, $mdToast, sbiModule_translate,sbiModule_restServices,cockpitModule_widgetServices,cockpitModule_widgetSelection){
		$scope.selectedTab = {'tab' : 0};
		$scope.widgetIsInit=false;
		$scope.totalCount = 0;
		$scope.translate = sbiModule_translate;
		$scope.summaryRow = {};
		$scope.datasetRecods = {};
		if($scope.ngModel.style==undefined){
			$scope.ngModel.style={};
		};
		
		
		$scope.tableFunction={
				widgetStyle:$scope.ngModel.style,			
		}

		$scope.getGridStyle=function(row,column,index){
			var gridStyle = {};
			//style summary row
			if($scope.ngModel.style.showSummary == true && index == $scope.datasetRecods.rows.length-1){
				return $scope.ngModel.style.summary;
			}
			if($scope.ngModel.style.grid !=undefined && $scope.ngModel.style.showGrid){
				gridStyle = angular.extend(gridStyle,$scope.ngModel.style.grid);
			}
			if($scope.ngModel.style.showAlternateRows){
				if((index % 2 == 0) && $scope.ngModel.style.alternateRows.evenRowsColor!=undefined ){
					gridStyle["background"] = $scope.ngModel.style.alternateRows.evenRowsColor;
				}
				if((Math.abs(index % 2) == 1) && $scope.ngModel.style.alternateRows.oddRowsColor!=undefined ){
					gridStyle["background"] = $scope.ngModel.style.alternateRows.oddRowsColor;
				}
			}
			var ind = $scope.indexInList(column.label, $scope.ngModel.content.columnSelectedOfDataset);
			if(ind!=-1 && $scope.ngModel.content.columnSelectedOfDataset[ind].style != undefined){
				gridStyle = angular.extend({},gridStyle,$scope.ngModel.content.columnSelectedOfDataset[ind].style);
			}
			return gridStyle;
		}
		
		$scope.selectRow=function(row,column,listId,index,evt,columnName){
			for(var i=0;i<$scope.ngModel.content.columnSelectedOfDataset.length;i++){
				if(angular.equals($scope.ngModel.content.columnSelectedOfDataset[i].alias,columnName)){
					if($scope.ngModel.content.columnSelectedOfDataset[i].isCalculated){
						return;
					}else{
						break;
					}
				}
			}
			$scope.doSelection(columnName,column);
		}

		$scope.calculatedRow = function(row,column,alias){

			var index = $scope.indexInList(alias, $scope.ngModel.content.columnSelectedOfDataset);

			if(index != -1){
				var formulaArray = $scope.ngModel.content.columnSelectedOfDataset[index].formula;
				var value = "";

				for(var i=0;i<formulaArray.length;i++){
					var obj = formulaArray[i];
					if(obj.type == 'measure'){
						value = value+column[obj.value];
					} else{
						value = value + obj.value;
					}
				}
				column[alias] = ""+$scope.$eval(value);
				row = column[alias];
				return row;
			}
		}

		$scope.indexInList=function(item, list) {
			for (var i = 0; i < list.length; i++) {
				var object = list[i];
				if(object.aliasToShow==item){
					return i;
				}
			}
			return -1;
		};


		$scope.lastChangePageConf={};
		$scope.changeDocPage = function(searchValue, itemsPerPage, currentPageNumber , columnsSearch,columnOrdering, reverseOrdering){
			if($scope.gridsterItem.hasClass("gridster-item-resizing") || !$scope.widgetIsInit ){
				return
			}
			var time=(new Date()).getTime();
			$scope.lastChangePageConf=time;
			$timeout(function(){
				if(angular.equals(time,	$scope.lastChangePageConf)){
					currentPageNumber--;
					var numberOfElement = angular.copy(itemsPerPage);
					if(searchValue==undefined || searchValue.trim().lenght==0 ){
						searchValue='';
					}
					if($scope.ngModel.style.showSummary == true){
						numberOfElement--;
					}
					if($scope.ngModel.content.maxRowsNumber !=undefined){
						numberOfElement = angular.copy($scope.ngModel.content.maxRowsNumber)
						if($scope.ngModel.style.showSummary == true){
							numberOfElement--;
						}
						var options = {page:currentPageNumber, itemPerPage:numberOfElement, columnOrdering:columnOrdering,reverseOrdering:reverseOrdering };
						$scope.refreshWidget(options);

					}else{
						var options = {page:currentPageNumber, itemPerPage:numberOfElement, columnOrdering:columnOrdering,reverseOrdering:reverseOrdering };
						$scope.refreshWidget(options);
					}
				}
			},500);
			 
		};
		$scope.isMobile = {
			    Android: function() {
			        return navigator.userAgent.match(/Android/i);
			    },
			    BlackBerry: function() {
			        return navigator.userAgent.match(/BlackBerry/i);
			    },
			    iOS: function() {
			        return navigator.userAgent.match(/iPhone|iPad|iPod/i);
			    },
			    Opera: function() {
			        return navigator.userAgent.match(/Opera Mini/i);
			    },
			    Windows: function() {
			        return navigator.userAgent.match(/IEMobile/i);
			    },
			    any: function() {
			        return ($scope.isMobile.Android() || $scope.isMobile.BlackBerry() || $scope.isMobile.iOS() || $scope.isMobile.Opera() || $scope.isMobile.Windows());
			    }
			};

		$scope.canSeeColumnByMobile = function(obj){
			if($scope.isMobile.any()!=null && obj!=undefined && obj.hideonMobile == true){
				return false;
			}
			return true;
		}
		
		$scope.refresh=function(element,width,height, datasetRecords,nature){
			if(angular.equals(nature,'fullExpand')){
				return
			}
			$scope.columnsToShow = [];
			$scope.datasetRecords = {};
			$scope.columnToshowinIndex = [];
			$scope.tableFunction.widgetStyle=$scope.ngModel.style;
			$scope.datasetRecods = datasetRecords;
			if($scope.ngModel.content.columnSelectedOfDataset!=undefined){
				$scope.datasetRecords  =datasetRecords;
				for(var i=0;i<$scope.ngModel.content.columnSelectedOfDataset.length;i++){
					var obj = {};
					obj.label= $scope.ngModel.content.columnSelectedOfDataset[i]['aliasToShow'];
					obj.name = $scope.ngModel.content.columnSelectedOfDataset[i]['alias'];
					obj.static=true;
					if($scope.ngModel.content.columnSelectedOfDataset[i].isCalculated){
						obj.customRecordsClass="noClickCursor";
					}
					obj.style = $scope.getGridStyle;
					if($scope.canSeeColumnByMobile($scope.ngModel.content.columnSelectedOfDataset[i].style)){
						$scope.columnsToShow.push(obj);
					}
					if($scope.datasetRecords != undefined){
						for(var j=1;j<$scope.datasetRecords.metaData.fields.length;j++){
							if($scope.datasetRecords.metaData.fields[j].header == $scope.ngModel.content.columnSelectedOfDataset[i]['alias']){
								$scope.columnToshowinIndex.push($scope.datasetRecords.metaData.fields[j].dataIndex);
							}
						}
					}

				}
				$scope.itemList=$scope.getRows($scope.columnToshowinIndex,$scope.datasetRecords);
				$scope.tableColumns=$scope.columnsToShow;
				if(datasetRecords !=undefined){
					$scope.totalCount = datasetRecords.results;
				}

			}


		}

		$scope.getRows = function(indexList, values){
			var table = [];
			if($scope.columnToshowinIndex.length >0 && values != undefined){
				for(var i=0;i<values.rows.length;i++){
					var obj = {};
					for(var j=0;j<indexList.length;j++){
						for(var k=1;k<values.metaData.fields.length;k++){
							if(indexList[j] == values.metaData.fields[k].dataIndex ){
								var style = $scope.ngModel.content.columnSelectedOfDataset[k-1].style;
								obj[values.metaData.fields[k].header] = values.rows[i][indexList[j]];

								
								if(style!=undefined && style.precision != undefined){
									obj[values.metaData.fields[k].header] = parseFloat(obj[values.metaData.fields[k].header]).toPrecision(style.precision);
								}
								if(style!=undefined && style.prefix !=undefined){
									obj[values.metaData.fields[k].header] = style.prefix +obj[values.metaData.fields[k].header];
								}
								if(style!=undefined && style.suffix !=undefined){
									obj[values.metaData.fields[k].header] = obj[values.metaData.fields[k].header] + style.suffix;
								}
							}
						}
					}
//					if($scope.ngModel.style.showSummary == true && i == values.rows.length-1){
//						//the last is summary row
//						$scope.summaryRow  = angular.copy(obj)
//					}else{
						table.push(obj);

//					}
				}
			}
			return table;
		}

		$scope.presentInTable = function(table, obj){
			for(var i=0;i<table.length;i++){
				var objT = table[i];
				if(objT == obj){
					return true
				}
			}
			return false;
		}
		
		$scope.tableColumns=$scope.ngModel.content.columnSelectedOfDataset;
		$scope.itemList=[]

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
		$scope.init=function(element,width,height){
			$scope.refreshWidget();
			$timeout(function(){
				$scope.widgetIsInit=true;
			},500);
			
		}
		$scope.getOptions =function(){
			var obj = {};
//			if($scope.ngModel.content.fixedRow == true && $scope.ngModel.content.maxRowsNumber != undefined){
				
				obj["page"] =0;
				obj["itemPerPage"] = $scope.ngModel.content.maxRowsNumber ;
				if($scope.ngModel.style.showSummary == true){
					obj["itemPerPage"]--;
				}
//			}
			return obj;
			
		}
		
		$scope.exportCsv=function(obj){
			var deferred = obj.def;
			var csv = '';
			var metas = this.ngModel.content.columnSelectedOfDataset;
			for(var k = 0; k < metas.length; k++){
				csv += metas[k].aliasToShow + ';';
			}
			csv += '\n';
			var datasetId = this.ngModel.dataset.dsId;
			var model = {content: {columnSelectedOfDataset: this.ngModel.content.columnSelectedOfDataset}};
			cockpitModule_datasetServices.loadDatasetRecordsById(datasetId, undefined, undefined, undefined, undefined, model).then(function(allDatasetRecords){
				obj.csvData = {};
				var allRows = $scope.getRows($scope.columnToshowinIndex, allDatasetRecords);
				var rows = allDatasetRecords.rows;
				allDatasetRecords = null;
				var numRecs = rows.length;
				for(var recIndex = 0; recIndex < numRecs; recIndex++){
					for(var col = 0; col < $scope.columnToshowinIndex.length; col++){
						csv += rows[recIndex][$scope.columnToshowinIndex[col]] + ';' 
					}
					csv += '\n';
				}
				obj.csvData = csv;
				deferred.resolve(obj);
			},function(error){
				deferred.reject(error);
			});
		}
		
		$scope.editWidget=function(index){
			var finishEdit=$q.defer();
			var config = {
					attachTo:  angular.element(document.body),
					controller: tableWidgetEditControllerFunction,
					disableParentScroll: true,
					templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/tableWidget/templates/tableWidgetEditPropertyTemplate.html',
					position: $mdPanel.newPanelPosition().absolute().center(),
					fullscreen :true,
					hasBackdrop: true,
					clickOutsideToClose: false,
					escapeToClose: false,
					focusOnOpen: true,
					preserveScope: true,
					autoWrap:false,
					locals: {finishEdit:finishEdit,originalModel:$scope.ngModel, getMetadata : $scope.getMetadata,scopeFather : $scope},
					
			};

			$mdPanel.open(config);
			return finishEdit.promise;

		}

	};

	function tableWidgetEditControllerFunction($scope,finishEdit,sbiModule_translate,$mdDialog,originalModel,mdPanelRef,getMetadata,scopeFather,$mdToast){
		$scope.translate=sbiModule_translate;
		$scope.fontFamily = ['Times New Roman','Georgia', 'Serif'];
		$scope.fontWeight = ['normal','bold','bolder','lighter','number','initial','inherit'];
		$scope.getMetadata = getMetadata;
		$scope.model = {};
		angular.copy(originalModel,$scope.model);
		
		$scope.colorPickerProperty={format:'rgb', placeholder:sbiModule_translate.load('sbi.cockpit.color.select')};
		$scope.colorPickerPropertyEvenOddRows = {placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:!$scope.model.style.showAlternateRows};
		$scope.colorPickerPropertyGrid = {placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb',disabled:!$scope.model.style.showGrid};
		$scope.saveConfiguration=function(){
			if($scope.model.dataset == undefined || $scope.model.dataset.dsId == undefined ){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
				return;
			}
			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
				return;
			}
			if(!$scope.checkAggregation()){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.erroraggregation'));
				return;
			}
			angular.copy($scope.model,originalModel);
			mdPanelRef.close();
			mdPanelRef.destroy();
			var options = {page:0, itemPerPage:$scope.model.content.maxRowsNumber-1, columnOrdering:undefined,reverseOrdering:undefined };
			scopeFather.refreshWidget(options);
			$scope.$destroy();
			if($scope.model.content.columnSelectedOfDataset == undefined || $scope.model.content.columnSelectedOfDataset.length==0){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.nocolumns'));
			}
			finishEdit.resolve();
		}
		$scope.enableAlternate = function(){
			$scope.colorPickerPropertyEvenOddRows['disabled'] = $scope.model.style.showAlternateRows;
		}
		$scope.enableGrid =  function(){
			$scope.colorPickerPropertyGrid['disabled'] = $scope.model.style.showGrid;
		}
		$scope.checkAggregation = function(){
			var measures =0;
			var noneAggr =0;
			for(var i=0;i<$scope.model.content.columnSelectedOfDataset.length;i++){
				var column = $scope.model.content.columnSelectedOfDataset[i];
				if(column.fieldType == 'MEASURE'){
					measures++;
					if(column.aggregationSelected == 'NONE'){
						noneAggr++;
					}
				}
			}
			if(noneAggr!=0){
				if(noneAggr != measures){
					return false;
				}
			}
			return true;
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
		$scope.cancelConfiguration=function(){
			mdPanelRef.close();
			mdPanelRef.destroy();
			$scope.$destroy();
			finishEdit.reject();
		
		}

		$scope.canSeeGrid = function(){
			if($scope.model!=undefined){
				return $scope.model.style.showGrid;
			}else{
				return false;
			}
		}

		$scope.canSeeSummary = function(){

			if($scope.model!=undefined){
				return $scope.model.style.showSummary;
			}else{
				return false;
			}
		}
		
		
	}
//	this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("table",{'initialDimension':{'width':20, 'height':20}});

})();