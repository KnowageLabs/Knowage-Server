
var app = angular.module('functionsCatalogControllerModule',['ngMaterial', 'angular_list', 'angular_table','sbiModule', 'angular_2_col','file_upload','angular-list-detail','ngSanitize','ui.codemirror']);
app.config(['$mdThemingProvider', function($mdThemingProvider) {
    $mdThemingProvider.theme('knowage')
    $mdThemingProvider.setDefaultTheme('knowage');
 }]);
app.controller('functionsCatalogController',["sbiModule_config","sbiModule_translate", "sbiModule_restServices", "$scope", "$mdDialog", "$mdToast","$log", "sbiModule_download","sbiModule_messaging","$sce",functionsCatalogFunction]);

function functionsCatalogFunction(sbiModule_config, sbiModule_translate, sbiModule_restServices, $scope, $mdDialog, $mdToast,$log,sbiModule_download,sbiModule_messaging,$sce){

	$scope.showDetail=false;
	$scope.shownFunction={"language":"Python"};
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
	$scope.isAdmin="";
	
	//For CodeMirror
    $scope.editorOptions = {
    	 lineWrapping : true,
         lineNumbers: true,	
         mode: $scope.shownFunction.language.toLowerCase(),
         autoRefresh:true 
        };
	
    	
	$scope.myF = function(text){
		$scope.userId = text;
	} 
	
	
	//Utility function
	

	
	$scope.showTabDialog = function(result,isDemoExecution) {
		   $mdDialog.show({
		     controller: functionCatalogResultsController,
		     templateUrl:  sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/functionsCatalog/templates/'+'functionCatalogResults.jsp',
		     //parent: angular.element(document.body),
			 preserveScope : true,
		     locals : {
			 	results : result,
			 	logger:	$log,
			 	translate:sbiModule_translate,
			 	isDemo:isDemoExecution
			 },
		     clickOutsideToClose:true
		   });
	};
	
	
	$scope.showNewInputDialog= function(data,datasetList,isDemoFunction)
	{
			$log.info("userId  --------------------------> "+$scope.userId);
			$log.info("ISADMIN --------------------------> "+$scope.isAdmin);

		    var executionResult = $mdDialog.show({
			     controller: executeWithNewDataController,
			     templateUrl:  sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/functionsCatalog/templates/'+'functionCatalogNewInputs.jsp',
			     //parent: angular.element(document.body),
			     //targetEvent: ev,			     
				 preserveScope : true,
			     locals : {
				 	demoData : data,
				 	logger:	$log,
				 	datasets: datasetList,
				 	userId: $scope.userId,
				 	translate:sbiModule_translate
				 },
			     clickOutsideToClose:true
			   });
		    executionResult.then(function(response){   //positive response, given from $mdDialog.hide(..)
		    	$scope.showTabDialog(response,isDemoFunction); 
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

		
		if(!$scope.checkCorrectArguments())
		{
		    $mdDialog.show(
		    	      $mdDialog.alert()
		    	        .parent(angular.element(document.querySelector('#popupContainer')))
		    	        .clickOutsideToClose(true)
		    	        .title('Some fields are not filled!!')
//		    	        .textContent('Fill missing informations to save function.')
		    	        .ariaLabel('Alert Dialog Demo')
		    	        .ok('OK')
		    	    );
		}
		else
		{
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
						$scope.obtainCatalogFunctionsRESTcall();
						
				}); 
				
				
				
				
			}	
		}
		
		
	}
	
	$scope.checkCorrectArguments=function()
	{
		//$scope.newFunction={"id":"" ,"name":"","inputDatasets":[] , "inputVariables":[] , "outputItems":[], "language":"Python", "script":"", "description":""};	

		for(var i=0;i<$scope.shownFunction.inputDatasets.length;i++)
		{
				if($scope.shownFunction.inputDatasets[i].label==undefined)
				{
					return false;
				}
		}
		for(var i=0;i<$scope.shownFunction.inputVariables.length;i++)
		{
				if($scope.shownFunction.inputVariables[i].name==undefined||$scope.shownFunction.inputVariables[i].value==undefined)
				{
					return false;
				}
		}
		for(var i=0;i<$scope.shownFunction.outputItems.length;i++)
		{
				if($scope.shownFunction.outputItems[i].label==undefined||$scope.shownFunction.outputItems[i].type==undefined)
				{
					return false;
				}
		}
		if($scope.shownFunction.description==""||$scope.shownFunction.description=="")
		{
			return false;
		}	
		return true; 
	}
	
	
	$scope.acSpeedMenu= [
		                    {
		                      label:sbiModule_translate.load("Execute Demo"),
		                      icon:'fa fa-play-circle-o',		 
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
	
	
	
    var deleteIcon={
    		label:sbiModule_translate.load("Delete"),
    		icon:'fa fa-trash',
    		action:function(item,event){
    			$scope.deleteFunction(item,event);
    		}
    }
    if(isAdminGlobal)
    {
    	$scope.acSpeedMenu.push(deleteIcon);
    }	
	
	
	
	
	
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
		
//		sbiModule_restServices.alterContextPath("/knowagedataminingengine");
		sbiModule_restServices.alterContextPath(sbiModule_config.dataMiningContextName);
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
			var isDemo=true;
			$scope.showTabDialog(results,isDemo);  
					
		});
		
