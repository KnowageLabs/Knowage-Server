/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.widgets.WizardPanel', {
	extend: 'Ext.panel.Panel'
	, layout : 'card'
	, config:{
		/**
	     * @property {Array} pages
	     * The pages that compose this wizard
	     */
		pages: null

		/**
	     * @property {Array} buttons
	     * The buttons that compose this wizard button bar
	     */
		, buttons: null

		, pageToActivateOnRender: null
		, border: false
		, activeItem : 0
		, autoScroll : true
	}

	, constructor : function(config) {
		Sbi.trace("[WizardPanel.constructor]: IN");
//		this.initConfig(config);
//		var defaultSettings = {
//			border: false,
//
//			activeItem : 0,
//			autoScroll : true
//		};

//		if(Sbi.settings && Sbi.widgets && Sbi.widgets.wizardPanel) {
//			defaultSettings = Ext.apply(defaultSettings,  Sbi.widgets.wizardPanel);
//		}
//		var c = Ext.apply(defaultSettings, config || {});
//		var c = Ext.apply({}, config || {});
//		Ext.apply(this, c);
//
		this.init();

//		c = Ext.apply(c, {
//			items: this.pages,
//			buttons: this.buttons
//		});

		this.callParent(arguments);
		this.addEvents('navigate', 'cancel', 'submit');

		Sbi.trace("[WizardPanel.constructor]: OUT");
	}

	,  initComponent: function() {
	        Ext.apply(this, {
//	            bbar: this.buttons,
	            items: this.pages
	           , buttons: this.buttons
	        });
	        this.callParent();
    }

	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getActivePage: function() {
		return this.layout.activeItem;
	}

	, getActivePageNumber: function() {
//		if(!this.layout.activeItem) {
//			Sbi.trace("[WizardPanel.getActivePageNumber]: [" + Sbi.toSource(this.layout, true) +  "]");
//			this.layout.setActiveItem(0);
//		}
		return this.layout.activeItem.itemId;
	}

	, getPageNumber: function(page) {
		if(page) return page.itemId;
		return -1;
	}

	, getPageCount: function() {
		return this.items.length;
	}

	, isValidPageNumber: function(pageNumber) {
		return pageNumber >= 0 && pageNumber <= this.getPageCount();
	}

	, getPage: function(pageNumber) {
		var page = null;
		if(Sbi.isValorized(pageNumber) && this.isValidPageNumber(pageNumber)) {
			page = this.pages[pageNumber];
		}
		return page;
	}

	, moveToNextPage: function() {
		var activePageNumber = this.getActivePageNumber();
		return this.moveToPage(activePageNumber+1);
	}

	, moveToPreviousPage: function() {
		var activePageNumber =  this.getActivePageNumber();
		return this.moveToPage(activePageNumber-1);
	}

	, moveToPage: function(targetPageNumber){
		Sbi.trace("[WizardPanel.moveToPage]: IN");

		if(this.rendered === false) {
			Sbi.trace("[WizardPanel.moveToPage]: Wizard not yet rendered");
			this.pageToActivateOnRender = targetPageNumber;
			this.on("afterrender", function() {
				Sbi.trace("[WizardPanel.afterrender]: IN");
				if(this.pageToActivateOnRender !== null) {
					this.moveToPage(this.pageToActivateOnRender);
					this.pageToActivateOnRender = null;
				}
				Sbi.trace("[WizardPanel.afterrender]: OUT");
			}, this);
			Sbi.trace("[WizardPanel.moveToPage]: OUT");
			return;
		}

		Sbi.trace("[WizardPanel.moveToPage]: target page number is equal to [" + targetPageNumber + "]");
		var activePageNumber =  this.getActivePageNumber();
		var totPageNumber  = this.getPageCount()-1;
		var isTabValid = true;

		if(this.isValidPageNumber(targetPageNumber) === false) {
			return;
		}

		Sbi.trace("[WizardPanel.moveToPage]: target page number is valid");

		if (this.doMoveToPageValidation(targetPageNumber)){

			Sbi.trace("[WizardPanel.moveToPage]: target page is valid");

			this.layout.setActiveItem(targetPageNumber);
			//Ext.getCmp('move-prev').setDisabled(targetPageNumber==0);
			this.backButton.setDisabled(targetPageNumber==0);
			//Ext.getCmp('move-next').setDisabled(targetPageNumber==totPageNumber);
			this.nextButton.setDisabled(targetPageNumber==totPageNumber);
		 	//Ext.getCmp('submit').setVisible(!(parseInt(targetPageNumber)<parseInt(totPageNumber)));
		 	this.submitButton.setVisible(!(parseInt(targetPageNumber)<parseInt(totPageNumber)));

		} else {
			var messages = this.getMoveToPageValidationErrorMessages(targetPageNumber);
			Sbi.exception.ExceptionHandler.showWarningMessage(Sbi.toSource(messages), "Impossible to move to page [" + targetPageNumber + "]");
			Sbi.trace("[WizardPanel.moveToPage]: target page is not valid");
		}

		Sbi.trace("[WizardPanel.moveToPage]:Page [" + this.getActivePageNumber() + "] is now the active page");

		Sbi.trace("[WizardPanel.moveToPage]: OUT");

		var activePage = this.getActivePage();
		// pass the current wizard state to the active page so it cat refresh if needed
		// its content. This is useful if some info contained in the active page depends uppon
		// values inserted by user in some other page of the wizard
		if (activePage.updateValues){
			var wizardState = this.getWizardState(false);
			activePage.updateValues(wizardState);
		}

		return activePage;
	}


	, getMoveToPageValidationErrorMessages: function(destinationPageNumber){
		Sbi.trace("[WizardPanel.messages]: IN");
		var messages = null;
		for(var i = 0; i < destinationPageNumber; i++) {
			var msg = this.getPageValidationErrorMessages(i);
			if(msg !== null) {
				if(messages === null) {
					messages = msg;
				} else {
					messages.addAll(msg);
				}
			}
		}

		Sbi.trace("[WizardPanel.messages]: OUT");
		return messages;
	}

	/**
	 * @returns true if the move to the specified page is valid. false otherwise. In general a
	 * the move is valid if all the pages that came before the destination page are valid
	 */
	, doMoveToPageValidation: function(destinationPageNumber){
		Sbi.trace("[WizardPanel.doMoveToPageValidation]: IN");
		var isValidMove = true;
		for(var i = 0; i < destinationPageNumber; i++) {
			isValidMove = isValidMove && this.doPageValidation(i);
		}

		Sbi.trace("[WizardPanel.doMoveToPageValidation]: OUT");
		return isValidMove;
	}

	, getPageValidationErrorMessages: function(pageNumber) {
		Sbi.trace("[WizardPanel.getPageValidationErrorMessages]: IN");


		var page = this.getPage(pageNumber);
		if(Sbi.isNull(page)) {
			return "Page [" + i + "] does not exist";
		}

		var messages = null;
		if(Sbi.isValorized(page.getValidationErrorMessages)) {
			messages = page.getValidationErrorMessages(pageNumber);
		}

		Sbi.trace("[WizardPanel.getPageValidationErrorMessages]: OUT");
		return messages;
	}

	/**
	 * @returns true if the specified page is valid. false otherwise
	 */
	, doPageValidation: function(pageNumber) {
		Sbi.trace("[WizardPanel.doPageValidation]: IN");

		var page = this.getPage(pageNumber);
		if(Sbi.isNull(page)) {
			return false;
		}

		var isPageValid = true;
		if(Sbi.isValorized(page.isValid)) {
			isPageValid = isPageValid && page.isValid();
		}

		isPageValid = isPageValid && this.isPageValid(page);

		Sbi.trace("[WizardPanel.doPageValidation]: OUT");
		return isPageValid;
	}

	/**
	 * @method
	 * @abstract
	 *
	 * TODO override it in subclasses
	 *
	 * Validate the target page
	 */
	, isPageValid: function(page) {
		return true;
	}

	, getPageState: function(page) {
		var state = {};
		if(page && page.applyPageState) {
			state = page.applyPageState(state);
		}
		return state;
	}

	, setPageState: function(page, state) {
		if(page && page.setPageState) {
			page.setPageState(state);
		}
	}

	// running is true if checking state in order to execute
	, getWizardState: function(running) {
		Sbi.trace("[WizardPanel.getWizardState]: IN");
		var state = {};
		for(var i = 0; i < this.getPageCount(); i++) {
			var page = this.getPage(i);
			if(page.applyPageState) {
				state = page.applyPageState(state, running);
				Sbi.trace("[WizardPanel.getWizardState]: apply page [" + i + "] state - " + Sbi.toSource(state));
			}
		}

		Sbi.trace("[WizardPanel.getWizardState]: state is equal to [" + Sbi.toSource(state) + "]");

		Sbi.trace("[WizardPanel.getWizardState]: OUT");
		return state;
	}

	, setWizardState: function(state) {
		Sbi.trace("[WizardPanel.setWizardState]: IN");

		for(var i = 0; i < this.getPageCount(); i++) {
			var page = this.getPage(i);
			if(page.setPageState) {
				page.setPageState(state);
			}
		}

		Sbi.trace("[WizardPanel.setWizardState]: OUT");
	}

	, resetWizardState: function(state) {
		for(var i = 0; i < this.getPageCount(); i++) {
			var page = this.getPage(i);
			if(page.resetPageState) {
				page.resetPageState();
			}
		}
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	/**
	 * @method
	 *
	 * Initialize the GUI
	 */
	, init: function() {
		Sbi.trace("[WizardPanel.init]: IN");
		this.initPages();
		this.initButtons();
		Sbi.trace("[WizardPanel.init]: OUT");
	}

	/**
	 * @method
	 * @abstract
	 *
	 * TODO override it in subclasses
	 *
	 * Initialize the pages contained in this wizard
	 */
	, initPages: function(){
		Sbi.trace("[WizardPanel.initPages]: IN");

		this.pages = new Array();


		var page1 = new Ext.Panel({
			itemId: 0
			, html: 'Page 1'
		});
		this.pages.push(page1);

		var page2 = new Ext.Panel({
			itemId: 1
			, html: 'Page 2'
		});
		this.pages.push(page2);

		var page3 = new Ext.Panel({
			itemId: 2
			, html: 'Page 3'
		});
		this.pages.push(page3);

		Sbi.trace("[WizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function() {
		Sbi.trace("[WizardPanel.initButtons]: IN");

		var buttonsBar = [];

		buttonsBar.push('->');

		this.backButton = Ext.create('Ext.Button', {
			text: LN('sbi.ds.wizard.back')
	        , handler: this.onMovePrevious
	        , scope: this
	        , disabled: (this.activeItem == 0)?true:false
	    });

		buttonsBar.push(this.backButton);

		this.nextButton = Ext.create('Ext.Button', {
			text:  LN('sbi.ds.wizard.next')
	        , handler: this.onMoveNext
	        , scope: this
	        , disabled: (this.activeItem == 0)?false:true
	    });
		buttonsBar.push(this.nextButton);

		this.submitButton = Ext.create('Ext.Button', {
			hidden: true
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
	    });
		buttonsBar.push(this.submitButton);

		this.applyButton = Ext.create('Ext.Button', {
			hidden: true
	        , text:  LN('sbi.ds.wizard.apply')
	        , handler: this.onApply
	        , scope: this
	    });
		buttonsBar.push(this.applyButton);

		this.cancelButton = Ext.create('Ext.Button', {
			text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });
		buttonsBar.push(this.cancelButton);

		this.buttons = buttonsBar;

		Sbi.trace("[WizardPanel.initButtons]: OUT");

		return buttonsBar;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // private methods
	// -----------------------------------------------------------------------------------------------------------------

	, onMoveNext: function() {
		Sbi.trace("[WizardPanel.onMoveNext]: IN");
		var page = this.moveToNextPage();
		Sbi.trace("[WizardPanel.onMoveNext]: OUT");
	}

	, onMovePrevious: function() {
		Sbi.trace("[WizardPanel.onMovePrevious]: IN");
		var page  = this.moveToPreviousPage();
		Sbi.trace("[WizardPanel.onMovePrevious]: OUT");
	}

	, onCancel: function() {
		Sbi.trace("[WizardPanel.onCancel]: IN");
		var page = this.fireEvent('cancel', this);
		Sbi.trace("[WizardPanel.onCancel]: OUT");
	}

	, onApply: function() {
		Sbi.trace("[WizardPanel.onApply]: IN");
		this.fireEvent('apply', this);
		Sbi.trace("[WizardPanel.onApply]: OUT");
	}

	, onSubmit: function() {
		Sbi.trace("[WizardPanel.onSubmit]: IN");
		this.fireEvent('submit', this);
		Sbi.trace("[WizardPanel.onSubmit]: OUT");
	}
});
