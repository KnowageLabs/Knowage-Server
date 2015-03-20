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

Sbi.qbe.RelationshipsWizard = function(config) {
	 
	var defaultSettings = {
		title : LN('sbi.qbe.relationshipswizard.title')
		, width : 700
		, height : 400
		, mainGridConfig : {
			width : 300
		}
	};
	  
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.relationshipswizard) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.relationshipswizard);
	}
	  
	var c = Ext.apply(defaultSettings, config || {});
	  
	Ext.apply(this, c);
	  
	this.services = this.services || new Array(); 

	this.init();
	 
	c = Ext.apply(c, {
		//layout: 'column'
		//, layoutConfig: {
		//	columns: 100
		//}
		layout : 'border'
		, items : [this.mainGrid, this.detailGrid]
	});

	// constructor
	Sbi.qbe.RelationshipsWizard.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.qbe.RelationshipsWizard, Ext.Panel, {
    
    services : null
    , mainGrid : null
    , detailGrid : null
    , mainStore : null
    , detailStore : null
    , ambiguousFields : null // must be set in the object passed to the constructor
    , pathSeparator: ' - '
    , detailFilterOtherEntities: false//filter activation flag linked to the button that filter the paths on the detail panel if exists in the path an entity not present in the fields of the query
   
    // private methods
    ,
    init : function () {
    	this.initMainStore();
    	this.initDetailStore();
    	this.initMainGrid(this.mainGridConfig || {});
    	this.initDetailGrid(this.detailGridConfig || {});
    }

    ,
    initMainStore : function () {
    	this.mainStore = new Ext.data.JsonStore({
    		idProperty : 'entity' 
    		,fields : [ 'id', 'name', 'entity', 'choices','queryFieldName','queryFieldAlias','queryFieldType' ]
    		, data : this.ambiguousFields 
    	});
    }
    
    ,
    initDetailStore : function () {
    	this.detailStore = new Ext.data.JsonStore({
    		idIndex : 0
    	    , fields : [ 'path', 'active', 'nodes', 'start', 'end' ]
    		, data : []
    	});
    	this.detailStore.on('load', this.setRecordsPath, this);
    }
    
    ,
    setRecordsPath : function ( theStore, records, options ) {
    	for (var i = 0; i < records.length; i++) {
    		var aRecord = records[i];
    		aRecord.set('path', this.getRecordPath(aRecord));
    	}
    	theStore.commitChanges();
    }
    
    ,
    getRecordPath : function (aRecord) {
    	var toReturn = '';
    	var nodes = aRecord.get('nodes');
    	var lastTarget = null; // this is useful because the relations are undirected, therefore I have to understand the target of a node
    	for (var i = 0; i < nodes.length; i++) {
    		var node = nodes[i];
    		if (i == 0) {
    			if(this.useRelation){
    				toReturn = '<b>'+node.sourceName+'</b>' + this.pathSeparator+'<i>' + node.relationshipName + '</i>'+this.pathSeparator + '<b>'+node.targetName+'</b>';
    			}else{
    				toReturn = '<b>'+node.sourceName+'</b>(' + node.sourceFields+')'+this.pathSeparator + '('+node.targetFields+')<b>'+node.targetName+'</b>';
    			}
    			
    			
    			lastTarget = node.targetName;
    			if(nodes.length>1){
    				var secondNode = nodes[1];
    				// if in the first relation the source and the target are inverse ordered
    				if(secondNode.sourceName==node.sourceName || secondNode.targetName==node.sourceName){
    	    			if(this.useRelation){
    	    				toReturn = '<b>'+node.targetName+'</b>' + this.pathSeparator+'<i>' + node.relationshipName + '</i>'+this.pathSeparator + '<b>'+node.sourceName+'</b>';
    	    			}else{
    	    				toReturn = '<b>'+node.targetName+'</b>(' + node.targetFields+')'+this.pathSeparator +"(" +node.sourceFields+')<b>'+node.sourceFields+'</b>';
    	    			}
    	    			lastTarget = node.sourceName;
    				}
    			}
    		} else {
    			var nextTarget = node.targetName == lastTarget ? node.sourceName : node.targetName;
    			if(this.useRelation){
    				toReturn += this.pathSeparator+'<i>' + node.relationshipName + '</i>'+this.pathSeparator+ '<b>'+nextTarget+'</b>';
    			}else{
    				toReturn +='(' + node.sourceFields+')'+ this.pathSeparator + '('+node.targetFields+')<b>'+node.targetName+'</b>';
    			}
    			
    			
    			lastTarget = nextTarget;
    		}
    	}
    	return toReturn;
    }
    
    ,
    initMainGrid: function (gridConfig) {
	    this.mainGrid = new Ext.grid.GridPanel(Ext.apply(gridConfig || {}, {
	        store : this.mainStore
	        , colModel: new Ext.grid.ColumnModel({
	            columns: [
	             //   {header: LN('sbi.qbe.relationshipswizard.columns.queryName'), dataIndex: 'queryFieldAlias'}
	              //  , {header: LN('sbi.qbe.relationshipswizard.columns.fieldName'), dataIndex: 'name'}
	                 {header: LN('sbi.qbe.relationshipswizard.columns.entity'), dataIndex: 'entity', id: 'entity'}
	            ]
	        })
	        , sm : new Ext.grid.RowSelectionModel({singleSelect : true})
	        , frame : true
	        , border : true  
	        , collapsible : false
	        , autoExpandColumn: 'entity'
	        , layout : 'fit'
	    	, collapsible: true
	    	, region : 'west'
		}));
	    this.mainGrid.on('rowclick', this.mainGridOnRowclickHandler, this);
    }
    
    ,
    initDetailGrid: function (gridConfig) {
 	    this.detailGrid = new Ext.grid.GridPanel(Ext.apply(gridConfig || {}, {
	        store : this.detailStore
	        , colModel: new Ext.grid.ColumnModel({
	            columns: [{
	            	header: LN('sbi.qbe.relationshipswizard.columns.path')
	            	, autoWidth: true
	                , dataIndex: 'path'
	                , renderer: this.getCellTooltip
	                , scope: this
	            }]
	        })
	        , sm : new Ext.grid.RowSelectionModel({singleSelect : true})
	        , frame : true
	        , border : true  
	        , collapsible : false
	        , layout : 'fit'
	        , viewConfig : {
	            forceFit : true
				, getRowClass : function(row, index) {
					var cls = '';
					var data = row.data;
					if (data.active == true) {
						cls = 'green-row'
					}
					return cls;
				}
	        }
	    	, region : 'center'
	    	, tbar: [
			{
				text: LN('sbi.qbe.relationshipswizard.buttons.filter.otherentities'),
				handler: this.filterPathWithOtherEntities,
				scope: this
				}	,'->', {
				text: LN('sbi.qbe.relationshipswizard.buttons.applytoentity'),
				handler: this.applyToEntityHandler,
				scope: this
			 }]
		}));
 	   this.detailGrid.on('rowdblclick', this.detailGridOnRowdblclickHandler, this);
    }
    
    /**
     * Filters the store of the detail panel:
     * remove from the view all the paths with entities not contained in the entities involved by the query 
     */
    , filterPathWithOtherEntities: function(button,event,keepFilterActive){
    	var entities = this.getMainStroreEntities();
    	if(!keepFilterActive){
    		//toggle the filter activation
    		this.detailFilterOtherEntities = !this.detailFilterOtherEntities;
    	}
    	this.detailStore.filter([
    	                         {
    	                        	 fn   : function(aRecord) {
    	                        		 if(!this.detailFilterOtherEntities){
    	                        			 return true;
    	                        		 }
    	                        		 var nodes = aRecord.get('nodes');
    	                        		 for (var i = 0; i < nodes.length; i++) {
    	                        			 var node = nodes[i];
    	                        			 var source = node.sourceName;
    	                        			 var target = node.targetName;
    	                        			 if(entities.indexOf(source)<0 || entities.indexOf(target)<0){
    	                        				 return false;
    	                        			 }
    	                        		 }
    	                        		 return true;

    	                        	 },
    	                        	 scope: this
    	                         }
    	                         ]);


    }
    ,
    getMainStroreEntities: function(){
    	var entities = new Array();
    	if(this.mainStore){
        	this.mainStore.each(function (aRecord) {
    			if (aRecord.get('entity')) {
    				entities.push(aRecord.get('entity'));
    			}
    		},this);
    	}
    	return entities;
    }

    ,
    mainGridOnRowclickHandler : function ( theGrid, rowIndex, e ) {
    	var choises = this.getOptionsByIndex(rowIndex);
    	this.detailStore.loadData(choises);
    	//apply active filters on the choices in the detail panel 
    	this.filterPathWithOtherEntities(null,null,true);
    }
    
    ,
    getOptionsByIndex : function ( rowIndex ) {
    	var record = this.mainStore.getAt(rowIndex);
    	var choises = record.data.choices;
    	return choises;
    }
    
    ,
    detailGridOnRowdblclickHandler : function ( theGrid, rowIndex, e ) {
    	this.toggleOptionByIndex(rowIndex);
    }
    
    ,
    toggleOptionByIndex : function ( rowIndex ) {
    	//this.removeCurrentActive();
		var activeRecord = this.detailStore.getAt(rowIndex);
		var active = activeRecord.get('active');
		activeRecord.set('active', !active);
		this.storeChangesInMainStore(false);
    }
    
    ,
    storeChangesInMainStore : function (applyToEntireEntity) {
    	var selectedRecord = this.mainGrid.getSelectionModel().getSelected();
    	var options = this.getDetailStoreContent();
    	if (applyToEntireEntity) {
    		// apply modifications on all fields of the same entity of the current selected field
    		this.mainStore.each(function (aRecord) {
    			if (aRecord.get('entity') == selectedRecord.get('entity')) {
    				aRecord.set('choices', options);
    			}
    		}, this);
    	} else {
    		// apply modifications only on the selected field
        	selectedRecord.set('choices', options);
    	}
    }
    

    ,
    getDetailStoreContent: function () {
    	var toReturn = [];
    	this.detailStore.each(function (aRecord) {
    		toReturn.push(aRecord.data);
    	});
    	return toReturn;
    }
    
    /*
    ,
    removeCurrentActive : function () {
    	var activeIndex = this.detailStore.find( 'active', true );
    	if (activeIndex > -1) {
    		var activeRecord = this.detailStore.getAt(activeIndex);
    		activeRecord.set('active', false);
    	}
    }
    */
    
    /*
	,
	getUserChoiceForField : function (theFieldRecord) {
		var toReturn = [];
		var choices = theFieldRecord.data.choices;
		var length = choices.length, element = null;
		for ( var i = 0; i < length; i++) {
			element = choices[i];
			if (element.active) {
				toReturn.push(element);
			}
		}
		return toReturn;
	}
	*/
	
	,
	getCellTooltip: function (value, cell, record) {
	 	var path = record.data.path;
	 	var items;
	 	if(this.useRelation){
	 		items = path.split(this.pathSeparator);
	 	}else{
	 		var expr = ")"+this.pathSeparator+"(";
	 		var contained = true;
	 		while (contained){
	 			path = path.replace(expr,'-->');
	 			contained = path.indexOf(expr)>=0;
	 		}
	 		
	 		
	 		contained = true;
	 		while (contained){
	 			path = path.replace(")","(");
	 			contained = path.indexOf(")")>=0;
	 		}
	 		
	 		items = path.split("(");
	 	}
	 	
	 	var tooltipString = '';
	 	for (var i = 0; i < items.length; i++) {
	 		for (var j = 0; j < i; j++) {
	 			tooltipString += '&nbsp;&nbsp;&nbsp;';
	 		}
	 		tooltipString += items[i] + '<br/>';
	 	}
	 	if (tooltipString !== undefined && tooltipString != null) {
	 		cell.attr = ' ext:hide="user" ext:qtip="'  + Sbi.qbe.commons.Utils.encodeEscapes(tooltipString)+ '"';
	 	}
	 	return value;
	}
	
	/*
	,
	removeNonActiveOptions : function (aFieldData) {
		var newArray = [];
		var oldArray = aFieldData['choices'];
		for (var i = 0; i < oldArray.length; i++) {
			var option = oldArray[i];
			if (option.active) {
				delete option.active;
				newArray.push(option);
			}
		}
		aFieldData['choices'] = newArray;
	}
	*/
	
	,
	applyToEntityHandler : function () {
		this.storeChangesInMainStore(true);
	}
	
	// public methods
	,
	getUserChoices : function() {
    	var toReturn = [];
    	this.mainStore.each(function (aRecord) {
    		var clone = Ext.apply({}, aRecord.data);
    		//the store contains only a field for each entity
    		for(var i=0; i<this.ambiguousFields.length; i++){
    			var field = this.ambiguousFields[i];
    			if(field.entity == clone.entity){
    				field.choices = clone.choices;
    				toReturn.push(field);
    			}
    		}
    	}, this);
    	return toReturn;
	}
    
});