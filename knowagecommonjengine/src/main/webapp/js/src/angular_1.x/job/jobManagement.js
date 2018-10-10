/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

var app=angular.module('jobManagementModule',['angular_table', 'ngMaterial', 'sbiModule']);

var jobManagementCtrl;

app.controller('JobController', ['$scope', 'sbiModule_translate', '$http', '$interval','$filter', 'sbiModule_config', jobManagementFunction]);

function jobManagementFunction($scope,sbiModule_translate, $http, $interval, $filter, sbiModule_config){
	$scope.translate=sbiModule_translate;
	
	$scope.config=sbiModule_config;
		
	$scope.jobColumnNames=[
	                       {"label": sbiModule_translate.load('sbi.commonj.processId'),"name":"pid", "size":"350"},
	                       {"label": sbiModule_translate.load('sbi.commonj.status'),"name":"status", "size":"350"},
	                       {"label": sbiModule_translate.load('sbi.commonj.time'), "name":"time", "size":"350"
	                    	  //, transformer: function(data){ return $filter('time')(data, 'dd/MM/yyyy');  }
	                       }	                    	
	                       ];	
	
	jobManagementCtrl = this;

	jobManagementCtrl.initValues = function(documentId, parameters){
		jobManagementCtrl.documentId = documentId;
		jobManagementCtrl.parameters = parameters;
		jobManagementCtrl.currentStatus = 'notstarted';
		jobManagementCtrl.jobList = [];
	}

	jobManagementCtrl.startJob=function(){
		
		var url = $scope.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=START_WORK';

		var driversPars=decodeURIComponent(jobManagementCtrl.parameters);
		if(driversPars && driversPars!=''){
			url +='&'+driversPars;
		}
		
		$http.get(url, {
			params: {
				DOCUMENT_ID: jobManagementCtrl.documentId,
				SBI_EXECUTION_ID: -1,
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			}
		}).then(function(response) {
			
			var statusToWrite = jobManagementCtrl.translateStatus(response.data.status);
			var dateToFormat = response.data.time;
			jobManagementCtrl.currentDate = jobManagementCtrl.useDateFormat(dateToFormat);
			
			jobManagementCtrl.status = statusToWrite;
			jobManagementCtrl.pid = response.data.pid;

			jobManagementCtrl.jobList = [];

			jobManagementCtrl.jobList = [{
				pid: jobManagementCtrl.pid, 
				status: jobManagementCtrl.status, 
				time: jobManagementCtrl.currentDate
			}];		

			jobManagementCtrl.currentStatus='started';

		});
//		.error(function(error){
//			$scope.showAlert('Attention, ' + $scope.userName,"Error Calling REST service for process. Please check if the server or connection is working.")
//		});
	}

	jobManagementCtrl.statusJob=function(){

		var url = $scope.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=STATUS_WORK';		
		$http.get(url, {
			params: {
				PROCESS_ID: jobManagementCtrl.pid,
				DOCUMENT_ID: jobManagementCtrl.documentId,
				SBI_EXECUTION_ID: -1,
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			}
		}).then(function(response) {
			var statusCode = response.data.status_code;
			if(statusCode == 0 ){
				jobManagementCtrl.currentStatus = 'notstarted';
			}
			else if(statusCode == 4 ){
				if(jobManagementCtrl.currentStatus != 'completed' || jobManagementCtrl.currentStatus != 'rejected'){
					var statusToWrite = jobManagementCtrl.translateStatus(response.data.status);
					var dateToFormat = response.data.time;
					jobManagementCtrl.currentDate = jobManagementCtrl.useDateFormat(dateToFormat);
					
					jobManagementCtrl.status = statusToWrite;
					jobManagementCtrl.jobList = [{
						pid: jobManagementCtrl.pid, 
						status: jobManagementCtrl.status, 
						time: jobManagementCtrl.currentDate
					}];		
				}
				jobManagementCtrl.currentStatus = 'completed';
				$interval.cancel(jobManagementCtrl.stop);

				
			}
			else if(statusCode == 2 ){
				jobManagementCtrl.currentStatus = 'rejected';
				$interval.cancel(jobManagementCtrl.stop);
			}
			else if(statusCode == 1 ){
				jobManagementCtrl.currentStatus = 'accepted';
			}
			else {
				jobManagementCtrl.currentStatus = 'started';

			}
		});
//		.error(function(error){
//			$scope.showAlert('Attention, ' + $scope.userName,"Error Calling REST service for process. Please check if the server or connection is working.")
//		});;

	}
	

	jobManagementCtrl.stopJob=function(){

		var url = $scope.config.contextName+'/servlet/AdapterHTTP?ACTION_NAME=STOP_WORK';		
		$http.get(url, {
			params: {
				PROCESS_ID: jobManagementCtrl.pid,
				DOCUMENT_ID: jobManagementCtrl.documentId,
				SBI_EXECUTION_ID: -1,
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
			}
		}).then(function(response) {
						
			var statusToWrite = jobManagementCtrl.translateStatus(response.data.status);
						
			var dateToFormat = response.data.time;
			jobManagementCtrl.currentDate = jobManagementCtrl.useDateFormat(dateToFormat);
			
			jobManagementCtrl.status = statusToWrite;
			jobManagementCtrl.jobList = [{
				pid: jobManagementCtrl.pid, 
				status: jobManagementCtrl.status, 
				time: jobManagementCtrl.currentDate
			}];		
			jobManagementCtrl.currentStatus='completed';
			$interval.cancel(jobManagementCtrl.stop);
		});
//		.error(function(error){
//			$scope.showAlert('Attention, ' + $scope.userName,"Error Calling REST service for process. Please check if the server or connection is working.")
//		});;

	}
	
	jobManagementCtrl.useDateFormat = function(dateToFormat){
		var formattedDate;
		if($scope.config.timestampFormat != null && $scope.config.timestampFormat != undefined && $scope.config.timestampFormat != 'null'){
			formattedDate = $filter('date')(dateToFormat, $scope.config.timestampFormat);
		}
		else{
			formattedDate = dateToFormat;
		}
		return formattedDate;
	}
	
	
	
	jobManagementCtrl.translateStatus=function(message){
		var statusToWrite;
		if(message === 'work_accepted'){
			statusToWrite = $scope.translate.load('sbi.commonj.work_started');
		}
		else if(message === 'work_not_started'){
			statusToWrite = $scope.translate.load('sbi.commonj.work_not_started');
		}
		else if(message === 'work_completed'){
			statusToWrite = $scope.translate.load('sbi.commonj.work_completed');
		}
		else if(message === 'work_rejected'){
			statusToWrite = $scope.translate.load('sbi.commonj.work_rejected');
		}
		return statusToWrite;
	}
	
	jobManagementCtrl.stop = $interval(jobManagementCtrl.statusJob, 5000);

}