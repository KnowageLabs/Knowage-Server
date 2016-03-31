angular.module('scorecardManager').controller('scorecardTargetDefinitionController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices','sbiModule_config','$filter','$mdDialog',scorecardTargetDefinitionControllerFunction ]);

function scorecardTargetDefinitionControllerFunction($scope,sbiModule_translate,sbiModule_restServices,sbiModule_config,$filter,$mdDialog){
	$scope.kpiList=[];
	
	$scope.parseDate = function(date){
		result = "";
		if(date == "d/m/Y"){
			result = "dd/MM/yyyy";
		}
		if(date =="m/d/Y"){
			result = "MM/dd/yyyy"
		}
		return result;
	};
	
	$scope.getListKPI = function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listKpi")
		.then(function(response){ 
			for(var i=0;i<response.data.length;i++){
				var obj = angular.extend({},response.data[i]);
				var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
				$scope.kpiList.push(obj);
			}
		},function(response){
			sbiModule_restServices.errorhandler(response.data,"");
		});
	};
	$scope.getListKPI();
	
	$scope.addKpiToTarget=function(){ 
		var tmpTargetKpis=[];
		if($scope.currentTarget.kpis==undefined){
			$scope.currentTarget.kpis = [];
		} 
		
		angular.copy($scope.currentTarget.kpis,tmpTargetKpis);
		debugger;
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: 'templatesaveKPI.html',
			clickOutsideToClose:false,
			preserveScope:true,
			locals: {
				kpiList: $scope.kpiList,
				tmpTargetKpis: tmpTargetKpis}
		})
		.then(function(data) {
		console.log(data)
		angular.copy(data,$scope.currentTarget.kpis);
		});
		
	};

	var DialogControllerKPI= function($scope,kpiList,tmpTargetKpis){
		debugger;
		$scope.kpiAllList=kpiList;
		$scope.kpiSelected=tmpTargetKpis;
		
		$scope.saveKpiToTarget=function(){
			  $mdDialog.hide($scope.kpiSelected);
		}
		$scope.close=function(){
			$mdDialog.cancel();
		}
	}
		
}