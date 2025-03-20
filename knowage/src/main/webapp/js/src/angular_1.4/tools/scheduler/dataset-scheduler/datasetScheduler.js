/**
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2019 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
(function() {
	
	angular.module('datasetSchedulerModule')
		   .directive('datasetScheduler', function(sbiModule_config){
				return {
					templateUrl : sbiModule_config.dynamicResourcesBasePath + '/angular_1.4/tools/scheduler/dataset-scheduler/dataset-scheduler.html',
					controller : scheduleDataset,
					scope : {
						model : '='
					},
					link : function(scope, elem, attrs) {

					}

				};
		   });
	
	function scheduleDataset($scope, $rootScope, sbiModule_translate, sbiModule_config, sbiModule_user, datasetScheduler_service) {
		
		$scope.translate = sbiModule_translate;
		$scope.scheduling = {};
		
		$scope.checkPickedEndDate = function() {
			if (new Date($scope.model.endDate) < new Date($scope.model.startDate)) {
				$scope.model.startDate = null;
			}
		}

		$scope.checkPickedStartDate = function() {
			if (new Date($scope.model.startDate) > new Date($scope.model.endDate)) {
				$scope.model.endDate = null;
			}
		}
		
		// Setting for minutes for Scheduling
		$scope.minutes = new Array(60);

		$scope.minutesClearSelections = function() {
			$scope.scheduling.minutesSelected = [];
			$scope.scheduling.minutesCustom = undefined;
		}

		// Setting for hours for Scheduling
		$scope.hours = new Array(24);

		$scope.hoursClearSelections = function() {
			$scope.scheduling.hoursSelected = [];
			$scope.scheduling.hoursCustom = undefined;
		}

		$scope.days = new Array();

		// Setting for days for Scheduling
		var populateDays = function() {
			for (i=1; i<=31; i++) {
				$scope.days.push(i);
			}
		}

		populateDays();

		$scope.daysClearSelections = function() {
			$scope.scheduling.daysSelected = [];
			$scope.scheduling.daysCustom = undefined;
		}

		// Setting for month for Scheduling
		$scope.months = new Array();

		var populateMonths = function() {
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.january"), 	value: 1});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.february"), 	value: 2});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.march"), 		value: 3});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.april"), 		value: 4});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.may"), 		value: 5});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.june"), 		value: 6});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.july"), 		value: 7});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.august"), 	value: 8});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.september"), 	value: 9});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.october"), 	value: 10});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.november"), 	value: 11});
			$scope.months.push({name: $scope.translate.load("kn.ds.persist.cron.month.december"), 	value: 12});
		}

		populateMonths();

		$scope.monthsClearSelections = function() {
			$scope.scheduling.monthsSelected = [];
			$scope.scheduling.monthsCustom = undefined;
		}

		// Setting for month for Scheduling
		$scope.weekdays = new Array();

		var populateWeekdays = function() {
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.monday"), 	value: 1});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.tuesday"), 	value: 2});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.wednesday"), 	value: 3});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.thursday"), 	value: 4});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.friday"), 	value: 5});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.saturday"), 	value: 6});
			$scope.weekdays.push({name: $scope.translate.load("kn.ds.persist.cron.weekday.sunday"), 	value: 7});
		}

		populateWeekdays();

		$scope.weekdaysClearSelections = function() {
			$scope.scheduling.weekdaysSelected = [];
			$scope.scheduling.weekdaysCustom = undefined;
		}
		
		var createSchedulingCroneLine = function() {
			
			var finalCronString = "";

			var secondsForCron = 0;
			var minutesForCron = "";
			var hoursForCron = "";
			var daysForCron = "";
			var monthsForCron = "";
			var weekdaysForCron = "";

			if ($scope.scheduling.minutesCustom) {
				for (i=0; i<$scope.scheduling.minutesSelected.length; i++) {
					minutesForCron += "" + $scope.scheduling.minutesSelected[i];

					if (i<$scope.scheduling.minutesSelected.length-1) {
						minutesForCron += ",";
					}
				}
			}
			else {
				minutesForCron = "*";
			}

			if ($scope.scheduling.hoursCustom) {
				for (i=0; i<$scope.scheduling.hoursSelected.length; i++) {
					hoursForCron += "" + $scope.scheduling.hoursSelected[i];

					if (i<$scope.scheduling.hoursSelected.length-1) {
						hoursForCron += ",";
					}
				}
			}
			else {
				hoursForCron = "*";
			}

			if ($scope.scheduling.daysCustom) {
				for (i=0; i<$scope.scheduling.daysSelected.length; i++) {
					daysForCron += "" + $scope.scheduling.daysSelected[i];

					if (i<$scope.scheduling.daysSelected.length-1) {
						daysForCron += ",";
					}
				}
			}
			else {
				daysForCron = "*";
			}

			if ($scope.scheduling.monthsCustom) {
				for (i=0; i<$scope.scheduling.monthsSelected.length; i++) {
					monthsForCron += "" + $scope.scheduling.monthsSelected[i];

					if (i<$scope.scheduling.monthsSelected.length-1) {
						monthsForCron += ",";
					}
				}
			}
			else {
				monthsForCron = "*";
			}

			if ($scope.scheduling.weekdaysCustom) {
				for (i=0; i<$scope.scheduling.weekdaysSelected.length; i++) {
					weekdaysForCron += "" + $scope.scheduling.weekdaysSelected[i];

					if (i<$scope.scheduling.weekdaysSelected.length-1) {
						weekdaysForCron += ",";
					}
				}
			}
			else {
				weekdaysForCron = "*";
			}

			if (daysForCron == '*' && weekdaysForCron != '*') {
				daysForCron = '?';
			} else {
				weekdaysForCron = '?';
			}

			finalCronString = minutesForCron + " " + hoursForCron +
								" " + daysForCron + " " + monthsForCron +  " " + weekdaysForCron;

			$scope.model.schedulingCronLine = secondsForCron + " " + finalCronString;
		
		} 
		
		var initScheduler = function() {
			datasetScheduler_service.setScheduler($scope.scheduling);
		}
		
		initScheduler();
		
	}
	
})();