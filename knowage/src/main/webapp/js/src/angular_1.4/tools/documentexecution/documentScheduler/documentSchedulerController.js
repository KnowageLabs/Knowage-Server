(function() {
	var documentExecutionModule = angular.module('documentExecutionModule');
	
	documentExecutionModule.directive('documentScheduler', ['sbiModule_config', function(sbiModule_config){
		return {
			restrict: 'E',
			templateUrl: sbiModule_config.contextName 
				+ '/js/src/angular_1.4/tools/documentexecution/documentScheduler/documentSchedulerTemplate.jsp',
			controller: documentSchedulerCtrl,
		};
	}]);
	
	
	var documentSchedulerCtrl = function($scope, sbiModule_config, sbiModule_translate, documentExecuteServices, $mdDialog
			,sbiModule_restServices,docExecute_urlViewPointService,execProperties,docExecute_paramRolePanelService,$filter,sbiModule_download) {
			
		$scope.column = [
                        {label:sbiModule_translate.load("sbi.generic.name"),name:"name"},
                        {label:sbiModule_translate.load("sbi.generic.descr"),name:"description"},
                        {label:sbiModule_translate.load("sbi.generic.creationdate"),name:"dateCreation",transformer:function(data){
                       	 return $filter('date')(data, "dd/MM/yyyy")
                       	 }}];

		$scope.closeFilter = function(){
			$mdDialog.cancel();
		}
		
		
		$scope.getDownload=function(item){
			
			console.log('item to download ' , item);
			
			sbiModule_restServices.get("1.0/documentsnapshot","getSnapshotContent","idSnap="+item.id+"&biobjectId="+item.biobjId).success(
					function(data, status, headers, config) {
						if (data.hasOwnProperty("errors")) {
							console.log("Snapshot non Ottenuti");
						} else {
							var text ;		
							var arr = data.snapshot;
							var byteArray = new Uint8Array(arr);
							sbiModule_download.getBlob(byteArray, item.name, item.contentType,'');

//							if($scope.typeWFS == 'geojson'){
//								$scope.download.getPlain(data, item.label, 'text/json', 'json');
//							} else if($scope.typeWFS == 'kml' || $scope.typeWFS == 'shp'){
//								$scope.download.getLink(data.url);
//							}
							$scope.closeFilter();
						}
					}).error(function(data, status, headers, config) {
						console.log("Snapshot non Ottenuti " + status);

					});


		}
		
		
		
		$scope.gvpCtrlSchedulerMenuOpt = 
			[ 			 		               	
			 { // Fill Form
				 label: sbiModule_translate.load("sbi.generic.download"),
				 icon:"fa fa-download",
				 color:'#222222',
				 action : function(item) { 
					 docExecute_urlViewPointService.snapshotItem = item;
					 
					 docExecute_urlViewPointService.snapshotUrl=docExecute_urlViewPointService.snapshotUrlPath+
					   "&ACTION_NAME=GET_SNAPSHOT_CONTENT"+"&SNAPSHOT_ID=" + item.id+"&OBJECT_ID=" + item.biobjId+
					   "&LIGHT_NAVIGATOR_DISABLED=TRUE";
					 $mdDialog.show({
							templateUrl: 'dialog1.tmpl.html',
							scope:$scope,
							preserveScope: true,
							targetEvent:item,
							//parent: angular.element(document.body),
							clickOutsideToClose:true
						})
					 					 
				 }	
			 }
			 ,{   //Delete Action
				 label: sbiModule_translate.load("sbi.generic.delete"),
				 icon:"fa fa-trash-o",
				 //backgroundColor:'red',
				 color:'#222222',
				 action : function(item) {
					 var confirm = $mdDialog
						.confirm()
						.title(sbiModule_translate.load("sbi.execution.snapshots.deleteSelectedTooltip"))
						.content(
							sbiModule_translate
							.load("sbi.execution.snapshots.deleteConfirm"))
							.ok(sbiModule_translate.load("sbi.general.continue"))
							.cancel(sbiModule_translate.load("sbi.general.cancel")
						);
					$mdDialog.show(confirm).then(function() {
						var index =docExecute_urlViewPointService.gvpCtrlSchedulers.indexOf(item);
						console.log('item ' , item); 
						var objSnapshot = JSON.parse('{ "SNAPSHOT" : "'+ item.id +'"}');
							sbiModule_restServices.post(
									"1.0/documentsnapshot",
									"deleteSnapshot", objSnapshot)
							   .success(function(data, status, headers, config) {
								   if(data.errors && data.errors.length > 0 ){
									   documentExecuteServices.showToast(data.errors[0].message);
									 }else{
										 docExecute_urlViewPointService.gvpCtrlSchedulers.splice(index, 1);
											 //message success 
									 }
								   //gvpctl.selectedParametersFilter = [];
							})
							.error(function(data, status, headers, config) {});
//							$scope.getViewpoints();
					}, function() {
						console.log('Annulla');
						//docExecute_urlViewPointService.getViewpoints();
					});	
				 }
			 } 	
		 ];
		
		
		
		
		
		
	};
})();