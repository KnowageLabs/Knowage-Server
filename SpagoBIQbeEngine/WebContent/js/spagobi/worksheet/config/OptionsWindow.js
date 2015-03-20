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

Ext.ns("Sbi.worksheet.config");

Sbi.worksheet.config.OptionsWindow = function(config) {

	var defaultSettings = {
		title : LN('sbi.config.optionswindow.title')
		, width: 550
		, height: 250
	};
		
	if (Sbi.settings && Sbi.settings.worksheet && Sbi.settings.worksheet.config && Sbi.settings.worksheet.config.optionsWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.worksheet.config.optionsWindow);
	}
	
	var c = Ext.apply(defaultSettings, config || {});
	
	Ext.apply(this, c);
	
	this.addEvents("apply");
	
	this.init();
	
	c = Ext.apply(c, {
		items : [this.optionsForm]
		, buttons :
			[
				{
					text : LN('sbi.worksheet.config.optionswindow.buttons.text.apply')
				    , handler : function() {
						this.fireEvent("apply", this, this.optionsForm.getFormState());
				    	this.close();
					}
					, scope : this
				}
				,
				{
					text : LN('sbi.worksheet.config.optionswindow.buttons.text.cancel')
				    , handler : function() {
				    	this.close();
					}
					, scope : this
				}
		    ]
	});
	
	// constructor
    Sbi.worksheet.config.OptionsWindow.superclass.constructor.call(this, c);
    
};

Ext.extend(Sbi.worksheet.config.OptionsWindow, Ext.Window, {

	options : null 			// the options to be displayed, it must be in the constructor input object
	, optionsForm : null	// the options's form

	,
	init : function () {
		this.optionsForm = new Sbi.widgets.ConfigurableForm({
			configuredItems : this.options
			, frame : true
		});
	}

	,
	getFormState : function () {
		return this.optionsForm.getFormState();
	}
	
	,
	setFormState : function (state) {
		this.optionsForm.setFormState(state);
	}
	
});