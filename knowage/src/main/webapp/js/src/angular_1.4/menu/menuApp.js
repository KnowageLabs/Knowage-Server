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

agGrid.initialiseAgGridWithAngular1(angular);
var myApp = angular.module('menuApp', ['ngMaterial','ngAria', 'sbiModule', 'agGrid','ngScrollbars']);


myApp.filter('i18n', function(sbiModule_i18n) {
	return function(label) {
		return sbiModule_i18n.getI18n(label);
	}
})
.controller('menuCtrl', ['$scope','$mdDialog','sbiModule_i18n',
    function ($scope,$mdDialog,sbiModule_i18n) {

		$scope.languages = [];

		$scope.openAside = false;

		$scope.i18n = sbiModule_i18n;
		$scope.i18n.loadI18nMap();

		$scope.toggleMenu = function(){
			if(!$scope.openAside) $scope.setNewsBadge();
			$scope.openAside = !$scope.openAside;
		}
    }]);

myApp.filter('trustAsHtml', function($sce) { return $sce.trustAsHtml; });

myApp.config(function($mdThemingProvider,ScrollBarsProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
    ScrollBarsProvider.defaults = {
            axis: 'y', // enable 2 axis scrollbars by default
            theme: 'minimal',
            autoHideScrollbar: true
        };
});

