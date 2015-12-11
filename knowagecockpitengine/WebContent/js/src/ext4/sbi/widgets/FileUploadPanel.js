/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Ceneselli" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


/**
 * Object name
 *
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
 * Authors
 * 		Antonella Giachino  (antonella.giachino@eng.it)
 *
 */

Ext.ns("Sbi.widgets");

Sbi.widgets.FileUploadPanel = function(config) {

	var defaultSettings =  {
	        labelWidth: 65,
	        frame:false,
	        defaultType: 'textfield',
	        isEnabled: true,
	        border: false
		};


	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	var panelItems;
	panelItems = this.initUploadForm(panelItems,config);

	c = {
			items: [
			        panelItems
			       ]
		};

	Sbi.widgets.FileUploadPanel.superclass.constructor.call(this, c);

};

Ext.extend(Sbi.widgets.FileUploadPanel, Ext.Panel, {

	initUploadForm : function(items,config){
//		this.label = new Ext.form.TextField({
//			readOnly:true,
//			value:config.labelFileName || LN('sbi.ds.fileName'),
//			columnWidth: 0.4,
//		}),
		
		//this isn't used anymore
		this.previewFileLabel = new Ext.form.DisplayField({
			value : (config.labelFileName || LN('sbi.ds.fileName')) +':',
//			columnWidth: 0.2,
			width:  80,
			allowBlank : false,
			readOnly:true,
			style:'font-size:12;font-family:arial'
//			,hidden: !this.isOwner || false
		});

		this.uploadField = new Ext.form.field.File({
			id: 'fileUploadField'
			,fieldLabel: config.labelFileName || LN('sbi.ds.fileName')
			,labelStyle: 'font-size:12;font-family:arial'
			,fieldStyle: 'border:0 none;background:transparent;width:auto'
			,buttonText: config.labelButtonText || ''
			,allowBlank: true
			,clearOnSubmit: false
			,columnWidth: 0.9
		});
		//fixing file name removing file path
		this.uploadField.on("change", function(o,v){
			return o.setRawValue(v.replace(/^.*(\\|\/)/, ''));
		}, this);

		this.uploadButton = new Ext.Button({
			id: 			'fileUploadButton',
			xtype:          'button',
        	handler:		this.uploadImgButtonHandler,
        	columnWidth:	0.1,
        	scope: 			this,
//        	tooltip: 		LN('sbi.worksheet.designer.sheettitlepanel.uploadimage'),
//        	style:			'padding-left: 10px',
        	iconCls:		'uploadImgIcon',
        	hidden: 		!this.isEnabled || false
	    });

		//Panel with the load file field
		this.fileUploadFormPanel = new Ext.Panel({
			height: 35, //45,
			width: '100%',
			layout: 'column', //'column',
			hideLabels: false,
			frame: false,
			header: false,
			border: false,
			labelAlign: 'left',
			items: [/*this.previewFileLabel, */this.uploadField, this.uploadButton]
		});

		return this.fileUploadFormPanel;

	}

	//Public Methods
	, setFormState: function(formState) {
		this.fileNameField.setValue(formState.fileName);
	}

	, getFormState: function() {
		var formState = {};

		formState.fileName = this.fileNameField.getValue();
		return formState;
	}

});