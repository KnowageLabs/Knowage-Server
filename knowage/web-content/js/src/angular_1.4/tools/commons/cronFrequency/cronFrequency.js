/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v1.0.0
 * 
 */
var scriptsCF = document.getElementsByTagName("script");
var currentScriptPathCF = scriptsCF[scriptsCF.length - 1].src;

angular.module('cron_frequency', [ 'ngMaterial','sbiModule','angular_time_picker'])
.directive('cronFrequency', function() {
	  return {
	    templateUrl: currentScriptPathCF.substring(0, currentScriptPathCF.lastIndexOf('/') + 1) +'/template/cronFrequencyTemplate.html',
	    controller: cronFrequencyFunction,
	    scope: {
	    	ngModel:'=',
	    	id:"@"
	    	},
	      link: function (scope, elm, attrs) { 
	    	  console.log("Inizializzo Crone");
	    	  function initNgModel(){
	    		  //convert string date to Date object
	    		  	scope.ngModel.startDate = new Date(scope.ngModel.startDate);
	    		  	if(scope.ngModel.endDate != undefined && scope.ngModel.endDate != "") {
	    		  		scope.ngModel.endDate = new Date(scope.ngModel.endDate);
					}
	    		  
	    		  	//load crono structure
	    		  	scope.eventSched.repetitionKind=scope.ngModel.crono.type
	    		  
					if(scope.eventSched.repetitionKind == 'minute'){
						scope.eventSched.minute_repetition_n=scope.ngModel.crono.parameter.numRepetition;
					} else if(scope.eventSched.repetitionKind == 'hour'){
						scope.eventSched.hour_repetition_n=scope.ngModel.crono.parameter.numRepetition;
					} else if(scope.eventSched.repetitionKind == 'day'){
						scope.eventSched.day_repetition_n=scope.ngModel.crono.parameter.numRepetition;
					} else if(scope.eventSched.repetitionKind == 'week') {	
						scope.selectedWeek = scope.ngModel.crono.parameter.days;
					} else if(scope.eventSched.repetitionKind== 'month') {
						if(scope.ngModel.crono.parameter.hasOwnProperty("months")) {
							scope.typeMonth = false;
							scope.month_repetition = scope.ngModel.crono.parameter.months;
						} else {
							scope.typeMonth = true;
							scope.monthrep_n = scope.ngModel.crono.parameter.numRepetition;
							scope.month_week_number_repetition = scope.ngModel.crono.parameter.weeks;
							scope.month_week_repetition = scope.ngModel.crono.parameter.days;
						}
						
						if(scope.ngModel.crono.parameter.hasOwnProperty("days")) {
							scope.typeMonthWeek = false;
							scope.month_week_number_repetition = scope.ngModel.crono.parameter.weeks;
							scope.month_week_repetition = scope.ngModel.crono.parameter.days;
						} else {
							scope.typeMonthWeek = true;
							scope.dayinmonthrep_week = scope.ngModel.crono.parameter.dayRepetition;
						}
					}
				
	    		  
	    	  }
	    	  initNgModel();
		    	  
	    	  }
	  }
	  	});


	function cronFrequencyFunction($scope,sbiModule_translate,$timeout){ 
		sbiModule_translate.addMessageFile("component_scheduler_messages");
		$scope.translate=sbiModule_translate;
		$scope.eventSched={};
		$scope.selectedWeek = [];
		$scope.eventSched.month_repetition=[];
		$scope.eventSched.month_week_repetition=[];
		$scope.EVENT_INTERVALS = [
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
		
		$scope.eventSched.repetitionKind=$scope.EVENT_INTERVALS[0].value;
		
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
		
			$scope.ngModel.crono = {
	    		"type": "week", 
	    		"parameter": {
	    			"days": []
	    		}
	        };
	        
	        for(var k in $scope.selectedWeek ) {
	        	$scope.ngModel.crono.parameter.days.push($scope.selectedWeek[k]);
	        }
		};
		
		$scope.weekIsChecked = function (item) {
			return 	($scope.ngModel.crono==undefined  || $scope.ngModel.crono.parameter==undefined || $scope.ngModel.crono.parameter.days == undefined)? false : 	$scope.ngModel.crono.parameter.days.indexOf(item) > -1;
		};
		
		$scope.changeTypeFrequency = function() {
			$timeout(function() {
				var tip = $scope.eventSched.repetitionKind;
				
				switch(tip) {
					
					case 'minute': 
						$scope.ngModel.crono = {
							"type": "minute", 
							"parameter": {
								"numRepetition": $scope.eventSched.minute_repetition_n
							}
						}; 
						
						break;
						
					case 'hour': 
						$scope.ngModel.crono = {
							"type": "hour", 
							"parameter": {
								"numRepetition": $scope.eventSched.hour_repetition_n
								}
						}; 
						
						break;
						
					case 'day': 
						$scope.ngModel.crono = {
							"type": "day", 
							"parameter": {
								"numRepetition": $scope.eventSched.day_repetition_n
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
		
		//init type frequency
		$scope.changeTypeFrequency();
		
		
		$scope.toggleMonthScheduler = function() {
			$scope.ngModel.crono = {
				"type": "month", 
				"parameter": {}
			};
			 
			if($scope.eventSched.typeMonth == true) {
				$scope.ngModel.crono.parameter.numRepetition = $scope.eventSched.monthrep_n;
			} else {
				$scope.ngModel.crono.parameter.months = [];
				for(var k in $scope.eventSched.month_repetition) {
					$scope.ngModel.crono.parameter.months.push($scope.eventSched.month_repetition[k]);
				}
			}
				
			if($scope.eventSched.typeMonthWeek == true) {
				$scope.ngModel.crono.parameter.dayRepetition = $scope.eventSched.dayinmonthrep_week;
			} else {
				var mwnr = $scope.eventSched.month_week_number_repetition;
				
				if(mwnr == undefined) {
					mwnr = '1';
				}
				
				$scope.ngModel.crono.parameter.weeks = mwnr;
				$scope.ngModel.crono.parameter.days = [];
				
				for(var k in $scope.eventSched.month_week_repetition) {
					$scope.ngModel.crono.parameter.days.push($scope.eventSched.month_week_repetition[k]);
				}
			}
		};
	}
