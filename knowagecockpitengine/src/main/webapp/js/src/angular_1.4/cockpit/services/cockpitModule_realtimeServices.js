/**
 * This service links datasets CometD notifications from server to frontend.
 * It permits to subscribe to server notifications and then update data models.
 *
 */
angular.module("cockpitModule").service("cockpitModule_realtimeServices",function($rootScope, sbiModule_user, sbiModule_util, sbiModule_restServices, sbiModule_config, cockpitModule_template, cockpitModule_datasetServices, sbiModule_messaging){

	broadcast = function(message, dsLabel){
		if(this.oldDataMap && this.oldDataMap[message.channel]){
			if(this.oldDataMap[message.channel] != message.data){
				this.oldDataMap[message.channel] = message.data;
			}else{
				return;
			}
		}else{
			if(!this.oldDataMap){
				this.oldDataMap = {};
			}
			this.oldDataMap[message.channel] = message.data;
		}

		var event = "UPDATE_FROM_REALTIME";
		var data=JSON.parse(message.data);
		if(data.isFoundInCache) {
			console.log("Broadcasting a WIDGET_EVENT named " + event + " for dataset " + dsLabel)
			$rootScope.$broadcast("WIDGET_EVENT", event, {dsLabel:dsLabel, data:data.dataStore});
		} else {
			console.log("Error while processing data from Context Broker. Data cannot be updated due to missing of previous data.");
			console.log("You can re-execute this dashboard to get updates again");
			sbiModule_messaging.showWarningMessage(sbiModule_translate.load("sbi.cockpit.storeManager.noDatasetInCache"));
		}
	};

	this.init = function(){
		console.log("Initializing realtime datasets subscriptions");
		for(var i=0;i<cockpitModule_template.configuration.datasets.length;i++){
			var label = cockpitModule_template.configuration.datasets[i].dsLabel;
			console.log("Getting metadata for dataset " + label);

			var ds = cockpitModule_datasetServices.getDatasetByLabel(label);
			console.log(ds);
		}
	};
})