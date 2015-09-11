

angular.module('bread_crumb', ['ngMaterial'])
.directive('breadCrumb', function() {
  return {
    templateUrl: '/athena/js/src/angular_1.4/tools/commons/templates/bread_crumb.html',
    controller: breadCrumbControllerFunction,
    scope: {
    	ngModel:'=',
    	itemName:"@",
    	id:"@",
    	selectedIndex:'=?',
    	selectedItem:'=?',
    	moveToCallback:'&?', //function callback to call when change item
    	control:'='
    		
    	},
      link: function (scope, elm, attrs) { 
    	  console.log("Inizializzo bread_crumb ");
    	  scope.control = scope.control || {};
    	  scope.ngModel= scope.ngModel || [];
    	  
    	  scope.control.insertBread=function(item){
    		scope.addItem(item);
    	  }
    	  scope.control.resetBreadCrumb=function(){
    		  scope.ngModel=[];
      	  }
    	  }
  }
  	});


function breadCrumbControllerFunction($scope){
	var s=$scope;
	
	s.moveToItem=function(item,index){
		console.log('breadCrumbControllerFunction-->moveToItem ')
		if(index!=s.selectedIndex){
			s.selectedIndex=index;
			s.selectedItem=item;
			s.ngModel=s.ngModel.slice(0,index+1);
			s.moveToCallback({item:item,index:index});
		}
	}
	s.addItem=function(item){
		  s.ngModel.push(item);
			s.selectedIndex=s.ngModel.length-1;
			s.selectedItem=item;

	}
	
	
	
	}

