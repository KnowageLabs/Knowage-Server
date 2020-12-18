/*
Knowage, Open Source Business Intelligence suite
Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.

Knowage is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

Knowage is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * @authors Giovanni Luca Ulivo (GiovanniLuca.Ulivo@eng.it)
 * v0.0.1
 *
 */
(function() {
	angular.module('cockpitModule')
		.directive('cockpitImageWidget', function(cockpitModule_widgetServices) {
			return {
				templateUrl: baseScriptPath + '/directives/cockpit-widget/widget/imageWidget/templates/imageWidgetTemplate.html',
				controller: cockpitImageWidgetControllerFunction,
				compile: function(tElement, tAttrs, transclude) {
					return {
						pre: function preLink(scope, element, attrs, ctrl, transclud) {
							element[0].classList.add("flex");
						},
						post: function postLink(scope, element, attrs, ctrl, transclud) {
							//init the widget
							element.ready(function() {
								scope.initWidget();
							});
						}
					};
				}
			};
		});

	function cockpitImageWidgetControllerFunction($scope, cockpitModule_widgetConfigurator, cockpitModule_properties, $mdDialog, sbiModule_config, sbiModule_restServices, sbiModule_translate, $q, $mdPanel, $timeout, sbiModule_user) {
		$scope.property = {
			style: {}
		};

		if ($scope.ngModel.cross == undefined) {
			$scope.ngModel.cross = {};
		};

		$scope.user = sbiModule_user;

		$scope.init = function(element, width, height) {
			var imgObj = element.find("img");
			$scope.safeApply();

			$timeout(function() {
				//imgObj.one("load",function(){
				$scope.refreshWidget(null, 'init');
				//});
			}, 500);
		};

		$scope.refresh = function(element, width, height, data, nature) {

			if ($scope.ngModel.style == undefined) {
				return;
			}

			if ((typeof ($scope.ngModel.style.heightPerc) != 'undefined' && typeof ($scope.ngModel.style.widthPerc) != 'undefined') && ($scope.ngModel.style.heightPerc != "" && $scope.ngModel.style.widthPerc != "")) {
				$scope.ngModel.style['background-size'] = $scope.ngModel.style.widthPerc + ' ' + $scope.ngModel.style.heightPerc;
			} else {
				$scope.ngModel.style['background-size'] = 'contain';
			}
			$scope.ngModel.style['background-position'] = $scope.ngModel.style.hAlign + ' ' + $scope.ngModel.style.vAlign;
			$scope.ngModel.style['background-image'] = "url(\'" + $scope.getUrl() + "\')";

			$scope.safeApply();
			if (nature == 'init') {
				$timeout(function() {
					$scope.widgetIsInit = true;
					cockpitModule_properties.INITIALIZED_WIDGETS.push($scope.ngModel.id);
				}, 500);
			}
		};

		$scope.getUrl = function() {
			return sbiModule_config.externalBasePath + "/restful-services/1.0/images/getImage?IMAGES_ID=" + $scope.ngModel.content.imgId;
		};

		$scope.editWidget = function(index) {
			var finishEdit = $q.defer();
			var config = {
				attachTo: angular.element(document.body),
				controller: EditWidgetController,
				disableParentScroll: true,
				templateUrl: baseScriptPath + '/directives/cockpit-widget/widget/imageWidget/templates/imageWidgetEditPropertyTemplate.html',
				position: $mdPanel.newPanelPosition().absolute().center(),
				fullscreen: true,
				hasBackdrop: true,
				clickOutsideToClose: false,
				escapeToClose: false,
				focusOnOpen: true,
				preserveScope: true,
				locals: { finishEdit: finishEdit, model: $scope.ngModel },
			};

			$mdPanel.open(config).then(function() {
				$scope.refresh();
			}, function() {
				$scope.refresh();
			});

			finishEdit.promise.then(function() {
				$scope.refresh();
			}, function() { });

			return finishEdit.promise;
		};
	};

	function EditWidgetController($scope, finishEdit, sbiModule_translate, $mdToast, sbiModule_config, sbiModule_restServices, model, mdPanelRef, sbiModule_user) {
		$scope.model = {};
		$scope.listImages = [];
		$scope.valigns = ['center', 'top', 'bottom'];
		$scope.haligns = ['center', 'left', 'right'];
		angular.copy(model, $scope.model);
		$scope.translate = sbiModule_translate;
		$scope.test = $scope.translate.load("sbi.generic.name");
		$scope.uploadImg = {};
		$scope.user = sbiModule_user;

		$scope.saveConfiguration = function() {
			if ($scope.model.content.imgId == undefined) {
				$scope.showAction($scope.translate.load('sbi.cockpit.widget.image.missingimg'));
				return;
			}
			angular.copy($scope.model, model);
			mdPanelRef.hide();
			$scope.$destroy();
			finishEdit.resolve();
		};
		$scope.cancelConfiguration = function() {
			mdPanelRef.close();
			$scope.$destroy();
			finishEdit.reject();
		};
		$scope.getImageUrl = function(img) {
			return sbiModule_config.externalBasePath + "/restful-services" + img.urlPreview;
		};

		$scope.setFile = function(element) {
			var max = 10 * 1024 * 1024;;
			if (element.files && element.files[0] && element.files[0].size > max) {
				$scope.showAction('File too large. Max file size is: ' + max / 1024 / 1024 + 'MB');
				element.value = null;
				delete $scope.uploadImg;
			} else {
				$scope.uploadImg.file = element.files[0];
				$scope.uploadImg.fileName = element.files[0] !== undefined ? element.files[0].name : '';
			}
			$scope.$apply();
		}

		$scope.showAction = function(text) {
			var toast = $mdToast.simple()
				.content(text)
				.action('OK')
				.highlightAction(false)
				.hideDelay(3000)
				.position('top')

			$mdToast.show(toast).then(function(response) {

				if (response == 'ok') {


				}
			});
		};

		$scope.erase = function(ev) {
			var imgId = $scope.model.content.imgId;
			sbiModule_restServices.restToRootProject();
			if (imgId == undefined) {
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.cockpit.widgets.image.missingselectedfile')).position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
			}
			else {
				var imageId = 'imageId=' + imgId;
				sbiModule_restServices.get("1.0/images", 'deleteImage', imageId)
					.then(function(response) {
						if (response.data.success) {
							refreshImagesList();
						} else if (response.data.hasOwnProperty("msg")) {
							$mdToast.show($mdToast.simple().content(sbiModule_translate.load(response.data.msg)).position('top').action(
								'OK').highlightAction(false).hideDelay(5000));
						} else {
							$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.genericError')).position('top').action(
								'OK').highlightAction(false).hideDelay(5000));
						}
					}, function(error) {
						$mdToast.show($mdToast.simple().content(error.data.ERROR).position('top').action(
							'OK').highlightAction(false).hideDelay(5000));

					});
			}
		}

		$scope.upload = function(ev) {
			if ($scope.uploadImg.fileName == "" || $scope.uploadImg.fileName == undefined) {
				$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.cockpit.widgets.image.missinguploadfile')).position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
			} else {
				var fd = new FormData();
				fd.append('uploadedImage', $scope.uploadImg.file);
				sbiModule_restServices.restToRootProject();
				sbiModule_restServices.post("1.0/images", 'addImage', fd, { transformRequest: angular.identity, headers: { 'Content-Type': undefined } })
					.then(function(response) {
						if (response.data.success) {
							refreshImagesList();
						} else if (response.data.hasOwnProperty("msg")) {
							$mdToast.show($mdToast.simple().content(sbiModule_translate.load(response.data.msg)).position('top').action(
								'OK').highlightAction(false).hideDelay(5000));
						} else {
							$mdToast.show($mdToast.simple().content(sbiModule_translate.load('sbi.generic.genericError')).position('top').action(
								'OK').highlightAction(false).hideDelay(5000));
						}
					}, function(error) {
						$mdToast.show($mdToast.simple().content(error.data.ERROR).position('top').action(
							'OK').highlightAction(false).hideDelay(5000));

					});
			}
		};
		function refreshImagesList() {

			sbiModule_restServices.restToRootProject();
			sbiModule_restServices.get("1.0/images", 'listImages').then(
				function(response) {
					if (response.data.hasOwnProperty("errors")) {
						sbiModule_logger.log("error:" + response.data.errors);
					} else {
						angular.copy(response.data.data, $scope.listImages);
					}
				}, function(error) {
					console.log("Error " + error.status);
				});
		};
		refreshImagesList();
	};

	//this function register the widget in the cockpitModule_widgetConfigurator factory
	addWidgetFunctionality("image", { 'updateble': false, 'cliccable': false });


})();