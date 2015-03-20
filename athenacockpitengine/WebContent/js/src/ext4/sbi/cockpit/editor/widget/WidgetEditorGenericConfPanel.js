/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.cockpit.editor.widget");

Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel = function(config) {

	this.initFields();

	var defaultSettings = {
		xtype: 'form',
		name:'WidgetEditorGenericConfPanel',
		title:'Generic Configuration',
		layout: 'form',
		bodyPadding: '5 5 0',
		items: this.fields
	};

	var settings = Sbi.getObjectSettings('Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel', defaultSettings);

	var c = Ext.apply(settings, config || {});
	Ext.apply(this, c);

	c = {
		height: 400
	};

	Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.cockpit.editor.widget.WidgetEditorGenericConfPanel, Ext.Panel, {

	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================

	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null,
	re: null

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	/*
	 * @method
	 *
	 * Initialize the GUI
	 */
	, initEmptyMsgPanel: function() {
		this.emptyMsgPanel = new Ext.Panel({
			html: this.emptyMsg
			, border: false
			, frame: true
		});
	}

	, initFields: function() {

		this.re = new RegExp("^([A-Z0-9 ]*)$","i");

		this.fields = [];

		var title = new Ext.form.field.Text({
			fieldLabel: 'Title',
			name: 'title',
            allowBlank: true,
            tooltip: 'Enter the widget title',
            regex: this.re,
            regextText: 'Not a valid title.  Must enter only alphnumeric character'
		});

		var description = new Ext.form.field.TextArea({
			fieldLabel: 'Description',
			name: 'description',
            allowBlank: true
		});

		var incomingeventsenabled = new Ext.form.field.Checkbox({
            name: 'incomingeventsenabled',
            fieldLabel: LN('sbi.cockpit.editor.widget.widgeteditorgenericconfpanel.incomingeventsenabled.label'),
            boxLabel: '&nbsp;',
            afterBoxLabelTpl : '<span class="help" data-qtip="'
            	+ LN('sbi.cockpit.editor.widget.widgeteditorgenericconfpanel.incomingeventsenabled.description')
            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
            checked: true
		});

		var outcomingeventsenabled = new Ext.form.field.Checkbox({
            name: 'outcomingeventsenabled',
            fieldLabel: LN('sbi.cockpit.editor.widget.widgeteditorgenericconfpanel.outcomingeventsenabled.label'),
            boxLabel: '&nbsp;',
            afterBoxLabelTpl : '<span class="help" data-qtip="'
            	+ LN('sbi.cockpit.editor.widget.widgeteditorgenericconfpanel.outcomingeventsenabled.description')
            	+ '">&nbsp;&nbsp;&nbsp;&nbsp;</span>',
            checked: true
		});

		this.fields.push(title);
		this.fields.push(description);
		this.fields.push(incomingeventsenabled);
		this.fields.push(outcomingeventsenabled);
	}

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFormState: function() {

		var formState = Ext.apply({}, {
			title: this.fields[0].getValue(),
			description: this.fields[1].getValue(),
			incomingeventsenabled: this.fields[2].getValue(),
			outcomingeventsenabled: this.fields[3].getValue()
		});

		return formState;
	}

	, setFormState: function(widgetConf) {
		Sbi.trace("[WidgetEditorGenericConfPanel.setFormState]: IN");

		this.fields[0].setValue(widgetConf.title);
		this.fields[1].setValue(widgetConf.description);
		this.fields[2].setValue(widgetConf.incomingeventsenabled);
		this.fields[3].setValue(widgetConf.outcomingeventsenabled);

		Sbi.trace("[WidgetEditorGenericConfPanel.setFormState]: OUT");
	}

	, resetFormState: function() {

        Sbi.trace("[WidgetEditorGenericConfPanel.resetFormState]: IN");

        this.fields[0].reset();
        this.fields[1].reset();
        this.fields[2].reset();
        this.fields[3].reset();

        Sbi.trace("[WidgetEditorGenericConfPanel.resetFormState]: OUT");
    }

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

});