/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Ceneselli" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/
 
  
 
  
 
  

/**
 * Authors
 * 		Marco Cortella  (marco.cortella@eng.it)
 * 		
 */

//fix for IE BUG Array.isArray not supported

(function () {
    var toString = Object.prototype.toString,
         strArray = Array.toString();
 
    Array.isArray = Array.isArray || function (obj) {
        return typeof obj == "object" && (toString.call(obj) == "[object Array]" || ("constructor" in obj && String(obj.constructor) == strArray));
    }
})();

//fix for Internet Explorer: missing trim function
if(typeof String.prototype.trim !== 'function') {
	  String.prototype.trim = function() {
	    return this.replace(/^\s+|\s+$/g, ''); 
	  }
	}

Ext.ns("Sbi.tools.dataset");

Sbi.tools.dataset.FileDatasetPanel = function(config) {
	
	var defaultSettings =  {
		frame: false
	    , defaultType: 'textfield'
	    , supportedEncodings: [
	        ['windows-1252', 'windows-1252']
	        , ['UTF-8', 'UTF-8']
	        //, ['UTF-16','UTF-16']
	        //, ['US-ASCII','US-ASCII']
	        //, ['ISO-8859-1','ISO-8859-1']
	    ]
	    , defaultEncoding: 'UTF-8'	
	};

	if (Sbi.settings && Sbi.settings.tools && Sbi.settings.tools.dataset && Sbi.settings.tools.dataset.filedatasetpanel) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.tools.dataset.filedatasetpanel);
	}
	
	Sbi.trace("[FileDatasetPanel.constructor]: default encoding is equal to [" + this.defaultEncoding + "]");
	
	var c = Ext.apply(defaultSettings, config || {});
	Ext.apply(this, c);

	var panelItems;
	
	if (this.fromExt4){
		panelItems = this.initUploadFormExt4(panelItems,config);
	}else{
		panelItems = this.initUploadFormExt3(panelItems,config);
	}
	
	c = {
		items: [
		   panelItems        
		]
	};


	Sbi.tools.dataset.FileDatasetPanel.superclass.constructor.call(this, c);	 	
};

