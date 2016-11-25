window.onload = function() {
    createChart();
}

function doClickOnSvg(id){
	var svg 	= document.getElementById("svgContainer");
    var elem 	= svg.contentDocument.getElementById(id);
	elem.onclick.apply(elem);
}

function createChart() {
	var serviceResponse = {};
	var labels	=[];
	var backgrounds = [];

    function serviceGetData() {
        var jqxhr = $.ajax({
            url: '/knowagesvgviewerengine/api/1.0/svgviewer/getCustomizedConfiguration',
            type: 'get',
        })
        .done(function(response) {
        	serviceResponse = response.data;
        	
        	
            for(var i=0;i<response.data.metaData.fields.length;i++){

            	if(response.data.metaData.fields[i].logicalType && response.data.metaData.fields[i].logicalType == "measure"){
            		labels.push(response.data.metaData.fields[i].header);
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
            for(var k in response.data.rows){
            	if(response.data.rows)
                var data = [response.data.rows[k]['column_3'],response.data.rows[k]['column_4'],response.data.rows[k]['column_5']];
            	var perc = [response.data.rows[k]['column_7'],response.data.rows[k]['column_8'],response.data.rows[k]['column_9']]
                initializeChart(response.CUSTOMIZE_SETTINGS,data,response.data.rows[k]['column_1'],perc);
            }
        })
        .fail(function(error) {
            alert(error.statusText);
        })
    }
    serviceGetData();
    
    window.onresize = function(event){
    	for(k in serviceResponse.rows){
    		var svg 		= document.getElementById("svgContainer");
            var centroide 	= svg.contentDocument.getElementById(serviceResponse.rows[k]['column_1']);
            var position 	= centroide.getBoundingClientRect(); 
            var canvas 		= document.getElementById(serviceResponse.rows[k]['column_1']);
            canvas.parentElement.style.top = position.top;
            canvas.parentElement.style.left = position.left;
            canvas.parentElement.style.width = position.width;
            canvas.parentElement.style.height = position.height;
    	}
    }
    
    function initializeLegend(){
    	var legend = document.getElementById("graphLegend");
    	for (var k in labels){
    		var label = document.createElement("div");
    		var span = document.createElement("div");
    		var color = document.createElement("div");
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
    
   


    function initializeChart(config,data,chartId,perc) {

        var svg = document.getElementById("svgContainer");
        var centroide = svg.contentDocument.getElementById(chartId);
        var position = centroide.getBoundingClientRect();
        
        function setChartToPosition(position){
            var div = document.createElement("div");
            div.className = "graph";
            div.setAttribute("onclick","doClickOnSvg('"+chartId+"')");
            var canvas = document.createElement("canvas");
            var percLegend = document.createElement("div");
            percLegend.className = "percLegend";
            for (var k in perc){
        		var label = document.createElement("div");
        		var span = document.createElement("div");
        		var color = document.createElement("div");
        		label.className = "graphLabel";
        		span.className 	= "graphSpan";
        		color.className = "graphColor";
        		span.innerHTML 	= parseFloat(perc[k]).toFixed(2)+"%";
        		color.style.backgroundColor = backgrounds[k];
        		label.appendChild(span);
        		label.appendChild(color);
        		percLegend.appendChild(label);
            }
            
            
            
            canvas.id = chartId;
            div.appendChild(canvas); 
            div.appendChild(percLegend);
            canvas.parentElement.style.top = position.top;
            canvas.parentElement.style.left = position.left;
            canvas.parentElement.style.width = position.width;
            canvas.parentElement.style.height = position.height;
            document.getElementById("dynamic-svg").appendChild(div);
            
        };
        setChartToPosition(position);
        
        var ctx = document.getElementById(chartId).getContext("2d");

        //DESCRITTORE GRAFICO A TORTA
        debugger;
        var myChart = new Chart(ctx, {
            type: config.CHART.type,
            data: {
                labels: labels,
                datasets: [{
                    data: data,
                    backgroundColor: backgrounds
                }]
            },
            options: {//config.CHART.OPTIONS//{
            	legend : {
            		display: false
            	}/*,
            	elements: {
            		arc: {
            			borderWidth: 0
            		}
            	}*/
            }
        });

    }

}
