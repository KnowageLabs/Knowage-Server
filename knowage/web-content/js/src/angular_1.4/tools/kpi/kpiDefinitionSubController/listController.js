var app = angular.module('kpiDefinitionManager').controller('listController', ['$scope','$filter','sbiModule_config','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionListControllerFunction ]);

function KPIDefinitionListControllerFunction($scope,$filter,sbiModule_config,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	$scope.currentKPI ={
			"formula": ""
	}

	$scope.addKpi= function(){
		$scope.flagLoaded = true;
		angular.copy($scope.emptyKpi,$scope.kpi);
		$scope.kpi.threshold.typeId=$scope.thresholdTypeList[0].valueId;
		$timeout(function(){
			$scope.selectedTab.tab=0;
		},0)

		$angularListDetail.goToDetail();
		$scope.flagActivateBrother('addEvent');

	}
	$scope.loadKPI=function(item){


		sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/"+item.version+"/loadKpi")
		.then(function(response){ 

			angular.copy({},$scope.cardinality);
			$timeout(function(){
				$scope.selectedTab.tab=0;
			},0)

			angular.copy(response.data,$scope.kpi); 
			$scope.flagActivateBrother('loadedEvent');

		},function(response){
		});

	}
	$scope.$on('savedEvent',function(e){
		$scope.kpiList=[];
		$scope.kpiListOriginal=[];
		$angularListDetail.goToList();
		$scope.getListKPI();
	});
	$scope.$on('cancelEvent', function(e) {  
		$angularListDetail.goToList();
	});
	$scope.$on('deleteKpiEvent', function(e) { 
		$scope.kpiList=[];
		$scope.kpiListOriginal=[];
		$scope.getListKPI();
		$angularListDetail.goToList();
	});
	
	$scope.getListKPI = function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listKpi")
		.then(function(response){ 
			angular.copy(response.data,$scope.kpiListOriginal);
			for(var i=0;i<response.data.length;i++){
				var obj = {};
				obj["name"]=response.data[i].name;
				obj["version"]=response.data[i].version;
				if(response.data[i].category!=undefined){
					obj["valueCd"] = response.data[i].category.valueCd;
				}
				obj["author"]=response.data[i].author;
				var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
				obj["id"]=response.data[i].id;
				$scope.kpiList.push(obj);
			}
		},function(response){
		});
	};
	$scope.getListKPI();

	
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
	
	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
}