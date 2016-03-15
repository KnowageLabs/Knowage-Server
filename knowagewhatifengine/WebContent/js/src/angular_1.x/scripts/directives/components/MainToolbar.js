angular.module('main_toolbar',[])
	.directive('mainToolbar',function(){
		return{
			restrict: "E",
			replace: 'true',
			templateUrl: '/knowagewhatifengine/html/template/main/toolbar/mainToolbar.html',
			controller: mainToolobarController
		}
	});
	
	function mainToolobarController($scope, $timeout, $window, $mdDialog, $http, $sce, sbiModule_messaging, sbiModule_restServices, sbiModule_translate) {
		
		/*filterXMLResult = function(res) {
			var regEx = /([A-Z]+_*)+/g;
			var i;
			
			while (i = regEx.exec(res)){
				var btn = {};
				btn.tooltip = messageResource.get("sbi.olap.toolbar."+ i[0], 'messages');
				btn.img =i[0];//"../img/show_parent_members.png"// url(../img/show_parent_members.png);
				$scope.toolbarButtons.push(btn);	
			}
				
		}

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
			default:
				console.log("something else clicked");
			}
			$scope.sendModelConfig($scope.modelConfig);
		}
		
		$scope.enableDisableSorting = function(){
			
			$scope.sortDisable();
		}
		
		$scope.enableDisableDrillThrough = function(){
			$scope.modelConfig.enableDrillThrough = !$scope.modelConfig.enableDrillThrough;
			$scope.sendModelConfig($scope.modelConfig);
		}
		
		
		$scope.changeDrillType = function(type){
			$scope.modelConfig.drillType = type;
			$scope.sendModelConfig($scope.modelConfig);
		}

		 //service for placing member on axis
		 $scope.putMemberOnAxis = function(fromAxis,member){
			 
			 sbiModule_restServices.promisePost
			 ('1.0/axis/'+fromAxis+'/moveDimensionToOtherAxis/'+member.uniqueName+'/'+member.axis+'?SBI_EXECUTION_ID='+JSsbiExecutionID,"",member)
				.then(function(response) {
					$scope.handleResponse(response);			
				}, function(response) {
					sbiModule_messaging.showErrorMessage("An error occured while placing member on axis", 'Error');
					
				});	
		}
		 
		 //service for sending modelConfig
		 $scope.sendModelConfig = function(modelConfig){
			 
			 sbiModule_restServices.promisePost
			 ("1.0/modelconfig?SBI_EXECUTION_ID="+JSsbiExecutionID,"",modelConfig)
				.then(function(response) {
					$scope.handleResponse(response);
				}, function(response) {
					sbiModule_messaging.showErrorMessage("An error occured while sending model config", 'Error');
					
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
		 
		 // dragan  sorting
		 
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
*/
	};
