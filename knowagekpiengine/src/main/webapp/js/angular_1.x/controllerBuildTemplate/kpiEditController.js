(function() {

var app = angular.module('templateBuild', [ 'ngMaterial', 'angular_table', 'sbiModule', 'expander-box','dinamic-list','kpi-style']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);


app.controller('templateBuildController', 
		['$scope',
		'sbiModule_translate', 
		'channelMessaging',
		"$mdDialog",
		"sbiModule_restServices",
		"$q","$mdToast",
		'$timeout',
		'sbiModule_config',
		'$httpParamSerializer',
		'$filter',
		'sbiModule_user',
		'sbiModule_config',
		'documentService',
		templateBuildControllerFunction ]);

function templateBuildControllerFunction(
		$scope,
		sbiModule_translate,
		channelMessaging,
		$mdDialog, 
		sbiModule_restServices,
		$q,
		$mdToast,
		$timeout,
		sbiModule_config,
		$httpParamSerializer,
		$filter,
		sbiModule_user,
		sbiModule_config,
		documentService
		){
	$scope.translate=sbiModule_translate;
	$scope.addKpis = [];
	$scope.typeDocument = 'widget';
	$scope.style = {};
	$scope.options = {"showvalue": true, "showtarget":true, "showtargetpercentage":false, "showthreshold":true,"vieweas":"Speedometer"};
	$scope.options.history = {"units": 'month',"size": 1};
	$scope.units = ['day', 'week', 'month', 'quarter', 'year'];
	$scope.typeOfWiew = [{'label':'speedometer','value':'Speedometer'},{'label':'kpicard','value':'Kpi Card'}];
	$scope.style.color = "rgb(14, 13, 13)";
	$scope.typeChart = 'kpi';
	$scope.selectedKpis = [];
	$scope.kpiList = [];
	$scope.scorecardSelected =[];
	$scope.allScorecard = [];
	$scope.showScorecards = sbiModule_user.functionalities.indexOf("ScorecardsManagement")>-1;
	$scope.documentService = documentService;
	
	
	


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
		sbiModule_restServices.promiseGet("1.0/kpiee","listScorecard")
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
//			templateUrl: '/knowagekpiengine/js/angular_1.x/controllerBuildTemplate/templateScorecard/templateScorecardDialog.html',
			templateUrl: sbiModule_config.contextName + '/js/angular_1.x/controllerBuildTemplate/templateScorecard/templateScorecardDialog.html',
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

	$scope.closeTemplate = function(){
		channelMessaging.sendMessage();
	}
	
	

	var saveTemplate = function(template){
		var obj = template;

		if(obj==null){
			$scope.showAction(sbiModule_translate.load('sbi.kpidocumentdesigner.errorrange'));
			return;
		}

		var formData = new FormData();
		formData.append("jsonTemplate",  JSON.stringify(obj));
		formData.append("docLabel",sbiModule_config.docLabel);
		console.log(JSON.stringify(obj));
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );

		sbiModule_restServices.promisePost("1.0/documents", 'saveKpiTemplate',
				$httpParamSerializer({jsonTemplate:JSON.stringify(obj), docLabel:sbiModule_config.docLabel}), {headers: {'Content-Type': 'application/x-www-form-urlencoded'}}).then(
						function(response) {
							console.log(response.data);

//							$scope.showAction("Template saved");
							var saveSuccessMsg = sbiModule_translate.load("sbi.kpidocumentdesigner.save.success");
							$scope.showAction(saveSuccessMsg);
						},function(response) {
							sbiModule_restServices.errorHandler(response.data,"");
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
				var kpiItem = response.data[i];
				var obj = {};

				obj["name"]=kpiItem.name;
				//obj["version"]=kpiItem.version;
				if(kpiItem.category!=undefined){
					obj["valueCd"] = kpiItem.category.valueCd;
				}
				obj["author"]=kpiItem.author;
				obj["id"]=kpiItem.id;
				obj["vieweAsList"] ='<md-input-container class="md-block">'
					+'<label>Widget Type</label>'
					+'<md-select ng-model="row.vieweAs" class="noMargin">'
					+'<md-option value=""></md-option>'
					+'<md-option ng-repeat="sev in scopeFunctions.vieweAs" value="{{sev.label}}">'
					+'{{sev.value}}'
					+' </md-option>'
					+'</md-select></md-input-container>';
				obj["rangeMinValueHTML"]= '<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}">Range Min Value</label>'
					+'<input required type="number" name="min" ng-model="row.rangeMinValue" />'
					+'</md-input-container>';
				obj["rangeMaxValueHTML"]='<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}" >Range Max Value</label>'
					+'<input required type="number" name="max" ng-model="row.rangeMaxValue" />'
					+'</md-input-container>';
				obj["prefixSuffixValue"] = kpiItem.prefixSuffixValue || '';
				obj["prefixSuffixValueHTML"] =
					'<md-input-container class="md-block">'
					+ '<label>' + sbiModule_translate.load('sbi.kpiedit.prefixSuffixValue') + '</label>'
					+ '<input type="text" name="max" ng-model="row.prefixSuffixValue" maxlength="3"/>'
					+'</md-input-container>';

				obj["isSuffix"] = kpiItem.isSuffix;
				obj["isSuffixHTML"] = '<md-input-container class="md-block">'
					+'<label>Prefix/Suffix</label>'
					+'<md-select ng-model="row.isSuffix">'
					+'<md-option value="false">'+ sbiModule_translate.load('sbi.kpiedit.prefix') +'</md-option>'
					+'<md-option value="true">'+ sbiModule_translate.load('sbi.kpiedit.suffix') +'</md-option>'
					+'</md-select>'
					+'</md-input-container>';


				$scope.kpiList.push(obj);
			}
			if(sbiModule_config.docLabel){
				$scope.loadTemplateIfExist();
			}
			
		},function(response){
		});
	};

	$scope.loadAllKpis();

	$scope.loadTemplateIfExist = function(){
		var obj={"id": sbiModule_config.docLabel};

		sbiModule_restServices.promisePost("1.0/kpisTemplate", 'getKpiTemplate',obj).then(
				function(response) {

					var template = response.data;
					if(template!=undefined && !angular.equals(template, {})){
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
		var nameScorecard = template.chart.data.scorecard.name;
		sbiModule_restServices.alterContextPath( sbiModule_config.externalBasePath );
		sbiModule_restServices.promiseGet("1.0/kpiee",nameScorecard+"/loadScorecardbyName")
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
			$scope.options.showtarget=template.chart.options.showtarget;

			$scope.options.showtargetpercentage=template.chart.options.showtargetpercentage;
			
			$scope.options.showthreshold=template.chart.options.showthreshold;
			
			$scope.options.showvalue=template.chart.options.showvalue;

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

		var selectedKpis = $scope.selectedKpis;
		var kpiList = $scope.kpiList;

		for(var i = 0; i < selectedKpis.length; i++){
			var selectedKpi = selectedKpis[i];

			var index = $scope.indexInList(selectedKpi, kpiList);
			if(index !=-1){
				var kpiListItem = kpiList[index];

				var obj = {};

				obj["name"]=kpiListItem.name;
				obj["version"]=kpiListItem.version;

				obj["valueCd"] = kpiListItem.valueCd;

				obj["author"]=kpiListItem.author;
				obj["id"]=kpiListItem.id;
				obj["vieweAs"]= selectedKpi.vieweas;
				obj["vieweAsList"] ='<md-input-container class="md-block">'
					+'<label>Widget Type</label>'
					+'<md-select ng-model="row.vieweAs" class="noMargin">'
					+'<md-option value=""></md-option>'
					+'<md-option ng-repeat="sev in scopeFunctions.vieweAs" value="{{sev.label}}">'
					+'{{sev.value}}'
					+' </md-option>'
					+'</md-select></md-input-container>';
				obj["rangeMinValueHTML"]= '<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}">Range Min Value</label>'
					+'<input required type="number" name="min" ng-model="row.rangeMinValue" />'
					+'</md-input-container>';
				obj["rangeMaxValueHTML"]='<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}" >Range Max Value</label>'
					+'<input required type="number" name="max" ng-model="row.rangeMaxValue" />'
					+'</md-input-container>';
				obj["rangeMinValue"]= parseFloat(selectedKpi.rangeMinValue);
				obj["rangeMaxValue"]= parseFloat(selectedKpi.rangeMaxValue);

				obj["prefixSuffixValue"] = selectedKpi.prefixSuffixValue || '';
				obj["prefixSuffixValueHTML"] =
					'<md-input-container class="md-block">'
					+ '<label>' + sbiModule_translate.load('sbi.kpiedit.prefixSuffixValue') + '</label>'
					+ '<input type="text" name="max" ng-model="row.prefixSuffixValue" maxlength="3"/>'
					+'</md-input-container>';

				obj["isSuffix"] = (selectedKpi.isSuffix != undefined && selectedKpi.isSuffix != '') ? selectedKpi.isSuffix : false;
				obj["isSuffixHTML"] = '<md-input-container class="md-block">'
					+'<label>Prefix/Suffix</label>'
					+'<md-select ng-model="row.isSuffix">'
					+'<md-option value="false">'+ sbiModule_translate.load('sbi.kpiedit.prefix') +'</md-option>'
					+'<md-option value="true">'+ sbiModule_translate.load('sbi.kpiedit.suffix') +'</md-option>'
					+'</md-select>'
					+'</md-input-container>';

				arr.push(obj);
			}
		}

		angular.copy(arr,$scope.selectedKpis);
	};

	$scope.indexInList = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
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
			var selectedKpis = $scope.selectedKpis;

			for(var i=0 ; i < selectedKpis.length; i++){
				var selectedKpi = selectedKpis[i];

				var kpiObject = {};
				kpiObject["name"] = selectedKpi.name;
				//kpiObject["version"] =  selectedKpi.version;
				kpiObject["vieweas"] = selectedKpi.vieweAs;
				if(selectedKpi.rangeMinValue >= selectedKpi.rangeMaxValue
						|| isNaN(selectedKpi.rangeMinValue)
						|| isNaN(selectedKpi.rangeMaxValue)){
					return null;
				}
				kpiObject["rangeMinValue"] = selectedKpi.rangeMinValue;
				kpiObject["rangeMaxValue"] = selectedKpi.rangeMaxValue;
				kpiObject["prefixSuffixValue"] = selectedKpi.prefixSuffixValue || '';
				kpiObject["isSuffix"] = selectedKpi.isSuffix;

				arr.push(kpiObject);
			}
			obj.chart.data["kpi"]=arr;

			obj.chart["options"] = $scope.options;
		}else{
			var scoreObject = {};
			scoreObject["name"] = $scope.scorecardSelected[0].name;
			obj.chart.data["scorecard"]=scoreObject;
		}
		obj.chart["style"] ={};
		obj.chart.style["font"] = $scope.style;

		return obj;
	};
	
	$scope.isWorkspace = function(){
		var sbiEnviroment = sbiModule_config.sbiEnviroment;
		return sbiEnviroment && sbiEnviroment.toUpperCase() == "WORKSPACE";
	}
	
	$scope.isWorkspace() ? $scope.saveTemplate = $scope.documentService.save : $scope.saveTemplate = saveTemplate;
	
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
