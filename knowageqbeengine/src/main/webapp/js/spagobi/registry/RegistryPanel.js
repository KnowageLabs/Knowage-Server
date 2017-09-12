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

Ext.ns("Sbi.registry");

Sbi.registry.RegistryPanel = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.registry.registrypanel.title')
	};
		
	if(Sbi.settings && Sbi.settings.registry && Sbi.settings.registry.registryPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.registry.registryPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
	
	this.init();
	
	c = Ext.apply(c, {
		layout: 'fit',
		autoScroll: true,
		items: [this.registryGridPanel]
	});
	
	// constructor
    Sbi.registry.RegistryPanel.superclass.constructor.call(this, c);
    
};

/**
 * @class Sbi.registry.RegistryPanel
 * @extends Ext.Panel
 * 
 * RegistryPanel
 */
Ext.extend(Sbi.registry.RegistryPanel, Ext.Panel, {
	
	registryConfiguration : null
	
	, init: function () {
		
		
		this.warningMessageItem = new Ext.Toolbar.TextItem('<font color="red">' 
				+ LN('sbi.qbe.datastorepanel.grid.beforeoverflow') 
				//+ ' [' + Sbi.config.queryLimit.maxRecords + '] '
				+ LN('sbi.qbe.datastorepanel.grid.afteroverflow') 
				+ '</font>');
		
		this.pagingTBar = new Ext.PagingToolbar({
            pageSize: 25,
            store: this.store,
            displayInfo: true,
            displayMsg: LN('sbi.qbe.datastorepanel.grid.displaymsg'),
            emptyMsg: LN('sbi.qbe.datastorepanel.grid.emptymsg'),
            beforePageText: LN('sbi.qbe.datastorepanel.grid.beforepagetext'),
            afterPageText: LN('sbi.qbe.datastorepanel.grid.afterpagetext'),
            firstText: LN('sbi.qbe.datastorepanel.grid.firsttext'),
            prevText: LN('sbi.qbe.datastorepanel.grid.prevtext'),
            nextText: LN('sbi.qbe.datastorepanel.grid.nexttext'),
            lastText: LN('sbi.qbe.datastorepanel.grid.lasttext'),
            refreshText: LN('sbi.qbe.datastorepanel.grid.refreshtext')

        });
		this.pagingTBar.on('render', function() {
			this.pagingTBar.addItem(this.warningMessageItem);
			this.warningMessageItem.setVisible(false);
		}, this);
		
		
		
		
		
		
		this.registryGridPanel = new Sbi.registry.RegistryEditorGridPanel({
			registryConfiguration : this.registryConfiguration || {}
		});
		this.registryGridPanel.on('afterrender', function () {
			this.registryGridPanel.load({});
		}, this);
	}

});