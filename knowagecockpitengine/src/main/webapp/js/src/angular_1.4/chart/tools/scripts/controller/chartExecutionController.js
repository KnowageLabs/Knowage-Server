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

/**
 * 'chartExecutionController' - the name of the Angular controller that is responsible for managing and handling all the logic and the
 * data needed for the execution of the chart (chart rendering). Its module injects the configuration that we provided (sbiModule_...)
 * and the WebServiceManagerFactory implementation ('chartExecutionWebServiceManagerFactory' service). Besides those injects, the
 * controller is injecting the $scope object and the $http service.
 */
angular
	.module('chartexecution.controller', ['chartexecution.directives','chartexecution.configuration','chartexecution.webservicemanagerfactory','chartengine.settings','chartRendererModule'])
	.controller('chartExecutionController', ["$rootScope","$scope","$http","chartExecutionWebServiceManagerFactory","sbiModule_translate","sbiModule_messaging","chartEngineSettings",chartExecutionFunction]);

/**
 * The function that represents the controller logic.
 * @param $scope
 * @param $http
 * @param chartExecutionWebServiceManagerFactory
 * @param sbiModule_translate
 * @param sbiModule_messaging
 */
function chartExecutionFunction($rootScope,$scope,$http,chartExecutionWebServiceManagerFactory,sbiModule_translate,sbiModule_messaging,chartEngineSettings) {

	var chartConfiguration, isChartHeightEmpty, isChartWidthEmpty, localeFormatted = null, wsPrefixPropText = "wsPrefix";


	// Put the module for message translation on the scope, so it can be accessed from the JSP as well.
	$scope.translate = sbiModule_translate;

	// Assign the acquired shared settings configuration from the service that provides data from the Setting.js file. (danristo)
	chartEngineSharedSettings = chartEngineSettings;

	$scope.heightType = "";
	$scope.widthType = "";

	// The indicators if the chart is about to be exported (downloaded) and if chart is about to render after receiving its JSON. (danristo)
	// These indicators help us informing users about the process that is happening (download/preparing), but only for Highcharts charts.
	$scope.showDownloadProgress = false;
	$scope.loadingChart = false;

	$scope.isLibChartJs = isLibChartJs;
	$scope.includeChartTable= includeChartTable;

    $scope.chartTableData;



	$scope.chartLibNamesConfig = chartLibNamesConfig;
	$scope.chartTemplate = angular.fromJson(jsonTemplate);
	$scope.datasetLabel = datasetLabel;
	$scope.jsonData = metaData;




	/**
	 * ----------------------------------------------------------------
	 * The code used for parsing locale to the expected value. [START]
	 * ----------------------------------------------------------------
	 */
		$scope.parseLocale = function() {

		if(  locale != 'undefined') {

			console.info("Locale (raw): ", locale);

			var localeToReturn = null;
			var localeParsed = locale.split("_");

			for(var i=0; i<localeParsed.length; i++) {
				(i==0) ? localeToReturn = localeParsed[i] : localeToReturn += "-" + localeParsed[i];
			}

			console.info("Locale (formatted for (D3) chart rendering): ",localeFormatted);

			return localeToReturn;

			}

		}

	localeFormatted = $scope.parseLocale();
	var bla={};



	// RENDERING THE CHART (END)

	// RESIZE HANDLER (START)
	window.onresize = function() {

		/*
			If there are chart dimension values (height and width) specified
			for this chart (chart that relies on the D3 library), variable
			'chartConfiguration' will stay 'null', since we did not enter
			the part of code that specify this value (actual JSON file) that
			we receive from the server. This way, resize will not be applied
			this chart and it will despite of resizing stay with the same
			size as on the beginning (on the initial render of the chart).
		*/
		if (chartConfiguration!=null) {

			var chartType = chartConfiguration.chart.type.toUpperCase();

			/*
				Check if the chart (document) that we want to render (run) on the page
				uses D3 as a library for rendering.
			*/
			var isD3Chart = (chartType == "SUNBURST" || chartType == "WORDCLOUD" || chartType == "PARALLEL" || chartType == "CHORD");

			// Provide resizing handling for the ChartJS charts as for the D3 charts - re-render the chart.	(danristo)
			if (isD3Chart || isLibChartJs) {
				/*
					Set new values for the height and the width of the chart (the DIV
					that contains the chart), as a consequence of a resizing the window
					(panel). This will eventually affect on those chart elements that
					depend on these two parameters.
				*/


//				widthDimType=="percentage" ? chartConfiguration.chart.width =
//					window.innerWidth*chartConfiguration.chart.width/100 - scrollbarWidth : chartConfiguration.chart.width = window.innerWidth - scrollbarWidth ;

//					if ((isChartWidthEmpty==true &&
//							(heightDimType=="pixels" && chartConfiguration.chart.height > window.innerHeight ||
//									heightDimType=="percentage" && chartConfiguration.chart.height*window.innerHeight/100 > window.innerHeight)) ||
//									(widthDimType=="pixels" && chartConfiguration.chart.width >= window.innerWidth ||
//											widthDimType=="percentage" && chartConfiguration.chart.width*window.innerWidth/100 >= window.innerWidth)) {
//
//					var scrollDiv = document.createElement("div");
////						scrollDiv.className = "scrollbar-measure";
//					scrollDiv.style = "width: 100px; height: 100px;	overflow: scroll; position: absolute; top: -9999px;";
//					document.body.appendChild(scrollDiv);
//
//					// Get the scrollbar width
//					scrollbarWidth = scrollDiv.offsetWidth - scrollDiv.clientWidth;
//					console.warn(scrollbarWidth); // Mac:  15
//
//					// Delete the DIV
//					document.body.removeChild(scrollDiv);
////						chartConfiguration.chart.width = window.innerWidth - scrollbarWidth;
//					console.log(window.innerWidth - scrollbarWidth);
////					chartConfiguration.chart.width = window.innerWidth - scrollbarWidth;
//					widthDimType=="percentage" ? chartConfiguration.chart.width = window.innerWidth - scrollbarWidth : chartConfiguration.chart.width =
//						window.innerWidth*chartConfiguration.chart.width/100 - scrollbarWidth ;
//
//				}
//
//				else {
					if (isChartHeightEmpty==true) {
//						chartConfiguration.chart.height = window.innerHeight-5; // sometimes is newHeight != window.innerHeight
						chartConfiguration.chart.height = window.innerHeight;
					}

					if (isChartWidthEmpty==true) {
//						chartConfiguration.chart.width = window.innerWidth-5; // sometimes is newWidth != window.innerWidth
						chartConfiguration.chart.width = window.innerWidth;
					}
//				}

				/* Re-render the chart after resizing the window (panel). */
					$scope.chartInitializer.renderChart(chartConfiguration,localeFormatted);
			}
		}

	}
	// RESIZE HANDLER (END)

	exportChart = function(exportType) {

		// Set (to TRUE) indicator for showing the downloading progress immediately on initialization of exporting.
		$scope.showDownloadProgress = true;

		/**
		 * 'chartType' - information about the chart type of the document that we are rendering. Useful for
		 * 		detection of the library that it belongs, since the parameters needed for the exporting are
		 * 		different for the Highcharts and the D3 charts.
		 *
		 * 'isD3Chart' - variable that indicates if the chart type is D3 (according to the 'chartType' value).
		 *
		 * 'thisContextName' - context name of the engine that is running and rendering the chart.
		 *
		 * 'exporterContextName' - the context name of the engine that is used to export the chart.
		 *
		 * 'jsonChartTemplateTemp' - we need this temporary variable, since the user can make multiple exporting
		 * 		(JPG and/or PDF) while the chart is rendered once. Namely, the 'jsonTemplate' is a JSON string,
		 * 		whilst we need the JSON object as the input parameter of the service that will be called for
		 * 		exporting of the chart.
		 */
		var chartType, isD3Chart, thisContextName, exporterContextName, jsonChartTemplateTemp;

		chartType = chartConfiguration.chart.type.toUpperCase();
		isD3Chart = (chartType == "SUNBURST" || chartType == "WORDCLOUD" || chartType == "PARALLEL" || chartType == "CHORD");

		thisContextName	= '${pageContext.request.contextPath}';  //'knowagechartengine', /'knowage';
		thisContextName.replace('/','');

		exporterContextName = 'highcharts-export-web';

//		console.log("isD3Chart:",isD3Chart);
//		console.log(chartConfiguration);

//		/*
//			Since we are calling the rendering part and VM (before the rendering) from the Highcharts
//			Export web application (we clicked on the Export option in File dropdown on the page where
//			chart renders), we need to forward this information towards the belonging VM so it can adapt
//			its code (skipping of some parts of the initial (standard) implementation). This additional
//			property (data) is "exportWebApp".
//			@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
//		*/

		/**
		 * When we need to export charts when they are rendered, we need to send a proper information
		 * about the dimensions of that chart, i.e. the dimensions and dimension types that user has
		 * set for the chart. If the type of the dimension is "percentage", we need to convert numeric
		 * values that are set for this/these dimension(s), because they represent a percentage, not
		 * the absolute value (in pixels).
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */

		jsonChartTemplateTemp = JSON.parse(jsonTemplate);

		var jsonObjHeight = jsonChartTemplateTemp.CHART.height;
		var jsonObjWidth = jsonChartTemplateTemp.CHART.width;

		var jsonObjHeightEmpty = false;
		var jsonObjWidthEmpty = false;

		/*
			CONVERSION OF DIMENSION(S) EXPRESSED IN PERCENTAGES INTO THE ABSOLUTE ONE,
			EXPRESSED IN PIXELS (SO WE CAN GIVE ACCURATE DATA TO THE PHANTOM JS).
		*/

		if (jsonObjHeight!=undefined && jsonObjHeight!="") {
			jsonObjHeight = Number(jsonObjHeight);
		}
		else {
			jsonObjHeight = window.innerHeight
			jsonObjHeightEmpty = true;
		}

		if (jsonObjWidth!=undefined && jsonObjWidth!="") {
			jsonObjWidth = Number(jsonObjWidth);
		}
		else {
			jsonObjWidth = window.innerWidth;
			jsonObjWidthEmpty = true;
		}

		if (jsonChartTemplateTemp.CHART.heightDimType=="percentage" && !jsonObjHeightEmpty) {
			jsonObjHeight = jsonObjHeight*window.innerHeight/100;
		}

		if (jsonChartTemplateTemp.CHART.widthDimType=="percentage" && !jsonObjWidthEmpty) {
			jsonObjWidth = jsonObjWidth*window.innerWidth/100;
		}

		jsonChartTemplateTemp.CHART.height = jsonObjHeight;
		jsonChartTemplateTemp.CHART.width = jsonObjWidth;

		var parameters = {
			jsonTemplate: JSON.stringify(jsonChartTemplateTemp),
			chartType: jsonChartTemplateTemp.CHART.type,
			exportWebApp: true,
			driverParams: driverParams
		};

		if (isD3Chart) {
			/*
				Forward the D3 chart's height and width towards the part of the code that calls the
				Highcharts service for exporting chart.
				@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			*/
			/**
			 * Submit the form with the data about the chart in the JSON form, so the Export engine can
			 * render it and later capture it through the PhantomJS and export it back to the browser.
			 */

			var form = document.getElementById('export-chart-form');

			var encoded = btoa(document.getElementsByTagName('BODY')[0].innerHTML);

			form.method = "post";

			form.elements[0].value = encoded;
         	form.elements[1].value = 'html';
         	form.elements[2].value = (exportType=='PDF')?'application/pdf':'image/png';
         	form.elements[3].value = '600';
         	form.elements[4].value = 'Chart';
         	form.elements[5].value = 'false';
         	form.elements[6].value = jsonObjHeight;
         	form.elements[7].value = jsonObjWidth;

			form.action = protocol + '//'+ hostName + ':' + serverPort + '/highcharts-export-web/';
         	form.target = '_blank'; // result into a new browser tab

         	form.submit();

         	// Reset (to FALSE) indicator for showing the downloading progress immediately after the submitting of the form.
         	$scope.showDownloadProgress = false;

		}
		else {

			chartExecutionWebServiceManagerFactory.run('jsonChartTemplate', parameters, [],

				function (response) {

						/*
							WORKAROUND: Replacing in other way - from the ASCII code for the single quote character to the "escaped" single quote combination in order
							to enable a proper (adequate) the exporting of the chart. This way we will decode the former single quote in the chart template that was
							exchanged for this code (JSON cannot handle single quote inside it) and have a single quote on its place in the exported chart.
							@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						*/
	//						response.responseText = response.responseText.replace(new RegExp("&#39;",'g'),"\\'");

						var chartConfig = response.data;

						/*
							If the chart is of the type that relies on the Highcharts library, check
							if the type is of HEATMAP or TREEMAP, since they need preparation of the
							data provided for such a chart. This preparation is done locally, inside
							this ('KnowageChartEngine') project and we do not have access to it from
							the	one that uses the Phantom JS ('highcharts-export' project).
							@author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
						*/
						if (chartType == 'TREEMAP' || chartType == 'HEATMAP') {

							var jsonChartConf = chartConfig;

							if(chartType == 'TREEMAP' && typeof(prepareChartConfForTreemap) == "function") {
								chartConfig = JSON.stringify(prepareChartConfForTreemap(jsonChartConf));
							}
							else if(chartType == 'HEATMAP' && typeof(prepareChartConfForHeatmap) == "function") {
								chartConfig = JSON.stringify(prepareChartConfForHeatmap(jsonChartConf));
							}

						}

						/**
						 * Submit the form with the data about the chart in the JSON form, so the Export engine can
						 * render it and later capture it through the PhantomJS and export it back to the browser.
						 */

						var form = document.getElementById('export-chart-form');

						// The method of submitting the form must be POST, in order not to send data through the URL.
						form.method = "post";

						console.log("Chart configuration:",chartConfig);
						console.log("Chart height:",jsonObjHeight);
						console.log("Chart width:",jsonObjWidth);

			         	form.elements[0].value = chartConfig;
	         			form.elements[1].value = 'options';
			         	form.elements[2].value = (exportType=='PDF')?'application/pdf':'image/png';
			         	form.elements[3].value = '600';
			         	form.elements[4].value = 'Chart';
			         	form.elements[5].value = 'false';

			         	// The URL that we are going to hit with the chart data in the JSON form.
						form.action = protocol + '//'+ hostName + ':' + serverPort + '/' + exporterContextName + '/';
						// When the form is about to be submitted, open a new (empty) browser tab - while exporting the chart.
			         	form.target = '_blank';

			         	form.submit();

			         	// Reset (to FALSE) indicator for showing the downloading progress immediately after the submitting of the form.
			         	$scope.showDownloadProgress = false;

				}

			);


		}
	}

	function transoformChartTableData(chartConf){
		var tableData={};
		tableData.data={};
		tableData.data.labels=[];
		tableData.data.datasets=[];
		var temp= {};
		temp.label='serie';
		temp.data=[];
		for (var i = 0; i < chartConf.data.length; i++) {
			tableData.data.labels.push(chartConf.data[i].label);
			temp.data.push(chartConf.data[i].value);

		}
		tableData.data.datasets.push(temp);

		return tableData;
	}

}



