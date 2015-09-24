angular.module('angular_time_picker', ['ngMaterial'])
.directive('angularTimePicker', function() {
  return {
    templateUrl: '/athena/js/src/angular_1.4/tools/commons/angular-time-picker//angular-time-picker.html',
    controller: angularTimePickerFunction,
    scope: {
    	ngModel:'=',
    	id:"@"
    	},
      link: function (scope, elm, attrs) { 
    	  console.log("Inizializzo angularTimePicker ");
      		
    	  }
  }
  	});


function angularTimePickerFunction($scope){
	var s=$scope;
	var date=new Date();
	
	s.hours=date.getHours()%13;
	s.minutes=date.getMinutes();
	s.ampm=date.getHours()<12 ?"AM" : "PM";


	
	
	s.alterNgModel=function(){
		var h=s.hours;
		var m=s.minutes%60;
		if(s.ampm=="PM" && h!=12){
			h=s.hours+12;
		}else if(s.ampm=="AM" && h==12){
			h=0;
		}
		h=h%24;
		s.ngModel=(h<10? '0'+h : h)+":"+(m<10? '0'+m : m);
	}
	
	s.alterNgModel();
	
	s.alterHours=function(up){
		if(up){
		s.hours=(s.hours+1)%13;
		if(s.hours==0)s.hours++;
		}else{
			s.hours-=1;
			if(s.hours<=0){s.hours=12;}
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
	
	s.checkValue=function(hour){
		if(hour){
			if(s.hours>12)s.hours=12;
			if(s.hours<0)s.hours=1;
			if(s.hours==undefined)s.hours=1;
		}else{
			if(s.minutes>60)s.minutes=59;
			if(s.minutes<0)s.minutes=0;
			if(s.minutes==undefined)s.minutes=00;
		}

		s.alterNgModel();
	}
	
	}

