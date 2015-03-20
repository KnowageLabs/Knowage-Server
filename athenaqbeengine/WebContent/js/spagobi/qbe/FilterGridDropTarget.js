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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.FilterGridDropTarget = function(filterGridPanel, config) {
	
	var c = Ext.apply({
		ddGroup    : 'gridDDGroup',
		copy       : false
	}, config || {});
	
	this.targetPanel = filterGridPanel;
	this.targetGrid = this.targetPanel.grid; 
	this.targetElement = this.targetGrid.getView().el.dom.childNodes[0].childNodes[1];
	
	// constructor
    Sbi.qbe.FilterGridDropTarget.superclass.constructor.call(this, this.targetElement, c);
    
};

Ext.extend(Sbi.qbe.FilterGridDropTarget, Ext.dd.DropTarget, {
    
    services: null
    , targetPanel: null
    , targetGrid: null
    , targetElement: null
   
    , notifyDrop : function(ddSource, e, data){
    	
		// the row index and the column number on witch the tree node has been dropped on
		var rowIndex;
		var colIndex;

	    if(this.targetGrid.targetRow) {			          
	      rowIndex = this.targetGrid.targetRowIndex;
	      colIndex = this.targetGrid.targetColIndex;		          
	    }
	
	    if(rowIndex == undefined || rowIndex === false) {
	      // append the new row
	      rowIndex = undefined;
	    }   
	    
	    if(colIndex == undefined || colIndex === false) {
				colIndex = undefined;
		} 
	
	    
	    
	  	var sourceObject;
	  	if(ddSource.tree && ddSource.tree.type ===  'datamartstructuretree') {
	    	this.notifyDropFromDatamartStructureTree(ddSource, e, data, rowIndex, colIndex);
	  	} else if(ddSource.tree && ddSource.tree.type ===  'querycataloguetree') {
	    	this.notifyDropFromQueryCatalogueTree(ddSource, e, data, rowIndex, colIndex);		
		} else if(ddSource.grid &&  ddSource.grid.type === 'selectgrid') {
	    	this.notifyDropFromSelectGrid(ddSource, e, data, rowIndex, colIndex);
	  	} else if(ddSource.grid &&  ddSource.grid.type === 'filtergrid') {
	    	this.notifyDropFromFilterGrid(ddSource, e, data, rowIndex, colIndex);
      	} else if(ddSource.grid &&  ddSource.grid.type === 'documentparametersgrid') {
      		this.notifyDropFromDocumentParametersGrid(ddSource, e, data, rowIndex, colIndex);
      	} else if(ddSource.grid &&  ddSource.grid.type === 'parametersgrid') {
      		this.notifyDropFromParametersGrid(ddSource, e, data, rowIndex, colIndex);
	  	} else {
	    	alert('Source object: unknown');
	  	}        
	}
	
	//=====================================================================================
	// from CATALOGUE
	// ====================================================================================
	, notifyDropFromQueryCatalogueTree: function(ddSource, e, data, rowIndex, colIndex) {

		var filter;
		var node;
		var dropColDataIndex;
		
		node = ddSource.dragData.node; 
		if(colIndex) {
			dropColDataIndex = this.targetGrid.getColumnModel().getDataIndex( colIndex );
		}
	
		if(dropColDataIndex === 'rightOperandDescription') {
			filter = {
				rightOperandValue: node.id
				, rightOperandDescription: node.props.query.name
				, rightOperandLongDescription: 'Subquery ' + node.props.query.name
				, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_SUBQUERY
			};
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else if(dropColDataIndex === 'leftOperandDescription') {
			filter = {
				leftOperandValue: node.id
				, leftOperandDescription: node.props.query.name
				, leftOperandLongDescription: 'Subquery ' + node.props.query.name
				, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SUBQUERY
			};
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else {
			Ext.Msg.show({
				   title:'Drop target not allowed',
				   msg: 'Subqueries can be dropped only on the "operand" columns of an existing filter',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
	}
	
	//=====================================================================================
	// from DOCUMENT PARAMETERS GRID
	// ====================================================================================
	, notifyDropFromDocumentParametersGrid: function(ddSource, e, data, rowIndex, colIndex) {

		var filter;
		var node;
		var dropColDataIndex;
		
		node = ddSource.dragData.node; 
		if (colIndex) {
			dropColDataIndex = this.targetGrid.getColumnModel().getDataIndex( colIndex );
		}
	
		var rows = ddSource.dragData.selections;  
		if(rows.length > 1 ) {
			Ext.Msg.show({
				   title:'Wrong dragged source',
				   msg: 'Cannot drop a collection of parameters',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		if (dropColDataIndex === 'rightOperandDescription') {
			filter = {
				rightOperandValue: (rows[0].data.type == 'NUM') ? 'P{' + rows[0].data.id + '}' : '\'P{' + rows[0].data.id + '}\''
				, rightOperandDescription: '[' + rows[0].data.label + ']'
				, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
				, rightOperandLongDescription: LN('sbi.qbe.documentparametersgridpanel.parameterreference') + ' [' + rows[0].data.label + ']'
			};
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else if (dropColDataIndex === 'leftOperandDescription') {
			filter = {
				leftOperandValue: (rows[0].data.type == 'NUM') ? 'P{' + rows[0].data.id + '}' : '\'P{' + rows[0].data.id + '}\''
				, leftOperandDescription: '[' + rows[0].data.label + ']'
				, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
				, leftOperandLongDescription: LN('sbi.qbe.documentparametersgridpanel.parameterreference') + ' [' + rows[0].data.label + ']'
			};
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else {
			Ext.Msg.show({
				   title:'Drop target not allowed',
				   msg: 'Parameters can be dropped only on the "operand" columns of an existing filter',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
	}
	
	//=====================================================================================
	// from PARAMETERS GRID
	// ====================================================================================
	, notifyDropFromParametersGrid: function(ddSource, e, data, rowIndex, colIndex) {

		var filter;
		var node;
		var dropColDataIndex;
		
		node = ddSource.dragData.node; 
		if (colIndex) {
			dropColDataIndex = this.targetGrid.getColumnModel().getDataIndex( colIndex );
		}
	
		var rows = ddSource.dragData.selections;  
		if(rows.length > 1 ) {
			Ext.Msg.show({
				   title:'Wrong dragged source',
				   msg: 'Cannot drop a collection of parameters',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		if (dropColDataIndex === 'rightOperandDescription') {
			filter = {
				rightOperandValue: (rows[0].data.type == 'NUM') ? 'P{' + rows[0].data.name + '}' : '\'P{' + rows[0].data.name + '}\''
				, rightOperandDescription: '[' + rows[0].data.name + ']'
				, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
				, rightOperandLongDescription: LN('sbi.qbe.parametersgridpanel.parameterreference') + ' [' + rows[0].data.name + ']'
				, promptable: true
			};
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else {
			Ext.Msg.show({
				   title:'Drop target not allowed',
				   msg: 'Parameters can be dropped only on the right "operand" column of an existing filter',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
	}
	
	// =====================================================================================
	// from TREE
	// =====================================================================================
	
	, notifyDropFromDatamartStructureTree: function(ddSource, e, data, rowIndex, colIndex) {
		var node;				// the node dragged from tree to grid
		var nodeType;
		var dropColDataIndex;	// the dataIndex of the column on which the node has been dropped. 
								// It is undefined if the node is not drop on an existing row (rowIndex == colIndex == undefined)
		var filter;				// configuration object to be passed to addFilter or insertFilter function of the grid (it not a record object)
		
		node = ddSource.dragData.node;     
		nodeType = node.attributes.type || node.attributes.attributes.type;
		
		if(colIndex) {
			dropColDataIndex = this.targetGrid.getColumnModel().getDataIndex( colIndex );
		}
	
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {		
			
			if(dropColDataIndex === 'rightOperandDescription') {			
				filter = {
					rightOperandValue: node.id
					, rightOperandDescription: node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field 
					, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
					, rightOperandAlias: node.attributes.attributes.field 
					, rightOperandLongDescription: node.attributes.attributes.longDescription
				};
				this.targetPanel.modifyFilter(filter, rowIndex);
			}else if(dropColDataIndex === 'leftOperandDescription') {			
				filter = {
					leftOperandValue: node.id
					, leftOperandDescription: node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
					, leftOperandAlias: node.attributes.attributes.field 
					, leftOperandLongDescription: node.attributes.attributes.longDescription
				};
				this.targetPanel.modifyFilter(filter, rowIndex);
			} else {
				filter = {
					leftOperandValue: node.id
					, leftOperandDescription: node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
					, leftOperandLongDescription: node.attributes.attributes.longDescription
					, leftOperandAlias: node.attributes.attributes.field 
				};
	  			this.targetPanel.insertFilter(filter, rowIndex);
			}
		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY){
			
			for(var i = 0; i < node.attributes.children.length; i++) {
				var filterType;
				var nodeType = node.attributes.children[i].attributes.type;
				if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
					filterType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD;
				} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
					filterType = Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD;
				} else {
					continue;
				}
				
				filter = {
					leftOperandValue: node.attributes.children[i].id
					, leftOperandDescription: node.attributes.children[i].attributes.entity + ' : ' + node.attributes.children[i].attributes.field 
					, leftOperandType: filterType
					, leftOperandLongDescription: node.attributes.children[i].attributes.longDescription
				};
				
				this.targetPanel.insertFilter(filter, rowIndex);
			}
		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD){
				filter = {
					leftOperandValue: node.attributes.attributes.formState
					, leftOperandDescription: node.attributes.entity + ' : ' + node.attributes.attributes.formState.alias 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
					, leftOperandLongDescription: node.attributes.attributes.formState.alias 
				};
				
				this.targetPanel.insertFilter(filter, rowIndex);

		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_RELATION){
			//no action requeired for relation
		} else {
			Ext.Msg.show({
				   title:'Drop target not allowed',
				   msg: 'Node of type [' + nodeType + '] cannot be dropped here',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	
		this.targetGrid.getView().refresh();
	}

	//=====================================================================================
	// from SELECT GRID
	// ====================================================================================
	, notifyDropFromSelectGrid: function(ddSource, e, data, rowIndex, colIndex) {
	
		var sm = this.targetGrid.getSelectionModel();
		var store = this.targetGrid.getStore();
		//var ddDs = this.targetGrid.getStore();;
		var rows = sm.getSelections();
		
		var dropColDataIndex;		
		var filter;				
		if(colIndex) {
			dropColDataIndex = this.targetGrid.getColumnModel().getDataIndex( colIndex );
		}
		
		var rows = ddSource.dragData.selections;  
		if(dropColDataIndex === 'leftOperandDescription' || dropColDataIndex === 'rightOperandDescription') {	
			if(rows.length > 1 ) {
				Ext.Msg.show({
					   title:'Wrong dragged source',
					   msg: 'Impossible to use as value into a filter a collection of fields',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}
		
			var operandType;
			if(rows[0].data.type == Sbi.constants.qbe.FIELD_TYPE_SIMPLE) {
				operandType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD;
			} else if(rows[0].data.type == Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED) {
				operandType = Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD;
			} else {
				Ext.Msg.show({
					   title:'Drop target not allowed',
					   msg: 'Select fields of type [' + rows[0].data.type + '] cannot be dropped into filter grid',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
			}
			
			if(dropColDataIndex === 'leftOperandDescription') {
				filter = {
					leftOperandValue: rows[0].data.id
					, leftOperandDescription: rows[0].data.entity + ' : ' + rows[0].data.field 
					, leftOperandType: operandType
					, leftOperandLongDescription: rows[0].data.longDescription
				};
			} else {
				filter = {
					rightOperandValue: rows[0].data.id
					, rightOperandDescription: rows[0].data.entity + ' : ' + rows[0].data.field 
					, rightOperandType: operandType
					, rightOperandLongDescription: rows[0].data.longDescription
				};
			}
			
			this.targetPanel.modifyFilter(filter, rowIndex);
		} else {
			rows = rows.sort(
				function(r1, r2) {
					var row1 = store.getById(r1.id);
					var row2 = store.getById(r2.id);
					return store.indexOf(r2) - store.indexOf(r1);
				}
			);
			
			if(rowIndex == undefined) {
	  			rows = rows.reverse();
			}
	   
			for (i = 0; i < rows.length; i++) {
				var operandType;
				if(rows[i].data.type == Sbi.constants.qbe.FIELD_TYPE_SIMPLE) {
					operandType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD;
				} else if(rows[i].data.type == Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED) {
					operandType = Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD;
				} else {
					Ext.Msg.show({
						   title:'Drop target not allowed',
						   msg: 'Select fields of type [' + rows[i].data.type + '] cannot be dropped into filter grid',
						   buttons: Ext.Msg.OK,
						   icon: Ext.MessageBox.WARNING
					});
				}
	  			if(!this.copy) {
	  				filter = {
	  					leftOperandValue: rows[i].data.id
						, leftOperandDescription: rows[i].data.entity + ' : ' + rows[i].data.field 
						, leftOperandType: operandType
						, leftOperandLongDescription: rows[i].data.longDescription
					};
	  				this.targetPanel.insertFilter( filter, rowIndex );
	  			}
			}     
	
			//this.targetGrid.getView().refresh();
		}
	}
	
	//=====================================================================================
	// from FILTER GRID (self)
	// ====================================================================================
	, notifyDropFromFilterGrid: function(ddSource, e, data,rowIndex, colIndex) {
		
		var sm= this.targetGrid.getSelectionModel();
		var ds = this.targetGrid.getStore();
		var rows = sm.getSelections();

		rows = rows.sort(
				function(r1, r2) {
					var row1 = ds.getById(r1.id);
					var row2 = ds.getById(r2.id);
					return ds.indexOf(r2) - ds.indexOf(r1);
				}
		);
		
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
	}
});