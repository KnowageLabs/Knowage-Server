/*
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

Ext.ns("Sbi.cockpit.widgets.chart");

Sbi.cockpit.widgets.chart.DefaultChartDimensionRetrieverStrategy = function(config) {

	var WIDTH_TO_HEIGHT_RATIO = 1.3;

	return {

		getChartDimension : function (chart) {

			var series = chart.getSeries();
			var categories = chart.getCategories();
			var legendFontSize = chart.legendFontSize;
			var showlegend = chart.getCustomConfiguration().showlegend;

			var width = '100%';
			var height = '100%';

			if (chart.getCustomConfiguration().wtype = 'piechart'){
				width = '100%';
				height = (Ext.isIE)?'80%' : '99%';
			}

			var seriesNumber = series.length;
			var categoriesNumber = categories.length;

			var maxSerieNameInLength = this.getMaxSerieNameInLength(series);
			var maxCategoryLength = this.getMaxCategoryLength(categories);

			if (seriesNumber > 20) {
				var heightNum = seriesNumber * legendFontSize * 2;
				var minWidthNum = Math.round(heightNum * WIDTH_TO_HEIGHT_RATIO);
				var widthNum = Math.max(minWidthNum, maxCategoryLength * 10 * categoriesNumber);
				if (showlegend) {
					widthNum = widthNum + maxSerieNameInLength * legendFontSize;
				}
				height = heightNum + 'px';
				width = widthNum + 'px';
			}

			var size = {};
			size.width = width;
			size.height = height;

//			if (Ext.isIE && size.height == '100%') {
//				//set the height if ie
//				size.height = '150px';//'400px';
//	//			size.width = '';
//			}

	//		alert('width : ' + size.width);
	//		alert('height : ' + size.height);
			return size;
		}

		, getMaxSerieNameInLength : function (series) {
			var toReturn = 0;
			for (var i = 0; i < series.length; i++) {
				var aSerie = series[i];
				if (aSerie.name.length > toReturn) {
					toReturn = aSerie.name.length;
				}
			}
			return toReturn;
		}

		, getMaxCategoryLength : function (categories) {
			var toReturn = 0;
			for (var i = 0; i < categories.length; i++) {
				var aCategory = categories[i];
				var words = aCategory.split(" ");
				for (var j = 0; j < words.length; j++) {
					var aWord = words[j];
					if (aWord.length > toReturn) {
						toReturn = aWord.length;
					}
				}
			}
			return toReturn;
		}

	}

};