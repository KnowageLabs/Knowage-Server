(function () {
	var kpiViewerModule = angular.module('kpiViewerModule');

	kpiViewerModule.factory('baseHighchartsTemplate', function () {
		var chartData = {

			chart: {
				renderTo: 'container',
	            type: 'gauge',
	            plotBackgroundColor: null,
	            plotBackgroundImage: null,
	            plotBorderWidth: 0,
	            plotShadow: false
	        },

	        title: {
	            text: 'Speedometer'
	        },

	        tooltip: {
	            enabled: false
	        },
	        
	        pane: {
	            startAngle: -90,
	            endAngle: 90,
	            background: [{
	                backgroundColor: null,
	                borderWidth: 0,
	                outerRadius: '109%'
	            }, ]
	        },
	        
	        plotOptions: {
	            series: {
	                animation: {
	                    duration: 1500
	                }
	            }
	        },

	        // the value axis
	        yAxis: [{
	            min: 0,
	            max: 200,

	            minorTickInterval: 'auto',
	            minorTickWidth: 1,
	            minorTickLength: 10,
	            minorTickPosition: 'inside',
	            minorTickColor: '#666',

	            tickPixelInterval: 30,
	            tickWidth: 2,
	            tickPosition: 'inside',
	            tickLength: 10,
	            tickColor: '#666',
	            labels: {
	                step: 2,
	                rotation: 'auto'
	            },
	            title: {
	                text: 'km/h'
	            },
	            
	            plotBands: [{
	                from: 0,
	                to: 120,
	                color: '#55BF3B', // green
	                thickness: '100%'
	            }, {
	                from: 120,
	                to: 160,
	                color: '#DDDF0D', // yellow
	                thickness: '100%'
	            }, {
	                from: 160,
	                to: 200,
	                color: '#DF5353', // red
	                thickness: '100%'
	            }]
	        }],

	        series: [{
	            name: 'Speed',
	            data: [87],
	            dataLabels: {
	                enabled: false
	            }
	        }],
	        
	        credits: {
	        	enabled: false
	        },
	    };
		
		return chartData;
	});
})();