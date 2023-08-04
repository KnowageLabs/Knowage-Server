var app = angular.module('schedulerKpi').controller('executionLogController', ['$scope','sbiModule_translate', 'sbiModule_messaging',"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout','sbiModule_dateServices','sbiModule_download',executionLogControllerFunction ]);

function executionLogControllerFunction($scope,sbiModule_translate, sbiModule_messaging,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout,sbiModule_dateServices,sbiModule_download){
	$scope.translate=sbiModule_translate;
	$scope.kpiValueExecLogList = [];
	$scope.numberLogs = 10;
	$scope.loadLog = function(){
		if($scope.numberLogs>0 && $scope.selectedScheduler && $scope.selectedScheduler.id){
			sbiModule_restServices.promiseGet("1.0/kpi",$scope.selectedScheduler.id+'/'+$scope.numberLogs+ '/logExecutionList').then(
					function(response) {
						angular.copy(response.data,$scope.kpiValueExecLogList)
						if($scope.kpiValueExecLogList.length != 0) {
							for(var i=0;i<$scope.kpiValueExecLogList.length;i++){
								if($scope.kpiValueExecLogList[i].outputPresent){
									$scope.kpiValueExecLogList[i]["icon"] = '<md-button class="md-icon-button" ng-click="scopeFunctions.download(row,$event)" > <md-icon md-font-icon="fa fa-download" style=" margin-top: 6px ; color: #153E7E;"></md-icon> </md-button>';
								}
								var date = new Date($scope.kpiValueExecLogList[i].timeRun);
								$scope.kpiValueExecLogList[i].timeRun = sbiModule_dateServices.formatDate(date)+"	"+sbiModule_dateServices.formatDate(date,'HH:mm:ss');
							}
						} else {
							sbiModule_messaging.showInfoMessage(sbiModule_translate.load("sbi.kbi.scheduler.info.nologs"), 'Info');
						}
					},function(response) {
						sbiModule_restServices.errorHandler(response.data,"");
					});
		}else{
			$scope.kpiValueExecLogList = [];
		}
	}

	$scope.tableFunction={
			download: function(item,evt){
				$scope.download(item);
			}
	}

	$scope.download = function(item){
		if(item.id!=undefined){
			//sbiModule_download
			if(item.outputPresent){
				sbiModule_restServices.promiseGet("1.0/kpi",item.id+'/logExecutionListOutputContent').then(
						function(response) {
							sbiModule_download.getBlob(response.data.output,$scope.selectedScheduler.name+"ErrorLog", 'text/plain', 'txt' );

					},function(response) {
						sbiModule_restServices.errorHandler(response.data,"");
					})
			}
		}


	}

	$scope.$on('activeExecutionLog', function(e) {
			$scope.loadLog();

	});


}