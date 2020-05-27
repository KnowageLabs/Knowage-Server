
function renderHeatmap(chartConf,handleCockpitSelection,handleCrossNavigationTo, exportWebApp,advanced,chartConfMergeService ) {

    chartConfig = prepareChartConfForHeatmap(chartConf,handleCockpitSelection,handleCrossNavigationTo, exportWebApp);

	chartConfMergeService.addProperty(advanced,chartConf);

    if (exportWebApp){
    	return chartConfig;
    }
    var chart = new Highcharts.Chart(chartConfig);

    return chart;
//    var getCrossParams= function(point){
//    	var params={
//    		point:{
//    			name: null, // category name
//    	        value: null, // category  value
//    	        crossNavigationDocumentName:null,
//    	        crossNavigationDocumentParams:null,
//
//    		series:{ // serie name and value
//    			name:null,
//    			value: null
//    		},
//    		group:{ // grouping category name and value
//    			name:null,
//    			value: null
//    		}
//    		}
//    	};
//
//    	params.point.crossNavigationDocumentName=chartConf.crossNavigation.crossNavigationDocumentName;
//    	params.point.crossNavigationDocumentParams=chartConf.crossNavigation.crossNavigationDocumentParams;
//    	params.point.name=chartConf.additionalData.columns[0].value;
//    	params.point.value= new Date(point.x);
//    	params.point.series.name=chartConf.additionalData.serie.value;
//    	params.point.series.value=point.value;
//    	params.point.group.name=chartConf.additionalData.columns[1].value;
//    	params.point.group.value=point.label;
//
//    	return params;
//    };
}


function getCrossParamsForHeatmap(point,chartConf){
	var params={
    		point:{
    			name: null, // category name
    	        value: null, // category  value
    	        y:null,
    	        crossNavigationDocumentName:null,
    	        crossNavigationDocumentParams:null,

    		series:{ // serie name and value
    			name:null,
    			y: null
    		},
    		group:{ // grouping category name and value
    			name:null,
    			value: null
    		}
    		}
    	};



    	params.point.category=chartConf.additionalData.columns[0].value;
    	if(chartConf.chart.xAxisDate){
    	params.point.name= point.original;
    	}else{
    	params.point.name=	chartConf.additionalData.firstCategory[point.x];
    	}
    	params.point.series.name=chartConf.additionalData.serie.value;
    	params.point.y=point.value;
    	params.point.group.name=chartConf.additionalData.columns[1].value;
    	params.point.group.value=point.label;

    	return params;

}


function getSelectionParammsForHeatmap(point,chartConf){
	var params={
    		point:{
    			name: null, // category name
    	        value: null, // category  value
    	    }
    	};
	params.point.name=point.label;
	params.point.value=point.value;

	return params;
}


