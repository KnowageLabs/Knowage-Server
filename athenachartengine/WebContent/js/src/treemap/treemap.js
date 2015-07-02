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
	
	(function (H) {
		
        var Series = H.Series,
            each = H.each,
            wrap = H.wrap,
            seriesTypes = H.seriesTypes;
        /**
         * Create a hidden canvas to draw the graph on. The contents is later copied over 
         * to an SVG image element.
         */
        Series.prototype.getContext = function () {
            if (!this.canvas) {
                this.canvas = document.createElement('canvas');
                this.canvas.setAttribute('width', this.chart.chartWidth);
                this.canvas.setAttribute('height', this.chart.chartHeight);
                this.image = this.chart.renderer.image('', 0, 0, this.chart.chartWidth, this.chart.chartHeight).add(this.group);
                this.ctx = this.canvas.getContext('2d');
            }
            return this.ctx;
        };

        /** 
         * Draw the canvas image inside an SVG image
         */ 
        Series.prototype.canvasToSVG = function () {
            this.image.attr({ href: this.canvas.toDataURL('image/png') });
        };

        /**
         * Wrap the drawPoints method to draw the points in canvas instead of the slower SVG,
         * that requires one shape each point.
         */
        H.wrap(H.seriesTypes.heatmap.prototype, 'drawPoints', function (proceed) {

            var ctx = this.getContext();
            
            if (ctx) {

                // draw the columns
                each(this.points, function (point) {
                    var plotY = point.plotY,
                        shapeArgs;

                    if (plotY !== undefined && !isNaN(plotY) && point.y !== null) {
                        shapeArgs = point.shapeArgs;

                        ctx.fillStyle = point.pointAttr[''].fill;
                        ctx.fillRect(shapeArgs.x, shapeArgs.y, shapeArgs.width, shapeArgs.height);
                    }
                });

                this.canvasToSVG();

            } else {
                this.chart.showLoading("Your browser doesn't support HTML5 canvas, <br>please use a modern browser");

                // Uncomment this to provide low-level (slow) support in oldIE. It will cause script errors on
                // charts with more than a few thousand points.
                //proceed.call(this);
            }
        });
        H.seriesTypes.heatmap.prototype.directTouch = false; // Use k-d-tree
    }(Highcharts));

	//var salesdata=[]; 
    var start;
    var startDate= new Date(chartConf.chart.dateresult[0]);
    var endDate= new Date(chartConf.chart.dateresult[1]);
    var points=[];
    var data=chartConf.data[0];
    var minValue=data[0][chartConf.chart.serie.value];
    var maxValue=data[0][chartConf.chart.serie.value];
    for( i=0;i<data.length;i++ ){
    	if(data[i][chartConf.chart.serie.value]< minValue){
    		minValue=data[i][chartConf.chart.serie.value];
    	}
    	
    	if(data[i][chartConf.chart.serie.value] > maxValue){
    		maxValue=data[i][chartConf.chart.serie.value];
    	}
    	
    	var point={
    		"x":new Date(data[i][chartConf.chart.columns[0].value]).getTime(),
    		"y":chartConf.chart.storeresult.indexOf(data[i][chartConf.chart.columns[1].value]),
    		"value":data[i][chartConf.chart.serie.value]
    	};
    	
    	points.push(point);
    }
    
   // var testPoints=points.slice(0,100);
    var chart = new Highcharts.Chart({
       
        chart: {
        	renderTo: 'mainPanel',
            type: 'heatmap',
            width: 1400,
            height: 800,
            margin: [80, 10, 80, 80]
        },
        title: {
			text: chartConf.title.text,
			style: {
                color: chartConf.title.style.fontColor,
                fontWeight: chartConf.title.style.fontWeight,
                fontSize: chartConf.title.style.fontSize,
                fontFamily: chartConf.title.style.fontFamily,
                align: chartConf.title.style.textAlign
            }
		},
		subtitle: {
			text: chartConf.subtitle.text,
			style: {
                color: chartConf.subtitle.style.fontColor,
                textDecoration: chartConf.subtitle.style.fontWeight,
                fontSize: chartConf.subtitle.style.fontSize,
                fontFamily: chartConf.subtitle.style.fontFamily,
                align: chartConf.subtitle.style.textAlign
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
                format: '{value:%B}' // long month
            },
            showLastLabel: true,
            tickLength: 16
        },

        yAxis: {
            title: {
                text: null
            },
            categories:chartConf.chart.storeresult,
            reversed: false
        },

        colorAxis: {
        	 stops: [
                     [0, '#ffff00'],
                     [0.2, '#009900'],
                     [0.4, '#e60000'],
                     [0.6,'#002eb8'],
                     [0.8,'#ff9900'],
                     [1, '#000000']
                 ],
                 min: minValue,
                 max: maxValue,
            labels: {
                format: '{value}'
            }
        },

        series: [{
            borderWidth: 0,
            nullColor: '#EFEFEF',
            colsize: 24 * 36e5, // one day
            tooltip: {
                headerFormat: chartConf.chart.serie.value+'<br/>',
                pointFormat: '{point.x:%e %b, %Y} {point.y}: <b>{point.value}</b>'
            },
            data:points,
            turboThreshold: Number.MAX_VALUE// #3404, remove after 4.0.5 release
        }]

    });
	
}