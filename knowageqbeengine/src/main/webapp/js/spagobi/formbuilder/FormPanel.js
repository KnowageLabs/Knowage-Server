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

Sbi.formbuilder.FormPanel = function(config) {
	
	var defaultSettings = {
		title: LN('sbi.formbuilder.formpanel.title')
	};
	
	this.services = new Array();
	var params = {};
	this.services['setFormBuilderState'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SET_FORM_BUILDER_STATE_ACTION'
		, baseParams: params
	});
	this.services['getFormPreview'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'GET_FORM_PREVIEW_ACTION'
		, baseParams: {'MODALITY' : 'SMARTFILTER_EDIT'}
	});
		
	if(Sbi.settings && Sbi.settings.formbuilder && Sbi.settings.formbuilder.formPanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.formbuilder.formPanel);
	}
		
	var c = Ext.apply(defaultSettings, config || {});
		
	Ext.apply(this, c);
	
	this.initButtons();
	this.prevButton.toggle(true, true);
	
	this.formBuilderPage = new Sbi.formbuilder.FormBuilderPage({
		closable: false,
		template: config.template
	});
	this.formPreviewPage = new Sbi.formbuilder.FormPreviewPage({closable: false});
	
	c = Ext.apply(c, {
		border: false
		, layout: 'card'
  		, activeItem: 0
      	, items: [this.formBuilderPage, this.formPreviewPage]
		, tbar: ['->', this.prevButton, this.nextButton]
	});
	
	// constructor
	Sbi.formbuilder.FormPanel.superclass.constructor.call(this, c);
	
	// if moving towards preview, send form builder state to server, and then update iframe content
	this.formPreviewPage.on('activate', function() {
		
		this.formBuilderPage.validateTemplate(
			function() {
				this.setFormBuilderState(this.refreshPreview, Sbi.exception.ExceptionHandler.handleFailure, this);
			}, function(errors) {
				var message = errors.join('<br>');
				Sbi.exception.ExceptionHandler.showErrorMessage(message, LN('sbi.formbuilder.formbuilderpage.validationerrors.title'));
		}, this);
		
	}, this);

};

/**
 * @class Sbi.formbuilder.FormPanel
 * @extends Ext.TabPanel
 * 
 * FormPanel
 */
Ext.extend(Sbi.formbuilder.FormPanel, Ext.Panel, {
	
	services: null
	, prevButton: null
	, nextButton: null
	
	,
	initButtons : function () {
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

	, setFormBuilderState: function(successCallback, failureCallback, scope) {
		var state = this.formBuilderPage.templateEditorPanel.getContents();
		var params = {
				"FORM_STATE": Sbi.commons.JSON.encode(state)
		};
		Ext.Ajax.request({
		    url: this.services['setFormBuilderState'],
		    success: successCallback,
		    failure: failureCallback,	
		    scope: scope,
		    params: params
		});   
	}
	
	, refreshPreview: function() {
		this.formPreviewPage.getFrame().setSrc(this.services['getFormPreview']);
	}
    
});