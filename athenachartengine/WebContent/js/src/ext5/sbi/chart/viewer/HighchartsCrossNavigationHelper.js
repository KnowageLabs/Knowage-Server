Ext.define('Sbi.chart.viewer.HighchartsCrossNavigationHelper', {
	
	extend: 'Ext.util.Observable',
	
	statics: {
		breadcrumb: [],
		
		navigateTo: function(documentName, documentParameters, categoryName, serieName){ 
			var parametersAsString = '';
			
			for(var i = 0; i < documentParameters.length; i++) {
				var param = documentParameters[i];
				
				if(param.type == 'CATEGORY_NAME') {
					parametersAsString += param.urlName + '=' + categoryName + '&';
				} else if(param.type == 'SERIE_NAME') {
					parametersAsString += param.urlName +  '=' + serieName + '&';
				} else if(param.type == 'ABSOLUTE' || param.type == 'RELATIVE') {
					parametersAsString += param.urlName +  '=' + param.value + '&';
				}
			}
			
			var frameName = "iframe_" + this.displayName;
			parent.execCrossNavigation(frameName, documentName, parametersAsString);
		},
		
		navigateBackTo: function(){
			Sbi.chart.viewer.HighchartsCrossNavigationHelper.breadcrumb.pop();
		}
	}
	
});