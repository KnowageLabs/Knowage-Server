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

Ext.ns("Sbi.formviewer");

Sbi.formbuilder.DynamicFilterGroupEditor = function(config) {
	
	var defaultSettings = {	
		groupTitle: LN('sbi.formbuilder.dynamicfiltergroupeditor.groupTitle')
		, width: 300
        , height: 150
        , autoWidth: false    	
        , emptyMsg: LN('sbi.formbuilder.dynamicfiltergroupeditor.emptymsg')
        , ddGroup    : 'formbuilderDDGroup'
        , droppable: {
			onFieldDrop: this.addField
		} 
	};
	
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.dynamicFilterGroupEditor) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.dynamicFilterGroupEditor);
	}
	
	this.operator = config.operator;
	delete config.operator;
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.initToolbar();
	
	Ext.apply(c, {
		filterTitle: this.groupTitle
		, filterFrame: true
		, tbar: this.toolbar
		, header: false
	});
	
	// constructor
	Sbi.formbuilder.DynamicFilterGroupEditor.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.DynamicFilterGroupEditor, Sbi.formbuilder.EditorPanel, {
    

	wizard: null
	, operator: null
	, groupTitle: null
	
	//--------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addField(contents[i]);
		}		
	}
	
	, getContents: function() {
		var c = {};
		c.title = undefined;
		if (this.groupTitle !== (LN('sbi.formbuilder.dynamicfiltereditorpanel.grouptitle') + ' (' + this.operator + ')')) {
			c.title = this.groupTitle;
		}
		c.operator = this.operator;
		c.admissibleFields = Sbi.formbuilder.DynamicFilterGroupEditor.superclass.getContents.call(this)
		return c;
	}
	
	, addField: function(fieldConf) {
		
		var field = new Sbi.formbuilder.DynamicFilterEditor(fieldConf);				
		this.addFilterItem(field);
		
		field.on('actionrequest', function(action, field) {
			if(action === 'edit') {
				this.editFilter(field);
			} else if(action === 'delete') {
				this.deleteField(field);
			}
		}, this);
	}
	
	, deleteField: function(f) {
		f.destroy();
		//this.remove(f, true);
	}
	
	, modifyFilter: function(state) {
		this.operator = state.operator;
		this.groupTitle = (state.title !== undefined && state.title !== '') ? 
				state.title 
				: 
				(LN('sbi.formbuilder.dynamicfiltereditorpanel.grouptitle') + ' (' + this.operator + ')');
		this.filterItemsCt.setTitle(this.groupTitle);
	}
	
	, editFilter: function() {
		this.fireEvent('editrequest', this);
	}
	
	, initToolbar: function() {
		this.toolbar =  new Ext.Toolbar({
			items: [
			    '->' , {
					text: LN('sbi.formbuilder.dynamicfiltergroupeditor.edit'),
					handler: function() {this.editFilter();},
					scope: this
			    } , {
					text: LN('sbi.formbuilder.dynamicfiltergroupeditor.remove'),
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
	
});