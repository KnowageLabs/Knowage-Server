angular.module('measureRoleManager').controller('measureRoleQueryController', [ '$scope','sbiModule_translate' ,'sbiModule_restServices',measureRoleQueryControllerFunction ]);

function measureRoleQueryControllerFunction($scope,sbiModule_translate,sbiModule_restServices){
	$scope.dataSourceTable= {};
	$scope.datasourcesList=[];
	
	
	
	CodeMirror.registerHelper(
		    "hint", "alias",
		    function (mirror, options) {
		    	var cur = mirror.getCursor();
		        var tok = mirror.getTokenAt(cur);
		        var start= tok.string.trim()==""? tok.start+1 : tok.start;
	        	var end= tok.end; 
	        	var hintList=[];
	        	for(var key in $scope.aliasList){
	        		 if(tok.string.trim()=="" || $scope.aliasList[key].name.startsWith(tok.string)){
	        			 hintList.push($scope.aliasList[key].name);
	        		 }
	        	} 
		        return {list:hintList, 
		        	from: CodeMirror.Pos(cur.line,start),
	                to: CodeMirror.Pos(cur.line, end)}
		    });
	
	CodeMirror.registerHelper(
		    "hint", "placeholder",
		    function (mirror, options) {
		    	var cur = mirror.getCursor();
		        var tok = mirror.getTokenAt(cur);
//		        var start= tok.string.trim()=="@"? tok.start+1 : tok.start;
		        var start= tok.start+1;
	        	var end= tok.end
	        	var str=tok.string.substring(1,tok.string.length); 
	        	var hintList=[];
	        	for(var key in $scope.placeholderList){
	        		 if(str=="" || $scope.placeholderList[key].name.startsWith(str)){
	        			 hintList.push($scope.placeholderList[key].name);
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
        extraKeys: {  "Ctrl-Space":   function(cm){  $scope.keyAssistFunc(cm) },
//        	"'@'":   function(cm){  $scope.keyAssistFunc(cm) }
        },
        hintOptions: {tables:$scope.dataSourceTable},
       };
	
	$scope.codemirrorLoaded =function(_editor){
		 _editor.on("keyup", function(cm,keyEv,c){
			 
			 	var cur = cm.getCursor();
		        var tok = cm.getTokenAt(cur);
		        if(tok.string=="@"){
		        	CodeMirror.showHint(cm, CodeMirror.hint.placeholder);
		        }
			 
			 });
		  
		 _editor.on("change", function(a,b,c){
			 $scope.detailProperty.queryChanged=true;
			 });
	}

	
	$scope.keyAssistFunc=function(cm){
		
        if($scope.isAliasCM(cm)){
        	CodeMirror.showHint(cm, CodeMirror.hint.alias);
        }else if($scope.isPlaceholderCM(cm)){
        	CodeMirror.showHint(cm, CodeMirror.hint.placeholder);
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
	
$scope.isPlaceholderCM=function(cm){
		var cursor = cm.getCursor();
		var token= cm.getTokenAt(cursor);
		if(token.string.startsWith("@")){
			return true;
		}
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
			$scope.detailProperty.dataSourcesIsSelected=true;
			angular.copy(response.data,$scope.dataSourceTable);
		},function(response){
			console.log("errore")
		});
	}
	
	
}