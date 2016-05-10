var app = angular.module('schedulerKpi', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror','color.picker','angular_list','angular_time_picker','ngMessages','cron_frequency']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('schedulerKpiController', ['$scope','sbiModule_messaging','sbiModule_config','sbiModule_translate', 'sbiModule_restServices','$mdDialog','$q','$mdToast','$timeout','$angularListDetail','$filter','$timeout','$cronFrequency',kpiTargetControllerFunction ]);

function kpiTargetControllerFunction($scope,sbiModule_messaging,sbiModule_config,sbiModule_translate,sbiModule_restServices,$mdDialog,$q,$mdToast,$timeout,$angularListDetail,$filter,$timeout,$cronFrequency){
	$scope.translate=sbiModule_translate;
	$scope.selectedScheduler={"frequency":{}};
	$scope.frequency = {type: 'scheduler', value : {}};
	$scope.isValidCronFrequency={"status":true};
	$scope.kpi = [];
	$scope.tmpchrono = {};
	$scope.kpiAllList = [];
	$scope.kpiSelected = [];
	$scope.placeHolder = [];
	$scope.engines = [];
	$scope.selectedWeek = [];
	$scope.listType = [];
	$scope.funcTemporal = [];
	$scope.lov = [];
	

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
			$scope.deleteMeasure(item);
		}
	},
	{ 
		label: function(row){
			return angular.equals(row.jobStatus.toUpperCase(),"SUSPENDED") ? sbiModule_translate.load('sbi.alert.resume') : sbiModule_translate.load('sbi.alert.suspend');
		},
		icon: function(row){
			return angular.equals(row.jobStatus.toUpperCase(),"SUSPENDED") ? 'fa fa-play' : 'fa fa-pause';
		}, 
		backgroundColor:'transparent',
		action : function(item,event) { 
			var data="?jobGroup=KPI_SCHEDULER_GROUP&triggerGroup=KPI_SCHEDULER_GROUP&jobName="+item.id+"&triggerName="+item.id; 
			sbiModule_restServices.promisePost("scheduler",(angular.equals(item.jobStatus.toUpperCase(),"SUSPENDED") ? 'resumeTrigger' : 'pauseTrigger')+""+data)
			.then(function(response){   
				item.jobStatus=angular.equals(item.jobStatus.toUpperCase(),"SUSPENDED") ? 'ACTIVE' : 'SUSPENDED';
			},function(response){
				sbiModule_restServices.errorHandler(response.data,sbiModule_translate.load("sbi.generic.deletingItemError"))});
			}
	
		}
	];
	$scope.deleteMeasure=function(item,event){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.kpi.measure.delete.title"))
		.content($scope.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete scheduler') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {


			$scope.removeEngine(item);


		}, function() {
		});
	}
	
	
	$scope.clearAllData = function(){
		$scope.kpi = [];
		$scope.selectedWeek = [];
		
		angular.copy({"frequency":{}},$scope.selectedScheduler);
		angular.copy([],$scope.kpi);
		angular.copy([],$scope.kpiSelected);
		angular.copy({type: 'scheduler', value : {}},$scope.frequency);
	}

	$scope.loadEngineKpi = function(){
		sbiModule_restServices.promiseGet("1.0/kpi","listSchedulerKPI")
		.then(function(response){ 
			angular.copy(response.data,$scope.engines);
			for(var i=0;i<$scope.engines.length;i++){
				if($scope.engines[i].frequency.endDate!=null && $scope.engines[i].frequency.endDate!=undefined){
					var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
					//parse date based on language selected
					$scope.engines[i].frequency.endDate=$filter('date')( $scope.engines[i].frequency.endDate, dateFormat);
				}
				if($scope.engines[i].frequency.startDate!=null && $scope.engines[i].frequency.startDate!=undefined){
					var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
					//parse date based on language selected
					$scope.engines[i].frequency.startDate=$filter('date')( $scope.engines[i].frequency.startDate, dateFormat);
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
		
		}, function(response) {
			
		});
	}
	

	$scope.addScheduler= function(cloning){
		if (!cloning){
			
		angular.copy({"frequency":{"cron": {"type":"minute","parameter":{"numRepetition":"1"}}}},$scope.selectedScheduler);
		angular.copy([],$scope.kpi);
		angular.copy([],$scope.kpiSelected);
		};
		$angularListDetail.goToDetail();
	}
	
	
	$scope.fixDataAfterLoad = function(kpiSched){
		angular.copy(kpiSched,$scope.selectedScheduler);
		
		$scope.selectedScheduler.frequency.cron = JSON.parse(kpiSched.frequency.cron);
		angular.copy(kpiSched.kpis,$scope.selectedScheduler.kpis);
		if($scope.selectedScheduler.kpis.category!=undefined){
			for(var i=0;i<$scope.selectedScheduler.kpis.length;i++){
				$scope.selectedScheduler.kpis[i]["valueCd"] = $scope.selectedScheduler.kpis[i].category.valueCd;
			}
		}
	}
	
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
	$scope.cloneEngine = function(item) {
		delete item.id;
		for(var i=0;i<item.filters.length;i++){
			delete item.filters[i].executionId;
		}

		item.name = sbiModule_translate.load("sbi.generic.copyof") + " " + item.name;
		$scope.fixDataAfterLoad(item);
		$scope.addScheduler(true);
	}
	
	

	$scope.addPlaceHolderMissing = function(){
		var keys = Object.keys($scope.placeHolder);
		for(var i=0;i<keys.length;i++){
			if($scope.selectedScheduler.filters!=undefined){
				var index = $scope.indexInList(keys[i],$scope.selectedScheduler.filters,"kpiName");
				
				if(index !=-1){
						

				}else{
					var objType = {"valueCd":"FIXED_VALUE","valueId":355};
					var array = JSON.parse($scope.placeHolder[keys[i]])
					for(var v=0;v<array.length;v++){
						var obj = {};
						obj.kpiName = keys[i];
						obj.placeholderName = Object.keys(array[v])[0];
						obj.value=array[v][obj.placeholderName];
						obj.type = objType;
						var index2 = $scope.indexInList(keys[i],$scope.kpiAllList,"name");
						obj.kpiId = $scope.kpiAllList[index2].id;
						obj.kpiVersion = $scope.kpiAllList[index2].version;

						$scope.selectedScheduler.filters.push(obj);
					}
				}	
				$scope.checkMissingType();
			}else{
				$scope.selectedScheduler["filters"]=[];
				var objType = {"valueCd":"FIXED_VALUE","valueId":355};
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
				var objType = {"valueCd":"FIXED_VALUE","valueId":355};
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
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.execute.value")).position('top').action('OK'));
			return false;
		}
		
		
		if ($scope.selectedScheduler.kpis == undefined || $scope.selectedScheduler.kpis.length == 0){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.missing.kpi.list")).position('top').action('OK'));
			return false;
		}
		
		if($scope.isValidCronFrequency.status==false){
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.wrong.cron.interval")).position('top').action('OK'));
			return false;	
		}
		
		return true;
	};
	
	$scope.saveSc=function(){
		if ($scope.validateScheduler() && $scope.completeDomain() && $scope.checkFiltersValue()){
			
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
	
	
	$scope.completeDomain = function(){
		for(var i=0;i<$scope.selectedScheduler.filters.length;i++){
			var index = $scope.indexInList($scope.selectedScheduler.filters[i].type.valueCd,$scope.listType,"VALUE_CD");
			
			if(index!=-1){
				var obj = $scope.listType[index];
				$scope.selectedScheduler.filters[i].type.valueId = obj["VALUE_ID"]
				
			}
		}
		return true;
	}
	
	$scope.checkFiltersValue = function(){
		for(var i=0;i<$scope.selectedScheduler.filters.length;i++){
			if($scope.selectedScheduler.filters[i].value==undefined || $scope.selectedScheduler.filters[i].value.trim() == ""){
				$scope.showAction($scope.translate.load("sbi.schedulerkpi.missingfiltervalue"));
				return false
			}
			
		}
		return true;
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
		$scope.getNameForBar = function(){
		return $scope.selectedScheduler.name != undefined ? $scope.selectedScheduler.name : $scope.translate.load('sbi.kpi.skeduler.new');
	}
	
	$scope.showSaveGUI= function(){
		var deferred = $q.defer();

		$mdDialog.show({
			controller: DialogControllerKPIScheduler,
			templateUrl: 'templatesaveKPIScheduler.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {selectedScheduler: $scope.selectedScheduler, sbiModule_restServices: sbiModule_restServices, sbiModule_messaging: sbiModule_messaging , loadEngineKpi:$scope.loadEngineKpi, $mdToast, translate:sbiModule_translate, engines:$scope.engines,cron : $cronFrequency}
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
	                    {label:"Start Date",name:"frequency.startDate"},
	                    {label:"End Date",name:"frequency.endDate",comparatorFunction:function(a,b){return 1}},
	                    {label:"Author",name:"author"},
	                    {label:"Status",name:"jobStatus"}]
	
}


function DialogControllerKPIScheduler($scope,$mdDialog,selectedScheduler, sbiModule_restServices, sbiModule_messaging,loadEngineKpi, $mdToast, translate, engines,cron){
$scope.selectedScheduler = selectedScheduler;
$scope.translate = translate;
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
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load("sbi.kbi.scheduler.error.save.name.toolong")).position('top').action('OK'));
			return;
			}
		var tmpScheduler = {};
		angular.copy($scope.selectedScheduler, tmpScheduler);
		
		cron.parseForBackend(tmpScheduler.frequency);
		
		sbiModule_restServices.promisePost("1.0/kpi","saveSchedulerKPI",tmpScheduler)
		.then(
		function(response) {
			loadEngineKpi();
			$scope.selectedScheduler.id = response.data.id;
			$mdToast.show($mdToast.simple().content(translate.load("sbi.glossary.success.save")).position('top').action('OK'));
		},
		function(response){
			sbiModule_restServices.errorHandler(response.data,translate.load("sbi.glossary.error.save"));

		});	
	}
}