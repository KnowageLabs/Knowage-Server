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
  * - name (mail)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.SelectGridDropTarget = function(selectGridPanel, config) {
	
	var c = Ext.apply({
		ddGroup    : 'gridDDGroup',
		copy       : false
	}, config || {});
	

	this.targetPanel = selectGridPanel;
	this.targetGrid = this.targetPanel.grid; 
	this.targetElement = this.targetGrid.getView().el.dom.childNodes[0].childNodes[1];
	
	// constructor
    Sbi.qbe.SelectGridDropTarget.superclass.constructor.call(this, this.targetElement, c);
};

Ext.extend(Sbi.qbe.SelectGridDropTarget, Ext.dd.DropTarget, {
    
    services: null
    , targetPanel: null
    , targetGrid: null
    , targetElement: null
   
    , notifyOver : function(ddSource, e, data){
    	return (ddSource.grid &&  ddSource.grid.type === 'filtergrid')? this.dropNotAllowed : this.dropAllowed;
    }
		
	, notifyDrop : function(ddSource, e, data){
    	
		// the row index on which the tree node has been dropped on
		var rowIndex;

		if(this.targetGrid.targetRow) {
			rowIndex = this.targetGrid.getView().findRowIndex( this.targetGrid.targetRow );
		}

		if(rowIndex == undefined || rowIndex === false) {
			// append the new row
			rowIndex = undefined;
		}  
		
		var sourceObject;
      	if(ddSource.tree) {
        	this.notifyDropFromDatamartStructureTree(ddSource, e, data, rowIndex);
      	} else if(ddSource.grid &&  ddSource.grid.type === 'selectgrid') {
        	this.notifyDropFromSelectGrid(ddSource, e, data, rowIndex);
      	} else if(ddSource.grid &&  ddSource.grid.type === 'filtergrid') {
        	this.notifyDropFromFilterGrid(ddSource, e, data);
      	} else if(ddSource.grid &&  (ddSource.grid.type === 'parametersgrid' || ddSource.grid.type === 'documentparametersgrid')) {
			Ext.Msg.show({
				   title:'Drop target not allowed',
				   msg: 'Parameters cannot be dropped here!',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
      	} else {
        	alert('Source object: unknown');
      	}        
	}

	// =====================================================================================
	// from TREE
	// =====================================================================================
	, notifyDropFromDatamartStructureTree: function(ddSource, e, data, rowIndex) {
		this.addNodeToSelect(ddSource.dragData.node, rowIndex);
	} 
	
	, addNodeToSelect: function(node, rowIndex, recordBaseConfig) {
	     
	    var nodeType;
	        		
		nodeType = node.attributes.type || node.attributes.attributes.type;

	    if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
	    	this.addSimpleNodeToSelect(node, rowIndex, recordBaseConfig);	        	
	    } else if(nodeType == Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD){
	    	this.addCalculatedNodeToSelect(node, rowIndex, recordBaseConfig);
	    } else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD){
	    	this.addInLineCalculatedNodeToSelect(node, rowIndex, recordBaseConfig);
	    } else if(nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY){
			this.addEntityNodeToSelect(node, rowIndex, recordBaseConfig);	
		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_RELATION){
			//no action requeired for relation
		}   else {
	       	Ext.Msg.show({
			   title:'Drop target not allowed',
			   msg: 'Node of type [' + nodeType + '] cannot be dropped here',
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});
	    }

	    this.targetGrid.getView().refresh();
	}

	
	, addSimpleNodeToSelect: function(node, rowIndex, recordBaseConfig) {
		var field = {
			id: node.id , 
        	entity: node.attributes.attributes.entity , 
        	field: node.attributes.attributes.field,
        	alias: node.attributes.attributes.field,
        	longDescription: node.attributes.attributes.longDescription
        };
        
		recordBaseConfig = recordBaseConfig || {};
		Ext.apply(field, recordBaseConfig);
		
        this.targetPanel.addField(field, rowIndex);
	}
	
	, addCalculatedNodeToSelect: function(node, rowIndex, recordBaseConfig) {
		
	
		var field = {
	       	id: node.attributes.attributes.formState,
	       	type: Sbi.constants.qbe.FIELD_TYPE_CALCULATED,
//	        entity: node.attributes.attributes.entity , 
//	        field: node.attributes.attributes.field,
//	        alias: node.attributes.attributes.field,
	       	entity: node.parentNode.text, 
	    	field: node.text,
		    alias: node.text,
	        longDescription: null
	    };
	            
		recordBaseConfig = recordBaseConfig || {};
		Ext.apply(field, recordBaseConfig);
			
		 this.targetPanel.addField(field, rowIndex);
	           
	    var seeds =  Sbi.qbe.CalculatedFieldWizard.getUsedItemSeeds('dmFields', node.attributes.attributes.formState.expression);
		for(var i = 0; i < seeds.length; i++) {
			
		  	var n = node.parentNode.findChildBy(function(childNode) {
		   		return childNode.id === seeds[i];
		   	});
		  	
		   	if(n) {
		   		this.addNodeToSelect(n, rowIndex, {visible:false});
		   	} else {
		   		alert('node  [' + seeds + '] not contained in entity [' + node.parentNode.text + ']');
		   	}
		}
	}
	
	, addInLineCalculatedNodeToSelect: function(node, rowIndex, recordBaseConfig) {
		var field = {
    			id: node.attributes.attributes.formState,
    			type: Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED,
    			entity: node.parentNode.text, 
    			field: node.text,
    			alias: node.text,
    			longDescription: null
	    };

		recordBaseConfig = recordBaseConfig || {};
		Ext.apply(field, recordBaseConfig);
		
        this.targetPanel.addField(field, rowIndex);
	}
	
	, addEntityNodeToSelect: function(node, rowIndex, recordBaseConfig) {
		for(var i = 0; i < node.attributes.children.length; i++) {
			var childNode = node.attributes.children[i];
			childNode.attributes.attributes = childNode.attributes;
			this.addNodeToSelect(childNode, rowIndex, recordBaseConfig);
		}
	}
	
	
	//=====================================================================================
	// from SELECT GRID (self)
	// ====================================================================================
    , notifyDropFromSelectGrid: function(ddSource, e, data, rowIndex) {
    	//alert('Source object: select-grid');
        var sm = this.targetGrid.getSelectionModel();
        var ds = this.targetGrid.getStore();
        var rows = sm.getSelections();
        
        rows = rows.sort(function(r1, r2) {
        	var row1 = ds.getById(r1.id);
            var row2 = ds.getById(r2.id);
            return ds.indexOf(r2) - ds.indexOf(r1);
         });
         if(rowIndex == undefined) {
            rows = rows.reverse();
         }
           
         for (i = 0; i < rows.length; i++) {
         	var rowData=ds.getById(rows[i].id);
            if(!this.copy) {
            	ds.remove(ds.getById(rows[i].id));
                if(rowIndex != undefined) {
                  ds.insert(rowIndex, rowData);
                } else {
                  ds.add(rowData);
                }
            }
         }
         
         this.targetGrid.getView().refresh();
      } // notifyDropFromSelectGrid
      
      , notifyDropFromFilterGrid: function(ddSource, e, data) {
      	//alert('Source object: filter-grid');
      } // notifyDropFromFilterGrid  	
});