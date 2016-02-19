var measureRoleApp = angular.module('measureRoleManager', [ 'ngMaterial',  'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror']);
measureRoleApp.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
}]);

measureRoleApp.controller('measureRoleMasterController', [ '$scope','sbiModule_translate' ,measureRoleMasterControllerFunction ]);
measureRoleApp.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog' ,measureListControllerFunction ]);
measureRoleApp.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureDetailControllerFunction ]); 

function measureRoleMasterControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.newMeasureFunction=function(){
	} 
	
	$scope.aliasList=["pippo","pino","pippino"];
}
 
function measureDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.currentMeasure={
			"query":"SELECT  usr.name as pippo FROM users AS usr WHERE usr.score>100"
				};
 	
	$scope.loadMetadata=function(){
		//remove select clausole
		var tmpText= $scope.currentMeasure.query.replace(/\n/g, " ").toLowerCase();
		
		if((tmpText.match(/(^|\s)select($|\s)/g) || []).length>1){
			alert("too many select clausole");
			return;
		}
		
		if((tmpText.match(/(^|\s)from($|\s)/g) || []).length>1){
			alert("too many from clausole");
			return;
		}
		
		var selectIndex=tmpText.search(/(^|\s)select($|\s)/);
		var fromIndex=tmpText.search(/(^|\s)from($|\s)/);
		if(selectIndex!=-1 && fromIndex!=-1){
			var aliasSubstring=tmpText.substring(selectIndex+6,fromIndex);
			var splittedAliasSubStr= aliasSubstring.trim().split(',');
			
			var tmpAliasList=[];
			for(var i=0;i<splittedAliasSubStr.length;i++){
				var tmpSplit= splittedAliasSubStr[i].trim().split(" as ");
				tmpAliasList.push(tmpSplit[tmpSplit.length-1]);
			}
			
			alert(tmpAliasList)
		}else{
			alert("query non completa")
		}
	}
}

function measureListControllerFunction($scope,sbiModule_translate,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.measureList=[
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    ];
	$scope.measureColumnsList=[
	                           {"label":$scope.translate.load("sbi.kpi.measureName"),"name":"measureName"},
	                           {"label":$scope.translate.load("sbi.kpi.rulesName"),"name":"rulesName"},
	                          {"label":$scope.translate.load("sbi.generic.author"),"name":"author"},
	                           ];
	
	$scope.measureClickFunction=function(item){
		console.log("click",item);
	}
	
	$scope.deleteMeasure=function(item,event){
		 var confirm = $mdDialog.confirm()
         .title($scope.translate.load("sbi.kpi.measure.delete.title"))
         .content($scope.translate.load("sbi.kpi.measure.delete.content"))
         .ariaLabel('delete measure') 
         .ok($scope.translate.load("sbi.general.yes"))
         .cancel($scope.translate.load("sbi.general.No"));
		   $mdDialog.show(confirm).then(function() {
		     console.log( 'You decided to get rid of your debt.');
		   }, function() {
		    console.log("annulla")
		   });
	}
	
	$scope.measureMenuOption= [{
		label : sbiModule_translate.load('sbi.generic.delete'),
		icon:'fa fa-trash' ,	 
		action : function(item,event) {
			$scope.deleteMeasure(item,event);
			}
	
		}];
}