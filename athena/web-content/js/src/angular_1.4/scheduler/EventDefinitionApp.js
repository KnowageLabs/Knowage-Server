var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial','angular_rest','angular_list','angular_time_picker']);

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
	loadJobDataCtrl.triggerName = '';
	loadJobDataCtrl.triggerGroup = '';

	
	loadJobDataCtrl.events = [];
	loadJobDataCtrl.selectedEvent = -1;
	loadJobDataCtrl.datasets=[];
	loadJobDataCtrl.documents=[];
	loadJobDataCtrl.typeEvents=[];
	loadJobDataCtrl.typeEvents.push({value:'rest',label:translate.load("sbi.scheduler.schedulation.events.event.type.rest")});
	loadJobDataCtrl.typeEvents.push({value:'jms',label:translate.load("sbi.scheduler.schedulation.events.event.type.jms")});
	loadJobDataCtrl.typeEvents.push({value:'contextbroker',label:translate.load("sbi.scheduler.schedulation.events.event.type.contextbroker")});
	loadJobDataCtrl.typeEvents.push({value:'dataset',label:translate.load("sbi.scheduler.schedulation.events.event.type.dataset")});
	
	loadJobDataCtrl.intervalsEvent=[{value:'minute',label:"Minute"},
	                                {value:'hour',label:"Hour"},
	                                {value:'day',label:"Daily"},
	                                {value:'week',label:"Weekly"},
	                                {value:'month',label:"Monthly"}
	                                ];
	
	loadJobDataCtrl.eventTipology=[{value:'single',label:"Single Execution"},
	                               {value:'scheduler',label:"Scheduler Execution"},
	                               {value:'event',label:"Event Execution"},]
	
	$scope.translate = translate;
	

	
	loadJobDataCtrl.initJobsValues= function(jobName, jobGroup,triggerName, triggerGroup) {
		loadJobDataCtrl.jobName = jobName;
		loadJobDataCtrl.jobGroup = jobGroup;
		loadJobDataCtrl.triggerGroup = triggerGroup;
		loadJobDataCtrl.triggerName = triggerName;
		loadJobDataCtrl.jobData = null;
		var loadtri=false;
		if(triggerName!=undefined && triggerGroup!=undefined){
			loadtri=true;
		}
		loadJobDataCtrl.loadDataset();
		loadJobDataCtrl.loadJobData(loadtri);
	}
	
	loadJobDataCtrl.loadDocuments = function(loadTri){
		var docs = loadJobDataCtrl.jobData.documents;
		for(var i = 0; i < docs.length; i++){
			var doc = {
//				id: docs[i].,
				label: docs[i].name,
				parameters: docs[i].condensedParameters
			};
			
			loadJobDataCtrl.documents.push(doc);
		}
		activityEventCtrl.createNewEvent(loadTri);
	}
	
	loadJobDataCtrl.getEmptyEvent=function(){
		var emptyEvent = {
				id: -1,
				name: '',
				description: '',
				isSuspended:  false,
				document:[],
				chronstring:"single{}"
				
				
			}
		activityEventCtrl.typeOperation='single'
		
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
	
	loadJobDataCtrl.loadScheduler = function(){
		restServices.get("scheduler", "getTriggerInfo?jobName="+loadJobDataCtrl.jobName+"&jobGroup="+loadJobDataCtrl.jobGroup+"&triggerGroup="+loadJobDataCtrl.triggerGroup+"&triggerName="+loadJobDataCtrl.triggerName)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(translate.load("sbi.glossary.load.error"))
				} else {
//					loadJobDataCtrl.events = data.item;
					
					console.log("evento caricato",data)
				}
			})
			.error(function(data, status, headers, config) {
				console.error(translate.load("sbi.glossary.load.error"))
			});
	}
	
	loadJobDataCtrl.loadJobData = function(loadTri){
		var parameters = 'jobName=' + loadJobDataCtrl.jobName + '&jobGroup=' + loadJobDataCtrl.jobGroup;
		restServices.get("scheduler", "getJob", parameters)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(translate.load("sbi.glossary.load.error"))
				} else {
					loadJobDataCtrl.jobData = data;
					loadJobDataCtrl.loadDocuments(loadTri);
				}
			})
			.error(function(data, status, headers, config) {
				console.error(translate.load("sbi.glossary.load.error"))
			});
	}
	
}]);

	

