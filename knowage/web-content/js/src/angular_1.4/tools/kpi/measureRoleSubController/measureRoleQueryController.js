angular.module('measureRoleManager').controller('measureRoleQueryController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureRoleQueryControllerFunction ]);

function measureRoleQueryControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.dataSourceTable= {};
	$scope.selectedDatasource={};
	$scope.dataSourcesIsSelected=false;
	$scope.datasourcesList=[];
	
	
	
	CodeMirror.registerHelper(
		    "hint", "alias",
		    function (mirror, options) {
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
		    });
	
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
			//^((.*\)\s*select)|(\s*select)) ((?!FROM).)* AS$
			//^select ((?!FROM).)* AS$
			//^((.*[\)]\s*select)|(\s*select)) ((?!FROM).)* AS$
			  
			  
			var patt = new RegExp(/^((.*\)\s*select)|(\s*select)) ((?!FROM).)* AS$/ig);
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