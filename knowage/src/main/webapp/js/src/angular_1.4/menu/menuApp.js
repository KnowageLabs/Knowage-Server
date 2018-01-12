/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


var myApp = angular.module('menuApp', ['ngMaterial','ngAria', 'sbiModule']);


myApp.controller('menuCtrl', ['$scope','$mdDialog',
    function ($scope,$mdDialog ) {

		$scope.languages = [];

		$scope.openAside = false;

		$scope.toggleMenu = function(){
			//debugger;
			$scope.openAside = !$scope.openAside;
		}
    }]);

myApp.config(function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
});

myApp.directive('menuAside', ['$http','$mdDialog','sbiModule_config', 'sbiModule_restServices', 'sbiModule_messaging','sbiModule_translate', 'sbiModule_i18n'
  				, function(
  						$http,
  						$mdDialog,
  						sbiModule_config,
  						sbiModule_restServices,
  						sbiModule_messaging,
  						sbiModule_translate,
  						sbiModule_i18n
  						) {
    return {
        restrict: 'E',
        templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/menuBar.html",
        replace: true,
        link: function ($scope, elem, attrs) {
        	$scope.translate = sbiModule_translate;
        	$scope.i18n = sbiModule_i18n;

        	$http.get(Sbi.config.contextName+'/restful-services/1.0/menu/enduser',{
        	    params: {
        	    		curr_country: Sbi.config.curr_country,
        	    		curr_language: Sbi.config.curr_language
        	    	}
        	}).success(function(data){
        		$scope.links = data.userMenu;
        		$scope.fixed = data.fixedMenu;
        		$scope.userName = data.userName;

        		$scope.i18n.loadI18nMap().then(function() {

        			if (data.customMenu != undefined && data.customMenu != null && data.customMenu.length > 0){

        				if(data.customMenu[0].menu != undefined){
        					$scope.customs = data.customMenu[0].menu;
        				}
        				else{
        					$scope.customs = {};
        				}
        			} else {
        				$scope.customs = {};
        			}

        			// i18n custom menu
        			for (var i = 0 ; i < $scope.customs .length; i ++ ){
        				$scope.customs[i].text = $scope.i18n.getI18n($scope.customs [i].text);
        			}

        		}); // end of load I 18n



        	}).
        	error(function(error){
        		$scope.showAlert('Attention, ' + $scope.userName,"Error Calling REST service for Menu. Please check if the server or connection is working.")
        	});

        	$scope.showAlert = function(title,messageText){
                var alert = $mdDialog.alert()
                .title(title)
                .content(messageText)
                .ok('Close');
                  $mdDialog
                    .show( alert )
                    .finally(function() {
                      alert = undefined;
                    });
        	}

        	$scope.showDialog = function showDialog() {
        	       var parentEl = angular.element(document.body);
        	       $mdDialog.show({
        	         parent: parentEl,
        	         templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/languageDialog.html",
        	         locals: {
        	           languages: $scope.languages
        	         }
        	         ,controller: DialogController
        	      });
        	      function DialogController(scope, $mdDialog, languages) {
        	        scope.languages = languages;
        	        scope.closeDialog = function() {
        	          $mdDialog.hide();
        	        }
        	        scope.menuCall=$scope.menuCall;
        	      }
        	    }

			$scope.redirectIframe = function(url){
				document.getElementById("iframeDoc").contentWindow.location.href = url;
				$scope.openAside = false;
			}

			$scope.execUrl = function(url){
				document.location.href = url;
				return;
			}

			$scope.roleSelection = function roleSelection(){
				if(Sbi.user.roles && Sbi.user.roles.length > 1){
					$scope.toggleMenu();
					$scope.serviceUrl = Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=SET_DEFAULT_ROLE_ACTION";
					var parentEl = angular.element(document.body);
					$mdDialog.show({
						parent: parentEl,
						templateUrl: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/roleSelection.html',
						locals: {
							title : sbiModule_translate.load('sbi.browser.defaultRole.title'),
							okMessage : sbiModule_translate.load('sbi.browser.defaultRole.save'),
							noDefaultRole : sbiModule_translate.load('sbi.browser.defaultRole.noDefRole'),
							serviceUrl : $scope.serviceUrl,
							sbiModule_translate : sbiModule_translate
						},
						controller: roleDialogController
					});

					function roleDialogController(scope, $mdDialog, title, okMessage, noDefaultRole, serviceUrl,sbiModule_translate) {
						 	scope.translate = sbiModule_translate;
							scope.noDefaultRole = noDefaultRole;
		        	        scope.title = title;
		        	        scope.okMessage = okMessage;
		        	        scope.roles = Sbi.user.roles;
		        	        scope.defaultRole = Sbi.user.defaultRole;
		        	        scope.serviceUrl = serviceUrl;
		        	        scope.closeDialog = function() {
		        	          $mdDialog.hide();
		        	        }
		        	        scope.save = function() {
		        	        	$http.get(scope.serviceUrl,{
		        	        	    params: {
		        	        	    		SELECTED_ROLE: scope.defaultRole,
		        	        	    	}
		        	        	}).success(function(data){
		        	        		console.log("default role set correcty");
		        	        		 //call again the home page
		        	        		var homeUrl = Sbi.config.contextName+"/servlet/AdapterHTTP?PAGE=LoginPage"
		        	        		window.location.href=homeUrl;
		        	        	}).
		        	        	error(function(error){
		        	        		console.log("Error: default role NOT set");
		        	        		$scope.showAlert('Attention, ' + $scope.userName,"Error setting default role. Please check if the server or connection is working.")
		        	        	});
		        	        }
	        	      }


				} else {
					$scope.openAside = false;
					$scope.showAlert('Role Selection','You currently have only one role');
				}
			}

			$scope.externalUrl = function externalUrl(url){
				window.open(url, "_blank")

			}

			$scope.info = function info(){
				$scope.toggleMenu();
				var parentEl = angular.element(document.body);
				$mdDialog.show({
					parent: parentEl,
					templateUrl: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/infos.jsp',
					locals: {
						title : sbiModule_translate.load('sbi.home.Info'),
						okMessage : sbiModule_translate.load('sbi.general.ok')
					},
					controller: infoDialogController
				});

				function infoDialogController(scope, $mdDialog, title, okMessage) {
	        	        scope.title = title;
	        	        scope.okMessage = okMessage;
	        	        scope.closeDialog = function() {
	        	          $mdDialog.hide();
	        	        }
        	      }
			}

			$scope.callExternalApp = function callExternalApp(url){
				if (!Sbi.config.isSSOEnabled) {
					if (url.indexOf("?") == -1) {
						url += '?<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
					} else {
						url += '&<%= SsoServiceInterface.USER_ID %>=' + Sbi.user.userUniqueIdentifier;
					}
				}

				$scope.redirectIframe(url);
			}

			$scope.goHome = function goHome(html){
				var url;
				if(!html){
					url = firstUrlTocallvar;
				}else{
					url = Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+html;
				}
				if(url){
					$scope.redirectIframe(url);
				}
			}

			$scope.languageSelection = function languageSelection(){
        		$scope.toggleMenu();

	 			var languages = [];

    	 		for (var j = 0; j < Sbi.config.supportedLocales.length ; j++) {
    	 			var aLocale = Sbi.config.supportedLocales[j];
     				var languageItem = {
    					text: aLocale.language,
    					iconCls:'icon-' + aLocale.language,
    					href: $scope.getLanguageUrl(aLocale),
    					linkType: 'execUrl'
    				};
     				languages.push(languageItem);
    	 		}

    	 		var languageTemplate;
    	 		for (var i = 0; i < languages.length; i++){
    	 			if (languageTemplate != undefined){
    	 				languageTemplate = languageTemplate + languages[i].text +"<br/>";
    	 			} else {
    	 				languageTemplate = languages[i].text +"<br/>";
    	 			}
    	 		}

    	 		$scope.languages = languages;

    	 		$scope.showDialog();
				//$scope.showAlert("Select Language",languageTemplate)
			}

			$scope.getLanguageUrl = function getLanguageUrl(config){
				var languageUrl = Sbi.config.contextName+"/servlet/AdapterHTTP?ACTION_NAME=CHANGE_LANGUAGE&LANGUAGE_ID="+config.language+"&COUNTRY_ID="+config.country+"&THEME_NAME="+Sbi.config.currTheme;
				return languageUrl;
			}

			$scope.accessibilitySettings = function (){

				console.log("IN ACCESSIBILITY SETTINGS");
				$scope.toggleMenu();
				$scope.showAccessibilityDialog();
			}

			$scope.showAccessibilityDialog= function(){
			      var parentEl = angular.element(document.body);
       	       $mdDialog.show({
       	         parent: parentEl,
       	         templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/accessibilityDialogTemplate.html",
       	         locals: {

       	         }
       	         ,controller: AccessibilityDialogController
       	      });

       	      function AccessibilityDialogController(scope, $mdDialog, $window,sbiModule_translate) {
       	    	scope.translate = sbiModule_translate;
       	    	scope.enableAccessibility = enableUIO;

       	        scope.saveAccessibilityPreferences = function(){
       	        	var preferencesObj={
       	        		id:null,
       	        		user:null,
       	        		enableUio:scope.enableAccessibility,
       	        		enableRobobraille:scope.enableAccessibility,
       	        		enableVoice: scope.enableAccessibility,
       	        		enableGraphSonification:scope.enableAccessibility,
       	        		preferences:null
       	        	};
       				sbiModule_restServices.promisePost('2.0/preferences','',preferencesObj)
       				.then(function(response) {
       			         console.log(response);
       			      sbiModule_messaging.showSuccessMessage("preferences saved successfully", 'Success');
       			        enableUIO=scope.enableUIO;
             	        enableRobobraille= scope.enableUIO;
             	    	enableVoice= scope.enableUIO;
             	    	enableGraphSonification= scope.enableUIO;
             	    	$mdDialog.hide();

             	    	$window.location.reload();
             	    	sbiModule_messaging.showSuccessMessage("Preferences saved successfully", 'Successs');

       				}, function(response) {
       					sbiModule_messaging.showErrorMessage(response, 'Error');

       				});

       	        }
       	        scope.closeDialog = function() {
       	          $mdDialog.hide();
       	        }
       	        scope.menuCall=$scope.menuCall;
       	      }

			}



			$scope.resetSessionObjects = function() {
				try {
					var STORE_NAME = sbiModule_config.sessionParametersStoreName;
					var PARAMETER_STATE_OBJECT_KEY = sbiModule_config.sessionParametersStateKey;


					//if (sbiModule_config.isStatePersistenceEnabled) {

						var store = new Persist.Store(STORE_NAME, {
							swf_path: sbiModule_config.contextName + '/js/lib/persist-0.1.0/persist.swf'
							});

						store.set(PARAMETER_STATE_OBJECT_KEY, angular.toJson({}));
					//}

				} catch (err) {
					//console.error("Error in deleting parameters data from session");
				}
			}

			$scope.menuCall = function menuCall(url,type){
				if (type == 'execDirectUrl'){
					$scope.redirectIframe(url);
				} else if (type == 'roleSelection'){
					$scope.roleSelection();
				} else if (type =="execUrl"){

					// if it s logout clean parameters cached in session
					if(url.indexOf("LOGOUT_ACTION") !== -1){
						$scope.resetSessionObjects();
					}

					$scope.execUrl(url)
				} else if (type == "externalUrl"){
					$scope.externalUrl(url)
				} else if (type == "info"){
					$scope.info();
				} else if (type == "callExternalApp"){
					$scope.callExternalApp(url)
				} else if (type == "goHome"){
					$scope.goHome(url);
				} else if (type == "languageSelection"){
					$scope.languageSelection();
				} else if (type == "accessibilitySettings"){
					$scope.accessibilitySettings();
				}
			}
        }
    };
}]);
