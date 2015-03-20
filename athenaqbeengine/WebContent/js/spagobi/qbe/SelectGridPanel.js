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

Sbi.qbe.SelectGridPanel = function(config) {

	var defaultSettings = {
		border: true
		//, enableToolbar: true
		, enableTbAddCalculatedBtn: false
		, enableTbHideNonvisibleBtn: true
		, enableTbDeleteAllBtn: true
	};
	
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.selectGridPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.selectGridPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	
	this.services = new Array();
	
	this.services['getParameters'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_PARAMETERS_ACTION'
		, baseParams: {}
	});
	
	this.services['getAttributes'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ATTRIBUTES_ACTION'
		, baseParams: {}
	});
	
	this.addEvents('filter', 'having');
	
	this.initStore(c);
	this.initSelectionModel(c);
	this.initColumnModel(c);
	this.initToolbar(c);
	this.initGrid(c);
	this.initGridListeners(c);

	
	Ext.apply(c, {
		layout: 'fit'
		/*
		, autoWidth: Ext.isIE ? false : true
		, width: Ext.isIE ? undefined : 'auto'
		*/
		, items: [this.grid]
	});	
	
	// constructor
	Sbi.qbe.SelectGridPanel.superclass.constructor.call(this, c);
	
	if(c.query && c.query.fields && c.query.fields.length > 0){
    	this.loadSavedData(c.query);
    }

};

