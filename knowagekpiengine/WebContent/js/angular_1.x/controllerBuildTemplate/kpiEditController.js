var app = angular.module('templateBuild', [ 'ngMaterial', 'angular_table' ,'sbiModule', 'expander-box','dinamic-list','kpi-style']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('templateBuildController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$timeout','sbiModule_config','$httpParamSerializer',templateBuildControllerFunction ]);

function templateBuildControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$timeout,sbiModule_config,$httpParamSerializer){
	$scope.translate=sbiModule_translate;
	$scope.addKpis = [];
	$scope.typeDocument = "widget";
	$scope.style = {};
	$scope.options = {"showvalue": true, "showtarget":true, "showtargetpercentage":false,"showlineargauge":true, "showthreshold":true,"vieweas":"Speedometer"};
	$scope.units = ['day', 'week', 'month', 'quarter', 'year'];
	$scope.typeOfWiew = ['Speedometer','Semaphore','Kpi Card'];
	$scope.style.color = "rgb(14, 13, 13)";
	
	$scope.selectedKpis = [];
	$scope.selectedKpi = [];
	$scope.kpiList = [];
	
	$scope.saveTemplate = function(){
		var obj = $scope.createJSONFromInfo();
		console.log(obj);
	
		var formData = new FormData();
		formData.append("jsonTemplate",  JSON.stringify(obj));
		formData.append("docLabel",sbiModule_config.docLabel);
	
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );

		sbiModule_restServices.promisePost("1.0/documents", 'saveChartTemplate', 
				$httpParamSerializer({jsonTemplate:JSON.stringify(obj), docLabel:sbiModule_config.docLabel}), {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).then(
				function(response) {
				
					console.log(response.data);
					$scope.showAction("Template saved");
				},function(response) {
					$scope.errorHandler(response.data,"");
				});
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

	$scope.loadAllKpis = function(){
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
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
				//var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				//obj["datacreation"]=$filter('date')(response.data[i].dateCreation, dateFormat);
				obj["id"]=response.data[i].id;
				$scope.kpiList.push(obj);
			}
		},function(response){
		});
	}
	$scope.loadAllKpis();
	
	$scope.loadTemplateIfExist = function(){
		var obj={"id": sbiModule_config.docLabel};
		sbiModule_restServices.promisePost("1.0/kpisTemplate", 'getKpiTemplate',obj).then(
				function(response) {
				
					var template = response.data;
					if(template!=undefined){
						
						$scope.typeDocument = template.chart.model;
						if($scope.typeDocument=="list"){
							//mode list
							$scope.selectedKpis = template.chart.data.kpi;
						}else{
							//mode widget
							$scope.selectedKpi.push(template.chart.data.kpi);
						}
						
						if(template.chart.style!=undefined){
							$scope.style = template.chart.style.font;
						}
						if(template.chart.options!=undefined){
							if(template.chart.options.showlineargauge=="true"){
								$scope.options.showlineargauge=true;
							}else{
								$scope.options.showlineargauge=false;
							}
							if(template.chart.options.showtarget=="true"){
								$scope.options.showtarget=true;
							}else{
								$scope.options.showtarget=false;
							}
							if(template.chart.options.showtargetpercentage=="true"){
								$scope.options.showtargetpercentage=true;
							}else{
								$scope.options.showtargetpercentage=false;
							}
							if(template.chart.options.showthreshold=="true"){
								$scope.options.showthreshold=true;
							}else{
								$scope.options.showthreshold=false;
							}
							if(template.chart.options.showvalue=="true"){
								$scope.options.showvalue=true;
							}else{
								$scope.options.showvalue=false;
							}		
					
							$scope.options.vieweas = template.chart.options.vieweas
						}
						
						if($scope.options.history!=undefined){
							$scope.options.history.size = parseInt($scope.options.history.size);
							$scope.options.history.units = $scope.options.history.units;
						}
						$scope.completeInfoKPI();
				}
				},function(response) {
					$scope.errorHandler(response.data,"");
				});
	}
	
	$scope.loadTemplateIfExist();
	$scope.completeInfoKPI = function(){
		var arr= [];
		var flagList = false;
		for(var i=0;i<$scope.selectedKpis.length;i++){
			flagList = true;
			var index = $scope.indexInList($scope.selectedKpis[i],$scope.kpiList);
			if(index !=-1){
				arr.push($scope.kpiList[index]);
			}
		}
		for(var j=0;j<$scope.selectedKpi.length;j++){
			var index = $scope.indexInList($scope.selectedKpi[j],$scope.kpiList);
			if(index !=-1){
				arr.push($scope.kpiList[index]);
			}
		}
		if(flagList){
			angular.copy(arr,$scope.selectedKpis);
		}else{
			angular.copy(arr,$scope.selectedKpi);
		}
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
	
	$scope.createJSONFromInfo = function(){
		var obj = {};
		obj["chart"] = {};
		obj.chart["type"]="kpi";
		obj.chart["model"]=$scope.typeDocument;
		obj.chart["data"]={};
		if($scope.typeDocument=="widget"){
			var kpiObject = {};
			kpiObject["id"] = $scope.selectedKpi[0].id;
			kpiObject["version"] =  $scope.selectedKpi[0].version;
			obj.chart.data["kpi"]=kpiObject;
		}else{
			var arr=[];
			for(var i=0;i<$scope.selectedKpis.length;i++){
				var kpiObject = {};
				kpiObject["id"] = $scope.selectedKpis[i].id;
				kpiObject["version"] =  $scope.selectedKpis[i].version;
				arr.push(kpiObject);
			}
			obj.chart.data["kpi"]=arr;
		}
		
		obj.chart["options"] = $scope.options;
	
		obj.chart["style"] ={};
		obj.chart.style["font"] = $scope.style;
		
		return obj;
	}
}