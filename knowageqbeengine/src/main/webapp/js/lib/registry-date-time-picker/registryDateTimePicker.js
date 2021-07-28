(function() {

angular.module('registry_date_time_picker', ['ngMaterial','sbiModule'])
.directive('registryDateTimePicker', function(sbiModule_config) {
  return {
    templateUrl: sbiModule_config.contextName + '/js/lib/registry-date-time-picker/registry-date-time-picker.html',
    controller: registryDateTimePickerFunction,
    scope: {
    	id:"@",
		ngModel:"="
    },
    link: function (scope, elm, attrs) {
    	  if(!attrs.id){
    		  scope.id= (new Date()).getTime();
    	  }
	  }
  	}
  });


function registryDateTimePickerFunction($scope) {
	
		$scope.initializeModel = function(hours, minutes, seconds) {
			if(!$scope.ngModel) {
				$scope.ngModel = new Date();
			}
			if(hours!=null) {
				$scope.ngModel.setHours(hours);
			}
			$scope.hours = $scope.ngModel.getHours();

			if(minutes!=null) {
				$scope.ngModel.setMinutes(minutes);
			}
			$scope.minutes = $scope.ngModel.getMinutes();

			if(seconds!=null) {
				$scope.ngModel.setSeconds(seconds);
			}	
			$scope.seconds = $scope.ngModel.getSeconds();
			
		}

		$scope.init = function() {
			if($scope.ngModel==="") {
				$scope.ngModel = null;
			}
			if($scope.ngModel != null) {
				$scope.hours = $scope.ngModel.getHours();
				$scope.minutes = $scope.ngModel.getMinutes();
				$scope.seconds = $scope.ngModel.getSeconds();
			}
		}
		$scope.init();
		
		$scope.handleDate = function (){
			$scope.hours = $scope.ngModel.getHours();
			$scope.minutes = $scope.ngModel.getMinutes();
			$scope.seconds = $scope.ngModel.getSeconds();
		}
		
		$scope.focusMe = function(){
			if($scope.ngModel==null){
				var elements = angular.element(document.querySelectorAll('.md-datepicker-input'));
				for(var idx in elements) {
					if (elements[idx].value === 'Invalid date'){
						elements[idx].value ="";
					}
				}
			}
		}
	
		$scope.checkHoursValid=function(){
			if(!$scope.hours || $scope.hours<0 || $scope.hours>23){
				$scope.hours = 0;
			}
			$scope.initializeModel($scope.hours, null, null);
		}
		$scope.checkMinuitesValid=function(){
			if(!$scope.minutes || $scope.minutes<0 || $scope.minutes>59){
				$scope.minutes = 0;
			}
			$scope.initializeModel(null, $scope.minutes, null);
			
		}
		$scope.checkSecondsValid=function(){
			if(!$scope.seconds || $scope.seconds<0 || $scope.seconds>59){
				$scope.seconds = 0;
			}
			$scope.initializeModel(null, null, $scope.seconds);
		}
		
		$scope.resetDatepickerInput=function(){
			angular.element(document.querySelector('.md-datepicker-input'))[0].value =null;
		}
		
		$scope.reset = function(){
			$scope.resetDate();
			//$scope.resetDatepickerInput();
		}

		$scope.alterHours=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.initializeModel(0, null, null);
			} else {
				var currHours = $scope.ngModel.getHours();
				var hours = 0;
				if(up){
					hours=(currHours+1)%24;
				}else{
					hours = currHours-1;
					if(hours<0){hours=23;}
				}
				
				$scope.hours = hours;
				$scope.ngModel.setHours($scope.hours);
			}
			
		}
		
		$scope.alterMinutes=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.initializeModel(null, 0, null);
			} else {
				var currMinutes = $scope.minutes;
				var minutes = 0;
				if(up){
					minutes=(currMinutes+1)%60;
				}else{
					minutes = currMinutes-1;
					if(minutes<0){minutes=59;}
				}
				
				$scope.minutes = minutes;
				$scope.ngModel.setMinutes($scope.minutes);
			}
		}

		$scope.alterSeconds=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.initializeModel(null, null, 0);
			} else {
				var currSeconds = $scope.seconds;
				var seconds = 0;
				if(up){
					seconds=(currSeconds+1)%60;
				}else{
					seconds = currSeconds-1;
					if(seconds<0){seconds=59;}
				}
				
				$scope.seconds = seconds;
				$scope.ngModel.setSeconds($scope.seconds);
			}
		}
		
		$scope.resetDate = function() {
			$scope.ngModel = null;
			$scope.hours = null;
			$scope.minutes = null;
			$scope.seconds = null;
		}

	}
})();