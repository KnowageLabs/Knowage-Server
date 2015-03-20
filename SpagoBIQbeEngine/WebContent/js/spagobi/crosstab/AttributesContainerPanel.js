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

Ext.ns("Sbi.crosstab");

Sbi.crosstab.AttributesContainerPanel = function(config) {
	
	var defaultSettings = {
	};
	
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.attributesContainerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.attributesContainerPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	this.hasSegmentAttribute = false;
	
	Ext.apply(this, c); // this operation should overwrite this.initialData content, that is initial grid's content
	
	this.addEvents("beforeAddAttribute", "attributeDblClick", "attributeRemoved");
	
	this.init(c);
	
	Ext.apply(c, {
        store: this.store
        , cm: this.cm
        , sm: this.sm
        , enableDragDrop: true
        , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
	    , layout: 'fit'
	    , viewConfig: {
	    	forceFit: true
	    }
		, tools: [
	          {
	        	  id: 'close'
	        	, handler: this.removeAllAttributes
	          	, scope: this
	          	, qtip: LN('sbi.crosstab.attributescontainerpanel.tools.tt.removeall')
	          }
		]
        , listeners: {
			render: function(grid) { // hide the grid header
				grid.getView().el.select('.x-grid3-header').setStyle('display', 'none');
    		}
        	, keydown: function(e) { 
        		if (e.keyCode === 46) {
        			this.removeSelectedAttributes();
      	      	}      
      	    }
        	, mouseover: function(e, t) {
        		this.targetRow = t; // for Drag&Drop
        	}
        	, mouseout: function(e, t) {
        		this.targetRow = undefined;
        	}
        	, rowdblclick: this.rowDblClickHandler
		}
        , scope: this
        , type: 'attributesContainerPanel'
	});	
	
	// constructor
    Sbi.crosstab.AttributesContainerPanel.superclass.constructor.call(this, c);
   
    this.on('render', this.initDropTarget, this);
    
	//clean the selection of the grid the mouse quit from the grid
	//to avoid focusing problem when the user navigate between different
	//AttributesContainerPanels
    this.on('mouseout',function(){        		
		var sm = this.getSelectionModel();
		sm.clearSelections();
	},this);
};

