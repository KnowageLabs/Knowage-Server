


(function(){

	var app = angular.module("JsonChartTemplateServiceModule");

	app.service("chartConfMergeService",["$parse",function ($parse){

		this.addProperty = function (advancedProp,chartConf) {
			if(advancedProp){
				for(var key in advancedProp){
					$parse(key).assign(chartConf,advancedProp[key]);
				}
			}
		}

	}]);

}());
