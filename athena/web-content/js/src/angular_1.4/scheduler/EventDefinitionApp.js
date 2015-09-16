var loadEventsByActivity = function(jobDataObj) {
	var eventTypes = ['rest', 'jms', 'contextbroker', 'dataset'];
	
	var events = [];
	
	for (var i = 0; i < 4; i++) {
		var event = {
			id: i,
			name: 'name_' + i,
			description: 'description_' + i,
			type: eventTypes[i%4],
			isSuspended: (i % 2)? true : false,
			dataset: 'dataset_' + i,
			frequency: 'frequency_' + i
		}
		
		events.push(event);
	}
	
	return events;
}

var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial','angular_rest']);

eventDefinitionApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey')
		.accentPalette('blue-grey');
});
	
eventDefinitionApp.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

eventDefinitionApp.controller('LoadJobDataController', ['translate', '$scope','restServices' ,function(translate, $scope,restServices) {
	var loadJobDataCtrl = this;
	
	loadJobDataCtrl.jobName = '';
	loadJobDataCtrl.jobGroup = '';
	loadJobDataCtrl.jobDescription = '';
	
	loadJobDataCtrl.events = [];
	loadJobDataCtrl.selectedEvent = -1;
	loadJobDataCtrl.dataset=[];
	
	$scope.translate = translate;
	
	loadJobDataCtrl.initJobsValues= function(jobName, jobGroup, jobDescription) {
		loadJobDataCtrl.jobName = jobName;
		loadJobDataCtrl.jobGroup = jobGroup;
		loadJobDataCtrl.jobDescription = jobDescription;
		
		loadJobDataCtrl.events = loadEventsByActivity({
			jobName: loadJobDataCtrl.jobName,
			jobGroup: loadJobDataCtrl.jobGroup,
			jobDescription: loadJobDataCtrl.jobDescription
		});

		loadJobDataCtrl.loadDataset();
		
	}
	
	this.selectEvent = function(id) {
		
	}
	
	loadJobDataCtrl.loadDataset=function(){
		restServices.get("2.0/datasets", "listDataset")
		.success(function(data, status, headers, config) {
					console.log(data)
					if (data.hasOwnProperty("errors")) {
						console.error(translate.load("sbi.glossary.load.error"))
					} else {
						console.log("list Dataset ottenute")
						loadJobDataCtrl.dataset=data.item;
					}

				})
		.error(function(data, status, headers, config) {
					console.error(translate.load("sbi.glossary.load.error"))
				})
	}
	
}]);

eventDefinitionApp.controller('ActivityEventController', ['translate', '$scope', function(translate, $scope) {
	var activityEventCtrl = this;
	
	activityEventCtrl.id = '';
	activityEventCtrl.name = '';
	activityEventCtrl.description = '';
	activityEventCtrl.type = '';
	activityEventCtrl.isSuspended = false;
	activityEventCtrl.dataset = '';
	activityEventCtrl.frequency = '';
	
	
	activityEventCtrl.setEvent = function(eventObj) {
		activityEventCtrl.id = eventObj.id;
		activityEventCtrl.name = eventObj.name;
		activityEventCtrl.description = eventObj.description;
		activityEventCtrl.type = eventObj.type;
		activityEventCtrl.isSuspended = eventObj.isSuspended;
		activityEventCtrl.dataset = eventObj.dataset;
		activityEventCtrl.frequency = eventObj.frequency;
	}
	
	activityEventCtrl.selectFirstEvent = function(eventsArray) {
		if(eventsArray.length > 0) {
			activityEventCtrl.setEvent(eventsArray[0]);
		}
	}
}]);