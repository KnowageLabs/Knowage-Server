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
  * - Andrea Gioia (andrea.gioia@eng.it)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.StaticOpenFilterWizard = function(openFilter, config) {
	
	var defaultSettings = {
		// set default values here
		title: LN('sbi.formbuilder.staticopenfilterwizard.title')
		, autoScroll: true
		, width: 550
		, height: 400
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticOpenFilterWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticOpenFilterWindow);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	var params = {'fieldId': openFilter.field};
	this.services = this.services || new Array();
	this.services['getEntityFields'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ENTITY_FIELDS'
		, baseParams: params
	});
	this.services['getQuery'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_QUERY_ACTION'
		, baseParams: {}
	});
	
	this.entityId = openFilter.field;
	this.queryType = openFilter.queryType || 'standard';
	if (openFilter.queryRootEntity === undefined || openFilter.queryRootEntity === null) {
		openFilter.queryRootEntity = false;
	} else {
		if(typeof openFilter.queryRootEntity === "string") {
			openFilter.queryRootEntity = (openFilter.queryRootEntity === 'true');
		}
	}
	
	this.initForm(openFilter);
	
	c = Ext.apply(c, {
      	items: [this.openFilterForm]
	});
	
	// constructor
    Sbi.formbuilder.StaticOpenFilterWizard.superclass.constructor.call(this, c);
    
    this.addEvents('apply');
    
};

