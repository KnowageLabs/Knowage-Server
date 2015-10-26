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

app.controller('Controller', [ "translate", "restServices", "$scope", "$mdDialog", "$mdToast", behavior ]);

function behavior(translate, restServices, $scope, $mdDialog, $mdToast) {
	ctrl = this;
	$scope.translate = translate;
	
	ctrl.tsList;
	listTimespan();
	
	ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
	ctrl.activeTab = 'empty';
	ctrl.from;
	ctrl.to;
	ctrl.delay;
	
	ctrl.menuTs = [{
		label : translate.load('sbi.generic.delete'),
		action : function(item, event) {
			ctrl.deleteTimespan(item, event);
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
				if (ctrl.delay){
					var dist = to.getDate() - from.getDate();
					f_date.setDate(to.getDate() + ctrl.delay); 
					t_date.setDate(to.getDate() + ctrl.delay + dist);					
				}
				var f = ("0" + f_date.getDate()).slice(-2) + "/" + ("0" + (f_date.getMonth()+1)).slice(-2) + "/" + f_date.getFullYear(); 
				var t = ("0" + t_date.getDate()).slice(-2) + "/" + ("0" + (t_date.getMonth()+1)).slice(-2) + "/" + t_date.getFullYear(); 
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
		ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
		ctrl.resetFields();
		ctrl.activeTab = 'empty';
	}
	
	
	ctrl.newTs = function() {
		ctrl.selectedItem = JSON.parse(JSON.stringify(emptyTs));
		ctrl.resetFields();
		ctrl.activeTab = 'details';
	}
	
	
	ctrl.saveTimespan = function() {
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
	
	
	ctrl.deleteTimespan = function(item, event) {
		var confirm = $mdDialog.confirm()
			.title(translate.load("sbi.timespan.delete"))
			.content(translate.load("sbi.timespan.delete.message"))
			.ariaLabel('Lucky day')
			.ok(translate.load("sbi.generic.delete"))
			.cancel( translate.load("sbi.ds.wizard.cancel")).targetEvent(event);

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
								$mdToast.show(
										$mdToast.simple()
										.content(translate.load("sbi.timespan.delete.error"))
										.position('top')
										.action('OK')
										.highlightAction(false).hideDelay(3000));
								
							} else {
								var index = ctrl.tsList.indexOf(item);
								ctrl.tsList.splice(index, 1);
								ctrl.activeTab = 'empty';
								$mdToast.show(
										$mdToast.simple()
										.content(translate.load("sbi.timespan.delete.success"))
										.position('top')
										.action('OK')
										.highlightAction(false).hideDelay(3000));
							}
						})
				.error(function(data, status, headers, config) {
					console.log("delete error "+status);
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
	
}
