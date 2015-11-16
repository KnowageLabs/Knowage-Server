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
  * - Davide Zerbetto (davide.zerbetto@eng.it)
  */

Ext.ns("Sbi.formbuilder");

Sbi.formbuilder.StaticOpenFilterGroupEditor = function(config) {
		
	var defaultSettings = {
		width: 400
		, autoWidth: false
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticOpenFilterGroupEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticOpenFilterGroupEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	this.initToolbar();
	
	Ext.apply(c, {
		tbar: this.toolbar
		, header: false
	});
	
	// constructor
    Sbi.formbuilder.StaticOpenFilterGroupEditor.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.formbuilder.StaticOpenFilterGroupEditor, Sbi.formbuilder.EditorPanel, {
	
	wizard: null
	
	//--------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addFilter(contents[i]);
		}
	}
	
	, getContents: function() {
		return this.contents[0].getContents();
	}
	
	, addFilter: function(filterConf) {
		var filter = new Sbi.formbuilder.StaticOpenFilterEditor(filterConf); 
		this.addFilterItem(filter);
	}
	
	, editFilter: function(f) {
		this.onFilterWizardShow(f);
	}
	
	, modifyFilter: function(filterConf) {
		//alert('modifyFilter');
		this.clearContents();
		//alert('clearContents');
		this.addFilter(filterConf);
		//alert('addFilter');
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	

	, initToolbar: function() {
		this.toolbar =  new Ext.Toolbar({
			items: [
			    '->' , {
					text: LN('sbi.formbuilder.staticopenfiltergroupeditor.edit'),
					handler: function() {this.editFilter(this.contents[0]);},
					scope: this
			    } , {
					text: LN('sbi.formbuilder.staticopenfiltergroupeditor.remove'),
					handler: function() {
				    	if(this.ownerCt) {
			    			this.ownerCt.remove(this, true);
			    		} else {
			    			this.destroy();
			    		}
			    		
				    },
					scope: this
			    }
			  ]
		});
	}
	
	, onFilterWizardShow: function(targetFilter) {
		//alert(targetFilter.getContents().toSource());
		var staticOpenFilterWindow = new Sbi.formbuilder.StaticOpenFilterWizard(targetFilter.getContents(), {});
		staticOpenFilterWindow.show();
		staticOpenFilterWindow.on('apply', this.modifyFilter , this);
	}
	
});