angular.module("cockpitModule").service("cockpitModule_crossServices",
		function(sbiModule_translate,sbiModule_restServices,cockpitModule_template, $q, $mdPanel,cockpitModule_widgetSelection,cockpitModule_properties,cockpitModule_utilstServices, $rootScope){
	var cross=this;
	this.crossList=[];
	
	this.loadCrossNavigationByDocument=function(docLabel){
		var def=$q.defer();
		sbiModule_restServices.restToRootProject();
		sbiModule_restServices.promiseGet("1.0/crossNavigation",docLabel+"/loadCrossNavigationByDocument")
		.then(function(response){
			angular.copy(response.data,cross.crossList);
			def.resolve();
		},function(response){
			sbiModule_restServices.errorHandler(response.data,"");
			def.reject();
		})
		return def.promise;
	};
	
	
	this.getCrossList=function(){
		return angular.copy(cross.crossList);
	}
	
	this.getChartParameters= function(type){
		var parameters= [];
		if(type==='BAR' || type=== 'LINE' || type === 'SCATTER' || type === 'RADAR' || type === 'PIE' || type=== 'TREEMAP' || type === 'WORDCLOUD'){
		parameters.push('SERIE_NAME');
		parameters.push('SERIE_VALUE');
		parameters.push('CATEGORY_NAME');
		parameters.push('CATEGORY_VALUE');
		}
		
		if(type==='GAUGE'){
			parameters.push('SERIE_NAME');
			parameters.push('SERIE_VALUE');
		}
		
		if(type==='PARALLEL'){
			parameters.push('GROUPING_NAME');
			parameters.push('GROUPING_VALUE');
			parameters.push('CATEGORY_NAME');
			parameters.push('CATEGORY_VALUE');
		}
		

		if(type==='HEATMAP'){
			parameters.push('GROUPING_NAME');
			parameters.push('GROUPING_VALUE');
			parameters.push('CATEGORY_NAME');
			parameters.push('CATEGORY_VALUE');
			parameters.push('SERIE_NAME');
			parameters.push('SERIE_VALUE');
		}
		
		return parameters;
	}
	
});