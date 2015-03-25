/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  

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
