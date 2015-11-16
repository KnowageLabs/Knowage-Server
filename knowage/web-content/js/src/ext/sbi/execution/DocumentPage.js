/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/


Ext.ns("Sbi.execution");

Sbi.execution.DocumentPage = function(config, doc) {
	
	// init properties...
	var defaultSettings = {
		// public
		eastPanelWidth: 300
		// private
		, state: 'BLANK'
	};
	
	if (Sbi.settings && Sbi.settings.execution && Sbi.settings.execution.documentpage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.execution.documentpage);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);
		
	// add events
    this.addEvents('beforesynchronize', 'loadurlfailure', 'crossnavigation', 'closeDocument', 'directExport');

	// declare exploited services
	this.initServices();
	this.init(config, doc);    
   
	var c = Ext.apply({}, config, {
		id: 'documentexecutionpage' + Ext.id()
		, layout: 'border'
		, border : false
		, items: [this.miframe]
	});
	
	// constructor
    Sbi.execution.DocumentPage.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.execution.DocumentPage
 * @extends Ext.Panel
 * 
 * bla bla bla bla bla ...
 */

/**
 * @cfg {Object} config
 * ...
 */
Ext.extend(Sbi.execution.DocumentPage, Ext.Panel, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	
	/**
	 * @property {Ext.ux.ManagedIframePanel} the embedded miframe
	 */
	, miframe: null
	
	/**
     * @property {String} state
     * The state of the page. Can be one of the following:
     * 
     *   - BLANK: no document executed yet. It's the initial state before the method #synchronize is invoked
     *   - LOADING: the url of document to execute has been retrieved successfully and it has been passed to the embedded miframe
     *   - FAILURE: an error occurred while retrieving the url of the document to execute
     *   - LOADED: the url of the document to execute has been successfully loaded in the embedded mifreme
     *   
     * Initial default value is BLANK
     * 
     * NOTE: It's not handled the case in which the url of the document to execute is successfully retrieved but an error occured while
     * loading in in the embedded miframe.
     *   
     */
	, state: null
	
	/**
	 * @property {String} the definition of cross navigation function
	 */
	, EXEC_CROSSNAVIGATION_FN_BODY: "parent.execCrossNavigation = function(d,l,p,s,ti,t,pw,ph) {" +
	"	sendMessage({'label': l, parameters: p, windowName: d, subobject: s, target: t, title: ti, width: pw, height: ph},'crossnavigation');" +
	"};"
	
	
   
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
		
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * @method
	 * 
	 * @return {Ext.ux.ManagedIframePanel} the miframe tha contains the executed document
	 */
	, getMiFrame: function() {
		return this.miframe;
	}

	/**
	 * @method 
	 * 
	 * Returns the state of the page. Can be one of the following:
     * 
     *   - BLANK: no document executed yet. It's the initial state before the method #synchronize is invoked
     *   - LOADING: the url of document to execute has been retrieved successfully and it has been passed to the embedded miframe
     *   - FAILURE: an error occurred while retrieving the url of the document to execute
     *   - LOADED: the url of the document to execute has been successfully loaded in the embedded mifreme
	 * 
	 * @return {String} The state of the panel
	 */
	, getState: function() {
		return this.state;
	}
	

	//-----------------------------------------------------------------------------------------------------------------
	// public methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Send a message to the document by means of the API provided by the embedded miframe
	 * 
	 * @param {Mixed} message The message payload 
	 * @param {String} tag Optional reference tag
	 */
	, sendMessage: function(message, tag) {
		this.miframe.sendMessage(message, tag);
	}
	
	
	 /**
	  * @method 
	  *  
	  * eval a javascript code block(string) within the context of the embedde miframe

      * @param {String} block A valid ('eval'able) script source block.
      * @param {Boolean} useDOM  if true, inserts the function
      * into a dynamic script tag, false does a simple eval on the function
      * definition. (useful for debugging) <p> Note: will only work after a
      * successful iframe.(Updater) update or after same-domain document has
      * been hooked, otherwise an exception is raised.
      */
    , execScript : function(block, useDOM) {
    	this.miframe.iframe.execScript(block, useDOM);
    }
	
    /**
	 * @method 
	 * 
	 * Inject the cross navigation function in the document loaded in the embedded miframe
	 */
    , injectCrossNavigationFunction: function() {
    	this.execScript(this.EXEC_CROSSNAVIGATION_FN_BODY, true);
    }
    
	/**
	 * @method 
	 * 
	 * Extend session (keep alive)
	 */
	, extendSession: function() {
		Sbi.commons.Session.extend();
	}
	
	/**
	 * @method 
	 * 
	 * Opens the loading mask 
	 */
	, showMask : function(message){
		if (this.loadMask == null) {
			this.loadMask = new Ext.LoadMask(this.getId(), {msg: message});
		}
		this.loadMask.show();
	}
	
	/**
	 * @method 
	 * 
	 * Closes the loading mask
	 * 
	 */
	, hideMask: function() {
		if (this.loadMask != null) {
			this.loadMask.hide();
		}
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
    // NOTE: the following methods initialize the interface with empty widgets. There are not yet a specific execution 
    // instance to work on. The interface itself can change then when synchronization methods
    // are invoked passing in a specific execution instance.
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - getUrlForExecutionService: get the execution url (by default GET_URL_FOR_EXECUTION_ACTION)
	 */
	, initServices: function() {
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
		this.services = new Array();
		this.services['getUrlForExecutionService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_URL_FOR_EXECUTION_ACTION'
			, baseParams: params
		});
		
		this.services['sendFeedback']= Sbi.config.serviceRegistry.getRestServiceUrl({
			serviceName: 'documents/sendFeedback',
			baseParams: params
		});
		
		this.services['saveDocumentService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_DOCUMENT_ACTION'
			, baseParams: {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE',
				MESSAGE_DET: 'MODIFY_GEOREPORT'
			}
		});
	}


	/**
	 * @method 
	 * 
	 * Initialize the GUI
	 */
	, init: function( config, doc ) {
		this.initMiframe(config, doc);
	}
	
	
	/**
	 * @method 
	 * 
	 * Initialize the iframe
	 */
	, initMiframe: function( config, doc ) {
		var listeners = this.initMiframeListeners();
		
		this.miframe = new Ext.ux.ManagedIframePanel({
			region:'center'
	        , frameConfig : {
				// setting an initial iframe height in IE, to fix resize problem
				autoCreate : Ext.isIE ? {style: 'height:500'} : { },
				disableMessaging : false
	        }
			, defaultSrc: 'about:blank'
	        , loadMask  : true
	        //, fitToParent: true  // not valid in a layout
	        , disableMessaging :false
	        , listeners: listeners
	        , border : false
	    });
		
		if(doc.refreshSeconds !== undefined && doc.refreshSeconds > 0){
			this.refr = function(seconds) {
						this.miframe.getFrame().setSrc( null ); // refresh the iframe with the latest url
						this.refr.defer(seconds*1000, this,[seconds]);
					}
			this.refr.defer(doc.refreshSeconds*1000, this,[doc.refreshSeconds]);
		}	
	}
	
	/**
	 * @method 
	 * 
	 * Initialize iframe's listeners
	 */
	, initMiframeListeners: function() {
		var listeners = {
			scope: this	
		};

		listeners['message:subobjectsaved'] = this.initSubObjectSavedMessageListner();
		listeners['message:contentexported'] = this.initContentExportedMessageListner();
    	listeners['message:worksheetexporttaberror'] = this.initWorksheetExportTabErrorMessageListner();
		listeners['message:crossnavigation'] = this.initCrossNavigationaMessageListner();
		listeners['message:closeDocument'] = this.closeDocumentListener();
		listeners['message:managebutton'] = this.initManageButton();
		listeners['message:sendFeedback'] = this.initSendFeedbackListner();
		listeners['message:modifyGeoReportDocument'] = this.initModifyGeoReportDocumentListner();


		
		listeners['domready'] = this.initDomReadyListner();
		listeners['documentloaded'] = this.initDocumentLoadedListner();
        listeners['resize'] = this.initResizeListner();
	
		return listeners;
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:subobjectsaved'
	 */
	, initManageButton: function() {
		return {
    		fn: function(srcFrame, message) {
	        	this.fireEvent("managebutton",message.data.button, message.data.property, message.data.value, message.data.target) 
    		}
    		, scope: this
    	};
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:subobjectsaved'
	 */
	, initSubObjectSavedMessageListner: function() {
		return {
    		fn: function(srcFrame, message) {
	        	// call metadata open window
    			if(message.data.id != null && message.data.id){
    				//this.shortcutsPanel.synchronizeSubobjectsAndOpenMetadata(message.data.id, this.executionInstance);
    				alert("Saved subobject");
    			}    
    		}
    		, scope: this
    	};
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:sendFeedback'
	 */
	, initSendFeedbackListner: function() {
		return {
    		fn: function(srcFrame, message) {
    			Sbi.debug('[DocumentPage.SendFeedbackListner] : IN' );

				var paramsToSend = {};
				if (message.data != undefined){
					
					paramsToSend.msg = message.data.msg;
					paramsToSend.label = message.data.label;
				}
				
				
				Ext.Ajax.request({
			        url: this.services['sendFeedback'],
			        params: paramsToSend,
			        success: function(response, options) {
			      		if(response !== undefined && response.responseText !== undefined) {
			      			var content = Ext.util.JSON.decode( response.responseText );
			      			if(content !== undefined) {
			      				if(content.errors !== undefined && content.errors.length > 0) {
			      					this.state = 'FAILURE';
									Sbi.exception.ExceptionHandler.handleFailure(response);
			      				} else {
			      					this.state = 'LOADING';
									Sbi.exception.ExceptionHandler.showInfoMessage(LN('sbi.geo.controlpanel.feedback.sendOK'));
			      				}
			      			} 
			      		} else {
			      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
			      		}
			        },
			        scope: this,
					failure: Sbi.exception.ExceptionHandler.handleFailure      
			   });
				
    		}
    		, scope: this
    	};
		Sbi.debug('[DocumentPage.SendFeedbackListner] : OUT' );

	}
	/**
	 * @method
	 * 
	 * init the listner for event 'message:modifyGeoReportDocument'
	 */
	,initModifyGeoReportDocumentListner: function() {
	return {
		fn: function(srcFrame, message) {
			Sbi.debug('[DocumentPage.ModifyGeoReportDocumentListner] : IN' );

			
			var theWindow = this.miframe.iframe.getWindow();
			Sbi.debug('[DocumentPage.ModifyGeoReportDocumentListner]: got window');
			
			var label;
			//information to send to the SaveDocumentAction
			if (message.data != undefined){
				
				label = message.data.label;
			}
			
			var template;
			if (theWindow.geoReportPanel != null) {
				template = theWindow.geoReportPanel.validate();
			}
			
			var paramsToSend = {
					'template': template,
					//'model_name': this.modelName,
					'typeid': 'MAP',
					'label': label
			};
			
			
			
			//TODO: invocazione request di salvataggio SaveDocumentAction
			Ext.Ajax.request({
		        url: this.services['saveDocumentService'],
		        params: paramsToSend,
		        success: function(response, options) {
		      		if(response !== undefined && response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content !== undefined) {
		      				if(content.errors !== undefined && content.errors.length > 0) {
		      					this.state = 'FAILURE';
								Sbi.exception.ExceptionHandler.handleFailure(response);
		      				} else {
		      					this.state = 'LOADING';
								Sbi.exception.ExceptionHandler.showInfoMessage('Document saved' );
		      				}
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
		   });
			
		}
		, scope: this
	};
	Sbi.debug('[DocumentPage.ModifyGeoReportDocumentListner] : OUT' );

	}
	/**
	 * @method
	 * 
	 * init the listner for event 'message:closeDocument'
	 */
	, closeDocumentListener: function(){
		return {
	    	fn: function(srcFrame, message) {
	        	if (this.loadMask != null) {
	        		this.hideMask();
	        	}  
	        	this.fireEvent('closeDocument', message.data);
	    	}
	    	, scope: this
	    };
	} 
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:contentexported'
	 */
	, initContentExportedMessageListner: function() {
		return {
	    	fn: function(srcFrame, message) {
	        	if (this.loadMask != null) {
	        		this.hideMask();
	        	}  
	    	}
	    	, scope: this
	    };
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:worksheetexporttaberror'
	 */
	, initWorksheetExportTabErrorMessageListner: function() {
		return {
	    	fn: function(srcFrame, message) {
	        	if (this.loadMask != null) {
	        		this.hideMask();
	        	}  
	    	}
	    	, scope: this
	    };
	}
	
	
	/**
	 * @method
	 * 
	 * init the listner for event 'message:crossnavigation'
	 */
	, initCrossNavigationaMessageListner: function() {
		return {
	    	fn: function(srcFrame, message){
	    		Sbi.trace('[DocumentPage.listeners(message:crossnavigation)]: IN');
	    		//alert('[DocumentPage.listeners(message:crossnavigation)]: IN');
	           	var config = {
	           		document: {'label': message.data.label}
	       			, preferences: {
	           			parameters: message.data.parameters
	           		  , subobject: {'name': message.data.subobject}
	           		}
	       	    };
	       	    if(message.data.target !== undefined){
	       	    	config.target = message.data.target;
	       	    }
	       	    
	       	    if(message.data.title !== undefined){
	       	    	config.title = message.data.title;
	       	    }
	       	    
	       	    if(message.data.width !== undefined){
	       	    	config.width = message.data.width;
	       	    }
	       	    
	       	    if(message.data.height !== undefined){
	       	    	config.height = message.data.height;
	       	    }
	       	    
	           	// workaround for document composition with a svg map on IE: when clicking on the map, this message is thrown
	           	// but we must invoke execCrossNavigation defined for document composition, only if it's not an external cross navigation
	           	if (Ext.isIE && this.executionInstance.document.typeCode == 'DOCUMENT_COMPOSITE') {	         
	           		if (message.data.typeCross !== undefined && message.data.typeCross === "EXTERNAL"){
	           			this.fireEvent('crossnavigation', config);
	           		}
	           		else {
	           			srcFrame.dom.contentWindow.execCrossNavigation(message.data.windowName, message.data.label, message.data.parameters);
	           		}
	           	} else {
	           		this.fireEvent('crossnavigation', config);
	           	}
	           	Sbi.trace('[DocumentPage.listeners(message:crossnavigation)]: OUT');
	    	}
	    	, scope: this
		};
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'domready'
	 */
	, initDomReadyListner: function() {
		return {
			fn: function(srcFrame, message) {
		

				//Only for OLAP Documents
				if (this.executionInstance != null &&  this.executionInstance.document.typeCode == 'OLAP') {
					//intercept click on <input> elements and show load  mask
					srcFrame.getDoc().on('click',function(){ frame.showMask() },this,     {delegate:'input[type=image]'});
				}
				
				//intercept click and extend SpagoBI session
				srcFrame.getDoc().on('click', this.extendSession, this);
			}
			, scope: this
		};
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'documentloaded'
	 */
	, initDocumentLoadedListner: function(frame) {
		return {
			fn: function(frame) {
				
				if (this.miframe.iframe.getDocumentURI() !== 'about:blank'){
					if (this.miframe.iframe.execScript){
						this.miframe.iframe.execScript("parent = document;", true);
					}
		  		}
				
				this.execScript(this.EXEC_CROSSNAVIGATION_FN_BODY, true);
				this.execScript("uiType = 'ext';", true);
				
				// iframe resize when iframe content is reloaded
				if (Ext.isIE) {
					var aFrame = this.miframe.getFrame();
					aFrame.dom.style.height = this.miframe.getSize().height - 6;
				}
				
				frame.hideMask();
				this.state = 'LOADED';
			}
		    , scope: this
	   };
	}
	
	/**
	 * @method
	 * 
	 * init the listner for event 'resize'
	 */
	, initResizeListner: function() {
		return {
			fn: function(frame) {
				if (Ext.isIE) {
					var aFrame = this.miframe.getFrame();
					// work-around: during cross navigation to a third document, this.miframe.getSize().height is 0
					// therefore this check is necessary in order to avoid height less than 0, that causes side effects in IE
					if (this.miframe.getSize().height > 6) {
						aFrame.dom.style.height = this.miframe.getSize().height - 6;
					}
				}
			}
		    , scope: this
	   };
	}
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // synchronization methods
	// -----------------------------------------------------------------------------------------------------------------
	// This methods change properly the interface according to the specific execution instance passed in
	
	/**
	* @param {Object} executionInstance the execution configuration
	* 
	 * @method
	 */
	, synchronize: function( executionInstance ) {
		
		Sbi.debug('[DocumentPage.synchronize] : IN' );
		
		if(this.fireEvent('beforesynchronize', this, executionInstance, this.executionInstance) !== false){
			this.executionInstance = executionInstance;
		
			Sbi.trace('[DocumentPage.synchronize]: Executing document with these parameters: ' + this.executionInstance.PARAMETERS);
			
			// check if output type is defined
			var typeCode = executionInstance.document.typeCode;
			var outputType = null;
			if(executionInstance.PARAMETERS){
				try {
					var parameters =  JSON.parse(executionInstance.PARAMETERS); // Produces a SyntaxError
					if(parameters.outputType){
						outputType = parameters.outputType;
						if(outputType != null){
							outputType = outputType.toUpperCase();
						}
					}
				} catch (error) {}
			}
			
			Ext.Ajax.request({
		        url: this.services['getUrlForExecutionService'],
		        params: this.executionInstance,
		        success: function(response, options) {
		      		if(response !== undefined && response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content !== undefined) {
		      				if(content.errors !== undefined && content.errors.length > 0) {
		      					this.state = 'FAILURE';
		      					this.fireEvent('loadurlfailure', content.errors);
		      				} else {
		      					Sbi.trace('[DocumentPage.synchronize]: Url for execution is equal to: ' + content.url);
		      					
		      					
		      					if(outputType == null || outputType == 'HTML' || outputType == 'HTM' || outputType == 'PDF'){
		      						// if output type is not specified or is HTML or PDF normal execution
		      						this.state = 'LOADING';
		      						this.miframe.getFrame().setSrc( content.url );	
		      					}
		      					else{
		      						// else make direct export (only in cases where direct export is provided)
		      						if(typeCode=='REPORT' || typeCode=='MAP' || typeCode=='DATAMART' || typeCode=='OLAP'){
		      							this.fireEvent("directExport", outputType, content.url, typeCode); 
		      						}
		      						else{
		      							// else proceede with normal execution
		      	 						this.state = 'LOADING';
			      						this.miframe.getFrame().setSrc( content.url );	
		      						}
		      					}

		      				}
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
		        },
		        scope: this,
				failure: Sbi.exception.ExceptionHandler.handleFailure      
		   });
			
			Sbi.debug('[DocumentPage.synchronize] : OUT' );
		}
	}
	
	
	
	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------
	

	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	
	//this.addEvents(
	/**
     * @event eventone
     * Fired when ...
     * @param {Sbi.execution.DocumentPage} this
     * @param {Ext.Toolbar} ...
     */
	//'eventone'
	/**
     * @event eventtwo
     * Fired before ...
     * @param {Sbi.execution.DocumentPage} this
     * @param {Object} ...
     */
	//'eventtwo'
	//);	
});