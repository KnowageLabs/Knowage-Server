window.onload = function() {
    createChart();
}

function createChart() {

    function serviceGetData() {
        var jqxhr = $.ajax({
            url: '/knowagesvgviewerengine/api/1.0/svgviewer/getCustomizedConfiguration',
            type: 'get',
        })
        .done(function(response) {
            var labels = [];
            for(i=2;i<response.data.metaData.fields.length;i++){
                labels.push(response.data.metaData.fields[i].header);
            }
            for(k in response.data.rows){
                var data = [response.data.rows[k]['column_2'],response.data.rows[k]['column_3'],response.data.rows[k]['column_4']];
                initializeChart(labels,response.CUSTOMIZE_SETTINGS,data,response.data.rows[k]['column_1']);
            }
        })
        .fail(function(error) {
            alert(error.statusText);
        })
    }
    serviceGetData();

    function initializeChart(labels,config,data,chartId) {
        //TODO controllare se esiste data o mostrare errore

        var svg = document.getElementById("svgContainer");
        var centroide = svg.contentDocument.getElementById(chartId);
        var position = centroide.getBoundingClientRect();
        
        function setChartToPosition(position){
            var div = document.createElement("div");
            div.className = "graph";
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
                    backgroundColor: [
                        "blue",
                        "orange",
                        "red"
                    ]
                }]
            },
            options: {
            	legend : {
            		display : false
            	}
            }
        });

    }

}
