angular.module('dinamic-list', ['ngMaterial','sbiModule'])
.directive('dinamicList', function() {
	return {
		templateUrl: '/knowagekpiengine/js/angular_1.x/kpi-dinamic-list/template/kpi-dinamic-list.html',
		controller: dinamicListController,
		scope: {
			ngModel:'=',
			selectedItem:'=', 
			multiSelect: '=',
			typeChart: '='
		},
		link: function (scope, elm, attrs) { 

		}
	}
});

function dinamicListController($scope,$mdDialog,$q,$mdToast,$timeout,sbiModule_restServices,sbiModule_translate,sbiModule_config,sbiModule_dateServices){
	var s=$scope;
	s.translate=sbiModule_translate;
	s.kpiAllList = [];
	s.tableKpisColumn = [];
	s.tableFunction={
			translate:sbiModule_translate,
			
			loadListKPI: function(item,evt){
			
				var promise = s.loadListKPI();
				promise.then(function(result){
					if(s.multiSelect==false){
						angular.copy([result],s.ngModel);
						
					}else{
						angular.copy(result,s.ngModel);
					}
					
				});
			},
			checkValue: function(item){
				if(isNaN(item.rangeMinValue) || isNaN(item.rangeMaxValue)){
					return true;
				}else{
					return item.rangeMinValue>=item.rangeMaxValue;
				}
			},
			vieweAs: [{"label":"speedometer","value":"Speedometer"},{"label":"kpicard","value":"Kpi Card"}]


	}

	s.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			s.removeKpi(item);
		}

	}];
	s.initKpiVariable = function(){
		if(s.multiSelect==false){
			s.ngModel = {};
		}else{
			s.ngModel = [];
		}

	}
	
	s.$watch('typeChart' , function(newValue,oldValue){
		if(newValue=='list'){
			s.tableKpisColumn=[
				                     {"label":"Name","name":"name"},
				                     {"label":"Category","name":"valueCd"},
				                     {"label":"Range Min Value","name":"rangeMinValueHTML"},
				                     {"label":"Range Max Value","name":"rangeMaxValueHTML"}
				                  ];
		}else{
			s.tableKpisColumn=[
				                     {"label":"Name","name":"name"},
				                     {"label":"Category","name":"valueCd"},
				                     {"label":"View As","name":"vieweAsList"},
				                     {"label":"Range Min Value","name":"rangeMinValueHTML"},
				                     {"label":"Range Max Value","name":"rangeMaxValueHTML"}
				                  ];
		}
		
	});
		
	
	s.setColumns = function(){
		if(s.typeChart=='list'){
			s.tableKpisColumn=[
				                     {"label":"Name","name":"name"},
				                     {"label":"Category","name":"valueCd"},
				                  ];
		}else{
			s.tableKpisColumn=[
				                     {"label":"Name","name":"name"},
				                     {"label":"Category","name":"valueCd"},
				                     {"label":"View As","name":"vieweAsList"}
				                  ];
		}
	}
	s.setColumns();
	s.removeKpi = function(item){
		var confirm = $mdDialog.confirm()
		.title(s.translate.load("sbi.kpi.measure.delete.title"))
		.content(s.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete kpi') 
		.ok(s.translate.load("sbi.general.yes"))
		.cancel(s.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {
			if(s.exists(item)){
				var index = s.indexInList(item, s.ngModel);
				s.ngModel.splice(index,1);
			}

		}, function() {
		});

	}
	s.typeOfWiew = [{'label':'speedometer','value':'Speedometer'},{'label':'semaphore','value':'Semaphore'},{'label':'kpicard','value':'Kpi Card'}];

	s.getListKPI = function(){
		var arr_name = [];
		sbiModule_restServices.alterContextPath(sbiModule_config.externalBasePath);
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
				obj["datacreation"]=sbiModule_dateServices.formatDate(new Date(response.data[i].dateCreation));
				obj["id"]=response.data[i].id;
				obj["vieweAsList"] ='<md-select ng-model="row.vieweAs" class="noMargin">'
					+'<md-option value=""></md-option>'
					+'<md-option ng-repeat="sev in scopeFunctions.vieweAs" value="{{sev.label}}">'
					+'{{sev.value}}'
					+' </md-option>'
					+'</md-select>';
				obj["vieweAs"]="";
				obj["rangeMinValueHTML"]= '<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}">Range Min Value</label>'
					+'<input required type="number" name="min" ng-model="row.rangeMinValue" />'
					+'</md-input-container>';
				obj["rangeMaxValueHTML"]='<md-input-container class="md-block">'
					+'<label ng-class="{\'redLabel\':scopeFunctions.checkValue(row)}" >Range Max Value</label>'
					+'<input required type="number" name="max" ng-model="row.rangeMaxValue" />'
					+'</md-input-container>';
				obj["rangeMinValue"]= "";
				obj["rangeMaxValue"]= "";
				s.kpiAllList.push(obj);

			}
		},function(response){
		});
	}
	s.getListKPI();
	s.loadListKPI = function(){
		var deferred = $q.defer();
		if(s.ngModel==undefined){
			s.ngModel = [];
		} else if(s.selectedItem.length==0){
			angular.copy(s.ngModel,s.selectedItem);
		}
		angular.copy(s.ngModel,s.selectedItem);
		$mdDialog.show({
			controller: DialogControllerKPI,
			templateUrl: '/knowagekpiengine/js/angular_1.x/kpi-dinamic-list/template/kpi-dinamic-list-dialog.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {items: deferred,kpi:s.kpi,kpiAllList:s.kpiAllList, kpiSelected: s.selectedItem,multiSelect:s.multiSelect}
		})
		.then(function(answer) {
			s.status = 'You said the information was "' + answer + '".';
			return deferred.promise;
		}, function() {
			s.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	};

	s.exists = function (item) {
		if(s.ngModel==undefined)return false;
		return  s.indexInList(item, s.ngModel)!=-1;

	};


	s.indexInList=function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}
		return -1;
	};

}


function DialogControllerKPI($scope,$mdDialog,items,kpi,kpiAllList,kpiSelected,multiSelect){
	//controller mdDialog to select kpi 
	var s = $scope;
	s.tableFunction={
			exists: function(item,evt){
				return s.exists(item);
			}

	}
	s.multiSelect = multiSelect;
	s.kpi=kpi;
	s.kpiAllList = kpiAllList;
	s.selectedItem = kpiSelected;

	s.exists = function (item) {
		return  s.indexInList(item, s.ngModel)==-1;

	};


	s.indexInList=function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}
		return -1;
	};
	s.close = function(){
		$mdDialog.cancel();
	}
	s.apply = function(){
		$mdDialog.cancel();
		items.resolve(s.selectedItem);
	}

	s.addKPIToCheck = function(){
		items.resolve(s.selectedItem);
		$mdDialog.cancel();
	}




}