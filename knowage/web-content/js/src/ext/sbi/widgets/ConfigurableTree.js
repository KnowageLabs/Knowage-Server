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


