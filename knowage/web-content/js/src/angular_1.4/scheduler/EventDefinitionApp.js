var eventDefinitionApp = angular.module('EventDefinitionApp', ['ngMaterial','sbiModule',  'angular_list', 'angular_time_picker','ngMessages']);

eventDefinitionApp.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette('blue-grey');
});
	
//this variable are global because i need to access at variable of one controller from another controller
var activityEventCtrl;

eventDefinitionApp.controller('ActivityEventController', 
		['sbiModule_translate', '$scope', '$mdDialog', '$mdToast', 'sbiModule_restServices', '$timeout', 
		 	function(sbiModule_translate, $scope, $mdDialog, $mdToast, sbiModule_restServices, $timeout) {
			
	activityEventCtrl = this;
	sbiModule_translate.addMessageFile("component_scheduler_messages");
	$scope.translate = sbiModule_translate;

	activityEventCtrl.SCHEDULER_TYPES = [
        {value: 'single', label: sbiModule_translate.load("scheduler.singleExec", "component_scheduler_messages")},
        {value: 'scheduler', label: sbiModule_translate.load("scheduler.schedulerExec", "component_scheduler_messages")},
        {value: 'event', label: sbiModule_translate.load("scheduler.eventExec", "component_scheduler_messages")}
    ];
	
	activityEventCtrl.EVENT_TYPES = [
        {value: 'rest', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.rest")},
        {value: 'jms', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.jms")},
	    {value: 'contextbroker', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.contextbroker")},
	    {value: 'dataset', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}
    ];
	
	activityEventCtrl.EVENT_INTERVALS = [
		{value: 'minute', label: sbiModule_translate.load("scheduler.minuteExec", "component_scheduler_messages")},
		{value: 'hour', label: sbiModule_translate.load("scheduler.hourExec", "component_scheduler_messages")},
		{value: 'day', label: sbiModule_translate.load("scheduler.dayExec", "component_scheduler_messages")},
		{value: 'week', label: sbiModule_translate.load("scheduler.weekExec", "component_scheduler_messages")},
		{value: 'month', label: sbiModule_translate.load("scheduler.monthExec", "component_scheduler_messages")}
	];
	
	activityEventCtrl.MONTHS = [
	    {label: sbiModule_translate.load("scheduler.jan", "component_scheduler_messages"), value: '1'},
	    {label: sbiModule_translate.load("scheduler.feb", "component_scheduler_messages"), value: '2'}, 
	    {label: sbiModule_translate.load("scheduler.mar", "component_scheduler_messages"), value: '3'}, 
	    {label: sbiModule_translate.load("scheduler.apr", "component_scheduler_messages"), value: '4'}, 
	    {label: sbiModule_translate.load("scheduler.may", "component_scheduler_messages"), value: '5'}, 
	    {label: sbiModule_translate.load("scheduler.jun", "component_scheduler_messages"), value: '6'}, 
	    {label: sbiModule_translate.load("scheduler.jul", "component_scheduler_messages"), value: '7'}, 
	    {label: sbiModule_translate.load("scheduler.aug", "component_scheduler_messages"), value: '8'}, 
	    {label: sbiModule_translate.load("scheduler.sep", "component_scheduler_messages"), value: '9'}, 
	    {label: sbiModule_translate.load("scheduler.oct", "component_scheduler_messages"), value: '10'}, 
	    {label: sbiModule_translate.load("scheduler.nov", "component_scheduler_messages"), value: '11'}, 
	    {label: sbiModule_translate.load("scheduler.dic", "component_scheduler_messages"), value: '12'}
    ];
	
	activityEventCtrl.WEEKS = [
        {label: sbiModule_translate.load("scheduler.sun", "component_scheduler_messages"), value: '1'}, 
        {label: sbiModule_translate.load("scheduler.mon", "component_scheduler_messages"), value: '2'}, 
        {label: sbiModule_translate.load("scheduler.tue", "component_scheduler_messages"), value: '3'}, 
        {label: sbiModule_translate.load("scheduler.wed", "component_scheduler_messages"), value: '4'}, 
        {label: sbiModule_translate.load("scheduler.thu", "component_scheduler_messages"), value: '5'}, 
        {label: sbiModule_translate.load("scheduler.fri", "component_scheduler_messages"), value: '6'}, 
        {label: sbiModule_translate.load("scheduler.sat", "component_scheduler_messages"), value: '7'}
    ];
	
	activityEventCtrl.WEEKS_ORDER = [
        {label: sbiModule_translate.load("scheduler.firstweek", "component_scheduler_messages"), value: '1'}, 
        {label: sbiModule_translate.load("scheduler.secondweek", "component_scheduler_messages"), value: '2'}, 
        {label: sbiModule_translate.load("scheduler.thirdweek", "component_scheduler_messages"), value: '3'}, 
        {label: sbiModule_translate.load("scheduler.fourthweek", "component_scheduler_messages"), value: '4'}, 
        {label: sbiModule_translate.load("scheduler.lastweek", "component_scheduler_messages"), value: '5'}, 
    ];
	
	activityEventCtrl.event = {};
	activityEventCtrl.disableName=false;
	activityEventCtrl.event.jobName = '';
	activityEventCtrl.event.jobGroup = '';
	activityEventCtrl.event.triggerName = '';
	activityEventCtrl.event.triggerGroup = '';
	activityEventCtrl.event.triggerDescription = '';
	activityEventCtrl.event.documents = [];

	activityEventCtrl.datasets = [];
	activityEventCtrl.JobDocuments = [];
	
	activityEventCtrl.eventSched = {};
	activityEventCtrl.selectedDocument = [];
	activityEventCtrl.selectedWeek = [];
	
	activityEventCtrl.initJobsValues = function(jobName, jobGroup, triggerName, triggerGroup) {
		activityEventCtrl.event.jobName = jobName;
		activityEventCtrl.event.jobGroup = jobGroup;
		activityEventCtrl.event.triggerName = triggerName;
		activityEventCtrl.event.triggerGroup = triggerGroup;
		activityEventCtrl.jobData = null;
		
		var loadtri = false;
		if(triggerName != undefined && triggerName != null && triggerName.trim() != "" 
			&& triggerGroup != undefined && triggerGroup != null && triggerGroup.trim() != "") {
			
			loadtri = true;
		}
		
		activityEventCtrl.loadDataset();
		activityEventCtrl.loadJobData(loadtri);
	};

	activityEventCtrl.loadDataset = function() {
		sbiModule_restServices.get("2.0/datasets", "listDataset")
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(sbiModule_translate.load("sbi.glossary.load.error"))
				} else {
					activityEventCtrl.datasets = data.item;
				}
			})
			.error(function(data, status, headers, config) {
				console.error(sbiModule_translate.load("sbi.glossary.load.error"))
			});
	};
	
	activityEventCtrl.loadJobData = function(loadTri) {
		var parameters = 'jobName=' + activityEventCtrl.event.jobName 
			+ '&jobGroup=' + activityEventCtrl.event.jobGroup;
		
		sbiModule_restServices.get("scheduler", "getJob", parameters)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(sbiModule_translate.load("sbi.glossary.load.error"))
				} else {
					console.log("data", data);
					
					activityEventCtrl.jobData = data.job;
					activityEventCtrl.lowFunc =data.functionality;
					activityEventCtrl.loadDocuments(loadTri);
				}
			})
			.error(function(data, status, headers, config) {
				console.error(sbiModule_translate.load("sbi.glossary.load.error"));
			});
	};
	
	activityEventCtrl.loadDocuments = function(loadTri) {
		var docs = activityEventCtrl.jobData.documents;
		for(var i = 0; i < docs.length; i++) {
			var doc = {
				labelId: docs[i].id + "__" + (i+1),
				id: docs[i].id,
				label: docs[i].name,
				parameters: docs[i].parameters
			};
			
			activityEventCtrl.JobDocuments.push(doc);
		}
		activityEventCtrl.createNewEvent(loadTri);
	};
	
	activityEventCtrl.getEmptyEvent = function() {
		var emptyEvent = {
			jobName: activityEventCtrl.event.jobName,
			jobGroup: activityEventCtrl.event.jobGroup,
			triggerName: activityEventCtrl.event.triggerName,
			triggerDescription: 
				(activityEventCtrl.event.triggerDescription  && activityEventCtrl.event.triggerDescription != null )? 
						activityEventCtrl.event.triggerDescription : '',
			triggerGroup: activityEventCtrl.event.triggerGroup,
			documents: [],
			chrono: {"type": "single"}
		};
		
		activityEventCtrl.typeOperation = 'single';
		
		//load document;
		for (var i = 0; i < activityEventCtrl.JobDocuments.length; i++) {
			var tmp = {};
			var doc = activityEventCtrl.JobDocuments[i];
			tmp.label = doc.label;
			tmp.parameters = doc.parameters;
			tmp.labelId = doc.labelId;
			tmp.id = doc.id;
			emptyEvent.documents.push(tmp);
		}
		
		return emptyEvent;
	};
	
	activityEventCtrl.loadScheduler = function() {
		var requestString = 
			"getTriggerInfo?jobName=" + activityEventCtrl.event.jobName
			+"&jobGroup=" + activityEventCtrl.event.jobGroup
			+"&triggerGroup=" + activityEventCtrl.event.triggerGroup
			+"&triggerName=" + activityEventCtrl.event.triggerName;
		
		sbiModule_restServices.get("scheduler", requestString	)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(sbiModule_translate.load("sbi.glossary.load.error"));
				} else {
					console.log("evento scaricato", data);
					activityEventCtrl.disableName=true;
					
					var d = data;
					activityEventCtrl.event.triggerName = d.triggerName;
					activityEventCtrl.event.triggerDescription = 
						(d.triggerDescription && d.triggerDescription != null) ? d.triggerDescription : "";
//					activityEventCtrl.event.startDate = new Date(d.startDate);
					activityEventCtrl.event.startDate = new Date(d.startDateRFC3339);
					activityEventCtrl.event.startTime = d.startTime;
					
					if(d.endTime != undefined && d.endTime != "") {
						activityEventCtrl.event.endTime = d.endTime;
					} else {
						activityEventCtrl.event.endTime = "";
					}
					
					if(d.endDate != undefined && d.endDate != "") {
//						activityEventCtrl.event.endDate = new Date(d.endDate);
						activityEventCtrl.event.endDate = new Date(d.endDateRFC3339);
					}
					
					activityEventCtrl.event.chrono = d.chrono;
					
					var op = d.chrono;
					activityEventCtrl.eventSched.repetitionKind = op.type;
					
					if(op.type == 'single') {
						activityEventCtrl.typeOperation = op.type;
						activityEventCtrl.shedulerType = false;
					} else if(op.type == 'event') {
						activityEventCtrl.typeOperation = op.type;
						activityEventCtrl.shedulerType = false;
						activityEventCtrl.eventSched.event_type = op.parameter.type;
						
						if(op.parameter.type == "dataset") {
							activityEventCtrl.eventSched.dataset = op.parameter.dataset;
							activityEventCtrl.eventSched.frequency = op.parameter.frequency;
						}
					} else {
						activityEventCtrl.typeOperation = "scheduler";
						activityEventCtrl.shedulerType = true;
						if(op.type == 'minute'){
							activityEventCtrl.eventSched.minute_repetition_n=op.parameter.numRepetition;
						} else if(op.type == 'hour'){
							activityEventCtrl.eventSched.hour_repetition_n=op.parameter.numRepetition;
						} else if(op.type == 'day'){
							activityEventCtrl.eventSched.day_repetition_n=op.parameter.numRepetition;
						} else if(op.type == 'week') {	
							activityEventCtrl.selectedWeek = op.parameter.days;
						} else if(op.type == 'month') {
							if(op.parameter.hasOwnProperty("months")) {
								activityEventCtrl.typeMonth = false;
								activityEventCtrl.month_repetition = op.parameter.months;
							} else {
								activityEventCtrl.typeMonth = true;
								activityEventCtrl.monthrep_n = op.parameter.numRepetition;
								activityEventCtrl.month_week_number_repetition = op.parameter.weeks;
								activityEventCtrl.month_week_repetition = op.parameter.days;
							}
							
							if(op.parameter.hasOwnProperty("days")) {
								activityEventCtrl.typeMonthWeek = false;
								activityEventCtrl.month_week_number_repetition = op.parameter.weeks;
								activityEventCtrl.month_week_repetition = op.parameter.days;
							} else {
								activityEventCtrl.typeMonthWeek = true;
								activityEventCtrl.dayinmonthrep_week = op.parameter.dayRepetition;
							}
						}
					}
					
					//carico le informazioni dei documenti
					activityEventCtrl.event.documents=d.documents;
				
					activityEventCtrl.selectedDocument = activityEventCtrl.event.documents[0];
				}
			})
			.error(function(data, status, headers, config) {
				console.error(sbiModule_translate.load("sbi.glossary.load.error"))
			});
	};

	
	
	activityEventCtrl.createNewEvent = function(loadTrigger) {
		activityEventCtrl.event = activityEventCtrl.getEmptyEvent();
		activityEventCtrl.setSelectedDocument();
		
		if(loadTrigger) {
			activityEventCtrl.loadScheduler();
		}
	};
	
	activityEventCtrl.setSelectedDocument = function() {
		activityEventCtrl.selectedDocument = 
			(activityEventCtrl.event.documents == undefined || activityEventCtrl.event.documents.length != 0)? 
					activityEventCtrl.event.documents[0] : [];
	};
	
	
	
	activityEventCtrl.triggerEvent = function() {
		var requestTriggerEvent = "eventName=" + activityEventCtrl.event.triggerName
		
		sbiModule_restServices.get("scheduler", "triggerEvent", requestTriggerEvent)
			.success(function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.error(sbiModule_translate.load("sbi.glossary.load.error"))
				} else {
					console.log("data", data);
				}
			})
			.error(function(data, status, headers, config) {
				console.error(sbiModule_translate.load("ERRORE triggerEvent"));
			});
		
	};
	
	activityEventCtrl.saveEvent = function(isValid,saveAndReturn) {
		if (!isValid) {
			return false;
		}
		var cloneData=JSON.parse(JSON.stringify(activityEventCtrl.event));
		if(cloneData.startDate!=undefined){
			cloneData.startDate=(new Date(cloneData.startDate)).getTime();
		}
		if(cloneData.endDate!=undefined){
			cloneData.endDate=(new Date(cloneData.endDate)).getTime();
		}
		
		sbiModule_restServices.post("scheduler", "saveTrigger", cloneData)
			.success(function(data) {
				if (data.hasOwnProperty("errors")) {
					console.error(data.errors[0].message);
					console.error(sbiModule_translate.load("sbi.glossary.error.save"));
				} else if (data.Status == "NON OK") {
					console.error("errori salvataggio",data.Errors);
					$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.glossary.error.save")+" "+data.Errors).position('top').action('OK').highlightAction(true));
				 } else {
					 activityEventCtrl.disableName=true;
					$mdToast.show($mdToast.simple().content("SALVATO").position('top').action('OK').highlightAction(false).hideDelay(3000));
					if(saveAndReturn){
						$timeout(function() {
							parent.angularWindow.close();
					    }, 3000);
						
					}
				}
			})
			.error(function(data, status, headers, config) {
				console.error(sbiModule_translate.load("sbi.glossary.error.save"));
				
				return false;
			});
		  
	};
	
	activityEventCtrl.changeTypeOperation = function() {
		var tip = activityEventCtrl.typeOperation;
		switch(tip) {
			case 'single': 
				activityEventCtrl.eventSched.repetitionKind = 'single'; 
				activityEventCtrl.shedulerType = false; 
				break;
			case 'scheduler': 
				activityEventCtrl.shedulerType = true; 
				break;
			case 'event': 
				activityEventCtrl.eventSched.repetitionKind = 'event'; 
				activityEventCtrl.shedulerType = false; 
				break;
		}
		
		activityEventCtrl.changeTypeFrequency();
	};
	
	activityEventCtrl.getActivityRepetitionKindForScheduler = function() {
		if(activityEventCtrl.eventSched.repetitionKind == undefined 
				|| activityEventCtrl.eventSched.repetitionKind == 'single' 
				|| activityEventCtrl.eventSched.repetitionKind == 'event' ) {
			
			activityEventCtrl.eventSched.repetitionKind = 'minute';
		}
	};
	
	activityEventCtrl.getNitem = function(n) {
		var r =[];
		for(var i = 1; i <= n; i++) {
			r.push(i);
		}
		return r;
	};
	
	activityEventCtrl.toggleMonthScheduler = function() {
		activityEventCtrl.event.chrono = {
			"type": "month", 
			"parameter": {}
		};
		 
		if(activityEventCtrl.typeMonth == true) {
			activityEventCtrl.event.chrono.parameter.numRepetition = activityEventCtrl.monthrep_n;
		} else {
			activityEventCtrl.event.chrono.parameter.months = [];
			for(var k in activityEventCtrl.month_repetition) {
				activityEventCtrl.event.chrono.parameter.months.push(activityEventCtrl.month_repetition[k]);
			}
		}
			
		if(activityEventCtrl.typeMonthWeek == true) {
			activityEventCtrl.event.chrono.parameter.dayRepetition = activityEventCtrl.dayinmonthrep_week;
		} else {
			var mwnr = activityEventCtrl.month_week_number_repetition;
			
			if(mwnr == undefined) {
				mwnr = 'first';
			}
			
			activityEventCtrl.event.chrono.parameter.weeks = mwnr;
			activityEventCtrl.event.chrono.parameter.days = [];
			
			for(var k in activityEventCtrl.month_week_repetition) {
				activityEventCtrl.event.chrono.parameter.days.push(activityEventCtrl.month_week_repetition[k]);
			}
		}
	};
	
	activityEventCtrl.toggleWeek = function(week) {
		if(week != undefined) {
			var idx = activityEventCtrl.selectedWeek.indexOf(week);
	        if (idx > -1) {
	        	activityEventCtrl.selectedWeek.splice(idx, 1);
	        } else {
	        	activityEventCtrl.selectedWeek.push(week);
	        }
		}
	
        activityEventCtrl.event.chrono = {
    		"type": "week", 
    		"parameter": {
//    			"numRepetition": 1, 
    			"days": []
    		}
        };
        
        for(var k in activityEventCtrl.selectedWeek ) {
        	activityEventCtrl.event.chrono.parameter.days.push(activityEventCtrl.selectedWeek[k]);
        }
	};
	
	activityEventCtrl.changeTypeFrequency = function() {
		$timeout(function() {
			var tip = activityEventCtrl.eventSched.repetitionKind;
			
			switch(tip) {
				case 'event': 
					activityEventCtrl.event.chrono = {
						"type": "event", 
						"parameter": {
							"type": activityEventCtrl.eventSched.event_type
						}
					};
								
					if(activityEventCtrl.eventSched.event_type == 'dataset') {
						activityEventCtrl.event.chrono.parameter.dataset = activityEventCtrl.eventSched.dataset;
						activityEventCtrl.event.chrono.parameter.frequency = activityEventCtrl.eventSched.frequency;
					}
					
					break;
					
				case 'single': 
					activityEventCtrl.event.chrono = {
						"type": "single"
					}; 
					
					break;
				case 'minute': 
					activityEventCtrl.event.chrono = {
						"type": "minute", 
						"parameter": {
							"numRepetition": activityEventCtrl.eventSched.minute_repetition_n
						}
					}; 
					
					break;
					
				case 'hour': 
					activityEventCtrl.event.chrono = {
						"type": "hour", 
						"parameter": {
							"numRepetition": activityEventCtrl.eventSched.hour_repetition_n
							}
					}; 
					
					break;
					
				case 'day': 
					activityEventCtrl.event.chrono = {
						"type": "day", 
						"parameter": {
							"numRepetition": activityEventCtrl.eventSched.day_repetition_n
							}
					};
					
					break;
					
				case 'week': 
					activityEventCtrl.toggleWeek(); 
					break;
				case 'month': 
					activityEventCtrl.toggleMonthScheduler();
					break;
			}
			
			console.log('chrono', activityEventCtrl.event.chrono);
		}, 500);
	};
	
	activityEventCtrl.isChecked = function (item, list, condition) {
		if(condition) {
			return list == undefined? 
					false : list.indexOf(item) > -1;
		} else {
			return false;
		}
	};
	
	activityEventCtrl.toggleDocFunct = function(doc, funct) {
		if(funct != undefined) {
			if(doc.funct == undefined) {
				doc.funct = [];
			}
			
			var idx = doc.funct.indexOf(funct);
	       
			if (idx > -1) {
	        	doc.funct.splice(idx, 1);
	        } else {
	        	doc.funct.push(funct);
	        }
		}
	};
	
	activityEventCtrl.onlyNumberConvert = function(item) {
		return item.replace(/\D/g,'');
	};
			
			
	activityEventCtrl.prova = function(item) {
		console.log("prova",item); 	
	};
	
	activityEventCtrl.toggleEnabled = function(item,item2) {
		console.log("toggleEnabled",item,item2); 	
	};
	
	activityEventCtrl.sampleModel=[{name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
	                               {name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
	                               {name:"name1",surname:"surname1",enabled:'true',age:'<md-checkbox  ng-checked="row.enabled" ng-click="row.enabled=!row.enabled">{{row.enabled}}</md-checkbox>'},
	                               {name:"name3",surname:"surname3",age:'<md-checkbox   ng-click="toggleEnabled(row, key)" ng-init="true"></md-checkbox>'},
	                               {name:"name5",surname:"surname5",age:"18"},
	                               {name:"name6",surname:"surname6",age:"32"},
	                               {name:"name7",surname:"surname7",age:"18"},
	                               {name:"name8",surname:"surname8",age:"18"},
	                               {name:"name9",surname:"surname9",age:"18"},
	                               {name:"name10",surname:"surname10",age:"18"},
	                               {name:"name11",surname:"surname11",age:"27"},
	                               {name:"name12",surname:"surname12",age:"18"},
	                               {name:"name13",surname:"surname13",age:"18"},
	                               {name:"name14",surname:"surname14",age:"11"},
	                               {name:"name15",surname:"surname15",age:"18"},
	                               {name:"name16",surname:"surname16",age:"18"},
	                               {name:"name17",surname:"surname17",age:"80"},
	                               {name:"name18",surname:"surname18",age:"18"},
	                               {name:"name19",surname:"surname19",age:"18"},
	                             ];
	activityEventCtrl.MenuOpt = 
		[{
			label : 'action1',
			action : function(item,event) {
					myfunction1(event,item);
			}
		},
		{
			label : 'action2',
			action : function(item,event) {
					myfunction2 (event,item);
			}
		}];

	activityEventCtrl.SpeedMenuOpt  = [
	            	 {
	            		label : 'action1',
	            		 icon:'fa fa-pencil' ,  
	            		backgroundColor:'red',  
	            		 color:'black',		
	            		action : function(item,event) {
	            				myFunction(event,item);
	            		 }
	            	} 
	            ];

	
	activityEventCtrl.showInfoBox=function(title,text,parentId){
				$mdDialog.show(
					      $mdDialog.alert()
					        .clickOutsideToClose(true)
					        .title(title)
					        .content(text)
					        .ariaLabel('info dialog')
					        .ok(sbiModule_translate.load("sbi.general.close")) 
					        .openFrom('#'+parentId)
					        .closeTo('#'+parentId)
					    );

	}
	
}]);