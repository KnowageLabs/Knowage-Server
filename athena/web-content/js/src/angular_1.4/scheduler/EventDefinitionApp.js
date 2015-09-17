/** Mock data load function */
var loadEventsByActivity = function(jobDataObj) {
	var eventTypes = ['rest', 'jms', 'contextbroker', 'dataset'];
	
	var events = [];
	
	for (var i = 0; i < 40; i++) {
		var event = {
			id: i,
			name: 'event_' + i,
			description: 'description_' + i,
			type: eventTypes[i%4],
			isSuspended: (i % 2)? true : false,
			dataset: 'dataset_' + i,
			frequency: ((i + 1) * 5)
		}
		
		events.push(event);
	}
	
	return events;
}

var getEmptyEvent=function(i){
	
	var emptyEvent = {
			id: -1,
			name: '',
			description: '',
			type: '',
			isSuspended:  false,
			dataset: '',
			frequency: 0
		}
	return emptyEvent;
}

var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial','angular_rest','angular_list']);
eventDefinitionApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey')
		.accentPalette('blue-grey');
});
	
eventDefinitionApp.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

//this variable are global because i need to access at variable of one controller from another controller
var activityEventCtrl;
var loadJobDataCtrl ;

eventDefinitionApp.controller('LoadJobDataController', ['translate', '$scope','restServices' ,function(translate, $scope,restServices) {
	 loadJobDataCtrl = this;

	loadJobDataCtrl.jobName = '';
	loadJobDataCtrl.jobGroup = '';
	loadJobDataCtrl.jobDescription = '';
	
	loadJobDataCtrl.events = [];
	loadJobDataCtrl.selectedEvent = -1;
	loadJobDataCtrl.datasets=[];
	loadJobDataCtrl.documents=[];
	loadJobDataCtrl.typeEvents=[];
	loadJobDataCtrl.typeEvents.push({value:'rest',label:translate.load("sbi.scheduler.activity.events.event.type.rest")});
	loadJobDataCtrl.typeEvents.push({value:'jms',label:translate.load("sbi.scheduler.activity.events.event.type.jms")});
	loadJobDataCtrl.typeEvents.push({value:'contextbroker',label:translate.load("sbi.scheduler.activity.events.event.type.contextbroker")});
	loadJobDataCtrl.typeEvents.push({value:'dataset',label:translate.load("sbi.scheduler.activity.events.event.type.dataset")});
	
	
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
		loadJobDataCtrl.loadDdocument();
	}
	
	loadJobDataCtrl.loadDdocument = function(){
		for(var i=0;i<10;i++){
			var ite={id:i,label:'document_'+i};
			loadJobDataCtrl.documents.push(ite);
		}
	}
	
	loadJobDataCtrl.loadDataset = function(){
		restServices.get("2.0/datasets", "listDataset")
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(translate.load("sbi.glossary.load.error"))
				} else {
					loadJobDataCtrl.datasets=data.item;
				}
			})
			.error(function(data, status, headers, config) {
				console.error(translate.load("sbi.glossary.load.error"))
			});
	}
	
}]);

	