eventDefinitionApp.controller('ActivityEventController', ['translate', '$scope','$mdDialog','$mdToast','restServices','$timeout', function(translate, $scope,$mdDialog,$mdToast,restServices,$timeout) {
	activityEventCtrl = this;
	activityEventCtrl.event={};
	activityEventCtrl.selectedDocument=[];
	activityEventCtrl.month=[{label:'JAN',value:'JAN'},{label:'FEB',value:'FEB'},{label:'MAR',value:'MAR'},{label:'APR',value:'APR'},{label:'MAY',value:'MAY'},{label:'JUN',value:'JUN'},{label:'JUL',value:'JUL'},{label:'AUG',value:'AUG'},{label:'SEP',value:'SEP'},{label:'OCT',value:'OCT'},{label:'NOV',value:'NOV'},{label:'DIC',value:'DIC'}];
	activityEventCtrl.week=[{label:'sun',value:'SUN'},{label:'mon',value:'MON'},{label:'tue',value:'TUE'},{label:'wed',value:'WED'},{label:'thu',value:'THU'},{label:'fri',value:'FRI'},{label:'sat',value:'SAT'}];
	activityEventCtrl.selectedWeek=[];
	activityEventCtrl.selectedWeekObj={};
	
	activityEventCtrl.createNewEvent=function(loadTrigger){
		activityEventCtrl.event=loadJobDataCtrl.getEmptyEvent();
		activityEventCtrl.setSelectedDocument();
		
		if(loadTrigger){
			loadJobDataCtrl.loadScheduler();
		}
	}
	
	activityEventCtrl.setSelectedDocument=function(){
		activityEventCtrl.selectedDocument=(activityEventCtrl.event.document==undefined || activityEventCtrl.event.document.length!=0)  ? activityEventCtrl.event.document[0]:[];
		}
	
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
						activityEventCtrl.createNewEvent();
						}
					})
			.error(function(data, status,headers, config) {
						console.error(translate.load("sbi.glossary.error.save"));
					});
		  
	}
	
	activityEventCtrl.changeTypeOperation=function(){
	
		var tip=activityEventCtrl.typeOperation;
		switch(tip){
			case 'single':activityEventCtrl.event.repetitionKind='single';activityEventCtrl.shedulerType=false;break;
			case 'scheduler':activityEventCtrl.shedulerType=true; break;
			case 'event':activityEventCtrl.event.repetitionKind='event'; activityEventCtrl.shedulerType=false;break;
		}
		
		activityEventCtrl.changeTypeFrequency();
	}
	
	activityEventCtrl.getActivityRepetitionKindForScheduler= function(){
		if(activityEventCtrl.event.repetitionKind==undefined || activityEventCtrl.event.repetitionKind=='single' || activityEventCtrl.event.repetitionKind=='event' ){
			activityEventCtrl.event.repetitionKind='minute';
		}
	}
	
	activityEventCtrl.getNitem=function(n){
		var r =[];
		for(var i=1;i<=n;i++){
			r.push(i);
		}
		return r;
	}
	
	activityEventCtrl.toggleMonthScheduler=function(){
		console.log("sched")
		
			var res="month{";
			
			if(activityEventCtrl.typeMonth==true){
				res+="numRepetition="+activityEventCtrl.monthrep_n+";months=NONE;";
				activityEventCtrl.event.month_selection='off';
			}else{
				res+="numRepetition=0;months=";
				activityEventCtrl.event.month_selection='on';
				
				if(activityEventCtrl.month_repetition==undefined || activityEventCtrl.month_repetition.length==0)res+="NONE";
				for(var k in activityEventCtrl.month_repetition){
					res+=activityEventCtrl.month_repetition[k]+","
					
				}
				res+=";"
				
			}
			
			if(activityEventCtrl.typeMonthWeek==true){
				res+="dayRepetition="+activityEventCtrl.dayinmonthrep_week+";";
				activityEventCtrl.dayinmonth_selection='on';
			}else{
				var mwnr=activityEventCtrl.month_week_number_repetition
				if(mwnr==undefined)mwnr='first';
				
				res+="dayRepetition=0;weeks="+mwnr+";days=";
				activityEventCtrl.dayinmonth_selection='off';
				
				if( activityEventCtrl.month_week_repetition==undefined || activityEventCtrl.month_week_repetition.length==0)res+="NONE";
				for(var k in activityEventCtrl.month_week_repetition){
					res+=activityEventCtrl.month_week_repetition[k]+","
					
				}
				res+=";"
				
			}
			
			
			res+="}"
			activityEventCtrl.event.chronstring=res;
			console.log('chronstring',res)
		
	}
	
	activityEventCtrl.toggleWeek=function(week){
		if(week!=undefined){
			var idx = activityEventCtrl.selectedWeek.indexOf(week);
	        if (idx > -1){
	        	activityEventCtrl.selectedWeek.splice(idx, 1);
	        }else{
	        	activityEventCtrl.selectedWeek.push(week);
	        }
		}
	    
        activityEventCtrl.event.chronstring="week{numRepetition=1;days=";
        
        for(var k in activityEventCtrl.selectedWeek ){
        	 activityEventCtrl.event.chronstring= activityEventCtrl.event.chronstring+""+activityEventCtrl.selectedWeek[k].value+",";
        }
        activityEventCtrl.event.chronstring= activityEventCtrl.event.chronstring+"}";
	}
	
	activityEventCtrl.changeTypeFrequency=function(){
		$timeout(function() {
			var tip=activityEventCtrl.event.repetitionKind;
			switch(tip){
			case 'event': activityEventCtrl.event.chronstring="event{}";break;
			case 'single':activityEventCtrl.event.chronstring="single{}";break;
			case'minute': activityEventCtrl.event.chronstring="minute{numRepetition="+activityEventCtrl.event.minute_repetition_n+"}";break;
			case'hour': activityEventCtrl.event.chronstring="hour{numRepetition="+activityEventCtrl.event.hour_repetition_n+"}";break;
			case'day': activityEventCtrl.event.chronstring="day{numRepetition="+activityEventCtrl.event.day_repetition_n+"}";break;
			case'week': activityEventCtrl.toggleWeek();break;
			case'month': activityEventCtrl.toggleMonthScheduler();break;
			}
			console.log('chronstring',activityEventCtrl.event.chronstring);
		}, 500);
		
		
	}
	
	activityEventCtrl.prova=function(){
		console.log("prova")
	}
	
	
}]);