Ext.extend(Sbi.qbe.SelectGridPanel, Ext.Panel, {
    
	services: null
	, store: null
	, Record: null
	, sm: null
	, cm: null
	, plgins: null
	, toolbar: null
	, distinctCheckBox: null
	, grid: null
	, dropTarget: null
	, calculatedFieldWizard : null
	, inLineCalculatedFieldWizard : null
	
	, type: 'selectgrid'
	
	// static members
	, aggregationFunctionsStore:  new Ext.data.SimpleStore({
		 fields: ['funzione', 'nome', 'descrizione'],
		 data: Sbi.constants.qbe.SELECT_CLAUSE_AGGREGATION_FUNCTION
	 })

	, orderingTypesStore: new Ext.data.SimpleStore({
	     fields: ['type', 'nome', 'descrizione'],
	     data : [
		    ['NONE', LN('sbi.qbe.selectgridpanel.sortfunc.name.none'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.none')],
		    ['ASC', LN('sbi.qbe.selectgridpanel.sortfunc.name.asc'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.asc')],
		    ['DESC', LN('sbi.qbe.selectgridpanel.sortfunc.name.desc'), LN('sbi.qbe.selectgridpanel.sortfunc.desc.desc')]
		] 
	})
	
	// public methods
	
	, loadSavedData: function(query) {
  		this.setFields(query.fields);
  		
  		if (query.distinct === true) {
  			this.distinctCheckBox.setValue(true);
  		} else {
  			this.distinctCheckBox.setValue(false);
  		}
  	}
  	
	, createField: function() {
		var field;
		
		field = new Object();		
		field = Ext.apply(field, {
			id: '', 
			alias: '', 
			type: Sbi.constants.qbe.FIELD_TYPE_SIMPLE,
			
			entity: '', 
			field: '',
			
			funct: '',
			group: '',
			order: '',
			
			
			'include': true, 
			visible: true
		});
			      
		return field;
	}

	, addField : function(field) {
		field = field || {};
		field = Ext.apply(this.createField(), field || {});
		var record = new this.Record( field );
		this.grid.store.add(record); 
	}
	
	, insertField: function(field, i) {
		if(i != undefined) {
			field = field || {};
			field = Ext.apply(this.createField(), field || {});
			var record = new this.Record( field );
			this.grid.store.insert(i, record); 
		} else {
			this.addField(field);
		}
	}
	
	, modifyField: function(field, i) {
		if(i != undefined) {			
			var record = this.store.getAt( i );
			Ext.apply(record.data, field || {});	
			record = this.store.getAt( i );
			this.store.fireEvent('datachanged', this.store) ;
		}
	}

	, addRow: function(config, i) {	
	   Sbi.qbe.commons.deprectadeFunction('SelectGridPanel', 'addRow');
	   this.insertField(config, i);
	}
	
	, deleteFields: function() {
		this.grid.store.removeAll();
	}
	
	, setFields: function(fields) {
		this.deleteFields();
		for(var i = 0; i < fields.length; i++) {
  			var field = fields[i];
  			var record = new this.Record(field);
  			this.store.add(record); 
  		}
	}
	
	, getFields: function() {
		var fields = [];
		for(i = 0; i < this.store.getCount(); i++) {
			var record = this.store.getAt(i);
			var field = Ext.apply({}, record.data);
			field.group = field.group || false;
			fields.push(field);
		}
		
		return fields;
	}
	
	, hideNonVisibleRows: function(button, pressed) {
		
		this.grid.store.filterBy(function(record, id) {
			if(!pressed) return true; // show all
			
			return record.data['visible'];
		});
	}
	
	
	, updateGroupByColumn: function() {
		
//		var index = this.grid.store.findBy(function(record) {
//			var isFunction = !(record.data['funct'] == undefined 
//					|| record.data['funct'].trim() == ''
//					|| record.data['funct'] == 'NONE') ||
//					   (record.data['type'] == 'inLineCalculatedField' && (
//						(record.data['id']['expression'].indexOf('AVG')>=0)||
//						(record.data['id']['expression'].indexOf('SUM')>=0))
//					   );
//			return isFunction;
//		});
//		
//		var groupFlag = (index == -1? '': 'true');			
//					
//		this.grid.store.each(function(record) {
//			
//			if(( record.data['funct'] == undefined
//						|| record.data['funct'].trim() == ''
//						|| record.data['funct'] == 'NONE') &&
//						record.data['type'] != 'inLineCalculatedField'
//			) {
//					//alert('true');
//					record.data['group'] = groupFlag;	
//				} else {
//					//alert('false');
//					record.data['group'] = '';	
//				}								
//	
//		}
//		
//		)
	}
	
   
	// --------------------------------------------------------------------------------
	// 	private methods
	// --------------------------------------------------------------------------------
	
	, initStore: function() {
		this.store =  new Ext.data.SimpleStore({
	        fields: [
	           {name: 'id'},
	           {name: 'alias'},
	           {name: 'type'},
	           
	           {name: 'entity'},
	           {name: 'longDescription'},
	           {name: 'field'},
	           
	           {name: 'funct'},	 
	           {name: 'group'},
	           {name: 'order'},
	           
	           {name: 'include'},
	           {name: 'visible'},
	           
	           {name: 'filter'},
	           {name: 'del'}          
	        ]
		});
		
		this.Record = Ext.data.Record.create([
		      {name: 'id', type: 'string'},
		      {name: 'alias', type: 'string'},
		      {name: 'type', type: 'string'},
		      
		      {name: 'entity', type: 'string'},
		      {name: 'longDescription', type: 'string'},
		      {name: 'field', type: 'string'},
		     
		      {name: 'funct', type: 'string'},
		      {name: 'group', type: 'string'},
		      {name: 'order', type: 'string'},
		      
		      {name: 'include', type: 'bool'},
		      {name: 'visible', type: 'bool'},
		      
		      {name: 'filter', type: 'string'},
		      {name: 'having', type: 'string'},
		      {name: 'del', type: 'string'}
		]); 
	}
	
	
	, initSelectionModel: function() {
		this.sm = new Ext.grid.RowSelectionModel();
	}

	, columns : {
		'visible': {
			hideable: true
			, hidden: false	
			, width: 50
			, sortable: false
		}
		
		, 'group': {
			hideable: true
			, hidden: false	
			, width: 50
			, sortable: false
		}
		
		, 'include': {
			hideable: true
			, hidden: false	
			, width: 50
			, sortable: false
		}
		
		, 'filter': {
			hideable: true
		    , hidden: false	
		    , width: 50
		    , sortable: false
		}
		
		, 'having': {
			hideable: true
		    , hidden: false	
		    , width: 50
		    , sortable: false
		}
		
		
		, 'funct': {
			hideable: true
		    , hidden: false
		    , width: 50
		    , sortable: false
		}
		
		, 'alias': {
			hideable: true
		    , hidden: false	
		    , sortable: false
		}
		
		, 'field': {
			hideable: true
			, hidden: false	
			, sortable: false
		}
		
		, 'entity': {
			hideable: true
			, hidden: false	
			, sortable: false
		}
		
		, 'order': {
			hideable: true
		    , hidden: false	
		    , width: 50
		    , sortable: false
		}
	}
	
	
	, initColumnModel: function() {
		// check-columns
		
	    var visibleCheckColumn = new Ext.grid.CheckColumn(
	    	Ext.apply({
	    		header: LN('sbi.qbe.selectgridpanel.headers.visible')
	    		, dataIndex: 'visible'
	    		, hideable: true
	    		, hidden: false	
	    		, width: 50
	    		, sortable: false
	    	}, this.columns['visible'] || {})
	    );
	    
	    var groupCheckColumn = new Ext.grid.CheckColumn(
	    	Ext.apply({
		       header:  LN('sbi.qbe.selectgridpanel.headers.group')
		       , dataIndex: 'group'
		       , hideable: true
			   , hidden: false	
			   , width: 50
			   , sortable: false
		    }, this.columns['group'] || {})
	    );
	    
	    var includeCheckColumn = new Ext.grid.CheckColumn(
		    Ext.apply({
		       header:  LN('sbi.qbe.selectgridpanel.headers.include')
		       , dataIndex: 'include'
		       , hideable: true
			   , hidden: false	
			   , width: 50
			   , sortable: false
		    }, this.columns['include'] || {})
	    );
	    
	    // button-columns
	    var delButtonColumn = new Ext.grid.ButtonColumn(
		    Ext.apply({
		       header:  LN('sbi.qbe.selectgridpanel.headers.delete.column')
		       , dataIndex: 'delete'
		       , imgSrc: '../img/actions/delete.gif'
		       , clickHandler:function(e, t){
		          var index = this.grid.getView().findRowIndex(t);
		          var record = this.grid.store.getAt(index);
		          this.grid.store.remove(record);
		       }
		       , hideable: true
		       , hidden: true	
		       , width: 50
		       , sortable: false
		    }, this.columns['delete'] || {})
	    );
	     
	    var filterButtonColumn = new Ext.grid.ButtonColumn(
		    Ext.apply({
			   header:  LN('sbi.qbe.selectgridpanel.headers.filter')
			   , dataIndex: 'filter'
			   , imgSrc: '../img/actions/filter.gif'
			      
			   , clickHandler:function(e, t){
			          var index = this.grid.getView().findRowIndex(t);
			          var record = this.grid.store.getAt(index);
			          this.grid.fireEvent('actionrequest', this, 'filter', record);
			   }
		       , hideable: true
		       , hidden: false	
		       , width: 50
		       , sortable: false
		    }, this.columns['filter'] || {})
	    );
	    
	    var havingButtonColumn = new Ext.grid.ButtonColumn(
		    Ext.apply({
			   header:  LN('sbi.qbe.selectgridpanel.headers.having')
			   , dataIndex: 'having'
			   , imgSrc: '../img/actions/filter.gif'
			      
			   , clickHandler:function(e, t){
			          var index = this.grid.getView().findRowIndex(t);
			          var record = this.grid.store.getAt(index);
			          this.grid.fireEvent('actionrequest', this, 'having', record);
			   }
		       , hideable: true
		       , hidden: false	
		       , width: 50
		       , sortable: false
		    }, this.columns['having'] || {})
	    );
		
	    this.cm = new Ext.grid.ColumnModel([
		     new Ext.grid.RowNumberer(),
		     Ext.apply({
		    	 header: LN('sbi.qbe.selectgridpanel.headers.entity')
		    	 , dataIndex: 'entity'
		    	 , hideable: true
			     , hidden: false	
			     , sortable: false
		     }, this.columns['entity'] || {})
		     
		     , Ext.apply({
		    	 id:'field'
		         , header: LN('sbi.qbe.selectgridpanel.headers.field')
		         , dataIndex: 'field'
		         , hideable: true
				 , hidden: false	
				 , sortable: false
				 , renderer: this.getCellTooltip
		     }, this.columns['field'] || {})
		     
		     , Ext.apply({
		         header: LN('sbi.qbe.selectgridpanel.headers.alias')
		         , dataIndex: 'alias'
		         , editor: new Ext.form.TextField({
		        	 allowBlank: true
		         })
			     , hideable: true
			     , hidden: false	
			     , sortable: false
		     }, this.columns['alias'] || {})
		     
		     , Ext.apply({
		    	 header: LN('sbi.qbe.selectgridpanel.headers.function')
		         , dataIndex: 'funct'
		         , editor: new Ext.form.ComboBox({
			         tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
			         allowBlank: true,
			         editable:false,
			         store: this.aggregationFunctionsStore,
			         displayField:'nome',
			         valueField:'funzione',
			         typeAhead: true,
			         mode: 'local',
			         triggerAction: 'all',
			         autocomplete: 'off',
			         emptyText: LN('sbi.qbe.selectgridpanel.aggfunc.editor.emptymsg'),
			         selectOnFocus:true
		         })
			     , hideable: true
			     , hidden: false
			     , width: 50
			     , sortable: false
		     }, this.columns['funct'] || {})
		     
		     , Ext.apply({
		    	 header: LN('sbi.qbe.selectgridpanel.headers.order')
		         , dataIndex: 'order'
		         , editor: new Ext.form.ComboBox({
			         tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
			         allowBlank: true,
			         editable:false,
			         store: this.orderingTypesStore,
			         displayField:'nome',
			         valueField:'type',
			         typeAhead: true,
			         mode: 'local',
			         triggerAction: 'all',
			         autocomplete: 'off',
			         emptyText: LN('sbi.qbe.selectgridpanel.sortfunc.editor.emptymsg'),
			         selectOnFocus:true
		         })
			     , hideable: true
			     , hidden: false	
			     , width: 50
			     , sortable: false
		     }, this.columns['order'] || {}), 
		     
		     groupCheckColumn, 
		     includeCheckColumn,
		     visibleCheckColumn,
		     filterButtonColumn,
		     havingButtonColumn,
		     delButtonColumn
	     ]);	
	    
	    this.plgins = [visibleCheckColumn, includeCheckColumn, groupCheckColumn, delButtonColumn, filterButtonColumn, havingButtonColumn];
	}
	
	, initWizards: function() {
		this.initCalculatedFieldWizard();
		this.initInLineCalculatedFieldWizard();
	}
	
	, initCalculatedFieldWizard: function() {
		
		var fields = new Array();
		
		var parametersLoader = new Ext.tree.TreeLoader({
	        baseParams: {},
	        dataUrl: this.services['getParameters']
	    });
		
		var attributesLoader = new Ext.tree.TreeLoader({
	        baseParams: {},
	        dataUrl: this.services['getAttributes']
	    });
		
		this.calculatedFieldWizard = new Sbi.qbe.CalculatedFieldWizard({
     		title:  LN('sbi.qbe.calculatedFields.title'),
     		expItemGroups: [
     		    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}, 
     		    {name:'parameters', text: LN('sbi.qbe.calculatedFields.parameters'), loader: parametersLoader}, 
     		    {name:'attributes', text: LN('sbi.qbe.calculatedFields.attributes'), loader: attributesLoader},
     		    {name:'arithmeticFunctions', text: LN('sbi.qbe.calculatedFields.functions.arithmetic')},
     		    {name:'groovyFunctions', text: LN('sbi.qbe.calculatedFields.functions.script')}
     		],
     		fields: fields,
     		arithmeticFunctions: Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS,
     		groovyFunctions: Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_SCRIPT_FUNCTIONS,
     		expertMode: true,
        	scopeComboBoxData :[
        	    ['STRING','String', LN('sbi.qbe.calculatedFields.string.type')],
        	    ['HTML', 'Html', LN('sbi.qbe.calculatedFields.html.type')],
        	    ['NUMBER', 'Number', LN('sbi.qbe.calculatedFields.num.type')]
        	],
     		validationService: {
 				serviceName: 'VALIDATE_EXPRESSION_ACTION'
 				, baseParams: {contextType: 'query'}
 				, params: null
 			}
     	});
 		
     	this.calculatedFieldWizard.on('apply', function(win, formState, targetRecord){
     		var field = {id: formState, alias: formState.alias, type: Sbi.constants.qbe.FIELD_TYPE_CALCULATED, longDescription: formState.expression};
     		if(targetRecord) {
     			Ext.apply(targetRecord.data, field);	
     			this.store.fireEvent('datachanged', this.store) ;
     		} else {
     			this.addField({id: formState, alias: formState.alias, type: Sbi.constants.qbe.FIELD_TYPE_CALCULATED, longDescription: formState.expression});
     		}
     	}, this);
     	
     	this.calculatedFieldWizard.mainPanel.on('expert', this.onPassToNormalMode, this);
	}
	
	, initInLineCalculatedFieldWizard: function(fields) {
		
		var fields = new Array();
		
		this.inLineCalculatedFieldWizard = new Sbi.qbe.CalculatedFieldWizard({
    		title: LN('sbi.qbe.inlineCalculatedFields.title'),
    		expItemGroups: [
    		    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}, 
    		    {name:'arithmeticFunctions', text:  LN('sbi.qbe.calculatedFields.functions.arithmentic')},
    		    {name:'aggregationFunctions', text:  LN('sbi.qbe.calculatedFields.aggrfunctions')},
    		    {name:'dateFunctions', text:  LN('sbi.qbe.calculatedFields.datefunctions')}
    		],
    		fields: fields,
    		arithmeticFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS, // functionsForInline,
    		aggregationFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_AGGREGATION_FUNCTIONS, // aggregationFunctions,
    		dateFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS, // dateFunctions,
    		expertMode: false,
        	scopeComboBoxData :[
        	     ['STRING','String', LN('sbi.qbe.calculatedFields.string.type')],
    	         ['NUMBER', 'Number', LN('sbi.qbe.calculatedFields.num.type')],
    	         ['DATE', 'Date', LN('sbi.qbe.calculatedFields.num.type')]
    	    ],
    		validationService: {
				serviceName: 'VALIDATE_EXPRESSION_ACTION'
				, baseParams: {contextType: 'query'}
				, params: null
			}
    	});
		
     	this.inLineCalculatedFieldWizard.mainPanel.on('notexpert', this.onPassToExpertMode, this);
		
    	this.inLineCalculatedFieldWizard.on('apply', function(win, formState, targetRecord){
    		var field = {id: formState, alias: formState.alias, type: Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED, longDescription: formState.expression};
    		
    		if(targetRecord) {
    			Ext.apply(targetRecord.data, field);
    			this.store.fireEvent('datachanged', this.store);
    		} else {
    			this.addField({id: formState, alias: formState.alias, type: Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED, longDescription: formState.expression});
    		}
    	}, this);
	}
	
	
	
	, showInLineCalculatedFieldWizard: function(targetRecord) {
		
		if(this.inLineCalculatedFieldWizard === null) {
			this.initInLineCalculatedFieldWizard();
		}

		// get all records
		var records = this.store.queryBy( function(record) {
			return record;
		});
		
		var fields = new Array();
		//removes from the fields the calculated fields
		records.each(function(r) {
			if(r.data.type == Sbi.constants.qbe.FIELD_TYPE_SIMPLE){
				var field = {
					uniqueName: r.data.id,
					alias: r.data.alias,
					text: r.data.alias, 
					qtip: r.data.entity + ' : ' + r.data.field, 
					type: 'field', 
					value: 'fields[\'' + r.data.alias + '\']'
				};
				fields.push(field);
			}
		});			
		this.inLineCalculatedFieldWizard.show();
		this.inLineCalculatedFieldWizard.validationService.params = {fields: Ext.util.JSON.encode(fields)};
		this.inLineCalculatedFieldWizard.setExpItems('fields', fields);
		this.inLineCalculatedFieldWizard.setTargetRecord(targetRecord);
		this.inLineCalculatedFieldWizard.show();
	}
	
	, showCalculatedFieldWizard: function(targetRecord) {
		
		if(this.calculatedFieldWizard === null) {
			this.initCalculatedFieldWizard();
		}
		
		var records = this.store.queryBy( function(record) {
			return record.data.include === true;
		});
		
		var fields = new Array();
		
		records.each(function(r) {
			//if the if statement isn't commented removes from the fields the calculated fields
			//if(r.data.type == "datamartField"){
				var field = {
					uniqueName: r.data.id,
					alias: r.data.alias,
					text: r.data.alias, 
					qtip: r.data.entity + ' : ' + r.data.field, 
					type: 'field', 
					value: 'fields[\'' + r.data.alias + '\']'
				};
				fields.push(field);
			//}
		});			
		
		this.calculatedFieldWizard.show();
		this.calculatedFieldWizard.validationService.params = {fields: Ext.util.JSON.encode(fields)};
		this.calculatedFieldWizard.setExpItems('fields', fields);
		this.calculatedFieldWizard.setTargetRecord(targetRecord);
		this.calculatedFieldWizard.show();
	}
	
	, onPassToExpertMode: function() {
		var alias;
		if(this.inLineCalculatedFieldWizard != null &&  this.inLineCalculatedFieldWizard != undefined &&
		   this.inLineCalculatedFieldWizard.inputFields !== null && this.inLineCalculatedFieldWizard.inputFields !== undefined){
      			alias = this.inLineCalculatedFieldWizard.inputFields.alias.getValue();
      	}
		this.showCalculatedFieldWizard(null);
		this.inLineCalculatedFieldWizard.hide();
    	this.calculatedFieldWizard.mainPanel.setCFAlias(alias);
    }
	
	, onPassToNormalMode: function(){
		var alias;
		if(this.calculatedFieldWizard!=null && this.calculatedFieldWizard != undefined &&
 				this.calculatedFieldWizard.inputFields !== null && this.calculatedFieldWizard.inputFields !== undefined){
 			alias = this.calculatedFieldWizard.inputFields.alias.getValue();
 		}
 		this.showInLineCalculatedFieldWizard(null);
 		this.calculatedFieldWizard.hide();
 		this.inLineCalculatedFieldWizard.mainPanel.setCFAlias(alias);
 	}
	
	, initToolbar: function() {
		this.distinctCheckBox = new Ext.form.Checkbox({
			checked: false,
			boxLabel: LN('sbi.qbe.selectgridpanel.buttons.text.distinct')
		});
		
		this.toolbar = new Ext.Toolbar({
			items: [
			  this.distinctCheckBox,'-',
			 {
		        text: LN('sbi.qbe.selectgridpanel.buttons.text.add'),
		        tooltip: LN('sbi.qbe.selectgridpanel.buttons.tt.add'),
		        iconCls:'option',
		        hidden: (this.enableTbAddCalculatedBtn === false),
		        listeners: {
				  'click': {
					fn: function(){this.showInLineCalculatedFieldWizard(null);},
					scope: this
				  }
		        }
		     },'-',{
	            text: LN('sbi.qbe.selectgridpanel.buttons.text.hide'),
	            tooltip: LN('sbi.qbe.selectgridpanel.buttons.tt.hide'),
	            hidden: (this.enableTbHideNonvisibleBtn === false),
	            enableToggle: true,
	            iconCls:'option',
	            listeners: {
	            	'toggle': {
 						fn: this.hideNonVisibleRows,
 						scope: this
 					}
	            }
	          },'-',{
	            text: LN('sbi.qbe.selectgridpanel.buttons.text.deleteall'),
	            tooltip: LN('sbi.qbe.selectgridpanel.buttons.tt.deleteall'),
	            hidden: (this.enableTbDeleteAllBtn === false),
	            iconCls:'remove',
	            listeners: {
	            	'click': {
 						fn: this.deleteFields,
 						scope: this
 					}
	            }
	        }]
		});
	}
	

	, initGrid: function() {
		
	     // create the Grid
		 this.grid = new Ext.grid.EditorGridPanel({
			    title: LN('sbi.qbe.selectgridpanel.title'),   
			 	store: this.store,
		        cm: this.cm,  
		        sm : this.sm,
		        tbar: this.toolbar,
		        clicksToEdit:1,
		        plugins: this.plgins,	        
		        height: this.gridHeight,
		        frame: true,
		        border:true,  
		        style:'padding:10px',
		        iconCls:'icon-grid',
		        collapsible:false,
		        layout: 'fit',
		        viewConfig: {
		            forceFit: true
		        },		

		        enableDragDrop:true,
     			ddGroup: 'gridDDGroup'	
		    });
		 	this.grid.type = this.type;
	}
	
	, initGridListeners: function() {
		this.grid.addEvents('actionrequest');
		this.grid.on("actionrequest", function(grid, action, record){
			if(action === 'filter' && record.data.type != Sbi.constants.qbe.FIELD_TYPE_CALCULATED) {
				this.fireEvent('filter', this, record);
			}
			if(action === 'having' && record.data.type != Sbi.constants.qbe.FIELD_TYPE_CALCULATED) {	
				this.fireEvent('having', this, record);
			}
		}, this);
		
		this.grid.on("rowdblclick", function(grid,  rowIndex, e){
	    	var row;
	       	var record = grid.getStore().getAt( rowIndex );
	       	if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED) {
	       		if(!record.data.id.slots || record.data.id.slots.length == 0) {
	       			this.showInLineCalculatedFieldWizard(record);
	       		}
	       	} else if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_CALCULATED) {
	       		this.showCalculatedFieldWizard(record);
	       	}
	     }, this);
		
		this.grid.on("mouseover", function(e, t){
	    	var row;
	        this.targetRow = t;
	        if((row = this.getView().findRowIndex(t)) !== false){
	            this.getView().addRowClass(row, "row-over");
	        }     
	     }, this.grid);
	     
	     this.grid.on("mouseout", function(e, t){
	        var row;
	        this.targetRow = undefined;
	        if((row = this.getView().findRowIndex(t)) !== false && row !== this.getView().findRowIndex(e.getRelatedTarget())){
	            this.getView().removeRowClass(row, "row-over");
	        }
	     }, this.grid);
	    
	    this.grid.on('keydown', function(e){ 
	      if(e.keyCode === 46) {
	        var sm=this.getSelectionModel();
	        var ds = this.getStore()
	        var rows=sm.getSelections();
	        for (i = 0; i < rows.length; i++) {
	          this.store.remove( ds.getById(rows[i].id) );
	        }
	      }      
	    }, this.grid);	
	    
	    this.grid.store.on('update', function(e){
	    	this.updateGroupByColumn();
	    }, this); 
	}
	
	, getCellTooltip: function (value, cell, record) {
	 	var tooltipString = record.data.longDescription;
	 	if (tooltipString !== undefined && tooltipString != null) {
	 		cell.attr = ' ext:qtip="'  + Sbi.qbe.commons.Utils.encodeEscapes(tooltipString)+ '"';
	 	}
	 	return value;
	}
	
});