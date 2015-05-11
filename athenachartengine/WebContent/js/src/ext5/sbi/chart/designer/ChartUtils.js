Ext.define('Sbi.chart.designer.ChartUtils', {
    extend: 'Ext.Base',
    alternateClassName: ['ChartUtils'],

    statics: {
    	ddGroup1: 'MEASURE',
		ddGroup2: 'ATTRIBUTE',
    	    	
    	convertJsonAxisObjToAxisData: function(axis) {
    		
    		var result = {};

    		result['alias'] = axis.alias;
    		result['axisType'] = axis.type;
    		result['position'] = axis.position;
    		
    		var axisStyle = axis.style.split(";");
    		var axisStyleAsMap = {};
    		
    		for(i in axisStyle){
    			var styleNameValue = axisStyle[i].split(':');
    			
    			axisStyleAsMap[styleNameValue[0]] = styleNameValue[1];
    		}

    		result['styleRotate'] = axisStyleAsMap.rotate;
    		result['styleAlign'] = axisStyleAsMap.align;
    		result['styleColor'] = axisStyleAsMap.color;
    		result['styleFont'] = axisStyleAsMap.font;
    		result['styleFontWeigh'] = axisStyleAsMap.fontWeight;
    		result['styleFontSize'] = axisStyleAsMap.fontSize;
    		
    		var majorgridStyle = axis.MAJORGRID.style.split(";");
    		var majorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MAJORGRID.style);
    		
    		result['majorgridInterval'] = axis.MAJORGRID.interval;
    		result['majorgridStyleTypeline'] = majorgridStyleAsMap.typeline;
    		result['majorgridStyleColor'] = majorgridStyleAsMap.color;
    		
    		var minorgridStyle = axis.MINORGRID.style.split(";");
    		var minorgridStyleAsMap = ChartUtils.jsonizeStyle(axis.MINORGRID.style);
    		result['minorgridInterval'] = axis.MINORGRID.interval;
    		result['minorgridStyleTypeline'] = minorgridStyleAsMap.typeline;
    		result['minorgridStyleColor'] = minorgridStyleAsMap.color;

    		result['titleText'] = axis.TITLE.text;
    		
    		var titlegridStyle = axis.TITLE.style.split(";");
    		var titlegridStyleAsMap = ChartUtils.jsonizeStyle(axis.TITLE.style);
    		result['titleStyleAlign'] = titlegridStyleAsMap.align;
    		result['titleStyleColor'] = titlegridStyleAsMap.color;
    		result['titleStyleFont'] = titlegridStyleAsMap.font;
    		result['titleStyleFontWeigh'] = titlegridStyleAsMap.fontWeight;
    		result['titleStyleFontSize'] = titlegridStyleAsMap.fontSize;
    		
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
    	jsonizeStyle: function (str) {
				var jsonStyle = {};
				var styles = str.split(';');
				for(style in styles) {
					var keyValue = style.split(':');
					jsonStyle[keyValue[0]] = keyValue[1];
				}
				
				return jsonStyle;
			}
    }
});