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

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.EditorPanel = function(config) {
	
	var defaultSettings = {
		
		// labels
		title: undefined
		, emptyMsg: 'Editor panel is empty'
		, filterItemName: 'filter'
		
		// options
		, enableAddBtn: true
		, enableClearBtn: true
			
		// layout
		/*
		, layout: 'table'
	    , layoutConfig: {
	        columns: 100
	    }
		*/
		
		// style
		, frame: true
		, autoScroll: true
		, autoWidth: true
		, autoHeight: true
	};
	
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.editorPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.editorPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.init();
	this.initTools();
	
	var items;
	if(this.filterFrame === true) {
		var frame = new Ext.form.FieldSet({
			title: this.filterTitle,
	        autoHeight: true,
	        autoWidth: true,
	        items: this.contents
		});
		
		this.filterItemsCt = frame;
		items = this.filterItemsCt;
	} else {
		this.filterItemsCt = this;
		items = this.contents;
	}
	
	
	
	Ext.apply(c, {
		tools: this.tools,
  		items: items
	});

	// constructor
    Sbi.formbuilder.EditorPanel.superclass.constructor.call(this, c);
	
    this.addEvents('addrequest', 'editrequest');
    
    if(this.droppable !== null) {
    	this.on('render', this.initDropTarget, this);
    }
    
    this.on('beforedestroy', this.onEditorDestroy, this);
    
};

Ext.extend(Sbi.formbuilder.EditorPanel, Ext.Panel, {
    
	services: null
	, contents: null
	, empty: null
	, emptyMsg: null
	, emptyMsgPanel: null
	, tools: null
	
	, filterTitle: null
	, filterFrame: false
	, filterItemsCt: null
	
	, droppable: null
	
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(contents) {
		alert('EditorPanel: setContents undefined');
	}

	, getContents: function() {
		var c = [];
		if(this.empty === true) return c;
		
		for(var i = 0; i < this.contents.length; i++) {
			var filterItem = this.contents[i];
			c.push( filterItem.getContents() );
		}
		
		return c;
	}

	, clearContents: function() {
		if(this.empty === false) {		
			this.reset();
		}
	}
	
	, addFilterItem: function(filtersItem) {
		//alert('addFilterItem IN');
		if(this.empty === true) {
			this.reset();
			this.empty = false;
			this.contents = [];			
		}
		
		filtersItem.on('destroy', this.onFilterItemDestroy, this);
			
		this.contents.push(filtersItem);
		
		if(this.rendered === true) {
			this.filterItemsCt.add(filtersItem);
			this.doLayout();	
			//alert('addFilterItem BETWEENN');
		}
		//alert('addFilterItem OUT');
	}

	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, reset: function() {	
		if(this.contents && this.contents.length) {
			for(var i = this.contents.length-1; i >= 0; i--) {
				this.contents[i].destroy();
				/*
				if(this.rendered === true) {
					// beware: "remove" fire destroy event that is catched above here. the callback modify the length of contents) 
					this.remove(this.contents[i], true);
				} else {
					this.contents[i].destroy();
				}
				*/
			}
		}
	}
	
	, init: function() {
		this.empty = true;
		this.contents = [];

		if(this.baseContents !== undefined && this.baseContents !== null && this.baseContents.length !== 0) {
			this.setContents(this.baseContents);	
		} else {
			this.initEmptyMsgPanel();
			this.contents.push(this.emptyMsgPanel);
		}
	}	

	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
		});
	}

	, initTools: function() {
		this.tools = [];
		
		
		if(this.enableAddBtn === true) {
			this.tools.push({
			    id:'plus',
			    qtip: LN('sbi.formbuilder.editorpanel.add') + ' ' + this.filterItemName,
			    handler: function(event, toolEl, panel){
					this.fireEvent('addrequest', this);
			    }
			    , scope: this
			});
		}
		
		if(this.enableClearBtn === true) {
			this.tools.push({
			    id:'delete',
			    qtip: LN('sbi.formbuilder.editorpanel.clearall'),
			    handler: function(event, toolEl, panel){
			  		this.clearContents();
			    }
			    , scope: this
			});
		}
		
		if(this.enableDebugBtn === true) {
			this.tools.push({
			    id:'pin',
			    qtip: 'debug',
			    handler: function(event, toolEl, panel){
					this.onDebug();
			    }
			    , scope: this
			});
		}
	}
	
	, initDropTarget: function() {
		this.removeListener('render', this.initDropTarget, this);
		this.dropTarget = new Sbi.formbuilder.EditorDropTarget(this, this.droppable);
	}
	
	
	, onDebug: function() {
	
	}
	
	, onEditorDestroy: function() {
		this.pendingDestruction = true;
		return true;
	}
	
	, onFilterItemDestroy: function(filterItem) {
		
		if (this.pendingDestruction && this.pendingDestruction === true) return;
		
		var t = this.contents;
		this.contents = [];
		for(var i = 0; i < t.length; i++) {
			if(filterItem.id !== t[i].id) {
				this.contents.push(t[i]);
			}
		}
		
		if(this.contents.length != this.items.length) {
			//alert('we are in truble');
			//alert(this.contents.length + ' ' + this.items.length);
			for(var i = 0; i < this.items.length; i++) {
				if(filterItem.id === this.items.get(i).id) {
					this.items.remove(this.items.get(i));
				}
			}
			
		}
		
		if(this.contents.length === 0) {
			this.empty = true;
			this.initEmptyMsgPanel();
			
			this.filterItemsCt.add(this.emptyMsgPanel);
			this.contents = [this.emptyMsgPanel];
			this.doLayout();
		}
		
	}

	   
	
  	
});