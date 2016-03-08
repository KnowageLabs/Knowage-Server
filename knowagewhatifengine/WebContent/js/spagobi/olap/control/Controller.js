/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 *
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 *
 * This component manage all the events of the table.
 * The standard use case is: the view send an event at the event manager,
 * the manager captures it, decores it and calls a method of the controller.
 * The controller execute the request and return the result at the event manager.
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.control.Controller', {
	extend: 'Object',

	/**
	 * @property {Sbi.olap.control.EventManager} executionPanel
	 *  Panel that contains the pivot and the chart
	 */
	eventManager: null,


	constructor : function(config) {
		this.eventManager = config.eventManager;
	},

	executeMdx: function(mdx){

		var service = Ext.create("Sbi.service.RestService",{
			url: "model",
			method: "POST",
			pathParams: [mdx]
		});

		service.callService(this);
	}
	,drillDown: function(axis, position,  member, uniqueName, positionUniqueName){

		var service = Ext.create("Sbi.service.RestService",{
			url: "member",
			subPath: "drilldown",
			pathParams: [axis, position, member, positionUniqueName, uniqueName]
		});

		service.callService(this);

	}
	, drillUp: function(axis, position,  member, uniqueName, positionUniqueName){

		var service = Ext.create("Sbi.service.RestService",{
			url: "member",
			subPath: "drillup",
			pathParams: [axis, position, member, positionUniqueName, uniqueName]
		});

		service.callService(this);

	}
	,swapAxis: function(){

		var service = Ext.create("Sbi.service.RestService",{
			url: "axis",
			subPath: "swap",
			method: "POST"
		});

		service.callService(this);

	}
	,addSlicer: function(hierarchy, member, multiSelection){

		var service = Ext.create("Sbi.service.RestService",{
			url: "hierarchy",
			pathParams: [hierarchy, "slice", member, multiSelection]
		});

		service.callService(this);
	}
	,
	moveHierarchy: function(uniqueName, axis, newPosition, direction){

		var service = Ext.create("Sbi.service.RestService",{
			url: "axis",
			method: 'POST',
			pathParams: [axis,"moveHierarchy", uniqueName, newPosition, direction]
		});

		service.callService(this);
	}
	,
	moveDimensionToOtherAxis: function(hierarchy1, fromAxis, toAxis){

		var service = Ext.create("Sbi.service.RestService",{
			url: "axis",
			method: 'POST',
			pathParams: [fromAxis,"moveDimensionToOtherAxis", hierarchy1, toAxis]
		});

		service.callService(this);
	}
	,
	updateHierarchyOnDimension: function(axis, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition){

		var service = Ext.create("Sbi.service.RestService",{
			url: "axis",
			method: 'POST',
			pathParams: [axis,"updateHierarchyOnDimension",newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition]
		});

		service.callService(this);
	}

	, setModelConfig: function(config){

		var service = Ext.create("Sbi.service.RestService",{
			url: "modelconfig",
			method: 'POST',
			jsonData: config

		});

		service.callService(this);

	}
	, placeMembersOnAxis: function(axis, members){

		var service = Ext.create("Sbi.service.RestService",{
			url: "axis",
			method: 'POST',
			pathParams: [axis,"placeMembersOnAxis"],
			jsonData: members

		});

		service.callService(this);

	}
	, setValue: function(ordinal, expression) {

		var service = Ext.create("Sbi.service.RestService", {
			url: "model",
			method: 'POST',
			pathParams: ["setValue" ,ordinal],
			jsonData: { "expression" : expression }
		});

		service.callService(this, null, null, false, true);
	}

	,
	undo: function() {

		var service = Ext.create("Sbi.service.RestService", {
			url: "model",
			method: 'POST',
			pathParams: ["undo"]
		});

		service.callService(this);
	}

	, cleanCache: function() {

		var service = Ext.create("Sbi.service.RestService", {
			url: "cache",
			method: 'POST'
		});

		service.callService(this);
	}

	,persistTransformations: function() {

		var service = Ext.create("Sbi.service.RestService", {
			url: "model",
			method: 'POST',
			pathParams: ["persistTransformations"],
			longExecution: true,
			timeout: Sbi.settings.olap.whatif.timeout.persistTransformations
		});

		service.callService(this);
	}
	,persistNewVersionTransformations: function(params) {

		var name = params.versionName;
		var descr = params.versionDescription;

		if(!name){
			name = "sbiNoDescription";
		}

		if(!descr){
			descr = "sbiNoDescription";
		}

		var service = Ext.create("Sbi.service.RestService", {
			url: "model",
			method: 'POST',
			pathParams: ["saveAs",name,descr],
			longExecution: true,
			timeout: Sbi.settings.olap.whatif.timeout.persistNewVersionTransformations
		});

		service.callService(this);
	}

	/**
	 * Call the rest service to delete the selected versions
	 */
	,deleteVersions: function(itemsToDelete){



		var service = Ext.create("Sbi.service.RestService", {
			url: "version",
			method: 'POST',
//			async: true,
			longExecution: true,
			pathParams: ["delete",itemsToDelete]
		});

//		service.on("executedAsync", function(status, response){
//		if(status){
//		Sbi.exception.ExceptionHandler.showInfoMessage('sbi.olap.control.controller.delete.version.ok');
//		}else{
//		Sbi.exception.ExceptionHandler.showErrorMessage('sbi.olap.control.controller.delete.version.error');
//		}
//		}, this);


		var mySuccessCallBack = function(response, options) {
			if(response != undefined && response.statusText != undefined && response.responseText!=null && response.responseText!=undefined) {
				if(response.responseText.length>21 && response.responseText.substring(0,13) =='{"errors":[{"'){
					Sbi.olap.eventManager.fireEvent('serviceExecutedWithError', response);
					Sbi.exception.ExceptionHandler.handleFailure(response);
				}else{
					Sbi.olap.eventManager.fireEvent('serviceExecuted', response);
					Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.olap.control.controller.delete.version.ok'));
				}
			} else {
				Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			}
		};

		service.callService(this,mySuccessCallBack);
	}

	,exportOutput: function(params){

		var pathParams = [params.exportType, params.version];

		if(params.exportType =="csv"){
			pathParams.push(params.csvFieldDelimiter);
			//pathParams.push(params.csvRowDelimiter);
		}else{
			pathParams.push(params.tableName);
		}

		var service = Ext.create("Sbi.service.RestService", {
			url: "analysis",
			method: 'GET',
			async: true,
			pathParams: pathParams,
			timeout: Sbi.settings.olap.whatif.timeout.persistTransformations
		});

		service.on("executedAsync", function(status, response){
			if(status){
				Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.olap.toolbar.exportoutput.ok'));
			}else{
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.olap.toolbar.exportoutput.error'));
			}
			this.eventManager.setLockTypeEdit(null);
		}, this);



		if(params.exportType =="csv"){
			var exportationUrl = service.getRestUrlWithParameters(true);
			window.open(exportationUrl,'exportOutput','resizable=1,height=550,width=700');//.document.write(['<html><head></head><body>'+LN('sbi.olap.toolbar.exportoutput.csv.window')+'</body></html>']);
		}else{
			service.callService(this);
			this.eventManager.setLockTypeEdit("export.output");
		}


	}


	/**
	 * Call the rest service to export the pivot table
	 */
	,exportPivotTable: function(format){
		var service = Ext.create("Sbi.service.RestService",{
			url: "model",
			method: 'GET',
			pathParams: ["export","X"]
		});

		service.callServiceInNewWindow(this);
	}

	,lockModel: function() {

		var olapToolbar = this.eventManager.olapPanel.executionPanel.olapToolbar;

		var externalUrlPath = Sbi.config.externalUrl;
		var artifactId = olapToolbar.modelConfig.artifactId;


		var service = Ext.create("Sbi.service.RestService", {
			url: "locker",
			method: 'POST',
			pathParams: [artifactId, "lock"],
			externalUrl: externalUrlPath
		});


		service.callService(olapToolbar,
				function(result){
			olapToolbar.renderLockModel(result);
			Sbi.olap.eventManager.hideLoadingMask();
		}
		);
	}
	,unlockModel: function() {

		var olapToolbar = this.eventManager.olapPanel.executionPanel.olapToolbar;

		var externalUrlPath = Sbi.config.externalUrl;
		var artifactId = olapToolbar.modelConfig.artifactId;

		var service = Ext.create("Sbi.service.RestService", {
			url: "locker",
			method: 'POST',
			pathParams: [artifactId, "unlock"],
			externalUrl: externalUrlPath
		});




		service.callService(olapToolbar,
				function(result){
			olapToolbar.renderUnlockModel(result);
			Sbi.olap.eventManager.hideLoadingMask();
		}
		);
	}

	, setAllocationAlgorithm: function(className){
		var service = Ext.create("Sbi.service.RestService", {
			url: "allocationalgorithm",
			method: 'POST',
			pathParams: [className]
		});

		service.callService(this, function(){Sbi.olap.eventManager.hideLoadingMask();});
	}

	/**
	 * Save a subobject
	 */
	, saveSubObject: function(name, description, scope){

		if(!description){
			description = " ";
		}

		var service = Ext.create("Sbi.service.RestService",{
			url: "subobject",
			method: 'POST',
			pathParams: [name, description, scope],
			async: true
		});


		service.on("executedAsync", function(status, response){
			if(status){
				Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.olap.subobject.save.ok'));
			}else{
				Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.olap.subobject.save.ko'));
			}
		}, this);

		service.callService(this);
	}


	/**
	 * Call the rest service to execute calculated member expression
	 */
	//author: Maria Caterina Russo from Osmosit
	, executeCalculatedMemberExpression: function(name,expression,ccParentUniqueName,ccAxis){
		var newExpression = expression.replace(/\//gi, this.DIVISION_SIGN);

		var	service = Ext.create("Sbi.service.RestService", {
			url: "calculatedmembers",
			method: 'POST',
			longExecution: true,
			pathParams: ["execute",name, newExpression,ccParentUniqueName,ccAxis]
		});

		service.callService(this);
	}

	//author: Maria Caterina Russo from Osmosit
	/**
	 * Initialize cross navigation
	 */
	,initCrossNavigation: function(){
		var	service = Ext.create("Sbi.service.RestService", {
			url: "crossnavigation/initialize",
			method: 'GET',
			longExecution: true			
		});
		service.callService(this);
	}

//	author: Maria Caterina Russo from Osmosit
	/**
	 * Get url for cross navigation
	 */
	,getCrossNavigationUrl: function(targetIndex, ordinal){
		var	service = Ext.create("Sbi.service.RestService", {
			url: "crossnavigation",
			longExecution: true,
			pathParams: ["getCrossNavigationUrl",targetIndex, ordinal]
		});
		Ext.Ajax.request({
			url: service.getRestUrlWithParameters(),
			params: service.getRequestParams(),
			method: 'POST',
			success: function(response){
				var data = response.responseText;		
				var tmpFunc = new Function(data);			
				tmpFunc();
			}
		});		
	}


	,DIVISION_SIGN: "{spagobi.operator.division}"

		/**
		 * Opens the calculated members wizard by double click
		 */
		//author: Maria Caterina Russo from Osmosit
		,getData: function() {
			var controller = this;
			var service = Ext.create("Sbi.service.RestService", {
				url: "calculatedmembers/initializeData",
				async: true
			});
			Ext.Ajax.request({
				url: service.getRestUrlWithParameters(),
				params: service.getRequestParams(),
				method: 'GET',
				success: function(response){
					var data = response.responseText;
					controller.eventManager.openCalculatedMembersWindow(data);

				}
			});
		}


});





