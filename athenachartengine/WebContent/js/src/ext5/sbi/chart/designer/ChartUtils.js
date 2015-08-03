Ext.define(
				'Sbi.chart.designer.ChartUtils',
				{
					extend : 'Ext.Base',
					alternateClassName : [ 'ChartUtils' ],

					statics : {
						ddGroupMeasure : 'MEASURE',
						ddGroupAttribute : 'ATTRIBUTE',
						globThis : this,
						
						isCockpitEngine: false,
						
						setCockpitEngine: function(isCockpit){
							Sbi.chart.designer.ChartUtils.isCockpitEngine = isCockpit;
						}
						
						, convertJsonAxisObjToAxisData : function(axis) {
							var result = {};

							result['id'] = axis.alias && axis.alias != '' ? axis.alias
									: '';
							result['alias'] = axis.alias && axis.alias != '' ? axis.alias
									: '';
							result['axisType'] = axis.type && axis.type != '' ? axis.type
									: '';
							result['position'] = axis.position
									&& axis.position != '' ? axis.position : '';

							var axisStyleAsMap = ChartUtils
									.jsonizeStyle(axis.style);
							result['styleRotate'] = axisStyleAsMap.rotate
									&& axisStyleAsMap.rotate != '' ? axisStyleAsMap.rotate
									: '';
							result['styleAlign'] = axisStyleAsMap.align
									&& axisStyleAsMap.align != '' ? axisStyleAsMap.align
									: '';
							result['styleColor'] = axisStyleAsMap.color
									&& axisStyleAsMap.color != '' ? axisStyleAsMap.color
									: '';
							result['styleFont'] = axisStyleAsMap.font
									&& axisStyleAsMap.font != '' ? axisStyleAsMap.font
									: '';
							result['styleFontWeigh'] = axisStyleAsMap.fontWeight
									&& axisStyleAsMap.fontWeight != '' ? axisStyleAsMap.fontWeight
									: '';
							result['styleFontSize'] = axisStyleAsMap.fontSize
									&& axisStyleAsMap.fontSize != '' ? axisStyleAsMap.fontSize
									: '';
							
							result['styleOpposite'] = axisStyleAsMap.opposite;

							if (axis.MAJORGRID) {
								result['majorgridInterval'] = axis.MAJORGRID.interval
										&& axis.MAJORGRID.interval != '' ? axis.MAJORGRID.interval
										: '';

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
								result['titleStyleAlign'] = titlegridStyleAsMap.align
										&& titlegridStyleAsMap.align != '' ? titlegridStyleAsMap.align
										: '';
								result['titleStyleColor'] = titlegridStyleAsMap.color
										&& titlegridStyleAsMap.color != '' ? titlegridStyleAsMap.color
										: '';
								result['titleStyleFont'] = titlegridStyleAsMap.font
										&& titlegridStyleAsMap.font != '' ? titlegridStyleAsMap.font
										: '';
								result['titleStyleFontWeigh'] = titlegridStyleAsMap.fontWeight
										&& titlegridStyleAsMap.fontWeight != '' ? titlegridStyleAsMap.fontWeight
										: '';
								result['titleStyleFontSize'] = titlegridStyleAsMap.fontSize
										&& titlegridStyleAsMap.fontSize != '' ? titlegridStyleAsMap.fontSize
										: '';
							}

							return result;
						},
						createEmptyAxisData : function(isCategory, isLeftSerie) {
							isCategory = isCategory || false;
							isLeftSerie = isLeftSerie || false;

							var result = {};

							result['id'] = 'Axis_'
									+ ChartColumnsContainerManager.instanceIdFeed;
							result['alias'] = 'Axis_'
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

						exportAsJson : function(chartModel) {
							var result = {};
							var CHART = {};

							CHART['type'] = Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType();

							var chartData = ChartUtils
									.getChartDataAsOriginaJson(chartModel);

							Ext.apply(CHART, chartData);

							var AXES_LIST = {};

							if (Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == "PARALLEL") {
								// *_* Added for the TIP tag for the PARALLEL
								// chart (AXES_LINES tag)
								var axesList = "";
								axesList += 'axisColNamePadd:'
										+ ((chartModel.get('axisColNamePadd')) ? chartModel
												.get('axisColNamePadd')
												: '') + ';';
								axesList += 'brushWidth:'
										+ ((chartModel.get('brushWidth')) ? chartModel
												.get('brushWidth')
												: '') + ';';
								axesList += 'axisColor:'
										+ ((chartModel.get('axisColor') != undefined && chartModel
												.get('axisColor') != '') ? '#'
												+ chartModel.get('axisColor')
												: '') + ';';
								axesList += 'brushColor:'
										+ ((chartModel.get('brushColor') != undefined && chartModel
												.get('brushColor') != '') ? '#'
												+ chartModel.get('brushColor')
												: '') + ';';

								AXES_LIST['style'] = axesList;
								CHART['AXES_LIST'] = AXES_LIST;
							}

							var AXIS = ChartUtils.getAxesDataAsOriginalJson();
							
							if (Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == "SCATTER")
							{								
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

							result['CHART'] = CHART;
							
							return result;
						},

						getAxesDataAsOriginalJson : function() {
							var result = [];

							/* START Chart left and right axes data */
							var leftAndRightAxisesContainers = [
									Ext.getCmp('chartLeftAxisesContainer'),
									Ext.getCmp('chartRightAxisesContainer') ];

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
									
									// TODO: Added 17.7
									// (danilo.ristovski@mht.net)
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
									 * (danristo)
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

									result.push(axisAsJson);
								}
							}
							/* END Chart left and right axes data */

							/* START Chart bottom axis data */
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

							return result;
						},

						getSeriesDataAsOriginalJson : function() {
							var result = [];

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
									;
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
										TOOLTIP['style'] = tooltipStyle;

										serie['TOOLTIP'] = TOOLTIP;
									}

									result.push(serie);
								}
							}

							return result;
						},

						getCategoriesDataAsOriginalJson : function() {
							var categoriesStore = Ext.data.StoreManager
									.lookup('categoriesStore');
							
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType();

							var result = [];

							if (chartType.toUpperCase() == "SUNBURST"
									|| chartType.toUpperCase() == "WORDCLOUD"
									|| chartType.toUpperCase() == "TREEMAP"
									|| chartType.toUpperCase() == "PARALLEL"
										|| chartType.toUpperCase() == "HEATMAP") {
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
//									category['categoryDataType'] = mainCategory
//											.get('colType') != undefined ? mainCategory
//											.get('colType')
//											: '';
//									console.log(mainCategory);
//									console.log(mainCategory.get('categoryDataType'));
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
										/\,$/, '');
								;
							}

							return result;
						},

						getChartDataAsOriginaJson : function(chartModel) {
							var CHART = {};

							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
							
							CHART['height'] = (chartModel.get('height') != undefined) ? chartModel
									.get('height')
									: '';
							CHART['width'] = (chartModel.get('width') != undefined) ? chartModel
									.get('width')
									: '';									
							
							CHART['isCockpitEngine'] = Sbi.chart.designer.ChartUtils.isCockpitEngine;		
									
							if (chartType.toUpperCase() == "SCATTER") 
							{
								CHART['zoomType'] = (chartModel.get('scatterZoomType')) ? chartModel.get('scatterZoomType') : '';
							}
							
							if (chartType.toUpperCase() == "WORDCLOUD"){
								CHART['sizeCriteria'] = (chartModel.get('sizeCriteria')) ? chartModel.get('sizeCriteria') : '';
							}
							
							if (chartModel.get('orientation') != undefined) {
								CHART['orientation'] = chartType.toUpperCase() != 'PIE' ? chartModel
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
							chartStyle += 'backgroundColor:'
									+ ((chartModel.get('backgroundColor') != undefined && chartModel
											.get('backgroundColor') != '') ? '#'
											+ chartModel.get('backgroundColor')
											: '') + ';';

							if (chartType.toUpperCase() == "SUNBURST") {
								chartStyle += 'opacMouseOver:'
										+ ((chartModel.get('opacMouseOver')) ? chartModel
												.get('opacMouseOver')
												: '') + ';';
							}

							if (chartType.toUpperCase() == "WORDCLOUD") {
								chartStyle += 'maxWords:'
										+ ((chartModel.get('maxWords')) ? chartModel
												.get('maxWords')
												: '') + ';';
								chartStyle += 'maxAngle:'
										+ ((chartModel.get('maxAngle')) ? chartModel
												.get('maxAngle')
												: '') + ';';
								chartStyle += 'minAngle:'
										+ ((chartModel.get('minAngle')) ? chartModel
												.get('minAngle')
												: '') + ';';
								chartStyle += 'maxFontSize:'
										+ ((chartModel.get('maxFontSize')) ? chartModel
												.get('maxFontSize')
												: '') + ';';
								chartStyle += 'wordPadding:'
										+ ((chartModel.get('wordPadding')) ? chartModel
												.get('wordPadding')
												: '') + ';';
							}

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
							emptymessageStyle += 'color:'
									+ ((chartModel.get('nodataColor') != undefined && chartModel
											.get('nodataColor') != '') ? '#'
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
							titleStyle += 'color:'
									+ ((chartModel.get('titleColor') != undefined && chartModel
											.get('titleColor') != '') ? '#'
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
							subtitleStyle += 'color:'
									+ ((chartModel.get('subtitleColor') != undefined && chartModel
											.get('subtitleColor') != '') ? '#'
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
							legendStyle += 'color:'
									+ ((chartModel.get('legendColor') != undefined && chartModel
											.get('legendColor') != '') ? '#'
											+ chartModel.get('legendColor')
											: '') + ';';
							legendStyle += 'backgroundColor:'
									+ ((chartModel.get('legendBackgroundColor') != undefined && chartModel
											.get('legendBackgroundColor') != '') ? '#'
											+ chartModel
													.get('legendBackgroundColor')
											: '') + ';';
							
							/**
							 * (danilo.ristovski@mht.net) (modified: 20.7)
							 */
							legendStyle += 'symbolWidth:'
								+ ((chartModel.get('symbolWidth') != undefined) ? chartModel
										.get('symbolWidth')
										: '') + ';';
							
							LEGEND['style'] = legendStyle;

							CHART['LEGEND'] = LEGEND;

							// *_* (START)
							if (chartType.toUpperCase() == "SUNBURST") {
								var TOOLBAR = {};
								var toolbarStyle = '';

								toolbarStyle += 'position:'
										+ ((chartModel.get('toolbarPosition')) ? chartModel
												.get('toolbarPosition')
												: '') + ';';
								toolbarStyle += 'height:'
										+ ((chartModel.get('toolbarHeight')) ? chartModel
												.get('toolbarHeight')
												: '') + ';';
								toolbarStyle += 'width:'
										+ ((chartModel.get('toolbarWidth')) ? chartModel
												.get('toolbarWidth')
												: '') + ';';
								toolbarStyle += 'spacing:'
										+ ((chartModel.get('toolbarSpacing')) ? chartModel
												.get('toolbarSpacing')
												: '') + ';';
								toolbarStyle += 'tail:'
										+ ((chartModel.get('toolbarTail')) ? chartModel
												.get('toolbarTail')
												: '') + ';';
								// toolbarStyle += 'padding:' +
								// ((chartModel.get('serieTooltipFontSize') !=
								// undefined)?
								// chartModel.get('serieTooltipFontSize'): '') +
								// ';';
								toolbarStyle += 'percFontColor:'
										+ ((chartModel
												.get('toolbarPercFontColor') != undefined && chartModel
												.get('toolbarPercFontColor') != '') ? '#'
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
								// *_* (END)

								// *_* (START)
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
								tipStyle += 'color:'
										+ ((chartModel.get('tipColor') != undefined && chartModel
												.get('tipColor') != '') ? '#'
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
								// tipStyle += 'position:' +
								// ((chartModel.get('tipPosition'))?
								// chartModel.get('tipPosition'): '') + ';';

								TIP['text'] = (chartModel.get('tipText') != undefined) ? chartModel
										.get('tipText')
										: '';
								TIP['style'] = tipStyle;
								CHART['TIP'] = TIP;
								// *_* (END)
							}

							if (chartType.toUpperCase() == "HEATMAP")
							{
								// *_* (START)
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
								tipStyle += 'color:'
										+ ((chartModel.get('tipColor') != undefined && chartModel
												.get('tipColor') != '') ? '#'
												+ chartModel.get('tipColor')
												: '') + ';';
								tipStyle += 'align:'
										+ ((chartModel.get('tipAlign')) ? chartModel
												.get('tipAlign')
												: '') + ';';
								
								TOOLTIP['style'] = tipStyle;
								CHART['TOOLTIP'] = TOOLTIP;
							}
							
							// *_* (START)
							if (chartType.toUpperCase() == "PARALLEL") {
								var LIMIT = {};
								var limitStyle = '';

								limitStyle += 'maxNumberOfLines:'
										+ ((chartModel.get('maxNumberOfLines')) ? chartModel
												.get('maxNumberOfLines')
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
										+ ((chartModel
												.get('parallelTooltipMinWidth')) ? chartModel
												.get('parallelTooltipMinWidth')
												: '') + ';';
								parallelTooltipStype += 'maxWidth:'
										+ ((chartModel
												.get('parallelTooltipMaxWidth')) ? chartModel
												.get('parallelTooltipMaxWidth')
												: '') + ';';
								parallelTooltipStype += 'minHeight:'
										+ ((chartModel
												.get('parallelTooltipMinHeight')) ? chartModel
												.get('parallelTooltipMinHeight')
												: '') + ';';
								parallelTooltipStype += 'maxHeight:'
										+ ((chartModel
												.get('parallelTooltipMaxHeight')) ? chartModel
												.get('parallelTooltipMaxHeight')
												: '') + ';';
								parallelTooltipStype += 'padding:'
										+ ((chartModel
												.get('parallelTooltipPadding')) ? chartModel
												.get('parallelTooltipPadding')
												: '') + ';';
								parallelTooltipStype += 'border:'
										+ ((chartModel
												.get('parallelTooltipBorder')) ? chartModel
												.get('parallelTooltipBorder')
												: '') + ';';
								parallelTooltipStype += 'borderRadius:'
										+ ((chartModel
												.get('parallelTooltipBorderRadius')) ? chartModel
												.get('parallelTooltipBorderRadius')
												: '') + ';';

								PARALLEL_TOOLTIP['style'] = parallelTooltipStype;
								CHART['PARALLEL_TOOLTIP'] = PARALLEL_TOOLTIP;
							}

							return CHART;
						},

						jsonizeStyle : function(str) {
							var jsonStyle = {};
							
							var globThis = this;
							
							if (str) {
								var styles = str.split(';');
								for (index in styles) {
									var keyValue = styles[index].split(':');
									jsonStyle[keyValue[0]] = keyValue[1];
								}
							}

							return jsonStyle;
						},

						removeStartingHash : function(colorWithHash) {
							return colorWithHash ? colorWithHash.replace("#",
									'') : colorWithHash;
						},

						enableToolbarAndTip : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'SUNBURST';
						},

						enablePalette : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() != 'SUNBURST'
									&& Sbi.chart.designer.Designer.chartTypeSelector
											.getChartType() != 'WORDCLOUD'
												&& Sbi.chart.designer.Designer.chartTypeSelector
												.getChartType() != 'SCATTER';
						},

						disableShowLegendCheck : function()
						{
							return Sbi.chart.designer.Designer.chartTypeSelector
							.getChartType() == 'SUNBURST'
							|| Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'WORDCLOUD'
							|| Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'PARALLEL'
							|| Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'TREEMAP'
										|| Sbi.chart.designer.Designer.chartTypeSelector
										.getChartType() == 'HEATMAP';
						},
						
						enableLegend : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() != 'SUNBURST'
									&& Sbi.chart.designer.Designer.chartTypeSelector
											.getChartType() != 'WORDCLOUD'
									&& Sbi.chart.designer.Designer.chartTypeSelector
											.getChartType() != 'PARALLEL'
									&& Sbi.chart.designer.Designer.chartTypeSelector
											.getChartType() != 'TREEMAP'
									&& Sbi.chart.designer.Designer.chartTypeSelector
											.getChartType() != 'HEATMAP';
						},

						// TODO: Modified 17.7
						/**
						 * If Designer is still not defined (created), i.e. in the process of running (creating) it
						 * we need to prevent 'disableChartWidth' and 'disableChartOrientation' functions from taking
						 * values of the undefined one (Designer = undefined).
						 */
						
						disableChartWidth : function() {		
							return Sbi.chart.designer.Designer.chartTypeSelector.getChartType() == 'SUNBURST';
						},

						disableChartOrientation : function() {
							var chartType = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
							
							return chartType == 'SUNBURST'
									|| chartType == 'WORDCLOUD'
									|| chartType == 'TREEMAP'
									|| chartType == 'PARALLEL'
									|| chartType == 'HEATMAP';
						},

						enableOpacityMouseOver : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'SUNBURST';
						},

						enableWordcloudPanel : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'WORDCLOUD';
						},

						enableParallelPanel : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'PARALLEL';
						},
						
						enableScatterElements : function() {
							return Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'SCATTER';
						},
						
						enableHeatmapLegendAndTooltip : function()
						{
							return Sbi.chart.designer.Designer.chartTypeSelector
								.getChartType() == 'HEATMAP';
						},

						createChartConfigurationModelFromJson : function(
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

							// *_* Variables used for SUNBURST chart
							/* START */
							var jsonToolbarStyle = jsonTemplate.CHART.TOOLBAR ? Sbi.chart.designer.ChartUtils
									.jsonizeStyle(jsonTemplate.CHART.TOOLBAR.style)
									: '';

							var jsonTipText = jsonTemplate.CHART.TIP ? jsonTemplate.CHART.TIP.text
									: '';
							
							/**
							 * Modified for HEATMAP (20.7)
							 */
							var jsonTipStyle = {};
							
							if (Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'SUNBURST')
							{
								jsonTipStyle = jsonTemplate.CHART.TIP ? Sbi.chart.designer.ChartUtils
										.jsonizeStyle(jsonTemplate.CHART.TIP.style)
										: '';
							}
							else if (Sbi.chart.designer.Designer.chartTypeSelector
									.getChartType() == 'HEATMAP')
							{
								jsonTipStyle = jsonTemplate.CHART.TOOLTIP ? Sbi.chart.designer.ChartUtils
										.jsonizeStyle(jsonTemplate.CHART.TOOLTIP.style)
										: '';
							}								
							/* END */

							// *_* Variables used for PARALLEL chart
							/* START */
							var jsonParallelLimitStyle = jsonTemplate.CHART.LIMIT ? Sbi.chart.designer.ChartUtils
									.jsonizeStyle(jsonTemplate.CHART.LIMIT.style)
									: '';
							var jsonParallelAxisStyle = jsonTemplate.CHART.AXES_LIST ? Sbi.chart.designer.ChartUtils
									.jsonizeStyle(jsonTemplate.CHART.AXES_LIST.style)
									: '';
							var jsonParallelTooltipStyle = jsonTemplate.CHART.PARALLEL_TOOLTIP ? Sbi.chart.designer.ChartUtils
									.jsonizeStyle(jsonTemplate.CHART.PARALLEL_TOOLTIP.style)
									: '';
							/* END */

							// *_* Variable used for the SCATTER chart
							/* START */
							var jsonScatterZoomType = jsonTemplate.CHART.zoomType ? jsonTemplate.CHART.zoomType : '';	
							var jsonScatterStartOnTick = jsonTemplate.CHART.AXES_LIST.AXIS[1].startOnTick;	
							var jsonScatterEndOnTick = jsonTemplate.CHART.AXES_LIST.AXIS[1].endOnTick;	
							var jsonScatterShowLastLabel = jsonTemplate.CHART.AXES_LIST.AXIS[1].showLastLabel;
							/* END */
							
							/**
							 * Variable used for HEATMAP chart (*_*)
							 */
							var jsonHeatmapChartSybmolWidth = jsonLegendStyle.symbolWidth ? jsonLegendStyle.symbolWidth : '';
							
							var colorPalette = [];
							if (jsonTemplate.CHART.COLORPALETTE
									&& jsonTemplate.CHART.COLORPALETTE.COLOR) {
								Ext.Array
										.each(
												jsonTemplate.CHART.COLORPALETTE.COLOR,
												function(color) {
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
											'Sbi.chart.designer.ChartConfigurationModel',
											{
												height : jsonTemplate.CHART.height,
												width : jsonTemplate.CHART.width,
												orientation : jsonTemplate.CHART.orientation ? jsonTemplate.CHART.orientation
														: 'vertical',
												backgroundColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonChartStyle.backgroundColor),
												font : jsonChartStyle.fontFamily,
												fontDimension : jsonChartStyle.fontSize,
												fontWeight : jsonChartStyle.fontWeight,

												// *_* Added for the SUNBURST
												opacMouseOver : jsonChartStyle.opacMouseOver,

												// *_* Added for the WORDCLOUD
												maxWords : jsonChartStyle.maxWords,
												maxAngle : jsonChartStyle.maxAngle,
												minAngle : jsonChartStyle.minAngle,
												maxFontSize : jsonChartStyle.maxFontSize,
												wordPadding : jsonChartStyle.wordPadding,
												sizeCriteria : jsonTemplate.CHART.sizeCriteria,

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
												legendColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonLegendStyle.color),
												legendBackgroundColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonLegendStyle.backgroundColor),

												colorPalette : colorPalette,

												// *_* Added for the TOOLBAR tag
												// for the SUNBURST chart
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

												// *_* Added for the TIP tag for
												// the SUNBURST chart
												tipText : jsonTipText,
												tipFontFamily : jsonTipStyle.fontFamily,
												tipFontWeight : jsonTipStyle.fontWeight,
												tipFontSize : jsonTipStyle.fontSize,
												tipColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonTipStyle.color),
												tipWidth : jsonTipStyle.width,

												// *_* Added for the TIP tag for
												// the PARALLEL chart (LIMIT
												// tag)
												maxNumberOfLines : jsonParallelLimitStyle.maxNumberOfLines,
												serieFilterColumn : jsonParallelLimitStyle.serieFilterColumn,
												orderTopMinBottomMax : jsonParallelLimitStyle.orderTopMinBottomMax,

												// *_* Added for the TIP tag for
												// the PARALLEL chart
												// (AXES_LINES tag)
												axisColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonParallelAxisStyle.axisColor),
												axisColNamePadd : jsonParallelAxisStyle.axisColNamePadd,
												brushColor : Sbi.chart.designer.ChartUtils
														.removeStartingHash(jsonParallelAxisStyle.brushColor),
												brushWidth : jsonParallelAxisStyle.brushWidth,

												// *_* Added for the TIP tag for
												// the PARALLEL chart (TOOLTIP
												// tag)
												parallelTooltipFontFamily : jsonParallelTooltipStyle.fontFamily,
												parallelTooltipFontSize : jsonParallelTooltipStyle.fontSize,
												parallelTooltipMinWidth : jsonParallelTooltipStyle.minWidth,
												parallelTooltipMaxWidth : jsonParallelTooltipStyle.maxWidth,
												parallelTooltipMinHeight : jsonParallelTooltipStyle.minHeight,
												parallelTooltipMaxHeight : jsonParallelTooltipStyle.maxHeight,
												parallelTooltipPadding : jsonParallelTooltipStyle.padding,
												parallelTooltipBorder : jsonParallelTooltipStyle.border,
												parallelTooltipBorderRadius : jsonParallelTooltipStyle.borderRadius,

												// *_* Added for the SCATTER chart 
												scatterZoomType : jsonScatterZoomType,
												scatterStartOnTick: jsonScatterStartOnTick,
												scatterEndOnTick: jsonScatterEndOnTick,
												scatterShowLastLabel: jsonScatterShowLastLabel,
												
												/**
												 * Added for the HEATMAP chart
												 */
												symbolWidth: jsonHeatmapChartSybmolWidth 
											});

							return cModel;
						},

						convertJsonToTreeFormat : function(data, level) {
							function isValue(data) {
								return (data != null && (typeof data === 'boolean'
										|| typeof data === 'string' || typeof data === 'number'));
							}

							var nivel = (level != undefined && typeof level === 'number') ? level
									: 0;
							var treeData = [];
							var keys = Object.keys(data);

							for (index in keys) {
								var key = keys[index];
								if (Array.isArray(data[key])) {
									var array = data[key];

									for (var i = 0; i < array.length; i++) {
										treeData
												.push({
													key : key,
													expanded : (nivel < 1),
													isArray : 1,
													children : ChartUtils
															.convertJsonToTreeFormat(
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

							if (nivel == 0) {
								var treeFormattedJson = {
									expanded : true,
									children : treeData
								};

								return treeFormattedJson;
							}
							return treeData;
						},

						convertTreeFormatToJson : function(data, isWrapper) {

							function areThereDifferentChildren(children) {
								if (children.length == 0) {
									return false;
								}
								var firstIsArray = children[0].isArray;
								for (i in children) {
									var isArray = children[i].isArray;
									if (firstIsArray != isArray) {
										return true;
									}
								}
								return false;
							}

							if (isWrapper && isWrapper == true) {
								var root = ChartUtils
										.convertTreeFormatToJson(data.children[0]);
								var rootKey = data.children[0].key;

								var result = {};
								result[rootKey] = root;
								return result;
							}

							if (data.leaf) {
								return data.value;
							} else if (data.children
									&& areThereDifferentChildren(data.children)) {
								var result = {};
								for (i in data.children) {
									var datum = data.children[i];
									if (result[datum.key] != undefined) { // Se
																			// già
																			// è
																			// presente
																			// un
																			// nodo
																			// conlo
																			// stesso
																			// nome
										var tempDatum = result[datum.key];
										if (Array.isArray(tempDatum)) {
											var newDatumKeyArray = [];
											for (j in tempDatum) {
												newDatumKeyArray
														.push(tempDatum[j]);
											}
											newDatumKeyArray
													.push(ChartUtils
															.convertTreeFormatToJson(datum));
											result[datum.key] = newDatumKeyArray;
										} else {
											var newDatumKeyArray = [];
											newDatumKeyArray.push(tempDatum);
											newDatumKeyArray
													.push(ChartUtils
															.convertTreeFormatToJson(datum));
											result[datum.key] = newDatumKeyArray;
										}
									} else {
										result[datum.key] = datum.isArray == 0 ? ChartUtils
												.convertTreeFormatToJson(datum)
												: [ ChartUtils
														.convertTreeFormatToJson(datum) ];
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
									array.push(ChartUtils
											.convertTreeFormatToJson(datum));
								}
								var result = {};
								result[data.children[0].key] = array;

								return result;
							} else {
								return {};
							}
						},

						clone : function(objToClone) {
							if (objToClone === null
									|| typeof (objToClone) !== 'object') {
								return objToClone;
							}

							var temp = objToClone.constructor();

							for ( var key in objToClone) {
								if (Object.prototype.hasOwnProperty.call(
										objToClone, key)) {
									temp[key] = ChartUtils
											.clone(objToClone[key]);
								}
							}
							return temp;
						},

						/**
						 * Creates a new merged object using matching key in
						 * case of array merging, keeping intact the original
						 * objects <code>target</code> and <code>source</code>.
						 * 
						 * @author Benedetto
						 * @param target
						 * @param source
						 */
						mergeObjects : function(target, source,
								removeNotFoundItemsFlag) {
							function isArray(o) {
								return Object.prototype.toString.call(o) == "[object Array]";
							}

							removeNotFoundItemsFlag = removeNotFoundItemsFlag || false;

							var item, tItem, o, idx;

							// If either argument is undefined, return the
							// other.
							// If both are undefined, return undefined.
							if (typeof source == 'undefined') {
								return source;
							} else if (typeof target == 'undefined') {
								return target;
							}

							var newTarget = ChartUtils.clone(target);
							// Assume both are objects and don't care about
							// inherited properties
							for ( var prop in source) {

								item = source[prop];
								if (typeof item == 'object' && item !== null) {

									if (isArray(item)) {

										// deal with arrays, will be either
										// array of primitives or array of
										// objects
										// If primitives
										if (item.length > 0
												&& typeof item[0] != 'object') {

											// if target doesn't have a similar
											// property, just reference it
											tItem = newTarget[prop];
											if (!tItem) {
												newTarget[prop] = item;

											} else {
												// Otherwise, copy only those
												// members that don't exist on
												// target

												// Create an index of items on
												// target
												o = {};
												for (var i = 0; i < tItem.length; i++) {
													o[tItem[i]] = true;
												}

												// Do check, push missing
												for (var j = 0; j < item.length; j++) {

													if (!(item[j] in o)) {
														tItem.push(item[j]);
													}
												}
											}
										} else {
											// Deal with array of objects
											// Create index of objects in target
											// object using ID property
											// Assume if target has same named
											// property then it will be similar
											// array
											idx = {};
											tItem = newTarget[prop];

											if (!tItem) {
												newTarget[prop] = item;
											} else {
												var forcedTItemArray = [];
												if (!isArray(tItem)) {
													// same length of source
													// array
													for ( var itemIndex in item) {
														var mixedMergedObj = ChartUtils
																.mergeObjects(
																		tItem,
																		item[itemIndex]);
														forcedTItemArray
																.push(mixedMergedObj);
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
														tItem
																.push(ChartUtils
																		.mergeObjects(
																				idxItem,
																				itemL));
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
										if (!tItem
												|| (isArray(tItem) && tItem.length == 0)) {
											newTarget[prop] = item;
										} else {
											newTarget[prop] = ChartUtils
													.mergeObjects(
															newTarget[prop],
															item);
										}
									}

								} else {
									// item is a primitive, just copy it over
									newTarget[prop] = item;
								}
							}

							if (removeNotFoundItemsFlag) {
								newTarget = ChartUtils.removeNotFoundItems(
										newTarget, source);
							}

							return newTarget;
						},

						removeNotFoundItems : function(target, source) {
							var newTarget = ChartUtils.clone(target);

							if (typeof newTarget == 'object'
									&& newTarget !== null) {
								for ( var prop in newTarget) {
									if (source[prop] == undefined) {
										delete newTarget[prop];
									} else if (!Array.isArray(newTarget[prop])) {
										newTarget[prop] = ChartUtils
												.removeNotFoundItems(
														newTarget[prop],
														source[prop]);
									}
								}
							}

							return newTarget;
						}
					}
				});