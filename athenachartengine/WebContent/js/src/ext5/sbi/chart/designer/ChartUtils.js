Ext.define('Sbi.chart.designer.ChartUtils', {
    extend: 'Ext.Base',
    alternateClassName: ['ChartUtils'],

    statics: {
    	ddGroupMeasure: 'MEASURE',
		ddGroupAttribute: 'ATTRIBUTE',
    	    	
    	convertJsonAxisObjToAxisData: function(axis) {
    		var result = {};

    		result['id'] = axis.alias && axis.alias != ''? axis.alias : '';
    		result['alias'] = axis.alias && axis.alias != ''? axis.alias : '';
    		result['axisType'] = axis.type && axis.type != ''? axis.type : '';
    		result['position'] = axis.position && axis.position != ''? axis.position : '';
    		
    		var axisStyleAsMap = ChartUtils.jsonizeStyle(axis.style);
    		result['styleRotate'] = axisStyleAsMap.rotate && axisStyleAsMap.rotate != ''? axisStyleAsMap.rotate : '';
    		result['styleAlign'] = axisStyleAsMap.align && axisStyleAsMap.align != ''? axisStyleAsMap.align : '';
    		result['styleColor'] = axisStyleAsMap.color && axisStyleAsMap.color != ''? axisStyleAsMap.color : '';
    		result['styleFont'] = axisStyleAsMap.font && axisStyleAsMap.font != ''? axisStyleAsMap.font : '';
    		result['styleFontWeigh'] = axisStyleAsMap.fontWeight && axisStyleAsMap.fontWeight != ''? axisStyleAsMap.fontWeight : '';
    		result['styleFontSize'] = axisStyleAsMap.fontSize && axisStyleAsMap.fontSize != ''? axisStyleAsMap.fontSize : '';
    		
    		if(axis.MAJORGRID) {
	    		result['majorgridInterval'] = axis.MAJORGRID.interval && axis.MAJORGRID.interval != ''? axis.MAJORGRID.interval : '';
	    		
	    		var majorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MAJORGRID.style);
	    		result['majorgridStyleTypeline'] = majorgridStyleAsMap.typeline && majorgridStyleAsMap.typeline != ''? majorgridStyleAsMap.typeline : '';
	    		result['majorgridStyleColor'] = majorgridStyleAsMap.color && majorgridStyleAsMap.color != ''? majorgridStyleAsMap.color : '';
	    	}
    		if(axis.MINORGRID) {
	    		var minorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MINORGRID.style);
	    		result['minorgridInterval'] = axis.MINORGRID.interval && axis.MINORGRID.interval != ''? axis.MINORGRID.interval : '';
	    		result['minorgridStyleTypeline'] = minorgridStyleAsMap.typeline && minorgridStyleAsMap.typeline != ''? minorgridStyleAsMap.typeline : '';
	    		result['minorgridStyleColor'] = minorgridStyleAsMap.color && minorgridStyleAsMap.color != ''? minorgridStyleAsMap.color : '';
    		}

    		if(axis.TITLE) {
	    		result['titleText'] = axis.TITLE.text && axis.TITLE.text != ''? axis.TITLE.text : '';
	    		
	    		var titlegridStyleAsMap = ChartUtils.jsonizeStyle(axis.TITLE.style);
	    		result['titleStyleAlign'] = titlegridStyleAsMap.align && titlegridStyleAsMap.align != ''? titlegridStyleAsMap.align : '';
	    		result['titleStyleColor'] = titlegridStyleAsMap.color && titlegridStyleAsMap.color != ''? titlegridStyleAsMap.color : '';
	    		result['titleStyleFont'] = titlegridStyleAsMap.font && titlegridStyleAsMap.font != ''? titlegridStyleAsMap.font : '';
	    		result['titleStyleFontWeigh'] = titlegridStyleAsMap.fontWeight && titlegridStyleAsMap.fontWeight != ''? titlegridStyleAsMap.fontWeight : '';
	    		result['titleStyleFontSize'] = titlegridStyleAsMap.fontSize && titlegridStyleAsMap.fontSize != ''? titlegridStyleAsMap.fontSize : '';
    		}
    		
    		return result;
    	}, 
    	createEmptyAxisData : function(){
    		var result = {};

    		result['id'] = 'Axis_' + ChartColumnsContainerManager.instanceIdFeed;
    		result['alias'] = 'Axis_' + ChartColumnsContainerManager.instanceIdFeed;
    		result['axisType'] = 'Serie';
    		result['position'] = 'right';

    		result['styleRotate'] = '';
    		result['styleAlign'] = '';
    		result['styleColor'] = '';
    		result['styleFont'] = '';
    		result['styleFontWeigh'] = '';
    		result['styleFontSize'] = '';
    		
    		result['majorgridInterval'] = '';
    		result['majorgridStyleTypeline'] = '';
    		result['majorgridStyleColor'] = '';
    		result['minorgridInterval'] = '';
    		result['minorgridStyleTypeline'] = '';
    		result['minorgridStyleColor'] = '';

    		result['titleText'] = '';
    		result['titleStyleAlign'] = '';
    		result['titleStyleColor'] = '';
    		result['titleStyleFont'] = '';
    		result['titleStyleFontWeigh'] = '';
    		result['titleStyleFontSize'] = '';
    		
    		return result;
    	},
    	
    	exportAsJson: function(chartModel){
    		var result = {};
    		var CHART = {};
    		
    		CHART['type'] = Sbi.chart.designer.Designer.chartTypeSelector.getChartType();
    		
    		var chartData = ChartUtils.getChartDataAsOriginaJson(chartModel);
    		Ext.apply(CHART, chartData);
    		    		
    		var AXES_LIST = {};
    		var AXIS = ChartUtils.getAxesDataAsOriginalJson();
    		AXES_LIST['AXIS'] = AXIS;
    		CHART['AXES_LIST'] = AXES_LIST;
    		
    		var VALUES = {};
    		var SERIE = ChartUtils.getSeriesDataAsOriginalJson();
    		VALUES['SERIE'] = SERIE;
    		var CATEGORY = ChartUtils.getCategoriesDataAsOriginalJson();
    		VALUES['CATEGORY'] = CATEGORY;
    		CHART['VALUES'] = VALUES;
    		
    		result['CHART'] = CHART;
    		
          	console.log('exportAsJson -> ' , result);
          	return result;
    	},
    	
    	getAxesDataAsOriginalJson: function() {
    		var result = [];

    		/* START Chart left and right axes data */
    		var leftAndRightAxisesContainers = [
    		    Ext.getCmp('chartLeftAxisesContainer'),
    		    Ext.getCmp('chartRightAxisesContainer')
    		];
    		
    		for(containerIndex in leftAndRightAxisesContainers) {
    			var axisContainer = leftAndRightAxisesContainers[containerIndex];
    			
    			var axisContainerItems = axisContainer.items.items;
    			for(index in axisContainerItems){
    				var axis = axisContainerItems[index];
    				var axisData = axis.axisData;
    				var axisAsJson = {};
    				
    				axisAsJson['id'] = axisData.id;
    				axisAsJson['alias'] = axisData.alias;
    				axisAsJson['type'] = axisData.axisType? axisData.axisType: '';
    				axisAsJson['position'] = axisData.position? axisData.position: '';
    				
    				var style = '';
    				style += 'rotate:' + ((axisData.styleRotate != undefined)? axisData.styleRotate : '') + ';';
    				style += 'align:' + ((axisData.styleAlign != undefined)? axisData.styleAlign : '') + ';';
    				style += 'color:' + ((axisData.styleColor != undefined)? axisData.styleColor : '') + ';';
    				style += 'font:' + ((axisData.styleFont != undefined)? axisData.styleFont : '') + ';';
    				style += 'fontWeight:' + ((axisData.styleFontWeigh != undefined)? axisData.styleFontWeigh : '') + ';';
    				style += 'fontSize:' + ((axisData.styleFontSize != undefined)? axisData.styleFontSize : '') + ';';
    				axisAsJson['style'] = style;
    				
    				var MAJORGRID = {};
    				MAJORGRID['interval'] = axisData.majorgridInterval? axisData.majorgridInterval : '';
    				var majorgridStyle = '';
    				majorgridStyle += 'typeline:' + ((axisData.majorgridStyleTypeline != undefined)? axisData.majorgridStyleTypeline: '') + ';';
    				majorgridStyle += 'color:' + ((axisData.majorgridStyleColor != undefined)? axisData.majorgridStyleColor: '') + ';';
    				MAJORGRID['style'] = majorgridStyle;
    				axisAsJson['MAJORGRID'] = MAJORGRID;
    				
    				var MINORGRID = {};
    				MINORGRID['interval'] = axisData.minorgridInterval? axisData.minorgridInterval: '';
    				var minorgridStyle = '';
    				minorgridStyle += 'typeline:' + ((axisData.minorgridStyleTypeline != undefined)? axisData.minorgridStyleTypeline: '') + ';';
    				minorgridStyle += 'color:' + ((axisData.minorgridStyleColor != undefined)? axisData.minorgridStyleColor: '') + ';';
    				MINORGRID['style'] = minorgridStyle;
    				axisAsJson['MINORGRID'] = MINORGRID;
    				
    				var TITLE = {};
    				TITLE['text'] = axisData.titleText? axisData.titleText : '';
    				var titleStyle = '';
    				titleStyle += 'align:' + ((axisData.titleStyleAlign != undefined)? axisData.titleStyleAlign: '') + ';';
    				titleStyle += 'color:' + ((axisData.titleStyleColor != undefined)? axisData.titleStyleColor: '') + ';';
    				titleStyle += 'font:' + ((axisData.titleStyleFont != undefined)? axisData.titleStyleFont: '') + ';';
    				titleStyle += 'fontWeight:' + ((axisData.titleStyleFontWeigh != undefined)? axisData.titleStyleFontWeigh: '') + ';';
    				titleStyle += 'fontSize:' + ((axisData.titleStyleFontSize != undefined)? axisData.titleStyleFontSize: '') + ';';
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
			style += 'rotate:' + ((axisData.styleRotate != undefined)? axisData.styleRotate : '') + ';';
			style += 'align:' + ((axisData.styleAlign != undefined)? axisData.styleAlign : '') + ';';
			style += 'color:' + ((axisData.styleColor != undefined)? axisData.styleColor : '') + ';';
			style += 'font:' + ((axisData.styleFont != undefined)? axisData.styleFont : '') + ';';
			style += 'fontWeight:' + ((axisData.styleFontWeigh != undefined)? axisData.styleFontWeigh : '') + ';';
			style += 'fontSize:' + ((axisData.styleFontSize != undefined)? axisData.styleFontSize : '') + ';';
			axisAsJson['style'] = style;
			
			var TITLE = {};
			TITLE['text'] = (axisData.titleText != undefined)? axisData.titleText: ''; 
			var titleStyle = '';
			titleStyle += 'align:' + ((axisData.titleStyleAlign != undefined)? axisData.titleStyleAlign: '') + ';';
			titleStyle += 'color:' + ((axisData.titleStyleColor != undefined)? axisData.titleStyleColor: '') + ';';
			titleStyle += 'font:' + ((axisData.titleStyleFont != undefined)? axisData.titleStyleFont: '') + ';';
			titleStyle += 'fontWeight:' + ((axisData.titleStyleFontWeigh != undefined)? axisData.titleStyleFontWeigh: '') + ';';
			titleStyle += 'fontSize:' + ((axisData.titleStyleFontSize != undefined)? axisData.titleStyleFontSize: '') + ';';
			TITLE['style'] = titleStyle;
			axisAsJson['TITLE'] = TITLE;
			
			result.push(axisAsJson);
    		/* END Chart bottom axis data */
    		
    		return result;
    	},
    	
    	getSeriesDataAsOriginalJson : function() {
    		var result = [];
    		
    		var serieStores = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
    		for(storeIndex in serieStores) {
    			var store = serieStores[storeIndex];
    			var axisAlias = store.axisAlias;
    			
    			var storeSerieDataLength = store.data.items.length;
    			for(var rowIndex = 0; rowIndex < storeSerieDataLength; rowIndex++) {
    				var serieAsMap = store.getAt(rowIndex);
    				var serie = {};
    				
    				serie['id'] = serieAsMap.get('id') != undefined? serieAsMap.get('id'): '';;
    				serie['axis'] = axisAlias;
    				serie['color'] = serieAsMap.get('serieColor') != undefined? serieAsMap.get('serieColor'): '';
    				serie['column'] = serieAsMap.get('serieColumn') != undefined? serieAsMap.get('serieColumn'): '';
    				serie['groupingFunction'] = serieAsMap.get('serieGroupingFunction') != undefined? serieAsMap.get('serieGroupingFunction'): '';
    				serie['name'] = serieAsMap.get('axisName') != undefined? serieAsMap.get('axisName'): '';
    				serie['orderType'] = serieAsMap.get('serieOrderType') != undefined? serieAsMap.get('serieOrderType'): '';
    				serie['postfixChar'] = serieAsMap.get('seriePostfixChar') != undefined? serieAsMap.get('seriePostfixChar'): '';
    				serie['precision'] = serieAsMap.get('seriePrecision') != undefined? serieAsMap.get('seriePrecision'): '';
    				serie['prefixChar'] = serieAsMap.get('seriePrefixChar') != undefined? serieAsMap.get('seriePrefixChar'): '';
    				serie['showValue'] = serieAsMap.get('serieShowValue') != undefined? serieAsMap.get('serieShowValue'): '';
    				serie['type'] = serieAsMap.get('serieType') != undefined? serieAsMap.get('serieType'): '';
    				
    				var TOOLTIP = {};
    				TOOLTIP['backgroundColor'] = serieAsMap.get('serieTooltipBackgroundColor') != undefined? 
    						serieAsMap.get('serieTooltipBackgroundColor'): '';
    				TOOLTIP['templateHtml'] = serieAsMap.get('serieTooltipTemplateHtml') != undefined? 
    						serieAsMap.get('serieTooltipTemplateHtml'): '';
    				
					var tooltipStyle = '';
					tooltipStyle += 'align:' + ((serieAsMap.get('serieTooltipAlign') != undefined)? serieAsMap.get('serieTooltipAlign'): '') + ';';					
					tooltipStyle += 'color:' + ((serieAsMap.get('serieTooltipColor') != undefined)? serieAsMap.get('serieTooltipColor'): '') + ';';					
					tooltipStyle += 'font:' + ((serieAsMap.get('serieTooltipFont') != undefined)? serieAsMap.get('serieTooltipFont'): '') + ';';					
					tooltipStyle += 'fontWeight:' + ((serieAsMap.get('serieTooltipFontWeight') != undefined)? serieAsMap.get('serieTooltipFontWeight'): '') + ';';					
					tooltipStyle += 'fontSize:' + ((serieAsMap.get('serieTooltipFontSize') != undefined)? serieAsMap.get('serieTooltipFontSize'): '') + ';';					
					TOOLTIP['style'] = tooltipStyle; 
    				
					serie['TOOLTIP'] = TOOLTIP;
    				result.push(serie);
    			}
    		}
    		
    		return result;
    	},
    	
    	getCategoriesDataAsOriginalJson: function() {
    		var categoriesStore = Ext.data.StoreManager.lookup('categoriesStore');
    		
    		var mainCategory = categoriesStore.getAt(0);
    		
    		var result = {};
    		result['name'] = mainCategory.get('axisName') != undefined? mainCategory.get('axisName') : mainCategory.get('categoryColumn');
    		result['column'] = mainCategory.get('categoryColumn') != undefined? mainCategory.get('categoryColumn') : '';
    		result['orderColumn'] = mainCategory.get('categoryOrderColumn') != undefined? mainCategory.get('categoryOrderColumn') : '';
    		result['orderType'] = mainCategory.get('categoryOrderType') != undefined? mainCategory.get('categoryOrderType') : '';
    		result['stackedType'] = mainCategory.get('categoryOrderType') != undefined? mainCategory.get('categoryOrderType') : '';
    		result['stacked'] = mainCategory.get('categoryStacked') != undefined? mainCategory.get('categoryStacked') : '';
    		
    		var categoriesStoreDataLength = categoriesStore.data.items.length;
    		
    		var groupby = ''; 
    		var groupbyNames = ''; 
    		if (categoriesStoreDataLength > 1) {
    			for(var rowIndex = 1; rowIndex < categoriesStoreDataLength; rowIndex++) {
    				var categorieItem = categoriesStore.getAt(rowIndex);
    				groupby += categorieItem.get('categoryColumn') != undefined ? categorieItem.get('categoryColumn') + ',' : '';
    				groupbyNames += categorieItem.get('axisName') != undefined ? categorieItem.get('axisName') + ',' : '';
    			}
    		}
    		result['groupby'] = groupby.replace(/\,$/,'');
    		result['groupbyNames'] = groupbyNames.replace(/\,$/,'');;
    		
    		return result;
    	},
    	
    	getChartDataAsOriginaJson: function(chartModel) {
    		var CHART = {};
    		
    		CHART['height'] = (chartModel.get('height') != undefined)? chartModel.get('height') : '';
    		CHART['width'] = (chartModel.get('width') != undefined)? chartModel.get('width') : '';
    		CHART['orientation'] = (chartModel.get('orientation') != undefined)? chartModel.get('orientation') : '';
    		
    		var chartStyle = '';
    		chartStyle += 'font:' + ((chartModel.get('font') != undefined)? chartModel.get('font') : '') + ';';
    		chartStyle += 'fontSize:' + ((chartModel.get('fontDimension') != undefined)? chartModel.get('fontDimension') : '') + ';';
    		chartStyle += 'fontWeight:' + ((chartModel.get('fontWeight') != undefined)? chartModel.get('fontWeight') : '') + ';';
    		chartStyle += 'backgroundColor:' + ((chartModel.get('backgroundColor') != undefined && chartModel.get('backgroundColor') != '')? 
    				'#' + chartModel.get('backgroundColor') : '') + ';';
    		CHART['style'] = chartStyle;
    		
    		var COLORPALETTE = {};
    		var COLOR = [];
    		var paletteStore = Ext.data.StoreManager.lookup('chartConfigurationPaletteStore');
    		var colors = paletteStore.getData();
    		for(i in colors.items){
    			var color = paletteStore.getAt(i);
    			var colorElement = {};
    			colorElement['id'] = color.get('id') != undefined? color.get('id'): '';
    			colorElement['gradient'] = color.get('gradient') != undefined? color.get('gradient'): '';
    			colorElement['name'] = color.get('name') != undefined? color.get('name'): '';
    			colorElement['order'] = color.get('order') != undefined? color.get('order'): '';
    			
    			var colorValue = color.get('value') != undefined? color.get('value'): '';
    			colorValue = colorValue.replace(/^#?([\dA-Fa-f]+)/, '#$1');
    			colorElement['value'] = colorValue;
    			
    			COLOR.push(colorElement);
    		}
    		
    		COLORPALETTE['COLOR'] = COLOR;
    		CHART['COLORPALETTE'] = COLORPALETTE;
    		
    		var EMPTYMESSAGE = {};
    		EMPTYMESSAGE['text'] = (chartModel.get('nodata') != undefined)? chartModel.get('nodata') : '';
    		
    		var emptymessageStyle = '';
    		emptymessageStyle += 'align:' + ((chartModel.get('nodataAlign') != undefined)? chartModel.get('nodataAlign') : '') + ';';
    		emptymessageStyle += 'color:'+ ((chartModel.get('nodataColor') != undefined)? '#' + chartModel.get('nodataColor') : '') + ';';
    		emptymessageStyle += 'font:' + ((chartModel.get('font') != undefined)? chartModel.get('font') : '') + ';';
    		emptymessageStyle += 'fontWeight:' + ((chartModel.get('nodataStyle') != undefined)? chartModel.get('nodataStyle') : '') + ';';
    		emptymessageStyle += 'fontSize:' + ((chartModel.get('nodataDimension') != undefined)? chartModel.get('nodataDimension') : '') + ';';
    		EMPTYMESSAGE['style'] = emptymessageStyle;
    		
    		CHART['EMPTYMESSAGE'] = EMPTYMESSAGE;
    		
    		var TITLE = {};
    		TITLE['text'] = (chartModel.get('title') != undefined)? chartModel.get('title') : '';
    		
    		var titleStyle = '';
    		titleStyle += 'align:' + ((chartModel.get('titleAlign') != undefined)? chartModel.get('titleAlign') : '') + ';';
    		titleStyle += 'color:' + ((chartModel.get('titleColor') != undefined)? '#' + chartModel.get('titleColor') : '') + ';';
    		titleStyle += 'font:' + ((chartModel.get('titleFont') != undefined)? chartModel.get('titleFont') : '') + ';';
    		titleStyle += 'fontWeight:' + ((chartModel.get('titleStyle') != undefined)? chartModel.get('titleStyle') : '') + ';';
    		titleStyle += 'fontSize:' + ((chartModel.get('titleDimension') != undefined)? chartModel.get('titleDimension') : '') + ';';
    		TITLE['style'] = titleStyle;
    		
    		CHART['TITLE'] = TITLE;
    		
    		var SUBTITLE = {};
    		SUBTITLE['text'] = (chartModel.get('subtitle') != undefined)? chartModel.get('subtitle') : '';
    		
    		var subtitleStyle = '';
    		subtitleStyle += 'align:' + ((chartModel.get('subtitleAlign') != undefined)? chartModel.get('subtitleAlign') : '') + ';';
    		subtitleStyle += 'color:' + ((chartModel.get('subtitleColor') != undefined)? '#' + chartModel.get('subtitleColor') : '') + ';';
    		subtitleStyle += 'font:' + ((chartModel.get('subtitleFont') != undefined)? chartModel.get('subtitleFont') : '') + ';';
    		subtitleStyle += 'fontWeight:' + ((chartModel.get('subtitleStyle') != undefined)? chartModel.get('subtitleStyle') : '') + ';';
    		subtitleStyle += 'fontSize:' + ((chartModel.get('subtitleDimension') != undefined)? chartModel.get('subtitleDimension') : '') + ';';
    		SUBTITLE['style'] = subtitleStyle;
    		
    		CHART['SUBTITLE'] = SUBTITLE;
    		
    		var LEGEND = {};
    		LEGEND['position'] = (chartModel.get('legendPosition') != undefined)? chartModel.get('legendPosition') : '';
    		LEGEND['layout'] = (chartModel.get('legendLayout') != undefined)? chartModel.get('legendLayout') : '';
    		LEGEND['floating'] = (chartModel.get('legendFloating') != undefined)? chartModel.get('legendFloating') : '';
    		LEGEND['x'] = (chartModel.get('legendX') != undefined)? chartModel.get('legendX') : '';
    		LEGEND['y'] = (chartModel.get('legendY') != undefined)? chartModel.get('legendY') : '';
    		
    		var legendStyle = '';
    		legendStyle += 'color:' + ((chartModel.get('legendColor') != undefined)? '#' + chartModel.get('legendColor') : '') + ';';
    		legendStyle += 'font:' + ((chartModel.get('legendFont') != undefined)? chartModel.get('legendFont') : '') + ';';
    		legendStyle += 'fontSize:' + ((chartModel.get('legendDimension') != undefined)? chartModel.get('legendDimension') : '') + ';';
    		legendStyle += 'fontWeight:' + ((chartModel.get('legendStyle') != undefined)? chartModel.get('legendStyle') : '') + ';';
    		legendStyle += 'borderWidth:' + ((chartModel.get('legendBorderWidth') != undefined)? chartModel.get('legendBorderWidth') : '') + ';';
    		legendStyle += 'backgroundColor:' + ((chartModel.get('legendBackgroundColor') != undefined)? chartModel.get('legendBackgroundColor') : '') + ';';
    		LEGEND['style'] = legendStyle;
    		
    		CHART['LEGEND'] = LEGEND;
    		
    		return CHART;
    	},
    	
    	jsonizeStyle: function (str) {
			var jsonStyle = {};
			if(str) {
				var styles = str.split(';');
				for(index in styles) {
					var keyValue = styles[index].split(':');
					jsonStyle[keyValue[0]] = keyValue[1];
				}
			}
			
			return jsonStyle;
		},
		
		removeStartingHash: function(colorWithHash) {
			return colorWithHash ? colorWithHash.replace("#", '') : colorWithHash;
		},
		
		createChartConfigurationModelFromJson: function(jsonTemplate){
			var jsonChartStyle = Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.style);
  			
  			var jsonTitleText = jsonTemplate.CHART.TITLE ? jsonTemplate.CHART.TITLE.text : '';
  			var jsonTitleStyle = jsonTemplate.CHART.TITLE ? Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.TITLE.style) : {};
  			
  			var jsonSubtitleText = jsonTemplate.CHART.SUBTITLE ? jsonTemplate.CHART.SUBTITLE.text : '';
  			var jsonSubtitleStyle = jsonTemplate.CHART.SUBTITLE ? Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.SUBTITLE.style) : {};
  			
  			var jsonEmptyMsgText = jsonTemplate.CHART.EMPTYMESSAGE ? jsonTemplate.CHART.EMPTYMESSAGE.text : '';
  			var jsonEmptyMsgStyle = jsonTemplate.CHART.EMPTYMESSAGE ? Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.EMPTYMESSAGE.style) : {};
  			
  			var chartLegend = jsonTemplate.CHART.LEGEND ? jsonTemplate.CHART.LEGEND : '';
  			var jsonLegendStyle = jsonTemplate.CHART.LEGEND ? Sbi.chart.designer.ChartUtils.jsonizeStyle(jsonTemplate.CHART.LEGEND.style) : {};
  			
  			var colorPalette = [];
  			if(jsonTemplate.CHART.COLORPALETTE && jsonTemplate.CHART.COLORPALETTE.COLOR) {
  				Ext.Array.each(jsonTemplate.CHART.COLORPALETTE.COLOR, function(color) {
  					colorPalette.push({
						'id': color.id != undefined ? color.id : color.name,
						'gradient': color.gradient != undefined ? color.gradient : '',
						'name': color.name,
						'order': color.order, 
						'value': Sbi.chart.designer.ChartUtils.removeStartingHash(color.value)
					});
  				});
  			}
  			
  			var cModel = Ext.create('Sbi.chart.designer.ChartConfigurationModel', {
  				height: jsonTemplate.CHART.height,
  				width: jsonTemplate.CHART.width, 
  				orientation: jsonTemplate.CHART.orientation,
  				backgroundColor: Sbi.chart.designer.ChartUtils.removeStartingHash(jsonChartStyle.backgroundColor),
  				font: jsonChartStyle.font,
  				fontDimension: jsonChartStyle.fontSize,
  				fontWeight: jsonChartStyle.fontWeight,
  				
  				title: jsonTitleText,
  				titleAlign: jsonTitleStyle.align,
  				titleColor: Sbi.chart.designer.ChartUtils.removeStartingHash(jsonTitleStyle.color),
  				titleFont: jsonTitleStyle.font,
  				titleDimension: jsonTitleStyle.fontSize,
  				titleStyle: jsonTitleStyle.fontWeight,
  				  				
  				subtitle: jsonSubtitleText,
  				subtitleAlign: jsonSubtitleStyle.align,
  				subtitleColor: Sbi.chart.designer.ChartUtils.removeStartingHash(jsonSubtitleStyle.color),
  				subtitleFont: jsonSubtitleStyle.font,
  				subtitleDimension: jsonSubtitleStyle.fontSize,
  				subtitleStyle: jsonSubtitleStyle.fontWeight,
  				
  				nodata: jsonEmptyMsgText,
  				nodataAlign: jsonEmptyMsgStyle.align,
  				nodataColor: Sbi.chart.designer.ChartUtils.removeStartingHash(jsonEmptyMsgStyle.color),
  				nodataFont: jsonEmptyMsgStyle.font,
  				nodataDimension: jsonEmptyMsgStyle.fontSize,
  				nodataStyle: jsonEmptyMsgStyle.fontWeight,
  				  				
  				legendPosition: chartLegend.position,
  				legendLayout: chartLegend.layout,
  				legendFloating: chartLegend.floating,
  				legendX: chartLegend.x,
  				legendY: chartLegend.y,
  				legendAlign: jsonTemplate.CHART.align,
  				legendColor: Sbi.chart.designer.ChartUtils.removeStartingHash(jsonLegendStyle.color),
  				legendFont: jsonLegendStyle.font,
  				legendDimension: jsonLegendStyle.fontSize,
  				legendStyle: jsonLegendStyle.fontWeight,
  				legendBorderWidth: jsonLegendStyle.borderWidth,
  				legendBackgroundColor: jsonLegendStyle.backgroundColor,
  				
  				colorPalette: colorPalette
  			});
  			
  			return cModel;
		},
		

		convertJsonToTreeFormat: function(data, level){
		   	function isValue(data) {
				return ( data != null 
					&& (typeof data === 'boolean' 
						|| typeof data === 'string' 
						|| typeof data === 'number'));
			}
		    
			var nivel = (level != undefined && typeof level === 'number')? level : 0;
			var treeData = [];
			var keys = Object.keys(data);
			
			for(index in keys) {
				var key = keys[index];
				if (Array.isArray(data[key])) {
					var array = data[key];
					
//					if(array.length == 0) {
//						treeData.push({
//							key: key,
//							expanded: (nivel < 1),
//							isArray: true,
//							children: [],
//						});
//					} else {
//					}
					for(var i = 0; i < array.length; i++){
						treeData.push({
							key: key,
							expanded: (nivel < 1),
							isArray: 1,
							children: ChartUtils.convertJsonToTreeFormat(array[i], nivel + 1),
						});
					}
				} else if( isValue(data[key]) ) {
		            var type = 'object';
		            if (typeof data[key] === 'boolean') type = 'boolean';
		            if (typeof data[key] === 'string') type = 'string';
		            if (typeof data[key] === 'number') type = 'number';
		            
					treeData.push({
						key: key,
						value: data[key],
		                type: type ,
						isArray: 0,
						leaf: true
					});
				} else {
					treeData.push({
						key: key,
						expanded: (nivel < 1),
						isArray: 0,
						children: ChartUtils.convertJsonToTreeFormat(data[key], nivel + 1)
					});
				}
			}
			
			if(nivel == 0) {
				var treeFormattedJson = {
						expanded: true,
						children: treeData
					};
				
				console.log('treeFormattedJson: ', treeFormattedJson);
				return treeFormattedJson;
			}
		    return treeData;
		},

		convertTreeFormatToJson: function(data, isWrapper){
			
			function areThereDifferentChildren(children) {
				if(children.length == 0) {
					return false;
				}
				var firstIsArray = children[0].isArray;
				for(i in children) {
					var isArray = children[i].isArray;
					if(firstIsArray != isArray) {
						return true;
					}
				}
				return false;
			}
			
			if(isWrapper && isWrapper == true) {
				var root = ChartUtils.convertTreeFormatToJson(data.children[0]);
				var rootKey = data.children[0].key;
				
				var result = {};
				result[rootKey] = root;
				return result;
			}
			
			if(data.leaf) {
				return data.value;
			} else if(data.children && areThereDifferentChildren(data.children)) {
				var result = {};
				for(i in data.children) {
					var datum = data.children[i];
					if(result[datum.key] != undefined) { //Se già è presente un nodo conlo stesso nome
						var tempDatum = result[datum.key];
						if(Array.isArray(tempDatum)){
							var newDatumKeyArray = [];
							for(j in tempDatum) {
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
						result[datum.key] = datum.isArray == 0 ? ChartUtils.convertTreeFormatToJson(datum) : [ChartUtils.convertTreeFormatToJson(datum)];
					}
				}
				return result;
				
			} else if(data.children && data.children[0] && data.children[0].isArray == 0) {
				var result = {};
				
				for(i in data.children) {
					var datum = data.children[i];
					result[datum.key] = ChartUtils.convertTreeFormatToJson(datum);
				}
				return result;
			} else if(data.children && data.children[0] && data.children[0].isArray == 1) {
				var array = [];
				
				for(i in data.children) {
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
		
		clone: function(objToClone) {
		    if(objToClone === null || typeof(objToClone) !== 'object') {
		        return objToClone;
		    }
		     
		    var temp = objToClone.constructor();
		    
		    for(var key in objToClone) {
		        if(Object.prototype.hasOwnProperty.call(objToClone, key)) {
		            temp[key] = ChartUtils.clone(objToClone[key]);
		        }
		    }
		    return temp;
		},
		
		/**
		 * Creates a new merged object using matching key in case of array merging, 
		 * keeping intact the original objects <code>target</code> and <code>source</code>.
		 * 
		 * @author Benedetto
		 * @param target
		 * @param source 
		 */
		mergeObjects: function(target, source) {
			function isArray(o) {
				return Object.prototype.toString.call(o) == "[object Array]";
			}

			var item, tItem, o, idx;

			// If either argument is undefined, return the other.
			// If both are undefined, return undefined.
			if (typeof source == 'undefined') {
				return source;
			} else if (typeof target == 'undefined') {
				return target;
			}

			var newTarget = ChartUtils.clone(target);
			// Assume both are objects and don't care about inherited properties
			for (var prop in source) {

				item = source[prop];
				if (typeof item == 'object' && item !== null) {

					if (isArray(item)) {

						// deal with arrays, will be either array of primitives or array of objects
						// If primitives
						if (item.length > 0 && typeof item[0] != 'object') {

							// if target doesn't have a similar property, just reference it
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

									if ( !(item[j] in o) ) {
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
								if( !isArray(tItem)) {
									//same length of source array
									for(var itemIndex in item) {
										var mixedMergedObj = ChartUtils.mergeObjects(tItem, item[itemIndex]);
										forcedTItemArray.push(mixedMergedObj);
									}
									
									tItem = forcedTItemArray;
								}
								
								for (var k=0; k < tItem.length; k++) {
									var tItemK = tItem[k];
									
									var idValue = tItemK.id;
									for (var l=0; l < item.length; l++) {
										var itemL = item[l];
										if( itemL.id == idValue) {
											idx[tItemK.id] = tItemK;
											break;
										}
									}
								}
								
								while(tItem.length > 0) {
									tItem.pop();
								}
								
								// Do updates
								for (var l=0; l < item.length; l++) {
									var itemL = item[l];
									var idxItem = idx[itemL.id];
									
									if(idxItem != undefined) {
										tItem.push(ChartUtils.mergeObjects(idxItem, itemL));
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
						if (!tItem) {
							newTarget[prop] = item;
						} else {
							newTarget[prop] = ChartUtils.mergeObjects(newTarget[prop], item);
						}
					}

				} else {
					// item is a primitive, just copy it over
					newTarget[prop] = item;
				}
			}
			return newTarget;
		},
    }
});