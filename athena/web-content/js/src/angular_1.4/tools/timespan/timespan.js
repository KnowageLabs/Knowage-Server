var app = angular.module('timespanManager', [ 'ngMaterial', 'ui.tree',
		'angularUtils.directives.dirPagination', 'ng-context-menu',
		'angular_rest', 'glossary_tree', 'angular_list', 'angular_time_picker' ]);

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

app.controller('Controller', [ "translate", "restServices", "$scope", behavior ]);

function behavior(translate, restServices, $scope) {
	ctrl = this;
	$scope.translate = translate;
	
	ctrl.tsList;
	listTimespan();
	
	ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
	ctrl.from;
	ctrl.to;
	ctrl.delay;
	
	ctrl.menuTs = [{
		label : translate.load('sbi.generic.delete'),
		action : function(item, event) {
			ctrl.deleteTimespan(item);
		}
	}];
	
	ctrl.tsType = [{
		"label":"Time",
		"value":"time"
	},{
		"label":"Temporal",
		"value":"temporal"
	}];
	
	
	ctrl.tsCategory = [{
		"label":"Cat 1",
		"value":"Cat1"
	},{
		"label":"Cat 2",
		"value":"Cat2"
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
					console.log("item loaded " + ctrl.selectedItem.id);					
				}
			}).error(function(data, status, headers, config) {
				console.log("Load error " + status);
		})

	}
	
	
	ctrl.addInterval = function(from, to) {
		if(from && to){
			if(ctrl.selectedItem.type=='temporal'){
				if (ctrl.delay){
					from = delayDate(from, ctrl.delay);
					to = delayDate(to, ctrl.delay);
				}
				var f = ("0" + from.getDate()).slice(-2) + "/" + ("0" + (from.getMonth()+1)).slice(-2) + "/" + from.getFullYear(); 
				var t = ("0" + to.getDate()).slice(-2) + "/" + ("0" + (to.getMonth()+1)).slice(-2) + "/" + to.getFullYear(); 
				var interval = { "from": f, "to": t };
			} else {
				var interval = { "from": from, "to": to };
			}
			ctrl.selectedItem.definition.push(interval);
		}
	}

	
	ctrl.removeInterval = function(interval) {
		ctrl.selectedItem.definition.splice(ctrl.selectedItem.definition
				.indexOf(interval), 1)
	}
	
	
	ctrl.cancel = function() {
		if (ctrl.selectedItem.isnew)
			ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
		else {
			ctrl.selectedItem = ctrl.loadTimespan(ctrl.selectedItem);
		}
		ctrl.resetFields();
	}
	
	
	ctrl.newTs = function() {
		ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
		ctrl.resetFields();
	}
	
	
	ctrl.saveTimespan = function() {
		console.log(ctrl.selectedItem);
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
					listTimespan();
				}
			}).error(function(data, status, headers, config) {
				console.log("save error "+status);
			})
	}
	
	
	ctrl.deleteTimespan = function(item) {
		restServices
		.remove("1.0/timespan",
				"deleteTimespan",
				"ID=" + item.id)
		.success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) { 
						console.log("delete error");
					} else {
						var index = ctrl.tsList.indexOf(item);
						ctrl.tsList.splice(index, 1);
						if (item.id == ctrl.selectedItem.id) {
							ctrl.newTs();
						}
					}
				})
		.error(function(data, status, headers, config) {
			console.log("delete error "+status);
		})
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
		restServices.get("1.0/timespan", "listTimespan").success(
			function(data, status, headers, config) {
				if (data.hasOwnProperty("errors")) {
					console.log("list error");
				} else {
					console.log("success");
					ctrl.tsList = data;
				}
			}).error(function(data, status, headers, config) {
				console.log("list error "+status);
			})
	}
	
	
	function delayDate(date, delay) {
		var d_orig = new Date(date);
		var d_delay = new Date();
		d_delay.setDate(d_orig.getDate()+delay);
		return d_delay;
	}
}
