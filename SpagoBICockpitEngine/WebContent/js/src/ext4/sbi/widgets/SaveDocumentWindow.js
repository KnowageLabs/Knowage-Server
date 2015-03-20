/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.widgets");

Sbi.widgets.SaveDocumentWindow = function(config) {
		this.services = new Array();

		var saveDocParams= {
			LIGHT_NAVIGATOR_DISABLED: 'TRUE',
			standardUrl:true
		};

		if(config.MESSAGE_DET != undefined && config.MESSAGE_DET != null ){
			saveDocParams.MESSAGE_DET = config.MESSAGE_DET;

			if(config.dataset_label != undefined && config.dataset_label != null ){
				saveDocParams.dataset_label = config.dataset_label;
			}

		} else{
			saveDocParams.MESSAGE_DET = 'DOC_SAVE';
		}

		this.services['saveDocumentService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_DOCUMENT_ACTION'
			, baseParams: saveDocParams
			, baseUrl:{contextPath: 'SpagoBI', controllerPath: 'servlet/AdapterHTTP'}
		});


		this.SBI_EXECUTION_ID = config.SBI_EXECUTION_ID;
		this.OBJECT_ID = config.OBJECT_ID;
		this.OBJECT_TYPE = config.OBJECT_TYPE;
		this.OBJECT_ENGINE = config.OBJECT_ENGINE;
		this.OBJECT_TEMPLATE = config.OBJECT_TEMPLATE;
		this.OBJECT_DATA_SOURCE = config.OBJECT_DATA_SOURCE;
		this.OBJECT_PREVIEW_FILE = config.OBJECT_PREVIEW_FILE;
		this.OBJECT_FUNCTIONALITIES = config.formState.OBJECT_FUNCTIONALITIES;
		this.isInsert = config.isInsert;

		this.initFormPanel(config.formState);

		var c = Ext.apply({}, config, {
			id:'popup_docSave',
			layout:'fit',
			width:500,
			height:300,
			resizable:false,
			modal: true,
			closeAction: 'destroy',
			buttons:[{
				  iconCls: 'icon-save'
				, handler: this.saveDocument
				, scope: this
				, text: LN('sbi.generic.save')
	           },{
				  iconCls: 'icon-saveAndGoBack'
				, handler: this.saveDocumentAndGoBack
				, scope: this
				, text: LN('sbi.generic.saveAndGoBack')
	           }, {
               	text:  LN('sbi.generic.cancel')
              , handler: this.closeWin
              , scope: this
            }],
			title: LN('sbi.savewin.title'),
			items: this.saveDocumentForm
		});

		Ext.apply(this,c);

		// init events...
		this.addEvents('savedocument','closedocument');

		// constructor
		Sbi.widgets.SaveDocumentWindow.superclass.constructor.call(this, c);

	};

