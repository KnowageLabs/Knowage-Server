var app = angular.module('kpiTarget', [ 'ngMaterial', 'angular_table', 'sbiModule', 'angular-list-detail', 'ui.codemirror', 'color.picker', 'angular_list']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
	$mdThemingProvider.theme('knowage')
	$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('targetDefinitionController', ['$scope', 'sbiModule_config', 'sbiModule_translate', 'sbiModule_restServices', '$mdDialog', '$filter', '$q', '$mdToast', '$angularListDetail', '$timeout', targetDefinitionControllerFunction]);

function targetDefinitionControllerFunction($scope, sbiModule_config, sbiModule_translate, sbiModule_restServices, $mdDialog, $filter, $q, $mdToast, $angularListDetail, $timeout) {
	this.formatDate = function(dts) {
		this.convertDateFormat = function(date) {
			result = "";
			if (date == "d/m/Y") {
				result = "dd/MM/yyyy";
			} else if (date == "m/d/Y") {
				result = "MM/dd/yyyy"
			}
			return result;
		};
		date = typeof dts == 'number' ? new Date(dts) : dts;
		var dateFormat = this.convertDateFormat(sbiModule_config.localizedDateFormat);
		return $filter('date')(date, dateFormat);
	};
	$scope.translate = sbiModule_translate;
	
	$scope.compareStartValidityDates = function(row1, row2) {
		return row1.startValidityDate.getTime() - row2.startValidityDate.getTime();
	}
	
	$scope.compareEndValidityDates = function(row1, row2) {
		return row1.endValidityDate.getTime() - row2.endValidityDate.getTime();
	}
	
	$scope.targetCategories = [];
	
	$scope.target = {};
	
	$scope.targets = [];
	
	$scope.targetsColumns = [{"label":"Name","name":"name"},{"label":"Category","name":"category.valueCd"},{"label":"Start Validity Date","name":"startValidity", "comparatorFunction": $scope.compareStartValidityDates},{"label":"Data End Validation","name":"endValidity", "comparatorFunction": $scope.compareEndValidityDates}];

	$scope.targetsActions = [
		{
			label: sbiModule_translate.load('sbi.generic.delete'),
			icon: 'fa fa-trash',
			action: function(removedTarget) {
				for (var i = 0; i < $scope.targets.length; i++) {
					if (typeof $scope.targets[i].id == 'undefined' || $scope.targets[i].id == null) continue;
					if ($scope.targets[i].id == removedTarget.id) {
						sbiModule_restServices
							.delete("1.0/kpi", removedTarget.id + "/deleteTarget")
							.success(
								function(data, status, headers, config) {
									$scope.targets.splice(i, 1);
									$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.resultMsg')).position('top')
											.action('OK').highlightAction(false).hideDelay(3000));
								}
							).error(
								function(data, status, headers, config) {
									$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.savingItemError')).position('top')
											.action('OK').highlightAction(false).hideDelay(5000));
								}
							);
						return;
					}
				}
			}
		} /*,
		{
			label: sbiModule_translate.load('sbi.generic.edit'),
			icon: 'fa fa-pencil',
			action: function() {}
		} */
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
		} /*,
		{
			label: sbiModule_translate.load('sbi.generic.edit'),
			icon: 'fa fa-pencil',
			action: function() {}
		} */
	];
	
	$scope.kpi = {};
	
	$scope.kpis = [];
	
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
				//	{
				//		label: sbiModule_translate.load('sbi.generic.edit'),
				//		icon: 'fa fa-pencil',
				//		action: function() {}
				//	}
				];
				$scope.foundKpi = {};
				$scope.foundKpis = [];
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
								this.formatDate = function(dts) {
									this.convertDateFormat = function(date) {
										result = "";
										if (date == "d/m/Y") {
											result = "dd/MM/yyyy";
										} else if (date == "m/d/Y") {
											result = "MM/dd/yyyy"
										}
										return result;
									};
									date = typeof dts == 'number' ? new Date(dts) : dts;
									var dateFormat = this.convertDateFormat(sbiModule_config.localizedDateFormat);
									return $filter('date')(date, dateFormat);
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
											value: 0
										}
									}
								}
								$scope.foundKpis = newKpis;
							}
						).error(
							function(data, status, headers, config) {
								$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.errorLoading')).position('top')
									.action('OK').highlightAction(false).hideDelay(3000));
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
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.target.kpiAdded')).position('top')
					.action('OK').highlightAction(false).hideDelay(5000));
		}, function() { });
	};

	$scope.fetchTargets = function() {
		sbiModule_restServices.get("1.0/kpi", "listTarget")
			.success(
				function(data, status, headers, config) {
					$scope.targets = [];
					this.formatDate = function(dts) {
						this.convertDateFormat = function(date) {
							result = "";
							if (date == "d/m/Y") {
								result = "dd/MM/yyyy";
							} else if (date == "m/d/Y") {
								result = "MM/dd/yyyy"
							}
							return result;
						};
						date = typeof dts == 'number' ? new Date(dts) : dts;
						var dateFormat = this.convertDateFormat(sbiModule_config.localizedDateFormat);
						return $filter('date')(date, dateFormat);
					};
					for (var i = 0; i < data.length; i++) {
						$scope.targets[$scope.targets.length] = {
							id: data[i].id,
							name: data[i].name,
							startValidityDate: new Date(data[i].startValidity),
							startValidity: this.formatDate(data[i].startValidity),
							endValidityDate: new Date(data[i].endValidity),
							endValidity: this.formatDate(data[i].endValidity),
							author: data[i].author,
							values: [], // Not needed yet
							category:
								typeof data[i].category != 'undefined' && data[i].category != null 
								? data[i].category : null
						}
					}
				}
			).error(
				function(data, status, headers, config) {
					$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.errorLoading')).position('top')
							.action('OK').highlightAction(false).hideDelay(3000));
				}
			);
	};
	
	$scope.cancel = function() {
		$scope.target = {};
		$scope.kpis = [];
		$angularListDetail.goToList();
	}
	
	$scope.saveTarget = function() {
		newTarget = {
			id: typeof $scope.target.id == 'undefined' ? null : $scope.target.id,
			name: $scope.target.name,
			startValidity: $scope.target.startValidityDate,
			endValidity: $scope.target.endValidityDate,
			author: $scope.target.author,
			values: [],
			category: $scope.target.category
		}
		for (var i = 0; i < $scope.kpis.length; i++) {
			newTarget.values[newTarget.values.length] = {
				kpiId: $scope.kpis[i].id,
				kpiVersion: $scope.kpis[i].version,
				targetId: newTarget.id,
				value: $scope.kpis[i].value
			}
		}
		var errors = "";
		if (newTarget.name == null) errors += sbiModule_translate.load('sbi.target.errorMissingName') + ". ";
		if (newTarget.startValidity == null) errors += sbiModule_translate.load('sbi.target.errorMissingStartValidity') + ". ";
		if (newTarget.endValidity == null) errors += sbiModule_translate.load('sbi.target.errorMissingEndValidity') + ". ";
		if (newTarget.values.length == 0) errors += sbiModule_translate.load('sbi.target.errorMissingKPI') + ". ";
		if (errors != "") {
			$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.savingItemError') + " - " + errors).position('top')
					.action('OK').highlightAction(false).hideDelay(10000));
			return;
		}
		sbiModule_restServices
			.post("1.0/kpi", "saveTarget", newTarget)
			.success(
				function(data, status, headers, config) {
					this.formatDate = function(dts) {
						this.convertDateFormat = function(date) {
							result = "";
							if (date == "d/m/Y") {
								result = "dd/MM/yyyy";
							} else if (date == "m/d/Y") {
								result = "MM/dd/yyyy"
							}
							return result;
						};
						date = typeof dts == 'number' ? new Date(dts) : dts;
						var dateFormat = this.convertDateFormat(sbiModule_config.localizedDateFormat);
						return $filter('date')(date, dateFormat);
					};
					if (typeof data.errors != 'undefined' && data.errors.length > 0) {
						$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.savingItemError')).position('top')
								.action('OK').highlightAction(false).hideDelay(5000));
						return;
					}
					var idx = $scope.targets.length;
					if (newTarget.id != null) {
						for (var i = 0; i < $scope.targets.length; i++) {
							if ($scope.targets[i].id == newTarget.id) {
								idx = i; // The target already exists
								break;
							}
						}
					} else {
						$scope.targets[idx] = {id: data.id}; // New target
					}
					$scope.targets[idx].name = $scope.target.name;
					$scope.targets[idx].startValidityDate = $scope.target.startValidityDate;
					$scope.targets[idx].startValidity = this.formatDate($scope.target.startValidityDate);
					$scope.targets[idx].endValidityDate = $scope.target.endValidityDate;
					$scope.targets[idx].endValidity = this.formatDate($scope.target.endValidityDate);
					$scope.targets[idx].category = $scope.target.category;
					$scope.target = {};
					$scope.kpis = [];
					$scope.fetchTargetCategories(); // Reload target categories
					$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.resultMsg')).position('top')
							.action('OK').highlightAction(false).hideDelay(3000));
					$angularListDetail.goToList();
				}
			).error(
				function(data, status, headers, config) {
					$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.savingItemError')).position('top')
							.action('OK').highlightAction(false).hideDelay(5000));
				}
			);
	}
	
	$scope.showSaveTargetDialog = function() {
		$mdDialog.show({
			controller:
				function ($scope, $mdDialog, targetCategories, targetCategory) {
					$scope.targetCategory = targetCategory;
					$scope.targetCategories = targetCategories;
					$scope.close = function() {
						$mdDialog.cancel();
					}
					$scope.apply = function() {
						$mdDialog.hide($scope.targetCategory);
					}
					$scope.querySearchCategory = function(query) {
						var results = query ? $scope.targetCategories.filter( createFilterFor(query) ) : [];
						var pushNeeded = true;
						for (var i = 0; i < results.length; i++) {
							if (angular.uppercase(results[i].valueCd) == angular.uppercase(query)) {
								pushNeeded = false;
								break;
							}
						}
						if (pushNeeded) results.push({valueCd: angular.uppercase(query)});
						return results;
					}
					function createFilterFor(query) {
						var lowercaseQuery = angular.lowercase(query);
						return function filterFn(state) {
							return (angular.lowercase(state.valueCd).indexOf(lowercaseQuery) === 0);
						};
					}
				},
			templateUrl: 'templatesaveTarget.html',
			clickOutsideToClose: true,
			preserveScope: true,
			locals: {
				targetCategories: $scope.targetCategories,
				targetCategory: typeof $scope.target.category != 'undefined' ? $scope.target.category : null
			}
		})
		.then(function(targetCategory) {
			$scope.target.category = targetCategory;
			$scope.saveTarget();
		}, function() {
			
		});
	}
	
	$scope.fetchTargetCategories = function() {
		sbiModule_restServices.promiseGet("2.0/domains","listByCode/KPI_TARGET_CATEGORY")
			.then(
				function(response) { 
					angular.copy(response.data, $scope.targetCategories);
				},
				function(response) {
					
				}
			);
	}
	
	
	// ==================
	// === FETCH DATA ===
	// ==================
	
	$scope.fetchTargetCategories();
	$scope.fetchTargets();
}
