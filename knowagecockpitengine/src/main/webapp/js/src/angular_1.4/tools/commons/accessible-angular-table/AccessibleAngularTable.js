



var scripts = document.getElementsByTagName("script");
var currentScriptPathAccessibleTable = scripts[scripts.length - 1].src;


angular.module('accessible_angular_table',[]).

directive('accessibleAngularTable',function(){
	
	return{
		templateUrl:currentScriptPathAccessibleTable.substring(0, currentScriptPathAccessibleTable.lastIndexOf('/') + 1) +'templates/accessible-angular-table.html',
		scope:{
			ngModel:'=',
			columns:'='	
		},
		
	}
	
})



