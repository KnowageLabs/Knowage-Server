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

Sbi.widgets.ConfigurableTree = function(config) { 
	
	this.treeLoader = config.treeLoader;
	this.nodeCount=0;
    var c =  {
    	border: false,
		autoWidth : true,
		height : 300,
		layout: 'fit',
		userArrows : true,
		animate : true,
		autoScroll : true,		
        style: {
            "border":"none"
        },
		loader: this.treeLoader,

		preloadTree : this.preloadTree,
		enableDD : false,
        enableDrop: false,
        enableDrag: false,
        ddAppendOnly: false ,
        ddGroup  : 'tree2tree',
		scope : this,
		shadow : true,
		root : config.rootNode
	};
    
    Sbi.widgets.ConfigurableTree.superclass.constructor.call(this, c );	
    
    this.initWidget();
	
};

Ext.extend(Sbi.widgets.ConfigurableTree, Ext.tree.TreePanel, {
	 treeLoader: null
	 ,nodeCount: null
	
	,initWidget: function(){
		this.initContextMenu();
		this.addListeners();
	}
	
	,addListeners: function(){
		//add the editor
		var field = new Ext.form.TextField();
		var treeEditor = new Ext.tree.TreeEditor(this,field);
		treeEditor.on('complete', function(a,newValue,oldValue){
			this.fireEvent('changedNodeName',newValue,oldValue);
		},this)
		this.on('contextmenu', this.onContextMenu, this);
	}
	
	, addNewItem : function(parent) {
		if (parent === undefined || parent == null) {
			alert(LN('sbi.models.DDNoParentMsg'));
			return;
		} else {
			parent.leaf = false;
		}
		var node = new Ext.tree.TreeNode( {
			text : '...',
	        leaf : true,
	        count : this.nodeCount,
	        parentCount: parent.parentCount,
	        editable: true
	    });
		
		this.fireEvent('addedNewItem',node,parent);

		parent.appendChild(node);
	}	
	
	, removeNode : function(node) {
		if (parent === undefined || parent == null) {
			alert(LN('sbi.models.DDNoParentMsg'));
			return;
		} 
		this.fireEvent('removedItem',node);
		
		node.parentNode.removeChild(node, false);
	}
	
	, initContextMenu : function() {

		this.menu = new Ext.menu.Menu( {
			items : [
			// ACID operations on nodes
					'-', {
						text : LN('sbi.models.addNode'),
						iconCls : 'icon-add',
						handler : function() {
							this.addNewItem(this.ctxNode);
						},
						scope : this
					}, {
						text : LN('sbi.models.remodeNode'),
						iconCls : 'icon-remove',
						handler : function() {
							this.removeNode(this.ctxNode);
						},
						scope : this
					} ]
		});

	}
	
	,onContextMenu : function(node, e) {
		if (this.menu == null) { // create context menu on first right click
			this.initContextMenu();
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


