


var entities = angular.module('entities',['sbiModule']);

entities.service('entity_service',function(sbiModule_action_builder){

	this.getEntitiyTree = function(datamartName){
//		var queryParam = {};
//
//		queryParam.datamartName = datamartName; 
//
//		return sbiModule_action.promiseGet('GET_TREE_ACTION',queryParam,null);
		var getTreeAction = sbiModule_action_builder.getActionBuilder("GET");
		getTreeAction.actionName = 'GET_TREE_ACTION';
		return getTreeAction.executeAction();

	}
})