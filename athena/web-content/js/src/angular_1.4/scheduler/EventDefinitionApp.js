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

var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial']);

eventDefinitionApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey')
		.accentPalette('blue-grey');
});
	
eventDefinitionApp.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

eventDefinitionApp.controller('LoadJobDataController', ['translate', '$scope', function(translate, $scope) {
	var loadJobDataCtrl = this;
	
	loadJobDataCtrl.jobName = '';
	loadJobDataCtrl.jobGroup = '';
	loadJobDataCtrl.jobDescription = '';
	
	loadJobDataCtrl.events = [];
	loadJobDataCtrl.selectedEvent = -1;
	
	$scope.translate = translate;
	
	loadJobDataCtrl.setJobValues = function(jobName, jobGroup, jobDescription) {
		loadJobDataCtrl.jobName = jobName;
		loadJobDataCtrl.jobGroup = jobGroup;
		loadJobDataCtrl.jobDescription = jobDescription;
		
		loadJobDataCtrl.events = loadEventsByActivity({
			jobName: loadJobDataCtrl.jobName,
			jobGroup: loadJobDataCtrl.jobGroup,
			jobDescription: loadJobDataCtrl.jobDescription
		});
	}
	
	this.selectEvent = function(id) {
		
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