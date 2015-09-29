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
		if(triggerName!=undefined && triggerName!="" && triggerGroup!=undefined && triggerGroup!=""){
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
				isSuspended:  false,
				document:[],
				chronstring:{"type":"single"}
				
				
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
					var d=data.item;
					activityEventCtrl.event.triggerName=d.triggerName;
					activityEventCtrl.event.triggerDescription=d.triggerDescription;
					activityEventCtrl.event.startDate=new Date(d.startDate);
					activityEventCtrl.event.startTime=d.startTime;
					if(d.endTime!=undefined && d.endTime!=""){
						activityEventCtrl.event.endTime=endTime;
					}else{
						activityEventCtrl.event.endTime=" "
					}
					if(d.endDate!=undefined && d.endDate!=""){
						activityEventCtrl.event.endDate=new Date(d.endDate);
					}
					
					var op=d.chronString.split("{")[0];
					
					switch(op){
						case 'single':activityEventCtrl.typeOperation=op; activityEventCtrl.shedulerType=false;break;
						case 'event':activityEventCtrl.typeOperation=op; activityEventCtrl.shedulerType=false;break;
						default :activityEventCtrl.typeOperation="scheduler"; activityEventCtrl.shedulerType=true; break;
						}
					
//					activityEventCtrl.event.=d.;
//					activityEventCtrl.event.=d.;
//					activityEventCtrl.event.=d.;
//					activityEventCtrl.event.=d.;
//					activityEventCtrl.event.=d.;
//					activityEventCtrl.event.=d.;
//					
					
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
	activityEventCtrl.eventSched={};
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
		restServices.post("scheduler", "saveTrigger", activityEventCtrl.event)
				.success(function(data) {
					if (data.hasOwnProperty("errors")) {
						console.error(data.errors[0].message);
						console.error(translate.load("sbi.glossary.error.save"));
					} else if (data.Status == "NON OK") {
						console.error(translate.load(data.Message));
					} else {
						$mdToast.show($mdToast.simple().content("SALVATO").position('top').action(
							'OK').highlightAction(false).hideDelay(3000));
							}
					})
			.error(function(data, status,headers, config) {
						console.error(translate.load("sbi.glossary.error.save"));
					});
		  
	}
	
	activityEventCtrl.changeTypeOperation=function(){
		var tip=activityEventCtrl.typeOperation;
		switch(tip){
			case 'single':activityEventCtrl.eventSched.repetitionKind='single';activityEventCtrl.shedulerType=false;break;
			case 'scheduler':activityEventCtrl.shedulerType=true; break;
			case 'event':activityEventCtrl.eventSched.repetitionKind='event'; activityEventCtrl.shedulerType=false;break;
		}
		
		activityEventCtrl.changeTypeFrequency();
	}
	
	activityEventCtrl.getActivityRepetitionKindForScheduler= function(){
		if(activityEventCtrl.eventSched.repetitionKind==undefined || activityEventCtrl.eventSched.repetitionKind=='single' || activityEventCtrl.eventSched.repetitionKind=='event' ){
			activityEventCtrl.eventSched.repetitionKind='minute';
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
		 activityEventCtrl.event.chronstring={"type":"month","parameter":{}};
		 if(activityEventCtrl.typeMonth==true){
				activityEventCtrl.event.chronstring.parameter.numRepetition=activityEventCtrl.monthrep_n;
				}else{
				activityEventCtrl.event.chronstring.parameter.months=[];
				for(var k in activityEventCtrl.month_repetition){
					activityEventCtrl.event.chronstring.parameter.months.push(activityEventCtrl.month_repetition[k]);
				}
			}
			
			if(activityEventCtrl.typeMonthWeek==true){
				activityEventCtrl.event.chronstring.parameter.dayRepetition=activityEventCtrl.dayinmonthrep_week;
			}else{
				var mwnr=activityEventCtrl.month_week_number_repetition
				if(mwnr==undefined)mwnr='first';
				activityEventCtrl.event.chronstring.parameter.dayRepetition=0;
				activityEventCtrl.event.chronstring.parameter.weeks=mwnr;
				activityEventCtrl.event.chronstring.parameter.days=[];
				for(var k in activityEventCtrl.month_week_repetition){
					activityEventCtrl.event.chronstring.parameter.days.push(activityEventCtrl.month_week_repetition[k]);
				}
				
			}
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
	
        activityEventCtrl.event.chronstring={"type":"week","parameter":{"numRepetition":1,"days":[]}};
        
        for(var k in activityEventCtrl.selectedWeek ){
        	activityEventCtrl.event.chronstring.parameter.days.push(activityEventCtrl.selectedWeek[k].value);
        }
    
	}
	
	activityEventCtrl.changeTypeFrequency=function(){
		$timeout(function() {
			var tip=activityEventCtrl.eventSched.repetitionKind;
			switch(tip){
			case 'event': activityEventCtrl.event.chronstring={"type":"event","parameter":{"type":activityEventCtrl.eventSched.event_type}};
							if(activityEventCtrl.eventSched.event_type=='dataset'){
								 activityEventCtrl.event.chronstring.parameter.dataset=activityEventCtrl.eventSched.dataset;
								 activityEventCtrl.event.chronstring.parameter.frequency=activityEventCtrl.eventSched.frequency;
							}
							break;
			case 'single':activityEventCtrl.event.chronstring={"type":"single"};break;
			case'minute': activityEventCtrl.event.chronstring={"type":"minute","parameter":{"numRepetition":activityEventCtrl.eventSched.minute_repetition_n}};break;
			case'hour': activityEventCtrl.event.chronstring={"type":"hour","parameter":{"numRepetition":activityEventCtrl.eventSched.hour_repetition_n}};break;
			case'day': activityEventCtrl.event.chronstring={"type":"day","parameter":{"numRepetition":activityEventCtrl.eventSched.day_repetition_n}};break;
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