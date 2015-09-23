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

eventDefinitionApp.controller('LoadJobDataController', ['translate', '$scope','restServices' ,function(translate, $scope, restServices) {
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
		loadJobDataCtrl.jobData = null;
//		loadJobDataCtrl.loadEvent();
		loadJobDataCtrl.loadDataset();
		loadJobDataCtrl.loadJobData();
	}
	
	loadJobDataCtrl.loadDocuments = function(){
		var docs = loadJobDataCtrl.jobData.documents;
		for(var i = 0; i < docs.length; i++){
			var doc = {
//				id: docs[i].,
				label: docs[i].name,
				parameters: docs[i].condensedParameters
			};
			
			loadJobDataCtrl.documents.push(doc);
			activityEventCtrl.createNewEvent(true);
		}
	}
	
	loadJobDataCtrl.getEmptyEvent=function(){
		var emptyEvent = {
				id: -1,
				name: '',
				description: '',
				type: '',
				isSuspended:  false,
				dataset: '',
				frequency: 0,
				document:[]
			}
		
		//load document;
		for (var i=0;i<loadJobDataCtrl.documents.length;i++){
			var tmp={};
			var doc=loadJobDataCtrl.documents[i];
			tmp.label=doc.label;
			tmp.parameters=doc.parameters;
//			tmp.saveassnapshot=false;
//			tmp.snapshotname="";
//			tmp.snapshotdescription="";
//			tmp.snapshothistorylength="";
//			tmp.saveasfile=false;
//			tmp.destinationfolder="";
//			tmp.zipFileDocument=false;
//			tmp.zipFileName="";
//			tmp.fileName="";
//			tmp.documentname="";
//			tmp.documentdescription="";
//			tmp.useFixedFolder=false;
//			tmp.useFolderDataset=false;
//			tmp.datasetFolderLabel="";
//			tmp.datasetFolderParameter="";
//			tmp.sendtojavaclass=false;
//			tmp.javaclasspath="";
//			tmp.sendmail=false;
//			tmp.uniqueMail=false;
//			tmp.zipMailDocument=false;
//			tmp.zipMailName="";
//			tmp.useFixedRecipients=false;
//			tmp.mailtos="";
//			tmp.useDataset=false;
//			tmp.datasetLabel="";
//			tmp.datasetParameter="";
//			tmp.useExpression=false;
//			tmp.expression="";
//			tmp.reportNameInSubject=false;
//			tmp.mailsubj="";
//			tmp.containedFileName="";
//			tmp.mailtxt="";
//			tmp.saveasdl=false;
			emptyEvent.document.push(tmp);
		}
		
		return emptyEvent;
	}
	
	
	
	loadJobDataCtrl.loadDataset = function(){
		restServices.get("2.0/datasets", "listDataset")
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(translate.load("sbi.glossary.load.error"))
				} else {
					loadJobDataCtrl.datasets = data.item;
				}
			})
			.error(function(data, status, headers, config) {
				console.error(translate.load("sbi.glossary.load.error"))
			});
	}
	
//	loadJobDataCtrl.loadEvent = function(){
//		restServices.get("1.0/eventJob", "listEvent")
//			.success(function(data, status, headers, config) {
//				if (data.hasOwnProperty("errors")) {
//					console.error(translate.load("sbi.glossary.load.error"))
//				} else {
//					loadJobDataCtrl.events = data.item;
//				}
//			})
//			.error(function(data, status, headers, config) {
//				console.error(translate.load("sbi.glossary.load.error"))
//			});
//	}
	
	loadJobDataCtrl.loadJobData = function(){
		var parameters = 'jobName=' + loadJobDataCtrl.jobName + '&jobGroup=' + loadJobDataCtrl.jobGroup;
		restServices.get("scheduler", "getJob", parameters)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(translate.load("sbi.glossary.load.error"))
				} else {
					loadJobDataCtrl.jobData = data;
					loadJobDataCtrl.loadDocuments();
				}
			})
			.error(function(data, status, headers, config) {
				console.error(translate.load("sbi.glossary.load.error"))
			});
	}
	
}]);

	

