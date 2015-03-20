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
Ext.ns("Sbi.qbe");

Sbi.qbe.QbeCardsPanel = function(config) {
	
	var defaultSettings = {
		title : LN('sbi.qbe.qbecardspanel.title')
		, activeItem : 0
	};
	
	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.qbecardspanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.qbecardspanel);
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
	
	if (this.activeItem == 0) {
		this.prevButton.toggle(true, true);
	} else {
		this.nextButton.toggle(true, true);
	}
	
	c = Ext.apply(c, {
			items: [this.items]
		    , border: false
			, layout: 'card'
			, tbar: this.items.length > 1 ? ['->', this.prevButton, this.nextButton] : null 
			, hideMode: !Ext.isIE ? 'nosize' : 'display'
	});
	
	Sbi.worksheet.designer.QueryFieldsCardPanel.superclass.constructor.call(this, c);
	
};

Ext.extend(Sbi.qbe.QbeCardsPanel, Ext.Panel, {
	
	items : null
	, prevButton : null
	, nextButton : null

	,
	setActiveItem : function(pageIndex) {
		this.getLayout().setActiveItem( pageIndex );
	}

});
