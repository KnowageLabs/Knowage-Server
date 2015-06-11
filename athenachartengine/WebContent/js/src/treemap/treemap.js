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
				color: Highcharts.getOptions().colors[level0_i]
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
			renderTo: 'mainPanel'
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
			text: chartConf.subtitle.text
		},
		title: {
			text: chartConf.title.text
		}
	});

}