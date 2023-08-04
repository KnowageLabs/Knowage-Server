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
		   .service('datasetScheduler_service', ['sbiModule_restServices', function(sbiModule_restServices){
			   var scheduler = {};
			   
			   this.setScheduler = function(schedul) {
				   scheduler = schedul;
			   }
			   
			   this.schedulDataset = function(dataset) {
				   return sbiModule_restServices.promisePost('scheduleree/persistence/dataset/id', dataset.id, angular.toJson(dataset));
			   }
			   
			   this.unschedulDataset = function(dataSet) {
				   return sbiModule_restServices.promiseDelete('scheduleree/persistence/dataset/label', dataSet.label, "/");
			   }
			   
			   this.createSchedulingCroneLine = function() {
					
					var finalCronString = "";

					var secondsForCron = 0;
					var minutesForCron = "";
					var hoursForCron = "";
					var daysForCron = "";
					var monthsForCron = "";
					var weekdaysForCron = "";

					if (scheduler.minutesCustom) {
						for (i=0; i<scheduler.minutesSelected.length; i++) {
							minutesForCron += "" + scheduler.minutesSelected[i];

							if (i<scheduler.minutesSelected.length-1) {
								minutesForCron += ",";
							}
						}
					}
					else {
						minutesForCron = "*";
					}

					if (scheduler.hoursCustom) {
						for (i=0; i<scheduler.hoursSelected.length; i++) {
							hoursForCron += "" + scheduler.hoursSelected[i];

							if (i<scheduler.hoursSelected.length-1) {
								hoursForCron += ",";
							}
						}
					}
					else {
						hoursForCron = "*";
					}

					if (scheduler.daysCustom) {
						for (i=0; i<scheduler.daysSelected.length; i++) {
							daysForCron += "" + scheduler.daysSelected[i];

							if (i<scheduler.daysSelected.length-1) {
								daysForCron += ",";
							}
						}
					}
					else {
						daysForCron = "*";
					}

					if (scheduler.monthsCustom) {
						for (i=0; i<scheduler.monthsSelected.length; i++) {
							monthsForCron += "" + scheduler.monthsSelected[i];

							if (i<scheduler.monthsSelected.length-1) {
								monthsForCron += ",";
							}
						}
					}
					else {
						monthsForCron = "*";
					}

					if (scheduler.weekdaysCustom) {
						for (i=0; i<scheduler.weekdaysSelected.length; i++) {
							weekdaysForCron += "" + scheduler.weekdaysSelected[i];

							if (i<scheduler.weekdaysSelected.length-1) {
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

					return secondsForCron + " " + finalCronString;
				
				} 
			   
		   }]);
}());