var app = angular.module('calendar', [ 'ngMaterial', 'ui.tree',
                                       'angularUtils.directives.dirPagination', 'ng-context-menu',
                                       'angular_list', 'angular_table' ,'angular-list-detail','sbiModule','file_upload', 'angular_2_col']);

app.config(['$mdThemingProvider', function($mdThemingProvider) {

	$mdThemingProvider.theme('knowage')

	$mdThemingProvider.setDefaultTheme('knowage');
}]);
app.controller('Controller', [ "sbiModule_download", "sbiModule_translate","sbiModule_restServices", "$scope","$mdDialog","$mdToast","$angularListDetail","$timeout","sbiModule_dateServices", controllerCalendar ]);

function controllerCalendar(sbiModule_download,sbiModule_translate,sbiModule_restServices, $scope, $mdDialog, $mdToast, $angularListDetail,$timeout,sbiModule_dateServices) {
	$scope.translate = sbiModule_translate;
	$scope.calendarList = [];
	$scope.selectCalendar= {};
	$scope.showCircularGenera = false;
	$scope.selectCalendar.realDateGenerated = [];
	$scope.calendarActions=[];
	$scope.listType = [];
	$scope.tablePage = 1;
	$scope.disableGenera = false;

	$scope.columns = [
	                  {
	                	  "label":"Date",
	                	  "name":"date",
	                	  "comparatorFunction" : function(a, b){

	                			var aValue = a.timeByDay.timeDate;
	                			var bValue = b.timeByDay.timeDate;

	                			return (bValue - aValue);
	                		}
	                  },
	                  {"label":"Day","name":"day"},
	                  {"label":"Festivity","name":"checkFestivity"},
	                  {"label":"National Fest","name":"nationalFest"},
	                  {"label":"Event","name":"selectEvent"}
	                  ];

	$scope.tableFunction={

			checkFestivity: function(item,evt){
				$scope.checkFestivityFunc(item);
			},
			checkNational: function(item,evt){
				$scope.checkNationalFunc(item);				
			},
			listType: $scope.listType,
			loadEvent: function(item){
				if(item.checkEvent.trim()!="" && item.checkEvent!=undefined){
					item.checkFestivityBoolean  = false;
					item.checkNationalBoolean = false;
				}
				//add other association 
				var index = $scope.indexinRealDate(item, $scope.selectCalendar.realDateGenerated);
				if(index!=-1 && (item.checkEvent.trim()!="" && item.checkEvent!=undefined)){
					//after add relation with other table
					var indexDom = $scope.indexofDomain(item.checkEvent, $scope.listType);
					if(indexDom !=-1){
						$scope.selectCalendar.realDateGenerated[index].attributeId = $scope.listType[indexDom].domainId;
						$scope.selectCalendar.realDateGenerated[index].calendarAttribute = {
								"calendarAttributeDomain": {"attributeDomainDescr": item.checkEvent}
						};
						$scope.selectCalendar.realDateGenerated[index].isHoliday = null;
						$scope.selectCalendar.realDateGenerated[index].pubHoliday = null;
					}

				}else{
					$scope.selectCalendar.realDateGenerated[index].calendarAttribute = null;
					$scope.selectCalendar.realDateGenerated[index].attributeId = 0;
				}
			}
	};

	$scope.indexofDomain = function(item, list){
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.attributeDomainDescr==item){
				return i;
			}
		}
	}



	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.removeRow(item);
		}
	}];

	$scope.calendarTable = [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,
		backgroundColor:'transparent',	
		action : function(item,event) {
			$scope.removeCalendar(item);
		}		
	}];
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

	$scope.saveCalendar = function(){
		if($scope.selectCalendar.calendar==undefined || $scope.selectCalendar.calendar.trim()==""){
			$scope.showAction(sbiModule_translate.load("sbi.calendar.errormissingname"));
		}else if($scope.selectCalendar.calStartDay==undefined){
			$scope.showAction(sbiModule_translate.load("sbi.calendar.errormissingstartdate"));
		}else if($scope.selectCalendar.calEndDay==undefined){
			$scope.showAction(sbiModule_translate.load("sbi.calendar.errormissingenddate"));
		}else{
			if($scope.selectCalendar.calendarId!=undefined){
				sbiModule_restServices.promisePost("calendar",+$scope.selectCalendar.calendarId+"/updateDaysGenerated", $scope.selectCalendar.realDateGenerated)
				.then(function(response){ 
					$scope.showAction(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"));

				},function(response){
					$scope.showAction(response.data);
				});
			}else{
				$scope.saveNewCalendar();

			}
		}


	}

	$scope.saveNewCalendar = function(){
		$scope.selectCalendar.calStartDay = new Date($scope.selectCalendar.calStartDay).getTime();
		$scope.selectCalendar.calEndDay = new Date($scope.selectCalendar.calEndDay).getTime();
		sbiModule_restServices.promisePost("calendar","saveCalendar", $scope.selectCalendar)
		.then(function(response){ 
			$scope.selectCalendar.calendarId = response.data;
			$scope.loadCalendarList();
			$scope.showAction(sbiModule_translate.load("sbi.execution.viewpoints.msg.saved"));
		},function(response){
			$scope.showAction(sbiModule_translate.load("sbi.generic.savingItemError"));
		});
	}
	$scope.loadCalendarList = function(){
		$scope.calendarList = [];
		sbiModule_restServices.promiseGet("calendar","getCalendarList")
		.then(function(response){ 
			for(var i=0;i<response.data.length;i++){

				var obj = {};
				obj["calendar"]=response.data[i].calendar;
				obj["calStartDay"]=new Date(response.data[i].calStartDay);
				obj["dateStartToShow"] = sbiModule_dateServices.formatDate(new Date(response.data[i].calStartDay));
				if(response.data[i].calType!=undefined){
					obj["calType"] = response.data[i].calType;
				}
				obj["calendarId"]=response.data[i].calendarId;
				obj["calEndDay"]=new Date(response.data[i].calEndDay);
				obj["dateEndToShow"] = sbiModule_dateServices.formatDate(new Date(response.data[i].calEndDay));
				obj["recStatus"]=response.data[i].recStatus;

				$scope.calendarList.push(obj);

			}


		},function(response){
			$scope.showAction(response.data);
		});
	}
	$scope.loadCalendarList();

	$scope.loadCalendar = function(item){
		$scope.disableGenera = true;
		$scope.showCircularGenera = false;
		angular.copy(item, $scope.selectCalendar);
		sbiModule_restServices.promiseGet("calendar",item.calendarId+"/getInfoCalendarById")
		.then(function(response){ 
			$scope.selectCalendar.realDateGenerated = response.data;
			if($scope.selectCalendar.realDateGenerated.length==0){
				$scope.disableGenera = false;
			}else{
				$scope.disableGenera = true;
				$scope.parseRealInfo(response.data);
			}


		},function(response){
		});
		$angularListDetail.goToDetail();
	}

	$scope.newCalendar = function(){
		$scope.selectCalendar= {};
		$scope.showCircularGenera = false;
		$scope.selectCalendar.realDateGenerated = [];
		$angularListDetail.goToDetail();
	}

	$scope.cancel = function(){
		$scope.disableGenera = false;
		$angularListDetail.goToList();
	}


	$scope.getDays = function(){
		var timeDiff = Math.abs((new Date($scope.selectCalendar.calEndDay)).getTime() - (new Date($scope.selectCalendar.calStartDay)).getTime());
		var diffDays = Math.ceil(timeDiff / (1000 * 3600 * 24)); 
		return diffDays+1;
	}
	$scope.generate = function(){
		if($scope.selectCalendar.realDateGenerated.length!=0){
			$scope.showAction($scope.translate.load("sbi.calendar.errorgenerate"));
		}else{
			var stringToShow = $scope.translate.load("sbi.calendar.confirmgenerate.description.first") + 
			" "+$scope.getDays()+" "
			+$scope.translate.load("sbi.calendar.confirmgenerate.description.second");
			var confirm = $mdDialog.confirm()
			.title($scope.translate.load("sbi.calendar.confirmgenerate"))
			.content(stringToShow)
			.ariaLabel('delete scheduler') 
			.ok($scope.translate.load("sbi.general.yes"))
			.cancel($scope.translate.load("sbi.general.No"));
			$mdDialog.show(confirm).then(function() {
				$scope.showCircularGenera = true;
				sbiModule_restServices.promisePost("calendar",$scope.selectCalendar.calendarId+"/generateCalendarDays")
				.then(function(response){ 
					sbiModule_restServices.promiseGet("calendar",$scope.selectCalendar.calendarId+"/getInfoCalendarById")
					.then(function(response){ 
						$scope.selectCalendar.realDateGenerated = response.data;
						$scope.parseRealInfo(response.data);
						$scope.showCircularGenera = false;
	
					},function(response){
					});
				},function(response){
					$scope.showAction(response.data);
				});
			}, function() {
			});
		}

	}
	Date.prototype.addDays = function(days) {
		var dat = new Date(this.valueOf())
		dat.setDate(dat.getDate() + days);
		return dat;
	}

	$scope.parseRealInfo = function(response){
		for(var i=0;i<$scope.selectCalendar.realDateGenerated.length;i++){
			//$scope.selectCalendar.realDateGenerated[i]["date"]=  response[i].timeByDay.dayDesc;
			$scope.selectCalendar.realDateGenerated[i]["date"] = response[i].timeByDay.dayOfMonth + "/"+ response[i].timeByDay.monthOfYear+ "/" + response[i].timeByDay.yearId;
			$scope.selectCalendar.realDateGenerated[i]["day"] = response[i].timeByDay.dayName;
			$scope.selectCalendar.realDateGenerated[i]["checkFestivity"] = '<div layout="row" layout-wrap>'
				+'<div>'
				+'<md-checkbox aria-label="BaseLayer" ng-checked="row.checkFestivityBoolean" ng-click="scopeFunctions.checkFestivity(row)"></md-checkbox>'
				+'</div></div>';
			$scope.selectCalendar.realDateGenerated[i]["nationalFest"] = '<div layout="row" layout-wrap>'
				+'<div>'
				+'<md-checkbox aria-label="BaseLayer" ng-checked="row.checkNationalBoolean" ng-click="scopeFunctions.checkNational(row)"></md-checkbox>'
				+'</div></div>';
			$scope.selectCalendar.realDateGenerated[i]["selectEvent"] = '<md-select  aria-label="BaseLayer" md-on-close="scopeFunctions.loadEvent(row)" ng-model="row.checkEvent" class="noMargin">'
				+'<md-option value=""></md-option>'
				+'<md-option ng-repeat="val in scopeFunctions.listType" value={{val.attributeDomainDescr}}>'
				+'{{val.attributeDomainDescr}}'
				+' </md-option>'
				+'</md-select>';
			if( response[i].isHoliday==1){
				$scope.selectCalendar.realDateGenerated[i]["checkFestivityBoolean"] = true;
			} else if(response[i].pubHoliday=="true"){
				$scope.selectCalendar.realDateGenerated[i]["checkNationalBoolean"] = true;
			}else if(response[i].attributeId!=0){
				$scope.selectCalendar.realDateGenerated[i]["checkEvent"] = response[i].calendarAttribute.calendarAttributeDomain.attributeDomainDescr;
			}

		}
	}

	$scope.loadDomainEvents = function(){
		sbiModule_restServices.promiseGet("calendar","getDomains")
		.then(function(response){ 
			angular.copy(response.data,$scope.listType);
		},function(response){
		});
	}

	$scope.loadDomainEvents();

	$scope.removeCalendar = function(item){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.kpi.measure.delete.title"))
		.content($scope.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete scheduler') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {
			//TODO remove from db
			sbiModule_restServices.promisePost("calendar",+item.calendarId+"/deleteCalendar")
			.then(function(response){ 
				$scope.showAction(sbiModule_translate.load("sbi.config.manageconfig.delete"));
				$scope.loadCalendarList();
			},function(response){
				$scope.showAction(response.data);
			});


		});
	}
	$scope.removeRow = function(item){
		var confirm = $mdDialog.confirm()
		.title($scope.translate.load("sbi.kpi.measure.delete.title"))
		.content($scope.translate.load("sbi.kpi.measure.delete.content"))
		.ariaLabel('delete scheduler') 
		.ok($scope.translate.load("sbi.general.yes"))
		.cancel($scope.translate.load("sbi.general.No"));
		$mdDialog.show(confirm).then(function() {

			sbiModule_restServices.promisePost("calendar",item.idCalComposition+"/deleteDayofCalendar")
			.then(function(response){ 
				var index = $scope.indexInList(item, $scope.selectCalendar.realDateGenerated);
				if(index!=-1){
					$scope.selectCalendar.realDateGenerated.splice(index,1);
				}
				$scope.showAction(sbiModule_translate.load("sbi.config.manageconfig.delete"));

			},function(response){
				$scope.showAction(response.data);
			});


		}, function() {
		});

	}
	$scope.indexInList = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.date==item.date){
				return i;
			}
		}

		return -1;
	};

	$scope.indexOfCalendar = function(item, list) {
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}

		return -1;
	};
	$scope.getDates= function(startDate, stopDate) {
		var dateArray = new Array();
		var currentDate = startDate;
		while (currentDate <= stopDate) {
			var obj = {"date": sbiModule_dateServices.formatDate(currentDate), "day": weekday[currentDate.getDay()],
					"checkFestivity": '<div layout="row" layout-wrap>'
						+'<div>'
						+'<md-checkbox aria-label="" ng-checked="row.checkFestivityBoolean" ng-click="scopeFunctions.checkFestivity(row)"></md-checkbox>'
						+'</div></div>', 
						"nationalFest": '<div layout="row" layout-wrap>'
							+'<div>'
							+'<md-checkbox aria-label="" ng-checked="row.checkNationalBoolean" ng-click="scopeFunctions.checkNational(row)"></md-checkbox>'
							+'</div></div>', 
							"selectEvent":'<md-select md-on-close="scopeFunctions.loadEvent(row)" ng-model="row.checkEvent" class="noMargin">'
								+'<md-option value=""></md-option>'
								+'<md-option ng-repeat="val in scopeFunctions.listType" value={{val.attributeDomainDescr}}>'
								+'{{val.attributeDomainDescr}}'
								+' </md-option>'
								+'</md-select>'
			};
			dateArray.push(obj)
			currentDate = currentDate.addDays(1);
		}
		return dateArray;
	}

	$scope.checkFestivityFunc = function(item){
		if(item.checkNationalBoolean || item.checkNationalBoolean==undefined || item.checkEvent!=undefined){
			item.checkNationalBoolean = false;
			item.checkEvent = ""
		}
		if(item.checkFestivityBoolean){
			item.checkFestivityBoolean = false;
		}else{
			item.checkFestivityBoolean  = true;
		}
		var index = $scope.indexinRealDate(item, $scope.selectCalendar.realDateGenerated);
		if(index!=-1 && $scope.selectCalendar.realDateGenerated[index].isHoliday!=1){
			$scope.selectCalendar.realDateGenerated[index].isHoliday = 1;
			$scope.selectCalendar.realDateGenerated[index].pubHoliday = null;
		}else if(index!=-1){
			$scope.selectCalendar.realDateGenerated[index].isHoliday = null;
			$scope.selectCalendar.realDateGenerated[index].calendarAttribute.attributeId = 0;
		}

	}
	$scope.checkNationalFunc = function(item){
		if(item.checkFestivityBoolean || item.checkFestivityBoolean==undefined || item.checkEvent!=undefined){
			item.checkFestivityBoolean  = false;
			item.checkEvent = ""
		}
		if(item.checkNationalBoolean){
			item.checkNationalBoolean = false;
		}else{
			item.checkNationalBoolean = true;
		}

		var index = $scope.indexinRealDate(item, $scope.selectCalendar.realDateGenerated);
		if(index!=-1 && $scope.selectCalendar.realDateGenerated[index].pubHoliday!="true"){
			$scope.selectCalendar.realDateGenerated[index].pubHoliday = "true";
			$scope.selectCalendar.realDateGenerated[index].isHoliday = null;
		}else if(index!=-1){
			$scope.selectCalendar.realDateGenerated[index].pubHoliday = null;
			$scope.selectCalendar.realDateGenerated[index].calendarAttribute.attributeId = 0;
		}
	}

	$scope.indexinRealDate=function(item, list){
		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.idCalComposition==item.idCalComposition){
				return i;
			}
		}
	}

}