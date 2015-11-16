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

Ext.ns("Sbi.qbe");

Sbi.qbe.RelationshipsWizardWindow = function(config) {

	var defaultSettings = {
			title : LN('sbi.qbe.relationshipswizardwindow.title')
			, width : 820
			, height : 520
			, closeAction : 'hide'
				, maximizable: true
	};

	if (Sbi.settings && Sbi.settings.qbe && Sbi.settings.qbe.relationshipswizardwindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.qbe.relationshipswizardwindow);
	}

	var c = Ext.apply(defaultSettings, config || {});

	Ext.apply(this, c);

	this.services = this.services || new Array(); 

	this.init();

	c = Ext.apply(c, {
		items : [this.relationshipsWizard]
	, layout : 'fit'
		, buttons : [{
			text: LN('sbi.qbe.bands.back.btn'),
			name:'back',
			handler: this.backHandler,
			hidden: true,
			scope: this
		},
		{
			text: LN('sbi.qbe.relationshipswizardwindow.buttons.apply'),
			handler: this.applyHandler,
			name:'apply',
			scope: this
		},
		{
			text: LN('sbi.qbe.bands.finish.btn'),
			handler: this.applyFinishHandler,
			name:'finish',
			hidden: true,
			scope: this
		},
		{
			text: LN('sbi.qbe.relationshipswizardwindow.buttons.cancel'),
			handler: this.cancelHandler,
			scope: this
		}]
	});

	// constructor
	Sbi.qbe.RelationshipsWizardWindow.superclass.constructor.call(this, c);

	this.addEvents("apply");

};

Ext.extend(Sbi.qbe.RelationshipsWizardWindow, Ext.Window, {

	relationshipsWizard : null
	, ambiguousFields : null // must be set in the object passed to the constructor
	, ambiguousRoles: null

	,
	init : function () {
		this.relationshipsWizard = new Sbi.qbe.RelationshipsWizardContainer({
			ambiguousFields : this.ambiguousFields
			, ambiguousRoles: this.ambiguousRoles
			, title : ''
		});
	}

	,
	applyHandler : function () {
		var roles = this.relationshipsWizard.getRoles();
		if(roles && roles.length>0){
			this.relationshipsWizard.updateRoleWizard(roles);
			this.nextHandler();
		}else{
			var userChoices = this.relationshipsWizard.getUserChoices();
			this.fireEvent('apply', this, userChoices, null);
		}
	}

	,
	cancelHandler : function () {
		this.close();
	}
	
	,
	nextHandler : function () {

		this.buttons[0].show();//show back
		this.buttons[1].hide();//hide apply
		this.buttons[2].show();//show finish
		this.relationshipsWizard.fireEvent('next', this);
	}
	
	,
	backHandler : function () {
		this.buttons[0].hide();//hide back
		this.buttons[1].show();//show apply
		this.buttons[2].hide();//hide finish
		this.relationshipsWizard.fireEvent('back', this);
	}
	
	, applyFinishHandler: function(){
		var errors = this.relationshipsWizard.validate();
		if(errors.length==0){
			var userChoices = this.relationshipsWizard.getUserChoices();
			var userRoles = this.relationshipsWizard.getUserSelectedRoles();
			this.fireEvent('apply', this, userChoices, userRoles);
		}else{
			var errorStr = "";
			for(var i=0; i<errors.length; i++){
				errorStr = errorStr+errors[i]+"<br>";
			}
			Sbi.exception.ExceptionHandler.showErrorMessage(errorStr,LN('sbi.qbe.relationshipswizard.roles.validation.error'));
		}

	}

});