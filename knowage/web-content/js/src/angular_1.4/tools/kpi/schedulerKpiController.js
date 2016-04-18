var app = angular.module('schedulerKpi', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list','angular_time_picker','ngMessages']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('schedulerKpiController', ['$scope','sbiModule_messaging','sbiModule_config','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','$angularListDetail','$filter','$timeout',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_messaging,sbiModule_config,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,$angularListDetail,$filter,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.selectedScheduler={"crono":{
		"type": "", 
		"parameter": {}
    }};
	$scope.frequency = {type: 'scheduler', value : {}};

	$scope.kpi = [];
	$scope.tmpchrono = {};
	$scope.kpiAllList = [];
	$scope.kpiSelected = [];
	$scope.placeHolder = [];
	$scope.engines = [];
	$scope.selectedWeek = [];
	//$scope.frequency ={value:{'minute':'','hour':'','day':''}};
	$scope.selectedScheduler.crono = {};
	$scope.selectedTab={'tab':0};
	$scope.engineMenuOptionList = [	{
		label : sbiModule_translate.load('sbi.generic.clone'),
		icon:'fa fa-files-o' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			sbiModule_restServices.promiseGet("1.0/kpi",item.id+"/loadSchedulerKPI")
			.then(function(response){ 
				$scope.cloneEngine(response.data);
			},function(response){
			});
		}
	},
	{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.removeEngine(item);
		}
	}];

	
	
	$scope.clearAllData = function(){
		$scope.kpi = [];
		$scope.selectedWeek = [];

		angular.copy({"crono":{
			"type": "", 
			"parameter": {}
	    }},$scope.selectedScheduler);
		angular.copy([],$scope.kpi);
		angular.copy([],$scope.kpiSelected);
		angular.copy({type: 'scheduler', value : {}},$scope.frequency);
	}

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
		if (!($scope.selectedScheduler.kpis == undefined || $scope.selectedScheduler.kpis.length == 0))
		for(var k=0;k<$scope.selectedScheduler.kpis.length;k++){
			var obj = {};
			obj["id"] = $scope.selectedScheduler.kpis[k].id;
			obj["version"] = $scope.selectedScheduler.kpis[k].version;
			arr.push(obj);
			//arr.push($scope.selectedScheduler.kpis[k].name);
		}
		sbiModule_restServices.promisePost("1.0/kpi", 'listPlaceholderByKpi',arr).then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						$scope.showAction(response.data);
					} else {
						angular.copy(response.data,$scope.placeHolder);
						$scope.addPlaceHolderMissing();
					}
				},function(response) {
					$scope.errorHandler(response.data,"");
				})
	}

	$scope.checkFilterParams = function(){
		if($scope.selectedScheduler.filters)
		for(var i=0;i<$scope.selectedScheduler.filters.length;i++){
			if($scope.selectedScheduler.filters[i].value=="" || $scope.selectedScheduler.filters[i].value==null){
				$scope.showAction($scope.translate.load("sbi.schedulerkpi.missingfiltervalue"));
				$timeout(function(){
					$scope.selectedTab.tab=1;
				},0)
			}
		}
	}

	$scope.removeEngine = function(item) {
		sbiModule_restServices.promiseDelete("1.0/kpi", item.id + "/deleteKpiScheduler")
		.then(function(response) {
			for (var i = 0; i < $scope.engines.length; i++) {
				if ($scope.engines[i].id == item.id) {
					$scope.engines.splice(i, 1);
					break;
				}
			}
			alert("Item ID " + item.id + " removal sucess.");
		}, function(response) {
			alert("Removal failed. Item: " + JSON.stringify(item));
		});
	}
	

	$scope.addScheduler= function(cloning){
		if (!cloning){
		angular.copy({},$scope.selectedScheduler);
		angular.copy([],$scope.kpi);
		angular.copy([],$scope.kpiSelected);
		};
		$angularListDetail.goToDetail();
	}
	
	
	$scope.fixDataAfterLoad = function(kpiSched){
		angular.copy(kpiSched,$scope.selectedScheduler);
		$scope.selectedScheduler.startDate = new Date(kpiSched.startDate);
		if (kpiSched.endDate)
			$scope.selectedScheduler.endDate = new Date(kpiSched.endDate);
		$scope.selectedScheduler.crono = JSON.parse(kpiSched.crono);
		$scope.frequency.selectInterval = $scope.selectedScheduler.crono.type;
		if ($scope.frequency.selectInterval == "minute")
			$scope.frequency.value.minute = $scope.selectedScheduler.crono.parameter.numRepetition;
		if ($scope.frequency.selectInterval == "hour")
			$scope.frequency.value.hour = $scope.selectedScheduler.crono.parameter.numRepetition;
		if ($scope.frequency.selectInterval == "day")
			$scope.frequency.value.day = $scope.selectedScheduler.crono.parameter.numRepetition;
		
		
		if ($scope.frequency.selectInterval == "week")
			angular.copy($scope.selectedScheduler.crono.parameter.days,$scope.selectedWeek);
		if ($scope.frequency.selectInterval == "month"){
			$scope.frequency.value.month_repetition=$scope.selectedScheduler.crono.parameter.months;
			$scope.frequency.value.month_week_number_repetition= $scope.selectedScheduler.crono.parameter.weeks;
			$scope.frequency.value.month_week_repetition=$scope.selectedScheduler.crono.parameter.days;
		}
		
		angular.copy(kpiSched.kpis,$scope.selectedScheduler.kpis);
		if($scope.selectedScheduler.kpis.category!=undefined){
			for(var i=0;i<$scope.selectedScheduler.kpis.length;i++){
				$scope.selectedScheduler.kpis[i]["valueCd"] = $scope.selectedScheduler.kpis[i].category.valueCd;
			}
		}
	}
	
	$scope.cloneEngine = function(item) {
		delete item.id;
		for(var i=0;i<item.filters.length;i++){
			delete item.filters[i].executionId;
		}
		
	//	$scope.addPlaceHolderMissing();
		item.name = sbiModule_translate.load("sbi.generic.copyof") + " " + item.name;
		$scope.fixDataAfterLoad(item);
		$scope.addScheduler(true);
	}
	
	

	$scope.addPlaceHolderMissing = function(){
		var keys = Object.keys($scope.placeHolder);
		for(var i=0;i<keys.length;i++){
			if($scope.selectedScheduler.filters!=undefined){
				var index = $scope.indexInList(keys[i],$scope.selectedScheduler.filters,"kpiName");
				var flag = false;
				if(index !=-1){// && ($scope.selectedScheduler.filters[index].value=="" || $scope.selectedScheduler.filters[index].value==null) ){
					{
						var tmp_filter = $scope.selectedScheduler.filters[index];
						$scope.selectedScheduler.filters.splice(index,1);
					}
					flag = true;
				}
				if(index==-1 || flag){

					flag=false;
					var objType = {"domainCode": "KPI_PLACEHOLDER_TYPE",
							"domainName": "KPI placeholder value type",
							"translatedValueDescription": "Fixed Value",
							"translatedValueName": "Fixed Value",
							"valueCd": "FIXED_VALUE",
							"valueDescription": "sbidomains.kpi.fixedvalue",
							"valueId": 355,
							"valueName": "sbidomains.kpi.fixedvalue"
					}
					var array = JSON.parse($scope.placeHolder[keys[i]])
					for(var v=0;v<array.length;v++){
						var obj = {};
						obj.kpiName = keys[i];
						obj.placeholderName = Object.keys(array[v])[0];
						obj.value=array[v][obj.placeholderName];
						obj.type = objType;
						obj.kpiId = tmp_filter.kpiId;
						obj.kpiVersion = tmp_filter.kpiVersion;

						$scope.selectedScheduler.filters.push(obj);
					}
				}	
				$scope.checkMissingType();
			}else{
				$scope.selectedScheduler["filters"]=[];
				var objType = {"domainCode": "KPI_PLACEHOLDER_TYPE",
						"domainName": "KPI placeholder value type",
						"translatedValueDescription": "Fixed Value",
						"translatedValueName": "Fixed Value",
						"valueCd": "FIXED_VALUE",
						"valueDescription": "sbidomains.kpi.fixedvalue",
						"valueId": 355,
						"valueName": "sbidomains.kpi.fixedvalue"
				}
				var array = JSON.parse($scope.placeHolder[keys[i]])
				for(var v=0;v<array.length;v++){
					var obj = {};
					obj.kpiName = keys[i];
					obj.placeholderName = Object.keys(array[v])[0];
					obj.value=array[v][obj.placeholderName];
					obj.type = objType;
					var index2 = $scope.indexInList(keys[i],$scope.kpiSelected,"name");
					obj.kpiId = $scope.kpiSelected[index2].id;
					obj.kpiVersion = $scope.kpiSelected[index2].version;
					

					$scope.selectedScheduler.filters.push(obj);
				}
			}
		}
	}

	$scope.checkMissingType = function(){
		if($scope.selectedScheduler.filters)
		for(var i=0;i<$scope.selectedScheduler.filters.length;i++){
			if($scope.selectedScheduler.filters[i].type==null){
				var objType = {"domainCode": "KPI_PLACEHOLDER_TYPE",
						"domainName": "KPI placeholder value type",
						"translatedValueDescription": "Fixed Value",
						"translatedValueName": "Fixed Value",
						"valueCd": "FIXED_VALUE",
						"valueDescription": "sbidomains.kpi.fixedvalue",
						"valueId": 355,
						"valueName": "sbidomains.kpi.fixedvalue"
				}
				$scope.selectedScheduler.filters[i].type = objType;
			}
		}
	}

	$scope.indexInList=function(item, list,param) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object[param]==item){
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
		$timeout(function(){
			$scope.selectedTab.tab=0;
		},0);
		$scope.clearAllData();
		$angularListDetail.goToList();
	}
	
	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	}

	$scope.validateScheduler = function(){
		if ($scope.selectedScheduler.delta == undefined){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.execute.value")).position('top').action('OK').highlightAction(true));
			return false;
		}
		
		if ($scope.selectedScheduler.startDate == undefined){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.start.time")).position('top').action('OK').highlightAction(true));
			return false;
		}
			
		if ($scope.frequency.selectInterval == undefined){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.repeat.interval")).position('top').action('OK').highlightAction(true));
			return false;
		}
		
		if ($scope.selectedScheduler.kpis == undefined || $scope.selectedScheduler.kpis.length == 0){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.kpi.list")).position('top').action('OK').highlightAction(true));
			return false;
		}
		
		if ($scope.selectedScheduler.crono.type == 'week'){
			if ($scope.selectedScheduler.crono.parameter.days.length == 0){
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kpi.scheduler.array.days.length.error")).position('top').action('OK').highlightAction(true));
			return false; 
			}
		}
		
		if ($scope.selectedScheduler.crono.type == 'month'){
			if ($scope.selectedScheduler.crono.parameter.days.length == 0){
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kpi.scheduler.array.days.length.error")).position('top').action('OK').highlightAction(true));
			return false; 
			}
			
			if ($scope.selectedScheduler.crono.parameter.months.length == 0){
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kpi.scheduler.array.months.length.error")).position('top').action('OK').highlightAction(true));
			return false; 
			}
			
			if (!$scope.selectedScheduler.crono.parameter.weeks){
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kpi.scheduler.array.week.length.error")).position('top').action('OK').highlightAction(true));
			return false; 
			}
		}
		
		$scope.selectedScheduler.startDate = new Date($scope.selectedScheduler.startDate).getTime();
		
		if ($scope.selectedScheduler.endDate != undefined)
			$scope.selectedScheduler.endDate = new Date($scope.selectedScheduler.endDate).getTime();
		
		return true;
	};
	
	$scope.saveSc=function(){
		if ($scope.validateScheduler()){
			$scope.showSaveGUI().then(function(response){
				{}
			$timeout(function(){
				$scope.selectedTab.tab=2;
			},0)
			if($scope.activeSave=="add"){
			}else{
			}
	
			});
		}
	}
	
	$scope.showSaveGUI= function(){
		var deferred = $q.defer();

		$mdDialog.show({
			controller: DialogControllerKPIScheduler,
			templateUrl: 'templatesaveKPIScheduler.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {selectedScheduler: $scope.selectedScheduler, sbiModule_restServices: sbiModule_restServices, sbiModule_messaging: sbiModule_messaging , loadEngineKpi:$scope.loadEngineKpi, $mdToast, sbiModule_translate, engines:$scope.engines}
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return ;
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	}
	
	$scope.tableColumn=[
	                    {label:"Name",name:"name"},
	                    {label:"KPI",name:"kpiNames"},
	                    {label:"Start Date",name:"startDate"},
	                    {label:"End Date",name:"endDate",comparatorFunction:function(a,b){return 1}},
	                    {label:"Author",name:"author"}]
	
}


function DialogControllerKPIScheduler($scope,$mdDialog,selectedScheduler, sbiModule_restServices, sbiModule_messaging,loadEngineKpi, $mdToast, sbiModule_translate, engines){
$scope.selectedScheduler = selectedScheduler;
$scope.sbiModule_restServices = sbiModule_restServices;
$scope.sbiModule_messaging = sbiModule_messaging;
	$scope.close = function(){
		$mdDialog.cancel();

	}
	
	$scope.apply = function(){
		$scope.saveScheduler();
	}
	
	$scope.saveScheduler = function(){
		$scope.saveSchedulerOnDataB();
		$mdDialog.cancel();
	};
	
	$scope.saveSchedulerOnDataB = function(){
		if ($scope.selectedScheduler.name.length > 40){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.save.name.toolong")).position('top').action('OK').highlightAction(true));
			return;
			}
		$scope.tmpchrono = $scope.selectedScheduler.crono;
		var str_chrono = JSON.stringify($scope.selectedScheduler.crono);
		$scope.selectedScheduler.crono = str_chrono;
		var jsondata = angular.toJson($scope.selectedScheduler);
		$scope.sbiModule_restServices.promisePost("1.0/kpi","saveSchedulerKPI",jsondata)
		.then(
		function(response) {
			loadEngineKpi();
			$scope.selectedScheduler.crono = $scope.tmpchrono;
			$scope.selectedScheduler.id = response.data.id;
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.glossary.success.save")).position('top').action('OK').highlightAction(true));
		},
		function(response){
			$scope.selectedScheduler.crono = $scope.tmpchrono;
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.glossary.error.save")).position('top').action('OK').highlightAction(true));

		});	
	}
}