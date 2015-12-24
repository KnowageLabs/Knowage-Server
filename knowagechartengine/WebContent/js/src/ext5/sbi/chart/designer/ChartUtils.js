Ext.define('Sbi.chart.designer.ChartUtils', {
	extend : 'Ext.Base',
	alternateClassName : ['ChartUtils'],

	statics : {
		ddGroupMeasure : 'MEASURE',
		ddGroupAttribute : 'ATTRIBUTE',
		globThis : this,

		isCockpitEngine : false,
		
		/**
		 * All XML style properties that are unwanted (on the 'blacklist')
		 * and that can be found inside of the XML style file. The code will
		 * later take care of those unwanted properties (if there are any in
		 * the style file) in a manner of removing them from the JSON object
		 * that will represent the template with which we are going to merge
		 * the JSON structure of our current chart object (document). These
		 * properties are unwanted and it is recommended for user not to 
		 * specify them since they should not be taken into account as a part
		 * of the style. However if user specify them, the code will remove 
		 * them. 
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		unwantedStyleProps: ["text"],

		setCockpitEngine : function (isCockpit) {
			Sbi.chart.designer.ChartUtils.isCockpitEngine = isCockpit;
		},
		convertJsonAxisObjToAxisData : function (axis) {
			var result = {};
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType().toUpperCase();

			result['id'] = axis.alias && axis.alias != '' ? axis.alias : '';
			result['alias'] = axis.alias && axis.alias != '' ? axis.alias : '';
			result['axisType'] = axis.type && axis.type != '' ? axis.type : '';
			result['position'] = axis.position && axis.position != '' ? axis.position : '';

			// This is excessive (danristo)
//			result['id'] = axis.alias && axis.alias != '' ? axis.alias
//				 : '';
//			result['alias'] = axis.alias && axis.alias != '' ? axis.alias
//				 : '';
//			result['axisType'] = axis.type && axis.type != '' ? axis.type
//				 : '';
//			result['position'] = axis.position
//				 && axis.position != '' ? axis.position : '';

			/**
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType == "GAUGE") {
				result['min'] = axis.min ? axis.min : '';
				result['max'] = axis.max ? axis.max : '';
				result['lineColor'] = axis.lineColor ? axis.lineColor : '';
				result['tickPosition'] = axis.tickPosition ? axis.tickPosition : '';
				result['tickColor'] = axis.tickColor ? axis.tickColor : '';
				result['minorTickLength'] = axis.minorTickLength ? axis.minorTickLength : '';
				//result['offset'] = (axis.offset != undefined && axis.offset != null) ? axis.offset : 0;
				result['lineWidth'] = axis.lineWidth ? axis.lineWidth : '';
				result['endOnTickGauge'] = axis.endOnTickGauge;
				result['minorTickInterval'] = axis.minorTickInterval ? axis.minorTickInterval : '';
				result['minorTickPosition'] = axis.minorTickPosition ? axis.minorTickPosition : '';
				result['minorTickWidth'] = axis.minorTickWidth ? axis.minorTickWidth : '';
				result['minorTickColor'] = axis.minorTickColor ? axis.minorTickColor : '';
				result['tickPixelInterval'] = axis.tickPixelInterval ? axis.tickPixelInterval : '';
				result['tickWidth'] = axis.tickWidth ? axis.tickWidth : '';
				result['tickLength'] = axis.tickLength ? axis.tickLength : '';
//				result['distance'] = axis.distance ? axis.distance : '';
//				result['rotation'] = axis.rotation ? axis.rotation : '';				
			}

			var axisStyleAsMap = ChartUtils.jsonizeStyle(axis.style);
//			console.log(axisStyleAsMap);
			result['styleRotate'] = axisStyleAsMap.rotate && axisStyleAsMap.rotate != '' ? axisStyleAsMap.rotate : '';
			result['styleAlign'] = axisStyleAsMap.align && axisStyleAsMap.align != '' ? axisStyleAsMap.align : '';
			result['styleColor'] = axisStyleAsMap.color && axisStyleAsMap.color != '' ? axisStyleAsMap.color : '';
			
			/**
			 * The problem appeared when introducing axis style configuration feature to 
			 * the CHORD chart. It has the "fontFamily" property, instead of "font" property.
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			if (axisStyleAsMap.font)
			{
				result['styleFont'] = axisStyleAsMap.font && axisStyleAsMap.font != '' ? axisStyleAsMap.font : '';
			}
			else if (axisStyleAsMap.fontFamily)
			{
				result['styleFont'] = axisStyleAsMap.fontFamily && axisStyleAsMap.fontFamily != '' ? axisStyleAsMap.fontFamily : '';
			}			
			
			result['styleFontWeigh'] = axisStyleAsMap.fontWeight && axisStyleAsMap.fontWeight != '' ? axisStyleAsMap.fontWeight : '';
			result['styleFontSize'] = axisStyleAsMap.fontSize && axisStyleAsMap.fontSize != '' ? axisStyleAsMap.fontSize : '';

			result['styleOpposite'] = axisStyleAsMap.opposite;

			if (axis.MAJORGRID) {
				result['majorgridInterval'] = axis.MAJORGRID.interval && axis.MAJORGRID.interval != '' ? axis.MAJORGRID.interval : '';

				var majorgridStyleAsMap = ChartUtils
					.jsonizeStyle(axis.MAJORGRID.style);
				result['majorgridStyleTypeline'] = majorgridStyleAsMap.typeline
					 && majorgridStyleAsMap.typeline != '' ? majorgridStyleAsMap.typeline
					 : '';
				result['majorgridStyleColor'] = majorgridStyleAsMap.color
					 && majorgridStyleAsMap.color != '' ? majorgridStyleAsMap.color
					 : '';
			}
			if (axis.MINORGRID) {
				var minorgridStyleAsMap = ChartUtils
					.jsonizeStyle(axis.MINORGRID.style);
				result['minorgridInterval'] = axis.MINORGRID.interval
					 && axis.MINORGRID.interval != '' ? axis.MINORGRID.interval
					 : '';
				result['minorgridStyleTypeline'] = minorgridStyleAsMap.typeline
					 && minorgridStyleAsMap.typeline != '' ? minorgridStyleAsMap.typeline
					 : '';
				result['minorgridStyleColor'] = minorgridStyleAsMap.color
					 && minorgridStyleAsMap.color != '' ? minorgridStyleAsMap.color
					 : '';
			}

			if (axis.TITLE) {
				result['titleText'] = axis.TITLE.text
					 && axis.TITLE.text != '' ? axis.TITLE.text
					 : '';

				var titlegridStyleAsMap = ChartUtils
					.jsonizeStyle(axis.TITLE.style);
				
				if (titlegridStyleAsMap.font)
				{
					result['titleStyleFont'] = titlegridStyleAsMap.font
					 && titlegridStyleAsMap.font != '' ? titlegridStyleAsMap.font
					 : '';
				}
				else if (titlegridStyleAsMap.fontFamily)
				{
					result['titleStyleFont'] = titlegridStyleAsMap.fontFamily
					 && titlegridStyleAsMap.fontFamily != '' ? titlegridStyleAsMap.fontFamily
					 : '';
				}
				
				result['titleStyleAlign'] = titlegridStyleAsMap.align
					 && titlegridStyleAsMap.align != '' ? titlegridStyleAsMap.align
					 : '';
				result['titleStyleColor'] = titlegridStyleAsMap.color
					 && titlegridStyleAsMap.color != '' ? titlegridStyleAsMap.color
					 : '';				
				result['titleStyleFontWeigh'] = titlegridStyleAsMap.fontWeight
					 && titlegridStyleAsMap.fontWeight != '' ? titlegridStyleAsMap.fontWeight
					 : '';
				result['titleStyleFontSize'] = titlegridStyleAsMap.fontSize
					 && titlegridStyleAsMap.fontSize != '' ? titlegridStyleAsMap.fontSize
					 : '';
			}

			/**
			 * Specific for the GAUGE chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (axis.LABELS) {
				result['distance'] = (axis.LABELS.distance != undefined && axis.LABELS.distance != null || axis.LABELS.distance === 0) ? axis.LABELS.distance : "";
				result['rotation'] = (axis.LABELS.rotation != undefined && axis.LABELS.rotation != null || axis.LABELS.rotation === 0) ? axis.LABELS.rotation : "";
			}

			/**
			 * Specific for the GAUGE chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (axis.PLOTBANDS) {
				
				var from = new Array();
				var to = new Array();
				var color = new Array();

				if (axis.PLOTBANDS.PLOT.length) {
					for (var i = 0; i < axis.PLOTBANDS.PLOT.length; i++) {
						from.push(axis.PLOTBANDS.PLOT[i].from);
						to.push(axis.PLOTBANDS.PLOT[i].to);
						color.push(axis.PLOTBANDS.PLOT[i].color);
					}
				} else {
					from.push(axis.PLOTBANDS.PLOT.from);
					to.push(axis.PLOTBANDS.PLOT.to);
					color.push(axis.PLOTBANDS.PLOT.color);
				}

				result['from'] = from ? from : "";
				result['to'] = to ? to : "";
				result['color'] = color ? color : "";
			}
			
			return result;
		},
		createEmptyAxisData : function (isCategory, isLeftSerie) {
			isCategory = isCategory || false;
			isLeftSerie = isLeftSerie || false;

			var result = {};

			result['id'] = ChartColumnsContainerManager.COLUMNS_CONTAINER_ID_PREFIX
				 + ChartColumnsContainerManager.instanceIdFeed;
			/* Increment the axis instance ID counter for the next one (Danilo Ristovski) */
			result['alias'] = ChartColumnsContainerManager.COLUMNS_CONTAINER_ID_PREFIX
				 + ChartColumnsContainerManager.instanceIdFeed++;
			result['axisType'] = isCategory ? 'Category'
				 : 'Serie';
			result['position'] = isCategory ? 'bottom'
				 : isLeftSerie ? 'left' : 'right';

			result['styleRotate'] = '';
			result['styleAlign'] = '';
			result['styleColor'] = '';
			result['styleFont'] = '';
			result['styleFontWeigh'] = '';
			result['styleFontSize'] = '';

			if (isCategory) {
				result['majorgridInterval'] = '';
				result['majorgridStyleTypeline'] = '';
				result['majorgridStyleColor'] = '';
				result['minorgridInterval'] = '';
				result['minorgridStyleTypeline'] = '';
				result['minorgridStyleColor'] = '';
			}

			result['titleText'] = '';
			result['titleStyleAlign'] = '';
			result['titleStyleColor'] = '';
			result['titleStyleFont'] = '';
			result['titleStyleFontWeigh'] = '';
			result['titleStyleFontSize'] = '';

			return result;
		},

		exportAsJson : function (chartModel) {
			var result = {};
			var CHART = {};
//console.log("exportAsJson (START)");
			CHART['type'] = Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType();

			var chartData = ChartUtils
				.getChartDataAsOriginaJson(chartModel);

			Ext.apply(CHART, chartData);

			var AXES_LIST = {};

			var AXIS = ChartUtils.getAxesDataAsOriginalJson();
			
			/**
			 * Only for the PARALLEL chart type we will have some properties (attributes) for
			 * the AXES_LIST tag.
			 * (danilo.ristovski@mht.net)
			 */
			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == "PARALLEL") {
				
				var axesList = "";

				axesList += 'axisColNamePadd:'
				 + ((Number(chartModel.get('axisColNamePadd'))) ? Number(chartModel
					.get('axisColNamePadd'))
					 : '') + ';';

				axesList += 'brushWidth:'
				 + (Number((chartModel.get('brushWidth'))) ? Number(chartModel
					.get('brushWidth'))
					 : '') + ';';

				/**
				 * Checking if the specified value for is 'transparent' is important since we must not 
				 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
				 * 
				 * @modifiedBy: danristo (danilo.ristovski@mht.net)
				 */
				axesList += 'axisColor:'
				 + ((chartModel.get('axisColor') != undefined && chartModel
						.get('axisColor') != '' && chartModel.get('axisColor') != 'transparent') ? '#'
					 + chartModel.get('axisColor')
					 : '') + ';';

				axesList += 'brushColor:'
				 + ((chartModel.get('brushColor') != undefined && chartModel
						.get('brushColor') != '' && chartModel.get('brushColor') != 'transparent') ? '#'
					 + chartModel.get('brushColor')
					 : '') + ';';

				AXES_LIST['style'] = axesList;
				CHART['AXES_LIST'] = AXES_LIST;
			}

//			console.log(AXIS);

			/**
			 * Only for the SCATTER chart type we will need these three parameters
			 * (danilo.ristovski@mht.net)
			 */
			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == "SCATTER") {
				AXIS[1]['startOnTick'] = chartModel.get('scatterStartOnTick');
				AXIS[1]['endOnTick'] = chartModel.get('scatterEndOnTick');
				AXIS[1]['showLastLabel'] = chartModel.get('scatterShowLastLabel');
			}

			AXES_LIST['AXIS'] = AXIS;
			CHART['AXES_LIST'] = AXES_LIST;

			var VALUES = {};

			var SERIE = ChartUtils
				.getSeriesDataAsOriginalJson();

			if (SERIE.length > 0) {
				VALUES['SERIE'] = SERIE;
			}
			var CATEGORY = ChartUtils
				.getCategoriesDataAsOriginalJson();

			if (CATEGORY && CATEGORY != null) {
				VALUES['CATEGORY'] = CATEGORY;
			}
			if (Object.keys(VALUES).length !== 0) {
				CHART['VALUES'] = VALUES;
			}
			
			// Exporting Cross navigation data
			var crossNavigationData = Sbi.chart.designer.Designer.crossNavigationPanel.getCrossNavigationData();
			if(crossNavigationData) {
				CHART['DRILL'] = crossNavigationData;
			}

			result['CHART'] = CHART;

//			console.log("-- After saving the chart: --");
			//console.log(result);
			//console.log("exportAsJson (END)");
			return result;
		},

		getAxesDataAsOriginalJson : function () {
			var result = [];
			//console.log("getAxesDataAsOriginalJson (START)");
			
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType().toUpperCase();

			/* START Chart left and right axes data */
			var leftAndRightAxisesContainers = [
				Ext.getCmp('chartLeftAxisesContainer'),
				Ext.getCmp('chartRightAxisesContainer')];

			for (containerIndex in leftAndRightAxisesContainers) {
				var axisContainer = leftAndRightAxisesContainers[containerIndex];

				var axisContainerItems = axisContainer.items.items;
				for (index in axisContainerItems) {
					var axis = axisContainerItems[index];
					var axisData = axis.axisData;
					var axisAsJson = {};

					axisAsJson['id'] = axisData.id;
					axisAsJson['alias'] = axisData.alias;
					axisAsJson['type'] = axisData.axisType ? axisData.axisType
						 : '';
					axisAsJson['position'] = axisData.position ? axisData.position
						 : '';

					/**
					 * (danilo.ristovski@mht.net)
					 */
					if (chartType == "GAUGE") {
						axisAsJson['min'] = Number(axisData.min) ? Number(axisData.min) : '';
						axisAsJson['max'] = Number(axisData.max) ? Number(axisData.max) : '';
						axisAsJson['lineColor'] = axisData.lineColor ? axisData.lineColor : '';
						axisAsJson['tickPosition'] = axisData.tickPosition ? axisData.tickPosition : '';
						axisAsJson['tickColor'] = axisData.tickColor ? axisData.tickColor : '';
//						axisAsJson['rotation'] = Number(axisData.rotation) ? Number(axisData.rotation) : '';
//						axisAsJson['distance'] = Number(axisData.distance) ? Number(axisData.distance) : '';
						axisAsJson['minorTickLength'] = Number(axisData.minorTickLength) ? Number(axisData.minorTickLength) : '';
						//axisAsJson['offset'] = Number(axisData.offset) ? Number(axisData.offset) : '';
						axisAsJson['lineWidth'] = Number(axisData.lineWidth) ? Number(axisData.lineWidth) : '';
						axisAsJson['endOnTickGauge'] = axisData.endOnTickGauge;
						axisAsJson['minorTickInterval'] = Number(axisData.minorTickInterval) ? Number(axisData.minorTickInterval) : '';
						axisAsJson['minorTickPosition'] = axisData.minorTickPosition ? axisData.minorTickPosition : '';
						axisAsJson['minorTickWidth'] = Number(axisData.minorTickWidth) ? Number(axisData.minorTickWidth) : '';
						axisAsJson['minorTickColor'] = axisData.minorTickColor ? axisData.minorTickColor : '';
						axisAsJson['tickPixelInterval'] = Number(axisData.tickPixelInterval) ? Number(axisData.tickPixelInterval) : '';
						axisAsJson['tickWidth'] = Number(axisData.tickWidth) ? Number(axisData.tickWidth) : '';
						axisAsJson['tickLength'] = Number(axisData.tickLength) ? Number(axisData.tickLength) : '';
					}

					var style = '';
					style += 'rotate:'
					 + ((axisData.styleRotate != undefined) ? axisData.styleRotate
						 : '') + ';';
					style += 'align:'
					 + ((axisData.styleAlign != undefined) ? axisData.styleAlign
						 : '') + ';';
					style += 'color:'
					 + ((axisData.styleColor != undefined) ? axisData.styleColor
						 : '') + ';';
					style += 'fontFamily:'
					 + ((axisData.styleFont != undefined) ? axisData.styleFont
						 : '') + ';';
					style += 'fontWeight:'
					 + ((axisData.styleFontWeigh != undefined) ? axisData.styleFontWeigh
						 : '') + ';';
					style += 'fontSize:'
					 + ((axisData.styleFontSize != undefined) ? axisData.styleFontSize
						 : '') + ';';

					/**
					 * (added by: danilo.ristovski@mht.net)
					 */
					style += 'opposite:'
					 + ((axisData.styleOpposite != undefined) ? axisData.styleOpposite
						 : 'false') + ';';

					axisAsJson['style'] = style;

					var MAJORGRID = {};
					MAJORGRID['interval'] = axisData.majorgridInterval ? axisData.majorgridInterval
						 : '';
					var majorgridStyle = '';
					majorgridStyle += 'typeline:'
					 + ((axisData.majorgridStyleTypeline != undefined) ? axisData.majorgridStyleTypeline
						 : '') + ';';

					/**
					 * Fixes the problem of MAJORGRID not shown on SCATTER
					 * (danilo.ristovski@mht.net)
					 */
					majorgridStyle += 'color:'
					 + ((axisData.majorgridStyleColor != undefined) ? axisData.majorgridStyleColor
						 : '#D8D8D8') + ';';

					MAJORGRID['style'] = majorgridStyle;
					axisAsJson['MAJORGRID'] = MAJORGRID;

					var MINORGRID = {};
					MINORGRID['interval'] = axisData.minorgridInterval ? axisData.minorgridInterval
						 : '';
					var minorgridStyle = '';
					minorgridStyle += 'typeline:'
					 + ((axisData.minorgridStyleTypeline != undefined) ? axisData.minorgridStyleTypeline
						 : '') + ';';
					minorgridStyle += 'color:'
					 + ((axisData.minorgridStyleColor != undefined) ? axisData.minorgridStyleColor
						 : '#E0E0E0') + ';';
					MINORGRID['style'] = minorgridStyle;
					axisAsJson['MINORGRID'] = MINORGRID;

					var TITLE = {};
					TITLE['text'] = axisData.titleText ? axisData.titleText
						 : '';
					var titleStyle = '';
					titleStyle += 'align:'
					 + ((axisData.titleStyleAlign != undefined) ? axisData.titleStyleAlign
						 : '') + ';';
					titleStyle += 'color:'
					 + ((axisData.titleStyleColor != undefined) ? axisData.titleStyleColor
						 : '') + ';';
					titleStyle += 'fontFamily:'
					 + ((axisData.titleStyleFont != undefined) ? axisData.titleStyleFont
						 : '') + ';';
					titleStyle += 'fontWeight:'
					 + ((axisData.titleStyleFontWeigh != undefined) ? axisData.titleStyleFontWeigh
						 : '') + ';';
					titleStyle += 'fontSize:'
					 + ((axisData.titleStyleFontSize != undefined) ? axisData.titleStyleFontSize
						 : '') + ';';

					TITLE['style'] = titleStyle;
					axisAsJson['TITLE'] = TITLE;

					/**
					 * (danilo.ristovski@mht.net)
					 */
					if (chartType == "GAUGE") {
						/**
						 * LABELS sub-tag inside the AXIS tag
						 */
						var LABELS = {};

						LABELS['distance'] = (axisData.distance != undefined && axisData.distance != null || (axisData.distance === 0 && axisData.distance != null)) ? axisData.distance : "";
						LABELS['rotation'] = (axisData.rotation != undefined && axisData.rotation != null || (axisData.rotation === 0 && axisData.rotation != null)) ? axisData.rotation : "";

						axisAsJson['LABELS'] = LABELS;

						/**
						 * PLOTBANDS sub-tag inside the AXIS tag
						 */
						var PLOTBANDS = {};
						var PLOT = new Array();
						
						if (axisData.from && axisData.to && axisData.from!="" && axisData.to!="")
						{
							if (axisData.from && axisData.from.length) {
								var numberOfPlots = axisData.from.length;

								for (var i = 0; i < numberOfPlots; i++) {
									var object = {};

									object['from'] = axisData.from[i];
									object['to'] = axisData.to[i]; 
									object['color'] = axisData.color[i] ? axisData.color[i] : '';

									PLOT.push(object);
								}

							} else {
								var object = {};

								object['from'] = axisData.from; 
								object['to'] = axisData.to; 
								object['color'] = axisData.color ? axisData.color : '';

								PLOT.push(object);
							}

							PLOTBANDS['PLOT'] = PLOT;

							axisAsJson['PLOTBANDS'] = PLOTBANDS;
						}
						
					}

					result.push(axisAsJson);
				}
			}
			/* END Chart left and right axes data */

			/* START Chart bottom axis data */
			if (chartType != "GAUGE") {
				var axisData = Ext.getCmp('chartBottomCategoriesContainer').axisData;
				var axisAsJson = {};
				
				axisAsJson['id'] = axisData.id;
				axisAsJson['alias'] = axisData.alias;
				axisAsJson['type'] = axisData.axisType;
				axisAsJson['position'] = axisData.position;

				var style = '';
				style += 'rotate:'
				 + ((axisData.styleRotate != undefined) ? axisData.styleRotate
					 : '') + ';';
				style += 'align:'
				 + ((axisData.styleAlign != undefined) ? axisData.styleAlign
					 : '') + ';';
				style += 'color:'
				 + ((axisData.styleColor != undefined) ? axisData.styleColor
					 : '') + ';';
				style += 'fontFamily:'
				 + ((axisData.styleFont != undefined) ? axisData.styleFont
					 : '') + ';';
				style += 'fontWeight:'
				 + ((axisData.styleFontWeigh != undefined) ? axisData.styleFontWeigh
					 : '') + ';';
				style += 'fontSize:'
				 + ((axisData.styleFontSize != undefined) ? axisData.styleFontSize
					 : '') + ';';
				axisAsJson['style'] = style;

				var TITLE = {};
				TITLE['text'] = (axisData.titleText != undefined) ? axisData.titleText
				 : '';

				var titleStyle = '';
				titleStyle += 'align:'
				 + ((axisData.titleStyleAlign != undefined) ? axisData.titleStyleAlign
					 : '') + ';';
				titleStyle += 'color:'
				 + ((axisData.titleStyleColor != undefined) ? axisData.titleStyleColor
					 : '') + ';';
				titleStyle += 'fontFamily:'
				 + ((axisData.titleStyleFont != undefined) ? axisData.titleStyleFont
					 : '') + ';';
				titleStyle += 'fontWeight:'
				 + ((axisData.titleStyleFontWeigh != undefined) ? axisData.titleStyleFontWeigh
					 : '') + ';';
				titleStyle += 'fontSize:'
				 + ((axisData.titleStyleFontSize != undefined) ? axisData.titleStyleFontSize
					 : '') + ';';

				TITLE['style'] = titleStyle;
				axisAsJson['TITLE'] = TITLE;

				result.push(axisAsJson);
				/* END Chart bottom axis data */
			}
			//console.log("getAxesDataAsOriginalJson (END)");
			return result;
		},

		getSeriesDataAsOriginalJson : function () {
			var result = [];
			//console.log("getSeriesDataAsOriginalJson (START)");
			var serieStores = Sbi.chart.designer.ChartColumnsContainerManager.storePool;

			for (storeIndex in serieStores) {
				var store = serieStores[storeIndex];
				var axisAlias = store.axisAlias;

				var storeSerieDataLength = store.data.items.length;
				for (var rowIndex = 0; rowIndex < storeSerieDataLength; rowIndex++) {
					var serieAsMap = store.getAt(rowIndex);
					var serie = {};

					serie['id'] = serieAsMap.get('id') != undefined ? serieAsMap
						.get('id')
						 : '';

					serie['axis'] = axisAlias;
					serie['color'] = serieAsMap
						.get('serieColor') != undefined ? serieAsMap
						.get('serieColor')
						 : '';
					serie['column'] = serieAsMap
						.get('serieColumn') != undefined ? serieAsMap
						.get('serieColumn')
						 : '';
					serie['groupingFunction'] = serieAsMap
						.get('serieGroupingFunction') != undefined ? serieAsMap
						.get('serieGroupingFunction')
						 : '';
					serie['name'] = serieAsMap.get('axisName') != undefined ? serieAsMap
						.get('axisName')
						 : '';
					serie['orderType'] = serieAsMap
						.get('serieOrderType') != undefined ? serieAsMap
						.get('serieOrderType')
						 : '';
					serie['postfixChar'] = serieAsMap
						.get('seriePostfixChar') != undefined ? serieAsMap
						.get('seriePostfixChar')
						 : '';
					serie['precision'] = serieAsMap
						.get('seriePrecision') != undefined ? serieAsMap
						.get('seriePrecision')
						 : '';
					serie['prefixChar'] = serieAsMap
						.get('seriePrefixChar') != undefined ? serieAsMap
						.get('seriePrefixChar')
						 : '';
					serie['showValue'] = serieAsMap
						.get('serieShowValue') != undefined ? serieAsMap
						.get('serieShowValue')
						 : '';
					serie['type'] = serieAsMap.get('serieType') != undefined ? serieAsMap
						.get('serieType')
						 : '';

					var chartType = Sbi.chart.designer.Designer.chartTypeSelector
						.getChartType();

					/**
					 * (if-checking added by: danilo.ristovski@mht.net)
					 */
					// TODO: Check if this should be excluded also for other chart types
					if (chartType.toUpperCase() != "SUNBURST"
						 && chartType.toUpperCase() != "WORDCLOUD") {

						var TOOLTIP = {};

						TOOLTIP['backgroundColor'] = serieAsMap
							.get('serieTooltipBackgroundColor') != undefined ? serieAsMap
							.get('serieTooltipBackgroundColor')
							 : '';

						TOOLTIP['templateHtml'] = serieAsMap
							.get('serieTooltipTemplateHtml') != undefined ? serieAsMap
							.get('serieTooltipTemplateHtml')
							 : '';

						var tooltipStyle = '';
						tooltipStyle += 'color:'
						 + ((serieAsMap
								.get('serieTooltipColor') != undefined) ? serieAsMap
							.get('serieTooltipColor')
							 : '') + ';';						
						tooltipStyle += 'fontFamily:'
						 + ((serieAsMap
								.get('serieTooltipFont') != undefined) ? serieAsMap
							.get('serieTooltipFont')
							 : '') + ';';
						tooltipStyle += 'fontWeight:'
						 + ((serieAsMap
								.get('serieTooltipFontWeight') != undefined) ? serieAsMap
							.get('serieTooltipFontWeight')
							 : '') + ';';
						tooltipStyle += 'fontSize:'
						 + ((serieAsMap
								.get('serieTooltipFontSize') != undefined) ? serieAsMap
							.get('serieTooltipFontSize')
							 : '') + ';';

						tooltipStyle += 'align:'
						 + ((serieAsMap.get('serieTooltipAlign') != undefined) ? serieAsMap.get('serieTooltipAlign')
							 : '') + ';';
						TOOLTIP['style'] = tooltipStyle;
						
						serie['TOOLTIP'] = TOOLTIP;
					}

					/**
					 * For GAUGE chart type add DIAL and DATA LABELS sub-tags
					 * (danilo.ristovski@mht.net)
					 */
					if (chartType.toUpperCase() == "GAUGE") {
						var DIAL = {};
						DIAL['backgroundColorDial'] = serieAsMap.get("backgroundColorDial") ? serieAsMap.get("backgroundColorDial") : "";
						serie['DIAL'] = DIAL;

						var DATA_LABELS = {};
						DATA_LABELS['yPositionDataLabels'] = serieAsMap.get("yPositionDataLabels") ? serieAsMap.get("yPositionDataLabels") : "";
						DATA_LABELS['colorDataLabels'] = serieAsMap.get("colorDataLabels") ? serieAsMap.get("colorDataLabels") : "";
						DATA_LABELS['formatDataLabels'] = serieAsMap.get("formatDataLabels") ? serieAsMap.get("formatDataLabels") : "";
						serie['DATA_LABELS'] = DATA_LABELS;
					}

					result.push(serie);
				}
			}
			console.log("getSeriesDataAsOriginalJson (END)");
			return result;
		},

		getCategoriesDataAsOriginalJson : function () {//console.log("getCategoriesDataAsOriginalJson (START)");
			var categoriesStore = Ext.data.StoreManager
				.lookup('categoriesStore');

			var chartType = Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType();

			var result = [];

			/**
			 * Enabling multiply categories for following chart types.
			 * 
			 * @author: danristo (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SUNBURST"
				 || chartType.toUpperCase() == "WORDCLOUD"
				 || chartType.toUpperCase() == "TREEMAP"
				 || chartType.toUpperCase() == "PARALLEL"
				 || chartType.toUpperCase() == "HEATMAP"
				 || chartType.toUpperCase() == "CHORD") {
				for (var i = 0; i < categoriesStore.data.length; i++) {
					var mainCategory = categoriesStore.getAt(i);

					if (mainCategory == null) {
						continue;
					}

					var category = {};

					category['name'] = mainCategory
						.get('axisName') != undefined ? mainCategory
						.get('axisName')
						 : mainCategory
						.get('categoryColumn');
					category['column'] = mainCategory
						.get('categoryColumn') != undefined ? mainCategory
						.get('categoryColumn')
						 : '';
					category['orderColumn'] = mainCategory
						.get('categoryOrderColumn') != undefined ? mainCategory
						.get('categoryOrderColumn')
						 : '';
					category['orderType'] = mainCategory
						.get('categoryOrderType') != undefined ? mainCategory
						.get('categoryOrderType')
						 : '';
					category['stackedType'] = mainCategory
						.get('categoryOrderType') != undefined ? mainCategory
						.get('categoryOrderType')
						 : '';
					category['stacked'] = mainCategory
						.get('categoryStacked') != undefined ? mainCategory
						.get('categoryStacked')
						 : '';
//					category['categoryDataType'] = mainCategory
//							.get('colType') != undefined ? mainCategory
//							.get('colType')
//							: '';
//					console.log(mainCategory);
//					console.log(mainCategory.get('categoryDataType'));
					var categoriesStoreDataLength = categoriesStore.data.items.length;

					var groupby = '';
					var groupbyNames = '';
					if (categoriesStoreDataLength > 1) {
						for (var rowIndex = 1; rowIndex < categoriesStoreDataLength; rowIndex++) {
							var categorieItem = categoriesStore
								.getAt(rowIndex);
							groupby += categorieItem
							.get('categoryColumn') != undefined ? categorieItem
							.get('categoryColumn')
							 + ','
							 : '';
							groupbyNames += categorieItem
							.get('axisName') != undefined ? categorieItem
							.get('axisName')
							 + ','
							 : '';
						}
					}
					category['groupby'] = groupby.replace(
							/\,$/, '');
					category['groupbyNames'] = groupbyNames
						.replace(/\,$/, '');

					result.push(category);
				}
			} else {
				var mainCategory = categoriesStore.getAt(0);

				if (mainCategory == null) {
					return null;
				}

				var result = {};
				result['name'] = mainCategory.get('axisName') != undefined ? mainCategory
					.get('axisName')
					 : mainCategory.get('categoryColumn');
				result['column'] = mainCategory
					.get('categoryColumn') != undefined ? mainCategory
					.get('categoryColumn')
					 : '';
				result['orderColumn'] = mainCategory
					.get('categoryOrderColumn') != undefined ? mainCategory
					.get('categoryOrderColumn')
					 : '';
				result['orderType'] = mainCategory
					.get('categoryOrderType') != undefined ? mainCategory
					.get('categoryOrderType')
					 : '';
				result['stackedType'] = mainCategory
					.get('categoryOrderType') != undefined ? mainCategory
					.get('categoryOrderType')
					 : '';
				result['stacked'] = mainCategory
					.get('categoryStacked') != undefined ? mainCategory
					.get('categoryStacked')
					 : '';

				var categoriesStoreDataLength = categoriesStore.data.items.length;

				var groupby = '';
				var groupbyNames = '';
				if (categoriesStoreDataLength > 1) {
					for (var rowIndex = 1; rowIndex < categoriesStoreDataLength; rowIndex++) {
						var categorieItem = categoriesStore
							.getAt(rowIndex);
						groupby += categorieItem
						.get('categoryColumn') != undefined ? categorieItem
						.get('categoryColumn')
						 + ','
						 : '';
						groupbyNames += categorieItem
						.get('axisName') != undefined ? categorieItem
						.get('axisName')
						 + ','
						 : '';
					}
				}
				result['groupby'] = groupby.replace(/\,$/, '');
				result['groupbyNames'] = groupbyNames.replace(
						/\,$/, ''); ;
			}
			//console.log("getCategoriesDataAsOriginalJson (END)");
			return result;
		},

		getChartDataAsOriginaJson : function (chartModel) {
			var CHART = {};
			//console.log("getChartDataAsOriginaJson (START)");
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();

			CHART['height'] = (chartModel.get('height') != undefined) ? chartModel
			.get('height')
			 : '';
			CHART['width'] = (chartModel.get('width') != undefined) ? chartModel
			.get('width')
			 : '';

			CHART['isCockpitEngine'] = Sbi.chart.designer.ChartUtils.isCockpitEngine;

			/**
			 * This parameter is needed for specifying the name of the style that is applied for
			 * the chart (document). This parameter is common for all the charts.
			 * (danilo.ristovski@mht.net)
			 */
			CHART['styleName'] = Sbi.chart.designer.Designer.styleName;
			//							CHART['styleCustom'] = Sbi.chart.designer.Designer.styleCustom;

			/**
			 * Parameter specific for the SCATTER chart only
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SCATTER") {
				CHART['zoomType'] = (chartModel.get('scatterZoomType')) ? chartModel.get('scatterZoomType') : '';
			}

			/**
			 * Parameter specific for the WORDCLOUD chart only
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "WORDCLOUD") 
			{
				CHART['sizeCriteria'] = (chartModel.get('sizeCriteria')) ? chartModel.get('sizeCriteria') : '';
				CHART['maxWords'] = (Number(chartModel.get('maxWords'))) ? Number(chartModel.get('maxWords')) : 0;
				CHART['maxAngle'] = (Number(chartModel.get('maxAngle'))) ? Number(chartModel.get('maxAngle')) : 0;
				CHART['minAngle'] = (Number(chartModel.get('minAngle'))) ? Number(chartModel.get('minAngle')) : 0;
				CHART['maxFontSize'] = (Number(chartModel.get('maxFontSize'))) ? Number(chartModel.get('maxFontSize')) : 0;
				CHART['wordPadding'] = (Number(chartModel.get('wordPadding'))) ? Number(chartModel.get('wordPadding')) : 0;
			}

			/**
			 * Parameter specific for the SUNBURST chart only.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SUNBURST") {

				CHART['opacMouseOver'] = (Number(chartModel.get('opacMouseOver'))) ? Number(chartModel.get('opacMouseOver')) : 100;
			}

			/**
			 * Parameter specific for the PARALLEL chart only.
			 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "PARALLEL")
			{
				CHART['showTableParallel'] = chartModel.get('showTableParallel') ? chartModel.get('showTableParallel') : false;	
			}
			
			if (chartModel.get('orientation') != undefined) {
				CHART['orientation'] = (chartType.toUpperCase() != 'PIE') ? chartModel
					.get('orientation')
					 : '';
			}

			var chartStyle = '';
			chartStyle += 'fontFamily:'
			 + ((chartModel.get('font') != undefined) ? chartModel
				.get('font')
				 : '') + ';';
			chartStyle += 'fontSize:'
			 + ((chartModel.get('fontDimension') != undefined) ? chartModel
				.get('fontDimension')
				 : '') + ';';
			chartStyle += 'fontWeight:'
			 + ((chartModel.get('fontWeight') != undefined) ? chartModel
				.get('fontWeight')
				 : '') + ';';
			
			/**
			 * Checking if the specified value for the background color of the chart is 'transparent'
			 * is important since we must not concatenate the hashtag sign (#) to the defined color
			 * value (in that case: '#transparent'). The 'transparent' value is useful when user wants
			 * to take predefined background value characteristic for the chart type used, i.e. for
			 * the library that this chart type uses.
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			chartStyle += 'backgroundColor:'
			 + ((chartModel.get('backgroundColor') != undefined && chartModel
					.get('backgroundColor') != '' && chartModel.get('backgroundColor')!="transparent") ? '#'
				 + chartModel.get('backgroundColor')
				 : '') + ';';
			
			CHART['style'] = chartStyle;

			var COLORPALETTE = {};
			var COLOR = [];
			var paletteStore = Ext.data.StoreManager
				.lookup('chartConfigurationPaletteStore');
			var colors = paletteStore.getData();
			for (i in colors.items) {
				var color = paletteStore.getAt(i);
				var colorElement = {};
				colorElement['id'] = color.get('id') != undefined ? color
					.get('id')
					 : '';
				colorElement['gradient'] = color
					.get('gradient') != undefined ? color
					.get('gradient') : '';
				colorElement['name'] = color.get('name') != undefined ? color
					.get('name')
					 : '';
				colorElement['order'] = color.get('order') != undefined ? color
					.get('order')
					 : '';

				var colorValue = color.get('value') != undefined ? color
					.get('value')
					 : '';
				colorValue = colorValue.replace(
						/^#?([\dA-Fa-f]+)/, '#$1');
				colorElement['value'] = colorValue;

				COLOR.push(colorElement);
			}

			COLORPALETTE['COLOR'] = COLOR;
			CHART['COLORPALETTE'] = COLORPALETTE;

			var EMPTYMESSAGE = {};
			EMPTYMESSAGE['text'] = (chartModel.get('nodata') != undefined) ? chartModel
			.get('nodata')
			 : '';

			var emptymessageStyle = '';
			emptymessageStyle += 'align:'
			 + ((chartModel.get('nodataAlign') != undefined) ? chartModel
				.get('nodataAlign')
				 : '') + ';';
			
			/**
			 * Checking if the specified value for is 'transparent' is important since we must not 
			 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			emptymessageStyle += 'color:'
			 + ((chartModel.get('nodataColor') != undefined && chartModel
					.get('nodataColor') != '' && chartModel.get('nodataColor') != 'transparent') ? '#'
				 + chartModel.get('nodataColor')
				 : '') + ';';
			
			emptymessageStyle += 'fontFamily:'
			 + ((chartModel.get('font') != undefined) ? chartModel
				.get('font')
				 : '') + ';';
			emptymessageStyle += 'fontWeight:'
			 + ((chartModel.get('nodataStyle') != undefined) ? chartModel
				.get('nodataStyle')
				 : '') + ';';
			emptymessageStyle += 'fontSize:'
			 + ((chartModel.get('nodataDimension') != undefined) ? chartModel
				.get('nodataDimension')
				 : '') + ';';
			EMPTYMESSAGE['style'] = emptymessageStyle;

			CHART['EMPTYMESSAGE'] = EMPTYMESSAGE;

			var TITLE = {};
			TITLE['text'] = (chartModel.get('title') != undefined) ? chartModel
			.get('title')
			 : '';

			var titleStyle = '';
			titleStyle += 'align:'
			 + ((chartModel.get('titleAlign') != undefined) ? chartModel
				.get('titleAlign')
				 : '') + ';';
			
			/**
			 * Checking if the specified value for is 'transparent' is important since we must not 
			 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
			 * 
			 * @modifiedBy: danristo (danilo.ristovski@mht.net)
			 */
			titleStyle += 'color:'
			 + ((chartModel.get('titleColor') != undefined && chartModel
					.get('titleColor') != ''&& chartModel.get('titleColor') != 'transparent') ? '#'
				 + chartModel.get('titleColor') : '')
			 + ';';
			titleStyle += 'fontFamily:'
			 + ((chartModel.get('titleFont') != undefined) ? chartModel
				.get('titleFont')
				 : '') + ';';
			titleStyle += 'fontWeight:'
			 + ((chartModel.get('titleStyle') != undefined) ? chartModel
				.get('titleStyle')
				 : '') + ';';
			titleStyle += 'fontSize:'
			 + ((chartModel.get('titleDimension') != undefined) ? chartModel
				.get('titleDimension')
				 : '') + ';';

			TITLE['style'] = titleStyle;

			CHART['TITLE'] = TITLE;

			var SUBTITLE = {};
			SUBTITLE['text'] = (chartModel.get('subtitle') != undefined) ? chartModel
			.get('subtitle')
			 : '';

			var subtitleStyle = '';
			subtitleStyle += 'align:'
			 + ((chartModel.get('subtitleAlign') != undefined) ? chartModel
				.get('subtitleAlign')
				 : '') + ';';
			
			 /**
			  * Checking if the specified value for is 'transparent' is important since we must not 
			  * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
			  * 
			  * @modifiedBy: danristo (danilo.ristovski@mht.net)
			  */
			subtitleStyle += 'color:'
			 + ((chartModel.get('subtitleColor') != undefined && chartModel
					.get('subtitleColor') != '' && chartModel.get('subtitleColor') != 'transparent') ? '#'
				 + chartModel.get('subtitleColor')
				 : '') + ';';
			
			subtitleStyle += 'fontFamily:'
			 + ((chartModel.get('subtitleFont') != undefined) ? chartModel
				.get('subtitleFont')
				 : '') + ';';
			subtitleStyle += 'fontWeight:'
			 + ((chartModel.get('subtitleStyle') != undefined) ? chartModel
				.get('subtitleStyle')
				 : '') + ';';
			subtitleStyle += 'fontSize:'
			 + ((chartModel.get('subtitleDimension') != undefined) ? chartModel
				.get('subtitleDimension')
				 : '') + ';';
			SUBTITLE['style'] = subtitleStyle;

			CHART['SUBTITLE'] = SUBTITLE;

			var LEGEND = {};

			/**
			 * LEGEND tag of the PARALLEL chart's XML template (specific for this type of chart)
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "PARALLEL") {
				var TITLE = {};
				var ELEMENT = {};

				var legendTitleStyle = '';
				var legendElementStyle = "";

				/**
				 * Parameters linked to the TITLE subtag of the LEGEND element (tag)
				 * on the PARALLEL chart
				 */
				legendTitleStyle += 'fontFamily:'
				 + ((chartModel.get('parallelLegendTitleFontFamily') != undefined) ? chartModel
					.get('parallelLegendTitleFontFamily')
					 : '') + ';';

				legendTitleStyle += 'fontSize:'
				 + ((chartModel.get('parallelLegendTitleFontSize') != undefined) ? chartModel
					.get('parallelLegendTitleFontSize')
					 : '') + ';';

				legendTitleStyle += 'fontWeight:'
				 + ((chartModel.get('parallelLegendTitleFontWeight') != undefined) ? chartModel
					.get('parallelLegendTitleFontWeight')
					 : '') + ';';

				/**
				 * Parameters linked to the ELEMENT subtag of the LEGEND element (tag)
				 * on the PARALLEL chart
				 */
				legendElementStyle += 'fontFamily:'
				 + ((chartModel.get('parallelLegendElementFontFamily') != undefined) ? chartModel
					.get('parallelLegendElementFontFamily')
					 : '') + ';';

				legendElementStyle += 'fontSize:'
				 + ((chartModel.get('parallelLegendElementFontSize') != undefined) ? chartModel
					.get('parallelLegendElementFontSize')
					 : '') + ';';

				legendElementStyle += 'fontWeight:'
				 + ((chartModel.get('parallelLegendElementFontWeight') != undefined) ? chartModel
					.get('parallelLegendElementFontWeight')
					 : '') + ';';

				TITLE['style'] = legendTitleStyle;
				ELEMENT['style'] = legendElementStyle;

				LEGEND['TITLE'] = TITLE;
				LEGEND['ELEMENT'] = ELEMENT;

			} else {
				LEGEND['show'] = (chartModel.get('showLegend') != undefined) ? chartModel
				.get('showLegend')
				 : false;
				LEGEND['position'] = (chartModel
					.get('legendPosition') != undefined) ? chartModel
				.get('legendPosition')
				 : '';
				LEGEND['layout'] = (chartModel.get('legendLayout') != undefined) ? chartModel
				.get('legendLayout')
				 : '';
				LEGEND['floating'] = (chartModel
					.get('legendFloating') != undefined) ? chartModel
				.get('legendFloating')
				 : '';
				LEGEND['x'] = (chartModel.get('legendX') != undefined) ? chartModel
				.get('legendX')
				 : '';
				LEGEND['y'] = (chartModel.get('legendY') != undefined) ? chartModel
				.get('legendY')
				 : '';

				/**
				 * If we have a HEATMAP chart we need 'symbolHeight' parameter since it is necessary
				 * for rendering of it's legend (the bar with the interval of 'temperatures'). This
				 * parameter represents the height of the bar (legend).
				 * (danilo.ristovski@mht.net)
				 */
				if (chartType.toUpperCase() == "HEATMAP") {
					LEGEND['symbolHeight'] = (chartModel.get('symbolHeight') != undefined) ? chartModel
					.get('symbolHeight') : 0;
				}

				var legendStyle = '';
				legendStyle += 'align:'
				 + ((chartModel.get('legendAlign') != undefined) ? chartModel
					.get('legendAlign')
					 : '') + ';';
				legendStyle += 'fontFamily:'
				 + ((chartModel.get('legendFont') != undefined) ? chartModel
					.get('legendFont')
					 : '') + ';';
				legendStyle += 'fontSize:'
				 + ((chartModel.get('legendDimension') != undefined) ? chartModel
					.get('legendDimension')
					 : '') + ';';
				legendStyle += 'fontWeight:'
				 + ((chartModel.get('legendStyle') != undefined) ? chartModel
					.get('legendStyle')
					 : '') + ';';
				legendStyle += 'borderWidth:'
				 + ((chartModel.get('legendBorderWidth') != undefined) ? chartModel
					.get('legendBorderWidth')
					 : '') + ';';
				
				/**
				 * Checking if the specified value for is 'transparent' is important since we must not 
				 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
				 * 
				 * @modifiedBy: danristo (danilo.ristovski@mht.net)
				 */
				legendStyle += 'color:'
				 + ((chartModel.get('legendColor') != undefined && chartModel
						.get('legendColor') != '' && chartModel.get('legendColor') != 'transparent') ? '#'
					 + chartModel.get('legendColor')
					 : '') + ';';
				
				legendStyle += 'backgroundColor:'
				 + ((chartModel.get('legendBackgroundColor') != undefined && chartModel
						.get('legendBackgroundColor') != '' && chartModel.get('legendBackgroundColor') != 'transparent') ? '#'
					 + chartModel
					.get('legendBackgroundColor')
					 : '') + ';';

				LEGEND['style'] = legendStyle;
			}

			CHART['LEGEND'] = LEGEND;

			/**
			 * Setting the TOOLBAR and TIP tag inside the XML template of the SUNBURST chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "SUNBURST") {

				var TOOLBAR = {};
				var toolbarStyle = '';

				toolbarStyle += 'position:'
				 + ((chartModel.get('toolbarPosition')) ? chartModel
					.get('toolbarPosition')
					 : '') + ';';
				toolbarStyle += 'height:'
				 + ((Number(chartModel.get('toolbarHeight'))) ? Number(chartModel
					.get('toolbarHeight'))
					 : '') + ';';
				toolbarStyle += 'width:'
				 + (Number((chartModel.get('toolbarWidth'))) ? Number(chartModel
					.get('toolbarWidth'))
					 : '') + ';';
				toolbarStyle += 'spacing:'
				 + (Number((chartModel.get('toolbarSpacing'))) ? Number(chartModel
					.get('toolbarSpacing'))
					 : '') + ';';
				toolbarStyle += 'tail:'
				 + (Number((chartModel.get('toolbarTail'))) ? Number(chartModel
					.get('toolbarTail'))
					 : '') + ';';

				/**
				 * Checking if the specified value for is 'transparent' is important since we must not 
				 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
				 * 
				 * @modifiedBy: danristo (danilo.ristovski@mht.net)
				 */
				toolbarStyle += 'percFontColor:'
				 + ((chartModel
						.get('toolbarPercFontColor') != undefined && chartModel
						.get('toolbarPercFontColor') != '' && chartModel.get('toolbarPercFontColor') != 'transparent') ? '#'
					 + chartModel
					.get('toolbarPercFontColor')
					 : '') + ';';
				
				toolbarStyle += 'fontFamily:'
				 + ((chartModel.get('toolbarFontFamily')) ? chartModel
					.get('toolbarFontFamily')
					 : '') + ';';
				toolbarStyle += 'fontWeight:'
				 + ((chartModel.get('toolbarFontWeight')) ? chartModel
					.get('toolbarFontWeight')
					 : '') + ';';
				toolbarStyle += 'fontSize:'
				 + ((chartModel.get('toolbarFontSize')) ? chartModel
					.get('toolbarFontSize')
					 : '') + ';';

				TOOLBAR['style'] = toolbarStyle;
				CHART['TOOLBAR'] = TOOLBAR;

				var TIP = {};
				var tipStyle = '';

				tipStyle += 'fontFamily:'
				 + ((chartModel.get('tipFontFamily')) ? chartModel
					.get('tipFontFamily')
					 : '') + ';';
				tipStyle += 'fontWeight:'
				 + ((chartModel.get('tipFontWeight')) ? chartModel
					.get('tipFontWeight')
					 : '') + ';';
				tipStyle += 'fontSize:'
				 + ((chartModel.get('tipFontSize')) ? chartModel
					.get('tipFontSize')
					 : '') + ';';
				
				/**
				 * Checking if the specified value for is 'transparent' is important since we must not 
				 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
				 * 
				 * @modifiedBy: danristo (danilo.ristovski@mht.net)
				 */
				tipStyle += 'color:'
				 + ((chartModel.get('tipColor') != undefined && chartModel
						.get('tipColor') != '' && chartModel.get('tipColor') != 'transparent') ? '#'
					 + chartModel.get('tipColor')
					 : '') + ';';
				
				tipStyle += 'align:'
				 + ((chartModel.get('tipAlign')) ? chartModel
					.get('tipAlign')
					 : '') + ';';
				tipStyle += 'width:'
				 + ((chartModel.get('tipWidth')) ? chartModel
					.get('tipWidth')
					 : '') + ';';

				TIP['text'] = (chartModel.get('tipText') != undefined) ? chartModel
				.get('tipText')
				 : '';
				TIP['style'] = tipStyle;
				CHART['TIP'] = TIP;

			}

			/**
			 * Setting the PANE tag inside the XML template of the GAUGE chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "GAUGE") {
				var PANE = {};
				var pane = '';

				PANE['startAngle'] = (chartModel.get('startAnglePane')) ? chartModel.get('startAnglePane') : 0;
				PANE['endAngle'] = (chartModel.get('endAnglePane')) ? chartModel.get('endAnglePane') : 0;

				CHART['PANE'] = PANE;
			}

			/**
			 * Setting the TOOLTIP tag inside the XML template of the HEATMAP chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "HEATMAP") {
				var TOOLTIP = {};
				var tipStyle = '';

				tipStyle += 'fontFamily:'
				 + ((chartModel.get('tipFontFamily')) ? chartModel
					.get('tipFontFamily')
					 : '') + ';';
				tipStyle += 'fontWeight:'
				 + ((chartModel.get('tipFontWeight')) ? chartModel
					.get('tipFontWeight')
					 : '') + ';';
				tipStyle += 'fontSize:'
				 + ((chartModel.get('tipFontSize')) ? chartModel
					.get('tipFontSize')
					 : '') + ';';
				
				/**
				 * Checking if the specified value for is 'transparent' is important since we must not 
				 * concatenate the hash tag sign (#) to the defined color value (in that case: '#transparent').
				 * 
				 * @modifiedBy: danristo (danilo.ristovski@mht.net)
				 */
				tipStyle += 'color:'
				 + ((chartModel.get('tipColor') != undefined && chartModel
						.get('tipColor') != '' && chartModel.get('tipColor') != 'transparent') ? '#'
					 + chartModel.get('tipColor')
					 : '') + ';';
				
				tipStyle += 'align:'
				 + ((chartModel.get('tipAlign')) ? chartModel
					.get('tipAlign')
					 : '') + ';';

				TOOLTIP['style'] = tipStyle;
				CHART['TOOLTIP'] = TOOLTIP;
			}

			/**
			 * Setting the LIMIT and PARALLEL_TOOLTIP tag inside the XML template
			 * of the PARALLEL chart type
			 * (danilo.ristovski@mht.net)
			 */
			if (chartType.toUpperCase() == "PARALLEL") {
				var LIMIT = {};
				var limitStyle = '';

				limitStyle += 'maxNumberOfLines:'
				 + (Number((chartModel.get('maxNumberOfLines'))) ? Number(chartModel
					.get('maxNumberOfLines'))
					 : '') + ';';
				limitStyle += 'serieFilterColumn:'
				 + ((chartModel.get('serieFilterColumn')) ? chartModel
					.get('serieFilterColumn')
					 : '') + ';';
				limitStyle += 'orderTopMinBottomMax:'
				 + ((chartModel
						.get('orderTopMinBottomMax')) ? chartModel
					.get('orderTopMinBottomMax')
					 : '') + ';';

				LIMIT['style'] = limitStyle;
				CHART['LIMIT'] = LIMIT;

				var PARALLEL_TOOLTIP = {};
				var parallelTooltipStype = "";

				parallelTooltipStype += 'fontFamily:'
				 + ((chartModel
						.get('parallelTooltipFontFamily')) ? chartModel
					.get('parallelTooltipFontFamily')
					 : '') + ';';
				parallelTooltipStype += 'fontSize:'
				 + ((chartModel
						.get('parallelTooltipFontSize')) ? chartModel
					.get('parallelTooltipFontSize')
					 : '') + ';';
				parallelTooltipStype += 'minWidth:'
				 + (Number((chartModel
						.get('parallelTooltipMinWidth'))) ? Number(chartModel
					.get('parallelTooltipMinWidth'))
					 : '') + ';';
				parallelTooltipStype += 'maxWidth:'
				 + ((Number(chartModel
						.get('parallelTooltipMaxWidth'))) ? Number(chartModel
					.get('parallelTooltipMaxWidth'))
					 : '') + ';';
				parallelTooltipStype += 'minHeight:'
				 + ((Number(chartModel
						.get('parallelTooltipMinHeight'))) ? Number(chartModel
					.get('parallelTooltipMinHeight'))
					 : '') + ';';
				parallelTooltipStype += 'maxHeight:'
				 + ((Number(chartModel
						.get('parallelTooltipMaxHeight'))) ? Number(chartModel
					.get('parallelTooltipMaxHeight'))
					 : '') + ';';
				parallelTooltipStype += 'padding:'
				 + ((Number(chartModel
						.get('parallelTooltipPadding'))) ? Number(chartModel
					.get('parallelTooltipPadding'))
					 : '') + ';';
				parallelTooltipStype += 'border:'
				 + ((Number(chartModel
						.get('parallelTooltipBorder'))) ? Number(chartModel
					.get('parallelTooltipBorder'))
					 : '') + ';';
				parallelTooltipStype += 'borderRadius:'
				 + ((Number(chartModel
						.get('parallelTooltipBorderRadius'))) ? Number(chartModel
					.get('parallelTooltipBorderRadius'))
					 : '') + ';';

				PARALLEL_TOOLTIP['style'] = parallelTooltipStype;
				CHART['PARALLEL_TOOLTIP'] = PARALLEL_TOOLTIP;
			}
			//console.log("getChartDataAsOriginaJson (END)");
			return CHART;
		},

		jsonizeStyle : function (str) {
			var jsonStyle = {};
//
//			var globThis = this;

			if (str) {
				var styles = str.split(';');
				for (index in styles) {
					var keyValue = styles[index].split(':');
					jsonStyle[keyValue[0]] = keyValue[1];
				}
			}

			return jsonStyle;
		},

		removeStartingHash : function (colorWithHash) {
			return colorWithHash ? colorWithHash.replace("#",
				'') : colorWithHash;
		},

		/**
		 * Methods that will determine whether to enable some later functionalities (such as showing/hiding
		 * of some graphical elements on the Designer's Step 2 tab) depending on the chart type.
		 *
		 * Methods are:
		 * 		- enableToolbarAndTip
		 * 		- enablePalette
		 * 		- disableShowLegendCheck
		 * 		- enableLegend
		 * 		- disableXBottomContainer
		 * 		- disableChartWidth
		 * 		- disableChartOrientation
		 * 		- enableOpacityMouseOver
		 * 		- enableWordcloudPanel
		 * 		- enableParallelPanel
		 * 		- enableScatterElements
		 * 		- enableHeatmapLegendAndTooltip
		 * 		- enableGaugePane
		 *
		 * (danilo.ristovski@mht.net)
		 */
		enableToolbarAndTip : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'SUNBURST';
		},

		enablePalette : function () {				
			return Sbi.chart.designer.Designer.chartTypeSelector.getChartType() != 'WORDCLOUD' && 
				Sbi.chart.designer.Designer.chartTypeSelector.getChartType() != 'GAUGE';
		},

		disableShowLegendCheck : function () {
			//console.log("disableShowLegendCheck (START)");
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'SUNBURST'
			 || Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'WORDCLOUD'
			 || Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'PARALLEL'
			 || Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'TREEMAP'
			 || Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'HEATMAP'
			 || Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'GAUGE'
			|| Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'CHORD';
			//console.log("disableShowLegendCheck (END)");
		},

		enableLegend : function () {
			//console.log("enableLegend (START)");
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'SUNBURST'
			 && Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'WORDCLOUD'
			 && Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'PARALLEL'
			 && Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'TREEMAP'
			 && Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'HEATMAP'
			 && Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'GAUGE'
			&& Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() != 'CHORD';
			//console.log("enableLegend (END)");
		},

		disableXBottomContainer : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'GAUGE';
		},

		/**
		 * If Designer is still not defined (created), i.e. in the process of running (creating) it
		 * we need to prevent 'disableChartWidth' and 'disableChartOrientation' functions from taking
		 * values of the undefined one (Designer = undefined).
		 */

		disableChartWidth : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector.getChartType() == 'SUNBURST';
		},

		disableChartOrientation : function () {
			var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();

			return chartType == 'SUNBURST'
			 || chartType == 'WORDCLOUD'
			 || chartType == 'TREEMAP'
			 || chartType == 'PARALLEL'
			 || chartType == 'HEATMAP'
				 || chartType == 'SCATTER'
					 || chartType == 'GAUGE'
						 || chartType == 'CHORD'
							 || chartType == 'PIE'
								 || chartType == 'RADAR';
		},

		enableOpacityMouseOver : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'SUNBURST';
		},

		enableWordcloudPanel : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'WORDCLOUD';
		},

		enableParallelPanel : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'PARALLEL';
		},

		enableScatterElements : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'SCATTER';
		},

		enableHeatmapLegendAndTooltip : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'HEATMAP';
		},

		enableGaugePane : function () {
			return Sbi.chart.designer.Designer.chartTypeSelector
			.getChartType() == 'GAUGE';
		},

		createChartConfigurationModelFromJson : function (
			jsonTemplate) {

			var jsonChartStyle = Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.style);

			var jsonTitleText = jsonTemplate.CHART.TITLE ? jsonTemplate.CHART.TITLE.text
				 : '';
			var jsonTitleStyle = jsonTemplate.CHART.TITLE ? Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.TITLE.style)
				 : {};

			var jsonSubtitleText = jsonTemplate.CHART.SUBTITLE ? jsonTemplate.CHART.SUBTITLE.text
				 : '';
			var jsonSubtitleStyle = jsonTemplate.CHART.SUBTITLE ? Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.SUBTITLE.style)
				 : {};

			var jsonEmptyMsgText = jsonTemplate.CHART.EMPTYMESSAGE ? jsonTemplate.CHART.EMPTYMESSAGE.text
				 : '';
			var jsonEmptyMsgStyle = jsonTemplate.CHART.EMPTYMESSAGE ? Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.EMPTYMESSAGE.style)
				 : {};

			var chartLegend = jsonTemplate.CHART.LEGEND ? jsonTemplate.CHART.LEGEND
				 : '';
			var jsonLegendStyle = jsonTemplate.CHART.LEGEND ? Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.LEGEND.style)
				 : {};

			/**
			 * Variables used for the SUNBURST chart
			 * (danilo.ristovski@mht.net)
			 */
			var jsonToolbarStyle = jsonTemplate.CHART.TOOLBAR ? Sbi.chart.designer.ChartUtils
				.jsonizeStyle(jsonTemplate.CHART.TOOLBAR.style)
				 : '';

			var jsonTipText = jsonTemplate.CHART.TIP ? jsonTemplate.CHART.TIP.text
				 : '';

			/**
			 * Variables used for the SUNBURST or HEATMAP chart
			 * (danilo.ristovski@mht.net)
			 */
			var jsonTipStyle = {};

			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'SUNBURST') {
				jsonTipStyle = jsonTemplate.CHART.TIP ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.TIP.style)
					 : '';
			} else if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'HEATMAP') {
				jsonTipStyle = jsonTemplate.CHART.TOOLTIP ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.TOOLTIP.style)
					 : '';
			}

			/**
			 * Variables used for the PARALLEL chart (LIMIT, AXES_LIST, PARALLEL_TOOLTIP, LEGEND)
			 * (danilo.ristovski@mht.net)
			 */
			var jsonParallelLimitStyle = null;
			var jsonParallelAxisStyle = null;
			var jsonParallelTooltipStyle = null;
			var jsonParallelLegendTitle = null;
			var jsonParallelLegendElement = null;

			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'PARALLEL') {
								
				jsonParallelLimitStyle = jsonTemplate.CHART.LIMIT ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.LIMIT.style)
					 : '';

				jsonParallelAxisStyle = jsonTemplate.CHART.AXES_LIST ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.AXES_LIST.style)
					 : '';
					
				jsonParallelTooltipStyle = jsonTemplate.CHART.PARALLEL_TOOLTIP ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.PARALLEL_TOOLTIP.style)
					 : '';

				jsonParallelLegendTitle = jsonTemplate.CHART.LEGEND.TITLE ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.LEGEND.TITLE.style) : "";

				jsonParallelLegendElement = jsonTemplate.CHART.LEGEND.ELEMENT ? Sbi.chart.designer.ChartUtils
					.jsonizeStyle(jsonTemplate.CHART.LEGEND.ELEMENT.style) : "";
			}

			/**
			 * Variables used for the SCATTER chart
			 * (danilo.ristovski@mht.net)
			 */
			var jsonScatterZoomType = null;
			var jsonScatterStartOnTick = null;
			var jsonScatterEndOnTick = null;
			var jsonScatterShowLastLabel = null;

			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'SCATTER') {
			
				var numberOfAxes = jsonTemplate.CHART.AXES_LIST.AXIS.length;
				var axisData = null;				
				
//				if (numberOfAxes == 1 && jsonTemplate.CHART.AXES_LIST.AXIS[0].position == "left")
//				{
//					/**
//					 * If we have only one axis defined in the JSON template and if that axis is Y-axis.
//					 * 
//					 * @author: danristo (danilo.ristovski@mht.net)
//					 */
//					axisData = jsonTemplate.CHART.AXES_LIST.AXIS[0];
//				}
//				else
//				{
					axisData = jsonTemplate.CHART.AXES_LIST.AXIS[1];
//				}
				
				jsonScatterZoomType = jsonTemplate.CHART.zoomType ? jsonTemplate.CHART.zoomType : '';
				jsonScatterStartOnTick = axisData.startOnTick;
				jsonScatterEndOnTick = axisData.endOnTick;
				jsonScatterShowLastLabel = axisData.showLastLabel;
			}
			
			var labelsData = null;
			
			if (Sbi.chart.designer.Designer.chartTypeSelector
					.getChartType() == 'GAUGE')
			{
				axisData = jsonTemplate.CHART.AXES_LIST.AXIS[0]; 
				labelsData = jsonTemplate.CHART.LABELS;
			}

			/**
			 * Variable used for the HEATMAP chart
			 * (danilo.ristovski@mht.net)
			 */
			var jsonHeatmapChartSybmolHeight = null;

			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'HEATMAP') {
				jsonHeatmapChartSybmolHeight = jsonTemplate.CHART.LEGEND.symbolHeight ? jsonTemplate.CHART.LEGEND.symbolHeight : '';
			}

			/**
			 * Variables used for the HEATMAP chart
			 * (danilo.ristovski@mht.net)
			 */
			var jsonGaugeMinAnglePane = null;
			var jsonGaugeMaxAnglePane = null;
			var jsonGaugeAxesListData = null;

			if (Sbi.chart.designer.Designer.chartTypeSelector
				.getChartType() == 'GAUGE') {
				jsonGaugeMinAnglePane = jsonTemplate.CHART.PANE.startAngle;
				jsonGaugeMaxAnglePane = jsonTemplate.CHART.PANE.endAngle;
				jsonGaugeAxesListData = axisData;
			}

			var colorPalette = [];
			if (jsonTemplate.CHART.COLORPALETTE
				 && jsonTemplate.CHART.COLORPALETTE.COLOR) {
				Ext.Array
				.each(
					jsonTemplate.CHART.COLORPALETTE.COLOR,
					function (color) {
					colorPalette
					.push({
						'id' : color.id != undefined ? color.id
						 : color.name,
						'gradient' : color.gradient != undefined ? color.gradient
						 : '',
						'name' : color.name,
						'order' : color.order,
						'value' : Sbi.chart.designer.ChartUtils
						.removeStartingHash(color.value)
					});
				});
			}
			
			var cModel = Ext
				.create(
					'Sbi.chart.designer.ChartConfigurationModel', {
					/**
					 * Generic parameters for charts. They are common for all chart types.
					 * @commentBy: danristo (danilo.ristovski@mht.net)
					 */
					height : jsonTemplate.CHART.height,
					width : jsonTemplate.CHART.width,
					orientation : jsonTemplate.CHART.orientation ? jsonTemplate.CHART.orientation
					 : 'vertical',
					backgroundColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonChartStyle.backgroundColor),
					font : jsonChartStyle.fontFamily,
					fontDimension : jsonChartStyle.fontSize,
					fontWeight : jsonChartStyle.fontWeight,

					title : jsonTitleText,
					titleAlign : jsonTitleStyle.align,
					titleColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonTitleStyle.color),
					titleFont : jsonTitleStyle.fontFamily,
					titleDimension : jsonTitleStyle.fontSize,
					titleStyle : jsonTitleStyle.fontWeight,

					subtitle : jsonSubtitleText,
					subtitleAlign : jsonSubtitleStyle.align,
					subtitleColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonSubtitleStyle.color),
					subtitleFont : jsonSubtitleStyle.fontFamily,
					subtitleDimension : jsonSubtitleStyle.fontSize,
					subtitleStyle : jsonSubtitleStyle.fontWeight,

					nodata : jsonEmptyMsgText,
					nodataAlign : jsonEmptyMsgStyle.align,
					nodataColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonEmptyMsgStyle.color),
					nodataFont : jsonEmptyMsgStyle.fontFamily,
					nodataDimension : jsonEmptyMsgStyle.fontSize,
					nodataStyle : jsonEmptyMsgStyle.fontWeight,

					showLegend : chartLegend.show,
					legendPosition : chartLegend.position,
					legendLayout : chartLegend.layout,
					legendFloating : chartLegend.floating,
					legendX : chartLegend.x,
					legendY : chartLegend.y,
					legendAlign : jsonLegendStyle.align,
					legendFont : jsonLegendStyle.fontFamily,
					legendDimension : jsonLegendStyle.fontSize,
					legendStyle : jsonLegendStyle.fontWeight,
					legendBorderWidth : jsonLegendStyle.borderWidth,
					legendColor : Sbi.chart.designer.ChartUtils.removeStartingHash(jsonLegendStyle.color),
					legendBackgroundColor : Sbi.chart.designer.ChartUtils.removeStartingHash(jsonLegendStyle.backgroundColor),

					colorPalette : colorPalette,

					/**
					 * Added for the WORDCLOUD chart
					 * (danilo.ristovski@mht.net)
					 */
					maxWords : jsonTemplate.CHART.maxWords,
					maxAngle : jsonTemplate.CHART.maxAngle,
					minAngle : jsonTemplate.CHART.minAngle,
					maxFontSize : jsonTemplate.CHART.maxFontSize,
					wordPadding : jsonTemplate.CHART.wordPadding,
					sizeCriteria : jsonTemplate.CHART.sizeCriteria,

					/**
					 * Added for the SUNBURST chart.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					opacMouseOver : jsonTemplate.CHART.opacMouseOver,
					
					/**
					 * Added for the PARALLEL chart.
					 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
					 */
					showTableParallel: jsonTemplate.CHART.showTableParallel,

					toolbarPosition : jsonToolbarStyle.position,
					toolbarHeight : jsonToolbarStyle.height,
					toolbarWidth : jsonToolbarStyle.width,
					toolbarSpacing : jsonToolbarStyle.spacing,
					toolbarTail : jsonToolbarStyle.tail,
					toolbarPercFontColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonToolbarStyle.percFontColor),

					toolbarFontFamily : jsonToolbarStyle.fontFamily,
					toolbarFontWeight : jsonToolbarStyle.fontWeight,
					toolbarFontSize : jsonToolbarStyle.fontSize,

					tipText : jsonTipText,
					tipFontFamily : jsonTipStyle.fontFamily,
					tipFontWeight : jsonTipStyle.fontWeight,
					tipFontSize : jsonTipStyle.fontSize,
					tipColor : Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonTipStyle.color),
					tipWidth : jsonTipStyle.width,

					/**
					 * Added for the PARALLEL chart (LIMIT tag)
					 * (danilo.ristovski@mht.net)
					 */
					maxNumberOfLines : (jsonParallelLimitStyle != null) ? jsonParallelLimitStyle.maxNumberOfLines : null,
					serieFilterColumn : (jsonParallelLimitStyle != null) ? jsonParallelLimitStyle.serieFilterColumn : null,
					orderTopMinBottomMax : (jsonParallelLimitStyle != null) ? jsonParallelLimitStyle.orderTopMinBottomMax : null,

					/**
					 * Added for the PARALLEL chart (AXES_LINES tag)
					 * (danilo.ristovski@mht.net)
					 */
					axisColor : (jsonParallelAxisStyle != null) ? Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonParallelAxisStyle.axisColor) : null,
					axisColNamePadd : (jsonParallelAxisStyle != null) ? jsonParallelAxisStyle.axisColNamePadd : null,
					brushColor : (jsonParallelAxisStyle != null) ? Sbi.chart.designer.ChartUtils
					.removeStartingHash(jsonParallelAxisStyle.brushColor) : null,
					brushWidth : (jsonParallelAxisStyle != null) ? jsonParallelAxisStyle.brushWidth : null,

					/**
					 * Added for the PARALLEL chart (TOOLTIP tag)
					 * (danilo.ristovski@mht.net)
					 */
					parallelTooltipFontFamily : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.fontFamily : null,
					parallelTooltipFontSize : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.fontSize : null,
					parallelTooltipMinWidth : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.minWidth : null,
					parallelTooltipMaxWidth : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.maxWidth : null,
					parallelTooltipMinHeight : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.minHeight : null,
					parallelTooltipMaxHeight : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.maxHeight : null,
					parallelTooltipPadding : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.padding : null,
					parallelTooltipBorder : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.border : null,
					parallelTooltipBorderRadius : (jsonParallelTooltipStyle != null) ? jsonParallelTooltipStyle.borderRadius : null,

					/**
					 * Parameters for the LEGEND's TITLE subtag of the PARALLEL chart
					 * (danilo.ristovski@mht.net)
					 */
					parallelLegendTitleFontFamily : (jsonParallelLegendTitle != null) ? jsonParallelLegendTitle.fontFamily : null,
					parallelLegendTitleFontSize : (jsonParallelLegendTitle != null) ? jsonParallelLegendTitle.fontSize : null,
					parallelLegendTitleFontWeight : (jsonParallelLegendTitle != null) ? jsonParallelLegendTitle.fontWeight : null,

					/**
					 * Parameters for the LEGEND's TITLE subtag of the PARALLEL chart
					 * (danilo.ristovski@mht.net)
					 */
					parallelLegendElementFontFamily : (jsonParallelLegendElement != null) ? jsonParallelLegendElement.fontFamily : null,
					parallelLegendElementFontSize : (jsonParallelLegendElement != null) ? jsonParallelLegendElement.fontSize : null,
					parallelLegendElementFontWeight : (jsonParallelLegendElement != null) ? jsonParallelLegendElement.fontWeight : null,

					/**
					 * Added for the SCATTER chart
					 * (danilo.ristovski@mht.net)
					 */
					scatterZoomType : (jsonScatterZoomType != null) ? jsonScatterZoomType : "",
					scatterStartOnTick : (jsonScatterStartOnTick != null) ? jsonScatterStartOnTick : false,
					scatterEndOnTick : (jsonScatterEndOnTick != null) ? jsonScatterEndOnTick : false,
					scatterShowLastLabel : (jsonScatterShowLastLabel != null) ? jsonScatterShowLastLabel : false,

					/**
					 * Added for the HEATMAP chart
					 * (danilo.ristovski@mht.net)
					 *
					 */
					symbolHeight : (jsonHeatmapChartSybmolHeight != null) ? jsonHeatmapChartSybmolHeight : null,

					/**
					 * Added for the GAUGE chart. Mandatory parameters for the chart -
					 * the document cannot be saved if these are not specified for this
					 * chart type).
					 * (danilo.ristovski@mht.net)
					 */
					startAnglePane : (jsonGaugeMinAnglePane != null) ? jsonGaugeMinAnglePane : null,
					endAnglePane : (jsonGaugeMaxAnglePane != null) ? jsonGaugeMaxAnglePane : null,

					gaugeMin : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.min : null,
					gaugeMax : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.max : null,
					gaugeLineColor : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.lineColor : null,
					//gaugeOffset : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.offset : null,
					gaugeLineWidth : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.lineWidth : null,
					gaugeEndOnTick : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.endOnTickGauge : null,

					gaugeTickPosition : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.tickPosition : null,
					gaugeTickColor : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.tickColor : null,
					gaugeTickPixelInterval : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.tickPixelInterval : null,
					gaugeTickWidth : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.tickWidth : null,
					gaugeTickLength : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.tickLength : null,

					gaugeMinorTickColor : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.minorTickColor : null,
					gaugeMinorTickInterval : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.minorTickInterval : null,
					gaugeMinorTickLength : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.minorTickLength : null,
					gaugeMinorTickPosition : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.minorTickPosition : null,
					gaugeMinorTickWidth : (jsonGaugeAxesListData != null) ? jsonGaugeAxesListData.minorTickWidth : null,

					gaugeDistance : (jsonGaugeAxesListData!=null && jsonGaugeAxesListData.LABELS.distance != null) ? jsonGaugeAxesListData.LABELS.distance : null,
					gaugeRotation : (jsonGaugeAxesListData!=null && jsonGaugeAxesListData.LABELS.rotation != null) ? jsonGaugeAxesListData.LABELS.rotation : null
				});
			
			return cModel;
		},

		convertJsonToTreeFormat : function (data, level) {
			function isValue(element) {
				return (element != null && (typeof element === 'boolean'
						 || typeof element === 'string' || typeof element === 'number'));
			};

			var nivel = (level != undefined && typeof level === 'number') ? level
			 : 0;
			var treeData = [];
			
			// TODO: danristo (for GAUGE: Step 1 -> Step 4 error of undefined)
			if (data) {			
			var keys = Object.keys(data);

			for (index in keys) {
				var key = keys[index];
				if (Array.isArray(data[key])) {
					var array = data[key];

					for (var i = 0; i < array.length; i++) {
						treeData.push({
							key : key,
							expanded : (nivel < 1),
							isArray : 1,
							children : ChartUtils.convertJsonToTreeFormat(
								array[i],
								nivel + 1),
						});
					}
				} else if (isValue(data[key])) {
					var type = 'object';
					if (typeof data[key] === 'boolean')
						type = 'boolean';
					if (typeof data[key] === 'string')
						type = 'string';
					if (typeof data[key] === 'number')
						type = 'number';

					treeData.push({
						key : key,
						value : data[key],
						type : type,
						isArray : 0,
						leaf : true
					});
				} else {
					treeData.push({
						key : key,
						expanded : (nivel < 1),
						isArray : 0,
						children : ChartUtils
						.convertJsonToTreeFormat(
							data[key], nivel + 1)
					});
				}
			}
		}

			if (nivel == 0) {
				var treeFormattedJson = {
					expanded : true,
					children : treeData
				};

				return treeFormattedJson;
			}
			return treeData;
		},

		convertTreeFormatToJson : function (data, isWrapper) {

			function areThereDifferentChildren(children) {
				if (children.length == 0) {
					return false;
				}
				var firstIsArray = children[0].isArray;
				var firstElementKey = children[0].key;
				
				for (i in children) {
					var isArray = children[i].isArray;
					var elementKey = children[i].key;
					
					if (firstIsArray != isArray || firstElementKey != elementKey) {
						return true;
					}
				}
				return false;
			};

			if (isWrapper && isWrapper == true) {
				var root = ChartUtils.convertTreeFormatToJson(data.children[0]);
				
				var rootKey = data.children[0].key;

				var result = {};
				result[rootKey] = root;
				return result;
			}

			if (data.leaf) {
				return data.value;
			} else if (data.children && areThereDifferentChildren(data.children)) {
				var result = {};
				for (i in data.children) {
					var datum = data.children[i];
					if (result[datum.key] != undefined) { // If it is present a node with the same name
						var tempDatum = result[datum.key];
						if (Array.isArray(tempDatum)) {
							var newDatumKeyArray = [];
							for (j in tempDatum) {
								newDatumKeyArray.push(tempDatum[j]);
							}
							newDatumKeyArray.push(ChartUtils.convertTreeFormatToJson(datum));
							
							result[datum.key] = newDatumKeyArray;
						} else {
							var newDatumKeyArray = [];
							newDatumKeyArray.push(tempDatum);
							newDatumKeyArray.push(ChartUtils.convertTreeFormatToJson(datum));
							result[datum.key] = newDatumKeyArray;
						}
					} else {
						result[datum.key] = datum.isArray == 0 ? 
								ChartUtils.convertTreeFormatToJson(datum) : [ChartUtils.convertTreeFormatToJson(datum)];
					}
				}
				return result;

			} else if (data.children && data.children[0]
				 && data.children[0].isArray == 0) {
				var result = {};

				for (i in data.children) {
					var datum = data.children[i];
					result[datum.key] = ChartUtils
						.convertTreeFormatToJson(datum);
				}
				return result;
			} else if (data.children && data.children[0]
				 && data.children[0].isArray == 1) {
				var array = [];

				for (i in data.children) {
					var datum = data.children[i];
					array.push(ChartUtils.convertTreeFormatToJson(datum));
				}
				var result = {};
				result[data.children[0].key] = array;

				return result;
			} else {
				return {};
			}
		},

		clone : function (objToClone) {
			if (objToClone === null
				 || typeof(objToClone) !== 'object') {
				return objToClone;
			}

			var temp = objToClone.constructor();

			for (var key in objToClone) {
				if (Object.prototype.hasOwnProperty.call(objToClone, key)) {
					temp[key] = ChartUtils.clone(objToClone[key]);
				}
			}
			return temp;
		},

		/**
		 * Static function that provides removing of all properties that are unwanted and that are 
		 * specified in static variable "unwantedStyleProps". The function removes those unwanted 
		 * properties from the JSON object representation of the XML file of the style applied to 
		 * the chart document (if there are any of those unwanted properties in the style file). 
		 * This JSON object is afterwards about to be the merging object with the current state 
		 * (structure) of the chart. 
		 * 
		 * @param obj - The input parameter that represents the JSON object of the XML file of the 
		 * style we want to apply to our document (chart) from which we want to remove all unwanted 
		 * properties specified in the static variable "unwantedStyleProps".
		 * 
		 * @param isYAxisEmpty - Tells this method if there are no series inside of the Y-axis panel
		 * so we can skip mergin of the source SERIE item that is an OBJECT (not an empty array) to 
		 * the target configuration that does not posses this tag at all. Otherwise, we will have an 
		 * SERIE object inside of the final configuration that will create fake (ghost) serie item 
		 * in the Y-axis panel of the newly created chart.
		 * 
		 * @author Danilo Ristovski (danristo, danilo.ristovski@mht.net)
		 */
		removeUnwantedPropsFromJsonStyle: function(obj,isYAxisEmpty)
		{			
			if (obj)
	    	{
		    	for (var key in obj) 
		    	{
		            if (typeof obj[key] == "object")
	            	{
		            	ChartUtils.removeUnwantedPropsFromJsonStyle(obj[key],isYAxisEmpty);
	            	}			               
		            else if (typeof obj[key] != "function")
	            	{
		            	for (var i=0; i<ChartUtils.unwantedStyleProps.length; i++)
	            		{
		            		if (key == ChartUtils.unwantedStyleProps[i])
		            		{
		            			//console.log(key);
			            		delete obj[key];
		            		}
	            		}	
		            	
		            	if (isYAxisEmpty == true)
	            		{
		            		/**
		            		 * Remove this tag since it contains the SERIE sub-tag that
		            		 * is in this case an object (not an empty array, as we are
		            		 * expecting to be), so to skip appereance of the ghost serie
		            		 * item in the Y-axis (that should however be empty).
		            		 */
		            		delete obj["VALUES"];
	            		}
	            	}			                
		        }	
	    	}			        

		    return obj;
		},
		
		/**
		 * Creates a new merged object using matching key in
		 * case of array merging, keeping intact the original
		 * objects <code>target</code> and <code>source</code>.
		 *
		 * @author Benedetto Milazzo (benedetto.milazzo@eng.it)
		 * @param target
		 * @param source
		 * @param config Object
		 * 		- removeNotFoundItemsFlag boolean - tells the mergeObjects method to remove id properties from each node and subnode
		 * 		- applyAxes boolean - makes a special apply to CHART.AXES_LIST.AXIS nodes of the source json obj and target json obj
		 */
		/**
		 * @param addNewAxis - 	If we add new Y-axis panel, skip applying current style
		 * 						to all Y-axis panel (to already existing ones as well 
		 * 						as on the one that we actually just added) and apply it
		 * 						just to the newly added one. 
		 */
		//		mergeObjects : function (target, source, removeNotFoundItemsFlag) {
		mergeObjects: function (target, source, config, addNewAxis) {
			function isArray(o) {
				return Object.prototype.toString.call(o) == "[object Array]";
			}

			config = config || {};
			//console.log("== marge objects (START) ==");
			var removeNotFoundItemsFlag = config.removeNotFoundItemsFlag || false;
			
			var applyAxes = config.applyAxes || false;
			var applySeries = config.applySeries || false;

			var item, // each single property of 'source' obj
			tItem, // each single property of 'target' obj
			o,
			idx;

			// If either argument is undefined, return the other.
			// If both are undefined, return undefined.
			if (typeof source == 'undefined') {
				return source;
			} else if (typeof target == 'undefined') {
				return target;
			}

			var newTarget = ChartUtils.clone(target);

			
			// Assume both are objects and don't care about
			// inherited properties
			for (var prop in source) {

				// Daniele (commented part)
//				if (applyAxes && prop == 'AXIS') {
				if (applyAxes && prop == 'AXES_LIST') {
					
					var axisTagSource = source[prop]['AXIS'];
					var axisTagTarget = target[prop]['AXIS'];
					
					if (axisTagSource!=undefined)
						newTarget[prop]['AXIS'] = ChartUtils.applyAxes(axisTagTarget, axisTagSource, addNewAxis);
					
					/**
					 * Characteristic for PARALLEL chart
					 */
					// TODO: Can i do something like this (just associating the property of the same level from right to left side)????
					if (source[prop]['style'] != undefined)
						newTarget[prop]['style'] = source[prop]['style'];
					
					/**
					 * If the chart type is GAUGE and we have PLOTBANDS defined inside of the XML styles
					 * apply it to all Y-axis panels, so every single of them can have the same plotbands,
					 * even though is not advised to define some non-empty values for PLOTs inside of the
					 * PLOTBANDS tag of the chart style XML template.
					 * 
					 * @author: danristo (danilo.ristovski@mht.net)
					 */
					if (axisTagSource.length > 1)
					{
						for (i=0; i<axisTagSource.length; i++)
						{
							if (axisTagSource[i].type=="Serie" && axisTagSource[i]["PLOTBANDS"]!=undefined)
							{
								/**
								 * Apply PLOTBANDS tag on all Y-axis panel that the GAUGE chart (document) has.
								 */
								for (j=0; j<axisTagTarget.length; j++)
								{
									newTarget[prop]['AXIS'][j]["PLOTBANDS"] = axisTagSource[i]["PLOTBANDS"];
								}	
							}														
						}
					}
					
					// Daniele (commented part)
//					newTarget['AXES_LIST'][prop] = ChartUtils.applyAxes(target['AXES_LIST'][prop], source[prop]);

					continue;
				}
				
				if (applySeries && prop == 'SERIE') {
					newTarget[prop] = ChartUtils.applySeries(target[prop], source[prop], addNewAxis);
					continue;
				}

				item = source[prop];
				
				if (typeof item == 'object' && item !== null) {
					if (isArray(item)) {

						// deal with arrays, will be either
						// array of primitives or array of
						// objects
						// If primitives
						if (item.length > 0 && typeof item[0] != 'object') {

							// if target doesn't have a similar
							// property, just reference it
							tItem = newTarget[prop];
							
							if (!tItem) {
								newTarget[prop] = item;

							} else {
								// Otherwise, copy only those members that don't exist on target

								// Create an index of items on target
								o = {};
								for (var i = 0; i < tItem.length; i++) {
									o[tItem[i]] = true;
								}

								// Do check, push missing
								for (var j = 0; j < item.length; j++) {

									if (!(item[j]in o)) {
										tItem.push(item[j]);
									}
								}
							}
						} else {
							// Deal with array of objects
							// Create index of objects in target object using ID property
							// Assume if target has same named property then it will be similar array
							idx = {};
							tItem = newTarget[prop];

							if (!tItem) {
								newTarget[prop] = item;
							} else {
								var forcedTItemArray = [];
								if (!isArray(tItem)) {
									// same length of source
									// array
									for (var itemIndex in item) {
										var mixedMergedObj =
											ChartUtils.mergeObjects(tItem, item[itemIndex], config, addNewAxis);
										forcedTItemArray.push(mixedMergedObj);
									}

									tItem = forcedTItemArray;
								}

								for (var k = 0; k < tItem.length; k++) {
									var tItemK = tItem[k];

									var idValue = tItemK.id;
									for (var l = 0; l < item.length; l++) {
										var itemL = item[l];
										if (itemL.id == idValue) {
											idx[tItemK.id] = tItemK;
											break;
										}
									}
								}
								while (tItem.length > 0) {
									tItem.pop();
								}

								// Do updates
								for (var l = 0; l < item.length; l++) {
									var itemL = item[l];
									var idxItem = idx[itemL.id];

									if (idxItem != undefined) {
										tItem.push(ChartUtils.mergeObjects(idxItem, itemL, config, addNewAxis));
									} else {
										tItem.push(itemL);
									}
								}

								newTarget[prop] = tItem;
							}
						}
					} else {
						// deal with object
						var tItem = newTarget[prop];
						if (!tItem || (isArray(tItem) && tItem.length == 0)) {
							newTarget[prop] = item;
						} else {
							newTarget[prop] = ChartUtils.mergeObjects(newTarget[prop], item, config, addNewAxis);
						}
					}

				} else {
					// item is a primitive, just copy it over
					newTarget[prop] = item;
				}	
				
			}

			if (removeNotFoundItemsFlag) {
				newTarget = ChartUtils.removeNotFoundItems(newTarget, source);
			}
			//console.log("== marge objects (END) ==");
			return newTarget;
		},

		/** Patch for axes merging */
		applyAxes : function (target, source, addNewAxis) {
						
			var styleSerieAxis,
			styleCategoryAxis;

			/**
			 * 'target' - 	content (properties) of the AXIS tag of the XML structure
			 * 				inside the 'jsonTemplate' that represent the target when
			 * 				merging.
			 * 'source' - 	content (properties) of the AXIS tag of the XML structure
			 * 				inside the 'jsonTemplate' that represent the source when
			 * 				merging.
			 * @comment by: danristo (danilo.ristovski@mht.net)
			 */

			// 'source' is an array containing the styles
			for (var i = 0; i < source.length; i++) {
				var axis = source[i];
				
				/**
				 * 'styleSerieAxis' - 		properties that are common for the AXIS tag of the
				 * 							axis for the SERIE items.
				 * 'styleCategoryAxis' - 	properties that are common for the AXIS tag of the
				 * 							axis for the CATEGORY items.
				 * @comment by: danristo (danilo.ristovski@mht.net)
				 */				
				if (axis.type.toLowerCase() == 'serie') {
					styleSerieAxis = axis;
				} else if (axis.type.toLowerCase() == 'category') {
					styleCategoryAxis = axis;
				}
			}

			var finalAxisArray = [];

			for (var i = 0; i < target.length; i++) {
				var appliedStyledAxis = {};
				var targetAxis = target[i];

				/**
				 * If we specify "addNewAxis" input parameter that should hold alias and ID
				 * of newly added Y-axis panel (when calling merging from handler that handles
				 * clicking action on the PLUS button on the Y-axis), then we should apply 
				 * current style only to Y-axis panel that is just added, not to others also.
				 * We will distinguish whether the current, i-th Y-axis panel is the one that
				 * is just added, basing on the fact that aliases and IDs of current Y-axis 
				 * pane (i-th) must match with the one that is forwarded from PLUS button clicking
				 * handler. In the case we call merging from this function, "addNewAxis" parameter
				 * will be defined, so we will skip merging of other panels (those that are not 
				 * the new one) and just forward them as they are. 
				 * 
				 * TODO: Check with Benedetto if this is OK !!!
				 */
				
				/**
				 * If merging is called from the PLUS button clicking handler (the one for adding new Y-axis).
				 */
				if (addNewAxis)
				{
					/**
					 * If we are deling with the newly added Y-axis panel, merge it with the
					 * current style...
					 */
					if (addNewAxis.alias==targetAxis.alias && addNewAxis.id==targetAxis.id)
					{
						if (targetAxis.type.toLowerCase() == 'serie') {
							appliedStyledAxis = ChartUtils.mergeObjects(targetAxis, styleSerieAxis);
						}
					}
					/**
					 * ... otherwise, leave axis as it is (do not apply current style to it).
					 */
					else
					{
						appliedStyledAxis = targetAxis;
					}					
				}
				/**
				 * If merging is called from some other place (not the PLUS sign clicking handler).
				 */
				else
				{					
					if (targetAxis.type.toLowerCase() == 'serie') {
						appliedStyledAxis = ChartUtils.mergeObjects(targetAxis, styleSerieAxis);
					} else if (targetAxis.type.toLowerCase() == 'category') {
						appliedStyledAxis = ChartUtils.mergeObjects(targetAxis, styleCategoryAxis);
					}
				}				

				finalAxisArray.push(appliedStyledAxis);
			}

			return finalAxisArray;
		},
		
		applySeries: function(target, source, addNewAxis) {
									
			var finalSerieArray = [];
			
			var newTarget = [];
			
			if(target === undefined) {
				target = {};
			}
			
			if(!Array.isArray(target)) {
				target = [target];
			}
			
			for(var i = 0; i < target.length; i++) {
				var targetItem = target[i];
				var newTargetItem = {};
										
				/** 
				 * TODO: Check with Benedetto if this is OK !!!
				 */
				
				/**
				 * If merging is called from the PLUS button clicking handler (the one for adding new Y-axis).
				 */
				if (addNewAxis)
				{
					/**
					 * If we are deling with the newly added Y-axis panel, merge it with the
					 * current style...
					 */
					if (addNewAxis.alias==targetItem.alias && addNewAxis.id==targetItem.id)
					{
						newTargetItem = ChartUtils.mergeObjects(targetItem, source);
					}
					/**
					 * ... otherwise, leave axis as it is (do not apply current style to it).
					 */
					else
					{
						newTargetItem = targetItem;
					}					
				}
				else
				{
					newTargetItem = ChartUtils.mergeObjects(targetItem, source);
				}
				
				newTarget.push(newTargetItem);
			}
			
			return newTarget;
			
		},

		removeNotFoundItems : function (target, source) {
			var newTarget = ChartUtils.clone(target);

			if (typeof newTarget == 'object' && newTarget !== null) {
				for (var prop in newTarget) {
					if (source[prop] == undefined) {
						delete newTarget[prop];
					} else if (!Array.isArray(newTarget[prop])) {
						newTarget[prop] = ChartUtils.removeNotFoundItems(newTarget[prop], source[prop]);
					}
				}
			}

			return newTarget;
		},
		
		stringStartsWith: function(string, prefix) {
		    return string.slice(0, prefix.length) == prefix;
		},
		stringEndsWith: function(string, suffix) {
		    return suffix == '' || string.slice(-suffix.length) == suffix;
		}
	}
});