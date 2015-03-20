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
  *  storeChanged: when the store is changed
  * 
  * Authors
  * 
  * - Alberto Ghedin (alberto.ghedin@eng.it), Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.QueryFieldsContainerPanel = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.worksheet.designer.tabledesignerpanel.fields')
	};
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.queryFieldsContainerPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.queryFieldsContainerPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c); // this operation should overwrite this.initialData content, that is initial grid's content
	
	this.addEvents('storeChanged', 'attributeDblClick', 'attributeRemoved');
	
	this.init(c);
	
	Ext.apply(c, {
        store: this.store
        , width: 250
        , height: 280
        , cls : 'table'
        , cm: this.cm
        , enableDragDrop: true
        , ddGroup: this.ddGroup || 'crosstabDesignerDDGroup'
	    , layout: 'fit'
	    , viewConfig: {
	    	forceFit: true
	    }
		, tools: [
	          {
	        	  id: 'close'
	        	, handler: this.removeAllValues
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
        			this.removeSelectedValues();
      	      	}      
      	    }
        	, mouseover: function(e, t) {
        		this.targetRow = t; // for Drag&Drop
        	}
        	, mouseout: function(e, t) {
        		this.targetRow = undefined;
        	}
        	, scope: this
		}
        , scope: this
        , type: 'queryFieldsContainerPanel'
	});	

	// constructor
	Sbi.worksheet.designer.QueryFieldsContainerPanel.superclass.constructor.call(this, c);
	
	this.on('rowdblclick', this.rowDblClickHandler, this);
};

Ext.extend(Sbi.worksheet.designer.QueryFieldsContainerPanel, Ext.grid.GridPanel, {
	
	initialData: undefined
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
				this.addField(this.initialData[i]);
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
                '<table style="margin-left: auto; margin-right: auto;" id="{4}" cellspacing="0" class="x-btn {3} {6}"><tbody class="{1}">',
                '<tr><td class="x-btn-tl"><i>&#160;</i></td><td class="x-btn-tc"></td><td class="x-btn-tr"><i>&#160;</i></td></tr>',
                '<tr><td class="x-btn-ml"><i>&#160;</i></td><td class="x-btn-mc"><button type="{0}" class=" x-btn-text {5}"></button>{7}</td><td class="x-btn-mr"><i>&#160;</i></td></tr>',
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
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon x-btn-invalid', Ext.id(), record.data.iconCls, record.data.iconCls+'_text', record.data.alias]		
		   	    		);		   	    		
				}
				else{
	   	    		toReturn = this.template.apply(
		        			['button', 'x-btn-small x-btn-icon-small-left', '', 'x-btn-text-icon', Ext.id(), record.data.iconCls, record.data.iconCls+'_text', record.data.alias]		
		   	    		);  	    							
				}
	   	    	return toReturn;
	   	    	
	   	    	
	    	}
	        , scope: this
	    });
	    this.cm = new Ext.grid.ColumnModel([fieldColumn]);
	}
	
	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.selections;
		var i = 0;
		for (; i < rows.length; i++) {
			var aRow = rows[i];
			// if the attribute is already present show a warning
			if (this.store.findExact('id', aRow.data.id) !== -1) {
				Ext.Msg.show({
					   title: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.title'),
					   msg: LN('sbi.crosstab.attributescontainerpanel.cannotdrophere.attributealreadypresent'),
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
			this.addField(aRow.data);
			this.fireEvent('storeChanged', this.store.getCount());
		}
	}
	
	
	, rowDblClickHandler: function(grid, rowIndex, event) {
		var record = grid.store.getAt(rowIndex);
		if (record.data.nature == 'attribute' || record.data.nature == 'segment_attribute') {
	     	this.fireEvent("attributeDblClick", this, record.data);
		}
	}
	
	, getContainedValues: function () {
		var attributes = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			attributes.push(record.data);
		}
		return attributes;
	}
	
	, setValues: function (attributes) {
		this.removeAllValues();
		var i = 0;
		for (; i < attributes.length; i++) {
  			var attribute = attributes[i];
  			this.addField(attribute); 
  		}
		this.fireEvent('storeChanged', this.store.getCount());
	}
	
	, addField : function (field) {
		var data = Ext.apply({}, field); // making a clone
		var record = new this.Record(data);
		this.store.add(record); 
	}
//    , getRecordFields : function () {
//    	var recordFields = [];
//    	var count = this.store.getCount();
//    	for (var i = 0; i < count; i++) {
//    		recordFields.push(this.store.getAt(i));
//    	}
//    	return recordFields;
//    }
	, removeSelectedValues: function() {
        var sm = this.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove(rows);
        this.fireEvent('storeChanged', this.store.getCount());
	}
	
	, removeAllValues: function() {
		this.store.removeAll(false); // CANNOT BE SILENT!!! it must throw the clear event for attributeRemoved event
		this.fireEvent('storeChanged',0);
	}

	, containsAttribute: function (attributeId) {
		if (this.store.findExact('id', attributeId) !== -1) {
			return true;
		}
		return false;
	}
	, validate: function (validFields) {
		
		this.validFields = validFields;

		var invalidFields = this.modifyStore(validFields);
		if(this.rendered){
			this.store.fireEvent("datachanged", this, null); 
		}
		return invalidFields;
			
	}
	, modifyStore: function (validFields) {
		var invalidFields = '';
		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			var isValid = this.validateRecord(record,validFields);
			record.data.valid = isValid;
			if(isValid == false){
				invalidFields+=''+record.data.alias+',';
			}
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