Ext.extend(Sbi.widgets.SaveDocumentWindow, Ext.Window, {
	msgArea: null,
	inputForm: null,
	saveDocumentForm: null,
	fileNameUploaded: null,
	fromMyAnalysis: null,
	SBI_EXECUTION_ID: null,
	OBJECT_ID: null,
	OBJECT_TYPE: null,
	OBJECT_ENGINE: null,
	OBJECT_TEMPLATE: null,
	OBJECT_DATA_SOURCE: null,
	OBJECT_PARS: null,
	OBJECT_PREVIEW_FILE: null,
	OBJECT_SCOPE: null,
	OBJECT_FUNCTIONALITIES: null,
	isInsert: false,

	initFormPanel: function (c){
		this.docVisibility = c.visibility;
		this.isPublic = c.isPublic;
//		this.docCommunity = this.OBJECT_COMMUNITIES_CODE;

		this.docLabel =  new Ext.form.TextField({
			id:'docLabel',
	        name: 'docLabel',
	        height: 0,
			value:c.docLabel,
			hidden:true
	    });
		this.docLabel.setValue(c.docLabel);

		this.docName = new Ext.form.TextField({
			id: 'docName',
			name: 'docName',
			allowBlank: false,
			inputType: 'text',
			maxLength: 200,
			enforceMaxLength: true,
			anchor : '95%', //350,
			fieldLabel:LN('sbi.savewin.name') ,
			value:c.docName
	    });
		this.docName.setValue(c.docName);

		this.docDescr = new Ext.form.TextArea({
			id:'docDescr',
	        name: 'docDescr',
			maxLength : 400,
			xtype : 'textarea',
			anchor : '95%',
			height : 80,
			autoScroll: true,
			fieldLabel : LN('sbi.savewin.description') ,
			allowBlank : true,
			value:c.docDescr
		});
		this.docDescr.setValue(c.docDescr);

		this.fileUpload = this.initFileUpload();

	    this.inputForm = new Ext.Panel({
	         itemId: 'detail'
	        , columnWidth: 1
	        , border: false
	        , items: {
	             xtype: 'fieldset',
	             labelWidth: 80,
	             defaults: {border:false},
	             defaultType: 'textfield',
	             autoScroll  : true,
	             border: false,
	             items: [this.docName,this.docDescr, this.fileUpload]
	    	}
	    });

	    this.saveDocumentForm =  new Ext.Panel({
		          autoScroll: true,
		          labelAlign: 'left',
		          autoWidth: true,
		          height: 250,
		          layout: 'column',
		          columnWidth:	0.1,
		          scope:this,
		          forceLayout: true,
		          trackResetOnLoad: true,
		          layoutConfig : {
		 				animate : true,
		 				activeOnTop : false

		 			},
		          items: [
		                 this.inputForm
		          ]

		      });
	}

	, saveDocument: function () {
		this.save(false);
	}

	, saveDocumentAndGoBack: function () {
		this.save(true);
	}


	, save: function (goBack) {

		var docLabel = this.docLabel.getValue();
		var docName = this.docName.getValue();
		var docDescr = this.docDescr.getValue();
		var previewFile =  this.fileNameUploaded;

		if(previewFile!=undefined && previewFile!=null){
			previewFile = Ext.JSON.encode(previewFile);
		}

		if(docName == null || docName == undefined || docName == '' ){
				var msgWarning = LN('sbi.savewin.saveWarning');
				Ext.MessageBox.show({
	                title: LN('sbi.generic.warning'),
	                msg: msgWarning,
	                //width: 180,
	                buttons: Ext.MessageBox.OK
	           });
		}else{
			this.showMask();
			var functs = Ext.JSON.encode(this.OBJECT_FUNCTIONALITIES);
			var params = {
		        	name :  docName,
		        	label : docLabel,
		        	description : docDescr,
		        	obj_id: this.OBJECT_ID,
					typeid: this.OBJECT_TYPE,
					previewFile: previewFile,
					template: this.OBJECT_TEMPLATE,
					datasourceid: this.OBJECT_DATA_SOURCE,
					visibility: this.docVisibility,
					isPublic: this.isPublic,
					SBI_EXECUTION_ID: this.SBI_EXECUTION_ID,
					functs: functs

		        };
			Sbi.config.docName = docName;
			Sbi.config.docDescription = docDescr;

			Ext.Ajax.request({
		        url: this.services['saveDocumentService'],
		        params: params,
		        success : function(response , options) {
			      		if(response !== undefined && response.responseText !== undefined) {
			      			var content = Ext.JSON.decode( response.responseText );
			      			if(content.responseText !== 'Operation succeded') {
			                    Ext.MessageBox.show({
			                        title: LN('sbi.generic.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });
				      		}else{
				      			Ext.MessageBox.show({
				                       title: LN('sbi.generic.resultMsg'),
				                        msg: LN('sbi.generic.resultMsg'),
				                        width: 200,
				                        buttons: Ext.MessageBox.OK
				                });

//				      			if (goBack){
//				      				this.fireEvent('closedocument', this);  	 //fire event to jump to the MyAnalysis page
//				                }
				      			Sbi.config.docLabel = this.docLabel.value;

				      			this.fireEvent('savedocument', this, goBack, params);
				      			this.destroy();
				      			this.hideMask();
				      		}
			      		} else {
			      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			      		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure
			});
		}
	}

	, closeWin: function(){
		this.close();
	}

	, initFileUpload: function(){
		//upload preview file
		var config={
				isEnabled: true,
				labelFileName:'Preview file'
		};
		var c = {};
		if (Sbi.settings.widgets.FileUploadPanel && Sbi.settings.widgets.FileUploadPanel.imgUpload){c = Ext.apply({}, config, Sbi.settings.widgets.FileUploadPanel.imgUpload);} else {c = Ext.apply({}, config);}
		Ext.apply(this,c);

		this.fileUpload = new Sbi.widgets.FileUploadPanel(c);

		var uploadButton = this.fileUpload.fileUploadFormPanel.getComponent('fileUploadButton');
		uploadButton.setHandler(this.uploadFileButtonHandler,this);
		var toReturn = new  Ext.FormPanel({
			  id: 'mapPreviewFileForm',
			  fileUpload: true, // this is a multipart form!!
			  isUpload: true,
			  border:false,
//			  layout:'form',
			  enctype:'multipart/form-data',
			  method: 'POST',
	          labelAlign: 'left',
	          bodyStyle:'padding:1px',
	          autoScroll:true,
	          trackResetOnLoad: true,
	          items: [this.fileUpload]
	      });

		return toReturn;
	}

	//handler for the upload file button
	,uploadFileButtonHandler: function(btn, e) {

		Sbi.debug("[PreviewFileWizard.uploadFileButtonHandler]: IN");
        var form = Ext.getCmp('mapPreviewFileForm').getForm();

        Sbi.debug("[PreviewFileWizard.uploadFileButtonHandler]: form is equal to [" + form + "]");

        var completeUrl =  Sbi.config.serviceRegistry.getServiceUrl({
					    		serviceName : 'MANAGE_FILE_ACTION',
					    		baseParams : {LIGHT_NAVIGATOR_DISABLED: 'TRUE', standardUrl:true},
					    		baseUrl:{contextPath: 'SpagoBI', controllerPath: 'servlet/AdapterHTTP'}
					    	});

		var baseUrl = completeUrl.substr(0, completeUrl.indexOf("?"));

		Sbi.debug("[PreviewFileWizard.uploadFileButtonHandler]: base url is equal to [" + baseUrl + "]");

		var queryStr = completeUrl.substr(completeUrl.indexOf("?") + 1);
		var params = Ext.urlDecode(queryStr);
		params.operation = 'UPLOAD';
		params.directory = this.directory || '';
		params.maxSize = this.maxSizeFile || '';
		params.extFiles = Ext.JSON.encode(this.extFiles) || '';


		Sbi.debug("[PreviewFileWizard.uploadFileButtonHandler]: form is valid [" + form.isValid() + "]");
		this.fileNameUploaded = Ext.getCmp('fileUploadField').getValue();
		this.fileNameUploaded = this.fileNameUploaded.replace("C:\\fakepath\\", "");

		form.submit({
			clientValidation: false,
			url : baseUrl // a multipart form cannot
							// contain parameters on its
							// main URL; they must POST
							// parameters
			,
			params : params,
			waitMsg : LN('sbi.generic.wait'),
			success : function(form, action) {
				Ext.MessageBox.alert('Success!','File Uploaded to the Server');
				this.fileNameUploaded = action.result.fileName;
			},
			failure : function(form, action) {
				switch (action.failureType) {
	            case Ext.form.Action.CLIENT_INVALID:
	                Ext.Msg.alert('Failure', 'Form fields may not be submitted with invalid values');
	                break;
	            case Ext.form.Action.CONNECT_FAILURE:
	                Ext.Msg.alert('Failure', 'Ajax communication failed');
	                break;
	            case Ext.form.Action.SERVER_INVALID:
	            	if(action.result.msg && action.result.msg.indexOf("NonBlockingError:")>=0){
	            		var error = Ext.JSON.decode(action.result.msg);
	            		Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.ds.'+error.error),LN("sbi.ds.failedToUpload"));
	            	}else{
	            		Sbi.exception.ExceptionHandler.showErrorMessage(action.result.msg,'Failure');
	            	}

				}
			},
			scope : this
		});

		Sbi.debug("[PreviewFileWizard.uploadFileButtonHandler]: OUT");
	}

	/**
	 * Opens the loading mask
	 */
    , showMask : function(){
    	if (this.loadMask == null) {
    		this.loadMask = new Ext.LoadMask(this.getId(), {msg: "Saving.."});
    	}
    	this.loadMask.show();
    }

	/**
	 * Closes the loading mask
	 */
	, hideMask: function() {
    	if (this.loadMask != null) {
    		this.loadMask.hide();
    	}
	}

});