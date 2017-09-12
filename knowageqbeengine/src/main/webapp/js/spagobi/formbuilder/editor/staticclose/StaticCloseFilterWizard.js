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
  * - by Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.StaticCloseFilterWizard = function(config) {
	
	var defaultSettings = {
		// set default values here
		title: LN('sbi.formbuilder.staticclosefilterwizard.title')
		, autoScroll: true
		, width: 550
		, height: 340
		, baseState: {
			filterTitle: undefined		
		}
		, modal: true
		, resizable: false
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticClosedXORFiltersWizard) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticClosedXORFiltersWizard);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.services = this.services || new Array();	
	this.services['getQueryFields'] = this.services['getQueryFields'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_QUERY_FIELDS_ACTION'
		, baseParams: new Object()
	});
	
	this.init();
	
	c = Ext.apply(c, {
		layout: 'fit',
		width: this.width,
		height: this.height,
		closeAction:'close', // closeAction must be 'close' and not 'hide'!!!
		// Using Ext 3.2.1, if closeAction is 'hide', a side effect appear: open the form designer, open a StaticCloseFilterWizard window,
		// then come back to the query designer: the FilterGridPanel of the static close filter is displayed between base query select clause
		// grid and where clause grid!!!
		plain: true,
		title: this.title,
		items: [this.formPanel]
	});
	
	// constructor
    Sbi.formbuilder.StaticCloseFilterWizard.superclass.constructor.call(this, c);
    
    this.handlePendingFn = function() {
    	if(this.pendingState) {
    		this.setFormState(this.pendingState);
    		delete this.pendingState;
    		this.un('show', this.handlePendingFn , this);
    		delete this.handlePendingFn;
    	}
    };
    
    this.on('show', this.handlePendingFn , this);
    
    
    if(this.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	
    this.addEvents('apply');
    
};

