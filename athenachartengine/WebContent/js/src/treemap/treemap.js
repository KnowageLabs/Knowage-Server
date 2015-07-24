function renderTreemap(chartConf) {
	
	var points = [],
	level0_p,
	level0_val,
	level0_i,
	level1_p,
	level1_i,
	level2_p,
	level2_i,
	level2_name = [];

	level0_i = 0;

	for (var level0 in chartConf.data[0]){

		level0_val = 0;
		level0_p = {
				id: "id_" + level0_i,
				name: level0,
				color: chartConf.colors[level0_i]
		};

		level1_i = 0;
		for (var level1 in chartConf.data[0][level0]) {

			level1_p = {
					id: level0_p.id + "_" + level1_i,
					name: level1,
					parent: level0_p.id
			};
			points.push(level1_p);
			level2_i = 0;

			for (var level2 in chartConf.data[0][level0][level1]) {

				level2_p = {
						id: level1_p.id + "_" + level2_i,
						//name: level2_name[level2],
						name: level1,
						parent: level1_p.id,
						value: Math.round(+chartConf.data[0][level0][level1][level2])
				};
				level0_val += level2_p.value;
				points.push(level2_p);
				level2_i++;
			}
			level1_i++;
		}
		//level0_p.value = Math.round(level0_val / level1_i);
		points.push(level0_p);
		level0_i++;

	}

	var chart = new Highcharts.Chart({
		chart: {
			renderTo: 'mainPanel',
			height: chartConf.chart.height,
			width: chartConf.chart.width,
			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
	            fontWeight: chartConf.chart.style.fontWeight
	        }
		},
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
                fontWeight: chartConf.subtitle.style.fontWeight,
                fontSize: chartConf.subtitle.style.fontSize,
                fontFamily: chartConf.subtitle.style.fontFamily
            }
		},
		title: {
			text: chartConf.title.text,
			align: chartConf.title.style.textAlign,
			style: {
                color: chartConf.title.style.fontColor,
                fontWeight: chartConf.title.style.fontWeight,
                fontSize: chartConf.title.style.fontSize,
                fontFamily: chartConf.title.style.fontFamily
            }
		},
		noData: {
			text: chartConf.emptymessage.text,
			align: chartConf.emptymessage.style.textAlign,
			style: {
                color: chartConf.emptymessage.style.fontColor,
                fontWeight: chartConf.emptymessage.style.fontWeight,
                fontSize: chartConf.emptymessage.style.fontSize,
                fontFamily: chartConf.emptymessage.style.fontFamily
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
     
    console.log();
    
    var chart = new Highcharts.Chart({
       
        chart: {
        	renderTo: 'mainPanel',
        	height: Number(chartConf.chart.height),
			width: Number(chartConf.chart.width),
            type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,
            margin: [80, 80, 80, 80],
			style: {
	            fontFamily: chartConf.chart.style.fontFamily,
	            fontSize: chartConf.chart.style.fontSize,
	            fontWeight: chartConf.chart.style.fontWeight
	        }
        },
        title: {
			text: chartConf.title.text,
            align: chartConf.title.style.textAlign,
			style: {
                color: chartConf.title.style.fontColor,
                fontWeight: chartConf.title.style.fontWeight,
                fontSize: chartConf.title.style.fontSize,
                fontFamily: chartConf.title.style.fontFamily
            }
		},
		subtitle: {
			text: chartConf.subtitle.text,
            align: chartConf.subtitle.style.textAlign,
			style: {
                color: chartConf.subtitle.style.fontColor,
                textDecoration: chartConf.subtitle.style.fontWeight,
                fontSize: chartConf.subtitle.style.fontSize,
                fontFamily: chartConf.subtitle.style.fontFamily
            }
		},
		
		noData: {
			text: chartConf.emptymessage.text,
			align: chartConf.emptymessage.style.textAlign,
			style: {
                color: chartConf.emptymessage.style.fontColor,
                fontWeight: chartConf.emptymessage.style.fontWeight,
                fontSize: chartConf.emptymessage.style.fontSize,
                fontFamily: chartConf.emptymessage.style.fontFamily
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
            symbolWidth: Number(chartConf.legend.style.symbolWidth)
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