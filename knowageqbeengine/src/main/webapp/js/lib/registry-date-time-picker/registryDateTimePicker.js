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
	
		$scope.resetDatepickerInput=function(){
			angular.element(document.querySelector('.md-datepicker-input'))[0].value =null;
		}

		$scope.checkHoursValid=function(){
			if($scope.ngModel.hours==null){
				angular.element(document.querySelector('registry-date-time-picker #hoursInput-'+$scope.id))[0].value=12;
			}
		}
		$scope.checkMinuitesValid=function(){
			if($scope.ngModel.minutes==null){
				angular.element(document.querySelector('registry-date-time-picker #minInput-'+$scope.id))[0].value=00;
			}
		}
		$scope.checkSecondsValid=function(){
			if($scope.ngModel.seconds==null){
				angular.element(document.querySelector('registry-date-time-picker #secondsInput-'+$scope.id))[0].value=00;
			}
		}
		
		$scope.getDate=function(){
			if($scope.ngModel==null || $scope.ngModel==undefined || ($scope.ngModel!=undefined && $scope.ngModel=='')){return};
			return $scope.ngModel;
		}

		$scope.getHours=function(){
			if($scope.ngModel==null || $scope.ngModel==undefined || ($scope.ngModel!=undefined && $scope.ngModel=='')){return};
			return parseInt($scope.ngModel.getHours());
		}
		
		$scope.getMinutes=function(){
			if($scope.ngModel==null || $scope.ngModel==undefined || ($scope.ngModel!=undefined && $scope.ngModel=='')){return};
			return parseInt($scope.ngModel.getMinutes());
		}
		$scope.getSeconds=function(){
			if($scope.ngModel==null || $scope.ngModel==undefined || ($scope.ngModel!=undefined && $scope.ngModel=='')){return};
			return parseInt($scope.ngModel.getSeconds());
		}
		
		$scope.reset = function(){
			$scope.setDate(null);
			$scope.resetDatepickerInput();
		}

		$scope.alterHours=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.ngModel = new Date();
			} else {
				var currHours = $scope.ngModel.getHours();
				var hours = 0;
				if(up){
					hours=(currHours+1)%24;
				}else{
					hours = currHours-1;
					if(currHours<0){hours=23;}
				}
				
				$scope.ngModel.setHours(hours);
			}
			
		}
		
		$scope.alterMinutes=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.ngModel = new Date();
			} else {
				var currMinutes = $scope.ngModel.getMinutes();
				var minutes = 0;
				if(up){
					minutes=(currMinutes+1)%60;
				}else{
					minutes = currMinutes-1;
					if(currMinutes<0){minutes=59;}
				}
				
				$scope.ngModel.setMinutes(minutes);
			}
		}

		$scope.alterSeconds=function(up){
			event.preventDefault();
			event.stopImmediatePropagation();
			if(!$scope.ngModel) {
				$scope.ngModel = new Date();
			} else {
				var currSeconds = $scope.ngModel.getSeconds();
				var seconds = 0;
				if(up){
					seconds=(currSeconds+1)%40;
				}else{
					seconds = currSeconds-1;
					if(currSeconds<0){seconds=59;}
				}
				
				$scope.ngModel.setSeconds(seconds);
			}
		}
		
		$scope.setDate = function(date) {
			$scope.ngModel = date;
		}

	}
})();