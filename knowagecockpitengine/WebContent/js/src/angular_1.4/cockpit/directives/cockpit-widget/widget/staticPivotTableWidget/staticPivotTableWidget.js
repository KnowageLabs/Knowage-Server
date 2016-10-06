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
.directive('cockpitStaticPivotTableWidget',function(cockpitModule_widgetServices){
	   return{
		   templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableWidgetTemplate.html',
		   controller: cockpitStaticPivotTableWidgetControllerFunction,
		   compile: function (tElement, tAttrs, transclude) {
                return {
                    pre: function preLink(scope, element, attrs, ctrl, transclud) {
                    	element[0].classList.add("flex");
                    	element[0].classList.add("layout");
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

function cockpitStaticPivotTableWidgetControllerFunction($scope,cockpitModule_widgetConfigurator,$q,$mdPanel,sbiModule_restServices,$compile,cockpitModule_generalOptions,$mdDialog){
	
	$scope.init=function(element,width,height){
		$scope.refreshWidget();
	};
	$scope.refresh=function(element,width,height, datasetRecords,nature){
		if(datasetRecords==undefined){
			return;
		}
		
		if(angular.equals(nature,'resize') || angular.equals(nature,'gridster-resized')){
			return;
		}
		
		var dataToSend={
				 config: {
				        type: "pivot"
				    },
				metadata: datasetRecords.metaData,
				jsonData: datasetRecords.rows,
				sortOptions:{}
		};
		
		angular.merge(dataToSend,$scope.ngModel.content);
		
		if( dataToSend.crosstabDefinition==undefined || 
			dataToSend.crosstabDefinition.measures==undefined||dataToSend.crosstabDefinition.measures.length==0 ||
			dataToSend.crosstabDefinition.rows==undefined||dataToSend.crosstabDefinition.rows.length==0 ||
			dataToSend.crosstabDefinition.columns==undefined||dataToSend.crosstabDefinition.columns.length==0 ){
			console.log("crossTab non configured")
			return;
		}
		
		sbiModule_restServices.promisePost("1.0/crosstab","update",dataToSend).then(
				function(response){
					$scope.subCockpitWidget.html(response.data.htmlTable);
					$compile(angular.element($scope.subCockpitWidget).contents())($scope)
					$scope.addPivotTableStyle();
				},
				function(response){
					sbiModule_restServices.errorHandler(response.data,"Pivot Table Error")
					}
				)
	}
	
	$scope.clickFunction=function(columnName,columnValue){
		 
		$scope.doSelection(columnName,columnValue);
	}

	
	$scope.addPivotTableStyle=function(){
		if($scope.ngModel.content.style!=undefined){
			var totalsItem;
			var subtotalsItem;
			var dataItem;
			var memberItem;
			var crossItem;
			//generic
			if($scope.ngModel.content.style.generic!=undefined && Object.keys($scope.ngModel.content.style.generic).length>0 ){
				totalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".totals"));
				subtotalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".partialsum"));
				dataItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".data"));
				memberItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".member"));
				crossItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".crosstab-header-text"));
				for(var prop in $scope.ngModel.content.style.generic){
					totalsItem.css(prop,$scope.ngModel.content.style.generic[prop]);
					subtotalsItem.css(prop,$scope.ngModel.content.style.generic[prop]);
					dataItem.css(prop,$scope.ngModel.content.style.generic[prop]);
					memberItem.css(prop,$scope.ngModel.content.style.generic[prop]);
					crossItem.css(prop,$scope.ngModel.content.style.generic[prop]);
				}
			}
			
			//altrnateRow
			
			if($scope.ngModel.content.style.measuresRow!=undefined && Object.keys($scope.ngModel.content.style.measuresRow).length>0 ){
				var rowList=angular.element($scope.subCockpitWidget[0].querySelectorAll("tr"));
				var tmpOddRow=false;
				angular.forEach(rowList,function(row,index){
					var dataColumnList=row.querySelectorAll(".data");
					if(dataColumnList.length>0){
						if(tmpOddRow){
							angular.element(dataColumnList).css("background-color",$scope.ngModel.content.style.measuresRow["odd-background-color"])
						}else{
							angular.element(dataColumnList).css("background-color",$scope.ngModel.content.style.measuresRow["even-background-color"])
						}
						tmpOddRow=!tmpOddRow;
					}else{
						tmpOddRow=false;
					}
				});
			}
			
			//totals
			if($scope.ngModel.content.style.totals!=undefined && Object.keys($scope.ngModel.content.style.totals).length>0 ){
				if(totalsItem==undefined){
					totalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".totals"));
				}
				for(var prop in $scope.ngModel.content.style.totals){
					totalsItem.css(prop,$scope.ngModel.content.style.totals[prop])
				}
			}
			//subTotals
			if($scope.ngModel.content.style.subTotals!=undefined && Object.keys($scope.ngModel.content.style.subTotals).length>0 ){
				if(subtotalsItem==undefined){
					subtotalsItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".partialsum"));
				}
				for(var prop in $scope.ngModel.content.style.subTotals){
					subtotalsItem.css(prop,$scope.ngModel.content.style.subTotals[prop])
				}
			}
			
			//measures
			if($scope.ngModel.content.style.measures!=undefined && Object.keys($scope.ngModel.content.style.measures).length>0 ){
				if(dataItem==undefined){
					dataItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".data"));
				}
				for(var prop in $scope.ngModel.content.style.measures){
					dataItem.css(prop,$scope.ngModel.content.style.measures[prop])
				}
			}
			
			//measuresHeaders
			if($scope.ngModel.content.style.measuresHeaders!=undefined && Object.keys($scope.ngModel.content.style.measuresHeaders).length>0 ){
				if(memberItem==undefined){
					memberItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".member"));
				}
				for(var prop in $scope.ngModel.content.style.measuresHeaders){
					memberItem.css(prop,$scope.ngModel.content.style.measuresHeaders[prop])
				}
			}
			//crossTabHeaders
			if($scope.ngModel.content.style.crossTabHeaders!=undefined && Object.keys($scope.ngModel.content.style.crossTabHeaders).length>0 ){
				if(crossItem==undefined){
					crossItem=angular.element($scope.subCockpitWidget[0].querySelectorAll(".crosstab-header-text"));
				}
				for(var prop in $scope.ngModel.content.style.crossTabHeaders){
					if(angular.equals("background-color",prop)){
						crossItem.parent().parent().parent().parent().css(prop,$scope.ngModel.content.style.crossTabHeaders[prop])
					}else{
						crossItem.css(prop,$scope.ngModel.content.style.crossTabHeaders[prop])
					}
				}
			}
				
		}
		
	};
	
	$scope.editWidget=function(index){
		
		var finishEdit=$q.defer();
		var config = {
				attachTo:  angular.element(document.body),
				controller: function($scope,finishEdit,sbiModule_translate,model,mdPanelRef,cockpitModule_datasetServices,cockpitModule_generalOptions,$mdDialog,$mdToast){
			    	  $scope.translate=sbiModule_translate;
			    	  $scope.localModel={};
			    	  $scope.currentDataset={};
			    	  $scope.originalCurrentDataset={};
			    	  $scope.dragUtils={dragObjectType:undefined};
			    	  $scope.colorPickerProperty={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb'}
			    	  $scope.cockpitModule_generalOptions=cockpitModule_generalOptions;
			    	  angular.copy(model,$scope.localModel);
			    	  
			    	  if($scope.localModel.content==undefined){
		    			  $scope.localModel.content={};
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition==undefined){
		    			  $scope.localModel.content.crosstabDefinition={};
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition.measures==undefined){
		    			  $scope.localModel.content.crosstabDefinition.measures=[];
		    		  }
			    	  if($scope.localModel.content.crosstabDefinition.rows==undefined){
			    		  $scope.localModel.content.crosstabDefinition.rows=[];
			    	  }
			    	  if($scope.localModel.content.crosstabDefinition.columns==undefined){
			    		  $scope.localModel.content.crosstabDefinition.columns=[];
			    	  }
			    	  
			    	  $scope.changeDatasetFunction=function(dsId,noReset){
			    		  $scope.currentDataset= cockpitModule_datasetServices.getDatasetById( dsId);
			    		  $scope.originalCurrentDataset=angular.copy( $scope.currentDataset);
			    		  if(noReset!=true){
			    			  $scope.localModel.content.crosstabDefinition.measures=[];
			    			  $scope.localModel.content.crosstabDefinition.rows=[];
			    			  $scope.localModel.content.crosstabDefinition.columns=[];
			    		  }
			    	  }
			    	  
			    	  if($scope.localModel.dataset!=undefined && $scope.localModel.dataset.dsId!=undefined){
			    		  $scope.changeDatasetFunction($scope.localModel.dataset.dsId,true)
			    	  }
			    	  
			    	  //remove used measure and attribute
			    	 $scope.clearUsedMeasureAndAttribute=function(){
			    		 if($scope.currentDataset.metadata==undefined){
			    			 return;
			    		 }
			    		 
			    		 var arrObje=["measures","rows","columns"];
			    		 var present=[];
			    		 for(var meas=0;meas<arrObje.length;meas++){
			    			 for(var i=0;i<$scope.localModel.content.crosstabDefinition[arrObje[meas]].length;i++){
			    				 present.push($scope.localModel.content.crosstabDefinition[arrObje[meas]][i].id);
			    			 }
			    		 }
			    		 
			    		 for(var i=0;i<$scope.currentDataset.metadata.fieldsMeta.length;i++){
			    			 if(present.indexOf($scope.currentDataset.metadata.fieldsMeta[i].name)!=-1){
			    				 $scope.currentDataset.metadata.fieldsMeta.splice(i,1);
			    				 i--;
			    			 }
			    		 }
			    		 
			    	 }
			    	 $scope.clearUsedMeasureAndAttribute();
			    	 
			    	 $scope.dropCallback=function(event, index, list,item, external, type, containerType){

			    		  if(angular.equals(type,containerType)){
			    			  var eleIndex=-1;
			    			  angular.forEach(list,function(ele,ind){
			    				  var tmp=angular.copy(ele);
			    				  delete tmp.$$hashKey;
			    				  if(angular.equals(tmp,item)){
			    					  eleIndex=ind;
			    				  }
			    			  });
			    			  
			    			  list.splice(eleIndex,1)
			    			  list.splice(index,0,item)
			    			  return false
			    		  }else{
			    			  var tmpItem;
			    			  if(angular.equals(containerType,"MEASURE-PT") || angular.equals(containerType,"COLUMNS") || angular.equals(containerType,"ROWS")){
			    				  
			    				  if( (angular.equals(containerType,"COLUMNS") &&  angular.equals(type,"ROWS")) || (angular.equals(containerType,"ROWS") &&  angular.equals(type,"COLUMNS"))){
			    					  tmpItem=item;
			    				  }else{
			    					  //convert item in specific format 
			    					   tmpItem={
			    							  id: item.name,
			    							  alias: item.alias,
			    							  iconCls: item.fieldType.toLowerCase(),
			    							  nature: item.fieldType.toLowerCase(),
			    							  values: "[]",
			    							  sortable: false,
			    							  width: 0
			    					  };
			    					   if(angular.equals(containerType,"MEASURE-PT")){
			    						   tmpItem.funct="SUM";
			    					   }
			    							   
			    				  }
			    				  
			    				  
			    			  }else{
			    				  //containerType == MEASURE or ATTRIBUTE
			    				  //load element from dataset field
			    				  for(var i=0;i<$scope.originalCurrentDataset.metadata.fieldsMeta.length;i++){
			    					  if(angular.equals($scope.originalCurrentDataset.metadata.fieldsMeta[i].name,item.id)){
			    						  tmpItem=angular.copy($scope.originalCurrentDataset.metadata.fieldsMeta[i]);
			    						  break;
			    					  }
			    				  }
			    			  }
			    		  
			    		  
			    			  list.splice(index,0,tmpItem)
			    			  return true;
			    		  }
			    		  
			    	  }
			    	  
			     
			    	  $scope.saveConfiguration=function(){
			    		  if($scope.localModel.dataset == undefined){
			  				$scope.showAction($scope.translate.load('sbi.cockpit.table.missingdataset'));
			    			return;
			    		  }
			    		  if($scope.localModel.content.crosstabDefinition.measures.length == 0 ||
			    			$scope.localModel.content.crosstabDefinition.rows.length == 0 ||
			    			$scope.localModel.content.crosstabDefinition.columns.length ==0
			    		  ){
			    			  $scope.showAction($scope.translate.load('sbi.cockpit.widgets.staticpivot.missingfield'));
			    			  return;
			    		  }
			    		  angular.copy($scope.localModel,model);
			    		  mdPanelRef.close();
			    		  $scope.$destroy();
			    		  finishEdit.resolve();

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
			    		  $scope.$destroy();
			    		  finishEdit.reject();

			    	  }
			    	  
			    	  $scope.editFieldsProperty=function(item){
			    		  $mdDialog.show({
			    		      controller: function($scope,sbiModule_translate,item){
			    		    	  $scope.translate=sbiModule_translate;
			    		    	  $scope.isMeasure=angular.equals(item.nature,"measure");
			    		    	  $scope.currentItem=angular.copy(item);
						    	  $scope.AggregationFunctions= cockpitModule_generalOptions.aggregationFunctions;


			    		    	  $scope.saveConfiguration=function(){
						    		  angular.copy($scope.currentItem,item);
						    		 $mdDialog.hide();
						    	  }
						    	  $scope.cancelConfiguration=function(){
						    		  $mdDialog.cancel();
						    	  }
						    	  
						    	  
			    		      },
			    		      templateUrl:baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableWidgetEditFieldsPropertyTemplate.html',
			    		      locals:{item:item},
			    		      hasBackdrop: true,
			  				  clickOutsideToClose: false,
			  				  escapeToClose: false,
			    		      fullscreen: true
			    		    })
			    	  }
			      },
				disableParentScroll: true,
				templateUrl: baseScriptPath+ '/directives/cockpit-widget/widget/staticPivotTableWidget/templates/staticPivotTableWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen :true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: {finishEdit:finishEdit,model:$scope.ngModel},
				onRemoving :function(){
					$scope.refreshWidget();
				}
		};

		$mdPanel.open(config);
		return finishEdit.promise;
		
	}
	
	$scope.orderPivotTable=function(column, axis, globalId){
		if($scope.ngModel.content.sortOptions==undefined){
			$scope.ngModel.content.sortOptions={};
		}
		var axisConfig;
		if(axis==1){
			if($scope.ngModel.content.sortOptions.columnsSortKeys==undefined){
				$scope.ngModel.content.sortOptions.columnsSortKeys={}
			}
			axisConfig = $scope.ngModel.content.sortOptions.columnsSortKeys;
		}else{
			if($scope.ngModel.content.sortOptions.rowsSortKeys==undefined){
				$scope.ngModel.content.sortOptions.rowsSortKeys={}
			}
			 
			axisConfig = $scope.ngModel.content.sortOptions.rowsSortKeys;
		}

		var direction = axisConfig[column];
		if(!direction){
			direction = 1;
		}
		direction = direction*(-1);

		axisConfig[column] = direction;
		
		$scope.refreshWidget();
	}
	
};


//this function register the widget in the cockpitModule_widgetConfigurator factory
addWidgetFunctionality("static-pivot-table",{'initialDimension':{'width':20, 'height':20}});

})();