/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.filters.FilterEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
	   storesList: null
	  , filters: null
	  , frame: false
	  , border: false
	}

	/**
	 * @property {Sbi.filters.editor.main.FilterEditorPage} filterEditorPage
	 * The page that manages filters editing
	 */
	, filterEditorPage: null

	, constructor : function(config) {
		Sbi.trace("[FilterEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[FilterEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFilterEditorPage: function() {
		return this.filterEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[FilterEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initFilterEditorPage();
		this.pages.push(this.filterEditorPage);

		Sbi.trace("[FilterEditorWizardPanel.initPages]: filter editor page succesfully adedd");

		Sbi.trace("[FilterEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[FilterEditorWizardPanel.initButtons]: IN");

		this.buttons = new Array();

		this.buttons.push('->');

		this.buttons.push({
			id: 'submit'
			, hidden: false
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
//	        , disabled: (this.activeItem == 0)?false:true
//	        , disabled: true
	    });

		this.buttons.push({
			id: 'cancel'
	        , text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });

		Sbi.trace("[FilterEditorWizardPanel.initButtons]: association editor buttons succesfully adedd");

		Sbi.trace("[FilterEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initFilterEditorPage: function() {
		Sbi.trace("[FilterEditorWizardPanel.initFilterEditorPage]: IN");
		this.filterEditorPage = Ext.create('Sbi.filters.editor.main.FilterEditorPage',{
			storesList: this.storesList
		  , filters: this.filters
		});
		Sbi.trace("[FilterEditorWizardPanel.initFilterEditorPage]: IN");
		return this.filterEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
