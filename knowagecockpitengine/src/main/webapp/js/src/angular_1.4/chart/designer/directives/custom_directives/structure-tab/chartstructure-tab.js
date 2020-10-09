/**
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

/**
 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
 */

var app = angular.module('chartstructure-tab', []);

app.directive('chartstructureTab', function(sbiModule_config,chartDesignerBasePath) {
		return {
			restrict: 'AE',
			replace: true,
			templateUrl: function(){
			      return chartDesignerBasePath + '/directives/custom_directives/structure-tab/chartstructure-tab.html'
		      },
			controller: structureTabControllerFunction
		}
	});

function structureTabControllerFunction($scope,sbiModule_translate,sbiModule_restServices, sbiModule_messaging, sbiModule_util, StructureTabService,ChartDesignerData){
	$scope.translate = sbiModule_translate;
	$scope.structureDetailsShown = false;
	$scope.chartLibNamesConfig = chartLibNamesConfig;
	$scope.categoriesContainer = [];
	$scope.categories = [];
	$scope.seriesContainers = [];
	$scope.seriesNumber = 0;
	$scope.checkCategoriesLength = 0;

	$scope.categ = {};
	$scope.categ.lengthh = $scope.checkCategoriesLength;

	$scope.$watch('categories',function(newValue,oldValue){
		$scope.categ.lengthh = $scope.categories.length;
	},true)

	$scope.$watch('chartTemplate.dateTime',function(newValue,oldValue){
		if($scope.chartTemplate && newValue!=oldValue){
			if(newValue==false) $scope.chartTemplate.CHART ? $scope.chartTemplate.CHART.categoryDate = "" : $scope.chartTemplate.categoryDate = "" ;
			else $scope.chartTemplate.CHART ? $scope.chartTemplate.CHART.categoryDate = $scope.categories[0].column : $scope.chartTemplate.categoryDate = $scope.categories[0].column;;
		}

	},true)

	$scope.$watch('categories[0].orderColumn',function(newValue, oldValue){

		if(oldValue && newValue != oldValue && $scope.categories[0].orderColumn != $scope.categories[0].name){
			sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.cockpit.widget.warning.message"), 'Warning');

		}

	})

	$scope.isInvalid = function (series) {

		if($scope.minMaxSeries.max){
			if(series.length>=$scope.minMaxSeries.min &&  series.length <= $scope.minMaxSeries.max){
				$scope.structureForm.$setValidity("seriesNumber", true);
				return false;
			}  else {
				$scope.structureForm.$setValidity("seriesNumber", false);
				return true;
			}
		}
		else {
			if(series.length>=$scope.minMaxSeries.min ){
				$scope.structureForm.$setValidity("seriesNumber", true);
				return false;
			}  else{
				$scope.structureForm.$setValidity("seriesNumber", false);
				return true;
			}
		}

	}

	$scope.numberOfSeriesContainers = 0;
	$scope.maxNumberOfSeriesContainers = 4;
	$scope.seriesContainersAliases = [];

	$scope.detailsForSeriesItem = {color: ""};

	// Indicator whether we should show the message that the maximum number of Series containers is exceeded
	$scope.showMaxNmbSerAxesExceeded = false;

	/**
	 * Show/hide the Structure Details panel.
	 */

	$scope.showStructureDetails = function(detailsForOption, item) {

		if(detailsForOption == 'categoriesOrdering' && $scope.categories.length == 0){
			sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.categorypanel.mincategnumber.showcategcolumnpopup.warningmessage"), 'Warning');
			$scope.structureDetailsShown = false;
			return;
		}
		$scope.structureTabDetailsName = StructureTabService.getStructureTabDetailsName(detailsForOption);
		$scope.structureTabDetailsTemplateURL = StructureTabService.getSeriesItemsConfDetailsTemplateURL(detailsForOption);
		$scope.structureDetailsShown = true;

		$scope.axisForDisplay = null;

		for (i=0; i<$scope.chartTemplate.AXES_LIST.AXIS.length; i++) {
			if ($scope.chartTemplate.AXES_LIST.AXIS[i].alias == item) {
				$scope.axisForDisplay = $scope.chartTemplate.AXES_LIST.AXIS[i];

				return;
			}
		}

	}


	$scope.hideStructureDetails = function() {
		$scope.structureDetailsShown = false;
	}

	$scope.checkAxis = function() {
		if($scope.chartTemplate.AXES_LIST.AXIS.constructor === Object){
			var temp = $scope.chartTemplate.AXES_LIST.AXIS;
			$scope.chartTemplate.AXES_LIST.AXIS = [];
			$scope.chartTemplate.AXES_LIST.AXIS.push(temp);
		}
		$scope.allAxis = $scope.chartTemplate.AXES_LIST.AXIS;
		for (var i = 0; i < $scope.allAxis.length; i++) {
			if($scope.allAxis[i].type == 'Category'){
			    $scope.categoriesAxis = $scope.allAxis[i];
			}
		}

	}

	$scope.deletePlotband = function(index) {
		$scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT.splice(index,1);
	}
	$scope.addNewPlotband = function() {
		var newPlot = {};
		if($scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS==""){
			$scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS = {};
			$scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT=[];
		}
		if($scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT.length == 0){
			newPlot = {
					"from": $scope.chartTemplate.AXES_LIST.AXIS[0].min,
					"to": $scope.chartTemplate.AXES_LIST.AXIS[0].max,
					"color": ""
				}
		}else{
			newPlot = {
					"from": $scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT[$scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT.length-1].to,
					"to": $scope.chartTemplate.AXES_LIST.AXIS[0].max,
					"color": ""
				}
		}

		$scope.chartTemplate.AXES_LIST.AXIS[0].PLOTBANDS.PLOT.push(newPlot);
	}

	$scope.disableUsingAttributeIfRealtimeDSisUsed = function (attribute){
		if(!$scope.isRealTimeDataset){
			return false;
		} else {
			if(attribute.id=="id") {
				return false
			} else {
				return true;
			}
		}
	}
	 // Called when the user clicks on the attribute in its container, so the attribute can be used as a category in the chart.

	/*$scope.moveAttributeToTooltip = function (item) {

		var index = findInArray($scope.categories,'column',item.alias);
		var indexTooltip = findInArray($scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.categories,'column',item.alias);

		if (index<0) {
			$scope.categories.push({column:item.alias,groupby:"", groupbyNames:"",name:item.alias,orderColumn:"",orderType:"",tooltip:true});
		}

		if (indexTooltip<0){
			$scope.chartTemplate.VALUES.SERIE[0].TOOLTIP.categories.push({column:item.alias,groupby:"", groupbyNames:"",name:item.alias,orderColumn:"",orderType:"",tooltip:true});
		}
		//sesic

	}*/

	 $scope.moveAttributeToCategories = function(item) {

			var chartType = $scope.chartTemplate.type;
			var index = sbiModule_util.findInArray($scope.categories,'column',item.alias);

			if (chartType.toUpperCase() == "SCATTER" || chartType.toUpperCase() == "WORDCLOUD") {
				if(chartType.toUpperCase() == "SCATTER" && item.colType=="String"){
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.structure.scatter.only.strings"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
				} else {
					if($scope.categories.length>=1){
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.categories"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
					} else {
						if(index<0){
							  $scope.categories.push({column:item.alias,groupby:"", groupbyNames:"",name:item.alias,orderColumn:"",orderType:"",fakeCategory:false});
						}
					}
				}

			} else if (chartType.toUpperCase() == "PARALLEL") {
				if($scope.categories.length>=2){
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.categories"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
				} else {
					if(index<0){

						  $scope.categories.push({column:item.alias,groupby:"", groupbyNames:"",name:item.alias,orderColumn:"",orderType:"",});
					  }
				}
			} else if(chartType.toUpperCase() == "CHORD" || chartType.toUpperCase() == "HEATMAP") {
				if($scope.categories.length>=2){
					sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.categories"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
				} else {
					if(index<0){
						if($scope.categories.length==1){
							$scope.categories.push({column:item.alias,groupby:item.alias, groupbyNames:item.alias,name:item.alias,orderColumn:"",orderType:"",});
							$scope.categories[0].groupby = item.alias;
							$scope.categories[0].groupbyNames = item.alias;
						} else {
							$scope.categories.push({column:item.alias,groupby:item.alias, groupbyNames:item.alias,name:item.alias,orderColumn:"",orderType:"",});
						}
					 }
				}
			} else if(chartType.toUpperCase() == "TREEMAP" || chartType.toUpperCase() == "SUNBURST" ||
						chartType.toUpperCase() == "BAR" || chartType.toUpperCase() == "BUBBLE" || chartType.toUpperCase() == "LINE"  ||
							chartType.toUpperCase() == "PIE" ||
							chartType.toUpperCase() == "RADAR" ) {
				if(index<0){
					  $scope.categories.push({column:item.alias,groupby:"", groupbyNames:"",name:item.alias,orderColumn:"",orderType:"",});
				  }
			}
	  }

	 var checkIt =function(type,array){

		switch (type) {
		case 'bar':
		case 'gauge':
		case 'heatmap':
		case 'line':
		case 'radar':
		case 'scatter':
		case 'sunburst':
		case 'bubble':

		return true;
			break;
		case 'parallel':
			if(array.length < 2){

				$scope.seriesLimit = false;
			}else{
				$scope.seriesLimit = true;
			}
			break;
		case 'pie':
			if(array.length >4){
				$scope.seriesLimit = false;
			}else{
				$scope.seriesLimit = true;
			}
			break;
		case 'wordcloud':
		case 'treemap':
		case 'chord':
			if(array.length > 1){
				$scope.seriesLimit = false;
			}else{
				$scope.seriesLimit = true;
			}
			break
		default:
			break;
		}

		}

	$scope.getAggregationFunction = function(serieName) {
		for (i=0; i<$scope.chartTemplate.VALUES.SERIE.length; i++) {
			if ($scope.chartTemplate.VALUES.SERIE[i].column == serieName) {
				return $scope.chartTemplate.VALUES.SERIE[i].groupingFunction;
			}
		}
	}

	// Called when the user clicks on the measure in its container, so the measure can be used as a series item in the chart.
	$scope.moveMeasureToSeries = function(item,seriesContainer) {

		// If we send an information about the particular Series container (happens when there are more more than 1 container)
		if (seriesContainer) {
			for (i=0; i<$scope.seriesContainers.length; i++) {
				if ($scope.seriesContainers[i].name == seriesContainer.name) {
					//checkIt($scope.selectedChartType,$scope.seriesContainers);

					if($scope.selectedChartType.toLowerCase() == 'bubble' && (seriesContainer.name=='X' || seriesContainer.name=='Z')){
						if($scope.seriesContainers[i].series.length==1){
							sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.series.axis"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
							break;
						}
					}
					if ($scope.seriesContainers[i].series.indexOf(item.alias)<0) {
						$scope.seriesContainers[i].series.push(item.alias);

					}else{
						console.log("duplicate");
					}
				}
			}
		}
		// If we want to move measure into the only series container that we have
		else {
			//checkIt($scope.selectedChartType,$scope.seriesContainers);
			if ($scope.seriesContainers[0].series.indexOf(item.alias)<0)  {
				$scope.seriesContainers[0].series.push(item.alias);
			}else if($scope.seriesLimit == false){
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.series"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
			}else{
				sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.series.duplicate"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
			}
		}
		for (var i = 0; i < $scope.seriesContainers.length; i++) {
			console.log($scope.seriesContainers[i]);
			for (var j = 0; j < $scope.seriesContainers[i].series.length; j++) {
				if($scope.seriesContainers[i].series[j] == item.alias){
					var base = "";
					switch ($scope.selectedChartType) {
					case 'parallel':
						base = StructureTabService.getParallelTemplate();
						break;
					case 'sunburst':
						base = StructureTabService.getSunburstTemplate();
						break;
					case 'scatter':
						base = StructureTabService.getScatterTemplate();
						break;
					case 'treemap':
						base = StructureTabService.getTreemapTemplate();
						break;
					case 'wordcloud':
						base = StructureTabService.getWordCloudTemplate();
						break;
					case 'gauge':
						base = StructureTabService.getGaugeTemplate();
						break;
					case 'line':
						base = StructureTabService.getBaseTemplate($scope.selectedChartType);
						break;
					case 'heatmap':
						base = StructureTabService.getHeatmapTemplate();
						break;
					case 'radar':
						base = StructureTabService.getRadarTemplate();
						break;
					case 'bar':
						base = StructureTabService.getBaseTemplate($scope.selectedChartType);
						break;
					case 'bubble':
						base = StructureTabService.getBubbleTemplate();
						break;
					case 'pie':
						base = StructureTabService.getBaseTemplate($scope.selectedChartType);
						break;
					case 'chord':
						base = StructureTabService.getChordTemplate();
						break;
					default:
						break;
					}

					var temp = base.VALUES.SERIE[0];
					temp.axis = $scope.seriesContainers[i].name;
					temp.column = item.alias;
					temp.name = item.alias;
					temp.precision = 0;
					temp.groupingFunction = item.aggregationSelected;
					if($scope.chartTemplate.type.toUpperCase()=="SCATTER" || $scope.chartTemplate.type.toUpperCase()=="BUBBLE"){
						temp.fakeSerie = false;
					}
					var checkForSameAxis = sbiModule_util.findInArray($scope.chartTemplate.VALUES.SERIE,'axis',temp.axis)
					var checkForSameColumn = sbiModule_util.findInArray($scope.chartTemplate.VALUES.SERIE,'column',temp.column);
					if( checkForSameAxis == -1 || checkForSameColumn == -1){
						$scope.chartTemplate.VALUES.SERIE.push(temp);
					}else if($scope.seriesLimit == false){
						sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.max.series"), sbiModule_translate.load("sbi.data.editor.association.AssociationEditor.warning"));
					}else{
						console.log("duplicate");
					}

				}
			}

		}
	}

	$scope.prepareSeriesContainersAliases = function() {

		// If the chart is already defined (it is NOT the new one - not yet persisted)

		var editingMode = $scope.chartTemplate;
		if (editingMode!=undefined) {

			var chartType = $scope.selectedChartType.toLowerCase();
			var typesWithMultipleSeriesContainersEnabled = $scope.seriesContainerAddAndRemoveIncludeTypes;
			var allChartAxes = $scope.chartTemplate.AXES_LIST.AXIS;

			// If the chart type is not GAUGE (in other words); if there is only one axis (only Series container)
			if (allChartAxes.length) {
				for (i=0; i<allChartAxes.length; i++) {
					if (allChartAxes[i].type.toLowerCase()=="serie") {
						$scope.seriesContainers.push({"name":allChartAxes[i].alias,"series":[]});
						$scope.numberOfSeriesContainers++;
					}
				}
			}
			else {
				$scope.numberOfSeriesContainers++;
				$scope.seriesContainers.push({"name":allChartAxes.alias,"series":[]});
			}

		}
		else {
			$scope.numberOfSeriesContainers++;
			$scope.seriesContainers.push({"name":"Y","series":[]});
			//console.log($scope.seriesContainers);
		}

	}

	$scope.checkSeriesForContainers = function() {
		$scope.seriesContainers.length=0
		$scope.prepareSeriesContainersAliases();
		var editingMode = editingMode = $scope.chartTemplate;

		if (editingMode !=undefined) {

			var allSeries = $scope.chartTemplate.VALUES.SERIE;

			for (i=0; i<$scope.seriesContainers.length; i++) {

				if (allSeries.length) {
					for (j=0; j<allSeries.length; j++) {

						if ($scope.seriesContainers[i].name==allSeries[j].axis  && !allSeries[j].fakeSerie) {
							if(allSeries[j].column!=""){
								$scope.seriesContainers[i].series.push(allSeries[j].column);
							}

						}

					}
				}
				else {
					if(allSeries.column!="" && allSeries.column!=undefined){
						$scope.seriesContainers[i].series.push(allSeries.column);
					}
				}

			}
			if($scope.chartTemplate.type.toUpperCase() == 'SCATTER'  || $scope.chartTemplate.type.toUpperCase()=="BUBBLE"){
				for (j=0; j<allSeries.length; j++) {
					if(allSeries[j].fakeSerie){
						$scope.indexSerie = j
						break;
					}
				}
				allSeries.splice($scope.indexSerie, allSeries.length - $scope.indexSerie)
			}
		}

//		console.log($scope.seriesContainers);

	}

	// When user clicks on the button for editing the Series item configuration
	$scope.prepareSeriesItemConfiguration = function(item,axis) {

		$scope.detailsForSeriesItem = null;
		$scope.detailsForSeriesTooltip = null;

		var allSeries = $scope.chartTemplate.VALUES.SERIE;
		if( allSeries.constructor === Object) {
			$scope.detailsForSeriesItem = allSeries;
			$scope.detailsForSeriesTooltip = allSeries.TOOLTIP;
			return;
		} else {
			for (i=0; i<allSeries.length; i++) {
				if (allSeries[i].column == item && allSeries[i].axis == axis) {
					$scope.detailsForSeriesItem = allSeries[i];
					$scope.detailsForSeriesTooltip = allSeries[i].TOOLTIP;
					return;
				}
			}
		}


	}
	/**
	 * Arrange categories that are available for the chart (set inside the chart template of the already existing chart)
	 * so that they populate their container (Category container).
	 */
	$scope.checkCategories = function() {

		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {

			$scope.categoriesExist = $scope.chartTemplate.VALUES.CATEGORY ? true : false;

			if ($scope.categoriesExist) {

				var categoryTag = $scope.chartTemplate.VALUES.CATEGORY;
//				console.log(categoryTag.length);
				// If the CATEGORY tag contains an array (e.g. this goes for the SUNBURST chart type)
				if (categoryTag.length) {
					if($scope.chartTemplate.type == 'SCATTER'){
						for (j=0; j<categoryTag.length; j++) {

							if(categoryTag[j].fakeCategory){
								$scope.indexCateg = j
								break;

							}

						}
						categoryTag.splice($scope.indexCateg, categoryTag.length - $scope.indexCateg)
					}

					//if($scope.chartTemplate.type=="PARALLEL" || $scope.chartTemplate.type=="HEATMAP" || $scope.chartTemplate.type=="CHORD") {
						for (i=0; i<categoryTag.length; i++) {
							$scope.categories.push(categoryTag[i]);
						}


				}
				// If all (if there are more than one) categories are under the single tag (column and groupby properties (attributes))
				else {

					//$scope.categoriesContainer.push(categoryTag.column);
					console.log(categoryTag)

					//groupby is array
					if (categoryTag.groupby.indexOf(",") > -1) {

						//and groupbyNames is an array
						if (categoryTag.groupbyNames.indexOf(",") > -1) {

							$scope.categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,});

							var groupBySplitArray = categoryTag.groupby.split(", ");
							for (i=0; i<groupBySplitArray.length; i++) {

								var obj = {column:"", groupby:"", groupbyNames:"", name:"", orderColumn:"", orderType:"", };
								obj.column = groupBySplitArray[i];
								var groupByNameSplitArray = categoryTag.groupbyNames.split(", ");
								for (var j = 0; j < groupByNameSplitArray.length; j++) {
									if(j==i){
										obj.name = groupByNameSplitArray[j];
									}
								}

								 $scope.categories.push(obj);
							}


						}

						//and groupbyNames is not an array
						else {

							$scope.categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:"",orderType:"",});

							var gbnCounter = 0;
							var groupBySplitArray = categoryTag.groupby.split(", ");
							for (i=0; i<groupBySplitArray.length; i++) {
								var obj = {column:"", groupby:"", groupbyNames:"", name:"", orderColumn:"", orderType:"", };
								//check if grpupByName is empty and case for first situation
								 if(categoryTag.groupbyNames!="" && gbnCounter==0) {
									 obj.column = groupBySplitArray[i];
									 obj.name = categoryTag.groupbyNames;
									 gbnCounter++;
								 } else {
									 obj.column = groupBySplitArray[i];
									 obj.name = "";
								 }
								 $scope.categories.push(obj);
							}
						}

					}
					//groupby no comma
					else {

						//categoryTag.groupby is empty
						if(categoryTag.groupby=="" && categoryTag.column!=""){
							$scope.categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,});
						} else {

							 if(categoryTag.name=="" && categoryTag.column!=""){
								 $scope.categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:"",orderType:"",});
								 } else if(categoryTag.name!="" && categoryTag.column!="") {
									 $scope.categories.push({column:categoryTag.column,groupby:"", groupbyNames:"",name:categoryTag.name, orderColumn:categoryTag.orderColumn,orderType:categoryTag.orderType,});
								 }

							 if(categoryTag.groupbyNames!="") {
								 $scope.categories.push({column:categoryTag.groupby,groupby:"", groupbyNames:"",name:categoryTag.groupbyNames, orderColumn:"",orderType:"",});
							 } else if (categoryTag.column!="") {
								 $scope.categories.push({column:categoryTag.groupby,groupby:"", groupbyNames:"",name:categoryTag.groupbyNames, orderColumn:"",orderType:"",});
							 }
						}
					}

					if( ($scope.chartTemplate.type == 'BAR' || $scope.chartTemplate.type == 'LINE') && !$scope.chartTemplate.VALUES.CATEGORY.drillOrder){
						$scope.chartTemplate.VALUES.CATEGORY.drillOrder={}

						for (var j = 0; j <  $scope.categories.length; j++) {

							$scope.chartTemplate.VALUES.CATEGORY.drillOrder[$scope.categories[j].column] = {orderColumn:$scope.categories[j].orderColumn, orderType:$scope.categories[j].orderType};

						}

					}
				}
			}
		}
		else {
			$scope.categoriesExist = true;
		}
	 var cflag = 0;
	 for (var i = $scope.fieldsMetadata.length-1; i>=0; i--) {
		if($scope.fieldsMetadata[i].fieldType == 'ATTRIBUTE'){
			for (var j = 0; j < $scope.categories.length; j++) {
				if($scope.categories[j].column == $scope.fieldsMetadata[i].alias){
					cflag++;

				}

			}
		}

	 }
	 if($scope.categories.length>0 && cflag == 0){
		 sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.dschange.categories"), 'Warning');
	 }
	}

	/**
	 * Arrange series that are available for the chart (set inside the chart template of the already existing chart)
	 * so that they populate their container(s) (Series container(s)).
	 */
	$scope.checkSeries = function() {

		$scope.checkSeriesForContainers();

		// If the chart is already defined (it is NOT the new one - not yet persisted)
		if ($scope.chartTemplate) {
			var series = [];
			   var chartSeries = [];
			   // Series from chart template (from its JSON)
			   if($scope.chartTemplate.VALUES.SERIE.constructor===Object){
			    var temp = $scope.chartTemplate.VALUES.SERIE;
			    chartSeries.push(temp);
			    $scope.chartTemplate.VALUES.SERIE = [];
			    $scope.chartTemplate.VALUES.SERIE.push(temp)

			   }
			   else {
			    chartSeries= $scope.chartTemplate.VALUES.SERIE;
			   }
			if(chartSeries[0] && chartSeries[0].column==""){
				$scope.chartTemplate.VALUES.SERIE = [];
			}
				if(chartSeries.length>0){
					for (i=0; i<chartSeries.length; i++) {
						if(chartSeries[i].column!="" && chartSeries[i].column!=undefined){
							$scope.seriesContainer.push(chartSeries[i].column);
						}
					}
				}

				var sflag = 0;
				 for (var i = $scope.fieldsMetadata.length-1; i>=0; i--) {
					if($scope.fieldsMetadata[i].fieldType == 'MEASURE'){
						for (var j = 0; j < $scope.chartTemplate.VALUES.SERIE.length; j++) {
							if($scope.chartTemplate.VALUES.SERIE[j].column == $scope.fieldsMetadata[i].alias){
								sflag++;

							}

						}
					}

				 }
				 if($scope.chartTemplate.VALUES.SERIE.length> 0 && sflag == 0){
					 sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.chartengine.designer.dschange.series"), 'Warning');
				 }
		}
		

	}

	/**
	 * Operations for categories inside the Category container: move up, move down and delete item.
	 */

	$scope.categoryMoveUp = function(item) {
		var index = $scope.categories.indexOf(item);
		var nextIndex = index-1;
		var temp = $scope.categories[index];
		$scope.categories[index] = $scope.categories[nextIndex];
		$scope.categories[nextIndex] = temp;
	}

	$scope.categoryMoveDown = function(item) {
		var index = $scope.categories.indexOf(item);
		var nextIndex = index+1;
		var temp = $scope.categories[index];
		$scope.categories[index] = $scope.categories[nextIndex];
		$scope.categories[nextIndex] = temp;
	}

	$scope.categoryRemove = function(indexOfItem) {
		if($scope.chartTemplate.VALUES.CATEGORY.drillOrder)
		delete $scope.chartTemplate.VALUES.CATEGORY.drillOrder[$scope.categories[indexOfItem].column];
		$scope.categories.splice(indexOfItem,1);
	}

	$scope.categoryRemoveAll = function() {
		$scope.categories = [];
	}

	$scope.seriesItemMoveUp = function(item,seriesContainer) {
		console.log("moving up");

		var index = sbiModule_util.findInArray($scope.chartTemplate.VALUES.SERIE,'column',item);
		var nextIndex = index-1;
		var temp = $scope.chartTemplate.VALUES.SERIE[index];
		$scope.chartTemplate.VALUES.SERIE[index] = $scope.chartTemplate.VALUES.SERIE[nextIndex];
		$scope.chartTemplate.VALUES.SERIE[nextIndex] = temp;

		console.log($scope.chartTemplate.VALUES.SERIE);


		for (i=0; i<$scope.seriesContainers.length; i++) {
			if ($scope.seriesContainers[i].name == seriesContainer.name) {
				var index = $scope.seriesContainers[i].series.indexOf(item);
				var nextIndex = index-1;
				var temp = $scope.seriesContainers[i].series[index];
				$scope.seriesContainers[i].series[index] = $scope.seriesContainers[i].series[nextIndex];
				$scope.seriesContainers[i].series[nextIndex] = temp;
				return;
			}
		}
	}


	$scope.seriesItemMoveDown = function(item,seriesContainer) {
		console.log("moving down");

		var index = sbiModule_util.findInArray($scope.chartTemplate.VALUES.SERIE,'column',item);
		var nextIndex = index+1;
		var temp = $scope.chartTemplate.VALUES.SERIE[index];
		$scope.chartTemplate.VALUES.SERIE[index] = $scope.chartTemplate.VALUES.SERIE[nextIndex];
		$scope.chartTemplate.VALUES.SERIE[nextIndex] = temp;

		console.log($scope.chartTemplate.VALUES.SERIE);

		for (i=0; i<$scope.seriesContainers.length; i++) {
			if ($scope.seriesContainers[i].name == seriesContainer.name) {
				var index = $scope.seriesContainers[i].series.indexOf(item);
				var nextIndex = index+1;
				var temp = $scope.seriesContainers[i].series[index];
				$scope.seriesContainers[i].series[index] = $scope.seriesContainers[i].series[nextIndex];
				$scope.seriesContainers[i].series[nextIndex] = temp;
				return;
			}
		}
	}


	$scope.seriesItemRemove = function(seriesItem,seriesContainerName) {

		if($scope.chartTemplate.groupedSerie!=undefined && $scope.chartTemplate.groupedSerie == seriesItem){
			delete $scope.chartTemplate.groupedSerie;
		}

		if($scope.chartTemplate.VALUES.SERIE.length != undefined && $scope.chartTemplate.VALUES.SERIE.constructor == Array){
			for (var i = 0; i < $scope.chartTemplate.VALUES.SERIE.length; i++) {
				if($scope.chartTemplate.VALUES.SERIE[i].column == seriesItem && $scope.chartTemplate.VALUES.SERIE[i].axis == seriesContainerName){
					$scope.chartTemplate.VALUES.SERIE.splice(i,1);
				}
			}
		} else {
			$scope.chartTemplate.VALUES.SERIE.column = "";
			$scope.chartTemplate.VALUES.SERIE.name = "";
		}


		console.log($scope.chartTemplate.VALUES.SERIE);
		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {

			if ($scope.seriesContainers[i].name == seriesContainerName) {

				// Go through all it's series items in order to eliminate the one that is aimed to be removed.
				for (j=0; j<$scope.seriesContainers[i].series.length; j++) {

					if ($scope.seriesContainers[i].series[j] == seriesItem) {
						$scope.seriesContainers[i].series.splice(j,1);
						return;
					}

				}

			}

		}

	}

	$scope.seriesRemoveAll = function(seriesContainer) {

		var seriesForDeletion = angular.copy(seriesContainer);
		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {
			if ($scope.seriesContainers[i].name == seriesContainer.name) {
				$scope.seriesContainers[i].series = [];
				break;
			}
		}

		for (var i = 0; i < $scope.chartTemplate.VALUES.SERIE.length; i++) {
			for (var j = 0; j < seriesForDeletion.series.length; j++) {

				if($scope.chartTemplate.VALUES.SERIE[i].name == seriesForDeletion.series[j]
				&& $scope.chartTemplate.VALUES.SERIE[i].axis == seriesForDeletion.name
				){
					$scope.chartTemplate.VALUES.SERIE.splice(i,1);
				}

			}

		}

	}

	$scope.addSeriesContainer = function() {

		// Number of Series containers
		var nmbOfSerConts = $scope.seriesContainers.length;

		if (nmbOfSerConts < $scope.maxNumberOfSeriesContainers) {

			var lastSerContName = $scope.seriesContainers[nmbOfSerConts-1].name;
			var newSerContName = "Axis_";	// The prefix of the name of the new Series container that will be added to Designer

			if (lastSerContName!="Y" && lastSerContName!='X' && lastSerContName!='Z') {
				var splitLastSerContName = lastSerContName.split("_");
				newSerContName += (Number(splitLastSerContName[1])+1);
			}
			else {
				newSerContName += "1";
			}

			$scope.seriesContainers.push({name:newSerContName, series:[]});
			var newAxis = {
					"alias": newSerContName,
					"id": newSerContName,
					"type": "Serie",
					"position": "right",
					"min":0,
					"max":0,
					"style":{
		            	   "rotate":"",
		            	   "align":"",
		            	   "color":"",
		            	   "fontFamily":"",
		            	   "fontSize":""
		               },
		               "MAJORGRID":{
		                  "interval":"",
		                  "style":{
		                	  "typeline":"",
		                	  "color":""
		                  }
		               },
		               "MINORGRID":{
		            	  "interval":"",
		                  "style":{
		                	  "typeline":"",
		                	  "color":""
		                  }
		               },
		               "TITLE":{
			                  "text":"",
			                  "style":{
			                	  "align":"",
			                	  "color":"",
			                	  "fontFamily":"",
			                	  "fontWeight":"",
			                	  "fontSize":""
			                  }
			               }
				}


			$scope.chartTemplate.AXES_LIST.AXIS.push(newAxis);
			// If we reach the maximum number of Series containers and we still click on plus to add a new one, set the indicator that should show the info.
			$scope.showMaxNmbSerAxesExceeded = $scope.seriesContainers.length==$scope.maxNumberOfSeriesContainers ? true : false;

			$scope.numberOfSeriesContainers++;

		}

	}

	$scope.clearAxisMajorGridDetails = function () {
		$scope.axisForDisplay.MAJORGRID = {"interval": "","style": {"typeline": "","color": ""}}
	}

	$scope.clearAxisMinorGridDetails = function () {
		$scope.axisForDisplay.MINORGRID = {"interval": "","style": {"typeline": "","color": ""}}
	}

	$scope.removeSeriesContainer = function(seriesContainer) {

		var seriesForDeletion = angular.copy(seriesContainer);

		// Go through all Series containers in order to focus on the one from which we want to remove a series item.
		for (i=0; i<$scope.seriesContainers.length; i++) {
			if ($scope.seriesContainers[i].name == seriesContainer.name) {
				$scope.seriesContainers.splice(i,1);
				$scope.showMaxNmbSerAxesExceeded = false;
				$scope.numberOfSeriesContainers--;
				break;
			}
		}
		for (var i = 0; i < $scope.chartTemplate.AXES_LIST.AXIS.length; i++) {
			if($scope.chartTemplate.AXES_LIST.AXIS[i].alias == seriesContainer.name){
				$scope.chartTemplate.AXES_LIST.AXIS.splice(i,1);
			}
		}

		for (var i = 0; i < $scope.chartTemplate.VALUES.SERIE.length; i++) {
			for (var j = 0; j < seriesForDeletion.series.length; j++) {

				if($scope.chartTemplate.VALUES.SERIE[i].name == seriesForDeletion.series[j]
				&& $scope.chartTemplate.VALUES.SERIE[i].axis == seriesForDeletion.name
				){
					$scope.chartTemplate.VALUES.SERIE.splice(i,1);
				}

			}

		}
	}

	// Function that enables the drop-down menu functionality
	$scope.openMenu = function($mdOpenMenu, ev) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
    };


 // Function that enables the drop-down menu functionality
	$scope.openMenuCategory = function($mdOpenMenu, ev, categoryName) {
	      originatorEv = ev;
	      $mdOpenMenu(ev);
    };

    /**
     * Manage visibility (show/hide) of the options for the Series container.
     */

    // TODO: we need also to take care if the series container is the main (for plus)/additional (for X)
    $scope.seriesContainerAddAndRemoveIncludeTypes = ["bar","line","bubble"];

    $scope.seriesContainerConfigDropDownExcludeTypes = ["sunburst","treemap","wordcloud"];

    $scope.seriesAxisTitleExcludeTypes = ["chord","pie","parallel"];
    $scope.seriesAxisMajorMinorGirdExcludeTypes = ["chord","pie","gauge","heatmap","parallel"];

    /**
     * Manage visibility (show/hide) of the options for the Series container.
     */

    // TODO: Check if these chart types are the only one that for which we should exclude the Ordering column option in the categories drop-down menu
    $scope.categoriesContainerConfigDropDownExcludeTypes = ["wordcloud","treemap","parallel"];

    $scope.categoriesOrderColumnExcludeTypes = ["parallel","chord","bubble"];
    $scope.seriesOrderColumnExcludeTypes = ['chord', 'heatmap', 'treemap', 'parallel', 'sunburst', 'wordcloud', 'bubble'];
    $scope.categoriesConfigExcludeTypes = ["pie","sunburst", "bubble"];
    $scope.categoriesTitleConfigExcludeTypes = ["parallel","pie","sunburst","chord", "bubble"];
    $scope.categoriesDateTimeIncludedTypes = ["bar","line","radar","scatter"];

    $scope.seriesItemTypes = StructureTabService.getSeriesItemTypes();
    $scope.seriesItemOrderingTypes = StructureTabService.getSeriesItemOrderingTypes();
    $scope.listDateFormats = StructureTabService.getListOfDateFormats();
    $scope.scaleFactorsFixed = StructureTabService.getScaleFactorsFixed();

    $scope.textAlignment = ChartDesignerData.getAlignTypeOptions();
    $scope.fontFamily = ChartDesignerData.getFontFamilyOptions();
    $scope.fontStyle = ChartDesignerData.getFontStyleOptions();
    $scope.fontSize = ChartDesignerData.getFontSizeOptions();
    $scope.lineTypeOptions = StructureTabService.getLineTypesOptions();
    $scope.gaugeTicksPositionOptions = StructureTabService.getGaugeTicksPosition();
    $scope.gaugeSubtypes = StructureTabService.getGaugeSybtypes();

    $scope.seriesItemAggregationTypes = StructureTabService.getSeriesItemAggregationTypes();

    /*
	@author: Radmila Selakovic (rselakov, radmila.selakovic@mht.net)
	function that filter list of agregation options
	"NONE" wll be present only if type of chart is SCATTER
	*/
	$scope.filterAgregations = function(item) {
		if($scope.chartTemplate.type!="SCATTER"){
			return item.name != 'NONE';
		}
		else{
			return item.name ;
		}
	}


	$scope.removeProperties = function(){
		if ($scope.chartTemplate.groupSeriesCateg==false){
			delete $scope.chartTemplate.coloredCategory;
			delete $scope.chartTemplate.groupedSerie;
		}
	}



	$scope.getMetadata = function(){
		$scope.allMeasures.length = 0;
		$scope.allAttributes.length = 0;
		$scope.fieldsMetadata = $scope.localMod.columnSelectedOfDatasetAggregations;
		for(var i = 0; i < $scope.fieldsMetadata.length; i++) {
			if($scope.fieldsMetadata[i].fieldType=="MEASURE") {
				$scope.allMeasures.push($scope.fieldsMetadata[i]);
			} else {
				$scope.allAttributes.push($scope.fieldsMetadata[i]);
			}
		}

		$scope.seriesContainers.length=0;

		$scope.checkSeries();
		$scope.checkAxis();
	}
	$scope.getMetadata();
	$scope.checkCategories();

	$scope.getFilteredSeries = function(){

		var filteredSeries = [];
		for(var i = 0; i < $scope.chartTemplate.VALUES.SERIE.length; i++) {

			var indexSeriesContainer_X = sbiModule_util.findInArray($scope.seriesContainers,'name',"X");
			var indexSeriesContainer_Z = sbiModule_util.findInArray($scope.seriesContainers,'name',"Z");

			if(indexSeriesContainer_X>-1){
				var indexSeries_X = $scope.seriesContainers[indexSeriesContainer_X].series.indexOf($scope.chartTemplate.VALUES.SERIE[i].column);
			}
			if(indexSeriesContainer_Z>-1){
				var indexSeries_Z = $scope.seriesContainers[indexSeriesContainer_Z].series.indexOf($scope.chartTemplate.VALUES.SERIE[i].column);
			}

			if(indexSeries_X==-1 && indexSeries_Z==-1) filteredSeries.push($scope.chartTemplate.VALUES.SERIE[i])
		}
		return filteredSeries
	}

}