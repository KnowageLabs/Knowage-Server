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

Sbi.browser.mexport.MassiveExportWizard = function(config) {

	var defaultSettings = {
			title: LN('sbi.browser.mexport.massiveExportWizard.title')
			, layout: 'fit'
			, width: 500
			, height: 380           	
			, closable: true
			, constrain: true
			, hasBuddy: false
			, resizable: true
	};
	if (Sbi.settings && Sbi.settings.browser 
			&& Sbi.settings.browser.mexport && Sbi.settings.browser.mexport.massiveExportWizard) {
		defaultSettings = Ext.apply(defaultSettings, Sbi.settings.browser.mexport.massiveExportWizard);
	}
	
	var c = Ext.apply(defaultSettings, config || {});	
	Ext.apply(this, c);

	this.services = this.services || new Array();
	

	//this.addEvents();
	this.initMainPanel(c);	
	c = Ext.apply(c, {
		layout: 'fit'
		, items: [this.mainPanel]	
	});

	// constructor
	Sbi.browser.mexport.MassiveExportWizard.superclass.constructor.call(this, c);
	
	//this.on('render', function() {this.pages[this.activePageNumber].fireEvent('select').defer(5000);}, this);
	
	//this.addEvents();
};

Ext.extend(Sbi.browser.mexport.MassiveExportWizard, Ext.Window, {

	services: null
    , mainPanel: null
    , btnPrev: null
	, btnNext: null
	, btnFinish: null
	, btnCancel: null
	
	, activePageNumber: null
	, pages: null
    , optionsPage: null
    , parametersPage: null
    , triggerPage : null
    
    , functId: null
	, executionInstances: null
    
	// ----------------------------------------------------------------------------------------
	// public methods
	// ----------------------------------------------------------------------------------------
	
	/**
	 * @returns whether this wizard could be finished without further user interaction. 
	 * Typically, this is used by the wizard to enable or disable the Finish button.
	 */
	, canFinish: function() {
		//return this.activePageNumber == this.PARAMETERS_PAGE_NUMBER;
		return this.activePageNumber == this.pages.length - 1;
	}
    
	/**
	 * Called by the wizard when the Finish button is pressed.
	 */
	, performFinish: function() {
		var params = {
				functId : this.functId
				, type : 'WORKSHEET'
		};
		
		//cycle on page to get contents
		for(i = 0; i < this.pages.length; i++){
			var page = this.pages[i];
			var name = page.getName();
			var content = page.getContent();
			params[name] = Sbi.commons.JSON.encode(content); // Ext.util.JSON.encode(content);
		}
		
		// Start massive export
		Ext.Ajax.request({
		        url: this.services['performFinishAction']// this.services['startMassiveExportThreadAction']
		        , params: params
		        , success : function(response, options){
		        	Ext.MessageBox.show({
						title: 'Success',
						msg: 'Execution of documents contained in selected folder has been succesfully scheduled',
						modal: true,
						buttons: Ext.MessageBox.OK,
						width:500,
						icon: Ext.MessageBox.INFO,
						animEl: 'root-menu'        			
		        	});
		        	this.close();
		        }
				, failure: Sbi.exception.ExceptionHandler.handleFailure      
				, scope: this
		});
		
	}
	
	/**
	 * Called by the wizard when the Cancel button is pressed.
	 */
	, performCancel: function() {
		this.close();
	}
	
	, setPageTitle: function(title, description) {
		description = description || '';
		this.titlePanel.body.update('<H1>' + title +'</H1><P>&nbsp;&nbsp;&nbsp;' + description +'</P>');
	}
	
    // ----------------------------------------------------------------------------------------
	// private methods
	// ----------------------------------------------------------------------------------------
	
    , initMainPanel: function(c) {
    	this.initButtons();
		this.initPages()
		this.titlePanel = new Ext.Panel({
			region: 'north',
			height: 45,
			frame: true,
			html: '<H1>'+LN('Sbi.browser.mexport.MassiveExportWizardOptionsPage.options')+'</H1><P>&nbsp;&nbsp;&nbsp;'+LN('sbi.browser.mexport.massiveExportWizardOptionsPage.title')+'</P>'
		});
		this.pagePanel = new Ext.Panel({
			layout: 'card',  
			activeItem: 0,  
			scope: this,
			height: 420,
			autoWidth: true,
			resizable: true,
			defaults: {border:false},  
			bbar: [
			       '->'
			       , this.btnPrev
			       , this.btnNext
			       , '-'
			       , this.btnFinish
			       , '-'
			       , this.btnCancel
			], 
			items: this.pages
		});
		
		this.mainPanel = new Ext.Panel({
			layout: 'border'
			, items: [this.titlePanel, new Ext.Panel({
				layout:'fit', 
				region: 'center',
				border: false,
				bodyBorder: false,
				items: [this.pagePanel]
			})]
		});
		
		this.mainPanel.doLayout();
		
		this.activePageNumber = 0;
    }

    , initButtons: function() {
    	this.btnPrev = new Ext.Button({
	        text: LN('sbi.browser.mexport.massiveExportWizard.button.back')
	        , handler: this.moveToPreviousPage
	        , scope: this
	        , disabled : true
		});
	
		this.btnNext = new Ext.Button({
	        text: LN('sbi.browser.mexport.massiveExportWizard.button.next')
	        , handler: this.moveToNextPage
	        , scope: this
	        , disabled : false
		});
	
		this.btnFinish = new Ext.Button({
	        text: LN('sbi.browser.mexport.massiveExportWizard.button.finish')
	        , handler: this.performFinish
	        , scope: this
	        , disabled: true
		});
		
		this.btnCancel = new Ext.Button({
	        text: LN('sbi.browser.mexport.massiveExportWizard.button.cancel')
	        , handler: this.performCancel
	        , scope: this
		});
    }
    
	, initPages: function() {
		this.pages = [];
		this.initOptionsPage();
		this.initParametersPage();
		if(this.wizardType === 'schedule') {
			this.initTriggerPage();
		}
	}
	
	, initOptionsPage: function() {
		this.optionsPage = new Sbi.browser.mexport.MassiveExportWizardOptionsPage({wizard:this, functId: this.functId});	
		this.pages.push(this.optionsPage);
	}
	
	, initParametersPage: function() {
		this.parametersPage = new Sbi.browser.mexport.MassiveExportWizardParametersPage({wizard:this, functId: this.functId});
		this.pages.push(this.parametersPage);
	}
	
	, initTriggerPage: function() {
		this.triggerPage = new Sbi.browser.mexport.MassiveExportWizardTriggerPage({wizard:this, functId: this.functId});
		this.pages.push(this.triggerPage);
	}


	, moveToPreviousPage: function() {
		this.moveToPage(this.activePageNumber - 1);
	}
	
	, moveToNextPage: function() {
		this.moveToPage(this.activePageNumber + 1);
	}

	, moveToPage: function(page) {
		
		if(page < 0 || page >= this.pages.length) {
			alert('Page number [' + page + '] out of range [0,' + (this.pages.length-1) + ']');
		}
		
		this.pages[this.activePageNumber].fireEvent('unselect');
		this.pagePanel.layout.setActiveItem(page);
		this.activePageNumber = page;
		this.pages[this.activePageNumber].fireEvent('select');
		
		this.updateButtons(page);
	}
	
	, updateButtons: function(page) {
		if(page === 0) {
			this.btnPrev.disable();
		} else {
			this.btnPrev.enable();
		}
		
		if(page === (this.pages.length -1)) {
			this.btnNext.disable();
		} else {
			this.btnNext.enable();
		}
				
		if(this.canFinish() === true) {
			this.btnFinish.enable();
		} else {
			this.btnFinish.disable();
		}
	}

});