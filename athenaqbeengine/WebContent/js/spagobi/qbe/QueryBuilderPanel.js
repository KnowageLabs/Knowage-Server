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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.QueryBuilderPanel = function(config) {
	
	var defaultSettings = {
		//title: LN('sbi.qbe.queryeditor.title'),
		frame: false, 
		border: false,
		collapseQueryCataloguePanel: true,
		enableTreeContextMenu: true,
		enableTreeToolbar: true,
		enableTreeTbSaveBtn: true,
		enableTreeTbPinBtn: true,
		enableTreeTbUnpinBtn: true,
		enableQueryTbSaveBtn: true,
		enableQueryTbSaveViewBtn: false,
		enableQueryTbValidateBtn: false,
		enableCatalogueTbDeleteBtn: true,
		enableCatalogueTbAddBtn: false,
		enableCatalogueTbInsertBtn: true,
		queryWidowTabs: ['sql','jpql']
  	};
	
	if(Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.queryBuilderPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.queryBuilderPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
		
	this.services = this.services || new Array();	
	this.services['saveQuery'] = this.services['saveQuery'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_QUERY_ACTION'
		, baseParams: new Object()
	});
	this.services['validateQuery'] = this.services['validateQuery'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'VALIDATE_QUERY_ACTION'
		, baseParams: new Object()
	});
	this.services['saveView'] = this.services['saveView'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'CREATE_VIEW_ACTION'
		, baseParams: new Object()
	});
	this.services['saveTree'] = this.services['saveTree'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_TREE_ACTION'
		, baseParams: params
	});
	this.services['getMeta'] = this.services['getMeta'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ANALYSIS_META_ACTION'
		, baseParams: params
	});
	this.services['getSQLQuery'] = this.services['getSQLQuery'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_SQL_QUERY_ACTION'
		, baseParams: params
	});
	/*
	this.services['getAmbiguousFields'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_AMBIGUOUS_FIELDS_ACTION'
		, baseParams: params
	});
	*/
		
	this.addEvents('save');
		
	this.initWestRegionPanel(c.westConfig || {});
	this.initCenterRegionPanel(c.centerConfig || {});
	this.initEastRegionPanel(c.eastConfig || {});
	
	c = Ext.apply(c, {
      	layout: 'border',  
		/*
		 * work-around: when executing the following operations:
		 * 1. executing a QBE document inside SpagoBI server
		 * 2. building the query with query builder panel
		 * 3. executing the query
		 * 4. expanding SpagoBI parameters panel
		 * 5. returning to query builder panel
		 * then:
		 * - using IE: the west region and the east region dimensions were recalculated and new width was too narrow;
		 * - using FF: the where clause and having close panels' dimensions were recalculated and new height was too little.
		 * Using 'offsets' hideMode, the panel's dimensions are not re-calculated.
		 * TODO: try to remove it when upgrading Ext library
		 */
        hideMode: 'offsets', //!Ext.isIE ? 'display' : 'offsets',
      	items: [this.westRegionPanel, this.centerRegionPanel, this.eastRegionPanel]
	});

	// constructor
    Sbi.qbe.QueryBuilderPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.qbe.QueryBuilderPanel, Ext.Panel, {
    
    services: null
    
    , eastRegionPanel: null
    , centerRegionPanel: null
    , westRegionPanel: null
    
    , qbeStructurePanel: null
    , queryCataloguePanel: null
    , currentDataMartStructurePanel: null
    , selectGridPanel: null
    , filterGridPanel: null
    , havingGridPanel: null
    
    , saveQueryWindow: null
    , saveViewWindow: null
    , queryWidowTabs: null
   
    // --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
    
    , setQuery: function(query) {
		
		var parentQuery = this.queryCataloguePanel.getParentQuery(query.id);
		this.filterGridPanel.query = query;
		this.filterGridPanel.parentQuery = parentQuery;
		
		this.selectGridPanel.setFields(query.fields);
		this.selectGridPanel.distinctCheckBox.setValue(query.distinct);
		this.filterGridPanel.setFilters(query.filters);
		
		this.filterGridPanel.setFiltersExpression(query.expression);
		if(query.isNestedExpression && query.isNestedExpression === true) {
			this.filterGridPanel.setWizardExpression(true);		
		} else {
			this.filterGridPanel.setWizardExpression(false);	
		}
		
		this.havingGridPanel.setFilters(query.havings);
	}
    
    , getQuery: function(asObject) {
    	var query = {};
    	
    	if(asObject) {
    		var selectedQuery = this.queryCataloguePanel.getSelectedQuery();
    		if(selectedQuery){
    			query.id = selectedQuery.id;
    			query.name = selectedQuery.name;
    			query.description = selectedQuery.description;
    		}
    		query.fields = this.selectGridPanel.getFields();
    		query.distinct = this.selectGridPanel.distinctCheckBox.getValue();
    		query.filters = this.filterGridPanel.getFilters();
    		
    		query.expression = this.filterGridPanel.getFiltersExpression();
    		query.isNestedExpression = this.filterGridPanel.isWizardExpression();
    		query.havings = this.havingGridPanel.getFilters();
    	} else {		
    		//alert("get query as string is deprecated");
			query.fileds =  this.selectGridPanel.getRowsAsJSONParams();
			query.distinct = this.selectGridPanel.distinctCheckBox.getValue();
			query.filters = this.filterGridPanel.getRowsAsJSONParams();
			query.expression = this.filterGridPanel.getFiltersExpressionAsJSON();
    	}
		return query;
	}
    
    , getQueries: function() {
    	var queries = this.queryCataloguePanel.getQueries();
		return queries;
	}
    
    
    /**
	 * apply all performed changes to the selected query 
	 */
	, applyChanges: function() {
		var query = this.queryCataloguePanel.getSelectedQuery();
		if(query) {
			this.queryCataloguePanel.setQuery(query.id, this.getQuery(true) );
		}
	}
	
	/**
	 * undo all performed changes reverting to the previous status of the selected query 
	 */
	, resetChanges: function() {
		var query = this.queryCataloguePanel.getSelectedQuery();
		this.setQuery(query);
	}

    , showSaveQueryWindow: function(){
        var nameMeta = "";
        var descriptionMeta = "";
        var scopeMeta = "";
        
	    if(this.saveQueryWindow === null) {
	    	this.saveQueryWindow = new Sbi.widgets.SaveWindow({
	    		title: LN('sbi.qbe.queryeditor.savequery')
	    		, descriptionFieldVisible: true
	    		, scopeFieldVisible: true
	    		, metadataFieldVisible: true
	    		
	    	});
	    	
		      //getting meta informations 
	       	Ext.Ajax.request({
				url:  this.services['getMeta'],
				success: function(response, options) {
   					if(response !== undefined && response.responseText !== undefined ) {
	      			    var content = Ext.util.JSON.decode( response.responseText );
	      			  
		      			if (content !== undefined) {      		      					                   			 
		      				nameMeta = content.name;                      
		      				descriptionMeta = content.description; 
		      				scopeMeta = (content.scope);    		                   
		      				this.saveQueryWindow.setFormState({ 
		      					name: nameMeta
	            	    		, description: descriptionMeta
	            	    		, scope: scopeMeta
	            	    	});   				      			
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
       			},
       			scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure		
	       	});  
	    	
	    	this.saveQueryWindow.on('save', function(win, formState){this.saveQuery(formState);}, this);
		}
	    this.saveQueryWindow.show();
	}
    
    , saveQuery: function(meta) {
    	this.applyChanges();
    	this.fireEvent('save', meta);
    }
   	
	, saveView: function(meta) {
		var url = this.services['saveView'];			
	    url += '&viewName=' + meta.name;
	       
       	Ext.Ajax.request({
				url:  url,
				callback: function(success, response, options) {
       				if(success) {
       					this.currentDataMartStructurePanel.load();
       				}
       			},
       			scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure		
       	});   
       	
	}
    
	, showSaveQueryWarning: function() {
		Ext.Msg.confirm('WARNING',LN('sbi.qbe.queryeditor.msgwarning'), function(btn) {
			if (btn == 'yes') {
				this.showSaveQueryWindow();
			}
		}, this);
	}

	
	
	, showSaveViewWindow: function() {
		if(this.saveViewWindow === null) {
			this.saveViewWindow = new Sbi.widgets.SaveWindow({
				title:LN('sbi.qbe.queryeditor.saveqasview')
	    		, descriptionFieldVisible: false
	    		, scopeFieldVisible: false
	    	});
			this.saveViewWindow.on('save', function(win, formState){this.saveView(formState);}, this);
		}
		this.saveViewWindow.show();
	}
	
	, getSQLQuery: function(callbackFn, scope, additionaParams) {
		var params = additionaParams ||{};
    	this.applyChanges();
    	this.queryCataloguePanel.commit(function() {
           	Ext.Ajax.request({
    			url: this.services['getSQLQuery'],
    			params: params,
    			success: function(response, options) {
    				var responseJSON = Ext.decode(response.responseText);
    				callbackFn.call(scope, responseJSON);
       			},
       			scope: this,
    			failure: Sbi.exception.ExceptionHandler.handleFailure		
           	});   
		}, this);
	}
	
	// --------------------------------------------------------------------------------
	// 	private methods
	// --------------------------------------------------------------------------------
	
	, init: function(c) {
		this.initWestRegionPanel(c);
	}
	
	, initWestRegionPanel: function(c) {

		c.actions = new Array();
		c.actions.push({
			text: 'Add to SELECT clause',
			handler: this.onAddNodeToSelectAndFocus,
			scope: this,
			iconCls: 'option'
		});
		c.actions.push({
			text: 'Add to WHERE clause',
			handler: this.onAddNodeToWhereAndFocus,
			scope: this,
			iconCls: 'option'
		});
		c.actions.push({
			text: 'Add to HAVING clause',
			handler: this.onAddNodeToHavingAndFocus,
			scope: this,
			iconCls: 'option'
		});
		
		this.datamarts = [];
		
		var datamartsName = c.datamartsName;
		for (var i = 0; i < datamartsName.length; i++) {
			var datamartName = datamartsName[i];
			var dataMartStructurePanelConfig = Ext.apply({}, c, {'title': datamartName});
			var aDataMartStructurePanel = new Sbi.qbe.DataMartStructurePanel(dataMartStructurePanelConfig);
			this.datamarts.push(aDataMartStructurePanel);
			aDataMartStructurePanel.on('click', function(panel, node) {
		    	if(node.attributes.field && node.attributes.type == 'field') {
				    var field = {
				    	 id: node.id,
				         entity: node.attributes.entity, 
				         field: node.attributes.field,
				         alias: node.attributes.field,
				         longDescription: node.attributes.longDescription
				      };
				      
				    this.selectGridPanel.addField(field); 
				 }
		    }, this);
			aDataMartStructurePanel.on('expand', function(panel, node) {
				this.currentDataMartStructurePanel = panel;
		    }, this);
			aDataMartStructurePanel.on('nodeclick', function(panel, node) {
				this.onNodeClick(node);
		    }, this);
		}
		
		this.currentDataMartStructurePanel = this.datamarts[0];
		
		this.qbeStructurePanel = new Ext.Panel({
	        id:'treepanel',
	        collapsible: false,
	        margins:'0 0 0 5',
	        layout:'accordion',
	        layoutConfig:{
	          animate:true
	        },
	        items: this.datamarts
	    });
		
		this.westRegionPanel = new Ext.Panel({
	        title:LN('sbi.qbe.queryeditor.westregion.title'),
	        region:'west',
	        width:250,
	        margins: '5 5 5 5',
	        layout:'fit',
	        collapsible: true,
	        collapseFirst: false,
	        split: true,
	        
	        tools:[{	// todo: marge pin and unpin button in one single toggle-button
	          id:'pin',
	          qtip: LN('sbi.qbe.queryeditor.westregion.tools.expand'),
	          hidden: (!this.enableTreeToolbar || !this.enableTreeTbPinBtn),
	          handler: function(event, toolEl, panel){
	        	this.currentDataMartStructurePanel.expandAll();
	          }
	          , scope: this
	        }, {
	          id:'unpin',
	          qtip: LN('sbi.qbe.queryeditor.westregion.tools.collapse'),
	          hidden: (!this.enableTreeToolbar || !this.enableTreeTbUnpinBtn),
	          handler: function(event, toolEl, panel){
	        	this.currentDataMartStructurePanel.collapseAll();
	          }
	          , scope: this
	        }, {
			  id:'save',
			  qtip: LN('save'),
			  hidden: (!this.enableTreeToolbar || !this.enableTreeTbSaveBtn),
			  handler: function(event, toolEl, panel){
	        	Ext.Ajax.request({
					url:  this.services['saveTree'],
					callback: function(success, response, options) {
	       				if(success) {
	       					Ext.Msg.show({
	       						title: 'Add',
	       						msg: 'Calculated fields succesfully saved',
	       						buttons: Ext.Msg.OK,
	       						icon: Ext.MessageBox.INFO
	       					});
	       				}
	       			},
	       			scope: this,
					failure: Sbi.exception.ExceptionHandler.handleFailure		
	        	});  
			  }
			  , scope: this
		    }],
	        
	        items:[this.qbeStructurePanel]
	    });

		
		/*
		 * work-around: when executing the following operations:
		 * 1. collapsing west region panel
		 * 2. expanding and collapsing SpagoBI parameters panel
		 * 3. executing query
		 * 4. coming back to query builder panel
		 * 5. expanding west region panel
		 * then the datamart structure tree was not displayed.
		 * This work-around forces the layout recalculation when west region panel is expanded
		 * TODO: try to remove it when upgrading Ext library
		 */
		this.westRegionPanel.on('expand', function() {
			this.westRegionPanel.doLayout();
		}, this);
	}
	
	/**
	 * Makes the input panel resizable; if the panel contains a grid property, its height is modified.
	 * It was conceived for selectGridPanel and filterGridPanel.
	 */
	/*
	, createResizable: function(aPanel) {
		aPanel.on('render', function() {
			var resizer = new Ext.Resizable(this.id, {
			    handles: 's',
			    minHeight: 150,
			    pinned: false
			});
			resizer.on('resize', function(resizable, width, height, event) {
				if (this.grid) {
					this.grid.setHeight(height - 5);
				}
			}, this);
		}, aPanel);
	}
	*/
	
	, initCenterRegionPanel: function(c) {
		c.documentParametersStore = this.documentParametersStore;
		//c.anchor = '-20'; // for anchor layout, see http://www.sencha.com/forum/showthread.php?71796-No-vertical-scrollbar-with-vbox-layout
		this.selectGridPanel = new Sbi.qbe.SelectGridPanel(c);
		//this.createResizable(this.selectGridPanel);
	    this.filterGridPanel = new Sbi.qbe.FilterGridPanel(Ext.apply(c || {}, {gridTitle: LN('sbi.qbe.filtergridpanel.title')}));
	    //this.createResizable(this.filterGridPanel);
	    this.havingGridPanel = new Sbi.qbe.HavingGridPanel(c);
	    
	    var sharedConf = {
			enableToggle : true
			, toggleGroup : 'sbi.qbe.querybuilderpanel.togglegroup'
			, allowDepress : false	
	    };
	    
	    this.selectGridButton = new Ext.Button(Ext.apply(sharedConf, {
	    	cls: "x-btn-over", 
	    	listeners:{
	    		mouseout: function(button){
	    			button.addClass("x-btn-over");
	    		}
	    	},
		    text: LN('sbi.qbe.selectgridpanel.title')
		}));
	    this.filterGridButton = new Ext.Button(Ext.apply(sharedConf, {
	    	cls: "x-btn-over", 
	    	listeners:{
	    		mouseout: function(button){
	    			button.addClass("x-btn-over");
	    		}
	    	},
		    text: LN('sbi.qbe.filtergridpanel.title')
		}));
	    this.havingGridButton = new Ext.Button(Ext.apply(sharedConf, {
	    	cls: "x-btn-over", 
	    	listeners:{
	    		mouseout: function(button){
	    			button.addClass("x-btn-over");
	    		}
	    	},
		    text: LN('sbi.qbe.havinggridpanel.title')
		}));
		this.selectGridButton.on('toggle', this.onButtonToggleHandler.createDelegate(this, [0], true), this);
		this.filterGridButton.on('toggle', this.onButtonToggleHandler.createDelegate(this, [1], true), this);
		this.havingGridButton.on('toggle', this.onButtonToggleHandler.createDelegate(this, [2], true), this);
		this.selectGridButton.toggle(true, true);
	    
		this.getAmbiguousFieldsButton = new Ext.Button({
		    text: LN('sbi.qbe.queryeditor.centerregion.buttons.relationshipswizard')
		});
		this.getAmbiguousFieldsButton.on('click', this.getAmbiguousFields, this);
		
		var toolbar = [this.selectGridButton, this.filterGridButton, this.havingGridButton, '->', this.getAmbiguousFieldsButton];
		
		if(this.queryWidowTabs && this.queryWidowTabs.length>0){
			this.getQueryButton = new Ext.Button({
			    text: LN('sbi.qbe.queryeditor.centerregion.buttons.getquery')
			});
			this.getQueryButton.on('click', this.getQueryString, this);
			
			toolbar = [this.selectGridButton, this.filterGridButton, this.havingGridButton, '->', this.getQueryButton, this.getAmbiguousFieldsButton];
		}

		
	    this.centerRegionPanel = new Ext.Panel({ 
	    	title: LN('sbi.qbe.queryeditor.centerregion.title'),
	        region:'center',
	        width: '100%', // this is necessary in order to set the proper width to the columns of select/where/having clauses' grids in Firefox
	        autoScroll: true,
	        layout: 'card',
	        hideMode: !Ext.isIE ? 'nosize' : 'display',
	        activeItem : 0,
	        style: {
	            overflow: 'auto'
	        },
	        margins: '5 5 5 5',
	        items: [this.selectGridPanel, this.filterGridPanel, this.havingGridPanel],
	        tbar: toolbar
	    });
	    
	    /*this.centerRegionPanel = new Ext.Panel({ 
	    	title: LN('sbi.qbe.queryeditor.centerregion.title'),
	        region:'center',
	        width: '100%', // this is necessary in order to set the proper width to the columns of select/where/having clauses' grids in Firefox
	        autoScroll: true,
	        layout: 'anchor', // do not use vbox layout, see http://www.sencha.com/forum/showthread.php?71796-No-vertical-scrollbar-with-vbox-layout
	        style: {
	            overflow: 'auto'
	        },
	        margins: '5 5 5 5',
	        tools:[{
	        	id:'save',
	        	qtip: LN('sbi.qbe.queryeditor.centerregion.tools.save'),
	        	hidden: (this.enableQueryTbSaveBtn == false),
	        	handler: c.saveButtonHandler || this.showSaveQueryWindow,
	        	scope: this
	        }, {
	          id:'saveView',
	          qtip: LN('sbi.qbe.queryeditor.centerregion.tools.view'),
	          hidden: (this.enableQueryTbSaveViewBtn == false),
	          handler: function(event, toolEl, panel){
	        	Ext.Ajax.request({
					   	url: this.services['synchronyzeQuery'],
					   	success: function(success, response, options) {
		       				this.showSaveViewWindow();
		       			},
		       			scope: this,
		       			failure: Sbi.exception.ExceptionHandler.handleFailure,					
		       			params: this.getParams
					});	   
				},
	            scope: this 
	        },{
	          id:'search',
	          qtip: LN('sbi.qbe.queryeditor.centerregion.tools.validate'),
	          hidden: (this.enableQueryTbValidateBtn == false),
	          handler: function(event, toolEl, panel){
	            // refresh logic
	          }
	        }],
	        
	        items: [this.selectGridPanel, this.filterGridPanel, this.havingGridPanel]
	    });*/
	  
	    this.selectGridPanel.on('filter', function(panel, record) {
	    	var operandType;
	    	if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_SIMPLE) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
	    	} else if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_CALCULATED) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_CALCULATED_FIELD
	    	} else if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
	    	} else{
	    		Ext.Msg.show({
				   title:'Invalid operation',
				   msg: 'Impossibe to add field [' + record.data.alias + '] of type [' + record.data.type + '] to where clause',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
	    		return;
	    	}
	    	
	    	filter = {
	    		leftOperandValue: record.data.id
				, leftOperandDescription: record.data.entity + ' : ' + record.data.field 
				, leftOperandType: operandType
				, leftOperandLongDescription: record.data.entity + ' : ' + record.data.alias
				, leftOperandAlias: record.data.alias

			};
	    	this.toggleCenterPanelToItem(1);
	    	this.filterGridPanel.addFilter(filter);
	    }, this);
	    
	    this.selectGridPanel.on('having', function(panel, record) {
	    	var operandType;
	    	if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_SIMPLE) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
	    	} else if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_CALCULATED) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_CALCULATED_FIELD
	    	} else if(record.data.type === Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED) {
	    		operandType = Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
	    	} else{
	    		Ext.Msg.show({
				   title:'Invalid operation',
				   msg: 'Impossibe to add field [' + record.data.alias + '] of type [' + record.data.type + '] to having clause',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
				});
	    		return;
	    	}
	    	
	    	filter = {
	    		leftOperandValue: record.data.id
				, leftOperandDescription: record.data.entity + ' : ' + record.data.field 
				, leftOperandType: operandType
				, leftOperandAggregator: record.data.funct
				, leftOperandLongDescription: record.data.longDescription
			};
	    	this.toggleCenterPanelToItem(2);
	    	this.havingGridPanel.addFilter(filter);
	    }, this);
	}
	
	,
	onButtonToggleHandler : function (button, pressed, activeItemIndex) {
		if (pressed) {
			this.centerRegionPanel.getLayout().setActiveItem(activeItemIndex);
		}
	}
	
	,
	getAmbiguousFields: function() {
		this.applyChanges();
		this.queryCataloguePanel.manageAmbiguousFields(function() {
			// do nothings after commit for the moment
		}, this);
	}
	
	,getQueryString: function(){
		
		var thisPanel =this;
		
		var callbackFn = function(params){
			var jpql = params.jpqlFormatted;
			var sql = params.sqlFormatted;

			
			var windowsItems = new Array();
			
			var jpqlPanel = {
                	title: LN('sbi.generic.query.JPQL'),
                	layout:'fit',
                	name: 'jpql',
                	html: jpql,
                	autoScroll : true
                };
			
			var sqlPanel = {
                	title: LN('sbi.generic.query.SQL'),
                	layout:'fit',
                	name: 'sql',
                	html: sql,
                	autoScroll : true
                };
			
			if(thisPanel.queryWidowTabs){
				for(var i=0; i<thisPanel.queryWidowTabs.length; i++){
					if(thisPanel.queryWidowTabs[i]=='jqpl'){
						windowsItems.push(jpqlPanel);
					}else if(thisPanel.queryWidowTabs[i]=='sql'){
						windowsItems.push(sqlPanel);
					}
				}
			}				
			
			var win= new Ext.Window({
                layout:'fit',
                width:350,
                height:500,
                plain: true,
                items: new Ext.TabPanel({
                    autoTabs:true,
                    activeTab:0,
                    deferredRender:false,
                    border:false,
                    items: windowsItems
                }),

                buttons: [{
                    text: 'Close',
                    handler: function(){
                        win.close();
                    }
                }]
            });
			win.show();
		}
		
		
		var additionalParams = {
				//used to set a default value to the parameters of the query
				replaceParametersWithQuestion: true
		};
		
		this.getSQLQuery(callbackFn, this, additionalParams);
	}
	
	/*
	,
	getAmbiguousFields: function() {
		this.applyChanges();
		var query = this.getQuery(true);
		var queries = this.getQueries();
		// call the server to get ambiguous fields
		Ext.Ajax.request({
			url: this.services['getAmbiguousFields'],
			params: {
				id: query.id
				, catalogue : Ext.util.JSON.encode(queries)
			},
			success : this.onAmbiguousFieldsLoaded,
			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure
		});
	}
	
	,
	onAmbiguousFieldsLoaded : function (response, opts) {
		var ambiguousFields = Ext.util.JSON.decode( response.responseText );
		if (ambiguousFields.length == 0) {
			Ext.Msg.show({
				   title: LN('sbi.qbe.queryeditor.noambiguousfields.title'),
				   msg: LN('sbi.qbe.queryeditor.noambiguousfields.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.INFO
			});
		} else {
			ambiguousFields = this.mergeAmbiguousFieldsWithCache(ambiguousFields);
			var relationshipsWindow = new Sbi.qbe.RelationshipsWizardWindow({
				ambiguousFields : ambiguousFields
				, closeAction : 'close'
				, modal : true
			});
			relationshipsWindow.show();
			relationshipsWindow.on('apply', this.onAmbiguousFieldsSolved, this);
		}
	}
	
	,
	onAmbiguousFieldsSolved : function (theWindow, ambiguousFieldsSolved) {
		theWindow.close();
		this.putAmbiguousFieldsSolvedOnCache(ambiguousFieldsSolved);
	}
	
	,
	putAmbiguousFieldsSolvedOnCache : function (ambiguousFieldsSolved) {
		var query = this.getQuery(true);
		Sbi.cache.memory.put(query.id, ambiguousFieldsSolved);
	}
	
	,
	mergeAmbiguousFieldsWithCache : function (ambiguousFields) {
		var cached = this.getAmbiguousFieldsFromCache();
		var ambiguousFieldsObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : ambiguousFields });
		var cachedObj = new Sbi.qbe.AmbiguousFields({ ambiguousFields : cached });
		ambiguousFieldsObj.merge(cachedObj);
		return ambiguousFieldsObj.getAmbiguousFieldsAsJSONArray();
	}
	
	,
	getAmbiguousFieldsFromCache : function () {
		var query = this.getQuery(true);
		var cached = Sbi.cache.memory.get(query.id);
		return cached;
	}
	*/
	
	/**
	 * activate panel specified by the index in input by toggling the relevant button
	 */
	,
	toggleCenterPanelToItem : function (activeItemIndex) {
		var activeItem = this.centerRegionPanel.getLayout().activeItem;
		var index = this.centerRegionPanel.items.indexOf(activeItem);
		if (index == activeItemIndex) {
			// panel is already activated
			return;
		}
		switch (activeItemIndex) {
			case 0:
				this.selectGridButton.toggle(true, false); // the panel will be actived by the 'toggle' event
				return;
			case 1:
				this.filterGridButton.toggle(true, false); // the panel will be actived by the 'toggle' event
				return;
			case 2:
				this.havingGridButton.toggle(true, false); // the panel will be actived by the 'toggle' event
				return;
		}
	}
	
	, initEastRegionPanel: function(c) {
		
		this.queryCataloguePanel = new Sbi.qbe.QueryCataloguePanel({margins: '0 5 0 0', region: 'center'});
		this.documentParametersGridPanel = c.parametersGridPanel || new Sbi.qbe.DocumentParametersGridPanel(
				{margins: '0 0 0 0', region: 'south'}
				, this.documentParametersStore
		);
		this.queryCataloguePanel.on('load', function() {
			try{
				var message = {};
				message.messageName = 'catalogueready';
				sendMessage(message);
			}catch(e){
				
			}

		}, this);
		
		this.eastRegionPanel = new Ext.Panel({
	        title: LN('sbi.qbe.queryeditor.eastregion.title'),
	        region:'east',
	        width:250,
	        margins: '5 5 5 5',
	        layout:'border',
	        collapsible: true,
	        //collapseMode: 'mini',
	        collapseFirst: false,
	        collapsed: this.collapseQueryCataloguePanel,
	        split: true,
	        tools:[
		        {
		          id:'delete',
		          qtip: LN('sbi.qbe.queryeditor.eastregion.tools.delete'),
		          hidden: (this.enableCatalogueTbDeleteBtn == false),
		          handler: function(event, toolEl, panel){
		        	var q = this.queryCataloguePanel.getSelectedQuery();
		        	this.queryCataloguePanel.deleteQueries(q);
		          }, 
		          scope: this
		        }, {
		          id:'list',
		          qtip:LN('sbi.qbe.queryeditor.eastregion.tools.add'),
		          hidden: (this.enableCatalogueTbAddBtn == false),
		          handler: function(event, toolEl, panel){
		        	this.queryCataloguePanel.addQuery();
		          },
		          scope: this
		        }, {
			      id:'tree',
			      qtip:LN('sbi.qbe.queryeditor.eastregion.tools.insert'),
			      hidden: (this.enableCatalogueTbInsertBtn == false),
			      handler: function(event, toolEl, panel){
		        	var q = this.queryCataloguePanel.getSelectedQuery();
			        this.queryCataloguePanel.insertQuery(q);
			      },
			      scope: this
			   }
		    ],
	        items: [this.queryCataloguePanel, this.documentParametersGridPanel]
	    });
		
		
		this.queryCataloguePanel.on('beforeselect', function(panel, newquery, oldquery){
			// save changes applied to old query before to move to the new selected one
			this.applyChanges(); 

			this.setQuery( newquery );
			// required in order to be sure to have all query stored at the server side while
			// joining a subquery to a parent query selected entity, but only if previousQuery is defined (i.e. not the first time)
			this.queryCataloguePanel.commit(function() {
				// do nothings after commit for the moment
				// todo: implement hidingMask in order to block edinting while commiting
			}, this);
		}, this);
	}
	
	
	// -- handler methods ---------------------------------------------------------------
	
	, onNodeClick : function (node) {
		var activeItem = this.centerRegionPanel.getLayout().activeItem;
		var index = this.centerRegionPanel.items.indexOf(activeItem);
		switch (index) {
			case 0:
				this.onAddNodeToSelect(node);
				return;
			case 1:
				this.onAddNodeToWhere(node);
				return;
			case 2:
				this.onAddNodeToHaving(node);
				return;
		}
	}
	
	
	, onAddNodeToSelectAndFocus: function (node, recordBaseConfig) {
		this.toggleCenterPanelToItem(0);
		this.onAddNodeToSelect(node, recordBaseConfig);
	}
	
	, onAddNodeToSelect: function(node, recordBaseConfig) {
		var field;
		var nodeType;
		
		if(node.attributes) {
			recordBaseConfig = recordBaseConfig || {};
			nodeType = node.attributes.type || node.attributes.attributes.type;
			
    		if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
			    
    			field = {
			    	id: node.id,
			    	type: Sbi.constants.qbe.FIELD_TYPE_SIMPLE,
			    	entity: node.attributes.attributes.entity, 
			    	field: node.attributes.attributes.field,
			    	alias: node.attributes.attributes.field,
			    	longDescription: node.attributes.attributes.longDescription
			    };		
    			
    			Ext.apply(field, recordBaseConfig);
    			
    			this.selectGridPanel.addField(field);
			    		    
    		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) {
    			// if is previously saved calculated field
//    			if(!node.attributes.formState && node.attributes.attributes) {
//    				node.attributes.formState = node.attributes.attributes.formState;
//    				alert(node.attributes.toSource());
//    			}
    			
 	    		var field = {
 	    			id: node.attributes.attributes.formState,
 	    			type: Sbi.constants.qbe.FIELD_TYPE_CALCULATED,
 	    			entity: node.parentNode.text, 
			    	field: node.text,
 			        alias: node.text,
 			        longDescription: null
 			    };
 	    		
 	    		Ext.apply(field, recordBaseConfig);
 	    		
 	    		this.selectGridPanel.addField(field);
 	    		

 	    		var seeds =  Sbi.qbe.CalculatedFieldWizard.getUsedItemSeeds('dmFields', node.attributes.attributes.formState.expression);
 	    		for(var i = 0; i < seeds.length; i++) {
 	    			var n = node.parentNode.findChildBy(function(childNode) {
 	    				return childNode.id === seeds[i];
 	    			});
 	    			
 	    			if(n) {
 	    				this.onAddNodeToSelect(n, {visible:false});
 	    			} else {
 	    				alert('node  [' + seeds + '] not contained in entity [' + node.parentNode.text + ']');
 	    			}
 	    			
 	    			
 	    		}
    		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {	
 	 	    		var field = {
 	 	    			id: node.attributes.attributes.formState,
 	 	    			type: Sbi.constants.qbe.FIELD_TYPE_INLINE_CALCULATED,
 	 	    			entity: node.parentNode.text, 
 				    	field: node.text,
 	 			        alias: node.text,
 	 			        longDescription: null
 	 			    };
 	 	    		
 	 	    		Ext.apply(field, recordBaseConfig);
 	 	    		
 	 	    		this.selectGridPanel.addField(field);
 	 	    		

 	 	    		var seeds =  Sbi.qbe.CalculatedFieldWizard.getUsedItemSeeds('dmFields', node.attributes.attributes.formState.expression);
 	 	    		for(var i = 0; i < seeds.length; i++) {
 	 	    			var n = node.parentNode.findChildBy(function(childNode) {
 	 	    				return childNode.id === seeds[i];
 	 	    			});
 	 	    			
 	 	    			if(n) {
 	 	    				this.onAddNodeToSelect(n, {visible:false});
 	 	    				//this.currentDataMartStructurePanel.fireEvent('click', this.currentDataMartStructurePanel, n);
 	 	    			} else {
 	 	    				alert('node  [' + seeds + '] not contained in entity [' + node.parentNode.text + ']');
 	 	    			}
 	 	    			
 	 	    			
 	 	    		}

    		} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY){
    			for(var i = 0; i < node.attributes.children.length; i++) {
    				if((node.attributes.children[i].attributes.type != 'field' && node.attributes.children[i].attributes.type != Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) || 
    					(node.attributes.children[i].attributes.type != 'field' && node.attributes.children[i].attributes.type != Sbi.constants.qbe.IN_LINE_CALCULATED_FIELD)) continue;
    				field = {
          				id: node.attributes.children[i].id , 
            			entity: node.attributes.children[i].attributes.entity , 
            			field: node.attributes.children[i].attributes.field,
                    	alias: node.attributes.children[i].attributes.field,
                    	longDescription: node.attributes.children[i].attributes.longDescription
          			};				
    				this.selectGridPanel.addField(field);
    			}
    		} else {
    			Ext.Msg.show({
					   title:'Invalid operation',
					   msg: 'Node of type [' + nodeType + '] cannot be added to select table',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
    		}
		}
	}
	
	, onAddNodeToWhereAndFocus: function (node, recordBaseConfig) {
		this.toggleCenterPanelToItem(1);
		this.onAddNodeToWhere(node, recordBaseConfig);
	}
	
	, onAddNodeToWhere: function(node, recordBaseConfig) {
		var filter;
		var nodeType;
		
		if(node.attributes) {
			recordBaseConfig = recordBaseConfig || {};
			nodeType = node.attributes.type || node.attributes.attributes.type;
			
    		if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
    			
				filter = {
					leftOperandValue: node.id
					, leftOperandDescription: node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
					, leftOperandLongDescription: node.attributes.attributes.longDescription
					, leftOperandAlias: node.attributes.attributes.field 
				};
				
		  		this.filterGridPanel.addFilter(filter);
			
			} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
    		
				filter = {
					leftOperandValue: node.attributes.attributes.formState
					, leftOperandDescription: node.attributes.entity + ' : ' + node.attributes.attributes.formState.alias 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
					, leftOperandLongDescription: node.attributes.attributes.formState.alias 
				};
				
				this.filterGridPanel.addFilter(filter);
				
			} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY) {
				
				for(var i = 0; i < node.attributes.children.length; i++) {
					
					var childNode = node.attributes.children[i];
					nodeType = childNode.attributes.type;
					
					if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
				    							
						filter = {
							leftOperandValue: childNode.id
							, leftOperandDescription: childNode.attributes.entity + ' : ' + childNode.attributes.field 
							, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
							, leftOperandLongDescription: childNode.attributes.longDescription
							, leftOperandAlias: node.attributes.attributes.field 
						};
						
						this.filterGridPanel.addFilter(filter);
					} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
						filter = {
							leftOperandValue: childNode.attributes.formState
							, leftOperandDescription: childNode.attributes.entity + ' : ' + childNode.attributes.formState.alias 
							, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
							, leftOperandLongDescription: childNode.attributes.formState.alias 
						};
							
						this.filterGridPanel.addFilter(filter);
					}
				}
				
			} else {
				Ext.Msg.show({
					   title:'Invalid operation',
					   msg: 'Node of type [' + nodeType + '] cannot be added to filters table',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
			}
		}
	} 
	
	, onAddNodeToHavingAndFocus: function (node, recordBaseConfig) {
		this.toggleCenterPanelToItem(2);
		this.onAddNodeToHaving(node, recordBaseConfig);
	}
	
	, onAddNodeToHaving: function(node, recordBaseConfig) {
		var filter;
		var nodeType;
		
		if(node.attributes) {
			recordBaseConfig = recordBaseConfig || {};
			nodeType = node.attributes.type || node.attributes.attributes.type;
			
			if(nodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
		    		
				filter = {
					leftOperandValue: node.id
					, leftOperandDescription: node.attributes.attributes.entity + ' : ' + node.attributes.attributes.field 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
					, leftOperandLongDescription: node.attributes.attributes.longDescription
				};
		  		this.havingGridPanel.addFilter(filter);
			
			} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD){
				filter = {
					leftOperandValue: node.attributes.attributes.formState
					, leftOperandDescription: node.attributes.entity + ' : ' + node.attributes.attributes.formState.alias 
					, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
					, leftOperandLongDescription: node.attributes.attributes.formState.alias 
				};
						
				this.havingGridPanel.addFilter(filter);
			} else if(nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY){
				
				for(var i = 0; i < node.attributes.children.length; i++) {
					var childNode = node.attributes.children[i];
					var childNodeType = childNode.attributes.type;
					
					if(childNodeType == Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
						  					
						filter = {
							leftOperandValue: childNode.id
							, leftOperandDescription: childNode.attributes.entity + ' : ' + childNode.attributes.field 
							, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_SIMPLE_FIELD
							, leftOperandLongDescription: childNode.attributes.longDescription
						};
						
						this.havingGridPanel.addFilter(filter);
					} else if(childNodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
					
						filter = {
							leftOperandValue: childNode.attributes.formState
							, leftOperandDescription: childNode.entity + ' : ' + childNode.attributes.formState.alias 
							, leftOperandType: Sbi.constants.qbe.OPERAND_TYPE_INLINE_CALCULATED_FIELD
							, leftOperandLongDescription: childNode.attributes.formState.alias 
						};
									
						this.havingGridPanel.addFilter(filter);
					}
				}
				
			} else {
				Ext.Msg.show({
					   title:'Invalid operation',
					   msg: 'Node of type [' + nodeType + '] cannot be added to having table',
					   buttons: Ext.Msg.OK,
					   icon: Ext.MessageBox.ERROR
				});
			}
		}
	} 
	
	// --------------------------------------------------------------------------------
	// deprecated methods
	// --------------------------------------------------------------------------------
	, getParams: function() {
		Sbi.qbe.commons.deprectadeFunction('getParams', 'QueryBuilderPanel.js')
    	var queryStr = '{';
    	queryStr += 'fields : ' + this.selectGridPanel.getRowsAsJSONParams() + ',';
    	queryStr += 'distinct : ' + this.selectGridPanel.distinctCheckBox.getValue() + ',';
    	queryStr += 'filters : ' + this.filterGridPanel.getRowsAsJSONParams() + ',';
    	queryStr += 'expression: ' +  this.filterGridPanel.getFiltersExpressionAsJSON();
    	queryStr += '}';
    	
    	
    	var params = {
    		query: queryStr 
    	};        	
    	
    	return params;
    }
	
	,
	setQueriesCatalogue: function (queriesCatalogue) {
		this.queryCataloguePanel.setQueriesCatalogue(queriesCatalogue);
	}
	
	,
	getParameters: function () {
		return this.documentParametersGridPanel.getParameters();
	}
	
	,
	setParameters: function (parameters) {
		this.documentParametersGridPanel.loadItems([]);
		this.documentParametersGridPanel.loadItems(parameters);
	}
		
});