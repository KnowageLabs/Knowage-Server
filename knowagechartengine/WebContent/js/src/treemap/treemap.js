function renderTreemap(chartConf) {
	
    chartConf = prepareChartConfForTreemap(chartConf);
    
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
    
	new Highcharts.Chart(chartConf);
     
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



function renderHeatmap(chartConf){
    
    chartConfig = prepareChartConfForHeatmap(chartConf); 
    
    var chart = new Highcharts.Chart(chartConfig);
    
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
    	
    	params.point.crossNavigationDocumentName=chartConf.crossNavigation.crossNavigationDocumentName;
    	params.point.crossNavigationDocumentParams=chartConf.crossNavigation.crossNavigationDocumentParams;
    	params.point.category=chartConf.additionalData.columns[0].value;
    	if(chartConf.chart.xAxisDate){
    	params.point.name= new Date(point.x);
    	}else{
    	params.point.name=	chartConf.additionalData.firstCategory[point.x];
    	}
    	params.point.series.name=chartConf.additionalData.serie.value;
    	params.point.y=point.value;
    	params.point.group.name=chartConf.additionalData.columns[1].value;
    	params.point.group.value=point.label;
        
    	return params;
	
}

function getSelectionParammsForHeatmap(point){
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

function getCrossParamsForTreemap(point,chartConf){
	var params={
			point:{
				name: null, // category value
				category: null, // category  value
				crossNavigationDocumentName:null,
				crossNavigationDocumentParams:null,

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

	params.point.crossNavigationDocumentName=chartConf.crossNavigation.crossNavigationDocumentName;
	params.point.crossNavigationDocumentParams=chartConf.crossNavigation.crossNavigationDocumentParams;

	params.point.name=point.name;

	params.point.y=point.value;


	return params;
	
	
}

function prepareChartConfForTreemap(chartConf) {
	
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
	else if (chartConf.chart.height!=""
		&& chartConf.chart.width!="")
	{
		chartObject =     	
		{
			height: chartConf.chart.height ? Number(chartConf.chart.height) : undefined,
			width: chartConf.chart.width ? Number(chartConf.chart.width) : undefined,
					
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
	
	/**
	 * Take drill up button (the "Back" button) setting from the VM.
	 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
	 */
    var drillUpButtonSettings = chartConf.series[0].drillUpButton;
	
	return 	{
		chart: chartObject,
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
	                    y: drillUpButtonSettings.position.y
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
			data: points,
			events:{
				click: function(event){
					console.log(event.point);
			        if(chartConf.chart.isCockpit==true){
			        	if(chartConf.chart.outcomingEventsEnabled){
			        	handleCockpitSelection(event);
			        	}
			        }else if(event.point.node.children.length==0){
						if(chartConf.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
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



function prepareChartConfForHeatmap(chartConf) {
	var start;
	 var startDate;
	 var endDate;
    if(chartConf.chart.xAxisDate){
     startDate= new Date(chartConf.additionalData.firstCategory[0]);
     endDate= new Date(chartConf.additionalData.firstCategory[1]); 
    }
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
    	
    	var xValue;
    	if(chartConf.chart.xAxisDate){
    		xValue=new Date(data[i][chartConf.additionalData.columns[0].value]).getTime();
    	}else{
    		xValue=chartConf.additionalData.firstCategory.indexOf(data[i][chartConf.additionalData.columns[0].value]);
    	}
    	var point={
    		"x":xValue,
    		"y":chartConf.additionalData.storeresult.indexOf(data[i][chartConf.additionalData.columns[1].value]),
    		"value":data[i][chartConf.additionalData.serie.value],
    		"label":data[i][chartConf.additionalData.columns[1].value]
    	};
    	
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
        	renderTo: 'mainPanel',
            type: 'heatmap',
            backgroundColor:chartConf.chart.style.backgroundColor,
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
        	renderTo: 'mainPanel',
        	
        	height: chartConf.chart.height ? Number(chartConf.chart.height) : undefined,
			width: chartConf.chart.width ? Number(chartConf.chart.width) : undefined,
            
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
    if(chartConf.chart.xAxisDate){
    	xAxisObject={
                type: 'datetime', // the numbers are given in milliseconds
                min: Date.UTC(startDate.getUTCFullYear(),startDate.getUTCMonth(),startDate.getUTCDate()),  // gets range from variables 
                max: Date.UTC(endDate.getUTCFullYear(),endDate.getUTCMonth(),endDate.getUTCDate()),  
                
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
                    format: '{value:%B %Y}',// long month
                    rotation: (chartConf.xaxis.labels.rotation!=undefined && chartConf.xaxis.labels.rotation!="") ? chartConf.xaxis.labels.rotation : '',	
                    align: (chartConf.xaxis.labels.align!=undefined && chartConf.xaxis.labels.align!="") ? chartConf.xaxis.labels.align : undefined,	
                    style:{
                    	color: (chartConf.xaxis.labels.style.color!=undefined && chartConf.xaxis.labels.style.color!="" && chartConf.xaxis.labels.style.color!="transparent") ? chartConf.xaxis.labels.style.color : '',
                        fontStyle:(chartConf.xaxis.labels.style.fontStyle!=undefined && chartConf.xaxis.labels.style.fontStyle!="") ? chartConf.xaxis.labels.style.fontStyle : '',
                        textDecoration: (chartConf.xaxis.labels.style.textDecoration!=undefined && chartConf.xaxis.labels.style.textDecoration!="") ? chartConf.xaxis.labels.style.textDecoration : '',
                        fontSize: (chartConf.xaxis.labels.style.fontSize!=undefined && chartConf.xaxis.labels.style.fontSize!="") ? chartConf.xaxis.labels.style.fontSize : '',
                        fontFamily: (chartConf.xaxis.labels.style.fontFamily!=undefined && chartConf.xaxis.labels.style.fontFamily!="") ? chartConf.xaxis.labels.style.fontFamily : '',
               	}	
                },
                tickInterval:30*24*3600*1000,
                showLastLabel: true,
                tickLength: 16
            };
    	
    	serieColSize=24 * 36e5;
    
          tooltipObject={
        		
              	headerFormat: '<b>'+chartConf.additionalData.serie.value+'</b><br/>',
                pointFormat: '{point.x:%e %b, %Y} | {point.label}: <b>{point.value}</b>',
                  style:{ 
                  	 color: chartConf.tooltip.style.fontColor,
                       fontSize: chartConf.tooltip.style.fontSize,
                       fontFamily: chartConf.tooltip.style.fontFamily
                  } 
          };
          
    }else{
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
                    rotation: (chartConf.xaxis.labels.rotation!=undefined && chartConf.xaxis.labels.rotation!="") ? chartConf.xaxis.labels.rotation : '',	
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
//               tickInterval:1,
                 tickLength: 16
            };
    	serieColSize=1;
    	tooltipFormatter= function () {
            return '<b>'+chartConf.additionalData.serie.value+'</b><br>' + this.series.xAxis.categories[this.point.x] + ': <b>' +
            this.point.value + '</b> | ' + this.series.yAxis.categories[this.point.y] + '';
    };
    
    tooltipObject={
    		
              formatter:tooltipFormatter,
              style:{ 
              	 color: chartConf.tooltip.style.fontColor,
                   fontSize: chartConf.tooltip.style.fontSize,
                   fontFamily: chartConf.tooltip.style.fontFamily
              } 
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
            	rotation: (chartConf.yaxis.labels.rotation!=undefined && chartConf.yaxis.labels.rotation!="") ? chartConf.yaxis.labels.rotation : '',	
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
        {
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
            	console.log(event.point);
            	if(chartConf.chart.isCockpit==true){
            		if(chartConf.chart.outcomingEventsEnabled){
            		var selectParams=getSelectionParammsForHeatmap(event.point);
            		handleCockpitSelection(selectParams);
            		}
            	}else{ 
            		if(chartConf.crossNavigation.hasOwnProperty('crossNavigationDocumentName')){
            		
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
            
            labels: 
            {
               format: '{value}'
           }
       };
	}
    
    return toReturn;
}