Ext.extend(Sbi.tools.dataset.FileDatasetPanel, Ext.Panel, {
	
	
	initUploadFormExt4 : function(items,config){
		if (this.isOwner == undefined) this.isOwner = true;
		
		//XLS Options Panel
		this.skipRowsField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.skiprows'),
			allowBlank : true,
			name: 'skipRows',
			value:0,
			minValue:0,
			width: 180,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        }
		});
		
		this.limitRowsField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.limitrows'),
			allowBlank : true,
			name: 'limitRows',
			minValue:0,
			width: 180,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        }
		});
		
		this.sheetNumberField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.sheetnumber'),
			allowBlank : true,
			name: 'xslSheetNumber',
			value: 1,
			minValue:1,
			width: 180,
			readOnly: !this.isOwner || false
		});		
		
		
		this.xlsOptionsPanel = new Ext.Panel({
			 fieldDefaults: {
			        labelAlign: 'top',
			        labelWidth: 150
			   },
			  margins: '50 50 50 50', 
	          bodyStyle:'padding:5px;',
	          layout: 'column',
	          width: '100%',			 
	          items: [ this.skipRowsField, this.limitRowsField, this.sheetNumberField  ]
		});
		this.xlsOptionsPanel.setVisible(false);

		
		
		//CSV Options Panel
		//not used now because CSV Reading library supports both Windows and Unix approach for EoL
		this.csvEndOfLineCombo = new Ext.form.ComboBox({
			name : 'csvEndOfLine',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvEndOfLineName',
		            'csvEndOfLineValue'
		        ],
		        data: [['Windows CR LF', '\\r\\n'], ['Unix, Mac Os X LF', '\\n']]
		    }),
			width : 150,
			fieldLabel : 'End of line Character',
			displayField : 'csvEndOfLineName', 
			valueField : 'csvEndOfLineValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        },
	        listeners: {
			    afterrender: function(combo) {
			    	if (!this.rawValue || this.rawValue == ''){
				        var recordSelected = combo.getStore().getAt(0);                     
				        combo.setValue(recordSelected.get('csvEndOfLineValue'));
			    	}
			    }
			}
		});	
		
		this.csvDelimiterCombo = new Ext.form.ComboBox({
			name : 'csvDelimiter',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvDelimiterName',
		            'csvDelimiterValue'
		        ],
		        data: [ [',', ','],[';', ';'], ['\\t', '\\t'], ['\|', '\|']]
		    }),
		    width: 180,
			fieldLabel : LN('sbi.ds.file.csv.delimiter'),
			displayField : 'csvDelimiterName', 
			valueField : 'csvDelimiterValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginLeft: '10px'
	        },
	        listeners: {
			    afterrender: function(combo) {
			    	if (!this.rawValue || this.rawValue == ''){
				        var recordSelected = combo.getStore().getAt(0);                     
				        combo.setValue(recordSelected.get('csvDelimiterValue'));
			    	}
			    }
			}
		});	
		
		this.csvQuoteCombo = new Ext.form.ComboBox({
			name : 'csvQuote',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvQuoteName',
		            'csvQuoteValue'
		        ],
		        data: [['"', '"'], ['\'', '\'']]
		    }),
		    width: 180,
			fieldLabel : LN('sbi.ds.file.csv.quote'),
			displayField : 'csvQuoteName', 
			valueField : 'csvQuoteValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginLeft: '10px'
	        },
	        listeners: {
			    afterrender: function(combo) {
			    	if (!this.rawValue || this.rawValue == ''){
				        var recordSelected = combo.getStore().getAt(0);                     
				        combo.setValue(recordSelected.get('csvQuoteValue'));
			    	}
			    }
			}
		});	
		
		
		
		this.csvEncodingCombo = new Ext.form.ComboBox({
			name : 'csvEncoding',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvEncodingName',
		            'csvEncodingValue'
		        ],
		        data: this.supportedEncodings
		    }),
		    width: 200,
			fieldLabel : 'Encoding',
			displayField : 'csvEncodingName', 
			valueField : 'csvEncodingValue', 
			typeAhead : true,
			forceSelection : true,
			value : this.defaultEncoding, //default value selected on creation
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginLeft: '10px'
	        }
		});	
		 
		this.csvOptionsPanel = new Ext.Panel({	
			  fieldDefaults: {
			        labelAlign: 'top',
			        labelWidth: 200
			  },
			  margins: '50 50 50 50',
	          bodyStyle:'padding:5px;',
	          layout: 'column',
	    	  width: '100%',
	          items: [ this.csvDelimiterCombo, this.csvQuoteCombo, this.csvEncodingCombo]
		});
		this.csvOptionsPanel.setVisible(false);		
		
		//Upload file fields
		this.fileType = new Ext.form.Field({name : 'fileType', id:'fileType', hidden:true});
		
		this.fileNameField = new Ext.form.DisplayField({
			fieldLabel : '',
			width:  300,
			allowBlank : false,
			id: 'fileNameField',
			name: 'fileName',
			readOnly:true,
			hidden:true
		});
		
	
		this.uploadField =  new Ext.form.FileUploadField({
            xtype: 'fileuploadfield',
            id: 'form-file',
//	            emptyText: '',
//	            fieldLabel: '',
            id: 'fileUploadField',
            name: 'fileUpload',
            buttonText: LN('sbi.ds.wizard.selectFile'),
            hidden: !this.isOwner || false,
            buttonOnly: true,
            hideLabel: true,
            buttonCfg: {
               // iconCls: 'upload-icon'
            	style:'padding:15px;left:50px;'
            },
            listeners: {
                'change': function(fb, v){
                    var el = Ext.getCmp('fileUploadButton');
                    el.setDisabled(false);
                    //clean the file name
                	v = v.replace("C:\\fakepath\\", "");
                	var elText = Ext.getCmp('fileDetailText');
                	elText.setText(LN('sbi.ds.wizard.selectedFile') + v);
                }
            }
         });

		this.uploadButton = new Ext.Button({
	        text: LN('sbi.ds.file.upload.button'),
	        id: 'fileUploadButton',
	        hidden: !this.isOwner || false, 
	        disabled: true,
	        style: 'top:15px;'
	    });
		
		var msgStart = LN('sbi.ds.wizard.startMsg');
		this.fileDetailText = new Ext.form.Label({
			text : msgStart,
			style: 'font-weight:bold;width:100%;text-align:center;border:0px;padding:15px;',
//			style: 'font-style:italic;width:100%;text-align:center;border:0px;padding:15px;',
			xtype: 'displayfield',
			id: 'fileDetailText',
			name: 'fileDetailText',
			readOnly:true
		});
		
		this.buttonsPanel = new Ext.Panel({
			 margins: '50 50 50 50', 
	         layout: 'vbox',
	         border: false, 
	         padding: '15px 15px 15px 230px;',
	         id: 'buttonsPanel',
	         items:[this.uploadField,this.uploadButton ]
		});
		
		//Main Panel
		this.fileUploadFormPanel = new Ext.Panel({
		  border:false,
          labelAlign: 'right',
          bodyStyle:'padding:10px 10px 120px 10px;',
          layout: 'column',
		  fileUpload: true,
		  id: 'fileUploadPanel',
		  items: [this.fileDetailText, this.buttonsPanel, this.uploadButton, this.fileNameField, this.fileType, this.csvOptionsPanel, this.xlsOptionsPanel]
		
		});
		return this.fileUploadFormPanel;

	},	

	initUploadFormExt3 : function(items,config){
		if (this.isOwner == undefined) this.isOwner = true;
		
		//XLS Options Panel
		this.skipRowsField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.skiprows'),
			allowBlank : true,
			name: 'skipRows',
			width: 100,
			value: 0,
			minValue:0,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        }
		});
		
		this.limitRowsField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.limitrows'),
			allowBlank : true,
			name: 'limitRows',
			width: 100,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        }
		});
		
		this.sheetNumberField = new Ext.form.NumberField({
			fieldLabel : LN('sbi.ds.file.xsl.sheetnumber'),
			allowBlank : true,
			name: 'xslSheetNumber',
			width: 100,
			value: 1,
			minValue:1,
			readOnly: !this.isOwner || false
		});		
		
		
		this.xlsOptionsPanel = new Ext.Panel({
			 fieldDefaults: {
			        labelAlign: 'top',
			        labelWidth: 150
			   },
			  margins: '50 50 50 50', 
	          bodyStyle:'padding:5px',
	          layout: 'form',
	          width: 500,			 
	          items: [ this.skipRowsField, this.limitRowsField, this.sheetNumberField  ]
		});
		this.xlsOptionsPanel.setVisible(false);
	
		
		
		//CSV Options Panel
		//not used now because CSV Reading library supports both Windows and Unix approach for EoL
		this.csvEndOfLineCombo = new Ext.form.ComboBox({
			name : 'csvEndOfLine',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvEndOfLineName',
		            'csvEndOfLineValue'
		        ],
		        data: [['Windows CR LF', '\\r\\n'], ['Unix, Mac Os X LF', '\\n']]
		    }),
			width : 150,
			fieldLabel : 'End of line Character',
			displayField : 'csvEndOfLineName', 
			valueField : 'csvEndOfLineValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginRight: '15px'
	        }
		});	
		
		this.csvDelimiterCombo = new Ext.form.ComboBox({
			name : 'csvDelimiter',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvDelimiterName',
		            'csvDelimiterValue'
		        ],
		        data: [[';', ';'], [',', ','], ['\\t', '\\t'], ['\|', '\|']]
		    }),
		    width: 150,
			fieldLabel : LN('sbi.ds.file.csv.delimiter'),
			displayField : 'csvDelimiterName', 
			valueField : 'csvDelimiterValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginLeft: '10px'
	        }
		});	
		
		this.csvQuoteCombo = new Ext.form.ComboBox({
			name : 'csvQuote',
			store: new Ext.data.ArrayStore({
		        fields: [
		            'csvQuoteName',
		            'csvQuoteValue'
		        ],
		        data: [['"', '"'], ['\'', '\'']]
		    }),
		    width: 150,
			fieldLabel : LN('sbi.ds.file.csv.quote'),
			displayField : 'csvQuoteName', 
			valueField : 'csvQuoteValue', 
			typeAhead : true,
			forceSelection : true,
			mode : 'local',
			triggerAction : 'all',
			selectOnFocus : true, 
			editable : false,
			allowBlank : false, 
			validationEvent : false,
			readOnly: !this.isOwner || false,
			style: {
	            marginLeft: '10px'
	        }
		});	
		 
		this.csvOptionsPanel = new Ext.Panel({	
			  fieldDefaults: {
			        labelAlign: 'top',
			        labelWidth: 200
			  },
			  margins: '50 50 50 50',
	          bodyStyle:'padding:5px',
	          layout: 'form',
	    	  width: 500,
	          items: [ this.csvDelimiterCombo, this.csvQuoteCombo]
		});
		this.csvOptionsPanel.setVisible(false);

		this.fileType = new Ext.form.Field({name : 'fileType',hidden:true});
		
		this.fileNameField = new Ext.form.DisplayField({
			fieldLabel : LN('sbi.ds.fileName'),
			width:  300,
			allowBlank : false,
			id: 'fileNameField',
			name: 'fileName',
			readOnly:true
		});
		
		this.uploadField = new Ext.form.TextField({
			inputType : 'file',
			fieldLabel : LN('sbi.generic.upload'),
			allowBlank : false,
			width: 300,
			id: 'fileUploadField',
			name: 'fileUpload',
			hidden: !this.isOwner || false
		});
		/*
		this.noChecks = new  Ext.form.Checkbox({
			fieldLabel : LN('sbi.ds.skip.checks'),
			checked: false,
			id: 'noChecks',
			name: 'SKIP_CHECKS'
		});
		*/
		
		this.uploadButton = new Ext.Button({
	        text: LN('sbi.ds.file.upload.button'),
	        id: 'fileUploadButton',
	        hidden: !this.isOwner || false
	    });
		
		//Main Panel
		this.fileUploadFormPanel = new Ext.Panel({
		  margins: '50 50 50 50',
	      labelAlign: 'left',
	      bodyStyle:'padding:5px',
	      layout: 'form',
		  defaultType: 'textfield',
		  fileUpload: true,
		  id: 'fileUploadPanel',
		  items: [this.fileNameField, this.uploadField, this.uploadButton,  this.fileType, this.csvOptionsPanel, this.xlsOptionsPanel]
	
		});