//		sbiModule_restServices.alterContextPath("/knowage");
		sbiModule_restServices.alterContextPath(sbiModule_config.contextName);
		
	};
	
	
	
	$scope.applyItem=function(item,event){ 
		
		$log.info("Execute with new data operation");

		//body=$scope.shownFunction; 
		demoData=item;
		var isDemo=false;
		
		$log.info("Execution Data ", demoData);
		$scope.showNewInputDialog(demoData,$scope.datasets,isDemo);

		
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
	
	$scope.datasetPreview=function(datasetLabel)
	{
		sbiModule_restServices.post("1.0/datasets",datasetLabel+"/content")
		.success(function(dataset)
		{
			$log.info("Received Dataset ", dataset);			
			$scope.showDatasetPreviewDialog(dataset,datasetLabel);	
				
		}); 
			
		
		
	}
	
	
	
	
	$scope.showDatasetPreviewDialog= function(datasetToSee,labelDS)
	{
			var executionResult = $mdDialog.show({
			     controller: datasetPreviewController,
			     templateUrl:  sbiModule_config.contextName	+ '/js/src/angular_1.4/tools/functionsCatalog/templates/'+'datasetPreview.jsp',		     
				 preserveScope : true,
			     locals : {
				 	logger:	$log,
				 	dataset: datasetToSee,
				 	datasetLabel: labelDS, 
				 	translate: sbiModule_translate
				 },
			     clickOutsideToClose:true
			   });
		    
	};
	
	
	
	//----------------------------------------------Application Logic-----------------------------------------
	
	
	
	
	$scope.obtainDatasetLabelsRESTcall();
	$scope.obtainCatalogFunctionsRESTcall();
	
	//----------------------------------------------Controllers-----------------------------------------------

	function functionCatalogResultsController($scope, $mdDialog, logger,results,translate,isDemo)
	{
		function isObject(obj)
		{
			  return obj === Object(obj);
		}
		
		logger.info("received results: ",results);
		$scope.numTab=results.length;
		$scope.results=results;
		$scope.translate=translate;
		$scope.isDemo=isDemo;
		$scope.truncate=false;
		$scope.error="";
		$scope.dataset = {rows:[]}; 
		for (var res in $scope.results) {
			  if ($scope.results.hasOwnProperty(res)) { 
			    //logger.info("res: " + res + " value: " + $scope.results[res])
				  if($scope.results[res].resultType=="image"||$scope.results[res].resultType=="Image")
			      {
					  $scope.results[res].imageString="data:image/png;base64," +$scope.results[res].result;
			      }
			    //logger.info($scope.results[res].imageString);
				  if($scope.results[res].resultType=="dataset"||$scope.results[res].resultType=="Dataset"||$scope.results[res].resultType=="spagobi_ds")
				  {
					  	var datasetLabel=$scope.results[res].result;
						sbiModule_restServices.post("1.0/datasets",datasetLabel+"/content")
						.success(function(dataset)
						{
							$log.info("Received Dataset ", dataset);			
							
							for(var i=0;i<dataset.rows.length;i++)
							{
								delete dataset.rows[i].id;
							}	
							
							$scope.dataset=dataset;
							$scope.datasetLabel=datasetLabel;
							if($scope.dataset.rows.length>10)
							{
								$scope.truncate=true;
							}	
							$scope.dataset.rows=$scope.dataset.rows.slice(0,9);
							
							

							$scope.headers=[];
							if(!isObject($scope.dataset.metaData.fields[0])) //in case first field is "recNO", skip it
							{
								i=1;
							}
							else
							{
								i=0;
							}
							
							for(i;i<$scope.dataset.metaData.fields.length;i++) 
							{
								if($scope.dataset.metaData.fields[i].header!=undefined && $scope.dataset.metaData.fields[i].header!="")
								{	
									var colToInsert={};
									colToInsert["label"]=$scope.dataset.metaData.fields[i].header;
									colToInsert["name"]=$scope.dataset.metaData.fields[i].name;
									$scope.headers.push(colToInsert);
								}

							}	

							
						}
						
						
						); 
				  }	
				  if(res=="errors")
				  {
					$scope.error=$scope.results.errors[0].localizedMessage+"\n"+$scope.results.errors[0].message;	
				  }	
			  }
			}
		
		
		
	};

	function executeWithNewDataController($scope, $mdDialog, logger,demoData,datasets,userId,translate)
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
		$scope.translate=translate;
		
		
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
			

		
		
		$scope.isExecuteDisabled=function()
		{
			disabled=false;
//			$log.info("replacingDatasetList:",$scope.replacingDatasetList);
//			$log.info("replacingVariableValues",$scope.replacingVariableValues);
//			$log.info("replacingDatasetOutLabels",$scope.replacingDatasetOutLabels);
			//$scope.replacingTextOutLabels NOT USED, DEMO LABELS VALUES USED 
			//$scope.replacingImageOutLabels NOT USED, DEMO LABELS VALUES USED 
			


			function isEmpty(obj) {
				
				// Speed up calls to hasOwnProperty
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
			
			
			// The output are generic outputItems with a Type field that distinguish them, in contrast with the input, that are divided in inputDatasets and inputVariables
			// in future make 3 lists of outputs (outputImages, outputDatasets and outputText) instead of outputItems (also in the jsp view!!) 
			$scope.numDSout=0;
			function classifyOutput()
			{
				for(var i=0;i<$scope.demoData.outputItems.length;i++)
				{
					if(demoData.outputItems[i].type=="Dataset"||demoData.outputItems[i].type=="dataset"||demoData.outputItems[i].type=="spabobi_ds")
					{
						$scope.numDSout=$scope.numDSout+1;
					}	
					
				}
				return $scope.numDSout;
			}
			
			 $scope.numDSout=classifyOutput();
			
			
			if((isEmpty($scope.replacingDatasetList) && $scope.demoData.inputDatasets.length!=0)||(isEmpty($scope.replacingVariableValues) && $scope.demoData.inputVariables.length!=0)||(isEmpty($scope.replacingDatasetOutLabels) && $scope.numDSout!=0))
			{
				disabled=true;
			}
			else
			{
				for (var property in $scope.replacingDatasetList) {
				    if ($scope.replacingDatasetList.hasOwnProperty(property)) {
				    	if(isEmpty($scope.replacingDatasetList[property]))
				    	{				
				    		disabled=true;
				    	}	
				    }
				}
				for (var property in $scope.replacingVariableValues) {
				    if ($scope.replacingVariableValues.hasOwnProperty(property)) {
				    	if(isEmpty($scope.replacingVariableValues[property]))
				    	{				
				    		disabled=true;
				    	}					  
				    }
				}
				for (var property in $scope.replacingDatasetOutLabels) {
				    if ($scope.replacingDatasetOutLabels.hasOwnProperty(property)) {
				    	if(isEmpty($scope.replacingDatasetOutLabels[property]))
				    	{				
				    		disabled=true;
				    	}
				    }
				}
				
			}	
			return disabled;
		}
		
		
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
				
			
//			sbiModule_restServices.alterContextPath("/knowagedataminingengine");
			sbiModule_restServices.alterContextPath(sbiModule_config.dataMiningContextName);
			sbiModule_restServices.post("executeFunctionWithNewData",$scope.functionId+"/?user_id="+$scope.userId,body)
			.success(function(executionResult)
			{			
					$log.info("Catalog Function executed with new data!!");
					$log.info("Execution result ", executionResult);
					
					$mdDialog.hide(executionResult);
			});
			
			

		}

					
	};

	function datasetPreviewController($scope, $mdDialog, logger,dataset,datasetLabel,translate)
	{
		function isObject(obj)
		{
			  return obj === Object(obj);
		}
				
		logger.info("Preview Controller, received dataset: ", dataset);
		//$scope.numTab=results.length;
		for(var i=0;i<dataset.rows.length;i++)
		{
			delete dataset.rows[i].id;
		}	
		$scope.dataset=dataset;
		$scope.truncate=false;
		$scope.datasetLabel=datasetLabel;
		$scope.translate=translate;
		if($scope.dataset.rows.length>10)
		{
			$scope.truncate=true;
		}	
		$scope.dataset.rows=$scope.dataset.rows.slice(0,9);
		
		
		$scope.headers=[];
		if(!isObject($scope.dataset.metaData.fields[0])) //in case first field is "recNO", skip it
		{
			i=1;
		}
		else
		{
			i=0;
		}
		
		for(i;i<$scope.dataset.metaData.fields.length;i++) 
		{
			if($scope.dataset.metaData.fields[i].header!=undefined && $scope.dataset.metaData.fields[i].header!="")
			{	
				var colToInsert={};
				colToInsert["label"]=$scope.dataset.metaData.fields[i].header;
				colToInsert["name"]=$scope.dataset.metaData.fields[i].name;
				$scope.headers.push(colToInsert);
			}

		}	
	}

	
};










