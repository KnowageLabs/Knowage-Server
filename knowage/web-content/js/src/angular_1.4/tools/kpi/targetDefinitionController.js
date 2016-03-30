var app = angular.module('kpiTarget', [ 'ngMaterial', 'angular_table', 'sbiModule', 'angular-list-detail', 'ui.codemirror', 'color.picker', 'angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('targetDefinitionController', ['$scope', 'sbiModule_config', 'sbiModule_translate', 'sbiModule_restServices', '$mdDialog', '$q', '$mdToast', '$angularListDetail', '$timeout', targetDefinitionControllerFunction]);

function targetDefinitionControllerFunction($scope, sbiModule_config, sbiModule_translate, sbiModule_restServices, $mdDialog, $q, $mdToast, $angularListDetail, $timeout) {
	$scope.translate = sbiModule_translate;
	$scope.target = {};
	$scope.targets = [
		{
			'name':'Target1',
			'category':'Categoria 1',
			'startValidity': new Date("2/03/2016"),
			'endValidity': new Date("3/03/2016")
		},
		{
			id: 2,
			'name':'Target2',
			'category':'Categoria 2',
			'startValidity': new Date("5/03/2016"),
			'endValidity': new Date("10/04/2016")
		},
		{
			'name':'Target3',
			'category':'Categoria 3',
			'startValidity': new Date("1/03/2016"),
			'endValidity': new Date("1/03/2017")
		},
	]; // TODO: replace with an empty array after debug

	$scope.targetsActions = [
		{
			label: sbiModule_translate.load('sbi.generic.delete'),
			icon: 'fa fa-trash',
			action: function(removedTarget) {
				for (var i = 0; i < $scope.targets.length; i++) {
					if (typeof $scope.targets[i].id == 'undefined' || $scope.targets[i].id == null) continue;
					if ($scope.targets[i].id == removedTarget.id) {
						$scope.targets.splice(i, 1);
						return;
					}
				}
			}
		},
		{
			label: sbiModule_translate.load('sbi.generic.edit'),
			icon: 'fa fa-pencil',
			action: function() {}
		}
	];
	$scope.kpisActions = [
		{
			label: sbiModule_translate.load('sbi.generic.delete'),
			icon: 'fa fa-trash',
			action: function(deletedKpi) {
				for (var i = 0; i < $scope.kpis.length; i++) {
					if (typeof $scope.kpis[i].id == 'undefined' || $scope.kpis[i].id == null) continue;
					if ($scope.kpis[i].id == deletedKpi.id) {
						$scope.kpis.splice(i, 1);
						return;
					}
				}
			}
		},
		{
			label: sbiModule_translate.load('sbi.generic.edit'),
			icon: 'fa fa-pencil',
			action: function() {}
		}
	];
	$scope.kpi = {};
	$scope.kpis = [
		{
			name:'Kpi1',
			value: 100
		},
		{
			name:'Kpi2',
			value: 50
		},
		{
			name:'Kpi3',
			value: 25
		}
	]; // TODO: replace with an empty array after debug
	
	$scope.kpisFunctions = {
		openShowDialog: function($event) {
			$scope.showDialog($event);
		}
	};
	$scope.showDialog = function($event) {
		var kpiIdToIdx = {};
		for (var i = 0; i < $scope.kpis.length; i++) {
			if (typeof $scope.kpis[i].id == 'undefined' || $scope.kpis[i].id == null) continue;
			kpiIdToIdx['' + $scope.kpis[i].id] = i;
		}
		$mdDialog.show({
			templateUrl: sbiModule_config.contextName + '//js/src/angular_1.4/tools/kpi/template/targetKpiAddDialog.jsp',
			hasBackdrop: true,
			clickOutsideToClose: false,
			controller: ['$scope', function($scope) {
				$scope.selectedKpis = [];
				$scope.foundActions = [
					{
						label: sbiModule_translate.load('sbi.generic.edit'),
						icon: 'fa fa-pencil',
						action: function() {}
					}
				];
				$scope.foundKpi = {};
				$scope.foundKpis = [
					{
						name: 'KPI A',
						category: 'Categoria A',
						date: '1/03/2016',
						author: "John Alpha",
						value: "3000"
					},
					{
						name: 'KPI B',
						category: 'Categoria B',
						date: '2/03/2016',
						author: "John Bravo",
						value: "50%"
					},
					{
						name: 'KPI C',
						category: 'Categoria C',
						date: '3/03/2016',
						author: "John Charlie",
						value: "365"
					}
				]; // TODO: replace with an empty array after debug
				$scope.close = function close() {
					$mdDialog.cancel();
				};
				$scope.ok = function ok() {
					$mdDialog.hide($scope.selectedKpis);
				};
				$scope.findKpis = function() {
					sbiModule_restServices.get("1.0/kpi", "listKpi")
						.success(
							function(data, status, headers, config) {
								//alert(JSON.stringify(data));
								this.formatDate = function(ts) {
									date = new Date(ts);
									var day = "00" + date.getDate(); day = day.substr(day.length - 2);
									var month = "00" + (1 + date.getMonth()); month = month.substr(month.length - 2);
									var year = 1900 + date.getYear();
									var s = day + "/" + month + "/" + year; // =  + "/" + (1 + date.getMonth()) + "/" (1900 + date.getYear());
									return s;
								};
								var newKpis = [];
								for (var i = 0; i < data.length; i++) {
									if (typeof(kpiIdToIdx['' + data[i].id]) == 'undefined') { 
										newKpis[newKpis.length] = {
											id: data[i].id,
											version: data[i].version,
											name: data[i].name,
											category: data[i].category,
											date: this.formatDate(data[i].dateCreation),
											author: data[i].author,
											value: Math.round(10 + 989 * Math.random()) / 10 // TODO: remove after debug
										}
									}
								}
								$scope.foundKpis = newKpis;
							}
						).error(
							function(data, status, headers, config) {
								showToast(sbiModule_translate.load('sbi.generic.errorLoading'), 3000);
							}
						);
				};
				$scope.findKpis();
			}],
			targetEvent: $event,
			//preserveScope: true,
			focusOnOpen: false,
			onRemoving: function() {
			}
		}).then(function(selectedKpis) {
			for (var i = 0; i < selectedKpis.length; i++) {
				var idx = typeof(kpiIdToIdx['' + selectedKpis[i].id]) == 'undefined'
					? $scope.kpis.length : kpiIdToIdx['' + selectedKpis[i].id];
				$scope.kpis[idx] = selectedKpis[i];
			}
			
		}, function() { });
	};

	$scope.fetchTargets = function() {
		sbiModule_restServices.get("1.0/kpi", "listTarget")
			.success(
				function(data, status, headers, config) {
					// $scope.targets = []; // TODO: uncomment after debug
					// alert(JSON.stringify(data));
					this.formatDate = function(ts) {
						date = new Date(ts);
						var day = "00" + date.getDate(); day = day.substr(day.length - 2);
						var month = "00" + (1 + date.getMonth()); month = month.substr(month.length - 2);
						var year = 1900 + date.getYear();
						var s = day + "/" + month + "/" + year; // =  + "/" + (1 + date.getMonth()) + "/" (1900 + date.getYear());
						return s;
					};
					for (var i = 0; i < data.length; i++) {
						$scope.targets[$scope.targets.length] = {
							id: data[i].id,
							name: data[i].name,
							startValidity: new Date(data[i].startValidity), //this.formatDate(data[i].startValidity),
							endValidity: new Date(data[i].endValidity), //this.formatDate(data[i].endValidity),
							author: data[i].author,
							values: [], // Not needed yet
							category:
								typeof data[i].category != 'undefined' && data[i].category != null 
								? data[i].category.valueName : null
						}
					}
				}
			).error(
				function(data, status, headers, config) {
					showToast(sbiModule_translate.load('sbi.generic.errorLoading'), 3000);
				}
			);
	};
	$scope.fetchTargets();
	
	$scope.cancel = function() {
		$angularListDetail.goToList();
	}

	$scope.saveTarget= function() {
		newTarget = {
			id: null,
			name: $scope.target.name,
			startValidity: $scope.target.startValidity,
			endValidity: $scope.target.endValidity,
			author: $scope.target.author,
			values: [],
			category: null
		}
		for (var i = 0; i < $scope.kpis.length; i++) {
			if (typeof $scope.kpis[i].id == 'undefined' || $scope.kpis[i].id == null) continue; // TODO: remove after debug
			newTarget.values[newTarget.values.length] = {
				kpiId: $scope.kpis[i].id,
				kpiVersion: $scope.kpis[i].version,
				targetId: newTarget.id,
				value: $scope.kpis[i].value
			}
		}
		//alert(JSON.stringify(newTarget));
		sbiModule_restServices
			.post("1.0/kpi", "saveTarget", newTarget)
			.success(
				function(data, status, headers, config) {
					$angularListDetail.goToDetail();
					showToast(sbiModule_translate.load('sbi.generic.resultMsg'), 3000);
				}
			).error(
				function(data, status, headers, config) {
					showToast(sbiModule_translate.load('sbi.generic.savingItemError'), 5000);
				}
			);
	}
}
