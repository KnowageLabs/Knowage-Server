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
  * - Andrea Gioia (adrea.gioia@eng.it)
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.DataMartStructurePanel = function(config) {
	
	var defaultSettings = {
		title: 'Datamart 1'
		, border:false
		, autoScroll: true
		, containerScroll: true
		, rootNodeText: 'Datamart'
		, ddGroup: 'gridDDGroup'
		, type: 'datamartstructuretree'
		, preloadTree: true
		, baseParams: {}
		, enableTreeContextMenu: false
  	};
	
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.dataMartStructurePanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.dataMartStructurePanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
	
	var params = c.title !== undefined ? {'datamartName': c.title} : {};
	
	this.services = this.services || new Array();	
	this.services['loadTree'] = this.services['loadTree'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_TREE_ACTION'
		, baseParams: params
	});

	this.services['getParameters'] = this.services['getParameters'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_PARAMETERS_ACTION'
		, baseParams: params
	});
	
	this.services['getAttributes'] = this.services['getAttributes'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_ATTRIBUTES_ACTION'
		, baseParams: params
	});
	
	this.services['addCalculatedField'] = this.services['addCalculatedField'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'ADD_CALCULATED_FIELD_ACTION'
		, baseParams: params
	});
	
	this.services['modifyCalculatedField'] = this.services['modifyCalculatedField'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'MODIFY_CALCULATED_FIELD_ACTION'
		, baseParams: params
	});
	
	this.services['deleteCalculatedField'] = this.services['deleteCalculatedField'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'DELETE_CALCULATED_FIELD_ACTION'
		, baseParams: params
	});
	
	
	this.addEvents('load', 'nodeclick');
	
	this.initTree(c.treeConfig || {});
		
	Ext.apply(c, {
		layout: 'fit'
		, items: [this.tree]
	});	
	
	// constructor
	Sbi.qbe.DataMartStructurePanel.superclass.constructor.call(this, c);
    
    
};

