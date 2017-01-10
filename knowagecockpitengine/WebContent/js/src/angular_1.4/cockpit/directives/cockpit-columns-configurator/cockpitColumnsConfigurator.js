(function () {
	angular.module('cockpitModule')
	.directive('cockpitColumnsConfigurator',function(cockpitModule_widgetServices,$mdDialog,$mdSidenav){

		return {
			templateUrl: baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsConfiguratorTemplate.jsp',
			controller: cockpitColumnsConfiguratorControllerFunction,
			compile: function (tElement, tAttrs, transclude) {
				return {
					pre: function preLink(scope, element, attrs, ctrl, transclud) {
					},
					post: function postLink(scope, element, attrs, ctrl, transclud) {

					}
				};
			}
		};
	});

	function cockpitColumnsConfiguratorControllerFunction($scope,$mdDialog,cockpitModule_datasetServices,$mdToast,cockpitModule_widgetConfigurator,sbiModule_restServices,sbiModule_translate,sbiModule_config,$mdSidenav,$q,cockpitModule_generalOptions){
		$scope.translate=sbiModule_translate;
		$scope.availableDatasets=cockpitModule_datasetServices.getAvaiableDatasets();

		if(!$scope.model.content.modalselectioncolumn){
			$scope.model.content.modalselectioncolumn="";
		}
		
		if(!$scope.model.sortingColumnAlias){
			$scope.model.sortingColumnAlias = undefined;
		}
		if(!$scope.model.sortingOrder){
			$scope.model.sortingOrder = "ASC";
		}
		
		$scope.selectedColumn;
		$scope.lastId = -1;
		if($scope.model.dataset == undefined){
			$scope.model.dataset = {};
		}
		$scope.showCircularcolumns = {value :false};
		$scope.resetValue = function(dsId){
			$scope.lastId = $scope.model.dataset.dsId;
			if($scope.lastId==-1 || $scope.lastId!=dsId){
				$scope.showCircularcolumns = {value : true};
				$scope.safeApply();
				$scope.model.dataset.dsId = dsId;
				//simulate on change
				$scope.local = {};
				if($scope.model.dataset.dsId !=-1){
					angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.local); 
					$scope.model.content.columnSelectedOfDataset  = [];
					for(var i=0;i<$scope.local.metadata.fieldsMeta.length;i++){
						var obj = $scope.local.metadata.fieldsMeta[i];
						obj["aggregationSelected"] = "SUM";
						obj["funcSummary"] = "SUM";
						obj["aliasToShow"] = obj.alias;
						$scope.model.content.columnSelectedOfDataset.push(obj);
					}
					$scope.lastId=$scope.model.dataset.dsId;
					$scope.showCircularcolumns ={value : false};
					$scope.safeApply();
				}else{
					$scope.model.content.columnSelectedOfDataset = [];
				}
				$scope.model.sortingColumnAlias = undefined;
			}	
		}

		$scope.safeApply=function(){
			if ($scope.$root.$$phase != '$apply' && $scope.$root.$$phase !='$digest') {
				$scope.$apply();
			}
		}
		$scope.colorPickerProperty={format:'rgb'}


		$scope.actionsOfCockpitColumns = [
		                                  {
		                                	  icon:'fa fa-calculator' ,   
		                                	  action : function(item,event) {
		                                		  $scope.addNewCalculatedField(item);
		                                	  },
		                                	  visible : function(row,column) {
		                                		  if(row.isCalculated){
		                                			  return true;
		                                		  }
		                                		  return false;
		                                	  }
		                                  } ,
		                                  {
		                                	  icon:'fa fa-sliders' ,   
		                                	  action : function(item,event) {
		                                		  $scope.addSummaryInfo(item);
		                                	  },
		                                	  visible : function(row,column) {
		                                		  if(row.fieldType == "MEASURE"){
		                                			  return true;
		                                		  }
		                                		  return false;
		                                	  }
		                                  } ,
		                                  {
		                                	  icon:'fa fa-trash' ,   
		                                	  action : function(item,event) {	
//		                                		  var confirm = $mdDialog.confirm();

//		                                		  confirm.title("Are you sure..?")
//		                                		  confirm.content("This item will be removed")
//		                                		  confirm.ariaLabel('delete column')
//		                                		  confirm.ok("YES")
//		                                		  confirm.cancel("NO");
//		                                		  $mdDialog.show(confirm).then(function() {
		                                		  var index=$scope.model.content.columnSelectedOfDataset.indexOf(item);
		                                		  $scope.model.content.columnSelectedOfDataset.splice(index,1);
//		                                		  }, function() {
//		                                		  });
		                                	  }
		                                  } 
		                                  ];
		$scope.metadataTableColumns=[
		                             {
		                            	 label:"  ",
		                            	 name:"move",
		                            	 size:"100px",
		                            	 transformer:function(item){
		                            		 var template = "<div layout=\"row\"> " 
		                            			 +"<md-button ng-click=\"scopeFunctions.moveUp($event,$parent.$parent.$parent.$index)\" ng-disabled=\"$parent.$parent.$parent.$parent.$parent.$index==0\" class=\"md-icon-button h20 \" aria-label=\"up\">" 
		                            			 +"  <md-icon md-font-icon=\"fa fa-arrow-up\"></md-icon>" 
		                            			 +" </md-button>" 
		                            			 +" <md-button ng-click=\"scopeFunctions.moveDown($event,$parent.$parent.$parent.$index)\" ng-disabled=\"$parent.$parent.$parent.$parent.$parent.$last\" class=\"md-icon-button h20\" aria-label=\"down\">" 
		                            			 +" <md-icon md-font-icon=\"fa fa-arrow-down\"></md-icon>" 
		                            			 +"</md-button>" 
		                            			 +"</div>";
		                            		 return template;
		                            	 },
		                            	 hideTooltip:true
		                             },
		                             {
		                            	 "label":"Column Name",
		                            	 "name":"alias",

		                            	 hideTooltip:true

		                             },
		                             {
		                            	 "label":"Title",
		                            	 "name":"aliasToShow",
		                            	 transformer:function(item){
		                            		 var template = "<md-input-container flex class=\"md-block\"> "
		                            			 +"<label>Text</label>"
		                            			 +"<input class=\"input_class\" ng-model=row.aliasToShow />" 
		                            			 +"</md-input-container>";
		                            		 return template;
		                            	 },
		                            	 hideTooltip:true

		                             },

		                             {
		                            	 "label":"Aggregation",
		                            	 "name":"aggregation",
		                            	 transformer:function(a,b,c){
		                            		 var template='<md-input-container class="md-block"> '
		                            			 +'<md-select  ng-show="scopeFunctions.canSee(row)" ng-if="scopeFunctions.AggregationFunctions != undefined" ng-model="row.aggregationSelected" aria-label="aria-label" >'		       
		                            			 +'<md-option ng-repeat="agF in scopeFunctions.AggregationFunctions" ng-value="agF.value">'
		                            			 +'{{agF.label}}'
		                            			 +'</md-option>'
		                            			 +'</md-select></md-input-container>';

		                            		 return template;
		                            	 },
		                            	 hideTooltip:true
		                             },
		                             {
		                            	 "label":"Type",
		                            	 "name":"typeList",
		                            	 transformer:function(){

		                            		 var temp = '<md-input-container class="md-block"> '
		                            			 +'<md-select aria-label="aria-label" ng-model="row.fieldType">'
		                            			 +'<md-option value=""></md-option>'
		                            			 +'<md-option value="ATTRIBUTE">String</md-option>'
		                            			 +'<md-option value="MEASURE">Number</md-option>'
		                            			 +'</md-select> </md-input-container>'
		                            			 return temp;
		                            	 },
		                            	 hideTooltip:true
		                             },{
		                            	 "label":" ",
		                            	 "name":" ",
		                            	 transformer:function(row,column,index){

		                            		 var temp = '<md-button class="md-icon-button" style="background:{{row.style.background}}" ng-click="scopeFunctions.draw(row,column,index)">'
		                            			 +'<md-icon style="color:{{row.style.color}}" md-font-icon="fa fa-paint-brush" aria-label="Paintbruh"></md-icon>'
		                            			 +'</md-button>'

		                            			 return temp;
		                            	 },
		                            	 size : "40px",
		                            	 hideTooltip:true
		                             }
		                             ];


		$scope.functionsCockpitColumn={ 
				translate:sbiModule_translate,
				moveUp: function(evt,index){
					$scope.model.content.columnSelectedOfDataset.splice(index-1, 0, $scope.model.content.columnSelectedOfDataset.splice(index, 1)[0]);

				},
				moveDown: function(evt,index){
					$scope.model.content.columnSelectedOfDataset.splice(index+1, 0, $scope.model.content.columnSelectedOfDataset.splice(index, 1)[0]);

				},
				canSee : function(row){				
					return angular.equals(row.fieldType, "MEASURE");
				},
				typeList: [{"code":"java.lang.String", "name":"String"},{"code":"java.lang.Integer", "name":"Number"},{"code":"java.math.BigDecimal", "name":"Number"}],
				getColor :function(){
					return $scope.selectedColumn.style !=undefined ? $scope.selectedColumn.style.color : "";
				},
				getBackground: function(){
					return $scope.selectedColumn.style !=undefined ?  $scope.selectedColumn.style.background : "";
				},
				draw :function(row,column,index) {
					$scope.selectedColumn = row;
					//  $mdSidenav("columnStyleTab").toggle();
					$mdDialog.show({
						templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnStyle.html',
						parent : angular.element(document.body),
						clickOutsideToClose:true,
						escapeToClose :true,
						preserveScope: true,
						autoWrap:false,
						fullscreen: true,
						locals:{model:$scope.model, selectedColumn : $scope.selectedColumn},
						controller: cockpitStyleColumnFunction

					}).then(function(answer) { 			
						console.log("Selected column:", $scope.selectedColumn);

					}, function() {
						console.log("Selected column:", $scope.selectedColumn);
					});

				},
				AggregationFunctions: cockpitModule_generalOptions.aggregationFunctions,

		}

		$scope.openListColumn = function(){
			if($scope.model.dataset==undefined || $scope.model.dataset.dsId == undefined){
				$scope.showAction($scope.translate.load("sbi.cockpit.table.missingdataset"));	
			}else{
				$mdDialog.show({
					templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitColumnsOfDataset.html',
					//parent: $scope.cockpitWidgetItem,
					parent : angular.element(document.body),
					clickOutsideToClose:true,
					escapeToClose :true,
					preserveScope: true,
					autoWrap:false,
					locals: {model:$scope.model, getMetadata : $scope.getMetadata},
					fullscreen: true,
					controller: controllerCockpitColumnsConfigurator
				}).then(function(answer) {
				}, function() {
				});
			}
		}

		$scope.addSummaryInfo = function(currentRow){
			var deferred = $q.defer();
			var promise ;
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitSummaryInfo.html',
				//parent: $scope.cockpitWidgetItem,
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {items: deferred,model:$scope.model, getMetadata : $scope.getMetadata, actualItem : currentRow},
				fullscreen: true,
				controller: controllerCockpitSummaryInfo
			}).then(function(answer) {
				deferred.promise.then(function(result){	
					console.log(result);
					currentRow.funcSummary = result.funcSummary;
				});
			}, function() {
			});
			promise =  deferred.promise;
			
			
		}

		$scope.addNewCalculatedField = function(currentRow){

			var deferred = $q.defer();
			var promise ;
			$mdDialog.show({
				templateUrl:  baseScriptPath+ '/directives/cockpit-columns-configurator/templates/cockpitCalculatedFieldTemplate.html',
				//parent: $scope.cockpitWidgetItem,
				parent : angular.element(document.body),
				clickOutsideToClose:true,
				escapeToClose :true,
				preserveScope: true,
				autoWrap:false,
				locals: {items: deferred,model:$scope.model, getMetadata : $scope.getMetadata, actualItem : currentRow},
				fullscreen: true,
				controller: controllerCockpitCalculatedFieldController
			}).then(function(answer) {
				deferred.promise.then(function(result){	
					if(currentRow != undefined){
						currentRow.aliasToShow = result.alias;
						currentRow.formula = result.formula;
						currentRow.formulaArray = result.formulaArray;
						currentRow.alias = result.alias;
					}else{
						$scope.model.content.columnSelectedOfDataset.push(result);

					}
				});
			}, function() {
			});
			promise =  deferred.promise;

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
		}
	}

})();



