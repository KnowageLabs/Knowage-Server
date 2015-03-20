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

Sbi.qbe.FilterGridPanel = function(config) {
	
	
	var defaultSettings = {
		border: true,
		gridStyle: 'padding:10px'
	};
		
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.filterGridPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.filterGridPanel);
	}
		
	var c = Ext.apply(Sbi.settings.qbe.filterGridPanel, config, defaultSettings || {});
	Ext.apply(this, c);

	
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE'};
	this.services = new Array();
	
	this.services['getValuesForQbeFilterLookupService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_VALUES_FOR_QBE_FILTER_LOOKUP_ACTION'
		, baseParams: params
	});
	
	this.documentParametersStore = c.documentParametersStore;
	
	this.filterIdPrefix = LN('sbi.qbe.filtergridpanel.namePrefix');
	
	this.idCount = 0;
	this.initStore(c);
	this.initSelectionModel(c);
	this.initColumnModel(c);
	this.initToolbar(c);
	this.initGrid(c);
	this.initGridListeners(c);
	
	c = Ext.apply(c, {
		layout: 'fit',
		//autoWidth: true,
		//width: 'auto',
		items: [this.grid]
	});
	
	// constructor
    Sbi.qbe.FilterGridPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.qbe.FilterGridPanel, Ext.Panel, {
    
	services: null
	
	, parentQuery: null
	, query: null
	
	, store: null
	, Record: null
	, sm: null
	, cm: null
	, plgins: null
	, toolbar: null
	, grid: null
	, wizardExpression: false
	, idCount: null
	, dropTarget: null
	
	, type: 'filtergrid'
	

	// static members
	
	, booleanOptStore: new Ext.data.SimpleStore({
        fields: ['funzione', 'nome', 'descrizione'],
        data : [
                ['AND', LN('sbi.qbe.filtergridpanel.boperators.name.and'), LN('sbi.qbe.filtergridpanel.boperators.desc.and')],
                ['OR', LN('sbi.qbe.filtergridpanel.boperators.name.or'), LN('sbi.qbe.filtergridpanel.boperators.desc.or')]
        ]
    })

	, filterOptStore: new Ext.data.SimpleStore({
	    fields: ['funzione', 'nome', 'descrizione'],
	    data: Sbi.constants.qbe.WHERE_CLAUSE_COMPARISON_FUNCTIONS
	    /*
	    data : [
	            ['NONE', LN('sbi.qbe.filtergridpanel.foperators.name.none'), LN()],
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
	            
	            ['BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.between'),  LN('sbi.qbe.filtergridpanel.foperators.desc.between')],
	            ['NOT BETWEEN', LN('sbi.qbe.filtergridpanel.foperators.name.notbetween'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notbetween')],
	            ['IN', LN('sbi.qbe.filtergridpanel.foperators.name.in'),  LN('sbi.qbe.filtergridpanel.foperators.desc.in')],
	            ['NOT IN', LN('sbi.qbe.filtergridpanel.foperators.name.notin'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notin')],
	            
	            ['NOT NULL', LN('sbi.qbe.filtergridpanel.foperators.name.notnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.notnull')],
	            ['IS NULL', LN('sbi.qbe.filtergridpanel.foperators.name.isnull'),  LN('sbi.qbe.filtergridpanel.foperators.desc.isnull')]
	    ]
	    */
	})

	// public methods
	
	, createFilter: function() {
		var filter;
		var filterName;
		
		filter = new Object();
		filterName = this.filterIdPrefix + (++this.idCount);
		
		filter = Ext.apply(filter, {
			filterId: filterName
			, filterDescripion: filterName
			
			, promptable: false
			
			, leftOperandValue: ''
			, leftOperandDescription: ''
			, leftOperandLongDescription: ''
			, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
			, leftOperandDefaultValue: null
			, leftOperandLastValue: null
			, leftOperandAlias: null
			
			, operator: ''
				
			, rightOperandValue: ''
			, rightOperandDescription: ''
			, rightOperandLongDescription: ''
			, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
			, rightOperandDefaultValue: null
			, rightOperandLastValue: null
			, rightOperandAlias: null
			
			, booleanConnector: 'AND'
				
			, deleteButton: false
		});
		
		return filter;
	}

	, addFilter : function(filter) {
		filter = filter || {};
		filter = Ext.apply(this.createFilter(), filter || {});
		if (filter.rightOperandValue && filter.rightOperandValue instanceof Array) {
			if (filter.rightOperandValue.length == 1) {
				// case of Field Content, Analytical Driver, Subquery .... or single static value
				filter.rightOperandValue = filter.rightOperandValue[0];
			} else {
				// case of list of static values
				var joinedValues = filter.rightOperandValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				filter.rightOperandValue = joinedValues;
				filter.rightOperandDescription = joinedValues;
				filter.rightOperandLongDescription = joinedValues;
			}
		}
		if (filter.rightOperandLastValue && filter.rightOperandLastValue instanceof Array) {
			filter.rightOperandLastValue = filter.rightOperandLastValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
		}
		if (filter.rightOperandDefaultValue && filter.rightOperandDefaultValue instanceof Array) {
			filter.rightOperandDefaultValue = filter.rightOperandDefaultValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
		}
		
		if(filter.leftOperandValue.expression!=undefined){
			filter.leftOperandDescription = filter.leftOperandValue.alias; 
		} else if(this.documentParametersStore) {
			filter = this.documentParametersStore.modifyFilter(filter);
		}
		var record = new this.Record( filter );
		this.grid.store.add(record); 
	}
	
	, insertFilter: function(filter, i) {
		if(i != undefined) {
			filter = filter || {};
			filter = Ext.apply(this.createFilter(), filter || {});
			var record = new this.Record( filter );
			this.grid.store.insert(i, record); 
		} else {
			this.addFilter(filter);
		}
	}
	
	, modifyFilter: function(filter, i) {
		if(i != undefined) {			
			var record = this.store.getAt( i );
			Ext.apply(record.data, filter || {});
			record = this.store.getAt( i );
			this.store.fireEvent('datachanged', this.store) ;
		}
	}
		
	, addRow : function(config, i) {
		Sbi.qbe.commons.unimplementedFunction('FilterGridPanel', 'addRow');
		this.insertFilter(config, i);
	}

	, deleteFilters : function() {
		this.grid.store.removeAll();
		this.activeEditingContext = null;
		this.setWizardExpression(false);
	}
	// make another delete filters for confirm option because the previous one is called in other situations
	, deleteFiltersConfirm : function() {
		Ext.Msg.show({
			title: LN('sbi.qbe.filtergridpanel.warning.deleteAll.title'),
		   	msg: LN('sbi.qbe.filtergridpanel.warning.deleteAll.msg'),
		   	buttons: Ext.Msg.YESNO,
		   	fn: function(btn) {
				if(btn === 'yes') {
					this.deleteFilters();
				}
			},
			scope: this
		});
			}

	, deleteFilter: function(record) {
		Ext.Msg.show({
			title: LN('sbi.qbe.filtergridpanel.warning.delete.title'),
		   	msg: LN('sbi.qbe.filtergridpanel.warning.delete.msg'),
		   	buttons: Ext.Msg.YESNO,
		   	fn: function(btn) {
				if(btn === 'yes') {
					this.store.remove( record );
				}
			},
			scope: this
		});
		
	}
	
	, getFilterAt: function(i) {
		var record;
		var filter;
	
		record =  this.grid.store.getAt(i);
		filter = Ext.apply({}, record.data);
		filter.promptable = filter.promptable || false;
		
		return filter;
	}
	
	, getFilters : function() {
		var filters = [];
		var record;
		var filter;
		
		for(i = 0; i <  this.grid.store.getCount(); i++) {
			record =  this.grid.store.getAt(i);
			filter = Ext.apply({}, record.data);
			filter.promptable = filter.promptable || false;
			var rightOperandValue = [''];
			var rightOperandLastValue = [''];
			var rightOperandDefaultValue = [''];
			
			// splitting values into an array
			if (filter.rightOperandType == Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE && (filter.operator == 'BETWEEN' || filter.operator == 'NOT BETWEEN' || 
					filter.operator == 'IN' || filter.operator == 'NOT IN')) {
				rightOperandValue = filter.rightOperandValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				if (filter.rightOperandLastValue && filter.rightOperandLastValue !== null) {
					rightOperandLastValue = filter.rightOperandLastValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				}
				if (filter.rightOperandDefaultValue && filter.rightOperandDefaultValue !== null) {
					rightOperandDefaultValue = filter.rightOperandDefaultValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				}
			} else {
				rightOperandValue = [filter.rightOperandValue];
				if (filter.rightOperandLastValue && filter.rightOperandLastValue !== null) {
					rightOperandLastValue = [filter.rightOperandLastValue];
				}
				if (filter.rightOperandDefaultValue && filter.rightOperandDefaultValue !== null) {
					rightOperandDefaultValue = [filter.rightOperandDefaultValue];
				}
			}
			filter.rightOperandValue = 			rightOperandValue;
			filter.rightOperandLastValue = 		rightOperandLastValue;
			filter.rightOperandDefaultValue = 	rightOperandDefaultValue;
			
			filters.push(filter);
		}
		
		return filters;
	}
	
	, setFilters: function(filters) {
		this.deleteFilters();
		for(var i = 0; i < filters.length; i++) {
  			var filter = filters[i];
  			this.addFilter(filter);
  		}
		this.updateCounter(filters);
	}
	
	, updateCounter: function(filters) {
		var max = 0;
		for(var i = 0; i < filters.length; i++) {
  			var filterName = filters[i].filterId;
  			if (filterName.substring(0, this.filterIdPrefix.length) == this.filterIdPrefix) {
  				var suffix = filterName.substring(this.filterIdPrefix.length);
  				var number = new Number(suffix);
  				if (!isNaN(number) && number > max) {
  					max = number;
  				}
  			}
  		}
		this.idCount = max;
	}
	
	, setFiltersExpression: function(expression) {
		if(expression !== undefined) {
			var expStr = this.loadSavedExpression(expression);
			it.eng.spagobi.engines.qbe.filterwizard.setExpression( expStr, true ); 
			this.setWizardExpression(true);
		}
	}
	  	
  	, loadSavedExpression : function(expression) {
  		var str = "";
  		
  		if(expression.type == 'NODE_OP') {
  			for(var i = 0; i < expression.childNodes.length; i++) {
  				var child = expression.childNodes[i];
  				var childStr = this.loadSavedExpression(child); 
  				if(child.type == "NODE_OP") {
  					childStr = "(" + childStr + ")";
  				}
  				str += (i==0?"": " " + expression.value);
				str += " " + childStr;
  			}
  		} else {
  			str += expression.value;
  		}
  		
  		return str;
  	}
  
  	
  	, setPromptableFiltersLastValues: function(formState) {
    	for (var filterName in formState) {
    		var index = this.grid.store.findExact('filterId', filterName);
    		if (index != -1) {
    			var record = this.grid.store.getAt(index);
    			var filterValue = formState[filterName];
    			if (filterValue !== null && filterValue instanceof Array) {
    				filterValue = filterValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
    			}
    			record.set('rightOperandLastValue', filterValue);
    		}
    	}
    }
  	
  	, setPromptableFiltersDefaultValues: function(formState) {
    	for (var filterName in formState) {
    		var index = this.grid.store.findExact('filterId', filterName);
    		if (index != -1) {
    			var record = this.grid.store.getAt(index);
    			var filterValue = formState[filterName];
    			if (filterValue !== null && filterValue instanceof Array) {
    				filterValue = filterValue.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
    			}
    			record.set('rightOperandDefaultValue', filterValue);
    		}
    	}
    }
  	
  	
	
    , setFreeFiltersLastValues: function(formState) {
    	Sbi.qbe.commons.deprectadeFunction('FilterGridPanel', 'setFreeFiltersLastValues');
    	this.setPromptableFiltersLastValues(formState);
    }
	
    , setFreeFiltersDefaultValues: function(formState) {
    	Sbi.qbe.commons.deprectadeFunction('FilterGridPanel', 'setFreeFiltersDefaultValues');
    	this.setPromptableFiltersDefaultValues(formState);
    }
    
	, syncWizardExpressionWithGrid: function() {
		var exp = '';
		var store = this.grid.store;
		for(i = 0; i <  store.getCount(); i++) {
			var currRecord =  store.getAt(i);
			var prevRecord =  store.getAt(i-1);
			if(i > 0) {
				exp += ' ' + prevRecord.data.booleanConnector + ' $F{' +  currRecord.data.filterId + '}';
			} else {
				exp += '$F{' + currRecord.data.filterId + '}';
			}
		}
		it.eng.spagobi.engines.qbe.filterwizard.setExpression(exp, true);
	}
	
	, appendFilterToWizardExpression: function(record) {
		var exp = it.eng.spagobi.engines.qbe.filterwizard.getExpression();
		if(exp === undefined || exp === 'undefined') return;
		exp += ' AND $F{' + record.data.filterId + '}';
		it.eng.spagobi.engines.qbe.filterwizard.setExpression(exp, true);
	}
	
	, getFiltersExpression : function() {	
		if(!this.isWizardExpression()) {
			this.syncWizardExpressionWithGrid();
		}
		return it.eng.spagobi.engines.qbe.filterwizard.getExpressionAsObject();
	}
	
	, getFiltersExpressionAsJSON : function() {	
		if(!this.isWizardExpression()) {
			this.syncWizardExpressionWithGrid();
		}
		var json = it.eng.spagobi.engines.qbe.filterwizard.getExpressionAsJSON();
		
		return json;
	}
	
	, showWizard: function() {	
		if(this.grid.store.getCount() == 0) {
			Ext.Msg.show({
				   title:'Warning',
				   msg: 'Impossible to create a filter expression. No filters have been defined yet.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.WARNING
			});
		} else { 		
			if(!this.isWizardExpression()) {
				this.syncWizardExpressionWithGrid();
			}					
			var operands = [];			
			for(i = 0; i <  this.grid.store.getCount(); i++) {
				var tmpRec =  this.grid.store.getAt(i);
				operands[i] = {
					text: tmpRec.data.filterId,
					ttip: tmpRec.data.filterId + ': ' + tmpRec.data.filterDescription,
					type: 'operand',
					value: '$F{' +  tmpRec.data.filterId + '}'
				};
			}
			
			it.eng.spagobi.engines.qbe.filterwizard.setOperands(operands);
			it.eng.spagobi.engines.qbe.filterwizard.show();	 
			this.setWizardExpression(true);
		}
	}
	
	, setWizardExpression: function(b) {
		if(b) {
			this.wizardExpression = true;
			//alert('I will use the xpression defined into wizard');
		} else {
			this.wizardExpression = false;
			//alert('I will use inline expression');
		}
	}
	
	, isWizardExpression: function() {
		return this.wizardExpression;
	}
	
	
    // -- private methods ----------------------------------------------------------------------------------------

	, initStore: function(config) {
		
		this.Record = Ext.data.Record.create([
		   {name: 'filterId', type: 'string'}, 
		   {name: 'filterDescripion', type: 'string'},
		   
		   {name: 'promptable', type: 'bool'},
		   
		   {name: 'leftOperandValue', type: 'auto'}, // id (field unique name)
		   {name: 'leftOperandDescription', type: 'string'}, // entity(entity label) + field(field label)
		   {name: 'leftOperandLongDescription', type: 'string'}, // entity(entity label) / ... / entity(entity label) + field(field label)
		   {name: 'leftOperandType', type: 'string'}, // NEW
		   {name: 'leftOperandDefaultValue', type: 'string'}, // RESERVED FOR FUTURE USE
		   {name: 'leftOperandLastValue', type: 'string'}, // RESERVED FOR FUTURE USE
		   {name: 'leftOperandAlias', type: 'string'}, // The alias of the field
		   
		   {name: 'operator', type: 'string'},
		   
		   {name: 'rightOperandValue', type: 'auto'}, // operand
		   {name: 'rightOperandDescription', type: 'string'}, // odesc
		   {name: 'rightOperandLongDescription', type: 'string'}, // entity(entity label) / ... / entity(entity label) + field(field label)
		   {name: 'rightOperandType', type: 'string'}, // otype
		   {name: 'rightOperandDefaultValue', type: 'string'}, // defaultvalue
		   {name: 'rightOperandLastValue', type: 'string'}, // lastvalue
		   {name: 'rightOperandAlias', type: 'string'}, // The alias of the field
		   
		   {name: 'booleanConnector', type: 'string'},
		   
		   {name: 'deleteButton', type: 'bool'}
		]);
		   
		
		
		this.store = new Ext.data.SimpleStore({
			reader: new Ext.data.ArrayReader({}, this.Record)
			, fields: [] // just to keep SimpleStore constructor happy (fields are taken from record)
		});
	}
	
	, initSelectionModel: function(config) {
		if(this.sm === null) {
			this.sm = new Ext.grid.RowSelectionModel();
		}
	}
	
	, initColumnModel: function(config) {
			
			var delButtonColumn = new Ext.grid.ButtonColumn(
			Ext.apply({
		       header: LN('sbi.qbe.filtergridpanel.headers.delete')
		       , tooltip: LN('sbi.qbe.filtergridpanel.headers.delete')
		       , dataIndex: 'deleteButton'
		       , imgSrc: '../img/actions/delete.gif'
		       , clickHandler:function(e, t){
		          var index = this.grid.getView().findRowIndex(t);
		          var record = this.grid.store.getAt(index);


		          Ext.Msg.show({
		        	title: LN('sbi.qbe.filtergridpanel.warning.delete.title'),
		  		   	msg: LN('sbi.qbe.filtergridpanel.warning.delete.msg'),
		  		   	buttons: Ext.Msg.YESNO,
		  		   	fn: function(btn) {
		  				if(btn === 'yes') {
		  					this.grid.store.remove(record);
		  				}
		  			},
		  			scope: this
		  		  });
		          
		       }
			   , hideable: true
		       , hidden: (this.enableRowRemoveBtn === false)
		       , width: 50
		    }, this.columns['deleteButton'] || {}));
		    
		    
		    var booleanOptColumnEditor = new Ext.form.ComboBox({
		    	tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
		        store: this.booleanOptStore, 
		        displayField:'nome',
		        valueField: 'funzione',
		        allowBlank: false,
		        editable: true,
		        typeAhead: true, // True to populate and autoselect the remainder of the text being typed after a configurable delay
		        mode: 'local',
		        forceSelection: true, // True to restrict the selected value to one of the values in the list
		        triggerAction: 'all',
		        emptyText: LN('sbi.qbe.filtergridpanel.boperators.editor.emptymsg'),
		        selectOnFocus:true, //True to select any existing text in the field immediately on focus
		        listeners: {
		        	'change': {
     					fn: function(){
		    				var exp = it.eng.spagobi.engines.qbe.filterwizard.getExpression();
		    				if(exp === undefined || exp === 'undefined') return;
		    				Ext.Msg.show({
		    					title: LN('sbi.qbe.filtergridpanel.warning.changebolop.title'),
		    				   	msg: LN('sbi.qbe.filtergridpanel.warning.changebolop.msg'),
		    				   	buttons: Ext.Msg.YESNOCANCEL,
		    				   	fn: function(btn) {
		    						if(btn === 'yes') {
		    							this.setWizardExpression(false);   
		    						}
		    					},
		    					scope: this
		    				});
		     				     						
		     			}
     					, scope: this
     				}
		         }
		    });
		   
		    
		    var isFreeCheckColumn = new Ext.grid.CheckColumn(
		    Ext.apply({
			       header: LN('sbi.qbe.filtergridpanel.headers.isfree')
			       , tooltip: LN('sbi.qbe.filtergridpanel.headers.isfree.desc')
			       , dataIndex: 'promptable'
			       , hideable: true
				   , hidden: false
				   , width: 50
		    }, this.columns['promptable'] || {}));
		    
		    var filterOptColumnEditor = new Ext.form.ComboBox({
	           	  tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
	           	  store: this.filterOptStore, 
	           	  displayField:'nome',
	              valueField: 'funzione',
	              maxHeight: 200,
	              allowBlank: true,
	              editable: true,
	              typeAhead: true, // True to populate and autoselect the remainder of the text being typed after a configurable delay
	              mode: 'local',
	              forceSelection: true, // True to restrict the selected value to one of the values in the list
	              triggerAction: 'all',
	              emptyText: LN('sbi.qbe.filtergridpanel.foperators.editor.emptymsg'),
	              selectOnFocus: true //True to select any existing text in the field immediately on focus
	        });

		   
			
		    this.cm = new Ext.grid.ColumnModel([
		        new Ext.grid.RowNumberer(),
		        Ext.apply({ 
		            header: LN('sbi.qbe.filtergridpanel.headers.name')
		           , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.name')
		           , dataIndex: 'filterId'       
		           , editor: this.columns['filterId'].editable === true? new Ext.form.TextField({allowBlank: false}): undefined
		           , hideable: true
		           , hidden: false		 
		           , sortable: false
		        }, this.columns['filterId'] || {}), 
		        
		        Ext.apply({
			       header: LN('sbi.qbe.filtergridpanel.headers.desc')
			       , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.desc')
			       , dataIndex: 'filterDescripion'       
			       , editor: this.columns['filterDescripion'].editable === true? new Ext.form.TextField({allowBlank: false}): undefined
			       , hideable: true
			       , hidden: true		 
			       , sortable: false
			    }, this.columns['filterDescripion'] || {}),
			    
			    // == LEFT OPERAND ========================================
			    Ext.apply({
				    header: LN('sbi.qbe.filtergridpanel.headers.lodesc')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lodesc')
				    , dataIndex: 'leftOperandDescription'       
				    , editor: this.columns['leftOperandDescription'].editable === true? new Ext.form.TextField({allowBlank: false}): undefined
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				    , renderer: this.getLeftOperandTooltip
			    }, this.columns['leftOperandDescription'] || {}),
				
				Ext.apply({
				    header: LN('sbi.qbe.filtergridpanel.headers.lotype')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lotype')
				    , dataIndex: 'leftOperandType'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				 }, this.columns['leftOperandType'] || {}),
				// == OPERATOR ========================================
				Ext.apply({
					header: LN('sbi.qbe.filtergridpanel.headers.operator')
			        , tooltip: LN('sbi.qbe.filtergridpanel.headers.operator')
			        , dataIndex: 'operator'     
			        , editor: this.columns['operator'].editable === true? filterOptColumnEditor : undefined
			        , hideable: false
			        , hidden: false	
			        , sortable: false
				 }, this.columns['operator'] || {}),
				// == RIGHT OPERAND ========================================
			    Ext.apply({
				    header: LN('sbi.qbe.filtergridpanel.headers.rodesc')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rodesc')
				    , dataIndex: 'rightOperandDescription'       
				    , hideable: false
				    , hidden: false	
				    , sortable: false
				    , renderer: this.getRightOperandTooltip
			    }, this.columns['rightOperandDescription'] || {}), 
				
				Ext.apply({
				    header: LN('sbi.qbe.filtergridpanel.headers.rotype')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rotype')
				    , dataIndex: 'rightOperandType'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				}, this.columns['rightOperandType'] || {}), 
		        
		        isFreeCheckColumn, 
		        
		        Ext.apply({
		           header: LN('sbi.qbe.filtergridpanel.headers.boperator')
		           , tooltip: LN('sbi.qbe.filtergridpanel.headers.boperator.desc')
		           , dataIndex: 'booleanConnector'
		           , editor: booleanOptColumnEditor
		           , renderer: function(val){
		        		return '<span style="color:green;">' + val + '</span>';  
			       }
		           , hideable: true
		           , hidden: false	
		           , width: 55
		           , sortable: false
		           
		        }, this.columns['booleanConnector'] || {}),
		        
		        delButtonColumn
		    ]);
		    
		    this.cm.defaultSortable = true;
		    
		    this.plgins = [delButtonColumn, isFreeCheckColumn];
	}
	
	, fireKeyHandler: function(e) {
        if(e.isSpecialKey()){
            this.fireEvent("specialkey", this, e);
        } else {
        	if(this.ownerGrid.activeEditingContext) {
	        	this.ownerGrid.activeEditingContext.dirty = true;
	        }
        }
	}

	, initToolbar: function(config) {
		this.toolbar = new Ext.Toolbar({
			items: [
				{
					text: LN('sbi.qbe.filtergridpanel.buttons.text.add'),
				    tooltip: LN('sbi.qbe.filtergridpanel.buttons.tt.add'),
				    iconCls:'add',
				    hidden: (this.enableTbAddFilterBtn === false),
				    listeners: {
				    	'click': {
							fn: function() {this.addFilter();},
							scope: this
						}
				    }
				} , {
					text: LN('sbi.qbe.filtergridpanel.buttons.text.delete'),
				    tooltip: LN('sbi.qbe.filtergridpanel.buttons.tt.delete'),
				    iconCls:'remove',
				    hidden: (this.enableTbRemoveAllFilterBtn === false),
				    listeners: {
				    	'click': {
			    			fn: this.deleteFiltersConfirm,
			    			scope: this
			    		}
				    }
				}, {
					text: LN('sbi.qbe.filtergridpanel.buttons.text.wizard'),
				    tooltip: LN('sbi.qbe.filtergridpanel.buttons.tt.wizard'),
				    iconCls:'option',
				    hidden: (this.enableTbExpWizardBtn === false),
				    listeners: {
				      	'click': {
							fn: this.showWizard,
			    			scope: this
			    		}
				    }
				} /*, {
				  	text: 'Debug',
				    tooltip: 'Remove before release',
				    iconCls:'option',
				    listeners: {
				      	'click': {
							fn: function() {
								alert('filters: ' + this.getFilters().toSource() + '\n\nexpression: '+ this.getFiltersExpression().toSource() + '\n\nuseExpression: ' + this.isWizardExpression());
							},
			    			scope: this
			    		}
				    }
				} */
			]
		});
	}
	
	, initGrid: function() {
		
		var gridConf = {
		        store: this.store,
		        cm: this.cm,
		        sm : this.sm,
		        tbar: this.toolbar,
		        plugins: this.plgins,
		        clicksToEdit:1,	        
		        style: this.gridStyle,
		        frame: true,
		        height: this.gridHeight,
		        border:true,  
		        collapsible:false,
		        layout: 'fit',
		        viewConfig: {
		            forceFit:true
		        },
		        enableDragDrop:true,
	    		ddGroup: 'gridDDGroup',
		        iconCls:'icon-grid'        
		}
		
		if(this.gridTitle != null) {
			gridConf.title = this.gridTitle;
		}
		
	    this.grid = new Ext.grid.EditorGridPanel(gridConf);
	    this.grid.type = this.type;
	}
	
	, initGridListeners: function(config) {
		this.grid.on("mouseover", function(e, t){
	    	var row;
	        this.targetRow = t;
	        
	         this.targetRowIndex = this.getView().findRowIndex(t);
    		 this.targetColIndex = this.getView().findCellIndex(t);
	        
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
	    	  
		  		Ext.Msg.show({
	        	title: LN('sbi.qbe.filtergridpanel.warning.delete.title'),
			   	msg: LN('sbi.qbe.filtergridpanel.warning.delete.msg'),
			   	buttons: Ext.Msg.YESNO,
			   	fn: function(btn) {
					if(btn === 'yes') {
						var sm=this.grid.getSelectionModel();
				        var ds = this.grid.getStore();
				        var rows=sm.getSelections();
						for (i = 0; i < rows.length; i++) {
							this.store.remove( ds.getById(rows[i].id) );
					    }
						this.activeEditingContext = null;					
					}
				},
				scope: this
			});
	        
	        
	        
	        
	      }      
	    }, this);
	    
	    //this.grid.on('beforeedit', this.onBeforeEdit, this);
	    this.grid.on('beforeedit', function(e) {
	    	
	    	var date = new Date();
	    	var curDate = null;
	    	var millis = 100;

	    	do { curDate = new Date(); }
	    	while(curDate-date < millis);
	    	
	    	this.onBeforeEdit(e);
	    	//this.onBeforeEdit.defer(500, this, [e]);
	        //alert('AFTER onBeforeEdit');
	    }, this);
	    
	    this.grid.store.on('remove', function(e){
	    	this.setWizardExpression(false);
	    }, this);
	    
	    this.grid.store.on('add', function(store, records, index){
	    	for(var i = 0; i < records.length; i++){
	    		this.appendFilterToWizardExpression(records[i]);
	    	}
	    }, this);
	}
	
	, onBeforeEdit: function(e) {
		/*
		 	grid - This grid
			record - The record being edited
			field - The field name being edited
			value - The value for the field being edited.
			row - The grid row index
			column - The grid column index
			cancel - Set this to true to cancel the edit or return false from your handler.
		 */
		var filter;
		if(this.activeEditingContext) {
			//alert("a "+this.activeEditingContext.row);
			filter = this.getFilterAt(this.activeEditingContext.row);
			if(this.activeEditingContext.dataIndex === 'leftOperandDescription') {
				if(this.activeEditingContext.dirty === true){
					this.modifyFilter({
						leftOperandValue: filter.leftOperandDescription, 
						leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, 
						leftOperandLongDescription: null
					}, this.activeEditingContext.row);
				}				
			} else if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
				if(this.activeEditingContext.dirty === true){
					this.modifyFilter({
						rightOperandValue: filter.rightOperandDescription, 
						rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, 
						rightOperandLongDescription: null
					}, this.activeEditingContext.row);
				}				
			}
		}
		
		this.activeEditingContext = Ext.apply({}, e);
		var col = this.activeEditingContext.column;
		var row = this.activeEditingContext.row;		
		var dataIndex = this.activeEditingContext.grid.getColumnModel().getDataIndex( col );
		this.activeEditingContext.dataIndex = dataIndex;
		this.activeEditingContext.dirty = false;
		//alert("b "+row);
		filter = this.getFilterAt(row);
		//alert(filter.leftOperandValue.expression);
		if(dataIndex === 'leftOperandDescription' || dataIndex === 'rightOperandDescription') {
			var editor;
			if(this.parentQuery !== null) {
				if(dataIndex === 'rightOperandDescription'){
					editor = this.createMultiButtonEditor();
				}else{
					editor = this.createParentFieldEditor();
				}
			} else if(dataIndex === 'rightOperandDescription' 
				&& (filter.leftOperandType == Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD 
						|| filter.leftOperandType ==  Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD) 
				&& filter.leftOperandValue != null 
				/*&& filter.leftOperandValue.expression==null */) {
				editor = this.createLookupFieldEditor();
			}  else {
				editor = this.createTextEditor();
			}
				
			this.grid.colModel.setEditor(col, editor);
		}	
		
	}
	
	, createMultiButtonEditor: function () {
		var multiButtonEditor = new Sbi.widgets.TriggerFieldMultiButton({
	     		allowBlank: true
	    });
    
	    multiButtonEditor.ownerGrid = this;
	    multiButtonEditor.fireKey = this.fireKeyHandler;
	    
	    multiButtonEditor.onTrigger1Click = this.openValuesForQbeFilterLookup.createDelegate(this);
	    multiButtonEditor.onTrigger2Click = this.onOpenValueEditor.createDelegate(this);  	
	    
	    multiButtonEditor.on('change', function(f, newValue, oldValue) {
	    	if (this.activeEditingContext) {
	    		if (this.activeEditingContext.dataIndex === 'rightOperandDescription') {
	    			this.modifyFilter({
		    				rightOperandValue: newValue, 
		    				rightOperandDescription: newValue,
		    				rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, 
		    				rightOperandLongDescription: null
	    				}, 
	    				this.activeEditingContext.row);
	    		}
	    	}		    	
	    }, this);
	    
	    return multiButtonEditor;
	}
	
	, createParentFieldEditor: function () {
	    var parentFieldEditor = new Ext.form.TriggerField({
            allowBlank: true
            , triggerClass: 'trigger-up'

	    });
	    parentFieldEditor.onTriggerClick = this.onOpenValueEditor.createDelegate(this);
	    parentFieldEditor.on('change', function(f, newValue, oldValue){
	    	if(this.activeEditingContext) {
	    		if(this.activeEditingContext.dataIndex === 'leftOperandDescription') {
	    			this.modifyFilter({leftOperandValue: newValue, leftOperandDescription: newValue, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, leftOperandLongDescription: null}, this.activeEditingContext.row);
	    		} else if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
	    			this.modifyFilter({rightOperandValue: newValue, rightOperandDescription: newValue, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, rightOperandLongDescription: null}, this.activeEditingContext.row);
	    		}
	    	}		    	
	    }, this);
	    
	    return parentFieldEditor;
	}
	
	, createLookupFieldEditor: function () {
	    var lookupFieldEditor = new Ext.form.TriggerField({
            allowBlank: true
            , triggerClass: 'x-form-search-trigger'
	    });
	    
		lookupFieldEditor.ownerGrid = this;
		lookupFieldEditor.fireKey = this.fireKeyHandler;
	    
	    lookupFieldEditor.onTriggerClick = this.openValuesForQbeFilterLookup.createDelegate(this);
	    lookupFieldEditor.on('change', function(f, newValue, oldValue){
	    	if(this.activeEditingContext) {
	    		if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
	    			this.modifyFilter({rightOperandValue: newValue, rightOperandDescription: newValue, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, rightOperandLongDescription: null}, this.activeEditingContext.row);
	    		}
	    	}		    	
	    }, this);
	    
	    return lookupFieldEditor;
	}
	
	, createTextEditor: function () {
	    var textEditor = new Ext.form.TextField({
            allowBlank: true
	    });
	    
	    textEditor.ownerGrid = this;
	    textEditor.fireKey = this.fireKeyHandler;
	    
	    textEditor.on('change', function(f, newValue, oldValue){
	    	if(this.activeEditingContext) {
	    		if(this.activeEditingContext.dataIndex === 'leftOperandDescription') {
	    			this.modifyFilter({leftOperandValue: newValue, leftOperandDescription: newValue, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, leftOperandLongDescription: null}, this.activeEditingContext.row);
	    		} else if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
	    			this.modifyFilter({rightOperandValue: newValue, rightOperandDescription: newValue, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE, rightOperandLongDescription: null}, this.activeEditingContext.row);
	    		} else {
	    			//alert('ONCHANGE: ' + this.activeEditingContext.dataIndex);
	    		}
	    	}		    	
	    }, this);
	    
	    return textEditor;
	}
	
	, onOpenValueEditor: function(e) {
		if(this.operandChooserWindow === undefined) {
			this.operandChooserWindow = new Sbi.qbe.OperandChooserWindow();
			this.operandChooserWindow.on('applyselection', function(win, node) {
				//var r = this.activeEditingContext.record;
				var filter;
				if(this.activeEditingContext.dataIndex === 'leftOperandDescription') {
					filter = {
						leftOperandType: 'Parent Field Content'
						, leftOperandDescription: this.parentQuery.id  + ' : ' +  node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field
						, leftOperandValue: this.parentQuery.id + ' ' + node.id
						, leftOperandLongDescription: 'Query ' + this.parentQuery.id + ', ' + node.attributes.attributes.longDescription
					}
					this.modifyFilter(filter, this.activeEditingContext.row);
				} else if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
					filter = {
						rightOperandType: 'Parent Field Content'
						, rightOperandDescription: this.parentQuery.id  + ' : ' +  node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field
						, rightOperandValue: this.parentQuery.id + ' ' + node.id
						, rightOperandLongDescription: 'Query ' + this.parentQuery.id + ', ' + node.attributes.attributes.longDescription
					}
					this.modifyFilter(filter, this.activeEditingContext.row);
				}
				//this.store.fireEvent('datachanged', this.store) ;
				//this.activeEditingContext = null;
			}, this);
			
			this.operandChooserWindow.on('applyselection', function(win, node) {
				this.activeEditingContext = null;
			}, this);
		}
		this.grid.stopEditing();
		this.operandChooserWindow.setParentQuery(this.parentQuery);
		this.operandChooserWindow.show();
	}
	
	, createStore: function() {
		var record = this.activeEditingContext.grid.store.getAt(this.activeEditingContext.row);
		
		var operandType = record.get('leftOperandType');
		var operandValue = record.get('leftOperandValue');
		
		var storeUrl = this.services['getValuesForQbeFilterLookupService'];
		var params = {};
		
		if(operandType === Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD) {
			params.fieldDescriptor = Ext.util.JSON.encode(operandValue);
		} else {
			storeUrl += '&ENTITY_ID=' + operandValue;
		}
		
		
		var store;	
		store = new Ext.data.JsonStore({
			url: storeUrl
			// does not work. SI do not why. As workaroud I inject my params befor store load (see below)
			//, baseParams : params
		});
	
		
		store.on('beforeload', function(store, options) {
			options =  Ext.apply(options.params, params);
		});
		 
		store.on('loadexception', function(store, options, response, e) {
			var msg = '';
			var content = Ext.util.JSON.decode( response.responseText );
  			if(content !== undefined) {
  				msg += content.serviceName + ' : ' + content.message;
  			} else {
  				msg += 'Server response is empty';
  			}
	
			Sbi.exception.ExceptionHandler.showErrorMessage(msg, response.statusText);
		});
		
		return store;	
	}
	
	, openValuesForQbeFilterLookup: function(e) {
			this.grid.stopEditing();
			var store = this.createStore();
			
			var baseConfig = {
		       store: store
		     , singleSelect: false
		     , valuesSeparator: Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator
			};
			
			this.lookupField = new Sbi.widgets.FilterLookupField(baseConfig);
			var record = this.activeEditingContext.grid.store.getAt(this.activeEditingContext.row);
		    var valuesToload = record.get('rightOperandValue').split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
		    // an empty string isn't an actual value
		    if (valuesToload.length == 1 && valuesToload[0] == '') {
		    	valuesToload = [];
		    }
			this.lookupField.onLookUp(valuesToload);
			this.lookupField.on('selectionmade', function(xselection) {
				var filter;
				if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
					filter = {
						rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
						, rightOperandDescription: xselection.Values.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator)
						, rightOperandValue: xselection.Values.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator)
						, rightOperandLongDescription: xselection.Values.join(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator)
					}
					this.modifyFilter(filter, this.activeEditingContext.row);
				}
			}, this);
			
	}
	
	, getLeftOperandTooltip: function (value, metadata, record) {
	 	var tooltipString = record.data.leftOperandLongDescription;
	 	if (tooltipString !== undefined && tooltipString != null) {
	 		metadata.attr = ' ext:qtip="'  + tooltipString + '"';
	 	}
	 	//if the left operand is a datamart field we show the long description (in this way we show also the father entity)
	 	//otherwise we show the leftOperandDescription
	 	if (record.data.leftOperandLongDescription !== null && record.data.leftOperandLongDescription !== ''){
	 		return record.data.leftOperandLongDescription;
	 	}
	 	return value;
	}
	
	, getRightOperandTooltip: function (value, metadata, record) {
	 	var tooltipString = record.data.rightOperandLongDescription;
	 	if (tooltipString !== undefined && tooltipString != null) {
	 		metadata.attr = ' ext:qtip="'  + tooltipString + '"';
	 	}
	 	return value;
	}
	
});