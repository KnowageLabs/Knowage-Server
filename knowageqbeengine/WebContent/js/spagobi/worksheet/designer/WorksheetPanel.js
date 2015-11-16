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
 * [list]
 * 
 * 
 * Public Events
 * 
 * [list]
 * 
 * Authors - Alberto Ghedin (alberto.ghedin@eng.it)
 */
Ext.ns("Sbi.worksheet.designer");

Sbi.worksheet.designer.WorksheetPanel = function(config) { 

	var defaultSettings = {
		title: LN('sbi.worksheet.title')
	};
	
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.designer && Sbi.settings.worksheet.designer.worksheetpanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.designer.worksheetpanel);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.prevButton =  new Ext.Button({
		    text: '&laquo; ' + LN('sbi.qbe.qbecardspanel.designer')
			, enableToggle : true
			, allowDepress : false
	});
	this.prevButton.on(
			'toggle',
			function (button, pressed) {
				if (pressed) {
					this.setActiveItem(0);
					this.nextButton.toggle(false, true);
				}
			}, 
			this
	);
	
	
	this.nextButton =  new Ext.Button({
	    text: LN('sbi.qbe.qbecardspanel.preview') + ' &raquo;'
		, enableToggle : true
		, allowDepress : false
	});
	this.nextButton.on(
			'toggle',
			function (button, pressed) { 
				if (pressed) {
					this.setActiveItem(1);
					this.prevButton.toggle(false, true);
				}
			}, 
			this
	);
	
	this.prevButton.toggle(true, true);
	
	this.initWorksheetPanel(config);
	
	var toolbarButtons = ['->', this.prevButton, this.nextButton];
	if (config.extraButtons && config.extraButtons.length > 0) {
		toolbarButtons = toolbarButtons.concat(config.extraButtons);
	}
	
	c = Ext.apply(c, {
			//id:'WorksheetPanel', 
			items: [this.worksheetDesignerPanel, this.worksheetPreviewPanel]
		    , enableDragDrop: true
		    , border: false
			, layout: 'card'
			, activeItem: 0
			, height: 100
			, style: 'margin-top: 0px; margin-left: auto; margin-right: auto;'
			, width: 250
			, tbar: toolbarButtons
	});
	
	Sbi.worksheet.designer.QueryFieldsCardPanel.superclass.constructor.call(this, c);

	this.worksheetDesignerPanel.on('afterworksheetinitialized', function(theWorksheetDesignerPanel) {
		this.worksheetDesignerPanel.designToolsPanel.refresh();
		this.worksheetEngineInitialized = true;
	}, this);
	
	this.on('activate', function(thePanel){
		// recalculate current fields in store and fires validateInvalidFieldsAfterLoad event
		var activeItem = this.getLayout().activeItem;
		var index = this.getActiveItemIndex();
		if (index == 1) {
			activeItem.fireEvent('activate', activeItem); // force refresh
		}
		
		if (this.worksheetEngineInitialized) {
			this.worksheetDesignerPanel.designToolsPanel.refresh();
		}
		
		this.worksheetDesignerPanel.designToolsPanel.on('validateInvalidFieldsAfterLoad', 
			function(){
				this.worksheetDesignerPanel.validate(function(){}, function(){}, this);
			}, this
		);
		
	}, this);
	
	
	};

Ext.extend(Sbi.worksheet.designer.WorksheetPanel, Ext.Panel, {
	
	worksheetDesignerPanel: null
	, worksheetPreviewPanel: null
	, prevButton: null
	, nextButton: null
	
	, initWorksheetPanel: function(config) {
		this.worksheetDesignerPanel = config.worksheetDesignerPanel;
		this.worksheetPreviewPanel = config.worksheetPreviewPanel;
	}

	,
	setActiveItem: function(pageIndex) {
		this.getLayout().setActiveItem( pageIndex );
	}
	
	, 
	getActiveItemIndex : function () {
		var activeItem = this.getLayout().activeItem;
		var index = this.items.indexOf(activeItem);
		return index;
	}
	
	,
	isWorksheetPageActive: function(){
		return this.getActiveItemIndex() == 1;
	}

});
