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
  * Chiara Chiarelli
  */

Ext.ns("Sbi.execution");

Sbi.execution.SaveDocumentWindow = function(config) {

	this.services = new Array();
	
	var saveDocParams= {
		LIGHT_NAVIGATOR_DISABLED: 'TRUE'
	};
	
	// case coming from createWorksheetObject.jsp
	if(config.MESSAGE_DET != undefined && config.MESSAGE_DET != null ){
		saveDocParams.MESSAGE_DET = config.MESSAGE_DET;
	
		if(config.dataset_label != undefined && config.dataset_label != null ){
			saveDocParams.dataset_label = config.dataset_label;
		}
		
		if(config.business_metadata != undefined && config.business_metadata != null ){
			saveDocParams.business_metadata = Ext.util.JSON.encode(config.business_metadata);
		}
		
	} 
	

	this.services['saveDocumentService'] = Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'SAVE_DOCUMENT_ACTION'
		, baseParams: saveDocParams
	});
	
	this.SBI_EXECUTION_ID = config.SBI_EXECUTION_ID;
	this.OBJECT_ID = config.OBJECT_ID;
	this.OBJECT_TYPE = config.OBJECT_TYPE;
	this.OBJECT_ENGINE = config.OBJECT_ENGINE;
	this.OBJECT_TEMPLATE = config.OBJECT_TEMPLATE;
	this.OBJECT_DATA_SOURCE = config.OBJECT_DATA_SOURCE;
	this.OBJECT_WK_DEFINITION = config.OBJECT_WK_DEFINITION;
	this.OBJECT_QUERY = config.OBJECT_QUERY;
	this.OBJECT_FORM_VALUES = config.OBJECT_FORM_VALUES;
	this.OBJECT_PREVIEW_FILE = config.OBJECT_PREVIEW_FILE;
	this.OBJECT_FUNCTIONALITIES = config.OBJECT_FUNCTIONALITIES;	
	this.OBJECT_SCOPE = config.OBJECT_SCOPE;
	this.isInsert = config.isInsert;
	
	this.initFormPanel(config.document || {});
	
	var c = Ext.apply({}, config, {
		id:'popup_docSave',
		layout: 'anchor', //'fit',
		width: 640,
		height:350,
		closeAction: 'close',
		buttons:[{ 
			  iconCls: 'icon-save' 	
			, handler: this.saveDocument
			, scope: this
			, text: LN('sbi.generic.update')
           }],
		title: LN('sbi.execution.saveDocument'),
		items: this.saveDocumentForm
	});   
	
    Sbi.execution.SaveDocumentWindow.superclass.constructor.call(this, c);
    this.addEvents('returnToMyAnalysis');
    
};