myApp.directive('menuAside', ['$http','$mdDialog','$timeout','sbiModule_config', 'sbiModule_restServices', 'sbiModule_messaging','sbiModule_translate', 'sbiModule_i18n', '$interval', '$httpParamSerializer', 'sbiModule_user'
  				, function(
  						$http,
  						$mdDialog,
  						$timeout,
  						sbiModule_config,
  						sbiModule_restServices,
  						sbiModule_messaging,
  						sbiModule_translate,
  						sbiModule_i18n,
  						$interval,
  						$httpParamSerializer,
  						sbiModule_user
  						) {
    return {
        restrict: 'E',
        templateUrl: sbiModule_config.dynamicResourcesBasePath+"/angular_1.4/menu/templates/menuBar.html",
        replace: true,
        link: function ($scope, elem, attrs) {
        	$scope.translate = sbiModule_translate;
        	$scope.i18n = sbiModule_i18n;

        	$scope.getImageBackgroundStyle = function(imagepath){
        		if(imagepath){
	        	    return {
	        	        'background-image':'url(' + imagepath + ')'
	        	    }
        		}
        	}

        	$scope.openedMenu = false;
        	$scope.toggleCheck = function(){
        		$scope.openedMenu = !$scope.openedMenu;
        		$scope.closeMenu();
        	}

        	$scope.closeCheck = function(){
        		$scope.openedMenu = false;
        	}

        	$scope.openCheck = function(){
        		$scope.openedMenu = true;
        	}

        	$scope.getCheck = function(){
        		return angular.element( document.querySelector( '#hamburger' ) )[0].checked;
        	}

        	$http.get(Sbi.config.contextName+'/restful-services/1.0/menu/enduser',{
        	    params: {
        	    		curr_country: Sbi.config.curr_country,
        	    		curr_language: Sbi.config.curr_language
        	    	}
        	}).then(function(response){
        		$scope.links = response.data.userMenu;
        		$scope.fixed = response.data.fixedMenu;
        		$scope.userName = response.data.userName;
        		$scope.userPicture = response.data.picture || Sbi.config.contextName+'/themes/commons/img/defaultTheme/logo.svg';

        		$scope.i18n.loadI18nMap().then(function() {

        			if (response.data.customMenu != undefined && response.data.customMenu != null && response.data.customMenu.length > 0){

        				if(response.data.customMenu[0].menu != undefined){
        					$scope.customs = response.data.customMenu[0].menu;
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



        	},function(error){
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
        	         templateUrl: sbiModule_config.dynamicResourcesBasePath+'/angular_1.4/menu/templates/languageDialog.html',
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
					$scope.serviceUrl = Sbi.config.contextName+"/servlet/AdapterHTTP";
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
		        	        scope.sessionRole = Sbi.user.sessionRole;
		        	        scope.serviceUrl = serviceUrl;
		        	        scope.closeDialog = function() {
		        	          $mdDialog.hide();
		        	        }
		        	        scope.save = function() {
		        	        	$http.post(scope.serviceUrl,
		        	        			$httpParamSerializer({ACTION_NAME: "SET_SESSION_ROLE_ACTION", SELECTED_ROLE: scope.sessionRole}),
		        	        			{headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'}})
		        	        	.success(function(data){
		        	        		console.log("default role set correcty");
		        	        		 //call again the home page
		        	        		var homeUrl = Sbi.config.contextName+"/servlet/AdapterHTTP?PAGE=LoginPage"
		        	        		window.location.href=homeUrl;
	        	        		}).error(function(error){
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

				function infoDialogController(scope, $mdDialog, title, okMessage, sbiModule_user, sbiModule_config, sbiModule_translate) {
						scope.user = sbiModule_user;
						scope.translate = sbiModule_translate;
						scope.config = sbiModule_config;
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

			$scope.goHome = function goHome(e,html){
				if(e){
					e.stopImmediatePropagation();
					e.preventDefault();
				}
				var url;
				if(!html){
					url = firstUrlTocallvar;
				}else{
					url = Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+html;
				}
				if(url){
					$scope.redirectIframe(url);
				}
				$scope.closeCheck();
			}

			$scope.languageSelection = function languageSelection(){
        		$scope.toggleMenu();

	 			var languages = [];

    	 		for (var j = 0; j < Sbi.config.supportedLocales.length ; j++) {
    	 			var aLocale = Sbi.config.supportedLocales[j];
     				var languageItem = {
						text: aLocale.country,
						iconCls: 'icon-' + aLocale.country.toLowerCase(),
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
				// $scope.showAlert("Select Language",languageTemplate)
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

			function calculateNewDownloads(data){
				var counter = 0;
				for(var k in data){
					if(data[k].alreadyDownloaded == false) counter ++;
				}
				return counter;
			}

			$http.get(Sbi.config.contextName+'/restful-services/2.0/export/dataset?showAll=true').then(function(result){
				$scope.downloadsList = result.data;
				$scope.newDownloadsNumber = calculateNewDownloads($scope.downloadsList);
			})

			// DOWNLOAD POLLING
			if(sbiModule_config.downloadPollingTime !== 0){
				$interval(function () {
					$http.get(Sbi.config.contextName + '/restful-services/2.0/export/dataset?showAll=true').then(function (result) {
						$scope.downloadsList = result.data;
						$scope.newDownloadsNumber = calculateNewDownloads($scope.downloadsList);
					}, function (error) {})
				}, sbiModule_config.downloadPollingTime || 10000);
			}


			$scope.downloads = function(){
				$scope.toggleMenu();
				var parentEl = angular.element(document.body);
				$mdDialog.show({
					parent: parentEl,
					templateUrl: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/downloads.html',
					controller: downloadsDialogController,
					locals: {downloadsList:$scope.downloadsList}
				});


				function downloadsDialogController(scope, $mdDialog, sbiModule_translate,downloadsList) {
	        	    scope.translate = sbiModule_translate;
	        	    scope.downloadsList = downloadsList;
	        	    scope.closeDialog = function(){
	        	    	$mdDialog.cancel();
	        	    }


	        	    scope.deleteDownload = function(index){
	        	    	scope.rowData.splice(index,1);
	        	    	scope.downloadGridOptions.api.setRowData(scope.rowData);
	        	    }

					scope.deleteAllDownload = function(){
						$http.delete(Sbi.config.contextName + '/restful-services/2.0/export').then(function(result){
							// Clear both lists
							$scope.downloadsList = [];
							scope.downloadsList = [];
						})
					}

					scope.$watch('downloadsList', function(newValue,oldValue){
						if(newValue && scope.downloadGridOptions.api) {
							scope.downloadGridOptions.api.setRowData(newValue);
						}
					})

					var columnDefs =[
					    {headerName: scope.translate.load('sbi.ds.fileName'), field: 'filename', cellClass: isNewDownload},
					    {headerName: "Creation Date", field: 'startDate', cellRenderer: dateRenderer, sort: 'desc'},
					    {headerName: '', field: 'download', cellRenderer: buttonRenderer,"field":"valueId","cellStyle":{"text-align": "right","display":"inline-flex","justify-content":"flex-end","border":"none"},
							suppressSorting:true,suppressFilter:true,width: 150,suppressSizeToFit:true, tooltip: false, "suppressMovable":true}
					];
					function isFullWidth(data) {
					    return data.pars ? true : false;
					}

					function isNewDownload(params){
						return !params.data.alreadyDownloaded && 'newDownload' ;
					}

					function dateRenderer(params){
						return moment(params.value).locale(sbiModule_config.curr_language).format('llll');
					}

					function buttonRenderer(params) {
						return '<md-button class="md-icon-button" ng-click="downloadContent(\''+ params.data.id +'\')" style="margin-top:4px;"><md-icon md-font-set="fa" md-font-icon="fa fa-download"></md-icon></md-button>' ;
					}

					scope.downloadContent = function(id){
						var encodedUri = encodeURI(Sbi.config.contextName+'/restful-services/2.0/export/dataset/'+id);
						var link = document.createElement("a");
						link.setAttribute("href", encodedUri);
						// link.setAttribute("download", "my_data.csv");
						document.body.appendChild(link); // Required for FF
						link.click();
					}

					function FullWidthCellRenderer() {}

					FullWidthCellRenderer.prototype.init = function(params) {
					    // trick to convert string of html into dom object
					    var eTemp = document.createElement('div');
					    eTemp.innerHTML = this.getTemplate(params);
					    this.eGui = eTemp.firstElementChild;
					};

					FullWidthCellRenderer.prototype.getTemplate = function(params) {
					    // var data = params.node.data;
					    return '<div class="full-width-panel" style="padding:8px">ciao</div>';
					};

					FullWidthCellRenderer.prototype.getGui = function() {
					    return this.eGui;
					};
					scope.downloadGridOptions = {
							angularCompileRows: true,
							// domLayout:'autoHeight',
						    defaultColDef: {
						        sortable: true,
						        filter: true
						    },
						    columnDefs: columnDefs,
						    rowData: scope.downloadList || [],
// getRowHeight: function (params) {
// return isFullWidth(params.data) ? 100 : 25;
// },
						    onGridReady: function (params) {
						        params.api.sizeColumnsToFit();
						    },
						    onGridSizeChanged: function(params){
						    	params.api.sizeColumnsToFit();
						    },
						    components: {
						        fullWidthCellRenderer: FullWidthCellRenderer
						    },
						    pagination: false,
						    isFullWidthCell: function (rowNode) {
						        return isFullWidth(rowNode.data);
						    },
						    fullWidthCellRenderer: 'fullWidthCellRenderer'
						};
				}

			}

			$scope.showAccessibilityDialog= function(){
			      var parentEl = angular.element(document.body);
       	       $mdDialog.show({
       	         parent: parentEl,
       	         templateUrl: sbiModule_config.dynamicResourcesBasePath+"/angular_1.4/menu/templates/accessibilityDialogTemplate.html",
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


					// if (sbiModule_config.isStatePersistenceEnabled) {

						var store = new Persist.Store(STORE_NAME, {
							swf_path: sbiModule_config.contextName + '/js/lib/persist-0.1.0/persist.swf'
							});

						store.set(PARAMETER_STATE_OBJECT_KEY, angular.toJson({}));
					// }

				} catch (err) {
					// console.error("Error in deleting parameters data from
					// session");
				}
			}

			$scope.menuCall = function menuCall(e,url,type,hasMenu,index,fromMenu){
				if(type) {
					$scope.closeCheck();
				}
				if (type == 'execDirectUrl'){
					// this is the case linked document would not be executable
// if(url == 'noExecutableDoc'){
// sbiModule_messaging.showErrorMessage(sbiModule_translate.load("sbi.execution.menu.noclickable"),
// sbiModule_translate.load('sbi.generic.genericWarning'));
// }
// else{
						$scope.redirectIframe(url);
// }
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
				} else if(type == "news"){
					$scope.news();
				}else if(type == "downloads"){
					$scope.downloads();
				}else if (type == "callExternalApp"){
					$scope.callExternalApp(url)
				} else if (type == "goHome"){
					$scope.goHome(null,url);
				} else if (type == "languageSelection"){
					$scope.languageSelection();
				} else if (type == "accessibilitySettings"){
					$scope.accessibilitySettings();
				}
				if(type) $scope.closeMenu();
				else{
					if(fromMenu) {
						$scope.nextMenu(e,index);
					}else if(hasMenu){
						if($scope.selectedCustom) {
							if($scope.path[0] == index){
								$scope.closeMenu();
							}else{
								$scope.openMenu(e,index);
							}
						}
						else {
							if($scope.openedMenu) $scope.openMenu(e,index);
							else {
								$scope.openCheck();
								$scope.openMenu(e,index);
							}
						}
					}
					else $scope.toggleCheck();
				}
			}

			$scope.safeApply = function(fn) {
				var phase = this.$root.$$phase;
				if(phase == '$apply' || phase == '$digest') {
					if(fn && (typeof(fn) === 'function')) {
						fn();
					}
				} else {
					this.$apply(fn);
				}
			}


			$scope.openMenu = function(e, cursor){
				e.preventDefault();
				e.stopImmediatePropagation();
				$scope.path = [cursor];
				$scope.selectedCustom = $scope.customs[$scope.path[$scope.path.length-1]];
			}

			$scope.nextMenu = function(e, cursor){
				e.preventDefault();
				e.stopImmediatePropagation();
				$scope.path.push(cursor);
				$scope.selectedCustom = $scope.selectedCustom.menu[$scope.path[$scope.path.length-1]];
			}

			$scope.backMenu = function(){
				$scope.tempAnimateClass = true;
				$timeout(function(){
					$scope.path.pop();
					if($scope.path.length == 0) delete $scope.selectedCustom;
					else{
						for(var k in $scope.path){
							if(k == 0) $scope.selectedCustom = $scope.customs[$scope.path[0]];
							else {
								$scope.selectedCustom = $scope.selectedCustom.menu[$scope.path[$scope.path[k]]];
							}
						}
					}$timeout(function(){
						$scope.tempAnimateClass = false;
					},300);
				},0);
			}

			$scope.toggleAdminMenu = function(e){
				if(e){
					e.preventDefault();
					e.stopImmediatePropagation();
				}
				$scope.adminOpened = !$scope.adminOpened;
			}

			$scope.closeMenu = function(){
				delete $scope.adminOpened;
				delete $scope.selectedCustom;
				delete $scope.tempSelectedCustom;
				$scope.path = [];
				$scope.safeApply();
			}

			$scope.setNewsBadge = function(){
				sbiModule_restServices.promiseGet("2.0", "newsRead/unread").then(function(response){
					$scope.unreadNewsNumber = response.data;
				})
			}
			$scope.setNewsBadge();

			$scope.showNewsButton = function(){
				sbiModule_restServices.promiseGet("2.0", "newsRead/total").then(function(response){
					$scope.totalNewsNumber = response.data;
				})
			}
			$scope.showNewsButton();

			$scope.news = function(){

				$scope.toggleMenu();
				var parentEl = angular.element(document.body);
				$mdDialog.show({
					parent: parentEl,
					templateUrl: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/news.jsp',
					controller: newsDialogController
				});

				function newsDialogController(scope, $mdDialog, sbiModule_translate) {
	        	    scope.translate = sbiModule_translate;
	        	    scope.loadingInfo = false;

	        	    scope.openDetail = function(category,message, index){
	        	    	if(!message.opened){
	        	    		scope.loadingInfo = true;
	        	    		if(!message.read) sbiModule_restServices.promisePost("2.0", "newsRead/" + message.id).then(function(response){})
		        	    	sbiModule_restServices.promiseGet("2.0", "news/" + message.id + "?isTechnical=false").then(function(response){
		        	    		message.html = response.data.html;
		        	    		message.read = true;
		        	    		scope.loadingInfo = false;
		        	    	})
	        	    	}
	        	    	message.opened = !message.opened;
	        	    }

	        	    sbiModule_restServices.promiseGet("2.0", "newsRead").then(function(readNews){
	        	    	scope.updateNews(readNews.data);
    				})

    				scope.updateNews = function(readNews){
	        	    	sbiModule_restServices.promiseGet("2.0", "news")
		    			.then(function(response) {
		    				scope.tempNews = response.data;
		    				scope.news = [{id:1, label:'News',messages:[]},{id:2,label:'Notifications',messages:[]},{id:3,label:'Warnings',messages:[]}];
		    				for(var n in response.data){
		    					for(var c in scope.news){
		    						if(response.data[n].type == scope.news[c].id){
		    							var tempNews = response.data[n];
		    							if(readNews.indexOf(tempNews.id) != -1) tempNews.read = true;
		    							scope.news[c].messages.push(tempNews);
		    						}
		    					}

		    				}

		    			}, function(response) {
		    				sbiModule_messaging.showErrorMessage(response.data.errors[0].message, $scope.translate.load('sbi.general.error'));
		    			});
	        	    }


        	        scope.closeDialog = function() {
        	          $mdDialog.hide();
        	        }
        	      }
			}
        }
    };
}]);
