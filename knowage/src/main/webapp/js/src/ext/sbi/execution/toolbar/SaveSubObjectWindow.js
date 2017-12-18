/*
 * Knowage, Open Source Business Intelligence suite
 * Copyright (C) 2016 Engineering Ingegneria Informatica S.p.A.
 * 
 * Knowage is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Knowage is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */ 

Ext.ns("Sbi.execution.toolbar");

Sbi.execution.toolbar.SaveSubObjectWindow = function(config) {

	// init properties...
	var defaultSettings = {
			// public
			title : LN('sbi.subobject.savewindow.title')
			, layout : 'fit'
				, width : 540
				, height : 260
				, closeAction : 'close'
					, frame : true
					// private
	};

	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.toolbar && Sbi.settings.execution.saveSubObjectWindow) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.saveSubObjectWindow);
	}

	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);

	this.addEvents('save');

	this.initScopeStore();
	this.initForm();

	var c = Ext.apply({}, config, {
		buttonAlign: 'right',
		buttons : [
		{ 
			handler: this.destroy
			, scope: this
			, text: LN('sbi.generic.cancel')
		},
		{ 
			handler: this.saveHandler
			, scope: this
			, text: LN('sbi.execution.executionpage.toolbar.save')
		}]
	, items : this.datasetForm
	});   

	Sbi.execution.toolbar.SaveSubObjectWindow.superclass.constructor.call(this, c);

};

/**
 * @class Sbi.execution.toolbar.SaveSubObjectWindow 
 * @extends Ext.Window
 * 
 */
Ext.extend(Sbi.execution.toolbar.SaveSubObjectWindow , Ext.Window, {

	datasetForm : null
	, queries : null


	, initForm: function () {

		this.nameField = new Ext.form.TextField({
			id: 'name',
			name: 'name',
			allowBlank: false, 
			maxLength: 50,
			autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '50'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.name') 
		});
		this.descriptionField = new Ext.form.TextArea({
			id: 'description',
			name: 'description',
			allowBlank: true, 
			maxLength: 160,
			autoCreate: {tag: 'textarea ', autocomplete: 'off', maxlength: '160'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.descr') 
		});
		this.scopeField = new Ext.form.ComboBox({
			fieldLabel: LN('sbi.generic.scope') ,
			mode : 'local',
    	    typeAhead: true,
    	    triggerAction: 'all',
			store: this.scopesStore,
			displayField: 'description',
			valueField: 'value'
		});

		//default value
		this.scopeField.setValue(this.scopesStore.getAt(1).get('value'));

		this.datasetForm = new Ext.FormPanel({
			columnWidth: 0.6
			, frame : true
			, autoScroll : true
			, items: {
				columnWidth : 0.4
				, xtype : 'fieldset'
				, labelWidth : 80
				//, defaults : { border : false }
				, defaultType : 'textfield'
					, autoHeight : true
					, autoScroll : true
					, bodyStyle : Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding:0px 5px;'
						, border : false
						, style : {
							//"margin-left": "4px",
							//"margin-top": "10px"
						}
		, items :  [ this.nameField, this.descriptionField, this.scopeField ]
			}
		});

	}

,
getFormState : function() {
	var formState = {};
	formState.name = this.nameField.getValue();
	formState.description = this.descriptionField.getValue();
	formState.scope = this.scopeField.getValue();
	return formState;
}

,
saveDatasetHandler: function () {

	var params = this.getInfoToBeSentToServer();
	Ext.MessageBox.wait(LN('sbi.generic.wait'));
	Ext.Ajax.request({
		url : this.services['saveDatasetService']
	, params : params
	, success : this.datasetSavedSuccessHandler
	, scope : this
	, failure : Sbi.exception.ExceptionHandler.handleFailure      
	});

}

,
initScopeStore : function () {
	var scopeComboBoxData = [
	                         ['public',LN('sbi.generic.scope.public')],
	                         ['private',LN('sbi.generic.scope.private')]
	                         ];

	this.scopesStore = new Ext.data.SimpleStore({
		fields: ['value', 'description'],
		data : scopeComboBoxData 
	}); 
}

,	saveHandler: function () {
	this.fireEvent("save",this.getFormState());
	
}


});