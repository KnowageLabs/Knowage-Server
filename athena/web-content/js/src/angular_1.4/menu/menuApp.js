var myApp = angular.module('menuApp', ['ngMaterial','ngAria']);


myApp.controller('menuCtrl', ['$scope','$mdDialog',
    function ($scope,$mdDialog ) {
	
		$scope.languages = [];
	
		$scope.openAside = false;
	
		$scope.toggleMenu = function(){
			debugger;
			$scope.openAside = !$scope.openAside;
		}
    }]);


myApp.directive('menuAside', ['$http','$mdDialog', function($http,$mdDialog) {
    return {
        restrict: 'E',
        templateUrl: Sbi.config.contextName+"/js/src/angular_1.4/menu/templates/menuBar.html",
        replace: true,
        link: function ($scope, elem, attrs) {
        	$http.get(Sbi.config.contextName+'/restful-services/1.0/menu/enduser',{
        	    params: { 
        	    		curr_country: Sbi.config.curr_country, 
        	    		curr_language: Sbi.config.curr_language
        	    	}
        	}).success(function(data){
        		$scope.links = data.userMenu;
        		$scope.fixed = data.fixedMenu;
        		$scope.userName = data.userName;
        	}).
        	error(function(error){
        		$scope.showAlert('Attention, ' + $scope.userName,"Error Calling REST service for Menu. Please check if the server or connection is working.")
        	});
        	
        	$scope.showAlert = function(title,messageText){
                var alert = $mdDialog.alert()
                .title(title)
                .content('<br/><div>'+messageText+'</div>')
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
					this.win_roles = new Sbi.home.DefaultRoleWindow({'SBI_EXECUTION_ID': ''});
					this.win_roles.show();
				} else {
					$scope.openAside = false;
					$scope.showAlert('Role Selection','You currently have only one role');
				}
			}
			
			$scope.externalUrl = function externalUrl(url){
				window.open(url, "_blank")

			}
			
			$scope.info = function info(){
				if(!win_info_1){

					win_info_1= new Ext.Window({
						frame: false,
						style:"background-color: white",
						id:'win_info_1',
						autoLoad: {url: Sbi.config.contextName+'/themes/'+Sbi.config.currTheme+'/html/infos.jsp'},             				
						layout:'fit',
						width:210,
						height:180,
						closeAction:'hide',
						//closeAction:'close',
						buttonAlign : 'left',
						plain: true,
						title: LN('sbi.home.Info')
					});
				}		
				win_info_1.show();
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
        		
        		debugger;
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
			
			
			$scope.menuCall = function menuCall(url,type){
				if (type == 'execDirectUrl'){
					$scope.redirectIframe(url);
				} else if (type == 'roleSelection'){
					$scope.roleSelection();
				} else if (type =="execUrl"){
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
				}
			}
        }
    };
}]);