//		this.fileUploadFormPanel.layout = 'form';

		return this.fileUploadFormPanel;

	}	

	,activateFileTypePanel : function(fileTypeSelected) {
		fileTypeSelected = fileTypeSelected.toUpperCase();
		this.fileType.setValue(fileTypeSelected);
		if (fileTypeSelected != null && fileTypeSelected == 'CSV') {
			this.csvOptionsPanel.setVisible(true);
			this.xlsOptionsPanel.setVisible(false);
		} else if (fileTypeSelected != null && fileTypeSelected == 'XLS') {
			this.csvOptionsPanel.setVisible(false);
			this.xlsOptionsPanel.setVisible(true);
		}
	}
	 
	,initialActivateFileTypePanel: function(fileTypeSelected){
		if (fileTypeSelected != null && fileTypeSelected == ''){
			this.csvOptionsPanel.setVisible(false);
			this.xlsOptionsPanel.setVisible(false);
		}
		else if (fileTypeSelected != null && fileTypeSelected == 'CSV') {
			this.csvOptionsPanel.setVisible(true);
			this.xlsOptionsPanel.setVisible(false);
		} else if (fileTypeSelected != null && fileTypeSelected == 'XLS') {
			this.csvOptionsPanel.setVisible(false);
			this.xlsOptionsPanel.setVisible(true);
		}
	}
	
	//Public Methods
	, setFormState: function(formState) {
		this.fileNameField.setValue(formState.fileName);
		if (this.fromExt4 && formState.fileName !== undefined && formState.fileName !== ''){
			this.fileDetailText.setText(LN('sbi.ds.wizard.loadedFile') + formState.fileName);
		}
		if (formState.csvDelimiter != null){
			this.csvDelimiterCombo.setValue(formState.csvDelimiter);
		}
		if (formState.csvQuote != null){
			this.csvQuoteCombo.setValue(formState.csvQuote);
		}
		if (formState.csvEncoding != null && formState.csvEncoding.trim() != ''){
			this.csvEncodingCombo.setValue(formState.csvEncoding);
		}
		
		if (formState.fileType != null){
//			this.fileTypeCombo.setValue(formState.fileType);
			this.fileType.setValue(formState.fileType);
			this.initialActivateFileTypePanel(formState.fileType);
		}
		if (formState.skipRows != null){
			this.skipRowsField.setValue(formState.skipRows);
		}
		if (formState.limitRows != null){
			this.limitRowsField.setValue(formState.limitRows);
		}
		if (formState.xslSheetNumber != null){
			this.sheetNumberField.setValue(formState.xslSheetNumber);	
		}
	}
	
	, getFormState: function() {
		var formState = {};
		
		formState.fileName = this.fileNameField.getValue();
		formState.csvDelimiter = this.csvDelimiterCombo.getValue();
		formState.csvQuote = this.csvQuoteCombo.getValue();
		formState.csvEncoding = this.csvEncodingCombo.getValue();
//		formState.fileType = this.fileTypeCombo.getValue();
		formState.fileType = this.fileType.getValue();
		formState.skipRows = this.skipRowsField.getValue();
		formState.limitRows = this.limitRowsField.getValue();
		formState.xslSheetNumber = this.sheetNumberField.getValue();

		return formState;
	}

	
});