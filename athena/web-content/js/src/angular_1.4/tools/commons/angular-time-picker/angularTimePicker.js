angular.module('angular_time_picker', ['ngMaterial'])
.directive('angularTimePicker', function() {
  return {
    templateUrl: '/knowage/js/src/angular_1.4/tools/commons/angular-time-picker//angular-time-picker.html',
    controller: angularTimePickerFunction,
    scope: {
    	ngModel:'=',
    	id:"@"
    	},
      link: function (scope, elm, attrs) { 
    	  console.log("Inizializzo angularTimePicker con id "+scope.id);
    	  
	    	  if(!attrs.id){
	    		  scope.id= (new Date()).getTime();
	    	  }
    	  }
  }
  	});


function angularTimePickerFunction($scope){
	var s=$scope;

	
	s.getHours=function(){
		if(s.ngModel==undefined){return 0};
		return parseInt(s.ngModel.split(":")[0]);
	}
	s.getMinutes=function(){
		if(s.ngModel==undefined){return 0};
		return parseInt(s.ngModel.split(":")[1]);
	}
	
	s.alterHours=function(up){
		if(up){
		s.hours=(s.hours+1)%24;
		
		}else{
			s.hours-=1;
			if(s.hours<0){s.hours=23;}
		}
		s.alterNgModel();
	}
	
	s.alterMinutes=function(up){
		if(up){
		s.minutes=(s.minutes+1)%60;
		}else{
			s.minutes-=1;
			if(s.minutes<0){s.minutes=59;}
		}

		s.alterNgModel();
	}
	
	s.alterNgModel=function(){
			if(s.ngModel==undefined || s.ngModel==""){
				var date=new Date();
				s.hours=date.getHours()%24;
				s.minutes=date.getMinutes();
			}
		var h=s.hours;
		var m=s.minutes;
		s.ngModel=(h<10? '0'+h : h)+":"+(m<10? '0'+m : m);
	}
	s.alterNgModel();
		
	
	
	$scope.$watch(
			function() {
				return s.ngModel;
			}, function(newValue, oldValue) {
				console.log("$watch",oldValue,newValue)
				if (newValue != oldValue) {
					s.hours=s.getHours();
					s.minutes=s.getMinutes();
					s.alterNgModel();
				}
			}, true);
	
	
	$scope.$watch(
			function() {
				var elem=angular.element(document.querySelector('angular-time-picker #minInput-'+$scope.id))[0];
				return elem==undefined? null:  elem.valueAsNumber;
			}, function(newValue, oldValue) {
				
				if (newValue != oldValue) {
					console.log("$watch elem",oldValue,newValue);
					if(newValue>59 || newValue<0){
						angular.element(document.querySelector('angular-time-picker #minInput-'+$scope.id))[0].value=oldValue;
					}else{
						s.minutes=newValue;
						s.alterNgModel();
					}
				}
			}, true);
	
	$scope.$watch(
			function() {
				var elem=angular.element(document.querySelector('angular-time-picker #hoursInput-'+$scope.id))[0];
				return elem==undefined? null:  elem.valueAsNumber;
			}, function(newValue, oldValue) {
				
				if (newValue != oldValue) {
					console.log("$watch elem",oldValue,newValue);
					if(newValue>23 || newValue<0){
						angular.element(document.querySelector('angular-time-picker #hoursInput-'+$scope.id))[0].value=oldValue;
					}else{
						s.hours=newValue;
						s.alterNgModel();
					}
				}
			}, true);
	
	}

