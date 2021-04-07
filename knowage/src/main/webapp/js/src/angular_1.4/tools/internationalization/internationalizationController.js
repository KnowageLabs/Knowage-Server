/**
 * Knowage, Open Source Business Intelligence suite Copyright (C) 2016
 * Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Knowage is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

(function() {
	'use strict';

	angular.module('InternationalizationModule', ['ngMaterial', 'sbiModule', 'i18nAvailableLanguagesModule'])
		   .config([
			   '$mdThemingProvider',
				'$httpProvider',
				function($mdThemingProvider, $httpProvider) {
				   $mdThemingProvider.theme('knowage');
				   $mdThemingProvider.setDefaultTheme('knowage');
			   }
		   ]).controller('internationalizationController', ['$scope', 'i18nAvailableLanguagesService', 'sbiModule_restServices', 'sbiModule_messaging', 'sbiModule_translate', '$mdDialog', '$filter', InternationalizationController])

	function InternationalizationController($scope, i18nAvailableLanguagesService, sbiModule_restServices, sbiModule_messaging, sbiModule_translate, $mdDialog,$filter) {
		var availableLanguagesService = i18nAvailableLanguagesService.getAvailableLanguages();
		$scope.availableLanguages = availableLanguagesService.languages;
		$scope.defaultLangMessages = [];
		$scope.messages = [];
		$scope.isTechnicalUser = isTechnicalUser;
		$scope.translate = sbiModule_translate;

		$scope.toggleEmptyMessages = function(){
			if($scope.emptyMessage.value){
				$scope.emptyMessage.originalMessages = angular.copy($scope.messages);
				$scope.messages = $filter('filter')($scope.messages, function(value){
					return !value.message
				})
			}else {
				$scope.messages = angular.copy($scope.emptyMessage.originalMessages);
			}
					
		}

		//REST
		$scope.getMessages = function(selectedTab) {
			$scope.messages = [];
			sbiModule_restServices.promiseGet("2.0/i18nMessages", "internationalization/?currLanguage="+selectedTab.languageTag)
				.then(function(response){
					$scope.emptyMessage = {value:false, originalMessages:[]};
					//For Default Language
					if(selectedTab.defaultLanguage) {
						//If database is empty show one row of input fields
						if(response.data.length == 0) {
							$scope.messages = [{
								language: '',
								label: '',
								message: ''
							}];
							$scope.defaultLangMessages = [];
						} else {
							$scope.defaultLangMessages = response.data;
							angular.copy($scope.defaultLangMessages, $scope.messages);
						}
					} else {
					//For other languages
						//If there are some messages in database
						if(response.data.length != 0) {
							$scope.defaultLangMessages.forEach(function(defMess){
								// searching if default message was translated into current language
								var translatedMessageArray = response.data.filter(function(item){
									return item.label == defMess.label;
								});

								if (translatedMessageArray[0]) {
									// in case default message was translated into current language, we add the default translation message as a reference for translators
									translatedMessageArray[0].defaultMessageCode = defMess.message;
									$scope.messages.push(translatedMessageArray[0]);
								} else {
									// in case default message was not translated into current language, we add an empty translation message
									var message = {
										language : selectedTab.languageTag,
										label : defMess.label,
										defaultMessageCode : defMess.message,
										message : ''
									};
									$scope.messages.push(message);
								}
							});

						} else {
						//If there is no messages in database, take Label and Message Code from Default one
							$scope.defaultLangMessages.forEach(function(defMess){
								var newMess = {};
								newMess.language = selectedTab.languageTag;
								newMess.label = defMess.label;
								newMess.defaultMessageCode = defMess.message;
								newMess.message = '';
								$scope.messages.push(newMess);
							});
						}
					}
				});
		};

		//Adding new blank row in Default Language table
		$scope.addLabel = function() {
			var tempMessage = {
					language: '',
					label: '',
					message: ''
			};
			$scope.messages.unshift(tempMessage);
		};

		//REST
		$scope.saveLabel = function(langObj, message) {
			if(message.hasOwnProperty('id')) {
				//UPDATE I18NMessage
				var toModify = angular.copy(message, {});
				delete toModify.defaultMessageCode;
				sbiModule_restServices.promisePut("2.0/i18nMessages", "", toModify)
				.then(function(response){
					console.log('[PUT]: SUCCESS!');
					sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.updated"), 'Success!');
					if(langObj.defaultLanguage) {
						$scope.getMessages(langObj);
					}
				}, function(response){
					sbiModule_messaging.showErrorMessage(response.data, 'Error');
				});

			} else {
				//INSERT I18NMessage
				var toInsert = angular.copy(message, {});
				if(toInsert.hasOwnProperty('defaultMessageCode')) {
					delete toInsert.defaultMessageCode;
				}
				toInsert.language = langObj.languageTag;
				sbiModule_restServices.promisePost("2.0/i18nMessages", "", toInsert)
					.then(function(response){
						console.log("[POST]: SUCCESS!");
						sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.created"), 'Success!');
						if(langObj.defaultLanguage) {
							$scope.getMessages(langObj);
						}
					}, function(response){
						sbiModule_messaging.showErrorMessage(response.data, 'Error');
					});
			}
		};

		//REST
		//DELETE
		$scope.deleteLabel = function(langObj, message, event) {
			if(message.hasOwnProperty('id')) {
				//Deleting Non-Default I18NMessage
				if(message.hasOwnProperty('defaultMessageCode')) {
					var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load('kn.internationalization.delete.confirm.title'))
		            .textContent(sbiModule_translate.load('kn.internationalization.delete.confirm.message'))
		            .targetEvent(event)
		            .ok(sbiModule_translate.load('kn.internationalization.delete.confirm.yes'))
		            .cancel(sbiModule_translate.load('kn.internationalization.delete.confirm.no'));

					$mdDialog.show(confirm).then(function(){
						sbiModule_restServices.promiseDelete("2.0/i18nMessages", message.id)
						.then(function(response){
							console.log("[DELETE]: SUCCESS!");
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
							$scope.getMessages(langObj);
						}, function(response){
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						});
					});
				} else {
					//Deleting Default I18NMessage
					var confirm = $mdDialog.confirm()
					.title(sbiModule_translate.load('kn.internationalization.delete.confirm.default.title'))
		            .textContent(sbiModule_translate.load('kn.internationalization.delete.confirm.default.message'))
		            .targetEvent(event)
		            .ok(sbiModule_translate.load('kn.internationalization.delete.confirm.yes'))
		            .cancel(sbiModule_translate.load('kn.internationalization.delete.confirm.no'));

					$mdDialog.show(confirm).then(function(){
						sbiModule_restServices.promiseDelete("2.0/i18nMessages/deletedefault", message.id)
						.then(function(response){
							console.log("[DELETE]: SUCCESS!");
							sbiModule_messaging.showSuccessMessage(sbiModule_translate.load("sbi.catalogues.toast.deleted"), 'Success!');
							$scope.getMessages(langObj);
						}, function(response){
							sbiModule_messaging.showErrorMessage(response.data.errors[0].message, 'Error');
						});
					});
				}
			} else {
				//Can't delete Default I18NMessage from other tab
				$mdDialog.show(
					$mdDialog.alert()
					.title(sbiModule_translate.load('kn.internationalization.delete.confirm.title'))
					.textContent(sbiModule_translate.load('kn.internationalization.delete.alert.message'))
					.targetEvent(event)
					.clickOutsideToClose(true)
					.ok(sbiModule_translate.load('kn.internationalization.delete.alert.ok'))
				);
			}

		};
	};
})();