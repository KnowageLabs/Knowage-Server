Ext.define('Sbi.chart.viewer.HighchartsCrossNavigationHelper', {
	
	extend: 'Ext.util.Observable',
	
	statics: {
		breadcrumb: [],
		
		navigateTo: function(documentName, documentParameters, categoryName, serieName){ 
//			console.log('navigateTo(documentName) -> ', documentName);
//			console.log('navigateTo(documentParameters) -> ', documentParameters);
//			console.log('navigateTo(categoryName) -> ', categoryName);
//			console.log('navigateTo(serieName) -> ', serieName);
			
			var parametersAsString = '';
			
			for(var i = 0; i < documentParameters.length; i++) {
				var param = documentParameters[i];
				
				if(param.type == 'CATEGORY') {
					parametersAsString += param.urlName + '=' + categoryName + '&';
				} else if(param.type == 'SERIE') {
					parametersAsString += param.urlName +  '=' + serieName + '&';
				} else if(param.type == 'ABSOLUTE' || param.type == 'RELATIE') {
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