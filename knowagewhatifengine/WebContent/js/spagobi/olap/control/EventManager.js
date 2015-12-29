/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * This component manage all the events. The standard use case is: the view notify an event to the event manager,
 * the manager decorates it and calls a method of the controller.
 * The controller execute the request and return the result at the event manager that manage the response.<br>
 * It's a Singleton and all classes can notify an event directly to the component
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.control.EventManager', {
	extend: 'Ext.util.Observable',

	/**
	 * @property {Sbi.olap.OlapPanel} olapPanel
	 *  The Olap Panel; the observable
	 */
	olapPanel: null,

	/**
	 * @property {Sbi.olap.execution.OlapExecutionPanel} executionPanel
	 *  Panel that contains the pivot and the chart
	 */
	olapController: null,

	/**
	 * @property {String} lastEditedFormula
	 *  the last edited formula. To restore the formula
	 */
	lastEditedFormula: null,

	/**
	 * @property {String} lastEditedCell
	 *  the last edited formula. To restore the formula
	 */
	lastEditedCell: null,

	/**
	 * @property {String} lockTypeEdit
	 * A flag that block the editing of a cel. The String is the type of the lock
	 */
	lockTypeEdit: null,

	constructor : function(config) {
		this.olapPanel = config.olapPanel;
		this.olapController = Ext.create('Sbi.olap.control.Controller', {eventManager: this});
		this.callParent(arguments);
		this.addEvents(
				/**
				 * [LIST OF EVENTS]
				 */
				/**
				 * @event executeService
				 * This event is thrown when a service is called for execution
				 */
				'executeService',
				/**
				 * @event serviceExecuted
				 * This event is thrown when a service has finished execution
				 * @param {Object} response
				 */
				'serviceExecuted',
				/**
				 * @event serviceExecutedWithError
				 * This event is thrown when a service has finished execution with error
				 * @param {Object} response
				 */
				'serviceExecutedWithError'
		);
		this.on('executeService', this.executeService, this);
		this.on('serviceExecuted', this.serviceExecuted, this);
		this.on('serviceExecutedWithError',this.serviceExecutedWithError, this);

	},


	/**
	 * Notifies the manager that the mdx query is changed
	 * @param {String} mdx the mdx query. If null the server will load the initial mdx query
	 */
	notifyMdxChanged: function(mdx){
		this.olapController.executeMdx(mdx);
	},

	/**
	 * Updates the view after the execution of the mdx query
	 * @param {String} pivotHtml the html representation of the pivot table
	 * @param {boolean} keepState if true keeps the values of the iternal variables
	 */
	updateAfterMDXExecution: function(pivotHtml, keepState){
		var tableJson = Ext.decode(pivotHtml);
		var pivot = Ext.create('Sbi.olap.PivotModel', tableJson);
		this.olapPanel.updateAfterMDXExecution(pivot, tableJson.modelConfig);
		this.loadingMask.hide();

		if(!keepState){
			this.cleanLastEditedFormula();
		}
	},
	hideLoadingMask: function(){
		this.loadingMask.hide();
	},
	/**
	 * Updates the view after drill down operation
	 * @param {int} axis position of the row
	 * @param {int} member position of the member
	 * @param {int} position in the Position array
	 * @param {String} uniqueName unique name of the member
	 */
	drillDown: function(axis, position,  member, uniqueName, positionUniqueName){
		this.olapController.drillDown(axis, member, position, uniqueName, positionUniqueName);
	},
	/**
	 * Updates the view after drill up operation
	 * @param {int} axis position of the row
	 * @param {int} member position of the member
	 * @param {int} position in the Position array
	 * @param {String} uniqueName unique name of the member
	 */
	drillUp: function(axis, position,  member, uniqueName, positionUniqueName){
		this.olapController.drillUp(axis, member, position, uniqueName, positionUniqueName);
	},
	/**
	 * Swaps the axis
	 */
	swapAxis: function(){
		this.olapController.swapAxis();
	},

	/**
	 * Adds a slicer for the hierarchy
	 * @param {Sbi.olap.HierarchyModel} hierarchy to slice
	 * @param {Sbi.olap.MemberModel} member the slicer value
	 */
	addSlicer: function(dimension, member, multiSelection){
		this.olapController.addSlicer(dimension.raw.selectedHierarchyUniqueName, member.uniqueName, multiSelection);
	},

	/**
	 * Swap 2 hierarchies in an axis
	 * @param {Sbi.olap.DimensionModel} hierarchy1 position of the first hierarchy to move
	 * @param {Number} newPosition new position of the dimension
	 * @param {Number} axis
	 */
	moveDimension: function(dimension, newPosition, direction){
		this.olapController.moveHierarchy(dimension.get("selectedHierarchyUniqueName"), dimension.get("axis"), newPosition, direction);
	},

	/**
	 * Move the dimension from an axis to another
	 * @param {Number} dimension1 position of the hierarchy to move
	 * @param {Number} fromAxis axis from witch remove the hierarchy
	 * @param {Number} toAxis axis to witch add the hierarchy
	 */
	moveDimensionToOtherAxis: function(hierarchy1, fromAxis, toAxis){
		this.olapController.moveDimensionToOtherAxis(hierarchy1, fromAxis, toAxis);
	},
	/**
	 * Updates the model configuration based on the toolbar settings
	 * @param {String} config toolbar configuration for the model
	 */
	setModelConfig: function(config){
		this.olapController.setModelConfig(config);
	},

	/**
	 * Place the members on the axis
	 * @param {Sbi.olap:DimensionModel} dimension the dimension
	 * @param {Array} The list of members to place in the axis
	 */
	placeMembersOnAxis: function(dimension, members){
		this.olapController.placeMembersOnAxis(dimension.get("axis"), members);
	},

	/**
	 * Place the members on the axis
	 * @param {Sbi.olap:DimensionModel} dimension the dimension
	 * @param {Array} The list of members to place in the axis
	 */
	showHelp: function(title, content, winConf){
		if(!title){
			title = LN('sbi.olap.help.title');
		}
		var win = Ext.create("Sbi.widgets.Help",Ext.apply({title: title, content: content}, winConf||{}));
		win.show();
	},

	/**
	 * Changes the hierarchy of the dimension
	 * @param axis the axis that contains the hierarchy
	 * @param newHierarchyUniqueName the unique name of the new hierarchy
	 * @param oldHierarchyUniqueName the unique name of the old hierarchy
	 * @param hierarchyPosition the position of the hierarchy
	 */
	updateHierarchyOnDimension: function(axis, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition){
		if (this.olapPanel.pivotModel.get("hasPendingTransformations")) {
			Ext.MessageBox.show({
				title: LN('sbi.common.warning'),
				msg: LN('sbi.olap.execution.table.dimension.cannotchangehierarchy'),
				buttons: Ext.MessageBox.OK,
				icon: Ext.MessageBox.WARNING
			});
		} else {
			this.olapController.updateHierarchyOnDimension(axis, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition);
		}
	},

	/**
	 * Updates the value of the cell
	 * @param id
	 * @param value
	 * @param startValue
	 */
	writeBackCell: function(id, value, startValue, originalValue){
		var type = "float";
		if ( startValue ) {
			startValue = Sbi.whatif.commons.Format.cleanFormattedNumber(startValue, Sbi.locale.formats[type]);
		}
		if ( value != startValue ) {
			var position = "";
			var unformattedValue = value;

			if ( id ) {
				var endPositionIndex = id.indexOf("!");
				position= id.substring(0, endPositionIndex);
			}

			try {
				if ( !isNaN(value) ) {
					//Value is a number
					unformattedValue = Sbi.whatif.commons.Format.formatInJavaDouble(value, Sbi.locale.formats[type]);
				} else {
					//Value is a string/expression
					unformattedValue = value;
				}
			} catch (err) {
				Sbi.error("Error while trying to convert [" + value + "] to a Java double: " + err);
			}

			//update the last edited values
			this.lastEditedFormula = unformattedValue;
			var separatorIndex = id.lastIndexOf('!');
			this.lastEditedCell = id.substring(0,separatorIndex);

			this.olapController.setValue(position, unformattedValue);
		} else {
			Sbi.debug("The new value is the same as the old one");
			var cell = Ext.get(id);
			cell.dom.childNodes[0].data = originalValue;
		}

	},

	/**
	 * Makes editable the dom element with the id equals to the passed id
	 * @param id the id of the dom element to make editable
	 */
	makeEditable: function(id, measureName){
		var unformattedValue = "";
		var modelStatus = null;

		//check the status of the lock
		try {
			modelStatus = this.olapPanel.executionPanel.olapToolbar.modelStatus;
		}catch (e) {};

		if(modelStatus  == 'locked_by_other' || modelStatus  == 'unlocked'){
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.olap.writeback.edit.no.locked'));
			return;
		}

		if(this.olapPanel && this.olapPanel.modelConfig && this.isMeasureEditable(measureName) && !this.lockTypeEdit){
			var cell = Ext.get(id);

			//check if the user is editing the same cell twice. If so we present again the last formula
			if(this.lastEditedFormula && this.lastEditedCell && Ext.String.startsWith( id,  this.lastEditedCell )){
				unformattedValue = this.lastEditedFormula;
			}else{
				var type = "float";
				var originalValue = "";

				try  {
					originalValue = Ext.String.trim(cell.dom.childNodes[0].data);
					if (originalValue  == '') { // in case the cell was empty, we type 0
						unformattedValue = 0;
					} else {
						unformattedValue = Sbi.whatif.commons.Format.cleanFormattedNumber(originalValue, Sbi.locale.formats[type]);
					}
				} catch(err) {
					Sbi.error("Error loading the value of the cell to edit" + err);
				}

				//it's not possible to edit a cell with value 0
				if(unformattedValue ==0){
					Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.olap.writeback.edit.no.zero'));
					return;
				}
			}

			var editor = Ext.create("Ext.Editor", {
				updateEl: true,
				field: {
					xtype: "textfield"
				},
				listeners:{
					complete:{
						fn: function( theEditor, value, startValue, eOpts ){
							this.writeBackCell(id, value, startValue, originalValue);
						},
						scope: this
					}
				}
			});

			editor.startEdit(cell.el, unformattedValue);
		}

		if(this.lockTypeEdit){
			Sbi.exception.ExceptionHandler.showWarningMessage(LN('sbi.olap.writeback.edit.lock.'+this.lockTypeEdit));
		}
	},

	/**
	 * Checks if the measure is editable
	 * @param measureName the name of the measure to check
	 * @returns {Boolean} return if the measure is editable
	 */
	isMeasureEditable: function(measureName){
		if(this.olapPanel.modelConfig && this.olapPanel.modelConfig.writeBackConf){
			if(this.olapPanel.modelConfig.writeBackConf.editableMeasures  == null || this.olapPanel.modelConfig.writeBackConf.editableMeasures.length ==0){
				return true;
			}else{
				var measures = (this.olapPanel.modelConfig.writeBackConf.editableMeasures);
				var contained =  Ext.Array.contains(measures, measureName);
				return contained;
			}
		}
		return false;
	}

	/**
	 * Undo last modification
	 */
	,
	undo : function () {
		this.olapController.undo();
	},

	/**
	 * Cleans the mondrian cache
	 */
	cleanCache: function () {
		this.olapController.cleanCache();
	},

	/**
	 * Persists the transformations in the db and cleans the stack
	 */
	persistTransformations: function () {
		this.olapController.persistTransformations();
	},

	/**
	 * Persists the transformations in the db
	 * creating a new version and cleans the stack
	 */
	persistNewVersionTransformations: function (params) {
		this.olapController.persistNewVersionTransformations(params);
	}
	,
	/**
	 * Call rest service to lock model
	 *
	 */
	lockModel: function () {
		this.olapController.lockModel();
	}
	,
	/**
	 * Call rest service to unlock model
	 */
	unlockModel: function () {
		this.olapController.unlockModel();
	}

	/**
	 * Call the rest service to delete the selected versions
	 */
	,deleteVersions: function(itemsToDelete){
		if(itemsToDelete && itemsToDelete.length>0){
			this.olapController.deleteVersions(itemsToDelete);
		}
	}

	/**
	 * Call the rest service to export the output
	 */
	,exportOutput: function(params){
		this.olapController.exportOutput(params);
	}

	/**
	 * Call the rest service to export the pivot table
	 */
	,exportPivotTable: function(format){
		this.olapController.exportPivotTable(format);
	}

	,executeService: function(text){
		if(!text){
			text = LN("sbi.common.wait");
		}
		this.loadingMask = new Ext.LoadMask(Ext.getBody(), {msg:text});
		this.loadingMask.show();
	}
	, serviceExecuted: function (response, keepState){
		this.updateAfterMDXExecution(response.responseText, keepState);

	}
	, serviceExecutedWithError: function (response, keepStateIfFails){
		this.loadingMask.hide();
		if(!keepStateIfFails){
			this.cleanLastEditedFormula();
		}
	}

	, setLockTypeEdit: function (value){
		this.lockTypeEdit = value;

	}

	, cleanLastEditedFormula: function(){
		this.lastEditedFormula = null;
		this.lastEditedCell = null;
	}


	/**
	* Execute calculated member expression
	*/
	//author: Maria Caterina Russo from Osmosit
	,executeCalculatedMemberExpression: function(name, expression){
		this.olapController.executeCalculatedMemberExpression(name,expression,this.ccParentUniqueName,this.ccAxis);
	}

	,setCalculatedFieldParent: function(uniqueName, axis){
		this.ccParentUniqueName = uniqueName;
		this.ccAxis = axis;
		this.olapController.getData();
	}

	/**
	* Opens the calculated members wizard by double click and set the label of the calculated member name
	*/
	//author: Maria Caterina Russo from Osmosit
	,openCalculatedMembersWindow: function(dimensions) {
		var wizard = Ext.create('Sbi.olap.calculatedmembers.CalculatedMembersWizard',{
			actualVersion: this.olapPanel.modelConfig.actualVersion,
			dimensions: dimensions
		});
		if(this.ccParentUniqueName.indexOf("Measures") > -1)
			{
				var lastIndex = this.ccParentUniqueName.lastIndexOf('[');
				var name = this.ccParentUniqueName.substring(0,lastIndex) +'[';
				Ext.getCmp('calculatedNameLeftId').setText(name);
			} else {Ext.getCmp('calculatedNameLeftId').setText(this.ccParentUniqueName+'.[');}
		wizard.show();
		}


	, setAllocationAlgorithm: function(className){
		this.olapController.setAllocationAlgorithm(className);
	}
	
	, saveSubObject: function(name, description, scope){
		this.olapController.saveSubObject(name, description, scope);
	},

	//author: Maria Caterina Russo from Osmosit
	/** 
	 * cross navigation can only work without writeback configuration
	 */
	initCrossNavigation: function(){
		if(this.checkLockStatusForCrossNavigation()){
			this.olapController.initCrossNavigation();
		}
	},
	
	//author: Maria Caterina Russo from Osmosit
	setTarget: function(targetId,position){
		var index = "";
		if (targetId) {
			var endPositionIndex = targetId.indexOf("_");
			index= targetId.substring(endPositionIndex+1);
		}
		this.olapController.getCrossNavigationUrl(index, position);
	},
	
	//author: Maria Caterina Russo from Osmosit
	/**
	 * create menu with targets titles in the dom element with the id equals to the passed id
	 * @param id the id of the dom element
	 */
	createCrossNavigationMenu: function(id){
		var cell = Ext.get(id);
		if(!this.crossNavigation)
			this.crossNavigation = this.olapPanel.modelConfig.crossNavigation;		
		
		var position = "";
		if (id) {
			var endPositionIndex = id.indexOf("!");
			position= id.substring(0, endPositionIndex);
		}
		var thisPanel = this;
		var targets = new Array();
		targets = this.crossNavigation.targets;		
		cell.el.addListener('click',
				function(e){
			if(!this.checkLockStatusForCrossNavigation()){
				return;
			}
			if(!Ext.getCmp('crossNavigationMenuId'+id)){
				var menu=Ext.create('Ext.menu.Menu', {
					id:'crossNavigationMenuId'+id, 
					plain: true,
				    margin: '0 0 10 0'
				});	
				var thisMenuItem= this;
				if(targets && targets.length>0){
					for(var i=0; i<targets.length; i++){
						var btnId='btnMenuId'+id+'_'+i;
						var button = {
								xtype : 'button',
								text : targets[i].title,							
								
								id: btnId,
								handler: function() {
									 thisMenuItem.setTarget(this.id,position);
									 Ext.getCmp('crossNavigationMenuId'+id).hide();			
								}
						};
						menu.add(button);
					}
				}
				
			}
			
			Ext.getCmp('crossNavigationMenuId'+id).showAt(e.getXY()); },thisPanel);	
	  
	}
});





