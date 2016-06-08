angular.module('impExpKpis').controller('importKpiControllerStep1', ['sbiModule_download','sbiModule_device',"$scope", "$mdDialog", "$timeout", "sbiModule_logger", "sbiModule_translate","sbiModule_restServices","sbiModule_config","importExportDocumentModule_importConf","$mdToast",importKpiStep1FuncController]);

function importKpiStep1FuncController(sbiModule_download,sbiModule_device,$scope, $mdDialog, $timeout, sbiModule_logger, sbiModule_translate, sbiModule_restServices,sbiModule_config,importExportDocumentModule_importConf,$mdToast) {



	$scope.selectKpi = function(item){
		//if is present remove
		var index = $scope.indexInList(item,$scope.IEDConf.roles.selectedKpi);
		if(index!=-1){
			$scope.IEDConf.roles.selectedKpi.splice(index,1);
		}else{
			//if not present add
			$scope.IEDConf.roles.selectedKpi.push(item);

		}


	}
	$scope.addKpi = function(){

		for(var i=0;i<$scope.IEDConf.roles.selectedKpi.length;i++){

			//add inf exportig kpi
			var index = $scope.indexInList($scope.IEDConf.roles.selectedKpi[i],$scope.IEDConf.roles.exportingKpi);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.exportingKpi.push($scope.IEDConf.roles.selectedKpi[i]);

			}
			//remove from IEDConf.exportedKpi
			var index = $scope.indexInList($scope.IEDConf.roles.selectedKpi[i],$scope.IEDConf.roles.exportedKpi);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.exportedKpi.splice(index,1);
			}else{
				//if not present add nothing action

			}
		}


		$scope.IEDConf.roles.selectedKpi=[];

	}
	$scope.removeKpi = function(){

		for(var i=0;i<$scope.IEDConf.roles.selectedKpi.length;i++){
			//add inf exportig kpi
			var index = $scope.indexInList($scope.IEDConf.roles.selectedKpi[i],$scope.IEDConf.roles.exportedKpi);
			if(index!=-1){
				//if present nothing action
			}else{
				//if not present add
				$scope.IEDConf.roles.exportedKpi.push($scope.IEDConf.roles.selectedKpi[i]);

			}
			//remove from IEDConf.roles.exportedKpi
			var index = $scope.indexInList($scope.IEDConf.roles.selectedKpi[i],$scope.IEDConf.roles.exportingKpi);
			if(index!=-1){
				//if present
				$scope.IEDConf.roles.exportingKpi.splice(index,1);
			}else{
				//if not present add nothing action

			}
		}


		$scope.IEDConf.roles.selectedKpi=[];

	}

	$scope.addAllKpi = function(){
		$scope.IEDConf.roles.selectedKpi=[];
		if($scope.IEDConf.roles.exportingKpi.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.exportedKpi.length;i++){
				$scope.IEDConf.roles.exportingKpi.push($scope.IEDConf.roles.exportedKpi[i]);
			}
		}else{
			$scope.IEDConf.roles.exportingKpi = $scope.IEDConf.roles.exportedKpi;
		}

		$scope.IEDConf.roles.exportedKpi=[];
	}

	$scope.removeAllKpi = function(){
		$scope.IEDConf.roles.selectedKpi=[];
		if($scope.IEDConf.roles.exportedKpi.length!=0){
			for(var i=0;i<$scope.IEDConf.roles.exportingKpi.length;i++){
				$scope.IEDConf.roles.exportedKpi.push($scope.IEDConf.roles.exportingKpi[i]);
			}
		}else{
			$scope.IEDConf.roles.exportedKpi = $scope.IEDConf.roles.exportingKpi;
		}

		$scope.IEDConf.roles.exportingKpi=[];
	};
	
	$scope.save = function(ev){
		if($scope.IEDConf.roles.exportingKpi.length == 0){
			//if not selected no one kpis
			$scope.showAction(sbiModule_translate.load("sbi.importkpis.anykpichecked"));
		}else{

			$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedRoles','component_impexp_messages')})
						
				
					
			
		}
	}
	
	


}
