var app = angular.module('measureRoleManager', [ 'ngMaterial',  'angular_table' ,'sbiModule', 'angular-list-detail','ui.codemirror']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {

    $mdThemingProvider.theme('knowage')

$mdThemingProvider.setDefaultTheme('knowage');
}]);

app.controller('measureRoleMasterController', [ '$scope','sbiModule_translate' ,measureRoleMasterControllerFunction ]);
app.controller('measureListController', [ '$scope','sbiModule_translate','$mdDialog' ,measureListControllerFunction ]);
app.controller('measureDetailController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureDetailControllerFunction ]); 

function measureRoleMasterControllerFunction($scope,sbiModule_translate){
	$scope.translate=sbiModule_translate;
	$scope.newMeasureFunction=function(){
		
	} 
	
	$scope.aliasList=["pippo","pino","pippino"];
}
 
function measureDetailControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.dataSourceTable= {};
	$scope.selectedDatasource={};
	$scope.dataSourcesIsSelected=false;
	$scope.datasourcesList=[];
	$scope.measureQuery="SELECT\n\nFROM\n\nWHERE";// "SELECT  usr.name FROM users AS usr WHERE usr.score>100";
	
	
	CodeMirror.registerHelper(
		    "hint", "alias",
		    function (mirror, options) {
//		        CodeMirror.commands.autocomplete(mirror, CodeMirror.hint.ajax, { async: true })
		    	var cur = mirror.getCursor();
		        var tok = mirror.getTokenAt(cur);
		        var start= tok.string.trim()==""? tok.start+1 : tok.start;
	        	var end= tok.end;
	        	
	        	var hintList=[];
	        	for(var i=0;i< $scope.aliasList.length;i++){
	        		 if(tok.string.trim()=="" || $scope.aliasList[i].startsWith(tok.string)){
	        			 hintList.push($scope.aliasList[i]);
	        		 }
	        	}
		        
		        return {list:hintList, 
		        	from: CodeMirror.Pos(cur.line,start),
	                to: CodeMirror.Pos(cur.line, end)}
		    })
	
	$scope.codemirrorOptions = {
			mode: 'text/x-mysql',
			indentWithTabs: true,
		    smartIndent: true,
        lineWrapping : true,
        matchBrackets : true,
        autofocus: true,
        theme:"eclipse",
        lineNumbers: true, 
        extraKeys: {  "Ctrl-Space":   function(cm){  $scope.keyAssistFunc(cm) }},
        hintOptions: {tables:$scope.dataSourceTable}
		};
	
	

	
	$scope.keyAssistFunc=function(cm){
		var isAlias=$scope.isAliasCM(cm);
        if(isAlias){
        	CodeMirror.showHint(cm, CodeMirror.hint.alias);
        }else{
        	CodeMirror.showHint(cm, CodeMirror.hint.autocomplete);
        }
	}
	
	$scope.isAliasCM=function(cm){
		
		var cursor = cm.getCursor();
		var token= cm.getTokenAt(cursor);
		
		if(token.string.trim()!=""){
			var tmpCursor=CodeMirror.Pos(cursor.line,token.start);
			tmpCursor.ch=token.start;
			token=cm.getTokenAt(tmpCursor);
		}
		
		var beforeCursor=CodeMirror.Pos(cursor.line,token.start);
		beforeCursor.ch=token.start;
		var beforeToken=cm.getTokenAt(beforeCursor);
		
		if(beforeToken.string.toLowerCase()=="as"){
			var text= cm.getDoc().getRange(CodeMirror.Pos(0,0),beforeCursor);
			  var patt = new RegExp("^select ((?!FROM).)* AS$","ig");
			if(!patt.test(text.replace(/\n/g, " "))){
				return false;
			}else{
				return true;
			}
		}
     return false;
	}
	
	
	$scope.loadDatasources=function(){
		sbiModule_restServices.promiseGet("datasources","")
		.then(function(response){
			angular.copy(response.data.root,$scope.datasourcesList);
		},function(response){
			console.log("errore")
		});
	}
	$scope.loadDatasources();
	
	$scope.alterDatasource=function(datasrc){
		sbiModule_restServices.promiseGet("2.0/datasources","structure/"+datasrc)
		.then(function(response){
			$scope.dataSourcesIsSelected=true;
			angular.copy(response.data,$scope.dataSourceTable);
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