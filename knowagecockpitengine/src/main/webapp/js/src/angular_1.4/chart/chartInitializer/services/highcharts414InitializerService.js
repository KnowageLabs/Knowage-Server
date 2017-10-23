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

.service('highcharts414',['highchartsDrilldownHelper','jsonChartTemplate',function(highchartsDrilldownHelper,jsonChartTemplate){

	this.chart = null;
	var chartConfConf = null;

	this.renderChart = function(chartConf,element,handleCockpitSelection,exportWebApp){
		chartConfConf = chartConf;
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
				this.chart = renderTreemap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo);
			}
		}
		else if (chartType == 'heatmap')
		{
			delete this.updateData;
			if(exportWebApp) {
				return renderHeatmap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo,exportWebApp );
			}
			this.chart = renderHeatmap(chartConf,handleCockpitSelection, this.handleCrossNavigationTo);
		}
		else
		{
			if (chartType == 'scatter') delete this.updateData;
			this.chart =  new Highcharts.Chart(chartConf);
			//return chart;

		}
	}

	this.initChartLibrary = function(panelId, drillUpText, decimalPoint, thousandsSep){



		Highcharts.setOptions({
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
				drillUpText : drillUpText,
				decimalPoint : decimalPoint,
				thousandsSep : thousandsSep
			},
			drilldown:{
				drillUpButton:{
					 position:
		                {
		                   align: "center"
		                },

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
				parent.execExternalCrossNavigation(navData,JSON.parse(driverParams), null, currentDocumentLabel )
			}

		}
	};


	this.handleDrilldown = function(e){
		var chart = this;

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


				highchartsDrilldownHelper.drilldown(e.point.name, e.point.series.name);


					jsonChartTemplate.drilldownHighchart(JSON.stringify(highchartsDrilldownHelper.breadcrumb))
					.then(function(series){

						if(chart.options.drilledCategories.length==0){
				        	   chart.options.drilledCategories.push(chart.xAxis[0].axisTitle.textStr);

				           }

				           chart.options.drilledCategories.push(series.category);
				            var xAxisTitle={
				            	text:series.category
				            };
				            var yAxisTitle={
				            		text:series.serieName
				            };
				            if(chart.xAxis[0].userOptions.title.customTitle==false){
				            chart.xAxis[0].setTitle(xAxisTitle);
				            }
				            if(chart.yAxis[0].userOptions.title.custom==false){
				            chart.yAxis[0].setTitle(yAxisTitle);
				            }

				            chart.addSeriesAsDrilldown(e.point, series);

				            var backText="Back to: <b>"+chart.options.drilledCategories[chart.options.drilledCategories.length-2]+"</b>";

				            chart.drillUpButton.textSetter(backText);

							chart.hideLoading();

					});

			}
		}
	}


	this.handleDrillup = function(){

		var chart=this;

		// sets the title on x axis

		chart.options.drilledCategories.pop();
		titleText=chart.options.drilledCategories[chart.options.drilledCategories.length-1];
		var backText=chart.options.drilledCategories[chart.options.drilledCategories.length-2];
		chart.drillUpButton.textSetter("Back to: <b>"+backText+"</b>");
        //  chart.redraw();
		var xAxisTitle={
            	text:titleText
            };
		    if(chart.xAxis[0].userOptions.title.customTitle==false){
            chart.xAxis[0].setTitle(xAxisTitle);
		    }

		var yAxisTitle={
				text: ' '
		};


       if(chart.drilldownLevels.length==0 && chart.yAxis[0].userOptions.title.custom==false){
    	   chart.yAxis[0].setTitle(yAxisTitle);
       }

    	// TODO: commented by: danristo (EXT -> ANGULAR)
		//Sbi.chart.viewer.HighchartsDrilldownHelper.drillup();
       highchartsDrilldownHelper.drillup();

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

		var newData = new Array();
		for (var i = 0; i < counterSeries; i++) {
			var newDataSerie = new Array();
			for (var j = 0; j < data.length; j++) {

				var pointOptions={};
				if (this.chart.options.chart.type == "gauge") {
					pointOptions.y = parseFloat(data[j]["column_" + (i + 1)]);
					newDataSerie.add(pointOptions);

				} else {
					if(this.chart.options.xAxis[0].type!="datetime"){
						pointOptions.y = parseFloat(data[j][seriesNamesColumnBind[this.chart.series[i].name]]);
						pointOptions.name=data[j][column];
						if(this.chart.options.chart.type!= "pie"){
							pointOptions.drilldown = drill;
						}
					}
					else{
						var date = data[j][column].split("/");
						var day = date[0];
						var month = date[1]-1;
						var year = date[2];
						pointOptions = [];
						pointOptions.push(Date.UTC(year, month, day));

						pointOptions.push(parseFloat(data[j][seriesNamesColumnBind[this.chart.series[i].name]]));
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




}])