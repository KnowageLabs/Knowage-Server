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
 	
 	$scope.saveMeasureFunction=function(){
 		alert("Salvato")
 	} 
 	$scope.cancelMeasureFunctionMeasureFunction=function(){
 		alert("cancelMeasureFunction")
 	} 
	
	$scope.aliasList=["pippo","pino","pippino"];
}
 
function measureDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.detailProperty={
			dataSourcesIsSelected:false,
			queryChanged:true,
			previewData:{rows:[],metaData:{fields:[]}},
			};
	
	$scope.currentMeasure={
			selectedDatasource:{},
			query:"SELECT * FROM employee as cur",
			metadata:{}
				};
 	
	 
	
	$scope.loadMetadata=function(){
		var postData={dataSourceId:$scope.currentMeasure.selectedDatasource,
				query:$scope.currentMeasure.query,
				maxItem:1
				}
		
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
//			angular.copy({},$scope.currentMeasure.metadata);
			
			var tmpMeas=[];
			
			for(var index in response.data.columns){
				tmpMeas.push(response.data.columns[index].label);
				if(!$scope.currentMeasure.metadata.hasOwnProperty(response.data.columns[index].label)){
					$scope.currentMeasure.metadata[response.data.columns[index].label]=response.data.columns[index]
				}
			}
			
			for(var index in $scope.currentMeasure.metadata){
				if(tmpMeas.indexOf($scope.currentMeasure.metadata[index].label)==-1){
					delete $scope.currentMeasure.metadata[index];
				}
			}
			
			
		},function(response){
			console.log("errore")
		});
//		//remove select clausole
//		var tmpText= $scope.currentMeasure.query.replace(/\n/g, " ").toLowerCase();
//		var selectIndex=tmpText.search(/(^|\s)select($|\s)/);
//		var fromIndex=tmpText.search(/(^|\s)from($|\s)/);
//		if(selectIndex!=-1 && fromIndex!=-1){
//			var aliasSubstring=tmpText.substring(selectIndex+6,fromIndex);
//			var splittedAliasSubStr= aliasSubstring.trim().split(',');
//			angular.copy([],$scope.currentMeasure.metadata);
//			for(var i=0;i<splittedAliasSubStr.length;i++){
//				var tmpSplit= splittedAliasSubStr[i].trim().split(" as ");
//				$scope.currentMeasure.metadata.push(tmpSplit[tmpSplit.length-1]);
//			}
//			 
//		}else{
//			alert("query non completa")
//		}
	}
	
	$scope.loadPreview=function(){
		var postData={dataSourceId:$scope.currentMeasure.selectedDatasource,
				query:$scope.currentMeasure.query,
				maxItem:10
				}
		
		sbiModule_restServices.promisePost("1.0/kpi","queryPreview",postData)
		.then(function(response){
			console.log("resp",response)
			 $scope.detailProperty.queryChanged=false;
			angular.copy(response.data,$scope.detailProperty.previewData);
		},function(response){
			console.log("errore")
		});
	}
	
	
}

function measureListControllerFunction($scope,sbiModule_translate,$mdDialog){
	$scope.translate=sbiModule_translate;
	$scope.measureList=[
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
	                    {"measureName":"measure2","rulesName":"regola1","dateCreation":"23/1/2016","category":"Cat1","author":"gino"},
	                    {"measureName":"measure3","rulesName":"regola2","dateCreation":"21/1/2016","category":"Cat2","author":"lino"},
	                    {"measureName":"measure4","rulesName":"regola3","dateCreation":"22/1/2016","category":"Cat3","author":"mario"},
	                    {"measureName":"measure5","rulesName":"regola3","dateCreation":"24/1/2016","category":"Cat3","author":"Acilly"},
	                    {"measureName":"measure1","rulesName":"regola1","dateCreation":"20/1/2016","category":"Cat1","author":"pino"},
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