eventDefinitionApp.controller('ActivityEventController', ['translate', '$scope','$mdDialog','$mdToast', function(translate, $scope,$mdDialog,$mdToast) {
	activityEventCtrl = this;
	
	activityEventCtrl.editedEvent=getEmptyEvent();
	
	activityEventCtrl.selectedEvent;
	
	activityEventCtrl.createNewEvent=function(noConfirm){
		//check if  there is a change in progress
		if(!activityEventCtrl.isEmptyNewEvent()  && noConfirm!=true){
			var confirm = $mdDialog
			.confirm()
			.title(translate.load("sbi.glossary.word.modify.progress"))
			.content(translate.load("sbi.glossary.word.modify.progress.message.showGloss"))
			.ariaLabel('Lucky day').ok(translate.load("sbi.general.continue")).cancel(translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(
				function() {
					activityEventCtrl.selectedEvent="";
					activityEventCtrl.editedEvent=getEmptyEvent();
					activityEventCtrl.editedEvent.newEvent=true;
				},
				function() {
					console.log('Annulla');
				});
		}else{
			activityEventCtrl.selectedEvent="";
			activityEventCtrl.editedEvent=getEmptyEvent();
			activityEventCtrl.editedEvent.newEvent=true;
		}
		
	}

	activityEventCtrl.setEvent = function(eventObj) {
		//check if  there is a change in progress
		if(!activityEventCtrl.isEmptyNewEvent()){
			var confirm = $mdDialog.confirm().title(translate.load("sbi.glossary.word.modify.progress"))
			.content(translate.load("sbi.glossary.word.modify.progress.message.showGloss"))
			.ariaLabel('Lucky day').ok(	translate.load("sbi.general.continue")).cancel(translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(
				function() {
					activityEventCtrl.editedEvent=eventObj;
				},
				function() {
				console.log('Annulla');
				});
		}else{
			activityEventCtrl.editedEvent=eventObj;
		}
		
	}
	
	activityEventCtrl.selectFirstEvent = function(eventsArray) {
		if(eventsArray.length > 0) {
			activityEventCtrl.setEvent(eventsArray[0]);
		}
	}
	
	activityEventCtrl.isEmptyNewEvent = function() {
		//compare the edited event with empty event template
		var nw= JSON.parse(JSON.stringify(activityEventCtrl.editedEvent ));
		if(nw.hasOwnProperty("newEvent")){
			delete nw.newEvent;
		}
		return (JSON.stringify(getEmptyEvent()) == JSON.stringify(nw));
	}
	
	activityEventCtrl.resetForm=function(){
		//check if  there is a change in progress
		if(!activityEventCtrl.isEmptyNewEvent()){
			var confirm = $mdDialog.confirm().title(translate.load("sbi.glossary.word.modify.progress"))
			.content(translate.load("sbi.glossary.word.modify.progress.message.showGloss"))
			.ariaLabel('Lucky day').ok(translate.load("sbi.general.continue")).cancel(translate.load("sbi.general.cancel"));
			$mdDialog.show(confirm).then(
				function() {
					activityEventCtrl.createNewEvent();
				},
				function() {
				console.log('Annulla');
				});
		}else{
			activityEventCtrl.createNewEvent();
		}
	}
	
	activityEventCtrl.saveEvent=function(isValid){
		 if (isValid) {
		   var SaveOrUpdate=activityEventCtrl.editedEvent.newEvent==true?'Saved':'Updated';
			 //TO-DO rest services
			 $mdToast.show($mdToast.simple().content(SaveOrUpdate).position('top').action(
				'OK').highlightAction(false).hideDelay(3000));
			 //if is new event, add it to list
			 if(activityEventCtrl.editedEvent.newEvent==true){
				 loadJobDataCtrl.events.push(activityEventCtrl.editedEvent);
			 }
			 activityEventCtrl.createNewEvent(true);
			
		    }else{
		    	return false;
		    }
	}
	

	
	activityEventCtrl.deleteEvent=function(item,ev){
		var confirm = $mdDialog.confirm().title(
				translate.load("sbi.glossary.word.delete")).content(
				translate.load("sbi.glossary.word.delete.message")).ariaLabel(
				'Lucky day').ok(translate.load("sbi.generic.delete")).cancel(
				translate.load("sbi.myanalysis.delete.cancel")).targetEvent(ev);

		$mdDialog
				.show(confirm)
				.then(
						function() {
							console.log("canciello");
							var index = loadJobDataCtrl.events.indexOf(item);
							loadJobDataCtrl.events.splice(index, 1);
						}, function() {
							console.log('annulla');
						});
		}
	
		activityEventCtrl.eventItemOpt = [ 
	 		               	{
	 		               		label : translate.load('sbi.generic.delete'),
	 		               		icon:"fa fa-times",
	 		               		backgroundColor:'transparent',
	 		               		color:"black",
	 		               		action : function(item,event) {
	 		               		activityEventCtrl.deleteEvent(item,event)
	 		               			}
	 		               	} 
	 		             
	 		             ];
	
}]);