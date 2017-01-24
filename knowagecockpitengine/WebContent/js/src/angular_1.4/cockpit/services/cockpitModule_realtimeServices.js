angular.module("cockpitModule").service("cockpitModule_realtimeServices",function(cockpitModule_template,$interval,$rootScope,cockpitModule_templateServices){
	var rt=this;
	
	this.isRealTime=function(dsLabel,tmpList){
		var dsList=tmpList==undefined? cockpitModule_template.configuration.datasets:tmpList;
		for(var i=0;i<dsList.length;i++){
			if(angular.equals(dsList[i].dsLabel,dsLabel) || angular.equals(dsList[i].label,dsLabel)){
				return !dsList[i].useCache 
			}
		}
	}
	this.getDatasetFrequency=function(dsLabel){
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			if(angular.equals(cockpitModule_template.configuration.datasets[i].dsLabel,dsLabel)){
				return cockpitModule_template.configuration.datasets[i].frequency; 
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
					debugger;
				},(aggr.frequency==undefined?60:aggr.frequency)*1000)
			}
		})
		
		angular.forEach(cockpitModule_templateServices.getDatasetUsetByWidgetNotAssociated(),function(dsLab){
			if(rt.isRealTime(dsLab)){
				var freq=rt.getDatasetFrequency(dsLab);
				if(freq && freq!=0){
					$interval(function(){
						debugger;
						$rootScope.$broadcast("WIDGET_EVENT","UPDATE_FROM_REALTIME",{dsList:[dsLab]});
					},(freq==undefined?60:freq)*1000)
				}
			}
			
		});
		
	};
})