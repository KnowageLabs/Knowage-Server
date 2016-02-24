var app = angular.module('kpiDefinitionManager').controller('formulaController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q",KPIDefinitionFormulaControllerFunction ]);

function KPIDefinitionFormulaControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	$scope.currentKPI ={
			"formula": ""
	}
	$scope.measures = ['pippo', 'pluto'];
	$scope.dataSourceTable= {};//{"name":, "icon: }
	$scope.functionalities = ['SUM','MAX','MIN','COUNT'];
	$scope.selectedFunctionalities='MAX';
	$scope.measureFunctionalities={};

	CodeMirror.registerHelper(
			"hint", "measures",
			function (mirror, options) {
				var cur = mirror.getCursor();
				var tok = mirror.getTokenAt(cur);
				var start= tok.string.trim()==""? tok.start+1 : tok.start;
				var end= tok.end;

				var hintList=[];
				for(var i=0;i< $scope.measures.length;i++){

					if(tok.string.trim()=="" || $scope.measures[i].startsWith(tok.string)){
						hintList.push($scope.measures[i]);
						
					}

				}

				return {list:hintList, 
					from: CodeMirror.Pos(cur.line,start),
					to: CodeMirror.Pos(cur.line, end)}
			});
	
	$scope.codemirrorLoaded = function(_editor){
		
		_editor.on("dblclick", function(cm,event){
			console.log(cm.getTokenAt(cm.coordsChar(event.pageX, event.pageY, "page")));
		
			if(event.srcElement.className.startsWith("cm-keyword") && cm.getSelection().trim()!="") {
				$scope.ShowFunction(cm );
			}

		});
		_editor.on("mousedown", function(cm,event){
			console.log(cm.getTokenAt(cm.coordsChar(event.pageX, event.pageY, "page")));
			
			if(event.srcElement.className.startsWith("cm-keyword")) {
				var height = event.srcElement.offsetHeight;
				var top = event.srcElement.offsetTop;
				$scope.ShowFunction(cm,height,top );
			}
		});
	
	}

	$scope.codemirrorOptions = {
			mode: 'text/x-mathematica',
			indentWithTabs: true,
			smartIndent: true,
			lineWrapping : true,
			matchBrackets : true,
			autofocus: true,
			theme:"eclipse",
			lineNumbers: true, 
			gutters: ["CodeMirror-lint-markers"],
			lint: true,
			extraKeys: {  "Ctrl-Space":   function(cm){  $scope.keyAssistFunc(cm) }},

	};


	$scope.keyAssistFunc=function(cm){
		
		CodeMirror.showHint(cm, CodeMirror.hint.measures);
	}




	$scope.ShowFunction=function(cm,h,t){
		$scope.showAdvanced().then(function(response){
			var cur = cm.getCursor();
			//var cur = cm.coordsChar(h,t);
			
			var token = cm.getTokenAt(cur);
			while(token.string.trim() ==""){
				cur.ch = cur.ch+1;
				token =  cm.getTokenAt(cur);
			}
			//cm.markTextAt({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end}).clear();
			if(response!=""){
				var arr = cm.findMarksAt({line:cm.getCursor().line,ch:token.end});
				for(var i=0;i<arr.length;i++){
					arr[i].clear();
				}
			}
			if(response=="MAX"){
				//if add atomic:true it is not modificable
				//angular.element(document.querySelectorAll(".CodeMirror-code span.cm-keyword")).addClass("cm-m-max")
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-max"});
			}else if(response=="MIN"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-min"})
			}else if(response=="COUNT"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-count"})
			}else if(response=="SUM"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-sum"})
			}


		});
	}
	
	$scope.getMeasures=function(){
		sbiModule_restServices.get("1.0/kpi", 'listMeasure').success(
				function(data, status, headers, config) {
					if (data.hasOwnProperty("errors")) {
						console.log("layer non Ottenuti");
					} else {
						//	$scope.measures = data;
						console.log($scope.measures);
					}

				}).error(function(data, status, headers, config) {
					console.log("layer non Ottenuti " + status);

				})
	}

	$scope.getMeasures();

	$scope.showAdvanced = function() {
		var deferred = $q.defer();
		$mdDialog.show({
			controller: DialogController,
			templateUrl: 'dialog1.tmpl.html',
			clickOutsideToClose:true,
			preserveScope:true,
			locals: {items: deferred}
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return deferred.resolve($scope.selectedFunctionalities);
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	};

	$scope.checkError = function(cm){
		var widgets = []

		for (var i = 0; i < widgets.length; ++i)
			cm.removeLineWidget(widgets[i]);
		widgets.length = 0;

		
		for (var i = 0; i < cm.lineCount(); i++) {
			var line = cm.getDoc().getLine(i);
			var lineSplit =line.split(" ");
			var err={} ;
			for(var j=0;j<lineSplit.length;j++){
				if($scope.measures.indexOf(lineSplit[j])==-1){
					//error word not present
					err.word = lineSplit[j];
					err.reason="Word not present";
					err.line = i;
				}
			}
			
			if (!err) continue;
			var msg = document.createElement("div");
			msg.className = "lint-error";
			
			for(var k=0;k<angular.element(document.querySelectorAll(".CodeMirror-linenumber")).length;k++){
				if(angular.element(document.querySelectorAll(".CodeMirror-linenumber"))[k].innerHTML==i){
					var string = angular.element(document.querySelectorAll(".CodeMirror-linenumber"))[0].className;
					var patt = new RegExp(/ errorWord/);
		            if(!patt.test("errorWord")){
		            	angular.element(document.querySelectorAll(".CodeMirror-linenumber ")).append(msg)
		            }
				}
			}
			

		}
		
	}


}
function DialogController($scope,$mdDialog,items){
	$scope.selectedFunctionalities='MAX';
	$scope.close = function(){
		$mdDialog.cancel();
		$scope.selectedFunctionalities="";
		items.resolve($scope.selectedFunctionalities);
	}
	$scope.apply = function(){
		$mdDialog.cancel();
		items.resolve($scope.selectedFunctionalities);
	}
}

