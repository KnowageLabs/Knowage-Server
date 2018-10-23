angular.module("cockpitModule").service("cockpitModule_nearRealtimeServices",function(cockpitModule_template,$interval,$rootScope,cockpitModule_templateServices){
	var nrt=this;

	this.isNearRealTime=function(dsLabel,tmpList){
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

	this.getNearRealTimeDatasetFromList=function(dsList,tmpList){
		var nrtList=[];
		angular.forEach(dsList,function(ds){
			if(nrt.isNearRealTime(ds,tmpList)){
				this.push(ds);
			}
		},nrtList)
		return nrtList
	}

	this.init=function(){

		angular.forEach(cockpitModule_template.configuration.aggregations,function(aggr){
			if(nrt.getNearRealTimeDatasetFromList(aggr.datasets).length>0){
				var freq = aggr.frequency;
				if(freq != undefined && freq > 0){
					$interval(function(){
						$rootScope.$broadcast("WIDGET_EVENT","UPDATE_FROM_NEAR_REALTIME",{dsList:aggr.datasets});
					},freq*1000);
				}
			}
		});

		angular.forEach(cockpitModule_templateServices.getDatasetUsetByWidgetNotAssociated(),function(dsLab){
			if(nrt.isNearRealTime(dsLab)){
				var freq = nrt.getDatasetFrequency(dsLab);
				if(freq != undefined && freq > 0){
					$interval(function(){
						$rootScope.$broadcast("WIDGET_EVENT","UPDATE_FROM_NEAR_REALTIME",{dsList:[dsLab]});
					},freq*1000);
				}
			}

		});

	};
})