


var entities = angular.module('entities',['sbiModule']);

entities.service('entity_service',function(sbiModule_action_builder,$filter){

	var entityTypes = ["geographical dimension", "cube", "generic"];

    this.getEntityTypes = function(){
        return entityTypes;
    }

    this.getEntityType = function(entities,fieldId){
    	return $filter('filter')(entities,{$:fieldId},true)[0] ? $filter('filter')(entities,{$:fieldId},true)[0].iconCls : "";
    }

    this.isEntityType = function(entities,fieldId,type){
    	return this.getEntityType(entities,fieldId) === type;
    }

	this.getEntitiyTree = function(datamartName){
		var getTreeAction = sbiModule_action_builder.getActionBuilder("GET");
		getTreeAction.actionName = 'GET_TREE_ACTION';
		getTreeAction.queryParams.datamartName = datamartName;
		return getTreeAction.executeAction();

	}
})