/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
/**
  * CalculatedFieldEditorPanel - short description
  * 
  * Object documentation ...
  * 
  * @author Monica Franceschini
  * @author Benedetto Milazzo
  */

Ext.ns("Sbi.cockpit.widgets.table.wizard");

Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel = function(config) {
	
	var c = Ext.apply({}, config || {}, {
		layout: 'border',
	    frame: false, 
	    border: false,
	    bodyStyle: 'background:#E8E8E8;',
	    padding: 3
	});

	Ext.apply(this, c);
	
	this.ddGroup = 'drag&drop';
	
	this.expertDisable = c.expertDisable;
	
	this.initNorthRegionPanel(c.northRegionConfig || {});
	this.initWestRegionPanel(c.westRegionConfig || {});
	this.initCenterRegionPanel(c.centerRegionConfig || {});
	
	Ext.apply(c, {
		items:  [this.westRegionPanel, this.centerRegionPanel, this.northRegionPanel]
	});	
	
	// constructor
	Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel, Ext.Panel, {
	hasBuddy: null
    , buddy: null
    
    , expertDisable: false
   
    , westRegionPanel: null 
    , centerRegionPanel: null
    , northRegionPanel: null
    
    , detailsFormPanel: null
    , expItemsTreeRootNode: null
    , expItemsTreeStore: null
	, expItemsTree: null
	, expItemsPanel: null
		
	, baseExpression: ''
	, expressionEditor: null
	, expressionEditorPanel: null
	
	, inputFields: null
//	, expertCheckBox: null
	
	, target: null
	
//	, validationService:  null
	, expertMode:  false
	
	, expItemGroups: null
	, groupRootNodes: null
	, scopeComboBoxData: null	
	
	, opWin: null
	

    // --------------------------------------------------------------------------------------------
    // public methods
    // --------------------------------------------------------------------------------------------
	
	
	, getExpression: function() {
		
		var expression;
		if(this.expressionEditor) {
	  		expression = this.expressionEditor.getValue();
//	  		expression = expression.replace(/<span recordid="(.+?)">.*?span>/ig,"$1 ").trim();
//	  		
//	  		expression = Ext.util.Format.stripTags( expression );
	  		expression = expression.replace(/&nbsp;/g," ");
	  		expression = expression.replace(/\u200B/g,"");
//	  		expression = expression.replace(/&gt;/g,">");
//	  		expression = expression.replace(/&lt;/g,"<");
//	  		
//	  		if(!this.expertMode){
//	  			expression = this.replaceFieldAliasesWithFieldUniqueNames(expression);
//	  		}
	  	}
		return expression;
	}


	, setExpression: function(expression) {
		
		if(this.expressionEditor) {
			expression = expression.replace(/ /g,"&nbsp;");
	  		expression = expression.replace(/</g,"&lt;");
	  		expression = expression.replace(/>/g,"&gt;");
	  		
	  		this.expressionEditor.setValue(expression);
	  		this.expressionEditor.insertAtCursor( expression );
		}
	}
	
	, replaceFieldAliasesWithFieldUniqueNames: function(expression) {
		var newExpression;
				
		newExpression = expression;
		var fieldNodes = this.groupRootNodes['fields'];
		var childNodes = fieldNodes.children;
		var aliasToUniqueNameMap = new Object();
		var aliasOrderedByLengthList = new Array();
		for(var i = 0; i < childNodes.length; i++) {
			var childNode = childNodes[i];
			var alias = childNode.alias;
			var uniqueName = childNode.uniqueName;
			aliasToUniqueNameMap[alias] = uniqueName;
			aliasOrderedByLengthList.push(alias);
		}
		
		// we need to replace first longest aliases in order to avoid replacing confilcts error that occur when one alias is the prefix of another one 
		// ex. 'Profit' & 'Profit per Unit'
		aliasOrderedByLengthList.sort(function(a,b){
			return b.length - a.length;
		});
		
		var uniqueNamesUsedInExpression = [];
		for(var i = 0; i < aliasOrderedByLengthList.length; i++) {
			var alias = aliasOrderedByLengthList[i];
			var uniqueName = aliasToUniqueNameMap[alias];
			
			if(newExpression.indexOf(alias) >= 0) {
				uniqueNamesUsedInExpression.push(uniqueName);
				//alert('replacing all occurrences of [' + alias + '](' + alias.length + ') with [' + uniqueName+ '] in expression [' + newExpression + ']');
				newExpression = newExpression.replace(new RegExp(alias, 'g'), '#' + (uniqueNamesUsedInExpression.length - 1));
				//alert('replacing [' + alias + '] with [' + '#' + (uniqueNamesUsedInExpression.length - 1) +']: ' + newExpression);
				//alert('Results: ' + newExpression);
			}
		}		
		
		for(var i = 0; i < uniqueNamesUsedInExpression.length; i++) {
			var uniqueNameEncoded = uniqueNamesUsedInExpression[i];
			uniqueNameEncoded = uniqueNameEncoded.replace(new RegExp('\\(' , 'g'), '[');
			uniqueNameEncoded = uniqueNameEncoded.replace(new RegExp('\\)' , 'g'), ']');
			newExpression = newExpression.replace(new RegExp('#' + i , 'g'), uniqueNameEncoded);
			//alert('replacing [' + '#' + i + '] with [' + uniqueNameEncoded +']: ' + newExpression);
		}
		
		return newExpression;
	}
	
	, replaceFieldUniqueNamesWithFieldAliases: function(expression) {
		var newExpression;
		
		newExpression = expression;
		var fieldNodes = this.groupRootNodes['fields'];
		var childNodes = fieldNodes.childNodes;
		var uniqueNameToAliasMap = new Object();
		var uniqueNameOrderedByLengthList = new Array();
		
		for(var i = 0; i < childNodes.length; i++) {
			var childNode = childNodes[i];
			var alias = childNode.attributes['alias'];
			var uniqueName = childNode.attributes['uniqueName'];
			uniqueNameToAliasMap[uniqueName] = alias;
			uniqueNameOrderedByLengthList.push(uniqueName);
		}
		
		// we need to replace first longest aliases in order to avoid replacing confilcts error that occur when one alias is the prefix of another one 
		// ex. 'Profit' & 'Profit per Unit'
		uniqueNameOrderedByLengthList.sort(function(a,b){
			return b.length - a.length;
		});
		
		var aliasesUsedInExpression = [];
		for(var i = 0; i < uniqueNameOrderedByLengthList.length; i++) {
			var uniqueName = uniqueNameOrderedByLengthList[i];
			var alias = uniqueNameToAliasMap[uniqueName];
			
			var uniqueNameRegEx = uniqueName;
			uniqueNameRegEx = uniqueNameRegEx.replace(new RegExp('\\(' , 'g'), '\\[');
			uniqueNameRegEx = uniqueNameRegEx.replace(new RegExp('\\)' , 'g'), '\\]');
			
			var uniqueNameEncoded = uniqueName;
			uniqueNameEncoded = uniqueNameEncoded.replace(new RegExp('\\(' , 'g'), '[');
			uniqueNameEncoded = uniqueNameEncoded.replace(new RegExp('\\)' , 'g'), ']');
			
			
			//alert('unique name [' + uniqueName +'] has been decoded [' + uniqueNameEncoded + ']');
			
			if(newExpression.indexOf(uniqueNameEncoded) >= 0) {
				aliasesUsedInExpression.push(alias);
				newExpression = newExpression.replace(new RegExp(uniqueNameRegEx, 'g'), '#' + (aliasesUsedInExpression.length - 1));
			} else {
				//alert('No match for [' + uniqueNameEncoded + '] in expression [' + newExpression + ']');
			}
		}	
		
		for(var i = 0; i < aliasesUsedInExpression.length; i++) {
			var alias = aliasesUsedInExpression[i];
			newExpression = newExpression.replace(new RegExp('#' + i , 'g'), alias);
		}
		
		return newExpression;
	}
	
	, getFormState : function() {      
    	
      	var formState = {};
      	
      	for(p in this.inputFields) {
      		formState[p] = this.inputFields[p].getValue();
      	}
      	
      	if(this.expressionEditor) {
      		formState.expression = this.getExpression();
      	}
      	
      	return formState;
    }
	
	, setTargetRecord: function(record) {
		this.target = record;
		if(this.target) {
			this.inputFields.alias.setValue( record.data.alias );
			this.inputFields.alias.setDisabled(true);
			this.setExpression(record.data.calculatedFieldFormula);
		} else {
			this.inputFields.alias.reset();
			this.inputFields.alias.setDisabled(false);
			this.expressionEditor.reset();
		}
	}
	
	, setTargetNode: function(node) {
		this.target = node;
		if(this.target) {
			var alias;
			var nodeType;
			
			alias =  node.text || node.attributes.text;
			nodeType = node.attributes.type || node.attributes.attributes.type;
			if(nodeType === Sbi.constants.qbe.NODE_TYPE_ENTITY) {
				this.inputFields.alias.reset();
//				this.inputFields.type.reset();
//				this.inputFields.nature.reset();
				this.expressionEditor.reset();
			} else if(nodeType === Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
				Sbi.qbe.commons.unimplementedFunction('handle [field] target');
			} else if(nodeType === Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) {
				this.inputFields.alias.setValue( node.attributes.attributes.formState.alias );
//				this.inputFields.type.setValue( node.attributes.attributes.formState.type );
//				this.inputFields.nature.setValue( node.attributes.attributes.formState.nature );
				this.setExpression.defer(100,this, [node.attributes.attributes.formState.expression] );
			} else if(nodeType === Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
				this.inputFields.alias.setValue( node.attributes.attributes.formState.alias );
//				this.inputFields.type.setValue( node.attributes.attributes.formState.type );
//				this.inputFields.nature.setValue( node.attributes.attributes.formState.nature );
				this.setExpression.defer(100,this,[node.attributes.attributes.formState.expression] );
			} else {
				alert('Impossible to edit node of type [' + nodeType +']');
			} 
		} 	
	}
	
	, setCFAlias: function(alias) {
		this.inputFields.alias.setValue(alias);
	}

//	, validate: function() {
//		if(this.expertMode) {
//			var serviceUrl;
//			var params;
//			if(typeof this.validationService === 'object') {
//				serviceUrl = Sbi.config.serviceRegistry.getServiceUrl(this.validationService);
//				params = this.validationService.params || {};
//			} else {
//				serviceUrl = this.validationService;
//				params = {};
//			}
//			
//			params.expression = this.getExpression();
//			
//			Ext.Ajax.request({
//			    url: serviceUrl,
//			    success: this.onValidationSuccess,
//			    failure: Sbi.exception.ExceptionHandler.handleFailure,	
//			    scope: this,
//			    params: params
//			}); 
//			
//		}else{
//			var error = SQLExpressionParser.module.validateInLineCalculatedField(this.getExpression());
//			if(error==""){
//				Sbi.exception.ExceptionHandler.showInfoMessage(
//						LN('sbi.cockpit.widgets.table.calculatedFields.validationwindow.success.text'), 
//						LN('sbi.cockpit.widgets.table.calculatedFields.validationwindow.success.title'));
//			}else{
//				Sbi.exception.ExceptionHandler.showWarningMessage(error, LN('sbi.cockpit.widgets.table.calculatedFields.validationwindow.fail.title'));
//			}
//		}		
//	}
//	
//	, onValidationSuccess: function(response) {
//		Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.cockpit.widgets.table.calculatedFields.validationwindow.success.text'), LN('sbi.cockpit.widgets.table.calculatedFields.validationwindow.success.title'));
//	}
//	
//	, onValidationFailure: function(response) {
//		
//	}
	//--------------------------------------------------------------------------------------------
	//private methods
	//--------------------------------------------------------------------------------------------

	, initNorthRegionPanel: function(c) {
		this.initDetailsFormPanel(Ext.apply({}, {
			region:'north',
    		split: false,
    		frame:false,
    		border:false,
    		height: 70,
    	    bodyStyle:'padding:5px;background:#E8E8E8;border-width:1px;border-color:#D0D0D0;',
    	    style: 'padding-bottom:3px'
		}, c || {}));
		
		this.northRegionPanel = this.detailsFormPanel;
	}
	
	, initWestRegionPanel: function(c) {		
	    this.initExpItemsPanel(Ext.apply({}, {
	    	region:'west',
	    	title: 'Items',
			layout: 'fit',
			split: true,
			collapsible: true,
			autoScroll: true,
		    frame: false, 
		    border: true,
//		    width: 120,
		    width: '25%',
		    minWidth: 120
		}, c || {}));
		
		this.westRegionPanel = this.expItemsPanel;
	}
	
	, initCenterRegionPanel: function(c) {
		this.initExpressionEditorPanel(Ext.apply({}, {
			region:'center',
		    frame: false, 
		    border: false
		}, c || {}));
		
		this.centerRegionPanel = this.expressionEditorPanel;
	}
	

	// details form
	, initDetailsFormPanel: function(c) {
		
		var fieldsWidth = 150;
		var fieldsLabelWidth = 40;
		var fieldsMargins = '0 30 0 0';
	
		if(this.inputFields === null) {
			this.inputFields = new Object();
		}
		
		this.inputFields['alias'] = new Ext.form.TextField({
    		name: 'alias',
    		allowBlank: false, 
    		inputType: 'text',
    		maxLength: 50,
    		width: fieldsWidth,
    		labelWidth: fieldsLabelWidth,
    		margin: fieldsMargins,
    		fieldLabel: 'Alias' 
    	});
    	var aliasPanel = new  Ext.form.FormPanel({
    		bodyStyle: "background-color: transparent; border-color: transparent",
    		items: [this.inputFields['alias'] ]
    	});
    	
    	var scopeComboBoxStore = new Ext.data.SimpleStore({
    		fields: ['value', 'field', 'description'],
    		data: this.scopeComboBoxData 
    	});  
    	
//    	var natureComboBoxStore = new Ext.data.SimpleStore({
//    		fields: ['value', 'field', 'description'],
//    		data: [
//    		    ['ATTRIBUTE', 'Attribute', 'Attribite is ...']
//    		    , ['MEASURE', 'Measure', 'Measure is ...']
//    		] 
//    	});  
    	
//    	this.inputFields['type'] = new Ext.form.ComboBox({
//    		tpl: '<tpl for="."><div ext: qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',	
//    		editable: false,
//    		fieldLabel: 'Type',
//    		forceSelection: true,
//    		allowBlank: false,
//    		mode: 'local',
//    		name: 'scope',
//    		store: scopeComboBoxStore,
//    		displayField: 'field',
//    		valueField: 'value',
//    		//value: 'STRING',
//    		emptyText: 'Select type...',
//    		typeAhead: true,
//    		triggerAction: 'all',
//    		width: fieldsWidth,
//    		labelWidth: fieldsLabelWidth,
//    		margin: fieldsMargins,
//    		selectOnFocus: true,
//    		value: 'STRING'
//    	});
//    	
//    	var typePanel = new  Ext.form.FormPanel({
//    		bodyStyle: "background-color: transparent; border-color: transparent",
//    		items: [this.inputFields['type'] ]
//    	});
    	
//    	this.inputFields['nature'] = new Ext.form.ComboBox({
//    		tpl: '<tpl for="."><div ext:qtip="{field}: {description}" class="x-combo-list-item">{field}</div></tpl>',	
//    		editable: false,
//    		fieldLabel: 'Nature',
//    		forceSelection: true,
//    		allowBlank: false,
//    		mode: 'local',
//    		name: 'scope',
//    		store: natureComboBoxStore,
//    		displayField: 'field',
//    		valueField: 'value',
//    		//value: 'ATTRIBUTE',
//    		emptyText: 'Select nature...',
//    		typeAhead: true,
//    		triggerAction: 'all',
//    		width: fieldsWidth,
//    		labelWidth: fieldsLabelWidth,
//    		margin: fieldsMargins,
//    		selectOnFocus: true,
//    		value: 'ATTRIBUTE'
//    	});
//    	
//    	var naturePanel = new  Ext.form.FormPanel({
//    		bodyStyle: "background-color: transparent; border-color: transparent; padding-left: 10px;",
//    		width: 280,
//    		items: [this.inputFields['nature']]
//    	});

    	this.detailsFormPanel = new Ext.Panel(
	    	 Ext.apply({
		    	 layout: 'table',
		    	 layoutConfig: {
//		    		 columns: 3
//		    		 columns: 2
			        columns: 1
			     },
	    		 items: [
	    		     aliasPanel,
//	    		     typePanel,
//	    		     naturePanel
	    		 ]
	    	 }, c || {})

    	);

	}
	
	
	// items tree
	, setExpItems: function(itemGroupName, items) {
		
		var groupRootNode = this.groupRootNodes[itemGroupName];
		var oldChildren = new Array();
		
		Ext.Array.each(groupRootNode.children, function(child, index) {
			oldChildren.push(child);
		});
		
		this[itemGroupName] = items;
		
		groupRootNode.children = [];
		
		for(var j = 0; j < items.length; j++) {
			var node = new Ext.tree.TreeNode(items[j]);
			Ext.apply(node.attributes, items[j]);
//			groupRootNode.appendChild( node );
			node.leaf = (node.children == undefined || node.children == null);
			groupRootNode.children.push( node );
		}
	}

	, initExpItemsPanel: function(c) {
		
		this.expItemsTreeRootNode = new Ext.tree.TreeNode({text:'Exp. Items', iconCls:'database',expanded:true});
		
		var children = [];
		
		if(this.expItemGroups) {
			this.groupRootNodes = new Object();
			for(var i = 0; i < this.expItemGroups.length; i++) {
				var groupName = this.expItemGroups[i].name;
				this.groupRootNodes[groupName] = new Ext.tree.TreeNode(Ext.apply({}, {leaf: false}, this.expItemGroups[i]));
				children.push(this.groupRootNodes[groupName]);
				
				if(this[groupName] != null) {
					this.setExpItems(groupName, this[groupName]);
				}
			}
		}
		
		this.expItemsTreeRootNode.children = children;
		
		this.expItemsTreeStore = new Ext.data.TreeStore({
    		root: this.expItemsTreeRootNode,
    		proxy: {
    	        type: 'localstorage'
    	    }
    	})
		
		var ddGroup = this.ddGroup;
	    this.expItemsTree = new Ext.tree.Panel({
	    	store: this.expItemsTreeStore,
	        enableDD: false,
	        expandable: true,
//	        collapsible: true,
	        autoHeight: true ,
	        bodyBorder: false ,
	        leaf: false,
	        lines: true,
	        layout: 'fit',
	        animate: true,
	        viewConfig: {
	            plugins: new Ext.tree.plugin.TreeViewDragDrop ({
	            	ddGroup : ddGroup,
	            	dragGroup : ddGroup,
	            	dropGroup : ddGroup,
	            	enableDrop : false
	            })
	        }
	    });
	    
		this.expItemsPanel = new Ext.Panel(
			Ext.apply({
				 title: 'Items',
				 layout: 'fit',
			 	 items: [this.expItemsTree]
			}, c || {}) 
		);
		
		this.expItemsTree.on('click', function(node, e) {
			this.addElementToEditor(node, this.expressionEditor);
		}, this);		
	}
	
	// expression editor
	, initExpressionEditorPanel: function(c) {
		
		var buttons = {};
		buttons.clear = new Ext.button.Button({
		    text: 'Clear All',
		    tooltip: 'Clear all selected fields',
//		    iconCls: 'remove'
		    iconCls: 'delete'
		});
		buttons.clear.addListener('click', function(){this.expressionEditor.reset();}, this);
		
		var ddGroup = this.ddGroup;
		
		this.expressionEditor = new Ext.form.HtmlEditor({
    		name: 'expression',
    	    frame: true,
    	    enableAlignments: false,
    	    enableColors: false,
    	    enableDragDrop: true,
    	    enableFont: false,
    	    enableFontSize: false, 
    	    enableFormat: false,
    	    enableLinks: false,
    	    enableLists: false,
    	    enableSourceEdit: false,
    	    
    	    listeners:{
    	    	'render': function(editor){
					var tb = editor.getToolbar();
					tb.add(buttons.clear);
//					tb.add(buttons.validate);
    	        },
    	        'activate': function(){
    	          //active = true;
    	        },
    	        'initialize': {
    	        	fn: function(){
    	        		this.expressionEditor.onFirstFocus();
    	        		this.expressionEditor.insertAtCursor(this.baseExpression) ; 
	    	        } , scope: this
    	        }
    	    }
    	});
		var expressionEditor = this.expressionEditor;
		
		var expressionEditorPanel = new Ext.form.Panel(
			Ext.apply({
				layout: 'fit',
			    items: [this.expressionEditor],
			}, c || {}) 
		);
		
		this.expressionEditorPanel = expressionEditorPanel;
		var addElementToEditor = this.addElementToEditor;
		
		expressionEditor.on('render', function(v) {
//			expressionEditor.dropZone = new Ext.dd.DropZone(v.getEl(), {
			expressionEditor.dropZone = new Ext.dd.DropTarget(v.getEl(), {
				ddGroup : ddGroup,
				
				notifyDrop: function(ddSource, e, data){
					var node = (data && data.records && data.records.length > 0 && data.records[0])?
			        		data.records[0] : null;
					
	                return node == null? 
	                		false : 
	                			addElementToEditor(node, expressionEditor);
				},
//		        notifyEnter: function (ddSource, event, data) {
//
//	            }
		    });
		});
	}
	, addElementToEditor: function(node, target) {
//        console.log('node', node);
		
        var record = node.raw? node.raw : null;
        
        if (record == null || record.leaf == false) {
        	return false;
        } else {
        	target.insertAtCursor((record.id? record.id : record.value ) + '&nbsp;');
//        			'<div recordId="' + (record.id? record.id : record.value ) + '"> ' + record.alias + '</div>');
//        	
//        	var prevValue = target.getValue() + '&nbsp;';
//        	
//        	target.setValue(prevValue + 
////        			'<span recordId="' + (record.id? record.id : record.value ) + '"> ' + record.alias + '</span>');
//        			(record.id? record.id : record.value ) + '&nbsp;');
        }
	}	
});
