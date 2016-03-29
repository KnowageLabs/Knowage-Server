var app = angular.module('schedulerKpi', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list','angular_time_picker','ngMessages']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('schedulerKpiController', ['$scope','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','$angularListDetail',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,$angularListDetail){
	$scope.translate=sbiModule_translate;
	$scope.selectedScheduler={};
	$scope.kpi = [];
	$scope.kpiAllList = [];
	//retry it after with a service rest

	$scope.engines = [
	                  {"name":"kpi1_Calcolo",
	                	  "kpi":[],
	                	  "startDate": "03/03/2015",
	                	  "endDate": "03/03/2015",
	                	  "author":"biadmin"
	                  },
	                  {"name":"kpi1_Calcolo2",
	                	  "kpi":[],
	                	  "startDate": "03/03/2015",
	                	  "endDate": "03/03/2015",
	                	  "author":"biadmin"
	                  },
	                  {"name":"kpi1_Calcolo3",
	                	  "kpi":[],
	                	  "startDate": "03/03/2015",
	                	  "endDate": "03/03/2015",
	                	  "author":"biadmin"
	                  }
	                  ];




	$scope.getListKPI = function(){
		var arr = [];
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
				console.log("ALLKPI",$scope.kpiAllList);

				if(i!=2){
					arr.push(obj);
					arr_name.push(response.data[i].name);
				}
			}

			for(var i=0; i<$scope.kpiAllList.length;i++){

				$scope.kpiAllList[i].icon = '<md-button ng-if="scopeFunctions.exists(row)" class="md-icon-button" > <md-icon md-font-icon="fa fa-check" ng-click="scopeFunctions.addKpi(row,$event)" style=" margin-top: 6px ; color: #153E7E;"></md-icon> </md-button>';

			}
			angular.copy(arr,$scope.engines[0].kpi );
			angular.copy(arr,$scope.engines[1].kpi );
			angular.copy(arr,$scope.engines[2].kpi );
			console.log("enginesKPI",$scope.engines);
			//angular.copy(response.data,$scope.kpiAllList);
			$scope.engines[0].kpiName = arr_name;
			$scope.engines[1].kpiName = arr_name;
			$scope.engines[2].kpiName = arr_name;
			console.log($scope.engines);

		},function(response){
		});
	}
	$scope.getListKPI();



	$scope.cancel = function(){
		$angularListDetail.goToList();
	}
}






