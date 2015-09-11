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
    for(i=0;i<colors.length;i++){
    	var stop=[i*(1/(colors.length-1)),colors[i]];
    	colorStops.push(stop);
    }
    
    var chartObject = null;
    
    if (chartConf.chart.height==""
    		|| chartConf.chart.width=="")
	{
    	chartObject = 
    	{
        	renderTo: 'mainPanel',
//        	height:  ? Number(chartConf.chart.height) : "",
//			width:  ? Number(chartConf.chart.width) : "",
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
                style:{
           		 color: chartConf.xaxis.style.fontColor,
                    fontWeight: chartConf.xaxis.style.fontWeight,
                    fontSize: chartConf.xaxis.style.fontSize,
                    fontFamily: chartConf.xaxis.style.fontFamily
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
            	style:{
            		 color: chartConf.yaxis.style.fontColor,
                     fontWeight: chartConf.yaxis.style.fontWeight,
                     fontSize: chartConf.yaxis.style.fontSize,
                     fontFamily: chartConf.yaxis.style.fontFamily
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
        }]

    });
	
}