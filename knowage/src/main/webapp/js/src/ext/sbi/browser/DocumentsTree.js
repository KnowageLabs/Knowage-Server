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
 * 
 * @author Andrea Gioia (andrea.gioia@eng.it)
 */

Ext.ns("Sbi.browser");

Sbi.browser.DocumentsTree = function(config) {    

	// always declare exploited services first!
	this.services = new Array();
	this.services['loadFTreeFoldersService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FTREE_FOLDERS_ACTION'
		, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
		}
	});
	this.services['loadFTreeFoldersFilteredService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FTREE_FOLDERS_ACTION'
		, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE',PERMISSION_ON_FOLDER: 'CREATION'
		}
	});
	// -----------------------------------------
	this.checkedIdNodes = new Array();
	
	if(config.drawUncheckedChecks == true){
		this.loader = new Ext.tree.TreeLoader({
	        dataUrl   : this.services['loadFTreeFoldersFilteredService'],
	        scope: this,
			createNode: this.createnode 
	    });
	}else{
		this.loader = new Ext.tree.TreeLoader({
	        dataUrl   : this.services['loadFTreeFoldersService']
	    });
	}
	
	this.loader.on('loadexception', function(loader, node, response) {
		Sbi.exception.ExceptionHandler.handleFailure(response);
	});
		
    var c = Ext.apply({}, config, {
    	title            : LN('sbi.browser.documentstree.title'),
    	bodyStyle		 :'padding:6px 6px 6px 6px; background-color:#FFFFFF',
        collapsible      : true,
        enableDD		 : true,
        animCollapse     : true,
        collapseFirst	 : false,
        border           : false,
        autoScroll       : true,
        containerScroll  : true,
        animate          : false,
        trackMouseOver 	 : true,
        useArrows 		 : true,
        loader           : this.loader
    })
    
    Sbi.browser.DocumentsTree.superclass.constructor.call(this, c);
    
    this.rootNodeId = config.rootNodeId || 'rootNode';    
    this.rootNode = new Ext.tree.AsyncTreeNode({
    	id			: this.rootNodeId,
        text		: LN('sbi.browser.documentstree.root'),
        iconCls		: 'icon-ftree-root',
        expanded	: true,
        expandable  : true,
        draggable	: false
    });    
    this.setRootNode(this.rootNode);    
    
    this.addListener('checkchange', this.updateCheckedNodesArray, this);
}


Ext.extend(Sbi.browser.DocumentsTree, Ext.tree.TreePanel, {
    
	services: null
	, loader: null
	, rootNode: null
	, rootNodeId: null
	, checkedIdNodes: null
	
	, refresh: function() {
		this.loader.load(this.rootNode, function(){});
	}

	, createnode: function(attr) {			
		attr.checked = false;
		var node = Ext.tree.TreeLoader.prototype.createNode.call(this, attr);
		return node;
	}

	, updateCheckedNodesArray: function(node, checked) {
		var nodeId = node.id;
		if (this.checkedIdNodes.indexOf(nodeId)!= -1){
			this.checkedIdNodes.remove(nodeId);
		}else{
			this.checkedIdNodes.push(nodeId);
		}
	}

	, returnCheckedIdNodesArray: function() {
		return this.checkedIdNodes;
	}
   
});
