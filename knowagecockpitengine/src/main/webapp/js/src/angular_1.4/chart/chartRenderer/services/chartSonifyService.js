/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

(function(){

	var app = angular.module("chartRendererModule");

	app.service("chartSonifyService",function (){

		this.chart = {};

		this.instruments = [{
	        instrument: 'triangleMajor',
	        instrumentMapping: {
	            duration: 200,
	            frequency: 'y',
	            volume: 0.7,
	            pan: -1
	        },
	        instrumentOptions: {
	            minFrequency: 220,
	            maxFrequency: 1900
	        }
	    }];

		this.getInstrumentOptions = function (){
			var instrumentOptions = [];
			var sonificationSerie = this.chart.userOptions.accessibility.sonificationSerie;
			var serieIndex = 0;
			for (var i = 0; i < this.chart.series.length; i++) {
				if (sonificationSerie == this.chart.series[i].name) serieIndex = i;
				instrumentOptions.push({
	                id: i+1,
	                instruments: [],
	                onPointStart: null,
	            })
			}
			instrumentOptions[serieIndex].instruments = this.instruments ;
			instrumentOptions[serieIndex].onPointStart = this.highlightPoint ;
			return instrumentOptions

		};

		this.highlightPoint = function (event, point) {
			var chart = point.series.chart,
	        hasVisibleSeries = chart.series.some(function (series) {
	            return series.visible;
	        });
			if (!point.isNull && hasVisibleSeries) {
				point.onMouseOver();
			} else {
				if (chart.tooltip) {
					chart.tooltip.hide(0);
				}
			}
		};

		this.setChart = function (chart){
			this.chart = chart;
		};

		this.playSonify = function () {
			var chart = this.chart;
		    if (!chart.sonification.timeline || chart.sonification.timeline.atStart()) {
		    	var seriesOptions = this.getInstrumentOptions()
		        chart.sonify({
		            duration:  this.chart.userOptions.accessibility.duration *1000,
		            order: 'simultaneous',
		            pointPlayTime: 'x',
		            seriesOptions: seriesOptions,
		            onEnd: function () {
		                if (chart.sonification.timeline) {
		                    delete chart.sonification.timeline;
		                }
		            }

		        });
		    } else {
		        chart.resumeSonify();
		    }
		};

		this.pauseSonify = function () {
		    this.chart.pauseSonify();
		};
		this.rewindSonify = function () {
		    this.chart.rewindSonify();
		};
		this.cancelSonify = function () {
			//this is for speed
		    this.chart.cancelSonify();
		};


	});

})();
