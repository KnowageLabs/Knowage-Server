/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 * 
 * Container for the grid and a detail panel. It define a layout and provides the stubs methods for the communication between the grid and the panel.
 * This methods should be overridden to define the logic. Only the methods onAddNewRow, onGridSelect and onCloneRow are implemented.
 * The configuration detailPanel must be passed to the object. This is the panel on the right. It should contains the method setFormState (it is used by onGridSelect)
 * 
 * 
 * 		@example
 *	Ext.define('Sbi.tools.datasource.DataSourceListDetailPanel', {
 *   extend: 'Sbi.widgets.compositepannel.ListDetailPanel'
 *	, constructor: function(config) {
 *		//init services
 *		this.initServices();
 *		//init form
 *		this.form =  Ext.create('Sbi.tools.datasource.DataSourceDetailPanel',{services: this.services});
 *		this.columns = [{dataIndex:"DATASOURCE_LABEL", header:"Name"}, {dataIndex:"DESCRIPTION", header:"description"}];
 *		this.fields = ["DATASOURCE_ID","DATASOURCE_LABEL","DESCRIPTION","DRIVER","DIALECT_ID","DIALECT_CLASS","DIALECT_NAME","JNDI_URL","USER","PASSWORD","SCHEMA","MULTISCHEMA","CONNECTION_URL"];
 *		this.form.on("save",this.onFormSave,this);
 *    	this.callParent(arguments);
 *    }
 *	, initServices: function(baseParams){
 *		this.services["getAllValues"]= Sbi.config.serviceRegistry.getRestServiceUrl({
 *			    							serviceName: 'datasources/listall'
 *			    							, baseParams: baseParams
 *			    						});
 *		...
 *		    	
 *	}
 *	, onDeleteRow: function(record){
 *		Ext.Ajax.request({
 *  	        url: this.services["delete"],
 *  	        params: {DATASOURCE_ID: record.get('DATASOURCE_ID')},
 *  	       ...
 *	}
 *	, onFormSave: function(record){
 *		Ext.Ajax.request({
 *  	        url: this.services["save"],
 *  	        params: record,
 *  	   ...
 *	}
 *});
 *			... 
 *
 * 
 * @author
 * Alberto Ghedin (alberto.ghedin@eng.it)
 */

Ext.define('Sbi.widgets.compositepannel.ListDetailPanel', {
    extend: 'Ext.Panel'

    ,config: {
    	/**
    	 * Name Of the model to use
    	 */
    	modelName: null,
    	/**
    	 * Grid object
    	 */
    	grid: null,//the grid on the left
    	/**
    	 * the form panel on the right
    	 */
    	detailPanel: null,
    	/**
    	 * Configuration object for the widgets buttons to add in every row of the grid
    	 */
    	buttonColumnsConfig:null,
    	/**
    	 * Configuration object for the buttons to add in the toolbar. {@link Sbi.widget.grid.StaticGridDecorator#StaticGridDecorator}
    	 */
    	buttonToolbarConfig: null,
    	/**
    	 * Configuration object for the combo to add in the toolbar. 
    	 * Like this:
    	 * 			this.customComboToolbarConfig = {
					data: [{
							"name": "Name 1",
							"value": "Value 1",		        
						}, {
							"name": "Name 2",
							"value": "Value 2",	
						}
					],
					fields: ["name","value"],
					displayField: "name",
					valueField: "value"
			
			}
    	 */
    	customComboToolbarConfig: null,
    	/**
    	 * The definition of the columns of the grid. {@link Sbi.widgets.store.InMemoryFilteredStore#InMemoryFilteredStore}
    	 */
    	columns: [],
    	/**
    	 * The list of the properties that should be filtered 
    	 */
    	filteredProperties: new Array(),
    	/**
    	 * Object with internal properties to filter
    	 */
    	filteredObjects: null
    }

	/**
	 * In this constructor you must pass the right panel (detailPanel)
	 */
	, constructor: function(config) {
		this.initConfig(config);
		this.layout = 'border';
		
		if( !this.detailPanel ) {
			alert('The detailPanel must be defined');
			throw "The detailPanel must be defined";
		}
		
		var fixedGridPanelConf = {
			pagingConfig:{},
			storeConfig:{ 
				pageSize: 5
			},
			columnWidth: 2/5,
			buttonToolbarConfig: this.buttonToolbarConfig,
			buttonColumnsConfig: this.buttonColumnsConfig,
			customComboToolbarConfig: this.customComboToolbarConfig,
			modelName: this.modelName,
			columns: this.columns,
			filterConfig: {},
			filteredProperties: this.filteredProperties,
    		filteredObjects: this.filteredObjects

		};
		
		Ext.apply(this,config||{});
		
		this.grid = Ext.create('Sbi.widgets.grid.FixedGridPanelInMemoryFiltered', fixedGridPanelConf);
		

		this.grid.region = "west";
		this.grid.width = "35%";
		this.detailPanel.region = "center";
		this.items = [this.grid, this.detailPanel];
			
		this.callParent(arguments);
		
		//ADD THE EVENTS HANDLERS:
		//grid
		this.grid.on("select", this.onGridSelect, this);
		this.grid.on("selectionchange", this.onGridSelectionChange, this);
		this.grid.on("clonerow", this.onCloneRow, this);
		this.grid.on("addnewrow", this.onAddNewRow, this);
		this.grid.view.on("deleterow",this.onDeleteRow,this)
		this.grid.view.on("selectrow",this.onSelectRow,this);
		//adjust the size of the grid to fit the height of the container 
		this.on("resize",function(){
			var h = this.getHeight();
			this.grid.setHeight(h);
			var page = Math.floor((h-70)/22);//calculate the number of the rows that the grid can show in a singla page
			this.grid.setPageSize(page);
		},this);
	}
	
	/**
	 * 
	 * @private
	 * Create a new object with empty fields. It takes the fields from the configuration variable fields
	 * @return a new object with empty fields 
	 */
	, createNewRow: function(){
		var record= {};
		for(var i=0; i<this.fields.length;i++){
			record[this.fields[i]]='';
		} 
		return record;
	}
	
	/**
	 * Manage the selection of a row of the grid. It takes the selected record and passes it as argument of the function setFormState of the right panel.
	 */
	, onGridSelect: function(selectionrowmodel, record, index, eOpts){
		this.detailPanel.setFormState(record.data);
	}

	/**
	 * 
	 * @private
	 * Clone the passed record
	 * @return clonation of the record 
	 */
	, cloneRecord: function(record){
		return Ext.apply({},record)
	}
	
	/**
	 * Manages the clone event.
	 * Creates a a new record equal to the selected one 
	 */
	, onCloneRow: function(selectedRecord){
		var record = this.grid.store.add(this.cloneRecord(selectedRecord));
		this.grid.getSelectionModel().select(record);
	}
	
	/**
	 * Adds a new empty row
	 */
	, onAddNewRow: function(){
		var record = this.grid.store.add(this.createNewRow());
		this.grid.getSelectionModel().select(record);
	}

	/**
	 * STUB for the selection change event of the grid. 
	 */
	, onGridSelectionChange: function(selectionrowmodel, selected, eOpts){}
	
	/**
	 * STUB for the selection event of the grid. Thrown when the user click on the button select in the grid
	 */
	, onSelectRow: function(selectedRecord,rowIndex,colIndex){}
	
	/**
	 * STUB for the delete event of the grid. Thrown when the user click on the button delete in the grid
	 */
	, onDeleteRow: function(selectedRecord,rowIndex,colIndex){}
	
});