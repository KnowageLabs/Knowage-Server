angular.module('sbi_table_toolbar',[])
.directive('sbiTableToolbar',function(sbiModule_messaging){
	return{
		restrict:"E",
		replace: true,
		templateUrl:'/knowagewhatifengine/html/template/right/tableToolbar.html',
		controller: tableToolobarController
	}
});

function tableToolobarController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {

	var olapButtonNames = ["BUTTON_MDX","BUTTON_EDIT_MDX","BUTTON_FLUSH_CACHE","BUTTON_EXPORT_XLS"];
	var whatifButtonNames= ["BUTTON_VERSION_MANAGER", "BUTTON_EXPORT_OUTPUT", "BUTTON_UNDO", "BUTTON_SAVE", "BUTTON_SAVE_NEW","lock-other-icon","unlock-icon","lock-icon"];
	var tableButtonNames = ["BUTTON_FATHER_MEMBERS","BUTTON_HIDE_SPANS","BUTTON_SHOW_PROPERTIES","BUTTON_HIDE_EMPTY","BUTTON_CALCULATED_MEMBERS"]
	//var alwaysVisibleBtns = ["BUTTON_EXPORT_OUTPUT"];
	$scope.outputWizardDescription = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.type.description');
	$scope.outputWizardTitle = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.title');
	$scope.outputWizardTypeLabel = sbiModule_translate.load('sbi.olap.toolbar.export.wizard.type');
	$scope.outputTypes = [{name:'table',value:'table'},{name:'file',value:'csv'}]
	$scope.outputVersions = []; //TODO
	$scope.lockTooltip;
	$scope.delimiter = "|";
	$scope.tableName = "WHATIFOUTPUTTABLE";
	$scope.outputType = $scope.outputTypes.length > 0 ? $scope.outputTypes[0].value:'';
	$scope.outputVersion ;
	whatifToolbarButtonsVisible=[];
	$scope.lockerClass = "";
	$scope.showFile = false;
	$scope.showTable = false;
	var exportBtn = {};
	var result;
	
	whatIfBtns = function(status){
			if(status == 'locked_by_user')
				$scope.whatifToolbarButtons = whatifToolbarButtonsVisible;
			else{
				var btn;
				//var exists = $scope.whatifToolbarButtons.indexOf("BUTTON_EXPORT_OUTPUT")>-1;
				$scope.whatifToolbarButtons = [];
				
				if(exportBtn.img == "BUTTON_EXPORT_OUTPUT"){
					$scope.whatifToolbarButtons.push(exportBtn);
				}			
				
			}
				
	};
	
	$scope.lockFunction = function(){
		if(status == 'unlocked'){
			lockUnlock('lock', $scope.modelConfig.artifactId);
		}
		if(status == 'locked_by_user'){
			lockUnlock('unlock', $scope.modelConfig.artifactId);
		}
		//$scope.$apply();
	}
	
	filterXMLResult = function(res) {
		var regEx = /([A-Z]+_*)+/g;
		var i;
		result = res;

		while (i = regEx.exec(res)){
			var btn = {};
			btn.tooltip = sbiModule_translate.load("sbi.olap.toolbar."+ i[0]);// messageResource.get("sbi.olap.toolbar."+ i[0], 'messages');
			btn.img =i[0];//"../img/show_parent_members.png"// url(../img/show_parent_members.png);
			
			if(btn.img == "BUTTON_EXPORT_OUTPUT")
				exportBtn = btn;
			
			if(olapButtonNames.indexOf(btn.img)>-1)
				$scope.olapToolbarButtons.push(btn);
			else if(whatifButtonNames.indexOf(btn.img)>-1)
				$scope.whatifToolbarButtons.push(btn);
			else if(tableButtonNames.indexOf(btn.img)>-1)
				$scope.tableToolbarButtons.push(btn);
			
		}
		whatifToolbarButtonsVisible = $scope.whatifToolbarButtons;
		whatIfBtns(status);
	};
	
	filterXMLResult(toolbarVisibleBtns);
			
	$scope.btnFunctions = function(name){
		switch(name){
		case "BUTTON_FATHER_MEMBERS":
			$scope.modelConfig.showParentMembers = !$scope.modelConfig.showParentMembers;
			break;
		case "BUTTON_HIDE_SPANS":
			$scope.modelConfig.hideSpans = !$scope.modelConfig.hideSpans;
			break;
		case "BUTTON_SHOW_PROPERTIES":
			$scope.modelConfig.showProperties = !$scope.modelConfig.showProperties;
			break;
		case "BUTTON_HIDE_EMPTY":
			$scope.modelConfig.suppressEmpty = !$scope.modelConfig.suppressEmpty;
			break;
		case "BUTTON_EXPORT_OUTPUT":
			$scope.showExportDialog(null);
			break;
		case "BUTTON_FLUSH_CACHE":
			flushCache();
			break;	
		default:
			console.log("something else clicked");
		}
		$scope.sendModelConfig($scope.modelConfig);
	}

	$scope.enableDisableSorting = function(){
		
		$scope.sortDisable();
	}
	
	$scope.enableCompactProperties = function(){
		if($scope.modelConfig.showProperties != true){
			$scope.modelConfig.showCompactProperties = !$scope.modelConfig.showCompactProperties;
			$scope.sendModelConfig($scope.modelConfig);	
		}else{
			sbiModule_messaging.showErrorMessage("Table Properties are on..");
		}
	}
	
	
	$scope.changeDrillType = function(type){
		$scope.modelConfig.drillType = type;
		$scope.sendModelConfig($scope.modelConfig);
	}
	
	$scope.isDisabledType = function(name){
		if($scope.modelConfig != undefined){
			if($scope.modelConfig.drillType == name)
				return true;
			else
				return false;
		}
	}
	
	$scope.enableCrossNavigation = function() {
    	sbiModule_restServices.promiseGet
		("1.0",'/crossnavigation/initialize/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
		.then(function(response) {
			$scope.handleResponse(response);
			$scope.crossNavigationEnabled = !$scope.crossNavigationEnabled;
		 }, function(response) {
			sbiModule_messaging.showErrorMessage("error", 'Error');
		});
    }
	 
	 $scope.sortDisable = function(){
		 
		 sbiModule_restServices.promiseGet("1.0","/member/sort/disable?SBI_EXECUTION_ID="+JSsbiExecutionID)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sorting", 'Error');
				
			});	
		  }
	 
	  $scope.sort = function(axisToSort,axis,positionUniqueName){
		  
		  var path; 
		  
		  if($scope.selectedSortingMode==='count'){
			  var path = '/member/sort/'+axisToSort+'/'+axis+'/'+positionUniqueName+'/'+$scope.selectedSortingMode+'/'+$scope.sortingCount+'?SBI_EXECUTION_ID='+JSsbiExecutionID;
		  }else {
			  var path = '/member/sort/'+axisToSort+'/'+axis+'/'+positionUniqueName+'/'+$scope.selectedSortingMode+'?SBI_EXECUTION_ID='+JSsbiExecutionID;
		  }
		 
		 sbiModule_restServices.promiseGet("1.0",path)
			.then(function(response) {
				$scope.handleResponse(response);
			}, function(response) {
				sbiModule_messaging.showErrorMessage("An error occured while sorting", 'Error');
				
			});	
		  }
	  
	  checkLock = function(s){
		  if(s=="locked_by_user"){
			  $scope.lockerClass="unlock-icon"; 
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.unlock');
		  }
		  if(s=="locked_by_other"){
			  $scope.lockerClass="lock-other-icon";
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.lock_other');
			  $scope.lockTooltip += " "+locker;
		  }
		  if(s=="unlocked"){
			  $scope.lockerClass="lock-icon";
			  $scope.lockTooltip = sbiModule_translate.load('sbi.olap.toolbar.lock');
		  }
	  };
	  
	  checkLock(status);
	  
	  function lockUnlock(type, id){
		  $http({
			  method:'POST',
			  url: 'http://localhost:8080/knowage/restful-services/1.0/locker/'+id+'/'+type
		  }).then(function(response){
						status = response.data.status;
						locker = response.data.locker;
						whatIfBtns(response.data.status);
						checkLock(response.data.status);
		  },function(response){
			  sbiModule_messaging.showErrorMessage("An error occured while locking", 'Error'); 
		  });
	  };
	  
	  $scope.showExportDialog= function(ev){
		  $scope.showDialog(ev,"/right/exportOutputDialog.html");
	  };
	  
	  $scope.exportDialogNext = function(){
		  if($scope.outputType == "csv")
			  $scope.showFile = true;
		  else
			  $scope.showTable = true;
	  }
	  
	  $scope.exportDialogBack = function(){
		  $scope.showFile = false;
		  $scope.showTable = false;
	  }
	  
	  flushCache = function(){
		  sbiModule_restServices.promisePost
			("1.0",'/cache/?SBI_EXECUTION_ID='+ JSsbiExecutionID)
			.then(function(response) {
						$scope.handleResponse(response);
		  },function(response){
			  sbiModule_messaging.showErrorMessage("An error occured while refreshing", 'Error'); 
		  });
	  };
	  
	  $scope.exportOutputVersion = function(){
		  var value = $scope.outputType == "csv"? $scope.delimiter : $scope.tableName;
		  
			  sbiModule_restServices.promiseGet
				("1.0",'/analysis/'+$scope.outputType+'/'+$scope.outputVersion+'/'+value+'?SBI_EXECUTION_ID='+ JSsbiExecutionID)
				.then(function(response) {
							//$scope.handleResponse(response);
					console.log(response)
			  },function(response){
				  sbiModule_messaging.showErrorMessage("An error occured while exporting version", 'Error'); 
			  });
		  
		  
	  };
	  
	  $scope.isOkBtnDisabled = function(){
		  if(!$scope.showFile && !$scope.showTable)
			  return true;
		  else if($scope.outputType == "table" && $scope.tableName.length < 1)
			  return true;		  
		  else if($scope.outputType == "csv" && $scope.delimiter.length < 1)
			  return true;		  
		  else
			  return false;
		  
	  }

};
