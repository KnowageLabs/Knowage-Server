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

Sbi.formbuilder.DynamicFilterEditorPanel = function(config) {
	
	var defaultSettings = {
		
		title: LN('sbi.formbuilder.dynamicfiltereditorpanel.title')
		, emptyMsg: LN('sbi.formbuilder.dynamicfiltereditorpanel.emptymsg')
		, filterItemName: LN('sbi.formbuilder.dynamicfiltereditorpanel.filteritemname')
		//, ddGroup    : 'formbuilderDDGroup'
		//, droppable: {
		//	onFieldDrop: this.onFieldDrop
		//} 
		, enableDebugBtn: false
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.dynamicFilterEditorPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.dynamicFilterEditorPanel);
	}
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	// constructor
    Sbi.formbuilder.DynamicFilterEditorPanel.superclass.constructor.call(this, c);
    
    this.on('addrequest', function() {
    	this.showFilterGroupWizard(null);
    }, this);
    
};

Ext.extend(Sbi.formbuilder.DynamicFilterEditorPanel, Sbi.formbuilder.EditorPanel, {
    
	wizard: null
	
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
	
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addFilterGroup(contents[i]);
		}
	}
	
	, addFilterGroup: function(content) {
		
		var groupTitle = (content.title !== undefined && content.title !== '') ? 
				content.title 
				: 
				(LN('sbi.formbuilder.dynamicfiltereditorpanel.grouptitle') + ' (' + content.operator + ')');
			
		var newGroupEditor = new Sbi.formbuilder.DynamicFilterGroupEditor({
			groupTitle: groupTitle
			, operator: content.operator
			, baseContents: content.admissibleFields
		});
	    newGroupEditor.on('editrequest', function(editor) {
	    	this.showFilterGroupWizard(editor);
	    }, this);
		this.addFilterItem(newGroupEditor);
		
		return newGroupEditor;
	}
		
	, addFilter: function(filterConf) {	
		alert('addFilter non implementato');
	}

	, showFilterGroupWizard: function(targetFilterGroup) {
		var wizard = this.getDynamicFilterGroupWizard();
		wizard.setTarget(targetFilterGroup || null);
		wizard.show();
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	, onDebug: function() {
		
	}
	
	/* unfortunately, this does not work properly: since there are 2 overlapping drop zones (this one and DynamicFilterGroupEditor's one), 
	 * the DynamicFilterGroupEditor.onFieldDrop is invoked twice
	, onFieldDrop: function(fieldConf) {
		var content = this.createDefaultEditorConfig( fieldConf );
		var targetFilterGroup = this.addFilterGroup( content );
		var wizard = this.getDynamicFilterGroupWizard();
		wizard.setTarget(targetFilterGroup);
		wizard.show();
	}
	*/
	
	,
	createDefaultEditorConfig : function (fieldConf) {
		var content = {};
		content.title = fieldConf.alias;
		content.operator = "EQUALS TO";
		var admissibleFields = [];
		
		var c = Ext.apply({}, fieldConf);
		c.uniqueName = c.id || c.field || 'not defined';
		if(c.id) delete c.id;
		if(c.field) delete c.field;
		
		c.alias = c.alias || c.text || 'not defined';
		delete c.text;
		
		admissibleFields[0] = c;
		content.admissibleFields = admissibleFields;
		return content;
	}
	
	,
	getDynamicFilterGroupWizard : function () {
		if (this.wizard === null) {
			this.wizard = new Sbi.formbuilder.DynamicFilterGroupWizard();
			this.wizard.on('apply', function(win, target, state) {
				if(target === null) {
					this.addFilterGroup(state);
				} else {
					target.modifyFilter(state);
				}
			}, this);
		}
		return this.wizard;
	}
  	
});