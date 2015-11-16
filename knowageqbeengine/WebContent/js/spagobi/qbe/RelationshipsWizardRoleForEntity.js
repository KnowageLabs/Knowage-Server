/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/







/**
 * Object name 
 * 
 * [description]
 * 
 * 
 * Public Properties
 * 
 * [list]
 * 
 * 
 * Public Methods
 * 
 *  [list]
 * 
 * 
 * Public Events
 * 
 *  [list]
 * 
 * Authors
 * 
 * - Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.qbe");

Sbi.qbe.RelationshipsWizardRoleForEntity = function(config) {

	var defaultSettings = {
//			roleEntityConfig: {
//	        	   name: "entity2",
//	        	   aliases: [
//	        	             {alias :"alias21",  fields: [{name:"field1"},{name:"field3"}]},
//	        	             {alias :"alias22",  fields: [{name:"field2"}]}],
//	        	   fields:[
//	        	           {name: "field1"},
//	        	           {name: "field2"},
//	        	           {name: "field4"}
//	        	           ]
//	           }

	};

	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.relationshipswizardroleforentity) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.relationshipswizardroleforentity);
	}

	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);
	
	if(this.roleEntityConfig){
		

		this.addEvents();
		this.init();
		this.services = this.services || new Array(); 

		//this.init();
		c = Ext.apply(c, {
			layout       : 'hbox',
			title: this.roleEntityConfig.name || "Entity",
			layoutConfig : { align : 'stretch' },
			items        : [
			                this.entitiesGrid,
			                this.entityFieldsCard ,
			                this.fieldGrid
			                ]
		});
	}


	// constructor
	Sbi.qbe.RelationshipsWizardRoleForEntity.superclass.constructor.call(this, c);

	this.on("afterlayout",function(){
		this.entitiesGrid.getSelectionModel().selectFirstRow();
		if(this.previous){
			this.setFormState(this.previous);
		}
	},this)

};

Ext.extend(Sbi.qbe.RelationshipsWizardRoleForEntity, Ext.Panel, {

	init: function(){
		this.sortRoleEntityConfig();
		this.initFieldsGrid();
		this.initAliasGrid();
		this.initEntityFieldsCard();

	},
	
	sortRoleEntityConfig: function(){
		var aliases= new Array();
		for(var i=0; i<this.roleEntityConfig.aliases.length; i++){
			aliases.push(this.roleEntityConfig.aliases[i].alias);
		}
		
		//sort the aliases
		aliases.sort();
		var roleEntityConfigSorted = new Array();
		
		//sort the alias entities
		for(var j=0; j<aliases.length; j++){
			var alias = aliases[j];
			for(var i=0; i<this.roleEntityConfig.aliases.length; i++){
		    	var aliasunsorted = this.roleEntityConfig.aliases[i].alias;
		    	if(alias == aliasunsorted){
		    		roleEntityConfigSorted.push(this.roleEntityConfig.aliases[i]);
		    		//this.roleEntityConfig.aliases[i]= null;
		    		break;
		    	}	    	
			}
		}
		
		this.roleEntityConfig.aliases = roleEntityConfigSorted;
	},

	initAliasGrid: function( gridConfig){

		var entityAliases = new Array();

		for(var i=0; i<this.roleEntityConfig.aliases.length; i++){
	    	var alias = this.roleEntityConfig.aliases[i].alias;
	    	var role = this.roleEntityConfig.aliases[i].role;
	    	

			entityAliases.push({
				alias: this.roleEntityConfig.aliases[i].alias,
				aliasTooltip: this.roleEntityConfig.aliases[i].aliasTooltip
			});
		}

		// create the data store
		var entitiesStore = new Ext.data.JsonStore({
			fields : [{name: 'alias', mapping : 'alias'},{name: 'aliasTooltip', mapping : 'aliasTooltip'}],
			data   : {
				records: entityAliases
			},
			root   : 'records'
		});
		
		var sm =  new Ext.grid.RowSelectionModel({singleSelect : true});
		// Column Model shortcut array
		var cols = [
		            { id : 'alias', header:  LN('sbi.qbe.relationshipswizard.roles.entity.alias.columns'), sortable: true, dataIndex: 'alias', renderer: this.renderEntityAlias}
		            ];

		this.entitiesGrid = new Ext.grid.GridPanel(Ext.apply(gridConfig || {}, {
			store : entitiesStore
			, columns: cols
			, sm : sm
			, stripeRows       : true
			, autoExpandColumn : 'alias'
			, title:  LN('sbi.qbe.relationshipswizard.roles.entity.alias.title')+this.roleEntityConfig.name 
			, flex: 1
		}));

		sm.on('rowselect', function(selectionmodel,rowNum){
			this.entityFieldsCard.getLayout().setActiveItem(rowNum);
		}, this);

	},

	renderEntityAlias: function (value, cell, record) {
	 	var tooltipString = record.data.aliasTooltip;

	 	if (tooltipString !== undefined && tooltipString != null) {
	 		cell.attr = ' ext:qtip="'   + Sbi.qbe.commons.Utils.encodeEscapes(tooltipString)+ '"';
	 	}
	 	return value;
	},
	

	initFieldsGrid: function(){
		// Generic fields array to use in both store defs.
		this.fields = [{name: 'name', mapping : 'name'},{name:'queryFieldAlias',mapping:"queryFieldAlias"} ];

		// create the data store
		var fieldsGridStore = new Ext.data.JsonStore({
			fields : this.fields,
			idProperty: "queryFieldAlias",
			data   : this.roleEntityConfig,
			root   : 'fields'
		});

		// Column Model shortcut array
		this.cols = [
		            { id : 'name', header:  LN('sbi.qbe.relationshipswizard.roles.field.column'), sortable: true, dataIndex: 'queryFieldAlias'}
		            ];

		// declare the source Grid
		this.fieldGrid = new Ext.grid.GridPanel({
			flex: 1,
			ddGroup          : 'fieldsGridDDGroup',
			store            : fieldsGridStore,
			columns          : this.cols,
			enableDragDrop   : true,
			stripeRows       : true,
			autoExpandColumn : 'name',
			title            : LN('sbi.qbe.relationshipswizard.roles.field.title')
		});

		this.fieldGrid.on("render",function(){
			var thisPanel = this;
			var fieldsGridDropTargetEl =  this.fieldGrid.getView().scroller.dom;
			var fieldsGridDropTarget = new Ext.dd.DropTarget( fieldsGridDropTargetEl, {
				ddGroup    : 'entityGridDDGroup',
				notifyDrop : function(ddSource, e, data){
					var records =  ddSource.dragData.selections;
					Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
					thisPanel.fieldGrid.store.add(records);
					thisPanel.fieldGrid.store.sort('name', 'ASC');
					return true
				}
			});

		},this);

	},


	initEntityFieldsCard: function(){

		this.entitiyFieldsGrids = new Array();

		for(var i=0; i<this.roleEntityConfig.aliases.length; i++){
			var entitiyFieldsGrid = this.buildEntityFieldsGrid(this.roleEntityConfig.aliases[i]);
			this.entitiyFieldsGrids.push(entitiyFieldsGrid);
		}

		this.entityFieldsCard = new Ext.Panel({
			items: this.entitiyFieldsGrids,
			layout: "card",
			flex: 1
		});

	},

	buildEntityFieldsGrid: function(entityAlias){

		var alias = entityAlias.alias;
    	var role = entityAlias.role;
    	
		var entityGridStore = new Ext.data.JsonStore({
			fields : this.fields,
			data   : entityAlias,
			root   : 'fields',
			idProperty: "queryFieldAlias"
		});

		var cols = [
			            { id : 'name', header: LN('sbi.qbe.relationshipswizard.roles.entity.alias.field.column'), sortable: true, dataIndex: 'queryFieldAlias'}
			            ];
		
		
		// create the destination Grid
		var entitiyFieldsGrid = new Ext.grid.GridPanel({
			ddGroup          : 'entityGridDDGroup',
			store            : entityGridStore,
			columns          : cols,
			enableDragDrop   : true,
			stripeRows       : true,
			autoExpandColumn : 'name',
			title            : alias,
			myEntityAlias: entityAlias
		});

		entitiyFieldsGrid.on("render",function(){
			// This will make sure we only drop to the view scroller element
			var entityGridDropTargetEl = entitiyFieldsGrid.getView().scroller.dom;
			var entityGridDropTarget = new Ext.dd.DropTarget(entityGridDropTargetEl, {
				ddGroup    : 'fieldsGridDDGroup',
				notifyDrop : function(ddSource, e, data){
					var records =  ddSource.dragData.selections;
					Ext.each(records, ddSource.grid.store.remove, ddSource.grid.store);
					entitiyFieldsGrid.store.add(records);
					entitiyFieldsGrid.store.sort('name', 'ASC');
					return true
				}
			});

		},this);

		
		return entitiyFieldsGrid;

	},
		
	setFormState: function(state){
		for(var i=0; i<state.length; i++){
			var aState = state[i];
			for(var y=0; y<state.length; y++){
				var aEntitiyFieldsGrids = this.entitiyFieldsGrids[y];
				if(aEntitiyFieldsGrids.myEntityAlias.role.rel==aState.role){
					var store = aEntitiyFieldsGrids.getStore();
					store.loadData(aState);
					break;
				}
			}
		}
		this.fieldGrid.getStore().removeAll();
	},
	
	getFormState: function(){
		var state = new Array();
		for(var i=0; i<this.entitiyFieldsGrids.length; i++){
			var aEntitiyFieldsGrids = this.entitiyFieldsGrids[i];
			var aState = {
				name : aEntitiyFieldsGrids.myEntityAlias.name,
				role : aEntitiyFieldsGrids.myEntityAlias.role.rel,
				alias : aEntitiyFieldsGrids.myEntityAlias.alias,
				fields : this.getFields(aEntitiyFieldsGrids.store.data.items)
			}
			state.push(aState);
			
		}
		return state;
	}

	, getFields: function(items){
		var fieldsSerialized = new Array();
		if(items){
			for(var i=0; i<items.length; i++){
				var field = items[i];
				var serializedField= field.json;
				fieldsSerialized.push(serializedField);
			}
		}
		return fieldsSerialized;
	}
	
	, validate: function(){
		var errors = new Array();
		if(this.fieldGrid && this.fieldGrid.getStore() && this.fieldGrid.getStore().getCount()>0 ){
			errors.push(this.roleEntityConfig.name+": "+LN('sbi.qbe.relationshipswizard.roles.validation.no.all.fields'));
		}
		for(var i=0; i<this.entitiyFieldsGrids.length; i++){
			var aEntitiyFieldsGrids = this.entitiyFieldsGrids[i];
			if(aEntitiyFieldsGrids.getStore().getCount()==0){
				errors.push(aEntitiyFieldsGrids.myEntityAlias.alias+": "+LN('sbi.qbe.relationshipswizard.roles.validation.alias.no.fieds'));
			}
		}
		return errors;
	}

});