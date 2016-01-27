angular.module('importExportDocumentModule').controller('importControllerStep1', ["$scope","importExportDocumentModule_importConf","sbiModule_restServices",importStep1FuncController]);

function importStep1FuncController($scope,importExportDocumentModule_importConf,sbiModule_restServices ) {
	
	function roleInList(role,list){
		for(var i in list){
			if(list[i].name==role.name){
				return i;
			}
		}
		return -1;
	}
	
	$scope.currentRoleIsSelectable=function(role,exprole){
		var roleinl=roleInList(role,importExportDocumentModule_importConf.roles.associatedRoles);
		if(roleinl!=-1 && roleinl!=exprole.id){
			return false
		}
		return true;	
		}
	
	function getExportedRole(){
		var expr=[];
		for(var key in importExportDocumentModule_importConf.roles.associatedRoles){
			expr.push({roleAssociateId:importExportDocumentModule_importConf.roles.associatedRoles[key].id,expRoleId:key});
		}
		return expr;
	}
	
	$scope.saveRoleAssociation=function(){
		sbiModule_restServices.post("1.0/serverManager/importExport/document", 'associateRoles',getExportedRole())
		.success(function(data, status, headers, config) {
			console.log("role--->",data)
			if(data.STATUS=="NON OK"){
				$scope.showToast(data.ERROR,4000);
			}
			else if(data.STATUS=="OK"){
				importExportDocumentModule_importConf.engines.exportedEngines=data.exportedEngines;
				importExportDocumentModule_importConf.engines.currentEngines=data.currentEngines;
				importExportDocumentModule_importConf.engines.associatedEngines=data.associatedEngines;
//				importExportDocumentModule_importConf.engines.exportedEngines.push({"id":178,"criptable":0,"name":"Gianluca Engine","description":"Gianluca Engine è una particolare estensione che serve per....","url":"/knowagegianlucaengine/api/1.0/pages/execute","secondaryUrl":null,"dirUpload":null,"dirUsable":null,"driverName":"it.eng.spagobi.engines.drivers.gianluca.ChartDriver","label":"knowagegianlucaengine","className":"","biobjTypeId":null,"engineTypeId":null,"useDataSource":false,"useDataSet":false});
				
				$scope.stepControl.insertBread({name: $scope.translate.load('SBISet.impexp.exportedEngines','component_impexp_messages')})
			}
		})
		.error(function(data, status, headers, config) {
			$scope.showToast(data,4000);
		});
	}
	
	
}
