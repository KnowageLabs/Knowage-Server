angular.module('impExpUsers').controller('importUserControllerStep0', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importUserStep0FuncController]);
 
function importUserStep0FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {
	$scope.upload = function(ev){
		if($scope.IEDConf.fileImport.fileName == "" || $scope.IEDConf.fileImport.fileName == undefined){
			$scope.showAction(sbiModule_translate.load("sbi.impexpusers.missinguploadfile"));
		}else{
			var fd = new FormData();
		
			fd.append('exportedArchive', $scope.IEDConf.fileImport.file);
			sbiModule_restServices.post("1.0/serverManager/importExport/users", 'import', fd, {transformRequest: angular.identity,headers: {'Content-Type': undefined}})
			.success(function(data, status, headers, config) {
				if(data.STATUS=="NON OK"){
					$mdToast.show($mdToast.simple().content("data.ERROR").position('top').action(
					'OK').highlightAction(false).hideDelay(5000));
				}
				else if(data.STATUS=="OK"){
					//check role missing
					$scope.flagShowUser=true;
					$scope.IEDConf.roles.currentRoles=data.currentRoles;
					$scope.IEDConf.roles.exportedRoles=data.exportedRoles;
					$scope.IEDConf.roles.associatedRoles=data.associatedRoles;
				//	if($scope.checkRole()){
						$scope.IEDConf.roles.exportedUser = data.exportedUser;
				//	}
					
					
					$scope.stepControl.insertBread({name: sbiModule_translate.load('SBISet.impexp.exportedUsers','component_impexp_messages')});
					
				}
				
				
			})
			.error(function(data, status, headers, config) {
				$mdToast.show($mdToast.simple().content("errore").position('top').action(
				'OK').highlightAction(false).hideDelay(5000));
				
			});
		}
	};
	
	$scope.checkRole = function(){
		
		for(var i=0;i<$scope.IEDConf.roles.exportedRoles.length;i++){
			var index = $scope.indexRoleInList($scope.IEDConf.roles.exportedRoles[i],$scope.IEDConf.roles.currentRoles );
			if(index==-1){
				$scope.showConfirm();
				return false;
				
			}
		}
		//all roles present
		return true;
		
	};
	
	$scope.indexRoleInList=function(item, list) {

		for (var i = 0; i < list.length; i++) {
			var object = list[i];
			if(object.name==item.name){
				return i;
			}
		}

		return -1;
	};
	 
}
	