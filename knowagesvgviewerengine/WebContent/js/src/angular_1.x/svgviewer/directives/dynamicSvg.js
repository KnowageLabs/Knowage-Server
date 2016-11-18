'use strict'
app.directive('dynamicSvg',function($http,$window){
	return{
		restrict : "E",
		templateUrl: "/knowagesvgviewerengine/js/src/angular_1.x/svgviewer/directives/dynamicSvg.html",	
		replace: true,
		link: function(scope, elem, attr){
			
			//WAIT FOR THE SVG TO LOAD
			$window.document.addEventListener("SVGLoaded", function(e){
				
				//REST API GET 
				$http({
					  method: 'GET',
					  url: '/knowagesvgviewerengine/api/1.0/svgviewer/getcustomizedconfiguration',
			        })
			        .then(function(response) {
			            var labels = [];
			            for(i=2;i<response.data.metaData.fields.length;i++){
			                labels.push(response.data.metaData.fields[i].header);
			            }
			            for(k in response.rows){
			                var data = [response.data.rows[k]['column_2'],response.data.rows[k]['column_3'],response.data.rows[k]['column_4']];
			                initializeChart(labels,data,response.data.rows[k]['column_1']);
			            }
			        },function(error) {
			            alert(error.statusText);
			        })
				scope.style = {};
			});
			
			//RESIZE LISTENER TO ADAPT GRAPH ON THE SVG
			angular.element($window).on('resize', function(){
				if(scope.response){
					for(k=0;k<scope.response.rows.length;k++){
						var ref = document.getElementById("svgContainer").contentDocument.getElementById(scope.response.rows[k]['column_1']);
							scope.style[k] = ref.getBoundingClientRect();
					}
				}
			});

			//CHART CONTAINER INITIALIZATION FUNCTION
		    scope.initializeChart = function (labels,data,index) {
		    	var chartId = scope.response.data.rows[index]['column_1'];
		        var svg = document.getElementById("svgContainer");
		        var centroide = svg.contentDocument.getElementById(chartId);
		        scope.style[index] = centroide.getBoundingClientRect();
		        
		        var ctx = document.getElementById("canvas_"+chartId).getContext("2d");
		        
		        //CHART GENERATOR
		        var myChart = new Chart(ctx, {
		            type: scope.response.CUSTOMIZE_SETTINGS.CHART.type,
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
		            options: scope.response.CUSTOMIZE_SETTINGS.CHART.OPTIONS
		        });

		    }
					
		}
	}
})
