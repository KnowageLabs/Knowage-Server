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
	        fromExt4: false,
	        isEnabled: true,
	        border: false,
	        layout: 'fit'
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

		this.uploadField = new Ext.form.TextField({
			inputType:	'file',
			fieldLabel : config.labelFileName || LN('sbi.ds.fileName'),
			id: 'fileUploadField',
			style: 'padding-left: 5px',
			columnWidth: 0.8,
			allowBlank: true
		});

		this.uploadButton = new Ext.Button({
			id: 			'fileUploadButton',			
			xtype:          'button',
        	handler:		this.uploadImgButtonHandler,
        	columnWidth:	0.1,
        	scope: 			this,
//        	tooltip: 		LN('sbi.worksheet.designer.sheettitlepanel.uploadimage'),
        	style:			'padding-left: 10px',
        	iconCls:		'uploadImgIcon',
        	hidden: 		!this.isEnabled || false
	    });
		
		//Panel with the load file field
		this.fileUploadFormPanel = new Ext.Panel({
			id: 'file-upload-panel',
			height: 60, //45,
			layout:'column',
//			frame: true,
			header: false,
			border: false,
			items: [this.uploadField,			        
			        this.uploadButton
			        ]
		});

		if (!this.fromExt4) {
			this.fileUploadFormPanel.layout = 'form';
		}
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