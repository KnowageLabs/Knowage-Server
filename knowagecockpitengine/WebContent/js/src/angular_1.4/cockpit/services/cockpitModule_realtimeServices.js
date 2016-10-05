angular.module("cockpitModule").service("cockpitModule_realtimeServices",function(cockpitModule_template,$interval,$rootScope){
	var rt=this;
	
	this.isRealTime=function(dsLabel,tmpList){
		var dsList=tmpList==undefined? cockpitModule_template.configuration.datasets:tmpList;
		for(var i=0;i<dsList.length;i++){
			if(angular.equals(dsList[i].dsLabel,dsLabel) || angular.equals(dsList[i].label,dsLabel)){
				return !dsList[i].useCache 
			}
		}
	}
	
	this.getRealTimeDatasetFromList=function(dsList,tmpList){
		var rtList=[];
		angular.forEach(dsList,function(ds){
			if(rt.isRealTime(ds,tmpList)){
				this.push(ds);
			}
		},rtList)
		return rtList
	}
	
	this.init=function(){
		angular.forEach(cockpitModule_template.configuration.aggregations,function(aggr){
			if(rt.getRealTimeDatasetFromList(aggr.datasets).length>0){
				$interval(function(){
					$rootScope.$broadcast("WIDGET_EVENT","UPDATE_FROM_REALTIME",{dsList:aggr.datasets});
				},(aggr.frequency==undefined?30:aggr.frequency)*60000)
			}
		})
		
	};
})