eventDefinitionApp.controller('ActivityEventController', ['translate', '$scope','$mdDialog','$mdToast','restServices', function(translate, $scope,$mdDialog,$mdToast,restServices) {
	activityEventCtrl = this;
activityEventCtrl.event={};
	activityEventCtrl.selectedDocument=[];

	
	activityEventCtrl.createNewEvent=function(noConfirm){
		activityEventCtrl.event=loadJobDataCtrl.getEmptyEvent();
		activityEventCtrl.setSelectedDocument();
		}
	
	activityEventCtrl.setSelectedDocument=function(){
		activityEventCtrl.selectedDocument=(activityEventCtrl.event.document==undefined || activityEventCtrl.event.document.length!=0)  ? activityEventCtrl.event.document[0]:[];
		}
//
//	activityEventCtrl.setEvent = function(eventObj) {
//		//check if  there is a change in progress
//		if(!activityEventCtrl.isEmptyNewEvent()){
//			var confirm = $mdDialog.confirm().title(translate.load("sbi.glossary.word.modify.progress"))
//			.content(translate.load("sbi.glossary.word.modify.progress.message.showGloss"))
//			.ariaLabel('Lucky day').ok(	translate.load("sbi.general.continue")).cancel(translate.load("sbi.general.cancel"));
//			$mdDialog.show(confirm).then(
//				function() {
//					activityEventCtrl.editedEvent=eventObj;
//					activityEventCtrl.setSelectedDocument();
//				},
//				function() {
//				console.log('Annulla');
//				});
//		}else{
//			activityEventCtrl.editedEvent=eventObj;
//			activityEventCtrl.setSelectedDocument();
//			}
//		
//	}
//	
//	activityEventCtrl.selectFirstEvent = function(eventsArray) {
//		if(eventsArray.length > 0) {
//			activityEventCtrl.setEvent(eventsArray[0]);
//		}
//	}
//	
//	activityEventCtrl.isEmptyNewEvent = function() {
//		//compare the edited event with empty event template
//		var nw= JSON.parse(JSON.stringify(activityEventCtrl.editedEvent ));
//		if(nw.hasOwnProperty("newEvent")){
//			delete nw.newEvent;
//		}
//		if(nw.hasOwnProperty("document")){
//			for(var i=0;i<nw.document.length;i++){
//				if(nw.document[i].hasOwnProperty("$$hashKey")){
//					delete nw.document[i].$$hashKey
//				}
//			}
//		}
//		
//		return (JSON.stringify(loadJobDataCtrl.getEmptyEvent()) == JSON.stringify(nw));
//	}
	
	activityEventCtrl.resetForm=function(){
		//check if  there is a change in progress
	
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
	
	}
	
	activityEventCtrl.saveEvent=function(isValid){
		if (!isValid) {return false;}
		return;
			var SaveOrUpdate=activityEventCtrl.editedEvent.newEvent==true?'Saved':'Updated';
			 //TO-DO rest services
			restServices.post("1.0/eventJob","addEvent", activityEventCtrl.editedEvent)
				.success(function(data) {
					if (data.hasOwnProperty("errors")) {
						console.error(data.errors[0].message);
						console.error(translate.load("sbi.glossary.error.save"));
					} else if (data.Status == "NON OK") {
						console.error(translate.load(data.Message));
					} else {
						$mdToast.show($mdToast.simple().content(SaveOrUpdate).position('top').action(
							'OK').highlightAction(false).hideDelay(3000));
						 //if is new event, add it to list
						if(activityEventCtrl.editedEvent.newEvent==true){
							activityEventCtrl.editedEvent.id=data.ID;
							 loadJobDataCtrl.events.push(activityEventCtrl.editedEvent);
						 }
						activityEventCtrl.createNewEvent(true);
						}
					})
			.error(function(data, status,headers, config) {
						console.error(translate.load("sbi.glossary.error.save"));
					});
		  
	}
	


//	
//	activityEventCtrl.deleteEvent=function(item,ev){
//		var confirm = $mdDialog.confirm().title(
//				translate.load("sbi.glossary.word.delete")).content(
//				translate.load("sbi.glossary.word.delete.message")).ariaLabel(
//				'Lucky day').ok(translate.load("sbi.generic.delete")).cancel(
//				translate.load("sbi.myanalysis.delete.cancel")).targetEvent(ev);
//
//		$mdDialog
//				.show(confirm)
//				.then(
//						function() {
//							console.log("canciello");
//							
//							
//							restServices.post("1.0/eventJob","deleteEvent", {"event_id":item.event_id})
//							.success(function(data) {
//								if (data.hasOwnProperty("errors")) {
//									console.error(data.errors[0].message);
//									console.error(translate.load("sbi.glossary.error.save"));
//								} else if (data.Status == "NON OK") {
//									console.error(translate.load(data.Message));
//								} else {
//									var index = loadJobDataCtrl.events.indexOf(item);
//									loadJobDataCtrl.events.splice(index, 1);
//								}
//								})
//						.error(function(data, status,headers, config) {
//									console.error(translate.load("sbi.glossary.error.save"));
//								});
//							
//							
//						}, function() {
//							console.log('annulla');
//						});
//		}
//	
//		activityEventCtrl.eventItemOpt = [ 
//	 		               	{
//	 		               		label : translate.load('sbi.generic.delete'),
//	 		               		icon:"fa fa-times",
//	 		               		backgroundColor:'transparent',
//	 		               		color:"black",
//	 		               		action : function(item,event) {
//	 		               		activityEventCtrl.deleteEvent(item,event)
//	 		               			}
//	 		               	} 
//	 		             
//	 		             ];
		
	
	
}]);