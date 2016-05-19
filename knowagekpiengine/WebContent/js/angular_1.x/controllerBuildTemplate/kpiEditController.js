(function() {

var app = angular.module('templateBuild', [ 'ngMaterial', 'angular_table', 'sbiModule', 'expander-box','dinamic-list','kpi-style']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('templateBuildController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$timeout','sbiModule_config','$httpParamSerializer','$filter',templateBuildControllerFunction ]);

function templateBuildControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$timeout,sbiModule_config,$httpParamSerializer,$filter){
	$scope.translate=sbiModule_translate;
	$scope.addKpis = [];
	$scope.typeDocument = 'widget';
	$scope.style = {};
	$scope.options = {"showvalue": true, "showtarget":true, "showtargetpercentage":false,"showlineargauge":true, "showthreshold":true,"vieweas":"Speedometer"};
	$scope.options.history = {"units": 'month',"size": 1};
	$scope.units = ['day', 'week', 'month', 'quarter', 'year'];
	$scope.typeOfWiew = [{'label':'speedometer','value':'Speedometer'},{'label':'kpicard','value':'Kpi Card'}];
	$scope.style.color = "rgb(14, 13, 13)";
	$scope.typeChart = 'kpi';
	$scope.selectedKpis = [];
	$scope.kpiList = [];
	$scope.scorecardSelected =[];
	$scope.allScorecard = [];

	$scope.tableFunction={
			
			translate:sbiModule_translate,
					
			loadListScorecard: function(item,evt){
				var promise =$scope.loadListScorecard();

				promise.then(function(result){		
					angular.copy([result],$scope.scorecardSelected);

				});
			},
			vieweAs: [{'label':'speedometer','value':'Speedometer'},{'label':'kpicard','value':'Kpi Card'}]
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
	
	$scope.loadListScorecardDialog = function() {
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
		sbiModule_restServices.promiseGet("1.0/kpi","listScorecard")
		.then(function(response){ 

			for(var i=0;i<response.data.length;i++){
				var obj = {};
				obj["name"]=response.data[i].name;
				obj["id"] = response.data[i].id;
				var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
				//parse date based on language selected
				obj["creationDate"]=$filter('date')( response.data[i].creationDate, dateFormat);
				obj["author"]=response.data[i].author;


				$scope.allScorecard.push(obj);
			}
		});
	};

	$scope.loadListScorecardDialog();
	
	$scope.loadListScorecard = function(){
		var deferred = $q.defer();

		$mdDialog.show({
			controller: DialogControllerScorecard,
			templateUrl: '/knowagekpiengine/js/angular_1.x/controllerBuildTemplate/templateScorecard/templateScorecardDialog.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {items: deferred,allScorecard:$scope.allScorecard, scorecardSelected:$scope.scorecardSelected}
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return deferred.promise;
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	};

	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.removeScorecard();
		}
	}];
	
	$scope.removeScorecard = function(){
		$scope.scorecardSelected =[];
	};
	
	$scope.saveTemplate = function(){
		var obj = $scope.createJSONFromInfo();
		
		if(obj==null){
			$scope.showAction(sbiModule_translate.load('sbi.kpidocumentdesigner.errorrange'));
			return;
		}

		var formData = new FormData();
		formData.append("jsonTemplate",  JSON.stringify(obj));
		formData.append("docLabel",sbiModule_config.docLabel);

		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );

		sbiModule_restServices.promisePost("1.0/documents", 'saveKpiTemplate', 
				$httpParamSerializer({jsonTemplate:JSON.stringify(obj), docLabel:sbiModule_config.docLabel}), {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).then(
						function(response) {
//							console.log(response.data);

//							$scope.showAction("Template saved");
							var saveSuccessMsg = sbiModule_translate.load("sbi.kpidocumentdesigner.save.success");
							$scope.showAction(saveSuccessMsg);
						},function(response) {
							$scope.errorHandler(response.data,"");
						});
	};
	
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
	};

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
				obj["id"]=response.data[i].id;
				obj["vieweAsList"] ='<md-select ng-model="row.vieweAs" class="noMargin">'
					+'<md-option value=""></md-option>'
					+'<md-option ng-repeat="sev in scopeFunctions.vieweAs" value="{{sev.label}}">'
					+'{{sev.value}}'
					+' </md-option>'
					+'</md-select>';
				obj["rangeMinValueHTML"]= '<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}">Range Min Value</label>'
					+'<input required type="number" name="min" ng-model="row.rangeMinValue" />'
					+'</md-input-container>';
				obj["rangeMaxValueHTML"]='<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}" >Range Max Value</label>'
					+'<input required type="number" name="max" ng-model="row.rangeMaxValue" />'
					+'</md-input-container>';
				$scope.kpiList.push(obj);
			}
			
			$scope.loadTemplateIfExist();
		},function(response){
		});
	};
	
	$scope.loadAllKpis();

	$scope.loadTemplateIfExist = function(){
		var obj={"id": sbiModule_config.docLabel};
		
		sbiModule_restServices.promisePost("1.0/kpisTemplate", 'getKpiTemplate',obj).then(
				function(response) {

					var template = response.data;
					if(template!=undefined){
						$scope.typeChart = template.chart.type;

						if($scope.typeChart=='kpi'){
							$scope.loadKpiTemplate(template);
						}else{
							$scope.loadScorecardTemplate(template);
						}
					}

				}, function(response) {
					console.log("No template");
				});
	};

	$scope.loadScorecardTemplate = function(template) {
		if(template.chart.style!=undefined){
			$scope.style = template.chart.style.font;
		}
		var idScorecard = template.chart.data.scorecard.id;
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
		sbiModule_restServices.promiseGet("1.0/kpi",idScorecard+"/loadScorecard")
		.then(function(response){
			var obj = {};
			obj["name"]=response.data.name;
			obj["id"] = response.data.id;
			var dateFormat = $scope.parseDate(sbiModule_config.localizedDateFormat);
			//parse date based on language selected
			obj["creationDate"]=$filter('date')( response.data.creationDate, dateFormat);
			obj["author"]=response.data.author;

			$scope.scorecardSelected.push(obj);

		},function(response){
		});
	};
	
	$scope.loadKpiTemplate = function(template) {
		$scope.typeDocument = template.chart.model;

		if(Array.isArray(template.chart.data.kpi)){
			$scope.selectedKpis = template.chart.data.kpi;
		}else{
			$scope.selectedKpis.push(template.chart.data.kpi);
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

			//$scope.options.vieweas = template.chart.options.vieweas
		}

		if(template.chart.options.history!=undefined){
			$scope.options.history = {};
			$scope.options.history.size = parseInt(template.chart.options.history.size);
			$scope.options.history.units = template.chart.options.history.units;
		}
		$scope.completeInfoKPI();
	};

	

	$scope.completeInfoKPI = function() {
		var arr= [];
		var flagList = false;
		for(var i=0;i<$scope.selectedKpis.length;i++){

			var index = $scope.indexInList($scope.selectedKpis[i],$scope.kpiList);
			if(index !=-1){
				var obj = {};
				obj["name"]=$scope.kpiList[index].name;
				obj["version"]=$scope.kpiList[index].version;

				obj["valueCd"] = $scope.kpiList[index].valueCd;

				obj["author"]=$scope.kpiList[index].author;
				obj["id"]=$scope.kpiList[index].id;
				obj["vieweAs"]= $scope.selectedKpis[i].vieweas;
				obj["vieweAsList"] ='<md-select ng-model="row.vieweAs" class="noMargin">'
					+'<md-option value=""></md-option>'
					+'<md-option ng-repeat="sev in scopeFunctions.vieweAs" value="{{sev.label}}">'
					+'{{sev.value}}'
					+' </md-option>'
					+'</md-select>';
				obj["rangeMinValueHTML"]= '<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}">Range Min Value</label>'
					+'<input required type="number" name="min" ng-model="row.rangeMinValue" />'
					+'</md-input-container>';
				obj["rangeMaxValueHTML"]='<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}" >Range Max Value</label>'
					+'<input required type="number" name="max" ng-model="row.rangeMaxValue" />'
					+'</md-input-container>';
				obj["rangeMinValue"]= parseFloat($scope.selectedKpis[i].rangeMinValue);
				obj["rangeMaxValue"]= parseFloat($scope.selectedKpis[i].rangeMaxValue);
				arr.push(obj);
			}
		}

		angular.copy(arr,$scope.selectedKpis);
	};

	$scope.indexInList = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};

	$scope.createJSONFromInfo = function() {
		var obj = {};
		obj["chart"] = {};
		obj.chart["type"]=$scope.typeChart;
		obj.chart["data"]={};
		if($scope.typeChart=="kpi"){
			obj.chart["model"]=$scope.typeDocument;
			var arr=[];
			for(var i=0;i<$scope.selectedKpis.length;i++){
				var kpiObject = {};
				kpiObject["id"] = $scope.selectedKpis[i].id;
				kpiObject["version"] =  $scope.selectedKpis[i].version;
				kpiObject["vieweas"] = $scope.selectedKpis[i].vieweAs;
				if($scope.selectedKpis[i].rangeMinValue>=$scope.selectedKpis[i].rangeMaxValue || isNaN($scope.selectedKpis[i].rangeMinValue) || isNaN($scope.selectedKpis[i].rangeMaxValue)){
					return null;
				}
				kpiObject["rangeMinValue"] = $scope.selectedKpis[i].rangeMinValue;
				kpiObject["rangeMaxValue"] = $scope.selectedKpis[i].rangeMaxValue;
				arr.push(kpiObject);
			}
			obj.chart.data["kpi"]=arr;

			obj.chart["options"] = $scope.options;
		}else{
			var scoreObject = {};
			scoreObject["id"] = $scope.scorecardSelected[0].id;
			obj.chart.data["scorecard"]=scoreObject;
		}
		obj.chart["style"] ={};
		obj.chart.style["font"] = $scope.style;

		return obj;
	};
};

function DialogControllerScorecard($scope,$mdDialog,items,allScorecard,scorecardSelected){
	$scope.scorecardSelected = scorecardSelected;
	$scope.allScorecard =allScorecard;
	$scope.selectedItem={};
	$scope.close = function(){
		$mdDialog.cancel();
	};
	
	$scope.apply = function(){
		$mdDialog.cancel();
		items.resolve($scope.selectedItem);
	};
};

})();
