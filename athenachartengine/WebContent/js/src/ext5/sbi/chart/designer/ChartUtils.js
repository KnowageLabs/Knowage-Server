Ext.define('Sbi.chart.designer.ChartUtils', {
    extend: 'Ext.Base',
    alternateClassName: ['ChartUtils'],

    statics: {
    	ddGroupMeasure: 'MEASURE',
		ddGroupAttribute: 'ATTRIBUTE',
    	    	
    	convertJsonAxisObjToAxisData: function(axis) {
    		
    		var result = {};

    		result['alias'] = axis.alias;
    		result['axisType'] = axis.type;
    		result['position'] = axis.position;
    		
    		var axisStyleAsMap = ChartUtils.jsonizeStyle(axis.style);
    		result['styleRotate'] = axisStyleAsMap.rotate;
    		result['styleAlign'] = axisStyleAsMap.align;
    		result['styleColor'] = axisStyleAsMap.color;
    		result['styleFont'] = axisStyleAsMap.font;
    		result['styleFontWeigh'] = axisStyleAsMap.fontWeight;
    		result['styleFontSize'] = axisStyleAsMap.fontSize;
    		
    		if(axis.MAJORGRID) {
	    		result['majorgridInterval'] = axis.MAJORGRID.interval;
	    		
	    		var majorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MAJORGRID.style);
	    		result['majorgridStyleTypeline'] = majorgridStyleAsMap.typeline;
	    		result['majorgridStyleColor'] = majorgridStyleAsMap.color;
	    	}
    		if(axis.MINORGRID) {
	    		var minorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MINORGRID.style);
	    		result['minorgridInterval'] = axis.MINORGRID.interval;
	    		result['minorgridStyleTypeline'] = minorgridStyleAsMap.typeline;
	    		result['minorgridStyleColor'] = minorgridStyleAsMap.color;
    		}

    		if(axis.TITLE) {
	    		result['titleText'] = axis.TITLE.text;
	    		
	    		var titlegridStyleAsMap = ChartUtils.jsonizeStyle(axis.TITLE.style);
	    		result['titleStyleAlign'] = titlegridStyleAsMap.align;
	    		result['titleStyleColor'] = titlegridStyleAsMap.color;
	    		result['titleStyleFont'] = titlegridStyleAsMap.font;
	    		result['titleStyleFontWeigh'] = titlegridStyleAsMap.fontWeight;
	    		result['titleStyleFontSize'] = titlegridStyleAsMap.fontSize;
    		}
    		
    		return result;
    	}, 
    	createEmptyAxisData : function(){
    		var result = {};

    		result['alias'] = '';
    		result['axisType'] = '';
    		result['position'] = '';

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
    		
          	console.log('exportAsJson -> ' + result);
          	return result;
    	},
    	
    	getAxesDataAsOriginalJson: function() {
    		var result = [];
    		var leftAndRightAxisesContainers = [
    		    Ext.getCmp('chartLeftAxisesContainer'),
    		    Ext.getCmp('chartRightAxisesContainer')
    		];
    		
    		for(containerIndex in leftAndRightAxisesContainers) {
    			var axesContainer = leftAndRightAxisesContainers[containerIndex];
    			
    			var axesContainerItems = axesContainer.items.items;
    			for(index in axesContainerItems){
    				var leftAxis = axesContainerItems[index];
    				var axisData = leftAxis.axisData;
    				var axisAsJson = {};
    				
    				axisAsJson['alias'] = axisData.alias;
    				axisAsJson['type'] = axisData.axisType;
    				axisAsJson['position'] = axisData.position;
    				
    				var style = {};
    				style['rotate'] = axisData.styleRotate;
    				style['align'] = axisData.styleAlign;
    				style['color'] = axisData.styleColor;
    				style['font'] = axisData.styleFont;
    				style['fontWeight'] = axisData.styleFontWeigh;
    				style['fontSize'] = axisData.styleFontSize;
    				axisAsJson['style'] = style;
    				
    				var MAJORGRID = {}
    				MAJORGRID['interval'] = axisData.majorgridInterval;
    				var majorgridStyle = {};
    				majorgridStyle['typeline'] = axisData.majorgridStyleTypeline;
    				majorgridStyle['color'] = axisData.majorgridStyleColor;
    				MAJORGRID['style'] = majorgridStyle;
    				axisAsJson['MAJORGRID'] = MAJORGRID;
    				
    				var MINORGRID = {}
    				MINORGRID['interval'] = axisData.minorgridInterval;
    				var minorgridStyle = {};
    				minorgridStyle['typeline'] = axisData.minorgridStyleTypeline;
    				minorgridStyle['color'] = axisData.minorgridStyleColor;
    				MINORGRID['style'] = minorgridStyle;
    				axisAsJson['MINORGRID'] = MINORGRID;
    				
    				var TITLE = {};
    				TITLE['text'] = axisData.titleText;
    				var titleStyle = {};
    				titleStyle['align'] = axisData.titleStyleAlign;
    				titleStyle['color'] = axisData.titleStyleColor;
    				titleStyle['font'] = axisData.titleStyleFont;
    				titleStyle['fontWeight'] = axisData.titleStyleFontWeigh;
    				titleStyle['fontSize'] = axisData.titleStyleFontSize;
    				TITLE['style'] = titleStyle;
    				axisAsJson['TITLE'] = TITLE;
    				
    				result.push(axisAsJson);
    			}
    		}
    		
    		return result;
    	},
    	
    	getSeriesDataAsOriginalJson : function() {
    		var result = [];
    		
    		var serieStores = Sbi.chart.designer.ChartColumnsContainerManager.storePool;
    		for(storeIndex in serieStores) {
    			var store = serieStores[storeIndex];
    			var axisAlias = store.axisAlias;
    			
    			var storeSerieDataLength = store.data.items.length;
    			for(rowIndex = 0; rowIndex < storeSerieDataLength; rowIndex++) {
    				var serieAsMap = store.getAt(rowIndex);
    				var serie = {};
    				
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
    				
					var tooltipStyle = {};
					tooltipStyle['align'] = serieAsMap.get('serieTooltipAlign') != undefined? serieAsMap.get('serieTooltipAlign'): '';
					tooltipStyle['color'] = serieAsMap.get('serieTooltipColor') != undefined? serieAsMap.get('serieTooltipColor'): '';
					tooltipStyle['font'] = serieAsMap.get('serieTooltipFont') != undefined? serieAsMap.get('serieTooltipFont'): '';
					tooltipStyle['fontWeight'] = serieAsMap.get('serieTooltipFontWeight') != undefined? serieAsMap.get('serieTooltipFontWeight'): '';
					tooltipStyle['fontSize'] = serieAsMap.get('serieTooltipFontSize') != undefined? serieAsMap.get('serieTooltipFontSize'): '';
					TOOLTIP['style'] = tooltipStyle; 
    				
					serie['TOOLTIP'] = TOOLTIP
    				result.push(serie);
    			}
    		}
    		
    		return result;
    	},
    	
    	getCategoriesDataAsOriginalJson: function() {
    		var result = {};
    		
    		return result;
    	},
    	
    	getChartDataAsOriginaJson: function(chartModel) {
    		var CHART = {};
    		
    		CHART['height'] = chartModel.get('height');
    		CHART['width'] = chartModel.get('width');
    		CHART['orientation'] = chartModel.get('orientation');
    		
    		var COLORSPALLET = {};
    		var COLOR = [];
    		COLORSPALLET['COLOR'] = COLOR;
    		CHART['COLORSPALLET'] = COLORSPALLET;
    		
    		var EMPTYMESSAGE = {};
    		CHART['EMPTYMESSAGE'] = EMPTYMESSAGE;
    		
    		var TITLE = {};
    		TITLE['text'] = chartModel.get('title');
    		
    		var titleStyle = {};
    		titleStyle['align'] = chartModel.get('titleAlign');
    		titleStyle['color'] = chartModel.get('titleColor');
    		titleStyle['font'] = chartModel.get('titleFont');
    		titleStyle['fontWeight'] = chartModel.get('titleStyle');
    		titleStyle['fontSize'] = chartModel.get('titleDimension');
    		TITLE['style'] = titleStyle;
    		
    		CHART['TITLE'] = TITLE;
    		
    		var SUBTITLE = {};
    		SUBTITLE['text'] = chartModel.get('subtitle');
    		
    		var subtitleStyle = {};
    		subtitleStyle['align'] = chartModel.get('subtitleAlign');
    		subtitleStyle['color'] = chartModel.get('subtitleColor');
    		subtitleStyle['font'] = chartModel.get('subtitleFont');
    		subtitleStyle['fontWeight'] = chartModel.get('subtitleStyle');
    		subtitleStyle['fontSize'] = chartModel.get('subtitleDimension');
    		SUBTITLE['style'] = subtitleStyle;
    		
    		CHART['SUBTITLE'] = SUBTITLE;
    		
    		var LEGEND = {};
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
		}
    }
});