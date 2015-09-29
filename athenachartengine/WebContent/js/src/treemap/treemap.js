function renderTreemap(chartConf) {

	var points = [];

	var counter=0;
	
	for (var dataset in chartConf.data[0]){

		level = {
				id: "id_" + counter,
				name: dataset,
				color: chartConf.colors[counter]
				
		}
		counter++;
		points.push(level);
		func(chartConf.data[0][dataset],dataset, level);

	}

	function func(resultData, nameds, dataValue){
		var counter=0;
		for (var resultRecord in resultData){
			
			level = {

					id: dataValue.id + "_" + counter,
					name: resultRecord,
					parent: dataValue.id
			
			}
			
			if (resultData[resultRecord].value){
				
				level.value = Math.round(Number(resultData[resultRecord].value));
				points.push(level);
			}
			else{
				
				points.push(level);
				func(resultData[resultRecord], resultRecord, level);
				
			}

			counter++;
		}

	}
	
	var chartObject = null;
    
    if (chartConf.chart.height==""
    		|| chartConf.chart.width=="")
	{
    	chartObject = 
    	{
    			renderTo: 'mainPanel',
//    			height: (chartConf.chart.height!=undefined || chartConf.chart.height!="") ? chartConf.chart.height : "",
//    			width: (chartConf.chart.width!=undefined || chartConf.chart.width!="") ? chartConf.chart.width : "",
    			style: {
    				fontFamily: chartConf.chart.style.fontFamily,
    				fontSize: chartConf.chart.style.fontSize,
    				fontWeight: chartConf.chart.style.fontWeight,
    				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "none",
    				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "none",
    				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : "none"
    			}
    		};
	}
    else if (chartConf.chart.height!=""
    		&& chartConf.chart.width!="")
	{
    	chartObject =     	
    	{
			renderTo: 'mainPanel',
			height:  Number(chartConf.chart.height),
			width:  Number(chartConf.chart.width),
			style: {
				fontFamily: chartConf.chart.style.fontFamily,
				fontSize: chartConf.chart.style.fontSize,
				fontWeight: chartConf.chart.style.fontWeight,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "none",
				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "none",
				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : "none"
			}
		};
	}

	var chart = new Highcharts.Chart({
		chart: chartObject,
		series: [{
			type: "treemap",
			layoutAlgorithm: 'squarified',
			allowDrillToNode: true,
			dataLabels: {
				enabled: false
			},
			levelIsConstant: false,
			levels: [{
				level: 1,
				dataLabels: {
					enabled: true
				},
				borderWidth: 3
			}],
			data: points
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
		noData: {
			text: chartConf.emptymessage.text,
			align: chartConf.emptymessage.style.textAlign,
			style: {
				color: chartConf.emptymessage.style.fontColor,
				fontWeight: chartConf.emptymessage.style.fontWeight,
				fontSize: chartConf.emptymessage.style.fontSize,
				fontFamily: chartConf.emptymessage.style.fontFamily,
				fontStyle: chartConf.emptymessage.style.fontStyle ? chartConf.emptymessage.style.fontStyle : "none",
				textDecoration: chartConf.emptymessage.style.textDecoration ? chartConf.emptymessage.style.textDecoration : "none",
				fontWeight: chartConf.emptymessage.style.fontWeight ? chartConf.emptymessage.style.fontWeight : "none"
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
		}

	});

}

function renderHeatmap(chartConf){
    var start;
    
    var startDate= new Date(chartConf.additionalData.dateresult[0]);
    var endDate= new Date(chartConf.additionalData.dateresult[1]);
    var points=[];
    var data=chartConf.data[0];
    var minValue=data[0][chartConf.additionalData.serie.value];
    var maxValue=data[0][chartConf.additionalData.serie.value];
    
    for( i=0;i<data.length;i++ ){
    	if(data[i][chartConf.additionalData.serie.value]< minValue){
    		minValue=data[i][chartConf.additionalData.serie.value];
    	}
    	
    	if(data[i][chartConf.additionalData.serie.value] > maxValue){
    		maxValue=data[i][chartConf.additionalData.serie.value];
    	}
    	
    	var point={
    		"x":new Date(data[i][chartConf.additionalData.columns[0].value]).getTime(),
    		"y":chartConf.additionalData.storeresult.indexOf(data[i][chartConf.additionalData.columns[1].value]),
    		"value":data[i][chartConf.additionalData.serie.value],
    		"label":data[i][chartConf.additionalData.columns[1].value]
    	};
    	
    	points.push(point);
    }
    
    var colors=chartConf.colors;
    var colorStops=[];
    
    /**
     * Check if user specified only 1 color from the color palette. 
     * @modifiedBy: danristo (danilo.ristovski@mht.net)
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
    	 * @author: danristo (danilo.ristovski@mht.net)
    	 */
    	var startIntervalColor = "#FFFFFF";	// White color
    	
    	colorStops.push([0,startIntervalColor]);
    	colorStops.push([1,colors[0]]);
	}    
    
    var chartObject = null;
    
    if (chartConf.chart.height==""
    		|| chartConf.chart.width=="")
	{
    	chartObject = 
    	{
        	renderTo: 'mainPanel',
            type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,
            margin: [80, 80, 80, 80],
			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "none",
				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "none",
				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : "none"
	        
			}
    	};
	}
    else if (chartConf.chart.height!=""
    		&& chartConf.chart.width!="")
	{
    	chartObject = 
    	{
        	renderTo: 'mainPanel',
        	height:  Number(chartConf.chart.height),
			width:  Number(chartConf.chart.width),
            type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,
            margin: [80, 80, 80, 80],
			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
				fontStyle: chartConf.chart.style.fontStyle ? chartConf.chart.style.fontStyle : "none",
				textDecoration: chartConf.chart.style.textDecoration ? chartConf.chart.style.textDecoration : "none",
				fontWeight: chartConf.chart.style.fontWeight ? chartConf.chart.style.fontWeight : "none"
	        
			}
    	};
	}
    
    var chart = new Highcharts.Chart({
       
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
				fontWeight: chartConf.title.fontWeight ? chartConf.title.fontWeight : "none"
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
				fontWeight: chartConf.subtitle.fontWeight ? chartConf.subtitle.fontWeight : "none"
            }
		},
		
		noData: {
			text: chartConf.emptymessage.text,
			align: chartConf.emptymessage.style.textAlign,
			style: {
                color: chartConf.emptymessage.style.fontColor,
                fontSize: chartConf.emptymessage.style.fontSize,
                fontFamily: chartConf.emptymessage.style.fontFamily,
                fontStyle: chartConf.emptymessage.style.fontStyle ? chartConf.emptymessage.style.fontStyle : "none",
				textDecoration: chartConf.emptymessage.style.textDecoration ? chartConf.emptymessage.style.textDecoration : "none",
				fontWeight: chartConf.emptymessage.fontWeight ? chartConf.emptymessage.fontWeight : "none"
            }
		},

        xAxis: {
            type: 'datetime', // the numbers are given in milliseconds
            min: Date.UTC(startDate.getUTCFullYear(),startDate.getUTCMonth(),startDate.getUTCDate()),  // gets range from variables 
            max: Date.UTC(endDate.getUTCFullYear(),endDate.getUTCMonth(),endDate.getUTCDate()),  
            labels: {
                align: 'left',
                x: 5,
                y: 15,
                format: '{value:%B %Y}',// long month
                rotation: (chartConf.xaxis.labels.rotation!=undefined && chartConf.xaxis.labels.rotation!="") ? chartConf.xaxis.labels.rotation : '',	
                align: (chartConf.xaxis.labels.align!=undefined && chartConf.xaxis.labels.align!="") ? chartConf.xaxis.labels.align : '',	
                style:{
           		 	color: chartConf.xaxis.labels.style.fontColor,
           		 	fontStyle:(chartConf.xaxis.labels.style.fontStyle!=undefined && chartConf.xaxis.labels.style.fontStyle!="") ? chartConf.xaxis.labels.style.fontStyle : '',
                    textDecoration: (chartConf.xaxis.labels.style.textDecoration!=undefined && chartConf.xaxis.labels.style.textDecoration!="") ? chartConf.xaxis.labels.style.textDecoration : '',
                    fontSize: chartConf.xaxis.labels.style.fontSize,
                    fontFamily: chartConf.xaxis.labels.style.fontFamily
           	}	
            },
            tickInterval:30*24*3600*1000,
            showLastLabel: true,
            tickLength: 16
        },

        yAxis: {
            title: {
                text: null
            },
            labels:{
            	rotation: (chartConf.yaxis.labels.rotation!=undefined && chartConf.yaxis.labels.rotation!="") ? chartConf.yaxis.labels.rotation : '',	
                align: (chartConf.yaxis.labels.align!=undefined && chartConf.yaxis.labels.align!="") ? chartConf.yaxis.labels.align : '',	
            	
            	style:{
            		 color: chartConf.yaxis.labels.style.fontColor,
                     fontStyle:(chartConf.yaxis.labels.style.fontStyle!=undefined && chartConf.yaxis.labels.style.fontStyle!="") ? chartConf.yaxis.labels.style.fontStyle : '',
                     textDecoration: (chartConf.yaxis.labels.style.textDecoration!=undefined && chartConf.yaxis.labels.style.textDecoration!="") ? chartConf.yaxis.labels.style.textDecoration : '',
                     fontSize: chartConf.yaxis.labels.style.fontSize,
                     fontFamily: chartConf.yaxis.labels.style.fontFamily
            	}
            },
            categories:chartConf.additionalData.storeresult,
            reversed: false
        },

        colorAxis: {
        	 stops:colorStops ,
                 min: minValue,
                 max: maxValue,
            labels: {
                format: '{value}'
            }
        },
        
        legend: {
            layout: 'horizontal',
            align: chartConf.legend.style.align, 
            symbolWidth: Number(chartConf.legend.symbolWidth)	// modified by: (danilo.ristovski@mht.net)
        },
        
        tooltip: {
        	headerFormat: '<b>'+chartConf.additionalData.serie.value+'</b><br/>',
            pointFormat: '{point.x:%e %b, %Y} | {point.label}: <b>{point.value}</b>',
            style:{ 
            	 color: chartConf.tooltip.style.fontColor,
                 fontSize: chartConf.tooltip.style.fontSize,
                 fontFamily: chartConf.tooltip.style.fontFamily
            }
        },
        series: [{
            borderWidth: 0,
            nullColor: '#EFEFEF',
            colsize: 24 * 36e5, // one day    
            data:points,
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
    });
	
}