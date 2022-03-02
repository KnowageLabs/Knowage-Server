/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2022 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

angular.module('cockpitModule').factory('knModule_chartOptions',function(sbiModule_config){
	return{
		chartBindings:{
			scatter:{
				categories: [1,1],
				numericalCategory: true,
				series: [1,100],
				seriesStacking: [false],
				groupedSeries: [false]
			},
			bubble:{
				categories: [1,100],
				series: [3,100],
				seriesStacking: [false],
				groupedSeries: [true, false]
			},
			line: {
				categories: [1, 100],
				series: [1, 100],
				seriesStacking: [true,false],
				groupedSeries: [true,false],
			},
			bar:{
				categories: [1, 100],
				series: [1, 100],
				seriesStacking: [true,false],
				groupedSeries: [true,false]
			},
			parallel:{
				categories: [1, 1],
				series: [2, 100],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			chord:{
				categories: [2, 2],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],

			},
			sunburst:{
				categories: [2, 100],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			pie:{
				categories: [1, 100],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			gauge:{
				categories: [0, 0],
				series: [1, 100],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			radar:{
				categories: [1, 1],
				series: [1, 100],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			heatmap:{
				categories: [2, 2],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			treemap:{
				categories: [2, 100],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],
			},
			wordcloud:{
				categories: [1, 1],
				series: [1, 1],
				seriesStacking: [false],
				groupedSeries: [false],
			}
		},
		rangeCheck(ranges){
			var max=[];
			for(var range of ranges){
				max.push(range.max);
			}
			return Math.max(max) < (Math.min(max)*10)
		},
		getAvailableCharts(model){
			var chartsToReturn = [];
			for(var k in this.chartBindings){
				var chartType = this.chartBindings[k];
				if(model.categoriesNumber < chartType.categories[0] || model.categoriesNumber > chartType.categories[1]) continue;
				if(chartType.numericalCategory && !model.numericalCategory) continue;
				if(model.seriesNumber < chartType.series[0] || model.seriesNumber > chartType.series[1]) continue;
				if(!chartType.seriesStacking.includes(model.seriesStacking)) continue;
				if(!chartType.groupedSeries.includes(model.groupedSeries)) continue;
				chartsToReturn.push(k)
			}
			return chartsToReturn;
		},
		getSuggestedChart(chart){
			
			var catCardinalityNum = Object.values(chart.categoriesCardinality[0])[0];
			
			if(chart.categoriesNumber==0){
				return 'gauge'
			}
			else if(chart.categoriesNumber==1 && chart.seriesNumber==1){
				if(['float','int'].includes(chart.typeOfCategory)){
					return 'scatter'
				}else{
					if(catCardinalityNum >= 1 && catCardinalityNum <= 2){
						return 'bar'
					}else if(catCardinalityNum >= 3 && catCardinalityNum <= 6){
						if(chart.typeOfCategory=='timestamp'){
							return 'line'
						}else{
							return 'pie'
						}
					}else if(catCardinalityNum >= 7 && catCardinalityNum <= 100){
						if(chart.typeOfCategory=='timestamp'){
							return 'line'
						}else{
							return 'bar'
						}
					}else{
						return 'wordcloud'
					}
				}
			}else if(chart.categoriesNumber==1 && chart.seriesNumber>1){
				if(chart.seriesStacking==true){
					return'bar'
				}else{
					if(['float','int'].includes(chart.typeOfCategory)){
							if(chart.typeOfCategory=='int'){
								return 'parallel'
							}else{
								return 'scatter'
							}
					}else{
						if(chart.seriesNumber==2){
							if(chart.typeOfCategory=='timestamp'){
								return 'line'
							}else{
								return 'bar'
							}
						}else if(chart.seriesNumber==3){
							if(this.rangeCheck(chart.range)){
								return 'bar'
							}else{
								return 'bubble'
							}
						}else {
							return 'parallel'
						}
					}
				}
			}else if(chart.categoriesNumber>1 && chart.seriesNumber==1){
				if(chart.categoriesNumber==2){
					if(chart.groupedSeries==true){
						if(chart.typeOfCategory=='timestamp'){
							return 'line'
						}else{
							return 'bar'
						}
					}else{
						return 'heatmap'
					}
				}else if(chart.categoriesNumber>=3){
					return 'treemap'
				}
			}else if(chart.categoriesNumber>1 && chart.seriesNumber>1){
				return 'bubble'
			}
		}
	}
})
