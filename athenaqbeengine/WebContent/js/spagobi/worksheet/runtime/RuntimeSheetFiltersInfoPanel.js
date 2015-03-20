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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors
 *  - Davide Zerbetto (davide.zerbetto@eng.it)
 */

Ext.ns("Sbi.worksheet.runtime");

Sbi.worksheet.runtime.RuntimeSheetFiltersInfoPanel = function(config) {
	
	var defaultSettings = {
		title : LN('sbi.worksheet.runtime.runtimesheetfiltersinfopanel.title')
		, valuesSeparator : ", "
		, style : 'padding: 15px'
		, frame : true
	};
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.runtime && Sbi.settings.worksheet.runtime.runtimeSheetFiltersInfoPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.runtime.runtimeSheetFiltersInfoPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.baseConfig = c;

	this.init();
	
	c = Ext.apply(c, {
		store : this.store
		, cm : this.cm
		, sm : this.sm
	    , width : 600
	    , height : 200
		, viewConfig: {
			forceFit : true
        	, emptyText : LN('sbi.worksheet.runtime.runtimesheetfiltersinfopanel.empty')
        	, deferEmptyText : false
		}
	}); 
	
	// constructor
    Sbi.worksheet.runtime.RuntimeSheetFiltersInfoPanel.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.worksheet.runtime.RuntimeSheetFiltersInfoPanel, Ext.grid.GridPanel, {
    
	store : null
	, filtersInfo : null // must be in the constructor input object
	, Record: Ext.data.Record.create([
	      {name: 'id', type: 'string'}
	      , {name: 'values', type: 'string'}
	])
	   
	// private methods
	   
	,
	init: function() {
		this.store = new Ext.data.SimpleStore({
			 fields : ['name', 'values']
		     , data : this.filtersInfo
		});
		this.cm = new Ext.grid.ColumnModel([
			 {
				 header: LN('sbi.worksheet.runtime.runtimesheetfiltersinfopanel.columns.attribute') 
				 , dataIndex: 'name'
				 , width: 50
				 , renderer: this.columnRenderer
			 },
			 {
				 header: LN('sbi.worksheet.runtime.runtimesheetfiltersinfopanel.columns.values') 
				 , dataIndex: 'values'
				 , renderer: this.columnRenderer
			 }
 	    ]);
		this.sm = new Ext.grid.RowSelectionModel({singleSelect:true})
	}

	,
	columnRenderer: function (value, metadata, record) {
	 	var tooltipString = value;
	 	if (tooltipString !== undefined && tooltipString != null) {
	 		metadata.attr = ' ext:qtip="'  + tooltipString + '"';
	 	}
	 	return value;
	}

	
	// public methods

	,
	getOriginalValue : function (fieldName) {
		for (var i = 0 ; i < this.filtersInfo.length ; i++) {
			var data = this.filtersInfo[i];
			if (data[0] == fieldName) {
				return data[1];
			}
		}
		return null;
	}
	
	,
	update : function (filtersInfo) {
		for (var c in filtersInfo) {
			var newValuesArray = filtersInfo[c];
			var newValuesJoined = null;
			if (Sbi.qbe.commons.Utils.isEmpty(newValuesArray)) {
				newValuesJoined = this.getOriginalValue(c);
			} else {
				newValuesJoined = newValuesArray.join(this.valuesSeparator);
			}
			var index = this.store.findExact('name', c);
			if (index != -1) {
				// existing record
				if (Sbi.qbe.commons.Utils.isEmpty(newValuesJoined)) {
					// record is present but must be removed
					this.store.removeAt(index);
				} else {
					// modify record
					var record = this.store.getAt(index);
					record.set('values', newValuesJoined);
				}
			} else {
				// non existing record
				if (Sbi.qbe.commons.Utils.isEmpty(newValuesJoined)) {
					continue;
				} else {
					// add new record
					var newRecord = new this.Record({
						name : c
						, values : newValuesJoined
					});
					this.store.add(newRecord);
				}
			}
			this.store.commitChanges();
		}
	}
	
});