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

Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.SaveRememberMeWindow = function(config) {

	// always declare exploited services first!
	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
	this.services = new Array();
	this.services['saveRememberMeService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_REMEMBER_ME_ACTION'
		, baseParams: params
	});
	
	this.SBI_EXECUTION_ID = config.SBI_EXECUTION_ID;
	
	this.rememberMeName = new Ext.form.TextField({
		id: 'nameRM',
		name: 'nameRM',
		allowBlank: false, 
		inputType: 'text',
		maxLength: 50,
		width: 250,
		fieldLabel: LN('sbi.rememberme.name') 
	});
	
	this.rememberMeDescr = new Ext.form.HtmlEditor({
        id:'descrRM',
        width: 550,
        height: 150,
		fieldLabel: LN('sbi.rememberme.descr')  
    });
	
    Ext.form.Field.prototype.msgTarget = 'side';
    this.saveRememberMeForm = new Ext.form.FormPanel({
        frame:true,
        bodyStyle:'padding:5px 5px 0',
        items: [this.rememberMeName, this.rememberMeDescr],
        buttons:[{text: LN('sbi.rememberme.save'), handler: this.saveRememberMe, scope: this}]
    });
	
	this.buddy = undefined;
	
	var c = Ext.apply({}, config, {
		id:'popup_saveRM',
		layout:'fit',
		width:700,
		height:300,
		//closeAction:'hide',
		plain: true,
		title: LN('sbi.execution.saveRememberMe'),
		items: this.saveRememberMeForm
	});   
	
	// constructor
    Sbi.execution.toolbar.SaveRememberMeWindow.superclass.constructor.call(this, c);
    
    if (this.buddy === undefined) {
    	this.buddy = new Sbi.commons.ComponentBuddy({
    		buddy : this
    	});
    }
    
};

Ext.extend(Sbi.execution.toolbar.SaveRememberMeWindow, Ext.Window, {
	
	rememberMeName: null
	, rememberMeDescr: null
	, saveRememberMeForm: null
	
	, saveRememberMe: function () {
		var name = this.rememberMeName.getValue();
		if (name == null || name == '') {
			Ext.MessageBox.show({
				msg: LN('sbi.rememberme.missingName'),
				buttons: Ext.MessageBox.OK,
				width:300,
				icon: Ext.MessageBox.WARNING
			});
			return;
		}
		Ext.Ajax.request({
	        url: this.services['saveRememberMeService'],
	        params: {'SBI_EXECUTION_ID': this.SBI_EXECUTION_ID, 
						'name': this.rememberMeName.getValue(), 'description': this.rememberMeDescr.getValue()},
	        callback : function(options , success, response) {
	  	  		if (success) {
		      		if(response !== undefined && response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if (content !== undefined) {
		      				var icon;
		      				var message;
		      				if (content.result === 'alreadyExisting') {
		      					icon = Ext.MessageBox.WARNING,
		      					message = LN('sbi.rememberme.alreadyExisting');
		      				} else {
		      					icon = Ext.MessageBox.INFO,
		      					message = LN('sbi.rememberme.saveOk');
		      				}
			      			Ext.MessageBox.show({
			      				title: 'Status',
			      				msg: message,
			      				modal: false,
			      				buttons: Ext.MessageBox.OK,
			      				width:300,
			      				icon: icon,
			      				animEl: 'root-menu'        			
			      			});
		      			} else {
		      				Sbi.exception.ExceptionHandler.showErrorMessage('Error while saving Remember Me', 'Service Error');
		      			}
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
	  	  		}
	        },
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
		});
	}
	
});