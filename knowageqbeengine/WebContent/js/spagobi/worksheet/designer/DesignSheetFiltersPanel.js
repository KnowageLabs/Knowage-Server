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
 * Authors - Davide Zerbetto (davide.zerbetto@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.DesignSheetFiltersPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.designer.designsheetfilterspanel.title')
		, frame: true
		, emptyMsg: LN('sbi.worksheet.designer.designsheetfilterspanel.emptymsg')
	};
		
	if(Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.designSheetFiltersPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.designSheetFiltersPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
	
	this.addEvents('attributeDblClick', 'attributeRemoved');
	
	this.init();
	
	c = Ext.apply(c, {
		title: this.title
        , layout: {
            type:'column'
        }
		, items: [this.emptyMsgPanel]
	});

	// constructor	
	Sbi.worksheet.designer.DesignSheetFiltersPanel.superclass.constructor.call(this, c);
	
	this.on('render', this.initDropTarget, this);

};

Ext.extend(Sbi.worksheet.designer.DesignSheetFiltersPanel, Ext.Panel, {
	
	store: null
	, emptyMsgPanel: null
	, splittingFilter: null //id of the splitting filter
    , filters: null
	, empty: null
	, contents: null
	, Record: Ext.data.Record.create([
	      {name: 'id', type: 'string'}
	      , {name: 'alias', type: 'string'}
	      , {name: 'funct', type: 'string'}
	      , {name: 'iconCls', type: 'string'}
	      , {name: 'nature', type: 'string'}
	      , {name: 'selection', type: 'string'}
	      , {name: 'mandatory', type: 'string'}
	])
	, editItemWindow: null
	
	, init: function() {
		this.initStore();
		this.initEmptyMsgPanel();
		this.contents = [this.emptyMsgPanel];
		this.empty = true;
	}
	
	, initStore: function() {
		//the store has been injected from the parent
		if(this.store==null){
			this.store =  new Ext.data.ArrayStore({
		        fields: ['id', 'alias', 'funct', 'iconCls', 'nature', 'selection', 'mandatory','valid']
			});
			// if there are initialData, load them into the store
			if (this.initialData !== undefined) {
				for (i = 0; i < this.initialData.length; i++) {
					this.addFilterIntoStore(this.initialData[i]);
				}
			}
		}else{
			if(this.store.getCount()>0){
				this.empty=false;
			}
		}
		this.store.on('remove', function (theStore, theRecord, index ) {
			this.fireEvent('attributeRemoved', this, theRecord.data);
		}, this);
	}
	
	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		var dropTarget = new Sbi.widgets.GenericDropTarget(this, {
			ddGroup: this.ddGroup
			, onFieldDrop: this.onFieldDrop
		});
	}

	, onFieldDrop: function(ddSource) {

		if (ddSource.grid && ddSource.grid.type && ddSource.grid.type === 'queryFieldsPanel') {
			this.notifyDropFromQueryFieldsPanel(ddSource);
		} else {
			Ext.Msg.show({
			   title: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.title'),
			   msg: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.unknownsource'),
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.WARNING
			});
		}
	}
	
	, notifyDropFromQueryFieldsPanel: function(ddSource) {
		var rows = ddSource.dragData.selections;
		var i = 0;
		for (; i < rows.length; i++) {
			var aRow = rows[i];
			// if the attribute is already present show a warning
			if (this.store.findExact('id', aRow.data.id) !== -1) {
				Ext.Msg.show({
					   title: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.title'),
					   msg: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.attributealreadypresent'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a measure show a warning
			if (aRow.data.nature === 'measure'  || aRow.data.nature === 'mandatory_measure') {
				Ext.Msg.show({
					   title: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.title'),
					   msg: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.measures'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.WARNING
				});
				return;
			}
			// if the field is a postLineCalculated show an error
			if (aRow.data.nature === 'postLineCalculated') {
				Ext.Msg.show({
					   title: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.title'),
					   msg: LN('sbi.worksheet.designer.designsheetfilterspanel.cannotdrophere.postlinecalculated'),
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
				return;
			}
			
			this.addFilter(aRow);

		}
	}
	
	, getFilters: function () {
		var filters = [];
		for(var i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			filters.push(record.data);
		}
		return filters;
	}
	
	, setFilters: function (filters) {
		this.reset();
		for(var i = 0; i < filters.length; i++) {
			var aFilter = filters[i];
			var aRecord = new this.Record(aFilter);
			this.addFilter(aRecord);
		}
	}
	
	, removeSelectedFilters: function() {
        var sm = this.getSelectionModel();
        var rows = sm.getSelections();
        this.store.remove(rows);
	}
	
	, removeAllFilters: function() {
		this.store.removeAll(false);
	}
	
	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
		});
	}

	, addFilter: function(aRow) {
		if (this.empty === true) {
			this.reset();
			this.empty = false;	
		}

		if(aRow.data.splittingFilter=='on'){
			//the filter is a splitting filter
			this.splittingFilter=aRow;
		}
		
		var newRow = this.addFilterIntoStore(aRow.data);
		var item = this.createFilterPanel(newRow);

		this.contents.push(item);
		
		this.add(item);
		this.doLayout();
	}
	
	, addFilterIntoStore : function (filter) {
		var data = Ext.apply({
			selection   : "multivalue"
			, mandatory : "no"
		}, filter); // making a clone
		var row = new this.Record(data); 
		this.store.add([row]);
		return row;
	}
	
	, createFilterPanel: function(aRow) {
		
		var deleteButton = new Ext.Button({
   		    template: new Ext.Template(
     		         '<div><div class="smallBtn" style="float: left">',
     		             '<div class="delete-icon"></div>',
     		             '<div class="btnText"></div>',
     		         '</div>'
     		             
     		    )
				 , hidden: true
     		     , buttonSelector: '.delete-icon'
     		  	 , iconCls: 'delete-icon'
     		     , text: '&nbsp;&nbsp;&nbsp;&nbsp;'
     		     , handler: this.closeHandler.createDelegate(this, [aRow], true)
     		     , scope: this
     		});
		
		var editButton = new Ext.Button({
   		    template: new Ext.Template(
      		         '<div><div class="smallBtn" style="float: left">',
      		             '<div class="edit-icon"></div>',
      		             '<div class="btnText"></div>',
      		         '</div>'
      		             
      		    )
				 , hidden: true
      		     , buttonSelector: '.edit-icon'
      		  	 , iconCls: 'edit-icon'
      		     , text: '&nbsp;&nbsp;&nbsp;&nbsp;'
      		     , handler: this.openEditItemWindow.createDelegate(this, [aRow], true)
      		     , scope: this	
      		});
		
		var deleteButtonPlaceHolder = new Ext.Panel({width: 12,height: 12,html: ' '}) ;
		var editButtonPlaceHolder = new Ext.Panel({width: 12,height: 12,html: ' '}) ;
		
		var thePanel = null;
		
		var invalidation = '';
		var isValid = this.checkRowValidation(aRow);
		if(isValid == false){
			invalidation = ' color:#ff0000; text-decoration:line-through;';			
		}
			
			thePanel = new Ext.Panel({
				html: '<div style="padding-right: 2px; cursor: pointer; '+invalidation+'">' + aRow.data.alias + '</div>'
			});
		
		thePanel.doLayout();
		
		
		//aRow.data.thePanel= thePanel;
		
		thePanel.on('render', function(panel) {
			panel.getEl().on('dblclick', function() {
		     	this.fireEvent("attributeDblClick", this, aRow.data);
			}, this);
		}, this);
		
		
		var item = new Ext.Panel({
			id: 'designsheetfilterspanel_' + aRow.data.alias
            , layout: {
                type:'column'
            }
			, style:'padding:0px 5px 5px 5px; float: left;' + (Ext.isIE) ? '' : 'width: auto;'
       		, items: [
       		     thePanel
       		     , deleteButtonPlaceHolder
       		     , editButtonPlaceHolder
       		     , deleteButton
       		     , editButton	
       		]
		});
		
		item.on('render', function(panel) {
		}, this);
		
		
		//Show/hide the tool buttons when the mouse enter/leave
		item.on('afterrender', function(){
			item.el.on('mouseenter', function(event) {
				deleteButtonPlaceHolder.hide();
				editButtonPlaceHolder.hide();
				deleteButton.show();
				editButton.show();
			}, this);
			item.el.on('mouseleave', function(event) {
				deleteButtonPlaceHolder.show();
				editButtonPlaceHolder.show();
				deleteButton.hide();
				editButton.hide();
			}, this);
		},this);
		return item;
	}
	
	, closeHandler: function (button, event, aRow) {
		this.removeFilter(aRow);
	}
	
	, removeFilter: function(aRow) {
		var rowId = aRow.data.id;
		var recordIndex = this.store.findExact('id', rowId);
		this.store.removeAt(recordIndex);
		var item = null;
		var i = this.contents.length-1;
		for (; i >= 0; i--) {
			var temp = this.contents[i];
			if (temp.getId() === 'designsheetfilterspanel_' + aRow.data.alias) {
				item = temp;
				break;
			}
		}
		this.contents.remove(item);
		item.destroy();
		if (this.contents.length === 0) {
			this.initEmptyMsgPanel();
			this.contents.push(this.emptyMsgPanel);
			this.add(this.emptyMsgPanel);
			this.empty = true;
		}
		
		if(this.splittingFilter!=undefined && this.splittingFilter!=null && aRow.data.id==this.splittingFilter.data.id){
			//the filter is a splitting filter
			this.splittingFilter=null;
		}
		
		this.doLayout();
	}
	, removeFilter2: function(aRow) {
		var rowId = aRow.data.id;
		var recordIndex = this.store.findExact('id', rowId);
		this.store.removeAt(recordIndex);
		var item = null;
		var i = this.contents.length-1;
		for (; i >= 0; i--) {
			var temp = this.contents[i];
			if (temp.id === 'designsheetfilterspanel_' + aRow.data.alias) {
				item = temp;
				break;
			}
		}
		this.contents.remove(item);
		item.destroy();
		if (this.contents.length === 0) {
			this.initEmptyMsgPanel();
			this.contents.push(this.emptyMsgPanel);
			this.add(this.emptyMsgPanel);
			this.empty = true;
		}
		
		if(this.splittingFilter!=undefined && this.splittingFilter!=null && aRow.data.id==this.splittingFilter.data.id){
			//the filter is a splitting filter
			this.splittingFilter=null;
		}
		
		this.doLayout();
	}
	, removeAllButtons: function(a) {
		for(var j = 0; j<this.contents.length; j++){
			var item = this.contents[j];
			this.contents.remove(item);
			item.destroy();
		}
		this.doLayout();
	}
	, removeOneButton: function(aRow) {
		var item = null;
		var i = this.contents.length-1;
		for (; i >= 0; i--) {
			var temp = this.contents[i];
			if(temp.id  === 'designsheetfilterspanel_' + aRow.data.alias){
				item= temp
				break;
			}
		}
		if(item != null){
		this.contents.remove(item);
		item.destroy();		
		this.doLayout();
}
	}

	, reset: function() {
		if (this.contents && this.contents.length) {
			var i = this.contents.length - 1;
			for (; i >= 0; i--) {
				this.contents[i].destroy();
			}
		}
		this.contents = new Array();
		this.empty = true;
		this.splittingFilter=null;
	}
	
	, updateFilters: function(){
		this.reset();
		if(this.store.getCount()==0){
			this.initEmptyMsgPanel();
			this.contents.push(this.emptyMsgPanel);
			this.add(this.emptyMsgPanel);
		}
		for(var i=0; i<this.store.getCount(); i++){
			var aRow = (this.store.getAt(i));
			var item = this.createFilterPanel(aRow);
			this.contents.push(item);
			this.add(item);
			this.empty = false;
		}
		this.doLayout();
	}
	, reloadFilters: function(){
//		this.removeAllButtons();
		for(var i=0; i<this.store.getCount(); i++){
			var aRow = (this.store.getAt(i));
			this.removeOneButton(aRow);
			//this.removeFilter2(aRow);			
			//this.closeHandler(null, null, aRow);
			var item = this.createFilterPanel(aRow);
			this.contents.push(item);
			this.add(item);
			this.empty = false;
		}
		this.doLayout();
	}
	
	//Edit filter: Wizard
	, openEditItemWindow: function(button, event, aRow){
		if(this.editItemWindow==null){
			this.editItemWindow = new Sbi.worksheet.designer.DesignSheetFiltersEditWizard();
			this.editItemWindow.on('apply', this.updateRecordProperties, this);			
			this.editItemWindow.on('afterrender', function(){this.editItemWindow.setFormState(aRow.data)}, this);
		};
		this.editItemWindow.setRowState(aRow);
		this.editItemWindow.setSplitFilter(this.splittingFilter);
		this.editItemWindow.show();
	}

	//Update the filter after edit
	, updateRecordProperties: function(theWizard){
		this.store.commitChanges();
		this.splittingFilter = this.editItemWindow.getSplitFilter();
	}
	
	, containsAttribute: function (attributeId) {
		var toReturn = this.store.findExact('id', attributeId) !== -1;
		return toReturn;
	}
	, validate: function (validFields) {
		var invalidFields;
		this.validFields = validFields;
		invalidFields = this.modifyStore(validFields);
		//this.reloadFilters();
		return invalidFields;
			
	}
	, modifyStore: function (validFields) {
		var toReturn='';
		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			var isValid = this.validateRecord(record,validFields);
			record.data.valid = isValid;
			if(isValid == false) toReturn+=''+record.data.alias+',';
		}
		return toReturn;
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
	, checkRowValidation: function (row){
		var valid= true;
		var num = this.store.getCount();
		for(var i = 0; i < num; i++) {
			var record = this.store.getAt(i);
			if(row.data.id == record.data.id){
				valid = record.data.valid;
				break;
			}
		}
		return valid;
	}
	
});