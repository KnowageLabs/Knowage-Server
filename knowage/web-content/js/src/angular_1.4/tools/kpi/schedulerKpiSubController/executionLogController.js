var app = angular.module('schedulerKpi').controller('executionLogController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout','sbiModule_dateServices','sbiModule_download',executionLogControllerFunction ]);

function executionLogControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout,sbiModule_dateServices,sbiModule_download){
	$scope.translate=sbiModule_translate;
	$scope.kpiValueExecLogList = [];
	$scope.numberLogs = 10;
	$scope.loadLog = function(){
		if($scope.numberLogs>0){
			sbiModule_restServices.promiseGet("1.0/kpi",$scope.selectedScheduler.id+'/'+$scope.numberLogs+ '/logExecutionList').then(
					function(response) {
						angular.copy(response.data,$scope.kpiValueExecLogList)
						for(var i=0;i<$scope.kpiValueExecLogList.length;i++){
							if($scope.kpiValueExecLogList[i].outputPresent){
								$scope.kpiValueExecLogList[i]["icon"] = '<md-button class="md-icon-button" ng-click="scopeFunctions.download(row,$event)" > <md-icon md-font-icon="fa fa-download" style=" margin-top: 6px ; color: #153E7E;"></md-icon> </md-button>';
							}
							$scope.kpiValueExecLogList[i].timeRun = sbiModule_dateServices.formatDate(new Date($scope.kpiValueExecLogList[i].timeRun));
						}
					},function(response) {
						$scope.errorHandler(response.data,"");
					})
		}

	}
	$scope.tableFunction={

			download: function(item,evt){
				$scope.download(item);
			}
	}

	$scope.download = function(item){
		//sbiModule_download
		if(item.outputPresent){
			sbiModule_restServices.promiseGet("1.0/kpi",item.id+'/logExecutionListOutputContent').then(
					function(response) {
						sbiModule_download.getBlob(response.data.output,$scope.selectedScheduler.name+"ErrorLog", 'text/plain', 'txt' );

					},function(response) {
						$scope.errorHandler(response.data,"");
					})
		}
		
	}

	$scope.$on('activeExecutionLog', function(e) { 
			$scope.loadLog();
		
	});


}