Ext.extend(Sbi.execution.SaveDocumentWindow, Ext.Window, {
	
	inputForm: null	
	,saveDocumentForm: null
	,isInsert: null
	,fromMyAnalysis: null
	,SBI_EXECUTION_ID: null
	,OBJECT_ID: null
	,OBJECT_TYPE: null
	,OBJECT_ENGINE: null
	,OBJECT_TEMPLATE: null
	,OBJECT_DATA_SOURCE: null
	,OBJECT_PARS: null
	,OBJECT_PREVIEW_FILE: null
	,OBJECT_FUNCTIONALITIES: null
	,OBJECT_SCOPE: null
	
	,initFormPanel: function (c){
		
		this.docLabel = new Ext.form.TextField({
	        id:'docLabel',
	        name: 'docLabel',
	        allowBlank: true, 
	        inputType: 'text',
	        maxLength: 20,
	        autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '20'},
	        anchor: '95%',
			hidden : true, //Since SpagoBI 5 the folder is setted automatically with the personal folder or by sharing action
			value: (this.isInsert)? '' : c.label 
	    });
		
		this.docName = new Ext.form.TextField({
			id: 'docName',
			name: 'docName',
			allowBlank: false, 
			inputType: 'text',
			maxLength: 200,
			autoCreate: {tag: 'input', type: 'text', autocomplete: 'off', maxlength: '200'},
			anchor: '95%',
			fieldLabel: LN('sbi.generic.name'),
			value: (this.isInsert)? '' : c.name 
		});
		
		this.docDescr = new Ext.form.TextArea({
	        id:'docDescr',
	        name: 'docDescr',
	        inputType: 'text',
	        allowBlank: true, 
	        maxLength: 400,
	        anchor:	 '95%',
	        height: 80,
			fieldLabel: LN('sbi.generic.descr'),
			value: (this.isInsert)? '' :  c.description 
	    });
	    
		this.fileUpload = this.initFileUpload();
		
	    this.inputForm = new Ext.Panel({
	         itemId: 'detail'
	        , columnWidth: 0.6
	        , items: {
		   		 id: 'items-detail',   	
	 		   	 itemId: 'items-detail',   	              
	 		   	 columnWidth: 0.4,
	             xtype: 'fieldset',
	             labelWidth: 80,
	             defaults: {border:false},    
	             defaultType: 'textfield',
	             autoHeight: true,
	             autoScroll  : true,
	             bodyStyle: Ext.isIE ? 'padding:0 0 5px 5px;' : 'padding:0px 5px;',
	             border: false,
	             style: {
	                 "margin-left": "4px",
	                 "margin-top": "25px"
	             },
	             items: [this.docName,this.docDescr,this.fileUpload]
	    	}
	    });
	    
	    this.saveDocumentForm = new Ext.Panel({
	    		  id: 'saveFormPanel',
		          frame: true,
		          autoScroll: true,
		          labelAlign: 'left',
		          autoWidth: true,
		          height: 350,// 650,
		          layout: 'fit', //'column',
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
	
	,saveDocument: function () {	
		var thisPanel = this;
		var docName = this.docName.getValue();
		var docDescr = this.docDescr.getValue();
		var query = this.OBJECT_QUERY;
		var formValues = this.OBJECT_FORM_VALUES;// the values of the form for the smart filter
		var wk_definition = this.OBJECT_WK_DEFINITION;
		var previewFile =  this.fileNameUploaded;
		
		if(formValues!=undefined && formValues!=null){
			formValues=Ext.encode(formValues);
		}
		if(query!=undefined && query!=null){
			query = Ext.util.JSON.encode(query);
		}
		if(wk_definition!=undefined && wk_definition!=null){
			wk_definition = Ext.util.JSON.encode(wk_definition);
		}
		
		if(previewFile!=undefined && previewFile!=null){
			previewFile = Ext.encode(previewFile);
		}
		
		if(docName == null || docName == undefined || docName == ''){
				Ext.MessageBox.show({
	                title: LN('sbi.generic.warning'),
	                msg:  LN('sbi.document.saveWarning2'),
	                width: 180,
	                buttons: Ext.MessageBox.OK
	           });
		}else{	
			var params = {
		        	name :  docName,
		        	description : docDescr,
		        	obj_id: this.OBJECT_ID,
					typeid: this.OBJECT_TYPE,
					wk_definition: wk_definition,
					previewFile: previewFile,
					query: query,
					formValues: formValues,
					//engineid: this.OBJECT_ENGINE,
					template: this.OBJECT_TEMPLATE,
					datasourceid: this.OBJECT_DATA_SOURCE,
					SBI_EXECUTION_ID: this.SBI_EXECUTION_ID,
					isPublic: this.OBJECT_SCOPE
		        };
			
			//defines the document label internally (Since SpagoBI 5 is not more visible in the GUI)
			if (this.isInsert == undefined || this.isInsert == null || this.isInsert == true){
				params.label = (this.OBJECT_TYPE == 'WORKSHEET')? 'ws__' : 'map__';
				params.label +=  Math.floor((Math.random()*1000000000)+1); 
				params.MESSAGE_DET = 'DOC_SAVE';	
			}else{
				params.label = this.docLabel.getValue();
				if (this.OBJECT_TYPE == 'MAP'){
					params.MESSAGE_DET = 'MODIFY_GEOREPORT';
				}else{
					params.MESSAGE_DET = 'DOC_UPDATE';					
				}				
			}
			
			Ext.Ajax.request({
		        url: this.services['saveDocumentService'],
		        params: params,
		        success : function(response , options) {
			      		if(response !== undefined && response.responseText !== undefined) {
			      			var content = Ext.util.JSON.decode( response.responseText );
			      			if(content.responseText !== 'Operation succeded') {
			                    Ext.MessageBox.show({
			                        title: LN('sbi.generic.error'),
			                        msg: content,
			                        width: 150,
			                        buttons: Ext.MessageBox.OK
			                   });              
				      		}else{			
				      			Ext.MessageBox.show({
				                        title: LN('sbi.generic.result'),
				                        msg: LN('sbi.execution.savedocumentwindow.saved'),
				                        width: 200,
				                        buttons: Ext.MessageBox.OK				                       
				                });
				      			if (this.fromMyAnalysis != undefined && this.fromMyAnalysis != null && this.fromMyAnalysis == true){
				      				this.fireEvent('returnToMyAnalysis',this);  //fire event to jump to the MyAnalysis page 
				                }
				      			thisPanel.close();
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
	
	, initFileUpload: function(){
		//upload preview file
		var config={
				fromExt4:false, 
				isEnabled: true, 
				labelFileName:'Preview file'
		};
		var c = Ext.apply({}, config);
		Ext.apply(this,c);
		
		this.fileUpload = new Sbi.widgets.FileUploadPanel(c);

		var uploadButton = this.fileUpload.fileUploadFormPanel.getComponent('fileUploadButton');	
		uploadButton.setHandler(this.uploadFileButtonHandler,this);
		var toReturn = new  Ext.FormPanel({
			  id: 'previewFileForm',
			  fileUpload: true, // this is a multipart form!!
			  isUpload: true,
			  border:false,
			  method: 'POST',
			  enctype: 'multipart/form-data',
	          labelAlign: 'left',
	          bodyStyle:'padding:1px',
	          autoScroll:true,
	          trackResetOnLoad: true,
	          items: this.fileUpload
	      });
		
		return toReturn;
	}
	
	//handler for the upload file button
	,uploadFileButtonHandler: function(btn, e) {
		
		Sbi.debug("[SaveDocumentWindow.uploadFileButtonHandler]: IN");
		
        var form = Ext.getCmp('previewFileForm').getForm();
        
        Sbi.debug("[SaveDocumentWindow.uploadFileButtonHandler]: form is equal to [" + form + "]");
		
        var completeUrl =  Sbi.config.serviceRegistry.getServiceUrl({
					    		serviceName : 'MANAGE_PREVIEW_FILE_ACTION',
					    		baseParams : {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
					    	});

		var baseUrl = completeUrl.substr(0, completeUrl
				.indexOf("?"));
		
		Sbi.debug("[SaveDocumentWindow.uploadFileButtonHandler]: base url is equal to [" + baseUrl + "]");
	 	
		var queryStr = completeUrl.substr(completeUrl.indexOf("?") + 1);
		var params = Ext.urlDecode(queryStr);
		params.operation = 'UPLOAD';
 
		Sbi.debug("[SaveDocumentWindow.uploadFileButtonHandler]: form is valid [" + form.isValid() + "]");		
		this.fileNameUploaded = Ext.getCmp('fileUploadField').getValue();
		this.fileNameUploaded = this.fileNameUploaded.replace("C:\\fakepath\\", "");

		form.submit({
			clientValidation: false,
			url : baseUrl // a multipart form cannot
							// contain parameters on its
							// main URL; they must POST
							// parameters
			,
			params :  params,
			waitMsg : LN('sbi.generic.wait'),
			success : function(form, action) {
				Ext.MessageBox.alert('Success!','File Uploaded to the Server');
				this.fileNameUploaded = action.result.fileName;
			},
			failure : function(form, action) {
				if (action.result && action.result.msg) {
	    			Ext.Msg.show({
	    				   title: LN('sbi.generic.error'),
	    				   msg: action.result.msg,
	    				   buttons: Ext.Msg.OK,
	    				   icon: Ext.MessageBox.ERROR
	    			});
				} else {
					Ext.Msg.alert('Failure', 'Error while uploading file');
				}
			},
			scope : this
		});		
		
		Sbi.debug("[SaveDocumentWindow.uploadFileButtonHandler]: OUT");
	}
	
});