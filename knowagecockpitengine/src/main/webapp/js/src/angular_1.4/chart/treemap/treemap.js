function renderTreemap(chartConf,handleCockpitSelection, handleCrossNavigationTo, exportWebApp,advanced,chartConfMergeService ) {
	 chartConf = prepareChartConfForTreemap(chartConf,handleCockpitSelection,handleCrossNavigationTo, exportWebApp);
	 chartConfMergeService.addProperty(advanced,chartConf);

    /**
     * Text that will be displayed inside the Back (drillup) button
     * that appears whenever we enter deeper levels of the TREEMAP
     * chart, i.e. whenever we drilldown through categories for
     * the serie user specified. This way we will keep record of the
     * current drill down level.
     *
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    (
		function (H)
		{
			H.wrap
			(
				H.seriesTypes.treemap.prototype,
				'showDrillUpButton',

				function (proceed)
				{
					arguments[1] = 'Back to: <b>' + this.nodeMap[this.rootNode].name + '</b>';
					proceed.apply(this, [].slice.call(arguments, 1));
				}
			);
		}(Highcharts)
	);
    if (exportWebApp){
    	return chartConf;
    }
	var chart = new Highcharts.Chart(chartConf);

	return chart;

//	var getCrossParams= function(point){
//		var params={
//				point:{
//					name: null, // category name
//					value: null, // category  value
//					crossNavigationDocumentName:null,
//					crossNavigationDocumentParams:null,
//
//					series:{ // serie name and value
//						name:null,
//						value: null
//					},
//					group:{ // grouping category name and value
//						name:null,
//						value: null
//					}
//				}
//		};
//
//		params.point.crossNavigationDocumentName=chartConf.crossNavigation.crossNavigationDocumentName;
//		params.point.crossNavigationDocumentParams=chartConf.crossNavigation.crossNavigationDocumentParams;
//
//		params.point.value=point.name;
//
//		params.point.series.value=point.value;
//
//
//		return params;
//	}
}

function getCrossParamsForTreemap(point,chartConf){
	var params={
			point:{
				name: null, // category value
				category: null, // category  value


				series:{ // serie name and value
					name:null,
					value: null
				},
				group:{ // grouping category name and value
					name:null,
					value: null
				}
			}
	};


	params.point.name=point.name;

	params.point.y=point.value;


	return params;


}

function prepareChartConfForTreemap(chartConf,handleCockpitSelection,handleCrossNavigationTo, exportWebApp) {

	//console.log(chartConf)

	var colors = [];

	if (chartConf.colors.length == Object.keys(chartConf.data[0]).length) {
		colors = chartConf.colors;
	} else if (chartConf.colors.length > Object.keys(chartConf.data[0]).length) {
		chartConf.colors.length = Object.keys(chartConf.data[0]).length;
		colors = chartConf.colors;
	} else {
		colors = chartConf.colors;
		for (var i = 0; i < Highcharts.getOptions().colors.length; i++) {
			colors.push(Highcharts.getOptions().colors[i])
		}
		colors.splice(Object.keys(chartConf.data[0]).length, colors.length)
	}

    var colorStops=[];

    /**
     * Provide the ending color for the color interval of the HEATMAP
     * if there is one for that. Otherwise, skip this snippet.
     *
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    if (colors.length)
	{
    	 /**
    	  * Check if user specified only 1 color from the color palette.
    	  * @modifiedBy Danilo Ristovski (danristo, danilo.ristovski@mht.net)
    	  */
		 if (colors.length > 1)
		 {
	    	for(i=0;i<colors.length;i++){
	        	var stop=[(i+1)*(1/(colors.length)),colors[i]];
	        	colorStops.push(stop);
	        }
		 }
		 else
		 {
	    	/**
	    	 * If user specified only one color from the color palette in order to specify the
	    	 * color interval for this chart type, then the interval of colors goes from the
	    	 * white color ("#FFFFFF") (the most left color on the legend of the chart) to the
	    	 * one specified by the user (that single one, 'colors[0]').
	    	 *
	    	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	    	 */
	    	var startIntervalColor = "#FFFFFF";	// White color

	    	colorStops.push([0,startIntervalColor]);
	    	colorStops.push([1,colors[0]]);
		 }
	}

    if(colorStops.length >= 2 && colorStops[1][0] != undefined && colorStops[0][0] != undefined){

        distance = colorStops[1][0] - colorStops[0][0];
    }

    modifiedStops = [];

    for (var i = 0; i < colorStops.length; i++) {
    	modifiedStops.push([colorStops[i][0] - distance - 0.001, '#ffffff']);
        modifiedStops.push(colorStops[i]);
        modifiedStops.push([colorStops[i][0] + 0.001, colorStops[i][1]]);
	}

	var points = [];

	var counter=0;
	precision = chartConf.additionalData.precision ?  chartConf.additionalData.precision : "";
	scaleFactor = chartConf.additionalData.scaleFactor ?  chartConf.additionalData.scaleFactor : "empty";

	for (var dataset in chartConf.data[0]){
		level = {
				id: "id_" + counter,
				name: dataset,
				parentName:dataset,
		}
		;
		points.push(level);
		func(chartConf.data[0][dataset],dataset, level, dataset,counter);
		counter++
	}



	function func(resultData, nameds, dataValue, dataset,parentNum){
		var counter=0;
		for (var resultRecord in resultData){
			level = {
					id: dataValue.id + "_" + counter,
					name: resultRecord,
					parent: dataValue.id,
					parentName:dataset,
					scale:parentNum*10
			}

			if (resultData[resultRecord].value){
				if(precision==''){
					if(typeof resultData[resultRecord].value == "string"){
						level.value = Number(level.value = resultData[resultRecord].value);
					} else {
						level.value = resultData[resultRecord].value;
					}
				} else {
					level.value = Number(Number(resultData[resultRecord].value).toFixed(precision));
				}
				points.push(level);
			}
			else{
				points.push(level);
				func(resultData[resultRecord], resultRecord, level, dataset,parentNum);
			}
			counter++;
		}
	}

	var scale = 0;
	var tickPositions1 = [];
	var tickPositions = [];
	tickPositions.push(0);
	var scaleObject = {};

	for (var i = 0; i < points.length; i++) {
		if(scaleObject.hasOwnProperty(points[i].parentName)){
			scaleObject[(points[i].parentName)].push(points[i]);
		} else {
			scaleObject[(points[i].parentName)] = []
			scaleObject[(points[i].parentName)].push(points[i])
		}
	}

	for (property in scaleObject) {
		var colorvalue = [];

		for (var i = 0; i < scaleObject[property].length; i++) {
			if(scaleObject[property][i].hasOwnProperty("value")){
				colorvalue.push(scaleObject[property][i]);
				var randomNum = Math.floor(Math.random() * 9 );
			scaleObject[property][i].colorValue = randomNum!=9? randomNum+Math.random(): randomNum;
			}
		}
	}

	var chartObject = null;

	if (chartConf.chart.height=="" || chartConf.chart.width=="") {
		chartObject = {
			//zoomType: 'xy', // Causes problems when zooming out (Zoom reset) (danristo)
			marginTop: chartConf.chart.marginTop ? chartConf.chart.marginTop : undefined,

			/**
			 * Leave enough space for the "Back" button for drill up.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			marginBottom: chartConf.chart.marginBottom ? chartConf.chart.marginBottom : undefined,

			style: {
				fontFamily: chartConf.chart.style.fontFamily,
				fontSize: chartConf.chart.style.fontSize,
				fontWeight: chartConf.chart.style.fontWeight,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "",
						textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "",
								fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : ""
			}
		};

		if (chartConf.chart.backgroundColor!=undefined && chartConf.chart.backgroundColor!="")
			chartObject.backgroundColor = chartConf.chart.backgroundColor;
	}
	else if (chartConf.chart.height!="" && chartConf.chart.width!="") {
		chartObject = {
			//zoomType: 'xy', // Causes problems when zooming out (Zoom reset) (danristo)
			marginTop: chartConf.chart.marginTop ? chartConf.chart.marginTop : undefined,

			/**
			 * Leave enough space for the "Back" button for drill up.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			marginBottom: chartConf.chart.marginBottom ? chartConf.chart.marginBottom : undefined,

					style: {
						fontFamily: chartConf.chart.style.fontFamily,
						fontSize: chartConf.chart.style.fontSize,
						fontWeight: chartConf.chart.style.fontWeight,
						fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "",
								textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "",
										fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : ""
					}
		};
		if(!exportWebApp){
			chartObject.height = chartConf.chart.height ? Number(chartConf.chart.height) : undefined;
			chartObject.width = chartConf.chart.width ? Number(chartConf.chart.width) : undefined;
		}
		if (chartConf.chart.backgroundColor!=undefined && chartConf.chart.backgroundColor!="")
			chartObject.backgroundColor = chartConf.chart.backgroundColor;
	}
	var tooltipObject={};
	prefix = chartConf.additionalData.prefixChar ? chartConf.additionalData.prefixChar : "";
	postfix = chartConf.additionalData.postfixChar ?  chartConf.additionalData.postfixChar : "";

   	tooltipFormatter= function () {

   		var ttColor = chartConf.additionalData.tooltip.ttColor ? chartConf.additionalData.tooltip.ttColor : '';

		var ttAlign = chartConf.additionalData.tooltip.ttAlign ? chartConf.additionalData.tooltip.ttAlign : '';
		var ttFont = chartConf.additionalData.tooltip.ttFont ? ' ' + chartConf.additionalData.tooltip.ttFont : '';
		var ttFontSize = chartConf.additionalData.tooltip.ttFontSize ? ' ' + chartConf.additionalData.tooltip.ttFontSize : '';
		var ttFontWeight = chartConf.additionalData.tooltip.ttFontWeight ? ' ' + chartConf.additionalData.tooltip.ttFontWeight : '';
		var tooltipFontStyle = "";

		if (ttFontWeight == " underline")
		{
			tooltipFontStyle = " text-decoration: underline;";
		}
		else if (ttFontWeight == " italic")
		{
			tooltipFontStyle = "font-style: italic;";
		}
		else if (ttFontWeight == " bold")
		{
			tooltipFontStyle = "font-weight: bold;";
		}
		else
		{
			tooltipFontStyle = "font-weight: normal;";
		}


   		var point = this.point,

   		group = this.series.data.filter(function (x) {
      	  return x.parent === point.parent;
        });

   		var groupTotal = group.map(function (x) {
			  return x.value;
		}).reduce(function (a, b) {
		  return a + b;
		}, 0);

        percentage = 100 * point.value/groupTotal;
        var decimalPoints = Highcharts.getOptions().lang.decimalPoint;
      	var thousandsSep = Highcharts.getOptions().lang.thousandsSep;
        var value = this.point.value;
        var newValue = "";
    	switch(scaleFactor.toUpperCase()) {

	  		case "EMPTY":
	  			/* No selection is provided for the number to be displayed as the data label (pure value). */
	  			newValue += Highcharts.numberFormat(value,precision,decimalPoints,thousandsSep);
	  			break;
	  		case "K":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,3),precision,decimalPoints,thousandsSep) + "k";
	  			break;
	  		case "M":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,6),precision,decimalPoints,thousandsSep) + "M";
	  			break;
	  		case "G":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,9),precision,decimalPoints,thousandsSep) + "G";
	  			break;
				case "T":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,12),precision,decimalPoints,thousandsSep) + "T";
	  			break;
	  		case "P":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,15),precision,decimalPoints,thousandsSep) + "P";
	  			break;
			case "E":
	  			newValue += Highcharts.numberFormat(value/Math.pow(10,18),precision,decimalPoints,thousandsSep) + "E";
	  			break;
			default:
					/* The same as for the case when user picked "no selection" - in case when the chart
					template does not contain the scale factor for current serie */
					newValue += Highcharts.numberFormat(value,precision,decimalPoints,thousandsSep);
	  		break;

    	}
        var result = "";
        result +=
			'<div style="padding:10px;color:' + ttColor + '; opacity: 0.9; font-family: ' + ttFont + '; '
				+ tooltipFontStyle + " font-size: " + ttFontSize + ';text-align:' + ttAlign + ';">';

		if(!chartConf.additionalData.showAbsValue && !chartConf.additionalData.showPercentage){
			result += '<span>' + point.name + '</span><br/></div>';
		} else if (!chartConf.additionalData.showAbsValue && chartConf.additionalData.showPercentage){
			result += '<span>' + point.name + '</span><br/>'+ percentage.toFixed(precision) + '%</div>';
		} else if (chartConf.additionalData.showAbsValue && !chartConf.additionalData.showPercentage ){
			result += '<span>' + point.name + '</span><br/>'+ newValue +" "+ postfix + '</div>';
		} else if(chartConf.additionalData.showAbsValue && chartConf.additionalData.showPercentage){
			result += '<span>' + point.name + '</span><br/>'+ percentage.toFixed(precision) + '%' + '<br>'+prefix +" "+newValue +" "+ postfix +'</div>';
		}
		return result;
	};

	tooltipObject={
    	borderWidth: chartConf.tooltip.borderWidth,
    	borderRadius: chartConf.tooltip.borderRadius,
    	useHTML: true,
    	backgroundColor: chartConf.additionalData.tooltip.ttBackColor,
		formatter:tooltipFormatter,

	};
	/**
	 * Take drill up button (the "Back" button) setting from the VM.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
    var drillUpButtonSettings = chartConf.series[0].drillUpButton;
    if(points.length > chartConf.plotOptions.series.turboThreshold){
		chartConf.emptymessage.text = "Your dataset is returning too much data"
	}
	return 	{
		chart: chartObject,
		colorAxis: {

			labels: { enabled: false },
			tickLength: 0,
			gridLineWidth:0,

			min: 0 ,
			max: colors.length * 10,
			stops: modifiedStops,
		},
		legend:{
			enabled: true,
			itemStyle: {
	            color: '#FFF',

	        }
		},
		tooltip: tooltipObject,
		series:
		[
         	{
         		/**
         		 * Customization of the "Back" button on the TREEMAP chart.
         		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
         		 */
				drillUpButton:
				{
	                position:
	                {
	                    align: drillUpButtonSettings.position.align,
//	                    x: drillUpButtonSettings.position.x,
	                    verticalAlign: drillUpButtonSettings.position.verticalAlign,
//	                    y: drillUpButtonSettings.position.y
	                },

	                theme:
	                {
	                    fill: drillUpButtonSettings.theme.fill,
	                    'stroke-width': drillUpButtonSettings.theme.strokeWidth,
	                    stroke: drillUpButtonSettings.theme.stroke,
	                    r: drillUpButtonSettings.theme.r,

	                    style:
	                    {
	                    	fontSize: drillUpButtonSettings.theme.style.fontSize
                    	},

	                    states:
	                    {
	                        hover:
	                        {
	                            //fill: drillUpButtonSettings.theme.states.hover.fill
	                        }
	                    }
	                }
	            },

			type: "treemap",
			layoutAlgorithm: 'squarified',
			allowDrillToNode: true,
			dataLabels: {
				enabled: false,

			},
			levelIsConstant: false,
			levels: [{
				level: 1,
				dataLabels: {
						style: {
                        	fontSize: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : ""
                        },
					
					enabled: true,
					formatter: function() {

						var point = this.point

						group = this.series.data.filter(function (x) {
	                    	  return x.parent === point.parent;
	                    });

						var decimalPoints = Highcharts.getOptions().lang.decimalPoint;
			          	var thousandsSep = Highcharts.getOptions().lang.thousandsSep;
			            var value = this.point.value;
			            var newValue = "";
			        	switch(scaleFactor.toUpperCase()) {

			    	  		case "EMPTY":
			    	  			/* No selection is provided for the number to be displayed as the data label (pure value). */
			    	  			newValue += Highcharts.numberFormat(value,precision,decimalPoints,thousandsSep);
			    	  			break;
			    	  		case "K":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,3),precision,decimalPoints,thousandsSep) + "k";
			    	  			break;
			    	  		case "M":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,6),precision,decimalPoints,thousandsSep) + "M";
			    	  			break;
			    	  		case "G":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,9),precision,decimalPoints,thousandsSep) + "G";
			    	  			break;
			    				case "T":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,12),precision,decimalPoints,thousandsSep) + "T";
			    	  			break;
			    	  		case "P":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,15),precision,decimalPoints,thousandsSep) + "P";
			    	  			break;
			    			case "E":
			    	  			newValue += Highcharts.numberFormat(value/Math.pow(10,18),precision,decimalPoints,thousandsSep) + "E";
			    	  			break;
			    			default:
			    					/* The same as for the case when user picked "no selection" - in case when the chart
			    					template does not contain the scale factor for current serie */
			    					newValue += Highcharts.numberFormat(value,precision,decimalPoints,thousandsSep);
			    	  		break;

			        	}

						var groupTotal = group.map(function (x) {
							  return x.value;
						}).reduce(function (a, b) {
						  return a + b;
						}, 0);

	                    percentage = 100 * point.value/groupTotal;

						if(!chartConf.additionalData.showAbsValue && !chartConf.additionalData.showPercentage){
							return point.name + '<br>';
						} else if (!chartConf.additionalData.showAbsValue && chartConf.additionalData.showPercentage){
							return point.name + '<br>' + percentage.toFixed(precision) + '%';

						} else if (chartConf.additionalData.showAbsValue && !chartConf.additionalData.showPercentage ){
							return point.name + '<br>'+ prefix +" "+  newValue +" "+ postfix;

						} else if(chartConf.additionalData.showAbsValue && chartConf.additionalData.showPercentage){
							return point.name + '<br>' + percentage.toFixed(precision) + '%' + '<br>'+prefix +" "+  newValue +" "+ postfix;
						}

						/*	for(var i = 0 ; i < this.point.node.children.length;i++){
							this.point.node.children[i].parentValue = this.point.value;
						}

						var val = this.point.value.toFixed(precision);

						if(this.point.name == this.point.parentName){
							val = (this.point.value / ukupnaVrednost) * 100;
						}
						else {
							val = (this.point.value / this.point.parentValue) * 100;
						}
						 this.point.node.children[]
						 ovde se stavlja funkcija

				        return this.point.name +  ': <b>' +

				        prefix + " " +val + " " + postfix;*/

					}
				},
				borderWidth: 6,
				borderColor: "#FFFFFF",
			}],
			data: points.map(function (point) {
                if (point.colorValue ) {
                	point.colorValue += point.scale
                }

                return point;
            }),
			events:{

				click: function(event){
					if(!exportWebApp){
						if(chartConf.chart.isCockpit){
					        if(this.chart.cliccable != undefined ? this.chart.cliccable : true && chartConf.chart.cliccable){
					        	handleCockpitSelection(event);
					        }
						 } else if(event.point.node.children.length==0){
				            	var params=getCrossParamsForTreemap(event.point,chartConf);
				            	handleCrossNavigationTo(params);
						 }
					}
				}
			}
		}],
		subtitle: {
			text: chartConf.subtitle.text,
			align: chartConf.subtitle.style.textAlign,
			style: {
				color: chartConf.subtitle.style.fontColor,
				fontSize: chartConf.subtitle.style.fontSize,
				fontFamily: chartConf.subtitle.style.fontFamily,
				fontStyle: chartConf.subtitle.style.fontStyle ? chartConf.subtitle.style.fontStyle : "none",
				textDecoration: chartConf.subtitle.style.textDecoration ? chartConf.subtitle.style.textDecoration : "none",
				fontWeight: chartConf.subtitle.style.fontWeight ? chartConf.subtitle.style.fontWeight : "none"
			}
		},
		title: {
			text: chartConf.title.text,
			align: chartConf.title.style.textAlign,
			style: {
				color: chartConf.title.style.fontColor,
				fontWeight: chartConf.title.style.fontWeight,
				fontSize: chartConf.title.style.fontSize,
				fontFamily: chartConf.title.style.fontFamily,
				fontStyle: chartConf.title.style.fontStyle ? chartConf.title.style.fontStyle : "none",
				textDecoration: chartConf.title.style.textDecoration ? chartConf.title.style.textDecoration : "none",
				fontWeight: chartConf.title.style.fontWeight ? chartConf.title.style.fontWeight : "none"
			}
		},
		lang: {
			noData : chartConf.emptymessage.text
		},
		noData: {
			style: {
                color: chartConf.emptymessage.style.fontColor,
                fontSize: chartConf.emptymessage.style.fontSize,
                fontFamily: chartConf.emptymessage.style.fontFamily,
                fontStyle: chartConf.emptymessage.style.fontStyle ? chartConf.emptymessage.style.fontStyle : "none",
				textDecoration: chartConf.emptymessage.style.textDecoration ? chartConf.emptymessage.style.textDecoration : "none",
				fontWeight: chartConf.emptymessage.style.fontWeight ? chartConf.emptymessage.style.fontWeight : "none"
            },
            position: {
            	align:  chartConf.emptymessage.style.textAlign,
    			verticalAlign: 'middle'
            }
		},


		/**
		 * Credits option disabled/enabled for the TREEMAP chart. This option (boolean value)
		 * is defined inside of the VM for the TREEMAP chart. If enabled credits link appears
		 * in the right bottom part of the chart.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		credits:
        {
    		enabled: (chartConf.credits.enabled!=undefined) ? chartConf.credits.enabled : false
		},

		plotOptions:
		{
			series:
				{
					turboThreshold: chartConf.plotOptions.series.turboThreshold,
					colorByPoint: chartConf.plotOptions.series.colorByPoint
				}
		}
	};

}
