angular.module('impExpUsers').controller('importUserControllerStep2', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importUserStep2FuncController]);

function importUserStep2FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {


	
	$scope.selectUser = function(item){
		//if is present remove
		var index = $scope.indexInList(item,$scope.IEDConf.roles.selectedRoles);
		if(index!=-1){
			$scope.IEDConf.roles.selectedRoles.splice(index,1);
		}else{
			//if not present add
			$scope.IEDConf.roles.selectedRoles.push(item);
			
		}
		
		
	}
	$scope.addUser = function(){

		for(var i=0;i<$scope.IEDConf.roles.selectedRoles.length;i++){
			
			//add inf exportig user
			var index = $scope.indexInList($scope.IEDConf.roles.selectedRoles[i],$scope.IEDConf.roles.currentRoles);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.currentRoles.push($scope.IEDConf.roles.selectedRoles[i]);
				
			}
			//remove from IEDConf.exportedUser
			var index = $scope.indexInList($scope.IEDConf.roles.selectedRoles[i],$scope.IEDConf.roles.exportedRoles);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.exportedRoles.splice(index,1);
			}else{
				//if not present add nothing action
				
			}
		}
		
		$scope.IEDConf.roles.selectedRoles=[];
		
	}
	$scope.removeUser = function(){
		
		for(var i=0;i<$scope.IEDConf.roles.selectedRoles.length;i++){
			//add inf exportig user
			var index = $scope.indexInList($scope.IEDConf.roles.selectedRoles[i],$scope.IEDConf.roles.exportedRoles);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.exportedRoles.push($scope.IEDConf.roles.selectedRoles[i]);
				
			}
			//remove from IEDConf.roles.exportedUser
			var index = $scope.indexInList($scope.IEDConf.roles.selectedRoles[i],$scope.IEDConf.roles.currentRoles);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.currentRoles.splice(index,1);
			}else{
				//if not present add nothing action
				
			}
		}
		
		$scope.IEDConf.roles.selectedRoles=[];
		
	}
	
	$scope.addAllUser = function(){
		$scope.IEDConf.roles.selectedRoles=[];
		if($scope.IEDConf.roles.currentRoles.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.exportedRoles.length;i++){
				$scope.IEDConf.roles.currentRoles.push($scope.IEDConf.roles.exportedRoles[i]);
			}
		}else{
			$scope.IEDConf.roles.currentRoles =$scope.IEDConf.roles.exportedRoles;
		}
		
		$scope.IEDConf.roles.exportedRoles=[];
	}
	
	$scope.removeAllUser = function(){
		$scope.IEDConf.roles.selectedRoles=[];
		if($scope.IEDConf.roles.exportedRoles.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.currentRoles.length;i++){
				$scope.IEDConf.roles.exportedRoles.push($scope.IEDConf.roles.currentRoles[i]);
			}
		}else{
			$scope.IEDConf.roles.exportedRoles = $scope.IEDConf.roles.currentRoles;
		}
		
		$scope.IEDConf.roles.currentRoles=[];
	};
	
	$scope.save = function(ev){
		if($scope.IEDConf.roles.currentRoles.length == 0){
			//if not selected no one users
			$scope.showAction(sbiModule_translate.load("sbi.importusers.anyuserchecked"));
		}else{
			//import User
			var data={
					"exportingUser":$scope.IEDConf.roles.exportingUser,
					"type":$scope.IEDConf.typeSaveUser,
					"importPersonalFolder":$scope.IEDConf.importPersonalFolder
			}
			
			sbiModule_restServices.post("1.0/serverManager/importExport/users","importUsers",data)
			.success(function(data, status, headers, config) {
			
				if(data.hasOwnProperty("errors")){
					$scope.stopImport(data.errors[0].message);	
				}else if(data.STATUS=="NON OK"){
					 $scope.stopImport(data.SUBMESSAGE,$scope.translate.load(data.ERROR,'component_impexp_messages'));	
				}
				else if(data.STATUS=="OK"){
					
					if(data.EXPORTOBJECT=="true"){
						importExportDocumentModule_importConf.engines.exportedEngines=data.exportedEngines;
						importExportDocumentModule_importConf.engines.currentEngines=data.currentEngines;
						importExportDocumentModule_importConf.engines.associatedEngines=data.associatedEngines;
//						importExportDocumentModule_importConf.engines.exportedEngines.push({"id":178,"criptable":0,"name":"Gianluca Engine","description":"Gianluca Engine Ã¨ una particolare estensione che serve per....","url":"/knowagegianlucaengine/api/1.0/pages/execute","secondaryUrl":null,"dirUpload":null,"dirUsable":null,"driverName":"it.eng.spagobi.engines.drivers.gianluca.ChartDriver","label":"knowagegianlucaengine","className":"","biobjTypeId":null,"engineTypeId":null,"useDataSource":false,"useDataSet":false});
						
						$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedEngines','component_impexp_messages')})
					}else{
//						$scope.stepControl.resetBreadCrumb();
//						$scope.stepControl.insertBread({name:$scope.translate.load('sbi.ds.file.upload.button')})
//						$scope.finishImport();
						
						$scope.stopImport($scope.translate.load("sbi.importusers.importuserok"));	
					}
					
					
				
				}
				
			}).error(function(data, status, headers, config) {
				$scope.stopImport(data);
			})
			
		}
	}
	
	
}
	