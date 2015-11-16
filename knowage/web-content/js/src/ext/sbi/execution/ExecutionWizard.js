/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice. 
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.ns("Sbi.execution");

Sbi.execution.ExecutionWizard = function(config, doc) {
	
	// init properties
	this.baseConfig = config;
	this.document = doc;
	this.isFromCross = config.isFromCross || false;
	
	this.addEvents('executionfailure', 'beforetoolbarinit', 'documentexecutionpageinit');
	
	this.initServices();
	this.initRoleSelectionPage(config, doc);
	this.initDocumentExecutionPage(config, doc);
	this.activePageNumber = this.ROLE_SELECTION_PAGE_NUMBER;
	
	var c = Ext.apply({}, config, {
		layout:'card',
		border : false,
		hideMode: !Ext.isIE ? 'nosize' : 'display',
		activeItem: this.activePanel || 0,
		items: [
		 this.roleSelectionPage
	   , this.documentExecutionPage
		 //, this.errorPage 
		]		        
	});
	
	// constructor
    Sbi.execution.ExecutionWizard.superclass.constructor.call(this, c);
};

/**
 * @class Sbi.execution.ExecutionWizard
 * @extends Ext.Panel
 * 
 * A panel that manage the execution of a single document. Document execution is a process composed by these three steps: 
 * role selection, parameters selection and document visualization. For each of these steps the ExecutionWizard have a dedicated
 * page (Sbi.execution.RoleSelectionPage and Sbi.execution.DocumentExecutionPage). The main
 * responsability of this class is to manage the execution workflow (i.e. the transition between the three pages mentioned above)
 */

/**
 * @cfg {Object} config
 * tbd
 */

/**
 * @cfg {Object} doc
 * tbd
 */