Ext.extend(Sbi.formbuilder.StaticCloseFilterWizard, Ext.Window, {

	formPanel: null
	, filterTitleField: null
	, leftOperandField: null
	, operatorField: null
	, rightOperandField: null
	, filterGrid: null
	
	, baseState: null
	, targetFilter: null
	, hasBuddy: null
    , buddy: null
	
	
	// --------------------------------------------------------------------------
	// Public
	// --------------------------------------------------------------------------
	
	, getFormState: function() {
		var s = {};
		
		s.text = this.filterTitleField.getValue();
		var filterGridState = this.getFilterGridState();
		s.filters = filterGridState;
		
		// adding an id to each filter
		for (var count = 0; count < filterGridState.length; count++) {
			filterGridState[count].id = Sbi.qbe.commons.Utils.randomString();
		}
		
		// building the filters expression
		var str = ''; // str is the filters expression
		for (var count = 0; count < filterGridState.length; count++) {
			if (count < filterGridState.length - 1) {
				str += '$F{' + filterGridState[count].id + '}' + ' ' + filterGridState[count].booleanConnector + ' ';
			} else {
				str += '$F{' + filterGridState[count].id + '}';
			}
		}
		
	    var error_offsets = new Array(); 
	    var error_lookaheads = new Array(); 
	    var error_count = 0;
	    error_count = boolstaf.module.parse( str, error_offsets, error_lookaheads );  
	    
	    if(error_count > 0) {
	      var errstr = new String(); 
	      for( var i = 0; i < error_count; i++ ) {
	        errstr += "Parse error in line " + ( str.substr( 0, error_offsets[i] ).match( /\n/g ) ? str.substr( 0, error_offsets[i] ).match( /\n/g ).length : 1 ) + " near \"" + str.substr( error_offsets[i] ) + "\", expecting \"" + error_lookaheads[i].join() + "\"\n" ; 
	        alert( errstr);
	        throw "Error while parsing expression";
	      }
	    }
	    
	    var expressionNode = boolstaf.module.getExperssionNode();
	    s.expression = this.getExpressionAsObject(expressionNode);
		
		return s;
	}

	, setFormState: function(s) {
		var d ;
		
		if(this.rendered === false) {
			this.pendingState = s;
			return;
		}
		
		if(s.text) {
			this.filterTitleField.setValue(s.text);
		}
		
		this.filterGrid.deleteFilters();
		if(s.filters) {
			for(var i = 0, l = s.filters.length; i < l; i++) {
				d = this.marshalFilterRecord(s.filters[i]);
				this.filterGrid.addFilter(d);
			}
		}
	}
	
	, setFilterDetailState: function(s) {
		this.leftOperandField.setValue(s.leftOperandValue);
		this.leftOperandField.setDescription(s.leftOperandDescription);
		this.operatorField.setValue(s.operator);
		this.rightOperandField.setValue(s.rightOperandValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator));
	}
	
	/*
	 * return an array containing 2*n - 1 elements where n is the number of row in the grid.
	 * the array have this form [filter1Conf{Object}, filter1BoolConnector{String}, ..., filterNConf{Object}] 
	 */
	, getFilterGridState: function() {
		var s = [];
		var store, r, c;
		
		store = this.filterGrid.store;
		for(var i = 0; i < store.getCount(); i++) {
			r = store.getAt(i);
			c = this.unmarshalFilterRecord(r)
			s.push(c);
		}
		
		return s;
	}
	
	, setFilterGridState: function(s) {
		
	}
	
	, resetFormState: function() {
		this.filterTitleField.reset();
		this.leftOperandField.reset();
		this.operatorField.reset();
		this.rightOperandField.reset();		
		this.resetFilterGrid();
	}
	
	, resetFilterGrid: function() {
		if(this.filterGrid.grid.rendered === false) return;
		this.filterGrid.deleteFilters();
		this.filterGrid.addFilter();
		//this.filterGrid.sm.selectRow(this.filterGrid.store.getCount()-1);
	}
	
	, setTarget: function(targetFilter) {
		this.targetFilter = targetFilter;
		
		if(this.targetFilter === null) {
			this.resetFormState();
		} else {
			this.setFormState(this.targetFilter.getContents());
		}
	}
	
	, getTarget: function() {
		return this.targetFilter;
	}
	
	// --------------------------------------------------------------------------
	// Private
	// --------------------------------------------------------------------------
	
	, init: function() {
		var items = [];
		
		this.filterTitleField = new Ext.form.TextField({
			fieldLabel: LN('sbi.formbuilder.staticclosefilterwizard.fields.filtertitle.label'),
			name:'filterTitleField',
    		value: this.baseState.filterTitle,
    		allowBlank: false, 
    		inputType:'text',
    		maxLength:50,
    		width:250
    	});
    	items.push(this.filterTitleField);
    	
    	var s = new Ext.data.JsonStore({
    		root: 'results'
    		, fields: ['id', 'alias']
    		, url: this.services['getQueryFields']
    	});
    	
    	
    	this.leftOperandField = new Sbi.widgets.LookupField({
    		fieldLabel: LN('sbi.formbuilder.staticclosefilterwizard.fields.leftoperand.label'),
			name: 'leftOperand',
    		store: s,
    		valueField: 'id',
    		displayField: 'alias',
    		descriptionField: 'alias',
    		enableFiltering: false,
    		cm: new Ext.grid.ColumnModel([
	    		new Ext.grid.RowNumberer(),
	    		{
	    			header: LN('sbi.formbuilder.staticclosefilterwizard.fields.leftoperand.label'),
	    		    dataIndex: 'alias',
	    		    width: 75
	    		}
    		]),
    		width:250
    	});
    	this.leftOperandField.on('change', this.onFormStateChange, this);
    	items.push(this.leftOperandField);
    	
    	this.operatorField = new Sbi.qbe.FilterComboBox({
    		fieldLabel: LN('sbi.formbuilder.staticclosefilterwizard.fields.operator.label'),
    		name: 'operator',
    		width:250
    	});
    	this.operatorField.on('change', this.onFormStateChange, this);
    	items.push(this.operatorField);
    	
    	this.rightOperandField = new Ext.form.TextField({
    		fieldLabel: LN('sbi.formbuilder.staticclosefilterwizard.fields.rightoperand.label'),
			name:'rightOperand',
			width:250
    	});
    	this.rightOperandField.on('change', this.onFormStateChange, this);
    	this.rightOperandField.on('keydown', this.onFormStateChangeX, this);    	
    	items.push(this.rightOperandField);
    	
    	
    	var sm = new Ext.grid.RowSelectionModel({singleSelect:true});
    	sm.on('rowselect', this.onRowSelect, this);
    	
    	this.filterGrid = new Sbi.qbe.FilterGridPanel({
    		//title: 'Expand this panel to add other filters...'
    		//, collapsible: true
    		//, titleCollapse: true
    		//, collapsed: true
    		gridHeight: 140
    		, width: 450
    		, gridStyle: 'padding:0px'
    		, sm: sm
    		, enableTbExpWizardBtn: false
    		, columns : {
				'filterId': {hideable: true, hidden: true, sortable: false, editable: false}
				, 'filterDescripion': {hideable: true, hidden: true, sortable: false, editable: false}
				, 'operator': {hideable: false, hidden: false, sortable: false, editable: false}
				, 'leftOperandDescription': {hideable: false, hidden: false, sortable: false, editable: false}
				, 'leftOperandType': {hideable: true, hidden: true, sortable: false, editable: false}
				, 'rightOperandDescription': {hideable: false, hidden: false, sortable: false, editable: false}				
				, 'rightOperandType': {hideable: true, hidden: true, sortable: false, editable: false}
				, 'booleanConnector': {hideable: true, hidden: false, sortable: false}	
				, 'deleteButton': {hideable: true, hidden: true, sortable: false}
				, 'promptable': {hideable: true, hidden: true, sortable: false}	
			}
    	});
    	this.filterGrid.grid.on('render', function(){
    		this.filterGrid.store.on('add', function(s, r, i){
        		this.filterGrid.sm.selectRow(i);
        	}, this);
    		this.resetFilterGrid();
    	}, this);
    	items.push(this.filterGrid);
    	
    	
    	this.formPanel = new Ext.form.FormPanel({
    		frame:true,
    		labelWidth: 80,
    		defaults: {
    			//width: 225
    		},
    	    bodyStyle:'padding:5px 5px 0',
    	    buttonAlign : 'center',
    	    items: items,
    	    buttons: [{
    			text: LN('sbi.formbuilder.staticclosefilterwizard.buttons.apply'),
    		    handler: function(){
    	    		this.fireEvent('apply', this, this.getTarget(), this.getFormState());
                	this.hide();
            	}
            	, scope: this
    	    },{
    		    text: LN('sbi.formbuilder.staticclosefilterwizard.buttons.cancel'),
    		    handler: function(){
                	this.hide();
            	}
            	, scope: this
    		}]
    	});
    	
    	
	}
	
	, unmarshalFilterRecord: function(r) {
		var filterConf = {};
		
		filterConf.leftOperandValue = r.data.leftOperandValue;
		filterConf.leftOperandDescription = r.data.leftOperandDescription;
		var operator = r.data.operator;
		filterConf.operator = operator;
		var filterValues = [];
		if (operator == 'BETWEEN' || operator == 'NOT BETWEEN' || 
				operator == 'IN' || operator == 'NOT IN') {
			filterValues = r.data.rightOperandValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
		} else {
			filterValues.push(r.data.rightOperandValue); 
		}
		filterConf.rightOperandValue = filterValues;
		filterConf.booleanConnector = r.data.booleanConnector;
		
		return filterConf;
	}
	
	, marshalFilterRecord: function(c) {
		var recordData = {};
		
		recordData.leftOperandValue = c.leftOperandValue;
		recordData.leftOperandDescription = c.leftOperandDescription || c.leftOperandValue;
		recordData.operator = c.operator;
		recordData.rightOperandValue = c.rightOperandValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
		recordData.rightOperandDescription = c.rightOperandValue;
		recordData.booleanConnector = c.booleanConnector || 'AND';
		
		return recordData;
	}
	
	, onRowSelect: function(selectionModel, rowIndex, r) {
		//alert('onRowSelect IN');
		this.setFilterDetailState( this.unmarshalFilterRecord(r) );
		//alert('onRowSelect OUT');
	}
		
	, onFormStateChange: function(field, newValue, oldValue) {
		//alert('onFormStateChange IN');
		var r = this.filterGrid.sm.getSelected();
		if(r === undefined) {
			this.filterGrid.sm.selectRow(this.filterGrid.store.getCount()-1);
			r = this.filterGrid.sm.getSelected();
		}		
		var i = this.filterGrid.store.indexOf(r);
		if(field.name === 'leftOperand') {
			var value = this.leftOperandField.getValue();
			var description = this.leftOperandField.getDescription();
			this.filterGrid.modifyFilter({leftOperandDescription: description, leftOperandValue: value}, i);
		} else if(field.name === 'operator') {
			this.filterGrid.modifyFilter({operator: newValue}, i);
		} else if(field.name === 'rightOperand') {
			this.filterGrid.modifyFilter({rightOperandDescription: newValue, rightOperandValue: newValue}, i);
		}
		//alert('onFormStateChange OUT');
	}
	
    , getExpressionAsObject: function(tree) {
        var o = {}; 
        var types = ['UNDEF', 'NODE_OP', 'NODE_CONST']; 
		  	var values = ['UNDEF', 'AND', 'OR', 'GROUP'];
		  	
		  	o.type = types[tree.attributes.type];
		    o.value = (tree.attributes.type==1? values[tree.attributes.value]: tree.attributes.value);
		    o.childNodes = [];
        if(tree.childNodes && tree.childNodes.length > 0) {             
          for(var i = 0; i < tree.childNodes.length; i++) {
        	  o.childNodes.push( this.getExpressionAsObject(tree.childNodes[i]) );
          }           
        }   
        
        return o;        
    }
	
});