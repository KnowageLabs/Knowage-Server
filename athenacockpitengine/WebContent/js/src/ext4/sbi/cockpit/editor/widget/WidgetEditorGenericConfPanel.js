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
		
		var title = Ext.create('Ext.form.HtmlEditor', {
			width: 525,
		    height: 100,
//		    labelWidth: 50,
		    fieldLabel: 'Title',
		    name: 'title',
		    enableLinks: false,
		    enableSourceEdit: false,
		    enableLists: false,
		});
		
		var titlePerc = Ext.create('Ext.form.field.Number', {
//			width: 525,
//		    height: 100,
//		    labelWidth: 50,
		    fieldLabel: LN('sbi.cockpit.widgeteditorgenericconfpanel.titleheightperc'),
		    name: 'titlePerc',
	        value: 10,
	        maxValue: 30,
	        minValue: 1
		});
		
//		var title = new Ext.form.field.Text({
//			fieldLabel: 'Title',
//			name: 'title',
//            allowBlank: true,
//            tooltip: 'Enter the widget title',
//            regex: this.re,
//            regextText: 'Not a valid title.  Must enter only alphnumeric character',
//            listeners: {
//		    	blur: function(d) {
//	                var newVal = d.getValue().trim();
//	                titleStyle.setValue(newVal);
//	                
//	                Ext.getCmp('titleStyleId').select();
//	            }
//		    }
//		});
		

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
		this.fields.push(titlePerc);
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
			titlePerc: this.fields[1].getValue(),
			description: this.fields[2].getValue(),
			incomingeventsenabled: this.fields[3].getValue(),
			outcomingeventsenabled: this.fields[4].getValue()
		});

		return formState;
	}

	, setFormState: function(widgetConf) {
		Sbi.trace("[WidgetEditorGenericConfPanel.setFormState]: IN");

		this.fields[0].setValue(widgetConf.title);
		if(widgetConf.titlePerc !== undefined && widgetConf.titlePerc !== null){
			this.fields[1].setValue(widgetConf.titlePerc);
		}else{
			//to manage old saved cockpit without this value
			this.fields[1].setValue(10);
		}
		this.fields[2].setValue(widgetConf.description);
		this.fields[3].setValue(widgetConf.incomingeventsenabled);
		this.fields[4].setValue(widgetConf.outcomingeventsenabled);

		Sbi.trace("[WidgetEditorGenericConfPanel.setFormState]: OUT");
	}

	, resetFormState: function() {

        Sbi.trace("[WidgetEditorGenericConfPanel.resetFormState]: IN");

        this.fields[0].reset();
        this.fields[1].reset();
        this.fields[2].reset();
        this.fields[3].reset();
        this.fields[4].reset();

        Sbi.trace("[WidgetEditorGenericConfPanel.resetFormState]: OUT");
    }

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

});