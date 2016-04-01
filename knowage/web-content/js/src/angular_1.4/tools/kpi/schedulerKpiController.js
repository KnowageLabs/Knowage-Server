var app = angular.module('schedulerKpi', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list','angular_time_picker','ngMessages']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('schedulerKpiController', ['$scope','sbiModule_config','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','$angularListDetail','$filter','$timeout',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_config,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,$angularListDetail,$filter,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.selectedScheduler={};
	$scope.kpi = [];
	$scope.kpiAllList = [];
	$scope.kpiSelected = [];
	$scope.placeHolder = [];
	//retry it after with a service rest

	$scope.engines = [];



	$scope.loadEngineKpi = function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listSchedulerKPI")
		.then(function(response){ 
			angular.copy(response.data,$scope.engines);
			for(var i=0;i<$scope.engines.length;i++){
				if($scope.engines[i].endDate!=null && $scope.engines[i].endDate!=undefined){
					var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
					//parse date based on language selected
					 $scope.engines[i].endDate=$filter('date')( $scope.engines[i].endDate, dateFormat);
				}
				if($scope.engines[i].startDate!=null && $scope.engines[i].startDate!=undefined){
					var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
					//parse date based on language selected
					 $scope.engines[i].startDate=$filter('date')( $scope.engines[i].startDate, dateFormat);
				}
			}
		},function(response){
		});

	}
	
	$scope.loadEngineKpi();
	
	$scope.getListKPI = function(){
		var arr_name = [];
		sbiModule_restServices.promiseGet("1.0/kpi","listKpi")
		.then(function(response){ 
			for(var i=0;i<response.data.length;i++){

				var obj = {};
				obj["name"]=response.data[i].name;
				obj["version"]=response.data[i].version;
				if(response.data[i].category!=undefined){
					obj["valueCd"] = response.data[i].category.valueCd;
				}
				obj["author"]=response.data[i].author;
				obj["datacreation"]=new Date(response.data[i].dateCreation);
				obj["id"]=response.data[i].id;

				$scope.kpiAllList.push(obj);

			}
		},function(response){
		});
	}
	$scope.getListKPI();

	
	$scope.loadAllInformationForKpi  = function(){
	
		var arr = [];
		
		for(var k=0;k<$scope.selectedScheduler.kpis.length;k++){
			arr.push($scope.selectedScheduler.kpis[k].name);
		}
		console.log(arr);
		
		sbiModule_restServices.promisePost("1.0/kpi", 'listPlaceholderByKpi',arr).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
					console.log("wow",response.data);
					}

				},function(response) {
					$scope.errorHandler(response.data,"");
				})
		

		

	}
	
	
	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
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

	$scope.cancel = function(){
		$angularListDetail.goToList();
	}

	$scope.tableColumn=[
	                    {label:"Name",name:"name"},
	                    {label:"KPI",name:"kpiNames"},
	                    {label:"Start Date",name:"startDate"},
	                    {label:"End Date",name:"endDate",comparatorFunction:function(a,b){return 1}},
	                    {label:"Author",name:"author"}]
}






