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

Sbi.qbe.HavingGridPanel = function(config) {
	
	
	var c = Ext.apply(Sbi.settings.qbe.havingGridPanel, config || {});
	Ext.apply(this, c);
	this.services = new Array();
	
	this.filterIdPrefix = 'having'; 
	
	this.idCount = 0;
	this.initStore(c);
	this.initSelectionModel(c);
	this.initColumnModel(c);
	this.initToolbar(c);
	this.initGrid(c);
	this.initGridListeners(c);
	
	c = Ext.apply(c, {
		border: true,
		layout: 'fit',
		//autoWidth: true,
		//width: 'auto',
		//width: 1000,
		items: [this.grid]
	});
	
	// constructor
    Sbi.qbe.HavingGridPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.qbe.HavingGridPanel, Ext.Panel, {
    
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
	
	, type: 'havinggrid'
	

	// static members
	, leftOperandAggregationFunctionsStore:  new Ext.data.SimpleStore({
		 fields: ['funzione', 'nome', 'descrizione'],
		 data: Sbi.constants.qbe.HAVING_CLAUSE_AGGREGATION_FUNCTION
	     /*
		 data : [
	        ['NONE', LN('sbi.qbe.selectgridpanel.aggfunc.name.none'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.none')],
	        ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
	        ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
	        ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
	        ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
	        ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
	        ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
	     ] 
	     */
	})

	, rightOperandAggregationFunctionsStore:  new Ext.data.SimpleStore({
		fields: ['funzione', 'nome', 'descrizione'],
		data: Sbi.constants.qbe.HAVING_CLAUSE_AGGREGATION_FUNCTION
	     /*
	    data : [
	       ['NONE', LN('sbi.qbe.selectgridpanel.aggfunc.name.none'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.none')],
	       ['SUM', LN('sbi.qbe.selectgridpanel.aggfunc.name.sum'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.sum')],
	       ['AVG', LN('sbi.qbe.selectgridpanel.aggfunc.name.avg'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.avg')],
	       ['MAX', LN('sbi.qbe.selectgridpanel.aggfunc.name.max'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.max')],
	       ['MIN', LN('sbi.qbe.selectgridpanel.aggfunc.name.min'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.min')],
	       ['COUNT', LN('sbi.qbe.selectgridpanel.aggfunc.name.count'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.count')],
	       ['COUNT_DISTINCT', LN('sbi.qbe.selectgridpanel.aggfunc.name.countdistinct'), LN('sbi.qbe.selectgridpanel.aggfunc.desc.countdistinct')]
	    ] 
	    */
	})
		
	, booleanOptStore: new Ext.data.SimpleStore({
        fields: ['funzione', 'nome', 'descrizione'],
        data : [
                ['AND', LN('sbi.qbe.filtergridpanel.boperators.name.and'), LN('sbi.qbe.filtergridpanel.boperators.desc.and')],
                ['OR', LN('sbi.qbe.filtergridpanel.boperators.name.or'), LN('sbi.qbe.filtergridpanel.boperators.desc.or')]
        ]
    })

	, filterOptStore: new Ext.data.SimpleStore({
	    fields: ['funzione', 'nome', 'descrizione'],
	    data: Sbi.constants.qbe.HAVING_CLAUSE_COMPARISON_FUNCTIONS
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
			
			, leftOperandAggregator: ''
			, leftOperandValue: ''
			, leftOperandDescription: ''
			, leftOperandLongDescription: null
			, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
			, leftOperandDefaultValue: null
			, leftOperandLastValue: null
			
			, operator: ''
				
			, rightOperandAggregator: ''
			, rightOperandValue: ''
			, rightOperandDescription: ''
			, rightOperandLongDescription: null
			, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE
			, rightOperandDefaultValue: null
			, rightOperandLastValue: null
			
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
		}else{
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
	
	, deleteFilters : function() {
		this.grid.store.removeAll();
	}
	
	, getFilterAt: function(i) {
		var record;
		var filter;
		
		record =  this.grid.store.getAt(i);
		if(!record) alert("No record at " + i);
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
			// splitting values into an array
			if (filter.rightOperandType == Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE && (filter.operator == 'BETWEEN' || filter.operator == 'NOT BETWEEN' || 
					filter.operator == 'IN' || filter.operator == 'NOT IN')) {
				filter.rightOperandValue = filter.rightOperandValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				if (filter.rightOperandLastValue && filter.rightOperandLastValue != null) {
					filter.rightOperandLastValue = filter.rightOperandLastValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				}
				if (filter.rightOperandDefaultValue && filter.rightOperandDefaultValue != null) {
					filter.rightOperandDefaultValue = filter.rightOperandDefaultValue.split(Sbi.settings.qbe.filterGridPanel.lookupValuesSeparator);
				}
			} else {
				filter.rightOperandValue = [filter.rightOperandValue];
				if (filter.rightOperandLastValue && filter.rightOperandLastValue != null) {
					filter.rightOperandLastValue = [filter.rightOperandLastValue];
				}
				if (filter.rightOperandDefaultValue && filter.rightOperandDefaultValue != null) {
					filter.rightOperandDefaultValue = [filter.rightOperandDefaultValue];
				}
			}
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
  		//alert(formState.toSource());
    	for (var filterName in formState) {
    		var index = this.grid.store.findExact('filterId', filterName);
    		//alert(index);
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

	
    // -- private methods ----------------------------------------------------------------------------------------

	, initStore: function(config) {
		
		this.Record = Ext.data.Record.create([
		   {name: 'filterId', type: 'string'}, 
		   {name: 'filterDescripion', type: 'string'},
		   
		   {name: 'promptable', type: 'bool'},
		   
		   {name: 'leftOperandAggregator', type: 'string'},
		   {name: 'leftOperandValue', type: 'auto'}, // id (field unique name)
		   {name: 'leftOperandDescription', type: 'string'}, // entity(entity label) + field(field label)
		   {name: 'leftOperandLongDescription', type: 'string'}, // entity(entity label) / ... / entity(entity label) + field(field label)
		   {name: 'leftOperandType', type: 'string'}, // NEW
		   {name: 'leftOperandDefaultValue', type: 'string'}, // RESERVED FOR FUTURE USE
		   {name: 'leftOperandLastValue', type: 'string'}, // RESERVED FOR FUTURE USE
		   
		   {name: 'operator', type: 'string'},
		   
		   {name: 'rightOperandAggregator', type: 'string'},
		   {name: 'rightOperandValue', type: 'auto'}, // operand
		   {name: 'rightOperandDescription', type: 'string'}, // odesc
		   {name: 'rightOperandLongDescription', type: 'string'}, // entity(entity label) / ... / entity(entity label) + field(field label)
		   {name: 'rightOperandType', type: 'string'}, // otype
		   {name: 'rightOperandDefaultValue', type: 'string'}, // defaultvalue
		   {name: 'rightOperandLastValue', type: 'string'}, // lastvalue
		   
		   {name: 'booleanConnector', type: 'string'},
		   
		   {name: 'deleteButton', type: 'bool'}
		]);
		   
		
		this.store = new Ext.data.SimpleStore({
			reader: new Ext.data.ArrayReader({}, this.Record)
			, fields: [] // just to keep SimpleStore constructor happy (fields are taken from record)
		});
	}
	
	, initSelectionModel: function(config) {
		this.sm = new Ext.grid.RowSelectionModel();
	}
	
	, initColumnModel: function(config) {
			
			var delButtonColumn = new Ext.grid.ButtonColumn({
		       header: LN('sbi.qbe.filtergridpanel.headers.delete')
		       , tooltip: LN('sbi.qbe.filtergridpanel.headers.delete')
		       , dataIndex: 'deleteButton'
		       , imgSrc: '../img/actions/delete.gif'
		       , clickHandler:function(e, t){
		          var index = this.grid.getView().findRowIndex(t);
		          var record = this.grid.store.getAt(index);
		          this.grid.store.remove(record);
		       }
			   , hideable: true
		       , hidden: true
		       , width: 50
		    });
		    
		    
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
		     				this.setWizardExpression(false);        						
		     			}
     					, scope: this
     				}
		         }
		    });
		    
		    var isFreeCheckColumn = new Ext.grid.CheckColumn({
			       header: LN('sbi.qbe.filtergridpanel.headers.isfree')
			       , tooltip: LN('sbi.qbe.filtergridpanel.headers.isfree.desc')
			       , dataIndex: 'promptable'
			       , hideable: true
				   , hidden: false
				   , width: 50
			});
		    
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
		    
		    var leftOperandAggregatorEditor = new Ext.form.ComboBox({
		         tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
		         allowBlank: true,
		         editable:false,
		         store: this.leftOperandAggregationFunctionsStore,
		         displayField:'nome',
		         valueField:'funzione',
		         typeAhead: true,
		         mode: 'local',
		         triggerAction: 'all',
		         autocomplete: 'off',
		         emptyText: LN('sbi.qbe.selectgridpanel.aggfunc.editor.emptymsg'),
		         selectOnFocus:true
	        });
		    
		    var rightOperandAggregatorEditor = new Ext.form.ComboBox({
		         tpl: '<tpl for="."><div ext:qtip="{nome}: {descrizione}" class="x-combo-list-item">{nome}</div></tpl>',	
		         allowBlank: true,
		         editable:false,
		         store: this.rightOperandAggregationFunctionsStore,
		         displayField:'nome',
		         valueField:'funzione',
		         typeAhead: true,
		         mode: 'local',
		         triggerAction: 'all',
		         autocomplete: 'off',
		         emptyText: LN('sbi.qbe.selectgridpanel.aggfunc.editor.emptymsg'),
		         selectOnFocus:true
	        });
		   
		    this.cm = new Ext.grid.ColumnModel([
		        new Ext.grid.RowNumberer(),
		        { 
		            header: LN('sbi.qbe.filtergridpanel.headers.name')
		           , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.name')
		           , dataIndex: 'filterId'       
		           , editor: new Ext.form.TextField({allowBlank: false})
		           , hideable: true
		           , hidden: false		 
		           , sortable: false
		           , width: 70
		        }, {
			       header: LN('sbi.qbe.filtergridpanel.headers.desc')
			       , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.desc')
			       , dataIndex: 'filterDescripion'       
			       , editor: new Ext.form.TextField({allowBlank: false})
			       , hideable: true
			       , hidden: true		 
			       , sortable: false
			    },
			    
			    // == LEFT OPERAND ========================================
			    /*
			    {
				    header: LN('sbi.qbe.filtergridpanel.headers.loval')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.loval')
				    , dataIndex: 'leftOperandValue'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				}, */
			    {
				    header: LN('sbi.qbe.selectgridpanel.headers.function')
				    , dataIndex: 'leftOperandAggregator'       
				    , editor: leftOperandAggregatorEditor
				    , hideable: true
				    , hidden: false		 
				    , sortable: false
				    , width: 60
				}, {
				    header: LN('sbi.qbe.filtergridpanel.headers.lodesc')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lodesc')
				    , dataIndex: 'leftOperandDescription'       
				    , editor: new Ext.form.TextField({allowBlank: false})
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				    , renderer: this.getLeftOperandTooltip
				}, {
				    header: LN('sbi.qbe.filtergridpanel.headers.lotype')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lotype')
				    , dataIndex: 'leftOperandType'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				}, /*{
				    header: LN('sbi.qbe.filtergridpanel.headers.lodef')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lodef')
				    , dataIndex: 'leftOperandDefaultValue'       
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				},	{
				    header: LN('sbi.qbe.filtergridpanel.headers.lolast')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.lolast')
				    , dataIndex: 'leftOperandLastValue'       
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				}, */
				// == OPERATOR ========================================
				{
					header: LN('sbi.qbe.filtergridpanel.headers.operator')
			        , tooltip: LN('sbi.qbe.filtergridpanel.headers.operator')
			        , dataIndex: 'operator'     
			        , editor: filterOptColumnEditor
			        , hideable: false
			        , hidden: false	
			        , sortable: false
			    },
				// == RIGHT OPERAND ========================================
				/*
			    {
				    header: LN('sbi.qbe.filtergridpanel.headers.roval')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.roval')
				    , dataIndex: 'rightOperandValue'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				}, */
			    {
				    header: LN('sbi.qbe.selectgridpanel.headers.function')
				    , dataIndex: 'rightOperandAggregator'       
				    , editor: rightOperandAggregatorEditor
				    , hideable: true
				    , hidden: false		 
				    , sortable: false
				    , width: 60
				}, {
				    header: LN('sbi.qbe.filtergridpanel.headers.rodesc')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rodesc')
				    , dataIndex: 'rightOperandDescription'
				    , editor: new Ext.form.TextField({allowBlank: false})
				    , hideable: false
				    , hidden: false	
				    , sortable: false
				    , renderer: this.getRightOperandTooltip
				}, {
				    header: LN('sbi.qbe.filtergridpanel.headers.rotype')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rotype')
				    , dataIndex: 'rightOperandType'       
				    , hideable: true
				    , hidden: true		 
				    , sortable: false
				}, /*{
				    header: LN('sbi.qbe.filtergridpanel.headers.rodef')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rodef')
				    , dataIndex: 'rightOperandDefaultValue'       
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				},	{
				    header: LN('sbi.qbe.filtergridpanel.headers.rolast')
				    , tooltip: LN('sbi.qbe.filtergridpanel.tooltip.rolast')
				    , dataIndex: 'rightOperandLastValue'       
				    , hideable: false
				    , hidden: false		 
				    , sortable: false
				}, */
		        
		        isFreeCheckColumn, 
		        {
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
		           
		        }
		        , delButtonColumn
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
				    listeners: {
				    	'click': {
			    			fn: this.deleteFilters,
			    			scope: this
			    		}
				    }
				}
			]
		});
	}
	
	, initGrid: function(config) {
		 // create the Grid
	    this.grid = new Ext.grid.EditorGridPanel({
	    	title: LN('sbi.qbe.havinggridpanel.title'),   
	        store: this.store,
	        cm: this.cm,
	        sm : this.sm,
	        tbar: this.toolbar,
	        plugins: this.plgins,
	        clicksToEdit:1,	        
	        style:'padding:10px',
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
	    });
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
	      if (e.keyCode === 46) {
	        var sm = this.grid.getSelectionModel();
	        var ds = this.grid.getStore();
	        var rows = sm.getSelections();
	        for (i = 0; i < rows.length; i++) {
	          this.store.remove( ds.getById(rows[i].id) );
	        }
	        // remove active editing context
	        delete this.activeEditingContext;
	      }      
	    }, this);
	    
	    this.grid.on('beforeedit', this.onBeforeEdit, this);
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
		if (this.activeEditingContext) {
			var filter = this.getFilterAt(this.activeEditingContext.row);
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
		
		if(dataIndex === 'leftOperandDescription' || dataIndex === 'rightOperandDescription') {
			var editor = this.createTextEditor();
			this.grid.colModel.setEditor(col, editor);
		}			
	}
	
	, createTextEditor: function() {
	    var textEditor = new Ext.form.TextField({
            allowBlank: true
	    });
	    
	    textEditor.ownerGrid = this;
	    textEditor.fireKey = this.fireKeyHandler;
	    
	    textEditor.on('change', function(f, newValue, oldValue){
	    	if(this.activeEditingContext) {
	    		if(this.activeEditingContext.dataIndex === 'leftOperandDescription') {
	    			this.modifyFilter({leftOperandValue: newValue, leftOperandDescription: newValue, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE}, this.activeEditingContext.row);
	    		} else if(this.activeEditingContext.dataIndex === 'rightOperandDescription') {
	    			this.modifyFilter({rightOperandValue: newValue, rightOperandDescription: newValue, rightOperandType: Sbi.constants.qbe.OPERAND_TYPE_STATIC_VALUE}, this.activeEditingContext.row);
	    		}
	    	}		    	
	    }, this);
	    
	    return textEditor;
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