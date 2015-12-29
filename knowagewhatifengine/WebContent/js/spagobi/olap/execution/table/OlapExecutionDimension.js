/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * The super class of the filters, rows and columns container
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */


Ext.define('Sbi.olap.execution.table.OlapExecutionDimension', {
	extend: 'Ext.panel.Panel',

	config:{
		/**
		 * @cfg {Sbi.olap.DimensionModel} dimension
		 * The dimension represented by the column
		 */
		dimension: null,
		/**
		 * @cfg {Sbi.olap.execution.table.OlapExecutionPivot} pivotContainer
		 * The container of the columns
		 */
		pivotContainer: null,
		/**
		 * @cfg {Sbi.olap.execution.table.OlapExecutionRow/Column/Filter} containerPanel
		 * The container of the dimension: filters, columns, rows
		 */
		containerPanel: null

	},


	constructor : function(config) {
		this.initConfig(config);
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionDimension) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionDimension);
		}

		this.callParent(arguments);
		this.addDragAndDrop();
	},


	initComponent: function() {

		var items = this.buildItems();

	//	this.on('dimensionClick', function(dimension){alert("cklik");},this);

		Ext.apply(this, {
			frame: true,
			items: items}
		);
		this.callParent();
	},



	/**
	 * Implements the drag and drop of the dimension between filters, rows and columns
	 */
	addDragAndDrop: function(){
		this.on("render",function(){
			var dd = Ext.create('Ext.dd.DDProxy', this.getEl(), 'dimensionDDGroup', {
				isTarget  : false
			});

			var thisPanel = this;

			Ext.apply(dd, {


				onDragDrop : function(evtObj, targetElId) {

					if(thisPanel.containerPanel.getId()!= targetElId && (thisPanel.pivotContainer.olapExecutionFilters.getId() == targetElId || thisPanel.pivotContainer.olapExecutionRows.getId() == targetElId || thisPanel.pivotContainer.olapExecutionColumns.getId() == targetElId)){

						if(thisPanel.pivotContainer.olapExecutionFilters.getId() == targetElId){
							thisPanel.pivotContainer.olapExecutionFilters.moveDimensionToOtherAxis(thisPanel);
						}
						if(thisPanel.pivotContainer.olapExecutionRows.getId() == targetElId){
							thisPanel.pivotContainer.olapExecutionRows.moveDimensionToOtherAxis(thisPanel);
						}
						if(thisPanel.pivotContainer.olapExecutionColumns.getId() == targetElId){
							thisPanel.pivotContainer.olapExecutionColumns.moveDimensionToOtherAxis(thisPanel);
						}
					}

				},
				endDrag : function() {
					// Empty. Just to prevent the user to drag the elements outside the dd area
				}
			});


			Ext.create('Ext.dd.DDTarget', thisPanel.pivotContainer.olapExecutionFilters.getId(), 'dimensionDDGroup');
			Ext.create('Ext.dd.DDTarget', thisPanel.pivotContainer.olapExecutionRows.getId(), 'dimensionDDGroup');
			Ext.create('Ext.dd.DDTarget', thisPanel.pivotContainer.olapExecutionColumns.getId(), 'dimensionDDGroup');
		},this);
	},

	/**
	 * Returns the name of the dimension
	 * @returns
	 */
	getDimensionName: function(){
		var dimensionName = this.dimension.raw.caption;
		return  dimensionName;
	},

	buildMultiHierarchiesButton: function(conf){
		var config = {
			style: "background-color: transparent !important",
			bodyStyle: "background-color: transparent !important",
			html: " ",
			border: false,
			width: 16,
			cls: "multi-hierarchy",
			listeners: {
				render:{
					fn: this.buildDimensionInfoPanel,
					scope: this
				}
			}
		};

		if(this.axisType =="column"){
			Ext.apply(config,{width: 20, height: 15});
		}else{
			Ext.apply(config,{height: 20});
		}
		config = Ext.apply(config, conf||{});
		return config;
	},

	buildDimensionInfoPanel: function(target){

		var thisPanel = this;

		//Build the combo box for the hierarachy selection
		var selectId = Ext.id()+"select";
		var html = "";
		if(this.dimension.raw.hierarchies.length>1){
			html = LN("sbi.olap.execution.table.dimension.selected.hierarchy")+"<i>"+(this.dimension.raw.hierarchies[this.dimension.raw.selectedHierarchyPosition]).caption+"</i>."+LN("sbi.olap.execution.table.dimension.selected.hierarchy.2")+
				"<table>"+
				//"<tr><td>The selected hierarchy is "+(this.dimension.raw.hierarchies[this.dimension.raw.selectedHierarchyPosition]).name+"</td></tr>"+
				"<tr><td class='multihierarchy-font'>"+
				LN('sbi.olap.execution.table.dimension.available.hierarchies')+
				"</td><td>"+
				"<select id = '"+selectId+"'>";

			for(var i=0; i<this.dimension.raw.hierarchies.length; i++){
				html = html+"<option value='"+this.dimension.raw.hierarchies[i].uniqueName+"'";
				if(this.dimension.raw.hierarchies[i].uniqueName  ==thisPanel.dimension.raw.selectedHierarchyUniqueName){
					html = html+" selected='selected' ";
				}

				html = html+">"+this.dimension.raw.hierarchies[i].caption+"</option>";
			}

			html = html+"</select></td></tr></table>";
		}

		//build the tooltip
		var tool = Ext.create('Ext.tip.ToolTip',{
            title: thisPanel.dimension.raw.name,
            target: target.getEl(),
            anchor: 'left',
            autoHide: false,
            html: html,
            closable: true,
            width: 300,
            padding: 5,
            buttons:[
		             '->',    {
		            	 text: LN('sbi.common.cancel'),
		            	 handler: function(){
		            		 tool.close();
		            	 }
		             },    {
		            	 text: LN('sbi.common.ok'),
		            	 handler: function(){
		            			var newHierarchy =Ext.get(selectId).dom.value;
		            			if(thisPanel.dimension.raw.selectedHierarchyUniqueName!=newHierarchy){
		           					thisPanel.updateHierarchyOnDimension(thisPanel.dimension.raw.axis, newHierarchy, thisPanel.dimension.raw.selectedHierarchyUniqueName, thisPanel.dimension.raw.positionInAxis );
		           					Sbi.debug("For the dimension "+thisPanel.dimension.raw.uniqueName+" the new hierarchy is "+ newHierarchy+". Was "+thisPanel.dimension.raw.selectedHierarchyUniqueName);
		            			}else{
		            				Sbi.debug("For the dimension "+thisPanel.dimension.raw.uniqueName+" the new hierarchy is the same of the old one: "+ newHierarchy);
		            			}
		            			 tool.close();
		            	 }
		             }]
        });


	},



	updateHierarchyOnDimension: function(axis, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition){
		Sbi.olap.eventManager.updateHierarchyOnDimension(axis, newHierarchyUniqueName, oldHierarchyUniqueName, hierarchyPosition);

	}


});





