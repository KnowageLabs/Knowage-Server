var app = angular.module('documentExecutionModule').controller('rankController', ['$scope', '$http', '$mdSidenav', '$mdDialog','$mdToast', 'sbiModule_translate', 'sbiModule_restServices', 
                                                                     			 'sbiModule_config', 'sbiModule_messaging', 'execProperties', 'documentExecuteFactories', 'sbiModule_helpOnLine',
                                                                    			 'documentExecuteServices','docExecute_urlViewPointService','docExecute_paramRolePanelService','infoMetadataService','sbiModule_download',
                                                                    			 rankControllerFunction ]);

function rankControllerFunction($scope, $http, $mdSidenav,$mdDialog,$mdToast, sbiModule_translate, sbiModule_restServices, sbiModule_config,
		sbiModule_messaging, execProperties, documentExecuteFactories, sbiModule_helpOnLine,documentExecuteServices
		,docExecute_urlViewPointService,docExecute_paramRolePanelService,infoMetadataService,sbiModule_download) {

	
	$scope.rateScore=function(value){
		$scope.setRank = true;
		$scope.requestToRating = {
				'rating':value,
				'obj':$scope.executionInstance.OBJECT_ID,
		};
		$scope.isClick=true;
	};
	
	$scope.saveRank = function(){
		sbiModule_restServices.promisePost("documentrating", 'vote',$scope.requestToRating)
		.then(function(response) {
				if (response.data.hasOwnProperty("errors")) {
					$scope.showAction(response.data);
				} else {
					$mdDialog.cancel();
					$scope.showAction(sbiModule_translate.load('sbi.execution.executionpage.toolbar.rating.saved'));
					$scope.isClick=false;
				}

			},
			function(response) {
				$scope.isClick=false;
				$scope.errorHandler(response.data,"");
			}
		);
	};
	
	$scope.hoverStar = function(value){
		if($scope.setRank){
			for(var i=1;i<=5;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
			}
		}
		$scope.isClick=false;
		for(var i=1;i<=value;i++){
			var string= "star"+i;
			angular.element(document.getElementById(string).firstChild).removeClass('fa-star-o');
			angular.element(document.getElementById(string).firstChild).addClass('fa-star');
			
		}
	};
	
	$scope.leaveStar = function(value){
		
		if(!$scope.isClick && !$scope.setRank){
			for(var i=1;i<=value;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
			}
		} else if(!$scope.isClick && $scope.setRank){
			for(var i=1;i<=5;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star-o');
			}
			for(var i=1;i<=$scope.requestToRating.rating;i++){
				var string= "star"+i;
				angular.element(document.getElementById(string).firstChild).removeClass('fa-star-o');
				angular.element(document.getElementById(string).firstChild).addClass('fa-star');
			}
		}
	};
	
	$scope.close=function(){
		$mdDialog.cancel();
		$scope.isClick=false;
	};
	
	$scope.showAction = function(text) {
		$scope.isClick=false;
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top');

		$mdToast.show(toast).then(function(response) {
			if ( response == 'ok' ) {

			}
		});
	};
}