Ext.extend(Sbi.crosstab.AttributesContainerPanel, Ext.grid.GridPanel, {
	
	initialData: undefined
	, store: null
	, targetRow: null
	, calculateTotalsCheckbox: null
	, calculateSubtotalsCheckbox: null
	, validFields: null
	, Record: Ext.data.Record.create([
	      {name: 'id', type: 'string'}
	      , {name: 'alias', type: 'string'}
	      , {name: 'funct', type: 'string'}
	      , {name: 'iconCls', type: 'string'}
	      , {name: 'nature', type: 'string'}
	      , {name: 'values', type: 'string'}
	])
	
	, init: function(c) {
		this.initStore(c);
		this.initColumnModel(c);
	}
	
	, initStore: function(c) {
		this.store =  new Ext.data.ArrayStore({
	        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'values', 'valid']
		});
		// if there are initialData, load them into the store
		if (this.initialData !== undefined) {
			for (i = 0; i < this.initialData.length; i++) {
				this.addAttribute(this.initialData[i]);
			}
		}
		this.store.on('remove', function (theStore, theRecord, index ) {
			this.fireEvent('attributeRemoved', this, theRecord.data);
		}, this);
		/*
		 * unfortunately, when removing all record with removeAll method, the event remove is not raised
		 */
		this.store.on('clear', function (theStore, theRecords ) {
			for (var i = 0 ; i < theRecords.length; i++) {
				var aRecord = theRecords[i];
				this.fireEvent('attributeRemoved', this, aRecord.data);
			}
		}, this);
	}
	
	, initColumnModel: function(c) {
        this.template = new Ext.Template( // see Ext.Button.buttonTemplate and Button's onRender method
        		// margin auto in order to have button center alignment
                '<table style="margin-left: auto; margin-right: auto;" id="{4}" cellspacing="0" class="x-btn {3}"><tbody class="{1}">',
                '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
                '<tr><td class="x-btn-ml"><i>&#160;</i></td><td class="x-btn-mc"><button type="{0}" class=" x-btn-text {5}"></button>{6}</td><td class="x-btn-mr"><i>&#160;</i></td></tr>',
                '<tr><td class="x-btn-bl"><i>&#160;</i></td><td class="x-btn-bc"></td><td class="x-btn-br"><i>&#160;</i></td></tr>',
                '</tbody></table>');
        
        this.template.compile();
		
	    var fieldColumn = new Ext.grid.Column({
	    	header:  ''
	    	, dataIndex: 'alias'
	    	, hideable: false
	    	, hidden: false	
	    	, sortable: false
	   	    , renderer : function(value, metaData, record, rowIndex, colIndex, store){

	   	    	
				if(record.data.valid != undefined && !record.data.valid){
	   	    		toReturn = this.template.apply(
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon x-btn-invalid', Ext.id(), record.data.iconCls, record.data.alias]		

		   	    		);		   	    		
				}
				else{
	   	    		toReturn = this.template.apply(
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon', Ext.id(), record.data.iconCls, record.data.alias]	
		   	    		);  	    							
				}
	   	    	return toReturn;
	   	    	
	   	    	}
	        , scope: this
	    });
	    this.cm = new Ext.grid.ColumnModel([fieldColumn]);
	}
	
	, initRowSelectionModel: function(){
		this.sm = new Ext.grid.RowSelectionModel({
            singleSelect: false
		});
	}

	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {
		
		if (ddSource.grid){
			var store = ddSource.grid.getStore();
			var index = store.find("nature","segment_attribute");
			if(index == -1 )this.hasSegmentAttribute = false;
			else this.hasSegmentAttribute = true;
		}
		
		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'queryFieldsPanel') {
			// dragging from QueryFieldsPanel
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'attributesContainerPanel') {
			// dragging from AttributesContainerPanel
			this.notifyDropFromAttributesContainerPanel(ddSource);
		} else if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'measuresContainerPanel') {
			Ext.Msg.show({
				   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
				   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.measures'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		}
		
	}
	
	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.selections;
		for (var i = 0; i < rows.length; i++) {
			var aRow = rows[i];
			// if the field is a measure show a warning
			if (aRow.data.nature === 'measure' || aRow.data.nature === 'mandatory_measure') {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.measures'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}

			if (this.fireEvent('beforeAddAttribute', this, aRow.data) == false) {
				return;
			}
			
			var data = Ext.apply({}, aRow.data);
			var newRecord = new this.Record(data);
			if (!newRecord.data.values) {
				newRecord.data.values = new Array(); // init the 'values' property as empty
			}
			this.store.add([newRecord]);
			
			
		}
	}
	
	, notifyDropFromAttributesContainerPanel: function(ddSource) {
		if (ddSource.grid.id === this.id) {
			// DD on the same AttributesContainerPanel --> re-order the fields
			var rows = ddSource.dragData.selections;
			if (rows.length > 1) {
				Ext.Msg.show({
					   title:'Drop not allowed',
					   msg: 'You can move only one field at a time',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
			} else {
				var row = rows[0];
				var rowIndex; // the row index on which the field has been dropped on
				if(this.targetRow) {
					rowIndex = this.getView().findRowIndex( this.targetRow );
				}
				if (rowIndex == undefined || rowIndex === false) {
					rowIndex = undefined;
				}
		           
	         	var rowData = this.store.getById(row.id);
	         	/*
	         	 * We suspend events since the remove method will raise the attributeRemoved event
	         	 */
	         	this.store.suspendEvents();
            	this.store.remove(this.store.getById(row.id));
            	this.store.resumeEvents();
                if (rowIndex != undefined) {
                	this.store.insert(rowIndex, rowData);
                } else {
                	this.store.add(rowData);
                }
		         
		        this.getView().refresh();
				
			}
		} else {
			// DD on another AttributesContainerPanel --> moving the fields from rows to columns or from columns to rows
			var rows = ddSource.dragData.selections;
			/*
			 * operation must be performed in this order: 
			 * 1. add new rows
			 * 2. remove rows from source
			 * because the attributeRemoved is fired when removing the attribute from the source, and we may loose filters on domain values
			 */
			this.store.add(rows);
			ddSource.grid.store.remove(rows);
		}
	}
	
	, getContainedAttributes: function () {
		var attributes = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			attributes.push(record.data);
		}
		return attributes;
	}
	
	, setAttributes: function (attributes) {
		this.removeAllAttributes();
		for (var i = 0; i < attributes.length; i++) {
  			var attribute = attributes[i];
  			this.addAttribute(attribute);
  		}
	}
	
	, removeSelectedAttributes: function() {
        var sm = this.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove(rows);
	}
	
	, removeAllAttributes: function() {
		this.store.removeAll(false); // CANNOT BE SILENT!!! it must throw the clear event for attributeRemoved event 
	}
	
	, rowDblClickHandler: function(grid, rowIndex, event) {
		var record = grid.store.getAt(rowIndex);
     	this.fireEvent("attributeDblClick", this, record.data);
	}
	
	, addAttribute : function (attribute) {
		var data = Ext.apply({}, attribute); // making a clone
		var row = new this.Record(data); 
		this.store.add([row]);
		return row;
	}
	, validate: function (validFields) {

		this.validFields = validFields;
		var invalidFields = this.modifyStore(validFields);
		this.store.fireEvent("datachanged", this, null); 
		return invalidFields;
	}
	, modifyStore: function (validFields) {
		var invalidFields='';

		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			var isValid = this.validateRecord(record,validFields);
			if(isValid == false){
				invalidFields+=''+record.data.alias+',';
			}
			record.data.valid = isValid;
		}
		
		return invalidFields;
	}
	, validateRecord: function (record, validFields) {
		var isValid = false;
		var i = 0;
		for(; i<validFields.length && isValid == false; i++){
			if(validFields[i].id == record.data.id){
			isValid = true;	
			}
		}
		return isValid;
	}

});