function prepareChartConfForHeatmap(chartConf,handleCockpitSelection,handleCrossNavigationTo,exportWebApp) {

	var start;
	 var startDate;
	 var endDate;
    if(chartConf.chart.xAxisDate){
     startDate= new Date(chartConf.additionalData.firstCategory[0]);
     endDate= new Date(chartConf.additionalData.firstCategory[1]);
    }
    var points=[];
    var data=chartConf.data[0];
    if(chartConf.chart.dateTime){
    	for( i=0;i<data.length;i++ ){
    		if(chartConf.chart.datetype=='string'){
    			data[i][chartConf.chart.datecolumn] = Date.parse(data[i][chartConf.chart.datecolumn])
    		}else if(chartConf.chart.datetype=='timestamp'){
    			var dateSplit = data[i][chartConf.chart.datecolumn].replace('/', ":").replace('/', ":").replace(' ', ":").replace('.', ":").split(":");
    			data[i][chartConf.chart.datecolumn] = (new Date(dateSplit[2], dateSplit[1]-1, dateSplit[0], dateSplit[3], dateSplit[4], dateSplit[5], dateSplit[6])).getTime();
    		}else{
    			var dateSplit = data[i][chartConf.chart.datecolumn].replace('/', ":").replace('/', ":").split(":");
    			data[i][chartConf.chart.datecolumn] = (new Date(dateSplit[2], dateSplit[1]-1, dateSplit[0])).getTime();
			}
    	}
	}
    var minValue=data.length >0 ? data[0][chartConf.additionalData.serie.value] : 0;
    var maxValue=data.length >0 ? data[0][chartConf.additionalData.serie.value] :0;

    //ordering yaxis starts
    if(chartConf.additionalData.differentOrdering==true && chartConf.additionalData.storeresultOrder){
    	var map = {};
	   	 for( i=0;i<chartConf.additionalData.storeresult.length;i++ ){
	   		 //orig:order
	   		 map[chartConf.additionalData.storeresult[i]] = chartConf.additionalData.storeresultOrder[i]

	   	 }
	    chartConf.additionalData.storeresultOrder.sort();
		if( chartConf.additionalData.secondColumnOrder.toLowerCase()=="desc")
			chartConf.additionalData.storeresultOrder.reverse();
    } else{
    	chartConf.additionalData.storeresult.sort();
    	if( chartConf.additionalData.secondColumnOrder.toLowerCase()=="desc")
    		chartConf.additionalData.storeresult.reverse();
    }

    // ordering yaxis ends


    for( i=0;i<data.length;i++ ){
    	if(data[i][chartConf.additionalData.serie.value]< minValue){
    		minValue=data[i][chartConf.additionalData.serie.value];
    	}

    	if(data[i][chartConf.additionalData.serie.value] > maxValue){
    		maxValue=data[i][chartConf.additionalData.serie.value];
    	}

    	var xValue;
    	var xValueOriginal;
    	if(chartConf.chart.xAxisDate){
    		xValue=data[i][chartConf.chart.datecolumn];

    		xValueOriginal =data[i][chartConf.additionalData.columns[0].value];
    	}else{
    		xValue=chartConf.additionalData.firstCategory.indexOf(data[i][chartConf.additionalData.columns[0].value]);
    	}
    	var point={
    		"x":xValue,
    		"original":xValueOriginal,
    		//"y":chartConf.additionalData.storeresult.indexOf(data[i][chartConf.additionalData.columns[1].value]),
    		"value":data[i][chartConf.additionalData.serie.value],
    		"label":data[i][chartConf.additionalData.columns[1].value]
    	};

		if(chartConf.additionalData.differentOrdering && chartConf.additionalData.storeresultOrder){
			point.y = chartConf.additionalData.storeresultOrder.indexOf(map[data[i][chartConf.additionalData.columns[1].value]])
		} else {
			point.y = chartConf.additionalData.storeresult.indexOf(data[i][chartConf.additionalData.columns[1].value])
		}

    	points.push(point);
    }

    var colors=chartConf.colors;
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
	        	var stop=[i*(1/(colors.length-1)),colors[i]];
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


    var chartObject = null;

    if (chartConf.chart.height==""
    		|| chartConf.chart.width=="")
	{
    	chartObject =
    	{
        	//renderTo: 'mainPanel',
            type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,

            /**
			 * The zoom in option for HEATMAP chart. User will be able to zoom in on either of those two chart types by both axes ('x' and 'y').
			 * [KNOWAGE-1110 JIRA ISSUE]
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
            zoomType: chartConf.chart.zoomType,

            //margin: [80, 80, 80, 80],

            /**
             * danristo
             */
//            marginTop: 100,
//            marginBottom: 100,
//            marginLeft: 200,
//            marginRight: 200,

			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "",
				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "",
				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : ""

			}
    	};
	}
    else if (chartConf.chart.height!=""
    		&& chartConf.chart.width!="")
	{
    	chartObject =
    	{
        	//renderTo: 'mainPanel',

        	height: chartConf.chart.height ? Number(chartConf.chart.height) : undefined,
			width: chartConf.chart.width ? Number(chartConf.chart.width) : undefined,

			/**
			 * The zoom in option for HEATMAP chart. User will be able to zoom in on either of those two chart types by both axes ('x' and 'y').
			 * [KNOWAGE-1110 JIRA ISSUE]
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			zoomType: chartConf.chart.zoomType,

			type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,
            //margin: [200, 200, 200, 200],

            /**
             * danristo
             */
//            marginTop: 100,
//            marginBottom: 100,
//            marginLeft: 150,
//            marginRight: 150,

			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "none",
				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "none",
				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : "none"

			}
    	};
	}

    var chartHeight = (chartConf.chart.height!="") ? chartConf.chart.height : window.innerHeight;
    var xAxisObject={};
    var serieColSize=0;
    var tooltipObject={};
    var checkDateFormat = function (dateFormat) {
    	var format = "%d-%m-%Y";
    	switch (dateFormat) {
		case "minus":
			format = "%d-%m-%Y";
			break;
		case "slash":
			format = "%d/%m/%Y";
			break;
		case "year":
			format = "%Y";
			break;
		case "month":
			format = "%B %Y";
			break;
		case "day":
			format = "%A, %b %e, %Y";
			break;
		case "hour":
			format = "%A, %b %e, %H";
			break;
		case "minute":
			format = "%A, %b %e, %H:%M";
			break;
		case "second":
			format = "%A, %b %e, %H:%M:%S";
			break;
		default:
			format = "%d-%m-%Y";
		break;
		}
    	return format;
    }

	var prefix = chartConf.additionalData.prefixChar ? chartConf.additionalData.prefixChar : "";
	var postfix = chartConf.additionalData.postfixChar ?  chartConf.additionalData.postfixChar : "";
	var precision = chartConf.additionalData.precision ?  chartConf.additionalData.precision : "";
	var scaleFactor = chartConf.additionalData.scaleFactor ?  chartConf.additionalData.scaleFactor : "empty";

    if(chartConf.chart.xAxisDate){
    	var dateFormat = checkDateFormat(chartConf.chart.dateFormat);
    	xAxisObject={
            type: 'datetime', // the numbers are given in milliseconds
          //  min: Date.UTC(startDate.getUTCFullYear(),startDate.getUTCMonth(),startDate.getUTCDate()),  // gets range from variables
            //max: Date.UTC(endDate.getUTCFullYear(),endDate.getUTCMonth(),endDate.getUTCDate()),

            title:
        	{
            	text: (chartConf.xaxis.title.text!=undefined && chartConf.xaxis.title.text!="") ? chartConf.xaxis.title.text : undefined,
            	align: chartConf.xaxis.title.align,

            	style:
        		{
            		color: (chartConf.xaxis.title.style.color!=undefined && chartConf.xaxis.title.style.color!="" && chartConf.xaxis.title.style.color!="transparent") ? chartConf.xaxis.title.style.color : '',
    				fontStyle: (chartConf.xaxis.title.style.fontStyle!=undefined && chartConf.xaxis.title.style.fontStyle!="") ? chartConf.xaxis.title.style.fontStyle : '',
					textDecoration: (chartConf.xaxis.title.style.textDecoration!=undefined && chartConf.xaxis.title.style.textDecoration!="") ? chartConf.xaxis.title.style.textDecoration : '',
					fontSize: (chartConf.xaxis.title.style.fontSize!=undefined && chartConf.xaxis.title.style.fontSize!="") ? chartConf.xaxis.title.style.fontSize : '',
					fontFamily:(chartConf.xaxis.title.style.fontFamily!=undefined && chartConf.xaxis.title.style.fontFamily!="") ? chartConf.xaxis.title.style.fontFamily : ''
        		}
        	},

            labels: {
                x: 5,
                y: 15,
                formatter: function() {
                    return '' + Highcharts.dateFormat(dateFormat, this.value);
                },
                rotation: (chartConf.xaxis.labels.rotation!=undefined && chartConf.xaxis.labels.rotation!="") ? chartConf.xaxis.labels.rotation : 0,
                align: (chartConf.xaxis.labels.align!=undefined && chartConf.xaxis.labels.align!="") ? chartConf.xaxis.labels.align : undefined,
                style:{
                	color: (chartConf.xaxis.labels.style.color!=undefined && chartConf.xaxis.labels.style.color!="" && chartConf.xaxis.labels.style.color!="transparent") ? chartConf.xaxis.labels.style.color : '',
                    fontStyle:(chartConf.xaxis.labels.style.fontStyle!=undefined && chartConf.xaxis.labels.style.fontStyle!="") ? chartConf.xaxis.labels.style.fontStyle : '',
                    textDecoration: (chartConf.xaxis.labels.style.textDecoration!=undefined && chartConf.xaxis.labels.style.textDecoration!="") ? chartConf.xaxis.labels.style.textDecoration : '',
                    fontSize: (chartConf.xaxis.labels.style.fontSize!=undefined && chartConf.xaxis.labels.style.fontSize!="") ? chartConf.xaxis.labels.style.fontSize : '',
                    fontFamily: (chartConf.xaxis.labels.style.fontFamily!=undefined && chartConf.xaxis.labels.style.fontFamily!="") ? chartConf.xaxis.labels.style.fontFamily : '',
                }
            },
            showLastLabel: true,
            tickLength: 16
        };
    	serieColSize=24 * 36e5;
    	tooltipFormatter= function () {

    		var color = chartConf.tooltip.color ? chartConf.tooltip.color : '';
    		var align = chartConf.tooltip.align ? chartConf.tooltip.align : '';
    		var fontFamily = chartConf.tooltip.fontFamily ? ' ' + chartConf.tooltip.fontFamily : '';
    		var fontSize = chartConf.tooltip.fontSize ? ' ' + chartConf.tooltip.fontSize : '';
    		var fontWeight = chartConf.tooltip.fontWeight ? ' ' + chartConf.tooltip.fontWeight : '';
    		var tooltipFontStyle = "";

    		if (fontWeight == " underline")
    		{
    			tooltipFontStyle = " text-decoration: underline;";
    		}
    		else if (fontWeight == " italic")
    		{
    			tooltipFontStyle = "font-style: italic;";
    		}
    		else if (fontWeight == " bold")
    		{
    			tooltipFontStyle = "font-weight: bold;";
    		}
    		else
    		{
    			tooltipFontStyle = "font-weight: normal;";
    		}


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


			var pointDate = Highcharts.dateFormat(dateFormat, this.point.x)
			var result = "";
            result +=
    			'<div style="padding:10px;color:' + color + '; opacity: 0.9; font-family: ' + fontFamily + '; '
    				+ tooltipFontStyle + " font-size: " + fontSize + ';text-align:' + align + ';">';

            result+= '<b>'+chartConf.additionalData.serie.value+'</b><br>' + pointDate + '| ' + this.series.yAxis.categories[this.point.y] + ': <b>' +
            prefix + " " +newValue + " " + postfix + ' </b> ';

            return result;
    	};
    	tooltipObject={
    		formatter:tooltipFormatter,
    		useHTML: true,
        	borderWidth: chartConf.tooltip.borderWidth,
        	borderRadius: chartConf.tooltip.borderRadius,
	    	backgroundColor: chartConf.tooltip.backgroundColor ? chartConf.tooltip.backgroundColor: "",

    	};

    } else {
    	xAxisObject={
			type: 'category',
			categories:chartConf.additionalData.firstCategory,
            title:
        	{
            	text: (chartConf.xaxis.title.text!=undefined && chartConf.xaxis.title.text!="") ? chartConf.xaxis.title.text : undefined,
            	align: chartConf.xaxis.title.align,

            	style:
        		{
            		color: (chartConf.xaxis.title.style.color!=undefined && chartConf.xaxis.title.style.color!="" && chartConf.xaxis.title.style.color!="transparent") ? chartConf.xaxis.title.style.color : '',
    				fontStyle: (chartConf.xaxis.title.style.fontStyle!=undefined && chartConf.xaxis.title.style.fontStyle!="") ? chartConf.xaxis.title.style.fontStyle : '',
					textDecoration: (chartConf.xaxis.title.style.textDecoration!=undefined && chartConf.xaxis.title.style.textDecoration!="") ? chartConf.xaxis.title.style.textDecoration : '',
					fontSize: (chartConf.xaxis.title.style.fontSize!=undefined && chartConf.xaxis.title.style.fontSize!="") ? chartConf.xaxis.title.style.fontSize : '',
					fontFamily:(chartConf.xaxis.title.style.fontFamily!=undefined && chartConf.xaxis.title.style.fontFamily!="") ? chartConf.xaxis.title.style.fontFamily : ''
        		}
        	},

            labels: {

              // x: 5,
              // y: 15,
                rotation: (chartConf.xaxis.labels.rotation!=undefined && chartConf.xaxis.labels.rotation!="") ? chartConf.xaxis.labels.rotation : 0,
                align: (chartConf.xaxis.labels.align!=undefined && chartConf.xaxis.labels.align!="") ? chartConf.xaxis.labels.align : undefined,
                style:{
                	color: (chartConf.xaxis.labels.style.color!=undefined && chartConf.xaxis.labels.style.color!="" && chartConf.xaxis.labels.style.color!="transparent") ? chartConf.xaxis.labels.style.color : '',
                    fontStyle:(chartConf.xaxis.labels.style.fontStyle!=undefined && chartConf.xaxis.labels.style.fontStyle!="") ? chartConf.xaxis.labels.style.fontStyle : '',
                    textDecoration: (chartConf.xaxis.labels.style.textDecoration!=undefined && chartConf.xaxis.labels.style.textDecoration!="") ? chartConf.xaxis.labels.style.textDecoration : '',
                    fontSize: (chartConf.xaxis.labels.style.fontSize!=undefined && chartConf.xaxis.labels.style.fontSize!="") ? chartConf.xaxis.labels.style.fontSize : '',
                    fontFamily: (chartConf.xaxis.labels.style.fontFamily!=undefined && chartConf.xaxis.labels.style.fontFamily!="") ? chartConf.xaxis.labels.style.fontFamily : '',
           	    }
            },

             showLastLabel: true,
//         	tickInterval:1,
             tickLength: 16
    	};
    	serieColSize=1;
    	tooltipFormatter= function () {

    		var color = chartConf.tooltip.color ? chartConf.tooltip.color : '';
    		var align = chartConf.tooltip.align ? chartConf.tooltip.align : '';
    		var fontFamily = chartConf.tooltip.fontFamily ? ' ' + chartConf.tooltip.fontFamily : '';
    		var fontSize = chartConf.tooltip.fontSize ? ' ' + chartConf.tooltip.fontSize : '';
    		var fontWeight = chartConf.tooltip.fontWeight ? ' ' + chartConf.tooltip.fontWeight : '';
    		var tooltipFontStyle = "";

    		if (fontWeight == " underline")
    		{
    			tooltipFontStyle = " text-decoration: underline;";
    		}
    		else if (fontWeight == " italic")
    		{
    			tooltipFontStyle = "font-style: italic;";
    		}
    		else if (fontWeight == " bold")
    		{
    			tooltipFontStyle = "font-weight: bold;";
    		}
    		else
    		{
    			tooltipFontStyle = "font-weight: normal;";
    		}


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
    			'<div style="padding:10px;color:' + color + '; opacity: 0.9; font-family: ' + fontFamily + '; '
    				+ tooltipFontStyle + " font-size: " + fontSize + ';text-align:' + align + ';">';

            result += '<b>'+chartConf.additionalData.serie.value+'</b><br>' + this.series.xAxis.categories[this.point.x] + ' | ' + this.series.yAxis.categories[this.point.y] + ': <b>' +
            	prefix + " " +newValue + " " + postfix + ' </b></div>';


    		return result;

    	};

    	tooltipObject={
    		formatter:tooltipFormatter,
    		useHTML: true,
        	borderWidth: chartConf.tooltip.borderWidth,
        	borderRadius: chartConf.tooltip.borderRadius,
	    	backgroundColor: chartConf.tooltip.backgroundColor ? chartConf.tooltip.backgroundColor: "",

    	};

    }

    var toReturn = {

    	chart: chartObject,

        title: {
			text: chartConf.title.text,
            align: chartConf.title.style.textAlign,
			style: {
                color: chartConf.title.style.fontColor,
                fontSize: chartConf.title.style.fontSize,
                fontFamily: chartConf.title.style.fontFamily,
                fontStyle: chartConf.title.style.fontStyle ? chartConf.title.style.fontStyle : "none",
				textDecoration: chartConf.title.style.textDecoration ? chartConf.title.style.textDecoration : "none",
				fontWeight: chartConf.title.style.fontWeight ? chartConf.title.style.fontWeight : "none"
            }
		},
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

        xAxis: xAxisObject,

        yAxis:
        {
        	title:
        	{
        		text: (chartConf.yaxis.title.text!=undefined && chartConf.yaxis.title.text!="") ? chartConf.yaxis.title.text : undefined,
            	align:(chartConf.yaxis.title.align!=undefined && chartConf.yaxis.title.align!="")?chartConf.yaxis.title.align:undefined,

            	/**
            	 * Fixed value for margin of the Y-axis title. If the alignment of labels of the Y-axis
            	 * is "right", then take the value of 40 (default one, provided by the Highcharts library
            	 * for this property.
            	 *
            	 * @author: danristo (danilo.ristovski@mht.net)
            	 */
            	margin: (chartConf.yaxis.labels.align!=undefined && chartConf.yaxis.labels.align!="" && chartConf.yaxis.labels.align!="right") ? 60 : 40,

    			style:
        		{
            		color: (chartConf.yaxis.title.style.color!=undefined && chartConf.yaxis.title.style.color!="" && chartConf.yaxis.title.style.color!="transparent" ) ? chartConf.yaxis.title.style.color : '',
    				fontStyle: (chartConf.yaxis.title.style.fontStyle!=undefined && chartConf.yaxis.title.style.fontStyle!="") ? chartConf.yaxis.title.style.fontStyle : '',
					textDecoration: (chartConf.yaxis.title.style.textDecoration!=undefined && chartConf.yaxis.title.style.textDecoration!="") ? chartConf.yaxis.title.style.textDecoration : '',
					fontSize: (chartConf.yaxis.title.style.fontSize!=undefined && chartConf.yaxis.title.style.fontSize!="") ? chartConf.yaxis.title.style.fontSize : '',
					fontFamily:(chartConf.yaxis.title.style.fontFamily!=undefined && chartConf.yaxis.title.style.fontFamily!="") ? chartConf.yaxis.title.style.fontFamily : ''
        		}
        	},
            labels:{
            	rotation: (chartConf.yaxis.labels.rotation!=undefined && chartConf.yaxis.labels.rotation!="") ? chartConf.yaxis.labels.rotation : 0,
                align: (chartConf.yaxis.labels.align!=undefined && chartConf.yaxis.labels.align!="") ? chartConf.yaxis.labels.align : '',

        		/**
        		 * Provide the perfect left alignment when this one is selected (picked) by the user
        		 * for the labels alignment.
        		 *
        		 * @author: danristo (danilo.ristovski@mht.net)
        		 */
                /**
                 * makes padding when the alignment is right
                 */
        		x:-10,

            	style:{
            		 color: (chartConf.yaxis.labels.style.color!=undefined && chartConf.yaxis.labels.style.color!="" && chartConf.yaxis.labels.style.color!="transparent" ) ? chartConf.yaxis.labels.style.color : undefined,
                     fontStyle:(chartConf.yaxis.labels.style.fontStyle!=undefined && chartConf.yaxis.labels.style.fontStyle!="") ? chartConf.yaxis.labels.style.fontStyle : '',
                     textDecoration: (chartConf.yaxis.labels.style.textDecoration!=undefined && chartConf.yaxis.labels.style.textDecoration!="") ? chartConf.yaxis.labels.style.textDecoration : '',
                     fontSize: (chartConf.yaxis.labels.style.fontSize!=undefined && chartConf.yaxis.labels.style.fontSize!="") ? chartConf.yaxis.labels.style.fontSize : "",
                     fontFamily: (chartConf.yaxis.labels.style.fontFamily!=undefined && chartConf.yaxis.labels.style.fontFamily!="") ? chartConf.yaxis.labels.style.fontFamily : "",
            	}
            },
            categories:chartConf.additionalData.storeresult,
            reversed: false
        },

        /**
         * Vertical legend of the HEATMAP will be positioned on the right side of the chart
         * always (fixed values). Dynamic values are ones that user specifies for the height
         * of the legend and its position relative to the vertical orientation (top, middle,
         * bottom).
         * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
         */
        legend:
        {	enabled: chartConf.legend.enabled,
            align: 'right',
            layout: 'vertical',
            verticalAlign: chartConf.legend.style.align,
            //y: (Number(chartHeight)-Number(chartConf.legend.symbolHeight))/2,
            symbolHeight: Number(chartConf.legend.symbolHeight),

            /**
             * Title for the HEATMAP legend (KNOWAGE-835 JIRA issue).
             * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
             */
            title: {

            	text: chartConf.legend.title.text,

            	style: {

            		color: chartConf.legend.title.style.color,
            		fontFamily: chartConf.legend.title.style.fontFamily,
            		fontSize: chartConf.legend.title.style.fontSize,
            		fontWeight: chartConf.legend.title.style.fontWeight

            	}

            }
        },

        tooltip: tooltipObject,
        series: [{
            borderWidth: 0,
            nullColor: '#EFEFEF',
            colsize: serieColSize,
            data:points,
            events: {
            click: function(event){
            	if(!exportWebApp){
                	if(chartConf.chart.isCockpit==true){
                		if(chartConf.chart.outcomingEventsEnabled){
                		var selectParams = getCrossParamsForHeatmap(event.point,chartConf);
                		handleCockpitSelection(selectParams);
                		}
                	}else{


                		var params=getCrossParamsForHeatmap(event.point,chartConf);
                	    handleCrossNavigationTo(params);

                	}
            	}

            }
            },
            turboThreshold: Number.MAX_VALUE// #3404, remove after 4.0.5 release
        }],


        /**
		 * Credits option disabled/enabled for the HEATMAP chart. This option (boolean value)
		 * is defined inside of the VM for the HEATMAP chart. If enabled credits link appears
		 * in the right bottom part of the chart.
		 * @author: danristo (danilo.ristovski@mht.net)
		 */
		credits:
        {
    		enabled: (chartConf.credits.enabled!=undefined) ? chartConf.credits.enabled : false
		}
    };

    /**
     * If there are no colors set in the color palette for the HEATMAP
     * chart, exclude 'colorAxis' property from the chart configuration
     * because we do not have a color that will server as an end color
     * of the color interval (there are no colors available within the
     * template).
     *
     * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
     */
    if (colors.length!=undefined)
	{
    	toReturn['colorAxis'] =
    	{
			stops:colorStops ,
            min: minValue,
            max: maxValue,
		    startOnTick: false,
		    endOnTick: false,
            labels:
            {
               format: '{value}'
           }
       };
	}


    if(chartConf.additionalData.differentOrdering && chartConf.additionalData.storeresultOrder){
    	var categories = []
    	for(i=0;i<chartConf.additionalData.storeresultOrder.length;i++){
    		for (key in map){
        		if(map[key]==chartConf.additionalData.storeresultOrder[i]) categories.push(key)
        	}
        }

		toReturn.yAxis.categories = categories
	}

    return toReturn;
}