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
 * - Giulio gavardi (giulio.gavardi@eng.it)
 */

Ext.ns("Sbi.browser.mexport");

Sbi.browser.mexport.MassiveExportWizardParametersPage = function(config) {

	var defaultSettings = {
			//title: LN('sbi.browser.mexport.massiveExportWizardParametersPage.title')  //LN('Sbi.browser.mexport.massiveExportWizardParametersPage.title')
			layout: 'fit'
			//, width: 500
			//, height: 300   
			, frame: true
			, closable: true
			, constrain: true
			, hasBuddy: false
			, resizable: true
	};
	if (Sbi.settings && Sbi.settings.browser 
			&& Sbi.settings.browser.mexport && Sbi.settings.browser.mexport.massiveExportWizardParametersPage) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.browser.mexport.massiveExportWizardParametersPage);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);

	this.services = this.services || new Array();

	var params = {LIGHT_NAVIGATOR_DISABLED: 'TRUE', SBI_EXECUTION_ID: null, TYPE: 'WORKSHEET'};
	this.services['StartMassiveExportExecutionProcessAction'] = this.services['StartMassiveExportExecutionProcessAction'] || Sbi.config.serviceRegistry.getServiceUrl({
		serviceName: 'START_MASSIVE_EXPORT_EXECUTION_PROCESS_ACTION'
		, baseParams: new Object()
	});	
	
	this.initMainPanel(c);	
	c = Ext.apply(c, {
		layout: 'fit'
		, items: [this.mainPanel]	
	});

	// constructor
	Sbi.browser.mexport.MassiveExportWizardParametersPage.superclass.constructor.call(this, c);
	
	this.addEvents('select', 'unselect');
	
	this.on('select', this.onSelection, this);
	this.on('unselect', this.onDeselection, this);	
};

Ext.extend(Sbi.browser.mexport.MassiveExportWizardParametersPage, Ext.Panel, {

	services: null
    , mainPanel: null
    , currentPage: null
    
    
	// ----------------------------------------------------------------------------------------
	// public methods
	// ----------------------------------------------------------------------------------------

	, onSelection: function() {
		this.currentPage = true;
		this.wizard.setPageTitle(LN('Sbi.browser.mexport.MassiveExportWizardParametersPage.parameters'), LN('Sbi.browser.mexport.MassiveExportWizardParametersPage.title'));
		
		// reset the fields in case you are coming to panel for the second time
		this.mainPanel.reset();

		// create ExecutionInstances and  get parameters 
		var selectedRole = this.getPreviousPage().getSelectedRole();	
	
		var params = {
			selectedRole : selectedRole
			, functId : this.functId
			, type : 'WORKSHEET'						
		}				
		this.createExecutionInstances(params);
	}
	
	, onDeselection: function() {
		this.currentPage = false;
	}
	
	, isTheCurrentPage: function() {
		return this.currentPage;
	}
	
	, getPageIndex: function() {
		var i;		
		for(i = 0; i < this.wizard.pages.length; i++) {
			if(this.wizard.pages[i] == this) break;
		}		
		return i;
	}
	
	, getPreviousPage: function() {
		var pages = this.wizard.pages;
		var i = this.getPageIndex();
		return (i != 0)? this.wizard.pages[i-1]: null;
	}
	
	, getNextPage: function() {
		var pages = this.wizard.pages;
		var i = this.getPageIndex();
		return (i != (pages.length-1))? this.wizard.pages[i+1]: null;
	}
	
	, getName: function(){
		return 'Sbi.browser.mexport.MassiveExportWizardParametersPage';
	}
	
	/**
	 * returns the value selected of the parameters in parametersPanel,
	 * and for each also the objparameterId (for label rinomination: name  => nameB)
	 */
	, getContent: function() {
		var state;
		
		state = {};
		for(p in this.mainPanel.fields) {
			var field = this.mainPanel.fields[p];
			var value = field.getValue();
			state[field.name] = value;
			var rawValue = field.getRawValue();
			if(value == "" && rawValue != ""){
				state[field.name] = rawValue;
			}
			if (rawValue !== undefined) {
				state[field.name + '_field_visible_description'] = rawValue;
			}
			
			// add objParsId information if present (massive export case)
			if(field.objParameterIds){
				for(pr=0;pr < field.objParameterIds.length;pr++){
					val = field.objParameterIds[pr];
					state[val+ '_objParameterId']=field.name;
				}
			}
			
			if(field.allowBlank==false){
				state[field.name+'_isMandatory']=true;
			}
			else{
				state[field.name+'_isMandatory']=false;
			}
			
		}
		return state;
	}
	
    // ----------------------------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------------------------

    , initMainPanel: function() {
    	var services = new Array();
		var params = {
				LIGHT_NAVIGATOR_DISABLED: 'TRUE'
				, SBI_EXECUTION_ID: null
					   
		};
		
		services['getParametersForExecutionService'] = Sbi.config.serviceRegistry.getServiceUrl({
			serviceName: 'GET_ANALYTICAL_DRIVER_FROM_DOCS_IN_FOLDER_ACTION'
			, baseParams: params
		});
	
		var config = {
			services : services	
			, contest : 'massiveExport'
			, drawHelpMessage : false	
			, columnNo: 2
			, columnWidth: 240
			, labelAlign: 'top'
			, fieldWidth: 215
			//, fieldLabelWidth: 100
		};
		
		this.mainPanel = new Sbi.execution.ParametersPanel(config);
    }
    
    , createExecutionInstances: function(params) {
    	
		params = Ext.apply(params, {modality: 'CREATE_EXEC_CONTEST_ID_MODALITY'});
		
		Ext.Ajax.request({
	        url: this.services['StartMassiveExportExecutionProcessAction'],
	        params: params,	        	 
	        success : function(response, options) {
	        	if(response !== undefined) {   
	        		
	        		if(response.responseText !== undefined) {
	        			var content = Ext.util.JSON.decode( response.responseText );
	        			if(content !== undefined) {
		      				this.executionInstances = {
		      					SBI_EXECUTION_ID: content.execContextId
		      				};
		      		  		for(p in this.mainPanel.fields){
		      		  			var field = this.mainPanel.fields[p];
		      		  			field.enable();
		      		  		}
		      		  		this.wizard.btnFinish.enable();
		      				params = Ext.apply(params, this.executionInstances);
		      				this.mainPanel.synchronize(params);
	        			} 
		      		} else {
		      			Sbi.exception.ExceptionHandler.showErrorMessage('Server response is empty', 'Service Error');
		      		}	
	        		
	        	} else {
	        		//clear preceding store if error happened
	        		for(p in this.mainPanel.fields){
		  			var field = this.mainPanel.fields[p];
		  			field.disable();
		  		}
		  		this.wizard.btnFinish.disable();
		  	}
	      },
	        scope: this,
			failure: Sbi.exception.ExceptionHandler.handleFailure      
	   });
	}	
	
});