Ext.extend(Sbi.formbuilder.StaticOpenFilterWizard, Ext.Window, {
	
	services: null
	
	, openFilterForm: null
	, standardQueryDetails: null
	, customQueryDetails: null
	
	// base
	, filterName: null
	, filterEntity: null
	, filterOperatorCombo: null
	, maxSelectionNumber: null
	// standard query
	, orderByFieldCombo: null
	, orderTypeCombo: null
	// custom query
	, lookupQueryCombo: null
	
	
	
	// ---------------------------------------------------------------------------------
	// Initialization methods
	// ---------------------------------------------------------------------------------
	
	, initForm: function(openFilter) {
	
		this.initFilterBaseFieldSet(openFilter);
		this.initLookupQueryFieldSet(openFilter);
	   
	    	    	
	    Ext.form.Field.prototype.msgTarget = 'side';
	    this.openFilterForm = new Ext.form.FormPanel({
	        frame: true,
	        monitorValid:true,
	        bodyStyle: 'padding:5px 5px 0',
	        items: [this.filterName, this.filterEntity, this.filterOperatorCombo, this.maxSelectionNumber, 
	                this.queryDetails],
	        buttons: [
	            {text: LN('sbi.formbuilder.staticopenfilterwizard.buttons.apply'),formBind:true, handler: this.apply, scope: this}
	            , {text: LN('sbi.formbuilder.staticopenfilterwizard.buttons.cancel'), handler: function () {this.close();}, scope: this}
	        ]
	    });
	    
	    return this.openFilterForm;	    
	}

	// ---------------------------------------------------------------------------------
	// Init base field set
	// ---------------------------------------------------------------------------------

	, initFilterBaseFieldSet: function(openFilter) {
		this.filterName = new Ext.form.TextField({
			id: 'text',
			name: 'text',
			value: openFilter.text,
			allowBlank: false, 
			inputType: 'text',
			maxLength: 100,
			width: 250,
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.filtername.label')
		});
		
		this.filterEntity = new Ext.form.TextField({
			id: 'entity',
			name: 'entity',
			value: openFilter.text,
			allowBlank: false, 
			inputType: 'text',
			maxLength: 100,
			width: 250,
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.filterentity.label'),
			disabled: true
		});
		
		var filterOptStore = new Ext.data.SimpleStore({
		    fields: ['funzione', 'nome', 'descrizione'],
		    data : [
		            //['NONE', LN('sbi.qbe.filtergridpanel.foperators.name.none'), LN()],
		            ['EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.eq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eq')],
		            ['NOT EQUALS TO', LN('sbi.qbe.filtergridpanel.foperators.name.noteq'),  LN('sbi.qbe.filtergridpanel.foperators.desc.noteq')],
		            ['GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.gt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.gt')],
		            ['EQUALS OR GREATER THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqgt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqgt')],
		            ['LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.lt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.lt')],
		            ['EQUALS OR LESS THAN', LN('sbi.qbe.filtergridpanel.foperators.name.eqlt'),  LN('sbi.qbe.filtergridpanel.foperators.desc.eqlt')],
		            ['STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.starts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.starts')],
		            ['NOT STARTS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notstarts'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notstarts')],
		            ['ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.ends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.ends')],
		            ['NOT ENDS WITH', LN('sbi.qbe.filtergridpanel.foperators.name.notends'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notends')],
		            ['CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.contains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.contains')],
		            ['NOT CONTAINS', LN('sbi.qbe.filtergridpanel.foperators.name.notcontains'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notcontains')],
		            
		            //['BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.between'),  LN('sbi.qbe.filtergridpanel.foperators.desc.between')],
		            //['NOT BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.notbetween'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notbetween')],
		            ['IN', LN('sbi.qbe.filtergridpanel.foperators.name.in'),  LN('sbi.qbe.filtergridpanel.foperators.desc.in')],
		            ['NOT IN', LN('sbi.qbe.filtergridpanel.foperators.name.notin'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notin')]
		            
		            //['NOT NULL', LN('sbi.qbe.filtergridpanel.foperators.name.notnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notnull')],
		            //['IS NULL', LN('sbi.qbe.filtergridpanel.foperators.name.isnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.isnull')]
		    ]
		});
		
	    this.filterOperatorCombo = new Ext.form.ComboBox({
			//tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',
	    	name: 'operator',
			store:  filterOptStore, 
			displayField: 'nome',
			valueField: 'funzione',
			maxHeight: 200,
			allowBlank: false,
			editable: true,
			typeAhead: true, // True to populate and autoselect the remainder of the text being typed after a configurable delay
			mode: 'local',
			forceSelection: true, // True to restrict the selected value to one of the values in the list
			triggerAction: 'all',
			emptyText: LN('sbi.qbe.filtergridpanel.foperators.editor.emptymsg'),
			selectOnFocus: true, //True to select any existing text in the field immediately on focus
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.filteroperator.label'),
			value: openFilter.operator
	    });
	    
	    var selectionNumbersStore = new Ext.data.SimpleStore({
		    fields: ['name', 'value'],
		    data : [
		            [1, 1],
		            [2, 2],
		            [3, 3],
		            [4, 4],
		            [5, 5],
		            [6, 6],
		            [7, 7],
		            [8, 8],
		            [9, 9]
		    ]
		});
	
	    this.maxSelectionNumber = new Ext.form.ComboBox({
	    	name: 'maxSelectedNumber',
			store:  selectionNumbersStore, 
			displayField: 'name',
			valueField: 'value',
			maxHeight: 200,
			allowBlank: false,
			editable: true,
			typeAhead: true, 
			mode: 'local',
			forceSelection: true,
			triggerAction: 'all',
			emptyText: '',
			selectOnFocus: true,
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.maxselectionnumber.label'),
			value: openFilter.maxSelectedNumber
	    });
	}

	
	// ---------------------------------------------------------------------------------
	// Init lookup query  field set
	// ---------------------------------------------------------------------------------
	
	, initLookupQueryFieldSet: function(openFilter) {
	    this.initStandardQueryFieldSet(openFilter);
	    this.initCustomQueryFieldSet(openFilter);
	    
	    this.standardQueryDetails.twinFieldSet = this.customQueryDetails;
	    this.customQueryDetails.twinFieldSet = this.standardQueryDetails;
	    
	    var toggleFn = function() {
	    	if(this.checkbox.dom.checked === true) {
	    		this.enable();
	    		this.twinFieldSet.checkbox.dom.checked = false;
	    		this.twinFieldSet.disable();
			} else {
				this.disable();
	    		this.twinFieldSet.checkbox.dom.checked = true;
	    		this.twinFieldSet.enable();
			}
	    }
	    
	    
	    
	    this.customQueryDetails.onCheckClick = toggleFn.createDelegate(this.customQueryDetails);
	    this.standardQueryDetails.onCheckClick = toggleFn.createDelegate(this.standardQueryDetails)
	    
	    this.queryDetails = new Ext.form.FieldSet({
            title: LN('sbi.formbuilder.staticopenfilterwizard.lookupquerydetailssection.title'),
            autoHeight: true,
            autoWidth: true,
            items: [this.standardQueryDetails, this.customQueryDetails]
        });
	    
	    return this.queryDetails;
	}
	
	
	
	, initStandardQueryFieldSet: function(openFilter) {
		// order by field
		var orderByFieldStore = new Ext.data.Store({
			proxy: new Ext.data.HttpProxy({
				url: this.services['getEntityFields']
			})
		    , reader: new Ext.data.JsonReader({id: 'id'}, [
	            {name:'id'},
	            {name:'name'}
	        ])
		});
		    
		this.orderByFieldCombo = new Ext.form.ComboBox({
		   	name: 'orderBy',
			store: orderByFieldStore, 
			displayField: 'name',
			valueField: 'id',
			maxHeight: 200,
			allowBlank: true,
			editable: true,
			typeAhead: true, 
			forceSelection: true,
			triggerAction: 'all',
			emptyText: '',
			selectOnFocus: true,
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.orderbyfield.label')
		});
		    
		this.orderByValue = openFilter.orderBy;
		if (this.orderByValue !== undefined && this.orderByValue !== '') {
			orderByFieldStore.on('load', function() {
				this.orderByFieldCombo.setValue(this.orderByValue);
			}, this);
			orderByFieldStore.load();
		}
		
		
		// order by type
		var orderingTypesStore = new Ext.data.SimpleStore({
			fields: ['type', 'nome', 'descrizione'],
			data : [
			        ['NONE', LN('sbi.qbe.selectgridpanel.sortfunc.name.none'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.none')],
				    ['ASC', LN('sbi.qbe.selectgridpanel.sortfunc.name.asc'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.asc')],
				    ['DESC', LN('sbi.qbe.selectgridpanel.sortfunc.name.desc'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.desc')]
			] 
		});
		     
		this.orderTypeCombo = new Ext.form.ComboBox({
		   	 name: 'orderType',
		     tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
		     allowBlank: true,
		     editable: false,
		     store: orderingTypesStore,
		     displayField:'nome',
		     valueField:'type',
		     typeAhead: true,
		     mode: 'local',
		     triggerAction: 'all',
		     autocomplete: 'off',
		     emptyText: LN('sbi.qbe.selectgridpanel.sortfunc.editor.emptymsg'),
		     fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.fields.ordertype.label'),
		     selectOnFocus: true,
		     value: openFilter.orderType === undefined ? '' : openFilter.orderType
	    });
		 
		// query details
		this.standardQueryDetails = new Ext.form.FieldSet({
			checkboxToggle:true,
	        title: LN('sbi.formbuilder.staticopenfilterwizard.standardquerydetailssection.title'),
	        autoHeight:true,
	        autoWidth: true,
	        defaultType: 'textfield',
	        collapsed: false,
	        items :[
		        this.orderByFieldCombo, 
		        this.orderTypeCombo,
		        {
		        	xtype: 'radio',
		        	hideLabel: false,
		        	fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.lookupquerydetailssection.promptvalues'),
		        	boxLabel: LN('sbi.formbuilder.staticopenfilterwizard.lookupquerydetailssection.donotqueryrootentity'),
		        	name: 'queryRootEntity',
		        	inputValue: false,
		        	checked: !openFilter.queryRootEntity
		        }, {
		        	xtype: 'radio',
		        	hideLabel: false,
		        	fieldLabel: '',
		        	labelSeparator: '',
		            boxLabel: LN('sbi.formbuilder.staticopenfilterwizard.lookupquerydetailssection.queryrootentity'),
		            name: 'queryRootEntity',
		            inputValue: true,
		            checked: openFilter.queryRootEntity
		        }
	        ]
	    });
		
		this.standardQueryDetails.on('render', function() {
	    	if(this.queryType !== 'standard') {
	    		this.standardQueryDetails.checkbox.dom.checked = false;
	    		this.standardQueryDetails.disable();
	    		//this.customQueryDetails.checkbox.dom.checked = true;
	    		//this.customQueryDetails.enable();
	    	} else {
	    		this.standardQueryDetails.checkbox.dom.checked = true;
	    		this.standardQueryDetails.enable();
	    	}
	    }, this);
		
		
		
		return this.standardQueryDetails;		   
	}
	
	, initCustomQueryFieldSet: function(openFilter) {
	
	    var lookupQueryStore = new Ext.data.JsonStore({
	        url: this.services['getQuery'],
	        // reader configs
	        root: 'results',
	        idProperty: 'id',
	        fields: ['id', 'name', 'description']
	    });
	    	    
	    this.lookupQueryCombo = new Ext.form.ComboBox({
	    	name: 'lookupQuery',
			store: lookupQueryStore, 
			displayField: 'name',
			valueField: 'id',
			maxHeight: 200,
			allowBlank: true,
			editable: true,
			typeAhead: true, 
			forceSelection: true,
			triggerAction: 'all',
			emptyText: '',
			selectOnFocus: true, 
			fieldLabel: LN('sbi.formbuilder.staticopenfilterwizard.customquerydetailssection.lookupquery')
	    });
	    
	    this.lookupQueryValue = openFilter.lookupQuery;
	    if (this.lookupQueryValue !== undefined && this.lookupQueryValue !== '') {
		    lookupQueryStore.on('load', function() {
		    	this.lookupQueryCombo.setValue(this.lookupQueryValue);
		    }, this);
		    lookupQueryStore.load();
	    };
	    	    
	    this.customQueryDetails = new Ext.form.FieldSet({
            checkboxToggle:true,
            title: LN('sbi.formbuilder.staticopenfilterwizard.customquerydetailssection.title'),
            autoHeight:true,
            autoWidth: true,
            defaultType: 'textfield',
            collapsed: false,
            //style: 'border-style:solid;border-width:1px;padding:25px 45px 20px 20px',
            //maskDisabled: false,
            items :[this.lookupQueryCombo]
        });
	    
	    this.customQueryDetails.on('render', function() {
	    	if(this.queryType === 'standard') {
	    		 //this.standardQueryDetails.checkbox.dom.checked = true;
	    		 //this.standardQueryDetails.enable();
	    		 this.customQueryDetails.checkbox.dom.checked = false;
	    		 this.customQueryDetails.disable();
	    	} else {
	    		this.customQueryDetails.checkbox.dom.checked = true;
	    		this.customQueryDetails.enable();
	    	}
	    }, this);
	    
	    

	    return this.customQueryDetails;
	}
	
	

	, apply : function () {
		var formState = this.getFormState();
		this.fireEvent('apply', formState);
		this.close();
	}
	
	, getFormState : function () {
		
		var openFilter = this.openFilterForm.getForm().getValues();
		// work-around: for comboboxes, form.getValues() retrieves the displayed value, not the hidden actual value
		openFilter.operator = this.filterOperatorCombo.getValue();
		openFilter.orderBy = this.orderByFieldCombo.getValue();
		openFilter.orderType = this.orderTypeCombo.getValue();
		openFilter.lookupQuery = this.lookupQueryCombo.getValue();
		// end work-around
		openFilter.field = this.entityId;
		if (openFilter.maxSelectedNumber == undefined || openFilter.maxSelectedNumber == null || openFilter.maxSelectedNumber == 1) {
			openFilter.singleSelection = true;
		} else {
			openFilter.singleSelection = false;
		}
		openFilter.queryRootEntity = (typeof openFilter.queryRootEntity === "string") ? openFilter.queryRootEntity === 'true' : openFilter.queryRootEntity;
		
		openFilter.queryType = (this.customQueryDetails.checkbox.dom.checked === true)? 'custom': 'standard';
		
		return openFilter;

	}
	
});