Ext.extend(Sbi.execution.ExecutionWizard, Ext.Panel, {
    
	// =================================================================================================================
	// PROPERTIES
	// =================================================================================================================
	
	/**
     * @property {Array} services
     * This array contains all the services invoked by this class
     */
	services: null
	
	/**
     * @property {Object} baseConfig
     * Base configuration
     */
	, baseConfig: null	
	
	/**
     * @property {Object} executionInstance
     * The execution instance object. It contains all the informations related to the execution of this document
     */
    , executionInstance: null
    
    /**
     * @property {Boolean} isFromCross
     * true if this document is teh destination of a cross navigation. false otherwise
     */
    , isFromCross: null
    
    /**
     * @property {Number} prevActivePageNumber
     * The number of previously active page (0 for roleSelectionPage and 1 for documentExecutionPage)
     */
    , prevActivePageNumber: null
    
    /**
     * @property {Number} activePageNumber
     * The number of active page (0 for roleSelectionPage and 1 for documentExecutionPage)
     */
    , activePageNumber: null
    
    /**
     * @property {Sbi.execution.RoleSelectionPage} roleSelectionPage
     * The page used for role selection
     */
    , roleSelectionPage: null
    
   
    /**
     * @property {Sbi.execution.ExecutionPage} documentExecutionPage
     * The page used for document execution
     */
    , documentExecutionPage: null 
    //, errorPage: null 
    
    /**
     * @property {Object} ROLE_SELECTION_PAGE_NUMBER
     * The number of role selection page. 0 by default
     */
    , ROLE_SELECTION_PAGE_NUMBER: 0 
    
	/**
     * @property {Object} EXECUTION_PAGE_NUMBER
     * The number of execution  page. 1 by default
     */
	, EXECUTION_PAGE_NUMBER: 1 
	//, ERROR_PAGE_NUMBER: 2
    
	// =================================================================================================================
	// METHODS
	// =================================================================================================================
	
	// -----------------------------------------------------------------------------------------------------------------
    // accessor methods
	// -----------------------------------------------------------------------------------------------------------------
	
	
	/**
	 * @method
	 * Returns the role selection page
	 
	 * @return {Sbi.execution.RoleSelectionPage} The role selection page
	 */
	, getRoleSelectionPage: function() {
		return this.roleSelectionPage;
	}
	
	/**
	 * @method
	 * Returns the document execution page
	 
	 * @return {Sbi.execution.DocumentExecutionPage} the document execution page
	 */
	, getDocumentExecutionPage: function() {
		return this.documentExecutionPage;
	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------
	
	/**
	 * @method 
	 * 
	 * Initialize the following services exploited by this component:
	 * 
	 *    - startExecutionService: start the execution of the document (by default START_EXECUTION_PROCESS_ACTION);
	 *    - getParametersForExecutionService: get the parameters associated to the document to execute (by default GET_PARAMETERS_FOR_EXECUTION_ACTION);
	 *    - showSendToForm: ??? (by default SHOW_SEND_TO_FORM);
	 *    - saveIntoPersonalFolder: save a reference to executed document in personal folder (by default SAVE_PERSONAL_FOLDER).
	 */
	, initServices: function() {
		var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null};
		
		this.services = this.services || new Array();
		
		this.services['startExecutionService'] = this.services['startExecutionService'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'START_EXECUTION_PROCESS_ACTION'
			, baseParams: params
		});
		
		this.services['getParametersForExecutionService'] = this.services['getParametersForExecutionService'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_PARAMETERS_FOR_EXECUTION_ACTION'
			, baseParams: params
		});
		
		this.services['showSendToForm'] = this.services['showSendToForm'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SHOW_SEND_TO_FORM'
			, baseParams: params
		});
		
		this.services['saveIntoPersonalFolder'] = this.services['saveIntoPersonalFolder'] || Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'SAVE_PERSONAL_FOLDER'
			, baseParams: params
		});
	}

	/**
	 * @method 
	 * 
	 * Initialize the role selection page
	 */
	, initRoleSelectionPage: function(config, doc) {
		var roleSelectionPageConfig = Ext.applyIf({}, config.preferences);
		this.roleSelectionPage = new Sbi.execution.RoleSelectionPage(roleSelectionPageConfig, doc);
		this.roleSelectionPage.maskOnRender = true;
		
		// 20100505: set if coming from tree or list of documents
		if (config.preferences){
			if(config.preferences.fromDocTreeOrList){
				if(config.preferences.fromDocTreeOrList == true){			
					this.roleSelectionPage.callFromTreeListDoc = true;
				}
			}
			if(config.preferences.fromMyAnalysis){
				if(config.preferences.fromMyAnalysis == true){		
					this.roleSelectionPage.fromMyAnalysis = true;
				}
			}
		}
		
		this.roleSelectionPage.on('movetonextpagerequest', this.moveToNextPage, this);
		this.roleSelectionPage.on('beforetoolbarinit', function(page, toolbar){
			this.fireEvent('beforetoolbarinit', toolbar);
		}, this);
		// 20100505
		this.roleSelectionPage.on('movetoadminpagerequest', this.backToAdmin, this);
		
		 this.roleSelectionPage.on('synchronize', this.onRolesForExecutionLoaded, this);
		 this.roleSelectionPage.on('synchronizeexception', this.onRolesForExecutionLoadException, this);    	
	    
		return this.roleSelectionPage;
	}
	
	/**
	 * @method 
	 * 
	 * Initialize the parameter selection page
	 */
	, initDocumentExecutionPage: function(config, doc) {
		// propagate preferences to parameters selection page
		var documentExecutionPageConfig = Ext.applyIf({isFromCross: this.isFromCross}, config.preferences);
		// 20100505: set if coming from tree or list of documents
		if (config.preferences){
			if(config.preferences.fromDocTreeOrList){
				if(config.preferences.fromDocTreeOrList == true){		
					documentExecutionPageConfig.callFromTreeListDoc = true;
				}
			}
			if(config.preferences.fromMyAnalysis){
				if(config.preferences.fromMyAnalysis == true){		
					documentExecutionPageConfig.fromMyAnalysis = true;
				}
			}
		}
		this.documentExecutionPage =  new Sbi.execution.DocumentExecutionPage(documentExecutionPageConfig || {}, this.document);
		this.documentExecutionPage.maskOnRender = true;

		this.documentExecutionPage.on('movetoprevpagerequest', this.moveToPreviousPage, this);
		this.documentExecutionPage.on('movetonextpagerequest', this.moveToNextPage, this);
		this.documentExecutionPage.on('beforetoolbarinit', function(page, toolbar){
			this.fireEvent('beforetoolbarinit', toolbar);
		}, this);
		
		// 20100505
		this.documentExecutionPage.on('movetoadminpagerequest', this.backToAdmin, this);
		
		return this.documentExecutionPage;
	}
	
