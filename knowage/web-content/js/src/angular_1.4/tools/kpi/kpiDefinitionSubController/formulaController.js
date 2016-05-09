var app = angular.module('kpiDefinitionManager').controller('formulaController', ['$scope','sbiModule_translate' ,"$mdDialog","sbiModule_restServices","$q","$mdToast",'$angularListDetail','$timeout',KPIDefinitionFormulaControllerFunction ]);

function KPIDefinitionFormulaControllerFunction($scope,sbiModule_translate,$mdDialog, sbiModule_restServices,$q,$mdToast,$angularListDetail,$timeout){
	$scope.translate=sbiModule_translate;
	$scope.measureFormula="";
	$scope.currentKPI ={
			"formula": ""
	}
	$scope.dataSourceTable= {};
	$scope.functionalities = ['SUM','MAX','MIN','COUNT'];
	$scope.selectedFunctionalities='SUM';
	$scope.measureFunctionalities={};
	$scope.token = "";
	$scope.measuresToJSON=[];
	$scope.functionsTOJSON=[];
	$scope.formula="";
	$scope.formulaDecoded="";
	$scope.formulaSimple="";
	$scope.formulaForDB = {};
	$scope.kpiList=[];
	$scope.kpiListOriginal=[];
	

	CodeMirror.registerHelper(
			"hint", "measures",
			function (mirror, options) {
				var cur = mirror.getCursor();
				var tok = mirror.getTokenAt(cur);
				var start= tok.string.trim()==""? tok.start+1 : tok.start;
				var end= tok.end;

				var hintList=[];
				for(var i=0;i< $scope.measures.length;i++){

					if(tok.string.trim()=="" || $scope.measures[i].alias.startsWith(tok.string)){
						hintList.push($scope.measures[i].alias);
					}

				}
				return {list:hintList, 
					from: CodeMirror.Pos(cur.line,start),
					to: CodeMirror.Pos(cur.line, end)}
			});

	$scope.codemirrorLoaded = function(_editor){

		_editor.on("keyup", function(cm,event,c){
			if($scope.flagLoaded){
				$scope.formulaModified.value=true;
			}
			angular.copy(cm,$scope.cm);

			if(event.keyIdentifier!="U+0008" && event.keyIdentifier!="Left" && event.keyIdentifier!="Right"){
				var cur = cm.getCursor();
				var token = cm.getTokenAt(cur);

				if(token.string=="{" || token.string=="}" || token.string=="[" || token.string=="]"){

					cm.replaceRange("", {line:cm.getCursor().line, ch : token.start}, {line:cm.getCursor().line, ch : token.end+1})

				}else	if((token.type=="operator" || token.type=="bracket") && token.string!="_"){
					token.string = " ";
					cm.replaceRange(token.string, {line:cm.getCursor().line, ch : token.end})
					cm.replaceRange(" ", {line:cm.getCursor().line, ch : token.start})
				}

			}
		});



		_editor.on("mousedown", function(cm,event){
			event.srcElement = event.target || event.srcElement;
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

			if(event.srcElement.className.startsWith("cm-keyword") ||  event.srcElement.className.startsWith("cm-variable-2")) {
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

		if($scope.measureInList(token.string,$scope.measures)==-1){
			//error word not present
			flag=true;
		}
		if (flag) 
			cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"error_word"})

			angular.element(document.querySelectorAll(".CodeMirrorMathematica .CodeMirror-code span.error_word ")).attr("target","Measure Missing")
	}



	$scope.ShowFunction=function(cm){
		$scope.showAdvanced().then(function(response){

			var cur = cm.getCursor();
			var token = cm.getTokenAt(cur);

			while(token.string.trim() ==""){
				cur.ch = cur.ch+1;
				token =  cm.getTokenAt(cur);
			}

			while(token.type == "operator" || token.type == "bracket" || token.type == "number"){
				cur.ch = cur.ch+1;
				token =  cm.getTokenAt(cur);
			}


			while(token.string.trim() ==""){
				cur.ch = cur.ch+1;
				token =  cm.getTokenAt(cur);
			}

			if(response!=""){
				var arr = cm.findMarksAt({line:cm.getCursor().line,ch:token.end});
				for(var i=0;i<arr.length;i++){
					arr[i].clear();
				}
			}
			if(response=="MAX"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-max",atomic:true});
			}else if(response=="MIN"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-min",atomic:true})
			}else if(response=="COUNT"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-count",atomic:true})
			}else if(response=="SUM"){
				cm.markText({line:cm.getCursor().line,ch:token.start},{line:cm.getCursor().line,ch:token.end},{className:"cm-m-sum",atomic:true})
			}

			$scope.checkError(cm,token);
		});
	}

	$scope.getMeasures=function(){
		
		sbiModule_restServices.promiseGet("1.0/kpi", 'listMeasure')
		.then(function(response){ 

			$scope.measures=response.data;
		},function(response){
			$scope.errorHandler(response.data,"");
		});

	}

	$scope.getMeasures();

	$scope.loadKPI=function(item){
		var cm =angular.element(document.getElementsByClassName("CodeMirror")[0])[0].CodeMirror;

		$scope.flagLoaded = true;
		$timeout(function(){
			cm.refresh();
		},0)

		cm.setValue("");
		cm.clearHistory();

		$scope.kpi.definition =JSON.parse($scope.kpi.definition);
		cm.setValue($scope.kpi.definition.formulaSimple);
		$scope.changeIndexWithMeasures($scope.kpi.definition,cm);
		$angularListDetail.goToDetail();
	}
	$scope.changeIndexWithMeasures= function(formula,cm){
		var meas = formula.measures;
		var func = formula.functions;
		var count =0;
		FORFirst: for(var i=0;i<cm.lineCount();i++){
			var array = $scope.removeSpace(cm.getLineTokens(i));
			for(var j=0;j<array.length;j++){
				var token = array[j];
				if(token.type=="keyword" || token.type=="variable-2"){
					var className = func[count];
					count++;
					if(className=="MAX"){
						cm.markText({line:i,ch:token.start},{line:i,ch:token.end},{className:"cm-m-max"});
					}else if(className=="MIN"){
						cm.markText({line:i,ch:token.start},{line:i,ch:token.end},{className:"cm-m-min"});
					}else if(className=="SUM"){
						cm.markText({line:i,ch:token.start},{line:i,ch:token.end},{className:"cm-m-sum"});
					}else if(className=="COUNT"){
						cm.markText({line:i,ch:token.start},{line:i,ch:token.end},{className:"cm-m-count"});
					}

				}
			}
		}
	}

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
		var numMeasures=0;

		FORFirst: for(var i=0;i<cm.lineCount();i++){
			var line = i+1;
			var array = $scope.removeSpace(cm.getLineTokens(i));
			for(var j=0;j<array.length;j++){
				var token = array[j];
				var arr = cm.findMarksAt({line:i,ch:token.end});
				if(token.string.trim()!=""){
					if(arr.length==0){
						if(j-1>=0){
							var token_before = array[j-1];
							if(token_before.type=="keyword" || token_before.type=="variable-2"){
								if(token.type=="keyword" || token.type=="number" || token.type=="variable-2" || token.string=="("){
								//	var line = i+1;
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingoperator")+line);
									$scope.reset();
									$scope.selectedTab.tab=0;
									break FORFirst;

								}
							}
							if(token_before.type=="operator" ){
								if(token.type=="operator" || token.string==")"){
									
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.reset();
									$scope.selectedTab.tab=0;
									break FORFirst;
								}
							}
							if(token_before.type=="number" ){
								if(token.type=="number" || token.string=="(" || token.type=="keyword" || token.type=="variable-2"){
							//		var line = i+1;
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.reset();
									$scope.selectedTab.tab=0;
									break FORFirst;
								}
							}
							if(token_before.type=="bracket" ){
							//	var line = i+1;
								if((token.string==")" && token_before.string=="(")||(token.string=="(" && token_before.string==")") ){
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.selectedTab.tab=0;
									break FORFirst;
								} if(token_before.string==")"){

									if(token.type=="keyword" || token.type=="number" || token.type=="variable-2"){
										$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingoperator")+line);
										$scope.reset();
										flag=false;
										$scope.selectedTab.tab=0;
										break FORFirst;
									}
								} 
							} if(token_before.string=="("){
								if(token.type=="operator"){
									flag=false;
									$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
									$scope.reset();
									$scope.selectedTab.tab=0;
									break FORFirst;
								}

							}
						}
						if(j==array.length-1){
							//last token
							if(token.type=="operator"){
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
								$scope.reset();
								flag=false;
								$scope.selectedTab.tab=0;
								break FORFirst;
							}
						}
						if(token.type=="operator"){
							//operator
							if(j==0){
							//	var line = i+1;
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.malformed")+line);
								$scope.reset();
								flag=false;
								$scope.selectedTab.tab=0;
								break FORFirst;
							}else{
								$scope.formula = $scope.formula+token.string;
								$scope.formulaDecoded =$scope.formulaDecoded+token.string;
								$scope.formulaSimple=$scope.formulaSimple+" "+token.string+" ";
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
							$scope.formulaSimple=$scope.formulaSimple+" "+token.string+" ";
						}else if(token.type=="number"){
							$scope.formula = $scope.formula+token.string;
							$scope.formulaDecoded =$scope.formulaDecoded+token.string;
							$scope.formulaSimple=$scope.formulaSimple+token.string;
						}else{
							//error no function associated
							$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingfunctions"));
							$scope.reset();
							flag=false;
							$scope.selectedTab.tab=0;
							break FORFirst;
						}

					}else{
						if(j-1>=0){
							var token_before = array[j-1];
							if(token_before.type=="number" || token_before.type=="keyword" || token_before.type=="variable-2"){
						//		var line = i+1;
								flag=false;
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingoperator")+line);
								$scope.reset();
								$scope.selectedTab.tab=0;
								break FORFirst;
							}

						}
						//parse classes token
						for(var k=0;k<arr.length;k++){
							var className = arr[k]["className"];
							if($scope.measureInList(token.string,$scope.measures)==-1){
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula"));
								$scope.selectedTab.tab=0;
								$scope.reset();
								flag=false;
							}
							if(className=="cm-m-max"){
								numMeasures++;
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("MAX")
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"MAX("+token.string+")";
								$scope.formulaSimple=$scope.formulaSimple+token.string;
							}else if(className=="cm-m-min"){
								numMeasures++;
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("MIN");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"MIN("+token.string+")";
								$scope.formulaSimple=$scope.formulaSimple+token.string;
							}else if(className=="cm-m-count"){
								numMeasures++;
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("COUNT");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"COUNT("+token.string+")";
								$scope.formulaSimple=$scope.formulaSimple+token.string;
							}else if(className=="cm-m-sum"){
								numMeasures++;
								$scope.measuresToJSON.push(token.string);
								$scope.functionsTOJSON.push("SUM");
								var index =$scope.measuresToJSON.length-1;
								var string = "M"+index;
								$scope.formula=$scope.formula+string;
								$scope.formulaDecoded =$scope.formulaDecoded+"SUM("+token.string+")";
								$scope.formulaSimple=$scope.formulaSimple+token.string;
							}else if(className=="error_word"){
								$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula"));
								$scope.reset();
								flag=false;
								$scope.selectedTab.tab=0;
								break FORFirst;
							}
						}
					}
				}


			}
		}
		if(countOpenBracket!=countCloseBracket && flag){
			$scope.reset();
			$scope.selectedTab.tab=0;
			$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingbracket"));
		}else{
			if(numMeasures==0 && flag){
				$scope.reset();
				$scope.selectedTab.tab=0;
				$scope.showAction($scope.translate.load("sbi.generic.kpi.errorformula.missingmeasure"));
			}
			if($scope.formula!="" && flag){
				$scope.formulaForDB["formula"] =$scope.formula ;
				$scope.formulaForDB["measures"]=$scope.measuresToJSON;
				$scope.formulaForDB["functions"]=$scope.functionsTOJSON;
				$scope.formulaForDB["formulaDecoded"]=$scope.formulaDecoded;
				$scope.formulaForDB["formulaSimple"]=$scope.formulaSimple;
				$scope.showGUI=true;
				return $scope.formulaForDB;
			}

		}
		return {};
		$scope.selectedTab.tab=0;
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
		$scope.formulaSimple="";
		$scope.showGUI=false;
	}

	$scope.$on('parseEvent', function(e) { 
		$scope.kpi.definition = $scope.parseFormula();
		$scope.$parent.showGUI=$scope.showGUI;
		if($scope.showGUI){

			if($scope.kpi.id!=undefined){
				//modify
				$scope.$parent.activeSave="up";
			}else{
				//new
				$scope.$parent.activeSave= "add";
				$scope.kpi.definition = $scope.parseFormula(); 

			}

		}
		return;


	});


	$scope.$on('loadedEvent', function(e) {  

		$scope.loadKPI($scope.kpi)
	});
	$scope.$on('addEvent', function(e) {  
		var cm =angular.element(document.getElementsByClassName("CodeMirror")[0])[0].CodeMirror;
		$timeout(function(){
			cm.refresh();
		},0)
		cm.setValue("");
		cm.clearHistory();
	});


	$scope.indexInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.id==item.id){
				return i;
			}
		}

		return -1;
	};
	$scope.measureInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.alias==item){
				return i;
			}
		}

		return -1;
	};

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

