/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

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