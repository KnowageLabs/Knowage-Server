/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


  
Ext.define('Sbi.execution.SaveDocumentWindowExt4', {
	extend: 'Ext.Window'
	
	,inputForm: null
	,saveDocumentForm: null
	,fileNameUploaded: null
	,SBI_EXECUTION_ID: null
	,OBJECT_ID: null
	,OBJECT_TYPE: null
	,OBJECT_ENGINE: null
	,OBJECT_TEMPLATE: null
	,OBJECT_DATA_SOURCE: null
	,OBJECT_PARS: null
	,OBJECT_PREVIEW_FILE: null
	,OBJECT_COMMUNITY: null
	,OBJECT_SCOPE: null
	,fromMyAnalysis: null
	,fromDocBrowser: null
	,id: null
	
	,constructor: function(config) {

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
				saveDocParams.business_metadata = Ext.JSON.encode(config.business_metadata);
			}
			
			if(config.model_name != undefined && config.model_name != null ){
				saveDocParams.model_name = config.model_name;
			}
			
			
		} else{
			saveDocParams.MESSAGE_DET = 'DOC_SAVE';		
		}
		
		/*
		if (config.fromMyAnalysis != undefined && config.fromMyAnalysis != null){
			if (config.fromMyAnalysis == 'TRUE'){
				this.fromMyAnalysis = true;
			}
		}
		*/
		
	
		this.services['saveDocumentService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_DOCUMENT_ACTION'
			, baseParams: saveDocParams
		});
		this.services['getCommunities'] =  Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'community/user'
				, baseParams: {
					LIGHT_NAVIGATOR_DISABLED: 'TRUE',
					EXT_VERSION: "3"
				}
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
		this.OBJECT_COMMUNITY = config.OBJECT_COMMUNITY;
		this.OBJECT_SCOPE = config.OBJECT_SCOPE;
		
		this.initFormPanel();
		
		var c = Ext.apply({}, config, {
			id:'popup_docSave',
			layout:'fit',
			width: 450, //640,
			height: 300,//450,
			closeAction: 'destroy',
			buttons:[{ 
				  iconCls: 'icon-save' 	
				, handler: this.saveDocument
				, scope: this
				, text: LN('sbi.generic.update')
	           }],
			title: LN('sbi.execution.saveDocument'),
			items: this.saveDocumentForm
		});   
		
		Ext.apply(this,c);
		
	    this.callParent(arguments);
		this.addEvents('returnToMyAnalysis');

	    
	}

	
	,initFormPanel: function (){
		
		this.docName = Ext.create("Ext.form.TextField", {
			id: 'docName',
			name: 'docName',
			allowBlank: false, 
			inputType: 'text',
			maxLength: 200,
			enforceMaxLength: true,
			anchor: '95%',
			fieldLabel: LN('sbi.generic.name') 
		});
		
		this.docLabel =  Ext.create("Ext.form.TextField",{
	        id:'docLabel',
	        name: 'docLabel',
	        allowBlank: false, 
	        inputType: 'text',
	        maxLength: 20,
	        enforceMaxLength: true,
	        anchor: '95%',
			fieldLabel: LN('sbi.generic.label'),
			hidden: true //since SpagoBI 5
	    });
		
		this.docDescr =  Ext.create("Ext.form.TextArea",{
	        id:'docDescr',
	        name: 'docDescr',
	        inputType: 'text',
	        allowBlank: true, 
	        maxLength: 400,
	        anchor:	 '95%',
	        height: 80,
			fieldLabel: LN('sbi.generic.descr')  
	    });
		
		this.docVisibility = Ext.create("Ext.form.field.Checkbox",{
			fieldLabel:'Document Visibility',
			id:'docVisibility',
	        boxLabel  : 'Visible',
            name      : 'docVisibility',
            inputValue: 1,
            checked   : true,
            hidden	  : true //since SpagoBI 5
           });
		// The data store holding the communities
		var storeComm = Ext.create('Ext.data.Store', {
			proxy:{
				type: 'rest',
				url : this.services['getCommunities'],
				reader: {
					type: 'json',
					root: 'root'
				}
			},

			fields: [
			         "communityId",
			         "name",
			         "description",
			         "owner",
			         "functCode"],
		    autoLoad: true
		});

		this.docCommunity = Ext.create('Ext.form.ComboBox', {
		    fieldLabel: 'Community',
		    queryMode: 'local',
		    store: storeComm,
		    displayField: 'name',
		    valueField: 'functCode',
		    allowBlank: true,
		    hidden: true //since SpagoBI 5
		});
			
		
//		if (Sbi.settings.saveWindow && Sbi.settings.saveWindow.showScopeInfo && Sbi.settings.saveWindow.showScopeInfo == true){
			var storeScope = Ext.create('Ext.data.Store', {
			    fields: ['field', 'value'],
			    data : [
			        {"field":"true", "value":"Public"},
			        {"field":"false", "value":"Private"}
			    ]
			}); 
			
			this.isPublic = Ext.create('Ext.form.ComboBox', {
			    fieldLabel: 'Scope',
	//		    queryMode: 'local',
			    store: storeScope,
			    displayField: 'value',
			    valueField: 'field',
			    allowBlank: false,
			    hidden: true //since SpagoBI 5
			});
//		}
		
		this.fileUpload = this.initFileUpload();
	    
	    this.inputForm =  Ext.create("Ext.Panel",{
	         itemId: 'detail'
	        , columnWidth: 0.6
	        , border: false
	        , items: {
	 		   	 columnWidth: 0.4,
	             xtype: 'fieldset',
	             labelWidth: 80,
	             defaults: {border:false},    
	             defaultType: 'textfield',
	             autoScroll  : true,
	             border: false,
	             style: {
	                 "margin-left": "4px",
	                 "margin-top": "25px"
	             },
	             items: [this.docLabel,this.docName,this.docDescr,this.docVisibility,this.fileUpload, this.docCommunity, this.isPublic]
	    	}
	    });
	    
	    /* Since SpagoBI 5 the folder is setted automatically with the personal folder
	    this.treePanel =  Ext.create("Sbi.browser.DocumentsTree",{
	    	  columnWidth: 0.4,
	          border: false,

	          drawUncheckedChecks: true
	    });
	    */
	    
	    this.saveDocumentForm =  Ext.create("Ext.form.FormPanel",{
		          autoScroll: true,
		          labelAlign: 'left',
		          autoWidth: true,
		          height: 350,
		          layout: 'fit',
//		          columnWidth:	1, //0.1,
		          scope:this,
		          forceLayout: true,
		          trackResetOnLoad: true,
		          layoutConfig : {
		 				animate : true,
		 				activeOnTop : false

		 			},
		          items: [
		              this.inputForm
		            //  , this.treePanel    //since SpagoBI 5 no folder are selected at this time       	  		
		          ]
		          
		      });
	}
	
	,saveDocument: function () {
		 
		var docName = this.docName.getValue();
		//defines the document label internally (Since SpagoBI 5 is not more visible in the GUI)
//		var docLabel = this.docLabel.getValue();		
		var docLabel = (this.OBJECT_TYPE == 'WORKSHEET')? 'ws__' : 'map__';
		docLabel +=  Math.floor((Math.random()*1000000000)+1); 
		var docDescr = this.docDescr.getValue();
		var docVisibility = this.docVisibility.getValue();
		//var functs = this.treePanel.returnCheckedIdNodesArray();
		var query = this.OBJECT_QUERY;
		var formValues = this.OBJECT_FORM_VALUES;// the values of the form for the smart filter
		var wk_definition = this.OBJECT_WK_DEFINITION;
		var previewFile =  this.fileNameUploaded;
		var docCommunity = this.docCommunity.getValue();
		var isPublic = (this.isPublic) ? this.isPublic.getValue() : false;
		
		if(formValues!=undefined && formValues!=null){
			formValues=Ext.encode(formValues);
		}
		if(query!=undefined && query!=null){
			query = Ext.JSON.encode(query);
		}
		if(wk_definition!=undefined && wk_definition!=null){
			wk_definition = Ext.JSON.encode(wk_definition);
		}
		
		if(previewFile!=undefined && previewFile!=null){
			previewFile = Ext.JSON.encode(previewFile);
		}
		
		if(docName == null || docName == undefined || docName == '' ||
		   docLabel == null || docLabel == undefined || docLabel == '' 
//		   ((functs == null || functs == undefined || functs.length == 0) &&
//				   (docCommunity == null || docCommunity == undefined || docCommunity == ''))
			){
				Ext.MessageBox.show({
	                title: LN('sbi.generic.warning'),
	                msg:  LN('sbi.document.saveWarning2'),
	                width: 180,
	                buttons: Ext.MessageBox.OK
	           });
		}else{	
//			functs = Ext.JSON.encode(functs);
			var params = {
		        	name :  docName,
		        	label : docLabel,
		        	description : docDescr,
		        	visibility: docVisibility,
		        	obj_id: this.OBJECT_ID,
					typeid: this.OBJECT_TYPE,
					wk_definition: wk_definition,
					previewFile: previewFile,
					query: query,
					formValues: formValues,
					template: this.OBJECT_TEMPLATE,
					datasourceid: this.OBJECT_DATA_SOURCE,
					communityid: docCommunity,
					isPublic: isPublic,
					SBI_EXECUTION_ID: this.SBI_EXECUTION_ID
//					functs: functs
		        };
			
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
				      			var thisWindow = this;
				      			Ext.MessageBox.show({
				                        title: LN('sbi.generic.result'),
				                        msg: LN('sbi.generic.resultMsg'),
				                        width: 200,
				                        //buttons: Ext.MessageBox.OK
				                        buttonText: { 
				                        	 ok: 'Ok'
				                        }
					      			, fn: function(btn){
						                //jump back to MyAnalysis page
						                if (thisWindow.fromMyAnalysis != undefined && thisWindow.fromMyAnalysis != null && thisWindow.fromMyAnalysis == 'TRUE'){
						                	thisWindow.fireEvent('returnToMyAnalysis',thisWindow);  //fire event to jump to the MyAnalysis page 
						                }else if (thisWindow.fromDocBrowser != undefined && thisWindow.fromDocBrowser != null && thisWindow.fromDocBrowser == 'TRUE'){
						                	thisWindow.fireEvent('returnToDocBrower',thisWindow);  //fire event to jump to the DocBrowser page 
						                }
		                            	
		                                Ext.MessageBox.hide(); 
		                                if (this.destroy) this.destroy();
					      		    }
				                });

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
				fromExt4:true, 
				isEnabled: true, 
				labelFileName:'Preview file'				
		};
		var c = {};
		if (Sbi.settings.widgets.FileUploadPanel && Sbi.settings.widgets.FileUploadPanel.imgUpload)
			c = Ext.apply({}, config, Sbi.settings.widgets.FileUploadPanel.imgUpload); 		
		else
			c = Ext.apply({}, config); 	
		Ext.apply(this,c);
		
		this.fileUpload = new Sbi.widgets.FileUploadPanel(c);
//		if (this.record !== undefined){
//			this.fileUpload.setFormState(this.record);
//		}
//		var uploadButton = this.fileUpload.getComponent('fileUploadPanel').getComponent('fileUploadButton');	
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
		
		Sbi.debug("[SaveDocumentWindowExt4.uploadFileButtonHandler]: IN");
		
        var form = Ext.getCmp('previewFileForm').getForm();
        
        Sbi.debug("[SaveDocumentWindowExt4.uploadFileButtonHandler]: form is equal to [" + form + "]");
		
        var completeUrl =  Sbi.config.serviceRegistry.getServiceUrl({
					    		serviceName : 'MANAGE_PREVIEW_FILE_ACTION',
					    		baseParams : {LIGHT_NAVIGATOR_DISABLED: 'TRUE'}
					    	});

		var baseUrl = completeUrl.substr(0, completeUrl
				.indexOf("?"));
		
		Sbi.debug("[SaveDocumentWindowExt4.uploadFileButtonHandler]: base url is equal to [" + baseUrl + "]");
	 	
		var queryStr = completeUrl.substr(completeUrl.indexOf("?") + 1);
		var params = Ext.urlDecode(queryStr);
		params.operation = 'UPLOAD';
 
		Sbi.debug("[SaveDocumentWindowExt4.uploadFileButtonHandler]: form is valid [" + form.isValid() + "]");		
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
		
		Sbi.debug("[SaveDocumentWindowExt4.uploadFileButtonHandler]: OUT");
	}

});