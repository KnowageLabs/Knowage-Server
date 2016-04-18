var app = angular.module('schedulerKpi');
app.controller('frequencyController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIControllerFunction ]);

function KPIControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	
	sbiModule_translate.addMessageFile("component_scheduler_messages");
	$scope.translate = sbiModule_translate;
	$scope.intervalsFrequency = {};
	$scope.typeToolbar = [
//{value: 'single', label: sbiModule_translate.load("scheduler.singleExec", "component_scheduler_messages")},
{value: 'scheduler', label: sbiModule_translate.load("scheduler.schedulerExec", "component_scheduler_messages")} //,
//{value: 'event', label: sbiModule_translate.load("scheduler.eventExec", "component_scheduler_messages")}
];

	$scope.EVENT_TYPES = [
	                      {value: 'rest', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.rest")},
	                      {value: 'jms', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.jms")},
	                      {value: 'contextbroker', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.contextbroker")},
	                      {value: 'dataset', label: sbiModule_translate.load("sbi.scheduler.schedulation.events.event.type.dataset")}
	                      ];


	$scope.intervals = [
	                    {value: 'minute', label: sbiModule_translate.load("scheduler.minuteExec", "component_scheduler_messages")},
	                    {value: 'hour', label: sbiModule_translate.load("scheduler.hourExec", "component_scheduler_messages")},
	                    {value: 'day', label: sbiModule_translate.load("scheduler.dayExec", "component_scheduler_messages")},
	                    {value: 'week', label: sbiModule_translate.load("scheduler.weekExec", "component_scheduler_messages")},
	                    {value: 'month', label: sbiModule_translate.load("scheduler.monthExec", "component_scheduler_messages")}
	                    ];

	$scope.MONTHS = [
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

	$scope.WEEKS = [
	                {label: sbiModule_translate.load("scheduler.sun", "component_scheduler_messages"), value: '1'}, 
	                {label: sbiModule_translate.load("scheduler.mon", "component_scheduler_messages"), value: '2'}, 
	                {label: sbiModule_translate.load("scheduler.tue", "component_scheduler_messages"), value: '3'}, 
	                {label: sbiModule_translate.load("scheduler.wed", "component_scheduler_messages"), value: '4'}, 
	                {label: sbiModule_translate.load("scheduler.thu", "component_scheduler_messages"), value: '5'}, 
	                {label: sbiModule_translate.load("scheduler.fri", "component_scheduler_messages"), value: '6'}, 
	                {label: sbiModule_translate.load("scheduler.sat", "component_scheduler_messages"), value: '7'}
	                ];

	$scope.WEEKS_ORDER = [
	                      {label: sbiModule_translate.load("scheduler.firstweek", "component_scheduler_messages"), value: '1'}, 
	                      {label: sbiModule_translate.load("scheduler.secondweek", "component_scheduler_messages"), value: '2'}, 
	                      {label: sbiModule_translate.load("scheduler.thirdweek", "component_scheduler_messages"), value: '3'}, 
	                      {label: sbiModule_translate.load("scheduler.fourthweek", "component_scheduler_messages"), value: '4'}, 
	                      {label: sbiModule_translate.load("scheduler.lastweek", "component_scheduler_messages"), value: '5'}, 
	                      ];

	$scope.getNitem = function(n) {
		var r =[];
		for(var i = 1; i <= n; i++) {
			r.push(i);
		}
		return r;
	};


	$scope.toggleWeek = function(week) {
		if(week != undefined) {
			var idx = $scope.selectedWeek.indexOf(week);
	        if (idx > -1) {
	        	$scope.selectedWeek.splice(idx, 1);
	        } else {
	        	$scope.selectedWeek.push(week);
	        }
		}
	
		$scope.selectedScheduler.crono = {
    		"type": "week", 
    		"parameter": {
//    			"numRepetition": 1, 
    			"days": []
    		}
        };
        
        for(var k in $scope.selectedWeek ) {
        	$scope.selectedScheduler.crono.parameter.days.push($scope.selectedWeek[k]);
        }
	};
	
	$scope.exists = function (item, list) {
		if(list==undefined)return false;
		return  $scope.indexInList(item, list)>-1;

	};

	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object==item){
				return i;
			}
		}

		return -1;
	};
	
	$scope.toggleMonthScheduler = function() {
		$scope.selectedScheduler.crono = {
			"type": "month", 
			"parameter": {}
		};
		 
		if($scope.typeMonth == true) {
			$scope.selectedScheduler.crono.parameter.numRepetition = $scope.frequency.value.month_rep;
		} else {
			$scope.selectedScheduler.crono.parameter.months = [];
			for(var k in $scope.frequency.value.month_repetition) {
				$scope.selectedScheduler.crono.parameter.months.push($scope.frequency.value.month_repetition[k]);
			}
		}
			
		if($scope.typeMonthWeek == true) {
			$scope.selectedScheduler.crono.parameter.dayRepetition = $scope.frequency.value.dayinmonthrep_week;
		} else {
			//var mwnr = $scope.frequency.value.month_week_number_repetition;
			
//			if($scope.mwnr == undefined) {
//				$scope.mwnr = 'first';
//			}
//			
			$scope.selectedScheduler.crono.parameter.days = [];
			
			$scope.selectedScheduler.crono.parameter.weeks = $scope.frequency.value.month_week_number_repetition;
			
			for(var k in $scope.frequency.value.month_week_repetition) {
				$scope.selectedScheduler.crono.parameter.days.push($scope.frequency.value.month_week_repetition[k]);
			}
		}
	};
	
	
	$scope.changeTypeFrequency = function() {
		$timeout(function() {
			var tip = $scope.frequency.selectInterval;
			
			switch(tip) {
				case 'event': 
					$scope.selectedScheduler.crono = {
						"type": "event", 
						"parameter": {
							"type": event_type
						}
					};
								
					if(event_type == 'dataset') {
						$scope.selectedScheduler.crono.parameter.dataset = dataset;
						$scope.selectedScheduler.crono.parameter.frequency = frequency;
					}
					
					break;
					
				case 'single': 
					$scope.selectedScheduler.crono = {
						"type": "single"
					}; 
					
					break;
				case 'minute': 
					$scope.selectedScheduler.crono = {
						"type": "minute", 
						"parameter": {
							"numRepetition": $scope.frequency.value.minute
						}
					}; 
					break;
					
				case 'hour': 
					$scope.selectedScheduler.crono = {
						"type": "hour", 
						"parameter": {
							"numRepetition": $scope.frequency.value.hour
							}
					}; 
					break;
					
				case 'day': 
					$scope.selectedScheduler.crono = {
						"type": "day", 
						"parameter": {
							"numRepetition": $scope.frequency.value.day
							}
					};
					break;
					
				case 'week': 
					$scope.toggleWeek(); 
					break;
				case 'month': 
					$scope.toggleMonthScheduler();
					break;
			}
		}, 500);

	};
	
	$scope.typeMonthEdit = function(){
		if ($scope.typeMonth==undefined)
			$scope.typeMonth!=true
			else $scope.typeMonth=!$scope.typeMonth ;
	}
	
	
	$scope.typeMonthWeekEdit = function(){
		if ($scope.typeMonthWeek==undefined)
			$scope.typeMonthWeek!=true
			else $scope.typeMonthWeek=!$scope.typeMonthWeek ;
	}

};