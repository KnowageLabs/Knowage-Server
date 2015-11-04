Ext.define('Sbi.chart.viewer.CrossNavigationHelper', {
	
	extend: 'Ext.util.Observable',
	
	statics: {
		breadcrumb: [],
		
		navigateTo: function(documentName, documentParameters, categoryName, categoryValue, serieName, serieValue, groupingCategoryName, groupingCategoryValue){ 
			var parametersAsString = '';
			
			for(var i = 0; i < documentParameters.length; i++) {
				var param = documentParameters[i];

				if (param.type == 'CATEGORY_NAME' && categoryName != null) {
					parametersAsString += param.urlName + '=' + categoryName + '&';
				} else if (param.type == 'CATEGORY_VALUE' && categoryValue != null) {
					parametersAsString += param.urlName + '=' + categoryValue + '&';
				} else if (param.type == 'SERIE_NAME' && serieName != null) {
					parametersAsString += param.urlName + '=' + serieName + '&';
				} else if (param.type == 'SERIE_VALUE' && serieValue != null) {
					parametersAsString += param.urlName + '=' + serieValue + '&';
				} else if (param.type == 'GROUPING_NAME' && groupingCategoryName != null) {
					parametersAsString += param.urlName + '=' + groupingCategoryName + '&';
				} else if (param.type == 'GROUPING_VALUE' && groupingCategoryValue != null) {
					parametersAsString += param.urlName + '=' + groupingCategoryValue + '&';
				} else if (param.type == 'ABSOLUTE' || param.type == 'RELATIVE') {
					parametersAsString += param.urlName + '=' + param.value + '&';
				}
			}
			
			var frameName = "iframe_" + this.displayName;
			parent.execCrossNavigation(frameName, documentName, parametersAsString);
		},
		
		navigateBackTo: function(){
			Sbi.chart.viewer.CrossNavigationHelper.breadcrumb.pop();
		}
	}
	
});