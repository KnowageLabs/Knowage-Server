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

Sbi.formbuilder.StaticOpenFilterEditorPanel = function(config) {
	
	var defaultSettings = {
		
		title: LN('sbi.formbuilder.staticopenfiltereditorpanel.title')
		, emptyMsg: LN('sbi.formbuilder.staticopenfiltereditorpanel.emptymsg')
		, ddGroup    : 'formbuilderDDGroup'
		, droppable: {
			onFieldDrop: this.onFieldDrop
		} 
		, filterItemName: 'static open filter'        
		, enableAddBtn: false			
		, layout: 'column'
		
	};
	if (Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.staticOpenFilterEditorPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.staticOpenFilterEditorPanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	// constructor
    Sbi.formbuilder.StaticOpenFilterEditorPanel.superclass.constructor.call(this, c);
};

Ext.extend(Sbi.formbuilder.StaticOpenFilterEditorPanel, Sbi.formbuilder.EditorPanel, {
	
	dummy: null
	
	// --------------------------------------------------------------------------------
	// public methods
	// --------------------------------------------------------------------------------
		
	, setContents: function(contents) {
		for(var i = 0, l = contents.length; i < l; i++) {
			this.addFilterGroup(contents[i]);
		}
	}
	
	, addFilterGroup: function(filtersGroupConf) {
		var c =  filtersGroupConf || {};
		c.baseContents = [filtersGroupConf];
		var filter = new Sbi.formbuilder.StaticOpenFilterGroupEditor( c );
		this.addFilterItem( filter );
	}
	
	// --------------------------------------------------------------------------------
	// private methods
	// --------------------------------------------------------------------------------
	
	
	, onFieldDrop: function(fieldConf) {
		
		
		var filtersGroupConf = {};
		filtersGroupConf.text = fieldConf.alias;
		filtersGroupConf.field = fieldConf.id;
		filtersGroupConf.operator = 'EQUALS TO';
		filtersGroupConf.maxSelectedNumber = 1;
		filtersGroupConf.orderBy = '';
		filtersGroupConf.orderType = '';
		filtersGroupConf.queryRootEntity = false;

		
		var staticOpenFilterWindow = new Sbi.formbuilder.StaticOpenFilterWizard(filtersGroupConf, {});
		
		staticOpenFilterWindow.show();	
		
		staticOpenFilterWindow.on('apply', function(filtersGroupConf) {
			this.addFilterGroup(filtersGroupConf);
		} , this); 
	}
	
});