Ext.extend(Sbi.qbe.DataMartStructurePanel, Ext.Panel, {
    
	services: null
	, treeLoader: null
	, rootNode: null
	, preloadTree: true
	, tree: null
	, type: null
	, pressedNode: null
	, calculatedFieldWizard : null
	, inLineCalculatedFieldWizard : null
	, slotWizard : null
	, menu: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, load: function(params) {
		if(params) {
			this.treeLoader.baseParams = params;
		}
		this.tree.setRootNode(this.createRootNode());
	}

	, expandAll: function() {
		this.tree.expandAll();
	}
	
	, collapseAll: function() {
		this.tree.collapseAll();
	}
	
	, removeCalculatedField:  function(fieldNode) {
		var nodeType;
		nodeType = fieldNode.attributes.type || fieldNode.attributes.attributes.type;
		if(nodeType === Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD 
		|| nodeType === Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			
			var entityId = fieldNode.parentNode.id;
			var formState;
			if( fieldNode.attributes.attributes!=null){
				formState = fieldNode.attributes.attributes.formState;
			}else{
				formState = fieldNode.attributes.formState;
			}
    		var f = {
    			alias: formState.alias
    			, type: formState.type
    			, calculationDescriptor: formState
    		};
    		
    		var params = {
    			entityId: entityId,
    			nodeId: fieldNode.id,
    			field: Ext.util.JSON.encode(f)
    		}
    		
			Ext.Ajax.request({
				url:  this.services['deleteCalculatedField'],
				success: function(response, options, a) {
					var node = this.tree.getNodeById(options.params.nodeId);
					node.unselect();
		            Ext.fly(node.ui.elNode).ghost('l', {
		                callback: node.remove, scope: node, duration: .4
		            });
       			},
       			scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure,	
				params: params
        	}); 
		} else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.operation.delete.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	}
	
	, editCalculatedField: function(fieldNode) {
		var nodeType;

		nodeType = fieldNode.attributes.type || fieldNode.attributes.attributes.type;
		

		//edit slot forbidden
		if(fieldNode.attributes.attributes.formState.slots !== undefined 
			&& fieldNode.attributes.attributes.formState.slots !== null
			&& fieldNode.attributes.attributes.formState.slots.length > 0){
		
			Ext.Msg.show({
				   title:'Invalid operation',
				   msg: 'Range cannot be edited. Use Edit Range menu function.',
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
			return;
		}
		
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) {
			
			if(this.calculatedFieldWizard === null || this.inLineCalculatedFieldWizard === null) {
				this.initWizards();
			}
			
			var parentEtityNode = fieldNode.parentNode;
			var fields = new Array();
			for(var i = 0; i < parentEtityNode.attributes.children.length; i++) {
				var child = parentEtityNode.attributes.children[i];
				var childType = child.attributes.type || child.attributes.attributes.type;
				if(childType === 'field') {
					var field = {
						uniqueName: child.id,
						alias: child.text,
						text: child.attributes.field, 
						qtip: child.attributes.entity + ' : ' + child.attributes.field, 
						type: 'field', 
						value: 'dmFields[\'' + child.id + '\']'
					};	
					fields.push(field);
				}
			}
			this.calculatedFieldWizard.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)};
		
			this.calculatedFieldWizard.setExpItems('fields', fields);
			
			this.calculatedFieldWizard.setTargetNode(fieldNode);
			this.calculatedFieldWizard.show();
		} else 	if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			
			if(this.calculatedFieldWizard === null || this.inLineCalculatedFieldWizard === null) {
				this.initWizards();
			}			
			
			var parentEtityNode = fieldNode.parentNode;
			var fields = new Array();
			for(var i = 0; i < parentEtityNode.attributes.children.length; i++) {
				var child = parentEtityNode.attributes.children[i];
				var childType = child.attributes.type || child.attributes.attributes.type;

			
				if(childType === 'field') {
					var field = {
						uniqueName: child.id,
						alias: child.text,
						text: child.attributes.field, 
						qtip: child.attributes.entity + ' : ' + child.attributes.field, 
						type: 'field', 
						value: 'dmFields[\'' + child.id + '\']'
					};	
					fields.push(field);
				}
			}
			this.inLineCalculatedFieldWizard.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)};
			this.inLineCalculatedFieldWizard.setExpItems('fields', fields);
			this.inLineCalculatedFieldWizard.setTargetNode(fieldNode);
			this.inLineCalculatedFieldWizard.show();
	
			
		} else{
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.operation.edit.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	}
	
	, addSlot: function(entityNode) {
		if(entityNode==null || entityNode==undefined){
			entityNode = this.pressedNode;
		}
		this.pressedNode=entityNode
		
		var selectNode;
		
		if(!entityNode) return;
		var type = entityNode.attributes.type || entityNode.attributes.attributes.type;
		var text = entityNode.text || entityNode.attributes.text;
		
		if(type === Sbi.constants.qbe.NODE_TYPE_ENTITY) {
				
			var fields = new Array();
			for(var i = 0; i < entityNode.attributes.children.length; i++) {
				var child = entityNode.attributes.children[i];
				var childType = child.attributes.type || child.attributes.attributes.type;
				if(childType === 'field') {
					var field = {
						uniqueName: child.id,
						alias: child.text,
						text: child.attributes.field, 
						qtip: child.attributes.entity + ' : ' + child.attributes.field, 
						type: 'field', 
						value: 'dmFields[\'' + child.attributes.field + '\']'
					};	
					fields.push(field);
				}
			}
			//creates a window
			this.slotWizard = new Sbi.qbe.SlotWizard( { 
				title: LN('sbi.qbe.bands.title'),
				startFromFirstPage: true,
		    	expItemGroups: [
	    		    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}, 
	    		    {name:'arithmeticFunctions', text: LN('sbi.qbe.calculatedFields.functions.arithmentic')},  		    
	    		    {name:'dateFunctions', text: LN('sbi.qbe.calculatedFields.datefunctions')}
	    		],
	    		fields: new Array(),
	    		arithmeticFunctions: Sbi.constants.qbe.SLOTS_EDITOR_ARITHMETIC_FUNCTIONS,
	    		dateFunctions: Sbi.constants.qbe.SLOTS_EDITOR_DATE_FUNCTIONS,
	    		expertMode: false,
	        	scopeComboBoxData :[
	        	    ['STRING','String', 'If the expression script returns a plain text string'],
	        	    ['NUMBER', 'Number', 'If the expression script returns a number'],
	        	    ['DATE', 'Date', LN('sbi.qbe.calculatedFields.num.type')]
	        	],
	        	validationService: {
					serviceName: 'VALIDATE_EXPRESSION_ACTION'
					, baseParams: {contextType: 'datamart'}
					, params: null
				}
			});
	    	
			var slotCalculatedFieldsPanel = this.slotWizard.getCalculatedFiledPanel();
			if(slotCalculatedFieldsPanel.mainPanel !== undefined && slotCalculatedFieldsPanel.mainPanel != null){
				slotCalculatedFieldsPanel.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)};
			}
			slotCalculatedFieldsPanel.setExpItems('fields', fields);
			slotCalculatedFieldsPanel.setTargetNode(entityNode);
		
			
			this.slotWizard.mainPanel.doLayout();
			this.slotWizard.show();

		
		} else if(type === Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD){
			//creates a window
			this.slotWizard = new Sbi.qbe.SlotWizard( { 
				title: LN('sbi.qbe.bands.title'),
				fieldForSlot: entityNode,
				startFromFirstPage: false
			});
			
			this.slotWizard.mainPanel.doLayout();
			this.slotWizard.show();
		} else {
			Ext.Msg.show({
				   title: LN('sbi.qbe.bands.wizard.invalid.definition'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.definition.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});		
		}	
		
		this.slotWizard.on('apply', this.onApplySlotNew, this);
	}

	, editSlot: function(entityNode) {
		if(entityNode==null || entityNode==undefined){
			entityNode = this.pressedNode;
		}
		this.pressedNode=entityNode
		
		var selectNode;
		
		if(!entityNode) return;
		var type = entityNode.attributes.type || entityNode.attributes.attributes.type;
		var text = entityNode.text || entityNode.attributes.text;
		
		if(type === Sbi.constants.qbe.NODE_TYPE_ENTITY) {			
			Ext.Msg.show({
			   title: LN('sbi.qbe.bands.wizard.invalid.operation'),
			   msg:  LN('sbi.qbe.bands.wizard.invalid.node'),
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});		
		} else if(type === Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD){
		
 			var fields = new Array();
 			var fieldNode = entityNode.parentNode;
 			for(var i = 0; i < fieldNode.childNodes.length; i++) {
 				var child = fieldNode.childNodes[i];
 				var childType = child.attributes.type || child.attributes.attributes.type;
 				if(childType === 'field') {
 					var field = {
 						uniqueName: child.id,
 						alias: child.text,
 						text: child.attributes.attributes.field, 
 						qtip: child.attributes.attributes.entity + ' : ' + child.attributes.attributes.field, 
 						type: 'field', 
 						value: 'dmFields[\'' + child.attributes.attributes.field + '\']'
 					};	
 					fields.push(field);
 				}
 			}
 			
 			//creates a window
 			this.slotWizard = new Sbi.qbe.SlotWizard( { 
 				title: LN('sbi.qbe.bands.title'),
				fieldForSlot: entityNode,
				modality: 'edit',
				startFromFirstPage: false,
				expItemGroups: [
				    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}, 
				    {name:'arithmeticFunctions', text: LN('sbi.qbe.calculatedFields.functions.arithmentic')},  		    
				    {name:'dateFunctions', text: LN('sbi.qbe.calculatedFields.datefunctions')}
				],
				fields: new Array(),
				arithmeticFunctions: Sbi.constants.qbe.SLOTS_EDITOR_ARITHMETIC_FUNCTIONS,
				dateFunctions: Sbi.constants.qbe.SLOTS_EDITOR_DATE_FUNCTIONS,
				expertMode: false,
				 		  
 	        	scopeComboBoxData :[
 	        	    ['STRING','String', 'If the expression script returns a plain text string'],
 	        	    ['NUMBER', 'Number', 'If the expression script returns a number'],
 	        	     ['DATE', 'Date', LN('sbi.qbe.calculatedFields.num.type')]
 	        	],
 	        	validationService: {
 					serviceName: 'VALIDATE_EXPRESSION_ACTION'
 					, baseParams: {contextType: 'datamart'}
 					, params: null
 				}
 			});
    	

			this.slotWizard.on('apply', this.onApplySlotEdit, this);
			
 			if(this.slotWizard.mainPanel.validationService !== undefined){
 				this.slotWizard.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)}; 		
 			}
			this.slotWizard.setExpItems('fields', fields);
						
			this.slotWizard.setTargetNode(entityNode);
			this.slotWizard.mainPanel.doLayout();
			this.slotWizard.show(); 	

		}else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.node'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});		
		}	

	}
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, createRootNode: function() {		
		var node = new Ext.tree.AsyncTreeNode({
	        text		: this.rootNodeText,
	        iconCls		: 'database',
	        expanded	: true,
	        draggable	: false
	    });
		return node;
	}
	
	, initTree: function(config) {
		
		this.treeLoader = new Ext.tree.TreeLoader({
	        baseParams: this.baseParams || {},
	        dataUrl: this.services['loadTree']
	    });
		this.treeLoader.on('load', this.oonLoad, this);
		this.treeLoader.on('loadexception', this.oonLoadException, this);
		
		this.rootNode = this.createRootNode();
		
		this.tree = new Ext.tree.TreePanel({
	        collapsible: false,
	        
	        enableDD: true,
	        ddGroup: this.ddGroup,
	        dropConfig: {
				isValidDropPoint : function(n, pt, dd, e, data){
					return false;
				}      
	      	},
	      	
	        animCollapse     : true,
	        collapseFirst	 : false,
	        border           : false,
	        autoScroll       : true,
	        containerScroll  : true,
	        animate          : false,
	        trackMouseOver 	 : true,
	        useArrows 		 : true,
	        loader           : this.treeLoader,
	        preloadTree		 : this.preloadTree,
	        root 			 : this.rootNode,
	        rootVisible		 : false
	    });	
		
		this.tree.type = this.type;
		
		this.tree.on('click', function(node) {
			var nodeType = node.attributes.type || node.attributes.attributes.type;
			if(nodeType !== 'entity') {
				this.fireEvent('nodeclick', this, node);
			}
		}, this);
		if(this.enableTreeContextMenu) {
			this.tree.on('contextmenu', this.onContextMenu, this);
			
		}
	}

	, initWizards: function() {
		this.initCalculatedFieldWizard();
		this.initInLineCalculatedFieldWizard();
	}
		
		
	, initInLineCalculatedFieldWizard: function() {	
		var fields = new Array();
		this.inLineCalculatedFieldWizard = new Sbi.qbe.CalculatedFieldWizard({
			title: LN('sbi.qbe.inlineCalculatedFields.title'),
    		expItemGroups: [
    		    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}
    		    , {name:'arithmeticFunctions', text: LN('sbi.qbe.calculatedFields.functions.arithmentic')}
    		    //, {name:'aggregationFunctions', text: LN('sbi.qbe.calculatedFields.aggrfunctions')},
    		    , {name:'dateFunctions', text: LN('sbi.qbe.calculatedFields.datefunctions')}
    		],
    		fields: fields,
    		arithmeticFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS,
    		//aggregationFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_AGGREGATION_FUNCTIONS,
    		dateFunctions: Sbi.constants.qbe.INLINE_CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS,
    		expertMode: false,
        	scopeComboBoxData :[
        	    ['STRING','String',  LN('sbi.qbe.calculatedFields.string.type')],
        	    ['NUMBER', 'Number', LN('sbi.qbe.calculatedFields.num.type')],
        	    ['DATE', 'Date', LN('sbi.qbe.calculatedFields.num.type')]
        	    ],
        	validationService: {
				serviceName: 'VALIDATE_EXPRESSION_ACTION'
				, baseParams: {contextType: 'datamart'}
				, params: null
			}
    	});
     	
     	this.inLineCalculatedFieldWizard.mainPanel.on('notexpert', this.onPassToExpertMode, this);
	
    	this.inLineCalculatedFieldWizard.on('apply', this.onApplyInlineCalculatedField, this);
    	
	}
	
	, onPassToExpertMode: function(){
		var alias;
		if(this.inLineCalculatedFieldWizard != null &&  this.inLineCalculatedFieldWizard != undefined &&
				   this.inLineCalculatedFieldWizard.inputFields !== null && this.inLineCalculatedFieldWizard.inputFields !== undefined) {
 			alias = this.inLineCalculatedFieldWizard.mainPanel.inputFields.alias.getValue();
 		}
		this.showCalculatedFieldWizard(this.pressedNode);
		this.inLineCalculatedFieldWizard.hide();
		this.calculatedFieldWizard.mainPanel.setCFAlias(alias);
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
    		title: LN('sbi.qbe.calculatedFields.title'),
     		expItemGroups: [
       		    {name:'fields', text: LN('sbi.qbe.calculatedFields.fields')}
       		    , {name:'parameters', text: LN('sbi.qbe.calculatedFields.parameters'), loader: parametersLoader}
       		    , {name:'attributes', text: LN('sbi.qbe.calculatedFields.attributes'), loader: attributesLoader}
       		    , {name:'arithmeticFunctions', text: LN('sbi.qbe.calculatedFields.functions.arithmentic')}
       		    , {name:'groovyFunctions', text: LN('sbi.qbe.calculatedFields.functions.script')}
       		    //, {name:'dateFunctions', text: LN('sbi.qbe.calculatedFields.datefunctions')}
       		],
       		fields: fields,
       		arithmeticFunctions: Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_ARITHMETIC_FUNCTIONS,
       		groovyFunctions: Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_SCRIPT_FUNCTIONS,
       		// dateFunctions: Sbi.constants.qbe.CALCULATED_FIELD_EDITOR_DATE_FUNCTIONS,
       		expertMode: true,
          	scopeComboBoxData :[
           	    ['STRING','String', LN('sbi.qbe.calculatedFields.string.type')],
           	    ['HTML', 'Html', LN('sbi.qbe.calculatedFields.html.type')],
           	    ['NUMBER', 'Number', LN('sbi.qbe.calculatedFields.num.type')]
           	],
    		validationService: {
				serviceName: 'VALIDATE_EXPRESSION_ACTION'
				, baseParams: {contextType: 'datamart'}
				, params: null
			}
    	});
		
		this.calculatedFieldWizard.on('apply', this.onApplyCalculatedField, this);
		
		this.calculatedFieldWizard.mainPanel.on('expert', this.onPassToSimpleMode, this);
	}
	
	, onPassToSimpleMode: function(){
		var alias;
		if(this.calculatedFieldWizard!=null && this.calculatedFieldWizard != undefined &&
 				this.calculatedFieldWizard.inputFields !== null && this.calculatedFieldWizard.inputFields !== undefined){
 			alias = this.calculatedFieldWizard.mainPanel.inputFields.alias.getValue();
 		}
 		
		this.inLineCalculatedFieldWizard.show();
 		this.calculatedFieldWizard.hide();
 		this.showInLineCalculatedFieldWizard(this.pressedNode);
 		this.inLineCalculatedFieldWizard.mainPanel.setCFAlias(alias);	
 	}
	
	, showCalculatedFieldWizard: function(entityNode) {
		if(entityNode==null || entityNode==undefined){
			entityNode = this.pressedNode;
		}
			
		var selectNode;
		this.pressedNode=entityNode;
		if(!entityNode) return;
		var type = entityNode.attributes.type || entityNode.attributes.attributes.type;
		var text = entityNode.text || entityNode.attributes.text;
		
		if(type === Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			
			if(this.calculatedFieldWizard === null) {
				this.initCalculatedFieldWizard();
			}
			
			var fields = new Array();
			for(var i = 0; i < entityNode.attributes.children.length; i++) {
				var child = entityNode.attributes.children[i];
				var childType = child.attributes.type || child.attributes.attributes.type;
				if(childType === Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
					var field = {
						uniqueName: child.id,
						alias: child.text,
						text: child.attributes.field, 
						qtip: child.attributes.entity + ' : ' + child.attributes.field, 
						type: 'field', 
						value: 'dmFields[\'' + child.id + '\']'
					};	
					fields.push(field);
				}
			}
			this.calculatedFieldWizard.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)};
			this.calculatedFieldWizard.setExpItems('fields', fields);
			this.calculatedFieldWizard.setTargetNode(entityNode);
			this.calculatedFieldWizard.show();
		
		} else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.calculatedFields.add.error'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});		
		}	
	}
	
	, showInLineCalculatedFieldWizard: function(entityNode) {
		if(entityNode==null || entityNode==undefined){
			entityNode = this.pressedNode;
		}
		this.pressedNode=entityNode
		
		var selectNode;
		
		if(!entityNode) return;
		var type = entityNode.attributes.type || entityNode.attributes.attributes.type;
		var text = entityNode.text || entityNode.attributes.text;
		
		if(type === Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			
			if(this.inLineCalculatedFieldWizard === null) {
				this.initInLineCalculatedFieldWizard();
			}
			
			var fields = new Array();
			for(var i = 0; i < entityNode.attributes.children.length; i++) {
				var child = entityNode.attributes.children[i];
				var childType = child.attributes.type || child.attributes.attributes.type;
				if(childType === Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
					var field = {
						uniqueName: child.id,
						alias: child.text,
						text: child.attributes.field, 
						qtip: child.attributes.entity + ' : ' + child.attributes.field, 
						type: 'field', 
						value: 'dmFields[\'' + child.attributes.field + '\']'
					};	
					fields.push(field);
				}
			}
			
			this.inLineCalculatedFieldWizard.show();
			this.inLineCalculatedFieldWizard.mainPanel.validationService.params = {fields: Ext.util.JSON.encode(fields)};
			this.inLineCalculatedFieldWizard.setExpItems('fields', fields);
			this.inLineCalculatedFieldWizard.setTargetNode(entityNode);
			this.inLineCalculatedFieldWizard.show();
		
		} else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.calculatedFields.add.error'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});		
		}	
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	, onApplyInlineCalculatedField : function(win, formState, targetNode, fieldType){
		
		var nodeType;
		var entityId;
		var fieldId;
		var editingMode;
		
		nodeType = targetNode.attributes.type || targetNode.attributes.attributes.type;
		
		// is it the editing of en existing field or the creation of a new one?
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			entityId = targetNode.parentNode.id;
			editingMode = 'modify';
			fieldId = targetNode.attributes.attributes.formState.alias; 
		} else if(Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			entityId = targetNode.id;
			editingMode = 'create';
		} else {
			Ext.Msg.show({
			   title:'Internal error',
			   msg: 'Input parameter [targetNode] of function [onApplyInlineCalculatedField] cannot be of type [' + nodeType + ']',
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});		
		}
		
		var f = {
			alias: formState.alias
			, id: formState
			, filedType: fieldType
			, type: formState.type
			, calculationDescriptor: formState
		};
		
		var params = {
			entityId: entityId,
			fieldId: fieldId,
			editingMode: editingMode,
			field: Ext.util.JSON.encode(f)
		}
		
		Ext.Ajax.request({
			url:  this.services['addCalculatedField'],
			success: function(response, options) {
//				Ext.Msg.show({
//					   title: 'Add',
//					   msg: 'Calculated field succesfully added to tree',
//					   buttons: Ext.Msg.OK,
//					   icon: Ext.MessageBox.INFO
//				});
   			},
   			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure,	
			params: params
    	}); 
		
		
		if(editingMode === 'modify') {
			targetNode.setText(formState.alias);
			targetNode.attributes.attributes.formState = formState;
			
		} else if (editingMode === 'create') {
			
			var node = new Ext.tree.TreeNode({
    			text: formState.alias,
    			leaf: true,
    			type: fieldType, 
    			longDescription: formState.expression,
    			formState: formState, 
    			iconCls: 'calculation',
    			attributes:{
        			text: formState.alias,
        			leaf: true,
        			type: Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD, 
        			longDescription: formState.expression,
        			formState: formState, 
        			iconCls: 'calculation'}
        		
    		});

			
			if (!targetNode.isExpanded()) {
    			targetNode.expand(false, true, function() {targetNode.appendChild( node );});
    		} else {
    			targetNode.appendChild( node );
    		}
		} 
	}

	, onApplyCalculatedField : function(win, formState, targetNode, fieldType){
		
		var nodeType;
		var entityId;
		var fieldId;
		var editingMode;
		
		nodeType = targetNode.attributes.type || targetNode.attributes.attributes.type;
			
		// is it the editing of en existing field or the creation of a new one?
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			entityId = targetNode.parentNode.id;
			editingMode = 'modify';
			fieldId = targetNode.attributes.attributes.formState.alias;
		} else if(Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			entityId = targetNode.id;
			editingMode = 'create';
		} else {
			Ext.Msg.show({
			   title:'Internal error',
			   msg: 'Input parameter [targetNode] of function [onApplyInlineCalculatedField] cannot be of type [' + nodeType + ']',
			   buttons: Ext.Msg.OK,
			   icon: Ext.MessageBox.ERROR
			});		
		}
		
		
		var entityId = (nodeType == Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD)? targetNode.parentNode.id: targetNode.id;
		var f = {
			alias: formState.alias
			, id: formState
			, type: formState.type
			, filedType: fieldType
			, calculationDescriptor: formState
		};
		var params = {
			editingMode : editingMode,
			entityId: entityId,
			fieldId: fieldId,
			field: Ext.util.JSON.encode(f)
		}
		
		Ext.Ajax.request({
			url:  this.services['addCalculatedField'],
			success: function(response, options) {
//				Ext.Msg.show({
//					   title: 'Add',
//					   msg: 'Calculated field succesfully added to tree',
//					   buttons: Ext.Msg.OK,
//					   icon: Ext.MessageBox.INFO
//				});
   			},
   			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure,	
			params: params
    	}); 
		

		if(nodeType == Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) {
			targetNode.id = formState;
			targetNode.setText(formState.alias);
			targetNode.attributes.attributes.formState = formState;
		} else if (nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			var node = new Ext.tree.TreeNode({
    			text: formState.alias,
    			leaf: true,
    			type: fieldType, 
    			longDescription: formState.expression,
    			formState: formState, 
    			iconCls: 'calculation',
    			attributes:{
        			text: formState.alias,
        			leaf: true,
        			type: Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD, 
        			longDescription: formState.expression,
        			formState: formState, 
        			iconCls: 'calculation'}
    		});

			
			if (!targetNode.isExpanded()) {
    			targetNode.expand(false, true, function() {targetNode.appendChild( node );});
    		} else {
    			targetNode.appendChild( node );
    		}
		} else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.operation.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
	}
	
	, onApplySlotNew : function(win, formState, targetNode){
		
		var nodeType;
		nodeType = targetNode.attributes.type || targetNode.attributes.attributes.type;
		
		var entityId = (nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD)? targetNode.parentNode.id: targetNode.id;
		var f = {
			alias: formState.alias
			, id: formState
			, filedType: Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD
			, type: formState.type
			, calculationDescriptor: formState
		};
		
		var params = {
			editingMode : 'create',
			entityId: entityId,
			field: Ext.util.JSON.encode(f)
		}
		
		Ext.Ajax.request({
			url:  this.services['addCalculatedField'],
			success: function(response, options) {
//				Ext.Msg.show({
//				   title: 'Add',
//				   msg: 'Calculated field succesfully added to tree',
//				   buttons: Ext.Msg.OK,
//				   icon: Ext.MessageBox.INFO
//			});
   			},
   			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure,	
			params: params
    	}); 
		
		
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			targetNode.text = formState.alias;
			targetNode.attributes.attributes.formState = formState;
		} else if (nodeType == Sbi.constants.qbe.NODE_TYPE_ENTITY) {
			var node = new Ext.tree.TreeNode({
    			text: formState.alias,
    			leaf: true,
    			type: Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD, 
    			longDescription: formState.expression,
    			formState: formState, 
    			iconCls: 'calculation',
    			attributes:{
        			text: formState.alias,
        			leaf: true,
        			type: Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD, 
        			longDescription: formState.expression,
        			formState: formState, 
        			iconCls: 'calculation'}
        		
    		});

			
			if (!targetNode.isExpanded()) {
    			targetNode.expand(false, true, function() {targetNode.appendChild( node );});
    		} else {
    			targetNode.appendChild( node );
    		}
		} else {
			Ext.Msg.show({
				   title: LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.operation.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
		win.close();
	}
	
	, onApplySlotEdit : function(win, formState, targetNode){
		
		var nodeType;
		nodeType = targetNode.attributes.type || targetNode.attributes.attributes.type;
		
		var entityId = (nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD)? targetNode.parentNode.id: targetNode.id;
		var f = {
			alias: formState.alias
			, id: formState
			, filedType: Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD
			, type: formState.type
			, calculationDescriptor: formState
		};
		
		var params = {
			editingMode: 'modify',
			entityId: entityId,
			fieldId: targetNode.attributes.attributes.formState.alias,
			field: Ext.util.JSON.encode(f)
		}
		
		Ext.Ajax.request({
			url:  this.services['addCalculatedField'],
			success: function(response, options) {
//				Ext.Msg.show({
//				   title: 'Add',
//				   msg: 'Calculated field succesfully added to tree',
//				   buttons: Ext.Msg.OK,
//				   icon: Ext.MessageBox.INFO
//			});
   			},
   			scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure,	
			params: params
    	}); 
		
		if(nodeType == Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
			targetNode.setText(formState.alias);
			targetNode.attributes.attributes.formState = formState;
		}  else {
			Ext.Msg.show({
				   title:LN('sbi.qbe.bands.wizard.invalid.operation'),
				   msg: LN('sbi.qbe.bands.wizard.invalid.operation.msg'),
				   buttons: Ext.Msg.OK,
				   icon: Ext.MessageBox.ERROR
			});
		}
		win.close();
	}
	
	, initMenu: function() {
		 this.menu = new Ext.menu.Menu({
             id:'feeds-ctx',
             items: [
             // ACID operations on nodes
             '-',{
            	 text:LN('sbi.qbe.calculatedFields.add'),
                 iconCls:'add',
                 handler:function(){
            	   	this.showInLineCalculatedFieldWizard(this.ctxNode);	         	 	
	             },
                 scope: this
             },{
            	 text:LN('sbi.qbe.calculatedFields.edit'),
                 iconCls:'edit',
                 handler:function(){
	         	 	this.editCalculatedField(this.ctxNode);
	             },
                 scope: this
             },{
            	 text: LN('sbi.qbe.menu.bands.add'),
                 iconCls:'slot',
                 handler:function(){
            		this.addSlot(this.ctxNode);	
	             },
                 scope: this
             },{
            	 text: LN('sbi.qbe.menu.bands.edit'),
                 iconCls:'slot',
                 handler:function(){
            		this.editSlot(this.ctxNode);	
	             },
                 scope: this
             },{
            	 text:LN('sbi.qbe.calculatedFields.remove'),
                 iconCls:'remove',
                 handler:  function() {
	            	 this.ctxNode.ui.removeClass('x-node-ctx');
	            	 this.removeCalculatedField(this.ctxNode);
	            	 this.ctxNode = null;
                 },
                 scope: this
             }]
         });

		 for(var i = 0; i < this.actions.length; i++) {
			
			 var item = new Ext.menu.Item({
				 text: this.actions[i].text,
	             iconCls: this.actions[i].iconCls,
	             handler:  this.executeAction.createDelegate(this, [i]),
	             scope: this
			 });
			 //item.action = this.actions[i];
			 this.menu.insert(i, item);
		 }
		 
		 
		 
         this.menu.on('hide', function(){
             if(this.ctxNode){
                 this.ctxNode.ui.removeClass('x-node-ctx');
                 this.ctxNode = null;
             }
         }, this);
	}
	
	, executeAction: function(actionIndex) {
		this.actions[actionIndex].handler.call(this.actions[actionIndex].scope, this.ctxNode);
	}
	
	, onContextMenu : function(node, e){
		if(this.menu != null){
			this.menu.destroy();
		}

		this.initMenu();// create context menu on first right click
        
        if(this.ctxNode){
            this.ctxNode.ui.removeClass('x-node-ctx');
            this.ctxNode = null;
        }
        
        this.ctxNode = node;
        this.ctxNode.ui.addClass('x-node-ctx');
        this.menu.showAt(e.getXY());
    }
	
	, oonLoad: function(treeLoader, node, response) {
		this.rootNode = this.tree.root;
		this.fireEvent('load', this, treeLoader, node, response);
	}
	
	, oonLoadException: function(treeLoader, node, response) {
		Sbi.exception.ExceptionHandler.handleFailure(response, treeLoader.baseParams || {});
	}
});