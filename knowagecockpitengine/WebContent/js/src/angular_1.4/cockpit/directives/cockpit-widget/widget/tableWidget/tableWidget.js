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
	})
	.directive('mdProgressLinearCustom', function() {
		return {
			restrict: 'E',
			scope: {
				color: '@',
				value: '@'
				
			},
			template: '<div class="md-container" style="background-color:grey">' +
		      '<div class="md-dashed"></div>' +
		     // '<div class="md-bar md-bar1" ng-style="linearStyle"></div>' +
		      '<div class="md-bar md-bar1" ng-style="linearStyle"></div>'+
		      '</div>',
			link: function(scope) {
				scope.perc=scope.value+'%';
				scope.bgColor=scope.color;
				if(scope.perc>100){ scope.perc=100;}
				else if(scope.perc<0){ scope.perc=0;}
				scope.linearStyle={ 'background-color': scope.bgColor , 'width': scope.perc, 'height':'5px'};
				
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
		if($scope.ngModel.multiselectable==undefined){
			$scope.ngModel.multiselectable=false;
		}
		
		
		if($scope.ngModel.style==undefined){
			$scope.ngModel.style={};
		};
		
		if($scope.ngModel.cross==undefined){
			$scope.ngModel.cross={};
		};
		
		$scope.getKeyByValue = function( obj,value ) {
		    for( var prop in obj ) {
		        if( obj.hasOwnProperty( prop ) ) {
		             if( obj[ prop ] === value )
		                 return prop;
		        }
		    }
		}
		
		
		$scope.tableFunction={
				widgetStyle:$scope.ngModel.style,			
		}

		$scope.getGridStyle=function(row,column,index){
			var ind = $scope.indexInList(column.label, $scope.ngModel.content.columnSelectedOfDataset);
			var gridStyle = {};
			//style summary row - returning the column style, if there are summary styles defined they override the default
			if($scope.ngModel.style.showSummary == true && index == $scope.datasetRecods.rows.length-1){
				var summaryStyle  = angular.merge({},$scope.ngModel.content.columnSelectedOfDataset[ind].style, $scope.ngModel.style.summary);
				return summaryStyle;
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
			if(ind!=-1 && $scope.ngModel.content.columnSelectedOfDataset[ind].style != undefined){
				gridStyle = angular.extend({},gridStyle,$scope.ngModel.content.columnSelectedOfDataset[ind].style);
			}
			//davverna - overriding this function behaviour, index -99 returns a single item instead of all the style object
			if(index==-99 && $scope.ngModel.content.columnSelectedOfDataset[ind].style != undefined && $scope.ngModel.content.columnSelectedOfDataset[ind].style.maxChars != undefined){
				parentGridStyle = angular.extend({},gridStyle,$scope.ngModel.content.columnSelectedOfDataset[ind].style);
				gridStyle = parentGridStyle.maxChars;
			}
			//davverna - if the max chars value setting for the column is not set the default is false
			if(index==-99 && ($scope.ngModel.content.columnSelectedOfDataset[ind].style == undefined || $scope.ngModel.content.columnSelectedOfDataset[ind].style.maxChars == undefined)){
				gridStyle = false;
			}
			return gridStyle;
		}
		
		$scope.selectRow=function(row,column,listId,index,evt,columnName){
			for(var i=0;i<$scope.ngModel.content.columnSelectedOfDataset.length;i++){
				if($scope.ngModel.content.modalselectioncolumn!=undefined)
				{
					if($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow==$scope.ngModel.content.modalselectioncolumn)
					{
						if(Object.prototype.toString.call( row ) === '[object Array]'){
							var newValue=row[0][$scope.ngModel.content.modalselectioncolumn];
						}else{
							var newValue=row[$scope.ngModel.content.modalselectioncolumn];
						}
					}
				}	
				
				if(angular.equals($scope.ngModel.content.columnSelectedOfDataset[i].aliasToShow,columnName)){
					if($scope.ngModel.content.columnSelectedOfDataset[i].fieldType=="MEASURE"
							|| $scope.ngModel.content.columnSelectedOfDataset[i].isCalculated){
						return;
					}	
				}
			}
			

			$scope.doSelection(columnName,column,$scope.ngModel.content.modalselectioncolumn,newValue,row);

			
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
		
		$scope.changeDocPage = function(searchValue, itemsPerPage, currentPageNumber, columnsSearch, columnOrdering, reverseOrdering){
			if($scope.gridsterItem.hasClass("gridster-item-resizing")
					|| !$scope.widgetIsInit
					|| itemsPerPage == 0){
				return;
			}
			if($scope.ngModel
					&& $scope.ngModel.content
					&& $scope.ngModel.content.currentPageNumber + 1 == currentPageNumber
					&& $scope.datasetRecords
					&& $scope.datasetRecords.rows
					&& $scope.datasetRecords.rows.length == itemsPerPage
					&& $scope.columnOrdering == columnOrdering
					&& $scope.reverseOrdering == reverseOrdering){
				return;
			}
			
			var time=(new Date()).getTime();
			$scope.lastChangePageConf=time;
			$timeout(function(){
				if(angular.equals(time,	$scope.lastChangePageConf)){
					
					if(searchValue==undefined || searchValue.trim().lenght==0 ){
						searchValue='';
					}
					
					var numberOfElement = angular.copy(itemsPerPage);					
					if($scope.ngModel.content.maxRowsNumber != undefined){
						numberOfElement = angular.copy($scope.ngModel.content.maxRowsNumber)
					}
					
					if($scope.ngModel.style.showSummary == true){
						numberOfElement--;
					}
					
					currentPageNumber--;
					$scope.ngModel.content.currentPageNumber = currentPageNumber;
					
					$scope.columnOrdering = columnOrdering;
					$scope.reverseOrdering = reverseOrdering;
					
					var options = {
						page: currentPageNumber,
						itemPerPage: numberOfElement,
						columnOrdering: columnOrdering,
						reverseOrdering: reverseOrdering,
						type: $scope.ngModel.type
					};
					$scope.refreshWidget(options);
				}
			},1000);
			 
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
			if(obj!=undefined && (obj.hiddenColumn == true || ($scope.isMobile.any()!=null && obj.hideonMobile == true))){
				return false;
			}
			return true;
		}
		
		
		
		$scope.freeValueFromPrefixAndSuffix=function(value,currentColumn){
			if(currentColumn.hasOwnProperty("style") && value!=undefined){
				if(currentColumn.style.hasOwnProperty("suffix")){
					value=value.replace(" "+currentColumn.style.suffix, "");
				}	
				if(currentColumn.style.hasOwnProperty("prefix")){
					value=value.replace(currentColumn.style.prefix+" ", "");
				}	
			}
			return parseFloat(value);
		}
		
		
		
		$scope.refresh=function(element,width,height, datasetRecords,nature){
			if(nature == 'gridster-resized' || nature == 'fullExpand'){
				return;
			}
			$scope.columnsToShow = [];
			$scope.datasetRecords = {};
			$scope.columnToshowinIndex = [];
			$scope.tableFunction.widgetStyle=$scope.ngModel.style;
			$scope.datasetRecods = datasetRecords;
			var calculateScaleValue=function(minVal, maxVal, val)
			{
				if(maxVal!=minVal)
				{	
					return ((val-minVal)/(maxVal-minVal))*100;
				}
				else
				{
					return 0;
				}
			}
			
			$scope.rgbToHex=function(rgbColor)
			{
				var a = rgbColor.split("(")[1].split(")")[0];
				a = a.split(",");
				var b = a.map(function(x){             //For each array element
				    x = parseInt(x).toString(16);      //Convert to a base16 string
				    return (x.length==1) ? "0"+x : x;  //Add zero if we get only one character
				})
				b = "0x"+b.join("");
				return b;
			}

			
			
			
			
			
			if($scope.ngModel.content.columnSelectedOfDataset!=undefined){
				$scope.datasetRecords  =datasetRecords;
				for(var i=0;i<$scope.ngModel.content.columnSelectedOfDataset.length;i++)
				{
					var obj = {};
					
					obj.label= $scope.ngModel.content.columnSelectedOfDataset[i]['aliasToShow'];
					obj.name = $scope.ngModel.content.columnSelectedOfDataset[i]['aliasToShow'];
					if(typeof($scope.ngModel.content.columnSelectedOfDataset[i].style) != "undefined" && $scope.ngModel.content.columnSelectedOfDataset[i].style['size']){
						obj.size = $scope.ngModel.content.columnSelectedOfDataset[i].style['size'];
					}
					

					
					if(angular.equals($scope.ngModel.content.columnSelectedOfDataset[i].fieldType,"MEASURE")){						
						this.test=i;
						obj.transformer=function(value,currentRow,columnName)
						{
								obj;
								var currentColumn;
								for(var j=0;j<$scope.ngModel.content.columnSelectedOfDataset.length;j++)
								{
									var column = $scope.ngModel.content.columnSelectedOfDataset[j];
									if(column.name==columnName || column.alias==columnName || column.aliasToShow==columnName)
									{						
										currentColumn=angular.copy(column);
										break;
									}	
								}	
								
								var prefix="";
								var suffix="";
								var formattedValue=$scope.formatValue(value, currentColumn);
								var horiz_align=$scope.getCellAlignment(currentColumn)?$scope.getCellAlignment(currentColumn):"center";
			
								
								var valueWithoutPrefixAndSuffix=$scope.freeValueFromPrefixAndSuffix(value,currentColumn);		
								if(!currentColumn.hasOwnProperty("visType") || currentColumn.visType=='Text')
								{										
									return "<span>"+formattedValue+"</span>"	
								}else{
								
								var htm="<div layout='row' layout-align='" + horiz_align + " center'>"; //default

								//find the highest index with != none condition 
								if(currentColumn.hasOwnProperty('colorThresholdOptions'))
								{
									for(var i=0; i<currentColumn.colorThresholdOptions.condition.length;i++) // display only the first alert found (condition respected)
									{	
										if(currentColumn.colorThresholdOptions.condition[i]!="none")
										{
											
											if(currentColumn.colorThresholdOptions.condition[i]=="<" && value<currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											else if(currentColumn.colorThresholdOptions.condition[i]==">" && value>currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											else if(currentColumn.colorThresholdOptions.condition[i]=="=" && value==currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											else if(currentColumn.colorThresholdOptions.condition[i]==">=" && value>=currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											else if(currentColumn.colorThresholdOptions.condition[i]=="<=" && value<=currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											else if(currentColumn.colorThresholdOptions.condition[i]=="!=" && value!=currentColumn.colorThresholdOptions.conditionValue[i])
											{
												htm="<div layout='row' style='background-color:"+currentColumn.colorThresholdOptions.color[i] +"' layout-align='" + horiz_align + " center'>";
												break;
											}
											
											
											
										}	

									}						
								}	
								
								if(!currentColumn.hasOwnProperty("visType") || currentColumn.visType=='Icon only')
								{										
									htm=htm+"<div>&nbsp;</div>"	;
								}	
								
												
								
								
								if(currentColumn.hasOwnProperty("minValue") && currentColumn.hasOwnProperty("maxValue")) // 	MinValue and MaxValue are present only if you have to display a chart
								{	
	
									var minValue=currentColumn.minValue;
									var maxValue=currentColumn.maxValue;
									
									barValue=calculateScaleValue(minValue,maxValue,valueWithoutPrefixAndSuffix);
									
									if(currentColumn.visType=='Chart')
									{	


										htm=htm+" <div>&nbsp;</div><md-progress-linear-custom flex  style='padding:0 8px 0 8px; width:"+currentColumn.chartLength +"px' value="+barValue+" color=\""+  currentColumn.chartColor +"\"> </md-progress-linear-custom>"
									}	
									else if(currentColumn.visType=='Text & Chart')
									{



											htm=htm+"<div class='inlineChartText' flex>"+formattedValue+"</div> &nbsp;  <md-progress-linear-custom flex  style='padding-right:8px;width:"+currentColumn.chartLength +"px' value="+barValue+" color=\""+  currentColumn.chartColor +"\"> </md-progress-linear-custom>"

	
									}	 
									else if(currentColumn.visType=='Chart & Text')
									{

											htm=htm+"<md-progress-linear-custom flex  style='padding-left:8px;width:"+currentColumn.chartLength +"px' value="+barValue+" color=\""+  currentColumn.chartColor +"\"> </md-progress-linear-custom> &nbsp; <div class='inlineChartText' flex>"+formattedValue+"</div>"
											

									}							

								}
								if(currentColumn.scopeFunc && currentColumn.scopeFunc.condition){
									var alreadyPutIcon=false;
									for(var i=0; i<currentColumn.scopeFunc.condition.length;i++) // display only the first alert found (condition respected)
									{
										var lastIter=false;
										if(i==currentColumn.scopeFunc.condition.length-1){lastIter=true;}
										
										var colInfo=currentColumn.scopeFunc.condition[i];
										if(colInfo.condition!="none")
										{
											if(colInfo.condition=='<')
											{	
												if(!alreadyPutIcon)
												{	
													if(parseFloat(value)<colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;
													}
													else if(lastIter)
													{
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>"; //blank icon
													}	
												}	
											}
											else if(colInfo.condition=='>')
											{	
												if(!alreadyPutIcon)
												{	

													if(parseFloat(value)>colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;

													}
													else if(lastIter)
													{
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>";
													}	
												}	
											}
											else if(colInfo.condition=='=')
											{
												if(!alreadyPutIcon)
												{	

													if(parseFloat(value)==colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;

													}
													else if(lastIter)
													{
														//htm=htm+"&nbsp; <div style='height:\"24px\"; width:\"24px\";'> </div>";
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>";
													}	
												}	
											}
											else if(colInfo.condition=='>=')
											{	
												if(!alreadyPutIcon)
												{	

													if(parseFloat(value)>=colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;

													}
													else if(lastIter)
													{
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>";
													}	
												}	
											}
											else if(colInfo.condition=='<=')
											{	
												if(!alreadyPutIcon)
												{	

													if(parseFloat(value)<=colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;

													}
													else if(lastIter)
													{
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>";
													}	
												}	
											}
											else if(colInfo.condition=='!=')
											{
												if(!alreadyPutIcon)
												{	

													if(parseFloat(value)!=colInfo.value)
													{	
														htm=htm+"&nbsp; <md-icon  style='color:"+ currentColumn.scopeFunc.condition[i].iconColor +"'  md-font-icon='"+currentColumn.scopeFunc.condition[i].icon+"'> </md-icon>";
														alreadyPutIcon=true;

													}
													else if(lastIter)
													{
														//htm=htm+"&nbsp; <div style='height:\"24px\"; width:\"24px\";'> </div>";
														htm=htm+"&nbsp; <md-icon md-font-icon='fa fa-fw'></md-icon>";
													}	
												}	
											}
	
												 
	
											//break;
										}
									}	
								}	
								
								return htm+"</div>";									
								}
						}
					}else{
						obj.static=true;
					}
					
					if($scope.ngModel.content.columnSelectedOfDataset[i].isCalculated){
						obj.customRecordsClass="noClickCursor";
					}
					obj.style = $scope.getGridStyle;
					if($scope.canSeeColumnByMobile($scope.ngModel.content.columnSelectedOfDataset[i].style)){
						$scope.columnsToShow.push(obj);
					}
					if($scope.datasetRecords != undefined){
						for(var j=1;j<$scope.datasetRecords.metaData.fields.length;j++){
							var header = $scope.datasetRecords.metaData.fields[j].header;
							if(header == $scope.ngModel.content.columnSelectedOfDataset[i]['aliasToShow']
									|| header == $scope.ngModel.content.columnSelectedOfDataset[i]['alias']){
								$scope.columnToshowinIndex.push($scope.datasetRecords.metaData.fields[j].dataIndex);
								break;
							}
						}
					}

				}
				$scope.itemList=$scope.getRows($scope.columnToshowinIndex,$scope.datasetRecords);
				$scope.tableColumns=$scope.columnsToShow;
				for(var i=0;i<$scope.tableColumns.length;i++)
				{
					$scope.tableColumns[i].hideTooltip=false;
				}
				
				if(datasetRecords !=undefined){
					$scope.totalCount = datasetRecords.results;
				}

			}
		}
		
		$scope.formatValue = function (value, column){			
		
			var output = value;
			var precision = 2;  //for default has 2 decimals 
			if (column.style && column.style.precision >= 0) precision =  column.style.precision;
			if (column.style && column.style.format){
		    	switch (column.style.format) {
		    	case "#.###":
		    		output = $scope.numberFormat(value, 0, ',', '.'); 
		    	break;            	
		    	case "#,###":
		    		output = $scope.numberFormat(value, 0, '.', ',');
		    	break;
		    	case "#.###,##":
		    		output = $scope.numberFormat(value, precision, ',', '.'); 
		    	break;
		    	case "#,###.##":
		    		output = $scope.numberFormat(value, precision, '.', ',');
		    		break;
		    	default:		    		
		    		break;
		    	} 
			}
		
	    	return output;
		}
		
		$scope.numberFormat = function (value, dec, dsep, tsep) {
    		
  		  if (isNaN(value) || value == null) return value;
  		 
  		  value = parseFloat(value).toFixed(~~dec);
  		  tsep = typeof tsep == 'string' ? tsep : ',';

  		  var parts = value.split('.'), fnums = parts[0],
  		    decimals = parts[1] ? (dsep || '.') + parts[1] : '';

  		    
  		  return fnums.replace(/(\d)(?=(?:\d{3})+$)/g, '$1' + tsep) + decimals;
		}

		//returns the horizontal cell alignment
		$scope.getCellAlignment = function (column){
			var align = "";
			if (column.style && column.style.textAlign){
				switch (column.style.textAlign)
	            {
	               case 'left': align="start";
	               break;
	            
	               case 'right': align="end";
	               break;
	            
	               case 'center': align="center";
	               break;						           
	            
	               default:  align="start";
	            }
			}
			
			return align;
		}
		
		$scope.getRows = function(indexList, values){
			var table = [];
			if($scope.columnToshowinIndex.length >0 && values != undefined){
				for(var i=0;i<values.rows.length;i++){
					var obj = {};
					for(var j=0;j<indexList.length;j++){
						for(var k=1;k<values.metaData.fields.length;k++){
							if(indexList[j] == values.metaData.fields[k].dataIndex ){
								var key=$scope.getKeyByValue(indexList,indexList[j]);
								var style = $scope.ngModel.content.columnSelectedOfDataset[key].style;
								//var style = $scope.ngModel.content.columnSelectedOfDataset[k-1].style;
								//var prefixedField=$scope.ngModel.content.columnSelectedOfDataset[k-1].name;//ADDED
								obj[values.metaData.fields[k].header] = values.rows[i][indexList[j]];

								
								if(style!=undefined && style.precision != undefined){
									
									// define eclosure
									Math.round = (function() {
										var originalRound = Math.round;
										return function(number, precision) {
											precision = Math.abs(parseInt(precision)) || 0;
											var multiplier = Math.pow(10, precision);
											return (originalRound(number * multiplier) / multiplier);
										};
									})();

									var header = obj[values.metaData.fields[k].header];
									var float = parseFloat(header);
									var toDo = Math.round(float, style.precision); 
									obj[values.metaData.fields[k].header] = toDo;
								}
								if(style!=undefined && style.prefix !=undefined){
									obj[values.metaData.fields[k].header] = style.prefix + ' ' + obj[values.metaData.fields[k].header];
								}
								if(style!=undefined && style.suffix !=undefined){
									obj[values.metaData.fields[k].header] = obj[values.metaData.fields[k].header] + ' ' + style.suffix;
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
		$scope.tableColumns=$scope.ngModel.content.columnSelectedOfDataset.filter(function(column){
			if(column.style && column.style.hiddenColumn){
				return false;
			}
			return true;
		});
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
				
				obj["page"] = $scope.ngModel.content.currentPageNumber ? $scope.ngModel.content.currentPageNumber : 0;
				obj["itemPerPage"] = $scope.ngModel.content.maxRowsNumber;
				if($scope.ngModel.style.showSummary == true){
					obj["itemPerPage"]--;
				}
				if($scope.columnOrdering){
					obj["columnOrdering"] = $scope.columnOrdering;
				}
				if($scope.reverseOrdering){
					obj["reverseOrdering"] = $scope.reverseOrdering;
				}
				obj["type"] = $scope.ngModel.type;
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
		$scope.fontFamily = ["Inherit","Roboto","Arial","Times New Roman","Tahoma","Verdana","Impact","Calibri","Cambria","Georgia","Gungsuh"],
		$scope.fontWeight = ['normal','bold','bolder','lighter','initial','inherit'];
		$scope.textAlign = ['left','right','center'];
		$scope.getMetadata = getMetadata;
		$scope.model = {};
		angular.copy(originalModel,$scope.model);
		console.log("originalModel:", originalModel);
		
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
			if(!$scope.checkAliases()){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.erroraliases'));
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
		$scope.checkAliases = function(){
			var columns = $scope.model.content.columnSelectedOfDataset;
			for(var i = 0; i < columns.length - 1; i++){
				for(var j = i + 1; j < columns.length; j++){
					if(columns[i].aliasToShow == columns[j].aliasToShow){
						return false;
					}
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
		
		$scope.$watchCollection('model.content.columnSelectedOfDataset', function(newColumns, oldColumns) {
			var disableShowSummary = true;
			if(newColumns){
				for(var i=0; i<newColumns.length; i++){
					if(newColumns[i].fieldType == "MEASURE"){
						disableShowSummary = false;
						break;
					}
				}
			}
			$scope.model.style.disableShowSummary = disableShowSummary;
			if(disableShowSummary){
				$scope.model.style.showSummary = false;
			}
		});
	}
//	this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("table",{'initialDimension':{'width':20, 'height':20},'updateble':true,'cliccable':true});

})();