
var app = angular.module('functionsCatalogControllerModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col','file_upload','angular-list-detail','ngSanitize','ui.codemirror']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller('functionsCatalogController',["sbiModule_config","sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast","$log", "sbiModule_download","sbiModule_messaging","$sce",functionsCatalogFunction]);

function functionsCatalogFunction(sbiModule_config, sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$log,sbiModule_download,sbiModule_messaging,$sce){

	$scope.showDetail=false;
	$scope.shownFunction={};
	$scope.datasetLabelList=[];
	$scope.datasetNamesList=[];
	$scope.datasets=[];
	$scope.tableSelectedFunction={};
	$scope.tableSelectedFunction.language="Python";
	$scope.languages=["Python","R"];
	$scope.outputTypes=["Dataset","Text", "Image"];
	$scope.inputTypes=["Simple Input", "Dataset Input"]
	$scope.simpleInputs=[]; //=Input variables
	$scope.inputDatasets=[];	 
	$scope.varIndex=0;
	$scope.datasetsIndex=0;
	/*$scope.functionsList=[{"id":1,"name":"AVG","inputDatasets":[] , "inputVariables":[] , "outputItems":[], "language":"Python", "script": "b=1;\n"},
	                       {"id":2,"name":"Variance","inputDatasets":[] , "inputVariables":[], "outputItems":[] , "language":"R", "script": "c=2;\n"}];*/
	$scope.functionsList=[];
		
	$scope.newFunction={"id":"" ,"name":"","inputDatasets":[] , "inputVariables":[] , "outputItems":[],"language":"Python", "script":"","description":""};	
	$scope.cleanNewFunction=function()
	{
		$scope.newFunction={"id":"" ,"name":"","inputDatasets":[] , "inputVariables":[] , "outputItems":[], "language":"Python", "script":"","description":""};	
	}
	$scope.datasetLabelsList=[];
	$scope.saveOrUpdateFlag="";
	$scope.userId="";
	
	//For CodeMirror
    $scope.editorOptions = {
    	 lineWrapping : true,
         lineNumbers: true,	
         mode: 'python',
         autoRefresh:true         
        };
	
	
	$scope.myF = function(text){
		$scope.userId = text;
	}
	
	
	//Utility function
	

	
	$scope.showTabDialog = function(result) {
		   $mdDialog.show({
		     controller: functionCatalogResults,
		     templateUrl:  sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/functionsCatalog/templates/'+'functionCatalogResults.jsp',
		     //parent: angular.element(document.body),
		     //targetEvent: ev,
			 preserveScope : true,
		     locals : {
			 	results : result,
			 	logger:	$log
			 },
		     clickOutsideToClose:true
		   });
	};
	
	
	$scope.showNewInputDialog= function(data,datasetList)
	{
			$log.info("userId --------------------------> "+$scope.userId);
		    var executionResult = $mdDialog.show({
			     controller: executeWithNewData,
			     templateUrl:  sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/functionsCatalog/templates/'+'functionCatalogNewInputs.jsp',
			     //parent: angular.element(document.body),
			     //targetEvent: ev,			     
				 preserveScope : true,
			     locals : {
				 	demoData : data,
				 	logger:	$log,
				 	datasets: datasetList,
				 	userId: $scope.userId
				 },
			     clickOutsideToClose:true
			   });
		    executionResult.then(function(response){   //positive response, given from $mdDialog.hide(..)
		    	$scope.showTabDialog(response); 
		    },function(data){							 //negative response, given from $mdDialog.close(..)
		    	
		    });
			$scope.obtainDatasetLabelsRESTcall();

	};
	
	
	
	function isEmpty(obj) {
		
		var hasOwnProperty = Object.prototype.hasOwnProperty;

	    // null and undefined are "empty"
	    if (obj == null) return true;

	    // Assume if it has a length property with a non-zero value
	    // that that property is correct.
	    if (obj.length > 0)    return false;
	    if (obj.length === 0)  return true;

	    // Otherwise, does it have any properties of its own?
	    // Note that this doesn't handle
	    // toString and valueOf enumeration bugs in IE < 9
	    for (var key in obj) {
	        if (hasOwnProperty.call(obj, key)) return false;
	    }

	    return true;
	}
	
	//--
	
	
	$scope.obtainCatalogFunctionsRESTcall=function()
	{		
		sbiModule_restServices.get("1.0/FunctionsCatalog","")
		.success(function(data)
		{			
				$log.info("Functions of the catalog returned", data);
				$scope.functionsList=data.functions;
				
		});			
	}	
	
	$scope.obtainDatasetLabelsRESTcall=function()
	{		
		sbiModule_restServices.get("2.0/datasets","listDataset")
		.success(function(datasets)
		{
			$log.info("Received Datasets ", datasets);			
				
			$scope.datasets=datasets;
			
			$scope.datasetsList=[];
			$scope.datasetLabelsList=[]; 
			$scope.datasetNamesList=[];
			for(d in datasets.item)
			{
				//$log.info("d[label]", datasets.item[d].name);			
				$scope.datasetLabelsList.push(datasets.item[d].label);
				$scope.datasetNamesList.push(datasets.item[d].name);
				
			}
			$log.info("Dataset labels list", $scope.datasetLabelsList);			
	
		}); 
	}
	
	
	 
	
	
	$scope.addFunction=function()
	{
		$scope.shownFunction=$scope.newFunction;
		$scope.showDetail=true;
		$scope.saveOrUpdateFlag="save"
	}

	$scope.saveFunction=function()
	{
		var body={};		

		if($scope.saveOrUpdateFlag=="save")
		{
			
			$log.info("Save operation");

			
			body=$scope.shownFunction;
			
			$log.info("Shown function to send with POST", body);
	
			
			sbiModule_restServices.post("1.0/FunctionsCatalog","insertCatalogFunction",body)
			.success(function(data)
			{			
					$log.info("Catalog Function Added!");
					$log.info("Function added to db with id: ",data);
					//$scope.functionsList.push(data);
					$scope.obtainCatalogFunctionsRESTcall();
					
					$scope.cleanNewFunction=function()
					{
						$scope.newFunction={"id":"" ,"name":"","inputDatasets":[] , "inputVariables":[] , "outputItems":[], "language":"Python", "script":"", "description":""};	
					}
					$scope.shownFunction=$scope.newFunction;
	
			});
		}
		else if($scope.saveOrUpdateFlag=="update")
		{
			$log.info("Update operation");
			body=$scope.shownFunction;
			functionId=$scope.shownFunction.id;
			$log.info("Shown function to send with PUT", body);
	

			sbiModule_restServices.put("1.0/FunctionsCatalog","updateCatalogFunction/"+functionId,body)
			.success(function(data)
			{			
					$log.info("Catalog Function Updated!");
					$log.info("Message returned: ",data);
					//$scope.functionsList.push(data); 
					$scope.obtainCatalogFunctionsRESTcall();
					
			}); 
			
			
			
			
		}	
		
		
		
	}
	
	
	$scope.acSpeedMenu= [
	                      {
	                    	  label:sbiModule_translate.load("Delete"),
	                    	  icon:'fa fa-trash',
	                    	  action:function(item,event){
	                    		  $scope.deleteFunction(item,event);
	                    	  }
	                      	},
		                    {
		                      label:sbiModule_translate.load("Execute Demo"),
		                      icon:'fa fa-play-circle',		 
		                      action:function(item,event){
		                    	  $scope.applyDemoItem(item,event);
		                      }
		                    },
		                    {
			                  label:sbiModule_translate.load("Execute with new data"),
			                  icon:'fa fa-play-circle',		 
			                  action:function(item,event){
			                  $scope.applyItem(item,event);
			                 }
		                    }
	                  
	                     ];
	
	$scope.deleteFunction=function(item,event){
 
		$scope.shownFunction=angular.copy(item);
		var functionId=$scope.shownFunction.id;
		
		sbiModule_restServices.get("1.0/FunctionsCatalog","deleteFunction/"+functionId)
		.success(function(data)
		{			
				$log.info("Catalog Function Deleted!");
				$log.info("Message returned: ",data);
				$scope.obtainCatalogFunctionsRESTcall();
				$scope.cleanNewFunction(); 
				$scope.shownFunction=$scope.newFunction;
				$scope.saveOrUpdateFlag="save";


		}); 
		
		
	};
	
	
	
	$scope.applyDemoItem=function(item,event){ 
		
		sbiModule_restServices.alterContextPath("/knowagedataminingengine");
		var functionId=item.id;
		
		$log.info("userId ", $scope.userId);

		//sbiModule_restServices.get("executeFunction",functionId+"/?user_id=biadmin&DOCUMENT_LABEL=PythonDoc&DOCUMENT_COMMUNITIES=%5B%5D&DOCUMENT_IS_VISIBLE=true&SBI_EXECUTION_ROLE=admin&SBICONTEXT=%2Fknowage&DOCUMENT_FUNCTIONALITIES=%5B2%5D&SBI_COUNTRY=IT&DOCUMENT_AUTHOR=biadmin&DOCUMENT_DESCRIPTION=&document=1&IS_TECHNICAL_USER=true&SBI_SPAGO_CONTROLLER=%2Fservlet%2FAdapterHTTP&language=en&country=US&SBI_LANGUAGE=it&DOCUMENT_NAME=PythonDoc&NEW_SESSION=TRUE&DOCUMENT_IS_PUBLIC=true&DOCUMENT_VERSION=5&SBI_HOST=http%3A%2F%2Flocalhost%3A8080&SBI_ENVIRONMENT=DOCBROWSER&SBI_EXECUTION_ID=b9d02267227211e68ccb75d9d507deea&timereloadurl=1464178682338")
		//sbiModule_restServices.get("executeFunction",functionId+"/?user_id="+$scope.userId)
	
		//"http://localhost:8080/knowagedataminingengine/restful-services/executeFunction/9/?user_id=biadmin"

		
		//sbiModule_restServices.post("1.0/functionexecution","url")
		sbiModule_restServices.get("executeFunction",functionId+"/?user_id="+$scope.userId)
		.success(function(results)
		{
			$log.info("Execution o function "+ functionId+" started, result:", results);			
			//$scope.trustedHtml = $sce.trustAsHtml(result);
			//sbiModule_restServices.alterContextPath("/knowagedataminingengine");
			//CatalogCommad keyword riservata per chiamare i servizi del catalogo
			//sbiModule_restServices.get("1.0/result","CatalogCommand/A/true")
			//.success(function(result2)
			//{
			//	$log.info("OUTPUT DATAMINING RESULT:", result2);	
				
			//});
			
			$scope.showTabDialog(results);  //$event
			
			
					
		});
		
		sbiModule_restServices.alterContextPath("/knowage");
		
	};
	
	
	
	$scope.applyItem=function(item,event){ 
		
		$log.info("Execute with new data operation");

		//body=$scope.shownFunction;  //cambiare
		demoData=item;
		
		$log.info("Demo data ", demoData);
		$scope.showNewInputDialog(demoData,$scope.datasets);

		
	};
	
	
	
	
	$scope.leftTableClick=function(item)
	{
		$scope.showDetail=true;
		$scope.shownFunction=angular.copy(item);
		//$scope.shownFunction=item;
		$scope.cleanNewFunction();
		$log.info("ShownFunction: ",$scope.shownFunction);
		$scope.saveOrUpdateFlag="update";

	}
	/*
	$scope.addInputItem=function()
	{
		$scope.cleanNewFunction();
		var inputItem={};

		//Mi procuro la lista di label dei dataset da mostrare nel caso il tipo di inputItem fosse SpagoBI Dataset
		
		sbiModule_restServices.get("2.0/datasets","listDataset")
		.success(function(datasets)
		{
			$log.info("Received Datasets ", datasets);			
				
			$scope.datasetsList=[];
			for(d in datasets.item)
			{
				$log.info("d[label]", datasets.item[d].name);			
				$scope.datasetsList.push(datasets.item[d].label);
				
			}
			$log.info("Datasets list", $scope.datasetsList);			

		});
			
		
		$scope.shownFunction.inputItems.push(inputItem);
		$log.info("Added an input ",$scope.shownFunction.inputItems);
		return inputItem;
	}
	*/
	
	$scope.addInputDataset=function() 
	{
		$scope.cleanNewFunction();
		var inputDataset={};

		//Mi procuro la lista di label dei dataset da mostrare nel caso il tipo di inputItem fosse SpagoBI Dataset
	
		$scope.shownFunction.inputDatasets.push(inputDataset);
		$log.info("Added an input Dataset ",$scope.shownFunction.inputDatasets);
		return inputDataset;
	}
	
	$scope.addInputVariable=function() 
	{
		$scope.cleanNewFunction();
		var inputVariable={};

		//Mi procuro la lista di label dei dataset da mostrare nel caso il tipo di inputItem fosse SpagoBI Dataset
	
		$scope.shownFunction.inputVariables.push(inputVariable);
		$log.info("Added an input Variable ",$scope.shownFunction.inputVariables);
		return inputVariable;
	}
	
	
	$scope.removeInputDataset=function(inputDataset)
	{
		var index=$scope.shownFunction.inputDatasets.indexOf(inputDataset);		
		$scope.shownFunction.inputDatasets.splice(index, 1);
		$log.info("Removed an input Dataset ",$scope.shownFunction.inputDatasets);
	}
	
	$scope.removeInputVariable=function(inputVariable)
	{
		var index=$scope.shownFunction.inputVariables.indexOf(inputVariable);		
		$scope.shownFunction.inputVariables.splice(index, 1);
		$log.info("Removed an input Variable ",$scope.shownFunction.inputVariables);
	}
	
	
	$scope.addDatasetFunc=function()
	{

	}
	
	$scope.removeDatasetFunc=function(dataset) 
	{		

		
	}
	
	$scope.addOutputItem=function()
	{
		var output={};
		$scope.shownFunction.outputItems.push(output);
		$log.info("Added an outputItem ",$scope.shownFunction.outputItems);
		return output;	
	}
	
	$scope.removeOutputItem=function(output)
	{		
		var index=$scope.shownFunction.outputItems.indexOf(output);		
		$scope.shownFunction.outputItems.splice(index, 1);
		$log.info("Removed an output Item ",$scope.shownFunction.output);
		
	}
	
	//----------------------------------------------Application Logic-----------------------------------------
	

	$scope.obtainDatasetLabelsRESTcall();
	$scope.obtainCatalogFunctionsRESTcall();
	

	function functionCatalogResults($scope, $mdDialog, logger,results)
	{
		logger.info("received results: ",results);
		$scope.numTab=results.length;
		$scope.results=results;
		for (var res in $scope.results) {
			  if ($scope.results.hasOwnProperty(res)) { 
			    //logger.info("res: " + res + " value: " + $scope.results[res])
			    $scope.results[res].imageString="data:image/png;base64," +$scope.results[res].result;
			    //logger.info($scope.results[res].imageString);
			  }
			}
		
		
		
	};

	function executeWithNewData($scope, $mdDialog, logger,demoData,datasets,userId)
	{
		logger.info("received demo function data: ", demoData);
		//$scope.numTab=results.length;
		$scope.demoData=demoData;
		$scope.datasets=datasets;
		$scope.functionId=demoData.id;
		$scope.userId=userId;
		logger.info("HAVING DATASETS: ", $scope.datasets);
		$scope.replacingDatasetList={};
		$scope.replacingVariableValues={};
		$scope.replacingDatasetOutLabels={};
		$scope.replacingTextOutLabels={};
		$scope.replacingImageOutLabels={};
		
		
		
		$scope.getDatasetNameByLabel=function (label,datasetList)
		{
			for (var d in datasetList.item) {
				  if (datasetList.item.hasOwnProperty(d)) { 
					  var datasetObj=datasetList.item[d];
					  if(datasetObj.label==label)
						  return datasetObj.name;
				  }
			}
		}	
		
		/*
		for (var res in $scope.results) {
			  if ($scope.results.hasOwnProperty(res)) { 
			    //logger.info("res: " + res + " value: " + $scope.results[res])
			    $scope.results[res].imageString="data:image/png;base64," +$scope.results[res].result;
			    logger.info($scope.results[res].imageString);
			  }
			}
		*/
		
		$scope.executeFunction=function()
		{
			var body=[];

			var obj={},obj2={},obj3={},obj4={},obj5={};
			obj.type="variablesIn";
			obj.items=$scope.replacingVariableValues;
			body.push(obj);
			 
			obj2.type="datasetsIn";
			obj2.items=$scope.replacingDatasetList;
			body.push(obj2);
			
			obj3.type="datasetsOut";
			obj3.items=$scope.replacingDatasetOutLabels;
			body.push(obj3);
			
			obj4.type="textOut";
			obj4.items=$scope.replacingTextOutLabels;
			body.push(obj4);
			
			obj5.type="imageOut";
			obj5.items=$scope.replacingImageOutLabels;
			body.push(obj5);
						
			logger.info("body: ", body);
				
			
			sbiModule_restServices.alterContextPath("/knowagedataminingengine");
			sbiModule_restServices.post("executeFunctionWithNewData",$scope.functionId+"/?user_id="+$scope.userId,body)
			.success(function(executionResult)
			{			
					$log.info("Catalog Function executed with new data!!");
					$log.info("Execution result ", executionResult);
					
					$mdDialog.hide(executionResult);

					
					/*
					//$scope.functionsList.push(data);
					$scope.obtainCatalogFunctionsRESTcall();
					
					$scope.cleanNewFunction=function()
					{
						$scope.newFunction={"id":"" ,"name":"","inputDatasets":[] , "inputVariables":[] , "outputItems":[], "language":"Python", "script":"", "description":""};	
					}
					$scope.shownFunction=$scope.newFunction;
					*/
			});
			
			

		}

		
		
		
		
	};

	

	
};










