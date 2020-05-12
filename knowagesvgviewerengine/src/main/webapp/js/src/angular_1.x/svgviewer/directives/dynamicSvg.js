window.onload = function() {
    createChart();
}

function doClickOnSvg(id){
	var svg = document.getElementById("svgContainer");
    var elem 	= svg.contentDocument.getElementById(id);
	elem.onclick.apply(elem);
}

function createChart() {
	var svg = document.getElementById("svgContainer");
	var serviceResponse = {},
		labels			= [],
		columns 		= [],
		percCol 		= [],
		backgrounds 	= [];

    function serviceGetData() {
        var jqxhr = $.ajax({
            url: '/knowagesvgviewerengine/api/1.0/svgviewer/getCustomizedConfiguration',
            type: 'post',
            body: _requestParameterMap
        })
        .done(function(response) {
        	serviceResponse = response.data;
        	
            for(var i=0;i<response.data.metaData.fields.length;i++){

            	if(response.data.metaData.fields[i].logicalType && response.data.metaData.fields[i].logicalType == "measure"){
            		labels.push(response.data.metaData.fields[i].header);
            		columns.push(response.data.metaData.fields[i].name);
            	}
            	if(response.data.metaData.fields[i].logicalType && response.data.metaData.fields[i].logicalType == "perc_value"){
            		percCol.push(response.data.metaData.fields[i].name)
            	}
            }
            if(response.CUSTOMIZE_SETTINGS.CHART.BACKGROUND){
            	for(var j=0; j<labels.length; j++){
            		if(response.CUSTOMIZE_SETTINGS.CHART.BACKGROUND[j]){
            			backgrounds.push(response.CUSTOMIZE_SETTINGS.CHART.BACKGROUND[j].background_color);
            		}else{
            			backgrounds.push('#'+Math.floor(Math.random()*16777215).toString(16));
            		}
            		
            	}
        	}
            else{
            	for(var b in labels){
            		backgrounds.push('#'+Math.floor(Math.random()*16777215).toString(16));
            	}
            }
            
            initializeLegend();
            if(response.data.rows){
            for(var k in response.data.rows){
            	
            		var data=[],perc=[];
            		for(var j in columns){
            			if(response.data.rows[k][columns[j]]!=""){
            				data.push(response.data.rows[k][columns[j]]);
            			}            			
            			if(percCol.length>0){
            				perc.push(response.data.rows[k][percCol[j]]);
            			}
            		}
                initializeChart(response.CUSTOMIZE_SETTINGS,data,response.data.rows[k]['column_1'],perc);
            }
            }
        })
        .fail(function(error) {
            alert(error.statusText);
        })
    }
    serviceGetData();
    
    window.onresize = function(event){
    	for(k in serviceResponse.rows){
    		if(svg.contentDocument.getElementById(serviceResponse.rows[k]['column_1'])){
    			var centroide 	= svg.contentDocument.getElementById(serviceResponse.rows[k]['column_1']),
            		position 	= centroide.getBoundingClientRect(),
            		canvas 		= document.getElementById(serviceResponse.rows[k]['column_1']);
    			
	            canvas.parentElement.style.top 		= position.top;
	            canvas.parentElement.style.left 	= position.left;
	            canvas.parentElement.style.width 	= position.width;
	            canvas.parentElement.style.height 	= position.height;
    		}
    	}
    }
    
    function initializeLegend(){
    	var legend = document.getElementById("graphLegend");
    	for (var k in labels){
    		var label = document.createElement("div"),
    			span = document.createElement("div"),
    			color = document.createElement("div");
    		label.className = "graphLabel";
    		span.className 	= "graphSpan";
    		color.className = "graphColor";
    		span.innerHTML 	= labels[k];
    		color.style.backgroundColor = backgrounds[k];
    		label.appendChild(span);
    		label.appendChild(color);
    		legend.appendChild(label);
    		
    	}
    }
    
    //getting the output format for the percentage from the template xml
    //available formats are ### - ###,## - ###,##
    function formatString(value,format){
	    var output = value,
	    	precision = 2;  //default 
	    if (format){
	    	switch (format) {
	        case "###":
	            output = numberFormat(value, 0, ','); 
	        break;
	        case "###,##":
	            output = numberFormat(value, precision, ','); 
	        break;
	        case "###.##":
	            output = numberFormat(value, precision, '.');
	            break;
	        default:                    
	            break;
	        }
	    } 
	    return output;
    }
    
    //creating the output with the correct decimal precision and separator
	function numberFormat (value, dec, sep) {
		
		value = parseFloat(value).toFixed(dec);
		var parts 		= value.split('.'), 
			fnums 		= parts[0],
			decimals 	= parts[1] ? (sep || '.') + parts[1] : '';
			
		return fnums + decimals;
    }
    	
    function initializeChart(config,data,chartId,perc) {
    	//checking if the id is in the svg, otherwise will not render the chart
    	if(svg.contentDocument.getElementById(chartId)){
    		var centroide = svg.contentDocument.getElementById(chartId),
        	position = centroide.getBoundingClientRect();
        
	        function setChartToPosition(position){
	            var div = document.createElement("div");
	            div.className = "graph";
	            div.setAttribute("onclick","doClickOnSvg('"+chartId+"')");
	            var canvas = document.createElement("canvas");
	            canvas.id = chartId;
	            div.appendChild(canvas);
	            if(data.length>0){
	            	var percLegend = document.createElement("div");
		            percLegend.className = "percLegend";
		            if(perc){
		            	for (var k in perc){
		            		var label 	= document.createElement("div"),
		            		 	span 	= document.createElement("div"),
		            		 	color 	= document.createElement("div");
		            		label.className = "graphLabel";
		            		span.className 	= "graphSpan";
		            		color.className = "graphColor";
		            		debugger;
		            		span.innerHTML 	= formatString(perc[k],config.CHART.OPTIONS.LEGEND.LABELS.format)+"%";
		            		color.style.backgroundColor = backgrounds[k];
		            		label.appendChild(span);
		            		label.appendChild(color);
		            		percLegend.appendChild(label);
		                }
		            	div.appendChild(percLegend);
		            }
	            }else {
	            	canvas.setAttribute("class","emptyPie");
	            }
	            
	            canvas.parentElement.style.top 		= position.top;
	            canvas.parentElement.style.left 	= position.left;
	            canvas.parentElement.style.width 	= position.width;
	            canvas.parentElement.style.height 	= position.height;
	            document.getElementById("dynamic-svg").appendChild(div);
	            
	        };
	        setChartToPosition(position);
        
	        var ctx = document.getElementById(chartId).getContext("2d");
	
	        //PIE CHART CHART.JS DESCRIPTOR
	        if(data.length>0){
	        	var myChart = new Chart(ctx, {
	                type: config.CHART.type,
	                data: {
	                    labels: labels,
	                    datasets: [{
	                        data: data,
	                        backgroundColor: backgrounds
	                    }]
	                },
	                options: {
	                	legend : {
	                		display: false
	                	}
	                }
	            });
	        }else{
	        	//If the chart doesn't have data it's shown in grey
	        	var myChart = new Chart(ctx, {
	                type: config.CHART.type,
	                data: {
	                	labels: ['noData'],
	                    datasets: [{       
	                        data: [1],
	                        backgroundColor: ['grey']
	                    }]
	                },
	                options: {
	                	tooltips: {
	                		enabled: false
	                	},
	                	legend : {
	                		display: false
	                	}
	                }
	            });
	        }
    	}else return;
    }

}
