var app = angular.module('kpiDefinitionManager').controller('formulaController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",KPIDefinitionFormulaControllerFunction ]);

function KPIDefinitionFormulaControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,items){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	$scope.currentKPI ={
			"formula": ""
	}
	$scope.measures = ['pippo', 'pluto','paperino'];
	$scope.dataSourceTable= {};//{"name":, "icon: }
	$scope.functionalities = ['SUM','MAX','MIN','COUNT'];
	$scope.selectedFunctionalities='SUM';
	$scope.measureFunctionalities={};
	$scope.token = "";
	$scope.measuresToJSON=[];
	$scope.functionsTOJSON=[];
	$scope.formula="";
	$scope.formulaDecoded="";
	$scope.formulaForDB = {};
	
	
	 
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

		_editor.on("keyup", function(cm,event,c){

			angular.copy(cm,$scope.cm);
			if(event.keyIdentifier!="U+0008" && event.keyIdentifier!="Left" && event.keyIdentifier!="Right"){
				var cur = cm.getCursor();
				var token = cm.getTokenAt(cur);
				if(token.string=="{" || token.string=="}" || token.string=="[" || token.string=="]"){

					cm.replaceRange("", {line:cm.getCursor().line, ch : token.start}, {line:cm.getCursor().line, ch : token.end+1})

				}else	if(token.type=="operator" || token.type=="bracket"){
					token.string = " ";
					cm.replaceRange(token.string, {line:cm.getCursor().line, ch : token.end})
					cm.replaceRange(" ", {line:cm.getCursor().line, ch : token.start})
				}

			}


		});



	 _editor.on("blur", function(cm,event,c){

			//$scope.parseFormula(cm);
		
			

		});
		_editor.on("mousedown", function(cm,event){
			//console.log(cm.getTokenAt(cm.coordsChar(event.pageX, event.pageY, "page")));
			
			for(var i=0;i<event.srcElement.classList.length;i++){
				$scope.token = event.srcElement.innerHTML;
				if(event.srcElement.classList[i]=="cm-m-max"){
					$scope.selectedFunctionalities='MAX';
					break;
				}else if(event.srcElement.classList[i]=="cm-m-min"){
					$scope.selectedFunctionalities='MIN';
					break;
				}else if(event.srcElement.classList[i]=="cm-m-count"){
					$scope.selectedFunctionalities='COUNT';
					break;
				}else if(event.srcElement.classList[i]=="cm-m-sum"){
					$scope.selectedFunctionalities='SUM';
					break;
				}
			}

			if(event.srcElement.className.startsWith("cm-keyword")) {
				$scope.ShowFunction(cm);
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

	$scope.checkError = function(cm,token){
		var flag=false;

		if($scope.measures.indexOf(token.string)==-1){
			//error word not present
			flag=true;
		}
		if (flag) 
			cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"error_word"})

			//angular.element(document.querySelectorAll(".CodeMirror-gutter-elt")).addClass("error_word fa fa-times-circle")
			angular.element(document.querySelectorAll(".CodeMirrorMathematica .CodeMirror-code span.error_word ")).attr("target","Measure Missing")

	}



	$scope.ShowFunction=function(cm){
		$scope.showAdvanced().then(function(response){

			var cur = cm.getCursor();
			//var cur = cm.coordsChar(h,t);

			var token = cm.getTokenAt(cur);

			while(token.type == "operator" || token.type == "bracket" ){
				cur.ch = cur.ch+1;
				token =  cm.getTokenAt(cur);
			}

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

			$scope.checkError(cm,token);
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
			locals: {items: deferred,token: $scope.token,selected:$scope.selectedFunctionalities }
		})
		.then(function(answer) {
			$scope.status = 'You said the information was "' + answer + '".';
			return deferred.resolve($scope.selectedFunctionalities);
		}, function() {
			$scope.status = 'You cancelled the dialog.';
		});
		return deferred.promise;
	};

	$scope.close = function(){
		$mdDialog.cancel();
		$scope.selectedFunctionalities="";
		items.resolve($scope.selectedFunctionalities);
	}
	$scope.apply = function(){
		$mdDialog.cancel();
		items.resolve($scope.selectedFunctionalities);
	}


	$scope.parseFormula=function(){
		$scope.reset();
		var countOpenBracket =0;
		var countCloseBracket =0;
		var cm =angular.element(document.getElementsByClassName("CodeMirror")[0])[0].CodeMirror;
		var flag = true;
		
		FORFirst: for(var i=0;i<cm.lineCount();i++){
			var array = $scope.removeSpace(cm.getLineTokens(i));
			for(var j=0;j<array.length;j++){
				var token = array[j];
				var arr = cm.findMarksAt({line:i,ch:token.end});
				if(token.string.trim()!=""){
					if(arr.length==0){
						if(j-1>=0){
							var token_before = array[j-1];
							if(token_before.type=="keyword"){
								if(token.type=="keyword" || token.type=="number"  || token.string=="("){
									var line = i+1;
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingoperator")+line);
									$scope.reset();
									break FORFirst;
								}
							}
							if(token_before.type=="operator" ){
								if(token.type=="operator" || token.string==")"){
									var line = i+1;
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.reset();
									break FORFirst;
								}
							}
							if(token_before.type=="bracket" ){
								var line = i+1;
								if(token.string==")" && token_before.string=="(" ){
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									break FORFirst;
								} if(token_before.string==")"){
									
									if(token.type=="keyword" || token.type=="number"){
										$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingoperator")+line);
										$scope.reset();
										flag=false;
										break FORFirst;
									}
								} 
							} if(token_before.string=="("){
								if(token.type=="operator"){
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.reset();
									break FORFirst;
								}

							}
						}
						if(j==array.length-1){
							//last token
							if(token.type=="operator"){
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
								$scope.reset();
								break FORFirst;
							}
						}
						if(token.type=="operator"){
							//operator
							if(j==0){
								var line = i+1;
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
								$scope.reset();
								break FORFirst;
							}else{
								$scope.formula = $scope.formula+token.string;
								$scope.formulaDecoded =$scope.formulaDecoded+token.string;
							}

						}else if(token.type=="bracket"){
							//bracket
							if(token.string=="("){
								countOpenBracket++;
							}else{
								countCloseBracket++;
							}
							$scope.formula = $scope.formula+token.string;
							$scope.formulaDecoded =$scope.formulaDecoded+token.string;
						}else if(token.type=="number"){
							$scope.formula = $scope.formula+token.string;
							$scope.formulaDecoded =$scope.formulaDecoded+token.string;
						}else{
							//error no function associated
							$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingfunctions"));
							$scope.reset();
							break FORFirst;
						}

					}else{
						//parse classes token
						for(var k=0;k<arr.length;k++){
							var className = arr[k]["className"];
							if(className=="cm-m-max"){
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("MAX")
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"MAX("+token.string+")";
							}else if(className=="cm-m-min"){
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("MIN");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"MIN("+token.string+")";
							}else if(className=="cm-m-count"){
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("COUNT");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"COUNT("+token.string+")";
							}else if(className=="cm-m-sum"){
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("SUM");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"SUM("+token.string+")";
							}else if(className=="error_word"){
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula"));

								$scope.reset();
								break FORFirst;
							}
						}
					}
				}


			}
		}
		if(countOpenBracket!=countCloseBracket && flag){
			$scope.reset();
			$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingbracket"));
		}else{
			if($scope.formula!="" && flag){
				$scope.showAction($scope.formulaDecoded);
				$scope.formulaForDB["formula"] =$scope.formula ;
				$scope.formulaForDB["measures"]=$scope.measuresToJSON;
				$scope.formulaForDB["functions"]=$scope.functionsTOJSON;
				$scope.formulaForDB["formulaDecoded"]=$scope.formulaDecoded;
				return $scope.formulaForDB;
			}

		}
		return null;
	}

	$scope.removeSpace=function(tokenList){
		for(var i=0;i<tokenList.length;i++){
			if(tokenList[i].type==null){
				tokenList.splice(i,1);
			}
		}
		return tokenList;
	}

	$scope.showAction = function(text) {
		var toast = $mdToast.simple()
		.content(text)
		.action('OK')
		.highlightAction(false)
		.hideDelay(3000)
		.position('top')

		$mdToast.show(toast).then(function(response) {

			if ( response == 'ok' ) {


			}
		});
	};

	$scope.reset = function(){
		$scope.measuresToJSON=[];
		$scope.functionsTOJSON=[];
		$scope.formula="";
		$scope.formulaDecoded="";
	}
	
	$scope.$on('parseEvent', function(e) {  
		 $scope.$parent.kpi = $scope.parseFormula();         
	    });
}
function DialogController($scope,$mdDialog,items,token,selected){
	$scope.selectedFunctionalities=selected;
	$scope.token = token;
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

