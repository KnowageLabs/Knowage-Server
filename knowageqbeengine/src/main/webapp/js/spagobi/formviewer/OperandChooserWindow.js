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
  */

Ext.ns("Sbi.qbe");

Sbi.qbe.OperandChooserWindow = function(config) {
	
	var c = Ext.apply({}, config || {}, {
		title: LN('sbi.qbe.operandchooserwindow.title')
		, width: 400
		, height: 380
		, hasBuddy: false
	});
	
	Ext.apply(this, c);	
	
	this.addEvents('applyselection', 'discardselection');
	
	this.initTreePanel();
	//this.initButtonz();
	
	// constructor
	Sbi.qbe.OperandChooserWindow.superclass.constructor.call(this, {
		layout: 'fit',
		width: this.width,
		height: this.height,
		closeAction:'hide',
		plain: true,
		title: this.title,
		items: [this.treePanel],
		//buttons: [this.buttonz]
		
		buttons: [{
			text: 'Select',
		    handler: function(){
	    		this.fireEvent('applyselection', this, this.treePanel.tree.getSelectionModel().getSelectedNode());
            	this.hide();
        	}
        	, scope: this
	    }, {
			text: 'Close',
		    handler: function(){
				this.fireEvent('discardselection', this, this.treePanel.tree.getSelectionModel().getSelectedNode());
				this.hide();
    		}
        	, scope: this
	    }]
    });
    
	if(c.hasBuddy === 'true') {
		this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
	}
	
	this.on('show', this.oonShow, this);
};

Ext.extend(Sbi.qbe.OperandChooserWindow, Ext.Window, {
    
    services: null
    , treePanel: null
    , buttonz: null
    , reloadOnShow: false
    , parentQuery: null
   
   
    // public methods
    
    , setParentQuery: function(query) {

		//if(!this.parentQuery || this.parentQuery.id !== query.id) {
			this.parentQuery = query;
			if(this.treePanel.tree.root.loaded !== true) {
			
				this.treePanel.treeLoader.baseParams = {
		    			parentQueryId : this.parentQuery.id
	    		};
			} else {
				
				this.reloadOnShow = true;
			}
			
		//}
	}
    
    // private methods
    
    , initTreePanel: function() {
    	
    	this.treePanel = new Sbi.qbe.DataMartStructurePanel({
    		title: undefined //'Selected entities in parent query'
    		, rootNodeText: 'Parent Query'
    		, ddGroup: 'gridDDGroup'
    		, type: 'operandchooserwindow'
    		, preloadTree: false
    	});
    	
    	this.treePanel.on('load', function() {
    		this.treePanel.tree.root.loaded = true;
    		this.reloadOnShow = false;
    		//this.treePanel.expandAll();
    	}, this);
    }
    
    , initButtonz: function() {
    	this.buttonz = [{
			text: 'select',
		    handler: function(){
	    		this.fireEvent('applyselection', this, this.treePanel.getSelectionModel().getSelectedNode());
            	this.hide();
        	}
        	, scope: this
	    }, {
			text: 'close',
		    handler: function(){
				this.fireEvent('discardselection', this, this.treePanel.getSelectionModel().getSelectedNode());
				this.hide();
    		}
        	, scope: this
	    }];
    }
    
    
	, oonShow: function() {
    	
		if(this.reloadOnShow === true) {
    		var p = {
    			parentQueryId : this.parentQuery.id
    		};
    		this.treePanel.load(p);    		
    	}
    }
});