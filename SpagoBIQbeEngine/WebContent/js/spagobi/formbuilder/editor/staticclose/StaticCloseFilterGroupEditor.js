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

Ext.ns("Sbi.formviewer");

Sbi.formbuilder.StaticCloseFilterGroupEditor = function(config) {
	

	var defaultSettings = {		
		width: 300
        , height: 150
        , autoWidth: false
		
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticCloseFilterGroupEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticCloseFilterGroupEditor);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.initToolbar();
	Ext.apply(c, {
		filterTitle: this.groupTitle || 'Filter Group'
		, filterFrame: true
		, tbar: this.toolbar
		, header: false
	});
	
	// constructor
	Sbi.formbuilder.StaticCloseFilterGroupEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.StaticCloseFilterGroupEditor, Sbi.formbuilder.EditorPanel, {
    

	wizard: null
	
	, groupTitle: null
	, singleSelection: null
	, allowNoSelection: null
	, noSelectionText: null
	, booleanConnector: null

	//--------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addFilter(contents[i]);
		}
		
	}
	
	, getContents: function() {
		var c = {};
		
		c.title = this.groupTitle;
		c.singleSelection = this.singleSelection;
		c.allowNoSelection = this.allowNoSelection;
		if (this.singleSelection === true) {
			c.noSelectionText = this.noSelectionText;
		} else {
			c.booleanConnector = this.booleanConnector;
		}
		c.options = Sbi.formbuilder.StaticCloseFilterGroupEditor.superclass.getContents.call(this);
		
		return c;
	}
	
	, addFilter: function(filtersConf) {
		filtersConf.singleSelection = this.singleSelection;
		var filter = new Sbi.formbuilder.StaticCloseFilterEditor(filtersConf);	
		filter.index = this.contents.length;
		this.addFilterItem(filter);
		
		filter.on('actionrequest', function(action, filter) {
			if(action === 'edit') {
				this.editFilter(filter);
			} else if(action === 'delete') {
				this.deleteFilter(filter);
			}
		}, this);
	}
	
	, modifyFilter: function(state) {
		var prevSingleSelection = this.singleSelection;
		Ext.apply(this, state);
		var newSingleSelection = this.singleSelection;
		
		this.filterItemsCt.setTitle(this.groupTitle);
		
		// if singleSelection has been changed, Radios must be trasformed into Checkboxes or Checkboxes to Radios
		if (prevSingleSelection !== newSingleSelection) {
			
			// save filters config into a temp array (previousFiltersConfig)
			var previousFiltersConfig = [];
			for (var i = 0; i < this.contents.length; i++) {
				var filterItem = this.contents[i];
				previousFiltersConfig[i] = filterItem.baseConfig;
			}
			
			// removing all filters
			this.clearContents();
			
			// re-creating filters
			this.setContents(previousFiltersConfig);
			
		}
	}
	
	, deleteFilter: function(f) {
		f.destroy();
		//this.remove(f, true);
	}
	
	, editFilter: function(f) {
		this.onFilterWizardShow(f)
	}
	
	, editFilterGroup: function() {
		this.fireEvent('editrequest', this);
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	

	, initToolbar: function() {
		this.toolbar = new Ext.Toolbar({
			items: [
			    '->'
			    , {
					text: LN('sbi.formbuilder.staticclosefiltergroupeditor.toolbar.add'),
					handler: function() {this.onFilterWizardShow();},
					scope: this
			    }, {
					text: LN('sbi.formbuilder.staticclosefiltergroupeditor.toolbar.edit'),
					handler: this.editFilterGroup,
					scope: this
			    }, {
					text: LN('sbi.formbuilder.staticclosefiltergroupeditor.toolbar.remove'),
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
		//if(this.wizard === null) { // must create a new StaticCloseFilterWizard instance:
		// see comments on 'close' closeAction config option in StaticCloseFilterWizard class
			this.wizard = new Sbi.formbuilder.StaticCloseFilterWizard();
			this.wizard.on('apply', function(win, target, state) {
				if(target === null) {
					this.addFilter(state);
				} else {
					target.setContents(state);
				}
				
			}, this);
		//}
		
		this.wizard.setTarget(targetFilter || null);		
		this.wizard.show();
	}
	
});