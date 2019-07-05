


(function(){

	var app = angular.module("JsonChartTemplateServiceModule");

	app.service("chartConfMergeService",["$parse",function ($parse){

		this.addProperty = function (advancedProp,chartConf) {
			if(advancedProp){
				for(var key in advancedProp){
					try{
						$parse(key).assign(chartConf,JSON.parse(advancedProp[key]));
					} catch (e) {
						$parse(key).assign(chartConf,advancedProp[key]);
					}

				}
			}
		}

	}]);

}());