function controllerCockpitColumnsConfigurator($scope,sbiModule_translate,$mdDialog,model,getMetadata,cockpitModule_datasetServices){
	$scope.translate=sbiModule_translate;
	$scope.model = model;
	$scope.columnSelected = [];
	$scope.localDataset = {};
	if($scope.model.dataset!=undefined && $scope.model.dataset.dsId != undefined){
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset);
	} else{
		$scope.model.dataset= {};
		angular.copy([], $scope.model.dataset.metadata.fieldsMeta); 
	}
	$scope.saveColumnConfiguration=function(){
		model = $scope.model;

		if(model.content.columnSelectedOfDataset == undefined){
			model.content.columnSelectedOfDataset = [];
		}
		for(var i=0;i<$scope.columnSelected.length;i++){
			var obj = $scope.columnSelected[i];
			obj.aggregationSelected = 'SUM';
			obj["funcSummary"] = "SUM";
			obj.typeSelected = $scope.columnSelected[i].type;
			obj.label = $scope.columnSelected[i].alias;
			obj.aliasToShow = $scope.columnSelected[i].alias;
			model.content.columnSelectedOfDataset.push(obj);
		}

		$mdDialog.hide();
	}
	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}
}

function cockpitStyleColumnFunction($scope,sbiModule_translate,$mdDialog,model,selectedColumn,cockpitModule_datasetServices,$mdToast){
	$scope.translate=sbiModule_translate;
	$scope.selectedColumn = angular.copy(selectedColumn);
	$scope.fontWeight = ['normal','bold','bolder','lighter','number','initial','inherit'];
	$scope.colorPickerProperty={placeholder:sbiModule_translate.load('sbi.cockpit.color.select') ,format:'rgb'}
	$scope.visTypes=['Chart','Text','Chart & Text', 'Text & Chart','Icon only'];
	
	if(!$scope.selectedColumn.hasOwnProperty('colorThresholdOptions'))
	{	
		$scope.selectedColumn.colorThresholdOptions={};
		$scope.selectedColumn.colorThresholdOptions.condition=[];
		for(var i=0;i<3;i++)
		{
			$scope.selectedColumn.colorThresholdOptions.condition[i]="none";
		}
	}	
	
	
	if($scope.selectedColumn.visType==undefined)
	{
		$scope.selectedColumn.visType="Text";
	}	
	if($scope.selectedColumn.minValue==undefined||$scope.selectedColumn.minValue===''||$scope.selectedColumn.maxValue==undefined||$scope.selectedColumn.maxValue==='')
	{
		$scope.selectedColumn.minValue=0;
		$scope.selectedColumn.maxValue=100;
	}	
	if($scope.selectedColumn.chartColor==undefined||$scope.selectedColumn.chartColor==='')
	{	
		$scope.selectedColumn.chartColor="rgb(19, 30, 137)";
	}
	if($scope.selectedColumn.chartLength==undefined||$scope.selectedColumn.chartLength==='')
	{
		$scope.selectedColumn.chartLength=200;
	}

                        
	$scope.conditions=['none','>','<','=','>=','<=','!='];
	if($scope.selectedColumn.scopeFunc==undefined)
	{	
		$scope.selectedColumn.scopeFunc={conditions:$scope.conditions, condition:[{condition:'none'},{condition:'none'},{condition:'none'},{condition:'none'}]};  
	}
	//------------------------- Threshold icon table -----------------------------	
	var conditionString0="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[0].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString1="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[1].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString2="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[2].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var conditionString3="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[3].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"

		
	var valueString0="<md-input-container class='md-block' ng-if='scopeFunctions.condition[0].condition!=undefined && scopeFunctions.condition[0].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[0].value' type='number' required> </md-input-container>";	
	var valueString1="<md-input-container class='md-block' ng-if='scopeFunctions.condition[1].condition!=undefined && scopeFunctions.condition[1].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[1].value' type='number' required> </md-input-container>";	
	var valueString2="<md-input-container class='md-block' ng-if='scopeFunctions.condition[2].condition!=undefined && scopeFunctions.condition[2].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[2].value' type='number' required> </md-input-container>";	
	var valueString3="<md-input-container class='md-block' ng-if='scopeFunctions.condition[3].condition!=undefined && scopeFunctions.condition[3].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[3].value' type='number' required> </md-input-container>";	

		
	$scope.thresholdsList=
		[{priority:0, icon:"<md-icon style='color:red'  md-font-icon='fa fa-exclamation-circle' ng-init='scopeFunctions.condition[0].iconColor=\"red\";	scopeFunctions.condition[0].icon=\"fa fa-exclamation-circle\"'></md-icon>",condition:conditionString0,	value:valueString0},{priority:1 , icon:"<md-icon style='color:red'	md-font-icon='fa fa-times-circle' ng-init='scopeFunctions.condition[1].iconColor=\"red\"; scopeFunctions.condition[1].icon=\"fa fa-times-circle\"'></md-icon>",condition:conditionString1, value:valueString1},	{priority:2 , icon:"<md-icon style='color:yellow'  md-font-icon='fa fa-exclamation-triangle' ng-init='scopeFunctions.condition[2].iconColor=\"yellow\"; scopeFunctions.condition[2].icon=\"fa fa-exclamation-triangle\"'></md-icon>",condition:conditionString2, value:valueString2},{priority:3 , icon:"<md-icon style='color:green'  md-font-icon='fa fa-check-circle' ng-init='scopeFunctions.condition[3].iconColor=\"green\";	scopeFunctions.condition[3].icon=\"fa fa-check-circle\"'></md-icon>",condition:conditionString3, value:valueString3}];	
	$scope.tableColumns=[{label:"Icon",name:"icon", hideTooltip:true},{label:"Condition",name:"condition", hideTooltip:true},{label:"Value",name:"value", hideTooltip:true}];

	//$scope.selectedColumn.conditions=$scope.scopeFunc.condition;
	
	//----------------------- Cell color table ------------------------------------
	
	var condString0="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[0].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var condString1="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[1].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"
	var condString2="	<md-input-container class='md-block'> 	<md-select ng-model='scopeFunctions.condition[2].condition'>	<md-option ng-repeat='cond in scopeFunctions.conditions' value='{{cond}}'>{{cond}}</md-option>	</md-select> </md-input-container>"

	var valString0="<md-input-container class='md-block' ng-if='scopeFunctions.condition[0].condition!=undefined && scopeFunctions.condition[0].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[0].value' type='number' required> </md-input-container>";	
	var valString1="<md-input-container class='md-block' ng-if='scopeFunctions.condition[1].condition!=undefined && scopeFunctions.condition[1].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[1].value' type='number' required> </md-input-container>";	
	var valString2="<md-input-container class='md-block' ng-if='scopeFunctions.condition[2].condition!=undefined && scopeFunctions.condition[2].condition!=\"none\"' flex>	<input class='input_class'  ng-model='scopeFunctions.condition[2].value' type='number' required> </md-input-container>";	

			
	$scope.cellColorThresholdsList=[{priority:0, color:"<md-input-container class=\"md-block\">  <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[0].value \"></color-picker>  </md-input-container>",condition:condString0, value:valString0},{priority:1 , color:"<md-input-container class=\"md-block\"> <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[1].value \"></color-picker></md-input-container>",condition:condString1, value:valString1},{priority:2 , color:"<md-input-container class=\"md-block\"> <color-picker  options=\"{format:'rgb'}\" ng-model=\"scopeFunctions.colorCondition[2].value \"></color-picker></md-input-container>",condition:condString2, value:valString2}];		
	$scope.cellColorTableColumns=[{label:"Color",name:"color", hideTooltip:true},{label:"Condition",name:"condition", hideTooltip:true},{label:"Value",name:"value", hideTooltip:true}];
	
	//----------------------------------------------------------------------------
	
	
	

	$scope.cleanStyleColumn = function(){
		$scope.selectedColumn.style = undefined;
	}
	$scope.saveColumnStyleConfiguration = function(){
		angular.copy($scope.selectedColumn,selectedColumn)

		$mdDialog.cancel();
	}

	$scope.cancelcolumnStyleConfiguration = function(){
		$mdDialog.cancel();
	}
	
	
	$scope.checkIfDisable = function(){
		
		if($scope.selectedColumn.selectThreshold==true)
		{	
			if($scope.selectedColumn.threshold==undefined||$scope.selectedColumn.threshold=="")
			{
				return true;
			}				
		}	
		
		if($scope.selectedColumn.maxValue==undefined || $scope.selectedColumn.minValue==undefined || $scope.selectedColumn.maxValue==="" || $scope.selectedColumn.minValue==="")
		{
			return true;
		}
		
		for(var i=0;i<$scope.selectedColumn.scopeFunc.condition.length;i++)
		{
			if($scope.selectedColumn.scopeFunc.condition[i].condition!=undefined && $scope.selectedColumn.scopeFunc.condition[i].condition!="none")
			{
				if($scope.selectedColumn.scopeFunc.condition[i].value==="" || $scope.selectedColumn.scopeFunc.condition[i].value==undefined)
				{
					return true;
				}	
			}	
		}
		return false;
	}
	
}
function controllerCockpitSummaryInfo($scope,sbiModule_translate,$mdDialog,items,model,getMetadata,actualItem,cockpitModule_datasetServices,$mdToast,cockpitModule_generalOptions){
	$scope.translate=sbiModule_translate;
	$scope.model = model;
	$scope.row  =actualItem;
	
	$scope.listType = cockpitModule_generalOptions.aggregationFunctions;
	
	$scope.saveColumnConfiguration=function(){

		items.resolve($scope.row);
		$mdDialog.hide();
	}
	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

}
function controllerCockpitCalculatedFieldController($scope,sbiModule_translate,$mdDialog,items,model,getMetadata,actualItem,cockpitModule_datasetServices,$mdToast){
	$scope.translate=sbiModule_translate;
	$scope.model = model;
	$scope.localDataset = {};
	$scope.formula = "";
	$scope.formulaElement = [];

	if($scope.model.dataset!=undefined && $scope.model.dataset.dsId != undefined){
		//load all measures
		angular.copy(cockpitModule_datasetServices.getDatasetById($scope.model.dataset.dsId), $scope.localDataset); 
	} 

	$scope.column = {};
	$scope.measuresList = [];
	$scope.operators = ['+','-','*','/'];
	$scope.brackets = ['(',')'];


	$scope.checkInput=function(event){
		console.log(event);
		if(event.key == "Backspace"){
			event.preventDefault();
			$scope.deleteLast();
		}
		else if(event.key=="+" || event.key=="-" || event.key=="/" ||  event.key=="*" ){
			event.preventDefault();
			$scope.addOperator(event.key);
		}else if (event.char=="+" || event.char=="-" || event.char=="/" ||  event.char=="*"){
			//internet explorer
			event.preventDefault();
			$scope.addOperator(event.char);
		}
		else if(event.key=="(" || event.key==")" ){
			event.preventDefault()
			$scope.addBracket(event.key);
		}else if(event.char=="(" || event.char==")"){
			//internet explorer
			event.preventDefault()
			$scope.addBracket(event.char);
		}

		var reg = new RegExp("[0-9\.\,]+");
		if(reg.test(event.key)){
			if($scope.formulaElement.length>0){
				var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
				if(lastObj.type=='measure'){
					$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
					event.preventDefault();
					return;
				}
			}
			var obj = {};
			obj.type = 'number';
			obj.value = event.key;
			$scope.formulaElement.push(obj);
			$scope.formula = $scope.formula +""+event.key+"";
			event.preventDefault();
		} else {
			event.preventDefault();
		}

	}

	//load all columns SELECTED of type measure

	for(var i=0;i<$scope.localDataset.metadata.fieldsMeta.length;i++){
		var obj = $scope.localDataset.metadata.fieldsMeta[i];
		if(obj.fieldType == 'MEASURE'){
			$scope.measuresList.push(obj);
		}
	}


	$scope.reloadValue = function(){
		$scope.formulaElement = angular.copy(actualItem.formulaArray);
		$scope.column.alias = angular.copy(actualItem.aliasToShow);
		$scope.redrawFormula();
	}

	$scope.saveColumnConfiguration=function(){
		if($scope.formulaElement.length>0){
			var obj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(obj.type=='operator'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula1'));
				return;
			}
		}
		if(!$scope.checkBrackets()){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula5'));
			return;
		}
		$scope.result = {};
		$scope.result.alias = $scope.column.alias != undefined ? $scope.column.alias : "NewCalculatedField";
		$scope.result.formulaArray = $scope.formulaElement;
		$scope.result.formula = $scope.formula;
		$scope.result.aggregationSelected = 'SUM';
		$scope.result["funcSummary"] = "SUM";
		$scope.result.aliasToShow = $scope.result.alias;
		$scope.result.fieldType = 'MEASURE';
		$scope.result.isCalculated = true;
		$scope.result.type = "java.lang.Integer";
		items.resolve($scope.result);
		$mdDialog.hide();
	}
	$scope.cancelConfiguration=function(){
		$mdDialog.cancel();
	}

	$scope.checkBrackets = function(){
		var countOpenBrackets = 0;
		var countCloseBrackets = 0;
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type == 'bracket'){
				if(obj.value == '('){
					countOpenBrackets++;
				} else {
					countCloseBrackets++;
				}
			}
		}

		if(countOpenBrackets != countCloseBrackets){
			return false;
		}
		return true;
	}
	$scope.addOperator= function(op){
		if($scope.formulaElement.length==0){
			$scope.showAction('Select a measure before.');
			return;
		}
		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj.type=='operator'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula2'));
			return;
		}
		var obj = {};
		obj.type = 'operator';
		obj.value = op;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +" "+op+" ";
	}
	$scope.addBracket= function(br){

		var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
		if(lastObj !=undefined && lastObj.type=='measure' && br == '('){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		if(lastObj !=undefined && lastObj.type=='operator' && br == ')'){
			$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula4'));
			return;
		}
		var obj = {};
		obj.type = 'bracket';
		obj.value = br;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +" "+br+" ";
	}
	$scope.addMeasures =function(meas){
		if($scope.formulaElement.length>0){
			var lastObj = $scope.formulaElement[$scope.formulaElement.length-1];
			if(lastObj.type=='measure' || lastObj.type=='number'){
				$scope.showAction($scope.translate.load('sbi.cockpit.table.errorformula3'));
				return;
			}
		}
		var obj = {};
		obj.type = 'measure';
		obj.value = meas.alias;
		$scope.formulaElement.push(obj);
		$scope.formula = $scope.formula +' "'+meas.alias+'"';
	}
	$scope.deleteLast = function(){
		if($scope.formulaElement.length>0){
			$scope.formulaElement.pop();
			$scope.redrawFormula();
		}
	}
	$scope.redrawFormula = function(){
		$scope.formula = "";
		for(var i=0;i<$scope.formulaElement.length;i++){
			var obj = $scope.formulaElement[i];
			if(obj.type=="number"){
				$scope.formula = $scope.formula +""+obj.value+"";
			}else if(obj.type=="measure"){
				$scope.formula = $scope.formula +'"'+obj.value+'"';
			}else{
				$scope.formula = $scope.formula +" "+obj.value+" ";
			}

		}
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
	}
	if(actualItem !=undefined){
		$scope.reloadValue();
	}

}

