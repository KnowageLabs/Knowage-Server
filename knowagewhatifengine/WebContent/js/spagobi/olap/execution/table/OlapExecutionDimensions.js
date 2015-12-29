/** SpagoBI, the Open Source Business Intelligence suite
 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

/**
 *
 * container of the columns definition of the pivot table
 *
 *
 *  @author
 *  Alberto Ghedin (alberto.ghedin@eng.it)
 */




Ext.define('Sbi.olap.execution.table.OlapExecutionDimensions', {
	extend: 'Ext.panel.Panel',

	config:{
		/**
	     * @cfg {Ext.data.Store} store
	     * The store with the Sbi.olap.execution.table.OlapExecutionDimension
	     */
		store: null,
		/**
	     * @cfg {Sbi.olap.execution.table.OlapExecutionPivot} pivotContainer
	     * The container of the columns
	     */
		pivotContainer: null,
		/**
	     * @cfg {String} dimensionClassName
	     * The name of the children classes
	     */
		dimensionClassName: null,
		/**
	     * @cfg {Number} axisPosition
	     * The position of the axis
	     */
		axisOrdinalPosition: -1
    },


	constructor : function(config) {
		this.initConfig(config);
		this.store = Ext.create('Ext.data.Store', {
		    model: 'Sbi.olap.DimensionModel'
		});
		if(Sbi.settings && Sbi.settings.olap && Sbi.settings.olap.execution && Sbi.settings.olap.execution.table && Sbi.settings.olap.execution.table.OlapExecutionDimensions) {
			Ext.apply(this, Sbi.settings.olap.execution.OlapExecutionDimensions);
		}
		this.callParent(arguments);
	},

	initComponent: function() {
		var items;

		if(this.store && this.store.getCount()>0){

			items = this.getRefreshedItems();
			Ext.apply(this, {items: items});
		}
		Ext.apply(this, {frame: true});
		this.callParent();
	},

    /**
     * Adds the Dimension from in Dimension container
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the Dimension to add
     */
	addDimension: function(dimension){
		this.store.add(dimension.dimension);
		this.refreshItems();
	},

    /**
     * Removes the dimension from the dimension container
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the dimension to remove
     */
	removeDimension: function(dimension){
		this.store.remove(dimension.dimension);
		this.refreshItems();
	},

    /**
     * Adds the Dimension from in Dimension container
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the Dimension to add
     */
	moveDimensionToOtherAxis: function(dimension){
		var originalAxis = dimension.dimension.get("axis");
		if(this.axisOrdinalPosition<0 && dimension.dimension.get("measure")){//can not remove measure
			Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.execution.table.filter.no.measure"));
		}else if(originalAxis>=0 && dimension.containerPanel.store.getCount()<=1){//there must be at least one dimension in the rows and in the columns
			Sbi.exception.ExceptionHandler.showInfoMessage(LN("sbi.olap.execution.table.dimension.no.enough"));
		}else{
			Sbi.olap.eventManager.moveDimensionToOtherAxis(dimension.dimension.get("selectedHierarchyUniqueName"), originalAxis, this.axisOrdinalPosition);
		}

	},


	/**
     * Moves up the dimension
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the dimension to move
	 */
	moveUpDimension: function(dimension){
		this.move(dimension, -1);
	},


	/**
     * Moves down the member
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the dimension to move
	 */
	moveDownDimension: function(dimension){
		this.move(dimension, 1);
	},

	/**
     * Moves the model of pos positions
     * @param {Sbi.olap.execution.table.OlapExecutionDimension} dimension the dimension to remove
	 * @param pos the positions
	 */
	move: function(dimension, pos){
		var index = this.store.indexOf(dimension.dimension);

		if((pos+index)>=0 && (pos+index)<this.store.getCount( )){
			Sbi.olap.eventManager.moveDimension(dimension.dimension, index+pos, pos);
		}
	},

    /**
     * Refresh content
     */
	refreshItems: function(){
		this.removeAll(true);
		this.updatePanelDefaultHtml();
		if(this.store && this.store.getCount()>0){
			var items = this.getRefreshedItems();
			for(var i=0; i<items.length; i++) {
				this.add(items[i]);
			}
		}

	},

    /**
     * Get the refreshed items: builds all the dimensions starting from the store
     */
	getRefreshedItems: function(){
		var items = new Array();

		if(this.store && this.store.getCount()>0){
			var dimensionsCount = this.store.getCount( );
			for(var i=0; i<dimensionsCount; i++) {
				var dimension = Ext.create(this.dimensionClassName,{dimension: this.store.getAt(i), pivotContainer: this.pivotContainer, containerPanel: this, firstDimension: (i ==0), lastDimension: (i ==dimensionsCount-1) });
				dimension.on("moveUp",this.moveUpDimension,this);
				dimension.on("moveDown",this.moveDownDimension,this);
				items.push(dimension);
				if(i<dimensionsCount-1){
					if(this.isColumnDimensions()){
						this.addMoveRightDimensionButton(dimension, items);
					}else if(this.isRowDimensions()){
						this.addMoveDownDimensionButton(dimension, items);
					}
				}

			}
		}

		return items;
	},

	/**
	 * Builds the central panel with the name of the dimension
	 */
	addMoveDownDimensionButton: function(dimension, items){
		items.push( {
			xtype: "panel",
			style: "background-color: transparent !important",
			bodyStyle: "background-color: transparent !important",
			cls:"swap-row-panel",
			border: false,
			html: "  ",
			height: 18,
			listeners: {
				el: {
					click: {
						fn: function () {
							this.fireEvent("moveDown",this);
						},
						scope: dimension
					}
				}
			}
		});
	},
	/**
	 * Builds the central panel with the name of the dimension
	 */
	addMoveRightDimensionButton: function(dimension, items){
		items.push( {
			xtype: "panel",
			style: "background-color: transparent !important",
			bodyStyle: "background-color: transparent !important",
			cls:"swap-column-panel",
			border: false,
			html: "  ",
			width: 18,
			listeners: {
				el: {
					click: {
						fn: function () {
							this.fireEvent("moveDown",this);
						},
						scope: dimension
					}
				}
			}
		});
	},

	/**
	 * Updates the visualization after the execution of a a mdx query
	 * @param pivotModel {Array} the list of dimensions to add
	 * @param axisOrdinalPosition {Number} the ordinal position of the axis
	 */
	updateAfterMDXExecution: function(dimensions, axisOrdinalPosition){
		this.axisOrdinalPosition = axisOrdinalPosition;
		this.store.removeAll();
		if(dimensions){
			for(var i=0; i<dimensions.length; i++){
				this.store.add(Ext.create("Sbi.olap.DimensionModel", dimensions[i]));
			}
		}
		this.refreshItems();
	},

	/**
	 * Returns true if this is the  container
	 */
	isRowDimensions: function(){
		return this.dimensionClassName  == 'Sbi.olap.execution.table.OlapExecutionRow';
	},

	/**
	 * Returns true if this is the columns container
	 */
	isColumnDimensions: function(){
		return this.dimensionClassName  == 'Sbi.olap.execution.table.OlapExecutionColumn';
	},

	/**
	 * Updates the content of the panel.. It is useful when the region (for example filters container) is empty, so we should
	 * show a message. It should be implemented by the subclasses
	 */
	updatePanelDefaultHtml: function(){}

});





