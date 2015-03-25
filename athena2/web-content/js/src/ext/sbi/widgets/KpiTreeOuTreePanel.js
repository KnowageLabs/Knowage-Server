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
 * Authors - Alberto Ghedin
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.KpiTreeOuTreePanel = function(config) {

	var conf = config.configurationObject;
	this.tabItems = conf.tabItems;
	this.OUTreeTitle = conf.OUTreeTitle;
	this.kpiTreeTitle = conf.kpiTreeTitle;

	this.initWidget(config);

	var c = Ext.apply( {}, config);

	c.items = this.tabItems;
	this.activeTab =0;

	Sbi.widgets.KpiTreeOuTreePanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.widgets.KpiTreeOuTreePanel, Ext.TabPanel, {

	preloadTree : true,
	rootNodeLeftText : null,
	rootNodeLeftId : null,
	rootNodeLeftText : null,
	rootNodeRightText : null,
	rootNodeRightId : null,
	menu : null,
	toolsMenu: null,
	tabItems: null,
	tabPanel: null,
	treePanel: null,


	initWidget : function(config) {

		this.tbSave = new Ext.Toolbar( {
			buttonAlign : 'right',
			items : [ new Ext.Toolbar.Button( {
				text : LN('sbi.generic.update'),
				iconCls : 'icon-save',
				handler : this.save,
				width : 30,
				scope : this
			}) ]
		});

		this.leftTree = new Ext.tree.TreePanel( {
			title : this.OUTreeTitle,
			layout : 'fit',
			userArrows : true,
			animate : true,
			autoScroll : true,		
			style: {
				"background-color": "white",
				"height": "100%"
			},
			cellCls: 'kpi-ou-trees',
			loader: this.outreeLoader,
			preloadTree : this.preloadTree,
			enableDD : true,
			enableDrop: true,
			ddAppendOnly: false ,
			scope : this,
			shadow : true,
			root : {
				nodeType : 'async',
				text : this.rootNodeLeftText,
				modelId : this.rootNodeLeftId,
				id:  this.rootNodeLeftId
			}
			,listeners:{
			}
		});


		this.rightTree = new Ext.tree.TreePanel( {
			title : this.kpiTreeTitle,
			cellCls: 'kpi-ou-trees',
			layout : 'fit',
			userArrows : true,
			animate : true,
			autoScroll : true,		
			style: {
				"background-color": "white",
				"height": "100%"
			},
			loader: this.kpitreeLoader,
			preloadTree : this.preloadTree,
			enableDD : true,
			enableDrop: true,
			ddAppendOnly: false ,
			scope : this,
			shadow : true,
			root : {
				nodeType : 'async',
				text : this.rootNodeRightText,
				modelId : this.rootNodeRightId,
				id:  this.rootNodeRightId,
				checked: true
			}
			,listeners:{
			}
		});

		this.leftTree.on('beforenodedrop', this.dropNodeBehavoiur, this);
		this.rightTree.on('beforenodedrop', this.dropNodeBehavoiur, this);	

		this.treePanel = new Ext.Panel({
			border: true,
			layout:'table',
            layoutConfig: {
                columns: 2,
                rows: 1
            },
            autoScroll : true,	
			region : 'center',
			border : true,
			collapseMode : 'mini',
			style: {
				"background-color": "white"
			},
			split : true,
			items : [this.leftTree, this.rightTree]
		});

		var gridItems =[this.treePanel];

		//south panel with the tools
		if(config.toolsMenuItems!=null){
			var panel = new Ext.Panel({
				border : true,
				collapseMode : 'mini',
				split : true,
				region : 'south',
				height: 75,
				collapsed: true,
				items : config.toolsMenuItems
			});
			gridItems.push(panel);
		};

		this.tabPanel = new Ext.FormPanel({
			id: 'treePanel',
			title: LN('sbi.grants.trees.tab')
			, tbar: this.tbSave
			, width: 430
			, layout: 'fit'
				, items: [ new Ext.Panel( {
					frame : true,
					autoScroll : true,
					labelAlign : 'left',
					width : 600,
					height : 550,
					layout : 'border',
					items : gridItems
				})]});

		this.tabItems.push(this.tabPanel);
		this.setListeners();
	},

	//init the tools in the south of the panel
	initToolsMenu: function(conf){
		this.toolsMenu = new Ext.form.FormPanel({
			items: conf.toolsMenuItems
		});
	},

	createNewRootNode: function() {
		var node = new Ext.tree.AsyncTreeNode({
			text		: '... - ...',
			expanded	: false,
			leaf		: false,	        
			draggable	: false
		});
		return node;
	},

	getSubTreeNodes: function(node){
		var children = node.childNodes;
		var subtreeNodes = new Array();
		subtreeNodes.push(node);
		for(var i=0; i<children.length; i++){
			subtreeNodes = subtreeNodes.concat(this.getSubTreeNodes(children[i]));
		}
		return subtreeNodes;	
	},

	fillDetail : function(sel, node) {
		alert("override");
	},
	renderTree : function(tree) {
		alert("override");
	},

	editNode : function(field, newVal, oldVal) {
		alert("override");
	},

	addNewItem : function(parent) {
		alert('overridden');
	},
	deleteItem : function(node) {
		alert('overridden');

	}

});

