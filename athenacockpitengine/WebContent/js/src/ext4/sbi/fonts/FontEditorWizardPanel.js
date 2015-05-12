/** SpagoBI, the Open Source Business Intelligence suite

 * Copyright (C) 2012 Engineering Ingegneria Informatica S.p.A. - SpagoBI Competency Center
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0, without the "Incompatible With Secondary Licenses" notice.
 * If a copy of the MPL was not distributed with this file, You can obtain one at http://mozilla.org/MPL/2.0/. **/

Ext.define('Sbi.fonts.FontEditorWizardPanel', {
		extend: 'Sbi.widgets.WizardPanel'

	, config:{
		fonts: null
	  , frame: false
	  , border: false
	}

	/**
	 * @property {Sbi.fonts.editor.main.FontEditorPage} fontEditorPage
	 * The page that manages font editing
	 */
	, fontEditorPage: null

	, constructor : function(config) {
		Sbi.trace("[FontEditorWizardPanel.constructor]: IN");
		this.initConfig(config);
		this.callParent(arguments);
		Sbi.trace("[FontEditorWizardPanel.constructor]: OUT");
	}


	// =================================================================================================================
	// METHODS
	// =================================================================================================================

	// -----------------------------------------------------------------------------------------------------------------
    // public methods
	// -----------------------------------------------------------------------------------------------------------------

	, getFontEditorPage: function() {
		return this.fontEditorPage;
	}

	// -----------------------------------------------------------------------------------------------------------------
    // init methods
	// -----------------------------------------------------------------------------------------------------------------

	, initPages: function(){
		Sbi.trace("[FontEditorWizardPanel.initPages]: IN");

		this.pages = new Array();

		this.initFontEditorPage();
		this.pages.push(this.fontEditorPage);

		Sbi.trace("[FontEditorWizardPanel.initPages]: font editor page succesfully adedd");

		Sbi.trace("[FontEditorWizardPanel.initPages]: OUT");

		return this.pages;
	}

	, initButtons: function(){
		Sbi.trace("[FontEditorWizardPanel.initButtons]: IN");

		this.buttons = new Array();

		this.buttons.push('->');

		this.buttons.push({
			id: 'submit'
			, hidden: false
	        , text:  LN('sbi.ds.wizard.confirm')
	        , handler: this.onSubmit
	        , scope: this
	    });

		this.buttons.push({
			id: 'cancel'
	        , text:  LN('sbi.ds.wizard.cancel')
	        , handler: this.onCancel
	        , scope: this
	    });

		Sbi.trace("[FontEditorWizardPanel.initButtons]: fonts editor buttons succesfully adedd");

		Sbi.trace("[FontEditorWizardPanel.initButtons]: OUT");

		return this.buttons;
	}

	, initFontEditorPage: function() {
		Sbi.trace("[FontEditorWizardPanel.initFontEditorPage]: IN");
		this.fontEditorPage = Ext.create('Sbi.fonts.editor.main.FontEditorPage',{
			  fonts: this.fonts
		});
		Sbi.trace("[FontEditorWizardPanel.initFontEditorPage]: IN");
		return this.fontEditorPage;
	}



	// -----------------------------------------------------------------------------------------------------------------
    // utility methods
	// -----------------------------------------------------------------------------------------------------------------

});
