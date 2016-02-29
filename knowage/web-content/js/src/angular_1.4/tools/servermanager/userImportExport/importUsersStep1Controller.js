angular.module('impExpUsers').controller('importUserControllerStep1', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importUserStep1FuncController]);

function importUserStep1FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {



	$scope.selectUser = function(item){
		//if is present remove
		var index = $scope.indexInList(item,$scope.IEDConf.roles.selectedUser);
		if(index!=-1){
			$scope.IEDConf.roles.selectedUser.splice(index,1);
		}else{
			//if not present add
			$scope.IEDConf.roles.selectedUser.push(item);

		}


	}
	$scope.addUser = function(){

		for(var i=0;i<$scope.IEDConf.roles.selectedUser.length;i++){

			//add inf exportig user
			var index = $scope.indexInList($scope.IEDConf.roles.selectedUser[i],$scope.IEDConf.roles.exportingUser);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.exportingUser.push($scope.IEDConf.roles.selectedUser[i]);

			}
			//remove from IEDConf.exportedUser
			var index = $scope.indexInList($scope.IEDConf.roles.selectedUser[i],$scope.IEDConf.roles.exportedUser);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.exportedUser.splice(index,1);
			}else{
				//if not present add nothing action

			}
		}


		$scope.IEDConf.roles.selectedUser=[];

	}
	$scope.removeUser = function(){

		for(var i=0;i<$scope.IEDConf.roles.selectedUser.length;i++){
			//add inf exportig user
			var index = $scope.indexInList($scope.IEDConf.roles.selectedUser[i],$scope.IEDConf.roles.exportedUser);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.exportedUser.push($scope.IEDConf.roles.selectedUser[i]);

			}
			//remove from IEDConf.roles.exportedUser
			var index = $scope.indexInList($scope.IEDConf.roles.selectedUser[i],$scope.IEDConf.roles.exportingUser);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.exportingUser.splice(index,1);
			}else{
				//if not present add nothing action

			}
		}


		$scope.IEDConf.roles.selectedUser=[];

	}

	$scope.addAllUser = function(){
		$scope.IEDConf.roles.selectedUser=[];
		if($scope.IEDConf.roles.exportingUser.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.exportedUser.length;i++){
				$scope.IEDConf.roles.exportingUser.push($scope.IEDConf.roles.exportedUser[i]);
			}
		}else{
			$scope.IEDConf.roles.exportingUser = $scope.IEDConf.roles.exportedUser;
		}

		$scope.IEDConf.roles.exportedUser=[];
	}

	$scope.removeAllUser = function(){
		$scope.IEDConf.roles.selectedUser=[];
		if($scope.IEDConf.roles.exportedUser.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.exportingUser.length;i++){
				$scope.IEDConf.roles.exportedUser.push($scope.IEDConf.roles.exportingUser[i]);
			}
		}else{
			$scope.IEDConf.roles.exportedUser = $scope.IEDConf.roles.exportingUser;
		}

		$scope.IEDConf.roles.exportingUser=[];
	};
	
	$scope.save = function(ev){
		if($scope.IEDConf.roles.exportingUser.length == 0){
			//if not selected no one users
			$scope.showAction(sbiModule_translate.load("sbi.importusers.anyuserchecked"));
		}else{

			$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')})
						
				
					
			
		}
	}
	
	


}
