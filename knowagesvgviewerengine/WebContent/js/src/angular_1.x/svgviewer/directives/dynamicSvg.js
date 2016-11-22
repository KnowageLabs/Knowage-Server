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
            
            for(var i=3;i<response.data.metaData.fields.length;i++){
            	//debugger;
            	//if(response.COLUMNS[i].type == "measure"){
            		labels.push(response.data.metaData.fields[i].header);
            	//}
            }
            
            for(var j in response.CUSTOMIZE_SETTINGS.CHART.BACKGROUND){
            	backgrounds.push(response.CUSTOMIZE_SETTINGS.CHART.BACKGROUND[j].background_color);
            }
            initializeLegend();
            for(var k in response.data.rows){
                var data = [response.data.rows[k]['column_3'],response.data.rows[k]['column_4'],response.data.rows[k]['column_5']];
                initializeChart(response.CUSTOMIZE_SETTINGS,data,response.data.rows[k]['column_1']);
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
            var position 	= centroide.getBoundingClientRect(); ;
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
    
   


    function initializeChart(config,data,chartId) {

        var svg = document.getElementById("svgContainer");
        var centroide = svg.contentDocument.getElementById(chartId);
        var position = centroide.getBoundingClientRect();
        
        function setChartToPosition(position){
            var div = document.createElement("div");
            div.className = "graph";
            div.setAttribute("onclick","doClickOnSvg('"+chartId+"')");
            var canvas = document.createElement("canvas");
            canvas.id = chartId;
            div.appendChild(canvas); 
            canvas.parentElement.style.top = position.top;
            canvas.parentElement.style.left = position.left;
            canvas.parentElement.style.width = position.width;
            canvas.parentElement.style.height = position.height;
            document.getElementById("dynamic-svg").appendChild(div);
            
        };
        setChartToPosition(position);
        
        var ctx = document.getElementById(chartId).getContext("2d");

        //DESCRITTORE GRAFICO A TORTA
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
            	 elements: {
            		    arc: {
            		      borderWidth: 0
            		    }
            		  },
            	legend : {
            		display : false
            	}
            }
        });

    }

}
