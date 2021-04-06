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


angular.module('chartInitializer')

.service('highcharts',['highchartsDrilldownHelper','jsonChartTemplate','chartConfMergeService','cockpitModule_datasetServices', 'cockpitModule_highchartsLocales',function(highchartsDrilldownHelper,jsonChartTemplate,chartConfMergeService,cockpitModule_datasetServices,cockpitModule_highchartsLocales){

	this.chart = null;
	Highcharts.setOptions(cockpitModule_highchartsLocales);
	var chartConfConf = null;
	this.changeDatasetColumns = function(chartConf, data){
		for (var attrname in chartConf) {
			if(!(typeof chartConf[attrname] == 'object')){
				if(typeof chartConf[attrname] =='string')
					chartConf[attrname]  =  chartConf[attrname].replace(/(\$F\{)([a-zA-Z0-9\-\_\s]*)(\})/g,function(match,p1,p2){
						var column = "";
						for (var j = 1; j < data.metaData.fields.length; j++) {
							if(data.metaData.fields[j].header.startsWith(p2)){
								column = data.metaData.fields[j].name
							}
						}
						return data.rows[0][column];
					})
			} else {
				this.changeDatasetColumns(chartConf[attrname],data);
			}
		}
	}

	this.renderChart = function(renderObj, jsonData){
		if(jsonData ){
			if(jsonData.jsonData) {
				var data = JSON.parse(jsonData.jsonData);
			} else if(jsonData.rows){
				var data = jsonData;
			}
			if(data && data.rows && data.rows.length>0){
				this.changeDatasetColumns(renderObj.chartConf,data);
			}

		}
		var chartConf = renderObj.chartConf;
		if(chartConf.chart.additionalData && chartConf.chart.additionalData.dateTime && chartConf.chart.additionalData.datetype!="string"){
			for (var i = 0; i < chartConf.series.length; i++) {
				var seria = chartConf.series[i];
				for (var j = 0; j < seria.data.length; j++) {
					var dat = seria.data[j];
					if(dat.datetype!="simpledate"){
						var dateSplit = dat.name.replace('/', ":").replace('/', ":").replace(' ', ":").replace('.', ":").split(":");
						dat.x = (new Date(dateSplit[2], dateSplit[1]-1, dateSplit[0], dateSplit[3], dateSplit[4], dateSplit[5], dateSplit[6])).getTime();
					} else {
						var dateSplit = dat.name.replace('/', ":").replace('/', ":").split(":");
						dat.x = (new Date(dateSplit[2], dateSplit[1]-1, dateSplit[0])).getTime();
					}
				}
			}
		}

		var element = renderObj.element;
		var handleCockpitSelection = renderObj.handleCockpitSelection;
		var exportWebApp = renderObj.exportWebApp;
		var widgetData = renderObj.widgetData;
		var selectionsAndParams = renderObj.selectionsAndParams;

		chartConfConf = chartConf;
		if(renderObj.chartTemplate.accessibility && renderObj.chartTemplate.accessibility.sonification){
			chartConf.accessibility = renderObj.chartTemplate.accessibility;
			for (var i = 0; i < chartConf.series.length; i++) {
				chartConf.series[i].id = i+1;
			}
			chartConf.tooltip = {enabled:false}
		}
		if(!exportWebApp) {
			adjustChartSize(element,chartConf);
		}
		var chartType = chartConf.chart.type.toLowerCase();

		if (chartType == 'treemap')
		{
			delete this.updateData;
			if(exportWebApp) {
				return  renderTreemap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,exportWebApp );
			} else {
				this.chart = renderTreemap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,false,renderObj.chartTemplate.advanced,chartConfMergeService);
				this.chart.drillable = chartConf.chart.drillable
			}
		}
		else if (chartType == 'heatmap')
		{
			delete this.updateData;
			if(exportWebApp) {
				return renderHeatmap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,exportWebApp );
			}
			this.chart = renderHeatmap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,false,renderObj.chartTemplate.advanced,chartConfMergeService);
		} else if (chartType == 'sunburst') {
			delete this.updateData;
			if(exportWebApp) {
				return renderHCSunburst(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,exportWebApp );
			}
			this.chart = renderHCSunburst(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,false,renderObj.chartTemplate.advanced,chartConfMergeService);
		}
		else
		{
			if (chartType == 'scatter') delete this.updateData;

			if(chartType == 'solidgauge' && chartConf.chart.subtype == 'solid') {

				var seriesOrder = chartConf.series;
				function sortYFunction (){
					seriesOrder.sort(function(a,b){
						return b[0].data[0].y - a[0].data[0].y;
					})
				}
			}
			var isBasic = false;
			var plotLines = null;
			var plotBands = null;
			var infoFroDrill = []
			if ((chartType == 'column' || chartType == 'bar' || chartType == 'line' || chartType == 'bubble') && !chartConf.chart.polar && (chartConf.plotOptions.column && !chartConf.plotOptions.column.stacking)) {
				var mapAxis = this.setExtremes(chartConf);
				for (var i =0; i < chartConf.yAxis.length; i++){

					plotLines = chartConf.yAxis[i].plotLines;
					plotBands = chartConf.yAxis[i].plotBands;
					var finalMin = Math.min.apply(Math, [mapAxis.min[i], plotBands && plotBands[0].from != plotBands[0].to ? plotBands[0].from : mapAxis.min[i], plotLines && plotLines[0].width > 0 ? plotLines[0].value : mapAxis.min[i]].map(function(o) { return o; }));
					var finalMax = Math.max.apply(Math, [mapAxis.max[i], plotBands && plotBands[0].to != plotBands[0].from ? plotBands[0].to : mapAxis.max[i],  plotLines && plotLines[0].width > 0 ? plotLines[0].value : mapAxis.max[i]].map(function(o) { return o; }));

					if(chartConf.yAxis[i].min===undefined || chartConf.yAxis[i].min=='auto'){
						chartConf.yAxis[i].min = finalMin>=0 ? finalMin * 0.5 : finalMin * 1.5;
					}
					finalMin = chartConf.yAxis[i].min;
					if(chartConf.yAxis[i].min=="") chartConf.yAxis[i].min = null;
					if(chartConf.yAxis[i].max===undefined || chartConf.yAxis[i].max=='auto') {
						chartConf.yAxis[i].max = finalMax>=0 ? finalMax * 1.1 : finalMax * 0.9;

					}
					finalMax = chartConf.yAxis[i].max
					if(chartConf.yAxis[i].max=="") chartConf.yAxis[i].max = null;
					infoFroDrill.push({"min":finalMin,"max":finalMax,"plotBands":plotBands,"plotLines":plotLines})
				}
				isBasic = true;
			}

			if( chartType == 'gauge'){
				for (var i =0; i < chartConf.yAxis.length; i++){
					var mapAxis = this.setExtremes(chartConf);
					var finalMin = mapAxis.min[i];
					var finalMax = mapAxis.max[i];
					if(chartConf.yAxis[i].min == chartConf.yAxis[i].max){
						chartConf.yAxis[i].min = finalMin>=0 ? Math.round(finalMin * 0.5) : Math.round(finalMin * 1.5)
						chartConf.yAxis[i].max = finalMax>=0 ? Math.round(finalMax * 1.1) : Math.round(finalMax * 0.9);
					}

				}

			}
			if(chartConf.plotOptions && chartConf.plotOptions.column && chartConf.plotOptions.column.stacking) {
				for (var i =0; i < chartConf.yAxis.length; i++){
					if(chartConf.yAxis[i].min !=undefined && (chartConf.yAxis[i].min == 'auto' || chartConf.yAxis[i].min=='')) delete chartConf.yAxis[i].min

					if(chartConf.yAxis[i].max !=undefined && (chartConf.yAxis[i].max == 'auto' || chartConf.yAxis[i].max=='')) delete chartConf.yAxis[i].max
				}
			}
			chartConfMergeService.addProperty(renderObj.chartTemplate.advanced,chartConf);

			if(chartConf.chart.additionalData && chartConf.chart.additionalData.orderColumnDataValues) {
				var orderColumnValues = JSON.parse(chartConf.chart.additionalData.orderColumnDataValues);
				var orderKeys = Object.keys(orderColumnValues);
				for(var i = 0; i < chartConf.series.length; i++) {
					for(var j = 0; j < orderKeys.length; j++) {
						if(chartConf.series[i].name == orderKeys[j]) {
							chartConf.series[i].orderValue = orderColumnValues[orderKeys[j]];
						}
					}
				}
			}
            if(renderObj.chartTemplate.VALUES.CATEGORY) {
			var groupBy = renderObj.chartTemplate.VALUES.CATEGORY.groupby;
			var drillOrder = renderObj.chartTemplate.VALUES.CATEGORY.drillOrder;
			if(renderObj.chartTemplate.groupSeriesCateg && drillOrder && drillOrder[groupBy] && drillOrder[groupBy].orderColumn) {
				var orderType = drillOrder[groupBy].orderType == "desc" ? 'desc' : 'asc';
				if(drillOrder[groupBy].orderColumn == groupBy){
					if(orderType=='asc') sortAsc(chartConf.series, 'name');
					else sortDesc(chartConf.series, 'name');
				}else {
					if(orderType=='asc') sortAsc(chartConf.series, 'orderValue');
					else sortDesc(chartConf.series, 'orderValue');
				}
			}
            }

			this.chart =  new Highcharts.Chart(chartConf);
			if(isBasic){
				this.chart.extremes = infoFroDrill;
			}

			this.chart.widgetData = widgetData;
			if(jsonData){
				if(jsonData.jsonData){
					this.chart.jsonData = JSON.parse(jsonData.jsonData);
				}else{
					this.chart.jsonData = jsonData;
				}
			}
			if(selectionsAndParams){
				this.chart.selectionsAndParams = selectionsAndParams;
			}
			renderObj.chartSonifyService.setChart(this.chart)
			//return chart;

		}
	}

	this.initChartLibrary = function(panelId, drillUpText, decimalPoint, thousandsSep){



		Highcharts.setOptions({
			time: {
				useUTC: false
			},
			chart : {
				renderTo : panelId,
				backgroundColor : {
					linearGradient : [ 0, 0, 500, 500 ],
					stops : [ [ 0, 'rgb(255, 255, 255)' ],
							[ 1, 'rgb(240, 240, 255)' ] ]
				},
				plotBackgroundColor : 'rgba(255, 255, 255, .9)',
				plotShadow : true,
				plotBorderWidth : 1
			},
			exporting : {

				/**
						Removing button on the top-right corner that offers us
						printing of the chart.

						@author: danristo (danilo.ristovski@mht.net)
				 */
				enabled : false,

				url : 'http://' + 'knowage' + ':' + '8080'
						+ '/highcharts-export-web/'
			},
			lang : {
				decimalPoint : decimalPoint,
				thousandsSep : thousandsSep
			},
			drilldown:{
				drillUpButton:{
					 position:
		                {
		                   align: "right",
		                },
		                theme: {
		                    fill: 'white',
		                    "font-size": 12,
		                    "stroke-width": 1,
		                    stroke: 'silver',
		                    r: 0,
		                    states: {
		                        hover: {
		                            fill: '#ccc'
		                        },
		                        select: {
		                            stroke: '#039',
		                            fill: '#ccc'
		                        }
		                    }
		                }

			}
			},
			drilledCategories:[] //array used to save category names when drilling

		});

		  Highcharts.seriesTypes.pie.prototype.setTitle = function (titleOption) {
		        var chart = this.chart,
		            center = this.center || (this.yAxis && this.yAxis.center),
		            labelBox,
		            box,
		            format;

		        if (center && titleOption) {
		            box = {
		                x: chart.plotLeft + center[0] - 0.5 * center[2],
		                y: chart.plotTop + center[1] - 0.5 * center[2],
		                width: center[2],
		                height: center[2]
		            };

					format = titleOption.text || titleOption.format;
		            format = Highcharts.format(format, this);

		            if (this.title) {
		                this.title.attr({
		                    text: format
		                });

		            } else {
		                this.title = this.chart.renderer.label(format)
		                    .css(titleOption.style)
		                    .add()
		            }
		            labelBBox = this.title.getBBox();
		            titleOption.width = labelBBox.width;
		            titleOption.height = labelBBox.height;
		            this.title.align(titleOption, null, box);
		        }
		    };

		    Highcharts.wrap(Highcharts.seriesTypes.pie.prototype, 'render', function (proceed) {
		        proceed.call(this);
		        this.setTitle(this.options.title);
		    });
		// function wraps library method that controls series order
		  (function(HC) {

			  	function defined(obj) {
			        return obj !== UNDEFINED && obj !== null;
			    }

			  	var each = HC.each,
			    		pick = HC.pick,
			        mathMin = Math.min,
			        mathMax = Math.max,
			        mathAbs = Math.abs,
			        UNDEFINED;

			    HC.wrap(HC.seriesTypes.column.prototype, 'getColumnMetrics', function(proceed) {
			      var series = this,
			        options = series.options,
			        xAxis = series.xAxis,
			        yAxis = series.yAxis,
			        reversedXAxis = xAxis.reversed,
			        stackKey,
			        stackGroups = {},
			        columnIndex,
			        columnCount = 0;

			      // Get the total number of column type series.
			      // This is called on every series. Consider moving this logic to a
			      // chart.orderStacks() function and call it on init, addSeries and removeSeries
			      if (options.grouping === false) {
			        columnCount = 1;
			      } else {
			        each(series.chart.series, function(otherSeries) {
			          var otherOptions = otherSeries.options,
			            otherYAxis = otherSeries.yAxis;
			          if (otherSeries.type === series.type && otherSeries.visible &&
			            yAxis.len === otherYAxis.len && yAxis.pos === otherYAxis.pos) { // #642, #2086
			            if (otherOptions.stacking) {
			              stackKey = otherSeries.stackKey;
			              if (stackGroups[stackKey] === UNDEFINED) {
			                stackGroups[stackKey] = columnCount++;
			              }
			              columnIndex = stackGroups[stackKey];
			            } else if (otherOptions.grouping !== false) { // #1162
			              columnIndex = columnCount++;
			            }
			            otherSeries.columnIndex = columnIndex;
			          }
			        });
			      }

			      var categoryWidth = mathMin(
			          mathAbs(xAxis.transA) * (xAxis.ordinalSlope || options.pointRange || xAxis.closestPointRange || xAxis.tickInterval || 1), // #2610
			          xAxis.len // #1535
			        ),
			        groupPadding = categoryWidth * options.groupPadding,
			        groupWidth = categoryWidth - 2 * groupPadding,
			        pointOffsetWidth = groupWidth / columnCount,
			        optionPointWidth = options.pointWidth,
			        pointPadding = defined(optionPointWidth) ? (pointOffsetWidth - optionPointWidth) / 2 :
			        pointOffsetWidth * options.pointPadding,
			        pointWidth = pick(optionPointWidth, pointOffsetWidth - 2 * pointPadding), // exact point width, used in polar charts
			        colIndex = (series.columnIndex || 0) + (reversedXAxis ? 1 : 0), // #1251, #3737
			        pointXOffset = pointPadding + (groupPadding + colIndex *
			          pointOffsetWidth - (categoryWidth / 2)) *
			        (reversedXAxis ? -1 : 1);

			      // Save it for reading in linked series (Error bars particularly)
			      return (series.columnMetrics = {
			        width: pointWidth,
			        offset: pointXOffset
			      });
			    });
			  })(Highcharts);

			 //function that swaps position of checkbox on labels
			  (function(H) {
					var each = H.each,
						css = H.css;
 					Highcharts.Legend.prototype.positionCheckboxes = function() {
						var alignAttr = this.group && this.group.alignAttr,
						translateY,
						clipHeight = this.clipHeight || this.legendHeight,
						titleHeight = this.titleHeight;
 						if (alignAttr) {
						translateY = alignAttr.translateY;
						each(this.allItems, function(item) {
							var checkbox = item.checkbox,
							groupW = item.legendGroup.element.getBBox().width,
							top;
 							if (checkbox) {
							top = translateY + titleHeight + checkbox.y +
								(this.scrollOffset || 0);
							css(checkbox, {
								left: (alignAttr.translateX + item.checkboxOffset +
								checkbox.x - groupW - 40) + 'px',
								top: top + 'px',
								display: this.proximate || (
									top > translateY - 6 &&
									top < translateY + clipHeight - 6
								) ?
								'' : 'none'
							});
							}
						}, this);
						}
					}
					}(Highcharts));

	}

	var sortAsc = function (array, comparator){
		array.sort(function(a, b){
		    var nameA=a[comparator].toLowerCase(), nameB=b[comparator].toLowerCase()
		    if (nameA < nameB) //sort string ascending
		        return -1
		    if (nameA > nameB)
		        return 1
		    return 0 //default return value (no sorting)
		})
	}

	var sortDesc = function (array, comparator){
		array.sort(function(a, b){
		    var nameA=a[comparator].toLowerCase(), nameB=b[comparator].toLowerCase()
		    if (nameA > nameB) //sort string descending
		        return -1
		    if (nameA < nameB)
		        return 1
		    return 0 //default return value (no sorting)
		})
	}

	this.handleCrossNavigationTo =function(e) {
		var date = new Date(e.point.x);
		var char =  "-" ;
		var theyear=date.getFullYear()
		var themonth=date.getMonth()+1
		var theday=date.getDate()
		var date_format = theday+char+themonth+char+theyear;
		var t = chartConfConf;
		if (!e.seriesOptions) {
			var chart = this;
			//chart.showLoading('Loading...');
			var categoryName = null;
			var categoryValue = t.xAxis && t.xAxis.type == "datetime" ? date_format : e.point.name;

			if (e.point.hasOwnProperty('category')) {
				if(isNaN(e.point.category)){
				categoryName = e.point.category;
				}
			}

			if(!categoryName && chartConfConf.chart.additionalData){
				categoryName = chartConfConf.chart.additionalData.categoryName;
			}

			//for scatter
			if(!categoryValue && e.point.category &&e.point.category.name){
				categoryValue = t.xAxis && t.xAxis.type == "datetime" ? date_format : e.point.category.name;
			}


			var serieName = e.point.series.name;
			var serieValue = e.point.y;

			var groupingCategoryName = null;
			var groupingCategoryValue = null;

			if (e.point.hasOwnProperty('group')) {
				groupingCategoryName = e.point.group.name;
				groupingCategoryValue = e.point.group.value;
			}



			if(parent.execExternalCrossNavigation){
				var navData={
         			chartType:	"HIGHCHART",
         			CATEGORY_NAME :categoryName,
         			CATEGORY_VALUE :categoryValue,
         			SERIE_NAME :serieName,
         			SERIE_VALUE :serieValue,
         			GROUPING_NAME:groupingCategoryName,
         			GROUPING_VALUE:groupingCategoryValue,
         			stringParameters:null
				};
				parent.execExternalCrossNavigation(navData,null, null, currentDocumentLabel )
			}

		}
	};

	var drilledSerie = "";
	var storeMinAndMax = {};
	var indexOfAxis = 0;
	this.handleDrilldown = function(e){
		var drillable = this.drillable != undefined ?
				this.drillable : (this.options.chart.additionalData.isCockpit ?
						this.options.chart.additionalData.drillable: this.options.chart.additionalData.drillableChart);
		if(!drillable){
			console.log("chart is not drillable")
			return;
		}
		var chart = this;
		if(!chart.breadcrumb)chart.breadcrumb=[];

		if (!e.seriesOptions)
		{
			/*
				Disable drill down when user clicks on the hyperlink that points to
				the category value (on X-axis). Drill down is enabled only when user
				clicks on single serie within one category value (group). Detection
				of clicking on the category hyperlink is done via checking if the
				appropriate property in input parameter of the listener is not a
				number. If it is, the hyperlink is clicked and we have a ordinal
				number of the category on which user clicked.

				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			if (isNaN(e.category))
			{
				chart.showLoading('Loading...');

					var params = {};
					if(chart.jsonData ){
						params.jsonMetaData = chart.jsonData.metaData;
					}
					if(chart.widgetData ){
						params.widgetData = chart.widgetData;
						var column = chart.widgetData.chartTemplate.CHART.VALUES.CATEGORY.column;
					}

					var drillValue = e.point.name;

					var highchartsDrilldownHelperDone = false;
					if(chart.jsonData ){
						params.jsonMetaData = chart.jsonData.metaData;
						try {
							var fields = chart.jsonData.metaData.fields;
							for(var i=0; i<fields.length;i++){
								var aField = fields[i];
								if(aField.header && aField.header==column){
									if(aField.type=="date"){
										highchartsDrilldownHelper.drilldown(drillValue, e.point.series.name, chart.breadcrumb, aField.dateFormatJava);
										highchartsDrilldownHelperDone = true;
									}
								}
							}
						}catch(e){
							console.log(e);
						}
					}

					if(!highchartsDrilldownHelperDone){
						highchartsDrilldownHelper.drilldown(e.point.name, e.point.series.name, chart.breadcrumb);
					}

					params.breadcrumb = JSON.stringify(chart.breadcrumb);
					var forQueryParam= "";
					if(chart.selectionsAndParams && chart.selectionsAndParams.parameters){
						params.parameters = cockpitModule_datasetServices.getParametersAsString(chart.selectionsAndParams.parameters);
					}
					if(chart.selectionsAndParams && chart.selectionsAndParams.selections){
						params.selections = chart.selectionsAndParams.selections;
					}
					if(chart.selectionsAndParams && chart.selectionsAndParams.aggregations){
						params.aggregations = chart.selectionsAndParams.aggregations;
					}
					if(chart.selectionsAndParams && chart.selectionsAndParams.par){
						forQueryParam = chart.selectionsAndParams.par;
					}
					jsonChartTemplate.drilldownHighchart(params,forQueryParam)
					.then(function(series){
							if(chart.userOptions.chart.type!="pie" && !chart.userOptions.plotOptions.column.stacking){
							var yaxis = chart.yAxis;
							var chartSeries = chart.series;
							drilledSerie = series.serieName;
							if(!chart.drilledSerie){
								for (var i = 0; i<yaxis.length; i++){
									for (var j = 0; j<chartSeries.length; j++){
										if((chartSeries[j].name == drilledSerie) && yaxis[i]==chartSeries[j].yAxis){
											chart.drilledSerie = drilledSerie;
											indexOfAxis = i;
										}
									}
								}
							}
                               
								var maxData = Math.max.apply(Math, series.data.map(function(o) { if(o.y){return o.y;}else{return null} }));
								if (maxData < 0) maxData = 0;
								var minData = Math.min.apply(Math, series.data.map(function(o) { if(o.y){return o.y;}else{return null} }));

							var minDrill = Math.min.apply(Math, [minData, chart.extremes[indexOfAxis].plotBands && chart.extremes[indexOfAxis].plotBands[0].from != chart.extremes[indexOfAxis].plotBands[0].to ? chart.extremes[indexOfAxis].plotBands[0].from : minData, chart.extremes[indexOfAxis].plotLines && chart.extremes[indexOfAxis].plotLines[0].width > 0 ? chart.extremes[indexOfAxis].plotLines[0].value : minData].map(function(o) { return o; }));
							var maxDrill = Math.max.apply(Math, [maxData, chart.extremes[indexOfAxis].plotBands && chart.extremes[indexOfAxis].plotBands[0].to != chart.extremes[indexOfAxis].plotBands[0].from ? chart.extremes[indexOfAxis].plotBands[0].to : maxData,  chart.extremes[indexOfAxis].plotLines && chart.extremes[indexOfAxis].plotLines[0].width > 0 ? chart.extremes[indexOfAxis].plotLines[0].value : maxData].map(function(o) { return o; }));
                         
							storeMinAndMax[series.name]={min:minDrill>=0 ? minDrill * 0.5 : minDrill * 1.5,max:maxDrill>=0 ? maxDrill * 1.1 : maxDrill * 0.9}
							setTimeout(function () {
		                        chart.yAxis[indexOfAxis].update({
		                            max: maxDrill*1.1,
		                            min: minDrill > 0 ? minDrill*0.5 : minDrill*1.1,
		                        });
		                        chart.redraw()
		                    }, 0);
						}

						chart.firstXaxisTitle;
						if(chart.options.drilledCategories.length==0){
							chart.firstXaxisTitle = chart.xAxis[0].options.title.text;
							 if(series.firstLevelCategory){
								 chart.options.drilledCategories.push(series.firstLevelCategory);
					        } else {
					          	chart.options.drilledCategories.push(chart.xAxis[0].userOptions.title.text);
					        }
						}
						chart.options.drilledCategories.push(series.category);
						var xAxisTitle={
								text:""
				        };

						if(chart.xAxis[0].options.title.text!=""){
							 xAxisTitle.text = series.category;
						}


			            var yAxisTitle={
			            		text:series.serieName
			            };
			            chart.xAxis[0].setTitle(xAxisTitle);

			            if(chart.options.chart.type!="pie" && chart.yAxis[0].userOptions.title.custom==false){
			            	chart.yAxis[0].setTitle(yAxisTitle);
			            }

			            chart.addSeriesAsDrilldown(e.point, series);

			            if(series.firstLevelCategory){
			            	var backText="Back to: <b>"+series.firstLevelCategory+"</b>";
			            } else {
			            	var backText="Back to: <b>"+ chart.options.drilledCategories[chart.options.drilledCategories.length-2]+"</b>";
			            }


			            chart.drillUpButton.attr({
		                    text: backText
		                }).align();

						chart.hideLoading();
					});
			}
		}
	}


	this.handleDrillup = function(){

		var chart=this;
		var axisTitle = chart.options.drilledCategories[chart.options.drilledCategories.length-2]
		chart.options.drilledCategories.pop();
		titleText=chart.options.drilledCategories[chart.options.drilledCategories.length-2] ? chart.options.drilledCategories[chart.options.drilledCategories.length-2] : chart.options.drilledCategories[0];
		var backText=titleText;

        var xAxisTitle={
				text:""
        };

		if(chart.xAxis[0].options.title.text!=""){
			 xAxisTitle.text = axisTitle;
		}

		if(chart.firstXaxisTitle && chart.options.drilledCategories.length==1){
			xAxisTitle.text = chart.firstXaxisTitle
		}

		chart.xAxis[0].setTitle(xAxisTitle);
		var yAxisTitle={
				text: ' '
		};
		if(chart.userOptions.chart.type!="pie"  && !chart.userOptions.plotOptions.column.stacking){
			setTimeout(function () {
	            chart.yAxis[indexOfAxis].update({
	                max: chart.breadcrumb[chart.breadcrumb.length-1] ? storeMinAndMax[chart.breadcrumb[chart.breadcrumb.length-1].selectedName].max : chart.extremes[indexOfAxis].max != "" ? chart.extremes[indexOfAxis].max : null,
	            	min: chart.breadcrumb[chart.breadcrumb.length-1] ? storeMinAndMax[chart.breadcrumb[chart.breadcrumb.length-1].selectedName].min : chart.extremes[indexOfAxis].min != "" ? chart.extremes[indexOfAxis].min : null,
	            });
	           if(!chart.breadcrumb[chart.breadcrumb.length-1]) {
	        	   delete chart.drilledSerie
	           }
	           chart.redraw()
	           if(chart.drillUpButton) {
	        	   chart.drillUpButton.textSetter("← Back to: <b>"+backText+"</b>");
	        	   chart.drillUpButton.fill = "white"
	           }

	        }, 0);
		}

       if(chart.drilldownLevels.length==0 && chart.options.chart.type!="pie" && chart.yAxis[0].userOptions.title.custom==false){
    	   chart.yAxis[0].setTitle(yAxisTitle);
       }
       chart.drillUpButton.textSetter("← Back to: <b>"+backText+"</b>");
    	// TODO: commented by: danristo (EXT -> ANGULAR)
		//Sbi.chart.viewer.HighchartsDrilldownHelper.drillup();
       highchartsDrilldownHelper.drillup(chart.breadcrumb);

	}
	this.setExtremes = function (chartConf){
		var mapAxis=  {min:{},max:{}};
		for (var i =0; i < chartConf.series.length; i++){
			var max = Math.max.apply(Math, chartConf.series[i].data.map(function(o) {  if(o.y){return o.y;}else{return null} }));
			var min = Math.min.apply(Math, chartConf.series[i].data.map(function(o) {  if(o.y){return o.y;}else{return null} }));
			if(mapAxis.min[chartConf.series[i].yAxis]!=undefined){
				if(mapAxis.min[chartConf.series[i].yAxis] > min)
				mapAxis.min[chartConf.series[i].yAxis] = min;
			} else {
				mapAxis.min[chartConf.series[i].yAxis] = min;
			}
			if(mapAxis.max[chartConf.series[i].yAxis]!=undefined){
				if(mapAxis.max[chartConf.series[i].yAxis] < max)
				mapAxis.max[chartConf.series[i].yAxis] = max;
			} else {
				mapAxis.max[chartConf.series[i].yAxis] = max;
			}
		}
		return mapAxis;
	}

	this.updateData = function(widgetData){


		var category = null;
		var column = null;
		var orderColumn = null;
		var data = widgetData.jsonData.rows;
		var counter = 0;
		var arrayOfMeasuers = [];

		for (var i = 0; i<widgetData.columnSelectedOfDataset.length; i++){
			if(widgetData.columnSelectedOfDataset[i].fieldType=="MEASURE"){
				arrayOfMeasuers.push(widgetData.columnSelectedOfDataset[i].alias)
			}
		}

		var columnsSerieValue = [];

		if (this.chart.options.chart.type != "gauge") {

		   category = widgetData.chartTemplate.CHART.VALUES.CATEGORY.name;
		   orderColumn = widgetData.chartTemplate.CHART.VALUES.CATEGORY.orderColumn == "" ? 2 : 3;

		   for (var j = 1; j < widgetData.jsonData.metaData.fields.length; j++) {

			   if (widgetData.jsonData.metaData.fields[j].header
					   && widgetData.jsonData.metaData.fields[j].header == category) {

				   column =  widgetData.jsonData.metaData.fields[j].name;

			   }
		   }

		}

		var seriesNamesColumnBind ={};
		for(var i =1 ; i<widgetData.jsonData.metaData.fields.length; i++){

			var field = widgetData.jsonData.metaData.fields[i];

			if(field.header){

				seriesNamesColumnBind[field.header]=field.name;

			  	if(field.header.lastIndexOf("_")>0){
			  		seriesNamesColumnBind[field.header.substring(0,field.header.lastIndexOf("_"))]=field.name;
			  	}
			  	if (arrayOfMeasuers.indexOf(field.header) !=-1) {
					columnsSerieValue.push(field.name);
				}
			}
		}


		var counterSeries =  widgetData.chartTemplate.CHART.VALUES.SERIE.length;

		var drill = null;



		for (var j = 0; j < this.chart.series.length; j++) {
			  if (this.chart.options.series[0].data.length > 0 && this.chart.options.series[0].data[0].drilldown) {
				  drill = this.chart.options.series[0].data[0].drilldown
			  } else {
				  drill = false
			  }
			  this.chart.series[j].setData([]);
		  }

		var areaRangeType = false;
		var newData = new Array();

		for (var i = 0; i < counterSeries; i++) {

			if(areaRangeType) {
				areaRangeType = false;
				continue;
				}

			var newDataSerie = new Array();
			for (var j = 0; j < data.length; j++) {

				var pointOptions={};
				if (this.chart.options.chart.type == "gauge") {
					pointOptions.y = parseFloat(data[j]["column_" + (i + 1)]);
					newDataSerie.push(pointOptions);

				} else {
					if(this.chart.options.xAxis[0].type!="datetime"){

						if(widgetData.chartTemplate.CHART.VALUES.SERIE[i].type == "arearangelow"){

							pointOptions.low = parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i].name]]);
							pointOptions.high = parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i+1].name]]);
							areaRangeType = true;

						} else {

							pointOptions.y = parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i].name]]);

						}

						pointOptions.name=data[j][column];
						if(this.chart.options.chart.type!= "pie"){
							pointOptions.drilldown = drill;
						}
					}
					else{
						pointOptions = [];
						pointOptions.push(Date.parse(data[j][column]));

						if(widgetData.chartTemplate.CHART.VALUES.SERIE[i].type == "arearangelow"){

							pointOptions.push(parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i].name]]));
							pointOptions.push(parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i+1].name]]));
							areaRangeType = true;

						} else {

							pointOptions.push(parseFloat(data[j][seriesNamesColumnBind[widgetData.chartTemplate.CHART.VALUES.SERIE[i].name]]));

						}


					}
					newDataSerie.push(pointOptions);
					if(Object.prototype.toString.call(pointOptions) === '[object Array]')
					pointOptions = [];
				}
			}
			newData.push(newDataSerie);
		}



		if(this.chart.options.chart.type == "pie"){
			for (var i = 0; i<newData.length; i++){
				for (var j = 0; j<newData[i].length; j++){

					if(newData[i][j].y==0){
						counter ++
					}
				}
				if (counter==newData[i].length){
					newData[i]=[];
					counter = 0;
				}
			}
		}


		for (var i = 0; i < counterSeries; i++) {
			if(widgetData.chartTemplate.CHART.VALUES.SERIE[i].type == "arearangelow"){

				counterSeries -= 1;

			}

			this.chart.series[i].update({data:newData[i]},false);
		}
		this.chart.redraw()
	}

	var adjustChartSize = function(container,chartConf){
		if(chartConf.chart.heightDimType=='percentage'){
			chartConf.chart.height = container.clientHeight*(chartConf.chart.height/100);
		}

		if(chartConf.chart.widthDimType=='percentage'){
			chartConf.chart.width = container.clientWidth*(chartConf.chart.width/100);
		}
	}




	/*this.cancelSonify = function () {
		//this is for speed
		chartSonifyService.cancelSonify();
	};

	this.playSonify = function () {
		chartSonifyService.playSonify()
	};

	this.pauseSonify = function () {
		chartSonifyService.pauseSonify();
	};
	this.rewindSonify = function () {
		chartSonifyService.rewindSonify();
	};*/

}])