//	/**
//	 * @method 
//	 * 
//	 * Initialize the document execution page
//	 */
//	, initDocumentExecutionPage: function() {
//		var documentExecutionPageConfig = Ext.applyIf({maskOnRender: true}, this.baseConfig.preferences);
//		// preferences for shortcuts ARE NOT PROPAGATED to execution page (since panels on ShortcutPanel are instantiated twice, this may generate conflicts)
//		if (documentExecutionPageConfig !== undefined) {
//			delete documentExecutionPageConfig.subobject;
//			delete documentExecutionPageConfig.snapshot;
//		}
//		if (this.documentExecutionPage.isParameterPanelReadyForExecution === true) {
//			documentExecutionPageConfig.hideParametersPanel = true; 
//		}
//		
//		this.fireEvent('documentexecutionpageinit');
//	}
	
	// -----------------------------------------------------------------------------------------------------------------
    // public  methods
	// -----------------------------------------------------------------------------------------------------------------
	
    /**
     * Move to the next page that is equal to the current active page number plus one
	 * @method 
	 */
    , moveToPreviousPage: function() {
    	this.moveToPage( this.activePageNumber-1 );
	}
    
    /**
     * Move to the previous page that is equal to the current active page number minus one
	 * @method 
	 */
    , moveToNextPage: function() {
    	this.moveToPage( this.activePageNumber+1 );
	}
    
	/**
	 * @method 
	 * 
	 * Move to the specified page number
	 * @param {Number} pageNumber the page number to move to
	 */
    , moveToPage: function(pageNumber) {
		
		this.prevActivePageNumber = this.activePageNumber;
		this.activePageNumber = pageNumber;
	
		// up-hill ->
		if(this.prevActivePageNumber == this.ROLE_SELECTION_PAGE_NUMBER && this.activePageNumber == this.EXECUTION_PAGE_NUMBER) {
		    this.roleSelectionPage.loadingMask.hide();
			this.startExecution();
		}
		
		// down-hill ->
		if(this.prevActivePageNumber == this.EXECUTION_PAGE_NUMBER && this.activePageNumber == this.ROLE_SELECTION_PAGE_NUMBER) {
			// just change page. Do nothinh else
		}
				
		this.getLayout().setActiveItem( this.activePageNumber );
	
	}
	
	// 20100505
    /**
	 * @method 
	 */
	, backToAdmin: function(){
		// build url to go back one page
		var serviceRegistry = Sbi.config.serviceRegistry;
		
		var urlToCall = Sbi.config.serviceRegistry.getBaseUrlStr({
			//isAbsolute :  true
		});		

		urlToCall = urlToCall+'?LIGHT_NAVIGATOR_BACK_TO=1';		
		
		//alert(urlStr);
		window.location=urlToCall;
    }
	
    // execution
	/**
	 * @method 
	 */
    , execute : function() {
    	if (this.roleSelectionPage.loadingMask) this.roleSelectionPage.loadingMask.show();
//    	this.roleSelectionPage.loadingMask.show();
		if(!this.document || (!this.document.id && !this.document.label) ) {
			Sbi.exception.ExceptionHandler.showErrorMessage(LN('sbi.execution.error.nodocid'), 'Intenal Error');
		}
		
		this.executionInstance = {}
		if(this.document.id) this.executionInstance.OBJECT_ID = this.document.id;
		if(this.document.label) this.executionInstance.OBJECT_LABEL = this.document.label;
		if(this.document.docVersion) this.executionInstance.OBJECT_VERSION = this.document.docVersion;
		this.executionInstance.document = this.document;
		this.executionInstance.isFromCross = this.isFromCross;
		
		this.loadRolesForExecution();
	}

    /**
	 * @method 
	 */
	, loadRolesForExecution: function() {
		this.roleSelectionPage.synchronize( this.executionInstance );
	}
	

	/**
	 * @method 
	 */
	, onRolesForExecutionLoaded: function(form, store, records, options) {
		var rolesNo = store.getCount();
		if(rolesNo === 0) {
			alert(LN('sbi.execution.error.novalidrole'));
		} else if(rolesNo === 1) {
			var role = store.getRange()[0];
			form.roleComboBox.setValue(role.data.name); 
			this.executionInstance.isPossibleToComeBackToRolePage = false;
			this.moveToNextPage();
		} else {
			this.roleSelectionPage.loadingMask.hide();
		}
	}
	
	/**
	 * @method 
	 */
	, onRolesForExecutionLoadException: function(form, store) {
		this.moveToPage(3);
		this.roleSelectionPage.loadingMask.hide();
	}
	
	/**
	 * @method 
	 */
	, startExecution: function() {
		var role = this.roleSelectionPage.getSelectedRole();
		this.executionInstance.ROLE = role;
		
		Ext.Ajax.request({
	          url: this.services['startExecutionService'],
	          params: this.executionInstance,
	          callback : function(options , success, response){
	    	  	if(success && response !== undefined) {   
		      		if(response.responseText !== undefined) {
		      			var content = Ext.util.JSON.decode( response.responseText );
		      			if(content !== undefined) {
		      				this.onExecutionStarted(content.execContextId);
		      				
		      			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}
	    	  	}
	          },
	          scope: this,
	  		  failure: Sbi.exception.ExceptionHandler.handleFailure      
	     });
	}
	
	/**
	 * @method 
	 */
	, onExecutionStarted: function( execContextId ) {
		this.executionInstance.SBI_EXECUTION_ID = execContextId;
		this.documentExecutionPage.synchronize(this.executionInstance);
	}
	
	/**
	 * @method 
	 */
	, onLoadUrlFailure: function ( errors ) {
		var messageBox = Ext.MessageBox.show({
				title: 'Error',
				msg: errors,
				modal: false,
				buttons: Ext.MessageBox.OK,
				width:300,
				icon: Ext.MessageBox.ERROR,
				animEl: 'root-menu'        			
		});
		if(this.prevActivePageNumber !== this.EXECUTION_PAGE_NUMBER){
			this.moveToPage( this.prevActivePageNumber ); 
		}		
	}
	
	// =================================================================================================================
	// EVENTS
	// =================================================================================================================
	
	//this.addEvents(
	/**
    * @event executionfailure
    * Fired when button clicked.
    * @param {Ext.master.Switch} this
    * @param {Number} times The number of times clicked.
    */
	//'executionfailure', 
	/**
     * @event beforetoolbarinit
     * Fired when button clicked.
     * @param {Ext.master.Switch} this
     * @param {Number} times The number of times clicked.
     */
	 //'beforetoolbarinit', 
	 /**
      * @event documentexecutionpageinit
      * Fired when button clicked.
      * @param {Ext.master.Switch} this
      * @param {Number} times The number of times clicked.
      */
	  //'documentexecutionpageinit'
	  //);
	
});
