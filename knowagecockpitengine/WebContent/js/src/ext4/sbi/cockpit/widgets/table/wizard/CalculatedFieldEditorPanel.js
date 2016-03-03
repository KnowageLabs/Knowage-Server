/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

/**
 * CalculatedFieldEditorPanel - short description
 *
 * Object documentation ...
 *
 * @author Monica Franceschini
 * @author Benedetto Milazzo
 */

Ext.ns("Sbi.cockpit.widgets.table.wizard");

Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel = function (config) {

	var c = Ext.apply({}, config || {}, {
		layout: 'border',
		frame: false,
		border: false,
		bodyStyle: 'background:#E8E8E8;',
		padding: 3,
		closeAction: 'destroy'
	});

	Ext.apply(this, c);

	this.ddGroup = 'drag&drop';

	this.expertDisable = c.expertDisable;

	this.initNorthRegionPanel(c.northRegionConfig || {});
	this.initWestRegionPanel(c.westRegionConfig || {});
	this.initCenterRegionPanel(c.centerRegionConfig || {});

	Ext.apply(c, {
		items : [this.westRegionPanel, this.centerRegionPanel, this.northRegionPanel]
	});

	// constructor
	Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.cockpit.widgets.table.wizard.CalculatedFieldEditorPanel, Ext.Panel, {
	hasBuddy : null,
	buddy : null,
	expertDisable : false,
	westRegionPanel : null,
	centerRegionPanel : null,
	northRegionPanel : null,
	detailsFormPanel : null,
	
	expItemsTreeRootNode : null,
	expItemsTreeStore : null,
	expItemsTree : null,
	expItemsPanel : null,
	
	baseExpression : '',
	expressionEditor : null,
	expressionEditorPanel : null,
	inputFields : null,
//	expertCheckBox: null,

	target : null,

//	validationService:  null,
	expertMode : false,
	expItemGroups : null,
	groupRootNodes : null,
	scopeComboBoxData : null,
	opWin : null,

	// --------------------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------------------

	getExpression : function () {
		var expression;
		if (this.expressionEditor) {
			expression = this.expressionEditor.getValue();
			
	  		expression = Ext.util.Format.stripTags( expression );
			expression = expression.replace(/&nbsp;/g, " ");
			expression = expression.replace(/\u200B/g, "");
		}
		return expression;
	},
	
	setExpression : function (expression) {
		if (this.expressionEditor) {
			expression = expression.replace(/ /g, "&nbsp;");
			expression = expression.replace(/</g, "&lt;");
			expression = expression.replace(/>/g, "&gt;");

			this.expressionEditor.setValue(expression);
		}
	},
	
	getFormState : function () {
		var formState = {};

		for (p in this.inputFields) {
			formState[p] = this.inputFields[p].getValue();
		}

		if (this.expressionEditor) {
			formState.expression = this.getExpression();
		}

		return formState;
	},
	
	setTargetRecord : function (record) {
		this.target = record;
		if (this.target) {
			this.inputFields.alias.setValue(record.data.alias);
			this.inputFields.alias.setDisabled(true);
			
			this.baseExpression = record.data.calculatedFieldFormula
			this.setExpression(record.data.calculatedFieldFormula);
			
		} else {
			this.inputFields.alias.reset();
			this.inputFields.alias.setDisabled(false);
			this.expressionEditor.reset();
		}
	},
	
	setTargetNode : function (node) {
		this.target = node;
		if (this.target) {
			var alias;
			var nodeType;

			alias = node.text || node.attributes.text;
			nodeType = node.attributes.type || node.attributes.attributes.type;
			if (nodeType === Sbi.constants.qbe.NODE_TYPE_ENTITY) {
				this.inputFields.alias.reset();
//				this.inputFields.type.reset();
//				this.inputFields.nature.reset();
				this.expressionEditor.reset();
			} else if (nodeType === Sbi.constants.qbe.NODE_TYPE_SIMPLE_FIELD) {
				Sbi.qbe.commons.unimplementedFunction('handle [field] target');
			} else if (nodeType === Sbi.constants.qbe.NODE_TYPE_CALCULATED_FIELD) {
				this.inputFields.alias.setValue(node.attributes.attributes.formState.alias);
//				this.inputFields.type.setValue( node.attributes.attributes.formState.type );
//				this.inputFields.nature.setValue( node.attributes.attributes.formState.nature );
				this.setExpression.defer(100, this, [node.attributes.attributes.formState.expression]);
			} else if (nodeType === Sbi.constants.qbe.NODE_TYPE_INLINE_CALCULATED_FIELD) {
				this.inputFields.alias.setValue(node.attributes.attributes.formState.alias);
//				this.inputFields.type.setValue( node.attributes.attributes.formState.type );
//				this.inputFields.nature.setValue( node.attributes.attributes.formState.nature );
				this.setExpression.defer(100, this, [node.attributes.attributes.formState.expression]);
			} else {
				alert('Impossible to edit node of type [' + nodeType + ']');
			}
		}
	}
	
	, setCFAlias : function (alias) {
		this.inputFields.alias.setValue(alias);
	}
	
	, initNorthRegionPanel : function (c) {
		this.initDetailsFormPanel(Ext.apply({}, {
				region : 'north',
				split : false,
				frame : false,
				border : false,
				height : 70,
				bodyStyle : 'padding:5px;background:#E8E8E8;border-width:1px;border-color:#D0D0D0;',
				style : 'padding-bottom:3px'
			}, c || {}));

		this.northRegionPanel = this.detailsFormPanel;
	},
	
	initWestRegionPanel : function (c) {
		this.initExpItemsPanel(Ext.apply({}, {
			region : 'west',
			title : 'Items',
			layout : 'fit',
			split : true,
			collapsible : true,
			autoScroll : true,
			frame : false,
			border : true,
//			width: 120,
			width : '25%',
			minWidth : 120
		}, c || {}));

		this.westRegionPanel = this.expItemsPanel;
	},
	
	initCenterRegionPanel : function (c) {
		this.initExpressionEditorPanel(Ext.apply({}, {
				region : 'center',
				frame : false,
				border : false
			}, c || {}));

		this.centerRegionPanel = this.expressionEditorPanel;
	}

	// details form
	, initDetailsFormPanel : function (c) {

		var fieldsWidth = 150;
		var fieldsLabelWidth = 40;
		var fieldsMargins = '0 30 0 0';

		if (this.inputFields === null) {
			this.inputFields = new Object();
		}

		this.inputFields['alias'] = new Ext.form.TextField({
			name : 'alias',
			allowBlank : false,
			inputType : 'text',
			maxLength : 50,
			width : fieldsWidth,
			labelWidth : fieldsLabelWidth,
			margin : fieldsMargins,
			fieldLabel : 'Alias'
		});
		var aliasPanel = new Ext.form.FormPanel({
			bodyStyle : "background-color: transparent; border-color: transparent",
			items : [this.inputFields['alias']]
		});

		var scopeComboBoxStore = new Ext.data.SimpleStore({
				fields : ['value', 'field', 'description'],
			data : this.scopeComboBoxData
		});

		this.detailsFormPanel = new Ext.Panel(
			Ext.apply({
				layout : 'table',
				layoutConfig : {
					columns : 1
				},
				items : [
					aliasPanel,
				]
			}, c || {}));
	}

	// items tree
	, setExpItems : function (itemGroupName, items) {

		var groupRootNode = this.groupRootNodes[itemGroupName];
		var oldChildren = new Array();

		Ext.Array.each(groupRootNode.children, function (child, index) {
			oldChildren.push(child);
		});

		this[itemGroupName] = items;

		groupRootNode.children = [];

		for (var j = 0; j < items.length; j++) {
			var node = new Ext.tree.TreeNode(items[j]);
			Ext.apply(node.attributes, items[j]);
//			groupRootNode.appendChild( node );
			node.leaf = (node.children == undefined || node.children == null);
			groupRootNode.children.push(node);
		}
	},
	
	initExpItemsPanel : function (c) {
		this.expItemsTreeRootNode = new Ext.tree.TreeNode({
			text : 'Exp. Items',
			iconCls : 'database',
			expanded : true
		});

		var children = [];

		if (this.expItemGroups) {
			this.groupRootNodes = new Object();
			for (var i = 0; i < this.expItemGroups.length; i++) {
				var groupName = this.expItemGroups[i].name;
				
				this.groupRootNodes[groupName] = 
					new Ext.tree.TreeNode(Ext.apply({}, {
							leaf : false
						}, this.expItemGroups[i]));
				
				children.push(this.groupRootNodes[groupName]);

				if (this[groupName] != null) {
					this.setExpItems(groupName, this[groupName]);
				}
			}
		}

		this.expItemsTreeRootNode.children = children;

		this.expItemsTreeStore = new Ext.data.TreeStore({
			root : this.expItemsTreeRootNode,
			proxy : {
				type : 'localstorage'
			}
		})

		var ddGroup = this.ddGroup;
		this.expItemsTree = new Ext.tree.Panel({
			store : this.expItemsTreeStore,
			enableDD : false,
			expandable : true,
//	        collapsible: true,
			autoHeight : true,
			bodyBorder : false,
			leaf : false,
			lines : true,
			layout : 'fit',
			animate : true,
			viewConfig : {
				plugins : new Ext.tree.plugin.TreeViewDragDrop({
					ddGroup : ddGroup,
					dragGroup : ddGroup,
					dropGroup : ddGroup,
					enableDrop : false
				})
			}
		});

		this.expItemsPanel = new Ext.Panel(
			Ext.apply({
				title : 'Items',
				layout : 'fit',
				items : [this.expItemsTree]
			}, c || {}));

		this.expItemsTree.on('itemclick', function (tree, record, item, index, e, eOpts ) {
			this.addElementToEditor(record, this.expressionEditor);
		}, this);
	}

	// expression editor
	, initExpressionEditorPanel : function (c) {

		var buttons = {};
		buttons.clear = new Ext.button.Button({
			text : 'Clear All',
			tooltip : 'Clear all selected fields',
//		    iconCls: 'remove'
			iconCls : 'delete'
		});
		
		buttons.clear.addListener('click', function () {
			this.expressionEditor.reset();
		}, this);

		var ddGroup = this.ddGroup;

		this.expressionEditor = new Ext.form.HtmlEditor({
			name : 'expression',
			frame : true,
			enableAlignments : false,
			enableColors : false,
			enableDragDrop : true,
			enableFont : false,
			enableFontSize : false,
			enableFormat : false,
			enableLinks : false,
			enableLists : false,
			enableSourceEdit : false,

			listeners : {
				'render' : function (editor) {
					console.log('render');
					var tb = editor.getToolbar();
					tb.add(buttons.clear);
//					tb.add(buttons.validate);
				},
				'initialize' : {
					fn : function () {
						this.expressionEditor.onFirstFocus();
					},
					scope : this
				}
			}
		});
		
		var expressionEditor = this.expressionEditor;

		var expressionEditorPanel = new Ext.form.Panel(
			Ext.apply({
				layout : 'fit',
				items : [this.expressionEditor],
			}, c || {})
		);

		this.expressionEditorPanel = expressionEditorPanel;
		var addElementToEditor = this.addElementToEditor;

		expressionEditor.on('render', function (v) {
//			expressionEditor.dropZone = new Ext.dd.DropZone(v.getEl(), {
			expressionEditor.dropZone = new Ext.dd.DropTarget(v.getEl(), {
				ddGroup : ddGroup,

				notifyDrop : function (ddSource, e, data) {
					var node = (data && data.records && data.records.length > 0 && data.records[0]) ?
					data.records[0] : null;

					return node == null ?
					false : addElementToEditor(node, expressionEditor);
				},
//		        notifyEnter: function (ddSource, event, data) {
//
//	            }
			});
		});
	},
	
	addElementToEditor : function (node, target) {
		var customCheck = function (regex) {
			return regex.test(Ext.userAgent);
		},
		customDocMode = document.documentMode,
		customIsOpera = customCheck(/opera/),
		customIsIE = !customIsOpera && (customCheck(/msie/) || customCheck(/trident/)),
		customIsIE11 = customIsIE && 
			((customCheck(/trident\/7\.0/) && customDocMode != 7 && customDocMode != 8 && customDocMode != 9 && customDocMode != 10) || customDocMode == 11);
		
		var record = node.raw ? node.raw : null;

		if (record == null || record.leaf == false) {
			return false;
		} else {
			var oldValue = target.getValue();
			target.insertAtCursor('&nbsp;' + (record.id ? record.id : record.value) + '&nbsp;');
			
			if(customIsIE) {
				var newValue = target.getValue();
				
				if(newValue == oldValue) { // the component has not be focused
					//add at the end of the text area
					target.reset();
					target.setValue(oldValue + '&nbsp;' + (record.id ? record.id : record.value) + '&nbsp;');
				}
			}
			
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