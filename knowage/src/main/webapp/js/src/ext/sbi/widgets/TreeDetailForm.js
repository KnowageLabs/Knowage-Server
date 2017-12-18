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
 * Authors - Monica Franceschini
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.TreeDetailForm = function(config) {

	this.hideContextMenu = config.hideContextMenu;

	var conf = config.configurationObject;
	this.services = new Array();
	this.services['manageTreeService'] = conf.manageTreeService;
	this.services['saveTreeService'] = conf.saveTreeService;
	this.services['deleteTreeService'] = conf.deleteTreeService;
	
	this.tabItems = conf.tabItems;

	this.treeTitle = conf.treeTitle;
	this.ddGroup = conf.dragndropGroup;
	
	this.initWidget();
	if(this.hideContextMenu === undefined || this.hideContextMenu == null || this.hideContextMenu != true ){
		this.initContextMenu();
	}

	var c = Ext.apply( {}, config, this.gridForm);

	Sbi.widgets.TreeDetailForm.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.widgets.TreeDetailForm, Ext.FormPanel, {

	gridForm : null,
	tabs : null,
	tabItems : null,
	treeLoader : null,
	rootNode : null,
	rootNodeId : null,
	preloadTree : true,
	rootNodeText : null,
	treeTitle : null,
	menu : null,
	hideContextMenu: null,
	ddGroup : null, //for dragndrop
	
	nodesToSave : new Array(),
	selectedNodeToEdit : null,
	

	initWidget : function() {

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

		this.tabs = new Ext.TabPanel( {
			enableTabScroll : true,
			activeTab : 0,
			columnWidth : 0.6,
			autoScroll : true,
			width : 450,
			height : 520,
			itemId : 'tabs',
			items : this.tabItems
		});

		this.mainTree = new Ext.tree.TreePanel( {
			title : this.treeTitle,
			width : 200,
			height : 520,
			userArrows : true,
			animate : true,
			autoScroll : true,
			ddGroup: this.ddGroup,
            style: {
                "background-color": "white"
            },
			loader: this.kpitreeLoader,

			preloadTree : this.preloadTree,
			enableDD : true,
            enableDrop: true,
            ddAppendOnly: false ,
			scope : this,
			shadow : true,
			tbar : this.tbSave,
			root : {
				nodeType : 'async',
				text : this.rootNodeText,
				modelId : this.rootNodeId,
				id:  this.rootNodeId
			}
		   ,listeners:{
		   }
		});
		
		this.mainTree.on('contextmenu', this.onContextMenu, this);
		this.mainTree.on('beforenodedrop', this.dropNodeBehavoiur, this);

		/*
		 * Here is where we create the Form
		 */
		this.gridForm = new Ext.FormPanel( {
			frame : true,
			autoScroll : true,
			labelAlign : 'left',
			title : this.panelTitle,
			//width : 600,
			height : 550,
			layout : 'column',
			layoutConfig : {
				animate : true,
				activeOnTop : false

			},
			trackResetOnLoad : true,
			items : [ {
				region : 'west',
				collapseMode : 'mini',
				layout : 'fit',
				columnWidth: 0.4,
				//width : 200,
				items : this.mainTree
			}, {
				border : false,
				frame : false,
				collapseMode : 'mini',
				split : true,
				region : 'center',
				columnWidth: 0.6,
				layout : 'fit',
				items : this.tabs
			} ]
		});
		this.setListeners();

	},
	save : function() {
		alert("Overridden");
		
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
		
	},
	onContextMenu : function(node, e) {
		if (this.menu == null) { // create context menu on first right click
			if(this.hideContextMenu === undefined || this.hideContextMenu == null || this.hideContextMenu != true ){
				this.initContextMenu();
			}else
				return;

		}
		
		if (this.ctxNode && this.ctxNode.ui) {
			this.ctxNode.ui.removeClass('x-node-ctx');
			this.ctxNode = null;
		}
		
		this.ctxNode = node;
		this.ctxNode.ui.addClass('x-node-ctx');
		this.menu.showAt(e.getXY());
	
	}

});

