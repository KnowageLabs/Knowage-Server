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
    	
    	exportAsJson: function(){
    		var result = {};
    		var CHART = {};
    		
    		var AXES_LIST = {};
    		var AXIS = ChartUtils.getAxesDataAsOriginalJson();
    		AXES_LIST['AXIS'] = AXIS;
    		CHART['AXES_LIST'] = AXES_LIST;
    		
    		var VALUES = {};
    		var SERIE = ChartUtils.getSeriesDataAsOriginalJson();
    		VALUES['SERIE'] = SERIE;
    		
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
    		var serieStores = Sbi.chart.designer.ChartColumnsContainerManager.storePool;

    		var result = [];
    		
    		for(storeIndex in serieStores) {
    			/*
    			var tooltip = serie.TOOLTIP ? serie.TOOLTIP : {};
  						var tooltipStyle = serie.TOOLTIP ? serie.TOOLTIP.style : '';
  						var jsonTooltipStyle = jsonizeStyle(tooltipStyle);
  						
  						var newCol = Ext.create('Sbi.chart.designer.AxisesContainerModel', {
  							axisName: serie.name,
  							axisType: 'MEASURE',
  							
  							serieGroupingFunction: '',
  							serieType: serie.type,
  							serieOrderType: serie.orderType,
  							serieColumn: serie.column,
  							serieColor: serie.color,
  							serieShowValue: serie.showValue,
  							seriePrecision: serie.precision+'',
  							seriePrefixChar: serie.prefixChar,
  							seriePostfixChar: serie.postfixChar,
  							
  							serieTooltipTemplateHtml: tooltip.templateHtml,
  							serieTooltipBackgroundColor: tooltip.backgroundColor,
  							serieTooltipAlign: jsonTooltipStyle.align,
  							serieTooltipColor: jsonTooltipStyle.color,
  							serieTooltipFont: jsonTooltipStyle.font,
  							serieTooltipFontWeight: jsonTooltipStyle.fontWeight,
  							serieTooltipFontSize: jsonTooltipStyle.fontSize
  						});
    			 */
    			var store = serieStores[storeIndex];
    			var serie = {};
    			
    		
    			result.push(serie);
    		}
    		
    		return result;
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