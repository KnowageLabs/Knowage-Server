var app = angular.module('timespanManager', [ 'ngMaterial',
		'angular_rest', 'angular_list', 'angular_time_picker' ]);

app.config(function($mdThemingProvider) {
	$mdThemingProvider.theme('default').primaryPalette('grey').accentPalette(
			'blue-grey');
});

app.service('translate', function() {
	this.load = function(key) {
		return messageResource.get(key, 'messages');
	};
});

var emptyTs = {
		name : "",
		type : "",
		category : "",
		definition : [],
		isnew : true
};

app.controller('Controller', [ "translate", "restServices", "$scope", "$mdDialog", "$mdToast", behavior ]);

function behavior(translate, restServices, $scope, $mdDialog, $mdToast) {
	ctrl = this;
	$scope.translate = translate;
	
	ctrl.tsList;
	ctrl.tsCategory;
	listTimespan();
	loadCategories();
	
	ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
	ctrl.activeTab = 'empty';
	ctrl.from;
	ctrl.to;
	ctrl.delay;
	
	ctrl.menuTs = [{
		label : translate.load('sbi.generic.delete'),
		action : function(item) {
			ctrl.deleteTimespan(item);
		}
	}];
	
	ctrl.tsType = [{
		"label": translate.load('sbi.timespan.type.time'),
		"value": "time"
	},{
		"label": translate.load('sbi.timespan.type.temporal'),
		"value": "temporal"
	}];
	
	
	ctrl.loadTimespan = function(item) {
		restServices.get("1.0/timespan", "loadTimespan", "ID="+item.id+"").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("error loading timespan");
				} else {
					ctrl.selectedItem = data;
					ctrl.selectedItem.isnew = false;
					ctrl.resetFields();
					ctrl.activeTab = 'details';
					console.log("item loaded " + ctrl.selectedItem.id);					
				}
			}).error(function(data, status, headers, config) {
				console.log("Load error " + status);
		})

	}
	
	ctrl.addInterval = function(from, to) {
		if(from && to){
			if(ctrl.selectedItem.type=='temporal'){
				var f_date = new Date(from);
				var t_date = new Date(to);
				
				if(f_date > t_date){
					var alert = $mdDialog.alert()
						.title(translate.load("sbi.generic.warning"))
						.content(translate.load("sbi.timespan.temporal.viceversa.alert"))
						.ok(translate.load("sbi.general.ok"));
					$mdDialog.show( alert );
					return false;
				}
				
				if (ctrl.delay){
					var dist = to.getDate() - from.getDate();
					f_date.setDate(to.getDate() + ctrl.delay); 
					t_date.setDate(to.getDate() + ctrl.delay + dist);					
				}
				
				for(var i in ctrl.selectedItem.definition){
					var start = new Date(ctrl.selectedItem.definition[i].from.replace( /(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3') );
					var end = new Date(ctrl.selectedItem.definition[i].to.replace( /(\d{2})\/(\d{2})\/(\d{4})/, '$2/$1/$3') );
					
					if( f_date <= end && t_date >= start ){
						var alert = $mdDialog.alert()
							.title(translate.load("sbi.generic.warning"))
							.content(translate.load("sbi.timespan.temporal.overlap.alert"))
							.ok(translate.load("sbi.general.ok"));
						$mdDialog.show( alert );
						return false;
					}
				}
				
				
				var f = ('0' + f_date.getDate()).slice(-2) + '/' + ('0' + (f_date.getMonth()+1)).slice(-2) + '/' + f_date.getFullYear(); 
				var t = ('0' + t_date.getDate()).slice(-2) + '/' + ('0' + (t_date.getMonth()+1)).slice(-2) + '/' + t_date.getFullYear(); 
				var interval = { from: f, to: t };
				
				
				
			} else {
				var f_time = Date.parse('01/01/2011 '+from);
				var t_time = Date.parse('01/01/2011 '+to);
				
				if(f_time > t_time){
					var alert = $mdDialog.alert()
						.title(translate.load("sbi.generic.warning"))
						.content(translate.load("sbi.timespan.time.viceversa.alert"))
						.ok(translate.load("sbi.general.ok"));
					$mdDialog.show( alert );
					return false;
				}
				
				for(var i in ctrl.selectedItem.definition){
					var start = Date.parse('01/01/2011 '+ctrl.selectedItem.definition[i].from);
					var end = Date.parse('01/01/2011 '+ctrl.selectedItem.definition[i].to);
					
					if( f_time <= end && t_time >= start ){
						var alert = $mdDialog.alert()
							.title(translate.load("sbi.generic.warning"))
							.content(translate.load("sbi.timespan.time.overlap.alert"))
							.ok(translate.load("sbi.general.ok"));
						$mdDialog.show( alert );
						return false;
					}
				}
				
				var interval = { from: from, to: to };
			}
			ctrl.selectedItem.definition.push(interval);
		}
	}

	
	ctrl.removeInterval = function(interval) {
		ctrl.selectedItem.definition.splice(ctrl.selectedItem.definition
				.indexOf(interval), 1)
	}
	
	
	ctrl.cancel = function() {
		var confirm = $mdDialog.confirm()
		.title(translate.load("sbi.timespan"))
		.content(translate.load("sbi.timespan.cancel.message"))
		.ariaLabel('Lucky day')
		.ok(translate.load("sbi.general.ok"))
		.cancel( translate.load("sbi.ds.wizard.cancel")).targetEvent(event);
		
		$mdDialog
		.show( confirm )
		.then(function() {
			ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
			ctrl.resetFields();
			ctrl.activeTab = 'empty';
		});
	}
	
	
	ctrl.newTs = function() {
		ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
		ctrl.resetFields();
		ctrl.activeTab = 'details';
	}
	
	
	ctrl.saveTimespan = function() {
		if(ctrl.selectedItem.definition.length==0){
			var alert = $mdDialog.alert()
				.title(translate.load("sbi.generic.warning"))
				.content(translate.load("sbi.timespan.nointerval.alert"))
				.ok(translate.load("sbi.general.ok"));
			$mdDialog.show( alert );
			
		} else {	
			
		restServices
		.post("1.0/timespan", "saveTimespan", ctrl.selectedItem).success(
			function(data, status, headers, config){
				if (data.hasOwnProperty("errors")) {
					console.log("save error");
				} else {
					if(ctrl.selectedItem.isnew){
						ctrl.selectedItem.id = data.id;
						ctrl.selectedItem.isnew = false;
						console.log("item saved "+data);
					} else {
						console.log("item updated "+data);
					}
					showToast(translate.load("sbi.timespan.save.success"));
					listTimespan();
				}
			}).error(function(data, status, headers, config) {
				showToast(translate.load("sbi.timespan.save.error"));
			})
		}
	}
	
	
	ctrl.deleteTimespan = function(item) {
		var confirm = $mdDialog.confirm()
			.title(translate.load("sbi.timespan.delete"))
			.content(translate.load("sbi.timespan.delete.message"))
			.ariaLabel('Lucky day')
			.ok(translate.load("sbi.generic.delete"))
			.cancel( translate.load("sbi.ds.wizard.cancel"));

		$mdDialog
			.show( confirm )
			.then(function() {
				restServices
				.remove("1.0/timespan",
						"deleteTimespan",
						"ID=" + item.id)
				.success(
						function( data, status, headers, config ) {
							if (data.hasOwnProperty( "errors" )) { 
								console.log( "delete error" );
								showToast(translate.load("sbi.timespan.delete.error"));
								
							} else {
								var index = ctrl.tsList.indexOf(item);
								ctrl.tsList.splice(index, 1);
								ctrl.activeTab = 'empty';
								showToast(translate.load("sbi.timespan.delete.success"));
							}
						})
				.error(function(data, status, headers, config) {
					console.log("delete error "+status);
					showToast(translate.load("sbi.timespan.delete.success"));
				})
			});
	}
	
	ctrl.changeType = function() {
		ctrl.selectedItem.definition = [];
		ctrl.resetFields();
	}
	
	
	ctrl.resetFields = function() {
		ctrl.from = "";
		ctrl.to = "";
		ctrl.delay = "";
	}
	
	
	function listTimespan() {
		restServices.get("1.0/timespan", "listDynTimespan").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("list error");
				} else {
					console.log("list success");
					ctrl.tsList = data;
				}
			}).error(function(data, status, headers, config) {
				console.log("list error "+status);
			})
	}
	
	function loadCategories() {
		restServices.get("domains", "listValueDescriptionByType",
		"DOMAIN_TYPE=TIMESPAN_CATEGORY").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("error loading category");
				} else {
					ctrl.tsCategory = data;
				}
			}).error(function(data, status, headers, config) {
				console.log("error loading category "+status);
			})
	}
	
	function showToast(msg) {
		$mdToast.show(
				$mdToast.simple()
				.content(msg)
				.position('top')
				.action('OK')
				.highlightAction(false).hideDelay(3000));
	}
	
	
}
